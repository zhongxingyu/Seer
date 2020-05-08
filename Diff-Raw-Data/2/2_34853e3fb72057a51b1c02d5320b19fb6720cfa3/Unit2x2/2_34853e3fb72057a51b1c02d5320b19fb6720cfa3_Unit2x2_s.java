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
 package de._13ducks.cor.game;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import de._13ducks.cor.game.client.ClientCore;
 import de._13ducks.cor.graphics.GraphicsContent;
 import de._13ducks.cor.graphics.GraphicsImage;
 import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
 import de._13ducks.cor.graphics.input.InteractableGameElement;
 import de._13ducks.cor.graphics.input.SelectionMarker;
 import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;
 
 /**
  * Eine echte Einheit mit 2 x 2 Feldern Grundfläche
  */
 public class Unit2x2 extends Unit {
 
     /**
      * Die Positionen auf denen die Einheit derzeit sichtbar ist.
      * Wird aus Performance-Gründen gecached.
      */
     private Position[] positions;
     /**
      * Wird vom Selektionsmechanismus verwaltet.
      * Speichert die zuletzt an das Inputmodul übergebene Position.
      */
     private Position[] lastPositions;
 
     public Unit2x2(int newNetId, Position mainPos) {
         super(newNetId, mainPos);
         positions = new Position[4];
     }
 
     /**
      * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
      * Attribute und Fähigkeiten dient.
      */
     public Unit2x2(DescParamsUnit params) {
         super(params);
     }
 
     /**
      * Erzeugt eine neue Einheit als eigenständige Kopie der Übergebenen.
      * Wichtige Parameter werden kopiert, Sachen die jede Einheit selber haben sollte nicht.
      * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
      * @param newNetId Die netId der neuen Einheit
      * @param copyFrom Die Einheit, dessen Parameter kopiert werden sollen
      */
     public Unit2x2(int newNetId, Unit2x2 copyFrom) {
         super(newNetId, copyFrom);
         positions = new Position[4];
     }
 
     @Override
     public void setMainPosition(Position mainPosition) {
         super.setMainPosition(mainPosition);
         positions[0] = mainPosition;
         positions[1] = new Position(mainPosition.getX() + 1, mainPosition.getY() - 1);
         positions[2] = new Position(mainPosition.getX() + 1, mainPosition.getY() + 1);
         positions[3] = new Position(mainPosition.getX() + 2, mainPosition.getY());
     }
 
     @Override
     public Position freeDirectAroundMe() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean isAroundMe(Position pos) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Position[] getPositions() {
         return positions;
     }
 
     @Override
     public Position getCentralPosition() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public GameObject getCopy(int newNetId) {
         return new Unit2x2(newNetId, this);
     }
 
     @Override
     public void kill() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Position[] getVisisbilityPositions() {
         return positions;
     }
 
     @Override
     public boolean gotClientBehaviours() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean gotServerBehaviours() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<ClientBehaviour> getClientBehaviours() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public List<ServerBehaviour> getServerBehaviours() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void mouseHovered() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean selPosChanged() {
         return (!Arrays.equals(positions, lastPositions));
     }
 
     @Override
     public SelectionMarker getSelectionMarker() {
         SelectionMarker marker = new SelectionMarker(this, lastPositions, positions);
         lastPositions = positions.clone();
         return marker;
     }
 
     @Override
     public void command(int button, List<InteractableGameElement> targets, boolean doubleKlick, ClientCore.InnerClient rgi) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void keyCommand(int key, char character) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void renderGroundEffect(Graphics g, int x, int y, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap, Color spriteColor) {
         clientManager.externalExecute(); // Updated die Position 
         float rx = (float) ((FloatingPointPosition) mainPosition).getfX();
         float ry = (float) ((FloatingPointPosition) mainPosition).getfY();
         //Einheit gehört zu / Selektiert
         if (isSelected()) {
             // Weiße Bodenmarkierung
             imgMap.get("img/game/sel_s2.png0").getImage().draw((float) (rx * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_2x2_X - scrollX + GraphicsContent.OFFSET_PRECISE_X), (float) (ry * GraphicsContent.FIELD_HALF_Y + GraphicsContent.OFFSET_2x2_Y - scrollY + GraphicsContent.OFFSET_PRECISE_Y));
         } else {
             // Spielerfarbe
            imgMap.get("img/game/sel_s2.png" + getPlayerId()).getImage().draw((float) (rx + GraphicsContent.OFFSET_2x2_X - scrollX + GraphicsContent.OFFSET_PRECISE_X),(float) (ry + GraphicsContent.OFFSET_2x2_Y - scrollY + GraphicsContent.OFFSET_PRECISE_Y));
         }
     }
 
     @Override
     public void renderSprite(Graphics g, int x, int y, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap, Color spriteColor) {
         GraphicsImage img = imgMap.get(getGraphicsData().defaultTexture);
         float rx = (float) ((FloatingPointPosition) mainPosition).getfX();
         float ry = (float) ((FloatingPointPosition) mainPosition).getfY();
         if (img != null) {
             img.getImage().draw((float) (rx * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_2x2_X - scrollX + GraphicsContent.OFFSET_PRECISE_X), (float) (ry * GraphicsContent.FIELD_HALF_Y + GraphicsContent.OFFSET_2x2_Y - scrollY + GraphicsContent.OFFSET_PRECISE_Y));
         } else {
             System.out.println("RENDER: Can't paint unit, texture <" + getGraphicsData().defaultTexture + "> not found!");
         }
     }
 
     @Override
     public void renderMinimapMarker(Graphics g, int x, int y, Color spriteColor) {
 	g.setColor(spriteColor);
 	g.fillRect(x, y, 2, 2);
     }
 }
