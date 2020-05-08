 package edu.agh.tunev.model.kkm;
 
 import java.awt.geom.Point2D;
 
 import edu.agh.tunev.model.PersonProfile;
 import edu.agh.tunev.model.PersonState;
 import edu.agh.tunev.world.Exit;
 import edu.agh.tunev.world.Obstacle;
 import edu.agh.tunev.world.Physics;
 
 public final class Agent {
 
 	/** Wspolczynnik przeskalowujacy temperature na zagrożenie */
 	static final double TEMP_THREAT_COEFF = 0.06;
 
 	/** Szerokość elipsy Agenta w [m]. */
 	public static final double BROADNESS = 0.33;
 
 	/** Ten drugi wymiar (grubość?) elipsy Agenta w [m]. */
 	public static final double THICKNESS = 0.2;
 
 	/**
 	 * Długość wektora orientacji Agenta w [m]. Nic nie robi, tylko do
 	 * rysowania.
 	 */
 	public static final double ORIENTATION_VECTOR = 1.0;
 
 	/** Kat miedzy promieniami wyznaczajacymi wycinek kola bedacy sasiedztwem */
 	private static final double CIRCLE_SECTOR = 45; // 360/8
 
 	/**
 	 * Wartosc podstawy w f. wykladniczej wykorzystywana do obliczania promienia
 	 * sasiedztwa za pomoca kata
 	 */
 	private static final double BASE_RADIUS_CALC = 1.2;
 
 	/**
 	 * Wartosc podstawy w f. wykladniczej wykorzystywana do obliczania
 	 * wspolczynnika atrakcyjnosci danego kierunku. Wspolczynniki dla kierunkow
 	 * o mniejszych katach, czyli takich, ktore pozwola mniej wiecej zachowac
 	 * kierunek ucieczki, maja odpowiednio wieksza wartosc.
 	 */
 	private static final double BASE_ATTR_CALC = 1.01;
 
 	/**
 	 * Współczynnik do skalowania funkcji wykładniczej wykorzystywanej do
 	 * obliczania promienia sąsiedztwa
 	 */
 	private static final double POW_RADIUS_COEFF = 8;
 
 	/**
 	 * Współczynnik do skalowania funkcji wykładniczej wykorzystywanej do
 	 * obliczania wspolczynnika atrakcyjnosci.
 	 */
 	private static final double POW_ATTR_COEFF = 1;
 
 	/**
 	 * Minimalna wartość współczynnika zagrożenia powodująca zmianę kierunku.
 	 * Agent zawsze kieruję się w stronę wyjścia, chyba że czynniki środowiskowe
 	 * mu na to nie pozwalają.
 	 */
 	private static final double MIN_THREAT_VAL = 60;
 
 	/**
 	 * Odleglosc od wyjscia, dla ktorej agent przestaje zwracac uwage na
 	 * czynniki zewnetrzne i rzuca sie do drzwi/portalu
 	 */
 	private static final double EXIT_RUSH_DIST = 3;
 
 	/** Minimalna temp. przy której agent widzi ogień */
 	private static final double MIN_FLAME_TEMP = 70;
 
 	/** Smiertelna wartosc temp. na wysokosci 1,5m */
 	private static final double LETHAL_TEMP = 80;
 
 	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
 	private static final double LETHAL_CO_CONCN = 30000.0;
 
 	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
 	private static final double LETHAL_HbCO_CONCN = 75.0;
 
 	/** Prędkość z jaką usuwane są karboksyhemoglobiny z organizmu */
 	private static final double CLEANSING_VELOCITY = 0.08;
 
 	/** Wsp. do obliczania gęstości dymu na podstawie stężenia CO */
 	// TODO: bardzo naciągane, ale to jest zbyt zmienne i nie ma danych
 	private static final double CO_SMOKE_COEFF = 6.5;
 	
 	/** Mol/mol do ppm*/
 	private static final double MOL_TO_PPM = 1E11;
 
 	/** Współczynnik funkcji przekształcającej odległość na czas reakcji */
	private static final double REACTION_COEFF = 0.3; // wspolczynnik *
 																// [s/ms]
 
 	/** Pozycja Agenta na planszy w rzeczywistych [m]. */
 	Point2D.Double position;
 
 	/** Aktualnie wybrane wyjście ewakuacyjne */
 	Exit exit;
 
 	/** Referencja do planszy. */
 	Board board;
 
 	/**
 	 * Orientacja: kąt między wektorem "wzroku" i osią OX w [deg]. Kiedy wynosi
 	 * 0.0 deg, to Agent "patrzy" jak oś OX (jak na geometrii analitycznej).
 	 * Wtedy też sin() i cos() działają ~intuicyjne, tak samo jak analityczne
 	 * wzory. :] -- m.
 	 */
 	double phi;
 
 	/** Flaga informująca o statusie jednostki - zywa lub martwa */
 	private boolean alive;
 
 	/** Flaga mówiąca o tym, czy Agentowi udało się już uciec. */
 	boolean exited;
 
 	/** Czas, który upłynie, nim agent podejmie decyzje o ruchu */
 	private double pre_movement_t;
 
 	/** Aktualne stezenie karboksyhemoglobiny we krwii */
 	private double hbco;
 
 	/** Czas ruchu agenta */
 	double dt;
 
 	/** 'Modul' ruchu agenta */
 	private Motion motion;
 
 	/** Charakterystyka psychiki agenta */
 	private Psyche psyche;
 
 	/**
 	 * Konstruktor agenta. Inicjuje wszystkie pola niezbędne do jego egzystencji
 	 * na planszy. Pozycja jest z góry narzucona z poziomu Board. Orientacja
 	 * zostaje wylosowana.
 	 * 
 	 * @param board
 	 *            referencja do planszy
 	 * @param position
 	 *            referencja to komórki będącej pierwotną pozycją agenta
 	 */
 	public final PersonProfile profile;
 	public Agent(Board board, PersonProfile profile) {
 		this.profile = profile;
 		this.board = board;
 		this.position = profile.initialPosition;
 		motion = new Motion(this);
 		psyche = new Psyche(this);
 
 		phi = profile.initialOrientation;
 		motion.stance = profile.initialMovement;
 
 		alive = true;
 		exited = false;
 		hbco = 0;
 		dt = 0;
 
 		// TODO: <michał> co z nearest fire src?
 		pre_movement_t = (REACTION_COEFF
 				* 1/*position.evalDist(board.getNearestFireSrc(position))*/ + psyche.reaction_t);
 	}
 
 	/**
 	 * Akcje agenta w danej iteracji.
 	 * 
 	 * 1. Sprawdza, czy agent nie powinien zginac w tej turze.
 	 * 
 	 * 2. Wybiera wyjście.
 	 * 
 	 * 3. Aktualizuje liste checkpointow.
 	 * 
 	 * 4. Aktualizuje stopień poddenerwowania agenta.
 	 * 
 	 * 5. Dostosowuje prędkość do warunków z uwzględnieniem poddenerwowania.
 	 * 
 	 * 6. Na podstawie danych otrzymanych w poprzednim punkcie podejmuje
 	 * decyzje.
 	 * 
 	 * 7. Wykonuje ruch.
 	 * 
 	 * @param dt
 	 *            Czas w [ms] jaki upłynął od ostatniego update()'u. Można
 	 *            wykorzystać go do policzenia przesunięcia w tej iteracji z
 	 *            zadaną wartością prędkości:
 	 *            {@code dx = dt * v * cos(phi); dy = dt * v * sin(phi);}
 	 * @throws NoPhysicsDataException
 	 */
 	public void update(double _dt) {
 		this.dt = _dt;
 		double curr_temp = getMeanPhysics(0, 360, BROADNESS,
 				Physics.Type.TEMPERATURE);
 		double curr_co = MOL_TO_PPM * getMeanPhysics(0, 360, BROADNESS, Physics.Type.CO);
 		checkIfIWillLive(curr_co, curr_temp);
 
 		if (alive) {
 			double smoke_density = curr_co * CO_SMOKE_COEFF;
 
 			chooseExit();
 			motion.updateCheckpoints();
 			psyche.expAnxiety(TEMP_THREAT_COEFF * curr_temp);
 			motion.adjustVelocity(smoke_density, psyche.anxiety);
 			makeDecision();
 			motion.move();
 		}
 
 		// jak wyszliśmy poza planszę, to wyszliśmy z tunelu? exited = true
 		// spowoduje zaprzestanie wyświetlania agenta i podbicie statystyk
 		// uratowanych w każdym razie :]
 		// TODO: zmieniać na true dopiero gdy doszliśmy do wyjścia
 		exited = (distToExit(exit) < THICKNESS)
 				|| (position.x < 0 || position.y < 0
 						|| position.x > board.getDimension().x || position.y > board
 						.getDimension().y);
 	}
 
 	public Point2D.Double getPosition() {
 		return position;
 	}
 
 	/**
 	 * 
 	 * @return obrot wzg OX
 	 */
 	public double getOrientation() {
 		return phi;
 	}
 
 	/**
 	 * Zwraca wartość niezależnie od tego, czy jest na planszy, czy nie.
 	 * 
 	 * @return stan zdrowia
 	 */
 	public boolean isAlive() {
 		return alive;
 	}
 
 	/**
 	 * Sprawdza czy agent jest na planszy
 	 * 
 	 * @return
 	 */
 	public boolean isExited() {
 		return exited;
 	}
 
 	/**
 	 * 
 	 * @return czas ktory uplynie przed podjeciem ruchu
 	 */
 	public double getPreMoveTime() {
 		return pre_movement_t;
 	}
 
 	/**
 	 * Zwraca stężenie karboksyhemoglobiny we krwi
 	 * 
 	 * @return hbco
 	 */
 	public double getHBCO() {
 		return hbco;
 	}
 
 	/**
 	 * Zwraca aktualna prędkość agenta
 	 * 
 	 * @return velocity
 	 */
 	public double getVelocity() {
 		return motion.velocity;
 	}
 
 	public PersonState.Movement getStance(){
 		return motion.stance;
 	}
 	
 	/**
 	 * Okresla, czy agent przezyje, sprawdzajac temperature otoczenia i stezenie
 	 * toksyn we krwii
 	 * 
 	 * @param curr_co
 	 *            stężenie co w bliskim otoczeniu agenta
 	 * @param curr_temp
 	 *            średnia temp. w bliskim otoczeniu agenta
 	 */
 	private void checkIfIWillLive(double curr_co, double curr_temp) {
 		evaluateHbCO(curr_co);
 		// <michał> wykomentowałem
 		//System.out.println(curr_co + " " + hbco);
 
 		if (hbco > LETHAL_HbCO_CONCN || curr_temp > LETHAL_TEMP)
 			alive = false;
 	}
 
 	/**
 	 * Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
 	 * zdolnosci organizmu do usuwania toksyn
 	 */
 	private void evaluateHbCO(double curr_co) {
 		// TODO: Dobrac odpowiednie parametry
 		if (hbco > dt * CLEANSING_VELOCITY)
 			hbco -= dt * CLEANSING_VELOCITY;
 
		hbco += dt * LETHAL_HbCO_CONCN * (curr_co / LETHAL_CO_CONCN);
 	}
 
 	/**
 	 * Podejmuje decyzje, co do kierunku ruchu lub ustala nowy checkpoint.
 	 */
 	private void makeDecision() {
 		phi = calculateNewPhi();
 		double attractivness_ahead = computeThreatComponent(0);
 		Object barrier = motion.isStaticCollision(0);
 
 		if (distToExit(exit) > EXIT_RUSH_DIST
 				&& attractivness_ahead > MIN_THREAT_VAL && barrier == null) {
 
 			double attractivness = Double.POSITIVE_INFINITY;
 			for (double angle = -180; angle < 180; angle += CIRCLE_SECTOR) {
 				if (angle == 0)
 					continue;
 
 				double attr_coeff = 1 / computeMagnitudeByAngle(POW_ATTR_COEFF,
 						BASE_ATTR_CALC, angle);
 				double curr_attractivness = attr_coeff
 						* computeThreatComponent(angle);
 
 				if (curr_attractivness < attractivness
 						&& motion.isStaticCollision(angle) == null) {
 
 					attractivness = curr_attractivness;
 					phi += angle;
 				}
 			}
 		}
 
 		if (barrier instanceof Obstacle)
 			motion.addCheckpoint(motion.avoidCollision((Obstacle) barrier));
 	}
 
 	/**
 	 * Metoda obliczająca kąt, który agent musi obrać, by skierować się do
 	 * wybranego checkpointa. Kąt jest wyznaczony przez oś X i odcinek łączący
 	 * najblizszy checkpoint z aktualną pozycją agenta. Korzysta z funkcji
 	 * atan2(), która w przeciwieństwie do atan() uwzględnia orientację na
 	 * płaszczyźnie.
 	 * 
 	 * @return kąt zawart w przedziale [-180, 180)
 	 */
 	private double calculateNewPhi() {
 		if (motion.checkpoints.isEmpty()) // TODO: chyba tak ma być, nie było
 											// tego sprawdzenia i wywalało
 											// ArrayIndexOutOfBoundsException --
 											// m.
 			return phi;
 
 		Point2D.Double checkpoint = motion.checkpoints
 				.get(motion.checkpoints.size() - 1);
 		double deltaY = checkpoint.y - position.y;
 		double deltaX = checkpoint.x - position.x;
 		double angle = Math.atan2(deltaY, deltaX);
 
 		return Math.toDegrees(angle);
 	}
 
 	/**
 	 * Wybór najbliższego wyjścia do którego możliwe jest przejście
 	 * 
 	 * @throws NoPhysicsDataException
 	 */
 	private void chooseExit() {
 		Exit chosen_exit = null;
 		Exit curr_exit = getNearestExit(-1);
 
 		do {
 			chosen_exit = curr_exit;
 			double dist_exit = distToExit(chosen_exit);
 			curr_exit = getNearestExit(dist_exit);
 		} while (checkForBlockage(chosen_exit) > 0 && curr_exit != null);
 
 		exit = chosen_exit;
 
 	}
 
 	/**
 	 * Bierze pod uwage odległości na tylko jednej osi. Szuka najbliższego
 	 * wyjścia w odległości nie mniejszej niż dist. Pozwala to na szukanie wyjść
 	 * będących alternatywami. Dla min_dist mniejszego od 0 szuka po prostu
 	 * najbliższego wyjścia
 	 * 
 	 * @param min_dist
 	 *            zadana minimalna odległość
 	 * @return najbliższe wyjście spełniające warunki
 	 */
 	private Exit getNearestExit(double min_dist) {
 		double shortest_dist = board.getDimension().x + board.getDimension().y;
 		Exit nearest_exit = null;
 
 		for (Exit e : board.getExits()) {
 			double dist = Math.abs(distToExit(e));
 			if (dist < shortest_dist && dist > min_dist) {
 				shortest_dist = dist;
 				nearest_exit = e;
 			}
 		}
 		return nearest_exit;
 	}
 
 	/**
 	 * Algorytm działa, poruszając sie po dwóch osiach: X - zawsze, Y - jeśli
 	 * znajdzie blokadę. Zaczyna od wspolrzędnej Y agenta i porszuamy się po tej
 	 * osi w stronę potencjalnego wyjścia. Jeśli natrafi na przeszkodę, to
 	 * sprawdza, czy cała szerokość tunelu dla tej wartości Y jest zablokowana.
 	 * Porszuając się po osi X o szerokość agenta, sprawdza, czy na całym
 	 * odcinku o dł. równej szerokości tunelu znajdują się blokady. Jeśli
 	 * znajdzie się choć jeden przesmyk - przejście istnieje -> sprawdzamy
 	 * kolejne punkty na osi Y. Jeśli nie istnieje, metoda zwraca wspolrzedna Y
 	 * blokady.
 	 * 
 	 * TODO: W bardziej rzeczywistym modelu agent wybierze kierunek przeciwny do
 	 * źródła ognia.
 	 * 
 	 * @param _exit
 	 *            wyjście, w kierunku którego agent chce uciekać
 	 * @return -1 jeśli drgoa do wyjścia _exit nie jest zablokowana wspolrzedna
 	 *         y blokady, jesli nie ma przejscia
 	 * @throws NoPhysicsDataException
 	 */
 	// TODO: rework, uwaga na (....XXX__XX...)
 	private double checkForBlockage(Exit _exit) {
 		boolean viable_route = true;
 		double exit_y = board.getExitY(_exit);
 		double dist = Math.abs(position.y - exit_y);
 		double ds = 0.5; /*board.getDataCellDimension().y*/; //TODO:<michał> co tutaj?
 
 		if (position.y > exit_y)
 			ds = -ds;
 
 		// poruszamy się po osi Y w kierunku wyjścia
 		double y_coord = position.y + ds;
 		while (Math.abs(y_coord - position.y) < dist) {
 			double x_coord = 0 + BROADNESS;
 			double checkpoint_y_temp = 0;
 			checkpoint_y_temp = board.getPhysics(
 					new Point2D.Double(x_coord, y_coord), Physics.Type.TEMPERATURE);
 
 			// poruszamy się po osi X, jeśli natrafiliśmy na blokadę
 			if (checkpoint_y_temp > MIN_FLAME_TEMP) {
 				viable_route = false;
 				while (x_coord < board.getDimension().x) {
 					double checkpoint_x_temp = MIN_FLAME_TEMP;
 					Point2D.Double checkpoint_x = new Point2D.Double(x_coord, y_coord);
 					checkpoint_x_temp = board.getPhysics(checkpoint_x,
 							Physics.Type.TEMPERATURE);
 
 					if (checkpoint_x_temp < MIN_FLAME_TEMP
 							|| motion.isObstacleInPos(checkpoint_x) == null)
 						viable_route = true;
 
 					x_coord += BROADNESS;
 				}
 			}
 			// jeśli nie ma przejścia zwracamy wsp. Y blokady
 			if (!viable_route)
 				return y_coord;
 
 			y_coord += ds;
 		}
 		return -1;
 	}
 
 	/**
 	 * Oblicza odleglosc miedzy aktualna pozycja a wyjsciem
 	 * 
 	 * @param _exit
 	 *            wybrane wyjscie
 	 * @return odleglosc
 	 */
 	double distToExit(Exit _exit) {
 		if (_exit == null)
 			return Double.POSITIVE_INFINITY;
 		Point2D.Double exit_closest_p = board.getExitClosestPoint(_exit, position);
 		return position.distance(exit_closest_p);
 	}
 
 	/**
 	 * Zwraca średnią wartość parametru fizycznego na wybranej powierzchni --
 	 * wycinka koła o środku w środku danego Agenta.
 	 * 
 	 * Koncept: 1) jedziemy ze stałym {@code dalpha} po całym {@code alpha}; 2)
 	 * dla każdego z tych kątów jedziemy ze stałym {@code dr} po {@code r}. 3)
 	 * Bierzemy wartość parametru w punkcie określonym przez {@code dalpha} i
 	 * {@code dr}, dodajemy do sumy, a na końcu 4) zwracamy sumę podzieloną
 	 * przez liczbę wybranych w ten sposób punktów.
 	 * 
 	 * Taki sposób ma 2 zalety: 1) jest ultraprosty, 2) punkty bliżej pozycji
 	 * Agenta są gęściej rozmieszczone na wycinku, dlatego większe znaczenie ma
 	 * temperatura przy nim. ^_^ (Jeszcze kwestia dobrego dobrania
 	 * {@code dalpha} i {@code dr}).
 	 * 
 	 * @param orientation
 	 *            Kąt między wektorem orientacji Agenta a osią symetrii wycinka
 	 *            koła. Innymi słowy, jak chcemy wycinek po lewej ręce danego
 	 *            Agenta, to dajemy tu 90.0 [deg], jak po prawej to -90.0 [deg].
 	 *            (Dlatego, że kąty w geometrii analitycznej rosną przeciwnie do
 	 *            ruchu wskazówek zegara!).
 	 * @param alpha
 	 *            Rozstaw "ramion" wycinka koła w [deg]. Jak chcemy np. 1/8
 	 *            koła, to dajemy 45.0 [deg], w miarę oczywiste chyba. Być może
 	 *            warto zmienić nazwę tego parametru.
 	 * 
 	 *            Nic nie stoi na przeszkodzie, żeby wywołać tę funkcję z
 	 *            {@code alpha == 0.0} i zdjąć średnią tylko z linii.
 	 * 
 	 *            Można także przyjąć {@code alpha == 360.0} i policzyć średnią
 	 *            z całego otoczenia, np. do wyznaczenia warunków śmierci
 	 *            (zamiast punktowo, tylko na pozycji Agenta). ^_^
 	 * @param r
 	 *            Promień koła, na powierzchni wycinka którego obliczamy
 	 *            średnią. (Ale konstrukt językowy ;b).
 	 * @param what
 	 *            O którą wielkość fizyczną nam chodzi.
 	 * @return
 	 */
 	private double getMeanPhysics(double orientation, double alpha, double r,
 			Physics.Type what) {
 		if (alpha < 0)
 			throw new IllegalArgumentException("alpha < 0");
 		if (r < 0)
 			throw new IllegalArgumentException("r < 0");
 
 		double dalpha = 10; // [deg]
 		double dr = 0.5; // [m]
 
 		double alphaA = phi + orientation - alpha / 2;
 		double alphaB = phi + orientation + alpha / 2;
 		double rA = 0;
 		double rB = r;
 
 		double sum = 0.0;
 		long num = 0;
 
 		alpha = alphaA;
 		// dlatego jest potrzebna konstrukcja do-while, żeby to wykonało się
 		// przynajmniej raz (nie jestem pewien czy przy kącie zerowym by
 		// zadziałało z użyciem for-a -- błędy numeryczne: nie można porównywać
 		// zmiennoprzecinkowych)
 		do {
 			double sin = Math.sin(Math.toRadians(alpha));
 			double cos = Math.cos(Math.toRadians(alpha));
 			r = rA;
 			do {
 				sum += board.getPhysics(new Point2D.Double(position.x + cos * r,
 						position.y + sin * r), what);
 				num++;
 				r += dr;
 			} while (r <= rB);
 			alpha += dalpha;
 		} while (alpha <= alphaB);
 
 		return sum / num;
 	}
 
 	/**
 	 * Oblicza wspolczynnik zagrozenia dla danego kierunku.
 	 * 
 	 * @param angle
 	 *            potencjalnie obrany kierunek
 	 * @return wspolczynnik atrakcyjnosci dla zadanego kierunku, im wyzszy tym
 	 *         GORZEJ
 	 */
 	private double computeThreatComponent(double angle) {
 		double attractivness_comp = 0.0;
 		double r_ahead = computeMagnitudeByAngle(POW_RADIUS_COEFF,
 				BASE_RADIUS_CALC, angle);
 
 		attractivness_comp += getMeanPhysics(angle, CIRCLE_SECTOR, r_ahead,
 				Physics.Type.TEMPERATURE);
 		return attractivness_comp;
 	}
 
 	/**
 	 * Dzieki tej funkcji mozemy latwo otrzymac odpowiednia dlugosc promienia
 	 * sasiedztwa, zaleznie od tego, pod jakim katem jest ono obrocone.
 	 * 
 	 * @param base
 	 *            podstawa potegowania, ma duzy wplyw na zroznicowanie dlugosci
 	 *            promienia, jako ze zmienia sie ona wykladniczo
 	 * @param angle
 	 * @return dlugosc promienia
 	 */
 	private double computeMagnitudeByAngle(double pow_coeff, double base,
 			double angle) {
 		return pow_coeff
 				* Math.pow(base, (180 - Math.abs(angle)) / CIRCLE_SECTOR);
 	}
 }
