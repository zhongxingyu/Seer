 package webApplication.grafica;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.SystemColor;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.plaf.basic.BasicTabbedPaneUI;
 
 import webApplication.business.Componente;
 import webApplication.business.ComponenteAlternative;
 import webApplication.business.ComponenteComposto;
 import webApplication.business.ComponenteSemplice;
 import webApplication.business.Immagine;
 import webApplication.business.Link;
 import webApplication.business.Testo;
 
 public class Wizard extends JDialog implements ActionListener, DocumentListener, ListDataListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8706297359676605479L;
 
 	protected boolean returnValue = false;
 
 	private static final String BASEELEMENTNAME = "Element";
 	private static int count = 0;// TODO spostare nel MainWindow
 
 	private static final String BASETITLE = "Create new element";
 
 	private static JPanel contentPane;
 	private static JTabbedPane tabbedPane;
 	protected static JTextField name;
 	private static JTextField category;
 	private static JComboBox choice_type;
 	private static JComboBox choice_emphasis;
 	private static JComboBox choice_visibility;
 
 	private PannelloText pannelloText;
 	private PannelloImage pannelloImage;
 	private PannelloLink pannelloLink;
 	private PannelloAlt pannelloAlt;
 	private PannelloComp pannelloComp;
 
 	private static JButton btnExit;
 	private static JButton btnBack;
 	private static JButton btnNext;
 	private static JButton btnDone;
 
 	// TAB TITLE & ICON & TOOLTIP
 	private static final String FIRSTSTEPTITLE = "1. Identification";
 	private static final String FIRSTSTEPTOOLTIP = "Specify the name and type of the element";
 
 	private static final String SECONDSTEPTITLE = "2. Presentation";
 	private static final String SECONDSTEPTOOLTIP = "Specify visualization properties of the element";
 
 	private static final String THIRDSTEPTITLE = "3. Content";
 	private static final String THIRDSTEPTOOLTIP = "Specify what the element represents";
 
 	// BUTTONS
 	private static final String NEXTTEXT = "Next";
 	private static final String EXITTEXT = "Cancel";
 	private static final String BACKTEXT = "Back";
 	private static final String DONETEXT = "Done";
 	private static final String NEXTACTION = "Next";
 	private static final String EXITACTION = "Cancel";
 	private static final String BACKACTION = "Back";
 	protected static final String DONEACTION = "Done";
 
 	private final static String[] tipi = { Testo.TEXTTYPE, Immagine.IMAGETYPE,
 			Link.LINKTYPE, ComponenteAlternative.ALTERNATIVETYPE,
 			ComponenteComposto.COMPOSTOTYPE };
 
 	/**
 	 * Crea il frame
 	 * @param owner	Il frame che ha richiesto la creazione del wizard
 	 */
 	public Wizard(JFrame owner) {
 		super(owner, ModalityType.APPLICATION_MODAL);
 		setResizable(false);
 		setTitle(BASETITLE);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 477, 374);
 
 		// centro la finestra
 		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension ownerDim = owner.getSize();
 		int w = getWidth();
 		int h = getHeight();
 		setLocation((screenDim.width - ownerDim.width) / 2 + (ownerDim.width - w) / 2, (screenDim.height - ownerDim.height) / 2	+ (ownerDim.height - h) / 2);
 
 		contentPane = new JPanel();
 		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
 		tabbedPane.setBounds(0, 0, 471, 374);
 		//Nasconde i tab: siamo sicuri di non volerli? fanno "Scena"
 		tabbedPane.setUI(new BasicTabbedPaneUI() {
 			@Override
 			protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
 				return 0;
 			}
 		});
 		contentPane.add(tabbedPane);
 
 		// Primo Tabbed Panel
 		JPanel firstStep = new JPanel();
 		firstStep.setLayout(null);
 		tabbedPane.addTab(FIRSTSTEPTITLE, null, firstStep, FIRSTSTEPTOOLTIP);
 
 		createBreadCumb(firstStep, 1);
 
 		JLabel lblName = new JLabel("Name:");
 		lblName.setBounds(125, 109, 46, 14);
 		firstStep.add(lblName);
 
 		createButtonsBar(firstStep, 1);
 		
 		name = new JTextField();
 		name.setBounds(181, 106, 174, 22);
 		name.getDocument().addDocumentListener(this);
 		name.setText(MainWindow.setDefaultName());
 		firstStep.add(name);
 
 		JLabel lblType = new JLabel("Type:");
 		lblType.setBounds(130, 168, 46, 14);
 		firstStep.add(lblType);
 
 		choice_type = new JComboBox(tipi);
 		choice_type.setBounds(181, 165, 174, 20);
 		firstStep.add(choice_type);
 
 		// Secondo Tabbed Panel
 		JPanel secondStep = new JPanel();
 		secondStep.setLayout(null);
 		tabbedPane.addTab(SECONDSTEPTITLE, null, secondStep, SECONDSTEPTOOLTIP);
 		tabbedPane.setEnabledAt(1, false);
 
 		createBreadCumb(secondStep, 2);
 
 		JLabel lblCategoryIdentifier = new JLabel("Category identifier:");
 		lblCategoryIdentifier.setBounds(56, 94, 108, 14);
 		secondStep.add(lblCategoryIdentifier);
 
 		createButtonsBar(secondStep, 2);
 		
 		category = new JTextField();
 		category.setBounds(181, 92, 174, 22);
 		category.getDocument().addDocumentListener(this);
 		category.setText("Category0");
 		secondStep.add(category);
 
 		JLabel lblImportance = new JLabel("Necessity:");
 		lblImportance.setBounds(105, 193, 108, 14);
 		secondStep.add(lblImportance);
 
 		choice_visibility = new JComboBox(MainWindow.necessity);
 		choice_visibility.setBounds(180, 192, 174, 20);
 		choice_visibility.setSelectedIndex(1);
 		secondStep.add(choice_visibility);
 
 		JLabel lblEnphasize = new JLabel("Emphasizes:");
 		lblEnphasize.setBounds(91, 144, 108, 14);// 195
 		secondStep.add(lblEnphasize);
 
 		choice_emphasis = new JComboBox(MainWindow.emphasis);
 		choice_emphasis.setBounds(180, 143, 174, 20);// 192
 		choice_emphasis.setSelectedIndex(1);
 		secondStep.add(choice_emphasis);
 
 		// Terzo Tabbed Panel
 		JPanel thirdStep = new JPanel();
 		thirdStep.setLayout(null);
 		tabbedPane.addTab(THIRDSTEPTITLE, null, thirdStep, THIRDSTEPTOOLTIP);
 		tabbedPane.setEnabledAt(2, false);
 	}
 
 	/**
 	 * Crea il pannello che gestisce la visualizzazione degli step del wizard
 	 * @param panel			Il pannello in cui inserirlo
 	 * @param currentStep	Lo step del wizard cui fa riferimento
 	 */
 	private void createBreadCumb(JPanel panel, int currentStep) {
 		JLabel lblStep = new JLabel("Step: ");
 		lblStep.setBounds(26, 21, 46, 14);
 		panel.add(lblStep);
 
 		JPanel stepOneIndicator = new JPanel();
 		if (currentStep == 1) {
 			stepOneIndicator.setBackground(new Color(139,139,139));
 			stepOneIndicator.setBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(139,139,139), null));
 		} else {
 			stepOneIndicator.setBackground(SystemColor.control);
 			stepOneIndicator.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
 		}
 		stepOneIndicator.setBounds(61, 7, 46, 43);
 		panel.add(stepOneIndicator);
 		stepOneIndicator.setLayout(null);
 
 		JLabel one = new JLabel("1");
 		one.setHorizontalAlignment(SwingConstants.CENTER);
 		one.setBounds(10, 0, 26, 43);
 		one.setFont(new Font("Tahoma", Font.PLAIN, 20));
 		stepOneIndicator.add(one);
 
 		JPanel stepTwoIndicator = new JPanel();
 		if (currentStep == 2) {
 			stepTwoIndicator.setBackground(new Color(139,139,139));
 			stepTwoIndicator.setBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(139,139,139), null));
 		} else {
 			stepTwoIndicator.setBackground(SystemColor.control);
 			stepTwoIndicator.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
 		}
 		stepTwoIndicator.setLayout(null);
 		stepTwoIndicator.setBounds(117, 7, 46, 43);
 		panel.add(stepTwoIndicator);
 
 		JLabel two = new JLabel("2");
 		two.setHorizontalAlignment(SwingConstants.CENTER);
 		two.setFont(new Font("Tahoma", Font.PLAIN, 20));
 		two.setBounds(10, 0, 26, 43);
 		stepTwoIndicator.add(two);
 
 		JPanel stepThreeIndicator = new JPanel();
 		if (currentStep == 3) {
 			stepThreeIndicator.setBackground(new Color(139,139,139));
 			stepThreeIndicator.setBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(139,139,139), null));
 		} else {
 			stepThreeIndicator.setBackground(SystemColor.control);
 			stepThreeIndicator.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
 		}
 		stepThreeIndicator.setLayout(null);
 		stepThreeIndicator.setBounds(173, 7, 46, 43);
 		panel.add(stepThreeIndicator);
 
 		JLabel three = new JLabel("3");
 		three.setHorizontalAlignment(SwingConstants.CENTER);
 		three.setFont(new Font("Tahoma", Font.PLAIN, 20));
 		three.setBounds(10, 0, 26, 43);
 		stepThreeIndicator.add(three);
 
 		JSeparator separator = new JSeparator();
 		separator.setForeground(Color.BLACK);
 		separator.setBounds(10, 54, 446, 2);
 		panel.add(separator);
 	}
 
 	/**
 	 * Crea la barra dei bottoni Back, Next, Cancel
 	 * @param panel			Il pannello in cui inserirla
 	 * @param currentStep	Lo step del wizard cui fa riferimento
 	 */
 	private void createButtonsBar(JPanel panel, int currentStep) {
 		JPanel buttonsPanel = new JPanel();
 		buttonsPanel = new JPanel();
 		buttonsPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
 		buttonsPanel.setLayout(null);
 		buttonsPanel.setBounds(10, 263, 446, 49);
 		panel.add(buttonsPanel);
 
 		btnNext = new JButton(NEXTTEXT);
 		// button_next_s1.addActionListener(this);
 		btnNext.setActionCommand(NEXTACTION);
 		btnNext.addActionListener(this);
 		btnNext.setBounds(350, 11, 86, 27);
 
 		btnBack = new JButton(BACKTEXT);
 		btnBack.setActionCommand(BACKACTION);
 		btnBack.addActionListener(this);
 		btnBack.setBounds(260, 11, 86, 27);
 
 		btnExit = new JButton(EXITTEXT);
 		btnExit.setActionCommand(EXITACTION);
 		btnExit.addActionListener(this);
 		btnExit.setBounds(10, 11, 86, 27);
 		buttonsPanel.add(btnExit);
 
 		btnDone = new JButton(DONETEXT);
 		btnDone.setActionCommand(DONEACTION);
 		//btnDone.setFont(new Font(btnDone.getFont().getName(), Font.BOLD, btnDone.getFont().getSize() + 2));
 		btnDone.setFont(new Font("Arial Black", Font.PLAIN, btnDone.getFont().getSize()+1 ));
 
 		btnDone.setEnabled(false);
 		btnDone.addActionListener(this);
 		btnDone.setBounds(350, 11, 86, 27);
 
 		switch (currentStep) {
 		case 1:
 			buttonsPanel.add(btnNext);
 			break;
 		case 2:
 			buttonsPanel.add(btnNext);
 			buttonsPanel.add(btnBack);
 			break;
 		case 3:
 			buttonsPanel.add(btnDone);
 			buttonsPanel.add(btnBack);
 			break;
 		}
 	}
 
 	/**
 	 * Costruisce il pannello relativo ad un elemento Testo
 	 */
 	private void buildTextPanel() {
 		JPanel thirdPanel = (JPanel) tabbedPane.getComponent(2);
 		thirdPanel.removeAll();
 
 		createBreadCumb(thirdPanel, 3);
 
 		// NOTA: per la history non ricreo l'elemento se ritorno a questo passo
 		// dopo essere tornato indietro
 		if (pannelloText == null) {
 			pannelloText = new PannelloText();
 		}
 		pannelloText.setLocation(25, 61);
 		pannelloText.textArea.getDocument().addDocumentListener(this);
 		pannelloText.isCorrect();
 		thirdPanel.add(pannelloText);
 
 		createButtonsBar(thirdPanel, 3);
 	}
 
 	/**
 	 * Costruisce il pannello relativo ad un elemento Immagine
 	 */
 	private void buildImagePanel() {
 		JPanel thirdPanel = (JPanel) tabbedPane.getComponent(2);
 		thirdPanel.removeAll();
 
 		createBreadCumb(thirdPanel, 3);
 
 		// NOTA: per la history non ricreo l'elemento se ritorno a questo passo
 		// dopo essere tornato indietro
 		if (pannelloImage == null) {
 			pannelloImage = new PannelloImage();
 		}
 		pannelloImage.setLocation(25, 61);
 		pannelloImage.imagepath.getDocument().addDocumentListener(this);
 		pannelloImage.isCorrect();
 		thirdPanel.add(pannelloImage);
 
 		createButtonsBar(thirdPanel, 3);
 		((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled(pannelloImage.isCorrect());
 	}
 
 	/**
 	 * Costruisce il pannello relativo ad un elemento Link
 	 */
 	private void buildLinkPanel() {
 		JPanel thirdPanel = (JPanel) tabbedPane.getComponent(2);
 		thirdPanel.removeAll();
 
 		createBreadCumb(thirdPanel, 3);
 
 		if (pannelloLink == null) {
 			pannelloLink = new PannelloLink();
 		}
 		pannelloLink.setLocation(25, 61);
 		pannelloLink.urlPath.getDocument().addDocumentListener(this);
 		pannelloLink.urlText.getDocument().addDocumentListener(this);
 		pannelloLink.isPathCorrect();
 		pannelloLink.isTextCorrect();
 		thirdPanel.add(pannelloLink);
 
 		createButtonsBar(thirdPanel, 3);
 	}
 
 	/**
 	 * Costruisce il pannello relativo ad un elemento Alternative
 	 */
 	private void buildAlternativePanel() {
 		JPanel thirdPanel = (JPanel) tabbedPane.getComponent(2);
 		thirdPanel.removeAll();
 
 		createBreadCumb(thirdPanel, 3);
 
 		// NOTA: per la history non ricreo l'elemento se ritorno a questo passo
 		// dopo essere tornato indietro
 		if (pannelloAlt == null)
 			pannelloAlt = new PannelloAlt(true);
 		pannelloAlt.setLocation(25, 61);
 		pannelloAlt.list_components.getModel().addListDataListener(this);
 		pannelloAlt.isCorrect();
 		thirdPanel.add(pannelloAlt);
 
 		createButtonsBar(thirdPanel, 3);
 	}
 
 	/**
 	 * Costruisce il pannello relativo ad un elemento Composite
 	 */
 	private void buildCompositePanel() {
 		JPanel thirdPanel = (JPanel) tabbedPane.getComponent(2);
 		thirdPanel.removeAll();
 
 		createBreadCumb(thirdPanel, 3);
 
 		// NOTA: per la history non ricreo l'elemento se ritorno a questo passo
 		// dopo essere tornato indietro
 		if (pannelloComp == null)
 			pannelloComp = new PannelloComp(true);
 		pannelloComp.setLocation(25, 61);
 		pannelloComp.list_components.getModel().addListDataListener(this);
 		thirdPanel.add(pannelloComp);
 		
 		createButtonsBar(thirdPanel, 3);
 	}
 
 	protected boolean showDialog() {
 		setVisible(true);
 		return returnValue;
 	}
 
 	/**
 	 * Costruisce il nuovo componente
 	 * @return Il nuovo componente
 	 */
 	protected Componente buildNewComponent() {
 		if (choice_type.getSelectedItem().equals(Testo.TEXTTYPE)) {
 			Testo newTesto = new Testo(name.getText(), category.getText(), choice_visibility.getSelectedIndex(), choice_emphasis.getSelectedIndex(), pannelloText.getText());
 			return newTesto;
 		} else if (choice_type.getSelectedItem().equals(Immagine.IMAGETYPE)) {
 			Immagine newImage = new Immagine(name.getText(), category.getText(), choice_visibility.getSelectedIndex(), choice_emphasis.getSelectedIndex(), pannelloImage.getPath());
 			return newImage;
 		} else if (choice_type.getSelectedItem().equals(Link.LINKTYPE)) {
 			Link newLink = new Link(name.getText(), category.getText(), choice_visibility.getSelectedIndex(), choice_emphasis.getSelectedIndex(), pannelloLink.getPath(),
 					pannelloLink.getText());
 			return newLink;
 		} else if (choice_type.getSelectedItem().equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 			ComponenteAlternative compAlt = new ComponenteAlternative(name.getText(), category.getText(), choice_visibility.getSelectedIndex(), choice_emphasis.getSelectedIndex());
 			compAlt.setOpzioni(pannelloAlt.getOpzioni());
 			return compAlt;
 		} else if (choice_type.getSelectedItem().equals(
 				ComponenteComposto.COMPOSTOTYPE)) {
 			ComponenteComposto compComp = new ComponenteComposto(name.getText(), category.getText(), choice_visibility.getSelectedIndex(), choice_emphasis.getSelectedIndex());
 			compComp.setOpzioni(pannelloComp.getOpzioni());
 			return compComp;
 		}
 		return null;
 	}
 
 	/**
 	 * Ritorna un vettore contenente gli indici dei nodi dell'albero principale da spostare nel nuovo nodo
 	 * @return Il vettore degli indici nell'albero principale
 	 */
 	protected Vector<Integer> getOriginalIndexes() {
 		if (choice_type.getSelectedItem().equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 			return pannelloAlt.getOriginalIndexes();
 		} else if (choice_type.getSelectedItem().equals(ComponenteComposto.COMPOSTOTYPE)) {
 			return pannelloComp.getOriginalIndexes();
 		}
 		return null;
 	}
 
 	/**
 	 * Ritorna un vettore contenente gli indici, relativi al nodo in creazione, di quelli da spostare dell'albero principale
 	 * @return	Il vettore degli indici nel nuovo nodo
 	 */
 	protected Vector<Integer> getNewIndexes() {
 		if (choice_type.getSelectedItem().equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 			return pannelloAlt.getNewIndexes();
 		} else if (choice_type.getSelectedItem().equals(ComponenteComposto.COMPOSTOTYPE)) {
 			return pannelloComp.getNewIndexes();
 		}
 		return null;
 	}
 	
 	/**
 	 * Aggiorna lo stato dei bottoni e dei componenti verificando la presenza di errori
 	 * @param e	L'evento che ha scatenato la verifica
 	 */
 	private void updateComponent(DocumentEvent e) {
 		if ((tabbedPane.getSelectedIndex()==0)) {
 			Utils.redify(name, Utils.isBlank(name)||(MainWindow.albero.nameExists(name.getText())));
 			((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled((!Utils.isBlank(name)&&(!MainWindow.albero.nameExists(name.getText()))));
 		} else if((tabbedPane.getSelectedIndex()==1)) {
 			Utils.redify(category, Utils.isBlank(category));
 			((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled(!Utils.isBlank(category));
 		} else if(tabbedPane.getSelectedIndex()==2) {
 			if (choice_type.getSelectedIndex()==0) {
 				((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled(pannelloText.isCorrect());
 			} else if (choice_type.getSelectedIndex()==1) {
 				((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled(pannelloImage.isCorrect());
 			} else if (choice_type.getSelectedIndex()==2) {
 				((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled((pannelloLink.isPathCorrect() && (pannelloLink.isTextCorrect())));
 			}
 		}
 
 	}
 
 	/**
 	 * Verifica se la stringa corrente rappresenta il nome di un nodo nell'albero o dell'elemento corrente
 	 * @param s	Il nome da verificare
 	 * @return	True se il nome esiste, False altrimenti
 	 */
 	public boolean nameExistsAll(String s) {
 		Vector<ComponenteSemplice> listOggetti = new Vector<ComponenteSemplice>();
 		if (choice_type.getSelectedItem().equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 			listOggetti = pannelloAlt.getOpzioni();
 		} else if (choice_type.getSelectedItem().equals(ComponenteComposto.COMPOSTOTYPE)) {
 			listOggetti = pannelloComp.getOpzioni();
 		}
 		boolean b = false;
 		for (int i = 0;i < listOggetti.size(); i++)
 			{
 				b |= s.equals(listOggetti.get(i).getNome());
 			}
 		return (MainWindow.albero.nameExists(s) || s.equals(name.getText()) || b); 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(BACKACTION)) {
 			// gestisco il bottone Back
 			tabbedPane.setEnabledAt(tabbedPane.getSelectedIndex() - 1, true);
 			tabbedPane.setEnabledAt(tabbedPane.getSelectedIndex(), false);
 			tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() - 1);
 		} else if (e.getActionCommand().equals(NEXTACTION)) {
 			// gestisco il bottone Next
 			tabbedPane.setEnabledAt(tabbedPane.getSelectedIndex() + 1, true);
 			tabbedPane.setEnabledAt(tabbedPane.getSelectedIndex(), false);
 			tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
 			if ((tabbedPane.getSelectedIndex()) == 2) {
 				if (tipi[choice_type.getSelectedIndex()].equals(Testo.TEXTTYPE)) {
 					buildTextPanel();
 				} else if (tipi[choice_type.getSelectedIndex()].equals(Immagine.IMAGETYPE)) {
 					buildImagePanel();
 				} else if (tipi[choice_type.getSelectedIndex()].equals(Link.LINKTYPE)) {
 					buildLinkPanel();
 				} else if (tipi[choice_type.getSelectedIndex()].equals(ComponenteAlternative.ALTERNATIVETYPE)) {
 					buildAlternativePanel();
 				} else if (tipi[choice_type.getSelectedIndex()].equals(ComponenteComposto.COMPOSTOTYPE)) {
 					buildCompositePanel();
 				}
 			}
 		} else if (e.getActionCommand().equals(EXITACTION)) {
 			// gestisco il bottone EXIT
 			dispose();
 		} else if (e.getActionCommand().equals(DONEACTION)) {
 			// gestisco il bottone Done
 			returnValue = true;
 			dispose();
 		}
 		if (!((tabbedPane.getSelectedIndex()) == 0)) {
 			setTitle(BASETITLE + " - " + name.getText() + " ["+ choice_type.getSelectedItem() + "]");
 		} else {
 			setTitle(BASETITLE);
 		}
 	}
 	
 //	private void manageTooltips(Component component, boolean b) { if
 //		(component == textField_imagepath){ if(b)
 //			textField_imagepath.setToolTipText(AddNew.URL_EMPTY); else
 //				textField_imagepath.setToolTipText(AddNew.URL); } else if (component ==
 //				name){ if(b) name.setToolTipText(AddNew.NAME_EMPTY); else
 //					name.setToolTipText(AddNew.NAME); if
 //					(MainWindow.nameExistsAll(name.getText()))
 //						name.setToolTipText(AddNew.NAME_EXISTING); } }
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void changedUpdate(DocumentEvent e) {
 		updateComponent(e);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void insertUpdate(DocumentEvent e) {
 		updateComponent(e);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeUpdate(DocumentEvent e) {
 		updateComponent(e);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void intervalAdded(ListDataEvent e) {
 		contentsChanged(e);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void intervalRemoved(ListDataEvent e) {
 		contentsChanged(e);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void contentsChanged(ListDataEvent e) {
 		if (choice_type.getSelectedIndex() == 3) {
 			((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled(pannelloAlt.isCorrect());
 		} else if (choice_type.getSelectedIndex() == 4) {
 			((JButton)((JPanel)((JPanel)tabbedPane.getSelectedComponent()).getComponent(6)).getComponent(1)).setEnabled(pannelloComp.isCorrect());
 		}
 	}
 
 }
