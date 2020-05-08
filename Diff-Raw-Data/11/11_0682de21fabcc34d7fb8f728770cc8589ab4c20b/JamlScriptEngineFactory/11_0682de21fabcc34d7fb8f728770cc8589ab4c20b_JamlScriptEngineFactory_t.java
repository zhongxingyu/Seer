 /*******************************************************************************
  * Copyright (C) 2011 by Harry Blauberg
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.jaml.jsr223;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineFactory;
 
 public class JamlScriptEngineFactory implements ScriptEngineFactory {
 
 	@Override
 	public String getEngineName() {
 		return "JAML Script Engine";
 	}
 
 	@Override
 	public String getEngineVersion() {
 		return "0.0.1 alpha";
 	}
 
 	@Override
 	public List<String> getExtensions() {
 		return Arrays.asList(new String[] { "jaml" });
 	}
 
 	@Override
 	public String getLanguageName() {
 		return "JAML Script";
 	}
 
 	@Override
 	public String getLanguageVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public String getMethodCallSyntax(String arg0, String arg1, String... arg2) {
 		return null;
 	}
 
 	@Override
 	public List<String> getMimeTypes() {
		return Arrays.asList(new String[] { "application/jaml+xml" });
 	}
 
 	@Override
 	public List<String> getNames() {
 		return Arrays.asList(new String[] { "JAML" });
 	}
 
 	@Override
 	public String getOutputStatement(String arg0) {
 		return null;
 	}
 
 	@Override
 	public Object getParameter(String arg0) {
 		return null;
 	}
 
 	@Override
 	public String getProgram(String... arg0) {
 		return null;
 	}
 
 	@Override
 	public ScriptEngine getScriptEngine() {
 		return new JamlScriptEngine(this);
 	}
 }
