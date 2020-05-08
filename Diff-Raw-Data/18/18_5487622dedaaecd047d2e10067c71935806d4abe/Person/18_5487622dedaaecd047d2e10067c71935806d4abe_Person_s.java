 package edu.agh.tunev.model.cellular.agent;
 
 import java.awt.geom.Point2D;
 import java.util.List;
 
 import edu.agh.tunev.model.PersonProfile;
 import edu.agh.tunev.model.PersonState;
 import edu.agh.tunev.model.PersonState.Movement;
 import edu.agh.tunev.model.cellular.AllowedConfigs;
 import edu.agh.tunev.model.cellular.Model;
 import edu.agh.tunev.model.cellular.NeighbourIndexException;
 import edu.agh.tunev.model.cellular.grid.Cell;
 import edu.agh.tunev.world.Physics.Type;
 import edu.agh.tunev.world.Physics;
 
 public final class Person {
 
 	private final static int PERCEPTION_RANGE = 100;
 
 	/** Physics coefficient useful for field value evaluation */
	private final static double PHYSICS_COEFF = 0.00; // TODO: set
 
 	/** Distance coefficient useful for field value evaluation */
 	private final static double DIST_COEFF = 1.0; // TODO: set
 
	private final static double STATIC_COEFF = 100.0; // TODO:
 
 	private final static double DYNAMIC_COEFF = 1.0; // TODO:
 
 	/** Smiertelna wartosc temp. na wysokosci 1,5m */
 	public static final double LETHAL_TEMP = 80;
 	
 	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
 	private static final double LETHAL_CO_CONCN = 30000.0;
 
 	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
 	private static final double LETHAL_HbCO_CONCN = 75.0;
 
 	/** Prędkość z jaką usuwane są karboksyhemoglobiny z organizmu */
 	private static final double CLEANSING_VELOCITY = 0.08;
 
	private static final double MAX_STANDING_TEMP = 35;
 
 	private static final double MAX_SQUATTING_TEMP = 55;
 
 	private static final double MIN_ALERT_TEMP = 30;
 	
	private static final double PHYSICS_BASE = 1.1;
 
 	// TODO: discard unnecessary fields
 	private Cell cell;
 	private PersonState currentState;
 	private Orientation orientation;
 	private Movement pose;
 	private boolean active;
 	private boolean alive;
 	private double hbco;
 	private double dtMultiplier; // velocity = Cell.CELL_SIZE / (dtMultiplier *
 									// DT) -- TODO: poor one, not really
 									// versatile
 	private int dtCntr; // TODO: time should be accessed as parameter in
 						// update();
 	private boolean alerted;
 
 	private final int reactionTime;
 	private final AllowedConfigs allowedConfigs;
 	public final PersonProfile profile;
 
 	public Person(PersonProfile _profile, Cell _cell,
 			AllowedConfigs _allowedConfigs) throws WrongOrientationException {
 		this.profile = _profile;
 		this.cell = _cell;
 		cell.setPerson(this);
 		this.allowedConfigs = _allowedConfigs;
 		this.orientation = Orientation.randomizeOrient();
 		this.pose = profile.initialMovement;
 		this.alive = true;
 		this.active = true;
 		this.reactionTime = (int) Math.ceil(3 * cell.getDistToFireSrc());
 		saveState();
 	}
 
 	/**
 	 * Maps {@code Orientation} to corresponding angle.
 	 * 
 	 * @param orient
 	 * @return
 	 * @throws WrongOrientationException
 	 */
 	public static Double orientToAngle(Orientation orient)
 			throws WrongOrientationException {
 		// starting at east
 		Double angle = 0.0;
 		Person.Orientation[] orientValues = Person.Orientation.values();
 		int i;
 
 		for (i = 0; i < orientValues.length && orient != orientValues[i]; ++i) {
 			angle += 45.0;
 		}
 
 		if (i > 7)
 			throw new WrongOrientationException();
 		else
 			return angle;
 	}
 
 	public void update() throws NeighbourIndexException,
 			WrongOrientationException, NotANeighbourException {
 
 		++dtCntr;
 
 		if (active && dtCntr > reactionTime) {
 			Physics currPhys = cell.getPhysics();
 			Cell destination = selectField();
 
 			if ((dtCntr % dtMultiplier) == 0) {
 				setOrient(destination);
 				setPosition(destination);
 			}
 
 			setAlert(currPhys);
 			setPose(currPhys);
 			setVelocity();
 			checkActivity(currPhys);
 
 		}
 
 		saveState();
 	}
 
 	/**
 	 * Checks if agent is alive.
 	 * 
 	 * @return
 	 */
 	public boolean isAlive() {
 		return alive;
 	}
 
 	/**
 	 * Sets the agent state checking if he's been rescued or died.
 	 * 
 	 * @param currPhys
 	 */
 	private void checkActivity(Physics currPhys) {
 		checkIfIWillLive(currPhys.get(Type.CO), currPhys.get(Type.TEMPERATURE));
 
 		if (!alive) {
 			deactivate(Movement.DEAD);
 		} else if (this.cell.isExit()) {
 			deactivate(Movement.HIDDEN);
 		}
 	}
 
 	/**
 	 * Sets pose of an agent. Correlated with temperature.
 	 * 
 	 * @param phys
 	 */
 	private void setPose(Physics phys) {
 		double currTemp = phys.get(Type.TEMPERATURE);
 
 		if (currTemp <= MAX_STANDING_TEMP)
 			pose = Movement.STANDING;
 		else if (currTemp > MAX_STANDING_TEMP && currTemp <= MAX_SQUATTING_TEMP)
 			pose = Movement.SQUATTING;
 		else if (currTemp > MAX_SQUATTING_TEMP)
 			pose = Movement.CRAWLING;
 	}
 
 	/**
 	 * Sets agent's position on a certain cell.
 	 * 
 	 * @param destination
 	 */
 	private void setPosition(Cell destination) {
 		cell.release();
 		cell = destination;
 		cell.setPerson(this);
 	}
 
 	/**
 	 * Sets orientation of an agent depending on his direction of movement.
 	 * 
 	 * @param destination
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	private void setOrient(Cell destination) throws NeighbourIndexException,
 			NotANeighbourException {
 		Orientation orient = turnTowardCell(destination);
 		orientation = orient;
 	}
 
 	/**
 	 * Sets agent's velocity depending on their pose.
 	 */
 	// TODO: inelegant and lame, needs major rework
 	private void setVelocity() {
 		switch (pose) {
 		case STANDING:
 			dtMultiplier = 4;
 			break;
 		case SQUATTING:
 			dtMultiplier = 8;
 			break;
 		case CRAWLING:
 			dtMultiplier = 12;
 			break; // TODO: set
 		default:
 			;
 		}
 
 		if (alerted)
 			dtMultiplier /= 4;
 	}
 
 	/**
 	 * Sets agent in alerted state if conditions are menacing.
 	 * 
 	 * @param phys
 	 */
 	private void setAlert(Physics phys) {
 		double currTemp = phys.get(Type.TEMPERATURE);
 		if (currTemp >= MIN_ALERT_TEMP)
 			alerted = true;
 	}
 
 	/**
 	 * Checks if agent is able to survive conditions (CO density, temperature)
 	 * around.
 	 * 
 	 * @param curCo
 	 * @param curTemp
 	 */
 	private void checkIfIWillLive(double curCo, double curTemp) {
 		evaluateHbCO(curCo, Model.DT);
 		// <michał> wykomentowałem
 		// System.out.println(curr_co + " " + hbco);
 
 		if (hbco > LETHAL_HbCO_CONCN || curTemp > LETHAL_TEMP)
 			alive = false;
 	}
 
 	/**
 	 * Evaluates level of hbco in blood taking into account agent's cleansing
 	 * ability.
 	 * 
 	 * @param currCo
 	 * @param dt
 	 */
 	private void evaluateHbCO(double currCo, double dt) {
 		// TODO: Dobrac odpowiednie parametry
 		if (hbco > dt * CLEANSING_VELOCITY)
 			hbco -= dt * CLEANSING_VELOCITY;
 
 		hbco += dt * LETHAL_HbCO_CONCN * (currCo / LETHAL_CO_CONCN);
 	}
 
 	/**
 	 * Sets agent in one of inactive states (dead or rescued).
 	 * 
 	 * @param movType
 	 */
 	private void deactivate(Movement movType) {
 		active = false;
 		pose = movType;
 		this.cell.release();
 	}
 
 	/**
 	 * Saves current agent's state. Useful for rendering.
 	 * 
 	 * @throws WrongOrientationException
 	 */
 	// TODO: remove currentState field, refactor function below
 	private void saveState() throws WrongOrientationException {
 		Point2D.Double position = Cell.d2c(cell.getPosition());
 		Double numOrient = orientToAngle(orientation);
 		Movement movement = pose; // TODO:
 									// adjusting
 									// pose
 									// to
 									// external
 									// conditions
 
 		currentState = new PersonState(position, numOrient, movement);
 	}
 
 	/**
 	 * Selects most attractive field in Moore's neighbourhood.
 	 * 
 	 * @return
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	private Cell selectField() throws NeighbourIndexException,
 			NotANeighbourException {
 		List<Cell> neighbours = cell.getNeighbours();
 		Cell selectedField = this.cell;
 		Double lowestPotential = evaluateCostFunc(this.cell);
 
 		for (Cell neighbour : neighbours) {
 			Double neighbourPotential = getFieldPotential(neighbour);
 			if (neighbourPotential < lowestPotential) {
 				selectedField = neighbour;
 				lowestPotential = neighbourPotential;
 			}
 		}
 
 		return selectedField;
 	}
 
 	/**
 	 * Returns field potential.
 	 * 
 	 * @param c
 	 * @return
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	private Double getFieldPotential(Cell c) throws NeighbourIndexException,
 			NotANeighbourException {
 
 		if (c.equals(this.cell))
 			throw new NotANeighbourException();
 
 		if (checkFieldAvailability(c))
 			return evaluateCostFunc(c);
 
 		return Double.MAX_VALUE;
 	}
 
 	/**
 	 * Evaluates cost function for certain field.
 	 * 
 	 * @param cell
 	 * @return
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	// TODO:change cost function, adjust to social dist model;
 	private Double evaluateCostFunc(Cell cell) throws NeighbourIndexException,
 			NotANeighbourException {
 
 		if (cell.equals(this.cell))
 			return Double.MAX_VALUE;
 
 		Double dist = evaluateDistComponent(cell);
 		Double heat = evaluateHeatComponent(cell);
 		return STATIC_COEFF * cell.getStaticFieldVal() + DYNAMIC_COEFF
 				* (DIST_COEFF * dist + PHYSICS_COEFF * Math.pow(PHYSICS_BASE, heat));
 	}
 
 	/**
 	 * Calculates heat component of a neighbouring cell. Component is an average
 	 * temperature in row of cells.
 	 * 
 	 * @param neighbour
 	 * @return heat component
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	private Double evaluateHeatComponent(Cell neighbour)
 			throws NeighbourIndexException, NotANeighbourException {
 
 		if (neighbour.equals(this.cell))
 			throw new NotANeighbourException();
 
 		int neighbourIndex = Cell.positionToIndex(this.cell, neighbour);
 		List<Cell> row = neighbour.getRow(neighbourIndex, PERCEPTION_RANGE);
 		Double sum = 0.0;
 		Double acc = 0.0;
 
 		for (Cell c : row) {
 			Physics phys = c.getPhysics();
 			if (phys != null) {
 				sum += c.getPhysics().get(Type.TEMPERATURE);
 				++acc;
 			}
 		}
 
 		if (acc == 0)
 			return Double.MAX_VALUE;
 
 		return sum / acc;
 	}
 
 	/**
 	 * Evaluates distance component of cost function.
 	 * 
 	 * @param neighbour
 	 * @return
 	 */
 	private Double evaluateDistComponent(Cell neighbour) {
 		Double dist = 0.0;
 
 		if (!neighbour.equals(this.cell)) {
 			Point2D.Double neighbourRealPosition = neighbour.getRealPosition();
 			Point2D.Double baseRealPosition = this.cell.getRealPosition();
 			dist = baseRealPosition.distance(neighbourRealPosition);
 		} else
 			dist = Math.sqrt(2) * Cell.CELL_SIZE;
 
 		return dist;
 	}
 
 	/**
 	 * Checks if coming onto {@code cell} is possible.
 	 * 
 	 * @param c
 	 * @return
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	private boolean checkFieldAvailability(Cell c)
 			throws NeighbourIndexException, NotANeighbourException {
 		if (c.isOccupied() || c.isBlocked())
 			return false;
 
 		boolean cellAvailability = allowedConfigs.checkCellAvailability(c,
 				turnTowardCell(c), this);
 
 		return cellAvailability;
 
 	}
 
 	/**
 	 * Rotates agent toward their destination.
 	 * 
 	 * @param c
 	 * @return
 	 * @throws NeighbourIndexException
 	 * @throws NotANeighbourException
 	 */
 	private Orientation turnTowardCell(Cell c) throws NeighbourIndexException,
 			NotANeighbourException {
 		if (c.equals(this.cell))
 			return orientation;
 
 		int index = Cell.positionToIndex(this.cell, c);
 		return Orientation.neighbourIndexToOrient(index);
 	}
 
 	public Movement getPose() {
 		return pose;
 	}
 
 	public void setPose(Movement pose) {
 		this.pose = pose;
 	}
 
 	public Cell getCell() {
 		return cell;
 	}
 
 	public Orientation getOrientation() {
 		return orientation;
 	}
 
 	public PersonState getCurrentState() {
 		return currentState;
 	}
 
 	/**
 	 * Represents orientation of an agent.
 	 */
 	public enum Orientation {
 		E, NE, N, NW, W, SW, S, SE;
 
 		/**
 		 * Returns index of a specific {@code Person.Orienation}
 		 * 
 		 * @param orient
 		 * @return index of orientation
 		 */
 		public static int getIndexOf(Orientation orient) {
 			Person.Orientation[] values = Person.Orientation.values();
 			int ind;
 
 			for (ind = 0; ind < values.length && values[ind] != orient; ++ind)
 				;
 
 			return ind;
 		}
 
 		/**
 		 * Randomizes {@code Person.Orientation}
 		 * 
 		 * @return random orientation
 		 */
 		public static Orientation randomizeOrient() {
 			Orientation[] values = values();
 			int index = (int) (Math.random() * 8);
 			return values[index];
 		}
 
 		/**
 		 * Agent needs to turn toward a cell before he moves. This method maps a
 		 * neighbour index (check {@link Cell#positionToIndex(Cell, Cell)}) of
 		 * that cell to the anticipated orientation of an agent.
 		 * 
 		 * @param index
 		 * @return
 		 * @throws NeighbourIndexException
 		 */
 		public static Orientation neighbourIndexToOrient(int index)
 				throws NeighbourIndexException {
 			switch (index) {
 			case 0:
 				return NW;
 			case 1:
 				return N;
 			case 2:
 				return NE;
 			case 3:
 				return W;
 			case 4:
 				return E;
 			case 5:
 				return SW;
 			case 6:
 				return S;
 			case 7:
 				return SE;
 			default:
 				throw new NeighbourIndexException();
 			}
 		}
 
 		/**
 		 * Check if an orientation belongs to {{@code W,SW,S,SE} and if so -
 		 * maps it to an orientation laying on the same axis but directed
 		 * inversly, eg. S -> N; SW -> NE.
 		 * 
 		 * @param orient
 		 * @return opposite orientation
 		 */
 		public static Orientation translateOrient(Orientation orient) {
 			int index = getIndexOf(orient);
 			int translatedIndex = index;
 			Orientation[] values = values();
 
 			if (index >= values.length / 2)
 				translatedIndex = (index + (values.length / 2)) % values.length;
 
 			return values[translatedIndex];
 		}
 	}
 
 }
