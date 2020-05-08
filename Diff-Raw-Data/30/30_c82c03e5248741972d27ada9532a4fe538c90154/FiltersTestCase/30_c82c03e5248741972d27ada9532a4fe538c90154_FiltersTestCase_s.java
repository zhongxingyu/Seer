 /*
  *    Copyright 2010 The Meiyo Team
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.googlecode.meiyo.filter;
 
 import static com.googlecode.meiyo.filter.Filters.annotatedWithType;
 import static com.googlecode.meiyo.filter.Filters.inPackage;
 import static com.googlecode.meiyo.filter.Filters.inSubpackage;
 
 import java.util.List;
 
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 import com.googlecode.meiyo.ClassPath;
 
 /**
  * 
  *
  * @version $Id$
  */
 @Test
 public final class FiltersTestCase {
 
     public void verifyAnnotatedWithType() {
         assert annotatedWithType(Test.class).matches(this.getClass());
         assert !annotatedWithType(Parameters.class).matches(this.getClass());
     }
 
     @Test(expectedExceptions = { IllegalArgumentException.class })
     public void noRuntimeRetention() {
         annotatedWithType(NoRuntimeRetention.class);
     }
 
     public void verifyInPackage() {
         Filter inMeiyoPackage = inPackage("com.googlecode.meiyo");
         assert inMeiyoPackage.matches(ClassPath.class);
         assert !inMeiyoPackage.matches(List.class);
     }
 
     public void verifyInSubPackage() {
         Filter inMeiyoPackage = inSubpackage("com.googlecode.meiyo");
         assert inMeiyoPackage.matches(Filters.class);
         assert !inMeiyoPackage.matches(List.class);
     }
 
 }
