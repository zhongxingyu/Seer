 package org.iucn.sis.server.utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipOutputStream;
 
 import org.hibernate.Hibernate;
 import org.hibernate.ReplicationMode;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
 import org.iucn.sis.server.api.io.UserIO;
 import org.iucn.sis.server.api.io.WorkingSetIO;
 import org.iucn.sis.server.api.locking.LockException;
 import org.iucn.sis.server.api.locking.LockType;
 import org.iucn.sis.server.api.locking.LockRepository.LockInfo;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.utils.DatabaseExporter;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Definition;
 import org.iucn.sis.shared.api.models.Infratype;
 import org.iucn.sis.shared.api.models.IsoLanguage;
 import org.iucn.sis.shared.api.models.Permission;
 import org.iucn.sis.shared.api.models.PermissionGroup;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Relationship;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.TaxonStatus;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.Virus;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.restlet.data.Status;
 
 import com.solertium.db.DBException;
 
 public class WorkingSetExporter extends DatabaseExporter {
 	
 	private static final int BATCH_SIZE = 200;
 	
 	private final boolean lock;
 	private final String username;
 	private final HashMap<Integer, String> locked;
 	
 	private final String location;
 	
 	protected Session source;
 	protected Session target;
 	
 	protected String fileName;
 	
 	public WorkingSetExporter(Integer workingSetID, String username, boolean lock, String location, String fileName) {
 		super(null, workingSetID);
 		this.username = username;
 		this.lock = lock && SIS.amIOnline();
 		this.location = location;
 		this.fileName = fileName;
 		
 		this.locked = new HashMap<Integer, String>();
 	}
 	
 	@Override
 	protected void afterRun() throws DBException {
 		//super.afterRun();
 		
 		if (location == null) {
 			write("Your database has been exported successfully.");
 			return;
 		};
 		
 		write("Write complete, zipping results...");
 		
 		try {
 			write("--- Complete ---");
 			write("You can now download your working set.");
 			write("<a target=\"blank\" href=\"/apps/org.iucn.sis.server/workingSetExporter/downloads/%s\">Click here to download</a>", zip());
 		} catch (Exception e) {
 			write("Failed to zip database");
 		}
 	}
 	
 	private String zip() throws Exception {
 		File folder = new File(location);
 		File tmp = new File(folder, fileName + ".zip");
 		
 		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
 		for (File file : folder.listFiles()) {
 			//Don't zip yourself...
 			if (file.getName().equals(tmp.getName()))
 				continue;
 			try {
 				InputStream in = new BufferedInputStream(new FileInputStream(file));
 				zos.putNextEntry(new ZipEntry(file.getName()));
 				
 				// Transfer bytes from the file to the ZIP file
 				byte[] buf = new byte[1024];
 				int len;
 				while ((len = in.read(buf)) > 0) {
 					zos.write(buf, 0, len);
 				}
 				
 				zos.closeEntry();
 			} catch (ZipException e) {
 				write("Failed to write %s to zip file: %s", file.getName(), e.getMessage());
 			}
 		}
 		
 		zos.close();
 		
 		return tmp.getName();
 	}
 	
 	@Override
 	protected void insertAssessment(Session session, Assessment assessment) throws DBException {
 		if (lock)
 			lockAssessment(assessment);
 		
 		/*
 		 * Initialize everything this way because it's lazy & recursive.
 		 */
 		assessment.toXML();
 		
 		source.clear();
 		target.replicate(assessment, ReplicationMode.OVERWRITE);
 	}
 	
 	private void lockAssessment(Assessment assessment) throws DBException {
 		Session session = SISPersistentManager.instance().openSession();
 		User user = new UserIO(session).getUserFromUsername(username);
 		
 		Status ret = SIS.get().getLocker().persistentLockAssessment(
 			assessment.getId(), LockType.CHECKED_OUT, user,
 				workingSetID + "");
 
 		if (!ret.isSuccess()) {
 			LockInfo lock;
 			try {
 				lock = SIS.get().getLocker().getAssessmentPersistentLock(assessment.getId());
 			} catch (LockException e) {
 				return;
 			}
 			if (lock.getLockType().equals(LockType.CHECKED_OUT)) {
 				write("Global draft assessment for " + assessment.getTaxon().getFullName()
 						+ " is already checked out by " + lock.getUsername());
 			} else {
 				write("Global draft assessment for " + assessment.getTaxon().getFullName()
 						+ " is locked for saving by " + lock.getUsername() + ". Save"
 						+ " locks are granted for a 2 minute period after each save." 
 						+ " Please try again later.");
 			}
 		} else
 			locked.put(assessment.getId(), username);
 		
 		session.close();
 	}
 	
 	protected void execute() throws Throwable {
 		String name;
 		Properties properties = new Properties();
		properties.setProperty("generator", "assigned");
 		if (location == null) {
 			String location = "localhost:5432";
 			name = source + "_target";
 			
 			properties.setProperty("database_dialect", "org.hibernate.dialect.PostgreSQLDialect");
 			properties.setProperty("dbsession." + name + ".uri", "jdbc:postgresql://" + location + "/sis_target");
 			properties.setProperty("dbsession." + name + ".driver", "org.postgresql.Driver");
 			properties.setProperty("dbsession." + name + ".user", "sa");
 			properties.setProperty("dbsession." + name + ".password", "");
 		}
 		else {
 			String location = this.location;
 			name = source + "_target";
 			
 			properties.setProperty("database_dialect", "org.hibernate.dialect.H2Dialect");
 			properties.setProperty("dbsession." + name + ".uri", "jdbc:h2:file:" + location + "/" + fileName);
 			properties.setProperty("dbsession." + name + ".driver", "org.h2.Driver");
 			properties.setProperty("dbsession." + name + ".user", "sa");
 			properties.setProperty("dbsession." + name + ".password", "");
 		}
 		
 		boolean fromScratch = true;
 		
 		source = SIS.get().getManager().openSession();
 		
 		final SISPersistentManager targetManager = SISPersistentManager.newInstance(name, properties, fromScratch);
 		target = targetManager.openSession();
 		
 		try {
 			if (fromScratch) {
 				List<Class<?>> copyAll = new ArrayList<Class<?>>();
 				copyAll.add(AssessmentType.class);
 				copyAll.add(Definition.class);
 				copyAll.add(Infratype.class);
 				copyAll.add(IsoLanguage.class);
 				copyAll.add(PermissionGroup.class);
 				copyAll.add(Permission.class);
 				copyAll.add(Region.class);
 				copyAll.add(Relationship.class);
 				copyAll.add(TaxonLevel.class);
 				copyAll.add(TaxonStatus.class);
 				copyAll.add(User.class);
 				copyAll.add(Virus.class);
 				
 				/*
 				 * TODO: Copy all references ... currently works, 
 				 * but takes a Really Long Time (TM)
 				 */
 				//copyAll.add(Reference.class);
 							
 				for (Class<?> clazz : copyAll) {
 					int i = 0;
 					List<?> list = SISPersistentManager.instance().listObjects(clazz, source);
 					source.clear();
 					write("Copying %s...", clazz.getSimpleName());
 					boolean started = false;
 					for (Object obj : list) {
 						if (!started) {
 							started = true;
 							target.beginTransaction();
 						}
 						target.replicate(obj, ReplicationMode.IGNORE);
 						
 						if (++i % BATCH_SIZE == 0) {
 							write("  %s...", i);
 							target.getTransaction().commit();
 							target.clear();
 							started = false;
 						}
 					}
 					if (started) {
 						write("  %s...", i);
 						target.getTransaction().commit();
 						target.clear();
 					}
 					target.clear();
 				}
 			}
 		} catch (Exception e) {
 			source.close();
 			target.close();
 			targetManager.shutdown();
 			throw e;
 		}
 		
 		final WorkingSet workingSet = new WorkingSetIO(source).readWorkingSet(workingSetID);
 		Hibernate.initialize(workingSet.getUsers());
 		
 		try {
 			final AssessmentFilterHelper helper = new AssessmentFilterHelper(source, workingSet.getFilter());
 			final int size = workingSet.getTaxon().size();
 			final HashSet<Integer> seen = new HashSet<Integer>();
 			
 			int count = 0;
 			for (Taxon taxon : workingSet.getTaxon()) {
 				target.beginTransaction();
 				insertTaxa(taxon, seen);
 				target.getTransaction().commit();
 				
 				Collection<Assessment> assessments = helper.getAssessments(taxon.getId());
 				write("Copying %s eligible assessments for %s, (%s/%s)", 
 					assessments.size(), taxon.getFullName(), ++count, size);
 				target.beginTransaction();
 				for (Assessment assessment : assessments) {
 					write("  Writing assessment #%s", assessment.getId());
 					insertAssessment(null, assessment);
 				}
 				target.getTransaction().commit();
 				target.clear();
 			}
 			
 			target.clear();
 			target.beginTransaction();
 			target.replicate(workingSet, ReplicationMode.OVERWRITE);
 			target.getTransaction().commit();
 		} finally {
 			source.close();
 			target.close();
 			targetManager.shutdown();
 		}
 		
 		afterRun();
 	}
 	
 	protected void insertTaxa(Taxon taxon, HashSet<Integer> seen) throws DBException {
 		if (taxon != null && !seen.contains(taxon.getId())) {
 			taxon = (Taxon)source.load(Taxon.class, taxon.getId());
 			
 			//Load any lazy data so it will replicate
 			taxon.toXML();
 			
 			source.clear();
 			target.replicate(taxon, ReplicationMode.OVERWRITE);
 			
 			seen.add(taxon.getId());
 			
 			//No need for recursion here, replicate will catch it...
 		}
 	}
 
 }
