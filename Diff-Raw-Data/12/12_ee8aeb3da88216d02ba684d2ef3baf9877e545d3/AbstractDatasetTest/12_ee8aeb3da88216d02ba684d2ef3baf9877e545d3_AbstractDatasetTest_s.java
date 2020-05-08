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
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.HashSet;
 
 import org.apache.commons.math3.complex.Complex;
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.TestUtils;
 
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
 		assertTrue(AbstractDataset.areShapesCompatible(new int[] {}, new int[] {}, 0));
 		assertTrue(AbstractDataset.areShapesCompatible(new int[] {2}, new int[] {3}, 0));
 		assertFalse(AbstractDataset.areShapesCompatible(new int[] {2,4}, new int[] {3,4}, 1));
 		assertTrue(AbstractDataset.areShapesCompatible(new int[] {2,4}, new int[] {3,4}, 0));
 //		assertTrue(AbstractDataset.areShapesCompatible(new int[] {}, new int[] {}));
 	}
 
 	@Test
 	public void testMaxMin() {
 		AbstractDataset a = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		a.setShape(3,4);
 		assertEquals("Max", 11, a.max().doubleValue(), 1e-6);
 		assertEquals("Max 0", AbstractDataset.array(new double[] {8,9,10,11}), a.max(0));
 		assertEquals("Max 1", AbstractDataset.array(new double[] {3,7,11}), a.max(1));
 		assertEquals("Max arg", 11, a.argMax());
 		assertEquals("Max arg 0 ", AbstractDataset.array(new int[] {2,2,2,2}), a.argMax(0));
 		assertEquals("Max arg 1 ", AbstractDataset.array(new int[] {3,3,3}), a.argMax(1));
 		a.set(Double.NaN, 1, 0);
 		System.out.println(a.toString(true));
 		assertTrue("Max", Double.isNaN(a.max().doubleValue()));
 		assertTrue("Max", !Double.isNaN(a.max(true).doubleValue()));
 		assertTrue("Max 0", equalsWithNaNs(AbstractDataset.array(new double[] {Double.NaN,9,10,11}), a.max(0)));
 		assertTrue("Max 1", equalsWithNaNs(AbstractDataset.array(new double[] {3,Double.NaN,11}), a.max(1)));
 		assertEquals("Max arg", 4, a.argMax());
 		assertEquals("Max arg 0 ", AbstractDataset.array(new int[] {1,2,2,2}), a.argMax(0));
 		assertEquals("Max arg 1 ", AbstractDataset.array(new int[] {3,0,3}), a.argMax(1));
 		assertEquals("Max", 11, a.max(true).doubleValue(), 1e-6);
 		assertEquals("Max 0", AbstractDataset.array(new double[] {8,9,10,11}), a.max(true,0));
 		assertEquals("Max 1", AbstractDataset.array(new double[] {3,7,11}), a.max(true,1));
 		assertEquals("Max arg", 11, a.argMax(true));
 		assertEquals("Max arg 0 ", AbstractDataset.array(new int[] {2,2,2,2}), a.argMax(true, 0));
 		assertEquals("Max arg 1 ", AbstractDataset.array(new int[] {3,3,3}), a.argMax(true, 1));
 
 		a.set(Double.NEGATIVE_INFINITY, 1, 1);
 		System.out.println(a.toString(true));
 		assertTrue("Max", Double.isNaN(a.max().doubleValue()));
 		assertTrue("Max", !Double.isNaN(a.max(true).doubleValue()));
 		assertTrue("Max", Double.isNaN(a.max(false, true).doubleValue()));
 		assertTrue("Max", !Double.isNaN(a.max(true, false).doubleValue()));
 		assertEquals("Max", 11, a.max(true).doubleValue(), 1e-6);
 		assertTrue("+Max", Double.isNaN(a.positiveMax(false).doubleValue()));
 		assertTrue("+Max", Double.isNaN(a.positiveMax(false, true).doubleValue()));
 		assertTrue("+Max", !Double.isNaN(a.positiveMax(true, false).doubleValue()));
 		assertEquals("+Max", 11, a.positiveMax(true).doubleValue(), 1e-6);
 		assertTrue("Min", Double.isNaN(a.min().doubleValue()));
 		assertTrue("Min", !Double.isNaN(a.min(true).doubleValue()));
 		assertTrue("Min", Double.isNaN(a.min(false, true).doubleValue()));
 		assertTrue("Min", !Double.isNaN(a.min(true, false).doubleValue()));
 		assertTrue("Min", !Double.isInfinite(a.min().doubleValue()));
 		assertTrue("Min", !Double.isInfinite(a.min(true).doubleValue()));
 		assertTrue("Min", !Double.isInfinite(a.min(false, true).doubleValue()));
 		assertTrue("Min", Double.isInfinite(a.min(true, false).doubleValue()));
 		assertEquals("Min", 0, a.min(true).doubleValue(), 1e-6);
 		assertTrue("+Min", !Double.isNaN(a.positiveMin(true).doubleValue()));
 		assertTrue("+Min", Double.isNaN(a.positiveMin(false).doubleValue()));
 		assertTrue("+Min", Double.isNaN(a.positiveMin(false, true).doubleValue()));
 		assertTrue("+Min", !Double.isNaN(a.positiveMin(true, false).doubleValue()));
 		assertTrue("+Min", !Double.isInfinite(a.positiveMin(true).doubleValue()));
 		assertTrue("+Min", !Double.isInfinite(a.positiveMin(false).doubleValue()));
 		assertTrue("+Min", !Double.isInfinite(a.positiveMin(true, false).doubleValue()));
 		assertTrue("+Min", !Double.isInfinite(a.positiveMin(false, true).doubleValue()));
 		assertEquals("+Min", 1, a.positiveMin(true).doubleValue(), 1e-6);
 
 		// test other code path
 		AbstractDataset b = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		b.setShape(3,4);
 		b.mean(); // trigger summary stats calculation
 		assertEquals("Max", 11, b.max().doubleValue(), 1e-6);
 		assertEquals("Max arg", 11, b.argMax());
 		b.set(Double.NaN, 1, 0);
 		b.mean(); // trigger summary stats calculation
 		assertTrue("Max", Double.isNaN(b.max().doubleValue()));
 		assertEquals("Max arg", 4, b.argMax());
 		b.mean(true);
 		assertEquals("Max", 11, b.max(true).doubleValue(), 1e-6);
 		assertEquals("Max arg", 11, b.argMax(true));
 	}
 
 	@Test
 	public void testHash() {
 		AbstractDataset a = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		a.setShape(3,4);
 		AbstractDataset b = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		b.setShape(3,4);
 
 		b.mean(); // trigger other code path
 		assertEquals("Hash code", a.hashCode(), b.hashCode());
 
 		a.set(Double.POSITIVE_INFINITY, 1, 0);
 		b.set(Double.POSITIVE_INFINITY, 1, 0);
 		b.mean(); // trigger other code path
 		assertEquals("Hash code", a.hashCode(), b.hashCode());
 
 		a.set(Double.NaN, 0, 1);
 		b.set(Double.NaN, 0, 1);
 		b.mean(); // trigger other code path
 		assertEquals("Hash code", a.hashCode(), b.hashCode());
 	}
 
 	private static boolean equalsWithNaNs(AbstractDataset a, AbstractDataset b) {
 		if (a.equals(b))
 			return true;
 
 		IndexIterator ita = a.getIterator();
 		IndexIterator itb = b.getIterator();
 		while (ita.hasNext() && itb.hasNext()) {
 			final double av = a.getElementDoubleAbs(ita.index);
 			final double bv = b.getElementDoubleAbs(itb.index);
 			if (Double.isNaN(av)) {
 				if (!Double.isNaN(bv))
 					return false;
 			} else {
 				if (av != bv)
 					return false;
 			}
 		}
 		return true;
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
 		a.set(Double.NaN, 0);
 		a.setShape(3, 4);
 		a.sort(-1);
 		System.out.println(a);
 		assertEquals("First element", 1, a.getDouble(0,0), 1e-6);
 		assertTrue("0,3 element", Double.isNaN(a.getDouble(0,3)));
 		assertEquals("Final element", 11, a.getDouble(2,3), 1e-6);
 
 		a.sort(0);
 		System.out.println(a);
 		assertEquals("First element", 1, a.getDouble(0,0), 1e-6);
 		assertEquals("0,3 element", 7, a.getDouble(0,3), 1e-6);
 		assertTrue("Final element", Double.isNaN(a.getDouble(2,3)));
 
 		a.set(12, 0);
 		a.sort(null);
 		System.out.println(a);
 		assertEquals("First element", 2, a.getDouble(0), 1e-6);
 		assertEquals("2,2 element", 12, a.getDouble(2,2), 1e-6);
 		assertTrue("Final element", Double.isNaN(a.getDouble(2,3)));
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
 
 		int[] os, ns;
 		os = new int[] { 2, 1, 5 };
 		ns = AbstractDataset.squeezeShape(os, false);
 		assertEquals(2, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(5, ns[1]);
 		ns = AbstractDataset.squeezeShape(os, true);
 		assertEquals(3, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(1, ns[1]);
 		assertEquals(5, ns[2]);
 
 		os = new int[] { 2, 1, 5, 1 };
 		ns = AbstractDataset.squeezeShape(os, false);
 		assertEquals(2, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(5, ns[1]);
 		ns = AbstractDataset.squeezeShape(os, true);
 		assertEquals(3, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(1, ns[1]);
 		assertEquals(5, ns[2]);
 
 		os = new int[] { 1, 2, 1, 5 };
 		ns = AbstractDataset.squeezeShape(os, false);
 		assertEquals(2, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(5, ns[1]);
 		ns = AbstractDataset.squeezeShape(os, true);
 		assertEquals(3, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(1, ns[1]);
 		assertEquals(5, ns[2]);
 
 		os = new int[] { 1, 2, 1, 5, 1 };
 		ns = AbstractDataset.squeezeShape(os, false);
 		assertEquals(2, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(5, ns[1]);
 		ns = AbstractDataset.squeezeShape(os, true);
 		assertEquals(3, ns.length);
 		assertEquals(2, ns[0]);
 		assertEquals(1, ns[1]);
 		assertEquals(5, ns[2]);
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
 		int[][] blocks = {{1024,1}, {256,4}, {64,16}, {32,32}, {16,64}, {4, 256}, {1,1024}};
 //		int[][] blocks = {{1024,64}, {256,64}, {64,64}, {32,64}, {16,64}, {4, 64}, {1,64}};
 
 		int[][] shapes = { {1024, 2048}, {2048, 2048}, {2048, 1024} };
 
 		for (int b = 0; b < blocks.length; b++) {
 			for (int s = 0; s < shapes.length; s++) {
 				for (int n = 0; n < 3; n++)
 					runTile(blocks[b][0], blocks[b][1], shapes[s][0], shapes[s][1]);
 			}
 		}
 	}
 
 	private void runTile(final int srows, final int scols, final int rows, final int cols) throws Exception {
 		AbstractDataset a = AbstractDataset.arange(srows*scols, AbstractDataset.FLOAT64).reshape(srows, scols);
 
 		long start, end;
 
 		System.out.printf("Tile %sx%d Block %dx%d: ", rows, cols, srows, scols);
 
 		final int nrows = rows/srows;
 		final int ncols = cols/scols;
 
 		start = System.currentTimeMillis();
 		DoubleDataset b = new DoubleDataset(rows, cols);
 		final double[] da = (double[]) a.getBuffer();
 		final double[] db = b.getData();
 		if (scols == 1) {
 			for (int i = 0; i < db.length; i++) {
 				db[i] = da[(i / cols) % srows];
 			}
 		} else if (srows == 1) {
 			for (int i = 0; i < db.length; i++) {
 				db[i] = da[i % scols];
 			}
 		} else {
 			for (int i = 0; i < db.length; i++) {
 				db[i] = da[((i / cols) % srows) * scols + i % scols];
 			}
 		}
 		end = System.currentTimeMillis();
 		long diff1 = end - start;
 		System.out.printf("array = %d ms, ", diff1);
 
 		start = System.currentTimeMillis();
 		final AbstractDataset tiled = DatasetUtils.tile(a, nrows, ncols);
 		end = System.currentTimeMillis();
 		long diff2 = end - start;
 		System.out.printf("tile = %d ms\n", diff2);
 
 		assertEquals(rows, tiled.getShape()[0]);
 		assertEquals(cols, tiled.getShape()[1]);
 		if (!tiled.equals(b))
 			throw new Exception("Datasets not equal!");
 
 		if (diff2 > (diff1 * 20))
 			throw new Exception("Creation of tile took more than 20x as long as array creation of same size! (It took "
 					+ diff2 + ")");
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
 		ds.set(-2, 1, 2);
 		assertEquals(-2., ds.getDouble(1, 2), 1e-6);
 		assertEquals(5., ta.getDouble(2, 1), 1e-6);
 		ta = ds.getTransposedView(); // test view has changed
 		assertEquals(-2., ta.getDouble(2, 1), 1e-6);
 
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
 	 * Tests for resize method
 	 */
 	@Test
 	public void testResize() {
 		int size = 6;
 		AbstractDataset ds = AbstractDataset.arange(size, AbstractDataset.FLOAT64);
 		DoubleDataset tf;
 		IndexIterator it;
 
 		tf = (DoubleDataset) DatasetUtils.resize(ds, 3);
 		assertArrayEquals(new int[] {3}, tf.getShape());
 		it = tf.getIterator();
 		while (it.hasNext()) {
 			assertEquals(it.index % size, tf.getElementDoubleAbs(it.index), 1e-6);
 		}
 
 		tf = (DoubleDataset) DatasetUtils.resize(ds, 8);
 		assertArrayEquals(new int[] {8}, tf.getShape());
 		it = tf.getIterator();
 		while (it.hasNext()) {
 			assertEquals(it.index % size, tf.getElementDoubleAbs(it.index), 1e-6);
 		}
 
 		tf = (DoubleDataset) DatasetUtils.resize(ds, 3, 4);
 		assertArrayEquals(new int[] {3, 4}, tf.getShape());
 		it = tf.getIterator();
 		while (it.hasNext()) {
 			assertEquals(it.index % size, tf.getElementDoubleAbs(it.index), 1e-6);
 		}
 
 		ds.setShape(2,3);
 
 		tf = (DoubleDataset) DatasetUtils.resize(ds, 3);
 		assertArrayEquals(new int[] {3}, tf.getShape());
 		it = tf.getIterator();
 		while (it.hasNext()) {
 			assertEquals(it.index % size, tf.getElementDoubleAbs(it.index), 1e-6);
 		}
 
 		tf = (DoubleDataset) DatasetUtils.resize(ds, 8);
 		assertArrayEquals(new int[] {8}, tf.getShape());
 		it = tf.getIterator();
 		while (it.hasNext()) {
 			assertEquals(it.index % size, tf.getElementDoubleAbs(it.index), 1e-6);
 		}
 
 		tf = (DoubleDataset) DatasetUtils.resize(ds, 3, 4);
 		assertArrayEquals(new int[] {3, 4}, tf.getShape());
 		it = tf.getIterator();
 		while (it.hasNext()) {
 			assertEquals(it.index % size, tf.getElementDoubleAbs(it.index), 1e-6);
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
 	public void testSlicingViews() {
 		AbstractDataset a, b, c;
 		a = AbstractDataset.arange(32, AbstractDataset.FLOAT64).reshape(4, 8);
 		checkSliceView(a, new int[] {0, 1}, new int[] {3, 5}, new int[] {1, 2});
 		checkSliceView(a, new int[] {1, -1}, new int[] {-1, 3}, new int[] {1, -2});
 
 		a = AbstractDataset.arange(60, AbstractDataset.FLOAT64).reshape(6, 10);
 		b = checkSliceView(a, new int[] {0, 1}, new int[] {6, 8}, new int[] {1, 2}); // 6x4
 		c = b.getSliceView(new int[] {0, 1}, new int[] {1, 4}, null);
 		c.setShape(3);
 		checkSliceView(b, new int[] {1, 0}, new int[] {5, 3}, new int[] {2, 1});
 		checkSliceView(b, new int[] {1, -1}, new int[] {5, 2}, new int[] {2, -1});
 		c = a.getSlice(new int[] {0, 1}, new int[] {6, 8}, new int[] {1, 2});
 		b.setShape(2,3,4);
 		c.setShape(2,3,4);
 		assertEquals(c, b);
 		b.setShape(6,4);
 		b.setShape(6,2,2);
 		c.setShape(6,2,2);
 		assertEquals(c, b);
 		b.setShape(6,4);
 		try {
 			b.setShape(2,12);
 			fail("Should have raised an exception");
 		} catch (IllegalArgumentException iae) {
 			// expected
 		} catch (Exception e) {
 			fail("Unexpected exception: " + e);
 		}
 
 		b = checkSliceView(a, new int[] {1, -1}, new int[] {-1, 2}, new int[] {1, -2}); // 4x4
 		checkSliceView(b, new int[] {1, 0}, new int[] {4, 3}, new int[] {2, 1});
 		checkSliceView(b, new int[] {1, -1}, new int[] {4, 2}, new int[] {2, -1});
 
 		b = checkSliceView(a, new int[] {0, 1}, new int[] {1, 8}, new int[] {1, 2}); // 1x4
 		b = checkSliceView(a, new int[] {0, 1}, new int[] {6, 2}, new int[] {1, 2}); // 6x1
 	}
 
 	private AbstractDataset checkSliceView(AbstractDataset a, int[] start, int[] stop, int[] step) {
 		AbstractDataset s = a.getSliceView(start, stop, step).squeeze();
 		AbstractDataset t = a.getSlice(start, stop, step).squeeze();
 		assertArrayEquals(t.getShape(), s.getShape());
 		assertEquals(t.toString(true), t, s);
 		IndexIterator iter = s.getIterator(true);
 		int[] pos = iter.getPos();
 		while (iter.hasNext()) {
 			assertEquals(iter.index, s.get1DIndex(pos));
 			int[] p = s.getNDPosition(iter.index);
 			assertArrayEquals(Arrays.toString(pos) + " : " + Arrays.toString(p), pos, p);
 		}
 		return s;
 	}
 
 	@Test
 	public void testSliceStrings() {
 		String s;
 		s = AbstractDataset.createSliceString(new int[] {3}, null, null, null);
 		assertEquals(":", s);
 
 		s = AbstractDataset.createSliceString(new int[] {3}, null, null, new int[] {1});
 		assertEquals(":", s);
 
 		s = AbstractDataset.createSliceString(new int[] {3}, null, new int[] {2}, new int[] {1});
 		assertEquals(":2", s);
 
 		s = AbstractDataset.createSliceString(new int[] {4}, new int[] {1}, new int[] {3}, new int[] {1});
 		assertEquals("1:3", s);
 
 		s = AbstractDataset.createSliceString(new int[] {4}, new int[] {1}, new int[] {2}, new int[] {1});
 		assertEquals("1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {4}, new int[] {1}, new int[] {3}, new int[] {2});
 		assertEquals("1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, null, null, new int[] {2});
 		assertEquals("::2", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {1}, new int[] {4}, new int[] {2});
 		assertEquals("1:4:2", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {1}, new int[] {5}, new int[] {2});
 		assertEquals("1::2", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {1}, new int[] {3}, new int[] {2});
 		assertEquals("1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {3}, null, null, new int[] {-1});
 		assertEquals("::-1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {3}, new int[] {1}, new int[] {-1});
 		assertEquals("3:1:-1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {4}, new int[] {1}, new int[] {-1});
 		assertEquals(":1:-1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {3}, new int[] {0}, new int[] {-1});
 		assertEquals("3:0:-1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {3}, new int[] {-1}, new int[] {-1});
 		assertEquals("3::-1", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {3}, new int[] {2}, new int[] {-1});
 		assertEquals("3", s);
 
 		s = AbstractDataset.createSliceString(new int[] {5}, new int[] {3}, new int[] {1}, new int[] {-2});
 		assertEquals("3", s);
 
 		s = AbstractDataset.createSliceString(new int[] {3, 2}, null, null, null);
 		assertEquals(":,:", s);
 	}
 
 	@Test
 	public void testCheckSlice() {
 		int[] step;
 		int[] lstart;
 		int[] lstop;
 		int[] shape;
 
 		step = new int[] {};
 		lstart = new int[] {};
 		lstop = new int[] {};
 		shape = AbstractDataset.checkSlice(new int[] {}, null, null, lstart, lstop, step);
 		assertEquals(0, shape.length);
 		Assert.assertArrayEquals(new int[] {}, shape);
 
 		try {
 			shape = AbstractDataset.checkSlice(new int[] {1}, null, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			shape = AbstractDataset.checkSlice(new int[] {1}, null, null, lstart, lstop, new int[] {1});
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			shape = AbstractDataset.checkSlice(new int[] {3}, null, null, new int[1], new int[1], new int[] {0});
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		step = new int[] {2};
 		try {
 			shape = AbstractDataset.checkSlice(new int[] {2, 3}, null, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		lstart = new int[1];
 		lstop = new int[1];
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {4}, shape);
 		Assert.assertArrayEquals(new int[] {0}, lstart);
 		Assert.assertArrayEquals(new int[] {7}, lstop);
 
 		lstart[0] = 0;
 		shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {4}, shape);
 		Assert.assertArrayEquals(new int[] {0}, lstart);
 		Assert.assertArrayEquals(new int[] {7}, lstop);
 
 		lstart[0] = 3;
 		shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {2}, shape);
 		Assert.assertArrayEquals(new int[] {3}, lstart);
 		Assert.assertArrayEquals(new int[] {7}, lstop);
 
 		lstart[0] = -4;
 		shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {2}, shape);
 		Assert.assertArrayEquals(new int[] {3}, lstart);
 		Assert.assertArrayEquals(new int[] {7}, lstop);
 
 		try {
 			lstart[0] = -8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstart[0] = 7;
 			shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstart[0] = 8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		lstop[0] = 7;
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {4}, shape);
 		Assert.assertArrayEquals(new int[] {0}, lstart);
 		Assert.assertArrayEquals(new int[] {7}, lstop);
 
 		lstop[0] = -3;
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {2}, shape);
 		Assert.assertArrayEquals(new int[] {0}, lstart);
 		Assert.assertArrayEquals(new int[] {4}, lstop);
 
 		try {
 			lstop[0] = 0;
 			shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstop[0] = -8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstop[0] = 8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 
 		step = new int[] {-2};
 		try {
 			shape = AbstractDataset.checkSlice(new int[] {2, 3}, null, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {4}, shape);
 		Assert.assertArrayEquals(new int[] {6}, lstart);
 		Assert.assertArrayEquals(new int[] {-1}, lstop);
 
 		lstart[0] = 0;
 		shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {1}, shape);
 		Assert.assertArrayEquals(new int[] {0}, lstart);
 		Assert.assertArrayEquals(new int[] {-1}, lstop);
 
 		lstart[0] = 3;
 		shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {2}, shape);
 		Assert.assertArrayEquals(new int[] {3}, lstart);
 		Assert.assertArrayEquals(new int[] {-1}, lstop);
 
 		lstart[0] = -4;
 		shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {2}, shape);
 		Assert.assertArrayEquals(new int[] {3}, lstart);
 		Assert.assertArrayEquals(new int[] {-1}, lstop);
 
 		try {
 			lstart[0] = -8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstart[0] = 7;
 			shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstart[0] = 8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, lstart, null, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		lstop[0] = -1;
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {4}, shape);
 		Assert.assertArrayEquals(new int[] {6}, lstart);
 		Assert.assertArrayEquals(new int[] {-1}, lstop);
 
 		lstop[0] = -3;
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {1}, shape);
 		Assert.assertArrayEquals(new int[] {6}, lstart);
 		Assert.assertArrayEquals(new int[] {4}, lstop);
 
 		lstop[0] = -8;
 		shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 		Assert.assertArrayEquals(new int[] {4}, shape);
 		Assert.assertArrayEquals(new int[] {6}, lstart);
 		Assert.assertArrayEquals(new int[] {-1}, lstop);
 
 		try {
 			lstop[0] = 6;
 			shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 
 		try {
 			lstop[0] = 8;
 			shape = AbstractDataset.checkSlice(new int[] {7}, null, lstop, lstart, lstop, step);
 			fail();
 		} catch (IllegalArgumentException e) {
 			System.out.println("As expected: " + e);
 		}
 	}
 	
 	@Test
 	public void test1DErrors() {
 		
 		// test 1D errors for single value
 		AbstractDataset a = AbstractDataset.arange(100, AbstractDataset.INT32);
 		a.setError(5);
 		
 		assertEquals(5.0, a.getError(0), 0.001);
 		assertEquals(5.0, a.getError(50), 0.001);
 		assertEquals(5.0, a.getError(99), 0.001);
 
 		assertTrue(a.hasErrors());
 
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
 		
 		assertEquals(10.0, a.getError(0), 0.001);
 		assertEquals(10.0, a.getError(50), 0.001);
 		assertEquals(10.0, a.getError(99), 0.001);
 		
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
 		
 		assertEquals(5.0, a.getError(0,0), 0.001);
 		assertEquals(5.0, a.getError(50,50), 0.001);
 		assertEquals(5.0, a.getError(99,99), 0.001);
 
 		assertTrue(a.hasErrors());
 
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
 		
 		assertEquals(10.0, a.getError(0,0), 0.001);
 		assertEquals(10.0, a.getError(50,50), 0.001);
 		assertEquals(10.0, a.getError(99,99), 0.001);
 		
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
 	public void testSetErrorBuffer() {
 		
 		AbstractDataset a = AbstractDataset.zeros(new int[] {100,100}, AbstractDataset.INT32);
 		AbstractDataset err = DatasetUtils.linSpace(0, a.getSize() - 1, a.getSize(), AbstractDataset.FLOAT64);
 		err.setShape(a.getShape());
 		
 		a.setErrorBuffer(null);
 		assertFalse(a.hasErrors());
 		
 		a.setErrorBuffer(25.0);
 		
 		assertEquals(5.0, a.getError(0,0), 0.001);
 		assertEquals(5.0, a.getError(50,50), 0.001);
 		assertEquals(5.0, a.getError(99,99), 0.001);
 
 		assertTrue(a.hasErrors());
 		
 		// now for pulling out the full error array and check compatibility
 		AbstractDataset error = a.getError();
 		try {
 			AbstractDataset.checkCompatibility(a, error);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 				
 		a.setErrorBuffer(err);
 
 		assertEquals(0.0, a.getError(0,0), 0.001);
 		assertEquals(Math.sqrt(50.0 + 100*50.0), a.getError(50,50), 0.001);
 		assertEquals(Math.sqrt(99.0 + 100*99.0), a.getError(99,99), 0.001);
 
 		assertTrue(a.hasErrors());
 		
 		// now for pulling out the full error array and check compatibility
 		error = a.getError();
 		try {
 			AbstractDataset.checkCompatibility(a, error);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 		
 		a.setErrorBuffer(err.getBuffer());
 
 		assertEquals(0.0, a.getError(0,0), 0.001);
 		assertEquals(Math.sqrt(35.0 + 100*25.0), a.getError(25,35), 0.001);
 		assertEquals(Math.sqrt(99.0 + 100*99.0), a.getError(99,99), 0.001);
 
 		assertTrue(a.hasErrors());
 		
 		// now for pulling out the full error array and check compatibility
 		error = a.getError();
 		try {
 			AbstractDataset.checkCompatibility(a, error);
 		} catch (Exception e) {
 			fail("Error shape is not the same as input datasets");
 		}
 	}
 	
 	@Test
 	public void testInternalErrors() {
 		
 		// test 1D errors for single value
 		AbstractDataset a = AbstractDataset.arange(100, AbstractDataset.INT32);
 		a.setError(5);
 		
 		// should be squared
 		Number ne = (Number) a.errorData;
 		assertEquals(25.0, ne.doubleValue(), 0.001);
 		
 		// now for pulling out the full error array
 		AbstractDataset error = a.getError();
 	
 		a.setError(Maths.multiply(error, 2));
 		
 		// should also be squared
 		AbstractDataset ae = (AbstractDataset) a.errorData;
 		assertEquals(100.0, ae.getDouble(0), 0.001);
 		assertEquals(100.0, ae.getDouble(50), 0.001);
 		assertEquals(100.0, ae.getDouble(99), 0.001);	
 	}
 
 	@Test
 	public void testZeroRankDatasets() {
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
 
 		a = AbstractDataset.array(1.f);
 		assertEquals("Rank", 0, a.getRank());
 		assertEquals("Shape", 0, a.getShape().length);
 		assertEquals("Value", 1.f, a.getObject());
 	}
 
 	@Test
 	public void testConcatenate() {
 		AbstractDataset a, b, c, d;
 		a = AbstractDataset.arange(6, AbstractDataset.FLOAT64);
 		b = AbstractDataset.arange(6, 8, 1, AbstractDataset.FLOAT64);
 		c = DatasetUtils.concatenate(new IDataset[] {a, b}, 0);
 		d = AbstractDataset.arange(8, AbstractDataset.FLOAT64);
 		assertEquals("Rank", 1, c.getRank());
 		assertTrue("Dataset", c.equals(d));
 
 		a = AbstractDataset.arange(6, AbstractDataset.FLOAT64).reshape(3,2);
 		b = AbstractDataset.arange(6, 8, 1, AbstractDataset.FLOAT64).reshape(1,2);
 		c = DatasetUtils.concatenate(new IDataset[] {a, b}, 0);
 		d = AbstractDataset.arange(8, AbstractDataset.FLOAT64).reshape(4,2);
 		assertEquals("Rank", 2, c.getRank());
 		assertTrue("Dataset", c.equals(d));
 
 		a.setShape(2,3);
 		b = AbstractDataset.arange(6, 9, 1, AbstractDataset.FLOAT64).reshape(1,3);
 		c = DatasetUtils.concatenate(new IDataset[] {a, b}, 0);
 		d = AbstractDataset.arange(9, AbstractDataset.FLOAT64).reshape(3,3);
 		assertEquals("Rank", 2, c.getRank());
 		assertTrue("Dataset", c.equals(d));
 
 		a = AbstractDataset.arange(2, AbstractDataset.FLOAT64).reshape(1,2);
 		b = AbstractDataset.arange(3, 5, 1, AbstractDataset.FLOAT64).reshape(1,2);
 		a = DatasetUtils.concatenate(new IDataset[] {a, b}, 0);
 		b = AbstractDataset.arange(2, 6, 3, AbstractDataset.FLOAT64).reshape(2,1);
 		c = DatasetUtils.concatenate(new IDataset[] {a, b}, 1);
 		d = AbstractDataset.arange(6, AbstractDataset.FLOAT64).reshape(2,3);
 		assertEquals("Rank", 2, c.getRank());
 		assertTrue("Dataset", c.equals(d));
 	}
 
 	@Test
 	public void testSum() {
 		AbstractDataset a = AbstractDataset.arange(1024*1024, AbstractDataset.INT32);
 
 		assertEquals("Typed sum", -524288, a.typedSum(AbstractDataset.INT32));
 
 		a = AbstractDataset.arange(12, AbstractDataset.FLOAT64);
 		a.setShape(3,4);
 		assertEquals("Sum", 11*6, ((Number) a.sum()).doubleValue(), 1e-6);
 		a.set(Double.NaN, 0,0);
 		assertTrue("Sum", Double.isNaN(((Number) a.sum()).doubleValue()));
 		assertEquals("Sum", 11*6, ((Number) a.sum(true)).doubleValue(), 1e-6);
 	}
 
 	@Test
 	public void testMakeFinite() {
 		AbstractDataset a = AbstractDataset.array(new double[] {0, Double.POSITIVE_INFINITY, Double.NaN, Double.NEGATIVE_INFINITY });
 		DatasetUtils.makeFinite(a);
 
 		assertTrue("Make finite", AbstractDataset.array(new double[] {0, Double.MAX_VALUE, 0, -Double.MAX_VALUE}).equals(a));
 	}
 
 	@Test
 	public void testCast() {
 		long[] udata = new long[] {0, 1, 127, 128, 255, 256, 32767, 32768, 65535, 65536, 2147483647L, 2147483648L, 4294967295L, 4294967296L};
 		AbstractDataset d = new LongDataset(udata);
 		AbstractDataset a;
 		a = AbstractDataset.array(DatasetUtils.cast(d, AbstractDataset.INT32), true);
 		assertEquals("", 0, a.getLong(13));
 		for (int i = 0; i < 13; i++)
 			assertEquals("", udata[i], a.getLong(i));
 
 		a = AbstractDataset.array(DatasetUtils.cast(d, AbstractDataset.INT16), true);
 		assertEquals("", 0, a.getLong(9));
 		for (int i = 0; i < 9; i++)
 			assertEquals("", udata[i], a.getLong(i));
 
 		a = AbstractDataset.array(DatasetUtils.cast(d, AbstractDataset.INT8), true);
 		assertEquals("", 0, a.getLong(5));
 		for (int i = 0; i < 5; i++)
 			assertEquals("", udata[i], a.getLong(i));
 	}
 
 	@Test
 	public void testRoll() {
 		AbstractDataset a = AbstractDataset.arange(10, AbstractDataset.INT32);
 
 		AbstractDataset r = DatasetUtils.roll(a, 2, null);
 
 		TestUtils.assertDatasetEquals(r, Maths.add(a, 10-2).iremainder(10), 1e-6, 1e-6);
 
 		r = DatasetUtils.roll(a, -2, null);
 		TestUtils.assertDatasetEquals(r, Maths.add(a, 10+2).iremainder(10), 1e-6, 1e-6);
 
 		a.setShape(2,5);
 		r = DatasetUtils.roll(a, 1, null);
 		TestUtils.assertDatasetEquals(r, Maths.add(a, 10-1).iremainder(10).reshape(2,5), 1e-6, 1e-6);
 
 		r = DatasetUtils.roll(a, 1, 0);
 		TestUtils.assertDatasetEquals(r, Maths.add(a, 5).iremainder(10).reshape(2,5), 1e-6, 1e-6);
 
 		r = DatasetUtils.roll(a, 1, 1);
 		TestUtils.assertDatasetEquals(r, new IntegerDataset(new int[] {4, 0, 1, 2, 3, 9, 5, 6, 7, 8}, 2,5), 1e-6, 1e-6);
 	}
 
 	@Test
 	public void testRollAxis() {
 		AbstractDataset a = AbstractDataset.ones(new int[] {3, 4, 5, 6}, AbstractDataset.INT8);
 		Assert.assertArrayEquals(new int[] {3, 6, 4, 5}, DatasetUtils.rollAxis(a, 3, 1).getShape());
 		Assert.assertArrayEquals(new int[] {5, 3, 4, 6}, DatasetUtils.rollAxis(a, 2, 0).getShape());
 		Assert.assertArrayEquals(new int[] {3, 5, 6, 4}, DatasetUtils.rollAxis(a, 1, 4).getShape());
 	}
 
 	@Test
 	public void testAppend() {
 		double[] x = { 0., 1., 2., 3., 4., 5. };
 		AbstractDataset d1 = DoubleDataset.arange(3.);
 		AbstractDataset d2 = DoubleDataset.arange(3., 6., 1.);
 		AbstractDataset d3 = DatasetUtils.append(d1, d2, 0);
 
 		for (int i = 0; i < x.length; i++) {
 			assertEquals("Append 1", x[i], d3.getDouble(i), 1e-8);
 		}
 
 		d1.setShape(1, 3);
 		d2.setShape(1, 3);
 		d3 = DatasetUtils.append(d1, d2, 0);
 		AbstractDataset d4 = new DoubleDataset(x);
 
 		d4.setShape(2, 3);
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < 3; j++) {
 				assertEquals("Append 2", d4.getDouble(i, j), d3.getDouble(i, j), 1e-8);
 			}
 		}
 
 		d3 = DatasetUtils.append(d1, d2, 1);
 		d4 = new DoubleDataset(x);
 		d4.setShape(1, 6);
 		for (int i = 0; i < 1; i++) {
 			for (int j = 0; j < 6; j++) {
 				assertEquals("Append 3", d4.getDouble(i, j), d3.getDouble(i, j), 1e-8);
 			}
 		}
 	}
 
 	public static void checkDatasets(IntegerDataset calc, IntegerDataset expected) {
 		IndexIterator at = calc.getIterator(true);
 		IndexIterator bt = expected.getIterator();
 		final int is = calc.getElementsPerItem();
 	
 		while (at.hasNext() && bt.hasNext()) {
 			for (int j = 0; j < is; j++) {
 				Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + "; " + j +
 						": ", expected.getAbs(at.index + j), calc.getAbs(bt.index + j));
 			}
 		}
 	}
 
 	public static void checkDatasets(BooleanDataset calc, BooleanDataset expected) {
 		IndexIterator at = calc.getIterator(true);
 		IndexIterator bt = expected.getIterator();
 		final int is = calc.getElementsPerItem();
 	
 		while (at.hasNext() && bt.hasNext()) {
 			for (int j = 0; j < is; j++) {
 				Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + "; " + j +
 						": ", expected.getAbs(at.index + j), calc.getAbs(bt.index + j));
 			}
 		}
 	}
 
 	public static void checkDatasets(DoubleDataset calc, DoubleDataset expected) {
 		IndexIterator at = calc.getIterator(true);
 		IndexIterator bt = expected.getIterator();
 		final int is = calc.getElementsPerItem();
 	
 		while (at.hasNext() && bt.hasNext()) {
 			for (int j = 0; j < is; j++) {
 				Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + "; " + j +
 						": ", expected.getAbs(at.index + j), calc.getAbs(bt.index + j), 1e-5);
 			}
 		}
 	}
 
 	public static void checkDatasets(AbstractDataset expected, AbstractDataset calc, double relTol, double absTol) {
 		int type = expected.getDtype();
 		Assert.assertEquals("Type", type, calc.getDtype());
 		Assert.assertEquals("Items", expected.getElementsPerItem(), calc.getElementsPerItem());
 		Assert.assertEquals("Size", expected.getSize(), calc.getSize());
 		Assert.assertArrayEquals("Shape", expected.getShape(), calc.getShape());
 		IndexIterator at = expected.getIterator(true);
 		IndexIterator bt = calc.getIterator();
 		final int is = calc.getElementsPerItem();
 
 		if (expected.elementClass().equals(Boolean.class)) {
 			while (at.hasNext() && bt.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					boolean e = expected.getElementBooleanAbs(at.index + j);
 					boolean c = calc.getElementBooleanAbs(bt.index + j);
 					Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + "; " + j +
 							": ", e, c);
 				}
 			}
 		} else if (expected.hasFloatingPointElements()) {
 			while (at.hasNext() && bt.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					double e = expected.getElementDoubleAbs(at.index + j);
 					double c = calc.getElementDoubleAbs(bt.index + j);
 					double t = Math.max(absTol, relTol*Math.max(Math.abs(e), Math.abs(c)));
 					Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + "; " + j +
 							": ", e, c, t);
 				}
 			}
 		} else if (type == AbstractDataset.STRING) {
 			StringDataset es = (StringDataset) expected;
 			StringDataset cs = (StringDataset) calc;
 
 			while (at.hasNext() && bt.hasNext()) {
 				Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + ": ",
 						es.getAbs(at.index), cs.getAbs(bt.index));
 			}
 		} else if (type == AbstractDataset.OBJECT) {
 			ObjectDataset eo = (ObjectDataset) expected;
 			ObjectDataset co = (ObjectDataset) calc;
 
 			while (at.hasNext() && bt.hasNext()) {
 				Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + ": ",
 						eo.getAbs(at.index), co.getAbs(bt.index));
 			}
 		} else {
 			while (at.hasNext() && bt.hasNext()) {
 				for (int j = 0; j < is; j++) {
 					long e = expected.getElementLongAbs(at.index + j);
 					long c = calc.getElementLongAbs(bt.index + j);
 					Assert.assertEquals("Value does not match at " + Arrays.toString(at.getPos()) + "; " + j +
 							": ", e, c);
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testSelect() {
 		DoubleDataset a = new DoubleDataset(new double[] { 0, 1, 3, 5, -7, -9 });
 		DoubleDataset b = new DoubleDataset(new double[] { 0.01, 1.2, 2.9, 5, -7.1, -9 });
 
 		AbstractDataset c = a.clone().reshape(2, 3);
 		BooleanDataset d = new BooleanDataset(new boolean[] {false, true, false, false, true, false}, 2, 3);
 
 		DoubleDataset e = (DoubleDataset) DatasetUtils.select(new BooleanDataset[] {d}, new Object[] {c}, -2);
 		checkDatasets(e, new DoubleDataset(new double[] {-2, 1, -2, -2, -7, -2}, 2, 3));
 
 		AbstractDataset f = b.clone().reshape(2, 3);
 		BooleanDataset g = new BooleanDataset(new boolean[] {false, true, true, false, false, false}, 2, 3);
 
 		e = (DoubleDataset) DatasetUtils.select(new BooleanDataset[] {d, g}, new AbstractDataset[] {c, f}, -2.5);
 
 		checkDatasets(e, new DoubleDataset(new double[] {-2.5, 1, 2.9, -2.5, -7, -2.5}, 2, 3));
 	}
 
 	@Test
 	public void testChoose() {
 		DoubleDataset a = new DoubleDataset(new double[] { 0, 1, 3, 5, -7, -9 });
 		DoubleDataset b = new DoubleDataset(new double[] { 0.01, 1.2, 2.9, 5, -7.1, -9 });
 
 		AbstractDataset c = a.clone().reshape(2, 3);
 		IntegerDataset d = new IntegerDataset(new int[] {0, 0, 1, 1, 0, 1}, 2, 3);
 
 		DoubleDataset e = (DoubleDataset) DatasetUtils.choose(d, new Object[] {c, -2}, true, false);
 		checkDatasets(e, new DoubleDataset(new double[] {0, 1, -2, -2, -7, -2}, 2, 3));
 
 		d = new IntegerDataset(new int[] {-2, 0, 3, 1, 0, 2}, 2, 3);
 		try {
 			e = (DoubleDataset) DatasetUtils.choose(d, new Object[] {c, -2}, true, false);
 			fail("Should have thrown an array index OOB exception");
 		} catch (ArrayIndexOutOfBoundsException oob) {
 			// expected
 		}
 		e = (DoubleDataset) DatasetUtils.choose(d, new Object[] {c, -2}, false, false);
 		checkDatasets(e, new DoubleDataset(new double[] {0, 1, -2, -2, -7, -9}, 2, 3));
 
 		e = (DoubleDataset) DatasetUtils.choose(d, new Object[] {c, -2}, false, true);
 		checkDatasets(e, new DoubleDataset(new double[] {0, 1, -2, -2, -7, -2}, 2, 3));
 
 		AbstractDataset f = b.clone().reshape(2, 3);
 		IntegerDataset g = new IntegerDataset(new int[] {1, 0, 1, 1, 2, 2}, 2, 3);
 
 		e = (DoubleDataset) DatasetUtils.choose(g, new Object[] {c, f, -2}, true, false);
 		checkDatasets(e, new DoubleDataset(new double[] {0.01, 1, 2.9, 5, -2, -2}, 2, 3));
 
 		g = new IntegerDataset(new int[] {-1, 3, 1, 1, 2, 2}, 2, 3);
 		try {
 			e = (DoubleDataset) DatasetUtils.choose(d, new Object[] {c, f, -2}, true, false);
 			fail("Should have thrown an array index OOB exception");
 		} catch (ArrayIndexOutOfBoundsException oob) {
 			// expected
 		}
 
 		e = (DoubleDataset) DatasetUtils.choose(g, new Object[] {c, f, -2}, false, false);
 		checkDatasets(e, new DoubleDataset(new double[] {-2, 1, 2.9, 5, -2, -2}, 2, 3));
 
 		e = (DoubleDataset) DatasetUtils.choose(g, new Object[] {c, f, -2}, false, true);
 		checkDatasets(e, new DoubleDataset(new double[] {0, -2, 2.9, 5, -2, -2}, 2, 3));
 	}
 
 	@Test
 	public void testSize() {
 		int[] zero = new int[] {0};
 		int[] one = new int[] {};
 		int[] small = new int[] {2};
 		int[] medium = new int[] {1024, 1024};
 		int[] large = new int[] {1024, 1024, 1024};
 		int[] xxxlarge = new int[] {1024, 1024, 1024, 1024};
 		int[] bad = new int[] {1024, -1, 1024};
 
 		assertEquals(0, AbstractDataset.calcLongSize(zero));
 		assertEquals(0, AbstractDataset.calcSize(zero));
 
 		assertEquals(1, AbstractDataset.calcLongSize(one));
 		assertEquals(1, AbstractDataset.calcSize(one));
 
 		assertEquals(2, AbstractDataset.calcLongSize(small));
 		assertEquals(2, AbstractDataset.calcSize(small));
 
 		assertEquals(1024*1024, AbstractDataset.calcLongSize(medium));
 		assertEquals(1024*1024, AbstractDataset.calcSize(medium));
 
 		assertEquals(1024*1024*1024, AbstractDataset.calcLongSize(large));
 		assertEquals(1024*1024*1024, AbstractDataset.calcSize(large));
 
 		assertEquals(1024*1024*1024*1024L, AbstractDataset.calcLongSize(xxxlarge));
 		try {
 			AbstractDataset.calcSize(xxxlarge);
 			fail("Should have thrown an illegal argument exception");
 		} catch (IllegalArgumentException e) {
 			// expected
 		} catch (Throwable t) {
 			fail("Should have thrown an illegal argument exception");
 		}
 
 		try {
 			AbstractDataset.calcLongSize(bad);
 			fail("Should have thrown an illegal argument exception");
 		} catch (IllegalArgumentException e) {
 			// expected
 		} catch (Throwable t) {
 			fail("Should have thrown an illegal argument exception");
 		}
 
 		try {
 			AbstractDataset.calcSize(bad);
 			fail("Should have thrown an illegal argument exception");
 		} catch (IllegalArgumentException e) {
 			// expected
 		} catch (Throwable t) {
 			fail("Should have thrown an illegal argument exception");
 		}
 	}
 }
