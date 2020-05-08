 package com.cjra.battleships;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 public class Selection extends Positionable {
     private final List<Position> selections = new ArrayList<Position>();
     private final List<Position> potentialSelections = new ArrayList<Position>();
     private final int maximumSize;
 
     public Selection(int maximumSize){
         this.maximumSize = maximumSize;
     }
 
     public int size() {
         return selections.size();
     }
 
     public List<Position> getPositions() {
         return Collections.unmodifiableList(selections);
     }
 
     public void select(int x, int y){
 
         if(isValidSelection(x, y)) {
             addSelection(x, y);
             updatePotentialSelections(x, y);
         }
     }
 
     public void clear(int x, int y) {
 
         Position start = start();
         Position end = end();
 
         if(start.matches(x,y)){
             selections.remove(start);
         }
         else if(end.matches(x,y)){
             selections.remove(end);
         }
         updatePotentialSelections(x, y);
     }
 
     public boolean isSelected(int x, int y) {
 
         for(Position position : selections){
             if(position.matches(x, y)){
                 return true;
             }
         }
         return false;
     }
 
     public Position start() {
         return selections.get(0);
     }
 
     public Position end() {
         return selections.get(selections.size()-1);
     }
 
     private boolean isEmpty(){
         return selections.size() == 0;
     }
 
     private boolean isValidSelection(int x, int y) {
 
         if(!isShortEnough()){
             return false;
         }
 
         if(isEmpty()){
             return true;
         }
 
         for(Position cell : potentialSelections){
             if(cell.matches(x, y)){
                 return true;
             }
         }
         return false;
     }
 
     private boolean isShortEnough() {
         return selections.size() < maximumSize;
     }
 
     private void addSelection(int x, int y) {
 
         selections.add(new Position(x, y));
         if(selections.size() > 1){
             sortSelections();
         }
     }
 
     private void updatePotentialSelections(int x, int y) {
 
         potentialSelections.clear();
 
         if(selections.size() == 1){
             addPotentialVerticalSelections(start(), end());
             addPotentialHorizontalSelections(start(), end());
         }
 
         if(isVertical()){
             addPotentialVerticalSelections(start(), end());
         }
         else if(isHorizontal()){
             addPotentialHorizontalSelections(start(), end());
         }
     }
 
     private void addPotentialHorizontalSelections(Position start, Position end) {
         addPotentialSelection(start.x - 1, start.y);
         addPotentialSelection(end.x + 1, end.y);
     }
 
     private void addPotentialVerticalSelections(Position start, Position end) {
         addPotentialSelection(start.x, start.y - 1);
         addPotentialSelection(end.x, end.y + 1);
     }
 
     private void addPotentialSelection(int x, int y) {
         potentialSelections.add(new Position(x, y));
     }
 
     private void sortSelections(){
         if(isVertical()){
             Collections.sort(selections, getVerticalComparator());
         }
         else if(isHorizontal()){
             Collections.sort(selections, getHorizontalComparator());
         }
     }
 
     private Comparator<Position> getHorizontalComparator() {
         return new Comparator<Position>() {
             @Override
             public int compare(Position one, Position other) {
                return one.x - other.x;
             }
         };
     }
 
     private Comparator<Position> getVerticalComparator() {
         return new Comparator<Position>() {
             public int compare(Position one, Position other) {
                 return one.y - other.y;
             }
         };
     }
 }
