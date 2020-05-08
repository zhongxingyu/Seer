 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import models.State;
 import models.SvtTask;
 import models.SvtUseCase;
 import models.SvtUseCaseFormatException;
 import models.json.Environment;
 import models.json.EnvironmentCount;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Query;
 import com.avaje.ebean.RawSql;
 import com.avaje.ebean.RawSqlBuilder;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class SVTs extends Controller {
 
 	private static Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
 
 	public static Result index() {
 
 		return ok(views.html.suppr.render());
 		// return redirect(routes.Timeline.index());
 	}
 
 	/**
 	 * get the json datas for usecases
 	 * 
 	 * @return
 	 */
 	public static Result getUsecaseHistory() {
 		Map<String, Object> gsonObject = new TreeMap<String, Object>();
 
 		// Get the environments
 		List<EnvironmentCount> list = EnvironmentCount.list();
 		Map<String, Environment> envs = new HashMap<String, Environment>();
 
 		for (EnvironmentCount environmentCount : list) {
 			Environment env = envs.get(environmentCount.name);
 			if (env == null) {
 				env = new Environment(envs.size() + 1, environmentCount.name);
 			}
 			switch (environmentCount.getStatus()) {
 			case OK:
 				if (!environmentCount.getState().equals(State.RUNNING)) {
 					env.addOkCount(environmentCount.count);
 				}
 				break;
 			case WARN:
 				env.addWarnCount(environmentCount.count);
 				break;
 			case ERROR:
 				env.addErrorCount(environmentCount.count);
 				break;
 
 			default:
 				break;
 			}
 			envs.put(environmentCount.name, env);
 		}
 
 		// Add environments to json
 		List<Environment> lst = new ArrayList<Environment>(envs.values());
 		Collections.sort(lst);
 		gsonObject.put("envs", lst);
 
 		// get the useCases
 		// Get the environments
 		String sql   
 		    = " select id, title, start_date, env_name, end_date, description, state, status, running_task, wait_count from ("
 		    + "    select  id, title, start_date, env_name, end_date, description, state, status, running_task, wait_count, "
 		    + "            rank() over (partition by env_name order by start_date desc) as ranking "
 		    + "    from SVT_USE_CASE"
 		    + "               ) where ranking <=3";
 		  
 		RawSql rawSql =   
 		    RawSqlBuilder.parse(sql)  
 		        // map resultSet columns to bean properties  
 		        .columnMapping("id",  					"id")  
 		        .columnMapping("title",  				"title")  
 		        .columnMapping("start_date",  	"startDate")  
 		        .columnMapping("env_name",    	"envName")  
 		        .columnMapping("end_date",    	"endDate")  
 		        .columnMapping("description", 	"description")  
 		        .columnMapping("state",    			"state")  
 		        .columnMapping("status",    		"status")  
 		        .columnMapping("running_task",	"runningTask")  
 		        .columnMapping("wait_count",    "waitCount")  
 		        .create();  
 		  
 		  
 		Query<SvtUseCase> query = Ebean.find(SvtUseCase.class);  
 		    query.setRawSql(rawSql);          
 		    //.where().gt("order.id", 0)  
 		    //.having().gt("totalAmount", 20);  
 		  
 		List<SvtUseCase> ucs =  query.findList();  
 
 		// Rearrange by environment
 		@SuppressWarnings("unchecked")
 		List<SvtUseCase>[] useCaseByenv = new List[lst.size()];
 		for (int i = 0; i < useCaseByenv.length; i++) {
 			useCaseByenv[i] = new ArrayList<SvtUseCase>();
 			
 		}
 		for (SvtUseCase svtUseCase : ucs) {
 			// get the env
 			Environment env = envs.get(svtUseCase.envName);
 			if (env != null) {
 				svtUseCase.initValues(env.getNum());
 				useCaseByenv[env.getNum()-1].add(svtUseCase);
 			}
 		}
 		
 		// sort by env, start_date
 		
 		gsonObject.put("useCases", useCaseByenv);
 
 		return ok(gson.toJson(gsonObject));
 
 	}
 
 	public static Result startTask(Long useCaseId, String taskPath) {
 		// Logger.debug("startTask(" + useCaseId + ", " + taskPath + ")");
 
 		SvtUseCase svtUseCase = SvtUseCase.find.byId(useCaseId);
 		if (svtUseCase == null) {
 			Logger.error("startTask KO : Usecase not found - " + useCaseId + " -");
 			return ok("KO : Usecase not found - " + useCaseId + " -");
 		}
 
 		SvtTask svtTask = svtUseCase.getTaskByName(taskPath);
 		if (svtTask == null) {
 			Logger.error("startTask KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 			return ok("KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 		}
 
 		svtUseCase.runningTask = svtTask.getPath();
 
 		svtTask.state = State.RUNNING;
 		svtTask.status = models.Status.OK;
 		svtTask.startDate = new Date();
 
 		svtUseCase.state = State.RUNNING;
 		svtUseCase.status = models.Status.getWorst(svtUseCase.status, models.Status.OK);
 
 		svtUseCase.save();
 
 		return ok("OK : Task started");
 	}
 
 	public static Result errorTask(Long useCaseId, String taskPath) {
 		// Logger.debug("errorTask(" + useCaseId + ", " + taskPath + ")");
 
 		SvtUseCase svtUseCase = SvtUseCase.find.byId(useCaseId);
 		if (svtUseCase == null) {
 			Logger.error("errorTask KO : Usecase not found - " + useCaseId + " -");
 			return ok("KO : Usecase not found - " + useCaseId + " -");
 		}
 
 		SvtTask svtTask = svtUseCase.getTaskByName(taskPath);
 		if (svtTask == null) {
 			Logger.error("errorTask KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 			return ok("KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 		}
 
 		svtUseCase.status = models.Status.ERROR;
 		svtUseCase.state = models.State.FINISHED;
 		svtUseCase.endDate = new Date();
 
 		// set error to task and parents
 		while (svtTask != null) {
 			svtTask.status = models.Status.ERROR;
 			svtTask.state = models.State.FINISHED;
 			svtTask.endDate = new Date();
 			svtTask = svtUseCase.getTaskByName(svtTask.parentPath);
 		}
 
 		svtUseCase.save();
 
 		return ok("OK : Task set to error");
 	}
 
 	public static Result waitClick(Long useCaseId, Long waitCount, String taskPath) {
 		// Logger.debug("waitClick(" + useCaseId + ", " + waitCount + ", " +
 		// taskPath + ")");
 
 		SvtUseCase svtUseCase = SvtUseCase.find.byId(useCaseId);
 		if (svtUseCase == null) {
 			Logger.error("startTask KO : Usecase not found - " + useCaseId + " -");
 			return ok("KO : Usecase not found - " + useCaseId + " -");
 		}
 
 		SvtTask svtTask = svtUseCase.getTaskByName(taskPath);
 		if (svtTask == null) {
 			Logger.error("startTask KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 			return ok("KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 		}
 
 		if ((svtUseCase.waitCount == null ? 0 : svtUseCase.waitCount) < waitCount) {
 			// Still waiting
 			if (svtTask.state != State.RUNNING) {
 				svtUseCase.runningTask = svtTask.getPath();
 				svtTask.state = State.RUNNING;
 				svtTask.status = models.Status.OK;
 				svtTask.waiting = true;
 				svtTask.startDate = new Date();
 				svtUseCase.save();
 			}
 
 			return ok("WAIT : Wait for a click");
 		} else {
 			svtUseCase.waitCount = waitCount;
 			svtTask.state = State.FINISHED;
 			svtTask.status = models.Status.OK;
 			svtTask.waiting = false;
 			svtTask.endDate = new Date();
 			svtUseCase.save();
 
 			return ok("OK : User click");
 		}
 
 	}
 
 	public static Result unPause(Long useCaseId, Long waitCount) {
 		// Logger.debug("waitClick(" + useCaseId + ", " + waitCount + ", " +
 		// taskPath + ")");
 
 		SvtUseCase svtUseCase = SvtUseCase.find.byId(useCaseId);
 		if (svtUseCase == null) {
 			Logger.error("startTask KO : Usecase not found - " + useCaseId + " -");
 			return ok("KO : Usecase not found - " + useCaseId + " -");
 		}
 
 		SvtTask svtTask = svtUseCase.getTaskByName(svtUseCase.runningTask);
 		if (svtTask != null) {
 			svtTask.state = models.State.FINISHED;
 		}
 
 		svtUseCase.waitCount = waitCount;
 		svtUseCase.save();
 
 		return ok("OK : User click");
 	}
 
 	public static Result startJob(Long useCaseId, String taskPath, String job) {
 		// Logger.debug("startJob(" + useCaseId + ", " + taskPath + ", " + job +
 		// ")");
 
 		SvtUseCase svtUseCase = SvtUseCase.find.byId(useCaseId);
 		if (svtUseCase == null) {
 			Logger.error("startTask KO : Usecase not found - " + useCaseId + " -");
 			return ok("KO : Usecase not found - " + useCaseId + " -");
 		}
 
 		SvtTask svtTask = svtUseCase.getTaskByName(taskPath);
 		if (svtTask == null) {
 			Logger.error("startTask KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 			return ok("KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 		}
 
 		svtTask.runningJob = job;
 		if (svtTask.startDate == null) {
 			svtTask.startDate = new Date();
 		}
 
 		svtUseCase.save();
 
 		return ok("OK : Task started");
 	}
 
 	public static Result finishJob(Long useCaseId, String taskPath, String job) {
 		// Logger.debug("finishJob(" + useCaseId + ", " + taskPath + ", " + job +
 		// ")");
 
 		taskPath = taskPath.replaceFirst("[|]", "");
 		taskPath = taskPath.replaceAll("[|]", "/");
 
 		SvtUseCase svtUseCase = SvtUseCase.find.byId(useCaseId);
 		if (svtUseCase == null) {
 			Logger.error("startTask KO : Usecase not found - " + useCaseId + " -");
 			return ok("KO : Usecase not found - " + useCaseId + " -");
 		}
 
 		SvtTask svtTask = svtUseCase.getTaskByName(taskPath);
 		if (svtTask == null) {
 			Logger.error("startTask KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 			return ok("KO : Task not found - " + useCaseId + " - " + taskPath + " -");
 		}
 
 		// Logger.debug("finishJob "+svtTask.jobDoneCount+" "+svtTask.runningJob);
 		svtTask.runningJob = "";
 		svtTask.jobDoneCount += 1;
 		// Logger.debug("finishJob "+svtTask.jobDoneCount+" "+svtTask.runningJob);
 
 		if (svtTask.jobDoneCount > svtTask.jobCount) {
 			svtTask.status = models.Status.ERROR;
 		} else if (svtTask.jobDoneCount == svtTask.jobCount) {
 			boolean allFinished = true;
 			// check all sub task
 			for (SvtTask child : svtTask.children) {
 				if (child.state != models.State.FINISHED) {
 					allFinished = false;
 					break;
 				}
 			}
 			if (allFinished) {
 				svtTask.state = models.State.FINISHED;
 				svtTask.endDate = new Date();
 			}
 			
 			
 		} 
 
 		svtUseCase.save();
 
 		return ok("OK : Task finished");
 	}
 
 	/**
 	 * Upload a use case file
 	 * 
 	 * @return
 	 */
 	public static Result uploadUseCase(String envName) {
 		MultipartFormData body = request().body().asMultipartFormData();
 
 		try {
 			if (body == null) {
 				Logger.error("uploadUseCase : Missing body");
 				return ok("KO : Missing body");
 			}
 			FilePart picture = body.getFile("uploadedFile");
 			if (picture != null) {
 				File file = picture.getFile();
 
 				SvtUseCase svtUseCase;
 				svtUseCase = new SvtUseCase(envName, file);
 				svtUseCase.save();
 
 				// Logger.debug("uploadUseCase -> "+svtUseCase.getId());
 				return ok("OK : " + svtUseCase.getId());
 			} else {
 				Logger.error("uploadUseCase : Missing uploadedFile");
 				return ok("KO : Missing file");
 			}
 		} catch (IOException e) {
 			Logger.error("uploadUseCase : " + e.getMessage());
 			return internalServerError(e.getMessage());
 		} catch (SvtUseCaseFormatException e) {
 			Logger.error("uploadUseCase : " + e.getMessage());
 			return ok("KO : " + e.getMessage());
 		}
 	}
 	
 	
 }
