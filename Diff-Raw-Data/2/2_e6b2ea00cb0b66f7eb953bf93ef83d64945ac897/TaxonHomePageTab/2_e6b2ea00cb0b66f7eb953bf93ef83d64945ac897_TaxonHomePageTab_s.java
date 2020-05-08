 package org.iucn.sis.client.tabs;
 
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.ui.models.taxa.TaxonListElement;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.PanelManager;
 import org.iucn.sis.client.panels.assessments.NewAssessmentPanel;
 import org.iucn.sis.client.panels.taxa.TaxonTreePopup;
 import org.iucn.sis.client.panels.taxomatic.CreateNewTaxonPanel;
 import org.iucn.sis.client.panels.taxomatic.LateralMove;
 import org.iucn.sis.client.panels.taxomatic.MergePanel;
 import org.iucn.sis.client.panels.taxomatic.MergeUpInfrarank;
 import org.iucn.sis.client.panels.taxomatic.SplitNodePanel;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticAssessmentMover;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticDemotePanel;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticUtils;
 import org.iucn.sis.client.panels.taxomatic.TaxomaticWindow;
 import org.iucn.sis.client.panels.taxomatic.TaxonBasicEditor;
 import org.iucn.sis.client.panels.taxomatic.TaxonCommonNameEditor;
 import org.iucn.sis.client.panels.taxomatic.TaxonSynonymEditor;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableFeature;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class TaxonHomePageTab extends TabItem {
 	private PanelManager panelManager = null;
 	private ToolBar toolBar = null;
 	private Button assessmentTools;
 	private Button taxonToolsItem;
 	private Button taxomaticToolItem;
 
 	private final boolean ENABLE_TAXOMATIC_FEATURES = true;
 
 	/**
 	 * Defaults to having Style.NONE
 	 */
 	public TaxonHomePageTab(PanelManager manager) {
 		super();
 		panelManager = manager;
 
 		build();
 	}
 
 	public void build() {
 		setText("Taxon Home Page");
 
 		BorderLayout layout = new BorderLayout();
 		// layout.setSpacing( 0 );
 		// layout.setMargin( 0 );
 		setLayout(layout);
 
 		toolBar = buildToolBar();
 		BorderLayoutData toolbarData = new BorderLayoutData(LayoutRegion.NORTH);
 		toolbarData.setSize(25);
 
 		BorderLayoutData summaryData = new BorderLayoutData(LayoutRegion.CENTER, .5f, 100, 500);
 		summaryData.setSize(300);
 		BorderLayoutData browserData = new BorderLayoutData(LayoutRegion.EAST, .5f, 100, 500);
 		browserData.setSize(250);
 
 		add(panelManager.taxonomicSummaryPanel, summaryData);
 		// add( panelManager.taxonomyBrowserPanel, browserData );
 		add(toolBar, toolbarData);
 
 		panelManager.taxonomicSummaryPanel.update(null);
 		// panelManager.taxonomyBrowserPanel.update();
 
 		layout();
 	}
 
 	private ToolBar buildToolBar() {
 		ToolBar toolbar = new ToolBar();
 
 		assessmentTools = new Button();
 		assessmentTools.setText("Assessment Tools");
 		assessmentTools.setIconStyle("icon-preferences-wrench");
 
 		MenuItem mItem = new MenuItem();
 		mItem.setText("Goto Most Recent");
 		mItem.setIconStyle("icon-go-jump");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 
 			}
 		});
 
 		mItem = new MenuItem();
 		mItem.setText("Assess Current Taxon");
 		mItem.setIconStyle("icon-new-document");
 
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				if (TaxonomyCache.impl.getCurrentTaxon().getFootprint().length < TaxonLevel.SPECIES) {
 					WindowUtils.errorAlert("You must select a species or lower taxa to assess.  "
 							+ "You can select a different taxon using the navigator, the search function, "
 							+ " or the browser.");
 				} else {
 					Window shell = WindowUtils.getWindow(true, false, "New "
 							+ TaxonomyCache.impl.getCurrentTaxon().getFriendlyName() + " Assessment");
 					shell.setLayout(new FillLayout());
 					shell.setSize(550, 250);
 					shell.add(new NewAssessmentPanel(panelManager));
 					shell.show();
 				}
 
 			}
 		});
 
 		Menu mainMenu = new Menu();
 		/*
 		 * Menu subMenu = new Menu();
 		 * 
 		 * MenuItem subMItem = new MenuItem();
 		 * subMItem.setIconStyle("icon-copy");
 		 * subMItem.setText("Using This Data"); subMItem.addListener(
 		 * Events.Select, new Listener() { public void handleEvent(BaseEvent be)
 		 * { AssessmentCache.impl.createNewUserAssessment( true ); } });
 		 * subMenu.add( subMItem );
 		 * 
 		 * subMItem = new MenuItem(); subMItem.setIconStyle("icon-copy");
 		 * subMItem.setText("From Scratch"); subMItem.addListener(
 		 * Events.Select, new Listener() { public void handleEvent(BaseEvent be)
 		 * { AssessmentCache.impl.createNewUserAssessment( false ); } });
 		 * subMenu.add( subMItem );
 		 * 
 		 * mItem.setSubMenu( subMenu ); mainMenu.add(mItem);
 		 */
 		mainMenu.add(mItem);
 		assessmentTools.setMenu(mainMenu);
 
 		toolbar.add(assessmentTools);
 		toolbar.add(new SeparatorToolItem());
 
 		taxonToolsItem = new Button();
 		taxonToolsItem.setText("Taxon Tools");
 		taxonToolsItem.setIconStyle("icon-preferences-wrench-orange");
 
 		mainMenu = new Menu();
 		taxonToolsItem.setMenu(mainMenu);
 
 		// mainMenu.add( mItem );
 
 		mItem = new MenuItem();
 		mItem.setText("View/Attach Note");
 		mItem.setIconStyle("icon-note");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				panelManager.taxonomicSummaryPanel.buildNotePopup();
 			}
 		});
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("View/Attach Reference");
 		mItem.setIconStyle("icon-book");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				panelManager.taxonomicSummaryPanel.buildReferencePopup();
 
 			}
 		});
 		mainMenu.add(mItem);
 
 		toolbar.add(taxonToolsItem);
 		toolbar.add(new SeparatorToolItem());
 
 		// BEGIN TAXOMATIC FEATURES
 		if (ENABLE_TAXOMATIC_FEATURES) {
 			taxomaticToolItem = new Button();
 			taxomaticToolItem.setText("Taxomatic Tools");
 			taxomaticToolItem.setIconStyle("icon-preferences-wrench-green");
 			mainMenu = new Menu();
 			taxomaticToolItem.setMenu(mainMenu);
 
 			mItem = new MenuItem();
 			mItem.setText("Edit Taxon");
 			mItem.setIconStyle("icon-note-edit");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new TaxonBasicEditor(panelManager));
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Edit Synonyms");
 			mItem.setIconStyle("icon-note-edit");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new TaxonSynonymEditor());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Edit Common Names");
 			mItem.setIconStyle("icon-note-edit");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new TaxonCommonNameEditor(panelManager));
 				}
 			});
 			mainMenu.add(mItem);
 
 			// TODO: Decide if need to guard against deprecated nodes
 			mItem = new MenuItem();
 			mItem.setText("Add New Child Taxon");
 			mItem.setIconStyle("icon-new-document");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					Taxon curNode = TaxonomyCache.impl.getCurrentTaxon();
 
 					if (curNode != null) {
 						if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.CREATE, curNode)) {
 							CreateNewTaxonPanel panel = new CreateNewTaxonPanel(TaxonomyCache.impl.getCurrentTaxon());
 							panel.setHeading("Add New Child Taxon");
 							panel.show();
 						}
 						else
 							WindowUtils.errorAlert("Insufficient Permission", "Sorry. You do not have create permissions for this taxon.");
 					} else {
 						WindowUtils.errorAlert("Please select a taxon to attach to.");
 					}
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Lateral Move");
 			mItem.setIconStyle("icon-lateral-move");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					popupChooser(new LateralMove(panelManager));
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Promote Taxon");
 			mItem.setIconStyle("icon-promote");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					final Taxon currentNode = TaxonomyCache.impl.getCurrentTaxon();
 					if (currentNode == null)  //Not possible??
 						WindowUtils.errorAlert("Please first select a taxon");
 					else if (currentNode.getLevel() != TaxonLevel.INFRARANK)
 						WindowUtils.errorAlert("You may only promote infraranks.");
 					else {
 						String message = "<b>Instructions:</b> By promoting " + currentNode.getFullName() + ", "
 							+ currentNode.getFullName() + " will become a species " + " and will have the same parent that "
 							+ currentNode.getParentName() + " has.";
 						
 						WindowUtils.confirmAlert("Confirm", message, new WindowUtils.SimpleMessageBoxListener() {
 							public void onYes() {
 								TaxomaticUtils.impl.performPromotion(currentNode, new GenericCallback<String>() {
 									public void onFailure(Throwable arg0) {
 										//Error message handled via default callback
 									}
 									public void onSuccess(String arg0) {
 										WindowUtils.infoAlert("Success", currentNode.getName() + " has successfully been promoted.");
 									}
 								});
 							}
 						}, "OK", "Cancel");
 					}
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setText("Demote Taxon");
 			mItem.setIconStyle("icon-demote");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					if (node == null || node.getLevel() != TaxonLevel.SPECIES)
 						WindowUtils.infoAlert("Not allowed", "You can only demote a species.");
 					else
 						popupChooser(new TaxomaticDemotePanel(panelManager));
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-merge");
 			mItem.setText("Merge Taxa");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					if (node != null)
 						popupChooser(new MergePanel());
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-merge-up");
 			mItem.setText("Merge Up Subspecies");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 
 				@Override
 				public void componentSelected(MenuEvent ce) {
 					final Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					if (node == null || node.getLevel() != TaxonLevel.SPECIES) {
 						WindowUtils.infoAlert("Not Allowed", "You can only merge subspecies into a species, please "
 								+ "visit the species you wish to merge a subspecies into.");
 					} else {
 						TaxonomyCache.impl.getTaxonChildren(node.getId() + "", new GenericCallback<List<Taxon>>() {
 							public void onFailure(Throwable caught) {
 								WindowUtils.infoAlert("Error", "There was an internal error while trying to "
 										+ "fetch the children of " + node.getFullName());
 							}
 							public void onSuccess(List<Taxon> list) {
 								boolean show = false;
 								for (Taxon childNode : list) {
 									if (childNode.getLevel() == TaxonLevel.INFRARANK) {
 										show = true;
 										break;
 									}
 								}
 								if (show) {
 									popupChooser(new MergeUpInfrarank());
 								} else {
 									WindowUtils.infoAlert("Not Allowed", node.getFullName()
 											+ " does not have any subspecies to promote.  " 
 											+ "You can only merge subspecies with their parent.");
 								}
 
 							}
 						});
 					}
 
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-split");
 			mItem.setText("Split Taxon");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					// TODO:
 					//Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					// if( !node.isDeprecatedStatus() )
 					popupChooser(new SplitNodePanel());
 					// else
 					// WindowUtils.errorAlert("Error",
 					// "Taxon selected for merging is not a valid taxon" +
 					// " (i.e. status is not A or U).");
 
 				}
 			});
 			mainMenu.add(mItem);
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-remove");
 			mItem.setText("Remove Taxon");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					final Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 					TaxonTreePopup.fetchChildren(node, new GenericCallback<List<TaxonListElement>>() {
 						public void onFailure(Throwable caught) {
 							String msg = "If this taxon has assessments, these will be moved to the trash as well. Move"
 										+ node.generateFullName() + " to the trash?";
 							
 							WindowUtils.confirmAlert("Confirm Delete", msg, new WindowUtils.SimpleMessageBoxListener() {
 								public void onYes() {
 									final Taxon taxon = TaxonomyCache.impl.getCurrentTaxon();
 									if (taxon != null) {
 										TaxomaticUtils.impl.deleteTaxon(taxon, new GenericCallback<String>() {
 											public void onSuccess(String result) {
 												TaxonomyCache.impl.clear();
 												TaxonomyCache.impl.evict(taxon.getParentId() + "," + taxon.getId());
 												TaxonomyCache.impl.fetchTaxon(taxon.getParentId(), true,
 														new GenericCallback<Taxon>() {
 													public void onFailure(Throwable caught) {
 														ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(null);
 														panelManager.recentAssessmentsPanel.update();
 													};
 													public void onSuccess(Taxon result) {
 														ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
 															.update(TaxonomyCache.impl.getCurrentTaxon().getId());
 														panelManager.recentAssessmentsPanel.update();
 													};
 												});
 											}
 											public void onFailure(Throwable caught) {
 												close();
 											}
 										});
 									}
 								}
 
 							});
 						}
 
 						public void onSuccess(List<TaxonListElement> result) {
 							WindowUtils.infoAlert("You cannot remove this Taxa without first removing its children.");
 							// ((List<ModelData>) result).size();
 
 						}
 					});
 				}
 			});
 			mainMenu.add(mItem);
 
 			// END TAXOMATIC FEATURES
 
 			toolbar.add(taxomaticToolItem);
 			toolbar.add(new SeparatorToolItem());
 
 			mItem = new MenuItem();
 			mItem.setIconStyle("icon-undo");
 			mItem.setText("Undo Taxomatic Operation");
 			mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 				@Override
 				public void componentSelected(MenuEvent ce) {
 					final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 					doc.getAsText(UriBase.getInstance().getSISBase() + "/taxomatic/undo", new GenericCallback<String>() {
 
 						public void onFailure(Throwable caught) {
 							WindowUtils.infoAlert("Unable to undo last operation", "SIS is unable to undo the last "
 									+ "taxomatic operation "
 									+ "because you are not the last user to perform a taxomatic "
 									+ "change, or there has not been a taxomatic operation to undo.");
 
 						}
 
 						public void onSuccess(String result) {
 							WindowUtils.confirmAlert("Undo Last Taxomatic Operation", doc.getText()
 									+ "  Are you sure you want to undo this operation?",
 									new WindowUtils.SimpleMessageBoxListener() {
 								public void onYes() {
 									final NativeDocument postDoc = SimpleSISClient.getHttpBasicNativeDocument();
 									postDoc.post(UriBase.getInstance().getSISBase() +"/taxomatic/undo", "", new GenericCallback<String>() {
 										public void onFailure(Throwable caught) {
 											WindowUtils.errorAlert(
 												"Unable to undo the last operation.  " +
 												"Please undo the operation manually."
 											);
 										}
 										public void onSuccess(String result) {
 											Taxon currentNode = TaxonomyCache.impl.getCurrentTaxon();
 											TaxonomyCache.impl.clear();
 											TaxonomyCache.impl.fetchTaxon(currentNode.getId(), true,
 													new GenericCallback<Taxon>() {
 												public void onFailure(Throwable caught) {
 													WindowUtils	.infoAlert("Success",
 													"Successfully undid the last taxomatic operation, " +
 													"but was unable to refresh the current taxon.");
 												}
 												public void onSuccess(Taxon result) {
 													ClientUIContainer.bodyContainer.refreshBody();
 													WindowUtils.infoAlert("Success",
 													"Successfully undid the last taxomatic operation.");
 												}
 											});
 										}
 									});
 								}
 							});
 						}
 					});
 				}
 			});
 		}
 		mainMenu.add(mItem);
 
 		mItem = new MenuItem();
 		mItem.setText("Move Assessments");
 		mItem.setIconStyle("icon-document-move");
 		mItem.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				popupChooser(new TaxomaticAssessmentMover(TaxonomyCache.impl.getCurrentTaxon()));
 			}
 		});
 		
 
 		mainMenu.add(mItem);
 
 		return toolbar;
 	}
 
 	private void popupChooser(TaxomaticWindow chooser) {
 		chooser.show();
		chooser.center();
 	}
 
 	public void setAppropriateRights(Taxon node) {
 		
 		if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 			Info.display("Insufficient Rights", "Notice: You do not have "
 					+ "sufficient permissions to edit this taxon.");
 			taxonToolsItem.hide();
 			taxomaticToolItem.hide();
 		} else if (!AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.USE_FEATURE, AuthorizableFeature.TAXOMATIC_FEATURE)) {
 			taxonToolsItem.show();
 			taxomaticToolItem.hide();
 		} else {
 			taxonToolsItem.show();
 			taxomaticToolItem.show();
 		}
 		
 		layout();
 	}
 
 }
