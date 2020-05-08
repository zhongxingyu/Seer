 package org.iugonet.www;
 
 import static org.junit.Assert.*;
 
 import java.net.URI;
 import java.net.URL;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class AplotTest {
 
 	class Sample extends Aplot {
 		Sample(){
 			super();
 		}
 
 		@Override
 		void readData(URL url) {
 			// TODO Auto-generated method stub
			System.out.println(url);
 		}
 	}
 	
 	Sample sample;
 	
 	@Before
 	public void setUp() throws Exception {
 		sample = new Sample();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		sample = null;
 		System.gc();
 	}
 
 	@Test
 	public void test_getRootDataDir01(){
 		String expected = "~/data";
 		String actual = sample.getRootDataDir();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void test_setRootDataDir01(){
 		String expected = "~/hoge";
 		sample.setRootDataDir(expected);
 		String actual = sample.getRootDataDir();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void test_getThemisDataDir01(){
 		String expected = "~/themis";
 		String actual = sample.getThemisDataDir();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void test_setThemisDataDir01(){
 		String expected = "~/hoge";
 		sample.setThemisDataDir(expected);
 		String actual = sample.getThemisDataDir();
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void test_getThemisRemoteDataDir01(){
 		String actual = sample.getThemisRemoteDataDir();
 		String expected = "http://themis.stp.isas.jaxa.jp/data/themis/";
 		assertEquals(expected, actual);
 	}
 	
 	@Test
 	public void test_setThemisRemoteDataDir01(){
 		String expected ="http://www.kyoto-u.ac.jp/";
 		sample.setThemisRemoteDataDir(expected);
 		String actual = sample.getThemisRemoteDataDir();
 		assertEquals(expected, actual);
 	}
 
 	@Test
 	public void test_readData01(){
 		try {
 			URI uri = new URI("spase://IUGONET/Granule/WDC_Kyoto/WDC/Dst/index/PT1H/dst198410_wdc");
 			sample.readData(uri);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 }
