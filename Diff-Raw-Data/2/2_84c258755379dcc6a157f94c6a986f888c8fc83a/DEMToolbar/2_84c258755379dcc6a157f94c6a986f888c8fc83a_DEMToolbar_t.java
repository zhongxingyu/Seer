 package org.iucn.sis.client.panels.dem;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.assessment.ReferenceableAssessment;
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.ViewCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.ui.users.panels.ManageCreditsWindow;
 import org.iucn.sis.client.api.ui.views.SISView;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.assessments.AssessmentAttachmentPanel;
 import org.iucn.sis.client.panels.assessments.NewAssessmentPanel;
 import org.iucn.sis.client.panels.assessments.TrackChangesPanel;
 import org.iucn.sis.client.panels.criteracalculator.ExpertPanel;
 import org.iucn.sis.client.panels.images.ImageManagerPanel;
 import org.iucn.sis.client.panels.taxomatic.TaxonCommonNameEditor;
 import org.iucn.sis.client.panels.taxomatic.TaxonSynonymEditor;
 import org.iucn.sis.shared.api.acl.InsufficientRightsException;
 import org.iucn.sis.shared.api.acl.UserPreferences;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.integrity.ClientAssessmentValidator;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Html;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.InfoConfig;
 import com.extjs.gxt.ui.client.widget.Popup;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class DEMToolbar extends ToolBar {
 	
 	public enum EditStatus {
 		READ_ONLY, EDIT_DATA
 	}
 	
 	private final AutosaveTimer autoSave;
 	
 	private Integer autoSaveInterval = 2;
 	
 	private Button editViewButton;
 	private ComplexListener<EditStatus> refreshListener;
 	private SimpleListener saveListener;
 	
 	public DEMToolbar() {
 		this.autoSave = new AutosaveTimer();
 		setAutoSaveInterval(SISClientBase.currentUser.getPreference(UserPreferences.AUTO_SAVE_TIMER, "2"));
 	}
 	
 	private void setAutoSaveInterval(String interval) {
 		if ("-1".equals(interval))
 			autoSaveInterval = null;
 		else {
 			try {
 				this.autoSaveInterval = Integer.valueOf(interval);
 			} catch (Exception e) {
 				this.autoSaveInterval = 2;
 			}
 			if (autoSaveInterval.intValue() < 0)
 				autoSaveInterval = null;
 		}
 	}
 	
 	public void setRefreshListener(ComplexListener<EditStatus> refreshListener) {
 		this.refreshListener = refreshListener;
 	}
 	
 	public void setSaveListener(SimpleListener saveListener) {
 		this.saveListener = saveListener;
 	}
 	
 	public void build() {
 		editViewButton = new Button();
 		editViewButton.setText("Read Only Mode");
 		editViewButton.setIconStyle("icon-read-only");
 		editViewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				Assessment cur = AssessmentCache.impl.getCurrentAssessment();
 				Button source = ce.getButton();
 
 				if (cur != null && !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, cur)) {
 					WindowUtils.errorAlert("You do not have rights to edit this assessment.");
 				} else {
 					EditStatus eventData;
 					if ("Read Only Mode".equals(source.getText())) {
 						source.setText("Edit Data Mode");
 						source.setIconStyle("icon-unlocked");
 						eventData = EditStatus.READ_ONLY;
 					} else {
 						source.setText("Read Only Mode");
 						source.setIconStyle("icon-read-only");
 						eventData = EditStatus.EDIT_DATA;
 					}
 					
 					if (refreshListener != null)
 						refreshListener.handleEvent(eventData);
 				}
 			}
 		});
 
 		add(editViewButton);
 		add(new SeparatorToolItem());
 
 		Button item = new Button();
 		item.setText("New");
 		item.setIconStyle("icon-new-document");
 
 		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 
 				if (TaxonomyCache.impl.getCurrentTaxon() == null) {
 					WindowUtils.errorAlert("Please select a taxon to create an assessment for.  "
 							+ "You can select a taxon using the navigator, the search function, " + " or the browser.");
 				}
 
 				else if (TaxonomyCache.impl.getCurrentTaxon().getFootprint().length < TaxonLevel.GENUS) {
 					WindowUtils.errorAlert("You must select a species or lower taxa to assess.  "
 							+ "You can select a different taxon using the navigator, the search function, "
 							+ " or the browser.");
 				} else {
 					final NewAssessmentPanel panel = new NewAssessmentPanel();
 					panel.show();
 				}
 			}
 
 		});
 
 		add(item);
 		add(new SeparatorToolItem());
 
 		item = new Button("Save");
 		item.setIconStyle("icon-save");
 		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (AssessmentCache.impl.getCurrentAssessment() == null)
 					return;
 
 				try {
 					boolean save = ViewCache.impl.getCurrentView() != null && AssessmentClientSaveUtils.shouldSaveCurrentAssessment(
 							ViewCache.impl.getCurrentView().getCurPage().getMyFields());
 
 					if (save) {
 						stopAutosaveTimer();
 						WindowUtils.showLoadingAlert("Saving assessment...");
 						AssessmentClientSaveUtils.saveAssessment(ViewCache.impl.getCurrentView().getCurPage().getMyFields(),
 								AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
 							public void onFailure(Throwable arg0) {
 								WindowUtils.hideLoadingAlert();
 								layout();
 								WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
 								resetAutosaveTimer();
 							}
 
 							public void onSuccess(Object arg0) {
 								WindowUtils.hideLoadingAlert();
 								Info.display("Save Complete", "Successfully saved assessment {0}.",
 										AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
 								Debug.println("Explicit save happened at {0}", AssessmentCache.impl.getCurrentAssessment().getLastEdit().getCreatedDate());
 								resetAutosaveTimer();
 								//TODO: ClientUIContainer.headerContainer.update();
 								if (saveListener != null)
 									saveListener.handleEvent();
 							}
 						});
 					} else {
 						WindowUtils.hideLoadingAlert();
 						layout();
 						Info.display(new InfoConfig("Save not needed", "No changes were made."));
 						resetAutosaveTimer();
 					}
 				} catch (InsufficientRightsException e) {
 					WindowUtils.errorAlert("Sorry, but you do not have sufficient rights " + "to perform this action.");
 				}
 			}
 		});
 		add(item);
 		add(new SeparatorToolItem());
 
 		item = new Button();
 		item.setIconStyle("icon-attachment");
 		item.setText("Attachments");
 		item.setEnabled(SimpleSISClient.iAmOnline);
 		item.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
 					WindowUtils.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
 					return;
 				}
 
 				final AssessmentAttachmentPanel attachPanel = new AssessmentAttachmentPanel(AssessmentCache.impl.getCurrentAssessment().getInternalId());
 				attachPanel.draw(new AsyncCallback<String>() {
 					public void onSuccess(String result) {
 						final Window uploadShell = WindowUtils.getWindow(true, true, "");
 						uploadShell.setLayout(new FitLayout());
 						uploadShell.setWidth(800);
 						uploadShell.setHeight(400);
 						uploadShell.setHeading("Attachments");
 						uploadShell.add(attachPanel);
 						uploadShell.show();
 						uploadShell.center();
 						uploadShell.layout();
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Server error: Unable to get file attachments for this assessment");				
 					}
 				});
 			}
 		});
 
 		add(item);
 
 		add(new Button());
 
 		item = new Button();
 		item.setIconStyle("icon-information");
 		item.setText("Summary");
 
 		Menu mainMenu = new Menu();
 		item.setMenu(mainMenu);
 
 		MenuItem mItem = new MenuItem();
 		mItem.setIconStyle("icon-expert");
 		mItem.setText("Quick " + ExpertPanel.titleText + " Result");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (AssessmentCache.impl.getCurrentAssessment() == null) {
 					WindowUtils.infoAlert("Alert", "Please select an assessment first.");
 					return;
 				}
 				ExpertPanel expertPanel = new ExpertPanel();
 				expertPanel.update();
 
 				Window s = WindowUtils.getWindow(true, false, ExpertPanel.titleText);
 				s.setLayout(new BorderLayout());
 				s.add(new Html("&nbsp"), new BorderLayoutData(LayoutRegion.WEST, 20));
 				s.add(new Html("&nbsp"), new BorderLayoutData(LayoutRegion.NORTH, 5));
 				s.add(new Html("&nbsp"), new BorderLayoutData(LayoutRegion.SOUTH, 5));
 				s.setSize(520, 360);
 				s.add(expertPanel, new BorderLayoutData(LayoutRegion.CENTER));
 				s.show();
 				s.center();
 			}
 		});
 
 		mainMenu.add(mItem);
 
 		add(item);
 		add(new SeparatorToolItem());
 		//add(new SeparatorToolItem());
 
 		item = new Button();
 		item.setText("Tools");
 		item.setIconStyle("icon-preferences-wrench");
 
 		mainMenu = new Menu();
 		item.setMenu(mainMenu);
 
 		mItem = new MenuItem();
 		mItem.setText("Edit Common Names");
 		mItem.setIconStyle("icon-text-bold");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (TaxonomyCache.impl.getCurrentTaxon() == null) {
 					Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
 					return;
 				}
 
 				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
 					WindowUtils
 					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
 					return;
 				}
 				
 				TaxonCommonNameEditor editor = new TaxonCommonNameEditor();
 				editor.show();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("Edit Synonyms");
 		mItem.setIconStyle("icon-text-bold");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (TaxonomyCache.impl.getCurrentTaxon() == null) {
 					Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
 					return;
 				}
 
 				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
 					WindowUtils
 					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
 					return;
 				}
 
 				TaxonSynonymEditor editor = new TaxonSynonymEditor();
 				editor.show();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("Attach Image");
 		mItem.setIconStyle("icon-image");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, TaxonomyCache.impl.getCurrentTaxon())) {
 					WindowUtils
 					.errorAlert("Sorry. You do not have sufficient permissions " + "to perform this action.");
 					return;
 				}
 
 				Popup imagePopup = new Popup();
 
 				if (!imagePopup.isRendered()) {
 					ImageManagerPanel imageManager = 
 						new ImageManagerPanel(TaxonomyCache.impl.getCurrentTaxon());
 					imagePopup.add(imageManager);
 				}
 
 				imagePopup.show();
 				imagePopup.center();
 			}
 		});
 		mainMenu.add(mItem);
 
 		// mItem = new MenuItem(Style.PUSH);
 		// mItem.setText("View Bibliography");
 		// mItem.setIconStyle("icon-book-open");
 		// mItem.addSelectionListener(new SelectionListener() {
 		// public void widgetSelected(BaseEvent be) {
 		// DEMToolsPopups.buildBibliographyPopup();
 		// }
 		// });
 		// mainMenu.add(mItem);
 		//
 		// mItem = new MenuItem(Style.PUSH);
 		// mItem.setText("View References By Field");
 		// mItem.setIconStyle("icon-book-open");
 		// mItem.addSelectionListener(new SelectionListener() {
 		// public void widgetSelected(BaseEvent be) {
 		// DEMToolsPopups.buildReferencePopup();
 		// }
 		// });
 		// mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("Manage References");
 		mItem.setIconStyle("icon-book");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				GenericCallback<Object> callback = new GenericCallback<Object>() {
 					public void onFailure(Throwable caught) {
 						startAutosaveTimer();
 						WindowUtils.errorAlert("Error committing changes to the "
 								+ "server. Ensure you are connected to the server, then try " + "the process again.");
 					}
 
 					public void onSuccess(Object result) {
 						startAutosaveTimer();
 						WindowUtils.infoAlert("Successfully committed reference changes.");
 					}
 				};
 				
 				ClientUIContainer.bodyContainer.openReferenceManager(
 						new ReferenceableAssessment(AssessmentCache.impl.getCurrentAssessment()), 
 						"Manage References -- Add to Global References", callback, callback);
 				stopAutosaveTimer();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("View Notes");
 		mItem.setIconStyle("icon-note");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				DEMToolsPopups.buildNotePopup();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setIconStyle("icon-changes");
 		mItem.setText("Changes");
 		mItem.addListener(Events.Select, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 				if( SimpleSISClient.iAmOnline ) {
 					TrackChangesPanel panel = new TrackChangesPanel(AssessmentCache.impl.getCurrentAssessment());
 					panel.show();
 					/*final AssessmentChangesPanel panel = new AssessmentChangesPanel();
 					
 					final Window window = WindowUtils.getWindow(true, false, "Assessment Changes");
 					window.setClosable(true);
 					window.setSize(900, 500);
 					window.setLayout(new FillLayout());
 					
 					panel.draw(new DrawsLazily.DoneDrawingCallback() {
 						public void isDrawn() {
 							window.add(panel);	
 							window.show();	
 						}
 					});*/
 				} else {
 					WindowUtils.errorAlert("Not available offline.", "Sorry, this feature is not " +
 							"available offline.");
 				}
 			}
 		});
 
 		mainMenu.add(mItem);
 
 //		mItem = new MenuItem();
 //		mItem.setIconStyle("icon-comments");
 //		mItem.setText("Comments");
 //		mItem.addListener(Events.Select, new Listener<BaseEvent>() {
 //			public void handleEvent(BaseEvent be) {
 //				Assessment a = AssessmentCache.impl.getCurrentAssessment();
 //				Window alert = WindowUtils.getWindow(false, false, "Assessment #" + a.getId());
 //				LayoutContainer c = alert;
 //				c.setLayout(new FillLayout());
 //				c.setSize(300, 450);
 //				TabItem item = new TabItem();
 //				item.setIconStyle("icon-comments");
 //				item.setText("Comments");
 //				String target = "/comments/browse/assessment/"
 //					+ FilenameStriper.getIDAsStripedPath(a.getId()) + ".comments.xml";
 //				SysDebugger.getInstance().println(target);
 //				item.setUrl(target);
 //				TabPanel tf = new TabPanel();
 //				tf.add(item);
 //				c.add(tf);
 //				alert.show();
 //				return;
 //			}
 //		});
 //		mainMenu.add(mItem);
 		add(item);
 
 		/*
 		 * The three items below are not and will not 
 		 * be ready for SIS 2.0 launch.
 		 */
 		/*
 		mItem = new MenuItem();
 		mItem.setText("View Report");
 		mItem.setIconStyle("icon-report");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				fetchReport();
 			}
 		});
 		mainMenu.add(mItem);
 		
 		final MenuItem integrity = new MenuItem();
 		integrity.setText("Validate Assessment");
 		integrity.setIconStyle("icon-integrity");
 		integrity.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				runIntegrityValidator();
 			}
 		});
 		
 		mainMenu.add(integrity);
 		
 		final MenuItem workflow = new MenuItem();
 		workflow.setText("Submission Process Notes");
 		workflow.setIconStyle("icon-workflow");
 		workflow.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				final WorkflowNotesWindow window = 
 					new WorkflowNotesWindow(AssessmentCache.impl.getCurrentAssessment().getId()+"");
 				window.show();
 			}
 		});
 		
 		mainMenu.add(workflow);*/
 
 		add(new SeparatorToolItem());
 		
 		Button mcbutton = new Button();
 		mcbutton.setText("Manage Credits");
 		mcbutton.setIconStyle("icon-user-group");
 
 		mcbutton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 
 				final ManageCreditsWindow panel = new ManageCreditsWindow();
 				panel.show(); 
 			}
 
 		});
 
 		add(mcbutton);
 		add(new SeparatorToolItem());
 		
 		Button saveMode = new Button("Auto-Save Options"); {
 			MenuItem timedAutoSave = new MenuItem("Timed Auto-Save"); {
 				Menu timedMenu = new Menu();
 				
 				SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						String newPreference = ce.getItem().getData("value");
 						
 						SimpleSISClient.currentUser.setProperty(UserPreferences.AUTO_SAVE_TIMER, newPreference);
 						setAutoSaveInterval(newPreference);
 						resetAutosaveTimer();
 					}
 				};
 				
 				for (String value : new String[] {"-1", "2", "5", "10"}) {
 					CheckMenuItem interval = new CheckMenuItem("-1".equals(value) ? "Off" : "Every " + value + " minutes.");
 					interval.setData("value", value);
 					interval.setGroup(UserPreferences.AUTO_SAVE_TIMER);
 					interval.setChecked("-1".equals(value) ? autoSaveInterval == null : Integer.valueOf(value).equals(autoSaveInterval));
 					interval.addSelectionListener(listener);
 					timedMenu.add(interval);
 				}
 				
 				timedAutoSave.setSubMenu(timedMenu);
 			}
 			MenuItem onPageChange = new MenuItem("On Page Change..."); {
 				Menu pageChangeMenu = new Menu();
 				
 				String savePreference = 
 					SimpleSISClient.currentUser.getPreference(UserPreferences.AUTO_SAVE, UserPreferences.PROMPT);
 				
 				SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						String newPreference = ce.getItem().getData("value");
 						SimpleSISClient.currentUser.setProperty(UserPreferences.AUTO_SAVE, newPreference);
 					}
 				};
 				
 				CheckMenuItem autoSave = new CheckMenuItem("Auto-Save");
 				autoSave.setData("value", UserPreferences.DO_ACTION);
 				autoSave.setGroup(UserPreferences.AUTO_SAVE);
 				autoSave.setChecked(savePreference.equals(UserPreferences.DO_ACTION));
 				autoSave.addSelectionListener(listener);
 				autoSave.setToolTip("When switching pages or assessments, any unsaved changes to an " +
 					"assessment will automatically be saved.");
 				pageChangeMenu.add(autoSave);
 				
 				CheckMenuItem autoPrompt = new CheckMenuItem("Prompt Before Auto-Save");
 				autoPrompt.setData("value", UserPreferences.PROMPT);
 				autoPrompt.setGroup(UserPreferences.AUTO_SAVE);
 				autoPrompt.setChecked(savePreference.equals(UserPreferences.PROMPT));
 				autoPrompt.addSelectionListener(listener);
 				autoPrompt.setToolTip("When switching pages or assessments, you will be prompted " +
 					"to save your changes if any unsaved changes are detected.");
 				pageChangeMenu.add(autoPrompt);
 				
 				CheckMenuItem ignore = new CheckMenuItem("Ignore");
 				ignore.setData("value", UserPreferences.IGNORE);
 				ignore.setGroup(UserPreferences.AUTO_SAVE);
				ignore.setChecked(savePreference.equals(UserPreferences.IGNORE));
 				ignore.addSelectionListener(listener);
 				ignore.setToolTip("When switching pages or assessments, any unsaved changes to an " +
 					"assessment will be thrown away; you will not be prompted to save them, nor " +
 					"will they be automatically saved.  Only clicking the \"Save\" button will save " +
 					"changes.");
 				pageChangeMenu.add(ignore);
 				
 				onPageChange.setSubMenu(pageChangeMenu);
 			}
 			
 			Menu saveModeOptions = new Menu();
 			saveModeOptions.add(onPageChange);
 			saveModeOptions.add(timedAutoSave);
 			
 			saveMode.setMenu(saveModeOptions);
 		}
 		add(saveMode);
 		
 		add(new FillToolItem());
 	}
 	
 	public void resetAutosaveTimer() {
 		autoSave.cancel();
 		startAutosaveTimer();
 	}
 	
 	public void startAutosaveTimer() {
 		if (autoSaveInterval != null && AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, AssessmentCache.impl.getCurrentAssessment())) {
 			Debug.println("Starting autosave.");
 			autoSave.schedule(autoSaveInterval.intValue() * 60 * 1000);
 		}
 	}
 
 	public void stopAutosaveTimer() {
 		Debug.println("Stopping autosave.");
 		autoSave.cancel();
 	}
 	
 	public void setViewOnly(boolean viewOnly) {
 		if (viewOnly) {
 			editViewButton.setText("Edit Data Mode");
 			editViewButton.setIconStyle("icon-unlocked");
 		}
 		else {
 			editViewButton.setText("Read Only Mode");
 			editViewButton.setIconStyle("icon-read-only");
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private void runIntegrityValidator() {
 		final Assessment data = AssessmentCache.impl.getCurrentAssessment();
 		//Popup new window:
 		ClientAssessmentValidator.validate(data.getId(), data.getType());
 	}
 
 	@SuppressWarnings("unused")
 	private void fetchReport() {
 		final CheckBox useLimited = new CheckBox();
 		useLimited.setValue(Boolean.valueOf(true));
 		useLimited.setFieldLabel("Use limited field set (more compact report)");
 		
 		final CheckBox showEmpty = new CheckBox();
 		showEmpty.setFieldLabel("Show empty fields");
 		
 		final FormPanel form = new FormPanel();
 		form.setLabelSeparator("?");
 		form.setLabelWidth(300);
 		form.setFieldWidth(50);
 		form.setHeaderVisible(false);
 		form.setBorders(false);
 		form.add(useLimited);
 		form.add(showEmpty);
 		
 		final Window w = WindowUtils.getWindow(true, false, "Report Options");
 		
 		form.addButton(new Button("Submit", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				Assessment a = AssessmentCache.impl.getCurrentAssessment();
 				String target = "/reports/";
 
 				if (a.getType().equals(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
 					target += "draft/";
 				} else if (a.getType().equals(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
 					target += "published/";
 				} else if (a.getType().equals(AssessmentType.USER_ASSESSMENT_TYPE)) {
 					target += "user/" + SimpleSISClient.currentUser.getUsername() + "/";
 				}
 
 				w.hide();
 				
 				com.google.gwt.user.client.Window.open(target + AssessmentCache.impl.getCurrentAssessment().getId()
 						+ "?empty=" + showEmpty.getValue() + "&limited=" + useLimited.getValue(),
 						"_blank", "");
 			}
 		}));
 		form.addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 			public void componentSelected(ButtonEvent ce) {
 				w.hide();
 			}
 		}));
 		
 		w.add(form);
 		w.setSize(400, 250);
 		w.show();
 		w.center();
 	}
 	
 	
 	private class AutosaveTimer extends Timer {
 		public void run() {
 			if (WindowUtils.loadingBox != null && WindowUtils.loadingBox.isVisible()) {
 				// loading panel is up ... don't shoot!
 				resetAutosaveTimer();
 				return;
 			}
 
 			try {
 				if (!ClientUIContainer.bodyContainer.isAssessmentEditor())
 					return;
 
 				final SISView currentView = ViewCache.impl.getCurrentView();
 				boolean save = currentView != null && currentView.getCurPage() != null &&
 					AssessmentClientSaveUtils.shouldSaveCurrentAssessment(currentView.getCurPage().getMyFields());
 				if (save) {
 					AssessmentClientSaveUtils.saveAssessment(currentView.getCurPage().getMyFields(),
 							AssessmentCache.impl.getCurrentAssessment(), new GenericCallback<Object>() {
 						public void onFailure(Throwable arg0) {
 							WindowUtils.errorAlert("Save Failed", "Failed to save assessment! " + arg0.getMessage());
 							startAutosaveTimer();
 						}
 
 						public void onSuccess(Object arg0) {
 							Info.display("Auto-save Complete", "Successfully auto-saved assessment {0}.",
 									AssessmentCache.impl.getCurrentAssessment().getSpeciesName());
 							startAutosaveTimer();
 							if (saveListener != null)
 								saveListener.handleEvent();
 						}
 					});
 				} else {
 					startAutosaveTimer();
 				}
 			} catch (InsufficientRightsException e) {
 				WindowUtils.errorAlert("Auto-save failed. You do not have sufficient "
 						+ "rights to perform this action.");
 			} catch (NullPointerException e1) {
 				Debug.println(
 						"Auto-save failed, on NPE. Probably logged " + "out and didn't stop the timer. {0}", e1);
 			}
 
 		}
 	}
 	
 }
