 package sf.pnr.base;
 
 import java.io.IOException;
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.TreeSet;
 
 public class GameManager {
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
     private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
     private static final DateFormat PERIOD_FORMAT = new SimpleDateFormat("mm:ss.SSS");
 
     static {
         PERIOD_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     private final String event;
     private final int initialTimes;
     private final int increments;
     private final int rounds;
     private UciRunner kibitzer;
 
     public GameManager(final String event, final int initialTime, final int increment, final int rounds) {
         this.event = event;
         this.initialTimes = initialTime;
         this.increments = increment;
         this.rounds = rounds;
     }
 
     public void setKibitzer(final UciRunner kibitzer) {
         this.kibitzer = kibitzer;
     }
 
     public TournamentResult play(final UciRunner... players) {
 
         final TournamentResult tournamentResult = new TournamentResult();
         int index = 1;
         for (int i = 0; i < rounds; i++) {
             for (int diff = 1; diff < players.length; diff++) {
                 for (int first = 0; first < players.length - diff; first++) {
                     final int second = first + diff;
                     play(i, index++, tournamentResult, players[first], players[second]);
                 }
             }
             for (int diff = 1; diff < players.length; diff++) {
                 for (int first = 0; first < players.length - diff; first++) {
                     final int second = first + diff;
                     play(i, index++, tournamentResult, players[second], players[first]);
                 }
             }
         }
         return tournamentResult;
     }
 
     private void play(final int round, final int index, final TournamentResult tournamentResult,
                       final UciRunner white, final UciRunner black) {
         System.out.printf("[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL]\t%2$s - %3$s\r\n",
             System.currentTimeMillis(), white.getName(), black.getName());
         System.out.printf("%4s\t%6s\t%6s\t%6s\t%7s\t%7s\t%7s\t%7s\t%9s\t%6s\t%3s\t%5s\t%8s\t%6s\t%7s\t%5s\t%6s\t%5s\r\n",
             "mc", "white", "black", "mt[ms]", "rtw[ms]", "rtb[ms]", "nodes", "a.nodes", "nps", "a.ply", "ply", "cp",
                 "kibitz", "k.mt", "k.nodes", "k.cp", "a.diff", "diff");
         final UciRunner[] players = new UciRunner[] {white, black};
         final int[] times = new int[]{initialTimes, initialTimes};
         GameResult result;
         final List<Integer> moves = new ArrayList<Integer>(100);
         final long startTime = System.currentTimeMillis();
         final Board board = new Board();
         Exception ex = null;
         board.restart();
         try {
             white.restart();
             white.uciNewGame();
             black.restart();
             black.uciNewGame();
             final UciRunner[] kibitzers = new UciRunner[2];
             if (kibitzer != null) {
                 kibitzers[0] = kibitzer.duplicate();
                 kibitzers[0].restart();
                 kibitzers[0].uciNewGame();
                 kibitzers[1] = kibitzer.duplicate();
                 kibitzers[1].restart();
                 kibitzers[1].uciNewGame();
             }
             int currentPlayer = 0;
             final int[] depths = new int[2];
             final long[] nodes = new long[2];
             final int[] scoreDiffs = new int[2];
             while (true) {
                 final UciRunner player = players[currentPlayer];
                 player.position(moves);
                 final int timeWhite = times[0];
                 final int timeBlack = times[1];
                 final int timeCurrent = times[currentPlayer];
                 player.go(timeWhite, timeBlack, increments, increments, timeCurrent + increments);
                 final long moveTime = player.getMoveTime();
                 times[currentPlayer] += increments - moveTime;
                 if (times[currentPlayer] < 0) {
                     result = GameResult.TIME_OUT;
                     break;
                 }
                 final String bestMove = player.getBestMove();
                 final int move = StringUtils.fromLong(board, bestMove);
                 if (move == 0) {
                     throw new IllegalMoveException(String.format("Zero move at FEN '%s'. Best line: %s",
                         StringUtils.toFen(board), player.getBestMoveLine()));
                 }
                 final Set<String> problems = Utils.checkMove(board, move);
                 if (!problems.isEmpty()) {
                     final String message = String.format("Illegal move ('%s') from '%s' at FEN %s",
                         StringUtils.toSimple(move), player.getName(), StringUtils.toFen(board));
                     System.out.println(message);
                     for (String problem: problems) {
                         System.out.println(problem);
                     }
                     throw new IllegalMoveException(message);
                 }
                 final String whiteMove;
                 final String blackMove;
                 if (currentPlayer == 0) {
                     whiteMove = StringUtils.toShort(board, move);
                     blackMove = "";
                 } else {
                     whiteMove = "";
                     blackMove = StringUtils.toShort(board, move);
                 }
                 depths[currentPlayer] += player.getDepth();
                 nodes[currentPlayer] += player.getNodeCount();
                 final String kMove;
                 final String kNodes;
                 final String kTime;
                 final String kScore;
                 final String kScoreDiff;
                 UciRunner kibitzer = kibitzers[currentPlayer];
                 if (kibitzer != null) {
                     kibitzer.position(moves);
                     kibitzer.go(Math.min(Math.max(1, player.getDepth()), 15), 0);
                     final String kBestMove = kibitzer.getBestMove();
                     kMove = StringUtils.toShort(board, StringUtils.fromLong(board, kBestMove)) +
                         (bestMove.equals(kBestMove)? " =": " !");
                     kTime = Long.toString(kibitzer.getMoveTime());
                     kNodes = Long.toString(kibitzer.getNodeCount());
                     kScore = Integer.toString(kibitzer.getScore());
                     final int scoreDiff = kibitzer.getScore() - player.getScore();
                     scoreDiffs[currentPlayer] += Math.abs(scoreDiff);
                     kScoreDiff = Integer.toString(scoreDiff);
                 } else {
                     kMove = "-";
                     kTime = "-";
                     kNodes = "-";
                     kScore = "-";
                     kScoreDiff = "-";
                 }
                 final int fullMoveCount = board.getFullMoveCount();
                 System.out.printf("%3d.\t%6s\t%6s\t%6d\t%7d\t%7d\t%7d\t%7d\t%9.1f\t%6.2f\t%3d\t%5d\t%8s\t%6s\t%7s\t%5s\t%6d\t%5s\r\n",
                     fullMoveCount, whiteMove, blackMove, moveTime, times[0], times[1], player.getNodeCount(),
                    nodes[currentPlayer] / fullMoveCount, ((double) player.getNodeCount() * 1000) / times[currentPlayer],
                     ((double) depths[currentPlayer]) / fullMoveCount, player.getDepth(), player.getScore(),
                     kMove, kTime, kNodes, kScore, scoreDiffs[currentPlayer] / fullMoveCount, kScoreDiff);
                 if (currentPlayer == 1 && fullMoveCount % 5 == 0) {
                     System.out.println();
                 }
                 board.move(move);
                 moves.add(move);
                 if (board.isMate()) {
                     result = GameResult.MATE;
                     break;
                 }
                 if (board.getRepetitionCount() >= 3) {
                     result = GameResult.THREEFOLD_REPETITION;
                     break;
                 }
                 if (Evaluation.drawByInsufficientMaterial(board)) {
                     result = GameResult.INSUFFICIENT_MATERIAL;
                     break;
                 }
                 currentPlayer = 1 - currentPlayer;
             }
         } catch (IOException e) {
             result = GameResult.ERROR;
             e.printStackTrace();
             System.out.println(StringUtils.toFen(board));
             ex=e;
         } catch (IllegalMoveException e) {
             result = GameResult.ILLEGAL_MOVE;
             System.out.println(e.getMessage());
             ex=e;
         }
 
         final int[] movesArr = new int[moves.size()];
         for (int j = 0; j < moves.size(); j++) {
             movesArr[j] = moves.get(j);
         }
 
         final GameDetails details =
             new GameDetails(event, round, white, black, index, startTime, result, movesArr, times, ex);
         tournamentResult.registerResult(white, black, details);
         System.out.printf("[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL] %s\r\n",
             System.currentTimeMillis(), tournamentResult.toString(white, black));
         System.out.println(details.toPgn());
         System.out.printf("[%1$tY%1tm%1$td %1$tH:%1$tM:%1$tS.%1$tL]\r\n%2$s\r\n",
             System.currentTimeMillis(), tournamentResult.toString());
     }
 
     private static enum GameResult {
         TIME_OUT(0, 'T', 't'), MATE(1, '1', '0'), THREEFOLD_REPETITION(0.5, 'R', 'r'),
         INSUFFICIENT_MATERIAL(0.5, 'I', 'i'), ERROR(1, 'e', 'E'), ILLEGAL_MOVE(1, 'm', 'M');
 
         private final double whiteScore;
         private final double blackScore;
         private final char whiteCode;
         private final char blackCode;
 
         private GameResult(final double whiteScore, final char whiteCode, final char blackCode) {
             this(whiteScore, 1 - whiteScore, whiteCode, blackCode);
         }
 
         private GameResult(final double whiteScore, final double blackScore, final char whiteCode, final char blackCode) {
             this.whiteScore = whiteScore;
             this.blackScore = blackScore;
             this.whiteCode = whiteCode;
             this.blackCode = blackCode;
         }
 
         public char getCode(final boolean white) {
             return white? whiteCode: blackCode;
         }
 
         public double getScore(final boolean white) {
             return white? whiteScore: blackScore;
         }
     }
 
     public static class GameDetails {
         private static String HOST_NAME;
         static {
             try {
                 HOST_NAME = Inet4Address.getLocalHost().getHostName();
             } catch (UnknownHostException e) {
                 HOST_NAME = "??";
             }
 
         }
         private final String event;
         private final UciRunner white;
         private final UciRunner black;
         private final int round;
         private final int gameIndex;
         private final long startTime;
         private final GameResult result;
         private final int[] moves;
         private final int[] remainedTimes;
         private final Exception ex;
 
         public GameDetails(final String event, final int round, final UciRunner white, final UciRunner black,
                            final int gameIndex, final long startTime, final GameResult result, final int[] moves,
                            final int[] remainedTimes, final Exception ex) {
             this.event = event;
             this.white = white;
             this.black = black;
             this.round = round;
             this.gameIndex = gameIndex;
             this.startTime = startTime;
             this.result = result;
             this.moves = moves;
             this.remainedTimes = remainedTimes;
             this.ex = ex;
         }
 
         public char getCode(final UciRunner player) {
             return result.getCode((player == white) ^ (moves.length % 2 == 0));
         }
 
         public double getScore(final UciRunner player) {
             return result.getScore((player == white) ^ (moves.length % 2 == 0));
         }
 
         public String getScoreStr(final UciRunner player) {
             final double score = result.getScore((player == white) ^ (moves.length % 2 == 0));
             return score == 0.5? "1/2": String.format("%.0f", score);
         }
 
         public String getTerminationStr() {
             final String termination;
             if (result == GameResult.ERROR) {
                 termination = "death";
             } else if (result == GameResult.ILLEGAL_MOVE) {
                 termination = "rules infraction";
             } else if (result == GameResult.TIME_OUT) {
                 termination = "time forfeit";
             } else {
                 termination = "normal";
             }
             return termination;
         }
 
         public String toPgn() {
             final StringBuilder builder = new StringBuilder();
             final Date startDateTime = new Date(startTime);
             builder.append(StringUtils.createPgnEntry("Event", event));
             builder.append(StringUtils.createPgnEntry("Site", getHostName()));
             builder.append(StringUtils.createPgnEntry("Date", DATE_FORMAT.format(startDateTime)));
             builder.append(StringUtils.createPgnEntry("Round", Integer.toString(round)));
             builder.append(StringUtils.createPgnEntry("White", white.getName()));
             builder.append(StringUtils.createPgnEntry("Black", black.getName()));
             builder.append(StringUtils.createPgnEntry("Game", Integer.toString(gameIndex)));
             builder.append(StringUtils.createPgnEntry("Result", getScoreStr(white) + "-" + getScoreStr(black)));
             builder.append(StringUtils.createPgnEntry("Time", TIME_FORMAT.format(startDateTime)));
             builder.append(StringUtils.createPgnEntry("PlyCount", Integer.toString(moves.length)));
             builder.append(StringUtils.createPgnEntry("Termination", getTerminationStr()));
             builder.append(StringUtils.createPgnEntry("RemainingTimeWhite", PERIOD_FORMAT.format(new Date(remainedTimes[0]))));
             builder.append(StringUtils.createPgnEntry("RemainingTimeBlack", PERIOD_FORMAT.format(new Date(remainedTimes[1]))));
             if (ex != null) {
                 builder.append(StringUtils.createPgnEntry("Exception", ex.getMessage()));
             }
             final Board board = new Board();
             board.restart();
             for (int i = 0; i < moves.length; i++) {
                 final int move = moves[i];
                 if (i % 2 == 0) {
                     if (i % 16 == 0) {
                         builder.append("\r\n");
                     }
                     builder.append((i / 2) + 1).append(". ");
                 }
                 builder.append(StringUtils.toShort(board, move)).append(" ");
                 board.move(move);
             }
             return builder.toString();
         }
 
         private String getHostName() {
             return HOST_NAME;
         }
     }
 
     public static class GameSeries {
         private final List<GameDetails> series = new ArrayList<GameDetails>();
 
         public void add(final GameDetails details) {
             series.add(details);
         }
 
         public String toStringShort(final UciRunner player) {
             final StringBuilder builder = new StringBuilder();
             for (GameDetails details: series) {
                 builder.append(details.getCode(player));
             }
             return builder.toString();
         }
 
         public double getScore(final UciRunner player) {
             double score = 0.0;
             for (GameDetails details: series) {
                 score += details.getScore(player);
             }
             return score;
         }
 
         public int getNumberOfGames() {
             return series.size();
         }
     }
 
     public static class TournamentResult {
         private final Map<UciRunner, Map<UciRunner, GameSeries>> games =
             new LinkedHashMap<UciRunner, Map<UciRunner, GameSeries>>();
 
 
         public void registerResult(final UciRunner white, final UciRunner black, final GameDetails gameDetails) {
             addResult(white, black, gameDetails);
             addResult(black, white, gameDetails);
         }
 
         private void addResult(final UciRunner player, final UciRunner opponent, final GameDetails gameDetails) {
             Map<UciRunner, GameSeries> matches = games.get(player);
             if (matches == null) {
                 matches = new HashMap<UciRunner, GameSeries>();
                 games.put(player, matches);
             }
             GameSeries series = matches.get(opponent);
             if (series == null) {
                 series = new GameSeries();
                 matches.put(opponent, series);
             }
             series.add(gameDetails);
         }
 
         public String toString(final UciRunner player, final UciRunner opponent) {
             final StringBuilder builder = new StringBuilder();
             builder.append(player.getName());
             builder.append(" - ");
             builder.append(opponent.getName());
             builder.append(": ");
             final Map<UciRunner, GameSeries> matches = games.get(player);
             if (matches == null) {
                 return builder.toString();
             }
             final GameSeries series = matches.get(opponent);
             if (series == null) {
                 return builder.toString();
             }
             final double playersScore = series.getScore(player);
             final double opponentsScore = series.getScore(opponent);
             builder.append(String.format("%.1f:%.1f (%s)", playersScore, opponentsScore, series.toStringShort(player)));
             return builder.toString();
         }
 
         public String toString() {
             int maxNameLen = 0;
             for (UciRunner player: games.keySet()) {
                 final String name = player.getName();
                 if (name.length() > maxNameLen) {
                     maxNameLen = name.length();
                 }
             }
             int maxResultLen = 3;
             for (Map<UciRunner, GameSeries> results: games.values()) {
                 for (Map.Entry<UciRunner, GameSeries> entry: results.entrySet()) {
                     final UciRunner opponent = entry.getKey();
                     final GameSeries series = entry.getValue();
                     final String result = series.toStringShort(opponent);
                     if (result.length() > maxResultLen) {
                         maxResultLen = result.length();
                     }
                 }
             }
 
             final Map<UciRunner, Score> scores = new HashMap<UciRunner, Score>();
             for (Map.Entry<UciRunner, Map<UciRunner, GameSeries>> entry: games.entrySet()) {
                 final UciRunner player = entry.getKey();
                 double score = 0.0;
                 int numberOfGames = 0;
                 for (GameSeries series: entry.getValue().values()) {
                     score += series.getScore(player);
                     numberOfGames += series.getNumberOfGames();
                 }
                 scores.put(player, new Score(player, score, numberOfGames));
             }
             for (Map.Entry<UciRunner, Map<UciRunner, GameSeries>> entry: games.entrySet()) {
                 final UciRunner player = entry.getKey();
                 double sbScore = 0.0;
                 for (Map.Entry<UciRunner, GameSeries> entry2: entry.getValue().entrySet()) {
                     final UciRunner opponent = entry2.getKey();
                     final GameSeries series = entry2.getValue();
                     sbScore += series.getScore(player) * scores.get(opponent).getScore();
                 }
                 scores.get(player).setSonnebornBerger(sbScore);
             }
 
             final Set<Score> ranking = new TreeSet<Score>(new Comparator<Score>() {
                 @Override
                 public int compare(final Score s1, final Score s2) {
                     final double score1 = s1.getScore() / Math.max(s1.getNumberOfGames(), 1);
                     final double score2 = s2.getScore() / Math.max(s2.getNumberOfGames(), 1);
                     int result = Double.compare(score2, score1);
                     if (result == 0) {
                         result = Double.compare(s2.getSonnebornBerger(), s1.getSonnebornBerger());
                     }
                     if (result == 0) {
                         result = Double.compare(s2.getScore(), s1.getScore());
                     }
                     if (result == 0) {
                         result = s1.getPlayer().getName().compareTo(s2.getPlayer().getName());
                     }
                     return result;
                 }
             });
             ranking.addAll(scores.values());
 
             final StringBuilder builder = new StringBuilder();
             appendPadded(builder, "", maxNameLen, true);
             for (Score score: ranking) {
                 builder.append("  ");
                 final String name = score.getPlayer().getName();
                 if (name.length() > maxResultLen) {
                     builder.append(name.substring(0, maxResultLen));
                 } else {
                     appendPadded(builder, name, maxResultLen, false);
                 }
             }
             builder.append("\r\n");
             for (Score score: ranking) {
                 final UciRunner player = score.getPlayer();
                 final String name = player.getName();
                 appendPadded(builder, name, maxNameLen, true);
                 final Map<UciRunner, GameSeries> map = games.get(player);
                 for (Score opponentScore: ranking) {
                     builder.append("  ");
                     final UciRunner opponent = opponentScore.getPlayer();
                     if (opponent == player) {
                         appendPadded(builder, "...", maxResultLen, false);
                     } else {
                         final GameSeries series = map.get(opponent);
                         final String seriesResult;
                         if (series != null) {
                             seriesResult = series.toStringShort(player);
                         } else {
                             seriesResult = "";
                         }
                         appendPadded(builder, seriesResult, maxResultLen, false);
                     }
                 }
                 builder.append(String.format("\t%.1f\t(/%d=%.2f)\t%.2f\r\n", score.getScore(), score.getNumberOfGames(),
                     score.getScore() / Math.max(score.getNumberOfGames(), 1), score.getSonnebornBerger()));
             }
             return builder.toString();
         }
 
         private void appendPadded(final StringBuilder builder, final String str, final int maxLen, final boolean leftPadded) {
             if (leftPadded) {
                 for (int i = 0; i < maxLen - str.length(); i++) {
                     builder.append(' ');
                 }
             }
             builder.append(str);
             if (!leftPadded) {
                 for (int i = 0; i < maxLen - str.length(); i++) {
                     builder.append(' ');
                 }
             }
         }
     }
 
     private static class IllegalMoveException extends Exception {
         public IllegalMoveException(final String message) {
             super(message);
         }
     }
 
     private static class Score {
         private final UciRunner player;
         private final double score;
         private final int numberOfGames;
         private double sonnebornBerger;
 
         private Score(final UciRunner player, final double score, final int numberOfGames) {
             this.player = player;
             this.score = score;
             this.numberOfGames = numberOfGames;
         }
 
         public UciRunner getPlayer() {
             return player;
         }
 
         public double getScore() {
             return score;
         }
 
         public int getNumberOfGames() {
             return numberOfGames;
         }
 
         public double getSonnebornBerger() {
             return sonnebornBerger;
         }
 
         public void setSonnebornBerger(final double sonnebornBerger) {
             this.sonnebornBerger = sonnebornBerger;
         }
     }
 }
