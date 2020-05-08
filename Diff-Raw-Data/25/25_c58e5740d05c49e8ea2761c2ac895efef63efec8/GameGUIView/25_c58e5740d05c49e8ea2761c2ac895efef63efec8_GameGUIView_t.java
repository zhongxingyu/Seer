 package projectrts.view;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import projectrts.io.ImageManager;
 import projectrts.model.IGame;
 import projectrts.model.abilities.IAbility;
 import projectrts.model.entities.IPlayerControlledEntity;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.tools.SizeValue;
 
 /**
  * A view class for the GUI
  * @author Filip Brynfors
  *
  */
 // TODO Afton: PMD: This class has too many methods, consider refactoring it.
 public class GameGUIView implements PropertyChangeListener {
 	private final Nifty nifty;
 	private Screen screen;
 	private final IGame game;
 	
 	private IPlayerControlledEntity selectedPce;
 	
 	private boolean activeMessage = false;
 	private float messageTimer = 0;
 	private static final float MESSAGE_MAX_TIME = 3;
 	private String message = ""; 
 	private Element labelMessage;
 	
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
 		game.getAbilityManager().setPropertyChangeLister(this);
 	}
 	
 	
 	/**
 	 * Initializes the view
 	 */
 	public void initialize(){
 		screen = nifty.getScreen("Screen_Game");
 		labelMessage = screen.findElementByName("Label_Message");
 				
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
     	showBuildInfo("");
     	updateAbilities();
     }
 	
 
 	@Override
 	public void propertyChange(PropertyChangeEvent pce) {
 		checkForEntityEvent(pce);
 		checkForBuildEvent(pce);
 		
 	}
 	
 	private void checkForEntityEvent(PropertyChangeEvent pce){
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
 	
	
 	private void checkForBuildEvent(PropertyChangeEvent pce){
 		if("NotEnoughResources".equals(pce.getPropertyName())){
 			showMessage("Not enough resources");
 		}else if("AlreadyTraining".equals(pce.getPropertyName())){
 			showMessage("That building is already training a unit");
 		}else if("BuildTimeLeft".equals(pce.getPropertyName())){
 			if(pce.getOldValue() == selectedPce){
 				showBuildInfo("Building structure\nTime left: " + pce.getNewValue());
 			}
 		}else if("TrainTimeLeft".equals(pce.getPropertyName())){
 			if(pce.getOldValue() == selectedPce){
 				showBuildInfo("Training Unit\nTime left: " + pce.getNewValue());
 			}
 		}else if("BuildCompleted".equals(pce.getPropertyName())){
 			if(pce.getOldValue() == selectedPce){
 				showBuildInfo("");
 			}
 		}
 	}
 	
 	/**
 	 * Shows the tooltip of the given ability, hides the tooltip of the given ability is null
 	 * @param ability shown ability
 	 */
 	public void showTooltip(IAbility ability){
 		Element panelTooltip = screen.findElementByName("Panel_Tooltip");
 		if(ability==null){
 			panelTooltip.hide();
 		} else {
 			Element labelTooltipHeader = screen.findElementByName("Label_TooltipHeader");
 			Element labelTooltipInfo = screen.findElementByName("Label_TooltipInfo");
 			panelTooltip.setVisible(true);
 			
 			panelTooltip.setConstraintX(new SizeValue(nifty.getNiftyMouse().getX()-panelTooltip.getWidth()+"px"));
 			panelTooltip.setConstraintY(new SizeValue(nifty.getNiftyMouse().getY()-panelTooltip.getHeight()+"px"));
 			
			//Update the screen so it moves the panel
 			screen.layoutLayers();
 			
 			labelTooltipHeader.getRenderer(TextRenderer.class).setText(ability.getName());
 			labelTooltipInfo.getRenderer(TextRenderer.class).setText(ability.getInfo());
 		}
 	}
 	
 	
 	private void updateTime(){
     	int sec = (int)game.getGameTime();
     	
     	StringBuffer buffer = new StringBuffer("Time: ");
     	if(sec/60>0){
     		buffer.append(sec/60);
     		buffer.append(':');
     	}
     	buffer.append(sec%60);
     	Element labelTime = screen.findElementByName("Label_Time");
     	labelTime.getRenderer(TextRenderer.class).setText(buffer.toString());
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
 		Element panelAbilities = screen.findElementByName("Panel_Abilities");
 		showTooltip(null);
 		if(selectedPce==null || !selectedPce.getOwner().equals(game.getHumanPlayer())){
 			panelAbilities.setVisible(false);
 		} else {
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
 		}
 	}
 	
 	private void updateSelectedInfo(){
 		Element panelInfo = screen.findElementByName("Panel_SelectedInfo");
     	if(selectedPce==null){
     		panelInfo.setVisible(false);
     	} else {
     		//Update the Info about the unit in the GUI
     		Element labelName = screen.findElementByName("Label_Name");
     		labelName.getRenderer(TextRenderer.class).setText(selectedPce.getName());
     		
    		
    		//Puts together strings with the info
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
     		
     		Element labelInfo = screen.findElementByName("Label_Info");
     		Element labelInfoValues = screen.findElementByName("Label_InfoValues");
     		labelInfoValues.getRenderer(TextRenderer.class).setText(infoValuesBuilder.toString());
     		labelInfo.getRenderer(TextRenderer.class).setText(infoBuilder.toString());
     		
     		panelInfo.setVisible(true);
     	}
 	}
 	
 	private void updatePlayerInfo(){
 		Element labelPlayerInfo = screen.findElementByName("Label_PlayerInfo");
 		labelPlayerInfo.getRenderer(TextRenderer.class).setText("Resources: "+game.getHumanPlayer().getResources());
 	}
 
 	private void showMessage(String message){
 		
 		labelMessage.getRenderer(TextRenderer.class).setText(message);
 		activeMessage = true;
 		labelMessage.setVisible(true);
 	}
 	
	//Shows the timer when constructing buildings or training units
 	private void showBuildInfo(String text){
 		Element buildInfoPanel = screen.findElementByName("Panel_BuildInfo");
 		
 		if(text == null || "".equals(text)){
 			buildInfoPanel.setVisible(false);
 		} else {		
 			buildInfoPanel.setVisible(true);
 			
 			Element buildTextPanel = screen.findElementByName("Label_BuildText");
 			buildTextPanel.getRenderer(TextRenderer.class).setText(text);
 
 		}
 	}
 }
