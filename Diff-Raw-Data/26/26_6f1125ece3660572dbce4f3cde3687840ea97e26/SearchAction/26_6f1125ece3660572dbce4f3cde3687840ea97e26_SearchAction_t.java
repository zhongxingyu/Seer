 package com.perevillega.mynet.action;
 
 import javax.persistence.EntityManager;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.log.Log;
 
 import com.perevillega.mynet.action.modelsupport.LinkList;
import com.perevillega.mynet.model.Category;
import com.perevillega.mynet.model.Tag;
 
 @Name("search")
 @Scope(ScopeType.CONVERSATION)
 public class SearchAction {
 	
 	@Logger	private Log log;
 
 	@In protected EntityManager entityManager;
 
 	@In protected FacesMessages facesMessages;	
 
 	@In(create=true) private LinkList linkList;
 	
 	private String terms;
 
 	public String search(){
 		linkList.setSearch(terms);
 		linkList.refresh();		
 		return "/search.xhtml"; 
 	}
	
	public String search(Tag tag){
		linkList.setTag(tag);
		linkList.refresh();		
		return "/byTag.xhtml"; 
	}
	
	public String search(Category category){
		linkList.setCategory(category);
		linkList.refresh();		
		return "/byCategory.xhtml"; 
	}
 
 	public String getTerms() {
 		return terms;
 	}
 
 	public void setTerms(String terms) {
 		this.terms = terms;
 	}
 	
 }
