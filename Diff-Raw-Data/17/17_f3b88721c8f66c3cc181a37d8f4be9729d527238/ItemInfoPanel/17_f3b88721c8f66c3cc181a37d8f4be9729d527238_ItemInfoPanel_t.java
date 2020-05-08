 package yuuki.ui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 
 import yuuki.graphic.ImageComponent;
 import yuuki.graphic.ImageFactory;
 import yuuki.item.Item;
 import yuuki.item.UsableItem;
 import yuuki.util.InvalidIndexException;
 
 /**
  * Shows information on an item.
  */
 @SuppressWarnings("serial")
 public class ItemInfoPanel extends JPanel {
 	
 	/**
 	 * A listener for events fired from this class.
 	 */
 	public static interface InfoPanelListener {
 		public void dropItemClicked(Item item);
 		public void useItemClicked(Item item);
 	}
 	
 	/**
 	 * The size of the description font.
 	 */
 	private static final int DESC_FONT_SIZE = 10;
 	
 	/**
 	 * The size of an image in the info panel.
 	 */
 	private static final int IMAGE_SIZE = 100;
 	
 	/**
 	 * The size of the usability and value fonts.
 	 */
 	private static final int INFO_FONT_SIZE = 10;
 	
 	/**
 	 * The size of the title font.
 	 */
 	private static final int TITLE_FONT_SIZE = 12;
 	
 	/**
 	 * The description of the item.
 	 */
 	private JTextArea description;
 	
 	/**
 	 * Holds the description as its client.
 	 */
 	private JScrollPane descScroll;
 	
 	/**
 	 * Drops the item when clicked.
 	 */
 	private JButton dropButton;
 	
 	/**
 	 * Whether the drop button is free to be enabled/disabled. This is false
 	 * when setDropButtonEnabled is used to force the drop button to be
 	 * disabled.
 	 */
 	private boolean dropButtonEnabledIsFree = true;
 	
 	/**
 	 * The large picture of the currently selected item.
 	 */
 	private ImageComponent image;
 	
 	/**
 	 * Generates image data from indexes.
 	 */
 	private ImageFactory imageFactory = null;
 	
 	/**
 	 * The listener for events.
 	 */
 	private InfoPanelListener listener;
 	
 	/**
 	 * The item being shown.
 	 */
 	private Item shownItem = null;
 	
 	/**
 	 * The title of the currently selected item.
 	 */
 	private JLabel title;
 	
 	/**
 	 * The line that explains the use of the item.
 	 */
 	private JLabel usability;
 	
 	/**
 	 * Uses the item when clicked.
 	 */
 	private JButton useButton;
 	
 	/**
 	 * Shows the number of uses remaining.
 	 */
 	private JLabel uses;
 	
 	/**
 	 * The line that tells the value of the item.
 	 */
 	private JLabel value;
 	
 	/**
 	 * Creates a new InfoPanel.
 	 * 
 	 * @param width The width of the info panel.
 	 * @param height The height of the info panel.
 	 */
 	public ItemInfoPanel(int width, int height) {
 		setLayout(null);
 		createComponents();
 		addComponents();
 		showInfo(null);
 	}
 	
 	/**
 	 * Sets the enabled status of the Drop button. By default, the enabled
 	 * status is 'true'.
 	 * 
 	 * @param b What to set the enabled status to.
 	 */
 	public void setDropButtonEnabled(boolean b) {
 		dropButtonEnabledIsFree = b;
 		dropButton.setEnabled(b);
 	}
 	
 	/**
 	 * Sets the image factory.
 	 * 
 	 * @param factory The image factory to use.
 	 */
 	public void setImageFactory(ImageFactory factory) {
 		imageFactory = factory;
 	};
 	
 	/**
 	 * Sets the listener for this panel.
 	 * 
 	 * @param l The listener.
 	 */
 	public void setListener(InfoPanelListener l) {
 		listener = l;
 	}
 	
 	/**
 	 * Sets the info panel to show information on an item.
 	 * 
 	 * @param item The item to show information on.
 	 */
 	public void showInfo(Item item) {
 		shownItem = item;
 		setInfo();
 	}
 	
 	/**
 	 * Adds child components to this panel.
 	 */
 	private void addComponents() {
 		add(image);
 		add(title);
 		add(usability);
 		add(value);
 		add(uses);
 		add(descScroll);
 		add(dropButton);
 		add(useButton);
 	}
 	
 	/**
 	 * Creates all child components.
 	 */
 	private void createComponents() {
 		createImage();
 		createTitle();
 		createUsability();
 		createValue();
 		createUses();
 		createDescription();
 		createDropButton();
 		createUseButton();
 	}
 	
 	/**
 	 * Creates the description component.
 	 */
 	private void createDescription() {
 		description = new JTextArea(4, 14);
 		description.setFont(new Font(Font.SERIF, Font.PLAIN,
 				DESC_FONT_SIZE));
 		description.setEditable(false);
 		description.setHighlighter(null);
 		description.setLineWrap(true);
 		description.setWrapStyleWord(true);
 		descScroll = new JScrollPane(description);
 		int policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
 		descScroll.setVerticalScrollBarPolicy(policy);
 	}
 	
 	/**
 	 * Creates the drop button component.
 	 */
 	private void createDropButton() {
 		dropButton = new JButton("Drop");
 		dropButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				listener.dropItemClicked(shownItem);
 			}
 		});
 	}
 	
 	/**
 	 * Creates the image component.
 	 */
 	private void createImage() {
 		Dimension d = new Dimension(IMAGE_SIZE, IMAGE_SIZE);
 		image = new ImageComponent();
 		image.setPreferredSize(d);
 		image.setMinimumSize(d);
 	}
 	
 	/**
 	 * Creates the title component.
 	 */
 	private void createTitle() {
 		title = new JLabel();
 		title.setFont(new Font(Font.SERIF, Font.PLAIN, TITLE_FONT_SIZE));
 		title.setAlignmentX(Component.LEFT_ALIGNMENT);
 	}
 	
 	/**
 	 * Creates the usability component.
 	 */
 	private void createUsability() {
 		usability = new JLabel();
 		usability.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,
 				INFO_FONT_SIZE));
 		usability.setAlignmentX(Component.LEFT_ALIGNMENT);
 	}
 	
 	/**
 	 * Creates the use button component.
 	 */
 	private void createUseButton() {
 		useButton = new JButton("Use");
 		useButton.setEnabled(false);
 		useButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				listener.useItemClicked(shownItem);
 			}
 		});
 	}
 	
 	/**
 	 * Creates the uses components.
 	 */
 	private void createUses() {
 		uses = new JLabel();
 		uses.setFont(new Font(Font.SANS_SERIF, Font.ITALIC,
 				INFO_FONT_SIZE));
 		uses.setAlignmentX(Component.RIGHT_ALIGNMENT);
 	}
 	
 	/**
 	 * Creates the value component.
 	 */
 	private void createValue() {
 		value = new JLabel();
 		value.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,
 				INFO_FONT_SIZE));
 		value.setAlignmentX(Component.LEFT_ALIGNMENT);
 	}
 	
 	/**
 	 * Lays out the components in this panel.
 	 */
 	private void layoutComponents() {
 		layoutImage();
 		layoutTitle();
 		layoutUsability();
 		layoutValue();
 		layoutDescription();
 		layoutDropButton();
 		layoutUseButton();
 		layoutUses();
 		layoutSelf();
 		repaint();
 	}
 	
 	/**
 	 * Lays out the description component.
 	 */
 	private void layoutDescription() {
 		Rectangle box = value.getBounds();
 		Dimension d = descScroll.getPreferredSize();
 		descScroll.setBounds(0, box.y + box.height + 1, d.width + 2, d.height);
 	}
 	
 	/**
 	 * Lays out the drop button.
 	 */
 	private void layoutDropButton() {
 		Rectangle box = descScroll.getBounds();
 		Dimension d = dropButton.getPreferredSize();
 		dropButton.setBounds(0, box.y + box.height + 1, d.width, d.height);
 	}
 	
 	/**
 	 * Lays out the image component.
 	 */
 	private void layoutImage() {
 		image.setBounds(0, 0, IMAGE_SIZE, IMAGE_SIZE);
 	}
 	
 	/**
 	 * Sets the bounds of this component.
 	 */
 	private void layoutSelf() {
 		Rectangle box = dropButton.getBounds();
 		Dimension size = new Dimension(box.y + box.height, box.x + box.width);
 		setPreferredSize(size);
 		setMinimumSize(size);
 	}
 	
 	/**
 	 * Lays out the title component.
 	 */
 	private void layoutTitle() {
 		Rectangle box = image.getBounds();
 		Dimension d = title.getPreferredSize();
 		title.setBounds(0, box.y + box.height + 1, d.width + 2, d.height);
 	}
 	
 	/**
 	 * Lays out the usability component.
 	 */
 	private void layoutUsability() {
 		Rectangle box = title.getBounds();
 		Dimension d = usability.getPreferredSize();
 		usability.setBounds(0, box.y + box.height + 1, d.width + 2, d.height);
 	}
 	
 	/**
 	 * Lays out the use button.
 	 */
 	private void layoutUseButton() {
 		Rectangle box = dropButton.getBounds();
 		Dimension d = useButton.getPreferredSize();
 		useButton.setBounds(box.width + 1, box.y, d.width, d.height);
 	}
 	
 	/**
 	 * Lays out the uses component.
 	 */
 	private void layoutUses() {
 		Rectangle box = value.getBounds();
 		Rectangle b2 = useButton.getBounds();
 		Dimension d = uses.getPreferredSize();
 		int x = b2.x + b2.width - (d.width + 2);
 		uses.setBounds(x, box.y, d.width + 2, d.height);
 	}
 	
 	/**
 	 * Lays out the value component.
 	 */
 	private void layoutValue() {
 		Rectangle box = usability.getBounds();
 		Dimension d = value.getPreferredSize();
 		value.setBounds(0, box.y + box.height + 1, d.width + 2, d.height);
 	}
 	
 	/**
 	 * Enables buttons based on the current item.
 	 */
 	private void setButtons() {
 		dropButton.setEnabled(dropButtonEnabledIsFree && shownItem != null);
 		useButton.setEnabled(shownItem != null && shownItem.isExternal());
 	}
 	
 	/**
 	 * Sets the description based on the current item.
 	 */
 	private void setDescription() {
 		String text = "No item selected";
 		Font f = description.getFont().deriveFont(Font.ITALIC);
 		if (shownItem != null) {
 			text = shownItem.getDescription();
 			f = f.deriveFont(Font.PLAIN);
 		}
 		description.setFont(f);
 		description.setText(text);
 		description.setCaretPosition(0);
 	}
 	
 	/**
 	 * Sets the image from the current item.
 	 */
 	private void setImage() {
 		Image img = null;
 		if (shownItem != null) {
 			try {
 				img = imageFactory.createImage(shownItem.getImage());
 			} catch (InvalidIndexException e) {
 				e.printStackTrace();
 			}
 		}
 		image.setBackgroundImage(img);
 	}
 	
 	/**
 	 * Sets the info panel for all information based on the current item.
 	 */
 	private void setInfo() {
 		setImage();
 		setTitle();
 		setValueLine();
 		setUsability();
 		setDescription();
 		setButtons();
 		layoutComponents();
 	}
 	
 	/**
 	 * Sets the title from the current item.
 	 */
 	private void setTitle() {
 		String titleText = "No item selected";
 		Font f = title.getFont().deriveFont(Font.ITALIC);
 		if (shownItem != null) {
 			titleText = shownItem.getName();
 			f = f.deriveFont(Font.PLAIN);
 		}
 		title.setFont(f);
 		title.setText(titleText);
 	}
 	
 	/**
 	 * Sets the usability line from the current item.
 	 */
 	private void setUsability() {
 		String usabilityText = "No item selected";
 		Font f = usability.getFont().deriveFont(Font.ITALIC);
 		if (shownItem != null) {
 			f = f.deriveFont(Font.PLAIN);
 			if (shownItem.isUsable()) {
 				usabilityText = "May be used";
 				if (!shownItem.isExternal()) {
 					usabilityText += " in battle only";
 				}
 			} else {
 				usabilityText = "Not usable";
 			}
 		}
 		usability.setFont(f);
 		usability.setText(usabilityText);
 	}
 	
 	/**
 	 * Sets the value and uses line from the current item.
 	 */
 	private void setValueLine() {
 		String valueText = "No item selected";
 		String usesText = "";
 		Font f = value.getFont().deriveFont(Font.ITALIC);
 		if (shownItem != null) {
 			f = f.deriveFont(Font.PLAIN);
 			if (shownItem.isUsable()) {
 				UsableItem item = ((UsableItem) shownItem);
				int remainingUses = item.getMaxUses() - item.getUses();
				usesText = remainingUses + " use%s left";
				usesText = String.format(usesText, ((remainingUses != 1) ? "s" : ""));
 			}
 			valueText = shownItem.getValue() + "cp";
 		}
 		value.setFont(f);
 		value.setText(valueText);
 		uses.setText(usesText);
 	}
 	
 }
