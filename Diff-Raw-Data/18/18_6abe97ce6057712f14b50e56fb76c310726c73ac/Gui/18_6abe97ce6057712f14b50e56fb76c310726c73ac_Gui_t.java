 /**
  * 
  */
 package aufgabe6;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JTree;
 import javax.swing.ListSelectionModel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import aufgabe6.net.Client;
 import aufgabe6.net.Server;
 
 /**
  * @author sascha
  *
  */
 public class Gui implements GuiInterface {
 	
 	private static final Dimension FENSTER_MIN_DIM = new Dimension(800,600);
 
 	private static final int NAMENSFELD_MAX_BREITE = 20;
 	private static final int IPFELD_MAX_BREITE = 20;
 
 	private JFrame fenster = null;
 	
 	private boolean ichBinDerServer = false;
 	private DefaultListModel serverList = new DefaultListModel();
 	
 	private JPanel serverSicht = null;
 	private final JList serverAnsicht = new JList(serverList);
 	
 	private JTree spielerAnsicht = null;
 	
 	private DefaultMutableTreeNode wurzelKnotenSpielerAnsicht = null;
 	
 	private JScrollPane serverAnsichtsContainer = null;
 	
 	private DefaultMutableTreeNode wurzelKnotenServerAnsicht = null;
 	
 	private JTextField namensFeld = null;
 	public String getNamensFeldInhalt()
     {
         return namensFeld.getText();
     }
 
 	private JTextField spielerNamensFeld = null;
 	public String getSpielerNamensFeldInhalt()
     {
         return spielerNamensFeld.getText();
     }
 
     private JTextField ipFeld = null;
 	
 	private JPanel knopfContainerServerAnsicht = null;
 	
 	private JButton erstellKnopf = null;
 	
 	private JButton hinzufuegenKnopf = null;
 	
 	private boolean verbindenHeisstVerbinden = true;
 	private JButton verbindeKnopf = null;
 	
 	private JTextPane spielNachrichten = null;
 	
 	private GuiSpielfeld spielfeld = null;
 
 	private JPanel spielfeldContainer = null;
 	
 	private static Gui singleTon = null;
 	
 	public static Gui getGui(){
 		if(Gui.singleTon == null){
 			Gui.singleTon = new Gui();
 		}
 		return Gui.singleTon;
 	}
 	
 	private void verbinde() {
 		Client c = Client.getInstance();
         int index = serverAnsicht.getSelectedIndex();
         System.out.println(c.getServerInfos().get(index).getIp());
         c.verbinde(c.getServerInfos().get(index).getIp(), spielerNamensFeld.getText());
         
         this.spielfeld = new GuiSpielfeld();
         
         this.spielfeldContainer.add(this.spielfeld);
         
         passeSpielfeldAn();
 
         
         System.out.println(this.spielfeld);
 	}
 	
	public void setStartenKnopfZustand(boolean wirKoennenStarten)
 	{
 		if (!ichBinDerServer)
 			return;
         if (wirKoennenStarten)
         {
         	this.erstellKnopf.setToolTipText("Das Spiel starten.");
         	this.erstellKnopf.setEnabled(true);
         }
         else
         {
     		this.erstellKnopf.setToolTipText("Wir haben noch keine Spieler.");
         	this.erstellKnopf.setEnabled(false);
         }
 	}
 	
 	private void toggleVerbindenKnopf() {
 		if(verbindenHeisstVerbinden) {
 			verbindeKnopf.setText("trennen");
 		} else {
 			verbindeKnopf.setText("verbinden");
 		}
 		verbindenHeisstVerbinden = !verbindenHeisstVerbinden;
 	}
 	
 	private Gui(){
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
 		
 		this.fenster = new JFrame("Mensch aergere dich nicht");
 		this.fenster.setMinimumSize(FENSTER_MIN_DIM);
 		this.fenster.setUndecorated(false);
 		this.fenster.setLocationByPlatform(true);
 		this.fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JSplitPane unterteiler = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); 
 		unterteiler.setResizeWeight(1d);
 		unterteiler.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
 		unterteiler.setDividerSize(5);
 		
 		this.spielfeldContainer  = new JPanel();
 //		
 //		this.spielfeld = new GuiSpielfeld();
 //		
 //		spielfeldContainer.add(this.spielfeld);
 		
 		unterteiler.setLeftComponent(spielfeldContainer);
 		
 		JPanel rechtesUnterFenster = new JPanel(new BorderLayout());
 		rechtesUnterFenster.setPreferredSize(new Dimension(200,1));
 		
 		this.serverSicht = new JPanel(new BorderLayout());
 				
 		serverAnsicht.setLayoutOrientation(JList.VERTICAL);
 		serverAnsicht.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		serverAnsicht.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent event) {
 				if (event.getFirstIndex() >= 0) {
 					verbindeKnopf.setToolTipText("Verbindet mit dem ausgewählten Server.");
 					verbindeKnopf.setEnabled(true);
 				} else {
 					verbindeKnopf.setToolTipText("Bitte einen Server zum Verbinden auswählen.");
 					verbindeKnopf.setEnabled(false);
 				}
 			}		
 		});
 		
 		serverAnsicht.addMouseListener(new MouseListener(){
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				if(event.getClickCount()>1 && verbindenHeisstVerbinden) {
 					verbinde();
 					toggleVerbindenKnopf();
 				}
 			}
 			public void mouseEntered(MouseEvent arg0) {}
 			public void mouseExited(MouseEvent arg0) {}
 			public void mousePressed(MouseEvent arg0) {}
 			public void mouseReleased(MouseEvent arg0) {}
 		});
 		
 		this.serverAnsichtsContainer = new JScrollPane(serverAnsicht);
 		
 		this.serverSicht.add(this.serverAnsichtsContainer,BorderLayout.NORTH);
 		
 		this.knopfContainerServerAnsicht = new JPanel(new GridLayout(2,3));
 		
 		this.namensFeld  = new JTextField("Entfernter Server", NAMENSFELD_MAX_BREITE);
 		this.namensFeld.setFont(this.namensFeld.getFont().deriveFont(9.f));
 		this.namensFeld.setEditable(true);
 						
 		this.erstellKnopf = new JButton("erstellen");
 		this.erstellKnopf.setToolTipText("Startet einen Serverprozeß im Hintergrund, der auf Verbindungen wartet.\nDer Name des Servers kommt aus dem Spieler/Server-Feld.");
 		this.erstellKnopf.addActionListener(
 		        new ActionListener()
 		        {
                     @Override
                     public void actionPerformed(ActionEvent e)
                     {
                     	if (ichBinDerServer)//ich bin host und habe "Spiel starten" geklickt
                     	{
                     		MenschMain.getDasSpiel().start();
                     		Client.getInstance().getClientKommunikationsThread().sendeBewegungsAufforderung(5);
                     		erstellKnopf.setEnabled(false);
                     	}
                     	else //"Spiel erstellen geklickt"
                     	{
                     		Server s = new Server(9999, spielerNamensFeld.getText());
                     		s.lausche();
                     		erstellKnopf.setText("Spiel starten");
                     		ichBinDerServer = true;
                    		setStartenKnopfZustand(false);
                    		
                     		Client c = Client.getInstance();
                     		Client.ServerInfo si = c.new ServerInfo(spielerNamensFeld.getText(),"127.0.0.1");
                     		Client.getInstance().getServerInfos().add(si);
                     		serverList.addElement(spielerNamensFeld.getText());
                     	}
                     }
 		        }
         );
 
 		this.spielerNamensFeld = new JTextField("Spieler/Server");
 		this.spielerNamensFeld.setFont(this.spielerNamensFeld.getFont().deriveFont(9.f));
 
 
 		this.namensFeld.setFont(this.namensFeld.getFont().deriveFont(9.f));		
 	    this.ipFeld = new JTextField("IP", IPFELD_MAX_BREITE);
 		this.ipFeld.setFont(this.ipFeld.getFont().deriveFont(9.f));
         this.ipFeld.setEditable(true);
 
         this.hinzufuegenKnopf = new JButton("hinzufügen");
 		this.hinzufuegenKnopf.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 Client c = Client.getInstance();
                 Client.ServerInfo si = c.new ServerInfo(namensFeld.getText(),ipFeld.getText());
                 Client.getInstance().getServerInfos().add(si);
                 serverList.addElement(namensFeld.getText());
             }
 		});
 		
 		this.verbindeKnopf = new JButton("verbinden");
 		this.verbindeKnopf.setEnabled(false);
 		this.verbindeKnopf.setToolTipText("Bitte einen Server zum Verbinden auswählen.");
 		this.verbindeKnopf.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				if(verbindenHeisstVerbinden) {
 					verbinde();
 					toggleVerbindenKnopf();
 				} else {
 					//trennen();
 					toggleVerbindenKnopf();
 				}
 			}
         });
 		
 		this.knopfContainerServerAnsicht.add(this.namensFeld);
 	    this.knopfContainerServerAnsicht.add(this.ipFeld);
 		this.knopfContainerServerAnsicht.add(this.hinzufuegenKnopf);
 		this.knopfContainerServerAnsicht.add(this.spielerNamensFeld);
         this.knopfContainerServerAnsicht.add(this.erstellKnopf);
 		this.knopfContainerServerAnsicht.add(this.verbindeKnopf);
 		
 		this.serverSicht.add(this.knopfContainerServerAnsicht, BorderLayout.CENTER);
 		
 		rechtesUnterFenster.add(this.serverSicht, BorderLayout.NORTH);
 		
 		this.wurzelKnotenSpielerAnsicht = new DefaultMutableTreeNode();
 		
 		this.spielerAnsicht = new JTree(this.wurzelKnotenSpielerAnsicht);
 		
 		this.spielNachrichten = new JTextPane();
 		this.spielNachrichten.setEditable(true);
 		
 		JScrollPane nachrichtenContainer = new JScrollPane(this.spielNachrichten);
 		
 		rechtesUnterFenster.add(nachrichtenContainer, BorderLayout.CENTER);
 		
 		unterteiler.setRightComponent(rechtesUnterFenster);
 		
 		this.fenster.add(unterteiler);
 	
 	}
 	
 	/* (non-Javadoc)
 	 * @see aufgabe6.GuiInterface#aenderFigurPosition(aufgabe6.Figur, int, int)
 	 */
 
 	@Override
 	public void aenderFigurPosition(Figur f, int vorher, int nachher) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void starteGui() {
 		this.fenster.pack();
 		//this.passeSpielfeldAn();
 		this.fenster.setVisible(true);	}
 
 	private void passeSpielfeldAn() {
 		int groesse = Math.min(this.spielfeldContainer.getWidth(), this.spielfeldContainer.getHeight());
 		Dimension d = new Dimension(groesse-(groesse%11),groesse-(groesse%11));
 		this.spielfeld.setPreferredSize(d);
 		this.spielfeld.setSize(d); // damit das Spielfeld auch unter Windows sofort in der richtigen Gr��e angezeigt wird... 
 		this.spielfeldContainer.validate();	
 	}
 	
 	@SuppressWarnings({ "serial" })
 	private class GuiSpielfeld extends JPanel implements MouseListener{
 		private byte [][] feld = null;
 		private Point[] figurenPositionen = null;
 		private int[] figurenImHaus = null;
 		
 		public GuiSpielfeld(){
 			this.setBackground(Color.WHITE);
 			
 			this.feld = new byte[][] { 
 					{ 2, 2, 0, 0, 1, 1, 3, 0, 0, 3, 3 },
 					{ 2, 2, 0, 0, 1, 3, 1, 0, 0, 3, 3 },
 					{ 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0 },
 					{ 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0 },
 					{ 2, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1 },
 					{ 1, 2, 2, 2, 2, 0, 4, 4, 4, 4, 1 },
 					{ 1, 1, 1, 1, 1, 5, 1, 1, 1, 1, 4 },
 					{ 0, 0, 0, 0, 1, 5, 1, 0, 0, 0, 0 },
 					{ 0, 0, 0, 0, 1, 5, 1, 0, 0, 0, 0 },
 					{ 5, 5, 0, 0, 1, 5, 1, 0, 0, 4, 4 },
 					{ 5, 5, 0, 0, 5, 1, 1, 0, 0, 4, 4 } };
 			
 			this.figurenPositionen = new Point[]{
 					new Point(4,0),new Point(4,1),new Point(4,2),new Point(4,3),new Point(4,4),new Point(3,4),
 					new Point(2,4),new Point(1,4),new Point(0,4),new Point(0,5),new Point(0,6),new Point(1,6),
 					new Point(2,6),new Point(3,6),new Point(4,6),new Point(4,7),new Point(4,8),new Point(4,9),
 					new Point(4,10),new Point(5,10),new Point(6,10),new Point(6,9),new Point(6,8),new Point(6,7),
 					new Point(6,6),new Point(7,6),new Point(8,6),new Point(9,6),new Point(10,6),new Point(10,5),
 					new Point(10,4),new Point(9,4),new Point(8,4),new Point(7,4),new Point(6,4),new Point(6,3),
 					new Point(6,2),new Point(6,1),new Point(6,0),new Point(5,0),new Point(5,1),new Point(5,2),
 					new Point(5,3),new Point(5,4),new Point(1,5),new Point(2,5),new Point(3,5), new Point(4,5),
 					new Point(5,6), new Point(5,7),new Point(5,8), new Point(5,9),new Point(6,5), new Point(7,5),
 					new Point(8,5), new Point(9,5)
 			};
 			
 			this.figurenImHaus = new int[]{ 0,0,0,0 };
 		}
 		
 		protected void paintComponent(Graphics g){
 			super.paintComponent(g);
 			int laenge = this.getWidth()/11;
 			Graphics2D g2 = (Graphics2D)g;
 			Color tmpColor = g2.getColor();
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			for(int i = 0; i< this.feld.length;i++){
 				for(int j = 0; j < feld[i].length; j++){
 					Color currentColor= berechneFarbe(feld[i][j]);
 					if(currentColor == null) continue;
 					g2.setColor(currentColor);
 					g2.fillOval(j*laenge+laenge/10, i*laenge+laenge/10, laenge-laenge/5, laenge-laenge/5);
 					g2.setColor(Color.BLACK);
 					g2.drawOval(j*laenge+laenge/10, i*laenge+laenge/10, laenge-laenge/5, laenge-laenge/5);			
 				}
 			}
 			ClientSicht sicht = Client.getInstance().getClientRelevanteDaten();
 			for(int i  = 0 ; i< 4; i++){
 				Color currentColor = berechneFarbe(i+2);
 				g2.setColor(currentColor.darker().darker());
 				int amStartZaehler = 0;
 				for(int figur : sicht.getSpielerFiguren()[i]){
 					if(figur > -2){
 						Point position = new Point();
 						if(figur == -1){
 							switch(i){
 							case 0:
 								position.x = 0;
 								position.y = 0;
 								break;
 							case 1:
 								position.x = 9;
 								position.y = 0;
 								break;
 							case 2:
 								position.x = 9;
 								position.y = 9;
 								break;
 							case 3:
 							default:
 								position.x = 0;
 								position.y = 9;
 								break;
 							}
 							switch(amStartZaehler){
 							case 3:
 								position.y++;
 								break;
 							case 1:
 								position.x++;
 								break;
 							case 2:
 								position.x++;
 								position.y++;
 								break;
 							
 							case 0:
 							default:
 								break;
 							}
 							amStartZaehler++;
 						}else{
 							position.x = this.figurenPositionen[figur].y;
 							position.y = this.figurenPositionen[figur].x;
 						}
 						position.x = position.x*laenge+laenge/3;
 						position.y = position.y*laenge+laenge/3;
 						g2.fillOval(position.x, position.y, laenge-laenge/3, laenge-laenge/3);
 						g2.setColor(currentColor.darker());
 						g2.fillOval(position.x, position.y, laenge-laenge/2, laenge-laenge/2);
 					}
 				}
 			}
 			g2.setColor(tmpColor);	
 		}
 
 		private Color berechneFarbe(int i) {
 			Color currentColor = null;
 			switch(i){
 			case 0:
 				break;
 			case 2:
 				currentColor = Color.GREEN;
 				break;
 			case 3:
 				currentColor = Color.RED;
 				break;
 			case 4:
 				currentColor = Color.BLUE;
 				break;
 			case 5:
 				currentColor = Color.YELLOW;
 				break;
 			case 1:
 			default:
 				currentColor = Color.WHITE;
 				break;
 			}
 			return currentColor;
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			int size = this.getWidth();
 			Point position = new Point(e.getX()*11/size, e.getY()*11/size);
 			int spielerNummer = Client.getInstance().getClientRelevanteDaten().getMeineNummer();
 			boolean figurExistiert = false;
 			for(int positionFigur: Client.getInstance().getClientRelevanteDaten().getSpielerFiguren()[spielerNummer]){
 				if(positionFigur==-1){
 					switch (spielerNummer) {
 					case 0:
 						if ((position.x == 0 && position.y == 0)
 								|| (position.x == 1 && position.y == 0)
 								|| (position.x == 1 && position.y == 1)
 								|| (position.x == 0 && position.y == 1)) {
 							figurExistiert = true;
 						}
 						break;
 					case 1:
 						if ((position.x == 0 && position.y == 9)
 								|| (position.x == 1 && position.y == 9)
 								|| (position.x == 1 && position.y == 10)
 								|| (position.x == 0 && position.y == 10)) {
 						figurExistiert = true;
 						}
 						break;
 					case 2:
 						if ((position.x == 9 && position.y == 9)
 								|| (position.x == 9 && position.y == 10)
 								|| (position.x == 10 && position.y == 10)
 								|| (position.x == 10 && position.y == 9)) {
 							figurExistiert = true;
 						}
 						break;
 					case 3:
 						if ((position.x == 9 && position.y == 0)
 								|| (position.x == 10 && position.y == 0)
 								|| (position.x == 10 && position.y == 1)
 								|| (position.x == 9 && position.y == 1)) {
 						figurExistiert = true;
 						}
 						break;
 					default:
 						break;
 					}
 				}else{
 					if(position.equals(this.figurenPositionen[positionFigur])){
 						figurExistiert = true;
 					}
 				}
 				if(figurExistiert){
 					Client.getInstance().getClientKommunikationsThread().sendeBewegungsAufforderung(positionFigur);
 					break;
 				}
 			}
 				
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		
 	}
 }
