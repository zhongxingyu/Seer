 package projectrts.view;
 
 import de.lessvoid.nifty.builder.HoverEffectBuilder;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 
 /**
  * A class that creates the GUI for the game
  * @author Filip Brynfors
  *
  */
 public class InGameGuiCreator {
 	private static final String greenColor = "#0F0F";
 	
 	private InGameGuiCreator(){
 	}
 	
 	/**
 	 * Creates the main layer for the game
 	 * @return the LayerBuilder for the layer
 	 */
 	public static LayerBuilder createMainLayer(){
 		LayerBuilder layer = new LayerBuilder("Layer_main") {{
 			childLayoutVertical();
 			
 			panel(new PanelBuilder("panel_main") {{
 				childLayoutVertical();
 				backgroundColor("#0000");
 				height("80%");
 			}});
 			
 			panel(new PanelBuilder("Panel_GUI") {{
 				childLayoutHorizontal();
 				visibleToMouse(true);
 				
 				panel(createPlayerInfoPanel());
 				
 				panel(createBuildInfoPanel());
 				
 				panel(createMiddlePanel()); 
 			
 				panel(createAbilityPanel());
 				
 			}});
 		}};
 		return layer;
 	}
 	
 	//Creates the panel that shows the player info
 	private static PanelBuilder createPlayerInfoPanel(){
 		PanelBuilder builder = new PanelBuilder("Panel_PlayerInfo"){{
 			width("20%");
 			childLayoutVertical();
 			
 			control(new LabelBuilder("Label_Time"){{
 				width("100%");
 				textHAlignLeft();
 				textVAlignTop();
 				color(greenColor);
 				
 			}});
 			
 			control(new LabelBuilder("Label_PlayerInfo"){{
 				width("100%");
 				textHAlignLeft();
 				textVAlignTop();
 				color(greenColor);
 				
 			}});
 			
 		}};
 		return builder;
 	}	
 	
 	//Creates the panel that shows build info
 	private static PanelBuilder createBuildInfoPanel(){
 		PanelBuilder builder = new PanelBuilder("Panel_BuildInfo"){{
 			width("20%");
 			childLayoutVertical();
 			visible(false);
 			
 			control(new LabelBuilder("Label_BuildText"){{
 				width("100%");
 				height("100%");
 				textVAlignTop();
 				color(greenColor);
 				
 			}});			
 		}};
 		return builder;
 	}
 	
 	//Creates the panel that shows current hp
 	private static PanelBuilder createMiddlePanel(){
 		PanelBuilder builder = new PanelBuilder("Panel_SelectedInfo"){{
 			width("20%");
 			childLayoutVertical();
 			backgroundColor("#000F");
 			visible(false);
 			
 			control(new LabelBuilder("Label_Name"){{
 				width("100%");
 				color("#00FF");
 			}});
 			
 
 			panel(new PanelBuilder("Panel_Info"){{
 				childLayoutHorizontal();
 				
 				control(new LabelBuilder("Label_Info"){{
 					width("30%");
 					height("100%");
 					textHAlignLeft();
 					textVAlignTop();
 					color(greenColor);
 					
 				}});
 				
 				control(new LabelBuilder("Label_InfoValues"){{
 					width("100%");
 					height("100%");
 					textHAlignLeft();
 					textVAlignTop();
 					color(greenColor);
 				}});
 			}});
 		}};
 		return builder;
 	}
 	
 	//Creates the panel for the ability buttons
 	private static PanelBuilder createAbilityPanel(){
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
 	private static ButtonBuilder createAbilityButton(final int i){
 		ButtonBuilder builder = new ButtonBuilder("Button_Ability_" + i){{
 	        width("25%");
 	        height("100%");
 	        visible(false);
 	        focusable(false);
 	        interactOnClick("buttonClicked("+i+")");
 	        
 	        onStartHoverEffect(new HoverEffectBuilder("nop"){{
 	        	effectParameter("onStartEffect","buttonMouseEnter("+i+")");
 	        }});
 	        
 	        onEndHoverEffect(new HoverEffectBuilder("nop"){{
 	        	effectParameter("onStartEffect","buttonMouseLeave("+i+")");
 	        }});
 	        
 	    }};
 		return builder;
 		
 	}
 
 	/**
 	 * Creates the layer for the messages
 	 * @return the LayerBuilder for the layer
 	 */
 	public static LayerBuilder createMessageLayer() {
 		LayerBuilder layer = new LayerBuilder("Layer_Message"){{
 			childLayoutCenter();
 			
 			panel(new PanelBuilder("Panel_Message"){{
 				childLayoutCenter();
 				
 				control(new LabelBuilder("Label_Message"){{
 					width("100%");
 					height("100%");
 					color("#F00F");
 					visible(false);
 					
 				}});
 			}});
 		}};
 		return layer;
 	}
 	
 	/**
 	 * Creates the layer for the tooltip info
 	 * @return the LayerBuilder for the layer
 	 */
 	public static LayerBuilder createTooltipLayer() {
 		LayerBuilder layer = new LayerBuilder("Layer_Tooltip"){{
 			childLayoutAbsolute();
 			
 			panel(new PanelBuilder("Panel_Tooltip"){{
 				childLayoutVertical();
 				
 				width("200px");
 				height("60px");
 				backgroundColor("#FFFF");
 				visible(false);
 				
 				control(new LabelBuilder("Label_TooltipHeader"){{
 					width("100%");
 					color("#F00F");
 					textVAlignTop();
 				}});
 				
 				control(new LabelBuilder("Label_TooltipInfo"){{
 					width("100%");
 					height("100%");
 					color("#F00F");
 					textVAlignTop();
 				}});
 			}});
 		}};
 		return layer;
 	}
 }
