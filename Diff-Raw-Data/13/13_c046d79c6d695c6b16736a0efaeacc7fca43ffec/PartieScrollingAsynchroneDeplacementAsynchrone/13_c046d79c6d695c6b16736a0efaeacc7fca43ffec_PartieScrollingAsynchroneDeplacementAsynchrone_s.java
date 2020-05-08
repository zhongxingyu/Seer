 package fr.iutvalence.java.projets.shootemup;
 
 // FIXME préciser la particularité de cette implémentation
 /**
  * Classe partie
  * 
  * @author Deguitre & Pignet
  * 
  */
 public class PartieScrollingAsynchroneDeplacementAsynchrone extends Partie implements Deplacement
 {
 
 	/**
 	 * appel au super constructeur de la classe partie
 	 * 
 	 * @param pseudo
 	 *            pseudo du joueur
 	 * @param joueur
 	 *            interface de controle
 	 * @param affichage
 	 *            interface d'affichage
 	 */
 	public PartieScrollingAsynchroneDeplacementAsynchrone(String pseudo, Joueur joueur, Affichage affichage)
 	{
 		super(pseudo, joueur, affichage);
 	}
 
 	/**
 	 * Débuter une partie
 	 */
 	public void start()
 	{
 		int i = 0;
 		while (this.vies > 0)
 		{
 			if (i == 0)
 			{
 				this.zone.modification(new Position((int) (Math.random() * ((this.zone.getTaille() - 1) + 1)) + 0, 0),
 						ContenuZone.ENNEMI);
 				i = 1;
 			}
 			else
 			{
 				i = 0;
 			}
 			pause(250);
 		}
 	}
 
 	@Override
 	public boolean move(Direction d)
 	{
		this.deplacement(d);
 		return (this.vies >0);
 	}
 
 }
