 package projectrts.view;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import projectrts.io.ImageManager;
 import projectrts.model.IGame;
 import projectrts.model.abilities.IAbility;
 import projectrts.model.entities.IEntity;
 import projectrts.model.entities.IPlayerControlledEntity;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.screen.Screen;
 
 /**
  * A view class for the GUI
  * @author Filip Brynfors
  *
  */
 public class GameGUIView implements PropertyChangeListener {
 	private Nifty nifty;
 	private Screen screen;
 	private IGame game;
 	
 	private Element labelName;
 	private Element labelInfo;
 	private Element labelInfoValues;
 	private Element labelTime;
 	private Element labelPlayerInfo;
 	private Element panelInfo;
 	private Element panelAbilities;
 	private Element labelMessage;
 	
 	private IPlayerControlledEntity selectedPce;
 	
 	private boolean activeMessage = false;
 	private float messageTimer = 0;
 	private final float MESSAGE_MAX_TIME = 3;
 	private String message = ""; 
 	
 	
 	/**
 	 * Creates a new view
 	 * @param nifty the nifty GUI object
 	 * @param game the model of the game
 	 */
 	public GameGUIView(Nifty nifty, IGame game){
 		this.nifty = nifty;
 		this.game = game;
 		
 		game.getHumanPlayer().addListener(this);
 		game.getEntityManager().addListener(this);
 	}
 	
 	
 	/**
 	 * Initializes the view
 	 */
 	public void initialize(){
 		screen = nifty.getScreen("Screen_Game");
 		labelName = screen.findElementByName("Label_Name");
 		labelInfo = screen.findElementByName("Label_Info");
 		labelInfoValues = screen.findElementByName("Label_InfoValues");
 		labelTime = screen.findElementByName("Label_Time");
 		labelPlayerInfo = screen.findElementByName("Label_PlayerInfo");
 		panelInfo = screen.findElementByName("Panel_SelectedInfo");
 		labelMessage = screen.findElementByName("Label_Message");
 		panelAbilities = screen.findElementByName("Panel_Abilities");
 		
 		updatePlayerInfo();
 	}
 	
 	
     /**
      * Updates the view.
      * @param tpf The time passed since the last frame.
      */
     public void update(float tpf) {
 
     	updateTime();
     	updateSelectedInfo();
     	updateMessage(tpf);
     }
     
     /**
 	 * Updates the abilities in the GUI
 	 * @param selectedEntities the abilities of the selected Entity
 	 */
 	public void updateSelected(IPlayerControlledEntity selectedPce){
     	this.selectedPce = selectedPce;
     	updateSelectedInfo();
     	updateAbilities();
     }
 	
 	private void updateTime(){
     	int sec = (int)game.getGameTime();
     	
     	String output="Time: ";
     	if(sec/60>0){
     		output+=sec/60+":";
     	}
     	output += sec%60;
     	labelTime.getRenderer(TextRenderer.class).setText(output);
 	}
 	
 	private void updateMessage(float tpf){
 		if(activeMessage){
 			messageTimer+=tpf;
 			if(messageTimer >= MESSAGE_MAX_TIME){
 				activeMessage = false;
 				messageTimer = 0;
 				labelMessage.setVisible(false);
 			}
 			
 		}
 	}
 	
 	private void updateAbilities(){
 		if(selectedPce!=null){
 			panelAbilities.setVisible(true);
 	    	List<IAbility> abilities = game.getAbilityManager().getAbilities(selectedPce);
 	    	
 	    	//Loops through every button and sets its attributes
 	    	for(int i = 0; i<8; i++){
 	    		Element button = screen.findElementByName("Button_Ability_" + (i+1));
 	  
 	    		if(button != null){
 	    			
 			    	if(abilities != null && i<abilities.size()){
 			    		IAbility ability = abilities.get(i);
 			    		
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
 	    	
 		} else {
 			panelAbilities.setVisible(false);
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
 	
 	private void updatePlayerInfo(){
 		labelPlayerInfo.getRenderer(TextRenderer.class).setText("Resources: "+game.getHumanPlayer().getResources());
 	}
 
 	private void showMessage(String message){
 		labelMessage.getRenderer(TextRenderer.class).setText(message);
 		activeMessage = true;
 		labelMessage.setVisible(true);
 	}
 	
 	@Override
 	public void propertyChange(PropertyChangeEvent pce) {
 		if("ResourceChange".equals(pce.getPropertyName())){
 			updatePlayerInfo();
 		} else if ("ShowMessage".equals(pce.getPropertyName())){
 			message = pce.getNewValue().toString();
 			showMessage(message);
 			
 		}else if (pce.getPropertyName().equals("entityRemoved")) {
			if(pce.getOldValue()==selectedPce ) {
 				selectedPce=null;
 				updateSelected(null);
 				
 			}
 		}else if("TargetNotResource".equals(pce.getPropertyName())){
 			showMessage("Target is invalid, must target a Resource");
 		}else if("TargetNotPCE".equals(pce.getPropertyName())){
 			showMessage("Target is invalid, must target a Unit or Structure");
 		}
 		
 	}
 }
