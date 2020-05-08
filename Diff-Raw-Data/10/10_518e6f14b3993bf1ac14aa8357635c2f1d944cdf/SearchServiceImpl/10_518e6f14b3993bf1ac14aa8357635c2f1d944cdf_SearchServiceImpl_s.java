 package net.canadensys.dataportal.vascan.impl;
 
 import java.util.List;
 
 import net.canadensys.dataportal.vascan.SearchService;
 import net.canadensys.dataportal.vascan.dao.NameDAO;
 import net.canadensys.dataportal.vascan.model.NameConceptModelIF;
 import net.canadensys.query.LimitedResult;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service("searchService")
 public class SearchServiceImpl implements SearchService{
 	
 	@Autowired
 	private NameDAO nameDAO;
 
 	@Override
 	public LimitedResult<List<NameConceptModelIF>> searchName(String text) {
 		return nameDAO.search(text);
 	}
 	
 	@Override
 	public LimitedResult<List<NameConceptModelIF>> searchName(String text, int page) {
 		return nameDAO.search(text,page);
 	}
 
 	@Override
 	public List<NameConceptModelIF> searchTaxon(String text) {
 		return nameDAO.searchTaxon(text);
 	}
 
 	@Override
 	public List<NameConceptModelIF> searchVernacularName(String text) {
 		return nameDAO.searchVernacular(text);
 	}
 	
 	@Override
 	public int getPageSize() {
 		return nameDAO.getPageSize();
 	}
 
 }
