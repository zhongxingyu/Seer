 package ch.zhaw.simulation.sysintegration.filechooser.script;
 
 import java.awt.Window;
 import java.io.File;
 
 import ch.zhaw.simulation.sysintegration.SimFileFilter;
 import ch.zhaw.simulation.sysintegration.filechooser.FilechooserException;
 
 public class LinuxFilechooser extends ScriptFilechooser {
 
 	@Override
 	public File showSaveDialog(Window parent, SimFileFilter filefilter, String lastSavePath) throws FilechooserException {
 		try {
 			String description = filefilter.getDescription();
 			String filter = filefilter.getExtension();
 			if (filter == null) {
 				filter = "*";
 			} else {
 				filter = "*" + filter;
 			}
 
 			String file = run(new String[] { "python", "filechooser/linux/filechooser.py", "Datei speichern...", description, filter, lastSavePath, "save" });
 			if (file == null) {
 				return null;
 			}
 			
			return checkFileSaveExists(file, parent, filefilter, lastSavePath);
 		} catch (Exception e) {
 			throw new FilechooserException(e);
 		}
 	}
 
 	@Override
 	public File showOpenDialog(Window parent, SimFileFilter filefilter, String lastSavePath) throws FilechooserException {
 		try {
 			String description = filefilter.getDescription();
 			String filter = filefilter.getExtension();
 			if (filter == null) {
 				filter = "*";
 			} else {
 				filter = "*" + filter;
 			}
 
 			String file = run(new String[] { "python", "filechooser/linux/filechooser.py", "Datei öffnen...", description, filter, lastSavePath, "open" });
 			if (file == null) {
 				return null;
 			}
 			return new File(file);
 		} catch (Exception e) {
 			throw new FilechooserException(e);
 		}
 	}
 }
