 package ca.ubc.cpsc304.r3.web.responder;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import ca.ubc.cpsc304.r3.db.BookDao;
 import ca.ubc.cpsc304.r3.db.ConnectionService;
 import ca.ubc.cpsc304.r3.dto.BookDto;
 import ca.ubc.cpsc304.r3.util.FormUtils;
 import ca.ubc.cpsc304.r3.web.DirectorServlet.ViewAndParams;
 
 public class BookController {
 
 	public ViewAndParams getNewBookForm(){
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/addNewBookForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams addNewBook(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/addNewBookResults.jsp");
 
 		try {
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
 			FormUtils.checkForBadInput(reqParams);
 
 			BookDto dto = new BookDto();
 			dto.setIsbn(Integer.parseInt(reqParams.get("isbn")[0]));
 			dto.setMainAuthor(reqParams.get("author")[0]);
 			dto.setTitle(reqParams.get("title")[0]);
 			dto.setPublisher(reqParams.get("publisher")[0]);
 			dto.setYear(Integer.parseInt(reqParams.get("year")[0]));
 
 			BookDao dao = new BookDao(ConnectionService.getInstance());
 			dao.addNewBook(dto);
 
 			vp.putViewParam("bookAdded", dto);
 			return vp;
 
 		} catch (Exception e){
			e.printStackTrace();
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 	}
 
 	/**
 	 * Gets the addNewBookCopyFrom
 	 * @return the addNewBookCopyForm
 	 */
 	public ViewAndParams getNewBookCopyForm() {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/addNewBookCopyForm.jsp");
 		return vp;
 	}
 
 	/**
 	 * Adds a new copy of a book
 	 * @param request the http request
 	 * @return the results page and params detailing the status of the operation
 	 */
 	public ViewAndParams addNewBookCopy(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/addNewBookCopyResults.jsp");
 		try {
 			
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
 			FormUtils.checkForBadInput(reqParams);
 
 			int callNumber = Integer.parseInt(reqParams.get("callNumber")[0]);
 			BookDao dao = new BookDao(ConnectionService.getInstance());
 			dao.addNewBookCopy(callNumber);
 			vp.putViewParam("callNumber", callNumber);
 
 			return vp;
 
 		} catch (Exception e){
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 
 	}
 
 	public ViewAndParams getRemoveBookForm() {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/removeBookForm.jsp");
 		return vp;
 	}
 
 	/**
 	 * 
 	 * From reqs:
 	 * Remove a book from the catalogue.  The librarian provides 
 	 * the catalogue number for the item and the system removes 
 	 * it from the database.
 	 * 
 	 * Note: I'm treating catalogue number as callNumber
 	 * 
 	 * @param request
 	 * @return the removeBookResults view and its view parameters
 	 */
 	public ViewAndParams removeBook(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/removeBookResults.jsp");
 		try {
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> params = request.getParameterMap();
 			FormUtils.checkForBadInput(params);
 			BookDao dao = new BookDao(ConnectionService.getInstance());
 			int callNumber = Integer.parseInt(params.get("callNumber")[0]);
 			int numBooksRemoved = dao.removeBook(callNumber);
 
 			vp.putViewParam("numBooksRemoved", numBooksRemoved);
 			vp.putViewParam("callNumber", callNumber);
 			return vp;
 		} catch (Exception e){
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 	}
 
 	public ViewAndParams getBookSearchForm(){
 		ViewAndParams vp = new ViewAndParams("/jsp/borrower/searchBooksForm.jsp");
 		return vp;
 	}
 	
 	public ViewAndParams getBookSearchResults(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/borrower/searchBooksResults.jsp");
 		try {
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
 
 			String keyword = reqParams.get("keyword")[0];
 			String stype = reqParams.get("stype")[0];
 			
 			if(stype.equals("titles")){
 				BookDao dao = new BookDao(ConnectionService.getInstance());
 				List<BookDto> dto = dao.searchTitleByKeyword(keyword);
 				if(dto.size()==0){
 					Exception e = new Exception("No books found with a title matching that keyword.");
 					throw e;
 				}
 				vp.putViewParam("books", dto);
 				return vp;
 			}
 			else if(stype.equals("authors")){
 				BookDao dao = new BookDao(ConnectionService.getInstance());
 				List<BookDto> dto = dao.searchAuthorByKeyword(keyword);
 				if(dto.size()==0){
 					Exception e = new Exception("No books found with an author matching that keyword.");
 					throw e;
 				}
 				vp.putViewParam("books", dto);
 				return vp;
 			}
 			else if(stype.equals("subjects")){
 				BookDao dao = new BookDao(ConnectionService.getInstance());
 				List<BookDto> dto = dao.searchSubjectByKeyword(keyword);
 				if(dto.size()==0){
 					Exception e = new Exception("No books found with a subject matching that keyword.");
 					throw e;
 				}
 				vp.putViewParam("books", dto);
 				return vp;
 			}
 			else{
 				Exception e = new Exception("Unrecognized keyword search type");
 				throw e;
 			}
 
 		} catch (Exception e){
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 	}
 }
