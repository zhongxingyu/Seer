 package ca.ubc.cpsc304.r3.web.responder;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import ca.ubc.cpsc304.r3.db.ConnectionService;
 import ca.ubc.cpsc304.r3.db.FineDao;
 import ca.ubc.cpsc304.r3.dto.FineDetailedDto;
 import ca.ubc.cpsc304.r3.util.FormUtils;
 import ca.ubc.cpsc304.r3.web.DirectorServlet.ViewAndParams;
 
 public class FineController {
 
 	public ViewAndParams getPayFineForm() {
 		ViewAndParams vp = new ViewAndParams("/jsp/borrower/payFineForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams getPayFineResults(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/borrower/payFineResults.jsp");
 		
 		try {
 		
 			Integer fid = getIntFromParams("fid", request);
 
 			// create dao's for the fine payment
 			FineDao daoFine = new FineDao(ConnectionService.getInstance());
 			
 			// pay the fine
 			int finesPaid = daoFine.payByFineID(fid);
 			
 			// get the results of paying the fine
 			List<FineDetailedDto> paidFines = daoFine.getDetailedByFid(fid);
 			
 			// attach the results to give them to the web page
 			vp.putViewParam("paidFine", paidFines);
 			vp.putViewParam("numFinesPaid", finesPaid);
 			
 			return vp;
 		} catch (Exception e) {
 			e.printStackTrace();
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", FormUtils.generateFriendlyError(e));
 			return vp;
 		}
 	}
 	
 	private Integer getIntFromParams(String key, HttpServletRequest request){
 		@SuppressWarnings("unchecked")
 		Map<String, String[]> reqParams = request.getParameterMap();
 		String[] params = reqParams.get(key);
 
 		Integer i = Integer.valueOf(params[0]);
 		return i;
 	}
 
 
 }
