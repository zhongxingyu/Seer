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
 
 public class ActivityData extends Entity {
 	protected ActivityData() {}
 
 	/**
 	 * @return the name of the topLevel
 	 */
 	public native final String getNameTop() /*-{
 		return this.nameTop;
 	}-*/;
 
 	/**
	 * @return the uri of the topLeve
 	 */
 	public native final String getUriTopLevel() /*-{
 		return this.uriTopLevel;
 	}-*/;
 	
 	/**
 	 * @return the age of the machine
 	 */
 	
 	public native final String getAge() /*-{
 		return this.age;
 	}-*/;
 	
 	/**
 	 * @return the total cost of the machin until now
 	 */
 
 	public native final String getCost() /*-{
 		return this.cost;
 	}-*/;
 
 
 	public static final native Collection<ActivityData> asCollection(String assumedSafe) /*-{
 		return eval("("+assumedSafe+")");
 		// return JSON.parse(assumedSafe);
 	}-*/;
 	public static final native ActivityData asActivityData(String assumedSafe) /*-{
 		return eval("("+assumedSafe+")");
 		// return JSON.parse(assumedSafe);
 	}-*/;
 
 }
