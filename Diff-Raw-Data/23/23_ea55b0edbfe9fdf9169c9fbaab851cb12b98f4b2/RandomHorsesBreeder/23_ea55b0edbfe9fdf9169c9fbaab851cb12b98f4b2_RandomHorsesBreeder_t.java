 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.joda.time.Hours;
 
 import controllers.Horses;
 
 import models.Horse;
 import play.Play;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 
 @OnApplicationStart
 public class RandomHorsesBreeder extends Job {
 
 	private static List<String> horsePrefixes;
 	private static List<String> horseSuffixes;
 
 	static {
 		horsePrefixes = new ArrayList<String>();
 		horseSuffixes = new ArrayList<String>();
 
 		horsePrefixes.addAll(Arrays.asList(new String[] { "windy", "shallow", "black", "freaky", "heavy", "amazing", "great", "superfast", "readily",
 				"lazy", "stumbling", "zorro" }));
 		horseSuffixes.addAll(Arrays.asList(new String[] { "traveller", "night", "shadow", "horse", "rider", "stumper", "humperdink", "legend",
 				"nightmare", "rabbit", "cow", "beast" }));
 	}
 
 	@Override
 	public void doJob() throws Exception {
		if (Play.mode.isDev()) {
 			Horse.deleteAll();
 			generateRandomHorses();
 		} else if (Horse.count() == 0) {
 			generateRandomHorses();
 		}
 	}
 
 	private void generateRandomHorses() {
 		Random random = new Random(new Date().getTime());
 		for (int i = 0; i < 20; i++) {
 			int randomIndex = random.nextInt(horsePrefixes.size());
 			Horse horse = new Horse(horsePrefixes.get(randomIndex) + " " + horseSuffixes.get(randomIndex), random.nextInt(2000));
 			System.out.println("Breeding a random horse: " + horse);
 			horse.save();
 		}
 	}
 
 }
