 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package es.eucm.eadventure.tracking.pub;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import es.eucm.eadventure.tracking.pub.config.Service;
 import es.eucm.eadventure.tracking.pub.config.TrackingConfig;
 
 
 /**
  * Loads the TrackingConfig xml file and creates tracking controller if possible.
  * @author Javier Torrente
  *
  */
 public class TrackingControllerProxy implements _TrackingController{
 
     private _TrackingController trackingController;
     private TrackingConfig trackingConfig;
     
     public TrackingControllerProxy ( String trackingConfigFile ){
         // Read config. First, try getting input stream for config file
         InputStream is = getTrackingConfigInputStream( trackingConfigFile );
         //If null, just shut down the system.
         if ( is == null ){
             System.err.println( "[GAMELOG] Config file "+trackingConfigFile +" not found, corrupt or unavailable. Tracking system will be disabled.");
             trackingConfig=null;
             trackingController=null;
         } 
         // If not null, try loading the configuration
         else {
             trackingConfig = loadTrackingConfig(is);
         }
         // Instantiate controller if tracking config is not null (that means loading succeeded).
         if (trackingConfig!=null && trackingConfig.isEnabled( ) && trackingConfig.getMainClass( )!=null && !trackingConfig.getMainClass( ).equals( "" )){
             try {
                 trackingController = (_TrackingController) Class.forName( trackingConfig.getMainClass( ) ).getConstructor( TrackingConfig.class ).newInstance( trackingConfig );
             }
             catch( Exception e ) {
                 System.err.println( "[GAMELOG] Controller class name undefined or not fully qualified. GameLog will be disabled");
                 trackingController = null;
             }
         } else {
             trackingController = null;
         }
     }
     
     public void start(){
         if (trackingController!=null)
             trackingController.start( );
     }
     
     public void terminate(){
         if (trackingController!=null)
             trackingController.terminate( );        
     }
     
     public _GameLog getGameLog( ) {
         if (trackingController!=null)
             return trackingController.getGameLog( );
         else
             return EmptyGameLog.get( ); 
     }
     
     /**
      * Tries to get an InputStream to read the config file from. Several locations and relative paths are tried. Also FileInputStream is tried
      * @param trackingConfigFile
      * @return
      */
     private InputStream getTrackingConfigInputStream( String trackingConfigFile ){
        if ( trackingConfigFile == null )
            return null;
        
         InputStream source = null;
         source = TrackingControllerProxy.class.getResourceAsStream( trackingConfigFile );
         if (source==null)
             source = TrackingControllerProxy.class.getResourceAsStream( "/"+trackingConfigFile );
         if (source==null)
             source = TrackingControllerProxy.class.getResourceAsStream( "./"+trackingConfigFile );
         if (source==null)
             source = TrackingControllerProxy.class.getResourceAsStream( "/tracking/"+trackingConfigFile );
         if (source==null){
             try {
                 source = new FileInputStream( trackingConfigFile );
             }
             catch( FileNotFoundException e ) {
                 source=null;
             }
         }        
         return source;
     }
     
     /**
      * Unmarshalls tracking config file from a not null inputstream.
      * @param is
      * @return
      */
     private TrackingConfig loadTrackingConfig ( InputStream is ){
         TrackingConfig config = null;
         try{
             JAXBContext jc = JAXBContext.newInstance ("es.eucm.eadventure.tracking.pub.config");
             //System.out.println( jc.toString() );
             Unmarshaller u = jc.createUnmarshaller ();
             config = (TrackingConfig) u.unmarshal (is);
         } catch (JAXBException e) {
             e.printStackTrace ();
         }
         return config;
 
     }
     
     public void printTrackingConfig(){
         TrackingConfig config=trackingConfig;
         System.out.println (config.isEnabled( ));
         System.out.println (config.getMainClass( ));
         
         System.out.println ("**** PROPERTIES ****");
         for (int i=0; i<config.getProperty( ).size( ); i++){
             System.out.println( "\t"+ config.getProperty( ).get( i ).getName( )+"="+config.getProperty( ).get( i ).getValue( ));
         }
         System.out.println ("**** SERVICES ****");
         for (int i=0; i<config.getService( ).size( ); i++){
             Service service = config.getService( ).get( i );
             System.out.println("\t"+service.getName( ) );
             System.out.println( "\t\tclass="+ service.getClazz( ));
             System.out.println( "\t\tenabled="+ service.isEnabled( ));
             System.out.println( "\t\tpath="+ service.getPath( ));
             System.out.println( "\t\turl="+ service.getUrl( ));
             System.out.println( "\t\tfrequency="+ service.getFrequency( ));
             for (int j=0; j<service.getProperty( ).size( ); j++){
                 System.out.println( "\t\t"+ service.getProperty( ).get( j ).getName( )+"="+service.getProperty( ).get( j ).getValue( ));
             }
 
         }        
     }
 }
 
     
