 package org.computer.knauss.reqtDiscussion.ui.ctrl;
 
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JOptionPane;
 import javax.swing.ProgressMonitor;
 import javax.swing.SwingWorker;
 
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 
 public abstract class AbstractDiscussionIterationCommand extends
 		AbstractCommand implements PropertyChangeListener {
 
 	private static final long serialVersionUID = 1L;
 	private ProgressMonitor progressMonitor;
 	private ClassificationItemTask task;
 	private HighlightRelatedDiscussions hrd;
 
 	public AbstractDiscussionIterationCommand(String name) {
 		super(name);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		progressMonitor = new ProgressMonitor(null,
 				getValue(AbstractAction.NAME), null, 0, 100);
 		progressMonitor.setProgress(0);
 		task = new ClassificationItemTask();
 		task.addPropertyChangeListener(this);
 		task.execute();
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if ("progress" == evt.getPropertyName()) {
 			int progress = (Integer) evt.getNewValue();
 			progressMonitor.setProgress(progress);
 			if (progressMonitor.isCanceled()) {
 				task.cancel(true);
 			}
 		}
 	}
 
 	protected HighlightRelatedDiscussions getHRD() {
 		if (this.hrd == null) {
 			try {
 				this.hrd = new HighlightRelatedDiscussions();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return this.hrd;
 	}
 
 	protected void preProcessingHook() {
 
 	}
 
 	protected void postProcessingHook() {
 
 	}
 
 	protected abstract void processDiscussionHook(Discussion[] d);
 
 	protected DiscussionEvent[] getDiscussionEvents(Discussion[] d) {
 		List<DiscussionEvent> ret = new LinkedList<DiscussionEvent>();
 		for (Discussion disc : d) {
 			ret.addAll(Arrays.asList(disc.getDiscussionEvents()));
 		}
 		return ret.toArray(new DiscussionEvent[0]);
 	}
 
 	class ClassificationItemTask extends SwingWorker<Void, Void> {
 		@Override
 		public Void doInBackground() {
 			preProcessingHook();
 
 			Discussion[] discussions = getDiscussionTableModel()
 					.getDiscussions();
 
 			// Deal with complex discussions
 			List<Discussion[]> aggregatedDiscussions;
 			if (getVisualizationConfiguration().isAggregateDiscussions())
 				aggregatedDiscussions = getHRD().getAllAggregatedDiscussions(
 						discussions);
 			else {
 				aggregatedDiscussions = new LinkedList<Discussion[]>();
 				for (Discussion d : discussions) {
 					aggregatedDiscussions.add(new Discussion[] { d });
 				}
 			}
 
 			int progress = 0;
			int total = aggregatedDiscussions.size();
 
 			setProgress(0);
 
 			try {
 				for (Discussion[] d : aggregatedDiscussions) {
 					if (isCancelled()) {
 						System.err.println(getClass().getSimpleName()
 								+ ": Canceled.");
 						break;
 					}
 					processDiscussionHook(d);
 					setProgress((progress++ * 100) / total);
 				}
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(null, e.getClass()
 						.getSimpleName() + ": " + e.getMessage(),
 						"Error performing " + getValue(AbstractAction.NAME),
 						JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 			}
 			// set progress to 100 to close progress monitor
 			setProgress((progress++ * 100) / total);
 			return null;
 		}
 
 		@Override
 		public void done() {
 			postProcessingHook();
 		}
 	}
 }
