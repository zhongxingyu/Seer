 package com.infinitiessoft.btrs.custom;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Out;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.log.Log;
 
 import com.infinitiessoft.btrs.action.ReportList;
 import com.infinitiessoft.btrs.enums.ReportTypeEnum;
 import com.infinitiessoft.btrs.enums.StatusEnum;
 import com.infinitiessoft.btrs.exceptions.BtrsRuntimeException;
 import com.infinitiessoft.btrs.model.Report;
 
 @Name("reportListDataPreparator")
 @Scope(ScopeType.CONVERSATION)
 public class ReportListDataPreparator implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Logger Log log;
 	
 	@In
 	ReportList reportList;
 	
 	@Out(required = false)
 	List<Report> preparedReports;
 	
 	private Map<Report, Boolean> selectedRowsMap;
 	private boolean selectAll;
 	
 	
 	private int scrollerPage = 1;
 
 
 	public int getScrollerPage() {
 		return scrollerPage;
 	}
 
 	public void setScrollerPage(int scrollerPage) {
 		this.scrollerPage = scrollerPage;
 	}
 	
 	public Map<Report, Boolean> getSelectedRowsMap() {
 		if (selectedRowsMap == null) {
 			selectedRowsMap = new HashMap<Report, Boolean>();
 		}
 		return selectedRowsMap;
 	}
 
 	public void setSelectedRowsMap(Map<Report, Boolean> selectedRowsMap) {
 		this.selectedRowsMap = selectedRowsMap;
 	}
 
 	public List<Report> getSelectedRows() {
 		List<Report> selectedRows = new ArrayList<Report>();
         for (Report key : getSelectedRowsMap().keySet()) {
             if (getSelectedRowsMap().get(key) == true){
                 selectedRows.add(key);
             }
         }
         log.debug("Selected for export reports are: #0", selectedRows);
         return selectedRows;
     }
 	
 	
 
 	public boolean isSelectAll() {
 		return selectAll;
 	}
 
 	public void setSelectAll(boolean selectAll) {
 		this.selectAll = selectAll;
 	}
 
 	public List<Report> getPreparedReports() {
 		return preparedReports;
 	}
 
 	public void setPreparedReports(List<Report> preparedReports) {
 		this.preparedReports = preparedReports;
 	}
 
 	public void prepareList(String type) {
 		long beginTime = System.currentTimeMillis();
 		log.debug("Preparing reports has been started");
 		
 		if (type == null || type.trim().isEmpty()) {
 			type = defaultType();
 		}
 		
 		List<Report> reports = null;
 		if (ReportTypeEnum.SUBMITTED.name().equalsIgnoreCase(type)) {
 			reports = reportList.getReportsForUserByStatus(StatusEnum.SUBMITTED);
 		} else if (ReportTypeEnum.REJECTED.name().equalsIgnoreCase(type)) {
 			reports = reportList.getReportsForUserByStatus(StatusEnum.REJECTED);
 		} else if (ReportTypeEnum.ALL.name().equalsIgnoreCase(type)) {
 			reports = reportList.getReportsWithExpensesForUser();
 		} else if (ReportTypeEnum.APPROVED.name().equalsIgnoreCase(type)) {
 			reports = reportList.getReportsForUserByStatus(StatusEnum.APPROVED);
 		} else if (ReportTypeEnum.GLOBAL.name().equalsIgnoreCase(type)) {
 			reports = reportList.getReportsGlobal();
 		} else {
 			throw new BtrsRuntimeException("Wrong type: " + type);
 		}
 		preparedReports = reports;
 		
 		long endTime = System.currentTimeMillis();
 		log.info("Preparing reports took: #0 ms", endTime - beginTime);
 	}
 
 	public String defaultType() {
 		return ReportTypeEnum.SUBMITTED.getLabel();
 	}
 
 	public void changeMap(Map<Report, Boolean> selectedRowsMap, Boolean blnValue) {
 		if (selectedRowsMap != null) {
 			Iterator<Report> itr = preparedReports.iterator();
 			while (itr.hasNext()) {
 				selectedRowsMap.put(itr.next(), blnValue);
 			}
 		}
 	}
 	
 	public void selectAllReports(ValueChangeEvent event) {
 		selectAll = !selectAll;
 		changeMap(getSelectedRowsMap(), selectAll);
 		FacesContext.getCurrentInstance().renderResponse();
 	}
 
 }
