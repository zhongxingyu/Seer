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
 
 /**
  *
  * @author Joseph Spencer
  */
 public class VariableValue extends GlobalVariableValue {
    private boolean isNestedInContextSelector;
    public VariableValue(Output output, boolean nestedInContextSelector) {
       super(output);
       isNestedInContextSelector=nestedInContextSelector;
    }
 
    private boolean hasOpenParen;
    @Override
    void execute(CharWrapper characters, ProductionContext context) throws Exception {
       Matcher match;
       characters.removeSpace();
       switch(characters.charAt(0)){
       case ')':
          if(hasOpenParen){
             characters.shift(1);
             output.prepend(")");
             context.removeProduction();
             return;
          } else {
             throw new Exception("Invalid VariableValue:  Unexpected close paren.");
          }
       case '@':
       case '\'':
       case '"':
       case '0':
       case '1':
       case '2':
       case '3':
       case '4':
       case '5':
       case '6':
       case '7':
       case '8':
       case '9':
          super.execute(characters, context);
          return;
       case 'p':
          match=characters.match(POSITION_FN);
          if(match.find()){
             characters.shift(match.group(1).length());
             output.prepend(js_position);
             context.removeProduction();
             return;
          }
          break;
       case 'c':
          match=characters.match(COUNT_FN);
          if(match.find()){
             hasOpenParen=true;
             Output contextSelectorOutput = new Output();
             characters.shift(match.group(1).length());
             output.
                prepend(js_CountElements).
                prepend("(").
                prepend(contextSelectorOutput);
             context.addProduction(new ContextSelector(contextSelectorOutput, isNestedInContextSelector));
             addCountFunctionToGlobalParams(context);
             return;//we need to come back for the close paren.
          }
          break;
       case 'l':
          match=characters.match(LAST_FN);
          if(match.find()){
             characters.shift(match.group(1).length());
             output.prepend(js_last);
             context.removeProduction();
             return;
          }
          break;
       case 'n':
          match = characters.match(NULL);
          if(match.find()){
             super.execute(characters, context);
             return;
          }
          match = characters.match(NAME_FN);
          if(match.find()){
             characters.shift(match.group(1).length());
             output.prepend(js_name);
             context.removeProduction();
             return;
          }
          break;
       case 't':
       case 'f':
          match = characters.match(BOOLEAN);
          if(match.find()){
             super.execute(characters, context);
             return;
          }
       }
       context.removeProduction();
       context.addProduction(new ContextSelector(output, isNestedInContextSelector));
    }
 
    private void addCountFunctionToGlobalParams(ProductionContext context){
       context.getParams().
       put(js_CountElements,               
          //CountElements
          "function(f){"+
             "var o,"+
             "c=0,"+
             "n;"+
            "try{o=f()}catch(e){}"+
             "if(!!o && typeof(o)==='object'){"+
                "if(o.slice&&o.join&&o.pop){"+
                   "return o.length>>>0;"+
                "}else{"+
                   "for(n in o){"+
                      "c++;"+
                   "}"+
                "}"+
             "}"+
             "return c"+
          "}");   
    }
 }
