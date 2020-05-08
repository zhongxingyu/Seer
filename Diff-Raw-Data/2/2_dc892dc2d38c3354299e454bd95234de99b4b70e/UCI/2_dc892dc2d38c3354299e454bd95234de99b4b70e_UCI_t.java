 package sf.pnr.io;
 
 import sf.pnr.base.BestMoveListener;
 import sf.pnr.base.Board;
 import sf.pnr.base.Configuration;
 import sf.pnr.base.StringUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  */
 public class UCI {
 
     private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
     private static final int CANCEL_THRESHOLD = 50;
     private static final Pattern POSITION_PATTERN = Pattern.compile(
         "(([1-8/pnbrqkPNBRQK]+ [wb] [KQkq-]+ [-a-h1-8]+ [0-9]+ [0-9]+)|startpos)( moves( [a-h][1-8][a-h][1-8][nbrq]?)+)");
     private static boolean verbose = false;
 
     private enum State {START, UCI_RECEIVED, SEARCHING, SEARCHING_PONDER}
 
     private final BufferedReader in;
     private final PrintStream out;
     private DebugBestMoveListener debugListener;
     private final PawnsNRoses chess;
     private Future<String> future;
     private volatile State state = State.START;
 
     public UCI(final BufferedReader in, final PrintStream out, final File book) {
         this.in = in;
         this.out = out;
         chess = new PawnsNRoses(book);
         debugListener = new DebugBestMoveListener(out);
         debugListener.setDebug(true);
         chess.setBestMoveListener(debugListener);
     }
 
     public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
         final PrintStream outputStream;
         if (args.length > 0) {
             outputStream = new PrintStream(new TeeOutputStream(System.out, new FileOutputStream(args[0])));
         } else {
             outputStream = System.out;
         }
 
         final String bookPath = System.getProperty("polyglot.book");
         final File book = bookPath != null? new File(bookPath): null;
 
         final String transpTableSizeStr = System.getProperty("transposition.table.size", "24");
         final int transpTableSize = Integer.parseInt(transpTableSizeStr);
         final String evalHashTableSizeStr = System.getProperty("eval.hashtable.size", "8");
         final int evalHashTableSize = Integer.parseInt(evalHashTableSizeStr);
         final Configuration config = Configuration.getInstance();
         config.setTranspositionTableSizeInMB(transpTableSize);
        config.setEvalHashTableSizeInMB(evalHashTableSize);
 
         final UCI protocol = new UCI(new BufferedReader(new InputStreamReader(System.in)), outputStream, book);
         protocol.run();
     }
 
     public void run() throws IOException, ExecutionException, InterruptedException {
         while (true) {
             final String line = in.readLine().trim();
             if ("uci".equals(line)) {
                 assert state == State.START;
                 state = State.UCI_RECEIVED;
                 out.println("id name Pawns N' Roses");
                 out.println("id author George Koch");
                 out.println("uciok");
                 out.printf("info string useBook: %b, book: %s\r\n", chess.useBook(), chess.getBook().getAbsolutePath());
             } else if (line.startsWith("debug ")) {
                 debugListener.setDebug("on".equals(line.substring(6).trim()));
             } else if ("isready".equals(line)) {
                 ensureReady();
                 print("readyok");
             } else if (line.startsWith("setoption ")) {
                 // no options yet
             } else if ("ucinewgame".equals(line)) {
                 ensureReady();
                 chess.restart();
             } else if (line.startsWith("position ")) {
                 ensureReady();
                 final String position = line.substring(9).trim();
                 final Matcher matcher = POSITION_PATTERN.matcher(position);
                 if (matcher.matches()) {
                     final String fen = matcher.group(1);
                     if (fen.equals("startpos")) {
                         chess.restartBoard();
                     } else {
                         chess.setBoard(fen);
                     }
                     if (matcher.groupCount() > 2) {
                         final String movesStr = matcher.group(3).substring(6).trim();
                         final String moves[] = movesStr.split(" ");
                         final Board board = chess.getBoard();
                         for (String move: moves) {
                             board.move(StringUtils.fromLong(board, move.trim()));
                         }
                     }
                 }
             } else if (line.startsWith("go ")) {
                 ensureReady();
                 processGo(line.substring(3).trim());
             } else if ("stop".equals(line)) {
                 ensureReady();
             } else if ("ponderhit".equals(line)) {
                 if (state == State.SEARCHING_PONDER) {
                     state = State.SEARCHING;
                 }
             } else if ("quit".equals(line)) {
                 break;
             }
         }
     }
 
     private void processGo(final String paramsStr) throws ExecutionException, InterruptedException {
         final String[] params = paramsStr.split(" ");
         state = State.SEARCHING;
         int timeWhite = -1;
         int timeBlack = -1;
         int incrementWhite = -1;
         int incrementBlack = -1;
         int movesToGo = -1;
         int depth = 0;
         int time = -1;
         for (int i = 0; i < params.length; i++) {
             final String param = params[i];
             if (param.equals("searchmoves")) {
                 // not supported yet
             } else if (param.equals("ponder")) {
                 state = State.SEARCHING_PONDER;
                 // ponder move not supported
             } else if (param.equals("wtime")) {
                 timeWhite = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("btime")) {
                 timeBlack = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("winc")) {
                 incrementWhite = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("binc")) {
                 incrementBlack = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("movestogo")) {
                 movesToGo = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("depth")) {
                 depth = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("nodes")) {
                 // not supported
             } else if (param.equals("mate")) {
                 // not supported
             } else if (param.equals("movetime")) {
                 time = Integer.parseInt(params[i + 1]);
                 i++;
             } else if (param.equals("infinite")) {
                 time = Integer.MAX_VALUE;
             }
         }
         ensureReady();
         int searchTime = 0;
         boolean whiteToMove = chess.isWhiteToMove();
         if (time >= 0) {
             searchTime = time;
         } else if (whiteToMove && timeWhite >= 0) {
             final TimeControl timeControl = getTimeControl(timeWhite, incrementWhite, movesToGo);
             searchTime = timeControl.getNextMoveTime();
         } else if (!whiteToMove && timeBlack >= 0) {
             final TimeControl timeControl = getTimeControl(timeBlack, incrementBlack, movesToGo);
             searchTime = timeControl.getNextMoveTime();
         }
         chess.setDepth(depth);
         chess.setTime(Math.max(searchTime - CANCEL_THRESHOLD, 10));
         future = THREAD_POOL.submit(new Callable<String>() {
             @Override
             public String call() throws Exception {
                 try {
                     if (state == State.SEARCHING_PONDER) {
                         out.println("bestmove ponder");
                         return "ponder";
                     } else {
                         final int move = chess.move();
                         final String moveStr = StringUtils.toLong(move);
                         out.println("bestmove " + moveStr);
                         return moveStr;
                     }
                 } catch (Exception e) {
                     out.println("info string " + e.getMessage());
                     e.printStackTrace(out);
                     throw e;
                 } catch (Error e) {
                     out.println("info string " + e.getMessage());
                     e.printStackTrace(out);
                     throw e;
                 }
             }
         });
     }
 
     private TimeControl getTimeControl(final int timeLeft, final int increment, final int movesToGo) {
         if (increment > 0) {
             if (movesToGo > 0) {
                 return new IncrementalTimeControl(timeLeft, increment, movesToGo);
             } else {
                 return new IncrementalTimeControl(timeLeft, increment);
             }
         } else {
             if (movesToGo > 0) {
                 return new ConventionalTimeControl(movesToGo, timeLeft);
             } else {
                 return new ConventionalTimeControl(timeLeft);
             }
         }
     }
 
     private void print(final String message) {
         out.println(message);
     }
 
     private void ensureReady() throws InterruptedException, ExecutionException {
         if (future != null) {
             if (verbose) {
                 out.print("info string cancelling game");
             }
             chess.cancel();
             future.get();
             future = null;
         }
     }
 
     private static class DebugBestMoveListener implements BestMoveListener {
         private final PrintStream out;
         private boolean debug = false;
 
         public DebugBestMoveListener(final PrintStream out) {
             this.out = out;
         }
 
         @Override
         public void bestMoveChanged(final int depth, final int bestMove, final int value, final long time,
                                     final int[] bestLine, final long nodes) {
             if (debug) {
                 final String message = String.format("info depth %d currmove %s score cp %d time %d pv %s nodes %d",
                     depth, StringUtils.toLong(bestMove), value, time, StringUtils.toLong(bestLine, " "), nodes);
                 out.println(message);
             }
         }
 
         public void setDebug(final boolean debug) {
             this.debug = debug;
         }
     }
 }
