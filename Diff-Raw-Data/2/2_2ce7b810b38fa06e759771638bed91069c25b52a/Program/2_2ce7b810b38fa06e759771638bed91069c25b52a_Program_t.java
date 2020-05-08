 /*
  * Copyright 2012 Joseph Spencer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jsl.strategies;
 
 import java.util.regex.Matcher;
 import jsl.*;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class Program extends Production {
    public Program(Output output){
       super(output);
       output.
          prepend("(function(){").
          append("function StringBuffer(){var v=[],i=0;this.append=function(s){v[i++]=s||'';};this.toString=function(){return v.join('');};}})();").
          append(
             "function count(obj){"+
                "var count=0;"+
                "var name;"+
                "if(!!obj && typeof obj === 'object'){"+
                   "if(obj.slice){"+
                      "return obj.length>>>0;"+
                   "} else {"+
                   "for(name in obj){"+
                      "count++;"+
                   "}"+
                "}"+
                "return count;"+
             "}"
          );
    }
 
    private boolean hasProgramNamespace;
 
    @Override
    public void execute(CharWrapper characters, ProductionContext context) throws Exception {
      String exception = "The first Production must be a ProgramNamespace.";
 
       if(characters.charAt(0) == open && !hasProgramNamespace){
          hasProgramNamespace=true;
          context.addProduction(new ProgramNamespace(output));
          return;
       } else if(hasProgramNamespace){
          characters.removeSpace();
          if(characters.charAt(0) == open){
             if(characters.charAt(1) == i){
                Output importOutput = new Output();
                output.prepend(importOutput);
                output.prepend("(function(){");
                output.append("})();");
                context.addProduction(new ImportStatements(importOutput));
                return;
             } else if(characters.charAt(1) == v){
                output.prepend(context.getCurrentVariableOutput());
                context.addProduction(new GlobalVariableDeclarations(output));
                return;
             }
             context.addProduction(new GlobalStatements(output));
             return;
          }
       }
       throw new Exception(exception);
    }
 
    @Override
    public void close(ProductionContext context) throws Exception {
       if(!hasProgramNamespace){
          throw new Exception("No ProgramNamespace was declared in: \""+context.filePath+"\"");
       }
    }
 }
