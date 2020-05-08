 package RockContorter;
 
 import java.awt.*;
 
 public class Game {
     public Board Board;
     public boolean mGameOver;
     public boolean Victory = false;
     public boolean ThrewRock = false;
     public boolean ThrewShield = false;
 
     public boolean FaceRight;
     public boolean FaceLeft;
     public boolean FaceUp;
     public boolean FaceDown;
 
     public int moveCount = 0;
     public int updateCount = 0;
     public int ThrowCounter = 0;
 
     public Point PlayerPosition;
     public Point LeapPosition;
     public Point RockShield;
     public Point ThrowRock;
     public Point newRockPosition;
     public Point newThrowRockPosition;
     public Point TunnelPosition;
 
     public Game(Board pBoard) {
         Board = pBoard;
         PlayerPosition = new Point(7, 5);
     }
 
     public enum Move {
         MOVELeft,
         MOVERight,
         MOVEUp,
         MoveDown
     }
 
     public void Update() {
         if  ((updateCount == 0)) {
             MoveThrowRock();
             updateCount = 0;
         } else {
             updateCount = updateCount + 1;
         }
     }
 
     public void Move (Move pMove) {
         Point newPosition = ConvertMoveToCoordinates(pMove);
         BoardState state = Board.GetState(newPosition);
         if (mGameOver) {
             return;
         }
         if (newPosition.y < PlayerPosition.y) {
             FaceUp = true;
             FaceRight = false;
             FaceDown = false;
             FaceLeft = false;
         } else if (newPosition.x < PlayerPosition.x){
             FaceUp = false;
             FaceRight = false;
             FaceDown = false;
             FaceLeft = true;
         } else if (newPosition.y > PlayerPosition.y){
             FaceUp = false;
             FaceRight = false;
             FaceDown = true;
             FaceLeft = false;
         } else if (newPosition.x > PlayerPosition.x) {
             FaceUp = false;
             FaceRight = true;
             FaceDown = false;
             FaceLeft = false;
         }
         if ((state == BoardState.WALL)||(state == BoardState.ROCK) || (newPosition == newRockPosition)) {
             // dont move him
         } else {
             PlayerPosition = newPosition;
         }
     }
 
     public Point RockPlace(){
         if (FaceUp && !FaceLeft && !FaceDown && ! FaceRight){
             newRockPosition = new Point(PlayerPosition.x, PlayerPosition.y - 1);
         }
         if (FaceLeft && !FaceDown && !FaceRight && !FaceUp){
             newRockPosition = new Point(PlayerPosition.x - 1, PlayerPosition.y);
         }
         if (FaceRight && !FaceDown && !FaceLeft && !FaceUp){
             newRockPosition = new Point(PlayerPosition.x + 1, PlayerPosition.y);
         }
         if (FaceDown && !FaceLeft && !FaceRight && !FaceUp){
             newRockPosition = new Point(PlayerPosition.x, PlayerPosition.y + 1);
         }
         RockShield = newRockPosition;
         Board.RockShieldAsBoardState(RockShield);
         return RockShield;
     }
 
     public boolean WallInTheWay(Point position) {
         return (Board.GetState(position) == BoardState.WALL);
     }
     public boolean RockInTheWay(Point position) {
         return (Board.GetState(position) == BoardState.ROCK);
     }
 
     public void RockThrowPositionRock(){
         if (FaceUp && !FaceLeft && !FaceDown && ! FaceRight){
             newThrowRockPosition = new Point(PlayerPosition.x, PlayerPosition.y - 1);
         }
         if (FaceLeft && !FaceDown && !FaceRight && !FaceUp){
             newThrowRockPosition = new Point(PlayerPosition.x - 1, PlayerPosition.y);
         }
         if (FaceRight && !FaceDown && !FaceLeft && !FaceUp){
             newThrowRockPosition = new Point(PlayerPosition.x + 1, PlayerPosition.y);
         }
         if (FaceDown && !FaceLeft && !FaceRight && !FaceUp){
             newThrowRockPosition = new Point(PlayerPosition.x, PlayerPosition.y + 1);
         }
     }
 
     public Point MoveThrowRock(){
         BoardState state = Board.GetState(RockShield);
         if (RockShield == null){
             return new Point(1,1);
         }
         if (!ThrewRock) {
             return new Point(1,2);
         }
         ThrowCounter = ThrowCounter + 1;
         if ((state == BoardState.ROCK) && (ThrowCounter < 2)){
             Board.BackToEmpty(RockShield);
             ThrewShield = true;
         }
         if (FaceUp){
             newThrowRockPosition = new Point(newThrowRockPosition.x, newThrowRockPosition.y - 1);
         } else if (FaceLeft){
             newThrowRockPosition = new Point(newThrowRockPosition.x - 1, newThrowRockPosition.y);
         } else if (FaceRight) {
             newThrowRockPosition = new Point(newThrowRockPosition.x + 1, newThrowRockPosition.y);
         } else {
             newThrowRockPosition = new Point(newThrowRockPosition.x, newThrowRockPosition.y + 1);
         }
         if (!WallInTheWay(newThrowRockPosition) && !RockInTheWay(newThrowRockPosition)) {
             ThrowRock = newThrowRockPosition;
         } else {
             ThrowRock = null;
             ThrewRock = false;
             RockShield = null;
         }
         return ThrowRock;
     }
 
     public void RockTunnel(Point TunnelPosition){
         if (FaceUp && !FaceLeft && !FaceDown && ! FaceRight){
             TunnelPosition = new Point(PlayerPosition.x, PlayerPosition.y - 1);
         }
         if (FaceLeft && !FaceDown && !FaceRight && !FaceUp){
             TunnelPosition = new Point(PlayerPosition.x - 1, PlayerPosition.y);
         }
         if (FaceRight && !FaceDown && !FaceLeft && !FaceUp){
             TunnelPosition = new Point(PlayerPosition.x + 1, PlayerPosition.y);
         }
         if (FaceDown && !FaceLeft && !FaceRight && !FaceUp){
             TunnelPosition = new Point(PlayerPosition.x, PlayerPosition.y + 1);
         }
        if (RockInTheWay(TunnelPosition)){
         Board.BoardGrid.put(TunnelPosition, BoardState.EMPTY);
        }
     }
 
     public Point Leap(){
         if (FaceUp){
             LeapPosition = new Point(PlayerPosition.x, PlayerPosition.y - 3);
             if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                 PlayerPosition = LeapPosition;
                 return PlayerPosition;
             } else
                 LeapPosition = new Point(PlayerPosition.x, PlayerPosition.y - 2);
                 if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                     PlayerPosition = LeapPosition;
                     return PlayerPosition;
                 } else
                     LeapPosition = new Point(PlayerPosition.x, PlayerPosition.y - 1);
                     if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                         PlayerPosition = LeapPosition;
                         return PlayerPosition;
                     } else
                         return new Point(1,1);
 
         } else if (FaceLeft){
             LeapPosition = new Point(PlayerPosition.x - 3, PlayerPosition.y);
             if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                 PlayerPosition = LeapPosition;
                 return PlayerPosition;
             } else
                 LeapPosition = new Point(PlayerPosition.x - 2, PlayerPosition.y);
                 if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                     PlayerPosition = LeapPosition;
                     return PlayerPosition;
                 } else
                     LeapPosition = new Point(PlayerPosition.x - 1, PlayerPosition.y);
                     if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                         PlayerPosition = LeapPosition;
                         return PlayerPosition;
                     } else
                         return new Point(1,1);
 
         } else if (FaceRight){
             LeapPosition = new Point(PlayerPosition.x + 3, PlayerPosition.y);
             if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                 PlayerPosition = LeapPosition;
                 return PlayerPosition;
             }else
                 LeapPosition = new Point(PlayerPosition.x + 2, PlayerPosition.y);
                 if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                     PlayerPosition = LeapPosition;
                     return PlayerPosition;
                 } else
                     LeapPosition = new Point(PlayerPosition.x + 1, PlayerPosition.y);
                     if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                         PlayerPosition = LeapPosition;
                         return PlayerPosition;
                     } else
                         return new Point(1,1);
 
         } else
             LeapPosition = new Point(PlayerPosition.x, PlayerPosition.y + 3);
             if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                 PlayerPosition = LeapPosition;
                 return PlayerPosition;
             } else
                 LeapPosition = new Point(PlayerPosition.x, PlayerPosition.y + 2);
                 if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                     PlayerPosition = LeapPosition;
                     return PlayerPosition;
                 } else
                     LeapPosition = new Point(PlayerPosition.x, PlayerPosition.y + 1);
                     if (!WallInTheWay(LeapPosition) && !RockInTheWay(LeapPosition)){
                         PlayerPosition = LeapPosition;
                         return PlayerPosition;
                     } else
                         return new Point(1,1);
     }
 
     private Point ConvertMoveToCoordinates(Move pMove) {
         Point newPosition;
         switch (pMove) {
             case MOVELeft:
                 newPosition = new Point(PlayerPosition.x - 1, PlayerPosition.y);
                 break;
             case MOVERight:
                 newPosition = new Point(PlayerPosition.x + 1, PlayerPosition.y);
                 break;
             case MOVEUp:
                 newPosition = new Point(PlayerPosition.x, PlayerPosition.y - 1);
                 break;
             default:
                 newPosition = new Point(PlayerPosition.x, PlayerPosition.y + 1);
                 break;
         }
         return newPosition;
     }
 
     public boolean GameOver() {
         return mGameOver;
     }
 }
 
 
 
 
