 /**
  * This class represents a big integer.
  * 
  * @author Jeff Goldsworthy
  *
  */
 public class BigInt {
     // Data members
 	int[] digits;
     
 	// Positive shifts indicates a reduce, a negative shifts value indicates 
 	// a shift.
 	int shifts = 0;
     
 	// Array of precomputed modulus powers of two.
 	BigInt precomputed[] = null;
 	
     
     /**
      * This ctor constructs a BigInt from the source string. The size of the
      * resulting BigInt is the length of the source string. A precondition
      * of this ctor is that all characters of the string are either a 1 or 0.
      * 
      * @param source
      * This is the source string to construct the big integer from.
      * 
      */
     public BigInt(String source) {
         // Construct a big int from the provided source string.
         digits = new int[source.length()];
         int sourceIndex = 0;
         
         for(int i = (source.length() - 1); i >= 0; --i) {
         	if (source.charAt(sourceIndex) == '1') {
         		digits[i] = 1;
         	}
         	else if (source.charAt(sourceIndex) == '0') {
         		digits[i] = 0;
         	}
         	else {
         		System.out.println("Invalid digit character at source index " 
         				+ sourceIndex + " in string: " + source);
                 System.exit(1);
         	}
             
         	// Increment the source index.
         	++sourceIndex;
         }
     }
     
     
     /**
      * This ctor constructs a BigInt of the specified size and inits it to zero.
      * 
      * @param size
      * The target size.
      * 
      */
     public BigInt(int size) {
     	// Construct a big int of the specified size of value 0.
         digits = new int[size];
         
         // Init the digits array with zeros.
         for (int i = 0; i < digits.length; ++i) {
         	digits[i] = 0;
         }
     }
     
     
     /**
      * This is the class copy constructor. This method will deep copy the 
      * underlying array.
      * 
      * @param source
      * Source big int to copy.
      */
     public BigInt(BigInt source) {
         digits = new int[source.size()];
         
         // Deep copy the big int.
         for (int index = 0; index < size(); ++index) {
         	digits[index] = source.getBit(index);
         }
     }
 
     
     /**
      * 
      * @param power
      * This is the power to raise this big integer by.
      * 
      * @param modBy
      * This is the value to mod the result of the power of this big integer by.
      * 
      * @return
      * This method returns the result of raising this big integer by the power
      * and mod'ing it by the mod value.
      */
 	public BigInt modPow(BigInt power, BigInt modBy) {
 		return null;
 	}
     
 	
 	
     /**
      * This method is a unary add that adds the rValue into this BigInt (the 
      * lValue). It add the bits contained in each big integer.
      * 
      * Precondition: The size of this BigInt must be large enough to handle 
      * having the lValue added into it including the possibility of a final 
      * carry.
      * 
      * @param rValue
      * The container in which to place the result.  This may already have a
      * value, such as an accumulator.
      * 
      * @return
      * This method returns a BigInt representing the sum of the two BigInts.
      */
 	public BigInt add(BigInt rValue) {
 //        if (result == null) {
 //        	result = new BigInt(Math.max(size(), rValue.size()) + 1);
 //        }
         if (rValue.size() > size()) {
         	System.out.println("Possible buffer overflow. This BigInt must"
         			+ " be larger in size than the value being added in.");
             throw new RuntimeException();
         }
         
 		boolean activeCarry = false;
         
         // Iterate over all bits of the two numbers, but we need to go one
 		// beyond because there could be one last carry bit.  The for loop
 		// could have checked for <= max(size), but checking for < Max(size)+1
 		// is more intuitive.
 		for (int i = 0; i < (size()); ++i) {
 			// Check if this is the most significant bit iteration.
 			// The lValue is always larger than the rValue and therefore will
 			// never have more than a carry bit added in.
 			if (i == size()) {
 				// Check the carry only.
 				if (activeCarry) {
                     if (getBit(i) == 1) {
                         // Uh oh, we got a buffer overflow.
                         System.out.println("BUFFER OVERFLOW. This BigInt isn't large enough to add in the rValue.");
                         System.exit(1);
                     }
                     
 					// Set the most significant bit to one.
 					setBit(i, 1);
 				}
 				else {
 // TODO: (goldsy) REMOVE AFTER TESTING.
 //					// The most significant bit was not needed.
 //					// Create a new result array with on less bit, copy the
 //					// result bits to the new array and set it in the resulting
 //					// big int.
 //					int[] temp = new int[result.size() - 1];
 //
 //					for (int index = 0; index < temp.length; ++index) {
 //						temp[index] = result.getBit(index);
 //					}
 //
 //					// Finally set the result to the temp.
 //					result.setArray(temp);
 				}
 			}
             // TODO: (goldsy) REMOVE AFTER TESTING.  The lValue is always larger than the rValue.
 			// Check if the index has gone beyond the last bit of the lValue.
 //			else if (i > (size() - 1)) {
 //				if (rValue.getBit(i) == 1) {
 //					if (activeCarry) {
 //						// Leave the carry active.
 //						result.setBit(i, 0);
 //					}
 //					else {
 //						// Clear the carry active flag.
 //						activeCarry = false;
 //						result.setBit(i, 1);
 //					}
 //				}
 //				// The bit is a zero.
 //				else {
 //					if (activeCarry) {
 //						result.setBit(i, 1);
 //					}
 //					else {
 //						result.setBit(i, 0);
 //					}
 //
 //					// Always clear the carry active flag.
 //					activeCarry = false;
 //				}
 //			}
 			// Check if the index has gone beyond the last bit of the rValue.
 			else if (i > (rValue.size() - 1)) {
 				if (getBit(i) == 1) {
 					if (activeCarry) {
 						// Leave the carry active.
 						setBit(i, 0);
 					}
 					else {
                         // Leave lValue bit[i] == 1.
 						// Clear the carry active flag.
 						activeCarry = false;
 					}
 				}
 				// The bit is a zero.
 				else {
 					if (activeCarry) {
 						setBit(i, 1);
 					}
                     
 					// If no carry leave lValue bit[i] == 0.
 
 					// Always clear the carry active flag.
 					activeCarry = false;
 				}
 			}
 			// The two bits from each number plus the potential for a carry
 			// must be accounted for.
 			else {
 				// Check if both the lValue and rValue bits are one.
 				if (getBit(i) + rValue.getBit(i) == 2) {
 					if (activeCarry) {
 						// Leave the carry flag set.
                         // Leave lValue bit[i] == 1.
 					}
 					else {
 						setBit(i, 0);
 
 						// Set the carry bit flag.
 						activeCarry = true;
 					}
 				}
 				// Check if only one of the lValue or rValue bits are one.
 				else if (getBit(i) + rValue.getBit(i) == 1) {
 					if (activeCarry) {
 						// Leave the carry flag set.
 						// Set the bit to zero. We don't know which is zero.
 						setBit(i, 0);
 					}
 					else {
 						// Leave the carry flag cleared.
 						// Set the bit to zero. We don't know which is 1.
 						setBit(i, 1);
 					}
 				}
 				// Both bits must be zero.
 				else {
 					if (activeCarry) {
 						setBit(i, 1);
 
 						// Clear the carry flag.
 						activeCarry = false;
 					}
                     
                     // If there is no active carry:
 					// Leave the carry flag cleared.
 					// Leave the bit lValue[i] == 0.
 				}
 			}
 		}
     
 		return this;
 	}
     
 	
 	
     /**
      * This is the add method for BigInts. It add the bits contained in each
      * big integer.
      * 
      * @param rValue
      * The right value in the addition.
      * 
      * @param result
      * The container in which to place the result.  This may already have a
      * value, such as an accumulator.
      * 
      * @return
      * This method returns a BigInt representing the sum of the two BigInts.
      */
 	public BigInt basicAdd(BigInt rValue, BigInt result) {
         if (result == null) {
         	result = new BigInt(Math.max(size(), rValue.size()) + 1);
         }
         
 		boolean activeCarry = false;
         
         // Iterate over all bits of the two numbers, but we need to go one
 		// beyond because there could be one last carry bit.  The for loop
 		// could have checked for <= max(size), but checking for < Max(size)+1
 		// is more intuitive.
 		for (int i = 0; i < (Math.max(this.size(), rValue.size()) + 1); ++i) {
 			// Check if this is the most significant bit iteration.
 			if (i == (Math.max(size(), rValue.size()) + 1)) {
 				// Check the carry only.
 				if (activeCarry) {
 					// Set the most significant bit to one.
 					result.setBit(i, 1);
 				}
 				else {
 					// The most significant bit was not needed.
 					// Create a new result array with on less bit, copy the
 					// result bits to the new array and set it in the resulting
 					// big int.
 					int[] temp = new int[result.size() - 1];
 
 					for (int index = 0; index < temp.length; ++index) {
 						temp[index] = result.getBit(index);
 					}
 
 					// Finally set the result to the temp.
 					result.setArray(temp);
 				}
 			}
 			// Check if the index has gone beyond the last bit of the lValue.
 			else if (i > (size() - 1)) {
 				if (rValue.getBit(i) == 1) {
 					if (activeCarry) {
 						// Leave the carry active.
 						result.setBit(i, 0);
 					}
 					else {
 						// Clear the carry active flag.
 						activeCarry = false;
 						result.setBit(i, 1);
 					}
 				}
 				// The bit is a zero.
 				else {
 					if (activeCarry) {
 						result.setBit(i, 1);
 					}
 					else {
 						result.setBit(i, 0);
 					}
 
 					// Always clear the carry active flag.
 					activeCarry = false;
 				}
 			}
 			// Check if the index has gone beyond the last bit of the rValue.
 			else if (i > (rValue.size() - 1)) {
 				if (getBit(i) == 1) {
 					if (activeCarry) {
 						// Leave the carry active.
 						result.setBit(i, 0);
 					}
 					else {
 						// Clear the carry active flag.
 						activeCarry = false;
 						result.setBit(i, 1);
 					}
 				}
 				// The bit is a zero.
 				else {
 					if (activeCarry) {
 						result.setBit(i, 1);
 					}
 					else {
 						result.setBit(i, 0);
 					}
 
 					// Always clear the carry active flag.
 					activeCarry = false;
 				}
 			}
 			// The two bits from each number plus the potential for a carry
 			// must be accounted for.
 			else {
 				// Check if both the lValue and rValue bits are one.
 				if (getBit(i) + rValue.getBit(i) == 2) {
 					if (activeCarry) {
 						// Leave the carry flag set.
 						result.setBit(i, 1);
 					}
 					else {
 						result.setBit(i, 0);
 
 						// Set the carry bit flag.
 						activeCarry = true;
 					}
 				}
 				// Check if only one of the lValue or rValue bits are one.
 				else if (getBit(i) + rValue.getBit(i) == 1) {
 					if (activeCarry) {
 						// Leave the carry flag set.
 						result.setBit(i, 0);
 					}
 					else {
 						// Leave the carry flag cleared.
 						result.setBit(i, 1);
 					}
 				}
 				// Both bits must be zero.
 				else {
 					if (activeCarry) {
 						// Leave the carry flag set.
 						result.setBit(i, 1);
 
 						// Clear the carry flag.
 						activeCarry = false;
 					}
 					else {
 						// Leave the carry flag cleared.
 						result.setBit(i, 0);
 					}
 				}
 			}
 		}
     
 		return result;
 	}
 
     
 	/**
 	 * This is the subtract method. It subtracts the right value from this
 	 * BigInt which is the left value.
      * 
 	 * @param rValue
      * The right value in the equation.
      * 
 	 * @return
      * This method returns the result of the subtraction.
 	 */
 	public BigInt subtract(BigInt rValue) { 
         // DEBUG
 		//System.out.println("lValue: " + toString());
 		//System.out.println("rValue: " + rValue.toString());
         
         // Since this changes the lValue we have to make a copy to preserve
 		// it's original value.  This probably screws the value of the non-
 		// standard subtract presented in class.
 		BigInt tempLValue = new BigInt(this);
 		
         // Find the most significant digit where the lValue is 1 and rValue
 		// is 0.
         
         int maxIndex = -1;
         
         // We only have to check the lValue because we know that the lValue must
 		// be greater than the rValue. We won't have negatives in our 
         // implementation.
 		for (int index = 0; index < tempLValue.size(); ++index) {
 			if (tempLValue.getBit(index) == 1 && rValue.getBit(index) == 0) {
 				maxIndex = index;
 			}
 		}
         
         // Check if that case doesn't exist.
 		if (maxIndex == -1) {
             // TODO: (goldsy) REMOVE AFTER TESTING.
             System.out.println("HOLD IT! WE GOT TWO NUMBERS THAT WILL RESULT IN A NEGATIVE OR ZERO (WHICH WE MIGHT HAVE).");
             
             // All bits must be considered.
 			maxIndex = tempLValue.size() - 1;
 		}
         
         // The result will never be any more than the largest value.
 		BigInt result = new BigInt(Math.max(tempLValue.size(), rValue.size()));
         
 		// Now that we found that bit. Borrow from it... Set maxIndex bit to 0.
         tempLValue.setBit(maxIndex, 0);
         
 		// Add 1 to every bit less than maxIndex.
         for (int index = 0; index < maxIndex; ++index) {
         	// Note: Add 1 to bits that are already 1 will result in an int of 2
         	//		which isn't binary, but this is a non-std subtraction algorithm.
         	tempLValue.digits[index] += 1;
         }
         
         // Set the carry bit flag.
         boolean carryFlag = true;
         
         // Do subtraction.
         for (int index = 0; index < tempLValue.size(); ++index) {
         	result.setBit(index, (tempLValue.digits[index] - rValue.getBit(index)));
         }
         
         // Now account for the carry flag from the borrow and carry for any
         // remaining 2s.
         for (int index = 0; index < result.size(); ++index) {
         	if (carryFlag) {
         		if (result.getBit(index) == 0) {
                     // Add 1 to the bit.
         			result.setBit(index, 1);
                     
         			// Reset the carry flag.
         			carryFlag = false;
         		}
         		else if (result.getBit(index) == 1) {
         			// Set the result bit to zero and keep the carry.
                     result.setBit(index, 0);
         		}
         		else {
         			// The value must have been a 2 so set the value to 1 and
         			// keep the carry.
                     result.setBit(index, 1);
         		}
         	}
         	else {
         		// There is no carry. A two has an implicit carry. The other
         		// two cases won't change the value.
                 if (result.getBit(index) == 2) {
                     // A 2 is a binary 10.
                 	result.setBit(index, 0);
                 	
                 	// Set the carry flag.
                 	carryFlag = true;
                 }
         	}
         }
         
         // TODO: (goldsy) Not sure if we need to but resize the result to the
         // 		exact size.
         result.compact();
         
         return result;
 	}
     
 	
     /**
      * This method returns the product of this BigInt with the right value.
      * 
      * @param rValue
      * The right value of the multiplication equation.
      * 
      * @return
      * This method returns the product of the multiplication.
      */
 	public BigInt multiply(BigInt rValue) {
         // The accumulator needs to be the size of the sum of the size of
 		// the two numbers.
 		BigInt accumulator = new BigInt(size() + rValue.size());
         int shiftCount = 0;
         
         // Break up rValue into linear combination and add the shifted lValue
 		// pieces into the accumulator.
         for (int index = 0; index < rValue.size(); ++index) {
             // If the rValue bit is 1 add the lValue otherwise 0 x anything =
         	// zero so skip it.
         	if (rValue.getBit(index) == 1) {
         		accumulator.add(this);
         	}
             
         	// Shift the lValue. Shifted after the first add because lValue is
         	// already 2^0.
         	shifts -= 1;
             ++shiftCount;
         }
         
         // After multiple restore the shift value to zero.
         shifts += shiftCount;
 		
 		return accumulator;
 	}
     
 	
     /**
      * This method calculates the modulus of this big int.
      * 
      * @param modulus
      * The modulus to use.
      * 
      * @return
      * This method returns the modulus of this BigInt.
      */
 	public BigInt mod(BigInt modulus) {
        if (precomputed == null) {
        	precomputeMods(modulus);
        }
        
         // The accumulator will never be larger than size of mod + 1.
         BigInt accumulator = new BigInt(modulus.size() + 1);
         
         // Loop through all of the bits of this number.  Only add in the
         // remainders when the bit we are looking at is a 1.
         for (int index = 0; index < size(); ++index) {
         	if (getBit(index) == 1) {
         		accumulator.add(precomputed[index]);
                 
                 // Mod the result.
         		// Check if the addition pushed us over modulus. If so then
         		// subtract it.
                 if (accumulator.isGreaterOrEquals(modulus)) {
                 	accumulator = accumulator.subtract(modulus);
                 }
         	}
         }
         
         accumulator.compact();
         
         return accumulator;
 	}
     
     
 	/**
 	 * This method precomputes the mods for the powers of two.
      * 
 	 * @param modulus
      * The modulus to use for the precomputed values.
 	 */
 	public void precomputeMods(BigInt modulus) {
 		// Pre-compute the modulus of the power of two to numBits.
 		int numBits = size();
 
 		precomputed = new BigInt[numBits];
 
 		// Since mod'ing by 0 or 1 makes no sense (the former being undefined)
 		// it can be safely assumed that 2^0 mod(x) = 1.
 		precomputed[0] = new BigInt("1");
 
 		for (int index = 1; index < numBits; ++index) {
 			// Set the next one equal to this one.
 			precomputed[index] = new BigInt(precomputed[index - 1]);
 
 			// Double it's value.
 			precomputed[index].shift();
 
 			// DEBUG
 			//System.out.println("After shifting: " + precomputed[index].toString());
 
 			// If the value is greater or = to the modulus subtract it out.
 			if (precomputed[index].isGreaterOrEquals(modulus)) {
 				// DEBUG
 				//System.out.println("This value [" + toString() + "] is greater than modulus [" + modulus + "]");
 				precomputed[index] = precomputed[index].subtract(modulus);
 			}
 		}
 
 		// DEBUG
 		//System.out.println("FINISHED PRECOMPUTING VALUES.");
 	}
 	
 	
     /**
      * Convenience function to create the accumulator for the modular 
      * exponentiation function.
      * 
      * @param exponent
      * Power to raise this BigInt by.
      * 
      * @param modulus
      * The modulus value.
      * 
      * @return
      * This function returns the the modulus of this big int rasied to power 
      * exponent.
      */
 	public BigInt powMod(BigInt exponent, BigInt modulus) {
         precomputeMods(modulus);
         
 		BigInt accumulator = new BigInt(modulus.size());
         
         // This method returns a reference to the accumulator passed in the
 		// parameters.
 		return powMod(exponent, modulus, accumulator);
 	}
     
 	
     /**
      * 
      * @param exponent
      * @param modulus
      * @param accumulator
      * @return
      */
 	private BigInt powMod(BigInt exponent, BigInt modulus, BigInt accumulator) {
         // DEBUG
 		System.out.println("Exponent value: " + exponent.toString() + " Size: " + exponent.size());
 		
         // Just make sure there are no leading zeros. This only costs a 
 		// function call if a resize isn't necessary.
         exponent.compact();
         
         if (exponent.toString().equals("1")) {
             // a (mod m)
             BigInt temp = mod(modulus);
             
         	// DEBUG
         	System.out.println("Finished mod'ing exponent:" + exponent.toString() + " temp: " + temp.toString());
             
         	return temp;
         }
         else if (exponent.isOdd()) {
             // [a (mod m)]*[a^(b/2)(mod m)]^2(mod m)
         	BigInt lValue = mod(modulus);
             
         	// Divide the exponent.
             exponent.reduce();
             
             // Call powMod() again.
         	BigInt rValue = powMod(exponent, modulus, accumulator);
             
         	// Square the resulting value.
             rValue = rValue.multiply(rValue);
             
             // Multiply the lValue and rValue together, mod() it and return the value.
             return lValue.multiply(rValue).mod(modulus);
         }
         else {
         	// The exponent is even.
             // [a^(b/2)(mod m)]^2(mod m)
             
         	// Divide the exponent.
             exponent.reduce();
             
             // Call powMod() again.
         	BigInt rValue = powMod(exponent, modulus, accumulator);
             
        	BigInt temp = new BigInt(rValue);
            
         	// Square the resulting value, mod it and return the value.
            rValue = rValue.multiply(temp);
            rValue.mod(modulus);
            
            return rValue;
         }
 	}
     
     
 	/**
 	 * This method returns the size of the BigInt.
      * 
 	 * @return
      * This method returns the size of this BigInt.
 	 */
 	public int size() {
         // We need to adjust the size by the shifts. If we did a shift then
 		// we added digits by adding least signification bits. The least
 		// significant bit is initially at the zero index in the array.
 		return (digits.length - shifts);
 	}
     
 	
     /**
      * This method logically shifts the value of this big int.
      */
 	public void shift() {
         // Since index 0 is least significant bit, we need to subtract to shift.
 		--shifts;
 	}
     
 	
     /**
      * This method logically reduces the value of this big int.
      */
 	public void reduce() {
         // Since index 0 is least significant bit, we need to add to reduce.
         // The effect is ignoring the actual low order bit(s).
 		++shifts;
 	}
     
 	
     /**
      * This method returns whether this Big Int is odd or not.
      * 
      * @return
      * This method returns true if the logically least significant bit is odd.
      */
 	public boolean isOdd() {
 		if (digits[adjustedIndex(0)] == 1) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
     
 	
     /**
      * This method determines if this BigInt is greater than or equals the
      * specified Big Int.
      * 
      * @param rValue
      * The right term.
      * 
      * @return
      * This method returns true if this Big Int is greater or equal in value
      * and false otherwise. */
 	public boolean isGreaterOrEquals(BigInt rValue) {
 		// It's more likely that the value will be greater or less than so
 		// check that first. Start at high bits and look for first difference.
         for(int index = Math.max(size(), rValue.size()); index >= 0; --index) {
         	if (getBit(index) > rValue.getBit(index)) {
         		return true;
         	}
         	else if (getBit(index) < rValue.getBit(index)) {
         		return false;
         	}
         }
         
         // If we made it through all of the bits, then they are the same.
         return true;
 	}
 	
 	
     /**
      * This method gives access to the internal representation of the int.
      * 
      * @return
      * This method returns a reference to the internal bit array.
      */
 	public int[] getArray() {
 		return digits;
 	}
     
     
     /**
      * This method sets the bit array to the source value.
      * 
      * @param source
      * This source arrays to get the digits array to.
      */
 	public void setArray(int[] source) {
 		digits = source;
 	}
 	
     
     /**
      * This method sets the specified bit to the specified value.
      * 
      * @param index
      * Target bit index.
      * 
      * @param value
      * Value to set the bit to.
      */
 	public void setBit(int index, int value) {
 		digits[index] = value;
 	}
     
 	
     /**
      * This method returns the bit value at the specified index.
      * 
      * @param index
      * The target index to get.
      * 
      * @return
      * This method returns the value found in the bit array at the specified
      * index.
      */
 	public int getBit(int index) {
         // Any value more significant than size() has to be zero.
         if ((index >= size()) || (adjustedIndex(index) < 0)) {
         	return 0;
         }
         else {
         	return digits[adjustedIndex(index)];
         }
 	}
     
 	
     /**
      * This method determines what the actual index is into the underlying 
      * array.  It is the responsibility of the caller not to access outside the
      * bounds of the underlying array.
      * 
      * @param index
      * The requested index.
      * 
      * @return
      * This method returns the requested index adjusted by the number of shifts.
      * 
      */
 	public int adjustedIndex(int index) {
 		return (index + shifts);
 	}
     
 	
     /**
      * This method compacts the BigInt by reducing the underlying array by
      * the size of any most significant zeros. In other words 00111 will be
      * compacted by 2 resulting in an underlying array of three locations
      * instead of 5. If the most significant bit is a one then this method
      * does nothing.
      */
 	public void compact() {
         if (size() == 1) {
         	// Already as small as we can get. Nothing to do.
             return;
         }
         
         int targetSize = -1;
         
         for (int index = (size() - 1); (index > 0) && (targetSize == -1); --index) {
         	if (getBit(index) == 1) {
         		targetSize = index + 1;
         	}
         }
         
         if (targetSize == size()) {
             // There is nothing to do.
         	return;
         }
         
         // All bits are zero.  Reduce to size 1.
         if (targetSize == -1) {
             targetSize = 1;
         }
         
         // Create a new array of the new size.
         int[] temp = new int[targetSize];
         
         // Copy the bits from the original into this one.
         for (int index = 0; index < targetSize; ++index) {
         	temp[index] = digits[index];
         }
         
         digits = temp;
 	}
     
 	
 	public String toString() {
         StringBuilder temp = new StringBuilder(size());
         
 		for (int i = (size() - 1); i >= 0; --i) {
 			temp.append(getBit(i));
 		}
         
 		return (temp.toString() /*+ " Shift Value: " + shifts + " Size: " + size()*/);
 	}
 }
