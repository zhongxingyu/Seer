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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.math3.complex.Complex;
 
 /**
  * Mathematics class
  */
 public class Maths {
 	/**
 	 * This function returns the name of the dataset, bracketed if it already
 	 * includes some mathematical symbol
 	 * 
 	 * @param dataset
 	 * @return the bracketed if necessary method name
 	 */
 	private static StringBuilder bracketIfNecessary(final AbstractDataset dataset) {
 		final String name = dataset.getName().trim();
 		StringBuilder newName = new StringBuilder(name);
 		if (name.contains("+") || name.contains("-") || name.contains("*") ||
 				name.contains("/") || name.contains("^") || name.contains("'")) {
 			newName.insert(0, '(');
 			newName.append(')');
 		}
 
 		return newName;
 	}
 
 	private static void addFunctionName(final AbstractDataset dataset, final String fname) {
 		StringBuilder name = new StringBuilder(dataset.getName());
 		name.insert(0, '(');
 		name.insert(0, fname);
 		name.append(')');
 
 		dataset.setName(name.toString());
 	}
 
 	private static AbstractDataset broadcastClone(final AbstractDataset a, final AbstractDataset b) {
 		final int rt = AbstractDataset.getBestDType(a.getDtype(), b.getDtype());
 		final int ia = a.getElementsPerItem();
 		final int ib = b.getElementsPerItem();
 
 		return ia > ib ? a.clone().cast(false, rt, ia) : a.clone().cast(true, rt, ib);
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a + b, addition of a and b
 	 */
 	public static AbstractDataset add(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final AbstractDataset result = broadcastClone(a, b);
 
 		result.iadd(b);
 
 		result.setName(bracketIfNecessary(a).append('+').append(bracketIfNecessary(b)).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a - b, subtraction of a by b
 	 */
 	public static AbstractDataset subtract(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final AbstractDataset result = broadcastClone(a, b);
 
 		result.isubtract(b);
 
 		result.setName(bracketIfNecessary(a).append('-').append(bracketIfNecessary(b)).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a*b, product of a and b
 	 */
 	public static AbstractDataset multiply(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final AbstractDataset result = broadcastClone(a, b);
 
 		result.imultiply(b);
 
 		// set the name based on the changes made
 		result.setName(bracketIfNecessary(a).append('*').append(bracketIfNecessary(b)).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a/b, division of a by b
 	 */
 	public static AbstractDataset divide(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final AbstractDataset result = broadcastClone(a, b);
 
 		result.idivide(b);
 
 		// set the name based on the changes made
 		result.setName(bracketIfNecessary(a).append('/').append(bracketIfNecessary(b)).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a**b, raise a to power of b
 	 */
 	public static AbstractDataset power(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final AbstractDataset result = broadcastClone(a, b);
 
 		result.ipower(b);
 
 		result.setName(bracketIfNecessary(a).append('^').append(bracketIfNecessary(b)).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a%b, reminder of division of a by b
 	 */
 	public static AbstractDataset remainder(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final AbstractDataset result = broadcastClone(a, b);
 
 		result.iremainder(b);
 
 		result.setName(bracketIfNecessary(a).append('%').append(bracketIfNecessary(b)).toString());
 
 		return result;
 	}
 
 	/**
 	 * Adds all sets passed in together
 	 * 
 	 * The first IDataset must cast to AbstractDataset
 	 * 
 	 * For memory efficiency sake if add(...) is called with a
 	 * set of size one, no clone is done, the original object is
 	 * returned directly. Otherwise a new data set is returned,
 	 * the sum of those passed in.
 	 * 
 	 * @param sets
 	 * @param requireClone
 	 * @return sum of collection
 	 */
 	public static AbstractDataset add(final Collection<IDataset> sets, boolean requireClone) {
 		if (sets.isEmpty())
 			return null;
 		final Iterator<IDataset> it = sets.iterator();
 		if (sets.size() == 1)
 			return (AbstractDataset) it.next();
 		AbstractDataset sum = requireClone ? ((AbstractDataset) it.next()).clone() : (AbstractDataset) it.next();
 
 		while (it.hasNext())
 			sum.iadd(it.next());
 
 		return sum;
 	}
 
 	/**
 	 * Multiplies all sets passed in together
 	 * 
 	 * The first IDataset must cast to AbstractDataset
 	 * 
 	 * @param sets
 	 * @param requireClone
 	 * @return product of collection
 	 */
 	public static AbstractDataset multiply(final Collection<IDataset> sets, boolean requireClone) {
 		if (sets.isEmpty())
 			return null;
 		final Iterator<IDataset> it = sets.iterator();
 		if (sets.size() == 1)
 			return (AbstractDataset) it.next();
 		AbstractDataset product = requireClone ? ((AbstractDataset) it.next()).clone() : (AbstractDataset) it.next();
 
 		while (it.hasNext())
 			product = product.imultiply(it.next());
 
 		return product;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a + b, addition of a and b
 	 */
 	public static AbstractDataset add(final AbstractDataset a, final Object b) {
 		if (b instanceof AbstractDataset) {
 			return add(a, (AbstractDataset) b);
 		}
 
 		IndexIterator it1 = a.getIterator();
 		final int is;
 		final int dt = AbstractDataset.getBestDType(a.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 		AbstractDataset result = AbstractDataset.zeros(a, dt);
 		double dvr, dvi;
 		long lv;
 		boolean bv;
 
 		switch (dt) {
 		case AbstractDataset.BOOL:
 			bv = AbstractDataset.toBoolean(b);
 			boolean[] bdata = ((BooleanDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				bdata[i++] = bv || a.getElementBooleanAbs(it1.index);
 			}
 			break;
 		case AbstractDataset.INT8:
 			lv = AbstractDataset.toLong(b);
 			byte[] i8data = ((ByteDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				i8data[i++] = (byte) (a.getElementLongAbs(it1.index) + lv);
 			}
 			break;
 		case AbstractDataset.INT16:
 			lv = AbstractDataset.toLong(b);
 			short[] i16data = ((ShortDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				i16data[i++] = (short) (a.getElementLongAbs(it1.index) + lv);
 			}
 			break;
 		case AbstractDataset.INT32:
 			lv = AbstractDataset.toLong(b);
 			int[] i32data = ((IntegerDataset) result).getData();
 			
 			for (int i = 0; it1.hasNext();) {
 				i32data[i++] = (int) (a.getElementLongAbs(it1.index) + lv);
 			}
 			break;
 		case AbstractDataset.INT64:
 			lv = AbstractDataset.toLong(b);
 			long[] i64data = ((LongDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				i64data[i++] = a.getElementLongAbs(it1.index) + lv;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai8data[i++] = (byte) (a.getElementLongAbs(it1.index + j) + lv);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			short[] ai16data = ((CompoundShortDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai16data[i++] = (short) (a.getElementLongAbs(it1.index + j) + lv);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			int[] ai32data = ((CompoundIntegerDataset) result).getData();
 			
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai32data[i++] = (int) (a.getElementLongAbs(it1.index + j) + lv);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			long[] ai64data = ((CompoundLongDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai64data[i++] = a.getElementLongAbs(it1.index + j) + lv;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			dvr = AbstractDataset.toReal(b);
 			float[] f32data = ((FloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				f32data[i++] = (float) (a.getElementDoubleAbs(it1.index) + dvr);
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			dvr = AbstractDataset.toReal(b);
 			double[] f64data = ((DoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				f64data[i++] = a.getElementDoubleAbs(it1.index) + dvr;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			dvr = AbstractDataset.toReal(b);
 			is = a.getElementsPerItem();
 			float[] a32data = ((CompoundFloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					a32data[i++] = (float) (a.getElementDoubleAbs(it1.index+j) + dvr);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			dvr = AbstractDataset.toReal(b);
 			is = a.getElementsPerItem();
 			double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					a64data[i++] = a.getElementDoubleAbs(it1.index+j) + dvr;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			dvr = AbstractDataset.toReal(b);
 			dvi = AbstractDataset.toImag(b);
 			float[] c64data = ((ComplexFloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				c64data[i++] = (float) (a.getElementDoubleAbs(it1.index) + dvr);
 				c64data[i++] = (float) (a.getElementDoubleAbs(it1.index+1) + dvi);
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			dvr = AbstractDataset.toReal(b);
 			dvi = AbstractDataset.toImag(b);
 			double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				c128data[i++] = a.getElementDoubleAbs(it1.index) + dvr;
 				c128data[i++] = a.getElementDoubleAbs(it1.index+1) + dvi;
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("add does not support this dataset type");
 		}
 
 		// set the name based on the changes made
 		result.setName(bracketIfNecessary(a).append('+').append(b).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a - b, subtraction of a by b
 	 */
 	public static AbstractDataset subtract(final Object a, final Object b) {
 		AbstractDataset result = null;
 		final int is;
 
 		if (a instanceof AbstractDataset) {
 			if (b instanceof AbstractDataset) {
 				return subtract((AbstractDataset) a, (AbstractDataset) b);
 			}
 
 			AbstractDataset ds = (AbstractDataset) a;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			long lv;
 			boolean bv;
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				bv = AbstractDataset.toBoolean(b);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = bv ^ ds.getElementBooleanAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(b);
 				byte[] i8data = ((ByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i8data[i++] = (byte) (ds.getElementLongAbs(it1.index) - lv);
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(b);
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i16data[i++] = (short) (ds.getElementLongAbs(it1.index) - lv);
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(b);
 				int[] i32data = ((IntegerDataset) result).getData();
 				
 				for (int i = 0; it1.hasNext();) {
 					i32data[i++] = (int) (ds.getElementLongAbs(it1.index) - lv);
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(b);
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i64data[i++] = ds.getElementLongAbs(it1.index) - lv;
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				lv = AbstractDataset.toLong(b);
 				is = result.getElementsPerItem();
 				byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai8data[i++] = (byte) (ds.getElementLongAbs(it1.index + j) - lv);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				lv = AbstractDataset.toLong(b);
 				is = result.getElementsPerItem();
 				short[] ai16data = ((CompoundShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai16data[i++] = (short) (ds.getElementLongAbs(it1.index + j) - lv);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				lv = AbstractDataset.toLong(b);
 				is = result.getElementsPerItem();
 				int[] ai32data = ((CompoundIntegerDataset) result).getData();
 				
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai32data[i++] = (int) (ds.getElementLongAbs(it1.index + j) - lv);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				lv = AbstractDataset.toLong(b);
 				is = result.getElementsPerItem();
 				long[] ai64data = ((CompoundLongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai64data[i++] = ds.getElementLongAbs(it1.index + j) - lv;
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvr = AbstractDataset.toReal(b);
 				float[] f32data = ((FloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f32data[i++] = (float) (ds.getElementDoubleAbs(it1.index) - dvr);
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvr = AbstractDataset.toReal(b);
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f64data[i++] = ds.getElementDoubleAbs(it1.index) - dvr;
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				dvr = AbstractDataset.toReal(b);
 				is = result.getElementsPerItem();
 				float[] a32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a32data[i++] = (float) (ds.getElementDoubleAbs(it1.index+j) - dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				dvr = AbstractDataset.toReal(b);
 				is = result.getElementsPerItem();
 				double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a64data[i++] = ds.getElementDoubleAbs(it1.index+j) - dvr;
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				float[] c64data = ((ComplexFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					c64data[i++] = (float) (ds.getElementDoubleAbs(it1.index) - dvr);
 					c64data[i++] = (float) (ds.getElementDoubleAbs(it1.index+1) - dvi);
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					c128data[i++] = ds.getElementDoubleAbs(it1.index) - dvr;
 					c128data[i++] = ds.getElementDoubleAbs(it1.index+1) - dvi;
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("subtract does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(bracketIfNecessary(ds).append('-').append(b).toString());
 
 		} else {
 			if (!(b instanceof AbstractDataset)) {
 				throw new IllegalArgumentException("Both arguments are not datasets");
 			}
 
 			AbstractDataset ds = (AbstractDataset) b;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(a.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			long lv;
 			boolean bv;
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				bv = AbstractDataset.toBoolean(a);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = bv ^ ds.getElementBooleanAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(a);
 				byte[] i8data = ((ByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i8data[i++] = (byte) (lv - ds.getElementLongAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(a);
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i16data[i++] = (short) (lv - ds.getElementLongAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(a);
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i32data[i++] = (int) (lv - ds.getElementLongAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(a);
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i64data[i++] = lv - ds.getElementLongAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				lv = AbstractDataset.toLong(a);
 				is = result.getElementsPerItem();
 				byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai8data[i++] = (byte) (lv - ds.getElementLongAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				lv = AbstractDataset.toLong(a);
 				is = result.getElementsPerItem();
 				short[] ai16data = ((CompoundShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai16data[i++] = (short) (lv - ds.getElementLongAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				lv = AbstractDataset.toLong(a);
 				is = result.getElementsPerItem();
 				int[] ai32data = ((CompoundIntegerDataset) result).getData();
 				
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai32data[i++] = (int) (lv - ds.getElementLongAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				lv = AbstractDataset.toLong(a);
 				is = result.getElementsPerItem();
 				long[] ai64data = ((CompoundLongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai64data[i++] = lv - ds.getElementLongAbs(it1.index + j);
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvr = AbstractDataset.toReal(a);
 				float[] f32data = ((FloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f32data[i++] = (float) (dvr - ds.getElementDoubleAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvr = AbstractDataset.toReal(a);
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f64data[i++] = dvr - ds.getElementDoubleAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				dvr = AbstractDataset.toReal(a);
 				is = result.getElementsPerItem();
 				float[] a32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a32data[i++] = (float) (dvr - ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				dvr = AbstractDataset.toReal(a);
 				is = result.getElementsPerItem();
 				double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a64data[i++] = dvr - ds.getElementDoubleAbs(it1.index+j);
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				float[] c64data = ((ComplexFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					c64data[i++] = (float) (dvr - ds.getElementDoubleAbs(it1.index));
 					c64data[i++] = (float) (dvi - ds.getElementDoubleAbs(it1.index+1));
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					c128data[i++] = dvr - ds.getElementDoubleAbs(it1.index);
 					c128data[i++] = dvi - ds.getElementDoubleAbs(it1.index+1);
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("subtract does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(new StringBuilder(a.toString()).append('-').append(bracketIfNecessary(ds)).toString());
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a * b, product of a and b
 	 */
 	public static AbstractDataset multiply(final AbstractDataset a, final Object b) {
 		if (b instanceof AbstractDataset) {
 			return multiply(a, (AbstractDataset) b);
 		}
 
 		IndexIterator it1 = a.getIterator();
 		final int is;
 
 		final int dt = AbstractDataset.getBestDType(a.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 		AbstractDataset result = AbstractDataset.zeros(a, dt);
 		double dvr, dvi;
 		long lv;
 		boolean bv;
 
 		switch (dt) {
 		case AbstractDataset.BOOL:
 			bv = AbstractDataset.toBoolean(b);
 			boolean[] bdata = ((BooleanDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				bdata[i++] = bv && a.getElementBooleanAbs(it1.index);
 			}
 			break;
 		case AbstractDataset.INT8:
 			lv = AbstractDataset.toLong(b);
 			byte[] i8data = ((ByteDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				i8data[i++] = (byte) (a.getElementLongAbs(it1.index) * lv);
 			}
 			break;
 		case AbstractDataset.INT16:
 			lv = AbstractDataset.toLong(b);
 			short[] i16data = ((ShortDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				i16data[i++] = (short) (a.getElementLongAbs(it1.index) * lv);
 			}
 			break;
 		case AbstractDataset.INT32:
 			lv = AbstractDataset.toLong(b);
 			int[] i32data = ((IntegerDataset) result).getData();
 			
 			for (int i = 0; it1.hasNext();) {
 				i32data[i++] = (int) (a.getElementLongAbs(it1.index) * lv);
 			}
 			break;
 		case AbstractDataset.INT64:
 			lv = AbstractDataset.toLong(b);
 			long[] i64data = ((LongDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				i64data[i++] = a.getElementLongAbs(it1.index) * lv;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai8data[i++] = (byte) (a.getElementLongAbs(it1.index + j) * lv);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			short[] ai16data = ((CompoundShortDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai16data[i++] = (short) (a.getElementLongAbs(it1.index + j) * lv);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			int[] ai32data = ((CompoundIntegerDataset) result).getData();
 			
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai32data[i++] = (int) (a.getElementLongAbs(it1.index + j) * lv);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			lv = AbstractDataset.toLong(b);
 			is = a.getElementsPerItem();
 			long[] ai64data = ((CompoundLongDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					ai64data[i++] = a.getElementLongAbs(it1.index + j) * lv;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			dvr = AbstractDataset.toReal(b);
 			dvi = AbstractDataset.toImag(b);
 			float[] f32data;
 			if (dvi == 0) {
 				f32data = ((FloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f32data[i++] = (float) (a.getElementDoubleAbs(it1.index) * dvr);
 				}
 			} else {
 				result = new ComplexFloatDataset(a.getShape());
 				f32data = ((ComplexFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					double r1 = a.getElementDoubleAbs(it1.index);
 					f32data[i++] = (float) (r1 * dvr);
 					f32data[i++] = (float) (r1 * dvi);
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			dvr = AbstractDataset.toReal(b);
 			dvi = AbstractDataset.toImag(b);
 			double[] f64data;
 			if (dvi == 0) {
 				f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f64data[i++] = a.getElementDoubleAbs(it1.index) * dvr;
 				}
 			} else {
 				result = new ComplexDoubleDataset(a.getShape());
 				f64data = ((ComplexDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					double r1 = a.getElementDoubleAbs(it1.index);
 					f64data[i++] = r1 * dvr;
 					f64data[i++] = r1 * dvi;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			dvr = AbstractDataset.toReal(b);
 			is = result.getElementsPerItem();
 			float[] a32data = ((CompoundFloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					a32data[i++] = (float) (a.getElementDoubleAbs(it1.index+j) * dvr);
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			dvr = AbstractDataset.toReal(b);
 			is = result.getElementsPerItem();
 			double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				for (int j = 0; j < is; j++) {
 					a64data[i++] = a.getElementDoubleAbs(it1.index+j) * dvr;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			dvr = AbstractDataset.toReal(b);
 			dvi = AbstractDataset.toImag(b);
 			float[] c64data = ((ComplexFloatDataset) result).getData();
 
 			if (dvi == 0) {
 				for (int i = 0; it1.hasNext();) {
 					double r1 = a.getElementDoubleAbs(it1.index);
 					double i1 = a.getElementDoubleAbs(it1.index + 1);
 					c64data[i++] = (float) (r1*dvr);
 					c64data[i++] = (float) (i1*dvr);
 				}
 			} else {
 				for (int i = 0; it1.hasNext();) {
 					double r1 = a.getElementDoubleAbs(it1.index);
 					double i1 = a.getElementDoubleAbs(it1.index + 1);
 					c64data[i++] = (float) (r1*dvr - i1*dvi);
 					c64data[i++] = (float) (r1*dvi + i1*dvr);
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			dvr = AbstractDataset.toReal(b);
 			dvi = AbstractDataset.toImag(b);
 			double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 			if (dvi == 0) {
 				for (int i = 0; it1.hasNext();) {
 					double r1 = a.getElementDoubleAbs(it1.index);
 					double i1 = a.getElementDoubleAbs(it1.index + 1);
 					c128data[i++] = r1*dvr;
 					c128data[i++] = i1*dvr;
 				}
 			} else {
 				for (int i = 0; it1.hasNext();) {
 					double r1 = a.getElementDoubleAbs(it1.index);
 					double i1 = a.getElementDoubleAbs(it1.index + 1);
 					c128data[i++] = r1*dvr - i1*dvi;
 					c128data[i++] = r1*dvi + i1*dvr;
 				}
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("multiply does not support this dataset type");
 		}
 
 		// set the name based on the changes made
 		result.setName(bracketIfNecessary(a).append('*').append(b).toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a / b, division of a by b
 	 */
 	public static AbstractDataset divide(final Object a, final Object b) {
 		AbstractDataset result = null;
 		final int is;
 
 		if (a instanceof AbstractDataset) {
 			if (b instanceof AbstractDataset) {
 				return divide((AbstractDataset) a, (AbstractDataset) b);
 			}
 
 			AbstractDataset ds = (AbstractDataset) a;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			long lv;
 			boolean bv;
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				bv = AbstractDataset.toBoolean(b);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = bv && ds.getElementBooleanAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					byte[] i8data = ((ByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i8data[i++] = (byte) (ds.getElementLongAbs(it1.index) / lv);
 					}
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					short[] i16data = ((ShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i16data[i++] = (short) (ds.getElementLongAbs(it1.index) / lv);
 					}
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					int[] i32data = ((IntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i32data[i++] = (int) (ds.getElementLongAbs(it1.index) / lv);
 					}
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					long[] i64data = ((LongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i64data[i++] = ds.getElementLongAbs(it1.index) / lv;
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai8data[i++] = (byte) (ds.getElementLongAbs(it1.index + j) / lv);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					short[] ai16data = ((CompoundShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai16data[i++] = (short) (ds.getElementLongAbs(it1.index + j) / lv);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai32data[i++] = (int) (ds.getElementLongAbs(it1.index + j) / lv);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					long[] ai64data = ((CompoundLongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai64data[i++] = ds.getElementLongAbs(it1.index + j) / lv;
 						}
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				if (dvi == 0) {
 					float[] f32data = ((FloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f32data[i++] = (float) (ds.getElementDoubleAbs(it1.index) / dvr);
 					}
 				} else {
 					result = new ComplexFloatDataset(ds.getShape());
 					float[] c64data = ((ComplexFloatDataset) result).getData();
 
 					if (Math.abs(dvr) < Math.abs(dvi)) {
 						double q = dvr/dvi;
 						double den = dvr*q + dvi;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c64data[i++] = (float) ((r1*q + i1) / den);
 							c64data[i++] = (float) ((i1*q - r1) / den);
 						}
 					} else {
 						double q = dvi/dvr;
 						double den = dvi*q + dvr;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c64data[i++] = (float) ((i1*q + r1) / den);
 							c64data[i++] = (float) ((i1 - r1*q) / den);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				if (dvi == 0) {
 					result = new DoubleDataset(ds.getShape());
 					double[] f64data = ((DoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f64data[i++] = ds.getElementDoubleAbs(it1.index) / dvr;
 					}
 				} else {
 					result = new ComplexDoubleDataset(ds.getShape());
 					double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 					if (Math.abs(dvr) < Math.abs(dvi)) {
 						double q = dvr/dvi;
 						double den = dvr*q + dvi;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c128data[i++] = (r1*q + i1) / den;
 							c128data[i++] = (i1*q - r1) / den;
 						}
 					} else {
 						double q = dvi/dvr;
 						double den = dvi*q + dvr;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c128data[i++] = (i1*q + r1) / den;
 							c128data[i++] = (i1 - r1*q) / den;
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				dvr = AbstractDataset.toReal(b);
 				is = result.getElementsPerItem();
 				float[] a32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a32data[i++] = (float) (ds.getElementDoubleAbs(it1.index+j) / dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				dvr = AbstractDataset.toReal(b);
 				is = result.getElementsPerItem();
 				double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a64data[i++] = ds.getElementDoubleAbs(it1.index+j) / dvr;
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				float[] c64data = ((ComplexFloatDataset) result).getData();
 
 				if (dvi == 0) {
 					for (int i = 0; it1.hasNext();) {
 						c64data[i++] = (float) (ds.getElementDoubleAbs(it1.index) / dvr);
 						c64data[i++] = (float) (ds.getElementDoubleAbs(it1.index+1) / dvr);
 					}	
 				} else {
 					if (Math.abs(dvr) < Math.abs(dvi)) {
 						double q = dvr/dvi;
 						double den = dvr*q + dvi;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c64data[i++] = (float) ((r1*q + i1) / den);
 							c64data[i++] = (float) ((i1*q - r1) / den);
 						}
 					} else {
 						double q = dvi/dvr;
 						double den = dvi*q + dvr;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c64data[i++] = (float) ((i1*q + r1) / den);
 							c64data[i++] = (float) ((i1 - r1*q) / den);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 				if (dvi == 0) {
 					for (int i = 0; it1.hasNext();) {
 						c128data[i++] = ds.getElementDoubleAbs(it1.index) / dvr;
 						c128data[i++] = ds.getElementDoubleAbs(it1.index+1) / dvr;
 					}	
 				} else {
 					if (Math.abs(dvr) < Math.abs(dvi)) {
 						double q = dvr/dvi;
 						double den = dvr*q + dvi;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c128data[i++] = (r1*q + i1) / den;
 							c128data[i++] = (i1*q - r1) / den;
 						}
 					} else {
 						double q = dvi/dvr;
 						double den = dvi*q + dvr;
 
 						for (int i = 0; it1.hasNext();) {
 							double r1 = ds.getElementDoubleAbs(it1.index);
 							double i1 = ds.getElementDoubleAbs(it1.index + 1);
 							c128data[i++] = (i1*q + r1) / den;
 							c128data[i++] = (i1 - r1*q) / den;
 						}
 					}
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("divide does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(bracketIfNecessary(ds).append('/').append(b).toString());
 		} else {
 			if (!(b instanceof AbstractDataset)) {
 				throw new IllegalArgumentException("Both arguments are not datasets");
 			}
 
 			AbstractDataset ds = (AbstractDataset) b;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(a.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			long lv;
 			boolean bv;
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				bv = AbstractDataset.toBoolean(a);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = bv && ds.getElementBooleanAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(a);
 				byte[] i8data = ((ByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					try {
 						i8data[i++] = (byte) (lv / ds.getElementLongAbs(it1.index));
 					} catch (ArithmeticException e) {
 						i++;
 					}
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(a);
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					try {
 						i16data[i++] = (short) (lv / ds.getElementLongAbs(it1.index));
 					} catch (ArithmeticException e) {
 						i++;
 					}
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(a);
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					try {
 						i32data[i++] = (int) (lv / ds.getElementLongAbs(it1.index));
 					} catch (ArithmeticException e) {
 						i++;
 					}
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(a);
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					try {
 						i64data[i++] = lv / ds.getElementLongAbs(it1.index);
 					} catch (ArithmeticException e) {
 						i++;
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							try {
 								ai8data[i++] = (byte) (lv / ds.getElementLongAbs(it1.index + j));
 							} catch (ArithmeticException e) {
 								i++;
 							}
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					short[] ai16data = ((CompoundShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							try {
 								ai16data[i++] = (short) (lv / ds.getElementLongAbs(it1.index + j));
 							} catch (ArithmeticException e) {
 								i++;
 							}
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							try {
 								ai32data[i++] = (int) (lv / ds.getElementLongAbs(it1.index + j));
 							} catch (ArithmeticException e) {
 								i++;
 							}
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					long[] ai64data = ((CompoundLongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							try {
 								ai64data[i++] = lv / ds.getElementLongAbs(it1.index + j);
 							} catch (ArithmeticException e) {
 								i++;
 							}
 						}
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				if (dvi == 0) {
 					float[] f32data = ((FloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f32data[i++] = (float) (dvr / ds.getElementDoubleAbs(it1.index));
 					}
 				} else {
 					result = new ComplexFloatDataset(ds.getShape());
 					float[] c64data = ((ComplexFloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						double r1 = ds.getElementDoubleAbs(it1.index);
 
 						c64data[i++] = (float) (dvr / r1);
 						c64data[i++] = (float) (dvi / r1);
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				if (dvi == 0) {
 					double[] f64data = ((DoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f64data[i++] = dvr / ds.getElementDoubleAbs(it1.index);
 					}
 				} else {
 					result = new ComplexDoubleDataset(ds.getShape());
 					double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						double r1 = ds.getElementDoubleAbs(it1.index);
 
 						c128data[i++] = dvr / r1;
 						c128data[i++] = dvi / r1;
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				float[] c64data = ((ComplexFloatDataset) result).getData();
 
 				if (dvi == 0) {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c64data[i++] = (float) (dvr*q / den);
 							c64data[i++] = (float) (-dvr / den);
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c64data[i++] = (float) (dvr / den);
 							c64data[i++] = (float) (-dvr*q / den);
 						}
 					}	
 				} else {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c64data[i++] = (float) ((dvr*q + dvi) / den);
 							c64data[i++] = (float) ((dvi*q - dvr) / den);
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c64data[i++] = (float) ((dvi*q + dvr) / den);
 							c64data[i++] = (float) ((dvi - dvr*q) / den);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 				if (dvi == 0) {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c128data[i++] = dvr*q / den;
 							c128data[i++] = -dvr / den;
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c128data[i++] = dvr / den;
 							c128data[i++] = -dvr*q / den;
 						}
 					}	
 				} else {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c128data[i++] = (dvr*q + dvi) / den;
 							c128data[i++] = (dvi*q - dvr) / den;
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c128data[i++] = (dvi*q + dvr) / den;
 							c128data[i++] = (dvi - dvr*q) / den;
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				dvr = AbstractDataset.toReal(a);
 				is = result.getElementsPerItem();
 				float[] a32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a32data[i++] = (float) (dvr / ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				dvr = AbstractDataset.toReal(a);
 				is = result.getElementsPerItem();
 				double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a64data[i++] = dvr / ds.getElementDoubleAbs(it1.index+j);
 					}
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("divide does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(new StringBuilder(a.toString()).append('/').append(bracketIfNecessary(ds)).toString());
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a / b, division of a by b but catches divide-by-zero
 	 */
 	public static AbstractDataset dividez(final Object a, final Object b) {
 		AbstractDataset result = null;
 		final int is;
 
 		if (a instanceof AbstractDataset) {
 			if (b instanceof AbstractDataset) {
 				final int at = ((AbstractDataset) a).getDtype();
 				final int bt = ((AbstractDataset) b).getDtype();
 
 				AbstractDataset d1, d2;
 				if (bt > at) {
 					d1 = (AbstractDataset) b;
 					d2 = (AbstractDataset) a;
 				} else {
 					d1 = (AbstractDataset) a;
 					d2 = (AbstractDataset) b;
 				}
 				d1.checkCompatibility(d2);
 				result = AbstractDataset.zeros(d1, bt);
 				IndexIterator it1 = d1.getIterator();
 				IndexIterator it2 = d2.getIterator();
 
 				switch (bt) {
 				case AbstractDataset.BOOL:
 					boolean[] bdata = ((BooleanDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						bdata[i++] = d1.getElementBooleanAbs(it1.index) && d2.getElementBooleanAbs(it2.index);
 					}
 					break;
 				case AbstractDataset.INT8:
 					byte[] i8data = ((ByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						long v = d2.getElementLongAbs(it2.index);
 
 						i8data[i++] = v == 0 ? 0 : (byte) (d1.getElementLongAbs(it1.index) / v);
 					}
 					break;
 				case AbstractDataset.INT16:
 					short[] i16data = ((ShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						long v = d2.getElementLongAbs(it2.index);
 
 						i16data[i++] = v == 0 ? 0 : (short) (d1.getElementLongAbs(it1.index) / v);
 					}
 					break;
 				case AbstractDataset.INT32:
 					int[] i32data = ((IntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						long v = d2.getElementLongAbs(it2.index);
 
 						i32data[i++] = v == 0 ? 0 : (int) (d1.getElementLongAbs(it1.index) / v);
 					}
 					break;
 				case AbstractDataset.INT64:
 					long[] i64data = ((LongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						long v = d2.getElementLongAbs(it2.index);
 
 						i64data[i++] = v == 0 ? 0 : (d1.getElementLongAbs(it1.index) / v);
 					}
 					break;
 				case AbstractDataset.ARRAYINT8:
 					byte[] ai8data = ((CompoundByteDataset) result).getData();
 					is = result.getElementsPerItem();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = d2.getElementLongAbs(it2.index+j);
 
 							ai8data[i++] = v == 0 ? 0 : (byte) (d1.getElementLongAbs(it1.index+j) / v);
 						}
 					}
 					break;
 				case AbstractDataset.ARRAYINT16:
 					short[] ai16data = ((CompoundShortDataset) result).getData();
 					is = result.getElementsPerItem();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = d2.getElementLongAbs(it2.index+j);
 
 							ai16data[i++] = v == 0 ? 0 : (short) (d1.getElementLongAbs(it1.index+j) / v);
 						}
 					}
 					break;
 				case AbstractDataset.ARRAYINT32:
 					int[] ai32data = ((CompoundIntegerDataset) result).getData();
 					is = result.getElementsPerItem();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = d2.getElementLongAbs(it2.index+j);
 
 							ai32data[i++] = v == 0 ? 0 : (int) (d1.getElementLongAbs(it1.index+j) / v);
 						}
 					}
 					break;
 				case AbstractDataset.ARRAYINT64:
 					long[] ai64data = ((CompoundLongDataset) result).getData();
 					is = result.getElementsPerItem();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = d2.getElementLongAbs(it2.index+j);
 
 							ai64data[i++] = v == 0 ? 0 : (d1.getElementLongAbs(it1.index+j) / v);
 						}
 					}
 					break;
 				case AbstractDataset.FLOAT32:
 					float[] f32data = ((FloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						double v = d2.getElementDoubleAbs(it2.index);
 
 						f32data[i++] = v == 0 ? 0 : (float) (d1.getElementDoubleAbs(it1.index) / v);
 					}
 					break;
 				case AbstractDataset.FLOAT64:
 					double[] f64data = ((DoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						double v = d2.getElementDoubleAbs(it2.index);
 
 						f64data[i++] = v == 0 ? 0 : (d1.getElementDoubleAbs(it1.index) / v);
 					}
 					break;
 				case AbstractDataset.ARRAYFLOAT32:
 					float[] af32data = ((CompoundFloatDataset) result).getData();
 					is = result.getElementsPerItem();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							double v = d2.getElementDoubleAbs(it2.index+j);
 
 							af32data[i++] = v == 0 ? 0 : (float) (d1.getElementDoubleAbs(it1.index+j) / v);
 						}
 					}
 					break;
 				case AbstractDataset.ARRAYFLOAT64:
 					double[] af64data = ((CompoundDoubleDataset) result).getData();
 					is = result.getElementsPerItem();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							double v = d2.getElementDoubleAbs(it2.index+j);
 
 							af64data[i++] = v == 0 ? 0 : (d1.getElementDoubleAbs(it1.index+j) / v);
 						}
 					}
 					break;
 				case AbstractDataset.COMPLEX64:
 					float[] c64data = ((ComplexFloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext(); ) {
 						double r1 = d1.getElementDoubleAbs(it1.index);
 						double i1 = d1.getElementDoubleAbs(it1.index + 1);
 						double r2 = d2.getElementDoubleAbs(it2.index);
 						double i2 = d2.getElementDoubleAbs(it2.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c64data[i++] = (float) ((r1*q + i1) / den);
 							c64data[i++] = (float) ((i1*q - r1) / den);
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c64data[i++] = den == 0 ? 0 : (float) ((i1*q + r1) / den);
 							c64data[i++] = den == 0 ? 0 : (float) ((i1 - r1*q) / den);
 						}
 					}
 					break;
 				case AbstractDataset.COMPLEX128:
 					double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext() && it2.hasNext(); ) {
 						double r1 = d1.getElementDoubleAbs(it1.index);
 						double i1 = d1.getElementDoubleAbs(it1.index + 1);
 						double r2 = d2.getElementDoubleAbs(it2.index);
 						double i2 = d2.getElementDoubleAbs(it2.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c128data[i++] = ((r1*q + i1) / den);
 							c128data[i++] = ((i1*q - r1) / den);
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c128data[i++] = den == 0 ? 0 : ((i1*q + r1) / den);
 							c128data[i++] = den == 0 ? 0 : ((i1 - r1*q) / den);
 						}
 					}
 					break;
 				default:
 					throw new UnsupportedOperationException("dividez does not support this dataset type");
 				}
 			} else {
 				AbstractDataset ds = (AbstractDataset) a;
 				final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(b
 						.getClass()));
 
 				double dvr;
 				dvr = AbstractDataset.toLong(b);
 				if (dvr == 0) {
 					result = AbstractDataset.zeros(ds, dt);
 
 					// set the name based on the changes made
 					result.setName(bracketIfNecessary(ds).append('/').append(b).toString());
 				} else {
 					result = divide(a, b);
 				}
 			}
 		} else {
 			if (!(b instanceof AbstractDataset)) {
 				throw new IllegalArgumentException("Both arguments are not datasets");
 			}
 
 			AbstractDataset ds = (AbstractDataset) b;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(a.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 
 			double dvr, dvi;
 			long lv;
 			boolean bv;
 
 			dvr = AbstractDataset.toReal(a);
 			if (dvr == 0) {
 				return result;
 			}
 
 			IndexIterator it1 = ds.getIterator();
 
 			switch (dt) {
 			case AbstractDataset.BOOL:
 				bv = AbstractDataset.toBoolean(a);
 				boolean[] bdata = ((BooleanDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					bdata[i++] = bv && ds.getElementBooleanAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					byte[] i8data = ((ByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						long v = ds.getElementLongAbs(it1.index);
 
 						i8data[i++] = v == 0 ? 0 : (byte) (lv / v);
 					}
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					long v = ds.getElementLongAbs(it1.index);
 
 					i16data[i++] = v == 0 ? 0 : (short) (lv / v);
 				}
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					long v = ds.getElementLongAbs(it1.index);
 
 					i32data[i++] = v == 0 ? 0 : (int) (lv / v);
 				}
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					long v = ds.getElementLongAbs(it1.index);
 
 					i64data[i++] = v == 0 ? 0 : lv / v;
 				}
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = ds.getElementLongAbs(it1.index+j);
 							ai8data[i++] = v == 0 ? 0 : (byte) (lv / v);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					short[] ai16data = ((CompoundShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = ds.getElementLongAbs(it1.index+j);
 							ai16data[i++] = v == 0 ? 0 : (short) (lv / v);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = ds.getElementLongAbs(it1.index+j);
 							ai32data[i++] = v == 0 ? 0 : (int) (lv / v);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				lv = AbstractDataset.toLong(a);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					long[] ai64data = ((CompoundLongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							long v = ds.getElementLongAbs(it1.index+j);
 							ai64data[i++] = v == 0 ? 0 : (lv / v);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvi = AbstractDataset.toImag(a);
 				if (dvi == 0) {
 					float[] f32data = ((FloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						double r1 = ds.getElementDoubleAbs(it1.index);
 
 						f32data[i++] = r1 == 0 ? 0 : (float) (dvr / r1);
 					}
 				} else {
 					result = new ComplexFloatDataset(ds.getShape());
 					float[] c64data = ((ComplexFloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						double r1 = ds.getElementDoubleAbs(it1.index);
 
 						c64data[i++] = r1 == 0 ? 0 : (float) (dvr / r1);
 						c64data[i++] = r1 == 0 ? 0 : (float) (dvi / r1);
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvi = AbstractDataset.toImag(a);
 				if (dvi == 0) {
 					double[] f64data = ((DoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						double r1 = ds.getElementDoubleAbs(it1.index);
 
 						f64data[i++] = r1 == 0 ? 0 : dvr / r1;
 					}
 				} else {
 					result = new ComplexDoubleDataset(ds.getShape());
 					double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						double r1 = ds.getElementDoubleAbs(it1.index);
 
 						c128data[i++] = r1 == 0 ? 0 : dvr / r1;
 						c128data[i++] = r1 == 0 ? 0 : dvi / r1;
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				is = result.getElementsPerItem();
 				float[] af32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						double r1 = ds.getElementDoubleAbs(it1.index+j);
 
 						af32data[i++] = r1 == 0 ? 0 : (float) (dvr / r1);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				is = result.getElementsPerItem();
 				double[] af64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						double r1 = ds.getElementDoubleAbs(it1.index+j);
 
 						af64data[i++] = r1 == 0 ? 0 : dvr / r1;
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvi = AbstractDataset.toImag(a);
 				float[] c64data = ((ComplexFloatDataset) result).getData();
 
 				if (dvi == 0) {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c64data[i++] = (float) (dvr*q / den);
 							c64data[i++] = (float) (-dvr / den);
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c64data[i++] = den == 0 ? 0 : (float) (dvr / den);
 							c64data[i++] = den == 0 ? 0 : (float) (-dvr*q / den);
 						}
 					}	
 				} else {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c64data[i++] = (float) ((dvr*q + dvi) / den);
 							c64data[i++] = (float) ((dvi*q - dvr) / den);
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c64data[i++] = den == 0 ? 0 : (float) ((dvi*q + dvr) / den);
 							c64data[i++] = den == 0 ? 0 : (float) ((dvi - dvr*q) / den);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvi = AbstractDataset.toImag(a);
 				double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 				if (dvi == 0) {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c128data[i++] = dvr*q / den;
 							c128data[i++] = -dvr / den;
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c128data[i++] = den == 0 ? 0 : dvr / den;
 							c128data[i++] = den == 0 ? 0 : -dvr*q / den;
 						}
 					}	
 				} else {
 					for (int i = 0; it1.hasNext();) {
 						double r2 = ds.getElementDoubleAbs(it1.index);
 						double i2 = ds.getElementDoubleAbs(it1.index + 1);
 						if (Math.abs(r2) < Math.abs(i2)) {
 							double q = r2/i2;
 							double den = r2*q + i2;
 							c128data[i++] = (dvr*q + dvi) / den;
 							c128data[i++] = (dvi*q - dvr) / den;
 						} else {
 							double q = i2/r2;
 							double den = i2*q + r2;
 							c128data[i++] = den == 0 ? 0 : (dvi*q + dvr) / den;
 							c128data[i++] = den == 0 ? 0 : (dvi - dvr*q) / den;
 						}
 					}
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("dividez does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(new StringBuilder(a.toString()).append('/').append(bracketIfNecessary(ds)).toString());
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a**b, raise a to power of b
 	 */
 	public static AbstractDataset power(final Object a, final Object b) {
 		AbstractDataset result = null;
 		final int is;
 
 		if (a instanceof AbstractDataset) {
 			if (b instanceof AbstractDataset) {
 				return power((AbstractDataset) a, (AbstractDataset) b);
 			}
 
 			final AbstractDataset ds = (AbstractDataset) a;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			final IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			dvr = AbstractDataset.toReal(b);
 
 			switch (dt) {
 			case AbstractDataset.INT8:
 				final byte[] i8data = ((ByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i8data[i++] = (byte) Math.pow(ds.getElementDoubleAbs(it1.index), dvr);
 				}
 				break;
 			case AbstractDataset.INT16:
 				final short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i16data[i++] = (short) Math.pow(ds.getElementDoubleAbs(it1.index), dvr);
 				}
 				break;
 			case AbstractDataset.INT32:
 				final int[] i32data = ((IntegerDataset) result).getData();
 				
 				for (int i = 0; it1.hasNext();) {
 					i32data[i++] = (int) Math.pow(ds.getElementDoubleAbs(it1.index), dvr);
 				}
 				break;
 			case AbstractDataset.INT64:
 				final long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i64data[i++] = (long) Math.pow(ds.getElementDoubleAbs(it1.index), dvr);
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				is = result.getElementsPerItem();
 				final byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai8data[i++] = (byte) Math.pow(ds.getElementDoubleAbs(it1.index+j), dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				is = result.getElementsPerItem();
 				final short[] ai16data = ((CompoundShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai16data[i++] = (short) Math.pow(ds.getElementDoubleAbs(it1.index+j), dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				is = result.getElementsPerItem();
 				final int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai32data[i++] = (int) Math.pow(ds.getElementDoubleAbs(it1.index+j), dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				is = result.getElementsPerItem();
 				final long[] ai64data = ((CompoundLongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai64data[i++] = (long) Math.pow(ds.getElementDoubleAbs(it1.index+j), dvr);
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvi = AbstractDataset.toImag(b);
 				if (dvi == 0) {
 					final float[] f32data = ((FloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f32data[i++] = (float) Math.pow(ds.getElementDoubleAbs(it1.index), dvr);
 					}
 				} else {
 					result = new ComplexFloatDataset(ds.getShape());
 					final float[] c64data = ((ComplexFloatDataset) result).getData();
 					final Complex p = new Complex(dvr, dvi);
 
 					for (int i = 0; it1.hasNext();) {
 						Complex tz = new Complex(ds.getElementDoubleAbs(it1.index),	ds.getElementDoubleAbs(it1.index+1)).pow(p);
 
 						c64data[i++] = (float) tz.getReal();
 						c64data[i++] = (float) tz.getImaginary();
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvi = AbstractDataset.toImag(b);
 				if (dvi == 0) {
 					result = new DoubleDataset(ds.getShape());
 					final double[] f64data = ((DoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f64data[i++] = Math.pow(ds.getElementDoubleAbs(it1.index), dvr);
 					}
 				} else {
 					result = new ComplexDoubleDataset(ds.getShape());
 					final double[] c128data = ((ComplexDoubleDataset) result).getData();
 					final Complex p = new Complex(dvr, dvi);
 
 					for (int i = 0; it1.hasNext();) {
 						Complex tz = new Complex(ds.getElementDoubleAbs(it1.index),	ds.getElementDoubleAbs(it1.index+1)).pow(p);
 
 						c128data[i++] = tz.getReal();
 						c128data[i++] = tz.getImaginary();
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				is = result.getElementsPerItem();
 				final float[] a32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a32data[i++] = (float) Math.pow(ds.getElementDoubleAbs(it1.index+j), dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				is = result.getElementsPerItem();
 				final double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a64data[i++] = Math.pow(ds.getElementDoubleAbs(it1.index+j), dvr);
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvi = AbstractDataset.toImag(b);
 				final float[] c64data = ((ComplexFloatDataset) result).getData();
 				final Complex p64 = new Complex(dvr, dvi);
 
 				for (int i = 0; it1.hasNext();) {
 					Complex tz = new Complex(ds.getElementDoubleAbs(it1.index),	ds.getElementDoubleAbs(it1.index+1)).pow(p64);
 
 					c64data[i++] = (float) tz.getReal();
 					c64data[i++] = (float) tz.getImaginary();
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvi = AbstractDataset.toImag(b);
 				final double[] c128data = ((ComplexDoubleDataset) result).getData();
 				final Complex p128 = new Complex(dvr, dvi);
 
 				for (int i = 0; it1.hasNext();) {
 					Complex tz = new Complex(ds.getElementDoubleAbs(it1.index),	ds.getElementDoubleAbs(it1.index+1)).pow(p128);
 
 					c128data[i++] = tz.getReal();
 					c128data[i++] = tz.getImaginary();
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("power does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(bracketIfNecessary(ds).append("**").append(b).toString());
 		} else {
 			if (!(b instanceof AbstractDataset)) {
 				throw new IllegalArgumentException("Both arguments are not datasets");
 			}
 
 			final AbstractDataset ds = (AbstractDataset) b;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(a.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			dvr = AbstractDataset.toReal(a);
 
 			switch (dt) {
 			case AbstractDataset.INT8:
 				final byte[] i8data = ((ByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i8data[i++] = (byte) Math.pow(dvr, ds.getElementDoubleAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT16:
 				final short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i16data[i++] = (short) Math.pow(dvr, ds.getElementDoubleAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT32:
 				final int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i32data[i++] = (int) Math.pow(dvr, ds.getElementDoubleAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT64:
 				final long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i64data[i++] = (long) Math.pow(dvr, ds.getElementDoubleAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				is = result.getElementsPerItem();
 				final byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai8data[i++] = (byte) Math.pow(dvr, ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				is = result.getElementsPerItem();
 				final short[] ai16data = ((CompoundShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai16data[i++] = (short) Math.pow(dvr, ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				is = result.getElementsPerItem();
 				final int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai32data[i++] = (int) Math.pow(dvr, ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				is = result.getElementsPerItem();
 				final long[] ai64data = ((CompoundLongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai64data[i++] = (long) Math.pow(dvr, ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvi = AbstractDataset.toImag(a);
 				if (dvi == 0) {
 					final float[] f32data = ((FloatDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f32data[i++] = (float) Math.pow(dvr, ds.getElementDoubleAbs(it1.index));
 					}
 				} else {
 					result = new ComplexFloatDataset(ds.getShape());
 					final float[] c64data = ((ComplexFloatDataset) result).getData();
 					final Complex b64 = new Complex(dvr, dvi);
 
 					for (int i = 0; it1.hasNext();) {
 						Complex tz = b64.pow(new Complex(ds.getElementDoubleAbs(it1.index),
 								ds.getElementDoubleAbs(it1.index+1)));
 
 						c64data[i++] = (float) tz.getReal();
 						c64data[i++] = (float) tz.getImaginary();
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvi = AbstractDataset.toImag(a);
 				if (dvi == 0) {
 					final double[] f64data = ((DoubleDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						f64data[i++] = Math.pow(dvr, ds.getElementDoubleAbs(it1.index));
 					}
 				} else {
 					result = new ComplexDoubleDataset(ds.getShape());
 					final double[] c128data = ((ComplexDoubleDataset) result).getData();
 					final Complex b128 = new Complex(dvr, dvi);
 
 					for (int i = 0; it1.hasNext();) {
 						Complex tz = b128.pow(new Complex(ds.getElementDoubleAbs(it1.index),
 								ds.getElementDoubleAbs(it1.index+1)));
 
 						c128data[i++] = tz.getReal();
 						c128data[i++] = tz.getImaginary();
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				is = result.getElementsPerItem();
 				final float[] a32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a32data[i++] = (float) Math.pow(dvr, ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				is = result.getElementsPerItem();
 				final double[] a64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						a64data[i++] = Math.pow(dvr, ds.getElementDoubleAbs(it1.index+j));
 					}
 				}
 				break;
 			case AbstractDataset.COMPLEX64:
 				dvi = AbstractDataset.toImag(a);
 				final float[] c64data = ((ComplexFloatDataset) result).getData();
 				final Complex b64 = new Complex(dvr, dvi);
 
 				for (int i = 0; it1.hasNext();) {
 					Complex tz = b64.pow(new Complex(ds.getElementDoubleAbs(it1.index),
 							ds.getElementDoubleAbs(it1.index+1)));
 
 					c64data[i++] = (float) tz.getReal();
 					c64data[i++] = (float) tz.getImaginary();
 				}
 				break;
 			case AbstractDataset.COMPLEX128:
 				dvi = AbstractDataset.toImag(a);
 				final double[] c128data = ((ComplexDoubleDataset) result).getData();
 				final Complex b128 = new Complex(dvr, dvi);
 
 				for (int i = 0; it1.hasNext();) {
 					Complex tz = b128.pow(new Complex(ds.getElementDoubleAbs(it1.index),
 							ds.getElementDoubleAbs(it1.index+1)));
 
 					c128data[i++] = tz.getReal();
 					c128data[i++] = tz.getImaginary();
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("power does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(new StringBuilder(a.toString()).append("**").append(bracketIfNecessary(ds)).toString());
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return a % b, remainder of division of a by b
 	 */
 	public static AbstractDataset remainder(final Object a, final Object b) {
 		AbstractDataset result = null;
 		final int is;
 
 		if (a instanceof AbstractDataset) {
 			if (b instanceof AbstractDataset) {
 				return remainder((AbstractDataset) a, (AbstractDataset) b);
 			}
 
 			AbstractDataset ds = (AbstractDataset) a;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(b.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			long lv;
 
 			switch (dt) {
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					byte[] i8data = ((ByteDataset) result).getData();
 					for (int i = 0; it1.hasNext();) {
 						i8data[i++] = (byte) (ds.getElementLongAbs(it1.index) % lv);
 					}
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					short[] i16data = ((ShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i16data[i++] = (short) (ds.getElementLongAbs(it1.index) % lv);
 					}
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					int[] i32data = ((IntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i32data[i++] = (int) (ds.getElementLongAbs(it1.index) % lv);
 					}
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					long[] i64data = ((LongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						i64data[i++] = ds.getElementLongAbs(it1.index) % lv;
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai8data[i++] = (byte) (ds.getElementLongAbs(it1.index + j) % lv);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					short[] ai16data = ((CompoundShortDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai16data[i++] = (short) (ds.getElementLongAbs(it1.index + j) % lv);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai32data[i++] = (int) (ds.getElementLongAbs(it1.index + j) % lv);
 						}
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				lv = AbstractDataset.toLong(b);
 				if (lv != 0) {
 					is = result.getElementsPerItem();
 					long[] ai64data = ((CompoundLongDataset) result).getData();
 
 					for (int i = 0; it1.hasNext();) {
 						for (int j = 0; j < is; j++) {
 							ai64data[i++] = ds.getElementLongAbs(it1.index + j) % lv;
 						}
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				if (dvi != 0) {
 					throw new IllegalArgumentException("remainder supports integer or real divisors only");
 				}
 				float[] f32data = ((FloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f32data[i++] = (float) (ds.getElementDoubleAbs(it1.index) % dvr);
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvr = AbstractDataset.toReal(b);
 				dvi = AbstractDataset.toImag(b);
 				if (dvi != 0) {
 					throw new IllegalArgumentException("remainder supports integer or real divisors only");
 				}
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f64data[i++] = ds.getElementDoubleAbs(it1.index) % dvr;
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				dvr = AbstractDataset.toReal(b);
 				is = result.getElementsPerItem();
 				float[] af32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						af32data[i++] = (float) (ds.getElementDoubleAbs(it1.index + j) % dvr);
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				dvr = AbstractDataset.toReal(b);
 				is = result.getElementsPerItem();
 				double[] af64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						af64data[i++] = ds.getElementDoubleAbs(it1.index + j) % dvr;
 					}
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("remainder does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(bracketIfNecessary(ds).append('%').append(b).toString());
 		} else {
 			if (!(b instanceof AbstractDataset)) {
 				throw new IllegalArgumentException("Both arguments are not datasets");
 			}
 
 			AbstractDataset ds = (AbstractDataset) b;
 			final int dt = AbstractDataset.getBestDType(ds.getDtype(), AbstractDataset.getDTypeFromClass(a.getClass()));
 			result = AbstractDataset.zeros(ds, dt);
 			IndexIterator it1 = ds.getIterator();
 
 			double dvr, dvi;
 			long lv;
 
 			switch (dt) {
 			case AbstractDataset.INT8:
 				lv = AbstractDataset.toLong(a);
 				byte[] i8data = ((ByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i8data[i++] = (byte) (lv % ds.getElementLongAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT16:
 				lv = AbstractDataset.toLong(a);
 				short[] i16data = ((ShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i16data[i++] = (short) (lv % ds.getElementLongAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT32:
 				lv = AbstractDataset.toLong(a);
 				int[] i32data = ((IntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i32data[i++] = (int) (lv % ds.getElementLongAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.INT64:
 				lv = AbstractDataset.toLong(a);
 				long[] i64data = ((LongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					i64data[i++] = lv % ds.getElementLongAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.ARRAYINT8:
 				is = result.getElementsPerItem();
 				lv = AbstractDataset.toLong(a);
 				byte[] ai8data = ((CompoundByteDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai8data[i++] = (byte) (lv % ds.getElementLongAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT16:
 				is = result.getElementsPerItem();
 				lv = AbstractDataset.toLong(a);
 				short[] ai16data = ((CompoundShortDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai16data[i++] = (short) (lv % ds.getElementLongAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT32:
 				is = result.getElementsPerItem();
 				lv = AbstractDataset.toLong(a);
 				int[] ai32data = ((CompoundIntegerDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai32data[i++] = (int) (lv % ds.getElementLongAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYINT64:
 				is = result.getElementsPerItem();
 				lv = AbstractDataset.toLong(a);
 				long[] ai64data = ((CompoundLongDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						ai64data[i++] = lv % ds.getElementLongAbs(it1.index + j);
 					}
 				}
 				break;
 			case AbstractDataset.FLOAT32:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				if (dvi != 0) {
 					throw new IllegalArgumentException("remainder supports integer or real numerators only");
 				}
 				float[] f32data = ((FloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f32data[i++] = (float) (dvr % ds.getElementDoubleAbs(it1.index));
 				}
 				break;
 			case AbstractDataset.FLOAT64:
 				dvr = AbstractDataset.toReal(a);
 				dvi = AbstractDataset.toImag(a);
 				if (dvi != 0) {
 					throw new IllegalArgumentException("remainder supports integer or real numerators only");
 				}
 				double[] f64data = ((DoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					f64data[i++] = dvr % ds.getElementDoubleAbs(it1.index);
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT32:
 				dvr = AbstractDataset.toReal(a);
 				is = result.getElementsPerItem();
 				float[] af32data = ((CompoundFloatDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						af32data[i++] = (float) (dvr % ds.getElementDoubleAbs(it1.index + j));
 					}
 				}
 				break;
 			case AbstractDataset.ARRAYFLOAT64:
 				dvr = AbstractDataset.toReal(a);
 				is = result.getElementsPerItem();
 				double[] af64data = ((CompoundDoubleDataset) result).getData();
 
 				for (int i = 0; it1.hasNext();) {
 					for (int j = 0; j < is; j++) {
 						af64data[i++] = dvr % ds.getElementDoubleAbs(it1.index + j);
 					}
 				}
 				break;
 			default:
 				throw new UnsupportedOperationException("remainder does not support this dataset type");
 			}
 
 			// set the name based on the changes made
 			result.setName(new StringBuilder(a.toString()).append('%').append(bracketIfNecessary(ds)).toString());
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param a
 	 * @param b
 	 * @return floor division of a and b
 	 */
 	public static AbstractDataset floorDivide(final Object a, final Object b) {
 		return divide(a, b).ifloor();
 	}
 
 	/**
 	 * Find reciprocal from dataset
 	 * @param a
 	 * @return reciprocal dataset
 	 */
 	public static AbstractDataset reciprocal(final AbstractDataset a) {
 		return divide(1, a);
 	}
 
 	/**
 	 * @param a
 	 * @return a^*, complex conjugate of a
 	 */
 	public static AbstractDataset conjugate(final AbstractDataset a) {
 		int at = a.getDtype();
 		IndexIterator it1 = a.getIterator();
 
 		AbstractDataset result = AbstractDataset.zeros(a);
 
 		switch (at) {
 		case AbstractDataset.COMPLEX64:
 			float[] c64data = ((ComplexFloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				c64data[i++] = (float) a.getElementDoubleAbs(it1.index);
 				c64data[i++] = (float) -a.getElementDoubleAbs(it1.index+1);
 			}
 			result.setName(bracketIfNecessary(a).append("^*").toString());
 			break;
 		case AbstractDataset.COMPLEX128:
 			double[] c128data = ((ComplexDoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext();) {
 				c128data[i++] = a.getElementDoubleAbs(it1.index);
 				c128data[i++] = -a.getElementDoubleAbs(it1.index+1);
 			}
 			result.setName(bracketIfNecessary(a).append("^*").toString());
 			break;
 		default:
 			result = a;
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param a side of right-angled triangle
 	 * @param b side of right-angled triangle
 	 * @return hypotenuse of right-angled triangle: sqrt(a^2 + a^2)
 	 */
 	public static AbstractDataset hypot(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final int is = Math.max(a.getElementsPerItem(), b.getElementsPerItem());
 
 		if (is > 1)
 			throw new UnsupportedOperationException("hypot does not support multiple-element dataset");
 
 		final int rt = AbstractDataset.getBestDType(a.getDtype(), b.getDtype());
 
 		final AbstractDataset result = a.clone().cast(rt);
 
 		IndexIterator it1 = a.getIterator();
 		IndexIterator it2 = a.getIterator();
 
 		switch (rt) {
 		case AbstractDataset.BOOL:
 			boolean[] bdata = ((BooleanDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				bdata[i++] = Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index)) != 0;
 			}
 			break;
 		case AbstractDataset.INT8:
 			byte[] i8data = ((ByteDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i8data[i++] = (byte) Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.INT16:
 			short[] i16data = ((ShortDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i16data[i++] = (short) Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.INT32:
 			int[] i32data = ((IntegerDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i32data[i++] = (int) Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.INT64:
 			long[] i64data = ((LongDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i64data[i++] = (long) Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			float[] f32data = ((FloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				f32data[i++] = (float) Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			double[] f64data = ((DoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				f64data[i++] = Math.hypot(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("hypot does not support multiple-element dataset");
 		}
 
 		// set the name based on the changes made
 		result.setName(new StringBuilder("hypot(").append(a.getName()).append(", ").append(b.getName()).append(")").toString());
 
 		return result;
 	}
 
 	/**
 	 * @param a opposite side of right-angled triangle
 	 * @param b adjacent side of right-angled triangle
 	 * @return angle of triangle: atan(a/b)
 	 */
 	public static AbstractDataset arctan2(final AbstractDataset a, final AbstractDataset b) {
 		a.checkCompatibility(b);
 
 		final int is = Math.max(a.getElementsPerItem(), b.getElementsPerItem());
 
 		if (is > 1)
 			throw new UnsupportedOperationException("hypot does not support multiple-element dataset");
 
 		final int rt = AbstractDataset.getBestDType(a.getDtype(), b.getDtype());
 
 		final AbstractDataset result = a.clone().cast(rt);
 
 		IndexIterator it1 = a.getIterator();
 		IndexIterator it2 = b.getIterator();
 
 		switch (rt) {
 		case AbstractDataset.BOOL:
 			boolean[] bdata = ((BooleanDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				bdata[i++] = Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index)) != 0;
 			}
 			break;
 		case AbstractDataset.INT8:
 			byte[] i8data = ((ByteDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i8data[i++] = (byte) Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.INT16:
 			short[] i16data = ((ShortDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i16data[i++] = (short) Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.INT32:
 			int[] i32data = ((IntegerDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i32data[i++] = (int) Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.INT64:
 			long[] i64data = ((LongDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				i64data[i++] = (long) Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			float[] f32data = ((FloatDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				f32data[i++] = (float) Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			double[] f64data = ((DoubleDataset) result).getData();
 
 			for (int i = 0; it1.hasNext() && it2.hasNext();) {
 				f64data[i++] = Math.atan2(a.getElementDoubleAbs(it1.index), b.getElementDoubleAbs(it2.index));
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("atan2 does not support multiple-element dataset");
 		}
 
 		// set the name based on the changes made
 		result.setName(new StringBuilder("atan2(").append(a.getName()).append(", ").append(b.getName()).append(")").toString());
 
 		return result;
 	}
 
 	/**
 	 * Interpolated a value from 1D dataset
 	 * @param d
 	 * @param x0
 	 * @return linear interpolation
 	 */
 	public static double getLinear(final AbstractDataset d, final double x0) {
 		final int[] shape = d.getShape();
 		if (shape.length != 1)
 			throw new IllegalArgumentException("Only 1d datasets allowed");
 		double r = 0;
 		double f1, f2;
 		double u0;
 		int i0;
 
 		i0 = (int) Math.floor(x0);
 		u0 = x0 - i0;
 		if (i0 < 0 || i0 >= shape[0])
 			return r;
 
 		// use linear interpolation
 		f1 = d.getElementDoubleAbs(i0);
 		if (u0 > 0) {
 			if (i0 == shape[0] - 1)
 				return r;
 			f2 = d.getElementDoubleAbs(i0 + 1);
 			r = (1 - u0) * f1 + u0 * f2;
 		} else {
 			r = f1;
 		}
 		return r;
 	}
 
 	/**
 	 * Interpolated a value from 1D compound dataset
 	 * @param values linearly interpolated array
 	 * @param d
 	 * @param x0
 	 */
 	public static void getLinear(final double[] values, final AbstractCompoundDataset d, final double x0) {
 		final int[] shape = d.getShape();
 		if (shape.length != 1)
 			throw new IllegalArgumentException("Only 1d datasets allowed");
 		final int is = d.isize;
 		if (is != values.length)
 			throw new IllegalArgumentException("Output array length must match elements in item");
 		final double[] f1, f2;
 		final double u0;
 		final int i0;
 
 		i0 = (int) Math.floor(x0);
 		u0 = x0 - i0;
 		if (i0 < 0 || i0 >= shape[0]) {
 			Arrays.fill(values, 0);
 			return;
 		}
 
 		// use linear interpolation
 		if (u0 > 0) {
 			if (i0 == shape[0] - 1) {
 				Arrays.fill(values, 0);
 				return;
 			}
 			f1 = new double[is];
 			d.getDoubleArray(f1, i0);
 			f2 = new double[is];
 			d.getDoubleArray(f2, i0 + 1);
 			for (int j = 0; j < is; j++)
 				values[j] = (1 - u0) * f1[j] + u0 * f2[j];
 		} else {
 			d.getDoubleArray(values, i0);
 		}
 	}
 
 	/**
 	 * Interpolated a value from 2D dataset
 	 * @param d input dataset
 	 * @param x0 coordinate
 	 * @param x1 coordinate
 	 * @return bilinear interpolation
 	 */
 	public static double getBilinear(final IDataset d, final double x0, final double x1) {
 		final int[] s = d.getShape();
 		if (s.length != 2)
 			throw new IllegalArgumentException("Only 2d datasets allowed");
 		double r = 0;
 		final double f1, f2, f3, f4;
 		final double u1, u0;
 		final int i0, i1;
 
 		i0 = (int) Math.floor(x0);
 		i1 = (int) Math.floor(x1);
 		u0 = x0 - i0;
 		u1 = x1 - i1;
 		if (i0 < -1 || i0 >= s[0] || i1 < -1 || i1 >= s[1])
 			return r;
 		// use bilinear interpolation
 		f1 = (i0 < 0 || i1 < 0) ? 0 : d.getDouble(i0, i1);
 		if (u1 > 0) {
 			if (i1 == s[1] - 1)
 				return f1;
 			if (u0 > 0) {
 				if (i0 == s[0] - 1)
 					return f1;
 				f2 = (i0 < 0) ? 0 : d.getDouble(i0, i1 + 1);
 				f3 = d.getDouble(i0 + 1, i1 + 1);
 				f4 = (i1 < 0) ? 0 : d.getDouble(i0 + 1, i1);
 				r = (1 - u1) * (1 - u0) * f1 + u1 * (1 - u0) * f2 + u1 * u0 * f3 + (1 - u1) * u0 * f4;
 			} else {
 				f2 = (i0 < 0) ? 0 : d.getDouble(i0, i1 + 1);
 				r = (1 - u1) * f1 + u1 * f2;
 			}
 		} else { // exactly on axis 1
 			if (u0 > 0) {
 				if (i0 == s[0] - 1)
 					return f1;
 				f4 = (i1 < 0) ? 0 : d.getDouble(i0 + 1, i1);
 				r = (1 - u0) * f1 + u0 * f4;
 			} else { // exactly on axis 0
 				r = f1;
 			}
 		}
 		return r;
 	}
 	
 	/**
 	 * Interpolated a value from 2D dataset with mask
 	 * @param d input dataset
 	 * @param m mask dataset
 	 * @param x0 coordinate
 	 * @param x1 coordinate
 	 * @return bilinear interpolation
 	 */
 	public static double getBilinear(final IDataset d, final IDataset m, final double x0, final double x1) {
 		if (m == null)
 			return getBilinear(d, x0, x1);
 
 		final int[] s = d.getShape();
 		if (s.length != 2)
 			throw new IllegalArgumentException("Only 2d datasets allowed");
 		double r = 0;
 		final double f1, f2, f3, f4;
 		final double u1, u0;
 		final int i0, i1;
 
 		i0 = (int) Math.floor(x0);
 		i1 = (int) Math.floor(x1);
 		u0 = x0 - i0;
 		u1 = x1 - i1;
 		if (i0 < -1 || i0 >= s[0] || i1 < -1 || i1 >= s[1])
 			return r;
 		// use bilinear interpolation
 		f1 = (i0 < 0 || i1 < 0) ? 0 : d.getDouble(i0, i1) * m.getDouble(i0, i1);
 		if (u1 > 0) {
 			if (i1 == s[1] - 1)
 				return f1;
 			if (u0 > 0) {
 				if (i0 == s[0] - 1)
 					return f1;
 				f2 = (i0 < 0) ? 0 : d.getDouble(i0, i1 + 1) * m.getDouble(i0, i1 + 1);
 				f3 = d.getDouble(i0 + 1, i1 + 1) * m.getDouble(i0 + 1, i1 + 1);
 				f4 = (i1 < 0) ? 0 : d.getDouble(i0 + 1, i1) * m.getDouble(i0 + 1, i1);
 				r = (1 - u1) * (1 - u0) * f1 + u1 * (1 - u0) * f2 + u1 * u0 * f3 + (1 - u1) * u0 * f4;
 			} else {
 				f2 = (i0 < 0) ? 0 : d.getDouble(i0, i1 + 1) * m.getDouble(i0, i1 + 1);
 				r = (1 - u1) * f1 + u1 * f2;
 			}
 		} else { // exactly on axis 1
 			if (u0 > 0) {
 				if (i0 == s[0] - 1)
 					return f1;
 				f4 = (i1 < 0) ? 0 : d.getDouble(i0 + 1, i1) * m.getDouble(i0 + 1, i1);
 				r = (1 - u0) * f1 + u0 * f4;
 			} else { // exactly on axis 0
 				r = f1;
 			}
 		}
 		return r;
 	}
 
 	/**
 	 * Interpolated a value from 2D compound dataset
 	 * @param values bilinear interpolated array
 	 * @param d
 	 * @param x0
 	 * @param x1
 	 */
 	public static void getBilinear(final double[] values, final AbstractCompoundDataset d, final double x0, final double x1) {
 		final int[] s = d.getShape();
 		if (s.length != 2)
 			throw new IllegalArgumentException("Only 2d datasets allowed");
 		final int is = d.isize;
 		if (is != values.length)
 			throw new IllegalArgumentException("Output array length must match elements in item");
 		final double[] f1, f2, f3, f4;
 		final double u1, u0;
 		final int i0, i1;
 
 		i0 = (int) Math.floor(x0);
 		i1 = (int) Math.floor(x1);
 		u0 = x0 - i0;
 		u1 = x1 - i1;
 		if (i0 < -1 || i0 >= s[0] || i1 < -1 || i1 >= s[1]) {
 			Arrays.fill(values, 0);
 			return;
 		}
 		// use bilinear interpolation
 		f1 = new double[is];
 		if (i0 >= 0 && i1 >= 0)
 			d.getDoubleArray(f1, i0, i1);
 
 		if (u1 > 0) {
 			if (i1 == s[1] - 1) {
 				Arrays.fill(values, 0);
 				return;
 			}
 			if (u0 > 0) {
 				if (i0 == s[0] - 1) {
 					Arrays.fill(values, 0);
 					return;
 				}
 				f2 = new double[is];
 				f3 = new double[is];
 				f4 = new double[is];
 				if (i0 >= 0)
 					d.getDoubleArray(f2, i0, i1 + 1);
 				d.getDoubleArray(f3, i0 + 1, i1 + 1);
 				if (i1 >= 0)
 					d.getDoubleArray(f4, i0 + 1, i1);
 				for (int j = 0; j < is; j++)
 					values[j] = (1 - u1) * (1 - u0) * f1[j] + u1 * (1 - u0) * f2[j] + u1 * u0 * f3[j] + (1 - u1) * u0 * f4[j];
 			} else {
 				f2 = new double[is];
 				if (i0 >= 0)
 					d.getDoubleArray(f2, i0, i1 + 1);
 				for (int j = 0; j < is; j++)
 					values[j] = (1 - u1) * f1[j] + u1 * f2[j];
 			}
 		} else { // exactly on axis 1
 			if (u0 > 0) {
 				if (i0 == s[0] - 1) {
 					Arrays.fill(values, 0);
 					return;
 				}
 				f4 = new double[is];
 				if (i1 >= 0)
 					d.getDoubleArray(f4, i0 + 1, i1);
 				for (int j = 0; j < is; j++)
 					values[j] = (1 - u0) * f1[j] + u0 * f4[j];
 			} else { // exactly on axis 0
 				d.getDoubleArray(values, i0, i1);
 			}
 		}
 	}
 
 	/**
 	 * Create a dataset of the arguments from a complex dataset
 	 * @param a dataset
 	 * @return dataset of angles
 	 */
 	public static AbstractDataset angle(final AbstractDataset a) {
 		AbstractDataset ds = null;
 		int dt = a.getDtype();
 		IndexIterator it = a.getIterator();
 
 		switch (dt) {
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			float[] f32data = ((FloatDataset) ds).getData();
 			float[] c64data = ((ComplexFloatDataset) a).data;
 
 			for (int i = 0; it.hasNext();) {
 				f32data[i++] = (float) Math.atan2(c64data[it.index+1], c64data[it.index]);
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			double[] f64data = ((DoubleDataset) ds).getData();
 			double[] c128data = ((ComplexDoubleDataset) a).data;
 
 			for (int i = 0; it.hasNext();) {
 				f64data[i++] = Math.atan2(c128data[it.index+1], c128data[it.index]);
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("angle does not support this dataset type");
 		}
 
 		return ds;
 	}
 
 	/**
 	 * Create a phase only dataset. NB it will contain NaNs if there are any items with zero amplitude
 	 * @param a dataset
 	 * @param keepZeros if true then zero items are returned as zero rather than NaNs
 	 * @return complex dataset where items have unit amplitude
 	 */
 	public static AbstractDataset phaseAsComplexNumber(final AbstractDataset a, final boolean keepZeros) {
 		AbstractDataset ds = null;
 		int dt = a.getDtype();
 		IndexIterator it = a.getIterator();
 
 		switch (dt) {
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a);
 
 			float[] z64data = ((ComplexFloatDataset) ds).getData();
 			float[] c64data = ((ComplexFloatDataset) a).data;
 
 			if (keepZeros) {
 				for (int i = 0; it.hasNext();) {
 					double rr = c64data[it.index];
 					double ri = c64data[it.index+1];
 					double am = Math.hypot(rr, ri);
 					if (am == 0) {
 						z64data[i++] = 0;
 						z64data[i++] = 0;
 					} else {
 						z64data[i++] = (float) (rr/am);
 						z64data[i++] = (float) (ri/am);
 					}
 				}
 			} else {
 				for (int i = 0; it.hasNext();) {
 					double rr = c64data[it.index];
 					double ri = c64data[it.index+1];
 					double am = Math.hypot(rr, ri);
 					z64data[i++] = (float) (rr/am);
 					z64data[i++] = (float) (ri/am);
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a);
 
 			double[] z128data = ((ComplexDoubleDataset) ds).getData();
 			double[] c128data = ((ComplexDoubleDataset) a).data;
 
 			if (keepZeros) {
 				for (int i = 0; it.hasNext();) {
 					double rr = c128data[it.index];
 					double ri = c128data[it.index+1];
 					double am = Math.hypot(rr, ri);
 					if (am == 0) {
 						z128data[i++] = 0;
 						z128data[i++] = 0;
 					} else {
 						z128data[i++] = rr/am;
 						z128data[i++] = ri/am;
 					}
 				}
 			} else {
 				for (int i = 0; it.hasNext();) {
 					double rr = c128data[it.index];
 					double ri = c128data[it.index+1];
 					double am = Math.hypot(rr, ri);
 					z128data[i++] = rr/am;
 					z128data[i++] = ri/am;
 				}
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("Dataset is not of complex type");
 		}
 
 		return ds;
 	}
 
 	/**
 	 * generate binomial coefficients with negative sign:
 	 * <p>
 	 * <pre>
 	 *  (-1)^i n! / ( i! (n-i)! )
 	 * </pre>
 	 * @param n
 	 * @return array of coefficients
 	 */
 	private static int[] bincoeff(final int n) {
 		final int[] b = new int[n+1];
 		final int hn = n/2;
 
 		int bc = 1;
 		b[0] = bc;
 		for (int i = 1; i <= hn; i++) {
 			bc = -(bc*(n-i+1))/i;
 			b[i] = bc;
 		}
		if (n % 2 != 0) {
 			for (int i = hn+1; i <= n; i++) {
 				b[i] = -b[n-i];
 			}
 		} else {
 			for (int i = hn+1; i <= n; i++) {
 				b[i] = b[n-i];
 			}
 		}
 		return b;
 	}
 
 	/**
 	 * 1st order discrete difference of dataset along flattened dataset using finite difference
 	 * @param a is 1d dataset
 	 * @param out is 1d dataset
 	 */
 	private static void difference(final AbstractDataset a, final AbstractDataset out) {
 		final int isize = a.getElementsPerItem();
 
 		final IndexIterator it = a.getIterator();
 		if (!it.hasNext())
 			return;
 		int oi = it.index;
 
 		switch (a.getDtype()) {
 		case AbstractDataset.INT8:
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				oi8data[i++] = (byte) (i8data[it.index] - i8data[oi]);
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.INT16:
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				oi16data[i++] = (short) (i16data[it.index] - i16data[oi]);
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.INT32:
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				oi32data[i++] = i32data[it.index] - i32data[oi];
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.INT64:
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				oi64data[i++] = i64data[it.index] - i64data[oi];
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int k = 0; k < isize; k++) {
 					oai8data[i++] = (byte) (ai8data[it.index + k] - ai8data[oi++]);
 				}
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int k = 0; k < isize; k++) {
 					oai16data[i++] = (short) (ai16data[it.index + k] - ai16data[oi++]);
 				}
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int k = 0; k < isize; k++) {
 					oai32data[i++] = ai32data[it.index + k] - ai32data[oi++];
 				}
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int k = 0; k < isize; k++) {
 					oai64data[i++] = ai64data[it.index + k] - ai64data[oi++];
 				}
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				of32data[i++] = f32data[it.index] - f32data[oi];
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				of64data[i++] = f64data[it.index] - f64data[oi];
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				oc64data[i++] = c64data[it.index] - c64data[oi];
 				oc64data[i++] = c64data[it.index + 1] - c64data[oi + 1];
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				oc128data[i++] = c128data[it.index] - c128data[oi];
 				oc128data[i++] = c128data[it.index + 1] - c128data[oi + 1];
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int k = 0; k < isize; k++) {
 					oaf32data[i++] = af32data[it.index + k] - af32data[oi++];
 				}
 				oi = it.index;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) out).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int k = 0; k < isize; k++) {
 					oaf64data[i++] = af64data[it.index + k] - af64data[oi++];
 				}
 				oi = it.index;
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("difference does not support this dataset type");
 		}
 	}
 
 	/**
 	 * Get next set of indexes
 	 * @param it
 	 * @param indexes
 	 * @return true if there is more
 	 */
 	private static boolean nextIndexes(IndexIterator it, int[] indexes) {
 		if (!it.hasNext())
 			return false;
 		int m = indexes.length;
 		int i = 0;
 		for (i = 0; i < m - 1; i++) {
 			indexes[i] = indexes[i+1];
 		}
 		indexes[i] = it.index;
 		return true;
 	}
 
 	
 	/**
 	 * General order discrete difference of dataset along flattened dataset using finite difference
 	 * @param a is 1d dataset
 	 * @param out is 1d dataset
 	 * @param n order of difference
 	 */
 	private static void difference(final AbstractDataset a, final AbstractDataset out, final int n) {
 		if (n == 1) {
 			difference(a, out);
 			return;
 		}
 
 		final int isize = a.getElementsPerItem();
 
 		final int[] coeff = bincoeff(n);
 		final int m = n + 1;
 		final int[] indexes = new int[m]; // store for index values
 
 		final IndexIterator it = a.getIterator();
 		for (int i = 0; i < n; i++) {
 			indexes[i] = it.index;
 			it.hasNext();
 		}
 		indexes[n] = it.index;
 
 		switch (a.getDtype()) {
 		case AbstractDataset.INT8:
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				int ox = 0;
 				for (int j = 0; j < m; j++) {
 					ox += i8data[indexes[j]] * coeff[j];
 				}
 				oi8data[i++] = (byte) ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				int ox = 0;
 				for (int j = 0; j < m; j++) {
 					ox += i16data[indexes[j]] * coeff[j];
 				}
 				oi16data[i++] = (short) ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				int ox = 0;
 				for (int j = 0; j < m; j++) {
 					ox += i32data[indexes[j]] * coeff[j];
 				}
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				long ox = 0;
 				for (int j = 0; j < m; j++) {
 					ox += i64data[indexes[j]] * coeff[j];
 				}
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) out).getData();
 			int[] box = new int[isize];
 			for (int i = 0; nextIndexes(it, indexes);) {
 				Arrays.fill(box, 0);
 				for (int j = 0; j < m; j++) {
 					double c = coeff[j];
 					int l = indexes[j];
 					for (int k = 0; k < isize; k++) {
 						box[k] += ai8data[l++] * c;
 					}
 				}
 				for (int k = 0; k < isize; k++) {
 					oai8data[i++] = (byte) box[k];
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) out).getData();
 			int[] sox = new int[isize];
 			for (int i = 0; nextIndexes(it, indexes);) {
 				Arrays.fill(sox, 0);
 				for (int j = 0; j < m; j++) {
 					double c = coeff[j];
 					int l = indexes[j];
 					for (int k = 0; k < isize; k++) {
 						sox[k] += ai16data[l++] * c;
 					}
 				}
 				for (int k = 0; k < isize; k++) {
 					oai16data[i++] = (short) sox[k];
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) out).getData();
 			int[] iox = new int[isize];
 			for (int i = 0; nextIndexes(it, indexes);) {
 				Arrays.fill(iox, 0);
 				for (int j = 0; j < m; j++) {
 					double c = coeff[j];
 					int l = indexes[j];
 					for (int k = 0; k < isize; k++) {
 						iox[k] += ai32data[l++] * c;
 					}
 				}
 				for (int k = 0; k < isize; k++) {
 					oai32data[i++] = iox[k];
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) out).getData();
 			long[] lox = new long[isize];
 			for (int i = 0; nextIndexes(it, indexes);) {
 				Arrays.fill(lox, 0);
 				for (int j = 0; j < m; j++) {
 					double c = coeff[j];
 					int l = indexes[j];
 					for (int k = 0; k < isize; k++) {
 						lox[k] += ai64data[l++] * c;
 					}
 				}
 				for (int k = 0; k < isize; k++) {
 					oai64data[i++] = lox[k];
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				float ox = 0;
 				for (int j = 0; j < m; j++) {
 					ox += f32data[indexes[j]] * coeff[j];
 				}
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				double ox = 0;
 				for (int j = 0; j < m; j++) {
 					ox += f64data[indexes[j]] * coeff[j];
 				}
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				float ox = 0;
 				float oy = 0;
 				for (int j = 0; j < m; j++) {
 					int l = indexes[j];
 					ox += c64data[l++] * coeff[j];
 					oy += c64data[l] * coeff[j];
 				}
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) out).getData();
 			for (int i = 0; nextIndexes(it, indexes);) {
 				double ox = 0;
 				double oy = 0;
 				for (int j = 0; j < m; j++) {
 					int l = indexes[j];
 					ox += c128data[l++] * coeff[j];
 					oy += c128data[l] * coeff[j];
 				}
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) out).getData();
 			float[] fox = new float[isize];
 			for (int i = 0; nextIndexes(it, indexes);) {
 				Arrays.fill(fox, 0);
 				for (int j = 0; j < m; j++) {
 					double c = coeff[j];
 					int l = indexes[j];
 					for (int k = 0; k < isize; k++) {
 						fox[k] += af32data[l++] * c;
 					}
 				}
 				for (int k = 0; k < isize; k++) {
 					oaf32data[i++] = fox[k];
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) out).getData();
 			double[] dox = new double[isize];
 			for (int i = 0; nextIndexes(it, indexes);) {
 				Arrays.fill(dox, 0);
 				for (int j = 0; j < m; j++) {
 					double c = coeff[j];
 					int l = indexes[j];
 					for (int k = 0; k < isize; k++) {
 						dox[k] += af64data[l++] * c;
 					}
 				}
 				for (int k = 0; k < isize; k++) {
 					oaf64data[i++] = dox[k];
 				}
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("difference does not support multiple-element dataset");
 		}
 	}
 
 	/**
 	 * Discrete difference of dataset along axis using finite difference
 	 * @param a
 	 * @param n order of difference
 	 * @param axis
 	 * @return difference
 	 */
 	public static AbstractDataset difference(AbstractDataset a, final int n, int axis) {
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 		final int rank = a.getRank();
 		final int is = a.getElementsPerItem();
 
 		if (axis < 0) {
 			axis += rank;
 		}
 		if (axis < 0 || axis >= rank) {
 			throw new IllegalArgumentException("Axis is out of range");
 		}
 		
 		int[] nshape = a.getShape();
 		if (nshape[axis] <= n) {
 			nshape[axis] = 0;
 			return AbstractDataset.zeros(is, nshape, dt);
 		}
 
 		nshape[axis] -= n;
 		ds = AbstractDataset.zeros(is, nshape, dt);
 		if (rank == 1) {
 			difference(a, ds, n);
 		} else {
 			final AbstractDataset src = AbstractDataset.zeros(is, new int[] { a.getShapeRef()[axis] }, dt);
 			final AbstractDataset dest = AbstractDataset.zeros(is, new int[] { nshape[axis] }, dt);
 			final PositionIterator pi = a.getPositionIterator(axis);
 			final int[] pos = pi.getPos();
 			final boolean[] hit = pi.getOmit();
 			while (pi.hasNext()) {
 				a.copyItemsFromAxes(pos, hit, src);
 				difference(src, dest, n);
 				ds.setItemsOnAxes(pos, hit, dest.getBuffer());
 			}
 		}
 
 		return ds;
 	}
 
 	private static double SelectedMean(AbstractDataset data, int Min, int Max) {
 
 		double result = 0.0;
 		for (int i = Min, imax = data.getSize(); i <= Max; i++) {
 			// clip i appropriately, imagine that effectively the two ends continue
 			// straight out.
 			int pos = i;
 			if (pos < 0) {
 				pos = 0;
 			} else if (pos >= imax) {
 				pos = imax - 1;
 			}
 			result += data.getElementDoubleAbs(pos);
 		}
 
 		// now the sum is complete, average the values.
 		result /= (Max - Min) + 1;
 		return result;
 	}
 
 	private static void SelectedMeanArray(double[] out, AbstractDataset data, int Min, int Max) {
 		final int isize = out.length;
 		for (int j = 0; j < isize; j++)
 			out[j] = 0.;
 
 		for (int i = Min, imax = data.getSize(); i <= Max; i++) {
 			// clip i appropriately, imagine that effectively the two ends continue
 			// straight out.
 			int pos = i*isize;
 			if (pos < 0) {
 				pos = 0;
 			} else if (pos >= imax) {
 				pos = imax - isize;
 			}
 			for (int j = 0; j < isize; j++)
 				out[j] += data.getElementDoubleAbs(pos+j);
 		}
 
 		// now the sum is complete, average the values.
 		double norm = 1./ (Max - Min + 1.);
 		for (int j = 0; j < isize; j++)
 			out[j] *= norm;
 
 	}
 
 	/**
 	 * Calculates the derivative of a line described by two datasets (x,y) given a spread of n either
 	 * side of the point
 	 * 
 	 * @param x
 	 *            The x values of the function to take the derivative of.
 	 * @param y
 	 *            The y values of the function to take the derivative of.
 	 * @param n
 	 *            The spread the derivative is calculated from, i.e. the
 	 *            smoothing, the higher the value, the more smoothing occurs.
 	 * @return A dataset which contains all the derivative point for point.
 	 */
 	public static AbstractDataset derivative(AbstractDataset x, AbstractDataset y, int n) {
 		if (x.getRank() != 1 || y.getRank() != 1) {
 			throw new IllegalArgumentException("Only one dimensional dataset supported");
 		}
 		if (y.getSize() > x.getSize()) {
 			throw new IllegalArgumentException("Length of x dataset should be greater than or equal to y's");
 		}
 		int dtype = y.getDtype();
 		AbstractDataset result;
 		switch (dtype) {
 		case AbstractDataset.BOOL:
 		case AbstractDataset.INT8:
 		case AbstractDataset.INT16:
 		case AbstractDataset.ARRAYINT8:
 		case AbstractDataset.ARRAYINT16:
 			result = AbstractDataset.zeros(y, AbstractDataset.FLOAT32);
 			break;
 		case AbstractDataset.INT32:
 		case AbstractDataset.INT64:
 		case AbstractDataset.ARRAYINT32:
 		case AbstractDataset.ARRAYINT64:
 			result = AbstractDataset.zeros(y, AbstractDataset.FLOAT64);
 			break;
 		case AbstractDataset.FLOAT32:
 		case AbstractDataset.FLOAT64:
 		case AbstractDataset.COMPLEX64:
 		case AbstractDataset.COMPLEX128:
 		case AbstractDataset.ARRAYFLOAT32:
 		case AbstractDataset.ARRAYFLOAT64:
 			result = AbstractDataset.zeros(y);
 			break;
 		default:
 			throw new UnsupportedOperationException("derivative does not support multiple-element dataset");
 		}
 
 		final int isize = y.getElementsPerItem();
 		if (isize == 1) {
 			for (int i = 0, imax = x.getSize(); i < imax; i++) {
 				double LeftValue = SelectedMean(y, i - n, i - 1);
 				double RightValue = SelectedMean(y, i + 1, i + n);
 				double LeftPosition = SelectedMean(x, i - n, i - 1);
 				double RightPosition = SelectedMean(x, i + 1, i + n);
 
 				// now the values and positions are calculated, the derivative can be
 				// calculated.
 				result.set(((RightValue - LeftValue) / (RightPosition - LeftPosition)), i);
 			}
 		} else {
 			double[] leftValues = new double[isize];
 			double[] rightValues = new double[isize];
 			for (int i = 0, imax = x.getSize(); i < imax; i++) {
 				SelectedMeanArray(leftValues, y, i - n, i - 1);
 				SelectedMeanArray(rightValues, y, i + 1, i + n);
 				double delta = SelectedMean(x, i - n, i - 1);
 				delta = 1./(SelectedMean(x, i + 1, i + n) - delta);
 				for (int j = 0; j < isize; j++) {
 					rightValues[j] -= leftValues[j];
 					rightValues[j] *= delta;
 				}
 				result.set(rightValues, i);
 			}
 		}
 
 		// set the name based on the changes made
 		result.setName(y.getName() + "'");
 
 		return result;
 	}
 
 	/**
 	 * Discrete difference of dataset along axis using finite central difference
 	 * @param a
 	 * @param axis
 	 * @return difference
 	 */
 	public static AbstractDataset centralDifference(AbstractDataset a, int axis) {
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 		final int rank = a.getRank();
 		final int is = a.getElementsPerItem();
 
 		if (axis < 0) {
 			axis += rank;
 		}
 		if (axis < 0 || axis >= rank) {
 			throw new IllegalArgumentException("Axis is out of range");
 		}
 
 		final int len = a.getShapeRef()[axis];
 		if (len < 2) {
 			throw new IllegalArgumentException("Dataset should have a size > 1 along given axis");
 		}
 		ds = AbstractDataset.zeros(is, a.getShapeRef(), dt);
 		if (rank == 1) {
 			centralDifference(a, ds);
 		} else {
 			final AbstractDataset src = AbstractDataset.zeros(is, new int[] { len }, dt);
 			final AbstractDataset dest = AbstractDataset.zeros(is, new int[] { len }, dt);
 			final PositionIterator pi = a.getPositionIterator(axis);
 			final int[] pos = pi.getPos();
 			final boolean[] hit = pi.getOmit();
 			while (pi.hasNext()) {
 				a.copyItemsFromAxes(pos, hit, src);
 				centralDifference(src, dest);
 				ds.setItemsOnAxes(pos, hit, dest.getBuffer());
 			}
 		}
 
 		return ds;
 	}
 
 	/**
 	 * 1st order discrete difference of dataset along flattened dataset using central difference
 	 * @param a is 1d dataset
 	 * @param out is 1d dataset
 	 */
 	private static void centralDifference(final AbstractDataset a, final AbstractDataset out) {
 		final int isize = a.getElementsPerItem();
 		final int dt = a.getDtype();
 
 		final int nlen = (out.shape[0] - 1)*isize;
 		if (nlen < 1) {
 			throw new IllegalArgumentException("Dataset should have a size > 1 along given axis");
 		}
 		final IndexIterator it = a.getIterator();
 		if (!it.hasNext())
 			return;
 		int oi = it.index;
 		if (!it.hasNext())
 			return;
 		int pi = it.index;
 
 		switch (dt) {
 		case AbstractDataset.INT8:
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) out).getData();
 			oi8data[0] = (byte) (i8data[pi] - i8data[oi]);
 			for (int i = 1; it.hasNext(); i++) {
 				oi8data[i] = (byte) ((i8data[it.index] - i8data[oi])/2);
 				oi = pi;
 				pi = it.index;
 			}
 			oi8data[nlen] = (byte) (i8data[pi] - i8data[oi]);
 			break;
 		case AbstractDataset.INT16:
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) out).getData();
 			oi16data[0] = (short) (i16data[pi] - i16data[oi]);
 			for (int i = 1; it.hasNext(); i++) {
 				oi16data[i] = (short) ((i16data[it.index] - i16data[oi])/2);
 				oi = pi;
 				pi = it.index;
 			}
 			oi16data[nlen] = (short) (i16data[pi] - i16data[oi]);
 			break;
 		case AbstractDataset.INT32:
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) out).getData();
 			oi32data[0] = i32data[pi] - i32data[oi];
 			for (int i = 1; it.hasNext(); i++) {
 				oi32data[i] = (i32data[it.index] - i32data[oi])/2;
 				oi = pi;
 				pi = it.index;
 			}
 			oi32data[nlen] = i32data[pi] - i32data[oi];
 			break;
 		case AbstractDataset.INT64:
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) out).getData();
 			oi64data[0] = i64data[pi] - i64data[oi];
 			for (int i = 1; it.hasNext(); i++) {
 				oi64data[i] = (i64data[it.index] - i64data[oi])/2;
 				oi = pi;
 				pi = it.index;
 			}
 			oi64data[nlen] = i64data[pi] - i64data[oi];
 			break;
 		case AbstractDataset.ARRAYINT8:
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) out).getData();
 			for (int k = 0; k < isize; k++) {
 				oai8data[k] = (byte) (ai8data[pi+k] - ai8data[oi+k]);
 			}
 			for (int i = isize; it.hasNext();) {
 				int l = it.index;
 				for (int k = 0; k < isize; k++) {
 					oai8data[i++] = (byte) ((ai8data[l++] - ai8data[oi++])/2);
 				}
 				oi = pi;
 				pi = it.index;
 			}
 			for (int k = 0; k < isize; k++) {
 				oai8data[nlen+k] = (byte) (ai8data[pi+k] - ai8data[oi+k]);
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) out).getData();
 			for (int k = 0; k < isize; k++) {
 				oai16data[k] = (short) (ai16data[pi+k] - ai16data[oi+k]);
 			}
 			for (int i = isize; it.hasNext();) {
 				int l = it.index;
 				for (int k = 0; k < isize; k++) {
 					oai16data[i++] = (short) ((ai16data[l++] - ai16data[oi++])/2);
 				}
 				oi = pi;
 				pi = it.index;
 			}
 			for (int k = 0; k < isize; k++) {
 				oai16data[nlen+k] = (short) (ai16data[pi+k] - ai16data[oi+k]);
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) out).getData();
 			for (int k = 0; k < isize; k++) {
 				oai32data[k] = ai32data[pi+k] - ai32data[oi+k];
 			}
 			for (int i = isize; it.hasNext();) {
 				int l = it.index;
 				for (int k = 0; k < isize; k++) {
 					oai32data[i++] = (ai32data[l++] - ai32data[oi++])/2;
 				}
 				oi = pi;
 				pi = it.index;
 			}
 			for (int k = 0; k < isize; k++) {
 				oai32data[nlen+k] = ai32data[pi+k] - ai32data[oi+k];
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) out).getData();
 			for (int k = 0; k < isize; k++) {
 				oai64data[k] = ai64data[pi+k] - ai64data[oi+k];
 			}
 			for (int i = isize; it.hasNext();) {
 				int l = it.index;
 				for (int k = 0; k < isize; k++) {
 					oai64data[i++] = (ai64data[l++] - ai64data[oi++])/2;
 				}
 				oi = pi;
 				pi = it.index;
 			}
 			for (int k = 0; k < isize; k++) {
 				oai64data[nlen+k] = ai64data[pi+k] - ai64data[oi+k];
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) out).getData();
 			of32data[0] = f32data[pi] - f32data[oi];
 			for (int i = 1; it.hasNext(); i++) {
 				of32data[i] = (f32data[it.index] - f32data[oi])*0.5f;
 				oi = pi;
 				pi = it.index;
 			}
 			of32data[nlen] = f32data[pi] - f32data[oi];
 			break;
 		case AbstractDataset.FLOAT64:
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) out).getData();
 			of64data[0] = f64data[pi] - f64data[oi];
 			for (int i = 1; it.hasNext(); i++) {
 				of64data[i] = (f64data[it.index] - f64data[oi])*0.5f;
 				oi = pi;
 				pi = it.index;
 			}
 			of64data[nlen] = f64data[pi] - f64data[oi];
 			break;
 		case AbstractDataset.COMPLEX64:
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) out).getData();
 			oc64data[0] = c64data[pi] - c64data[oi];
 			oc64data[1] = c64data[pi+1] - c64data[oi+1];
 			for (int i = 2; it.hasNext();) {
 				oc64data[i++] = (c64data[it.index] - c64data[oi++])*0.5f;
 				oc64data[i++] = (c64data[it.index + 1] - c64data[oi])*0.5f;
 				oi = pi;
 				pi = it.index;
 			}
 			oc64data[nlen] = c64data[pi] - c64data[oi];
 			oc64data[nlen+1] = c64data[pi+1] - c64data[oi+1];
 			break;
 		case AbstractDataset.COMPLEX128:
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) out).getData();
 			oc128data[0] = c128data[pi] - c128data[oi];
 			oc128data[1] = c128data[pi+1] - c128data[oi+1];
 			for (int i = 2; it.hasNext();) {
 				oc128data[i++] = (c128data[it.index] - c128data[oi++])*0.5f;
 				oc128data[i++] = (c128data[it.index + 1] - c128data[oi])*0.5f;
 				oi = pi;
 				pi = it.index;
 			}
 			oc128data[nlen] = c128data[pi] - c128data[oi];
 			oc128data[nlen+1] = c128data[pi+1] - c128data[oi+1];
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) out).getData();
 			for (int k = 0; k < isize; k++) {
 				oaf32data[k] = af32data[pi+k] - af32data[oi+k];
 			}
 			for (int i = isize; it.hasNext();) {
 				int l = it.index;
 				for (int k = 0; k < isize; k++) {
 					oaf32data[i++] = (af32data[l++] - af32data[oi++])*0.5f;
 				}
 				oi = pi;
 				pi = it.index;
 			}
 			for (int k = 0; k < isize; k++) {
 				oaf32data[nlen+k] = af32data[pi+k] - af32data[oi+k];
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) out).getData();
 			for (int k = 0; k < isize; k++) {
 				oaf64data[k] = af64data[pi+k] - af64data[oi+k];
 			}
 			for (int i = isize; it.hasNext();) {
 				int l = it.index;
 				for (int k = 0; k < isize; k++) {
 					oaf64data[i++] = (af64data[l++] - af64data[oi++])*0.5;
 				}
 				oi = pi;
 				pi = it.index;
 			}
 			for (int k = 0; k < isize; k++) {
 				oaf64data[nlen+k] = af64data[pi+k] - af64data[oi+k];
 			}
 			break;
 		default:
 			throw new UnsupportedOperationException("difference does not support this dataset type");
 		}
 	}
 
 	/**
 	 * Calculate gradient (or partial derivatives) by central difference
 	 * @param y
 	 * @param x one or more datasets for dependent variables
 	 * @return a list of datasets (one for each dimension in y)
 	 */
 	public static List<AbstractDataset> gradient(AbstractDataset y, AbstractDataset... x) {
 		final int rank = y.getRank();
 
 		if (x.length > 0) {
 			if (x.length != rank) {
 				throw new IllegalArgumentException("Number of dependent datasets must be equal to rank of first argument");
 			}
 			for (int a = 0; a < rank; a++) {
 				int rx = x[a].shape.length;
 				if (rx != rank && rx != 1) {
 					throw new IllegalArgumentException("Dependent datasets must be 1-D or match rank of first argument");
 				}
 				if (rx == 1) {
 					if (y.shape[a] != x[a].shape[0]) {
 						throw new IllegalArgumentException("Length of dependent dataset must match axis length");
 					}
 				} else {
 					y.checkCompatibility(x[a]);
 				}
 			}
 		}
 
 		List<AbstractDataset> grad = new ArrayList<AbstractDataset>(rank);
 
 		for (int a = 0; a < rank; a++) {
 			AbstractDataset g = centralDifference(y, a);
 			grad.add(g);
 		}
 
 		if (x.length > 0) {
 			for (int a = 0; a < rank; a++) {
 				AbstractDataset g = grad.get(a);
 				AbstractDataset dx = x[a];
 				int r = dx.shape.length;
 				if (r == rank) {
 					g.idivide(centralDifference(dx, a));
 				} else {
 					final int dt = dx.getDtype();
 					final int is = dx.getElementsPerItem();
 					final AbstractDataset bdx = AbstractDataset.zeros(is, y.shape, dt);
 					final PositionIterator pi = y.getPositionIterator(a);
 					final int[] pos = pi.getPos();
 					final boolean[] hit = pi.getOmit();
 					dx = centralDifference(dx, 0);
 
 					while (pi.hasNext()) {
 						bdx.setItemsOnAxes(pos, hit, dx.getBuffer());
 					}
 					g.idivide(bdx);
 				}
 			}
 		}
 		return grad;
 	}
 
 // Start of generated code - see functions.txt and generatefunctions.py
 	/**
 	 * sin - evaluate the sine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset sin(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.sin(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.sin(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.sin(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.sin(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.sin(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.sin(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.sin(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.sin(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.sin(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.sin(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.sin(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.sin(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.sin(ix)*Math.cosh(iy));
 				oy = (float) (Math.cos(ix)*Math.sinh(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.sin(ix)*Math.cosh(iy));
 				oy = (double) (Math.cos(ix)*Math.sinh(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("sin supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "sin");
 		return ds;
 	}
 
 	/**
 	 * cos - evaluate the cosine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset cos(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.cos(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.cos(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.cos(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.cos(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.cos(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.cos(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.cos(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.cos(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.cos(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.cos(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.cos(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.cos(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.cos(ix)*Math.cosh(iy));
 				oy = (float) (-Math.sin(ix)*Math.sinh(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.cos(ix)*Math.cosh(iy));
 				oy = (double) (-Math.sin(ix)*Math.sinh(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("cos supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "cos");
 		return ds;
 	}
 
 	/**
 	 * tan - evaluate the tangent function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset tan(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.tan(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.tan(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.tan(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.tan(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.tan(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.tan(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.tan(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.tan(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.tan(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.tan(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.tan(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.tan(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float x;
 				float y;
 				float tf;
 				float ox;
 				float oy;
 				x = (float) (2.*ix);
 				y = (float) (2.*iy);
 				tf = (float) (1./(Math.cos(x)+Math.cosh(y)));
 				ox = (float) (tf*Math.sin(x));
 				oy = (float) (tf*Math.sinh(y));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double x;
 				double y;
 				double tf;
 				double ox;
 				double oy;
 				x = (double) (2.*ix);
 				y = (double) (2.*iy);
 				tf = (double) (1./(Math.cos(x)+Math.cosh(y)));
 				ox = (double) (tf*Math.sin(x));
 				oy = (double) (tf*Math.sinh(y));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("tan supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "tan");
 		return ds;
 	}
 
 	/**
 	 * arcsin - evaluate the inverse sine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset arcsin(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.asin(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.asin(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.asin(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.asin(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.asin(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.asin(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.asin(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.asin(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.asin(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.asin(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.asin(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.asin(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(ix, iy).asin();
 				ox = (float) (tz.getReal());
 				oy = (float) (tz.getImaginary());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(ix, iy).asin();
 				ox = (double) (tz.getReal());
 				oy = (double) (tz.getImaginary());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("arcsin supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "arcsin");
 		return ds;
 	}
 
 	/**
 	 * arccos - evaluate the inverse cosine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset arccos(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.acos(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.acos(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.acos(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.acos(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.acos(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.acos(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.acos(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.acos(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.acos(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.acos(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.acos(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.acos(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(ix, iy).acos();
 				ox = (float) (tz.getReal());
 				oy = (float) (tz.getImaginary());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(ix, iy).acos();
 				ox = (double) (tz.getReal());
 				oy = (double) (tz.getImaginary());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("arccos supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "arccos");
 		return ds;
 	}
 
 	/**
 	 * arctan - evaluate the inverse tangent function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset arctan(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.atan(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.atan(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.atan(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.atan(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.atan(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.atan(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.atan(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.atan(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.atan(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.atan(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.atan(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.atan(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(ix, iy).atan();
 				ox = (float) (tz.getReal());
 				oy = (float) (tz.getImaginary());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(ix, iy).atan();
 				ox = (double) (tz.getReal());
 				oy = (double) (tz.getImaginary());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("arctan supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "arctan");
 		return ds;
 	}
 
 	/**
 	 * sinh - evaluate the hyperbolic sine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset sinh(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.sinh(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.sinh(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.sinh(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.sinh(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.sinh(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.sinh(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.sinh(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.sinh(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.sinh(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.sinh(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.sinh(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.sinh(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.sinh(ix)*Math.cos(iy));
 				oy = (float) (Math.cosh(ix)*Math.sin(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.sinh(ix)*Math.cos(iy));
 				oy = (double) (Math.cosh(ix)*Math.sin(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("sinh supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "sinh");
 		return ds;
 	}
 
 	/**
 	 * cosh - evaluate the hyperbolic cosine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset cosh(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.cosh(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.cosh(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.cosh(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.cosh(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.cosh(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.cosh(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.cosh(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.cosh(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.cosh(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.cosh(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.cosh(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.cosh(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.cosh(ix)*Math.cos(iy));
 				oy = (float) (Math.sinh(ix)*Math.sin(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.cosh(ix)*Math.cos(iy));
 				oy = (double) (Math.sinh(ix)*Math.sin(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("cosh supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "cosh");
 		return ds;
 	}
 
 	/**
 	 * tanh - evaluate the tangent hyperbolic function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset tanh(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.tanh(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.tanh(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.tanh(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.tanh(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.tanh(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.tanh(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.tanh(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.tanh(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.tanh(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.tanh(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.tanh(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.tanh(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float tx;
 				float ty;
 				float tf;
 				float ox;
 				float oy;
 				tx = (float) (2.*ix);
 				ty = (float) (2.*iy);
 				tf = (float) (1./(Math.cos(tx)+Math.cosh(ty)));
 				ox = (float) (tf*Math.sinh(tx));
 				oy = (float) (tf*Math.sin(ty));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double tx;
 				double ty;
 				double tf;
 				double ox;
 				double oy;
 				tx = (double) (2.*ix);
 				ty = (double) (2.*iy);
 				tf = (double) (1./(Math.cos(tx)+Math.cosh(ty)));
 				ox = (double) (tf*Math.sinh(tx));
 				oy = (double) (tf*Math.sin(ty));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("tanh supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "tanh");
 		return ds;
 	}
 
 	/**
 	 * arcsinh - evaluate the inverse hyperbolic sine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset arcsinh(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix + Math.sqrt(ix*ix + 1)));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(-iy, ix).asin();
 				ox = (float) (tz.getImaginary());
 				oy = (float) (-tz.getReal());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(-iy, ix).asin();
 				ox = (double) (tz.getImaginary());
 				oy = (double) (-tz.getReal());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("arcsinh supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "arcsinh");
 		return ds;
 	}
 
 	/**
 	 * arccosh - evaluate the inverse hyperbolic cosine function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset arccosh(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix + Math.sqrt(ix*ix - 1)));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(-iy, ix).acos();
 				ox = (float) (tz.getImaginary());
 				oy = (float) (-tz.getReal());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(-iy, ix).acos();
 				ox = (double) (tz.getImaginary());
 				oy = (double) (-tz.getReal());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("arccosh supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "arccosh");
 		return ds;
 	}
 
 	/**
 	 * arctanh - evaluate the inverse hyperbolic tangent function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset arctanh(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (0.5*Math.log((1 + ix)/(1 - ix)));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (0.5*Math.log((1 + ix)/(1 - ix)));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (0.5*Math.log((1 + ix)/(1 - ix)));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (0.5*Math.log((1 + ix)/(1 - ix)));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (0.5*Math.log((1 + ix)/(1 - ix)));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (0.5*Math.log((1 + ix)/(1 - ix)));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (0.5*Math.log((1 + ix)/(1 - ix)));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(-iy, ix).atan();
 				ox = (float) (tz.getImaginary());
 				oy = (float) (-tz.getReal());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(-iy, ix).atan();
 				ox = (double) (tz.getImaginary());
 				oy = (double) (-tz.getReal());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("arctanh supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "arctanh");
 		return ds;
 	}
 
 	/**
 	 * log - evaluate the logarithm function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset log(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.log(Math.hypot(ix, iy)));
 				oy = (float) (Math.atan2(iy, ix));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.log(Math.hypot(ix, iy)));
 				oy = (double) (Math.atan2(iy, ix));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("log supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "log");
 		return ds;
 	}
 
 	/**
 	 * log2 - evaluate the logarithm function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset log2(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix)/Math.log(2.));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix)/Math.log(2.));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix)/Math.log(2.));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix)/Math.log(2.));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix)/Math.log(2.));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix)/Math.log(2.));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix)/Math.log(2.));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix)/Math.log(2.));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.log(ix)/Math.log(2.));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.log(ix)/Math.log(2.));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.log(ix)/Math.log(2.));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log(ix)/Math.log(2.));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.log(Math.hypot(ix, iy))/Math.log(2.));
 				oy = (float) (Math.atan2(iy, ix));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.log(Math.hypot(ix, iy))/Math.log(2.));
 				oy = (double) (Math.atan2(iy, ix));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("log2 supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "log2");
 		return ds;
 	}
 
 	/**
 	 * log10 - evaluate the logarithm function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset log10(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.log10(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.log10(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.log10(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.log10(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.log10(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.log10(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.log10(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log10(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.log10(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.log10(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.log10(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log10(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.log10(Math.hypot(ix, iy)));
 				oy = (float) (Math.atan2(iy, ix));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.log10(Math.hypot(ix, iy)));
 				oy = (double) (Math.atan2(iy, ix));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("log10 supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "log10");
 		return ds;
 	}
 
 	/**
 	 * log1p - evaluate the logarithm function of 1 plus on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset log1p(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.log1p(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.log1p(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.log1p(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.log1p(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.log1p(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.log1p(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.log1p(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log1p(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.log1p(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.log1p(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.log1p(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.log1p(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (0.5*Math.log1p(ix*ix + 2.*ix + iy*iy));
 				oy = (float) (Math.atan2(iy, ix+1));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (0.5*Math.log1p(ix*ix + 2.*ix + iy*iy));
 				oy = (double) (Math.atan2(iy, ix+1));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("log1p supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "log1p");
 		return ds;
 	}
 
 	/**
 	 * exp - evaluate the exponential function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset exp(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.exp(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.exp(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.exp(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.exp(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.exp(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.exp(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.exp(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.exp(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.exp(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.exp(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.exp(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.exp(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float tf;
 				float ox;
 				float oy;
 				tf = (float) (Math.exp(ix));
 				ox = (float) (tf*Math.cos(iy));
 				oy = (float) (tf*Math.sin(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double tf;
 				double ox;
 				double oy;
 				tf = (double) (Math.exp(ix));
 				ox = (double) (tf*Math.cos(iy));
 				oy = (double) (tf*Math.sin(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("exp supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "exp");
 		return ds;
 	}
 
 	/**
 	 * expm1 - evaluate the exponential function - 1 on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset expm1(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.expm1(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.expm1(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.expm1(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.expm1(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.expm1(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.expm1(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.expm1(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.expm1(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.expm1(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.expm1(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.expm1(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.expm1(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float tf;
 				float ox;
 				float oy;
 				tf = (float) (Math.expm1(ix));
 				ox = (float) (tf*Math.cos(iy));
 				oy = (float) (tf*Math.sin(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double tf;
 				double ox;
 				double oy;
 				tf = (double) (Math.expm1(ix));
 				ox = (double) (tf*Math.cos(iy));
 				oy = (double) (tf*Math.sin(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("expm1 supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "expm1");
 		return ds;
 	}
 
 	/**
 	 * sqrt - evaluate the square root function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset sqrt(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.sqrt(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.sqrt(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.sqrt(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.sqrt(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.sqrt(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.sqrt(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.sqrt(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.sqrt(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.sqrt(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.sqrt(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.sqrt(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.sqrt(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(ix, iy).sqrt();
 				ox = (float) (tz.getReal());
 				oy = (float) (tz.getImaginary());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(ix, iy).sqrt();
 				ox = (double) (tz.getReal());
 				oy = (double) (tz.getImaginary());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("sqrt supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "sqrt");
 		return ds;
 	}
 
 	/**
 	 * cbrt - evaluate the cube root function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset cbrt(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.cbrt(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.cbrt(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.cbrt(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.cbrt(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.cbrt(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.cbrt(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.cbrt(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.cbrt(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.cbrt(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.cbrt(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.cbrt(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.cbrt(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				Complex tz;
 				float ox;
 				float oy;
 				tz = new Complex(ix, iy).pow(new Complex(1./3.,0));
 				ox = (float) (tz.getReal());
 				oy = (float) (tz.getImaginary());
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				Complex tz;
 				double ox;
 				double oy;
 				tz = new Complex(ix, iy).pow(new Complex(1./3.,0));
 				ox = (double) (tz.getReal());
 				oy = (double) (tz.getImaginary());
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("cbrt supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "cbrt");
 		return ds;
 	}
 
 	/**
 	 * square - square each element
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset square(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (ix*ix);
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (ix*ix);
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (ix*ix);
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (ix*ix);
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (ix*ix);
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (ix*ix);
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (ix*ix);
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (ix*ix);
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (ix*ix);
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (ix*ix);
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (ix*ix);
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (ix*ix);
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (ix*ix - iy*iy);
 				oy = (float) (2.*ix*iy);
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (ix*ix - iy*iy);
 				oy = (double) (2.*ix*iy);
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("square supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "square");
 		return ds;
 	}
 
 	/**
 	 * floor - evaluate the floor function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset floor(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (ix);
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (ix);
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (ix);
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (ix);
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (ix);
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (ix);
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (ix);
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (ix);
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.floor(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.floor(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.floor(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.floor(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.floor(ix));
 				oy = (float) (Math.floor(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.floor(ix));
 				oy = (double) (Math.floor(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("floor supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "floor");
 		return ds;
 	}
 
 	/**
 	 * ceil - evaluate the ceiling function on each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset ceil(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (ix);
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (ix);
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (ix);
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (ix);
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (ix);
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (ix);
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (ix);
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (ix);
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.ceil(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.ceil(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.ceil(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.ceil(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.ceil(ix));
 				oy = (float) (Math.ceil(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.ceil(ix));
 				oy = (double) (Math.ceil(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("ceil supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "ceil");
 		return ds;
 	}
 
 	/**
 	 * rint - round each element of the dataset
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset rint(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (ix);
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (ix);
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (ix);
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (ix);
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (ix);
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (ix);
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (ix);
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (ix);
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.rint(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.rint(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.rint(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.rint(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.rint(ix));
 				oy = (float) (Math.rint(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.rint(ix));
 				oy = (double) (Math.rint(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("rint supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "rint");
 		return ds;
 	}
 
 	/**
 	 * toDegrees - convert to degrees
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset toDegrees(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.toDegrees(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.toDegrees(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.toDegrees(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.toDegrees(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.toDegrees(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.toDegrees(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.toDegrees(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.toDegrees(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.toDegrees(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.toDegrees(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.toDegrees(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.toDegrees(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.toDegrees(ix));
 				oy = (float) (Math.toDegrees(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.toDegrees(ix));
 				oy = (double) (Math.toDegrees(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("toDegrees supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "toDegrees");
 		return ds;
 	}
 
 	/**
 	 * toRadians - convert to radians
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset toRadians(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final float[] oi8data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				float ox;
 				ox = (float) (Math.toRadians(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final short[] i16data = ((ShortDataset) a).data;
 			final float[] oi16data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				float ox;
 				ox = (float) (Math.toRadians(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final double[] oi32data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				double ox;
 				ox = (double) (Math.toRadians(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final double[] oi64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				double ox;
 				ox = (double) (Math.toRadians(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final float[] oai8data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					float ox;
 					ox = (float) (Math.toRadians(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final float[] oai16data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					float ox;
 					ox = (float) (Math.toRadians(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final double[] oai32data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					double ox;
 					ox = (double) (Math.toRadians(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final double[] oai64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					double ox;
 					ox = (double) (Math.toRadians(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.toRadians(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.toRadians(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.toRadians(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.toRadians(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.toRadians(ix));
 				oy = (float) (Math.toRadians(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.toRadians(ix));
 				oy = (double) (Math.toRadians(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("toRadians supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "toRadians");
 		return ds;
 	}
 
 	/**
 	 * signum - sign of each element
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset signum(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (ix > 0 ? 1 : (ix < 0 ? -1 : 0));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.signum(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.signum(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.signum(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.signum(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (Math.signum(ix));
 				oy = (float) (Math.signum(iy));
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (Math.signum(ix));
 				oy = (double) (Math.signum(iy));
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("signum supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "signum");
 		return ds;
 	}
 
 	/**
 	 * abs - absolute value of each element
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset abs(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (Math.abs(ix));
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (Math.abs(ix));
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (Math.abs(ix));
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (Math.abs(ix));
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (Math.abs(ix));
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (Math.abs(ix));
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (Math.abs(ix));
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (Math.abs(ix));
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (Math.abs(ix));
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (Math.abs(ix));
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (Math.abs(ix));
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (Math.abs(ix));
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				ox = (float) (Math.hypot(ix, iy));
 				oc64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				ox = (double) (Math.hypot(ix, iy));
 				oc128data[i++] = ox;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("abs supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "abs");
 		return ds;
 	}
 
 	/**
 	 * negative - negative value of each element
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset negative(final AbstractDataset a) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				ox = (byte) (-ix);
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				ox = (short) (-ix);
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				ox = (long) (-ix);
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				ox = (int) (-ix);
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					ox = (byte) (-ix);
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					ox = (short) (-ix);
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					ox = (long) (-ix);
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					ox = (int) (-ix);
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				ox = (float) (-ix);
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				ox = (double) (-ix);
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					ox = (float) (-ix);
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					ox = (double) (-ix);
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.COMPLEX64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX64);
 			final float[] c64data = ((ComplexFloatDataset) a).data;
 			final float[] oc64data = ((ComplexFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = c64data[it.index];
 				final float iy = c64data[it.index+1];
 				float ox;
 				float oy;
 				ox = (float) (-ix);
 				oy = (float) (-iy);
 				oc64data[i++] = ox;
 				oc64data[i++] = oy;
 			}
 			break;
 		case AbstractDataset.COMPLEX128:
 			ds = AbstractDataset.zeros(a, AbstractDataset.COMPLEX128);
 			final double[] c128data = ((ComplexDoubleDataset) a).data;
 			final double[] oc128data = ((ComplexDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = c128data[it.index];
 				final double iy = c128data[it.index+1];
 				double ox;
 				double oy;
 				ox = (double) (-ix);
 				oy = (double) (-iy);
 				oc128data[i++] = ox;
 				oc128data[i++] = oy;
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("negative supports integer, compound integer, real, compound real, complex datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "negative");
 		return ds;
 	}
 
 	/**
 	 * clip - clip elements to limits
 	 * @param a
 	 * @return dataset
 	 */
 	@SuppressWarnings("cast")
 	public static AbstractDataset clip(final AbstractDataset a, final Object pa, final Object pb) {
 		final int isize;
 		final IndexIterator it = a.getIterator();
 		AbstractDataset ds;
 		final int dt = a.getDtype();
 		final double pax = AbstractDataset.toReal(pa);
 		final double pbx = AbstractDataset.toReal(pb);
 
 		switch(dt) {
 		case AbstractDataset.INT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT8);
 			final byte[] i8data = ((ByteDataset) a).data;
 			final byte[] oi8data = ((ByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final byte ix = i8data[it.index];
 				byte ox;
 				if (ix < pax)
 					ox = (byte) (pax);
 				else if (ix > pbx)
 					ox = (byte) (pbx);
 				else
 					ox = (byte) (ix);
 				oi8data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT16);
 			final short[] i16data = ((ShortDataset) a).data;
 			final short[] oi16data = ((ShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final short ix = i16data[it.index];
 				short ox;
 				if (ix < pax)
 					ox = (short) (pax);
 				else if (ix > pbx)
 					ox = (short) (pbx);
 				else
 					ox = (short) (ix);
 				oi16data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT64);
 			final long[] i64data = ((LongDataset) a).data;
 			final long[] oi64data = ((LongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final long ix = i64data[it.index];
 				long ox;
 				if (ix < pax)
 					ox = (long) (pax);
 				else if (ix > pbx)
 					ox = (long) (pbx);
 				else
 					ox = (long) (ix);
 				oi64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.INT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.INT32);
 			final int[] i32data = ((IntegerDataset) a).data;
 			final int[] oi32data = ((IntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final int ix = i32data[it.index];
 				int ox;
 				if (ix < pax)
 					ox = (int) (pax);
 				else if (ix > pbx)
 					ox = (int) (pbx);
 				else
 					ox = (int) (ix);
 				oi32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYINT8:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT8);
 			isize = a.getElementsPerItem();
 			final byte[] ai8data = ((CompoundByteDataset) a).data;
 			final byte[] oai8data = ((CompoundByteDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final byte ix = ai8data[it.index+j];
 					byte ox;
 					if (ix < pax)
 						ox = (byte) (pax);
 					else if (ix > pbx)
 						ox = (byte) (pbx);
 					else
 						ox = (byte) (ix);
 					oai8data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT16:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT16);
 			isize = a.getElementsPerItem();
 			final short[] ai16data = ((CompoundShortDataset) a).data;
 			final short[] oai16data = ((CompoundShortDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final short ix = ai16data[it.index+j];
 					short ox;
 					if (ix < pax)
 						ox = (short) (pax);
 					else if (ix > pbx)
 						ox = (short) (pbx);
 					else
 						ox = (short) (ix);
 					oai16data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT64);
 			isize = a.getElementsPerItem();
 			final long[] ai64data = ((CompoundLongDataset) a).data;
 			final long[] oai64data = ((CompoundLongDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final long ix = ai64data[it.index+j];
 					long ox;
 					if (ix < pax)
 						ox = (long) (pax);
 					else if (ix > pbx)
 						ox = (long) (pbx);
 					else
 						ox = (long) (ix);
 					oai64data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYINT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYINT32);
 			isize = a.getElementsPerItem();
 			final int[] ai32data = ((CompoundIntegerDataset) a).data;
 			final int[] oai32data = ((CompoundIntegerDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final int ix = ai32data[it.index+j];
 					int ox;
 					if (ix < pax)
 						ox = (int) (pax);
 					else if (ix > pbx)
 						ox = (int) (pbx);
 					else
 						ox = (int) (ix);
 					oai32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.FLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT32);
 			final float[] f32data = ((FloatDataset) a).data;
 			final float[] of32data = ((FloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final float ix = f32data[it.index];
 				float ox;
 				if (Double.isNaN(ix))
 					ox = (float) ((pax+pbx)/2.);
 				else if (ix < pax)
 					ox = (float) (pax);
 				else if (ix > pbx)
 					ox = (float) (pbx);
 				else
 					ox = (float) (ix);
 				of32data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.FLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.FLOAT64);
 			final double[] f64data = ((DoubleDataset) a).data;
 			final double[] of64data = ((DoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				final double ix = f64data[it.index];
 				double ox;
 				if (Double.isNaN(ix))
 					ox = (double) ((pax+pbx)/2.);
 				else if (ix < pax)
 					ox = (double) (pax);
 				else if (ix > pbx)
 					ox = (double) (pbx);
 				else
 					ox = (double) (ix);
 				of64data[i++] = ox;
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT32:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT32);
 			isize = a.getElementsPerItem();
 			final float[] af32data = ((CompoundFloatDataset) a).data;
 			final float[] oaf32data = ((CompoundFloatDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final float ix = af32data[it.index+j];
 					float ox;
 					if (Double.isNaN(ix))
 						ox = (float) ((pax+pbx)/2.);
 					else if (ix < pax)
 						ox = (float) (pax);
 					else if (ix > pbx)
 						ox = (float) (pbx);
 					else
 						ox = (float) (ix);
 					oaf32data[i++] = ox;
 				}
 			}
 			break;
 		case AbstractDataset.ARRAYFLOAT64:
 			ds = AbstractDataset.zeros(a, AbstractDataset.ARRAYFLOAT64);
 			isize = a.getElementsPerItem();
 			final double[] af64data = ((CompoundDoubleDataset) a).data;
 			final double[] oaf64data = ((CompoundDoubleDataset) ds).getData();
 			for (int i = 0; it.hasNext();) {
 				for (int j = 0; j < isize; j++) {
 					final double ix = af64data[it.index+j];
 					double ox;
 					if (Double.isNaN(ix))
 						ox = (double) ((pax+pbx)/2.);
 					else if (ix < pax)
 						ox = (double) (pax);
 					else if (ix > pbx)
 						ox = (double) (pbx);
 					else
 						ox = (double) (ix);
 					oaf64data[i++] = ox;
 				}
 			}
 			break;
 		default:
 			throw new IllegalArgumentException("clip supports integer, compound integer, real, compound real datasets only");
 		}
 
 		ds.setName(a.getName());
 		addFunctionName(ds, "clip");
 		return ds;
 	}
 }
