 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 
 public class PrecisionRecallCalc {
 
 	
 	public static void main(String[] args) {
 		if(args.length < 2)
 		{
 			System.out.println("Arguments required: <learned network file> <gold standard network file> <output file path>");
 			return;
 		}
 		NetworkParser p = new NetworkParser();
 		ArrayList<Integer> learned = p.parse(args[0]);
 		ArrayList<Integer> standard = p.parse(args[1]);
 		int truePositives = getNumTruePositives(learned, standard);
		String error = null;
		double precision = 0;
		if(learned.size() == 0)
		{
			error = "No SNPs were accepted.";
		}
		else
		{
			precision = (double)truePositives/learned.size();
		}
		double recall = (double)truePositives/standard.size();
 		double distance = Math.sqrt(Math.pow(1-precision, 2)+Math.pow(1-recall, 2));
 		PrintWriter out;
 		try 
 		{
 			out = new PrintWriter(new FileWriter(args[args.length-1]));
 			out.println("Precision: "+precision);
 			out.println("Recall: "+recall);
 			out.println("Distance from perfect precision and recall: "+distance);
			if(error != null)
			{
				out.println(error);
			}
 			out.close();
 		} 
 		catch (IOException e) 
 		{
 			System.out.println("Can not write to file.");
 		} 
 	}
 	
 	public static int getNumTruePositives(ArrayList<Integer> learned, ArrayList<Integer> standard)
 	{ //returns the number of SNPs that are in the learned network that are also in the gold standard network
 		int counter = 0;
 		for(int i = 0; i < standard.size(); i++)
 		{
 			if(learned.contains(standard.get(i)))
 			{
 				counter++;
 			}
 		}
 		return counter;
 	}
 
 }
