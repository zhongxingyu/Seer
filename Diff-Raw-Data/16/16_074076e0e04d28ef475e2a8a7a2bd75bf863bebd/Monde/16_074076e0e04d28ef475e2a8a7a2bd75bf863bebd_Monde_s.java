 package jeu;
 
 import factory.BlocsBlessantFactory;
 import factory.PlateformeFactory;
 import factory.SwitchFactory;
 import interfaces.SlickAdapter;
 import item_joueurs.ItemsRamassable;
 import item_joueurs.PotionEnergie;
 import item_joueurs.PotionMana;
 import item_joueurs.PotionSaut;
 import item_joueurs.PotionVie;
 import item_joueurs.PotionVitesse;
 import items.Baril;
 import items.Caisse;
 import items.Items;
 import items.Poutre;
 
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import net.phys2d.math.Vector2f;
 import net.phys2d.raw.Body;
 import net.phys2d.raw.StaticBody;
 import net.phys2d.raw.World;
 import net.phys2d.raw.shapes.Box;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import blocs.Blocs;
 
 import personnages.Boss;
 import personnages.ChauveSouris;
 import personnages.Ennemis;
 import personnages.Kamikaze;
 import personnages.Serpent;
 import personnages.Personnage;
 import personnages.Robot;
 import weapon.Balle;
 
 /**
  * Equipe RoboTech  Repr�sente le niveau o� le Robot �volue
  */
 public class Monde implements SlickAdapter {
 
 	/**
 	 *  Repr�sente le monde physique
 	 */
 	private World world;
 
 	/**
 	 *  Repr�sente le niveau
 	 */
 	private TiledMap niveau;
 
 	/**
 	 * Les Obstacles
 	 */
 	private ArrayList<Rectangle> obstacles;
 
 	/**
 	 * Fabrique de Plateforme
 	 */
 	private PlateformeFactory f;
 
 	/**
 	 * Fabrique de bouton
 	 */
 	private SwitchFactory s;
 
 	/**
 	 * Fabrique de BlocBlessant
 	 */
 	private BlocsBlessantFactory b;
 
 	/**
 	 *  Les Balles pr�sentes dans le niveau
 	 */
 	protected ArrayList<Balle> balles;
 
 	/**
 	 * Les personnages �voluant dans le niveau La structure nous permet un
 	 * gain de performance dans la recherche de donn�e
 	 */
 	protected HashMap<Body, Personnage> personnages;
 
 	/**
 	 *  Les Items
 	 */
 	protected ArrayList<Items> items;
 
 	/**
 	 *  Les Items qui peuvent �tre rammass
 	 */
 	protected ArrayList<Items> itemsRamassable;
 
 	/**
 	 * Les blocs qui rendent le niveau intractif
 	 */
 	protected ArrayList<Blocs> interaction;
 
 	/**
 	 *  Le robot contrl par le Joueur
 	 */
 	Robot player;
 
 	Animation courir;
 
 	//
 	/**
 	 * Constructeur de la classe Monde
 	 * 
 	 * @throws SlickException
 	 */
 	public Monde() throws SlickException {
 		// initialise les variables de la classe
 		world = null;
 		niveau = null;
 		obstacles = new ArrayList<Rectangle>();
 		personnages = new HashMap<Body, Personnage>();
 		itemsRamassable = new ArrayList<Items>();
 		balles = new ArrayList<Balle>();
 		items = new ArrayList<Items>();
 		interaction = new ArrayList<Blocs>();
 		}
 
 	public void setPlayer(Robot player) {
 		this.player = player;
 	}
 
 	public Robot getPlayer() {
 		return player;
 	}
 
 	/**
 	 * Fonction qui permet d'initialiser le niveau
 	 * 
 	 * @throws SlickException
 	 */
 	public void initialisationMonde() throws SlickException {
 		// monde soumis a la physique
 		world = new World(new Vector2f(0, 20), 20);
 		// chargement de la map (TiledMap)
		niveau = new TiledMap("res/niveau2.tmx");
 
 		// initialise robot sur le niveau
 		initialiserRobot(niveau);
 
 		// genere les plateformes (obstacles) du niveau
 		generatePlateformes();
 		s = new SwitchFactory(world, niveau);
 		f = new PlateformeFactory(world, niveau, s);
 		b = new BlocsBlessantFactory(world, niveau);
 		// genere les objets/personnages du niveau
 		initialiserObjets(niveau);
 		interaction.addAll(f.getProduit());
 		interaction.addAll(s.getProduit());
 		interaction.addAll(b.getProduit());
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		// affiche la map
 		niveau.render(0, 0);
 		// g.setFont(font);
 		// affiche les plateformes (obstacles) du niveau
 		
 		Iterator<Blocs> it4 = interaction.iterator();
 		while (it4.hasNext())
 			it4.next().render(g);
 		// affiche les personnages sur le niveau
 		Iterator<Personnage> it = personnages.values().iterator();
 		while (it.hasNext())
 			it.next().render(gc, sbg, g);
 
 		// affiche les items non ramassable sur le niveau
 		Iterator<Items> it2 = items.iterator();
 		while (it2.hasNext())
 			it2.next().render(gc, sbg, g);
 
 		// affiche les items ramassables sur le niveau
 		it2 = itemsRamassable.iterator();
 		while (it2.hasNext())
 			it2.next().render(gc, sbg, g);
 
 		// affiche les balles tirees sur le niveau
 		Iterator<Balle> it3 = balles.iterator();
 		while (it3.hasNext())
 			it3.next().render(gc, sbg, g);
 	
 	}
 
 	/**
 	 * Retourne le monde physique du niveau
 	 * 
 	 * @return
 	 */
 	public World getWorld() {
 		return world;
 	}
 
 	/**
 	 * Fonction qui génére les plateformes (obstacles) du niveau
 	 */
 	public void generatePlateformes() {
 
 		int largeurMap = niveau.getWidth(); // recupere la largeur de la map
 		int hauteurMap = niveau.getHeight(); // recupere la hauteur de la map
 		int largeurTile = niveau.getTileWidth(); // recupere la largeur d'un
 													// tile
 													// (block de la map)
 		int hauteurTile = niveau.getTileHeight(); // recupere la hauteur d'un
 													// tile
 													// (block de la map)
 
 		// on parcours ligne par ligne
 		for (int y = 0; y < hauteurMap; y++) {
 
 			for (int x = 0; x < largeurMap; x++) {
 
 				int largeurPlateformeDessine = 0; // contient la largeur de la
 													// plateforme sur la ligne a
 													// dessiner
 				int XdepartPlateformeDessine = 0; // contient le debut de la
 													// plateforme sur la ligne
 
 				// si c'est un bloc qui represente une plateforme
 				if (niveau.getTileImage(x, y,
 						niveau.getLayerIndex("BlocsStatiques")) != null) {
 					XdepartPlateformeDessine = x; // conserve la coordonnee X du
 													// debut de la plateforme
 					// rajoute la taille du tile, a la longueur de la plateforme
 					largeurPlateformeDessine += largeurTile;
 
 					// on teste les prochains tile sur la ligne x, pour savoir
 					// s'il appartienne a la meme plateforme a construire
 					while ((x + 1) < largeurMap
 							&& niveau.getTileImage(x + 1, y,
 									niveau.getLayerIndex("BlocsStatiques")) != null) {
 						// a chaque tile rajouter pour construire la plateforme,
 						// on rajoute sa taille a la longueur de la plateforme
 						// totale
 						largeurPlateformeDessine += largeurTile;
 						x++;
 					}
 
 					// une fois qu'on a construit notre plateforme, on cree un
 					// corps statique sur la map qui represente physiquement la
 					// plateforme
 					Body body = new StaticBody("StaticBody_" + x + "_" + y,
 							new Box(largeurPlateformeDessine, hauteurTile));
 					body.setFriction(1f);
 					body.setRestitution(1f);
 					// positionne la plateforme a sa place sur le niveau
 					body.setPosition(
 							(XdepartPlateformeDessine * largeurTile + (largeurPlateformeDessine / 2)),
 							(y * hauteurTile + (hauteurTile / 2)));
 					// rajoute la plateforme au monded physique
 					world.add(body);
 
 					// dessine les contours de la plateforme sur le niveau, pour
 					// vérifier les collisions
 					Rectangle obstacle = new Rectangle(XdepartPlateformeDessine
 							* largeurTile, y * hauteurTile,
 							largeurPlateformeDessine, hauteurTile);
 					// ajoute le contour de la plateforme a la liste des
 					// obstacles
 					obstacles.add(obstacle);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Fonction qui place les objets
 	 * @param map Carte o les objets sont  placer
 	 * @throws SlickException
 	 */
 	public void initialiserObjets(TiledMap map) throws SlickException {
 		for (int i = 0; i < map.getObjectGroupCount(); i++) {
 			for (int j = 0; j < map.getObjectCount(i); j++) {
 
 				if (map.getObjectType(i, j).equals("Switch")) {
 					s.createSwitch(i, j);
 				}
 
 				if (map.getObjectType(i, j).equals("Plateforme_Base")) {
 					f.createPlateforme(i, j);
 				}
 				if (map.getObjectType(i, j).equals("Plateforme_Point"))
 					f.addPosition(i, j);
 
 				if (map.getObjectType(i, j).equals("BlocsBlessant")) {
 					b.createBlocsBlessant(i, j);
 				}
 				if (map.getObjectType(i, j).equals("personnage")) {
 					if (map.getObjectName(i, j).equals("chauveSouris")) {
 						Personnage chauveSouris = new ChauveSouris(
 								map.getObjectX(i, j) + (49 / 2),
 								map.getObjectY(i, j), 2f, 49, this);
 						addPersonnages(chauveSouris);
 					}
 					if (map.getObjectName(i, j).equals("kamikaze")) {
 						Kamikaze k = new Kamikaze(map.getObjectX(i, j)
 								+ (24 / 2), map.getObjectY(i, j), 1.f, 24,
 								this, 100);
 						personnages.put(k.getBody(), k);
 						world.add(k.getBody());
 					}
 
 					if (map.getObjectName(i, j).equals("serpent")) {
 						Personnage chauveSouris = new Serpent(map.getObjectX(i,
 								j) + (49 / 2), map.getObjectY(i, j), 2f, 49,
 								this);
 						addPersonnages(chauveSouris);
 					}
 					if (map.getObjectName(i, j).equals("boss")) {
 						Personnage boss = new Boss(map.getObjectX(i, j)
 								+ (64 / 2), map.getObjectY(i, j), 2f, 64, this);
 						addPersonnages(boss);
 					}
 
 				} else if (map.getObjectType(i, j).equals("ramassable")) {
 					if (map.getObjectName(i, j).equals("potionVie")) {
 
 						ItemsRamassable potionV = new PotionVie(map.getObjectX(
 								i, j) + (10 / 2), map.getObjectY(i, j), 10, 14,
 								0.8f, player, 1);
 						addItemsRamassable(potionV);
 					} else if (map.getObjectName(i, j).equals("potionMana")) {
 
 						ItemsRamassable potionM = new PotionMana(
 								map.getObjectX(i, j) + (10 / 2),
 								map.getObjectY(i, j), 10, 14, 0.8f, player, 1);
 						addItemsRamassable(potionM);
 					} else if (map.getObjectName(i, j).equals("potionEnergie")) {
 
 						ItemsRamassable potionE = new PotionEnergie(
 								map.getObjectX(i, j) + (10 / 2),
 								map.getObjectY(i, j), 10, 14, 0.8f, player, 1);
 						addItemsRamassable(potionE);
 					} else if (map.getObjectName(i, j).equals("potionVitesse")) {
 
 						ItemsRamassable potionV = new PotionVitesse(
 								map.getObjectX(i, j) + (10 / 2),
 								map.getObjectY(i, j), 10, 14, 0.8f, player, 2);
 						addItemsRamassable(potionV);
 					} else if (map.getObjectName(i, j).equals("potionSaut")) {
 
 						ItemsRamassable potionS = new PotionSaut(
 								map.getObjectX(i, j) + (10 / 2),
 								map.getObjectY(i, j), 10, 14, 0.8f, player, 2);
 						addItemsRamassable(potionS);
 					}
 				} else if (map.getObjectType(i, j).equals("objet")) {
 					if (map.getObjectName(i, j).equals("poutre")) {
 
 						Items poutre = new Poutre(map.getObjectX(i, j)
 								+ (25 / 2), map.getObjectY(i, j), 25, 150, 6.5f);
 						addItems(poutre);
 					} else if (map.getObjectName(i, j).equals("caisse")) {
 
 						Items caisse = new Caisse(map.getObjectX(i, j)
 								+ (40 / 2), map.getObjectY(i, j), 40, 40, 8.f);
 						addItems(caisse);
 					} else if (map.getObjectName(i, j).equals("baril")) {
 
 						Items baril = new Baril(
 								map.getObjectX(i, j) + (28 / 2),
 								map.getObjectY(i, j), 28, 40, 3.5f);
 						addItems(baril);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Initialise le robot
 	 * @param map Carte o est situ le robot
 	 * @throws SlickException
 	 */
 	public void initialiserRobot(TiledMap map) throws SlickException {
 		for (int i = 0; i < map.getObjectGroupCount(); i++) {
 			for (int j = 0; j < map.getObjectCount(i); j++) {
 
 				if (map.getObjectType(i, j).equals("personnage")) {
 					if (map.getObjectName(i, j).equals("robot")) {
 						Robot robot = new Robot(
 								map.getObjectX(i, j) + (22 / 2),
 								map.getObjectY(i, j), 1.f, 45, this);
 						player = robot;
 						addPersonnages(robot);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Fonction qui permet d'ajouter de nouveau personnage au niveau physique
 	 * 
 	 * @param pers
 	 *            à ajouter au niveau physique
 	 */
 	public void addPersonnages(Personnage pers) {
 		world.add(pers.getBody());
 		pers.setWorld(world);
 		personnages.put(pers.getBody(), pers);
 	}
 
 	/**
 	 * Fonction qui permet d'ajouter de nouveau items ramassable au niveau
 	 * physique
 	 * 
 	 * @param item
 	 *            ramassable à ajouter au niveau
 	 */
 	public void addItemsRamassable(Items item) {
 		world.add(item.getBody());
 		item.setWorld(world);
 		itemsRamassable.add(item);
 	}
 
 	/**
 	 * Fonction qui permet d'ajouter de nouvelle balles au niveau physique
 	 * 
 	 * @param balle
 	 *            à ajouter au niveau
 	 */
 	public void addBalles(Balle balle) {
 		world.add(balle.getBody());
 		balle.setWorld(world);
 		balles.add(balle);
 	}
 
 	/**
 	 * Fonction qui permet d'ajouter de nouveau items non ramassable au niveau
 	 * physique
 	 * 
 	 * @param item
 	 *            à ajouter au niveau
 	 */
 	public void addItems(Items item) {
 		world.add(item.getBody());
 		item.setWorld(world);
 		items.add(item);
 	}
 
 	/**
 	 * 
 	 * Initialise les différents élements du niveau
 	 * 
 	 * @param container
 	 * @param game
 	 * @param delta
 	 * @throws SlickException
 	 */
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 
 		Iterator<Personnage> it = personnages.values().iterator();
 		while (it.hasNext())
 			it.next().init(container, game);
 
 		Iterator<Items> it2 = items.iterator();
 		while (it2.hasNext())
 			it2.next().init(container, game);
 
 		Iterator<Items> it3 = itemsRamassable.iterator();
 		while (it3.hasNext())
 			it3.next().init(container, game);
 
 		Iterator<Blocs> it4 = interaction.iterator();
 		while (it4.hasNext())
 			it4.next().init(container, game);
 
 	}
 
 	/**
 	 * Met à jour les items ramassés du niveau, vérification à chaque tour
 	 * 
 	 * @param container
 	 * @param game
 	 * @param delta
 	 * @throws SlickException
 	 */
 	public void updateItem(GameContainer container, StateBasedGame game,
 			int delta) throws SlickException {
 		Items current;
 		Iterator<Items> it = itemsRamassable.iterator();
 
 		while (it.hasNext()) {
 			current = it.next();
 			current.update(container, game, delta);
 			if (current.getUsed())
 				it.remove();
 		}
 
 	}
 
 	/**
 	 * Met à jour les personnages du niveau qui sont mort, vérification à
 	 * chaque tour
 	 * 
 	 * @param container
 	 * @param game
 	 * @param delta
 	 */
 	public void updatePersonnage(GameContainer container, StateBasedGame game,
 			int delta) {
 		Personnage current;
 		Iterator<Personnage> it = personnages.values().iterator();
 		while (it.hasNext()) {
 			current = it.next();
 			if (current.getVie() == 0) {
 				world.remove(current.getBody());
 				it.remove();
 			}
 		}
 	}
 
 	/**
 	 * Met à jour les balles du niveau qui ont touchés un element du niveau
 	 * pour les détruires, vérification à chaque tour
 	 * 
 	 * @param container
 	 * @param game
 	 * @param delta
 	 * @throws SlickException
 	 */
 	public void updateBalle(GameContainer container, StateBasedGame game,
 			int delta) throws SlickException {
 		Balle current;
 		Iterator<Balle> it = balles.iterator();
 		while (it.hasNext()) {
 			current = it.next();
 			current.update(container, game, delta);
 			if (current.collision(personnages)) {
 				world.remove(current.getBody());
 				it.remove();
 			}
 		}
 
 	}
 
 	/**
 	 * Met � jour les blocs interagissant avec le player
 	 * 
 	 * @param container
 	 * @param game
 	 * @param delta
 	 */
 	public void updateBlocs(GameContainer container, StateBasedGame game,
 			int delta) {
 		Iterator<Blocs> it = interaction.iterator();
 		Blocs current;
 		while (it.hasNext()) {
 			current = it.next();
 			current.collision(player);
 		
 			try {
 				current.update(container, game, delta);
 			} catch (SlickException e) {
 				System.out.println("SlickException");
 			}
 		}
 
 	}
 
 	/**
 	 * Met à jour le niveau à chaque tour
 	 */
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 		boolean first = true;
 		// Temps total pour la mise a jour du monde physique
 		int tempsTotalMiseAjour = delta;
 		// Temps pour la mise a jour du monde physique
 		int tempsMiseAjour = 5;
 		Iterator<Personnage> it = personnages.values().iterator();
 
 		// fait entre 2 et 3 tours de boucle pour mettre a jour le monde
 		// physique, on fait en premier une preUpdate des personnages, puis une
 		// update de ceux-ci
 
 		while (tempsTotalMiseAjour > tempsMiseAjour) {
 			world.step(tempsMiseAjour * 0.01f); // mise a jour du monde physique
 			tempsTotalMiseAjour -= tempsMiseAjour;
 			if (first) {
 				first = false;
 				while (it.hasNext())
 					it.next().preUpdate(delta);
 			}
 			it = personnages.values().iterator();
 			while (it.hasNext())
 				it.next().update(container, game, tempsMiseAjour);
 
 		}
 
 		updatePersonnage(container, game, delta);
 		updateBalle(container, game, delta);
 		updateItem(container, game, delta);
 		updateBlocs(container, game, delta);
 	}
 
 	/**
 	 * 
 	 * @param x
 	 * @param y
 	 * @param ennemis
 	 * @return
 	 */
 	public boolean estSolPosition(int x, int y, Ennemis ennemis) {
 		boolean result = false;
 		try {
 			int e = x;
 			x = x / niveau.getTileWidth();
 			y = y / niveau.getTileHeight();
 
 			if ((niveau.getTileImage(x, y,
 					niveau.getLayerIndex("BlocsStatiques")) == null)
 					|| (niveau.getTileImage(x - 1, y - 1,
 							niveau.getLayerIndex("BlocsStatiques")) != null)
 					|| (niveau.getTileImage(x + 1, y - 1,
 							niveau.getLayerIndex("BlocsStatiques")) != null)) {
 				result = false;
 			} else
 				result = true;
 
 			for (int i = 0; i < niveau.getObjectGroupCount(); i++) {
 				for (int j = 0; j < niveau.getObjectCount(i); j++) {
 					if (niveau.getObjectType(i, j).equals("ramassable"))
 
 						if (niveau.getObjectX(i, j) + 24 == e
 								&& (niveau.getObjectY(i, j) / niveau
 										.getTileWidth()) + 1 == y
 								&& !ennemis.getDirectionDroite()) {
 							result = false;
 						} else if (niveau.getObjectX(i, j) - 16 == e
 								&& (niveau.getObjectY(i, j) / niveau
 										.getTileWidth()) + 1 == y
 								&& ennemis.getDirectionDroite()) {
 							result = false;
 						}
 				}
 			}
 
 			for (int i = 0; i < niveau.getObjectGroupCount(); i++) {
 				for (int j = 0; j < niveau.getObjectCount(i); j++) {
 					if (niveau.getObjectType(i, j).equals("objet")) {
 						if (niveau.getObjectX(i, j) + 42 == e
 								&& (niveau.getObjectY(i, j) / niveau
 										.getTileWidth()) + 1 == y
 								&& !ennemis.getDirectionDroite()) {
 							result = false;
 						} else if (niveau.getObjectX(i, j) - 15 == e
 								&& (niveau.getObjectY(i, j) / niveau
 										.getTileWidth()) + 1 == y
 								&& ennemis.getDirectionDroite()) {
 
 							result = false;
 						}
 
 					}
 				}
 			}
 
 		} catch (Exception e) {
 
 		}
 		return result;
 	}
 
 }// /
