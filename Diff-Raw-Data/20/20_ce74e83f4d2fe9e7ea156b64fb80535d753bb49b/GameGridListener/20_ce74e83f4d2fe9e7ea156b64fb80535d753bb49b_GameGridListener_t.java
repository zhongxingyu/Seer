 package br.controller.listeners;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.controller.Controller;
 import br.controller.net.ERequest;
 import br.controller.net.Request;
 import br.model.ModelFacade;
 import br.model.logic.GameGrid;
 import br.vue.ViewFacade;
 
 public class GameGridListener implements MouseListener, MouseMotionListener {
 
 	private ModelFacade model;
 	private Controller controller;
 	private ViewFacade view;
 
 	private boolean vertical;
 	private int sizeShip;
 
 	private boolean isPlacing;
 	private boolean isPlaying;
 
 	private List<String> actions;
 
 	public GameGridListener(ModelFacade model, ViewFacade view,
 			Controller controller) {
 		this.isPlacing = false;
 		this.controller = controller;
 		this.model = model;
 		this.vertical = true;
 		this.sizeShip = -1;
 		this.view = view;
 		this.actions = new ArrayList<>();
 	}
 
 	@Override
	public synchronized void mouseClicked(MouseEvent e) {
 		if (isPlacing) {
 			if (e.getButton() == MouseEvent.BUTTON3) {
 				vertical = !vertical;
 				mouseMoved(e);
 			} else if (e.getButton() == MouseEvent.BUTTON1) {
 				if (model.getGrid().isCurrentPositionValid()) {
 					System.out.println("Position valide");
 					this.isPlacing = false;
 					List<String> args = model.getGrid().getCurrent()
 							.getIndexes();
 					controller.makeRequest(new Request(ERequest.PUTSHIP, args));
 				} else {
 					view.getGame()
 							.getLog()
 							.ajouterMessage(
 									"Jeu",
 									"Impossible de placer votre sous-marin à cette position",
 									Color.RED);
 				}
 			}
 		} else if (isPlaying && model.getGrid().hasActionLeft()) {
 			// cas de figure
 			// On click gauche : le modèle renvoie la liste des actions a
 			// effectué
 			if (e.getButton() == MouseEvent.BUTTON1) {
 				actions.addAll(model.getGrid().processClick(getIndexCase(e)));
 				// Si plus d'action après ça : on envoie la requête
 				if (!model.getGrid().hasActionLeft()) {
 					controller
 							.makeRequest(new Request(ERequest.ACTION, actions));
 					actions.clear();
 				}
 			}
 
 			// On click droit : on passe son tour => on envoie la liste au
 			// serveur
 			if (e.getButton() == MouseEvent.BUTTON3) {
 				controller.makeRequest(new Request(ERequest.ACTION, actions));
 				model.getGrid().stopTurn();
 				actions.clear();
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	public void setPlacing(boolean isPlacing) {
 		this.isPlacing = isPlacing;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
	public synchronized void mouseMoved(MouseEvent e) {
 		if (isPlacing) {
 			Point p = getIndexCase(e);
 			model.getGrid().placeTmpShip(p.x, p.y, sizeShip, vertical);
 		} else if (isPlaying) {
 			// mise à jour du chemin?
 		}
 	}
 
 	private Point getIndexCase(MouseEvent e) {
 		int width_game = view.getGame().getGrid().getWidth(), height_game = view
 				.getGame().getGrid().getHeight();
 		double ratio_width = (double) width_game / GameGrid.width;
 		double ratio_height = (double) height_game / GameGrid.height;
 		double indexX = e.getX() / ratio_width;
 		double indexY = e.getY() / ratio_height;
 		indexX -= GameGrid.margin;
 		indexY -= GameGrid.margin;
 		indexX /= (GameGrid.cell_size + 1);
 		indexY /= (GameGrid.cell_size + 1);
 		if ((int) indexX < 0)
 			indexX = 0;
 		else if ((int) indexX > 15)
 			indexX = 15;
 		if ((int) indexY < 0)
 			indexY = 0;
 		else if ((int) indexY > 15)
 			indexY = 15;
 
 		return new Point((int) indexX, (int) indexY);
 	}
 
 	public void setSizeShip(int sizeShip) {
 		this.sizeShip = sizeShip;
 	}
 
 	public void setPlaying(boolean isPlaying) {
 		this.isPlaying = isPlaying;
 	}
 }
