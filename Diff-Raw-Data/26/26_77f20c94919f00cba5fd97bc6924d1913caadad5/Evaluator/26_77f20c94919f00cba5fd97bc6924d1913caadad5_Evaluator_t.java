 import java.util.Iterator;
 import java.util.TreeSet;
 
 
 public class Evaluator {
 	
 	
 
 	public Recommender train(InstanceBase _userData, InstanceBase[] _itemSimilarity) {
 		ParameterSet parameters = new ParameterSet();
 		boolean[] filter = new boolean[_userData.getNumInstances()];
 		SparseFloatMatrix ratrings = createRatingMatrix(_userData, filter, parameters);
 		return new Recommender(ratrings);
 	}
 	
 	/**
 	 * Calculates root mean squared error 
 	 * @param _rec recomendation model
 	 * @param _testMatrix test data
 	 * @return rmse
 	 */
 	public float RMSE(Recommender _rec, SparseFloatMatrix _testMatrix) {
 		float rmse = 0;
 		int n = 0;
 		for(int i = 0; i < _testMatrix.getNumRows(); i++) {
 			for(Iterator<SparseFloatMatrix.IndexValuePair> it = _testMatrix.getSkipIterator(i); it.hasNext(); ) {
 				SparseFloatMatrix.IndexValuePair entry = it.next();
 				 rmse += Math.pow(_rec.getPrediction(i, entry.index) - entry.value, 2);
 				 n++;
 			}	
 		}
 		
 		return (float) Math.sqrt(rmse / n);		
 	}
 	
 	/**
 	 * Calculates part of root mean squared error, that will be used for calculation of rmse for all folds. 
 	 * @param _rec recomendation model
 	 * @param _testMatrix test data
 	 * @return part of rmse
 	 */
 	public float rmseCrossValidation(Recommender _rec, SparseFloatMatrix _testMatrix) {
 		float rmse = 0;
 		for(int i = 0; i < _testMatrix.getNumRows(); i++) {
 			for(Iterator<SparseFloatMatrix.IndexValuePair> it = _testMatrix.getSkipIterator(i); it.hasNext(); ) {
 				SparseFloatMatrix.IndexValuePair entry = it.next();
 				 rmse += Math.pow(_rec.getPrediction(i, entry.index) - entry.value, 2);
 			}	
 		}
 		
 		return rmse;		
 	}
 	
 	/**
 	 * Shuffles an array 
 	 * @param _array array that will be shuffled
 	 * @param _size number of elements in the set to cross validate
 	 * @return randomly shuffled array
 	 */
 	private int[] shuffleArray(int[] _array, int _size)
 	{
 		int[] newArray = _array;
 	    int n = _size;
 	    while (n > 1)
 	    {
 	        // 0 <= k < n
 	        int k = (int) (Math.random() * n);
 
 	        // n is now the last pertinent index
 	        n--;
 
 	        // swap array[n] with array[k]
 	        int temp = newArray[n];
 	        newArray[n] = newArray[k];
 	        newArray[k] = temp;
 	    }
 	    return newArray;
 	}
 
 	/**
 	 * Creates indices for cross validation folds 
 	 * @param _size number of elements in the set to cross validate
 	 * @param _k number of folds
 	 * @return an array with randomly distributed indices 
 	 */
 	private int[] kfold(int _size, int _k)
 	{
 		int[] resArray = new int[_size];
 	    double inc = (double)_k/_size;
 
 	    for (int i = 0; i < _size; i++) {
 	        resArray[i] = (int) (Math.ceil((i+0.9)*inc) - 1);
 	    }	    
 	    
 	    return shuffleArray(resArray,_size);
 	}
 	
 	/**
 	 * Fill a sparse matrix with all allowed actions from the database.
 	 * @param _userData The database which contains all actions
 	 * @param _filterActions An array with the same number of entries as in the
 	 * 	database. For each entry set to true the matrix will be updated. If
 	 * 	filter action is false the entry will be skipped.
 	 * @return A matrix with a size of #users x #items.
 	 */
 	SparseFloatMatrix createRatingMatrix(InstanceBase _userData, boolean[] _filterActions, ParameterSet _param) {
 		int numItems = _userData.getNumUniqueEntries(2);
 		int numUsers = _userData.getNumUniqueEntries(0);
 		SparseFloatMatrix ratrings = new SparseFloatMatrix( numUsers, numItems );
 				
 		// Iterate over the training data and increase the entries for the users
 		// actions.
 		int i=0;
 		for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
 			int[] line = it.next();
 			if( _filterActions[i++] ) {
 				float newValue = _param.ActionWeight[line[1]];
 				newValue += ratrings.get(line[0], line[2]);
 				ratrings.set(line[0], line[2], newValue);
 			}
 		}
 		
 		return ratrings;
 	}
 
 	/**
 	 * Cross validates input data. Creates recommender for each fold of train data. 
 	 * Calculates and returns cumulative RMSE error of the model.
 	 * @param _userData The database which contains all actions
 	 * @param _numberOfFolds number of folds for cross validation
 	 * @return rmse error of the model
 	 */
	public float crossValidate(InstanceBase _userData, int _numberOfFolds) {
		
		ParameterSet parameters = new ParameterSet();
 		
 		int[] folds = kfold(_userData.getNumInstances(), _numberOfFolds);
 		float rmse = 0;
 		int numItems = _userData.getNumUniqueEntries(2);
 		int numUsers = _userData.getNumUniqueEntries(0);
 		
 		for(int fold = 0; fold < _numberOfFolds; fold++) {
 			SparseFloatMatrix ratingsTest = new SparseFloatMatrix( numUsers, numItems );
 			SparseFloatMatrix ratingsTrain = new SparseFloatMatrix( numUsers, numItems );
 			// Iterate over the training data and increase the entries for the users
 			// actions.
 			int i=0;
 			for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
 				int[] line = it.next();
 				if( folds[i++] == fold) {
					float newValue = parameters.ActionWeight[line[1]];
 					newValue += ratingsTest.get(line[0], line[2]);
 					ratingsTest.set(line[0], line[2], newValue);
 				}
 				else{
					float newValue = parameters.ActionWeight[line[1]];
 					newValue += ratingsTrain.get(line[0], line[2]);
 					ratingsTrain.set(line[0], line[2], newValue);
 				}
 			}
 			
 			Recommender recommender = new Recommender(ratingsTrain);
 			rmse += rmseCrossValidation(recommender, ratingsTest);
 		}
 		
 		
 		return (float) Math.sqrt(rmse/_userData.getNumInstances());
 	}
 	
 	/**
 	 * Derived Data
 	 * 
 	 * The userList contains all unique user ids which can be accessed by there
 	 * indices afterwards.
 	 */
 	//private String[] m_UserList;
 
 	/**
 	 * Derived Data
 	 * 
 	 * A list of unique names.
 	 */
 	//private String[] m_NameList;
 }
