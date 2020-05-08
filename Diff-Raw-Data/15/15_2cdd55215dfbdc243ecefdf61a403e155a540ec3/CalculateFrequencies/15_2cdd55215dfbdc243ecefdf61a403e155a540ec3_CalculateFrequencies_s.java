 package gr.di.uoa.a8.sivac.utils;
 
 import gr.di.uoa.a8.sivac.SiVaCGraph;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 
 public class CalculateFrequencies {
 
	private static Map<Double, Integer> freqs = new HashMap<Double, Integer>();
 
 	private static void calculateFrequencies(File file, int D) throws NumberFormatException, IOException {
 
		List<Integer> nodes = new ArrayList<Integer>();
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 		String line;
 		while ((line = br.readLine()) != null) {
 			String[] temp = line.split("\\s+");
 			int a = Integer.parseInt(temp[0]) - 1;
 			int b = Integer.parseInt(temp[1]) - 1;
 			nodes.add(a);
 			nodes.add(b);
 		}
 		int size = nodes.size();
 		System.out.println("Size: " + size);
 		br.close();
 		br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
 		int largest = SiVaCGraph.getSerialization(size - 1, size - 1, size, D);
 		byte[] array = new byte[largest / 8 + (largest % 8 != 0 ? 1 : 0)];
 		while ((line = br.readLine()) != null) {
 			String[] temp = line.split("\\s+");
 			int a = Integer.parseInt(temp[0]) - 1;
 			int b = Integer.parseInt(temp[1]) - 1;
 			if (SiVaCUtils.isDiagonal(a, b, D)) {
 				int no = SiVaCGraph.getSerialization(a, b, size, D);
 				array[no / 8] = SiVaCGraph.set_bit(array[no / 8], no % 8);
 			}
 
 		}
 		br.close();

 		for (int i = 0; i < size; i++)
 		{
 			double number = 0;
 			for(int j=i-D;j<i+D+1;j++)
 			{
 				try
 				{
 					int no = SiVaCGraph.getSerialization(i, j, size, D);
 					if(SiVaCGraph.isSet(array[no / 8], no % 8))
 						number += Math.pow(2, j-i+D);	
 				}
 				catch(Exception e)
 				{
 				}
 				
 			}
 			if (!freqs.containsKey(number))
 				freqs.put(number, 1);
 			else
 				freqs.put(number, freqs.get(number) + 1);
 		}
 
 //		int cur = 0;
 //		int number = 0;
 //		int max = 0;
 //		boolean[] cur_byte = new boolean[2 * D + 1];
 //		// for(int i=0;i<Math.pow(2*D+1-1, 2)*2;i++)
 //		// freqs.put(i, 0);
 //		while ((line = br.readLine()) != null) {
 //			String[] temp =if (!freqs.containsKey(number))
 //		freqs.put(number, 1);
 //	else
 //	 line.split("\\s+");
 //			int a = Integer.parseInt(temp[0]) - 1;
 //			int b = Integer.parseInt(temp[1]) - 1;
 //			if (a > max)
 //				max = a;
 //			if (b > max)
 //				max = b;
 //			if (SiVaCUtils.isDiagonal(a, b, D)) {
 //				if (a == cur) {
 //					cur_byte[b - a + D] = true;
 //				} else if (a > cur) {
 //					for (int i = 0; i < 2 * D + 1; i++) {
 //						if (cur_byte[i] == true)
 //							number += Math.pow(2, i);
 //					}
 //					if (!freqs.containsKey(number))
 //						freqs.put(number, 1);
 //					else
 //						freqs.put(number, freqs.get(number) + 1);
 //					cur_byte = new boolean[2 * D + 1];
 //					cur++;
 //					while (a > cur) {
 //						number = 0;
 //						if (!freqs.containsKey(number))
 //							freqs.put(number, 1);
 //						else
 //							freqs.put(number, freqs.get(number) + 1);
 //						cur++;
 //					}
 //					number = 0;
 //					cur_byte[b - a + D] = true;
 //				}
 //			}
 //
 //		}
 //		br.close();
 		System.out.println(D + ": " + freqs);
 	}
 
 	public static void main(String[] args) throws FileNotFoundException, IOException {
 
 		String filename = args[0];
 		int iterations = Integer.parseInt(args[1]);
 		for (int i = 0; i < iterations; i++)
 			calculateFrequencies(new File(filename), i + 1);
 	}
 
 }
