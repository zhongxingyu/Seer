 package org.noip.staticattic.GUI.Events;
 
 import java.awt.Color;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import org.noip.staticattic.GUI.MainWindow;
 import org.noip.staticattic.fileutils.TextureHandler;
 
 public class ShowMenuScreen extends GUIEvent{
 
 	public ShowMenuScreen(MainWindow main, int delay) {
 		
 		super(main, delay);
 		
 	}
 	
 	@Override
 	public void execute() {
 		
 		super.getMain().mainpanel.removeAll();
 		super.getMain().mainpanel.setBackground(Color.WHITE);
		super.getMain().mainpanel.setOpaque(true);
 		
 		JButton exit = new JButton();
 		JButton newgame = new JButton();
 		JButton cont = new JButton();
 		JButton options = new JButton();
 		
 		JLabel gamelogo = new JLabel(TextureHandler.getLogo());
 		gamelogo.setBounds((int)(super.getMain().screensize.getWidth()/2)-485, (int)(super.getMain().screensize.getHeight()/4)-65, 970, 130);
 		
 		newgame.setIcon(TextureHandler.getNewUp());
 		newgame.setPressedIcon(TextureHandler.getNewDown());
 		newgame.setRolloverIcon(TextureHandler.getNewOver());
 		newgame.setContentAreaFilled(false);
 		newgame.setBorderPainted(false);
 		newgame.setBounds((int)(super.getMain().screensize.getWidth()/2)-200, gamelogo.getY()+gamelogo.getHeight()+60, 400, 60);
 		newgame.setActionCommand("newgame");
 		newgame.addActionListener(super.getMain());
 		
 		cont.setIcon(TextureHandler.getContUp());
 		cont.setPressedIcon(TextureHandler.getContDown());
 		cont.setRolloverIcon(TextureHandler.getContOver());
 		cont.setContentAreaFilled(false);
 		cont.setBorderPainted(false);
 		cont.setBounds((int)(super.getMain().screensize.getWidth()/2)-200, newgame.getY()+90, 400, 60);
 		cont.setActionCommand("continue");
 		cont.addActionListener(super.getMain());
 		
 		options.setIcon(TextureHandler.getOptionsUp());
 		options.setPressedIcon(TextureHandler.getOptionsDown());
 		options.setRolloverIcon(TextureHandler.getOptionsOver());
 		options.setContentAreaFilled(false);
 		options.setBorderPainted(false);
 		options.setBounds((int)(super.getMain().screensize.getWidth()/2)-200, cont.getY()+90, 400, 60);
 		options.setActionCommand("options");
 		options.addActionListener(super.getMain());
 		
 		exit.setIcon(TextureHandler.getExitUp());
 		exit.setPressedIcon(TextureHandler.getExitDown());
 		exit.setRolloverIcon(TextureHandler.getExitOver());
 		exit.setContentAreaFilled(false);
 		exit.setBorderPainted(false);
 		exit.setBounds((int)(super.getMain().screensize.getWidth()/2)-200, options.getY()+90, 400, 60);
 		exit.setActionCommand("exit");
 		exit.addActionListener(super.getMain());
 		
 		super.getMain().mainpanel.add(exit);
 		super.getMain().mainpanel.add(gamelogo);
 		super.getMain().mainpanel.add(options);
 		super.getMain().mainpanel.add(newgame);
 		super.getMain().mainpanel.add(cont);
 		
 		super.getMain().repaint();
 		
 	}
 
 }
