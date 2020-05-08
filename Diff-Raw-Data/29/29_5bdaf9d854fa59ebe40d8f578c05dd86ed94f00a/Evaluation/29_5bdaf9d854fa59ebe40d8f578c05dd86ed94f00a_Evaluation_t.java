 import static training.Split2.split;
 import static training.SGD_Train.sgd_train;
 
 
 import static util.IO.readData;
 import static util.IO.writeData;
 
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
 
 	private static final int NR_OF_QUERIES = 10;			//nr of queries that are posed to each user
 	private static final int NR_OF_TESTUSER = 100;			//run algorithm for that number of new users
 	private static final int NR_OF_TRAININGSUSER = 343;		//how much user should be used for inital training of latent itemfactors
 	private static final int NR_OF_LATENT_FACTORS = 50;		//how much item latent factors
 															//Todo must be given to sgdtrain
 	
 	public static final int MIN_INIT_TRAIN_POOL = 3;		//how much ratings should be used for the initial training of a new user
 	public static final int MIN_TEST_POOL = 20;				//how much ratings should be used for evaluation of MAE 
 	public static final int TRAINING_LOOP = 100;			//how much training loops for initial training of the user
 	private static final double RETRAIN_LEARN_RATE = 0.001f;
 	
 	//store initial datasets
 	public static Instances userlatent;						//stores userlatent factors
 	public static Instances itemlatent;						//stores itemlatent factors
 	public static Instances traindata;						//stores data that was used for training of latent itemfactors
 	public static Instances testdata;						//stores data that will be used for the evaluation
 	private static final boolean SAVE_DATASETS = true;		//should initial datasets be stored to a file?
 
 	
 	public static ArrayList<Instances> querypools;			//all possible queries per user
 	public static ArrayList<Instances> testpools;			//all ratings provided for MAE calculation per user
 	public static ArrayList<Instances> trainpools;			//all ratings used for training of the user 
 	public static ArrayList<Attribute> poolattributes;		
 	
 	public static Random generator;
 	
 	//store results
 	public static double[] errorKarimi;
 	public static double[] errorRandom;
 	public static double initialError;
 	
 	public static void main(String[] args) throws Exception {
 		createLatentFactors();	//split dataset into train and testdata, train itemlatent factors on traindata
 		addNewUsers();			//add new users and initialize their userlatent factors
 		initialError = caculateInitialError();
 		System.out.println("initial MAE (on test data): " + initialError);
 		
 		System.out.println();
 		System.out.println("Start Active Learning with minimal MAE selection");
 		errorKarimi = runMF(new minimalMaeSelection());
 		System.out.println("final MAE (on test data): " + errorKarimi[NR_OF_QUERIES-1]);
 		
 		System.out.println();
 		System.out.println("initial MAE (on test data): " + initialError);
 		System.out.println("Start Active Learning with random selection");
 		errorRandom = runMF(new randomSelection());
 		System.out.println("final MAE (on test data): " + errorRandom[NR_OF_QUERIES-1]);
 		
//		double[] plotDataKarimi = new double[errorKarimi.length+1];
		double[] plotDataKarimi = new double[errorKarimi.length+1];
		double[] plotDataRandom = new double[errorRandom.length+1];
		plotDataKarimi[0] = initialError;
		plotDataRandom[0] = initialError;
		for (int i = 0; i < errorKarimi.length; i++)
		{
			plotDataKarimi[i+1] = errorKarimi[i];
			plotDataRandom[i+1] = errorRandom[i];
		}
		
		Plot.drawFunction(new double[][]{plotDataKarimi, plotDataRandom}, "Evaluation", "Active Learning with Optimal Selection"
				,new String[] {"minimal MAE selection", "random selection"}, "Number of Questions", "MAE");
 //		Plot.drawFunction(new double[][]{errorRandom}, "Evaluation", "Active Learning with Random Selection"
 //				,new String[] {"MAE"}, "Number of Questions", "MAE");
 	}
 
 
 	private static double caculateInitialError() {
 		double[] errorPerUser = new double[NR_OF_TESTUSER];
 		for (int user = 0; user < NR_OF_TESTUSER; user++)
 		{
 			errorPerUser[user] = MatrixFactorization.calculateCurrentError(itemlatent, 
 					userlatent.get(userlatent.numInstances()-NR_OF_TESTUSER+user), 
 					testpools.get(user));
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
 		ArrayList<Instances> tmpQueryPools = cleanArrayListInstancesCopy(querypools);
 		ArrayList<Instances> tmpTestPools =cleanArrayListInstancesCopy(testpools);
 		ArrayList<Instances> tmpTrainPools = cleanArrayListInstancesCopy(trainpools);
 		
 		return MatrixFactorization.MF(tmpItemlatent, tmpUserlatent, querySelector, NR_OF_QUERIES, NR_OF_TESTUSER, tmpQueryPools, tmpTestPools, tmpTrainPools);
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
 			
 			trainNewUser(currentuser);	//initialisation training
 		}
 	}
 
 	private static void trainNewUser(Instance currentuser) {
 		//randomly initialize userfactors
 
 		
 		for (int e = 1; e <= NR_OF_LATENT_FACTORS; e++) {
 			double r = (double) generator.nextInt(10);
 			r = r / 100;
 			currentuser.setValue(e, r);
 		}
 
 		double alpha = RETRAIN_LEARN_RATE;
 		double rui = 0;
 	
 		for(int i = 0; i < TRAINING_LOOP; i++)
 		{
 			for (Instance query : trainpools.get(trainpools.size()-1))
 			{
 				for (int lat_factor = 1; lat_factor <= NR_OF_LATENT_FACTORS; lat_factor++) 
 				{ 
 				//	every runthrough has new rui rating cause of updated user latent factors
 					rui = 0;
 					for (int lat_factor2 = 1; lat_factor2 <= NR_OF_LATENT_FACTORS; lat_factor2++) { 
 						rui = rui + (currentuser.value(lat_factor2) 
 								* itemlatent.instance((int) query.value(1)-1).value(lat_factor2));
 					} 
 					
 					currentuser.setValue(lat_factor, 
 							currentuser.value(lat_factor) - 
 							(2 * alpha * (rui - query.value(2))*itemlatent.instance((int) query.value(1)-1).value(lat_factor)));
 				}
 			}
 		}
 		userlatent.add(currentuser);
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
 		Instances[] latent = sgd_train(splitData.get(1));	//train factors on trainset
 		traindata = splitData.get(1);						//save sets and factors
 		testdata = splitData.get(0);
 		userlatent = latent[0];
 		itemlatent = latent[1];
 		itemlatent.sort(0);			//sort is extremely neccessary
 		
 		if (SAVE_DATASETS) writeIntoFiles();				//store in files if you want to
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
