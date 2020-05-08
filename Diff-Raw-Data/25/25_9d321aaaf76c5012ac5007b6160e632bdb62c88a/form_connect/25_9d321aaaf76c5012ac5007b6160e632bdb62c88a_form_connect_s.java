 package windows.forms;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.io.*;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.JCheckBox;
 
 import windows.actions.menus.menubar_quit;
 import windows.actions.buttons.connect_button;
 import windows.actions.menus.menubar_a_propos;
 
 public class form_connect extends form_abstract {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel pan;
 	private JLabel Titre;
 	private JLabel labeluser;
 	private JLabel labelpass;
 	private JTextField mail;
 	private JPasswordField password;
 	private JButton connect;
 	protected JFrame fram;
 	private JComboBox status;
 	private JCheckBox save;
 	
 	private String r_user,r_pass,file,str;
 	
 	public form_connect()
 	{
 		ReadConfigFile();
 		BuildWindow();
 	}
 
 	private void ReadConfigFile(){
 		r_user="";
 		r_pass="";
 		file ="savedvariables";
 		str="";
 		try
 		{
			InputStream ips=new FileInputStream(file); 
 			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
 			String ligne;
 			while ((ligne=br.readLine())!=null)
 				str+=ligne+"\n";
 
 			br.close();
 			String tab[]=str.split("\n");
 				r_user=tab[0];
 				r_pass=tab[1];
 		}		
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	protected void BuildWindow()
 	{
 		BuildFrame();
 		BuildMenuBar();
 		
 		pan=new JPanel();
 		pan.setLayout(new FlowLayout());
 		pan.setBackground(new Color(128,128,255));
 		pan.setLayout(new FlowLayout(FlowLayout.CENTER,600,10));
 
 		Titre=new JLabel("Bienvenue dans Cookie Messenger",JLabel.CENTER);
 		//image
 		labeluser = new JLabel("Adresse e-mail");
 		mail = new JTextField(r_user,15);
 		labelpass = new JLabel("Mot de passe");
 		password = new JPasswordField(r_pass,15);
 		status= new JComboBox();
 		status.addItem("Online");
 		status.addItem("Busy");
 		status.addItem("AFK");
 		status.addItem("Offline");
 		save = new JCheckBox("Enregistrer les infos");
 		if(r_user != "" && r_user != "")
 			save.setSelected(true);
 		connect=new JButton("Connexion");
 		connect.addActionListener(new connect_button(status,mail,password,save.isSelected()));
 
 		pan.add(Titre);
 		pan.add(labeluser);
 		pan.add(mail);
 		pan.add(labelpass);
 		pan.add(password);
 		pan.add(status);
 		pan.add(connect);
 		
 		fram.setContentPane(pan);        
 	    fram.setVisible(true); 
 	}
 	
 	protected void BuildFrame()
 	{
 		fram=new JFrame();
 		fram.setTitle("Cookie Messenger - Connexion"); 
 		fram.setSize(300,600); 
 		fram.setLocationRelativeTo(null);
 		fram.setResizable(false);
 		fram.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 	}
 	
 	protected void BuildMenuBar() 
 	{
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu1 = new JMenu("Menu");
 		JMenu menu2 = new JMenu("?");
 		JMenuItem quitter;
 		JMenuItem aPropos;
 		
 		/*contenu du menu "menu":*/
 		quitter = new JMenuItem("Quitter");
 		quitter.addActionListener(new menubar_quit());
 		menu1.add(quitter);
 		menuBar.add(menu1);
 
 		/*contenu du menu "?":*/
 		aPropos = new JMenuItem("A propos");
 		aPropos.addActionListener(new menubar_a_propos());
 		menu2.add(aPropos);
 		menuBar.add(menu2);
 		fram.setJMenuBar(menuBar);
 	}
 }
