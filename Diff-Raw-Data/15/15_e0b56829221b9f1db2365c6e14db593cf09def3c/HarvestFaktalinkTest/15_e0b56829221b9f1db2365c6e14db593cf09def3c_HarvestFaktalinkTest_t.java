 package dk.dbc.opensearch.plugins.tests;
 
 import dk.dbc.opensearch.common.pluginframework.PluginID;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.plugins.HarvestFaktalink;
 
import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.IOException;
 
 //import static org.easymock.classextension.EasyMock.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 
 
 
 public class HarvestFaktalinkTest
 {
 	
 	//private HarvestFaktalink mockHarvestFaktaLink;
 	
 	
 	@Before 
 	public void SetUp()
 	{
 		//mockHarvestFaktaLink = createMock( HarvestFaktalink.class );
 	}
 	
 	
 	@After
 	public void TearDown()
 	{
 		//reset( mockHarvestFaktaLink );
 	}
 	
 	
 	@Test
 	public void testPluginID() throws FileNotFoundException
 	{		
 		String submitter = "DBC";
 		String format = "text/xml";
 		String task = "";
 			
 		PluginID pid = new PluginID( submitter, format, task );		

                String teststring = "æøå";
                InputStream data = new ByteArrayInputStream( teststring.getBytes( ) );
 		HarvestFaktalink hfl = new HarvestFaktalink();
 		hfl.init( pid, data );
 		
 		PluginID pidRet = hfl.getPluginID();
 		
 		boolean submitterRet = submitter.equals( pidRet.getPluginSubmitter() );		
 		assertTrue( submitterRet );
 		
 		boolean formatRet = format.equals( pidRet.getPluginFormat() );
 		assertTrue( formatRet );
 		
 		boolean taskRet = task.equals( pidRet.getPluginTask() );
 		assertTrue( taskRet );
 	}
 	
 	
     @Test
     public void testCargoContainer() throws IllegalArgumentException, NullPointerException, IOException
     {

        String teststring = "æøå";
        InputStream data = new ByteArrayInputStream( teststring.getBytes( "UTF-8" ) );
     	
     	String submitter = "DBC";
     	String format = "text/xml";
     	String task = "";
     	String lang = "DA";
     	
     	//mockHarvestFaktaLink = new HarvestFaktalink( "id", path, submitter, "format" );
     	PluginID pid = new PluginID( submitter, format, task );
     	HarvestFaktalink hfl = new HarvestFaktalink();
     	hfl.init(pid, data);
     	
     	// CargoContainer cc = mockHarvestFaktaLink.getCargoContainer();
     	CargoContainer cc = hfl.getCargoContainer();
     	
     	boolean submitterChecked = cc.checkSubmitter( submitter );    	
     	assertTrue( submitterChecked );
     	
     	boolean langChecked = cc.checkLanguage( lang );
     	assertTrue( langChecked );
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
