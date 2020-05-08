 package controllers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipException;
 
 import javax.management.RuntimeErrorException;
 
 import models.Job;
 import models.Notification;
 import models.Setting;
 import models.Upload;
 import models.User;
 
 import org.w3c.dom.Node;
 
 import pipeline2.Pipeline2WS;
 import pipeline2.Pipeline2WSResponse;
 import pipeline2.models.script.Argument;
 import pipeline2.models.script.arguments.*;
 import pipeline2.models.Script;
 import play.Logger;
 import play.api.libs.json.Json;
 import play.db.ebean.Transactional;
 import play.libs.XPath;
 import play.mvc.*;
 import scala.actors.threadpool.Arrays;
 
 public class Jobs extends Controller {
 
 	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 	public static Result getJobs() {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null || user.id < 0)
 			return redirect(routes.Login.login());
 		
 		Pipeline2WSResponse jobs = pipeline2.Jobs.get(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"));
 		
 		if (jobs.status != 200) {
 			return Application.error(jobs.status, jobs.statusName, jobs.statusDescription, jobs.asText());
 		}
 		
 		List<Job> jobList = new ArrayList<Job>();
 		
 		List<Node> jobNodes = XPath.selectNodes("//d:job", jobs.asXml(), Pipeline2WS.ns);
 		for (Node jobNode : jobNodes) {
 			
 			Job job = Job.findById(XPath.selectText("@id", jobNode, Pipeline2WS.ns));
 			if (job == null) {
 				Logger.warn("No job with id "+XPath.selectText("@id", jobNode, Pipeline2WS.ns)+" was found.");
 			} else {
 				job.href = XPath.selectText("@href", jobNode, Pipeline2WS.ns);
 				job.status = XPath.selectText("@status", jobNode, Pipeline2WS.ns);
 				if (user.admin || user.id == job.user)
 					jobList.add(job);
 			}
 		}
 		
 		Collections.sort(jobList);
 		Collections.reverse(jobList);
 		if (user.admin)
 			flash("showOwner", "true");
 		
 		flash("browserId",""+new Random().nextLong());
 		return ok(views.html.Jobs.getJobs.render(jobList));
 	}
 	
 	public static Result getJob(String id) {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 		
 		if (id.startsWith("guest")) {
 			Logger.debug(">>> guest");
 			Long userId = Long.parseLong("-"+id.split("-", 3)[1]);
 			Logger.debug(">>> "+userId);
 			id = id.substring(6+(userId+"").length());
 			Logger.debug(">>> "+id);
 			if (userId < 0) {
 				Logger.debug(">>> "+userId+" < 0");
 				Logger.debug("userid parameter set; logging in as given guest user");
 				session("userid", ""+userId);
 		    	session("name", models.Setting.get("guest.name"));
 		    	session("email", "");
 		    	session("password", "");
 		    	session("admin", "false");
 			}
 		}
 		
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null)
 			return redirect(routes.Login.login());
 		
 		Pipeline2WSResponse response = pipeline2.Jobs.get(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id, null);
 		
 		if (response.status != 200 && response.status != 201) {
 			return Application.error(response.status, response.statusName, response.statusDescription, response.asText());
 		}
 		
 		pipeline2.models.Job job = new pipeline2.models.Job(response.asXml());
 		Logger.debug(utils.XML.toString(response.asXml()));
 		
 		Job webuiJob = Job.findById(job.id);
 		if (webuiJob == null) {
 			Logger.debug("Job #"+job.id+" was not found.");
 			return notFound("Sorry; something seems to have gone wrong. The job was not found.");
 		}
 		if (!webuiJob.user.equals(user.id))
 			return forbidden();
 		
 		if (!Job.lastMessageSequence.containsKey(job.id) && job.messages.size() > 0) {
 			Collections.sort(job.messages);
 			Job.lastMessageSequence.put(job.id, job.messages.get(job.messages.size()-1).sequence);
 		}
 		if (!Job.lastStatus.containsKey(job.id)) {
 			Job.lastStatus.put(job.id, job.status);
 		}
 		
 		flash("browserId",""+new Random().nextLong());
 		return ok(views.html.Jobs.getJob.render(job, webuiJob));
 	}
 
 	public static Result getResult(String id) {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 		
 		if (request().queryString().containsKey("userid") && request().queryString().get("userid").length > 0) {
 			Long userId = Long.parseLong(request().queryString().get("userid")[0]);
 			if (userId < 0) {
 				session("userid", ""+userId);
 		    	session("name", models.Setting.get("guest.name"));
 		    	session("email", "");
 		    	session("password", "");
 		    	session("admin", "false");
 			}
 		}
 		
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null)
 			return redirect(routes.Login.login());
 		
 		Pipeline2WSResponse result = pipeline2.Jobs.getResult(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id);
 		
 		// TODO: check content type of incoming stream? Implement result.getContentType() ?
 		
 		response().setHeader("Content-Disposition", "attachment; filename=\"result-"+id+".zip\"");
 		response().setContentType("application/zip");
 
 		return ok(result.asStream());
 	}
 
 	public static Result getLog(final String id) {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 		
 		if (request().queryString().containsKey("userid") && request().queryString().get("userid").length > 0) {
 			Long userId = Long.parseLong(request().queryString().get("userid")[0]);
 			if (userId < 0) {
 				session("userid", ""+userId);
 		    	session("name", models.Setting.get("guest.name"));
 		    	session("email", "");
 		    	session("password", "");
 		    	session("admin", "false");
 			}
 		}
 		
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null)
 			return redirect(routes.Login.login());
 
 		Pipeline2WSResponse jobLog = pipeline2.Jobs.getLog(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id);
 
 		if (jobLog.status != 200 && jobLog.status != 201 && jobLog.status != 204) {
 			return Application.error(jobLog.status, jobLog.statusName, jobLog.statusDescription, jobLog.asText());
 		}
 
 		if (jobLog.status == 204) {
 			return ok(views.html.Jobs.emptyLog.render(id));
 
 		} else {
 			String[] lines = jobLog.asText().split("\n");
 			return ok(views.html.Jobs.getLog.render(id, Arrays.asList(lines)));
 		}
 	}
 
 	@Transactional
 	public static Result postJob() {
 		if (FirstUse.isFirstUse())
 			return redirect(routes.FirstUse.getFirstUse());
 
 		User user = User.authenticate(session("userid"), session("email"), session("password"));
 		if (user == null)
 			return redirect(routes.Login.login());
 
 		Logger.debug("------------------------------ Posting job... ------------------------------");
 
 		Map<String, String[]> params = request().body().asFormUrlEncoded();
 		if (params == null) {
 			return Application.error(500, "Internal Server Error", "Could not read form data", request().body().asText());
 		}
 
 		String id = params.get("id")[0];
 
 		// Get a description of the script from Pipeline 2 Web Service
 		Pipeline2WSResponse scriptResponse = pipeline2.Scripts.get(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id);
 		if (scriptResponse.status != 200) { return Application.error(scriptResponse.status, scriptResponse.statusName, scriptResponse.statusDescription, scriptResponse.asText()); }
 		Script script = new Script(scriptResponse);
 		
 		// Parse and validate the submitted form (also create any necessary output directories in case of local mode)
 		Scripts.ScriptForm scriptForm = new Scripts.ScriptForm(user.id, script, params);
 		String timeString = new Date().getTime()+"";
 		for (Argument arg : script.arguments) {
 			if ("result".equals(arg.output)) {
 				File href = new File(Setting.get("dp2ws.resultDir")+timeString+"/"+arg.kind+"-"+arg.name+"/");
 				href.mkdirs();
 				script.arguments.set(script.arguments.indexOf(arg), new ArgFile(arg, href.toURI().toString()));
 				
 			} else if ("temp".equals(arg.output)) {
 				File href = new File(Setting.get("dp2ws.tempDir")+timeString+"/"+arg.kind+"-"+arg.name+"/");
 				href.mkdirs();
 				script.arguments.set(script.arguments.indexOf(arg), new ArgFile(arg, href.toURI().toString()));
 			}
 		}
 		scriptForm.validate();
 
 		File contextZipFile = null;
 
 		if (scriptForm.uploads.size() > 0) {
 
 			// ---------- See if there's an existing ZIP we can use as the context ----------
 			//	models.Upload contextZipUpload = null;
 			//	Long biggestZipSoFar = 0L;
 			//	
 			//	for (Long uploadId : uploads.keySet()) {
 			//		Upload upload = uploads.get(uploadId);
 			//		if (upload.isZip()) {
 			//			File file = upload.getFile();
 			//			if (file.length() > biggestZipSoFar) {
 			//				biggestZipSoFar = file.length();
 			//				contextZipUpload = upload;
 			//			}
 			//		}
 			//	}
 			//	
 			//	if (contextZipUpload == null)
 			//		Logger.debug("There's no ZIP files available to use as context");
 			//	else
 			//		Logger.debug("The ZIP file '"+contextZipUpload.getFile().getAbsolutePath()+"' is the biggest ZIP file available and will be used as context");
 
 			// ---------- Create a temporary directory ("the context") ----------
 			File contextDir = null;
 			try {
 				contextDir = File.createTempFile("jobContext", "");
 
 				if (!(contextDir.delete())) {
 					Logger.error("Could not delete contextDir file: " + contextDir.getAbsolutePath());
 
 				} if (!(contextDir.mkdir())) {
 					Logger.error("Could not create contextDir directory: " + contextDir.getAbsolutePath());
 				}
 
 			} catch (IOException e) {
 				Logger.error("Could not create temporary file (context directory): "+e.getMessage(), e);
 				return internalServerError("Could not create temporary file (context directory)");
 			}
 
 			Logger.debug("Created context directory: "+contextDir.getAbsolutePath());
 
 			// ---------- Copy or unzip all uploads to a common directory ----------
 			Logger.debug("number of uploads: "+scriptForm.uploads.size());
 			for (Long uploadId : scriptForm.uploads.keySet()) {
 				Upload upload = scriptForm.uploads.get(uploadId);
 				if (upload.isZip()) {
 					//					if (contextZipUpload != null && contextZipUpload.id == upload.id) {
 					//						Logger.debug("not unzipping context zip ("+upload.getFile().getAbsolutePath()+")");
 					//						continue;
 					//					}
 					Logger.debug("unzipping "+upload.getFile()+" to contextDir");
 					try {
 						utils.Files.unzip(upload.getFile(), contextDir);
 					} catch (IOException e) {
 						Logger.error("Unable to unzip files into context directory.", e);
 						return Application.error(500, "Internal Server Error", "Unable to unzip uploaded ZIP file", "");
 					}
 				} else {
 					File from = upload.getFile();
 					File to = new File(contextDir, from.getName());
 					Logger.debug("copying "+from+" to "+to);
 					try {
 						utils.Files.copy(from, to); // We could do file.renameTo here to move it instead of making a copy, but copying makes it easier in case we need to re-run a job
 					} catch (IOException e) {
 						Logger.error("Unable to copy files to context directory.", e);
 						throw new RuntimeErrorException(new Error(e), "Unable to copy files to context directory.");
 					}
 				}
 			}
 			
 			if ("true".equals(Setting.get("dp2ws.sameFilesystem"))) {
 				Logger.debug("Running the Web UI and fwk on the same filesystem, no need to ZIP files...");
 				for (Argument arg : script.arguments) {
					if (arg.output != null)
 						continue;
 					if (arg instanceof ArgFile) {
 						((ArgFile)arg).href = contextDir.toURI().resolve(((ArgFile)arg).href).toString();
 					}
 					if (arg instanceof ArgFiles) {
						for (String href : ((ArgFiles)arg).hrefs) {
							href = contextDir.toURI().resolve(href).toString();
 						}
 					}
 				}
 				
 			} else {
 			
 				//	if (contextZipUpload == null) {
 				if (contextDir.list().length == 0) {
 					contextZipFile = null;
 				} else {
 					try {
 						contextZipFile = File.createTempFile("jobContext", ".zip");
 						Logger.debug("Created job context zip file: "+contextZipFile);
 					} catch (IOException e) {
 						Logger.error("Unable to create temporary job context ZIP file.", e);
 						throw new RuntimeErrorException(new Error(e), "Unable to create temporary job context ZIP file.");
 					}
 					try {
 						utils.Files.zip(contextDir, contextZipFile);
 					} catch (IOException e) {
 						Logger.error("Unable to zip context directory.", e);
 						throw new RuntimeErrorException(new Error(e), "Unable to zip context directory.");
 					}
 				}
 				//	} else {
 				//		contextZipFile = contextZipUpload.getFile();
 				//		try {
 				//			Logger.debug("adding contents of '"+contextDir+"' to the ZIP '"+contextZipFile+"'");
 				//			utils.Files.addDirectoryContentsToZip(contextZipFile, contextDir);
 				//		} catch (IOException e) {
 				//			Logger.error("Unable to add files to existing context ZIP file.", e);
 				//			throw new RuntimeErrorException(new Error(e), "Unable to add files to existing context ZIP file.");
 				//		}
 				//	}
 			}
 
 		}
 		
 		Map<String,String> callbacks = new HashMap<String,String>();
 		if (play.Play.isDev()) { // TODO: only in dev until the callback API is fully implemented
 			callbacks.put("messages", routes.Callbacks.postCallback("messages").absoluteURL(request()));
 			callbacks.put("status", routes.Callbacks.postCallback("status").absoluteURL(request()));
 		}
 		
 		if (contextZipFile == null)
 			Logger.debug("No files in context, submitting job without context ZIP file");
 		else
 			Logger.debug("Context ZIP file is present, submitting job with context ZIP file");
 		
 		Pipeline2WSResponse job = pipeline2.Jobs.post(
 				Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"),
 				scriptForm.script.href, scriptForm.script.arguments, contextZipFile, callbacks
 		);
 		
 		if (job.status != 200 && job.status != 201) {
 			return Application.error(job.status, job.statusName, job.statusDescription, job.asText());
 		}
 		
 		String jobId = XPath.selectText("/*/@id", job.asXml(), Pipeline2WS.ns);
 		Job webUiJob = new Job(jobId, user);
 		webUiJob.nicename = id;
 		if (scriptForm.uploads != null && scriptForm.uploads.size() > 0) {
 			String filenames = "";
 			int i = 0;
 			for (Long uploadId : scriptForm.uploads.keySet()) {
 				if (i > 0)
 					filenames += ", ";
 				if (i++ >= 3) {
 					filenames += "...";
 					break;
 				}
 				filenames += scriptForm.uploads.get(uploadId).getFile().getName();
 			}
 			if (filenames.length() > 0)
 				webUiJob.nicename = id + " ("+filenames+")";
 		}
 		webUiJob.started = new Date();
 		webUiJob.save();
 		User.push(webUiJob.user, new Notification("job-started-"+webUiJob.id, webUiJob.started.toString()));
 		for (Long uploadId : scriptForm.uploads.keySet()) {
 			// associate uploads with job
 			scriptForm.uploads.get(uploadId).job = jobId;
 			scriptForm.uploads.get(uploadId).save();
 		}
 		
 		webUiJob.pushNotifications();
 		
 		if (user.id < 0) {
 			jobId = "guest" + user.id + "-" + jobId;
 			if (scriptForm.guestEmail != null) {
				Account.sendEmail(
						"Job started: "+webUiJob.nicename,
						"To view your Pipeline 2 job, go to this web address: <a href=\""+controllers.routes.Jobs.getJob(jobId).absoluteURL(request())+"\">"+controllers.routes.Jobs.getJob(jobId).absoluteURL(request())+"</a>.",
						"To view your Pipeline 2 job, go to this web address: "+controllers.routes.Jobs.getJob(jobId).absoluteURL(request())+".",
						scriptForm.guestEmail,
						scriptForm.guestEmail
				);
 			}
 		}
 		
 		return redirect(controllers.routes.Jobs.getJob(jobId));
 	}
 	
 }
