 //***************************************************
 // 
 // 
 // 
 // 
 //***************************************************
 
 import static utils.Constants.*;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * @author Karan Goel
  * TODO: Change to a dialog
  */
 public class EditDB implements ActionListener {
 	
 	//TODO: real-time editing of data!
 
 	private String databaseName; // Holds the database's name being worked on
 	private JFrame frame;
 	private JTable table; // Shows all questions and answers from the database
 	private JPanel north, south;
 	private JButton deleteSelected, save, addRow;
 	Vector<Vector<Object>> data; // Stores all data from selected database
 	Vector<String> columnNames; // Stores names of columns of table
 	private DefaultTableModel model; // Stores the model for table
 
 	/**
 	 * Work on the GUI. Displays the table and buttons.
 	 * Also initializes the state of the program.
 	 * @param databaseName to be worked on
 	 * @param quesToAnsMap a map from questions to answers in passed database
 	 */
 	public EditDB(String databaseName, Map<String, String> quesToAnsMap) {
 		this.databaseName = databaseName;
 		createAndShowGUI(databaseName, quesToAnsMap);
 	}
 
 	/**
 	 * Listens to, and responds to various events in the program.
 	 */
 	public void actionPerformed(ActionEvent e) {
 		try {
 			if (e.getSource() == addRow) { 
 				// Add a new row
 				boolean isRowAdded = false;
 				JTextField question = new JTextField();
 				JTextField answer = new JTextField();
 				Object[] message = {"Question:", question, "Answer:", answer};
 				// Show a popup asking for question and answer from user
 				while (!isRowAdded) {
 					int option = JOptionPane.showConfirmDialog(frame, message, "Add New", 
 							JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (option == JOptionPane.OK_OPTION) {
 						if (question.getText() != null && answer.getText() != null &&
 								question.getText().length() > 0 && answer.getText().length() > 0) {
 							// User input is valid superficially
 							Vector<Object> newQuestion = new Vector<Object>(3, 1);
 							newQuestion.add(question.getText());
 							newQuestion.add(answer.getText());
 							newQuestion.add(false);
 							model.addRow(newQuestion); // Update the model with new question
 							table.scrollRectToVisible(table.getCellRect(model.getRowCount() + 1, 
 									model.getColumnCount(), false)); // Auto scroll to last row
 							isRowAdded = true;
 						} else {
 							JOptionPane.showMessageDialog(frame, "Please enter all fields");
 						}
 					} else { // User clicked cancel, so we don't need to loop here!
 						isRowAdded = true;
 					}
 				}
 			} else if (e.getSource() == save) {
 				// Save the database
 				File file = new File(DATABASE + databaseName);
 				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 				for (Vector<Object> singleRow : data) {
 					writer.write(singleRow.get(0) + ":::::" + singleRow.get(1));
 					writer.newLine(); // to insert a blank new line in file
 					writer.flush();
 				}
 				writer.close();
 				JOptionPane.showMessageDialog(null, "Database saved successfully!");
 				} else if (e.getSource() == deleteSelected) {
 				// Delete all selected rows
 				// System.out.println("Before deletion: " + data);
 				if (data.size() <= 0) {
 					JOptionPane.showMessageDialog(null, "Nothing to delete!");
 				} else {
 					// All rows to be removed
 					Vector<Vector<Object>> toRemove = new Vector<Vector<Object>>();
 					java.util.Iterator<Vector<Object>> dataItr = data.iterator();
 					while (dataItr.hasNext()) {
 						Vector<Object> singleRow = dataItr.next();
 						if ((Boolean) singleRow.get(2)) {
 							toRemove.add(singleRow);
 						}
 					}
 					if (!data.removeAll(toRemove)) {
 						JOptionPane.showMessageDialog(frame, 
 								"Could not delete rows. Something is wrong!");
 					}
 					model.setDataVector(data, columnNames); // Update the model!
 				}
 				// System.out.println("After deletion: " + data);
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 
 
 	//********************** BUILD GUI **********************//
 	/**
 	 * Puts the GUI together, initializes data.
 	 * @param databaseName of the database to work on
 	 * @param quesToAnsMap of all questions and answers in passed database
 	 */
 	private void createAndShowGUI(String databaseName, Map<String, String> quesToAnsMap) {
 		initializeFrame(databaseName);
 		columnNames = new Vector<String>(3, 0);
 		columnNames.add("Question");
 		columnNames.add("Answer");
 		columnNames.add("Delete?");
 		initializeNorth(quesToAnsMap, columnNames);
 		initializeSouth();
 		frame.add(north, BorderLayout.NORTH);
 		frame.add(south, BorderLayout.SOUTH);
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Initializes the state of frame and sets all properties.
 	 */
 	private void initializeFrame(String databaseName) {
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.setSize(new Dimension(550, 500));
 		frame.setLocation(new Point(450, 300));
 		frame.setTitle("Edit Database " + databaseName);
 		frame.setResizable(false);
 		frame.setLayout(new BorderLayout());
 	}
 
 	/**
 	 * Initializes the state of north portion of frame which holds
 	 * the table of data.
 	 */
 	private void initializeNorth(Map<String, String> quesToAnsMap, Vector<String> columnNames) {
 		north = new JPanel(new GridLayout(1, 1));
 		model = new DefaultTableModel(convertMapToVector(quesToAnsMap), columnNames) {
 			@Override
 			public Class<?> getColumnClass(int col) {
 				return getValueAt(0, col).getClass();
 			}
 		};
 		table = new JTable(model);
 		table.setFillsViewportHeight(true);
 		table.setRowHeight(25);
 		table.setRowMargin(5);
 		table.getTableHeader().setReorderingAllowed(false); // Disable reordering of columns
 		JScrollPane scrollPane = new JScrollPane(table);
 		north.add(scrollPane);
 	}
 
 	/**
 	 * Initializes the state of south portion of frame which holds the
 	 * buttons for all actions on data.
 	 */
 	private void initializeSouth() {
 		south = new JPanel(new GridLayout(1, 3));
 		deleteSelected = new JButton("Deleted Selected Rows");
 		deleteSelected.addActionListener(this);
 		addRow = new JButton("Add Row");
 		addRow.addActionListener(this);
 		save = new JButton("Save Database");
 		save.addActionListener(this);
 		south.add(deleteSelected);
 		south.add(addRow);
 		south.add(save);
 	}
 	//********************** BUILD GUI **********************//
 
 
 	/**
 	 * Converts a passed map of String to String to a 2D Vector and returns it.
 	 * @param quesToAnsMap to be converted to a Vector
 	 * @return a Vector of questions and answers
 	 */
 	public Vector<Vector<Object>> convertMapToVector(Map<String, String> quesToAnsMap) {
		data = new Vector<Vector<Object>>(quesToAnsMap.size(), 1);
 		if (quesToAnsMap.size() > 0) {
 			for (String question : quesToAnsMap.keySet()) {
 				Vector<Object> singleQuestion = new Vector<Object>(3, 1);
 				singleQuestion.add(question);
 				singleQuestion.add(quesToAnsMap.get(question));
 				singleQuestion.add(false);
 				data.add(singleQuestion);
 			}
 		}
		return data;
 	}
 
 }
