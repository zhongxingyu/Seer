 package com.sanisidro.bean;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.component.UICommand;
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 import com.sanisidro.to.UserTO;
 import com.sanisidro.to.UserTypeTO;
 import com.sanisidro.wrapper.SanIsidroWrapper;
 
 public class SearchCustomerMB 
 {
 	private String message;
 	private String messageSuccess;
 	private ArrayList<SelectItem> userTipes = new ArrayList<SelectItem>();
 	private List<UserTO> customers = new ArrayList<UserTO>();
 	private Long idCurrent;
 	private long totalRows;
     private long firstRow;
     private long rowsPerPage;
     private int currentPage;
     private int totalPages;
     private UserTO userSearch = new UserTO();
	private Long userTypeSelected;	
 	
 	public String search()
 	{
 		UserTypeTO userTypeTO = new UserTypeTO();
 		userTypeTO.setId(userTypeSelected);
 		UserTypeTO type = SanIsidroWrapper.getInstance().getUserTypeByID(userTypeTO);
 		userSearch.setType(type);	
 		page(0); 
 		init(firstRow, rowsPerPage);
 		return "";
 	}
     
     public String pageFirst() {
         page(0);        
         return "";
     }
 
     public String pageNext() {
         page(firstRow + rowsPerPage);
         return "";
     }
 
     public String pagePrevious() {
         page(firstRow - rowsPerPage);
         return "";
     }
     
     public void page(ActionEvent event) {
         page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);        
     }
 
     public String pageLast() {
         page(totalRows - ((totalRows % rowsPerPage != 0) ? totalRows % rowsPerPage : rowsPerPage));
         return "";
     }
     
     private void page(long firstRow) {
         this.firstRow = firstRow;
         currentPage = (int)(firstRow / rowsPerPage) + 1;
         init(firstRow, rowsPerPage);
     }
 
 	public ArrayList<SelectItem> getUserTipes() {
 		return userTipes;
 	}
 
 	public void setUserTipes(ArrayList<SelectItem> userTipes) {
 		this.userTipes = userTipes;
 	}
 
 	public Long getIdCurrent() {
 		return idCurrent;
 	}
 
 	public void setIdCurrent(Long idCurrent) {
 		this.idCurrent = idCurrent;
 	}
 
 	public SearchCustomerMB()
 	{
 		rowsPerPage = 10;       
         firstRow = 0;
         currentPage = 1;        
 		init(firstRow, rowsPerPage);
 	}	
 	public void init(long first, long maxResutl) 
 	{		
 		try
 		{
 			customers = SanIsidroWrapper.getInstance().getAllCustomers((int)first, (int)maxResutl, userSearch);
 			totalRows = SanIsidroWrapper.getInstance().coutUsers();
 			userTipes.clear();
 			setTotalPages(totalRows % rowsPerPage == 0 ? (int)(totalRows / rowsPerPage) : (int)(totalRows / rowsPerPage) + 1); 
 			List<UserTypeTO> tos = SanIsidroWrapper.getInstance().getAllUserTypes();
 			for (UserTypeTO u : tos)
 			{
 				userTipes.add(new SelectItem(u.getId(), u.getName()));
 			}
 		}
 		catch (Exception e)
 		{
 			message = e.getMessage();
 		}
 	}
 	
 	public String editCustomer()
 	{
 		messageSuccess = "";
 		for (UserTO to : customers)
 		{
 			if (to.getDni() == idCurrent)
 			{
 				to.setEditable(false);				
 			}
 			else if (!to.isEditable())
 			{
 				to.setEditable(true);
 			}
 		}
 		return "";
 	}
 	
 	public String updateCustomer()
 	{	
 		for (UserTO u : customers)
 		{
 			if (!u.isEditable())
 			{
 				try 
 				{
 					u = SanIsidroWrapper.getInstance().updateUser(u);
 					u.setEditable(true);
 					messageSuccess = "Actualizacin Exitosa";
 					message = "";
 					init(firstRow, rowsPerPage);
 				} 
 				catch (Exception e) 
 				{
 					message = e.getMessage();
 				}
 				break;				
 			}
 		}		
 		return "";
 	}
 	
 	
 	
 	public int getCurrentPage() {
 		return currentPage;
 	}
 
 	public void setCurrentPage(int currentPage) {
 		this.currentPage = currentPage;
 	}
 
 	public String getAllCustomers()
 	{
 		return "";
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public List<UserTO> getCustomers() {
 		return customers;
 	}
 
 	public void setCustomers(List<UserTO> customers) {
 		this.customers = customers;
 	}
 
 	public String getMessageSuccess() {
 		return messageSuccess;
 	}
 
 	public void setMessageSuccess(String messageSuccess) {
 		this.messageSuccess = messageSuccess;
 	}
 
 	public long getFirstRow() {
 		return firstRow;
 	}
 
 	public void setFirstRow(long firstRow) {
 		this.firstRow = firstRow;
 	}
 
 	public long getRowsPerPage() {
 		return rowsPerPage;
 	}
 
 	public void setRowsPerPage(long rowsPerPage) {
 		this.rowsPerPage = rowsPerPage;
 	}	
 
 
 
 	public long getTotalRows() {
 		return totalRows;
 	}
 
 	public void setTotalRows(long totalRows) {
 		this.totalRows = totalRows;
 	}
 
 	public int getTotalPages() {
 		return totalPages;
 	}
 
 	public void setTotalPages(int totalPages) {
 		this.totalPages = totalPages;
 	}
 
 	public UserTO getUserSearch() {
 		return userSearch;
 	}
 
 	public void setUserSearch(UserTO userSearch) {
 		this.userSearch = userSearch;
 	}
 
 	public Long getUserTypeSelected() {
 		return userTypeSelected;
 	}
 
 	public void setUserTypeSelected(Long userTypeSelected) {
 		this.userTypeSelected = userTypeSelected;
 	}	
 	
 	
 }
