 package Projet.Carte;
 
 public class Trophee extends Carte
 {
     public Trophee (int num, String couleur)
     {
         super(num, couleur);
     }
     
	public String getCouleur()	{	return super.getCouleur();	}
	public int getNumero()			{	return super.getNumero();		}
	
     // modifiable comme vous le souhaitez
     public String toString()
     {
         return "Carte de type Trophe" + super.toString();
     }
 }
 
