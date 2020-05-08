 package org.iucn.sis.client.panels.taxa;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.ui.notes.NoteAPI;
 import org.iucn.sis.client.api.ui.notes.NotesWindow;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.client.panels.taxomatic.CommonNameToolPanel;
 import org.iucn.sis.client.panels.taxomatic.EditCommonNamePanel;
 import org.iucn.sis.client.tabs.TaxonHomePageTab.ReferenceableTaxon;
 import org.iucn.sis.client.tabs.TaxonHomePageTab.TaxonNoteAPI;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.utils.CommonNameComparator;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.InfoConfig;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.events.ComplexListener;
 import com.solertium.util.events.SimpleListener;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class TaxonHomeGeneralInformationTab extends LayoutContainer implements DrawsLazily {
 	
 	public TaxonHomeGeneralInformationTab() {
 		super(new FillLayout());
 		setLayoutOnChange(true);
 	}
 	
 	@Override
 	public void draw(DoneDrawingCallback callback) {
 		removeAll();
 		
 		final Taxon node = TaxonomyCache.impl.getCurrentTaxon();
 		
 		final LayoutContainer container = new LayoutContainer(new BorderLayout());
 		container.add(drawGeneralInformation(node), new BorderLayoutData(LayoutRegion.CENTER));
 		container.add(drawCommonNamesAndSynonyms(node), new BorderLayoutData(LayoutRegion.EAST, .5f));
 		
 		add(container);
 		
 		callback.isDrawn();
 	}
 	
 	private ContentPanel drawGeneralInformation(final Taxon node) {
 		LayoutContainer data = new LayoutContainer();
 		//data.setWidth(240);
 		if (!node.isDeprecated())
 			data.add(new HTML("Name: <i>" + node.getName() + "</i>"));
 		else
 			data.add(new HTML("Name: <s>" + node.getName() + "</s>"));
 		data.add(new HTML("&nbsp;&nbsp;Taxon ID: "
 				+ "<a target='_blank' href='http://www.iucnredlist.org/apps/redlist/details/" + node.getId()
 				+ "'>" + node.getId() + "</a>"));
 
 		if (node.getLevel() >= TaxonLevel.SPECIES) {
 			data.add(new HTML("Full Name:  <i>" + node.getFullName() + "</i>"));
 		}
 		data.add(new HTML("Level: " + node.getDisplayableLevel()));
 		if (node.getParentName() != null) {
 			HTML parentHTML = new HTML("Parent:  <i>" + node.getParentName() + "</i>"
 					+ "<img src=\"images/icon-tree.png\"></img>");
 			parentHTML.addStyleName("clickable");
 			parentHTML.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					new TaxonTreePopup(node).show();
 				}
 			});
 			data.add(parentHTML);
 		}
 		if (node.getTaxonomicAuthority() != null && !node.getTaxonomicAuthority().equalsIgnoreCase("")) {
 			data.add(new HTML("Taxonomic Authority: " + node.getTaxonomicAuthority()));
 		}
 
 		data.add(new HTML("Status: " + node.getStatusCode()));
 		data.add(new HTML("Hybrid: " + node.getHybrid()));
 		
 		HorizontalPanel refPanel = new HorizontalPanel(); {
 			int size = node.getReference().size();
 			final HTML display = new HTML("References (" + size + "): ");
 			
 			final Image image = new Image(size > 0 ? "images/icon-book.png" : "images/icon-book-grey.png");
 			image.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					SimpleSISClient.getInstance().onShowReferenceEditor(
 						"Manage References for " + node.getFullName(), 
 						new ReferenceableTaxon(node, new SimpleListener() {
 							public void handleEvent() {
 								//Short way...
 								int size = node.getReference().size();
 								display.setHTML("References (" + size + "): ");
 								image.setUrl(size > 0 ? "images/icon-book.png" : "images/icon-book-grey.png");
 								
 								//Long way
 								//update(taxon.getId());
 							}
 						}), 
 						null, null
 					);
 				}
 			});
 			
 			refPanel.add(display);
 			refPanel.add(image);
 		}
 		data.add(refPanel);
 		
 		HorizontalPanel notesPanel = new HorizontalPanel(); {
 			int size = node.getNotes().size();
 			final HTML display = new HTML("Notes (" + size + "): ");
 			
 			final Image image = new Image(size > 0 ? "images/icon-note.png" : "images/icon-note-grey.png");
 			image.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					final NotesWindow window = new NotesWindow(new TaxonNoteAPI(node));
 					window.setHeading("Notes for " + node.getFullName());
 					window.show();	
 				}
 			});
 			
 			notesPanel.add(display);
 			notesPanel.add(image);
 		}
 		
 		data.add(notesPanel);
 		
 		final ContentPanel generalInformation = new ContentPanel();
 		generalInformation.setLayoutOnChange(true);
 		generalInformation.setHeading("General Information");
 		generalInformation.setStyleName("x-panel");
 		//generalInformation.setWidth(350);
 		// generalLayout.setSpacing(5);
 		//generalInformation.setHeight(panelHeight);
 		generalInformation.add(data);//, new BorderLayoutData(LayoutRegion.CENTER));
 		
 		return generalInformation;
 	}
 	
 	private LayoutContainer drawCommonNamesAndSynonyms(final Taxon node) {
 		LayoutContainer data = new LayoutContainer();
 		
 		// ADD SYNONYMS
 		if (!node.getSynonyms().isEmpty()) {
 			addSynonyms(node, data);
 		}
 
 		// ADD COMMON NAMES
 		Image addName = new Image("images/add.png");
 		addName.setSize("14px", "14px");
 		addName.setTitle("Add New Common Name");
 		addName.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				Window addNameBox = new EditCommonNamePanel(null, node, 
 						new ComplexListener<CommonName>() {
 					public void handleEvent(CommonName eventData) {
 						//TaxonomyCache.impl.setCurrentTaxon(node);
 						ClientUIContainer.bodyContainer.refreshBody();
 					}
 				});
 				addNameBox.show();
 			}
 		});
 //
 		HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");
 		
 		LayoutContainer commonNamePanel = new LayoutContainer();
 		if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node))
 			commonNamePanel.add(addName);
 		commonNamePanel.add(commonNamesHeader);
 		data.add(new HTML("<hr><br />"));
 		data.add(commonNamePanel);
 
 		if (!node.getCommonNames().isEmpty()) {
 			int loop = 5;
 			if (node.getCommonNames().size() < 5)
 				loop = node.getCommonNames().size();
 			
 			addCommonNames(node, data, loop);
 		} else
 			data.add(new HTML("No Common Names."));
 		
 		final ContentPanel generalInformation = new ContentPanel();
 		generalInformation.setLayoutOnChange(true);
 		generalInformation.setHeading("Common Names & Synonyms");
 		generalInformation.setStyleName("x-panel");
 		generalInformation.setWidth(350);
 		// generalLayout.setSpacing(5);
 		//generalInformation.setHeight(panelHeight);
 		generalInformation.add(data, new BorderLayoutData(LayoutRegion.CENTER));
 		
 		return generalInformation;
 	}
 	
 	private void addSynonyms(final Taxon node, LayoutContainer data) {
 		data.add(new HTML("<hr><br />"));
 		data.add(new HTML("<b>Synonyms</b>"));
 		int size = node.getSynonyms().size();
 		if (size > 5)
 			size = 5;
 
 		for (final Synonym curSyn : node.getSynonyms()) {
 			size--;
 			if (size < 0)
 				break;
 			
 			HorizontalPanel hp = new HorizontalPanel();
 			
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 				final Image notesImage = new Image("images/icon-note.png");
 				if (curSyn.getNotes().isEmpty())
 					notesImage.setUrl("images/icon-note-grey.png");
 				notesImage.setTitle("Add/Remove Notes");
 				notesImage.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						final NotesWindow window = new NotesWindow(new SynonymNoteAPI(node, curSyn));
 						window.show();
 					}
 				});
 				hp.add(notesImage);
 			}
 
 			String value = curSyn.toDisplayableString();
 			if (curSyn.getStatus().equals(Synonym.ADDED) || curSyn.getStatus().equals(Synonym.DELETED))
 				value += "-- " + curSyn.getStatus();
 
 			hp.add(new HTML("&nbsp;&nbsp;" + value));
 
 			data.add(hp);
 		}
 		
 		if (node.getSynonyms().size() > 5) {
 			HTML viewAll = new HTML("View all...");
 			viewAll.setStyleName("SIS_HyperlinkLookAlike");
 			viewAll.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					if (TaxonomyCache.impl.getCurrentTaxon() == null) {
 						Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
 						return;
 					}
 
 					final Window s = WindowUtils.getWindow(false, false, "Synonyms");
 					s.setSize(400, 400);
 					LayoutContainer data = s;
 					data.setScrollMode(Scroll.AUTO);
 
 					VerticalPanel currentSynPanel = new VerticalPanel();
 					currentSynPanel.setSpacing(3);
 
 					HTML curHTML = new HTML("Current Synonyms");
 					curHTML.addStyleName("bold");
 					currentSynPanel.add(curHTML);
 
 					if (TaxonomyCache.impl.getCurrentTaxon().getSynonyms().size() == 0)
 						currentSynPanel.add(new HTML("There are no synonyms for this taxon."));
 
 					for (Synonym curSyn : TaxonomyCache.impl.getCurrentTaxon().getSynonyms()) {
 						curHTML = new HTML(curSyn.getFriendlyName());
 						currentSynPanel.add(curHTML);
 					}
 
 					data.add(currentSynPanel);
 					s.show();
 
 				}
 			});
 			data.add(viewAll);
 		}
 	}
 	
 	private Widget getCommonNameDisplay(CommonName curName, Taxon node) {
 		HorizontalPanel hp = new HorizontalPanel();
 		
		StringBuilder display = new StringBuilder();
		if (curName.isPrimary())
			display.append("*&nbsp;");
		else
			display.append("&nbsp;&nbsp;");
		
		display.append(curName.getName());
 		if (curName.getIso() != null) {
			display.append(" -- " + curName.getLanguage());
 		}
 		
		HTML html = new HTML(display.toString());
 		if (curName.getChangeReason() == CommonName.DELETED) {
 			html.addStyleName("deleted");
 		} else {
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 				CommonNameToolPanel cntp = new CommonNameToolPanel(curName, node);
 				hp.add(cntp);
 			}
 		}
 		hp.add(html);
 		return hp;
 	}
 	
 	private void addCommonNames(final Taxon node, LayoutContainer data, int loop) {
 		final ArrayList<CommonName> commonNames = new ArrayList<CommonName>(node.getCommonNames());
 		Collections.sort(commonNames, new CommonNameComparator());
 		for (final CommonName curName : commonNames) {
 			loop--;
 			if (loop < 0)
 				break;			
 			data.add(getCommonNameDisplay(curName, node));
 		}
 
 		HTML viewAll = new HTML("View all...");
 		viewAll.setStyleName("SIS_HyperlinkLookAlike");
 		viewAll.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				final Window container = WindowUtils.getWindow(false, false, "Edit Common Names");
 				container.setScrollMode(Scroll.AUTO);
 
 				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 					Button item = new Button();
 					item.setText("New Common Name");
 					item.setIconStyle("icon-add");
 					item.addSelectionListener(new SelectionListener<ButtonEvent>() {
 						public void componentSelected(ButtonEvent ce) {
 							container.hide();
 							Window addNameBox = new EditCommonNamePanel(null, node, new ComplexListener<CommonName>() {
 								public void handleEvent(CommonName eventData) {
 									//TaxonomyCache.impl.setCurrentTaxon(node);
 									ClientUIContainer.bodyContainer.refreshBody();
 								}
 							});
 							addNameBox.show();
 						}
 					});
 
 					ToolBar tBar = new ToolBar();
 					tBar.add(item);
 
 					container.add(tBar);
 				}
 				
 				HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");
 
 				LayoutContainer commonNamePanel = new LayoutContainer();
 				commonNamePanel.add(commonNamesHeader);
 
 				container.add(new HTML("<hr><br />"));
 				container.add(commonNamePanel);
 
 				if (commonNames.size() != 0) {
 					for (CommonName curName : commonNames) {
 						container.add(getCommonNameDisplay(curName, node));
 					}
 				} else
 					container.add(new HTML("No Common Names."));
 				container.setSize(350, 550);
 				container.show();
 				container.center();
 
 			}
 		});
 		if (node.getCommonNames().size() > 5)
 			data.add(viewAll);
 	}
 	
 private static class SynonymNoteAPI implements NoteAPI {
 		
 		private final Synonym synonym;
 		private final Taxon taxon;
 		
 		private boolean hasChanged;
 		
 		public SynonymNoteAPI(Taxon taxon, Synonym synonym) {
 			this.synonym = synonym;
 			this.taxon = taxon;
 			
 			hasChanged = false;
 		}
 
 		@Override
 		public void addNote(final Notes note, final GenericCallback<Object> callback) {
 			note.setSynonym(synonym);
 			
 			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 			document.put(UriBase.getInstance().getNotesBase() + "/notes/synonym/" + synonym.getId(), note.toXML(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					Notes newNote = Notes.fromXML(document.getDocumentElement());
 					
 					note.setEdits(newNote.getEdits());
 					note.setId(newNote.getId());
 					
 					synonym.getNotes().add(note);
 					
 					hasChanged = true;
 					
 					callback.onSuccess(result);
 				}public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 		@Override
 		public void deleteNote(final Notes note, final GenericCallback<Object> callback) {
 			final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
 			document.delete(UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId(), new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					synonym.getNotes().remove(note);
 					hasChanged = true;
 					callback.onSuccess(result);
 				}
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 		
 		@Override
 		public void loadNotes(ComplexListener<Collection<Notes>> listener) {
 			listener.handleEvent(synonym.getNotes());
 		}
 		
 		@Override
 		public void onClose() {
 			if (hasChanged)
 				//TaxonomyCache.impl.setCurrentTaxon(taxon);
 				ClientUIContainer.bodyContainer.refreshBody();
 				//ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(taxon.getId());			
 		}
 		
 	}
 
 }
