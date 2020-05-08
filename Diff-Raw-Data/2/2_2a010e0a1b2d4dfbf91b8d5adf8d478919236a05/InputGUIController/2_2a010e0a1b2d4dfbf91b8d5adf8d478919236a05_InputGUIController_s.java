 package projectrts.controller;
 
 import java.util.List;
 
 import projectrts.global.utils.ImageManager;
 import projectrts.model.entities.IAbility;
 import projectrts.model.entities.IEntity;
 import projectrts.model.entities.IPlayerControlledEntity;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.niftygui.NiftyJmeDisplay;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 /**
  * A controller class that handles input from the gui
  * @author Filip Brynfors
  *
  */
 public class InputGUIController implements ScreenController {
 	private Nifty nifty;
 	private Screen screen;
 	
 	private InputController input;
 	private ScreenController sc;
 	private int i;
 	private SimpleApplication app;
 	
 	private List<IAbility> abilities; 
 
 	
 	public InputGUIController(Application app, InputController input) {
 		sc = this;
 		this.app = (SimpleApplication) app;
 		initializeGUI();
 		this.input = input;
 		input.setGUIControl(this);
 	}
 	
 	private void initializeGUI() {
 		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
 	            app.getAssetManager(), app.getInputManager(), app.getAudioRenderer(), app.getGuiViewPort());
 	    nifty = niftyDisplay.getNifty();
 	    app.getGuiViewPort().addProcessor(niftyDisplay);
 	    app.getFlyByCamera().setDragToRotate(true);
 	 
 	    nifty.loadStyleFile("nifty-default-styles.xml");
 	    nifty.loadControlFile("nifty-default-controls.xml");
 	 
 	    ImageManager.INSTANCE.initializeImages(nifty);
 	    
 	    // <screen>
 	    nifty.addScreen("Screen_ID", new ScreenBuilder("GUI Screen"){{
 	        controller(sc); // Screen properties       
 	 
 	        // <layer>
 	        layer(new LayerBuilder("Layer_ID") {{
 	            childLayoutVertical(); // layer properties, add more...
 
 	            
 	            
 	            panel(new PanelBuilder("panel_main") {{
 	                childLayoutVertical();
 	                backgroundColor("#0000");
 	                height("80%");
 
 	                // <!-- spacer -->
 	            }});
 	            
 	 
 	            // <panel>
 	            panel(new PanelBuilder("Panel_GUI") {{
 	               childLayoutHorizontal(); // panel properties, add more...  
 	               backgroundColor("#f00f"); 
 		           height("20%");
 		           visibleToMouse(true);
 		           
 		           
 	               panel(new PanelBuilder("Panel_Main"){{
 	            	   width("60%");
 	            	   childLayoutVertical();
 	            	   
 	               }});
 		           
 	               panel(new PanelBuilder("Panel_Abilities"){{
 	            	   width("40%");
 	            	   childLayoutVertical();
 	    
 	 
 	            	   //First row with buttons
 		               panel(new PanelBuilder("Panel_Abilities_Row1"){{
 		            	   height("50%");
 		            	   childLayoutHorizontal();
 		            	   
 		            	   for(i = 1; i<=4; i++){
 				                // GUI elements
 				                control(new ButtonBuilder("Button_Ability_" + i){{
 				                    width("25%");
 				                    height("100%");
 				                    visible(false);
 				                    focusable(false);
 				                    interactOnClick("buttonClicked("+i+")");
 				                }});
 			                
 		            	   }
 		
 		               }});    
 		               
 		               
 		               
 		               //Second row with buttons
 		               panel(new PanelBuilder("Panel_Abilities_Row2"){{
 		            	   height("50%");
 		            	   childLayoutHorizontal();
 			                
 		            	   for(i = 5; i<=8; i++){
 				                // GUI elements
 				                control(new ButtonBuilder("Button_Ability_" + i){{
 				                    width("25%");
 				                    height("100%");
 				                    visible(false);
 				                    focusable(false);
 				                    interactOnClick("buttonClicked("+i+")");
 				                }});
 			                
 		            	   }
 	                
 		               }});
 	               }});
 	 
 	            }});
 	            // </panel>
 	          }});
 	        // </layer>
 	      }}.build(nifty));
 	    // </screen>
 	 
 	    nifty.gotoScreen("Screen_ID"); // start the screen
 		
 	}
 	
 	public void updateAbilities(List<IEntity> selectedEntities){
     	Screen screen = nifty.getScreen("Screen_ID");
     	
     	boolean oneIsSelected = selectedEntities.size()==1;
     	abilities = null;
     
     	
     	if(oneIsSelected && selectedEntities.get(0) instanceof IPlayerControlledEntity){
     		IPlayerControlledEntity pce = (IPlayerControlledEntity) selectedEntities.get(0);
     		abilities = pce.getAbilities();
     	}
     	
     	//Loops through every button and sets its attributes
     	for(int i = 0; i<8; i++){
     		Element button = screen.findElementByName("Button_Ability_" + (i+1));
   
     		if(button != null){
     			
 		    	if(abilities != null && i<abilities.size()){
 		    		IAbility ability = abilities.get(i);
 		    		//button.setVisibleToMouseEvents(true);
 		    		
		    		NiftyImage image = ImageManager.INSTANCE.getImage(ability.getName());
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
 
 	@Override
 	public void bind(Nifty nifty, Screen screen) {
 		this.nifty = nifty;
 		this.screen = screen;
 		
 	}
 
 	@Override
 	public void onEndScreen() {
 		
 	}
 
 	@Override
 	public void onStartScreen() {
 		
 	}
 	
 	public void buttonClicked(String nr) {
 		try {
 			
 			int iNr = Integer.parseInt(nr);
 			
 			if(iNr-1<abilities.size()){
 				input.selectAbility(abilities.get(iNr-1));
 			}
 			
 			
 		} catch (NumberFormatException e){
 			
 		}
 		
 		
 	}
 
 }
