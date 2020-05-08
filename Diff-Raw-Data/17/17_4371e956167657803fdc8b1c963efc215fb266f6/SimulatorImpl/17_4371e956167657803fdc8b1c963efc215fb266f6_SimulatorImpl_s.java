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
 
 import org.jfree.ui.RefineryUtilities;
 
 import ss.api.Iteration;
 import ss.api.Project;
 import ss.api.ReasignationStrategy;
 import ss.api.Simulator;
 import ss.apiImpl.charts.BoxStrategiesChart;
 import ss.apiImpl.charts.RealTimePlotter;
 import ss.gui.SimulationListener;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class SimulatorImpl implements Simulator {
 
 	private int simulationDays;
 	private List<Project> projects;
 	private int idleProgrammers;
 	private ReasignationStrategy strategy;
 	private SimulationListener listener;
 	private Map<String, LinkedList<BackupItem>> finishedProjects = new HashMap<>();
 	private RealTimePlotter plotter;
	private int INV_MAX = 60;
	private int INV_MIN = 30;
 
 	public static final int MAX_PROGRAMMER_PER_PROJECT = 8;
 
 	public void build(SimulationListener listener, int strategy,
 			RealTimePlotter plotter) {
 		this.listener = listener;
 		this.projects = buildProjects(5);
 		this.idleProgrammers = 15;
 		this.simulationDays = 365; // In days
 		this.strategy = assignStrategy(strategy);
 		this.plotter = plotter;
 	}
 
 	public void start(int totalTimes) {
 		// Build method must be called before
 
 		LinkedList<Map<String, LinkedList<BackupItem>>> backupsList = new LinkedList<>();
 		int boxTimes = 3;
 		while (boxTimes-- > 0) {
 			int totalProjects = projects.size();
 			int projectsId = totalProjects;
 			plotter.setProjects(projects);
 			int strategiesFinished = 0;
 			int times = totalTimes;
 			finishedProjects = Maps.newHashMap();
 			strategy = assignStrategy(0);
 			while (--times >= 0 && strategiesFinished < 3) {
 				int today = 0;
 				int projectsFinished = 0;
 				int totalCost = 0;
 				totalProjects = projects.size();
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
 									if (strategy.isFreelanceStrategy()) {
 										project.decreaseInvestment();
 									}
 									idleProgrammers -= strategy.reasing(
 											project, projects, idleProgrammers);
 									listener.updateWorkingProgrammers(project);
 									listener.updateIdleProgrammers(idleProgrammers);
 									listener.updateIterationEstimate(project);
 									listener.updateInvestment(project);
 								}
 								if (project.getProgrammersWorking() > 0) {
 									iteration.decreaseLastingDays();
 								}
 							} else {
 								if (strategy.isFreelanceStrategy()) {
 									project.removeFreelanceProgrammers();
 								}
 								// If iteration has pending days, adds them to
 								// the
 								// next iteration
 								int extraTime = iteration.getEstimate()
 										- iteration.getDuration();
 								extraTime = (extraTime > 0) ? extraTime : 0;
 								if (project.nextIteration(extraTime)) {
 									listener.finishProject(project);
 								} else {
 									listener.updateIterationDuration(project);
 								}
 
 								// If idle strategy only, programmers must be
 								// released in every iteration
 								if (strategy.isIdleStrategy()
 										&& !strategy.isFreelanceStrategy()
 										&& !strategy.isSwitchStrategy()) {
 									idleProgrammers += project
 											.removeProgrammers();
 									listener.updateIdleProgrammers(idleProgrammers);
 								}
 
 							}
 						} else {
 							projectIterator.remove();
 							plotter.removeProject(project);
 							totalCost += project.getTotalCost();
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
 						// if(p.getDuration() > (simulationDays - today)) {
 						projects.add(p);
 						totalProjects++;
 						listener.addProject(p);
 						plotter.addProject(p);
 						// }
 					}
 					today++;
 					listener.updateTime(today);
 					plotter.updateTime();
 					if ((today == simulationDays)) {
 						finishedProjects.get(strategy.getStrategy()).add(
 								new BackupItem(projectsFinished, totalCost,
 										totalProjects));
 					}
 				}
 				if (times == 0 && ++strategiesFinished < 3) {
 					// strategiesFinished es equal to the strategy id
 					strategy = assignStrategy(strategiesFinished);
 					times = totalTimes;
 				}
 
 				build(listener, strategy.getStrategyID(), plotter);
 				projectsId = projects.size();
 				plotter = plotter.newInstance(projects);
 				listener.reset();
 			}
 			backupsList.add(finishedProjects);
 			finishedProjects = new HashMap<>();
			INV_MAX *= Math.exp(3 - boxTimes);
			INV_MIN *= Math.exp(3 - boxTimes);
 		}
 		BoxStrategiesChart demo = new BoxStrategiesChart(backupsList);
 		demo.pack();
 		RefineryUtilities.centerFrameOnScreen(demo);
 		demo.setVisible(true);
 
 	}
 
 	private ReasignationStrategyImpl assignStrategy(int strategy) {
 		switch (strategy) {
 		case 0:
 			if (finishedProjects.get("available") == null) {
 				finishedProjects.put("available", new LinkedList<BackupItem>());
 			}
 			return new ReasignationStrategyImpl(true, false, false);
 		case 1:
 			if (finishedProjects.get("switch") == null) {
 				finishedProjects.put("switch", new LinkedList<BackupItem>());
 			}
 			return new ReasignationStrategyImpl(true, true, false);
 		default: // Case 2
 			if (finishedProjects.get("freelance") == null) {
 				finishedProjects.put("freelance", new LinkedList<BackupItem>());
 			}
 			return new ReasignationStrategyImpl(true, true, true);
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
 		int iterationsQty = r.nextInt(6) + 1; // (0,6]
 		for (int j = 0; j < iterationsQty; j++) {
 			int duration = r.nextInt(22 - 17) + 17; // (17,22]
 			Iteration it = new IterationImpl(IssueFactory.createBackendIssue(),
 					IssueFactory.createFrontEndIssue(), duration);
 			iterations.push(it);
 		}
 		int maxInvestment = r.nextInt(INV_MAX - INV_MIN) + INV_MIN; // (COST_MIN,
 																	// COST_MAX]
 		return new ProjectImpl(iterations, maxInvestment, id);
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
 
 	@Override
 	public int getStrategyID() {
 		if (strategy == null) {
 			return 0;
 		}
 		return strategy.getStrategyID();
 	}
 
 }
