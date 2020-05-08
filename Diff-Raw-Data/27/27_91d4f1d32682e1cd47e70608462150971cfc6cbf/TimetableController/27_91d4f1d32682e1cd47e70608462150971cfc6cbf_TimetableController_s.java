 package com.rakursy.timetable.controller;
 
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.select;
 import static org.hamcrest.Matchers.equalTo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
import javax.annotation.PreDestroy;
 import javax.ejb.Stateful;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.drools.planner.core.Solver;
 import org.drools.planner.core.score.buildin.hardandsoft.DefaultHardAndSoftScore;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.jboss.logging.Logger;
 import org.jboss.seam.security.annotations.LoggedIn;
 import org.jboss.solder.logging.Category;
 
 import com.rakursy.timetable.controller.SolverManager.SolverState;
 import com.rakursy.timetable.model.Grade;
 import com.rakursy.timetable.model.Period;
 import com.rakursy.timetable.model.SchoolClass;
 import com.rakursy.timetable.model.SchoolDay;
 import com.rakursy.timetable.model.Timetable;
 
 @Stateful
 @Named
 @SessionScoped
 @LoggedIn
 public class TimetableController {
 	
 	@Inject
 	@Category("timetable")
 	private Logger log;
 
 	@Inject
 	private SolverManager solverManager;
 
 	private Map<SchoolDay, List<Period>> periodsForSchoolDays = new HashMap<SchoolDay, List<Period>>();
 
 	public Solver getSolver() {
 		return solverManager.getSolver();
 	}
 
 	@Produces
 	@Named("workingSolution")
 	public Timetable getSolution() {
 		return solverManager.getSolution();
 	}
 	
 	public Boolean getSolving() {
 		return solverManager.getSolverState() == SolverState.INITIALIZING 
 				|| solverManager.getSolverState() == SolverState.SOLVING;
 	}
 
 	public HardAndSoftScore getScore() {
 		if (getSolution() == null || getSolution().getScore() == null) {
 			return DefaultHardAndSoftScore.parseScore("-999999hard/-999999soft");
 		}
 		return (HardAndSoftScore) getSolution().getScore();
 	}
 
 	public Map<SchoolDay, List<Period>> getPeriodsForSchoolDays() {
 		return periodsForSchoolDays;
 	}
 
 	public List<SchoolClass> getSchoolClassForTimeAndGrade(Period period, Grade grade) {
 		List<SchoolClass> schoolClasses = select(getSolution().getSchoolClasses(),
 				having(on(SchoolClass.class).getPeriod(), equalTo(period))
 						.and(having(on(SchoolClass.class).getStudentGroup().getGrade(), equalTo(grade))));
 		if (schoolClasses.isEmpty())
 			return null;
 		return schoolClasses;
 	}
 
 	public void generatePeriodsForSchoolDays() {
 		Map<SchoolDay, List<Period>> periodsForSchoolDays = new HashMap<SchoolDay, List<Period>>();
 		for (SchoolDay schoolDay : getSolution().getSchoolDays()) {
 			List<Period> periods = select(getSolution().getPeriods(),
 					having(on(Period.class).getSchoolDay(), equalTo(schoolDay)));
 			periodsForSchoolDays.put(schoolDay, periods);
 		}
 		this.periodsForSchoolDays = periodsForSchoolDays;
 	}
 
 	public void solve() {
 		clearWorkingSolution();
 
 		solverManager.initializeSolver();
 		
 		log.info("Starting solver " + getSolver().getClass().getSimpleName() + " with solution "
 				+ getSolution().getClass().getSimpleName());
 
 		if (!solverManager.hasAvailableThreads()) {
 			FacesContext context = FacesContext.getCurrentInstance();
 			ResourceBundle bundle = context.getApplication().getResourceBundle(context, "messages");
 			context.addMessage(null, new FacesMessage(bundle.getString("noAvailableThreads")));
 		}
 		
 		solverManager.solve();
 		
 		generatePeriodsForSchoolDays();
 	}
 
 	public void terminateEarly() {
 		solverManager.terminateEarly();
 	}
 
 
 	public Boolean save() {
 		log.info("Saving solution");
 		solverManager.save();
 		
 		log.info("Saved.");
 		return true;
 	}
 
 	private void clearWorkingSolution() {
		terminateEarly();
 		solverManager.clearWorkingSolution();
 	}
 
	@PreDestroy
	public void onPreDestroy() {
		log.info("Session is getting destroyed. Stopping any possible leftover Solver.");
		clearWorkingSolution();
	}

 }
