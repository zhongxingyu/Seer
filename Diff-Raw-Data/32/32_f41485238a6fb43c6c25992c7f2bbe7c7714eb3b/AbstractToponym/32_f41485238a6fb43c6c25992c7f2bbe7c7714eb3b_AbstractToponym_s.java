 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.model.geo;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
import de.cosmocode.json.JSONRenderer;
 
 /**
  * Abstract base implementation of the {@link ToponymBase} interface.
  * 
  * @author Willi Schoenborn
  */
 @MappedSuperclass
 public abstract class AbstractToponym implements ToponymBase {
     
     @Id
     private long id;
 
     private String name;
 
     @Column(name = "country_code")
     private String countryCode;
 
     @Override
     public long getId() {
         return id;
     }
     
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public String getCountryCode() {
         return countryCode;
     }
 
     @Override
    public JSONRenderer renderAsMap(JSONRenderer renderer) {
        return renderer.
             key("id").value(getId()).
             key("name").value(getName()).
             key("countryCode").value(getCountryCode()).
            key("aliases").array(getAliases());
     }
 }
