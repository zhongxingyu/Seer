 package edu.helsinki.sulka.controllers;
 
 import java.awt.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import edu.helsinki.sulka.models.RecoveryDatabaseRow;
 import edu.helsinki.sulka.models.RingingDatabaseRow;
 import edu.helsinki.sulka.models.RingingDatabaseRowList;
 import edu.helsinki.sulka.models.User;
 import edu.helsinki.sulka.services.LocalDatabaseService;
 
 
 @Controller
 public class LocalStorageController extends JSONController {
 	
 	@Autowired
 	private LocalDatabaseService localDatabaseService;
 	
 	public static class LocalStorageException extends Exception {
 		private static final long serialVersionUID = 1L;
 		LocalStorageException(String message) {
 			super(message);
 		}
 	}
 	
 	@PreAuthorize("hasRole('USER')")
 	@RequestMapping(value = "/api/storage/ringings",
 					method = RequestMethod.GET,
 					produces = "application/json;charset=UTF-8")
 	@ResponseBody
 	public ListResponse<RingingDatabaseRow> getRingings(HttpSession session) {
 		String userId = ((User) session.getAttribute("user")).getLogin_id();
 		return new ListResponse<RingingDatabaseRow>(localDatabaseService.getRingings(userId));
 	}
 	
 		
 	@PreAuthorize("hasRole('USER')")
 	@RequestMapping(value = "/api/storage/ringings",
 					method = RequestMethod.POST,
 					produces = "application/json;charset=UTF-8",
 					consumes = "application/json")
 	@ResponseBody
 	public ObjectResponse<RingingDatabaseRow> saveRinging(HttpSession session,
 			@RequestBody RingingDatabaseRow ringing,
 			BindingResult bindingResult) throws LocalStorageException {
 		
 		if(bindingResult.hasErrors()){
 			throw new LocalStorageException("Database update failed");
 		}
 		ringing.setUserId(((User) session.getAttribute("user")).getLogin_id());
 		
 		return new ObjectResponse<RingingDatabaseRow>(localDatabaseService.addRinging(ringing));
 	}
 	
 	@PreAuthorize("hasRole('USER')")
 	@RequestMapping(value = "/api/storage/ringings",
 					method = RequestMethod.DELETE,
 					produces = "application/json;charset=UTF-8",
 					consumes="application/json")
 	@ResponseBody
 	public ObjectResponse<String> deleteRinging(HttpSession session,
 			@RequestBody RingingDatabaseRowList ringing,
 			BindingResult bindingResult) throws LocalStorageException {
 		if(bindingResult.hasErrors()){
 			throw new LocalStorageException("Database update failed.");
 		}
 		
 		for (RingingDatabaseRow ringingDatabaseRow : ringing) {
 			if(!((User) session.getAttribute("user")).getLogin_id().equals(ringingDatabaseRow.getUserId()))
 				throw new LocalStorageException("Database update failed. User id and row owner id doesn't match.");
 			localDatabaseService.removeRinging(ringingDatabaseRow);
 
 		}
 		return new ObjectResponse<String>("Database updated.");
 	}
 	
 	@PreAuthorize("hasRole('USER')")
 	@RequestMapping(value = "/api/storage/recoveries",
 					method = RequestMethod.GET,
 					produces = "application/json;charset=UTF-8")
 	@ResponseBody
 	public ListResponse<RecoveryDatabaseRow> getRecoveries(HttpSession session) {
 		String userId = ((User) session.getAttribute("user")).getLogin_id();
 		return new ListResponse<RecoveryDatabaseRow>(localDatabaseService.getRecoveries(userId));
 	}
 	
 	@PreAuthorize("hasRole('USER')")
 	@RequestMapping(value = "/api/storage/recoveries",
 					method = RequestMethod.POST,
 					produces = "application/json;charset=UTF-8",
 					consumes="application/json")
 	@ResponseBody
 	public ObjectResponse<RecoveryDatabaseRow> saveRecovery(HttpSession session,
 			@RequestBody RecoveryDatabaseRow recovery,
 			BindingResult bindingResult) throws LocalStorageException {
 		if(bindingResult.hasErrors()){
 			throw new LocalStorageException("Database update failed");
 		}
 		recovery.setUserId(((User) session.getAttribute("user")).getLogin_id());
 		
 		return new ObjectResponse<RecoveryDatabaseRow>(localDatabaseService.addRecovery(recovery));
 	}
 	
 	@PreAuthorize("hasRole('USER')")
 	@RequestMapping(value = "/api/storage/recoveries",
 					method = RequestMethod.DELETE,
 					produces = "application/json;charset=UTF-8",
 					consumes="application/json")
 	@ResponseBody
 	public ObjectResponse<String> deleteRecovery(HttpSession session,
 			@RequestBody RecoveryDatabaseRow recovery,
 			BindingResult bindingResult) throws LocalStorageException {
 		if(bindingResult.hasErrors()){
 			throw new LocalStorageException("Database update failed.");
 		}
 		if(!((User) session.getAttribute("user")).getLogin_id().equals(recovery.getUserId()))
 			throw new LocalStorageException("Database update failed. User id and row owner id doesn't match.");
 
 		localDatabaseService.removeRecovery(recovery);
 		
 		return new ObjectResponse<String>("Database updated.");
 	}
 	
 	
 	/**
 	 * Handles storage exceptions gracefully.
 	 */
 	@ExceptionHandler(LocalStorageException.class)
 	@ResponseBody
 	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 	public ErrorResponse handleAPIException(Exception e) {
 		return new ErrorResponse(e.getMessage());
 	}
 
 }
