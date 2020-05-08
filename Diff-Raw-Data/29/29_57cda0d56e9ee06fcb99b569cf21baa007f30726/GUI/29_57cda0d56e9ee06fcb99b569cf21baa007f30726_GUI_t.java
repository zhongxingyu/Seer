 package de.fhk.spacequest.vis.gui;
 
 import de.fhk.spacequest.vis.Spacequest;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * GUI der Visualisierung.
  * Dies ist die Klasse, die alles verwaltet, was mit der grafischen Oberfläche zu tun hat.
  *
  * @author Robert Giacinto
  */
 public final class GUI {
 
     private Collection<GUIComponent> guiComponents;
     private Input input;
     private Spacequest spacequest;
     private boolean focus = false;
     private boolean paused = false;
     IniFile iniFile;
 
     public GUI(Spacequest spacequest, Input input, IniFile iniFile) throws SlickException {
         this.spacequest = spacequest;
         this.guiComponents = new ArrayList<GUIComponent>();
         this.input = input;
         this.iniFile = iniFile;
         initGUI();
     }
 
     public Spacequest getSpacequest() {
         return spacequest;
     }
 
     public void addCompoment(GUIComponent newComponent) {
         guiComponents.add(newComponent);
         input.addMouseListener(newComponent);
     }
 
     public boolean hasFocus() {
         return focus;
     }
 
     /**
      * @param focus
      */
     public void setFocus(boolean focus) {
         this.focus = focus;
     }
 
     /**
      * Aktualisiert den Zustand der GUI-Komponenten.
      */
     public void updateComponents() {
         for (GUIComponent c : guiComponents) {
             c.update();
         }
     }
 
     /**
      * Zeichnet die Komponenten der GUI.
      *
      * @param g das Graphics-Object
      */
     public void draw(Graphics g) {
         for (GUIComponent c : guiComponents) {
             c.draw(g);
         }
     }
 
     /**
      * Entfernt Fokus von GUI.
      */
     public void unfocus() {
         for (GUIComponent c : guiComponents) {
             input.removeMouseListener(c);
         }
         paused = true;
     }
 
     /**
      * Aktiviert den Fokus der GUI.
      */
     public void focus() {
         for (GUIComponent c : guiComponents) {
             input.addMouseListener(c);
         }
         paused = false;
     }
 
     /**
      * Gibt zurück, ob GUI MouseEvents bearbeitet oder nicht.
      *
      * @return <code>>true</code> wenn GUI keinen Fokus besitzt.
      */
     public boolean isPaused() {
         return paused;
     }
 
     /**
      * Initialisiert die Standardkomponenten der GUI.
      *
      * @throws SlickException
      */
     private void initGUI() throws SlickException {
 
         // addCompoment(new AnalogClock(spacequest.getScreenWidth() - (spacequest.getScreenWidth() / 5), spacequest.getScreenHeight() / 80, spacequest.getScreenWidth() / 16, this, IniFile.get(2)));
 
         /*
         for (PhaseButton pb : PhaseButtonFactory.createPhaseButtons(950, 10, true, this, IniFile.get(3), IniFile.get(4))) {
             addCompoment(pb);
         }
         */
        PropertyFile p = new PropertyFile();
 
         try {
            for (GUIComponent gc : p.createGUIElements("/data/default.xml", this)) {
                 addCompoment(gc);
             }
         } catch (Exception error) {
 
         }
 
         /*
         addCompoment(new DigitalClock(20, 690, this, IniFile.get(2)));
         addCompoment(new StartToggleButton(20, 10, this, IniFile.get(3), IniFile.get(4)));
         addCompoment(new PresetToggleButton(60, 10, this, IniFile.get(3), IniFile.get(4)));
         addCompoment(new ZoomSlider(40, 400, 1.9292545E-6f, 3.2829426E-5f, 200, this, IniFile.get(7)));
          */
     }
 }
