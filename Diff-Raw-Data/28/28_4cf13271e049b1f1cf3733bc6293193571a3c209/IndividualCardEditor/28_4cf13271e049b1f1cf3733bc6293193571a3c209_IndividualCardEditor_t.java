 package com.github.assisstion.MTGCardPortfolio;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.border.EmptyBorder;
 
 public class IndividualCardEditor extends JFrame{
 	
 	private static final long serialVersionUID = 2465673811478519895L;
 	
 	private JPanel contentPane;
 	private JTextField nameField;
 	private JTextField manaCostField;
 	private JTextField convertedManaCostField;
 	private JTextField powerField;
 	private JTextField toughnessField;
 	private JTextField artistField;
 	private JTextField imageURLField;
 	private CardData old;
 	private CardData current;
 	private PortfolioFrame over;
 	private SelectionPanel color;
 	private SelectionPanel rarity;
 	private SelectionPanel expansion;
 	private SelectionPanel superType;
 	private SelectionPanel type;
 	private SelectionPanel subType;
 	private JTextField expansionShortField;
 	private JTextField loyaltyField;
 	private JTextPane textField;
 	
 	public IndividualCardEditor(PortfolioFrame instance, CardData data){
 		over = instance;
 		old = data.clone();
 		current = data;
 		setResizable(false);
 		setTitle("Edit Card - " + current.name);
 		setBounds(150, 150, 900, 450);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		getContentPane().setLayout(null);
 		
 		JLabel lblName = new JLabel("Name:");
 		lblName.setBounds(29, 12, 47, 16);
 		contentPane.add(lblName);
 		
 		nameField = new JTextField();
 		nameField.setBounds(79, 6, 209, 28);
 		contentPane.add(nameField);
 		nameField.setColumns(10);
 		
 		JLabel lblManaCost = new JLabel("Mana Cost:");
 		lblManaCost.setBounds(6, 50, 70, 16);
 		contentPane.add(lblManaCost);
 		
 		manaCostField = new JTextField();
 		manaCostField.setBounds(79, 44, 111, 28);
 		contentPane.add(manaCostField);
 		manaCostField.setColumns(10);
 		
 		color = new SelectionPanel("Color");
 		color.setBounds(300, 0, 200, 200);
 		color.comboBox.addItem("White");
 		color.comboBox.addItem("Blue");
 		color.comboBox.addItem("Black");
 		color.comboBox.addItem("Red");
 		color.comboBox.addItem("Green");
 		getContentPane().add(color);
 		
 		superType = new SelectionPanel("Supertype");
 		superType.setBounds(300, 200, 200, 200);
 		superType.comboBox.addItem("Basic");
 		superType.comboBox.addItem("Legendary");
 		superType.comboBox.addItem("Snow");
 		superType.comboBox.addItem("World");
 		getContentPane().add(superType);
 		
 		type = new SelectionPanel("Type");
 		type.setBounds(500, 200, 200, 200);
 		type.comboBox.addItem("Creature");
 		type.comboBox.addItem("Artifact");
 		type.comboBox.addItem("Enchantment");
 		type.comboBox.addItem("Land");
 		type.comboBox.addItem("Instant");
 		type.comboBox.addItem("Sorcery");
 		type.comboBox.addItem("Tribal");
 		type.comboBox.addItem("Planeswalker");
 		getContentPane().add(type);
 		
 		subType = new SelectionPanel("Subtype");
 		subType.setBounds(700, 200, 200, 200);
 		getContentPane().add(subType);
 		
 		expansion = new SelectionPanel("Expansion");
 		expansion.setBounds(700, 0, 200, 200);
 		getContentPane().add(expansion);
 		
 		rarity = new SelectionPanel("Rarity");
 		rarity.setBounds(500, 0, 200, 200);
 		rarity.comboBox.addItem("Common");
 		rarity.comboBox.addItem("Uncommon");
 		rarity.comboBox.addItem("Rare");
 		rarity.comboBox.addItem("Mythic Rare");
 		getContentPane().add(rarity);
 		
 		JLabel lblImageUrl = new JLabel("Image URL:\n");
 		lblImageUrl.setBounds(6, 146, 79, 16);
 		contentPane.add(lblImageUrl);
 		
 		JLabel lblCmc = new JLabel("CMC:");
 		lblCmc.setBounds(202, 50, 33, 16);
 		contentPane.add(lblCmc);
 		
 		convertedManaCostField = new JTextField();
 		convertedManaCostField.setBounds(236, 46, 47, 28);
 		contentPane.add(convertedManaCostField);
 		convertedManaCostField.setColumns(10);
 		
 		JLabel lblPower = new JLabel("Power:");
 		lblPower.setBounds(29, 78, 47, 16);
 		contentPane.add(lblPower);
 		
 		powerField = new JTextField();
 		powerField.setBounds(79, 78, 61, 28);
 		contentPane.add(powerField);
 		powerField.setColumns(10);
 		
 		JLabel lblToughness = new JLabel("Toughness:\n");
 		lblToughness.setBounds(156, 84, 79, 16);
 		contentPane.add(lblToughness);
 		
 		toughnessField = new JTextField();
 		toughnessField.setBounds(236, 78, 47, 28);
 		contentPane.add(toughnessField);
 		toughnessField.setColumns(10);
 		
 		JLabel lblArtist = new JLabel("Artist:");
 		lblArtist.setBounds(29, 106, 39, 16);
 		contentPane.add(lblArtist);
 		
 		artistField = new JTextField();
 		artistField.setBounds(77, 106, 134, 28);
 		contentPane.add(artistField);
 		artistField.setColumns(10);
 		
 		JLabel lblCardText = new JLabel("Card Text:");
 		lblCardText.setBounds(6, 171, 70, 16);
 		contentPane.add(lblCardText);
 		
 		imageURLField = new JTextField();
 		imageURLField.setBounds(79, 140, 134, 28);
 		contentPane.add(imageURLField);
 		imageURLField.setColumns(10);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(6, 200, 285, 191);
 		contentPane.add(scrollPane);
 		
 		textField = new JTextPane();
 		scrollPane.setViewportView(textField);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		panel.setBounds(0, 400, 900, 50);
 		contentPane.add(panel);
 		
 		JButton btnRevert = new JButton("Revert");
 		btnRevert.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				current.setValues(old);
 				over.getTableModel().fireTableDataChanged();
 				dataToField();
 			}
 		});
 		panel.add(btnRevert);
 		
 		JButton btnCancel = new JButton("Exit Without Saving");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				current.setValues(old);
 				over.getTableModel().fireTableDataChanged();
 				setEnabled(false);
 				setVisible(false);
 			}
 		});
 		panel.add(btnCancel);
 		
 		JButton btnSave = new JButton("Save");
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				fieldToData();
 				over.getTableModel().fireTableDataChanged();
 				over.save();
 			}
 		});
 		panel.add(btnSave);
 		
 		JButton btnSaveAndExit = new JButton("Save and Exit");
 		btnSaveAndExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				fieldToData();
 				over.getTableModel().fireTableDataChanged();
 				over.save();
 				setEnabled(false);
 				setVisible(false);
 			}
 		});
 		panel.add(btnSaveAndExit);
 		
 		JLabel lblExpansion = new JLabel("Expansion");
 		lblExpansion.setBounds(223, 106, 65, 16);
 		contentPane.add(lblExpansion);
 		
 		JLabel lblshort = new JLabel("(Short):");
 		lblshort.setBounds(223, 129, 61, 16);
 		contentPane.add(lblshort);
 		
 		expansionShortField = new JTextField();
 		expansionShortField.setBounds(227, 146, 61, 28);
 		contentPane.add(expansionShortField);
 		expansionShortField.setColumns(10);
 		
 		JLabel lblLoyalty = new JLabel("Loyalty:");
 		lblLoyalty.setBounds(171, 171, 55, 16);
 		contentPane.add(lblLoyalty);
 		
 		loyaltyField = new JTextField();
 		loyaltyField.setBounds(227, 172, 61, 28);
 		contentPane.add(loyaltyField);
 		loyaltyField.setColumns(10);
 		
 		dataToField();
 	}
 	
 	private void fieldToData(){
 		
 		current.name = nameField.getText();
 		if(current.name.length() == 0){
 			current.name = null;
 		}
 		current.manaCost = manaCostField.getText();
 		if(current.manaCost.length() == 0){
 			current.manaCost = null;
 		}
 		try{
 			String s = convertedManaCostField.getText();
 			if(s.length() > 0){
 				current.convertedManaCost = Integer.parseInt(s);
 			}
 			else{
 				current.convertedManaCost = Integer.MIN_VALUE;
 			}
 		}
 		catch(NumberFormatException e){
 			throw new NumberFormatException("Parsing NumberFormatException! Code: 201-01a");
 		}
 		current.text = textField.getText();
 		if(current.text.length() == 0){
 			current.text = null;
 		}
		current.superTypes = formatArray(" ", superType.mlm.getList());
 		if(current.superTypes.length() == 0){
 			current.superTypes = null;
 		}
		current.types = formatArray(" ", type.mlm.getList());
 		if(current.types.length() == 0){
 			current.types = null;
 		}
		current.subTypes = formatArray(" ", subType.mlm.getList());
 		if(current.subTypes.length() == 0){
 			current.subTypes = null;
 		}
		current.expansion = formatArray("/", expansion.mlm.getList());
 		if(current.expansion.length() == 0){
 			current.expansion = null;
 		}
		current.color = formatArray("/", expansion.mlm.getList());
 		if(current.color.length() == 0){
 			current.color = null;
 		}
		current.rarity = formatArray("/", rarity.mlm.getList());
 		if(current.rarity.length() == 0){
 			current.rarity = null;
 		}
 		try{
 			if(current.types != null && current.types.toLowerCase().contains("creature")){
 				String s = powerField.getText();
 				if(s.length() > 0){
 					current.power = Integer.parseInt(s);
 				}
 				else{
 					current.power = Integer.MIN_VALUE;
 				}
 			}
 		}
 		catch(NumberFormatException e){
 			throw new NumberFormatException("Parsing NumberFormatException! Code: 201-01b");
 		}
 		try{
 			if(current.types != null && current.types.toLowerCase().contains("creature")){
 				String s = toughnessField.getText();
 				if(s.length() > 0){
 					current.toughness = Integer.parseInt(s);
 				}
 				else{
 					current.toughness = Integer.MIN_VALUE;
 				}
 			}
 		}
 		catch(NumberFormatException e){
 			throw new NumberFormatException("Parsing NumberFormatException! Code: 201-01c");
 		}
 		current.expansionShort = expansionShortField.getText();
 		if(current.expansionShort.length() == 0){
 			current.expansionShort = null;
 		}
 		current.artist = artistField.getText();
 		if(current.artist.length() == 0){
 			current.artist = null;
 		}
 		try{
 			String s = loyaltyField.getText();
 			if(current.types != null && current.types.toLowerCase().contains("planeswalker")){
 				if(s.length() > 0){
 					current.loyalty = Integer.parseInt(s);
 				}
 				else{
 					current.loyalty = Integer.MIN_VALUE;
 				}
 			}
 		}
 		catch(NumberFormatException e){
 			throw new NumberFormatException("Parsing NumberFormatException! Code: 201-01d");
 		}
 	}
 	
	public <T> String formatArray(String delimiter, Iterable<T> array){
 		String r = "";
 		boolean first = true;
 		for(Object s : array){
 			if(first){
 				first = false;
 			}
 			else{
 				r += delimiter;
 			}
 			r += s;
 		}
 		return r;
 	}
 	
 	private void dataToField(){
 		if(current.name != null){
 			nameField.setText(current.name);
 		}
 		if(current.manaCost != null){
 			manaCostField.setText(current.manaCost);
 		}
 		if(current.convertedManaCost != Integer.MIN_VALUE){
 			convertedManaCostField.setText(String.valueOf(current.convertedManaCost));
 		}	
 		if(current.superTypes != null){
 			selectIndices(current.superTypes, " ", superType);
 		}
 		if(current.types != null){
 			selectIndices(current.types, " ", type);
 		}
 		if(current.subTypes != null){
 			selectIndices(current.subTypes, " ", subType);
 		}
 		if(current.expansion != null){
 			selectIndices(current.expansion, " ", expansion);
 		}
 		if(current.color != null){
 			selectIndices(current.rarity, " ", rarity);
 		}
 		if(current.color != null){
 			selectIndices(current.color, " ", color);
 		}
 		if(current.text != null){
 			textField.setText(current.text);
 		}
 		if(current.power != Integer.MIN_VALUE){
 			powerField.setText(String.valueOf(current.power));
 		}
 		if(current.toughness != Integer.MIN_VALUE){
 			toughnessField.setText(String.valueOf(current.power));
 		}
 		if(current.artist != null){
 			artistField.setText(current.artist);
 		}
 		if(current.expansion != null){
 			expansionShortField.setText(current.expansionShort);
 		}
 		if(current.loyalty != Integer.MIN_VALUE){
 			loyaltyField.setText(String.valueOf(current.loyalty));
 		}
 	}
 	
 	private static void selectIndices(String input, String separator, SelectionPanel panel){
 		String[] split = input.split(separator);
 		int[] index = new int[split.length];
 		int counter = 0;
 		for(String s : split){
 			int i = indexOf(s, panel.mlm.getList());
 			if(i >= 0){
 				index[counter] = i;
 			}
 			else{
 				panel.mlm.addElement(s);
 				index[counter] = panel.mlm.getList().size() - 1;
 			}
 			counter++;
 		}
 		panel.list.setSelectedIndices(index);
 	}
 	
 	private static <T> int indexOf(T obj, List<T> array){
 		if(!array.contains(obj)){
 			return -1;
 		}
 		for(int i = 0; i < array.size(); i++){
 			T t = array.get(i);
 			if(obj.equals(t)){
 				return i;
 			}
 		}
 		return -1;
 	}
 }
