 package de.atlassoft.ui;
 
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 import de.atlassoft.model.Node;
 import de.atlassoft.model.ScheduleScheme;
 import de.atlassoft.model.SimulationStatistic;
 import de.atlassoft.model.TrainType;
 import de.atlassoft.util.I18NService;
 import de.atlassoft.util.I18NSingleton;
 import de.atlassoft.util.ImageHelper;
 
 /**
  * This class is for the SimulationStatistic Dialog where
  * you can see the Statistic after a Simulation.
  * 
  * @author Tobias Ilg
  */
 public class SimulationStatisticDialog {
 	private Shell shell;
 	private SimulationStatistic simulationStatistic;
 	
 	/**
 	 * Public constructor for the SimulationStatistic Dialog. Creates a new
 	 * SimulationStatistic shell and open it.
 	 * 
 	 * @param applicationService
 	 * 			to access application layer
 	 */
 	
 	public SimulationStatisticDialog (SimulationStatistic simulationStatistic) {
 		this.simulationStatistic = simulationStatistic;
 		I18NService I18N = I18NSingleton.getInstance();
 		
 		Display display = Display.getCurrent();
 		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		shell.setText(I18N.getMessage("SimulationStatisticDialog.Title"));
 		shell.setSize(SWT.DEFAULT, SWT.DEFAULT);
 		shell.setLayout(new GridLayout(1, true));
 		
 		initUI();
 		MainWindow.center(shell);
 		shell.open();
 	}
 	
 	/**
 	 * A method which creates the content, 
 	 * label, text, buttons and tool tips of
 	 * the shell of the SimulationStatistic Dialog.
 	 * 
 	 */
 	private void initUI() {
 		final I18NService I18N = I18NSingleton.getInstance();
 		Composite mainComposite = new Composite (shell, SWT.NULL);
 		mainComposite.setLayout (new GridLayout(3, false));
 		
 		// First row with three labels for "TotalDelay"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.TotalDelay") + "\t");
 		Label totalDelay = new Label(mainComposite, SWT.NONE);
 		totalDelay.setText(String.valueOf(simulationStatistic.getTotalDelay()));
 		toTheEnd(totalDelay);
 		new Label(mainComposite, SWT.NONE).setText("s");
 		
 		// Second row with three labels for "MeanDelay"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.MeanDelay") + "\t");
 		Label meanDelay = new Label(mainComposite, SWT.NONE);
 		meanDelay.setText(String.valueOf(simulationStatistic.getMeanDelay()));
 		toTheEnd(meanDelay);
 		new Label(mainComposite, SWT.NONE).setText("s");
 		
 		// Third row with three labels for "MeanDelay (TrainType)"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayTrainType") + "\t");
 	 	final List<TrainType> trainTypes = simulationStatistic.getInvolvedTrainTypes();
 	 	final String[] trainTypeSelection = new String [trainTypes.size()];
 		int index = 0;
 		for (TrainType type : trainTypes) {
 			trainTypeSelection[index] = type.getName();
 			index++;
 		}
 		final Combo comboTrainType = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    comboTrainType.setItems(trainTypeSelection);
 	    comboTrainType.select(0);
 		new Label(mainComposite, SWT.NONE);
 		
 		// Fourth row with the result of "MeanDelay (TrainType)"
 		new Label(mainComposite, SWT.NONE);
 		TrainType selectedTT = null;
 		for (TrainType type : trainTypes) {
 			if (type.getName().equals(comboTrainType.getText())) {
 				selectedTT = type;
 			}
 		}
 		final Label meanDelayTrainType = new Label(mainComposite, SWT.NONE);
 		meanDelayTrainType.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedTT)));
 		toTheEnd(meanDelayTrainType);
 		new Label(mainComposite, SWT.NONE).setText("s");
 		
 		// Listener for the combo TrainType
 		comboTrainType.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				TrainType selectedTrainType = null;
 				for (TrainType type : trainTypes) {
 					if (type.getName().equals(comboTrainType.getText())) {
 						selectedTrainType = type;
 					}
 				}
 				meanDelayTrainType.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedTrainType)));
 			}
 		});
 		
 		// Fifth row with "MeanDelay (Node)"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayNode") + "\t");
 	 	final java.util.List<Node> nodes = simulationStatistic.getInvolvedNodes();
 		final String[] nodeSelection = new String [nodes.size()];
 		index = 0;
 		for (Node node : nodes) {
 			nodeSelection[index] = node.getName();
 			index++;
 		}
 		final Combo comboNode = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 	    comboNode.setItems(nodeSelection);
 	    comboNode.select(0);
 		new Label(mainComposite, SWT.NONE);
 		
 		// Sixth row with the result of "MeanDelay (Node)"
 		new Label(mainComposite, SWT.NONE);
 		Node selectedN = null;
 		for (Node node : nodes) {
 			if (node.getName().equals(comboNode.getText())) {
 				selectedN = node;
 			}
 		}
 		final Label meanDelayNode = new Label(mainComposite, SWT.NONE);
 		meanDelayNode.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedN)));
 		toTheEnd(meanDelayNode);
 		new Label(mainComposite, SWT.NONE).setText("s");
 		
 		// Listener for the combo Node
 		comboNode.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Node selectedNode = null;
 				for (Node node : nodes) {
 					if (node.getName().equals(comboNode.getText())) {
 						selectedNode = node;
 					}
 				}
 				meanDelayNode.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedNode)));
 			}
 		});
 		
 		// Seventh row with "MeanDelay (ScheduleScheme)"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayScheduleScheme") + "\t");
 	 	final java.util.List<ScheduleScheme> scheduleSchemes = simulationStatistic.getInvolvedScheduleSchemes();
 		final String[] scheduleSchemeSelection = new String [scheduleSchemes.size()];
 		index = 0;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			scheduleSchemeSelection[index] = scheduleScheme.getID();
 			index++;
 		}
 		final Combo comboScheduleScheme = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		comboScheduleScheme.setItems(scheduleSchemeSelection);
 		comboScheduleScheme.select(0);
 		new Label(mainComposite, SWT.NONE);
 		
 		// Eighth row with the result of "MeanDelay (ScheduleScheme)"
 		new Label(mainComposite, SWT.NONE);
 		ScheduleScheme selectedSS = null;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			if (scheduleScheme.getID().equals(comboScheduleScheme.getText())) {
 				selectedSS = scheduleScheme;
 			}
 		}
 		Label meanDelayScheduleScheme = new Label(mainComposite, SWT.NONE);
 		meanDelayScheduleScheme.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedSS)));
 		toTheEnd(meanDelayScheduleScheme);
 		new Label(mainComposite, SWT.NONE).setText("s");
 		
 		// Listener for the combo ScheduleScheme
 		comboScheduleScheme.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				ScheduleScheme selectedScheduleScheme = null;
 				for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 					if (scheduleScheme.getID().equals(comboScheduleScheme.getText())) {
 						selectedScheduleScheme = scheduleScheme;
 					}
 				}
 				meanDelayTrainType.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedScheduleScheme)));
 			}
 		});
 		
 		// Ninth row with "MeanDelay (ScheduleScheme, Node)"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.MeanDelayScheduleSchemeAndNode") + "\t");
 		final Combo comboScheduleScheme2 = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		comboScheduleScheme2.setItems(scheduleSchemeSelection);
 		comboScheduleScheme2.select(0);
 		final Combo comboNode2 = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
 		ScheduleScheme selectedScheduleScheme2 = null;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 				selectedScheduleScheme2 = scheduleScheme;
 			}
 		}
 		
		if (selectedScheduleScheme2 != null) {
			java.util.List<Node> nodes2 = selectedScheduleScheme2.getStations();
			String[] node2Selection = new String [nodes2.size()];
			int index2 = 0;
			for (Node node : nodes2) {
				node2Selection[index2] = node.getName();
				index2++;
			}
			comboNode2.setItems(node2Selection);
			comboNode2.select(0);
 		}
 	    
 	    // Tenth row with the result of "MeanDelay (ScheduleScheme, Node)"
 		new Label(mainComposite, SWT.NONE);
 		ScheduleScheme selectedSS2 = null;
 		for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 			if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 				selectedSS2 = scheduleScheme;
 			}
 		}
 		Node selectedN2 = null;
 		for (Node node : nodes) {
 			if (node.getName().equals(comboNode2.getText())) {
 				selectedN2 = node;
 			}
 		}
 		final Label meanDelayScheduleSchemeAndNode = new Label(mainComposite, SWT.NONE);
 		meanDelayScheduleSchemeAndNode.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedSS2, selectedN2)));
 		toTheEnd(meanDelayScheduleSchemeAndNode);
 		new Label(mainComposite, SWT.NONE).setText("s");
 		
 		// Listener for the combo scheduleScheme and Node
 		comboScheduleScheme2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				ScheduleScheme selectedScheduleScheme2 = null;
 				for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 					if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 						selectedScheduleScheme2 = scheduleScheme;
 					}
 				}
 				
 				java.util.List<Node> nodes2 = selectedScheduleScheme2.getStations();
 				String[] node2Selection = new String [nodes2.size()];
 				int index = 0;
 				for (Node node : nodes2) {
 					node2Selection[index] = node.getName();
 					index++;
 				}
 				comboNode2.setItems(node2Selection);
 				comboNode2.select(0);
 				
 				Node selectedNode2 = null;
 				for (Node node : nodes) {
 					if (node.getName().equals(comboNode2.getText())) {
 						selectedNode2 = node;
 					}
 				}
 				meanDelayScheduleSchemeAndNode.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedScheduleScheme2, selectedNode2)));
 			}
 		});
 		
 		comboNode2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				ScheduleScheme selectedScheduleScheme2 = null;
 				for (ScheduleScheme scheduleScheme : scheduleSchemes) {
 					if (scheduleScheme.getID().equals(comboScheduleScheme2.getText())) {
 						selectedScheduleScheme2 = scheduleScheme;
 					}
 				}
 				Node selectedNode2 = null;
 				for (Node node : nodes) {
 					if (node.getName().equals(comboNode2.getText())) {
 						selectedNode2 = node;
 					}
 				}
 				meanDelayScheduleSchemeAndNode.setText(String.valueOf(simulationStatistic.getMeanDelay(selectedScheduleScheme2, selectedNode2)));
 			}
 		});
 		
 		// Eleventh row with "NumberOfRides"
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.NumberOfRides") + "\t");
 		Label meanDelayRides = new Label(mainComposite, SWT.NONE);
 		meanDelayRides.setText(String.valueOf(simulationStatistic.getNumberOfRides()));
 		toTheEnd(meanDelayRides);
 		new Label(mainComposite, SWT.NONE).setText(I18N.getMessage("SimulationStatisticDialog.Rides"));
 		
 		// Twelfth row with Button "Save as PDF"
 		Button save = new Button(shell, SWT.PUSH);
 		save.setImage(ImageHelper.getImage("loadButton"));
 		save.setText(I18N.getMessage("SimulationStatisticDialog.Save"));
 		GridData dataSave = new GridData(GridData.FILL);
 		dataSave.horizontalAlignment = GridData.CENTER;
 	    save.setLayoutData(dataSave);
 		
 		// Function of Save-Button
 		save.addSelectionListener(new SelectionAdapter() {
 	        public void widgetSelected(SelectionEvent e) {
 	       		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
 	       		messageBox.setText(I18N.getMessage("SimulationStatisticDialog.informationSaved"));
         	    messageBox.setMessage(I18N.getMessage("SimulationStatisticDialog.informationStatisticSaved"));
         	    shell.setVisible(false);
         	    messageBox.open();
 	            shell.close();
 	        }
 	    });
 		
 		shell.pack();
 	}
 	
 	private void toTheEnd(Label label) {
 		GridData dataEnd = new GridData(GridData.FILL);
 		dataEnd.horizontalAlignment = GridData.END;
 		label.setLayoutData(dataEnd);
 	}
 }
