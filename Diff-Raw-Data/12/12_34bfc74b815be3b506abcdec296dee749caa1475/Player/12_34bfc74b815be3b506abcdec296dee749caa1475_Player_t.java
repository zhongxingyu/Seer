 package players;
 
 import common.GameField;
 
 public class Player {
     private char symbol;
     private char playerType;
     private int x,y;
 
     public Player(char c, char t){
         symbol = c;
         playerType = t;
     }
 
     public char getSymbol(){
         return symbol;
     }
 
     public void setX(int x){
         this.x = x;
     }
 
     public void setY(int y){
         this.y = y;
     }
 
     public int getX(){
         return x - GameField.RULER_MIN_VALUE;
     }
 
     public int getY(){
         return y - GameField.RULER_MIN_VALUE;
     }
 
     public boolean checkNum(int num, GameField field){
        return (num <= field.getFieldLength() && num >= GameField.RULER_MIN_VALUE);
     }
 
     public void step(GameField gameField){
 
         if(playerType == 'H'){
             stepHuman(gameField);
         }
         else if(playerType == 'C'){
             stepComputer(gameField);
         }
         if(!checkNum(y,gameField)) return;
         if(!checkNum(x,gameField)) return;
         gameField.setPoint(this);
         gameField.checkWin(this);
     }
 
     public void stepHuman(GameField gameField){}
     public void stepComputer(GameField gameField){}
 }
