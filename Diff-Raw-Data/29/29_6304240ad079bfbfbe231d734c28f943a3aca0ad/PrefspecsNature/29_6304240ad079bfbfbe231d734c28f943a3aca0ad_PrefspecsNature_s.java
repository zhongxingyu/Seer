 package prefspecs.safari.builders;
 
 import org.eclipse.core.resources.IProject;
 
 import org.eclipse.uide.core.ProjectNatureBase;
 import org.eclipse.uide.runtime.IPluginLog;
 
 import com.ibm.watson.smapifier.builder.SmapiProjectNature;
 
 import prefspecs.PrefspecsPlugin;
 
 public class PrefspecsNature extends ProjectNatureBase {
     public static final String k_natureID = PrefspecsPlugin.kPluginID + ".safari.nature";
 
     public String getNatureID() {
         return k_natureID;
     }
 
     public String getBuilderID() {
        return PrefspecsBuilder.BUILDER_ID;
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
