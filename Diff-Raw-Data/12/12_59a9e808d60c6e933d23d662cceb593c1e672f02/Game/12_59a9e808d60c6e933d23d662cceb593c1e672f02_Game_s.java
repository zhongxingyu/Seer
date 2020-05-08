 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package connectFour;
 
 /**
  *
  * @author Ryan
  */
 public class Game {
     public static String PLAYING;
     public static Object QUIT;
     private static Object ONE_PLAYER;
     private static Object TWO_PLAYER;
     private static String EXIT;
     private static Object Playing;
     private String PLAYER_A_DEFAULT_MARKER;
     private String PLAYER_B_DEFAULT_MARKER;
    private Object Status;
     private Player PlayerA;
     private Player PlayerB;
     public Board board;
     public Object dimensions;
 
     public Board getBoard() {
         return board;
     }
 
     public void setBoard(Board board) {
         this.board = board;
     }
 
     public Object getDimensions() {
         return dimensions;
     }
 
     public void setDimensions(Object dimensions) {
         this.dimensions = dimensions;
     }
     
     public static String getPLAYING() {
         return PLAYING;
     }
 
     public static void setPLAYING(String PLAYING) {
         Game.PLAYING = PLAYING;
     }
 
     public static Object getQUIT() {
         return QUIT;
     }
 
     public static void setQUIT(Object QUIT) {
         Game.QUIT = QUIT;
     }
 
     public static Object getONE_PLAYER() {
         return ONE_PLAYER;
     }
 
     public static void setONE_PLAYER(Object ONE_PLAYER) {
         Game.ONE_PLAYER = ONE_PLAYER;
     }
 
     public static Object getTWO_PLAYER() {
         return TWO_PLAYER;
     }
 
     public static void setTWO_PLAYER(Object TWO_PLAYER) {
         Game.TWO_PLAYER = TWO_PLAYER;
     }
 
     public static String getEXIT() {
         return EXIT;
     }
 
     public static void setEXIT(String EXIT) {
         Game.EXIT = EXIT;
     }
 
     public static Object getPlaying() {
         return Playing;
     }
 
     public static void setPlaying(Object Playing) {
         Game.Playing = Playing;
     }
 
     public String getPLAYER_A_DEFAULT_MARKER() {
         return PLAYER_A_DEFAULT_MARKER;
     }
 
     public void setPLAYER_A_DEFAULT_MARKER(String PLAYER_A_DEFAULT_MARKER) {
         this.PLAYER_A_DEFAULT_MARKER = PLAYER_A_DEFAULT_MARKER;
     }
 
     public String getPLAYER_B_DEFAULT_MARKER() {
         return PLAYER_B_DEFAULT_MARKER;
     }
 
     public void setPLAYER_B_DEFAULT_MARKER(String PLAYER_B_DEFAULT_MARKER) {
         this.PLAYER_B_DEFAULT_MARKER = PLAYER_B_DEFAULT_MARKER;
     }
 
     public Object getStatus() {
         return Status;
     }
 
     public void setStatus(Object setStatus) {
         this.Status = setStatus;
     }
 
     public Player getPlayerA() {
         return PlayerA;
     }
 
     public void setPlayerA(Player PlayerA) {
         this.PlayerA = PlayerA;
     }
 
     public Player getPlayerB() {
         return PlayerB;
     }
 
     public void setPlayerB(Player PlayerB) {
         this.PlayerB = PlayerB;
     }
 
 }
