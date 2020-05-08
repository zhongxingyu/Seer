 package herbstJennrichLehmannRitter.engine.utils;
 
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Player;
 
 
 public abstract class MagicUtils {
 
 	/**
 	 * MagicUtils should never be instantiated
 	 */
 	private MagicUtils() {}	
 	
 	public static void addValueToStringBuilder(Object value, StringBuilder stringBuilder) {
 		
 		if (stringBuilder.length() > 0) {
 			stringBuilder.append(", ");
 		}
 		stringBuilder.append(value);
 	}
 	
 	public static void addValueToStringBuilder(Object key, Number value, StringBuilder stringBuilder, boolean displayPlus) {
 		if (value == null
 			|| value.doubleValue() == 0d ) {
 			return;
 		}
 		
 		String valueStr = (displayPlus && value.doubleValue() > 0d ) ? "+" : "";
		valueStr += value.toString();
 		String keyStr = key.toString();
 		
 		if (valueStr.length() > 0) {
 			addValueToStringBuilder(valueStr, stringBuilder);
 			stringBuilder.append(' ');
 			stringBuilder.append(keyStr);
 		}
 	}
 	
 	public static void addValueToStringBuilder(Object key, Number value, StringBuilder stringBuilder) {
 		addValueToStringBuilder(key, value, stringBuilder, false);
 	}
 	
 	public static boolean canPlayerEffortCard(Player player, Card card) {
 		return card.getCostBrick() < player.getMine().getStock()
 			&& card.getCostCrystal() < player.getMagicLab().getStock()
 			&& card.getCostMonsters() < player.getDungeon().getStock();
 	}
 }
