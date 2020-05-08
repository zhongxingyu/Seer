 package cz.janhrcek.chess.FEN;
 
 import cz.janhrcek.chess.model.api.GameState;
 import cz.janhrcek.chess.model.api.enums.Castling;
 import cz.janhrcek.chess.model.api.enums.Piece;
 import cz.janhrcek.chess.model.api.enums.Square;
 import cz.janhrcek.chess.model.api.Position;
 import cz.janhrcek.chess.model.impl.GameStateImpl;
 import cz.janhrcek.chess.model.impl.PositionImpl;
 import static java.lang.String.format;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author jhrcek
  */
 public class FenParser {
 
     public FenParser() {
         resetParserState();
     }
 
     /**
      * Converts given Game state to equivalent FEN string.
      *
      * @param state the game state to convert
      * @return the FEN string representing information stored in the game state
      */
     public String gameStateToFen(GameState state) {
         LOG.info("Converting game state to FEN");
         StringBuilder sb = new StringBuilder(50);
         Square ep = state.getEnPassantTarget();
         sb.append(positionToFen(state.getPosition()))
                 .append(state.isWhiteToMove() ? " w " : " b ")
                 .append(Castling.toFenCastlingSubstring(state.getCastlings()))
                 .append(" ")
                 .append(ep == null ? "-" : ep.toString().toLowerCase())
                 .append(" ")
                 .append(state.getHalfmoveClock())
                 .append(" ")
                 .append(state.getFullmoveNumber());
         return sb.toString();
     }
 
     /**
      *
      * @param position the position to convert to FEN piece placement substring
      * @return the string representing piece placement (1st component of FEN
      * record)
      */
     public String positionToFen(Position position) {
         StringBuilder sb = new StringBuilder(64);
         int counter = 0;
         for (Square sq : Square.values()) {
             Piece p = position.getPiece(sq);
             if (p != null) {
                 if (counter != 0) { //empty the counter before each piece
                     sb.append(counter);
                     counter = 0;
                 }
                 sb.append(p.getFenLetter());
             } else {
                 counter++;
             }
             if (sq.getFile() == 7) {
                 if (counter != 0) { //empty the counter before each rank
                     sb.append(counter);
                     counter = 0;
                 }
                 if (sq.getRank() != 0) {
                     sb.append("/");
                 }
             }
         }
         return sb.toString();
     }
 
     /**
      * Parses given string, populating GameState object. If the parameter is
      * invalid fen string, an {@link InvalidFenException} is thrown. Valid fen
      * string has the following properties: <ul> <li>It is not null</li>
      * <li>Must contain exactly 6 fields separated by space</li><ul> <li>1st
      * field: represents piece positioning and additionally conforms to the
      * following restrictions:</li> <ul> <li>contains exactly 8 substrings (1
      * per rank) separated by "/"</li><li>only contains letters:
      * pnbrqkPNBRQK12345678/</li> <li>each component (rank) "sums up" to 8 (sum
      * of all digits + 1 for each piece)</li> </ul>
      *
      * <li>2nd field: is either w or b, representing player to move</li> <li>3rd
      * field: consists of (some of) the letters KQkq (in that order)
      * representing castling availability</li> <li>4th field: is either - or
      * target square for en-passant capture</li> <li>5th field: represents
      * half-move clock</li> <li>6th field: represents full-move number</li>
      * </ul></ul>
      */
     public GameState fenToGameState(String fenString) throws InvalidFenException {
         if (fenString == null) {
             throw new NullPointerException("fenString must not be null!");
         }
         resetParserState();
         LOG.info("Parsing: \"{}\"", fenString);
 
         //0. It must have 6 fields separated by spaces
         String[] fields = fenString.split(" ");
         if (fields.length != 6) {
             throw new InvalidFenException(format(SIX_FIELD_MSG, fenString, fields.length));
         }
 
         //1. Process placement string
         this.position = fenToPosition(fields[0]);
 
         //2. Check active color - only 1 letter w or b
         if (!"w".equals(fields[1]) && !"b".equals(fields[1])) {
             throw new InvalidFenException(format(PLAYER_FIELD_MSG, fields[1]));
         } else {
             this.whiteToMove = "w".equals(fields[1]) ? true : false;
             LOG.debug("    2. It's {} to move", this.whiteToMove ? "WHITE" : "BLACK");
         }
 
         //3. check castling availability string
         if (!CASTLING_AVAILABILITY_PATTERN.matcher(fields[2]).matches()) {
             throw new InvalidFenException(format(CA_FIELD_MSG, fields[2]));
         } else {
             this.castlings = Castling.parseFenCastlingSubstring(fields[2]);
             LOG.debug("    3. Castling availabilities: {}", this.castlings);
         }
 
         //4. Check en-passant target square
         if (!EN_PASSANT_PATTERN.matcher(fields[3]).matches()) {
             throw new InvalidFenException(format(EP_FIELD_MSG, fields[3]));
         } else {
             if (!"-".equals(fields[3])) {
                 this.enPassantTargetSquare = Square.valueOf(fields[3].toUpperCase());
             } //else it remains null
             LOG.debug("    4. En-passant target square: {}", this.enPassantTargetSquare);
         }
 
         //check halfmove clock & fullmove number
         if (!DIGIT_PATTERN.matcher(fields[4]).matches()
                 || !DIGIT_PATTERN.matcher(fields[5]).matches()) {
             throw new InvalidFenException(format(COUNTERS_FILED_MSG, fields[4], fields[5]));
         } else {
             this.halfmoveClock = Integer.valueOf(fields[4]);
             LOG.debug("    5. Half-move clock: {}", this.halfmoveClock);
             this.fullmoveNumber = Integer.valueOf(fields[5]);
             LOG.debug("    6. Full-move number: {}", this.fullmoveNumber);
         }
 
         return new GameStateImpl(position, whiteToMove, castlings, enPassantTargetSquare, halfmoveClock, fullmoveNumber);
     }
 
     /**
      *
      * @param piecePlacementSubstring The first field (piece placement) of Fen
      * string
      * @return MutablePosition with the same piece placement as the one
      * described in the input fen piece placement field
      * @throws InvalidFenException
      */
     public Position fenToPosition(String piecePlacementSubstring) throws InvalidFenException {
         String[] ranks = piecePlacementSubstring.split("/");
         //Check there are exactly 8 ranks, separated by "/"
         if (ranks.length != 8) {
             throw new InvalidFenException("piecePlacement field of FEN must have"
                     + " exactly 8 ranks, each of them separated by \"/\". But "
                     + "this one (" + piecePlacementSubstring + ") had "
                     + ranks.length + "ranks");
         }
 
         //Check, that there are only correct characters (piece FEN names + digits 0-8 and slashes "/"
         if (!piecePlacementSubstring.matches("^[pnbrqkPNBRQK1-8/]+$")) {
             throw new InvalidFenException("piecePlacement must only contain the"
                     + " following charasters: pnbrqkPNBRQK12345678 - but yours"
                     + " contained something else: " + piecePlacementSubstring);
         }
 
         //Check, that each rank "sums up" to 8 (sum of all digits digits summed + 1 for each piece)
         for (int i = 0; i < 8; i++) {
             int rankSum = sumRank(ranks[i]);
             if (rankSum != 8) {
                 throw new InvalidFenException("Each rank in piecePlacement must"
                         + " sum up to 8 (sum all digits + 1 for each piece), but"
                         + " your rank (" + ranks[i] + ") summed to " + rankSum);
             }
         }
 
         //Everything seems OK, initialize the position using the info from piece-placement substring
         Map<Square, Piece> piecePlacement = new HashMap<>();
         for (int rankIdx = 7; rankIdx >= 0; rankIdx--) {
             int colIdx = 0;
             for (char c : ranks[7 - rankIdx].toCharArray()) {
                 if (Character.isLetter(c)) { //it is letter -> put corresponding piece on
                     //LOG.debug("Putting {} on {}", Piece.getPiece(c), Square.getSquare(colIdx, rankIdx));
                     piecePlacement.put(Square.getSquare(colIdx, rankIdx), Piece.getPiece(c));
                     colIdx++;
                 } else { //it is number --> move 'c' columns to the right
                     colIdx += Character.getNumericValue(c);
                 }
             }
         }
         Position newPos = new PositionImpl(piecePlacement);
         LOG.debug("    1. Position \n{}", newPos);
         return newPos;
     }
 
     /**
      * Given fen string of rank (one of the 8 things you get by splitting the
      * piece Placement substring of fen around "/" character) it computes the
      * number of squares on that rank (i.e. sums all digits + adds 1 for each
      * piece)
      *
      * @param fenStringOfRank
      * @return
      */
     private int sumRank(String fenStringOfRank) {
         int sum = 0;
         for (char c : fenStringOfRank.toCharArray()) {
             sum += Character.isDigit(c) ? Character.getNumericValue(c) : 1;
         }
         return sum;
     }
 
     /**
      * Used to reset inner state of parser to "clear" state.
      */
     private void resetParserState() {
         position = null;
         whiteToMove = false;
         castlings = EnumSet.noneOf(Castling.class);
         enPassantTargetSquare = null;
         halfmoveClock = 0;
         fullmoveNumber = 0;
     }
     //fields for storing initialization values of currently parsed fen string
     private Position position;
     private boolean whiteToMove;
     private EnumSet<Castling> castlings;
     private Square enPassantTargetSquare;
     private int halfmoveClock;
     private int fullmoveNumber;
     //loggind and exception messages
     private static final Logger LOG = LoggerFactory.getLogger(FenParser.class);
     private static final String SIX_FIELD_MSG = "Fen string must have exactly 6 fields separated by spaces, but this one (%s) had %d";
     private static final String PLAYER_FIELD_MSG = "The 2nd field of FEN must be either letter w or b, but yours was: %s";
     private static final String CA_FIELD_MSG = "The 3rd field of FEN must consist of (some of the) letters KQkq in that order or be -, but yours was: %s";
     private static final String EP_FIELD_MSG = "The 4th fen field must be either \"-\" or valid square for en-passant capture - (on 3rd or 6th rank). But yours was: %s";
     private static final String COUNTERS_FILED_MSG = "The 5th and 6th fen fields must be valid decimal digits, but yours were: %d and %d";
     //Patterns for checking valid values for FEN string fields
     private static final Pattern DIGIT_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern CASTLING_AVAILABILITY_PATTERN = Pattern.compile("^KQ?k?q?$|^K?Qk?q?$|^K?Q?kq?$|^K?Q?k?q$|^-$"); //Means either one or more of the mentioned, or "-", NOT the empty string
     private static final Pattern EN_PASSANT_PATTERN = Pattern.compile("^[abcdefgh][36]$|^-$");
     //
     public static final String INITIAL_STATE_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
 }
