 /*
  *  The MIT License
  *
  *  Copyright 2011 Praqma A/S.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 
 package net.praqma.jenkins.plugin.reloaded;
 
 import java.io.IOException;
 import java.util.List;
 
 import net.praqma.jenkins.plugin.reloaded.MatrixReloadedState.BuildState;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.matrix.MatrixRun;
 import hudson.model.EnvironmentContributor;
 import hudson.model.ParameterValue;
 import hudson.model.ParametersAction;
 import hudson.model.StringParameterValue;
 import hudson.model.TaskListener;
 import hudson.model.Run;
 
 @Extension
 public class MatrixReloadedEnvironmentContributor extends EnvironmentContributor {
     public void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener) throws IOException,
             InterruptedException {
     	
         List<ParametersAction> actionList = r.getActions(ParametersAction.class);
 
         if (actionList.size() == 0) {
             return;
         }
         
         List<ParameterValue> pvs = actionList.get(0).getParameters();
         StringParameterValue uuid = (StringParameterValue)getParameterValue(pvs, Definitions.__UUID);
         
         BuildState bs = MatrixReloadedState.getInstance().getBuildState(uuid.value);
 
         if (bs.rebuildNumber > 0) {
             envs.put(Definitions.__REBUILD_VAR_NAME,bs.rebuildNumber + "");
         }
         
     }
     
     private ParameterValue getParameterValue(List<ParameterValue> pvs, String key) {
         for (ParameterValue pv : pvs) {
             if (pv.getName().equals(key)) {
                 return pv;
             }
         }
 
         return null;
     }
 }
