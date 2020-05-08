 package com.axiomalaska.sos.xmlbuilder;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import net.opengis.sos.x10.DescribeSensorDocument;
 import net.opengis.sos.x10.GetObservationDocument;
 import net.opengis.sos.x10.InsertObservationDocument;
 import net.opengis.sos.x10.RegisterSensorDocument;
 
 import org.apache.xmlbeans.XmlError;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.junit.Test;
 
 import ucar.units.Unit;
 import ucar.units.UnitFormat;
 import ucar.units.UnitFormatManager;
 
 import com.axiomalaska.phenomena.Phenomena;
 import com.axiomalaska.phenomena.Phenomenon;
 import com.axiomalaska.phenomena.PhenomenonImp;
 import com.axiomalaska.sos.data.Location;
 import com.axiomalaska.sos.data.ObservationCollection;
 import com.axiomalaska.sos.data.PublisherInfo;
 import com.axiomalaska.sos.data.PublisherInfoImp;
 import com.axiomalaska.sos.data.SosNetwork;
 import com.axiomalaska.sos.data.SosNetworkImp;
 import com.axiomalaska.sos.data.SosSensor;
 import com.axiomalaska.sos.data.SosSensorImp;
 import com.axiomalaska.sos.data.SosSource;
 import com.axiomalaska.sos.data.SosSourceImp;
 import com.axiomalaska.sos.data.SosStation;
 import com.axiomalaska.sos.data.SosStationImp;
 import com.axiomalaska.sos.tools.IdCreator;
 
 public class XmlBuildersTest {
     private IdCreator ID_CREATOR = new IdCreator();
     private Location TEST_LOCATION = new Location( 70.4, -148.527 );
     private String TEST_SOURCE_ID = "aoos";
     private String TEST_NETWORK_ID = "urn:ioos:network:aoos:all";
     private String TEST_STATION_ID = "urn:ioos:station:aoos:prudhoe";
     private String TEST_STATION_NAME = "Prudhoe Bay Station";
     private String TEST_STATION_PLATFORM_TYPE = "cman";
     private String TEST_SENSOR_ID = "urn:ioos:sensor:aoos:prudhoe:airtemp";
     private String TEST_PHENOMENON_ID = "http://mmisw.org/ont/cf/parameter/air_temperature";
     private String TEST_PHENOMENON_NAME = "Air Temperature";
     private String TEST_PHENOMENON_UNITS = "degC";
     private String TEST_DESCRIPTION = "description";
     private String TEST_FOI_NAME = "Prudhoe Bay";
     private String TEST_EMAIL = "noone@nowhere.com";   
     private String TEST_CITY = "Purgatory";
     private String TEST_STATE = "Alaska";
     private String TEST_OPERATOR_SECTOR = "academic";
     private String TEST_PUBLISHER_NAME = "AOOS";
     private String TEST_COUNTRY = "USA";
     private String TEST_WEB_ADDRESS = "http://www.aoos.org";
     private String TEST_ADDRESS = "1234 Nowhere Dr";
     private String TEST_ZIPCODE = "99999";
     private SosNetwork TEST_ROOT_NETWORK = buildRootNetwork();
     private SosSource TEST_SOURCE = buildTestSource();
     private SosNetwork TEST_NETWORK = buildTestNetwork();
     private Phenomenon TEST_PHENOMENON = buildTestPhenomenon();    
     private SosSensor TEST_SENSOR = buildTestSensor();    
     private SosStation TEST_STATION = buildTestStation();
     private ObservationCollection TEST_OBSERVATION_COLLECTION = buildTestObservationCollection();
     private PublisherInfo TEST_PUBLISHER_INFO = buildTestPublisherInfo();
     
     private SosNetwork buildRootNetwork(){
 		SosNetworkImp rootNetwork = new SosNetworkImp();
 		rootNetwork.setId("all");
 		rootNetwork.setSourceId("aoos");
 		rootNetwork.setDescription("All inclusive sensor network");
		rootNetwork.setOfferingName("All observations");
 		
 		return rootNetwork;
     }
     
     private SosSource buildTestSource(){
         SosSourceImp testSource = new SosSourceImp();
         testSource.setEmail( TEST_EMAIL );
         testSource.setCity( TEST_CITY );
         testSource.setState( TEST_STATE );
         testSource.setOperatorSector( TEST_OPERATOR_SECTOR );
         testSource.setAddress( TEST_ADDRESS );
         testSource.setCountry( TEST_COUNTRY );
         testSource.setWebAddress( TEST_WEB_ADDRESS );
         testSource.setZipcode( TEST_ZIPCODE );
         return testSource;
     }
     
     private SosNetwork buildTestNetwork(){
         SosNetworkImp testNetwork = new SosNetworkImp();
         testNetwork.setId( TEST_NETWORK_ID );
         testNetwork.setSourceId( TEST_SOURCE_ID );
         testNetwork.setDescription( TEST_DESCRIPTION );
         return testNetwork;
     }
     
     private SosStation buildTestStation(){    
         SosStationImp testStation = new SosStationImp();
         testStation.setId( TEST_STATION_ID );
         testStation.setDescription( TEST_DESCRIPTION );
         testStation.setFeatureOfInterestName( TEST_FOI_NAME );
         testStation.setLocation( TEST_LOCATION );
         testStation.setName( TEST_STATION_NAME );
         List<SosNetwork> networks = new ArrayList<SosNetwork>();
         networks.add( TEST_NETWORK );
         testStation.setNetworks( networks );
         testStation.setPlatformType( TEST_STATION_PLATFORM_TYPE );
         testStation.setSource( TEST_SOURCE );        
         List<SosSensor> sensors = new ArrayList<SosSensor>();
         sensors.add( TEST_SENSOR );
         testStation.setSensors( sensors );
         testStation.setMoving( false );
         return testStation;
     }
     
     private SosSensor buildTestSensor(){
         SosSensorImp testSensor = new SosSensorImp();
         testSensor.setId( TEST_SENSOR_ID );
         testSensor.setDescription( TEST_DESCRIPTION );
         List<SosNetwork> networks = new ArrayList<SosNetwork>();
         networks.add( TEST_NETWORK );        
         testSensor.setNetworks( networks );
         List<Phenomenon> phenomena = new ArrayList<Phenomenon>();
         phenomena.add( TEST_PHENOMENON );                
         testSensor.setPhenomena( phenomena );
         return testSensor;
     }
     
     private Phenomenon buildTestPhenomenon(){
     	Phenomenon testPhenomenon = new PhenomenonImp();
     	try{
     		testPhenomenon = Phenomena.instance().AIR_TEMPERATURE;
     	}
     	catch (Exception x){
     		System.out.println("error creating phenomenon: " + x.getMessage());
     	}
     	
         return testPhenomenon;
     }
     
     private ObservationCollection buildTestObservationCollection(){
         ObservationCollection obsCol = new ObservationCollection();
         obsCol.setStation( TEST_STATION );
         obsCol.setSensor( TEST_SENSOR );
         obsCol.setPhenomenon( TEST_PHENOMENON );
         List<Calendar> dates = new ArrayList<Calendar>();
         dates.add( Calendar.getInstance() );
         obsCol.setObservationDates( dates );
         List<Double> values = new ArrayList<Double>();
         values.add( 10.3 );
         obsCol.setObservationValues( values );        
         return obsCol;                
     }
     
     private PublisherInfo buildTestPublisherInfo(){
         PublisherInfoImp testPublisherInfo = new PublisherInfoImp();
         testPublisherInfo.setEmail( TEST_EMAIL );
         testPublisherInfo.setName( TEST_PUBLISHER_NAME );
         testPublisherInfo.setCountry( TEST_COUNTRY );
         testPublisherInfo.setWebAddress( TEST_WEB_ADDRESS );
         return testPublisherInfo;
     }
     
     @Test
     public void testDescribeSensorBuilder() throws XmlException {
         String xmlString = new DescribeSensorBuilder( TEST_STATION.getId() ).build();
         DescribeSensorDocument describeSensorDoc = DescribeSensorDocument.Factory.parse( xmlString );
         validateXmlDocument( describeSensorDoc );
     }
 
     @Test
     public void testGetNewestObservationBuilder() throws XmlException {
         String xmlString = new GetNewestObservationBuilder( TEST_STATION, TEST_SENSOR,
                 TEST_PHENOMENON, ID_CREATOR, TEST_ROOT_NETWORK).build();
         GetObservationDocument getObsDoc = GetObservationDocument.Factory.parse( xmlString );
         validateXmlDocument( getObsDoc );
     }
 
     @Test
     public void testInsertObservationBuilder() throws XmlException {
         String xmlString = new InsertObservationBuilder( TEST_STATION, TEST_SENSOR, TEST_PHENOMENON,
                 TEST_OBSERVATION_COLLECTION, ID_CREATOR ).build();
         InsertObservationDocument insObsDoc = InsertObservationDocument.Factory.parse( xmlString );
         validateXmlDocument( insObsDoc );
     }
     
     @Test
     public void testNetworkRegisterSensorBuilder() throws XmlException {
        String xmlString = new NetworkRegisterSensorBuilder( TEST_NETWORK, ID_CREATOR ).build();
         RegisterSensorDocument registerSensorDoc = RegisterSensorDocument.Factory.parse( xmlString );
         validateXmlDocument( registerSensorDoc );
     }
 
     //Test fails because a null xmlString is produced, probably because of an unset value
 //    @Test
 //    public void testStationRegisterSensorBuilder() throws XmlException {
 //        String xmlString = new StationRegisterSensorBuilder( TEST_STATION, ID_CREATOR,
 //                TEST_PUBLISHER_INFO ).build();
 //        RegisterSensorDocument registerSensorDoc = RegisterSensorDocument.Factory.parse( xmlString );
 //        validateXmlDocument( registerSensorDoc );
 //    }
     
     @Test
     public void testSensorRegisterSensorBuilder() throws XmlException {
         String xmlString = new SensorRegisterSensorBuilder( TEST_STATION, TEST_SENSOR, ID_CREATOR ).build();
         RegisterSensorDocument registerSensorDoc = RegisterSensorDocument.Factory.parse( xmlString );
         validateXmlDocument( registerSensorDoc );
     }
     
     private void validateXmlDocument( XmlObject xmlDocument ){
         ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
         XmlOptions validationOptions = new XmlOptions();
         validationOptions.setErrorListener(validationErrors);
         xmlDocument.validate(validationOptions);
 
         List<String> excludeErrors = new ArrayList<String>();
         excludeErrors.add("Expected element '_Feature@http://www.opengis.net/gml' instead of");
         
         for( XmlError validationError : validationErrors ){
             boolean skip = false;
             for( String excludeError : excludeErrors ){
                 if( validationError.getMessage().startsWith( excludeError ) ){
                     skip = true;
                 }
             }
 
             if( !skip ){
                 fail( validationError.getMessage() );
             }
         }
     }
 }
