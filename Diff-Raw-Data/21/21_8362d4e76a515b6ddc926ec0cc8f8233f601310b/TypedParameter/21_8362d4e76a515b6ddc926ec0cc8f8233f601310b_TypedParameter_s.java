 /**
  * @author Nigel Cook
  *
  * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 package n3phele.client.model;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 public class TypedParameter extends JavaScriptObject {
 	
 	protected TypedParameter() {}
 	
 	/**
 	 * @return the name
 	 */
 	public native final String getName() /*-{
 		return this.name;
 	}-*/;
 	
 	/**
 	 * name the name to set
 	 */
 	public native final void setName(String name) /*-{
 		this.name = name;
 	}-*/;
 	/**
 	 * @return the description
 	 */
 	public native final String getDescription() /*-{
 		return this.description;
 	}-*/;
 	/**
 	 * @return the type
 	 */
 	public native final String getType() /*-{
 		return this.type;
 	}-*/;
 	/**
 	 * @return the value
 	 */
 	public native final String getValue() /*-{
 		return this.value;
 	}-*/;
 	
 	/**
 	 * @param value the value to set
 	 */
 	public native final void setValue(String value) /*-{
 	    this.value = value;
 }-*/;
 	
 	
	
 	/**
 	 * @return the defaultValue
 	 */
 	public native final String getDefaultValue() /*-{
 		return this.defaultValue;
 	}-*/;
 	
 	
 }
