 package annahack.utils;
 
 import java.util.ArrayList;
 
 public class DiceSet 
 {
 	private double average, stdDev;
 	private int min, max;
 	private ArrayList<Dice> dice=new ArrayList<Dice>();
 	
 	/**
 	 * Constructs and calculates DiceSet
 	 * @param takes a string in the form of "2d6+8d12+4 (constant must be at end)"
 	 * @throws BadDiceException
 	 */
 	public DiceSet(String d) throws BadDiceException
 	{
 		String[] a=d.split("\\+");
 		
 		min=0;
 		max=0;
 
 		int add=0;
 		if(!a[a.length-1].contains("d"))
 		{
 			add=Integer.parseInt(a[a.length-1]);
 			min+=add;
 			max+=add;
 			String[] temp=new String[a.length-1];
 			for(int i=0; i<a.length-1; i++)
 			{
 				temp[i]=a[i];
 			}
 			a=temp;
 		}
 		int rolls=1;
 		for(int i=0; i<a.length; i++)
 		{
 			dice.add(new Dice(a[i]));
 		}
 
 		
 		for(int i=0; i<dice.size(); i++)
 		{
 			for(int k=0; k<dice.get(i).getNum(); k++)
 			{
 				min+=1;
 				max+=dice.get(i).getMax();
 				rolls*=dice.get(i).getMax();
 			}
 		}
 		
 		int[] bins = new int[max+1]; //It will be easier this way, I swear
 		bins[add] = 1;
 		//Begin Dirty Hack (a la Chris)
 		for(int i=0; i<dice.size(); i++)
 		{
 			for(int j=0; j<dice.get(i).getNum(); j++)
 			{
 				int[] temp = new int[max+1];
 				
 				for(int k=1; k<=dice.get(i).getMax(); k++)
 				{
 					for(int aa = 0; aa < max+1-k;aa++)
 					{
 						temp[aa+k] += bins[aa];
 					}
 				}
 				
 				bins = temp;
 			}
 		}
 		//End Dirty Hack
 		int sum=0;
 		for(int i=0; i<bins.length; i++)
 		{
 			sum+=bins[i]*i;
 		}
 		average=(double)sum/rolls;
 		
 		//It's Standard Deviation time
 		double dsum=0.0;
 		for(int i=0; i<bins.length; i++)
 		{
 			dsum+=(Math.pow(i-average,2)*bins[i]);
 		}
		stdDev=Math.sqrt(dsum/(rolls));
 	}
 	
 	/**
 	 * 
 	 * @return minimum possible roll
 	 */
 	public int getMin()
 	{
 		return min;
 	}
 	
 	/**
 	 * 
 	 * @return maximum possible roll
 	 */
 	public int getMax()
 	{
 		return max;
 	}
 	
 	/**
 	 * 
 	 * @return average roll
 	 */
 	public double getAverage()
 	{
 		return average;
 	}
 	
 	/**
 	 * 
 	 * @return standard deviation
 	 */
 	public double getStdDev()
 	{
 		return stdDev;
 	}
 }
