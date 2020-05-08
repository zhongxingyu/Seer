 package org.tomac.tools;
 
/**
 * To run quickfix performance tests, 
 * 1. download the quickfixj jars from quickfixj.org and place them in lib
 * 2. In eclipse add the project dependencies, and if you use maven uncomment the quickfix section in the pom.xml
 * 3. uncomment the sections below, and update the pom.xml including qucik fix libs. 
 *
 */

/*
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import quickfix.ConfigError;
 import quickfix.DataDictionary;
 import quickfix.Message;
 import quickfix.fix50.MarketDataSnapshotFullRefresh;
*/
 
 public class TestQuickFixPerformance  {
	/*
 	DataDictionary sessDictionary;
 	DataDictionary appDictionary;
 	final int ITERATIONS = 100000;
 	final int DO_SAMPLE_DATA = 100;
 	// discard data > 7000 due to grabage collection distortions and jvm warmup
	final long DISCARD_LEVEL = 7000L;
 	
 	@Before
 	public void setUp() {
 		try {
 	        sessDictionary = new DataDictionary(TestQuickFixPerformance.class.getClassLoader()
 	                .getResourceAsStream("FIXT11.xml"));
 	        appDictionary = new DataDictionary(TestQuickFixPerformance.class.getClassLoader()
 	                .getResourceAsStream("FIX50.xml"));
 	        assertNotNull(sessDictionary);
 	        assertNotNull(appDictionary);
 		} catch (ConfigError e) {
 			fail();
 		}
 	}
 	
 	@Test
 	public void testInBoundLatency() throws Exception {
         final String data = "8=FIXT.1.19=24935=D49=SenderCompId56=TargetCompId34=3752=20070223-22:28:33"
             + "11=18333922=838=140=244=1248=BHP54=255=BHP59=1"
             + "60=20060223-22:38:33526=362078=279=AllocACC180=1010.1"
             + "79=AllocACC280=2020.2453=2448=8447=D452=4448=AAA35354447=D452=310=168";
 
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
 
         System.out.println("QuickFix testInBoundLatency");
         while (count < ITERATIONS) {
             final Message message = new Message();
 
         	long t0 = System.nanoTime();
         	message.fromString(data, sessDictionary, appDictionary, true);
         	long t1 = System.nanoTime();
         	cumTime += t1 - t0;
         	cumTimeIntervall += t1 - t0;
         	++count;
         	if (count % DO_SAMPLE_DATA == 0) {
         		if (cumTimeIntervall / DO_SAMPLE_DATA < DISCARD_LEVEL) {
         			sampleCount++;
         			sampleTime += cumTimeIntervall / DO_SAMPLE_DATA;
         		}
         		//System.out.println(cumTime / count + " " + cumTimeIntervall / DO_SAMPLE_DATA);
         		cumTimeIntervall = 0L;
         	}
         }
 		System.out.println("ns/msg\t#count\ttotns/msg\t#totCount");
 		System.out.println(sampleTime / sampleCount + "\t" + 
 				sampleCount * DO_SAMPLE_DATA + "\t" + cumTime / ITERATIONS + "\t" + ITERATIONS);
 	}
 
 	@Ignore
 	@Test
 	public void testInBoundLatencyNoValidation() throws Exception {
         final String data = "8=FIXT.1.19=22335=D49=SenderCompId56=TargetCompId34=3752=20070223-22:28:33"
             + "11=18333922=838=140=244=1248=BHP54=255=BHP59=1"
             + "60=20060223-22:38:33526=362078=379=AllocACC180=1010.1"
             + "79=AllocACC280=2020.2453=2448=8447=D452=4448=AAA35354447=D452=310=161";
 
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
 
         System.out.println("QuickFix testInBoundLatencyNoValidation");
         while (count < ITERATIONS) {
             final Message message = new Message();
 
         	long t0 = System.nanoTime();
         	message.fromString(data, sessDictionary, appDictionary, false);
         	long t1 = System.nanoTime();
         	cumTime += t1 - t0;
         	cumTimeIntervall += t1 - t0;
         	++count;
         	if (count % DO_SAMPLE_DATA == 0) {
         		if (cumTimeIntervall / DO_SAMPLE_DATA < DISCARD_LEVEL) {
         			sampleCount++;
         			sampleTime += cumTimeIntervall / DO_SAMPLE_DATA;
         		}
         		//System.out.println(cumTime / count + " " + cumTimeIntervall / DO_SAMPLE_DATA);
         		cumTimeIntervall = 0L;
         	}
         }
 		System.out.println("ns/msg\t#count\ttotns/msg\t#totCount");
 		System.out.println(sampleTime / sampleCount + "\t" + 
 				sampleCount * DO_SAMPLE_DATA + "\t" + cumTime / ITERATIONS + "\t" + ITERATIONS);
 	}
 	
 	
     @Test
     public void testOutBoundLatency() throws Exception {
         final String data = "8=FIXT.1.19=23435=W34=249=ABFX52=20080722-16:37:11.23456=X2RV1"
                 + "55=EUR/USD262=CAP0000011268=2269=0270=1.5784415=EUR271=500000272=20080724"
                 + "269=1270=1.5786915=EUR271=500000272=2008072410=097";
         final MarketDataSnapshotFullRefresh message = new MarketDataSnapshotFullRefresh();
         message.fromString(data, sessDictionary, appDictionary, true);
         
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
         
         System.out.println("QuickFix testOutBoundLatency");
         while (count < ITERATIONS) {
         	long t0 = System.nanoTime();
         	message.toString();
         	long t1 = System.nanoTime();
         	cumTime += t1 -t0;
         	cumTimeIntervall += t1 -t0;
         	message.clear();
         	++count;
         	if (count % DO_SAMPLE_DATA == 0) {
         		if (cumTimeIntervall / DO_SAMPLE_DATA < 7000) {
         			sampleCount++;
         			sampleTime += cumTimeIntervall / DO_SAMPLE_DATA;
         		}
         		//System.out.println(cumTime / count + " " + cumTimeIntervall / DO_SAMPLE_DATA);
         		cumTimeIntervall = 0L;
         	}
         }
 		System.out.println("ns/msg\t#count\ttotns/msg\t#totCount");
 		System.out.println(sampleTime / sampleCount + "\t" + 
 				sampleCount * DO_SAMPLE_DATA + "\t" + cumTime / ITERATIONS + "\t" + ITERATIONS);
     }
	*/
 }
