 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.scan;
 
 import static org.mockito.Matchers.anyDouble;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import gda.TestHelpers;
 import gda.configuration.properties.LocalProperties;
 import gda.device.Scannable;
 
 import org.junit.After;
 import org.junit.Test;
 import org.mockito.InOrder;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 /**
  * These tests rerun all the tests in ConcurrentScanTest, but with concurrentScan configured to readout
  * detectors is parallel 
  */
 public class ConcurrentScanWithConcurrentReadoutTest extends ConcurrentScanTest {
 
 	private DelayedAnswer detlev5Readout1;
 	private DelayedAnswer detlev9aReadout1;
 	private DelayedAnswer detlev9bReadout1;
 	private DelayedAnswer detlev5Readout2;
 	private DelayedAnswer detlev9aReadout2;
 	private DelayedAnswer detlev9bReadout2;
 
 	@Override
 	protected void setLocalProperties() {
 		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
 		LocalProperties.set(LocalProperties.GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY, "true"); // default as interpreted by ConcurrentScan
 	}
 
 	@After
 	public void cleanUp() {
 		LocalProperties.clearProperty(LocalProperties.GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY);
 	}
 	
 	
 	/**
 	 * Used here to emulate e.g. slow readout() method.
 	 */
 	private class DelayedAnswer implements Answer<Object> {
 		
 		private final double delayS;
 		private final Object result;
 		
 		public DelayedAnswer(double delayS, Object result) {
 			this.delayS = delayS;
 			this.result = result;
 		}
 		
 		@Override
 		public Object answer(InvocationOnMock invocation) throws Throwable {
 			String functionName = ((Scannable) invocation.getMock()).getName() + "." + invocation.getMethod().getName() + "()";
 			
 			System.out.println(functionName + " blocking " + delayS + "s");
 			Thread.sleep((long) (delayS * 1000));
 			System.out.println(functionName + " returning");
 			complete();
 			return result;
 		}
 
 		public void complete() {
 			// for spying
 		}
 		
 	}
 	@Override
 	protected InOrder runBigScan() throws InterruptedException, Exception {
 		Double[] result = new Double[] { 1.0, 2.0 };
 		detlev5Readout1 = spy(new DelayedAnswer(.2, result));
 		detlev9aReadout1 = spy(new DelayedAnswer(.4, result));
 		detlev9bReadout1 = spy(new DelayedAnswer(.5, result));
 		detlev5Readout2 = spy(new DelayedAnswer(.2, result));
 		detlev9aReadout2 = spy(new DelayedAnswer(.4, result));
 		detlev9bReadout2 = spy(new DelayedAnswer(.5, result));
 		when(detlev5.readout()).thenAnswer(detlev5Readout1).thenAnswer(detlev5Readout2);
 		when(detlev9a.readout()).thenAnswer(detlev9aReadout1).thenAnswer(detlev9aReadout2);
 		when(detlev9b.readout()).thenAnswer(detlev9bReadout1).thenAnswer(detlev9bReadout2);
 		super.runBigScan();
 		return inOrder(lev4, lev5a, lev5b, lev6, lev6b, detlev9a, detlev9b, detlev5,
 				detlev5Readout1, detlev9aReadout1,detlev9bReadout1,  detlev5Readout2, detlev9aReadout2,detlev9bReadout2);
 	}
 
 	@Override
 	public void testBigScanWholeMalarchy() throws InterruptedException, Exception {
 		// pass
 		// Instead test the four paths through this code
 	}
 	
 	@Override
 	public void testBigScanLevelReadoutAndGetPosition() throws InterruptedException, Exception {
 		// pass
 		// Instead test the four paths through this code
 	}
 	
 	@Test
 	public void testBigScanWholeMalarchyVerifyMainPathOnly() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanWholeMalarchy", true);
 
 		InOrder inOrder = runBigScan();
 		
 		inOrder.verify(detlev9a).setCollectionTime(2.0); // TODO: Called in the constructor !!!???
 		inOrder.verify(detlev5).setCollectionTime(2.5); // TODO: Called in the constructor !!!???
 		inOrder.verify(detlev9a).prepareForCollection();
 		inOrder.verify(detlev9b).prepareForCollection();
 		inOrder.verify(detlev5).prepareForCollection();
 		verifyBigScanAtScanStart(inOrder);
 		verifyBigScanAtScanLineStart(inOrder);
 
 		verifyBigScanAtPointStart(inOrder);
 		verifyBigScanMoveLevel4Scannables(inOrder, 0.);
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		// don't test readout path
 		inOrder.verify(lev4).atPointEnd();
 		inOrder.verify(lev5a).atPointEnd();
 		inOrder.verify(lev5b).atPointEnd();
 		inOrder.verify(lev6).atPointEnd();
 		inOrder.verify(lev6b).atPointEnd();
 
 		inOrder.verify(lev4).atPointStart();
 		inOrder.verify(lev5a).atPointStart();
 		inOrder.verify(lev5b).atPointStart();
 		inOrder.verify(lev6).atPointStart();
 		inOrder.verify(lev6b).atPointStart();
 		verifyBigScanMoveLevel4Scannables(inOrder, 1.);
 		// readout thread joins here
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		// don't test readout path
 		inOrder.verify(lev4).atPointEnd();
 		inOrder.verify(lev5a).atPointEnd();
 		inOrder.verify(lev5b).atPointEnd();
 		inOrder.verify(lev6).atPointEnd();
 		inOrder.verify(lev6b).atPointEnd();
 
 		verifyBigScanAtScanLineEnd(inOrder);
 		verifyBigScanAtScanEnd(inOrder);
 		inOrder.verify(detlev9a).endCollection();
 		inOrder.verify(detlev5).endCollection();
 
 		verify(detlev9a, times(1)).setCollectionTime(anyDouble());
 		verify(detlev9b, never()).setCollectionTime(anyDouble());
 		verify(detlev5, times(1)).setCollectionTime(anyDouble());
 		verify(detlev9a, times(1)).prepareForCollection();
 		verify(detlev9b, times(1)).prepareForCollection();
 		verify(detlev5, times(1)).prepareForCollection();
 		verify(detlev9a, times(1)).endCollection();
 		verify(detlev9b, times(1)).endCollection();
 		verify(detlev5, times(1)).endCollection();
 	}
 	
 // lev4, 0., 1., 1., lev5a, 1., lev5b, 2., lev6, 3., lev6b, detlev9a, 2., detlev9b, detlev5, 2.5
 	@Test
 	public void testBigScanWholeMalarchyDetlev5Path() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanWholeMalarchy", true);
 
 		InOrder inOrder = runBigScan();
 		
 		verifyBigScanAtPointStart(inOrder);
 		// skip some
 		verifyBigScanGetPosition(inOrder);
 		// main scan path diverges
 		inOrder.verify(detlev5).readout();
 		inOrder.verify(detlev5Readout1).complete();
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 //		inOrder.verify(detlev9a).atPointStart();
 //		inOrder.verify(detlev9b).atPointStart();
 //		inOrder.verify(detlev5).atPointStart();
 		
 		// readout thread joins here
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		
 		inOrder.verify(detlev5).readout();
 		inOrder.verify(detlev5Readout2).complete();
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 		
 		verifyBigScanAtScanLineEnd(inOrder);
 	}
 	@Test
 	public void testBigScanWholeMalarchyDetlev9aPath() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanWholeMalarchy", true);
 		
 		InOrder inOrder = runBigScan();
 		
 		verifyBigScanAtPointStart(inOrder);
 		// skip some
 		verifyBigScanGetPosition(inOrder);
 		// main scan path diverges
 		inOrder.verify(detlev9a).readout();
 		inOrder.verify(detlev9aReadout1).complete();
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 //		inOrder.verify(detlev9a).atPointStart();
 //		inOrder.verify(detlev9b).atPointStart();
 //		inOrder.verify(detlev5).atPointStart();
 		
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		
 		inOrder.verify(detlev9a).readout();
 		inOrder.verify(detlev9aReadout2).complete();
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 		
 		verifyBigScanAtScanLineEnd(inOrder);
 	}
 	@Test
 	public void testBigScanWholeMalarchyDetlev9bPath() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanWholeMalarchy", true);
 		
 		InOrder inOrder = runBigScan();
 		
 		verifyBigScanAtPointStart(inOrder);
 		// skip some
 		verifyBigScanGetPosition(inOrder);
 		// main scan path diverges
 		inOrder.verify(detlev9b).readout();
 		inOrder.verify(detlev9bReadout1).complete();
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 //		inOrder.verify(detlev9a).atPointStart();
 //		inOrder.verify(detlev9b).atPointStart();
 //		inOrder.verify(detlev5).atPointStart();
 		
 		verifyBigScanMoveLevel5AndAboveScannablesAndcollectDetectors(inOrder,  1., 2., 3.);
 		verifyBigScanGetPosition(inOrder);
 		
 		inOrder.verify(detlev9b).readout();
 		inOrder.verify(detlev9bReadout2).complete();
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 		
 		verifyBigScanAtScanLineEnd(inOrder);
 	}
 	
 	@Override
 	@Test
 	public void testBigScanAtPointStartAndEnd() throws InterruptedException, Exception {
 		testScratchDirectoryName = TestHelpers.setUpTest(this.getClass(), "testBigScanAtPointStartAndEnd",
 				true);
 		InOrder inOrder = runBigScan();
 
 		inOrder.verify(lev4).atPointStart();
 		inOrder.verify(lev5a).atPointStart();
 		inOrder.verify(lev5b).atPointStart();
 		inOrder.verify(lev6).atPointStart();
 		inOrder.verify(lev6b).atPointStart();
 		inOrder.verify(detlev9a).atPointStart();// TODO: honour detector level for: atPointStart()?
 		inOrder.verify(detlev9b).atPointStart();
 		inOrder.verify(detlev5).atPointStart();
 		
 		inOrder.verify(lev4).atPointEnd();
 		inOrder.verify(lev5a).atPointEnd();
 		inOrder.verify(lev5b).atPointEnd();
 		inOrder.verify(lev6).atPointEnd();
 		inOrder.verify(lev6b).atPointEnd();
 		
 		inOrder.verify(lev4).atPointStart();
 		inOrder.verify(lev5a).atPointStart();
 		inOrder.verify(lev5b).atPointStart();
 		inOrder.verify(lev6).atPointStart();
 		inOrder.verify(lev6b).atPointStart();
 		
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 		inOrder.verify(detlev9a).atPointStart();
 		inOrder.verify(detlev9b).atPointStart();
 		inOrder.verify(detlev5).atPointStart();
 		
 		inOrder.verify(lev4).atPointEnd();
 		inOrder.verify(lev5a).atPointEnd();
 		inOrder.verify(lev5b).atPointEnd();
 		inOrder.verify(lev6).atPointEnd();
 		inOrder.verify(lev6b).atPointEnd();
 		
 		inOrder.verify(detlev9a).atPointEnd();
 		inOrder.verify(detlev9b).atPointEnd();
 		inOrder.verify(detlev5).atPointEnd();
 	}
 	
 	@Override
 	public void testBigTwoDimensionalScanWholeMalarchy() throws InterruptedException, Exception {
 		//TODO split out the cases here.
 	}
 	
 }
