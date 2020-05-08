 public class Nature {
 	private static final String[] natureList = { "Hardy", "Lonely", "Brave",
 			"Adamant", "Naughty", "Bold", "Docile", "Relaxed", "Impish", "Lax",
 			"Timid", "Hasty", "Serious", "Jolly", "Naive", "Modest", "Mild",
 			"Quiet", "Bashful", "Rash", "Calm", "Gentle", "Sassy", "Careful",
 			"Quirky", };
 	private static final Nature[] natures;
 
 	static {
 		natures = new Nature[25];
 		for (int i = 0; i < 25; i++) {
 			natures[i] = new Nature(i);
 		}
 	}
 	private int natureValue;
 	private NatureStat plusStat;
 	private NatureStat minusStat;
 
 	private Nature(int value) {
 		natureValue = value;
 		int plus = (natureValue / 5) % 5;
 		int minus = natureValue % 5;
 		if (plus == minus) {
 			plusStat = NatureStat.None;
 			minusStat = NatureStat.None;
 		} else {
 			plusStat = NatureStat.getNatureStat(plus);
 			minusStat = NatureStat.getNatureStat(minus);
 		}
 	}
 
 	public static Nature getNature(long pid) {
 		int value = Settings.game.generationIndex() == 2 ? (int) ((pid >> 8) % 25)
 				: (int) (pid % 25);
 		return natures[value];
 	}
 
 	public static Nature getNature(String s) {
 		for (int i = 0; i < natureList.length; i++) {
 			if (natureList[i].equalsIgnoreCase(s))
				return getNature(i);
 		}
		return getNature(0);
 	}
 
 	public String toString() {
 		if (plusStat == NatureStat.None && minusStat == NatureStat.None) {
 			// neutral nature
 			return natureList[natureValue] + " (neutral)";
 		} else {
 			return String.format("%s (+%s, -%s)", natureList[natureValue],
 					plusStat, minusStat);
 		}
 	}
 
 	public NatureStat getPlusStat() {
 		return plusStat;
 	}
 
 	public NatureStat getMinusStat() {
 		return minusStat;
 	}
 }
