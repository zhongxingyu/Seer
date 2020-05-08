 /*
  * Copyright 2008-2012 Zuse Institute Berlin (ZIB)
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
  *
  */
 
 package de.zib.gndms.taskflows.failure.server.utils;
 
 /**
  * @date: 23.03.12
  * @time: 11:10
  * @author: Jörg Bachmann
  * @email: bachmann@zib.de
  */
 public class ExceptionThrower {
    public static void throwUncheked( Throwable e ) {
         ExceptionThrower.< RuntimeException >throwIt(e);
     }
 
     @SuppressWarnings("unchecked")
     public static < E extends Throwable > void throwIt( Throwable e ) throws E {
         throw ( E )e;
     }
 }
