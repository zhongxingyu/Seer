 import java.awt.BorderLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 /**
  * Used as display view
  * @author John Frashier
  *
  */
 public class Graph extends JFrame{
 
 	/**
 	 * serial ID
 	 */
 	private static final long serialVersionUID = 748846064587327061L;
 
 	/**
 	 * type of graph to create
 	 */
 	private String typeOfGraph;
 	
 	/**
 	 * the x-axis label/variable name
 	 */
 	private String xLabel;
 	
 	/**
 	 * the y-axis label/variable name
 	 */
 	private String yLabel;
 	
 	/**
 	 * the title of the graph
 	 */
 	private String title;
 	
 	/**
 	 * the values of each bar
 	 */
 	private double[] valuesBar;
 	
 	/**
 	 * the names of each bar
 	 */
 	private String[] namesBar;
 	
 	/**
 	 * Author object used for calculating data for the author
 	 */
 	private Scholar scholar;
 	
 	/**
 	 * Map of all authors
 	 */
 	private HashMap<String, Scholar> scholars;
 	
 	/**
 	 * Default Graph shown at start of program
 	 */
 	public Graph(){
 		setTypeOfGraph("");
 		setXLabel("");
 		setYLabel("");
 		setTitle("");
 		setAuthor("");
 	}
 	
 	/**
 	 * Constructor for a Graph given the type of graph asked for by the user
 	 * @param type        type of graph to create
 	 */
 	public Graph(String type){
 		
 		setTypeOfGraph(type);
 	}
 	
 	/**
 	 * Constructor for a Graph given the type of graph asked for by the user
 	 * @param type        type of graph to create
 	 */
 	public Graph(String type, String authorName, HashMap<String, Scholar> scholars){
 		setScholars(scholars);
 		setAuthor(authorName);
 		setTypeOfGraph(type);
 	}
 	
 	
 	/**
 	 * Displays the desired graph in a GUI.
 	 */
 	public void displayGraph(){
 		//GUI creation here
 		JFrame f = new JFrame();
 	    f.setSize(800, 500);
 	    JLabel xLabel = new JLabel(getXLabel());
 	    JLabel yLabel = new JLabel(getYLabel());
 	    yLabel.setHorizontalAlignment(SwingConstants.CENTER);
 	    f.add(xLabel, BorderLayout.WEST);
 	    f.add(yLabel, BorderLayout.SOUTH);
 	    f.getContentPane().add(new ChartPanel(this.valuesBar, this.namesBar, this.title), BorderLayout.CENTER);
 
 	    WindowListener wndCloser = new WindowAdapter() {
 	      public void windowClosing(WindowEvent e) {
 	        System.exit(0);
 	      }
 	    };
 	    f.addWindowListener(wndCloser);
 	    f.setVisible(true);
 	}
 
 
 	//Accessors for class variables
 	public String getTypeOfGraph() {
 		return typeOfGraph;
 	}
 
 	//Sets the variables used to create each graph given the type of graph
 	public void setTypeOfGraph(String typeOfGraph) {
 		this.typeOfGraph = typeOfGraph;
 		ArrayList<Publication> pub = getScholar().getPublishedPapers();
 		
 		if(typeOfGraph.equals("TP")){
 			setXLabel("Number of Publications");
 			setYLabel("Type of Publication");
 			setTitle("Number of Each Type of Publication by " + getScholar().getName().getNameFull());
 			double[] values = new double[2];
 		    String[] names = new String[2];
 		    values[0] = Stats.NumCPs(pub);
 		    names[0] = "Conference Paper";
 		    values[1] = Stats.NumJournals(pub);
 		    names[1] = "Journal Article";
 		    setValuesBar(values);
 		    setNamesBar(names);
 		}
 		else if(typeOfGraph=="PY"){
 			setXLabel("Number of Publications");
 			setYLabel("Year");
 			setTitle("Number of Publications Each Year by " + getScholar().getName().getNameFirst() + " " + getScholar().getName().getNameLast());
 			ArrayList<Publication> cPub = getScholar().getPublishedPapers();
 			
 			HashMap<String, Integer> years = Stats.NumOfYears(cPub);
 			
 		    String[] names = new String[years.size()];
 		    
 		    double[] values = new double[years.size()];
 		    
 		    int index = 0;
 		    for (Integer value : years.values()) {
 		    	values[index] = value;
 		    	index++;
 		    }
 		    
 		    index = 0;
 		    for (String value : years.keySet()) {
 		    	names[index] = value;
 		    	index++;
 		    }
 		    
 		    setValuesBar(values);
 		    setNamesBar(names);
 		}
 		else if(typeOfGraph=="CPY"){
 			setXLabel("Number of Conference Papers");
 			setYLabel("Year");
 			setTitle("Number of Conference Papers Each Year by " + getScholar().getName().getNameFirst() + " " + getScholar().getName().getNameLast());
 			ArrayList<Publication> jPub = getScholar().getPublishedPapers();
 			HashMap<String, Integer> years = Stats.NumOfCPYears(jPub);
 			
 			String[] names = new String[years.size()];
 			    
 		    double[] values = new double[years.size()];
 		    
 		    int index = 0;
 		    for (Integer value : years.values()) {
 		    	values[index] = value;
 		    	index++;
 		    }
 		    
 		    index = 0;
 		    for (String value : years.keySet()) {
 		    	names[index] = value;
 		    	index++;
 		    }
 		    
 		    setValuesBar(values);
 		    setNamesBar(names);
 		}
 		else if(typeOfGraph=="JAY"){
 			setXLabel("Number of Journal Articles");
 			setYLabel("Year");
 			setTitle("Number of Journal Articles Each Year by " + getScholar().getName().getNameFirst() + " " + getScholar().getName().getNameLast());
 			ArrayList<Publication> jPub = getScholar().getPublishedPapers();
 			HashMap<String, Integer> years = Stats.NumOfJounalYears(jPub);
 			
 			String[] names = new String[years.size()];
 			
 		    double[] values = new double[years.size()];
 		    
 		    int index = 0;
 		    for (Integer value : years.values()) {
 		    	values[index] = value;
 		    	index++;
 		    }
 		    
 		    index = 0;
 		    for (String value : years.keySet()) {
 		    	names[index] = value;
 		    	index++;
 		    }
 		    
 		    setValuesBar(values);
 		    setNamesBar(names);
		    
 		}
 		else if(typeOfGraph=="NC"){
 			setXLabel("Number of Times");
 			setYLabel("Number of Co Authors");
 			setTitle("Number of Co Authors per Publication by " + getScholar().getName().getNameFirst() + " " + getScholar().getName().getNameLast());
 			
 			HashMap<String, Integer> coAuthors = Stats.NumCoAuthors(pub);
 			
 			double[] values = new double[coAuthors.size()];
 		    //String[] names = new String[coAuthors.size()];
 		    double[] tempValues = new double[coAuthors.size()];
 		    
 		    int index = 0;
 		    for (Integer value : coAuthors.values()) {
 		    	tempValues[index] = value;
 		    	index++;
 		    }
 		    Arrays.sort(tempValues);
 		    int size = (int) tempValues[tempValues.length -1];
 		    String[] names = new String[size+1];
 		    values = new double[size+1];
 		    names[0] = "0";
 		    for(int k=1; k<=size;k++){
 	        	names[k] = String.valueOf(Integer.valueOf(names[k-1])+1);
 	        }
 		    
 		    for(String n: names){
 		    	double nDbl = (double) Integer.valueOf(n);
 		    	for(double t: tempValues){
 		    		if(t==nDbl){
 		    			values[(int) nDbl]++;
 		    		}
 		    	}
 	        }
 		    
 	        setValuesBar(values);
 	        setNamesBar(names);
				
 			
 			
 			/*HashMap<String, Integer> coAuthors = Stats.NumCoAuthors(pub);
 			
 			double[] values = new double[coAuthors.size()];
 		    String[] names = new String[coAuthors.size()];
 
 
 		    int index = 0;
 		    for (Integer value : coAuthors.values()) {
 		    	values[index] = value;
 		    	index++;
 		    }
 		    
 		    index = 0;
 		    for (String value : coAuthors.keySet()) {
 		    	names[index] = value;
 		    	index++;
 		    }
 		    
 		    setValuesBar(values);
 		    setNamesBar(names);	
 		    */		
 		}
 		
 	}
 
 	public String getXLabel() {
 		return xLabel;
 	}
 
 	public void setXLabel(String xLabel) {
 		this.xLabel = xLabel;
 	}
 
 	public String getYLabel() {
 		return yLabel;
 	}
 
 	public void setYLabel(String yLabel) {
 		this.yLabel = yLabel;
 	}
 	
 	public String getTitle() {
 		return yLabel;
 	}
 
 	public void setTitle(String title){
 		this.title = title;
 	}
 	
 	public String getAuthorName() {
 		return yLabel;
 	}
 
 	public double[] getValuesBar() {
 		return valuesBar;
 	}
 
 	public void setValuesBar(double[] values) {
 		this.valuesBar = values;
 	}
 
 	public String[] getNamesBar() {
 		return namesBar;
 	}
 
 	public void setNamesBar(String[] namesBar) {
 		this.namesBar = namesBar;
 	}
 
 	public Scholar getScholar() {
 		return scholar;
 	}
 	
 	public void setAuthor(String scholarName){
 		scholar = scholars.get(scholarName);
 	}
 
 	public HashMap<String, Scholar> getScholars() {
 		return scholars;
 	}
 
 	public void setScholars(HashMap<String, Scholar> scholars) {
 		this.scholars = scholars;
 	}
 }
