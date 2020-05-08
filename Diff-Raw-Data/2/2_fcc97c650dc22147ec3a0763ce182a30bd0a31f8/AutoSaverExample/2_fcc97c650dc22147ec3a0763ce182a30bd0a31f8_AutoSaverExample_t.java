 package com.example.librelib.android.examples;
 
 import com.karoldepka.librelib.android.AutoSaver;
 
 public class AutoSaverExample {
 	
 	private final AutoSaver autoSaver = new AutoSaver(2000) {
 		@Override public void saveCustom() {
			// save/push/update somewhere
 		}
 	};
 	
 	/** Called when the user modifies the data */
 	public void onModified() {
 		autoSaver.documentModified();
 	}
 	
 	public void onForceSave() {
 		autoSaver.forceSaveNow();
 	}
 	
 	public void onDestroy() {
 		autoSaver.destroy();
 	}
 	
 }
