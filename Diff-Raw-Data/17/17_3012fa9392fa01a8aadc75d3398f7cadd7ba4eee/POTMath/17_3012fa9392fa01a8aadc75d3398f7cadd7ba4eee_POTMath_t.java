 /**
  * Copyright 2013 Pimm Hogeling
  *
  * Giant Snail open is free software. Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software without restriction, including without
  * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
  * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  *
  * * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  * Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  * Alternatively, the Software may be used under the terms of either the GNU General Public License Version 3 or later (the
  * "GPL"), or the GNU Lesser General Public License Version 3 or later (the "LGPL"), in which case the provisions of the GPL or
  * the LGPL are applicable instead of those above.
  */
 
 package org.ilumbo.giantsnail.mathematics;
 
 /**
  * Mathematical operations related to powers of two.
  *
  * These operations can be useful when working with APIs that only accept powers of two. Say you have a bitmap that you have to
  * paint onto a texture that must be a power of two wide. {@link #ceil(int)}, passing the width of the bitmap, will tell you
  * minimal width of the texture.
  */
 public final class POTMath {
 	/**
 	 * ln(2).
 	 */
 	private static final double LOG_2 = .69315;
 	/**
 	 * Returns the lowest power of two that is equal to or greater than the passed input. Behaviour is undefined for
	 * non-positive inputs. 1 is considered a power of two (2⁰).
 	 */
 	public static final int ceil(final int input) {
 		// If input & (input - 1) is zero, the input is a power of two itself (or zero) and therefore the expected output.
 		if (0 == (input & input - 1)) {
 			return input;
 		}
 		return 1 << 32 - Integer.numberOfLeadingZeros(input);
 	}
 	/**
 	 * Returns the highest (greatest) power of two that is equal to or smaller than the passed input. Behaviour is undefined
	 * for non-positive inputs. 1 is considered a power of two (2⁰).
 	 */
 	public static final int floor(final int input) {
 		return 1 << 31 - Integer.numberOfLeadingZeros(input);
 	}
 	/**
	 * Returns 2ⁿ, where n is closer to ²log(input) than n - 1 and n + 1 are. Behaviour is undefined for non-positive inputs. 1
	 * is considered a power of two (2⁰).
 	 */
 	public static final int round(final int input) {
 		return 1 << Math.round(Math.log(input) / LOG_2);
 	}
 }
