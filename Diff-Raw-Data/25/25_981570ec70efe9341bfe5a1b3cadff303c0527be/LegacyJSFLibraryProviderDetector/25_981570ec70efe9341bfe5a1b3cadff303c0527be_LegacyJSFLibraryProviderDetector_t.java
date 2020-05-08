 /******************************************************************************
  * Copyright (c) 2008 Oracle
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial implementation and ongoing maintenance
  ******************************************************************************/
 
 package org.eclipse.jst.jsf.core.internal.project.facet;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
 import org.eclipse.jst.common.project.facet.core.libprov.LegacyLibraryProviderDetector;
 import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.jsflibraryconfig.JSFLibraryRegistryUtil;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.jsflibraryconfiguration.JSFLibraryConfigurationHelper;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 
 /**
  * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
  */
@SuppressWarnings("deprecation")
 public final class LegacyJSFLibraryProviderDetector
 
     extends LegacyLibraryProviderDetector
     
 {
     private static final String LEGACY_JSF_LIBRARY_PROVIDER_ID 
         = "legacy-jsf-library-provider"; //$NON-NLS-1$
 
     @Override
     public ILibraryProvider detect( final IProject project,
                                     final IProjectFacet facet )
     {
         try
         {
             final IJavaProject jproj = JavaCore.create( project );
             
             for( IClasspathEntry cpe : jproj.getRawClasspath() )
             {
                 if( detect( cpe ) )
                 {
                     return LibraryProviderFramework.getProvider( LEGACY_JSF_LIBRARY_PROVIDER_ID );
                 }
             }
         }
         catch( Exception e )
         {
             JSFCorePlugin.log( e, e.getMessage() );
         }
 
         return null;
     }
     
    /**
     * @param cpe
     * @return true if the classpath entry is detected
     */
     public static boolean detect( final IClasspathEntry cpe )
     {
         if( cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER )
         {
             final IPath path = cpe.getPath();
             
             if( isJSFLibraryContainer( path ) ) 
             {
                 String libId = path.lastSegment();
                 JSFLibrary ref = JSFLibraryRegistryUtil.getInstance().getJSFLibraryRegistry().getJSFLibraryByID(libId);
                 
                 if( ref != null && ref.isImplementation() )
                 {
                     return true;
                 }
             }
         }
         
         return false;
     }
     
     private static boolean isJSFLibraryContainer(IPath path) {
         return path != null && path.segmentCount() == 2 && JSFLibraryConfigurationHelper.JSF_LIBRARY_CP_CONTAINER_ID.equals(path.segment(0));
     }
     
 }
