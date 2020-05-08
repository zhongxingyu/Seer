 package prefspecs.safari.builders;
 
 import org.eclipse.core.resources.IProject;
 
 import org.eclipse.uide.core.ProjectNatureBase;
 import org.eclipse.uide.runtime.IPluginLog;
 
 import com.ibm.watson.smapifier.builder.SmapiProjectNature;
 
 import prefspecs.PrefspecsPlugin;
 
 public class PrefspecsNature extends ProjectNatureBase {
     public static final String k_natureID = PrefspecsPlugin.kPluginID + ".safari.nature";
 
    String projectName = "org.eclipse.uide.prefspecs";
    
    // SMS 20 Mar 2007
    // Added
    public PrefspecsNature() {
    	super();
    	setProjectName(projectName);
    }
    
    
     public String getNatureID() {
         return k_natureID;
     }
 
    // SMS 20 Mar 2007
    // BUILDER_ID is the unqualfied ID of the builder; for use by
    // others it needs to be referenced by its project-qualified name
    // (just as the Nature extension itself needs to refer to the
    // builder by its project-qualified name).  
     public String getBuilderID() {
        //return PrefspecsBuilder.BUILDER_ID;
    	return projectName + "." + PrefspecsBuilder.BUILDER_ID;
     }
 
     public void addToProject(IProject project) {
         super.addToProject(project);
         new SmapiProjectNature("prefspecs").addToProject(project);
     };
 
     protected void refreshPrefs() {
         // TODO implement preferences and hook in here
     }
 
     public IPluginLog getLog() {
         return PrefspecsPlugin.getInstance();
     }
 
     protected String getDownstreamBuilderID() {
         // TODO Auto-generated method stub
         return null;
     }
 }
