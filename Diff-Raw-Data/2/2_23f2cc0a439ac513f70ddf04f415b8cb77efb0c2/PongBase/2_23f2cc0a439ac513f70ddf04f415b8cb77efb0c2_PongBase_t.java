 /*
  *  PongBase.java
  *
  *  Copyright 2011 Kévin Gomez Pinto <contact@kevingomez.fr>
  *                 Jonathan Da Silva <Jonathan.Da_Silva1@etudiant.u-clermont1.fr>
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *  MA 02110-1301, USA.
  */
 
 package game;
 
 import game.objects.Wall;
 import game.objects.Player;
 import game.Constants.State;
 import game.objects.Ball;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import network.Connection;
 
 
 public abstract class PongBase extends JFrame implements KeyListener, Runnable, MouseListener, MouseMotionListener {
 
     /**
      * ID de sérialisation
      */
     private static final long serialVersionUID = -8330079307530116835L;
 
     /**
      * Thread qui sera chargé de la gestion du jeu
      */
     private Thread runner;
 
     /**
      * Connexion au second joueur ou socket serveur
      */
     protected Connection sock;
 
     /**
      * Adresse de l'hôte distant
      */
     private InetAddress distantPlayerHost;
 
     /**
      * Port de l'hôte distant
      */
     private int distantPlayerPort = 6000;
 
     /**
      * Etat actuel du jeu (lancé, en pause, etc.)
      */
     private State state = State.WAITING;
 
     protected Wall wall;
     protected Rectangle wallZone;
 
     private Image offscreeni;
     private Graphics offscreeng;
     protected Rectangle plane;
 
     /**
      * Joueurs
      */
     protected Player player1, player2;
 
     protected Ball ball;
 
     /**
      * Utilisée pour faire clignoter le jeu
      */
     private boolean deathMode = false;
 
 
 	/**
 	 * Lance le jeu
 	 */
 	public final void start() {
         initGUI();
         initGame();
         
 		// démarrage du thread de gestion du jeu
 		startGame();
 	}
 
     protected abstract void initGame();
 
 	/**
 	 * Initialise la partie graphique.
 	 */
 	private void initGUI() {
 		// caractéristiques de la fenêtre
 		setVisible(true);
 		setBounds(100, 100, 640, 480);
 		//setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setResizable(false);
 
 		// ajout des listener
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		addKeyListener(this);
 
         // création des joueurs
         try {
             player1 = new Player(1, Constants.IMG_RACKET_P1);
             player2 = new Player(2, Constants.IMG_RACKET_P2);
         } catch (IOException e) {
             showAlert("Impossible de créer les joueurs : "+e.getMessage());
             System.exit(1);
         }
 
         // placement des joueurs
         player1.setPos(35, getHeight() / 2 - 25);
         player2.setPos(getWidth() - 45, getHeight() / 2 - 25);
 
         // création du plateau de jeu
         offscreeni = createImage(getWidth(), getHeight());
         offscreeng = offscreeni.getGraphics();
 
         wallZone = new Rectangle(Constants.EFFECTS_ZONE_MARGIN,
                                  Constants.EFFECTS_ZONE_MARGIN,
                                  getWidth() - 2 * Constants.EFFECTS_ZONE_MARGIN,
                                  getHeight() - 2 * Constants.EFFECTS_ZONE_MARGIN);
 
         // création du mur
         try {
             wall = new Wall(Constants.IMG_WALL,
                             new Dimension((int) wallZone.getWidth(),
                                           (int) wallZone.getHeight()),
                             Constants.EFFECTS_ZONE_MARGIN);
         } catch (IOException e) {
             showAlert("Impossible de charger le mur : "+e.getMessage());
             System.exit(1);
         }
 
         // chargement de l'image de la balle
         try {
             ball = new Ball(Constants.IMG_BALL);
         } catch (IOException e) {
             showAlert("Impossible de charger la balle : "+e.getMessage());
             System.exit(1);
         }
 
         resetBall();
 
         // zone de jeu
         plane = new Rectangle(15, 15, getWidth(), getHeight() - 30);
 
         // actualisation de l'affichage
         repaint();
 
         // chargement des sons
         loadSounds();
     }
 
     /**
      * Pré-charge les sons de manières à ce qu'ils puissent être joués
      * immédiatement lors de l'appel à Sound.play()
      */
     private void loadSounds() {
         try {
             Sound.load(Constants.SOUND_CONTACT);
             Sound.load(Constants.SOUND_START);
         } catch (Exception ex) {
             showAlert("Impossible de charger les sons : "+ex.getMessage());
         }
     }
 
     public void setDistantHost(String host) throws UnknownHostException {
         distantPlayerHost = InetAddress.getByName(host);
     }
 
     public void setDistantHost(InetAddress host) {
         distantPlayerHost = host;
     }
 
     public void setDistantPort(int port) {
         distantPlayerPort = port;
     }
 
     public InetAddress getDistantHost() {
         return distantPlayerHost;
     }
 
     public int getDistantPort() {
         return distantPlayerPort;
     }
 
     protected abstract Player getMyPlayer();
 
     private boolean myWin() {
         Player winner = (player1.getScore() > player2.getScore()) ? player1 : player2;
 
         return getMyPlayer().equals(winner);
     }
 
     /**
      * Position la balle au centre du terrain, avec une vitesse nulle.
      */
     protected final void resetBall() {
         ball.x = getWidth() / 2;
         ball.y = getHeight() / 2;
         
         ball.setSpeed(0, 0);
     }
 
     /**
      * Crée un thread avec la classe courante.
      * Ce thread sera chargé de mettre à jour l'affichage en fonction
      * des échanges entre le client et le serveur.
      */
     private void startGame() {
         if (runner != null)
             return;
 
         runner = new Thread(this);
 
         try {
             runner.start();
         } catch (IllegalStateException e) {
             showAlert("Impossible de créer le thread du jeu : "+e.getMessage());
             System.exit(1);
         }
     }
 
     /**
      * Envoie un message au joueur distant
      *
      * @param msg Le message à envoyer
      */
     protected final void sendToDistantPlayer(String msg) {
         if(distantPlayerHost == null)
             return;
 
         try {
             sock.send(distantPlayerHost, distantPlayerPort, msg);
         } catch (IOException e) {
             showAlert("Erreur à l'envoi de données vers le client : "+ e);
         }
     }
 
 
     /**
      * Sera appelée lors du début du mode pause
      */
     protected void onGamePause() {
         if(state != State.STARTED)
             return;
 
         changeState(State.PAUSED);
 
         repaint();
     }
 
     /**
      * Sera appelée lors de la sortie du mode pause
      */
     protected void onGameResume() {
         if(state != State.PAUSED)
             return;
 
         changeState(State.STARTED);
 
         repaint();
     }
 
     protected void onGameOver() {
         showAlert(myWin() ? "Vous avez gagné \\o/" : "Vous avez perdu [-_-]\"");
     }
 
     protected void onWallMoved(int x, int y, boolean visible) {}
 
     /**
      * Change l'état actuel du jeu
      *
      * @param newState Nouvel état
      */
     protected void changeState(State newState) {
         state = newState;
 
         // lancement du jeu
         if(newState == State.STARTED && player1.getScore() == 0 && player2.getScore() == 0) {
             Sound.play(Constants.SOUND_START);
         }
     }
 
     /**
      * Retourne l'état actuel du jeu
      *
      * @return L'état courant du jeu
      */
     protected final State currentState() {
         return state;
     }
 
     /**
      * Servira à mettre le jeu en pause lors de l'appui sur les touches P ou p
      *
      * @param e Event lié au clavier
      */
     @Override
     public void keyPressed(KeyEvent e) {
         char c = e.getKeyChar();
 
         if(c != 'p' && c != 'P') {
             return;
         }
 
         if(state == State.PAUSED)
             onGameResume();
         else
             onGamePause();
     }
 
     /**
      * Met à jour la position du pavé du joueur 2 par rapport
      * aux mouvements de la souris
      *
      * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
      *
      * @param e Event lié à la souris
      */
     @Override
     public void mouseMoved(MouseEvent e) {
         Player player = getMyPlayer();
 
         player.y = e.getY() - 25;
         sendToDistantPlayer(String.format("%s %s %s", Constants.MSG_MOVE,
                                                       player, player.y));
     }
 
     @Override
     public void mouseClicked(MouseEvent e) { }
 
     /**
      * Effectue un sleep
      *
      * @param delay Nombre de millisecondes à attendre
      */
     protected final void wait(int delay) {
         try {
             Thread.sleep(delay);
         } catch (InterruptedException e) {
             // rien
         }
     }
 
     /**
      * Analyse un message transmis par le réseau pour
      * exécuter la méthode qui va bien.
      *
      * @param cmd Message à analyser
      */
     protected final void executeCmd(String cmd) {
         String[] args = cmd.split(" ");
         String cmdName = args[0];
 
         switch(args.length) {
             case 1:
                 executeOneArgCmd(cmdName);
                 break;
             case 2:
                 executeTwoArgsCmd(cmdName, args);
                 break;
             case 3:
                 executeThreeArgsCmd(cmdName, args);
                 break;
             case 4:
                 executeFourArgsCmd(cmdName, args);
                 break;
         }
     }
 
     private void executeOneArgCmd(String cmd) {
         if(cmd.equals(Constants.MSG_WALL_TOUCHED)) {
             onWallTouched();
         } else if (cmd.equals(Constants.MSG_CONTACT)) {
             Sound.play(Constants.SOUND_CONTACT);
         }
     }
 
     private void executeTwoArgsCmd(String cmd, String[] args) {
         if (cmd.equals(Constants.MSG_STATE_CHANGED))
             changeState(State.valueOf(args[1]));
     }
 
     private void executeThreeArgsCmd(String cmd, String[] args) {
         if(cmd.equals(Constants.MSG_MOVE)) { // changement de la position des joueurs
             int y = Integer.parseInt(args[2]);
 
             if(args[1].equals("P1"))
                 player1.y = y;
             else
                 player2.y = y;
         } else if(cmd.equals(Constants.MSG_BALL)) { // changement de la position de la balle
             ball.x = Integer.parseInt(args[1]);
             ball.y = Integer.parseInt(args[2]);
         } else if(cmd.equals(Constants.MSG_SCORE)) { // mise à jour des scores
             int score = Integer.parseInt(args[2]);
 
             if(args[1].equals("P1"))
                 player1.setScore(score);
             else
                 player2.setScore(score);
         }
     }
 
     private void executeFourArgsCmd(String cmd, String[] args) {
         if(!cmd.equals(Constants.MSG_WALL_POS))
             return;
 
         int x = Integer.parseInt(args[1]);
         int y = Integer.parseInt(args[2]);
 
         onWallMoved(x, y, args[3].equals("on"));
     }
 
     /**
      * Demande de re-dessiner l'interface
      *
      * @param g Element dans lequel on dessine
      */
     @Override
     public void update(Graphics g) {
         paint(g);
     }
 
     /**
      * Dessine l'interface
      *
      * @param g Element dans lequel on dessine
      */
     @Override
     public void paint(Graphics g) {
         if (offscreeng == null)
             return;
 
         offscreeng.setColor(new Color(244, 122, 0)); // orange foncé
         offscreeng.fillRect(0, 0, getWidth(), getHeight());
         offscreeng.setColor(!deathMode ? Color.white : Color.red);
 
         displayScores();
 
         if (plane != null) {
             offscreeng.clipRect(plane.x, plane.y, plane.width - 28,
                                 plane.height + 1);
             offscreeng.drawRect(plane.x, plane.y, plane.width - 30, plane.height);
 
             // affichage des raquettes
             player1.drawOn(offscreeng);
             player2.drawOn(offscreeng);
 
             // affichage d'un message si besoin
             if(!drawStateMessage()) {
                 drawGroundLines();
 
                 // affichage du mur
                 if(wall.isVisible())
                     wall.drawOn(offscreeng);
 
                 // affichage de la balle
                 ball.drawOn(offscreeng);
             }
         }
 
         g.drawImage(offscreeni, 0, 10, this);
     }
 
     /**
      * Affiche le message correspondant à l'état du jeu (s'il y en a un).
      *
      * @return true si un message a été affiché, false sinon
      */
     private boolean drawStateMessage() {
         offscreeng.setFont(new Font("Dialog", Font.BOLD, 40));
         
         switch (state) {
             case WAITING:
                 offscreeng.drawString("En attente ...",
                                       getWidth() / 2 - 90, getHeight() / 2);
                 break;
             case READY:
                 offscreeng.drawString("Prêt ?",
                                       getWidth() / 2 - 40, getHeight() / 2);
                 break;
             case PAUSED:
                 offscreeng.drawString("Pause", getWidth() / 2 - 50, getHeight() / 2 );
                 break;
             case FINISHED:
                 offscreeng.drawString(myWin() ? "You win !" : "Game Over !",
                                       getWidth() / 2 - 110, getHeight() / 2);
 				break;
 			default:
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Dessine les lignes du terrain
 	 */
 	private void drawGroundLines() {
 		int circleRadius = 75;
 		int circleOriginY = getHeight() / 2;
 		int circleOriginX = getWidth() / 2;
 		int thickness = 4;
 
 		drawCircle(offscreeng, circleOriginX, circleOriginY, circleRadius, thickness);
 
 		//creation de la ligne de fond verticale ( drawLine(x1,y1,x2,y2) ) du point (x1,y1) au point (x2,y2)
 		// on en fait plusieurs pour gérer l'épaisseur du trait
 		offscreeng.drawLine(getWidth()/2,getHeight(),getWidth()/2, -getHeight());
 		offscreeng.drawLine(getWidth()/2+1,getHeight(),getWidth()/2+1, -getHeight());
 		offscreeng.drawLine(getWidth()/2-1,getHeight(),getWidth()/2-1, -getHeight());
 	}
 
 	/**
 	 * 	Calls the drawOval method of java.awt.Graphics
 	 *  with a square bounding box centered at specified
 	 *  location with width/height of 2r.
 	 *
 	 * @param g The Graphics object.
 	 * @param x The x-coordinate of the center of the
 	 *          circle.
 	 * @param y The y-coordinate of the center of the
 	 *          circle.
 	 * @param r The radius of the circle.
 	 */
 	private static void drawCircle(Graphics g, int x, int y, int r) {
 		g.drawOval(x-r, y-r, 2*r, 2*r);
 	}
 
 	/**
 	 * 	Draws a circle of radius r at location (x,y) with
 	 *  the specified line width. Note that the radius r
 	 *  is to the <B>center</B> of the doughnut drawn.
 	 *  The outside radius will be r+lineWidth/2 (rounded
 	 *  down). Inside radius will be r-lineWidth/2
 	 *  (rounded down).
 	 *
 	 * @param g The Graphics object.
 	 * @param x The x-coordinate of the center of the
 	 *          circle.
 	 * @param y The y-coordinate of the center of the
 	 *          circle.
 	 * @param r The radius of the circle.
 	 * @param thickness Pen thickness of circle drawn.
 	 */
 	private static void drawCircle(Graphics g, int x, int y, int r, int thickness) {
 		// correction du rayon pour prendre en compte l'épaisseur du trait
 		int radius = r + thickness / 2;
 
 		for(int i=0; i < thickness; i++) {
 			drawCircle(g, x, y, r);
 
 			if (i+1 < thickness) {
 				drawCircle(g, x+1, y, radius-1);
 				drawCircle(g, x-1, y, radius-1);
 				drawCircle(g, x, y+1, radius-1);
 				drawCircle(g, x, y-1, radius-1);
 
 				radius--;
 			}
 		}
 	}
 
 	/**
 	 * Fait clignoter l'interface (par exemple lorsqu'un point
 	 * a été marqué).
 	 */
	private void blink() {
 		for (int i = 3; i > 0; i--) {
 			deathMode = true;
 			repaint();
 
 			wait(300);
 
 			deathMode = false;
 			repaint();
 
 			wait(300);
 		}
 	}
 
 	/**
 	 * Affiche l'état des scores
 	 */
 	private void displayScores() {
 		offscreeng.setFont(new Font("Dialog", Font.BOLD, 14));
 
 		offscreeng.drawString(String.format("Joueur 1 : %d", player1.getScore()),
 							  getWidth() / 10, 35);
 		offscreeng.drawString(String.format("Joueur 2 : %d", player2.getScore()),
 							  4 * getWidth() / 5, 35);
 	}
 
 	/**
 	 * Appelée lorsqu'un mur a été touché.
 	 */
 	protected final void onWallTouched() {
 		displayScores();
 
 		blink();
 	}
 
 	/**
 	 * Affiche une boite de dialogue contenant le message passé en paramètre.
      * Seul le bouton "OK" est proposé par la fenêtre affichée.
      *
      * @param msg Message à afficher dans la fenêtre.
      */
     protected final void showAlert(String msg) {
         JOptionPane.showMessageDialog(this, msg);
     }
 
     /*
      * Les méthodes suivantes sont requises par des interfaces
      * mais ne nous sont pas utiles ...
      */
     @Override
     public void mouseEntered(MouseEvent e) { }
 
     @Override
     public void mouseExited(MouseEvent e) {    }
 
     @Override
     public void mousePressed(MouseEvent e) { }
 
     @Override
     public void mouseReleased(MouseEvent e) { }
 
     @Override
     public void mouseDragged(MouseEvent e) { }
 
     @Override
     public void keyReleased(KeyEvent e) { }
 
     @Override
     public void keyTyped(KeyEvent e) { }
 }
