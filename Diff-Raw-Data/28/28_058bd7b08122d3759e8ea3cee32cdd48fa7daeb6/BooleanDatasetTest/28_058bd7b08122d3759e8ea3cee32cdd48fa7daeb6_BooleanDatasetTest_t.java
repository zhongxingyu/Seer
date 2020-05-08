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
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 
 public class BooleanDatasetTest {
 
 	@Test
 	public void testConstructor() {
 		boolean[] da = { false, true, false, true, false, true, false, true, false, true, false, true};
 		BooleanDataset a = new BooleanDataset(da);
 
 		IndexIterator it = a.getIterator();
 		for (int i = 0; it.hasNext(); i++) {
			assertEquals(i % 2 != 0, a.getElementBooleanAbs(it.index));
 		}
 
 		BooleanDataset b = new BooleanDataset(da, 3, 4);
 
 		it = b.getIterator();
 		for (int i = 0; it.hasNext(); i++) {
			assertEquals(i % 2 != 0, b.getElementBooleanAbs(it.index));
 		}
 
 		// test hashes
 		a.hashCode();
 		b.hashCode();
 	}
 
 	@Test
 	public void testGetter() {
 		boolean[] da = { false, true, false, true, false, true, false, true, false, true, false, true};
 		BooleanDataset a = new BooleanDataset(da);
 		int l = da.length;
 		for (int i = 0; i < l; i++) {
 			assertEquals(da[i], a.getBoolean(i));
 		}
 		
 		for (int i = 0; i < l; i++) {
 			boolean r = da[l - 1 - i];
 			assertEquals(r, a.getBoolean(-(i + 1)));
 		}
 
 		AbstractDataset sv = a.getSliceView(new Slice(2,7));
 		AbstractDataset sc = a.getSlice(new Slice(2,7));
 		l = sc.getSize();
 		for (int i = 0; i < l; i++) {
 			boolean r = sc.getBoolean(-(i + 1));
 			assertEquals(r, sv.getBoolean(-(i + 1)));
 		}
 	}
 }
