 /*
    Copyright 2010 Andy Hedges <andy@hedges.net>
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package net.hedges.fandangled.bindings.domain;
 
 import net.hedges.fandangled.bindings.domain.document.Document;
 
 public class Return {
 
 	private TypeInfo typeInfo;
	private Document description;
 
 	public TypeInfo getTypeInfo() {
 		return typeInfo;
 	}
 
 	public void setTypeInfo(TypeInfo typeInfo) {
 		this.typeInfo = typeInfo;
 	}
 
 	public Document getDescription() {
 		return description;
 	}
 
 	public void setDescription(Document description) {
 		this.description = description;
 	}
 
 	@Override
 	public String toString() {
 		return "Return [typeInfo=" + typeInfo + ", description=" + description
 				+ "]";
 	}
 	
 	
 
 }
