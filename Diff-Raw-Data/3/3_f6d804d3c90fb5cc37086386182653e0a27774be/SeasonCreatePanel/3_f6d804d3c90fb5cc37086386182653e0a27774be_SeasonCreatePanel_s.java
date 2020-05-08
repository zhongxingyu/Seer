 package admin.panel.season;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.Utils;
 import admin.MainFrame;
 
 import data.GameData;
 import data.Person;
 
 /**
  * Screen for creating a season. Controls the number of contestants and weeks
  * and the two tribes name.
  * 
  * @author CS2212 GROUP TWO 2011/2012
  * 
  */
 public class SeasonCreatePanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private boolean programChange = false; // true when spinner value changed by
 											// program
 
 	private JSpinner spnWeek;
 	private JSpinner spnContestant;
 
 	private SpinnerNumberModel weekModel;
 	private SpinnerNumberModel contestantModel;
 
 	private JTextField txtTribe1;
 	private JTextField txtTribe2;
 
 	private JLabel lblWelcomeText;
 	private JLabel lblInfoText;
 	private JLabel lblWeeks;
 	private JLabel lblContestants;
 	private JLabel lblTribe1;
 	private JLabel lblTribe2;
 
 	private JButton btnCreate;
 
 	private JPanel innerPanel;
 	private SeasonCreateFieldsPanel fieldsPanel;
 
 	public SeasonCreatePanel() {
 
 		// ------instantiations-------
 		lblWelcomeText = new JLabel("Welcome to SurvivorPool!");
 		lblInfoText = new JLabel(
 				"Fill out the fields below to create a new season.");
 		lblWeeks = new JLabel("Weeks:");
 		lblContestants = new JLabel("Contestants:");
 		lblTribe1 = new JLabel("Tribe 1:");
 		lblTribe2 = new JLabel("Tribe 2:");
 
 		weekModel = new SpinnerNumberModel(3, 3, 12, 1); // default,low,min,step
 		spnWeek = new JSpinner(weekModel);
 		JFormattedTextField nonEditableWeek = ((JSpinner.DefaultEditor) spnWeek
 				.getEditor()).getTextField();
 		nonEditableWeek.setEditable(false);
 
 		contestantModel = new SpinnerNumberModel(6, 6, 15, 1);// default,low,min,step
 		spnContestant = new JSpinner(contestantModel);
 		JFormattedTextField nonEditableCont = ((JSpinner.DefaultEditor) spnContestant
 				.getEditor()).getTextField();
 		nonEditableCont.setEditable(false);
 
 		txtTribe1 = new JTextField("");
 		txtTribe2 = new JTextField("");
 
 		btnCreate = new JButton("Create Season");
 
 		fieldsPanel = new SeasonCreateFieldsPanel(lblWeeks, spnWeek,
 				lblContestants, spnContestant, lblTribe1, txtTribe1, lblTribe2,
 				txtTribe2, btnCreate);
 
 		innerPanel = new JPanel();
 
 		// ------component settings-------
 		this.setLayout(new BorderLayout());
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 
 		innerPanel.setLayout(new BorderLayout());
 
 		lblWelcomeText.setFont(new Font("sanserif", Font.BOLD, 30));
 		lblWelcomeText.setHorizontalAlignment(SwingConstants.CENTER);
 
 		lblInfoText.setFont(new Font("sanserif", Font.ITALIC, 18));
 		lblInfoText.setHorizontalAlignment(SwingConstants.CENTER);
 
 		// ------component assembly------
 		this.add(lblWelcomeText, BorderLayout.NORTH);
 		this.add(innerPanel, BorderLayout.CENTER);
 		innerPanel.add(lblInfoText, BorderLayout.NORTH);
 		innerPanel.add(fieldsPanel, BorderLayout.CENTER);
 
 		// ------component listeners------
 		spnWeek.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				JSpinner spn = (JSpinner) ce.getSource();
 				if (!programChange) // makes sure that the code did not change
 									// the value
 					changeSpinnerValue(spn);
 			}
 
 		});
 
 		spnContestant.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				JSpinner spn = (JSpinner) ce.getSource();
 				if (!programChange)
 					changeSpinnerValue(spn);
 			}
 
 		});
 
 		btnCreate.addActionListener(new ActionListener() {
 
 			// write the number of contestants and weeks to the file.
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				try {
 					if (checkValidTribeName(txtTribe1.getText())) {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"Invalid tribe name", txtTribe1);
 						// lblAlert.setText("Invalid tribe names!");
 						return;
 					} else if (checkValidTribeName(txtTribe2.getText())) {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"Invalid tribe name", txtTribe2);
 						// lblAlert.setText("Invalid tribe names!");
 						return;
 					} else if (txtTribe1.getText().equals(txtTribe2.getText())) {
 						MainFrame.getRunningFrame().setStatusErrorMsg(
 								"Invalid tribe names, cannot be the same",
 								txtTribe1, txtTribe2);
 						return;
 					}
 					MainFrame.getRunningFrame().setStatusMsg(
 							"Valid tribe names");
 					GameData.initSeason(Integer.parseInt(spnContestant
 							.getValue().toString()));
 					GameData.getCurrentGame().setTribeNames(
 							txtTribe1.getText(), txtTribe2.getText());
 
 					MainFrame.createSeason();
 				} catch (Exception i) {
 					i.printStackTrace();
 				}
 
 			}
 
 		});
 	}
 
 	/**
 	 * Changes the other spinner value.
 	 * 
 	 * @param spn
 	 *            The JSpinner that called this function.
 	 */
 	private void changeSpinnerValue(JSpinner spn) {
 		programChange = true;
 		if (spn.equals(spnContestant))
 			spnWeek.setValue(Integer.parseInt(spnContestant.getValue()
 					.toString()) - 3);
 		else
 			spnContestant.setValue(Integer.parseInt(spnWeek.getValue()
 					.toString()) + 3);
 		programChange = false;
 	}
 
 	/**
 	 * Checks if the tribe names are valid according to specifications.
 	 * 
 	 * @return boolean depending if tribe names are alphanumber and between 1-30
 	 *         characters
 	 */
 	private boolean checkValidTribeName(String t) {
 		// regex for alphanumeric and between 1-30 characters long
 		return !(Utils.checkString(t, Person.TRIBE_PATTERN));
 	}
 
 }
