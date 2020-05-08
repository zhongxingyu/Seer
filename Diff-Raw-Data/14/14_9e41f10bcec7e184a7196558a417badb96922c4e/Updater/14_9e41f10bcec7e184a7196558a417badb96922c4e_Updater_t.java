 package org.opensixen.omvc.client;
 
 import java.util.List;
 
 import org.compiere.util.CLogger;
 import org.compiere.util.Env;
 import org.opensixen.dev.omvc.interfaces.IRevisionDownloader;
 import org.opensixen.dev.omvc.model.Revision;
 import org.opensixen.dev.omvc.model.Script;
 import org.opensixen.model.MRevision;
 import org.opensixen.model.POFactory;
 import org.opensixen.omvc.client.model.SQLEngine;
 
 public class Updater {
 
 	private CLogger log = CLogger.getCLogger(getClass());
 	private IRevisionDownloader downloader;
 
 	public Updater()	{
 		downloader = Activator.getDownloader();
 	}
 	
	public boolean update()	{	
 		
 		SQLEngine engine = new SQLEngine();
 		
 		List<MRevision> projects = POFactory.getList(Env.getCtx(), MRevision.class);
 		
 		for (MRevision r:projects)	{		
 			List<Revision> revisions =  downloader.getRevisions(r.getProject_ID(),r.getRevision());			
 			for (Revision rev:revisions)	{				
 				log.info("Running revision_ID " + rev.getRevision_ID() + ": " + rev.getDescription());
 				List<Script> scripts = downloader.getScripts(rev, engine.getAvailableEngines());
 				for (Script script: scripts)	{
 					if (!engine.run(script))	{
 						return false;
 					}
 					// Actualizamos el proyecto a la revision descargada.
 					r.setRevision(rev.getRevision_ID());
 					r.save();
 				}				
 			}
 		}
 		return true;
 	}
 	
 }
