 /**
  * @author cadesalaberry
  */
 
 package a1posted;
 
 /*
  *   STUDENT NAME      : Charles-Antoine de Salaberry
  *   STUDENT ID        : 260400928
  *   
  *   If you have any issues that you wish the T.A.s to consider, then you
  *   should list them here.   If you discussed on the assignment in depth 
  *   with another student, then you should list that student's name here.   
  *   We insist that you each write your own code.   But we also expect 
  *   (and indeed encourage) that you discuss some of the technical
  *   issues and problems with each other, in case you get stuck.    
  */
 
 import java.util.LinkedList;
 
 public class NaturalNumber {
 
 	int base;
 
 	private LinkedList<Integer> coefficients;
 
 	// For any base and any positive integer, the representation of that
 	// positive
 	// integer as a sum of powers of that base is unique.
 	// Moreover, we require that the "last" coefficient (namely, the coefficient
 	// of the largest power) is non-zero.
 	// For example, 350 is a valid representation (which we call
 	// "three hundred fifty")
 	// but 0353 is not.
 
 	// Constructors
 
 	// This constructor is called from the Tester class.
 	// Note that it reverses the order of the coefficients, namely it reads in
 	// an array
 	// and then constructs a LinkedList by successively reading the elements of
 	// the array
 	// and adding them to the front of the LinkedList.
 	// e.g. if the array is a[0] = 5, a[1] = 7, then it constructs the
 	// LinkedList (7, 5).
 
 	NaturalNumber(int base, int[] intarray) throws Exception {
 		this.base = base;
 		coefficients = new LinkedList<Integer>();
 		for (int i = 0; i < intarray.length; i++) {
 			if ((i >= 0) && (intarray[i] < base))
 				coefficients.addFirst(new Integer(intarray[i]));
 			else {
 				System.out
 						.println("constructor error:  all coefficients should be non-negative and less than base");
 				throw new Exception();
 			}
 		}
 	}
 
 	// This constructor acts as a helper. It is not called from the Tester
 	// class.
 
 	NaturalNumber(int base) {
 		this.base = base;
 		coefficients = new LinkedList<Integer>();
 	}
 
 	// This constructor acts as a helper. It is not called from the Tester
 	// class.
 
 	NaturalNumber(int base, int i) throws Exception {
 		this.base = base;
 		coefficients = new LinkedList<Integer>();
 
 		if ((i >= 0) && (i < base))
 			coefficients.addFirst(new Integer(i));
 		else {
 			System.out
 					.println("constructor error: all coefficients should be non-negative and less than base");
 			throw new Exception();
 		}
 	}
 
 	/**
 	 * Adds the two numbers and returns the result. It assumes that the two
 	 * numbers to add are of the same base.
 	 * 
 	 * @return addition
 	 */
 	public NaturalNumber add(NaturalNumber second) {
 
 		// Gets the biggest number of the two, and produces a copy of the two.
 		LinkedList<Integer> biggest = this.coefficients.size() > second.coefficients
 				.size() ? this.clone().coefficients
 				: second.clone().coefficients;
 
 		LinkedList<Integer> smallest = this.coefficients.size() > second.coefficients
 				.size() ? second.clone().coefficients
 				: this.clone().coefficients;
 
 		// Initialises the sum as an empty list of coefficients.
 		NaturalNumber sum = new NaturalNumber(this.base);
 
 		// Initialises the carry.
 		int carry = 0;
 
 		// Fills in the sum until we have no more carry.
 		while (biggest.size() != 0 || carry != 0) {
 
 			// Makes sure we have no overflow.
 			int digitSmallest = (smallest.size() > 0 ? smallest.pop() : 0);
 			int digitBiggest = (biggest.size() > 0 ? biggest.pop() : 0);
 
 			// Fills in the list of int.
 			sum.coefficients.add((digitBiggest + digitSmallest + carry)
 					% this.base);
 
 			// Stores the carry.
 			carry = (digitBiggest + digitSmallest + carry) / this.base;
 
 			// Debug Line to summarise the action of add().
 			// System.err.println("sum[" + sum.coefficients.size() + "]\t= " +
 			// sum.coefficients.peekLast()
 			// + "\t = " + digitBiggest + " + " + digitSmallest + "\t+ " +
 			// carry);
 
 		}
 
 		return cleanTrailingZeros(sum);
 	}
 
 	/*
 	 * The subtract method computes a.subtract(b) where a>b. If a<b, then it
 	 * throws an exception.
 	 */
 
 	public NaturalNumber subtract(NaturalNumber second) throws Exception {
 
 		// initialize difference as an empty list of coefficients
 
 		NaturalNumber difference = new NaturalNumber(this.base);
 
 		// The subtract method shouldn't affect the number itself.
 		// But the grade school algorithm sometimes requires us to "borrow"
 		// from a higher coefficient to a lower one. So let's just work
 		// with a copy (a clone) of 'this' so that we don't modify 'this'.
 
 		if (this.compareTo(second) < 0) {
 			System.out.println("Error:  subtract a-b requires that a > b");
 			throw new Exception();
 		}
 
 		// Produces a copy of the two numbers.
 		LinkedList<Integer> biggest = this.clone().coefficients;
 		LinkedList<Integer> smallest = second.clone().coefficients;
 
 		// Initialises the carry.
 		int carry = 0;
 
 		// Fills in the sum until we have no more digits.
 		while (biggest.size() != 0) {
 
 			// Makes sure we have no overflow.
 			int digitSmallest = (smallest.size() > 0 ? smallest.pop() : 0);
 			int digitBiggest = (biggest.size() > 0 ? biggest.pop() : 0);
 
 			// Generate the next digit.
 			int finalDigit = (digitBiggest - digitSmallest - carry);
 
 			// Determines if there is a carry or not.
 			if (finalDigit < 0) {
 				finalDigit += this.base;
 				carry = 1;
 			} else {
 				carry = 0;
 			}
 
 			difference.coefficients.add(finalDigit);
 
 			// Debug Line to summarise the action of add().
 			// System.out.println("diff[" + difference.coefficients.size()
 			// + "]\t= " + difference.coefficients.peekLast() + "\t = "
 			// + digitBiggest + " - " + digitSmallest + "\t(" + carry
 			// + ")");
 		}
 
 		return cleanTrailingZeros(difference);
 	}
 
 	// The multiply method should NOT be the same as what you learned in
 	// grade school since that method requires space proportional to the
 	// square of the number of coefficients in the number. Instead,
 	// you must write a method that uses space that is proportional to
 	// the number of coefficients. This can be done by basically
 	// changing the order of loops, as was sketched in class.
 	//
 	// You are not allowed to simply perform addition repeatedly.
 	// Such a method would be correct, but way too slow to be useful.
 
 	public NaturalNumber multiply(NaturalNumber second) throws Exception {
 
 		// initialises product as an empty list of coefficients.
 		NaturalNumber product = new NaturalNumber(this.base);
 		NaturalNumber partialProduct = new NaturalNumber(this.base);
 
 		// Copies the two numbers to avoid modification by mistake.
 		NaturalNumber x = this.clone();
 		NaturalNumber y = second.clone();
 
 		// Produces a copy of the two lists to prevent any modifications.
 		LinkedList<Integer> firstList = x.clone().coefficients;
 		LinkedList<Integer> secondList = y.clone().coefficients;
 
 		int carry = 0;
 		int digitToAdd = this.base;
 
 		// Reads the entire second number.
 		while (secondList.size() != 0 || carry != 0) {
 
 			// Makes sure we have no overflow.
 			int digitOfSecond = (secondList.size() > 0 ? secondList.pop() : 0);
 
 			// Clears the partial Product we will fill in.
 			partialProduct.coefficients.clear();
 
 			for (int i = 0; i < firstList.size() || carry != 0; i++) {
 
 				// Makes sure we have no overflow.
 				int digitOfFirst = (firstList.size() > i ? firstList.get(i) : 0);
 
 				// Fills in the list of int.
 				digitToAdd = (digitOfFirst * digitOfSecond + carry) % this.base;
 				partialProduct.coefficients.add(digitToAdd);
 
 				// Stores the carry.
 				carry = (digitOfFirst * digitOfSecond + carry) / this.base;
 
 				// System.err.println("\td: " + digitToAdd + "\tc: " + carry
 				// + "\tpart : " + partialProduct.toString());
 			}
 
 			// Adds the partial product to the final result.
 			product = product.add(partialProduct);
 
 			// Adds a zero to x, equivalent to shifting left in original
 			// algorithm.
 			firstList.push(0);
 
 		}
 
 		return cleanTrailingZeros(product);
 	}
 
 	// The divide method divides 'this' by 'second' i.e. this/second.
 	// 'this' is the "dividend", 'second' is the "divisor".
 	// This method ignores the remainder.
 	//
 	// You are not allowed to simply subtract the divisor repeatedly.
 	// This would give the correct result, but it is way too slow!
 
 	public NaturalNumber divide(NaturalNumber divisor) throws Exception {
 
 		// Initializes quotient as an empty list of coefficients
 		NaturalNumber quotient = new NaturalNumber(this.base);
 
				// Produces a copy of the two lists to prevent any modifications.
 		NaturalNumber mainDividend = this.clone();
 		NaturalNumber mainDivisor = divisor.clone();
 		NaturalNumber tempDividend = new NaturalNumber(this.base);
 
 		// Loops while we haven't read every digits of the Divisor.
 		while (mainDividend.coefficients.size() > 0) {
 
 			// Starts reading the Dividend.
 			tempDividend.coefficients
 					.push(mainDividend.coefficients.pollLast());
 
 			// Logs the Dividend. (Debug Line)
 			// System.err.print("tempDividend = " + tempDividend);
 
 			// Counts how many times the Divisor fills in the Dividend.
 			int digitQuotient = 0;
			while (tempDividend.compareTo(mainDivisor) > 0) {
 
 				tempDividend = tempDividend.subtract(mainDivisor);
 				digitQuotient++;
 
 			}
 
 			// Records the previous number of times as the first digit of
 			// Quotient.
			if (digitQuotient != 0) {
				quotient.coefficients.push(digitQuotient);
			}
 
 			// Logs the procedure. (Debug Line)
 			// System.err.println("\t= " + digitQuotient + " * " + mainDivisor +
 			// " + " + tempDividend);
 		}
 
 		return cleanTrailingZeros(quotient);
 	}
 
 	private NaturalNumber cleanTrailingZeros(NaturalNumber toClean) {
 
 		// Cleans the number to get ride of all the zeros.
 		while (toClean.coefficients.peekLast() == 0
 				&& toClean.coefficients.size() > 1) {
 			toClean.coefficients.removeLast();
 		}
 		return toClean;
 	}
 
 	/*
 	 * The methods should not alter the two numbers. If a method require that
 	 * one of the numbers be altered (e.g. borrowing in subtraction) then you
 	 * need to clone the number and work with the cloned number instead of the
 	 * original.
 	 */
 	@Override
 	public NaturalNumber clone() {
 
 		// For technical reasons we'll discuss later, this methods
 		// has to be declared public (not private).
 		// This detail need not concern you now.
 
 		NaturalNumber copy = new NaturalNumber(this.base);
 		for (int i = 0; i < this.coefficients.size(); i++) {
 			copy.coefficients.addLast(new Integer(this.coefficients.get(i)));
 		}
 		return copy;
 	}
 
 	/*
 	 * The subtraction method computes a-b and requires that a>b. The
 	 * a.compareTo(b) method is useful for checking this condition. It returns
 	 * -1 if a < b, it returns 0 if a == b, and it returns 1 if a > b.
 	 */
 
 	private int compareTo(NaturalNumber second) {
 
 		int diff = this.coefficients.size() - second.coefficients.size();
 		if (diff < 0)
 			return -1;
 		else if (diff > 0)
 			return 1;
 		else {
 			boolean done = false;
 			int i = this.coefficients.size() - 1;
 			while (i >= 0 && !done) {
 				diff = this.coefficients.get(i) - second.coefficients.get(i);
 				if (diff < 0) {
 					return -1;
 				} else if (diff > 0)
 					return 1;
 				else {
 					i--;
 				}
 			}
 			return 0;
 		}
 	}
 
 	/*
 	 * computes a*base^n where a is the number represented by 'this'
 	 */
 
 	private NaturalNumber multiplyByBaseToThePower(int n) {
 		for (int i = 0; i < n; i++) {
 			this.coefficients.addFirst(new Integer(0));
 		}
 		return this;
 	}
 
 	// This method is invoked by System.out.print.
 	// It returns the string with coefficients in the reverse order
 	// which is the natural format for people to reading numbers,
 	// i.e.. [ a[N-1], ... a[2], a[1], a[0] ] as in the Tester class.
 	// It does so simply by make a copy of the list with elements in
 	// reversed order, and then printing the list using the LinkedList's
 	// toString() method.
 
 	public String toString() {
 		LinkedList<Integer> reverseCoefficients = new LinkedList<Integer>();
 		for (int i = 0; i < coefficients.size(); i++)
 			reverseCoefficients.addFirst(coefficients.get(i));
 		return reverseCoefficients.toString();
 	}
 
 }
