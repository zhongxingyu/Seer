 package com.razie.sdk.assets;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.w3c.dom.Element;
 
 import com.razie.assets.FileInventory;
import com.razie.pub.assets.AssetBrief;
import com.razie.pub.assets.AssetKey;
import com.razie.pub.assets.AssetLocation;
 import com.razie.pub.base.data.XmlDoc;
 import com.razie.pub.base.log.Log;
 
 /**
  * proxy generic invnetory, for smart assets. It will keep instances in a map...
  * 
  * TODO detailed docs
  * 
  * @author razvanc
  * 
  */
 public class NewProxyInventory extends FileInventory {
 
    Map<AssetKey, SdkAsset> assets = new HashMap<AssetKey, SdkAsset>();
 
     public NewProxyInventory() {
         Log.logThis("INIT proxyiinv");
     }
 
     /** find this inventory in assetmgr and register each object */
    public void register(AssetKey k, SdkAsset o) {
         assets.put(k, o);
     }
 
     /** use the base and add details */
    public Map<AssetKey, AssetBrief> find(String type, AssetLocation env, boolean recurse) {
        Map<AssetKey, AssetBrief> ret = new HashMap<AssetKey, AssetBrief>();
 
         for (AssetKey k : assets.keySet())
            if (k.getType().equals(type))
                 ret.put(k, assets.get(k).getBrief());
 
         return ret;
     }
 
     public AssetBrief getBrief(AssetKey ref) {
        return get(ref).getBrief();
     }
 
    public SdkAsset get(AssetKey ref) {
         return assets.get(ref);
     }
 
     static void init(XmlDoc file) {
         for (Element e : file.listEntities("/config/newassets/assets")) {
             String xpath = e.getAttribute("xpath");
 
             for (Element o : file.listEntities(xpath)) {
 
             }
         }
     }
 }
