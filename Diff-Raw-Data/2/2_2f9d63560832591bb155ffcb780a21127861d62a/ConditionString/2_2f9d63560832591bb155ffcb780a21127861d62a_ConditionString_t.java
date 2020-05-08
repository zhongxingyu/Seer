 package org.ilumbo.giantsnail.patterns;
 
 import org.ilumbo.giantsnail.mathematics.BitArray;
 
 /**
  * A string of conditions.
  */
 public final class ConditionString {
 	/**
 	 * A single condition, part of a string.
 	 */
 	public final class Condition {
 		/**
 		 * The identifier of this condition in the string.
 		 */
 		private final int identifier;
 		/**
 		 * Whether this condition has been met. Used internally by this condition only (not by the string).
 		 */
 		private boolean met;
 		public Condition(int identifier) {
 			this.identifier = identifier;
 		}
 		/**
 		 * Returns true if this condition is met, and false if this condition is unmet.
 		 */
 		public final boolean getIsMet() {
 			return met;
 		}
 		/**
 		 * Marks this condition as met.
 		 *
 		 * Returns true if the state of the string this condition is a part of changed from unmet to met because of this call.
 		 * Returns false if said string includes other conditions that are not met. Also returns false if the state of said
 		 * string was already met before this call.
 		 */
 		public final boolean meet() {
 			// Do nothing if this condition was already met.
 			if (met) {
 				return false;
 			}
 			// Set the met bit to true for this condition. Return true if the met array is now equal to the true array, meaning
 			// the state of the string changed from unmet to met.
 			return trueArray == (metArray = BitArray.setBit(metArray, identifier, met = true));
 		}
 		/**
 		 * Marks this condition as unmet.
 		 *
 		 * Returns true if the state of the string this condition is part of changed from met to unmet because of this call.
 		 * Returns false if said string includes other conditions that are met. Also returns false if the state of said string
 		 * was already unmet before this call.
 		 */
 		public final boolean unmeet() {
 			// Do nothing if this condition was not met.
 			if (false == met) {
 				return false;
 			}
 			// Returns true if the met array equals the true array (before the bit set below), meaning the state of the
 			// string is now met (and will change to unmet by the bit set below).
 			final boolean result = trueArray == metArray; 
 			// Set the met bit to false for this condition.
 			metArray = BitArray.setBit(metArray, identifier, met = false);
 			return result;
 		}
 	}
 	/**
 	 * The number of conditions in this string.
 	 */
 	private int length;
 	/**
 	 * A bit array that defines whether the conditions in this string are met.
 	 */
 	/* package */ int metArray;
 	/**
 	 * A bit array with the same length as the met array, but with all true bits.
 	 */
 	/* package */ int trueArray;
 	public ConditionString() {
 	}
 	/**
 	 * Adds a new (initially unmet) condition to the string, and returns it.
 	 *
 	 * Note: adding a new condition might change the state of this string from met to unmet.
 	 */
 	public final Condition add() {
 		// Update the true array.
 		trueArray = BitArray.setBit(trueArray, length, true);
 		// Create the condition.
 		return this.new Condition(length++);
 	}
 	/**
	 * Returns true if every conditions in this string is met. Returns false if this string includes conditions that are not
 	 * met. As a special case: returns true if this string is empty.
 	 */
 	public final boolean getIsMet() {
 		return trueArray == metArray;
 	}
 }
