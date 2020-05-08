 /*
  * Copyright 2012 Joseph Spencer.
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
 
 import com.xforj.Output;
 import com.xforj.CharWrapper;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author Joseph Spencer
  */
 public abstract class AbstractVariableDeclaration extends Production {
    public AbstractVariableDeclaration(Output output) {
       super(output);
    }
 
    private boolean hasValue;
 
    @Override
    public final void execute(CharWrapper characters, ProductionContext context) throws Exception {
       characters.removeSpace();
       String extraExcMsg="";
 
       if(!hasValue){
          Matcher match = characters.match(getPattern());
          if(match.find()){
             characters.shift(match.group(1).length());
             characters.removeSpace();
             Matcher nameMatch = characters.match(NAME);
             if(nameMatch.find()){
                String name = nameMatch.group(1);
                characters.shift(name.length());
                if(characters.removeSpace()){
                  hasValue=true;
                   
                   Output assignmentOutput = new Output();
                   doAssignment(name, assignmentOutput);
                   context.getCurrentVariableOutput().add(name, assignmentOutput);
                   context.addProduction(getProduction(assignmentOutput));
                } else {
                   doNoAssignment(name, context);
                }
                return;
             }
          }
          extraExcMsg="  No Name found.";
       } else if(characters.charAt(0) == close){
          characters.shift(1);
          context.removeProduction();
          return;
       }
       throw new Exception("Invalid "+getClassName()+"."+extraExcMsg);
    }
    protected abstract Pattern getPattern();
    protected abstract Production getProduction(Output output);
    /**
     * This gives the instances a chance to add something special to the assignment.
     * 
     * For instance, in the case of ParamDeclarations, we want the following to be
     * prepended to the assignment: 'params.d||'.
     * 
     * @param name
     * @param output  The Assignment Output.
     * @throws Exception 
     */
    protected abstract void doAssignment(String name, Output output) throws Exception;
    protected abstract void doNoAssignment(String name, ProductionContext context) throws Exception;
 }
