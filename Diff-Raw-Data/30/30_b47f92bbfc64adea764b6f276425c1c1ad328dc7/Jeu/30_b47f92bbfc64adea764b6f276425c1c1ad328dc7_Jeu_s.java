 package l2info.jeuDeLaVie;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 public class Jeu {
 	protected ArrayList<Cellule> listeCellule;
 	protected int type;
 	protected String name;
 	protected int tailleQueue;
 	protected int minX, minY;
 	protected int maxX, maxY;
 	protected int nbGeneration;
 	protected int periode;
 
 	public static final int MORT = 1;
 	public static final int STABLE = 2;
 	public static final int VAISSEAU = 4;
 	public static final int OSCILLATEUR = 3;
 	public static final int INDETERMINE = 5;
 	public static final int MONDE_NORMAL = 10;
 	public static final int MONDE_CIRCULAIRE = 11;
 	public static final int MONDE_FRONTIERES = 12;
 
 	/**
 	 * Constructeur par defaut, créé un nouveau Jeu vide.
 	 */
 	public Jeu() {
 		listeCellule = new ArrayList<Cellule>();
 		type = Jeu.INDETERMINE;
 		this.nbGeneration = 0;
 		this.name = "";
 		this.tailleQueue = 0;
 		this.minX = 999999;
 		this.minY = 999999;
 		this.maxX = -999999;
 		this.maxY = -999999;
 		this.periode = 0;
 	}
 
 	/**
 	 * Créé un Jeu, à partir d'une ArrayList, avec un nom et les coordonnées
 	 * minimal et maximal du Jeu.
 	 * 
 	 * @param name
 	 *            Nom du Jeu.
 	 * @param liste
 	 *            ArrayList contenant la disposition des Cellules.
 	 * @param minX
 	 *            Abscisse à laquelle le terrain commence
 	 * @param minY
 	 *            Ordonnée à laquelle le terrain commence.
 	 * @param maxX
 	 *            Abscisse à laquelle le terrain se termine.
 	 * @param maxY
 	 *            Abscisse à laquelle le terrain se termine.
 	 */
 
 	public Jeu(String name, ArrayList<Cellule> liste, int minX, int minY,
 			int maxX, int maxY) {
 		this.name = name;
 		this.listeCellule = liste;
 		this.type = Jeu.INDETERMINE;
 		this.periode = 0;
 		this.tailleQueue = 0;
 		this.minX = minX;
 		this.minY = minY;
 		this.maxX = maxX;
 		this.maxY = maxY;
 		this.nbGeneration = 0;
 	}
 
 	/**
 	 * Ajoute une Cellule au Jeu.
 	 * 
 	 * @param c
 	 *            Cellule à ajouter au Jeu.
 	 */
 	public void ajouterCellule(Cellule c) {
 		listeCellule.add(c);
 	}
 
 	/**
 	 * Définie la liste de Cellule du Jeu.
 	 * 
 	 * @param c
 	 *            Une ArrayList contenant la disposition des Cellules.
 	 */
 	public void setListeCellule(ArrayList<Cellule> c) {
 		this.listeCellule = c;
 	}
 
 	/**
 	 * Retourne la liste contenant les Cellules du Jeu.
 	 * 
 	 * @return Une ArrayList de Cellule correspondant à la disposition actuelle
 	 *         des Cellules.
 	 */
 	public ArrayList<Cellule> getListeCellule() {
 		return this.listeCellule;
 	}
 
 	/**
 	 * Calcule l'évolution du Jeu sur un nombre de tours donné.
 	 * 
 	 * @param nbTours
 	 *            Nombre de tours à effectuer.
 	 * @param typeMonde
 	 *            Type de monde ( Normal, Frontiere ou Circulaire ) du Jeu.
 	 * @return Le type d'évolution asymptotique du Jeu.
 	 */
 	public int evaluer(int nbTours, int typeMonde,boolean afficher) {
 		int nbGenerationTemoin = 0;
 		ArrayList<Cellule> listeCelTemoin = new ArrayList<Cellule>();
 		for (Cellule e : this.listeCellule) {
 			listeCelTemoin.add(e);
 		}
 
 		for (int i = 0; i < nbTours; i++) {
 			ArrayList<Cellule> temp = this.calculer(this.listeCellule,
 					typeMonde);
 			this.nbGeneration++;
 			if(afficher){
 				System.out.println(this.display());
 				System.out.println("Genereation "+ nbGeneration);
 			}
 			if (this.listeCellule.equals(temp)) {
 				this.type = Jeu.STABLE;
 				this.tailleQueue = this.nbGeneration;
 				this.periode = 1;
 				return Jeu.STABLE;
 			}
 
 			this.listeCellule = temp;
 			listeCelTemoin = this.calculer(listeCelTemoin, typeMonde);
 			listeCelTemoin = this.calculer(listeCelTemoin, typeMonde);
 			nbGenerationTemoin += 2;
 
 			if (this.listeCellule.isEmpty()) {
 				this.type = Jeu.MORT;
 				this.tailleQueue = this.nbGeneration;
 				this.periode = nbGenerationTemoin - this.nbGeneration;
 				return MORT;
 			} else if (this.listeCellule.equals(listeCelTemoin)) {
 				this.type = Jeu.OSCILLATEUR;
 				this.tailleQueue = this.nbGeneration;
 				this.periode = nbGenerationTemoin - this.nbGeneration;
 				return Jeu.OSCILLATEUR;
 			} else if (isTranslation(this.listeCellule, listeCelTemoin)) {
 				this.type = Jeu.VAISSEAU;
 				this.tailleQueue = this.nbGeneration;
 				this.periode = nbGenerationTemoin - this.nbGeneration;
 				return VAISSEAU;
 			}
 		}
 		return INDETERMINE;
 	}
 
 	private boolean isTranslation(ArrayList<Cellule> l1, ArrayList<Cellule> l2) {
 		Iterator<Cellule> it1 = l1.iterator();
 		Iterator<Cellule> it2 = l2.iterator();
 		boolean equal = true;
 		if (l1.size() == l2.size()) {
 			int diffX = it1.next().getX() - it2.next().getX();
 			int diffY = it1.next().getY() - it2.next().getY();
 
 			while (equal && it1.hasNext()) {
 				Cellule c1 = it1.next();
 				Cellule c2 = it2.next();
 				if (c2.getX() + diffX != c1.getX()
 						|| c2.getY() + diffY != c1.getY()) {
 					equal = false;
 				}
 			}
 			return equal;
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * Calcule l'evolution sur un tour des Cellules du Jeu.
 	 * 
 	 * @param listCel
 	 *            ArrayList contenant les Cellule du Jeu
 	 * @param typeMonde
 	 *            Type de monde dans lequel ce passe la simulation.
 	 * @return Une ArrayList contenant la nouvelle disposition des Cellules.
 	 */
 	public ArrayList<Cellule> calculer(ArrayList<Cellule> listCel, int typeMonde) {
		int minX = (this.minX < 999999 ? this.minX : 999999);
		int minY = (this.minY < 999999 ? this.minY : 999999);
		int maxX = (this.maxX > -999999 ? this.maxX : -999999);
		int maxY = (this.maxY > -999999 ? this.maxY : -999999);
		;
 
 		if (listCel.isEmpty()) {
 			return new ArrayList<Cellule>();
 		}
 
 		ListeCellulePotentielle[] listesCellulePot = new ListeCellulePotentielle[9];
 		for (int i = 0; i < 9; i++) {
 			listesCellulePot[i] = new ListeCellulePotentielle();
 		}
 
 		Iterator<Cellule> itCels = listCel.iterator();
 		while (itCels.hasNext()) {
 			Cellule cpTemp = itCels.next();
 			minX = Math.min(minX, cpTemp.getX());
 			minY = Math.min(minY, cpTemp.getY());
 			maxX = Math.max(maxX, cpTemp.getX());
 			maxY = Math.max(maxY, cpTemp.getY());
 
 			listesCellulePot[0].ajouterElement(new CellulePotentielle(cpTemp
 					.getX() - 1, cpTemp.getY() - 1, false, 1));
 			listesCellulePot[1].ajouterElement(new CellulePotentielle(cpTemp.x,
 					cpTemp.y - 1, false, 1));
 			listesCellulePot[2].ajouterElement(new CellulePotentielle(
 					cpTemp.x + 1, cpTemp.y - 1, false, 1));
 			listesCellulePot[3].ajouterElement(new CellulePotentielle(
 					cpTemp.x - 1, cpTemp.y, false, 1));
 			listesCellulePot[4].ajouterElement(new CellulePotentielle(cpTemp.x,
 					cpTemp.y, true, 1));
 			listesCellulePot[5].ajouterElement(new CellulePotentielle(
 					cpTemp.x + 1, cpTemp.y, false, 1));
 			listesCellulePot[6].ajouterElement(new CellulePotentielle(
 					cpTemp.x - 1, cpTemp.y + 1, false, 1));
 			listesCellulePot[7].ajouterElement(new CellulePotentielle(cpTemp.x,
 					cpTemp.y + 1, false, 1));
 			listesCellulePot[8].ajouterElement(new CellulePotentielle(
 					cpTemp.x + 1, cpTemp.y + 1, false, 1));
 
 		}
 
 		ListeCellulePotentielle listeSomme = listesCellulePot[0];
 		for (int i = 1; i < 9; i++) {
 			ListeCellulePotentielle first = listeSomme;
 			this.additionCelPot(first, listesCellulePot[i]);
 		}
 
 		if (typeMonde == Jeu.MONDE_CIRCULAIRE) {
 			ListeCellulePotentielle liste = listeSomme;
 			ListeCellulePotentielle[] exterieur = new ListeCellulePotentielle[4];
 			for (int i = 0; i < exterieur.length; i++) {
 				exterieur[i] = new ListeCellulePotentielle();
 			}
 			while (liste != null) {
 				if (liste.tete().getX() == minX - 1
 						&& liste.tete().getY() >= minY
 						&& liste.tete().getY() <= maxY) {
 					exterieur[0].ajouterElement(new CellulePotentielle(maxX,
 							liste.tete().getY(), false, liste.tete()
 							.getNbVoisin()));
 				} else if (liste.tete().getX() == maxX + 1
 						&& liste.tete().getY() >= minY
 						&& liste.tete().getY() <= maxY) {
 					exterieur[1].ajouterElement(new CellulePotentielle(minX,
 							liste.tete().getY(), false, liste.tete()
 							.getNbVoisin()));
 				} else if (liste.tete().getY() == minY - 1
 						&& liste.tete().getX() >= minX
 						&& liste.tete().getX() <= maxX) {
 					exterieur[2].ajouterElement(new CellulePotentielle(liste
 							.tete().getX(), maxY, false, liste.tete()
 							.getNbVoisin()));
 				} else if (liste.tete().getY() == maxY + 1
 						&& liste.tete().getX() >= minX
 						&& liste.tete().getX() <= maxX) {
 					exterieur[3].ajouterElement(new CellulePotentielle(liste
 							.tete().getX(), minY, false, liste.tete()
 							.getNbVoisin()));
 				}
 				liste = liste.queue();
 			}
 
 			for (int i = 0; i < exterieur.length; i++) {
 				liste = listeSomme;
 				if (exterieur[i].tete() != null)
 					listeSomme = this.additionCelPot(listeSomme, exterieur[i]);
 			}
 		}
 
 		ArrayList<Cellule> lCellule = new ArrayList<Cellule>();
 		while (listeSomme != null) {
 			if (!listeSomme.tete().exists()
 					&& listeSomme.tete().getNbVoisin() == 3) {
 				if (typeMonde == Jeu.MONDE_FRONTIERES
 						|| typeMonde == Jeu.MONDE_CIRCULAIRE) {
 					if (listeSomme.tete().getX() > minX
 							&& listeSomme.tete().getY() >= minY
 							&& listeSomme.tete().getX() <= maxX
 							&& listeSomme.tete().getY() <= maxY) {
 						lCellule.add(new Cellule(listeSomme.tete().getX(),
 								listeSomme.tete().getY()));
 					}
 				} else {
 					lCellule.add(new Cellule(listeSomme.tete().getX(),
 							listeSomme.tete().getY()));
 				}
 
 			} else if (listeSomme.tete().exists()
 					&& (listeSomme.tete().getNbVoisin() == 3 || listeSomme
 					.tete().getNbVoisin() == 4)) {
 				lCellule.add(new Cellule(listeSomme.tete().getX(), listeSomme
 						.tete().getY()));
 			}
 			listeSomme = listeSomme.queue();
 		}
 
 		return lCellule;
 	}
 
 	private ListeCellulePotentielle additionCelPot(ListeCellulePotentielle lc1,
 			ListeCellulePotentielle lc2) {
 		ListeCellulePotentielle temp = lc1;
 		while (lc2 != null) {
 			if (lc1.tete().getY() == lc2.tete().getY()
 					&& lc1.tete().getX() == lc2.tete().getX()) {
 				lc1.tete().ajouterVoisin();
 				if (lc2.tete().exists())
 					lc1.tete().setExist(true);
 				lc2 = lc2.queue();
 			}
 
 			else if (lc1.tete().getY() < lc2.tete().getY()
 					|| (lc1.tete().getY() == lc2.tete().getY() && lc1.tete()
 					.getX() < lc2.tete().getX())) {
 				if (lc1.queue() == null
 						|| (lc1.queue().tete().y > lc2.tete().getY() || (lc1
 								.queue().tete().getY() == lc2.tete().getY() && lc1
 								.queue().tete().getX() > lc2.tete().getX()))) {
 					lc1.insertElementAfter(lc2.tete());
 					lc1 = lc1.queue();
 					lc2 = lc2.queue();
 				} else {
 					if (lc1.queue() != null)
 						lc1 = lc1.queue();
 				}
 			} else {
 				lc1 = lc1.AjouterTete(lc2.tete());
 				temp = lc1;
 				lc2 = lc2.queue();
 			}
 		}
 		return temp;
 	}
 
 	/**
 	 * Génère le code HTML affichant les résultats des simulations d'évolution.
 	 * 
 	 * @param listeJeu
 	 *            ArrayList des Jeux dont on souhaite afficher les résultats.
 	 * @return String contenant le code HTML généré.
 	 */
 	private static StringBuffer toFullHTML(ArrayList<Jeu> listeJeu) {
 		StringBuffer html = new StringBuffer();
 		html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html><head><title>Resultats Jeu de la vie</title></head><body> <table border=\"1\"> <tr><th>Nom</th><th>type</th><th>Taille queue</th></tr><tr>");
 		for (Jeu jeu : listeJeu) {
 			html.append("<td>" + jeu.name + "</td><td>");
 
 			switch (jeu.type) {
 			case Jeu.INDETERMINE:
 				html.append("Indetermine");
 				break;
 			case Jeu.MORT:
 				html.append("Mort");
 				break;
 			case Jeu.OSCILLATEUR:
 				html.append("Oscillateur");
 				break;
 			case Jeu.STABLE:
 				html.append("Stable");
 				break;
 			case Jeu.VAISSEAU:
 				html.append("Vaisseau");
 			}
 			html.append("</td><td>"
 					+ (jeu.tailleQueue > 0 ? jeu.tailleQueue : "Inconnue")
 					+ "</td></tr>");
 		}
 
 		html.append("</table> </body></html");
 		return html;
 	}
 
 	/**
 	 * Initialise les coordonnées mininales du jeu indiquées dans le fichier lif
 	 * 
 	 * @param stk
 	 *            Stringtokenizer ayant pour valeur la ligne du fichier lif
 	 *            contenant les parametres du jeu
 	 */
 	private void ParamGame(StringTokenizer stk) {
 		ArrayList<Integer> nb = new ArrayList<Integer>();
 		while (stk.hasMoreTokens()) {
 			try {
 				nb.add(Integer.parseInt(stk.nextToken()));
 			} catch (Exception e) {
 			}
 		}
 		this.minX = this.maxX = nb.get(0);
 		this.minY = this.maxY = nb.get(1);
 	}
 
 	/**
 	 * Lecture du fichier lif contenant le jeu et ses paramètres
 	 * 
 	 * @param src
 	 * @throws IOException
 	 *             Nom du fchier à lire
 	 */
 	public void loadGame(String src) throws IOException {
 		ArrayList<Cellule> jeu = new ArrayList<Cellule>();
 		this.name = src;
 		Vector<String> monVector = new Vector<String>();
 		BufferedReader b = new BufferedReader(new FileReader(src));
 		String a = b.readLine();
 		while (a != null) {
 			monVector.addElement(a);
 			a = b.readLine();
 		}
 		String s = monVector.firstElement();
 		monVector.removeElementAt(0);
 		StringTokenizer stk = new StringTokenizer(s, " ");
 		this.ParamGame(stk);
 		for (int i = 0; i < monVector.size(); i++) {
 			if (this.maxY < this.maxY + monVector.get(i).length() - 1)
 				this.maxY += monVector.get(i).length() - 1;
 			for (int j = 0; j < monVector.get(i).length(); j++) {
 				if (monVector.get(i).charAt(j) == '*') {
 					jeu.add(new Cellule(this.minX + i, this.minY + j));
 				}
 			}
 		}
 		this.maxX = this.minX + monVector.size() - 1;
 		b.close();
 		this.listeCellule = jeu;
 	}
 
 	public static void GenerateHTML(ArrayList<Jeu> listejeu) throws IOException {
 		BufferedWriter b = new BufferedWriter(new FileWriter("JeuDeLaVie.html"));
 		b.write("" + toFullHTML(listejeu));
 		b.close();
 	}
 
 	// Test si la cellule c existe
 	private boolean IsPresent(Cellule c) {
 		boolean suite = true;
 		Iterator<Cellule> it = listeCellule.listIterator();
 		while (it.hasNext() && suite) {
 			if (c.getX() < it.next().getX()) // Si l'absisse de listeCellule > à
 				// celle de c on retourne faux
 				// car elle existe pas
 				suite = false;
 			else {
 				if (c.equals(it.next()))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	// Affichage du jeu
 	public String display() {
 
 		String tab = "";
 		if(!this.listeCellule.isEmpty()){
 			int xActuel = this.minX;
 			int yActuel = this.minY;
 			for(Cellule c : this.listeCellule){
 
 				while(yActuel < c.getY()){
 					tab += "| ";
 					for(;xActuel<this.maxX; xActuel++){
 						tab += "| ";
 					}
 					tab += "|\n";
 					yActuel++;
 					xActuel = this.minX;
 				}
 
 				while(xActuel < c.getX()){
 					tab += "| ";
 					xActuel++;
 				}
 				
 				tab += "|*";
 				xActuel++;
 				if(xActuel == this.maxX+1){
 					tab += "|";
 				}
 
 			}
 		}
 		return tab;
 	}
 
 }
