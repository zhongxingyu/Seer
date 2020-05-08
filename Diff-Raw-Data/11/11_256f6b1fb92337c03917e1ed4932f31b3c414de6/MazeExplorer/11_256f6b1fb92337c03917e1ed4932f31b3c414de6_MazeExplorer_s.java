 package mazeAlgorithm;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import mapping.Barcode;
 import mapping.ExtMath;
 import mapping.Orientation;
 import mapping.Seesaw;
 import mapping.Tile;
 import simulator.pilot.AbstractPilot;
 
 public class MazeExplorer {
 
     private final Vector<Tile> allTiles = new Vector<Tile>();
     private final Vector<Tile> queue = new Vector<Tile>();
     private Tile startTile;
     private AbstractPilot pilot;
     private boolean align;
     private final int amountOfTilesUntilAlign = 0;
     private int currentAmount;
     private boolean quit = false;
 
     public MazeExplorer(final Tile startTile, final AbstractPilot pilot,
             boolean align) {
         this.startTile = startTile;
         this.pilot = pilot;
         this.align = align;
         currentAmount = amountOfTilesUntilAlign;
     }
 
     private void algorithm(Tile currentTile) {
         // Explore tile and set current tile on "Explored".
         if (!currentTile.isMarkedExploreMaze()) {
             exploreTile(currentTile);
         }
 
         // Update queue.
         for (final Tile neighbourTile : currentTile.getReachableNeighbours()) {
             if (neighbourTile != null && !(neighbourTile.isMarkedExploreMaze())) {
                 queue.add(neighbourTile);
             }
         }
 
         // Algorithm finished?
         if (queue.isEmpty() || quit) {
             System.out.println("[EXPLORE] Robot " + pilot.getPlayerNumber()
                     + " has finished exploring.");
             return;
         }
 
         // Get next optimal tile.
         Tile nextTile = getPriorityNextTile(currentTile);
         while (allTiles.contains(nextTile)) {
             currentTile = nextTile;
             if (!currentTile.isMarkedExploreMaze()) {
                 exploreTile(currentTile);
             }
             for (final Tile neighbourTile : currentTile
                     .getReachableNeighbours()) {
                 if (neighbourTile != null
                         && !(neighbourTile.isMarkedExploreMaze())) {
                     queue.add(neighbourTile);
                 }
             }
             if (queue.isEmpty() || quit) {
                 System.out.println("[EXPLORE] Robot " + (pilot.getPlayerNumber()+1)
                         + " has finished exploring.");
                 return;
             }
             nextTile = getPriorityNextTile(currentTile);
         }
 
         // Is the next tile useful?
         while (!doesHaveOtherNeighboursToCheck(nextTile)) {
             nextTile.setMarkingExploreMaze(true);
             allTiles.add(nextTile);
             removeTileFromQueue(nextTile);
 
             if (queue.isEmpty() || quit) {
                 System.out.println("[EXPLORE] Robot " + pilot.getPlayerNumber()
                         + " has finished exploring.");
                 return;
             }
 
             nextTile = getPriorityNextTile(currentTile);
             while (allTiles.contains(nextTile)) {
                 currentTile = nextTile;
                 if (!currentTile.isMarkedExploreMaze()) {
                     exploreTile(currentTile);
                 }
                 for (final Tile neighbourTile : currentTile
                         .getReachableNeighbours()) {
                     if (neighbourTile != null
                             && !(neighbourTile.isMarkedExploreMaze())) {
                         queue.add(neighbourTile);
                     }
                 }
                 if (queue.isEmpty() || quit) {
                     System.out.println("[EXPLORE] Robot "
                             + pilot.getPlayerNumber()
                             + " has finished exploring.");
                     return;
                 }
                 nextTile = getPriorityNextTile(currentTile);
             }
         }
 
         // Add the current tile to the finish-queue and remove it from the
         // todo-queue.
         allTiles.add(nextTile);
         removeTileFromQueue(nextTile);
 
         // Go to the next tile.
         final ShortestPath shortestPath = new ShortestPath(pilot, currentTile,
                 nextTile, allTiles);
         currentAmount = shortestPath.goShortestPath(align, currentAmount,
                 amountOfTilesUntilAlign);
 
         // the next tile contains a barcode.
         // this means that the robot can alter its mapping. therefore, the queue
         // has to be updated
         if (nextTile.getContent() instanceof Barcode) {
             checkExploredQueue();
 
             while (pilot.isExecutingBarcode()) {
                 try {
                     Thread.sleep(100);
                 } catch (Exception e) {
 
                 }
             }
         }
         // Repeat with next tile.
         algorithm(nextTile);
     }
 
     /**
      * Checks whether the queue only contains unexplored tiles and whether all
      * unexplored tiles are in the queue. For, it is possible that a pilot
      * explores and adds some tiles by itself. For example, when he finds a
      * barcode that says that there is a treasure - and a dead-end - on the next
      * tile.
      */
     private void checkExploredQueue() {
         for (Tile tile : pilot.getMapGraphConstructed().getTiles()) {
             if (!allTiles.contains(tile) && !queue.contains(tile)) {
                 if (tile.isMarkedExploreMaze()) {
                     allTiles.add(tile);
                 } else {
                     queue.add(tile);
                 }
             }
         }
     }
 
     /**
      * checkt of alle neighbourTiles al gecheckt zijn, indien ja niet meer nodig
      * om nextTile nog te checken.
      */
     private boolean doesHaveOtherNeighboursToCheck(final Tile nextTile) {
         int j = 0;
         // if (!nextTile.isStraightTile())
         for (final Tile neighbourTile : nextTile.getNeighbours()) {
             if (neighbourTile != null && neighbourTile.isMarkedExploreMaze()) {
                 j++;
             }
         }
         if (j == 4) {
             return false;
         }
         return true;
     }
 
     /**
      * Checkt voor alle neighbours of ze al behandeld zijn. Indien niet, checkt
      * of er een muur tussen staat. Indien wel, plaats een muur. Indien niet,
      * voeg een nieuwe lege tile toe op de juiste plaats.
      */
     private void exploreTile(final Tile currentTile) {
         // numbervariable met als inhoud het getal van de orientatie (N = 1,...)
         int numberVariable = pilot.getOrientation().ordinal();
 
         // Array met alle buren van deze tile
         final ArrayList<Tile> array = currentTile.getNeighbours();
 
         for (int i = 0; i < 4; i++) {
             // Do nothing if tile with numbervariable as orientation is already
             // done
             if (array.get(numberVariable) != null
                     && (array.get(numberVariable).isMarkedExploreMaze())) {
                 ;
             } else {
                 double angle = (((numberVariable - pilot.getOrientation()
                         .ordinal()) * 90) + 360) % 360;
                 angle = ExtMath.getSmallestAngle(angle);
                 pilot.rotate(angle);
                 pilot.setObstructionOrTile();
             }
             // Next orientation
             numberVariable = numberVariable + 1;
             if (numberVariable == 4) {
                 numberVariable = 0;
             }
         }
 
         // zet het mark-veld van de currentTile op true zodat deze niet meer
         // opnieuw in de queue terecht kan komen
         currentTile.setMarkingExploreMaze(true);
     }
 
     private Tile getOtherEndOfSeesaw(Tile tile) {
         for (Tile neighbour : tile.getReachableNeighboursIgnoringSeesaw()) {
             if (neighbour != null && neighbour.getContent() instanceof Seesaw) {
                 for (Tile nextNeighbour : neighbour
                         .getReachableNeighboursIgnoringSeesaw()) {
                     if (nextNeighbour != null
                             && nextNeighbour.getContent() instanceof Seesaw) {
                         for (Tile nextNextNeighbour : nextNeighbour
                                 .getReachableNeighboursIgnoringSeesaw()) {
                             if (nextNextNeighbour != null
                                     && nextNextNeighbour.getContent() instanceof Barcode) {
                                 for (Tile nextNextNextNeighbour : nextNextNeighbour
                                         .getReachableNeighboursIgnoringSeesaw()) {
                                     if (nextNextNextNeighbour != null
                                             && !(nextNextNextNeighbour
                                                     .getContent() instanceof Seesaw)
                                             && !nextNextNextNeighbour
                                                     .equals(tile)) {
                                         return nextNextNextNeighbour;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * Check for every orientation whether the given tile has a neighbour tile
      * that is worth visiting. If none of them is, give the last tile from the
      * queue.
      * 
      * @param currentTile
      * @return
      */
     private Tile getPriorityNextTile(final Tile currentTile) {
         if (isGoodNextTile(currentTile, pilot.getOrientation())) {
             return currentTile.getEdgeAt(pilot.getOrientation()).getNeighbour(
                     currentTile);
         } else if (isGoodNextTile(currentTile, pilot.getOrientation()
                 .getCounterClockwiseOrientation())) {
             return currentTile.getEdgeAt(
                     pilot.getOrientation().getCounterClockwiseOrientation())
                     .getNeighbour(currentTile);
         } else if (isGoodNextTile(currentTile, pilot.getOrientation()
                 .getCounterClockwiseOrientation().getOppositeOrientation())) {
             return currentTile.getEdgeAt(
                     pilot.getOrientation().getCounterClockwiseOrientation()
                             .getOppositeOrientation())
                     .getNeighbour(currentTile);
         } else if (isGoodNextTile(currentTile, pilot.getOrientation()
                 .getOppositeOrientation())) {
             return currentTile.getEdgeAt(
                     pilot.getOrientation().getOppositeOrientation())
                     .getNeighbour(currentTile);
         } else {
             Tile loopdetect = queue.lastElement();
             Tile tile = queue.lastElement();
             while (!isReachableWithoutWip(currentTile, tile, new Vector<Tile>())) {
                 queue.remove(tile);
                 queue.add(0, tile);
                 tile = queue.lastElement();
                 if (tile.equals(loopdetect)) {
                     return searchAndCrossOpenSeesaw(currentTile);
                 }
             }
             return queue.lastElement();
         }
     }
 
     /**
      * A tile next to the current tile (in the given orientation) is a good one
      * to visit if: - the edge between the currentTile is passable - the tile is
      * existing - the tile is in the queue
      * 
      * @param currentTile
      * @param orientation
      * @return
      */
     private boolean isGoodNextTile(final Tile currentTile,
             final Orientation orientation) {
         return currentTile.getEdgeAt(orientation).isPassable()
                 && currentTile.getEdgeAt(orientation).getNeighbour(currentTile) != null
                 && queue.contains(currentTile.getEdgeAt(orientation)
                         .getNeighbour(currentTile));
     }
 
     private boolean isReachableWithoutWip(Tile currentTile, Tile endTile,
             Vector<Tile> tilesPath) {
         tilesPath.add(currentTile);
         for (final Tile neighbourTile : currentTile.getReachableNeighbours()) {
             if (neighbourTile.equals(endTile)) {
                 return true;
             }
             if (!tilesPath.contains(neighbourTile)
                     && allTiles.contains(neighbourTile)
                     && !(neighbourTile.getContent() instanceof Seesaw)) {
                 if (isReachableWithoutWip(neighbourTile, endTile, tilesPath)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public void quit() {
         quit = true;
     }
 
     private void removeTileFromQueue(final Tile tile) {
         // Multiple times in queue so multiple times remove.
         while (queue.contains(tile)) {
             queue.remove(tile);
         }
         // TODO: Somehow some tiles do not get removed... Investigate! AND FIX THAT LAST DAMN THING :(
         for (Tile queuetile : queue) {
             if (queuetile.getPosition().x == tile.getPosition().x && queuetile.getPosition().y == tile.getPosition().y)
                 queue.remove(queuetile);
         	if(queuetile.isMarkedExploreMaze())
         		queue.remove(queuetile);
        	for(Tile allTilesTile : allTiles)
        		if(allTilesTile.getPosition().x == queuetile.getPosition().x && allTilesTile.getPosition().y == queuetile.getPosition().y)
        			;//queue.remove(queuetile);
         }
     }
 
     private Tile searchAndCrossOpenSeesaw(Tile currentTile) {
         while (true) {
             pilot.shuffleSeesawBarcodeTiles();
             for (Tile tile : pilot.getSeesawBarcodeTiles()) {
                 if (isReachableWithoutWip(currentTile, tile, new Vector<Tile>())) {
                     ShortestPath shortestPath = new ShortestPath(pilot,
                             currentTile, tile, allTiles);
                     currentAmount = shortestPath.goShortestPath(align,
                             currentAmount, amountOfTilesUntilAlign);
                     currentTile = tile;
                     checkExploredQueue();
                     while (pilot.isExecutingBarcode()) {
                         try {
                             Thread.sleep(100);
                         } catch (Exception e) {
 
                         }
                     }
                     for (Tile neighbour : currentTile
                             .getReachableNeighboursIgnoringSeesaw()) {
                         if (neighbour.getContent() instanceof Seesaw
                                 && !((Seesaw) neighbour.getContent())
                                         .isClosed()) {
                             Tile endTile = getOtherEndOfSeesaw(currentTile);
                             pilot.setReadBarcodes(false);
                             pilot.travel(140);
                             pilot.setReadBarcodes(true);
                             if (!allTiles.contains(endTile)) {
                                 allTiles.add(endTile);
                             }
                             removeTileFromQueue(endTile);
                             pilot.travel(20); // Zodat een eventuele barcode op
                                               // volgende tile wel gelezen wordt
                                               // maar de laatste barcode van de
                                               // wip niet.
                             //TODO: whiteline! want onnauwkeurig na wip!
                             return endTile;
                         }
                     }
                     // Wip is gesloten, dus rij 1 tegel achteruit
                     for (Tile neighbour : currentTile
                             .getReachableNeighboursIgnoringSeesaw()) {
                         if (!(neighbour.getContent() instanceof Seesaw)) {
                             shortestPath = new ShortestPath(pilot, currentTile,
                                     neighbour, allTiles);
                             currentAmount = shortestPath.goShortestPath(align,
                                     currentAmount, amountOfTilesUntilAlign);
                             currentTile = neighbour;
                             checkExploredQueue();
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Deze methode wordt opgeroepen als het object het algoritme moet uitvoeren
      */
     public void startExploringMaze() {
         try {
             allTiles.add(startTile);
             algorithm(startTile);
             for (final Object tile : allTiles) {
                 ((Tile) tile).setMarkingExploreMaze(false);
             }
 
         } catch (NullPointerException e) {
             // if(!quit)
             // System.out.println("Exception in MazeExplorer!");
             e.printStackTrace();
         }
     }
 }
