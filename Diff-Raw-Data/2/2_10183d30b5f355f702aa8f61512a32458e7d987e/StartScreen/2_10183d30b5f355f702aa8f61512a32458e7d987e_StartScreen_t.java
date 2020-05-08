 package de.htw.hundertwasser.view;
 
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.Locale;
 
 import javax.naming.OperationNotSupportedException;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.LookAndFeel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import de.htw.hundertwasser.res.RessourcenEnummeration;
 import de.htw.hundertwasser.res.RessourcenEnummeration.*;
 
 /**
  * Klasse die den StartScreen des DBPM anzeigt.
  * 
  * @author Fabian
  * @version 9.9.12
  */
 
 public class StartScreen extends JFrame{
 	//Konstanten
 	public static final String DBPM = "Dunkelbunt Photo Manager";
 	public static final String PBOXES = "Your Photo Boxes";
 	public static final String PALBUMS = "Your Photo Albums";
 	
 	//Variablen
 	private static JFrame mainScreen;
 	private static StartScreenSubPanel photoBoxes;
 	private static StartScreenSubPanel photoAlbums;
 	private static Color backgroundColor = Color.WHITE; //Hintergrundfarbe des StartScreens
 	//TODO in PhotoBox/-Album einfuegen
 	public static int noOfAlbums = 0;
 	public static int noOfBoxes = 0;
 	//TODO End
 	private static String albumText = PALBUMS + " (" + noOfAlbums + ")";
 	private static String boxText = PBOXES + " (" + noOfBoxes + ")";
 	private static JLabel albumTextLabel;
 	private static JLabel boxTextLabel;
 	public static Dimension screenSize;
 	public static Dimension textSize;
 	public static Dimension subSystemSize;
 	public static Dimension scrollSize;
 	public static Dimension elementSize; 
 	
 	
 	public StartScreen() {
 		super(DBPM);
 		setBackground(Color.BLACK);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Wenn man auf das X drckt wird das Programm komplett beendet.
 		setSize(screenSize);
 
 		setLocationRelativeTo(null); //Setzt das Fenster in die Mitte
 		setLayout( new GridLayout(0, 1, 0, 1)); //Anzahl der Spalten, Zeilen, Frewwwwwiraum(L/R), Freiraum(O/U)
 	}
 	
 	public static void main(String[] args) {
 		try {
 //			for(LookAndFeelInfo info:UIManager.getInstalledLookAndFeels())
 //			{
 //				System.out.println(info.getClassName());
 //			}
 //			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			UIManager.setLookAndFeel(
 			"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
 //			"com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"
 			);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		initialiseSizes();
 		mainScreen = new StartScreen(); 
 		mainScreen.add(initialisePhotoBoxes());
 		mainScreen.add(initialisePhotoAlbums());
 		mainScreen.setVisible(true);
 		
 	}
 	
 	/**
 	 * Methode die das SubPanel f�r die Photoalben erstellt.
 	 */
 	private static JPanel initialisePhotoAlbums() {
 		photoAlbums = new StartScreenSubPanel();
 		photoAlbums.setBackground(getBGColor());  //Hintergrundfarbe einstellen
 		albumTextLabel = new JLabel(albumText, JLabel.CENTER); //Text erstellen
 		albumTextLabel.setForeground(Color.BLACK); //Textfarbe einstellenFont boxFont;
 		albumTextLabel.setPreferredSize(textSize);
 		Font albumFont;
 		try {
 			albumFont = RessourcenEnummeration.FONT_CALIBRI_BOLD.getFont().deriveFont(40f);
 //			albumText.setFont(new Font("Calibri", 1, 40)); //Hier wird schriftart und gre bestimmt... Globalisieren?
 			albumTextLabel.setFont(albumFont);
 			photoAlbums.add(albumTextLabel, BorderLayout.NORTH);  //Text in das GUI einfgen
 			JPanel albumMainPanel = new JPanel();
 			photoAlbums.initialiseElements(albumMainPanel, StartScreenElement.ALBUM); //SubPanel wird hier erstellt
 
 		} catch (OperationNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FontFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		photoAlbums.setPreferredSize(subSystemSize);
 		return photoAlbums;
 	}
 
 	/**
 	 * Methode die das SubPanel f�r die Photoboxen erstellt.
 	 */
 	private static JPanel initialisePhotoBoxes() {
 		photoBoxes = new StartScreenSubPanel();
 		photoBoxes.setBackground(getBGColor());
 		boxTextLabel = new JLabel(boxText, JLabel.CENTER);
 		boxTextLabel.setForeground(Color.BLACK);
 		boxTextLabel.setPreferredSize(textSize);
 		Font boxFont;
 		
 		try {
 			boxFont = RessourcenEnummeration.FONT_CALIBRI_BOLD.getFont().deriveFont(40f);
 //			boxText.setFont(new Font("Calibri", 1, 40)); //Hier wird schriftart und gre bestimmt... Globalisieren?
 			boxTextLabel.setFont(boxFont);
 			photoBoxes.add(boxTextLabel, BorderLayout.NORTH);
 			JPanel boxMainPanel = new JPanel();
 			photoBoxes.initialiseElements(boxMainPanel, StartScreenElement.BOX);
 
 		} catch (OperationNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FontFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return photoBoxes;
 	}
 	
 	/**
 	 * Methode die eine Farbe zur�ckliefert.
 	 * sollte benutztwerdne um eine einheitliche Hintergrundfarbe zu verwenden.
 	 * @return Color: Farbe, die in backGroundColor steht
 	 */
 	public static Color getBGColor() {
 		return backgroundColor;
 	}
 	
 	public static void initialiseSizes() {
 		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		textSize = new Dimension(screenSize.width, 100);
 		subSystemSize = new Dimension( screenSize.width*8/10, screenSize.height/2-20);
 		scrollSize = new Dimension(subSystemSize.width*3/4-20, subSystemSize.height-111);
 		elementSize = new Dimension(scrollSize.width/3-7, scrollSize.height-10);
 		
 	}
 	
 	public static void retextAlbum() {
 		albumText = PALBUMS + " (" + noOfAlbums + ")";
 		albumTextLabel.setText(albumText);
 		albumTextLabel.repaint();
 	}
 
 	public static void retextBox() {
 		boxText = PBOXES +  " (" + noOfBoxes + ")";
 		boxTextLabel.setText(boxText);
 		boxTextLabel.repaint();
 	}
 	
 	public static Dimension getScreenSize() {
 		return screenSize;
 	}
 	
 	public static Dimension getSubSystemSize() {
 		return subSystemSize;
 	}
 	
 	public static Dimension getScrollSize() {
 		return scrollSize;
 	}
 	
 	public static Dimension getElementSize() {
 		return elementSize;
 	}
 }
 
 
 
