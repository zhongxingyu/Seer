 package geneConversion;
 
 import nodes.node;
 import cosiRand.ranBinom;
 import coalescent.CoalescentMain;
 
 public class gc {
 	ranBinom random;
 	double geneConversionRate,gcRate,lastGCRate;
 	int length,gcLength;
 	final boolean  DEBUG = true;
 	public gc(){
 		geneConversionRate = 0;
 		gcRate = 0;
 		lastGCRate = 0;
 		gcLength = 500; //in bp
 		//random = new ranBinom();
 	}
 	public void setGCRate(double gcr){
 		geneConversionRate = gcr;
 		gcRate = geneConversionRate * (length + gcLength);
 	}
 	public void setLength(int l){
 		length = l;
 		gcRate = geneConversionRate * ( length + gcLength);
 		if(DEBUG){
 			System.out.println("rate: "+ geneConversionRate + " length: "+length + " ");
 			
 		}
 	}
 	public double getR(){
 		return gcRate;
 	}
 	public double getRate(){
 		int numPops = CoalescentMain.dem.getNumPops();
 		double rate = 0;
 		int numNodes;
 		if(gcRate==0)return 0;
 		
 		for(int i = 0;i<numPops;i++){
 			numNodes = CoalescentMain.dem.getNumNodesInPopByIndex(i);
 			rate += (numNodes * gcRate);
 		}
 		lastGCRate = rate;
 		return rate;
 	}
 	public int pickPopIndex(){
 		//figure out which pop to recombine
 		int popIndex = 0;
		double randCounter = cosiRand.randomNum.randomDouble() * lastGCRate;//maybe need to call Random class instead less memory?
 		int numPops = CoalescentMain.dem.getNumPops();
 		int numNodes;
 		double rate = 0;
 		int i;
 		//weight pops by numNodes
 		if(numPops>1){
 			for( i = 0; i<numPops&&rate<randCounter;i++){
 				numNodes = CoalescentMain.dem.getNumNodesInPopByIndex(i);
 				rate += (numNodes * gcRate);
 				//don't need this multiplication // comment in original code....
 			}
 			popIndex = i - 1;
 		}
 		return popIndex;
 	}
	public node[] execute(double gen , int popIndex,Double location1,Double location2){
 		double loc,loc2;
 		double temp,temp1;
 		//choosing location..
		temp1 = cosiRand.randomNum.randomDouble();
 		temp = (double) (temp1*gcRate);
 		loc = (double) ((int)(temp/gcRate*(length + gcLength) - gcLength)) / length ;
 		loc2 = (double) ((int)(temp/gcRate * (length + gcLength)))/length;
 		
 		if(loc2>length) loc2 = length;
		location1 = loc;
		location2 = loc2;//the passing of these locations as memory may need to be addressed
		
 		return CoalescentMain.dem.gcByIndex(popIndex, gen, loc, loc2);
 	}
 }
