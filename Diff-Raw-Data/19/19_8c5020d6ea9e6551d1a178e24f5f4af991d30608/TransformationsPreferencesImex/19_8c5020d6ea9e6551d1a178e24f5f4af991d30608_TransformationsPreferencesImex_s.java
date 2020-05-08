 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.ui.prefs;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import net.atos.optimus.m2m.engine.core.Activator;
 import net.atos.optimus.m2m.engine.core.masks.PreferencesTransformationMask;
 import net.atos.optimus.m2m.engine.core.transformations.ExtensionPointTransformationDataSource;
 import net.atos.optimus.m2m.engine.core.transformations.TransformationReference;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * @author Maxence Vanbésien (mvaawl@gmail.com)
  * @since 1.0
  */
 public class TransformationsPreferencesImex {
 
 	private static final String EXPORT_PREF = "PreferencesExportPath";
 
 	private static final String IMPORT_PATH_PREF = "PreferencesImportPath";
 
 	private static final String IMPORT_NAME_PREF = "PreferencesImportName";
 
 	static void exportPreferences() {
 		Properties properties = new Properties();
 
 		ExtensionPointTransformationDataSource manager = ExtensionPointTransformationDataSource.instance();
 
 		for (TransformationReference reference : manager.getAll()) {
 			properties.put(reference.getId(),
					PreferencesTransformationMask.getInstance().isTransformationEnabled(reference.getId()));
 		}
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		FileDialog dialog = new FileDialog(new Shell(Display.getDefault()));
 		String pathName = preferenceStore.getString(EXPORT_PREF);
 		if (pathName != null && pathName.length() > 1)
 			dialog.setFilterPath(pathName);
 		dialog.setFileName("OptimusEnginePreferences-" + System.currentTimeMillis() + ".properties");
 		dialog.setText("Please select where to write the preferences (Selecting existing file will overwrite it)...");
 
 		String result = dialog.open();
 		if (result == null)
 			return;
 
 		String newPathName = dialog.getFilterPath();
 		String newfileName = dialog.getFileName();
 
 		if (!newPathName.equals(pathName))
 			preferenceStore.putValue(EXPORT_PREF, newPathName);
 
 		String fullpath = newPathName + File.separator + newfileName;
 
 		FileOutputStream fos = null;
 		try {
 			File file = new File(fullpath);
 			file.createNewFile();
 			fos = new FileOutputStream(file);
 			properties.store(fos, null);
 			Activator
 					.getDefault()
 					.getLog()
 					.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Preferences exported successfully at "
 							+ fullpath));
 			fos.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} finally {
 			if (fos != null)
 				try {
 					fos.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 	}
 
 	static void importPreferences() {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 
 		FileDialog dialog = new FileDialog(new Shell(Display.getDefault()));
 		String pathName = preferenceStore.getString(IMPORT_PATH_PREF);
 		String fileName = preferenceStore.getString(IMPORT_NAME_PREF);
 
 		dialog.setText("Please select file containing the preferences to import");
 		dialog.setFilterExtensions(new String[] { "*.properties" });
 		dialog.setFilterPath(pathName);
 		dialog.setFileName(fileName);
 		dialog.open();
 
 		String newFileName = dialog.getFileName();
 		String newPathName = dialog.getFilterPath();
 		String fullPath = newPathName + File.separator + newFileName;
 
 		if (!newFileName.equals(fileName))
 			preferenceStore.putValue(IMPORT_NAME_PREF, newFileName);
 		if (!newPathName.equals(pathName))
 			preferenceStore.putValue(IMPORT_NAME_PREF, newPathName);
 
 		File file = new File(fullPath);
 
 		if (!file.exists() || file.isDirectory()) {
 			// LOG MESSAGE
 			return;
 		}
 		Properties properties = new Properties();
 		FileInputStream fis;
 		try {
 			fis = new FileInputStream(file);
 			properties.load(fis);
 			fis.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		String keyPattern = Activator.PLUGIN_ID + ".disabled.";
 
 		for (Object key : properties.keySet()) {
 			String keyAsString = String.valueOf(key);
 			if (keyAsString.startsWith(keyPattern)) {
 				String id = keyAsString.substring(keyPattern.length());
				PreferencesTransformationMask.getInstance().setTransformationEnabled(id,
 						Boolean.parseBoolean(properties.getProperty(keyAsString)));
 			}
 		}
 	}
 }
