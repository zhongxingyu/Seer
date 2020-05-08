 package org.spacebar.escape.j2se;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
 
 import org.spacebar.escape.common.BitInputStream;
 import org.spacebar.escape.common.Misc;
 import org.spacebar.escape.common.Solution;
 import org.spacebar.escape.common.hash.MD5;
 
 public class PlayerInfo {
     static final private String PLAYER_MAGIC = "ESXP";
 
     static final private String PLAYERTEXT_MAGIC = "ESPt";
 
     static final private String SOL_MARKER = "-- solutions";
 
     static final private String RAT_MARKER = "-- ratings";
 
 //    static final private String PREF_MARKER = "-- prefs";
 
     static final private int IGNORED_FIELDS = 8;
 
     int webID;
 
     int webSeqH;
 
     int webSeqL;
 
     String name;
 
     Map<MD5, List<Solution>> solutions = new HashMap<MD5, List<Solution>>();
 
     /**
      * @return Returns the solutions.
      */
     public Map<MD5, List<Solution>> getSolutions() {
         return solutions;
     }
 
     public PlayerInfo(BitInputStream in) throws IOException {
         // read magic
         String magic = Misc.getStringFromData(in, 4);
 
         // decide
         if (magic.equals(PLAYER_MAGIC)) {
             decodeBinaryFormat(in);
         } else if (magic.equals(PLAYERTEXT_MAGIC)) {
             decodeTextFormat(in);
         } else {
             throw new IOException("Bad magic: '" + magic + "'");
         }
     }
 
     private void decodeTextFormat(InputStream in) throws IOException {
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
         String s;
 
         // get line after magic
         br.readLine();
 
         // get web stuff
         webID = Integer.parseInt(br.readLine());
         webSeqH = Integer.parseInt(br.readLine());
         webSeqL = Integer.parseInt(br.readLine());
 
         // skip over
         for (int i = 0; i < IGNORED_FIELDS; i++) {
             br.readLine();
         }
 
         // name
         name = br.readLine();
 
         // all solutions
         if (!br.readLine().equals(SOL_MARKER)) {
             throw new IOException("Solution marker not found");
         }
 
         for (;;) {
             s = br.readLine();
             //            System.out.println("'" + s + "'");
             if (s.equals(RAT_MARKER)) {
                 break;
             }
 
             // XXX XXX
             StringTokenizer st = new StringTokenizer(s);
             // md5
             String str = st.nextToken();
             MD5 md5 = new MD5(str);
 
             str = st.nextToken();
 
             if (str.equals("*")) {
                 // named, and multiple solutions, ending with "!"
                 str = st.nextToken();
                 addSolution(md5, new Solution(str, true), true);
 
                 // read rest of lines until !
                while (((s = br.readLine()) != null) && !(s.trim()).equals("!")) {
                     addSolution(md5, new Solution(s, true), true);
                 }
             } else {
                 // old, single solution
                 addSolution(md5, new Solution(str, false), false);
             }
         }
 
         // read optional ratings
         // XXX
 
         // read optional chunks
         // XXX
     }
 
     private void decodeBinaryFormat(BitInputStream in) throws IOException {
         // get web stuff
         webID = in.readInt();
         webSeqH = in.readInt();
         webSeqL = in.readInt();
 
         // skip over
         for (int i = 0; i < IGNORED_FIELDS; i++) {
             in.readInt();
         }
 
         // name
         int len = in.readInt();
         name = Misc.getStringFromData(in, len);
 
         // all solutions
         int numSolutions = in.readInt();
         while (numSolutions-- > 0) {
             // md5
             MD5 md5 = new MD5(in);
 
             // discard length of rle encoded bytes
             in.readInt();
 
             // get solution
             Solution s = new Solution(in);
 
             addSolution(md5, s, false);
         }
 
         // read optional ratings
         // XXX
 
         // read optional chunks
         // XXX
     }
 
     @Override
 	public String toString() {
         return getName() + " #" + getWebID() + ", " + getSolutions().size()
                 + " solutions";
     }
 
     private void addSolution(MD5 md5, Solution s, boolean append) {
         List<Solution> v = solutions.get(md5);
         if (v == null) {
             v = new ArrayList<Solution>();
             solutions.put(md5, v);
         }
 
         // if there is already something, and we are not appending, forget it
         if (!append && !v.isEmpty()) {
             return;
         }
 
         // add the item to the front
         v.add(0, s);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getWebID() {
         return webID;
     }
 
     public void setWebID(int webID) {
         this.webID = webID;
     }
 
     public int getWebSeqH() {
         return webSeqH;
     }
 
     public void setWebSeqH(int webSeqH) {
         this.webSeqH = webSeqH;
     }
 
     public int getWebSeqL() {
         return webSeqL;
     }
 
     public void setWebSeqL(int webSeqL) {
         this.webSeqL = webSeqL;
     }
 }
