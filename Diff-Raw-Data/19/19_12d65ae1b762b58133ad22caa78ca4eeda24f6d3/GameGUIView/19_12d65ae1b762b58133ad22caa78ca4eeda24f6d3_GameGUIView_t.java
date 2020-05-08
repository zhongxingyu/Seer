 package projectrts.view;
 
 import java.util.List;
 
 import projectrts.global.utils.ImageManager;
 import projectrts.model.IGame;
 import projectrts.model.entities.IAbility;
 import projectrts.model.entities.IPlayerControlledEntity;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.screen.Screen;
 
 public class GameGUIView {
 	private Nifty nifty;
 	private Screen screen;
 	private IGame game;
 	
 	private Element labelName;
 	private Element labelInfo;
 	private Element labelInfoValues;
 	private Element labelTime;
 	private Element labelPlayerInfo;
 	private Element panelInfo;
 	
 	private IPlayerControlledEntity selectedPce;
 	
 	
 	
 	public GameGUIView(Nifty nifty, IGame game){
 		this.nifty = nifty;
 		this.game = game;
 	}
 	
 	public void initialize(){
 		screen = nifty.getScreen("Screen_Game");
 		labelName = screen.findElementByName("Label_Name");
 		labelInfo = screen.findElementByName("Label_Info");
 		labelInfoValues = screen.findElementByName("Label_InfoValues");
 		labelTime = screen.findElementByName("Label_Time");
 		labelPlayerInfo = screen.findElementByName("Label_PlayerInfo");
 		panelInfo = screen.findElementByName("Panel_SelectedInfo");
 		
 		labelPlayerInfo.getRenderer(TextRenderer.class).setText("Resources: "+game.getPlayer().getResources());
 	}
 	
 	
     /**
      * Updates the view.
      * @param tpf The time passed since the last frame.
      */
     public void update(float tpf) {
     	int sec = (int)game.getGameTime();
     	
     	String output="Time: ";
     	if(sec/60>0){
     		output+=sec/60+":";
     	}
     	output += sec%60;
     	labelTime.getRenderer(TextRenderer.class).setText(output);
     }
     
     /**
 	 * Updates the abilities in the GUI
 	 * @param selectedEntities the abilities of the selected Entity
 	 */
 	public void updateSelected(IPlayerControlledEntity selectedPce){
     	
 		
     	this.selectedPce = selectedPce;
     	updateSelectedInfo();
    	List<IAbility> abilities = selectedPce.getAbilities();
     	
     	//Loops through every button and sets its attributes
     	for(int i = 0; i<8; i++){
     		Element button = screen.findElementByName("Button_Ability_" + (i+1));
   
     		if(button != null){
     			
 		    	if(abilities != null && i<abilities.size()){
 		    		IAbility ability = abilities.get(i);
 		    		//button.setVisibleToMouseEvents(true);
 		    		
 		    		NiftyImage image = ImageManager.INSTANCE.getImage(ability.getClass().getSimpleName());
 		    		if(image==null){
 		    			image = ImageManager.INSTANCE.getImage("NoImage");
 		    		}
 		    		
 		    		button.getRenderer(ImageRenderer.class).setImage(image);
 		    		button.setVisible(true);
 		    		
 		    	} else {
 		    		button.setVisible(false);
 		    	}
     		}
 
     	}
     }
 	
 	private void updateSelectedInfo(){
     	if(selectedPce!=null){
     		//Update the Info about the unit in the GUI
     		labelName.getRenderer(TextRenderer.class).setText(selectedPce.getName());
     		
     		StringBuilder infoValuesBuilder = new StringBuilder();
     		StringBuilder infoBuilder = new StringBuilder();
     		
     		infoBuilder.append("HP:");
     		infoValuesBuilder.append(selectedPce.getCurrentHealth()+"/"+selectedPce.getMaxHealth()+" ("+100*selectedPce.getCurrentHealth()/selectedPce.getMaxHealth()+"%)");
     		infoBuilder.append("\nDmg:");
     		infoValuesBuilder.append("\n"+	selectedPce.getDamage());
     		infoBuilder.append("\nSpeed:");
     		infoValuesBuilder.append("\n" + selectedPce.getSpeed());
     		infoBuilder.append("\nRange:");
     		infoValuesBuilder.append("\n" + selectedPce.getSightRange());
     		
     		labelInfoValues.getRenderer(TextRenderer.class).setText(infoValuesBuilder.toString());
     		labelInfo.getRenderer(TextRenderer.class).setText(infoBuilder.toString());
     		
     		panelInfo.setVisible(true);
     	} else {
     		panelInfo.setVisible(false);
     	}
 	}
 }
