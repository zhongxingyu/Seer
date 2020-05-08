 package org.peie.avicultura.viewer;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.HeadlessException;
 import java.awt.SystemColor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 //import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import org.apache.log4j.Logger;
 import org.peie.avicultura.helper.AviInternalFrame;
 import org.peie.avicultura.helper.AviProperties;
 import org.peie.avicultura.helper.AviculturaException;
 import org.peie.avicultura.helper.BirdObject;
 import org.peie.avicultura.helper.DbHelper;
 import org.peie.avicultura.helper.GenderObj;
 import org.peie.avicultura.helper.Helper;
 import org.peie.avicultura.helper.SpeciesObj;
 
 public class AppNewBirdWindow {
 
 	public static final int LABEL_FONT_STYLE_BOLD = Font.BOLD;
 	public static final int LABEL_FONT_STYLE = Font.PLAIN;
 	public static final String LABEL_FONT = "Arial";
 	public static final int LABEL_FONT_SIZE = 11;
 	public static final String _0_1 = "0.1";
 	public static final String _1_0 = "1.0";
 
 	private JFrame frame;
 	private final static Logger LOG = Logger.getLogger(Application.class);
 
 	private JDesktopPane desktopPane;
 	private DbHelper dbhelper;
 	private BufferedImage iconNewBird, iconError, iconBird, iconEdit;
 
 	// private List<JLabel> navLables = new LinkedList<JLabel>();
 	private final static Application app = new Application();
 	private JComboBox<String> speciesBox;
 	private JTextField newSpecies;
 	private JTextField ringNo;
 	private JTextField color;
 	private AppDateField breedStart;
 	private AppDateField ringAt;
 	private AppDateField buyAt;
 	private AppDateField sellAt;
 	private AppDateField deathAt;
 	private AppDateField medicStart;
 	private AppDateField medicEnd;
 	private JComboBox<String> comboBoxMale;
 	private JComboBox<String> comboBoxFemale;
 	private BirdObject birdEditObj;
 	private boolean newBirdFlag;
 	private boolean copyBirdFlag;
 	private String modBirdId;
 	private JLabel lblSchnesittich;
 	private AviInternalFrame internalFrame;
 	private JCheckBox chckbxOffen;
 	private JRadioButton male;
 	private JRadioButton female;
 	private JTextPane buyFrom;
 	private JTextPane sellTo;
 	private JTextPane deathWhy;
 	private JTextPane docu;
 	private JTextPane obtman;
 	private JTextPane gov;
 	private JTextPane medicComment;
 	private JTextPane medicCheck;
 	private JButton btnSpeichern;
 	private BufferedImage iconSave;
 	private BufferedImage iconCopy;
 	private AviProperties properties;
 	private JPanel taskbar;
 
 	public AppNewBirdWindow(DbHelper dbhelper, JDesktopPane desktopPane,
 			AviProperties properties, JPanel taskbar) {
 		super();
 		this.dbhelper = dbhelper;
 		this.desktopPane = desktopPane;
 		this.taskbar = taskbar;
 		newBirdFlag = true;
 		copyBirdFlag = false;
 		modBirdId = null;
 		this.properties = properties;
 
 		initIcons();
 
 	}
 
 	public AppNewBirdWindow(DbHelper dbhelper, JDesktopPane desktopPane,
 			BirdObject birdEditObj, AviProperties properties, JPanel taskbar) {
 		super();
 		this.dbhelper = dbhelper;
 		this.desktopPane = desktopPane;
 		this.birdEditObj = birdEditObj;
 		this.taskbar = taskbar;
 		newBirdFlag = false;
 		copyBirdFlag = false;
 		modBirdId = birdEditObj.getBirdDataId();
 		this.properties = properties;
 
 		initIcons();
 	}
 
 	private void initIcons() {
 		URL urlNewBird = ClassLoader
 				.getSystemResource(Application.ICONS_SKY_LIGHT_ICONS_PNG_16X16_ACTIVE_NEW_PNG);
 
 		URL urlError = ClassLoader
 				.getSystemResource(Application.ICONS_SKY_LIGHT_ICONS_PNG_16X16_ACTIVE_NO_PNG);
 		URL urlBird = ClassLoader
 				.getSystemResource(Application.ICONS_LOGO_SMALL_PNG);
 		URL urlEdit = ClassLoader
 				.getSystemResource(Application.ICONS_SKY_LIGHT_ICONS_PNG_16X16_ACTIVE_DOCUMENT_PNG);
 		URL urlSave = ClassLoader
 				.getSystemResource(Application.ICONS_SKY_LIGHT_ICONS_PNG_16X16_ACTIVE_SAVE_PNG);
 		URL urlCopy = ClassLoader
 				.getSystemResource(Application.ICONS_SKY_LIGHT_ICONS_PNG_16X16_ACTIVE_COPY_PNG);
 		try {
 
 			iconNewBird = ImageIO.read(urlNewBird);
 
 			iconError = ImageIO.read(urlError);
 			iconBird = ImageIO.read(urlBird);
 			iconEdit = ImageIO.read(urlEdit);
 			iconSave = ImageIO.read(urlSave);
 			iconCopy = ImageIO.read(urlCopy);
 		} catch (IOException e) {
 			LOG.error("IO Error", e);
 		}
 	}
 
 	public void fillCopySheet(BufferedImage icon, String title) {
 		if (!newBirdFlag) {
 			copyBirdFlag = true;
 			// internalFrame.setFrameIcon(new ImageIcon(icon));
 			internalFrame.setButtonBild(new ImageIcon(icon));
 			internalFrame.setTitle(title);
 			// ringNo.setText(birdEditObj.getRingNo());
 			// ringNo.setEditable(false);
 			speciesBox.setVisible(false);
 			lblSchnesittich.setVisible(true);
 			lblSchnesittich.setText(birdEditObj.getSpeciesID());
 			lblSchnesittich.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE_BOLD,
 					LABEL_FONT_SIZE));
 			/*
 			 * chckbxOffen.setEnabled(false); if (birdEditObj.isRingType()) {
 			 * chckbxOffen.setSelected(true); }
 			 */
 			/*
 			 * male.setEnabled(false); female.setEnabled(false); if
 			 * (birdEditObj.isMale()) { male.setSelected(true);
 			 * female.setSelected(false); } else { male.setSelected(false);
 			 * female.setSelected(true); }
 			 */
 
 			color.setText(birdEditObj.getColor());
 			// color.setEditable(false);
 
 			// fillGenderComboBox(birdEditObj.getSpeciesID());
 
			comboBoxMale.setSelectedItem(birdEditObj.getComboBoxMale());
 
			comboBoxFemale.setSelectedItem(birdEditObj.getComboBoxFemale());
 
			comboBoxFemale.setEditable(false);
			comboBoxFemale.setEnabled(false);
 
			comboBoxMale.setEditable(false);
			comboBoxMale.setEnabled(false);
 
 			breedStart.setText(birdEditObj.getBreedStart());
 			ringAt.setText(birdEditObj.getRingAt());
 			buyAt.setText(birdEditObj.getBuyAt());
 			buyFrom.setText(birdEditObj.getBuyFrom());
 
 			sellAt.setText(birdEditObj.getSellAt());
 			sellTo.setText(birdEditObj.getSellTo());
 
 			docu.setText(birdEditObj.getDocu());
 			obtman.setText(birdEditObj.getObtman());
 			gov.setText(birdEditObj.getGov());
 
 			medicEnd.setText(birdEditObj.getMedicEnd());
 			medicStart.setText(birdEditObj.getMedicStart());
 
 			deathAt.setText(birdEditObj.getDeathAt());
 			deathWhy.setText(birdEditObj.getDeathWhy());
 
 			medicCheck.setText(birdEditObj.getMedicCheck());
 			medicComment.setText(birdEditObj.getMedicComment());
 			try {
 				birdPairField.setText(dbhelper.getBirdPairNo(birdEditObj
 						.getBirdPairId()));
 			} catch (AviculturaException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			app.setLabelTitel(gobalFrameCounter,
 					"Ring Nr.: " + birdEditObj.getRingNo(), iconEdit);
 
 		}
 	}
 
 	public void fillDataSheet(BufferedImage icon, String title) {
 		if (!newBirdFlag) {
 			btnCopy.setVisible(true);
 			// internalFrame.setFrameIcon(new ImageIcon(icon));
 			internalFrame.setButtonBild(new ImageIcon(icon));
 			internalFrame.setTitle(title);
 			ringNo.setText(birdEditObj.getRingNo());
 			ringNo.setEditable(false);
 			speciesBox.setVisible(true);
 
 			speciesBox.setSelectedItem(birdEditObj.getSpeciesID());
 
 			lblSchnesittich.setVisible(false);
 			lblSchnesittich.setText(birdEditObj.getSpeciesID());
 			lblSchnesittich.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE_BOLD,
 					LABEL_FONT_SIZE));
 			// chckbxOffen.setEnabled(false);
 			if (birdEditObj.isRingType()) {
 				chckbxOffen.setSelected(true);
 			}
 			// male.setEnabled(false);
 			// female.setEnabled(false);
 			if (birdEditObj.isMale()) {
 				male.setSelected(true);
 				female.setSelected(false);
 			} else {
 				male.setSelected(false);
 				female.setSelected(true);
 			}
 
 			color.setText(birdEditObj.getColor());
 			color.setEditable(true);
 
 			// fillGenderComboBox(birdEditObj.getSpeciesID());
 
 			// comboBoxMale.setSelectedItem(birdEditObj.getComboBoxMale());
 
 			// comboBoxFemale.setSelectedItem(birdEditObj.getComboBoxFemale());
 
 			breedStart.setText(birdEditObj.getBreedStart());
 			ringAt.setText(birdEditObj.getRingAt());
 			buyAt.setText(birdEditObj.getBuyAt());
 			buyFrom.setText(birdEditObj.getBuyFrom());
 
 			sellAt.setText(birdEditObj.getSellAt());
 			sellTo.setText(birdEditObj.getSellTo());
 
 			docu.setText(birdEditObj.getDocu());
 			obtman.setText(birdEditObj.getObtman());
 			gov.setText(birdEditObj.getGov());
 
 			medicEnd.setText(birdEditObj.getMedicEnd());
 			medicStart.setText(birdEditObj.getMedicStart());
 
 			deathAt.setText(birdEditObj.getDeathAt());
 			deathWhy.setText(birdEditObj.getDeathWhy());
 
 			medicCheck.setText(birdEditObj.getMedicCheck());
 			medicComment.setText(birdEditObj.getMedicComment());
 			try {
 				birdPairField.setText(dbhelper.getBirdPairNo(birdEditObj
 						.getBirdPairId()));
 			} catch (AviculturaException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			app.setLabelTitel(gobalFrameCounter,
 					"Ring Nr.: " + birdEditObj.getRingNo(), iconEdit);
 
 		}
 	}
 
 	private int gobalFrameCounter = 0;
 	private JButton btnCopy;
 	private JTextField birdPairField;
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	void openNewBird() throws AviculturaException {
 
 		List<SpeciesObj> names = dbhelper.getBirdSpecies();
 
 		internalFrame = new AviInternalFrame(taskbar);
 		internalFrame.getContentPane().setBackground(
 				SystemColor.activeCaptionBorder);
 		internalFrame.getContentPane().setFont(
 				new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		// internalFrame.setFrameIcon(new ImageIcon(iconNewBird));
 
 		// internalFrame.setButtonBild(new ImageIcon(iconNewBird));
 
 		internalFrame.setMaximizable(true);
 		internalFrame.setResizable(true);
 		internalFrame.setClosable(true);
 		internalFrame.setBounds(77, 11, 700, 600);
 		desktopPane.add(internalFrame);
 		internalFrame.getContentPane().setLayout(null);
 		final int counter = app.newNavLabel("Neuer Vogel", internalFrame, true,
 				iconNewBird);
 		internalFrame.setTitle("Neuer Vogel" + (counter + 1));
 
 		internalFrame.setButtonBild(new ImageIcon(iconNewBird));
 
 		gobalFrameCounter = counter;
 
 		/*
 		 * internalFrame.addInternalFrameListener(new InternalFrameListener() {
 		 * public void internalFrameClosed(InternalFrameEvent e) { //
 		 * navLables.get(counter).setVisible(false);
 		 * app.setNavLableHidden(counter); }
 		 * 
 		 * public void internalFrameClosing(InternalFrameEvent e) { //
 		 * navLables.get(counter).setVisible(false);
 		 * app.setNavLableHidden(counter); }
 		 * 
 		 * @Override public void internalFrameActivated(InternalFrameEvent arg0)
 		 * {
 		 * 
 		 * }
 		 * 
 		 * @Override public void internalFrameDeactivated(InternalFrameEvent
 		 * arg0) {
 		 * 
 		 * }
 		 * 
 		 * @Override public void internalFrameDeiconified(InternalFrameEvent
 		 * arg0) {
 		 * 
 		 * }
 		 * 
 		 * @Override public void internalFrameIconified(InternalFrameEvent arg0)
 		 * {
 		 * 
 		 * }
 		 * 
 		 * @Override public void internalFrameOpened(InternalFrameEvent arg0) {
 		 * 
 		 * } });
 		 */
 
 		JLabel lblNewLabel_2 = new JLabel("Vogelart");
 		lblNewLabel_2.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblNewLabel_2.setBounds(20, 29, 46, 14);
 		internalFrame.getContentPane().add(lblNewLabel_2);
 
 		speciesBox = new JComboBox<String>();
 		speciesBox.setBackground(SystemColor.activeCaptionBorder);
 		speciesBox.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		// speciesBox.setModel(new DefaultComboBoxModel(names));
 		for (int i = 0; i < names.size(); i++) {
 			speciesBox.addItem(names.get(i).getName());
 		}
 
 		speciesBox.setBounds(67, 26, 102, 20);
 		internalFrame.getContentPane().add(speciesBox);
 
 		final JLabel lblNeu = new JLabel("Neu:");
 		lblNeu.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		lblNeu.setBounds(20, 54, 46, 14);
 		internalFrame.getContentPane().add(lblNeu);
 
 		newSpecies = new JTextField();
 
 		newSpecies.setBounds(67, 54, 102, 20);
 		internalFrame.getContentPane().add(newSpecies);
 		newSpecies.setColumns(10);
 
 		JLabel lblRingNr = new JLabel("Ring Nr.:");
 		lblRingNr.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblRingNr.setBounds(190, 29, 46, 14);
 		internalFrame.getContentPane().add(lblRingNr);
 
 		ringNo = new JTextField();
 		ringNo.setText(properties.getProperty(AviProperties.KEY_RING_NO)
 				+ properties.getProperty(AviProperties.KEY_YEAR));
 		ringNo.setBounds(238, 26, 86, 20);
 		internalFrame.getContentPane().add(ringNo);
 		ringNo.setColumns(10);
 
 		JLabel lblFarbe = new JLabel("Farbe:");
 		lblFarbe.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		lblFarbe.setBounds(190, 54, 46, 14);
 		internalFrame.getContentPane().add(lblFarbe);
 
 		color = new JTextField();
 		color.setBounds(238, 54, 86, 20);
 		internalFrame.getContentPane().add(color);
 		color.setColumns(10);
 
 		JLabel lblGeschlecht = new JLabel("Geschlecht");
 		lblGeschlecht.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblGeschlecht.setBounds(398, 11, 58, 14);
 		lblGeschlecht.setVisible(false);
 		internalFrame.getContentPane().add(lblGeschlecht);
 
 		male = new JRadioButton(_1_0);
 		male.setBackground(SystemColor.activeCaptionBorder);
 		male.setSelected(true);
 		male.setBounds(398, 25, 51, 23);
 		internalFrame.getContentPane().add(male);
 
 		female = new JRadioButton(_0_1);
 		female.setBackground(SystemColor.activeCaptionBorder);
 		female.setBounds(398, 50, 58, 23);
 		internalFrame.getContentPane().add(female);
 		internalFrame.setVisible(true);
 		ButtonGroup group = new ButtonGroup();
 
 		group.add(male);
 		group.add(female);
 
 		JLabel lblSelbstgezchteterVogel = new JLabel(
 				"Selbstgez\u00FCchteter Vogel");
 		lblSelbstgezchteterVogel.setFont(new Font(LABEL_FONT,
 				LABEL_FONT_STYLE_BOLD, LABEL_FONT_SIZE));
 		lblSelbstgezchteterVogel.setBounds(20, 97, 149, 14);
 		internalFrame.getContentPane().add(lblSelbstgezchteterVogel);
 
 		JLabel lblBrutbegin = new JLabel("Brutbeginn");
 		lblBrutbegin.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblBrutbegin.setBounds(20, 125, 58, 14);
 		internalFrame.getContentPane().add(lblBrutbegin);
 
 		breedStart = new AppDateField();
 		breedStart.setToolTipText("Datum im Format tt.mm.jjjj");
 		breedStart.setBounds(88, 122, 102, 20);
 		breedStart.datePopUp();
 		internalFrame.getContentPane().add(breedStart);
 		breedStart.setColumns(10);
 
 		JLabel lblBegringtAm = new JLabel("Begringt am:");
 		lblBegringtAm.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblBegringtAm.setBounds(20, 156, 58, 14);
 		internalFrame.getContentPane().add(lblBegringtAm);
 
 		ringAt = new AppDateField();
 		ringAt.setBounds(88, 153, 102, 20);
 		ringAt.datePopUp();
 		internalFrame.getContentPane().add(ringAt);
 		ringAt.setColumns(10);
 
 		JLabel lblErworbenerVogel = new JLabel("Erworbener Vogel");
 		lblErworbenerVogel.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE_BOLD,
 				LABEL_FONT_SIZE));
 		lblErworbenerVogel.setBounds(215, 97, 124, 14);
 		internalFrame.getContentPane().add(lblErworbenerVogel);
 
 		JLabel lblErworbenAm = new JLabel("Erworben am:");
 		lblErworbenAm.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblErworbenAm.setBounds(215, 125, 70, 14);
 		internalFrame.getContentPane().add(lblErworbenAm);
 
 		buyAt = new AppDateField();
 		buyAt.setBounds(295, 122, 97, 20);
 		buyAt.datePopUp();
 		internalFrame.getContentPane().add(buyAt);
 		buyAt.setColumns(10);
 
 		JLabel lblAdresse = new JLabel("Adresse:");
 		lblAdresse.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblAdresse.setBounds(215, 156, 46, 14);
 		internalFrame.getContentPane().add(lblAdresse);
 
 		buyFrom = new JTextPane();
 		buyFrom.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		buyFrom.setBounds(295, 156, 97, 69);
 		internalFrame.getContentPane().add(buyFrom);
 
 		JLabel lblVerkaufterVogel = new JLabel("Verkaufter Vogel");
 		lblVerkaufterVogel.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE_BOLD,
 				LABEL_FONT_SIZE));
 		lblVerkaufterVogel.setBounds(419, 97, 103, 14);
 		internalFrame.getContentPane().add(lblVerkaufterVogel);
 
 		JLabel lblVerkauftAm = new JLabel("Verkauft am:");
 		lblVerkauftAm.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblVerkauftAm.setBounds(419, 125, 63, 14);
 		internalFrame.getContentPane().add(lblVerkauftAm);
 
 		sellAt = new AppDateField();
 		sellAt.setBounds(485, 122, 102, 20);
 		sellAt.datePopUp();
 		internalFrame.getContentPane().add(sellAt);
 		sellAt.setColumns(10);
 
 		JLabel label = new JLabel("Adresse:");
 		label.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		label.setBounds(419, 156, 46, 14);
 		internalFrame.getContentPane().add(label);
 
 		sellTo = new JTextPane();
 		sellTo.setBounds(485, 156, 102, 69);
 		internalFrame.getContentPane().add(sellTo);
 
 		JLabel lblNewLabel_3 = new JLabel("Abgang durch Tod");
 		lblNewLabel_3.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE_BOLD,
 				LABEL_FONT_SIZE));
 		lblNewLabel_3.setBounds(20, 248, 124, 14);
 		internalFrame.getContentPane().add(lblNewLabel_3);
 
 		JLabel lblAm = new JLabel("am:");
 		lblAm.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		lblAm.setBounds(20, 281, 46, 14);
 		internalFrame.getContentPane().add(lblAm);
 
 		deathAt = new AppDateField();
 		deathAt.setBounds(88, 278, 102, 20);
 		deathAt.datePopUp();
 		internalFrame.getContentPane().add(deathAt);
 		deathAt.setColumns(10);
 
 		JLabel lblUrsache = new JLabel("Ursache:");
 		lblUrsache.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblUrsache.setBounds(20, 323, 46, 14);
 		internalFrame.getContentPane().add(lblUrsache);
 
 		deathWhy = new JTextPane();
 		deathWhy.setBounds(88, 323, 102, 69);
 		internalFrame.getContentPane().add(deathWhy);
 
 		JLabel lblTierrztlicheBehandlung = new JLabel(
 				"Tier\u00E4rztliche Behandlung");
 		lblTierrztlicheBehandlung.setFont(new Font(LABEL_FONT,
 				LABEL_FONT_STYLE_BOLD, LABEL_FONT_SIZE));
 		lblTierrztlicheBehandlung.setBounds(215, 248, 149, 14);
 		internalFrame.getContentPane().add(lblTierrztlicheBehandlung);
 
 		JLabel lblBeginn = new JLabel("Beginn:");
 		lblBeginn.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblBeginn.setBounds(215, 281, 46, 14);
 		internalFrame.getContentPane().add(lblBeginn);
 
 		JLabel lblArtdosierung = new JLabel("Art/Dosierung");
 		lblArtdosierung.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblArtdosierung.setBounds(215, 323, 70, 14);
 		internalFrame.getContentPane().add(lblArtdosierung);
 
 		medicStart = new AppDateField();
 		medicStart.setBounds(295, 281, 97, 20);
 		medicStart.datePopUp();
 		internalFrame.getContentPane().add(medicStart);
 		medicStart.setColumns(10);
 
 		medicComment = new JTextPane();
 		medicComment.setBounds(295, 323, 97, 69);
 		internalFrame.getContentPane().add(medicComment);
 
 		JLabel lblEnde = new JLabel("Ende");
 		lblEnde.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		lblEnde.setBounds(419, 281, 46, 14);
 		internalFrame.getContentPane().add(lblEnde);
 
 		JLabel lblKontriolle = new JLabel("Kontrolle");
 		lblKontriolle.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblKontriolle.setBounds(419, 323, 46, 14);
 		internalFrame.getContentPane().add(lblKontriolle);
 
 		medicEnd = new AppDateField();
 		medicEnd.setBounds(485, 281, 102, 20);
 		medicEnd.datePopUp();
 		internalFrame.getContentPane().add(medicEnd);
 		medicEnd.setColumns(10);
 
 		medicCheck = new JTextPane();
 		medicCheck.setBounds(485, 323, 102, 69);
 		internalFrame.getContentPane().add(medicCheck);
 
 		JLabel lblNewLabel_4 = new JLabel("Bemerkung");
 		lblNewLabel_4.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE_BOLD,
 				LABEL_FONT_SIZE));
 		lblNewLabel_4.setBounds(20, 413, 124, 14);
 		internalFrame.getContentPane().add(lblNewLabel_4);
 
 		JLabel lblNewLabel_5 = new JLabel("Einfuhrdoku./Erwerbsbe.");
 		lblNewLabel_5.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblNewLabel_5.setBounds(88, 427, 148, 14);
 		internalFrame.getContentPane().add(lblNewLabel_5);
 
 		docu = new JTextPane();
 		docu.setBounds(88, 452, 197, 69);
 		internalFrame.getContentPane().add(docu);
 
 		JLabel lblNewLabel_6 = new JLabel("Sichtvermerk. Vertrauensmann");
 		lblNewLabel_6.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblNewLabel_6.setBounds(295, 427, 149, 14);
 		internalFrame.getContentPane().add(lblNewLabel_6);
 
 		obtman = new JTextPane();
 		obtman.setBounds(295, 452, 184, 69);
 		internalFrame.getContentPane().add(obtman);
 
 		gov = new JTextPane();
 		gov.setBounds(485, 452, 184, 69);
 		internalFrame.getContentPane().add(gov);
 
 		JLabel lblSichtvermerkBehrde = new JLabel("Sichtvermerk. Beh\u00F6rde");
 		lblSichtvermerkBehrde.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		lblSichtvermerkBehrde.setBounds(485, 427, 184, 14);
 		internalFrame.getContentPane().add(lblSichtvermerkBehrde);
 
 		// comboBoxMale = new JComboBox<String>();
 		// comboBoxMale.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 		// LABEL_FONT_SIZE));
 		// comboBoxMale.setBounds(513, 26, 156, 20);
 
 		// internalFrame.getContentPane().add(comboBoxMale);
 
 		birdPairField = new JTextField(10);
 		birdPairField.setBounds(513, 26, 156, 20);
 		birdPairField.setEditable(false);
 		internalFrame.getContentPane().add(birdPairField);
 
 		JLabel lblVater = new JLabel("Zuchtpaar");
 		lblVater.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		lblVater.setBounds(457, 29, 100, 14);
 		internalFrame.getContentPane().add(lblVater);
 
 		JButton newBirdPair = new JButton("Zuchtpaar", new ImageIcon(
 				iconNewBird));
 		newBirdPair.setBounds(513, 54, 156, 20);
 
 		internalFrame.getContentPane().add(newBirdPair);
 
 		newBirdPair.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				ZuchtPaarAuswahl zpa = new ZuchtPaarAuswahl(desktopPane,
 						taskbar, dbhelper, birdPairField);
 				
 				String text = speciesBox.getSelectedItem().toString();
 
 				// fillGenderComboBox(text);
 				String type = "";
 
 				if (text.endsWith(DbHelper.NEUER_VOGEL)) {
 					type = newSpecies.getText();
 				}else{
 					type = text;
 				}
 				
 				try {
 					zpa.neueAuswahl(type);
 				} catch (AviculturaException e) {
 					e.viewError(frame);
 				}
 
 			}
 		});
 
 		// comboBoxFemale = new JComboBox<String>();
 		// comboBoxFemale.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 		// LABEL_FONT_SIZE));
 		// comboBoxFemale.setBounds(513, 54, 156, 20);
 
 		// internalFrame.getContentPane().add(comboBoxFemale);
 
 		// String text = speciesBox.getSelectedItem().toString();
 
 		// fillGenderComboBox(text);
 
 		// JLabel lblMutter = new JLabel("Mutter");
 		// lblMutter.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 		// LABEL_FONT_SIZE));
 		// lblMutter.setBounds(457, 54, 46, 14);
 		// internalFrame.getContentPane().add(lblMutter);
 
 		btnSpeichern = new JButton("Speichern");
 		btnSpeichern.setIcon(new ImageIcon(iconSave));
 		btnSpeichern.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		btnSpeichern.setBounds(419, 537, 106, 23);
 		internalFrame.getContentPane().add(btnSpeichern);
 
 		btnCopy = new JButton("Kopieren");
 		btnCopy.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				BirdObject birdEditObj;
 				try {
 					birdEditObj = dbhelper.getBirdEdit(ringNo.getText());
 					AppNewBirdWindow copyBird = new AppNewBirdWindow(dbhelper,
 							desktopPane, birdEditObj, properties, taskbar);
 
 					copyBird.openNewBird();
 					copyBird.fillCopySheet(iconCopy, "Neuer Vogel kopiert von "
 							+ ringNo.getText());
 				} catch (AviculturaException e) {
 					e.viewError(frame);
 				}
 
 			}
 		});
 		btnCopy.setIcon(new ImageIcon(iconCopy));
 		btnCopy.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE, LABEL_FONT_SIZE));
 		btnCopy.setBounds(559, 537, 110, 23);
 		btnCopy.setVisible(false);
 		internalFrame.getContentPane().add(btnCopy);
 
 		chckbxOffen = new JCheckBox("offen");
 		chckbxOffen.setBackground(SystemColor.activeCaptionBorder);
 		chckbxOffen.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 		chckbxOffen.setBounds(330, 25, 58, 23);
 		internalFrame.getContentPane().add(chckbxOffen);
 
 		lblSchnesittich = new JLabel("N/A");
 		lblSchnesittich.setBounds(67, 29, 70, 14);
 		internalFrame.getContentPane().add(lblSchnesittich);
 		lblSchnesittich.setVisible(false);
 		lblSchnesittich.setFont(new Font(LABEL_FONT, LABEL_FONT_STYLE,
 				LABEL_FONT_SIZE));
 
 		newSpecies.setVisible(false);
 		lblNeu.setVisible(false);
 
 		speciesBox.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				String text = speciesBox.getSelectedItem().toString();
 
 				// fillGenderComboBox(text);
 
 				if (text.endsWith(DbHelper.NEUER_VOGEL)) {
 					newSpecies.setVisible(true);
 					lblNeu.setVisible(true);
 				} else {
 					newSpecies.setVisible(false);
 					lblNeu.setVisible(false);
 
 				}
 
 			}
 		});
 
 		btnSpeichern.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				try {
 					if (!dbhelper.isRingNoExists(ringNo.getText())) {
 
 						if (checkDateFormat(breedStart)
 								&& checkDateFormat(ringAt)
 								&& checkDateFormat(buyAt)
 								&& checkDateFormat(sellAt)
 								&& checkDateFormat(deathAt)
 								&& checkDateFormat(medicStart)
 								&& checkDateFormat(medicEnd)) {
 
 							BirdObject bird = fillBirdObject();
 							try {
 								if (bird.isNewOwnBird()
 										|| bird.isNewBuiedBird()) {
 									dbhelper.birdIntoDb(bird);
 
 									btnSpeichern.setEnabled(false);
 									btnCopy.setVisible(true);
 									JOptionPane.showMessageDialog(frame,
 											"Ringnr.: " + bird.getRingNo(),
 											"Neuer Vogel gespeichert",
 											JOptionPane.PLAIN_MESSAGE,
 											new ImageIcon(iconBird));
 								} else if (bird.isNewBirdWithNewSpecies()) {
 									bird.setSpeciesID(dbhelper.newSpecies(bird
 											.getNewSpecies()));
 									dbhelper.birdIntoDb(bird);
 
 									btnSpeichern.setEnabled(false);
 									btnCopy.setVisible(true);
 									JOptionPane.showMessageDialog(
 											frame,
 											"Ringnr.: " + bird.getRingNo()
 													+ "\nneue Art: "
 													+ bird.getNewSpecies(),
 											"Neuer Vogel gespeichert",
 											JOptionPane.PLAIN_MESSAGE,
 											new ImageIcon(iconBird));
 								} else {
 
 									JOptionPane.showMessageDialog(frame,
 											"keinen Vogel gespeichert",
 											"Fehler",
 											JOptionPane.ERROR_MESSAGE,
 											new ImageIcon(iconError));
 
 								}
 							} catch (AviculturaException e1) {
 								LOG.error(e1.getMessage(), e1);
 								JOptionPane.showMessageDialog(frame,
 										e1.getMessage(), "Fehler",
 										JOptionPane.ERROR_MESSAGE,
 										new ImageIcon(iconError));
 							}
 
 						}
 					} else {
 						if (newBirdFlag || copyBirdFlag) {
 							JOptionPane.showMessageDialog(frame, "Ringnr: "
 									+ ringNo.getText(), "Vogel vorhanden",
 									JOptionPane.ERROR_MESSAGE, new ImageIcon(
 											iconError));
 						} else {
 							int check = JOptionPane.showConfirmDialog(frame,
 									"Ring Nr.: " + ringNo.getText(),
 									"nderungen speichern?",
 									JOptionPane.YES_NO_CANCEL_OPTION,
 									JOptionPane.PLAIN_MESSAGE, new ImageIcon(
 											iconBird));
 
 							if (check == 0) {
 								BirdObject bird = fillBirdObject();
 								btnSpeichern.setEnabled(false);
 								if (dbhelper.birdModDb(bird, modBirdId)) {
 									JOptionPane.showMessageDialog(frame,
 											"Ringnr.: " + bird.getRingNo(),
 											"Vogel gespeichert",
 											JOptionPane.PLAIN_MESSAGE,
 											new ImageIcon(iconBird));
 								} else {
 									JOptionPane.showMessageDialog(frame,
 											"kein Vogel gespeichert", "Fehler",
 											JOptionPane.ERROR_MESSAGE,
 											new ImageIcon(iconError));
 								}
 
 							}
 						}
 					}
 				} catch (HeadlessException e1) {
 					LOG.error("Headless error", e1);
 				} catch (AviculturaException e1) {
 					e1.viewError(frame);
 				}
 
 			}
 		});
 
 	}
 
 	private BirdObject fillBirdObject() {
 		Map<String, String> speciesMapping = dbhelper.getSpeciesMapping();
 
 		BirdObject bird = new BirdObject();
 
 		LOG.info("newBirdFlag " + newBirdFlag);
 
 		if (newBirdFlag) {
 			bird.setSpeciesID(speciesMapping.get(speciesBox.getSelectedItem()));
 		} else {
 			bird.setSpeciesID(speciesMapping.get(lblSchnesittich.getText()));
 		}
 
 		bird.setNewSpecies(newSpecies.getText());
 
 		bird.addTestField(newSpecies);
 		bird.setRingNo(ringNo.getText());
 		bird.addTestField(ringNo);
 		bird.setColor(color.getText());
 		bird.addTestField(color);
 		bird.setMale(male.isSelected());
 		bird.setFemale(female.isSelected());
 		//bird.setComboBoxFemale(comboBoxFemale.getSelectedItem().toString());
 		//bird.setComboBoxMale(comboBoxMale.getSelectedItem().toString());
 		bird.setBreedStart(breedStart.getText());
 		bird.addTestField(breedStart);
 		bird.setRingAt(ringAt.getText());
 		bird.addTestField(ringAt);
 		bird.setBuyAt(buyAt.getText());
 		bird.addTestField(buyAt);
 		bird.setBuyFrom(buyFrom.getText());
 		bird.addTextPane(buyFrom);
 		bird.setSellAt(sellAt.getText());
 		bird.addTestField(sellAt);
 		bird.setSellTo(sellTo.getText());
 		bird.addTextPane(sellTo);
 		bird.setDeathAt(deathAt.getText());
 		bird.addTestField(deathAt);
 		bird.setDeathWhy(deathWhy.getText());
 		bird.addTextPane(deathWhy);
 		bird.setMedicStart(medicStart.getText());
 		bird.addTestField(medicStart);
 		bird.setMedicComment(medicComment.getText());
 		bird.addTextPane(medicComment);
 		bird.setMedicEnd(medicEnd.getText());
 		bird.addTestField(medicEnd);
 		bird.setMedicCheck(medicCheck.getText());
 		bird.addTextPane(medicCheck);
 		bird.setDocu(docu.getText());
 		bird.addTextPane(docu);
 		bird.setObtman(obtman.getText());
 		bird.addTextPane(obtman);
 		bird.setGov(gov.getText());
 		bird.addTextPane(gov);
 		bird.setRingType(chckbxOffen.isSelected());
 		bird.setBirdPairIdWithYear(birdPairField.getText());
 
 		bird.print();
 		return bird;
 	}
 
 	/**
 	 * wichtige varibalen
 	 * 
 	 * speciesBox newSpecies ringNo color male female comboBoxMale
 	 * comboBoxFemale breedStart ringAt buyAt buyFrom sellAt sellTo deathAt
 	 * deathWhy medicStart medicComment medicEnd medicCheck docu obtman gov
 	 * 
 	 * 
 	 */
 
 	private boolean checkDateFormat(JTextField dateField) {
 		if (!Helper.isDateFormat(dateField.getText())) {
 			dateField.setForeground(Color.RED);
 			JOptionPane.showMessageDialog(frame,
 					"Falsches Datum: " + dateField.getText(), "Fehler",
 					JOptionPane.ERROR_MESSAGE, new ImageIcon(iconError));
 
 			return false;
 		} else {
 			dateField.setForeground(Color.BLACK);
 			return true;
 		}
 	}
 
 	@Deprecated
 	private void fillGenderComboBox(String species) {
 		List<GenderObj> maleList;
 		List<GenderObj> femaleList;
 		try {
 
 			maleList = dbhelper.getGender(_1_0, species);
 			femaleList = dbhelper.getGender(_0_1, species);
 
 			if (comboBoxMale.getItemCount() > 0)
 				comboBoxMale.removeAllItems();
 			if (comboBoxFemale.getItemCount() > 0)
 				comboBoxFemale.removeAllItems();
 
 			comboBoxMale.addItem("N/A");
 			comboBoxFemale.addItem("N/A");
 
 			for (int i = 0; i < maleList.size(); i++) {
 				comboBoxMale.addItem(maleList.get(i).getFormatString());
 
 			}
 
 			for (int i = 0; i < femaleList.size(); i++) {
 				comboBoxFemale.addItem(femaleList.get(i).getFormatString());
 
 			}
 		} catch (AviculturaException e1) {
 			e1.viewError(frame);
 		}
 	}
 }
