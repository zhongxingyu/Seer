 package com.bk.lazydatamodel;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.bk.model.Book;
 import com.bk.service.BookService;
 import com.bk.util.PaginatedHibernateSearch;
 
 /**
  * @author Andrei Petraru
  * Mar 23, 2013
  */
 @Component
 public class BookSearchLazyDataModel extends LazyDataModel<Book> {
 
 	@Autowired
 	private BookService bookService;
 
 	private String searchTerm;
 
 	@Override
 	public List<Book> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
		if (StringUtils.length(searchTerm) < 3) {
 			return null;
 		}
 		
 		PaginatedHibernateSearch<Book> books = bookService.search(searchTerm, first, pageSize); 
 		setRowCount(books.getResultsSize());
 		return books.getResults();
 	}
 
 	public String getSearchTerm() {
 		return searchTerm;
 	}
 
 	public void setSearchTerm(String searchTerm) {
 		this.searchTerm = searchTerm;
 	}
 
 }
