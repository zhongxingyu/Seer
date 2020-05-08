 package edu.agh.tunev.model.kkm;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import edu.agh.tunev.model.PersonState;
 import edu.agh.tunev.world.Obstacle;
 
 class Motion {
 	/** Wspolczynnik predkosci dla pozycji zgiętej */
 	private final static double BENT_COEFF = 0.75;
 
 	/** Wspolczynnik predkosci dla czołgania */
 	private final static double CRAWL_COEFF = 0.1;
 
 	/** Gęstość dymu przy której agent musi się zgiąć */
 	private final static double SMOKE_BENT_DENSITY = 5200;
 
 	/** Gęstość dymu, przy której agent musi się czołgać */
 	private final static double SMOKE_CRAWL_DENSITY = 20800;
 
 	/** Standardowa, poczatkowa predkosc ruchu */
	private final static double AVG_MOVING_SPEED = 1.6;
 
 	/** Aktualna postawa agenta */
 	PersonState.Movement stance;
 
 	/**
 	 * Lista punktow, ktore zamierzamy odwiedzic. Wybrane wyjscie jest
 	 * checkpointem o indeksie 0
 	 */
 	List<Point2D.Double> checkpoints;
 
 	/** Aktualna predkosc */
 	double velocity;
 
 	/** Referencja do szefa */
 	private Agent agent;
 
 	/** Wsp. predkosci, cecha osobnicza agenta */
 	private double velocity_coeff;
 
 	Motion(Agent _agent) {
 		this.agent = _agent;
 		checkpoints = new ArrayList<Point2D.Double>();
 		velocity_coeff = (Math.random() / 2) + 0.75; // range [0.75, 1.25]
 		velocity = velocity_coeff * AVG_MOVING_SPEED;
 		stance = PersonState.Movement.STANDING;
 	}
 
 	/** Ruch w danym kierunki z aktualna predkoscia */
 	void move() {
 		double x = agent.position.x + velocity * agent.dt
 				* Math.cos(Math.toRadians(agent.phi));
 		double y = agent.position.y + velocity * agent.dt
 				* Math.sin(Math.toRadians(agent.phi));
 
 		Point2D.Double dest = new Point2D.Double(x, y);
 
 		if (!isDynamicCollision(dest))
 			agent.position = dest;
 	}
 
 	/**
 	 * Dostosowuje predkosc agenta do warunkow srodowiskowych i uwzglednia
 	 * poziom jego przerazenia
 	 * 
 	 * @param smoke_density
 	 *            gęstość dymu na aktualnej pozycji
 	 * @param anxiety
 	 *            poziom przerażenia
 	 */
 	void adjustVelocity(double smoke_density, double anxiety) {
 		changeStance(smoke_density);
 		velocity = velocity_coeff * anxiety * AVG_MOVING_SPEED;
 
 		if (stance == PersonState.Movement.SQUATTING)
 			velocity *= BENT_COEFF;
 		else if (stance == PersonState.Movement.CRAWLING)
 			velocity *= CRAWL_COEFF;
 
 	}
 
 	/**
 	 * Sprawdza, czy w punkcie, do ktorego sie chcemy przemiescic nie ma
 	 * przeszkody. Punkt jest wyliczany na podstawie kata i predkosci ruchu.
 	 * 
 	 * @param angle
 	 *            kat wyznaczajacy kierunek ruchu
 	 * @return przeszkoda(Obstacle lub Wall) albo null, jesli jest miejsce do
 	 *         ruchu
 	 */
 	// TODO: quick-fix: <michał> zmienić hierarchię? przerobiłem na Object -,-
 	Object isStaticCollision(double angle) {
 		double path_length = velocity * agent.dt + Agent.BROADNESS;
 		double alpha = angle + agent.phi;
 		double sin = Math.sin(Math.toRadians(alpha));
 		double cos = Math.cos(Math.toRadians(alpha));
 
 		Point2D.Double p = new Point2D.Double(agent.position.x + path_length * cos,
 				agent.position.y + path_length * sin);
 
 		if (agent.board.isOutOfBounds(p))
 			return new Board.Wall();
 
 		return isObstacleInPos(p);
 	}
 
 	/**
 	 * Sprawdza czy w danym punkcie znajduje się przeszkoda.
 	 * 
 	 * @param p
 	 *            punkt
 	 * @return referencja do przeszkody
 	 */
 	Obstacle isObstacleInPos(Point2D.Double p) {
 		for (Obstacle ob : agent.board.getObstacles()) {
 			if (ob.contains(p, 2 * Agent.BROADNESS))
 				return ob;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Funkcja oblicza punkt, w ktory agent musi sie udac, by obejsc przeszkode.
 	 * Najpierw sprawdza, z której strony Obstacle sie znajduje, a nastepnie
 	 * wybiera, w strone ktorego wierzcholka sie poruszyc (wybiera ten, ktory
 	 * znajduje sie blizej wyjscia);
 	 * 
 	 * @param ob
 	 *            przeszkoda, ktora agent chce ominac
 	 * @return wspolrzedne checkpointa
 	 */
 	// TODO: Motion
 	Point2D.Double avoidCollision(Obstacle ob) {
 		Point2D.Double start_point = ob.p1;
 		Point2D.Double end_point = ob.p2;
 
 		// obliczamy wspolrzedne wierzcholkow przeszkody (za malym zapasem)
 		Point2D.Double left_bot = new Point2D.Double(start_point.x - Agent.BROADNESS,
 				start_point.y - Agent.BROADNESS);
 		Point2D.Double left_top = new Point2D.Double(start_point.x - Agent.BROADNESS, end_point.y
 				+ Agent.BROADNESS);
 		Point2D.Double right_bot = new Point2D.Double(end_point.x + Agent.BROADNESS,
 				start_point.y - Agent.BROADNESS);
 		Point2D.Double right_top = new Point2D.Double(end_point.x + Agent.BROADNESS, end_point.y
 				+ Agent.BROADNESS);
 
 		// obliczamy odleglosci agenta od poszczegolnych bokow przeszkody (z
 		// zapasem)
 		List<Double> dist_list = new ArrayList<Double>();
 		dist_list.add(Math.abs(agent.position.x
 				- (start_point.x - 2 * Agent.BROADNESS))); // left
 		dist_list.add(Math.abs(agent.position.y
 				- (end_point.y + 2 * Agent.BROADNESS))); // top
 		dist_list.add(Math.abs(agent.position.x
 				- (end_point.x + 2 * Agent.BROADNESS))); // right
 		dist_list.add(Math.abs(agent.position.y
 				- (start_point.y - 2 * Agent.BROADNESS))); // bottom
 
 		// wybieramy najmniejsza odleglosc
 		double min_dist = Collections.min(dist_list);
 		Point2D.Double[] selected_points = new Point2D.Double[2];
 
 		// wybieramy odpowiednie wierzcholki
 		// left
 		if (min_dist == dist_list.get(0)) {
 			selected_points[0] = left_bot;
 			selected_points[1] = left_top;
 			// top
 		} else if (min_dist == dist_list.get(1)) {
 			selected_points[0] = left_top;
 			selected_points[1] = right_top;
 			// right
 		} else if (min_dist == dist_list.get(2)) {
 			selected_points[0] = right_bot;
 			selected_points[1] = right_top;
 			// bottom
 		} else if (min_dist == dist_list.get(3)) {
 			selected_points[0] = left_bot;
 			selected_points[1] = right_bot;
 		}
 
 		Point2D.Double exit_pos = agent.board.getExitClosestPoint(agent.exit, agent.position);
 		double[] p_dists = new double[2];
 		p_dists[0] = exit_pos.distance(selected_points[0]);
 		p_dists[1] = exit_pos.distance(selected_points[1]);
 
 		// wybieramy wierzcholek blizszy wyjsciu
 		if (p_dists[0] < p_dists[1])
 			return selected_points[0];
 		else
 			return selected_points[1];
 	}
 
 	/**
 	 * Dodaje checkpoint do listy. Jesli jego odleglosc od wyjscia jest mniejsza
 	 * niz innych, to odpowiednio przycina liste.
 	 * 
 	 * @param new_checkpoint
 	 */
 	void addCheckpoint(Point2D.Double new_checkpoint) {
 		trimCheckpoints(new_checkpoint);
 		checkpoints.add(new_checkpoint);
 	}
 
 	/**
 	 * Aktualizuje liste checkpointow, uwzgledniajac aktualna pozycje agenta.
 	 * Jesli jest blizej wyjscia niz, ktorys z punktow na liscie, to ow punkt
 	 * jest usuwany. Koordynaty wybranego wyjscia(exit) sa zawsze pierwsze na
 	 * liscie.
 	 */
 	void updateCheckpoints() {
 		if (agent.exit == null) // TODO:dodałem, bo wywalało
 								// NullPointerException --
 								// m.
 			return;
 		Point2D.Double exit_pos = agent.board.getExitClosestPoint(agent.exit, agent.position);
 		if (!checkpoints.isEmpty())
 			checkpoints.set(0, exit_pos);
 		else
 			checkpoints.add(exit_pos);
 		trimCheckpoints(agent.position);
 	}
 
 	/**
 	 * Usuwa te checkpointy z listy, ktore sa dalej od wyjscia niz punkt p
 	 * 
 	 * @param p
 	 */
 	private void trimCheckpoints(Point2D.Double p) {
 		if (!checkpoints.isEmpty()) {
 			Point2D.Double exit_pos = agent.board.getExitClosestPoint(agent.exit, agent.position);
 			double new_dist = exit_pos.distance(p);
 
 			int index = 0;
 			while (index <= checkpoints.size() - 1
 					&& new_dist > exit_pos.distance(checkpoints.get(index)))
 				++index;
 
 			checkpoints.subList(index, checkpoints.size()).clear();
 		}
 	}
 
 	/**
 	 * Określa postawę agenta w zależnosci od gęstości dymu.
 	 * 
 	 * @param smoke_density
 	 */
 	private void changeStance(double smoke_density) {
 		if (smoke_density < SMOKE_BENT_DENSITY)
 			stance = PersonState.Movement.STANDING;
 		else if (smoke_density >= SMOKE_BENT_DENSITY
 				&& smoke_density < SMOKE_CRAWL_DENSITY)
 			stance = PersonState.Movement.SQUATTING;
 		else
 			stance = PersonState.Movement.CRAWLING;
 	}
 
 	/**
 	 * Sprawdza, czy w punkcie, do ktorego agent sie chce przemiescic, nie
 	 * znajduje się inny ewakuowany
 	 * 
 	 * @param dest
 	 *            punkt do ktorego agent chce sie przemiescic
 	 * @return
 	 */
 	private boolean isDynamicCollision(Point2D.Double dest) {
 		for (Agent a : agent.board.getAgents()) {
 			if (!a.isAlive() || a.isExited() || a.equals(agent))
 				continue;
 			
 			double dest_dist = a.getPosition().distance(dest);
 			if (dest_dist < Agent.THICKNESS)
 				return true;
 		}
 
 		return false;
 	}
 }
