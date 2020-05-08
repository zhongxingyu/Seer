 /**
  * 
  */
 package com.yeegol.DIYWear.res;
 
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.util.SparseIntArray;
 
 import com.yeegol.DIYWear.R;
 import com.yeegol.DIYWear.entity.Model;
 
 /**
  * a static,singleton class for store some never changed data
  * 
  * @author sharl
  * 
  */
 public class DataHolder {
 
 	private static final String TAG = DataHolder.class.getName();
 
 	static DataHolder instance;
 
 	HashMap<String, Integer> charMapping; // workaround for layer-mapping
 
 	SparseIntArray layerMapping;
 
 	static Context context;
 
 	private DataHolder() {
 		layerMapping = new SparseIntArray();
 		charMapping = new HashMap<String, Integer>();
 		charMapping.put("xiezi", R.string.main_type_shoes);
 		charMapping.put("weijin", R.string.main_type_shawl);
 		charMapping.put("waitao", R.string.main_type_coat);
 		charMapping.put("xiazhuang", R.string.main_type_upper_clothes);
 		charMapping.put("shangzhuang", R.string.main_type_down_clothes);
 		charMapping.put("fuzhuang", R.string.main_type_wears);
 		charMapping.put("裤子", Model.DOWN_CLOTHES_LAYER);
 		charMapping.put("外套", Model.COAT_LAYER);
 		charMapping.put("卫衣", Model.COAT_LAYER);
		charMapping.put("连衣裙", Model.UPPER_CLOTHES_LAYER);
 		charMapping.put("半身裙", Model.UPPER_CLOTHES_LAYER);
 		charMapping.put("针织衫", Model.UPPER_CLOTHES_LAYER);
 		charMapping.put("衬衫", Model.UPPER_CLOTHES_LAYER);
 		charMapping.put("T恤", Model.UPPER_CLOTHES_LAYER);
 		charMapping.put("鞋子", Model.SHOES_LAYER);
 		charMapping.put("皮鞋", Model.SHOES_LAYER);
 		charMapping.put("运动鞋", Model.SHOES_LAYER);
 		charMapping.put("单鞋", Model.SHOES_LAYER);
 		charMapping.put("靴子", Model.SHOES_LAYER);
 		charMapping.put("围巾", Model.SHAWL_LAYER);
 		charMapping.put("箱/包/袋", Model.ACCESSORY_LAYER);
 	}
 
 	public int getResIdByPinyin(String s) {
 		return charMapping.get(s);
 	}
 
 	public int getMappingLayerByName(String s) {
 		return charMapping.get(s);
 	}
 
 	public void mapThisToLow(int layer) {
 		layerMapping.clear();
 		for (int i = layer - 1; i > Model.SHOES_LAYER; i--) {
 			if (Model.getInstance().getLayers().get(i) != null) {
 				layerMapping.put(layer, i);
 				layerMapping.put(i, layer);
 				break;
 			}
 		}
 	}
 
 	public void mapThisToHigh(int layer) {
 		layerMapping.clear();
 		for (int i = layer + 1; i < Model.getInstance().getLayers().size(); i++) {
 			if (Model.getInstance().getLayers().get(i) != null) {
 				layerMapping.put(layer, i);
 				layerMapping.put(i, layer);
 				break;
 			}
 		}
 	}
 
 	public int getMappingLayer(int layer) {
 		return layerMapping.get(layer, layer);
 	}
 
 	public static DataHolder getInstance() {
 		if (instance == null) {
 			instance = new DataHolder();
 		}
 		return instance;
 	}
 
 	public static void init(Context c) {
 		context = c;
 		getInstance();
 	}
 
 	public void resetLayerMapping() {
 		layerMapping.clear();
 	}
 
 	public Resources getResource() {
 		return context.getResources();
 	}
 
 	public Context getContext() {
 		return context;
 	}
 }
