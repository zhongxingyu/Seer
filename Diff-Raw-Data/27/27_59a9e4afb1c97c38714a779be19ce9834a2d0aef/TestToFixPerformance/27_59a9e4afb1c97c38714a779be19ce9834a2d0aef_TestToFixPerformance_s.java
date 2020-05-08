 /**
  * Copyright (c) 2011 Sebastian Tomac (tomac.org)
  * Licensed under LGPL licenses.
  * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
  **/
 package org.tomac.tools;
 
 import static org.junit.Assert.assertFalse;
 
 import java.nio.ByteBuffer;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import org.tomac.protocol.fix.FixValidationError;
 import org.tomac.protocol.fix.messaging.FixMarketDataSnapshotFullRefresh;
 import org.tomac.protocol.fix.messaging.FixMessageListenerImpl;
 import org.tomac.protocol.fix.messaging.FixMessageParser;
 import org.tomac.protocol.fix.messaging.FixNewOrderSingle;
 
 
 /**
  * @author seto
  *
  */
 public class TestToFixPerformance {
 	final int ITERATIONS = 100000;
 	final int DO_SAMPLE_DATA = 100;
 	// discard data > 7000 due to grabage collection distortions and jvm warmup
	final long DISCARD_LEVEL = 7000L; 
 	FixValidationError err;
 	FixMessageParser parser;
 	FixMessageListenerImpl listener;
 	FixMarketDataSnapshotFullRefresh message;
 	
 	@Before
 	public void setUp() {
 		err = new FixValidationError();
 		parser = new FixMessageParser();
 		listener = new FixMessageListenerImpl() {
     		@Override
     		public void onFixNewOrderSingle(FixNewOrderSingle msg) {
     		}
     		@Override
     		public void onFixMarketDataSnapshotFullRefresh(FixMarketDataSnapshotFullRefresh msg) {
     			message = msg.clone();
     		}
     	};
 	}
 	
 	@Test
 	public void testInBoundLatency() throws Exception {
         final String data = "8=FIXT.1.19=24935=D49=SenderCompId56=TargetCompId34=3752=20070223-22:28:33"
             + "11=18333922=838=140=244=1248=BHP54=255=BHP59=1"
             + "60=20060223-22:38:33526=362078=279=AllocACC180=1010.1"
             + "79=AllocACC280=2020.2453=2448=8447=D452=4448=AAA35354447=D452=310=168";
         ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
 
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
 
         System.out.println("toFIX testInBoundLatency");
         while (count < ITERATIONS) {
         	long t0 = System.nanoTime();
         	parser.parse(buf, err, listener);
         	long t1 = System.nanoTime();
         	buf.flip();
         	assertFalse(err.hasError());
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
         final String data = "8=FIXT.1.19=18235=W34=249=ABFX52=20080722-16:37:11.23456=X2RV1"
                 + "55=EUR/USD262=CAP0000011268=2269=0270=1.5784415=EUR271=500000272=20080724"
                 + "269=1270=1.5786915=EUR271=500000272=2008072410=097";
         ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
         
     	parser.parse(buf, err, listener);
     	assertFalse(err.hasError());
         ByteBuffer out = ByteBuffer.allocate(1024);
 
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
         
         System.out.println("toFIX testOutBoundLatency");
         while (count < ITERATIONS) {
         	long t0 = System.nanoTime();
         	message.encode(out);
         	long t1 = System.nanoTime();
         	out.clear();
         	cumTime += t1 -t0;
         	cumTimeIntervall += t1 -t0;
         	message.clear();
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
 	
 	
 }
