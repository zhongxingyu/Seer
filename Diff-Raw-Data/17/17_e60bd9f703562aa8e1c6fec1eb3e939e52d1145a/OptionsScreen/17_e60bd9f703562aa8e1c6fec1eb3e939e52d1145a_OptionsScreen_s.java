 package caskman.polygonsim.screens;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import caskman.polygonsim.Launcher;
 import caskman.polygonsim.Parameters;
 import caskman.polygonsim.model.GameModel;
 import caskman.polygonsim.model.Vector;
 import caskman.polygonsim.screens.OptionItem.OptionItemListener;
 import caskman.polygonsim.screens.SelectionScreen.SelectionAction;
 
 public class OptionsScreen extends GameScreen {
 	
 	private GameModel model;
 	private List<Item> optionItems;
 
 	public OptionsScreen(ScreenManager manager, boolean isFullscreen,GameModel gameModel) {
 		super(manager, isFullscreen);
 		model = gameModel;
 		
 		initialize();
 	}
 	
 	private void initialize() {
 		optionItems = new ArrayList<Item>();
 		OptionItem i;
 		StackPanel sp = new StackPanel();
 		Dimension stackDims = new Dimension(manager.getScreenDims().width/8,3*manager.getScreenDims().height/8);
 		sp.setDims(stackDims);
 		sp.setPosition(new Vector((manager.getScreenDims().width - stackDims.width)/2,3*manager.getScreenDims().height/8));
 		sp.setOrientation(StackPanel.VERTICAL);
 		optionItems.add(sp);
 		
 		
 		i = new OptionItem();
 		i.setText("Options");
 		i.setTextSize(40);
 		i.setDims(new Dimension(200,50));
 		i.setPosition(new Vector((manager.getScreenDims().width - i.getDims().width)/2,manager.getScreenDims().height/4));
 		optionItems.add(i);
 		
 		i = new OptionItem();
 		i.setText("Change Window Mode");
 		i.setDims(new Dimension(150,20));
 		sp.add(i);
 		
 		i = new OptionItem();
 		i.setText("Change Dot Color");
 		i.addOptionItemListener(new OptionItemListener() {
 			@Override
 			public void itemActivated() {
 				manager.addScreen(new SelectionScreen(manager,false,Parameters.getDotColorChoices(),new SelectionAction() {
 					@Override
 					public void selectionMade(int selection) {
 						try {
 							Parameters.DOT_COLOR = Parameters.string2Color(Parameters.getDotColorChoices()[selection]);
 						} catch (Exception e) {
 							
 						}
 					}
 				}));
 			}
 		});
 		i.setDims(new Dimension(150,20));
 		sp.add(i);
 		
 		i = new OptionItem();
 		i.setText("Change Line Color");
 		i.addOptionItemListener(new OptionItemListener() {
 			@Override
 			public void itemActivated() {
 				manager.addScreen(new SelectionScreen(manager,false,Parameters.getLineColorChoices(),new SelectionAction() {
 					@Override
 					public void selectionMade(int selection) {
 						try {
						Parameters.LINE_COLOR = Parameters.string2Color(Parameters.getLineColorChoices()[selection]);
 						} catch (Exception e) {
 							
 						}
 					}
 				}));
 			}
 		});
 		i.setDims(new Dimension(150,20));
 		sp.add(i);
 		
 		i = new OptionItem();
 		i.setText("Change Background Color");
 		i.addOptionItemListener(new OptionItemListener() {
 			@Override
 			public void itemActivated() {
 				manager.addScreen(new SelectionScreen(manager,false,Parameters.getBackgroundColorChoices(),new SelectionAction() {
 					@Override
 					public void selectionMade(int selection) {
 						try {
 							Parameters.LINE_COLOR = Parameters.string2Color(Parameters.getBackgroundColorChoices()[selection]);
 						} catch (Exception e) {
 							
 						}
 					}
 				}));
 			}
 		});
 		i.setDims(new Dimension(150,20));
 		sp.add(i);
 		
 		i = new OptionItem();
 		i.setText("Exit");
 		i.setDims(new Dimension(100,20));
 		i.setTextSize(20);
 		i.addOptionItemListener(new OptionItemListener() {
 			@Override
 			public void itemActivated() {
 				Launcher.exit();
 			}
 		});
 		sp.add(i);
 		
 		
 	}
 
 	@Override
 	public void updateScreen() {
 		
 	}
 
 	@Override
 	public void drawScreen(Graphics2D g, float interpol) {
 		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5F));
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, manager.getScreenDims().width, manager.getScreenDims().height);
 		g.setComposite(AlphaComposite.SrcOver);
 		
 		Rectangle r = new Rectangle(manager.getScreenDims().width/8, manager.getScreenDims().height/8, 3*manager.getScreenDims().width/4, 3*manager.getScreenDims().height/4);
 		g.setColor(Color.BLACK);
 		g.fillRect(r.x,r.y,r.width,r.height);
 		g.setColor(Color.WHITE);
 		g.drawRect(r.x,r.y,r.width,r.height);
 		
 		for (Item i : optionItems) {
 			i.draw(g,interpol);
 		}
 	}
 
 	@Override
 	public void manageInput(InputEvent e) {
 		if (e.isKeyInput()) {
 			if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getType() == InputEvent.KEY_PRESSED) {
 				exitScreen();
 				model.setPaused(false);
 			}
 		}
 		
 		for (Item i : optionItems) {
 			i.manageInput(e);
 		}
 	}
 	
 	
 
 }
