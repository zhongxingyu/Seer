 package elw.web;
 
 import elw.dao.*;
 import elw.miniweb.Message;
 import elw.vo.*;
 import org.akraievoy.gear.G4Parse;
 import org.akraievoy.gear.G4mat;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.*;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 public class StudentController extends MultiActionController implements WebSymbols {
 	private static final Logger log = LoggerFactory.getLogger(StudentController.class);
 
 	protected final CourseDao courseDao;
 	protected final GroupDao groupDao;
 	protected final EnrollDao enrollDao;
 	protected final CodeDao codeDao;
 	private final ReportDao reportDao;
 
 	protected final ObjectMapper mapper = new ObjectMapper();
 	protected final long cacheBustingToken = System.currentTimeMillis();
 
 	private static final int UPLOAD_LIMIT = 2 * 1024 * 1024;
 
 	public StudentController(CourseDao courseDao, GroupDao groupDao, EnrollDao enrollDao, final CodeDao codeDao, ReportDao reportDao) {
 		this.courseDao = courseDao;
 		this.groupDao = groupDao;
 		this.enrollDao = enrollDao;
 		this.codeDao = codeDao;
 		this.reportDao = reportDao;
 	}
 
 	protected HashMap<String, Object> auth(final HttpServletRequest req, final HttpServletResponse resp, final boolean redirect) throws IOException {
 		final Group group = (Group) req.getSession(true).getAttribute(S_GROUP);
 		final Student student = (Student) req.getSession(true).getAttribute(S_STUD);
 
 		if (group == null || student == null) {
 			if (redirect) {
 				Message.addWarn(req, "Login required");
 				resp.sendRedirect("login");
 			} else {
 				resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Login required");
 			}
 
 			return null;
 		}
 
 		final HashMap<String, Object> model = new HashMap<String, Object>();
 
 		model.put(S_GROUP, group);
 		model.put(S_STUD, student);
 		model.put(S_MESSAGES, Message.drainMessages(req));
 
 		return model;
 	}
 
 	public ModelAndView do_login(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = new HashMap<String, Object>();
 
 		model.put("groupName", req.getSession(true).getAttribute("groupName"));
 		model.put("studentName", req.getSession(true).getAttribute("studentName"));
 		model.put(S_MESSAGES, Message.drainMessages(req));
 
 		return new ModelAndView("s/login", model);
 	}
 
 	public ModelAndView do_loginPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final String groupName = req.getParameter("groupName");
 		final String studentName = req.getParameter("studentName");
 
 		final HttpSession session = req.getSession(true);
 
 		if (
 				groupName == null || studentName == null ||
 				groupName.trim().length() == 0 || studentName.trim().length() == 0
 		) {
 			Message.addWarn(req, "fields NOT set");
 		} else {
 			final Group[] groups = groupDao.findAllGroups();
 			final Group group = IdName.findByName(groups, groupName, true);
 
 			if (group != null) {
 				final Student[] students = group.getStudents();
 				final Student student = IdName.findByName(students, studentName, true);
 
 				if (student != null) {
 					session.setAttribute(S_GROUP, group);
 					session.setAttribute(S_STUD, student);
 
 					Message.addInfo(req, "logged on");
 					session.removeAttribute("groupName");
 					session.removeAttribute("studentName");
 
 					resp.sendRedirect("courses");
 					return null;
 				} else {
 					Message.addWarn(req, "no such student");
 				}
 			} else {
 				Message.addWarn(req, "no such group");
 			}
 		}
 
 		session.setAttribute("groupName", groupName);
 		session.setAttribute("studentName", studentName);
 
 		resp.sendRedirect("login");
 		return null;
 	}
 
	public ModelAndView do_logout(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		req.getSession(true).invalidate();
		resp.sendRedirect("courses");
		return null;
	}

 	public ModelAndView do_courses(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp, true);
 		if (model == null) {
 			return null;
 		}
 
 		final Group group = (Group) req.getSession().getAttribute(S_GROUP);
 
 		final String groupId = group.getId();
 		List<Course> courses = enrollDao.findCoursesByGroupId(groupId);
 
 		model.put("courses", courses.toArray(new Course[courses.size()]));
 
 		return new ModelAndView("s/courses", model);
 	}
 
 	//	LATER check enrollment also
 	public ModelAndView do_course(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp, true);
 		if (model == null) {
 			return null;
 		}
 
 		final Course course = courseDao.findCourse(req.getParameter("id"));
 		if (course == null) {
 			Message.addWarn(req, "course not found");
 			resp.sendRedirect("courses");
 			return null;
 		}
 
 		final Student student = (Student) req.getSession(true).getAttribute(S_STUD);
 		final int studId = Integer.parseInt(student.getId());
 		model.put("studId", studId);
 		model.put("course", course);
 
 		return new ModelAndView("s/course", model);
 	}
 
 	public ModelAndView do_uploadReport(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, FileUploadException {
 		VersionLookup lookup = versionLookup(req, resp, true);
 		if (lookup == null) {
 			return null;
 		}
 
 		if (lookup.getAss().isShared()) {
 			Message.addWarn(req, "shared assignment do not require any reports");
 			resp.sendRedirect("course?id=" + lookup.course.getId());
 			return null;
 		}
 
 		if (req.getContentLength() > UPLOAD_LIMIT) {
 			Message.addWarn(req, "apparently you're trying to upload more than " + G4mat.formatMem(UPLOAD_LIMIT));
 			resp.sendRedirect("uploadPage?path="+lookup.getPath());
 			return null;
 		}
 
 		final DiskFileItemFactory fif = new DiskFileItemFactory();
 		fif.setRepository(new File(System.getProperty("java.io.tmpdir")));
 		fif.setSizeThreshold(2 * UPLOAD_LIMIT);
 
 		final ServletFileUpload sfu = new ServletFileUpload(fif);
 
 		final FileItemIterator fii = sfu.getItemIterator(req);
 		int fileCount = 0;
 		while (fii.hasNext()) {
 			final FileItemStream item = fii.next();
 			if (item.isFormField()) {
 					continue;
 			}
 
 			if (fileCount > 0) {
 				Message.addWarn(req, "one file per upload, please");
 				resp.sendRedirect("uploadPage?path="+lookup.getPath());
 				return null;
 			}
 
 			final String fileName = item.getName();
 			if (!fileName.endsWith(".rtf")) {
 				Message.addWarn(req, "is that an .rtf file or what?");
 				resp.sendRedirect("uploadPage?path="+lookup.getPath());
 				return null;
 			}
 
 			final String contentType = item.getContentType();
 			if (!"application/rtf".equalsIgnoreCase(contentType)) {
 				Message.addWarn(req, "contentType is '" + contentType + "', which is not what is expected for an .rtf file");
 				resp.sendRedirect("uploadPage?path="+lookup.getPath());
 				return null;
 			}
 
 			final InputStream itemIs = item.openStream();
 			final Group group = (Group) req.getSession(true).getAttribute(S_GROUP);
 			reportDao.createReport(lookup.createPath(group), itemIs);
 			fileCount++;
 		}
 
 		if (fileCount == 1) {
 			Message.addInfo(req, "Your upload has succeeded");
 			resp.sendRedirect("uploadPage?path="+lookup.getPath());
 		} else {
 			Message.addWarn(req, "Something went terribly wrong");
 			resp.sendRedirect("uploadPage?path="+lookup.getPath());
 		}
 
 		return null;
 	}
 
 	public ModelAndView do_uploadPage(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		VersionLookup lookup = versionLookup(req, resp, true);
 		if (lookup == null) {
 			return null;
 		}
 
 		if (lookup.getAss().isShared()) {
 			Message.addWarn(req, "shared assignments do not require any reports");
 			resp.sendRedirect("course?id=" + lookup.course.getId());
 			return null;
 		}
 
 		final HashMap<String, Object> model = lookup.getModel();
 		model.put("verBean", lookup.getVer());
 		model.put("upPath", lookup.getPath());
 		model.put("course", lookup.getCourse());
 		model.put("uploadLimit", G4mat.formatMem(UPLOAD_LIMIT));
 
 		return new ModelAndView("s/uploadPage", model);
 	}
 
 	public ModelAndView do_launch(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		VersionLookup lookup = versionLookup(req, resp, true);
 		if (lookup == null) {
 			return null;
 		}
 
 		final StringWriter verSw = new StringWriter();
 		mapper.writeValue(verSw, lookup.getVer());
 		final Version verCopy = mapper.readValue(verSw.toString(), Version.class);
 
 		final Group group = (Group) req.getSession(true).getAttribute(S_GROUP);
 		final AssignmentPath path = lookup.createPath(group);
 		final long lastStamp = codeDao.findLastStamp(path);
 
 		if (!lookup.getAss().isShared() || lastStamp >= 0) {
 			verCopy.setSolution(codeDao.findCodeByStamp(path, lastStamp));
 		}
 
 		final StringWriter verCopySol = new StringWriter();
 		mapper.writeValue(verCopySol, verCopy);
 		final String solutionStr = verCopySol.toString();
 
 		final HashMap<String, Object> model = lookup.getModel();
 		model.put("verBean", verCopy);
 		//	LATER use HTTP instead of applet parameter for passing the problem/code to applet
 		model.put("ver", solutionStr.replaceAll("&", "&amp;").replaceAll("\"", "&quot;"));
 		model.put("upHeader", "JSESSIONID=" + req.getSession(true).getId());
 		model.put("upPath", lookup.getPath());
 		model.put("cacheBustingToken", cacheBustingToken);
 		model.put("course", lookup.getCourse());
 
 		return new ModelAndView("s/launch", model);
 	}
 
 	public ModelAndView do_upload(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		VersionLookup lookup = versionLookup(req, resp, false);
 		if (lookup == null) {
 			return null;
 		}
 
 		final Group group = (Group) req.getSession(true).getAttribute(S_GROUP);
 		final BufferedReader codeReader = req.getReader();
 		try {
 			codeDao.createCode(lookup.createPath(group), codeReader);
 		} catch (IOException e) {
 			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
 		}
 
 		resp.setStatus(HttpServletResponse.SC_OK);
 		return null;
 	}
 
 	protected VersionLookup versionLookup(HttpServletRequest req, HttpServletResponse resp, final boolean redirect) throws IOException {
 		final VersionLookup vl = new VersionLookup();
 		vl.model = auth(req, resp, redirect);
 		if (vl.model == null) {
 			return null;
 		}
 
 		vl.path = req.getParameter("path");
 		if (vl.path == null) {
 			if (redirect) {
 				Message.addWarn(req, "no assignment path");
 				resp.sendRedirect("courses");
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "no assignment path");
 			}
 			return null;
 		}
 
 		final String[] ids = vl.path.split("--");
 		if (ids.length != 4) {
 			if (redirect) {
 				Message.addWarn(req, "malformed path:" + Arrays.toString(ids));
 				resp.sendRedirect("courses");
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "malformed path:" + Arrays.toString(ids));
 			}
 			return null;
 		}
 
 		final String courseId = ids[0];
 		vl.course = courseDao.findCourse(courseId);
 		if (vl.course == null) {
 			if (redirect) {
 				Message.addWarn(req, "course not found by id " + courseId);
 				resp.sendRedirect("courses");
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "course not found by id " + courseId);
 			}
 			return null;
 		}
 
 		vl.assBundleIndex = G4Parse.parse(ids[1], -1);
 		if (vl.assBundleIndex < 0 || vl.course.getAssBundles().length <= vl.assBundleIndex) {
 			if (redirect) {
 				Message.addWarn(req, "bundle not found by index " + vl.assBundleIndex);
 				resp.sendRedirect("course?id=" + vl.course.getId());
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "bundle not found by index " + vl.assBundleIndex);
 			}
 			return null;
 		}
 
 		vl.assId = ids[2];
 		final AssignmentBundle bundle = vl.course.getAssBundles()[vl.assBundleIndex];
 		vl.ass = IdName.findById(bundle.getAssignments(), vl.assId);
 		if (vl.ass == null) {
 			if (redirect) {
 				Message.addWarn(req, "assignment not found by id " + vl.assId);
 				resp.sendRedirect("course?id=" + vl.course.getId());
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "assignment not found by id " + vl.assId);
 			}
 			return null;
 		}
 
 		final String verIdStr = ids[3];
 		vl.ver = IdName.findById(vl.ass.getVersions(), verIdStr);
 		if (vl.ver == null) {
 			if (redirect) {
 				Message.addWarn(req, "version not found by id " + verIdStr);
 				resp.sendRedirect("course?id=" + vl.course.getId());
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "version not found by id " + verIdStr);
 			}
 			return null;
 		}
 
 		vl.student = (Student) req.getSession(true).getAttribute(S_STUD);
 		final int studId = Integer.parseInt(vl.student.getId());
 		final int verIdx = IdName.indexOfId(vl.ass.getVersions(), vl.ver.getId());
 		if (!vl.ass.isShared() && (studId) % vl.ass.getVersions().length != verIdx) {
 			if (redirect) {
 				Message.addWarn(req, "variant mismatch");
 				resp.sendRedirect("course?id=" + vl.course.getId());
 			} else {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "variant mismatch");
 			}
 			return null;
 		}
 
 		return vl;
 	}
 
 	static class VersionLookup {
 		protected HashMap<String, Object> model;
 		protected String path;
 		protected Course course;
 		protected int assBundleIndex;
 		protected String assId;
 		protected Assignment ass;
 		protected Version ver;
 		protected Student student;
 
 		public VersionLookup() {
 		}
 
 		public Course getCourse() {
 			return course;
 		}
 
 		public int getAssBundleIndex() {
 			return assBundleIndex;
 		}
 
 		public String getAssId() {
 			return assId;
 		}
 
 		public Version getVer() {
 			return ver;
 		}
 
 		public Student getStudent() {
 			return student;
 		}
 
 		public Assignment getAss() {
 			return ass;
 		}
 
 		public HashMap<String, Object> getModel() {
 			return model;
 		}
 
 		public String getPath() {
 			return path;
 		}
 
 		protected AssignmentPath createPath(Group group) {
 			return new AssignmentPath(
 					getCourse().getId(), group.getId(), getStudent(),
 					getAssBundleIndex(), getAssId(), getVer().getId()
 			);
 		}
 	}
 }
