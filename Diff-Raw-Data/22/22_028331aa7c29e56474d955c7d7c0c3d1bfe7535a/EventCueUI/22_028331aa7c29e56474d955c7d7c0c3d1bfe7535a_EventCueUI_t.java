 package jcue.ui;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.ListModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import jcue.domain.AbstractCue;
 import jcue.domain.eventcue.*;
 
 /**
  *
  * @author Jaakko
  */
 public class EventCueUI extends AbstractCueUI implements ListSelectionListener {
     
     private JList eventsList;
     
    private JButton addButton, removeButton;
     private JPopupMenu addMenu;
     private JMenuItem addTransport, addLoop, addMute, addEffect;
     
     private JPanel controlPanel;
     
     private TransportEventPanel transportPanel;
     private MuteEventPanel mutePanel;
     private EffectEventPanel effectPanel;
     
     private EventCue cue;
 
     public EventCueUI() {
         this.eventsList = new JList();
         this.eventsList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
         this.eventsList.addListSelectionListener(this);
         
         //Event adding stuff
         this.addButton = new JButton(new ImageIcon("images/add_small.png"));
         this.addButton.addActionListener(this);
         
         this.addMenu = new JPopupMenu();
         
         this.addTransport = new JMenuItem("Transport event");
         this.addTransport.addActionListener(this);
         this.addMenu.add(addTransport);
         
         this.addMute = new JMenuItem("Mute event");
         this.addMute.addActionListener(this);
         this.addMenu.add(addMute);
         
         this.addLoop = new JMenuItem("Loop event");
         this.addLoop.addActionListener(this);
         this.addMenu.add(addLoop);
         
         this.addEffect = new JMenuItem("Effect event");
         this.addEffect.addActionListener(this);
         this.addMenu.add(addEffect);
         //*******

        this.removeButton = new JButton(new ImageIcon("images/remove_small.png"));
        removeButton.addActionListener(this);
         
         this.controlPanel = new JPanel(new CardLayout());
         
         this.transportPanel = new TransportEventPanel(cue);
         this.mutePanel = new MuteEventPanel(cue);
         this.effectPanel = new EffectEventPanel(cue);
         
         this.controlPanel.add(new JPanel(), "empty");
         this.controlPanel.add(this.transportPanel, "transport");
         this.controlPanel.add(this.mutePanel, "mute");
         this.controlPanel.add(this.effectPanel, "effect");
         
         
         addComponents();
     }
     
     private void addComponents() {
         this.add(this.eventsList, "split 2, span, hmin 150, wmin 100");
         this.add(this.controlPanel, "growx, aligny top, wrap");
         
        this.add(this.addButton, "split 2");
        this.add(this.removeButton);
     }
 
     @Override
     protected void update() {
         super.update();
         
         if (this.cue != null) {
             ArrayList<AbstractEvent> events = this.cue.getEvents();
             AbstractEvent[] tmpArray = new AbstractEvent[events.size()];
             AbstractEvent[] eventArray = events.toArray(tmpArray);
             
             ListModel lm = new DefaultComboBoxModel(eventArray);
             this.eventsList.setModel(lm);
         }
     }
 
     @Override
     protected void setCurrentCue(AbstractCue cue) {
         super.setCurrentCue(cue);
         
         this.cue = (EventCue) cue;
         
         this.transportPanel.setEvent(null);
         this.mutePanel.setEvent(null);
         this.effectPanel.setEvent(null);
         
         CardLayout cl = (CardLayout) this.controlPanel.getLayout();
         cl.show(this.controlPanel, "empty");
         
         this.update();
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         super.actionPerformed(ae);
         
         Object source = ae.getSource();
         
         if (source == this.addButton) {
             this.addMenu.show(this, addButton.getX(), addButton.getY() + addButton.getHeight());
         } else if (source == this.addTransport) {
             this.cue.addEvent(new TransportEvent());
         } else if (source == this.addMute) {
             this.cue.addEvent(new MuteEvent());
         } else if (source == this.addEffect) {
             this.cue.addEvent(new EffectEvent());
        } else if (source == this.removeButton) {
            AbstractEvent selection = (AbstractEvent) eventsList.getSelectedValue();

            if (cue != null && selection != null) {
                cue.removeEvent(selection);


                CardLayout cl = (CardLayout) this.controlPanel.getLayout();
                cl.show(this.controlPanel, "empty");
            }
         }
         
         this.update();
     }
 
     @Override
     public void valueChanged(ListSelectionEvent lse) {
         if (!lse.getValueIsAdjusting()) {
             AbstractEvent selection = (AbstractEvent) this.eventsList.getSelectedValue();
             
             if (selection != null) {
                 CardLayout cl = (CardLayout) this.controlPanel.getLayout();
                 
                 if (selection instanceof TransportEvent) {
                     TransportEvent te = (TransportEvent) selection;
                     
                     cl.show(this.controlPanel, "transport");
                     
                     this.transportPanel.setEvent(te);
                 } else if (selection instanceof MuteEvent) {
                     MuteEvent me = (MuteEvent) selection;
                     
                     cl.show(this.controlPanel, "mute");
                     
                     this.mutePanel.setEvent(me);
                 } else if (selection instanceof EffectEvent) {
                     EffectEvent ee = (EffectEvent) selection;
                     
                     cl.show(this.controlPanel, "effect");
                     
                     this.effectPanel.setEvent(ee);
                 }
             }
         }
     }
     
     
 }
