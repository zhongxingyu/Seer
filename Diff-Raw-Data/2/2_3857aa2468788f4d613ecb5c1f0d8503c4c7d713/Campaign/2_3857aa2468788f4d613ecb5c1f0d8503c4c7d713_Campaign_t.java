 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.model;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.model.drawing.DrawablePaint;
 import net.rptools.maptool.model.drawing.DrawableTexturePaint;
 import net.rptools.maptool.model.drawing.DrawnElement;
 
 
 /**
  * This object contains {@link Zone}s and {@link Asset}s that make up a campaign.
  * Roughly this is equivalent to multiple tabs that will appear on the client and
  * all of the images that will appear on it.
  */
 public class Campaign {
 	
 	public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";
 	
     private GUID id = new GUID();
     private Map<GUID, Zone> zones = Collections.synchronizedMap(new LinkedHashMap<GUID, Zone>());
     private ExportInfo exportInfo;
     private Map<String, List<String>> tokenTypeMap;
     
    private transient boolean isBeingSerialized;
 
     public GUID getId() {
         return id;
     }
 
     /**
      * This is a workaround to avoid the renderer and the serializer interating on the drawables at the same time
      */
     public boolean isBeingSerialized() {
 		return isBeingSerialized;
 	}
 
 
 
     /**
      * This is a workaround to avoid the renderer and the serializer interating on the drawables at the same time
      */
 	public void setBeingSerialized(boolean isBeingSerialized) {
 		this.isBeingSerialized = isBeingSerialized;
 	}
 
 
 
 	public List<String> getTokenTypes() {
     	List<String> list = new ArrayList<String>();
     	list.addAll(getTokenTypeMap().keySet());
     	Collections.sort(list);
     	return list;
     }
 
     public List<String> getTokenPropertyList(String tokenType) {
     	return getTokenTypeMap().get(tokenType);
     }
     
     private Map<String, List<String>> getTokenTypeMap() {
     	if (tokenTypeMap == null) {
     		tokenTypeMap = new HashMap<String, List<String>>();
     		tokenTypeMap.put(DEFAULT_TOKEN_PROPERTY_TYPE, createBasicPropertyList());
     	}
     	return tokenTypeMap;
     }
     
     private List<String> createBasicPropertyList() {
     	List<String> list = new ArrayList<String>();
     	list.add("Strength");
     	list.add("Dexterity");
     	list.add("Constitution");
     	list.add("Intelligence");
     	list.add("Wisdom");
     	list.add("Charisma");
     	list.add("HP");
     	list.add("AC");
     	list.add("Defense");
     	list.add("Movement");
     	list.add("Elevation");
     	return list;
     }
     
     public void setExportInfo(ExportInfo exportInfo) {
     	this.exportInfo = exportInfo;
     }
     
     public ExportInfo getExportInfo() {
     	return exportInfo;
     }
     
     public void setId(GUID id) {
         this.id = id;
     }
 
     public List<Zone> getZones() {
         return new ArrayList<Zone>(zones.values());
     }
 
     public Zone getZone(GUID id) {
         return zones.get(id);
     }
 
     public void putZone(Zone zone) {
         zones.put(zone.getId(), zone);
     }
 
     public void removeZone(GUID id) {
         zones.remove(id);
     }
 
     public boolean containsAsset(Asset asset) {
     	return containsAsset(asset.getId());
     }
     
     public boolean containsAsset(MD5Key key) {
     	
     	for (Zone zone : zones.values()) {
     		
     		if (zone.getAssetID() != null && zone.getAssetID().equals(key)) {
     			return true;
     		}
     		
     		for (Token token : zone.getAllTokens()) {
     			
     			if (token.getAssetID().equals(key)) {
     				return true;
     			}
     		}
     		
     		for (DrawnElement drawn : zone.getDrawnElements()) {
     			DrawablePaint paint = drawn.getPen().getPaint(); 
     			if (paint instanceof DrawableTexturePaint) {
     				if (((DrawableTexturePaint)paint).getAssetId().equals(key)) {
     					return true;
     				}
     			}
     			
     			paint = drawn.getPen().getBackgroundPaint();
     			if (paint instanceof DrawableTexturePaint) {
     				if (((DrawableTexturePaint)paint).getAssetId().equals(key)) {
     					return true;
     				}
     			}
     		}
     	}
     	
     	return false;
     }
 	
 }
