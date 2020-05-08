 package code.controllers;
 
 import code.gui.railboard.VAnt;
 import code.gui.railboard.VInstrumentPanel;
 import code.model.instruments.VIPlayable;
 import code.model.instruments.VInstrument;
 import code.model.instruments.VKey;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import javax.swing.JButton;
 
 /**
  *
  * @author Jose Carlos
  */
 public class VInstrumentPanelController extends VAbstractController
 {
     private VInstrumentPanel panel;
     private VInstrument loaded;
     private Map<VKey, JButton> antMap;
 
     public VInstrumentPanelController(VInstrumentPanel panel) 
     {
         this.panel = panel;
         this.antMap = new HashMap<VKey, JButton>();
     }
     
     public void loadInstrument(VInstrument toLoad)
     {
         this.loaded = toLoad;
         fillLabels();
     }
     
     public void unloadInstrument()
     {
         this.loaded = null;
         this.panel.removeAll();
         this.antMap.clear();
     }
     
     public VAnt playInstrument(VKey key)
     {
         VAnt triggered = null;
         JButton associated = (JButton)this.antMap.get(key);
         if (associated != null)
         {
             int antXStart = associated.getX();
             int antYStart = this.panel.getY();
             String antLabel = key.getKeySymbol();
             triggered = new VAnt(antLabel, antXStart, antYStart);
             this.loaded.playElement(key);
         }
         return triggered;
     }
     
     private void fillLabels() 
     {
         Set<Map.Entry<VKey, VIPlayable>> elements = this.loaded.getElements().entrySet();
         for (Map.Entry entry : elements)
         {
            String labelText = entry.getKey().toString();
             JButton newElement = new JButton(labelText);
             this.antMap.put((VKey)entry.getKey(), newElement);
             this.panel.add(newElement);
         }
     }
 }
