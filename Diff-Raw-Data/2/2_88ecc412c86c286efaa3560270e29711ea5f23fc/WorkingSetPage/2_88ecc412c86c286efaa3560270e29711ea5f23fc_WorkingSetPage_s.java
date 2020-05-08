 package org.iucn.sis.client.tabs;
 
 import java.util.Date;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.PublicationCache;
 import org.iucn.sis.client.api.caches.RegionCache;
 import org.iucn.sis.client.api.caches.SchemaCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.api.ui.models.workingset.WSStore;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.api.utils.SIS;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.filters.AssessmentFilterPanel;
 import org.iucn.sis.client.panels.utils.RefreshLayoutContainer;
 import org.iucn.sis.client.panels.workingsets.DeleteWorkingSetPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetAddAssessmentsPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetEditBasicPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetOptionsPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetPermissionPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetReportPanel;
 import org.iucn.sis.client.panels.workingsets.WorkingSetSummaryPanel;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableDraftAssessment;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 import com.solertium.util.gwt.ui.StyledHTML;
 
 public class WorkingSetPage extends FeaturedItemContainer<Integer> {
 	
 	public static String URL_HOME = "home";
 	public static String URL_EDIT = "edit";
 	public static String URL_TAXA = "taxa";
 	public static String URL_ASSESSMENTS = "assessments";
 		
 	
 	private final WorkingSetSummaryPanel homePage = new WorkingSetSummaryPanel();
 	private final WorkingSetEditBasicPanel editor = new WorkingSetEditBasicPanel(this);
 	private final WorkingSetAddAssessmentsPanel assessments = new WorkingSetAddAssessmentsPanel(this);
 	private final WorkingSetOptionsPanel taxa = new WorkingSetOptionsPanel();
 	
 	@Override
 	protected void drawBody(DoneDrawingCallback callback) {
 		String url = getUrl();
 		
 		if (URL_EDIT.equals(url))
 			setBodyContainer(editor);
 		else if (URL_TAXA.equals(url))
 			setBodyContainer(taxa);
 		else if (URL_ASSESSMENTS.equals(url))
 			setBodyContainer(assessments);
 		else
 			setBodyContainer(homePage);
 		
 		callback.isDrawn();
 	}
 	
 	public void refreshFeature() {
 		drawFeatureArea();
 		ClientUIContainer.headerContainer.centerPanel.refreshWorkingSetView();
 	}
 	
 	@Override
 	public LayoutContainer updateFeature() {
 		final WorkingSet item = WorkingSetCache.impl.getWorkingSet(getSelectedItem());
 		
 		final LayoutContainer container = new LayoutContainer();
 		container.add(new StyledHTML("<center>" + item.getName() + "</center>", "page_workingSet_featured_header"));
 		container.add(createSpacer(40));
 		final Grid stats = new Grid(3, 2);
 		stats.setCellSpacing(3);
 		stats.setWidget(0, 0, new StyledHTML("Created:", "page_workingSet_featured_prompt"));
 		stats.setWidget(0, 1, new StyledHTML(FormattedDate.impl.getDate(item.getCreatedDate()), "page_workingSet_featured_content"));
 		stats.setWidget(1, 0, new StyledHTML("Mode:", "page_workingSet_featured_prompt"));
 		stats.setWidget(1, 1, new StyledHTML("Public", "page_workingSet_featured_content"));
 		stats.setWidget(2, 0, new StyledHTML("Scope:", "page_workingSet_featured_prompt"));
 		stats.setWidget(2, 1, new StyledHTML(AssessmentFilterPanel.getString(item.getFilter()), "page_workingSet_featured_content"));
 		
 		for (int i = 0; i < stats.getRowCount(); i++)
 			stats.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);
 		
 		container.add(stats);
 		
 		return container;
 	}
 	
 	@Override
 	protected void updateSelection(Integer selection) {
 		//WorkingSetCache.impl.setCurrentWorkingSet(selection, true);
 		StateManager.impl.setWorkingSet(WorkingSetCache.impl.getWorkingSet(selection));
 	}
 	
 	protected void setBodyContainer(LayoutContainer container) {
 		bodyContainer.removeAll();
 		
 		if (container instanceof RefreshLayoutContainer)
 			((RefreshLayoutContainer)container).refresh();
 		
 		bodyContainer.add(container);
 	}
 	
 	@Override
 	protected void drawOptions(DrawsLazily.DoneDrawingCallback callback) {
 		//final WorkingSet item = WorkingSetCache.impl.getWorkingSet(getSelectedItem());
 		
 		if (optionsContainer.getItemCount() == 0) {
 			final TableLayout layout = new TableLayout(1);
 			layout.setCellHorizontalAlign(HorizontalAlignment.CENTER);
 			layout.setCellVerticalAlign(VerticalAlignment.MIDDLE);
 			layout.setCellSpacing(20);
 			
 			final LayoutContainer buttonArea = new LayoutContainer(layout);
 			buttonArea.setScrollMode(Scroll.AUTOY);
 			
 			buttonArea.add(createButton("Edit Basic Information", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					setBodyContainer(editor);
 				}
 			}));
 			buttonArea.add(createButton("Taxa Manager", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					setBodyContainer(taxa);
 				}
 			}));
 			buttonArea.add(createButton("Create Draft Assessment", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					setBodyContainer(assessments);
 				}
 			}));
 			buttonArea.add(createButton("Permission Manager", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.GRANT, 
 							WorkingSetCache.impl.getCurrentWorkingSet())) {
 						final WorkingSetPermissionPanel panel = new WorkingSetPermissionPanel();
 						panel.draw(new DrawsLazily.DoneDrawingCallback() {
 							public void isDrawn() {
 								setBodyContainer(panel);	
 							}
 						});
 					} else
 						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission to manage " +
 								"the permissions for this Working Set.");
 				}
 			}));
 			buttonArea.add(createButton("Report Generator", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					setBodyContainer(new WorkingSetReportPanel());
 				}
 			}));			
 			buttonArea.add(createButton("Submit For Publication", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					if (AuthorizationCache.impl.canUse(AuthorizableFeature.PUBLICATION_MANAGER_FEATURE)) {
 						WindowUtils.SimpleMessageBoxListener listener = new WindowUtils.SimpleMessageBoxListener() {
 							public void onYes() {
 								PublicationCache.impl.submit(WorkingSetCache.impl.getCurrentWorkingSet(), new GenericCallback<Object>() {
 									public void onSuccess(Object result) {
 										WindowUtils.infoAlert("All assessments in this working set were submitted successfully.");
 									}
 									public void onFailure(Throwable caught) {
 										// TODO: list assessments that caused failure
 										WindowUtils.errorAlert("Failed to submit working set, please try again later.");
 									}
 								});
 							}
 						};
 						
 						WindowUtils.confirmAlert("Confirm", "Are you sure you want to " +
 							"submit this working set? Assessments will only be submitted " +
 							"if <b>every</b> assessment in this working set is eligible " +
							"for submission and passes integrity valiation.", listener);
 					}
 					else
 						WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission to submit " +
 								"this working set.");
 				}
 			}));
 			buttonArea.add(createButton("Export to Offline", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					//setBodyContainer(new WorkingSetExporter(WorkingSetPage.this));
 					WindowUtils.confirmAlert("Export Working Set", "A dialog box will appear and ask"
 							+ " you where you like to save the zipped working set.  The zipped file "
 							+ "will contain the entire working set including the basic information, the "
 							+ "taxa information, and the draft assessments associated with each taxa if they" 
 							+ " exist.  Proceed?", new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							export(WorkingSetCache.impl.getWorkingSet(getSelectedItem()));
 						}
 					});
 				}
 			}));
 			buttonArea.add(createButton("Export to Access", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					WindowUtils.confirmAlert("Confirm", "Are you sure you want to begin the exporting process?", new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							final WorkingSet workingSet = WorkingSetCache.impl.getCurrentWorkingSet(); 
 							final String url = UriBase.getInstance().getExportBase() + "/sources/access/" + 
 								workingSet.getId() + "?time=" + new Date().getTime();
 						
 							final ContentPanel content = new ContentPanel();
 							content.setUrl(url);
 						
 							final Window exportWindow = WindowUtils.newWindow("Export " + workingSet.getName() + "...");
 							exportWindow.setScrollMode(Scroll.AUTO);
 							exportWindow.setSize(500, 400);
 							exportWindow.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
 								public void componentSelected(ButtonEvent ce) {
 									exportWindow.hide();
 								}
 							}));
 							exportWindow.setUrl(url);
 							exportWindow.show();
 						}
 					});
 				}
 			}));
 			
 			buttonArea.add(createButton("Unsubscribe", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					WindowUtils.confirmAlert("Unsubscribe?", "Are you sure you want to unsubscribe " +
 							"from this working set? You will be able to subscribe again if your " +
 							"permissions are unchanged.",
 							new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							WorkingSetCache.impl.unsubscribeToWorkingSet(WorkingSetCache.impl.getWorkingSet(getSelectedItem()), new GenericCallback<String>() {
 								public void onSuccess(String result) {
 									WindowUtils.infoAlert("You have successfully unsubscribed from the working set " + result + ".");
 									WSStore.getStore().update();
 									StateManager.impl.reset();
 								}
 								public void onFailure(Throwable caught) {
 									WindowUtils.errorAlert("Failed to unsubscribe from this working set. Please try again later.");
 								}
 							});
 						}
 					});
 				}
 			}));
 			
 			buttonArea.add(createButton("Delete", new SelectionListener<ButtonEvent>() {
 				public void componentSelected(ButtonEvent ce) {
 					final WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
 					if (!canDelete(ws)) {
 						WindowUtils.errorAlert("You do not have permission to delete this working set.");
 						return;
 					}
 					
 					WindowUtils.confirmAlert("Delete working set?", "Are you sure you " +
 							"want to completely delete this working set? <b>You can not " +
 							"undo this operation.</b>", new WindowUtils.SimpleMessageBoxListener() {
 						public void onYes() {
 							DeleteWorkingSetPanel.ensurePermissionsCleared(ws.getId(), new GenericCallback<String>() {
 								public void onFailure(Throwable caught) {
 									if (caught != null) {
 										WindowUtils.errorAlert("Error communicating with the server. Please try again later.");
 									}
 									else {
 										WindowUtils.errorAlert("Permission Error", "There are still users that are granted permissions via this Working Set. " +
 										"Before you can delete, please visit the Permission Manager and remove all of these users.");
 									}
 								}
 								public void onSuccess(String result) {
 									WorkingSetCache.impl.deleteWorkingSet(ws, new GenericCallback<String>() {
 										public void onFailure(Throwable caught) {
 											WindowUtils.errorAlert("Failed to delete this working set. Please try again later.");
 										}
 										@Override
 										public void onSuccess(String result) {
 											WindowUtils.infoAlert("You have successfully deleted the working set " + result + ".");
 											WSStore.getStore().update();
 											StateManager.impl.reset();
 										}
 									});
 								}
 							});
 						}
 					});
 				}
 			}));
 			
 			optionsContainer.removeAll();
 			optionsContainer.add(buttonArea);
 		}
 		callback.isDrawn();
 	}
 	
 	private boolean canDelete(WorkingSet workingSet) {
 		return AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.DELETE, workingSet);
 	}
 	
 	private Button createButton(String text, SelectionListener<ButtonEvent> listener) {
 		return createButton(text, null, listener);
 	}
 	
 	private Button createButton(String text, String icon, SelectionListener<ButtonEvent> listener) {
 		Button button = new Button(text);
 		button.setIconStyle(icon);
 		button.setWidth(150);
 		button.addSelectionListener(listener);
 		
 		return button;
 	}
 	
 	public void setEditWorkingSetTab() {
 		setBodyContainer(editor);
 	}
 	
 	public void setManagerTab() {
 		setBodyContainer(homePage);
 	}
 	
 	public void setAssessmentTab() {
 		setBodyContainer(assessments);
 	}
 	
 	public void setEditTaxaTab() {
 		setBodyContainer(taxa);
 	}
 	
 	private void export(final WorkingSet ws) {
 		if (SIS.isOnline()) {
 			WindowUtils.confirmAlert("Lock Assessments", "Would you like to lock the online version " +
 					"of the draft assessments of the regions " + RegionCache.impl.getRegionNamesAsReadable(ws.getFilter()) + 
 					" for this working set? You can only commit changes to online versions via an " +
 					"import if you have obtained the locks.", new WindowUtils.MessageBoxListener() {
 				public void onYes() {
 					attemptLocking(ws);
 				}
 				public void onNo() {
 					fireExport(ws, false);
 				}
 			}, "Yes", "No");
 		} else {
 			attemptLocking(ws);
 		}
 	}
 	
 	/**
 	 * TODO: do all this mess on the server.
 	 * @param ws
 	 */
 	private void attemptLocking(final WorkingSet ws) {
 		String permissionProblem = null;
 		for (Integer curSpecies : ws.getSpeciesIDs()) {
 			AuthorizableDraftAssessment d = new AuthorizableDraftAssessment(
 					TaxonomyCache.impl.getTaxon(curSpecies), 
 					SchemaCache.impl.getDefaultSchema(), 
 					ws.getFilter().getRegionIDsCSV());
 			
 			if(!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, d))
 				permissionProblem = d.getTaxon().getFullName();
 		}
 
 		if (permissionProblem == null) {
 			fireExport(ws, true);
 		} else {
 			WindowUtils.confirmAlert("Insufficient Permissions", "You cannot lock " +
 					"the assessments for this working set as you do not have sufficient " +
 					"permissions to edit the draft assessments for at least " +
 					"the taxon " + permissionProblem + ". Would you like to export the " +
 					"working set without locking anyway?", new WindowUtils.SimpleMessageBoxListener() {
 				public void onYes() {
 					fireExport(ws, false);
 				}
 			});
 		}
 	}
 	
 	public void fireExport(final WorkingSet workingSet, boolean lock) {
 		final String url = UriBase.getInstance().getSISBase() + "/workingSetExporter/public/"
 			+ SISClientBase.currentUser.getUsername() + "/" + workingSet.getId() + "?lock="
 			+ lock + "&time=" + new Date().getTime();
 		
 		final ContentPanel content = new ContentPanel();
 		content.setUrl(url);
 		
 		final Window exportWindow = WindowUtils.newWindow("Export " + workingSet.getName() + "...");
 		exportWindow.setScrollMode(Scroll.AUTO);
 		exportWindow.setSize(500, 400);
 		exportWindow.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				exportWindow.hide();
 			}
 		}));
 		exportWindow.setUrl(url);
 		exportWindow.show();
 		
 		WindowUtils.infoAlert("Export Started", "Your working sets are being exported. A popup "
 				+ "will notify you when the export has finished and when the files are "
 				+ "available for download.  Please be patient as larger working sets will "
 				+ "take longer to export.");
 		
 		/*
 		WorkingSetCache.impl.exportWorkingSet(workingSet.getId(), lock, new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Export failed, please try again later.");
 			}
 			public void onSuccess(String arg0) {
 				WorkingSetExporter.saveExportedZip(arg0, workingSet);
 			}
 		});*/
 	}
 
 }
