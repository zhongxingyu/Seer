 /* Copyright (C) 2013, Cameron White
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE PROJECT OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package edu.pdx.cawhite.math;
 
 import edu.pdx.cawhite.iterators.BidirectionalIterator;
 import edu.pdx.cawhite.iterators.IteratorException;
 
 /**
  * Implements a mathematical vector.
  * 
  * @author Cameron Brandon White
  */
 public class Vector {
 
 	protected double[] components;
 
     /** 
      * Create a vector of the given length.
      *
      * The vector will be of the length given and each component will
      * be initialized to 0.0.
      *
      * @param length 	The length of the vector to create. 
      */
 	public Vector(int length) throws LengthException {
 		super();
 		if (length < 0)
 			throw new LengthException("Length must be greater than 0.");
 		this.components = new double[length];
 	}
 
 	/**
 	 * Create a vector with the following components.
 	 *
 	 * The components that will not be copied. This means it is possible
 	 * for the components to be referenced elsewhere.
 	 *
 	 * @param components	The components in the Vector.
 	 */
     public Vector(double[] components) throws LengthException {
     	super();
     	if (components.length <= 0)
     		throw new LengthException("Length must be greater than 0.");
 		this.components = components;
     }
 
     protected Vector() {
     	super();
     }
 
     /**
      * Copy constructor
      *
      * The vector will be copied. This will result in the new vector
      * and the old vector not referring to the same memory.
      *
      * @param vector 	Vector to copy
      */
     public Vector(Vector vector) throws LengthException {
     	super();
 
     }
 
     /** 
      * Add this matrix with the other in a new vector.
      * 
      * @param other The other vector.
      * @return The new vector. 
      */
 	public Vector add(Vector other) throws LengthException {
 		Vector newVector = null;
 		try {
 			newVector = new Vector(getLength());
 		} catch (LengthException e) {
 			assert false : "Fatal programming error";
 		}
 		return add(other, newVector);
 	}
 
     /** 
      * Add this matrix with the other in the new vector provided.
      * 
      * @param other 	The other vector.
      * @param newVector The new vector.
      * @return The new vector. 
      */
 	public Vector add(Vector other, Vector newVector) 
 			throws LengthException {
 
 		this.checkForEqualLength(other);
 		this.checkForEqualLength(newVector);
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				newVector.set(i, this.get(i) + other.get(i));
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return newVector;
 	}
 
 	/**
 	 * Copy the vector.
 	 *
 	 * Both vectors must be of the same length. This method will copy
 	 * the memory of the vector. This will result in the vectors not
 	 * Referring to the same memory.
 	 *
 	 * @param vector 	The vector to copy.
 	 * @return This vector.
 	 */
 	public void copy(Vector vector) throws LengthException {
 		
 		this.checkForEqualLength(vector);
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				this.set(i, vector.get(i));
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 	}
 
 	/**
 	 * Get the component at the given index.
 	 *
 	 * The first component is at index 0.
 	 *
 	 * @param index 	The index.
 	 */
 	public double get(int index) throws IndexOutOfBoundsException {
 		try {
 			return this.components[index];
 		} catch (ArrayIndexOutOfBoundsException e) {
 			throw new IndexOutOfBoundsException();
 		}
 	}
 
 	/**
 	 * Get the number of components in the vector.
 	 * 
 	 * @return The number of element in the vector.
 	 */
 	public int getLength() {
 		return this.components.length;
 	}
 
 	public Vector.Iterator getIterator() {
 		return this.new Iterator();
 	}
 
 	/**
 	 * Set the value of the component at the given index.
 	 * 
 	 * The first value is at index 0.
 	 *
 	 * @param index  The index of the element.
 	 * @param value  The new value.
 	 */
 	public void set(int index, double value) 
 			throws IndexOutOfBoundsException {
 		try {
 			this.components[index] = value;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			throw new IndexOutOfBoundsException();
 		}
 	}
 
     /** 
      * Compute dot product of this vector with the other vector 
      * 
      * @param other  The other vector. 
      * @return  The dot product
      */
 	public double dot(Vector other)
 			throws LengthException {
 
 		this.checkForEqualLength(other);
 
 		double dotProduct = 0.0;
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				dotProduct += this.get(i) * other.get(i);
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return dotProduct;	
 	}
 
 
 	/**
 	 * Compute the magnitude of the vector.
 	 *
 	 * @return the magnitude of the vector.
 	 */
 	public double magnitude() {
 		double magnitude = 0.0;
 		double component = 0.0;
 
 		try {
 			for (int i = 0; i < getLength(); i++) {
 				component = this.get(i);
 				magnitude += component * component;
 			}
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return Math.sqrt(magnitude);
 	} 
 
     /** 
      * Multiply this matrix by a scalar in a new matrix.
      * 
      * @param scalar The scalar to multiply by.
      * @return The new vector. 
      */
 	public Vector mul(double scalar) {
 		Vector newVector = null;
 		try {
 			newVector = new Vector(getLength());
 			newVector = mul(scalar, newVector);
 		} catch (LengthException e) {
 			assert false : "Fatal programming error";
 		}
 		return newVector;
 	}
 
     /** 
      * Multiply this matrix by a scalar in a new matrix.
      * 
      * @param scalar The scalar to multiply by.
      * @return The new vector. 
      */
 	public Vector mul(double scalar, Vector newVector) {
 
 		try {
 			this.checkForEqualLength(newVector);
 
 			for (int i = 0; i < getLength(); i++)
 				newVector.set(i, scalar * this.get(i));
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		} catch (LengthException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return newVector;
 	}
 
 
 	/**
 	 * Return a new vector of this vector normalized.
 	 *
 	 * @return new normalized vector.
 	 */
 	public Vector normalize() {
 
 		Vector newVector = null;
 		
 		try {
 			newVector = new Vector(getLength());
 			double magnitude = this.magnitude();
 
 			for (int i = 0; i < getLength(); i++)
 				newVector.set(i, this.get(i) / magnitude);
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		} catch (LengthException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return newVector;
 	}
 
 
     /** 
      * Subtract this matrix from the other as a new vector.
      * 
      * @param other The other vector.
      * @return The new vector. 
      */
 	public Vector sub(Vector other) throws LengthException {
 		
 		Vector newVector = null;
 		
 		try {
 			newVector = new Vector(getLength());
 		} catch (LengthException e) {
 			assert false : "Fatal programming error";
 		}
 		
 		return sub(other, newVector);
 	}
 
     /** 
      * Subtract this matrix from the other as a new vector.
      * 
      * @param other The other vector.
      * @return The new vector. 
      */
 	public Vector sub(Vector other, Vector newVector) 
 			throws LengthException {
 
 		checkForEqualLength(other);
 		checkForEqualLength(newVector);
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				newVector.set(i, this.get(i) - other.get(i));
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		} 
 
 		return newVector;
 	}
 
     /** 
      * Add this matrix with the other in place.
      * 
      * @param other The other vector.
      * @return This vector modified. 
      */
 	public Vector iadd(Vector other) 
 			throws LengthException {
 
 		checkForEqualLength(other);
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				this.set(i, this.get(i) + other.get(i));
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return this;
 	}
 
 
     /** 
      * Multiply this matrix by a scalar in place. 
      * 
      * @param scalar The scalar to multiply by.
      * @return The modified vector. 
      */
 	public Vector imul(double scalar) {
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				this.set(i, scalar * this.get(i));
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return this;
 	}
 
 	/**
 	 * Normalize the vector in place.
 	 *
 	 * @return this vector
 	 */
 	public Vector inormalize() {
 
 		double magnitude = this.magnitude();
 
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				this.set(i, this.get(i) / magnitude );
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return this;
 	}	
 
     /** 
      * Subtract this matrix from the other in place.
      * 
      * @param other The other vector.
      * @return This vector modified. 
      */
 	public Vector isub(Vector other) 
 			throws LengthException {
 
 		checkForEqualLength(other);
 
 		try {
 			for (int i = 0; i < getLength(); i++)
 				this.set(i, this.get(i) - other.get(i));
 
 		} catch (IndexOutOfBoundsException e) {
 			assert false : "Fatal programming error";
 		}
 
 		return this;
 	}
 
 	/**
 	 * Check that this vector and the other vector are the same length.
 	 *
 	 * @param other The other vector.
 	 */
 	protected void checkForEqualLength(Vector other) throws LengthException {
 
 		if (this.getLength() != other.getLength())
 			throw new LengthException("Vectors must be of equal length");
 	}
 
 	public class Iterator extends BidirectionalIterator {
 
 
 		protected int index;
 
 
 		public Iterator() {
 			this.index = 0;
 		}
 
 		public double get() 
 				throws IteratorException {
 			try {
 				return Vector.this.get(index);
 			} catch (IndexOutOfBoundsException e) {
 				throw new IteratorException();
 			}
 		}
 
 		public void set(double value) 
 				throws IteratorException {
 			try {
 				Vector.this.set(index, value);
 			} catch (IndexOutOfBoundsException e) {
 				throw new IteratorException();
 			}
 		}
 
 		public void next() {
 			index++;
 		}
 
 		public void previous() {
 			index--;
 		}
 
 		public void restart() {
 			index = 0;
 		}
 
 		public Boolean hasNext() {
			if (index < getLength())
 				return true;
 			else
 				return false;
 		}
 		
 		public Boolean hasPrevious() {
 			if (index <= 0)
 				return false;
 			else if (index > getLength())
 				return false;
 			else
 				return true;
 		}
 	}
 }
