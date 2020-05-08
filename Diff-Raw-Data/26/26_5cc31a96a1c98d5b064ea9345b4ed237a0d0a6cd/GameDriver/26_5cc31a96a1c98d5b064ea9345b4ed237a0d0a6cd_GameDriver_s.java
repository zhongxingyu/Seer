 package com.linfords.detangle;
 
 import com.linfords.detangle.Space.State;
 
 /**
  *
  * @author Scott
  */
 public class GameDriver {
 
     public final static boolean TEST_RUN = true;
     public final static boolean VERBOSE = false;
 
     static class Record {
 
         int highScore = 0;
         long gamesCompleted = 0;
         EventStack active = new EventStack();
 
         void add(final Event.Type type, final Space.Coordinates pos, final int marker, final int rotation, final int score) {
             active.push(new Event(type, pos, marker, rotation, score));
         }
 
         String toStringDetail() {
             StringBuilder sb = new StringBuilder();
             for (Event m : active) {
                 sb.append(m.type).append(m.pos).append(" r(").append(m.rotation).append(") s(").append(m.score).append("); ");
             }
             return sb.toString();
         }
 
         String toStringSummary() {
             return gamesCompleted + "] " + rotationSequence() + " length(" + pathLength() + ")" + " score(" + score() + ")";
         }
 
         boolean inProgress() {
             return active.peek().type != Event.Type.End;
         }
 
         boolean isLastGame() {
             // Have to play the game out to know for sure.
             if (inProgress()) {
                 return false;
             }
 
             for (Event m : active) {
                 switch (m.type) {
                     case Play:
                     case Flow:
                         if (m.rotation < (Tile.SIDE_QTY - 1)) {
                             return false;
                         }
                         break;
 
                     case Start:
                     case End:
                         break;
                 }
             }
 
             return true;
         }
 
         void rewind(Board board) {
             if (TEST_RUN) {
                 validateRecord();
             }
 
             gamesCompleted++;
 
             if (score() > highScore) {
                 highScore = score();
             }
 
             rewind:
             while (active.size() > 1) {
                 switch (active.peek().type) {
                     case End:
                     case Flow:
                         active.pop();
                         break;
                     case Play:
                         Event played = active.pop();
                         final int r = played.rotation + 1;
 
                         if (r == Tile.SIDE_QTY) {
                             if (active.peek().type == Event.Type.Start) {
                                 break rewind;
                             }
                             board.putTileBack(played.pos);
                         } else {
                             board.undoPlay(active.peek().pos, active.peek().marker, played.pos, r);
                             break rewind;
                         }
                         break;
                     case Start:
                     default:
                         throw new IllegalStateException("Rewound down to " + active.peek().type + " size: " + active.size());
                 }
             }
         }
 
         int pathLength() {
             int length = active.size() - 1;
             if (active.peek().type == Event.Type.End) {
                 length--;
             }
             return length < 0 ? 0 : length;
         }
 
         @Override
         public String toString() {
             return active.toString();
         }
 
         private int score() {
             return active.peek().score;
         }
 
         private int size() {
             return active.size();
         }
 
         private String rotationSequence() {
             StringBuilder sb = new StringBuilder();
             for (Event m : active) {
                 switch (m.type) {
                     case Flow:
                         sb.append('-');
                         break;
                     case Play:
                         sb.append(m.rotation);
                         break;
                     case Start:
                         sb.append(">");
                         break;
                     case End:
                         sb.append("|");
                         break;
                 }
             }
             return sb.toString();
         }
 
         private boolean isHighScore() {
             return score() > highScore;
         }
 
         private void validateRecord() {
             switch ((int) gamesCompleted) {
                 case 549:
                     assert toStringSummary().equals("549] >000100013-0--------0010-101010-100-011301-------00---422--------5-----------| length(76) score(252)") : toStringSummary();
                     break;
                 case 144349:
                     assert toStringSummary().equals("144349] >000100013-0--------0010-101010-103-014450----------50----24-----------10---------------| length(87) score(378)") : toStringSummary();
                     break;
             }
         }
     }
 
     private void grind() {
         Board board = new Board();
         Record record = new Record();
         record.add(Event.Type.Start, board.current.pos, board.current.marker, 0, 0);
 
         if (VERBOSE) {
             System.out.println(board.current + " (start)");
         }
 
         while (!record.isLastGame()) {
             if (!record.inProgress()) {
                 record.rewind(board);
             }
 
             while (board.adjacent.state == State.Playable) {
                 final Space playable = board.adjacent;
                 int p = 1;
                 if (VERBOSE) {
                     System.out.println(playable + " (playing) +" + p);
                 }
 
                 board.play();
                 record.add(Event.Type.Play, playable.pos, playable.marker, playable.tile.getRotation(), record.score() + p);
                 while (board.adjacent.state == State.Played) {
                     final Space flowable = board.adjacent;
                     p++;
                     if (VERBOSE) {
                         System.out.println(flowable + " (flowing) +" + p);
                     }
                     board.flow();
                     record.add(Event.Type.Flow, flowable.pos, flowable.marker, flowable.tile.getRotation(), record.score() + p);
                 }
             }
 
             record.add(Event.Type.End, board.adjacent.pos, board.adjacent.marker, 0, record.score());
 
             if (VERBOSE) {
                 System.out.println(board.adjacent + " (end)");
                 System.out.println(record.toStringSummary());
                 System.out.println(record.toStringDetail());
                 System.out.println();
             } else if (record.isHighScore()) {
                 System.out.println(record.toStringSummary());
                 System.out.println(record.toStringDetail());
                 System.out.println();
            } else if ((record.gamesCompleted % 25_000_000) == 0) {
                 System.out.println(record.toStringSummary());
             }
         }
     }
 
     public static void main(String[] args) {
         new GameDriver().grind();
     }
 }
