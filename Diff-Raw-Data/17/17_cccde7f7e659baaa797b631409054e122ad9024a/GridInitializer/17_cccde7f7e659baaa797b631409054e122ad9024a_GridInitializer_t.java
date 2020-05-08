 package grid;
 
 /**
  * TODO: 	Later zullen we van onze GridBuilder een echte builder maken
  * 			en dit wordt de Director-klasse.
  * 
  * @author 	Group 8
  * @version	April 2013
  */
 public class GridInitializer {
 	
 	private static final GridInitializer instance = new GridInitializer();
 	
 	private GridBuilder gridBuilder;
 	
 	public static GridInitializer getInstance()  { 
 		return instance;
 	}
 	
 	public GridBuilder getGridBuilder() {
 		return gridBuilder;
 	}
 	
 	public void setGridBuilder(GridBuilder gridBuilder) {
 		this.gridBuilder = gridBuilder;
 	}
 	
 	
 	// TODO: verwijderen
 	
 	
 	
 }
