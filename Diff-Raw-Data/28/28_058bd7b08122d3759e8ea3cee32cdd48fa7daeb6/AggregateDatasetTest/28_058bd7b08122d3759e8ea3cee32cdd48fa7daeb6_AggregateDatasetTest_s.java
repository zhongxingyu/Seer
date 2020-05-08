 /*-
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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AggregateDatasetTest {
 	ILazyDataset[] datasets;
 
 	@Before
 	public void init() {
 		datasets = new ILazyDataset[] {
 				AbstractDataset.zeros(new int[] {2,3}, AbstractDataset.INT32).fill(0),
 				AbstractDataset.zeros(new int[] {2,3}, AbstractDataset.INT32).fill(1),
 				AbstractDataset.zeros(new int[] {2,3}, AbstractDataset.INT32).fill(2),
 				AbstractDataset.zeros(new int[] {2,3}, AbstractDataset.INT32).fill(3),
 				AbstractDataset.zeros(new int[] {2,3}, AbstractDataset.INT32).fill(4)
 		};
 	}
 
 	@Test
 	public void testConstructorFailures() {
 		@SuppressWarnings("unused")
 		AggregateDataset a;
 		try {
 			a = new AggregateDataset(true, new ILazyDataset[] {});
 		} catch (Exception e) {
 			try {
 				a = new AggregateDataset(false, new ILazyDataset[] {});
 			} catch (Exception e1) {
 				try {
 					a = new AggregateDataset(false, new ILazyDataset[] {
 							AbstractDataset.ones(new int[] { 2, 3 }, AbstractDataset.BOOL),
 							AbstractDataset.ones(new int[] { 3, 4 }, AbstractDataset.BOOL), });
 				} catch (Exception e2) {
 					try {
 						a = new AggregateDataset(true, new ILazyDataset[] {
 								AbstractDataset.ones(new int[] { 2, 3 }, AbstractDataset.BOOL),
 								AbstractDataset.ones(new int[] { 3, 3 }, AbstractDataset.BOOL), });
 					} catch (Exception e3) {
 						return;
 					}
 				}
 			}
 		}
 		fail("No exceptions thrown!");
 	}
 
 	@Test
 	public void testUnextendedShape() {
 		AggregateDataset a = new AggregateDataset(false, datasets);
 		assertEquals("Incorrect rank", datasets[0].getRank(), a.getRank());
 		assertArrayEquals("Incorrect shape", new int[] {2*datasets.length, 3}, a.getShape());
 
 		AbstractDataset s;
 		s = DatasetUtils.convertToAbstractDataset(a.getSlice((Slice) null, null));
 		assertArrayEquals("Incorrect shape", new int[] {2*datasets.length, 3}, s.getShape());
 
 		s = DatasetUtils.convertToAbstractDataset(a.getSlice(null, new int[] {2,2}, null));
 		assertArrayEquals("Incorrect shape", new int[] {2, 2}, s.getShape());
 
 		s = DatasetUtils.convertToAbstractDataset(a.getSlice(new int[] {1,0}, new int[] {2,2}, null));
 		assertArrayEquals("Incorrect shape", new int[] {1, 2}, s.getShape());
 	}
 
 	@Test
 	public void testExtendedShape() {
 		AggregateDataset a = new AggregateDataset(true, datasets);
 		assertEquals("Incorrect rank", datasets[0].getRank() + 1, a.getRank());
 		assertArrayEquals("Incorrect shape", new int[] {datasets.length, 2, 3}, a.getShape());
 
 		AbstractDataset s;
 		s = DatasetUtils.convertToAbstractDataset(a.getSlice((Slice) null, null, null));
 		assertArrayEquals("Incorrect shape", new int[] {datasets.length, 2, 3}, s.getShape());
 
 		s = DatasetUtils.convertToAbstractDataset(a.getSlice(null, new int[] {2,2,2}, null));
 		assertArrayEquals("Incorrect shape", new int[] {2, 2, 2}, s.getShape());
 
 		s = DatasetUtils.convertToAbstractDataset(a.getSlice(new int[] {1,0,0}, new int[] {2,2,2}, null));
 		assertArrayEquals("Incorrect shape", new int[] {1, 2, 2}, s.getShape());
 	}
 
 	@Test
 	public void testRepeatedDataset() {
 		AbstractDataset a = AbstractDataset.arange(3, AbstractDataset.FLOAT64);
 		AbstractDataset[] as = new AbstractDataset[5];
 		Arrays.fill(as, a);
 		AggregateDataset b = new AggregateDataset(true, as);
 		assertEquals("Incorrect rank", a.getRank() + 1, b.getRank());
 		assertArrayEquals("Incorrect shape", new int[] {as.length, 3}, b.getShape());
 
 		AbstractDataset s;
 		s = DatasetUtils.convertToAbstractDataset(b.getSlice(new int[] {1,0}, new int[] {2,2}, null));
 		assertArrayEquals("Incorrect shape", new int[] {1, 2}, s.getShape());
 		assertArrayEquals("Incorrect values", new double[] {0, 1}, (double[])s.getBuffer(), 1e-5);
 
 		s = DatasetUtils.convertToAbstractDataset(b.getSlice(new int[] {0,1}, new int[] {2,2}, null));
 		assertArrayEquals("Incorrect shape", new int[] {2, 1}, s.getShape());
 		assertArrayEquals("Incorrect values", new double[] {1, 1}, (double[])s.getBuffer(), 1e-5);
 	}
 }
