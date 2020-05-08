 package ifmo.ru.eugene.luckyanets.OARobot;
 
 /**
  * @author eugene
  * 
  *         Типы кроссовера
  */
 public class Crossover {
 	public enum CrossoverTypes {
 		/**
 		 * Самый обычный кроссовер
 		 */
 		DefaultCrossover,
 		/**
 		 * Кроссовер с одной из особей в «элите»: производится обычный кроссовер,
 		 * при этом данная особь меняется, а особь из «элиты» — нет
 		 */
 		EliteCrossover,
 		/**
 		 * Кроссоовер: два автомата меняются случайными состояниями
 		 */
 		stateCrossover
 	}
 	
	private static int numberOfCrossovers = 3;
 	
 	public static int getNumberOfCrossovers() {
 		return numberOfCrossovers;
 	}
 }
