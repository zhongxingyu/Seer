 package client.view;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import client.JeuPanel;
 import client.controller.EcranMenuMultijoueurListener;
 
 import common.Constante;
 import common.Etat;
 import common.Translator;
 
 public class EcranMenuMultijoueur extends NamedJPanel{
 	private static final long serialVersionUID = Constante.NUMERO_DE_VERSION;
 	
 	private JeuPanel jeu;
 	
 	private String[] parties;
 	
 	private String[] column = { "ID","Nom","Nb joueurs","Statut","Password","Action" };
 	private Object[][] data;
 	
 	private JTable table = new JTable();
 	private JScrollPane scrollPane = new JScrollPane(table);
 	
 	
 	private JCoolButton creerPartie;
 	private JCoolButton deconnexion;
 	
 	private JCoolButton rafraichir;
 	
 	private static ImageIcon icon_lock = new ImageIcon(System.getProperty("user.dir") + "/ressources/fr/images/EcranMenuMultijoueur/lock.png");
 	private static ImageIcon icon_unlock = new ImageIcon(System.getProperty("user.dir")
 			+ "/ressources/fr/images/EcranMenuMultijoueur/unlock.png");
 	
 	public EcranMenuMultijoueur(JeuPanel jeuParam){
 		super("ecranMenuMultijoueur");
 		
 		jeu = jeuParam;
 		setLayout(null);
 		
 		
 		creerPartie = new JCoolButton(Translator.translate("creerPartie"));
 		// rejoindrePartie.setEnabled(false); // on rend le bouton inutilisable tant qu'une partie a rejoindre n'a pas été selectionné
 		deconnexion = new JCoolButton(Translator.translate("deconnexion"));
 		rafraichir = new JCoolButton(Translator.translate("rafraichir"));
 		
 		EcranMenuMultijoueurListener ecranLoginListener = new EcranMenuMultijoueurListener(jeu,this); // ajout du listener aux boutons
 		
 		creerPartie.addActionListener(ecranLoginListener);
 		deconnexion.addActionListener(ecranLoginListener);
 		rafraichir.addActionListener(ecranLoginListener);
 		
 		// placement précis pour les composant se superposetn parfaitement a l'image chargé en fond
 		// la heuteur du panel est recalculé en fonction du nb de ligne a afficher + l'entete des colonnes
 		scrollPane.setBounds(110,220,800,300);
 		add(scrollPane);
 		
 		creerPartie.setBounds(450,160,200,50);
 		deconnexion.setBounds(700,160,200,50);
 		rafraichir.setBounds(245,528,200,25);
 		
 		add(creerPartie);
 		add(deconnexion);
 		add(rafraichir);
 		
 		
 		table.addMouseListener(new MouseAdapter(){
 			@Override
 			public void mouseClicked(MouseEvent e){
 				if (e.getClickCount() == 2){
 					int idrow = table.getSelectedRow();
 					String[] partie = parties[idrow].split(Constante.MESSAGE_SEPARATOR);
 					String id_partie = partie[0];
 					// String nom_partie = partie[1];
 					// String nb_joueur_partie = partie[2];
 					
 					boolean password_obligatoire = new Boolean(partie[4]);
 					Etat etat = Etat.get(partie[3]);
 					
 					String password = "";
 					String[] args = new String[3];
 					
 					switch(etat){
 						case SAUVEGARDEE:
 							if (password_obligatoire){
 								password = JOptionPane.showInputDialog(Translator.translate("saisirMotdepasseReprendrePartie"));
 							}
 							args[0] = Constante.COMMANDE_REPRENDRE_PARTIE;
 							args[1] = id_partie;
 							args[2] = password;
 							jeu.getDialogueServeur().sendCommand(args);
 							break;
 						
 						case EN_ATTENTE_JOUEUR:
 							if (password_obligatoire){
 								password = JOptionPane.showInputDialog(Translator.translate("saisirMotdepasseRejoindrePartie"));
 							}
 							args[0] = Constante.COMMANDE_REJOINDRE_PARTIE;
 							args[1] = id_partie;
 							args[2] = password;
 							jeu.getDialogueServeur().sendCommand(args);
 							break;
 						
 						case COMMENCEE:
							jeu.notificationJoueur(Translator.translate("partieDejaCommencee"));
 							break;
 					}
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void paintComponent(Graphics g){
 		try{
 			Image img = null;
 			img = ImageIO.read(new File("ressources/" + Translator.getLangue() + "/images/EcranMenuMultijoueur/fondEcranMenuMultijoueur.png"));
 			g.drawImage(img,0,0,getWidth(),getHeight(),this);// Pour une image de fond
 		}catch (IOException e){
 			if (Constante.MODE_DEBUG){
 				System.out.println("problème lors du chargement de l'image de fond");
 				// e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	public JCoolButton getCreerPartie(){
 		return creerPartie;
 	}
 	
 	public JCoolButton getDeconnexion(){
 		return deconnexion;
 	}
 	
 	public JCoolButton getRafraichir(){
 		return rafraichir;
 	}
 	
 	public void rafraichirCadrePartie(){
 		jeu.recuperationListePartie();
 	}
 	
 	
 	public String[] getParties(){
 		return parties;
 	}
 	
 	public void setParties(String[] parties){
 		this.parties = parties;
 		updateData();
 	}
 	
 	private void updateData(){
 		int rows = parties.length;
 		if (rows > 0){
 			
 			data = new Object[rows][column.length];
 			
 			for (int i = 0 ; i < parties.length ; i++){
 				String[] partie = parties[i].split(Constante.MESSAGE_SEPARATOR);
 				
 				data[i][0] = partie[0];// ID
 				data[i][1] = partie[1];// NOM
 				data[i][2] = partie[2];// NB JOUEURS
 				
 				if (new Boolean(partie[4])){
 					data[i][4] = new JLabel(icon_lock);
 				}else{
 					data[i][4] = new JLabel(icon_unlock);
 				}
 				
 				Etat etat = Etat.get(partie[3]);
 				JLabel button = null;
 				switch(etat){
 					case SAUVEGARDEE:
 						data[i][3] = Translator.translate("etatSauvegardee");// ETAT
 						button = new JLabel(Translator.translate("chargerPartie"));
 						break;
 					
 					case EN_ATTENTE_JOUEUR:
 						data[i][3] = Translator.translate("etatAttenteJoueur");// ETAT
 						button = new JLabel(Translator.translate("rejoindrePartie"));
 						break;
 					
 					case COMMENCEE:
 						data[i][3] = Translator.translate("etatCommencee");// ETAT
 						break;
 				}
 				data[i][5] = button;
 				
 			}
 			
 			table.setModel(new MonModele(data,column));
 			table.setRowHeight(32);
 			
 			TableColumn col = table.getColumnModel().getColumn(0);
 			col.setPreferredWidth(50);
 			
 			col = table.getColumnModel().getColumn(1);
 			col.setPreferredWidth(300);
 			
 			col = table.getColumnModel().getColumn(2);
 			col.setPreferredWidth(75);
 			
 			col = table.getColumnModel().getColumn(3);
 			col.setPreferredWidth(150);
 			
 			col = table.getColumnModel().getColumn(4);
 			col.setPreferredWidth(150);
 			
 			col = table.getColumnModel().getColumn(5);
 			col.setPreferredWidth(150);
 			
 			table.getColumnModel().getColumn(4).setCellRenderer(new MyRenderer());
 			table.getColumnModel().getColumn(5).setCellRenderer(new MyRenderer());
 		}
 	}
 	
 	
 	class MonModele extends AbstractTableModel{
 		private static final long serialVersionUID = 1L;
 		private Object donnees[][];
 		private String titres[];
 		
 		public MonModele(Object donnees[][],String titres[]){
 			this.donnees = donnees;
 			this.titres = titres;
 		}
 		
 		public int getColumnCount(){
 			return donnees[0].length;
 		}
 		
 		public Object getValueAt(int parm1,int parm2){
 			return donnees[parm1][parm2];
 		}
 		
 		public int getRowCount(){
 			return donnees.length;
 		}
 		
 		@Override
 		public String getColumnName(int col){
 			return titres[col];
 		}
 	}
 	
 	class MyRenderer extends DefaultTableCellRenderer{
 		private static final long serialVersionUID = 1L;
 		
 		@Override
 		public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){
 			return (Component)value;
 		}
 	}
 }
