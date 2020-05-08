 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package flickr;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 import javax.xml.stream.XMLStreamException;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  *
  * @author peixoton
  */
 public class Flickr extends JFrame implements ActionListener {
 
     public double screenWidth;
     public double screenHeight;
     
     public Container contenuFenetre;
     public JPanel panelConnexion;
     public JPanel panelSearch;
     public JPanel imagePanel;
     public JPanel panelPhotoZoomed;
     public static final int LARGEUR_FENETRE = 600;
     public static final int HAUTEUR_FENETRE = 400;
     public static final String api_key = "5ba9bb9bbac0804efaccd0f9d5b4b756";
     public Recherche recherche;
     public ParserXML parser;
     public ArrayList<Photo> lesPhotos;
 
     /* Ecran de connexion */
     public JLabel hint_identifiant;
     public JLabel hint_password;
     public JTextField identifiant;
     public JPasswordField password;
     public JButton submit_connexion;
     public JButton submit_forgot;
     /* Model connexion */
     public Connexion connexion_token;
     
     /* Ecran de recherche */
     public JLabel hint_recherche;
     public JTextField field_recheche;
     public JButton bouton_recherche;
     
     /* Ecran des photos */
     
    public int nbCol = 2;
    public int nbLig = 2;
     public int widthImage = 100;
     public int heightImage = 100;
     public JButton images[];
     public JButton retourRecherche;
     
     /* Zoom sur Photo */
     
     public JButton photoZoomed;
     public JButton redirectWeb;
     public Photo laPhoto;
 
     public Flickr() throws MalformedURLException, IOException {
 
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         this.screenWidth = (int)screenSize.getWidth();
         this.screenHeight = (int)screenSize.getHeight();
         
         /*this.ecranConnexion();*/
         this.contenuFenetre = this.getContentPane();
         this.contenuFenetre.setLayout(null);
         this.ecranRecherche();
         /*this.contenuFenetre.add(this.panelConnexion);
         this.contenuFenetre.add(this.panelSearch);*/
 
         setLocation(100, 100);
         setUndecorated(true);
         setVisible(true);
         /*setSize(Flickr.LARGEUR_FENETRE, Flickr.HAUTEUR_FENETRE);*/
         setTitle("Flickr Project Application");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setResizable(false);
         setLayout(null);
     }
 
     public void ecranConnexion() {
 
         /*int width = 400;
         int height = 75;
         int x = this.LARGEUR_FENETRE / 2 - width / 2;
         int y = this.HAUTEUR_FENETRE / 2 - height / 2;
 
         this.panelConnexion = new JPanel();
         this.panelConnexion.setLayout(new GridLayout(3, 2));
         this.panelConnexion.setBounds(x, y, width, height);
 
         this.hint_identifiant = new JLabel("Entrez votre identifiant...");
         this.hint_password = new JLabel("Entrez votre mot de passe...");
 
         this.identifiant = new JTextField();
         this.identifiant.setText("Entrez votre identifiant");
         this.password = new JPasswordField();
 
         this.submit_connexion = new JButton("Connexion");
         this.submit_forgot = new JButton("Mot de passe oublié?");
 
         this.panelConnexion.add(hint_identifiant);
         this.panelConnexion.add(identifiant);
         this.panelConnexion.add(hint_password);
         this.panelConnexion.add(password);
         this.panelConnexion.add(submit_connexion);
         this.panelConnexion.add(submit_forgot);
 
         this.submit_connexion.addActionListener(this);
         this.submit_forgot.addActionListener(this);*/
 
     }
     
     public void ecranRecherche() throws MalformedURLException, IOException{
         int width = 400;
         int height = 200;
         
         Border emptyBorder = BorderFactory.createEmptyBorder();
         
         this.panelSearch = new JPanel();
         this.panelSearch.setLayout(null);
         this.panelSearch.setBounds(0, 0, width, height);
         this.panelSearch.setBackground(Color.black);
         
        this.hint_recherche = new JLabel("RECHERCHER SUR FLICKR");
         this.field_recheche = new JTextField();
         this.bouton_recherche = new JButton("RECHERCHER");
         
         this.field_recheche.setColumns(20);
         this.panelSearch.add(this.hint_recherche);
         this.panelSearch.add(this.field_recheche);
         this.panelSearch.add(this.bouton_recherche);
        this.hint_recherche.setBounds(93, 40, 215, 16);
         this.hint_recherche.setFont(new Font("Verdana", Font.PLAIN, 16));
         this.hint_recherche.setForeground(Color.white);
         this.field_recheche.setBounds(60, 85, 280, 33);
         this.field_recheche.setBorder(emptyBorder);
         this.field_recheche.setBorder(BorderFactory.createCompoundBorder(
         this.field_recheche.getBorder(), 
         BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         this.bouton_recherche.setBounds(60, 120, 280, 33);
         this.bouton_recherche.setFont(new Font("Verdana", Font.PLAIN, 16));
         this.bouton_recherche.setForeground(Color.white);
         this.bouton_recherche.setBackground(new Color(255, 0, 128));
         this.bouton_recherche.setBorder(emptyBorder);
         
         this.bouton_recherche.addActionListener(this);
         
         this.contenuFenetre.add(this.panelSearch);
         
         String path = "http://www.howdoyousay.fr/FlickrAPI/Images/bg.png";
         URL url = new URL(path);
         BufferedImage image = ImageIO.read(url);
         JLabel fond = new JLabel(new ImageIcon(image));
         this.retourRecherche = new JButton(new ImageIcon(image));
         this.panelSearch.add(fond);
         fond.setBounds(0, 0, width, height);
         
         setSize(width, height);
     }
     
     public void ecranImages(){
         try {
             int width = this.nbCol * this.widthImage;
             int height = this.nbLig * this.heightImage;
             
             this.imagePanel = new JPanel();
             this.imagePanel.setLayout(new GridLayout(this.nbLig, this.nbLig));
             this.imagePanel.setBounds(0, 0, width, height);
             
             this.images = new JButton[this.nbCol * this.nbLig];
             
             Border emptyBorder = BorderFactory.createEmptyBorder();
             
             for(int i = 0; i < this.nbCol * this.nbLig; i++){
             
                 if( i < (this.nbCol * this.nbLig) - 1 ){
                     String path = this.lesPhotos.get(i).photo_url;
                     System.out.println("Get Image from " + path);
                     URL url = new URL(path);
                     BufferedImage image = ImageIO.read(url);
                     image.getWidth();
                     image.getHeight();
                     System.out.println("Load image into frame...");
                     JButton button = new JButton(new ImageIcon(image));
                     this.images[i] = button;
                     this.images[i].setBorder(emptyBorder);
                     this.imagePanel.add(this.images[i]);
                     this.images[i].addActionListener(this);
                 }else{
                     String path = "http://www.howdoyousay.fr/FlickrAPI/Images/recherche.png";
                     URL url = new URL(path);
                     BufferedImage image = ImageIO.read(url);
                     this.retourRecherche = new JButton(new ImageIcon(image));
                     this.retourRecherche.setBorder(emptyBorder);
                     this.imagePanel.add(retourRecherche);
                     this.retourRecherche.addActionListener(this);
                 }
                 
             }
             
             setSize(width, height);
             
         } catch (Exception exp) {
             exp.printStackTrace();
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == submit_connexion) {
             
         } else if (e.getSource() == submit_forgot) {
             try {
                 URI uri = new URI("https://edit.europe.yahoo.com/forgotroot?done=https%3A%2F%2Flogin.yahoo.com%2Fconfig%2Fvalidate%3F.src%3Dflickrsignin%26.pc%3D8190%26.scrumb%3D0%26.pd%3Dc%253DJvVF95K62e6PzdPu7MBv2V8-%26.intl%3Dfr%26.done%3Dhttp%253A%252F%252Fwww.flickr.com%252Fsignin%252Fyahoo%252F%253Fredir%253Dhttp%25253A%25252F%25252Fwww.flickr.com%25252F&src=flickrsignin&partner=&intl=fr&lang=fr-FR");
                 Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                 if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                     try {
                         desktop.browse(uri);
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             } catch (URISyntaxException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
         }else if(e.getSource() == this.bouton_recherche){
             try {
                 
                 recherche = new Recherche(this.field_recheche.getText());
                 this.lesPhotos = Recherche.lesPhotos;
                 if(this.lesPhotos.size() > 0){
                     this.panelSearch.setVisible(false);
                     this.ecranImages();
                     this.contenuFenetre.add(this.imagePanel);
                 }else{
                     
                 }
                 
 
             } catch (URISyntaxException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SAXException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             } catch (XMLStreamException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
         }else if(e.getSource() == this.retourRecherche){
             remove(this.panelSearch); 
             remove(this.imagePanel);
             try {
                 ecranRecherche();
             } catch (MalformedURLException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
             this.contenuFenetre.add(this.panelSearch);
             this.panelSearch.setVisible(true);
         }else if(e.getSource() == this.redirectWeb){
             try {
                 URI uri = new URI(this.laPhoto.photo_url);
                 Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                 if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                     try {
                         desktop.browse(uri);
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             } catch (URISyntaxException ex) {
                 Logger.getLogger(Flickr.class.getName()).log(Level.SEVERE, null, ex);
             }
         }else if(e.getSource() instanceof JButton ){
             System.out.println(e.getSource());
             JButton image = new JButton();
             Photo photo = new Photo();
             int found = 0;
             for(int i = 0; i < this.nbCol * this.nbLig; i++){
                 if(e.getSource() == this.images[i]){
                     image = this.images[i];
                     this.laPhoto = this.lesPhotos.get(i);
                     found = 1;
                 }
             }
             
             if(found == 1){
             
                 ImageIcon theImage = (ImageIcon) image.getIcon();
                 this.photoZoomed = new JButton(theImage);
                 Border emptyBorder = BorderFactory.createEmptyBorder();
                 this.photoZoomed.setBorder(emptyBorder);
                 this.imagePanel.setVisible(false);
                 this.panelPhotoZoomed = new JPanel();
                 this.panelPhotoZoomed.setLayout(null);
                 this.panelPhotoZoomed.add(this.photoZoomed);
                 this.photoZoomed.addActionListener(this);
                 this.photoZoomed.setBounds(0, 0, theImage.getIconWidth(), theImage.getIconHeight());
                 
                 this.redirectWeb = new JButton("Afficher dans mon navigateur");
                 this.panelPhotoZoomed.add(this.redirectWeb );
                 this.redirectWeb.setBounds(0, theImage.getIconHeight(), theImage.getIconWidth(), 33 );
                 this.redirectWeb.setBackground(new Color(255, 0, 128));
                 this.redirectWeb.setForeground(Color.white);
                 this.redirectWeb.setBorder(emptyBorder);
                 this.redirectWeb.setFont(new Font("Verdana", Font.PLAIN, 16));
                 this.redirectWeb.addActionListener(this);
 
                 this.panelPhotoZoomed.setVisible(true);
                 this.panelPhotoZoomed.setBounds(0, 0, theImage.getIconWidth(), theImage.getIconHeight() + 33 );
                 this.contenuFenetre.add(this.panelPhotoZoomed);
                 setSize( theImage.getIconWidth(), theImage.getIconHeight() + 33 );
                 
             }else{
                 /* Quand on clique sur la photo qui est déjà zoomée. */
                 int width = this.nbCol * this.widthImage;
                 int height = this.nbLig * this.heightImage;
                 setSize(width, height);
                 this.panelPhotoZoomed.setVisible(false);
                 this.imagePanel.setVisible(true);
                 
             }
             
             
         }
     }
 
     public static void main(String[] args) throws MalformedURLException, IOException {
         Flickr application = new Flickr();
     }
 }
