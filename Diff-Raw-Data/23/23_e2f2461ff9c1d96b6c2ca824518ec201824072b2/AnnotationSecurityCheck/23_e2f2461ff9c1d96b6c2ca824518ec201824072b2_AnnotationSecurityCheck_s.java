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
 
 import com.googlecode.jbp.common.annotations.AnnotationHelper;
 import com.googlecode.wicketelements.security.annotations.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.settings.IApplicationSettings;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import static com.googlecode.jbp.common.requirements.Reqs.PARAM_REQ;
 
 public class AnnotationSecurityCheck implements SecurityCheck {
 
     public <T extends Component> List<Class<? extends SecurityConstraint>> findSecurityConstraintsForEnable(final T componentParam) {
        return Arrays.asList(componentParam.getClass().getAnnotation(EnableAction.class).constraints());
     }
 
     public <T extends Component> List<Class<? extends SecurityConstraint>> findSecurityConstraintsForRender(final T componentParam) {
        return Arrays.asList(componentParam.getClass().getAnnotation(RenderAction.class).constraints());
     }
 
     public <T extends Component> List<Class<? extends InstantiationSecurityConstraint>> findSecurityConstraintsForInstantiation(final Class<T> componentClassParam) {
        return Arrays.asList(componentClassParam.getAnnotation(InstantiateAction.class).constraints());
     }
 
     public <T extends Component> boolean isAllSecurityConstraintsSatisfiedForAction(final T componentParam, final List<Class<? extends SecurityConstraint>> securityConstraintsParam) {
         for (final Class<? extends SecurityConstraint> constraintClass : securityConstraintsParam) {
             SecurityConstraint constraint = instantiateSecurityConstraint(constraintClass);
             if (!constraint.isSatisfiedConstraint(componentParam)) {
                 return false;
             }
         }
         return true;
     }
 
     private SecurityConstraint instantiateSecurityConstraint(Class<? extends SecurityConstraint> constraintClassParam) {
         try {
             for (final Method m : constraintClassParam.getDeclaredMethods()) {
                 if (m.isAnnotationPresent(Factory.class)) {
                     try {
                         return (SecurityConstraint) m.invoke(null);
                     } catch (InvocationTargetException ex) {
                         throw new IllegalStateException("Cannot execute factory method for InstantiationSecurityConstraint: " + constraintClassParam.getName());
                     }
                 }
             }
             return constraintClassParam.newInstance();
         } catch (InstantiationException ex) {
             throw new IllegalStateException("Cannot instantiate security constraint class.", ex);
         } catch (IllegalAccessException ex) {
             throw new IllegalStateException("Cannot instantiate security constraint class.", ex);
         }
     }
 
     public <T extends Component> boolean isAllSecurityConstraintsSatisfiedForInstantiation(final Class<T> componentClassParam, final List<Class<? extends InstantiationSecurityConstraint>> securityConstraintsParam) {
         for (final Class<? extends InstantiationSecurityConstraint> constraintClass : securityConstraintsParam) {
             InstantiationSecurityConstraint constraint = instantiateSecurityConstraint(constraintClass);
             if (!constraint.isSatisfiedConstraint(componentClassParam)) {
                 return false;
             }
         }
         return true;
     }
 
     private InstantiationSecurityConstraint instantiateSecurityConstraint(Class<? extends InstantiationSecurityConstraint> constraintClassParam) {
         try {
             for (final Method m : constraintClassParam.getDeclaredMethods()) {
                 if (m.isAnnotationPresent(Factory.class)) {
                     try {
                         return (InstantiationSecurityConstraint) m.invoke(null);
                     } catch (InvocationTargetException ex) {
                         throw new IllegalStateException("Cannot execute factory method for InstantiationSecurityConstraint: " + constraintClassParam.getName());
                     }
                 }
             }
             return constraintClassParam.newInstance();
         } catch (InstantiationException ex) {
             throw new IllegalStateException("Cannot instantiate security constraint class.", ex);
         } catch (IllegalAccessException ex) {
             throw new IllegalStateException("Cannot instantiate security constraint class.", ex);
         }
     }
 
     public final boolean isSecurityAnnotatedComponent(final Class<? extends Component> componentClassParam) {
         return AnnotationHelper.isQualifiedAnnotationPresent(componentClassParam, SecurityActionQualifier.class);
     }
 
     public final boolean isAtLeastOnePermissionGivenToUser(final Collection<String> permissionsParam) {
         final IUser user = SecureSession.get().getUser();
         for (final String perm : permissionsParam) {
             if (!StringUtils.isBlank(perm) && user.hasPermission(perm)) {
                 return true;
             }
         }
         return false;
     }
 
     public final <T extends Component, A extends Annotation> Set<String> findImpliedPermissions(final Class<T> componentClassParam, final Class<A> actionAnnotationClass) {
         PARAM_REQ.Object.requireNotNull(componentClassParam, "Component class must not be null.");
         PARAM_REQ.Object.requireNotNull(actionAnnotationClass, "Action annotation class must not be null.");
         final Set<String> impliedPermissions = new HashSet<String>();
         final List<Annotation> securityAnnotations = AnnotationHelper.getQualifiedAnnotations(componentClassParam, SecurityActionQualifier.class);
         for (final Annotation securityAnnotation : securityAnnotations) {
             if (impliesAction(securityAnnotation.annotationType(), actionAnnotationClass)) {
                 if (securityAnnotation instanceof InstantiateAction) {
                     final InstantiateAction action = (InstantiateAction) securityAnnotation;
                     impliedPermissions.add(action.permission());
                 } else if (securityAnnotation instanceof RenderAction) {
                     final RenderAction action = (RenderAction) securityAnnotation;
                     impliedPermissions.add(action.permission());
                 } else if (securityAnnotation instanceof EnableAction) {
                     final EnableAction action = (EnableAction) securityAnnotation;
                     impliedPermissions.add(action.permission());
                 }
             }
         }
         return impliedPermissions;
     }
 
     private final <T extends Annotation> boolean impliesAction(final Class<T> annotationParam, final Class<? extends Annotation> impliedParam) {
         if (impliedParam.isAssignableFrom(annotationParam)) {
             return true;
         }
         if (annotationParam.isAnnotationPresent(ImpliesSecurityAction.class)) {
             final ImpliesSecurityAction a = annotationParam.getAnnotation(ImpliesSecurityAction.class);
             for (final Class<? extends Annotation> annotClass : a.impliedActions()) {
                 return impliesAction(annotClass, impliedParam);
             }
         }
         return false;
     }
 
     public final boolean isSignInPage(final Class<? extends Page> pageClassParam) {
         PARAM_REQ.Object.requireNotNull(pageClassParam, "Sign in page parameter must not be null.");
         return pageClassParam.equals(signInPage()) || pageClassParam.isAnnotationPresent(PageWithSignIn.class);
     }
 
     public final boolean isSignOutPage(final Class<? extends Page> pageClassParam) {
         PARAM_REQ.Object.requireNotNull(pageClassParam, "Sign out page parameter must not be null.");
         return pageClassParam.equals(signOutPage());
     }
 
     public final boolean isErrorPage(final Class<? extends Page> pageClassParam) {
         PARAM_REQ.Object.requireNotNull(pageClassParam, "Eror page parameter must not be null.");
         final IApplicationSettings settings = Application.get().getApplicationSettings();
         return pageClassParam.isAssignableFrom(settings.getAccessDeniedPage())
                 || pageClassParam.isAssignableFrom(settings.getInternalErrorPage())
                 || pageClassParam.isAssignableFrom(settings.getPageExpiredErrorPage()) || isComplementaryErrorPage(pageClassParam);
     }
 
     /**
      * This method may be overridden to check if a page is a custom complementary error page.  For example, these pages
      * may be shown in case of specific exceptions.
      *
      * @param pageClassParam The page.
      * @return {@code true} if the page is a custom complementary error page, {@code false} else.
      */
     protected boolean isComplementaryErrorPage(final Class<? extends Page> pageClassParam) {
         return false;
     }
 
     public final boolean isSignInRequired() {
         return Application.get().getClass().isAnnotationPresent(SignInRequired.class);
     }
 
     public final boolean isApplicationWithSignInPageSpecified() {
         if (Application.get().getClass().isAnnotationPresent(SignIn.class)) {
             final SignIn annot = Application.get().getClass().getAnnotation(SignIn.class);
             return (annot.page() != null);
         }
         return false;
     }
 
     public final Class<? extends Page> signInPage() {
         Class<? extends Page> page = Application.get().getHomePage();
         if (Application.get().getClass().isAnnotationPresent(SignIn.class)) {
             final SignIn annot = Application.get().getClass().getAnnotation(SignIn.class);
             page = annot.page();
         }
         return page;
     }
 
     public final Class<? extends Page> signOutPage() {
         Class<? extends Page> page = null;
         if (Application.get().getClass().isAnnotationPresent(SignOut.class)) {
             final SignOut annot = Application.get().getClass().getAnnotation(SignOut.class);
             page = annot.page();
         }
         return page;
     }
 }
