 	/*
  * ELW : e-learning workspace
  * Copyright (C) 2010  Anton Kraievoy
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package elw.web;
 
 import elw.dao.*;
 import elw.miniweb.Message;
 import elw.miniweb.ViewJackson;
 import elw.vo.*;
 import elw.web.core.Core;
 import elw.web.core.LogFilter;
 import elw.web.core.W;
 import org.akraievoy.gear.G4Parse;
 import org.apache.commons.fileupload.FileUploadException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.*;
 import java.util.*;
 
 	@Controller
 @RequestMapping("/a/**/*")
 public class AdminController extends ControllerElw {
 	private static final Logger log = LoggerFactory.getLogger(AdminController.class);
 	
 	private final CourseDao courseDao;
 	private final GroupDao groupDao;
 	private final EnrollDao enrollDao;
 	private final ScoreDao scoreDao;
 	private final FileDao fileDao;
 
 	private final AdminDao adminDao;
 	private final SortedSet<String> validNonces = new TreeSet<String>();
 
 	public AdminController(
 			CourseDao courseDao, EnrollDao enrollDao, GroupDao groupDao, ScoreDao scoreDao, FileDao fileDao,
 			AdminDao adminDao,
 			Core core
 	) {
 		super(core);
 
 		this.courseDao = courseDao;
 		this.enrollDao = enrollDao;
 		this.groupDao = groupDao;
 		this.scoreDao = scoreDao;
 		this.fileDao = fileDao;
 
 		this.adminDao = adminDao;
 	}
 
 	protected HashMap<String, Object> auth(final HttpServletRequest req, final HttpServletResponse resp, final String pathToRoot) throws IOException {
 		final HttpSession session = req.getSession(true);
 		final Admin admin = (Admin) session.getAttribute(S_ADMIN);
 
 		if (admin == null) {
 			if (pathToRoot != null) {
 				Message.addWarn(req, "Admin authentication required");
 				if (req.getQueryString() != null) {
 					session.setAttribute("loginTo", req.getRequestURI() + "?" + req.getQueryString());
 				} else {
 					session.setAttribute("loginTo", req.getRequestURI());
 				}
 				resp.sendRedirect(pathToRoot + "login");
 			} else {
 				resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin authentication required");
 			}
 
 			return null;
 		}
 
 		session.removeAttribute("loginTo");	//	LATER extract constant
 
 		final HashMap<String, Object> model = prepareDefaultModel(req);
 		model.put(S_ADMIN, admin);
 		model.put(R_CTX, Ctx.fromString(req.getParameter(R_CTX)).resolve(enrollDao, groupDao, courseDao));
 
 		return model;
 	}
 
 	@RequestMapping(value = "login", method = RequestMethod.GET)
 	public ModelAndView do_login(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = prepareDefaultModel(req);
 
 		final String nonce = Long.toString(System.currentTimeMillis(), 36);
 		synchronized (validNonces) {
 			validNonces.add(nonce);
 		}
 		model.put("nonce", nonce);
 
 		return new ModelAndView("a/login", model);
 	}
 
 	@RequestMapping(value = "loginPost", method = RequestMethod.POST)
 	public ModelAndView do_loginPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final String login = req.getParameter("login");
 		final String nonce = req.getParameter("nonce");
 		final String hash = req.getParameter("hash");
 
 		final HttpSession session = req.getSession(true);
 		if (
 				nonce == null || nonce.trim().length() == 0 ||
 				hash == null || hash.trim().length() == 0 ||
 				login == null || login.trim().length() == 0
 		) {
 			Message.addWarn(req, "nonce/hash parameters NOT set");
 		} else {
 			boolean nonceValid;
 			synchronized (validNonces) {
 				nonceValid = validNonces.remove(nonce);
 				validNonces.headSet(nonce).clear();
 			}
 
 			if (nonceValid) {
 				final Admin admin = adminDao.findAdminById(login);
 				final String hashExpected;
 				if (admin != null) {
 					hashExpected = Ctx.digest(nonce + "-->" + login + "-->" + admin.getPassword());
 				} else {
 					hashExpected = null;
 				}
 
 				if (hashExpected != null && hashExpected.equalsIgnoreCase(hash)) {
 					session.setAttribute(S_ADMIN, admin);
 					session.setMaxInactiveInterval(300);
 					Message.addInfo(req, "Admin area : logged on");
 					final Object loginToAttr = session.getAttribute("loginTo");
 					if (loginToAttr instanceof String) {
 						session.removeAttribute("loginTo");
 						resp.sendRedirect((String) loginToAttr);
 					} else {
 						resp.sendRedirect("index");
 					}
 					return null;
 				} else {
 					Message.addWarn(req, "terribly wrong login/password, please retry");
 				}
 			} else {
 				Message.addWarn(req, "nonce expired, please retry authentication");
 			}
 		}
 
 		resp.sendRedirect("login");
 		return null;
 	}
 
 	@RequestMapping(value = "logout", method = RequestMethod.GET)
 	public ModelAndView do_logout(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		req.getSession(true).invalidate();
 		resp.sendRedirect("index");
 		return null;
 	}
 
 	@RequestMapping(value = "index", method = RequestMethod.GET)
 	public ModelAndView do_index(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp, "");
 		if (model == null) {
 			return null;
 		}
 
 		return new ModelAndView("a/index", model);
 	}
 
 	@RequestMapping(value = "rest/index", method = RequestMethod.GET)
 	public ModelAndView do_restIndex(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp, null);
 		if (model == null) {
 			return null;
 		}
 
 		final Enrollment[] enrolls = enrollDao.findAllEnrollments();
 		final List<Object[]> indexData = core.index(enrolls);
 		return new ModelAndView(ViewJackson.success(indexData));
 	}
 
 	@RequestMapping(value = "summary", method = RequestMethod.GET)
 	public ModelAndView do_summary(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmECG(req, resp, "", new WebMethodCtx() {
 			@Override public ModelAndView handleCtx() throws IOException {
 				W.storeFilter(req, model);
 				W.filterDefault(model, "f_mode", "a");
 				final LogFilter filter = W.parseFilter(req);
 
 				final SortedMap<String, Map<String, List<Entry<FileMeta>>>> fileMetas = new TreeMap<String, Map<String, List<Entry<FileMeta>>>>();
 				final Map<String, Double> ctxToScore = new HashMap<String, Double>();
 
 				core.tasksData(ctx, filter, true, fileMetas, ctxToScore, null);
 
 				model.put("fileMetas", fileMetas);
 				model.put("ctxToScore", ctxToScore);
 				model.put("totalBudget", ctx.getEnr().cmpTotalBudget());
 
 				return new ModelAndView("a/summary", model);
 			}
 		});
 	}
 
 	@RequestMapping(value = "log", method = RequestMethod.GET)
 	public ModelAndView do_log(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmECG(req, resp, "", new WebMethodCtx() {
 			public ModelAndView handleCtx() throws IOException {
 				W.storeFilter(req, model);
 				return new ModelAndView("a/log", model);
 			}
 		});
 	}
 
 	@RequestMapping(value = "rest/log", method = RequestMethod.GET)
 	public ModelAndView do_restLog(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmECG(req, resp, null, new WebMethodCtx() {
 			public ModelAndView handleCtx() throws IOException {
 				final Format format = (Format) model.get(FormatTool.MODEL_KEY);
 				final List<Object[]> logData = core.log(
 						ctx, format, W.parseFilter(req), true
 				);
 
 				return new ModelAndView(ViewJackson.success(logData));
 			}
 		});
 	}
 
 	@RequestMapping(value = "tasks", method = RequestMethod.GET)
 	public ModelAndView do_tasks(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmECG(req, resp, "", new WebMethodCtx() {
 			public ModelAndView handleCtx() throws IOException {
 				W.storeFilter(req, model);
 				return new ModelAndView("a/tasks", model);
 			}
 		});
 	}
 
 	@RequestMapping(value = "rest/tasks", method = RequestMethod.GET)
 	public ModelAndView do_restTasks(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmECG(req, resp, null, new WebMethodCtx() {
 			public ModelAndView handleCtx() throws IOException {
 				final Format format = (Format) model.get(FormatTool.MODEL_KEY);
 
 				final List<Object[]> logData = core.tasks(
 						ctx, W.parseFilter(req), format, true
 				);
 
 				return new ModelAndView(ViewJackson.success(logData));
 			}
 		});
 	}
 
 	@RequestMapping(value = "approve", method = RequestMethod.GET)
 	public ModelAndView do_approve(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmScore(req, resp, "", new WebMethodScore() {
 			public ModelAndView handleScore(
 					HttpServletRequest req, HttpServletResponse resp, Ctx ctx, FileSlot slot, Entry<FileMeta> file, Stamp stamp, Map<String, Object> model
 			) {
 				final SortedMap<Stamp, Entry<Score>> allScores = scoreDao.findAllScores(ctx, slot.getId(), file.getMeta().getId());
 
 				final Entry<Score> lastScoreEntry = allScores.isEmpty() ? null : allScores.get(allScores.lastKey());
 				final Entry<Score> scoreEntry = stamp == null ? lastScoreEntry : allScores.get(stamp);
 				final Score score = ScoreDao.updateAutos(ctx, slot.getId(), file, scoreEntry == null ? null : scoreEntry.getMeta());
 
 				model.put("stamp", stamp);
 				model.put("score", score);
 				model.put("slot", slot);
 				model.put("file", file);
 
 				return new ModelAndView("a/approve", model);
 			}
 		});
 	}
 
 	@RequestMapping(value = "rest/scoreLog", method = RequestMethod.GET)
 	public ModelAndView do_restScoreLog(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmScore(req, resp, null, new WebMethodScore() {
 			public ModelAndView handleScore(
 					HttpServletRequest req, HttpServletResponse resp, Ctx ctx, FileSlot slot, Entry<FileMeta> file, Stamp stamp, Map<String, Object> model
 			) {
 				final Format f = (Format) model.get(FormatTool.MODEL_KEY);
 
 				final ScoreDao scoreDaoLocal = scoreDao;
 				final SortedMap<Stamp, Entry<Score>> allScores = scoreDaoLocal.findAllScoresAuto(ctx, slot, file);
 
 				final String mode = req.getParameter("f_mode");
 
 				final List<Object[]> logData = core.logScore(allScores, ctx, slot, file, f, mode, stamp);
 
 				return new ModelAndView(ViewJackson.success(logData));
 			}
 		});
 	}
 
 	@RequestMapping(value = "approve", method = RequestMethod.POST)
 	public ModelAndView do_approvePost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmScore(req, resp, "", new WebMethodScore() {
 			public ModelAndView handleScore(
 					HttpServletRequest req, HttpServletResponse resp, Ctx ctx, FileSlot slot, Entry<FileMeta> file, Stamp stamp, Map<String, Object> model
 			) throws IOException {
 				final Score scoreByStamp;
 				final String fileId = file.getMeta().getId();
 				if (stamp == null) {
 					scoreByStamp = null;
 				} else {
 					scoreByStamp = scoreDao.findScoreByStamp(ctx, stamp, slot.getId(), fileId).getMeta();
 				}
 				final Score score = ScoreDao.updateAutos(ctx, slot.getId(), file, scoreByStamp);
 
 				final String action = req.getParameter("action");
 				if ("next".equalsIgnoreCase(action) || "approve".equalsIgnoreCase(action) || "decline".equalsIgnoreCase(action)) {
 					if ("approve".equalsIgnoreCase(action) || "decline".equalsIgnoreCase(action)) {
 						score.setApproved("approve".equalsIgnoreCase(action));
 
 						final Criteria[] cris = slot.getCriterias();
 						for (Criteria cri : cris) {
 							final int powDef = score.getPow(slot, cri);
 							final double ratioDef = score.getRatio(slot, cri);
 
 							final String idFor = score.idFor(slot, cri);
 							final String powReq = req.getParameter(idFor);
 							final String ratioReq = req.getParameter(idFor+"--ratio");
 
 							score.getPows().put(idFor, G4Parse.parse(powReq, powDef));
 							score.getRatios().put(idFor, G4Parse.parse(ratioReq, ratioDef));
 						}
 
 						score.setComment(req.getParameter("comment"));
 						scoreDao.createScore(ctx, slot.getId(), fileId, score);
 					}
 
 					resp.sendRedirect(core.cmpForwardToEarliestPendingSince(ctx, slot, file.getMeta().getCreateStamp()));
 				} else {
 					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad action: " + action);
 				}
 
 				return null;
 			}
 		});
 	}
 
 	@RequestMapping(value = "groups", method = RequestMethod.GET)
 	public ModelAndView do_groups(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp, "");
 		if (model == null) {
 			return null;
 		}
 
 		final Group[] groups = groupDao.findAllGroups();
 
 		model.put("groups", groups);
 
 		return new ModelAndView("a/groups", model);
 	}
 
 	@RequestMapping(value = "ul", method = {RequestMethod.GET})
 	public ModelAndView do_ul(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, FileUploadException {
 		return wmFile(req, resp, "", null, new WebMethodFile() {
 			@Override
 			protected ModelAndView handleFile(String scope, FileSlot slot) throws IOException {
 				model.put("slot", slot);
				final Ctx ctx = (Ctx) model.get("elw_ctx");
				model.put("elw_ctx_type", Ctx.forEnr(ctx.getEnr()).extCourse(ctx.getCourse()).extIndex(ctx.getIndex()));
 				return new ModelAndView("a/ul", model);
 			}
 		});
 	}
 
 	@RequestMapping(value = "ul", method = RequestMethod.POST)
 	public ModelAndView do_ulPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, FileUploadException {
 		return wmFile(req, resp, "", null, new WebMethodFile() {
 			@Override
 			protected ModelAndView handleFile(String scope, FileSlot slot) throws IOException {
 				final String failureUri = core.getUri().upload(ctx, scope, slot.getId());
 				final String refreshUri = core.getUri().logCourseE(ctx.getEnr().getId());
 				final String authorName = ((Admin)model.get(S_ADMIN)).getName();
 
 				return storeFile(scope, slot, refreshUri, failureUri, authorName, core.getFileDao());
 			}
 		});
 	}
 
 	@RequestMapping(value = "dl/*.*", method = RequestMethod.GET)
 	public ModelAndView do_dl(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		return wmFile(req, resp, "../", null, new WebMethodFile() {
 			@Override
 			protected ModelAndView handleFile(String scope, FileSlot slot) throws IOException {
 				final String fileId = req.getParameter("fId");
 				if (fileId == null || fileId.trim().length() == 0) {
 					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "no fileId (fId) defined");
 					return null;
 				}
 
 				final Entry<FileMeta> entry = fileDao.findFileFor(scope, ctx, slot.getId(), fileId);
 				if (entry == null) {
 					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no file found");
 					return null;
 				}
 
 				retrieveFile(slot, entry);
 
 				return null;
 			}
 		});
 	}
 }
 
