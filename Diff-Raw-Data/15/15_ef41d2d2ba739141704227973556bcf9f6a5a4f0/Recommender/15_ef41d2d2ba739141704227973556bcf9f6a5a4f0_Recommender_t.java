 import java.util.Iterator;
 
 
 public class Recommender {
 	
 	// Parameters to tune the algorithm
 	int MAX_RANK = 12;			// The rank how many weights should be used per item.
			 					// 	Using a larger number should always increase the quality of the outcome.
 	
 	int ITERATRIONS = 15;		// Number of iterations - above 20 the quality is not increased that much.
 	float GAMMA = 0.002f;
 	float LAMBDA = 0.04f;
 	
 	float ALPHA = 25;			// Initialization shrinkage
 	float EPSILON = 0.0001f;	// Error on initialization 
 	
 	private SparseFloatMatrix m_WeightTable;
 	private FloatVector m_X[];	// characterize items (2D array: #items * FloatVector(MAX_RANK))
 	private FloatVector m_Y[];	// characterize users based on the items they rated (2D array: #items * FloatVector(MAX_RANK))
 	private FloatVector m_Q[];	// Is the item's influence positive or negative (array: #items). Using m_Q = m_X enforces symmetric weights (see page 177) -> left out
	private FloatVector m_P[];	// User factors of the fallback method (array: #users)
 	float m_AverageRating;		// The average rating over the whole table
 	private float m_Bu[];		// Observed deviations of user u from the average
 	private float m_Bi[];		// Observed deviations of item i from the average
 	
 	/**
 	 * Creates a new trained recommender.
 	 * @param _userData The training data.
 	 * @param _itemList A list of all available items.
 	 * 
 	 * TODO: use a mask to disable the usage of all userData (cross validation)
 	 */
 	public Recommender( InstanceBase _userData, String[] _itemList, ParameterSet _Param ) {
 		int numItems = _userData.getNumUniqueEntries(2);
 		int numUsers = _userData.getNumUniqueEntries(0);
 		m_WeightTable = new SparseFloatMatrix( numUsers, numItems );
 		
 		m_AverageRating = 0.0f;
 		
 		// Iterate over the training data and increase the entries for the users
 		// actions.
 		for(Iterator<int[]> it = _userData.getMappedIterator(); it.hasNext(); ) {
 			int[] line = it.next();
 			float newValue = _Param.ActionWeight[line[1]];
 			m_AverageRating += newValue;
 			newValue += m_WeightTable.get(line[0], line[2]);
 			m_WeightTable.set(line[0], line[2], newValue);
 		}
 		
 		m_AverageRating /= numItems*numUsers;
 		learnFactorizedNeighborhoodModel();
 	}
 	
 	
 	public String[] getItemListForUser(int index) {
 		for( int i=0; i<m_WeightTable.getNumRows(); ++i) {
 			int iLastCol = -1;
 			// Temporary test code
 			for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(i); it.hasNext(); ) {
 				SparseFloatMatrix.IndexValuePair e0 = it.next();
 				if( iLastCol > e0.index )
 					System.out.println("Error in the matrix layout.");
 				iLastCol = e0.index;
 			}
 		}
 		return null;
 	}
 	
 
 	/**
 	 * Compute the minimum between how many items did the user rate for and
 	 * how often an item was counted
 	 * @return
 	 */
 	private int support( int _user, int _item )
 	{
 		// count how often an item is rated by polling.
 		int ic = 0;
 		for( int i=0; i<m_WeightTable.getNumRows(); ++i )
 			if( m_WeightTable.get(i, _item) > 0.0f ) ++ic;
 		return Math.min(m_WeightTable.getNumEntriesInRow(_user), ic);
 	}
 	
 	/**
 	 * Computes sum (r_ui-r^_ui) with
 	 * r^_ui = P dot Q
 	 * This measurement is only required for initialization of the latent
 	 * factor space. Later on r^_ui consists of more factors.
 	 * @return Rating error of matrices Q and P.
 	 */
 	private float squaredError()
 	{
 		float res = 0;
 		for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
 			for( Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
 				SparseFloatMatrix.IndexValuePair e0 = it.next();
 				float ratingError = e0.value - m_P[u].dot(m_Q[e0.index]);
 				res += ratingError * ratingError;
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Compute the f-th column of matrices Q,X and Y. Columns 1 to 1-f are
 	 * already computed.
 	 * @param f
 	 */
 	private void computeNextFactor( int f )
 	{
 		// Compute residuals-portion not explained by previous factors.
 		SparseFloatMatrix ratingErrors = new SparseFloatMatrix(m_WeightTable.getNumRows(), m_WeightTable.getNumColumns());
 		// For each known rating
 		for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
 			for( Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
 				SparseFloatMatrix.IndexValuePair e0 = it.next();
 				int i = e0.index;
 				// Dot product of P_i and Q_j with respect to the computed part
 				// only (which is easy, because everything else is 0.
 				float ratingError = e0.value - m_P[u].dot(m_Q[i]);
 				// Shrinkage
 				float n_ui = support(u,i);
 				ratingError = n_ui*ratingError / (n_ui+ALPHA * f);
 				ratingErrors.set(u, i, e0.value);
 			}
 		}
 		
 		// Solving many least square problems
 		// THE WHILE LOOP IN [NetflixKDD07] HAS AN SENSLESS CONDITION?
 		float errOld = squaredError();
 		float errNew = errOld;
 		while( errNew/errOld > 1-EPSILON ) {
 			// For each user
 			for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
 				float newFactorNum = 0;
 				float newFactorDen = 0.00000001f;
 				for( Iterator<SparseFloatMatrix.IndexValuePair> it = ratingErrors.getSkipIterator(u); it.hasNext(); ) {
 					SparseFloatMatrix.IndexValuePair e0 = it.next();
 					newFactorNum += e0.value * m_Q[e0.index].get(f);
 					newFactorDen += m_Q[e0.index].get(f) * m_Q[e0.index].get(f);
 				}
 				m_P[u].set( f, newFactorNum/newFactorDen );
 			}
 			// for each item
 			for( int i=0; i<m_WeightTable.getNumColumns(); ++i ) {
 				float newFactorNum = 0;
 				float newFactorDen = 0.00000001f;
 				for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
 					newFactorNum += ratingErrors.get(u, i) * m_P[u].get(f);
 					newFactorDen += m_P[u].get(f) * m_P[u].get(f);
 				}
 				m_Q[i].set( f, newFactorNum/newFactorDen );
 			}			
 			
 			errNew = squaredError();
 		}
 	}
 	
 	/**
 	 * Once m_P and m_Q are computed derive an initial state of m_X and m_Y.
 	 */
 	private void deriveXAndY() {
 		// Use symmetric case m_Q = m_X as initial state -> copy
 		for( int i=0; i<m_WeightTable.getNumColumns(); ++i )
 			for( int f=0; f<MAX_RANK; ++f )
 				m_X[i].set(f, m_Q[i].get(f));
 		// P_u = |R(u)|^-0.5 SUM iR(u) [(r_uj-b_uj)*x_i+y_i]
 		//	   = |R(u)|^-0.5 SUM iR(u) [(r_uj-b_uj)*x_i] + SUM iR(u) [y_i]
 		// SUM iR(u) [y_i] = P_u - |R(u)|^-0.5 SUM iR(u) [(r_uj-b_uj)*x_i]
 		// => Equation system (left side unknown, right side compute-able
 		// To get a rough estimate use "inverse Radon transformation"
 		// y_i += (P_u - |R(u)|^-0.5 SUM iR(u) [(r_uj-b_uj)*x_i])/|R(u)|			(1)
 		for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
 			float norm = (float)(1.0/(m_WeightTable.getNumEntriesInRow(u)*Math.sqrt(m_WeightTable.getNumEntriesInRow(u))));
 			FloatVector p = new FloatVector(MAX_RANK);
 			for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
 				SparseFloatMatrix.IndexValuePair entry = it.next();	// entry.index==j, entry.value==r_uj
 				float b_uj = computeBaselinePredictor(u, entry.index);
 				p.add( FloatVector.mul((entry.value-b_uj)*norm, m_X[entry.index] ) );
 			}
 			p = FloatVector.mad( -1.0f/m_WeightTable.getNumEntriesInRow(u), m_P[u], p );	// == - right side of (1)
 			for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); )
 				m_Y[it.next().index].sub(p);	// -= -right side   ==   += right side
 		}
 	}
 	
 	private void initializeItemAttributes() {
 		// Initialization of the two matrices x,y means to fill them with zero
 		m_X = new FloatVector[m_WeightTable.getNumColumns()];
 		m_Y = new FloatVector[m_WeightTable.getNumColumns()];
 		m_Q = new FloatVector[m_WeightTable.getNumColumns()];
 		for( int i=0; i<m_WeightTable.getNumColumns(); ++i ) {
 			// TODO: fill in non zeros
 			m_X[i] = new FloatVector(MAX_RANK);
 			m_Y[i] = new FloatVector(MAX_RANK);
 			m_Q[i] = new FloatVector(MAX_RANK);
 		}
 		m_P = new FloatVector[m_WeightTable.getNumRows()];
 		for( int i=0; i<m_WeightTable.getNumRows(); ++i )
 			m_P[i] = new FloatVector(MAX_RANK);
 		
 		// Fill vectors with latent factors
 		for( int i=0; i<MAX_RANK; ++i )
 			computeNextFactor( i );
 		deriveXAndY();
 		
 		m_Bu = new float[m_WeightTable.getNumRows()];
 		m_Bi = new float[m_WeightTable.getNumColumns()];
 	}
 	
 	/**
 	 * Compute baseline predictors b_ui.
 	 * @param _user The user which's items are of interest.
 	 * @param _item The item of interest.
 	 * @return The baseline predictor.
 	 */
 	private float computeBaselinePredictor( int _user, int _item ) {
 		return m_Bu[_user] + m_Bi[_item] + m_AverageRating;
 	}
 
 	private void learnFactorizedNeighborhoodModel() {
 		initializeItemAttributes();
 		for( int i=0; i<ITERATRIONS; ++i ) {
 			for( int u=0; u<m_WeightTable.getNumRows(); ++u ) {
 				FloatVector sum = new FloatVector(MAX_RANK);				
 				float norm = (float)(1.0/Math.sqrt(m_WeightTable.getNumEntriesInRow(u)));	// This is |R(u)|^-0.5 in the document
 				// Compute |R(u)|^-0.5 SUM jR(u) [(r_uj-b_uj)*x_j+y_j]
 				FloatVector p = new FloatVector(MAX_RANK);
 				for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
 					SparseFloatMatrix.IndexValuePair entry = it.next();	// entry.index==j, entry.value==r_uj
 					float b_uj = computeBaselinePredictor(u, entry.index);
 					p.add( FloatVector.mad(entry.value-b_uj, m_X[entry.index], m_Y[entry.index] ) );
 				}
 				p.mul(norm);
 				
 				for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
 					SparseFloatMatrix.IndexValuePair entry = it.next();
 					int j = entry.index;
 					float rh_ui = computeBaselinePredictor(u, j) + m_Q[j].dot(p);
 					float e_ui = entry.value - rh_ui;
 					// Accumulate information for gradient descent steps on m_X, m_Y
 					sum.add( FloatVector.mul(e_ui, m_Q[j]) );
 					// Perform gradient steps on m_Q, b_u and b_i
 					m_Q[j].add( FloatVector.mad(GAMMA*e_ui, p, FloatVector.mul(-GAMMA*LAMBDA, m_Q[j])) );
 					m_Bu[u] += GAMMA * (e_ui - LAMBDA * m_Bu[u]);
 					m_Bi[j] += GAMMA * (e_ui - LAMBDA * m_Bi[j]);
 				}
 				
 				for(Iterator<SparseFloatMatrix.IndexValuePair> it = m_WeightTable.getSkipIterator(u); it.hasNext(); ) {
 					SparseFloatMatrix.IndexValuePair entry = it.next();
 					int j = entry.index;
 					m_X[j].add( FloatVector.mad(GAMMA*norm*(entry.value-computeBaselinePredictor(u, j)), sum, FloatVector.mul(-GAMMA*LAMBDA, m_X[j])) );
 					m_Y[j].add( FloatVector.mad(GAMMA*norm, sum, FloatVector.mul(-GAMMA*LAMBDA, m_Y[j])) );
 				}				
 			}
 		}
 	}
 }
