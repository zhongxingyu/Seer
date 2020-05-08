 package project2;
 import java.util.Arrays;
 public class parcelList {
 	parcel[] newParcel;
 	private int numOfParcels;
 	private final int SIZE = 10;
 	
 public parcelList() {
 	newParcel = new parcel[SIZE];
 	numOfParcels = 0;
 }
 public parcelList (int a) {
 	if (a <= 0)
 	{
 	newParcel = new parcel[SIZE];
 	}
 	else
 	{
 		newParcel = new parcel[a];
 	}
 	numOfParcels = 0;
 }
 public boolean addParcel (parcel x) {
 	if (numOfParcels != newParcel.length)
 	{
 		newParcel[numOfParcels] = x;
 		numOfParcels++;
 		return true;
 	}
 	else
 	{
 		return false;
 	}
 }
 public int getNumOfParcels () {
 	return numOfParcels;
 }
 public String[] summaryParcels() {
 	String[] msg = new String [newParcel.length];
 	for (int i = 0;i<newParcel.length;i++)
 	{
 		if (newParcel[i] == null)
 		{
 			msg[i] = "\n" + (i+1) + ".Empty";
 		}
 		else
 		{
 			msg[i] = "\n" + (i+1) + "." + newParcel[i].getCost();
 		}
 	}
 	return msg;
 }
 public String[] allParcels() {
 	String[] parcels = new String [newParcel.length];
 	for (int i=0;i<newParcel.length;i++)
 	{
 		if (newParcel[i] == null)
 		{
 			parcels[i] = "\n" + (i+1) + ".Empty";
 		}
 		else
 		{
 			parcels[i] ="\n" + (i+1) + "." + newParcel[i].toString();
 		}
 	}
 	return parcels;
 }
 public double totalCost() {
 	double tcost = 0;
 	for (int i = 0;i<newParcel.length;i++)
 	{
 		if (newParcel[i] == null)
 		{
 			tcost = tcost + 0;
 		}
 		else
 		{
 			tcost = tcost + newParcel[i].getCost();
 		}
 	}
 	return tcost;
 }
 public String getMaxCostParcel() {
 	double maxCost = newParcel[0].getCost();
 	String msg = "";
 	for (int i=1;i<newParcel.length;i++)
 	{
 		if (newParcel[i] != null)
 		{
 			if(newParcel[i].getCost() > maxCost)
 			{
 				maxCost = newParcel[i].getCost();
 			}	
 		}msg = "Found the max cost parcel " + "with price " + maxCost; // not 100% correct, need more think
 	}
 	return msg;
 }
 public String getMinCostParcel() {
 	double minCost = newParcel[0].getCost();
 	String msg = "";
 	for (int i=1;i<newParcel.length;i++)
 	{
 		if (newParcel[i] != null)
 		{
 		if(newParcel[i].getCost() < minCost)
 		{
 			minCost = newParcel[i].getCost();
 		}
 		}msg = "Found the min cost parcel " + "with price " + minCost; //index numbering is still incorrect, like in maxcost, need more think
 	}
 	return msg;
 }
 public void getParcelByCost(double cost1, double cost2) {
 	double minCost = cost1, maxCost = cost2;
 	boolean check = false;
 	int i = 0;
 	for (i = 0;i<newParcel.length;i++)
 	{
 		if (newParcel[i] != null)
 		{
 			if (minCost == newParcel[i].getCost())
 			{
 				if (maxCost == newParcel[i].getCost())
 				{
 					check = true;
 				}
 			}
 		}
 		if (check == true)
 		{
 			for (i = 0;i<newParcel.length;i++)
 			{
 				if (newParcel[i] != null)
 				{
 					if (newParcel[i].getCost() >= minCost)
 					{
 						if (newParcel[i].getCost() <= maxCost)
 						{
 							System.out.print("\n" + newParcel[i].getCost());
 						}
 					}
 				}
 			}
 		}
 	}
 }
 public void getParcelsByDestination (char a) {
 	boolean found = false;
 	for (int i=0; i < newParcel.length;)
 	{
 		if (a == newParcel[i].getDestination())
 		{
 			found = true;
 			System.out.print("\n" + newParcel[i]);
 		}
 		else
 		{
 			i++;
 		}
 	}
 	if (found == false)
 	{
 		System.out.print("\n");
 	}
 }
 public String[] sortedBy(String sort) {
 	String[] hasil = new String [newParcel.length];
 	parcel[] copy = new parcel [newParcel.length];
 	int i = 0;
 	for (i = 0;i<newParcel.length;i++)
 	{
 		copy[i] = newParcel[i];
 	}
 	if (sort.equalsIgnoreCase("weight"))
 	{
		Arrays.sort(newParcel[i].getParcelWeight());//this one gives "- The method sort(int[]) in the type Arrays is not applicable for the arguments (double)"
 		for (i = 0;i<newParcel.length;i++)
 		{
 			if (newParcel[i] != null)
 			{
 				
 			}
 		}
 	}
 	return hasil;
 	}
 	}
