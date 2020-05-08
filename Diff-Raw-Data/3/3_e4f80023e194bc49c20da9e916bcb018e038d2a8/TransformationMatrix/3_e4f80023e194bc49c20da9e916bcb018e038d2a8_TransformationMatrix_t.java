 package it.unito.geosummly;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 /**
  * @author Giacomo Falcone
  *
  * M*N Transformation matrix creation.
  * M is the number of bounding box cell.
  * N is the total number of categories found in the bounding box.
  * The cell C_ij, 0<i<M-1 and 0<j<N-1, contains the occurrence of the (N_j)th category for the (M_i)th cell,
  * normalized with respect to the M_i area and the total number of categories found in M_i 
  */
 public class TransformationMatrix {
 	private ArrayList<ArrayList<Double>> matrix; //data structure
 	private HashMap<String, Integer> map; //Map category to index
 	private ArrayList<String> header; //Sorted list of categories by column index 
 	
 	public static Logger logger = Logger.getLogger(TransformationMatrix.class.toString());
 	
 	public TransformationMatrix(){}
 	
 	public ArrayList<ArrayList<Double>> getMatrix() {
 		return matrix;
 	}
 	
 	public void setMatrix(ArrayList<ArrayList<Double>> matrix) {
 		this.matrix = matrix;
 	}
 	
 	public HashMap<String, Integer> getMap() {
 		return map;
 	}
 	
 	public void setMap(HashMap<String, Integer> map) {
 		this.map = map;
 	}
 	
 	public ArrayList<String> getHeader() {
 		return header;
 	}
 
 	public void setHeader(ArrayList<String> header) {
 		header.add("Latitude"); //First two values (columns) of the header have to be lat and lng
 		header.add("Longitude");
 		this.header = header;
 	}
 
 	public void addRow(ArrayList<Double> row) {
 		this.matrix.add(row);
 	}
 	
 	public String toString() {
 		String s= "Matrix Rows: "+matrix.size()+"\nMatrix Columns:"+matrix.get(0).size()+"\nAll categories frequencies:";
 		for(ArrayList<Double> r : matrix) {
 			for(Double d: r)
 				s+=d+", ";
 			s+="\n";
 		}
 		return s;
 	}
 	
 	//Update the hash map with new categories
 	public void updateMap(ArrayList<String> categories) {
 		for(String s: categories)
 			if(!this.map.containsKey(s)) {
 				this.map.put(s, this.map.size()+2); //first value in the map has to be 2
 				this.header.add(s);
 			}
 	}
 	
 	//Build a row of the matrix
 	public ArrayList<Double> fillRow(ArrayList<Integer> occurrences, ArrayList<String> distincts, int cat_num, double lat, double lng, double area) {
 		ArrayList<Double> row=new ArrayList<Double>();
 		row.add(lat); //lat and lng are in position 0 and 1
 		row.add(lng);
 		for(int i=0; i<this.map.size(); i++) {
 			row.add(0.0);
 		}
 		for(int i=0;i<distincts.size();i++){
 			int category_index=this.map.get(distincts.get(i)); //get the category corresponding to its occurrence value
 			double occ= (double) occurrences.get(i);
 			double num=(double) cat_num;
			row.set(category_index, (occ/num)/(area/1000)); //put the occurrence value (normalized with the corresponding cell area) in the "right" position
			                                                //normalize by Km    
 		}
 		return row;
 	}
 	
 	//Fix the row length to have rows with the same length value
 	public void fixRowsLength(int tot_num) {
 		for(ArrayList<Double> row: this.matrix)
 			for(int i=row.size();i<tot_num;i++) {
 				row.add(0.0);
 			}	
 	}
 	
 	//Normalize coordinate value in range [0,1]
 	public double normalizeCoordinate(double min, double max, double c) {
 		double norm_c=(c-min)/(max-min);
 		return norm_c;
 	}
 	
 }
