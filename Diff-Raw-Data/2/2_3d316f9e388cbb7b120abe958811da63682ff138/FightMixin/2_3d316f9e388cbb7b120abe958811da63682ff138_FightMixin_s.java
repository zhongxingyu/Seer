 package cn.edu.tsinghua.academic.c00740273.magictower.standard;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import cn.edu.tsinghua.academic.c00740273.magictower.engine.Coordinate;
 
 public class FightMixin implements RegularTileMixin {
 
 	private static final long serialVersionUID = 1L;
 
 	protected String attackAttributeName;
 	protected String defenseAttributeName;
 	protected String healthAttributeName;
 	protected String deathAttributeName;
 
 	protected long opponentInitialAttack;
 	protected long opponentInitialDefense;
 	protected long opponentInitialHealth;
 
 	@Override
 	public void initialize(JSONObject dataMixinValue) throws JSONException,
 			DataFormatException {
 		this.attackAttributeName = dataMixinValue.getString("attack-attribute");
 		this.defenseAttributeName = dataMixinValue
 				.getString("defense-attribute");
 		this.healthAttributeName = dataMixinValue.getString("health-attribute");
 		this.deathAttributeName = dataMixinValue.getString("death-attribute");
 		this.opponentInitialAttack = dataMixinValue.getLong("attack-opponent");
 		this.opponentInitialDefense = dataMixinValue
 				.getLong("defense-opponent");
 		this.opponentInitialHealth = dataMixinValue.getLong("health-opponent");
 	}
 
 	@Override
 	public boolean enter(StandardEvent event, Coordinate coord,
 			RegularTile tile, Coordinate sourceCoord, CharacterTile sourceTile,
 			StandardGame game) {
 		long opponentAttack = this.opponentInitialAttack;
 		long opponentDefense = this.opponentInitialDefense;
 		long opponentHealth = this.opponentInitialHealth;
 		long selfAttack = ((Number) game.getAttribute(event,
 				this.attackAttributeName)).longValue();
 		long selfDefense = ((Number) game.getAttribute(event,
 				this.defenseAttributeName)).longValue();
 		long selfHealth = ((Number) game.getAttribute(event,
 				this.healthAttributeName)).longValue();
 		Map<String, Long> fightDetails = new HashMap<String, Long>();
 		fightDetails.put("opponent-attack-before", opponentAttack);
 		fightDetails.put("opponent-defense-before", opponentDefense);
 		fightDetails.put("opponent-health-before", opponentHealth);
 		fightDetails.put("self-attack-before", selfAttack);
 		fightDetails.put("self-defense-before", selfDefense);
 		fightDetails.put("self-health-before", selfHealth);
 		if (selfAttack <= opponentDefense) {
 			fightDetails.put("quick-death", -1L);
 			event.setAttributeChange(this.deathAttributeName, -1L);
 			event.addExtraInformation("fight-details", fightDetails);
 			return true;
 		} else {
 			fightDetails.put("quick-death", 0L);
 		}
 		while (opponentHealth > 0) {
 			opponentHealth -= selfAttack - opponentDefense;
			selfHealth -= opponentAttack - selfDefense;
 		}
 		event.setAttributeChange(this.attackAttributeName, selfAttack);
 		event.setAttributeChange(this.defenseAttributeName, selfDefense);
 		event.setAttributeChange(this.healthAttributeName, selfHealth);
 		fightDetails.put("opponent-attack-after", opponentAttack);
 		fightDetails.put("opponent-defense-after", opponentDefense);
 		fightDetails.put("opponent-health-after", opponentHealth);
 		fightDetails.put("self-attack-after", selfAttack);
 		fightDetails.put("self-defense-after", selfDefense);
 		fightDetails.put("self-health-after", selfHealth);
 		event.addExtraInformation("fight-details", fightDetails);
 		return true;
 	}
 }
