 /**
  * Copyright (C) 2012-2014 Philip W. Sorst <philip@sorst.net>
  * and individual contributors as indicated
  * by the @authors tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.dontdrinkandroot.wicket.bootstrap.css;
 
 import net.dontdrinkandroot.wicket.css.CssClass;
 
 
 public enum FontAwesomeIconClass implements CssClass {
 
 	ASTERISK("fa-asterisk"),
 	CHECK("fa-check"),
	COG("fa-cog"),
 	FEMALE("fa-female"),
 	LOCK("fa-lock"),
 	MALE("fa-male"),
 	PENCIL("fa-pencil"),
 	PLUS("fa-plus"),
 	POWER_OFF("fa-power-off"),
	TIMES("fa-times"),
 	TRASH("fa-trash");
 
 	private final String classString;
 
 
 	private FontAwesomeIconClass(String classString) {
 
 		this.classString = classString;
 	}
 
 
 	@Override
 	public String getClassString() {
 
 		return this.classString;
 	}
 
 }
