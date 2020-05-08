 package logic;
 
 import java.util.List;
 
 public class PrijsBerekenaar {
 
 	List<Vraag>					vragen;
 	int							ronde					= 0;
 	int							score[]					= { 0, 0, 0, 0 };
 	private static final int	AANTAL_VRAGEN_PER_RONDE	= 9;
 	private static final int	MAX_BELASTINGVRIJ		= 454;
 	private static final int[]	GELD_PER_RONDE			= { 20, 25, 30, 35 };
 
 	public PrijsBerekenaar() {
 
 	}
 
 	public int getGeld() {
 		int bedrag = 0;
 
 		for (int i = 0; i < score.length; i++)
 			// Als er in ronde 4 een fout gemaakt is verliest de gebuiker al
 			// zijn geld
 			if (i == 3 && score[i] < AANTAL_VRAGEN_PER_RONDE)
 				return 0;
 			else bedrag += score[i] * GELD_PER_RONDE[i];
 
 		// Betaal belasting indien nodig
 		if (bedrag > MAX_BELASTINGVRIJ) bedrag = bedrag / 100 * 75;
 
 		return bedrag;
 	}
 
 	public int getScore() {
 		int totalScore = 0;
 
 		for (int i = 0; i < score.length; i++)
 			totalScore += score[i] * (i + 1 * 5);
 
 		return totalScore;
 	}
 
 	/**
 	 * Telt het aantal goede vragen
 	 */
 	private void parseVragen() {
 		int i = 0;
 		for (Vraag vraag : vragen) {
			score[i] = vraag.getScore();
 			i++;
 		}
 	}
 
 	public void setRonde(int rondeNummer) {
 		ronde = rondeNummer;
 	}
 
 	public void setVragen(List<Vraag> vragen) {
 		this.vragen = vragen;
 		parseVragen();
 	}
 
 }
