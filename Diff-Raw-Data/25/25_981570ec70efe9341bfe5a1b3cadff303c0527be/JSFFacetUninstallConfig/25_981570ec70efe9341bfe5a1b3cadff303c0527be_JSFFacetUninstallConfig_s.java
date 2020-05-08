 package org.eclipse.jst.jsf.core.internal.project.facet;
 
 import org.eclipse.jst.common.project.facet.core.libprov.LibraryUninstallDelegate;
 import org.eclipse.wst.common.project.facet.core.ActionConfig;
 import org.eclipse.wst.common.project.facet.core.IActionConfigFactory;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 public class JSFFacetUninstallConfig
 
     extends ActionConfig
     
 {
     private LibraryUninstallDelegate librariesUninstallDelegate = null;
     
     public LibraryUninstallDelegate getLibrariesUninstallDelegate()
     {
         return this.librariesUninstallDelegate;
     }
 
     @Override
     public void setFacetedProjectWorkingCopy( final IFacetedProjectWorkingCopy fpjwc )
     {
         super.setFacetedProjectWorkingCopy( fpjwc );
         init();
     }
 
     @Override
     public void setProjectFacetVersion( final IProjectFacetVersion fv )
     {
         super.setProjectFacetVersion( fv );
         init();
     }
     
     private void init()
     {
         final IFacetedProjectWorkingCopy fpjwc = getFacetedProjectWorkingCopy();
         final IProjectFacetVersion fv = getProjectFacetVersion();
         
         if( this.librariesUninstallDelegate == null && fpjwc != null && fv != null )
         {
             this.librariesUninstallDelegate = new LibraryUninstallDelegate( fpjwc, fv );
         }
     }
     
     public static final class Factory
         
         implements IActionConfigFactory
         
     {
         public Object create()
         {
             return new JSFFacetUninstallConfig();
         }
     }
     
 }
