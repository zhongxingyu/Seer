 package page.rank.algorithm;
 
 import Jama.Matrix;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Set;
 
 public class MatrixFactory {
 
 	private PageFactory pageFactory;
 
 	public MatrixFactory(PageFactory pageFactory) {
 		this.pageFactory = pageFactory;
 	}
 
 	public Matrix createMatrixFromReader(BufferedReader bufferedReader) {
 		Matrix matrix = new MatrixReader(bufferedReader).readMatrix();
 
 		return matrix;
 	}
 
 	private class MatrixReader {
 		private Boolean nextLineExists = true;
 		private BufferedReader bufferedReader;
 
 		public MatrixReader(BufferedReader bufferedReader) {
 			this.bufferedReader = bufferedReader;
 		}
 		
 		private Matrix readMatrix() {
 			Integer numberOfPages = readNumberOfPages();
 			Matrix matrix = new Matrix(numberOfPages, numberOfPages);
 
 			for (int i = 0; i < numberOfPages; i++) {
 				if (hasNextLine()) {
 					Page page = pageFactory.createPageFromString(readLine());
 					Set<Link> links = page.getLinks();
 
 					for (Link link : links) {
						matrix.set(i, link.getTarget(), (double)link.getNumberOfLinks());
 					}
 				}
 			}
 
 			return matrix;
 		}
 
 		private Integer readNumberOfPages() {
 			return Integer.parseInt(readLine());
 		}
 
 		private String readLine() {
 			try {
 				String line = bufferedReader.readLine();
 				nextLineExists = line != null;
 				return line;
 			} catch (IOException ex) {
 				throw new CannotReadMatrix("Reading of line failed", ex);
 			}
 		}
 
 		private Boolean hasNextLine() {
 			return nextLineExists;
 		}
 	}
 }
