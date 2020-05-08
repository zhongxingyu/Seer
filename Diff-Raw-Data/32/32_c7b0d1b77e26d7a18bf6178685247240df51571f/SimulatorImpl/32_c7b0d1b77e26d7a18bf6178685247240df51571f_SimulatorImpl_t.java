 package ss.apiImpl;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import ss.api.Iteration;
 import ss.api.Project;
 import ss.api.ReasignationStrategy;
 import ss.api.Simulator;
 
 public class SimulatorImpl implements Simulator {
 
 	private int simulationDays;
 	private List<Project> projects;
 	private int idleProgrammers;
 	private ReasignationStrategy strategy;
 
 	public SimulatorImpl(int simulationDays, List<Project> projects,
 			int programmersQty, ReasignationStrategy strategy) {
 		this.simulationDays = simulationDays;
 		this.projects = projects;
 		this.idleProgrammers = programmersQty;
 		this.strategy = strategy;
 	}
 
 	public void addProject(Project project) {
 	}
 
 	public void start() {
 		int today = 0;
 		int projectsFinished = 0;
 		int totalProjects = projects.size();
 		boolean finished = false;
 		while (today < simulationDays && !finished) {
 			Collections.sort(projects, new ProjectComparator());
 			Iterator<Project> it = projects.iterator();
 			while (it.hasNext()) {
 				Project project = it.next();
 				if (!project.finished()) {
 					Iteration iteration = project.getCurrentIteration();
					if (!iteration.finished()) {
						if (iteration.isDelayed()) {
							idleProgrammers = strategy.reasing(project,
									projects, idleProgrammers);
						}
						if (iteration.getProgrammersWorking() > 0) {
							iteration.decreaseLastingDays();
						}
					} else {
 						project.nextIteration();
 					}
 				} else {
 					it.remove();
 					projectsFinished++;
 				}
 			}
 			today++;
 			if (projectsFinished == totalProjects) {
 				finished = true;
 			}
 		}
 		System.out.println("Cantidad proyectos originales: " + totalProjects);
 		System.out.println("Proyectos terminados: " + projectsFinished);
 		System.out.println("Dias de desarrollo: " + today);
 
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
 				return 1;
 			} else if (!delayedP1 && delayedP2) {
 				return -1;
 			} else {
 				return diffP1 - diffP2;
 			}
 
 		}
 	}
 
 	@Override
 	public String toString() {
 		return "Simulator simulationDays: " + simulationDays + " projectsQty: "
 				+ projects.size() + " idleProgrammers: " + idleProgrammers;
 	}
 
 }
