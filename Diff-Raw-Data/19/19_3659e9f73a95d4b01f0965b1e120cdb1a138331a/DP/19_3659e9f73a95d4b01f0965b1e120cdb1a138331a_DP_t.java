 
 package DP;
 import java.io.*;
 import java.text.DecimalFormat;
 import java.util.*;
 
 import kMeans.Cluster;
 import kMeans.KMeans;
 import kMeans.KMeansResult;
 import kMeans.Point;
 /*
  * The stopping criteria numbers should be declared as constants in the programm 
  * The code is running Ok but needs little bit better documentation and review to be excelent
  * make the output with text !
 */
 import Data.AbstactData;
 import Data.IData;
 import Distribution.Distribute;
 import classifier.WekaClassifier;
 
 import Matrix.*;
 
 public class DP {
 	
 
 	private double [] storageD; //array of storage discretisation
 	private int sD; //storage discretization number
 	
 	
 	
 
 	
 	// next should be separate class flow diskretization and transition matrices
 	//maybe basic class and stochasticDP1 to be inheritance 
 	
 	private double [] flow;
 	private int fD; //flow discretization number-- how many flow that many periods
 	
 	
 
 	
 	//private double [][][] tVFDtable;
 
 	private double [][][]		tVVFDtableFlood;
 	private double [][][]		tVVFDtableRecreation;
 	
 	private double [][][]		tVVFDtableTowns_PZ;
 	private double [][][]		tVVFDtableTowns_SS;
 	private double [][][]		tVVFDtableAgriculture_UZ;
 	private double [][][]		tVVFDtableAgriculture_LZ;
 	private double [][][]		tVVFDtableEcology;
 	private double [][][]		tVVFDtableHydro_Power;
 	private double[][][] tVFDtable;
 	
 	
 	private double [] recreationWeights;
 	private double [] floodWeights;
 	private double [] townWeights_PZ;
 	private double [] townWeights_SS;
 	private double [] agricultureWeights_UZ;
 	private double [] agricultureWeights_LZ;
 	private double [] ecologyWeights;
 	private double [] hydropowerWeights;
 	
 	private double [][][] allocated;
 	
 	
 	private int  [][] stateFinal;
 	private double [][] releaseFinal;
 
 	double nInfiniteDouble = Double.NEGATIVE_INFINITY;
 	
 	private boolean print=true;
 	private double[][] sumSquareCal;
 
 	
 	private double [] reservoir_area;
 	
 	
 	
 	
 	
 	
 	
 	public DP()
 	{
 		
 	}
 	
 	
 	
 	boolean setDistributionAlgorithmLP=true;
 	
 	public void setDistriubtionAlgorithLP(boolean a )
 	{
 		setDistributionAlgorithmLP=a;
 	}
 	
 	
 	
 	public void inicialization () throws Exception
 	{
 		AbstactData test = new AbstactData();
 		int a;
 		a=test.testDatabaseConnection();
 		
 	IData test1 = new AbstactData();
 	
 	
 	double [] storagediskretization;
 	
 
 	storagediskretization=test1.getStorageDiscretisation();
 	
 		
 	for (int i=0;i<storagediskretization.length;i++)
 	System.out.println("Discretization of storage  ------" + storagediskretization[i]);
 	
 
 	double [] inflow;
 	
 	inflow=test1.getInflow();
 	
 	for (int i=0;i<inflow.length;i++)
 		System.out.println("Inflow" +i+"   " + inflow[i]);
 	System.out.println("Inflow.lenght" + inflow.length);
 
 	
 	this.storageD=storagediskretization;
 	this.flow=inflow;
 	this.fD=inflow.length;
 	this.sD=storagediskretization.length;
 	
 	}
 	
 	
 	
 	
 	
 	
 	public void setPrint()
 	{
 		this.print=true;
 	}
 	
 	
 	public void unPrint()
 	{
 		this.print=false;
 	}
 	
 	
 	public double [] reservoirVolumeToArea(double [] storage)
 	{
 		double [] reservoir_area = new double [sD];
 		reservoir_area[0]=0;
 		
 		
 		
 		for(int i=0;i<sD;i++)  { 
 									reservoir_area[i]=
 									0.00000000000004*Math.pow(storage[i], 3)
 									-0.000000002*Math.pow(storage[i], 2)
 									+0.00006*storage[i]
 									+0.0385;
 		System.out.println("the storage is "+storageD[i]+"the reservoir area is "+reservoir_area[i]);
 		}
 	/*	
 	 * 
 	 * for(int i=0;i<sD;i++)  { reservoir_area[i]=(-0.0006)*storage[i]*storage[i]+0.0454*storage[i]+0.0574;
 		System.out.println("the storage is "+storageD[i]+"the reservoir area is "+reservoir_area[i]);
 		}
 	 * 
 	 * 
 double [] reservoir_area1= new double [sD];
 		
 		for (int i=0;i<sD;i++)
 		{
 			if ((storage[i]>=0)&& (storage[i]<0.2581015)){ reservoir_area1[i]=0.2*storage[i];} 
 			else if ((storage[i]>=0.2581015)&& (storage[i]<1.00194226)) {reservoir_area1[i]=0.1112*storage[i]+0.0229;}
 				else if ((storage[i]>=1.00194226)&& (storage[i]<3.20902792)) {reservoir_area1[i]=0.449*storage[i]+0.0893;}
 					else if ((storage[i]>=3.20902792)&& (storage[i]<6.10104382)) {reservoir_area1[i]=0.0385*storage[i]+0.1099;}
 						else if ((storage[i]>=6.10104382)&& (storage[i]<10.11895657)) {reservoir_area1[i]=0.0283*storage[i]+0.1721;}	
 					else if ((storage[i]>=10.11895657)&& (storage[i]<15.36823722)) {reservoir_area1[i]=0.0252*storage[i]+0.2033;}	
 				else if ((storage[i]>=15.36823722)&& (storage[i]<22.01049192)) {reservoir_area1[i]=0.022*storage[i]+0.2531;}	
 			else if ((storage[i]>=22.01049192)&& (storage[i]<30.16427517)) {reservoir_area1[i]=0.0192*storage[i]+0.3157;}
 
 			
 			else {reservoir_area1[i]=0.0192*storage[i]+0.3157;}
 
 		}
 		
 		*/
 			for(int i=0;i<sD;i++)
 			{
 				System.out.println("the reservoir area by function " +reservoir_area[i] + " piecewise  "+ reservoir_area[i] +
 						"  difference "+ (reservoir_area[i]-reservoir_area[i]));
 			}
 		
 		
 		return reservoir_area;
 	}
 	
 	
 	double [] evapotranspiration = {0.0015,	0.006,	0.0228,	0.049,	0.088,	0.117,	0.134,	0.124,	0.083,	0.048,	0.017,	0.0047};
 	double alpha=100;  // to be in the new settings average of 100 per step when infow is between 100-4000
 	
 	
 	public void FlowDeviationTable() throws Exception
 	{
 		
 	
 		
 		
 		int i,k,brperiodi;
 		double [][][] allocated = new double [fD][sD][sD];
 		
 		
 		// initialization of seepage losses and evapotranspiraion
 		//double [][][] losses= new double[fD][sD][sD];
 
 		
 		/*here the reservoir area is calculated from the storage volume
 		 * the first 0 is taken as 0 because the regression formula is not good for 0 !! not now
 		 */
 		double [] reservoir_area = new double [sD];
 		
 		reservoir_area=reservoirVolumeToArea( storageD);
 		
 		
 		
 			
 		
 		for (brperiodi=0;brperiodi<fD;brperiodi++)
 		
 		{	
 		for (i=0;i<sD;i++)
 			{
 			
 				for(k=0;k<sD;k++)
 				{
 				if(	(storageD[i]+flow[brperiodi]-storageD[k]-alpha*evapotranspiration[brperiodi%12]*((reservoir_area[i]+reservoir_area[k])/2))<0)
 					allocated[brperiodi][i][k]=nInfiniteDouble;   
 				else
 					allocated[brperiodi][i][k]=storageD[i]+flow[brperiodi]-storageD[k];
 				
 				//the allocated is deacrised by the evapotranspiration 
 				allocated[brperiodi][i][k]=allocated[brperiodi][i][k]-alpha*evapotranspiration[brperiodi%12]*(reservoir_area[i]+reservoir_area[k])/2;
 			//System.out.println("the allocated is "+allocated[brperiodi][i][k]+"the evapotranspirat "+alpha*reservoir_area[i]*evapotranspiration[i]+"   procentage " +alpha*reservoir_area[i]*evapotranspiration[i]/allocated[brperiodi][i][k]);
 					
 				}
 			}
 		}
 		
 		this.allocated=allocated;
 		//Matrix3DDouble pr1= new Matrix3DDouble(allocated);
 		//System.out.println("od tukaa");
 		//pr1.PrintMatrix3D();
 		//System.out.println("do tukaaa");
 		
 		
 		DPOptimalAllocation t1=new DPOptimalAllocation(allocated,sD,fD,storageD,flow,setDistributionAlgorithmLP);
 		t1.initialize(); //don't forget this line to initialize the variables
 		t1.distribute_Middle_inflow(); // THIS IS THE NEW CODE FOR DISTRIBUTION OF MIDDLE INFLOW BETWEEN USERS AND FUNCTIONS!!!
 		this.tVFDtable=t1.sumSquaredDeviation();
 		
 		
 		this.tVVFDtableFlood=t1.getSquareErrorFlood();
 		this.tVVFDtableRecreation=t1.getSquareErrorRecreation();
 		this.tVVFDtableTowns_PZ=t1.getSquareErrorTargetTowns_PZ();
 		this.tVVFDtableTowns_SS=t1.getSquareErrorTargetTowns_SS();
 		this.tVVFDtableAgriculture_UZ=t1.getSquareErrorTargetAgriculture_UZ();
 		this.tVVFDtableAgriculture_LZ=t1.getSquareErrorTargetAgriculture_LZ();
 		this.tVVFDtableEcology=t1.getSquareErrorTargetEcology();
 		this.tVVFDtableHydro_Power=t1.getSquareErrorHydro_Power();
 
 		
 		this.agricultureWeights_UZ=t1.getAgricultureWeights_UZ();
 		this.agricultureWeights_LZ=t1.getAgricultureWeights_LZ();
 		this.ecologyWeights=t1.getEcologyWeights();
 		this.townWeights_PZ=t1.getTownsWeights_PZ();
 		this.townWeights_SS=t1.getTownsWeights_SS();
 		this.floodWeights=t1.getFloodWeights();
 		this.recreationWeights=t1.getRecreationWeight();
 		this.hydropowerWeights=t1.getHydroPowerWeight();
 		
 		
 		
 		//this.releaseTargets=t1.getReleaseTargets();
 		
 		//System.out.println("444444444444444444444 ");
 		//System.out.println(" ");
 		
 		//Matrix3DDouble t1115=new Matrix3DDouble(tVVFDtableFlood);
 		//t1115.PrintMatrix3D();
 		
 		
 		
 	}
 
 	public void DPCalculation() throws IOException
 	{
 		/*
 		 * Dynamic programming calculation made here is not designed to have multiple solutions
 		 * That means that in any state only one action is the best that leads to one unique final state
 		 * The code can be upgraded to have the capability to store multiple solutions 
 		 * Maybe better and more convenient solution is to make the total squared deviation table 
 		 * better i.e to have complex decision process and good distinction between states.
 		 * Also like an idea is to give positive reward (in this case decrease/increase TSD) if water is saved in s
 		 * winter period and more releases in summer... 
 		 * In practical implementation objectives will be more complex ( expecialy user demand)
 		 */
 		
 		// initialization of propagation matrix
 		//first iteration
 		int i,j;
 		
 		int [][] result= new int  [fD][sD]; //calculation result
 		double [] errorCal = new double[sD];
 		
 		double [][] sumSquareCalc = new double[fD][sD];
 	
 		int  [][] stateFinal = new int  [fD][sD];
 		double [][] releaseFinal = new double [fD][sD];
 	
 		// calculation will start from the last period and going backwards 	
 		
 		
 		/*
 		 * *down here first we calculate the errorCal[] which contains sumSquaredDeviation error between the states(volumes)
 		 * this is the  error of the final state-->action-->state because algorithm starts from back (propagation)
 		 * THIS INFACT IS THE FIRST STEP
 		 */
 		for (i=0;i<sD;i++)
 		{
 			errorCal[i]=tVFDtable[fD-1][i][0];
 			// System.out.println(" Error888 "+errorCal[i] + "   "+ i);
 			for(j=1;j<sD;j++) 
 			{			
 				if (errorCal[i]>=tVFDtable[fD-1][i][j])
 				{errorCal[i]=tVFDtable[fD-1][i][j];
 				stateFinal[fD-1][i]=j;                              //THE ORIGINAL CODE IS CHANGED HERE
 				releaseFinal[fD-1][i]=allocated[fD-1][i][j];
 				/*System.out.print(" Release "+releaseFinal[fD-1][i]);
 				System.out.print(" Final state "+stateFinal[fD-1][i]);
 				System.out.print(" Error "+errorCal[i]);
 				System.out.println(" ");*/
 				}
 				
 			}
 		}
 	
 	/*	for (i=0;i<sD;i++)
 		{
 		
 		System.out.print(" Release1 "+releaseFinal[fD-1][i]);
 		System.out.print(" Final state1 "+stateFinal[fD-1][i]);
 		System.out.print(" Error1 "+errorCal[i]);
 		System.out.println(" ");
 		
 		
 		}
 	*/
 		
 		
 		// number of itterration
 		double[][] PropagationMatrix=new double[sD][sD];
 		
 		int itterationCycle=0;
 		
 		int brojac=fD*fD-2; // this is because one iteration is done previously -1
 		int Calperiod;
 		
 		int repeat_result=0; //stoping criteria
 		
 		
 		
 		while( brojac>0)  // brojac defines the maximum number of itteration for the cycle
 		{
 			Calperiod=brojac%fD;
 			if (Calperiod==0) itterationCycle++;
 			
 		//System.out.println("calperiod e  "+Calperiod+"  i brojac  "+brojac+"   i itterationCycle         "+ itterationCycle);
 			
 			
 			for (i=0;i<sD;i++)
 			{
 				
 				for(j=0;j<sD;j++) 
 				{	
 			PropagationMatrix[i][j]=tVFDtable[Calperiod][i][j]+errorCal[j];				
 				}
 			}
 		
 		//MatrixDouble pr=new MatrixDouble(PropagationMatrix);
 		//pr.PrintMatrix();
 		
 		//System.out.println("  ");
 		//System.out.println("  ");
 			
 		
 			for (i=0;i<sD;i++)
 			{
 				// state 0 -- first state is taken as start to compare with other states
 				errorCal[i]=PropagationMatrix[i][0];
 				releaseFinal[Calperiod][i]=allocated[Calperiod][i][0];
 				stateFinal[Calperiod][i]=i;
 				
 				for(j=0;j<sD;j++) 
 				{			
 					if (errorCal[i]>=PropagationMatrix[i][j])
 					{errorCal[i]=PropagationMatrix[i][j];
 					
 					releaseFinal[Calperiod][i]=allocated[Calperiod][i][j];
 					stateFinal[Calperiod][i]=j;
 					sumSquareCalc[Calperiod][i]=errorCal[i];
 					}
 					
 				}
 			}
 		
 		/*	for (i=0;i<sD;i++)
 			{
 				System.out.println("The error of each node and node value            the release and         final state  ");
 			System.out.print("  "+sumSquareCalc[Calperiod][i]+"                        "+i);
 			System.out.print("                           "+releaseFinal[Calperiod][i]);
 			System.out.print("           "+stateFinal[Calperiod][i]);
 			
 			System.out.println("  ");
 			}
 			
 			
 		*/
 			
 			//System.out.println("  ");
 		//	System.out.println("  ");
 			
 			
 			
 			
 			if ((itterationCycle>1) && (Calperiod==0))// this is needed for warm up the calculation and
 				//calperiod is the ending of the cycle
 			{	
 				MatrixDouble pom = new MatrixDouble();
 				
 				
 				if (pom.MatrixEqualInt(stateFinal, result)) repeat_result++;
 			
 				if (repeat_result==2)  // if two times the same final matrix is calculated then stop calculating
 			{ 
 				System.out.println("Prekiniiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii " );
 
 				
 				break;
 			}
 				else
 				{
 					result=pom.MatrixCopy(stateFinal);
 				}
 				
 
 	
 			}
 			
 			
 			
 			brojac--;
 			}
 		
 		
 		System.out.println(" This is the summary of the calculations ");
 		
 		
 		this.stateFinal=stateFinal;
 		this.releaseFinal=releaseFinal;
 		this.sumSquareCal=sumSquareCalc;
 		
 		
 		System.out.println(" States");
 		MatrixDouble pr1=new MatrixDouble(stateFinal);
 		pr1.PrintMatrixInt();
 		System.out.println("  ");
 		
 		
 		
 		double [][] stateFinalStorage = new double  [fD][sD];
 		
 		for(i=0;i<fD;i++)
 		for(j=0;j<sD;j++)
 			stateFinalStorage[i][j]=storageD[stateFinal[i][j]];
 		
 		
 		System.out.println(" States--volumes");
 		MatrixDouble pr4=new MatrixDouble(stateFinalStorage);
 		pr4.PrintMatrix();
 		System.out.println("  ");
 		
 		
 		
 		System.out.println(" Releases");
 		MatrixDouble pr2=new MatrixDouble(releaseFinal);
 		pr2.PrintMatrix();
 		System.out.println("  ");
 		
 		System.out.println(" Sum Square Errors by each state");
 		MatrixDouble pr3=new MatrixDouble(sumSquareCalc);
 		pr3.PrintMatrix();
 		System.out.println(" gotovoooooooooooooooooooooooooo ");
 		
 		
 		
 		
 		
 		
 		
 	/*	THIS IS FOR DATA DRIVEN MODELING PART
 		int timestep=0;
 		
 		segmentation();
 		//double [][][] resultsAdd = new double [12][sD][clasterValues.length];
 		//double [][][] resultsRelease = new double [12][sD][clasterValues.length];
 		//double [][][] resultsAddReleases = new double [12][sD][clasterValues.length];
 		int interval_which,k;
 		
 		
 		System.out.println(" ");
 		System.out.println(" ");
 		for(i=0;i<clasterValues.length;i++)
 			System.out.print("    " + clasterValues[i]);
 		System.out.println(" a sega intervalite");
 		
 		for (i=0;i<intervals.length;i++)
 			System.out.print("     "+intervals[i]);
 		System.out.println(" ");
 		System.out.println(" ");
 		
 		*/
 		
 		
 		
 		
 		
 		/*
 		for(i=0;i<fD;i++)
 		{
 			timestep=i%12;
 			
 			for (j=0;j<sD;j++)
 			{
 				System.out.println(timestep + "   " + storageD[j]+"   "+  flow[i]+"    "+releaseFinal[i][j]);
 				
 				interval_which=belongToInterval(flow[i]);
 				resultsAdd[timestep][j][interval_which]++;
 				resultsAddReleases[timestep][j][interval_which]+=releaseFinal[i][j];
 				
 				
 				System.out.println("**********"+ timestep+"   "+ storageD[j]+ "   " + interval_which+   "    " + flow[i]+ "    " + releaseFinal[i][j]+
 						"    "+resultsAdd[timestep][j][interval_which]+"    "+resultsRelease[timestep][j][interval_which]);
 			}
 			
 		}
 		
 		System.out.println("");
 		System.out.println("");
 		System.out.println("THIS IS FINAL TABLEEEEE1111");
 		
 		System.out.println("timestep storage flowSegment releasefinal" );
 		
 		for(i=0;i<fD;i++)
 		{
 			timestep=i%12;
 			
 			for (j=0;j<sD;j++)
 			{
 				interval_which=belongToInterval(flow[i]);
 				System.out.println(timestep + "   " + storageD[j]+"   "+  flow[i]+"    "+releaseFinal[i][j]);
 				
 				
 			}
 			
 		}
 		
 		
 		*/
 		
 		
 		
 		/* THIS IS NOT NEEDED RIGHT NOW IT IS FOR DATA DRIVEN MODELING
 		 * 
 		System.out.println("THIS IS FINAL TABLEEEEE");
 		System.out.println("");
 		System.out.println("");
 		System.out.println("");
 
 		
 		
 		String [] month = { "jan", "feb", "mar","apr","may", "jun","jul","aug","sep","oct","nov","dec"};
 		
 		
 		System.out.println("");
 		System.out.println("THIS IS FINAL TABLEEEEE22222");
 		
 		System.out.println("timestep storage flowSegment releasefinal" );
 		
 		for(i=60;i<fD-59;i++)
 		{
 			timestep=i%12;
 			
 			for (j=0;j<sD;j++)
 			{
 				interval_which=belongToInterval(flow[i]);
 				//System.out.println(month[timestep] + "," + storageD[j]+","+  clasterValues[interval_which]+","+releaseFinal[i][j]);
 			//	System.out.println(month[timestep] + "," + storageD[j]+","+  flow[i]+","+releaseFinal[i][j]);
 				//System.out.println(month[timestep] + "," + storageD[j]+","+  flow[i]+","+stateFinalStorage[i][j]);
 				
 				System.out.println(timestep + "," + j+","+  flow[i]+","+stateFinal[i][j]);
 			}
 			
 		}
 		
 		
 		
 		System.out.println("THIS IS FINAL TABLEEEEE");
 		System.out.println("");
 		System.out.println("");
 		System.out.println("");
 		
 		
 		
 		
 		
 		*/
 		
 		
 		
 		
 		
 		
 		
 		
 		/*  SHOULD BE DELETED
 		
 		
 		for(i=0;i<12;i++)
 		{
 			for (j=0;j<sD;j++)
 			{
 				for (k=0;k<clasterValues.length;k++)
 					if (resultsAdd[i][j][k]!=0)					resultsAddReleases[i][j][k]=resultsAddReleases[i][j][k]/resultsAdd[i][j][k];
 					else resultsAddReleases[i][j][k]=0;
 			}
 		}
 
 		
 		
 		
 		System.out.println(" time step          storage         number of time inflow        claster inflow       optimal_release   ");
 		
 		for(i=0;i<12;i++)
 		{
 			
 			
 			for (j=0;j<sD;j++)
 			{
 				for (k=0;k<clasterValues.length;k++)
 				{
 					
 					System.out.println( i+"   "+ storageD[j]+ "    "+resultsAdd[i][j][k]+"    "   + clasterValues[k]+"     "  +resultsAddReleases[i][j][k]);
 				}
 			}
 			
 		}
 		
 		
 		
 		this.resultRelease=resultsRelease;
 	*/
 		}
 		
 	
 	public double [][][] resultRelease;
 	public int sections;
 	public double [] intervals;
 	public double [] clasterValues;
 	
 	
 	public void segmentation () throws IOException
 	{
 		List<double[]> a;
 		int golemina=flow.length;
 			
 		int k_steps=(int) Math.sqrt(golemina/2);
 		
 		double [] niza = flow;
 		a=Arrays.asList(this.flow);
 		
 		List<Point> points = new ArrayList<Point>();
 
 		for(int i=0; i<niza.length; i++)
 		{
 			String m=String.valueOf(niza[i]);
 		    Point p = new Point(m);
 		    points.add(p);
 		}
 		
 
 		KMeans kmeans = new KMeans();
 		for (int k = 1; k <= k_steps; k++) 
 		{
 		    KMeansResult result = kmeans.calculate(points, k);
 		    	System.out.println("------- Con k=" + k + " ofv=" + result.getOfv() + "-------\n");
 		    	int i = 0;
 		    	for (Cluster cluster : result.getClusters()) 
 		    	{
 		    		i++;
 		    		System.out.println("-- Cluster " + i + " --\n");
 		    			for (Point punto : cluster.getPoints()) 
 		    				{
 		    				System.out.println(punto.getX() + ", " + "\n");
 		    				}
 		    	System.out.println("\n");
 		    	System.out.println(cluster.getCentroide().getX() + ", ");
 		    	System.out.println("\n\n");
 		    	}
 	
 		}
 		
 		int k1=4;
 		
 		InputStreamReader converter = new InputStreamReader(System.in);
         BufferedReader in = new BufferedReader(converter);
         System.out.println("----------------------------------------------------------------");
         System.out.println("Insert number of clasters");
         System.out.println("----------------------------------------------------------------");
 	String id;
    id= in.readLine();
    
    
 try {k1=Integer.parseInt(id);} catch (NumberFormatException e) {      System.exit(0);   }
 		
 
 KMeansResult result = kmeans.calculate(points, k1);
 double [] clastersInterval=new double [k1];
 
 System.out.println("------- Con k=" + k1 + " ofv=" + result.getOfv() + "-------\n");
 	int i = 0;
 	for (Cluster cluster : result.getClusters()) 
 	{
 	i++;
 	System.out.println("-- Cluster " + i + " --\n");
 		for (Point punto : cluster.getPoints()) 
 			{
 			System.out.println(punto.getX() + ", " + "\n");
 			}
 	System.out.println("\n");
 	System.out.println(cluster.getCentroide().getX() + ", ");
 	clastersInterval[i-1]=cluster.getCentroide().getX();
 	
 	System.out.println("\n\n");
 	}
 
 	
 	Arrays.sort(clastersInterval);
 	
 	
 	for (i=0;i<k1;i++)
 	{ System.out.println("\n Ova e rezultatoto111");
 		System.out.print(clastersInterval[i] + ", ");
 	}
 	
 	
 	
 	double [] intervals1 = new double [k1+1];
 	
 	intervals1[0]=0;
 	
 	System.out.print(" intervals  " +intervals1[0]);
 	
 	for (i=0;i<clastersInterval.length-1;i++)
 	{
 		intervals1[i+1]=clastersInterval[i]+(clastersInterval[i+1]-clastersInterval[i])/2 ;           // regular intervals -- can be with different interval not only regular
 	
 	}
 	
 	
 	intervals1[k1]=clastersInterval[k1-1]*10;  // the begining in 0 the end is twice the last one !!!
 	
 	for (i=0;i<intervals1.length;i++)
 	{
 		
 		System.out.print(" intervalsssssss  " +intervals1[i]);
 	}
 	
 	
 	
 	this.intervals=intervals1;
 	this.sections=k1;
 	this.clasterValues=clastersInterval;
 	
 	
 	
 	}
 	
 	
 	
 	public int  belongToInterval(double value)
 	{
 	
 	int i=0;
 	
 	for (i=0;i<intervals.length-1;i++)
 	{
 		if ((value>=intervals[i])&& (value<intervals[i+1]))
 		{
 			return i;
 		
 		}
 	}
 	System.out.println("this line should not print !!!!! " +value );
 	return i;
 	}
 	
 	
 	
 	
 	
 	public int Simulation(int st) throws Exception
 	{
 		double[] OptimalStorage=new double [fD];
 		                              //!!! this must/can be improved from witch state the path should start
 		int i,t;
 		
 		int finS;
 		// always the algoritm will start from 0 period 
 		// one cycle usualy one year
 		System.out.println(" This is simulation of the calculated results");
 		//String t1=" This is simulation of the calculated results";
 		//System.out.println("Period     Starting state     inflow         release        final state        TDSt");
 		
 		
 		double storageList [] = new double [fD];
 		double optimalStorage [] = new double [fD];
 		int repeat_result=0;
 		for(t=0;t<(10*fD);t++)
 		{
 			
 			
 			i=t%fD;
 		
 			finS=stateFinal[i][st];
 			
 System.out.println( i  +  "       "+ storageD[st]+ "               "+ flow[i]+"             "
 		+ releaseFinal[i][st]+"            " +storageD[finS]+ "             "+tVFDtable[i][st][finS] );
 			
 			storageList[i]=storageD[st];
 
 		st=finS;
                                                                                                             
 		
 		
 		if ((t>2*fD)&& (i==fD-1))		{
 			
 			ArrayDouble pom = new ArrayDouble(); 
 			pom.ArrayCompare(storageList, optimalStorage);
 			
 			
 			if (pom.ArrayCompare(storageList, optimalStorage)) repeat_result++;
 		
 			if (repeat_result==2)  // if two times the same final matrix is calculated then stop calculating
 		{ 
 			System.out.println("Prekiniiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii " );
 
 			
 			break;
 		}
 			else
 			{
 				optimalStorage=pom.ArrayDoubleCopy(storageList);
 			}
 		
 		}
 		
 		
 		}
 		
 		
 		System.out.println(" ");
 		System.out.println(" ");
 		System.out.println(" This is the final solution");
 		
 		
 		double TotalDeviationWeight=0;
 		double TotalDeviation=0;
 		double TotalDeviationSum=0;
 		
 		
 
 		double deviationRecreation=0;
 		double deviationFlood=0;
 		double deviationTowns_PZ=0;
 		double deviationTowns_SS=0;
 		double deviationAgriculture_UZ=0;
 		double deviationAgriculture_LZ=0;
 		double deviationEcology=0;
 		double deviationHP=0;
 		
 		
 		double deviationRecreationWithWeight=0;
 		double deviationFloodWithWeight=0;
 		double deviationTownsWithWeight_PZ=0;
 		double deviationTownsWithWeight_SS=0;
 		double deviationAgricultureWithWeight_UZ=0;
 		double deviationAgricultureWithWeight_LZ=0;
 		double deviationEcologyWithWeight=0;
 
 		double deviationHPWithWeight=0;
 		
 		double [][] MatrixPrint= new double [50][12];
 		int brojac=-1;
 		int tt=1;
 		
 		int return_strarting_state=st;
 		
 		 DecimalFormat df = new DecimalFormat("#,###,000.0");
 		
 		System.out.println("time      Storage     Inflow       Release     " +
 		"      Next Storage         TotalDev(with weight)       TotalDeviation        SquareTDFlood " +
 		"        SquareTDRecreation                     SQ Towns_PZ      SQ Towns_SS         SQ Agricultyre UZ       SQ Agricultyre LZ     SQ Ecology         SQ Hydro Power ") ;
 		for(i=61;i<fD-59;i++)
 		//for(i=0;i<fD;i++)
 		{
 			
 			
 			
 			//System.out.println("ova e vrednosta na i "+i+"   i na t"+t);
 		//	System.out.println("         "+period);
 			finS=stateFinal[i][st];
 			
 			OptimalStorage[i]=storageD[st];
 			
 			
 		
               TotalDeviationWeight+=tVFDtable[i][st][finS]; 
               TotalDeviation=tVVFDtableRecreation[i][st][finS]+tVVFDtableFlood[i][st][finS]+tVVFDtableTowns_PZ[i][st][finS]+tVVFDtableTowns_SS[i][st][finS]+
               tVVFDtableAgriculture_UZ[i][st][finS]+tVVFDtableAgriculture_LZ[i][st][finS]+tVVFDtableEcology[i][st][finS]+tVVFDtableHydro_Power[i][st][finS];
               TotalDeviationSum+=TotalDeviation;
              
               deviationRecreation+=tVVFDtableRecreation[i][st][finS];
               deviationRecreationWithWeight+=tVVFDtableRecreation[i][st][finS]*recreationWeights[i];
               
               deviationFlood+=tVVFDtableFlood[i][st][finS];
               deviationFloodWithWeight+=tVVFDtableFlood[i][st][finS]*floodWeights[i];
               
               deviationTowns_PZ+=tVVFDtableTowns_PZ[i][st][finS];
               deviationTownsWithWeight_PZ+=tVVFDtableTowns_PZ[i][st][finS]*townWeights_PZ[i];
               
               deviationTowns_SS+=tVVFDtableTowns_SS[i][st][finS];
               deviationTownsWithWeight_SS+=tVVFDtableTowns_SS[i][st][finS]*townWeights_SS[i];
               
               
               deviationAgriculture_UZ+=tVVFDtableAgriculture_UZ[i][st][finS];
               deviationAgricultureWithWeight_UZ+=tVVFDtableAgriculture_UZ[i][st][finS]*agricultureWeights_UZ[i];
               
               deviationAgriculture_LZ+=tVVFDtableAgriculture_LZ[i][st][finS];
               deviationAgricultureWithWeight_LZ+=tVVFDtableAgriculture_LZ[i][st][finS]*agricultureWeights_LZ[i];
               
               
               deviationEcology+=tVVFDtableEcology[i][st][finS];
               deviationEcologyWithWeight+=tVVFDtableEcology[i][st][finS]*ecologyWeights[i];
               
               deviationHP+=tVVFDtableHydro_Power[i][st][finS];
               deviationHPWithWeight+=tVVFDtableHydro_Power[i][st][finS]*hydropowerWeights[i];
               
            //   tt=i%12;
             //  if (tt==1) brojac++;
            //  MatrixPrint[brojac][tt]=storageD[st];
               
               
               System.out.println( i  +  "           "+ df.format(storageD[st])+ "       "+ df.format(flow[i])+"        "+
             		  df.format(releaseFinal[i][st])+"                   " +  df.format(storageD[finS])+ "                        "+df.format(tVFDtable[i][st][finS]) +"                          "
             		  + df.format(TotalDeviation)+"                    "+  df.format(tVVFDtableFlood[i][st][finS]) + "                         "+ df.format(tVVFDtableRecreation[i][st][finS])
   					+"                              "+ df.format(tVVFDtableTowns_PZ[i][st][finS])+"                              "+ df.format(tVVFDtableTowns_SS[i][st][finS])+
   					"                         " +df.format(tVVFDtableAgriculture_UZ[i][st][finS])+"                         " +df.format(tVVFDtableAgriculture_LZ[i][st][finS])
   					+"                        "+df.format(tVVFDtableEcology[i][st][finS])+ "                "+df.format(tVVFDtableHydro_Power[i][st][finS]));
   		
               
               
               
               
               st=finS;
 		}
 		
 System.out.println("TDS             RL                FL               PZ                UZ                SS              LZ               E              HP       " );
 		
 		System.out.println(df.format(TotalDeviationSum)+"   "+df.format(deviationRecreation)+"     "+df.format(deviationFlood)+"     "+
 				df.format(deviationTowns_PZ)+"    "+df.format(deviationAgriculture_UZ)+"      "+df.format(deviationTowns_SS)+"      "+
 				df.format(deviationAgriculture_LZ)+"      "+df.format(deviationEcology)+"      "+df.format(deviationHP));
 		System.out.println("  ");
 		System.out.println("");
 
 		
 		System.out.println("TDSWeight         RL             FL               PZ                UZ               SS               LZ              E              HP                                     " );
 		 
 				System.out.println(df.format(TotalDeviationWeight)+"   "+df.format(deviationRecreationWithWeight)+"     "+df.format(deviationFloodWithWeight)+"     "+
 				df.format(deviationTownsWithWeight_PZ)+"    "+df.format(deviationAgricultureWithWeight_UZ)+"      "+df.format(deviationTownsWithWeight_SS)+"      "+
 				df.format(deviationAgricultureWithWeight_LZ)+"      "+df.format(deviationEcologyWithWeight)+"      "+df.format(deviationHPWithWeight));
 		
 		
 		
 		
 		
 	
 		
 	/*	for (i=0;i<40;i++)
 		{
 			for (int j=0;j<12;j++)
 			{
 				System.out.print("  "  +MatrixPrint[i][j]);
 			}
 			System.out.println(" ");
 		}
 		*/
 		for(i=0;i<fD;i++)
 		{
 			System.out.println(OptimalStorage[i]);
 			
 		}
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		AbstactData test = new AbstactData();
 		int a;
 		a=test.testDatabaseConnection();
 		
 	IData test1 = new AbstactData();
 		
 		test1.putStorageOptimal(OptimalStorage,fD);
 	
 		
 		return return_strarting_state;
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	 * THIS IS THE PART OF SIMULATION !!!
 	 */
 	
 	
 	
 	
 	
 	int reservoir_volume_closest(double a)
 	{
 		//System.out.print("the volume   "+ a);
 		int i=0;
 		if(storageD[storageD.length-1]<=a) i=storageD.length-1;
 		else
 			if (a<storageD[0]) i=1;
 		else
 		{		while(storageD[i]<a) i++;}
 		
 		//System.out.println("the value "+a+" the storaged i " + storageD[i-1]);
 		
 		if ((i<1)&&(i>storageD.length-1))System.out.println("WROOOONG");
 		return i-1;
 	}
 	
 	
 	
 	
 	
 	public void simulation_with_2_stage(int st) throws Exception
 	{
 		
 		
 		/* ONLY FOR COMPARISON WITH 2 STAGE METHOD
 		 * 
 		 */
 		
 		
 		System.out.println(" ");
 		System.out.println(" ");
 		System.out.println(" This is the solution with 2 stage");
 		
 		
 		double TotalDeviationWeight=0;
 		double TotalDeviation=0;
 		double TotalDeviationSum=0;
 		
 		
 
 		double deviationRecreation=0;
 		double deviationFlood=0;
 		double deviationTowns_PZ=0;
 		double deviationTowns_SS=0;
 		double deviationAgriculture_UZ=0;
 		double deviationAgriculture_LZ=0;
 		double deviationEcology=0;
 		double TdeviationTowns_PZ=0;
 		double TdeviationTowns_SS=0;
 		double TdeviationAgriculture_UZ=0;
 		double TdeviationAgriculture_LZ=0;
 		double TdeviationEcology=0;
 		
 		
 		double deviationRecreationWithWeight=0;
 		double deviationFloodWithWeight=0;
 		double deviationTownsWithWeight_PZ=0;
 		double deviationTownsWithWeight_SS=0;
 		double deviationAgricultureWithWeight_UZ=0;
 		double deviationAgricultureWithWeight_LZ=0;
 		double deviationEcologyWithWeight=0;
 
 		double deviationHPWithWeight=0;
 		
 		int i;
 		
 		
 		DecimalFormat df = new DecimalFormat("#,###,000.0");
 		
 		System.out.println("time      Storage     Inflow       Release     " +
 		"      Next Storage         TotalDev(with weight)       TotalDeviation        SquareTDFlood " +
 		"        SquareTDRecreation                     SQ Towns_PZ      SQ Towns_SS         SQ Agricultyre UZ       SQ Agricultyre LZ     SQ Ecology         SQ Hydro Power ") ;
 		
 		
 		double[] TargetsTowns_PZ=new double [fD];
 		double[]  TargetsTownsWeights_PZ=new double [fD];
 		
 		
 		double[] TargetsTowns_SS=new double [fD];
 		double[]  TargetsTownsWeights_SS=new double [fD];
 		
 		double[] TargetsAgriculture_UZ=new double [fD];
 		double[]  TargetsAgricultureWeights_UZ=new double [fD];
 		
 		
 		double[] TargetsAgriculture_LZ=new double [fD];
 		double[]  TargetsAgricultureWeights_LZ=new double [fD];
 		
 
 		double[] storageTargetsEcology=new double [fD];
 		double[]  storageTargetsEcologyWeights=new double [fD];
 		
 		
 
 		
 		AbstactData test = new AbstactData();
 		int a;
 		a=test.testDatabaseConnection();
 		
 	IData test1 = new AbstactData();
 		
 		
 	
 		
 		TargetsTowns_PZ=test1.getTargetsTowns_Probistip_Zletovo_2stage();
 		TargetsTownsWeights_PZ=test1.getTargetsTownsWeights_Probistip_Zletovo_2stage();
 		
 		
 
 		TargetsTowns_SS=test1.getTargetsTowns_Shtip_svNikole_2stage();
 		TargetsTownsWeights_SS=test1.getTargetsTownsWeights_Shtip_svNikole_2stage();
 		
 		
 		TargetsAgriculture_UZ=test1.getTargetsAgriculture_UZ_2stage();
 		TargetsAgricultureWeights_UZ=test1.getTargetsAgricultureWeights_UZ_2stage();
 		
 		
 		TargetsAgriculture_LZ=test1.getTargetsAgriculture_LZ_2stage();
 		TargetsAgricultureWeights_LZ=test1.getTargetsAgricultureWeights_LZ_2stage();
 		
 		
 		
 		storageTargetsEcology=test1.getstorageTargetsEcology_2stage();
 		storageTargetsEcologyWeights=test1.getstorageTargetsEcologyWeights_2stage();
 		
 		
 		
 		
 		
 		
 		
 	
 			
 			System.out.println("Towns Probistip zletovo [] ------ TownsWeights []");
 			for(i=61;i<fD-59;i++)System.out.println(TargetsTowns_PZ[i]+ "                "+ TargetsTownsWeights_PZ[i]);
 			System.out.println(" ");
 			
 			
 			System.out.println("Towns Shtip Sv Nikole[] ------ TownsWeights []");
 			for(i=61;i<fD-59;i++)System.out.println(TargetsTowns_SS[i]+ "                "+ TargetsTownsWeights_SS[i]);
 			System.out.println(" ");
 		
 			System.out.println("Agriculture Upper zone [] ------ AgricultureNWeights");
 			for(i=61;i<fD-59;i++) System.out.println(TargetsAgriculture_UZ[i]+ "            "+ TargetsAgricultureWeights_UZ[i]);
 			System.out.println(" ");
 			
 			
 			System.out.println("Agriculture Lower zone [] ------ AgricultureNWeights");
 			for(i=61;i<fD-59;i++) System.out.println(TargetsAgriculture_LZ[i]+ "            "+ TargetsAgricultureWeights_LZ[i]);
 			System.out.println(" ");
 			
 			System.out.println("Ecology [] ------EcologyNWeights []");
 			for(i=61;i<fD-59;i++)System.out.println(storageTargetsEcology[i]+ "  "+ storageTargetsEcologyWeights[i]);
 			System.out.println(" ");
 		
 		
 	
 		
 		Distribute distribution = new Distribute() ;
 		
 		
 		double[] OptimalStorage=new double [fD];
 		double [] solution=new double [5]; 
 		
 		int finS=0;
 		
 		DecimalFormat df1 = new DecimalFormat("#,###,000.0");
 		System.out.println ( " Release       PZ              SS           UZ                LZ             E   ");
 		
 		double [] deviation = new double [5];
 		for(i=61;i<fD-59;i++)
 		//for(i=0;i<fD;i++)
 		{
 			
 			finS=stateFinal[i][st];
 			
 			OptimalStorage[i]=storageD[st];
 			
 			
 			
 			
 			double []  demands= { TargetsTowns_PZ[i],TargetsTowns_SS[i],TargetsAgriculture_UZ[i],TargetsAgriculture_LZ[i] ,storageTargetsEcology[i] };
 			double[] weights = { TargetsTownsWeights_PZ[i],TargetsTownsWeights_SS[i],TargetsAgricultureWeights_UZ[i],TargetsAgricultureWeights_LZ[i],storageTargetsEcologyWeights[i]};
 		
 			if (releaseFinal[i][st]>=(TargetsTowns_PZ[i]+TargetsTowns_SS[i]+TargetsAgriculture_UZ[i]+TargetsAgriculture_LZ[i]+storageTargetsEcology[i]))
 			{
 				solution[0]=TargetsTowns_PZ[i];solution[1]=TargetsTowns_SS[i];
 				solution[2]=TargetsAgriculture_UZ[i];solution[3]=TargetsAgriculture_LZ[i];
 				solution[4]=storageTargetsEcology[i];
 	
 			}
 		
 				else
 			
 			
 				{
 			
 			
 			distribution.setInput(demands, weights,releaseFinal[i][st] ,50);
 			solution=distribution.calculateDPQuadraticDistribution();
 			
 			
 			
 			
 				}
 			
 			 if (solution[0]<=demands[0]) deviation[0]=Math.pow((demands[0]-solution[0]),2); else deviation[0]=0;
 			 if (solution[1]<=demands[1]) deviation[1]=Math.pow((demands[1]-solution[1]),2); else deviation[1]=0;
 			 
 			 
 		     if (solution[2]<=demands[2]) deviation[2]=Math.pow((demands[2]-solution[2]),2);else deviation[2]=0;
 		     if (solution[3]<=demands[3]) deviation[3]=Math.pow((demands[3]-solution[3]),2);else deviation[3]=0;
 		    
 		     
 			 if (solution[4]<=demands[4]) deviation[4]=Math.pow((demands[4]-solution[4]),2); else deviation[4]=0; 
 		   
 			  deviationTowns_PZ=deviation[0]; 
 			  deviationTowns_SS=deviation[1]; 
 			  deviationAgriculture_UZ=deviation[2]; 
 			  deviationAgriculture_LZ=deviation[3]; 
 			  deviationEcology=deviation[4]; 
 			 
 			 if(demands[0]==0) deviationTowns_PZ=0;
 			 if(demands[1]==0) deviationTowns_SS=0;
 			 if(demands[2]==0) deviationAgriculture_UZ=0;
 			 if(demands[3]==0) deviationAgriculture_LZ=0;
 			 if(demands[4]==0) deviationEcology=0;
 				
 			System.out.println("");
 			for (int k=0;k<5;k++) {System.out.println(demands[k]+"   " +solution[k]+"   "+deviation[k]  );}
 			System.out.println(i+ "   "+df1.format(releaseFinal[i][st])  +"  " +df1.format( deviationTowns_PZ)+"       "+ df1.format( deviationTowns_SS)+ "           "+ df1.format( deviationAgriculture_UZ)  
 					+"        "+ df1.format( deviationAgriculture_LZ)+"       "+df1.format( deviationEcology));
 			System.out.println("");
 			
 			TdeviationTowns_PZ+= deviationTowns_PZ;
 			TdeviationTowns_SS+=deviationTowns_SS;
			TdeviationAgriculture_LZ+=deviationAgriculture_LZ;
 			TdeviationAgriculture_UZ+=deviationAgriculture_UZ;
 			TdeviationEcology+=deviationEcology;
 			
 			 st=finS;
 			
 		}
 		
 		System.out.println(" devijaciite se " + df1.format(TdeviationTowns_PZ)+"  "+df1.format(TdeviationTowns_SS)+ "  "
 				+df1.format(TdeviationAgriculture_UZ)  
 				+"   "+ df1.format(TdeviationAgriculture_LZ)+" "+df1.format(TdeviationEcology)+ "  suma "+ 
 				df1.format((TdeviationTowns_PZ+TdeviationTowns_SS+TdeviationAgriculture_UZ +TdeviationAgriculture_LZ+TdeviationEcology))
 				);
 		
 		
 		System.out.println(" with Weights devijaciite se " + df1.format(TargetsTownsWeights_PZ[1]*TdeviationTowns_PZ)+
				
				"  "+df1.format(TargetsAgricultureWeights_UZ[1]*TdeviationAgriculture_UZ)  +
				
 				"  "+df1.format(TargetsTownsWeights_SS[1]*TdeviationTowns_SS)+
				"   "+ df1.format(TargetsAgricultureWeights_LZ[1]*TdeviationAgriculture_LZ)+
 						" "+df1.format(storageTargetsEcologyWeights[1]*TdeviationEcology)+ "  suma "+
 
 						df1.format((TargetsTownsWeights_PZ[1]*TdeviationTowns_PZ+
				
						TargetsAgricultureWeights_UZ[1]*TdeviationAgriculture_UZ+  			
					TargetsTownsWeights_SS[1]*TdeviationTowns_SS+ 
				
				TargetsAgricultureWeights_LZ[1]*TdeviationAgriculture_LZ+
 				storageTargetsEcologyWeights[1]*TdeviationEcology)
 				));
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	String [] month = { "jan", "feb", "mar","apr","may", "jun","jul","aug","sep","oct","nov","dec"};
 	
 	String [] segment = { "1", "2", "3","4","5", "6","7","8","9","10","11","12"};
 	public void Simulation_With_Other_set(int st,int num) throws Exception
 	{
 		
 		double optimalStorage [] = new double [num];
 			int timestep=0;
 		int i,j,k;
 
 	//	int interval_which;
 		
 	
 		
 		double reservoir_volume_next=0;
 		int finS;
 		
 		
 		System.out.println(" ");
 		System.out.println(" ");
 		System.out.println(" This is the TOTAL solution");
 		
 		
 		double TotalDeviationWeight=0;
 		double TotalDeviation=0;
 		double TotalDeviationSum=0;
 		
 		
 
 		double deviationRecreation=0;
 		double deviationFlood=0;
 		double deviationTowns_PZ=0;
 		double deviationTowns_SS=0;
 		double deviationAgriculture_UZ=0;
 		double deviationAgriculture_LZ=0;
 		double deviationEcology=0;
 		double deviationHP=0;
 		
 		
 		double deviationRecreationWithWeight=0;
 		double deviationFloodWithWeight=0;
 		double deviationTownsWithWeight_PZ=0;
 		double deviationTownsWithWeight_SS=0;
 		double deviationAgricultureWithWeight_UZ=0;
 		double deviationAgricultureWithWeight_LZ=0;
 		double deviationEcologyWithWeight=0;
 
 		double deviationHPWithWeight=0;
 		
 		
 		
 		
 		 DecimalFormat df = new DecimalFormat("000.000");
 			
 			System.out.println("time      Storage     Inflow       Release     " +
 			"      Next Storage         TotalDev(with weight)       TotalDeviation        SquareTDFlood " +
 			"        SquareTDRecreation                     SQ Towns_PZ      SQ Towns_SS         SQ Agricultyre UZ       SQ Agricultyre LZ     SQ Ecology         SQ Hydro Power ") ;
 			
 			
 			
 		double release;
 			WekaClassifier t = new WekaClassifier();
 			Object [] y={"jan",13.4,1.3};
 			release=t.classify(y);
 			
 			System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEE  "+release);
 			
 			int tt;
 			int brojac=0;
 		
 		for(i=0;i<num;i++)
 		{
 			timestep=i%12;
 			
 			//interval_which=belongToInterval(flow[i]);
 						
 			//System.out.println(timestep + " ooooooo  " + storageD[st]+"   "+  flow[i]+"    "+resultRelease[timestep][st][interval_which]);
 			y[0]=(Object) month[timestep];//month[1];
 			y[1]=storageD[st];
 			//y[2]=(Object) segment[interval_which];
 			y[2]=(Object) flow[i];
 			release=t.classify(y);
 			
 			if((release<0)&& release>1000) release=0;
 			if (Double.isNaN(release)) release=0;
 			optimalStorage[i]=storageD[st];
 			reservoir_volume_next=storageD[st]+flow[i]-release;//-alpha*evapotranspiration[timestep]*(reservoir_area[st]);
 			
 		//	System.out.println("the release "+ release +"  reservoir voluke "+reservoir_volume_next );
 			finS=reservoir_volume_closest(reservoir_volume_next);
 		//	
 		//	System.out.println("!!!! "+i+"  "+st+ "   "+"   "+finS);
 			
 			  TotalDeviationWeight+=tVFDtable[i][st][finS]; 
               TotalDeviation=tVVFDtableRecreation[i][st][finS]+tVVFDtableFlood[i][st][finS]+tVVFDtableTowns_PZ[i][st][finS]+tVVFDtableTowns_SS[i][st][finS]+
               tVVFDtableAgriculture_UZ[i][st][finS]+tVVFDtableAgriculture_LZ[i][st][finS]+tVVFDtableEcology[i][st][finS]+tVVFDtableHydro_Power[i][st][finS];
               TotalDeviationSum+=TotalDeviation;
              
               deviationRecreation+=tVVFDtableRecreation[i][st][finS];
               deviationRecreationWithWeight+=tVVFDtableRecreation[i][st][finS]*recreationWeights[i];
               
               deviationFlood+=tVVFDtableFlood[i][st][finS];
               deviationFloodWithWeight+=tVVFDtableFlood[i][st][finS]*floodWeights[i];
               
               deviationTowns_PZ+=tVVFDtableTowns_PZ[i][st][finS];
               deviationTownsWithWeight_PZ+=tVVFDtableTowns_PZ[i][st][finS]*townWeights_PZ[i];
               
               deviationTowns_SS+=tVVFDtableTowns_SS[i][st][finS];
               deviationTownsWithWeight_SS+=tVVFDtableTowns_SS[i][st][finS]*townWeights_SS[i];
               
               
               deviationAgriculture_UZ+=tVVFDtableAgriculture_UZ[i][st][finS];
               deviationAgricultureWithWeight_UZ+=tVVFDtableAgriculture_UZ[i][st][finS]*agricultureWeights_UZ[i];
               
               deviationAgriculture_LZ+=tVVFDtableAgriculture_LZ[i][st][finS];
               deviationAgricultureWithWeight_LZ+=tVVFDtableAgriculture_LZ[i][st][finS]*agricultureWeights_LZ[i];
               
               
               deviationEcology+=tVVFDtableEcology[i][st][finS];
               deviationEcologyWithWeight+=tVVFDtableEcology[i][st][finS]*ecologyWeights[i];
               
               deviationHP+=tVVFDtableHydro_Power[i][st][finS];
               deviationHPWithWeight+=tVVFDtableHydro_Power[i][st][finS]*hydropowerWeights[i];
               
            
               
               
               System.out.println( i  +  "           "+ df.format(storageD[st])+ "       "+ df.format(flow[i])+"        "+
             		  df.format(release)+"                   " +  df.format(storageD[finS])+ "                        "+df.format(tVFDtable[i][st][finS]) +"                          "
             		  + df.format(TotalDeviation)+"                    "+  df.format(tVVFDtableFlood[i][st][finS]) + "                         "+ df.format(tVVFDtableRecreation[i][st][finS])
   					+"                              "+ df.format(tVVFDtableTowns_PZ[i][st][finS])+"                              "+ df.format(tVVFDtableTowns_SS[i][st][finS])+
   					"                         " +df.format(tVVFDtableAgriculture_UZ[i][st][finS])+"                         " +df.format(tVVFDtableAgriculture_LZ[i][st][finS])
   					+"                        "+df.format(tVVFDtableEcology[i][st][finS])+ "                "+df.format(tVVFDtableHydro_Power[i][st][finS]));
   		
               
               
               
               
               st=finS;
             
             
             
 		}
 		
 		
 		
 		
 		System.out.println("TDS        RL           FL           PZ            UZ            SS            LZ            E            HP       " );
 		
 		System.out.println(df.format(TotalDeviationSum)+"   "+df.format(deviationRecreation)+"     "+df.format(deviationFlood)+"     "+
 				df.format(deviationTowns_PZ)+"    "+df.format(deviationAgriculture_UZ)+"      "+df.format(deviationTowns_SS)+"      "+
 				df.format(deviationAgriculture_LZ)+"      "+df.format(deviationEcology)+"      "+df.format(deviationHP));
 		System.out.println("  ");
 		System.out.println("");
 
 		
 		System.out.println("TDSWeight      RL         FL           PZ            UZ            SS            LZ            E            HP                                     " );
 		 
 				System.out.println(df.format(TotalDeviationWeight)+"   "+df.format(deviationRecreationWithWeight)+"     "+df.format(deviationFloodWithWeight)+"     "+
 				df.format(deviationTownsWithWeight_PZ)+"    "+df.format(deviationAgricultureWithWeight_UZ)+"      "+df.format(deviationTownsWithWeight_SS)+"      "+
 				df.format(deviationAgricultureWithWeight_LZ)+"      "+df.format(deviationEcologyWithWeight)+"      "+df.format(deviationHPWithWeight));
 		
 		
 		
 		
 		
 		
 		
 		
 		//System.out.println("Period     Starting state     inflow         release        final state        TDSt");
 		
 		
 		
 		for(i=0;i<num;i++)
 		{
 			System.out.println(optimalStorage[i]);
 			
 		}
 		
 		
 		
 		
 		
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/*
 	
 	public void Simulation_starting_state(int st) throws Exception
 	{
 		double[] OptimalStorage=new double [fD];
 		                              //!!! this must/can be improved from witch state the path should start
 		int i,t;
 		
 		int finS;
 		// always the algoritm will start from 0 period 
 		// one cycle usualy one year
 		System.out.println(" This is simulation of the calculated results");
 		//String t1=" This is simulation of the calculated results";
 		//System.out.println("Period     Starting state     inflow         release        final state        TDSt");
 		
 		
 		
 	
 		
 		for(t=0;t<fD;t++)
 		{
 			
 			
 			i=t%fD;
 		
 			finS=stateFinal[i][st];
 			
 System.out.println( i  +  "       "+ storageD[st]+ "               "+ flow[i]+"             "
 		+ releaseFinal[i][st]+"            " +storageD[finS]+ "             "+tVFDtable[i][st][finS] );
 			
 	OptimalStorage[i]=storageD[st];
 
 		st=finS;
                                                                                                             
 		}
 		
 		
 			
 			
 		
 		
 		System.out.println(" ");
 		System.out.println(" ");
 		System.out.println(" This is the final solution");
 		
 		
 		double TotalDeviation=0;
 		double deviationTarget=0;
 		double deviationRecreation=0;
 		double deviationFlood=0;
 		
 		System.out.println("timestep     Storage           Inflow   ReleaseTarget    Release     " +
 		"      Next Storage     SQuareTDTotal  SquareTD Target  SquareTDFlood  SquareTDRecreation   ") ;
 		for(t=0;t<fD;t++)
 		{
 			
 			
 			i=t%fD;
 			//System.out.println("ova e vrednosta na i "+i+"   i na t"+t);
 		//	System.out.println("         "+period);
 			finS=stateFinal[i][st];
 			
 			OptimalStorage[i]=storageD[st];
 			
 			System.out.println( i  +  "     %.2d  "+ storageD[st]+ "             %.2f  "+ flow[i]+"           %.2f  "+releaseTargets[i]+" %.2f    "+
 					+ releaseFinal[i][st]+"      %.2f      " +  +storageD[finS]+ "       %.2f      "+tVFDtable[i][st][finS] +
 					"       %.2f  "+ tVVFDtableTarget[i][st][finS]+ "     %.2f    "+ tVVFDtableFlood[i][st][finS] + "  %.2f    "+ tVVFDtableRecreation[i][st][finS]);  ;
 		
 		
               TotalDeviation+=tVFDtable[i][st][finS];   
               deviationTarget+=tVVFDtableTarget[i][st][finS];
               deviationRecreation+=tVVFDtableRecreation[i][st][finS];
               deviationFlood=+tVVFDtableFlood[i][st][finS];
               
               
               st=finS;
 		}
 		
 		System.out.println("Total Temporal Difference "+TotalDeviation);
 		System.out.println("Total Temporal Difference Flood "+deviationFlood);
 		System.out.println("Total Temporal Difference Recreation "+deviationRecreation);
 		System.out.println("Total Temporal Difference Target "+deviationTarget);
 		
 		AbstactData test = new AbstactData();
 		int a;
 		a=test.testDatabaseConnection();
 		
 	IData test1 = new AbstactData();
 		
 		test1.putStorageOptimal(OptimalStorage,fD);
 	
 		
 	}
 	
 	
 	
 	
 	*/
 	
 	}
 
 	
