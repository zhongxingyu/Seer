 package de.unihalle.sim.main;
 
 import java.util.List;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.mitre.sim.Simulation;
 
 import com.google.common.collect.Lists;
 
 import de.unihalle.sim.entities.BeeHive;
 import de.unihalle.sim.entities.Flower;
 import de.unihalle.sim.entities.Meadow;
 import de.unihalle.sim.entities.PositionedEntity;
 import de.unihalle.sim.util.Position;
 import de.unihalle.sim.util.TimeUtil;
 
 public class BeeSimulation extends Simulation {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int SIMULATION_PACE = 100;
 	private static final double SIMULATION_TIME = TimeUtil.months(12);
 
 	private static Environment _environment = new Environment(-500, 500, -500, 500);
 	private static List<EventListener> _listeners = Lists.newArrayList();
 	private static InputData _inputData = new InputData();
 	private static BeeSimulation _simulation;
 
 	@Override
 	public void initialize() {
 		setTimeLast(SIMULATION_TIME);
 		setPace(SIMULATION_PACE);
 		createFlowers();
 		createHives();
 	}
 
 	private void createHives() {
 		registerHive(Position.createFromCoordinates(5, 5), _inputData.getNumberOfBeesPerHive(), "Rome");
 		registerHive(Position.createFromCoordinates(5, 5), 4, "Rome");
 		registerHive(Position.createFromCoordinates(-5, -5), 4, "Milan");
 		registerHive(Position.createFromCoordinates(5, -5), 4, "Naples");
 		registerHive(Position.createFromCoordinates(-5, 5), 4, "Turin");
 		// register(new BeeHive(Position.createFromCoordinates(0, 0), 1), "Palermo");
 		// register(new BeeHive(Position.createFromCoordinates(0, 0), 1), "Genoa");
 		// register(new BeeHive(Position.createFromCoordinates(0, 0), 1), "Bologna");
 		// register(new BeeHive(Position.createFromCoordinates(0, 0), 1), "Florence");
 		// register(new BeeHive(Position.createFromCoordinates(0, 0), 1), "Bari");
 		// register(new BeeHive(Position.createFromCoordinates(0, 0), 1), "Catania");
 	}
 
 	private void createFlowers() {
 		for (int i = 0; i < _inputData.getNumberOfFlowers(); i++) {
 			registerFlower("Flower" + i);
 		}
 		register(new Meadow(), "Meadow");
 	}
 
 	@Override
 	public void simulationComplete() {
 		info("Simulation complete.");
 		for (EventListener e : _listeners) {
 			e.close();
 		}
 	}
 
 	public static Environment getEnvironment() {
 		return _environment;
 	}
 
 	public static InputData getInputData() {
 		return _inputData;
 	}
 
 	public static BeeSimulation getSimulation() {
 		return _simulation;
 	}
 
 	public void stopSimulation() {
 		simulationComplete();
 		System.exit(0);
 	}
 
 	public static void notifyListeners(PositionedEntity entity) {
 		for (EventListener e : _listeners) {
 			e.notify(entity);
 		}
 	}
 
 	public static void addEventListener(EventListener e) {
 		_listeners.add(e);
 	}
 
 	private void registerHive(Position pos, int capacity, String name) {
 		BeeHive newHive = new BeeHive(pos, capacity);
 		register(newHive, name);
 		_environment.addHive(newHive);
 	}
 
 	private void registerFlower(String name) {
 		Flower currentFlower = Flower.create();
 		register(currentFlower, name);
 		_environment.addFlower(currentFlower);
 	}
 
 	public static void main(String[] args) throws Exception {
 		BeeSimulation.addEventListener(new ReportEventListener("report.csv"));
 		BeeSimulation.addEventListener(new VisualisationEventListener());
 		
 		Options options = new Options();
 		options.addOption("h", "help", false, "prints information about passing arguments");
 		options.addOption("n", "number", true, "number of groups of hives");
		options.addOption("s", "size", true, "size of each group, number of hives in group");
 		
 		CommandLineParser commandLineParser = new BasicParser();
 		CommandLine commandLine = commandLineParser.parse(options, args);
 		
 		if (commandLine.hasOption("h")) {
 			HelpFormatter helpFormatter = new HelpFormatter();
 			helpFormatter.printHelp("BeeSimulation <command> [<arg>]", options);
 		}
 		if (commandLine.hasOption("n")) {
 			System.out.println(commandLine.getOptionValue("n"));
 		}
 		
 		if (commandLine.hasOption("s")) {
 			System.out.println(commandLine.getOptionValue("s"));
 		}
 
 		_simulation = new BeeSimulation();
 		_simulation.run();
 	}
 
 }
