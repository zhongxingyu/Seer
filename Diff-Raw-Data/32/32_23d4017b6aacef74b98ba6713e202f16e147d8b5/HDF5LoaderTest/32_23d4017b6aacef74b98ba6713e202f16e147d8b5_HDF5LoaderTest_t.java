 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import gda.analysis.io.ScanFileHolderException;
 import gda.util.TestUtils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import ncsa.hdf.object.FileFormat;
 import ncsa.hdf.object.h5.H5File;
 
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 import uk.ac.diamond.scisoft.analysis.dataset.StringDataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Group;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 
 public class HDF5LoaderTest {
 	final static String TestFileFolder = "testfiles/gda/analysis/io/NexusLoaderTest/";
 
 	private void checkDataset(String name, IDataset data, int[] expectedShape) {
 		int[] shape = data.getShape();
 		assertEquals("Rank of " + name, expectedShape.length, shape.length);
 		for (int i = 0; i < expectedShape.length; i++)
 			assertEquals("Dim of " + name, expectedShape[i], shape[i]);
 	}
 
 	@Test
 	public void testLoadingSpeed() {
 		testLoadingSpeed(false);
 		testLoadingSpeed(true);
 	}
 
 	private void testLoadingSpeed(boolean async) {
 		List<Long> ourTimes = new ArrayList<Long>();
 		List<Long> theirTimes = new ArrayList<Long>();
 		String name = TestFileFolder + "manygroups.h5";
 //		String name = "/dls/sci-scratch/ExampleData/NeXus/XPDSi7x7_2010-07-08_23-00-50.nxs";
 
 		for (int i = 0; i < 3; i++) {
 			long start;
 
 			start = -System.currentTimeMillis();
 			try {
 				HDF5Loader l = new HDF5Loader(name);
 				l.setAsyncLoad(async);
 				l.loadTree(null);
 			} catch (ScanFileHolderException e) {
 			}
 			start += System.currentTimeMillis();
 			ourTimes.add(start);
 
 			start = -System.currentTimeMillis();
 			try {
 				new H5File(name, FileFormat.READ).open();
 			} catch (Exception e) {
 			}
 			start += System.currentTimeMillis();
 			theirTimes.add(start);
 		}
 
 		Collections.sort(ourTimes);
 		Collections.sort(theirTimes);
 		System.out.printf("Load took %d ms cf %d ms\n", ourTimes.get(0), theirTimes.get(0));
 	}
 
 	@Test
 	public void testLoadingStrings() throws ScanFileHolderException {
 		String n = TestFileFolder + "strings1d.h5";
 		HDF5Loader l = new HDF5Loader(n);
 
 		@SuppressWarnings("unused")
 		HDF5File tree = l.loadTree(null);
 
 		n = TestFileFolder + "strings2d.h5";
 		l = new HDF5Loader(n);
 
 		tree = l.loadTree(null);
 	}
 
 	@Test
 	public void testLoadingTest() throws ScanFileHolderException {
 		testLoadingTest(false);
 		testLoadingTest(true);
 	}
 
 	private void testLoadingTest(boolean async) throws ScanFileHolderException {
 		String n = TestFileFolder + "testlinks.nxs";
 		HDF5Loader l = new HDF5Loader(n);
 		l.setAsyncLoad(async);
 
 		HDF5File tree = l.loadTree(null);
 		System.out.println(tree.getNodeLink());
 
 		List<ILazyDataset> list;
 		IDataset dataset;
 		String name;
 
 		// original
 		name = "original";
 		list = tree.getGroup().getDatasets("d1");
 		assertEquals("Number of " + name, 1, list.size());
 		dataset = list.get(0).getSlice();
 		checkDataset(name, dataset, new int[] { 25, 3 });
 		assertEquals("Value in " + name, 1, dataset.getInt(0, 1));
 		assertEquals("Value in " + name, 5, dataset.getInt(1, 2));
 		assertEquals("Value in " + name, 37, dataset.getInt(12, 1));
 
 		// hard link
 		name = "hard link";
 		list = tree.getGroup().getDatasets("d_hl");
 		assertEquals("Number of " + name, 1, list.size());
 		dataset = list.get(0).getSlice();
 		checkDataset(name, dataset, new int[] { 25, 3 });
 		assertEquals("Value in " + name, 1, dataset.getInt(0, 1));
 		assertEquals("Value in " + name, 5, dataset.getInt(1, 2));
 		assertEquals("Value in " + name, 37, dataset.getInt(12, 1));
 
 		// soft link
 		name = "soft link";
 		list = tree.getGroup().getDatasets("d_sl");
 		assertEquals("Number of " + name, 1, list.size());
 		dataset = list.get(0).getSlice();
 		checkDataset(name, dataset, new int[] { 25, 3 });
 		assertEquals("Value in " + name, 1, dataset.getInt(0, 1));
 		assertEquals("Value in " + name, 5, dataset.getInt(1, 2));
 		assertEquals("Value in " + name, 37, dataset.getInt(12, 1));
 
 		// external link
 		name = "external link";
 		list = tree.getGroup().getDatasets("d_el");
 		assertEquals("Number of " + name, 1, list.size());
 		dataset = list.get(0).getSlice();
 		checkDataset(name, dataset, new int[] { 2, 5 });
 		assertEquals("Value of " + name, 1., dataset.getDouble(0, 1), 1e-8);
 		assertEquals("Value of " + name, 9., dataset.getDouble(1, 4), 1e-8);
 
 		// NAPI mount
 		name = "NAPI";
 		list = tree.getGroup().getDatasets("extdst");
 		assertEquals("Number of " + name, 1, list.size());
 		dataset = list.get(0).getSlice();
 		checkDataset(name, dataset, new int[] { 2, 5 });
 		assertEquals("Value of " + name, 1., dataset.getDouble(0, 1), 1e-8);
 		assertEquals("Value of " + name, 9., dataset.getDouble(1, 4), 1e-8);
 
 		System.out.println(tree.findNodeLink("/entry1/to/this/level"));
 	}
 
 	@Test
 	public void testLoadingNames() throws ScanFileHolderException {
 		testLoadingNames(false);
 		testLoadingNames(true);
 	}
 	
 
 	private void testLoadingNames(boolean async) throws ScanFileHolderException {
 		final String n = TestUtils.getGDALargeTestFilesLocation() + "327.nxs";
 		HDF5Loader l = new HDF5Loader(n);
 		l.setAsyncLoad(async);
 
 		HDF5File tree = l.loadTree(null);
 		System.out.println(tree.getNodeLink());
 		for (HDF5NodeLink nl : tree.getGroup())
 			System.out.println(nl);
 
 		HDF5Group g = tree.getGroup().getGroup("entry1");
 		assertEquals("Group is wrongly named" , "/entry1/EDXD_Element_00", g.getNodeLink("EDXD_Element_00").getFullName());
 		g = g.getGroup("EDXD_Element_00");
 		assertEquals("Attribute is wrongly named" , "/entry1/EDXD_Element_00/a@axis", g.getDataset("a").getAttribute("axis").getFullName());
 	}
 
 	@Test
 	public void testLoading() throws ScanFileHolderException {
 		String n = TestFileFolder + "FeKedge_1_15.nxs";
 		HDF5Loader l = new HDF5Loader(n);
 
 		HDF5File tree = l.loadTree(null);
 		System.out.println(tree.getNodeLink());
 
 		HDF5NodeLink nl;
 
 		nl = tree.findNodeLink("/");
 		assertTrue("Not a group", nl.isDestinationAGroup());
 		assertTrue("Wrong name", nl.getName().equals("/"));
 
 		nl = tree.findNodeLink("/entry1");
 		assertTrue("Not a group", nl.isDestinationAGroup());
 		assertTrue("Wrong name", nl.getName().equals("entry1"));
 
 		nl = tree.findNodeLink("/entry1/FFI0");
 		assertTrue("Not a group", nl.isDestinationAGroup());
 		assertTrue("Wrong name", nl.getName().equals("FFI0"));
 
 		nl = tree.findNodeLink("/entry1/FFI0/Energy");
 		assertTrue("Not a group", nl.isDestinationADataset());
 		assertTrue("Wrong name", nl.getName().equals("Energy"));
 
 		nl = tree.getGroup().getNodeLink("entry1");
 		System.out.println(nl);
 
 		nl = ((HDF5Group) nl.getDestination()).getNodeLink("FFI0");
 		System.out.println(nl);
 
 		String name = "Energy";
 		nl = ((HDF5Group) nl.getDestination()).getNodeLink(name);
 		System.out.println(nl);
 
 		List<ILazyDataset> list = tree.getGroup().getDatasets(name);
 		assertEquals("Number of " + name, 1, list.size());
 		IDataset dataset = list.get(0).getSlice();
 		checkDataset(name, dataset, new int[] { 489 });
 		assertEquals("Value of " + name, 6922, dataset.getDouble(2), 6922 * 1e-8);
 		assertEquals("Value of " + name, 7944.5, dataset.getDouble(479), 7944.5 * 1e-8);
 		nl = tree.findNodeLink("/entry1/user01/username");
 		System.out.println(nl);
 		HDF5Node nd = nl.getDestination();
 		assertTrue("Dataset node", nd instanceof HDF5Dataset);
 		HDF5Dataset dn = (HDF5Dataset) nd;
 		assertTrue("String dataset", dn.isString());
 		AbstractDataset a = (AbstractDataset) dn.getDataset();
 		assertEquals("Username", "rjw82", a.getString(0));
 	}
 
 	@Test
 	public void testLoadingMetadata() throws Exception {
 		String n = TestFileFolder + "FeKedge_1_15.nxs";
 		HDF5Loader l = new HDF5Loader(n);
 
 		IMetaData md = l.loadFile().getMetadata();
 
 		System.out.println(md.getMetaNames());
 		assertTrue("Wrong version", md.getMetaValue("/@NeXus_version").equals("4.2.0"));
 
 		assertTrue("Wrong axis value", md.getMetaValue("/entry1/FFI0/Energy@axis").equals("1"));
 
 		assertTrue("Wrong name", md.getMetaValue("/entry1/instrument/source/name").equals("DLS"));
 		assertTrue("Wrong voltage", md.getMetaValue("/entry1/instrument/source/voltage").equals("-1000.0000"));
 
 	}
 
 	@Test
 	public void testLoadingChunked() throws ScanFileHolderException {
 
 		final String n = TestUtils.getGDALargeTestFilesLocation() + "/NexusUITest/sino.h5";
 		long timeAtStartms = System.currentTimeMillis();
 
 		HDF5Loader l = new HDF5Loader(n);
 
 		HDF5File tree = l.loadTree(null);
 		HDF5NodeLink nl;
 		nl = tree.findNodeLink("/RawDCT/data");
 		HDF5Node nd = nl.getDestination();
 		assertTrue("Dataset node", nd instanceof HDF5Dataset);
 		HDF5Dataset dn = (HDF5Dataset) nd;
 
 		AbstractDataset ad;
 		double x;
 
 		// slice with chunks
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(1), new Slice(1), null);
 		checkDataset("data", ad, new int[] { 1, 1, 1481 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 164.12514, x, x * 1e-5);
 
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(1), new Slice(null, null, 3), null);
 		checkDataset("data", ad, new int[] { 1, 75, 1481 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 40271.562, x, x * 1e-5);
 
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(1), new Slice(null, null, 3), new Slice(2));
 		checkDataset("data", ad, new int[] { 1, 75, 2 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 3.7149904, x, x * 1e-5);
 
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(null, null, 2), new Slice(1), null);
 		checkDataset("data", ad, new int[] { 31, 1, 1481 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 10522.864, x, x * 1e-5);
 
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(null, null, 2), new Slice(null, null, 3), null);
 		checkDataset("data", ad, new int[] { 31, 75, 1481 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 1640010.1, x, x * 1e-3);
 
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(null, null, 2), new Slice(null, null, 3), new Slice(2));
 		checkDataset("data", ad, new int[] { 31, 75, 2 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 137.25012, x, x * 1e-5);
 
 		// slice across chunks
 		ad = (AbstractDataset) dn.getDataset().getSlice(new Slice(null, null, 2), new Slice(null, null, 3), new Slice(1, 2));
 		checkDataset("data", ad, new int[] { 31, 75, 1 });
 		x = ((Number) ad.sum()).doubleValue();
 		System.err.println(x);
 		assertEquals("Value of sum", 64.191261, x, x * 1e-5);
 		long timeTaken = System.currentTimeMillis() - timeAtStartms;
 		System.out.printf("Time taken = %d ms\n", timeTaken);
 		assertTrue(timeTaken < 5000);
 	}
 
 	@Test
 	public void testLoadingChunkedSpeed() throws Exception {
 		final String n = TestUtils.getGDALargeTestFilesLocation() + "/NexusUITest/3dDataChunked.nxs";
 		long timeAtStartms = System.currentTimeMillis();
 
		HDF5Loader.loadData(n, "entry/instrument/detector/data", new int[] { 0, 0, 0 }, new int[] { 1, 1795, 2069 },
				new int[] { 1, 1, 1 }, -1, false);
 		long timeTaken = System.currentTimeMillis() - timeAtStartms;
 		System.out.printf("Time taken = %d ms\n", timeTaken);
 		assertTrue(timeTaken < 5000);
 	}
 	
 	
 	@Test
 	public void testCanonicalization() {
 		String[] before = { "/asd/sdf/dfg/../ds/../../gfd", "/asd/asd/../as", "/asd/as/.././bad", "/asd/..", "/abal/." };
 		String[] after = { "/asd/gfd", "/asd/as", "/asd/bad", "/", "/abal" };
 
 		for (int i = 0; i < before.length; i++)
 			assertEquals("Path", after[i], HDF5File.canonicalizePath(before[i]));
 	}
 
 	@Test
 	public void testScanFileHolderLoading() throws ScanFileHolderException {
 		String n = TestFileFolder + "FeKedge_1_15.nxs";
 		HDF5Loader l = new HDF5Loader(n);
 		DataHolder dh = l.loadFile();
 		assertEquals("File does not have the correct number of datasets", 51, dh.getNames().length);
 		if (dh.contains("/entry1/xspress2system/data")) {
 			ILazyDataset data = dh.getLazyDataset("/entry1/xspress2system/data");
 			assertEquals("Dataset is not the right shape", 3, data.getShape().length);
 			assertEquals("Dataset dimention 0 is not of the correct shape", 489, data.getShape()[0]);
 			assertEquals("Dataset dimention 1 is not of the correct shape", 1, data.getShape()[1]);
 			assertEquals("Dataset dimention 2 is not of the correct shape", 64, data.getShape()[2]);
 		}
 	}
 
 	@Test
 	public void testLoadingDatasets() throws ScanFileHolderException {
 		String n = TestFileFolder + "FeKedge_1_15.nxs";
 		HDF5Loader l = new HDF5Loader(n);
 		List<ILazyDataset> ds = l.findDatasets(new String[] {"scan_command", "title"}, 1, null);
 		assertEquals("File does not have the correct number of datasets", 1, ds.size());
 		ILazyDataset d = ds.get(0);
 		assertTrue(d instanceof StringDataset);
 	}
 	
 }
