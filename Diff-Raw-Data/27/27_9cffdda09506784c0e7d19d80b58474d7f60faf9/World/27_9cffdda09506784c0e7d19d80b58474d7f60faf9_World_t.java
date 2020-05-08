 package world;
 
 import java.awt.Point;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 public class World {
 	
 	private static int largeur;
 	private static int hauteur;
 	//map d'élément représentant la carte
 	private static Elements map[][];
 	private static LinkedList<Balle> listeBalle;
 	
 	//constructeur bidon
 	public World(){
 	}
 	
 	//constructeur avec la largeur de la map et de la hauteur
 	public World(int largeur,int hauteur){
 		listeBalle = new LinkedList<Balle>();
 		this.setLargeur(largeur);
 		this.setHauteur(hauteur);
 		World.setMap(new Elements[hauteur][largeur]);
 	}
 	
 	//Permet d'ajouter le robot rentré en paramètre à la map static 
 	public static void ajouter(Robot robot){
 		setMapCase(robot, robot.getPosition());
 	}
 	
 	//Permet d'ajouter une balle à la liste
 	public static void ajouter(Balle balle){
 		listeBalle.add(balle);
 	}
 	
 	//Permet d'enlever une balle à la liste
 	public static void enleverBalle(Point point){
 		Iterator<Balle> iterator = listeBalle.iterator();
 		while(iterator.hasNext()){
 			Balle balle = (Balle) iterator.next();
 			if (balle.position == point){
 				iterator.remove();
 			}
 		}
 	}
 			
 	public static LinkedList<Elements> scan(Robot r){
 	//A ajouter en variable de la méthode pour modifier le rayon du scanner
 	int x = r.getPosition().x;
 	int y = r.getPosition().y;
 	LinkedList<Elements> liste = new LinkedList<Elements>();
 	for(int i = x-3; i < x+3 ; i++){
 		for (int j = y-3; j < y+3 ; j ++){
 			if ( !horsMap(new Point(j, i)) && !caseLibre(new Point(j, i)) && !(i==y && j==x)  ) {
 				liste.add(World.getMap()[i][j]); 
 			}
 		}			
 	}
 	return liste;
 	}
 	
 	
 	//Permet de scanner 1 case autour du robot rentré en paramètre
 	/*public static LinkedList<Elements> scan01(Robot r){
 		//A ajouter en variable de la méthode pour modifier le rayon du scanner
 		int nombreDeCasesAScanner = 1;
 		int x = r.getPosition().x;
 		int y = r.getPosition().y;
 		LinkedList<Elements> liste = new LinkedList<Elements>();
 		for(int i = y -nombreDeCasesAScanner; i <= y+nombreDeCasesAScanner ; i++){
 			for (int j = x - nombreDeCasesAScanner ; j <= x+nombreDeCasesAScanner ; j ++){
 				if ( !horsMap(new Point(j, i)) && !caseLibre(new Point(j, i)) && !(i==y && j==x)  )
 					liste.add(getMap()[i][j]);
 			}			
 		}
 		return liste;
 	}*/
 	
 	//Renvoi vrai si la case est vide (si == null) sinon Faux
 	public static boolean caseLibre(Point position){
 		return (getMap()[position.y][position.x] == null);
 	}
 	
 	public static boolean horsMap(Point position){
 		if (position.x<0|| position.x>(largeur-1) || position.y<0 || position.y>(hauteur-1))
 			return true;
 		else 
 			return false;
 	}
 	
 	public static int getLargeur() {
 		return largeur;
 	}
 
 	private void setLargeur(int largeur) {
 		World.largeur = largeur;
 	}
 
 	public static int getHauteur() {
 		return hauteur;
 	}
 
 	private void setHauteur(int hauteur) {
 		World.hauteur = hauteur;
 	}
 
 	public static Elements[][] getMap() {
 		return map;
 	}
 
 	private static void setMap(Elements[][] map) {
 		World.map = map;
 	}
 	
 	//Permet de placer l'element dans la map au point position
 	public static void setMapCase(Elements element, Point position){
 		try {
 			if (World.caseLibre(position))
 			map[position.y][position.x] = element;
 		} catch (Exception e) {
 			System.out.println("Attention la position rentrée n'est pas valide ");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void modifier(Robot r, Point p){
		getMap()[p.y][p.x] = null;
 		setMapCase(r, r.getPosition());
 	}
 	
 	  public static void toStringMap(){
 		  for (int i = 0 ; i< hauteur ; i++){
 			  for(int j = 0 ; j< largeur ; j++){
 				  System.out.print("Position ligne :"+i+" colonne "+j+":" );
 				  if (World.map[i][j] instanceof Robot)
 					  System.out.println("Un robot est présent");
 				  if (caseLibre(new Point(j, i)))
 					  System.out.println("Aucun robot présent");
 			  }
 		  }
 	  }
 	  
 	  public static void toStringScan (Robot r){
 		  LinkedList<Elements> liste =  World.scan(r);
 			  for(int j = 0 ; j<liste.size()  ; j++){
 				  System.out.println("position : "+liste.get(j).position.x+";"+liste.get(j).position.y);
 				  
 			  }
 	  }
 
 	public static LinkedList<Balle> getListeBalle() {
 		return listeBalle;
 	}
 
 	public static void setListeBalle(LinkedList<Balle> listeBalle) {
 		World.listeBalle = listeBalle;
 	}
 	  	
 	  
 	  	
 }
