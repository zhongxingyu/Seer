 package simulator;
 
 /**
  * GameInit class bootstraps the entire game.
  * 
  * It creates the Plant object and populates it
  * with the system model defined in the JSON config file.
  * It also instantiates the GUI (Interface and gives it  
  * a reference to the presenter. 
  * The presenter is given a reference to the Plant (Model)
  * 
  * @author WillFrew
  *
  */
 public class GameInit {
 	
 	private Plant model;
 	private TextUI view;
 	private PlantPresenter presenter; 
 	
 	public GameInit() {
 		model = new Plant();
 		presenter = new PlantPresenter(model);
		view = new TextUI(presenter);
 	}
 	
 	public static void main(String[] args) {
 		GameInit game = new GameInit();
 	}
 	
 }
