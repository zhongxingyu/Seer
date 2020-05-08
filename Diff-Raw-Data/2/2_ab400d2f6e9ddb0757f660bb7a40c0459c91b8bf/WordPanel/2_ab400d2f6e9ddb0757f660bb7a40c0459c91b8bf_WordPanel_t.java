 package database.tabs;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.swing.*;
 
 import database.CheckURL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kevingoy
  * Date: 14.01.13
  * Time: 15:20
  * To change this template use File | Settings | File Templates.
  */
 public class WordPanel extends JPanel {
 
 		private JPanel metaBox = new JPanel();
 		private JTable wordTable;
 		private JScrollPane scrollPane = new JScrollPane(wordTable);
 		private String[] wordTableHeader = new String[]{"Schlagwort"};
 		private CheckURL db;
 
 		public WordPanel(CheckURL db) {
 			this.db = db;
 			setLayout(new GridLayout(2, 1));
 			generateMetaBoxComponents();
 			updateAndAddTable();
 		}
 
 		public void updateAndAddTable() 	{
 			ResultSet shelfSet = db.executeSelect("Select * from Schlagwort");
 			wordTable = new JTable(getTableContent(shelfSet, wordTableHeader.length), wordTableHeader){
 				public boolean isCellEditable(int rowIndex, int colIndex) {
 					return false;   //Disallow the editing of any cell
 				}
 			};
 			wordTable.setAutoCreateRowSorter(true);
 			wordTable.addMouseListener(new MouseListener() {
 				@Override
 				public void mouseClicked(MouseEvent mouseEvent) {
 					if (mouseEvent.getClickCount() == 2) {
 						makeUpdatePopUp();
 					}
 				}
 
 				@Override
 				public void mousePressed(MouseEvent mouseEvent) {
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent mouseEvent) {
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent mouseEvent) {
 				}
 
 				@Override
 				public void mouseExited(MouseEvent mouseEvent) {
 				}
 			});
 			remove(scrollPane);
 			scrollPane = new JScrollPane(wordTable);
 			add(scrollPane);
 
 		}
 
 		private void generateMetaBoxComponents() {
 			final JLabel residenceLabel = new JLabel("Schlagwort");
 			final JTextField residenceTextField = new JTextField();
 
 			JButton okButton = new JButton("OK");
 			okButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					getAllContentOfComponentsAndInsert(residenceTextField);
 				}
 			});
 			metaBox.setLayout(new GridLayout(0, 2));
 			metaBox.add(residenceLabel);
 			metaBox.add(residenceTextField);
 			metaBox.add(new Label(""));
 			metaBox.add(okButton);
 			add(metaBox);
 
 		}
 
 		private void makeUpdatePopUp() {
 			final String word = String.valueOf(wordTable.getValueAt(wordTable.getSelectedRow(), 0));
 			final int wordID = getID("Select schlagwortid FROM schlagwort where schlagwort ='", word);
 			final JFrame updateFrame = new JFrame();
 			updateFrame.setTitle("Schlagwort aendern.");
 			updateFrame.setVisible(true);
 			updateFrame.setAlwaysOnTop(true);
 			updateFrame.setSize(400, 400);
 			JLabel wordLabel = new JLabel("Schlagwort");
 			final JTextField wordTextField = new JTextField();
 			JButton okButton = new JButton("OK");
 			okButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					String newWord = String.valueOf(wordTextField.getText());
 					if (!wordTextField.getText().equals("")) {
						db.executeChanges("UPDATE schlagwort SET schlagwort='" + newWord + "' WHERE schlagwortid='" + wordID + "'");
 					}
 					updateFrame.setVisible(false);
 					updateAndAddTable();
 
 				}
 			});
 			JButton cancelButton = new JButton("Cancel");
 			cancelButton.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent actionEvent) {
 					updateFrame.setVisible(false);
 				}
 			});
 			updateFrame.setLayout(new GridLayout(0, 2));
 			updateFrame.add(wordLabel);
 			updateFrame.add(wordTextField);
 			updateFrame.add(cancelButton);
 			updateFrame.add(okButton);
 		}
 
 		private void getAllContentOfComponentsAndInsert(JTextField wordTextField) {
 			String word = wordTextField.getText();
 			boolean validName = true;
 			ResultSet shelfSet = db.executeSelect("Select * from Schlagwort");
 			try {
 				while (shelfSet.next()) {
 					if (shelfSet.getString(2).equals(word)) {
 						validName = false;
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			if (validName) {
 				db.executeChanges("INSERT INTO schlagwort Values(DEFAULT,'" + word + "')");
 				updateAndAddTable();
 			} else {
 				JOptionPane jOptionPane = new JOptionPane();
 				JOptionPane.showMessageDialog(jOptionPane, "Das Schlagwort ist bereits vorhanden.", "Schlagwortfehler", JOptionPane.ERROR_MESSAGE);
 			}
 
 		}
 
 		private int getID(String sql, String idName) {
 			int id = 0;
 			ResultSet resultSet = db.executeSelect(sql + idName + "'");
 			try {
 				resultSet.next();
 				id = resultSet.getInt(1);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			return id;
 		}
 
 		private String[][] getTableContent(ResultSet resultSet, int columnLength) {
 			String[][] tableContent = null;
 			try {
 				int rowCount = 0;
 				while (resultSet.next()) {
 					rowCount++;
 				}
 				resultSet.beforeFirst();
 				tableContent = new String[rowCount][columnLength];
 				int rowIndex = 0;
 				while (resultSet.next()) {
 					tableContent[rowIndex][0] = resultSet.getString("schlagwort");
 					rowIndex++;
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			return tableContent;
 		}
 	}
 
 
 
