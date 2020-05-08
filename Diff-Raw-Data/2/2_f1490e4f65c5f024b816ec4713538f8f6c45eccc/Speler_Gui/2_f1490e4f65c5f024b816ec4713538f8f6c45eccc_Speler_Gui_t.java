 package ambiorix.gui;
 
 import java.util.Vector;
 
 import ambiorix.spelbord.Pion;
 import ambiorix.spelbord.PionBasis;
 import ambiorix.spelbord.Tegel;
 import ambiorix.spelbord.TegelBasis;
 import ambiorix.spelers.Speler;
 
 public class Speler_Gui {
 	private Speler speler;
 	private Vector<Pion_Gui> pionnen;
 	private Vector<Tegel_Gui> tegels;
 	public Speler_Gui(Speler s)
 	{
 		pionnen = new Vector<Pion_Gui>();
 		tegels = new Vector<Tegel_Gui>();
 		this.speler =s;
 	}
 	public String geefNaam()
 	{
 		return speler.getNaam();
 	}
 	public int geefScore()
 	{
 		return speler.getScore();
 	}
 	public Speler geefSpeler()
 	{
 		return speler;
 	}
 	public Pion_Gui geefPion(int index)
 	{
 		return pionnen.get(index);
 	}
 	public Tegel_Gui geefTegel(int index)
 	{
 		return tegels.get(index);
 	}
 	public int geefAantalPionnen()
 	{
 		return pionnen.size();
 	}
 	public int geefAantalTegels()
 	{
 		return tegels.size();
 	}
 	public void voegPionToe(PionBasis pion)
 	{
 		Pion_Gui nieuwePion = new Pion_Gui(pion);
 		pionnen.add(nieuwePion);
 	}
 	public void voegTegelToe(TegelBasis tegel)
 	{
 		Tegel_Gui nieuweTegel = new Tegel_Gui(tegel);
 		tegels.add(nieuweTegel);
 	}
 	public void verwijderPion(PionBasis pion)
 	{
 		Pion_Gui teVerwijderenPion = null;
 		for( Pion_Gui pg : pionnen)
 		{
 			if(pg.getPion().getID() == pion.getID())
 			{
 				teVerwijderenPion = pg;
 			}
 		}
 		pionnen.remove(teVerwijderenPion);
 	}
 	public void verwijderTegel(TegelBasis tegel)
 	{
 		Tegel_Gui teVerwijderenTegel = null;
 		for( Tegel_Gui tg : tegels)
 		{
 			if(tg.getTegel().getID() == tegel.getID())
 			{
 				teVerwijderenTegel = tg;
 			}
 		}
		tegels.remove(teVerwijderenTegel);
 	}
 }
