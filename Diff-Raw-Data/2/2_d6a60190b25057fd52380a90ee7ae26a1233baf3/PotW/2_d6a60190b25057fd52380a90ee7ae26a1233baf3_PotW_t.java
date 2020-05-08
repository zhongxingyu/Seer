 /* TODO:
  * Manuelles Auswählen der Formation (Lineuppanel lässt sich nicht ändern,
  * aber ähnliche Funktion wie beim normalen Aufstellungspanel wäre möglich
  *
  * done: Taktik, die der einzelne Spieler benutzt hat anzeigen (Off/Def/Normal)
  * Liste mit Tore pro Spieler pro Spieltag
  * Players of the Year
  *
 
 Hallo,
 
 ich hab mich gewundert, weil ich jetzt (in Matchweek 4) nur eine
 
 POTW des ersten Spieltags bekomme, und dann hab ich mir die Mannschaften
 angeschaut:
 es ist eine andere Liga. Ich hab nämlich für den ersten Spieltag auch die
 Liga, in der mein alter
 Mitstreiter aus der VII. Liga jetzt spielt, geladen. D.h. ich hab unter
 "Ligatabelle" mehrere Ligen
 zur Auswahl.
 Kann man es vielleicht einrichten, dass man
 in Deinem Plugin auch noch die
 Liga angibt, für die die POTW errechnet
 werden?
 
 
 */
 package hoplugins;
 
 import hoplugins.potw.MatchLineupPlayer;
 
 import plugins.*;
 import java.sql.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.Color;
 import java.awt.event.*;
 
 public class PotW implements ActionListener,IPlugin,ChangeListener, plugins.IRefreshable {
 	private double version = 0.221;
 
 	private javax.swing.JPanel      m_jpPanel       =   null;
 	private plugins.IHOMiniModel    m_clModel       =   null;
 
 	private javax.swing.JSpinner	cSpieltag = null;
 	private javax.swing.JComboBox	cLigaSaison = null;
 	private javax.swing.JButton		cPoty = null;
 
 	plugins.IDebugWindow debugWindow = null;
 	private LineupPanel lup = new LineupPanel(LineupPanel.LINEUP_NORMAL_SEQUENCE);
 	private int spieltag, saison;
 	private int maxSpieltag;
 	public PotW() {}
 
 
 
 	public String getName() {
 		return "Players of the Week V " + version + "by Ivian (Ivian@gmx.net)";
 	}
 
 	/**
 	 * Plugin version.
 	 */
     public double getVersion() {
         return version;
     }
 
 	public void refresh ()
 	{
 		spieltag = ((Integer) cSpieltag.getValue()).intValue();
 		//debugWindow.setVisible ( true );
 		debugWindow.append(spieltag + "\n");
 		fillLineup(lup,getSpielerListe(debugWindow));
 	}
 
 
 	public void getBestonPositions(MatchLineupPlayer[][] sl,MatchLineupPlayer[] mlp, int i, int pos1, int pos2)
 	{
 		if (sl[i][pos1].nom > mlp[pos1].nom) {
 									mlp[pos1] = sl[i][pos1];
 								}
 								else if (sl[i][pos1].nom > mlp[pos2].nom) {
 									mlp[pos2] = sl[i][pos1];
 								}
 								if (sl[i][pos2].nom > mlp[pos2].nom) {
 									mlp[pos2] = sl[i][pos2];
 								}
 								else if (sl[i][pos2].nom > mlp[pos1].nom) {
 									mlp[pos1] = sl[i][pos2];
 								}
 	}
 
 	public MatchLineupPlayer[] assignNom2ndPass(MatchLineupPlayer[][] sl) {
 
 		MatchLineupPlayer[] mlp = new MatchLineupPlayer[11];
 		mlp = sl[0];
 
 		for (int i = 0; i < maxSpieltag; i++) {
 						//goalie
 						if (sl[i][0].nom > mlp[0].nom) {
 							mlp[0] = sl[i][0];
 						}
 						//wingbacks
 			getBestonPositions(sl,mlp,i,1,2);
 						//central defenders
 			getBestonPositions(sl,mlp,i,3,4);
 						//ims
 			getBestonPositions(sl,mlp,i,5,6);
 						//wings
 			getBestonPositions(sl,mlp,i,7,8);
 						//forwards
 			getBestonPositions(sl,mlp,i,9,10);
 				}
 
 		for (int i = 0; i < 11; i++){
 		mlp[i].nname = mlp[i].nname + " (" + mlp[i].nom + ")"; }
 
 		return mlp;
 	}
 
 	public void generatePoty ()
 		{
 			int backupSpieltag = spieltag;
 			MatchLineupPlayer[][] sl = new MatchLineupPlayer[maxSpieltag][];
 
 			for (spieltag = 1; spieltag <= maxSpieltag; spieltag++)
 			{
 				sl[spieltag - 1] = calcBestPlayers(getSpielerListe(debugWindow));
 			}
 
 			//analysiere die oben gegebenen Listen von sl
 
 			for (int pos = 0; pos < 11; pos++) {
 			for (int i = 0; i < maxSpieltag; i++) {
 
 				for (int j = i+1; j < maxSpieltag; j++) {
 
 						//if ((sl[i][pos].vname.equals(sl[j][pos].vname)) && (sl[i][pos].nname.equals(sl[j][pos].nname))) {
 						if (sl[i][pos].SpielerID == sl[j][pos].SpielerID) {
 
 							sl[i][pos].nom++;
 							sl[j][pos].nom = -100;
 							debugWindow.append(sl[i][pos].vname + " " + sl[i][pos].nname + " - " + sl[j][pos].vname + " " + sl[j][pos].vname +  " i:" + i + " j: " + j + " nom: " + sl[i][pos].nom);
 						}
 					}
 				}
 			}
 
 			MatchLineupPlayer[] mlp = assignNom2ndPass(sl);
 
 
 			fillLineup(lup,mlp,true);
 			spieltag = backupSpieltag;
 			debugWindow.append("Poty");
 		}
 
 
 	public void stateChanged (ChangeEvent e)
 	{
 
 		  if ( e.getSource ().equals ( cSpieltag ) )
 		   {
 			refresh();
 			cSpieltag.transferFocus();
 			  }
 	  }
 
 
 	public void actionPerformed (ActionEvent e) {
 		if ( e.getSource ().equals ( cPoty ) )
 				  {
 				   generatePoty();
 				   //debugWindow.append("Poty");
 					 }
 
 		else if ( e.getSource ().equals ( cLigaSaison ) )
 		{
 			debugWindow.append("CLigaSaison");
 			if ( m_clModel.getBasics().getSeason() == ((Integer) cLigaSaison.getSelectedItem()).intValue()) {
 				if (spieltag > maxSpieltag) { spieltag = 1; }
 				//cSpieltag = 1 to maxSpieltag;
 				javax.swing.SpinnerNumberModel cSNM = new javax.swing.SpinnerNumberModel(spieltag,1,maxSpieltag,1);
 				cSpieltag.setModel(cSNM);
 				debugWindow.append("MaxSpieltag: " + maxSpieltag + " - " + spieltag);
 			}
 			else
 			{
 				//cSpieltag = 1 to 14
 				javax.swing.SpinnerNumberModel cSNM = new javax.swing.SpinnerNumberModel(spieltag,1,14,1);
 				cSpieltag.setModel(cSNM);
 				debugWindow.append("Komplette Saison: " + spieltag);
 			}
 		}
 	}
 
 	public void start (plugins.IHOMiniModel hOMiniModel)
 	   {
 		m_clModel       =   hOMiniModel;
 		IJDBCAdapter db = m_clModel.getAdapter();
 
     	spieltag = m_clModel.getBasics().getSpieltag() - 1 ;
     	maxSpieltag = spieltag;
 		saison = m_clModel.getBasics().getSeason() ;
 		m_jpPanel = hOMiniModel.getGUI ().createGrassPanel ();
 		debugWindow = m_clModel.getGUI ().createDebugWindow ( new java.awt.Point( 100, 200 ), new java.awt.Dimension( 200,  100 ) );
 
 		cPoty = new javax.swing.JButton("Players of the Year");
 		cPoty.addActionListener(this);
 		cSpieltag = new javax.swing.JSpinner();
 		cLigaSaison = new javax.swing.JComboBox();
 		javax.swing.SpinnerNumberModel cSNM = new javax.swing.SpinnerNumberModel(spieltag,1,spieltag,1);
 		cSpieltag.setModel(cSNM);
 		cSpieltag.addChangeListener( this );
 		cSpieltag.setPreferredSize( new java.awt.Dimension (40,20));
 		cSpieltag.setFocusable(false);
 		cLigaSaison.addActionListener(this);
 
 		String sqlstmt;
 		java.sql.ResultSet rs;
 
 
 		sqlstmt = "SELECT LIGANAME,SAISON FROM SPIELPLAN";
 		rs = db.executeQuery(sqlstmt);
 
 
 				try {
 				while (rs.next() == true) {
 				 debugWindow.append(rs.getString("LIGANAME"));
 				 debugWindow.append("" + rs.getInt("SAISON"));
 				 cLigaSaison.addItem(new Integer(rs.getInt("SAISON")));
 				}    	}
 				catch (SQLException e) {
 							debugWindow.append("SQL-Fehler !! " + e + "\n");
 				}
 				//cLigaSaison.addItem(new Integer(24));
 
 		JLabel jl = new JLabel("Matchweek: ");
 		JLabel sl = new JLabel("Season: ");
 		JPanel north = hOMiniModel.getGUI ().createGrassPanel ();
 		jl.setForeground(java.awt.Color.white);
 		jl.setLabelFor(cSpieltag);
 		sl.setForeground(java.awt.Color.white);
 		sl.setLabelFor(cLigaSaison);
 		fillLineup(lup,getSpielerListe(debugWindow));
 		north.setPreferredSize(  new java.awt.Dimension( 120, 30 ));
 		north.add(jl);
 		north.add(cSpieltag);
 		north.add(sl);
 		north.add(cLigaSaison);
 		//north.add(cPoty);
 		north.setOpaque(true);
 	   lup.setSize( 400, 400 );
 	   m_jpPanel.setLayout ( new java.awt.BorderLayout() );
 	   m_jpPanel.add ( north,java.awt.BorderLayout.NORTH);
 		JLabel infoLabel = new JLabel ("PotW-Plugin by Ivian(Ivian@gmx.net) - FC Altrip(339495)");
 	   infoLabel.setForeground(java.awt.Color.white);
 	   infoLabel.setHorizontalAlignment ( javax.swing.JLabel.RIGHT );
 	   m_jpPanel.add ( lup,java.awt.BorderLayout.CENTER );
 	   m_jpPanel.add ( infoLabel,java.awt.BorderLayout.SOUTH );
 		m_clModel.getGUI ().addTab ( "PotW v" + version,  m_jpPanel );
 
 			  m_clModel.getGUI ().registerRefreshable ( this );
 
 
 //		debugWindow.setVisible(true);
 
 	   }
 
 	private LinkedList<MatchLineupPlayer> getSpielerListe(IDebugWindow debugWindow) {
 		IJDBCAdapter db = m_clModel.getAdapter();
 
 		saison = ((Integer) cLigaSaison.getSelectedItem()).intValue();
 		String sqlstmt;
 		java.sql.ResultSet rs;
 
 				 sqlstmt = "SELECT * FROM PAARUNG WHERE SPIELTAG='" + spieltag + "' AND SAISON='" + saison + "'";
 				 rs = db.executeQuery(sqlstmt);
 
 				boolean moreEntries = true;
 				int[] matchIDs = new int[4];
 				String[] heimteams = new String[4];
 				int[] heimteamsID = new int[4];
 				String[] awayteams = new String[4];
 				int i = 0;
 
 			 //getMatchIDs
 				try {
 
 				while (moreEntries == true && i <= 3) {
 					moreEntries = rs.next();
 					matchIDs[i] = (rs.getInt("MATCHID"));
 					heimteams[i] = (rs.getString("HEIMNAME"));
 					heimteamsID[i] = (rs.getInt("HEIMID"));
 					awayteams[i] = (rs.getString("GASTNAME"));
 					i++;
 				}
 				}
 				catch (SQLException e) {
 					debugWindow.append("SQL-Fehler !! " + e + "\n");
 				}
 
 			   //getPlayersthatplayed
 				LinkedList<MatchLineupPlayer> spieler = new LinkedList<MatchLineupPlayer>();
 
 
 				for (int j = 0;j<=3;j++)
 				{
 					sqlstmt = "SELECT * FROM MATCHLINEUPPLAYER WHERE MATCHID='"+ matchIDs[j] + "'";
 
 					rs = db.executeQuery(sqlstmt);
 					moreEntries = true;
 					try {
 
 						while (moreEntries == true) {
 						moreEntries = rs.next();
 						if (rs.getInt("TEAMID") == heimteamsID[j]) {
 						spieler.add(new MatchLineupPlayer(rs,heimteams[j]));
 						}
 						else {
 						spieler.add(new MatchLineupPlayer(rs,awayteams[j]));
 						}
 
 						}
 						}
 					catch (SQLException e) {
 						debugWindow.append("SQL-Fehler !! " + e + "\n");
 						}
 				}
 		//debugWindow.append("getSpielerListe Ende");
 		return spieler;
 	}
 
 	private MatchLineupPlayer[] calcBestPlayers(LinkedList<MatchLineupPlayer> spieler) {
 		MatchLineupPlayer g,lwb,rwb,cd1,cd2,im1,im2,s1,s2,lw,rw = null;
 
 				//debugWindow.append("Spieler: " + spieler.size() + "\n");
 
 				MatchLineupPlayer fake = new MatchLineupPlayer();
 
 					g = fake;
 					lwb = fake;
 					rwb = fake;
 					cd1 = fake;
 					cd2 = fake;
 					lw = fake;
 					rw = fake;
 					im1 = fake;
 					im2 = fake;
 					s2 = fake;
 					s1 = fake;
 					//ispieler = hOMiniModel.getSpieler(SpielerID);
 				//debugWindow.append(g.TeamID + "\n");
 
 				ListIterator<MatchLineupPlayer> p = spieler.listIterator(0);
 				while (p.hasNext()) {
 					MatchLineupPlayer mlp = p.next();
					if ((mlp.PositionCode == ISpielerPosition.KEEPER) && (mlp.Rating > g.Rating)) { g = mlp;	}
 					//Außenverteidiger
 					if ((mlp.PositionCode >= 4) && (mlp.PositionCode <= 7) && (mlp.Rating > lwb.Rating)) { if (mlp.Rating > rwb.Rating) { lwb = rwb; rwb = mlp; } else { lwb = mlp; }	 }
 					//Flügel
 					if ((mlp.PositionCode >= 12) && (mlp.PositionCode <= 15) && (mlp.Rating > lw.Rating)) { if (mlp.Rating > rw.Rating) { lw = rw; rw = mlp; } else { lw = mlp; } }
 					//Mittelfeld
 					if ((mlp.PositionCode >= 8) && (mlp.PositionCode <= 11) && (mlp.Rating > im1.Rating)) { if (mlp.Rating > im2.Rating) { im1 = im2; im2 = mlp; } else { im1 = mlp; }}
 					//Innenverteidiger
 					if ((mlp.PositionCode >= 1) && (mlp.PositionCode <= 3) && (mlp.Rating > cd1.Rating)) { if (mlp.Rating > cd2.Rating) { cd1 = cd2; cd2 = mlp; } else { cd1 = mlp; }}
 					//Sturm
 					if ((mlp.PositionCode >= 16) && (mlp.PositionCode <= 17) && (mlp.Rating > s1.Rating)) { if (mlp.Rating > s2.Rating) { s1 = s2; s2 = mlp; } else { s1 = mlp; }}
 
 				}
 
 			MatchLineupPlayer[] mlp = new MatchLineupPlayer[11];
 			mlp[0] = g;
 			mlp[1] = lwb;
 			mlp[2] = rwb;
 			mlp[3] = cd1;
 			mlp[4] = cd2;
 			mlp[5] = im1;
 			mlp[6] = im2;
 			mlp[7] = lw;
 			mlp[8] = rw;
 			mlp[9] = s1;
 			mlp[10] = s2;
 			return mlp;
 
 	}
 
 	private void fillLineup( plugins.LineupPanel lineupPanel, MatchLineupPlayer[] sl)
 	{
 		fillLineup(lineupPanel,sl,false);
 	}
 
 
 	private void fillLineup( plugins.LineupPanel lineupPanel, MatchLineupPlayer[] sl,boolean noStars)
 			{
 				if (noStars == false) {
 				fillPanel( lineupPanel.getKeeperPanel (), sl[0]);
 				fillPanel( lineupPanel.getLeftWingbackPanel (), sl[1]);
 				fillPanel( lineupPanel.getLeftCentralDefenderPanel (),sl[3]);
 				fillPanel( lineupPanel.getRightCentralDefenderPanel (), sl[4]);
 				fillPanel( lineupPanel.getRightWingbackPanel (),  sl[2]);
 				fillPanel( lineupPanel.getLeftWingPanel (), sl[7]);
 				fillPanel( lineupPanel.getLeftMidfieldPanel (), sl[5]);
 				fillPanel( lineupPanel.getRightMidfieldPanel (), sl[6]);
 				fillPanel( lineupPanel.getRightWingPanel (),  sl[8]);
 				fillPanel( lineupPanel.getLeftForwardPanel (), sl[9]);
 				fillPanel( lineupPanel.getRightForwardPanel (),  sl[10]);
 				}
 				else {
 				fillPanel( lineupPanel.getKeeperPanel (), sl[0],true);
 				fillPanel( lineupPanel.getLeftWingbackPanel (), sl[1],true);
 				fillPanel( lineupPanel.getLeftCentralDefenderPanel (),sl[3],true);
 				fillPanel( lineupPanel.getRightCentralDefenderPanel (), sl[4],true);
 				fillPanel( lineupPanel.getRightWingbackPanel (),  sl[2],true);
 				fillPanel( lineupPanel.getLeftWingPanel (), sl[7],true);
 				fillPanel( lineupPanel.getLeftMidfieldPanel (), sl[5],true);
 				fillPanel( lineupPanel.getRightMidfieldPanel (), sl[6],true);
 				fillPanel( lineupPanel.getRightWingPanel (),  sl[8],true);
 				fillPanel( lineupPanel.getLeftForwardPanel (), sl[9],true);
 				fillPanel( lineupPanel.getRightForwardPanel (),  sl[10],true);
 				}
 	  			lineupPanel.setTeamName ( "Players of the Week - Week " + spieltag );
 			}
 
 	private void fillLineup( plugins.LineupPanel lineupPanel, LinkedList<MatchLineupPlayer> spieler ) {
 		MatchLineupPlayer[] sl = calcBestPlayers(spieler);
 		fillLineup(lineupPanel,sl);
 
 	}
 
 	private int toInt(float i) {
 		return new Float(i * 2).intValue();
 	}
 
 
 	private JLabel createLabel(String text,Color farbe, int Bordertype) {
 		JLabel bla = new JLabel(text);
 		bla.setHorizontalAlignment( JLabel.CENTER);
 		bla.setForeground(farbe);
 		bla.setBorder( javax.swing.BorderFactory.createEtchedBorder(Bordertype));
 		return bla;
 	}
 
 	private void fillPanel( javax.swing.JPanel panel, MatchLineupPlayer mlp)
 	{
 		fillPanel(panel,mlp,false);
 	}
 
 private void fillPanel( javax.swing.JPanel panel, MatchLineupPlayer mlp,boolean noStars)
 		{
 			JLabel spielername;
 			JLabel teamname;
 			JPanel spielerdetails;
 			JPanel sternzahl;
 			JPanel mainPanel;
 			JLabel position;
 			String posi;
 
 			int m = mlp.PositionCode;
 
 			if (m == 0) { posi = "Keeper"; }
 			else if (m == 1) { posi = "Central Defender"; }
 			else if (m == 2) { posi = "Central Defender - Offensive"; }
 			else if (m == 3) { posi = "Central Defender - To Wing"; }
 			else if (m == 4) { posi = "Wingback"; }
 			else if (m == 5) { posi = "Wingback - Offensive"; }
 			else if (m == 6) { posi = "Wingback - To Mid"; }
 			else if (m == 7) { posi = "Wingback - Defensive"; }
 			else if (m == 8) { posi = "Inner Midfield"; }
 			else if (m == 9) { posi = "Inner Midfield - Offensive"; }
 			else if (m == 10) { posi = "Inner Midfield - Defensive"; }
 			else if (m == 11) { posi = "Inner Midfield - To Wing"; }
 			else if (m == 12) { posi = "Wing"; }
 			else if (m == 13) { posi = "Wing - Offensive"; }
 			else if (m == 14) { posi = "Wing - Defensive"; }
 			else if (m == 15) { posi = "Wing - To Mid"; }
 			else if (m == 16) { posi = "Striker"; }
 			else if (m == 17) { posi = "Striker - Defensive"; }
 			else { posi = "Unknown"; }
 
 			panel.removeAll();
 
 			spielername = createLabel( mlp.vname + mlp.nname,Color.black,javax.swing.border.EtchedBorder.LOWERED );
 			teamname = createLabel( mlp.teamname,Color.black,javax.swing.border.EtchedBorder.LOWERED );
 
 			position = createLabel( posi,Color.black,javax.swing.border.EtchedBorder.RAISED );
 
 			spielerdetails = new JPanel();
 			spielerdetails.setBorder( javax.swing.BorderFactory.createEtchedBorder()  );
 			spielerdetails.setBackground( Color.white);
 			spielerdetails.setLayout ( new java.awt.BorderLayout() );
 			spielerdetails.add( spielername, java.awt.BorderLayout.NORTH );
 			spielerdetails.add( teamname, java.awt.BorderLayout.SOUTH );
 
 			sternzahl = m_clModel.getGUI().createStarPanel(toInt(mlp.Rating),true);
 			sternzahl.setOpaque(true);
 			sternzahl.setBorder( javax.swing.BorderFactory.createEtchedBorder()  );
 
 
 			mainPanel = m_clModel.getGUI().createImagePanel();
 			mainPanel.setLayout ( new java.awt.BorderLayout() );
 			mainPanel.setBorder( javax.swing.BorderFactory.createRaisedBevelBorder()  );
 			mainPanel.setPreferredSize ( new java.awt.Dimension( 180, 80 ) );
 			mainPanel.add( position, java.awt.BorderLayout.NORTH );
 			mainPanel.add( spielerdetails, java.awt.BorderLayout.CENTER );
 
 			if (noStars == false) { mainPanel.add(sternzahl,java.awt.BorderLayout.SOUTH); }
 
 			panel.add(mainPanel);
 					}
 }
 
