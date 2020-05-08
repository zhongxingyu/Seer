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
 
 package gda.device.detector.addetector.filewriter;
 
 import static org.junit.Assert.*;
 import gda.configuration.properties.LocalProperties;
 import gda.data.PathConstructor;
 import gda.device.detector.areadetector.v17.NDFile;
 import gda.device.detector.areadetector.v17.NDPluginBase;
 import gda.jython.ICurrentScanInformationHolder;
 import gda.jython.InterfaceProvider;
 import gda.scan.ScanInformation;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 
 // TODO: Tests here are minimal
 
 public class SingleImagePerFileWriterTest {
 
 	private NDPluginBase mockNDPluginBase;
 	
 	private NDFile mockNdFile;
 
 	private SingleImagePerFileWriter writer;
 
 	
 	@Before
 	public void setUp() {
 		mockNDPluginBase = mock(NDPluginBase.class);
 		mockNdFile = mock(NDFile.class);
 		when(mockNdFile.getPluginBase()).thenReturn(mockNDPluginBase);
 		writer = new SingleImagePerFileWriter("detname");
 		writer.setNdFile(mockNdFile);
 		LocalProperties.set(PathConstructor.getDefaultPropertyName(), "path/to/datadir");
 		configureScanInformationHolder();
 	}
 
 	private void configureScanInformationHolder() {
 		ScanInformation scanInfo = mock(ScanInformation.class);
 		ICurrentScanInformationHolder currentScanHolder = mock(ICurrentScanInformationHolder.class);
 		when(currentScanHolder.getCurrentScanInformation()).thenReturn(scanInfo);
 		when(scanInfo.getScanNumber()).thenReturn((long) 12345);
 		InterfaceProvider.setCurrentScanInformationHolderForTesting(currentScanHolder);
 	}
 	
 	@Test
 	public void testGetFileTemplateDefault() {
		assertEquals("%s%s%05d.tif", writer.getFileTemplate());
 	}
 
 	@Test
 	public void testGetFilePathDefault() {
 		assertEquals("path/to/datadir/12345-detname-files", writer.getFilePath());
 	}
 	
 	@Test
 	public void testGetFileNameDefault() {
 		assertEquals("", writer.getFileName());
 	}
 	
 	@Test
 	public void testPrepareforCollectionSetsNextNumberDefault() throws Exception {
 		writer.prepareForCollection(1);
 		verify(mockNdFile).setFileNumber(1);
 	}
 
 	@Test
 	public void testPrepareforCollectionSetsNextNumberNonDefault() throws Exception {
 		writer.setFileNumberAtScanStart(54321);
 		writer.prepareForCollection(1);
 		verify(mockNdFile).setFileNumber(54321);
 	}
 	
 }
