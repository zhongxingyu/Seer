 /**
  * Copyright (c) 2010-2011 Martin M Reed
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hardisonbrewing.maven.cxx.xcode;
 
 import generated.Plist;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.hardisonbrewing.maven.core.JoJoMojoImpl;
 import org.hardisonbrewing.maven.core.TemplateService;
 
 /**
  * @goal xcode-ipa-manifest
  * @phase xcode-ipa-manifest
  */
 public final class GenerateIpaManifestMojo extends JoJoMojoImpl {
 
     private static final String DOWNLOAD_ICON_URL = "downloadIconUrl";
     private static final String ITUNES_ICON_URL = "itunesIconUrl";
 
     @Override
     public final void execute() {
 
         for (String target : XCodeService.getTargets()) {
             execute( target );
         }
     }
 
     private final void execute( String target ) {
 
         if ( !XCodeService.isApplicationType( target ) ) {
             StringBuffer stringBuffer = new StringBuffer();
             stringBuffer.append( "No targets found of type `" );
             stringBuffer.append( XCodeService.PRODUCT_TYPE_APPLICATION );
             stringBuffer.append( "`...skipping" );
             getLog().info( stringBuffer.toString() );
             return;
         }
 
         // http://developer.apple.com/library/ios/#featuredarticles/FA_Wireless_Enterprise_App_Distribution/Introduction/Introduction.html
 
         StringBuffer destPath = new StringBuffer();
         destPath.append( TargetDirectoryService.getTempPackagePath( target ) );
         destPath.append( File.separator );
         destPath.append( "manifest.vm" );
         File dest = new File( destPath.toString() );
 
         if ( !dest.getParentFile().exists() ) {
             dest.getParentFile().mkdirs();
         }
 
         getLog().info( "Generating " + destPath + "..." );
 
         Plist plist = XCodeService.readInfoPlist( XCodeService.getConvertedInfoPlist( target ) );
 
         StringBuffer ipaFilePath = new StringBuffer();
         ipaFilePath.append( target );
         ipaFilePath.append( "." );
         ipaFilePath.append( XCodeService.IPA_EXTENSION );
 
         VelocityContext velocityContext = new VelocityContext();
        velocityContext.put( "ipaUrl", ipaFilePath.toString() );
        velocityContext.put( "bundleIdentifier", XCodeService.getBundleIdentifier() );
         velocityContext.put( "bundleVersion", XCodeService.getBundleVersion() );
         velocityContext.put( "title", InfoPlistService.getString( plist, "CFBundleDisplayName" ) );
 
         File iconFile = null;
 
         String bundleIconFileId = InfoPlistService.getString( plist, InfoPlistService.PROP_BUNDLE_ICON );
         if ( bundleIconFileId == null || bundleIconFileId.length() == 0 ) {
 
             StringBuffer stringBuffer = new StringBuffer();
             stringBuffer.append( "There was no `" );
             stringBuffer.append( InfoPlistService.PROP_BUNDLE_ICON );
             stringBuffer.append( "` specified in the " );
             stringBuffer.append( InfoPlistService.INFO_PLIST );
             stringBuffer.append( "." );
             getLog().warn( stringBuffer.toString() );
 
             velocityContext.put( DOWNLOAD_ICON_URL, "" );
             velocityContext.put( ITUNES_ICON_URL, "" );
         }
         else {
             iconFile = XCodeService.getProjectFile( bundleIconFileId );
             if ( iconFile == null ) {
                 StringBuffer stringBuffer = new StringBuffer();
                 stringBuffer.append( "`" );
                 stringBuffer.append( InfoPlistService.PROP_BUNDLE_ICON );
                 stringBuffer.append( "` was specified in the " );
                 stringBuffer.append( InfoPlistService.INFO_PLIST );
                 stringBuffer.append( " but could not be located: " );
                 stringBuffer.append( bundleIconFileId );
                 getLog().error( stringBuffer.toString() );
                 throw new IllegalStateException();
             }
            String iconUrlValue = "${serverBaseUrl}" + iconFile.getName();
             velocityContext.put( DOWNLOAD_ICON_URL, iconUrlValue );
             velocityContext.put( ITUNES_ICON_URL, iconUrlValue );
         }
 
         Template template = TemplateService.getTemplate( "/xcode/ipaManifest.vm" );
 
         try {
             TemplateService.writeTemplate( template, velocityContext, dest );
         }
         catch (IOException e) {
             throw new IllegalStateException( e.getMessage(), e );
         }
     }
 }
