 package no.thunaes.petter.svg.app.gui.domain;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import no.smidsrod.robin.svg.library.Chart;
 import no.smidsrod.robin.svg.library.Item;
 import no.thunaes.petter.svg.app.Controller;
 
 public class ItemPanel extends JPanel implements ActionListener,
 		DocumentListener, ItemListener, MouseListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private JButton editValuesButton = new JButton("Values");
 	private JButton removeItemButton = new JButton("Remove");
 
 	private JLabel nameLabel = new JLabel("Label");
 	private JLabel colorLabel = new JLabel("Color");
 	private JLabel highlightLabel = new JLabel("Highlighted?");
 
 	private JLabel itemColorLabel = new JLabel("");
 	private JTextField itemNameText = new JTextField(10);
 	private JCheckBox itemHighlightedCheckbox = new JCheckBox();
 
 	private ValueEditWindow valueEditWindow;
 	private Item item;
 
 	public ItemPanel(Item item) {
 		this.item = item;
 
 		setLayout(new FlowLayout(FlowLayout.LEFT));
 		setPreferredSize(new Dimension(390, 70));
 
 		itemColorLabel.setOpaque(true);
 		itemColorLabel.setBorder(BorderFactory.createEtchedBorder());
 		itemColorLabel.setBackground(item.getColor());
 
 		itemHighlightedCheckbox.setSelected(item.isHighlighted());
 		itemNameText.setText(item.getName());
 
 		Dimension buttonPreferredSize = new Dimension(80, 25);
 		editValuesButton.setPreferredSize(buttonPreferredSize);
 		removeItemButton.setPreferredSize(buttonPreferredSize);
 
 		nameLabel.setPreferredSize(new Dimension(115, 15));
 
 		Dimension colorLabelPreferredSize = new Dimension(70, 15);
 		colorLabel.setPreferredSize(colorLabelPreferredSize);
 		itemColorLabel.setPreferredSize(colorLabelPreferredSize);
 
 		Dimension highlightPreferredSize = new Dimension(73, 15);
 		highlightLabel.setPreferredSize(highlightPreferredSize);
 		itemHighlightedCheckbox.setPreferredSize(highlightPreferredSize);
 
 		itemNameText.getDocument().addDocumentListener(this);
 		itemHighlightedCheckbox.addItemListener(this);
 		itemColorLabel.addMouseListener(this);
 		editValuesButton.addActionListener(this);
 		removeItemButton.addActionListener(this);
 
 		add(nameLabel);
 		add(colorLabel);
 		add(highlightLabel);
 		add(editValuesButton);
 		add(itemNameText);
 		add(itemColorLabel);
 		add(itemHighlightedCheckbox);
 		add(removeItemButton);
 	}
 
 	public void addValuePanel(Chart c) {
 		getValueEditWindow().addValuePanel(c);
 	}
 
 	public void removeValuePanel(ValuePanel v) {
 		getValueEditWindow().removeValuePanel(v);
 	}
 
 	public void generateValueFields(ValuePanel v, Chart c) {
 		getValueEditWindow().generateValueFields(v, c);
 	}
 
 	public Item getItem() {
 		return item;
 	}
 
 	private ValueEditWindow getValueEditWindow() {
 		if (valueEditWindow == null) {
 			valueEditWindow = new ValueEditWindow(this);
 		}
 		return valueEditWindow;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(editValuesButton)) {
 			getValueEditWindow().setVisible(true);
			Controller.addValuePanel(this);
 		}
 		if (e.getSource().equals(removeItemButton)) {
 			Controller.removeItemPanel(this);
 		}
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		handleDocumentEvent(e);
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		handleDocumentEvent(e);
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		handleDocumentEvent(e);
 	}
 
 	private void handleDocumentEvent(DocumentEvent e) {
 		if (e.getDocument().equals(itemNameText.getDocument())) {
 			item.setName(itemNameText.getText());
 			getValueEditWindow().updateTitle();
 		}
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getStateChange() == ItemEvent.SELECTED) {
 			item.setHighlighted(true);
 		}
 		if (e.getStateChange() == ItemEvent.DESELECTED) {
 			item.setHighlighted(false);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		final JColorChooser colorChooser = new JColorChooser();
 		ActionListener ok = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				itemColorLabel.setBackground(colorChooser.getColor());
 				item.setColor(itemColorLabel.getBackground());
 			}
 		};
 		JDialog colorChooserDialog = JColorChooser.createDialog(this,
 				"Select item color", true, colorChooser, ok, null);
 		colorChooserDialog.setVisible(true);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 }
