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
 package org.jbasics.types.tuples;
 
 public class Quadruple<A, B, C, D> implements Tuple<A, Tuple<B, Tuple<C, D>>> {
 	private final A left;
 	private final Triplet<B, C, D> right;
 
 	public Quadruple(A first, B second, C third, D fourth) {
 		this.left = first;
 		this.right = new Triplet<B, C, D>(second, third, fourth);
 	}
 
 	public A left() {
 		return this.left;
 	}
 
 	public Triplet<B, C, D> right() {
 		return this.right;
 	}
 
 	public int size() {
 		return 4;
 	}
 
 	public A first() {
		return this.first();
 	}
 
 	public B second() {
 		return this.right.first();
 	}
 
 	public C third() {
 		return this.right.second();
 	}
 
 	public D fourth() {
 		return this.right.third();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return (17 + ((this.left == null) ? 0 : this.left.hashCode())) * 17 + this.right.hashCode();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		} else if (obj == null || !(obj instanceof Quadruple<?, ?, ?, ?>)) {
 			return false;
 		}
 		Quadruple<?, ?, ?, ?> other = (Quadruple<?, ?, ?, ?>) obj;
 		if (this.left == null) {
 			if (other.left != null) {
 				return false;
 			}
 		} else if (!this.left.equals(other.left)) {
 			return false;
 		}
 		return this.right.equals(other.right);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("QUADRUPLE(").append(this.left).append(", ").append(this.right.first()).append(", ").append(this.right.second()).append(", ")
 				.append(this.right.third()).append(")");
 		return builder.toString();
 	}
 
 }
