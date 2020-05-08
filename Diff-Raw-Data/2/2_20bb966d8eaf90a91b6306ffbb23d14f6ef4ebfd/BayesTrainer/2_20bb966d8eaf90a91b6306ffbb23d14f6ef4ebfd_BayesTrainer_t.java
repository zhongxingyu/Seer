 package danger_zone;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.1
 *@since 2012-10-28
 *
* Class to overview the training of the Naive Bayes Algorithm from the Dataset. //
 */
 public class BayesTrainer{
 	/**
 	*Instance of a classifier to use
 	*/
 	private	NaiveBayes bayes = new NaiveBayes();
 
 	/**
 	*Category specifier
 	*/
 	private NaiveBayes specific = new NaiveBayes(new int[] {SubCategories.UNCLASSIFIED,SubCategories.WEATHER,SubCategories.VIOLENCE,SubCategories.ACCIDENT});
 
 	/**
 	*The Dataset to use to train the NaiveBayes instance
 	*/
 	private DataSet data = new DataSet();
 
 	/**
 	*The dataset to use to train the specific subcategory database monstrosity
 	*/
 	private DataSet specificData = new DataSet();
 
 	/**
 	*Initalizes the dataset, if a connection cannot be made the function returns false.
 	*@param password The password to connect to the database.
 	*@return returns true or false depending on whether the dataset is able to connect to the database.
 	*/
 	public boolean initializeData(String password){
 		try{
 			//Open the connection to the database
 			boolean initialized = data.initialize(password);
 			if(!initialized){
 				return false;
 			}
 		}catch(Exception e){
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			return false;
 		}
 		try{
 			//Open the specific the database
 			boolean initialized = specificData.initializeSpecific(password);
 			if(!initialized){
 				return false;
 			}
 		}catch(Exception e){
 			System.out.println(e.getMessage());
 			System.out.println(e.getStackTrace());
 			return false;	
 		}
 		return true;
 	}
 
 	/**
 	*Trains the naive bayes on the entire data set.
 	*/
 	public void trainBayes(){
 		//Train the outer bayes
 		Training_Tweet tweet = (Training_Tweet)data.getNext();
 		int maxTrainOn = data.size() - data.size()/10;
 		for( int k = 0; k < maxTrainOn && tweet != null; k++){
 			//Train based on the bit
 			bayes.train(tweet.getCategory(),tweet.getTweetText());
 			tweet = (Training_Tweet)data.getNext();
 		}
 		//Train the inner sub cat bayes
 		tweet = (Training_Tweet)specificData.getNext();
 		maxTrainOn = specificData.size() - data.size()/10;
 		for(int k =0; k < maxTrainOn && tweet != null; k++){
 			specific.train(tweet.getCategory(),tweet.getTweetText());
 			tweet = (Training_Tweet)specificData.getNext();
 		}
 	}
 
 	/**
 	*Trains the bayes on the tweet text.
 	*@param tweet The tweet string to be trained upon.
 	*/
 	public int classify(String tweet){
 		return bayes.classify(tweet);
 	}
 
 	public int classifyDanger(String tweet){
 		return specific.classify(tweet);
 	}
 
 	/**
 	*Trains the bayes on just text and category versus on a tweet.
 	*@param text The text to be trained upon
 	*@param cat The category the text belongs to true or false whether or not 
 	*@return Returns true or false if the training was commited to the database.
 	*/
 	public boolean trainOnText(String text, int cat){
 		//make a dummy tweet
 		boolean valid = false;
 		for(int acat : bayes.categories){
 			if(cat == acat){
 				valid = true;
 			}
 		}
 		//We will not accept weird categories.
 		if(!valid){return false;}
 		//Train the bayes
 		bayes.train(cat,text);
 		//commit the changes to the database so we can preserve our smartness
 		return commitTrain(text,cat);
 
 	}
 
 	/**
 	*Commits the text and category to the database that the naive bayes will train from. Without the training data being specifically a tweet.
 	*@param text the text to train on
 	*@param cat the category to classify the text into.
 	*@return true if the data was commited to the database
 	*/
 	public boolean commitTrain(String text, int cat){
 		return data.sendTrainingData(cat,text);
 	}
 
 	/**
 	*Checks to see the percentage correct on a set of training tweets. 
 	*@param set Arraylist of Training Tweets to validate the bayes on. 
 	*@return Returns the percentage correct on the validation set.
 	*/
 	public float validateOn(ArrayList<Training_Tweet> set){
 		//Go through the set and see if the categorys of the tweets
 		//match what the bayes returns. Return a percentage of correctness
 		int total = set.size();
 		int correct = 0;
 		int false_positives = 0;
 		int false_negatives = 0;
 		Training_Tweet t = set.get(0);
 		for(int s = 1; s < total && t != null; s++){
 			
 			int c = t.getCategory();
 			int guess = bayes.classify(t.getTweetText());
 			if(c == guess){
 				correct = correct + 1;
 			}else if(c == NaiveBayes.CAT_SAFE){
 				false_negatives++;
 			}else{
 				false_positives++;
 			}
 			t = set.get(s);
 		}
 		System.out.println("Correct: "+ correct);
 		System.out.println("Incorrect: "+ (total - correct));
 		System.out.println("False Positives: " + false_positives);
 		System.out.println("False Negatives: " + false_negatives);
 		
 		return correct/(float)total;
 	}
 
 	/**
 	*Checks to see the percentage correct on a default training set. Validates on a fifth of the data set itself.
 	*@return Returns the percentage correct on the validation set.
 	*/
 	public float validateOn(){
 		//Create a validation set
 		ArrayList<Training_Tweet> set = new ArrayList<Training_Tweet>();
 		for(int i = 0; i < data.size()/10; i ++){
 			set.add((Training_Tweet)data.getNext());
 		}
 
 		return validateOn(set);
 	}
 
 	/**
 	*Performs K-fold cross validation with k=10 on the dataset. Partitions the full data set into 10 sets and performs the training and folds.	
 	*/
 	public void crossValidation(){
 		//K fold cross validation with k=10
 		int size = data.size();
 		int foldSize = size/10;
 		//Break everything into sets
 		ArrayList<List<Training_Tweet>> validationSets = new ArrayList<List<Training_Tweet>>();
 		for(int k = 0; k < 10; k++){
 			//Get the validation sets:
 			List<Training_Tweet> set = new ArrayList<Training_Tweet>();
 			for(int j = 0; j < foldSize; j++){
 				Training_Tweet t = (Training_Tweet)data.getNext();
 				if(t != null){	
 					set.add(t);
 				}
 			}
 			validationSets.add(set);
 		}
 		//Train with each set and validate?
 		ArrayList<Training_Tweet> validSet;
 		ArrayList<Training_Tweet> set;
 		for(int k=0; k < 10; k++){
 			validSet = (ArrayList<Training_Tweet>) validationSets.get(k);
 			for(int j = 0; j < 10; j++){
 				//Don't train on the validation set
 				if(j!=k){
 					set = (ArrayList<Training_Tweet>)validationSets.get(j);
 					for(int h = 0; h < set.size(); h++){ 
 						bayes.train(set.get(h).getCategory(),set.get(h).getTweetText());
 					}
 				}
 			}
 			//Validate:
 			//System.out.println(validateOn(validSet));
 		}
 
 
 	}
 
 	/**
 	*Runs the BayesTrainer, iniatlizes the database and then trains the bayes on it.
 	*TODO: Make it wait around and classify everything for us via some server socket interactions
 	*/
 	public void run(String password,boolean debugOn){
 		//Create the DataSet
 		if(!initializeData(password)){
 			System.out.println("Failed to Initalize Data Set for Classifier");
 			return;
 		}
 		//Begin Training the data on everything in the dataset
 		//crossValidation();
 		trainBayes();
 		if(debugOn){ 
 			System.out.println(validateOn());
 		}
 
 
 	}
 
 	/**
 	*Closes the connection to the DataSet
 	*/
 	public void close(){
 		data.close();
 	}
 
 	public static void main(String[] args) {
 		//Command line parameter of password
 		if(args.length < 1){
 			System.out.println("Required parameter: Password");
 			System.exit(1);
 		}
 
 		BayesTrainer bt = new BayesTrainer();
 		bt.run(args[0],true);
 		
 	}
 
 
 
 
 }
