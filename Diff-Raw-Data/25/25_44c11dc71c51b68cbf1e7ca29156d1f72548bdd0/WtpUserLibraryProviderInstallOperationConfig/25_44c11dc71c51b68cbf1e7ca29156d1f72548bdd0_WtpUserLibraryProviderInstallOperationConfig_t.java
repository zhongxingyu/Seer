 /******************************************************************************
 * Copyright (c) 2008, 2012 Oracle
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial implementation and ongoing maintenance
  *    Paul Fullbright - [324111] Need better enablement behavior for WTP library providers
 *    Karen Butzke - [380963] IllegalArgumentException during initialization of WEB_FACET 
  ******************************************************************************/
 
 package org.eclipse.jst.j2ee.internal.common.classpath;
 
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
 import org.eclipse.jst.common.project.facet.core.libprov.LibraryProviderFramework;
 import org.eclipse.jst.common.project.facet.core.libprov.user.UserLibraryProviderInstallOperationConfig;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectBase;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
 
 /**
  * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
  */
 
 @SuppressWarnings( "boxing" )
 
public class WtpUserLibraryProviderInstallOperationConfig extends UserLibraryProviderInstallOperationConfig
 {
     private static final String CLASS_NAME 
         = WtpUserLibraryProviderInstallOperationConfig.class.getName();
     
     public static final String PROP_INCLUDE_WITH_APPLICATION_ENABLED 
         = CLASS_NAME + ".INCLUDE_WITH_APPLICATION_ENABLED"; //$NON-NLS-1$
 
     public static final String PROP_INCLUDE_WITH_APPLICATION_SETTING_ENABLED 
         = CLASS_NAME + ".INCLUDE_WITH_APPLICATION_SETTING_ENABLED"; //$NON-NLS-1$
 
     private boolean includeWithApplicationEnabled;
     private boolean includeWithApplicationSettingEnabled;
     private IFacetedProjectListener facetedProjectListener;
     
     @Override
     public synchronized void init( final IFacetedProjectBase fproj,
                                    final IProjectFacetVersion fv,
                                    final ILibraryProvider provider )
     {
         super.init( fproj, fv, provider );
         
         this.includeWithApplicationEnabled = hasModuleFacet( fproj );
         
         final IProject project = fproj.getProject();
         
         if( project != null )
         {
             final IProjectFacet f = fv.getProjectFacet();
             
             final ILibraryProvider currentProvider 
                 = LibraryProviderFramework.getCurrentProvider( project, f );
             
             if( currentProvider == provider )
             {
                 this.includeWithApplicationEnabled = false;
                 
                 final IJavaProject jproj = JavaCore.create( project );
                 
                 try
                 {
                     for( IClasspathEntry cpe : jproj.getRawClasspath() )
                     {
                         if( cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER )
                         {
                             final IPath path = cpe.getPath();
                             
                             if( path.segmentCount() >= 2 && path.segment( 0 ).equals( JavaCore.USER_LIBRARY_CONTAINER_ID ) )
                             {
                                 for( IClasspathAttribute attr : cpe.getExtraAttributes() )
                                 {
                                     if( attr.getName().equals( IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY ) )
                                     {
                                         this.includeWithApplicationEnabled = true;
                                         break;
                                     }
                                 }
                             }
                         }
                         
                         if( this.includeWithApplicationEnabled )
                         {
                             break;
                         }
                     }
                 }
                 catch( CoreException e )
                 {
                     throw new RuntimeException( e );
                 }
             }
         }
         
         this.includeWithApplicationSettingEnabled 
             = ( this.includeWithApplicationEnabled ) ? true : hasModuleFacet( fproj );
         
         this.facetedProjectListener = new IFacetedProjectListener() 
         {
             public void handleEvent( final IFacetedProjectEvent event ) 
             {
                 final boolean moduleFaceted = hasModuleFacet( event.getWorkingCopy() );
                 setIncludeWithApplicationEnabled( moduleFaceted );
                 setIncludeWithApplicationSettingEnabled( moduleFaceted );
             }
         };
         
         fproj.addListener( this.facetedProjectListener, IFacetedProjectEvent.Type.PROJECT_FACETS_CHANGED );
     }
 
     public boolean isIncludeWithApplicationEnabled()
     {
         return this.includeWithApplicationEnabled;
     }
     
     public void setIncludeWithApplicationEnabled( final boolean value )
     {
         final boolean oldValue = this.includeWithApplicationEnabled;
         this.includeWithApplicationEnabled = value;
         notifyListeners( PROP_INCLUDE_WITH_APPLICATION_ENABLED, oldValue, this.includeWithApplicationEnabled );
     }
 
     public boolean isIncludeWithApplicationSettingEnabled()
     {
         return this.includeWithApplicationSettingEnabled;
     }
     
     public void setIncludeWithApplicationSettingEnabled( final boolean value )
     {
         final boolean oldValue = this.includeWithApplicationSettingEnabled;
         this.includeWithApplicationSettingEnabled = value;
         notifyListeners( PROP_INCLUDE_WITH_APPLICATION_SETTING_ENABLED, oldValue, this.includeWithApplicationSettingEnabled );
     }
 
     public IClasspathAttribute[] getClasspathAttributes()
     {
         final IFacetedProjectBase fproj = getFacetedProject();
        final boolean isWebProject = isWebProject(fproj);
         
         IClasspathAttribute attr = null;
         
         if ( isIncludeWithApplicationSettingEnabled() )
         {
             if( isIncludeWithApplicationEnabled() )
             {
                 attr = JavaCore.newClasspathAttribute( IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY,
                                                        ClasspathDependencyUtil.getDefaultRuntimePath( isWebProject ).toString() );
             }
             else
             {
                 attr = JavaCore.newClasspathAttribute( IClasspathDependencyConstants.CLASSPATH_COMPONENT_NON_DEPENDENCY, "" ); //$NON-NLS-1$
             }
         }
         
         return ( attr == null ? null : new IClasspathAttribute[] { attr } );
     }
     
    private static boolean isWebProject(IFacetedProjectBase fproj) {
        if (ProjectFacetsManager.isProjectFacetDefined(IJ2EEFacetConstants.DYNAMIC_WEB)) {
            return fproj.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IJ2EEFacetConstants.DYNAMIC_WEB));
        }
        return false;
    }

     private static boolean hasModuleFacet( final IFacetedProjectBase fproj ) 
     {
         final Set<IProjectFacetVersion> moduleFacets = ProjectFacetsManager.getGroup( "modules" ).getMembers(); //$NON-NLS-1$
         
         for( IProjectFacetVersion facetVersion : fproj.getProjectFacets() ) 
         {
             if( moduleFacets.contains( facetVersion ) )
             {
                 return true;
             }
         }
         
         return false;
     }
     
     @Override
     public void dispose()
     {
         super.dispose();
         getFacetedProject().removeListener( this.facetedProjectListener );
     }
     
 }
