 package quarto.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Stack;
 
 import quarto.constante.Constante;
 import quarto.ia.Move;
 
 
 public class Board extends Observable{
 	
 	private List<Pion> pions;
 	private Case[][] cases;
 	private Stack <Object> historicMove;
 	private boolean canSelectedPion;
 	private Case caseWin;
 	private int constante;
 	
 	private long timeStart;
 	
 	private boolean playIa;
 	
 	public void setIa(boolean b) {
 		playIa = b;
 	}
 	
 	public boolean playIa() {
 		return playIa;
 	}
 	
 	/* INITIALISATION
 	 ****************
 	 */
 	/**
 	 * Constructeur
 	 */
 	public Board() {
 		createPions();
 		createCase();
 		historicMove = new Stack<Object>();
 		canSelectedPion = true;
 		
 		timeStart = System.currentTimeMillis();
 		
 	}
 	/**
 	 * Crée les pions de la board
 	 */
 	private void createPions() {
 		pions =  new ArrayList<Pion>();
 		
 		for (int i=0; i<2; i++)
 		{
 			for (int j=0; j<2; j++)
 			{
 				for (int k=0; k<2; k++)
 				{
 					for(int l=0; l<2; l++)
 					{
 						pions.add(new Pion(i<1, j<1, k<1, l<1));
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * Crée les cases de la board
 	 */
 	private void createCase() {
 		cases = new Case [4][4];
 		for(int i=0; i<4; i++) {
 			for(int j=0; j<4; j++) {
 				cases[i][j] = new Case(i, j);
 			}
 		}
 	}
 	
 	/* OPERATION D'UNE PARTIE
 	 ************************ 
 	 */
 	/**
 	 * Bouge le pion selectionné sur la case 
 	 * Deselectionne le pion
 	 * Retire le pion de la liste
 	 * @param c
 	 */
 	public void move(Case c, String player) {
 		Pion p = getSelectedPion();
 		c.addPion(p);
 		pions.remove(p);
 		p.setSelected(false);
 		
 		if(!playIa) {
 			p.change();
 			c.change();
 		}
 		endOfAction(c, BoardField.PION_ACTIF, player);
 		
 		if(winGame(c)) {
 			paintWinCase();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param player
 	 */
 	public void pionSelected(String player) {
 		endOfAction(getSelectedPion(), BoardField.CASE_ACTIVE, player);
 	}
 	
 	/**
 	 * Change le moment de la partie, cad alterne entre selection de pion et pose de pion
 	 * puis met à jour l'interface
 	 * 
 	 * @param typeChange
 	 * @param msg
 	 */
 	private void endOfAction(Object o, boolean typeChange, String msg) {
 		historicMove.push(o);
 		canSelectedPion = !canSelectedPion;
 		change(this);
 		change(msg);
 		change(typeChange);
 	}
 	
 	/**
 	 * Selectionne le pion et deselectionne les autres puis met à jour l'interface
 	 * @param pion
 	 */
 	public void selectPion(Pion pion) {
 		for(Pion p : pions) {
 			p.setSelected(p==pion);
 			if(!playIa)
 				p.change();
 		}
 		change(this);
 		change(BoardField.PION_ACTIF);
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean lastMoveisCase() {
 		return historicMove.lastElement() instanceof Case;
 	}
 	
 	/**
 	 * 
 	 * @param player
 	 */
 	public void undo(String player) {
 		if(lastMoveisCase()) {
 			caseWin = null;
 			Case c = (Case) historicMove.pop();
 			Pion p = c.getPion();
 			c.deletePion();
 			pions.add(p);
 			
 			p.setAvailable(true);
 		
 			p.change();
 			c.change();
 			selectPion(p);
 			pionSelected(player);
			change(player);
 			
 			historicMove.pop();
 		}
 		else {
 			Pion p = (Pion) historicMove.pop();
 			p.setSelected(false);
 			
 			canSelectedPion=true;			
 			
 			change(this);
 			change(player);
 			change(BoardField.PION_ACTIF);
 			
 		}
 	}
 
 	public void change(Object s) {
 		if(!playIa) {
 			setChanged();
 			this.notifyObservers(s);
 			clearChanged();
 		}
 	}
 	
 	
 	/* OPERATION D'INFORMATION
 	 ************************* 
 	 */
 	
 	public int getGameStat() {
 		if(caseWin != null) {
 			return Constante.WINGAME;
 		}
 		else {
 			return Constante.DRAWGAME;
 		}
 	}
 	
 	public int getNumberMove() {
 		if(historicMove.size() == 0) 
 			return 0;
 		else if(lastMoveisCase()) {
 			return historicMove.size()/2;
 		}
 		else{
 			return historicMove.size()-1 /1;
 		}
 	}
 	
 	public long getGameTime() {
 		System.out.println(System.currentTimeMillis()-timeStart);
 		return System.currentTimeMillis()-timeStart;
 	}
 	/**
 	 * 
 	 * @param i
 	 * @param j
 	 * @return
 	 */
 	public Case getCase(int i, int j) {
 		return cases[i][j];
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public List<Pion> getAvailablePions() {
 		return pions;
 	}
 	/**
 	 * 
 	 * @return
 	 */
 	public Pion getSelectedPion() {
 		for(Pion p :pions) {
 			if(p.isSelected()) return p;
 		}
 		return null;
 	}
 	
 	public boolean canSelectedPion() {
 		return getSelectedPion() != null && canSelectedPion;
 	}
 	
 	public boolean isHistoric() {
 		return !historicMove.isEmpty();
 	}
 	
 	public void paintWinCase() {
 		for(int i=0; i<4; i++) {
 			cases[CaseField.getRow(caseWin, i, constante)][CaseField.getLine(caseWin, i, constante)].win();
 		}
 	}
 	
 	public List<Case> getAvailableCase() {
 		List<Case> casesAvailable = new ArrayList<Case>();
 		
 		for(int i=0; i<cases.length; i++) {
 			for(int j=0; j<cases[i].length; j++) {
 				if(!cases[i][j].isPion()) {
 					casesAvailable.add(cases[i][j]);
 				}
 			}
 		}
 		
 		return casesAvailable;
 	}
 	
 	/* TEST DE VICTOIRE
 	 ****************** 
 	 */
 	public boolean winGame(Case c) {
 		for (int i=0; i<4; i++) {
 			if((Filled(c, CaseField.WINTAB[i]) && win(c, CaseField.WINTAB[i]))) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean Filled(Case c, int constante) {
 		boolean res = true;
 		for(int i=0; i<4; i++) {
 			res = res && cases[CaseField.getRow(c, i, constante)][CaseField.getLine(c, i, constante)].isPion();
 		}
 		return res;
 	}
 	
 	private boolean win(Case c, int constante) {
 		for(int i=0; i<4; i++) {
 			if(winBis(c, i, cases[CaseField.getRow(c, i, constante)][CaseField.getLine(c, i, constante)].getPion().getCaract(i), 0, constante)) {
 				caseWin = c;
 				this.constante = constante;
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean winBis(Case c, int i, boolean carac, int next, int constante) {
 		if(next>3) {
 			return true;
 		}
 		else {
 			if(cases[CaseField.getRow(c, next, constante)][CaseField.getLine(c, next, constante)].getPion().getCaract(i) == carac ) {
 				return winBis(c, i, carac, next+1, constante);
 			}
 			else {
 				return false;
 			}
 		}
 	}
 	
 	/* POUR L'IA
 	 *********** 
 	 */
 	
 	public void playMove(Move m) {
 		cases[m.getX()][m.getY()].addPion(m.getPionPose());
 		pions.remove(m.getPionPose());
 		for(Pion p : pions) {
 			if(p.equals(m.getSelectedPion())) {
 				p.setSelected(true);
 			}
 		}
 	}
 	
 	public void undoMove(Move m) {
 		Pion previous = cases[m.getX()][m.getY()].getPion();
 		cases[m.getX()][m.getY()].deletePion();
 		pions.add(previous);
 		
 		for(Pion p : pions) {
 			if(p.equals(m.getSelectedPion())) {
 				p.setSelected(false);
 			}
 			p.setAvailable(true);
 		}
 	}
 	
 	public Case getWinCase() {
 		return caseWin;
 	}
 	
 	public int sizeHistoric() {
 		return historicMove.size();
 	}
 }
