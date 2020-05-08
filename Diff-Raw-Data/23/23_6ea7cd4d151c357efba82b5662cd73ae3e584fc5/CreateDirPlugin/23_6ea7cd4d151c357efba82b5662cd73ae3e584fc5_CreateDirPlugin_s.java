 package cz.zcu.kiv.kc.plugin.createDir;
 
 import java.io.File;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import cz.zcu.kiv.kc.interfaces.ICreateDirPlugin;
 import cz.zcu.kiv.kc.plugin.AbstractPlugin;
 
 public class CreateDirPlugin extends AbstractPlugin implements ICreateDirPlugin {
 
 	public CreateDirPlugin()
 	{
 		super();
 		UIManager.put("ClassLoader", getClass().getClassLoader());
 	}
 	@Override
 	public void executeAction(List<File> selectedFiles, String destinationPath,
 			String sourcePath) {
 		String name = askForName("New directory name:");
 		if (name == null)
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Operace byla zruena uivatelem.",
 				"Operace zruena.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		if (name.trim().isEmpty())
 		{
 			JOptionPane.showMessageDialog(
 				this.mainWindow,
 				"Neplatn zadn.",
 				"Chyba.",
 				JOptionPane.ERROR_MESSAGE
 			);
 			return;
 		}
 		
		new File(sourcePath + File.separator + name).mkdir();
 		sendEvent(sourcePath);		
 	}
 
 	@Override
 	public String getName() {
 		return "New directory";
 	}
 
 }
