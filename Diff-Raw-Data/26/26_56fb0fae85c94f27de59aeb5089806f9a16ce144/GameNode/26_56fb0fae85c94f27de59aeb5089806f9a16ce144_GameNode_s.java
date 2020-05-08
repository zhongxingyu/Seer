 //package AI;
 
 /***
  * A node that represents a paticular board setup in the decision tree.
  * 
  * @author hoshi
  *
  */
 public class GameNode {
     
     public GameTree gameTree;
     public GameNode parent;
     public GameNode[] children;
     public byte gamePiece;
     public int column;
     public double score = 0;
     public double childrenScore = 0;
 
     /**
      * Initial GameNode, used as parent node in the GameTree
      * @param boardWidth
      */
     public GameNode(int boardWidth, GameTree gameTree){
         this.gameTree = gameTree;
         children = new GameNode[boardWidth * 2];        
     }
     
     /**
      * A child GameNode representing an additional piece played 
      * @param parent
      * @param xPos
      * @param piece
      * @param boardWidth
      */
     public GameNode(GameNode parent, int xPos, byte piece, int boardWidth){        
         this.parent = parent;
         column = xPos;
         gamePiece = piece;
         children = new GameNode[boardWidth * 2];        
     }
     
     public double calculateScore(IsWin isWin, int last_col){        
         score = isWin.winFunction(this, last_col);        
         return score;
     }
     
     public double calculateChildrensScore(){
        double totalScore = score;
         if(children == null || children.length == 0){
            return score;
        }        
         for(GameNode gameNode: children){
             if(gameNode == null){
                 continue;
             }
             totalScore += gameNode.calculateChildrensScore();
         }
        return totalScore;
     }
 
 }
