 // Oliver Kullmann, 6.12.2010 (Swansea)
 
 class Board {
     public static final int N = 8;
     public static final char
         white_king = 'K',
         white_queen = 'Q',
         white_rook = 'R',
         white_bishop = 'B',
         white_knight = 'N',
         white_pawn = 'P',
         black_king = 'k',
         black_queen = 'q',
         black_rook = 'r',
         black_bishop = 'b',
         black_knight = 'n',
         black_pawn = 'p',
         empty = 0;
 
     public static final String
         initial_position = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
     
     private final char[][] board;
     private char active_colour; // 'w' or 'b'
     // Castling codes: '-' (none) or 'k' (only king-side) or
     // 'q' (only queen-side) or 'b' (both)
     private char white_castling;
     private char black_castling;
     private int halfmoves;
     private int fullmoves;
 
     private final Moves M;
 
     public Board() { // standard position
         M = new Moves(this);
         board = new char[N][N];
         fromFEN(initial_position);
     }
     public Board(final String fen_position) {
         M = new Moves(this);
         board = new char[N][N];
         fromFEN(fen_position);
     }
 
     public char get(final char file, final char rank) {
         return board[rank2index(rank)][file2index(file)];
     }
     public char get_active_colour() {
         return active_colour;
     }
     public char get_white_castling() {
         return white_castling;
     }
     public char get_black_castling() {
         return black_castling;
     }
     public int get_halfmoves() {
         return halfmoves;
     }
     public int get_fullmoves() {
         return fullmoves;
     }
     
     public void do_white_kingside_castling() {
         assert(M.check_white_kingside_castling());
         reset('e','1');
         reset('h','1');
         set('f','1','R');
         set('g','1','K');
         active_colour = 'b';
         white_castling = '-';
         ++halfmoves;
     }
     public void do_white_queenside_castling() {
         assert(M.check_white_queenside_castling());
         // XXX
     }
     public void do_black_kingside_castling() {
         assert(M.check_black_kingside_castling());
         reset('e','8');
         reset('h','8');
         set('f','8','r');
         set('g','8','k');
         active_colour = 'w';
         black_castling = '-';
         ++halfmoves;
         ++fullmoves;
     }
     public void do_black_queenside_castling() {
         assert(M.check_black_queenside_castling());
         // XXX
     }
 
     public void do_normal_white_move(final char file0, final char rank0,
                                      final char file1, final char rank1) {
         assert(M.check_normal_white_move(file0,rank0,file1,rank1));
         // XXX
     }
     public void do_normal_black_move(final char file0, final char rank0,
                                      final char file1, final char rank1) {
         assert(M.check_normal_black_move(file0,rank0,file1,rank1));
         // XXX
     }
 
     // set figure on field:
     private void set(final char file, final char rank, final char figure) {
         assert(is_valid_figure(figure));
         board[rank2index(rank)][file2index(file)] = figure;
     }
     // delete figure on field:
     private void reset(final char file, final char rank) {
         board[rank2index(rank)][file2index(file)] = empty;
     }
 
     public static boolean is_valid_figure(final char figure) {
         return is_valid_white_figure(figure) || is_valid_black_figure(figure);
     }
     public static boolean is_valid_white_figure(final char figure) {
         return figure == white_king || figure == white_queen ||
             figure == white_rook || figure == white_bishop ||
             figure == white_knight || figure == white_pawn;
     }
     public static boolean is_valid_black_figure(final char figure) {
         return figure == black_king || figure == black_queen ||
             figure == black_rook || figure == black_bishop ||
             figure == black_knight || figure == black_pawn;
     }
 
     public static boolean validFEN(final String position) {
         final String[] parts = position.split("\\s+");
         if (parts.length != 6) return false;
         final String fields = parts[0];
         final String colour = parts[1];
         final String castling = parts[2];
         final String en_passant = parts[3];
         final String halfmoves = parts[4];
         final String fullmoves = parts[5];
         if (! colour.equals("w") && ! colour.equals("b")) return false;
         try {
           final int h = Integer.parseInt(halfmoves);
           if (h < 0) return false;
           final int f = Integer.parseInt(fullmoves);
           if (f <= 0) return false;
         }
         catch (RuntimeException e) { return false; }
         if (! en_passant.equals("-")) {
           if (en_passant.length() != 2) return false;
           final char file = en_passant.charAt(0);
           if (file < 'a' || file > 'h') return false;
           final char rank = en_passant.charAt(1);
           if (rank < '1' || rank > '8') return false;
         }
         if (! castling.equals("-")) {
           int count = 0;
          int max = -1;
           final int K = castling.indexOf("K");
           if (K != -1) ++count;
          max = Math.max(max,K);
          final int Q = castling.indexOf("Q",max);
           if (Q != -1) ++count;
          max = Math.max(max,Q);
          final int k = castling.indexOf("k",max);
           if (k != -1) ++count;
          max = Math.max(max,k);
          final int q = castling.indexOf("q",max);
           if (q != -1) ++count;
           if (count != castling.length()) return false;
         }
         final String[] rows = fields.split("/");
         if (rows.length != N) return false;
         for (int r = 0; r < N; ++r)
           if (! check_fen_row(rows[r])) return false;
         return true;
     }
     private static boolean check_fen_row(final String row) {
       int num_fields = 0;
       for (int i = 0; i < row.length(); ++i) {
         final char c = row.charAt(i);
         if (is_valid_figure(c)) ++num_fields;
         else
           if (c < '1' || c > '8') return false;
           else num_fields += c - '0';
       }
       return num_fields == N;
     }
 
     // set the position according to description in Forsyth-Edwards notation:
     private void fromFEN(final String position) {
         assert(validFEN(position));
         // XXX
     }
     // describe the position in Forsyth-Edwards notation:
     public String toFEN() {
         String result = "";
         // XXX
         return result;
     }
     // printing out position in 8 rows of characters,
     // using spaces for empty fields:
     public String toString() {
         String result = "";
         for (int x = N-1; x >= 0; --x) {
             result += (x+1) + ":";
             for (int y = 0; y < N; ++y) {
                 final char figure = board[x][y];
                 result += (figure == empty) ? '.' : figure;
             }
             result += "\n";
         }
         result += "\nActive: " + active_colour + "\n";
         return result;
     }
 
     public boolean equals(final Board other) {
         for (int x = 0; x <= 7; ++x)
             for (int y = 0; y <= 7; ++y)
                 if (board[x][y] != other.board[x][y]) return false;
         return active_colour == other.active_colour &&
             white_castling == other.white_castling &&
             black_castling == other.black_castling &&
             halfmoves == other.halfmoves &&
             fullmoves == other.fullmoves;
     }
 
     private boolean is_empty(final int x, final int y) {
         assert(0 <= x);
         assert(x <= 7);
         assert(0 <= y);
         assert(y <= 7);
         return board[x][y] == empty;
     }
     // from file a, ..., h to index 0, ..., 7:
     private static int file2index(final char file) {
         assert(file >= 'a');
         assert(file <= 'h');
         return file - 'a';
     }
     // from rank 1, ..., 8 to index 0, ..., 7:
     private static int rank2index(final char rank) {
         assert(rank >= '1');
         assert(rank <= '8');
         return rank - '1';
     }
 
     // Unit testing:
     public static void main(final String[] args) {
       // testing default construction
       {
         final Board b = new Board();
         /*
         assert(b.toFEN().equals(initial_position));
         assert(b.active_colour == 'w');
         assert(b.white_castling == 'b');
         assert(b.black_castling == 'b');
         assert(b.halfmoves == 0);
         assert(b.fullmoves == 1);
         int count_blacks = 0, count_whites = 0;
         for (int x = 0; x < N; ++x)
           for (int y = 0; y < N; ++y)
             if (! b.is_empty(x,y)) {
               final char f = b.board[x][y];
               assert(is_valid_figure(f));
               if (is_valid_white_figure(f)) ++count_whites;
               else ++ count_blacks;
             }
         assert(count_whites == 16);
         assert(count_blacks == 16);
         */
       }
       // testing construction from fen
       {
 
       }
       // testing get-functions
       {
 
       }
       // testing castling functions
       {
 
       }
       // testing normal moves
       {
 
       }
       // testing validation of figures
       {
 
       }
       // testing validFEN
       {
         assert(validFEN("8/8/8/8/8/8/8/8 w - - 0 1"));
         assert(!validFEN("8/8/8/8/8/8/8/8 w - - 0 0"));
         assert(!validFEN("8/8/8/8/8/8/8/8 w - - -1 1"));
         assert(!validFEN("8/8/8/8/8/8/8/8 w j - 0 1"));
         assert(!validFEN("8/8/8/8/8/8/8/8 w QK - 0 1"));
         assert(!validFEN("8/8/8/8/8/8/8/8 w qk - 0 1"));
        assert(!validFEN("8/8/8/8/8/8/8/8 w kK - 0 1"));
         assert(!validFEN("8/8/8/8/8/8/8/8 w KK - 0 1"));
         assert(validFEN("8/8/8/8/8/8/8/8 b - - 6 99"));
         assert(validFEN("PKQNBR2/8/8/8/8/8/8/2pKQNBR b - - 6 99"));
         assert(validFEN("8/8/bBp5/8/8/2K4p/8/8 b Kq - 0 2"));
         assert(!validFEN("8/8/bBp5/8/8/2K3p/8/8 b Kq - 0 2"));
       }
       // testing fromFEN
       {
 
       }
       // testing toFEN
       {
 
       }
       // testing equals
       {
 
       }
       // testing conversions
       {
 
       }
     }
 }
