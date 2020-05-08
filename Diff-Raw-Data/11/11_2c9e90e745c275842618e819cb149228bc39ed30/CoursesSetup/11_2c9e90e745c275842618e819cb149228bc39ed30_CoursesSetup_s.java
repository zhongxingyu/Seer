 
 package edu.acu.files;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.Persistence;
 import org.apache.log4j.Logger;
 import com.xythos.common.api.VirtualServer;
 import com.xythos.common.api.XythosException;
 import com.xythos.fileSystem.DirectoryEntry;
 import com.xythos.security.api.Context;
 import com.xythos.security.api.PrincipalManager;
 import com.xythos.security.api.UserBase;
 import com.xythos.storageServer.admin.api.AdminUtil;
 import com.xythos.storageServer.api.CreateDirectoryData;
 import com.xythos.storageServer.api.FileSystem;
 import com.xythos.storageServer.api.FileSystemDirectory;
 import com.xythos.storageServer.api.FileSystemEntry;
 import com.xythos.storageServer.permissions.api.DirectoryAccessControlEntry;
 import edu.acu.wip.model.Course;
 import edu.acu.wip.model.Dropbox;
 import edu.acu.wip.model.Person;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.persistence.EntityManagerFactory;
 
 /**
  *
  * @author cjs00c
  */
 public class CoursesSetup {
 
 	/**
 	 * Custom logger for this class
 	 */
 	public static Logger log = Logger.getLogger(CoursesSetup.class);
 
 	/**
 	 * ACU Model connection factory
 	 */
 	private static EntityManagerFactory emf = null;
 	
 	/**
 	 * Course folder structure
 	 */
 	public static String coursesBasePath = "/courses/";
 	public static String courseDirName = null;
 	public static String classInfoDirName = "Class Info";
 	public static String groupsDirName = "Groups";
 	public static String dropboxDirName = "Dropbox";
 	public static String profWorkDirName = "Prof Workspace";
 	public static String returnDirName = "Return";
 	public static String studentsDirName = "Students";
 	public static String trashDirName = "Trash";
 
 	/**
 	 * Common resources
 	 */
 	private static VirtualServer vs = null;
 	private static Context adminContext = null;
 	private static FileSystemDirectory courseDir = null;
 	private static FileSystemDirectory classInfoDir = null;
 	private static FileSystemDirectory groupsDir = null;
 	private static FileSystemDirectory dropboxDir = null;
 	private static FileSystemDirectory returnDir = null;
 	private static FileSystemDirectory profWorkDir = null;
 	private static FileSystemDirectory studentsDir = null;
 	private static FileSystemDirectory trashDir = null;
 	//private static EntityManager em = null;
 
 	/**
 	 * Main sets up the required resources and passes them to processActions().
 	 * If any of the resources fail to be initialized the program should die
 	 * immediately because nothing else will work.
 	 * 
 	 * @param args
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
 		log.info("***********************************************");
 		log.info("**       Automated Course Folders Setup      **");
 		log.debug("**                (debug mode)               **");
 		log.info("***********************************************");
 		
 		// Set up resources
 		try {
 			log.debug("Creating Xythos resources");
 			vs = VirtualServer.getDefaultVirtualServer();
 			
 			// Get an admin context
 			adminContext = AdminUtil.getContextForAdmin("CoursesSetup");
 			
 			log.debug("Initializing the acu-model");
 			emf = Persistence.createEntityManagerFactory("acu-model-custom");
 			EntityManager em = emf.createEntityManager();
 			
 			// Get a list of dropbox actions to perform
 			log.debug("Getting a list of actions to process");
 			List<Dropbox> dropboxActions = null;
 			try {
 				dropboxActions = em.createQuery("SELECT d FROM Dropbox d WHERE d.completed = false").getResultList();
 				log.info(dropboxActions.size()+" Dropbox Actions Found");
 			} catch (Exception e) {
 				log.error("Problem getting the dropbox actions; Is the data correct?", e);
 			}
 			
 			// Process dropbox actions
 			if (dropboxActions != null) {
 				processActions(dropboxActions);
 			}
 		} catch (Exception e) {
 			log.error("Something bad happened", e);
 		}
 		log.info("-- Completed Course Folders Setup --");
 	}
 	
 	
 	/**
 	 * Determines what action needs to be taken and executes the appropriate method.
 	 * Each action is tried individually so that if one fails the rest can complete.
 	 * 
 	 * @param dropboxActions
 	 */
 	private static void processActions(List<Dropbox> dropboxActions) {
 		log.debug("** Processing dropbox actions **");
 		
 		// Loop throgh all the actions
 		for (Dropbox dropboxActionId : dropboxActions) {
 			// Start Transaction
 			EntityManager em = emf.createEntityManager();
 			em.getTransaction().begin();
 			
 			// Get a dropbox action managed by the EM
 			Dropbox dropboxAction = (Dropbox)em.createQuery("SELECT d FROM Dropbox d WHERE d.id = "+dropboxActionId.getId()).getSingleResult();
 			
 			try {
 				log.debug("");
 				log.debug("Beginning dropbox action #"+dropboxAction.getId()+": "+dropboxAction.getAction().toString()+" "+
 						dropboxAction.getCourseRank().getRank()+" "+dropboxAction.getPerson().getEmailId()+" to "+
 						dropboxAction.getCourse().getCourseId());
 
 				// Get User
 				log.debug("Looking up user "+dropboxAction.getPerson().getEmailId());
				UserBase user = PrincipalManager.findUser(dropboxAction.getPerson().getEmailId(), null);
 				
 				// Get Master Course
 				log.debug("Determining the master course");
 				Course masterCourse = dropboxAction.getCourse().getParent();
 				if (masterCourse == null)
 					masterCourse = dropboxAction.getCourse();
 				
 				// Format the course title
 				String courseTitle = masterCourse.getTitle().replaceAll("[\\\\|/|:|\\*|\\?|\"|<|>|\\|]", "-");
 				if (!courseTitle.equals(masterCourse.getTitle())) {
 					log.debug("Formatted course title from: '"+masterCourse.getTitle()+"' to: '"+courseTitle+"'");
 				}
 
 				// Course Directory
 				log.debug("Getting the course directory in Xythos");
 				String courseName = masterCourse.getCourseId() + " - " + courseTitle;
 				String coursePath = coursesBasePath + courseName + "/";
 				courseDir = getFolder(coursesBasePath, courseName, false);
 				boolean newCourse = false;
 				if (courseDir == null) {
 					log.info("Setting up a new course: " + courseName);
 					newCourse = true;
 					courseDir = getFolder(coursesBasePath, courseName, true);
 				}
 				
 				
 				
 				/**** Perform Action ****/
 				
 				String action = dropboxAction.getAction().toString();
 				
 				// Drop action
 				if (action.equalsIgnoreCase("drop")) {
 					dropPerson(user);
 				}
 				
 				// Add action
 				else if (action.equalsIgnoreCase("add")) {
 					// Get course directories (creates them if needed)
 					classInfoDir = getFolder(coursePath, classInfoDirName);
 					groupsDir = getFolder(coursePath, groupsDirName);
 					dropboxDir = getFolder(coursePath, dropboxDirName);
 					returnDir = getFolder(coursePath, returnDirName);
 					profWorkDir = getFolder(coursePath, profWorkDirName);
 					studentsDir = getFolder(coursePath, studentsDirName);
 
 					// Setup trash
 					if (newCourse) {
 						setupTrash(coursePath);
 					}
 					trashDir = getFolder(coursePath, trashDirName);
 
 					
 					// Student
 					if (dropboxAction.getCourseRank().getRank().equalsIgnoreCase("stud")) {
 
 						// Get/create student dir
 						log.debug("Getting student dir: " + user.getID() + " - " + user.getDisplayName());
 						FileSystemDirectory studentDir = getFolder(coursePath + studentsDirName + "/", user.getID() + " - " + user.getDisplayName());
 						log.debug("Setting trash dir to: " + trashDir.getName());
 						studentDir.changeTrashcan(trashDir.getName());
 
 						// Give student permissions to course folders
 						addStudent(user, studentDir);
 						
 						// Give instructors access to student directory
 						List<Person> instructors = getInstructors(em, dropboxAction);
 						for (Person person : instructors) {
 							log.debug(person.getEmailId() + " needs access to student directory");
 							UserBase instructor = null;
 							try {
 								instructor = PrincipalManager.findUser(person.getEmailId(), vs.getName());
 							} catch (XythosException e) {
 								log.error("Problem getting xythos userbase for "+person.getEmailId(), e);
 								em.getTransaction().setRollbackOnly();
 							}
 							
 							if (instructor != null) {
 								giveFullAccessToDirContent(instructor, studentDir);
 							} else {
 								log.error("Problem getting xythos userbase for "+person.getEmailId());
 								em.getTransaction().setRollbackOnly();
 								break;
 							}
 						}
 					}
 					
 					// Instructor
 					else if (dropboxAction.getCourseRank().getRank().equalsIgnoreCase("prof") || 
 							dropboxAction.getCourseRank().getRank().equalsIgnoreCase("asst")) {
 						addInstructor(user);
 					}
 				}
 				
 				// Invalid action
 				else {
 					log.error("Action '" + action + "' is not valid");
 				}
 				
 				
 				
 			/**** Commit Action ****/
 				
 			} catch (XythosException e) {
 				log.error("Problem with dropbox action " + dropboxAction.getId() + "; skipping it and moving on", e);
 				em.getTransaction().setRollbackOnly();
 			} finally {
 				// Persist the changes
 				try {
 					// Commit xythos changes
 					if (em.getTransaction().getRollbackOnly()) {
 						log.error("Rolling back the Xythos context");
 						adminContext.rollbackContext();
 					} else {
 						log.debug("Committing the changes to Xythos");
 						adminContext.commitContext();
 					}
 				} catch (XythosException e) {
 					log.error("Problem committing changes to Xythos", e);
 					em.getTransaction().setRollbackOnly();
 				} finally {
 					// Update Database
 					if (em.getTransaction().getRollbackOnly()) {
 						log.error("Rolling back the transaction");
 						em.getTransaction().rollback();
 					} else {
 						// Get current date/time
 						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 						Date now = new Date();
 						String date = dateFormat.format(now);
 						
 						// Mark Action as Completed
 						log.debug("Dropbox action #"+dropboxAction.getId()+" complete at "+date);
 						dropboxAction.setCompleted(true);
 						dropboxAction.setCompletedTime(date);
 
 						// Commit ACU Model Changes
						log.debug("Commiting the dropbox action as complete");
 						em.getTransaction().commit();
 					}	
 				}
 			}
 		}
 		log.debug("");
 		log.debug("** Finished processing dropbox actions **");
 	}
 	
 	
 	/**
 	 * Takes a user and removes all permissions on the course directory and all
 	 * entries it contains.
 	 * 
 	 * @param user
 	 * @throws XythosException
 	 */
 	private static void dropPerson(UserBase user) throws XythosException {
 		log.info("** Dropping "+user.getID()+" from "+courseDir.getName()+" **");
 		courseDir.deleteAccessControlEntryRecursive(user.getPrincipalID());
 	}
 	
 	
 	/**
 	 * Takes a student and gives them the appropriate permissions on the dropbox
 	 * directories and their own student directory.
 	 * 
 	 * @param user
 	 * @param studentDir
 	 * @throws com.xythos.common.api.XythosException
 	 */
 	private static void addStudent(UserBase user, FileSystemDirectory studentDir) throws XythosException {
 		log.info("** Adding student "+user.getID()+" to "+courseDir.getName()+" **");
 	
 		// Course directory - read
 		log.debug("  Setting permissions on "+courseDir.getName());
 		DirectoryAccessControlEntry courseDirAccess = (DirectoryAccessControlEntry)courseDir.getAccessControlEntry(user.getPrincipalID());
 		courseDirAccess.setAccessControlEntry(false, true, false, false, false, false, false, false, false);
 		
 		// Class Info directory - read/inherit-read
 		log.debug("  Setting permissions on "+classInfoDir.getName());
 		DirectoryAccessControlEntry classInfoDirAccess = (DirectoryAccessControlEntry)classInfoDir.getAccessControlEntry(user.getPrincipalID());
 		classInfoDirAccess.setAccessControlEntry(true, true, false, false, false, true, false, false, false);
 
 		// Groups directory - read
 		log.debug("  Setting permissions on "+groupsDir.getName());
 		DirectoryAccessControlEntry groupsDirAccess = (DirectoryAccessControlEntry)groupsDir.getAccessControlEntry(user.getPrincipalID());
 		groupsDirAccess.setAccessControlEntry(false, true, false, false, false, false, false, false, false);
 		
 		// Dropbox - read/write
 		// students should get read/write/delete access to their entry at time of drop
 		log.debug("  Setting permissions on "+dropboxDir.getName());
 		DirectoryAccessControlEntry dropboxDirAccess = (DirectoryAccessControlEntry)dropboxDir.getAccessControlEntry(user.getPrincipalID());
 		dropboxDirAccess.setAccessControlEntry(false, true, true, false, false, false, false, false, false);
 
 		// Students directory - read
 		log.debug("  Setting permissions on "+studentsDir.getName());
 		DirectoryAccessControlEntry studentsDirAccess = (DirectoryAccessControlEntry)studentsDir.getAccessControlEntry(user.getPrincipalID());
 		studentsDirAccess.setAccessControlEntry(false, true, false, false, false, false, false, false, false);
 		
 		// Student directory - full access
 		giveFullAccessToDirContent(user, studentDir);
 
 		// Trash directory - read/write/delete/inherit-write/inherit-delete
 		log.debug("  Setting permissions on "+trashDir.getName());
 		DirectoryAccessControlEntry trashDirAccess = (DirectoryAccessControlEntry)trashDir.getAccessControlEntry(user.getPrincipalID());
 		trashDirAccess.setAccessControlEntry(false, true, true, true, false, false, true, true, false);
 	}
 	
 	
 	/**
 	 * Takes an instructor and gives them read access to the course directory
 	 * and full access to all of the contents.
 	 * 
 	 * @param user
 	 * @throws XythosException
 	 */
 	private static void addInstructor(UserBase user) throws XythosException {
 		log.info("** Adding instructor "+user.getID()+" to "+courseDir.getName()+" **");
 		
 		// Course Directory - read
 		log.debug("  Setting permissions on "+courseDir.getName());
 		DirectoryAccessControlEntry courseDirAccess = (DirectoryAccessControlEntry)courseDir.getAccessControlEntry(user.getPrincipalID());
 		courseDirAccess.setAccessControlEntry(false, true, false, false, false, false, false, false, false);
 
 		// Class Info - read/write/inherit-read/inherit-write/inherit-delete
 		// contents: read/write/delete/inherit-read/inherit-write/inhereit-delete
 		log.debug("  Setting permissions on "+classInfoDir.getName());
 		giveFullAccessToDirContent(user, classInfoDir);
 
 		// Dropbox - read/inherit-read/inherit-write/inherit-delete
 		// contents: read/write/delete/inherit-read/inherit-write/inhereit-delete
 		log.debug("  Setting permissions on "+dropboxDir.getName());
 		DirectoryAccessControlEntry dropboxDirAccess = (DirectoryAccessControlEntry)dropboxDir.getAccessControlEntry(user.getPrincipalID());
 		dropboxDirAccess.setAccessControlEntry(true, true, true, true, false, true, true, true, false);
 		dropboxDirAccess.setWriteable(false);
 		dropboxDirAccess.setDeleteable(false);
 
 		// Groups - read/write
 		// contents: read/write/delete/permission/inherit-read/inherit-write/inherit-delete/inherit-permission
 		log.debug("  Setting permissions on "+groupsDir.getName());
 		DirectoryAccessControlEntry groupsDirAccess = (DirectoryAccessControlEntry)groupsDir.getAccessControlEntry(user.getPrincipalID());
 		groupsDirAccess.setAccessControlEntry(true, true, true, true, true, true, true, true, true);
 		groupsDirAccess.setDeleteable(false);
 		groupsDirAccess.setPermissionable(false);
 
 		// Prof Workspace - read/write/inherit-read/inherit-write/inherit-delete
 		// contents: read/write/delete/inherit-read/inherit-write/inherit-delete
 		log.debug("  Setting permissions on "+profWorkDir.getName());
 		giveFullAccessToDirContent(user, profWorkDir);
 
 		// Return - read/write/delete/inherit-read/inherit-write/inherit-delete
 		// contents: read/write/delete/inherit-read/inherit-write/inherit-delete
 		log.debug("  Setting permissions on "+returnDir.getName());
 		giveFullAccessToDirContent(user, returnDir);
 
 		// Students - read
 		log.debug("  Setting permissions on "+studentsDir.getName());
 		DirectoryAccessControlEntry studentsDirAccess = (DirectoryAccessControlEntry)studentsDir.getAccessControlEntry(user.getPrincipalID());
 		studentsDirAccess.setAccessControlEntry(false, true, false, false, false, false, false, false, false);
 
 		// Student Directories - read/write/delete/inherit-read/inherit-write/inherit-delete
 		// contents: read/write/delete/inherit-read/inherit-write/inherit-delete
 		FileSystemEntry[] contents = studentsDir.getDirectoryContents(false);
 		for (FileSystemEntry entry : contents) {
 			giveFullAccessToDirContent(user, (FileSystemDirectory)entry);
 		}
 
 		// Trash directory - read/inherit-read
 		log.debug("  Setting permissions on "+trashDir.getName());
 		DirectoryAccessControlEntry trashDirAccess = (DirectoryAccessControlEntry)trashDir.getAccessControlEntry(user.getPrincipalID());
 		trashDirAccess.setAccessControlEntry(true, true, true, true, false, true, true, true, false);
 	}
 
 
 	/**
 	 * Sets up the trashcan for the initial course creation
 	 *
 	 * @param coursePath
 	 */
 	public static void setupTrash(String coursePath) {
 
 		// Setup trash
 		String trashPath = coursePath + trashDirName;
 		try {
 			// Commit xythos changes (folders must exist before we can change their trashcans)
			log.debug("Commiting changes to Xythos");
 			adminContext.commitContext();
 
 
 			log.info("Setting course trashcan to: " + trashPath);
 			courseDir.changeTrashcan(trashPath);
 			classInfoDir.changeTrashcan(trashPath);
 			groupsDir.changeTrashcan(trashPath);
 			dropboxDir.changeTrashcan(trashPath);
 			returnDir.changeTrashcan(trashPath);
 			profWorkDir.changeTrashcan(trashPath);
 			studentsDir.changeTrashcan(trashPath);
 
 			if (courseDir instanceof DirectoryEntry) {
 				log.info("Creating trashcan " + trashPath);
 				((DirectoryEntry)courseDir).createTrashCan(trashPath);
 			}
 
 			// Commit xythos changes (folders must exist before we can change their trashcans)
			log.debug("Commiting changes to Xythos");
 			adminContext.commitContext();
 		} catch (XythosException e) {
 			log.error("Problem setting up course trashcan", e);
 		}
 	}
 	
 	
 	/**
 	 * Gives a user full access (read/write/delete/inherit-read/inherit-write/inherit-delete) to
 	 * the contents of a directory. The delete access is then removed from the directory itself.
 	 * 
 	 * @param user
 	 * @param dir
 	 * @throws XythosException
 	 */
 	private static void giveFullAccessToDirContent(UserBase user, FileSystemDirectory dir) throws XythosException {
 		log.debug("Giving "+user.getID()+" full access to contents of "+dir.getName());
 		
 		// Get access control
 		DirectoryAccessControlEntry dirAccess = null;
 		try {
 			dirAccess = (DirectoryAccessControlEntry)dir.getAccessControlEntry(user.getPrincipalID());
 		} catch (XythosException e) {
 			log.error("Problem getting directory access control", e);
 		}
 		
 		// Set access on passed directory
 		if (dirAccess != null) {
 			try {
 				// Give full access to everything in the directory recursively
 				dirAccess.setAccessControlEntry(true, true, true, true, false, true, true, true, false);
 				// Remove delete access from directory itself
 				dirAccess.setDeleteable(false);
 				log.debug(user.getID()+" now has full access to "+dir.getName());
 			} catch (XythosException e) {
 				log.error("Problem setting permissions on directory", e);
 			}
 		} else {
 			log.error("Unable to set permissions on directory - directory access control is null");
 		}
 	}
 	
 	
 	/**
 	 * Gets a list of instructors for a specific course. This includes
 	 * members with a rank of "PROF" or "ASST"
 	 * 
 	 * @param em
 	 * @param dropboxAction
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private static List<Person> getInstructors(EntityManager em, Dropbox dropboxAction) {
 		List<Person> instructors;
 		Course course = dropboxAction.getCourse();
 		Course masterCourse = course.getParent();
 		if (masterCourse == null) {
 			String query = "SELECT DISTINCT p FROM Person p, IN(p.courses) c, IN(p.courseRanks) cr " +
 				"WHERE c = :course AND cr.course = c AND (cr.rank = 'PROF' OR cr.rank = 'ASST')";
 			instructors = em.createQuery(query).setParameter("course", dropboxAction.getCourse()).getResultList();
 		} else {
 			String query = "SELECT DISTINCT p FROM Person p, IN(p.courses) c, IN(p.courseRanks) cr " +
 				"WHERE (c = :course OR c = :masterCourse) AND cr.course = c AND (cr.rank = 'PROF' OR cr.rank = 'ASST')";
 			instructors = em.createQuery(query).setParameter("course", course).setParameter("masterCourse", masterCourse).getResultList();
 		}
 		return instructors;
 	}
 	
 
 	/**
 	 * Looks up a given directory and returns it. If it doesn't already exist,
 	 * it is created.
 	 *
 	 * @param baseDirectory
 	 * @param directoryName
 	 * @return
 	 * @throws com.xythos.common.api.XythosException
 	 */
 	private static FileSystemDirectory getFolder(String baseDirectory, String directoryName) throws XythosException {
 		return getFolder(baseDirectory, directoryName, true);
 	}
 
 
 	/**
 	 * Looks up a given directory and returns it. Option to create the directory if it doesn't exist.
 	 * 
 	 * @param baseDirectory
 	 * @param directoryName
 	 * @param createDir
 	 * @return
 	 * @throws com.xythos.common.api.XythosException
 	 */
 	private static FileSystemDirectory getFolder(String baseDirectory, String directoryName, boolean createDir) throws XythosException {
 		FileSystemDirectory folder = (FileSystemDirectory)FileSystem.findEntry(vs, baseDirectory+directoryName, false, adminContext);
 		if (createDir && folder == null) {
 			log.debug("Creating directory: " + baseDirectory + directoryName);
 			CreateDirectoryData directoryData = new CreateDirectoryData(vs, baseDirectory, directoryName, adminContext.getContextUser().getPrincipalID());
 			folder = (FileSystemDirectory)FileSystem.createDirectory(directoryData, adminContext);
 		}
 		return folder;
 	}
 
 }
