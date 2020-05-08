 package slick;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.util.Log;
 
 //Il faut tendre la classe BasicGame
 public class Projet2a extends BasicGame {
 
	private static final int xMax = 1366;
	private static final int yMax = 768;
 	private Joueur hero;
 	private ArrayList<Monstre> monstre;
 	private ArrayList<Balle> tir;
 	private ArrayList<Potion> item;
 	private int tour;
 	private Image cursor;
 
 	// Il faut un constructeur de base.
 	// Toutes les erreurs seront catchs au moment de
 	// l'excution. (c'est  dire, dans le main)
 	public Projet2a() throws SlickException {
 		// Initialise le nom de la fenetre ..
 		super("Projet2a");
 		Log.info("fin d'initialisation");
 	}
 
 	// Initialise mes objets. Excut au lancement du jeux.
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		this.item = new ArrayList<Potion>();
 		this.item.add(new Potion(Projet2a.xMax, Projet2a.yMax));
 		this.monstre = new ArrayList<Monstre>();
 		this.monstre.add(new Monstre(Projet2a.xMax, Projet2a.yMax));
 		this.hero = new Joueur(Projet2a.xMax, Projet2a.yMax);
 		this.tir = new ArrayList<Balle>();
 		this.tour = 1;
 		this.cursor = new Image("img/cible.gif");
 		container.setMouseCursor(this.cursor, 10, 10);
 
 	}
 
 	// Mthode appel en boucle. C'est ici que l'on fait vivre nos objets
 	// "non graphique" comme la gestion des touches, de nos proprits etc
 	@Override
 	public void update(GameContainer container, int delta)
 			throws SlickException {
 		this.updateTour();
 		for (Monstre m : this.monstre) {
 			if (m.isHit(this.tir)) {
 				m.setLife(m.getLife() - 1);
 			}
 		}
 
 		if (this.hero.getLife() < 0) {
 			this.hero.setLife(hero.getLifemax());
 		}
 
 		for (Potion p : this.item) {
 			if (p.isLoot(this.hero)) {
 				this.hero.heal(p.value);
 			}
 		}
 
 		for (Monstre m : this.monstre) {
 			if ((hero.getPosX() < m.getPosX() + m.getSizeX() && hero.getPosX()
 					+ hero.getSizeX() > m.getPosX())
 					&& (hero.getPosY() + hero.getSizeY() > m.getPosY() && hero
 							.getPosY() < m.getPosY() + m.getSizeY())) {
 				this.hero.setLife(hero.getLife() - 1);
 			}
 		}
 		for (int i = 0; i < monstre.size(); i++) {
 			if (monstre.get(i).isDead()) {
 				monstre.remove(i);
 			}
 		}
 
 		for (int i = 0; i < item.size(); i++) {
 			if (item.get(i).isUsed()) {
 				item.remove(i);
 			}
 		}
 
 		for (int i = 0; i < tir.size(); i++) {
 			if (tir.get(i).getDistance() <= 0) {
 				tir.remove(i);
 			}
 		}
 
 		if (container.getInput().isKeyPressed((Input.KEY_F1))) {
 			this.monstre.add(new Monstre(Projet2a.xMax, Projet2a.yMax));
 		}
 
 		if (container.getInput().isKeyPressed((Input.KEY_F2))) {
 			this.item.add(new Potion(Projet2a.xMax, Projet2a.yMax));
 		}
 
 		// Si on appuie sur la fleche Haut
 		if (container.getInput().isKeyDown((Input.KEY_DOWN))) {
 			if (this.tour % 2 == 0) {
 				this.hero.setPosY(this.hero.getPosY() + 0.35f * delta);
 				this.hero.setDirection(2);
 				this.hero.setLocation("img/persobas.png");
 				this.hero.setOnMove(true);
 			}
 		}
 		// Si on appuie sur la fleche Bas
 		if (container.getInput().isKeyDown(Input.KEY_UP)) {
 			if (this.tour % 2 == 0) {
 				this.hero.setPosY(this.hero.getPosY() - 0.35f * delta);
 				this.hero.setDirection(0);
 				this.hero.setLocation("img/persohaut.png");
 				this.hero.setOnMove(true);
 			}
 		}
 
 		if (container.getInput().isKeyDown(Input.KEY_RIGHT)) {
 			if (this.tour % 2 == 0) {
 				this.hero.setPosX(this.hero.getPosX() + 0.35f * delta);
 				this.hero.setDirection(1);
 				this.hero.setLocation("img/persodroite.png");
 				this.hero.setOnMove(true);
 			}
 		}
 
 		if (container.getInput().isKeyDown(Input.KEY_LEFT)) {
 			if (this.tour % 2 == 0) {
 				this.hero.setPosX(this.hero.getPosX() - 0.35f * delta);
 				this.hero.setDirection(3);
 				this.hero.setLocation("img/persogauche.png");
 				this.hero.setOnMove(true);
 			}
 		}
 
 		if (container.getInput().isKeyPressed(Input.KEY_SPACE)) {
 
 		}
 
 		if (container.getInput().isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 			Balle b = this.hero.tirer(container.getInput().getMouseX(),
 					container.getInput().getMouseY());
 			b.start();
 			this.tir.add(b);
 		}
 		this.hero.initVisuel();
 		this.hero.verifPos(Projet2a.xMax, Projet2a.yMax);
 		if (this.tour % 6 == 0)
 			for (Monstre m : this.monstre) {
 				m.deplacerMonstre(this.hero, delta);
 			}
 	}
 
 	// Mthode appel en boucle. C'est ici qu'on gre l'affichage.
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		g.setColor(Color.white);
 		g.drawString("F1 : Monstre++", 0, 0);
 		g.drawString("F2 : Potion++", 0, 12);
 		for (Monstre m : this.monstre) {
 			m.affich(Projet2a.xMax, Projet2a.yMax, g);
 		}
 		for (Balle b : this.tir) {
 			b.affich(Projet2a.xMax, Projet2a.yMax, g);
 		}
 		for (Potion it : this.item) {
 			it.affich(Projet2a.xMax, Projet2a.yMax, g);
 		}
 		this.hero.affich(Projet2a.xMax, Projet2a.yMax, g);
 		this.hero.showLifeBar(Projet2a.xMax, Projet2a.yMax, g);
 	}
 
 	public void updateTour() {
 		tour++;
 		if (tour > 1000) {
 			tour = 1;
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			// Dmarre un jeux  partir de ma classe
 			
 			Projet2a s = new Projet2a();
 			AppGameContainer app = new AppGameContainer(s);
 			String[] icons = { "img/favicon.png", "img/faviconh.png" };
 			app.setIcons(icons);
 			app.setTargetFrameRate(500);
 			app.setShowFPS(true);
			app.setDisplayMode(Projet2a.xMax, Projet2a.yMax, true);
 			app.start();
 
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 }
