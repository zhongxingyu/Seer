 package com.linfords.detangle;
 
 import com.linfords.detangle.Space.Coordinates;
 import com.linfords.detangle.Space.State;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Scott
  */
 class Board {
 
     private final Map<Space.Coordinates, Space> board = new HashMap();
     private final TileStack tiles = new TileStack();
    private final Space.Coordinates startPos = new Space.Coordinates(0, 0);
     private Tile swapTile;
     Space current;
     Space adjacent;
 
     Board() {
         this.swapTile = tiles.pop();
        this.current = locateSpace(startPos);
         this.adjacent = locateSpace(calculateAdjacentCoordinates(current));
         this.adjacent.matchMarker(this.current);
     }
 
     private static Space.Coordinates calculateAdjacentCoordinates(Space space) {
         switch (space.marker) {
             case 0:
             case 1:
                 return new Space.Coordinates(space.pos.x, space.pos.y + 2);
             case 2:
             case 3:
                 return new Space.Coordinates(space.pos.x + 2, space.pos.y + 1);
             case 4:
             case 5:
                 return new Space.Coordinates(space.pos.x + 2, space.pos.y - 1);
             case 6:
             case 7:
                 return new Space.Coordinates(space.pos.x, space.pos.y - 2);
             case 8:
             case 9:
                 return new Space.Coordinates(space.pos.x - 2, space.pos.y - 1);
             case 10:
             case 11:
                 return new Space.Coordinates(space.pos.x - 2, space.pos.y + 1);
             default:
                 throw new IllegalArgumentException("Space token(" + space.marker + ")");
         }
     }
     
     private Space createStartingSpace() {
         Coordinates pos = new Coordinates(0, 0);
         Space space = new Space(pos);
         board.put(pos, space);
         space.state = State.Wall;
         space.marker = 0;
         return space;
     }
 
     private Space locateSpace(Space.Coordinates pos) {
         Space space = board.get(pos);
         if (space == null) {
             space = new Space(pos);
             board.put(pos, space);
             if (outOfBound(pos)) {
                 space.state = State.Wall;
             } else {
                 space.state = State.Playable;
                 space.tile = tiles.pop();
             }
         }
 
         return space;
     }
 
     private void advance() {
         adjacent.traverse();
         current = adjacent;
         adjacent = locateSpace(calculateAdjacentCoordinates(current));
         adjacent.matchMarker(current);
     }
 
     void play() {
         if (adjacent.state != State.Playable) {
             throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
         }
 
         adjacent.state = State.Played;
         advance();
     }
 
     void flow() {
         if (adjacent.state != State.Played) {
             throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
         }
 
         advance();
     }
 
     private boolean outOfBound(final Coordinates pos) {
         final int x = Math.abs((int) pos.x);
         if (x > 6) {
             return true;
         }
 
         final int y = Math.abs(pos.y);
 
         switch (x) {
             case 0:
                 return y > 6;
            case 1:
                return y > 5;
             case 2:
                 return y > 4;
            case 3:
                 return y > 3;
         }
 
         return false;
     }
 
     void putTileBack(final Coordinates pos) {
         board.remove(pos);
         tiles.unpop();
     }
 
     /** Undo play on adjacentPos and set its rotation for another try. */
     void undoPlay(final Coordinates currentPos, final int currentMarker,
             final Coordinates adjacentPos, final int rotation) {
         current = locateSpace(currentPos);
         current.marker = currentMarker;
         adjacent = locateSpace(adjacentPos);
         adjacent.marker = Tile.adjacentNode(currentMarker);
         adjacent.tile.setRotation(rotation);
         adjacent.state = Space.State.Playable;
     }
 }
