 /*-
  * Copyright 2012 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.io.ILazyLoader;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class LazyDatasetTest {
 
 	private void setShape(String msg, boolean well, LazyDataset l, int... shape) {
 		try {
 			l.setShape(shape);
 			if (well)
 				System.out.println("Succeeded setting shape for " + msg);
 			else
 				Assert.fail("Should have thrown exception for " + msg);
 		} catch (IllegalArgumentException iae) {
 			// do nothing
 			if (well)
 				Assert.fail("Unexpected exception for " + msg);
 			else
 				System.out.println("Correctly failed setting shape for " + msg);
 		} catch (Exception e) {
 			if (well)
 				Assert.fail("Unexpected exception for " + msg);
 			else
 				Assert.fail("Thrown wrong exception for " + msg);
 		}
 	}
 
 	@Test
 	public void testSetShape() {
 		LazyDataset ld = new LazyDataset("", AbstractDataset.INT, new int[] {1, 2, 3, 4}, null);
 
 		setShape("check on same rank", true, ld, 1, 2, 3, 4);
 		setShape("check on same rank", false, ld, 1, 2, 3, 5);
 
 		setShape("check on greater rank", true, ld, 1, 1, 1, 2, 3, 4);
 		setShape("check on greater rank", false, ld, 1, 2, 2, 3, 5);
 		setShape("check on greater rank", false, ld, 2, 1, 2, 3, 4);
 
 		setShape("check on lesser rank", true, ld, 2, 3, 4);
 		setShape("check on lesser rank", false, ld, 3, 4);
 		setShape("check on lesser rank", false, ld, 2, 3);
 	}
 
 	@Test
 	public void testGetSlice() {
 		final AbstractDataset d = Random.randn(new int[] {1, 2, 3, 4});
 		LazyDataset ld = new LazyDataset("", AbstractDataset.INT, new int[] {1, 2, 3, 4}, new ILazyLoader() {
 			@Override
 			public boolean isFileReadable() {
 				return true;
 			}
 			
 			@Override
 			public AbstractDataset getDataset(IMonitor mon, int[] shape, int[] start, int[] stop, int[] step)
					throws ScanFileHolderException {
 				return d.getSlice(mon, start, stop, step);
 			}
 		});
 
 		Slice[] slice;
 		slice = new Slice[]{null, new Slice(1), null, new Slice(1, 3)};
 		Assert.assertEquals("Full slice", d, ld.getSlice());
 		Assert.assertEquals("Full slice", d, ld.getSlice((Slice) null));
 		Assert.assertEquals("Full slice", d, ld.getSlice((Slice) null, null));
 		Assert.assertEquals("Full slice", d, ld.getSlice(null, null, null));
 		Assert.assertEquals("Full slice", d, ld.getSlice(null, null, new int[] {1, 1, 1, 1}));
 		Assert.assertEquals("Full slice", d, ld.getSlice(new int[4], null, new int[] {1, 1, 1, 1}));
 		Assert.assertEquals("Full slice", d, ld.getSlice(new int[4], new int[] { 1, 2, 3, 4 }, new int[] { 1, 1, 1, 1 }));
 		Assert.assertEquals("Part slice", d.getSlice(slice), ld.getSlice(slice));
 
 		AbstractDataset nd;
 		ld.setShape(1, 1, 1, 2, 3, 4);
 		nd = d.getView();
 		nd.setShape(1, 1, 1, 2, 3, 4);
 		slice = new Slice[]{null, null, null, new Slice(1), null, new Slice(1, 3)};
 		Assert.assertEquals("Full slice", nd, ld.getSlice());
 		Assert.assertEquals("Part slice", nd.getSlice(slice), ld.getSlice(slice));
 
 		ld.setShape(2, 3, 4);
 		nd = d.getView();
 		nd.setShape(2, 3, 4);
 		slice = new Slice[]{new Slice(1), null, new Slice(1, 3)};
 		Assert.assertEquals("Full slice", nd, ld.getSlice());
 		Assert.assertEquals("Part slice", nd.getSlice(slice), ld.getSlice(slice));
 	}
 }
