 import java.util.Arrays;
 import java.util.Set;
 
 public class IntSubDescriptor extends ProblemDescriptor {
 	int borrows;
 	int[] numDigits;
 	boolean borrowAcrossZero;
 	
 	public IntSubDescriptor() { }
 	
 	/**
 	 * currently only generates probs with positive diffs
 	 * 
 	 * I think this works for the most part now
 	 */
 	public Problem makeProblem() {
 		//check the fields and make sure they are a legal description
 		if (borrowAcrossZero) {
 			throw new UnsupportedOperationException("borrow across zero not yet implemented");
 			// return makeProbWithAcrossZero()...
 		} else {
 			if (numDigits.length != 2)
 				throw new IllegalArgumentException("can only make subtraction probs with two operands");
 			
 			if (Math.max(numDigits[0], numDigits[1]) < borrows)
 				throw new IllegalArgumentException("too many borrows for the number of digits");
 			
 			
 			int[] first = new int[Math.max(numDigits[0], numDigits[1])];
 			int[] second = new int[first.length];
 			int smaller = Math.min(numDigits[0], numDigits[1]);
			Set<Integer> borrowIndices = chooseIndices(borrows, (first.length - smaller) + 1, second.length);
 			
 			first[0] = DigitGenerator.randomNonZeroDigit();
 			for (int i = 1; i < first.length; i++) {
 				
 				if (borrowIndices.contains(i) && borrowIndices.contains(i + 1)) // check for other cases too!
 					first[i] = DigitGenerator.randomNonZeroDigit();
 				else if (borrowIndices.contains(i) && !borrowIndices.contains(i + 1))
 					first[i] = DigitGenerator.inRange(0, 9);
 				else if (borrowIndices.contains(i + 1) && !borrowIndices.contains(i))
 					first[i] = DigitGenerator.randomNonZeroDigit();
 				else
 					first[i] = DigitGenerator.randomDigit();
 			}
 			
 			int[] afterBorrows = new int[first.length];
 			for (int i = 0; i < afterBorrows.length; i++)
 				afterBorrows[i] = borrowIndices.contains(i + 1) ? first[i] - 1 : first[i];
 			
 			for (int i = 1; i <= smaller; i++) {
 				if (borrowIndices.contains(second.length - i))
 					second[second.length - i] = DigitGenerator.withBorrow(afterBorrows[first.length - i]);
 				else
 					second[second.length - i] = DigitGenerator.noBorrow(afterBorrows[first.length - i]);
 			}
 
 			System.out.println(borrowIndices);
 			System.out.println(Arrays.toString(first));
 			System.out.println(Arrays.toString(second));
 			
 			int op1 = intFromArray(first);
 			int op2 = intFromArray(second);
 			
 			System.out.println(op1 - op2);
 			System.out.println();
 			
 			return null;
 		}
 	}
 	
 	
 	
 }
