 /**
  * Copyright (C) 2012 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcmixins4j.plugin;
 
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.emftext.language.java.JavaClasspath;
 import org.emftext.language.java.resource.java.IJavaOptions;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handles mixin source code generation in Eclipse.
  */
 public final class SrcMixins4JPlugin extends Plugin {
 
     private static final Logger LOG = LoggerFactory
             .getLogger(SrcMixins4JPlugin.class);
 
     private static SrcMixins4JPlugin plugin;
 
     private ResourceSet resourceSet = null;
 
     private IElementChangedListener cpChangeListener;
 
     private IJavaProject currentProject = null;
 
     @Override
     public final void start(final BundleContext context) throws Exception {
         super.start(context);
         LOG.trace("BEGIN start(BundleContext");
         plugin = this;
         cpChangeListener = new IElementChangedListener() {
             @Override
             public void elementChanged(final ElementChangedEvent event) {
                 final IJavaElementDelta delta = event.getDelta();
                 if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0) {
                     resetClasspath();
                 }
             }
         };
         JavaCore.addElementChangedListener(cpChangeListener);
         LOG.trace("END start(BundleContext");
     }
 
     @Override
     public final void stop(final BundleContext context) throws Exception {
         LOG.trace("BEGIN stop(BundleContext");
         JavaCore.removeElementChangedListener(cpChangeListener);
         plugin = null;
         cpChangeListener = null;
         super.stop(context);
         LOG.trace("END stop(BundleContext");
     }
 
     /**
      * Returns the plugin itself.
      * 
      * @return Plugin.
      */
     public static SrcMixins4JPlugin getDefault() {
         return plugin;
     }
 
     /**
      * Returns a resource set for a given project. The resource set is cached
      * until a resource set for another class path is requested.
      * 
      * @param project
      *            Project to return a resource set for.
      * 
      * @return Resource set.
      */
     public final ResourceSet getResourceSet(final IJavaProject project) {
         if (LOG.isTraceEnabled()) {
             LOG.trace("BEGIN getResourceSet(IJavaProject");
             LOG.trace("project: " + project.getElementName());
         }
 
         if (resourceSet == null) {
             LOG.info("Create new ResourceSet");
             resourceSet = new ResourceSetImpl();
             resourceSet.getLoadOptions().put(
                     IJavaOptions.DISABLE_CREATING_MARKERS_FOR_PROBLEMS,
                    Boolean.TRUE);
         }
 
         if (!project.equals(currentProject)) {
             LOG.info("project != currentProject [project=" + asString(project)
                     + ", currentProject=" + asString(currentProject) + "]");
             resetClasspath();
         }
         currentProject = project;
 
         if (LOG.isTraceEnabled()) {
             LOG.trace("resourceSet: " + resourceSet);
             LOG.trace("END getResourceSet(IJavaProject");
         }
         return resourceSet;
     }
 
     private static String asString(final IJavaProject project) {
         if (project == null) {
             return "null";
         }
         return project.getElementName() + "@"
                 + Integer.toHexString(System.identityHashCode(project));
     }
 
     /**
      * Reset the cached class path.
      */
     public final void resetClasspath() {
         LOG.trace("BEGIN resetClasspath()");
         for (Adapter a : resourceSet.eAdapters()) {
             if (a instanceof JavaClasspath) {
                 resourceSet.eAdapters().remove(a);
                 LOG.trace("removed: " + a);
                 break;
             }
         }
         LOG.trace("END resetClasspath()");
     }
 
 }
