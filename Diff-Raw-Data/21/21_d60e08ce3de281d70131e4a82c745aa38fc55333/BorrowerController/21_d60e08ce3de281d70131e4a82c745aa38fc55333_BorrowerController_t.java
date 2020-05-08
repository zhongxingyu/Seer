 package ca.ubc.cpsc304.r3.web.responder;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import ca.ubc.cpsc304.r3.db.BookDao;
 import ca.ubc.cpsc304.r3.db.BorrowerDao;
 import ca.ubc.cpsc304.r3.db.BorrowingDao;
 import ca.ubc.cpsc304.r3.db.ConnectionService;
 import ca.ubc.cpsc304.r3.db.FineDao;
 import ca.ubc.cpsc304.r3.db.HoldRequestDao;
 import ca.ubc.cpsc304.r3.dto.BookDto;
 import ca.ubc.cpsc304.r3.dto.BorrowerDto;
 import ca.ubc.cpsc304.r3.dto.BorrowingDetailedDto;
 import ca.ubc.cpsc304.r3.dto.FineDetailedDto;
 import ca.ubc.cpsc304.r3.dto.HoldRequestDetailedDto;
 import ca.ubc.cpsc304.r3.util.FormUtils;
 import ca.ubc.cpsc304.r3.web.DirectorServlet.ViewAndParams;
 
 public class BorrowerController {
 
 	/**
 	 * From requirements: Remove a borrower from the library. The borrow id is
 	 * provided.
 	 * 
 	 * @param request
 	 *            the http request
 	 * @return the removeBorrowerResults view and the associated parameters
 	 */
 	public ViewAndParams removeBorrower(HttpServletRequest request) {
 
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/librarian/removeBorrowerResults.jsp");
 		try {
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
 			FormUtils.checkForBadInput(reqParams);
 			int borrowerId = Integer.parseInt(reqParams.get("borrowerId")[0]);
 
 			BorrowerDao dao = new BorrowerDao(ConnectionService.getInstance());
 			int numBorrowersRemoved = dao.removeBorrower(borrowerId);
 			vp.putViewParam("numBorrowersRemoved", numBorrowersRemoved);
 			vp.putViewParam("borrowerId", borrowerId);
 			return vp;
 
 		} catch (Exception e) {
 
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 
 	}
 
 	public ViewAndParams getRemoveBorrowerForm() {
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/librarian/removeBorrowerForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams addNewBorrower(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/clerk/addNewBorrowerResults.jsp");
 		boolean hasError = false;
 		@SuppressWarnings("unchecked")
 		Map<String, String[]> reqParams = request.getParameterMap();
 
 		try {
 			
 			BorrowerDao bdao = new BorrowerDao(ConnectionService.getInstance());
 			BorrowerDto bdto = new BorrowerDto();
 			FormUtils.checkForBadInput(reqParams);
 			bdto.setAddress(reqParams.get("address")[0]);
 			bdto.setName(reqParams.get("name")[0]);
 			bdto.setEmail(reqParams.get("email")[0]);
 			bdto.setPassword(reqParams.get("password")[0]);
 			bdto.setPhone(Integer.valueOf(reqParams.get("phone")[0]));
 			bdto.setType(reqParams.get("btype")[0]);
 			bdto.setSin(Integer.valueOf(reqParams.get("sin")[0]));
 			int bid = bdao.addBorrower(bdto);
 			vp.putViewParam("bname", reqParams.get("name")[0]);
 			vp.putViewParam("bid", bid);
 		} catch (Exception e) {
 			hasError = true;
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 		}
 		finally {
 			vp.putViewParam("hasError", hasError);
 		}
 		
 		return vp;
 	}
 
 	public ViewAndParams getAddNewBorrowerForm() {
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/clerk/addNewBorrowerForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams getCheckAccountForm() {
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/borrower/checkAccountForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams getCheckAccountResults(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/borrower/checkAccountResults.jsp");
 
 		try {
 
 			Integer bid = getIntFromParams("bid", request);
 
 			// create dao's for the 3 different queries
 			BorrowingDao daoBorrowing = new BorrowingDao(
 					ConnectionService.getInstance());
 			FineDao daoFine = new FineDao(ConnectionService.getInstance());
 			HoldRequestDao daoHoldRequest = new HoldRequestDao(
 					ConnectionService.getInstance());
 			
 			// check to make sure the bid is valid
 			BorrowerDao daoBorower = new BorrowerDao(ConnectionService.getInstance());
 			
 			if(!daoBorower.checkValidBorid(bid)){
 				Exception badBorid = new Exception("Unrecognized borrower ID entered.");
 				throw badBorid;
 			}
 			else{
 
 				// get the results of the 3 queries
 				List<BorrowingDetailedDto> borrowedItems = daoBorrowing.getBorrowedDetailedByID(bid);
 				List<FineDetailedDto> outstandingFines = daoFine.getDetailedUnpaid(bid);
 				List<HoldRequestDetailedDto> currentHolds = daoHoldRequest.getDetailedByID(bid);
 				
 				// get the number of elements in each of the lists
 				int numBorrowedItems = borrowedItems.size();
 				int numOutstandingFines = outstandingFines.size();
 				int numCurrentHolds = currentHolds.size();
 	
 				// attach the results to give them to the web page
 				vp.putViewParam("borrowedItems", borrowedItems);
 				vp.putViewParam("numBorrowedItems", numBorrowedItems);
 				
 				vp.putViewParam("outstandingFines", outstandingFines);
 				vp.putViewParam("numOutstandingFines", numOutstandingFines);
 				
 				vp.putViewParam("currentHolds", currentHolds);
 				vp.putViewParam("numCurrentHolds", numCurrentHolds);
 	
 				return vp;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 	}
 
 	private Integer getIntFromParams(String key, HttpServletRequest request) {
 		@SuppressWarnings("unchecked")
 		Map<String, String[]> reqParams = request.getParameterMap();
 		String[] params = reqParams.get(key);
		FormUtils.checkForBadInput(reqParams);
 
 		Integer i = Integer.valueOf(params[0]);
 		return i;
 	}
 	
 	public ViewAndParams getHoldRequestForm() {
 		ViewAndParams vp = new ViewAndParams(
 				"/jsp/borrower/holdRequestForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams placeHoldRequestResults(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/borrower/holdRequestResults.jsp");
 		try {
 			HoldRequestDao dao = new HoldRequestDao(ConnectionService.getInstance());
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
			FormUtils.checkForBadInput(reqParams);
 			
 			Integer callNo = Integer.valueOf(reqParams.get("callNumber")[0]);
 			Integer borId = Integer.valueOf(reqParams.get("bid")[0]);
 			
 			BookDao daoB = new BookDao(ConnectionService.getInstance());
 			List<BookDto> books =daoB.getByCallNumber(callNo);
 			
 			BorrowerDao daoBo = new BorrowerDao(ConnectionService.getInstance());
 			
 			if(books.size()!=1){
 				Exception badCallNo = new Exception("Invalid call number entered");
 				throw badCallNo;
 			}
 			else if(!daoBo.checkValidBorid(borId)){
 				Exception badBorid = new Exception("Invalid borrower ID entered");
 				throw badBorid;
 			}
 			else{
 				int numHeld = dao.placeByCallNumberAndID(callNo, borId);
 				vp.putViewParam("numHeld", numHeld);
 			}
 		} catch (Exception e){
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 
 		return vp;
 	}
 
 }
