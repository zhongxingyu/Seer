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
 
 import java.io.Reader;
 import java.io.StringReader;
 
 import javax.script.AbstractScriptEngine;
 import javax.script.Bindings;
 import javax.script.ScriptContext;
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptException;
 import javax.script.SimpleBindings;
 
import org.jaml.core.JamlReader;

 public class JamlScriptEngine extends AbstractScriptEngine {
 	protected ScriptEngineFactory factory;
 
 	public JamlScriptEngine(ScriptEngineFactory scriptEngineFactory) {
 		factory = scriptEngineFactory;
 	}
 
 	@Override
 	public Bindings createBindings() {
 		return new SimpleBindings();
 	}
 
 	@Override
 	public Object eval(String script, ScriptContext context)
 			throws ScriptException {
 		return eval(new StringReader(script), context);
 	}
 
 	@Override
 	public Object eval(Reader reader, ScriptContext context)
 			throws ScriptException {
 		return JamlReader.load(reader);
 	}
 
 	@Override
 	public ScriptEngineFactory getFactory() {
 		return factory;
 	}
 }
