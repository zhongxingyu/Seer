 package controllers.admin;
 
 import controllers.CRUD;
 import controllers.Check;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import models.InputFile;
 import models.OutputFile;
 import models.Role;
 import models.Solution;
 import models.Task;
 import models.User;
 import play.db.jpa.JPA;
 import play.mvc.After;
 import play.mvc.Before;
 import play.mvc.Router;
 import play.mvc.With;
 import services.task.ContestantStatistics;
 import services.task.ContestantResult;
 import services.task.SystemReplyStatistics;
 import services.task.SystemReplyResult;
 
 @Check(Role.Check.ADMIN)
 @With({
 	controllers.Secure.class,
 	controllers.admin.with.CRUDSearch.class,
 	controllers.admin.with.Menu.class
 })
 public class Tasks extends CRUD {
 	
 	@Before(only="create")
 	public static void beforeCreate(java.io.File inputData, java.io.File outputData) throws IOException {
 		params.put("object.creator.id", User.getLoggedUser().getId().toString());
 		validation.required(inputData);
 		
 		if(!validation.hasErrors()) {
 			InputFile inputFile = new InputFile(inputData);
 			inputFile.save();
 			params.put("object.inputData.id", inputFile.getId().toString());
 			
 			if(outputData != null) {
 				OutputFile outputFile = new OutputFile(outputData);
 				outputFile.save();
 				params.put("object.outputData.id", outputFile.getId().toString());
 			}
 		}
 	}
 	
 	@Before(only="save")
 	public static void beforeSave(Long id, java.io.File inputData, java.io.File outputData, boolean removeOutputData) throws IOException {
         models.Task task = models.Task.findById(id);
 		notFoundIfNull(task);
 
 		if(removeOutputData)
 			task.deleteOutputData();
 		
 		if(inputData != null) {
 			InputFile inputFile = new InputFile(inputData);
 			inputFile.save();
 			params.put("object.inputData.id", inputFile.getId().toString());
 		}
 		if(outputData != null) {
 			OutputFile outputFile = new OutputFile(outputData);
 			outputFile.save();
 			params.put("object.outputData.id", outputFile.getId().toString());
 		}
 	}
 	
 	@After(only={"save", "delete"})
 	public static void afterSaveAndDelete() {
 		InputFile.deleteUnused();
 		OutputFile.deleteUnused();
 	}
 	
 	
 	@After
 	public static void rollbackOnValidationError() {
 		if(validation.hasErrors())
 			JPA.setRollbackOnly();
 	}
 	
 	public static void statistics(Long id) {
 		Task task = Task.findById(id);
 		notFoundIfNull(task);
 
 		Collection<SystemReplyResult> systemReplyStatistics = new SystemReplyStatistics().getStatistics(task);
 		Collection<ContestantResult> contestantStatistics = new ContestantStatistics().getStatistics(task, 10);
 		
 		render(task, systemReplyStatistics, contestantStatistics);
 	}
 	
 	public static void gotoSolution(Long task, Long user, Integer time) {
 		Solution first = Solution.find("task.id = ? AND user.id=? AND timeLength=?", task, user, time).first();
 		notFoundIfNull(first);
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("id", first);
 		redirect(Router.getFullUrl("admin.Solutions.show", map));
 	}
 	
 }
