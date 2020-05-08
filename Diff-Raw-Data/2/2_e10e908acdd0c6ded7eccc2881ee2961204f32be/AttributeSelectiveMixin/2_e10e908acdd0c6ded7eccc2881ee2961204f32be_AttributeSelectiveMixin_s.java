 package cn.edu.tsinghua.academic.c00740273.magictower.standard;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import cn.edu.tsinghua.academic.c00740273.magictower.engine.Coordinate;
 
 public class AttributeSelectiveMixin implements RegularTileMixin {
 
 	private static final long serialVersionUID = 1L;
 
 	protected static String[] supportedOperators = { "==", "!=", "<", ">",
 			"<=", ">=" };
 	protected String attributeName;
 	protected Map<String, RegularTileMixin> mixins;
 	protected long referenceValue;
 
 	@Override
 	public void initialize(JSONObject dataValue) throws JSONException,
 			DataFormatException {
 		this.mixins = new HashMap<String, RegularTileMixin>();
 		this.attributeName = dataValue.getString("attribute");
 		this.referenceValue = dataValue.getLong("reference");
 		for (String operator : supportedOperators) {
 			JSONObject dataMixinValue = dataValue.optJSONObject(operator);
 			if (dataMixinValue != null) {
 				this.mixins.put(operator, ClassUtils.makeMixin(dataMixinValue));
 			}
 		}
 	}
 
 	@Override
 	public boolean enter(StandardEvent event, Coordinate coord,
 			RegularTile tile, Coordinate sourceCoord, CharacterTile sourceTile,
 			Map<String, Object> args, StandardGame game) {
 		Number valueObj = (Number) game.getAttribute(event, this.attributeName);
 		long value = valueObj.longValue();
 		String[] matchingOperators;
 		if (value < this.referenceValue) {
 			matchingOperators = new String[] { "!=", "<", "<=" };
 		} else if (value > this.referenceValue) {
 			matchingOperators = new String[] { "!=", ">", ">=" };
 		} else {
 			matchingOperators = new String[] { "==", "<=", ">=" };
 		}
 		for (String operator : matchingOperators) {
 			RegularTileMixin mixin = this.mixins.get(operator);
 			if (mixin != null) {
 				mixin.enter(event, coord, tile, sourceCoord, sourceTile, args,
 						game);
 			}
 		}
		return false;
 	}
 }
