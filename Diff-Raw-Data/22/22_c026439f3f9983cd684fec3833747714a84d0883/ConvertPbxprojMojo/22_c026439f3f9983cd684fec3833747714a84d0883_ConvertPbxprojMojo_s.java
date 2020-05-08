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
 
 import generated.Dict;
 import generated.Plist;
 
 import java.io.File;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.hardisonbrewing.maven.core.JoJoMojoImpl;
 
 /**
  * @goal xcode-convert-pbxproj
  * @phase xcode-convert-pbxproj
  */
 public final class ConvertPbxprojMojo extends JoJoMojoImpl {
 
     /**
      * @parameter
      */
     public String provisioningProfile;
 
     private final Hashtable<String, String> groupIndex = new Hashtable<String, String>();
     private final Hashtable<String, String> fileIndex = new Hashtable<String, String>();
     private final Hashtable<String, Dict> keyIndex = new Hashtable<String, Dict>();
     private final Hashtable<String, Vector<String>> isaIndex = new Hashtable<String, Vector<String>>();
 
     @Override
     public final void execute() {
 
         File plistFile = getPlistFile();
         generatePlistFile( plistFile );
 
         Plist plist = PlistService.readPlist( plistFile );
         indexProperties( plist );
 
         Properties properties = buildProperties();
 
         for (Object key : properties.keySet()) {
             getLog().info( key + ": " + properties.getProperty( (String) key ) );
         }
 
         PropertiesService.storeXCodeProperties( properties );
     }
 
     private File getPlistFile() {
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( TargetDirectoryService.getTargetDirectoryPath() );
         stringBuffer.append( File.separator );
         stringBuffer.append( XCodeService.getProject() );
         stringBuffer.append( ".pbxproj.plist" );
         return new File( stringBuffer.toString() );
     }
 
     private Properties buildProperties() {
 
         Properties properties = new Properties();
 
         for (String key : isaIndex.get( "XCBuildConfiguration" )) {
 
             Dict dict = keyIndex.get( key );
 
             Dict buildSettings = PlistService.getDict( dict, "buildSettings" );
             if ( buildSettings == null ) {
                 continue;
             }
 
             String infoPlistFile = PlistService.getString( buildSettings, "INFOPLIST_FILE" );
             if ( infoPlistFile == null ) {
                 continue;
             }
 
             StringBuffer stringBuffer = new StringBuffer();
             stringBuffer.append( PlistService.getString( dict, "name" ) );
             stringBuffer.append( ".infoPlistFile" );
             String infoPlistFileKey = stringBuffer.toString();
 
             properties.put( infoPlistFileKey, infoPlistFile );
         }
 
         for (String key : isaIndex.get( "PBXGroup" )) {
             Dict dict = keyIndex.get( key );
             List<String> children = PlistService.getStringArray( dict, "children" );
             for (String child : children) {
                 groupIndex.put( child, key );
             }
         }
 
         for (String key : isaIndex.get( "PBXProject" )) {
             Dict dict = keyIndex.get( key );
             putDefaultCongurationName( dict, properties );
             putTargets( dict, properties );
         }
 
         for (String key : isaIndex.get( "PBXNativeTarget" )) {
             Dict dict = keyIndex.get( key );
             putTargetDefaultCongurationName( dict, properties );
             putTargetProductType( dict, properties );
             putTargetProductReference( dict, properties );
         }
 
         for (String key : isaIndex.get( "PBXFileReference" )) {
             Dict dict = keyIndex.get( key );
             String name = PlistService.getString( dict, "name" );
             if ( name == null ) {
                 name = PlistService.getString( dict, "path" );
             }
             String path = resolvePath( key );
             fileIndex.put( name, path );
         }
         XCodeService.setFileIndex( fileIndex );
 
         return properties;
     }
 
     private String resolvePath( String key ) {
 
         StringBuffer stringBuffer = new StringBuffer();
         while (key != null) {
             Dict dict = keyIndex.get( key );
             String path = PlistService.getString( dict, "path" );
             if ( path != null ) {
                 if ( stringBuffer.length() != 0 ) {
                     stringBuffer.insert( 0, File.separator );
                 }
                 stringBuffer.insert( 0, path );
             }
             key = groupIndex.get( key );
         }
         return stringBuffer.toString();
     }
 
     private void putTargets( Dict dict, Properties properties ) {
 
         List<String> targets = PlistService.getStringArray( dict, "targets" );
         String _targets = "";
 
        for (String target : targets) {
            Dict productReference = keyIndex.get( target );
            if ( !_targets.isEmpty() ) {
                _targets += ",";
             }
            _targets += PlistService.getString( productReference, "name" );
         }
 
         properties.put( "targets", _targets );
     }
 
     private void putDefaultCongurationName( Dict dict, Properties properties ) {
 
         String buildConfigurationListId = PlistService.getString( dict, "buildConfigurationList" );
         Dict buildConfigurationList = keyIndex.get( buildConfigurationListId );
         String defaultConfigurationName = PlistService.getString( buildConfigurationList, "defaultConfigurationName" );
         properties.put( "defaultConfigurationName", defaultConfigurationName );
     }
 
     private void putTargetProductType( Dict dict, Properties properties ) {
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( PlistService.getString( dict, "name" ) );
         stringBuffer.append( ".productType" );
         String propertyKey = stringBuffer.toString();
 
         String productType = PlistService.getString( dict, "productType" );
         properties.put( propertyKey, productType );
     }
 
     private void putTargetProductReference( Dict dict, Properties properties ) {
 
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( PlistService.getString( dict, "name" ) );
         stringBuffer.append( ".productReference" );
         String propertyKey = stringBuffer.toString();
 
         String productReferenceId = PlistService.getString( dict, "productReference" );
         Dict productReference = keyIndex.get( productReferenceId );
         String productReferencePath = PlistService.getString( productReference, "path" );
         properties.put( propertyKey, productReferencePath );
     }
 
     private void putTargetDefaultCongurationName( Dict dict, Properties properties ) {
 
         // defaultConfigurationName key
         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append( PlistService.getString( dict, "name" ) );
         stringBuffer.append( ".defaultConfigurationName" );
         String propertyKey = stringBuffer.toString();
 
         String buildConfigurationListId = PlistService.getString( dict, "buildConfigurationList" );
         Dict buildConfigurationList = keyIndex.get( buildConfigurationListId );
         String defaultConfigurationName = PlistService.getString( buildConfigurationList, "defaultConfigurationName" );
         properties.put( propertyKey, defaultConfigurationName );
     }
 
     private void indexProperties( Plist plist ) {
 
         Dict root = (Dict) PlistService.getRoot( plist );
         Dict objects = (Dict) PlistService.getValue( root, "objects" );
 
         List<Object> objectsValues = objects.getKeyOrArrayOrDataOrDateOrDictOrRealOrIntegerOrStringOrTrueOrFalse();
         for (int i = 0; i < objectsValues.size() - 1; i += 2) {
 
             generated.Key key = (generated.Key) objectsValues.get( i );
             Dict dict = (Dict) objectsValues.get( i + 1 );
             keyIndex.put( key.getvalue(), dict );
 
             String isa = PlistService.getString( dict, "isa" );
             if ( isa != null ) {
                 Vector<String> keys = isaIndex.get( isa );
                 if ( keys == null ) {
                     keys = new Vector<String>();
                 }
                 keys.add( key.getvalue() );
                 isaIndex.put( isa, keys );
             }
         }
     }
 
     private void generatePlistFile( File file ) {
 
         String pbxprojFilePath = XCodeService.getPbxprojPath();
 
         List<String> cmd = new LinkedList<String>();
         cmd.add( "plutil" );
         cmd.add( "-convert" );
         cmd.add( "xml1" );
         cmd.add( "-o" );
         cmd.add( file.getAbsolutePath() );
         cmd.add( pbxprojFilePath );
         execute( cmd );
     }
 }
