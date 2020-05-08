 package com.cee.news.client.workingset;
 
 import com.cee.news.client.ConfirmView;
 import com.cee.news.client.async.EntityUpdateResult;
 import com.cee.news.client.async.NotificationCallback;
 import com.cee.news.client.content.AddSiteWorkflow;
 import com.cee.news.client.content.NewSiteWizardView;
 import com.cee.news.client.content.SiteAddedEvent;
 import com.cee.news.client.content.SiteAddedHandler;
 import com.cee.news.client.content.SiteListContentModel;
 import com.cee.news.client.error.ErrorHandler;
 import com.cee.news.client.error.ErrorSourceBase;
 import com.cee.news.client.list.MultiSelectListPresenter;
 import com.cee.news.client.list.SelectionListChangedEvent;
 import com.cee.news.client.list.SelectionListChangedHandler;
 import com.cee.news.model.EntityKey;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class WorkingSetWorkflow extends ErrorSourceBase {
 
 	private final WorkingSetListModel workingSetListModel;
 
 	private final WorkingSetServiceAsync service = WorkingSetServiceAsync.Util.getInstance();
 
 	private final WorkingSetView workingSetView;
 	
 	private final ConfirmView confirmDeletionView;
 	
 	private final AddSiteWorkflow addSiteWorkflow;
 
 	public WorkingSetWorkflow(final WorkingSetListModel workingSetListModel,
 	        final SiteListContentModel siteListModel,
 	        final WorkingSetView workingSetView,
 	        final NewSiteWizardView newSiteWizard,
 	        final ConfirmView confirmDeletionView) {
 
 	    this.workingSetListModel = workingSetListModel;
 		this.workingSetView = workingSetView;
 		this.confirmDeletionView = confirmDeletionView;
 		workingSetView.getButtonCancel().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				workingSetView.hide();
 			}
 		});
 		workingSetView.getButtonSave().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				validate();
 			}
 		});
 		workingSetView.getButtonRemoveAllSites().addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 siteListModel.clearSelection();
             }
         });
 		workingSetView.addSelectionListChangedHandler(new SelectionListChangedHandler<EntityKey>() {
             @Override
             public void onSelectionListChanged(SelectionListChangedEvent<EntityKey> event) {
                 siteListModel.setSelections(event.getKeys());
             }
         });
 		siteListModel.addSelectionListChangedHandler(new SelectionListChangedHandler<EntityKey>() {
             @Override
             public void onSelectionListChanged(SelectionListChangedEvent<EntityKey> event) {
                 workingSetView.setSelectedSites(event.getKeys());
             }
         });
 		
 		addSiteWorkflow = new AddSiteWorkflow(newSiteWizard);
 		workingSetView.getButtonAddNewSite().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				addSiteWorkflow.start();
 			}
 		});
 		addSiteWorkflow.addSiteAddedHandler(new SiteAddedHandler() {
 			@Override
 			public void onSiteAdded(final SiteAddedEvent event) {
 				siteListModel.findAllSites(new NotificationCallback() {
 					
 					@Override
 					public void finished() {
 					    siteListModel.addSelection(event.getEntityKey());
 					}
 				});
 			}
 		});
 		
 		new MultiSelectListPresenter<EntityKey>(siteListModel, siteListModel, workingSetView.getAvailableSitesList(), workingSetView.getSelectedSitesList());
 	
 		confirmDeletionView.getButtonNo().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				confirmDeletionView.hide();
 			}
 		});
 		confirmDeletionView.getButtonYes().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				confirmDeletionView.hide();
 				performDeletion();
 			}
 		});
 	}
 
 	public void newWorkingSet() {
 		WorkingSetData workingSetData = new WorkingSetData();
 		workingSetData.setIsNew(true);
 		showEditor(workingSetData);
 	}
 	
 	public void editWorkingSet() {
 		final EntityKey workingSetKey = workingSetListModel.getSelectedKey();
 		service.getWorkingSet(workingSetKey, new AsyncCallback<WorkingSetData>() {
 			
 			@Override
 			public void onSuccess(WorkingSetData result) {
 				showEditor(result);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				fireErrorEvent(caught, "Could not retrieve working set \"" + workingSetKey + "\"");
 			}
 		});
 	}
 	
 	public void deleteWorkingSet() {
 		final EntityKey workingSetKey = workingSetListModel.getSelectedKey();
 		confirmDeletionView.getLabelQuestion().setText("Are you sure deleting working set " + workingSetKey.getName() + "?");
 		confirmDeletionView.center();
 	}
 	
 	private void performDeletion() {
 		final EntityKey workingSetKey = workingSetListModel.getSelectedKey();
 		service.deleteWorkingSet(workingSetKey, new AsyncCallback<Void>() {
 			
 			@Override
 			public void onSuccess(Void result) {
 				workingSetListModel.findAllWorkingSets(new NotificationCallback() {
 					
 					@Override
 					public void finished() {
 						if (workingSetListModel.getContentCount() > 0) {
 		                    EntityKey firstWorkingSet = workingSetListModel.getKeys().get(0);
 		                    workingSetListModel.setSelectedKey(firstWorkingSet);
 		                } else {
 		                	workingSetListModel.setSelectedKey(null);
 		                }
 					}
 				});
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				fireErrorEvent(caught, "Could not delete working set \"" + workingSetKey + "\"");
 			}
 		});
 	}
 	
 	private void showEditor(WorkingSetData wsd) {
		workingSetView.getErrorText().setText("");
 	    workingSetView.edit(wsd);
 	    workingSetView.center();
 	}
 
 	private void validate() {
 		final WorkingSetData workingSetData = workingSetView.getData();
 		if (workingSetView.hasValidationErrors()) {
 		    workingSetView.showValidationErrors();
 		    return;
 		}
 		String name = workingSetData.getNewName();
 		if (name == null || name.trim().isEmpty()) {
 			showValidationError("Missing or invalid working set name!");
 			return;
 		}
 
 		service.update(workingSetData, new AsyncCallback<EntityUpdateResult>() {
 
 			@Override
 			public void onSuccess(final EntityUpdateResult result) {
 				switch (result.getState()) {
 				case entityExists:
 					showValidationError("Working Set with name " + workingSetData.getNewName() + " exists!");
 					break;
 				case ok:
 				    workingSetView.hide();
 					workingSetListModel.findAllWorkingSets(new NotificationCallback() {
 						
 						@Override
 						public void finished() {
 							workingSetListModel.setSelectedKey(result.getKey());
 						}
 					});
 					break;
 
 				default:
 					break;
 				}
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				fireErrorEvent(caught, "Could not store working set");
 			}
 		});
 	}
 
 	private void showValidationError(String message) {
 	    workingSetView.getErrorText().setText(message);
 	}
 	
 	@Override
 	public HandlerRegistration addErrorHandler(ErrorHandler handler) {
 		addSiteWorkflow.addErrorHandler(handler);
 		return super.addErrorHandler(handler);
 	}
 }
