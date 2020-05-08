 package ss.apiImpl;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import ss.api.Iteration;
 import ss.api.Project;
 import ss.api.ReasignationStrategy;
 import ss.api.Simulator;
import ss.apiImpl.charts.StrategiesChart;
 import ss.apiImpl.strategies.ReasignationStrategyImpl;
 import ss.gui.out.SimulationListener;
 
 import com.google.common.collect.Lists;
 
 public class SimulatorImpl implements Simulator {
 
 	private int simulationDays;
 	private List<Project> projects;
 	private int idleProgrammers;
 	private ReasignationStrategy strategy;
 	private SimulationListener listener;
 	private Map<String, LinkedList<Integer>> finishedProjects = new HashMap<>();
 
 	public void build(SimulationListener listener, int strategy) {
 		this.listener = listener;
 		this.projects = buildProjects(5);
 		this.idleProgrammers = 12;
 		this.simulationDays = 365; // In days
 		this.strategy = assignStrategy(strategy);
 	}
 
 	public void start(int totalTimes) {
 		// Build method must be called before
 
 		int totalProjects = projects.size();
 		int projectsId = totalProjects;
 		finishedProjects.put("idle", new LinkedList<Integer>());
 //		StrategiesChart chart = new StrategiesChart(projects);
 //		chart.start();
 
 		while (--totalTimes >= 0) {
 			int today = 0;
 			int projectsFinished = 0;
 			while (today < simulationDays) {
 				Collections.sort(projects, new ProjectComparator());
 				Iterator<Project> projectIterator = projects.iterator();
 				int projectsQty = projects.size();
 				while (projectIterator.hasNext()) {
 					Project project = projectIterator.next();
 					if (!project.finished()) {
 						Iteration iteration = project.getCurrentIteration();
 						if (!iteration.finished()) {
 							if (iteration.isDelayed()) {
 								idleProgrammers -= strategy.reasing(project,
 										projects, idleProgrammers);
 								listener.updateWorkingProgrammers(project);
 								listener.updateIdleProgrammers(idleProgrammers);
 								listener.updateIterationEstimate(project);
 							}
 							if (project.getProgrammersWorking() > 0) {
 								iteration.decreaseLastingDays();
 							}
 						} else {
 
 							// If iteration has pending days, adds them to the
 							// next iteration
 							int extraTime = iteration.getEstimate()
 									- iteration.getDuration();
 							extraTime = (extraTime > 0) ? extraTime : 0;
 							if(project.nextIteration(extraTime)){
 								listener.finishProject(project);
 							}else{
 								listener.updateIterationDuration(project);
 							}
 
 							// If idle strategy only, programmers must be
 							// released in every iteration
 							if (strategy.isIdleStrategy()
 									&& !strategy.isFreelanceStrategy()
 									&& !strategy.isSwitchStrategy()) {
 								idleProgrammers += project.removeProgrammers();
 								listener.updateIdleProgrammers(idleProgrammers);
 							}
 
 						}
 					} else {
 						projectIterator.remove();
 //						chart.removeProject(project);
 						idleProgrammers += project.removeProgrammers();
 						listener.updateIdleProgrammers(idleProgrammers);
 						projectsFinished++;
 						listener.updateFinishedProjects(projectsFinished);
 					}
 				}
 
 				// If a project was removed, a new one must be created
 				int newProjectsQty = projects.size();
 				int diff = projectsQty - newProjectsQty;
 				for (int i = 0; i < diff; i++) {
 					Project p = buildProject(projectsId++);
					projects.add(buildProject(projectsId));
 					listener.addProject(p);
 //					chart.addProject(p);
 				}
 				today++;
 				listener.updateTime(today);
 //				chart.updateTime();
 				if ((today == simulationDays)) {
 					finishedProjects.get(strategy.getStrategy()).push(
 							projectsFinished);
 				}
 			}
 			build(listener, strategy.getStrategyID());
 			// chart.restart(projects);
 //			chart = new StrategiesChart(projects);
			listener.reset();
 		}
 
 	}
 
 	private ReasignationStrategyImpl assignStrategy(int strategy) {
 		switch (strategy) {
 		case 0:
 			return new ReasignationStrategyImpl(true, false, false, listener);
 		case 1:
 			finishedProjects.put("switch", new LinkedList<Integer>());
 			return new ReasignationStrategyImpl(true, true, false, listener);
 		default: // Case 2
 			finishedProjects.put("freelance", new LinkedList<Integer>());
 			return new ReasignationStrategyImpl(true, true, true, listener);
 		}
 	}
 
 	private class ProjectComparator implements Comparator<Project> {
 		@Override
 		public int compare(Project p1, Project p2) {
 			int diffP1 = p1.getCurrentIteration().getDuration()
 					- p1.getCurrentIteration().getEstimate();
 			int diffP2 = p2.getCurrentIteration().getDuration()
 					- p2.getCurrentIteration().getEstimate();
 			boolean delayedP1 = p1.getCurrentIteration().isDelayed();
 			boolean delayedP2 = p2.getCurrentIteration().isDelayed();
 			if (delayedP1 && !delayedP2) {
 				return -1;
 			} else if (!delayedP1 && delayedP2) {
 				return 1;
 			} else {
 				if ((diffP1 <= 0 && diffP2 <= 0)
 						|| (diffP1 >= 0 && diffP2 >= 0)) {
 					if (diffP1 < diffP2) {
 						return -1;
 					} else if (diffP1 > diffP2) {
 						return 1;
 					} else {
 						return 0;
 					}
 				}
 				System.out.println("Shouldn't happen");
 				return 0;
 			}
 
 		}
 	}
 
 	/**
 	 * Builds qty different proyects
 	 * 
 	 * @param qty
 	 * @return
 	 */
 	private List<Project> buildProjects(int qty) {
 		List<Project> retList = Lists.newArrayList();
 		for (int i = 0; i < qty; i++) {
 			retList.add(buildProject(i));
 		}
 		return retList;
 
 	}
 
 	/**
 	 * Build proyect with random iterations and durations
 	 * 
 	 * @param id
 	 * @return
 	 */
 	private Project buildProject(int id) {
 		Deque<ss.api.Iteration> iterations = new LinkedBlockingDeque<>();
 		Random r = new Random();
 		int iterationsQty = r.nextInt(7) + 1; // (0,7]
 		for (int j = 0; j < iterationsQty; j++) {
 			int duration = r.nextInt(27 - 20) + 20; // (20,27]
 			Iteration it = new IterationImpl(IssueFactory.createBackendIssue(),
 					IssueFactory.createFrontEndIssue(), duration);
 			iterations.push(it);
 		}
 		int maxCost = r.nextInt(10 - 5) + 5; // (5,10]
 		return new ProjectImpl(iterations, maxCost, id);
 	}
 
 	@Override
 	public int getIdleProgrammers() {
 		return idleProgrammers;
 	}
 
 	@Override
 	public List<Project> getProjects() {
 		return this.projects;
 	}
 
 	@Override
 	public int getSimulationDays() {
 		return simulationDays;
 	}
 
 	@Override
 	public String toString() {
 		return "Simulator simulationDays: " + simulationDays + " projectsQty: "
 				+ projects.size() + " idleProgrammers: " + idleProgrammers;
 	}
 
 }
