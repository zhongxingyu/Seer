 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package thirteenducks.cor.graphics.input;
 
 //import elementcorp.rog.RogMapElement.collision;
 import thirteenducks.cor.graphics.CoreGraphics;
 import thirteenducks.cor.game.Position;
 import thirteenducks.cor.game.client.ClientCore;
 import thirteenducks.cor.game.Building;
 import thirteenducks.cor.game.GameObject;
 import thirteenducks.cor.game.Unit;
 import java.awt.Dimension;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.newdawn.slick.*;
 import thirteenducks.cor.game.NetPlayer;
 import thirteenducks.cor.game.Pauseable;
 import thirteenducks.cor.graphics.AbilityHud;
 
 /**
  *
  * @author tfg
  *  Das Inputmodul
  */
 public class CoRInput implements Pauseable {
 
     /**
      * Veraltete Referenz auf das Grafikmodul
      * @deprecated
      */
     private CoreGraphics graphics;
     /**
      * Referenz auf den CoR-Kern, wird benötigt um die Funktionen desselben aufzurufen. Eher Pfusch.
      */
     private ClientCore.InnerClient rgi;
     /**
      * Alle derzeit selektierten IGE's.
      */
     private List<InteractableGameElement> selected;
     /**
      * Alle überhaupt verfügbaren IGE's.
      * Das Inputmodul verarbeitet alle IGE's in dieser Liste.
      */
     private List<InteractableGameElement> iges;
     /**
      * Die SelektionsMap, die alle Position speichert, an denen derzeit etwas selektierbar ist.
      */
     private SelectionMap selMap;
     /**
      * Die Liste mit allen OverlayListeners, die Mausaktionen möglichweise abfangen könnten.
      */
     private List<OverlayMouseListener> overlays;
     /**
      * Ein anderer Eingabemodus. Kann durch einen Aufruf gesetzt werden, dann kann sich das Inputmodul kurzzeitig
      * komplett anders verhalten, was zusätzliche Eingaben ermöglicht.
      */
     private CoRInputMode specialMode = null;
     /**
      * Das Input-Subsystem
      */
     private org.newdawn.slick.Input input;
     /**
      * True, während die shift-Taste (umschalt) gedrückt ist.
      */
     private boolean shiftDown = false;
     /**
      * True, während die ctrl-Taste (Strg) gedrückt ist.
      */
     private boolean ctrlDown = false;
     /**
      * Veralteter Scroll-Mechanismus, PFUSCH!
      * @deprecated
      */
     public boolean[] scroll;
     /**
      * Chatmodus, der die meisten Tastatureingaben an den Chat weiterreicht.
      * Auch PFUSCH, sollte mit InputModes gemacht werden.
      * @deprecated
      */
     public boolean chatMode = false;
     /**
      * Die auf den Nummerntasten 0-9 speicherbaren Belegungen
      */
     private InteractableGameElement[][] savedSelections; // Gespeicherte Selektion (0-9)
     /**
      * Der Zeitpunkt, zu dem zuletzt eine savedSelection aufgerufen wurde. Zur Doppelklick-Erkennung
      */
     private long lastSavedKlick = 0;
     /**
      * Die zuletzt aufgerufene savedSelection. Für Doppelklick-Erkennung
      */
     private int lastSavedRead = -1;
     /**
      * Wird bei jedem normalen Linksklick auf Einheiten gesetzt.
      * Dient zur Doppelklick-Selektion
      */
     private long klickTime = 0;
     /**
      * Die Zeit, die maximal zwischen 2 Klicks vergehen darf,
      * damit es noch ein Doppelklick ist.
      */
     public static final int doubleKlickDelay = 400;
     /**
      * Die Koordinaten des Startpunktes der Selektionsbox.
      */
     public Dimension boxselectionstart;
     /**
      * Sind wir derzeit im Box-Selektionsmodus?
      */
     public boolean dragSelectionBox = false;
     /**
      * Interner Zustandsspeicher, benötigt für das Overlayssystem
      */
     private int lastMouseX;
     /**
      * Interner Zustandsspeicher, benötigt für das Overlayssystem
      */
     private int lastMouseY;
     /**
      * Die Fähigkeitenanzeige des Huds.
      */
     private AbilityHud abHud;
 
     public void initAsSub(CoreGraphics rg, int mapX, int mapY) {
         graphics = rg;
         selMap = new SelectionMap(mapX, mapY);
         abHud = AbilityHud.createAbilityHud(rgi);
         graphics.content.overlays.add(abHud);
         rgi.logger("[RogInput][Init]: Adding Listeners to Gui...");
         initListeners();
         rgi.logger("[RogInput] RogInput is ready to rock! (init completed)");
     }
 
     /**
      * Ersetzt die interne IGE-Liste mit (einer Kopie) der gegebenen.
      * Dadurch werden alle vorher getrackten IGE's nichtmehr beachtet und die
      * bisherige Selektionsmap geht verloren.
      * Muss nach dem Laden einer Map einmal aufgerufen werden, damit bereits vorhandene IGE's berücksichtigt werden.
      * Muss überhaupt einmal aufgerufen worden sein, damit das Inputmodul arbeiten kann.
      * @param iges
      */
     public void setIGEs(List<InteractableGameElement> iges) {
         selMap.clear();
         this.iges = new ArrayList<InteractableGameElement>(iges);
     }
 
     public boolean hasSpecialMode() {
         return (specialMode != null);
     }
 
     public void addAndReplaceSpecialMode(CoRInputMode newmode) {
 //        // Fügt den neuen Modi hinzu und entfernt den alten (falls vorhanden)
 //        if (specialMode != null) {
 //            // Alten entfernen
 //            specialMode.endMode();
 //        }
 //        input.removeAllMouseListeners();
 //
 //        // Neuen registrieren
 //
 //        specialMode = newmode;
 //        specialMode.startMode();
 //        input.addMouseListener(new MouseListener() {
 //
 //            @Override
 //            public void mouseWheelMoved(int change) {
 //            }
 //
 //            @Override
 //            public void mouseClicked(int button, int x, int y, int clickCount) {
 //                if (!graphics.content.pauseMode) {
 //                    specialMode.mouseKlicked(button, x, y, clickCount);
 //                }
 //            }
 //
 //            @Override
 //            public void mousePressed(int button, int x, int y) {
 //                /*    if (!graphics.content.pauseMode) {
 //                // Hud, Minimap?
 //                if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
 //                if (y > (graphics.content.viewY * 15 / 7 * 1.2) && y < graphics.content.viewY * 15 / 7 * 3 - graphics.content.viewY * 15 / 7 * 0.2) {
 //                // Auf Minimap!
 //                graphics.miniMapScrolling = true;
 //                }
 //                }
 //                } */
 //            }
 //
 //            @Override
 //            public void mouseReleased(int button, int x, int y) {
 //                if (!graphics.content.pauseMode) {
 //                    graphics.miniMapScrolling = false;
 //                }
 //            }
 //
 //            @Override
 //            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 //                // Position markieren
 //                graphics.content.mouseX = newx;
 //                graphics.content.mouseY = newy;
 //                if (!graphics.content.pauseMode) {
 //                    specialMode.mouseMoved(oldx, oldy, newx, newy);
 //                }
 //            }
 //
 //            @Override
 //            public void setInput(Input input) {
 //            }
 //
 //            @Override
 //            public boolean isAcceptingInput() {
 //                return true;
 //            }
 //
 //            @Override
 //            public void inputEnded() {
 //            }
 //
 //            @Override
 //            public void mouseDragged(int oldx, int oldy, int newx, int newy) {
 //                // Position markieren
 //                graphics.content.mouseX = newx;
 //                graphics.content.mouseY = newy;
 //                if (!graphics.content.pauseMode) {
 //                    specialMode.mouseMoved(oldx, oldy, newx, newy);
 //                }
 //            }
 //
 //            @Override
 //            public void inputStarted() {
 //            }
 //        });
 //
 //
 //        input.resume();
     }
 
     public void removeSpecialMode() {
 //        // Entfernt den SpecialMode und stellt das normale Verhalten wieder her
 //        if (specialMode != null) {
 //            specialMode.endMode();
 //        }
 //        // Listener löschen, falls welche da sind
 //        input.removeAllMouseListeners();
 //        // Ursprungliche Listener wiederherstellen
 //        initListeners();
     }
 
     /*
      * Sucht das Overlay unter der Maus.
      * Sucht rückwärts, damit später hinzugefügte Overlays, die "drüber" liegen zuerst gefunden werden.
      */
     private OverlayMouseListener findOverlay() {
         for (int i = overlays.size() - 1; i >= 0; i--) {
             OverlayMouseListener listener = overlays.get(i);
             // Trifft das zu?
             if (listener.getCatch1X() <= lastMouseX && listener.getCatch2X() >= lastMouseX && listener.getCatch1Y() <= lastMouseY && listener.getCatch2Y() >= lastMouseY) {
                 // Bingo!
                 return listener;
             }
         }
         return null;
     }
 
     private void initListeners() {
         // Mauszeiger
         input.removeAllMouseListeners();
         input.removeAllKeyListeners();
         input.addMouseListener(new MouseListener() {
 
             @Override
             public void mouseWheelMoved(int change) {
                 OverlayMouseListener listener = findOverlay();
                 if (listener != null) {
                     listener.mouseWheelMoved(change);
                 }
             }
 
             @Override
             public void mouseClicked(final int button, final int x, final int y, final int clickCount) {
                 // Klicked gibts nicht, nur pressed oder released
             }
 
             @Override
             public void mousePressed(int button, int x, int y) {
                 OverlayMouseListener listener = findOverlay();
                 if (listener != null) {
                     listener.mousePressed(button, x - listener.getCatch1X(), y - listener.getCatch1Y());
                 } else {
                     // Game
                     if (!graphics.content.pauseMode) {
                         // Hud oder Game?
                 /*    if (x > graphics.content.hudX) {
                         // Hud, Minimap?
                         if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
                         if (y > (graphics.content.viewY * 15 / 7 * 1.2) && y < graphics.content.viewY * 15 / 7 * 3 - graphics.content.viewY * 15 / 7 * 0.2) {
                         // Auf Minimap!
                         if (button == 0) {
                         graphics.miniMapScrolling = true;
                         }
                         }
                         }
                         } else { */
                         // Im Game.
                         // Selektionskästchen ziehen
                         if (button == 0) {
                             startSelectionBox(x, y);
                             graphics.dSBX = x;
                             graphics.dSBY = y;
                         } else if (button == 1) {
                             if (rgi.rogGraphics.rightScrollingEnabled) {
                                 graphics.startRightScrolling();
                             }
                         }
                         //    }
                     }
                 }
             }
 
             @Override
             public void mouseReleased(final int button, final int x, final int y) {
                 OverlayMouseListener listener = findOverlay();
                 if (listener != null) {
                     listener.mouseReleased(button, x - listener.getCatch1X(), y - listener.getCatch1Y());
                 } else {
                     if (!graphics.content.pauseMode) {
                         if (rgi.rogGraphics.rightScrollingEnabled) {
                             graphics.stopRightScrolling();
                         }
                         Thread t = new Thread(new Runnable() {
 
                             @Override
                             public void run() {
                                 graphics.miniMapScrolling = false;
                                 if (button == 0) {
                                     // Wenn die Maus um mindestens 5 Pixel bewegt wurde ist es ein Selektionsrahmen, sonst ein normaler Klick
                                     int dx = graphics.dSBX - x;
                                     int dy = graphics.dSBY - y;
                                     if ((dx > 5 || dx < -5) && (dy > 5 || dy < -5)) {
                                         if (dragSelectionBox) {
                                             // Selektion ist an, jetzt abschalten und eingeschlossene Einheiten selektieren
                                             if (!shiftDown) {
                                                 for (int i = 0; i < selected.size(); i++) {
                                                     selected.get(i).setSelected(false);
                                                 }
                                                 selected.clear();
                                                 abHud.setActiveObjects(null);
                                             }
                                             List<InteractableGameElement> selectedIGE = getBoxSelected(x, y);
                                             if (selectedIGE != null) {
                                                 for (InteractableGameElement ige : selectedIGE) {
                                                     ige.setSelected(true);
                                                     selected.add(ige);
                                                 }
                                                 abHud.setActiveObjects(selectedIGE);
                                             }
 
                                         }
                                     } else {
                                         // Kein Rahmen, normaler klick
                                         CoRInput.this.mouseKlickedLeft(button, x, y, 1);
                                     }
                                     // Das auf jeden Fall machen:
                                     stopSelectionBox();
                                 }
                                 if (button == 1 && !rgi.rogGraphics.rightScrollingEnabled || (System.currentTimeMillis() - rgi.rogGraphics.rightScrollStart < 200)) {
                                     /*    if (x > graphics.content.hudX) {
                                     // Auf Minimap?
                                     if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
                                     if (y > (graphics.content.realPixY / 7 * 1.4) && y < graphics.content.realPixY / 7 * 3) {
                                     // Auf Minimap!
                                     mouseKlickedRightMiniMap(button, x, y);
                                     }
                                     }
                                     } else { */
                                     mouseKlickedRight(button, x, y);
                                     //  }
                                 }
                             }
                         });
                         t.setDaemon(true);
                         t.setName("InputEventWorker - MouseReleased");
                         t.start();
                     }
                 }
             }
 
             @Override
             public void mouseMoved(int oldx, int oldy, int newx, int newy) {
                 // Position markieren
                 graphics.content.mouseX = newx;
                 graphics.content.mouseY = newy;
                 lastMouseX = newx;
                 lastMouseY = newy;
                 OverlayMouseListener listener = findOverlay();
                 if (listener != null) {
                     listener.mouseMoved(newx - listener.getCatch1X(), newy - listener.getCatch1Y());
                 }
             }
 
             @Override
             public void setInput(Input input) {
             }
 
             @Override
             public boolean isAcceptingInput() {
                 return true;
             }
 
             @Override
             public void mouseDragged(int i, int i1, int newx, int newy) {
                 // Position markieren
                 graphics.content.mouseX = newx;
                 graphics.content.mouseY = newy;
                 lastMouseX = newx;
                 lastMouseY = newy;
                 OverlayMouseListener listener = findOverlay();
                 if (listener != null) {
                     listener.mouseDragged(newx - listener.getCatch1X(), newy - listener.getCatch1Y());
                 }
             }
 
             @Override
             public void inputStarted() {
             }
 
             @Override
             public void inputEnded() {
             }
         });
 
         input.addKeyListener(new KeyListener() {
 
             @Override
             public void keyPressed(int key, char c) {
                 if (!graphics.content.pauseMode) {
                     if (!chatMode) {
                         switch (key) {
                             case Input.KEY_LSHIFT:
                             case Input.KEY_RSHIFT:
                                 shiftDown = true;
                                 break;
                             case Input.KEY_LCONTROL:
                             case Input.KEY_RCONTROL:
                                 ctrlDown = true;
                                 break;
                             case Input.KEY_W:
                             case Input.KEY_UP:
                                 scroll[0] = true;
                                 break;
                             case Input.KEY_A:
                             case Input.KEY_LEFT:
                                 scroll[1] = true;
                                 break;
                             case Input.KEY_S:
                             case Input.KEY_DOWN:
                                 scroll[2] = true;
                                 break;
                             case Input.KEY_D:
                             case Input.KEY_RIGHT:
                                 scroll[3] = true;
                                 break;
                             case Input.KEY_0:
                                 manageSavedSel(0, ctrlDown);
                                 break;
                             case Input.KEY_1:
                                 manageSavedSel(1, ctrlDown);
                                 break;
                             case Input.KEY_2:
                                 manageSavedSel(2, ctrlDown);
                                 break;
                             case Input.KEY_3:
                                 manageSavedSel(3, ctrlDown);
                                 break;
                             case Input.KEY_4:
                                 manageSavedSel(4, ctrlDown);
                                 break;
                             case Input.KEY_5:
                                 manageSavedSel(5, ctrlDown);
                                 break;
                             case Input.KEY_6:
                                 manageSavedSel(6, ctrlDown);
                                 break;
                             case Input.KEY_7:
                                 manageSavedSel(7, ctrlDown);
                                 break;
                             case Input.KEY_8:
                                 manageSavedSel(8, ctrlDown);
                                 break;
                             case Input.KEY_9:
                                 manageSavedSel(9, ctrlDown);
                                 break;
                             case Input.KEY_COMMA:
                                 System.out.println("AddMe: Reimplement select workless workers!");
                                 break;
                         }
                     }
                 }
             }
 
             @Override
             public void keyReleased(int key, char c) {
                 if (!graphics.content.pauseMode) {
                     if (!chatMode) {
                         switch (key) {
                             case Input.KEY_LSHIFT:
                             case Input.KEY_RSHIFT:
                                 shiftDown = false;
                                 break;
                             case Input.KEY_LCONTROL:
                             case Input.KEY_RCONTROL:
                                 ctrlDown = false;
                                 break;
                             case Input.KEY_P:
                             case Input.KEY_PAUSE:
                                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 6, 0, 0, 0, 0));
                                 break;
                             case Input.KEY_F10:
                                 // Ende
                                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 10, 0, 0, 0, 0));
                                 rgi.shutdown(0);
                                 break;
                             case Input.KEY_M:
                                 // Mute umschalten
                                 rgi.rogSound.toggleMute();
                                 break;
                             case Input.KEY_E:
                                 // Energiebalken umschalten
                                 rgi.rogGraphics.setAlwaysShowEnergyBars(!rgi.rogGraphics.getAlwaysShowEnergyBars());
                                 break;
                             case Input.KEY_Y:
                             case Input.KEY_X:
                                 // Chat umschalten
                                 rgi.chat.toggleChat();
                                 break;
                             case Input.KEY_ESCAPE:
                                 // Speziellen Inputmodus verlassen
                                 CoRInput.this.removeSpecialMode();
                                 break;
                             case Input.KEY_F2:
                                 break;
                             case Input.KEY_F3:
                                 // Koordinatenmodus
                                 rgi.netctrl.broadcastString("F3", (byte) 44);
                                 if (rgi.isInDebugMode()) {
                                     rgi.rogGraphics.content.coordMode = !rgi.rogGraphics.content.coordMode;
                                 }
                                 break;
                             case Input.KEY_F4:
                                 // ID-Modus
                                 rgi.netctrl.broadcastString("F4", (byte) 44);
                                 if (rgi.isInDebugMode()) {
                                     rgi.rogGraphics.content.idMode = !rgi.rogGraphics.content.idMode;
                                 }
                                 break;
                             case Input.KEY_F5:
                                 // ServerKollisionsModus
                                 rgi.netctrl.broadcastString("F5", (byte) 44);
                                 if (rgi.isInDebugMode()) {
                                     rgi.rogGraphics.content.serverColMode = !rgi.rogGraphics.content.serverColMode;
                                 }
                                 break;
                             case Input.KEY_F6:
                                 // FoW abschalten
                                 rgi.netctrl.broadcastString("F6", (byte) 44);
                                 if (rgi.isInDebugMode()) {
                                     rgi.rogGraphics.disableFoW();
                                 }
                                 break;
                             case Input.KEY_F7:
                                 // Ressourcen herbeicheaten
                                 rgi.netctrl.broadcastString("F7", (byte) 44);
                                 if (rgi.isInDebugMode()) {
                                     NetPlayer player = rgi.game.getOwnPlayer();
                                     player.res1 += 1000;
                                     player.res2 += 1000;
                                     player.res3 += 1000;
                                     player.res4 += 1000;
                                     player.res5 += 1000;
                                 }
                                 break;
                             case Input.KEY_F8:
                                 // Einheitenverhalten-Debugmodus
                                 if (rgi.isInDebugMode()) {
                                     rgi.rogGraphics.content.unitDestMode += 1;
                                     if (rgi.rogGraphics.content.unitDestMode == 4) {
                                         rgi.rogGraphics.content.unitDestMode = 0;
                                     }
                                 }
                                 break;
                             case Input.KEY_W:
                             case Input.KEY_UP:
                                 scroll[0] = false;
                                 break;
                             case Input.KEY_A:
                             case Input.KEY_LEFT:
                                 scroll[1] = false;
                                 break;
                             case Input.KEY_S:
                             case Input.KEY_DOWN:
                                 scroll[2] = false;
                                 break;
                             case Input.KEY_D:
                             case Input.KEY_RIGHT:
                                 scroll[3] = false;
                                 break;
                             case Input.KEY_DELETE:
                                 // Alle derzeit selektierten Einheiten löschen
                                 rgi.mapModule.deleteSelected(selected);
                                 break;
                             case Input.KEY_T:
                                 rgi.teamSel.toggle();
                                 break;
                         }
                     } else {
                         // Tasten, die auch im Chatmode verarbeitet werden
                         if (key == Input.KEY_F10) {
                             // Ende
                             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 10, 0, 0, 0, 0));
                             rgi.shutdown(0);
                         } else if (key == Input.KEY_ESCAPE) {
                             // Chatmode verlassen
                             // Chat umschalten
                             rgi.chat.toggleChat();
                         } else {
                             // An den Chat weiterleiten
                             rgi.chat.input(key, c);
                         }
                     }
                 } else {
                     if (!chatMode) {
                         // Befehle, die auch im Pause-Modus funktionieren:
                         switch (key) {
                             case Input.KEY_P:
                             case Input.KEY_PAUSE:
                                 // Pause umschalten
                                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 6, 0, 0, 0, 0));
                                 break;
                             case Input.KEY_X:
                             case Input.KEY_Y:
                                 // Chat umschalten
                                 rgi.chat.toggleChat();
                                 break;
                         }
                     } else {
                         // Tasten, die auch im Chatmode verarbeitet werden
                         if (key == Input.KEY_ESCAPE) {
                             // Chatmode verlassen
                             // Chat umschalten
                             rgi.chat.toggleChat();
                         } else {
                             // An den Chat weiterleiten
                             rgi.chat.input(key, c);
                         }
                     }
                 }
             }
 
             @Override
             public void setInput(Input input) {
             }
 
             @Override
             public boolean isAcceptingInput() {
                 return true;
             }
 
             @Override
             public void inputStarted() {
             }
 
             @Override
             public void inputEnded() {
             }
         });
 
         input.resume();
     }
 
     /**
      * Verwaltet die gespeicherten Gruppen
      */
     private void manageSavedSel(int number, boolean set) {
         // Setzen oder auslesen?
         if (set) {
             // Setzen
             savedSelections[number] = selected.toArray(new GameObject[selected.size()]);
         } else {
             // Doppelklick?
             if (lastSavedRead == number && System.currentTimeMillis() - lastSavedKlick < 250) {
                 // Ansicht zum ersten Go scollen
                 if (!selected.isEmpty()) {
                     if (selected.get(0) instanceof Unit) {
                         Unit u = (Unit) selected.get(0);
                         rgi.rogGraphics.jumpTo(u.getMainPosition().getX() - (rgi.rogGraphics.content.viewX / 2), u.getMainPosition().getY() - (rgi.rogGraphics.content.viewY / 2));
                     } else {
                         Building b = (Building) selected.get(0);
                         rgi.rogGraphics.jumpTo((b.getMainPosition().getX() + 6) - (rgi.rogGraphics.content.viewX / 2), b.getMainPosition().getY() - (rgi.rogGraphics.content.viewY / 2));
                     }
                 }
             } else {
                 // Alle momentan selektierten Abwählen (falls nicht shift gedrückt wurde)
                 if (!shiftDown) {
                     for (InteractableGameElement obj : selected) {
                         obj.setSelected(false);
                     }
                     selected.clear();
                 }
                 // Wenn was gespeichert ist, dann das setzen
                 InteractableGameElement[] list = savedSelections[number];
                 if (list != null) {
                     for (InteractableGameElement obj : list) {
                         if (!selected.contains(obj)) {
                             obj.setSelected(true);
                             selected.add(obj);
                         }
                     }
                 }
             }
             lastSavedRead = number;
             lastSavedKlick = System.currentTimeMillis();
         }
     }
 
 //    /**
 //     * Wird beim Rechtsklick auf die Minimap aufgerufen
 //     * @param e
 //     */
 //    private void mouseKlickedRightMiniMap(final int button, final int x, final int y) {
 //        // Nur bei selektieren Einheiten
 //        if (!selected.isEmpty() && selected.get(0).getClass().equals(Unit.class)) {
 //            // Alle da hin schicken, immer mit der Zielsuch-Logik
 //            // Erstmal grundlegendes Ziel berechnen
 //            Dimension selField = rgi.rogGraphics.content.searchMiniMid(x, y);
 //            // Auf 2er-Raster anpassen
 //            if ((selField.width + selField.height) % 2 == 1) {
 //                selField.height--;
 //            }
 //
 //            for (int i = 0; i < selected.size(); i++) {
 //                Unit tmpUnit = (Unit) selected.get(i);
 //                Position target = new Position(selField.width, selField.height).aroundMe(i, rgi, 10000);
 //                tmpUnit.sendToPosition(target, rgi, true);
 //            }
 //
 //        }
 //    }
     /**
      * Wird bei Linksklick aufgerufen. Behandelt an/abwählen von Einheiten, Gebäuden.
      * @param e     MouseEvent, enthält Position, welcher Button, wie oft, welche Maus, etc..
      */
     public void mouseKlickedLeft(final int button, final int x, final int y, int clickCount) {
         System.out.println("AddMe: doubleklick on mKL-Input!");
         final int myPlayer = rgi.game.getOwnPlayer().playerId;
         Position selField = graphics.content.translateCoordinatesToField(x, y);
         List<InteractableGameElement> elems = selMap.getIGEsAt(selField.getX(), selField.getY());
         // Alle rausschmeißen, die sich nicht selektieren lassen und den Rest auf multi-nicht multi untersuchen
         boolean containsMulti = false;
         for (int i = 0; i < elems.size(); i++) {
             InteractableGameElement elem = elems.get(i);
             if (elem.selectable() && elem.isSelectableByPlayer(myPlayer)) {
                 if (elem.isMultiSelectable()) {
                     // Das ist ein Multi, ok. Nur eines Anwählen!
                     elems.clear();
                     elems.add(elem);
                     break;
                 }
             } else {
                 // Rauswerfen, das können wir nicht anklicken
                 elems.remove(i--);
             }
         }
         // Liste bereinigen - wenn ein multi drin ist, alle singles rausschmeißen.
         // Wenn nicht: Alle Singles bis auf eines rausschmeißen
         if (containsMulti) {
             // Singles weg
             for (int i = 0; i < elems.size(); i++) {
                 if (!elems.get(i).isMultiSelectable()) {
                     elems.remove(i--);
                 }
             }
         } else { // Nur wenn gar keine Multis
             // Alle Singles bis auf das erste Weg - falls welche da sind
             if (elems.size() > 0) {
                 elems.retainAll(elems.subList(0, 1));
             }
         }
         // Die Liste entählt jetzt entweder nur noch Multis (Anzahl egal) oder nur ein Single.
         // Wenn ein Single, dann auf jeden Fall alles alte abwählen. Bei Multis alles alte abwählen, falls shift NICHT gedrückt ist.
         if (!containsMulti || !shiftDown) {
             for (int i = 0; i < selected.size(); i++) {
                 selected.get(i).setSelected(false);
                 System.out.println("Deselected: " + selected.get(i).toString());
             }
             selected.clear();
             abHud.setActiveObjects(null);
         }
 
         // Alles, was noch da ist anwählen:
         for (InteractableGameElement elem : elems) {
             elem.setSelected(true);
             selected.add(elem);
             System.out.println("Selected: " + elem.toString());
         }
         abHud.setActiveObjects(elems);
     }
 
     /**
      * Wird bei Rechtsklick aufgerufen. Behandelt Befehle an die Einheiten (Angreifen, bewegen, Gebäude weiterbauen, ...)
      * @param e: MouseEvent, enthält Position, welcher Button, wie oft, etc..
      */
     public void mouseKlickedRight(final int button, final int x, final int y) {
         System.out.println("AddMe: Check for double-klicks");
         Position selField = graphics.content.translateCoordinatesToField(x, y);
         // Überhaupt was selektiert?
         if (!selected.isEmpty()) {
             // Ziele finden:
             List<InteractableGameElement> targets = selMap.getIGEsAt(selField.getX(), selField.getY());
             if (!targets.isEmpty()) {
                 for (InteractableGameElement elem : selected) {
                     elem.command(button, targets, false, rgi);
                 }
             } else {
                 // Default = Move
                selected.get(0).command(button, selField, Collections.unmodifiableList(selected), false, rgi);
             }
         }
     }
 
     // Überhaupt was selektiert?
 
     /*      if (!selected.isEmpty()) {
 
     // Was wurde angeklickt?
 
     final Dimension selField = graphics.content.getGameSelectedField(x, y);
     final Unit selUnit = graphics.content.identifyUnit(x, y);
     final Building selBuilding = graphics.content.identifyBuilding(selField.width, selField.height);
 
     final int playerId = selected.get(0).getPlayerId();
 
     // Ganz unbekannt:
     final boolean clickedInBlack = (rgi.rogGraphics.content.fowmap[(int) selField.getWidth()][(int) selField.getHeight()] < 1);
     // Grob bekannt:
     final boolean clickedInKnown = (rgi.rogGraphics.content.fowmap[(int) selField.getWidth()][(int) selField.getHeight()] < 2);
 
     // Ab jetzt multithreaden, sonst wird die Grafik überlastet
 
     Thread t = new Thread(new Runnable() {
 
     @Override
     public void run() {
     if (!clickedInBlack) { //Weiß der Spieler, was er anklickt?
 
     // Was ist zurzeit selektiert
 
     if (selected.get(0).getClass().equals(Unit.class)) {
 
     // Unit selektiert
 
     if (selUnit != null && selUnit.getPlayerId() != playerId && !rgi.game.areAllies(selUnit, rgi.game.getPlayer(playerId)) && !clickedInKnown) { // Man kann Feinde im erkundeten nicht angereifen
 
     // Angreifen - Einzeln?
     if (selected.size() == 1) {
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 29, selected.get(0).netID, selUnit.netID, 2, 0));
     } else {
     // Bei mehr als 2 sind mehrere Packete nötig:
     if (selected.size() == 2) {
     // Nur ein Packet:
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selUnit.netID, selected.get(0).netID, selected.get(1).netID, 0));
     } else {
     // Das erste gleich mal raushauen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selUnit.netID, selected.get(0).netID, selected.get(1).netID, selected.get(2).netID));
     // Jetzt den Rest abhandeln
     int[] ids = new int[4];
     for (int i = 0; i < 4; i++) {
     ids[i] = 0;
     }
     int nextselindex = 3;
     int nextidindex = 0;
     // Solange noch was da ist:
     while (nextselindex < selected.size()) {
     // Auffüllen
     ids[nextidindex] = selected.get(nextselindex).netID;
     nextidindex++;
     nextselindex++;
     // Zu weit?
     if (nextidindex == 4) {
     // Einmal rausschicken & löschen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
     for (int i = 0; i < 4; i++) {
     ids[i] = 0;
     }
     nextidindex = 0;
     }
     }
     // Fertig, den Rest noch senden
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
     }
     }
     } else if (selBuilding != null && selBuilding.getPlayerId() == playerId && selBuilding.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
     // Gebäude - gleicher Spieler : Bauen
     Unit unit = (Unit) selected.get(0);
     // Wenn noch keiner da dran rumbaut:
     System.out.println("AddMe: Check for other builders!");
     // Kann die Einheit das?
     AbilityBuild ro = unit.getBuildAbility(selBuilding.getDescTypeId());
     if (ro != null) {
     // Ja, geht.
     //unit.moveToBuilding(rBuilding, rgi);
     // Unit hinlaufen lassen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 26, unit.netID, selBuilding.netID, 0, 0));
     // Signal senden
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 17, unit.netID, selBuilding.netID, ro.duration, 0));
     }
     } else if (selBuilding != null && selBuilding.getPlayerId() == playerId && selBuilding.ready) {
     // Gebäude - gleicher Spieler : Betreten
     // So viele Einheiten reinschicken, wie noch Platz frei ist.
     int searchCount = 0;
     int free = selBuilding.intraFree();
     while (free > 0 && selected.size() > searchCount) {
     if (selBuilding.accepts == Building.ACCEPTS_ALL) {
     selBuilding.goIntra((Unit) selected.get(searchCount), rgi);
     searchCount++;
     free--;
     } else {
     boolean found = true;
     while (!((Unit) selected.get(searchCount)).canHarvest) {
     searchCount++;
     if (selected.size() <= searchCount) {
     found = false;
     break;
     }
     }
     if (found) {
     selBuilding.goIntra((Unit) selected.get(searchCount), rgi);
     searchCount++;
     free--;
     }
     }
     }
 
     } else if (selBuilding != null && selBuilding.playerId != playerId && !rgi.game.areAllies(selBuilding, rgi.game.getPlayer(playerId)) && selBuilding.wasSeen) { // Man kann Gebäude im erkundeten angreiffen, aber nur wenn sie bereits entdeckt wurden
     // Gebäude - fremder Spieler : Gebäudeangriff
     // Gruppenangriff?
     if (selected.size() == 1) {
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 29, selected.get(0).netID, selBuilding.netID, 2, 0));
     } else {
     // Hier sind unter umständen mehrere Packete nötig:
     if (selected.size() == 2) {
     // Nur ein Packet:
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selBuilding.netID, selected.get(0).netID, selected.get(1).netID, 0));
     } else {
     // Das erste gleich mal raushauen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selBuilding.netID, selected.get(0).netID, selected.get(1).netID, selected.get(2).netID));
     // Jetzt den Rest abhandeln
     int[] ids = new int[4];
     for (int i = 0; i < 4; i++) {
     ids[i] = 0;
     }
     int nextselindex = 3;
     int nextidindex = 0;
     // Solange noch was da ist:
     while (nextselindex < selected.size()) {
     // Auffüllen
     ids[nextidindex] = selected.get(nextselindex).netID;
     nextidindex++;
     nextselindex++;
     // Zu weit?
     if (nextidindex == 4) {
     // Einmal rausschicken & löschen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
     for (int i = 0; i < 4; i++) {
     ids[i] = 0;
     }
     nextidindex = 0;
     }
     }
     // Fertig, den Rest noch senden
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
     }
     }
 
     } else if (selRessource != null) { // Man kann Ressourcen im grauen anklicken
     // Ernten
     if (selRessource.getType() < 5) {
     for (GameObject obj : selected) {
     try {
     Unit unit = (Unit) obj;
     unit.goHarvest(selRessource, rgi);
     } catch (ClassCastException ex1) {
     }
     }
     }
     } else {
     // Bewegen - man kann sich ins graue bewegen
     // Mehrere?
     if (selected.size() == 1) {
     Unit tmpUnit = (Unit) selected.get(0);
     Position target = new Position(selField.width, selField.height);
     tmpUnit.sendToPosition(target, rgi, true);
     } else {
     // Alle vorbereiten:
     for (int i = 0; i < selected.size(); i++) {
     Unit mover = (Unit) selected.get(i);
     mover.prepareMove();
     }
     // Befehl abschicken:
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, selField.width, selField.height, selected.get(0).netID, selected.get(1).netID));
     // Hier sind unter umständen mehrere Packete nötig:
     if (selected.size() == 2) {
     // Nein, abbrechen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, 0, 0, 0, 0));
     } else {
     // Jetzt den Rest abhandeln
     int[] ids = new int[4];
     for (int i = 0; i < 4; i++) {
     ids[i] = 0;
     }
     int nextselindex = 2;
     int nextidindex = 0;
     // Solange noch was da ist:
     while (nextselindex < selected.size()) {
     // Auffüllen
     ids[nextidindex] = selected.get(nextselindex).netID;
     nextidindex++;
     nextselindex++;
     // Zu weit?
     if (nextidindex == 4) {
     // Einmal rausschicken & löschen
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
     for (int i = 0; i < 4; i++) {
     ids[i] = 0;
     }
     nextidindex = 0;
     }
     }
     // Fertig, den Rest noch senden
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
     }
     }
     }
 
     } else if (selected.get(0).getClass().equals(Building.class)) {
     // Building selektiert
     // Nur Gebäude die auch was rekrutieren, dürfen Sammelpunkte setzen
     boolean recruits = false;
     for (Ability ab : selected.get(0).abilitys) {
     if (ab.type == Ability.ABILITY_RECRUIT) {
     recruits = true;
     break;
     }
     }
     // Kann das Gebäude angreifen?
     if (selected.get(0).getDamage() != 0) {
     // Eigene Position berechnen
     Building bui = (Building) selected.get(0);
     float bx = 0;
     float by = 0;
     bx = bui.position.X + ((bui.z1 - 1) * 1.0f / 2);
     by = bui.position.Y - ((bui.z1 - 1) * 1.0f / 2);
     bx += ((bui.z2 - 1) * 1.0f / 2);
     by += ((bui.z2 - 1) * 1.0f / 2);
     Position Omg = new Position((int) bx, (int) by);
     // Feindliche Einheit angeklickt?
     if (selUnit != null && selUnit.playerId != playerId && !rgi.game.areAllies(selUnit, rgi.game.getPlayer(playerId))) {
     // In Reichweite?
     if (selUnit.position.getDistance(Omg) <= bui.getRange()) {
     // Idle deaktivieren
     bui.getbehaviourC(10).deactivate();
     // Angriff an Server senden
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, bui.netID, selUnit.netID, 0, 0));
     }
     } else if (selBuilding != null && selBuilding.playerId != playerId && !rgi.game.areAllies(selBuilding, rgi.game.getPlayer(playerId))) {
     //Eines der Gebäudefelder in Reichweite?
     //z1 nuff, z2 nab
     boolean inreichweite = false;
     int randfelder = (selBuilding.z1 + selBuilding.z2 - 2) * 2;
     Position Test = new Position(selBuilding.position.X, selBuilding.position.Y);
     for (int i = 0; i < randfelder; i++) {
     if (i == 0) {
     } else if (i < selBuilding.z2) {
     Test.X++;
     Test.Y++;
     } else if (i < selBuilding.z1 + selBuilding.z2) {
     Test.X++;
     Test.Y--;
     } else if (i < selBuilding.z1 + 2 * selBuilding.z2) {
     Test.X--;
     Test.Y--;
     } else if (i < 2 * (selBuilding.z1 + selBuilding.z2)) {
     Test.X--;
     Test.Y++;
     } else {
     break;
     }
     if (Omg.getDistance(Test) <= bui.getRange()) {
     inreichweite = true;
     break;
     }
     }
     if (inreichweite) {
     // Idle deaktivieren
     bui.getbehaviourC(10).deactivate();
     // Angriff an Server senden
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, bui.netID, selBuilding.netID, 0, 0));
     }
     }
     } else if (recruits) {
     // Wegpunkt setzen - man darf das ins Graue tun...
     // ...kann es aber auch auf ein Ressourcengebäude oder auf Ressourcen tun, um die neuen Einheiten sofort ernten zu lassen
     if (selRessource != null) {
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 27, selected.get(0).netID, selRessource.netID, selField.width, selField.height));
     } else if (selBuilding != null && selBuilding.playerId == rgi.game.getOwnPlayer().playerId && selBuilding.maxIntra > 0) {
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 27, selected.get(0).netID, selBuilding.netID, selField.width, selField.height));
     } else {
     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 27, selected.get(0).netID, 0, selField.width, selField.height));
     }
     }
     }
     } else {
     // Ins schwarze geklickt, das ist nur für Einheiten zulässig:
     if (selected.get(0).getClass().equals(Unit.class)) {
     // Dahin laufen lassen
     // Die Positionen der Einheiten freimachen, für Wegfindung, und wenn sie sich bewegen sind die felder eh frei
     for (int a = 0; a < selected.size(); a++) {
     rgi.mapModule.setCollision(selected.get(a).position, collision.free);
     }
 
     for (int i = 0; i < selected.size(); i++) {
     Unit tmpUnit = (Unit) selected.get(i);
     Position target = new Position(selField.width, selField.height).aroundMe(i, rgi, 10000);
     tmpUnit.sendToPosition(target, rgi, true);
     }
     }
     }
 
     }
     });
 
     t.setDaemon(true);
     t.setName("InputHandler: RightClick");
     t.start();
     }
 
     return;
     } */
     public CoRInput(ClientCore.InnerClient inner, org.newdawn.slick.Input inp) {
         rgi = inner;
         selected = new ArrayList<InteractableGameElement>();        //Die Liste der angewählten Einheiten initialisieren
         input = inp;
         scroll = new boolean[4];
         savedSelections = new GameObject[10][];
         overlays = new LinkedList<OverlayMouseListener>();
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void unpause() {
         this.removeSpecialMode();
     }
 
     private List<InteractableGameElement> getBoxSelected(final int x, final int y) {
         // Wir wollen alle Einheiten im SelectionBox Feld.
         // Wir müssen die Ecken der SelectionShadows mit den Ecken der SelektionBox vergleichen
         LinkedList<InteractableGameElement> finalList = new LinkedList<InteractableGameElement>();
         Dimension finalDimension = new Dimension(x, y);
         if (finalDimension.equals(boxselectionstart)) {
             // Wir haben gar keine Box gezogen...
             return null;
         }
         // Gleiches Tauschsystem, wie bei der drawSelBox Methode
         int dirX = finalDimension.width - this.boxselectionstart.width;
         int dirY = finalDimension.height - this.boxselectionstart.height;
         // Bpxen können nur von links oben nach rechts unten berechnet werden - eventuell Koordinaten tauschen
         if (dirX < 0 && dirY > 0) {
             // Nur x Tauschen
             int backupX = finalDimension.width;
             finalDimension.width = this.boxselectionstart.width;
             this.boxselectionstart.width = backupX;
         } else if (dirY < 0 && dirX > 0) {
             // Nur Y tauschen
             int backupY = finalDimension.height;
             finalDimension.height = this.boxselectionstart.height;
             this.boxselectionstart.height = backupY;
         } else if (dirX < 0 && dirY < 0) {
             // Beide tauschen
             int backupX = finalDimension.width;
             finalDimension.width = this.boxselectionstart.width;
             this.boxselectionstart.width = backupX;
             int backupY = finalDimension.height;
             finalDimension.height = this.boxselectionstart.height;
             this.boxselectionstart.height = backupY;
         }
         // Felder im Rahmen berechnen & Dort anwählbare Elemente zur Liste hinzufügen
         // Felder berechnen:
         Position eckeLO = graphics.content.translateCoordinatesToField(boxselectionstart.width, boxselectionstart.height);
         Position eckeRU = graphics.content.translateCoordinatesToField(finalDimension.width, finalDimension.height);
         for (int cx = eckeLO.getX(); cx <= eckeRU.getX(); cx++) {
             for (int cy = eckeLO.getY(); cy <= eckeRU.getY(); cy++) {
                 List<InteractableGameElement> elems = selMap.getIGEsWithTeamAt(cx, cy, rgi.game.getOwnPlayer().playerId);
                 for (InteractableGameElement elem : elems) {
                     if (!finalList.contains(elem)) {
                         finalList.add(elem);
                     }
                 }
             }
         }
 
         // Nachbearbeiten.
         // Manche Objekte sind nicht zusammen mit anderen auswählbar. (Builindgs mit Units)
         // Sobald ein multi-fähiges auftaucht, alle nicht-multis rausschmeißen
         // Außerdem alle singles nach dem ersten single entfernen
         int singleIndex = -1;
         boolean killSingle = false;
         for (int i = 0; i < finalList.size(); i++) {
             InteractableGameElement elem = finalList.get(i);
             if (elem.isMultiSelectable()) {
                 if (singleIndex != -1) {
 
                     // Single entfernen
                     finalList.remove(singleIndex);
                     singleIndex = -1; // Nicht mehrfach löschen
                     i--;
                 }
                 killSingle = true; // Es war ein Multi da, singles sind jetzt uninteressant
             } else {
                 if (killSingle || singleIndex != -1) {
                     // Schon eins da? Dann das hier löschen
                     finalList.remove(i--);
                     continue;
                 } else {
                     singleIndex = i;
                 }
             }
         }
         // Fertig, zurückgeben
         return finalList;
 
     }
 
     public void startSelectionBox(int x, int y) {
         dragSelectionBox = true;
         boxselectionstart = new Dimension(x, y);
     }
 
     public void stopSelectionBox() {
         dragSelectionBox = false;
     }
 
     /**
      * Fügt einen OverlayMouseListener zum InputModul hinzu.
      * Der neue Listener ist ab sofort aktiv.
      * Falls sich der catch-Bereich mit denen anderer Überschneiden sollte, so wird das Inputmodul
      * immer den zuletzt hinzugefügten aufrufen.
      * Damit lassen sich beispielsweise Abfragen auf einem anderen Menü realisieren.
      * @param listener Der Listener, der hinzugefügt werden soll.
      */
     public void addOverlayMouseListener(OverlayMouseListener listener) {
         overlays.add(0, listener);
     }
 
     /**
      * Löscht den gegebenen Listener wieder as dem InputModul, er fängt also künftig keine Mausklicks mehr.
      * Achtung: Löscht den Listener nur einmal. Sollten aus Versehen mehrere geadded worden sein, müssen auch genau so viele
      * wieder einzeln gelöscht werden.
      * Sollte der Listener gar nicht vorhanden sein, geschieht nichts weiter.
      * @param listener Der Listener, der gelöscht werden soll.
      */
     public void removeOverlayMouseListener(OverlayMouseListener listener) {
         overlays.remove(listener);
     }
 
     /**
      * Löscht diese Einheit aus der aktuellen Selektion (falls sie selektiert ist)
      * Benötigt für gestorbene Einheit oder Einheiten, die Gebäude betreten
      * @param unit
      */
     public void removeFromSelection(GameObject unit) {
         selected.remove(unit);
     }
 
     /**
      * Updated das Selektionssystem.
      * In der Regel ziemlich schnell, kann also vermutlich mit jedem Frame einmal aufgerufen werden.
      */
     public void updateIGEs() {
         // Alle IGEs updaten:
         for (int i = 0; i < iges.size(); i++) {
             InteractableGameElement ige = iges.get(i);
             if (ige.selPosChanged()) {
                 ige.getSelectionMarker().updateSelectionMap(selMap);
             }
         }
     }
 
     /**
      * Added dieses GO zum selektionssystem.
      * Zunkünftig werden Mausgesten auf dieses IGE erkannt.
      * @param go das hinzuzufügende go
      */
     public void addGO(GameObject go) {
         iges.add(go);
     }
 
     /**
      * Entfernt dieses GO wieder aus dem selektionssystem.
      * Mausgesten werden zukünftig nichtmehr erkannt.
      * @param go das zu entfernende go
      */
     public void removeGO(GameObject go) {
         iges.remove(go);
     }
 }
