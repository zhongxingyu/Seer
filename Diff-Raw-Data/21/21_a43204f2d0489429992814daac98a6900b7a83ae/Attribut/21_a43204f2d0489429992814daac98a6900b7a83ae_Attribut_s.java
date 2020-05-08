 package modele;
 
 public class Attribut {
 	private String nomBDD;
 	private String nomInterface;
 	private int type;
 	
 	public Attribut(String nomBDD, String nomInterface, int type) {
 		super();
 		this.nomBDD = nomBDD;
 		this.nomInterface = nomInterface;
 		this.type = type;
 	}
 	public String getNomBDD() {
 		return nomBDD;
 	}
 	public String getNomInterface() {
 		return nomInterface;
 	}
 	public int getType() {
 		return type;
 	}
 	
 }
