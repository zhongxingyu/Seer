 package de.dhbw.muehle.gui;
 
 import java.awt.FlowLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import de.dhbw.muehle.gui.menus.GamePanel;
 import de.dhbw.muehle.gui.menus.MainMenu;
 import de.dhbw.muehle.gui.menus.Menu;
 import de.dhbw.muehle.gui.menus.SettingsPanel;
 import de.dhbw.muehle.model.theme.Theme;
 
 
 
 /**
  * Hier wird die View gebaut. Nur der Aufbau !!
  * sämtliche Funktionen werden im ViewController und den View Actions implementiert.
  * @author Kreistschen
  *
  */
 
 public class View extends JFrame{
 	
 	private Theme theme;
 
 	protected MainMenu mainMenu;
 	protected GamePanel gamePanel;
 	protected SettingsPanel settingsPanel;
 	
 	
 	public View(ViewController vController, Theme theme){
 		this.theme = theme;
 		
 		setTitle("JMuehle");		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setBounds(100,100,500,500);
 		
 		
 		// mainMenu initialisieren
 		mainMenu = new MainMenu(vController, this);
 		
 		// gamePanel initialisieren
 		gamePanel = new GamePanel(vController, this);
 		
 		// setingsPanel initialisieren
 		settingsPanel = new SettingsPanel(vController, this);
 	}
 	
 	
 	
 	/**
 	 * Setzt eine Theme
 	 * @param theme Theme
 	 */
 	public void setTheme(Theme theme){
 		this.theme = theme;
 		repaint();
 	}
 	
 	/**
 	 * Liefert das aktuell verwendete Theme
 	 * @return Theme
 	 */
 	public Theme getTheme(){
 		return theme;
 	}
 	
 	
 	
 	/**
 	 * Setzt das Hauptpanel des Frames
 	 * @param container Container (In diesem Fall das JPanel)
 	 */
 	public void setContentPane(Menu container){
 		
 		// aktuelles Panel entfernen, sofern vorhanden
 		if(getContentPane() != null)
 			remove(getContentPane());
 		
 		// neues Panel setzen
 		super.setContentPane(new JPanel());
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
 		getContentPane().add(container);
 		
 		// Framegröße auf Panelgröße anpassen
 		if(container.getOriginalSize().height > getHeight() || container.getOriginalSize().width > getWidth()){
 			container.setPreferredSize(container.getOriginalSize());
 			
 			pack();
 						
 			//Frameminimumgröße auf Panelgröße festlegen
 			setMinimumSize(getSize());
 		}
 		
 		validate();
 		repaint();
 	}
 }
