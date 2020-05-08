 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
package ${packageInPathFormat};
 
 import cytoscape.Cytoscape;
 import cytoscape.plugin.CytoscapePlugin;
 
 
 /**
  * This class is used to instantiate your plugin. Put whatever initialization code
  * you need into the no argument constructor (the only one that will be called).
  * The actual functionality of your plugin can be in this class, but should 
  * probably be separated into separted classes that get instantiated here.
  */
 public class MyPlugin extends CytoscapePlugin {
 
 	public MyPlugin() {
 		// Properly initializes things.
 		super();
 
 		// This action represents the actual behavior of the plugin.
 		MyAction action = new MyAction();
 		Cytoscape.getDesktop().getCyMenus().addAction(action);
 	}
 }	
