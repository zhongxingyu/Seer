 package wwe.preferences;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import wwe.Activator;
 
 public class ToolboxInitializer extends AbstractPreferenceInitializer {
	private static final String TOOLBAR_FOLDER = "toolbaritems";
 
 	public ToolboxInitializer() {
 	}
 
 	@Override
 	public void initializeDefaultPreferences() {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		// store.setDefault("PATH",
 		// "C:\\Users\\Administrator\\git\\WWE\\WWE\\toolbar");
 		loadToolBar();
 		String seperator = System.getProperty("file.separator");
 
 		store.setDefault(
				"PATH", System.getProperty("user.home") + seperator + "WWE" + seperator + TOOLBAR_FOLDER); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private void loadToolBar() {
 		final byte[] BUFFER = new byte[0xFFFF];
 
 		String seperator = System.getProperty("file.separator");
 		String pathToFolder = System.getProperty("user.home") + seperator
 				+ "WWE" + seperator;
 		JarFile jar = null;
 
 		try {
 			File folder = new File(pathToFolder);
 
 			// create folder on start up
 			if (!folder.exists()) {
 				folder.mkdirs();
 			}
 
 			jar = new JarFile(this.getClass().getProtectionDomain()
 					.getCodeSource().getLocation().getPath());
 
 			// we will loop through all the entries in the jar file
			Enumeration<JarEntry> entries = jar.entries();

			while (entries.hasMoreElements()) {
 				JarEntry entry = entries.nextElement();
 
 				File file = new File(pathToFolder, entry.getName());
				if (entry.getName().contains(TOOLBAR_FOLDER)) {
 
 					if (entry.isDirectory())
 						file.mkdirs();
 					else {
 						new File(file.getParent()).mkdirs();
 
 						InputStream is = null;
 						OutputStream os = null;
 
 						try {
 							is = jar.getInputStream(entry);
 							os = new FileOutputStream(file);
 
 							for (int len; (len = is.read(BUFFER)) != -1;)
 								os.write(BUFFER, 0, len);
 						} finally {
 							if (os != null)
 								os.close();
 							if (is != null)
 								is.close();
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("could not load toolbar! Do it manually");
 		} finally {
 			if (jar != null)
 				try {
 					jar.close();
 				} catch (IOException e) {
 					System.out.println("could not close jar");
 				}
 		}
 	}
 }
