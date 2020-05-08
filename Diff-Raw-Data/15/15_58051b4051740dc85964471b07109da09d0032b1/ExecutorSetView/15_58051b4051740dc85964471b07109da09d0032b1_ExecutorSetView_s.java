 package plural.swing;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.LayoutManager;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.WindowConstants;
 
 import eventful.EventfulListener;
 
 import plural.executor.ExecutorSet;
 import plural.executor.ExecutorState;
 import plural.executor.PluralExecutor;
 import swidget.icons.StockIcon;
 import swidget.widgets.ImageButton;
 import swidget.widgets.Spacing;
 
 public class ExecutorSetView extends JDialog {
 
 	ExecutorSet<?> executors;
 	private JProgressBar progress;
 	
 	public ExecutorSetView(JFrame owner, ExecutorSet<?> _tasks){
 		
 		super(owner, "Working...", true);
 		this.executors = _tasks;
 		init(owner);
 	}
 	
 	public ExecutorSetView(JDialog owner, ExecutorSet<?> _tasks){
 		
 		super(owner, "Working...", true);
 		this.executors = _tasks;
 		init(owner);
 	}
 		
 	private void init(Window owner)
 	{
 	
 		setResizable(false);
 		
 		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		
 		
 		
         JPanel panel = new JPanel();
         getContentPane().add(panel);
 
 		
         LayoutManager layout = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         panel.setLayout(layout);
 		
         c.gridx = 0;
         c.gridy = 0;
        c.weighty = 1.0;
         c.weighty = 0.0;
         c.anchor = GridBagConstraints.FIRST_LINE_START;
         
         JLabel title = new JLabel(executors.getDescription());
         title.setFont(title.getFont().deriveFont(Font.BOLD).deriveFont(title.getFont().getSize() + 2f));
         title.setBorder(Spacing.bMedium());
         panel.add(title, c);
         
 
 		ExecutorView view;
 		for (PluralExecutor pl : executors){
 			
 			c.gridy += 1;
 			
 			view = new ExecutorView(pl);
 			panel.add(view, c);
 			
 		}
 
         
 		c.gridy += 1;
 		c.weighty = 1.0;
 		
 		progress = new JProgressBar();
 		progress.setMaximum(100);
 		progress.setMinimum(0);
 		progress.setValue(0);
 		JPanel progressPanel = new JPanel();
 		progressPanel.add(progress);
 		progressPanel.setBorder(Spacing.bLarge());
 		panel.add(progressPanel, c);
         
 		c.weighty = 0.0;
 		c.gridy += 1;
 		c.anchor = GridBagConstraints.LAST_LINE_END;
 		ImageButton cancel = new ImageButton(StockIcon.CHOOSE_CANCEL, "Cancel", true);
 		cancel.addActionListener(new ActionListener() {
 		
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				executors.requestAbortWorking();
 			}
 		});
 		panel.add(cancel, c);
 		
 		
 		panel.setBorder(Spacing.bHuge());
 		
 		executors.addListener(new EventfulListener() {
 		
 			public void change() {
 			
 				javax.swing.SwingUtilities.invokeLater(new Runnable() {
 					
 					public void run()
 					{
 						if (executors.isAborted()){
 							executors.finished();
 							setVisible(false);
 							dispose();
 						}
 						else if (executors.getCompleted()){
 							executors.finished();
 							setVisible(false);
 							dispose();
 						} else {
 							updateProgressBar();
 						}
 					}
 				});
 				
 
 			}
 		});
 		
 		pack();
 		setLocationRelativeTo(owner);
 		
 		addWindowListener(new WindowListener() {
 			
 			public void windowOpened(WindowEvent e)
 			{
 				executors.startWorking();
 			}
 		
 			public void windowIconified(WindowEvent e){}
 		
 			public void windowDeiconified(WindowEvent e){}
 		
 			public void windowDeactivated(WindowEvent e){}
 		
 			public void windowClosing(WindowEvent e){}
 			
 			public void windowClosed(WindowEvent e){}
 
 			public void windowActivated(WindowEvent e){}
 		});
 		setVisible(true);
         
         
 	}
 	
 	
 	@Override
 	public synchronized void setVisible(boolean b)
 	{
 		super.setVisible(b);
 	}
 	
 	protected void updateProgressBar(){
 				
 		for (PluralExecutor e : executors){
 			if (e.getState() == ExecutorState.WORKING){
 				progress.setValue((int)(e.getProgress() * 100));
 				progress.setIndeterminate(false);
 				break;
 			} else if (e.getState() == ExecutorState.STALLED){
 				progress.setIndeterminate(true);
 			}
 		}
 		
 	}
 		
 }
