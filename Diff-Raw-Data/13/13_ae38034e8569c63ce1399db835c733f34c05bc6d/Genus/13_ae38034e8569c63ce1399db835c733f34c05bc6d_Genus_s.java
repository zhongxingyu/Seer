 package lichen.model;
 
 import ij.gui.Roi;
 
 import java.awt.Color;
 import java.util.ArrayList; 
 
 import javax.naming.NameNotFoundException;
 
 import lichen.controller.Processor;
 import lichen.view.MainGUI;
 
 /**
  * Singleton
  * Represents a list of Species
  * @author menzel
  */
 public class Genus { 
 
 	private ArrayList<Species> genus = new ArrayList<Species>();
 	private static Genus instance;
 	private int genusCount;
 	private double area = 40000;
 
 	/**
 	 * 
 	 */
 	private Genus() {
 	}
 
 	/**
 	 * Singelton 
 	 * @return instance
 	 */
 	public static Genus getInstance(){
 		if (instance == null){
 			instance = new Genus(); 
 		}
 		return instance;
 	}
 
 	/**
 	 * Push species on List
 	 * @param s  - species to be pushed
 	 */
 	public void push(Species s){
 		genus.add(s);
 	}
 
 	/**
 	 * Returns all species as array list
 	 * @return - list of all species
 	 */
 	public ArrayList<Species> returnAll(){
 		return genus;
 	}
 
 	/**
 	 * Returns species with the id
 	 * @param id - of the species to be returned
 	 * @return - species 
 	 * @throws NameNotFoundException if no species is found
 	 */
 	public Species getSpeciesFromID(int id) throws NameNotFoundException{
 		for(Species s: genus){
 			if(s.getId() == id){
 				return s;
 			}
 		}
 		throw new NameNotFoundException("No species found"); 
 	}
 
 
 	/**
 	 * Returns size of list
 	 * @return int size of list
 	 */
 	public int getSize(){
 		return genus.size();
 	}
 
 	public void setGenusCount(int geni) {
 		this.genusCount = geni; 
 	}
 
 	public int getGenusCount() {
 		return genusCount;
 	}
 
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Object[][] getResultTableData() { 
 
 		Object[][] t = new Object[this.getSize()][4] ;	
 
 		int index =0;
 
 		for(Species g: this.returnAll()){ 
 			
 			if(g.getResults() != null){
 				if(g.getResults().getArea() > 0){ 
 					t[index][0] = g.getId();
 					t[index][1] = g.getName();
 					t[index][2] =Math.round(g.getResults().getArea() *100.0)/100.0; 
 					t[index++][3] =Math.round((100/area)*g.getResults().getArea()*1000.0)/1000.0+" %"; 
 				}
 			}
 		} 
 
 		if(index == 0){ 
 			return null;
 		} 
 		return t; 
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public Object[][] getTableData() {
 		//TODO: Trennstriche zwischen Arten
 		
 		Object[][] t = new Object[(this.getSize()+this.getGenusCount())][5] ;	
 
 		int index =0;
 		String lastGenus = "";
 
 		for(Species g: this.returnAll()){
 			if(!lastGenus.equals(g.getGenus())){
 				t[index++][1] = "<html><b>" + g.getGenus() + "</html></b>";
 			}
 
 			lastGenus = g.getGenus();
 			t[index][0] = g.getId();
 			t[index++][1] = g.getName(); 
 		} 
 		return t;
 
 
 	}
 
 	/**
 	 * Prepears the data for export, returns a formatted 2D Array with the values measured.
 	 * Can be written to a file in csv format
 	 * @return Object [][] with data and names
 	 */
 	public Object[][] getExportResultTableData() {
 
 		Object[][] t = new Object[1000][7] ;	
 		//TODO: evaluate 500 as fixed lenght
 
 		int index =0;
 
		t[index][0] = "ID";
 		t[index][1] = "Gattung";
 		t[index][2] = "Art";
 		t[index][3] = "Fläche [mm^2]"; 
 		t[index][4] = "Fläche [%]"; 
 		t[index][5] = "Anzahl Thalli";
 		t[index++][6] = "Farbe in Analyse[RGB]";
 
 
 		index++;
		//TODO: display overview table at the top 
 		
 		
 		for(Species g: this.returnAll()){ 
 			if(g.getResults() != null){ 
 				
 				t[index][0] = g.getId();
 				t[index][1] = g.getName();
 				t[index][2] = g.getGenus();
 				t[index][3] =Double.toString(Math.round(g.getResults().getArea()*100.0)/100.0).replace(".", ","); 
 				t[index][4] =Double.toString(Math.round((100/area)*g.getResults().getArea()*1000.0)/1000.0).replace(".", ","); 
 				Color c = g.getResults().getColor();
 				t[index][5] = g.getResults().getThalliList().size();
 				t[index++][6] = "r=" + c.getRed() + "," + "g=" +c.getGreen() + "," + "b=" + c.getBlue();
 				
 			}
 		}
 		
 		index++; 
 		index++;
 		
 
 		for(Species g: this.returnAll()){ 
 			if(g.getResults() != null){
 
 
 				t[index][0] = g.getId();
 				t[index][1] = g.getName();
 				t[index][2] = g.getGenus();
 				t[index][3] =Double.toString(Math.round(g.getResults().getArea()*100.0)/100.0).replace(".", ","); 
 				t[index][4] =Double.toString(Math.round((100/area)*g.getResults().getArea()*1000.0)/1000.0).replace(".", ","); 
 				Color c = g.getResults().getColor();
 				t[index][5] = g.getResults().getThalliList().size();
 				t[index++][6] = "r=" + c.getRed() + "," + "g=" +c.getGreen() + "," + "b=" + c.getBlue();
 				
 				index++;
 				
 				
 				t[index][1] = "Thalli einzeln:";
 				t[index][2] = "Pos x:y";
 				t[index++][3] = "Größe [mm^2]";
 				
 				index++;
 				
 				for(String[] s: g.getResults().getThalliList()){
 					t[index][2] = s[0];
 					t[index++][3] = Double.toString(Math.round(Integer.parseInt(s[1])/g.getResults().getPixelrate()*1000.0)/1000.0).replace(".",",");
 				}
 				
 				index++;
 				
 
 			}
 		}
 
 		return t;
 	}
 
 	public boolean exists(int id) throws NameNotFoundException{
 		for(Species s: genus){
 			if(s.getId() == id){
 				return true;
 			}
 		}
 		throw new NameNotFoundException("No species found"); 
 	}
 
 	/**
 	 * Deletes references to measurements from each species
 	 */
 	public void reset() {
 		for(Species s: genus){
 			s.setResults(null); 
 		} 
 	} 
 
 	/**
 	 * Set Roi size for % calculation in results table
 	 * @param roi user definded ROI 
 	 */
 	public void setArea(Roi roi){ 
 		this.area =  roi.getBounds().width*roi.getBounds().height/MainGUI.getInstance().getMyProcessor().getPixelRate(null);
 
 	}
 
 	public double getArea() {
 		return this.area;
 	}
 }
