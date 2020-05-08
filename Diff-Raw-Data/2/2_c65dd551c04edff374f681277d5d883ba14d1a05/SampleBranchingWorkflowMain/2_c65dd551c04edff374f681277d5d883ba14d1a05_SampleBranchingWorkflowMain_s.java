 package com.fairyteller.swing;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 
 import com.fairyteller.swing.concurrent.workflow.DefaultSwingUiHandler;
 import com.fairyteller.utilities.concurrent.workflow.AbstractWorkflowRunnable;
 import com.fairyteller.utilities.concurrent.workflow.DefaultBranchingWorkflowElement;
 import com.fairyteller.utilities.concurrent.workflow.MainRunnable;
 import com.fairyteller.utilities.concurrent.workflow.UIHandler;
 import com.fairyteller.utilities.concurrent.workflow.UIRunnable;
 import com.fairyteller.utilities.concurrent.workflow.Workflow;
 import com.fairyteller.utilities.concurrent.workflow.WorkflowFactory;
 import com.fairyteller.utilities.concurrent.workflow.WorkflowService;
 
 public class SampleBranchingWorkflowMain {
 
 	public static void main(String[] args) {
 		Map<String, Object> context = new HashMap<String, Object>();
 		context.put("counter", 0);
 
 		final Object[] arguments = new Object[1];
 		arguments[0] = context;
 		
 		final JPanel myComponent = new JPanel(new FlowLayout());
 
 		FrameHelper.createMainFrame("test Workflow", myComponent, true);
 
 		UIRunnable firstStartUiRunnable = new AbstractWorkflowRunnable() {
 			@Override
 			public void run() {
 				myComponent.add(new JLabel("First task started..."));
 				myComponent.revalidate();
 			}
 		};
 
 		MainRunnable firstMainRunnable = new AbstractWorkflowRunnable() {
 
 			@Override
 			public void run() {
 				try {
 					int counter = ((Integer) ((Map<String, Object>)getArguments()[0]).get("counter"));
 					setProceed(true);
 					//Thread.sleep(1000);
 					setSuccess(counter % 2 == 0);
 					((Map<String, Object>)getArguments()[0]).put("counter", counter+1);
 					((Map<String, Object>)getArguments()[0]).put("date", Calendar.getInstance().getTime());
 				} catch (Exception e) {
 					setProceed(false);
 				}
 			}
 		};
 
 		UIRunnable firstFinishUiRunnable = new AbstractWorkflowRunnable() {
 
 			@Override
 			public void run() {
 				int counter = ((Integer) ((Map<String, Object>)getArguments()[0]).get("counter"));
 				Date date = ((Date) ((Map<String, Object>)getArguments()[0]).get("date"));
 				if (counter % 2 == 0)
 					myComponent.add(new JLabel(Calendar.getInstance().getTime().toString()+": First task completed! ("
 							+ counter + ")"));
 				else
 					myComponent.add(new JLabel(Calendar.getInstance().getTime().toString()+": First task completed again! ("
 							+ counter + ")"));
 				
 				myComponent.add(new JLabel(date.toString()+": current date in the arguments"));
 				myComponent.revalidate();
 			}
 		};
 
 		UIRunnable secondStartUiRunnable = new AbstractWorkflowRunnable() {
 
 			@Override
 			public void run() {
 				Date date = ((Date) ((Map<String, Object>)getArguments()[0]).get("date"));
 				myComponent.add(new JLabel(Calendar.getInstance().getTime().toString()+": Second task started..."));
 				myComponent.add(new JLabel(date.toString()+": current date in the arguments"));
 				myComponent.revalidate();
 			}
 		};
 
 		MainRunnable secondMainRunnable = new AbstractWorkflowRunnable() {
 			@Override
 			public void run() {
 				try {
 					setProceed(true);
 					setSuccess(true);
 				} catch (Exception e) {
 					setProceed(false);
 				}
 
 			}
 		};
 
 		UIRunnable secondFinishUIRunnable = new AbstractWorkflowRunnable() {
 
 			@Override
 			public void run() {
 				if (isSuccess()) {
 					myComponent.add(new JLabel("Second task completed!"));
 				} else {
 					myComponent.add(new JLabel("Second task failed!"));
 				}
 				myComponent.revalidate();
 			}
 		};
 
 		UIRunnable timeoutUiRunnable = new AbstractWorkflowRunnable() {
 
 			@Override
 			public void run() {
 				myComponent.add(new JLabel("Timeout :("));
 				myComponent.revalidate();
 			}
 		};
 
 		final UIHandler handler = new DefaultSwingUiHandler();
 
 		DefaultBranchingWorkflowElement[] welements = new DefaultBranchingWorkflowElement[] {
 				new DefaultBranchingWorkflowElement(0, "first", firstStartUiRunnable,
 						firstMainRunnable, firstFinishUiRunnable, 3000,
 						handler, timeoutUiRunnable),
 				new DefaultBranchingWorkflowElement(1, "second", secondStartUiRunnable,
 						secondMainRunnable, secondFinishUIRunnable, 3000,
 						handler, timeoutUiRunnable) };
 
 		welements[0].setNextStateYes(0);
 		welements[0].setNextStateNo(1);
 		welements[1].setStateFinal(true);
 		
 		
 		final Workflow workflow = WorkflowFactory
 				.createBranchingWorkflow(welements);
 
 		final WorkflowService service = new WorkflowService();
 
 		Action processWorkflowAction = new AbstractAction(
 				"Process the workflow") {
 
 			private static final long serialVersionUID = 3949757251884123525L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				this.setEnabled(false);
 				new SwingWorker<String, Object>() {
 					@Override
 					protected String doInBackground() throws Exception {
 						service.start(workflow, arguments);
 						return "done";
 					}
 
 					@Override
 					protected void done() {
 						try {
 							get();
 							setEnabled(true);
 						} catch (InterruptedException e) {
 							JOptionPane.showMessageDialog(myComponent,
									"problme!");
 						} catch (ExecutionException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 					}
 				}.execute();
 			}
 		};
 
 		myComponent.add(new JButton(processWorkflowAction));
 
 		myComponent.revalidate();
 	}
 }
