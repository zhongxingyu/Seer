 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.variables;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.python.pydev.core.IInterpreterInfo;
 import org.python.pydev.core.IInterpreterManager;
 import org.python.pydev.core.Tuple;
 import org.python.pydev.core.docutils.StringUtils;
 import org.python.pydev.plugin.nature.PythonNature;
 import org.python.pydev.ui.interpreters.IInterpreterObserver;
 import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
 
 import uk.ac.diamond.scisoft.analysis.rcp.preference.AnalysisRpcAndRmiPreferencePage;
 
 /**
  * Inject into PyDev's IInterpreterInfos the global environment variables for AnalysisRPC/RMI port and temp location.
  */
 public class InjectVariablesInPyDevInterpretterInfo implements IInterpreterObserver {
 
 	
 	private Map<String, String> entriesToAdd = new HashMap<String, String>();
 	{
 		entriesToAdd.put("SCISOFT_RPC_PORT", "${scisoft_rpc_port}");
 		entriesToAdd.put("SCISOFT_RMI_PORT", "${scisoft_rmi_port}");
 		entriesToAdd.put("SCISOFT_RPC_TEMP", "${scisoft_rpc_temp}");
 	}
 	
 	/**
 	 * To be overridden solely in test.
 	 * @return {@link AnalysisRpcAndRmiPreferencePage#isInjectVariablesAutomaticallyIntoPyDev()}
 	 */
 	protected boolean isInjectOn() {
 		return AnalysisRpcAndRmiPreferencePage.isInjectVariablesAutomaticallyIntoPyDev();
 	}
 	
 	private void injectVariables(IInterpreterManager manager,
 			IProgressMonitor monitor) {
 		if (isInjectOn()) {
 			boolean anyChange = false;
 			IInterpreterInfo[] interpreterInfos = manager.getInterpreterInfos();
 			for (IInterpreterInfo iInterpreterInfo : interpreterInfos) {
 				Map<String, String> envMap = new HashMap<String, String>();
 				String[] env = iInterpreterInfo.getEnvVariables();
 				if (env != null) {
 					for (String s : env) {
 						Tuple<String, String> sp = StringUtils.splitOnFirst(s, '=');
 						envMap.put(sp.o1, sp.o2);
 					}
 				}
 				
 				Map<String, String> original = new HashMap<String, String>(envMap);
 				envMap.putAll(entriesToAdd);
 				if (!original.equals(envMap)) {
 					anyChange = true;
 
 					String[] outEnv = new String[envMap.size()];
 					int i = 0;
 					for (Entry<String, String> entry : envMap.entrySet()) {
 						outEnv[i++] = entry.getKey() + "=" + entry.getValue();
 					}
 					((InterpreterInfo)iInterpreterInfo).setEnvVariables(outEnv);
 				}
 				
 				
 			}
 			if (anyChange) {
 				// We want an empty interpreterNamesToRestore here because we don't
 				// want to recreate all the data just for these variable changes
				// Note that if we do include the interpretter being updated in this
 				// list, then we will be called again 
 				Set<String> empty = Collections.emptySet();
 				manager.setInfos(interpreterInfos, empty, monitor);
 			}
 		}
 	}
 
 	@Override
 	public void notifyDefaultPythonpathRestored(IInterpreterManager manager, String interpreter,
 			IProgressMonitor monitor) {
 		// this is when a new pydev config is created
 		injectVariables(manager, monitor);
 	}
 
 	@Override
 	public void notifyInterpreterManagerRecreated(IInterpreterManager manager) {
 		// this is when pydev starts up
 		injectVariables(manager, new NullProgressMonitor());
 	}
 
 	@Override
 	public void notifyNatureRecreated(PythonNature arg0, IProgressMonitor arg1) {
 		// Nothing to do here, this applies to individual projects
 	}
 
 	@Override
 	public void notifyProjectPythonpathRestored(PythonNature arg0, IProgressMonitor arg1) {
 		// Nothing to do here, this applies to individual projects
 	}
 
 }
