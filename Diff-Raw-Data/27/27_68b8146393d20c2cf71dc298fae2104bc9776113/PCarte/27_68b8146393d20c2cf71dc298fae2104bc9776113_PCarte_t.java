 package solitaire.presentation ;
 
 //import solitaire.controle.* ;
 import java.awt.* ;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.* ;
 import java.io.IOException;
 
 import javax.swing.* ;
 
 import solitaire.controleur.CCarte;
 
 /**
  * Composant Prsentation d'une carte
  */
 public class PCarte extends JPanel implements Transferable{
 
     protected CCarte controleur ;		// contrleur associ
     public CCarte getControleur()
     {
         return controleur;
     }
 
     public void setControle( CCarte controle )
     {
         this.controleur = controle;
     }
 
     protected JLabel face, dos ;
     protected ImageIcon icone ;			// image de la face
     protected static ImageIcon iconeDos;	// image du dos
     public static int largeur, hauteur ;
 
     /**
      * initialiser une carte
      * @param chaine : nom de la carte (exemple "3H" = 3 Heart)
      */
     public PCarte (final String chaine, final CCarte controle) {
     //public PCarte (final String chaine) {
 	this.controleur = controle ;
 
 	// image de la face 
 	icone = new ImageIcon(ClassLoader.getSystemResource(chaine + ".gif"));
 	face = new JLabel (icone) ;
 	add (face) ;
 	face.setSize (largeur, hauteur) ;
 
 	// image du dos
 	dos = new JLabel (iconeDos) ;
 	add (dos) ;
 	dos.setLocation (0, 0) ;
 	dos.setSize (largeur, hauteur) ;
 
 	// le JPanel
 	setLayout (null) ;
 	setBackground (Color.GRAY) ;
 	setOpaque (true);
 	setSize (face.getSize ()) ;
 	setPreferredSize (getSize ()) ;
 	setFaceVisible(false);
 	
 	
 	
     } // constructeur
 
     /**
      * changer la visibilit de la carte
      * @param faceVisible : vrai si la face est visible, faux sinon
      */
     public void setFaceVisible (boolean faceVisible) {
 	face.setVisible(faceVisible);
 	dos.setVisible(!faceVisible);
     }
 
 
     public ImageIcon getIcone () {
 	return icone ;
     }
 
     /**
        initialiser l'image du dos et les dimensions d'une PCarte
     */
     static {
	iconeDos = new ImageIcon(ClassLoader.getSystemResource("dosRB.jpg")) ;
 	largeur = iconeDos.getIconWidth () + 4;
 	hauteur = iconeDos.getIconHeight () + 4;
     }
 
     /**
      * programme de test :  dplacer dans une classe ddie aux tests
      * @param args
      */
     public static void main (String args []) {
 	JFrame f = new JFrame ("Test PCarte") ;
 	f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 	f.setLayout(new FlowLayout()); // au lieu de BorderLayout par dfaut
 	f.getContentPane ().setBackground(new Color(143, 143, 195)); // violet ple
 
 	// une carte visible
 	/*PCarte pc = new PCarte ("QH");
 	pc.setFaceVisible(true);
 	f.getContentPane ().add(pc) ;
 
 	// une carte cache
 	pc = new PCarte("1D");
 	pc.setFaceVisible(false);
 	f.getContentPane ().add(pc) ;
 
 	f.pack () ;		// dimensionner le cadre
 	f.setLocation(200,100);	// le positionner
 	f.setVisible (true) ;	// et le rendre visible*/
     } // main
 
     @Override
     public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException
     {
         return this;
     }
 
     @Override
     public DataFlavor[] getTransferDataFlavors()
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public boolean isDataFlavorSupported( DataFlavor flavor )
     {
         return true;
     }
 
 } // PCarte
