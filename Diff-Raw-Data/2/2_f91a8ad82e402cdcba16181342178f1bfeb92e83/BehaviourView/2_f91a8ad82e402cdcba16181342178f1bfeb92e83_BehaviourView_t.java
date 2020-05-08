 package de.hattrickorganizer.gui.lineup.substitution;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import plugins.ISpielerPosition;
 import plugins.ISubstitution;
 import de.hattrickorganizer.gui.lineup.substitution.PositionSelectionEvent.Change;
 import de.hattrickorganizer.gui.model.CBItem;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.lineup.Substitution;
 import de.hattrickorganizer.tools.Helper;
 
 public class BehaviourView extends JPanel {
 
 	private static final long serialVersionUID = 6041242290064429972L;
 	private JComboBox playerComboBox;
 	private JComboBox behaviourComboBox;
 	private JComboBox positionComboBox;
 	private JComboBox redCardsComboBox;
 	private JComboBox standingComboBox;
 	private PositionChooser positionChooser;
 	private JSlider whenSlider;
 	private WhenTextField whenTextField;
 
 	public BehaviourView() {
 		initComponents();
 		addListeners();
 
 		Map<Integer, PlayerPositionItem> lineupPositions = SubstitutionDataProvider.getLineupPositions();
 
 		this.playerComboBox.setModel(new DefaultComboBoxModel(lineupPositions.values().toArray()));
 		this.playerComboBox.setSelectedItem(null);
 
 		List<PlayerPositionItem> positions = SubstitutionDataProvider.getFieldPositions(
 				ISpielerPosition.keeper, ISpielerPosition.startReserves);
 		this.positionComboBox.setModel(new DefaultComboBoxModel(positions.toArray()));
 		this.positionComboBox.setSelectedItem(null);
 
 		this.positionChooser.init(lineupPositions);
 
 	}
 
 	/**
 	 * Initializes the view with the given {@link ISubstitution}. The given
 	 * object will not be changed. To retrieve the data from the view, use
 	 * {@link #getSubstitution()} method.
 	 * 
 	 * @param sub
 	 *            the substitution to initialize the view.
 	 */
 	public void init(ISubstitution sub) {
 		ComboBoxModel model = this.playerComboBox.getModel();
 		for (int i = 0; i < model.getSize(); i++) {
 			if (((PlayerPositionItem) model.getElementAt(i)).getSpieler().getSpielerID() == sub.getPlayerIn()) {
 				playerComboBox.setSelectedItem(model.getElementAt(i));
 				break;
 			}
 		}
 
 		model = this.positionComboBox.getModel();
 		for (int i = 0; i < model.getSize(); i++) {
 			if (((PlayerPositionItem) model.getElementAt(i)).getPosition().byteValue() == sub.getPos()) {
 				positionComboBox.setSelectedItem(model.getElementAt(i));
 				break;
 			}
 		}
 
 		Helper.markierenComboBox(this.behaviourComboBox, sub.getBehaviour());
 		Helper.markierenComboBox(this.redCardsComboBox, sub.getCard());
 		Helper.markierenComboBox(this.standingComboBox, sub.getStanding());
 
 		this.whenTextField.setValue(Integer.valueOf(sub.getMatchMinuteCriteria()));
 	}
 
 	/**
 	 * Gets a new {@link ISubstitution} which represents the values chosen in
 	 * the view. Note that <i>new</i> is returned and not the one which may be
 	 * provided to the {@link #init(ISubstitution)} method.
 	 * 
 	 * @return
 	 */
 	public ISubstitution getSubstitution() {
 		ISubstitution sub = new Substitution();
 		sub.setBehaviour((byte) getSelectedId(this.behaviourComboBox));
 		sub.setCard((byte) getSelectedId(this.redCardsComboBox));
 		sub.setMatchMinuteCriteria(((Integer) this.whenTextField.getValue()).byteValue());
 		sub.setOrderType(ISubstitution.BEHAVIOUR);
 		PlayerPositionItem item = (PlayerPositionItem) this.playerComboBox.getSelectedItem();
 		if (item != null) {
 			sub.setPlayerIn(item.getSpieler().getSpielerID());
 		}
 		// sub.setPlayerOrderId(id); ???????????
 		item = (PlayerPositionItem) this.positionComboBox.getSelectedItem();
 		if (item != null) {
 			if (item.getSpieler() != null) {
 				sub.setPlayerOut(item.getSpieler().getSpielerID());
 			}
 			sub.setPos(item.getPosition().byteValue());
 		}
 		sub.setStanding((byte) getSelectedId(this.standingComboBox));
 		return sub;
 	}
 
 	private int getSelectedId(JComboBox comboBox) {
 		CBItem item = (CBItem) comboBox.getSelectedItem();
 		if (item != null) {
 			return item.getId();
 		}
 		return -1;
 	}
 
 	private void addListeners() {
 		// ChangeListener that will updates the "when" textfield with the number
 		// of minutes when slider changed
 		this.whenSlider.addChangeListener(new ChangeListener() {
 
 			public void stateChanged(ChangeEvent e) {
 				whenTextField.setValue(Integer.valueOf(whenSlider.getModel().getValue()));
 			}
 		});
 
 		// PropertyChangeListener that will update the slider when value in the
 		// "when" textfield changed
 		this.whenTextField.addPropertyChangeListener("value", new PropertyChangeListener() {
 
 			public void propertyChange(PropertyChangeEvent evt) {
 				Integer value = (Integer) whenTextField.getValue();
 				if (value != null) {
 					whenSlider.setValue(value.intValue());
 				} else {
 					whenSlider.setValue(0);
 				}
 			}
 		});
 
 		// ItemListener that will update the PositionChooser if selection in the
 		// position combobox changes
 		this.positionComboBox.addItemListener(new ItemListener() {
 
 			public void itemStateChanged(ItemEvent e) {
 				PlayerPositionItem item = (PlayerPositionItem) positionComboBox.getSelectedItem();
 				if (item != null) {
 					positionChooser.select(Integer.valueOf(item.getPosition()));
 				} else {
 					positionChooser.select(null);
 				}
 			}
 		});
 
 		// PositionSelectionListener that will update position combobox
 		// selection if selection in the PositionChooser changes
 		this.positionChooser.addPositionSelectionListener(new PositionSelectionListener() {
 
 			public void selectionChanged(PositionSelectionEvent event) {
 				if (event.getChange() == Change.SELECTED) {
 					for (int i = 0; i < positionComboBox.getModel().getSize(); i++) {
 						PlayerPositionItem item = (PlayerPositionItem) positionComboBox.getModel()
 								.getElementAt(i);
 						if (event.getPosition().equals(item.getPosition())) {
 							if (item != positionComboBox.getSelectedItem()) {
 								positionComboBox.setSelectedItem(item);
 							}
 							break;
 						}
 					}
 				} else {
 					if (positionComboBox.getSelectedItem() != null) {
 						positionComboBox.setSelectedItem(null);
 					}
 				}
 			}
 		});
 	}
 
 	private void initComponents() {
 		setLayout(new GridBagLayout());
 
 		JLabel playerLabel = new JLabel(HOVerwaltung.instance().getLanguageString("subs.Player"));
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.anchor = GridBagConstraints.WEST;
 		gbc.insets = new Insets(10, 10, 4, 2);
 		add(playerLabel, gbc);
 
 		this.playerComboBox = new JComboBox();
 		Dimension comboBoxSize = new Dimension(200, this.playerComboBox.getPreferredSize().height);
 		this.playerComboBox.setMinimumSize(comboBoxSize);
 		this.playerComboBox.setPreferredSize(comboBoxSize);
 		gbc.gridx = 1;
 		gbc.insets = new Insets(10, 2, 4, 10);
 		add(this.playerComboBox, gbc);
 
 		JLabel behaviourLabel = new JLabel(HOVerwaltung.instance().getLanguageString("subs.Behavior"));
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.anchor = GridBagConstraints.WEST;
 		gbc.insets = new Insets(4, 10, 4, 2);
 		add(behaviourLabel, gbc);
 
 		this.behaviourComboBox = new JComboBox(SubstitutionDataProvider.getBehaviourItems());
 		this.behaviourComboBox.setMinimumSize(comboBoxSize);
 		this.behaviourComboBox.setPreferredSize(comboBoxSize);
 		gbc.gridx = 1;
 		gbc.insets = new Insets(4, 2, 4, 10);
 		add(this.behaviourComboBox, gbc);
 
		JLabel whenLabel = new JLabel(HOVerwaltung.instance().getLanguageString("subs.When"));
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.insets = new Insets(4, 10, 4, 2);
 		add(whenLabel, gbc);
 
 		this.whenTextField = new WhenTextField(HOVerwaltung.instance()
 				.getLanguageString("subs.MinuteAnytime"), HOVerwaltung.instance().getLanguageString(
 				"subs.MinuteAfterX"));
 		Dimension textFieldSize = new Dimension(200, this.whenTextField.getPreferredSize().height);
 		this.whenTextField.setMinimumSize(textFieldSize);
 		this.whenTextField.setPreferredSize(textFieldSize);
 		gbc.gridx = 1;
 		gbc.insets = new Insets(4, 2, 4, 10);
 		add(this.whenTextField, gbc);
 
 		this.whenSlider = new JSlider(0, 119, 0);
 		gbc.gridx = 1;
 		gbc.gridy++;
 		gbc.insets = new Insets(0, 2, 8, 10);
 		add(this.whenSlider, gbc);
 
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 2;
 		gbc.insets = new Insets(8, 4, 8, 4);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.weightx = 1.0;
 		add(new Divider(HOVerwaltung.instance().getLanguageString("subs.AdvancedConditions")), gbc);
 
 		JLabel positionLabel = new JLabel(HOVerwaltung.instance().getLanguageString("subs.Position"));
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.insets = new Insets(4, 10, 4, 2);
 		gbc.fill = GridBagConstraints.NONE;
 		gbc.weightx = 0.0;
 		add(positionLabel, gbc);
 
 		this.positionComboBox = new JComboBox();
 		this.positionComboBox.setMinimumSize(comboBoxSize);
 		this.positionComboBox.setPreferredSize(comboBoxSize);
 		gbc.gridx = 1;
 		gbc.insets = new Insets(4, 2, 4, 10);
 		add(this.positionComboBox, gbc);
 
 		this.positionChooser = new PositionChooser();
 		gbc.gridy = 6;
 		gbc.insets = new Insets(2, 10, 8, 10);
 		add(this.positionChooser, gbc);
 
 		JLabel redCardsLabel = new JLabel(HOVerwaltung.instance().getLanguageString("subs.RedCard"));
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.insets = new Insets(4, 10, 4, 2);
 		gbc.fill = GridBagConstraints.NONE;
 		gbc.weightx = 0.0;
 		add(redCardsLabel, gbc);
 
 		this.redCardsComboBox = new JComboBox(SubstitutionDataProvider.getRedCardItems());
 		this.redCardsComboBox.setMinimumSize(comboBoxSize);
 		this.redCardsComboBox.setPreferredSize(comboBoxSize);
 		gbc.gridx = 1;
 		gbc.insets = new Insets(4, 2, 4, 10);
 		add(this.redCardsComboBox, gbc);
 
 		JLabel standingLabel = new JLabel(HOVerwaltung.instance().getLanguageString("subs.Standing"));
 		gbc.gridx = 0;
 		gbc.gridy++;
 		gbc.insets = new Insets(4, 10, 4, 2);
 		add(standingLabel, gbc);
 
 		this.standingComboBox = new JComboBox(SubstitutionDataProvider.getStandingItems());
 		this.standingComboBox.setMinimumSize(comboBoxSize);
 		this.standingComboBox.setPreferredSize(comboBoxSize);
 		gbc.gridx = 1;
 		gbc.insets = new Insets(4, 2, 4, 10);
 		add(this.standingComboBox, gbc);
 	}
 
 }
