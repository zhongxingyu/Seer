 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan;
 
 import java.io.File;
 import net.jot.logger.JOTLogger;
 import net.jot.persistance.JOTDBUpgrader;
import net.jot.utils.JOTUtilities;
 
 /**
  * Handles DB upgrades
  * @author tcolar
  */
 public class FanDbUpgrader extends JOTDBUpgrader
 {
 
 	private int VERSION = 2;
 
 	@Override
 	public void upgradeDb(String dbName, int fromVersion) throws Exception
 	{
 		// For now just whipeout the DB and let it rebuild
		if(fromVersion != VERSION)
 		{
 			JOTLogger.info(this, "Indexer database version changed! - Deleting current index");
			synchronized(this)
 			{
				File dbHome = new File(FanUtilities.getFanUserHome().getAbsolutePath()+File.separator + "db"+File.separator+dbName+File.separator);
				if(dbHome.exists())
					JOTUtilities.deleteFolderContent(dbHome);
 			}
 		}
 	}
 
 	@Override
 	public int getLatestVersion()
 	{
 		return VERSION;
 	}
 }
