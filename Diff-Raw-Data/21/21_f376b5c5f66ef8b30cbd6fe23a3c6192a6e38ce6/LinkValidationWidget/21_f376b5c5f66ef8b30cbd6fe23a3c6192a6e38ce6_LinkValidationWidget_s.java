 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.gui.cp.client.europeanawidgets;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import eu.europeana.uim.gui.cp.client.services.RetrievalServiceAsync;
 import eu.europeana.uim.gui.cp.shared.validation.LinkDTO;
 import eu.europeana.uim.gui.cp.shared.validation.LinksResultDTO;
 import eu.europeana.uim.gui.cp.shared.validation.MetaDataRecordDTO;
 import eu.europeana.uim.gui.cp.shared.validation.MetaDataResultDTO;
 
 import com.google.gwt.cell.client.ActionCell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.RunAsyncCallback;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import eu.europeana.uim.gui.cp.client.IngestionWidget;
 import eu.europeana.uim.gui.cp.client.services.RepositoryServiceAsync;
 import eu.europeana.uim.gui.cp.shared.CollectionDTO;
 import eu.europeana.uim.gui.cp.shared.ProviderDTO;
 
 import java.io.UnsupportedEncodingException;
 
 import com.google.gwt.http.client.URL;
 
 /**
  * Table view showing current links for link checking
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Apr 27, 2011
  */
 public class LinkValidationWidget extends IngestionWidget {
 
    // /** String PORTAL_SINGLE_RECORD_URL */
    private static final String PORTAL_SINGLE_RECORD_URL = "http://www.europeana.eu/portal/record/";
    private static final String REPOSITORY_PREVIEW_URL = "http://127.0.0.1:8181/gui/EuropeanaIngestionControlPanel/mongoImageView";
     /**
      * The UiBinder interface used by this example.
      */
     interface Binder extends UiBinder<Widget, LinkValidationWidget> {
     }
 
     private final RepositoryServiceAsync  repositoryService;
     private final RetrievalServiceAsync   retrievalService;
 
     private final List<MetaDataRecordDTO> records     = new ArrayList<MetaDataRecordDTO>();
     private final List<ProviderDTO>       providers   = new ArrayList<ProviderDTO>();
     private final List<CollectionDTO>     collections = new ArrayList<CollectionDTO>();
     private final List<LinkDTO>           linkList    = new ArrayList<LinkDTO>();
 
     /**
      * Box with providers for selection
      */
     @UiField
     ListBox                               providerBox;
     /**
      * Box with collections for selection
      */
     @UiField
     ListBox                               collectionBox;
     /**
      * Box with collections for selection
      */
     @UiField
     TextBox                               queryBox;
     /**
      * Button to trigger search in repository
      */
     @UiField
     Button                                searchButton;
 
     /**
      * Panel that holds the Shows the
      */
     @UiField
     Frame                                 leftFrame;
 
     @UiField(provided = true)
     CellTable<LinkDTO>                    linkCellTable;
 
     /**
      * The main CellTable.
      */
     @UiField(provided = true)
     CellTable<MetaDataRecordDTO>          cellTable;
 
     /**
      * The pager used to change the range of data.
      */
     @UiField(provided = true)
     SimplePager                           pager;
 
     /**
      * Creates a new instance of this class.
      * 
      * @param repositoryService
      * @param retrievalService
      */
     public LinkValidationWidget(RepositoryServiceAsync repositoryService,
                                 RetrievalServiceAsync retrievalService) {
         super("Ingested Metadata Preview",
                 "This view allows the validation of the links and data in the production and test environments.");
         this.repositoryService = repositoryService;
         this.retrievalService = retrievalService;
     }
 
     @Override
     public Widget onInitialize() {
         cellTable = new CellTable<MetaDataRecordDTO>(MetaDataRecordDTO.KEY_PROVIDER);
         cellTable.setWidth("100%", true);
         cellTable.setPageSize(5);
         cellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
             @Override
             public void onRangeChange(RangeChangeEvent arg0) {
                 if (collections.size() > 0 && collectionBox.getSelectedIndex() >= 0 &&
                     collectionBox.getSelectedIndex() < collections.size()) {
                     CollectionDTO collection = collections.get(collectionBox.getSelectedIndex());
                     if (collection != null) {
                         Range range = cellTable.getVisibleRange();
                         int start = range.getStart();
                         int length = range.getLength();
                         updateRows(collection, start, length, queryBox.getText());
                     }
                 } else {
                     Window.alert("Please select a provider and collection to browse the repository!");
                 }
             }
         });
 
         // Add a selection model to handle user selection.
 
         final SingleSelectionModel<LinkDTO> linkSelectionModel = new SingleSelectionModel<LinkDTO>();
         // Add a selection model to handle user selection.
 
         linkCellTable = new CellTable<LinkDTO>();
         linkCellTable.setSelectionModel(linkSelectionModel);
         linkSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
             @Override
             public void onSelectionChange(SelectionChangeEvent event) {
                 LinkDTO selected = linkSelectionModel.getSelectedObject();
                 leftFrame.setUrl(selected.getUrl());
             }
 
         });
 
         final SingleSelectionModel<MetaDataRecordDTO> selectionModel = new SingleSelectionModel<MetaDataRecordDTO>();
         cellTable.setSelectionModel(selectionModel);
         selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
             @Override
             public void onSelectionChange(SelectionChangeEvent event) {
                 MetaDataRecordDTO selected = selectionModel.getSelectedObject();
                 if (selected != null) {
                     updateBottomFrames(selected, linkSelectionModel);
                 }
             }
 
         });
 
         final ListDataProvider<MetaDataRecordDTO> dataProvider = new ListDataProvider<MetaDataRecordDTO>();
         dataProvider.setList(records);
         dataProvider.addDataDisplay(cellTable);
 
         SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
         pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
         pager.setDisplay(cellTable);
 
         initTableColumns();
 
         linkCellTable.setWidth("100%", true);
 
         final ListDataProvider<LinkDTO> linkDataProvider = new ListDataProvider<LinkDTO>();
         linkDataProvider.setList(linkList);
         linkDataProvider.addDataDisplay(linkCellTable);
 
         initLinkTableColumns();
 
         Binder uiBinder = GWT.create(Binder.class);
         Widget widget = uiBinder.createAndBindUi(this);
 
         repositoryService.getProviders(new AsyncCallback<List<ProviderDTO>>() {
             @Override
             public void onFailure(Throwable caught) {
                 caught.printStackTrace();
             }
 
             @Override
             public void onSuccess(List<ProviderDTO> result) {
                 providers.clear();
                 providerBox.clear();
                 
                 
                 Collections.sort(result, new Comparator<ProviderDTO>() {
                     @Override
                     public int compare(ProviderDTO o1, ProviderDTO o2) {
                         String n1 = o1.getCountry() == null ? "eu: " + o1.getName()
                                 : o1.getCountry() + ": " + o1.getName();
                         String n2 = o2.getCountry() == null ? "eu: " + o2.getName()
                                 : o2.getCountry() + ": " + o2.getName();
 
                         return n1.compareTo(n2);
                     }
                 });
                 
                 providers.addAll(result);
                 
                 for (ProviderDTO provider : providers) {
                     String name = provider.getCountry() == null ? "eu: " + provider.getName()
                             : provider.getCountry() + ": " + provider.getName();
                     providerBox.addItem(name);
                 }
 
                 if (providers.size() == 0) { return; }
 
                 repositoryService.getCollections(providers.get(0).getId(),
                         new AsyncCallback<List<CollectionDTO>>() {
                             @Override
                             public void onFailure(Throwable caught) {
                                 caught.printStackTrace();
                             }
 
                             @Override
                             public void onSuccess(List<CollectionDTO> result) {
                                 collections.clear();
                                 collectionBox.clear();
                                 Collections.sort(result, new Comparator<CollectionDTO>() {
                                     @Override
                                     public int compare(CollectionDTO o1, CollectionDTO o2) {
                                         String n1 = o1.getMnemonic() + ": " + o1.getName();
                                         String n2 = o2.getMnemonic() + ": " + o2.getName();
                                         return n1.compareTo(n2);
                                     }
                                 });
 
                                 
                                 collections.addAll(result);
 
                                 for (CollectionDTO collection : collections) {
                                     String name = collection.getMnemonic() + ": " + collection.getName();
                                     collectionBox.addItem(name);
                                 }
                             }
                         });
             }
         });
 
         providerBox.addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
                 ProviderDTO provider = providers.get(providerBox.getSelectedIndex());
 
                 repositoryService.getCollections(provider.getId(),
                         new AsyncCallback<List<CollectionDTO>>() {
                             @Override
                             public void onFailure(Throwable caught) {
                                 caught.printStackTrace();
                             }
 
                             @Override
                             public void onSuccess(List<CollectionDTO> result) {
                                 collections.clear();
                                 collectionBox.clear();
                                 
                                 Collections.sort(result, new Comparator<CollectionDTO>() {
                                     @Override
                                     public int compare(CollectionDTO o1, CollectionDTO o2) {
                                         String n1 = o1.getMnemonic() + ": " + o1.getName();
                                         String n2 = o2.getMnemonic() + ": " + o2.getName();
                                         return n1.compareTo(n2);
                                     }
                                 });
 
                                 
                                 collections.addAll(result);
 
                                 for (CollectionDTO collection : collections) {
                                     String name = collection.getMnemonic() + ": " + collection.getName();
                                     collectionBox.addItem(name);
                                 }
                             }
                         });
             }
         });
         searchButton.setText("Search");
         searchButton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent arg0) {
                 if (collections.size() > 0 && collectionBox.getSelectedIndex() >= 0 &&
                     collectionBox.getSelectedIndex() < collections.size()) {
                     CollectionDTO collection = collections.get(collectionBox.getSelectedIndex());
                     if (collection != null) {
                         updateRows(collection, 0, 6, queryBox.getText());
                     }
                 } else {
                     Window.alert("Please select a provider and collection to browse the repository!");
                 }
             }
         });
 
         return widget;
     }
 
     private void updateRows(final CollectionDTO collection, final int offset, final int maxSize,
             String recordId) {
 
         retrievalService.getRecordsForCollection((String)collection.getId(), offset, maxSize, recordId,
                 new AsyncCallback<MetaDataResultDTO>() {
                     @Override
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                     }
 
                     @Override
                     public void onSuccess(MetaDataResultDTO result) {
                         records.clear();
                         cellTable.setRowCount(result.getNumberRecords());
                         records.addAll(result.getRecords());
                         cellTable.setRowData(offset, records);
                     }
                 });
 
     }
 
     @Override
     protected void asyncOnInitialize(final AsyncCallback<Widget> callback) {
         GWT.runAsync(LinkValidationWidget.class, new RunAsyncCallback() {
 
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
      * Add the columns to the table.
      */
     private void initTableColumns() {
 
         // ID
         Column<MetaDataRecordDTO, String> idColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
 
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getId().toString();
 
             }
         };
         cellTable.addColumn(idColumn, "ID");
         cellTable.setColumnWidth(idColumn, 10, Unit.PCT);
 
         // Title
         Column<MetaDataRecordDTO, String> titleColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getTitle();
             }
         };
         cellTable.addColumn(titleColumn, "Title");
         cellTable.setColumnWidth(titleColumn, 15, Unit.PCT);
 
         // Creator
         Column<MetaDataRecordDTO, String> creatorColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getCreator();
             }
         };
         cellTable.addColumn(creatorColumn, "Creator");
         cellTable.setColumnWidth(creatorColumn, 13, Unit.PCT);
 
         // Contributor
         Column<MetaDataRecordDTO, String> contributorColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getContributor();
             }
         };
         cellTable.addColumn(contributorColumn, "Publisher");
         cellTable.setColumnWidth(contributorColumn, 13, Unit.PCT);
 
         // Year
         Column<MetaDataRecordDTO, String> yearColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getPublicationYear();
             }
         };
         cellTable.addColumn(yearColumn, "Year");
         cellTable.setColumnWidth(yearColumn, 9, Unit.PCT);
 
         // Place
         Column<MetaDataRecordDTO, String> countryColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getPublicationPlace();
             }
         };
         cellTable.addColumn(countryColumn, "Place");
         cellTable.setColumnWidth(countryColumn, 10, Unit.PCT);
 
         // Language
         Column<MetaDataRecordDTO, String> langColumn = new Column<MetaDataRecordDTO, String>(
                 new TextCell()) {
             @Override
             public String getValue(MetaDataRecordDTO object) {
                 return object.getWorkLanguage();
             }
         };
         cellTable.addColumn(langColumn, "Language");
         cellTable.setColumnWidth(langColumn, 10, Unit.PCT);
 
         // Show Record Details
         Column<MetaDataRecordDTO, MetaDataRecordDTO> plainColumn = new Column<MetaDataRecordDTO, MetaDataRecordDTO>(
                 new ActionCell<MetaDataRecordDTO>("Content",
                         new ActionCell.Delegate<MetaDataRecordDTO>() {
                             @Override
                             public void execute(MetaDataRecordDTO record) {
                                 final DialogBox updateBox = new RecordDetailsDialogBox(
                                         (String)record.getId(), retrievalService);
                                 updateBox.show();
                             }
                         })) {
             @Override
             public MetaDataRecordDTO getValue(MetaDataRecordDTO object) {
                 return object;
             }
         };
         cellTable.addColumn(plainColumn, "Show");
         cellTable.setColumnWidth(plainColumn, 4, Unit.PCT);
 
 
     }
 
     private void initLinkTableColumns() {
 
         // Description
         Column<LinkDTO, String> typeColumn = new Column<LinkDTO, String>(new TextCell()) {
 
             @Override
             public String getValue(LinkDTO object) {
                 return object.getDescription().toString();
             }
 
         };
         linkCellTable.addColumn(typeColumn, "Description");
         linkCellTable.setColumnWidth(typeColumn, 50, Unit.PCT);
 
         // Link
 
         Column<LinkDTO, String> linkColumn = new Column<LinkDTO, String>(new LinkCell()) {
 
             @Override
             public String getValue(LinkDTO object) {
                 return object.getUrl();
             }
         };
         linkCellTable.addColumn(linkColumn, "URL");
         linkCellTable.setColumnWidth(linkColumn, 50, Unit.PCT);
     }
 
     private void updateBottomFrames(MetaDataRecordDTO selected,
             final SingleSelectionModel<LinkDTO> linkSelectionModel) {
 
         // try to get the current position in the link selection to try to restore it for the newly
         // selected record
 
         int selectedIdx = 0;
         for (int i = 0; i < linkList.size(); i++) {
             if (linkSelectionModel.isSelected(linkList.get(i))) {
                 selectedIdx = i;
                 break;
             }
         }
 
         final int oldSelectedIdx = selectedIdx;
 
         // get the newly selected record

         final String recordId = (String)selected.getId();
        
        String collid = selected.getId().toString();

        String uencoderecordId = URL.encodeQueryString(recordId);
        

        
         linkList.clear();
        linkList.add(new LinkDTO(PORTAL_SINGLE_RECORD_URL + "/" + collid +"/" + uencoderecordId, "Record in portal"));
        linkList.add(new LinkDTO(REPOSITORY_PREVIEW_URL + "?recordID=" + uencoderecordId, "Preview cahched images in repository"));
        
        
         retrievalService.getLinks(recordId, new AsyncCallback<LinksResultDTO>() {
 
             @Override
             public void onSuccess(LinksResultDTO result) {
                 linkList.addAll(result.getLinks());
                 linkCellTable.setRowCount(linkList.size());
                 linkCellTable.setRowData(linkList);
 
                 int toSelectIdx = 0;
                 if (oldSelectedIdx < linkList.size()) {
                     toSelectIdx = oldSelectedIdx;
                 }
                 // set the links to the first row (portal url)
                 linkSelectionModel.setSelected(linkList.get(toSelectIdx), true);
             }
 
             @Override
             public void onFailure(Throwable caught) {
                 Window.alert("Retrieval of links failed !" + getCustomStackTrace(caught));
                 caught.printStackTrace();
             }
         });
 
     }
 
     private static String getCustomStackTrace(Throwable aThrowable) {
         // add the class name and any message passed to constructor
         final StringBuilder result = new StringBuilder("Trace: ");
         result.append(aThrowable.toString());
         final String NEW_LINE = "\n";
         result.append(NEW_LINE);
 
         // add each element of the stack trace
         for (StackTraceElement element : aThrowable.getStackTrace()) {
             result.append(element);
             result.append(NEW_LINE);
         }
         return result.toString();
     }
 }
