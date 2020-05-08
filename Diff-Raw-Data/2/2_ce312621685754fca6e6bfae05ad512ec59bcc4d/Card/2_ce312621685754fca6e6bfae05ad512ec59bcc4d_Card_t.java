 /**
  * @file Cardv2.java
  * @author Jia Chen
  * @date May 04, 2012
  * @description 
  * 		Cardv2.java is the test instance of Card.java.
  */
 
 package CardAssociation;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import javax.imageio.ImageIO;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.TransferHandler;
 
 public class Card implements Serializable, MouseListener, MouseMotionListener,
 		Comparable<Object>, Transferable {
 
 	/* * * * * * * * * * * *
 	 * serialized versions *
 	 */
 	// private static final long serialVersionUID = 5876059325645604130L;
 	// private static final long serialVersionUID = 5876059325645604131L;
 	private static final long serialVersionUID = 5876059325645604132L;
 
 	// card properties
 	private String[] sameID;
 	private String id;
 	private String pID;
 	private String cardName;
 	private String cardName_e;
 	private int dupCount = 0;
 	private ArrayList<String> effects;
 	private ArrayList<String> effects_e;
 	private int power;
 	private Trigger trigger;
 	private int level;
 	private int cost;
 	private int soul;
 	private Type t;
 	private CCode c;
 	private String trait1;
 	private String trait2;
 	private String trait1_e;
 	private String trait2_e;
 	private String flavorText;
 	private String flavorText_e;
 	private String realCardName;
 	private ArrayList<Attribute> attributes;
 	private ArrayList<Card> associatedCards;
 	private boolean isAlternateArt;
 	private boolean isEPSign;
 
 	// game play properties
 	private State currentState;
 	private Zone currentZone;
 
 	// other properties
 	private String imageResource;
 	private String backResource;
 	private DataFlavor[] flavors;
 	private int MINILEN = 3;
 
 	private UUID uniqueID;
 
 	@Override
 	public Object getTransferData(DataFlavor flavor) {
 		if (isDataFlavorSupported(flavor)) {
 			return this;
 		}
 		return null;
 	}
 
 	@Override
 	public DataFlavor[] getTransferDataFlavors() {
 		return flavors;
 	}
 
 	@Override
 	public boolean isDataFlavorSupported(DataFlavor flavor) {
 		return flavors[0].equals(flavor);
 	}
 
 	public boolean equals(Object o) {
 		Card card = (Card) o;
 		if (card != null)
 			return card.getID().equals(id);
 		else
 			return false;
 	}
 
 	@Override
 	public int compareTo(Object arg0) {
 		return id.compareTo(((Card) arg0).id);
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// customCanvas.repaint();
 		// customCanvas.setLocation(e.getX(), e.getY());
 		// System.out.println(cardName + " dragged " + customCanvas.getX() +
 		// ", "
 		// + customCanvas.getY());
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		System.out.println("Cardv2.java:clicked " + cardName);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		System.out.println("Cardv2.java:pressed cardName = " + cardName);
 		System.out.println("Cardv2.java:pressed name = " + getCardName());
 		JComponent comp = (JComponent) arg0.getSource();
 		TransferHandler handler = comp.getTransferHandler();
 		handler.exportAsDrag(comp, arg0, TransferHandler.COPY);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 
 	}
 
 	/* * * * * * * * * * * * * * * * * * * *
 	 * Standard Card Information and Image *
 	 */
 
 	public Card(String id, String name) {
 		sameID = new String[MINILEN];
 		for (int i = 0; i < sameID.length; i++) {
 			sameID[i] = "";
 		}
 		setID(id);
 		// setName(id);
 		setCardName(name);
 
 		realCardName = name;
 
 		effects = new ArrayList<String>();
 		effects_e = new ArrayList<String>();
 		flavorText = "";
 		flavorText_e = "";
 		setCurrentState(State.NONE);
 		// imageFile = new File("FieldImages/cardBack-s.jpg");
 		imageResource = "/resources/FieldImages/cardBack-s.jpg";
 		backResource = "/resources/FieldImages/cardBack-s.jpg";
 		setAssociatedCards(new ArrayList<Card>());
 		setAttributes(new ArrayList<Attribute>());
 		// addMouseListener(this);
 	}
 
 	// create a card
 	public Card() {
 		effects = new ArrayList<String>();
 		effects_e = new ArrayList<String>();
 		flavorText = "";
 		flavorText_e = "";
 		setCurrentState(State.NONE);
 		sameID = new String[MINILEN];
 		for (int i = 0; i < sameID.length; i++) {
 			sameID[i] = "";
 		}
 	}
 
 	public boolean setID(String id) {
 		String newId = id.replace(" ", "");
 		pID = newId.charAt(0) + "";
 
 		for (int i = 1; i < newId.length(); i++) {
 			if ((Character.isLetter(newId.charAt(i)) && Character.isDigit(newId
 					.charAt(i - 1)))
 					|| (Character.isSpaceChar(newId.charAt(i)) && Character
 							.isDigit(newId.charAt(i - 1)))) {
 				break;
 			} else {
 				pID += newId.charAt(i);
 			}
 		}
 
 		boolean isDupCard = false;
 
 		for (int i = 0; i < sameID.length; i++) {
 			if (sameID[i] == null || sameID[i].isEmpty()
 					|| sameID[i].equals(pID)) {
 				if (sameID[i].equals(pID)) {
 					isDupCard = false;
 				} else {
 					isDupCard = true;
 				}
 				sameID[i] = pID;
 				break;
 			}
 		}
 
 		// this.id = sameID[0];
 		this.id = pID;
 		this.id = id;
 
 		return isDupCard;
 	}
 
 	public JLabel initiateImage() {
 		JLabel imageLabel = new JLabel();
 		try {
 			Image image = ImageIO.read(getClass().getResourceAsStream(
 					getImageResource()));
 			// Image image = ImageIO.read(new
 			// File("src/FieldImages/cardBack-s.jpg").toURI().toURL());
 			// Image image = ImageIO.read((imageFile.toURI()).toURL());
 			// ImageIcon img = new ImageIcon(image);
 
 			ImageIcon img = new ImageIcon(image.getScaledInstance(
 					(int) (image.getWidth(null) * 0.44),
 					(int) (image.getHeight(null) * 0.44), Image.SCALE_SMOOTH));
 
 			imageLabel.setIcon(img);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return imageLabel;
 	}
 
 	// set the card image
 	public void setImageResource(String resPath) {
 		imageResource = resPath;
 		if (resPath.contains("_holo") || resPath.contains("_alt")) {
 			setAlternateArt(true);
 		}
 		if (resPath.contains("_sign")) {
 			setEPSign(true);
 		}
 		// setName(id);
 	}
 
 	// get the card image
 	public String getImageResource() {
 		// if (isWindows)
 		// return "/" + new File(imageResource).getPath();
 		// else
 		return imageResource;
 	}
 
 	public JPanel getInfoPane(int w, int h) {
 
 		// Font font = new Font("Courier New", Font.BOLD, 12);
 
 		JPanel infoPanel = new JPanel();
 		infoPanel.setPreferredSize(new Dimension(w, h));
 
 		GroupLayout layout = new GroupLayout(infoPanel);
 		infoPanel.setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 
 		JTextArea description = new JTextArea(10, 10);
 		if (c == CCode.RED)
 			description.setBackground(Color.PINK);
 		else if (c == CCode.BLUE)
 			description.setBackground(Color.CYAN);
 		else if (c == CCode.YELLOW)
 			description.setBackground(Color.YELLOW);
 		else if (c == CCode.GREEN)
 			description.setBackground(Color.GREEN);
 		// description.setFont(font);
 		description.setLineWrap(true);
 		description.setWrapStyleWord(true);
 		description.setEditable(false);
 
 		String cardText = "";
 		if (t == Type.CHARACTER) {
 			if (!getTrait1_j().equals(""))
 				cardText += getTrait1();
 			if (!getTrait2_j().equals(""))
 				cardText += (!cardText.equals("") ? " | " : "") + getTrait2();
 			cardText += "\n\n";
 		}
 		cardText += getEffects() + "\n";
 
 		if (!getFlavorText().equals("")) {
 			cardText += "Flavor Text: \n" + getFlavorText();
 		}
 
 		description.setText(cardText);
 
 		description.setCaretPosition(0);
 		JScrollPane descContainer = new JScrollPane(description);
 
 		JTextField nameLabel = new JTextField(cardName);
 		nameLabel.setEditable(false);
 		// nameLabel.setFont(font);
 
 		JTextField idLabel = new JTextField(id.replace("_alt", "").replace("_sign", ""));
 		idLabel.setEditable(false);
 		// idLabel.setFont(font);
 		
 		JTextField typeLabel = new JTextField(t.toString());
 		typeLabel.setEditable(false);
 		// typeLabel.setFont(font);
 		
 		JTextField levelLabel = new JTextField("Level: "
 				+ (level >= 0 ? level : " -"));
 		levelLabel.setEditable(false);
 		// levelLabel.setFont(font);
 		
 		JTextField costLabel = new JTextField("Cost: "
 				+ (cost >= 0 ? cost : " -"));
 		costLabel.setEditable(false);
 		// costLabel.setFont(font);
 		
 		JTextField soulLabel = new JTextField("Trigger: " + trigger.toString());
 		soulLabel.setEditable(false);
 		// soulLabel.setFont(font);
 		
 		JTextField powerLabel = new JTextField("Power: "
 				+ (power > 0 ? power : " -"));
 		powerLabel.setEditable(false);
 		// powerLabel.setFont(font);
 		
 		JTextField damageLabel = new JTextField("Soul: "
 				+ (soul > 0 ? soul : " -"));
 		damageLabel.setEditable(false);
 		// damageLabel.setFont(font);
 		
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 
 		layout.setHorizontalGroup(layout
 				.createParallelGroup()
 				.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 350,
 						GroupLayout.PREFERRED_SIZE)
 				.addGroup(
 						layout.createSequentialGroup()
 								.addGroup(
 										layout.createParallelGroup()
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		idLabel,
 																		GroupLayout.PREFERRED_SIZE,
 																		125,
 																		GroupLayout.PREFERRED_SIZE))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		levelLabel,
 																		GroupLayout.PREFERRED_SIZE,
 																		60,
 																		GroupLayout.PREFERRED_SIZE)
 																.addComponent(
 																		costLabel)))
 								.addGroup(
 										layout.createParallelGroup()
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		typeLabel))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		powerLabel,
 																		GroupLayout.PREFERRED_SIZE,
 																		90,
 																		GroupLayout.PREFERRED_SIZE)))
 								.addGroup(
 										layout.createParallelGroup()
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		soulLabel,
 																		GroupLayout.PREFERRED_SIZE,
																		125,
 																		GroupLayout.PREFERRED_SIZE))
 												.addGroup(
 														layout.createSequentialGroup()
 																.addComponent(
 																		damageLabel))))
 				.addComponent(descContainer, GroupLayout.PREFERRED_SIZE, 350,
 						GroupLayout.PREFERRED_SIZE));
 
 		layout.setVerticalGroup(layout
 				.createSequentialGroup()
 				.addGroup(layout.createParallelGroup().addComponent(nameLabel))
 				.addGroup(
 						layout.createParallelGroup().addComponent(idLabel)
 								.addComponent(typeLabel)
 								.addComponent(soulLabel))
 				.addGroup(
 						layout.createParallelGroup().addComponent(levelLabel)
 								.addComponent(costLabel)
 								.addComponent(powerLabel)
 								.addComponent(damageLabel))
 				.addGroup(
 						layout.createParallelGroup()
 								.addComponent(descContainer)));
 
 		// System.out.println("getInfoPane");
 
 		return infoPanel;
 
 	}
 
 	public JPanel displayImage(int w, int h) {
 
 		JPanel imagePane = new JPanel();
 		imagePane.setPreferredSize(new Dimension(w, h));
 
 		try {
 			// Image image = ImageIO.read((imageFile.toURI()).toURL());
 			// System.out.println(getImageResource());
 			Image image = ImageIO.read(getClass().getResourceAsStream(
 					getImageResource()));
 			ImageIcon img = new ImageIcon(image);
 			imagePane.add(new JLabel(img));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return imagePane;
 	}
 
 	// used in Deck.java to check how many copies of the card is there
 	public int getCardCount() {
 		return dupCount;
 	}
 
 	public String getID() {
 		return id;
 	}
 
 	// used in Deck.java to increment the number of copies of the card
 	public void addCount() {
 		dupCount++;
 	}
 
 	public void setCount(int dupCount) {
 		this.dupCount = dupCount;
 	}
 
 	// set the card name of the card
 	public void setCardName(String name) {
 		this.cardName = name;
 		setUniqueID(UUID.randomUUID());
 	}
 
 	// get the card name of the card
 	public String getCardName() {
 		return cardName;
 	}
 
 	public void setCardName_e(String cardName_e) {
 		this.cardName_e = cardName_e;
 	}
 
 	public String getCardName_e() {
 		return cardName_e;
 	}
 
 	// get the card effects
 	public String getEffects() {
 		String result = getEffects_j();
 		String effectsStr_e = getEffects_e();
 		if (!effectsStr_e.isEmpty()) {
 			result += "\n" + effectsStr_e;
 		}
 		return result;
 	}
 
 	public String getEffects_j() {
 		String result = "";
 
 		for (int i = 0; i < effects.size(); i++) {
 			result += effects.get(i) + "\n";
 		}
 
 		return result;
 	}
 
 	public String getEffects_e() {
 		String result = "";
 
 		for (int i = 0; i < effects_e.size(); i++) {
 			result += effects_e.get(i) + "\n";
 		}
 
 		return result;
 	}
 
 	// set the card effects
 	public void addEffect(String e) {
 		if (!e.isEmpty())
 			effects.add(e);
 		// TODO: process effects to make attributes
 	}
 
 	public void addEffect_e(String e) {
 		if (!e.isEmpty())
 			effects_e.add(e);
 		// TODO: process effects to make attributes
 	}
 
 	// set the power value of the card
 	public void setPower(int power) {
 		this.power = power;
 	}
 
 	// get the power value of the card
 	public int getPower() {
 		return power;
 	}
 
 	// set the soul count of the card
 	public void setSoul(int soul) {
 		this.soul = soul;
 	}
 
 	// get the soul count of the card
 	public int getSoul() {
 		return soul;
 	}
 
 	// set the color of the card
 	public void setC(CCode c) {
 		this.c = c;
 	}
 
 	// get the color of the card
 	public CCode getC() {
 		return c;
 	}
 
 	// set the first trait of the card
 	public void setTrait1(String trait1) {
 		this.trait1 = trait1;
 	}
 
 	public String getTrait1() {
 		return getTrait1_j() + " " + getTrait1_e();
 	}
 
 	// get the first trait of the card
 	public String getTrait1_j() {
 		return trait1;
 	}
 
 	// set the second trait of the card
 	public void setTrait2(String trait2) {
 		this.trait2 = trait2;
 	}
 
 	public String getTrait2() {
 		return getTrait2_j() + " " + getTrait2_e();
 	}
 
 	// get the second trait of the card
 	public String getTrait2_j() {
 		return trait2;
 	}
 
 	public void setTrait1_e(String trait1_e) {
 		this.trait1_e = trait1_e;
 	}
 
 	public String getTrait1_e() {
 		return trait1_e;
 	}
 
 	public void setTrait2_e(String trait2_e) {
 		this.trait2_e = trait2_e;
 	}
 
 	public String getTrait2_e() {
 		return trait2_e;
 	}
 
 	// set the level of the card
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	// get the level of the card
 	public int getLevel() {
 		return level;
 	}
 
 	// set the cost of the card
 	public void setCost(int cost) {
 		this.cost = cost;
 	}
 
 	// get the cost of the card
 	public int getCost() {
 		return cost;
 	}
 
 	// set the trigger information of the card
 	public void setTrigger(Trigger trigger) {
 		this.trigger = trigger;
 		if (this.trigger == null)
 			this.trigger = Trigger.NONE;
 	}
 
 	// get the trigger information of the card
 	public Trigger getTrigger() {
 		return trigger;
 	}
 
 	// get the card type
 	public void setT(Type t) {
 		this.t = t;
 	}
 
 	// set the card type
 	public Type getT() {
 		return t;
 	}
 
 	public void resetCount() {
 		dupCount = 0;
 	}
 
 	// used in Deck.java to decrement the number of copies of the card
 	public void removeCount() {
 		dupCount--;
 	}
 
 	/**
 	 * Check to see if the card meets the requirements given
 	 * 
 	 * @param sId
 	 * @param sName
 	 * @param sColor
 	 * @param sType
 	 * @param sLevel
 	 * @param sCost
 	 * @param sTrigger
 	 * @param sPower
 	 * @param sSoul
 	 * @param sTrait
 	 * @param sAbility
 	 * @return
 	 */
 	public boolean meetsRequirement(String sId, String sName, CCode sColor,
 			Type sType, int sLevel, int sCost, Trigger sTrigger, int sPower,
 			int sSoul, String sTrait, String sAbility) {
 
 		boolean isMet = true;
 
 		if (!id.isEmpty()) {
 
 			String[] parts = sId.split(" ");
 
 			for (int i = 0; i < sameID.length; i++) {
 				isMet = true;
 				for (int j = 0; j < parts.length; j++) {
 					isMet = isMet
 							&& sameID[i].toLowerCase().contains(
 									parts[j].toLowerCase());
 					/*
 					 * if (sameID[i].toLowerCase()
 					 * .contains(parts[j].toLowerCase()))
 					 * System.out.println(sameID[i] + "???" + parts[j]);
 					 */
 				}
 				if (isMet) {
 					break;
 				}
 
 			}
 			isMet = true;
 			for (int j = 0; j < parts.length; j++) {
 				isMet = isMet
 						&& id.toLowerCase().contains(parts[j].toLowerCase());
 				/*
 				 * if (id.toLowerCase().contains(parts[j].toLowerCase()))
 				 * System.out.println(id + "::CONTAINS::" + parts[j]);
 				 */
 			}
 
 			/*
 			 * if (isMet) { for (int i = 0; i < sameID.length; i++) {
 			 * System.out.print("[(" + i + ")" + sameID[i] + "]"); }
 			 * System.out.println(); }
 			 */
 		}
 
 		if (!sName.isEmpty()) {
 			isMet = isMet
 					&& (cardName.toLowerCase().contains(sName.toLowerCase()) || cardName_e
 							.toLowerCase().contains(sName.toLowerCase()));
 		}
 
 		if (sColor != null && sColor != CCode.ALL) {
 			isMet = isMet && (sColor == c);
 		}
 
 		if (sType != null && sType != CardAssociation.Type.ALL) {
 			isMet = isMet && (sType == t);
 		}
 
 		if (sLevel > -1) {
 			isMet = isMet && (sLevel == level);
 		}
 
 		if (sCost > -1) {
 			isMet = isMet && (sCost == cost);
 		}
 
 		if (sTrigger != null && sTrigger != Trigger.ALL) {
 			isMet = isMet && (sTrigger == trigger);
 		}
 
 		if (sPower > -1) {
 			isMet = isMet && (sPower == power);
 		}
 
 		if (sSoul > -1) {
 			isMet = isMet && (sSoul == soul);
 		}
 
 		if (!sTrait.isEmpty()) {
 			isMet = isMet
 					&& (trait1.toLowerCase().contains(sTrait)
 							|| trait2.toLowerCase().contains(sTrait)
 							|| trait1_e.toLowerCase().contains(sTrait) || trait2_e
 							.toLowerCase().contains(sTrait));
 		}
 
 		if (!sAbility.isEmpty()) {
 
 			String[] parts = sAbility.split(" ");
 
 			for (int i = 0; i < parts.length; i++) {
 				isMet = isMet
 						&& (getEffects().toLowerCase().contains(
 								parts[i].toLowerCase()) || getEffects_e()
 								.toLowerCase().contains(parts[i].toLowerCase()));
 			}
 		}
 
 		return isMet;
 	}
 
 	public void setFlavorText(String flavorText) {
 		this.flavorText = flavorText;
 	}
 
 	public String getFlavorText() {
 		return getFlavorText_j() + " " + getFlavorText_e();
 	}
 
 	public String getFlavorText_j() {
 		return flavorText;
 	}
 
 	public void setFlavorText_e(String flavorText_e) {
 		this.flavorText_e = flavorText_e;
 	}
 
 	public String getFlavorText_e() {
 		return flavorText_e;
 	}
 
 	public void setRealName(String name) {
 		realCardName = name;
 	}
 
 	public String getRealName() {
 		return realCardName;
 	}
 
 	public Card clone() {
 		Card cloned = new Card(id, cardName);
 
 		cloned.setCount(dupCount);
 		cloned.setEffects(effects);
 		cloned.setEffects_e(effects_e);
 		cloned.setPower(power);
 		cloned.setTrigger(trigger);
 		cloned.setLevel(level);
 		cloned.setCost(cost);
 		cloned.setSoul(soul);
 		cloned.setT(t);
 		cloned.setC(c);
 		cloned.setTrait1(trait1);
 		cloned.setTrait1_e(trait1_e);
 		cloned.setTrait2(trait2);
 		cloned.setTrait2_e(trait2_e);
 		cloned.setFlavorText(flavorText);
 		cloned.setFlavorText_e(flavorText_e);
 		cloned.setImageResource(imageResource);
 
 		return cloned;
 	}
 
 	private void setEffects(ArrayList<String> effects) {
 		this.effects = effects;
 	}
 
 	private void setEffects_e(ArrayList<String> effects_e) {
 		this.effects_e = effects_e;
 	}
 
 	/* * * * * * * * * * * * * * * * * * * * * *
 	 * Game Play Property Setting and Creation *
 	 */
 
 	// private Rectangle cardBound;
 	Canvas customCanvas = null;
 
 	public Zone getCurrentZone() {
 		return currentZone;
 	}
 
 	public void setCurrentZone(Zone currentZone) {
 		this.currentZone = currentZone;
 	}
 
 	// used in Game.java to set the current state of the card
 	public void setCurrentState(State currentState) {
 		this.currentState = currentState;
 	}
 
 	// used in Game.java to get the current state of the card
 	public State getCurrentState() {
 		return currentState;
 	}
 
 	public Rectangle getCardBound() {
 		Rectangle boundBox = new Rectangle();
 		boundBox.setBounds((int) customCanvas.getLocation().x,
 				(int) (customCanvas.getLocation().y + Game.Game.translatedY),
 				customCanvas.getWidth(), customCanvas.getHeight());
 
 		return boundBox;
 	}
 
 	public Image getCardImage() throws MalformedURLException, IOException {
 		return ImageIO.read(getClass().getResourceAsStream(getImageResource()));
 	}
 
 	public Canvas toCanvas() {
 		if (customCanvas == null) {
 			customCanvas = new Canvas() {
 				private static final long serialVersionUID = 932367309486409810L;
 
 				public void paint(Graphics g) {
 					Image after;
 					try {
 
 						// img = ImageIO.read((imageFile.toURI()).toURL());
 
 						BufferedImage before = ImageIO.read(getClass()
 								.getResourceAsStream(getImageResource()));
 
 						if (currentState == State.FD_REST
 								|| currentState == State.FD_STAND) {
 							before = ImageIO.read(getClass()
 									.getResourceAsStream(backResource));
 						}
 						/*
 						 * BufferedImage before =
 						 * ImageIO.read((imageFile.toURI()) .toURL());
 						 */
 						int wid = before.getWidth();
 						int hit = before.getHeight();
 						after = new BufferedImage(wid, hit,
 								BufferedImage.TYPE_INT_ARGB);
 
 						AffineTransform at = new AffineTransform();
 
 						at.scale(Game.Game.gameScale, Game.Game.gameScale);
 
 						if (currentState == State.REST
 								|| currentState == State.FD_REST) {
 							at.translate((after.getHeight(null) - before
 									.getWidth(null)) / 2,
 									(after.getWidth(null) - before
 											.getHeight(null)) / 2);
 							if (getT() == Type.CLIMAX
 									&& currentState == State.REST) {
 								at.rotate(Math.toRadians(-90),
 										before.getWidth(null) / 2,
 										before.getHeight(null) / 2);
 							} else {
 								at.rotate(Math.toRadians(90),
 										before.getWidth(null) / 2,
 										before.getHeight(null) / 2);
 							}
 						} else if (currentState == State.REVERSE) {
 							at.rotate(Math.toRadians(180),
 									before.getWidth(null) / 2,
 									before.getHeight(null) / 2);
 						}
 
 						AffineTransformOp scaleOp = new AffineTransformOp(at,
 								AffineTransformOp.TYPE_BILINEAR);
 						after = scaleOp.filter(before, null);
 
 						// prepareImage(img, null);
 						g.drawImage(after, getLocation().x, getLocation().y,
 								null);
 						customCanvas.setBounds(getLocation().x,
 								getLocation().y, (int) (after.getWidth(null)),
 								(int) (after.getHeight(null)));
 
 						boolean debug = false;
 
 						String outputString = (int) getCardBound().getX()
 								+ " + " + getCardBound().width + ","
 								+ (int) getCardBound().getY() + " + "
 								+ getCardBound().height;
 						if (debug) {
 							g.setColor(Color.BLUE);
 							g.fillRect((int) getCardBound().getX(),
 									(int) getCardBound().getY(),
 									getCardBound().width, getCardBound().height);
 							g.setColor(Color.RED);
 							g.drawRect((int) getCardBound().getX(),
 									(int) getCardBound().getY(),
 									getCardBound().width, getCardBound().height);
 							g.setColor(Color.BLACK);
 
 							g.drawString(outputString, (int) getCardBound()
 									.getX(), (int) getCardBound().getY());
 						}
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 				}
 
 				public void update(Graphics g) {
 					paint(g);
 				}
 
 			};
 		}
 
 		return customCanvas;
 	}
 
 	public void setDisplay(boolean isFaceUp, boolean isTapped) {
 
 		// if(isFaceUp && isTapped)
 		// currentState = State.REST;
 		// else if(isFaceUp && !isTapped)
 		// currentState = State.STAND;
 		// else if(!isFaceUp && isTapped)
 		// currentState = State.FD_REST;
 		// else
 		// currentState = State.FD_STAND;
 
 	}
 
 	// Hard code special cases where you may put >4 cards in the deck
 	// FZ/SE13-24 C
 	// FZ/SE13-26 C
 	// MF/S13-034 U
 	// MF/S13-040 C
 	// ID/W10-014 C
 	// SG/W19-038 C
 	// FT/SE10-29
 
 	public static int getMaxInDeck(Card c) {
 		if (c.getID().equals("FZ/SE13-24 C")
 				|| c.getID().equals("FZ/SE13-26 C")
 				|| c.getID().equals("MF/S13-034 U")
 				|| c.getID().equals("MF/S13-040 C")
 				|| c.getID().equals("ID/W10-014 C")
 				|| c.getID().equals("SG/W19-038 C"))
 			return 50;
 		else if (c.getID().contains("FT/SE10-29"))
 			return 6;
 		else
 			return 4;
 	}
 
 	public String toString() {
 		return cardName;
 	}
 
 	public void setAlternateArt(boolean isAlternateArt) {
 		this.isAlternateArt = isAlternateArt;
 	}
 
 	public boolean isAlternateArt() {
 		return isAlternateArt;
 	}
 
 	public boolean isEPSign() {
 		return isEPSign;
 	}
 
 	public void setEPSign(boolean isEPSign) {
 		this.isEPSign = isEPSign;
 	}
 
 	public ArrayList<Attribute> getAttributes() {
 		return attributes;
 	}
 
 	public void setAttributes(ArrayList<Attribute> attributes) {
 		this.attributes = attributes;
 	}
 
 	public ArrayList<Card> getAssociatedCards() {
 		return associatedCards;
 	}
 
 	public void setAssociatedCards(ArrayList<Card> associatedCards) {
 		this.associatedCards = associatedCards;
 	}
 
 	public UUID getUniqueID() {
 		return uniqueID;
 	}
 
 	public void setUniqueID(UUID uniqueID) {
 		this.uniqueID = uniqueID;
 	}
 
 }
