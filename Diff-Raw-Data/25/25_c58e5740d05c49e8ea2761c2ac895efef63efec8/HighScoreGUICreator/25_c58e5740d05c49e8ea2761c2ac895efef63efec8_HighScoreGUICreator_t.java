 package projectrts.view;
 
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 
 /**
  * A class that creates the GUI for the Highscore menu
  * @author Filip Brynfors
  *
  */
 public class HighScoreGUICreator {
 	
 	private HighScoreGUICreator(){	
 	}
 	
 	/**
 	 * Creates the Main layer for the menu GUI
 	 * @param time the time for the highscore
 	 * @return the LayerBuilder for the layer
 	 */
 	public static LayerBuilder createMainLayer(final float time) {
 		LayerBuilder layer = new LayerBuilder("Layer_Highscore_Main") {{
 			childLayoutCenter();
			
			
 			panel(new PanelBuilder("Panel_GUI") {{
 				childLayoutVertical();
 				valignCenter();
 
 				control(new LabelBuilder("Label_Score"){{
 					width("100%");
 					int min = (int) (time/60);
 					int sec = (int) (time%60);
 					text("Congratulations!\nYou managed to survive "+min+" minutes and "+sec+" seconds!");
 				}});
 				
				//Menu buttons
 				control(new ButtonBuilder("Button_Menu", "Back to Menu"){{
 					alignCenter();
 					interactOnClick("buttonRestartClicked()");
 				}}); 
 				control(new ButtonBuilder("Button_Exit", "Exit Game"){{
 					alignCenter();
 					interactOnClick("buttonExitClicked()");
 				}});
 			}});
 		}};
 		return layer;
 	}
 }
