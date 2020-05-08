 package de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import de.uni_hamburg.informatik.sep.zuul.client.ClientGUI;
 
 public class StartFenster
 {
 
 	private StartfensterUI _ui;
 
 	private String _ipAdresse;
 	private int _port;
 	private String _spielername;
 
 	/**
 	 * Erstellt ein neues Startfenster zum auswählen zwischen Single - und
 	 * Multiplayer
 	 */
 	public StartFenster()
 	{
 		_ui = new StartfensterUI();
 
 		_ipAdresse = "127.0.0.1";
 		_port = 1090;
		
		_spielername = "Dr. Little";
		
 		initialisiereUI();
 	}
 
 	private void initialisiereUI()
 	{
 		_ui.getSinglePlayerButton().addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					new ClientGUI("RmiServer", "127.0.0.1", 1090, "Dr. Little");
 				}
 				catch(Exception e1)
 				{
 					e1.printStackTrace();
 				}
 				_ui.dispose();
 			}
 		});
 
 		_ui.getMultiPlayerButton().addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				_ui.serverIPeingabe();
 			}
 		});
 
 		_ui.getBestaetigen().addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
				
				_spielername = _ui.getSpielerNameTextField().getText();
				
 				try
 				{
 					new ClientGUI("RmiServer", _ipAdresse, _port, _spielername);
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
				_ui.dispose();
 
 			}
 
 		});
 
 		_ui.getAbbrechenButton().addActionListener(new ActionListener()
 		{
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				System.exit(0);
 			}
 		});
 
 		_ui.getIPTextField().addKeyListener(new KeyListener()
 		{
 
 			@Override
 			public void keyTyped(KeyEvent arg0)
 			{
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0)
 			{
 				pruefeEingabe();
 			}
 
 			@Override
 			public void keyPressed(KeyEvent arg0)
 			{
 				// TODO Auto-generated method stub
 
 			}
 
 		});
 
 		_ui.getPortTextField().addKeyListener(new KeyListener()
 		{
 
 			@Override
 			public void keyTyped(KeyEvent e)
 			{
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e)
 			{
 				pruefeEingabe();
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e)
 			{
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 	}
 
 	private void pruefeEingabe()
 	{
 
 		String eingabeIP = _ui.getIPTextField().getText();
 		String eingabePort = _ui.getPortTextField().getText();
 
 		if(eingabeIP
 				.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")
 				&& eingabePort.matches("109[0-9]"))
 		{
 			_ui.getBestaetigen().setEnabled(true);
 			_ipAdresse = eingabeIP;
 			_port = Integer.parseInt(eingabePort);
 		}
 		else
 		{
 			_ui.getBestaetigen().setEnabled(false);
 		}
 
 	}
 }
