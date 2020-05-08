 package gui;
 import gui.steps.PersonDescriptionStep;
 import gui.steps.TimeRangeStep;
 import gui.steps.ViewModelConnection;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Stack;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import javax.swing.WindowConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 
 import model.Model;
 import model.steps.InformationGatherStepModel;
 
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class MainFrame extends javax.swing.JFrame implements ActionListener {
 	private JPanel ctrlPnl;
 	private JPanel mainPnl, errorMsg;
 	private JButton backBtn;
 	private JButton nextBtn;
 	private JLabel msg;
 	
 	private Model model;
 	private ViewModelConnection currentViewStepConnection;
 	private Stack<ViewModelConnection> stepHistory;
 	
 
 	/**
 	* Auto-generated main method to display this JFrame
 	*/
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				MainFrame inst = new MainFrame();
 				inst.setLocationRelativeTo(null);
 				inst.setVisible(true);
 			}
 		});
 	}
 	
 	public MainFrame() {
 		super();
 		model = new Model();
 		stepHistory = new Stack<ViewModelConnection>();
 		initGUI();
 	}
 	
 	public void loadInitianalStep()
 	{
 		currentViewStepConnection = new PersonDescriptionStep();
 		mainPnl.add(currentViewStepConnection.getVisualisationUI(), BorderLayout.CENTER);
 	}
 	
 	public void lastStep()
 	{
 		cleanError();
 		
 		if(!stepHistory.empty())
 		{
 			ViewModelConnection lastStep = stepHistory.pop();
 			mainPnl.remove(currentViewStepConnection.getVisualisationUI());
 			currentViewStepConnection = lastStep;
 			mainPnl.add(currentViewStepConnection.getVisualisationUI());
 			mainPnl.revalidate();
 			mainPnl.repaint();
 		} else {
 			backBtn.setEnabled(false);
 		}
 	}
 	
 	public void nextStep() 
 	{
 		cleanError();
 		currentViewStepConnection.fillModel();
 		InformationGatherStepModel stepModel = currentViewStepConnection.getModel();
 		String error = stepModel.getError();
 		
 		if(error == null)
 		{
 			updateBreadcrubs(model.getInformationGatherTrace());
 			
			InformationGatherStepModel 	nextStepModel = model.getNextStep();
 			ViewModelConnection 		nextViewModelConnection = nextStepModel.getViewModelConnection();
 			JComponent 					nextJComponent = nextViewModelConnection.getVisualisationUI();
 			
 			nextStepModel.setPreselectionIfItWasntAlredySet();
 			nextViewModelConnection.fillMask();
 			
 			stepHistory.push(currentViewStepConnection);
 			
 			backBtn.setEnabled(true);
 					
 			mainPnl.remove(currentViewStepConnection.getVisualisationUI());
 			mainPnl.add(nextJComponent);
 			
 			currentViewStepConnection = nextViewModelConnection;
 			mainPnl.revalidate();
 			mainPnl.repaint();
 		}
 		else
 		{
 			displayError(error); 
 		}
 	}
 	
 	private void cleanError()
 	{
 		msg.setText("");
 	}
 	
 	private void displayError(String error) 
 	{
 		msg.setText(error);
 	}
 	
 	private void updateBreadcrubs(InformationGatherStepModel[] stepModels)
 	{
 		
 	}
 	
 	private void initGUI() {
 		try {
 			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			BorderLayout thisLayout = new BorderLayout();
 			getContentPane().setLayout(thisLayout);
 			{
 				ctrlPnl = new JPanel();
 				GridBagLayout ctrlPnlLayout = new GridBagLayout();
 				getContentPane().add(ctrlPnl, BorderLayout.SOUTH);
 				ctrlPnl.setPreferredSize(new java.awt.Dimension(645, 48));
 				ctrlPnl.setLayout(ctrlPnlLayout);
 				ctrlPnlLayout.rowWeights = new double[] {0.1};
 				ctrlPnlLayout.rowHeights = new int[] {7};
 				ctrlPnlLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0};
 				ctrlPnlLayout.columnWidths = new int[] {0, 0, 0, 0, -73};
 				{
 					nextBtn = new JButton();
 					ctrlPnl.add(nextBtn, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
 					nextBtn.setText("Weiter");
 					nextBtn.setBounds(566, 12, 58, 28);
 					nextBtn.setActionCommand("next");
 					nextBtn.addActionListener(this);
 				}
 				{
 					backBtn = new JButton();
 					ctrlPnl.add(backBtn, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
 					backBtn.setText("Zurck");
 					backBtn.setBounds(485, 12, 58, 28);
 					backBtn.setActionCommand("back");
 					backBtn.addActionListener(this);
 					backBtn.setEnabled(false);
 				}
 			}
 			{
 				mainPnl = new JPanel();
 				mainPnl.setPreferredSize(new Dimension(691, 416));
 				mainPnl.setBorder(new EmptyBorder(10, 10, 10, 10));
 				
 				loadInitianalStep();
 				
 				getContentPane().add(mainPnl, BorderLayout.CENTER);		
 				
 				errorMsg = new JPanel();
 				getContentPane().add(errorMsg, BorderLayout.NORTH);
 				msg = new JLabel();
 				msg.setForeground(Color.red);
 				errorMsg.add(msg);
 			}
 			pack();
 			this.setSize(750, 650);
 			
 		} catch (Exception e) {
 		    //add your error handling code here
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent actionEvent) 
 	{
 		if(actionEvent.getActionCommand().equals("next"))
 		{
 			nextStep();
 		}
 		else if(actionEvent.getActionCommand().equals("back"))
 		{
 			lastStep();
 		}
 	}
 
 
 }
