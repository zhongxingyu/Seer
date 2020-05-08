 // Oliver Kullmann, 6.12.2010 (Swansea)
 
 /*
   After construction, an object G of type Game offers the following functions:
    - G.num_valid_halfmoves is -1 if the move-sequence was syntactically invalid
    - G.get_move_sequence() is a copy of the array of valid half-moves.
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
 
   public final int num_halfmoves; // -1 iff invalid movetext
   private int num_valid_halfmoves;
   private String simplified_movetext;
 
   private final Board B;
   private final Moves M;
 
   private final char[][] move_seq;
   /* A single move is a char-array of length 5, 2 or 3, where the first
      char is 'w' or 'b', followed either
       - by the initial field and the target field (each as file, rank)
       - or by 'k', 'q' for the kingside resp. queenside castling
       - or by file and figure for a pawn promotion.
      If an invalid move is found, then from this move on all array-pointers
      will be null.
   */
 
   Game(final String ev, final String si, final String da, final int ro,
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
     num_halfmoves = valid_move_sequence();
     M = new Moves(B);
     num_valid_halfmoves = 0;
     if (num_halfmoves == -1)
       move_seq = null;
     else
       move_seq = fill_move_seq();
     if (monitor) System.out.println(this);
   }
 
   // checks for syntactical correctness (only!); returns -1 in case of
   // a syntactical error, and the number of halfmoves (>= 0) otherwise:
   private int valid_move_sequence() {
     simplified_movetext = remove_comments(movetext);
     if (simplified_movetext.isEmpty()) return -1;
     final String[] parts = simplified_movetext.split("//s+");
     
     // code will be provided YYY
     return 0;
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
 
   private char[][] fill_move_seq() {
     char[][] ms = new char[num_halfmoves][];
     // XXX fill ms with the moves
     while (num_valid_halfmoves < num_halfmoves) {
       if (ms[num_valid_halfmoves] != null) ++num_valid_halfmoves;
       else break;
     }
     return ms;
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
       final Game g = new Game(
         "F/S Return Match",
         "Belgrade, Serbia Yugoslavia|JUG",
         "1992.11.04",
         29,
         "Fischer, Robert J.",
         "Spassky, Boris V.",
         "1/2-1/2",
         "1. e4 e5 2. Nf3 Nc6 3. Bb5 {This opening is called Ruy Lopez.} 3... a6 4. Ba4 Nf6 5. 0-0 Be7 6. Re1 b5 7. Bb3 d6 8. c3 0-0 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5 Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6 23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5 hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kfe Bf5 35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6 Nf2 42. g4 Bd3 43. Re6 1/2-1/2",
         "",
         true);
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
 
