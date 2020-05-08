 package net.praqma.jenkins.plugin.reloaded;
 
 import java.util.List;
 
 import net.praqma.jenkins.plugin.reloaded.MatrixReloadedState.BuildState;
 
 import hudson.Extension;
 import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.listeners.MatrixBuildListener;
 import hudson.model.ParameterValue;
 import hudson.model.ParametersAction;
 import hudson.model.StringParameterValue;
 
 @Extension
 public class MatrixReloadedBuildListener extends MatrixBuildListener{
 	
 	public boolean doBuildConfiguration(MatrixBuild b, MatrixConfiguration c)
 	{
         List<ParametersAction> actionList = b.getActions(ParametersAction.class);
 
         if (actionList.size() == 0) {
             return true;
         }
         
         List<ParameterValue> pvs = actionList.get(0).getParameters();
        
        /* If the list is null */
        if( pvs == null ) {
        	return true;
        }
        
         StringParameterValue uuid = (StringParameterValue)getParameterValue(pvs, Definitions.__UUID);
         
        /* If the uuid is not defined, return true */
        if( uuid == null ) {
        	return true;
        }
        
         BuildState bs = MatrixReloadedState.getInstance().getBuildState(uuid.value);
         
         return bs.getConfiguration(c.getCombination());
 	}
 	
     /**
      * Convenience method for retrieving {@link ParameterValue}s.
      * 
      * @param pvs A list of {@link ParameterValue}s.
      * @param key The key of the {@link ParameterValue}.
      * @return The parameter or null
      */
     private ParameterValue getParameterValue(List<ParameterValue> pvs, String key) {
         for (ParameterValue pv : pvs) {
             if (pv.getName().equals(key)) {
                 return pv;
             }
         }
 
         return null;
     }
 }
