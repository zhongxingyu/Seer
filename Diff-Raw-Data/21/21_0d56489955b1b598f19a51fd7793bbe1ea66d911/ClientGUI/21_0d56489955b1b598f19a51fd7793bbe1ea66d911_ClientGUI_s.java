 package de.uni_hamburg.informatik.sep.zuul.client;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.MalformedURLException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 import de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui.BefehlsPanel;
 import de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui.BildPanel;
 import de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui.Hauptfenster;
 import de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui.KonsolenPanel;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class ClientGUI extends Client
 {
 
 	private Hauptfenster _hf;
 	private KonsolenPanel _kp;
 	private BildPanel _bildPanel;
 	private BefehlsPanel _bp;
 
 	private Raumbilderzeuger _bilderzeuger;
 	private Map<String, JButton> _befehlButtonMap = new HashMap<String, JButton>();
 
 	public ClientGUI(String serverName, String serverIP, int clientport,
 			String clientName) throws MalformedURLException, RemoteException,
 			NotBoundException
 	{
 		super(serverName, serverIP, clientport, clientName);
 
 		if(!serverIP.equals("127.0.0.1"))
 		{
 			startFenster();
 		}
 		else
 		{
 			login();
 			_server.empfangeStartEingabe(_clientName);
 		}
 
 	}
 
 	private void startFenster() throws RemoteException
 	{
 		final JFrame startFrame = new JFrame("Warten auf Start des Spiels");
 
 		JPanel panel = new JPanel();
 		startFrame.setMinimumSize(new Dimension(300, 150));
 		startFrame.setLocationRelativeTo(null);
 
 		final JButton _startButton = new JButton("Los gehts!");
 
 		panel.add(_startButton);
 
 		startFrame.setContentPane(panel);
 
 		startFrame.setVisible(true);
 
 		login();
 
 		_startButton.addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				_startButton.setEnabled(false);
 
 				try
 				{
 					_server.empfangeStartEingabe(_clientName);
 				}
 				catch(RemoteException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				finally
 				{
 					startFrame.dispose();
 				}
 
 			}
 		});
 
 	}
 
 	/**
 	 * Ruft der Server am Client auf, wenn er das Startsignal emfängt.
 	 */
 	public void starteClientUI(ClientPaket paket) throws RemoteException
 	{
 		_bilderzeuger = new Raumbilderzeuger();
 		_bilderzeuger.setPaket(paket);
 		initialisiereUI();
 	}
 
 	@Override
 	public void schreibeText(String text)
 	{
 		for(String zeile : text.split("\n"))
 			schreibeTextNL(zeile);
 	}
 
 	/**
 	 * @param text
 	 */
 	private void schreibeTextNL(String text)
 	{
 		JTextArea anzeige = _kp.getAnzeigeArea();
 		anzeige.append(text + "\r\n");
 		anzeige.setCaretPosition(anzeige.getDocument().getLength());
 	}
 
 	@Override
 	public synchronized boolean zeigeAn(final ClientPaket paket)
 	{
 
 		SwingUtilities.invokeLater(new Runnable()
 		{
 
 			@Override
 			public void run()
 			{
 				aktualisiereUI(paket, false);
 			}
 		});
 
 		//nötig??
 		return true;
 
 	}
 
 	@Override
 	public synchronized boolean zeigeVorschau(final ClientPaket paket)
 			throws RemoteException
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			//TODO Jetzt bekommen wir zwei Pakete. In Ordnung? Wie damit umgehen?
 			@Override
 			public void run()
 			{
 				aktualisiereUI(paket, true);
 			}
 		});
 
 		return true;
 
 	}
 
 	/**
 	 * @param paket
 	 */
 	private void aktualisiereUI(ClientPaket paket, boolean vorschau)
 	{
 		aktualisiereMoeglicheAusgaenge(paket.getMoeglicheAusgaenge());
 		_bp.setLebensenergie(paket.getLebensEnergie());
 
 		String nachricht = paket.getNachricht();
 		if(nachricht != null)
 			schreibeText(nachricht);
 
 		setzeBefehlsverfuegbarkeit(paket.getVerfuegbareBefehle());
 
 		int val = getOptimalIconSize();
 
 		if(paket.isShowWinScreen())
 		{
 			_bildPanel.setRaumanzeige(_bilderzeuger.getWinScreen(val));
 			return;
 		}
 		else if(paket.isShowLoseScreen())
 		{
 			_bildPanel.setRaumanzeige(_bilderzeuger.getGameOverScreen(val));
 			return;
 		}
 
		_bildPanel.zeigeSchauen(_bilderzeuger.getRaumansicht(val, paket,
				vorschau));
 	}
 
 	private void setzeBefehlsverfuegbarkeit(
 			Map<String, Boolean> verfuegbareBefehle)
 	{
 		for(Entry<String, Boolean> entry : verfuegbareBefehle.entrySet())
 		{
 			String befehl = entry.getKey();
 			boolean enabled = entry.getValue();
 
 			System.err.println(befehl + ": " + enabled);
 
 			JButton button = _befehlButtonMap.get(befehl);
 			if(button != null && button != _bp.getQuitButton())
 				button.setEnabled(enabled);
 		}
 	}
 
 	private void aktualisiereMoeglicheAusgaenge(String[] ausgaenge)
 	{
 		List<String> list = Arrays.asList(ausgaenge);
 
 		boolean n = list.contains(TextVerwalter.RICHTUNG_NORDEN);
 		boolean o = list.contains(TextVerwalter.RICHTUNG_OSTEN);
 		boolean s = list.contains(TextVerwalter.RICHTUNG_SUEDEN);
 		boolean w = list.contains(TextVerwalter.RICHTUNG_WESTEN);
 
 		_bildPanel.getTuerNordButton().setVisible(n);
 		_bildPanel.getTuerOstButton().setVisible(o);
 		_bildPanel.getTuerSuedButton().setVisible(s);
 		_bildPanel.getTuerWestButton().setVisible(w);
 
 	}
 
 	private final class ActionListenerBefehlAusfuehren implements
 			ActionListener
 	{
 		private String _befehlszeile;
 
 		public ActionListenerBefehlAusfuehren(String befehlszeile)
 		{
 			_befehlszeile = befehlszeile;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			sendeEingabe(_befehlszeile);
 		}
 
 		/**
 		 * @return the befehlszeile
 		 */
 		public String getBefehlszeile()
 		{
 			return _befehlszeile;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	void initialisiereUI()
 	{
 		_bp = new BefehlsPanel();
 		_kp = new KonsolenPanel();
 		_bildPanel = new BildPanel();
 		_hf = new Hauptfenster(_bildPanel, _kp, _bp);
 
 		_hf.setVisible(true);
 		_kp.getEnterButton().addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				String str = _kp.getEingabeZeile().getText();
 				_kp.getEingabeZeile().setText("");
 
 				try
 				{
 					verarbeiteEingabe(str);
 				}
 				catch(RemoteException e1)
 				{
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 			}
 		});
 
 		_kp.getEingabeZeile().addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				_kp.getEnterButton().doClick();
 			}
 		});
 
 		_bildPanel.getTuerNordButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_GEHEN
 						+ " " + TextVerwalter.RICHTUNG_NORDEN));
 
 		_bildPanel.getTuerOstButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_GEHEN
 						+ " " + TextVerwalter.RICHTUNG_OSTEN));
 
 		_bildPanel.getTuerSuedButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_GEHEN
 						+ " " + TextVerwalter.RICHTUNG_SUEDEN));
 
 		_bildPanel.getTuerWestButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_GEHEN
 						+ " " + TextVerwalter.RICHTUNG_WESTEN));
 
 		_bp.getQuitButton()
 				.addActionListener(
 						new ActionListenerBefehlAusfuehren(
 								TextVerwalter.BEFEHL_BEENDEN));
 
 		_bp.getHelpButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_HILFE));
 
 		_bp.getEssenAusTascheButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_ESSEN
 						+ " " + TextVerwalter.ORT_TASCHE));
 
 		String befehlEssenString = TextVerwalter.BEFEHL_ESSEN + " "
 				+ TextVerwalter.ORT_TASCHE + " ";
 		_bp.getEssenTascheGutButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(befehlEssenString
 						+ "guter krümel"));
 		_bp.getEssenTascheSchlechtButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(befehlEssenString
 						+ "schlechter krümel"));
 		_bp.getEssenTascheUnbekanntButton()
 				.addActionListener(
 						new ActionListenerBefehlAusfuehren(befehlEssenString
 								+ "krümel"));
 
 		_bp.getEssenBodenButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_ESSEN
 						+ " " + TextVerwalter.ORT_BODEN));
 
 		_bp.getNehmenButton()
 				.addActionListener(
 						new ActionListenerBefehlAusfuehren(
 								TextVerwalter.BEFEHL_NEHMEN));
 
 		_bp.getGibButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(
 						TextVerwalter.BEFEHL_UNTERSUCHE));
 
 		_bp.getLadenButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_LADEN));
 
 		_bp.getFuettereButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(
 						TextVerwalter.BEFEHL_FUETTERE));
 
 		_bp.getFuettereGutButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(
 						TextVerwalter.BEFEHL_FUETTERE_GUT));
 		_bp.getFuettereSchlechtButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(
 						TextVerwalter.BEFEHL_FUETTERE_SCHLECHT));
 		_bp.getFuettereUnbekanntButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(
 						TextVerwalter.BEFEHL_FUETTERE_UNBEKANNT));
 
 		_bp.getInventarButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(
 						TextVerwalter.BEFEHL_INVENTAR));
 
 		_bp.getAblegenButton()
 				.addActionListener(
 						new ActionListenerBefehlAusfuehren(
 								TextVerwalter.BEFEHL_ABLEGEN));
 
 		_bp.getAblegenGutButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_ABLEGEN
 						+ " guter krümel"));
 		_bp.getAblegenSchlechtButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_ABLEGEN
 						+ " schlechter krümel"));
 		_bp.getAblegenUnbekanntButton().addActionListener(
 				new ActionListenerBefehlAusfuehren(TextVerwalter.BEFEHL_ABLEGEN
 						+ " krümel"));
 
 		_bildPanel.addComponentListener(new ComponentAdapter()
 		{
 
 			@Override
 			public void componentResized(ComponentEvent arg0)
 			{
 				int val = getOptimalIconSize();
 
 				_bildPanel.setRaumanzeige(_bilderzeuger.zeichneBildErneut(val));
 			}
 
 		});
 
 		_bildPanel.getTuerNordButton().addMouseListener(new MouseAdapter()
 		{
 
 			@Override
 			public void mouseClicked(MouseEvent m)
 			{
 				if(SwingUtilities.isRightMouseButton(m)
 						&& m.getClickCount() == 1)
 				{
 					sendeEingabe("schaue nord");
 				}
 
 			}
 		});
 
 		_bildPanel.getTuerOstButton().addMouseListener(new MouseAdapter()
 		{
 
 			@Override
 			public void mouseClicked(MouseEvent m)
 			{
 				if(SwingUtilities.isRightMouseButton(m)
 						&& m.getClickCount() == 1)
 				{
 					sendeEingabe("schaue ost");
 				}
 
 			}
 		});
 
 		_bildPanel.getTuerSuedButton().addMouseListener(new MouseAdapter()
 		{
 
 			@Override
 			public void mouseClicked(MouseEvent m)
 			{
 				if(SwingUtilities.isRightMouseButton(m)
 						&& m.getClickCount() == 1)
 				{
 					sendeEingabe("schaue süd");
 				}
 
 			}
 		});
 
 		_bildPanel.getTuerWestButton().addMouseListener(new MouseAdapter()
 		{
 
 			@Override
 			public void mouseClicked(MouseEvent m)
 			{
 				if(SwingUtilities.isRightMouseButton(m)
 						&& m.getClickCount() == 1)
 				{
 					sendeEingabe("schaue west");
 				}
 			}
 		});
 
 		_bildPanel.getSchauenLabel().addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent m)
 			{
 				_bildPanel.versteckeSchauen();
 			}
 		});
 
 		createActionListenerMap();
 
 	}
 
 	private void createActionListenerMap()
 	{
 		createActionListenerMap(_bp.getNormalButtons());
 		createActionListenerMap(_bp.getSystemButtons());
 		createActionListenerMap(_bp.getExtraButtons());
 	}
 
 	private void createActionListenerMap(JButton[] buttons)
 	{
 		for(JButton button : buttons)
 		{
 			for(ActionListener listener : button.getActionListeners())
 			{
 				if(listener instanceof ActionListenerBefehlAusfuehren)
 				{
 					ActionListenerBefehlAusfuehren actionListenerBefehlAusfuehren = (ActionListenerBefehlAusfuehren) listener;
 					_befehlButtonMap.put(
 							actionListenerBefehlAusfuehren.getBefehlszeile(),
 							button);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	private void UIsetEnabled(boolean value)
 	{
 		_kp.getEingabeZeile().setEnabled(value);
 		_kp.getEnterButton().setEnabled(value);
 		_bp.getGibButton().setEnabled(value);
 		_bp.getEssenAusTascheButton().setEnabled(value);
 		_bp.getEssenBodenButton().setEnabled(value);
 		_bp.getNehmenButton().setEnabled(value);
 		_bp.getHelpButton().setEnabled(value);
 		_bp.getQuitButton().setEnabled(value);
 		_bp.getFuettereButton().setEnabled(value);
 		_bp.getAblegenButton().setEnabled(value);
 		_bp.getInventarButton().setEnabled(value);
 		_bildPanel.getTuerNordButton().setEnabled(value);
 		_bildPanel.getTuerOstButton().setEnabled(value);
 		_bildPanel.getTuerSuedButton().setEnabled(value);
 		_bildPanel.getTuerWestButton().setEnabled(value);
 
 	}
 
 	public void schliesseFenster()
 	{
 		_hf.dispose();
 	}
 
 	public void spielen(String level) throws RemoteException
 	{
 		UIsetEnabled(true);
 
 		super.run();
 
 	}
 
 	/**
 	 * @param befehlszeile
 	 * 
 	 */
 	private void sendeEingabe(String befehlszeile)
 	{
 		try
 		{
 			verarbeiteEingabe(befehlszeile);
 		}
 		catch(RemoteException e1)
 		{
 			// TODO Auto-generated catch block
 			// TODO: exception verarbeiten
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private int getOptimalIconSize()
 	{
 		int val = 0;
 		if(_bildPanel.getWidth() > _bildPanel.getHeight()
 				&& _bildPanel.getHeight() != 0 && _bildPanel.getWidth() != 0)
 			val = _bildPanel.getLabelFuerIcon().getHeight();
 		else if(_bildPanel.getHeight() != 0 && _bildPanel.getWidth() != 0)
 			val = _bildPanel.getLabelFuerIcon().getWidth();
 		return val;
 	}
 }
