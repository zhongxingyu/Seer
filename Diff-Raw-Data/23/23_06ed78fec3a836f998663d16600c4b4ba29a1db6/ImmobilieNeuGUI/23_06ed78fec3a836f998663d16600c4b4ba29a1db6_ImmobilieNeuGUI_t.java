 package ersteVersuche.Immobilie;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Locale;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 import ersteVersuche.Material.Haus;
 import ersteVersuche.Material.Immobilie;
 import ersteVersuche.Material.Wohnung;
 
 public class ImmobilieNeuGUI extends JDialog {
 
 	private static final long serialVersionUID = 42;
 
 	private static final int TEXTFELD_BREITE = 10;
 
 	private JPanel _dataPanelBase;
 	private final JPanel _dataPanel;
 
 	private JTextField _ort;
 	private JFormattedTextField _plz;
 	private JTextField _strasse;
 	private JFormattedTextField _hausNr;
 	private JFormattedTextField _flaeche;
 
 	private final JButton _okButton;
 
 	private ButtonGroup _classButtonGroup;
 	private JRadioButton _classWohnung;
 	private JRadioButton _classHaus;
 
 	private final JPanel _dataPanelExtend;
 	private JPanel _hausExtend;
 	private JPanel _wohnungExtend;
 
 	private JFormattedTextField _haus_stockwerke;
 	private JFormattedTextField _haus_kaufpreis;
 	private JCheckBox _haus_garten;
 
 	private JFormattedTextField _wohnung_stockwerk;
 	private JFormattedTextField _wohnung_mietpreis;
 	private JFormattedTextField _wohnung_zimmer;
 	private JCheckBox _wohnung_balkon;
 	private JCheckBox _wohnung_ebk;
 
 	public ImmobilieNeuGUI(JFrame frame) {
 		super(frame, true);
 
 		setTitle("Makler anlegen");
 		// setSize(800, 600);
 
 		// setLocation(280, 100);
 
 		setLayout(new BorderLayout());
 		setBackground(new Color(255, 255, 255));
 
 		initClassWahl();
 
 		_dataPanel = new JPanel(new BorderLayout());
 		add(_dataPanel, BorderLayout.CENTER);
 
 		initBase();
 		_dataPanel.add(_dataPanelBase, BorderLayout.NORTH);
 
 		_dataPanelExtend = new JPanel(new BorderLayout());
 
 		_dataPanel.add(_dataPanelExtend, BorderLayout.SOUTH);
 
 		initWohnung();
 		initHaus();
 
 		_dataPanelExtend.add(_hausExtend);
 
 		UpdateClassData();
 
 		_okButton = new JButton("OK");
 
 		add(_okButton, BorderLayout.SOUTH);
 
 		pack();
 	}
 
 	private void initClassWahl() {
 		JPanel selectClassPanel = new JPanel(new GridLayout(0, 2));
 
 		_classButtonGroup = new ButtonGroup();
 		_classHaus = new JRadioButton("Haus");
 		_classHaus.setSelected(true);
 		_classWohnung = new JRadioButton("Wohnung");
 
 		selectClassPanel.add(_classHaus);
 		selectClassPanel.add(_classWohnung);
 
 		_classButtonGroup.add(_classHaus);
 		_classButtonGroup.add(_classWohnung);
 		add(selectClassPanel, BorderLayout.NORTH);
 
 		_classHaus.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				UpdateClassData();
 			}
 		});
 
 		_classWohnung.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				UpdateClassData();
 			}
 		});
 	}
 
 	private void initBase() {
 		_dataPanelBase = new JPanel(new GridLayout(5, 2));
 
 		_ort = new JTextField(TEXTFELD_BREITE);
 		_strasse = new JTextField(TEXTFELD_BREITE);
 		_hausNr = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		_hausNr.setColumns(TEXTFELD_BREITE);
 		_hausNr.setValue(0);
 		_flaeche = new JFormattedTextField(NumberFormat.getNumberInstance());
 		_flaeche.setColumns(TEXTFELD_BREITE);
 		_flaeche.setValue(0.0f);
 		_plz = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		_plz.setColumns(TEXTFELD_BREITE);
 		_plz.setValue(0);
 
 		_dataPanelBase.add(new JLabel("Ort:"));
 		_dataPanelBase.add(_ort);
 		_dataPanelBase.add(new JLabel("PLZ:"));
 		_dataPanelBase.add(_plz);
 		_dataPanelBase.add(new JLabel("Straße:"));
 		_dataPanelBase.add(_strasse);
 		_dataPanelBase.add(new JLabel("Hausnummer:"));
 		_dataPanelBase.add(_hausNr);
 		_dataPanelBase.add(new JLabel("Fläche [m²]:"));
 		_dataPanelBase.add(_flaeche);
 	}
 
 	private void initWohnung() {
 		_wohnungExtend = new JPanel(new GridLayout(0, 2));
 
 		_wohnung_stockwerk = new JFormattedTextField(
 				NumberFormat.getIntegerInstance());
 		_wohnung_stockwerk.setColumns(TEXTFELD_BREITE);
 		_wohnung_stockwerk.setValue(0);
 		_wohnung_mietpreis = new JFormattedTextField(
 				NumberFormat.getNumberInstance());
 		_wohnung_mietpreis.setColumns(TEXTFELD_BREITE);
 		_wohnung_mietpreis.setValue(0.0f);
 		_wohnung_zimmer = new JFormattedTextField(
 				NumberFormat.getIntegerInstance());
 		_wohnung_zimmer.setColumns(TEXTFELD_BREITE);
 		_wohnung_zimmer.setValue(0);
 
 		_wohnung_balkon = new JCheckBox();
 		_wohnung_ebk = new JCheckBox();
 
 		_wohnungExtend.add(new JLabel("Stockwerk:"));
 		_wohnungExtend.add(_wohnung_stockwerk);
 		_wohnungExtend.add(new JLabel("Mietpreis:"));
 		_wohnungExtend.add(_wohnung_mietpreis);
 		_wohnungExtend.add(new JLabel("Zimmer:"));
 		_wohnungExtend.add(_wohnung_zimmer);
 		_wohnungExtend.add(new JLabel("Balkon:"));
 		_wohnungExtend.add(_wohnung_balkon);
 		_wohnungExtend.add(new JLabel("EBK:"));
 		_wohnungExtend.add(_wohnung_ebk);
 	}
 
 	private void initHaus() {
 		_hausExtend = new JPanel(new GridLayout(0, 2));
 
 		_haus_stockwerke = new JFormattedTextField(
 				NumberFormat.getIntegerInstance());
 		_haus_stockwerke.setColumns(TEXTFELD_BREITE);
 		_haus_stockwerke.setValue(0);
 		_haus_kaufpreis = new JFormattedTextField(
 				NumberFormat.getNumberInstance());
 		_haus_kaufpreis.setColumns(TEXTFELD_BREITE);
 		_haus_kaufpreis.setValue(0.0f);
 
 		_haus_garten = new JCheckBox();
 
 		_hausExtend.add(new JLabel("Stockwerke:"));
 		_hausExtend.add(_haus_stockwerke);
 		_hausExtend.add(new JLabel("Kaufpreis:"));
 		_hausExtend.add(_haus_kaufpreis);
 		_hausExtend.add(new JLabel("Garten:"));
 		_hausExtend.add(_haus_garten);
 
 	}
 
 	private void UpdateClassData() {
 		_dataPanelExtend.remove(_wohnungExtend);
 		_dataPanelExtend.remove(_hausExtend);
 
 		if (_classHaus.isSelected())
 			_dataPanelExtend.add(_hausExtend);
 		else if (_classWohnung.isSelected())
 			_dataPanelExtend.add(_wohnungExtend);
 		pack();
 		// repaint();
 	}
 
 	public void AddOKListener(ActionListener al) {
 		_okButton.addActionListener(al);
 	}
 
 	private float getFloatFromFormatTextbox(JFormattedTextField text) {
 		float f;
 		Object o = text.getValue();
 		if (o instanceof Long || o instanceof Integer)
 			f = (Long) o;
 		else if (o instanceof Double)
 			f = ((Double) o).floatValue();
 		else
 			f = (Float) o;
 		return f;
 	}
 
 	private int getIntFromFormatTextbox(JFormattedTextField text) {
 		int i;
 		Object o = text.getValue();
 		if (o instanceof Long)
 			i = ((Long) o).intValue();
 		else
 			// if( o instanceof Integer)
 			i = ((Integer) o).intValue();
 
 		return i;
 	}
 
 	public Immobilie getImmobilie() {
 
 		NumberFormat formatter = NumberFormat.getNumberInstance(Locale.GERMANY);
 		float flaeche = 0.0f;
 		try {
 			flaeche = formatter.parse(_flaeche.getText()).floatValue();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		flaeche = getFloatFromFormatTextbox(_flaeche);
 
 		if (_classHaus.isSelected()) {
 			int stockwerke = getIntFromFormatTextbox(_haus_stockwerke); // (Integer)
 																		// _haus_stockwerke.getValue();
 			float preis = getFloatFromFormatTextbox(_haus_kaufpreis);
 
			return new Haus(-1, _ort.getText(), getIntFromFormatTextbox(_plz),
					_strasse.getText(), getIntFromFormatTextbox(_hausNr),
					flaeche, stockwerke, preis, _haus_garten.isSelected());
 		} else if (_classWohnung.isSelected()) {
 			int stockwerk = getIntFromFormatTextbox(_wohnung_stockwerk); // (Integer)
 																			// _wohnung_stockwerk.getValue();
 			float preis = getFloatFromFormatTextbox(_wohnung_mietpreis);
 			int zimmer = getIntFromFormatTextbox(_wohnung_zimmer); // (Integer)
 																	// _wohnung_zimmer.getValue();
 
			return new Wohnung(-1, _ort.getText(),
					getIntFromFormatTextbox(_plz), _strasse.getText(),
					getIntFromFormatTextbox(_hausNr), flaeche, stockwerk,
					preis, zimmer, _wohnung_balkon.isSelected(),
					_wohnung_ebk.isSelected());
 
 		}
 
 		return null;
 	}
 
 }
