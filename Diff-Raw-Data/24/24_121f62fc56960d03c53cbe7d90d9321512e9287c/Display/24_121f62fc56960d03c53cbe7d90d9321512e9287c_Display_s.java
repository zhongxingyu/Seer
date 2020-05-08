 package org.iucn.sis.shared.api.displays;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.iucn.sis.client.api.assessment.AssessmentClientSaveUtils;
 import org.iucn.sis.client.api.assessment.FieldAttachmentWindow;
 import org.iucn.sis.client.api.assessment.ReferenceableField;
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.DefinitionCache;
 import org.iucn.sis.client.api.caches.NotesCache;
 import org.iucn.sis.client.api.caches.SchemaCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.ui.notes.NoteAPI;
 import org.iucn.sis.client.api.ui.notes.NotesWindow;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.acl.InsufficientRightsException;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.acl.feature.AuthorizableDraftAssessment;
 import org.iucn.sis.shared.api.acl.feature.AuthorizablePublishedAssessment;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.data.DefinitionPanel;
 import org.iucn.sis.shared.api.data.DisplayData;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Relationship;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.structures.DisplayStructure;
 import org.iucn.sis.shared.api.structures.SISRelatedStructures;
 import org.iucn.sis.shared.api.structures.SISStructureCollection;
 import org.iucn.sis.shared.api.structures.Structure;
 import org.iucn.sis.shared.api.utils.clipboard.Clipboard;
 import org.iucn.sis.shared.api.utils.clipboard.UsesClipboard;
 
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.IconButtonEvent;
 import com.extjs.gxt.ui.client.event.MenuEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.button.IconButton;
 import com.extjs.gxt.ui.client.widget.menu.Menu;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.StyledHTML;
 import com.solertium.util.portable.XMLWritingUtils;
 
 /**
  * Display.java
  * 
  * Holds references to data Structure objects, with this abstraction allowing
  * for a consistent way to store data and get their values. Instantiating
  * classes differ by how they display their data to the user, and implement this
  * into the show() function
  * 
  * @author carl.scott
  */
 @SuppressWarnings("unchecked")
 public abstract class Display implements Referenceable {
 	
 	private enum BatchChangeMode {
 		APPEND, OVERWRITE, OVERWRITE_IF_BLANK
 	}
 
 	public static final String VERTICAL = "vertical";
 	public static final String HORIZONTAL = "horizontal";
 	public static final String TABLE = "table";
 
 	protected Field field;
 	
 	//protected Panel dockPanel;
 	// UI Display Vars
 	protected List<DisplayStructure> myStructures; // List of Structures
 	//protected ComplexPanel displayPanel; // The panel to add a display to
 	protected HorizontalPanel iconPanel;
 	protected Image infoIcon = null;
 	protected Image helpIcon = null;
 	protected Image notesIcon = null;
 	protected Image refIcon = null;
 	protected Image viewRefIcon = null;
 
 	protected Menu optionsMenu = null;
 	// Data Vars
 	protected String structure = ""; // what type of input element is this?
 	protected String description; // the input prompt
 	protected String canonicalName; // the canonical name - unique identifier!
 	protected String classOfService; // the class of service
 	protected Object data; // data specific to the field (string, hashmap,
 	// arraylist)
 	protected String groupName; // for radio buttons, group 'em, depricated
 	protected String location; // for complex structures
 	protected String displayID; // id num for the field
 	protected String associatedFieldId; // id this field is associated with
 
 	protected int dominantStructureIndex = 0;
 	
 	private IDAssigner assigner;
 	private ReferenceableFieldFactory referenceableFieldFactory;
 
 	/**
 	 * Creates a new Display by instantiating an ArrayList of Structures
 	 */
 	public Display() {
 		this("", "", null, "", "", "", "", "");
 	}
 
 	public Display(DisplayData displayData) {
 		this(displayData.getStructure(), displayData.getDescription(), displayData.getData(), "", displayData
 				.getDisplayId(), displayData.getCanonicalName(), displayData.getClassOfService(), "");
 	}
 
 	public Display(String struct, String descript, Object data, String group, String displayID, String canonicalName,
 			String classOfService, String associate) {
 		myStructures = new ArrayList<DisplayStructure>();
 
 		this.structure = struct;
 		this.description = descript;
 		this.data = data;
 		this.groupName = group;
 		this.displayID = displayID;
 		this.canonicalName = canonicalName;
 		this.classOfService = classOfService;
 		this.associatedFieldId = associate;
 		
 		assigner = new IDAssigner() {
 			public void assignID(Field field, final GenericCallback<Object> callback) {
 				Assessment assessment = AssessmentCache.impl.getCurrentAssessment();
 				initializeField();
 				assessment.getField().add(field);
 				
 				try {
 					AssessmentClientSaveUtils.saveAssessment(null, assessment, new GenericCallback<Object>() {
 						public void onSuccess(Object result) {
 							callback.onSuccess(result);
 						}
 						public void onFailure(Throwable caught) {
 							callback.onFailure(caught);
 						}
 					});
 				} catch (InsufficientRightsException e) {
 					callback.onFailure(e);
 				}
 			}
 		};
 		
 		referenceableFieldFactory = new ReferenceableFieldFactory() {
 			public Referenceable newReferenceableField(Field field) {
 				return new ReferenceableField(field);
 			}
 		};
 	}
 	
 	public void setAssigner(IDAssigner assigner) {
 		this.assigner = assigner;
 	}
 	
 	public void setReferenceableFieldFactory(
 			ReferenceableFieldFactory referenceableFieldFactory) {
 		this.referenceableFieldFactory = referenceableFieldFactory;
 	}
 
 	@Override
 	public void addReferences(ArrayList<Reference> references, final GenericCallback<Object> callback) {
 		Referenceable referenceableField = referenceableFieldFactory.newReferenceableField(field);
 		referenceableField.addReferences(references, new GenericCallback<Object>() {
 			public void onSuccess(Object result) {
 				if (field != null && field.getReference().size() == 0)
 					refIcon.setUrl("images/icon-book-grey.png");
 				else
 					refIcon.setUrl("images/icon-book.png");
 				
 				callback.onSuccess(result);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 
 	public void addStructure(DisplayStructure structureToAdd) {
 		myStructures.add(structureToAdd);
 	}
 
 	private VerticalPanel buildDefintionPanel() {
 		VerticalPanel panelContainer = new VerticalPanel();
 		panelContainer.setSpacing(3);
 
 		ArrayList<String> found = new ArrayList<String>();
 
 		String lowerCaseDesc = description.toLowerCase();
 
 		for (String curDefinable : DefinitionCache.impl.getDefinables()) {
 			if (lowerCaseDesc.indexOf(curDefinable) >= 0 && !found.contains(curDefinable))
 				found.add(curDefinable);
 		}
 
 		if (found.size() == 0)
 			panelContainer.add(new HTML("No definable terms were found."));
 		else {
 			final DefinitionPanel dPanel = new DefinitionPanel();
 
 			panelContainer.add(new HTML("Definable terms:"));
 			for (final String curDef : found) {
 				HTML curDefHTML = new HTML(curDef);
 				curDefHTML.addStyleName("SIS_HyperlinkLookAlike");
 				curDefHTML.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						dPanel.updateContent(curDef);
 					}
 				});
 
 				panelContainer.add(curDefHTML);
 			}
 
 			panelContainer.add(dPanel);
 		}
 		return panelContainer;
 	}
 
 	public void checkSecurity() {
 		// ModuleController.getSecurityManager().setUserDefaultDisplayVisibility(
 		// this, SecurityManager.DISPLAY_LEVEL);
 	}
 
 	public void disableStructures() {
 		for (int i = 0; i < myStructures.size(); i++)
 			myStructures.get(i).disable();
 	}
 
 	public void enableStructures() {
 		for (int i = 0; i < myStructures.size(); i++)
 			myStructures.get(i).enable();
 	}
 
 	protected abstract Widget generateContent(boolean viewOnly);
 
 	public String getCanonicalName() {
 		return canonicalName;
 	}
 
 	public String getClassOfService() {
 		return classOfService;
 	}
 
 	/**
 	 * Builds the clipboard context menu.
 	 * 
 	 * @param clipList
 	 *            the list of structures *that implement UsesClipboard*. You
 	 *            should NEVER put an object in this list that does not
 	 *            implement UsesClipboard.
 	 * @see getStructuresThatUseClipboard()
 	 * @return the menu the context menu
 	 */
 	private Menu getClipboardMenu(ArrayList<Structure> clipList) {
 		Menu menu = new Menu();
 
 		MenuItem open = new MenuItem();
 		open.setText("Open Clipboard");
 		open.setIconStyle("icon-open-folder");
 		open.addSelectionListener(new SelectionListener<MenuEvent>() {
 			@Override
 			public void componentSelected(MenuEvent ce) {
 				Clipboard.getInstance().show();
 			}
 		});
 
 		MenuItem copyFrom = new MenuItem();
 		copyFrom.setIconStyle("icon-copy");
 		copyFrom.setText("Copy text from...");
 		{
 			Menu subMenu = new Menu();
 			for (int i = 0; i < clipList.size(); i++) {
 				try {
 					final UsesClipboard struct = (UsesClipboard) clipList.get(i);
 					MenuItem item = new MenuItem();
 					item.setText(((Structure) clipList.get(i)).getDescription());
 					item.addSelectionListener(new SelectionListener<MenuEvent>() {
 						@Override
 						public void componentSelected(MenuEvent ce) {
 							struct.copyToClipboard();
 						}
 					});
 					subMenu.add(item);
 				} catch (Exception e) {
 					continue;
 				}
 			}
 
 			if (subMenu.getItemCount() == 0) {
 				MenuItem empty = new MenuItem();
 				empty.setText("[none available]");
 				subMenu.add(empty);
 			}
 
 			copyFrom.setSubMenu(subMenu);
 		}
 
 		MenuItem pasteTo = new MenuItem();
 		pasteTo.setIconStyle("icon-paste");
 		pasteTo.setText("Paste text to...");
 		{
 			Menu subMenu = new Menu();
 
 			if (Clipboard.getInstance().isEmpty()) {
 				MenuItem empty = new MenuItem();
 				empty.setText("[clipboard empty]");
 				subMenu.add(empty);
 			} else {
 				for (int i = 0; i < clipList.size(); i++) {
 					try {
 						final UsesClipboard struct = (UsesClipboard) clipList.get(i);
 						MenuItem item = new MenuItem();
 						item.setText((clipList.get(i)).getDescription());
 						item.addSelectionListener(new SelectionListener<MenuEvent>() {
 							@Override
 							public void componentSelected(MenuEvent ce) {
 								Clipboard.getInstance().pasteConditions(new Clipboard.ClipboardPasteCallback() {
 									@Override
 									public void onPaste(ArrayList items) {
 										struct.pasteFromClipboard(items);
 									}
 								});
 							}
 						});
 						subMenu.add(item);
 					} catch (Exception e) {
 						continue;
 					}
 				}
 			}
 
 			if (subMenu.getItemCount() == 0) {
 				MenuItem empty = new MenuItem();
 				empty.setText("[none available]");
 				subMenu.add(empty);
 			}
 
 			pasteTo.setSubMenu(subMenu);
 		}
 
 		menu.add(open);
 		menu.add(copyFrom);
 		menu.add(pasteTo);
 
 		return menu;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 	
 	public String getDescription(String defaultValue) {
 		return description == null ? defaultValue : description;
 	}
 
 	public int getDominantStructureIndex() {
 		if (dominantStructureIndex < 0 || dominantStructureIndex > myStructures.size())
 			return this.dominantStructureIndex;
 		else
 			return 0;
 	}
 
 	public Field getField() {
 		return field;
 	}
 	
 	/**
 	 * Returns the ID of ths Trackable object implements Trackable
 	 * 
 	 * @return the id
 	 */
 	public String getID() {
 		return displayID;
 	}
 
 	public ArrayList<Widget> getMyWidgets() {
 		ArrayList<Widget> retWidgets = new ArrayList<Widget>();
 		for (int i = 0; i < this.myStructures.size(); i++) {
 			retWidgets.add(this.myStructures.get(i).generate());
 		}
 		Debug.println(
 				"Returned " + myStructures.size() + " widgets to show for " + this.description);
 		return retWidgets;
 	}
 
 	public Widget getNoPermissionPanel() {
 		return new SimplePanel();
 	}
 
 	public Set<Reference> getReferencesAsList() {
 		if (!isSaved())
 			return new HashSet<Reference>();
 		
 		return referenceableFieldFactory.newReferenceableField(field).getReferencesAsList();
 	}
 	
 	@Override
 	public ReferenceGroup groupBy() {
 		return ReferenceGroup.Field;
 		//TODO: use this? return referenceableFieldFactory.newReferenceableField(field).groupBy();
 	}
 
 	public List<DisplayStructure> getStructures() {
 		return myStructures;
 	}
 
 	/**
 	 * Looks in the display's structures and locates structures that implement
 	 * UsesClipboard
 	 * 
 	 * @return the list
 	 */
 	private ArrayList<DisplayStructure> getStructuresThatUseClipboard(List<DisplayStructure> structures) {
 		ArrayList<DisplayStructure> list = new ArrayList<DisplayStructure>();
 		for (DisplayStructure structure : structures) {
 			if (structure instanceof SISRelatedStructures) {
 				if (((SISRelatedStructures) structure).getDominantStructure() instanceof UsesClipboard)
 					list.add(((SISRelatedStructures) structure).getDominantStructure());
 
 				list.addAll(getStructuresThatUseClipboard(
 					((SISRelatedStructures) structure).getDependantStructures()
 				));
 			} else if (structure instanceof SISStructureCollection) {
 				list.addAll(getStructuresThatUseClipboard(
 					((SISStructureCollection) structure).getStructures()
 				));
 			} else if (structure instanceof UsesClipboard)
 				list.add(structure);
 		}
 		return list;
 	}
 	
 	protected void initializeField() {
 		if (AssessmentCache.impl.getCurrentAssessment() != null)
 			field = AssessmentCache.impl.getCurrentAssessment().getField(canonicalName);
 		
 		if (field == null)
 			field = new Field(canonicalName, null);
 	}
 
 	public abstract boolean hasChanged();
 	
 	public abstract void save();
 	
 	/********* DISPLAY VISIBILITY ************/
 
 	public void hideStructures() {
 		iconPanel.setVisible(false);
 		for (int i = 0; i < myStructures.size(); i++)
 			myStructures.get(i).hide();
 	}
 
 	public void onReferenceChanged(GenericCallback<Object> callback) {
 		/*
 		 * The reference changed, I don't think this requires us 
 		 * saving the assessment...
 		 */
 		/*try {
 			AssessmentClientSaveUtils.saveAssessment(AssessmentCache.impl.getCurrentAssessment(), callback);
 		} catch (InsufficientRightsException e) {
 			WindowUtils.errorAlert("Insufficient Permissions", "You do not have "
 					+ "permission to modify this assessment. The changes you " + "just made will not be saved.");
 		}*/
 	}
 	
 	private void assignIDToField(final GenericCallback<Object> callback) {
 		if (isSaved())
 			callback.onSuccess(null);
 		
 		final WindowUtils.MessageBoxListener listener = new WindowUtils.SimpleMessageBoxListener() {
 			public void onYes() {
 				assigner.assignID(field, new GenericCallback<Object>() {
 					public void onSuccess(Object result) {
 						Debug.println("New ID assigned to field {0}: {1}", canonicalName, field.getId());
 						callback.onSuccess(result);
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Could not load field information, " +
 							"please try saving this assessment first before continuing.");
 						callback.onFailure(caught);
 					}
 				});
 			}
 		};
 		
 		//WindowUtils.confirmAlert("Confirm", "You must save your changes before continuing.  Proceed?", listener);
 		//Do we want to prompt before doing a server trip?
 		listener.onYes();
 	}
 
 	protected void openEditViewNotesPopup() {
 		final GenericCallback<Object> callback = new GenericCallback<Object>() {
 			public void onSuccess(Object result) {
 				Debug.println("Field saved, now it has an ID of {0}", field.getId());
 				NotesWindow window = new NotesWindow(createNoteAPI());
 				window.show();
 			}
 			public void onFailure(Throwable caught) {
 				//methinks this is already alerted elsewhere...
 			}
 		};
 		
 		if (isSaved())
 			callback.onSuccess(null);
 		else
 			assignIDToField(callback);
 	}
 	
 	private NoteAPI createNoteAPI() {
 		return new FieldNotes(field) {
 			public void onClose(List<Notes> list) {
 				if (list == null || list.isEmpty())
 					notesIcon.setUrl("images/icon-note-grey.png");
 				else
 					notesIcon.setUrl("images/icon-note.png");
 			}
 		};
 	}
 
 	private void rebuildIconPanel() {
 		if (iconPanel == null)
 			return;
 		
 		iconPanel.clear();
 		iconPanel.setSpacing(5);
 
 		/* This code adds the Clipboard icon to the display mini-menuCS */
 		final ArrayList clipList = getStructuresThatUseClipboard(myStructures);
 		if (!clipList.isEmpty()) {
 			Button iconButton = new Button();
 			iconButton.setIconStyle("icon-paste");
 			iconButton.setToolTip(new ToolTipConfig("Clipboard",
 					"You can use the clipboard to copy and paste text <br/>"
 							+ "from one field to another in SIS, where supported. <br/>"
 							+ "The clipboard can hold multiple pieces of text at a time."));
 			iconButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
 				@Override
 				public void componentSelected(ButtonEvent ce) {
 					getClipboardMenu(clipList).show(ce.getComponent());
 				}
 			});
 
 			iconPanel.add(iconButton);
 		}
 		iconPanel.add(notesIcon);
 		
 		
 		
 		if (field != null && !field.getReference().isEmpty())
 			refIcon.setUrl("images/icon-book.png");
 		else
 			refIcon.setUrl("images/icon-book-grey.png");
 
 		iconPanel.add(refIcon);
 		// iconPanel.add(new Label("References"));
 		iconPanel.add(helpIcon);
 		// iconPanel.add(new Label("Help"));
 	}
 
 	public void removeReferences(ArrayList<Reference> references, final GenericCallback<Object> callback) {
 		Referenceable referenceableField = referenceableFieldFactory.newReferenceableField(field);
 		referenceableField.removeReferences(references, new GenericCallback<Object>() {
 			public void onSuccess(Object result) {
 				if (field != null && field.getReference().size() == 0)
 					refIcon.setUrl("images/icon-book-grey.png");
 				else
 					refIcon.setUrl("images/icon-book.png");
 					
 				callback.onSuccess(result);
 			}
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 
 	public abstract void removeStructures();
 
 	/*
 	 * public void revert() { for (int i = 0; i < myStructures.size(); i++) {
 	 * ((Structure)myStructures.get(i)).revert(); } }
 	 * 
 	 * public void save() { for (int i = 0; i < myStructures.size(); i++) {
 	 * ((Structure)myStructures.get(i)).save(); } }
 	 */
 
 	public void setCanonicalName(String canonicalName) {
 		this.canonicalName = canonicalName;
 	}
 
 	public void setClassOfService(String classOfService) {
 		this.classOfService = classOfService;
 	}
 	
 	public final void setData(Field field) {
 		setField(field);
 		rebuildIconPanel();
 	}
 
 	public abstract void setField(Field field);
 	
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public void setDisplayID(String displayID) {
 		this.displayID = displayID;
 	}
 
 	public void setDominantStructureIndex(int index) {
 		this.dominantStructureIndex = index;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public void setStructure(String structure) {
 		this.structure = structure;
 	}
 
 	protected void setupIconPanel() {
 		if (iconPanel != null)
 			return;
 
 		iconPanel = new HorizontalPanel();
 		iconPanel.setStylePrimaryName("SIS_iconPanel");
 		iconPanel.setSpacing(1);
 
 		if (canonicalName == null || canonicalName.equals(""))
 			return;
 
 		if (refIcon == null) {
 			refIcon = new Image();
 			refIcon.setStyleName("SIS_iconPanelIcon");
 			refIcon.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					final GenericCallback<Object> callback = new GenericCallback<Object>() {
 						public void onSuccess(Object result) {
 							GenericCallback<Object> listener = new GenericCallback<Object>() {
 								public void onFailure(Throwable caught) {
 									WindowUtils.errorAlert("Error!", "Error committing changes to the "
 											+ "server. Ensure you are connected to the server, then try " + "the process again.");
 								}
 			
 								public void onSuccess(Object result) {
 									rebuildIconPanel();
 								}
 							};
 							SISClientBase.getInstance().onShowReferenceEditor("Add a references to " + canonicalName, 
 									Display.this, listener, listener);
 						}
 						public void onFailure(Throwable caught) {
 						}
 					};
 					
 					if (!isSaved())
 						assignIDToField(callback);
 					else
 						callback.onSuccess(null);
 				}
 			});
 		}
 		
 		helpIcon = new Image("images/icon-help.png");
 		helpIcon.setStyleName("SIS_iconPanelIcon");
 		helpIcon.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				Window s = WindowUtils.newWindow("Definitions for " + canonicalName, null, false, true);
 				s.setScrollMode(Scroll.AUTO);
 				s.add(buildDefintionPanel());
 				s.setSize(400, 400);
 
 				s.show();
 				s.center();
 			}
 		});
 
 		List<Notes> notes = NotesCache.impl.getNotesForCurrentAssessment(field);
 		if (notes == null || notes.isEmpty()) {
 			notesIcon = new Image("images/icon-note-grey.png");
 		} else {
 			notesIcon = new Image("images/icon-note.png");
 		}
 
 		notesIcon.setStyleName("SIS_iconPanelIcon");
 		notesIcon.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				openEditViewNotesPopup();
 			}
 		});
 		rebuildIconPanel();
 	}
 
 	/**
 	 * Display in the proper form
 	 * 
 	 * @return the panel
 	 */
 	public Widget showDisplay() {
 		return this.showDisplay(false);
 	}
 
 	/**
 	 * Sets the protected class member dockPanel to be the desired Panel. This method is
 	 * expected to return the dockPanel object.
 	 *
 	 * NOTE: If you override this method, MAKE SURE YOU INVOKE setupIconPanel() before you
 	 * add the iconPanel.
 	 *  
 	 * @param viewOnly - whether or not to build the Panel in view only mode
 	 * @return the dockPanel class member
 	 */
 	protected Widget showDisplay(boolean viewOnly) {
 		try {
 			setupIconPanel();
 			
 			boolean canRemoveDescription = myStructures.size() == 1 && 
 				getDescription("").equals(myStructures.get(0).getDescription());
 			String description = canRemoveDescription ? getDescription() : "";
 			
 			final Grid grid = new Grid(1, 2);
 			grid.addStyleName("page_assessment_body_fieldGrid");
 			grid.setWidth("100%");
 			grid.setWidget(0, 0, new StyledHTML(description, "page_assessment_body_fieldName"));
 			grid.setWidget(0, 1, getMenuIcon());
 			grid.getCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
 			grid.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
 			
 			DockPanel myPanel = new DockPanel();
 			myPanel.setSize("100%", "100%");
 			myPanel.clear();
 
 			myPanel.add(grid, DockPanel.NORTH);
 			myPanel.add(generateContent(viewOnly), DockPanel.CENTER);
 			return myPanel;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new VerticalPanel();
 			
 		}
 
 		//return dockPanel;
 	}
 	
 	protected Widget getMenuIcon() {
 		IconButton icon = new IconButton("icon-gear");
 		icon.addSelectionListener(new SelectionListener<IconButtonEvent>() {
 			public void componentSelected(IconButtonEvent ce) {
 				buildOptionsMenu();
 				
 				optionsMenu.show(ce.getIconButton());
 			}
 		});
 		
 		return icon;
 	}
 	
 	protected void buildOptionsMenu() {
 		boolean hasAssessment = AssessmentCache.impl.getCurrentAssessment() != null;
 		
 		optionsMenu = new Menu();
 		
 		/* This code adds the Clipboard icon to the display mini-menuCS */
 		final ArrayList clipList = getStructuresThatUseClipboard(myStructures);
 		if (!clipList.isEmpty()) {
 			MenuItem clipboard = new MenuItem("Clipboard");
 			clipboard.setIconStyle("icon-paste");
 			clipboard.setToolTip(new ToolTipConfig("Clipboard",
 					"You can use the clipboard to copy and paste text <br/>"
 							+ "from one field to another in SIS, where supported. <br/>"
 							+ "The clipboard can hold multiple pieces of text at a time."));
 			
 			clipboard.setSubMenu(getClipboardMenu(clipList));
 
 			optionsMenu.add(clipboard);
 		}
 		
 		if (hasAssessment) {
 			String notesIconStyle;
 			List<Notes> notes = NotesCache.impl.getNotesForCurrentAssessment(field);
 			if (notes == null || notes.isEmpty())
 				notesIconStyle = ("images/icon-note-grey.png");
 			else
 				notesIconStyle = ("images/icon-note.png");
 			
 			MenuItem notesMenu = new MenuItem("Notes");
 			notesMenu.setIconStyle(notesIconStyle);
 			notesMenu.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					openEditViewNotesPopup();
 				}
 			});
 			optionsMenu.add(notesMenu);
 		}
 		
 		String referencesIconStyle;
 		if (field != null && !field.getReference().isEmpty())
 			referencesIconStyle = "images/icon-book.png";
 		else
 			referencesIconStyle = "images/icon-book-grey.png";
 		
 		MenuItem referenceMenu = new MenuItem("References");
 		referenceMenu.setIconStyle(referencesIconStyle);
 		referenceMenu.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				final GenericCallback<Object> callback = new GenericCallback<Object>() {
 					public void onSuccess(Object result) {
 						GenericCallback<Object> listener = new GenericCallback<Object>() {
 							public void onFailure(Throwable caught) {
 								WindowUtils.errorAlert("Error!", "Error committing changes to the "
 										+ "server. Ensure you are connected to the server, then try " + "the process again.");
 							}
 		
 							public void onSuccess(Object result) {
 								rebuildIconPanel();
 							}
 						};
 						SISClientBase.getInstance().onShowReferenceEditor("Add a references to " + canonicalName, 
 								Display.this, listener, listener);
 					}
 					public void onFailure(Throwable caught) {
 					}
 				};
 				
 				if (!isSaved())
 					assignIDToField(callback);
 				else
 					callback.onSuccess(null);
 			}
 		});
 
 		optionsMenu.add(referenceMenu);
 		
 		MenuItem definitions = new MenuItem("Definitions");
 		definitions.setIconStyle("icon-help");
 		definitions.addSelectionListener(new SelectionListener<MenuEvent>() {
 			public void componentSelected(MenuEvent ce) {
 				Window s = WindowUtils.newWindow("Definitions for " + canonicalName, null, false, true);
 				s.setScrollMode(Scroll.AUTO);
 				s.add(buildDefintionPanel());
 				s.setSize(400, 400);
 
 				s.show();
 				s.center();
 			}
 		});
 		
 		optionsMenu.add(definitions);
 		
 		final WorkingSet ws = WorkingSetCache.impl.getCurrentWorkingSet();
 		if (hasAssessment && isSaved() && ws != null) {
 			MenuItem batchChange = new MenuItem("Batch Change " + ws.getName() + " Working Set");
 			batchChange.setIconStyle("icon-page-copy");
 			
 			Menu subMenu = new Menu(); {
 				MenuItem append = new MenuItem("Append");
 				append.setToolTip("Appends the contents of this field to any existing contents of the target field.");
 				append.addSelectionListener(new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						doSimpleBatchChange(ws, field, BatchChangeMode.APPEND);
 					}
 				});
 				
 				MenuItem overwrite = new MenuItem("Overwrite");
 				overwrite.setToolTip("Overwrites the content of the target field with the content of this field, even if there is already data in the target field.");
 				overwrite.addSelectionListener(new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						doSimpleBatchChange(ws, field, BatchChangeMode.OVERWRITE);
 					}
 				});
 				
 				
 				MenuItem overwriteIfBlank = new MenuItem("Overwrite Only If Blank");
 				overwriteIfBlank.setToolTip("Overwrites the content of the target field with the content of this field, " +
 					"but only if there target field has no data.");
 				overwriteIfBlank.addSelectionListener(new SelectionListener<MenuEvent>() {
 					public void componentSelected(MenuEvent ce) {
 						doSimpleBatchChange(ws, field, BatchChangeMode.OVERWRITE_IF_BLANK);
 					}
 				});
 				
 				boolean isClassScheme = getField().isClassificationScheme();
 				if (getField().isNarrativeField() || isClassScheme)
 					subMenu.add(append);
 				
 				if (!isClassScheme) {
 					subMenu.add(overwrite);
 					subMenu.add(overwriteIfBlank);
 				}
 			}
 			
 			batchChange.setSubMenu(subMenu);
 			
 			optionsMenu.add(batchChange);
 		}
 		
 		if (hasAssessment && isSaved() && field.isAttachable()) {
 			MenuItem attach = new MenuItem();
 			attach.setText("Attach File");
 			attach.setIconStyle("icon-attachment");
 			attach.addSelectionListener(new SelectionListener<MenuEvent>() {
 				public void componentSelected(MenuEvent ce) {
 					FieldAttachmentWindow window = new FieldAttachmentWindow(AssessmentCache.impl.getCurrentAssessment(), field);
 					window.show();
 				}
 			});
 			
 			optionsMenu.add(attach);
 		}
 	}
 	
 	/**
 	 * FIXME: why can't I just send my field and my working set down to the 
 	 * server and handle all this mess there?!?
 	 */
 	private void doSimpleBatchChange(final WorkingSet ws, final Field field, final BatchChangeMode mode) {
 		final AssessmentFilter filter = ws.getFilter();
 		if (filter.getRegionType().equalsIgnoreCase(Relationship.ALL) || filter.getRegionType().equalsIgnoreCase(Relationship.OR)) {
 			WindowUtils.errorAlert("Unable to perform operations on working sets that don't have an exact region match.");
 			return;
 		}
 		
 		TaxonomyCache.impl.fetchList(ws.getSpeciesIDs(), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				String taxaIDs = null;
 				if (filter.isAllPublished() || filter.isRecentPublished()) {
 					taxaIDs = "";
 					for (Taxon curTaxa : ws.getSpecies()) {
 						for (Integer region : filter.listRegionIDs()) {
 							String regionRegex = region+"";
 							if (region == Region.GLOBAL_ID)
 								regionRegex = "global";
 							if(!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, 
 									new AuthorizablePublishedAssessment(curTaxa, SchemaCache.impl.getDefaultSchema(), regionRegex)) ) {
 								WindowUtils.errorAlert("Unauthorized!", "You are unauthorized to modify " +
 										"published assessments for at least the taxon " + curTaxa.getFullName() +
 								". This operation has been cancelled.");
 								return;
 							}
 						}
 						taxaIDs += curTaxa.getId() + ",";
 					}
 					taxaIDs = taxaIDs.substring(0, taxaIDs.length()-1);
 				}
 				if (filter.isDraft()) 
 				{
 					taxaIDs = "";
 					for (Taxon curTaxa : ws.getSpecies()) {
 						for (Integer region : filter.listRegionIDs())
							if (!AuthorizationCache.impl.hasRight(SISClientBase.currentUser, AuthorizableObject.WRITE, new AuthorizableDraftAssessment(curTaxa, region+"")))
 							{	
 								WindowUtils.hideLoadingAlert();
 								WindowUtils.errorAlert("Unauthorized!", "You are unauthorized to modify " +
 										"draft assessments for at least the taxon " + curTaxa.getFullName() +
 								". This operation has been cancelled.");
 								return;
 							}
 						taxaIDs += curTaxa.getId() + ",";
 					}
 					taxaIDs = taxaIDs.substring(0, taxaIDs.length()-1);
 				}
 				if (taxaIDs == null) {
 					WindowUtils.errorAlert("Error!", "There were no assessments "
 							+ "to be modified in your selected working set "
 							+ "of the selected type. Please try again.");
 					return;
 				}
 				StringBuffer fieldXML = new StringBuffer("<fields>");
 				fieldXML.append("<field>" + field.getName() + "</field>");
 				fieldXML.append("</fields>");
 				
 				StringBuffer xml = new StringBuffer("<batchChange>\n");
 				xml.append("<assessment>" + AssessmentCache.impl.getCurrentAssessment().getId() + "</assessment>");
 				xml.append(filter.toXML());
 				xml.append(fieldXML.toString());
 				xml.append("<taxa>" + taxaIDs + "</taxa>");
 				xml.append(XMLWritingUtils.writeTag("append", Boolean.toString(BatchChangeMode.APPEND.equals(mode))));
 				xml.append(XMLWritingUtils.writeTag("overwrite", Boolean.toString(BatchChangeMode.OVERWRITE_IF_BLANK.equals(mode))));
 				xml.append(XMLWritingUtils.writeTag("set", Boolean.toString(BatchChangeMode.OVERWRITE.equals(mode))));
 				xml.append("</batchChange>\n");
 				
 				final NativeDocument document = SISClientBase.getHttpBasicNativeDocument();
 				document.post(UriBase.getInstance().getBatchChangeBase() + "/batchChange", xml.toString(), new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						final NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("change");
 						for (int i = 0; i < nodes.getLength(); i++) {
 							NativeElement el = nodes.elementAt(i);
 							AssessmentCache.impl.uncache(Integer.valueOf(el.getAttribute("id")));
 						}
 							
 						WindowUtils.infoAlert("Success", "Batch change process was completed successfully.");
 					}
 					public void onFailure(Throwable caught) {
 						WindowUtils.errorAlert("Error in processing batch change. Please try again later.");
 					}
 				});
 			}
 			public void onFailure(Throwable caught) {
 				WindowUtils.errorAlert("Error fetching taxa in selected working set.");
 			}
 		});
 	}
 
 	public void showStructures() {
 		iconPanel.setVisible(true);
 		for (int i = 0; i < myStructures.size(); i++)
 			myStructures.get(i).show();
 	}
 
 	public Widget showViewOnly() {
 		return showDisplay(true);
 	}
 	
 	public boolean isSaved() {
 		return field != null && field.getId() > 0;
 	}
 	
 	public static interface IDAssigner {
 		
 		public void assignID(Field field, GenericCallback<Object> callback);
 		
 	}
 	
 	public static interface ReferenceableFieldFactory {
 		
 		public Referenceable newReferenceableField(Field field);
 		
 	}
 
 }// class Display
