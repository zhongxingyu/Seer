 package aigilas.reactions;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.entities.Elements;
 import aigilas.entities.ReactionMarker;
 import sps.core.Logger;
 import sps.core.Settings;
 import sps.text.ActionText;
 import sps.text.TextManager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ReactionMeter {
     private final BaseCreature _parent;
     private final List<Elements> _elements = new ArrayList<Elements>();
     private final List<ReactionMarker> _markers = new ArrayList<ReactionMarker>();
     private static final int _maxTimer = Settings.get().defaultSpeed * 4;
     private int _reactionTimer = _maxTimer;
 
     public ReactionMeter(BaseCreature parent) {
         _parent = parent;
     }
 
     private void resetComboDisplay() {
         _reactionTimer = _maxTimer;
         for (ReactionMarker _marker : _markers) {
             _marker.setInactive();
         }
         _markers.clear();
         for (Elements element : _elements) {
             _markers.add(new ReactionMarker(_parent, element));
             _markers.get(_markers.size() - 1).loadContent();
         }
     }
 
     public void draw() {
         for (ReactionMarker marker : _markers) {
             marker.draw();
         }
     }
 
     public void add(Elements element) {
         if (!_elements.contains(element)) {
             if (_elements.size() == 2) {
                 if (_elements.get(0).Value > element.Value) {
                     _elements.add(0, element);
                 }
                 else if (_elements.get(1).Value > element.Value) {
                     _elements.add(1, element);
                 }
                 else {
                     _elements.add(element);
                 }
             }
             else if (_elements.size() == 1) {
                 if (_elements.get(0).Value > element.Value) {
                     _elements.add(0, element);
                 }
                 else {
                     _elements.add(element);
                 }
             }
             if (_elements.size() == 0) {
                 _elements.add(element);
             }
             resetComboDisplay();
         }
     }
 
     public void update() {
         for (ReactionMarker marker : _markers) {
             marker.update();
         }
         int key = 0;
         if (_elements.size() == 3) {
             key = _elements.get(0).Value * 100 + _elements.get(1).Value * 10 + _elements.get(2).Value;
         }
         if (_elements.size() == 2) {
             key = _elements.get(0).Value * 10 + _elements.get(1).Value;
         }
         if (_elements.size() == 1) {
             key = _elements.get(0).Value;
         }
         react(key);
     }
 
     private BaseReaction reaction;
 
     private void react(int key) {
         _reactionTimer--;
         if (_reactionTimer <= 0) {
             Reaction reactionId = ReactionRegistry.get().get(key);
             if (reaction != null) {
                 reaction = ReactionFactory.create(reactionId);
                 if (reaction != null) {
                     reaction.affect(_parent);
                     Logger.gameplay(_parent + " affected by " + reactionId.toString());
                     TextManager.add(new ActionText(reactionId.toString(), 10, (int) _parent.getLocation().PosX, (int) _parent.getLocation().PosY));
                 }
             }
             _elements.clear();
             resetComboDisplay();
             _reactionTimer = _maxTimer;
         }
     }
 }
