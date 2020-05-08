 package admin;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 
 import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
 
 /**
  * Screen for creating a season. Controls the number of contestants and weeks
  * and the two tribes name.
  * @author CS2212 GROUP TWO 2011/2012
  *
  */
 public class SeasonCreatePanel extends JPanel {
 
 	JSpinner spnWeek;
 	JSpinner spnContestant;
 	boolean programChange = false; //true when spinner value changed by program
 	
 	private FileWriter fileWrite = null; // I/O
 	private BufferedWriter buffWrite = null;
 
 	public SeasonCreatePanel(){
 		this.setPreferredSize(new Dimension(400,400));
 		this.setLayout(new GridLayout(6,2));
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 		
 		JLabel lblInitSeason = new JLabel("Create Season");
 		JLabel lblFiller = new JLabel("");
 		JLabel lblWeeks = new JLabel("Weeks:");
 		JLabel lblContestants = new JLabel("Contestants:");
 		JLabel lblTribe1 = new JLabel("Tribe 1:");
 		JLabel lblTribe2 = new JLabel("Tribe 2:");
 		
 		
 		//is there a default number of weeks?
 		SpinnerNumberModel weekModel = new SpinnerNumberModel(3,3,12,1); //default,low,min,step
 		spnWeek = new JSpinner(weekModel);
 		
 		SpinnerNumberModel contestantModel = new SpinnerNumberModel(6,6,15,1);//default,low,min,step
 		spnContestant = new JSpinner(contestantModel);
 		
 		
 		JTextField txtTribe1 = new JTextField("");
 		JTextField txtTribe2 = new JTextField("");
 		
 		JButton btnCreate = new JButton("Create Season");
 		
 		this.add(lblInitSeason);
 		this.add(lblFiller);
 		this.add(lblWeeks);
 		this.add(spnWeek);
 		this.add(lblContestants);
 		this.add(spnContestant);
 		this.add(lblTribe1);
 		this.add(txtTribe1);
 		this.add(lblTribe2);
 		this.add(txtTribe2);
 		this.add(btnCreate);
		
 		spnWeek.addChangeListener(new ChangeListener(){
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				JSpinner spn = (JSpinner) ce.getSource();
				if(!programChange)
					changeSpinnerValue(spn);	
				
 			}
 			
 		});
 		spnContestant.addChangeListener(new ChangeListener(){
 
 			@Override
 			public void stateChanged(ChangeEvent ce) {
 				JSpinner spn = (JSpinner) ce.getSource();
 				if(!programChange)
					changeSpinnerValue(spn);	
				
 			}
 			
 		});
 		
 		btnCreate.addActionListener(new ActionListener(){
 
 			//write the number of contestants and weeks to the file.
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				try {
 					
 					fileWrite = new FileWriter(
 							"src/data/SeasonSettings", false);
 					buffWrite = new BufferedWriter(fileWrite);
 					 String tempString = spnContestant.getValue().toString();
 					buffWrite.write("Number_Of_Contestants: " + tempString); // first line
 					buffWrite.newLine();
 					tempString = spnWeek.getValue().toString();
 					buffWrite.write("Number_Of_Weeks: " + tempString); // second line
 					buffWrite.newLine();
 					buffWrite.close(); // close the file
 				} catch (Exception i) {
 				}
 				
 			}
 			
 		});
 	}
 	
 	/**
 	 * Changes the other spinner value.
 	 * @param spn The JSpinner that called this function. 
 	 */
 	private void changeSpinnerValue(JSpinner spn){
 		programChange=true;
 		if(spn.equals(spnContestant))
 			spnWeek.setValue(Integer.parseInt(spnContestant.getValue().toString())-3);
 		else
 			spnContestant.setValue(Integer.parseInt(spnWeek.getValue().toString())+3);
 		programChange=false;
 	}
 
 }
 
