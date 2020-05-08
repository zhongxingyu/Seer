 package emr_vis_nlp.controller;
 
 import emr_vis_nlp.ml.MLPredictor;
 import emr_vis_nlp.ml.deprecated.DeprecatedMLPredictor;
 import emr_vis_nlp.view.DocDetailsTableModel;
 import emr_vis_nlp.view.doc_map.TreeMapSelectorTableModel;
 import emr_vis_nlp.view.doc_table.DocTableModel;
 import emr_vis_nlp.view.doc_table.AttrTableModel;
 import emr_vis_nlp.view.doc_grid.DocGridTableSelectorModel;
 import emr_vis_nlp.model.*;
 import emr_vis_nlp.view.*;
 import emr_vis_nlp.view.doc_grid.DocumentGrid;
 import emr_vis_nlp.view.doc_grid.DocumentGrid.DocGridDragControl;
 import emr_vis_nlp.view.doc_grid.DocumentGridTable;
 import emr_vis_nlp.view.doc_map.DocumentTreeMapView;
 import java.io.File;
 import java.util.*;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 
 /**
  *
  * Fulfills the Controller role of the MVC design pattern. Responsible for all
  * communication and coordination among the backing MainModel and the front-end
  * MainView. Is tasked with querying the back-end model and building many of the
  * intermediate data structures (ie, TableModels) required by front-end
  * components, and monitoring and updating these intermediate structures as
  * necessary.
  *
  * @author alexander.p.conrad@gmail.com
  */
 public class MainController {
     
     /**
      * single controller instance (singleton design pattern)
      */
     private static MainController mainController = null;
     /**
      * main model controlled by this
      */
     private MainModel model;
     /**
      * main view controlled by this
      */
     private MainView view;
     /**
      * currently-loaded machine learning predictor
      */
     private MLPredictor predictor;
     /**
      * list of active popup windows
      */
     private List<JFrame> activePopups;
     /**
      * list of active popup window doc IDs, for convenience
      */
     private List<Integer> activePopupIDs;
     /**
      * current treemap view component (if any)
      */
     private JComponent docTreeMapViewComponent = null;
     /**
      * current treemap view attr selection model (if any)
      */
     private TreeMapSelectorTableModel docTreeMapSelectionModel = null;
     /**
      * current doc grid view attr selection model (if any)
      */
     private DocGridTableSelectorModel docGridSelectionModel = null;
     /**
      * current document grid component (if any)
      */
     private DocumentGrid documentGrid = null;
     /**
      * backing table for current document grid (if any)
      */
     DocumentGridTable documentGridTable = null;
     /*
      * list of predicates for document disabling
      */
     private Map<AttrValPredicate, Boolean> disabledAttrValsMap;
     /*
      * current string for search
      */
     private String searchText;
     
     /**
      * Factory method for generating a singleton MainController.
      * 
      * @return the governing controller object for the visualization, or a new MainController if none exist yet
      */
     public static MainController getMainController() {
         if (mainController == null) {
             mainController = new MainController();
         }
         return mainController;
     }
     
     /**
      * private constructor for use with the static factory method.
      */
     private MainController() {
         activePopups = new ArrayList<>();
         activePopupIDs = new ArrayList<>();
         disabledAttrValsMap = new HashMap<>();
         searchText = "";
     }
 
     /**
      * Associates the controller with a back-end MainModel data source.
      * 
      * @param model the model with which this controller should be associated.
      */
     public void setModel(MainModel model) {
         this.model = model;
         
         // update view once model loading is complete
         if (view != null) view.resetAllViews();
     }
     
     /**
      * Associates the controller with a back-end NLP prediction module.
      * 
      * @param predictor nlp / ml module which should be used for predictions
      */
     public void setPredictor(MLPredictor predictor) {
         this.predictor = predictor;
         
         if (view != null) view.resetAllViews();
     }
 
     /**
      * Associates the controller with a front-end MainView to which view events should be pushed.
      * 
      * @param view the MainView with which this controller is associated.
      */
     public void setView(MainView view) {
         this.view = view;
     }
     
     /**
      * Associates the controller with a new back-end MainModel loaded from a doclist file. Refreshes the visualization after loading is complete.
      * 
      * @param file doclist file pointing the resources required by MainModel.
      */
     public void setModelFromDoclist(File file) {
 
         // load new model from doclist
         // TODO add support for dataset types beyond MpqaColon
         
         // if type == mpqa_colon {
         model = new MpqaColonMainModel(this);
         ((MpqaColonMainModel) model).loadDataFromDoclist(file);
         // load predictor for dataset as well
         predictor = new DeprecatedMLPredictor(model);
         // }
         
         // update view once model loading is complete
         if (view != null) view.resetAllViews();
 
     }
 
 //    /**
 //     * Filters documents according to presence of string.
 //     *
 //     * @param str
 //     */
 //    public void applySimpleStringFilter(String str) {
 //        throw new UnsupportedOperationException();  // TODO
 ////        model.applySimpleStringFilter(str);  // no longer part of model?
 //        // update components as appropriate
 ////        view.resetAllViews();
 //
 //    }
 
     /**
      * Builds a new backing DocTableModel for a document-based table in the visualization. Bases this table on the currently-loaded MainModel.
      *
      * @return backing DocTableModel, or null if no model is currently loaded.
      */
     public DocTableModel buildSimpleDocTableModel() {
         if (model != null) {
             DocTableModel docTableModel = new DocTableModel(model.getAllDocuments(), model.getAllSelectedDocuments(), model.getAllAttributes(), model.getAllSelectedAttributes());
             return docTableModel;
         }
         return null;
     }
 
     /**
      * Builds a new backing AttrTableModel for an attribute selection table in the visualization. Bases this table on the currently-loaded MainModel.
      *
      * @return backing AttrTableModel, or null if not model is currently loaded.
      */
     public AttrTableModel buildSimpleAttrSelectionTableModel() {
         if (model != null) {
             AttrTableModel attrTableModel = new AttrTableModel(model.getAllAttributes(), model.getAllSelectedAttributes(), this);
             return attrTableModel;
         }
         return null;
     }
 
 //    public TableModel buildSimpleAttrSelectionTableModelFocusOnly() {
 //        throw new UnsupportedOperationException("Not supported yet.");
 ////        TableModel attrSelectionTableModel = model.buildSimpleAttrSelectionTableModelFocusOnly();
 ////        return attrSelectionTableModel;
 //    }
 
     /**
      * This method is called when the list of selected attributes (for a boolean-selection-based view) is changed. This method is responsible for pushing updates to all relevant views and view-backing intermediate models.
      * 
      * @param newSelectedAttributes boolean list indicating whether or not each attribute is selected.
      */
     public void attributeSelectionUpdated(List<Boolean> newSelectedAttributes) {
         
         // update selection in model
         // TODO eliminate this? backing model should not be changed in response to selection actions on the front-end?
         model.setSelectedAttributes(newSelectedAttributes);
 
         // indicate to main view that all relevant displays should be redrawn / rebuilt
         view.attributeSelectionChanged();
         
         // rebuild table models for all active document popups
         for (JFrame popup : activePopups) {
             ((DocFocusPopup) popup).rebuildDocDetailsTable();
         }
 
     }
     
     /**
      * Builds and returns a new DocumentTreeMapView component based on the current back-end model.
      * 
      * @return treemap-like interactive visualization for exploring the dataset
      */
     public JComponent buildDocTreeMapViewComponent() {
         if (model != null) {
             // get selected attributes from interface
             List<String> selectedAttrsForTree = new ArrayList<>();
             if (docTreeMapSelectionModel != null) {
                 TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel) docTreeMapSelectionModel;
                 selectedAttrsForTree = treeMapSelectorTableModel.getSelectedAttributeList();
             } else {
                 // if there's no docTreeMapSelectionModel currently loaded, then the interface has not been initialized properly
                 // TODO eliminate, replace with proper default initialization?
 //            selectedAttrsForTree.add("Indicator_1");
 //            selectedAttrsForTree.add("Indicator_21");
 //            selectedAttrsForTree.add("Indicator_19");
             }
             JComponent newDocTreeMapViewComponent = DocumentTreeMapView.buildNewTreeMapOnly(this, model.getAllDocuments(), model.getAllSelectedDocuments(), selectedAttrsForTree);
             docTreeMapViewComponent = newDocTreeMapViewComponent;
             return docTreeMapViewComponent;
         }
         return null;
     }
 
     /**
      * Builds backing TableModel for a treemap-oriented attribute selection table, based on the attributes in the currently-loaded Model.
      * 
      * @return backing model for categorical attribute selection, else null if no model is loaded.
      */
     public TreeMapSelectorTableModel buildSimpleTreeMapSelectionTableModel() {
         if (model != null) {
             TreeMapSelectorTableModel newDocTreeMapSelectionTableModel = new TreeMapSelectorTableModel(model.getAllAttributes(), this);
             docTreeMapSelectionModel = newDocTreeMapSelectionTableModel;
             return docTreeMapSelectionModel;
         } return null;
     }
 
     /**
      * Builds and returns a backing TableModel for presenting a detailed view of the attributes, predictions, certainties, and manually-annotated values associated with a particular document.
      * 
      * @param docGlobalId index of the target document
      * @return backing TableModel
      */
     public DocDetailsTableModel buildAttrAndPredictionModelForDoc(int docGlobalId) {
         
         if (model != null && predictor != null) {
             Document doc = model.getAllDocuments().get(docGlobalId);
             List<String> allAttributes = model.getAllAttributes();
             List<Boolean> allAttributesEnabled = model.getAllSelectedAttributes();
 
 //            Map<String, PredictionCertaintyTuple> attrPredictionMap = model.getPredictionsForDoc(docGlobalId);
             Map<String, PredictionCertaintyTuple> attrPredictionMap = predictor.getPredictionsForDoc(docGlobalId);
 
             DocDetailsTableModel docDetailsTableModel = new DocDetailsTableModel(doc, docGlobalId, attrPredictionMap, allAttributes, allAttributesEnabled);
             return docDetailsTableModel;
         }
         return null;
 
     }
 
     /**
      * Rebuilds the text content of a given AbstractDocument, populating it with
      * the text of a particular target document. Also highlights and blows-up
      * particular terms of importance to the back-end NLP model for a particular
      * attribute.
      *
      * @param abstDoc AbstractDocument to have its text replaced with the target document's highlighted text
      * @param globalDocId index of the target document
      * @param attrName name of the attribute for which terms should be highlighted and enlarged
      */
     public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, String attrName) {
         
         // map attrName to int
         if (model != null) {
             List<String> attrs = model.getAllAttributes();
             int attrIndex = -1;
             for (int a=0; a<attrs.size(); a++) {
                 if (attrs.get(a).equals(attrName)) {
                     attrIndex = a;
                     break;
                 }
             }
             
             if (attrIndex != -1) {
                 writeDocTextWithHighlights(abstDoc, globalDocId, attrIndex);
             } else {
                 System.err.println("MainController.writeDocTextWithHighlights: could not find index for attribute: "+attrName);
             }
             
         }
         
     }
     
     /**
      * Rebuilds the text content of a given AbstractDocument, populating it with
      * the text of a particular target document. Also highlights and blows-up
      * particular terms of importance to the back-end NLP model for a particular
      * attribute.
      *
      * @param abstDoc AbstractDocument to have its text replaced with the target document's highlighted text
      * @param globalDocId index of the target document
      * @param globalAttrId index of the target attribute
      */
     public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
 
         if (model != null && predictor != null) {
             // delegate this to the model, if possible
 //            if (globalAttrId != -1 && model.canWriteDocTextWithHighlights(globalDocId, globalAttrId)) {
 //                model.writeDocTextWithHighlights(abstDoc, globalDocId, globalAttrId);
             if (globalAttrId != -1 && predictor.canWriteDocTextWithHighlights(globalDocId, globalAttrId)) {
                 predictor.writeDocTextWithHighlights(abstDoc, globalDocId, globalAttrId);
             } else {
                 // if model doesn't support highlighting for this text, just insert plaintext
                 try {
                     abstDoc.remove(0, abstDoc.getLength());
                 } catch (BadLocationException e) {
                     e.printStackTrace();
                     System.out.println("err: could not reset doc text for doc " + globalDocId + " focus window");
                 }
 
                 int maxFontSize = 32;
                 int minFontSize = 12;
 
                 String docText = model.getAllDocuments().get(globalDocId).getText();
                 SimpleAttributeSet attrSet = new SimpleAttributeSet();
                 StyleConstants.setFontSize(attrSet, minFontSize);
                 try {
                     abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
                 } catch (BadLocationException e) {
                     e.printStackTrace();
                     System.out.println("err: could not update doc text for doc " + globalDocId + " focus window");
                 }
             }
         }
     }
     
     /**
      * Called when attributes for a target document have been updated; is responsible for pushing out updates to relevant views.
      * 
      * @param docGlobalID 
      */
     public void documentAttributesUpdated(int docGlobalID) {
         if (view != null) {
             // refresh all relevant tables
             view.attributeSelectionChanged();
             // rebuild table models for all active document popups
             for (JFrame popup : activePopups) {
                 ((DocFocusPopup) popup).rebuildDocDetailsTable();
             }
         }
     }
 
     /**
      * Constructs a new window for presenting a single document in detail, ie,
      * in terms of its text and attribute values. This method should generally
      * only be called by displayDocDetailsWindow, after that method checks for
      * the existence of the details window for the target document.
      *
      * @param docGlobalID index of target document
      * @return new top-level container for target doc, or null if there's no mainmodel
      */
     private JFrame buildDocDetailsWindow(int docGlobalID) {
         if (model != null) {
         Document doc = model.getAllDocuments().get(docGlobalID);
         JFrame popup = new DocFocusPopup(doc, docGlobalID);
         activePopups.add(popup);
         activePopupIDs.add(docGlobalID);
         return popup;
         } return null;
     }
 
     /**
      * Displays a window for presenting a single document in detail, ie, in
      * terms of its text and attribute values. If the target window already
      * exists, attention is brought to the target window. Otherwise, a new
      * window is created.
      * 
      * @param docGlobalID index of the target document to display
      */
     public void displayDocDetailsWindow(int docGlobalID) {
 
         // if window is already open for doc, bring to front
         int docGlobalIdIndex = -1;
         for (int p = 0; p < activePopupIDs.size(); p++) {
             if (activePopupIDs.get(p) == docGlobalID) {
                 docGlobalIdIndex = p;
                 break;
             }
         }
 
         if (docGlobalIdIndex != -1) {
             JFrame popup = activePopups.get(docGlobalIdIndex);
             popup.toFront();
         } else {
             // else, popup new window in a separate thread
             final JFrame newPopup = buildDocDetailsWindow(docGlobalID);
             java.awt.EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     newPopup.setVisible(true);
                 }
             });
         }
     }
 
     /**
      * Removes a target document's details popup from the controller's internal
      * tracking. This should be called whenever the details window is closed.
      *
      * @param popup window to be closed
      * @return true if popup was being tracked by controller, false otherwise
      */
     public boolean removeDocDetailsWindow(JFrame popup) {
 
         if (activePopups.contains(popup)) {
             int popupIndex = activePopups.indexOf(popup);
             activePopups.remove(popup);
             activePopupIDs.remove(popupIndex);
             return true;
         }
         // shouldn't happen? all popups should be in activePopups?
         return false;
 
     }
     
     /**
      * Called when the categorically-selected attributes for the TreeMap view
      * have been changed. Pushes relevant adjustments to the relevant views.
      *
      */
     public void updateTreeMapAttributes() {
 
         // get selected attrs from the table model
         TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel) docTreeMapSelectionModel;
         List<String> currentSelectedAttrs = treeMapSelectorTableModel.getSelectedAttributeList();
 
         // feed selected attrs to the treemap
         //DocumentTreeMapView.updatePanelWithNewTreemap(docTreeMapViewComponent, model.getAllDocuments(), model.getAllSelectedDocuments(), currentSelectedAttrs);
         view.orderedAttrSelectionChanged();
 
     }
     
     /**
      * Builds DocumentGrid view from the current backing model.
      * 
      * @return DocumentGrid to be loaded into the mainview
      */
     public DocumentGrid buildDocumentGrid() {
 
         // default values for axes; these should be eliminated and overridden
         String xAxisAttrName = "Indicator_9";
         String yAxisAttrName = "Indicator_21";
         String shapeAttrName = "";
         String colorAttrName = "";
         if (docGridSelectionModel != null) {
             // if docGridSelectionModel == null, something probably wasn't initialized correctly in the mainview?
             xAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getXAxisAttribute();
             yAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getYAxisAttribute();
             shapeAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getShapeAttribute();
             colorAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getColorAttribute();
         }
 
         // build table for grid
         documentGridTable = new DocumentGridTable(model.getAllAttributes(), model.getAllDocuments(), model.getAllSelectedDocuments());
         // build, return grid (while maintaining reference)
         documentGrid = new DocumentGrid(documentGridTable, xAxisAttrName, yAxisAttrName, shapeAttrName, colorAttrName);
         return documentGrid;
 
     }
 
     /**
      * similar to buildDocumentGrid(), but rather than rebuilding the grid from
      * scratch, simply updates the attributes by which the grid is laid out
      * based on the appropriate TableModel.
      *
      */
     public void updateDocumentGrid() {
 
         String xAxisAttrName = "";
         String yAxisAttrName = "";
         String shapeAttrName = "";
         String colorAttrName = "";
         if (docGridSelectionModel != null) {
             xAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getXAxisAttribute();
             yAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getYAxisAttribute();
             shapeAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getShapeAttribute();
             colorAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getColorAttribute();
         }
 
         // build table for grid
         if (documentGrid != null) {
             documentGrid.updateXAxis(xAxisAttrName);
             documentGrid.updateYAxis(yAxisAttrName);
             documentGrid.updateShapeAttr(shapeAttrName);
             documentGrid.updateColorAttr(colorAttrName);
         }
     }
 
     /**
      * Builds and returns a new backing table model for the table used to select attributes for the document grid view.
      * 
      * @return backing DocGridTableSelectorModel
      */
     public DocGridTableSelectorModel buildSimpleDocGridSelectionTableModel() {
         DocGridTableSelectorModel newDocGridTableModel = new DocGridTableSelectorModel(model.getAllAttributes());
 //        TableModel newDocGridSelectionTableModel = model.buildSimpleDocGridSelectionTableModel();
         docGridSelectionModel = newDocGridTableModel;
         return docGridSelectionModel;
     }
 
     /**
      * Builds a VarBarChartForCell display for a given attribute. Returned
      * display is meant to be used as cell in table.
      *
      * @param attrName name of target attribute for which display shoudl be created
      * @return varbarchart visualization for target attribute
      */
     public VarBarChartForCell getVarBarChartForCell(String attrName) {
         VarBarChartForCell varBarChart = new VarBarChartForCell(this, attrName, model.getAllDocuments());
         return varBarChart;
     }
     
     /**
      * Updates the value of a particular attribute for a particular document. 
      * NOTE: because this method may be called from multiple threads running in parallel (ie, from separate doc details windows), we should make sure it's synchronized
      * 
      * @param docID
      * @param docAttr
      * @param docAttrVal 
      */
     public synchronized void updateDocumentAttr(int docID, String docAttr, String docAttrVal) {
         if (model != null) {
             // update value in model
             model.updateDocumentAttr(docID, docAttr, docAttrVal);
             // update value in applicable visual tables
             documentGridTable.setString(docID, docAttr, docAttrVal);
         }
     }
     
     /**
      * Resets the documentgrid view, causing it to perform necessary layout, preforce, and other operations.
      */
     public void resetDocGridView() {
         if (documentGrid != null) {
             documentGrid.resetView();
         }
     }
     
     /**
      * Returns whether a back-end model contains a prediction for a given attribute for a given document.
      * 
      * @param globalDocId
      * @param attrName
      * @return 
      */
     public boolean hasPrediction(int globalDocId, String attrName) {
 //        if (model != null) return model.hasPrediction(globalDocId, attrName);
         if (predictor != null) return predictor.hasPrediction(globalDocId, attrName);
         return false;
     }
     
     /**
      * Gets from the back-end model its prediction for a given attribute for a given document.
      * 
      * @param globalDocId
      * @param attrName
      * @return 
      */
     public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
 //        if (model != null) return model.getPrediction(globalDocId, attrName);
         if (predictor != null) return predictor.getPrediction(globalDocId, attrName);
         return null;
     }
 
     /**
      * Highlights documents matching attribute value criteria in applicable
      * views.
      *
      * @param attrName
      * @param attrValue
      */
     public void highlightDocsWithAttrVal(String attrName, String attrValue) {
         // TODO apply to more general case
         // for now, assume only a single highlight set at a time
         documentGrid.setHighlightPredicate(attrName, attrValue);
     }
     
     /**
      * Unhighlights all documents in all applicable views.
      * 
      */
     public void unhighlightAllDocs() {
         documentGrid.resetHighlightPredicate();
     }
     
     public void disableDocsWithAttrVal(String attrName, String attrValue) {
         AttrValPredicate pred = new AttrValPredicate(attrName, attrValue);
         disabledAttrValsMap.put(pred, true);
         attrValPairsUpdated();
     }
     
     public void enableDocsWithAttrVal(String attrName, String attrValue) {
         AttrValPredicate pred = new AttrValPredicate(attrName, attrValue);
         if (disabledAttrValsMap.containsKey(pred)) {
             disabledAttrValsMap.remove(pred);
         }
         attrValPairsUpdated();
     }
     
     public void enableAllDocs() {
         disabledAttrValsMap.clear();
         attrValPairsUpdated();
         if (docGridSelectionModel != null) {
             docGridSelectionModel.resetVarBarCharts();
         }
     }
     
     private void attrValPairsUpdated() {
         // update appropriate components
         // build predicate string for Prefuse components
         StringBuilder predDisabledAttrValsBuilder = new StringBuilder();
         Set<AttrValPredicate> keySet = disabledAttrValsMap.keySet();
         Iterator<AttrValPredicate> keySetIterator = keySet.iterator();
         while (keySetIterator.hasNext()) {
             AttrValPredicate pred = keySetIterator.next();
 //            predDisabledAttrValsBuilder.append(pred.getTrueStrPred());
             predDisabledAttrValsBuilder.append(pred.getFalseStrPred());
             if (keySetIterator.hasNext()) {
 //                predDisabledAttrValsBuilder.append(" OR ");
                 predDisabledAttrValsBuilder.append(" AND ");
             }
         }
         // push string to appropriate Prefuse components
         String predDisabledAttrVals = predDisabledAttrValsBuilder.toString();
         if (documentGrid != null) {
             documentGrid.resetDocsVisiblePredicate(predDisabledAttrVals);
         }
         
     }
     
     /**
      * Retrieves the glasspane from the mainview for use by other requesting views.
      * 
      * @return 
      */
     public MainViewGlassPane getGlassPane() {
         if (view != null) {
             return view.getGlassPane();
         }
         return null;
     }
     
     public DocumentGrid getDocumentGrid() {
         if (documentGrid != null)
             return documentGrid;
         return null;
     }
     
     public void setSearchText(String text) {
         searchText = text;
         // update relevant views
         view.setSearchText(searchText);
     }
     
     public String getSearchText() {
         return searchText;
     }
     
     public DocGridDragControl getDocDragControl() {
         if (documentGrid != null)
             return documentGrid.getDragControl();
         return null;
     }
     
     public void setFisheyeEnabled(boolean enableFisheye) {
         if (documentGrid != null)
             documentGrid.setFisheyeEnabled(enableFisheye);
     }
     
     
     
 }
