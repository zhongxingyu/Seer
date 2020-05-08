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
 package com.xforj.productions;
 
 import com.xforj.*;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class Program extends Production {
    Output programNamespaceOutput;
    Output importOutput;
    Output variableOutput;
    Output globalStatementsOutput;
    /**
     * Program is the entry point for all productions in the grammar.  When importing 
     * another file, Program is nested within other programs.
     * @param output
     * @param currentVariableOutput
     * @param previousContext May be null;
     */
    public Program(
       Output output, 
       VariableOutput currentVariableOutput, 
       ProductionContext context,
       boolean isNested
    ){
       super(output);
       programNamespaceOutput=new Output();
       importOutput=new Output();
       variableOutput=currentVariableOutput;
       globalStatementsOutput=new Output();
       JSParameters globalParams = context.getParams();
       JSParametersWrapper globalParamNames = context.getJSParametersWrapper();
 
       output.
          prepend( "(function(").prepend(globalParamNames).prepend("){").
             prepend(programNamespaceOutput).
             prepend(importOutput).
          prepend("(function(").prepend(globalParamNames).prepend("){").
                prepend(variableOutput).
                prepend(globalStatementsOutput).
             prepend("})(").prepend(globalParamNames).prepend(");");
 
       if(isNested){
          output.prepend("})(").prepend(globalParamNames).prepend(");");
       } else {
          if(!context.assignTemplatesGlobally){
             output.prepend("return "+js_TemplateBasket);
          }
 
          output.
          prepend("})(").
             prepend(context.getArgumentsWrapper()).
          prepend(");");
          globalParams.put(js_StringBuffer, 
             //StringBuffer
             "function(){"+
                "var r=[],i=0,t='number string boolean',f="+
                   "function(s){"+
                      "var y,v;"+
                      "try{"+
                         "v=s();"+
                      "}catch(e){"+
                         "v=s;"+
                      "}"+
                      "y=typeof(v);"+
                      "r[i++]=(t.indexOf(y)>-1)?v:''"+
                   "};"+
                   "f.s=function(){"+
                      "return r.join('')"+
                   "};"+
                "return f"+
             "}"
          ).put(
             js_TemplateBasket, 
             (
                context.assignTemplatesGlobally?
                   "(function(){return this})()":
                   "{}"
             )
          ).put(js_SafeValue, 
            "function(v){try{return v()}catch(e){return typeof(v)==='function'?void(0):v}}"
          );
       }
    }
 
    private boolean hasProgramNamespace;
    private boolean hasGlobalVariableDeclarations;
 
    @Override
    public void execute(CharWrapper characters, ProductionContext context) throws Exception {
       String exception = "The first Production must be a ProgramNamespace.";
 
       if(characters.charAt(0) == '{' && !hasProgramNamespace){
          hasProgramNamespace=true;
          context.addProduction(new ProgramNamespace(programNamespaceOutput));
          return;
       } else if(hasProgramNamespace){
          characters.removeSpace();
          if(characters.charAt(0) == '{'){
             if(characters.charAt(1) == 'i'){
                if(!hasGlobalVariableDeclarations){
                   Output importStatementsOutput = new Output();
                   importOutput.
                      prepend(importStatementsOutput);
                   context.addProduction(new ImportStatements(importStatementsOutput));
                   return;
                } else {
                   throw new Exception("ImportStatements must appear before GlobalVariableStatements.");
                }
             } else if(characters.charAt(1) == 'v'){
                hasGlobalVariableDeclarations=true;
                context.addProduction(new GlobalVariableDeclarations(variableOutput));
                return;
             }
             context.addProduction(new GlobalStatements(globalStatementsOutput));
             return;
          }
       }
       throw new Exception(exception);
    }
 
    @Override
    public void close(ProductionContext context) throws Exception {
       if(!hasProgramNamespace){
          context.handleFileError("No ProgramNamespace was declared in: ");
       }
    }
 }
