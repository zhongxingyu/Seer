 /*
  * Copyright 2013 Blazebit.
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
 package com.blazebit.security;
 
 import java.util.Collection;
 
 /**
  *
  * @author Christian Beikov
  */
 public interface PermissionService {
 
     /**
      *
      * @param subject
      * @param action
      * @return
      */
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> boolean isGranted(Subject<R, P, Q> subject, Action action); // Resource
 
     /**
      *
      * @param authorizer
      * @param subject
      * @param action
      * @throws SecurityException
      */
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> void grant(Subject<R, P, Q> authorizer, Subject<R, P, Q> subject, Action action); // Resource
 
     /**
      *
      * @param authorizer
      * @param subject
      * @param action
      * @throws SecurityException
      */
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> void revoke(Subject<R, P, Q> authorizer, Subject<R, P, Q> subject, Action action); // Resource
 
     /**
      *
      * @param subject
      * @return
      */
     public <R extends Role<R, P, Q>, P extends Permission<?>, Q extends Permission<?>> Collection<Action> getAllowedActions(Subject<R, P, Q> subject); // Resource
 
     public Action getGrantAction();
 
     public Action getRevokeAction();
 
     public Action getAllAction();
 
     public Action getNoneAction();
 
     public Resource getAllResource();
 }
