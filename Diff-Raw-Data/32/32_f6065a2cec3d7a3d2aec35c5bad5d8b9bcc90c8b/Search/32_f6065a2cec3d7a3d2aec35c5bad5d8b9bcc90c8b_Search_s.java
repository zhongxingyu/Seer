 package com.tipumc;
 
 
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 public final class Search {
 
     public static final class Result
     {
         public Position endPosition;
         public ArrayList<Direction> path;
     }
 
     public static Result dfs(State state, SearchTest test, int startX, int startY)
     {
         /* Create stack to store nodes */
         Stack<Position> nodes = new Stack<Position>();
         /* Create integers that is needed */
         int width = state.getWidth();
         int height = state.getHeight();
         /* Create 2D array to store visited positions */
         Direction visitedPositions[][] = new Direction[width][height];
         /* Declare an positionobject to pop to from stack */
         Position currentPosition = new Position(startX, startY);
         int x = currentPosition.x, y = currentPosition.y;
         
         /* Push the start position node, for the search on the stack */
         nodes.push(currentPosition);
         
         /* Search for a path to the wanted goal */
         while (!nodes.empty()){
             currentPosition = nodes.pop();
             x = currentPosition.x;
             y = currentPosition.y;
             
             if (test.isEnd(state, currentPosition.x, currentPosition.y)){
                 Result result = new Result();
                 result.path = getPath(visitedPositions, currentPosition.x, currentPosition.y, startX, startY);
                 result.endPosition = new Position(currentPosition.x, currentPosition.y);
                 return result;
             }
 
             /* Create child nodes */
             testAddPosition(state, test, nodes, visitedPositions, x, y-1, Direction.UP);
             testAddPosition(state, test, nodes, visitedPositions, x, y+1, Direction.DOWN);
             testAddPosition(state, test, nodes, visitedPositions, x-1, y, Direction.LEFT);
             testAddPosition(state, test, nodes, visitedPositions, x+1, y, Direction.RIGHT);
         }
         return null;
     }
 
     private static ArrayList<Direction> getPath(Direction[][] moves, int fromX, int fromY, int toX, int toY)
     {
         int x = fromX, y = fromY;
         //System.err.println(toX + " TO " + toY);
         //System.err.println(fromX + " FROM " + fromY);
         ArrayList<Direction> path = new ArrayList<Direction>();
         Position dir = new Position();
         while ((y != toY) | (x != toX) )
         {
             //System.err.println(x + " B " + y);
             
             Direction move = moves[x][y];
             path.add(move);
             
             move.getDirection(dir);
             x -= dir.x;
             y -= dir.y;
             //System.err.println(x + " A " + y);
         }
         return path;
     }
 
     public static ArrayList<Position> findAll(State state, SearchTest test, int startX, int startY)
     {
         /* Create stack to store nodes */
         Stack<Position> nodes = new Stack<Position>();
         /* Create integers that is needed */
         int width = state.getWidth();
         int height = state.getHeight();
         /* Create 2D array to store visited positions */
         Direction visitedPositions[][] = new Direction[width][height];
         /* Declare an positionobject to pop to from stack */
         Position currentPosition = new Position(startX, startY);
         int x = currentPosition.x, y = currentPosition.y;
 
         /* Push the start position node, for the search on the stack */
         nodes.push(currentPosition);
 
         ArrayList<Position> result = new ArrayList<Position>();
         /* Search for a path to the wanted goal */
         while (!nodes.empty()){
             currentPosition = nodes.pop();
             x = currentPosition.x;
             y = currentPosition.y;
             if (test.isEnd(state, x, y))
             {
                 result.add(new Position(x, y));
             }
 
             /* Create child nodes */
             testAddPosition(state, test, nodes, visitedPositions, x, y-1, Direction.UP);
             testAddPosition(state, test, nodes, visitedPositions, x, y+1, Direction.DOWN);
             testAddPosition(state, test, nodes, visitedPositions, x-1, y, Direction.LEFT);
             testAddPosition(state, test, nodes, visitedPositions, x+1, y, Direction.RIGHT);
         }
 
         return result;
     }
 
     private static void testAddPosition(State state, SearchTest test, Stack<Position> nodes, Direction[][] visitedPositions, int x, int y, Direction move)
     {
         if (visitedPositions[x][y] == null & (state.isFree(x, y) | test.isEnd(state, x, y))){
             Position possibleStep = new Position(x, y);
             visitedPositions[x][y] = move;
             nodes.push(possibleStep);
         }
     }
 
 
     public static Result findBoxPath(State state, SearchTest test, int startX, int startY, int playerStartX, int playerStartY, int index)
     {
         /* Create stack to store nodes */
         Stack<State> nodes = new Stack<State>();
         /* Create integers that is needed */
         int width = state.getWidth();
         int height = state.getHeight();
         /* Create 2D array to store visited positions */
         Direction visitedPositions[][] = new Direction[width][height];
         /* Declare an positionobject to pop to from stack */
         State currentState = state;
         currentState.player = new Position(playerStartX, playerStartY);
         int x /*= currentPosition.x*/, y /*= currentPosition.y*/;
 
         /* Push the start position node, for the search on the stack */
         nodes.push(currentState);
 
         /* Search for a path to the wanted goal */
         while (!nodes.empty()){
             /*if (currentPosition != null)
             {
                 playerPosition.x = currentPosition.x;
                 playerPosition.y = currentPosition.y;
             }*/
             currentState = nodes.pop();
             x = currentState.boxes.get(index).x;
             y = currentState.boxes.get(index).y;
 
             if (test.isEnd(state, x, y)){
                 Result result = new Result();
                result.path = getPath(visitedPositions, x, y, startX, startY);
                //result.path = getPlayerPath(currentState);
                 result.endPosition = new Position(x, y);
                 return result;
             }
 
             /* Create child nodes */
             testBoxAddPosition(currentState, nodes, visitedPositions, currentState.player, x, y - 1, Direction.UP, index);
             testBoxAddPosition(currentState, nodes, visitedPositions, currentState.player, x, y + 1, Direction.DOWN, index);
             testBoxAddPosition(currentState, nodes, visitedPositions, currentState.player, x - 1, y, Direction.LEFT, index);
             testBoxAddPosition(currentState, nodes, visitedPositions, currentState.player, x + 1, y, Direction.RIGHT, index);
         }
         System.err.println("no path");
         return null;
     }
 
     private static void testBoxAddPosition(State state, Stack<State> nodes, Direction[][] visitedPositions, Position player, int boxX, int boxY, Direction move, int index)
     {
         Result result = new Result();
         result = Search.dfs(state, new IsNextToBox(move, state.boxes.get(index).x, state.boxes.get(index).y), player.x, player.y);
        System.err.println(player.x + " " + player.y + " BOX: " + state.boxes.get(index).x + " " + state.boxes.get(index).y);
         if (visitedPositions[boxX][boxY] == null && state.isFree(boxX, boxY)
             && result != null){
             ArrayList<Position> boxes = new ArrayList<Position>(state.boxes);
             boxes.set(index, new Position(boxX, boxY));
             State possibleStep = new State(state.map, new Position(state.boxes.get(index).x, state.boxes.get(index).y), boxes, result.path, state);
             visitedPositions[boxX][boxY] = move;
             nodes.push(possibleStep);
            System.err.println(result.path);
         }
     }
     
     public static ArrayList<Direction> getPlayerPath(State current){
         if (current.parent == null){
             return current.playerPath;
         } else {
             ArrayList<Direction> tempPath = getPlayerPath(current.parent);
            tempPath.addAll(current.playerPath);
             return tempPath;
         }
         
     }
 }
