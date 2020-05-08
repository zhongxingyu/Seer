 /******************************************************************************
  * CourseArchiveBean.java - created by Sakai App Builder -AZ
  * 
  * Copyright (c) 2008 Sakai Project/Sakai Foundation
  * Licensed under the Educational Community License version 1.0
  * 
  * A copy of the Educational Community License has been included in this 
  * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
  * 
  *****************************************************************************/
 
 package org.sakaiproject.coursearchive.tool.jsf;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sakaiproject.coursearchive.logic.CourseArchiveLogic;
 import org.sakaiproject.coursearchive.logic.ExternalLogic;
 
 import org.sakaiproject.coursearchive.model.CourseArchiveAssignment;
 import org.sakaiproject.coursearchive.model.CourseArchiveItem;
 import org.sakaiproject.coursearchive.model.CourseArchiveSyllabus;
 
 /**
  * This is a backing bean for the JSF app which handles the events and
  * sends the information from the logic layer to the UI
  * @author Sakai App Builder -AZ
  */
 public class CourseArchiveBean {
 
 	private static Log log = LogFactory.getLog(CourseArchiveBean.class);
 
 	private DataModel itemsModel;
 	private DataModel itemStudents;
 	private DataModel itemAssignments;
 	private DataModel itemSyllabi;
 	private DataModel syllabusAttachments;
 
 	private CourseArchiveItemWrapper currentItem = null;
 	private CourseArchiveLogic logic;
 	private ExternalLogic externalLogic;
 
 	private String itemCode;
 	private String itemName;
 	private String itemTerm;
 	private String itemPrimaryInstructor;
 	private String itemOtherInstructors;
 	private String itemAssistants;
 	private String itemComments;
 	private String itemSyllabusURL;
 	private String itemDelegateEid;
 	private String itemDelegateName;
 	private long itemEnrollment;
 	private boolean itemPublic;
 	private boolean itemCanEdit;
 
 	private CourseArchiveSyllabus currentSyllabus;
 
 	private int itemA;
 	private int itemA_MINUS;
 	private int itemB_PLUS;
 	private int itemB;
 	private int itemB_MINUS;
 	private int itemC_PLUS;
 	private int itemC;
 	private int itemC_MINUS;
 	private int itemD_PLUS;
 	private int itemD;
 	private int itemF;
 
 	private boolean hasMore = true;
 
 	private String searchQuery;
 
 	public CourseArchiveBean() {
 	}
 
 	/**
 	 * Actions
 	 */
 
 	public String processActionAddAssignment() {
 		List assignments = (List)itemAssignments.getWrappedData();
 		CourseArchiveAssignment assignment = new CourseArchiveAssignment(currentItem.getItem());
 		CourseArchiveAssignmentWrapper wrapper = new CourseArchiveAssignmentWrapper(assignment);
 		assignments.add(wrapper);
 		itemAssignments.setWrappedData(assignments);
 
 		return "editItem";
 	}
 
 	public String processActionDelete() {
 		FacesContext fc = FacesContext.getCurrentInstance();
 		List items = (List) itemsModel.getWrappedData();
 		int itemsRemoved = 0;
 
 		for(Iterator iter = items.iterator(); iter.hasNext();) {
 			CourseArchiveItemWrapper wrapper = (CourseArchiveItemWrapper)iter.next();
 			if(wrapper.isSelected()) {
 				logic.removeItem(wrapper.getItem());
 				itemsRemoved++;
 			}
 		}
 
 		String message = "Removed " + itemsRemoved + " items";
 		fc.addMessage("items", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
 
 		processActionSearch();
 		return "deletedItems";
 	}
 
 	public String processActionEdit() {
 		if(currentItem == null) { loadCurrentItem(); }
 		return "editItem";
 	}
 
 	public String processActionList() {
 		searchQuery = null;
 		currentItem = null;
 		hasMore     = true;
 		return "listItems";
 	}
 
 	public String processActionSearch() {
 		currentItem = null;
 
		if(searchQuery == null || searchQuery == "") {
 			itemsModel = wrapItems(logic.getUserItems());
 			hasMore    = true;
 		} else {
 			itemsModel = wrapItems(logic.searchItems(searchQuery));
 			hasMore    = false;
 		}
 
 		return "listItems";
 	}
 
 	public String processActionShow() {
 		if(currentItem == null) { loadCurrentItem(); }
 		itemAssignments = wrapAssignments(logic.getItemAssignments(currentItem.getItem()));
 		itemSyllabi = new ListDataModel(logic.getItemSyllabi(currentItem.getItem()));
 		return "showItem";
 	}
 
 	public String processActionShowOlder() {
 		currentItem = null;
 		itemsModel  = wrapItems(logic.getUserItems(true));
 		hasMore     = false;
 		return "listItems";
 	}
 
 	public String processActionShowRoster() {
 		itemStudents = new ListDataModel(logic.getItemStudents(currentItem.getItem()));
 		return "showRoster";
 	}
 
 	public String processActionShowSyllabus() {
 		currentSyllabus = (CourseArchiveSyllabus)itemSyllabi.getRowData();
 		syllabusAttachments = new ListDataModel(logic.getSyllabusAttachments(currentSyllabus));
 		return "showSyllabus";
 	}
 
 	public String processActionUpdate() {
 		FacesContext fc = FacesContext.getCurrentInstance();
 
 		if(itemCode != null && !itemCode.equals("") &&
 		   itemName != null && !itemName.equals("") &&
 		   itemTerm != null && !itemTerm.equals("") &&
 		   itemPrimaryInstructor != null && !itemPrimaryInstructor.equals("")) {
 			CourseArchiveItem item = currentItem.getItem();
 
 			item.setCode(itemCode);
 			item.setName(itemName);
 			item.setTerm(itemTerm);
 			item.setPrimaryInstructor(itemPrimaryInstructor);
 			item.setOtherInstructors(itemOtherInstructors);
 			item.setAssistants(itemAssistants);
 			item.setComments(itemComments);
 			item.setSyllabusURL(itemSyllabusURL);
 			item.setDelegateId(externalLogic.getUserId(itemDelegateEid));
 			item.setPublic(itemPublic);
 
 			item.setA(itemA);
 			item.setA_MINUS(itemA_MINUS);
 			item.setB_PLUS(itemB_PLUS);
 			item.setB(itemB);
 			item.setB_MINUS(itemB_MINUS);
 			item.setC_PLUS(itemC_PLUS);
 			item.setC(itemC);
 			item.setC_MINUS(itemC_MINUS);
 			item.setD_PLUS(itemD_PLUS);
 			item.setD(itemD);
 			item.setF(itemF);
 
 			logic.saveItem(item);
 
 			List items = (List)itemAssignments.getWrappedData();
 
 			for(Iterator iter = items.iterator(); iter.hasNext();) {
 				CourseArchiveAssignmentWrapper wrapper = (CourseArchiveAssignmentWrapper)iter.next();
 				CourseArchiveAssignment assignment = wrapper.getItem();
 
 				if(wrapper.isSelected()) {
 					Long id = assignment.getId();
 					if(id != null && id != 0) {
 						logic.removeAssignment(assignment);
 					}
 				} else {
 					String title = assignment.getTitle();
 					if(title != null && !title.equals("")) {
 						logic.saveAssignment(assignment);
 					}
 				}
 			}
 
 			String message = "Updated item: " + item.getTitle();
 			fc.addMessage("items", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
 
 			resetItem();
 		} else {
 			String message = "Could not add item without a code, name, term or primary instructor.";
 			fc.addMessage("items", new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
 		}
 
 		return "updatedItem";
 	}
 
 	public String processActionMerge() {
 		FacesContext fc = FacesContext.getCurrentInstance();
 		List items = (List) itemsModel.getWrappedData();
 		List<CourseArchiveItem> toMerge = new ArrayList<CourseArchiveItem>();
 
 		for(Iterator iter = items.iterator(); iter.hasNext();) {
 			CourseArchiveItemWrapper wrapper = (CourseArchiveItemWrapper)iter.next();
 			if(wrapper.isSelected()) {
 				toMerge.add(wrapper.getItem());
 			}
 		}
 
 		if(toMerge.size() < 2) {
 			String message = "Select at least 2 items to merge.";
 			fc.addMessage("items", new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
 			return "mergedItems";
 		}
 
 		String ownerId = toMerge.get(0).getOwnerId();
 		boolean ownersMatch = true;
 
 		for(Iterator iter = toMerge.iterator(); iter.hasNext();) {
 			CourseArchiveItem item = (CourseArchiveItem)iter.next();
 			if(!item.getOwnerId().equals(ownerId)) {
 				ownersMatch = false;
 				break;
 			}
 		}
 
 		if(!ownersMatch) {
 			String message = "Items to merge must belong to the same user.";
 			fc.addMessage("items", new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
 			return "mergedItems";
 		}
 
 		logic.mergeItems(toMerge);
 
 		String message = "Merged " + toMerge.size() + " items";
 		fc.addMessage("items", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
 
 		processActionSearch();
 		return "mergedItems";
 	}
 
 	public String processActionArchiveSyllabi() {
 		logic.archiveSyllabi(currentItem.getItem());
 		itemSyllabi = new ListDataModel(logic.getItemSyllabi(currentItem.getItem()));
 
 		String message = "Archived " + itemSyllabi.getRowCount() + " syllabus items.";
 
 		FacesContext fc = FacesContext.getCurrentInstance();
 		fc.addMessage("itemDetails", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
 
 		return "archivedSyllabi";
 	}
 
 	/**
 	 * Item Management
 	 */
 
 	public DataModel getItems() {
 		if(itemsModel == null) {
 			itemsModel = wrapItems(logic.getUserItems());
 		}
 
 		return itemsModel;
 	}
 
 	public String getItemTitle() {
 		return currentItem.getItem().getTitle();
 	}
 
 	public DataModel wrapItems(List items) {
 		log.debug("wrapping items for JSF datatable...");
 		List<CourseArchiveItemWrapper> wrappedItems = new ArrayList<CourseArchiveItemWrapper>();
 
 		for(Iterator iter = items.iterator(); iter.hasNext();) {
 			CourseArchiveItem item = (CourseArchiveItem)iter.next();
 			item.setEnrollment(logic.getItemEnrollment(item));
 			CourseArchiveItemWrapper wrapper = new CourseArchiveItemWrapper(item);
 
 			// Mark the item if the current user owns it and can delete it
 			if(logic.canWriteItem(wrapper.getItem(), externalLogic.getCurrentUserId())) {
 				wrapper.setCanEdit(true);
 			} else {
 				wrapper.setCanEdit(false);
 			}
 
 			wrappedItems.add(wrapper);
 		}
 
 		return new ListDataModel(wrappedItems);
 	}
 
 	public DataModel wrapAssignments(List assignments) {
 		log.debug("wrapping assignments for JSF datatable...");
 		List<CourseArchiveAssignmentWrapper> wrappedAssignments = new ArrayList<CourseArchiveAssignmentWrapper>();
 
 		for(Iterator iter = assignments.iterator(); iter.hasNext();) {
 			CourseArchiveAssignment item = (CourseArchiveAssignment)iter.next();
 			CourseArchiveAssignmentWrapper wrapper = new CourseArchiveAssignmentWrapper(item);
 			wrappedAssignments.add(wrapper);
 		}
 
 		return new ListDataModel(wrappedAssignments);
 	}
 
 	public void resetItem() {
 		currentItem = null;
 		itemCode = "";
 		itemName = "";
 		itemTerm = "";
 		itemPrimaryInstructor = getCurrentUserDisplayName();
 		itemOtherInstructors = "";
 		itemAssistants = "";
 		itemEnrollment = 0;
 		itemComments = "";
 		itemSyllabusURL = "";
 		itemDelegateEid = "";
 		itemDelegateName = "";
 		itemPublic = false;
 		itemStudents = null;
 		itemSyllabi = null;
 	}
 
 	private void loadCurrentItem() {
 		currentItem = (CourseArchiveItemWrapper)itemsModel.getRowData();
 		CourseArchiveItem item = currentItem.getItem();
 
 		itemCode         = item.getCode();
 		itemName         = item.getName();
 		itemTerm         = item.getTerm();
 		itemEnrollment   = item.getEnrollment();
 		itemComments     = item.getComments();
 		itemSyllabusURL  = item.getSyllabusURL();
 		itemDelegateEid  = externalLogic.getUserEid(item.getDelegateId());
 		itemDelegateName = externalLogic.getUserDisplayName(item.getDelegateId());
 		itemPublic       = item.isPublic();
 
 		itemPrimaryInstructor = item.getPrimaryInstructor();
 		itemOtherInstructors  = item.getOtherInstructors();
 		itemAssistants        = item.getAssistants();
 
 		itemA       = item.getA();
 		itemA_MINUS = item.getA_MINUS();
 		itemB_PLUS  = item.getB_PLUS();
 		itemB       = item.getB();
 		itemB_MINUS = item.getB_MINUS();
 		itemC_PLUS  = item.getC_PLUS();
 		itemC       = item.getC();
 		itemC_MINUS = item.getC_MINUS();
 		itemD_PLUS  = item.getD_PLUS();
 		itemD       = item.getD();
 		itemF       = item.getF();
 
 		itemCanEdit = currentItem.isCanEdit();
 	}
 
 	/**
 	 * User Management
 	 */
 
 	public String getCurrentUserDisplayName() {
 		return externalLogic.getUserDisplayName(externalLogic.getCurrentUserId());
 	}
 
 	public Boolean getUserCanDeleteItems() {
 		return logic.canDeleteItems(externalLogic.getCurrentUserId());
 	}
 
 	/**
 	 * Getters and Setters
 	 */
 	public void setLogic(CourseArchiveLogic logic) {
 		this.logic = logic;
 	}
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 	public String getItemCode() {
 		return itemCode;
 	}
 	public void setItemCode(String itemCode) {
 		this.itemCode = itemCode;
 	}
 	public String getItemName() {
 		return itemName;
 	}
 	public void setItemName(String itemName) {
 		this.itemName = itemName;
 	}
 	public String getItemTerm() {
 		return itemTerm;
 	}
 	public void setItemTerm(String itemTerm) {
 		this.itemTerm = itemTerm;
 	}
 	public String getItemPrimaryInstructor() {
 		return itemPrimaryInstructor;
 	}
 	public void setItemPrimaryInstructor(String itemPrimaryInstructor) {
 		this.itemPrimaryInstructor = itemPrimaryInstructor;
 	}
 	public String getItemOtherInstructors() {
 		return itemOtherInstructors;
 	}
 	public void setItemOtherInstructors(String itemOtherInstructors) {
 		this.itemOtherInstructors = itemOtherInstructors;
 	}
 	public String getItemAssistants() {
 		return itemAssistants;
 	}
 	public void setItemAssistants(String itemAssistants) {
 		this.itemAssistants = itemAssistants;
 	}
 	public boolean getItemPublic() {
 		return itemPublic;
 	}
 	public void setItemPublic(boolean itemPublic) {
 		this.itemPublic = itemPublic;
 	}
 	public long getItemEnrollment() {
 		return itemEnrollment;
 	}
 	public String getItemComments() {
 		return itemComments;
 	}
 	public void setItemComments(String itemComments) {
 		this.itemComments = itemComments;
 	}
 	public String getItemSyllabusURL() {
 		return itemSyllabusURL;
 	}
 	public void setItemSyllabusURL(String itemSyllabusURL) {
 		this.itemSyllabusURL = itemSyllabusURL;
 	}
 	public String getItemDelegateEid() {
 		return itemDelegateEid;
 	}
 	public void setItemDelegateEid(String itemDelegateEid) {
 		this.itemDelegateEid = itemDelegateEid;
 	}
 	public String getItemDelegateName() {
 		return itemDelegateName;
 	}
 	public boolean getItemCanEdit() {
 		return itemCanEdit;
 	}
 	public String getSearchQuery() {
 		return searchQuery;
 	}
 	public void setSearchQuery(String searchQuery) {
 		this.searchQuery = searchQuery;
 	}
 	public DataModel getItemStudents() {
 		return itemStudents;
 	}
 	public DataModel getItemAssignments() {
 		return itemAssignments;
 	}
 	public DataModel getItemSyllabi() {
 		return itemSyllabi;
 	}
 	public DataModel getSyllabusAttachments() {
 		return syllabusAttachments;
 	}
 	public CourseArchiveSyllabus getCurrentSyllabus() {
 		return currentSyllabus;
 	}
 	public boolean isHasMore() {
 		return hasMore;
 	}
 	public int getItemA()       { return itemA; }
 	public int getItemA_MINUS() { return itemA_MINUS; }
 	public int getItemB_PLUS()  { return itemB_PLUS; }
 	public int getItemB()       { return itemB; }
 	public int getItemB_MINUS() { return itemB_MINUS; }
 	public int getItemC_PLUS()  { return itemC_PLUS; }
 	public int getItemC()       { return itemC; }
 	public int getItemC_MINUS() { return itemC_MINUS; }
 	public int getItemD_PLUS()  { return itemD_PLUS; }
 	public int getItemD()       { return itemD; }
 	public int getItemF()       { return itemF; }
 	public void setItemA(int itemA)             { this.itemA = itemA; }
 	public void setItemA_MINUS(int itemA_MINUS) { this.itemA_MINUS = itemA_MINUS; }
 	public void setItemB_PLUS(int itemB_PLUS)   { this.itemB_PLUS = itemB_PLUS; }
 	public void setItemB(int itemB)             { this.itemB = itemB; }
 	public void setItemB_MINUS(int itemB_MINUS) { this.itemB_MINUS = itemB_MINUS; }
 	public void setItemC_PLUS(int itemC_PLUS)   { this.itemC_PLUS = itemC_PLUS; }
 	public void setItemC(int itemC)             { this.itemC = itemC; }
 	public void setItemC_MINUS(int itemC_MINUS) { this.itemC_MINUS = itemC_MINUS; }
 	public void setItemD_PLUS(int itemD_PLUS)   { this.itemD_PLUS = itemD_PLUS; }
 	public void setItemD(int itemD)             { this.itemD = itemD; }
 	public void setItemF(int itemF)             { this.itemF = itemF; }
 }
