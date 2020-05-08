 import static training.Split2.split;
 import static training.SGD_Train.sgd_train;
 
 
 import static util.IO.readData;
 import static util.IO.writeData;
 import static util.IO.writeErrorSeries;
 
 import interfaces.querySelectionAlgorithm;
 import selectionStrategies.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import util.Plot;
 import weka.core.Attribute;
 import weka.core.DenseInstance;
 import weka.core.Instance;
 import weka.core.Instances;
 
 
 public class Evaluation {
 
 	private static final int EVALLOOPS = 10;				//Number of Times the Evalutation will be Performed
 	
 	private static final int NR_OF_QUERIES = 50;			//nr of queries that are posed to each user
 	private static final int NR_OF_TESTUSER = 600;			//run algorithm for that number of new users
 	private static final int NR_OF_TRAININGSUSER = 343;		//how much user should be used for inital training of latent itemfactors
 	private static final int NR_OF_LATENT_FACTORS = 10;		//how much item latent factors
 															//Todo must be given to sgdtrain
 	
 	public static final int MIN_INIT_TRAIN_POOL = 0;		//how much ratings should be used for the initial training of a new user
 	public static final int MIN_TEST_POOL = 20;				//how much ratings should be used for evaluation of MAE 
 	public static final int TRAINING_LOOPS = 100;			//how much training loops for initial training of the user
 	private static final double RETRAIN_LEARN_RATE = 0.001;
 //	private static final double INIT_LEARN_RATE = 0.01;
 //	private static final double DELTA = 0.0001;
 	
 	//store initial datasets
 	public static Instances userlatent;						//stores userlatent factors
 	public static Instances newuserlatent;					//stores latent factors for new users
 	public static Instances itemlatent;						//stores itemlatent factors
 	public static Instances traindata;						//stores data that was used for training of latent itemfactors
 	public static Instances testdata;						//stores data that will be used for the evaluation
 	private static final boolean SAVE_DATASETS = true;		//should initial datasets be stored to a file?
 
 	public static double[] RMs;								//precalculated all ratingaverages for all items from all Training users
 	public static int[][] rCounts;
 	public static double[][] rProbs;							//rating Probabilities based on Training users
 	public static ArrayList<Instances> querypools;			//all possible queries per user
 	public static ArrayList<Instances> testpools;			//all ratings provided for MAE calculation per user
 	public static ArrayList<Instances> trainpools;			//all ratings used for training of the user 
 	public static ArrayList<Attribute> poolattributes;		
 	
 	public static Random generator;
 	
 	//store results
 	public static double[] errorKarimi;
 	public static double[] errorRandom;
 	public static double[] error1ToAverage;
 	public static double[] errorRMWeighted;
 	public static double[] errorRandomStuffTM;
 	public static double initialError;
 	
 	public static void main(String[] args) throws Exception {
 		for (int evalloop = 0; evalloop < EVALLOOPS; evalloop++)
 		{
 			createLatentFactors();	//split dataset into train and testdata, train itemlatent factors on traindata
 			addNewUsers();			//add new users and initialize their userlatent factors
 			RMs = calcRMs();		// calculate Average Ratings
 			rProbs = calcRProbs();  // calculate Rating Probabilty Distribution
 			initialError = calculateInitialError();
 			System.out.println("initial MAE (on test data): " + initialError);
 
 			System.out.println();
 			System.out.println("Start Active Learning with random selection");
 			errorRandom = runMF(new randomSelection());
 			System.out.println("final MAE (on test data): " + errorRandom[NR_OF_QUERIES-1]);
 
 			System.out.println();
 			System.out.println("Start Active Learning with minimal MAE selection");
 			errorKarimi = runMF(new minimalMaeSelection());
 			System.out.println("final MAE (on test data): " + errorKarimi[NR_OF_QUERIES-1]);
 
 			System.out.println();
 			System.out.println("Start Active Learning with minimal MAE with average rui* rating selection");
 			error1ToAverage = runMF(new minimalMAE_UserAverage_Selection());
 			System.out.println("final MAE (on test data): " + error1ToAverage[NR_OF_QUERIES-1]);
 
 			System.out.println();
 			System.out.println("Start Active Learning with minimal MAE with probability weighted rui* selection");
 			errorRMWeighted = runMF(new minimalMAE_ProbabilityBased_Selection());
 			System.out.println("final MAE (on test data): " + errorRMWeighted[NR_OF_QUERIES-1]);
 		
 			System.out.println();
 			System.out.println("Start Active Learning with minimal MAE with probability weighted rui* and rum* selection");
 			errorRandomStuffTM = runMF(new testSelection());
 			System.out.println("final MAE (on test data): " + errorRandomStuffTM[NR_OF_QUERIES-1]);
 		
 			drawPlot();
 		
 			if (evalloop == 0)
 			{
 				writeErrorSeries("data_movielens/output/errorSeries.txt", "random selection", errorRandom, false);
 			} else 
 			{
 				writeErrorSeries("data_movielens/output/errorSeries.txt", "random selection", errorRandom, true);
 			}
 			writeErrorSeries("data_movielens/output/errorSeries.txt", "minimal MAE selection", errorKarimi, true);
 			writeErrorSeries("data_movielens/output/errorSeries.txt", "minimal MAE with average rui* rating selection", error1ToAverage, true);
 			writeErrorSeries("data_movielens/output/errorSeries.txt", "minimal MAE with probability weighted rui* selection", errorRMWeighted, true);
			writeErrorSeries("data_movielens/output/errorSeries.txt", "randomstuffTM selection", errorRandomStuffTM, true);
 		}
 	}
 
 
 	private static void drawPlot() {
 		//		double[] plotDataKarimi = new double[errorKarimi.length+1];
 				double[] plotDataKarimi = new double[errorKarimi.length+1];
 				double[] plotDataRandom = new double[errorRandom.length+1];
 				double[] plotDataAverage = new double[error1ToAverage.length+1];
 				double[] plotDataRMWeighted = new double[errorRMWeighted.length+1];
 				double[] plotDataRandomStuffTM = new double[errorRandomStuffTM.length+1];
 				
 				plotDataKarimi[0] = initialError;
 				plotDataRandom[0] = initialError;
 				plotDataAverage[0] = initialError;
 				plotDataRMWeighted[0] = initialError;
 				plotDataRandomStuffTM[0] = initialError;
 				
 				for (int i = 0; i < errorKarimi.length; i++)
 				{
 					plotDataKarimi[i+1] = errorKarimi[i];
 					plotDataRandom[i+1] = errorRandom[i];
 					plotDataAverage[i+1] = error1ToAverage[i];
 					plotDataRMWeighted[i+1] = errorRMWeighted[i];
 					plotDataRandomStuffTM[i+1] = errorRandomStuffTM[i];
 				}
 				
 				Plot.drawFunction(new double[][]{plotDataRandom, plotDataKarimi, plotDataAverage, plotDataRMWeighted, plotDataRandomStuffTM}, "Evaluation", "Active Learning with Optimal Selection"
 						,new String[] {"random selection", "minimal MAE selection", "1 to Average selection", "weighted RM selection", "randomstuffTM selection"}, "Number of Questions", "MAE");
 		//		Plot.drawFunction(new double[][]{errorRandom}, "Evaluation", "Active Learning with Random Selection"
 		//				,new String[] {"MAE"}, "Number of Questions", "MAE");
 	}
 
 	private static double calculateInitialError() {
 		double[] errorPerUser = new double[NR_OF_TESTUSER];
 		for (int newUserPos = 0; newUserPos < NR_OF_TESTUSER; newUserPos++)
 		{
 			errorPerUser[newUserPos] = MatrixFactorization.calculateCurrentError(itemlatent, 
 					newuserlatent.get(newUserPos), 
 					testpools.get(newUserPos));
 		}
 		double average = 0;
 		for(int i=0; i < errorPerUser.length; i++){
 			average=average + errorPerUser[i];
 		}
 		
 		return average/errorPerUser.length;
 	}
 
 	private static double[] runMF(querySelectionAlgorithm querySelector)
 	{
 		Instances tmpItemlatent = new Instances(itemlatent);
 		Instances tmpUserlatent = new Instances(userlatent);
 		Instances tmpNewUserlatent = new Instances(newuserlatent);
 		ArrayList<Instances> tmpQueryPools = cleanArrayListInstancesCopy(querypools);
 		ArrayList<Instances> tmpTestPools  = cleanArrayListInstancesCopy(testpools);
 		ArrayList<Instances> tmpTrainPools = cleanArrayListInstancesCopy(trainpools);
 		double[] tmpRMs = deepCopy(RMs);
 		double[][] tmprProbs = deepCopy(rProbs);
 		int[][] tmprCounts = deepCopy(rCounts);
 		
 		return MatrixFactorization.MF(
 				traindata, tmpItemlatent, tmpUserlatent, tmpNewUserlatent
 				, querySelector, NR_OF_QUERIES, NR_OF_TESTUSER, RETRAIN_LEARN_RATE, TRAINING_LOOPS
 				, tmpRMs, tmprProbs, tmprCounts
 				, tmpQueryPools, tmpTestPools, tmpTrainPools);
 	}
 	
 	private static ArrayList<Instances> cleanArrayListInstancesCopy(ArrayList<Instances> list)
 	{
 		ArrayList<Instances> newlist = new ArrayList<Instances>();
 		for(Instances i : list)
 			newlist.add(new Instances(i));
 		return newlist;
 	}
 
 	private static void addNewUsers() {
 		generator = new Random();
 		
 		// prepare pool of queries
 		Attribute Attribute1 = new Attribute("userid");
 		Attribute Attribute2 = new Attribute("itemid");
 		Attribute Attribute3 = new Attribute("rating");
 		Attribute Attribute4 = new Attribute("timestamp");
 		poolattributes = new ArrayList<Attribute>(4);
 		poolattributes.add(Attribute1);
 		poolattributes.add(Attribute2);
 		poolattributes.add(Attribute3);
 		poolattributes.add(Attribute4);
 		
 		ArrayList<Attribute> newUserAttrInfo = new ArrayList<Attribute>(NR_OF_LATENT_FACTORS + 1);
 		newUserAttrInfo.add(new Attribute("userid"));
 		for (int facNum = 0; facNum < NR_OF_LATENT_FACTORS; facNum++)
 		{
 			newUserAttrInfo.add(new Attribute("f" + facNum));
 		}
 		newuserlatent = new Instances("newuserlatent", newUserAttrInfo, NR_OF_TESTUSER);
 		
 		querypools 	= new ArrayList<Instances>();
 		testpools  	= new ArrayList<Instances>(); 
 		trainpools 	= new ArrayList<Instances>(); 
 		
 		
 		for (int i = 0; i < NR_OF_TESTUSER; i++)
 		{
 			Instance currentuser = new DenseInstance(NR_OF_LATENT_FACTORS+1);
 			currentuser.setValue(0, testdata.instance(0).value(0));			//save id of the user
 			
 			//create new pools
 			trainpools.add(new Instances("trainpool", poolattributes, MIN_INIT_TRAIN_POOL));
 			querypools.add(new Instances("querypool", poolattributes, 4));
 			testpools.add(new Instances("testpool", poolattributes, MIN_TEST_POOL));
 			
 			// add all his known ratings of this user to the pool of available
 			// queries or test items
 			int itemcounter = 0;
 			Instances temp = new Instances("temppool", poolattributes, 4);
 			while (!testdata.isEmpty() && testdata.instance(0).value(0) == currentuser.value(0)) {			
 				temp.add(testdata.instance(0));
 				testdata.remove(0);
 			}
 			
 			int r = 0;
 			while (!temp.isEmpty()) {
 				r = generator.nextInt(temp.numInstances());
 				if (itemcounter < MIN_INIT_TRAIN_POOL)
 					trainpools.get(trainpools.size()-1).add(temp.instance(r));
 				else 
 				{
 					if (itemcounter <  MIN_INIT_TRAIN_POOL + MIN_TEST_POOL)
 						testpools.get(testpools.size()-1).add(temp.instance(r));
 					else
 						querypools.get(querypools.size()-1).add(temp.instance(r));
 				}
 				temp.remove(r);
 				itemcounter++;
 			}
 			
 			initNewUser(currentuser);	//initialisation training
 			newuserlatent.add(currentuser);
 		}
 	}
 
 	private static void initNewUser(Instance currentuser) 
 	{
 		Random generator = new Random();
 		double rndQuotient = 1/(0.01 * 2);
 		for (int e = 1; e <= currentuser.numAttributes() - 1; e++) {
 			double r = generator.nextDouble()/rndQuotient - 0.01;
 			currentuser.setValue(e, r);
 		}
 	}
 	
 	private static void trainNewUser(Instance currentuser) {
 		//randomly initialize userfactors
 //		double err_current_iteration = 0;
 //		double err_past_iteration = 0;
 		
 		for (int e = 1; e <= NR_OF_LATENT_FACTORS; e++) {
 			double r = (double) generator.nextInt(10);
 			r = r / 100;
 			currentuser.setValue(e, r);
 		}
 
 //		double alpha = INIT_LEARN_RATE;
 //		double rui = 0;
 	
 //		for(int i = 0; i < TRAINING_LOOPS; i++)
 //		{
 //			err_current_iteration = 0;
 //			for (Instance query : trainpools.get(trainpools.size()-1))
 //			{
 //				for (int lat_factor = 1; lat_factor <= NR_OF_LATENT_FACTORS; lat_factor++) 
 //				{ 
 //				//	every runthrough has new rui rating cause of updated user latent factors
 //					rui = 0;
 //					for (int lat_factor2 = 1; lat_factor2 <= NR_OF_LATENT_FACTORS; lat_factor2++) { 
 //						rui = rui + (currentuser.value(lat_factor2) 
 //								* itemlatent.instance((int) query.value(1)-1).value(lat_factor2));
 //					} 
 //					
 //					currentuser.setValue(lat_factor, 
 //							currentuser.value(lat_factor) - 
 //							( alpha * (rui - query.value(2))*itemlatent.instance((int) query.value(1)-1).value(lat_factor)));
 //				}
 //				err_current_iteration += Math.abs(query.value(2) - rui);
 //			}
 //			
 //			err_current_iteration = err_current_iteration / trainpools.get(trainpools.size()-1).size();
 //			if (i > 0 && Math.abs(err_current_iteration - err_past_iteration) < DELTA)
 //			{
 //				//System.out.println("Abbruch des Trainings nach " + i + " Iterationen");
 //				break;
 //			}
 //			err_past_iteration = err_current_iteration;
 //
 //		}
 //		userlatent.add(currentuser);
 	}
 
 	private static void createLatentFactors() {
 		Instances Data = null;
 		try {
 			Data = readData("data_movielens/raw/data.arff");
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		//Randomsplit at number of maximal users in testdata
 		List<Instances> splitData = split(Data, Data.numDistinctValues(0) - NR_OF_TRAININGSUSER);	//split into train and testset
 		Instances[] latent = sgd_train(splitData.get(1), NR_OF_LATENT_FACTORS);	//train factors on trainset
 		traindata = splitData.get(1);						//save sets and factors
 		testdata = splitData.get(0);
 		userlatent = latent[0];
 		itemlatent = latent[1];
 		
 		traindata.sort(0);
 		testdata.sort(0);
 		itemlatent.sort(0);			//sort is extremely neccessary
 		userlatent.sort(0);
 		
 		if (SAVE_DATASETS) writeIntoFiles();				//store in files if you want to
 	}
 	
 	private static double[] calcRMs()
 	{
 		double itemAvg = traindata.meanOrMode(2);
 		double[] RMs = new double[itemlatent.size()];
 		double[] itemCount = new double[itemlatent.size()];
 		for (Instance rating : traindata)
 		{
 			RMs[(int)(rating.value(1)-1)] += rating.value(2);
 			++itemCount[(int)(rating.value(1)-1)];
 		}
 		for (int itemNr = 0; itemNr < itemlatent.size(); itemNr++)
 		{
 			if (RMs[itemNr] == 0)
 			{
 				RMs[itemNr] = itemAvg;
 			} else
 			{
 				RMs[itemNr] /= itemCount[itemNr];
 			}
 		}
 		return RMs;
 	}
 	
 	private static double[][] calcRProbs()
 	{
 		double[][] rProbs = new double[itemlatent.size()+1][5];
 		rCounts = new int[itemlatent.size()+1][5];
 		int[] rSum = new int[itemlatent.size()+1];
 		// initialize with 1s, Laplace Estimation
 		for (int i = 0; i < rCounts.length; i++)
 			for (int k = 0; k < rCounts[i].length; k++)
 				rCounts[i][k] = 1;
 				
 		for (Instance rating : traindata)
 		{
 			switch ( (int)(rating.value(2)) )
 			{
 			case 1: {
 				rCounts[(int) rating.value(1) - 1][0]++;
 				rCounts[rCounts.length-1][0]++;
 				break;
 			}
 			case 2: {
 				rCounts[(int) rating.value(1) - 1][1]++;
 				rCounts[rCounts.length-1][1]++;
 				break;
 			}
 			case 3: {
 				rCounts[(int) rating.value(1) - 1][2]++;
 				rCounts[rCounts.length-1][2]++;
 				break;
 			}
 			case 4: {
 				rCounts[(int) rating.value(1) - 1][3]++;
 				rCounts[rCounts.length-1][3]++;
 				break;
 			}
 			case 5: {
 				rCounts[(int) rating.value(1) - 1][4]++;
 				rCounts[rCounts.length-1][4]++;
 				break;
 			}
 				default: break;
 			}
 		}
 		
 		for (int i = 0; i < rCounts.length; i++)
 		{
 			for (int k = 0; k < rCounts[i].length; k++)
 			{
 				rSum[i] += rCounts[i][k];
 			}
 		}
 		
 		for (int i = 0; i < rProbs.length; i++)
 		{
 			for (int k = 0; k < rProbs[i].length; k++)
 			{
 				rProbs[i][k] = (double)rCounts[i][k] / (double)rSum[i];
 			}
 		}
 	
 		return rProbs;
 	}
 	
 	// the badness
 	private static int[] deepCopy(int[] o)
 	{
 		int[] res = new int[o.length];
 		for (int x = 0; x < o.length; x++)
 		{
 			res[x] = o[x];
 		}
 		return res;
 	}
 	
 	// ohhhh
 	private static double[] deepCopy(double[] o)
 	{
 		double[] res = new double[o.length];
 		for (int x = 0; x < o.length; x++)
 		{
 			res[x] = o[x];
 		}
 		return res;
 	}
 	
 	// hhhhhhhh
 	private static double[][] deepCopy(double[][] o)
 	{
 		double[][] res = new double[o.length][o[0].length];
 		for (int x = 0; x < o.length; x++)
 		{
 			for (int y = 0; y < o[0].length; y++)
 			{
 				res[x][y] = o[x][y];
 			}
 		}
 		return res;
 	}
 	
 	// the badness
 	private static int[][] deepCopy(int[][] o)
 	{
 		int[][] res = new int[o.length][o[0].length];
 		for (int x = 0; x < o.length; x++)
 		{
 			for (int y = 0; y < o[0].length; y++)
 			{
 				res[x][y] = o[x][y];
 			}
 		}
 		return res;
 	}
 	
 	public static void writeIntoFiles()
 	{
 		try {
 			writeData("data_movielens/train/traindata.arff", traindata);
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			writeData("data_movielens/train/testdata.arff", testdata);
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			writeData("data_movielens/train/userlatent.arff", userlatent);
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			writeData("data_movielens/train/itemlatent.arff", itemlatent);
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 
 }
