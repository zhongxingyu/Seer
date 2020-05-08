 package elw.web;
 
 import elw.dao.CourseDao;
 import elw.dao.EnrollDao;
 import elw.dao.GroupDao;
 import elw.miniweb.Message;
 import elw.vo.*;
 import org.akraievoy.gear.G4Parse;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 public class StudentController extends MultiActionController {
 	private static final Logger log = LoggerFactory.getLogger(StudentController.class);
 
 	public static final String S_GROUP = "elw_group";
 	public static final String S_STUD = "elw_student";
 	public static final String S_MESSAGES = "elw_messages";
 
 	protected final CourseDao courseDao;
 	protected final GroupDao groupDao;
 	protected final EnrollDao enrollDao;
 
 	protected final ObjectMapper mapper = new ObjectMapper();
 
 	public StudentController(CourseDao courseDao, GroupDao groupDao, EnrollDao enrollDao) {
 		this.courseDao = courseDao;
 		this.groupDao = groupDao;
 		this.enrollDao = enrollDao;
 	}
 
 	protected HashMap<String, Object> auth(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final Group group = (Group) req.getSession(true).getAttribute(S_GROUP);
 		final Student student = (Student) req.getSession(true).getAttribute(S_STUD);
 
 		if (group == null || student == null) {
 			Message.addWarn(req, "Login required");
 			resp.sendRedirect("login");
 
 			return null;
 		}
 
 		final HashMap<String, Object> model = new HashMap<String, Object>();
 
 		model.put(S_GROUP, group);
 		model.put(S_STUD, student);
 
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
 
 	public ModelAndView do_courses(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp);
 		if (model == null) {
 			return null;
 		}
 
 		final Group group = (Group) req.getSession().getAttribute(S_GROUP);
 
 		final String groupId = group.getId();
 		List<Course> courses = enrollDao.findCoursesByGroupId(groupId);
 
 		model.put("courses", courses.toArray(new Course[courses.size()]));
 		model.put(S_MESSAGES, Message.drainMessages(req));
 
 		return new ModelAndView("s/courses", model);
 	}
 
 	public ModelAndView do_course(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp);
 		if (model == null) {
 			return null;
 		}
 
 		final Course course = courseDao.findCourse(req.getParameter("id"));
 		if (course == null) {
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		model.put("course", course);
 
 		return new ModelAndView("s/course", model);
 	}
 
 	public ModelAndView do_launch(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final HashMap<String, Object> model = auth(req, resp);
 		if (model == null) {
 			return null;
 		}
 
 		final String path = req.getParameter("path");
 		final String[] ids = path.split("--");
 		if (ids.length != 4) {
 			log.warn("malformed path {}", Arrays.toString(ids));
 
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final String courseId = ids[0];
 		final Course course = courseDao.findCourse(courseId);
 		if (course == null) {
 			log.warn("course not found by id {}", courseId);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final int assBundleIndex = G4Parse.parse(ids[1], -1);
 		if (assBundleIndex < 0 || course.getAssBundles().length <= assBundleIndex) {
 			log.warn("bundle not found by index {}", assBundleIndex);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final String assId = ids[2];
 		final AssignmentBundle bundle = course.getAssBundles()[assBundleIndex];
 		final Assignment ass = IdName.findById(bundle.getAssignments(), assId);
 		if (ass == null) {
 			log.warn("assignment not found by id {}", assId);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final String verId = ids[3];
 		final Version ver = IdName.findById(ass.getVersions(), verId);
 		if (ver == null) {
 			log.warn("assignment not found by id {}", verId);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final StringWriter verSw = new StringWriter();
 		mapper.writeValue(verSw, ver);
 
 		final Version verNoSolution = mapper.readValue(verSw.toString(), Version.class);
 		verNoSolution.setSolution(new String[]{"#  your code", "#    goes here", "#      :)"});
 
 		final StringWriter verNsSw = new StringWriter();
 		mapper.writeValue(verNsSw, verNoSolution);
 
 		model.put("ver", verNsSw.toString().replaceAll("&", "&amp;").replaceAll("\"", "&quot;"));
 
 		return new ModelAndView("s/launch", model);
 	}
 }
