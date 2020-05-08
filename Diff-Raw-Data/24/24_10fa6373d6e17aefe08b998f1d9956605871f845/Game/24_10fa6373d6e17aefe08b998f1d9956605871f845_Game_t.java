 package cz.janhrcek.chess.model.impl;
 
 import com.google.inject.Inject;
 import cz.janhrcek.chess.FEN.InvalidFenException;
 import cz.janhrcek.chess.guice.FenString;
 import cz.janhrcek.chess.model.api.GameState;
 import cz.janhrcek.chess.model.api.GameStateFactory;
 import cz.janhrcek.chess.rules.IllegalMoveException;
 import cz.janhrcek.chess.model.api.Move;
import cz.janhrcek.chess.model.api.Promotion;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author jhrcek
  */
 public class Game {
 
     @Inject
     public Game(GameStateFactory gsf, @FenString String initialStateFen) throws InvalidFenException {
         this.stateFactory = gsf;
         GameState initialState = stateFactory.create(initialStateFen);
         this.rootNode = new Node(null, null, initialState, NODE_ID_GENERATOR.getAndIncrement());
         this.focusedNode = rootNode;
         this.id2Node = new HashMap<>();
         id2Node.put(rootNode.getId(), rootNode);
     }
 
     public GameState getRootState() {
         return rootNode.getGameState();
     }
 
     public GameState getFocusedState() {
         return focusedNode.getGameState();
     }
 
     void focusRoot() {
         log.info("Browsing tree: focusing root node");
         focusedNode = rootNode;
     }
 
     public void focusParent() {
         if (focusedNode.parent != null) {
             //if we are not already at the root
             log.info("Browsing tree: focusing parent of the current node");
             focusedNode = focusedNode.parent;
         } else {
             log.info("Browsing tree: can't focus parent, we are already at the root of the tree!");
         }
     }
 
     void focusFirstChild() {
         if (focusedNode.children.size() > 0) {
             log.info("Browsing tree: focusing first child of the current node");
             focusedNode = focusedNode.getChildren().get(0);
         } else {
             log.info("Browsing tree: can't focus child, we are already at the end of current line!");
         }
     }
 
     void focusLeaf() {
         log.info("Browsing tree: moving to the end of the current line");
         while (focusedNode.children.size() > 0) {
             focusedNode = focusedNode.getChildren().get(0);
         }
     }
 
     void focusNodeWithId(int id) {
         log.info("Browsing tree: focusing node with id = {}", id);
         focusedNode = id2Node.get(id);
     }
 
     public void addMove(Move newMove) throws PieceNotPresentException, IllegalMoveException {
         //if currently focused node already has a child preceded by move in the node, just move the gocus
         for (Node childNode : focusedNode.getChildren()) {
             if (childNode.getMove().equals(newMove)) {
                 log.debug("There is already a child with move {} -> just switching focus to that child, not adding the move", newMove);
                 focusedNode = childNode;
                 return;
             }
         }
         log.debug("Adding new GameState using {}", newMove);
         GameState stateAfterMove = stateFactory.create(getFocusedState(), newMove);
         Node newFocusedNode = new Node(focusedNode, newMove, stateAfterMove, NODE_ID_GENERATOR.getAndIncrement());
         id2Node.put(newFocusedNode.getId(), newFocusedNode);
         focusedNode.addChild(newFocusedNode);
         focusedNode = newFocusedNode;
     }
 
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         result.append("<html><body>");
         histNodeToString(rootNode, result);
         result.append("</body></html>");
         return result.toString();
     }
 
 //--------------------------- PRIVATE IMPLEMENTATION ---------------------------
     private void histNodeToString(Node node, StringBuilder sb) { //TODO - fix order of displaying variations
         String startTag = node.equals(focusedNode) ? "&nbsp;<a class=\"focus\" href=\"" : "&nbsp;<a href=\""; //We want to highlight the focused move
         sb.append(startTag).append(node.getId());
 
         if (node.getMove() != null) {
             sb.append("\">")
                     .append(node.toLan())
                     .append("</a>")
                     .append(node.getGameState().isWhiteToMove() ? "<br/>" : ""); //line break after black's move
         } else { //Case for root node (= initial state of the game)- has no parent
             sb.append("\">Start</a><br/>");
         }
 
         if (node.getChildren().size() >= 1) {
             histNodeToString(node.getChildren().get(0), sb);       //1) Main line
         }
         if (node.getChildren().size() > 1) {
             sb.append("(");                                        //2) Variations
             for (int i = 1; i < node.getChildren().size(); i++) {
                 histNodeToString(node.getChildren().get(i), sb);
                 sb.append(";");
             }
             sb.append(")");
         }
     }
     private static final Logger log = LoggerFactory.getLogger(Game.class);
     private final GameStateFactory stateFactory;
     private final Node rootNode;
     private Node focusedNode;
     private final Map<Integer, Node> id2Node;
     private final AtomicInteger NODE_ID_GENERATOR = new AtomicInteger(0);
 
     private static class Node {
 
         private static final Logger log = LoggerFactory.getLogger(Node.class);
         //link to parent (to enable taking back of moves - moving to previous states)
         private final Node parent;
         //list of children
         private final List<Node> children;
         //Data
         private final Move move;
         private final GameState state;
        private final int ID; //Unique ID withing a game
 
         public Node(Node parent, Move move, GameState s, int id) {
             this.parent = parent;
             this.move = move;
             this.state = s;
             this.ID = id;
             this.children = new ArrayList<>();
 
         }
 
         public GameState getGameState() {
             return state;
         }
 
         public Move getMove() {
             return move;
         }
 
         public void addChild(Node child) {
             log.debug("Adding new child node for move {}", child.getMove());
             children.add(child);
         }
 
         public List<Node> getChildren() {
             return children;
         }
 
         public Node getParent() {
             return parent;
         }
 
         public int getId() {
             return ID;
         }
 
         /**
          * Takes the node and converts the move, that led to this node to Long
          * Algebraic Notation (LAN).
          *
          * @return Long Algebraic Notation representation of the move
          */
         private String toLan() {
             if (getParent() == null) {
                throw new IllegalArgumentException("Cannot convert root node to LAN!");
             }
 
             StringBuilder result = new StringBuilder();
 
             String fullMoveNumber = getGameState().isWhiteToMove() ? "" : getGameState().getFullmoveNumber() + ".";
            Move move = getMove();

            String pieceLetter = move.getPiece().getSanName();
            String fromSquare = move.getFrom().toString().toLowerCase();
            boolean wasCapture = getParent().getGameState().getPosition().getPiece(move.getTo()) != null;
            String toSquare = move.getTo().toString().toLowerCase();
            String promotion = (move instanceof Promotion) ? "=" + ((Promotion) move).getPromoPiece().getSanName() : "";
 
             result.append(fullMoveNumber)
                     .append(pieceLetter)
                     .append(fromSquare)
                     .append(wasCapture ? "x" : "-")
                    .append(toSquare)
                    .append(promotion);

             return result.toString();
         }
     }
 }
