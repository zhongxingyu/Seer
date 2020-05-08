 package jcue.domain.audiocue.effect;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import jcue.domain.ProjectFile;
 import jcue.domain.audiocue.AudioCue;
 import jouvieje.bass.Bass;
 import jouvieje.bass.enumerations.BASS_FX_BFX;
 import jouvieje.bass.structures.BASS_BFX_ECHO;
 import jouvieje.bass.structures.HFX;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  *
  * @author Jaakko
  */
 public class EffectRack {
     
     private AudioCue cue;
     
     private ArrayList<AbstractEffect> effects;
 
     public EffectRack(AudioCue cue) {
         this.cue = cue;
         
         this.effects = new ArrayList<AbstractEffect>();
     }
 
     public void addEffect(AbstractEffect effect) {
         if (this.cue != null && this.cue.getAudio() != null) {
             ArrayList<HFX> handles = null;
             handles = this.cue.getAudio().addEffect(effect.getType(), 0);
             
             if (handles != null && !handles.isEmpty()) {
                 effect.setHandles(handles);
                 
                 Bass.BASS_FXGetParameters(handles.get(0), effect.getEffectStruct());
                 effect.updateParameters();
                 
                 effect.updateEffect();
             }
             
             this.effects.add(effect);
             
             this.update();
         }
     }
     
     private void addEffect(AbstractEffect effect, int priority) {
         if (this.cue != null) {
             ArrayList<HFX> handles = null;
             
             if (effect.isActive()) {
                 handles = this.cue.getAudio().addEffect(effect.getType(), priority);
             }
             
             if (handles != null && !handles.isEmpty()) {
                 effect.setHandles(handles);
                 
                 effect.updateEffect();
             }
             
             this.effects.add(effect);
         }
     }
     
     public void removeEffect(AbstractEffect effect) {
         if (this.effects.contains(effect)) {
             this.cue.getAudio().removeEffect(effect);
             
             this.effects.remove(effect);
         }
     }
 
     public ArrayList<AbstractEffect> getEffects() {
         return effects;
     }
 
     public AbstractEffect getEffect(String name) {
         for (AbstractEffect ae : effects) {
             if (ae.getName().equals(name)) {
                 return ae;
             }
         }
 
         return null;
     }
 
     public void resetToDefaults() {
         for (AbstractEffect ae : effects) {
             ae.setActive(ae.isDefaultActive());
         }
         
         update();
     }
     
     public void update() {
         ArrayList<AbstractEffect> tmpEffects = new ArrayList<AbstractEffect>(this.effects);
         
         for (AbstractEffect ae : tmpEffects) {
             removeEffect(ae);
         }
         
         int size = tmpEffects.size();
         int i = 0;
         
         for (AbstractEffect ae : tmpEffects) {
             this.addEffect(ae, size - i);
             
             i++;
         }
     }
     
     public void clear() {
         Iterator<AbstractEffect> it = this.effects.iterator();
         while (it.hasNext()) {
             AbstractEffect ae = it.next();
             
             this.cue.getAudio().removeEffect(ae);
         }
         
         this.effects.clear();
     }
     
     public int size() {
         return this.effects.size();
     }
 
     public Element toElement(Document doc) {
         Element result = doc.createElement("effectrack");
 
         //Effects
         for (AbstractEffect ae : effects) {
             result.appendChild(ae.toElement(doc));
         }
 
         return result;
     }
 
     public void fromElement(Element elem) {
         NodeList effectNodes = elem.getElementsByTagName("effect");
         for (int i = 0; i < effectNodes.getLength(); i++) {
             Element effectElem = (Element) effectNodes.item(i);
 
             if (effectElem.getParentNode() == elem) {
                 int type = Integer.parseInt(ProjectFile.getTagValue("type", effectElem));
                 AbstractEffect effect = null;
 
                 //Create effect
                if (type == BASS_FX_BFX.BASS_FX_BFX_ECHO.asInt()) {
                     effect = new EchoEffect();
                 } else if (type == BASS_FX_BFX.BASS_FX_BFX_REVERB.asInt()) {
                     effect = new ReverbEffect();
                 } else if (type == BASS_FX_BFX.BASS_FX_BFX_LPF.asInt()) {
                     effect = new LowPassFilter(cue.getAudio());
                 } else if (type == BASS_FX_BFX.BASS_FX_BFX_BQF.asInt()) {
                     effect = new HighPassFilter(cue.getAudio());
                 }
 
                 //Load parameters
                 if (effect != null) {
                     addEffect(effect);
 
                     NodeList paramNodes = effectElem.getElementsByTagName("parameter");
                     for (int j = 0; j < paramNodes.getLength(); j++) {
                         Element paramElem = (Element) paramNodes.item(j);
 
                         if (paramElem.getParentNode().getParentNode() == effectElem) {
                             String param = paramElem.getAttribute("key");
                             double value = Double.parseDouble(paramElem.getAttribute("value"));
 
                             effect.setParameter(param, value);
                         }
                     }
 
                     //Set active
                     boolean active = Boolean.parseBoolean(ProjectFile.getTagValue("active", effectElem));
                     effect.setDefaultActive(active);
                 }
             }
         }
     }
 }
