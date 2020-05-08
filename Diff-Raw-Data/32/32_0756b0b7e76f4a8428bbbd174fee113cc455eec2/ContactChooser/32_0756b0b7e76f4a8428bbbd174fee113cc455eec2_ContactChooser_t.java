 package ciknow.zk.survey.response;
 
 import ciknow.domain.Group;
 import ciknow.domain.*;
 import ciknow.zk.survey.design.AddContactWindow;
 
 import java.util.*;
 import org.apache.commons.collections.comparators.ComparatorChain;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.transaction.annotation.Transactional;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.InputEvent;
 import org.zkoss.zk.ui.event.SelectEvent;
 import org.zkoss.zk.ui.select.annotation.Listen;
 import org.zkoss.zk.ui.select.annotation.Wire;
 import org.zkoss.zul.*;
 
 /**
  *
  * @author gyao
  */
 public class ContactChooser extends SurveyQuestionBase {
 
     private static final long serialVersionUID = 2563309178690889901L;
     private static Log logger = LogFactory.getLog(ContactChooser.class);
     // components
     @Wire
     private Listbox availableContactsBox;
     @Wire
     private Auxhead auxhead;
     @Wire
     private Listhead listhead;
     @Wire
     private Listfooter footer;
     @Wire
     private Listbox selectedContactsBox;
     @Wire
     private Listheader selectedContactsHeader;
     @Wire
     private Vlayout filterArea;
     @Wire
     private Listbox filterBox;
     @Wire
     private Grid filterGrid;
    @Wire
    private Button addNodeBtn;
    
     // state
     private boolean showImage;
     private Map<String, Question> questionMap;
     private Map<String, Textbox> filterMap;
     private LinkedHashMap<String, String> columns;
     private List<ContactChooserItem> availableContactItems;
     private ListModelList<ContactChooserItem> availableContactsModel;
     private List<ContactChooserItem> selectedContactItems;
     private ListModelList<ContactChooserItem> selectedContactsModel;
     private Group privateGroup;
 
     public ContactChooser(Question currentQuestion) {
         super(currentQuestion);
     }
 
     public ContactChooser(Node respondent, Question currentQuestion) {
         super(respondent, currentQuestion);
     }
 
     @Override
     protected void createUI() {
         Executions.createComponents("/WEB-INF/zk/survey/response/ContactChooser.zul", this, null);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void afterCreationComplete() {
         super.afterCreationComplete();
 
         filterMap = new HashMap<String, Textbox>();
         FilterEventListener listener = new FilterEventListener();
 
        // allow adding new node?
        if (!respondent.isAdmin() && !currentQuestion.allowUserCreatedNode()){
        	addNodeBtn.setVisible(false);
        }
        
         // Determine availableContactsBox columns
         showImage = currentQuestion.showCCImage();
         columns = currentQuestion.getCCColumns();
 
         Auxheader auxheader;
         Listheader listheader;
 
         // image column
         if (showImage) {
             auxheader = new Auxheader();
             auxheader.setLabel("Search: ");
             auxheader.setAlign("right");
             auxheader.setParent(auxhead);
 
             listheader = new Listheader("Photo");
             listheader.setParent(listhead);
 
             footer.setSpan(columns.size() + 1);
         } else {
             footer.setSpan(columns.size());
         }
 
         // label column
         auxheader = new Auxheader();
         auxheader.setParent(auxhead);
         Textbox searchBox = new Textbox();
         searchBox.setParent(auxheader);
         searchBox.setWidth("95%");
         searchBox.addEventListener("onChanging", listener);
         filterMap.put("label", searchBox);
 
         String columnLabel = columns.get("label");
         if (columnLabel == null) {
             columnLabel = "Label";
         }
         listheader = new Listheader(columnLabel);
         listheader.setId("label");
         listheader.setSortAscending(new ContactChooserItemComparator("label", true, false, true));
         listheader.setSortDescending(new ContactChooserItemComparator("label", false, false, true));
         listheader.setParent(listhead);
 
         for (String column : columns.keySet()) {
             if (column.equals("label")) {
                 continue;
             }
             columnLabel = columns.get(column);
 
             auxheader = new Auxheader();
             auxheader.setParent(auxhead);
             searchBox = new Textbox();
             searchBox.setParent(auxheader);
             searchBox.setWidth("95%");
             searchBox.addEventListener("onChanging", listener);
             filterMap.put(column, searchBox);
 
             listheader = new Listheader(columnLabel);
             listheader.setId(column);
             listheader.setSortAscending(new ContactChooserItemComparator(column, true, false, true));
             listheader.setSortDescending(new ContactChooserItemComparator(column, false, false, true));
             listheader.setParent(listhead);
         }
 
         // Determine selectedContactsBox columns
         listhead = new Listhead();
         listhead.setParent(selectedContactsBox);
         selectedContactsHeader = new Listheader("Selected Contacts");
         selectedContactsHeader.setSortDirection("ascending");
         selectedContactsHeader.setSortAscending(new ContactChooserItemComparator("label", true, false, true));
         selectedContactsHeader.setSortDescending(new ContactChooserItemComparator("label", false, false, true));
         selectedContactsHeader.setParent(listhead);
 
 
         // Get available contacts
         Set<Long> nodeIds;
         try {
             nodeIds = Question.getCombinedAvailableNodeIds(currentQuestion, respondent, false);
         } catch (Exception e) {
             logger.error(e.getMessage());
             e.printStackTrace();
             Messagebox.show(e.getMessage());
             return;
         }
         List<Node> availableContacts = nodeDao.loadByIds(nodeIds);
         availableContactItems = new ArrayList<ContactChooserItem>();
         questionMap = new HashMap<String, Question>();
         Survey survey = currentQuestion.getPage().getSurvey();
         for (Page page : survey.getPages()) {
             for (Question question : page.getQuestions()) {
                 questionMap.put(question.getShortName(), question);
             }
         }
         for (Node node : availableContacts) {
             ContactChooserItem item = new ContactChooserItem(node, showImage, columns, questionMap);
             availableContactItems.add(item);
         }
 
         // Get selected contacts
         String privateGroupName = Group.getPrivateGroupName(respondent.getUsername(), currentQuestion.getShortName());
         privateGroup = groupDao.findByName(privateGroupName);
         if (privateGroup == null) {
             privateGroup = new Group();
             privateGroup.setName(privateGroupName);
             groupDao.save(privateGroup);
         }
         selectedContactItems = new ArrayList<ContactChooserItem>();
         for (ContactChooserItem item : availableContactItems) {
             if (item.getContact().getGroups().contains(privateGroup)) {
                 selectedContactItems.add(item);                
             }
         }
 
         // Initial sort
         List<Comparator<ContactChooserItem>> comps = currentQuestion.getCCSortComparators();
         if (comps == null || comps.isEmpty()) {
             comps.add(new ContactChooserItemComparator("label", true, false, true));
         }
         for (Comparator<ContactChooserItem> comp : comps){
         	ContactChooserItemComparator c = (ContactChooserItemComparator) comp;
         	Listheader lh = (Listheader)listhead.query("#" + c.getColumn());
         	if (lh != null) lh.setSortDirection(c.isAscending()?"ascending":"descending");
         }
         ComparatorChain chain = new ComparatorChain(comps);
         Collections.sort(availableContactItems, chain);
         Collections.sort(selectedContactItems, new ContactChooserItemComparator("label", true, false, true));
         
         // Attach assign model and renderer to listboxes
         availableContactsModel = new ListModelList<ContactChooserItem>(availableContactItems);
         availableContactsModel.setMultiple(true);
         for (ContactChooserItem ccItem : selectedContactItems){
         	ccItem.setSelected(true);
         	availableContactsModel.addToSelection(ccItem);        	
         }
         availableContactsBox.setModel(availableContactsModel);
         availableContactsBox.setItemRenderer(new ContactChooserItemRowRenderer());
 
         selectedContactsModel = new ListModelList<ContactChooserItem>(selectedContactItems);
         //selectedContactsModel.setMultiple(true);
         selectedContactsBox.setModel(selectedContactsModel);
         selectedContactsBox.setItemRenderer(new ContactChooserItemLabelRenderer());
 
         // filterBox
         if (!columns.isEmpty()){
 	        for (String name : columns.keySet()){
 	        	String label = columns.get(name);
 	        	Listitem li = new Listitem(label);
 	        	li.setParent(filterBox);
 	        	li.setValue(name);
 	        }
         } else filterArea.setParent(null);
         
         updateSize();
     }
 
     private void updateSize() {
         footer.setLabel("There are " + availableContactsModel.size() + " available contacts.");
         selectedContactsHeader.setLabel(currentQuestion.getSelectedNodesHeader() + " (" + selectedContactsModel.getSize() + ")");
     }
 
     public void removeInstruction() {
         instruction.setParent(null);
     }
 
     @Listen("onSelect = #filterBox")
     public void onSelect$filterBox() {
         Listitem item = filterBox.getSelectedItem();
         String column = (String) item.getValue();
         logger.debug("filter changed: " + column);
 
         // clear 
         Rows rows = filterGrid.getRows();
         while (!rows.getChildren().isEmpty()) {
             rows.removeChild(rows.getLastChild());
         }
 
         // get distinct values for selected column
         Set<String> distinctValues = new TreeSet<String>();
         for (ContactChooserItem contact : availableContactItems) {
             String distinctValue = contact.getValue(column);
             if (distinctValue != null) {
                 distinctValues.add(distinctValue);
             }
         }
 
         // populate grid
         CheckboxFilterListener listener = new CheckboxFilterListener();
         for (String value : distinctValues) {
             Row row = new Row();
             row.setParent(rows);
 
             Checkbox checkbox = new Checkbox(value);
             checkbox.setParent(row);
             checkbox.addEventListener("onCheck", listener);
         }
     }
 
     private void filter() {
         Listitem selectedtem = filterBox.getSelectedItem();
         String column = (String) selectedtem.getValue();
         logger.debug("filtering on column: " + column);
 
         Set<String> filterValues = new HashSet<String>();
         for (Object o : filterGrid.getRows().getChildren()) {
             Row row = (Row) o;
             Checkbox checkbox = (Checkbox) row.getChildren().get(0);
             if (!checkbox.isChecked()) {
                 continue;
             }
 
             String filterValue = checkbox.getLabel().toLowerCase();
             filterValues.add(filterValue);
             logger.debug("filtering criteria: " + filterValue);
         }
 
         availableContactsModel.clear();
         List<ContactChooserItem> filteredItems = new ArrayList<ContactChooserItem>();
         for (ContactChooserItem item : availableContactItems) {
             String value = item.getValue(column);
             if (filterValues.contains(value.toLowerCase())) {
             	filteredItems.add(item);
             }
         }
         Collections.sort(filteredItems, new ContactChooserItemComparator("label", true, false, true));
         availableContactsModel.addAll(filteredItems);
        for (ContactChooserItem item : filteredItems){
        	if (item.isSelected()) availableContactsModel.addToSelection(item);
        }
         
         // update UI
         updateSize();
     }
     
 
     @SuppressWarnings({ "rawtypes"})
 	@Listen("onSelect = #availableContactsBox")
    public void onSelect(SelectEvent e){    
    	List<ContactChooserItem> availableContactsList = availableContactsModel.getInnerList();
     	Set<ContactChooserItem> selectedItems = availableContactsModel.getSelection();
    	for (ContactChooserItem item : availableContactsList){
    		if (!selectedItems.contains(item)) {
    			item.setSelected(false);
    		} else item.setSelected(true);
     	}
     	
     	selectedContactsModel.clear();
    	for (ContactChooserItem item : availableContactItems){
    		if (item.isSelected()) selectedContactsModel.add(item);
    	}
     	Collections.sort(selectedContactsModel.getInnerList(), new ContactChooserItemComparator("label", true, false, true));
        
     	updateSize();
     }
 
     /*
     @Listen("onClick = #selectBtn")    
     public void select() {
         logger.info("select");
         Set<ContactChooserItem> items = availableContactsModel.getSelection();
         for (ContactChooserItem item : items) {
             if (selectedContactsModel.contains(item)) {
                 continue;
             }
             selectedContactsModel.add(item);
 
             // update ui
             item.setSelected(true);
             int index = availableContactsModel.indexOf(item);
             availableContactsModel.set(index, item);
         }
 
         availableContactsModel.clearSelection();
         selectedContactsModel.clearSelection();
 
         updateSize();
     }
 
     @Listen("onClick = #deselectBtn")
     public void deselect() {
         logger.info("deselect");
         Set<ContactChooserItem> items = selectedContactsModel.getSelection();
         
         // update ui
         for (ContactChooserItem item : items) {
         	item.setSelected(false);
             int index = availableContactsModel.indexOf(item);
             availableContactsModel.set(index, item);
         }
         availableContactsModel.clearSelection();
         
         try {
         	selectedContactsModel.removeAll(items);
         } catch (Exception e){
         	Messagebox.show("Error occurs. Don't worry, it is not persisted into database yet. Please click OK to refresh and try again. Hint: Avoid deselecting the last contact together with other contacts at the same time." , "", 
         			Messagebox.OK, Messagebox.ERROR, new EventListener<Event>(){
 
 						@Override
 						public void onEvent(Event event) throws Exception {
 							Executions.sendRedirect(null);
 						}
         		
         	});
         }
         
         updateSize();
     }
 	*/
     
     @Listen("onClick = #addNodeBtn")
     public void createContact() throws InterruptedException {
         AddContactWindow win = new AddContactWindow(this, columns);
         win.doModal();
     }
 
     /**
      * Update interface after the new contact is created
      *
      * @param contact
      * @param add
      */
     public void onNewContactCreated(Node contact, boolean add) {
         ContactChooserItem item = new ContactChooserItem(contact, showImage, columns, questionMap);
         availableContactItems.add(item);
         availableContactsModel.add(item);
         if (add) {
             item.setSelected(true);    
             availableContactsModel.addToSelection(item);
             selectedContactsModel.add(item);
         }
     }
 
     @Transactional
     @Override
     public void save() {
         logger.info("to be implemented.");
 
         // save respondent
         nodeDao.save(respondent);
 
         // save modified contacts
         List<Node> modifiedNodes = new ArrayList<Node>();
         List<Long> addNodeIds = new ArrayList<Long>();
         for (Object o : selectedContactsModel.getInnerList()) {
             ContactChooserItem item = (ContactChooserItem) o;
             if (selectedContactItems.contains(item)) {
                 selectedContactItems.remove(item);
             } else {
                 addNodeIds.add(item.getContact().getId());
             }
         }
         List<Node> nodes = nodeDao.loadByIds(addNodeIds);
         for (Node node : nodes) {
             node.getGroups().add(privateGroup);
             modifiedNodes.add(node);
         }
 
         List<Long> removeNodeIds = new ArrayList<Long>();
         for (ContactChooserItem item : selectedContactItems) {
             removeNodeIds.add(item.getContact().getId());
         }
         nodes = nodeDao.loadByIds(removeNodeIds);
         for (Node node : nodes) {
             node.getGroups().remove(privateGroup);
             modifiedNodes.add(node);
         }
 
         nodeDao.save(modifiedNodes);
     }
 
     private class CheckboxFilterListener implements EventListener<Event> {
 
         @Override
         public void onEvent(Event event) throws Exception {
             filter();
         }
     }
 
     private class FilterEventListener implements EventListener<Event> {
 
         @Override
         public void onEvent(Event event) throws Exception {
             InputEvent e = (InputEvent) event;
             String searchTerm = e.getValue();
             Textbox textbox = (Textbox) event.getTarget();
             textbox.setValue(searchTerm);
 
             availableContactsModel.clear();
             for (ContactChooserItem item : availableContactItems) {
                 boolean matched = true;
                 for (String column : filterMap.keySet()) {
                     String value = item.getValue(column);
                     Textbox tb = filterMap.get(column);
                     String input = tb.getValue();
                     if (input == null || input.isEmpty()) {
                         continue;
                     }
                     if (value == null || value.isEmpty()) {
                         matched = false;
                         break;
                     }
                     if (value.toLowerCase().indexOf(input.toLowerCase()) < 0) {
                         matched = false;
                         break;
                     }
                 }
 
                 if (matched) {
                     availableContactsModel.add(item);
                    if (item.isSelected()) availableContactsModel.addToSelection(item);
                 }
             }
 
             // update UI
             updateSize();
             textbox.setFocus(true);
             textbox.setSelectionRange(searchTerm.length(), searchTerm.length());
         }
     }
 }
