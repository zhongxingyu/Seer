 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.event.ChangeListener;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.ScrollPane;
 
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JTextPane;
 import javax.swing.ImageIcon;
 import java.awt.Color;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JComboBox;
 
 
 public class ErfgoedPanel extends ImagePanel  {
 	private JTextField naamVeld;
 	private JTextField locatieVeld;
 	private JTextField linkVeld;
 	private JTextArea geschiedenisVeld;
 	private JTextArea kenmerkenVeld;
 	private JTextArea nuttigeinfoVeld;
 	private JComboBox gemeente;
 	private JComboBox statuut;
 	private JComboBox type;
 	
 	private Model model;
 	private hoofd_scherm parentFrame;
 	private ErfgoedPanel parent;
 	private Erfgoed erfgoed;
 	private int nr = -1;
 	/**
 	 * Create the panel.
 	 */
 	public ErfgoedPanel(Model m, Parameter para) {
 		super("erfgoed.jpg");
 		model = m;
 		parent=this;
 		parentFrame=model.getHoofdframe();
 		setLayout(null);
 		setPreferredSize(new Dimension(1000, 500));
 		JLabel lblNaam = new JLabel("Naam");
 		lblNaam.setBounds(70, 67, 77, 14);
 		add(lblNaam);
 		
 		
 		JLabel lblLocatie = new JLabel("Locatie : ");
 		lblLocatie.setBounds(70, 120, 125, 14);
 		add(lblLocatie);
 		
 		JLabel lblStau = new JLabel("Kenmerken");
 		lblStau.setBounds(710, 67, 125, 14);
 		add(lblStau);
 		
 		JLabel lblGeschiedenis = new JLabel("Geschiedenis");
 		lblGeschiedenis.setBounds(246, 67, 83, 14);
 		add(lblGeschiedenis);
 		
 		JLabel lblNuttigeInfo = new JLabel("Nuttige info");
 		lblNuttigeInfo.setBounds(479, 67, 103, 14);
 		add(lblNuttigeInfo);
 		
 		naamVeld = new JTextField();
 		naamVeld.setBounds(70, 89, 149, 20);
 		add(naamVeld);
 		naamVeld.setColumns(10);
 		
 		locatieVeld = new JTextField();
 		locatieVeld.setBounds(70, 140, 149, 20);
 		add(locatieVeld);
 		locatieVeld.setColumns(10);
 		
 		JLabel lblLink = new JLabel("Link");
 		lblLink.setBounds(70, 171, 46, 14);
 		add(lblLink);
 		
 		linkVeld = new JTextField("http://");
 		linkVeld.setBounds(70, 190, 149, 20);
 		add(linkVeld);
 		linkVeld.setColumns(10);
 		
 		geschiedenisVeld = new JTextArea();
 		geschiedenisVeld.setWrapStyleWord(true);
 		geschiedenisVeld.setLineWrap(true);
 		JScrollPane geschScroll = new JScrollPane(geschiedenisVeld);
 		geschScroll.setBounds(246, 87, 200, 380);
 		add(geschScroll);
 		
 		
 		nuttigeinfoVeld = new JTextArea();
 		JScrollPane nuttigeScroll = new JScrollPane(nuttigeinfoVeld);
 		nuttigeinfoVeld.setWrapStyleWord(true);
 		nuttigeinfoVeld.setLineWrap(true);
 		nuttigeScroll.setBounds(479, 87, 200, 380);
 		add(nuttigeScroll);
 		
 		kenmerkenVeld = new JTextArea();
 		JScrollPane kenmerkenScroll = new JScrollPane(kenmerkenVeld);
 		kenmerkenVeld.setWrapStyleWord(true);
 		kenmerkenVeld.setLineWrap(true);
 		kenmerkenScroll.setBounds(710, 87, 200, 380);
 		add(kenmerkenScroll);
 		
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBounds(0, 1, 1000, 27);
 		add(menuBar);
 		
 		JMenu mnDitErfgoed = new JMenu("Dit erfgoed");
 		menuBar.add(mnDitErfgoed);
 		
 		JMenuItem mntmNieuwItem = new JMenuItem("Nieuw Item");
 		mntmNieuwItem.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				model.getHoofdframe().newPanel(new DashbordPanel(model,new Parameter(erfgoed, "nieuw")));
 			}
 		});
 		mnDitErfgoed.add(mntmNieuwItem);
 		
 		JMenuItem mntmToonAlleItems = new JMenuItem("Toon alle items");
 		mnDitErfgoed.add(mntmToonAlleItems);
 		mntmToonAlleItems.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				parentFrame.newPanel(new DashbordPanel(model,new Parameter(erfgoed, "zoeken")));
 				
 			}
 		});
 		
 		JMenuItem mntmBewaren = new JMenuItem("Bewaren");
 		mnDitErfgoed.add(mntmBewaren);
 		mntmBewaren.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				if(!naamVeld.getText().equals("")) {
 					if(nr == -1) {
 					model.schrijfNieuwErfgoed(new Erfgoed(naamVeld.getText(), locatieVeld.getText(), type.getSelectedItem().toString(), linkVeld.getText(), geschiedenisVeld.getText(), nuttigeinfoVeld.getText(), kenmerkenVeld.getText(), statuut.getSelectedItem().toString(), gemeente.getSelectedItem().toString() ));
 					model.leesAlleItems();
 					}
 					else
 					{
 					model.schrijfErfgoed(new Erfgoed(nr, naamVeld.getText(), locatieVeld.getText(), type.getSelectedItem().toString(), linkVeld.getText(), geschiedenisVeld.getText(), nuttigeinfoVeld.getText(), kenmerkenVeld.getText(), statuut.getSelectedItem().toString(), gemeente.getSelectedItem().toString() ));
 					model.leesAlleItems();
 					}
					JOptionPane.showMessageDialog(null, "Het erfgoed is opgeslagen");
 				}
 				else {
 					JOptionPane.showMessageDialog(model.getHoofdframe(), "Je moet minstens een naam voor het erfgoed opgeven");
 				}
 			}
 		});
 		
 		JMenuItem mntmVerwijderen = new JMenuItem("Verwijderen");
 		mnDitErfgoed.add(mntmVerwijderen);
 		mntmVerwijderen.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Erfgoed erfgoed = new Erfgoed(nr, naamVeld.getText(), locatieVeld.getText(), type.getSelectedItem().toString(), linkVeld.getText(), geschiedenisVeld.getText(), nuttigeinfoVeld.getText(), kenmerkenVeld.getText(), statuut.getSelectedItem().toString(), gemeente.getSelectedItem().toString() );
 				if(nr != -1) {
 					int result = JOptionPane.showConfirmDialog(null, "Ben je zeker dat je het erfgoed " + erfgoed.getNaam() + " wilt verwijderen?");
 					if(result == JOptionPane.YES_OPTION) {
 						if(model.magErfgoedVerwijderdWorden(erfgoed)) {
 							Erfgoed e = new Erfgoed();
 							model.removeErfgoed(erfgoed);
 							naamVeld.setText(e.getNaam());
 							locatieVeld.setText(e.getLocatie());
 							gemeente.setSelectedIndex(0);
 							linkVeld.setText(e.getLink());
 							statuut.setSelectedIndex(0);
 							kenmerkenVeld.setText(e.getKenmerken());
 							type.setSelectedIndex(0);
 							geschiedenisVeld.setText(e.getGeschiedenis());
 							nuttigeinfoVeld.setText(e.getInfo());
 						}
 						else {
 							JOptionPane.showMessageDialog(null, "Het erfgoed heeft relateerde items en kan dus niet verwijderd worden.");
 						}
 				}
 				}
 			}
 		});
 		
 		JMenuItem mntmLeegmaken = new JMenuItem("Leegmaken");
 		mnDitErfgoed.add(mntmLeegmaken);
 		mntmLeegmaken.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				parentFrame.newPanel(new ErfgoedPanel(model,new Parameter("nieuw", String.valueOf(nr))));
 			}
 		});
 		
 		JMenu mnErfgoeden = new JMenu("Erfgoeden");
 		menuBar.add(mnErfgoeden);
 		
 		JMenuItem mntmSelecteerErfgoed = new JMenuItem("Selecteer Erfgoed");
 		mnErfgoeden.add(mntmSelecteerErfgoed);
 		mntmSelecteerErfgoed.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ArrayList<Erfgoed> erfgoeden = model.getErfgoeden();
 				Erfgoed input = (Erfgoed) JOptionPane.showInputDialog(null,
 						"Selecteer het erfgoed dat je wilt bewerken","Erfgoedbeheer", JOptionPane.INFORMATION_MESSAGE, 
 						null, erfgoeden.toArray(),"Tennis");
 				parentFrame.newPanel(new ErfgoedPanel(model,new Parameter(input)));
 			}
 		});
 		
 		JMenuItem mntmNieuwErfgoed = new JMenuItem("Nieuw Erfgoed");
 		mnErfgoeden.add(mntmNieuwErfgoed);
 		mntmNieuwErfgoed.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				parentFrame.newPanel(new ErfgoedPanel(model,new Parameter("nieuw")));
 			}
 		});
 		
 		JLabel lblGemeente = new JLabel("Gemeente");
 		lblGemeente.setBounds(70, 221, 77, 14);
 		add(lblGemeente);
 		
 		JLabel lblStatuut = new JLabel("Statuut");
 		lblStatuut.setBounds(70, 269, 77, 14);
 		add(lblStatuut);
 		String[] gemeentes = {"Herzele", "Borsbeke","Hillegem","Ressegem", "Sint-Antelinks", "Sint-Lievens-Esse", "Steenhuize-Wijnhuize", "Woubrechtegem" };
 		gemeente = new JComboBox(gemeentes);
 		gemeente.setBounds(70, 238, 149, 20);
 		add(gemeente);
 		String[] statuten = {"Geklasseerd", "Andere" };
 		statuut = new JComboBox(statuten);
 		statuut.setBounds(70, 286, 149, 20);
 		add(statuut);
 		
 		JLabel lblTypeGebouw = new JLabel("Type");
 		lblTypeGebouw.setBounds(70, 317, 103, 14);
 		add(lblTypeGebouw);
 		
 		String[] types = {"Landschappen", "Dorpsgezichten", "Gebouwen", "Archeologische sites", "Andere" };
 		type = new JComboBox(types);
 		type.setBounds(70, 334, 149, 20);
 		add(type);
 		processParameter(para);
 	}
 	private void processParameter(Parameter para)
 	{
 		if(para.getSoort() == "nieuw")
 		{
 			if(para.getParameter() != null)
 			{
 				nr = Integer.parseInt(para.getParameter());
 			}
 		}
 		if(para.getObject() != null)
 		{
 			if(para.getObject() instanceof Erfgoed)
 			{
 				Erfgoed e = (Erfgoed) para.getObject();
 				erfgoed = e;
 				nr = e.getErfgoedNr();
 				naamVeld.setText(e.getNaam());
 				locatieVeld.setText(e.getLocatie());
 				gemeente.setSelectedItem(e.getGemeente());
 				linkVeld.setText(e.getLink());
 				statuut.setSelectedItem(e.getStatuut());
 				kenmerkenVeld.setText(e.getKenmerken());
 				type.setSelectedItem(e.getType());
 				geschiedenisVeld.setText(e.getGeschiedenis());
 				nuttigeinfoVeld.setText(e.getInfo());
 			}
 		}
 	}
 }
 
