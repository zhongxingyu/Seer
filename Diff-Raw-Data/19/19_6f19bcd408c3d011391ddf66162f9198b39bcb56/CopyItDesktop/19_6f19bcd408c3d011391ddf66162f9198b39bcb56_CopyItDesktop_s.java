 package net.mms_projects.copyit.app;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import net.mms_projects.copyit.FileStreamBuilder;
 import net.mms_projects.copyit.PathBuilder;
 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.ui.AbstractUi;
 import net.mms_projects.copyit.ui.SwtGui;
 
 public class CopyItDesktop extends CopyIt {
 
 	protected Settings settings;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		CopyItDesktop app = new CopyItDesktop();
 		app.run();
 	}
 
 	public void run() {
 		this.settings = new Settings();
 		try {
 			this.settings.setFileStreamBuilder(new StreamBuilder());
 		} catch (IOException e) {
 			e.printStackTrace();
 
 			System.exit(1);
 		}
 		this.settings.loadProperties();
 
 		AbstractUi ui = new SwtGui(this.settings);
 		ui.open();
 
 		this.settings.saveProperties();
 	}
 
 	class StreamBuilder extends FileStreamBuilder {
 
 		private File settingsFile;
 
 		public StreamBuilder() throws IOException {
 			this.settingsFile = new File(PathBuilder.getConfigDirectory(),
 					"options.properties");
 			if (!this.settingsFile.exists()) {
 				System.out.println("No settings file. Creating it.");
 				this.settingsFile.createNewFile();
 			}
 		}
 
 		@Override
 		public FileInputStream getInputStream() throws IOException {
 			return new FileInputStream(this.settingsFile);
 		}
 
 		@Override
 		public FileOutputStream getOutputStream() throws IOException {
 			return new FileOutputStream(this.settingsFile);
 		}
 
 	}
 
 }
