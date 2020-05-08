 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package gda.device.detector.addetector;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import gda.TestHelpers;
 import gda.data.nexus.tree.INexusTree;
 import gda.data.nexus.tree.NexusTreeProvider;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.continuouscontroller.HardwareTriggerProvider;
 import gda.device.detector.NXDetectorData;
 import gda.device.detector.addetector.triggering.HardwareTriggeredPilatus;
 import gda.device.detector.addetector.triggering.SingleExposurePilatus;
 import gda.device.detector.areadetector.v17.ADBase.ImageMode;
 import gda.device.detector.areadetector.v17.ADDriverPilatus;
 import gda.device.detector.areadetector.v17.ADDriverPilatus.PilatusTriggerMode;
 import gda.device.detector.areadetector.v17.NDFile;
 
 import java.io.File;
 import java.util.concurrent.Callable;
 
 import org.junit.Test;
 import org.mockito.InOrder;
 
 // TODO: Move these tests up into ADDetectorTest
 
 public class ADPilatusTest extends ADDetectorTest {
 
 	private static final PilatusTriggerMode HARDWARE_TRIGGER_MODE = PilatusTriggerMode.EXTERNAL_TRIGGER;
 
 	private HardwareTriggerableADDetector adPilatus;
 
 	private ADDriverPilatus mockAdDriverPilatus;
 
 	private HardwareTriggerProvider triggerProvider;
 	
 	private NDFile ndFile;
 
 	public ADPilatusTest() {
 		READOUT_TIME = .003;
 	}
 
 	@Override
 	public Detector det() {
 		return adPilatus;
 	}
 
 	@Override
 	public ADDetector adDet() {
 		return adPilatus;
 	}
 	
 	public HardwareTriggerableADDetector pil() {
 		return adPilatus;
 	}
 
 	@Override
 	protected void createDetector() throws Exception {
 		adPilatus = new HardwareTriggerableADDetector();
 		adPilatus.setIntegratesBetweenPoints(true);
 		mockAdDriverPilatus = mock(ADDriverPilatus.class);
 		collectionStrategy = spy(new SingleExposurePilatus(adBase, 0.003));
 		adPilatus.setNonHardwareTriggeredCollectionStrategy(collectionStrategy); // default strategy
 		adPilatus.setHardwareTriggeredCollectionStrategy(new HardwareTriggeredPilatus(adBase, mockAdDriverPilatus, 0.003, HARDWARE_TRIGGER_MODE)); // default strategy
 		ndFile = mock(NDFile.class);
 		adPilatus.setNdFile(ndFile);
 	}
 
 	@Override
 	protected void setUpNoConfigure() throws Exception {
 		createDetector();
 		super.setUpNoConfigure();
 		triggerProvider = mock(HardwareTriggerProvider.class);
 		pil().setHardwareTriggerProvider(triggerProvider);
 	}
 
 	@Override
 	@Test
 	public void testConstructor() throws Exception {
 		super.testConstructor();
 		// TODO: move elsewhere:assertEquals(TriggerMode.MULT_TRIGGER, pil().getModeForHardwareTriggering());
 		// TODO: move elsewhere:assertEquals(TriggerMode.INTERNAL, pil().getModeForNonHardwareTriggering());
 		// TODO: move elsewhere: assertEquals(.003, pil().getReadoutTime(), .0000001);
 		// TODO: move elsewhere: ssertEquals(.003, pil().getReadoutTime(), .0000001);
 		assertTrue(pil().integratesBetweenPoints());
 		assertFalse(pil().isExposureCompleteWhenFileIsVisible());
 		assertFalse(pil().isExposureCompleteWhenArrayCounterSaysSo());
 		assertEquals(60., pil().getExposureCompletionTimeoutS(), .01);
 	}
 
 	@Test
 	public void testSetHardwareTriggering_True() {
 		// TODO
 	}
 	@Test
 	public void testSetHardwareTriggering_False() {
 		// TODO
 	}
 
 //	getAdBase().setTriggerMode(triggerMode.ordinal());
 //	getAdBase().setNumImages(numImages);
 //	getAdDriverPilatus().setDelayTime(0);
 //	getAdBase().setImageModeWait(ImageMode.MULTIPLE);
 //	getAdBase().setArrayCallbacks((short) 1); // TODO: move from here propbably
 	
 	
 	@Test
 	public void testArm() throws Exception {
 		when(triggerProvider.getNumberTriggers()).thenReturn(11); // one to close at end
 		pil().setHardwareTriggering(true);
 		pil().setCollectionTime(1.);
 		pil().setNumberImagesToCollect(10);
 		pil().collectData();
 		InOrder inOrder = inOrder(adBase, mockAdDriverPilatus);
 		inOrder.verify(adBase).setAcquirePeriod(1.);
 		inOrder.verify(adBase).setAcquireTime(1 - READOUT_TIME);
 		inOrder.verify(adBase).setTriggerMode(HARDWARE_TRIGGER_MODE.ordinal());
 		inOrder.verify(adBase).setImageModeWait(ImageMode.MULTIPLE);
 		inOrder.verify(adBase).setNumImages(10);
 		inOrder.verify(mockAdDriverPilatus).setDelayTime(0);
 		inOrder.verify(adBase).startAcquiring();
 		inOrder.verify(mockAdDriverPilatus).waitForArmed(30);
 	}
 
 	@Test
 	public void testGetPositionCallableOutsideAHardwareTriggeredScan() throws Exception {
 		setupForReadoutAndGetPositionWithFilenameAndTimes();
 		NXDetectorData readout = (NXDetectorData) pil().getPositionCallable().call();
 		checkReadoutWithFilenameAndTimes(readout, "/full/path/to/file99.cbf");
 	}
 
 	@Test
 	public void testGetPositionCallableInAHardwareTriggeredScan() throws Exception {
 		setupGetPositionCallableInAHardwareTriggeredScan("/full/path/to/");
 		Callable<NexusTreeProvider> callablePoint0 = pil().getPositionCallable();
 		Callable<NexusTreeProvider> callablePoint1 = pil().getPositionCallable();
 		Callable<NexusTreeProvider> callablePoint2 = pil().getPositionCallable();
 		checkReadoutWithFilenameAndTimes((NXDetectorData) callablePoint0.call(), "/full/path/to/file99_00000.cbf");
 		checkReadoutWithFilenameAndTimes((NXDetectorData) callablePoint1.call(), "/full/path/to/file99_00001.cbf");
 		checkReadoutWithFilenameAndTimes((NXDetectorData) callablePoint2.call(), "/full/path/to/file99_00002.cbf");
 	}
 
 	protected void setupGetPositionCallableInAHardwareTriggeredScan(String filepath) throws Exception, DeviceException {
 		setupForReadoutAndGetPositionWithFilenameAndTimes();
 		when(ndFile.getFileTemplate_RBV()).thenReturn("%s%s%d.cbf");
 		when(ndFile.getFilePath_RBV()).thenReturn(filepath);
 		when(ndFile.getFileName_RBV()).thenReturn("file");
 		when(ndFile.getFileNumber_RBV()).thenReturn(99);
 		pil().atScanLineStart();
 		pil().setHardwareTriggering(true);
 		pil().collectData();
 	}
 
 	@Override
 	protected void checkReadoutWithFilenameAndTimes(NXDetectorData readout, String pathname) {
 		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
 		assertArrayEquals(new double[] { 0.5 }, (double[]) rootNode.getChildNode("count_time", "SDS").getData()
 				.getBuffer(), .001);
 		assertArrayEquals(new double[] { 0.55 }, (double[]) rootNode.getChildNode("period", "SDS").getData()
 				.getBuffer(), .001);
 		String actualPath = new String((byte[]) rootNode.getChildNode("data_file", "NXnote")
 				.getChildNode("file_name", "SDS").getData().getBuffer());
 		assertEquals(pathname, actualPath.trim()); // trim gets rid of the internal null bytes from
 	}
 
 	@Test
 	public void testPositionCallableWaitingNoChecks() throws Exception {
 		setupGetPositionCallableInAHardwareTriggeredScan("/full/path/to/");
 		pil().getPositionCallable().call();
 		verify(adBase, never()).waitForArrayCounterToReach(anyInt(), anyInt());
 		// Will hang if waiting for file
 	}
 
 	@Test
 	public void testPositionCallableWaitingCheckingAgainstCounter() throws Exception {
 		pil().setExposureCompleteWhenArrayCounterSaysSo(true);
 		setupGetPositionCallableInAHardwareTriggeredScan("/full/path/to/");
 		pil().getPositionCallable().call();
 		verify(adBase).waitForArrayCounterToReach(anyInt(), anyInt());
 		// Will hang if waiting for file
 
 	}
 
 	@Test
 	public void testPositionCallableWaitingCheckingAgainstFile() throws Exception {
 		pil().setExposureCompleteWhenFileIsVisible(true);
 		String testdir = TestHelpers.setUpTest(ADPilatusTest.class, "testPositionCallableWaitingCheckingAgainstFile",
 				true) + "/";
 		setupGetPositionCallableInAHardwareTriggeredScan(testdir);
 		File file = new File(pil().createFileNameForExposure(0));
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				try {
 					pil().getPositionCallable().call();
 				} catch (Exception e) {
 					fail("Exception: " + e.toString());
 				}
 			}
 		};
 		t.start();
 		Thread.sleep(500);
 		assertTrue(t.isAlive());
 		assertTrue(file.createNewFile());
 		t.join(10000);
 	}
 }
