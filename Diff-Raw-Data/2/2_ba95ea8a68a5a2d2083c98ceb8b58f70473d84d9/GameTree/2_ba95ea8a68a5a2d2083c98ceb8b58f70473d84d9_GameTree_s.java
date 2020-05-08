 package AI;
 
 public class GameTree {
     
     public GameNode head;
     public GameNode current;
     public int boardWidth;
     public int boardHeight;
     
     /**
      * Basic GameTree constructor that makes an empty game tree
      * 
      * @param num_col Number of columns in the game
      * @param num_row Number of rows in the game
      */
     public GameTree(int num_col, int num_row)
     {
         head = new GameNode(num_col, num_row);
         current = head;
         
         boardWidth = num_col;
         boardHeight = num_row;
     }
     
     /**
      * Creates a GameTree from the formatted from file boardInfo
      * @param boardInfo
      */
     public GameTree(byte[][] gameData)
     {
         head = new GameNode(gameData);
         current = head;
         
         boardWidth = gameData.length;
         boardHeight = gameData[0].length;
     }
    
     
     /**
      * Recursively generates the GameTree by adding a level of children for levelsDeep
      * 
      * @param currNode
      * @param levelsDeep
      * @param isJarvisTurn
      */
     public void GenerateChildren(GameNode currNode, int levelsDeep, boolean isJarvisTurn)
     {
         
         if(levelsDeep == 0){
             return;
         }
         byte colouredNode = Util.gamePiece_r;
         if(isJarvisTurn)
         {
             colouredNode = Util.gamePiece_b;
         }
        int newLevelsDeep = levelsDeep--;
         
         for(int i = 0; i < boardWidth; i++)
         {
             //add coloured node
             if(currNode.children[i]==null){
                 currNode.children[i] = new GameNode(currNode, i, colouredNode);
             }
             GenerateChildren(currNode.children[i],newLevelsDeep,!isJarvisTurn);
             //add space
             if(currNode.children[(boardWidth)*2 -i] ==null){
                 currNode.children[(boardWidth)*2 -i]  = new GameNode(currNode, i, colouredNode);
             }
             GenerateChildren(currNode.children[i],newLevelsDeep,!isJarvisTurn);
             
         }
         
         
          
         
         
     }
 
 }
