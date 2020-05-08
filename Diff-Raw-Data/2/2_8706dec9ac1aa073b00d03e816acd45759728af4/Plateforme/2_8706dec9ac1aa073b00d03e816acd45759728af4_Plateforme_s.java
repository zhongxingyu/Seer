 package blocs;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import personnages.Robot;
 
 /**
  * <i>Represente une plateforme mouvante</i>
  * 
  * @author Antoine
  * 
  */
 public class Plateforme extends BlocsDynamiques {
 
 	/**
 	 * Les points de sa trajectoire
 	 */
 	ArrayList<Point> Trajectoire;
 	/**
 	 * Le dernier point atteint (initialis  0 par le constructeur
 	 */
 	int pointd;
 	/**
 	 * Taille de la plateforme
 	 */
 	int taille;
 
 	/**
 	 * Le pas de translation horizontal lmentaire pour animer la plateforme
 	 */
 	float epsilon_x;
 
 	/**
 	 * Le pas de translation vertical lmentaire pour animer la plateforme
 	 */
 	float epsilon_y;
 	/**
 	 * La plateforme peut faire un dplacement aller-retour ou boucle
 	 */
 	boolean on_reverse = false;
 	/**
 	 * En marche arrire
 	 */
 	boolean reverse = false;
 	/**
 	 * la vitesse de dpacement de la plateforme
 	 */
 	private float vitesse;
 
 	/**
 	 * marge d'erreur
 	 */
 	Point eps;
 
 	/**
 	 * 
 	 * @param Point_x
 	 *            Abscisses des points  suivre
 	 * @param Point_y
 	 *            Ordonne des points  suivre
 	 * @param box_image
 	 *            Image du rendu de la plateforme
 	 * @param width
 	 *            Largeur de la plateforme
 	 * @param height
 	 *            hauteur de la plateforme
 	 */
 
 	public Plateforme(float Point_x[], float Point_y[], Image box_image,
 			float width, float height) {
 		super(box_image, height, width, new Point(Point_x[0], Point_y[0]));
 		Trajectoire = new ArrayList<Point>();
 		setTrajectoire(Point_x, Point_y);
 		pointd = 0;
 		taille = Trajectoire.size();
 		vitesse = 1;
 		eps = new Point((float) 1e-01, (float) 1e-01);
 		initialise(0, 1);
 	}
 
 	public Plateforme(Point org, Image box_image, float width, float height) {
 		super(box_image, height, width, org);
 		Trajectoire = new ArrayList<Point>();
 		pointd = 0;
 		vitesse = 1;
 		eps = new Point((float) 1e-01, (float) 1e-01);
 	}
 
 	public void setTrajectoire(float Point_x[], float Point_y[]) {
 		for (int i = 0; i < Point_x.length; i++)
 			Trajectoire.add(new Point(Point_x[i], Point_y[i]));
 	}
 
 	public void addPoint(float x, float y) {
 		Trajectoire.add(new Point(x, y));
 	}
 
 	public float get_epsilon_x() {
 		return epsilon_x;
 	}
 
 	public float get_epsilon_y() {
 		return epsilon_y;
 	}
 
 	public float get_vitesse() {
 		return vitesse;
 	}
 
 	public void set_vitesse(float vitesse) {
 		this.vitesse = vitesse;
 	}
 
 	public void set_reverse(boolean reverse) {
 		on_reverse = reverse;
 	}
 
 	/**
 	 * Calcule le pas lmentaire de translation horizontal et vertical
 	 * 
 	 * @param point_current
 	 *            point origine
 	 * @param next_point
 	 *            point destination
 	 */
 	public void initialise(int point_current, int next_point) {
 		float n = vitesse;
 		Point current = Trajectoire.get(point_current);
 		Point next = Trajectoire.get(next_point);
 		epsilon_x = (next.get_x() - current.get_x()) * (n / 100);
 		epsilon_y = (next.get_y() - current.get_y()) * (n / 100);
 
 	}
 
 	/**
 	 * Ralise le chemin aller de la plateforme
 	 */
 	public void aller() {
 		/*
 		 * On s'est rendu compte que on devait utilise un epsilon pour tester
 		 */
 		if (signal) {
 			Point next = Trajectoire.get((pointd + 1) % taille);
 			if (center.near(next, eps)) {
 				pointd = (pointd + 1) % taille;
 				if (pointd == taille - 1)
 					return;
 				initialise(pointd, (pointd + 1) % taille);
 			}
 			if (pointd < taille - 1) {
 				center.add(epsilon_x, epsilon_y);
 				getBody().setPosition(center.get_x(), center.get_y());
 			}
 		}
 	}
 
 	/**
 	 * Ralise le chemin retour de la plateforme
 	 */
 	public void retour() {
 
 		if (signal) {
 			Point previous = Trajectoire.get((pointd - 1) % taille);
 			if (center.near(previous, eps)) {
 				pointd = (pointd - 1) % taille;
 				if (pointd == 0)
 					return;
 				initialise(pointd, (pointd - 1) % taille);
 			}
 			center.add(epsilon_x, epsilon_y);
 			getBody().setPosition(center.get_x(), center.get_y());
 		}
 	}
 
 	/**
 	 * Ralise le dplacement complet de la plateforme
 	 */
 	public void deplacement() {
 
 		if (pointd == taille - 1) {
 			if (on_reverse) {
 				reverse = true;
 				initialise(pointd, (pointd - 1) % taille);
 			}
 		} else if (pointd == 0) {
 			if (on_reverse) {
 				reverse = false;
 				initialise(pointd, (pointd + 1) % taille);
 			}
 		}
 		if (!reverse)
 			aller();
 		else if (reverse)
 			retour();
 	}
 
 	@Override
 	/**
 	 * {@inheritDoc}
 	 */
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		taille = Trajectoire.size();
 		initialise(0, 1);
 
 	}
 
 	@Override
 	/**
 	 * {@inheritDoc}
 	 */
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		deplacement();
 	}
 
 	/**
 	 * {@inheritDoc} <br/>
 	 * <b>Comportement :</b><br />
 	 * Ici on ne rajoute que le dessin de la trajectoire que la plateforme va
 	 * suivre.
 	 */
 	public void render_spec(Graphics g) {
 		g.setColor(Color.gray);
 		Point current;
 		Point next;
 		for (int i = 0; i < taille - 1; i++) {
 			current = Trajectoire.get(i);
 			next = Trajectoire.get(i + 1);
 			g.drawLine(current.get_x(), current.get_y(), next.get_x(),
 					next.get_y());
 		}
 		g.setColor(Color.white);
 	}
 
 	/**
 	 * {@inheritDoc} <br/>
 	 * <b>Comportement :</b><br />
 	 * Ici on ne rajoute que le personnage suit le mouvement du blocs
 	 */
 	public void collision_action(Robot player) {
		if (get_on_bloc() == true && player.getEnMouvement() == false) {
 			player.set_coor(player.getX() + epsilon_x, player.getY()
 					+ epsilon_y);
 		}
 	}
 }
