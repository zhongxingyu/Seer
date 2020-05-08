 // Oliver Kullmann, 6.12.2010 (Swansea)
 
 /*
   After construction, an object G of type Game offers G.get_move_sequence(),
   a copy of the array of valid half-moves (null iff the move-sequence was
   syntactically invalid), plus the input data via constant data members.
 */
 
 class Game {
 
   public final String
     white_won = "1-0",
     black_won = "0-1",
     draw = "1/2-1/2",
     unknown = "*";
 
   public final String
     event, site, date, name_w, name_b, result, movetext, fen;
   public final int round;
 
   public final boolean monitor;
 
   private int num_halfmoves; // -1 iff invalid movetext
   private int num_valid_halfmoves;
   private String simplified_movetext;
 
   private final Board B;
   private final Moves M;
 
   private char[][] move_seq;
   /* A single move is a char-array of length 6, 3 or 4, where the first
      char is 'w' or 'b' ("white" or "black"), followed by 'c' for "check"
      or 'm' for "mate" or '-' for neither, followed either
       - by the initial field and the target field (each as (file, rank))
       - or by 'k' or 'q' for the kingside resp. queenside castling
       - or by file and figure for a pawn promotion.
      If an invalid move is found, then from this move on all array-pointers
      will be null.
      If the move-sequence was syntactically invalid, then move_seq is null.
   */
 
   public Game(final String ev, final String si, final String da, final int ro,
               final String nw, final String nb, final String re,
               final String mo, final String fe, final boolean mon) {
     assert(!ev.isEmpty());
     assert(!si.isEmpty());
     assert(!da.isEmpty());
     assert(!nw.isEmpty());
     assert(!nb.isEmpty());
     assert(re.equals(white_won) || re.equals(black_won) || re.equals(draw) || re.equals(unknown));
     assert(fe.isEmpty() || Board.validFEN(fe));
     // if fen is empty then the standard position is used
     event = ev; site = si; date = da; round = ro;
     name_w = nw; name_b = nb; result = re; movetext = mo;
     fen = fe; monitor = mon;
     if (fen.isEmpty()) B = new Board();
     else B = new Board(fen);
     num_halfmoves = -1;
     valid_move_sequence();
     M = new Moves(B);
     num_valid_halfmoves = 0;
     move_seq = null;
     if (num_halfmoves != -1) fill_move_seq();
     if (monitor) System.out.println(this);
   }
 
   // checks for syntactical correctness (only!); sets num_halfmoves and
   // simplified_movetext, where num_halfmoves == -1 in case of
   // a syntactical error:
   private void valid_move_sequence() {
     simplified_movetext = remove_comments(movetext);
     if (simplified_movetext.isEmpty()) return;
     final String[] parts = simplified_movetext.split("\\s+");
     boolean white_current_colour = (B.get_active_colour() == 'w');
     int fullmoves = B.get_fullmoves();
     String new_movetext = "";
     boolean read_number = true;
     for (int i = 0; i < parts.length; ++i) {
       if (white_current_colour) {
         if (read_number) {
           if (convert(parts[i],true) != fullmoves) {
             num_halfmoves = -1; return;
           }
           else read_number = false;
         }
         else {
           if (! valid_movement(parts[i])) { num_halfmoves = -1; return; }
           else {
             ++num_halfmoves; new_movetext += parts[i] + " ";
             white_current_colour = false; read_number = true;
           }
         }
       }
       else {
         if (read_number)
           if (convert(parts[i],false) == fullmoves) {
             read_number = false; continue;
           }
         if (! valid_movement(parts[i])) { num_halfmoves = -1; return; }
         else {
           ++num_halfmoves; new_movetext += parts[i] + " ";
           white_current_colour = true; read_number = true;
           ++fullmoves;
         }
       }
     }
     simplified_movetext = new_movetext;
   }
   // removes comments, returning the empty string in case of error; assumes
   // that "{" or "}" are not used in comments opened by ";":
   private static String remove_comments(String seq) {
     // first removing comments of the form "{...}":
     for (int opening_bracket = seq.indexOf("{"); opening_bracket != -1;
          opening_bracket = seq.indexOf("{")) {
       final int closing_bracket = seq.indexOf("}");
       if (closing_bracket < opening_bracket) return "";
       seq = seq.substring(0,opening_bracket) + seq.substring(closing_bracket+1);
     }
     if (seq.contains("}")) return "";
     // now removing comments of the form ";... EOL":
     for (int semicolon = seq.indexOf(";"); semicolon != -1;
          semicolon = seq.indexOf(";")) {
       final int eol = seq.indexOf("\n",semicolon);
       if (eol == -1) return "";
       seq = seq.substring(0,semicolon) + seq.substring(eol+1);
     }
     return seq;
   }
   // converts for example "32." into 32 (for white) and "4..." into 4 (for
   // black), while invalid move-numbers result in -1:
   private static int convert(final String s, final boolean white) {
     assert(!s.contains(" "));
     final int index = s.indexOf(".");
     if (index == -1) return -1;
     if (white) { if (index+1 != s.length()) return -1; }
     else {
       if (s.length() - index != 3) return -1;
       if (s.charAt(index+1) != '.' || s.charAt(index+2) != '.') return -1;
     }
     int result;
     try { result = Integer.parseInt(s.substring(0,index)); }
     catch (RuntimeException e) { return -1; }
     if (result < 1) return -1;
     return result;
   }
   // checks whether m represents a valid SAN (like "e4" or "Bb5xa6+"):
   private static boolean valid_movement(final String m) {
     // XXX
     return true;
   }
 
   // computing the move-sequence from the from simplified_movetext, determining
   // num_valid_halfmoves and move_seq:
   private void fill_move_seq() {
     move_seq = new char[num_halfmoves][];
     // XXX fill move_seq with the moves
     while (num_valid_halfmoves < num_halfmoves) {
       if (move_seq[num_valid_halfmoves] != null) ++num_valid_halfmoves;
       else break;
     }
   }
 
   public char[][] get_move_sequence() {
     if (move_seq == null) return null;
     assert(num_valid_halfmoves >= 0);
     final char[][] result = new char[num_valid_halfmoves][];
     for (int i = 0; i < num_valid_halfmoves; ++i) {
       final int items_move = move_seq[i].length;
       result[i] = new char[items_move];
       for (int j = 0; j < items_move; ++j)
         result[i][j] = move_seq[i][j];
     }
     return move_seq;
   }
 
   public String toString() {
     String s = "";
     s += "Event: " + event + "\n";
     s += "Site: " + site + "\n";
     s += "Date: " + date + "\n";
     s += "Round: " + round + "\n";
     s += "White: " + name_w + "\n";
     s += "Black: " + name_b + "\n";
     s += "Result: " + result + "\n";
     s += B;
     if (num_halfmoves == -1) s += "Invalid move sequence.\n";
     return s;
   }
 
   // unit testing:
   public static void main(final String[] args) {
     // construction
     {
       final String
         ev1 = "F/S Return Match", si1 = "Belgrade, Serbia Yugoslavia|JUG",
         da1 = "1992.11.04", nw1 = "Fischer, Robert J.",
         nb1 = "Spassky, Boris V.", re1 = "1/2-1/2",
         mo1_0 = "1. e4 e5 2. Nf3 Nc6 3. Bb5 {This opening is called Ruy Lopez.} 3... a6 4. Ba4 Nf6 5. 0-0 Be7 6. Re1 b5 7. Bb3 d6 8. c3 0-0 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5 Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6 23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5 hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kfe Bf5 35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6 Nf2 42. g4 Bd3 43. Re6",
         mo1_1 = " 1/2-1/2", mo1 = mo1_0 + mo1_1, fe1 = "";
       final int ro1 = 29;
       final Game g1 = new Game(ev1,si1,da1,ro1,nw1,nb1,re1,mo1,fe1,true);
       assert(g1.event == ev1);
       assert(g1.site == si1);
       assert(g1.date == da1);
       assert(g1.round == ro1);
       assert(g1.name_w == nw1);
       assert(g1.name_b == nb1);
       assert(g1.result == re1);
       assert(g1.movetext == mo1);
       assert(g1.fen == fe1);
       final char[][] ms1 = g1.get_move_sequence();
       assert(ms1 != null);
       assert(ms1.length == 85);
       final String ev2="x",si2="x",da2="x",nw2="x",nb2="x",re2="1/2-1/2",mo2="1. 1/2-1/2",fe2="";
       final int ro2 = 0;
       final Game g2 = new Game(ev2,si2,da2,ro2,nw2,nb2,re2,mo2,fe2,true);
       final char[][] ms2 = g2.get_move_sequence();
       assert(ms2 != null);
       assert(ms2.length == 0);
      final Game g3 = new Game(ev2,si2,da2,ro2,nw2,nb2,re2,"1. e4",fe2,true);
      final char[][] ms3 = g3.get_move_sequence();
      assert(ms3 == null);
     }
     // syntax check
     {
       assert(remove_comments("").equals(""));
       assert(remove_comments("{").equals(""));
       assert(remove_comments("}").equals(""));
       assert(remove_comments("{}").equals(""));
       assert(remove_comments("xyz { jyt } kjh { bvc po5 } ").equals("xyz  kjh  "));
       assert(remove_comments(";  \nabc\nxyz;333\n").equals("abc\nxyz"));
       assert(remove_comments("sdjd{,,l;}; djsks\n{   ]}sjfdk ").equals("sdjdsjfdk "));
       assert(remove_comments(";abc").equals(""));
       assert(remove_comments(";]\na").equals("a"));
       assert(remove_comments(";}\na").equals(""));
       assert(convert("",true) == -1);
       assert(convert("",false) == -1);
       assert(convert("x",true) == -1);
       assert(convert("x",false) == -1);
       assert(convert(".",true) == -1);
       assert(convert(".",false) == -1);
       assert(convert("44..",true) == -1);
       assert(convert("44..",false) == -1);
       assert(convert("33.",true) == 33);
       assert(convert("33.",false) == -1);
       assert(convert("13...",true) == -1);
       assert(convert("13...",false) == 13);
       assert(convert("0.",true) == -1);
       assert(convert("0.",false) == -1);
       assert(convert("-2.",true) == -1);
       assert(convert("-2.",false) == -1);
       assert(convert("3[3.",true) == -1);
       assert(convert("3[3.",false) == -1);
     }
   }
 }
 
