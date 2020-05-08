 /*
  *  Copyright 2011 Yannick LOTH.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.googlecode.wicketelements.security;
 
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 
 import java.lang.annotation.Annotation;
 import java.util.Collection;
 import java.util.Set;
 
 public interface SecurityCheck {
     <T extends Component, A extends Annotation> Set<String> findImpliedPermissions(final Class<T> componentClassParam, final Class<A> actionAnnotationClass);
 
    boolean isApplicationWithSignInPageSpecified();

     boolean isOnePermissionGivenToUser(final Collection<String> permissionsParam);
 
     <T extends Annotation> boolean impliesAction(final Class<T> annotationParam, final Class<? extends Annotation> impliedParam);
 
     boolean isSecurityAnnotatedComponent(final Class<? extends Component> componentClassParam);
 
     Class<? extends Page> signInPage();
 
     Class<? extends Page> signOutPage();
 
     boolean isSignInRequired();
 
     boolean isErrorPage(final Class<? extends Page> pageClassParam);
 
     boolean isSignInPage(final Class<? extends Page> pageClassParam);
 
     boolean isSignOutPage(final Class<? extends Page> pageClassParam);
 }
