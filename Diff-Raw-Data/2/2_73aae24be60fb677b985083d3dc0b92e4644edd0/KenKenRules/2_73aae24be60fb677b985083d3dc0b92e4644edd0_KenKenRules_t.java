 public class KenKenRules implements Rule  {
     
     private Game game;
     private int dimensions;
     
     public KenKenRules(Game g){
         game = g;
         dimensions = g.getDimensions()[0];
     }
     
 
     public boolean row(Space s){
         
         int y = s.getY();
         
         for (int i = 0; i<dimensions; i++){
             if (game.getSpaceAt(i,y).getValue() == s.getValue()){
                 if (i != s.getX())
                     return false;
             }
         }
         
         return true;
         
     }
     
     
     public boolean column(Space s){
         
         int x = s.getX();
         
         for (int i = 0; i < dimensions; i++){
             if (game.getSpaceAt(x,i).getValue() == s.getValue()){
                 if (i != s.getY())
                     return false;
             }
         }
         
         return true;
     }
     
     public boolean shape(Space s){
         
         Group g = s.getGroup();
         String op = g.getOp();
         Space[] spaces = g.getSpaces();
         
         int cumulative = spaces[0].getValue();
                 
       
         if (op.equals("*")){
             for (int i = 1; i<spaces.length;i++){
                 int n = spaces[i].getValue();
                 cumulative*=spaces[i].getValue();
                 
             }
         }
         else if (op.equals("+")){
             for (int i = 1; i<spaces.length;i++){
                 int n = spaces[i].getValue();
                 cumulative+=n;
             }
         }
         
         else if (op.equals("-")){
             for (int i = 1; i<spaces.length;i++){
                 int n = spaces[i].getValue();
                 cumulative-=n;
             }
         }
         else if (op.equals("/")){
             for (int i = 1; i<spaces.length;i++){
                 if (spaces[i].getValue()!=0){
                     int n = spaces[i].getValue();
                     cumulative/=n;
                 }
             }
         }
        else cumulative = s.getValue();
         
         if (g.anyEmpty())
             return true;
         
         if (cumulative == g.getTotal())
             return true;
                     
         return false;
     }
         
     public boolean constraints(Space s){
         
         if (shape(s) && row(s) && column(s))
             return true;
         return false;
     }
     
     
     public boolean allConstraints(){
         
         Group[] g = game.getGroup();
         
         for (int i = 0; i < g.length; i++){
             if (!shape(g[i].getSpace(0)))
                 return false;
         }
         
         return true;
     }
 
 }
