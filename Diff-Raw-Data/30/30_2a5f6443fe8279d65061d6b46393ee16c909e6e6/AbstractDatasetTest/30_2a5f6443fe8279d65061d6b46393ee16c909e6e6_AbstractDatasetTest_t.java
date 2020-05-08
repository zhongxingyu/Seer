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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.HashSet;
 
 import org.apache.commons.math.complex.Complex;
 import org.junit.Test;
 
 public class AbstractDatasetTest {
 
 	@Test
 	public void testCompatibleShapes() {
 		assertTrue("[] and []", AbstractDataset.areShapesCompatible(new int[] {}, new int[] {}));
 		assertTrue("[1] and []", AbstractDataset.areShapesCompatible(new int[] {1}, new int[] {}));
 		assertFalse("[2] and []", AbstractDataset.areShapesCompatible(new int[] {2}, new int[] {}));
 		assertTrue("[2] and [2]", AbstractDataset.areShapesCompatible(new int[] {2}, new int[] {2}));
 		assertTrue("[3] and [3]", AbstractDataset.areShapesCompatible(new int[] {3}, new int[] {3}));
 		assertTrue("[1,2] and [2]", AbstractDataset.areShapesCompatible(new int[] {1,2}, new int[] {2}));
 		assertTrue("[2] and [1,2]", AbstractDataset.areShapesCompatible(new int[] {2}, new int[] {1,2}));
 		assertFalse("[10,10] and [10,10,10]", AbstractDataset.areShapesCompatible(new int[] {10,10}, new int[] {10,10,10}));
 		assertFalse("[10,10,10] and [10,10]", AbstractDataset.areShapesCompatible(new int[] {10,10,10}, new int[] {10,10}));
 		assertTrue("[2] and [2,1,1,1]", AbstractDataset.areShapesCompatible(new int[] {2}, new int[] {2,1,1,1}));
 		assertTrue("[2,1] and [2,1,1,1]", AbstractDataset.areShapesCompatible(new int[] {2,1}, new int[] {2,1,1,1}));
 		assertFalse("[2,1] and [3,1,1,2]", AbstractDataset.areShapesCompatible(new int[] {2,1}, new int[] {3,1,1,2}));
 		assertFalse("[2,1] and [3,1,1,1]", AbstractDataset.areShapesCompatible(new int[] {2,1}, new int[] {3,1,1,1}));
 		assertTrue("[1,2,1] and [2,1,1,1]", AbstractDataset.areShapesCompatible(new int[] {1,2,1}, new int[] {2,1,1,1}));
 		assertTrue("[1,2,1,3] and [2,1,1,1,3]", AbstractDataset.areShapesCompatible(new int[] {1,2,1,3}, new int[] {2,1,1,1,3}));
 		assertTrue("[2,1,1] and [1,1,2]", AbstractDataset.areShapesCompatible(new int[] {2,1,1}, new int[] {1,1,2}));
 		assertFalse("[2,1,1] and [1,1,3]", AbstractDataset.areShapesCompatible(new int[] {2,1,1}, new int[] {1,1,3}));
 		assertFalse("[2,1,4] and [2,1,1,3]", AbstractDataset.areShapesCompatible(new int[] {2,1,4}, new int[] {2,1,1,3}));
 		assertFalse("[2,1,4] and [2,1,3]", AbstractDataset.areShapesCompatible(new int[] {2,1,4}, new int[] {2,1,3}));
 		assertFalse("[2,4] and [2,3]", AbstractDataset.areShapesCompatible(new int[] {2,4}, new int[] {2,3}));
 		assertTrue("[2,1,4] and [2,1,4]", AbstractDataset.areShapesCompatible(new int[] {2,1,4}, new int[] {2,1,4}));
 		assertTrue("[2,1,4] and [2,1,1,4]", AbstractDataset.areShapesCompatible(new int[] {2,1,4}, new int[] {2,1,1,4}));
 		assertFalse("[2,4] and [2,4,3]", AbstractDataset.areShapesCompatible(new int[] {2,4}, new int[] {2,4,3}));
 		assertFalse("[2,1,4] and [2,4,3]", AbstractDataset.areShapesCompatible(new int[] {2,1,4}, new int[] {2,4,3}));
 //		assertTrue(AbstractDataset.areShapesCompatible(new int[] {}, new int[] {}));
 	}
 
 	@Test
 	public void testMax() {
 		AbstractDataset a = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		System.out.println(a.max());
 	}
 
 	@Test
 	public void testMaxSpeed() {
 		long start;
 		long elapsed;
 		final int ITERATIONS = 200;
 
 		AbstractDataset a = AbstractDataset.arange(1000000, AbstractDataset.FLOAT64);
 		for (int i = 0; i < 10; i++) {
 			a.set(1, 0);
 			start = -System.nanoTime();
 			a.max();
 			start += System.nanoTime();
 		}
 		elapsed = 0;
 		for (int i = 0; i < ITERATIONS; i++) {
 			a.set(1, 0);
 			start = -System.nanoTime();
 			a.max();
 			start += System.nanoTime();
 
 			elapsed += start;
 		}
 		System.out.printf("Max double calculation took %g ms\n", elapsed*1e-6/ITERATIONS);
 
 		a = AbstractDataset.arange(1000000, AbstractDataset.INT16);
 		elapsed = 0;
 		for (int i = 0; i < ITERATIONS; i++) {
 			a.set(1, 0);
 			start = -System.nanoTime();
 			a.max();
 			start += System.nanoTime();
 
 			elapsed += start;
 		}
 		System.out.printf("Max short calculation took %g ms\n", elapsed*1e-6/ITERATIONS);
 	}
 
 	@Test
 	public void testSort() {
 		AbstractDataset a = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		a.set(12, 0);
 		System.out.println(a.sort(null));
 	}
 
 	@Test
 	public void testTake() {
 		AbstractDataset a = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		AbstractDataset t;
 
 		t = a.take(new int[] {0, 2, 4}, 0);
 		System.out.println(t);
 
 		a.setShape(new int[] {3,4});
 		System.out.println(a);
 
 		t = a.take(new int[] {0}, 0);
 		System.out.println(t);
 
 		t = a.take(new int[] {1}, 0);
 		System.out.println(t);
 
 		t = a.take(new int[] {2}, 0);
 		System.out.println(t);
 
 		t = a.take(new int[] {0}, 1);
 		System.out.println(t);
 
 		t = a.take(new int[] {1}, 1);
 		System.out.println(t);
 
 		t = a.take(new int[] {2}, 1);
 		System.out.println(t);
 
 		t = a.take(new int[] {3}, 1);
 		System.out.println(t);
 
 	}
 
 	/**
 	 * Tests for squeeze method
 	 */
 	@Test
 	public void testSqueeze() {
 		AbstractDataset ds = AbstractDataset.arange(10, AbstractDataset.FLOAT64);
 		ds.setShape(2,1,5);
 
 		ds.squeeze();
 		assertEquals(2, ds.getShape().length);
 		assertEquals(2, ds.getShape()[0]);
 		assertEquals(5, ds.getShape()[1]);
 	}
 
 	/**
 	 * Tests for tile method
 	 */
 	@Test
 	public void testTile() {
 		// 1D
 		AbstractDataset ds = AbstractDataset.arange(3, AbstractDataset.FLOAT64);
 
 		AbstractDataset ta = DatasetUtils.tile(ds, 2);
 		double[] xa = { 0., 1., 2., 0., 1., 2. };
 
 		assertEquals(1, ta.getShape().length);
 		assertEquals(6, ta.getShape()[0]);
 		for (int i = 0; i < xa.length; i++) {
 			assertEquals(xa[i], ((DoubleDataset) ta).getData()[i], 1e-6);
 		}
 
 		AbstractDataset tb = DatasetUtils.tile(ds, 1, 2);
 
 		assertEquals(2, tb.getShape().length);
 		assertEquals(1, tb.getShape()[0]);
 		assertEquals(6, tb.getShape()[1]);
 		for (int i = 0; i < xa.length; i++) {
 			assertEquals(xa[i], ((DoubleDataset) tb).getData()[i], 1e-6);
 		}
 
 		AbstractDataset tc = DatasetUtils.tile(ds, 2, 1);
 
 		assertEquals(2, tc.getShape().length);
 		assertEquals(2, tc.getShape()[0]);
 		assertEquals(3, tc.getShape()[1]);
 		for (int i = 0; i < xa.length; i++) {
 			assertEquals(xa[i], ((DoubleDataset) tc).getData()[i], 1e-6);
 		}
 
 		// 2D
 		ds = AbstractDataset.arange(6, AbstractDataset.FLOAT64);
 		ds.setShape(2,3);
 		AbstractDataset td = DatasetUtils.tile(ds, 2);
 		double[] xd = { 0., 1., 2., 0., 1., 2., 3., 4., 5., 3., 4., 5. };
 
 		assertEquals(2, td.getShape().length);
 		assertEquals(2, td.getShape()[0]);
 		assertEquals(6, td.getShape()[1]);
 		for (int i = 0; i < xd.length; i++) {
 			assertEquals(xd[i], ((DoubleDataset) td).getData()[i], 1e-6);
 		}
 
 		AbstractDataset te = DatasetUtils.tile(ds, 1, 2);
 
 		assertEquals(2, te.getShape().length);
 		assertEquals(2, te.getShape()[0]);
 		assertEquals(6, te.getShape()[1]);
 		for (int i = 0; i < xd.length; i++) {
 			assertEquals(xd[i], ((DoubleDataset) te).getData()[i], 1e-6);
 		}
 
 		AbstractDataset tf = DatasetUtils.tile(ds, 2, 1);
 		double[] xf = { 0., 1., 2., 3., 4., 5., 0., 1., 2., 3., 4., 5. };
 
 		assertEquals(2, tf.getShape().length);
 		assertEquals(4, tf.getShape()[0]);
 		assertEquals(3, tf.getShape()[1]);
 		for (int i = 0; i < xf.length; i++) {
 			assertEquals(xf[i], ((DoubleDataset) tf).getData()[i], 1e-6);
 		}
 	}
	
	@Test
	public void testTileSpeed() throws Exception {

		final AbstractDataset a = new DoubleDataset(new double[]{1.0}, 1);
		
		long start, end;
		
		start = System.currentTimeMillis();
		final double[] da = new double[2048*2048];
		for (int i = 0; i < da.length; i++) {
			da[i] = 1.0d;
		}
		end = System.currentTimeMillis();
		long diff1 = end-start;
		System.out.println("Array 2028x2048 completed in "+diff1+"ms");
		
 
		start = System.currentTimeMillis();
		final AbstractDataset tiled = DatasetUtils.tile(a, 2048, 2048);
		end = System.currentTimeMillis();
		assertEquals(2048, tiled.getShape()[1]);
		long diff2 = end-start;
		
		if (diff2>(diff1*20)) throw new Exception("Creation of tile took more than 20x as long as array creation of same size!");
		
		System.out.println("Tile 2028x2048 completed in "+diff2+"ms");
		
	}
 	/**
 	 * Tests for transpose method
 	 */
 	@Test
 	public void testTranspose() {
 		// 2D
 		AbstractDataset ds = AbstractDataset.arange(6, AbstractDataset.FLOAT64);
 		ds.setShape(2,3);
 
 		AbstractDataset ta = DatasetUtils.transpose(ds, 1, 0);
 		double[][] xa = { { 0., 1., 2. }, { 3., 4., 5. } };
 
 		assertEquals(2, ta.getShape().length);
 		assertEquals(3, ta.getShape()[0]);
 		assertEquals(2, ta.getShape()[1]);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 3; j++) {
 				assertEquals(xa[i][j], ta.getDouble(j, i), 1e-6);
 			}
 		}
 
 		// 3D
 		ds = AbstractDataset.arange(24, AbstractDataset.FLOAT64);
 		ds.setShape(2, 3, 4);
 
 		double[][][] xb = { {{ 0., 1., 2., 3.}, {4., 5., 6., 7.}, {8., 9., 10., 11. }},
 				{{12., 13., 14., 15.}, {16., 17., 18., 19.}, {20., 21., 22., 23.}} };
 
 		AbstractDataset tb;
 
 		try {
 			tb = DatasetUtils.transpose(ds, 0);
 		} catch (IllegalArgumentException e) {
 			// this is correct.
 		} catch (Exception e) {
 			fail("wrong exception type passed from incorrect arguments being passed to the constructor");
 		}
 
 		try {
 			tb = DatasetUtils.transpose(ds, 0, -1, 0);
 		} catch (IllegalArgumentException e) {
 			// this is correct.
 		} catch (Exception e) {
 			fail("wrong exception type passed from incorrect arguments being passed to the constructor");
 		}
 
 		try {
 			tb = DatasetUtils.transpose(ds, 0, 1, 1);
 		} catch (IllegalArgumentException e) {
 			// this is correct.
 		} catch (Exception e) {
 			fail("wrong exception type passed from incorrect arguments being passed to the constructor");
 		}
 
 		tb = DatasetUtils.transpose(ds, 0, 1, 2);
 		assertEquals(3, tb.getShape().length);
 		assertEquals(2, tb.getShape()[0]);
 		assertEquals(3, tb.getShape()[1]);
 		assertEquals(4, tb.getShape()[2]);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 3; j++) {
 				for (int k = 0; k < 4; k++) {
 					assertEquals(xb[i][j][k], tb.getDouble(i, j, k), 1e-6);
 				}
 			}
 		}
 
 		tb = DatasetUtils.transpose(ds, 1, 0, 2);
 		assertEquals(3, tb.getShape().length);
 		assertEquals(3, tb.getShape()[0]);
 		assertEquals(2, tb.getShape()[1]);
 		assertEquals(4, tb.getShape()[2]);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 3; j++) {
 				for (int k = 0; k < 4; k++) {
 					assertEquals(xb[i][j][k], tb.getDouble(j, i, k), 1e-6);
 				}
 			}
 		}
 
 		tb = DatasetUtils.transpose(ds, 2, 0, 1);
 		assertEquals(3, tb.getShape().length);
 		assertEquals(4, tb.getShape()[0]);
 		assertEquals(2, tb.getShape()[1]);
 		assertEquals(3, tb.getShape()[2]);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 3; j++) {
 				for (int k = 0; k < 4; k++) {
 					assertEquals(xb[i][j][k], tb.getDouble(k, i, j), 1e-6);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Tests for repeat method
 	 */
 	@Test
 	public void testRepeat() {
 		// 2D
 		AbstractDataset ds = AbstractDataset.arange(6, AbstractDataset.FLOAT64);
 		ds.setShape(2,3);
 
 		double[] xa = { 0., 0., 1., 1., 2., 2., 3., 3., 4., 4., 5., 5. };
 		DoubleDataset ta = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {2}, -1);
 		assertEquals(1, ta.getShape().length);
 		assertEquals(12, ta.getShape()[0]);
 		for (int i = 0; i < 12; i++) {
 			assertEquals(xa[i], ta.get(i), 1e-6);
 		}
 
 		double[][] xb = { { 0., 0., 1., 1., 2., 2. }, {  3., 3., 4., 4., 5., 5. }  };
 		DoubleDataset tb = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {2}, 1);
 		assertEquals(2, tb.getShape().length);
 		assertEquals(2, tb.getShape()[0]);
 		assertEquals(6, tb.getShape()[1]);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 6; j++) {
 				assertEquals(xb[i][j], tb.get(i, j), 1e-6);
 			}
 		}
 
 		double[][] xc = { { 0., 1., 2. }, { 0., 1., 2. }, {  3., 4., 5. }, {  3., 4., 5. }  };
 		DoubleDataset tc = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {2}, 0);
 		assertEquals(2, tc.getShape().length);
 		assertEquals(4, tc.getShape()[0]);
 		assertEquals(3, tc.getShape()[1]);
 		for (int i = 0; i < 4; i++) {
 			for (int j = 0; j < 3; j++) {
 				assertEquals(xc[i][j], tc.get(i, j), 1e-6);
 			}
 		}
 
 		double[][] xd = { { 0., 1., 2. }, { 0., 1., 2. }, {  3., 4., 5. } };
 		DoubleDataset td = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {2, 1}, 0);
 		assertEquals(2, td.getShape().length);
 		assertEquals(3, td.getShape()[0]);
 		assertEquals(3, td.getShape()[1]);
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				assertEquals(xd[i][j], td.get(i, j), 1e-6);
 			}
 		}
 
 		double[][] xe = { { 0., 1., 1., 2., 2., 2.}, {  3., 4., 4., 5., 5., 5. }  };
 		DoubleDataset te = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {1, 2, 3}, 1);
 		assertEquals(2, te.getShape().length);
 		assertEquals(2, te.getShape()[0]);
 		assertEquals(6, te.getShape()[1]);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 6; j++) {
 				assertEquals(xe[i][j], te.get(i, j), 1e-6);
 			}
 		}
 
 		double[] xf = { 0., 1., 2., 2., 5., 5., 5. };
 		DoubleDataset tf = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {1, 1, 2, 0, 0, 3}, -1);
 		assertEquals(1, tf.getShape().length);
 		assertEquals(7, tf.getShape()[0]);
 		for (int i = 0; i < 7; i++) {
 			assertEquals(xf[i], tf.get(i), 1e-6);
 		}
 
 		// expanded dataset test
 		AbstractDataset dt = AbstractDataset.arange(1024, AbstractDataset.FLOAT64);
 		dt.set(0, 1024);
 
 		DoubleDataset tg = (DoubleDataset) DatasetUtils.repeat(dt, new int[] {2}, -1);
 		assertEquals(1, tg.getShape().length);
 		assertEquals(2050, tg.getShape()[0]);
 		for (int i = 0; i < 1024; i++) {
 			assertEquals(i, tg.get(2*i), 1e-6);
 			assertEquals(i, tg.get(2*i+1), 1e-6);
 		}
 
 		// expanded 2D dataset test
 		dt = AbstractDataset.arange(1024, AbstractDataset.FLOAT64);
 		dt.setShape(16, 64);
 		dt.set(0., 0, 64);
 
 		tg = (DoubleDataset) DatasetUtils.repeat(dt, new int[] {2}, 1);
 		assertEquals(2, tg.getShape().length);
 		assertEquals(16, tg.getShape()[0]);
 		assertEquals(130, tg.getShape()[1]);
 		for (int i = 0; i < 16; i++) {
 			for (int j = 0; j < 64; j++) {
 				assertEquals(i*64+j, tg.get(i, 2*j), 1e-6);
 				assertEquals(i*64+j, tg.get(i, 2*j+1), 1e-6);
 			}
 		}
 
 		tg = (DoubleDataset) DatasetUtils.repeat(dt, new int[] {2}, 0);
 		assertEquals(2, tg.getShape().length);
 		assertEquals(32, tg.getShape()[0]);
 		assertEquals(65, tg.getShape()[1]);
 		for (int i = 0; i < 16; i++) {
 			for (int j = 0; j < 64; j++) {
 				assertEquals(i*64+j, tg.get(2*i, j), 1e-6);
 				assertEquals(i*64+j, tg.get(2*i+1, j), 1e-6);
 			}
 		}
 
 		try {
 			tf = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {0}, 3);
 		} catch (IllegalArgumentException e) {
 			// this is correct.
 		} catch (Exception e) {
 			fail("wrong exception type passed from incorrect arguments being passed to the constructor");
 		}
 
 		try {
 			tf = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {2, 1}, -1);
 		} catch (IllegalArgumentException e) {
 			// this is correct.
 		} catch (Exception e) {
 			fail("wrong exception type passed from incorrect arguments being passed to the constructor");
 		}
 
 		try {
 			tf = (DoubleDataset) DatasetUtils.repeat(ds, new int[] {-1}, -1);
 		} catch (IllegalArgumentException e) {
 			// this is correct.
 		} catch (Exception e) {
 			fail("wrong exception type passed from incorrect arguments being passed to the constructor");
 		}
 	}
 
 	/**
 	 * Test contents functions
 	 */
 	@Test
 	public void testContents() {
 		double[] x = { 0, 2., -12.3 };
 		double[] y = { 2.3, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };
 		double[] z = { 1e14, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };
 
 		DoubleDataset ta = new DoubleDataset(x);
 		assertEquals(false, ta.containsNans());
 		assertEquals(false, ta.containsInfs());
 
 		DoubleDataset tb = new DoubleDataset(y);
 		assertEquals(true, tb.containsNans());
 		assertEquals(true, tb.containsInfs());
 		assertEquals(true, Double.isNaN(tb.min().doubleValue()));
 		assertEquals(false, Double.isInfinite(tb.min().doubleValue()));
 		assertEquals(true, Double.isNaN(tb.max().doubleValue()));
 		assertEquals(false, Double.isInfinite(tb.max().doubleValue()));
 
 		AbstractDataset f = tb.cast(AbstractDataset.FLOAT32);
 		assertEquals(true, f.containsNans());
 		assertEquals(true, f.containsInfs());
 		assertEquals(true, Double.isNaN(f.min().doubleValue()));
 		assertEquals(false, Double.isInfinite(f.min().doubleValue()));
 		assertEquals(true, Double.isNaN(f.max().doubleValue()));
 		assertEquals(false, Double.isInfinite(f.max().doubleValue()));
 
 		DoubleDataset tc = new DoubleDataset(z);
 		assertEquals(true, Double.isInfinite(tc.min().doubleValue()));
 		assertEquals(true, Double.isInfinite(tc.max().doubleValue()));
 
 		ta = DoubleDataset.arange(1024);
 		ta.set(3., 1024);
 		assertEquals(false, ta.containsNans());
 		assertEquals(false, ta.containsInfs());
 	}
 	
 	/**
 	 * Test auto allocation
 	 */
 	@Test
 	public void testAutoAllocation() {
 		DoubleDataset y = new DoubleDataset(1);
 		for(int i=0; i< 1000; i++){
 			y.set(i,i);
 		}
 		IndexIterator iter = y.getIterator();
 		int i=0;
 		while(iter.hasNext()){
 			if( i< 1000){
 				assertEquals(i, y.get(iter.index), i*1e-5);
 			} 
 			i++;
 		}
 		assertEquals(i, 1000);
 	}
 
 	@Test
 	public void testView() {
 		AbstractDataset a = AbstractDataset.arange(20, AbstractDataset.FLOAT64);
 		AbstractDataset b = a.getView();
 		assertEquals(true, a.equals(b));
 	}
 
 	/**
 	 * Test equals and hashCode
 	 */
 	@Test
 	public void testEquals() {
 		AbstractDataset a, b, c, d, e;
 		a = AbstractDataset.arange(20, AbstractDataset.FLOAT64);
 		b = AbstractDataset.arange(20, AbstractDataset.FLOAT64);
 		c = a.clone();
 		d = Maths.add(a, 0.5);
 		e = AbstractDataset.arange(20, AbstractDataset.FLOAT32);
 
 		assertTrue(a.equals(b));
 		assertFalse(a == b);
 		assertTrue(a.equals(c));
 		assertFalse(a == c);
 		assertFalse(a.equals(d));
 		assertFalse(a.equals(e));
 		HashSet<AbstractDataset> set = new HashSet<AbstractDataset>();
 		set.add(a);
 		assertTrue(set.contains(a));
 		assertTrue(set.contains(b));
 		assertTrue(set.contains(c));
 		assertFalse(set.contains(d));
 		assertFalse(set.contains(e));
 		set.add(b);
 		assertEquals(1, set.size());
 		set.add(d);
 		set.add(e);
 		assertEquals(3, set.size());
 		assertTrue(set.contains(d));
 		assertTrue(set.contains(e));
 		assertTrue(set.contains(Maths.subtract(d, 0.5)));
 		assertFalse(set.contains(Maths.subtract(d, 0.5001)));
 		assertTrue(set.contains(e.cast(AbstractDataset.FLOAT64)));
 		assertTrue(set.contains(b.cast(AbstractDataset.FLOAT32)));
 	}
 
 	@Test
 	public void testPrint() {
 		AbstractDataset a = AbstractDataset.arange(1000000, AbstractDataset.INT32);
 
 		System.out.println(a);
 
 		System.out.println(a.reshape(1000, 1000));
 
 		System.out.println(a.reshape(100, 100, 100));
 
 //		System.out.println(a.reshape(10, 10, 100, 100));
 
 		AbstractDataset b = AbstractDataset.arange(12, AbstractDataset.INT32);
 
 		System.out.println(b);
 
 		System.out.println(b.reshape(1,12));
 
 		System.out.println(b.reshape(4,1,3));
 	}
 	
 	@Test
 	public void testSlicing() {
 		AbstractDataset a = AbstractDataset.arange(1000, AbstractDataset.INT32);
 		AbstractDataset s, t;
 		IndexIterator is, it;
 
 		s = a.getSlice(null, new int[] {10}, null);
 		assertEquals(1, s.getShape().length);
 		assertEquals(10, s.getShape()[0]);
 
 		is = s.getIterator();
 		for (int i = 0; is.hasNext(); i++) {
 			assertEquals(i, s.getElementLongAbs(is.index));
 		}
 
 		t = a.getSlice(new Slice(10));
 		assertEquals(1, t.getShape().length);
 		assertEquals(10, t.getShape()[0]);
 
 		it = t.getIterator();
 		for (int i = 0; it.hasNext(); i++) {
 			assertEquals(i, t.getElementLongAbs(it.index));
 		}
 
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(new int[] {9}, null, new int[] {-1});
 		assertEquals(1, s.getShape().length);
 		assertEquals(10, s.getShape()[0]);
 
 		is = s.getIterator();
 		for (int i = 9; is.hasNext(); i--) {
 			assertEquals(i, s.getElementLongAbs(is.index));
 		}
 
 		t = a.getSlice(new Slice(9, null, -1));
 		assertEquals(1, t.getShape().length);
 		assertEquals(10, t.getShape()[0]);
 
 		it = t.getIterator();
 		for (int i = 9; it.hasNext(); i--) {
 			assertEquals(i, t.getElementLongAbs(it.index));
 		}
 
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(new int[] {2}, new int[] {10}, null);
 		t = a.getSlice(new Slice(2, 10));
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(new int[] {2}, new int[] {10}, new int[] {3});
 		t = a.getSlice(new Slice(2, 10, 3));
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		try {
 			t = a.getSlice(new Slice(2000));
 			fail("No exception thrown");
 		} catch (IllegalArgumentException iae) {
 			// passed
 		} catch (Exception e) {
 			fail("Wrong exception type passed, this should give an IllegalArgumentException");
 		}
 
 		try {
 			t = a.getSlice(new Slice(12, 10, 3));
 			fail("No exception thrown");
 		} catch (IllegalArgumentException iae) {
 			// passed
 		} catch (Exception e) {
 			fail("Wrong exception type passed, this should give an IllegalArgumentException");
 		}
 
 		try {
 			t = a.getSlice(new Slice(2, 10, -3));
 			fail("No exception thrown");
 		} catch (IllegalArgumentException iae) {
 			// passed
 		} catch (Exception e) {
 			fail("Wrong exception type passed, this should give an IllegalArgumentException");
 		}
 
 		a.setShape(10, 10, 10);
 
 		s = a.getSlice(null, null, null);
 		t = a.getSlice();
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(null, null, null);
 		Slice[] slice = null;
 		t = a.getSlice(slice);
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(null, new int[] {8, 10, 10}, null);
 		t = a.getSlice(new Slice(8));
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(null, new int[] {8, 3, 10}, null);
 		t = a.getSlice(new Slice(8), new Slice(3));
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 
 		s = a.getSlice(null, new int[] {8, 10, 3}, null);
 		t = a.getSlice(new Slice(8), null, new Slice(3));
 		is = s.getIterator();
 		it = t.getIterator();
 		while (is.hasNext() && it.hasNext()) {
 			assertEquals(s.getElementLongAbs(is.index), t.getElementLongAbs(it.index));
 		}
 }
 
 	@Test
 	public void test1DErrors() {
 		
 		// test 1D errors for single value
 		AbstractDataset a = AbstractDataset.arange(100, AbstractDataset.INT32);
 		a.setError(5);
 		
 		assertEquals(5.0, a.getErrorDouble(0), 0.001);
 		assertEquals(5.0, a.getErrorDouble(50), 0.001);
 		assertEquals(5.0, a.getErrorDouble(99), 0.001);
 		
 		// now for pulling out the full error array
 		AbstractDataset error = a.getError();
 		
 		// check compatibility
 		try {
 			AbstractDataset.checkCompatibility(a, error);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 		
 		assertEquals(5.0, error.getDouble(0), 0.001);
 		assertEquals(5.0, error.getDouble(50), 0.001);
 		assertEquals(5.0, error.getDouble(99), 0.001);
 		
 		// Now set the error as a whole array
 		a.setError(Maths.multiply(error, 2));
 		
 		assertEquals(10.0, a.getErrorDouble(0), 0.001);
 		assertEquals(10.0, a.getErrorDouble(50), 0.001);
 		assertEquals(10.0, a.getErrorDouble(99), 0.001);
 		
 		// test pulling the error out again, to make sure its correct
 		AbstractDataset error2 = a.getError();
 		
 		// check compatibility
 		try {
 			AbstractDataset.checkCompatibility(a, error2);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 		
 		assertEquals(10.0, error2.getDouble(0), 0.001);
 		assertEquals(10.0, error2.getDouble(50), 0.001);
 		assertEquals(10.0, error2.getDouble(99), 0.001);
 	}
 	
 	
 	@Test
 	public void test2DErrors() {
 		
 		// test 1D errors for single value
 		AbstractDataset a = AbstractDataset.zeros(new int[] {100,100}, AbstractDataset.INT32);
 		a.setError(5);
 		
 		assertEquals(5.0, a.getErrorDouble(0,0), 0.001);
 		assertEquals(5.0, a.getErrorDouble(50,50), 0.001);
 		assertEquals(5.0, a.getErrorDouble(99,99), 0.001);
 		
 		// now for pulling out the full error array
 		AbstractDataset error = a.getError();
 		
 		// check compatibility
 		try {
 			AbstractDataset.checkCompatibility(a, error);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 		
 		assertEquals(5.0, error.getDouble(0,0), 0.001);
 		assertEquals(5.0, error.getDouble(50,50), 0.001);
 		assertEquals(5.0, error.getDouble(99,99), 0.001);
 		
 		// Now set the error as a whole array
 		a.setError(Maths.multiply(error, 2));
 		
 		assertEquals(10.0, a.getErrorDouble(0,0), 0.001);
 		assertEquals(10.0, a.getErrorDouble(50,50), 0.001);
 		assertEquals(10.0, a.getErrorDouble(99,99), 0.001);
 		
 		// test pulling the error out again, to make sure its correct
 		AbstractDataset error2 = a.getError();
 		
 		// check compatibility
 		try {
 			AbstractDataset.checkCompatibility(a, error2);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 		
 		assertEquals(10.0, error2.getDouble(0,0), 0.001);
 		assertEquals(10.0, error2.getDouble(50,50), 0.001);
 		assertEquals(10.0, error2.getDouble(99,99), 0.001);
 	}
 	
 	
 	@Test
 	public void testInternalErrors() {
 		
 		// test 1D errors for single value
 		AbstractDataset a = AbstractDataset.arange(100, AbstractDataset.INT32);
 		a.setError(5);
 		
 		// should be squared
 		assertEquals(25.0, a.errorValue.doubleValue(), 0.001);
 		
 		// now for pulling out the full error array
 		AbstractDataset error = a.getError();
 	
 		a.setError(Maths.multiply(error, 2));
 		
 		// should also be squared
 		assertEquals(100.0, a.errorData.getDouble(0), 0.001);
 		assertEquals(100.0, a.errorData.getDouble(50), 0.001);
 		assertEquals(100.0, a.errorData.getDouble(99), 0.001);	
 	}
 
 	@Test
 	public void testScalarDatasets() {
 		AbstractDataset a;
 		a = DoubleDataset.ones();
 		assertEquals("Rank", 0, a.getRank());
 		assertEquals("Shape", 0, a.getShape().length);
 		assertEquals("Value", 1.0, a.getObject());
 		assertEquals("Value", true, a.equals(new Double(1.0)));
 
 		a = AbstractDataset.zeros(new int[] {}, AbstractDataset.INT16);
 		assertEquals("Rank", 0, a.getRank());
 		assertEquals("Shape", 0, a.getShape().length);
 		assertEquals("Value", (short) 0, a.getObject());
 
 		a = AbstractDataset.array(new Complex(1.0, -0.5));
 		assertEquals("Rank", 0, a.getRank());
 		assertEquals("Shape", 0, a.getShape().length);
 		assertEquals("Value", new Complex(1.0, -0.5), a.getObject());
 	}
 }
