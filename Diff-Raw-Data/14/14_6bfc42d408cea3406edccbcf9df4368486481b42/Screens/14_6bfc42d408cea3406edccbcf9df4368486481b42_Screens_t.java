 package FullGame;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.ImageBuilder;
 import de.lessvoid.nifty.builder.TextBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.screen.DefaultScreenController;
 import de.lessvoid.nifty.screen.Screen;
 
 public class Screens {
 
 	public Screen startScreen(Nifty nifty, final HippoDisplay a){
 		nifty.loadStyleFile("nifty-default-styles.xml");
 		nifty.loadControlFile("nifty-default-controls.xml");
 		Screen screen = new ScreenBuilder("start") {{
	        controller(new FullGame.MyScreens(a));
 	        layer(new LayerBuilder("background") {{
 	            childLayoutCenter();
 	            //backgroundColor("#000f");
 	            // <!-- ... -->
 	            image(new ImageBuilder() {{
 	                filename("Interface/FFF.jpg");
 	            }});
 	        }});
 	 
 	        layer(new LayerBuilder("foreground") {{
 	                childLayoutVertical();
 	                backgroundColor("#0000");
 	 
 	            // panel added
 	            panel(new PanelBuilder("panel_top") {{
 	                childLayoutCenter();
 	                alignCenter();
 	                //backgroundColor("#f008");
 	                height("25%");
 	                width("75%");
 	                
 	                text(new TextBuilder() {{
 	                    text("Famished Famished Frogs!");
 	                    font("Interface/verdana-48-regular.fnt");
 	                    height("100%");
 	                    width("100%");
 	                }});
 	            }});
 	 
 	            panel(new PanelBuilder("panel_mid") {{
 	                childLayoutCenter();
 	                alignCenter();
 	                //backgroundColor("#0f08");
 	                height("50%");
 	                width("75%");
 	                
 	                text(new TextBuilder() {{
 	                    text("The frogs are hungry and want to scarf down their food! "+
 	                         "Help them eat by playing this game like Hungry, Hungry Hippos");
 	                    font("Interface/verdana-48-regular.fnt");
 	                    wrap(true);
 	                    height("100%");
 	                    width("100%");
 	                }});
 	            }});
 	 
 	            panel(new PanelBuilder("panel_bottom") {{
 	                childLayoutHorizontal();
 	                alignCenter();
 	                //backgroundColor("#00f8");
 	                height("25%");
 	                width("75%");
 	 
 	                panel(new PanelBuilder("panel_bottom_left") {{
 	                    childLayoutCenter();
 	                    valignCenter();
 	                    //backgroundColor("#44f8");
 	                    height("50%");
 	                    width("50%");
 	                    
 	                    control(new ButtonBuilder("SinglePlayer", "Single Player") {{
 	                        alignCenter();
 	                        valignCenter();
 	                        height("50%");
 	                        width("50%");
 	                        visibleToMouse(true);
 	                        interactOnClick("singlePlayerGame()");
 	                      }});
 	                }});
 	 
 	                panel(new PanelBuilder("panel_bottom_right") {{
 	                    childLayoutCenter();
 	                    valignCenter();
 	                    //backgroundColor("#88f8");
 	                    height("50%");
 	                    width("50%");
 	                    
 	                    control(new ButtonBuilder("MultiPlayer", "Multiplayer") {{
 	                        alignCenter();
 	                        valignCenter();
 	                        height("50%");
 	                        width("50%");
 	                        visibleToMouse(true);
 	                        //interactOnClick("multiplayerGame");
 	                      }});
 	   
 	                }});
 	            }}); // panel added
 	        }});
 	 
 	    }}.build(nifty);
 		return screen;
 	}
 	
 	public Screen hudScreen(Nifty nifty, final HippoDisplay a){
 		nifty.loadStyleFile("nifty-default-styles.xml");
 		nifty.loadControlFile("nifty-default-controls.xml");
 		Screen screen = new ScreenBuilder("hud") {{
	        controller(new FullGame.MyScreens(a));
 	        
 	        layer(new LayerBuilder("background") {{
 	            childLayoutCenter();
 	            //backgroundColor("#000f");
 	            /*
 	            image(new ImageBuilder() {{
 	                filename("Interface/hud-frame.png");
 	                width("100%");
 	                height("100%");
 	            }}); */
 	        }});
 	 
 	        layer(new LayerBuilder("foreground") {{
 	            childLayoutHorizontal();
 	            //backgroundColor("#0000");
 	 
 	            // panel added
 	            panel(new PanelBuilder("panel_left") {{
 	                childLayoutVertical();
 	                //backgroundColor("#0f08");
 	                height("100%");
 	                width("80%");
 	                // <!-- spacer -->
 	            }});
 	 
 	            panel(new PanelBuilder("panel_right") {{
 	                childLayoutVertical();
 	                //backgroundColor("#ffff");
 	                height("100%");
 	                width("20%");
 	 
 	                panel(new PanelBuilder("panel_top_right1") {{
 	                    childLayoutCenter();
 	                    //backgroundColor("#00f8");
 	                    height("20%");
 	                    width("100%");
 
 	                    control(new LabelBuilder(){{
 	                    	id("score");
 	                        text("Current Score: 0"); 
 		                    font("Interface/verdana-48-regular.fnt");
 	                        width("100%"); 
 	                        height("100%");
 	                    }});
 	                }});
 	 
 	                panel(new PanelBuilder("panel_top_right2") {{
 	                    childLayoutCenter();
 	                    //backgroundColor("#44f8");
 	                    height("15%");
 	                    width("100%");
 	                    
 	                    image(new ImageBuilder() {{
 	                        filename("Interface/face1.png");
 	                        valignCenter();
 	                        alignCenter();
 	                        height("50%");
 	                        width("30%");
 	                    }});
 	                }});
 	 
 	                panel(new PanelBuilder("panel_bot_right") {{
 	                    childLayoutCenter();
 	                    valignCenter();
 	                    //backgroundColor("#88f8");
 	                    height("70%");
 	                    width("100%");
 	                }});
 	            }}); // panel added
 	        }});
 	    }}.build(nifty);
 		return screen;
 	}
 	
 	public Screen pauseScreen(Nifty nifty, final HippoDisplay a){
 		nifty.loadStyleFile("nifty-default-styles.xml");
 		nifty.loadControlFile("nifty-default-controls.xml");
 		Screen screen = new ScreenBuilder("pause") {{
	        controller(new FullGame.MyScreens(a));
 	        layer(new LayerBuilder("background") {{
 	            childLayoutCenter();
 	            //backgroundColor("#000f");
 	            // <!-- ... -->
 	           // image(new ImageBuilder() {{
 	            //    filename("Interface/FFF.jpg");
 	            //}});
 	        }});
 	 
 	        layer(new LayerBuilder("foreground") {{
 	                childLayoutCenter();
 	                backgroundColor("#0000");
 
 	            panel(new PanelBuilder("panel_mid") {{
 	                childLayoutVertical();
 	                alignCenter();
 	                //backgroundColor("#0f08");
 	                height("50%");
 	                width("75%");
 	                
 	                panel(new PanelBuilder("panel_middle_text") {{
 	                    childLayoutVertical();
 	                    alignCenter();
 	                    //backgroundColor("#44f8");
 	                    height("50%");
 	                    width("45%");
 	                    
 		                text(new TextBuilder() {{
 		                    text("Go get yourself a SNACK, ${CALL.getName()}!" +
 		                    		" YOU DESERVE IT. CHAMP.");
 		                    font("Interface/verdana-48-regular.fnt");
 		                    wrap(true);
 		                    height("50%");
 		                    width("100%");
 		                }});
 		                
 	                    control(new LabelBuilder(){{
 	                    	id("score");
 	                        text("Current score: 0"); 
 		                    font("Interface/verdana-48-regular.fnt");
 	                        width("100%"); 
 	                        height("50%");
 	                    }});                    
 	             
 
 	                }});
 	                
 	                panel(new PanelBuilder("panel_middle_button1") {{
 	                    childLayoutCenter();
 	                    alignCenter();
 	                    //backgroundColor("#44f8");
 	                    height("25%");
 	                    width("30%");
 	                    
 		                text(new TextBuilder() {{
 		                    text("Press 'p' to resume");
 		                    font("Interface/verdana-48-regular.fnt");
 		                    wrap(true);
 		                    height("100%");
 		                    width("100%");
 		                }});
 	                }});
 	                
 	                panel(new PanelBuilder("panel_middle_button2") {{
 	                    childLayoutCenter();
 	                    alignCenter();
 	                    //backgroundColor("#44f8");
 	                    height("25%");
 	                    width("30%");
 	                    
 	                    control(new ButtonBuilder("QuitButton", "Quit") {{
 	                        alignCenter();
 	                        //valignCenter();
 	                        height("60%");
 	                        width("100%");
 	                        visibleToMouse(true);
 	                        interactOnClick("exitGame()");
 	                    }});
 	                }});                       
    
 	            }});
 
 	        }});
 	 
 	    }}.build(nifty);
 		return screen;
 	}
 
 }
