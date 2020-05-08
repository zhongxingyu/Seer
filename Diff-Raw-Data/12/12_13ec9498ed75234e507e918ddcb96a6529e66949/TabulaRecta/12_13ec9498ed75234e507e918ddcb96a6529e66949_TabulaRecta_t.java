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
         String pass = this.get(startRow, startCol);
 
         int row = startRow;
         int col = startCol;
 
         for (SequenceItem i : sequence.getItemList()) {
 
             Direction dir = i.getDirection();
 
             for (int x = 0; x < i.getLength(); x++) {
                 switch (dir) {
                 case N:
                     row--;
                     break;
                 case S:
                     row++;
                 case E:
                     col--;
                 case W:
                     col++;
                 case NE:
                     row--;
                     col--;
                 case NW:
                    row--;
                     col++;
                 case SE:
                     row++;
                     col--;
                 case SW:
                    row++;
                     col++;
                 }
 
                 // XXX add better algorithm here to detect when we hit a wall
                 //     need to define direction precedence. If can't got N then go W etc.
                 if (row >= this.rows())
                     row = this.rows() - 1;
                 if (col >= this.cols())
                     col = this.cols() - 1;
 
                 pass += this.get(row, col);
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
 
         // rand.reSeedSecure();
 
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
