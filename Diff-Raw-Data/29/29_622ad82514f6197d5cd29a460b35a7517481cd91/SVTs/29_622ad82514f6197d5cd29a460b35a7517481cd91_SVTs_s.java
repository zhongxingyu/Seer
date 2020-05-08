 package controllers;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
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
 		List<SvtUseCase> ucs = SvtUseCase.find.orderBy("start_date desc").findList();
 		for (SvtUseCase svtUseCase : ucs) {
 			// get the env
 			Environment env = envs.get(svtUseCase.envName);
 			if (env != null) {
 				svtUseCase.initValues(env.getNum());
 			}
 			
 		}
 		gsonObject.put("useCases", ucs);
 
 		return ok(gson.toJson(gsonObject));
 
 	}
 
 	public static Result startTask(Long useCaseId, String taskPath) {
 		Logger.debug("startTask(" + useCaseId + ", " + taskPath + ")");
 
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
 
 		svtUseCase.state = State.RUNNING;
 		svtUseCase.status = models.Status.getWorst(svtUseCase.status, models.Status.OK);
 
 		// TODO : Stop running task (and check job count)
 		// TODO : Stop running use case on the same env
 
 		svtUseCase.save();
 
 		return ok("OK : Task started");
 	}
 
 	public static Result waitClick(Long useCaseId, Long waitCount, String taskPath) {
 		Logger.debug("waitClick(" + useCaseId + ", " + waitCount + ", " + taskPath + ")");
 
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
 			svtUseCase.runningTask = svtTask.getPath();
 			svtTask.state = State.RUNNING;
 			svtTask.status = models.Status.OK;
 			svtTask.waiting = true;
 			svtUseCase.save();
 
 			return ok("WAIT : Wait for a click");
 		} else {
 			svtUseCase.waitCount = waitCount;
 			svtTask.state = State.FINISHED;
 			svtTask.status = models.Status.OK;
 			svtTask.waiting = false;
 			svtUseCase.save();
 
 			return ok("OK : User click");
 		}
 
 	}
 
 	public static Result startJob(Long useCaseId, String taskPath, String job) {
 		Logger.debug("startJob(" + useCaseId + ", " + taskPath + ", " + job + ")");
 
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
 
 		// TODO : Check Job count
 
 		svtUseCase.save();
 
 		return ok("OK : Task started");
 	}
 
 	public static Result finishJob(Long useCaseId, String taskPath, String job) {
 		Logger.debug("finishJob(" + useCaseId + ", " + taskPath + ", " + job + ")");
 
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
 
 		Logger.debug("finishJob "+svtTask.jobDoneCount+" "+svtTask.runningJob);
 		svtTask.runningJob = "";
 		svtTask.jobDoneCount += 1;
 		Logger.debug("finishJob "+svtTask.jobDoneCount+" "+svtTask.runningJob);
 
 		// TODO : Check Job count
 
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
 				// TODO : check older usecase and clean them
 				svtUseCase.save();
 
 				Logger.debug("uploadUseCase -> "+svtUseCase.getId());
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
