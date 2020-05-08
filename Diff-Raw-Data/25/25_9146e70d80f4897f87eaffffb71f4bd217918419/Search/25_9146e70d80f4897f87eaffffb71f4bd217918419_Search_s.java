 package search;
 
 import index.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class Search {
 	//Cette classe lance des seekers qui vont chacun chercher un mot dans le fichier texte et en font le bilan
 	private List<String> toSeek;
 	public static IndexBuilder builder;
 	//Expression entree dans le champ recherche
 	public String expression; 
 	public Seeker seeker;
 	public List<String> result;
 	public List<Long> lineResults;
 	/**
 	 * 
 	 * @param expression entree dans le moteur de recherche
 	 */
 	public Search(String expression) {
 		this.toSeek = new ArrayList<String>();
 		this.expression=expression;
 		this.seeker = new Seeker();
 		this.result = new ArrayList<String>();
 		lineResults = new ArrayList<Long>();
 	}
 	/**
 	 * Methode ne servant a rien dans l'etat actuel des choses
 	 * 
 	 * @param word
 	 */
 	/*public void renderSearch(String word)
 	{
 		if(seeker.isPresent(word)){
 			toSeek.addAll(seeker.getFichiers(word));
 		}
 	}*/
 	
 	public void evaluate()
 	{
 		StringTokenizer st = new StringTokenizer(expression, " ");
 	    while (st.hasMoreTokens()) {
 	        
 	    }
 	}
 	
 	public void seek(String word){
 		//test
 		seeker.seek(word);
 	}
 
 	public List<String> getToSeek() {
 		return toSeek;
 	}
 
	public void setToSeek(List<String> toSeek) {
		supprNonIndexe(toSeek);
		this.toSeek = toSeek;
 	}
 	/**
 	 * Supprime les mots de la liste des mots entres s'ils ne sont pas indexes
 	 * @param motsEntres
 	 */
	private void supprNonIndexe(List<String> motsEntres){
 		List<String> nonIndexe = new ArrayList<String>();
		String[] tab = {"et", "ou", "où", "de", "des", "d", "le", "les","l","la","je","il","au","aux","du","un",
				"une","a","à","or","ni","que","si","y","m","mon","ma","mes","me","ne",
				"nous","on","sa","ses","se","qui","s","t","ta","tes","te","il","là","qu","sans","sur"};
 		nonIndexe=Arrays.asList(tab);
 		for (String mot : motsEntres){
			if (nonIndexe.contains(mot)){
				motsEntres.remove(mot);
 			}
 		}
 	}
 	
 	public void toDo(){
 		String[] arguments = expression.split(" ");
 		List<String> motsAChercher =new ArrayList<String>(Arrays.asList(arguments));
 		
 		
 		this.setToSeek(motsAChercher);
 		
 		for(String mot : this.toSeek){
 			this.seek(mot);
 			/*this.renderSearch(arguments[i]);
 			
 			this.result.addAll(seeker.getFichiers(arguments[i]));
 			this.lineResults.addAll(seeker.getLines(arguments[i]));*/
 		}
 	}
 }
