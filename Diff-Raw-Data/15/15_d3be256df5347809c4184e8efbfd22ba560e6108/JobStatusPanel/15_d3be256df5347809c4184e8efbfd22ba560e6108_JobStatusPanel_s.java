 package org.ourgrid.portal.client.common.gui;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.ourgrid.portal.client.OurGridPortal;
 import org.ourgrid.portal.client.common.CommonServiceConstants;
 import org.ourgrid.portal.client.common.JobListener;
 import org.ourgrid.portal.client.common.StateConstants;
 import org.ourgrid.portal.client.common.actions.GetJobStatusAction;
 import org.ourgrid.portal.client.common.gui.model.UserModel;
 import org.ourgrid.portal.client.common.image.JobStatusTreeIcons;
 import org.ourgrid.portal.client.common.to.model.AbstractTreeNodeTO;
 import org.ourgrid.portal.client.common.to.model.JobTO;
 import org.ourgrid.portal.client.common.to.model.ResultTO;
 import org.ourgrid.portal.client.common.to.model.TaskPageTO;
 import org.ourgrid.portal.client.common.to.response.ResponseTO;
 import org.ourgrid.portal.client.common.to.service.CancelJobTO;
 import org.ourgrid.portal.client.common.util.JobSubmissionMessages;
 import org.ourgrid.portal.client.common.util.OurGridPortalServerUtil;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.BaseTreeLoader;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelIconProvider;
 import com.extjs.gxt.ui.client.data.TreeLoader;
 import com.extjs.gxt.ui.client.data.TreeModelReader;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.TreePanelEvent;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.widget.Container;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 
 public class JobStatusPanel extends VerticalPanel {
 	
 	public static final JobStatusTreeIcons ICONS = GWT.create(JobStatusTreeIcons.class);
 
 	protected JobTreePanel jobStatusTreePanel;
 	
 	protected ContentPanel nodeDetails;
 	
 	private LayoutContainer jobStatusContainer;
 	
 	private ToolBar cancelJobToolBar;
 	
 	private Timer getJobStatusTimer;
 	
 	private Integer jobViewId;
 	
 	private Integer tabCount;
 	
 	private TabItem container;
 	
 	private boolean jobEnded;
 	
 	private List<JobListener> jobListeners;
 	
 	private Integer firstCounter;
 
 	public JobStatusPanel(Integer jobViewId, Integer tabCount, TabItem container) {
 		this.jobViewId = jobViewId;
 		this.tabCount = tabCount;
 		this.container = container;
 		this.jobEnded = false;
 		this.firstCounter = 0;
 		this.jobListeners = new ArrayList<JobListener>();
 		init();
 	}
 	
 	public Container<?> getContainer() {
 		return container;
 	}
 
 	private void init() {
 		createMainPanel();
 		
 		createAndAddCancelButton();
 		
 		createJobStatusContainer();
 
 		createJobStatusTreePanel();
 		
 		createPanelDetail();
 		
 		this.add(jobStatusContainer);
 	}
 	
 	private void createMainPanel() {
 		this.setWidth(700);
 		this.setHeight(382);
 		this.setAutoWidth(true);
 		this.setAutoHeight(true);
 	}
 	
   	private void createJobStatusContainer() {
   		jobStatusContainer = new HorizontalPanel();
   		jobStatusContainer.setLayout(new FitLayout());
   		jobStatusContainer.setWidth(690);
   		jobStatusContainer.setHeight(382);
   		jobStatusContainer.setAutoWidth(true);
   		jobStatusContainer.setAutoHeight(true);
 	}
 
 	private void createPanelDetail() {
   		nodeDetails = new ContentPanel();
   		nodeDetails.setSize(325, 305);
   		nodeDetails.setBorders(true);
   		nodeDetails.setHeaderVisible(false);
   		nodeDetails.setScrollMode(Scroll.AUTO);
   		nodeDetails.setId("panelDetailIdJob");
   		
   		jobStatusContainer.add(nodeDetails);
   	}
 
 	private void createAndAddCancelButton() {
 		this.cancelJobToolBar = new ToolBar();
 		
 		Button cancelJobButton = new Button("Cancel Job");
 		cancelJobButton.setBorders(true);
 		cancelJobButton.setVisible(true);
 		cancelJobButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			public void componentSelected(ButtonEvent ce) {
 				cancelJob();
 			}
 			
 		});
 		
 		this.cancelJobToolBar.add(cancelJobButton);
 		this.add(this.cancelJobToolBar);
 	}
 	
 	private void createJobStatusTreePanel() {
 		
 		TreeModelReader<List<AbstractTreeNodeTO>> treeModelReader = new TreeModelReader<List<AbstractTreeNodeTO>>();
 		TreeLoader<AbstractTreeNodeTO> loader = new BaseTreeLoader<AbstractTreeNodeTO>(treeModelReader) {  
 			public boolean hasChildren(AbstractTreeNodeTO parent) {  
 				
 				String type = parent.getType();
 				if (!type.equals(TaskPageTO.typeValue)) {
 					return !parent.isLeaf();
 				}
 				
 				return true;  
 			}  
 		};
 		
 		TreeStore<AbstractTreeNodeTO> store = new TreeStore<AbstractTreeNodeTO>(loader);
 		
 		AbstractTreeNodeTO createRoot = createRoot();
 		store.add(createRoot, true);
 		
 		jobStatusTreePanel = new JobTreePanel(store);
 		jobStatusTreePanel.setSize(325, 305);
 		jobStatusTreePanel.setStateful(true);
 		jobStatusTreePanel.setBorders(true);  
 		jobStatusTreePanel.setTrackMouseOver(false);  
 		jobStatusTreePanel.setDisplayProperty("name");
 		
 		jobStatusTreePanel.setIconProvider(new ModelIconProvider<AbstractTreeNodeTO>() {
 		      public AbstractImagePrototype getIcon(AbstractTreeNodeTO model) {
 		    	  
 		          String type = model.getType();
 		          
 		          if (type.equals(ResultTO.typeValue)) {
 		        	  return ICONS.download();
 		          } else if (type.equals(TaskPageTO.typeValue)) {
 		        	  
 		        	  if (jobStatusTreePanel.isExpanded(model) && model.getChildCount() == 0) {
 		        		  AbstractImagePrototype loading = ICONS.loading();
 						return loading;
 		        	  } else {
 		        		  return null;
 		        	  }
 		          } 
 		          
 		          String status = (String) model.get("status");
 		          
 		          if (status != null) {
 		      		if (status.equals(StateConstants.UNSTARTED.toString())) {
 		    			return ICONS.unstarted();
 		    		} else if (status.equals(StateConstants.RUNNING.toString())) {
 		    			return ICONS.running();
 		    		} else if (status.equals(StateConstants.FINISHED.toString())) {
 		    			return ICONS.finished();
 		    		} else if (status.equals(StateConstants.FAILED.toString())) {
 		    			return ICONS.failed();
 		    		} else if (status.equals(StateConstants.CANCELLED.toString())) {
 		    			return ICONS.cancelled();
 		    		} else {
 		    			return ICONS.aborted();
 		    		} 
 		          } else {
 		        	  ICONS.unstarted();
 		          }
 		          
 		          return null;
 		        }
 		      });
 		
 		jobStatusTreePanel.addListener(Events.OnClick, new Listener<TreePanelEvent<AbstractTreeNodeTO>>() {
 			
 			@SuppressWarnings("unchecked")
 			public void handleEvent(TreePanelEvent<AbstractTreeNodeTO> be) {
 				
 				TreeNode node = be.getNode();
 
 				if (node == null) return;
 				
 				ModelData model = node.getModel();
 				String type = (String) model.get("type");
 				
 				if (type != null && type.equals(ResultTO.typeValue)) {
 					triggerDownload((ResultTO) model);
 				} else {
 					updateNodeDetails((String) model.get("description"));
 				}
 			}
 		});
 		
 		jobStatusTreePanel.addListener(Events.Expand, new Listener<TreePanelEvent<AbstractTreeNodeTO>>() {
 
 			public void handleEvent(TreePanelEvent<AbstractTreeNodeTO> be) {
 				ModelData model = be.getNode().getModel();
 				String type = (String) model.get("type");
 				
 				if (type != null && type.equals(TaskPageTO.typeValue)){
 					UserModel userModel = OurGridPortal.getUserModel();
 					userModel.addTaskPage(getJobId(), ((TaskPageTO) model).getFirstTaskId());
 				}
 			}
 			
 		});
 		
 	    jobStatusContainer.add(jobStatusTreePanel);
 	}
 	
 	protected void triggerDownload(ResultTO resultTO) {
 		String link = GWT.getModuleBaseURL() + resultTO.getUrl();
 		com.google.gwt.user.client.Window.open(link, "", "");
 	}
 
 	protected void updateNodeDetails(String description) {
 		nodeDetails.removeAll();
 		nodeDetails.addText(description);
 		refresh();
 	}
 
 	public void refresh() {
 		this.doLayout();	
 	}
 
 	private AbstractTreeNodeTO createRoot() {
 		JobTO jobTO = new JobTO();
 		int relativeJobId = tabCount + 1;
 		
 		jobTO.setRelativeId(relativeJobId);
 		jobTO.set("name", "Job : " + relativeJobId);
 		jobTO.setDescription("");
 		
 		return jobTO;
 	}
 
 	public void cancelJob() {
 		
 		CancelJobTO cancelJobTO = createCancelJobTO(getJobId());
 		OurGridPortalServerUtil.getInstance().execute(cancelJobTO, new AsyncCallback<ResponseTO>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				if (caught.getMessage().equals(JobSubmissionMessages.JOB_ALREADY_CANCELLED_MESSAGE)) {
 					setClosable();
 				}
 
 				MessageBox.alert("Cancel Job Error", caught.getMessage(), null);
 			}
 
 			@Override
 			public void onSuccess(ResponseTO result) {
 				setClosable();
 
 				MessageBox.info("Job " + (tabCount+1), result.getMessage(), null);
 			}
 
 		});
 
 	}
 	
 	protected void setClosable() {
 		container.setClosable(true);
 	}
 	
 	public Integer getJobId() {
 		return OurGridPortal.getUserModel().getJobId(jobViewId);
 	}
 	
 	private CancelJobTO createCancelJobTO(Integer jobId) {
 		CancelJobTO cancelJobTO = new CancelJobTO();
 		cancelJobTO.setExecutorName(CommonServiceConstants.CANCEL_JOB_EXECUTOR);
 		cancelJobTO.setJobId(jobId);
 		return cancelJobTO;
 	}
 
 	public void scheduleJobDescriptionRepeatedAction() {
 		GetJobStatusAction action = new GetJobStatusAction(getJobId(), this);
 		action.runAction();
 		getJobStatusTimer = action.scheduleUpdateTimer();
 	}
 	
 	public void updateJobStatus(JobTO newJobStatus) {
 		boolean isFinished = newJobStatus.getStatus().equalsIgnoreCase("finished");
 		
 		updateStatusPanel(newJobStatus);
         
 		if (!jobIsRunning(newJobStatus.getStatus())) {
         	setClosable();
         	
         	if (!jobEnded) {
         		OurGridPortal.refreshFileExplorerRoot();
         	}
         	jobEnded = true;
         }
         
         if (isFinished) {
         	firstCounter++;
         	for (JobListener jobListener : jobListeners) {
         		jobListener.jobFinished(newJobStatus);
         	}
         }
 	}
 	
 	public void registerJobListener(JobListener jobListener) {
 		jobListeners.add(jobListener);
 	}
 	
 	private void updateStatusPanel(JobTO newJobTo) {
 		
 		newJobTo.setRelativeId(tabCount + 1);
 		newJobTo.setName(newJobTo.toString());
 
 		TreeStore<AbstractTreeNodeTO> store = jobStatusTreePanel.getStore();
 
 		JobTO oldJobTO = null;
 		if (!store.getModels().isEmpty()) {
 			oldJobTO = (JobTO) store.getModels().iterator().next();
 		}
 		
 		if (oldJobTO == null) {
 			return;
 		}
 		
		update(oldJobTO, newJobTo);
		store.commitChanges();
 	}
 
	void update(AbstractTreeNodeTO oldTO, AbstractTreeNodeTO newTO) {
 		oldTO.setProperties(newTO.getProperties());
 		List<AbstractTreeNodeTO> orphans = new LinkedList<AbstractTreeNodeTO>();
 		for (ModelData child : newTO.getChildren()) {
 			AbstractTreeNodeTO newChild = (AbstractTreeNodeTO) child;
 			AbstractTreeNodeTO oldChild = oldTO.getChild(newChild.get(AbstractTreeNodeTO.id));
 			if (oldChild == null) {
 				orphans.add(newChild);
 			} else {
				update(oldChild, newChild);
 			}
 		}
 		for (AbstractTreeNodeTO orphan : orphans) {
 			oldTO.add(orphan);
 		}
 		jobStatusTreePanel.refresh(oldTO);
 	}
 	
 	private boolean jobIsRunning(String state) {
 		return state.equals(StateConstants.UNSTARTED.toString()) || 
 			state.equals(StateConstants.RUNNING.toString());
 	}
 	
 	//TODO chamar esse metodo quando nao tiver mais job running e ativar o timer qndo uma super task for expandida.
 	public void stopStatusTimer() {
 		if (getJobStatusTimer != null) {
 			this.getJobStatusTimer.cancel();
 		}
 	}
 	
 	public Integer getJobViewId() {
 		return jobViewId;
 	}
 
 }
