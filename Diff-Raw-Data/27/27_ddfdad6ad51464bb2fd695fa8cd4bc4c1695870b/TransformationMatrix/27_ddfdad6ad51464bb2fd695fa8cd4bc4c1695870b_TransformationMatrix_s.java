 package it.unito.geosummly;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
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
 	public ArrayList<Double> fillRow(ArrayList<Integer> occurrences, ArrayList<String> distincts, int cat_num, double lat, double lng) {
 		ArrayList<Double> row=new ArrayList<Double>();
 		row.add(lat); //lat and lng are in position 0 and 1
 		row.add(lng);
 		for(int i=0; i<this.map.size(); i++) {
 			row.add(0.0);
 		}
 		for(int i=0;i<distincts.size();i++){
 			int category_value=this.map.get(distincts.get(i)); //get the category corresponding to its occurrence value 
 			row.set(category_value, ((double) occurrences.get(i))/((double) cat_num)); //put the occurrence value in the "right" position
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
 	
 }
