 package ifmo.ru.eugene.luckyanets.OARobot;
 
 import ifmo.ru.eugene.luckyanets.OARobot.Crossover.CrossoverTypes;
 
 /**
  * @author eugene
  * 
  *         Инициализация процесса и запуск опыта
  */
 public class Main {
	static final int numberOfStepsInGame = 200;
 	static final int numberOfStarts = 20;
 	static final int numberOfGenerations = 25000;
 	static final int generationSize = 300;
 	static final double mutatePossibility = 0.1;
 	static final double eletePart = 0.04;
 	static final String fieldFileName = "fields/standart.field";
 	final static double crossParts[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
 	public static PlayingField field = null;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		field = new PlayingField();
 		field.readField(fieldFileName);
 		Crossover.CrossoverTypes ct[] = new Crossover.CrossoverTypes[1];
 		
 		for (int st = 4; st <= 7; st++) {
 			ct[0] = CrossoverTypes.DefaultCrossover;
 			(new Test(crossParts, ct, st)).startTest();
 			ct[0] = CrossoverTypes.EliteCrossover;
 			(new Test(crossParts, ct, st)).startTest();
 			ct[0] = CrossoverTypes.stateCrossover;
 			(new Test(crossParts, ct, st)).startTest();
 		}
 	}
 }
