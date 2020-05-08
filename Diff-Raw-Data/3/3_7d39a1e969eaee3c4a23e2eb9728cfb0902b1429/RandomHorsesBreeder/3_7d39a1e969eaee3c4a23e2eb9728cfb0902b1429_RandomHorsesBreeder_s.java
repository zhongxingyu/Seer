 package domainservices;
 
 import models.Horse;
 import models.HorseNamePrefix;
 import models.HorseNameSuffix;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 public class RandomHorsesBreeder {
 
 	public Horse createRandomHorse() {
 		if (allRandomHorsesAreExhausted()) {
 			throw new RuntimeException("All horses exhausted. Couldn't breed new horse!");
 		}
 
 		List<HorseNamePrefix> horsePrefixes = HorseNamePrefix.findAll();
 		List<HorseNameSuffix> horseSuffixes = HorseNameSuffix.findAll();
 
 		Random random = new Random(new Date().getTime());
 		int randomPrefix = random.nextInt(horsePrefixes.size());
 		int randomSuffix = random.nextInt(horseSuffixes.size());
 		Horse horse = new Horse(horsePrefixes.get(randomPrefix).prefix + " " + horseSuffixes.get(randomSuffix).suffix, random.nextInt(2000));

 		long horseCount = Horse.count("byName", horse.getName());
 		if (horseCount > 0) {
 			return createRandomHorse();
 		}
 
 		return horse;
 	}
 
 	private static boolean allRandomHorsesAreExhausted() {
 		long totalAmountOfPossibleRandomHorses = HorseNamePrefix.count() * HorseNameSuffix.count();
 		long amountOfHorsesInDatabase = Horse.count();
 
 		return totalAmountOfPossibleRandomHorses == amountOfHorsesInDatabase;
 	}
 
 }
