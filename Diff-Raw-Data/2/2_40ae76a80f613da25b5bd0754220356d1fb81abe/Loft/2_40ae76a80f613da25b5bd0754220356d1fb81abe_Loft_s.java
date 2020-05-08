 import java.util.LinkedList;
 
 
 public class Loft {
 
 	protected String nom;
 	protected int[] dimensions;
 	protected LinkedList<Aliment> population;
 	protected LinkedList<Aliment>[][] cases;
 	protected int limiteNbTours;
 
 	@SuppressWarnings("unchecked")
 	public Loft(String nom, int[] dim, LinkedList<Aliment> pop, int limTours){
 		this.nom = nom;
 		this.dimensions = dim;
 		this.population = pop;
 		this.limiteNbTours = limTours;
 		this.cases = new LinkedList[dim[0]][dim[1]];
 		for (int i = 0; i < dim[0]; i++) {
 			for (int j = 0; j < dim[1]; j++) {
 				this.cases[i][j] = new LinkedList<Aliment>();
 			}
 		}
 	}
 
 	public void dessiner(){
		PixelLoft demo = new PixelLoft(this.dimensions);
 		demo.pack();
 		demo.setVisible(true);
 	}
 
 	public void ajouter(Aliment objet){
 		int idMax = 0;
 		for (Aliment obj : population) {
 			if (obj.identifiant>idMax)
 				idMax = obj.identifiant;
 		}
 		if (objet instanceof Neuneu) {
 			objet.identifiant = idMax+1;
 			this.population.add(objet);
 		}
 		this.cases[objet.position[0]][objet.position[1]].add(objet);
 	}
 
 	public void retirer (int id){
 		for (int i = 0; i < population.size(); i++) {
 			if (population.get(i).identifiant == id) {
 				this.population.remove(i);
 			}
 		}
 	}
 
 	public void lancerTour(){
 		for (int i = 0; i < population.size(); i++) {
 			((Neuneu)population.get(i)).vivre();
 		}
 	}
 
 	public String getNom() {
 		return nom;
 	}
 	public void setNom(String nom) {
 		this.nom = nom;
 	}
 	public int[] getDimensions() {
 		return dimensions;
 	}
 	public void setDimensions(int[] dimensions) {
 		this.dimensions = dimensions;
 	}
 	public LinkedList<Aliment> getPopulation() {
 		return population;
 	}
 	public void setPopulation(LinkedList<Aliment> population) {
 		this.population = population;
 	}
 	public LinkedList<Aliment>[][] getCases() {
 		return cases;
 	}
 	public void setCases(LinkedList<Aliment>[][] cases) {
 		this.cases = cases;
 	}
 	public int getLimiteNbTours() {
 		return limiteNbTours;
 	}
 	public void setLimiteNbTours(int limiteNbTours) {
 		this.limiteNbTours = limiteNbTours;
 	}
 }
