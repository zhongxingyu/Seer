 package RockContorter;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 public class Game {
     public Board Board;
     public boolean mGameOver;
     public boolean Victory = false;
     public boolean ThrewRock = false;
     public boolean MadeWave = false;
 
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
     private Point newRockPosition;
     private Point newThrowRockPosition;
     public Point IsThereARockHere;
 
     public java.util.List<Point> Shell;
 
     public Point ChopWallSection1;
     public Point ChopWallSection2;
     public Point ChopWallSection3;
 
     public Point TopOfShell;
     public Point LeftOfShell;
     public Point RightOfShell;
     public Point BottomOfShell;
 
 
     public Game(Board pBoard) {
         Board = pBoard;
         PlayerPosition = new Point(7, 5);
 
         Shell = new ArrayList<Point>();
         Shell.add(new Point());
         Shell.add(new Point());
         Shell.add(new Point());
         Shell.add(new Point());
 
     }
 
     public enum Move {
         MOVELeft,
         MOVERight,
         MOVEUp,
         MoveDown
     }
 
     public void Update() {
         if  ((updateCount == 0)) {
             ThrowRock();
             RockWave();
             updateCount = 0;
         } else {
             updateCount = updateCount + 1;
         }
     }
 
     public void Move (Move pMove) {
         Point newPosition = ConvertMoveToCoordinates(pMove);
         BoardState state = Board.GetState(newPosition);
         state.Update();
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
         if ((state instanceof Wall)||(state instanceof Static_Rock) || (newPosition == newRockPosition)) {
             // dont move him
         } else {
             PlayerPosition = newPosition;
         }
     }
 
     public Point PositionRock(){
         //the engine for the positions of blocking and throwing rocks
         if (FaceUp && !FaceLeft && !FaceDown && ! FaceRight){
             newThrowRockPosition = new Point(PlayerPosition.x, PlayerPosition.y - 1);
             IsThereARockHere = new Point(PlayerPosition.x, PlayerPosition.y - 1);
         }
         if (FaceLeft && !FaceDown && !FaceRight && !FaceUp){
             newThrowRockPosition = new Point(PlayerPosition.x - 1, PlayerPosition.y);
             IsThereARockHere = new Point(PlayerPosition.x - 1, PlayerPosition.y);
         }
         if (FaceRight && !FaceDown && !FaceLeft && !FaceUp){
             newThrowRockPosition = new Point(PlayerPosition.x + 1, PlayerPosition.y);
             IsThereARockHere = new Point(PlayerPosition.x + 1, PlayerPosition.y);
         }
         if (FaceDown && !FaceLeft && !FaceRight && !FaceUp){
             newThrowRockPosition = new Point(PlayerPosition.x, PlayerPosition.y + 1);
             IsThereARockHere = new Point(PlayerPosition.x, PlayerPosition.y + 1);
         }
         return newThrowRockPosition;
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
         Board.RockAsBoardState(RockShield);
         return RockShield;
     }
 
     public void RockShell(){
 
         Shell.set(0, new Point(PlayerPosition.x, PlayerPosition.y - 1));
         TopOfShell = Shell.get(0);
         Board.RockAsBoardState(Shell.get(0));
 
         Shell.set(1, new Point(PlayerPosition.x - 1, PlayerPosition.y));
         LeftOfShell = Shell.get(1);
         Board.RockAsBoardState(Shell.get(1));
 
         Shell.set(2, new Point(PlayerPosition.x + 1, PlayerPosition.y));
         RightOfShell = Shell.get(2);
         Board.RockAsBoardState(Shell.get(2));
 
         Shell.set(3, new Point(PlayerPosition.x, PlayerPosition.y + 1));
         BottomOfShell = Shell.get(3);
         Board.RockAsBoardState(Shell.get(3));
     }
 
     public void RockWave(){
         if (MadeWave){
             if (Shell.get(0) != null){
             Shell.set(0, new Point(Shell.get(0).x, Shell.get(0).y - 1));
             }
             if (Shell.get(1) != null){
             Shell.set(1, new Point(Shell.get(1).x - 1, Shell.get(1).y));
             }
             if (Shell.get(2) != null) {
                 Shell.set(2, new Point(Shell.get(2).x + 1, Shell.get(2).y));
             }
             if (Shell.get(3) != null){
             Shell.set(3, new Point(Shell.get(3).x, Shell.get(3).y + 1));
             }
             if (WallInTheWay(Shell.get(0)) || RockInTheWay(Shell.get(0))) {
                 Shell.set(0, null);
             } else {
                 TopOfShell = Shell.get(0);
             }
             if (WallInTheWay(Shell.get(1)) || RockInTheWay(Shell.get(1))) {
                 Shell.set(1, null);
             } else {
                 LeftOfShell = Shell.get(1);
             }
             if (WallInTheWay(Shell.get(2)) || RockInTheWay(Shell.get(2))) {
                 Shell.set(2, null);
             } else {
                 RightOfShell = Shell.get(2);
             }
             if (WallInTheWay(Shell.get(3)) || RockInTheWay(Shell.get(3))) {
                 Shell.set(3, null);
             } else {
                 BottomOfShell = Shell.get(3);
             }
          }
     }
 
     public boolean WallInTheWay(Point position) {
         return (Board.GetState(position) instanceof Wall);
     }
 
     public boolean RockInTheWay(Point position) {
         return (Board.GetState(position) instanceof Static_Rock);
     }
 
     public void RockInFront(){
         BoardState IsThere = Board.GetState(IsThereARockHere);
         if (! (IsThere instanceof Static_Rock)){
             ThrewRock = false;
         }
     }
 
     public Point ThrowRock(){
         BoardState state = Board.GetState(newThrowRockPosition);
 
         if (!ThrewRock) {
             return new Point(1,2);
         }
         ThrowCounter = ThrowCounter + 1;
         if ((state instanceof Static_Rock) && (ThrowCounter < 2)){
             Board.BackToEmpty(newThrowRockPosition);
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
         }
         return ThrowRock;
     }
 
     public void ChopWall(){
         if (FaceUp){
             ChopWallSection1 = new Point(PlayerPosition.x, PlayerPosition.y - 1);
             ChopWallSection2 = new Point(PlayerPosition.x, PlayerPosition.y - 2);
             ChopWallSection3 = new Point(PlayerPosition.x, PlayerPosition.y - 3);
         } else if (FaceLeft){
             ChopWallSection1 = new Point(PlayerPosition.x - 1, PlayerPosition.y);
             ChopWallSection2 = new Point(PlayerPosition.x - 2, PlayerPosition.y);
             ChopWallSection3 = new Point(PlayerPosition.x - 3, PlayerPosition.y);
         } else if (FaceRight){
             ChopWallSection1 = new Point(PlayerPosition.x + 1, PlayerPosition.y);
             ChopWallSection2 = new Point(PlayerPosition.x + 2, PlayerPosition.y);
             ChopWallSection3 = new Point(PlayerPosition.x + 3, PlayerPosition.y);
         } else {
             ChopWallSection1 = new Point(PlayerPosition.x, PlayerPosition.y + 1);
             ChopWallSection2 = new Point(PlayerPosition.x, PlayerPosition.y + 2);
             ChopWallSection3 = new Point(PlayerPosition.x, PlayerPosition.y + 3);
         }
         Board.RockAsBoardState(ChopWallSection1);
         Board.RockAsBoardState(ChopWallSection2);
         Board.RockAsBoardState(ChopWallSection3);
 
         //later make so it hurts enemies when hits, but not forever: null the points in update
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
 
 
 
 
