 package player.millitta.Evaluate;
 
 
 import player.millitta.Board;
 import player.millitta.Helper;
 import player.millitta.LookupTable;
 
 public class Evaluate extends Board {
 
     /* board state for only the player to evaluate */
     private long playersBoard_ = 0L;
     private long oppsBoard_ = 0L;
     private long boardWithoutMills = -1L;
 
     /*
          0-22 = Mens of White Player (0)
         24-47 = Mens of Black Player (1)
            48 = Player ID to evaluate
         49-51 = Game phase
     */
     public Evaluate(long board) {
         super(board);
 
         // Spieler muss getauscht werden, da dies ja das Board fuer die naechste Runde ist, aber
         // die Auswertung noch fuer diesen stattfinden soll.
         //this.board = switchPlayer(this.board);
 
 
         if ((this.board & (1L << BIT_PLAYER)) != 0) {
             oppsBoard_ = board;
             playersBoard_ = board & ~((long) (Math.pow(2, 24) - 1)); // Alles bis Bit 24 loeschen
             playersBoard_ |= board >> 24; // Player 1 Daten auf Player 0 Datenposition verschieben
         } else {
             playersBoard_ = board;
             oppsBoard_ = board & ~((long) (Math.pow(2, 24) - 1));
             oppsBoard_ |= board >> 24;
         }
     }
 
     public double getFitness() {
         double fitness = 0.f;
 
         if( isLost() ) {
             return -1L;
         }
 
 
         fitness += Weighting[WEIGHT_CLOSED_MILL] * getClosedMills();
         fitness += Weighting[WEIGHT_OPEN_MILL] * getOpenMills();
         fitness += Weighting[WEIGHT_DOUBLE_MILL] * getDoubleMills();
         fitness += Weighting[WEIGHT_MEN] * getMyMenVsOppMen();
         fitness += Weighting[WEIGHT_MOVABLE] * getMovable();
 
         System.out.println(board);
         Helper.printBoardState(board);
         Helper.printBoard(board);
         System.out.println("Fitness: " + fitness + " | OM: " + getOpenMills() + ", CM: " + getClosedMills() +
                 ", Mvs: " + getMyMenVsOppMen() + ", ZM: " + getDoubleMills() + ", MA: " + getMovable());
 
         return fitness;
     }
 
     /*
         Verloren hat, wer nur noch zwei Spielsteine hat oder sich nicht mehr bewegen kann.
      */
     private boolean isLost() {
         if( getMyMenOnBoard(board) + getMyRest(board) <= 2 || (getMovable() == 0 && getMyRest(board) <= 0) ) {
             return true;
         }
         return false;
     }
 
     /*
         Es existieren insgesamt 16 moegliche Muehlen.
         Fuer jede Muehle wird eine 24 Bit-Maske angelegt und die entsprechenden Bits gemaess des Spielfeldes gesetzt.
         Auf diese Masken kann dann eine logische UND-Verknuepfung mit den Bits der gesetzten Steine
         des entsprechenden Spielers angewandt werden.
 
         Sind bei einer Maske noch 3 Bits gesetzt, so hat der Spieler dort eine geschlossene Muehle.
         Durch Abzaehlen, bei wievielen der 16 Masken dies vorkommt,
         weiss man wieviele Muehlen der Spieler insgesamt hat.
      */
     private int getClosedMills() {
         int closedMills = 0;
         boardWithoutMills = playersBoard_;
 
         for (int mill : LookupTable.mills) {
             if (Long.bitCount(playersBoard_ & mill) == 3) {
                 closedMills++;
                 boardWithoutMills &= ~(mill);
             }
         }
 
         return closedMills;
     }
 
     /*
         Genau wie beim Erkennen der geschlossenen Muehlen werden hier die Steine des Spielers mit allen 16 Masken
         logisch mit UND-Verknuepft. Eine offene Muehle liegt genau dann vor,
         wenn 2 Bits in einer verknuepften Maske noch gesetzt sind.
 
         Solange der auszuwertende Spieler noch Steine zum Setzen hat oder springen kann,
         reicht diese Bedingung schon aus. Ansonsten muss geprueft werden, ob ein angrenzedes Feld zu dem Feld,
         in dem der Stein zur fertigen Muehle fehlt, auch ein Stein des selben Spielers liegt.
         Dieser Stein darf dann natuerlich nicht selber schon Teil der gerade betrachteten offenen Muehle sein.
 
         TODO lockup table benutzen
      */
     private int getOpenMills() {
         int openMills = 0;
 
         for (int mill : LookupTable.mills) {
             if (Long.bitCount(playersBoard_ & mill) == 2) {
                 if ((playersBoard_ & (1L << BIT_PHASE)) != 0) { // Setz oder Flugphase
                     openMills++;
                 } else { // Angrenzend bewegbarer Stein, der nicht in einer Muehle ist
                     int holePos = (int)Math.round(Math.log((playersBoard_ & mill) ^ mill) / LOG2);
                     long boardWithoutOpenAndClosedMills = boardWithoutMills & ~(mill);
 
                     for( int neighbor : LookupTable.neighbors[holePos]) {
                        if( (boardWithoutOpenAndClosedMills & (1L << neighbor)) == 0L) {
                             openMills++;
                             break;
                         }
                     }
                 }
             }
         }
 
         return openMills;
     }
 
     /*
         Erweiterung des Algorithmus zum erkennen von offenen Muehlen.
         Hier muss nur der angrenzende Stein selbst schon in einer Muehle sein.
      */
     private int getDoubleMills() {
         int doubleMills = 0;
 
         for (int mill : LookupTable.mills) {
             // Offene Muehle
             if (Long.bitCount(playersBoard_ & mill) == 2) {
                 // Position des fehlenden Steines
                 int holePos = (int) Math.round(Math.log((playersBoard_ & mill) ^ mill) / LOG2);
 
                 // Fuer jeden Nachbarn davon
                 for (int neighbor : LookupTable.neighbors[holePos]) {
                     // Der Nachbar darf nicht mit in der selben (offenen) Muehle sein
                     if ((mill & (1L << neighbor)) == 0L) {
                         for (int mill2 : LookupTable.millAt[neighbor]) {
                             // Ist der Nachbar dann Teil einer Muehle, so haben wir hier eine Zwickmuehle :)
                             if (Long.bitCount(playersBoard_ & mill2) == 3) {
                                 doubleMills++;
                                 break;
                             }
                         }
                     }
                 }
             }
         }
 
 
         return doubleMills;
     }
 
     private double getMyMenVsOppMen() {
         if (getMyMenOnBoard() == 0 || getOppMenOnBoard() == 0) {
             return 0.f;
         }
         return (float) getMyMenOnBoard() / (float) getOppMenOnBoard();
     }
 
     /*
         Anzahl der noch beweglichen Steine.
      */
     private int getMovable() {
         int movable = 0;
         for (int i = 0; i < 24; i++) {
             // Fuer jeden der Spielfiguren
             if ((playersBoard_ & (1L << i)) != 0L) {
                 // Alle Nachbarschaftsfelder ueberpruefen
                 for (int neighbor : LookupTable.neighbors[i]) {
                     // Ob sie frei sind
                     if ((oppsBoard_ & (1L << neighbor)) == 0L && (playersBoard_ & (1L << neighbor)) == 0) {
                         movable++;
                         break;
                     }
                 }
             }
         }
 
         return movable;
     }
 
 
 }
