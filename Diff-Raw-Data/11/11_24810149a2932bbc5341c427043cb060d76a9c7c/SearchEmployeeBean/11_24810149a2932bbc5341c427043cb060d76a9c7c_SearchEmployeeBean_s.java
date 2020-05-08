 package fall13.dec.agileanalyzer.session;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import fall13.dec.agileanalyzer.access.EmployeeDAO;
 import fall13.dec.agileanalyzer.model.Employee;
 
 @ManagedBean
 @ViewScoped
 public class SearchEmployeeBean implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	@EJB EmployeeDAO empDAO;
 	private String searchKeyword;
 	private List<Employee> searchResults;
 	
 	public String getSearchKeyword() {
 		return searchKeyword;
 	}
 	public void setSearchKeyword(String searchKeyword) {
 		this.searchKeyword = searchKeyword;
 	}
 	public List<Employee> getSearchResults() {
 		return searchResults;
 	}
 	public void setSearchResults(List<Employee> searchResults) {
 		this.searchResults = searchResults;
 	}
 	
 	
 	public void search(){
		if(searchKeyword!=null || Integer.parseInt(searchKeyword)==0)
 		this.searchResults = empDAO.getEmployeeByName(searchKeyword);
 		else{
 		this.searchResults = new ArrayList<Employee>();
 		this.searchResults.add(empDAO.getEmployeeById(Integer.parseInt(searchKeyword)));
 		}
 	}
 	
 	
 	
 
 }
