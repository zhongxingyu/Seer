 package de.zib.gndms.model.gorfx.types.io.tests;
 
 import de.zib.gndms.model.gorfx.types.*;
 import de.zib.gndms.model.gorfx.types.io.*;
 
 import java.util.HashMap;
 import java.util.Properties;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import org.joda.time.DateTime;
 
 /**
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * <p/>
  * User: mjorra, Date: 18.09.2008, Time: 16:19:48
  */
 public class ProviderStageInORQIOTest {
 
     public static void main( String[] args ) {
 
         DataDescriptor ddt = new DataDescriptor();
         DataConstraints dc = new DataConstraints();
         String[] ol = { "hello", "world" };
         String[] cfl = { "foo", "bar", "foobar", "blubber", };
         HashMap<String,String> cl = new HashMap<String,String>( );
         cl.put( "gibson", "les-paul");
         cl.put( "fender", "Stratocaster" );
         cl.put( "duesenberg", "double-cat" );
 
         SpaceConstraint sc = new SpaceConstraint();
         sc.setLatitude( new MinMaxPair( -1.3, 5.7 ) );
         sc.setLongitude( new MinMaxPair( 2.7, 42.0 ) );
         sc.setAltitude(  new MinMaxPair( 1500., 3000.  ) );
         sc.setVerticalCRS( "some vertical crs" );
         sc.setAreaCRS( "the area crs" );
 
         DateTime tm = new DateTime( );
         TimeConstraint tc = new TimeConstraint();
         tc.setMinTime( tm );
         tc.setMaxTime( tm.plusDays( 6 ) );
 
         String dff = "plain";
         String dfaf = "zip";
         String mff = "ini";
         String mfaf = "tar";
 
         dc.setCFList( cfl );
         dc.setSpaceConstraint( sc );
         dc.setTimeConstraint( tc );
         dc.setConstraintList( cl );
         
         ddt.setObjectList( ol );
         ddt.setConstrains( dc );
         ddt.setDataFormat( dff );
         ddt.setDataArchiveFormat( dfaf );
         ddt.setMetaDataFormat( mff );
         ddt.setMetaDataArchiveFormat( mfaf );
 
         ProviderStageInORQ orq = new ProviderStageInORQ();
         orq.setDataDescriptor( ddt );
         orq.setDataFile( "data_file" );
         orq.setMetadataFile( "meta_data_file" );
 
         Properties prop = new Properties( );
         ProviderStageInORQPropertyWriter wrt = new ProviderStageInORQPropertyWriter( prop );
         ProviderStageInORQConverter conv = new ProviderStageInORQConverter( wrt, orq );
         conv.convert();
         try {
             prop.store( System.out, "hallo welt" );
         } catch ( IOException e ) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         ProviderStageInORQPropertyReader reader = new ProviderStageInORQPropertyReader( prop );
         reader.begin( );
         reader.read();
         ProviderStageInORQ rorq = reader.getProduct();
 
 
         // example of converter reusage
         ProviderStageInORQStdoutWriter stdwrt = new ProviderStageInORQStdoutWriter( );
         conv.setWriter( stdwrt );
         conv.setModel( rorq );
         conv.convert( );
 
         System.out.println( "Reading test prop from file" );
         String fn = "prop_test_file.txt";
         try {
             InputStream is = new FileInputStream( fn );
             Properties np = new Properties( );
             np.load( is );
             np.store( System.out, "test out" );
             reader.setProperties( np );
             reader.begin( );
             reader.read(); 
             showORQ( reader.getProduct() );
         } catch ( FileNotFoundException e ) {
             System.out.println( "File not found make!" );
             System.out.println( "Please make sure your working dir contains " + fn );
         } catch ( IOException e ) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
 
         // example for just download
         DataDescriptor ddt2 = new DataDescriptor();
         String[] objs = { "no", "download" };
 
         dff = "plain";
         dfaf = "zip";
         mff = "ini";
         mfaf = "tar";
 
         ddt2.setObjectList( objs );
         ddt2.setDataFormat( dff );
         ddt2.setDataArchiveFormat( dfaf );
         ddt2.setMetaDataFormat( mff );
         ddt2.setMetaDataArchiveFormat( mfaf );
 
         Properties prop2 = new Properties( );
         DataDescriptorPropertyWriter ddw = new DataDescriptorPropertyWriter( prop2 );
         DataDescriptorConverter dcon = new DataDescriptorConverter( ddw, ddt2 );
         dcon.convert( );
 
         try {
            prop2.store( System.out, "just downloading?" );
         } catch ( IOException e ) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         
        if( prop2.containsKey( SfrProperty.JUST_DOWNLOAD.key ) )
            System.out.println( ">>>>>>>>>>> JUST_DOWNLOAD contained");
         else
             throw new IllegalStateException( "JUST_DOWNLOAD missing" );
 
         DataDescriptorPropertyReader ddpr = new DataDescriptorPropertyReader( prop2 );
         ddpr.performReading();
        DataDescriptor ddt3 = ddpr.getProduct();

        Properties prop3 = new Properties( );
        ddw = new DataDescriptorPropertyWriter( prop3 );
        dcon.setWriter( ddw );
        dcon.setModel( ddt3 );
        dcon.convert( );

        if( prop2.equals( prop3 ) )
            System.out.println( "Prop write read write: OKAY" );
        else
            System.out.println( "Prop write read write: ERROR (different results)"  );
 
     }
 
     public static void showORQ( ProviderStageInORQ orq ) {
 
         ProviderStageInORQStdoutWriter stdwrt = new ProviderStageInORQStdoutWriter( );
         ProviderStageInORQConverter conv = new ProviderStageInORQConverter( stdwrt, orq );
         conv.convert( );
     }
 
 }
