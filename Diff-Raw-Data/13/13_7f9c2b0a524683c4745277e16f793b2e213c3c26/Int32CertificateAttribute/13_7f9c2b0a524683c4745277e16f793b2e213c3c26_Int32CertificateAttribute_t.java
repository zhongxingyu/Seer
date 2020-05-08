 /*
  * Copyright 2012 Piratenpartei Schweiz
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.piratenpartei.pivote.model.crypto;
 
 import ch.piratenpartei.pivote.serialize.util.Field;
 import ch.piratenpartei.pivote.serialize.util.Serialize;
 
 import static com.google.common.base.Preconditions.*;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 @Serialize(@Field(name = "value", type = "int32"))
 public class Int32CertificateAttribute extends CertificateAttribute {
 
     @Override
     public Integer getValue() {
         return (Integer)super.getValue();
     }
 
    public void setValue(Integer value) {
        super.setValue(value);
    }

     @Override
     public void setValue(Object value) {
         checkArgument(value != null || !(value instanceof Integer), "Integer value expected");
         super.setValue(value);
     }
 }
