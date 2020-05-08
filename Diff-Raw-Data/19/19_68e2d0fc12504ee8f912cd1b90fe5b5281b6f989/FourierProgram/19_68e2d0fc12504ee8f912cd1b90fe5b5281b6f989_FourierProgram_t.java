 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 public class FourierProgram {
 
	public static native int fourier(int[] samples, int k);
 
	static {
		System.load((new File("ft.dll").getAbsolutePath()));
	}
 	
 	public static int fourierJ(int[] ints, int k) {
 		double sumi = 0;
 		double sumr = 0;
 
 		for (int i = 0; i < ints.length; i++) {
 			sumr += ints[i] * Math.cos(2 * 3.141596 * i * k / ints.length);
 			sumi += ints[i] * Math.sin(2 * 3.141596 * i * k / ints.length);
 		}
 
 		return (int) (Math.sqrt(sumr * sumr + sumi * sumi) / (k == 0 ? ints.length: 1));
 		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		//System.loadLibrary("ft");
 		int windowSize = 256;
 		
 		Scanner in = null;
 
 		if (args.length == 0) {
 			in = new Scanner(System.in);
 		} else {
 			try {
 				in = new Scanner(new File(args[0]));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		int sampleRate = Integer.parseInt(in.nextLine().substring(1).trim());
 		String fileType = in.nextLine();
 
 		//if (!fileType.equals("#MONO"))
 		//	return;
 
 		LinkedList<Integer> nums = new LinkedList<Integer>();
 
 		long start = System.currentTimeMillis();
 		while (in.hasNext()) {
 			nums.add(in.nextInt());
 		}
 
 		int[] ints = new int[nums.size()];
 
 		for (int i = 0; i < nums.size(); i++) {
 			ints[i] = nums.get(i);
 		}
 
 		int[] fMags = new int[nums.size() / 2];
 
 		for (int i = 0; i < nums.size() / 2; i++) {
			fMags[i] = fourier(ints, i);
 		}
 
 		for (int i = 0; i < fMags.length; i++) {
 			System.out.println(fMags[i]);
 		}
 		
 		long end = System.currentTimeMillis();
 		System.out.print((end - start)/ 1000.0);
 	}
 
 }
