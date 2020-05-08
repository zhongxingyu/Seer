 package view;
 
 import model.ConfigLoader;
 import model.PluginTableModel;
 import org.junit.Test;
 import controller.BigLogic;
 
 public class TDIDialogTest {
 
 	@Test
 	public void test() {
 		Object rowData[][] = { { "Music", true }, { "Anilator", true },
 				{ "PlugMeIn!", false }, { "Baestewogibtplugin", true },
 				{ "Plugin Nr.5", false }, };
 		PluginTableModel pluginTableModel = new PluginTableModel(rowData);
 		new TDIDialog(new BigLogic(), pluginTableModel);
		//TDIDialog t = new TDIDialog(); 
 		while (true) {
 
 		}
 	}
 
 }
