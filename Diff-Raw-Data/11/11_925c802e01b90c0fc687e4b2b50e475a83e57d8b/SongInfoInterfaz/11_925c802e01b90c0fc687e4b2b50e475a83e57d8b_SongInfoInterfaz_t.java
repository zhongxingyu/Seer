 package IS2011.Interfaz;
 import java.awt.*;
 
 import javax.swing.event.*;
 import javax.swing.*;
 
 
 //import javazoom.jlgui.basicplayer.BasicPlayerException;
 import IS2011.bibliotecaXML.Track;
 
 import com.sun.awt.AWTUtilities;
 
 
 public class SongInfoInterfaz extends JPanelTransparente {
 	
 	private static final long serialVersionUID = 1L;
 
 	private SongInfoInterfaz principal = null;
 	private InterfazAvanzada interfazAvanzada;
 	
 	private JPanel caratulaPanel = null;
 	private JPanelTransparente infoPanel = null;
 	private JLabel  etiqueta = null;
 	private JLabel etiquetaCaratula = null;
 	private ImageIcon caratula = null;
 
 	private Track track = null;
 
 	private Color c= new Color(240,240,240);
 	/**
 	 *  @param track de la que hay que mostrar la info 
 	 *  @throws InterruptedException 
 	 */
 	public SongInfoInterfaz(){
 		super();
 		principal = this;
 		//principal.setLayout(new GridBagLayout());
 		//principal.setBackground(Color.white);
 		principal.setEnabled(true);	
 		principal.setSize(500,150);
 		//principal.setAlwaysOnTop(true);
 		//principal.setResizable(false);
 		principal.setVisible(true);
 		principal.setArcw(0); 
 		principal.setArch(0); 
 		principal.setColorPrimario(Color.black);
 		principal.setColorSecundario(Color.black);
 		principal.setTran(0.8f);
 		principal.setVisible(true);
 		principal.setLayout(new GridBagLayout());
 
 		//cerrar a los 5 min
 		
 	}
 	
 	private JPanelTransparente getInfoPanel() {
 		if(infoPanel == null){
 			infoPanel = new JPanelTransparente();
 			infoPanel.setTran(0);
 			infoPanel.setLayout(new GridLayout());
 			//infoPanel.setBackground(Color.black);
			infoPanel.setSize(300,120);
 			infoPanel.setArcw(0); 
 			infoPanel.setArch(0); 
 			infoPanel.setColorPrimario(Color.white);
 			infoPanel.setColorSecundario(Color.white);
 			//infoPanel.setForeground(Color.white);
 			etiqueta = new JLabel();
 			etiqueta.setFont(new java.awt.Font("Helvetica", 1, 12));
 			//etiqueta.setBackground(null);			
 			etiqueta.setForeground(c);	
 			
 			
 			infoPanel.add(etiqueta);
 		}
 		etiqueta.setText("<html> Artist: " + track.getArtist()
 				+"<html><br/> Title: " + track.getName() 
 				+ "<html><br/> Album: " + track.getAlbum());
 		return infoPanel;
 	}
 	
 	public void actualiza(Track pista){
 		this.track = pista;
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.weightx = 0.0;
 		constraints.insets= new Insets(10,10,10,10);
 		constraints.fill = GridBagConstraints.NONE;
 		constraints.anchor = GridBagConstraints.WEST; 
 		principal.add(getCaratulaPanel(),constraints);
 	/*	JPanel caratula = getCaratulaPanel();
 		caratula.setBounds(20,20, 110, 110);
 		principal.add(getCaratulaPanel());*/
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = 1;
 		constraints.weightx = 1.0;
 		constraints.anchor = GridBagConstraints.CENTER; 
 		constraints.fill = GridBagConstraints.NONE;
 		/*JPanelTransparente info = getInfoPanel();
 		info.setBounds(160,20,110,400);
 		principal.add(getInfoPanel());
 		principal.setSize(600,150);*/
 		principal.add(getInfoPanel(),constraints);
 		
 
 		
 	}
 	private JPanel getCaratulaPanel() {
 		if(caratulaPanel == null){
 			caratulaPanel = new JPanel();
			caratulaPanel.setSize(120,120);
 			caratulaPanel.setBackground(Color.black);
 			//caratulaPanel.setForeground(Color.c);	
 			etiquetaCaratula = new JLabel();
 			caratulaPanel.add(etiquetaCaratula);
 			/*if(track.getArtwork() != null)
 				caratulaPanel.getGraphics().drawImage((Image)track.getArtwork(), 0, 0, null);*/
 		}
 		if(track.getNumCaratulas() > 0 && track.getArtwork() != null){
			caratula = new ImageIcon(track.getArtwork().getScaledInstance(120,120,Image.SCALE_SMOOTH));
 		}else 	caratula = new ImageIcon(new ImageIcon("images/monkeyIcon2.png").getImage()
											.getScaledInstance(120,120, Image.SCALE_SMOOTH));
 		etiquetaCaratula.setIcon(caratula);
 
 		return caratulaPanel;
 	}
 	
 
 	
 	
 	public Track getTrack(){
 		return track;
 	}
 	
 	public SongInfoInterfaz getPrincipal(){
 		return principal;
 	}
 	
 }
