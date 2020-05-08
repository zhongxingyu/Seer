 package ss.gui.out;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import ss.api.Iteration;
 import ss.api.Project;
 import ss.api.Simulator;
 import ss.apiImpl.SimulatorImpl;
 
 import com.google.common.collect.Lists;
 
 /**
  * This class is the one in charge of building the frame for the graphic
  * interface
  */
 @SuppressWarnings("serial")
 public class Frame extends JFrame {
 	private static final int CELL_SIZE = 35;
 	private int rows = 25;
 	private int cols = 40;
 	private JPanel simulationPanel;
 	private Sprite sprite;
 	private JTextArea idleProgrammers;
 	private JTextArea finishedProjects;
 	private JTextArea totalTime;
 	private Simulator simulator;
 	private List<ProjectTimes> projectEstimationTimes = new ArrayList<>();
 	private List<ProjectTimes> projectDurationTimes = new ArrayList<>();
 	private List<ProjectTimes> projectCosts = new ArrayList<>();
 	private List<ProjectTimes> iterations = new ArrayList<>();
 	private List<ProjectTimes> programmers = new ArrayList<>();
 	private List<Color> colors = Lists.newArrayList();
 	private int INITIALVALUESTOP = 10;
 	private int FINAVALUETOP = 30;
 
 	public Frame() throws IOException {
 		this.simulator = new SimulatorImpl();
 
 		// Initializes MenuBar
 		MenuBar menuBar = new MenuBar(this);
 		setJMenuBar(menuBar.getMenu());
 
 		// Initializes basic Frame and MapHash of images
 		setTitle("Simulador: Distribución de programadores en una Software Factory");
 		setLayout(null);
 		// setIconImage(ImageUtils.loadImage("resources/images/ss.jpg"));
 		initializeFrame();
 		initColors();
 	}
 
 	private void initColors() {
 		colors.add(Color.BLACK);
 		colors.add(Color.BLUE);
 		colors.add(Color.DARK_GRAY);
 		colors.add(Color.GRAY);
 		colors.add(Color.LIGHT_GRAY);
 		colors.add(Color.GREEN);
 		colors.add(Color.MAGENTA);
 		colors.add(Color.ORANGE);
 		colors.add(Color.PINK);
 		colors.add(Color.RED);
 
 	}
 
 	/**
 	 * Returns the player's sprite
 	 * 
 	 * @return Sprite
 	 */
 	public Sprite getPlayerSprite() {
 		return this.sprite;
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 	}
 
 
 	/*
 	 * Initializes the Frame according to a column and a row and positions it in
 	 * the center of the screan
 	 */
 	private void initializeFrame() {
 		setSize(cols * CELL_SIZE - 54, rows * CELL_SIZE - 20);
 		// Dimension screan = Toolkit.getDefaultToolkit().getScreenSize();
 		// Dimension window = this.getSize();
 		// setLocation((screan.width - window.width) / 2,
 		// (screan.height - window.height) / 2);
 		paintAll(this.getGraphics());
 	}
 
 	private class ProjectTimes {
 		JTextArea area;
 		Project project;
 
 		public ProjectTimes(JTextArea area, Project project) {
 			this.area = area;
 			this.project = project;
 		}
 
 		public Project getProject() {
 			return project;
 		}
 
 		public JTextArea getArea() {
 			return area;
 		}
 	}
 
 	/**
 	 * Initializes the graphic panel with sprites and if already existed one,
 	 * first removes it in order to refresh
 	 * 
 	 * @throws IOException
 	 *             If there was a problem loading the image
 	 */
 	public void initializePanel() throws IOException {
 		if (existsPanel()) {
 			projectEstimationTimes.clear();
 			projectDurationTimes.clear();
 			projectCosts.clear();
 			iterations.clear();
			programmers.clear();
 			remove(simulationPanel);
 		}
 		simulationPanel = new SimulationPanel();
 		simulationPanel.setSize(cols * CELL_SIZE, rows * CELL_SIZE);
 		simulationPanel.setLayout(null);
 		simulationPanel.setBackground(Color.WHITE);
 		add(simulationPanel);
 
 		JTextArea area = new JTextArea("Programadores ociosos: ");
 		area.setBounds(50, INITIALVALUESTOP, 150, 20);
 		simulationPanel.add(area);
 		idleProgrammers = new JTextArea(String.valueOf(simulator
 				.getIdleProgrammers()));
 		idleProgrammers.setBounds(210, INITIALVALUESTOP, 30, 20);
 		simulationPanel.add(idleProgrammers);
 
 		area = new JTextArea("Tiempo de simulación: ");
 		area.setBounds(270, INITIALVALUESTOP, 150, 20);
 		simulationPanel.add(area);
 
 		totalTime = new JTextArea("0");
 		totalTime.setBounds(420, INITIALVALUESTOP, 30, 20);
 		simulationPanel.add(totalTime);
 
 		area = new JTextArea("Tiempo máximo de simulación: ");
 		area.setBounds(480, INITIALVALUESTOP, 200, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea(String.valueOf(simulator.getSimulationDays()));
 		area.setBounds(690, INITIALVALUESTOP, 40, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Cantidad proyectos: ");
 		area.setBounds(50, FINAVALUETOP, 150, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea(String.valueOf(simulator.getProjects().size()));
 		area.setBounds(210, FINAVALUETOP, 30, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Proyectos terminados: ");
 		area.setBounds(270, FINAVALUETOP, 150, 20);
 		simulationPanel.add(area);
 
 		// titulos de las columnas
 		area = new JTextArea("Proyecto");
 		area.setBounds(50, FINAVALUETOP + 30, 70, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Duración");
 		area.setBounds(150, FINAVALUETOP + 30, 150, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Estimación");
 		area.setBounds(300, FINAVALUETOP + 30, 150, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Costo permitido");
 		area.setBounds(470, FINAVALUETOP + 30, 150, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Costo actual");
 		area.setBounds(690, FINAVALUETOP + 30, 150, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Iteraciones");
 		area.setBounds(850, FINAVALUETOP + 30, 150, 20);
 		simulationPanel.add(area);
 
 		area = new JTextArea("Programadores");
 		area.setBounds(1000, FINAVALUETOP + 30, 150, 20);
 		simulationPanel.add(area);
 
 		finishedProjects = new JTextArea("0");
 		finishedProjects.setBounds(420, FINAVALUETOP, 30, 20);
 		simulationPanel.add(finishedProjects);
 		initializeProjects(simulator.getProjects());
 	}
 
 	private void initializeProjects(List<Project> projects) {
 		for (Project project : projects) {
 			int id = project.getId();
 
			final JTextArea projectName = new JTextArea("Proyecto "
 					+ String.valueOf(id));
 			int margin = 40;
 			int height = 25;
 			int addition = 90;
 
 			projectName.setBounds(50, margin * id + addition, 100, height);
 			simulationPanel.add(projectName);
 			Iteration iteration = project.getCurrentIteration();
 			JTextArea duration = new JTextArea(String.valueOf(iteration
 					.getDuration()));
 			duration.setBounds(170, margin * id + addition, 40, height);
 			simulationPanel.add(duration);
 
 			JTextArea estimation = new JTextArea("0");
 			estimation.setBounds(330, margin * id + addition, 40, height);
 			simulationPanel.add(estimation);
 
 			JTextArea permittedCost = new JTextArea(String.valueOf(project
 					.getMaxCost()));
 			permittedCost.setBounds(520, margin * id + addition, 40, height);
 			simulationPanel.add(permittedCost);
 
 			JTextArea actualCost = new JTextArea("0");
 			actualCost.setBounds(730, margin * id + addition, 40, height);
 			simulationPanel.add(actualCost);
 
 			JTextArea iterationsQty = new JTextArea("0");
 			iterationsQty.setBounds(870, margin * id + addition, 10, height);
 			simulationPanel.add(iterationsQty);
 			JTextArea iterationsTotal = new JTextArea("/ "
 					+ String.valueOf(project.getIterationsQty()));
 			iterationsTotal.setBounds(880, margin * id + addition, 20, height);
 			simulationPanel.add(iterationsTotal);
 
 			JTextArea programmersQty = new JTextArea("0");
 			programmersQty.setBounds(1040, margin * id + addition, 20, height);
 			simulationPanel.add(programmersQty);
 
 			projectEstimationTimes.add(new ProjectTimes(estimation, project));
 			projectDurationTimes.add(new ProjectTimes(duration, project));
 			projectCosts.add(new ProjectTimes(actualCost, project));
 			iterations.add(new ProjectTimes(iterationsQty, project));
 			programmers.add(new ProjectTimes(programmersQty, project));
 		}
 	}
 
 	/**
 	 * Returns true if already existed a Panel and false if not.
 	 * 
 	 * @return boolean
 	 */
 	public boolean existsPanel() {
 		return simulationPanel != null;
 	}
 
 	public void restart() {
 		repaint();
 
 		try {
 			initializePanel();
 		} catch (IOException e) {
 			errorWhileReading("restart of simulation.");
 		}
 		initializeFrame();
 
 	}
 
 	/**
 	 * Creates a panel of error with a message starting in "Error at ".
 	 * 
 	 * @param string
 	 */
 	public void errorWhileReading(String string) {
 		JOptionPane.showMessageDialog(null, "Error at " + string,
 				"Simulator Error", JOptionPane.ERROR_MESSAGE);
 		System.exit(1);
 	}
 
 	public void updateIdleProgrammers(int qty) {
 		idleProgrammers.setText(String.valueOf(qty));
 
 	}
 
 	public void updateTime(int time) {
 		totalTime.setText(String.valueOf(time));
 
 	}
 
 	public void updateFinishedProjects(int qty) {
 		finishedProjects.setText(String.valueOf(qty));
 	}
 
 	public Simulator getSimulator() {
 		return simulator;
 	}
 
 	public void updateIterationEstimate(Project project) {
 		for (ProjectTimes projectTime : projectEstimationTimes) {
 			if (projectTime.getProject().equals(project)) {
 				if (project.getCurrentIteration().getEstimate() == Integer.MAX_VALUE) {
 					projectTime.getArea().setText("-");
 				} else {
 					projectTime.getArea().setText(
 							String.valueOf(project.getCurrentIteration()
 									.getEstimate()));
 				}
 				return;
 			}
 
 		}
 
 	}
 
 	/**
 	 * Returns the actualLevel
 	 * 
 	 * @return Level
 	 */
 
 	public void updateIterationDuration(Project project) {
 		for (ProjectTimes projectTime : projectDurationTimes) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText(
 						String.valueOf(project.getCurrentIteration()
 								.getDuration()));
 				break;
 			}
 
 		}
 
 		for (ProjectTimes projectTime : iterations) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText(
 						String.valueOf(project.getIterationsLeft()));
 				return;
 			}
 
 		}
 
 	}
 
 	public void updateProgrammersQty(Project project) {
 		for (ProjectTimes projectTime : programmers) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText(
 						String.valueOf(project.getProgrammersWorking()));
 				return;
 			}
 
 		}
 	}
 
 	public void updateCost(Project project) {
 		for (ProjectTimes projectTime : projectCosts) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText(
 						String.valueOf(project.getTotalCost()));
 				return;
 			}
 
 		}
 
 	}
 
 	private class SimulationPanel extends JPanel {
 		@Override
 		public void paint(Graphics g) {
 			// TODO Auto-generated method stub
 			super.paint(g);
 			for (ProjectTimes projectTime : programmers) {
 				Project project = projectTime.project;
 				int x = projectTime.getArea().getX();
 				int y = projectTime.getArea().getY();
 				if (project.finished()) {
 					try {
 						Image image = ImageIO.read(new File(
 								"resources/images/tick.png"));
 						g.drawImage(image, x + 140, y - 8, null);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				} else {
 					try {
 						Image image = ImageIO.read(new File(
 								"resources/images/cross.png"));
 						g.drawImage(image, x + 140, y - 10, null);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 
 	public void addProject(Project project) {
 		List<Project> projects = new ArrayList<>();
 		projects.add(project);
 		initializeProjects(projects);
 		
 
 	}
 
 	public void finishProject(Project project) {
 		for (ProjectTimes projectTime : projectDurationTimes) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText("0");
 				break;
 			}
 
 		}
 		
 		for (ProjectTimes projectTime : projectEstimationTimes) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText("0");
 				break;
 			}
 
 		}
 		
 		for (ProjectTimes projectTime : programmers) {
 			if (projectTime.getProject().equals(project)) {
 				projectTime.getArea().setText("0");
 				return;
 			}
 
 		}
 		
 	}
 
 }
