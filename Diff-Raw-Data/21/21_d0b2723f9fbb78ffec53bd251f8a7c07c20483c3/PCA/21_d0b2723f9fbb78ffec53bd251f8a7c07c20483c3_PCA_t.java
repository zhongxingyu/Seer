 package pt.utl.ist.tese.pca;
 
 import java.io.Serializable;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 
 import org.grlea.log.SimpleLogger;
 
 import pt.utl.ist.tese.domain.ImageInfo;
 import pt.utl.ist.tese.exception.DifferentSizesException;
 import pt.utl.ist.tese.exception.EmptyInputException;
 import pt.utl.ist.tese.exception.InvalidDimensionException;
 import pt.utl.ist.tese.utils.MemoryUsage;
 import pt.utl.ist.tese.utils.Timer;
 
 import Jama.Matrix;
 
 /**
  * Uses the Principal Components Analysis to reduce the dimension of an object, 
  * it has now the capability to reduce a byte and double array
  * 
  * @author André
  */
 public class PCA implements Serializable {
 	/**
 	 * Unique identifier for the class
 	 */
 	private static final long serialVersionUID = 2863960796181415298L;
 	/**
 	 * logger variable
 	 */
 	private static final SimpleLogger log = new SimpleLogger(PCA.class);
 	
 	/**
 	 * Static method that receives a list of ImageInfo and calculates the PCA 
 	 * with the given sample and subspace
 	 * @param list 		containing ImageInfo objects
 	 * @param index 	of the target subspace
 	 * @param sample 	number of images to be used when calculating the PCA
 	 * @return a PCA object ready to be used to reduce data
 	 */
 	public static PCA calculatePCA(ArrayList<ImageInfo> list , int index , int sample) {
 		log.entry("pca -- calculating the pca and its components on a " + list.size() + " collection using a " +
 				sample + " sample (resolution: " + 
 				ImageInfo.SUBSPACES[index] + "x" + ImageInfo.SUBSPACES[index] +")");
 		MemoryUsage mem = new MemoryUsage();
 		Timer timer = new Timer();
 		PCA pca = null;
 		ArrayList<byte[]> subspace = new ArrayList<byte[]>();
 		int start = 0, end;
 		do {
 			end = ImageInfo.loadPixelVectorsToMemory( index , list , subspace , sample);
 			
 			start = subspace.size() + start;			
 		} while ( end <= 0 && subspace.size() < sample );
 		
 		try {	
 			pca = new PCA( subspace , true );
 			//pca = new PCA( StatisticalSampleData.getData(StatisticalSampleData.SAMPLE_DATA_SAD));
 		} catch (InvalidDimensionException e) {
 			log.error("error: " + e.getMessage());
 		} catch (EmptyInputException e) {
 			log.error("error: " + e.getMessage());
 		} catch (DifferentSizesException e) {
 			log.error("error: " + e.getMessage());
 		}
 		
 		log.exit("pca -- reduced from " + pca._dimension + " to " + pca.getReducedDimension() + " -- in " + timer + " - memory usage " + mem);
 		return pca;
 	}
 	/**
 	 * temporary stores the EigenValue and EigenVectors while finding the most 
 	 * significant EigenVectors
 	 */
 	protected EigenvalueDecomposition _eig;
 	/**
 	 * this array stores the mean values for each dimension and it's used to adjust 
 	 * the data before calculating the EigenValues and EigenVectors
 	 */
 	protected double[] _mean;
 	/**
 	 * number of images to be used when calculating the PCA (the more images the more 
 	 * representative is the PCA, but the more dimension it will have 
 	 */
 	protected int _sample;
 	/**
 	 * original dimension of the data
 	 */
 	protected int _dimension;
 	/**
 	 * original resolution of the data (ex: a 256x256 image has 256 resolution)
 	 */
 	protected int _originalResolution;
 	/**
 	 * the matrix with the most significant EigenVectors that will be used to 
 	 * reduce the dimension of the data
 	 */
 	protected Matrix _eigenVectorsReduced;
 	/**
 	 * Constructor for data that consists of byte arrays
 	 * 
 	 * @param data		data that's used to calculate the PCA
 	 * @param isByte 	flag that lets the constructor know that each of 
 	 * 				the ArrayList's element is a byte array 
 	 * @throws EmptyInputException
 	 * @throws DifferentSizesException
 	 * @throws InvalidDimensionException
 	 */
 	public PCA( ArrayList<byte[]> data , boolean isByte ) throws EmptyInputException, DifferentSizesException, InvalidDimensionException{	
 		MemoryUsage mem = new MemoryUsage();
 		Timer timer = new Timer();
 		_sample = data.size();
 		log.debug("starting by calculating the CovarianceMatrix");
 		CovarianceMatrix co = new CovarianceMatrix(data , isByte);
 		log.debug("CoVarianceMatrix -- mem: " + mem + " - " + timer + " (next: EigenvalueDecomposition)");
 		privateConstructor(co, mem , timer);
 	}
 	/**
 	 * Constructor for data that consists of double arrays
 	 * 
 	 * @param data data that's used to calculate the PCA
 	 *
 	 * @throws EmptyInputException
 	 * @throws DifferentSizesException
 	 * @throws InvalidDimensionException
 	 */
 	public PCA( ArrayList<double[]> data ) throws EmptyInputException, DifferentSizesException, InvalidDimensionException{
 		MemoryUsage mem = new MemoryUsage();
 		Timer timer = new Timer();
 		_sample = data.size();
 		log.debug("starting by calculating the CovarianceMatrix");
 		CovarianceMatrix co = new CovarianceMatrix(data);
 		log.debug("CoVarianceMatrix -- mem: " + mem + " - " + timer + " (next: EigenvalueDecomposition)");
 		privateConstructor(co, mem , timer);
 	}
 	/**
 	 * Calculates the matrix with the most significant components using the Kaiser 
 	 * criteria, which says that if the EigenValue is less than 1 then that dimension 
 	 * can be discarded.
 	 *  
 	 * @return a new Matrix with the most significant EigenVectors
 	 * @throws InvalidDimensionException
 	 */
 	public Matrix calculateComponents( ) throws InvalidDimensionException {
 
 		int cont = 0;
 		double[] eigValues = _eig.getRealEigenvalues();
 
 		for ( double a : eigValues )
 			if ( a >= 1 ) cont++;
 
 		return calculateComponents( cont );
 	}
 	/**
 	 * Calculates the matrix with the most significant components using the Kaiser 
 	 * criteria, which says that if the EigenValue is less than 1 then that dimension 
 	 * can be discarded.
 	 *  
 	 * @param newDimension new dimension
 	 * @return a new Matrix with the most significant EigenVectors
 	 * @throws InvalidDimensionException
 	 */
 	public Matrix calculateComponents( int newDimension ) throws InvalidDimensionException {
 
 		if ( newDimension > _eig.dimension() || newDimension <= 0)
 			throw new InvalidDimensionException("Dimension \'" + newDimension + 
 					"\' is invalid, the new dimension must be bigger than 0 and " +
 					"smaller than the current (" + _eig.dimension() + ")");
 
 		int dim = _eig.dimension();
 
 		double[][] eigenVectors = new double[newDimension][];
 
 		int eigMask[] = new int[newDimension];
 		for ( int j = 0 , i = dim  ; i >= 0 && i > dim - newDimension ; i-- , j++) {
 			eigMask[j] = i - 1;
 		}
 		int j = 0;
 		for ( int i : eigMask ) {
 			eigenVectors[j++] = _eig.getV()[i];
 		}
 
 		return new Matrix(eigenVectors);
 	}
 	/**
 	 * Adjusts the data of a single double array 
 	 * 
 	 * @param input original data
 	 * @return an array with the data transformed
 	 */
 	protected double[] dataAdjust( double[] input ) {
 		for ( int k = 0 ; k  < input.length ; k++  )
 			input[k] -= _mean[k];
 		return input;
 	}
 	/** 
 	 * Adjusts the data of a several double array 
 	 * 
 	 * @param input original data
 	 * @return a table with the data transformed
 	 */
 	protected double[][] dataAdjust( double[][] input ) {
 		for ( double[] vect : input )
 			dataAdjust( vect );
 		return input;
 	}
 	public Matrix getReducedMatrix() { return _eigenVectorsReduced; }
 	/**
 	 * getter for the original dimension of the data 
 	 * @return returns the original dimension of the data
 	 */
 	public int getOriginalDimension( ) {
 		return _dimension;
 	}
 	/**
 	 * getter for the original resolution of the data 
 	 * @return returns the original resolution of the data
 	 */
 	public int getOriginalResolution( ) {
 		return _originalResolution;
 	}
 	/**
 	 * getter that returns how many significant EigenVectors have been kept  
 	 * @return the dimension of the reduced data
 	 */
 	public int getReducedDimension( ) {
 		return _eigenVectorsReduced.getRowDimension();
 	}
 		
 	/**
 	 * getter of the sample variable
 	 * 
 	 * @return the number of images that were used to calculate the PCA
 	 */
 	public int getSample() {
 		return _sample;
 	}
 	/**
 	 * builds a string with the matrix content
 	 * @param m the matrix 
 	 * @return a new string displaying the content of the matrix
 	 */
 	public String print( Matrix m ) { return print( m , false); }
 	/**
 	 * builds a string with the matrix content
 	 * @param m 	the matrix
 	 * @param eigen flag that lets the method know if the matrix is an 
 	 * 		EigenVector or not 
 	 * @return a new string displaying the content of the matrix
 	 */
 	public String print( Matrix m , boolean eigen ) {
 
 		String s = "{";
 
 		int j = 0;
 		int rows = m.getArray().length;
 		for ( double[] vect : m.getArray()) {
 			s += "{";
 
 			for ( int i = 0 ; i < vect.length ; i++ ) {
 				s+= "" + NumberFormat.getInstance().format(vect[i]);
 				//				while ( a.length() != 4 ) 
 				//					a += " ";
 
 				if ( eigen ) s+="t";
 				if ( i < vect.length - 1 ) s +=",";
 			}
 			s += "}";
 			if ( j++ < rows - 1 ) s+= ",\n";
 		}
 		s +="}";
 
 		return s;
 
 	}
 	/**
 	 * builds a string with the EigenValues
 	 * 
 	 * @return a new string array with all the EigenValues
 	 */
 	public String[] printEigenValues( )  {
 		double a[] = _eig.getRealEigenvalues();
 
 		ArrayList<String> s = new ArrayList<String>();
 
 		int k = 0;
 
 		for ( double val : a )
 			s.add( new String( k++ + ": " + val + "\t" ));
 
		return s.toArray(new String[ 1 ]);
 
 	}
 	/**
 	 * builds a string with the EigenValues
 	 * 
 	 * @param eig an EigenValueDecomposition to be printed
 	 * @return a new string displaying the EigenValues
 	 */
 	public String printEigenValues( EigenvalueDecomposition eig ) {
 		double a[] = eig.getRealEigenvalues();
 		String s = "eigen values: \t";
 		int k = 0;
 
 		for ( double val : a ) {
 			s += k++ + ": " + val + "\t";
 
 		}
 		return s;
 	}
 	/**
 	 * This method contains the common steps of the constructor that includes 
 	 * initialising some variables and calculate the EigenValue Decomposition, 
 	 * as well as the most relevant components
 	 * 
 	 * @param covarianceMatrix	the covariance matrix 
 	 * @param mem				a memory object that's used to display the memory 
 	 * 						usage of the PCA process
 	 * @param timer				a timer object that's used to display how long the 
 	 * 						PCA process takes 
 	 * @throws InvalidDimensionException
 	 */
 	private void privateConstructor( CovarianceMatrix covarianceMatrix, MemoryUsage mem, Timer timer ) throws InvalidDimensionException{
 		_dimension = covarianceMatrix.dimension();
 		_originalResolution = (int)Math.sqrt(covarianceMatrix.dimension() / 3);
 		_mean = covarianceMatrix.mean();
 		_eig = new EigenvalueDecomposition( covarianceMatrix.m() );
 		log.debug("EigenvalueDecomposition -- mem: " + mem + " - " + timer + " (next: calculateComponents)");
 		_eigenVectorsReduced = calculateComponents();
 		log.debug("calculateComponents -- mem: " + mem + " - " + timer);
 		_eig = null;
 	}
 	/**
 	 * reduces the dimension of a byte array
 	 * @param data to be reduced
 	 * @return the transformed data in a new dimension
 	 */
 	public float[] reduce( byte[] data ) {
 		return reduce( data , _eigenVectorsReduced);
 	}
 	/**
 	 * reduces the dimension of a byte array using the given Matrix
 	 * @param data 			to be reduced
 	 * @param components	matrix that is used to reduce the data
 	 * @return the transformed data in a new dimension
 	 */
 	protected float[] reduce( byte[] data , Matrix components) {
 		if (data.length != components.getColumnDimension()) {
 			log.error("matrix inner dimensions must agree -- " + 
 					components.getRowDimension() + "x" + components.getColumnDimension() + " " + data.length + "x1");
 			return null;
 		}
 		double[][] A = components.getArray();
 		
 		float[] C = new float[components.getRowDimension()];
 		
 		for (int i = 0; i < components.getRowDimension() ; i++) {
 			
 			double[] Arowi = A[i];
 			double s = 0;
 			for (int k = 0; k < data.length ; k++) {
 				s += Arowi[k] * data[k];
 			}
 			C[i] = (float)s;
 		}
 		return C;
 	}
 	/**
 	 * reduces the dimension of a byte array using the given Matrix
 	 * @param data to be reduced
 	 * @return the transformed data in a new dimension
 	 * @throws DifferentSizesException
 	 */
 	public double[] reduce( double[] data  ) throws DifferentSizesException {
 
 		double[][] temp = new double[1][];
 		temp[0] = data;
 
 		return reduce( new Matrix(temp)  , _eigenVectorsReduced )[0];
 	}
 	/**
 	 * reduces the dimension of a byte array using the given Matrix (this method need 
 	 * an EigenValueDecomposition in order to reduce the data to the given dimension, 
 	 * remove the "_eig = null" line from the privateConstructor method)"
 	 * 
 	 * @param data 			to be reduced
 	 * @param newDimension 	dimension of the transformed data 	
 	 * @return the transformed data in a new dimension
 	 * @throws DifferentSizesException
 	 * @throws InvalidDimensionException
 	 */
 	protected  double[] reduce( double[] data , int newDimension ) throws DifferentSizesException , InvalidDimensionException {
 		double[][] temp = { data };
 
 		return reduce( new Matrix(temp) , calculateComponents( newDimension ))[0];
 	}
 	/**
 	 * reduces the dimension of a byte array using the given Matrix
 	 * @param data to be reduced
 	 * @return the transformed data in a new dimension
 	 * @throws DifferentSizesException
 	 */
 	public double[][] reduce( double[][] data ) throws DifferentSizesException {
 		return reduce( new Matrix(data) , _eigenVectorsReduced );
 	}
 	/**
 	 * reduces the dimension of a byte array using the given Matrix
 	 * @param matrix 		to be reduced
 	 * @param components 	matrix that is used to reduce the data
 	 * @return the transformed data in a new dimension
 	 * @throws DifferentSizesException
 	 */
 	protected double[][] reduce ( Matrix matrix , Matrix components )
 	throws DifferentSizesException {
 
 		if ( matrix.getColumnDimension() != components.getColumnDimension() )
 			throw new DifferentSizesException(
 					"matrix and components size don't match, in order to reduce the dimension " + 
 			"the columns of both matrixes must match (data matrix will be transposed)");
 
 		int newDimension = components.getRowDimension();
 
 		Matrix newMatrix = new Matrix( matrix.getRowDimension() , newDimension );
 		int cols = matrix.getColumnDimension() - 1;
 
 		for ( int rowIndex = 0 ; rowIndex < matrix.getRowDimension() ; rowIndex++ ) {
 			Matrix row = matrix.getMatrix(rowIndex, rowIndex, 0, cols );
 			Matrix temp = components.times(row.transpose()).transpose();
 			newMatrix.setMatrix( rowIndex, rowIndex, 0, newDimension - 1, temp);
 		}
 
 		return newMatrix.getArray();
 	}
 	/**
 	 * setter for the sample variable
 	 * @param _sample
 	 */
 	public void setSample(int _sample) {
 		this._sample = _sample;
 	}
 
 }
 
 
