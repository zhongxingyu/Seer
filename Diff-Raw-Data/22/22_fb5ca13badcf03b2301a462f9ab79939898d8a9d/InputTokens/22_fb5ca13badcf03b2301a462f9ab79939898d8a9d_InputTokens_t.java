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
 
 import com.xforj.*;
 import java.util.regex.*;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class InputTokens extends Production {
    public InputTokens(Output output) {
       super(output);
    }
 
    @Override
    void execute(CharWrapper characters, ProductionContext context) throws Exception {
       Matcher inputTokens = characters.match(INPUT_TOKENS);
       if(inputTokens.find()){
          String oldTokens = inputTokens.group(1);
          characters.shift(oldTokens.length());
 
          String newTokens = oldTokens;
 
          if(context.stripNewLines){
             newTokens = newTokens.replaceAll("\\s++", " ");
          }
 
          if(context.minifyHTML){
             newTokens = newTokens.replaceAll("(>|<)\\s++|\\s++(>|<)", "$1$2");
          }
 
          newTokens = context.escapeOutput(newTokens);
          output.add(js_bld+"('"+newTokens+"');");
         context.removeProduction();
      } else {
         throw new Exception("Invlid Character found while evaluating InputTokens.");
       }
    }
 
 }
