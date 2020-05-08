 package training;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 
 import weka.core.Attribute;
 import weka.core.DenseInstance;
 import weka.core.Instance;
 import weka.core.Instances;
 
 import static training.Split2.split;
 import static util.IO.readData;
 import static util.IO.writeData;
 import static util.Plot.drawFunction;
 import static util.Functions.*;
 
 public class SGD_Train {
 	public static final String LATENT_NAME_BASE = "f";
 	public static final int Z = 100;							// number of training iterations
	public static final int F = 50;								// number of latent factors
 	public static final double ALPHA = 0.01;					// learning rate
 	public static final double LAMBDA = 0.15;					// regularization parameter
 	public static final double DELTA = 0.0001;					// min error, if reached the training can be stopped early
 	public static final double MAX_INIT_LAT = 0.01;				// absolute value of initial latent factors
 	
 	// debug
 	public static double[] mae_on_train = new double[Z];		// containing the MAE on training Data after each Iteration	
 	
 	public static void main(String[] args) {
 		Instances Data = null;
 		try {
 			Data = readData("data_movielens/raw/data.arff");
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		// Randomsplit
 		List<Instances> splitData = split(Data, 943 - 343);
 //		splitData.get(1).randomize(new Random());
 		Instances[] latent = sgd_train(splitData.get(1));
 		
 		try {
 			writeData("data_movielens/train/traindata.arff", splitData.get(1));
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			writeData("data_movielens/train/testdata.arff", splitData.get(0));
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			writeData("data_movielens/train/userlatent.arff", latent[0]);
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			writeData("data_movielens/train/itemlatent.arff", latent[1]);
 		} catch (NullPointerException | IllegalArgumentException | IOException e) {
 			e.printStackTrace();
 		}
 		
 		drawFunction(new double[][]{mae_on_train}, "Evalutation", "MAE vs Iterations"
 				, new String[] {"MAE"}, "Iterations", "MAE");
 	}
 	
 	public static Instances[] sgd_train(Instances trainingData)
 	{
 		return sgd_train(trainingData, F);
 	}
 	
 	public static Instances[] sgd_train(Instances trainingData, int numLatFac)
 	{
 		return sgd_train(trainingData, numLatFac, ALPHA, LAMBDA, DELTA, Z, MAX_INIT_LAT);
 	}
 	
 	public static Instances[] sgd_train(Instances trainingData, int numLatFac
 			, double alpha, double lambda, double delta, int max_its, double maxInitLat)
 	{
 		if (trainingData == null)
 			throw new NullPointerException("Training Data is null");
 		
 		double[][] ratings;
 		
 		// needed for output, list of all User- and ItemIDs
 		double[] userIDs = new double[trainingData.numDistinctValues(0)];
 		double[] itemIDs = new double[trainingData.numDistinctValues(1)];
 		
 		// Mappings for the possibly random and non-continuous User- and ItemIDs
 		Map<Integer,Integer> userIDMap = new TreeMap<Integer,Integer>();
 		Map<Integer,Integer> itemIDMap = new TreeMap<Integer,Integer>();
 		
 		double[][] userLatent = new double[trainingData.numDistinctValues(0)][numLatFac];
 		double[][] itemLatent = new double[trainingData.numDistinctValues(1)][numLatFac];
 		
 		initLatentFactors(userLatent, maxInitLat);
 		initLatentFactors(itemLatent, maxInitLat);
 		
 		
 		ratings = convertToMatrix(trainingData, userIDs, itemIDs ,userIDMap, itemIDMap);
 		
 		double err = 0;
 		double serr = 0;				// storing intermediate result: single error for user u and item i
 		double[] eui_pu = null;			// storing intermediate result: eui * pu
 		double[] eui_qi = null;			// storing intermediate result: eui * qi
 		double[] lambda_qi = null;		// storing intermediate result: lambda * qi
 		double[] lambda_pu = null;		// storing intermediate result: lambda * pu
 		double[] gradientPu = null;
 		double[] gradientQi = null;
 		int curUserIndex = -1;
 		int curItemIndex = -1;
 		
 		System.out.println("Number of (UserIDs, ItemIDs) : " + "("+ userIDs.length + "," + itemIDs.length + ")");
 		System.out.println("initial MAE(on training data): "
 				+ calcMAE(trainingData, ratings, userIDMap, itemIDMap, userLatent, itemLatent));
 		System.out.print("Start Training:");
 		
 		for (int itCount = 0; itCount < max_its; itCount++)
 		{
 			// train
 			if (itCount % 100 == 0)
 			{
 				System.out.println();
 			} else if (itCount % 50 == 0)
 			{
 				System.out.print("\t");
 			} else if (itCount % 10 == 0)
 			{
 				System.out.print(" ");
 			}
 			System.out.print(".");
 			
 			// seems to prevent overfitting to a certain degree
 //			trainingData.randomize(new Random());
 			
 			for (Instance i : trainingData)
 			{
 				curUserIndex = userIDMap.get( (int)(i.value(0)) );
 				curItemIndex = itemIDMap.get( (int)(i.value(1)) );
 				
 				serr = ratings[curUserIndex][curItemIndex] - dotProduct(itemLatent[curItemIndex], userLatent[curUserIndex]);
 				
 				// apply gradient descent to latent User factors
 				eui_qi = scalarMult(serr, itemLatent[curItemIndex]);
 				lambda_pu = scalarMult(lambda, userLatent[curUserIndex]);
 				gradientPu = minus(eui_qi, lambda_pu);
 				userLatent[curUserIndex] = plus( userLatent[curUserIndex], scalarMult(alpha, gradientPu) );
 				
 				// apply gradient descent to latent Item factors
 				eui_pu = scalarMult(serr, userLatent[curUserIndex]);
 				lambda_qi = scalarMult(lambda, itemLatent[curItemIndex]);
 				gradientQi = minus(eui_pu, lambda_qi);
 				itemLatent[curItemIndex] = plus( itemLatent[curItemIndex], scalarMult(alpha, gradientQi) );
 			}
 			
 			// calculate error
 			err = 0;
 			for (Instance i : trainingData)
 			{
 				curUserIndex = userIDMap.get( (int)(i.value(0)) );
 				curItemIndex = itemIDMap.get( (int)(i.value(1)) );
 				
 				err += Math.pow(ratings[curUserIndex][curItemIndex] - dotProduct(userLatent[curUserIndex], itemLatent[curItemIndex]), 2)
 						+ lambda * ( normToPow2(userLatent[curUserIndex]) + normToPow2(itemLatent[curItemIndex]) );
 			}
 			if (err < delta)
 				break;
 			
 			mae_on_train[itCount] = calcMAE(trainingData, ratings, userIDMap, itemIDMap, userLatent, itemLatent);
 		}
 		System.out.println("\nTraining done!");
 		System.out.println("final MAE(on training data): " 
 				+ calcMAE(trainingData, ratings, userIDMap, itemIDMap, userLatent, itemLatent));
 		System.out.println();
 		
 		Instances userLatentInstances = createLatentInstances("userlatent", "userid", userLatent.length, numLatFac);
 		Instances itemLatentInstances = createLatentInstances("itemlatent", "itemid", itemLatent.length, numLatFac);
 		
 		convertToInstances(userLatent, userIDs, userIDMap, userLatentInstances);
 		convertToInstances(itemLatent, itemIDs, itemIDMap, itemLatentInstances);
 		
 		return new Instances[]{userLatentInstances, itemLatentInstances};
 	}
 	
 	private static Instances createLatentInstances(String name, String nameFirstAttr, int capacity, int numLatFac)
 	{
 		ArrayList<Attribute> attrInfo = new ArrayList<Attribute>(numLatFac + 1);
 		attrInfo.add(new Attribute(nameFirstAttr));
 		for (int facNum = 0; facNum < numLatFac; facNum++)
 		{
 			attrInfo.add(new Attribute(LATENT_NAME_BASE + facNum));
 		}
 		return new Instances(name, attrInfo, capacity);
 	}
 	
 	private static void convertToInstances(double[][] in, double[] IDlist
 			,Map<Integer,Integer> IDMap, Instances out)
 	{
 		double[] attrs = null;
 		int curID = -1;
 		int curIndex = -1;
 		for (int idCount = 0; idCount < IDlist.length; idCount++)
 		{
 			curID = (int)(IDlist[idCount]);
 			attrs = new double[out.numAttributes()];
 			curIndex = IDMap.get(curID);
 			attrs[0] = curID;
 			for (int attrNum = 0; attrNum < in[curIndex].length; attrNum++)
 			{
 				// first attribute in attrs is ID
 				attrs[attrNum + 1] = in[curIndex][attrNum];
 			}
 			out.add(new DenseInstance(1.0, attrs));
 		}
 	}
 	
 	private static double[][] convertToMatrix(Instances data
 			, double[] outUserIDs, double[] outItemIDs
 			, Map<Integer,Integer> outUserIDMap, Map<Integer,Integer> outItemIDMap)
 	{
 		assert (data != null 
 				&& outUserIDs != null && outItemIDs != null
 				&& outUserIDMap != null && outItemIDMap != null);
 		
 		double[][] res = new double[data.numDistinctValues(0)][data.numDistinctValues(1)];
 		
 		int curUserID = -1;
 		int curItemID = -1;
 		Integer curUserIndex = -1;
 		Integer curItemIndex = -1;
 
 		for (Instance i : data)
 		{
 			curUserID = (int)i.value(0);
 			curItemID = (int)i.value(1);
 			curUserIndex = outUserIDMap.get(curUserID);
 			curItemIndex = outItemIDMap.get(curItemID);
 			if (curUserIndex == null)
 			{
 				curUserIndex = outUserIDMap.size();
 				outUserIDs[curUserIndex] = curUserID;
 				outUserIDMap.put(curUserID, curUserIndex);
 			}
 			if (curItemIndex == null)
 			{
 				curItemIndex = outItemIDMap.size();
 				outItemIDs[curItemIndex] = curItemID;
 				outItemIDMap.put(curItemID, curItemIndex);
 			}
 			res[curUserIndex][curItemIndex] = i.value(2);
 			
 		}
 		return res;
 	}
 	
 	private static void initLatentFactors(double[][] outLatentFactors, double maxInitLat)
 	{
 		assert (outLatentFactors != null);
 		Random rnd = new Random();
 		double rndQuotient = calcRndQuotient(maxInitLat);
 		
 		for (int i = 0; i < outLatentFactors.length; i++)
 		{
 			for (int fac = 0; fac < outLatentFactors[0].length; fac++)
 			{
 				// initialize latent factors randomly with values in [-maxInitLat, maxInitLat)
 				outLatentFactors[i][fac] = rnd.nextDouble()/rndQuotient - maxInitLat;
 			}
 		}
 	}
 	
 	private static double calcRndQuotient(double maxValue)
 	{
 		return 1/(maxValue * 2);
 	}
 	
 	private static double normToPow2(double[] vec)
 	{
 		double res = 0;
 		for (int elem = 0; elem < vec.length; elem++)
 		{
 			res += vec[elem] * vec[elem];
 		}
 		return res;
 	}
 	
 	private static double calcMAE(Instances data, double[][] ratings
 			, Map<Integer,Integer> userIDMap, Map<Integer,Integer> itemIDMap
 			, double[][] userLatent, double[][] itemLatent)
 	{
 		assert(data != null
 				&& ratings != null
 				&& userIDMap != null && itemIDMap != null
 				&& userLatent != null && itemLatent != null);
 		
 		double mae = 0;
 		int curUserID = -1;
 		int curItemID = -1;
 		Integer curUserIndex = -1;
 		Integer curItemIndex = -1;
 		for (Instance i : data)
 		{
 			curUserID = (int)(i.value(0));
 			curItemID = (int)(i.value(1));
 			curUserIndex = userIDMap.get(curUserID);
 			curItemIndex = itemIDMap.get(curItemID);
 			mae += Math.abs(ratings[curUserIndex][curItemIndex] - dotProduct(itemLatent[curItemIndex], userLatent[curUserIndex]));
 		}
 		mae = mae/(data.numInstances());
 		return mae;
 	}
 }
