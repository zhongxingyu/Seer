 package com.bouncingdata.plfdemo.controller;
 
 import java.io.File;
 import java.io.Writer;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.WebRequest;
 
 import com.bouncingdata.plfdemo.datastore.pojo.dto.ActionResult;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.Attachment;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.DashboardDetail;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.DashboardPosition;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.VisualizationDetail;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.VisualizationType;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Analysis;
 import com.bouncingdata.plfdemo.datastore.pojo.model.AnalysisDataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.AnalysisVote;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Comment;
 import com.bouncingdata.plfdemo.datastore.pojo.model.CommentVote;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Dataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Tag;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.UserActionLog;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Visualization;
 import com.bouncingdata.plfdemo.service.ApplicationStoreService;
 import com.bouncingdata.plfdemo.service.BcDatastoreService;
 import com.bouncingdata.plfdemo.service.DatastoreService;
 import com.bouncingdata.plfdemo.util.PageType;
 import com.bouncingdata.plfdemo.util.Utils;
 
 @Controller
 @RequestMapping("/anls")
 public class AnalysisController {
 
 	private Logger logger = LoggerFactory.getLogger(AnalysisController.class);
 
 	@Autowired
 	private DatastoreService datastoreService;
 
 	@Autowired
 	private BcDatastoreService userDataService;
 
 	@Autowired
 	private ApplicationStoreService appStoreService;
 
 	@RequestMapping(value = { "/delanls" }, method = RequestMethod.POST)
 	public @ResponseBody
 	ActionResult deletePageViewStream(
 			@RequestParam(value = "iguid", required = true) String iguid,
 			@RequestParam(value = "iname", required = true) String iname,
 			WebRequest request, ModelMap model, Principal principal,
 			HttpSession session) {
 
 		User user = (User) ((Authentication) principal).getPrincipal();
 		try {
 
 			ObjectMapper logmapper = new ObjectMapper();
 			String data = logmapper.writeValueAsString(new String[] { "0" });
 			datastoreService.logUserAction(user.getId(),
 					UserActionLog.ActionCode.GET_MORE_ACTIVITY, data);
 
 			Analysis anl = datastoreService.getAnalysisByGuid(iguid);
 
 			if (anl == null || !anl.getName().equals(iname))
 				return new ActionResult(-1, "Type of the item isn't found !");
 
 			// delete Analysis
 			boolean isRemoveAnls = datastoreService.removeAnalysis(anl.getId());
 
 			// delete Analysis file in server
 			boolean isDeletefolder = false;
 
 			if (isRemoveAnls)
 				isDeletefolder = appStoreService.deleteAnalysisDirectory(iguid);
 
 			if (isRemoveAnls && isDeletefolder)
 				return new ActionResult(0, "Delete Analysis successfully !");
 
 		} catch (Exception e) {
 			logger.debug("Failed to log action", e);
 		}
 
 		return new ActionResult(-1, "Can't delete analysis !");
 	}
 
 	@RequestMapping(value = { "/dels" }, method = RequestMethod.POST)
 	public @ResponseBody
 	ActionResult deleteAnalysisStream(
 			@RequestParam(value = "iguid", required = true) String iguid,
 			@RequestParam(value = "itype", required = true) String itype,
 			@RequestParam(value = "iname", required = true) String iname,
 			WebRequest request, ModelMap model, Principal principal,
 			HttpSession session) {
 
 		User user = (User) ((Authentication) principal).getPrincipal();
 		try {
 
 			ObjectMapper logmapper = new ObjectMapper();
 			String data = logmapper.writeValueAsString(new String[] { "0" });
 			datastoreService.logUserAction(user.getId(),
 					UserActionLog.ActionCode.GET_MORE_ACTIVITY, data);
 
 			if (itype.toLowerCase().equals("analysis")) {
 				Analysis anl = datastoreService.getAnalysisByGuid(iguid);
 
 				if (anl == null || !anl.getName().equals(iname))
 					return new ActionResult(-1,
 							"Type of the item isn't found !");
 
 				// delete Analysis
 				boolean isRemoveAnls = datastoreService.removeAnalysis(anl
 						.getId());
 
 				// delete Analysis file in server
 				boolean isDeletefolder = false;
 
 				if (isRemoveAnls)
 					isDeletefolder = appStoreService
 							.deleteAnalysisDirectory(iguid);
 
 				if (isRemoveAnls && isDeletefolder)
 					return new ActionResult(0, "Delete Analysis successfully !");
 
 			} else
 				return new ActionResult(-1, "Type of the item isn't found !");
 
 		} catch (Exception e) {
 			logger.debug("Failed to log action", e);
 		}
 
 		return new ActionResult(-1, "User isn't found !");
 	}
 
 	@RequestMapping(value = "/{guid}", method = RequestMethod.GET)
 	public String viewAnalysis(@PathVariable String guid, ModelMap model,
 			Principal principal) {
 		logger.debug("Received request for analysis {}", guid);
 		try {
 			Analysis anls = datastoreService.getAnalysisByGuid(guid);
 			if (anls == null) {
 				model.addAttribute("errorMsg", "Analysis not found!");
 				return "error";
 			}
 
 			User user = (User) ((Authentication) principal).getPrincipal();
 			try {
 				ObjectMapper logmapper = new ObjectMapper();
 				String data = logmapper.writeValueAsString(new String[] { "1",
 						guid });
 				datastoreService.logUserAction(user.getId(),
 						UserActionLog.ActionCode.VIEW_ANALYSIS, data);
 			} catch (Exception e) {
 				logger.debug("Failed to log action", e);
 			}
 			if (anls.getUser().getUsername().equals(user.getUsername())) {
 				model.addAttribute("isOwner", true);
 			} else
 				model.addAttribute("isOwner", false);
 
 			model.addAttribute("anls", anls);
 
 			List<Visualization> visuals = datastoreService
 					.getAnalysisVisualizations(anls.getId());
 			Map<String, VisualizationDetail> visualsMap = null;
 			if (visuals != null) {
 				visualsMap = new HashMap<String, VisualizationDetail>();
 				for (Visualization v : visuals) {
 					if ("html".equals(v.getType())) {
 						visualsMap.put(
 								v.getName(),
 								new VisualizationDetail(v.getGuid(),
 										"visualize/app/" + guid + "/"
 												+ v.getGuid() + "/html",
 										VisualizationType.getVisualType(v
 												.getType())));
 					} else if ("png".equals(v.getType())) {
 						try {
 							String source = appStoreService.getVisualization(
 									guid, v.getGuid(), v.getType());
 							visualsMap
 									.put(v.getName(),
 											new VisualizationDetail(
 													v.getGuid(), source,
 													VisualizationType
 															.getVisualType(v
 																	.getType())));
 						} catch (Exception e) {
 							if (logger.isDebugEnabled()) {
 								logger.debug(
 										"Error occurs when retrieving visualizations {} from analysis {}",
 										v.getGuid(), guid);
 								logger.debug("Exception detail", e);
 							}
 							continue;
 						}
 					}
 				}
 			}
 
 			Map<String, DashboardPosition> dashboard = Utils
 					.parseDashboard(anls);
 
 			DashboardDetail dbDetail = new DashboardDetail(visualsMap,
 					dashboard);
 			ObjectMapper mapper = new ObjectMapper();
 			model.addAttribute("dashboardDetail",
 					mapper.writeValueAsString(dbDetail));
 
 			String code = appStoreService.getScriptCode(guid, null);
 			model.addAttribute("anlsCode",
 					StringEscapeUtils.escapeJavaScript(code));
 
 			try {
 				List<AnalysisDataset> relations = datastoreService
 						.getAnalysisDatasets(anls.getId());
 				if (relations != null) {
 					// key: dataset guid, value: dataset name
 					Map<String, String> datasetList = new HashMap<String, String>();
 					for (AnalysisDataset relation : relations) {
 						Dataset ds = relation.getDataset();
 						datasetList.put(ds.getGuid(), ds.getName());
 					}
 					model.addAttribute("datasetList", datasetList);
 				}
 			} catch (Exception e) {
 				logger.debug("Error when trying to get relation datasets", e);
 			}
 
 			List<Attachment> attachments = appStoreService
 					.getAttachmentData(guid);
 			model.addAttribute("attachments", attachments);
 
 			// page view increment
 			int pageView = datastoreService.increasePageView(anls.getId(),
 					PageType.ANALYSIS.getType());
 			model.addAttribute("pageView", pageView);
 
 			return "analysis";
 		} catch (Exception e) {
 			logger.debug("Failed to load analysis {}", guid);
 			model.addAttribute("errorMsg", e.getMessage());
 			return "error";
 		}
 	}
 
 	/**
 	 * 
 	 * @param guid
 	 * @return
 	 */
 	@RequestMapping(value = "/commentlist/{guid}", method = RequestMethod.GET)
 	public @ResponseBody
 	List<Comment> getCommentList(@PathVariable String guid) {
 		try {
 			Analysis anls = datastoreService.getAnalysisByGuid(guid);
 			if (anls == null)
 				return null;
 
 			return datastoreService.getComments(anls.getId());
 		} catch (Exception e) {
 			logger.debug(
 					"Error occurs when retrieving comment list for analysis {}",
 					guid);
 			return null;
 		}
 	}
 
 	@RequestMapping(value = "/commentpost/{guid}", method = RequestMethod.POST)
 	public @ResponseBody
 	Comment postComment(@PathVariable String guid,
 			@RequestParam(value = "message", required = true) String message,
 			@RequestParam(value = "parentId", required = true) int parentId,
 			ModelMap model, Principal principal) throws Exception {
 
 		User user = (User) ((Authentication) principal).getPrincipal();
 		if (user == null) {
 			logger.debug("User not found!");
 			return null;
 		}
 		try {
 			ObjectMapper logmapper = new ObjectMapper();
 			String data = logmapper.writeValueAsString(new String[] { "3",
 					guid, message, Integer.toBinaryString(parentId) });
 			datastoreService.logUserAction(user.getId(),
 					UserActionLog.ActionCode.POST_COMMENT, data);
 		} catch (Exception e) {
 			logger.debug("Failed to log action", e);
 		}
 		Analysis analysis = datastoreService.getAnalysisByGuid(guid);
 		if (analysis == null) {
 			logger.debug("The analysis {} does not exist anymore.", guid);
 			return null;
 		}
 
 		Comment c = new Comment();
 		c.setAccepted(true);
 		c.setCreateAt(new Date());
 		c.setLastUpdate(new Date());
 		c.setMessage(message);
 		c.setParentId(parentId);
 
 		datastoreService.addComment(user.getId(), analysis.getId(), c);
 		return c;
 	}
 
 	/**
 	 * Votes the analysis
 	 * 
 	 * @param guid
 	 * @param vote
 	 * @param model
 	 * @param principal
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/vote/{guid}", method = RequestMethod.POST)
 	public @ResponseBody
 	String vote(@PathVariable String guid,
 			@RequestParam(value = "vote", required = true) int vote,
 			ModelMap model, Principal principal) throws Exception {
 		User user = (User) ((Authentication) principal).getPrincipal();
 
 		if (user == null) {
 			return "0";
 		}
 		try {
 			ObjectMapper logmapper = new ObjectMapper();
 			String data = logmapper.writeValueAsString(new String[] { "2",
 					guid, Integer.toBinaryString(vote) });
 			datastoreService.logUserAction(user.getId(),
 					UserActionLog.ActionCode.VOTE, data);
 		} catch (Exception e) {
 			logger.debug("Failed to log action", e);
 		}
 
 		Analysis anls = datastoreService.getAnalysisByGuid(guid);
 		if (anls == null) {
 			return "0";
 		}
 
 		vote = vote > 0 ? 1 : -1;
 
 		try {
 
 			AnalysisVote anlsVote = new AnalysisVote();
 			anlsVote.setVote(vote);
 			anlsVote.setVoteAt(new Date());
 			anlsVote.setActive(true);
 			boolean result = datastoreService.addAnalysisVote(user.getId(),
 					anls.getId(), anlsVote);
 
 			return (result ? "1" : "0");
 		} catch (Exception e) {
 			logger.debug(
 					"Failed to add new vote to analysis id {}, user id {}",
 					anls.getId(), user.getId());
 		}
 
 		return "0";
 	}
 
 	/**
 	 * Votes comment
 	 * 
 	 * @param guid
 	 * @param vote
 	 * @param model
 	 * @param principal
 	 */
 	@RequestMapping(value = "/commentvote/{guid}", method = RequestMethod.POST)
 	public @ResponseBody
 	void voteComment(@PathVariable String guid,
 			@RequestParam(value = "commentId", required = true) int commentId,
 			@RequestParam(value = "vote", required = true) int vote,
 			ModelMap model, Principal principal) {
 		User user = (User) ((Authentication) principal).getPrincipal();
 		if (user == null) {
 			return;
 		}
 
 		vote = vote >= 0 ? 1 : -1;
 
 		try {
 
 			try {
 				ObjectMapper logmapper = new ObjectMapper();
 				String data = logmapper.writeValueAsString(new String[] { "2",
 						guid, Integer.toBinaryString(commentId),
 						Integer.toBinaryString(vote) });
 				datastoreService.logUserAction(user.getId(),
 						UserActionLog.ActionCode.VOTE_COMMENT, data);
 			} catch (Exception e) {
 				logger.debug("Failed to log action", e);
 			}
 			Comment comment = datastoreService.getComment(commentId);
 			if (comment == null) {
 				// logging
 				return;
 			}
 
 			CommentVote commentVote = new CommentVote();
 			commentVote.setVote(vote);
 			commentVote.setVoteAt(new Date());
 			commentVote.setActive(true);
 			datastoreService.addCommentVote(user.getId(), commentId,
 					commentVote);
 		} catch (Exception e) {
 			if (logger.isDebugEnabled()) {
 				logger.debug(
 						"Failed to add new vote to comment id {}, user id {}",
 						commentId, user.getId());
 				logger.debug("Exception detail", e);
 			}
 		}
 	}
 
 	@RequestMapping(value = "/{guid}/tagset", method = RequestMethod.GET)
 	public @ResponseBody
 	List<Tag> getAnalysisTagSet(@PathVariable String guid, ModelMap model)
 			throws Exception {
 		Analysis anls = datastoreService.getAnalysisByGuid(guid);
 		if (anls == null) {
 			return null;
 		}
 
 		Set<Tag> tagset = anls.getTags();
 		if (tagset != null && tagset.size() > 0) {
 			ArrayList<Tag> sortedResult = new ArrayList<Tag>(tagset);
 			Collections.sort(sortedResult, new Comparator<Tag>() {
 
 				@Override
 				public int compare(Tag o1, Tag o2) {
 					if (o1.getPopularity() > o2.getPopularity()) {
 						return 1;
 					} else if (o1.getPopularity() == o2.getPopularity()) {
 						return o1.getTag().compareTo(o2.getTag());
 					} else
 						return -1;
 				}
 
 			});
 			return sortedResult;
 		} else
 			return null;
 	}
 
 	@RequestMapping(value = "/clone/processing/{guid}", method = RequestMethod.GET)
 	public String cloneProcess(@PathVariable String guid, ModelMap model)
 			throws Exception {
 		Analysis anls = datastoreService.getAnalysisByGuid(guid);
 		if (anls == null) {
 			model.addAttribute("errorMsg", "Analysis not found!");
 			return "error";
 		}
 		model.addAttribute("guid", guid);
 		return "clone-process";
 	}
 
 	@RequestMapping(value = "/clone/{guid}", method = RequestMethod.GET)
 	public @ResponseBody
 	String clone(@PathVariable String guid, ModelMap model, Principal principal)
 			throws Exception {
 		User user = (User) ((Authentication) principal).getPrincipal();
 		Analysis anls = datastoreService.getAnalysisByGuid(guid);
 		if (anls == null) {
 			return "error";
 		}
 
 		Analysis script = new Analysis();
 		script.setName(anls.getName() + "_clone");
 		script.setDescription(anls.getDescription());
 		script.setLanguage(anls.getLanguage());
 		script.setLineCount(anls.getLineCount());
 		script.setPublished(false);
 		Date date = Utils.getCurrentDate();
 		script.setCreateAt(date);
 		script.setLastUpdate(date);
 		script.setUser(user);
 		script.setExecuted(false);
 		script.setCreateSource("web");
 
 		String newGuid = null;
 		try {
 			newGuid = datastoreService.createBcDataScript(script, "analysis");
 		} catch (Exception e) {
 			logger.error("Failed to create application " + script.getName()
 					+ " for user " + user.getUsername(), e);
 			return "error";
 		}
 
 		if (newGuid == null) {
 			logger.error(
 					"Can't get the guid of application {} so the code cannot saved",
 					script.getName());
 			return "error";
 		}
 
 		// copy script code & visualizations
 		try {
 			String code = appStoreService.getScriptCode(guid,
 					anls.getLanguage());
 			appStoreService.createApplicationFile(newGuid,
 					script.getLanguage(), code);
 		} catch (Exception e) {
 			logger.error("Error occurs when save application code, guid {}",
 					newGuid);
 		}
 
 		List<Visualization> vizs = datastoreService
 				.getAnalysisVisualizations(anls.getId());
 		for (Visualization viz : vizs) {
 			String vGuid = Utils.generateGuid();
 
 			Visualization v = new Visualization();
 			v.setAnalysis(script);
 			v.setUser(user);
 			v.setName(viz.getName());
 			v.setType(viz.getType());
 			v.setGuid(vGuid);
 			v.setActive(true);
 			datastoreService.createVisualization(v);
 
 			File vFile = new File(appStoreService.getStorePath()
 					+ Utils.FILE_SEPARATOR + newGuid + Utils.FILE_SEPARATOR
 					+ "v" + Utils.FILE_SEPARATOR + vGuid + "." + viz.getType());
 
 			appStoreService.copyVisualization(guid, viz.getGuid(),
 					viz.getType(), vFile);
 		}
 
 		return newGuid;
 	}
 
 	@RequestMapping(value = "/changetitle", method = RequestMethod.POST)
 	public @ResponseBody
 	ActionResult changeTitle(
 			@RequestParam(value = "guid", required = true) String guid,
 			@RequestParam(value = "newTitle", required = true) String newTitle,
 			WebRequest request, ModelMap model, Principal principal,
 			HttpSession session) {
 		User user = (User) ((Authentication) principal).getPrincipal();
 		Analysis anls;
 		try {
 			anls = datastoreService.getAnalysisByGuid(guid);
 			if (anls == null) {
 				return new ActionResult(-1, "Can not get analysis");
 			}
 			
 			if (!user.getUsername().equals(anls.getUser().getUsername())) {
 				return new ActionResult(-1,
 						"Error: User does not have permission to change title");
 			}
 		
 			if (anls.getName().equals(newTitle)) {
				return new ActionResult(0,
						"They are the same name. No need to change");
 			} else {
 				anls.setName(newTitle);
 				datastoreService.updateAnalysis(anls);
 			}
 		} catch (Exception e) {
			// TODO Auto-generated catch block			
 			return new ActionResult(-1,
 					"An exception occurs when changing the title");
 		}
 
 		return new ActionResult(0, "OK");
 	}
 
 }
