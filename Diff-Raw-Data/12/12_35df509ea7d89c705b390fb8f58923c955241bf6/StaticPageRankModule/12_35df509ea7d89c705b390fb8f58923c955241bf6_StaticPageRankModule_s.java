 package it.uniroma1.dis.wsngroup.extensions.pagerank;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import it.uniroma1.dis.wsngroup.extensions.simulations.Utils;
 
 import com.csvreader.CsvReader;
 import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.impl.DenseDoubleMatrix1D;
 import cern.colt.matrix.impl.DenseDoubleMatrix2D;
 import cern.colt.matrix.impl.SparseDoubleMatrix2D;
 
 /** 
  * Starting from a CSV file representing the weighted adjacency matrix of a graph,
  * computes the PageRank values at each node of the graph through the iterative
  * power method until convergence is reached 
  */
 public class StaticPageRankModule {
 	private Reader reader;
 	private char delimiter;
 	private CsvReader csvReader;
 	private SparseDoubleMatrix2D aggregatedMatrix;
 	private ArrayList<String> nodeIDs;
 	private NumberFormat nf;
 	
 	public StaticPageRankModule(Reader r, char del) throws IOException {
 		System.out.println("Initializing...");
 		reader = r;
 		delimiter = del;
 		
 		csvReader = new CsvReader(reader, delimiter);
 		nodeIDs = new ArrayList<String>();
 		
 		if (!csvReader.readRecord()) {
 			System.out.println("ERROR: the file is empty");
 			System.exit(-1);
 		}
 		
 		String[] firstRow = csvReader.getValues(); //in the first row there are only node IDs
 		for (int i = 1; i < firstRow.length; i++)
 			nodeIDs.add(firstRow[i]);
 		int n = firstRow.length - 1;
 		
 		aggregatedMatrix = new SparseDoubleMatrix2D(n, n);
 		
 		int countRows = 0;
 		while (csvReader.readRecord()) {
 			if (countRows > n - 1) {
 				System.out.println("ERROR: the matrix is not square");
 				System.exit(-1);
 			}
 				
 			String[] rowValues = csvReader.getValues();
 			
 			if (rowValues.length != n + 1) {
 				System.out.println("ERROR: not all rows have the same number of elements");
 				System.exit(-1);
 			}
 			
 			for (int i = 1; i < rowValues.length; i++) //we ignore the first element of each row (it's an ID)
 				aggregatedMatrix.setQuick(countRows, i-1, Double.parseDouble(rowValues[i]));
 
 			countRows++;
 		}
 		
 		if (countRows != n) {
 			System.out.println("ERROR: the matrix is not square");
 			System.exit(-1);
 		}
 		
 		nf = NumberFormat.getInstance();
 		nf.setMaximumFractionDigits(5);
 	}
 	
 	/** Normalizes the matrix such that every row sums to either 1 or 0 */
 	public SparseDoubleMatrix2D normalizeByRowSums(SparseDoubleMatrix2D inputMatrix) throws IOException {
 		System.out.println("Normalizing rows...");
 		SparseDoubleMatrix2D normalizedMatrix = new SparseDoubleMatrix2D(inputMatrix.rows(), inputMatrix.columns());
 		
 		for (int i = 0; i < normalizedMatrix.rows(); i++) {
 			int rowSum = 0;
 
 			for (int j = 0; j < normalizedMatrix.columns(); j++)
 				rowSum += inputMatrix.getQuick(i, j);
 			
 			for (int j = 0; j < normalizedMatrix.columns(); j++) {
 				if (rowSum != 0) {
 					double newElement = (double)inputMatrix.getQuick(i, j)/rowSum;
 					normalizedMatrix.setQuick(i, j, newElement);
 				}
 				else {
 					normalizedMatrix.setQuick(i, j, 0.0);
 				}
 			}
 		}
 		
 		return normalizedMatrix;
 	}
 	
 	/** Replaces elements in zero rows with 1/n */
 	public DenseDoubleMatrix2D fillZeroRows(SparseDoubleMatrix2D inputMatrix) {
 		System.out.println("Filling zero rows...");
 		
 		DenseDoubleMatrix2D stochasticMatrix = new DenseDoubleMatrix2D(inputMatrix.rows(), inputMatrix.columns());
 		double newZeroElementsValue = (double)1/inputMatrix.columns();
 		
 		for (int i = 0; i < stochasticMatrix.rows(); i++) {
 			boolean zeroRow = true;
 
 			for (int j = 0; j < stochasticMatrix.columns(); j++) {
 				if (inputMatrix.getQuick(i, j) > 0) {
 					zeroRow = false;
 					break;
 				}
 			}
 			
 			if (!zeroRow) {
 				for (int j = 0; j < stochasticMatrix.columns(); j++)
 					stochasticMatrix.setQuick(i, j, inputMatrix.get(i, j));
 			}
 			else {
 				for (int j = 0; j < stochasticMatrix.columns(); j++)
 					stochasticMatrix.setQuick(i, j, newZeroElementsValue);
 			}
 		}
 		
 		return stochasticMatrix;
 	}
 	
 	/** P'' = alpha * P' + (1 - alpha) * E */
 	public DenseDoubleMatrix2D addPerturbationMatrix(DenseDoubleMatrix2D stochasticMatrix, double alpha) {
 		System.out.println("Adding perturbation matrix...");
 		
 		DenseDoubleMatrix2D irreducibleMatrix = new DenseDoubleMatrix2D(stochasticMatrix.rows(), stochasticMatrix.columns());
 		int n = stochasticMatrix.columns();
 		
 		// perturbation matrix: eeT/n
 		double perturbationValue = (double)1/n;
 		
 		irreducibleMatrix.assign(perturbationValue);
 		
 		for (int k = 0; k < n; k++) {
 			for (int z = 0; z < n; z++) {
 				irreducibleMatrix.setQuick(k, z, alpha * stochasticMatrix.getQuick(k, z) + 
 						(1 - alpha) * irreducibleMatrix.getQuick(k, z));
 			}
 		}
 		
 		return irreducibleMatrix;
 	}
 	
	/** pi(k+1) = pi(k) * P'' */
 	public DenseDoubleMatrix1D computePageRank(DenseDoubleMatrix2D irreducibleMatrix) {
 		System.out.println("Computing PageRanks...");
 		
 		int n = irreducibleMatrix.columns();
 		double initValue = (double)1/n;
 		
 		DenseDoubleMatrix1D pi = new DenseDoubleMatrix1D(n);
 		
 		//Initialization: pi(0) = eT/n
 		pi.assign(initValue);
 
 		for (int i = 0; i < 350; i++) //how many iterations of the power method?			
 			pi = (DenseDoubleMatrix1D)irreducibleMatrix.zMult(pi, null, 1, 0, true);  
 		
 		return pi;
 	}
 	
 	public void printMatrix(DoubleMatrix2D matrix) {
 		for (int i = 0; i < matrix.rows(); i++) {
 			for (int j = 0; j < matrix.columns(); j++)
 				System.out.print(nf.format(matrix.getQuick(i, j)) + " ");
 			System.out.println("");
 		}
 	}
 	
 	public SparseDoubleMatrix2D getAggregatedMatrix() {
 		return aggregatedMatrix;
 	}
 	
 	public ArrayList<String> getNodeIDs() {
 		return nodeIDs;
 	}
 	
 	public static void main(String[] args) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		System.out.print("Input adjacency matrix CSV file: ");
 		String input = br.readLine();
 		
 		File inputFile = new File(input);
 		
 		if (!inputFile.exists()) {
 			System.out.println("ERROR: invalid file");
 			System.exit(-1);
 		}
 		
 		Reader reader = new FileReader(inputFile);
 		System.out.print("CSV delimiter (1 char): ");
 		char delimiter = br.readLine().charAt(0);
 		
 		StaticPageRankModule am = new StaticPageRankModule(reader, delimiter);
 		SparseDoubleMatrix2D aggregatedMatrix = am.getAggregatedMatrix();
 		SparseDoubleMatrix2D normalizedMatrix = am.normalizeByRowSums(aggregatedMatrix);
 		DenseDoubleMatrix2D stochasticMatrix = am.fillZeroRows(normalizedMatrix);
 		DenseDoubleMatrix2D irreducibleMatrix = am.addPerturbationMatrix(stochasticMatrix, 0.85);
 		DenseDoubleMatrix1D pageRankVector = am.computePageRank(irreducibleMatrix);
 		
 		System.out.println("Computed PageRanks");
 		ArrayList<String> nodeIDs = am.getNodeIDs();
 		
 		HashMap<String, Double> pageRankMap = new HashMap<String, Double>(); 
 		ValueComparator vc =  new ValueComparator(pageRankMap);
         TreeMap<String,Double> sortedPageRankMap = new TreeMap<String,Double>(vc);
         
 		double sum = 0;
 		for (int i = 0; i < pageRankVector.size(); i++) {
 			sum += pageRankVector.get(i);
 			pageRankMap.put(nodeIDs.get(i), pageRankVector.get(i));
 		}
 		
 		sortedPageRankMap.putAll(pageRankMap);
 		
 		StringBuffer sb = new StringBuffer();
 		
 		for(Map.Entry<String,Double> entry : sortedPageRankMap.entrySet()) {
 			String key = entry.getKey();
 			Double value = entry.getValue();
 
 			sb.append(key + ";" + value);
 			sb.append('\n');
 		}
 		
 		Utils.printToCSVFile(sb.toString(), inputFile.getParentFile().getAbsolutePath() + 
 				"/pagerank.csv");
 		
 		System.out.println("[Sanity check] Total sum (must be 1): "+sum);
 		System.out.println("Done!");
 	}
 }
 
 class ValueComparator implements Comparator<String> {
 
     Map<String, Double> base;
     public ValueComparator(Map<String, Double> base) {
         this.base = base;
     }
 
     public int compare(String a, String b) {
         if (base.get(a) >= base.get(b)) {
             return -1;
         } else {
             return 1;
         }
     }
 }
