 /*******************************************************************************
  * Copyright 2009 Regents of the University of Minnesota. All rights
  * reserved.
  * Copyright 2009 Mayo Foundation for Medical Education and Research.
  * All rights reserved.
  *
  * This program is made available under the terms of the Eclipse
  * Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR
  * IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS
  * OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
  * PARTICULAR PURPOSE.  See the License for the specific language
  * governing permissions and limitations under the License.
  *
  * Contributors:
  * Minnesota Supercomputing Institute - initial API and implementation
  ******************************************************************************/
 
 package edu.umn.msi.tropix.webgui.client.components.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Function;
 import com.google.common.base.Supplier;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Timer;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.smartgwt.client.data.DataSource;
 import com.smartgwt.client.data.DataSourceField;
 import com.smartgwt.client.types.FieldType;
 import com.smartgwt.client.types.ListGridFieldType;
 import com.smartgwt.client.types.SelectionStyle;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Button;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.Window;
 import com.smartgwt.client.widgets.events.DoubleClickEvent;
 import com.smartgwt.client.widgets.events.DoubleClickHandler;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
 import com.smartgwt.client.widgets.grid.events.SelectionEvent;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.Layout;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 import edu.umn.msi.tropix.models.BowtieAnalysis;
 import edu.umn.msi.tropix.models.BowtieIndex;
 import edu.umn.msi.tropix.models.Database;
 import edu.umn.msi.tropix.models.FileType;
 import edu.umn.msi.tropix.models.Folder;
 import edu.umn.msi.tropix.models.ITraqQuantitationAnalysis;
 import edu.umn.msi.tropix.models.ITraqQuantitationTraining;
 import edu.umn.msi.tropix.models.IdentificationAnalysis;
 import edu.umn.msi.tropix.models.IdentificationParameters;
 import edu.umn.msi.tropix.models.InternalRequest;
 import edu.umn.msi.tropix.models.Note;
 import edu.umn.msi.tropix.models.ProteomicsRun;
 import edu.umn.msi.tropix.models.ScaffoldAnalysis;
 import edu.umn.msi.tropix.models.TissueSample;
 import edu.umn.msi.tropix.models.TropixFile;
 import edu.umn.msi.tropix.models.TropixObject;
 import edu.umn.msi.tropix.models.VirtualFolder;
 import edu.umn.msi.tropix.models.locations.Locations;
 import edu.umn.msi.tropix.models.proteomics.IdentificationType;
 import edu.umn.msi.tropix.models.utils.ModelUtils;
 import edu.umn.msi.tropix.models.utils.TropixObjectVisitorImpl;
 import edu.umn.msi.tropix.models.utils.TropixObjectVistorUtils;
 import edu.umn.msi.tropix.webgui.client.AsyncCallbackImpl;
 import edu.umn.msi.tropix.webgui.client.Resources;
 import edu.umn.msi.tropix.webgui.client.Session;
 import edu.umn.msi.tropix.webgui.client.components.CanvasComponent;
 import edu.umn.msi.tropix.webgui.client.components.ComponentFactory;
 import edu.umn.msi.tropix.webgui.client.components.LocationCommandComponentFactory;
 import edu.umn.msi.tropix.webgui.client.components.MultiSelectionWindowComponent;
 import edu.umn.msi.tropix.webgui.client.components.PageConfiguration;
 import edu.umn.msi.tropix.webgui.client.components.tree.TreeItem;
 import edu.umn.msi.tropix.webgui.client.components.tree.TropixObjectTreeItem;
 import edu.umn.msi.tropix.webgui.client.constants.PageConstants;
 import edu.umn.msi.tropix.webgui.client.identification.ParametersPanel;
 import edu.umn.msi.tropix.webgui.client.identification.ParametersPanelFactory;
 import edu.umn.msi.tropix.webgui.client.mediators.ActionMediator;
 import edu.umn.msi.tropix.webgui.client.mediators.LocationActionEventImpl;
 import edu.umn.msi.tropix.webgui.client.mediators.LocationUpdateMediator;
 import edu.umn.msi.tropix.webgui.client.mediators.LocationUpdateMediator.UpdateEvent;
 import edu.umn.msi.tropix.webgui.client.mediators.NavigationSelectionMediator;
 import edu.umn.msi.tropix.webgui.client.models.ModelFunctions;
 import edu.umn.msi.tropix.webgui.client.modules.ModuleManager;
 import edu.umn.msi.tropix.webgui.client.smart.handlers.CommandClickHandlerImpl;
 import edu.umn.msi.tropix.webgui.client.smart.handlers.CommandDoubleClickHandlerImpl;
 import edu.umn.msi.tropix.webgui.client.utils.FileSizeUtils;
 import edu.umn.msi.tropix.webgui.client.utils.Listener;
 import edu.umn.msi.tropix.webgui.client.utils.Maps;
 import edu.umn.msi.tropix.webgui.client.utils.StringUtils;
 import edu.umn.msi.tropix.webgui.client.widgets.Buttons;
 import edu.umn.msi.tropix.webgui.client.widgets.CanvasWithOpsLayout;
 import edu.umn.msi.tropix.webgui.client.widgets.ClientListGrid;
 import edu.umn.msi.tropix.webgui.client.widgets.GWTDownloadFormPanel;
 import edu.umn.msi.tropix.webgui.client.widgets.PageFrameSupplierImpl;
 import edu.umn.msi.tropix.webgui.client.widgets.PageFrameSupplierImpl.Section;
 import edu.umn.msi.tropix.webgui.client.widgets.SmartUtils;
 import edu.umn.msi.tropix.webgui.client.widgets.WidgetSupplierImpl;
 import edu.umn.msi.tropix.webgui.services.object.ObjectService;
 import edu.umn.msi.tropix.webgui.services.object.TropixObjectContext;
 import edu.umn.msi.tropix.webgui.services.protip.IdentificationParametersService;
 import edu.umn.msi.tropix.webgui.services.session.Module;
 import edu.umn.msi.tropix.webgui.services.tropix.RequestService;
 
 @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SIC", justification = "Refactoring inner classes to be static would result in considerable duplication of code.")
 public class PageComponentFactoryImpl implements ComponentFactory<PageConfiguration, CanvasComponent<Layout>> {
   private static final String SECTION_ITEM_BORDER = "0px";
   private final Function<TropixObject, String> iconFunction = ModelFunctions.getIconFunction16();
   private ActionMediator actionMediator;
   private ComponentFactory<PageConfiguration, ? extends CanvasComponent<? extends Canvas>> sharingComponentFactory;
   private Supplier<? extends MultiSelectionWindowComponent<TreeItem, ? extends Window>> objectSelectionWindowComponentSupplier;
   private ComponentFactory<Note, ? extends CanvasComponent<? extends Canvas>> noteComponentFactory;
   private NavigationSelectionMediator navigationSelectionMediator;
   private ParametersPanelFactory parametersPanelFactory;
   private LocationCommandComponentFactory<? extends Command> deleteCommandComponentFactory, renameCommandComponentFactory,
       changeDescriptionCommandComponentFactory;
   private final HashMap<String, Boolean> expandedMap = new HashMap<String, Boolean>();
   private ModuleManager moduleManager;
   private Session session;
   
   @Inject
   public void setSession(final Session session) {
     this.session = session;
   }
   
   @Inject
   public void setModuleManager(final ModuleManager moduleManager) {
     this.moduleManager = moduleManager;
   }
 
   @Inject
   public void setSharingComponentFactory(
       @Named("sharing") final ComponentFactory<PageConfiguration, ? extends CanvasComponent<? extends Canvas>> sharingComponentFactory) {
     this.sharingComponentFactory = sharingComponentFactory;
   }
 
   @Inject
   public void setActionMediator(final ActionMediator actionMediator) {
     this.actionMediator = actionMediator;
   }
 
   public CanvasComponent<Layout> get(final PageConfiguration pageConfiguration) {
     return new PageComponentImpl(pageConfiguration);
   }
 
   private class PageComponentImpl extends WidgetSupplierImpl<Layout> implements CanvasComponent<Layout> {
     PageComponentImpl(final PageConfiguration pageConfiguration) {
       this.setWidget(new PageVisitor(pageConfiguration).get());
     }
   }
 
   class PageVisitor extends TropixObjectVisitorImpl implements CanvasComponent<Layout> {
     private final PageFrameSupplierImpl pageFrameSupplier;
     private ClientListGrid downloadGrid, associationGrid, operationsGrid;
     private final MetadataSection metadataSection;
     private final String name;
     private final TropixObjectTreeItem tropixObjectTreeItem;
     private final TropixObjectContext tropixObjectContext;
     private final PageConfiguration pageConfiguration;
     private String type = "Object";
 
     private Section quickDownloadSection = null, sharingSection = null, associationSection = null, viewSection = null, operationsSection = null;
 
     public PageVisitor(final PageConfiguration pageConfiguration) {
       this.tropixObjectContext = pageConfiguration.getTropixObjectContext();
       this.pageConfiguration = pageConfiguration;
       this.tropixObjectTreeItem = pageConfiguration.getLocation();
       this.name = tropixObjectTreeItem.getObject().getName();
       this.metadataSection = new MetadataSection(tropixObjectTreeItem.getObject());
       final String title = StringUtils.sanitize(this.name) + " (" + this.type + ")";
       final String icon = iconFunction.apply(tropixObjectTreeItem.getObject());
       TropixObjectVistorUtils.visit(tropixObjectTreeItem.getObject(), this);
       this.pageFrameSupplier = new PageFrameSupplierImpl(title, icon, Arrays.asList(metadataSection, quickDownloadSection, associationSection,
           operationsSection, sharingSection, viewSection), expandedMap);
     }
 
     @Override
     public void visitTropixObject(final TropixObject tropixObject) {
       this.type = ModelFunctions.getTypeFunction().apply(tropixObject);
       if(moduleManager.containsModules(Module.SHARING)) {
         initSharingSection(tropixObject);
       }
       this.initOperationsSection();
     }
 
     @Override
     public void visitInternalRequest(final InternalRequest internalRequest) {
       this.metadataSection.set("Request State", internalRequest.getState()); // Will be sanitized
       this.addOperation("Get Status Report", new Command() {
         public void execute() {
           RequestService.Util.getInstance().getStatusReport(internalRequest.getId(), new AsyncCallbackImpl<String>() {
             @Override
             public void onSuccess(final String statusReport) {
               SC.say("Request Status Report: " + StringUtils.sanitize(statusReport));
             }
           });
         }
       });
     }
 
     @Override
     public void visitNote(final Note note) {
       this.viewSection = new Section();
       this.viewSection.setTitle("View Wiki Note");
       this.viewSection.setResizeable(false);
       this.viewSection.setExpanded(true);
 
       final Canvas viewCanvas = noteComponentFactory.get(note).get();
       viewCanvas.setHeight(500);
       viewCanvas.setWidth100();
       this.viewSection.setItem(viewCanvas);
     }
 
     private Long getFileSize(final TropixFile tropixFile) {
       return null; // tropixFile.getPhysicalFile() == null ? null : tropixFile.getPhysicalFile().getSize();
     }
 
     @Override
     public void visitTropixFile(final TropixFile tropixFile) {
       initDownloads();
       initFileMetadata(tropixFile);
       initDownloadThisFile(tropixFile);
     }
 
     private void initFileMetadata(final TropixFile tropixFile) {
       final FileType fileType = tropixFile.getFileType();
       if(fileType != null) {
         this.metadataSection.set("File Type", StringUtils.toString(fileType.getShortName()));
       }
 
       Long fileSize = getFileSize(tropixFile);
       if(fileSize != null) {
         this.metadataSection.set("File Size", FileSizeUtils.formatFileSize(fileSize));
       }
       this.metadataSection.set("File Storage Service", StringUtils.toString(tropixFile.getStorageServiceUrl()));
       this.metadataSection.set("File Storage Service Id", StringUtils.toString(tropixFile.getFileId()));
     }
 
     private void initDownloadThisFile(final TropixFile tropixFile) {
       final ListGridRecord record = new ListGridRecord();
       record.setAttribute("id", tropixFile.getId());
       record.setAttribute("filename", StringUtils.sanitize(tropixFile.getName()));
       record.setAttribute("File", "This File");
       // SmartGWT bug hack: Have to delay the add or add two records or this just doesn't work.
       new Timer() {
         public void run() {
           downloadGrid.addData(record);
         }
       }.schedule(1);
     }
 
     @Override
     public void visitFolder(final Folder folder) {
       this.addAssociations(folder, "Contents", "contents", true);
       if(tropixObjectContext.isModifiable()) {
         this.addOperation("Create New Subfolder", new Command() {
           public void execute() {
             actionMediator.handleEvent(LocationActionEventImpl.forItems("newItemFolder", Arrays.<TreeItem>asList(tropixObjectTreeItem)));
           }
         });
       }
     }
 
     @Override
     public void visitITraqQuantitationAnalysis(final ITraqQuantitationAnalysis iTraqQuantitationAnalysis) {
       initDownloads();
 
       this.addQuickDownload(iTraqQuantitationAnalysis, "Raw Output (.xml)", iTraqQuantitationAnalysis.getName(), "output");
       final ListGridRecord record = new ListGridRecord();
       record.setAttribute("id", iTraqQuantitationAnalysis.getId());
       record.setAttribute("filename", iTraqQuantitationAnalysis.getName() + ".xls");
       record.setAttribute("type", "quantification");
       record.setAttribute("File", "Output as Spreadsheet (.xls)");
       this.downloadGrid.addData(record);
       this.addQuickDownload(iTraqQuantitationAnalysis, "Input Spectrum Report (.xls)", iTraqQuantitationAnalysis.getName() + " Input", "report");
 
       this.addAssociations(iTraqQuantitationAnalysis, "Output File", "output", false);
       this.addAssociations(iTraqQuantitationAnalysis, "Input Spectrum Report", "report", false);
     }
 
     @Override
     public void visitITraqQuantitationTraining(final ITraqQuantitationTraining iTraqQuantitationTraining) {
       initDownloads();
 
       this.addQuickDownload(iTraqQuantitationTraining, "Training File (.xml)", iTraqQuantitationTraining.getName(), "trainingFile");
       this.addQuickDownload(iTraqQuantitationTraining, "Input Spectrum Report (.xls)", iTraqQuantitationTraining.getName() + " Input", "report");
 
       this.addAssociations(iTraqQuantitationTraining, "Training File", "trainingFile", false);
       this.addAssociations(iTraqQuantitationTraining, "Input Spectrum Report", "report", false);
     }
 
     @Override
     public void visitBowtieAnalysis(final BowtieAnalysis bowtieAnalysis) {
       this.initDownloads();
 
       this.addQuickDownload(bowtieAnalysis, "Output (.txt)", bowtieAnalysis.getName(), "output");
 
       this.addAssociations(bowtieAnalysis, "Output File", "output", false);
       this.addAssociations(bowtieAnalysis, "Input Sequence Databases", "databases", true);
     }
 
     @Override
     public void visitProteomicsRun(final ProteomicsRun proteomicsRun) {
       initDownloads();
       this.addQuickDownload(proteomicsRun, "MzXML File", proteomicsRun.getName(), "mzxml");
       this.addAssociations(proteomicsRun, "MzXML File", "mzxml", false);
       this.addAssociations(proteomicsRun, "Protein Identification Searches", "identificationAnalyses", true);
 
       this.addQuickDownload(proteomicsRun, "Source File", proteomicsRun.getName(), "source");
       this.addAssociations(proteomicsRun, "Source File", "source", false);
 
       addMgf(proteomicsRun, "MGF File (standard)", "DEFAULT");
       addMgf(proteomicsRun, "MGF File (MSM style)", "MSM");
       addMgf(proteomicsRun, "MGF File (ProteinPilot style)", "PROTEIN_PILOT");
     }
 
     private void addMgf(final ProteomicsRun proteomicsRun, final String title, final String mgfStyle) {
       final ListGridRecord record = new ListGridRecord();
       record.setAttribute("id", proteomicsRun.getId());
       record.setAttribute("filename", proteomicsRun.getName() + ".mgf");
       record.setAttribute("type", "mgf");
       record.setAttribute("File", title + " (.mgf)");
 
       final Map<String, Object> parameters = Maps.newLinkedHashMap();
       parameters.put("mgfStyle", mgfStyle);
       record.setAttribute("parameters", parameters);
 
       this.downloadGrid.addData(record);
     }
 
     @Override
     public void visitDatabase(final Database database) {
       this.addQuickDownload(database, "Sequence File", database.getName(), "databaseFile");
       this.addAssociations(database, "Sequence File", "databaseFile", false);
       metadataSection.set("Database Format", database.getType());
     }
 
     @Override
     public void visitBowtieIndex(final BowtieIndex bowtieIndex) {
       this.addQuickDownload(bowtieIndex, "Indexes File (.ebwt.zip)", bowtieIndex.getName(), "indexesFile");
       this.addAssociations(bowtieIndex, "Indexes File", "indexesFile", false);
     }
 
     @Override
     public void visitIdentificationAnalysis(final IdentificationAnalysis identificationAnalysis) {
       final IdentificationType type = IdentificationType.fromParameterType(identificationAnalysis.getIdentificationProgram());
       if(type != null) {
         this.metadataSection.set("Identification Type", type.getDisplay());
       }
       this.addQuickDownload(identificationAnalysis, "Output", identificationAnalysis.getName(), "output");
       this.addAssociations(identificationAnalysis, "Output File", "output", false);
       this.addAssociations(identificationAnalysis, "Parameters", "parameters", false);
       this.addAssociations(identificationAnalysis, "Database", "database", false);
       this.addAssociations(identificationAnalysis, "Scaffold Analyses", "scaffoldAnalyses", true);
     }
 
     @Override
     public void visitScaffoldAnalysis(final ScaffoldAnalysis scaffoldAnalysis) {
       this.addQuickDownload(scaffoldAnalysis, "Output (.sfd/.sf3)", scaffoldAnalysis.getName(), "outputs");
       this.addQuickDownload(scaffoldAnalysis, "Input (.xml)", scaffoldAnalysis.getName(), "input");
 
       this.addAssociations(scaffoldAnalysis, "Output File", "outputs", false);
       this.addAssociations(scaffoldAnalysis, "Input File", "input", false);
       this.addAssociations(scaffoldAnalysis, "Protein Identification Searches", "identificationAnalyses", true);
     }
 
     private Command getHideSharedFolderCommand(final VirtualFolder virtualFolder) {
       return new Command() {
         public void execute() {
           ObjectService.Util.getInstance().hideSharedFolder(virtualFolder.getId(), new AsyncCallbackImpl<Void>() {
             @Override
             public void onSuccess(final Void ignored) {
               LocationUpdateMediator.getInstance().onEvent(new UpdateEvent(Locations.MY_SHARED_FOLDERS_ID, null));
             }
           });
         }
       };
     }
 
     private Command getHideGroupSharedFolderCommand(final VirtualFolder virtualFolder) {
       return new Command() {
         public void execute() {
          ObjectService.Util.getInstance().hideGroupSharedFolder(virtualFolder.getId(), session.getPrimaryGroup().getId(), new AsyncCallbackImpl<Void>() {
             @Override
             public void onSuccess(final Void ignored) {
               LocationUpdateMediator.getInstance().onEvent(new UpdateEvent(Locations.MY_GROUP_SHARED_FOLDERS_ID, null));
             }
           });
         }
       };
     }
 
     private Command getAddItemsToSharedFolderCommand(final VirtualFolder virtualFolder) {
       return new Command() {
         public void execute() {
           final MultiSelectionWindowComponent<TreeItem, ? extends Window> component = objectSelectionWindowComponentSupplier.get();
           component.setMultiSelectionCallback(new Listener<Collection<TreeItem>>() {
             public void onEvent(final Collection<TreeItem> selectedObjects) {
               final ArrayList<String> ids = new ArrayList<String>(selectedObjects.size());
               for(final TreeItem selectedObject : selectedObjects) {
                 ids.add(selectedObject.getId());
               }
               ObjectService.Util.getInstance().addToSharedFolder(ids, virtualFolder.getId(), false, new AsyncCallbackImpl<Void>() {
                 @Override
                 public void onSuccess(final Void ignored) {
                   LocationUpdateMediator.getInstance().onEvent(new UpdateEvent(virtualFolder.getId(), null));
                 }
               });
             }
           });
           component.execute();
         }
       };
     }
 
     @Override
     public void visitVirtualFolder(final VirtualFolder virtualFolder) {
       if(Locations.isMySharedFoldersItem(tropixObjectTreeItem.getParent())) {
         this.addOperation("Hide this shared folder", getHideSharedFolderCommand(virtualFolder));
       }
       if(Locations.isMyGroupSharedFoldersItem(tropixObjectTreeItem.getParent())) {
         this.addOperation("Hide this shared folder", getHideGroupSharedFolderCommand(virtualFolder));
       }
       
       if(tropixObjectContext.isModifiable()) {
         this.addOperation("Add items to this shared folder", getAddItemsToSharedFolderCommand(virtualFolder));
       }
     }
 
     @Override
     public void visitTissueSample(final TissueSample tissueSample) {
       this.addAssociations(tissueSample, "Peak Lists", "proteomicsRuns", true);
     }
 
     @Override
     public void visitIdentificationParameters(final IdentificationParameters parameters) {
       this.metadataSection.set("Identification Type", IdentificationType.forParameters(parameters).getDisplay());
       this.viewSection = new Section();
       this.viewSection.setTitle("View Parameters");
       this.viewSection.setResizeable(false);
       final VLayout layout = new VLayout();
 
       layout.setHeight("500px");
 
       IdentificationParametersService.Util.getInstance().getParameterMap(parameters.getId(), new AsyncCallbackImpl<Map<String, String>>() {
         @Override
         public void onSuccess(final Map<String, String> parametersMap) {
           if(parametersMap != null) {
             final ParametersPanel panel = PageComponentFactoryImpl.this.parametersPanelFactory.createParametersPanel(parameters.getType(),
                 new AsyncCallbackImpl<ParametersPanel>() {
                   @Override
                   public void onSuccess(final ParametersPanel panel) {
                     panel.setTemplateMap(parametersMap);
                   }
                 }, true);
             panel.setHeight("500px");
             panel.setWidth("100%");
             final Label label = new Label("These parameters have already been saved and cannot be modified.");
             label.setWrap(false);
             label.setHeight(20);
             layout.addMember(label);
             layout.addMember(panel);
           } else {
             final Label label = new Label("TINT has no information about the parameters this analysis was ran with.");
             layout.addMember(label);
           }
         }
       });
       this.viewSection.setItem(layout);
       this.viewSection.setExpanded(false);
     }
 
     private void initAssociationGrid() {
       if(this.associationGrid == null) {
         final Button goButton = Buttons.getGoButton();
         goButton.setDisabled(true);
 
         this.associationGrid = new ClientListGrid();
         this.associationGrid.setAlternateRecordStyles(false);
         this.associationGrid.setMinHeight(150);
         this.associationGrid.setHeight(150);
         this.associationGrid.setGroupByField("associationName");
         this.associationGrid.setSelectionType(SelectionStyle.SINGLE);
 
         final ListGridField nameField = new ListGridField("name", "Name");
         nameField.setType(ListGridFieldType.TEXT);
 
         final ListGridField typeField = new ListGridField("icon", " ");
         typeField.setType(ListGridFieldType.IMAGE);
         typeField.setImageURLPrefix(GWT.getHostPageBaseURL());
         typeField.setImageURLSuffix("");
         typeField.setWidth("20px");
 
         final ListGridField descriptionField = new ListGridField("description", "Description");
         descriptionField.setType(ListGridFieldType.TEXT);
 
         final ListGridField groupField = new ListGridField("associationName", "Association Type");
         groupField.setType(ListGridFieldType.TEXT);
         groupField.setHidden(true);
 
         this.associationGrid.setFields(typeField, nameField, descriptionField, groupField);
 
         this.associationGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
           public void onSelectionChanged(final SelectionEvent event) {
             final ListGridRecord selectedRecord = PageVisitor.this.associationGrid.getSelectedRecord();
             goButton.setDisabled(selectedRecord == null || selectedRecord.getAttributeAsObject("object") == null);
           }
         });
 
         final Command goCommand = new Command() {
           public void execute() {
             navigationSelectionMediator.go((TropixObject) PageVisitor.this.associationGrid.getSelectedRecord().getAttributeAsObject("object"));
           }
         };
         this.associationGrid.addDoubleClickHandler(new CommandDoubleClickHandlerImpl(goCommand));
         goButton.addClickHandler(new CommandClickHandlerImpl(goCommand));
 
         final CanvasWithOpsLayout<ClientListGrid> associationLayout = new CanvasWithOpsLayout<ClientListGrid>(this.associationGrid, goButton);
         associationLayout.setHeight(190);
         this.associationSection = new Section();
         this.associationSection.setTitle("Associated Objects");
         this.associationSection.setResizeable(true);
 
         this.associationSection.setItem(associationLayout);
         this.associationSection.setExpanded(false);
       }
     }
 
     private void addOperation(final String operation, final Command command) {
       final ListGridRecord record = new ListGridRecord();
       record.setAttribute("name", operation);
       record.setAttribute("operation", command);
       this.operationsGrid.addData(record);
     }
 
     private void addToAssociationGrid(final TropixObject object, final String name) {
       if(object == null) {
         return;
       }
       final ListGridRecord record = new ListGridRecord();
       record.setAttribute("name", StringUtils.sanitize(object.getName()));
       record.setAttribute("description", StringUtils.sanitize(object.getDescription()));
       record.setAttribute("icon", iconFunction.apply(object));
       record.setAttribute("object", object);
       record.setAttribute("associationName", name);
       this.associationGrid.addData(record);
     }
 
     private void addAssociations(final TropixObject object, final String name, final String associationName, final boolean multiple) {
       this.initAssociationGrid();
       if(multiple) {
         ObjectService.Util.getInstance().getAssociations(object.getId(), associationName, new AsyncCallbackImpl<List<TropixObject>>() {
           @Override
           public void onSuccess(final List<TropixObject> objects) {
             for(final TropixObject object : objects) {
               PageVisitor.this.addToAssociationGrid(object, name);
             }
           }
         });
       } else {
         ObjectService.Util.getInstance().getAssociation(object.getId(), associationName, new AsyncCallbackImpl<TropixObject>() {
           @Override
           public void onSuccess(final TropixObject object) {
             PageVisitor.this.addToAssociationGrid(object, name);
           }
         });
       }
     }
 
     protected void addQuickDownload(final TropixObject object, final String name, final String filename, final String associationName) {
       this.addQuickDownload(object, name, filename, associationName, false);
     }
 
     private void initOperationsSection() {
       this.operationsSection = new Section();
       this.operationsSection.setTitle("Operation(s)");
 
       final Label operationsLabel = new Label(PageConstants.INSTANCE.operationsLabel());
       operationsLabel.setWidth100();
       operationsLabel.setHeight(20);
 
       this.operationsSection.setResizeable(false);
 
       this.operationsGrid = new ClientListGrid();
       this.operationsGrid.setAlternateRecordStyles(false);
       final ListGridField nameField = new ListGridField("name", "Operation Description");
       this.operationsGrid.setFields(nameField);
       this.operationsGrid.setMinHeight(100);
       this.operationsGrid.setHeight(100);
       this.operationsGrid.setWidth("100%");
       this.operationsGrid.setSelectionType(SelectionStyle.SINGLE);
       this.operationsGrid.addDoubleClickHandler(new DoubleClickHandler() {
         public void onDoubleClick(final DoubleClickEvent event) {
           final ListGridRecord record = PageVisitor.this.operationsGrid.getSelectedRecord();
           if(record != null) {
             final Command operation = (Command) record.getAttributeAsObject("operation");
             operation.execute();
           }
         }
       });
       final VLayout operationsLayout = new VLayout();
       operationsLayout.setHeight(120);
       operationsLayout.setWidth100();
       operationsLayout.setLayoutMargin(10);
       operationsLayout.setMembersMargin(10);
       operationsLayout.setMembers(operationsLabel, this.operationsGrid);
       this.operationsSection.setItem(operationsLayout);
 
       final Collection<TreeItem> itemAsList = Arrays.<TreeItem>asList(tropixObjectTreeItem);
       if(tropixObjectContext.isModifiable()) {
         if(deleteCommandComponentFactory.acceptsLocations(itemAsList)) {
           this.addOperation("Delete", deleteCommandComponentFactory.get(itemAsList));
         }
 
         if(renameCommandComponentFactory.acceptsLocations(itemAsList)) {
           this.addOperation("Rename", renameCommandComponentFactory.get(itemAsList));
         }
 
         if(changeDescriptionCommandComponentFactory.acceptsLocations(itemAsList)) {
           this.addOperation("Change Description", new Command() {
             public void execute() {
               changeDescriptionCommandComponentFactory.get(itemAsList).execute();
             }
           });
         }
       }
     }
 
     private void initDownloads() {
       if(this.downloadGrid == null) {
         final GWTDownloadFormPanel smartDownloadFormPanel = new GWTDownloadFormPanel();
         final Button downloadButton = buildDownloadButton();
         final Command downloadCommand = buildDownloadCommand(smartDownloadFormPanel);
         initDownloadGrid();
         SmartUtils.enabledWhenHasSelection(downloadButton, downloadGrid);
         downloadGrid.addDoubleClickHandler(new CommandDoubleClickHandlerImpl(downloadCommand));
         downloadButton.addClickHandler(new CommandClickHandlerImpl(downloadCommand));
         initDownloadSection(smartDownloadFormPanel, downloadButton);
       }
     }
 
     private void initDownloadSection(final GWTDownloadFormPanel smartDownloadFormPanel, final Button downloadButton) {
       this.quickDownloadSection = new Section();
       this.quickDownloadSection.setTitle("Quick Download(s)");
       this.quickDownloadSection.setResizeable(true);
       final HLayout buttonLayout = new HLayout();
       buttonLayout.setHeight(20);
       buttonLayout.setWidth100();
       buttonLayout.addMember(downloadButton);
       buttonLayout.addChild(smartDownloadFormPanel);
       final CanvasWithOpsLayout<ListGrid> layout = new CanvasWithOpsLayout<ListGrid>(this.downloadGrid, buttonLayout);
       layout.setHeight(140);
       layout.setMaxHeight(140);
       this.quickDownloadSection.setItem(layout);
       this.quickDownloadSection.setExpanded(true);
     }
 
     private void initDownloadGrid() {
       final DataSourceField nameField = SmartUtils.getFieldBuilder("File", "File").ofType(FieldType.TEXT).get();
       final DataSourceField idField = SmartUtils.getHiddenIdField();
       final DataSource dataSource = SmartUtils.newDataSourceWithFields(nameField, idField);
       this.downloadGrid = new ClientListGrid(dataSource);
       this.downloadGrid.setAlternateRecordStyles(false);
       this.downloadGrid.setMinHeight(100);
       this.downloadGrid.setHeight(100);
       this.downloadGrid.setWidth100();
       this.downloadGrid.setEmptyMessage("No download(s) available for this item.");
       this.downloadGrid.setSelectionType(SelectionStyle.SINGLE);
     }
 
     private Command buildDownloadCommand(final GWTDownloadFormPanel smartDownloadFormPanel) {
       final Command downloadCommand = new Command() {
         public void execute() {
           final ListGridRecord record = downloadGrid.getSelectedRecord();
           final String filename = record.getAttribute("filename");
           String type = record.getAttribute("type");
           if(type == null) {
             type = "simple";
           }
 
           final Map<String, Object> extraParameters = record.getAttributeAsMap("parameters");
           if(extraParameters != null) {
             for(final Map.Entry<String, Object> extraParameter : extraParameters.entrySet()) {
               smartDownloadFormPanel.setParameter(extraParameter.getKey(), extraParameter.getValue());
             }
           }
           smartDownloadFormPanel.setType(type);
           smartDownloadFormPanel.setFilename(filename);
           final String id = record.getAttribute("id");
           smartDownloadFormPanel.setId(id);
           smartDownloadFormPanel.submit();
         }
       };
       return downloadCommand;
     }
 
     private Button buildDownloadButton() {
       final Button downloadButton = SmartUtils.getButton("Download", Resources.DOWNLOAD);
       downloadButton.disable();
       downloadButton.setTop(10);
       downloadButton.setHeight(20);
       downloadButton.setLeft(10);
       return downloadButton;
     }
 
     protected void addQuickDownload(final TropixObject object, final String name, final String basename, final String associationName,
         final boolean multiple) {
       this.initDownloads();
       if(!multiple) {
         ObjectService.Util.getInstance().getAssociation(object.getId(), associationName, new AsyncCallbackImpl<TropixObject>() {
           @Override
           public void onSuccess(final TropixObject to) {
             if(to instanceof TropixFile) {
               final String filename = basename + ModelUtils.getExtension((TropixFile) to);
               final ListGridRecord record = new ListGridRecord();
               record.setAttribute("id", to.getId());
               record.setAttribute("filename", StringUtils.sanitize(filename));
               record.setAttribute("File", StringUtils.sanitize(name));
               downloadGrid.addData(record);
             }
           }
         });
       } else {
         ObjectService.Util.getInstance().getAssociations(object.getId(), associationName, new AsyncCallbackImpl<List<TropixObject>>() {
           @Override
           public void onSuccess(final List<TropixObject> tos) {
             for(final TropixObject to : tos) {
               final String filename = basename + ModelUtils.getExtension((TropixFile) to);
               final ListGridRecord record = new ListGridRecord();
               record.setAttribute("id", to.getId());
               record.setAttribute("filename", StringUtils.sanitize(filename));
               record.setAttribute("File", StringUtils.sanitize(name));
               downloadGrid.addData(record);
             }
           }
         });
       }
     }
 
     private void initSharingSection(final TropixObject tropixObject) {
       final CanvasComponent<? extends Canvas> sharingComponent = sharingComponentFactory.get(pageConfiguration);
       if(sharingComponent != null) {
         this.sharingSection = new Section();
         this.sharingSection.setTitle("Sharing");
         final Canvas sharingCanvas = sharingComponent.get();
         sharingCanvas.setBorder(PageComponentFactoryImpl.SECTION_ITEM_BORDER);
         this.sharingSection.setItem(sharingCanvas);
       }
     }
 
     public Layout get() {
       return pageFrameSupplier.get();
     }
   }
 
   @Inject
   public void setNavigationSelectionMediator(final NavigationSelectionMediator navigationSelectionMediator) {
     this.navigationSelectionMediator = navigationSelectionMediator;
   }
 
   @Inject
   public void setObjectSelectionWindowComponentSupplier(
       @Named("concreteObjects") final Supplier<? extends MultiSelectionWindowComponent<TreeItem, ? extends Window>> objectSelectionWindowComponentSupplier) {
     this.objectSelectionWindowComponentSupplier = objectSelectionWindowComponentSupplier;
   }
 
   @Inject
   public void setParametersPanelFactory(final ParametersPanelFactory parametersPanelFactory) {
     this.parametersPanelFactory = parametersPanelFactory;
   }
 
   @Inject
   public void setNoteComponentFactory(final ComponentFactory<Note, ? extends CanvasComponent<? extends Canvas>> noteComponentFactory) {
     this.noteComponentFactory = noteComponentFactory;
   }
 
   @Inject
   public void setDeleteCommandComponentFactory(@Named("delete") final LocationCommandComponentFactory<? extends Command> deleteCommandComponentFactory) {
     this.deleteCommandComponentFactory = deleteCommandComponentFactory;
   }
 
   @Inject
   public void setRenameCommandComponentFactory(@Named("rename") final LocationCommandComponentFactory<? extends Command> renameCommandComponentFactory) {
     this.renameCommandComponentFactory = renameCommandComponentFactory;
   }
 
   @Inject
   public void setChangeDescriptionCommandComponentFactory(
       @Named("changeDescription") final LocationCommandComponentFactory<? extends Command> changeDescriptionCommandComponentFactory) {
     this.changeDescriptionCommandComponentFactory = changeDescriptionCommandComponentFactory;
   }
 
 }
