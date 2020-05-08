 //Stephanie Reagle, Marc Mendiola
 
 package factory.swing;
 
 
 
 import factory.Part;
 import factory.managers.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 
 /*
  * This class is the GUI for the Parts Manager. This will be
  * instantiated in the general PartsManager class (which extends Client).
  */
 
 
 
 public class PartsManPanel extends JPanel{
 
 	private static final long serialVersionUID = -8456999510669881291L;
 	// Data
 	AddPanel addPanel;
 	EditPanel editPanel;
 	ArrayList<String> fileNames;
 	PartsManager partsManager;
 	JScrollPane scrollPane;
 	JPanel currentListPanel;
 	JTable table;
 	PartsTableModel model;
 	PartsTableCellRenderer renderer;
 	JPanel basePanel1;
 	JPanel basePanel2;
 	int currentID;
 
 	//New Part
 
 	// Methods
 
 	public PartsManPanel(PartsManager p){
 		this.setLayout(new CardLayout());
 		partsManager = p;
 		currentID = partsManager.parts.size();
 		fileNames = new ArrayList<String>();
 		fileNames.add("eye");
 		fileNames.add("body");
 		fileNames.add("hat");
 		fileNames.add("arm");
 		fileNames.add("shoe");
 		fileNames.add("mouth");
 		fileNames.add("nose");
 		fileNames.add("moustache");
 		fileNames.add("ear");
 		fileNames.add("cane");
 		fileNames.add("sword");
 		fileNames.add("tentacle");
 
 		model = new PartsTableModel();
 		model.addColumn("ID");
 		model.addColumn("Name");
 		model.addColumn("Stabalization Time");
 		model.addColumn("Image");
 		renderer = new PartsTableCellRenderer();
 		table = new JTable(model);
 		table.setDefaultRenderer(Integer.class, renderer);
 		table.setDefaultRenderer(String.class, renderer);
 		table.setAutoCreateRowSorter(true);
 		table.addMouseListener(new MouseListener());
 		TableColumn column = null;
 		for (int i = 0; i < 2; i ++){
 			column = table.getColumnModel().getColumn(i);
 			if(i==0){
 				column.setPreferredWidth(2);
 			}else if (i == 1){
 				column.setPreferredWidth(80);
 			}else if (i == 2){
 				column.setPreferredWidth(2);
 			}else if (i == 3){
 				column.setPreferredWidth(4);
 			}
 		}
 		table.setRowHeight(30);
 
 		scrollPane = new JScrollPane(table);
 		scrollPane.setMaximumSize(new Dimension(460, 300));
 		table.setFillsViewportHeight(true);
 		for(int i = 1; i < partsManager.parts.size(); i++){
 			for(Part temp : partsManager.parts.values()){
 				if(i == temp.id){
 					ImageIcon image = new ImageIcon(temp.imagePath);
 					Object[] row = {temp.id, temp.name, temp.nestStabilizationTime, image};
 					model.insertRow(model.getRowCount(), row);
 				}
 			}
 		}
 		addPanel = new AddPanel();
 		editPanel = new EditPanel();
 
 		basePanel1 = new JPanel();
 		basePanel1.setLayout(new BoxLayout(basePanel1, BoxLayout.Y_AXIS));
 		basePanel2 = new JPanel();
 		basePanel1.add(addPanel);
 		basePanel1.add(scrollPane);
 		basePanel2.add(editPanel);
 		this.add("basePanel1", basePanel1);
 		this.add("basePanel2", basePanel2);
 	}
 
 	public void setManager(PartsManager manager){
 		partsManager = manager;
 	}
 
 
 
 	// Internal Classes
 
 	private class AddPanel extends JPanel implements ActionListener{
 
 		private static final long serialVersionUID = 500493950494624477L;
 		// Data
 		JLabel title;
 		JLabel nameLabel;
 		JLabel imageLabel;
 		JLabel idLabel;
 		JLabel descriptionLabel;
 		JLabel nestStabalizationTimeLabel;
 		JLabel previewFrame;
 		JTextField name;
 		JComboBox imageSelection;
 		JSpinner nestStabalizationTime;
 		JTextArea description;
 		JScrollPane descriptionScrollPane;
 
 		JButton saveItem;
 
 
 		//Methods
 
 		public AddPanel(){
 			this.setLayout(new GridBagLayout());
 			GridBagConstraints c = new GridBagConstraints();
 
 
 			title = new JLabel ("Parts Manager");
 			title.setFont(new Font("Serif", Font.BOLD, 16));
 			nameLabel = new JLabel ("New Item : ");
 			name = new JTextField(8);
 			imageLabel = new JLabel ("Image : ");
 			idLabel = new JLabel("ID# : " + currentID);
 			descriptionLabel = new JLabel("Description : ");
 			nestStabalizationTimeLabel = new JLabel ("Nest Stabalization Time : ");
 			nestStabalizationTime = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
 						JFormattedTextField tf = ((JSpinner.DefaultEditor) nestStabalizationTime.getEditor()).getTextField();
 						tf.setEditable(false);
 			description = new JTextArea("Enter description here", 2, 10);
 			description.setLineWrap(true);
 			description.setWrapStyleWord(true);
 			descriptionScrollPane = new JScrollPane(description);
 			imageSelection = new JComboBox();
 			for(int i = 0; i < fileNames.size(); i++){
 				imageSelection.addItem(fileNames.get(i));
 			}
 			imageSelection.addActionListener(this);
 			previewFrame = new JLabel();
 			ImageIcon imagePreview = new ImageIcon("Images/" + fileNames.get(0) + ".png");
 			previewFrame.setIcon(imagePreview);
 			saveItem = new JButton ("Save Item");
 			saveItem.addActionListener(this);
 
 			//currentList = new JScrollPane();
 
 			c.gridx = 0;
 			c.gridy = 0;
 			c.gridwidth = 2;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.insets = new Insets(5,0,20,0);
 			this.add(title, c);
 			c.fill = GridBagConstraints.VERTICAL;
 			c.anchor = GridBagConstraints.LINE_START;
 			c.gridwidth = 1;
 			c.gridx = 0;
 			c.gridy = 1;
 			this.add(nameLabel, c);
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridx = 1;
 			c.gridy = 1;
 			this.add(name, c);
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridx = 0;
 			c.gridy = 2;
 			this.add(idLabel, c);
 			c.gridx = 0;
 			c.gridy = 3;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(nestStabalizationTimeLabel, c);
 			c.gridx = 1;
 			c.gridy = 3;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(nestStabalizationTime, c);
 			c.gridx = 0;
 			c.gridy = 4;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(imageLabel, c);
 			c.gridx = 1;
 			c.gridy = 4;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(imageSelection, c);
 			c.gridx = 2;
 			c.gridy = 4;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.insets = new Insets(5,20,20,0);
 			this.add(previewFrame, c);
 			c.insets = new Insets(5,0,20,0);
 			c.gridx = 0;
 			c.gridy = 5;
 			c.anchor = GridBagConstraints.LINE_START;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(descriptionLabel, c);
 			c.gridx = 1;
 			c.gridy = 5;
 			c.gridheight = 2;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(descriptionScrollPane, c);
 			c.gridx = 0;
 			c.gridy = 6;
 			c.gridheight = 1;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.anchor = GridBagConstraints.LINE_START;
 			this.add(saveItem, c);
 
 		}
 
 		public void updatePicture(String item){
 			ImageIcon imagePreview = new ImageIcon("Images/" + item + ".png");
 			previewFrame.setIcon(imagePreview);
 		}
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			String messageAddPanel = new String (" ");
 			if (ae.getSource() == saveItem){
 				String testName;
 				name.setText(name.getText().replaceAll("\\s","")) ;
 				testName = name.getText().toUpperCase();
 				boolean nameTaken = false;
 				for(String tempName : partsManager.parts.keySet()){
 					if(testName.equals(tempName.toUpperCase()))
 						nameTaken = true;
 				}
 				if(nameTaken){
 					name.setText("Name taken.");
 				}else if (name.getText().equals("")){
 					name.setText("No input.");
 				}else if ((Integer)nestStabalizationTime.getValue() <= 0){
 					name.setText("Invalid time.");	
 				}else{
 					System.out.println("I will create a new part and send it to the server.");
 
 					System.out.println("parts size : " + partsManager.parts.size());
 					Part p = new Part(name.getText(), currentID, description.getText(),"Images/" + (String)imageSelection.getSelectedItem() + ".png", (Integer)nestStabalizationTime.getValue());
 					addItem(p);
 					currentID++;
					messageAddPanel = "pm multi cmd addpartname " + p.name + " " + p.id + " " +
 							p.imagePath + " " + p.nestStabilizationTime + " " + p.description;
 					partsManager.sendCommand(messageAddPanel);
 					idLabel.setText("ID# : " + currentID);
 					name.setText("");
 					description.setText("Enter description here");
 					imageSelection.setSelectedIndex(0);
 					nestStabalizationTime.setValue(0);
 				}
 			}else{
 				JComboBox cb = (JComboBox)ae.getSource();
 				String selectedItem = (String)cb.getSelectedItem();
 				updatePicture(selectedItem);
 			}
 
 		}
 
 
 	}
 
 	private class EditPanel extends JPanel implements ActionListener{
 		// Data
 
 		private static final long serialVersionUID = -5141689953794161944L;
 		// Data
 		JLabel title;
 		JLabel nameLabel;
 		JLabel imageLabel;
 		JLabel idLabel;
 		JLabel descriptionLabel;
 		JLabel nestStabalizationTimeLabel;
 		JLabel previewFrame;
 		JTextField name;
 		JComboBox imageSelection;
 		JSpinner nestStabalizationTime;
 		JTextArea description;
 		JScrollPane descriptionScrollPane;
 
 		JButton saveItem;
 		JButton removeItem;
 		JButton cancel;
 		Part currentPart;
 
 
 
 		//Methods
 
 		public EditPanel(){
 			this.setLayout(new GridBagLayout());
 			GridBagConstraints c = new GridBagConstraints();
 			currentPart = new Part();
 
 			title = new JLabel ("Parts Manager");
 			title.setFont(new Font("Serif", Font.BOLD, 16));
 			nameLabel = new JLabel ("New Item : ");
 			name = new JTextField(8);
 			imageLabel = new JLabel ("Image : ");
 			idLabel = new JLabel("ID# : " + currentID);
 			descriptionLabel = new JLabel("Description : ");
 			nestStabalizationTimeLabel = new JLabel ("Nest Stabalization Time : ");
 			nestStabalizationTime = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
 			JFormattedTextField tf = ((JSpinner.DefaultEditor) nestStabalizationTime.getEditor()).getTextField();
 			description = new JTextArea("Enter description here", 2, 10);
 			description.setLineWrap(true);
 			description.setWrapStyleWord(true);
 			descriptionScrollPane = new JScrollPane(description);
 			imageSelection = new JComboBox();
 			for(int i = 0; i < fileNames.size(); i++){
 				imageSelection.addItem(fileNames.get(i));
 			}
 			imageSelection.addActionListener(this);
 			previewFrame = new JLabel();
 			ImageIcon imagePreview = new ImageIcon("Images/" + fileNames.get(0) + ".png");
 			previewFrame.setIcon(imagePreview);
 			saveItem = new JButton ("Save Changes");
 			saveItem.addActionListener(this);
 			removeItem = new JButton ("Remove Item");
 			removeItem.addActionListener(this);
 			cancel = new JButton ("Cancel");
 			cancel.addActionListener(this);
 			c.gridx = 0;
 			c.gridy = 0;
 			c.gridwidth = 2;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.insets = new Insets(5,0,20,0);
 			this.add(title, c);
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridwidth = 1;
 			c.anchor = GridBagConstraints.LINE_START;
 			c.gridx = 0;
 			c.gridy = 1;
 			this.add(nameLabel, c);
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridx = 1;
 			c.gridy = 1;
 			this.add(name, c);
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridx = 0;
 			c.gridy = 2;
 			this.add(idLabel, c);
 			c.gridx = 0;
 			c.gridy = 3;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(nestStabalizationTimeLabel, c);
 			c.gridx = 1;
 			c.gridy = 3;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(nestStabalizationTime, c);
 			c.gridx = 0;
 			c.gridy = 4;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(imageLabel, c);
 			c.gridx = 1;
 			c.gridy = 4;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(imageSelection, c);
 			c.gridx = 2;
 			c.gridy = 4;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.insets = new Insets(5,20,20,0);
 			this.add(previewFrame, c);
 			c.insets = new Insets(5,0,20,0);
 			c.gridx = 0;
 			c.gridy = 5;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.anchor = GridBagConstraints.LINE_START;
 			this.add(descriptionLabel, c);
 			c.gridx = 1;
 			c.gridy = 5;
 			c.gridheight = 2;
 			c.fill = GridBagConstraints.VERTICAL;
 			this.add(descriptionScrollPane, c);
 			c.gridx = 0;
 			c.gridy = 7;
 			c.gridheight = 1;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.anchor = GridBagConstraints.LINE_START;
 			this.add(saveItem, c);
 			c.gridx = 1;
 			c.gridy = 7;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.anchor = GridBagConstraints.LINE_START;
 			this.add(removeItem, c);
 			c.gridx = 2;
 			c.gridy = 7;
 			c.fill = GridBagConstraints.VERTICAL;
 			c.anchor = GridBagConstraints.CENTER;
 			this.add(cancel, c);
 
 
 
 		}
 
 		public void updatePicture(String path){
 			ImageIcon imagePreview = new ImageIcon(path);
 			previewFrame.setIcon(imagePreview);
 		}
 
 		public void actionPerformed(ActionEvent ae) {
 			String messageEditPanel = new String (" ");
 
 			if (ae.getSource() == removeItem){
 				System.out.println("I will remove a part and update the server.");
				messageEditPanel = "pm multi cmd rmpartname " + currentPart.name; 
 				partsManager.sendCommand(messageEditPanel);

 				removeItem(currentPart);
 				basePanel1.setVisible(true);
 				basePanel2.setVisible(false);
 			}else if(ae.getSource() == saveItem){
 
 				String testName; //hi
 				name.setText(name.getText().replaceAll("\\s","")) ;
 				testName = name.getText().toUpperCase();
 				boolean nameTaken = false;
 				for(String tempName : partsManager.parts.keySet()){
 					if(testName.equals(tempName.toUpperCase()) && !testName.equals(currentPart.name.toUpperCase()))
 						nameTaken = true;
 				}
 				if(nameTaken){
 					name.setText("Name taken.");
 				}else if (name.getText().equals("")){
 					name.setText("No input.");
 				}else if ((Integer)nestStabalizationTime.getValue() <= 0){
 					name.setText("Invalid time.");	
 				}else{
 
 					Part p = new Part(name.getText(), currentPart.id, description.getText(),"Images/" + 
 							(String)imageSelection.getSelectedItem() + ".png", (Integer)nestStabalizationTime.getValue());
					messageEditPanel = "pm multi set editpartname " + currentPart.name + " " + p.name + " " + 
 							p.id + " " + p.imagePath + " " + p.nestStabilizationTime + " " + p.description;
 					partsManager.sendCommand(messageEditPanel);
 					editItem(p);
 					partsManager.parts.remove(currentPart.name);
 					partsManager.parts.put(p.name, p);
 					currentPart = p;
 					basePanel1.setVisible(true);
 					basePanel2.setVisible(false);
 				}
 			}else if(ae.getSource() == cancel){
 				basePanel1.setVisible(true);
 				basePanel2.setVisible(false);
 
 			}else{
 				JComboBox cb = (JComboBox)ae.getSource();
 				String selectedItem = (String)cb.getSelectedItem();
 				updatePicture("Images/" + selectedItem + ".png");
 			}
 
 		}
 
 		public void updateEditPanel(JTable tempTable, int tempRow){
 			String tempName;
 			Part p;
 			tempName = (String) tempTable.getValueAt(tempRow, 1);
 			p = partsManager.parts.get(tempName);
 			currentPart = p;
 			name.setText(p.name);
 			idLabel.setText("ID# : " + p.id);
 			description.setText(p.description);
 			nestStabalizationTime.setValue((int)p.nestStabilizationTime);
 			for(int i = 0; i < fileNames.size(); i++){
 				if(p.imagePath.equals("Images/" + fileNames.get(i) + ".png")){
 					System.out.println("Image : " + fileNames.get(i));
 					imageSelection.setSelectedIndex(i);
 					break;
 				}
 			}
 		}
 
 	}
 
 
 	public void addItem(Part p){
 		partsManager.parts.put(p.name, p);
 		ImageIcon image = new ImageIcon(p.imagePath);
 		Object[] rowData = {p.id,p.name, p.nestStabilizationTime, image};
 		model.insertRow(model.getRowCount(),rowData);
 		table.revalidate();
 	}
 
 	public void editItem(Part p){
 		ImageIcon image = new ImageIcon(p.imagePath);
 		Object[] rowData = {p.id,p.name, p.nestStabilizationTime, image};
 		for(int i = 0; i < model.getRowCount(); i++){
 			if((Integer)p.id == (Integer)model.getValueAt(i, 0)){
 				model.removeRow(i);
 				model.insertRow(i,rowData);
 				break;
 			}
 
 		}
 
 		table.revalidate();
 	}
 
 	public void removeItem(Part p){
 		System.out.println("Removing " + p.name + " " + p.id);
 		for(int i = 0; i < model.getRowCount(); i++){
 			if((Integer)p.id == (Integer)model.getValueAt(i, 0)){
 				model.removeRow(i);
 				break;
 			}
 
 		}
 		partsManager.parts.remove(p.name);
 		table.revalidate();
 	}
 
 
 
 	public class PartsTableModel extends DefaultTableModel{
 		private static final long serialVersionUID = -3819364278880242952L;
 
 		public boolean isCellEditable(int row, int column){
 			return false;
 		}
 		public Class getColumnClass(int column)
 		{
 			return getValueAt(0, column).getClass();
 		}
 		public TableCellRenderer getCellRenderer(int row, int column){
 			return renderer;
 		}
 	}
 
 	public class PartsTableCellRenderer	extends DefaultTableCellRenderer{
 
 		private static final long serialVersionUID = 4476957663412431729L;
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 			JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(
 					table, value, isSelected, hasFocus, row, column);
 			renderedLabel.setHorizontalAlignment(SwingConstants.CENTER);
 			return renderedLabel;
 		}
 
 
 	}
 
 	public class MouseListener extends MouseAdapter{
 
 		public void mouseClicked(MouseEvent ae) {
 			if (ae.getClickCount() == 2) {
 				JTable target = (JTable)ae.getSource();
 				int row = target.getSelectedRow();
 				int column = target.getSelectedColumn();
 				editPanel.updateEditPanel(target, row);
 				basePanel1.setVisible(false);
 				basePanel2.setVisible(true);
 				addPanel.name.setText("");
 				addPanel.description.setText("Enter description here");
 				addPanel.imageSelection.setSelectedIndex(0);
 				addPanel.nestStabalizationTime.setValue(0);
 
 			}
 		}
 
 	}
 
 }
