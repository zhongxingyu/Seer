 package org.iucn.sis.client.panels.taxomatic;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.iucn.sis.client.api.caches.AuthorizationCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.utils.FormattedDate;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.client.panels.ClientUIContainer;
 import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.models.CommonName;
 import org.iucn.sis.shared.api.models.Notes;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.WindowManager;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.lwxml.shared.utils.ArrayUtils;
 import com.solertium.util.extjs.client.WindowUtils;
 
 public class CommonNameDisplay implements Referenceable {
 
 	static class AddCommonNameClickListener extends SelectionListener<ButtonEvent> {
 		TextBox nameBox;
 		ListBox isoBox;
 		CheckBox isPrimary;
 		Taxon  node;
 		CommonName commonName;
 		GenericCallback<String> callback;
 
 		public AddCommonNameClickListener(TextBox name, ListBox iso, CheckBox isPrimary, Taxon  node,
 				CommonName cName, GenericCallback<String> callback) {
 			this.nameBox = name;
 			this.isoBox = iso;
 			this.isPrimary = isPrimary;
 			this.node = node;
 			this.commonName = cName;
 			this.callback = callback;
 		}
 
 		@Override
 		public void componentSelected(ButtonEvent ce) {
 			if (nameBox.getText() != null && !nameBox.getText().equalsIgnoreCase("")) {
 				String name = nameBox.getText();
 				String language = getLanguage();
 				String iso = getIsoCode();
 				boolean primary = isPrimary.isChecked();
 
 				if (commonName != null) {
 					if (!commonName.getName().equals(name))
 						commonName.setName(name);
 					if (!commonName.getLanguage().equals(language))
 						commonName.setLanguage(language);
 					if (!commonName.isPrimary() == primary) {
 						commonName.setPrincipal(primary);
 					}
 					if (!commonName.getIsoCode().equals(iso))
 						commonName.setIsoCode(iso);
 
 				} else {
 					commonName = CommonName.createCommonName(name, language, iso, primary);
 					commonName.setChangeReason(CommonName.ADDED);
 					node.getCommonNames().add(commonName);
 				}
 
 				TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 					public void onFailure(Throwable caught) {
 						ClientUIContainer.headerContainer.update();
 						callback.onFailure(null);
 						destroy();
 					};
 
 					public void onSuccess(String result) {
 						ClientUIContainer.headerContainer.update();
 						callback.onSuccess(null);
 						destroy();
 					};
 				});
 
 			} else {
 				WindowUtils.errorAlert("The name field cannot be blank!");
 			}
 		}
 
 		private String getIsoCode() {
 			String itemText = isoBox.getItemText(isoBox.getSelectedIndex());
 			return itemText.substring(itemText.indexOf("| ") + 2, itemText.length());
 
 			// return isoBox.getValue(isoBox.getSelectedIndex());
 		}
 
 		private String getLanguage() {
 			String itemText = isoBox.getItemText(isoBox.getSelectedIndex());
 			return itemText.substring(0, itemText.indexOf(" |"));
 
 			// return isoBox.getItemText(isoBox.getSelectedIndex());
 
 		}
 	}
 
 	private static void destroy() {
 		panel.hide();
 		panel.removeFromParent();
 		panel = null;
 	}
 
 	public static Window getNewCommonNameDisplay(Taxon  node, final CommonName cName,
 			GenericCallback<String> callback) {
 		if (panel != null)
 			return panel;
 
 		panel = WindowUtils.getWindow(false, false, "");
 		VerticalPanel contentPanel = new VerticalPanel();
 		contentPanel.setSpacing(5);
 
 		VerticalPanel leftPanel = new VerticalPanel();
 		leftPanel.setSpacing(5);
 		leftPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		VerticalPanel rightPanel = new VerticalPanel();
 		rightPanel.setSpacing(5);
 		rightPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
 
 		HorizontalPanel buttonPanel = new HorizontalPanel();

 		if (cName == null)
 			panel.setHeading("Add New Common Name");
 		else
 			panel.setHeading("Edit Common Name");
 
 		TextBox nameBox = new TextBox();

		// TextBox isoBox = new TextBox();
 		final ListBox isoBox = new ListBox();
 		final CheckBox isPrimary = new CheckBox();
 		if (node.getCommonNames().size() == 0) {
 			isPrimary.setChecked(true);
 			isPrimary.setEnabled(false);
 		}
 		isPrimary.setText("Primary Name");
 
 		final Button add = new Button();
 		if (cName == null)
 			add.setText("Add Name");
 		else
 			add.setText("Save");
 		add.addSelectionListener(new AddCommonNameClickListener(nameBox, isoBox, isPrimary, node, cName, callback));
 		add.setEnabled(false);
 
 		Button cancel = new Button();
 		cancel.setText("Cancel");
 		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				destroy();
 			}
 		});
 
 		final NativeDocument isoDoc = SimpleSISClient.getHttpBasicNativeDocument();
 		isoDoc.get(UriBase.getInstance().getSISBase() +"/raw/utils/ISO-639-2_utf-8.xml", new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				destroy();
 				WindowUtils.errorAlert("Error Loading Languages", "Could not load "
 						+ "languages for the drop down. Please check your Internet "
 						+ "connectivity if you are running online, or check your local "
 						+ "server if you are running offline, then try again.");
 			}
 
 			public void onSuccess(String result) {
 				NativeNodeList isolist = isoDoc.getDocumentElement().getElementsByTagName("language");
 
 				String[] names = new String[isolist.getLength()];
 
 				for (int i = 0; i < isolist.getLength(); i++) {
 					NativeElement cur = isolist.elementAt(i);
 
 					String isoCode = cur.getElementByTagName("bibliographic").getText();
 					String lang = cur.getElementByTagName("english").getText();
 
 					if (cName != null) {
 						if (cName.getIsoCode() == null || cName.getIsoCode().equals(""))
 							if (cName.getLanguage() != null && cName.getLanguage().equals(lang))
 								cName.setIsoCode(isoCode);
 
 						if (cName.getLanguage() == null || cName.getLanguage().equals(""))
 							if (cName.getIsoCode() != null && cName.getIsoCode().equals(isoCode))
 								cName.setLanguage(lang);
 					}
 
 					names[i] = lang + " | " + isoCode;
 				}
 
 				ArrayUtils.quicksort(names);
 
 				for (int i = 0; i < names.length; i++) {
 					String lang = names[i].substring(0, names[i].indexOf(" |"));
 					String iso = names[i].substring(names[i].indexOf("| ") + 2);
 
 					isoBox.addItem(names[i]);
 
 					if (cName != null && iso.equals(cName.getIsoCode()))
 						isoBox.setSelectedIndex(i);
 
 					if (cName == null && lang.equals("English"))
 						isoBox.setSelectedIndex(i);
 				}
 
 				add.setEnabled(true);
 			}
 		});
 
 		if (cName != null) {
 			nameBox.setText(cName.getName());
 			isPrimary.setChecked(cName.isPrimary());
			// isoBox.setText( cName.getLanguage() );
 		}
 
 		HTML nameLabel = new HTML("Name: ");
 		HTML langLabel = new HTML("Language: ");
 
 		leftPanel.add(nameLabel);
 		rightPanel.add(nameBox);
 
 		leftPanel.add(langLabel);
 		rightPanel.add(isoBox);
 		rightPanel.add(isPrimary);
 
 		buttonPanel.add(add);
 		buttonPanel.add(cancel);
 
 		HorizontalPanel wrap = new HorizontalPanel();
 		wrap.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		wrap.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		wrap.add(leftPanel);
 		wrap.add(rightPanel);
 
 		contentPanel.add(wrap);
 		contentPanel.add(buttonPanel);
 
 		panel.add(contentPanel);
 		panel.setSize(700, 300);
 		return panel;
 	}
 
 	Taxon  node;
 
 	CommonName name;
 
 	Image notesImage;
 
 	private HorizontalPanel labelPanel = new HorizontalPanel();
 
 	private static Window panel;
 
 	public CommonNameDisplay(Taxon  theNode) {
 		node = theNode;
 		name = null;
 	}
 
 	public CommonNameDisplay(Taxon  theNode, CommonName theName) {
 		node = theNode;
 		name = theName;
 	}
 
 	public void addReferences(ArrayList<Reference> references, final GenericCallback<Object> callback) {
 		if (name == null) {
 			callback.onFailure(new Exception("No common name selected."));
 			return;
 		}
 
 		int added = 0;
 		for (int i = 0; i < references.size(); i++) {
 			Reference current = references.get(i);
 			if (name.getReference().add(current) )
 				added++;
 		}
 
 		if (added > 0) {
 			TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 			
 				public void onSuccess(String result) {
 					ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
 					callback.onSuccess(result);
 				}
 			
 				public void onFailure(Throwable caught) {
 					callback.onFailure(caught);
 				}
 			});
 		}
 	}
 
 	public Set<Reference> getReferencesAsList() {
 		return name.getReference();
 	}
 
 	public void onReferenceChanged(final GenericCallback<Object> callback) {
 		TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 		
 			@Override
 			public void onSuccess(String result) {
 				callback.onSuccess(result);		
 			}
 		
 			@Override
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 	}
 
 	public void removeReferences(ArrayList<Reference> references, final GenericCallback<Object> callback) {
 		if (name == null) {
 			callback.onFailure(new Exception("No common name selected."));
 			return;
 		}
 
 		int removed = 0;
 		for (int i = 0; i < references.size(); i++)
 			if (name.getReference().remove(references.get(i)))
 				removed++;
 
 		// TODO: Find out why the removed count isn't moving correctly...?
 		// if (removed > 0)
 		// {
 		TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 			
 			public void onSuccess(String result) {
 				ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
 				callback.onSuccess(result);
 			}
 		
 			public void onFailure(Throwable caught) {
 				callback.onFailure(caught);
 			}
 		});
 		
 		// }
 
 	}
 
 	public Widget show(final GenericCallback<String> callback) {
 		Image removeImage = new Image("images/icon-note-delete.png");
 		removeImage.setPixelSize(14, 14);
 		removeImage.setTitle("Remove this common name");
 		removeImage.addClickListener(new ClickListener() {
 			public void onClick(Widget sender) {
 				if (com.google.gwt.user.client.Window.confirm("Really remove this common name?")) {
 					// node.getCommonNames().remove( name );
 					name.setChangeReason(CommonName.DELETED);
 					TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 						public void onFailure(Throwable caught) {
 							callback.onFailure(caught);
 						};
 
 						public void onSuccess(String result) {
 							callback.onSuccess(null);
 						};
 					});
 				}
 			}
 		});
 		Image editImage = new Image("images/icon-note-edit.png");
 		editImage.setPixelSize(14, 14);
 		editImage.setTitle("Edit this common name");
 		editImage.addClickListener(new ClickListener() {
 			public void onClick(Widget sender) {
 				WindowManager.get().hideAll();
 				Window temp = getNewCommonNameDisplay(node, name, callback);
 				// temp.show();
 				temp.show();
 				temp.center();
 			}
 		});
 
 		Image referenceImage = new Image("images/icon-book.png");
 		if (name.getReference().size() == 0)
 			referenceImage.setUrl("images/icon-book-grey.png");
 		referenceImage.setPixelSize(14, 14);
 		referenceImage.setTitle("Add/Remove References");
 		referenceImage.addClickListener(new ClickListener() {
 			public void onClick(Widget sender) {
 				final Window s = WindowUtils.getWindow(true, true, "Add a references to Common Name" + name.getName());
 				s.setIconStyle("icon-book");
 				s.setLayout(new FillLayout());
 
 				ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel
 						.setReferences(CommonNameDisplay.this);
 
 				s.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);
 
 				s.setSize(850, 550);
 				s.show();
 				s.center();
 			}
 		});
 
 		notesImage = new Image("images/icon-note.png");
 		if (name.getNotes().size() == 0)
 			notesImage.setUrl("images/icon-note-grey.png");
 		notesImage.setTitle("Add/Remove Notes");
 		notesImage.addClickListener(new ClickListener() {
 			public void onClick(Widget sender) {
 				final Window s = WindowUtils.getWindow(true, true, "Notes for common name" + name);
 				final LayoutContainer container = s;
 				container.setLayoutOnChange(true);
 				FillLayout layout = new FillLayout();
 				layout.setOrientation(Orientation.VERTICAL);
 				container.setLayout(layout);
 
 				final VerticalPanel panelAdd = new VerticalPanel();
 				panelAdd.setSpacing(3);
 
 				panelAdd.add(new HTML("Add Note: "));
 
 				final TextArea area = new TextArea();
 				area.setSize("400", "75");
 				panelAdd.add(area);
 
 				Button save = new Button("Add Note");
 				save.addSelectionListener(new SelectionListener<ButtonEvent>() {
 					@Override
 					public void componentSelected(ButtonEvent ce) {
 						if (area.getText().equalsIgnoreCase("")) {
 							WindowUtils.errorAlert("Must enter note body.");
 						} else {
 							Notes currentNote = new Notes();
 							currentNote.setValue(area.getText());
 							currentNote.setCommonName(name);
 							name.getNotes().add(currentNote);
 							notesImage.setUrl("images/icon-note.png");
 							s.hide();
 							TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 								public void onFailure(Throwable caught) {
 									callback.onFailure(null);
 								};
 
 								public void onSuccess(String result) {
 									callback.onSuccess(result);
 								};
 							});
 							// callback.onSuccess( null );
 							// WindowUtils.infoAlert("Success", "Note Added.");
 						}
 
 					}
 				});
 				Button close = new Button("Close");
 				close.addSelectionListener(new SelectionListener<ButtonEvent>() {
 					@Override
 					public void componentSelected(ButtonEvent ce) {
 						s.hide();
 					}
 				});
 
 				final Set<Notes> notes = name.getNotes();
 				if (notes == null || notes.size() == 0) {
 					container
 							.add(new HTML(
 									"<div style='padding-top:10px';background-color:grey><b>There are no notes for this field.</b></div>"));
 					// container.add( panelAdd );
 				} else {
 
 					ContentPanel eBar = new ContentPanel();
 					eBar.setHeight(200);
 
 					FillLayout notelayout = new FillLayout();
 					notelayout.setOrientation(Orientation.VERTICAL);
 					eBar.setLayout(notelayout);
 					eBar.setLayoutOnChange(true);
 					eBar.setScrollMode(Scroll.AUTO);
 
 					for (final Notes current : notes) {
 						Image deleteNote = new Image("images/icon-note-delete.png");
 						deleteNote.setTitle("Delete Note");
 						deleteNote.addClickListener(new ClickListener() {
 							public void onClick(Widget sender) {
 								name.getNotes().remove(current);
 								if (name.getNotes().size() == 0)
 									notesImage.setUrl("images/icon-note-grey.png");
 								s.hide();
 								TaxonomyCache.impl.saveTaxonAndMakeCurrent(node, new GenericCallback<String>() {
 									public void onFailure(Throwable caught) {
 										callback.onFailure(null);
 									};
 
 									public void onSuccess(String result) {
 										callback.onSuccess(result);
 									};
 								});
 							}
 						});
 
 						LayoutContainer a = new LayoutContainer();
 						RowLayout innerLayout = new RowLayout();
 						innerLayout.setOrientation(Orientation.HORIZONTAL);
 						// innerLayout.setSpacing(10);
 						a.setLayout(innerLayout);
 						a.setLayoutOnChange(true);
 						// a.setWidth(400);
 						a.add(deleteNote, new RowData());
 						a.add(new HTML("<b>" + current.getEdit().getUser().getDisplayableName() 
 								+ " [" + FormattedDate.impl.getDate(current.getEdit().getCreatedDate()) 
 								+ "]</b>  --" + current.getValue()), new RowData(1d, 1d));// );
 
 						eBar.add(a, new RowData(1d, 1d));
 					}
 					container.add(eBar);
 				}
 
 				panelAdd.add(save);
 				panelAdd.add(close);
 				container.add(panelAdd);
 
 				s.setSize(500, 400);
 				s.show();
 				s.center();
 			}
 		});
 
 		String display = "&nbsp;&nbsp;" + name.getName() + " --- "
				+ (name.getLanguage().equals("") ? name.getIsoCode() : name.getLanguage());
 
 		if (name.getChangeReason() != 0) {
 			display += " -- " + CommonName.reasons[name.getChangeReason()];
 		}
 		HTML disp = new HTML(display);
 
 		if (!CommonName.reasons[name.getChangeReason()].equals("DELETED")) {
 			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
 				labelPanel.add(removeImage);
 				labelPanel.add(editImage);
 				labelPanel.add(referenceImage);
 				labelPanel.add(notesImage);
 			}
 		} else {
 			disp.setStyleName("deleted");
 		}
 
 		labelPanel.add(disp);
 
 		return labelPanel;
 	}
 
 }
