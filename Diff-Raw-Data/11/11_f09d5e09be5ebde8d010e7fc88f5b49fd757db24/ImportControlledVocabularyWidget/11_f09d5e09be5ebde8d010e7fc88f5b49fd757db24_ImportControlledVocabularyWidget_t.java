 package eu.europeana.uim.gui.cp.client.europeanawidgets;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.RunAsyncCallback;
 import com.google.gwt.dom.client.InputElement;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
 import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import eu.europeana.uim.gui.cp.client.IngestionWidget;
 import eu.europeana.uim.gui.cp.client.services.ImportVocabularyProxyAsync;
 import eu.europeana.uim.gui.cp.client.services.RepositoryServiceAsync;
 import eu.europeana.uim.gui.cp.client.services.ResourceServiceAsync;
 import eu.europeana.uim.gui.cp.client.utils.EuropeanaClientConstants;
 import eu.europeana.uim.gui.cp.shared.ControlledVocabularyDTO;
 import eu.europeana.uim.gui.cp.shared.EdmFieldDTO;
 import eu.europeana.uim.gui.cp.shared.MappingDTO;
 import eu.europeana.uim.gui.cp.shared.OriginalFieldDTO;
 
 /**
  * Page to import a new Controlled Vocabulary
  * 
  * @author Yorgos.Mamakis@ kb.nl
  * 
  */
 public class ImportControlledVocabularyWidget extends IngestionWidget {
 
 	@UiField(provided = true)
 	TabLayoutPanel importVocabulary;
 	FileUpload saveAndUpload;
 	Button createMapping;
 	Button saveMapping;
 	Button deleteMapping;
 	Button deleteVocabulary;
 	Button editVocabulary;
 	Button refreshVocabularies;
 	CellList<OriginalFieldDTO> originalFields;
 	List<String> storedVocabularies;
 	CellList<EdmFieldDTO> mappableFields;
 	CellList<MappingDTO> mappedFields;
 	CellList<ControlledVocabularyDTO> vocabularyTable;
 	List<MappingDTO> mappings;
 	private final ImportVocabularyProxyAsync importedVocabulary;
 	ControlledVocabularyDTO vocabulary;
 
 	// temporary fields
 	MappingDTO mappedField;
 	String originalField;
 	String edmField;
 
 	/**
 	 * Constructor for the call to the service
 	 * 
 	 * @param name
 	 *            Name of the page
 	 * @param description
 	 *            Description of the page
 	 * @param importedVocabulary
 	 *            The service to handle the AJAX calls
 	 */
 	public ImportControlledVocabularyWidget(String name, String description,
 			ImportVocabularyProxyAsync importedVocabulary) {
 		super(name, description);
 		this.importedVocabulary = importedVocabulary;
 	}
 
 	/**
 	 * Constructor for the call to the service
 	 * 
 	 * @param repositoryService
 	 *            The repository service
 	 * @param resourceService
 	 *            The resource service
 	 * @param importedVocabulary
 	 *            The service to handle the AJAX calls
 	 */
 	public ImportControlledVocabularyWidget(
 			RepositoryServiceAsync repositoryService,
 			ResourceServiceAsync resourceService,
 			ImportVocabularyProxyAsync importedVocabulary) {
 		super("Import Controlled Vocabulary", "Import Controlled Vocabulary");
 		this.importedVocabulary = importedVocabulary;
 	}
 
 	interface Binder extends UiBinder<Widget, ImportControlledVocabularyWidget> {
 	}
 
 	/**
 	 * Initialization code creating the tabs
 	 */
 	@Override
 	public Widget onInitialize() {
 		importVocabulary = new TabLayoutPanel(2.5, Unit.PCT);
 		importVocabulary.setWidth("32em");
 		importVocabulary.add(createImportTable(), "Upload Vocabulary");
 		importVocabulary.add(createFieldMappingPage(), "Vocabulary Mapping");
 		Binder uiBinder = GWT.create(Binder.class);
 		Widget widget = (Widget) uiBinder.createAndBindUi(this);
 		return widget;
 	}
 
 	/**
 	 * The field-mapping tab
 	 * 
 	 * @return A split layout panel with 3 lists (original, mappable and mapped
 	 *         fields)
 	 */
 	private SplitLayoutPanel createFieldMappingPage() {
 		SplitLayoutPanel vpanel = new SplitLayoutPanel();
 		vpanel.addWest(createOriginalFieldTable(), 150);
 		vpanel.addEast(createButtons(), 250);
 		vpanel.addEast(createMappedFieldsTable(), 250);
 		vpanel.add(createMappableFieldTable());
 		return vpanel;
 	}
 
 	/**
 	 * Construct a FlexTable with the buttons to map and delete a mapping.
 	 * 
 	 * @return The flextable with the buttons
 	 */
 	protected FlexTable createButtons() {
 		FlexTable buttons = new FlexTable();
 		createMapping = new Button(EuropeanaClientConstants.VOCCREATEMAPPING);
 		createMapping.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent arg0) {
 				importedVocabulary.mapField(originalField, edmField,
 						new AsyncCallback<MappingDTO>() {
 							@Override
 							public void onSuccess(MappingDTO result) {
 								ListDataProvider<MappingDTO> list = new ListDataProvider<MappingDTO>(
 										mappedFields.getVisibleItems());
 								list.addDataDisplay(mappedFields);
 								replace(mappedFields.getVisibleItems(), result);
 
 								list.refresh();
 								mappedFields.getSelectionModel().setSelected(
 										result, false);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("The mapping failed due to:"
 										+ caught.getMessage());
 							}
 						});
 			}
 		});
 		saveMapping = new Button(EuropeanaClientConstants.VOCSAVEMAPPING);
 		saveMapping.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent arg0) {
 				importedVocabulary.saveMapping(new AsyncCallback<Boolean>() {
 					@Override
 					public void onSuccess(Boolean result) {
 						Window.alert("The mapping was saved successfully");
 						fillVocabularyTable();
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("The mapping was not saved. "
 								+ caught.getMessage());
 						caught.printStackTrace();
 					}
 				});
 			}
 		});
 		deleteMapping = new Button(EuropeanaClientConstants.VOCDELETEMAPPING);
 		deleteMapping.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent arg0) {
 				importedVocabulary.mapField(originalField, "null",
 						new AsyncCallback<MappingDTO>() {
 							@Override
 							public void onSuccess(MappingDTO result) {
 								Window.alert("Mapping was deleted");
 								ListDataProvider<MappingDTO> list = new ListDataProvider<MappingDTO>(
 										mappedFields.getVisibleItems());
 								list.addDataDisplay(mappedFields);
 								replace(mappedFields.getVisibleItems(), result);
 								list.refresh();
 								mappedFields.getSelectionModel().setSelected(
 										result, false);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("The mapping was not deleted. "
 										+ caught.getMessage());
 							}
 						});
 			}
 		});
 		buttons.setWidget(0, 0, createMapping);
 		buttons.setWidget(1, 0, saveMapping);
 		buttons.setWidget(2, 0, deleteMapping);
 		return buttons;
 	}
 
 	// Replace the mapping
 	private List<MappingDTO> replace(List<MappingDTO> mapping, MappingDTO result) {
 		for (MappingDTO mappingDTO : mapping) {
 			if (mappingDTO.getOriginal().getField()
 					.equalsIgnoreCase(result.getOriginal().getField())) {
 				mappingDTO.setMapped(result.getMapped());
 				return mapping;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Constructs a Scrollpanel with a list of mapped cells
 	 * 
 	 * @return A Scrollpanel The table with mapped fields
 	 */
 	protected ScrollPanel createMappedFieldsTable() {
 		ScrollPanel dock = new ScrollPanel();
 		final SelectionModel<MappingDTO> selectionModel = new SingleSelectionModel<MappingDTO>();
 		mappedFields = new CellList<MappingDTO>(new MappedCell());
 		mappedFields.setSelectionModel(selectionModel);
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					@Override
 					public void onSelectionChange(SelectionChangeEvent event) {
 						mappedField = ((SingleSelectionModel<MappingDTO>) selectionModel)
 								.getSelectedObject();
 					}
 				});
 		dock.add(mappedFields);
 		dock.setTitle(EuropeanaClientConstants.VOCMAPPEDFIELDS);
 		return dock;
 	}
 
 	/**
 	 * Create the Original Fields table
 	 * 
 	 * @return A scrollpanel with the List of the original Fields
 	 */
 	protected ScrollPanel createOriginalFieldTable() {
 		ScrollPanel dock = new ScrollPanel();
 		final SelectionModel<OriginalFieldDTO> selectionModel = new SingleSelectionModel<OriginalFieldDTO>();
 		originalFields = new CellList<OriginalFieldDTO>(new OriginalCell());
 		originalFields.setSelectionModel(selectionModel);
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					@Override
 					public void onSelectionChange(SelectionChangeEvent event) {
 						originalField = ((SingleSelectionModel<OriginalFieldDTO>) selectionModel)
 								.getSelectedObject().getField();
 					}
 				});
 		dock.add(originalFields);
 		dock.setTitle(EuropeanaClientConstants.VOCORIGINALFIELDS);
 		return dock;
 	}
 
 	/**
 	 * Create the Mappable EDM fields table
 	 * 
 	 * @return A Scrollpanel with the List of Mappable EDM fields
 	 */
 	protected ScrollPanel createMappableFieldTable() {
 		ScrollPanel dock = new ScrollPanel();
 		final SelectionModel<EdmFieldDTO> selectionModel = new SingleSelectionModel<EdmFieldDTO>();
 		mappableFields = new CellList<EdmFieldDTO>(new MappableCell());
 		mappableFields.setSelectionModel(selectionModel);
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					@Override
 					public void onSelectionChange(SelectionChangeEvent event) {
 						edmField = ((SingleSelectionModel<EdmFieldDTO>) selectionModel)
 								.getSelectedObject().getField();
 					}
 				});
 		dock.add(mappableFields);
 		dock.setTitle(EuropeanaClientConstants.VOCMAPPABLEFIELDS);
 		return dock;
 	}
 
 	/**
 	 * Method creating the Split layout panel
 	 * 
 	 * @return The split layout panel for importing metadata
 	 */
 	protected SplitLayoutPanel createImportTable() {
 		SplitLayoutPanel split = new SplitLayoutPanel();
 		split.addNorth(createImportFields(), 150);
 
 		split.addSouth(createDeleteVocabulary(), 150);
 		split.add(createUploadedVocabularies());
 		return split;
 	}
 
 	/**
 	 * Create a table with buttons to delete and edit vocabulary
 	 * 
 	 * @return The HTML table for deleting the vocabulary
 	 */
 	private FlexTable createDeleteVocabulary() {
 		FlexTable flex = new FlexTable();
 		deleteVocabulary = new Button(
 				EuropeanaClientConstants.VOCDELETEVOCABULARY);
 		editVocabulary = new Button(EuropeanaClientConstants.VOCEDITVOCABULARY);
 		refreshVocabularies = new Button(
 				EuropeanaClientConstants.VOCREFRESHVOCABULARY);
 		deleteVocabulary.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				importedVocabulary.removeVocabulary(vocabulary.getName(),
 						new AsyncCallback<Boolean>() {
 							@Override
 							public void onSuccess(Boolean result) {
 								Window.alert("Vocabulary "
 										+ vocabulary.getName()
 										+ " was deleted successfully");
 								fillVocabularyTable();
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								Window.alert("Problem deleting vocabulary "
 										+ vocabulary.getName() + ": "
 										+ caught.getMessage());
 							}
 						});
 			}
 		});
 		editVocabulary.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				fillMappingTables();
 				importVocabulary.selectTab(1);
 			}
 		});
 		refreshVocabularies.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				fillVocabularyTable();
 
 			}
 		});
 		flex.setWidget(0, 0, editVocabulary);
 		flex.setWidget(0, 1, deleteVocabulary);
 		flex.setWidget(0, 2, refreshVocabularies);
 		return flex;
 	}
 
 	/**
 	 * Create the uploaded vocabulary table
 	 * 
 	 * @return A ScrollPanel with all the vocabularies uploaded
 	 */
 	protected ScrollPanel createUploadedVocabularies() {
 		ScrollPanel scroll = new ScrollPanel();
 		vocabularyTable = new CellList<ControlledVocabularyDTO>(
 				new VocabularyCell());
 		final SelectionModel<ControlledVocabularyDTO> selectionModel = new SingleSelectionModel<ControlledVocabularyDTO>();
 		vocabularyTable.setSelectionModel(selectionModel);
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					@Override
 					public void onSelectionChange(SelectionChangeEvent event) {
 						vocabulary = ((SingleSelectionModel<ControlledVocabularyDTO>) selectionModel)
 								.getSelectedObject();
 					}
 				});
 		scroll.add(vocabularyTable);
 		fillVocabularyTable();
 		return scroll;
 	}
 
 	/**
 	 * Method to fill in the vocabulary tables
 	 */
 	protected void fillVocabularyTable() {
 		importedVocabulary
 				.retrieveVocabularies(new AsyncCallback<List<ControlledVocabularyDTO>>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(caught.getMessage());
 					}
 
 					@Override
 					public void onSuccess(List<ControlledVocabularyDTO> result) {
 						if (result.size() > 0) {
 							vocabularyTable.setRowData(result);
 						} else {
 							vocabularyTable
 									.setEmptyListWidget(new CellList<List<ControlledVocabularyDTO>>(
 											null));
 						}
 					}
 				});
 	}
 
 	/**
 	 * Creates a form panel to upload the Controlled Vocabulary to a servlet. It
 	 * consists of a popup
 	 * 
 	 * @return The Form Panel
 	 */
 	protected FormPanel createImportFields() {
 		final FormPanel form = new FormPanel();
 		final TextBox url = new TextBox();
 		url.setEnabled(false);
 		form.setEncoding(FormPanel.ENCODING_MULTIPART);
 		FlexTable table = new FlexTable();
 		table.setCellSpacing(0);
 		table.setWidth("500px");
 		table.getColumnFormatter().getElement(0).setPropertyInt("width", 100);
 		table.getColumnFormatter().getElement(1).setPropertyInt("width", 400);
 		table.setWidget(0, 0,
 				new Label(EuropeanaClientConstants.VOCABULARYNAME));
 		TextBox vocabularyName = new TextBox();
 		setDOMID(vocabularyName, "vocabularyName");
 		vocabularyName.setName("vocabularyName");
 		table.setWidget(0, 1, vocabularyName);
 		table.setWidget(1, 0, new Label(EuropeanaClientConstants.VOCABULARYURI));
 		final TextBox vocabularyUri = new TextBox();
 		setDOMID(vocabularyUri, "vocabularyURI");
 		vocabularyUri.setName("vocabularyURI");
 		final RadioButton radioButton = new RadioButton("file", "Local File");
 		radioButton.ensureDebugId("file-local");
 		radioButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				saveAndUpload.setEnabled(true);
 				url.setEnabled(false);
 			}
 		});
 		table.setWidget(2, 0, radioButton);
 		saveAndUpload = new FileUpload();
 		saveAndUpload.setEnabled(false);
 		saveAndUpload.ensureDebugId("local");
 		saveAndUpload.setTitle(EuropeanaClientConstants.VOCSAVEANDUPLOAD);
 		table.setWidget(2, 1, saveAndUpload);
 		final RadioButton radioButton1 = new RadioButton("file", "Remote File");
 		radioButton1.ensureDebugId("file-remote");
 		radioButton1.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				saveAndUpload.setEnabled(false);
 				url.setEnabled(true);
 			}
 		});
 		table.setWidget(3, 0, radioButton1);
 
 		url.ensureDebugId("url");
 		setDOMID(url, "url");
 		url.setName("url");
 		table.setWidget(3, 1, url);
 		table.setWidget(1, 1, vocabularyUri);
 		table.setWidget(4, 0, new Label(
 				EuropeanaClientConstants.VOCABULARYRULES));
 		final TextBox vocabularyRules = new TextBox();
 		setDOMID(vocabularyRules, "vocabularyRules");
 		vocabularyRules.setName("vocabularyRules");
 		table.setWidget(4, 1, vocabularyRules);
 		table.setWidget(5, 0, new Label(
 				EuropeanaClientConstants.VOCABULARYSUFFIX));
 		TextBox vocabularySuffix = new TextBox();
 		setDOMID(vocabularySuffix, "vocabularySuffix");
 		vocabularySuffix.setName("vocabularySuffix");
 		table.setWidget(5, 1, vocabularySuffix);
 		Button submit = new Button(EuropeanaClientConstants.VOCSAVEANDUPLOAD);
 		submit.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				form.submit();
 			}
 		});
 		table.setWidget(6, 1, submit);
 		TextBox vocabularyLocation = new TextBox();
 		vocabularyLocation.setName("vocabularyLocation");
 		vocabularyLocation.setVisible(false);
 		table.setWidget(6,0,vocabularyLocation);
 		form.add(table);
 		form.addSubmitHandler(new SubmitHandler() {
 			@Override
 			public void onSubmit(SubmitEvent event) {
 				if (radioButton.getValue()) {
 					form.setMethod(FormPanel.METHOD_POST);
 					form.setAction(EuropeanaClientConstants.UPLOAD_SERVLET_URL);
 					Window.alert("Saving, you will be notified when the file is uploaded");
 				} else if (radioButton1.getValue()) {
 					form.setMethod(FormPanel.METHOD_GET);
 					form.setAction(EuropeanaClientConstants.REMOTE_UPLOAD_SERVLET_URL);
 					Window.alert("Downloading remote file, you will be notified when the file is saved");
 				} else {
 					Window.alert("Select a file");
 				}
 			}
 		});
 		form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
 			@Override
 			public void onSubmitComplete(SubmitCompleteEvent event) {
 				if (radioButton.getValue() || radioButton1.getValue()) {
 					uploadControlledVocabulary();
 					fillVocabularyTable();
 					
 				}
 			}
 		});
 		return form;
 	}
 
 	/**
 	 * Create the Controlled vocabulary in UIM
 	 */
 	protected void uploadControlledVocabulary() {
 		vocabulary = new ControlledVocabularyDTO();
 		vocabulary.setName(DOM.getElementById("vocabularyName")
 				.<InputElement> cast().getValue());
 		vocabulary.setSuffix(DOM.getElementById("vocabularySuffix")
 				.<InputElement> cast().getValue());
 		vocabulary.setLocation(retrieveFileName());
 		vocabulary.setUri(DOM.getElementById("vocabularyURI")
 				.<InputElement> cast().getValue());
 		vocabulary.setRules(DOM.getElementById("vocabularyRules")
 				.<InputElement> cast().getValue().split(" "));
 		importedVocabulary.importVocabulary(vocabulary,
 				new AsyncCallback<ControlledVocabularyDTO>() {
 					@Override
 					public void onSuccess(ControlledVocabularyDTO result) {
 						Window.alert("Upload was successful");
 						vocabulary = result;
 						mappings = result.getMapping();
 						fillMappingTables();
 						importVocabulary.selectTab(1);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("Upload failed " + caught.getMessage());
 					}
 				});
 	}
 
 	/**
 	 * Extract the File name out of a URL or URI. It is equal to File.getName();
 	 * 
 	 * @return the File name.
 	 */
 	private String retrieveFileName() {
 		if (DOM.getElementById("vocabularyName").<InputElement> cast()
 				.getValue().contains("/")) {
 			String[] directoryLocation = DOM.getElementById("vocabularyName")
 					.<InputElement> cast().getValue().split("/");
 			return directoryLocation[directoryLocation.length - 1];
 		} else {
 			return DOM.getElementById("vocabularyName").<InputElement> cast()
 					.getValue();
 		}
 	}
 
 	/**
 	 * Fill the mapping tables(initially they will be empty)
 	 */
 	private void fillMappingTables() {
 		List<OriginalFieldDTO> originals = new ArrayList<OriginalFieldDTO>();
 		for (MappingDTO map : vocabulary.getMapping()) {
 			originals.add(map.getOriginal());
 		}
 		originalFields.setRowData(originals);
 		mappedFields.setRowData(vocabulary.getMapping());
 		retrieveEdmFields();
 	}
 
 	private void retrieveEdmFields() {
 
 		importedVocabulary
 				.retrieveEdmFields(new AsyncCallback<List<EdmFieldDTO>>() {
 
 					@Override
 					public void onSuccess(List<EdmFieldDTO> result) {
 						mappableFields.setRowData(result);
 
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert("Something went wrong in retrieving the EDM fields");
 
 					}
 				});
 
 	}
 
 	/**
 	 * Set the unique DOM id for each object
 	 * 
 	 * @param widg
 	 *            The widget
 	 * @param id
 	 *            The id to set
 	 */
 	private void setDOMID(Widget widg, String id) {
 		DOM.setElementProperty(widg.getElement(), "id", id);
 	}
 
 	/**
 	 * AJAX call
 	 */
 	@Override
 	protected void asyncOnInitialize(final AsyncCallback<Widget> callback) {
 		GWT.runAsync(ImportControlledVocabularyWidget.class,
 				new RunAsyncCallback() {
 					@Override
 					public void onFailure(Throwable caught) {
 						callback.onFailure(caught);
 					}
 
 					@Override
 					public void onSuccess() {
 						callback.onSuccess(onInitialize());
 					}
 				});
 	}
 
 	/**
 	 * Original Field Cell representation
 	 * 
 	 * 
 	 */
 	private class OriginalCell extends AbstractCell<OriginalFieldDTO> {
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				OriginalFieldDTO value, SafeHtmlBuilder sb) {
 			sb.append(SafeHtmlUtils.fromString(value.getField()));
 		}
 	}
 
 	/**
 	 * EDM Field Cell representation
 	 * 
 	 * 
 	 */
 	private class MappableCell extends AbstractCell<EdmFieldDTO> {
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				EdmFieldDTO value, SafeHtmlBuilder sb) {
 			sb.appendHtmlConstant("<table><tr><td>");
 			sb.appendHtmlConstant(value.getField()
 					+ "</td></tr></table>");
 		}
 	}
 
 	/**
 	 * Mapped fields Cell representation. Currently each field will be mapped as
 	 * Original Field - Mapped Field
 	 * 
 	 * 
 	 */
 	private class MappedCell extends AbstractCell<MappingDTO> {
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				MappingDTO value, SafeHtmlBuilder sb) {
 			sb.appendHtmlConstant("<table><tr><td>");
 			sb.appendHtmlConstant(value.getOriginal().getField()
 					+ "</td><td>-></td><td>");
 			sb.appendHtmlConstant(value.getMapped().getField()
 					+ "</td></tr></table>");
 		}
 	}
 
 	/**
 	 * Controlled vocabulary field representation
 	 * 
 	 * 
 	 */
 	private class VocabularyCell extends AbstractCell<ControlledVocabularyDTO> {
 		@Override
 		public void render(com.google.gwt.cell.client.Cell.Context context,
 				ControlledVocabularyDTO value, SafeHtmlBuilder sb) {
 			sb.appendHtmlConstant("<table><tr><td>");
 			sb.appendHtmlConstant(value.getName() + "|</td><td>");
 			sb.appendHtmlConstant((value.getLocation() == null ? "Not Set"
 					: value.getLocation()) + "|</td><td>");
 			sb.appendHtmlConstant((value.getUri()) == null ? "Not Set" : value
 					.getUri() + "|</td><td>");
 			sb.appendHtmlConstant((value.getSuffix() == null ? "Not Set"
 					: value.getSuffix()) + "|</td></tr></table>");
 		}
 	}
 }
