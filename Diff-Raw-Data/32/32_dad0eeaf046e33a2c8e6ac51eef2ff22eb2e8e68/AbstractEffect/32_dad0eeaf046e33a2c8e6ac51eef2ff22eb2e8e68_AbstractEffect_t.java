 package jcue.domain.audiocue.effect;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import jouvieje.bass.Bass;
 import jouvieje.bass.structures.HFX;
 import jouvieje.bass.utils.Pointer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  *
  * @author Jaakko
  */
 public abstract class AbstractEffect {
     
     private String name;
     
     private ArrayList<HFX> handles;
     private Pointer effectStruct;
     
     private boolean defaultActive, active;
 
     protected int type;
     
     protected LinkedHashMap<String, EffectParameter> params;
 
     public AbstractEffect(int type, String name) {
         this.type = type;
         this.params = new LinkedHashMap<String, EffectParameter>();
         this.name = name;
         
         this.defaultActive = true;
         this.active = true;
         
         this.handles = new ArrayList<HFX>();
     }
     
     public ArrayList<HFX> getHandles() {
         return handles;
     }
 
     public void setHandles(ArrayList<HFX> handles) {
         this.handles = handles;
     }
     
     public Pointer getEffectStruct() {
         return effectStruct;
     }
 
     public void setEffectStruct(Pointer effectStruct) {
         this.effectStruct = effectStruct;
     }
 
     public int getType() {
         return type;
     }
     
     protected void updateEffect() {
         if (this.effectStruct != null && this.handles.size() > 0) {
             for (HFX hfx : this.handles) {
                 Bass.BASS_FXSetParameters(hfx, this.effectStruct);
             }
         }
     }
     
     public ArrayList<EffectParameter> getParameters() {
         return new ArrayList<EffectParameter>(this.params.values());
     }
     
     public ArrayList<String> getParameterKeys() {
         return new ArrayList<String>(this.params.keySet());
     }
 
     public boolean isActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public boolean isDefaultActive() {
         return defaultActive;
     }
 
     public void setDefaultActive(boolean defaultActive) {
         this.defaultActive = defaultActive;
     }
     
     @Override
     public String toString() {
        return name;
     }
     
     
     
     public EffectParameter getParameter(String param) {
         String lowParam = param.toLowerCase();
         
         if (this.params.containsKey(lowParam)) {
             return this.params.get(lowParam);
         }
 
         return null;
     }
     
     public String getName() {
         return name;
     }
     
     public abstract void setParameter(String param, double value);
     public abstract void updateParameters();
 
     public Element toElement(Document doc) {
         Element result = doc.createElement("effect");
 
         //Type
         Element typeElem = doc.createElement("type");
         typeElem.appendChild(doc.createTextNode(Integer.toString(type)));
         result.appendChild(typeElem);
 
         //Name
         Element nameElem = doc.createElement("name");
         nameElem.appendChild(doc.createTextNode(name));
         result.appendChild(nameElem);
 
         //Active by default
         Element activeElem = doc.createElement("active");
         activeElem.appendChild(doc.createTextNode(Boolean.toString(defaultActive)));
         result.appendChild(activeElem);
 
         //Parameters
         Element paramsElem = doc.createElement("parameters");
         for (String s : params.keySet()) {
             Element paramElem = params.get(s).toElement(doc);
             paramElem.setAttribute("key", s);
             paramsElem.appendChild(paramElem);
         }
         result.appendChild(paramsElem);
 
         return result;
     }
 }
