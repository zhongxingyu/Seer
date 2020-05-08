 package com.brendteneickstaedt.finalproject;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Map;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import com.mongodb.Mongo;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.MongoException;
 
 import cern.colt.matrix.DoubleMatrix1D;
 import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.impl.SparseDoubleMatrix1D;
 import cern.colt.matrix.impl.SparseDoubleMatrix2D;
 import cern.colt.matrix.linalg.Algebra;
 import cern.colt.matrix.linalg.EigenvalueDecomposition;
 
 public class Cluster {
 
 	public static double cosineSimularity(SparseDoubleMatrix1D c_i,
 			SparseDoubleMatrix1D c_j) {
 		if (c_i.cardinality() != 0 && c_j.cardinality() != 0) {
 			Algebra alg = new Algebra();
 			double i_dot_j = c_i.zDotProduct(c_j);
 			double norm_i = alg.norm1(c_i);
 			double norm_j = alg.norm1(c_j);
 			System.out.println("i dot j: " + i_dot_j + ", norm_i: " + norm_i
 					+ ", norm_j: " + norm_j);
 			return i_dot_j / norm_i * norm_j;
 		} else {
 			return 0.0;
 		}
 	}
 	
 
 	/**
 	 * @param args
 	 * 
 	 */
 	@SuppressWarnings(value = "unchecked")
 	public static void main(String[] args) {
 		
 		int m = 10;
 		double alpha = 0.01;
 		int k_min = 30;
 		int k_max = 45;
 		double tau = 0.4;
 
 	
 		DBCollection venue_coll, user_coll;
 		String affinityFilename = "affinity.ser";
 		String Dfilename = "a_diagonal.ser";
 		String LnormFilename = "l_norm.ser";
 		File f = null;
 		File g = null;
 		File h = null;
 		SparseDoubleMatrix2D A = null;
 		DoubleMatrix2D D = null;
 		EigenvalueDecomposition L_norm_eigen = null;
 		DoubleMatrix2D L = null;
 		DoubleMatrix2D L_norm = null;
 		DoubleMatrix1D k_smallest_eigen_vals = null;
 		DoubleMatrix2D k_smallest_eigen_vecs = null;
 		DoubleMatrix2D E = null;
 		FileInputStream fis = null;
 		ObjectInputStream in = null;
 		f = new File(affinityFilename);
 		g = new File(Dfilename);
 		h = new File(LnormFilename);
 		cern.jet.math.Functions F = cern.jet.math.Functions.functions;
 		Algebra alg = new Algebra();
 		
 		 if (!f.exists()) {
 			 // Create A
 			System.out.println("Creating A");
 			try {
 				// Load the collected and pre-processed venue and user data from
 				// MongoDB (see
 				// https://github.com/brendte/livehoods-twitter/tree/develop for
 				// this ruby code)
 				Mongo mongo = new Mongo("ds033157.mongolab.com", 33157);
 				DB db = mongo.getDB("heroku_app4504006");
 				char[] password = "a9cotq6m6dg4llvm3iut5hglag".toCharArray();
 				boolean auth = db.authenticate("heroku_app4504006", password);
 				System.out.println("auth: " + auth);
 				venue_coll = db.getCollection("philadelphia_grid");
 				user_coll = db.getCollection("philadelphia_users");
 				DBCursor venue_cur = venue_coll.find();
 				DBCursor user_cur = user_coll.find();
 				ArrayList<DBObject> venues = new ArrayList<DBObject>();
 				ArrayList<DBObject> users = new ArrayList<DBObject>();
 				for (DBObject venue_rec : venue_cur) {
 					venues.add(venue_rec);
 				}
 				venue_cur.close();
 				for (DBObject user_rec : user_cur) {
 					users.add(user_rec);
 				}
 				user_cur.close();
 				mongo.close();
 
 				// Build the final venue checkin vectors from MongoDB venue and
 				// user collections
 				int venueArraySize = venues.size();
 				int c_vSize = users.size();
 				System.out.println("venues: " + venueArraySize);
 				System.out.println("users: " + c_vSize);
 				ArrayList<SparseDoubleMatrix1D> venueCheckinVectors = new ArrayList<SparseDoubleMatrix1D>();
 				venueCheckinVectors.ensureCapacity(venueArraySize);
 				for (int i = 0; i <= venueArraySize; i++) {
 					venueCheckinVectors.add(null);
 				}
 				System.out.println(venueCheckinVectors.size());
 				for (DBObject venue : venues) {
 					int venueID = ((Integer) venue.get("box_id")).intValue();
 					SparseDoubleMatrix1D c_v = new SparseDoubleMatrix1D(c_vSize);
 					Map venueMap = venue.toMap();
 					Set<Map.Entry<String, Integer>> checkinBagMap = ((BasicDBObject) venueMap.get("check_in_bag")).toMap().entrySet();
 					for (Map.Entry<String, Integer> userCheckin : checkinBagMap) {
 						c_v.setQuick(
 								new Integer(userCheckin.getKey()).intValue(),
 								1.0 * userCheckin.getValue().intValue());
 					}
 					venueCheckinVectors.add(venueID, c_v);
 				}
 
 				// build affinity matrix A
 				A = new SparseDoubleMatrix2D(venueArraySize, venueArraySize);
 				for (int i = 1; i <= venueArraySize; i++) {
 					SparseDoubleMatrix1D c_i = venueCheckinVectors.get(i);
 					for (int j = 1; j <= venueArraySize; j++) {
 						SparseDoubleMatrix1D c_j = venueCheckinVectors.get(j);
 						// if necessary, add distance(i, j) calculation here
 						double affinityScore = Cluster.cosineSimularity(c_i,
 								c_j);
 						if (affinityScore != 0.0) {
 							affinityScore += alpha;
 							A.setQuick(i, j, affinityScore);
 						}
 						System.out.println("done: (" + i + "," + j
 								+ "). affinity score: " + affinityScore);
 					}
 				}
 				System.out.println("Cardinality of A: " + A.cardinality());
 				FileOutputStream fos = null;
 				ObjectOutputStream out = null;
 
 				fos = new FileOutputStream(f);
 				out = new ObjectOutputStream(fos);
 				out.writeObject(A);
 				out.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (MongoException e) {
 				e.printStackTrace();
 			}
 		} else if (!g.exists()) {
 			System.out.println("Creating D");
 			try {
 				fis = new FileInputStream(f);
 				in = new ObjectInputStream(fis);
 				A = (SparseDoubleMatrix2D) in.readObject();
 				in.close();
 				D = new SparseDoubleMatrix2D(A.rows(), A.columns());
 				for (int i = 0; i < A.viewRow(1).size() ; i++) {					
 					double degree = A.viewRow(i).zSum();
 					System.out.println("Row " + i + " degree: " + degree);
 					D.setQuick(i, i, degree);
 				}
//				A_eigen = new EigenvalueDecomposition(A);
//				D = A_eigen.getD();
 				
 				FileOutputStream fos = null;
 				ObjectOutputStream out = null;
 				fos = new FileOutputStream(g);
 				out = new ObjectOutputStream(fos);
 				out.writeObject(D);
 				out.close();
 				System.out.println("D cardinality: " + D.cardinality());
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}  else if (!h.exists())  { //f and g exist
 			System.out.println("Normalizing");
 			try {
 				fis = new FileInputStream(f);
 				in = new ObjectInputStream(fis);
 				A = (SparseDoubleMatrix2D) in.readObject();
 				in.close();
 				fis = new FileInputStream(g);
 				in = new ObjectInputStream(fis);
				D = (DoubleMatrix2D) in.readObject();
 				in.close();
 				
 				L = D.copy();
				System.out.println("L cardinality: " + L.cardinality());
 				L.assign(A, F.minus);
 				System.out.println("L cardinality: " + L.cardinality());
//				DoubleMatrix2D L_inv_sqrt = L.copy();
				L.assign(F.sqrt).assign(F.inv); // L is now L^-1/2
				L.zMult(D, L_norm);
				L_norm.zMult(L, L_norm);
 				 
 				// Store L_norm
 				FileOutputStream fos = null;
 				ObjectOutputStream out = null;
 				fos = new FileOutputStream(h);
 				out = new ObjectOutputStream(fos);
 				out.writeObject(L_norm);
 				out.close();
 				
 				System.out.println("L_norm cardinality: " + L_norm.cardinality());
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		} else { //f, g and h exist
 			try {
 				fis = new FileInputStream(f);
 				in = new ObjectInputStream(fis);
 				A = (SparseDoubleMatrix2D) in.readObject();
 				in.close();
 				fis = new FileInputStream(g);
 				in = new ObjectInputStream(fis);
 				D = (DoubleMatrix2D) in.readObject();
 				in.close();
 				fis = new FileInputStream(g);
 				in = new ObjectInputStream(fis);
 				L_norm = (DoubleMatrix2D) in.readObject();
 				in.close();
 				
 				L_norm_eigen = new EigenvalueDecomposition(L_norm); 
 				k_smallest_eigen_vals = L_norm_eigen.getRealEigenvalues().viewSorted().viewPart(0, k_max - 1);
 				k_smallest_eigen_vecs = L_norm_eigen.getV().viewSorted(0).viewPart(0, 0, k_max - 1, A.viewRow(1).size() - 1);
 				E = k_smallest_eigen_vecs.viewDice().copy();
 				
 				// Cluster E using k-means. Use  weka.clusterers.SimpleKMeans
 				// Output clusters to file
 				// Read each box_id (row id in E) for each cluster, and look-up lat/lng for that box in Mongo
 				// Plot each box using lat/lng on Google maps, with each box in a cluster having the same color, and no two clusters sharing the same color
 				 
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
