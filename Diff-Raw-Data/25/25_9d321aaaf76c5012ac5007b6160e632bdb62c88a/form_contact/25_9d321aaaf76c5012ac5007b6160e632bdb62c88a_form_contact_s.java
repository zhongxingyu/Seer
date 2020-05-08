 package windows.forms;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import session.Session;
 import session.objects.group;
 
 /*import windows.actions.Menu_Ajout_Contact;
 import windows.actions.Clicks_Contact;
 import windows.actions.Menu_Deconnexion;
 import windows.actions.Menu_principal_apropos;*/
 import windows.actions.buttons.changestatus_button;
 import windows.actions.menus.menubar_changepseudo;
 import windows.actions.menus.menubar_quit;
 
 public class form_contact extends form_abstract{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	protected JFrame fram;
 	private JPanel pan;
 	private JLabel Titre;
 	private JLabel Soustitre;
 	private JList  list;
 	private JComboBox changstatus;
 	private JButton addcontact;
 	private JButton changepseudo;
 	private JMenuItem quitter;
 	private JMenuItem ajoutcontact;
 	private JMenuItem changerpseudo;
 	private JMenuItem deconnexion;
 	private JMenuItem aPropos;
 
 	public form_contact()
 	{
 		BuildWindow();
 	}
 
 	protected void BuildWindow()
 	{
 		
 		BuildFrame();
 		BuildMenuBar();
 		
 		pan=new JPanel();
 		pan.setLayout(new FlowLayout());
 		pan.setBackground(new Color(128,128,255));
 		pan.setLayout(new FlowLayout(FlowLayout.CENTER,600,15));
 
 		Titre=new JLabel("Vous etes connect en tant que " + Session.getPseudo(),JLabel.CENTER);
 		
 		changstatus= new JComboBox();
 		changstatus.addItem("Online");
 		changstatus.addItem("Busy");
 		changstatus.addItem("AFK");
 		changstatus.addItem("Offline");
 		changstatus.setSelectedIndex(Session.getStatus() - 1);
 		changstatus.addActionListener(new changestatus_button(changstatus.getSelectedIndex() + 1));
 		
 		Soustitre = new JLabel("Liste de vos contacts : ");
 		
 		pan.add(Titre);
 		pan.add(changstatus);
 		pan.add(Soustitre);
 		
 		setlistcontact();	
 		
 		fram.setContentPane(pan);        
 		fram.setVisible(true); 
 	    
 	}
 	
 	private void setlistcontact()
 	{
		Vector<group> contacts= Session.getGroups();
		//remplacer par listecontacts bien sur...
		list = new JList(contacts);
 		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//list.addMouseListener(new Clicks_Contact(list));
 		pan.add(list);
 		JScrollPane listeAvecAscenseur = new JScrollPane(list);
 		listeAvecAscenseur.setPreferredSize(new Dimension(150, 100));
 		pan.add(listeAvecAscenseur);
 	}
 	
 	protected void BuildMenuBar() 
 	{
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu1 = new JMenu("Menu");
 		JMenu menu2 = new JMenu("?");
 		
 		AddItem(menu1,"Quitter",new menubar_quit());
 		//AddItem(menu1,"Ajouter un contact",new Menu_Ajout_Contact());
 		AddItem(menu1,"Changer de pseudo",new menubar_changepseudo(Titre));
 		//AddItem(menu1,"Se dconnecter",new Menu_Deconnexion(fram));
 		//AddItem(menu2,"A propos...",new Menu_principal_apropos());
 	
 		menuBar.add(menu1);
 		menuBar.add(menu2);
 		fram.setJMenuBar(menuBar);
 	}
 	
 
 	protected void BuildFrame() 
 	{
 		fram=new JFrame();
 		fram.setTitle("Cookie Messenger - Contacts"); 
 		fram.setSize(400,600); 
 		fram.setLocationRelativeTo(null);
 		fram.setResizable(true);
 		fram.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
 	}
 	
 	private void AddItem(JMenu menu, String label, ActionListener obj)
 	{
 		JMenuItem temp = new JMenuItem(label);
 		temp.addActionListener(obj);
 		menu.add(temp);
 		
 	}
 	public JPanel getPan(){
 		return this.pan;
 	}
 
 	
 }
