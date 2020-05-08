 package com.rakursy.timetable.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 
 import org.drools.planner.config.XmlSolverFactory;
 import org.drools.planner.core.Solver;
 import org.drools.planner.core.event.BestSolutionChangedEvent;
 import org.drools.planner.core.event.SolverEventListener;
 import org.jboss.logging.Logger;
 import org.jboss.solder.logging.Category;
 
 import com.rakursy.timetable.model.DayOfWeek;
 import com.rakursy.timetable.model.Grade;
 import com.rakursy.timetable.model.GradeSubject;
 import com.rakursy.timetable.model.Period;
 import com.rakursy.timetable.model.PeriodOffRequest;
 import com.rakursy.timetable.model.Room;
 import com.rakursy.timetable.model.SchoolClass;
 import com.rakursy.timetable.model.SchoolDay;
 import com.rakursy.timetable.model.SchoolHour;
 import com.rakursy.timetable.model.StudentGroup;
 import com.rakursy.timetable.model.Subject;
 import com.rakursy.timetable.model.Teacher;
 import com.rakursy.timetable.model.Timetable;
 import com.rakursy.timetable.model.User;
 
 @Stateful
 @SessionScoped
 public class SolverManager {
 	
 	public static enum SolverState {
 		NEW,
 		UNINITIALIZED_TERMINATED,
 		INITIALIZING,
 		SOLVING,
 		TERMINATED,
 		FINISHED_UNSAVED,
 		FINISHED_SAVED;
 	}
 	
 	private static final String SOLVER_CONFIG = "/com/rakursy/timetable/rules/timetableScoreRules.xml";
 	
 	@Inject
 	@Category("timetable")
 	private Logger log;
 	
 	@Inject
 	@Named("loggedInUser")
 	private User user;
 	
 	@Inject
 	private EntityManager em;
 	
 	private Solver solver;
 	
 	private Timetable solution;
 
 	private Future<?> solverTask;
 	
 	private SolverState solverState;
 	
 	@Inject
 	private SolverPoolManager poolManager;
 	
 	public void initializeSolver() {
 		solverStarting();
 		
 		solver = createSolver();
 		solution = createSolution();
 		solver.setPlanningProblem(solution);
 
 		solver.addEventListener(new SolverEventListener() {
 			public void bestSolutionChanged(BestSolutionChangedEvent event) {
 				synchronized (solution) {
 					solution = (Timetable) event.getNewBestSolution();
 				}
 				
 				if (solverState == SolverState.INITIALIZING) {
 					solverInitialized();
 				}
 			}
 		});
 	}
 	
 	public void solve() {
 		solverInitializing();
 
 		// as expected, the interface locks up due to this
 		// method not returning since the solver is working
 		solverTask = poolManager.submitSolver(new Runnable() {
 			public void run() {
 				solver.solve();
 				synchronized (solution) {
 					solution = (Timetable) solver.getBestSolution();
 				}
 				
 				solverFinished();
 				
 				log.info("Solver has finished.");
 			}
 		});
 		
 		log.info("Available threads: " + poolManager.getAvailableThreadCount());
 	}
 	
 	public void terminateEarly() {
 		if (solver != null && solver.isSolving()) {
 			solver.terminateEarly();
 		}
 		
 		if (solverTask != null) {
 			try {
 				solverTask.get();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 			
 			solverTask.cancel(false);
 		}
 		
 		solverTerminated();
 	}
 	
 	public void save() {
 		if (getSolution() != null && (solverState == SolverState.TERMINATED || solverState == SolverState.FINISHED_UNSAVED)) {
 			getSolution().setSchool(user.getSchool());
 			for (SchoolDay schoolDay : getSolution().getSchoolDays()) {
 				em.persist(schoolDay);
 			}
 
 			for (SchoolHour schoolHour : getSolution().getSchoolHours()) {
 				em.persist(schoolHour);
 			}
 
 			for (Period period : getSolution().getPeriods()) {
 				em.persist(period);
 			}
 
 			for (SchoolClass schoolClass : getSolution().getSchoolClasses()) {
 				em.persist(schoolClass);
 			}
 
 			em.persist(getSolution());
 			em.flush();
 			solverState = SolverState.FINISHED_SAVED;
 			
 			solutionSaved();
 		}
 	}
 	
 	public void clearWorkingSolution() {
 		if (solverState == SolverState.TERMINATED || solverState == SolverState.FINISHED_UNSAVED) {
 			poolManager.removeLock(user.getSchool());
 		}
 	}
 
 	public Solver createSolver() {
 		XmlSolverFactory configurer = new XmlSolverFactory();
 		configurer.configure(SOLVER_CONFIG);
 		return configurer.buildSolver();
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	private Timetable createSolution() {
 		Timetable timetable = new Timetable();
 
 		// Get the actual data from the database
 		List<Grade> grades = em.createQuery("select g from Grade g where g.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 		List<GradeSubject> gradeSubjects = em.createQuery("select gs from GradeSubject gs where gs.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 		List<StudentGroup> studentGroups = em.createQuery("select sg from StudentGroup sg where sg.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 		List<Teacher> teachers = em.createQuery("select t from Teacher t where t.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 		List<PeriodOffRequest> periodOffRequests = em
 				.createQuery("select por from PeriodOffRequest por where por.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 		List<Subject> subjects = em.createQuery("select s from Subject s where s.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 		List<Room> rooms = em.createQuery("select r from Room r where r.school = :school")
 				.setParameter("school", user.getSchool()).getResultList();
 
 		List<SchoolDay> schoolDays = new ArrayList<SchoolDay>();
 		List<SchoolHour> schoolHours = new ArrayList<SchoolHour>();
 		List<Period> periods = new ArrayList<Period>();
 		List<SchoolClass> schoolClasses = new ArrayList<SchoolClass>();
 
 		for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
 			SchoolDay schoolDay = new SchoolDay();
 			schoolDay.setDayOfWeek(dayOfWeek);
 			schoolDay.setMinAllowedWeight(3);
 			schoolDay.setMaxAllowedWeight(140);
 			schoolDay.setSchool(user.getSchool());
 
 			schoolDays.add(schoolDay);
 		}
 
 		for (int i = 1; i <= 8; i++) {
 			SchoolHour schoolHour = new SchoolHour();
 			schoolHour.setActualStartTime(i);
 			schoolHour.setSchool(user.getSchool());
 
 			schoolHours.add(schoolHour);
 		}
 
 		for (SchoolDay schoolDay : schoolDays) {
 			for (SchoolHour schoolHour : schoolHours) {
 				Period period = new Period();
 				period.setSchoolDay(schoolDay);
 				period.setSchoolHour(schoolHour);
 				period.setSchool(user.getSchool());
 
 				periods.add(period);
 			}
 		}
 
 		for (StudentGroup studentGroup : studentGroups) {
 			Grade grade = studentGroup.getGrade();
 
 			Integer classCount = grade.getClassCountForSubjectWithTeacher(studentGroup.getSubject(),
 					studentGroup.getTeacher());
 			for (int i = 0; i < classCount; i++) {
 				SchoolClass schoolClass = new SchoolClass();
 				schoolClass.setStudentGroup(studentGroup);
 				schoolClass.setSchool(user.getSchool());
 				// Period and room for each school class are added
 				// accordingly by the construction heuristic of Drools Planner
 
 				schoolClasses.add(schoolClass);
 			}
 		}
 
 		timetable.setGrades(grades);
 		timetable.setTeachers(teachers);
 		timetable.setSubjects(subjects);
 		timetable.setRooms(rooms);
 		timetable.setGradeSubjects(gradeSubjects);
 		timetable.setStudentGroups(studentGroups);
 		timetable.setSchoolDays(schoolDays);
 		timetable.setSchoolHours(schoolHours);
 		timetable.setPeriods(periods);
 		timetable.setPeriodOffRequests(periodOffRequests);
 		timetable.setSchoolClasses(schoolClasses);
 		return timetable;
 	}
 	
 	private void solverStarting() {
 		poolManager.addLock(user.getSchool());
 		solverState = SolverState.NEW;
 	}
 	
 	private void solverInitializing() {
 		solverState = SolverState.INITIALIZING;
 	}
 	
 	private void solverInitialized() {
 		solverState = SolverState.SOLVING;
 	}
 	
 	private void solverTerminated() {
 		if (solverState == SolverState.INITIALIZING) {
 			poolManager.removeLock(user.getSchool());
 			solution = null;
 			solverState = SolverState.NEW;
 		} else if (solverState == SolverState.SOLVING) {
 			solverState = SolverState.TERMINATED;
 		}
 	}
 	
 	private void solverFinished() {
 		if (solverState == SolverState.SOLVING) {
 			solverState = SolverState.FINISHED_UNSAVED;
 		}
 	}
 	
 	private void solutionSaved() {
 		if (solverState == SolverState.FINISHED_SAVED) {
 			poolManager.removeLock(user.getSchool());
 			solution = null;
 		}
 	}
 	
 	public SolverState getSolverState() {
 		return solverState;
 	}
 
 	public void setSolverState(SolverState solverState) {
 		this.solverState = solverState;
 	}
 
 	public Solver getSolver() {
 		return solver;
 	}
 
 	public Timetable getSolution() {
 		return solution;
 	}
 	
 	public Boolean hasAvailableThreads() {
 		return poolManager.getAvailableThreadCount() > 0;
 	}

 }
