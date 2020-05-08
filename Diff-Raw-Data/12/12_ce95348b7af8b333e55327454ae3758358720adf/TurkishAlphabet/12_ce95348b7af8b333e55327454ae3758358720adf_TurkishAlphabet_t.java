 package zemberek3.structure;
 
 import java.util.Arrays;
 
 import static zemberek3.structure.TurkicLetter.builder;
 
 /**
  * Contains Turkish Letters, Turkish Letter equivalent chars, several helper methods.
  * TurkishAlphanbet only contains small case letters.
  */
 public class TurkishAlphabet {
 
     // Turkish specific characters.
     public static final char C_CC = '\u00c7'; // Ç
     public static final char C_cc = '\u00e7'; // ç
     public static final char C_GG = '\u011e'; // Ğ
     public static final char C_gg = '\u011f'; // ğ
     public static final char C_ii = '\u0131'; // ı
     public static final char C_II = '\u0130'; // İ
     public static final char C_OO = '\u00d6'; // Ö
     public static final char C_oo = '\u00f6'; // ö
     public static final char C_SS = '\u015e'; // Ş
     public static final char C_ss = '\u015f'; // ş
     public static final char C_UU = '\u00dc'; // Ü
     public static final char C_uu = '\u00fc'; // ü
 
     // letters used in turkish text having circumflex.
     public static final char A_CIRC = '\u00c2'; // Â
     public static final char a_CIRC = '\u00e2'; // â
     public static final char I_CIRC = '\u00ce'; // Î
     public static final char i_CIRC = '\u00ee'; // î
     public static final char U_CIRC = '\u00db'; // Û
     public static final char u_CIRC = '\u00fb'; // û
 
     /**
      * Turkish Letters. q,x,w is also added for foreign proper nouns. They are marked as 'foreign'
      */
     public static final TurkicLetter L_a = builder('a', 1).vowel().build();
     public static final TurkicLetter L_b = builder('b', 2).build();
     public static final TurkicLetter L_c = builder('c', 3).build();
     public static final TurkicLetter L_cc = builder(C_cc, 4).notInAscii().voiceless().stopConsosnant().similarAscii('c').build();
     public static final TurkicLetter L_d = builder('d', 5).build();
     public static final TurkicLetter L_e = builder('e', 6).vowel().frontalVowel().build();
     public static final TurkicLetter L_f = builder('f', 7).voiceless().build();
     public static final TurkicLetter L_g = builder('g', 8).build();
     public static final TurkicLetter L_gg = builder(C_gg, 9).notInAscii().similarAscii('g').build();
     public static final TurkicLetter L_h = builder('h', 10).build();
     public static final TurkicLetter L_ii = builder(C_ii, 11).vowel().notInAscii().similarAscii('i').build();
     public static final TurkicLetter L_i = builder('i', 12).vowel().frontalVowel().build();
     public static final TurkicLetter L_j = builder('j', 13).build();
     public static final TurkicLetter L_k = builder('k', 14).voiceless().stopConsosnant().build();
     public static final TurkicLetter L_l = builder('l', 15).build();
     public static final TurkicLetter L_m = builder('m', 16).build();
     public static final TurkicLetter L_n = builder('n', 17).build();
    public static final TurkicLetter L_o = builder('o', 18).vowel().roundedVowel().build();
     public static final TurkicLetter L_oo = builder(C_oo, 19).vowel().frontalVowel().roundedVowel().notInAscii().similarAscii('o').build();
     public static final TurkicLetter L_p = builder('p', 20).voiceless().stopConsosnant().build();
     public static final TurkicLetter L_r = builder('r', 21).build();
     public static final TurkicLetter L_s = builder('s', 22).build();
     public static final TurkicLetter L_ss = builder(C_ss, 23).notInAscii().voiceless().similarAscii('s').build();
     public static final TurkicLetter L_t = builder('t', 24).voiceless().stopConsosnant().build();
     public static final TurkicLetter L_u = builder('u', 25).vowel().roundedVowel().build();
     public static final TurkicLetter L_uu = builder(C_uu, 26).vowel().roundedVowel().frontalVowel().similarAscii('u').notInAscii().build();
     public static final TurkicLetter L_v = builder('v', 27).build();
     public static final TurkicLetter L_y = builder('y', 28).build();
     public static final TurkicLetter L_z = builder('z', 29).build();
     // Not Turkish but sometimes appears in geographical names etc.
     public static final TurkicLetter L_q = builder('q', 30).foreign().build();
     public static final TurkicLetter L_w = builder('w', 31).foreign().build();
     public static final TurkicLetter L_x = builder('x', 32).foreign().build();
     // Circumflexed letters
     public static final TurkicLetter L_ac = builder(a_CIRC, 33).vowel().frontalVowel().similarAscii('a').notInAscii().build();
     public static final TurkicLetter L_ic = builder(i_CIRC, 34).vowel().frontalVowel().similarAscii('i').notInAscii().build();
     public static final TurkicLetter L_uc = builder(u_CIRC, 35).vowel().foreign().frontalVowel().similarAscii('u').roundedVowel().notInAscii().build();
 
     private static final TurkicLetter[] TURKISH_LETTERS = {
             L_a, L_b, L_c, L_cc, L_d, L_e, L_f, L_g,
             L_gg, L_h, L_ii, L_i, L_j, L_k, L_l, L_m,
             L_n, L_o, L_oo, L_p, L_r, L_s, L_ss, L_t,
             L_u, L_uu, L_v, L_y, L_z, L_q, L_w, L_x, 
             L_ac, L_ic, L_uc};
 
     private static final int ALPHABET_LETTER_COUNT = TURKISH_LETTERS.length;
 
     // 0x15f is the maximum char value in turkish specific characters. It is the size
     // of our lookup tables. This could be done better, but for now it works.
     private static final int MAX_CHAR_VALUE = 0x15f + 1;
     private static final TurkicLetter[] CHAR_TO_LETTER_LOOKUP = new TurkicLetter[MAX_CHAR_VALUE];
     private static final char[] TURKISH_ALPHABET_CHARS = new char[MAX_CHAR_VALUE];
     private static final int[] TURKISH_ALPHABET_INDEXES = new int[MAX_CHAR_VALUE];
     private static final boolean[] VALID_CHAR_TABLE = new boolean[MAX_CHAR_VALUE];
 
     static {
         Arrays.fill(CHAR_TO_LETTER_LOOKUP, TurkicLetter.UNDEFINED);
         Arrays.fill(TURKISH_ALPHABET_INDEXES, -1);
         Arrays.fill(VALID_CHAR_TABLE, false);
         for (TurkicLetter turkicLetter : TURKISH_LETTERS) {
             CHAR_TO_LETTER_LOOKUP[turkicLetter.charValue()] = turkicLetter;
             TURKISH_ALPHABET_CHARS[turkicLetter.alphabeticIndex() - 1] = turkicLetter.charValue();
             TURKISH_ALPHABET_INDEXES[turkicLetter.charValue()] = turkicLetter.alphabeticIndex();
             VALID_CHAR_TABLE[turkicLetter.charValue()] = true;
         }
     }
 
     /**
      * returns the TurkicLetter equivalent of character c.
      *
      * @param c input character
      * @return TurkishLetter equivalent.
      * @throws IllegalArgumentException if input character is out of alphabet.
      */
     public TurkicLetter getLetter(char c) {
         if (c >= MAX_CHAR_VALUE || !VALID_CHAR_TABLE[c])
             throw new IllegalArgumentException("Unexpected char:" + c);
         else return CHAR_TO_LETTER_LOOKUP[c];
     }
 
     /**
      * returns the TurkicLetter equivalent with given alphabetic index. index starts from 1.
      *
      * @param alphabeticIndex alphabetical index. starts from 1
      * @return TurkicLetter for given alphabetical index.
      * @throws IllegalArgumentException if index is [< 1] or [> alphabetsize]
      */
     public TurkicLetter getLetter(int alphabeticIndex) {
         if (alphabeticIndex < 1 || alphabeticIndex > ALPHABET_LETTER_COUNT)
             throw new IllegalArgumentException("Unexpected alphabetic index:" + alphabeticIndex);
         return TURKISH_LETTERS[alphabeticIndex - 1];
     }
 
     /**
      * returns the alphabetic index of a char.
      *
      * @param c char
      * @return alphabetic index.
      * @throws IllegalArgumentException if char is out of alphabet.
      */
     public int getAphabeticIndex(char c) {
         if (!isValid(c))
             throw new IllegalArgumentException("unexpected char:" + c);
         return TURKISH_ALPHABET_INDEXES[c];
     }
 
     /**
      * retrieves a character from alphabetical index.
      *
      * @param alphabeticIndex index
      * @return char.
      * @throws IllegalArgumentException if alphabeticIndex is [< 1] or [> alphabetsize]
      */
     public char getCharByAlphabeticIndex(int alphabeticIndex) {
         if (alphabeticIndex < 1 || alphabeticIndex > ALPHABET_LETTER_COUNT)
             throw new IllegalArgumentException("Unexpected alphabetic index:" + alphabeticIndex);
         return TURKISH_ALPHABET_CHARS[alphabeticIndex - 1];
     }
 
     // ------------------------ ASCII equivalency ----------------------------------
     // This lookup table maps each Turkish letter to its ASCII counterpart.
     private static TurkicLetter[] ASCII_EQUIVALENT_LETTER_LOOKUP = {
             L_a, L_b, L_c, L_c, L_d, L_e, L_f, L_g,
             L_g, L_h, L_i, L_i, L_j, L_k, L_l, L_m,
             L_n, L_o, L_o, L_p, L_r, L_s, L_s, L_t,
             L_u, L_u, L_v, L_y, L_z, L_q, L_w, L_x,
             L_a, L_i, L_u};
 
     private static char[] ASCII_EQUIVALENT_CHARS_LOOKUP = new char[MAX_CHAR_VALUE];
 
     static {
         Arrays.fill(ASCII_EQUIVALENT_CHARS_LOOKUP, (char) 0);
         for (TurkicLetter turkicLetter : TURKISH_LETTERS) {
             ASCII_EQUIVALENT_CHARS_LOOKUP[turkicLetter.charValue] = turkicLetter.englishEquivalentChar();
         }
     }
 
     /**
      * returns the English equivalnet letter. such as [a->a] and [c with cedil -> c]
      *
      * @param letter turkicletter
      * @return english equivalent letter.
      */
     public TurkicLetter getAsciiEquivalentLetter(TurkicLetter letter) {
         return ASCII_EQUIVALENT_LETTER_LOOKUP[letter.alphabeticIndex() - 1];
     }
 
     /**
      * checks if two characters are enlish character equal.
      *
      * @param c1 first char
      * @param c2 second char.
      * @return true if equals or enlish equivalents are same.
      */
     public boolean asciiEqual(char c1, char c2) {
         return (isValid(c1) && isValid(c2)) &&
                 (c1 == c2 || 
                     ASCII_EQUIVALENT_CHARS_LOOKUP[c1] == ASCII_EQUIVALENT_CHARS_LOOKUP[c2]);
     }
 
     public char getAsciiEquivalentChar(char c) {
         if (!isValid(c))
             throw new IllegalArgumentException("unexpected char:" + c);
         return CHAR_TO_LETTER_LOOKUP[c].englishEquivalentChar();
     }
 
     public TurkicLetter getAsciEquivalentLetter(char c) {
         if (!isValid(c))
             throw new IllegalArgumentException("unexpected char:" + c);
         return ASCII_EQUIVALENT_LETTER_LOOKUP[getAphabeticIndex(c) - 1];
     }
 
     /**
      * checks if a character is part of TurkishAlphabet.
      * @param c character to check
      * @return true if it is part of the Turkish alphabet. false otherwise
      */
     public final boolean isValid(char c) {
         return c < MAX_CHAR_VALUE && VALID_CHAR_TABLE[c];
     }
 
 	public byte[] toByteIndexes(String s) {
 		byte[] indexes = new byte[s.length()];
 		for (int i=0; i<indexes.length; i++) {
 			indexes[i] = (byte)getAphabeticIndex(s.charAt(i));
 		}
 		return indexes;
 	}
 
 
 }
