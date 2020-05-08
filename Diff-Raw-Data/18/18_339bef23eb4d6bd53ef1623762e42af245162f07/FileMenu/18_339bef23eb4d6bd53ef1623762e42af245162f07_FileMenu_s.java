 package btlshp.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import btlshp.Btlshp;
 import btlshp.enums.AppState;
 
 public class FileMenu extends JMenu {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8339680861310634872L;
 	
 	protected JMenuItem[] fileMenuItems;
 	
 	public FileMenu() {
 		super("File");
 		setMnemonic(KeyEvent.VK_F);
 		getAccessibleContext().setAccessibleDescription("Main menu used to starting, saving, or quitting games.");
 		
 		fileMenuItems = new JMenuItem[7];
 		
 		JMenuItem newGameItem = fileMenuItems[0] = new JMenuItem("New");
 		newGameItem.setMnemonic(KeyEvent.VK_N);
 		newGameItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				Btlshp.getGame().newGame();
 			}	
 		});
 		add(newGameItem);
 		addSeparator();
 		JMenuItem joinGameItem = fileMenuItems[1] = new JMenuItem("Join game");
 		joinGameItem.setMnemonic(KeyEvent.VK_J);
 		joinGameItem.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent ev){
 				Btlshp.getGame().joinGame();
 			}
 		});
 		add(joinGameItem);
 		JMenuItem saveGameItem = fileMenuItems[2] = new JMenuItem("Save");
 		saveGameItem.setMnemonic(KeyEvent.VK_S);
 		saveGameItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				Btlshp.getGame().saveGame();
 			}	
 		});
 		add(saveGameItem);
 		
 		JMenuItem restoreGameItem = fileMenuItems[3] = new JMenuItem("Restore saved game");
 		restoreGameItem.setMnemonic(KeyEvent.VK_O);
 		restoreGameItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				Btlshp.getGame().restoreGame();
 			}	
 		});
 		add(restoreGameItem);
 		addSeparator();
 		
 		
 		JMenuItem forfeitGameItem = fileMenuItems[4] = new JMenuItem("Forfeit current game");
 		forfeitGameItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Btlshp.getGame().forfeitGame();
 			}
 		});
 		add(forfeitGameItem);
 		addSeparator();
 		
 		JMenuItem helpItem = fileMenuItems[5] = new JMenuItem("Help");
 		helpItem.setMnemonic(KeyEvent.VK_H);
 		helpItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ev) {
 				Btlshp.getGame().helpScreen();
 			}	
 		});
 		add(helpItem);
 		addSeparator();
 		
 		JMenuItem quitItem = fileMenuItems[6] = new JMenuItem("Quit");
 		quitItem.setMnemonic(KeyEvent.VK_Q);
 		quitItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Btlshp.getGame().quitGame();
 			}
 		});
 		add(quitItem);
 	}
 	
 	
 	
 	public void updateMenuItems() {
 		if(Btlshp.getGame().getAppState() == AppState.NoGame) {
			fileMenuItems[3].setEnabled(false);
			//fileMenuItems[2].setEnabled(false);
			fileMenuItems[5].setEnabled(false);
 		}
 		else {
			fileMenuItems[1].setEnabled(true);
 			fileMenuItems[2].setEnabled(true);
			fileMenuItems[3].setEnabled(true);
 		}
 	}
 	
 }
