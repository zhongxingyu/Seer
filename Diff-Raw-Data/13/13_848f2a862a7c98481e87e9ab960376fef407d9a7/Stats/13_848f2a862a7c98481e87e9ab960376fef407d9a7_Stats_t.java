 package rpisdd.rpgme.gamelogic.player;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class Stats {
 	// levels and experience
 	private int level = 1;
 	private int totalExp = 0;
 
 	// The player's base stats. To be displayed in "stats" menu
 	private int baseEnergy = 10;
 	private int baseStrength = 1;
 	private int baseIntelligence = 1;
 	private int baseWill = 1;
 	private int baseSpirit = 1;
 
 	public Stats() {
 	}
 
 	public Stats(int aStrength, int aIntel, int aWill, int aSpirit) {
 		this(1, 0, aStrength, aIntel, aWill, aSpirit);
 		baseEnergy = 10;
 	}
 
 	public Stats(int aLevel, int aTotal, int aStrength, int aIntel, int aWill,
 			int aSpirit) {
 		level = aLevel;
 		totalExp = aTotal;
 
 		baseStrength = aStrength;
 		baseIntelligence = aIntel;
 		baseWill = aWill;
 		baseSpirit = aSpirit;
 	}
 
 	// aName and aToNext are just ignored, so don't use this function. I'll
 	// remove it later.
 	@Deprecated
 	public Stats(String aName, int aLevel, int aToNext, int aTotal,
 			int aStrength, int aIntel, int aWill, int aSpirit) {
 		level = aLevel;
 		totalExp = aTotal;
 
 		baseStrength = aStrength;
 		baseIntelligence = aIntel;
 		baseWill = aWill;
 		baseSpirit = aSpirit;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public void incrementLevel() {
 		level++;
 	}
 
 	public int getExp() {
 		return totalExp;
 	}
 
 	public void incExp(int amount) {
 		totalExp += amount;
 	}
 
 	public int getBaseEnergy() {
 		return baseEnergy;
 	}
 
 	public void incBaseEnergy(int amount) {
 		baseEnergy += amount;
 	}
 
 	public int getBaseStr() {
 		return baseStrength;
 	}
 
 	public void incBaseStr(int amount) {
 		baseStrength += amount;
 	}
 
 	public int getBaseInt() {
 		return baseIntelligence;
 	}
 
 	public void incBaseInt(int amount) {
 		baseIntelligence += amount;
 	}
 
 	public int getBaseWill() {
 		return baseWill;
 	}
 
 	public void incBaseWill(int amount) {
 		baseWill += amount;
 	}
 
 	public int getBaseSpr() {
 		return baseSpirit;
 	}
 
 	public void incBaseSpr(int amount) {
 		baseSpirit += amount;
 	}
 
 	public void load(SharedPreferences p) {
 		this.level = p.getInt("level", 1);
 		this.totalExp = p.getInt("exp", 0);
 		this.baseStrength = p.getInt("str", 1);
 		this.baseIntelligence = p.getInt("int", 1);
 		this.baseWill = p.getInt("wil", 1);
 		this.baseSpirit = p.getInt("spi", 1);
 		this.baseEnergy = p.getInt("eng", 1);
 	}
 
 	public void save(Editor e) {
 		e.putInt("level", level);
 		e.putInt("exp", totalExp);
 		e.putInt("str", baseStrength);
 		e.putInt("int", baseIntelligence);
 		e.putInt("wil", baseWill);
 		e.putInt("spi", baseSpirit);
 		e.putInt("eng", baseEnergy);
 	}
 
 	public static class Mod {
 		public final int str;
 		public final int intel;
 		public final int will;
 		public final int spirit;
 
 		private Mod(int str, int intel, int will, int spirit) {
 			this.str = str;
 			this.intel = intel;
 			this.will = will;
 			this.spirit = spirit;
 		}
 
 		public static Mod strMod(int amount) {
 			return new Mod(amount, 0, 0, 0);
 		}
 
 		public static Mod intelMod(int amount) {
 			return new Mod(0, amount, 0, 0);
 		}
 
 		public static Mod willMod(int amount) {
 			return new Mod(0, 0, amount, 0);
 		}
 
 		public static Mod spiritMod(int amount) {
 			return new Mod(0, 0, 0, amount);
 		}
 
 		public static Mod fromJsonObject(JsonObject amounts) {
			JsonElement strAmount = amounts.get("strength");
			JsonElement intAmount = amounts.get("intelligence");
			JsonElement willAmount = amounts.get("will");
			JsonElement spiritAmount = amounts.get("spirit");
			return new Mod((strAmount != null) ? strAmount.getAsInt() : 0,
					(intAmount != null) ? intAmount.getAsInt() : 0,
					(willAmount != null) ? willAmount.getAsInt() : 0,
					(spiritAmount != null) ? spiritAmount.getAsInt() : 0);
 		}
 	}
 }
