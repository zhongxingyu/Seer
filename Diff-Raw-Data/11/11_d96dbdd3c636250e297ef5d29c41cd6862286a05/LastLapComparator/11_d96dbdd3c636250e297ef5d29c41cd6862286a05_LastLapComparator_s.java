 import java.util.Comparator;
 
 public class LastLapComparator implements Comparator<Club> {
 	public int compare(Club club1, Club club2) {
 
 		Lap lap1 = club1.getChamp().getLastRace().getLastLap();
		Lap lap2 = club1.getChamp().getLastRace().getLastLap();
 
 		if(lap2.getSpeed() != lap1.getSpeed()) {
 			return lap2.getSpeed() - lap1.getSpeed();
 		} else if (lap2.getPlusMinusDif() != lap1.getPlusMinusDif()) {
 			return lap2.getPlusMinusDif() - lap1.getPlusMinusDif();
 		} else if (lap2.getMatch().getPlus() != lap1.getMatch().getPlus()) {
 			return lap2.getMatch().getPlus() - lap1.getMatch().getPlus();
 		} else if (lap2.getMatch().getResult() != lap1.getMatch().getResult()) {
 			return lap2.getMatch().getResult() - lap1.getMatch().getResult();
 		} else if (lap2.getGoalsDif() != lap1.getGoalsDif()) {
 			return lap2.getGoalsDif() - lap1.getGoalsDif();
 		} else {
 			return lap2.getMatch().getScoredGoals() 
 					- lap1.getMatch().getScoredGoals();
 		}
 	}
 }
