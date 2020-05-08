 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
 *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
 *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
 *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
 *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.math;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
import org.jbasics.arrays.ArrayIterator;
 import org.jbasics.checker.ContractCheck;
 
 public class BigDecimalMatrix implements Iterable<Collection<BigDecimal>> /* , Comparable<BigDecimalMatrix> */{
 	private final int rows, columns;
 	private final BigDecimal[][] matrix;
 	private transient Collection<BigDecimal>[] iterables;
 
 	public static BigDecimalMatrixBuilder create() {
 		return new BigDecimalMatrixBuilder();
 	}
 
 	public static BigDecimalMatrix createColumnVector(final Number... values) {
 		return new BigDecimalMatrix(ContractCheck.mustNotBeNullOrEmpty(values, "values").length, 1, values);
 	}
 
 	public static BigDecimalMatrix createRowVector(final Number... values) {
 		return new BigDecimalMatrix(1, ContractCheck.mustNotBeNullOrEmpty(values, "values").length, values);
 	}
 
 	public static BigDecimalMatrix createIdentityMatrix(final int size) {
 		final BigDecimalMatrix result = new BigDecimalMatrix(size, size);
 		for (int i = 0; i < size; i++) {
 			result.matrix[i][i] = BigDecimal.ONE;
 		}
 		return result;
 	}
 
 	public static BigDecimalMatrix createRandomMatrix(final int rows, final int columns) {
 		return BigDecimalMatrix.createRandomMatrix(rows, columns, new JavaRandomNumberSequence());
 	}
 
 	public static BigDecimalMatrix createRandomMatrix(final int rows, final int columns, final RandomNumberSequence<? extends Number> r) {
 		final BigDecimalMatrix result = new BigDecimalMatrix(rows, columns);
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < columns; j++) {
 				result.matrix[i][j] = NumberConverter.toBigDecimal(r.nextRandomNumber());
 			}
 		}
 		return result;
 	}
 
 	public static BigDecimalMatrix createRandomColumnVector(final int size) {
 		return BigDecimalMatrix.createRandomMatrix(ContractCheck.mustBeInRange(size, 0, Integer.MAX_VALUE, "size"), 1); //$NON-NLS-1$
 	}
 
 	public static BigDecimalMatrix createRandomRowVector(final int size) {
 		return BigDecimalMatrix.createRandomMatrix(1, ContractCheck.mustBeInRange(size, 0, Integer.MAX_VALUE, "size")); //$NON-NLS-1$
 	}
 
 	public static BigDecimalMatrix createRandomColumnVector(final int size, final RandomNumberSequence<? extends Number> r) {
 		return BigDecimalMatrix.createRandomMatrix(ContractCheck.mustBeInRange(size, 0, Integer.MAX_VALUE, "size"), 1, r); //$NON-NLS-1$
 	}
 
 	public static BigDecimalMatrix createRandomRowVector(final int size, final RandomNumberSequence<? extends Number> r) {
 		return BigDecimalMatrix.createRandomMatrix(1, ContractCheck.mustBeInRange(size, 0, Integer.MAX_VALUE, "size"), r); //$NON-NLS-1$
 	}
 
 	public BigDecimalMatrix(final int rows, final int columns, final Number... values) {
 		this.rows = ContractCheck.mustBeInRange(rows, 1, Integer.MAX_VALUE, "rows"); //$NON-NLS-1$
 		this.columns = ContractCheck.mustBeInRange(columns, 1, Integer.MAX_VALUE, "columns"); //$NON-NLS-1$
 		this.matrix = new BigDecimal[rows][columns];
 		int i = 0, j = 0;
 		if (values != null) {
 			for (final Number value : values) {
 				this.matrix[i][j] = value == null ? BigDecimal.ZERO : NumberConverter.toBigDecimal(value);
 				if (++j >= columns) {
 					j = 0;
 					if (++i >= rows) {
 						return;
 					}
 				}
 			}
 		}
 		// fill the possible rest with zero
 		for (; j < columns; j++) {
 			this.matrix[i][j] = BigDecimal.ZERO;
 		}
 		for (; i < rows; i++) {
 			for (j = 0; j < columns; j++) {
 				this.matrix[i][j] = BigDecimal.ZERO;
 			}
 		}
 	}
 
 	public BigDecimalMatrix(final int rows, final int columns, final List<List<Number>> values) {
 		ContractCheck.mustNotBeNullOrEmpty(values, "values"); //$NON-NLS-1$
 		this.rows = ContractCheck.mustBeInRange(rows, 1, Integer.MAX_VALUE, "rows"); //$NON-NLS-1$
 		this.columns = ContractCheck.mustBeInRange(columns, 1, Integer.MAX_VALUE, "columns"); //$NON-NLS-1$
 		this.matrix = new BigDecimal[this.rows][this.columns];
 		for (int i = 0; i < this.rows; i++) {
 			final List<? extends Number> colData = values.size() <= i ? null : values.get(i);
 			for (int j = 0; j < this.columns; j++) {
 				final Number v = colData == null || colData.size() <= j ? null : colData.get(j);
 				this.matrix[i][j] = v == null ? BigDecimal.ZERO : NumberConverter.toBigDecimal(v);
 			}
 		}
 	}
 
 	public BigDecimalMatrix(final List<List<Number>> values) {
 		this(ContractCheck.mustNotBeNullOrEmpty(values, "values").size(), BigDecimalMatrix.determinColumnSize(values), values); //$NON-NLS-1$
 	}
 
 	public BigDecimal get(final int row, final int column) {
 		return this.matrix[row][column];
 	}
 
 	public int rowSize() {
 		return this.matrix.length;
 	}
 
 	public int columnSize() {
 		return this.matrix[0].length;
 	}
 
 	public BigDecimalMatrix transpose() {
 		final BigDecimalMatrix result = new BigDecimalMatrix(this.columns, this.rows);
 		for (int i = 0; i < this.rows; i++) {
 			for (int j = 0; j < this.columns; j++) {
 				result.matrix[j][i] = this.matrix[i][j];
 			}
 		}
 		return result;
 	}
 
 	public BigDecimalMatrix add(final BigDecimalMatrix summant) {
 		return add(summant, MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix add(final BigDecimalMatrix summant, final MathContext mc) {
 		if (ContractCheck.mustNotBeNull(summant, "summant").rows != this.rows || summant.columns != this.columns) {
 			throw new IllegalArgumentException("Matrix summation can only be done with matrices of the exact same dimensions");
 		}
 		final BigDecimalMatrix result = new BigDecimalMatrix(this.rows, this.columns);
 		for (int i = 0; i < this.matrix.length; i++) {
 			for (int j = 0; j < this.matrix[i].length; j++) {
 				result.matrix[i][j] = this.matrix[i][j].add(summant.matrix[i][j], mc);
 			}
 		}
 		return result;
 	}
 
 	public BigDecimalMatrix subtract(final BigDecimalMatrix summant) {
 		return subtract(summant, MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix subtract(final BigDecimalMatrix summant, final MathContext mc) {
 		if (ContractCheck.mustNotBeNull(summant, "summant").rows != this.rows || summant.columns != this.columns) {
 			throw new IllegalArgumentException("Matrix summation can only be done with matrices of the exact same dimensions");
 		}
 		final BigDecimalMatrix result = new BigDecimalMatrix(this.rows, this.columns);
 		for (int i = 0; i < this.matrix.length; i++) {
 			for (int j = 0; j < this.matrix[i].length; j++) {
 				result.matrix[i][j] = this.matrix[i][j].subtract(summant.matrix[i][j], mc);
 			}
 		}
 		return result;
 	}
 
 	public BigDecimalMatrix multiply(final long scalar) {
 		return multiply(BigDecimal.valueOf(scalar), MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix multiply(final double scalar) {
 		return multiply(BigDecimal.valueOf(scalar), MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix multiply(final Number scalar) {
 		return multiply(NumberConverter.toBigDecimal(scalar), MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix multiply(final Number scalar, final MathContext mc) {
 		return multiply(NumberConverter.toBigDecimal(scalar), mc);
 	}
 
 	public BigDecimalMatrix multiply(final BigDecimal scalar) {
 		return multiply(scalar, MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix multiply(final BigDecimal scalar, final MathContext mc) {
 		final BigDecimalMatrix result = new BigDecimalMatrix(this.rows, this.columns);
 		for (int i = 0; i < this.matrix.length; i++) {
 			for (int j = 0; j < this.matrix[i].length; j++) {
 				result.matrix[i][j] = this.matrix[i][j].multiply(scalar, mc);
 			}
 		}
 		return result;
 	}
 
 	public BigDecimalMatrix multiply(final BigDecimalMatrix factor) {
 		return multiply(factor, MathContext.UNLIMITED);
 	}
 
 	public BigDecimalMatrix multiply(final BigDecimalMatrix factor, final MathContext mc) {
 		if (this.columns != factor.rows) {
 			throw new IllegalArgumentException("The rows of the matrix factor must be equal to the columns of this matrix"); //$NON-NLS-1$
 		}
 		final BigDecimalMatrix result = new BigDecimalMatrix(this.rows, factor.columns);
 		for (int i = 0; i < result.columns; i++) {
 			for (int j = 0; j < result.rows; j++) {
 				BigDecimal temp = BigDecimal.ZERO;
 				for (int k = 0; k < this.columns; k++) {
 					temp = temp.add(this.matrix[j][k].multiply(factor.matrix[k][i]));
 				}
 				result.matrix[j][i] = temp.round(mc);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	@SuppressWarnings("nls")
 	public String toString() {
 		final StringBuilder t = new StringBuilder();
 		for (final BigDecimal[] element : this.matrix) {
 			t.append(t.length() == 0 ? "[ " : " | ");
 			for (int j = 0; j < element.length; j++) {
 				if (j > 0) {
 					t.append(" ");
 				}
 				t.append(element[j]);
 			}
 		}
 		return t.append(" ]").toString();
 	}
 
 	@SuppressWarnings("unchecked")
 	public Iterator<Collection<BigDecimal>> iterator() {
 		if (this.iterables == null) {
 			this.iterables = new Collection[this.matrix.length];
 			for (int i = 0; i < this.matrix.length; i++) {
 				// this.iterables[i] = new ArrayCollection<BigDecimal>(this.matrix[i]);
 				this.iterables[i] = Arrays.asList(this.matrix[i]);
 			}
 		}
 		return new ArrayIterator<Collection<BigDecimal>>(this.iterables);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 17;
 		int result = 1;
 		for (final BigDecimal[] row : this.matrix) {
 			for (final BigDecimal value : row) {
 				result = result * prime + (value == null ? BigDecimal.ZERO.hashCode() : value.hashCode());
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		} else {
 			final BigDecimal[][] m = ((BigDecimalMatrix) obj).matrix;
 			if (m.length != this.matrix.length) {
 				return false;
 			}
 			for (int i = 0; i < this.matrix.length; i++) {
 				if (m[i].length != this.matrix[i].length) {
 					return false;
 				}
 				for (int j = 0; j < this.matrix[i].length; j++) {
 					BigDecimal left = this.matrix[i][j];
 					final BigDecimal right = m[i][j];
 					if (left == null) {
 						left = BigDecimal.ZERO;
 					}
 					if (right == null) {
 						left = BigDecimal.ZERO;
 					}
 					if (!left.equals(right)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 	}
 
 	private static int determinColumnSize(final List<List<Number>> values) {
 		assert values != null;
 		int maxColumns = 0;
 		for (final List<Number> column : values) {
 			maxColumns = Math.max(maxColumns, column != null ? column.size() : 0);
 		}
 		return maxColumns;
 	}
 
 }
