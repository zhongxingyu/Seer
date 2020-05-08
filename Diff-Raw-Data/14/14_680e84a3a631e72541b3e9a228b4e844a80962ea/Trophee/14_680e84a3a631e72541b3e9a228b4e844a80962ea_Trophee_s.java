 package Projet.Carte;
 
 public class Trophee extends Carte
 {
     public Trophee (int num, String couleur)
     {
         super(num, couleur);
     }
	
	public String getCouleur()	{	return couleur;	}
	public int getNum()			{	return num;		}
     
     // modifiable comme vous le souhaitez
     public String toString()
     {
         return "Carte de type Trophe" + super.toString();
     }
 }
 
