 package edu.uci.lighthouse.core.util;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.JavaCore;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.core.preferences.DatabasePreferences;
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseEvent.TYPE;
 import edu.uci.lighthouse.model.LighthouseInterface;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseModelUtil;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.jpa.LHEntityDAO;
 import edu.uci.lighthouse.model.util.DatabaseUtility;
 import edu.uci.lighthouse.model.util.LHStringUtil;
 
 public class ModelUtility {
 
 	private static Logger logger = Logger.getLogger(ModelUtility.class);
 	
 	public static boolean belongsToImportedProjects(IFile iFile, boolean checkDatabase) {
 		IJavaElement jFile = JavaCore.create(iFile);
 		if (jFile != null) {
 			/* When the user is checking out, there is no working copy to compare with. So, we have to compare with the database to guarantee that imported projects existing in database can be checkout. */
 			if (checkDatabase) {
 				String clazzName = getClassFullyQualifiedName(iFile);
 				try {
 					LighthouseEntity entity = new LHEntityDAO().get(LHStringUtil.getMD5Hash(clazzName));
 					return (entity!=null);
 				} catch (Exception e) {
 					logger.info("Class: " + clazzName + " is not on the database");
 				}
 			}
 			String projectName = jFile.getJavaProject().getElementName();
 			LighthouseModel model = LighthouseModel.getInstance();
 			if (model.getProjectNames().contains(projectName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static List<String> getClassesFullyQualifiedName(
 			Map<IFile, ISVNInfo> svnFiles) {
 		LinkedList<String> result = new LinkedList<String>();
 		for (IFile iFile : svnFiles.keySet()) {
 			String fqn = getClassFullyQualifiedName(iFile);
 			if (fqn != null) {
 				result.add(fqn);
 			}
 		}
 		return result;
 	}
 
 	public static String getClassFullyQualifiedName(IFile iFile) {
 		String result = "";
 		try {
 			/*
 			 * When the Java file is out of sync with eclipse, get the fully
 			 * qualified name from ICompilationUnit doesn't work. So we decide
 			 * to do this manually, reading the file from the file system and
 			 * parsing it.
 			 */
 			String packageName = null;
 			BufferedReader d = new BufferedReader(new InputStreamReader(
 					new FileInputStream(iFile.getLocation().toOSString())));
 			while (d.ready()) {
 				String line = d.readLine();
 				if (line.contains("package")) {
 					String[] tokens = line.split("package\\s+|;");
 					for (String token : tokens) {
 						if (token.matches("[\\w\\.]+")) {
 							packageName = token;
 							break;
 						}
 					}
 					break;
 				}
 			}
 			/*
 			 * Java files should have at least one class with the same name of
 			 * the file
 			 */
 			String fileNameWithoutExtension = iFile.getName().replaceAll(
 					".java", "");
 			if (packageName == null) {
 				result = iFile.getProject().getName() + "." + fileNameWithoutExtension;
 			} else {
 				result = iFile.getProject().getName() + "." + packageName + "." + fileNameWithoutExtension;
 			}
 		} catch (Exception e) {
 			logger.error(e, e);
 		}
 
 		return result;
 	}
 	
 	public static void fireModificationsToUI(Collection<LighthouseEvent> events) {
 		logger.debug("fireModificationsToUI ("+events.size()+" events)");
 		// We need hashmap to avoid repaint the UI multiple times
 		HashMap<LighthouseClass, LighthouseEvent.TYPE> mapClassEvent = new HashMap<LighthouseClass, LighthouseEvent.TYPE>();
 		HashMap<LighthouseRelationship, LighthouseEvent.TYPE> mapRelationshipEvent = new HashMap<LighthouseRelationship, LighthouseEvent.TYPE>();
 		LighthouseModel model = LighthouseModel.getInstance();
 		for (LighthouseEvent event : events) {
 			Object artifact = event.getArtifact();
 			// TODO: comment more this method. It is confusing.
 			// ADD creates a new class node in the view and populates it
 			// MODIFY just re-populates the class node
 			if (artifact instanceof LighthouseClass) {
 				LighthouseClass klass = (LighthouseClass) artifact;
 				mapClassEvent.put(klass, event.getType());
 			} else if (artifact instanceof LighthouseEntity) {
 				LighthouseModelManager manager = new LighthouseModelManager(
 						model);
 				LighthouseClass klass = manager
 				.getMyClass((LighthouseEntity) artifact);
 				if (klass != null) {
 					// Never overwrite the ADD event
 					if (!mapClassEvent.containsKey(klass)) {
 						// Just refresh the class node
 						mapClassEvent.put(klass, LighthouseEvent.TYPE.MODIFY);
 					}
 				}
 			} else if (artifact instanceof LighthouseRelationship) {
 				LighthouseRelationship relationship = (LighthouseRelationship) artifact;
 				mapRelationshipEvent.put(relationship, event.getType());
 			}
 		}
 		// Fire class changes to the UI
 		for (Entry<LighthouseClass, TYPE> entry : mapClassEvent.entrySet()) {
 			logger.debug("Firing class: "+entry.getKey());
 			model.fireClassChanged(entry.getKey(), entry.getValue());
 		}
 		// Fire relationship changes to the UI
 		for (Entry<LighthouseRelationship, TYPE> entry : mapRelationshipEvent
 				.entrySet()) {
 			logger.debug("Firing relationship: "+entry.getKey());
 			model.fireRelationshipChanged(entry.getKey(), entry.getValue());
 		}
 	}
 	
 	/**Does not save in database, only in the lighthouse model*/
	//TODO (nilmax): Comment this method.
 	public static void updateEvents(Collection<LighthouseEvent> listEvents) {
 		LighthouseModelManager LhManager = new LighthouseModelManager(LighthouseModel.getInstance());
 		Collection<String> listClassesToRemove = new LinkedHashSet<String>(); 
 		// for each entity event
 		for (LighthouseEvent event : listEvents) {
 			Object artifact = event.getArtifact();
 			if (artifact instanceof LighthouseEntity) {
 				logger.debug("updating: " + event.toString());
 				LhManager.addEvent(event);
 				/**
 				 * if I have an event to remove a class/interface AND this class is not on
 				 * my workspace than I have to remove this class/interface from my model and
 				 * hence from my visualization
 				 */
 				if (event.getType()==TYPE.REMOVE) {
 					if (artifact instanceof LighthouseClass || artifact instanceof LighthouseInterface) {
 						LighthouseEntity entity = (LighthouseEntity) artifact;
 						String fqn = entity.getFullyQualifiedName();
						//TODO (nilmax): Why I need to verify if the class is not on my workspace?
 						if ( event.getAuthor().equals(Activator.getDefault().getAuthor()) 
								/*|| (Controller.getInstance().getWorkingCopy().get(fqn)==null)*/)  {
 							listClassesToRemove.add(fqn);
 						}
 					}
 				}
 			} else if (artifact instanceof LighthouseRelationship) {
 				if (event.getType()==TYPE.REMOVE) {
 					LighthouseRelationship rel = (LighthouseRelationship) artifact;
 					if (rel.getType()!=LighthouseRelationship.TYPE.INSIDE) {
 						LhManager.removeRelationship(rel);
 					}
 				}
 			}
 		}
 		// for each relationship event
 		for (LighthouseEvent event : listEvents) {
 			Object artifact = event.getArtifact();
 			if (artifact instanceof LighthouseRelationship) {
 				logger.debug("updating: " + event.toString());
 				if (event.getType()==TYPE.ADD) {
 					LhManager.addEvent(event);	
 				}
 			}
 		}
 		LhManager.removeArtifactsAndEvents(listClassesToRemove);
 	}
 
 	public static Collection<LighthouseEvent> getEventsForCommiting(Map<IFile, ISVNInfo> svnFiles/*List<String> listClazzFqn, Date svnCommittedTime, LighthouseAuthor author*/) {
 		// The commit time is equal for all the files in the map. So, let's pick the first one.
 		Date svnCommittedTime = svnFiles.values().toArray(new ISVNInfo[0])[0].getLastChangedDate();
		
 		LighthouseAuthor author = Activator.getDefault().getAuthor();
		
 		Collection<LighthouseEvent> listEvents = LighthouseModelUtil.getEventsInside(LighthouseModel.getInstance(), getClassesFullyQualifiedName(svnFiles)); 
 		LinkedHashSet<LighthouseEvent> listEventsToCommitt = new LinkedHashSet<LighthouseEvent>();
 		for (LighthouseEvent event : listEvents) {
 			if (event.getAuthor().equals(author)) {
				// FIXME (nilmax): Is it possible the event being commit twice? Why the check?
 				if (!event.isCommitted()) {
 					event.setCommitted(true);
 					event.setCommittedTime(svnCommittedTime);
 					listEventsToCommitt.add(event);
 				}
 			}
 		}
 		return listEventsToCommitt;
 	}
 	
 	/**
 	 * Adjust the committed time from all events using the database timezone.
 	 * 
 	 * @param listEvents
 	 * @throws SQLException
 	 */
 	public static void adjustCommittedTimeToServerTime(Collection<LighthouseEvent> listEvents) throws SQLException {
 		for (LighthouseEvent event : listEvents) {
 			if (event.isCommitted()) {
 				Date committedTime = event.getCommittedTime();
 				Date adjustedCommittedTime = DatabaseUtility.getAdjustedDateTime(committedTime, DatabasePreferences.getServerTimezone());
 				event.setCommittedTime(adjustedCommittedTime);
 			}
 		}
 	}
 
 }
