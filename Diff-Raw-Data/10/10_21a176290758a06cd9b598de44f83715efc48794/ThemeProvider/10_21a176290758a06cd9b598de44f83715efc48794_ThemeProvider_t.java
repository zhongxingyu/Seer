 /*
  * Copyright (c) 2013, Blackboard, Inc. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided with the distribution. 3. Neither the
  * name of the Blackboard Inc. nor the names of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * BLACKBOARD MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-
  * INFRINGEMENT. BLACKBOARD SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
  * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  */
 
 package blackboard.plugin.hayabusa.provider;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import blackboard.data.navigation.NavigationItem;
 import blackboard.data.navigation.NavigationItemControl;
 import blackboard.persist.PersistenceRuntimeException;
 import blackboard.persist.navigation.NavigationItemDbLoader;
 import blackboard.platform.branding.BrandingUtil;
 import blackboard.platform.branding.common.Branding;
 import blackboard.platform.branding.common.ColorPalette;
 import blackboard.platform.branding.common.Theme;
 import blackboard.platform.branding.service.BrandingManager;
 import blackboard.platform.branding.service.ColorPaletteManager;
 import blackboard.platform.branding.service.ColorPaletteManagerFactory;
 import blackboard.platform.branding.service.ThemeManagerFactory;
 import blackboard.platform.context.Context;
 import blackboard.platform.context.ContextManagerFactory;
 import blackboard.platform.security.NonceUtil;
 import blackboard.plugin.hayabusa.command.Category;
 import blackboard.plugin.hayabusa.command.Command;
 import blackboard.plugin.hayabusa.command.PostCommand;
 import blackboard.portal.data.PortalBranding;
 import blackboard.portal.persist.PortalBrandingDbLoader;
 
 import com.google.common.collect.Sets;
 
 /**
  * A {@link Provider} for courses
  * 
  * @author Zhaoxia Yang
  * @since 1.0
  */
 public class ThemeProvider implements Provider
 {
   public static final String NONCE_ID = "blackboard.webapps.portal.brands.struts.CustomizeBrandForm";
   public static final String NONCE_CONTEXT = "/webapps/portal";
   public static final String URI = "/webapps/portal/execute/brands/customizeBrand";
 
   @Override
   public Iterable<Command> getCommands()
   {
     List<Theme> themes = ThemeManagerFactory.getInstance().getAllThemes();
     Set<Command> commands = Sets.newTreeSet();
     Context bbCtxt = ContextManagerFactory.getInstance().getContext();
     NavigationItem ni = null;
     List<PortalBranding> portalBrandings = null;
     PortalBranding currentPortalBranding = null;
     Branding branding = null;
     ColorPalette colorPalette = null;
     Theme currentTheme = null;
     try
     {
       NavigationItemDbLoader niDbLoader = NavigationItemDbLoader.Default.getInstance();
       ni = niDbLoader.loadByInternalHandle( "pa_customize_brand" );
       NavigationItemControl nic = NavigationItemControl.createInstance( ni );
       if ( !nic.userHasAccess() )
       {
         return commands;
       }
       currentTheme = BrandingUtil.getCurrentBrandTheme( bbCtxt.getHostName() );
       branding = BrandingManager.Factory.getInstance().getBrandingByHostNameAndRole( bbCtxt.getHostName(), null );
       ColorPaletteManager colorPaletteManager = ColorPaletteManagerFactory.getInstance();
       colorPalette = colorPaletteManager.getColorPaletteByBrandingId( branding.getId() );
 
       PortalBrandingDbLoader pbLoader = PortalBrandingDbLoader.Default.getInstance();
       portalBrandings = pbLoader.loadByThemeId( currentTheme.getId() );
     }
     catch ( Exception e )
     {
       throw new PersistenceRuntimeException( e );
     }
 
     for ( PortalBranding pb : portalBrandings )
     {
       if ( pb.isDefault() )
       {
         currentPortalBranding = pb;
         break;
       }
     }
     for ( Theme theme : themes )
     {
       String themeExtRef = theme.getExtRef();
 
       HashMap<String, String> params = new HashMap<String, String>();
       params.put( "cmd", "save" );
       params.put( "brand_id", branding.getId().getExternalString() );
       params.put( "pageType", "Navigation" );
       params.put( "usesCustomBrand", "true" );
       params.put( "startThemeExtRef", currentTheme.getExtRef() );
      String colorExtRef = "";
      if ( colorPalette != null )
      {
        colorExtRef = colorPalette.getExtRef();
      }
      params.put( "startPaletteExtRef", colorExtRef );
      params.put( "color_palette_extRef", colorExtRef );
       params.put( "theme_extRef", themeExtRef );
       params.put( "deleteBrandCss", "false" );
       params.put( "tabStyle", currentTheme.getTabStyle().getAbbrevString() );
       params.put( "tabAlign", currentTheme.getTabAlignment().toString() );
       params.put( "frameSize", currentTheme.getFrameSize().toString() );
 
       params.put( "bannerImage_attachmentType", "AL" );
       params.put( "bannerImage_fileId", currentPortalBranding.getBannerImage() );
       params.put( "bannerImage_LocalFile0", "" );
       params.put( "bannerImageLink", currentPortalBranding.getBannerUrl() );
       params.put( "bannerAltText", currentPortalBranding.getBannerText() );
       params.put( "pde_institution_role", bbCtxt.getUser().getPortalRoleId().toExternalString() );
 
       params.put( "courseNameUsage", branding.getCourseNameUsage().toExternalString() );
 
       params.put( NonceUtil.NONCE_KEY, NonceUtil.create( bbCtxt.getSession(), NONCE_ID, NONCE_CONTEXT ) );
 
       commands.add( new PostCommand( themeExtRef, URI, Category.THEME, params, "multipart/form-data" ) );
     }
     return commands;
   }
 }
