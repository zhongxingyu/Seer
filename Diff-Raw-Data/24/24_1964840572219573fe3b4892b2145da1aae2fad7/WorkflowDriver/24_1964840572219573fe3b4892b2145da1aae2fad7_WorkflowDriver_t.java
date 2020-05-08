 /**
 Copyright (c) 2012, University of Texas at El Paso
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
 Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation 
 and/or other materials provided with the distribution.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 DAMAGE.
  */
 
 package edu.utep.cybershare.DerivA;
 
 import java.awt.Cursor;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFactory;
 
 import edu.utep.cybershare.DerivA.util.ServerCredentials;
 import edu.utep.cybershare.DerivAUI.DerivAUI;
 import edu.utep.cybershare.DerivAUI.components.AgentComboBox;
 import edu.utep.cybershare.DerivAUI.components.IndividualComboBox.Individual;
 import edu.utep.cybershare.DerivAUI.components.IndividualComboBox;
 import edu.utep.cybershare.DerivAUI.components.IndividualList;
 import edu.utep.cybershare.DerivAUI.components.InferenceRulesComboBox;
 import edu.utep.cybershare.DerivAUI.components.PMLJComboBox;
 import edu.utep.cybershare.DerivAUI.components.PMLJList;
 import edu.utep.cybershare.DerivAUI.components.formatComboBox;
 import edu.utep.cybershare.DerivAUI.components.typeComboBox;
 
 public class WorkflowDriver extends javax.swing.JFrame {
 
 	private DerivAUI instance;
 	private String workflow, conclusion;
 	private DerivationMaker DM;
 	private ServerCredentials creds;
 
 	// Variables declaration - do not modify                     
 	private javax.swing.JButton addAntecedentButton;
 	private javax.swing.JButton browseButton;
 	private javax.swing.JTextField browseTextField;
 	private javax.swing.JLabel conclusionFormatLabel;
 	private javax.swing.JLabel conclusionTypeLabel;
 	private javax.swing.JLabel conclusionTypesFound;
 	private javax.swing.JLabel inferenceAgentLabel;
 	private javax.swing.JLabel inferenceAgentsFound;
 	private javax.swing.JLabel inferenceRuleLabel;
 	private javax.swing.JLabel inferenceRulesFound;
 	private javax.swing.JScrollPane jScrollPane1;
 	private javax.swing.JScrollPane jScrollPane2;
 	private javax.swing.JButton procedeButton;
 	private javax.swing.JButton removeAntecedentButton;
 	private javax.swing.JButton returnButton;
 	private javax.swing.JLabel selectedWorkflowLabel;
 	private javax.swing.JLabel text1;
 	private javax.swing.JLabel text2;
 	private javax.swing.JLabel text3;
 	private javax.swing.JLabel text4;
 	private javax.swing.JLabel text5;
 	private javax.swing.JLabel text6;
 
 	private JFileChooser fc;
 	private File oFile;
 
 	//Cold Start Variables
 	private javax.swing.JLabel coldStartLabel;
 	private javax.swing.JButton selectConclusionCS;
 	private javax.swing.JButton stopColdStart;
 
 	private PMLJComboBox availConclusionsComboBox;
 	private IndividualList availableAntecedentsList;
 	private IndividualList selectedAntecedentsList;
 	private formatComboBox conclusionFormatComboBox;
 	private typeComboBox conclusionTypeComboBox;
 	private AgentComboBox inferenceAgentComboBox;
 	private InferenceRulesComboBox inferenceRuleComboBox;
 
 	private Vector<Individual> inferenceRule, inferenceEngine, conclusionType, nextConclusionType;
 	private Vector<IndividualList.Individual> CurrentlySelectedAntecedentsVector, AvailAntecedentsVector; 
 
 	public WorkflowDriver(DerivAUI ui, ServerCredentials sc, String wf, String conc){
 		creds = sc;
 		conclusion = conc;
 		instance = ui;
 		workflow = wf;
 
 		if(conclusion != null){
 			initComponents();
 		}else{
 			coldStartInit();
 		}
 	}
 
 	private void initComponents() {
 
 		text1 = new javax.swing.JLabel();
 		text2 = new javax.swing.JLabel();
 		text3 = new javax.swing.JLabel();
 		text4 = new javax.swing.JLabel();
 		text5 = new javax.swing.JLabel();
 		text6 = new javax.swing.JLabel();
 		inferenceAgentsFound = new javax.swing.JLabel();
 		inferenceRulesFound = new javax.swing.JLabel();
 		conclusionTypesFound = new javax.swing.JLabel();
 		selectedWorkflowLabel = new javax.swing.JLabel();
 		inferenceAgentLabel = new javax.swing.JLabel();
 		inferenceRuleLabel = new javax.swing.JLabel();
 		conclusionTypeLabel = new javax.swing.JLabel();
 		conclusionFormatLabel = new javax.swing.JLabel();
 
 		jScrollPane1 = new javax.swing.JScrollPane();
 		jScrollPane2 = new javax.swing.JScrollPane();
 		browseTextField = new javax.swing.JTextField();
 		addAntecedentButton = new javax.swing.JButton();
 		removeAntecedentButton = new javax.swing.JButton();
 		browseButton = new javax.swing.JButton();
 		procedeButton = new javax.swing.JButton();
 		returnButton = new javax.swing.JButton();
 
 		inferenceRuleComboBox = new InferenceRulesComboBox();
 		inferenceAgentComboBox = new AgentComboBox();
 		conclusionTypeComboBox = new typeComboBox();
 		conclusionFormatComboBox = new formatComboBox();
 
 		availableAntecedentsList = new IndividualList();
 		selectedAntecedentsList = new IndividualList();
 
 		initQueryComponents();
 
 		if(coldStartLabel != null)
 			this.remove(coldStartLabel);
 		if(selectConclusionCS != null)
 			this.remove(selectConclusionCS);
 		if(stopColdStart != null)
 			this.remove(stopColdStart);
 		if(availConclusionsComboBox != null)
 			this.remove(availConclusionsComboBox);
 
 		setTitle("derivA - Workflow Driver");
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
 		text1.setFont(new java.awt.Font("Tahoma", 1, 11));
 		text1.setText("According to the selected workflow: ");
 
 		selectedWorkflowLabel.setText(workflow);
 
 		text2.setFont(new java.awt.Font("Tahoma", 1, 11));
 		text2.setText("With the following antecedent(s)");
 
 		jScrollPane1.setViewportView(availableAntecedentsList);
 		jScrollPane2.setViewportView(selectedAntecedentsList);
 
 		addAntecedentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("../DerivAUI/images/001_01.gif"))); // NOI18N
 		addAntecedentButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				addAntecedentAction(evt);
 			}
 		});
 
 		removeAntecedentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("../DerivAUI/images/001_02.gif"))); // NOI18N
 		removeAntecedentButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				removeAntecedentAction(evt);
 			}
 		});
 
 		text3.setFont(new java.awt.Font("Tahoma", 1, 11));
 		text3.setText("Using");
 
 		inferenceAgentLabel.setText("Inference Agent: ");
 
 		int IACount = inferenceAgentComboBox.getItemCount();
 		inferenceAgentsFound.setText("Found " + IACount);
 
 		text4.setFont(new java.awt.Font("Tahoma", 1, 11));
 		text4.setText("Executing");
 
 		inferenceRuleLabel.setText("Inference Rule:");
 
 		int IRCount = inferenceRuleComboBox.getItemCount();
 		inferenceRulesFound.setText("Found " + IRCount);
 
 		text5.setFont(new java.awt.Font("Tahoma", 1, 11));
 		text5.setText("The following artifact is created:");
 
 		conclusionTypeLabel.setText("Artifact Type:");
 
 		conclusionFormatLabel.setText("Artifact Format:");
 
 		text6.setFont(new java.awt.Font("Tahoma", 1, 11));
 		text6.setText("Browse Artifact:");
 
 		browseButton.setText("Browse");
 		browseButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				browseAction(evt);
 			}
 		});
 
 		int TypeCount = conclusionTypeComboBox.getItemCount();
 		conclusionTypesFound.setText("Found " + TypeCount);
 
 		procedeButton.setText("Procede");
 		procedeButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				submitAction(evt);
 			}
 		});
 
 		returnButton.setText("Return to DerivA");
 		returnButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				cancelAction(evt);
 			}
 		});
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addGroup(layout.createSequentialGroup()
 										.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addGap(18, 18, 18)
 										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 												.addComponent(addAntecedentButton)
 												.addComponent(removeAntecedentButton))
 												.addGap(14, 14, 14)
 												.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
 												.addGroup(layout.createSequentialGroup()
 														.addComponent(inferenceAgentLabel)
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 														.addComponent(inferenceAgentComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 														.addComponent(inferenceAgentsFound))
 														.addGroup(layout.createSequentialGroup()
 																.addComponent(inferenceRuleLabel)
 																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																.addComponent(inferenceRuleComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																.addComponent(inferenceRulesFound))
 																.addGroup(layout.createSequentialGroup()
 																		.addComponent(browseButton)
 																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																		.addComponent(browseTextField))
 																		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 																				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 																						.addGroup(layout.createSequentialGroup()
 																								.addComponent(conclusionFormatLabel)
 																								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																								.addComponent(conclusionFormatComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 																								.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
 																										.addComponent(conclusionTypeLabel)
 																										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																										.addComponent(conclusionTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
 																										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																										.addComponent(conclusionTypesFound))
 																										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 																												.addGap(0, 0, Short.MAX_VALUE)
 																												.addComponent(returnButton)
 																												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																												.addComponent(procedeButton))
 																												.addGroup(layout.createSequentialGroup()
 																														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																																.addComponent(text1)
 																																.addComponent(text2)
 																																.addComponent(text3)
 																																.addComponent(text4)
 																																.addComponent(text5)
 																																.addComponent(text6)
 																																.addComponent(selectedWorkflowLabel))
 																																.addGap(0, 0, Short.MAX_VALUE)))
 																																.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(text1)
 						.addGap(7, 7, 7)
 						.addComponent(selectedWorkflowLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(text2)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addGroup(layout.createSequentialGroup()
 										.addComponent(addAntecedentButton)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 										.addComponent(removeAntecedentButton))
 										.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 										.addGap(18, 18, 18)
 										.addComponent(text3)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(inferenceAgentLabel)
 												.addComponent(inferenceAgentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addComponent(inferenceAgentsFound))
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 												.addComponent(text4)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 														.addComponent(inferenceRuleLabel)
 														.addComponent(inferenceRuleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 														.addComponent(inferenceRulesFound))
 														.addGap(18, 18, 18)
 														.addComponent(text5)
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																.addComponent(conclusionTypeLabel)
 																.addComponent(conclusionTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 																.addComponent(conclusionTypesFound))
 																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																		.addComponent(conclusionFormatLabel)
 																		.addComponent(conclusionFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																		.addComponent(text6)
 																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																				.addComponent(browseButton)
 																				.addComponent(browseTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																						.addComponent(procedeButton)
 																						.addComponent(returnButton))
 																						.addContainerGap())
 				);
 
 		pack();
 		setLocationRelativeTo(instance);
 	}
 
 	private void initQueryComponents(){
 
 		System.out.println("conclusion: " + conclusion);
 
 
 		//Get conclusion Type.
 		String ConcType = getConclusionTypeFromNode(conclusion);
 
 		System.out.println("conclusion Type: " + ConcType);
 		//Get Formats, IR, IE, Types for the next derivation
 		conclusionFormatComboBox.queryFormats();
 		
 		ArrayList<String> IR = inferenceRuleComboBox.queryInferenceRulesByWorkflow(ConcType, workflow);
 		ArrayList<String> antTypes = new ArrayList<String>();
 		
 		for(Iterator<String> iter = IR.iterator(); iter.hasNext();){
 			
 			String sIR = iter.next();
 			inferenceAgentComboBox.queryInfernceEngineByWorkflow(sIR);
 			conclusionTypeComboBox.queryNextInformationTypesByWorkflow(sIR, workflow);
 			antTypes = conclusionTypeComboBox.getAllAntecedentTypesFromInferenceRule(sIR, workflow);
 		}
 		
 		PMLJList ants = new PMLJList();
 
 		for(Iterator<String> iter = antTypes.iterator(); iter.hasNext();){
			System.out.println("# of types: " + antTypes.size());
 			String temp = iter.next();
 			
 			if(ants.queryAntecedentsByWorkflow(temp) == 0){
 				Object[] options = {"Make Assertion","Procede"};
 				temp = temp.substring(temp.lastIndexOf('/'));
 				int n = JOptionPane.showOptionDialog(this,
 						"An antecedent of type: " + temp + " is missing, Do you wish to create it first?",
 						"derivA - Workflow Driver",
 						JOptionPane.YES_NO_OPTION,
 						JOptionPane.QUESTION_MESSAGE,
 						null,
 						options,
 						options[0]);
 
 				if(n == 0){
 					dispose();
 				}else{
 					System.out.println("Do Something Else");
 				}
 			}
 		}
 		
 
 		AvailAntecedentsVector = ants.getPMLList();
 		availableAntecedentsList.setModel(AvailAntecedentsVector);
 		availableAntecedentsList.repaint();
 
 
 	}
 
 	private void coldStartInit(){
 
 		System.out.println("Cold Start");
 
 		selectConclusionCS = new javax.swing.JButton();
 		stopColdStart = new javax.swing.JButton();
 		coldStartLabel = new javax.swing.JLabel();
 		availConclusionsComboBox = new PMLJComboBox();
 		availConclusionsComboBox.queryPMLJ(workflow);
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 		setTitle("Select Conclusion Start Point");
 
 		selectConclusionCS.setText("Select Conclusion");
 		selectConclusionCS.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				submitColdStartAction(evt);
 			}
 		});
 
 		stopColdStart.setText("Make New Assertion");
 		stopColdStart.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				cancelAction(evt);
 			}
 		});
 
 		coldStartLabel.setText("Select Conclusion Start Point:");
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addGroup(layout.createSequentialGroup()
 										.addComponent(coldStartLabel)
 										.addGap(0, 0, Short.MAX_VALUE))
 										.addGroup(layout.createSequentialGroup()
 												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 														.addComponent(availConclusionsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 														.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 																.addGap(0, 71, Short.MAX_VALUE)
 																.addComponent(stopColdStart)
 																.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																.addComponent(selectConclusionCS)))
 																.addContainerGap())))
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(coldStartLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(availConclusionsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 								.addComponent(selectConclusionCS)
 								.addComponent(stopColdStart))
 								.addContainerGap())
 				);
 
 		pack();
 		setLocationRelativeTo(instance);
 		setVisible(true);
 	}
 
 	public void submitColdStartAction(java.awt.event.ActionEvent evt){
 
 		IndividualComboBox.Individual wf = (IndividualComboBox.Individual) availConclusionsComboBox.getSelectedItem();
 		conclusion = wf.getURI();
 		
 		System.out.println("Conclusion: " + conclusion);
 		
 		initComponents();
 
 	}
 
 	public void submitAction(java.awt.event.ActionEvent evt){
 
 
 		IndividualComboBox.Individual formatURI = (IndividualComboBox.Individual) conclusionFormatComboBox.getSelectedItem();
 		IndividualComboBox.Individual TypeURI = (IndividualComboBox.Individual) conclusionTypeComboBox.getSelectedItem();
 		IndividualComboBox.Individual InferenceAgentURI = (IndividualComboBox.Individual) inferenceAgentComboBox.getSelectedItem();
 		IndividualComboBox.Individual InferenceRuleURI = (IndividualComboBox.Individual) inferenceRuleComboBox.getSelectedItem();
 
 		setVisible(false);
 
 		instance.derivateArtifact(browseTextField.getText(), formatURI.getURI(),TypeURI.getURI(),InferenceAgentURI.getURI(),InferenceRuleURI.getURI(),CurrentlySelectedAntecedentsVector);
 
 		dispose();
 	}
 
 	public void cancelAction(java.awt.event.ActionEvent evt){
 		dispose();
 	}
 
 	public void addAntecedentAction(java.awt.event.ActionEvent evt){
 		try{
 			Object CSA = availableAntecedentsList.getSelectedValue();
 			if(CurrentlySelectedAntecedentsVector != null){
 				if(!CurrentlySelectedAntecedentsVector.contains(CSA)){
 					CurrentlySelectedAntecedentsVector.add((IndividualList.Individual) CSA);
 				}
 			}else{
 				CurrentlySelectedAntecedentsVector = new Vector<IndividualList.Individual>();
 				CurrentlySelectedAntecedentsVector.add((IndividualList.Individual) CSA);
 			}
 
 			selectedAntecedentsList.setModel(CurrentlySelectedAntecedentsVector);
 			selectedAntecedentsList.repaint();
 		}catch (Exception e){
 			JOptionPane.showMessageDialog(this, "Select Antecedent to Add", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	public void removeAntecedentAction(java.awt.event.ActionEvent evt){
 		try{
 		Object CSA = selectedAntecedentsList.getSelectedValue();
 		if(CurrentlySelectedAntecedentsVector != null){
 			if(CurrentlySelectedAntecedentsVector.contains(CSA)){
 				CurrentlySelectedAntecedentsVector.remove(CSA);
 			}
 		}
 
 		selectedAntecedentsList.setModel(CurrentlySelectedAntecedentsVector);
 		selectedAntecedentsList.repaint();
 		}catch (Exception e){
 			JOptionPane.showMessageDialog(this, "Select Antecedent to Remove", "Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public void browseAction(java.awt.event.ActionEvent evt){
 		//Create a file chooser
 		fc = new JFileChooser(browseTextField.getText());
 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 		int returnVal = fc.showOpenDialog(WorkflowDriver.this);
 
 		String file = null;
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = fc.getSelectedFile().getAbsolutePath();
 			browseTextField.setText(file);
 		}
 
 		browseTextField.setText(file);
 		oFile = fc.getSelectedFile();
 	}
 
 	///////////////////////
 	//// QUERY METHODS ////
 	///////////////////////
 
 	public String getConclusionTypeFromNode(String conclusion){
 
 		String getConclusionTypeQuery =  "SELECT DISTINCT ?conclusionType " +
 				"WHERE { " +
 				"<" + conclusion + "> a <http://inference-web.org/2.0/pml-justification.owl#NodeSet> . " +
 				"<" + conclusion + "> <http://inference-web.org/2.0/pml-justification.owl#hasConclusion> ?conclusion . " +
 				"?conclusion a ?conclusionType . " +
 				"}";
 
 		edu.utep.trust.provenance.RDFStore_Service service = new edu.utep.trust.provenance.RDFStore_Service();
 		edu.utep.trust.provenance.RDFStore proxy = service.getRDFStoreHttpPort();
 		
 		String qResult = proxy.doQuery(getConclusionTypeQuery);
 		ResultSet rSet = ResultSetFactory.fromXML(qResult);
 
 		System.out.println(qResult);
 
 		String conclusionTypeURI = null, conclusionTypeLabel;
 
 		if(rSet != null)
 			while(rSet.hasNext()){
 				conclusionTypeURI = rSet.nextSolution().get("conclusionType").toString();
 				if(!conclusionTypeURI.endsWith("#Information"))
 					break;
 			}
 
 		return conclusionTypeURI;
 	}
 
 }
 
