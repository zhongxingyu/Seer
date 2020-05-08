 /**
  * Digi-Lib - base library for Digi components
  * 
 * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
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
 
 package org.digimead.digi.lib.aop;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface cache {
 	// namespace id - where all cached date saved
 	int namespace() default 0;
 	// time period when data is actual
 	int period() default 0;
 	// check last boolean attribute of the method, if true, force renew
 	boolean examination() default false;
 }
 
