 package jcue.ui;
 
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import javax.swing.*;
 import jcue.domain.audiocue.AudioCue;
 import jcue.domain.audiocue.effect.AbstractEffect;
 import jcue.domain.eventcue.EffectEvent;
 import jcue.domain.eventcue.EventCue;
 
 /**
  *
  * @author oppilas
  */
 public class EffectEventPanel extends AbstractEventPanel {
 
     private EffectEvent event;
     
     private JComboBox targetEffectSelect;
     
     private ButtonGroup modeGroup;
     private ArrayList<JRadioButton> modeSelect;
     
     public EffectEventPanel(EventCue cue) {
         super(cue);
         
         this.targetEffectSelect = new JComboBox();
         this.targetEffectSelect.addActionListener(this);
         
         this.modeGroup = new ButtonGroup();
         this.modeSelect = new ArrayList<JRadioButton>();
         
         for (int i = 1; i <= EffectEvent.MODE_COUNT; i++) {
             JRadioButton rb = new JRadioButton(EffectEvent.getModeString(i));
             rb.addActionListener(this);
             
             this.modeSelect.add(rb);
             this.modeGroup.add(rb);
         }
         
         addComponents();
     }
 
     private void addComponents() {
         this.add(new JLabel("Target effect:"), "split 2");
         this.add(targetEffectSelect, "wmin 200, span, wrap");
         
         for (JRadioButton rb : modeSelect) {
             this.add(rb, "wrap");
         }
     }
 
     public void setEvent(EffectEvent event) {
         super.setEvent(event);
         
         this.event = event;
         
         this.update();
     }
 
     @Override
     protected void update() {
         super.update();
         
         if (this.event != null) {
             if (event.getTargetCue() != null) {
                 //Update effect select menu
                 ArrayList<AbstractEffect> effects = event.getTargetCue().getEffectRack().getEffects();
                 AbstractEffect[] tmpArray = new AbstractEffect[effects.size()];
                 AbstractEffect[] effectArray = effects.toArray(tmpArray);
                 
                 this.targetEffectSelect.setModel(new DefaultComboBoxModel(effectArray));
                 
                 //Set current selection
                 targetEffectSelect.setSelectedItem(event.getTargetEffect());
            } else {
                targetEffectSelect.setSelectedItem(null);
            }

            int mode = this.event.getMode();

            for (int i = 1; i <= EffectEvent.MODE_COUNT; i++) {
                if (mode == i) {
                    this.modeSelect.get(i - 1).setSelected(true);
                    break;
                }
             }
         } else {
             targetEffectSelect.setSelectedItem(null);
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         super.actionPerformed(ae);
         
         Object source = ae.getSource();

        if (source == super.targetCueSelect) {
            this.update();
        } else if (source == targetEffectSelect) {
             JComboBox cb = (JComboBox) source;
             
             if (this.event != null) {
                 event.setTargetEffect((AbstractEffect) cb.getSelectedItem());
             }
         }
         
         for (int i = 0; i < EffectEvent.MODE_COUNT; i++) {
             if (source == this.modeSelect.get(i)) {
                 this.event.setMode(i + 1);
                 break;
             }
         }
     }
 }
