 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  * research group.
  * 
  * Copyright 2005-2012 e-UCM research group.
  * 
  * e-UCM is a research group of the Department of Software Engineering and
  * Artificial Intelligence at the Complutense University of Madrid (School of
  * Computer Science).
  * 
  * C Profesor Jose Garcia Santesmases sn, 28040 Madrid (Madrid), Spain.
  * 
  * For more info please visit: <http://e-adventure.e-ucm.es> or
  * <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  * You can access a list of all the contributors to eAdventure at:
  * http://e-adventure.e-ucm.es/contributors
  * 
  * ****************************************************************************
  * eAdventure is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * eAdventure is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Adventure. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.loader;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 
 import es.eucm.eadventure.common.data.adaptation.AdaptationProfile;
 import es.eucm.eadventure.common.data.adaptation.AdaptationRule;
 import es.eucm.eadventure.common.data.adaptation.AdaptedState;
 import es.eucm.eadventure.common.data.adventure.AdventureData;
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.data.animation.Animation;
 import es.eucm.eadventure.common.data.animation.ImageLoaderFactory;
 import es.eucm.eadventure.common.data.assessment.AssessmentProfile;
 import es.eucm.eadventure.common.data.chapter.Chapter;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.incidences.Incidence;
 import es.eucm.eadventure.common.loader.parsers.AdaptationHandler;
 import es.eucm.eadventure.common.loader.parsers.AdventureHandler;
 import es.eucm.eadventure.common.loader.parsers.AnimationHandler;
 import es.eucm.eadventure.common.loader.parsers.AssessmentHandler;
 import es.eucm.eadventure.common.loader.parsers.ChapterHandler;
 import es.eucm.eadventure.common.loader.parsers.DescriptorHandler;
 
 /**
  * This class loads the e-Adventure data from a XML file
  */
 public class Loader {
 
     /**
      * AdventureData structure which has been previously read. (For Debug
      * execution)
      */
     private static AdventureData adventureData;
 
     /**
      * Cache the SaxParserFactory
      */
     private static SAXParserFactory factory = SAXParserFactory.newInstance( );
 
     /**
      * Private constructor
      */
     private Loader( ) {
 
     }
 
     /**
      * Loads the adventure data from the given ZIP file.
      * 
      * @param zipFile
      *            Path to the zip file which holds the adventure
      * @return The adventure data, null if there was an error
      */
     public static AdventureData loadAdventureData( InputStreamCreator isCreator, List<Incidence> incidences ) {
 
        AdventureData adventureData = null;
         try {
             // Set the adventure handler
             AdventureHandler adventureParser = new AdventureHandler( isCreator, incidences );
             factory.setValidating( false );
             SAXParser saxParser = factory.newSAXParser( );
 
             // Read and close the input stream
             InputStream descriptorIS = isCreator.buildInputStream( "descriptor.xml" );
             saxParser.parse( descriptorIS, adventureParser );
             descriptorIS.close( );
 
             // Load the assessment and adaptation profiles. It must be after parse
             // the adventure data because the profile's load from xml inserts each profile
             // in each chapter.
             adventureParser.loadProfiles( );
             // Store the adventure data
            adventureData = adventureParser.getAdventureData( );
 
         }
         catch( ParserConfigurationException e ) {
             incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.SAX" ), e ) );
         }
         catch( SAXException e ) {
             incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.SAX" ), e ) );
         }
         catch( IOException e ) {
             incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.IO" ), e ) );
         }
         catch( IllegalArgumentException e ) {
             incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.NoDescriptor" ), e ) );
         }
 
        return adventureData;
     }
 
     /**
      * Loads the descriptor of the current ZIP adventure loaded
      * 
      * @return The descriptor data of the game
      */
     public static DescriptorData loadDescriptorData( InputStreamCreator isCreator, List<Incidence> incidences  ) {
 
         DescriptorData descriptorData = null;
 
         if( Loader.adventureData != null ) {
             descriptorData = Loader.adventureData;
         }
         else {
 
             try {
                 // Set the adventure handler
                 DescriptorHandler descriptorParser = new DescriptorHandler( isCreator );
 
                 factory.setValidating( false );
                 SAXParser saxParser = factory.newSAXParser( );
 
                 // Read and close the inputstrea
                 InputStream descriptorIS = isCreator.buildInputStream( "descriptor.xml" );
                 saxParser.parse( descriptorIS, descriptorParser );
                 descriptorIS.close( );
 
                 // Store the adventure data
                 descriptorData = descriptorParser.getGameDescriptor( );
 
             }
             catch( ParserConfigurationException e ) {
                 incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.SAX" ), e ) );
             }
             catch( SAXException e ) {
                 incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.SAX" ), e ) );
             }
             catch( IOException e ) {
                 incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.IO" ), e ) );
             }
             catch( IllegalArgumentException e ) {
                 incidences.add( Incidence.createDescriptorIncidence( TC.get( "Error.LoadDescriptor.NoDescriptor" ), e ) );
             }
         }
         return descriptorData;
 
     }
 
     /**
      * Loads the script data from the given XML file
      * 
      * @param filename
      *            Name of the XML file containing the script
      * @param validate
      *            distinguish between if the load is made in editor or engine
      * @return The script stored as game data
      */
     public static Chapter loadChapterData( InputStreamCreator isCreator, String fileName, List<Incidence> incidences ) {
 
         // Create the chapter
         Chapter currentChapter = new Chapter( );
         boolean chapterFound = false;
         if( Loader.adventureData != null ) {
             for( Chapter chapter : adventureData.getChapters( ) ) {
                 if( chapter != null && chapter.getChapterPath( ) != null && chapter.getChapterPath( ).equals( fileName ) ) {
                     currentChapter = chapter;
                     chapterFound = true;
                     break;
 
                 }
                 else if( chapter != null && chapter.getChapterPath( ) == null ) {
 
                     currentChapter = chapter;
                     chapterFound = true;
                     currentChapter.setChapterPath( "chapter1.xml" );
                     break;
 
                 }
             }
 
         }
         if( !chapterFound ) {
 
             InputStream chapterIS = null;
 
             //if (zipFile!=null){
             chapterIS = isCreator.buildInputStream( fileName );
             currentChapter.setChapterPath( fileName );
 
             //} else{
             // Then fileName is an absolutePath
             //String chapterPath = fileName.substring( Math.max (fileName.lastIndexOf( '\\' ), fileName.lastIndexOf( '/' ) ), fileName.length( ));
             //currentChapter.setName( chapterPath );
             //try {
             //	chapterIS = new FileInputStream( fileName );
             //} catch (FileNotFoundException e) {
             //e.printStackTrace();
             //	incidences.add( Incidence.createChapterIncidence( TextConstants.getText( "Error.LoadData.IO" ), fileName ) );
             //}
             //}
 
             // Open the file and load the data
             try {
                 if( chapterIS != null ) {
                     // Set the chapter handler
                     ChapterHandler chapterParser = new ChapterHandler( isCreator, currentChapter );
 
                     factory.setValidating( false );
 
                     SAXParser saxParser = factory.newSAXParser( );
 
                     // Parse the data and close the data
                     saxParser.parse( chapterIS, chapterParser );
                     chapterIS.close( );
                 }
 
             }
             catch( ParserConfigurationException e ) {
                 incidences.add( Incidence.createChapterIncidence( TC.get( "Error.LoadData.SAX" ), fileName, e ) );
             }
             catch( SAXException e ) {
                 incidences.add( Incidence.createChapterIncidence( TC.get( "Error.LoadData.SAX" ), fileName, e ) );
             }
             catch( IOException e ) {
                 incidences.add( Incidence.createChapterIncidence( TC.get( "Error.LoadData.IO" ), fileName, e ) );
             }
         }
         return currentChapter;
     }
 
     /**
      * Loads the assessment profile (set of assessment rules) stored in file
      * with path xmlFile in zipFile
      * 
      * @param zipFile
      * @param xmlFile
      * @param incidences
      * @return
      */
     public static AssessmentProfile loadAssessmentProfile( InputStreamCreator isCreator, String xmlFile, List<Incidence> incidences ) {
 
         AssessmentProfile newProfile = null;
         if( Loader.adventureData != null ) {
             for( Chapter chapter : Loader.adventureData.getChapters( ) ) {
                 if( chapter.getAssessmentProfiles( ).size( ) != 0 ) {
                     for( AssessmentProfile profile : chapter.getAssessmentProfiles( ) ) {
                         if( profile.getName( ).equals( xmlFile ) ) {
                             try {
                                 newProfile = (AssessmentProfile) profile.clone( );
                             }
                             catch( CloneNotSupportedException e ) {
                                 e.printStackTrace( );
                             }
                             break;
                         }
                     }
                 }
             }
         }
         else {
 
             // Open the file and load the data
             try {
                 // Set the chapter handler
                 AssessmentProfile profile = new AssessmentProfile( );
 
                 String name = xmlFile;
                 name = name.substring( name.indexOf( "/" ) + 1 );
                 if( name.indexOf( "." ) != -1 )
                     name = name.substring( 0, name.indexOf( "." ) );
                 profile.setName( name );
                 AssessmentHandler assParser = new AssessmentHandler( isCreator, profile );
 
                 factory.setValidating( true );
                 SAXParser saxParser = factory.newSAXParser( );
 
                 // Parse the data and close the data
                 InputStream assessmentIS = isCreator.buildInputStream( xmlFile );
 
                 saxParser.parse( assessmentIS, assParser );
                 assessmentIS.close( );
 
                 // Finally add the new controller to the list
                 // Create the new profile
 
                 // Fill flags & vars
                 newProfile = profile;
 
             }
             catch( ParserConfigurationException e ) {
                 incidences.add( Incidence.createAssessmentIncidence( false, TC.get( "Error.LoadAssessmentData.SAX" ), xmlFile, e ) );
             }
             catch( SAXException e ) {
                 incidences.add( Incidence.createAssessmentIncidence( false, TC.get( "Error.LoadAssessmentData.SAX" ), xmlFile, e ) );
             }
             catch( IOException e ) {
                 incidences.add( Incidence.createAssessmentIncidence( false, TC.get( "Error.LoadAssessmentData.IO" ), xmlFile, e ) );
             }
         }
         return newProfile;
     }
 
     /**
      * Loads the adaptation profile (set of adaptation rules + initial state)
      * stored in file with path xmlFile in zipFile
      * 
      * @param zipFile
      * @param xmlFile
      * @param incidences
      * @return
      */
     public static AdaptationProfile loadAdaptationProfile( InputStreamCreator isCreator, String xmlFile, List<Incidence> incidences ) {
 
         AdaptationProfile newProfile = null;
         if( Loader.adventureData != null ) {
             for( Chapter chapter : Loader.adventureData.getChapters( ) ) {
                 if( chapter.getAssessmentProfiles( ).size( ) != 0 ) {
                     for( AdaptationProfile profile : chapter.getAdaptationProfiles( ) )
 
                         if( profile.getName( ).equals( xmlFile ) ) {
                             newProfile = profile;
                             break;
                         }
                 }
             }
 
         }
         else {
 
             // Open the file and load the data
             try {
                 // Set the chapter handler
                 List<AdaptationRule> rules = new ArrayList<AdaptationRule>( );
                 AdaptedState initialState = new AdaptedState( );
                 AdaptationHandler adpParser = new AdaptationHandler( isCreator, rules, initialState );
 
                 factory.setValidating( true );
                 SAXParser saxParser = factory.newSAXParser( );
 
                 // Parse the data and close the data
                 InputStream adaptationIS = isCreator.buildInputStream( xmlFile );
 
                 saxParser.parse( adaptationIS, adpParser );
                 adaptationIS.close( );
 
                 // Finally add the new controller to the list
                 // Create the new profile
                 String name = xmlFile;
                 name = name.substring( name.indexOf( "/" ) + 1 );
                 name = name.substring( 0, name.indexOf( "." ) );
                 newProfile = new AdaptationProfile( adpParser.getAdaptationRules( ), adpParser.getInitialState( ), name, adpParser.isScorm12( ), adpParser.isScorm2004( ) );
 
                 newProfile.setFlags( adpParser.getFlags( ) );
                 newProfile.setVars( adpParser.getVars( ) );
 
             }
             catch( ParserConfigurationException e ) {
                 incidences.add( Incidence.createAdaptationIncidence( false, TC.get( "Error.LoadAdaptationData.SAX" ), xmlFile, e ) );
             }
             catch( SAXException e ) {
                 incidences.add( Incidence.createAdaptationIncidence( false, TC.get( "Error.LoadAdaptationData.SAX" ), xmlFile, e ) );
             }
             catch( IOException e ) {
                 incidences.add( Incidence.createAdaptationIncidence( false, TC.get( "Error.LoadAdaptationData.IO" ), xmlFile, e ) );
             }
         }
         return newProfile;
     }
 
     /**
      * @param adventureData
      *            the adventureData to set
      */
     public static void setAdventureData( AdventureData adventureData ) {
 
         Loader.adventureData = adventureData;
     }
 
     /**
      * Loads an animation from a filename
      * 
      * @param filename
      *            The xml descriptor for the animation
      * @return the loaded Animation
      */
     public static Animation loadAnimation( InputStreamCreator isCreator, String filename, ImageLoaderFactory imageloader ) {
 
         AnimationHandler animationHandler = new AnimationHandler( isCreator, imageloader );
 
         // Create a new factory
         factory.setValidating( false );
         SAXParser saxParser;
         try {
             saxParser = factory.newSAXParser( );
 
             // Read and close the input stream
             //File file = new File(filename);
             InputStream descriptorIS = null;
             /*try {
             	System.out.println("FILENAME="+filename);
             	descriptorIS = ResourceHandler.getInstance( ).buildInputStream(filename);
             	System.out.println("descriptorIS==null?"+(descriptorIS==null));
             	
             	//descriptorIS = new InputStream(ResourceHandler.getInstance().getResourceAsURLFromZip(filename));
             } catch (Exception e) {
             	e.printStackTrace();
             }
             if (descriptorIS == null) {
             	descriptorIS = AssetsController.getInputStream(filename);
             }*/
             descriptorIS = isCreator.buildInputStream( filename );
 
             saxParser.parse( descriptorIS, animationHandler );
             descriptorIS.close( );
 
         }
         catch( ParserConfigurationException e ) {
             e.printStackTrace( );
             System.err.println( filename );
         }
         catch( SAXException e ) {
             e.printStackTrace( );
             System.err.println( filename );
         }
         catch( FileNotFoundException e ) {
             e.printStackTrace( );
             System.err.println( filename );
         }
         catch( IOException e ) {
             e.printStackTrace( );
             System.err.println( filename );
         }
 
         if( animationHandler.getAnimation( ) != null )
             return animationHandler.getAnimation( );
         else
             return new Animation( "anim" + ( new Random( ) ).nextInt( 1000 ), imageloader );
     }
 
     public static SAXParserFactory getFactory( ) {
 
         return factory;
     }
 
     /**
      * Returns true if the given file contains an eAdventure game from a newer
      * version. Essentially, it looks for the "ead.properties" file in the new
      * eAdventure games. If it's found, then returns true
      * 
      * @param f
      *            the file to check
      * @return if the game requires a newer version
      */
     public static boolean requiresNewVersion( java.io.File f ) {
         boolean isOldProject = true;
         FileInputStream in = null;
         ZipInputStream zipIn = null;
         try {
             in = new FileInputStream( f );
             zipIn = new ZipInputStream( in );
             ZipEntry zipEntry = null;
             while( ( zipEntry = zipIn.getNextEntry( ) ) != null ) {
                 if( zipEntry.getName( ).endsWith( "ead.properties" ) ) {
                     isOldProject = false;
                 }
             }
             zipIn.close( );
         }
         catch( IOException e ) {
 
         }
         finally {
             if( in != null ) {
                 try {
                     in.close( );
                 }
                 catch( IOException e ) {
 
                 }
             }
 
             if( zipIn != null ) {
                 try {
                     zipIn.close( );
                 }
                 catch( IOException e ) {
                 }
             }
         }
         return !isOldProject;
     }
 }
