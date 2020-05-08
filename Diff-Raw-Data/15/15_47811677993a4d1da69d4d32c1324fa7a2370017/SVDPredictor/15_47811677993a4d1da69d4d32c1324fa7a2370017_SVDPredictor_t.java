 package svd;
 
 import java.io.File;
 
 import tortuga.Predictor;
 
 import java.io.*;
 
import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.linalg.*;
 
 public class SVDPredictor implements Predictor{
 	/* instantiated by passing in an index file, has a float predictRating(int movieid, int userid) */
 	
 	private SingularValueDecomposition svd;
 	private int k;
 	private DoubleMatrix2D svdRecon;
 	
 	public SVDPredictor(File index, String svdFile, int reduce) throws IOException, ClassNotFoundException {
 		try{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(svdFile));
 			svd = (SingularValueDecomposition)ois.readObject();
 			
 			k = reduce;
 			
 			ois.close();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		Algebra A = new Algebra();
 		
 		DoubleMatrix2D Uk, Vprimek, Sk;
 		DoubleMatrix2D temp1, temp2; 
 		
 		Sk = svd.getS().viewPart(0, 0, k, k).copy();
 		
 		temp1 = svd.getU();
 		Uk = temp1.viewPart(0, 0, temp1.rows(), k).copy();
 		
 		temp1 = A.transpose(svd.getV());
 		Vprimek = temp1.viewPart(0, 0, k, temp1.columns()).copy();
 		
 		temp1 = A.mult(Uk, Sk);
 		temp2 = A.mult(Sk, Vprimek);
 		
 		svdRecon = A.mult(temp1, temp2);
 	}
 	
 	public float predictRating(int movieID, int userID){
		return (float)svdRecon.get(movieID, userID);
 	}
 }
