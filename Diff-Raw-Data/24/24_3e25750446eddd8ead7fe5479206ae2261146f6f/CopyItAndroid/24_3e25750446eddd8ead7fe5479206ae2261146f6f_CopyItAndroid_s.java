 package net.mms_projects.copyit.app;
 
import net.mms_projects.copyit.FileStreamBuilder;
 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.ui.AbstractUi;
 import net.mms_projects.copyit.ui.AndroidGui;
 import net.mms_projects.copyit.ui.android.MainActivity;
 
 public class CopyItAndroid extends CopyIt {
 
 	protected Settings settings;
 
	public void run(MainActivity activity, FileStreamBuilder streamBuilder) {
 		this.settings = new Settings();
		this.settings.setFileStreamBuilder(streamBuilder);
		this.settings.loadProperties();
 
 		AbstractUi ui = new AndroidGui(this.settings, activity);
 		ui.open();
 	}
 
 }
