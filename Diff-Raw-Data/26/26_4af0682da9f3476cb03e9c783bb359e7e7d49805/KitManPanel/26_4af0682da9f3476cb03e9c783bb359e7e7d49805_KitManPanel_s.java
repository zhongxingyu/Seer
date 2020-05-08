 //Stephanie Reagle, Marc Mendiola
 //CS 200
 package factory.swing;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import factory.KitConfig;
 import factory.Part;
 import factory.managers.KitManager;
 
 public class KitManPanel extends JPanel{
 
 	private static final long serialVersionUID = -5760501420685052853L;
 
 
 	KitConfig currentKit = new KitConfig();
 	//for Create Kit Tab
 	AddPanel addPanel;
 	EditPanel editPanel;
 
 	JScrollPane scrollPane;
 	JPanel currentListPanel;
 	JTable table;
 	PartsTableModel model;
 	PartsTableCellRenderer renderer;
 	JPanel basePanel1;
 	JPanel basePanel2;
 	MouseListener mouseListener;
 
 
 	KitManager kitManager;
 
 	public KitManPanel(KitManager k){
 		setLayout(new CardLayout());
 		kitManager = k;
 		addPanel = new AddPanel();
 		editPanel = new EditPanel();
 		mouseListener = new MouseListener();
 		model = new PartsTableModel();
 		model.addColumn("Name");
 		renderer = new PartsTableCellRenderer();
 		table = new JTable(model);
 		table.setDefaultRenderer(Integer.class, renderer);
 		table.setDefaultRenderer(Double.class, renderer);
 		table.setDefaultRenderer(String.class, renderer);
 		table.setAutoCreateRowSorter(true);
 		table.addMouseListener(mouseListener);
 		TableColumn column = null;
 		for (int i = 0; i < 1; i ++){
 			column = table.getColumnModel().getColumn(i);
 			if(i==0)
 				column.setPreferredWidth(2);
 		}
 		table.setRowHeight(30);
 
 		scrollPane = new JScrollPane(table);
 		scrollPane.setMaximumSize(new Dimension(460, 300));
 		table.setFillsViewportHeight(true);
 
 
 		for(int i = 1; i < kitManager.getKitConfigList().size(); i++){
 			for(KitConfig temp : kitManager.getKitConfigList().values()){
 				Object[] row = {temp.kitName};
 				model.insertRow(model.getRowCount(), row);
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
 	public class AddPanel extends JPanel implements ActionListener{
 
 		JTextField cKitName = new JTextField( "Kit Name");
 		JLabel cMessages = new JLabel ("Messages:");
 		JButton cSave = new JButton("Save Kit Configuration");
 
 
 
 		JLabel cLabel1 = new JLabel ("Item 1");
 		JLabel cLabel2 = new JLabel ("Item 2");
 		JLabel cLabel3 = new JLabel ("Item 3");
 		JLabel cLabel4 = new JLabel ("Item 4");
 		JLabel cLabel5 = new JLabel ("Item 5");
 		JLabel cLabel6 = new JLabel ("Item 6");
 		JLabel cLabel7 = new JLabel ("Item 7");
 		JLabel cLabel8 = new JLabel ("Item 8");
 
 		JComboBox cItemComboBox1 = new JComboBox();
 		JComboBox cItemComboBox2 = new JComboBox();
 		JComboBox cItemComboBox3 = new JComboBox();
 		JComboBox cItemComboBox4 = new JComboBox();
 		JComboBox cItemComboBox5 = new JComboBox();
 		JComboBox cItemComboBox6 = new JComboBox();
 		JComboBox cItemComboBox7 = new JComboBox();
 		JComboBox cItemComboBox8 = new JComboBox();
 
 		JLabel cItemFrame1;
 		JLabel cItemFrame2;
 		JLabel cItemFrame3;
 		JLabel cItemFrame4;
 		JLabel cItemFrame5;
 		JLabel cItemFrame6;
 		JLabel cItemFrame7;
 		JLabel cItemFrame8;
 		ImageIcon cItem1;
 		ImageIcon cItem2;
 		ImageIcon cItem3;
 		ImageIcon cItem4;
 		ImageIcon cItem5;
 		ImageIcon cItem6;
 		ImageIcon cItem7;
 		ImageIcon cItem8;
 
 		public AddPanel(){
 
 
 
 
 
 			//Create Panel
 			cItemComboBox1.addActionListener(this);
 			cItemComboBox2.addActionListener(this);
 			cItemComboBox3.addActionListener(this);
 			cItemComboBox4.addActionListener(this);
 			cItemComboBox5.addActionListener(this);
 			cItemComboBox6.addActionListener(this);
 			cItemComboBox7.addActionListener(this);
 			cItemComboBox8.addActionListener(this);
 
 			cItem1 = new ImageIcon("Images/none.png");
 			cItem2 = new ImageIcon("Images/none.png");
 			cItem3 = new ImageIcon("Images/none.png");
 			cItem4 = new ImageIcon("Images/none.png");
 			cItem5 = new ImageIcon("Images/none.png");
 			cItem6 = new ImageIcon("Images/none.png");
 			cItem7 = new ImageIcon("Images/none.png");
 			cItem8 = new ImageIcon("Images/none.png");
 
 			cItemFrame1 = new JLabel();
 			cItemFrame1.setIcon(cItem1);
 			cItemFrame2 = new JLabel();
 			cItemFrame2.setIcon(cItem2);
 			cItemFrame3 = new JLabel();
 			cItemFrame3.setIcon(cItem3);
 			cItemFrame4 = new JLabel();
 			cItemFrame4.setIcon(cItem4);
 			cItemFrame5 = new JLabel();
 			cItemFrame5.setIcon(cItem5);
 			cItemFrame6 = new JLabel();
 			cItemFrame6.setIcon(cItem6);
 			cItemFrame7 = new JLabel();
 			cItemFrame7.setIcon(cItem7);
 			cItemFrame8 = new JLabel();
 			cItemFrame8.setIcon(cItem8);
 
 			setLayout(new GridBagLayout());
 			GridBagConstraints c = new GridBagConstraints();
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridx = 0;
 			c.gridy = 0;
 			this.add(cLabel1, c);
 
 			c.gridy = 1;
 			this.add(cLabel2, c);
 
 			c.gridy = 2;
 			this.add(cLabel3, c);
 
 			c.gridy = 3;
 			this.add(cLabel4, c);
 
 			c.gridy = 4;
 			this.add(cLabel5, c);
 
 			c.gridy = 5;
 			this.add(cLabel6, c);
 
 			c.gridy = 6;
 			this.add(cLabel7, c);
 
 			c.gridy = 7;
 			this.add(cLabel8, c);
 
 			c.gridy = 9;
 			this.add(cKitName, c);
 
 			c.gridy = 10;
 			this.add(cSave, c);
 
 			c.gridx = 1;
 			c.gridy = 0;
 			this.add(cItemComboBox1, c);
 
 			c.gridy = 1;
 			this.add(cItemComboBox2, c);
 
 			c.gridy = 2;
 			this.add(cItemComboBox3, c);
 
 			c.gridy = 3;
 			this.add(cItemComboBox4, c);
 
 			c.gridy = 4;
 			this.add(cItemComboBox5, c);
 
 			c.gridy = 5;
 			this.add(cItemComboBox6, c);
 
 			c.gridy = 6;
 			this.add(cItemComboBox7, c);
 
 			c.gridy = 7;
 			this.add(cItemComboBox8, c);
 
 			c.gridy = 8;
 			this.add(cMessages, c);
 
 			c.gridx = 2;
 			c.gridy = 0;
 			this.add(cItemFrame1, c);
 
 			c.gridy = 1;
 			this.add(cItemFrame2, c);
 
 			c.gridy = 2;
 			this.add(cItemFrame3, c);
 
 			c.gridy = 3;
 			this.add(cItemFrame4, c);
 
 			c.gridy = 4;
 			this.add(cItemFrame5, c);
 
 			c.gridy = 5;
 			this.add(cItemFrame6, c);
 
 			c.gridy = 6;
 			this.add(cItemFrame7, c);
 
 			c.gridy = 7;
 			this.add(cItemFrame8, c);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			String set = new String();
 			if (ae.getSource() == cSave) {
 
 			}else{
 				JComboBox cb = (JComboBox)ae.getSource();
 				String selectedItem = (String)cb.getSelectedItem();
 				if(cb == this.cItemComboBox1)
 					updatePicture(cItem1, cItemFrame1, selectedItem);
 				else if(cb == cItemComboBox2)
 					updatePicture(cItem2, cItemFrame2, selectedItem);
 				else if(cb == cItemComboBox3)
 					updatePicture(cItem3, cItemFrame3, selectedItem);
 				else if(cb == cItemComboBox4)
 					updatePicture(cItem4, cItemFrame4, selectedItem);
 				else if(cb == cItemComboBox5)
 					updatePicture(cItem5, cItemFrame5, selectedItem);
 				else if(cb == cItemComboBox6)
 					updatePicture(cItem6, cItemFrame6, selectedItem);
 				else if(cb == cItemComboBox7)
 					updatePicture(cItem7, cItemFrame7, selectedItem);
 				else if(cb == cItemComboBox8)
 					updatePicture(cItem8, cItemFrame8, selectedItem);
 
 			}
 		}
 
 	}
 
 
 	public class EditPanel extends JPanel implements ActionListener{
 		JLabel mItemFrame1;
 		JLabel mItemFrame2;
 		JLabel mItemFrame3;
 		JLabel mItemFrame4;
 		JLabel mItemFrame5;
 		JLabel mItemFrame6;
 		JLabel mItemFrame7;
 		JLabel mItemFrame8;
 		ImageIcon mItem1;
 		ImageIcon mItem2;
 		ImageIcon mItem3;
 		ImageIcon mItem4;
 		ImageIcon mItem5;
 		ImageIcon mItem6;
 		ImageIcon mItem7;
 		ImageIcon mItem8;
 		JComboBox mKitComboBox = new JComboBox();
 
 		JLabel mLabel1 = new JLabel ("Item 1");
 		JLabel mLabel2 = new JLabel ("Item 2");
 		JLabel mLabel3 = new JLabel ("Item 3");
 		JLabel mLabel4 = new JLabel ("Item 4");
 		JLabel mLabel5 = new JLabel ("Item 5");
 		JLabel mLabel6 = new JLabel ("Item 6");
 		JLabel mLabel7 = new JLabel ("Item 7");
 		JLabel mLabel8 = new JLabel ("Item 8");
 
 		JComboBox mItemComboBox1 = new JComboBox();
 		JComboBox mItemComboBox2 = new JComboBox();
 		JComboBox mItemComboBox3 = new JComboBox();
 		JComboBox mItemComboBox4 = new JComboBox();
 		JComboBox mItemComboBox5 = new JComboBox();
 		JComboBox mItemComboBox6 = new JComboBox();
 		JComboBox mItemComboBox7 = new JComboBox();
 		JComboBox mItemComboBox8 = new JComboBox();
 
 
 		JTextField mKitName = new JTextField( "Default Kit");
 		JLabel mMessages = new JLabel ("Messages:");
 		JButton mSave = new JButton("Save Kit Configuration");
 		
 		JButton mRemove = new JButton("Remove Kit");
 		JButton cancel = new JButton("Cancel");
 
 		public EditPanel(){
 			mItemComboBox1.addItem("None");
 			mItemComboBox2.addItem("None");
 			mItemComboBox3.addItem("None");
 			mItemComboBox4.addItem("None");
 			mItemComboBox5.addItem("None");
 			mItemComboBox6.addItem("None");
 			mItemComboBox7.addItem("None");
 			mItemComboBox8.addItem("None");
 
 			for(String key : kitManager.getPartsList().keySet()){
 				mItemComboBox1.addItem(key);
 				mItemComboBox2.addItem(key);
 				mItemComboBox3.addItem(key);
 				mItemComboBox4.addItem(key);
 				mItemComboBox5.addItem(key);
 				mItemComboBox6.addItem(key);
 				mItemComboBox7.addItem(key);
 				mItemComboBox8.addItem(key);
 			}
 
 			for(String key : kitManager.getKitConfigList().keySet()){
 				mKitComboBox.addItem(key);
 			}
 			mKitComboBox.addActionListener(this);
 
 
 
 			mItemComboBox1.addActionListener(this);
 			mItemComboBox2.addActionListener(this);
 			mItemComboBox3.addActionListener(this);
 			mItemComboBox4.addActionListener(this);
 			mItemComboBox5.addActionListener(this);
 			mItemComboBox6.addActionListener(this);
 			mItemComboBox7.addActionListener(this);
 			mItemComboBox8.addActionListener(this);
 
 			mItem1 = new ImageIcon("Images/none.png");
 			mItem2 = new ImageIcon("Images/none.png");
 			mItem3 = new ImageIcon("Images/none.png");
 			mItem4 = new ImageIcon("Images/none.png");
 			mItem5 = new ImageIcon("Images/none.png");
 			mItem6 = new ImageIcon("Images/none.png");
 			mItem7 = new ImageIcon("Images/none.png");
 			mItem8 = new ImageIcon("Images/none.png");
 
 
 			mItemFrame1 = new JLabel();
 			mItemFrame1.setIcon(mItem1);
 			mItemFrame2 = new JLabel();
 			mItemFrame2.setIcon(mItem2);
 			mItemFrame3 = new JLabel();
 			mItemFrame3.setIcon(mItem3);
 			mItemFrame4 = new JLabel();
 			mItemFrame4.setIcon(mItem4);
 			mItemFrame5 = new JLabel();
 			mItemFrame5.setIcon(mItem5);
 			mItemFrame6 = new JLabel();
 			mItemFrame6.setIcon(mItem6);
 			mItemFrame7 = new JLabel();
 			mItemFrame7.setIcon(mItem7);
 			mItemFrame8 = new JLabel();
 			mItemFrame8.setIcon(mItem8);
 			
 			mSave.addActionListener(this);
 			mRemove.addActionListener(this);
 			cancel.addActionListener(this);
 			
 			JTabbedPane tabbedPane = new JTabbedPane();
 			GridBagConstraints c = new GridBagConstraints();
 
 
 
 			setLayout(new GridBagLayout());
 
 			c.fill = GridBagConstraints.VERTICAL;
 			c.gridx = 0;
 			c.gridy = 0;
 			add(mKitComboBox, c);
 
 			c.gridy = 1;
 			add(mLabel1, c);
 
 			c.gridy = 2;
 			add(mLabel2, c);
 
 			c.gridy = 3;
 			add(mLabel3, c);
 
 			c.gridy = 4;
 			add(mLabel4, c);
 
 			c.gridy = 5;
 			add(mLabel5, c);
 
 			c.gridy = 6;
 			add(mLabel6, c);
 
 			c.gridy = 7;
 			add(mLabel7, c);
 
 			c.gridy = 8;
 			add(mLabel8, c);
 
 			c.gridx = 1;
 			c.gridy = 1;
 			add(mItemComboBox1, c);
 
 			c.gridy = 2;
 			add(mItemComboBox2, c);
 
 			c.gridy = 3;
 			add(mItemComboBox3, c);
 
 			c.gridy = 4;
 			add(mItemComboBox4, c);
 
 			c.gridy = 5;
 			add(mItemComboBox5, c);
 
 			c.gridy = 6;
 			add(mItemComboBox6, c);
 
 			c.gridy = 7;
 			add(mItemComboBox7, c);
 
 			c.gridy = 8;
 			add(mItemComboBox8, c);
 
 			c.gridy = 9;
 			add(mKitName, c);
 			
 			c.gridx = 0;
 			c.gridy = 10;
 			add(mSave, c);
 
 			c.gridx = 1;
 			c.gridy = 10;
 			add(mRemove, c);
 			
 			c.gridx = 2;
 			c.gridy = 10;
 			add(cancel, c);
 
 			c.gridx = 2;
 			c.gridy = 1;
 			add(mItemFrame1, c);
 
 			c.gridy = 2;
 			add(mItemFrame2, c);
 
 			c.gridy = 3;
 			add(mItemFrame3, c);
 
 			c.gridy = 4;
 			add(mItemFrame4, c);
 
 			c.gridy = 5;
 			add(mItemFrame5, c);
 
 			c.gridy = 6;
 			add(mItemFrame6, c);
 
 			c.gridy = 7;
 			add(mItemFrame7, c);
 
 			c.gridy = 8;
 			add(mItemFrame8, c);
 		}
 
 
 		public void actionPerformed(ActionEvent ae) {
 			JComboBox cb = (JComboBox)ae.getSource();
 			String selectedItem = (String)cb.getSelectedItem();
 			if(cb == mItemComboBox1)
 				updatePicture(mItem1, mItemFrame1, selectedItem);
 			else if(cb == mItemComboBox2)
 				updatePicture(mItem2, mItemFrame2, selectedItem);
 			else if(cb == mItemComboBox3)
 				updatePicture(mItem3, mItemFrame3, selectedItem);
 			else if(cb == mItemComboBox4)
 				updatePicture(mItem4, mItemFrame4, selectedItem);
 			else if(cb == mItemComboBox5)
 				updatePicture(mItem5, mItemFrame5, selectedItem);
 			else if(cb == mItemComboBox6)
 				updatePicture(mItem6, mItemFrame6, selectedItem);
 			else if(cb == mItemComboBox7)
 				updatePicture(mItem7, mItemFrame7, selectedItem);
 			else if(cb == mItemComboBox8)
 				updatePicture(mItem8, mItemFrame8, selectedItem);
 			else if(cb == mKitComboBox){
 
 				KitConfig k;
 				k = kitManager.getKitConfigList().get(mKitComboBox.getSelectedItem());
 				currentKit = k;
 				System.out.println(currentKit.kitName);
 				mKitName.setText(currentKit.kitName);
 
 				mItemComboBox1.setSelectedItem(currentKit.listOfParts.get(0).name);
 				mItemComboBox2.setSelectedItem(currentKit.listOfParts.get(1).name);
 				mItemComboBox3.setSelectedItem(currentKit.listOfParts.get(2).name);
 				mItemComboBox4.setSelectedItem(currentKit.listOfParts.get(3).name);
 				if(currentKit.listOfParts.size() == 5){
 					mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 					mItemComboBox6.setSelectedItem("None");
 					mItemComboBox7.setSelectedItem("None");
 					mItemComboBox8.setSelectedItem("None");
 				}else if (currentKit.listOfParts.size() == 6){
 					mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 					mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 					mItemComboBox7.setSelectedItem("None");
 					mItemComboBox8.setSelectedItem("None");
 
 				}else if (currentKit.listOfParts.size() == 7){
 					mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 					mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 					mItemComboBox7.setSelectedItem(currentKit.listOfParts.get(6).name);
 					mItemComboBox8.setSelectedItem("None");
 				}else if (currentKit.listOfParts.size() == 8){
 					mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 					mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 					mItemComboBox7.setSelectedItem(currentKit.listOfParts.get(6).name);
 					mItemComboBox8.setSelectedItem(currentKit.listOfParts.get(7).name);
 
 				}else{
 					mItemComboBox5.setSelectedItem("None");
 					mItemComboBox6.setSelectedItem("None");
 					mItemComboBox7.setSelectedItem("None");
 					mItemComboBox8.setSelectedItem("None");
 				}
 
 			}
 
 
 
 
 		}
 
 
 		public void updateEditPanel(JTable tempTable, int tempRow){
 			KitConfig k;
 			k = kitManager.getKitConfigList().get(mKitComboBox.getSelectedItem());
 			currentKit = k;
 			System.out.println(currentKit.kitName);
 			mKitName.setText(currentKit.kitName);
 
 			mItemComboBox1.setSelectedItem(currentKit.listOfParts.get(0).name);
 			mItemComboBox2.setSelectedItem(currentKit.listOfParts.get(1).name);
 			mItemComboBox3.setSelectedItem(currentKit.listOfParts.get(2).name);
 			mItemComboBox4.setSelectedItem(currentKit.listOfParts.get(3).name);
 			if(currentKit.listOfParts.size() == 5){
 				mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 				mItemComboBox6.setSelectedItem("None");
 				mItemComboBox7.setSelectedItem("None");
 				mItemComboBox8.setSelectedItem("None");
 			}else if (currentKit.listOfParts.size() == 6){
 				mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 				mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 				mItemComboBox7.setSelectedItem("None");
 				mItemComboBox8.setSelectedItem("None");
 
 			}else if (currentKit.listOfParts.size() == 7){
 				mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 				mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 				mItemComboBox7.setSelectedItem(currentKit.listOfParts.get(6).name);
 				mItemComboBox8.setSelectedItem("None");
 			}else if (currentKit.listOfParts.size() == 8){
 				mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 				mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 				mItemComboBox7.setSelectedItem(currentKit.listOfParts.get(6).name);
 				mItemComboBox8.setSelectedItem(currentKit.listOfParts.get(7).name);
 
 			}else{
 				mItemComboBox5.setSelectedItem("None");
 				mItemComboBox6.setSelectedItem("None");
 				mItemComboBox7.setSelectedItem("None");
 				mItemComboBox8.setSelectedItem("None");
 			}
 
 		}
 
 
 
 
 
 	}
 
 	public void updatePicture(ImageIcon icon, JLabel frame, String path){
 		icon = new ImageIcon("Images/" + path + ".png");
 		System.out.println(path);
 		frame.setIcon(icon);
 	}
 
 
 
 
 
 
 
 	public class PartsTableModel extends DefaultTableModel{
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
 				if(ae.getSource() == table){
 					JTable target = (JTable)ae.getSource();
 					int row = target.getSelectedRow();
 					int column = target.getSelectedColumn();
 					editPanel.updateEditPanel(target, row);
 					basePanel1.setVisible(false);
 					basePanel2.setVisible(true);
 /*
 					KitConfig k;
 					k = kitManager.getKitConfigList().get(editPanel.mKitComboBox.getSelectedItem());
 					currentKit = k;
 					System.out.println(currentKit.kitName);
 					editPanel.mKitName.setText(currentKit.kitName);
 
 					editPanel.mItemComboBox1.setSelectedItem(currentKit.listOfParts.get(0).name);
 					editPanel.mItemComboBox2.setSelectedItem(currentKit.listOfParts.get(1).name);
 					editPanel.mItemComboBox3.setSelectedItem(currentKit.listOfParts.get(2).name);
 					editPanel.mItemComboBox4.setSelectedItem(currentKit.listOfParts.get(3).name);
 					if(currentKit.listOfParts.size() == 5){
 						editPanel.mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 						editPanel.mItemComboBox6.setSelectedItem("None");
 						editPanel.mItemComboBox7.setSelectedItem("None");
 						editPanel.mItemComboBox8.setSelectedItem("None");
 					}else if (currentKit.listOfParts.size() == 6){
 						editPanel.mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 						editPanel.mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 						editPanel.mItemComboBox7.setSelectedItem("None");
 						editPanel.mItemComboBox8.setSelectedItem("None");
 
 					}else if (currentKit.listOfParts.size() == 7){
 						editPanel.mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 						editPanel.mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 						editPanel.mItemComboBox7.setSelectedItem(currentKit.listOfParts.get(6).name);
 						editPanel.mItemComboBox8.setSelectedItem("None");
 					}else if (currentKit.listOfParts.size() == 8){
 						editPanel.mItemComboBox5.setSelectedItem(currentKit.listOfParts.get(4).name);
 						editPanel.mItemComboBox6.setSelectedItem(currentKit.listOfParts.get(5).name);
 						editPanel.mItemComboBox7.setSelectedItem(currentKit.listOfParts.get(6).name);
 						editPanel.mItemComboBox8.setSelectedItem(currentKit.listOfParts.get(7).name);
 
 					}else{
 						editPanel.mItemComboBox5.setSelectedItem("None");
 						editPanel.mItemComboBox6.setSelectedItem("None");
 						editPanel.mItemComboBox7.setSelectedItem("None");
 						editPanel.mItemComboBox8.setSelectedItem("None");
 					}
 */
 				}
 			}
 		}
 
 
 	}
 }
