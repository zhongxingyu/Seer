 /*
  * Copyright 2012 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.command;
 
import java.util.LinkedHashMap;
 import java.util.Map;
 
import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryScript;
 import org.araqne.logdb.QueryScriptInput;
 import org.araqne.logdb.QueryScriptOutput;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.Row;
 import org.osgi.framework.BundleContext;
 
 public class Script extends QueryCommand {
 	private BundleContext bc;
 	private String scriptName;
 	private Map<String, String> params;
 	private QueryScript script;
 	private DefaultScriptInput input;
 	private DefaultScriptOutput output;
 
 	public Script(BundleContext bc, String scriptName, Map<String, String> params, QueryScript script) {
 		this.bc = bc;
 		this.scriptName = scriptName;
		this.params = new LinkedHashMap<String, String>(params);
 		this.script = script;
 		this.input = new DefaultScriptInput();
 		this.output = new DefaultScriptOutput();
 	}
 
 	@Override
 	public String getName() {
 		return "script";
 	}
 
 	public String getScriptName() {
 		return scriptName;
 	}
 
 	public Map<String, String> getParameters() {
 		return params;
 	}
 
 	public QueryScript getQueryScript() {
 		return script;
 	}
 
 	@Override
 	public void onPush(Row m) {
 		input.data = m.map();
 		script.handle(input, output);
 	}
 
 	@Override
 	public boolean isReducer() {
 		return true;
 	}
 
 	@Override
 	public void onClose(QueryStopReason reason) {
 		script.eof(output);
 	}
 
 	private void out(Row data) {
 		pushPipe(data);
 	}
 
 	private class DefaultScriptInput implements QueryScriptInput {
 		private Map<String, Object> data;
 
 		@Override
 		public BundleContext getBundleContext() {
 			return bc;
 		}
 
 		@Override
 		public Map<String, Object> getData() {
 			return data;
 		}
 	}
 
 	@Override
 	public String toString() {
 		String opts = "";
 		for (String key : params.keySet()) {
 			String val = params.get(key);
 			if (val == null)
 				val = "";
 
 			opts += " " + key + "=" + val;
 		}
 
 		return "script" + opts + " " + scriptName;
 	}
 
 	private class DefaultScriptOutput implements QueryScriptOutput {
 		@Override
 		public void write(Map<String, Object> data) {
 			out(new Row(data));
 		}
 	}
 }
