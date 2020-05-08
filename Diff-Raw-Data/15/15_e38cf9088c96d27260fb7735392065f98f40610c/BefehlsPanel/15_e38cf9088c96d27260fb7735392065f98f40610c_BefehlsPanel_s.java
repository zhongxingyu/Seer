 package de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.sun.xml.internal.ws.util.StringUtils;
 
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class BefehlsPanel extends JPanel
 {
 	//TODO ANZAHLBUTTONS BEI NEUEN BEFEHLEN ANPASSEN!!!
 	//	public static final String PATH = BefehlsPanel.class.getResource("bilder/")
 	//			.getPath();
 	public static final int ZEILENANZAHL = 5;
 	public static final int ABSTAND_NORMAL = 5;
 	public static final int ABSTAND_EXTRA = 2;
 	private final ImageIcon GUT = new ImageIcon(getClass().getResource(
 			"bilder/kruemel_gut.png"));
 	private final ImageIcon SCHLECHT = new ImageIcon(getClass().getResource(
 			"bilder/kruemel_schlecht.png"));
 	private final ImageIcon UNBEKANNT = new ImageIcon(getClass().getResource(
 			"bilder/kruemel.png"));
 	private JButton _quitButton;
 	private JButton _helpButton;
 	private JButton _essenTascheButton;
 	private JButton _essenTascheGutButton;
 	private JButton _essenTascheSchlechtButton;
 	private JButton _essenTascheUnbekanntButton;
 	private JButton _nehmenButton;
 	private JButton _gibButton;
 	private JButton _essenBodenButton;
 	private JButton _ladenButton;
 	private JButton _fuettereButton;
 	private JButton _fuettereGutButton;
 	private JButton _fuettereSchlechtButton;
 	private JButton _fuettereUnbekanntButton;
 	private JButton _ablegenButton;
 	private JButton _ablegenGutButton;
 	private JButton _ablegenSchlechtButton;
 	private JButton _ablegenUnbekanntButton;
 	private JButton _inventarButton;
 	private JButton[] _normalButtons;
 	private JButton[] _extraButtons;
 	private JButton[] _systemButtons;
 
 	private JLabel _labelFuerLebensenergie;
 	private int _lebensenergieMaximum;
 	private int _lebensenergieAktuell;
 
 	private static final long serialVersionUID = 1L;
 
 	public BefehlsPanel()
 	{
 		setLayout(null);
 		setSize(682, 512);
 
 		_labelFuerLebensenergie = new JLabel();
 		_labelFuerLebensenergie.setLocation(ABSTAND_NORMAL, 0);
 		_lebensenergieMaximum = 8;
 		_lebensenergieAktuell = _lebensenergieMaximum;
 
 		_nehmenButton = new JButton(
 				StringUtils.capitalize(TextVerwalter.BEFEHL_NEHMEN));
 
 		_gibButton = new JButton(
 				StringUtils.capitalize(TextVerwalter.BEFEHL_UNTERSUCHE));
 
 		_fuettereButton = new JButton(
 				StringUtils.capitalize(TextVerwalter.BEFEHL_FUETTERE));
 
 		_fuettereGutButton = new JButton(GUT);
 
 		_fuettereSchlechtButton = new JButton(SCHLECHT);
 
 		_fuettereUnbekanntButton = new JButton(UNBEKANNT);
 
 		_essenTascheButton = new JButton(
 				StringUtils
 						.capitalize(TextVerwalter.BEFEHL_ESSEN + " aus der ")
 						+ StringUtils.capitalize(TextVerwalter.ORT_TASCHE));
 
 		_essenTascheGutButton = new JButton(GUT);
 
 		_essenTascheSchlechtButton = new JButton(SCHLECHT);
 
 		_essenTascheUnbekanntButton = new JButton(UNBEKANNT);
 
 		_essenBodenButton = new JButton(
 				StringUtils.capitalize(TextVerwalter.BEFEHL_ESSEN + " vom ")
 						+ StringUtils.capitalize(TextVerwalter.ORT_BODEN));
 
 		_inventarButton = new JButton(
 				StringUtils.capitalize(TextVerwalter.BEFEHL_INVENTAR));
 
 		_ablegenButton = new JButton(
 				StringUtils.capitalize(TextVerwalter.BEFEHL_ABLEGEN));
 
 		_ablegenGutButton = new JButton(GUT);
 
 		_ablegenSchlechtButton = new JButton(SCHLECHT);
 
 		_ablegenUnbekanntButton = new JButton(UNBEKANNT);
 
 		_helpButton = new JButton(new ImageIcon(getClass().getResource(
 				"bilder/faq-icon.png")));
 
 		_ladenButton = new JButton(new ImageIcon(getClass().getResource(
 				"bilder/disk.gif")));
 
 		_quitButton = new JButton(new ImageIcon(getClass().getResource(
 				"bilder/exitIcon.gif")));
 		_quitButton.setBackground(new Color(0, 175, 0));
 
 		_normalButtons = new JButton[] { _nehmenButton, _gibButton,
 				_fuettereButton, _essenBodenButton, _essenTascheButton,
 				_inventarButton, _ablegenButton };
 		// REIHENFOLGE NICHT ÄNDERN!!!!
 		_extraButtons = new JButton[] { _fuettereGutButton,
 				_fuettereSchlechtButton, _fuettereUnbekanntButton,
 				_ablegenGutButton, _ablegenSchlechtButton,
 				_ablegenUnbekanntButton, _essenTascheGutButton,
 				_essenTascheSchlechtButton, _essenTascheUnbekanntButton };
 		_systemButtons = new JButton[] { _helpButton, _ladenButton, _quitButton };
 
 		for(JButton b : _normalButtons)
 		{
 			b.setFocusable(false);
 			add(b);
 		}
 		String[] gutschlechtunbekannt = { "Guter Krümel", "Schlechter Krümel",
 				"Unidentifierter Krümel" };
 		int c = 0;
 		for(JButton b : _extraButtons)
 		{
 			b.setFocusable(false);
 			b.setToolTipText(gutschlechtunbekannt[c % 3]);
 			add(b);
 			c++;
 		}
 		for(JButton b : _systemButtons)
 		{
 			b.setFocusable(false);
 			b.setSize(50, 50);
 			add(b);
 		}
 
 		add(_labelFuerLebensenergie);
 
 		addComponentListener(new ComponentAdapter()
 		{
 
 			@Override
 			public void componentResized(ComponentEvent arg0)
 			{
 				int buttonhoehe = (BefehlsPanel.this.getHeight() - (ABSTAND_NORMAL * 2 * (ZEILENANZAHL)))
 						/ ZEILENANZAHL;
 
 				int buttonhoeheExtra = (int) (buttonhoehe * 0.75);
 
 				Dimension buttongroesse = new Dimension(
 						(BefehlsPanel.this.getWidth() - 100) / 2 - 25,
 						buttonhoehe);
 
 				Dimension buttongroesseExtra = new Dimension(
 						buttongroesse.width / 3, buttonhoeheExtra);
 
 				// TODO
 				for(JButton b : _normalButtons)
 				{
 					b.setSize(buttongroesse);
 				}
 
 				for(JButton b : _extraButtons)
 				{
 					b.setSize(buttongroesseExtra);
 				}
 
 				//linke Spalte
 				int xpos_1 = 65;
 
 				_nehmenButton.setLocation(xpos_1, 5);
 				_gibButton.setLocation(xpos_1,
 						(int) (buttongroesse.getHeight() + 10));
 				_essenBodenButton.setLocation(xpos_1,
 						(int) (buttongroesse.getHeight() * 2 + 15));
 				_fuettereButton.setLocation(xpos_1,
 						(int) (buttongroesse.getHeight() * 3 + 20));
 
 				_fuettereGutButton.setLocation(xpos_1,
 						(int) (_fuettereButton.getY() + buttonhoehe + 2));
 				_fuettereSchlechtButton.setLocation(
 						xpos_1 + _fuettereGutButton.getWidth() + 1,
 						(int) (_fuettereButton.getY() + buttonhoehe + 2));
 				_fuettereUnbekanntButton.setLocation(
 						_fuettereSchlechtButton.getLocation().x
 								+ _fuettereSchlechtButton.getWidth() + 1,
 						(int) (_fuettereButton.getY() + buttonhoehe + 2));
 
 				//rechte Spalte
 
 				int x = _nehmenButton.getWidth()
 						+ _nehmenButton.getLocation().x + 10;
 
 				_inventarButton.setLocation(x, 5);
 
 				_ablegenButton.setLocation(x, buttonhoehe + 10);
 
 				_ablegenGutButton.setLocation(x, (int) (_ablegenButton.getY()
 						+ buttonhoehe + 2));
 				_ablegenSchlechtButton.setLocation(
 						_ablegenGutButton.getLocation().x
 								+ buttongroesseExtra.width + 1,
 						_ablegenButton.getY() + buttonhoehe + 2);
 
 				_ablegenUnbekanntButton.setLocation(
 						_ablegenSchlechtButton.getLocation().x
 								+ buttongroesseExtra.width + 1,
 						_ablegenButton.getY() + buttonhoehe + 2);
 
 				_essenTascheButton.setLocation(x,
 						(int) (buttongroesse.getHeight() * 3 + 20));
 
 				_essenTascheGutButton.setLocation(x,
 						(int) (_essenTascheButton.getY() + buttonhoehe + 2));
 				_essenTascheSchlechtButton.setLocation(x
 						+ buttongroesseExtra.width + 1,
 						(int) (_essenTascheButton.getY() + buttonhoehe + 2));
 				_essenTascheUnbekanntButton.setLocation(
 						_essenTascheSchlechtButton.getX()
 								+ buttongroesseExtra.width + 1,
 						(int) (_essenTascheButton.getY() + buttonhoehe + 2));
 
 				// system buttons
 				_helpButton.setLocation(_inventarButton.getX()
 						+ _inventarButton.getWidth() + 5, 5);
 
 				_ladenButton.setLocation(_inventarButton.getX()
 						+ _inventarButton.getWidth() + 5, _helpButton.getY()
 						+ _helpButton.getHeight() + 5);
 
 				_quitButton.setLocation(_inventarButton.getX()
 						+ _inventarButton.getWidth() + 5, _ladenButton.getY()
 						+ _ladenButton.getHeight() + 5);
 
 				_labelFuerLebensenergie.setSize(50,
 						BefehlsPanel.this.getHeight());
				setLebensenergie(16);
 
 			}
 
 		});
 
 	}
 
 	public JButton getQuitButton()
 	{
 		return _quitButton;
 	}
 
 	public JButton getHelpButton()
 	{
 		return _helpButton;
 	}
 
 	public JButton getEssenAusTascheButton()
 	{
 		return _essenTascheButton;
 	}
 
 	public JButton getNehmenButton()
 	{
 		return _nehmenButton;
 	}
 
 	public JButton getGibButton()
 	{
 		return _gibButton;
 	}
 
 	public JButton getEssenBodenButton()
 	{
 		return _essenBodenButton;
 	}
 
 	public JButton getLadenButton()
 	{
 		return _ladenButton;
 	}
 
 	public JButton getFuettereButton()
 	{
 		return _fuettereButton;
 	}
 
 	public JButton getAblegenButton()
 	{
 		return _ablegenButton;
 	}
 
 	public JButton getInventarButton()
 	{
 		return _inventarButton;
 	}
 
 	public void setLebensenergie(int lebensenergie)
 	{
 		if(_labelFuerLebensenergie.getHeight() != 0
 				&& _labelFuerLebensenergie.getWidth() != 0)
 		{
 			BufferedImage lebensbalken = new BufferedImage(50,
 					_labelFuerLebensenergie.getHeight(),
 					BufferedImage.TYPE_INT_ARGB);
 
 			_lebensenergieAktuell = lebensenergie;
 
 			int balkenhoehe = (int) (_labelFuerLebensenergie.getHeight() / 100.0 * (_lebensenergieAktuell / (_lebensenergieMaximum / 100.0)));
 
 			GradientPaint gp = new GradientPaint(new Point2D.Double(0,
 					_labelFuerLebensenergie.getHeight() - balkenhoehe),
 					Color.red, new Point2D.Double(50,
 							_labelFuerLebensenergie.getHeight()), Color.blue);
 
 			Graphics2D g2d = (Graphics2D) lebensbalken.getGraphics();
 			g2d.setPaint(gp);
 			Rectangle rec = new Rectangle(0,
 					_labelFuerLebensenergie.getHeight() - balkenhoehe, 50,
 					balkenhoehe);
 			g2d.fill(rec);
 			g2d.draw(rec);
 
 			_labelFuerLebensenergie.setIcon(new ImageIcon(lebensbalken));
 		}
 
 	}
 
 	public JButton getEssenTascheGutButton()
 	{
 		return _essenTascheGutButton;
 	}
 
 	public JButton getEssenTascheSchlechtButton()
 	{
 		return _essenTascheSchlechtButton;
 	}
 
 	public JButton getEssenTascheUnbekanntButton()
 	{
 		return _essenTascheUnbekanntButton;
 	}
 
 	public JButton getFuettereGutButton()
 	{
 		return _fuettereGutButton;
 	}
 
 	public JButton getFuettereSchlechtButton()
 	{
 		return _fuettereSchlechtButton;
 	}
 
 	public JButton getFuettereUnbekanntButton()
 	{
 		return _fuettereUnbekanntButton;
 	}
 
 	public JButton getAblegenGutButton()
 	{
 		return _ablegenGutButton;
 	}
 
 	public JButton getAblegenSchlechtButton()
 	{
 		return _ablegenSchlechtButton;
 	}
 
 	public JButton getAblegenUnbekanntButton()
 	{
 		return _ablegenUnbekanntButton;
 	}
 
 	/**
 	 * @return the normalButtons
 	 */
 	public JButton[] getNormalButtons()
 	{
 		return _normalButtons;
 	}
 
 	/**
 	 * @return the extraButtons
 	 */
 	public JButton[] getExtraButtons()
 	{
 		return _extraButtons;
 	}
 
 	/**
 	 * @return the systemButtons
 	 */
 	public JButton[] getSystemButtons()
 	{
 		return _systemButtons;
 	}
 }
