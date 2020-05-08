 /*
  *  Copyright 2010 Richard Nichols.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package org.carpediem.lesscss;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 final public class LessCompiler {
     private final String LESS_SRC = "org/carpediem/lesscss/less-1.1.5.js";
     private final String RUN_SRC = "org/carpediem/lesscss/run.js";
    private final String ENGINE_SRC = "org/carpediem/lesscss/engine.js";
    private final String ENV_SRC = "org/carpediem/lesscss/env.js";
     private final ScriptEngine scriptEngine;
 
     public LessCompiler() throws IOException, ScriptException {
        final ClassLoader classLoader = getClass().getClassLoader();
         final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
         scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        for (String s : new String[]{ENV_SRC, LESS_SRC, ENGINE_SRC, RUN_SRC}) {
            final InputStream resourceAsStream = classLoader.getResourceAsStream(s);
            scriptEngine.eval(new InputStreamReader(resourceAsStream));
            resourceAsStream.close();
            if (s.equals(ENGINE_SRC)) {
                scriptEngine.eval("lessenv.charset = '" + "UTF-8" + "';");
                scriptEngine.eval("lessenv.css = " + "false" + ";");
            }
        }
     }
 
     public String compile(InputStream input) throws IOException, ScriptException {
         String data = asString(input);
         final String replace = data.replace("\"", "\\\"").replaceAll("\n", "").replaceAll("\r", "");
         StringBuilder sb = new StringBuilder("lessIt(\"").append(replace).append("\")");
         String lessitjs = sb.toString();
         return scriptEngine.eval(lessitjs).toString();
     }
 
     private String asString(InputStream inputStream) throws IOException {
         BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = br.readLine()) != null) {
             sb.append(line);
         }
         return sb.toString();
     }
 }
