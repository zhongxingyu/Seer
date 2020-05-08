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
 
 package jsl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class VariableOutput extends Output {
    private VariableOutput parentScope;
    private static final String variablePrefix = "__";
    protected Map<String, Object> variables = new HashMap<String, Object>();
    protected ArrayList<String> keys = new ArrayList<>();
 
    public VariableOutput() {}
 
    public VariableOutput(VariableOutput parentScope) {
       this.parentScope = parentScope;
    }
 
    public VariableOutput add(String name, Object value) throws Exception {
       if(variables.containsKey("__"+name)){
          throw new Exception("The following variable has been declared twice: "+name);
       }
       if(value == null){
          throw new Exception("Null value was discovered for the following variable: \""+name+"\"");
       }
       variables.put("__"+name, value);
       keys.add("__"+name);
       return this;
    }
 
    public boolean hasVariableBeenDeclared(String name){
       String proposedName = variablePrefix+name;
      if(null == parentScope){
         int size = keys.size();
         if(variables.containsKey(proposedName) || size > 0 && proposedName.equals(keys.get(size-1))){
            return true;
         } else {
            return false;
         }
       }
      return parentScope.hasVariableBeenDeclared(name);
    }
 
    @Override
    public String toString(){
       if(keys.size() > 0){
          String first = keys.remove(0);
          String firstValue = variables.get(first).toString();
          prepend("var "+first);
 
          if(!"".equals(firstValue)){
             prepend("="+firstValue);
          }
          for(String key : keys){
             prepend(","+key);
             String value = variables.get(key).toString();
             if(!"".equals(value)){
                prepend("="+value);
             }
          }
          prepend(";");
          keys.clear();
          variables.clear();
       }
       return super.toString();
    }
 
 }
