 /*
 3.3.14. 
 Genome. Implement a data type to store the genome of an organism. 
 Biologists often abstract away the genome to a sequence of nucleotides (A, C, G, or T). The data type should support the
 check that only legal nucleotides are added, can change to more time or memory efficient implementation without affecting client.
 
     StringGenome.java has one instance variable of type String. It implements addNucleotide with string concatenation. Each method call takes time proportional to the size of the current genome. Not practical spacewise either for large genomes since nucleotide is stored as a 16-bit char.
     
 Genome.java implements a genome as an array of characters. The size of the array is doubled when the array fills up. The method addNucleotide is now constant time. Space consumption is still 16 bits per nucleotide.
     
     CompactGenome.java implements a genome as boolean array. We need to use two bits per nucleotide since there are 4 different nucleotides. As in the previous implementation, we use a dynamic array with repeated doubling. Now, each nucleotide consumes 2 bits of storage (instead of 16). 
 битовые операции используются для того чтобы хранить 4 двухбитовых значения. для модификации отдельных значений нужно применять битовые операции над типом int. типа прочитать первые два бита, изменить биты 4 и 5..
 
 
 */
 package JavaBook;
 import JavaBook.stanfStd.*;
 
 public class Genome {
 
     private static final char[] NUCLEOTID_VALUES = {'A', 'T', 'G', 'C'};
     private static final char[] DEFAULT_VALUES = {'A', 'T', 'G', 'C','G', 'C','G', 'A'};
 
     private char[] values;
     private int endIndex; // index of the last value in array (since array length can b grtr than the number of nucleotides in it) 
 
     // constructors:
     public Genome() {
         this(DEFAULT_VALUES);
     }
 
     public Genome(char[] values) {
         this(values, values.length-1);
     }
 
     public Genome(char[] values, int endIndex) {
         if (checkArray(values)) {
             this.values = values; // should do defensive copy i guess (and add Character.toUpperCase() )
             this.endIndex = endIndex;
         } else {
             StdOut.println("trying to create genome failed.");
             // change it to throw exception
             this.values = null;
             this.endIndex = 0;
         }
     }
     // methods
 
     private static boolean isNucleotide (char inputChar) {
         if (inputChar == 'A' || inputChar == 'T' || inputChar == 'C' || inputChar == 'G' ) {
             return true;
         } else {
             StdOut.println("not nucleotide (checking input char " + Character.toString(inputChar) + ").");
             return false;
         }
     }
 
     private static boolean checkArray (char[] inputArray) {
         for (int i = 0; i < inputArray.length; i++) {
             if (!isNucleotide(inputArray[i])) {
                 StdOut.println("not nucleotide in checking array.");
                 return false;
             }
         }
         return true;
     }
 
     public Genome addCodon (char[] codon) {
         if (checkArray(codon)) {
             Genome genomeWithCodon = new Genome(this.values, this.endIndex);
             for (int i = 0; i < codon.length; i++) {
                 genomeWithCodon = this.addNucl(codon[i]);
             }
             return genomeWithCodon;
         } else {
             StdOut.println("Adding codon failed.");
             return this; // yeah, exception needed ..or msg should b like "no changes were made"
         }
     }
 
     public Genome addNucl (char nucleotide) {
         if (isNucleotide(nucleotide)) {
             if (this.values.length-1 == this.endIndex) { 
                 Genome newGenome = this.increaseLength();
                 newGenome.values[++newGenome.endIndex] = nucleotide;
                 return newGenome;
             }
             this.values[++this.endIndex] = nucleotide;
             return this;
         }
         StdOut.println("Adding nucleotide failed cause it's not nucleotide char."); // exception?
         return this; // yeah, exception needed ..or msg should b like "no changes were made"
     }
 
     @Override
     public String toString () {
         String out = "";
         for (int i = 0; i <= this.endIndex; i++) {
             out += this.values[i];
         }
         return out;
     }
 
     public static String toString (Genome input) {
         String out = "";
         for (int i = 0; i <= input.endIndex; i++) {
             out += input.values[i];
         }
         return out;
     }
 
     private Genome increaseLength () {
        char[] increasedArray = new char[this.values.length * 2];
        copyChArray(this.values, increasedArray);
        this.values = increasedArray;
        return this;
     }
 
     // could possibly use StdArraysIO instead
    private static void copyChArray (char[] src, char[] dst) {
        if (src.length > dst.length) {
            throw new RuntimeException("Not enough space in dst");
        }
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i];
         }
     }
 
     public char nucleotideAt (int i) {
         if (i <= this.endIndex) {
             return this.values[i];
         } else {
             StdOut.println("Some error msg."); // change to exception not to return char
             return ' ';
         }
     }
 
     public static void main(String[] args) {
         // read dna sequence
         String dna = args[0];
         char[] input = new char[dna.length()];
         for (int i = 0; i < dna.length(); i++) {
             input[i]=dna.charAt(i);
         }
         Genome inputG = new Genome(input);
         Genome defaultG = new Genome();
 
         StdOut.println("Inputed array: "+ inputG.toString() + " or another method: " + toString(inputG));
         StdOut.println("Default array: "+ defaultG.toString() + " (or with another method: " + toString(defaultG) + ").");
         StdOut.println("After adding A to input: "+ inputG.addNucl('A') + " and to default: " + defaultG.addNucl('A') );
         char[] codon = {'T', 'G', 'T'};
         StdOut.println("After adding codon TGT to input: "+ inputG.addCodon(codon) + " and to default: " + defaultG.addCodon(codon) );
         StdOut.println("In default gene nucleotide at position 3 is: "+ defaultG.nucleotideAt(3) );
     }
 }
