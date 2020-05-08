 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import org.jblas.DoubleMatrix;
 import org.jblas.MatrixFunctions;
 
 /*
  * Matrix methods and implementation of the SNPrank algorithm.
  * Authors:  Brett McKinney and Nick Davis
  * Email:  brett.mckinney@gmail.com, nick@nickdavis.name
  */
 public class SNPrankData {
 	private String [] header;
 	private DoubleMatrix data;
 	
 	public SNPrankData(String file) {
 		header = null;
 		data = null;
 		readFile(file);
 	}
 	
 	private void readFile(String filename) {
 		try {
 			FileReader fr = new FileReader(filename);
 			BufferedReader br = new BufferedReader(fr);
 
 			// strRow is used to read line from file
 			String delimiter = "";
 			String strRow = br.readLine();
 			if(strRow.indexOf(',')>=0) {
 				delimiter = "\\,";
 			}else {
 				delimiter = "\\s";
 			}
 			//set the header from first line of the input file
 			this.setHeader(strRow.split(delimiter));
 			
 			data = new DoubleMatrix(this.getHeader().length, this.getHeader().length);
 
 			// set the data part from other lines of the input file
 			int index = 0;
 			while ((strRow = br.readLine()) != null) {
 				if(strRow.indexOf(',')>=0) {
 					delimiter = "\\,";
 				}else {
 					delimiter = "\\s";
 				}
 				String [] strArray = strRow.trim().split(delimiter);
 				for(int i = 0; i < data.rows; i++) {
 					data.put(index, i, Double.parseDouble(strArray[i].trim()));
 				}
 				index++;
 			}
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 	}
 	
 	public void snprank(String[] name, DoubleMatrix G, String gamma, String outFile) {
 		// G diagonal is information gain
 		DoubleMatrix Gdiag = G.diag();		
 		
 		double Gtrace = Gdiag.sum();
 		
 		// vector of column sums of G
 		DoubleMatrix colsum = G.columnSums();
 		
 		// find the indices of non-zero c array elements
 		int[] colsum_nzidx = colsum.findIndices();
 		
 		// sparse matrix where the nonzero indices are filled with 1/colsum
 		DoubleMatrix D = DoubleMatrix.zeros(G.rows, G.columns);
 		DoubleMatrix fillD = DoubleMatrix.ones(1, colsum_nzidx.length)
 								.div(colsum.get(colsum_nzidx));
 		for (int i = 0; i < fillD.length; i++){
 			D.put(colsum_nzidx[i], colsum_nzidx[i], fillD.get(i));
 		}		
 		
 		// non-zero elements of colsum/d_j have (1 - gamma) in the numerator of 
 		// the second term (Eq. 5 from SNPrank paper)
 		DoubleMatrix T_nz = DoubleMatrix.ones(1, G.columns);
 		T_nz.put(colsum_nzidx, 1 - Double.parseDouble(gamma));
 		
 		// Compute T, Markov chain transition matrix
 		DoubleMatrix T = G.mmul(D).mul(Double.parseDouble(gamma))
 						.add(Gdiag.mmul(T_nz).mul(1.0 / Gtrace));
 
 		// initial arbitrary vector 
 		DoubleMatrix r = new DoubleMatrix(G.rows, 1);
 		r.fill(1.0 / G.rows);
 		
 		double threshold = 1.0E-4;
 		double lambda = 0.0;
 		boolean converged = false;
 		DoubleMatrix r_old = r;
 
 		// if the absolute value of the difference between old and current r 
 		// vector is < threshold, we have converged
 		while(!converged) {
 			r_old = r;
 			r = T.mmul(r);
 			
 			// sum of r elements
 			lambda = r.sum();
 			
 			// normalize eigenvector r so sum(r) == 1
 			r = r.div(lambda);
 			
 			// check convergence, ensure all elements of r - r_old < threshold
 			if (MatrixFunctions.abs(r.sub(r_old)).lt(threshold).min() == 1.0){
 				converged = true;
 			}
 			
 			else converged = false;
 		}
 
 		// location of indices, used for sorting 
 		Integer[] indices = new Integer[r.length];
 		for (int i = 0; i < indices.length; i++){
 			indices[i] = i;
 		}
 
 		final double[][] r_data = r.toArray2();
 		
 		// sort r, preserving sorted order of original indices		
 		Arrays.sort(indices, new Comparator<Integer>() {
 		    public int compare(final Integer o1, final Integer o2) {
 		        return Double.compare(r_data[o1][0], r_data[o2][0]);
 		    }
 		});
 		
 		// reverse sorted list of indices (sort descending)
 		for (int i = 0; i < indices.length / 2; i++){
 			int current = indices[i];
 			indices[i] = indices[indices.length - 1 - i];
 			indices[indices.length - 1 - i] = current;
 		}
 
 		// output to file, truncating values to 6 decimal places
         try {
 			FileWriter fw = new FileWriter(outFile);
 			BufferedWriter writer = new BufferedWriter(fw);
 			writer.write("SNP\tSNPrank\tIG\n");
 			int index = 0;
 			for(int i=0; i<r.length; i++) {
 				index = indices[i];
 				writer.write(name[index] + "\t" + 
 						String.format("%8.6f", r.get(index)) + "\t" +
						String.format("%8.6f", colsum.get(index)) + "\n");
 			}
 			
 			writer.close();
 			fw.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * @return the header
 	 */
 	public String[] getHeader() {
 		return header;
 	}
 	/**
 	 * @param header the header to set
 	 */
 	public void setHeader(String[] header) {
 		this.header = header;
 	}
 	/**
 	 * @return the data
 	 */
 	public DoubleMatrix getData() {
 		return data;
 	}
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(DoubleMatrix data) {
 		this.data = data;
 	}
 }
