 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 
 public class MagicSquaresTest {
 	
 	@Test
 	public void testMainOrder1() {
 		MagicSquares.main(new String[] {"1"});
 	}
 	
 	@Test
 	public void testMainOrder2() {
 		MagicSquares.main(new String[] {"2"});
 	}
 	
 	@Test
 	public void testMainOrder3() {
 		MagicSquares.main(new String[] {"3"});
 	}
 	
 	@Test
 	public void testMainOrder4() {
 		MagicSquares.main(new String[] {"4"});
 	}
 	
 	@Test
 	public void testMatrixRotation() {
 		MagicSquares obj = new MagicSquares(4);
 		int[] data = new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
 		MagicSquares.SquareMatrix m = obj.new SquareMatrix(data);
 		m.rotate_right(data);
 		assertArrayEquals(data, new int[] {13,9,5,1,14,10,6,2,15,11,7,3,16,12,8,4});
 		m.rotate_right(data);
 		assertArrayEquals(data, new int[] {16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1});
 		m.rotate_right(data);
 		assertArrayEquals(data, new int[] {4,8,12,16,3,7,11,15,2,6,10,14,1,5,9,13});
 		m.rotate_right(data);
 		assertArrayEquals(data, new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
 	}
 	
 	@Test
 	public void testMatrixTransposition() {
 		MagicSquares obj = new MagicSquares(4);
 		int[] data = new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
 		MagicSquares.SquareMatrix m = obj.new SquareMatrix(data);
 		m.transpose(data);
 		assertArrayEquals(m.data, new int[] {1,5,9,13,2,6,10,14,3,7,11,15,4,8,12,16});
 		m.transpose(data);
 		assertArrayEquals(m.data, new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
 	}
 
 	@Test
 	public void testMatrixEquals() {
 		MagicSquares obj = new MagicSquares(4);
 		MagicSquares.SquareMatrix m1 = obj.new SquareMatrix(new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
 		int[][] e = new int[][] {
 				{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16},
 				{13,9,5,1,14,10,6,2,15,11,7,3,16,12,8,4},
 				{16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1},
 				{4,8,12,16,3,7,11,15,2,6,10,14,1,5,9,13},
 				{1,5,9,13,2,6,10,14,3,7,11,15,4,8,12,16},
 				{13,14,15,16,9,10,11,12,5,6,7,8,1,2,3,4},
 				{16,12,8,4,15,11,7,3,14,10,6,2,13,9,5,1},
 				{4,3,2,1,8,7,6,5,12,11,10,9,16,15,14,13}
 		};
 		for (int i = 0; i < e.length; i++) {
 			MagicSquares.SquareMatrix m2 = obj.new SquareMatrix(e[i]);
 			assertTrue(m1.equals(m2));
 		}
 	}
 	
 	
 	@Test
 	public void testGetSumCombintations() {
 		MagicSquares obj = new MagicSquares(4);
 		ArrayList<int[]> result = obj.get_sum_combinations();
 		for (int[] r: result) {
 			System.out.println(Arrays.toString(r));
 		}
 	}
 	
 	@Test
 	public void generateRuntimeStats() {
 		int sample_size = 10;
 		int max_order = 4;
 		for (int order = 1; order <= max_order; order++) {
 			
 			System.out.println("Order "+order+" stats:");
 			
 			long[] runtimes = new long[sample_size];
 			long ms_sum = 0;
 			for (int i = 0; i < sample_size; i++) {
 				MagicSquares obj = new MagicSquares(order);
 				obj.print_squares = false;
 				obj.init_magic_tree();
 				long end_time = System.currentTimeMillis();
 				long runtime = end_time - obj.start_time;
 				runtimes[i] = runtime;
 				ms_sum += runtime;
 				System.out.println("Trial "+(i+1)+":\t"+runtime+"ms");
 			}
 			
 			double average = (double) ms_sum / (double) sample_size;
 			
 			double deviation_sum = 0;
 			for (int i = 0; i < sample_size; i++) {
 				deviation_sum += Math.pow((double) runtimes[i] - (double) average,2);
 			}
 			double stdev = Math.sqrt(deviation_sum / ((double) sample_size-1));
 			
 			System.out.println("Avg:\t"+String.format("%f", average)+"ms");
 			System.out.println("StDev:\t"+String.format("%f", stdev)+"ms");
 			System.out.println();
 			
 		}
 		
 		
 	}
 	
 	@Test
 	public void testMagicTree3() {
 		MagicSquares obj = new MagicSquares(3);
 		obj.init_magic_tree();
 	}
 	
 	@Test
 	public void testMagicTree4() {
 		MagicSquares obj = new MagicSquares(4);
 		obj.print_squares = true;
 		obj.init_magic_tree();
 	}
 	
 	@Test
 	public void testMagicTree5() {
 		MagicSquares obj = new MagicSquares(5);
 		obj.init_magic_tree();
 	}
 	
 	@Test
	public void testSumCombinations5() {
		MagicSquares obj = new MagicSquares(5);
		MagicSquares.SumPermutationsList sum_permutations_list = obj.new SumPermutationsList();
	}
	
	@Test
 	public void testSumPermutationQuery() {
 		MagicSquares obj = new MagicSquares(4);
 		MagicSquares.SumPermutationsList sum_permutations_list = obj.new SumPermutationsList();
 		ArrayList<int[] >result = sum_permutations_list.query(new int[] {3,5});
 		for (int[] r: result) {
 			System.out.println(Arrays.toString(r));
 		}
 	}
 
 }
