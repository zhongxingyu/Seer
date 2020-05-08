 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.eclipse.nature;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.preference.IPersistentPreferenceStore;
 import org.seasar.dolteng.core.types.AsTypeResolver;
 import org.seasar.dolteng.core.types.MxComponentValueResolver;
 import org.seasar.dolteng.core.types.TypeMappingRegistry;
 import org.seasar.dolteng.core.types.impl.AsTypeResolverImpl;
 import org.seasar.dolteng.core.types.impl.BasicTypeMappingRegistry;
 import org.seasar.dolteng.core.types.impl.KuinaTypeMappingRegistry;
 import org.seasar.dolteng.core.types.impl.MxComponentValueResolverImpl;
 import org.seasar.dolteng.core.types.impl.StandardTypeMappingRegistry;
 import org.seasar.dolteng.eclipse.Constants;
 import org.seasar.dolteng.eclipse.DoltengCore;
 import org.seasar.dolteng.eclipse.DoltengProject;
 import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
 import org.seasar.dolteng.eclipse.preferences.impl.DoltengPreferencesImpl;
 
 /**
  * @author taichi
  * 
  */
 public class DoltengNature implements DoltengProject, IProjectNature {
 
     protected IProject project;
 
     protected DoltengPreferences preference;
 
     protected BasicTypeMappingRegistry registry;
 
     protected AsTypeResolverImpl resolver;
 
     protected MxComponentValueResolverImpl mxResolver;
 
     public DoltengNature() {
         super();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.resources.IProjectNature#configure()
      */
     public void configure() {
         init();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.resources.IProjectNature#deconfigure()
      */
     public void deconfigure() {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.resources.IProjectNature#getProject()
      */
     public IProject getProject() {
         return this.project;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core
      * .resources.IProject)
      */
     public void setProject(IProject project) {
         this.project = project;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.DoltengProject#getProjectPreferences()
      */
     public synchronized DoltengPreferences getProjectPreferences() {
         if (this.preference == null) {
             init();
         }
         return this.preference;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.seasar.dolteng.eclipse.DoltengProject#getTypeMappingRegistry()
      */
     public synchronized TypeMappingRegistry getTypeMappingRegistry() {
         if (this.registry == null) {
             init();
         }
         return this.registry;
     }
 
     public synchronized AsTypeResolver getAsTypeResolver() {
         if (this.resolver == null) {
             init();
         }
         return this.resolver;
     }
 
     public synchronized MxComponentValueResolver getMxComponentValueResolver() {
         if (this.mxResolver == null) {
             init();
         }
         return this.mxResolver;
     }
 
     public void init() {
         try {
             this.preference = new DoltengPreferencesImpl(getProject());
             if (Constants.DAO_TYPE_KUINADAO
                     .equals(this.preference.getDaoType())) {
                 this.registry = new KuinaTypeMappingRegistry();
            } else if (Constants.DAO_TYPE_S2JDBC.equals(this.preference
                    .getDaoType())) {
                this.registry = new BasicTypeMappingRegistry();
             } else {
                 this.registry = new StandardTypeMappingRegistry();
             }
             this.registry.initialize();
 
             this.resolver = new AsTypeResolverImpl();
             this.resolver.initialize();
             this.mxResolver = new MxComponentValueResolverImpl();
             this.mxResolver.initialize();
         } catch (Exception e) {
             DoltengCore.log(e);
         }
     }
 
     public synchronized void destroy() {
         try {
             IPersistentPreferenceStore store = getProjectPreferences()
                     .getRawPreferences();
             store.save();
         } catch (Exception e) {
             DoltengCore.log(e);
         }
     }
 
     public static DoltengNature getInstance(IProject project) {
         if (project != null && project.isOpen()) {
             try {
                 IProjectNature nature = project.getNature(Constants.ID_NATURE);
                 if (nature instanceof DoltengNature) {
                     return (DoltengNature) nature;
                 }
             } catch (CoreException e) {
                 DoltengCore.log(e);
             }
         }
         return null;
     }
 
 }
