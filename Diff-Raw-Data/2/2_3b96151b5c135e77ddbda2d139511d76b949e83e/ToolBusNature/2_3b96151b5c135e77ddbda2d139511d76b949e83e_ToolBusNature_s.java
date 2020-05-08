 package toolbus_ide;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.imp.builder.ProjectNatureBase;
 import org.eclipse.imp.runtime.IPluginLog;
 
 public class ToolBusNature extends ProjectNatureBase {
 	public static final String k_natureID = Activator.kPluginID + ".nature";
 
 	public ToolBusNature() {
 		super();
 	}
 	
 	public String getNatureID() {
 		return k_natureID;
 	}
 
 	public String getBuilderID() {
 		return Builder.BUILDER_ID;
 	}
 
 	public void addToProject(IProject project) {
 		super.addToProject(project);
	};
 
 	protected void refreshPrefs() {
 	}
 
 	public IPluginLog getLog() {
 		return Activator.getInstance();
 	}
 
 	protected String getDownstreamBuilderID() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
