 package gatherer.listener;
 
 import gatherer.TelnetOutputListener;
 import gatherer.WeiqiStorage;
 import play.Logger;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static gatherer.IgsConstants.*;
 
 // 15 Game 43 I: ryoken (14 359 4) vs rokujidoo (15 598 24)
 // 15 Game 530 I: k4152 (16 533 7) vs kindol (2 531 19)
 // 15   0(B): Handicap 2
 // 15 269(W): J9 K9
 // 15 301(W): Pass
 // 2
 // 1 8
 public class IgsMoves implements TelnetOutputListener {
 
     public static final String HANDICAP_PREFIX = "HC";
 
     private String server;
 
     private boolean retrieveMoves;
 
     private GameStatus status;
 
     private static final Pattern GAME_STATUS = Pattern.compile("15 Game +" //
             + "([0-9]+) " // game nr
             + "I: " // ONLY IGS GO GAMES, CHESS IS BORING
             // white player: name, prisoners, seconds, byo-yomi period
             + "([^ ]+) " + "\\(([0-9]+) +([0-9]+) +([0-9-]+)\\)" + " vs "
             // black player: ...
             + "([^ ]+) " + "\\(([0-9]+) +([0-9]+) +([0-9-]+)\\)" + ".*");
 
     private static final Pattern MOVE_PATTERN = Pattern.compile("15 +" //
             + "([0-9]+)\\((W|B)\\): " // move nr and player
             + "([A-HJ-T0-9 ]+|Pass|Handicap [0-9])"); // coordinates
 
     private final WeiqiStorage storage;
 
     public IgsMoves(String server, WeiqiStorage storage) {
         this.server = server;
         this.storage = storage;
     }
 
     public boolean isRetrieving() {
         return retrieveMoves;
     }
 
     @Override
     public boolean notify(String line) {
 
         if (retrieveMoves) {
             if (line.equals(OBSERVED)) { // ignore flag output for observed move
                Logger.trace("end of observed");
                 return true;
             } else if (line.equals(OK) || line.equals(MOVE_LIST_OK)) { // all moves read
                 Logger.trace("move list of %s retrieved", status.getId());
                 retrieveMoves = false;
                 return false;
             }
         }
 
         // Game 13 I: SHIGE08 (2 546 17) vs YMT123 (3 462 1)
         Matcher gsm = GAME_STATUS.matcher(line);
         if (gsm.matches()) {
             status = mapGameStatus(gsm);
             Logger.trace("game status: " + line);
             retrieveMoves = true;
             return true;
         }
 
         // 15 0(B): Handicap 2
         // 15 269(W): J9 K9
         // 15 301(W): Pass
         Matcher mpm = MOVE_PATTERN.matcher(line);
         if (mpm.matches()) {
 
             int number = Integer.parseInt(mpm.group(1));
             String player = mpm.group(2);
 
             List<String> list = Arrays.asList(mpm.group(3).split(" "));
             LinkedList<String> stones = new LinkedList<String>(list);
 
             String coordinate = stones.remove();
             if (coordinate.equals("Handicap")) {
                 coordinate = HANDICAP_PREFIX + stones.remove();
             }
             String[] prisoners = stones.toArray(new String[stones.size()]);
 
             GamePlayerStatus gps;
            if (player == "W") {
                 gps = status.getWhite();
             } else {
                 gps = status.getBlack();
             }
 
             String game = status.getId();
             int seconds = gps.getSeconds();
             int byo = gps.getByo();
 
             storage.addMove(server, game, number, player, coordinate, seconds, byo, prisoners);
            Logger.trace("game %s move %s", game, line);
             return true;
         }
 
         return false;
     }
 
     private GameStatus mapGameStatus(Matcher gsm) {
         String id = gsm.group(1);
 
         GamePlayerStatus white = new GamePlayerStatus();
         white.setPrisoners(Integer.parseInt(gsm.group(3)));
         white.setSeconds(Integer.parseInt(gsm.group(4)));
         white.setByo(Integer.parseInt(gsm.group(5)));
 
         GamePlayerStatus black = new GamePlayerStatus();
         black.setPrisoners(Integer.parseInt(gsm.group(7)));
         black.setSeconds(Integer.parseInt(gsm.group(8)));
         black.setByo(Integer.parseInt(gsm.group(9)));
 
         return new GameStatus(id, white, black);
     }
 }
