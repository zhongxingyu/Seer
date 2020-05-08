 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.util.PersistenceUtil;
 
import org.apache.log4j.Logger;

 /**
  * @author tylere
  * 
  * Attempts to recover campaigns when the application crashes.
  */
 public class AutoSaveManager implements ActionListener {
 	
	private static final Logger log = Logger.getLogger(AutoSaveManager.class);
	
 	private Timer autoSaveTimer;
 	public static final File AUTOSAVE_FILE = new File(AppUtil.getAppHome("autosave"), "AutoSave" + AppConstants.CAMPAIGN_FILE_EXTENSION);
 
 	private boolean working;
 	
 	public void start() {
 		restart();
 	}
 	
 	/**
 	 */
 	public void restart()
 	{
 		int interval = AppPreferences.getAutoSaveIncrement();
 		
 		//convert to milliseconds
 		int delay = interval*60*1000;
 		
 		if (autoSaveTimer == null) {
 			if(interval <= 0) {
 				return;
 			} else {
 				autoSaveTimer = new Timer(delay, this);
 				autoSaveTimer.start();
 			}		
 		} else {
 			if(interval <= 0) {
 				autoSaveTimer.stop();
 				autoSaveTimer = null;
 			} else {
 				autoSaveTimer.setDelay(delay);
 				autoSaveTimer.restart();
 			}	
 		}
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		if (working) {
 			return;
 		}
 		
 		// Don't autosave if we don't "own" the campaign
 		if (!MapTool.isHostingServer() && !MapTool.isPersonalServer()) {
 			return;
 		}
 
 		MapTool.getFrame().setStatusMessage("Autosaving campaign ...");
 		
 		// This occurs on the event dispatch thread, so it's ok to mess with the models
 		// We need to clone the campaign so that we can save in the background, but
 		// not have concurrency issues with the original model
 		// NOTE: This is a cheesy way to clone the campaign, but it makes it so that I
 		// don't have to keep all the various models' clone method updated on each change 
 		working = true;
 		long start = System.currentTimeMillis();
 		final Campaign campaign = new Campaign(MapTool.getCampaign());
 		//System.out.println("Time: " + (System.currentTimeMillis() - start));
 
 		// Now that we have a copy of the model, save that one
 		// TODO: Replace this with a swing worker
 		new Thread(new Runnable(){
 			public void run() {
 				try {
 					PersistenceUtil.saveCampaign(campaign, AUTOSAVE_FILE);
 					MapTool.getFrame().setStatusMessage("Autosave complete");
 				} catch (IOException ioe) {
					log.error("Autosave Failed: " + ioe, ioe);
 					MapTool.showError("Autosave failed: " + ioe);
				} catch (Throwable t) {
					log.error("Autosave Failed: " + t, t);
					MapTool.showError("Autosave failed: " + t);
 				}
 				
 				working = false;
 			}
 		}).start();
 	}
 	
 	/**
 	 * Removes any autosaved files
 	 */
 	public void purge() {
 
 		if( AUTOSAVE_FILE.exists() ) {
 			AUTOSAVE_FILE.delete();
 		}	
 	}
 	
 	/**
 	 * Removes the campaignFile if it's from Autosave, forcing to save as new
 	 */
 	public void tidy() {
 		if( AppState.getCampaignFile() == AUTOSAVE_FILE ) {
 			AppState.setCampaignFile(null);
 		}
 		purge();
 	}
 	
 	/**
 	 * Check to see if autosave recovery is necessary
 	 */
 	public void check() {
 		if( AUTOSAVE_FILE.exists() ) {
 			if( JOptionPane.showConfirmDialog(
 				MapTool.getFrame(), 
 					"An autosave file exists, would you like to retrieve it?", 
 					"Autosave Recovery", 
 					JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
 					
 				AppActions.loadCampaign(AUTOSAVE_FILE);
 			}
 		}	
 	}
 
 }
