 package projectrts.controller;
 
 import java.util.List;
 
 import projectrts.global.utils.ImageManager;
 import projectrts.model.entities.IAbility;
 import projectrts.model.entities.IEntity;
 import projectrts.model.entities.IPlayerControlledEntity;
 import projectrts.view.GameGUIView;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 /**
  * A controller class that handles input from the gui
  * @author Filip Brynfors Modified by Jakob Svensson
  *
  */
 public class InputGUIController implements ScreenController {
 	private Nifty nifty;
 	private Screen screen;
 	private GameGUIView guiView;
 	
 	private InputController input;
 	
 	private IPlayerControlledEntity selectedPce;
 
 	/**
 	 * Creates a new inputGUIController
 	 * @param app the application
 	 * @param input the inputController
 	 * @param nifty the nifty
 	 */
 	public InputGUIController(InputController input, Nifty nifty, GameGUIView guiView) {
 		this.input = input;
 		this.nifty = nifty;
 		this.guiView = guiView;
 		
 		initializeGUI();
 		input.setGUIControl(this);
 	}
 	
 	private void initializeGUI() {
 	    
 	    // <screen>
 	    nifty.addScreen("Screen_Game", new ScreenBuilder("GUI Screen"){{
	        controller(InputGUIController.this); // Screen properties       
 	 
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
 	    	        visibleToMouse(true);
 	    	           
 	                panel(createLeftPanel());
 	                
 	                
 	                panel(createMiddlePanel()); 
 	                
 	                panel(createAbilityPanel());
 	                
 	            }});
 	            // </panel>
 	          }});
 	        // </layer>
 	      }}.build(nifty));
 	    // </screen>
 	    
 	    Screen screen = nifty.getScreen("Screen_Game");
 	    Element guiPanel = screen.findElementByName("Panel_GUI");
 	    NiftyImage image = ImageManager.INSTANCE.getImage("GUIBackground");
 	    guiPanel.getRenderer(ImageRenderer.class).setImage(image);
 	    
 		Element panelInfo = screen.findElementByName("Panel_SelectedInfo");
 		
 		panelInfo.setVisible(false);
 		
 	    nifty.gotoScreen("Screen_Game"); // start the screen
 
 	}
 	
 	//Creates the panel that shows the player info
 	private PanelBuilder createLeftPanel(){
 		PanelBuilder builder = new PanelBuilder("Panel_PlayerInfo"){{
 			width("40%");
 			childLayoutVertical();
 			
 			control(new LabelBuilder("Label_Time"){{
 				width("100%");
 				textHAlignLeft();
 				textVAlignTop();
 				color("#0F0F");
 				
 			}});
 			
 			control(new LabelBuilder("Label_PlayerInfo"){{
 				width("100%");
 				textHAlignLeft();
 				textVAlignTop();
 				color("#0F0F");
 				
 			}});
 			
 		}};
 		return builder;
 	}
 	
 	//Creates the panel that shows current hp
 	private PanelBuilder createMiddlePanel(){
 		PanelBuilder builder = new PanelBuilder("Panel_SelectedInfo"){{
 			width("20%");
 			childLayoutVertical();
 			backgroundColor("#000F");
 			
 			//TODO: Afton: Remove or use this testing code
 
 			/*
 			panel(new PanelBuilder("Filler") {{
 				height("10px");
 				
 			}});
 			
     		
     		//labelName.getRenderer(TextRenderer.class).setTextMinHeight(new SizeValue("40px"));
     		//labelName.getRenderer(TextRenderer.class).setTextLineHeight(new SizeValue("40px"));
     		//labelName.getRenderer(TextRenderer.class)
 			
 			text(new TextBuilder() {{
 				font("aurulent-sans-16.fnt");
 				color("#f00f");
 				text("Hello World!");
 				
 				alignCenter();
 				valignCenter();
 			}});
 			*/
 			
 			
 			control(new LabelBuilder("Label_Name"){{
 				//height("50px");
 				width("100%");
 				color("#00FF");
 
 				//TODO: Afton, fix text size
 				/*
 				onActiveEffect(new EffectBuilder("textSize") {{
 					//effectParameter("", "10px");
 				}});
 				*/
 				
 			}});
 			
 
 			panel(new PanelBuilder("Panel_Info"){{
 				childLayoutHorizontal();
 				
 				control(new LabelBuilder("Label_Info"){{
 					width("30%");
 					height("100%");
 					textHAlignLeft();
 					textVAlignTop();
 					color("#0F0F");
 					
 				}});
 				
 				control(new LabelBuilder("Label_InfoValues"){{
 					width("100%");
 					height("100%");
 					textHAlignLeft();
 					textVAlignTop();
 					color("#0F0F");
 				}});
 				
 			}});
 			
 
 
 		}};
 		return builder;
 	}
 	
 	//Creates the panel for the ability buttons
 	private PanelBuilder createAbilityPanel(){
 		PanelBuilder builder = new PanelBuilder("Panel_Abilities"){{
 			width("40%");
          	childLayoutVertical();
  
          	   		//First row with buttons
          	panel(new PanelBuilder("Panel_Abilities_Row1"){{
          		height("50%");
          		childLayoutHorizontal();
         	   
          		for(int i = 1; i<=4; i++){
          			// GUI elements
          			control(createAbilityButton(i));
          		}
            }});    
            
          
            //Second row with buttons
            panel(new PanelBuilder("Panel_Abilities_Row2"){{
         	   height("50%");
         	   childLayoutHorizontal();
                 
         	   for(int i = 5; i<=8; i++){
 	                // GUI elements
 	                control(createAbilityButton(i));
         	   }
      
            }});
 		}};
 		return builder;
 		
 	}
 	
 	//Creates an abilityButton
 	private ButtonBuilder createAbilityButton(final int i){
 		ButtonBuilder builder = new ButtonBuilder("Button_Ability_" + i){{
 	        width("25%");
 	        height("100%");
 	        visible(false);
 	        focusable(false);
 	        interactOnClick("buttonClicked("+i+")");
 	    }};
 		return builder;
 		
 	}
 	
 	/**
 	 * Updates the abilities in the GUI
 	 * @param selectedEntities the abilities of the selected Entity
 	 */
 	public void updateAbilities(List<IEntity> selectedEntities){
     	boolean oneIsSelected = selectedEntities.size()==1;    
     	
     	if(oneIsSelected && selectedEntities.get(0) instanceof IPlayerControlledEntity){
     		selectedPce = (IPlayerControlledEntity) selectedEntities.get(0);
     	}
     	guiView.updateSelected(selectedPce);
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
 	
 	
 	/**
 	 * Used when any of the Ability buttons are clicked
 	 * @param nr the ID of the clicked button
 	 */
 	public void buttonClicked(String nr) {
 		try {
 
 			int iNr = Integer.parseInt(nr);
 			
 			List<IAbility> abilities = selectedPce.getAbilities();
 			if(iNr-1<abilities.size()){
 				input.selectAbility(abilities.get(iNr-1), selectedPce);
 			}
 			
 			
 		} catch (NumberFormatException e){
 			
 		}
 		
 	}
 
 }
