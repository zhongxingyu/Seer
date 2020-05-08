 package org.qnot.pwrecta;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 
 import org.apache.commons.math.random.RandomDataImpl;
 
 import com.google.gson.Gson;
 
 public class TabulaRecta {
 
     private String[][] tabulaRecta;
     private Alphabet headerAlphabet;
     private Alphabet dataAlphabet;
 
     public TabulaRecta(Alphabet headerAlphabet, Alphabet dataAlphabet) {
         this.headerAlphabet = headerAlphabet;
         this.dataAlphabet = dataAlphabet;
         this.tabulaRecta = new String[headerAlphabet.size()][headerAlphabet
                 .size()];
     }
 
     public static TabulaRecta fromJSON(String json) {
         Gson gson = new Gson();
         return gson.fromJson(json, TabulaRecta.class);
     }
 
     public String get(int row, int col) {
         return this.tabulaRecta[row][col];
     }
 
     public Alphabet getHeader() {
         return this.headerAlphabet;
     }
 
     public Alphabet getDataAlphabet() {
         return this.dataAlphabet;
     }
 
     public String[][] getRawData() {
         return this.tabulaRecta;
     }
 
     public int rows() {
         return this.tabulaRecta.length;
     }
 
     public int cols() {
         return this.tabulaRecta.length;
     }
 
     public String[][] asStringArray() {
         String[][] array = new String[this.rows() + 1][this.cols() + 1];
         array[0][0] = " ";
 
         for (int i = 0; i < this.cols(); i++) {
             array[0][i + 1] = this.headerAlphabet.get(i);
         }
 
         for (int i = 0; i < this.rows(); i++) {
             array[i + 1][0] = this.headerAlphabet.get(i);
             for (int j = 0; j < this.cols(); j++) {
                 array[i + 1][j + 1] = this.get(i, j);
             }
         }
 
         return array;
     }
 
     public String getPassword(int startRow, int startCol, Sequence sequence) {
         return this.getPassword(startRow, startCol, sequence, false, null);
     }
 
     public String getPassword(int startRow, int startCol, Sequence sequence,
             Direction[] directionPriority) {
         return this.getPassword(startRow, startCol, sequence, false,
                 directionPriority);
     }
 
     public String getPassword(int startRow, int startCol, Sequence sequence,
             boolean skipStart, Direction[] directionPriority) {
         String pass = "";
         
         if(!skipStart) {
             pass += this.get(startRow, startCol);
         }
 
         Position pos = new Position(startRow, startCol);
 
         for (SequenceItem i : sequence.getItemList()) {
 
            Direction dir = i.getDirection();
             int len = i.getLength();
             boolean skip = false;
 
             if (len < 0) {
                 skip = true;
                 len = Math.abs(len);
             }
 
             for (int x = 0; x < len; x++) {
                 pos.move(dir);
 
                 if (pos.isOutOfBounds(this.rows() - 1, this.cols() - 1)
                         && directionPriority != null) {
                     pos.backup();
 
                     for (Direction dp : directionPriority) {
                         pos.move(dp);
                         if (pos.isOutOfBounds(this.rows() - 1, this.cols() - 1)) {
                             pos.backup();
                         } else {
                             break;
                         }
                     }
                 }
 
                 if (pos.isOutOfBounds(this.rows() - 1, this.cols() - 1)) {
                     pos.setWithinBounds(this.rows() - 1, this.cols() - 1);
                 }
 
                 if (!skip)
                     pass += this.get(pos.getRow(), pos.getCol());
             }
         }
 
         return pass;
     }
 
     public void generate() {
         RandomDataImpl rand = new RandomDataImpl();
         try {
             rand.setSecureAlgorithm("SHA1PRNG", "SUN");
         } catch (NoSuchProviderException e) {
             throw new RuntimeException("Failed to set secure provider", e);
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException("Failed to set secure algorithm", e);
         }
 
         for (int i = 0; i < this.headerAlphabet.size(); i++) {
             tabulaRecta[i] = generateRow(i, rand);
         }
     }
 
     private String[] generateRow(int rowIndex, RandomDataImpl rand) {
         String[] row = new String[this.headerAlphabet.size()];
 
         for (int i = 0; i < this.headerAlphabet.size(); i++) {
             int index = rand.nextSecureInt(0, (this.dataAlphabet.size() - 1));
             row[i] = this.dataAlphabet.get(index);
         }
 
         return row;
     }
 }
