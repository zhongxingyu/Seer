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
 
 package jsl.strategies;
 
 import jsl.*;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class Operator extends Production {
    public Operator(Output output) {
       super(output);
    }
    
    @Override
    void execute(CharWrapper characters, ProductionContext context) throws Exception {
       characters.removeSpace();
       switch(characters.charAt(0)){
       case equal:
          if(characters.charAt(1) == equal){
             characters.shift(2);
             String value = "==";
             if(characters.charAt(0) == equal){
                characters.shift(1);
                value = value + "=";
             }
             output.prepend(value);
             context.removeProduction();
             return;
          }
          break;
       case exclamation:
          if(characters.charAt(1) == equal){
             characters.shift(2);
             String value = "!=";
             if(characters.charAt(0) == equal){
                characters.shift(1);
                value = value + "=";
             }
             output.prepend(value);
             context.removeProduction();
             return;
          }
          break;
       case pipe:
          if(characters.charAt(1) == pipe){
             characters.shift(2);
             output.prepend("||");
             context.removeProduction();
             return;
          }
          break;
       case amp:
          if(characters.charAt(1) == amp){
             characters.shift(2);
             output.prepend("&&");
             context.removeProduction();
             return;
          }
          break;
       case plus:
       case minus:
       case mod:
       case asterisk:
       case forward:
          output.prepend(characters.charAt(0));
         characters.shift(1);
          context.removeProduction();
          return;
       }
       throw new Exception("Invalid Operator.");
    }
 }
