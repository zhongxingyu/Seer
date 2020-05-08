 package ohtu.justinbiber.domain;
 
 import java.util.HashMap;
 import org.springframework.stereotype.Service;
 /**
  * Enables the use of special characters (ä,ö,å,...) in the program.
 * 
  */
 @Service
 public class Mappings {
   private HashMap<String, String> mappings = new HashMap<String, String>();
  
   public Mappings(){
      
   }
     /**
      * Replaces the special characters met in a string with a code
      * equivalent to them.
      * @param str
      * @return
      */
     public String fixMappings(String str) {
         str = str.replace("\"", "\\\"");
         str = str.replace("ä", "\\\"{a}");
         str = str.replace("Ä", "\\\"{A}");
         str = str.replace("ö", "\\\"{o}");
         str = str.replace("Ö", "\\\"{O}");
         str = str.replace("å", "\\aa");
         str = str.replace("Å", "\\AA");
         str = str.replace("$", "\\$");
         //str = str.replace("{", "\\{");
         return str;
     }
   /*
     mappings.put("\u00A0","~"); // NO-BREAK SPACE
     mappings.put("\u00A1","{\\textexclamdown}"); // INVERTED EXCLAMATION MARK
     mappings.put("\u00A2","{\\textcent}"); // CENT SIGN
     mappings.put("\u00A3","{\\textsterling}"); // POUND SIGN
     mappings.put("\u00A5","{\\textyen}"); // YEN SIGN
     mappings.put("\u00A6","{\\textbrokenbar}"); // BROKEN BAR
     mappings.put("\u00A7","{\\textsection}"); // SECTION SIGN
     mappings.put("\u00A8","{\\textasciidieresis}"); // DIAERESIS
     mappings.put("\u00A9","{\\textcopyright}"); // COPYRIGHT SIGN
     mappings.put("\u00AA","{\\textordfeminine}"); // FEMININE ORDINAL INDICATOR
     mappings.put("\u00AB","{\\guillemotleft}"); // LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
     mappings.put("\u00AC","{\\textlnot}"); // NOT SIGN
     mappings.put("\u00AD","-"); // SOFT HYPHEN
     mappings.put("\u00AE","{\\textregistered}"); // REGISTERED SIGN
     mappings.put("\u00AF","{\\textasciimacron}"); // MACRON
     mappings.put("\u00B0","{\\textdegree}"); // DEGREE SIGN
     mappings.put("\u00B1","{\\textpm}"); // PLUS-MINUS SIGN
     mappings.put("\u00B2","{\\texttwosuperior}"); // SUPERSCRIPT TWO
     mappings.put("\u00B3","{\\textthreesuperior}"); // SUPERSCRIPT THREE
     mappings.put("\u00B4","{\\textasciiacute}"); // ACUTE ACCENT
     mappings.put("\u00B5","{\\textmu}"); // MICRO SIGN
     mappings.put("\u00B6","{\\textparagraph}"); // PILCROW SIGN
     mappings.put("\u00B7","{\\textperiodcentered}"); // MIDDLE DOT
     mappings.put("\u00B8","{\\c\\ }"); // CEDILLA
     mappings.put("\u00B9","{\\textonesuperior}"); // SUPERSCRIPT ONE
     mappings.put("\u00BA","{\\textordmasculine}"); // MASCULINE ORDINAL INDICATOR
     mappings.put("\u00BB","{\\guillemotright}"); // RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
     mappings.put("\u00BC","{\\textonequarter}"); // VULGAR FRACTION ONE QUARTER
     mappings.put("\u00BD","{\\textonehalf}"); // VULGAR FRACTION ONE HALF
     mappings.put("\u00BE","{\\textthreequarters}"); // VULGAR FRACTION THREE QUARTERS
     mappings.put("\u00BF","{\\textquestiondown}"); // INVERTED QUESTION MARK
     mappings.put("\u00C6","{\\AE}"); // LATIN CAPITAL LETTER AE
     mappings.put("\u00D0","{\\DH}"); // LATIN CAPITAL LETTER ETH
     mappings.put("\u00D7","{\\texttimes}"); // MULTIPLICATION SIGN
     mappings.put("\u00DE","{\\TH}"); // LATIN CAPITAL LETTER THORN
     mappings.put("\u00DF","{\\ss}"); // LATIN SMALL LETTER SHARP S
     mappings.put("\u00E6","{\\ae}"); // LATIN SMALL LETTER AE
     mappings.put("\u00F0","{\\dh}"); // LATIN SMALL LETTER ETH
     mappings.put("\u00F7","{\\textdiv}"); // DIVISION SIGN
     mappings.put("\u00FE","{\\th}"); // LATIN SMALL LETTER THORN
     mappings.put("\u0131","{\\i}"); // LATIN SMALL LETTER DOTLESS I
     mappings.put("\u0132","IJ"); // LATIN CAPITAL LIGATURE IJ
     mappings.put("\u0133","ij"); // LATIN SMALL LIGATURE IJ
     mappings.put("\u0138","k"); // LATIN SMALL LETTER KRA
     mappings.put("\u0149","'n"); // LATIN SMALL LETTER N PRECEDED BY APOSTROPHE
     mappings.put("\u014A","{\\NG}"); // LATIN CAPITAL LETTER ENG
     mappings.put("\u014B","{\\ng}"); // LATIN SMALL LETTER ENG
     mappings.put("\u0152","{\\OE}"); // LATIN CAPITAL LIGATURE OE
     mappings.put("\u0153","{\\oe}"); // LATIN SMALL LIGATURE OE
     mappings.put("\u017F","s"); // LATIN SMALL LETTER LONG S
     mappings.put("\u02B9","'"); // MODIFIER LETTER PRIME
     mappings.put("\u02BB","'"); // MODIFIER LETTER TURNED COMMA
     mappings.put("\u02BC","'"); // MODIFIER LETTER APOSTROPHE
     mappings.put("\u02BD","'"); // MODIFIER LETTER REVERSED COMMA
     mappings.put("\u02C6","{\\textasciicircum}"); // MODIFIER LETTER CIRCUMFLEX ACCENT
     mappings.put("\u02C8","'"); // MODIFIER LETTER VERTICAL LINE
     mappings.put("\u02C9","-"); // MODIFIER LETTER MACRON
     mappings.put("\u02CC",","); // MODIFIER LETTER LOW VERTICAL LINE
     mappings.put("\u02D0",":"); // MODIFIER LETTER TRIANGULAR COLON
     mappings.put("\u02DA","o"); // RING ABOVE
     mappings.put("\u02DC","\\~{}"); // SMALL TILDE
     mappings.put("\u02DD","{\\textacutedbl}"); // DOUBLE ACUTE ACCENT
     mappings.put("\u0374","'"); // GREEK NUMERAL SIGN
     mappings.put("\u0375",","); // GREEK LOWER NUMERAL SIGN
     mappings.put("\u037E",";"); // GREEK QUESTION MARK
     mappings.put("\u2000"," "); // EN QUAD
     mappings.put("\u2001","  "); // EM QUAD
     mappings.put("\u2002"," "); // EN SPACE
     mappings.put("\u2003","  "); // EM SPACE
     mappings.put("\u2004"," "); // THREE-PER-EM SPACE
     mappings.put("\u2005"," "); // FOUR-PER-EM SPACE
     mappings.put("\u2006"," "); // SIX-PER-EM SPACE
     mappings.put("\u2007"," "); // FIGURE SPACE
     mappings.put("\u2008"," "); // PUNCTUATION SPACE
     mappings.put("\u2009"," "); // THIN SPACE
     mappings.put("\u2010","-"); // HYPHEN
     mappings.put("\u2011","-"); // NON-BREAKING HYPHEN
     mappings.put("\u2012","-"); // FIGURE DASH
     mappings.put("\u2013","{\\textendash}"); // EN DASH
     mappings.put("\u2014","{\\textemdash}"); // EM DASH
     mappings.put("\u2015","{\\textemdash}"); // HORIZONTAL BAR or QUOTATION DASH (not in LaTeX -- use EM DASH)
     mappings.put("\u2016","{\\textbardbl}"); // DOUBLE VERTICAL LINE
     mappings.put("\u2017","{\\textunderscore}"); // DOUBLE LOW LINE
     mappings.put("\u2018","{\\textquoteleft}"); // LEFT SINGLE QUOTATION MARK
     mappings.put("\u2019","{\\textquoteright}"); // RIGHT SINGLE QUOTATION MARK
     mappings.put("\u2018","`"); // LEFT SINGLE QUOTATION MARK
     mappings.put("\u2019","'"); // RIGHT SINGLE QUOTATION MARK
     mappings.put("\u201A","{\\quotesinglbase}"); // SINGLE LOW-9 QUOTATION MARK
     mappings.put("\u201B","'"); // SINGLE HIGH-REVERSED-9 QUOTATION MARK
     mappings.put("\u201C","{\\textquotedblleft}"); // LEFT DOUBLE QUOTATION MARK
     mappings.put("\u201D","{\\textquotedblright}"); // RIGHT DOUBLE QUOTATION MARK
     mappings.put("\u201E","{\\quotedblbase}"); // DOUBLE LOW-9 QUOTATION MARK
     mappings.put("\u201F","{\\quotedblbase}"); // DOUBLE HIGH-REVERSED-9 QUOTATION MARK
     mappings.put("\u2020","{\\textdagger}"); // DAGGER
     mappings.put("\u2021","{\\textdaggerdbl}"); // DOUBLE DAGGER
     mappings.put("\u2022","{\\textbullet}"); // BULLET
     mappings.put("\u2023",">"); // TRIANGULAR BULLET
     mappings.put("\u2024","."); // ONE DOT LEADER
     mappings.put("\u2025",".."); // TWO DOT LEADER
     mappings.put("\u2026","{\\textellipsis}"); // HORIZONTAL ELLIPSIS
     mappings.put("\u2027","-"); // HYPHENATION POINT
     mappings.put("\u202F"," "); // NARROW NO-BREAK SPACE
     mappings.put("\u2030","{\\textperthousand}"); // PER MILLE SIGN
     mappings.put("\u2032","'"); // PRIME
     mappings.put("\u2033","'"); // DOUBLE PRIME
     mappings.put("\u2034","'''"); // TRIPLE PRIME
     mappings.put("\u2035","`"); // REVERSED PRIME
     mappings.put("\u2036","``"); // REVERSED DOUBLE PRIME
     mappings.put("\u2037","```"); // REVERSED TRIPLE PRIME
     mappings.put("\u2039","{\\guilsinglleft}"); // SINGLE LEFT-POINTING ANGLE QUOTATION MARK
     mappings.put("\u203A","{\\guilsinglright}"); // SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
     mappings.put("\u203C","!!"); // DOUBLE EXCLAMATION MARK
     mappings.put("\u203E","-"); // OVERLINE
     mappings.put("\u2043","-"); // HYPHEN BULLET
     mappings.put("\u2044","{\\textfractionsolidus}"); // FRACTION SLASH
     mappings.put("\u2048","?!"); // QUESTION EXCLAMATION MARK
     mappings.put("\u2049","!?"); // EXCLAMATION QUESTION MARK
     mappings.put("\u204A","7"); // TIRONIAN SIGN ET
     mappings.put("\u2070","$^{0}$"); // SUPERSCRIPT ZERO
     mappings.put("\u2074","$^{4}$"); // SUPERSCRIPT FOUR
     mappings.put("\u2075","$^{5}$"); // SUPERSCRIPT FIVE
     mappings.put("\u2076","$^{6}$"); // SUPERSCRIPT SIX
     mappings.put("\u2077","$^{7}$"); // SUPERSCRIPT SEVEN
     mappings.put("\u2078","$^{8}$"); // SUPERSCRIPT EIGHT
     mappings.put("\u2079","$^{9}$"); // SUPERSCRIPT NINE
     mappings.put("\u207A","$^{+}$"); // SUPERSCRIPT PLUS SIGN
     mappings.put("\u207B","$^{-}$"); // SUPERSCRIPT MINUS
     mappings.put("\u207C","$^{=}$"); // SUPERSCRIPT EQUALS SIGN
     mappings.put("\u207D","$^{(}$"); // SUPERSCRIPT LEFT PARENTHESIS
     mappings.put("\u207E","$^{)}$"); // SUPERSCRIPT RIGHT PARENTHESIS
     mappings.put("\u207F","$^{n}$"); // SUPERSCRIPT LATIN SMALL LETTER N
     mappings.put("\u2080","$_{0}$"); // SUBSCRIPT ZERO
     mappings.put("\u2081","$_{1}$"); // SUBSCRIPT ONE
     mappings.put("\u2082","$_{2}$"); // SUBSCRIPT TWO
     mappings.put("\u2083","$_{3}$"); // SUBSCRIPT THREE
     mappings.put("\u2084","$_{4}$"); // SUBSCRIPT FOUR
     mappings.put("\u2085","$_{5}$"); // SUBSCRIPT FIVE
     mappings.put("\u2086","$_{6}$"); // SUBSCRIPT SIX
     mappings.put("\u2087","$_{7}$"); // SUBSCRIPT SEVEN
     mappings.put("\u2088","$_{8}$"); // SUBSCRIPT EIGHT
     mappings.put("\u2089","$_{9}$"); // SUBSCRIPT NINE
     mappings.put("\u208A","$_{+}$"); // SUBSCRIPT PLUS SIGN
     mappings.put("\u208B","$_{-}$"); // SUBSCRIPT MINUS
     mappings.put("\u208C","$_{=}$"); // SUBSCRIPT EQUALS SIGN
     mappings.put("\u208D","$_{(}$"); // SUBSCRIPT LEFT PARENTHESIS
     mappings.put("\u208E","$_{)}$"); // SUBSCRIPT RIGHT PARENTHESIS
     mappings.put("\u20AC","{\\texteuro}"); // EURO SIGN
     mappings.put("\u2100","a/c"); // ACCOUNT OF
     mappings.put("\u2101","a/s"); // ADDRESSED TO THE SUBJECT
     mappings.put("\u2103","{\\textcelsius}"); // DEGREE CELSIUS
     mappings.put("\u2105","c/o"); // CARE OF
     mappings.put("\u2106","c/u"); // CADA UNA
     mappings.put("\u2109","F"); // DEGREE FAHRENHEIT
     mappings.put("\u2113","l"); // SCRIPT SMALL L
     mappings.put("\u2116","{\\textnumero}"); // NUMERO SIGN
     mappings.put("\u2117","{\\textcircledP}"); // SOUND RECORDING COPYRIGHT
     mappings.put("\u2120","{\\textservicemark}"); // SERVICE MARK
     mappings.put("\u2121","TEL"); // TELEPHONE SIGN
     mappings.put("\u2122","{\\texttrademark}"); // TRADE MARK SIGN
     mappings.put("\u2126","{\\textohm}"); // OHM SIGN
     mappings.put("\u212A","K"); // KELVIN SIGN
     mappings.put("\u212B","A"); // ANGSTROM SIGN
     mappings.put("\u212E","{\\textestimated}"); // ESTIMATED SYMBOL
     mappings.put("\u2153"," 1/3"); // VULGAR FRACTION ONE THIRD
     mappings.put("\u2154"," 2/3"); // VULGAR FRACTION TWO THIRDS
     mappings.put("\u2155"," 1/5"); // VULGAR FRACTION ONE FIFTH
     mappings.put("\u2156"," 2/5"); // VULGAR FRACTION TWO FIFTHS
     mappings.put("\u2157"," 3/5"); // VULGAR FRACTION THREE FIFTHS
     mappings.put("\u2158"," 4/5"); // VULGAR FRACTION FOUR FIFTHS
     mappings.put("\u2159"," 1/6"); // VULGAR FRACTION ONE SIXTH
     mappings.put("\u215A"," 5/6"); // VULGAR FRACTION FIVE SIXTHS
     mappings.put("\u215B"," 1/8"); // VULGAR FRACTION ONE EIGHTH
     mappings.put("\u215C"," 3/8"); // VULGAR FRACTION THREE EIGHTHS
     mappings.put("\u215D"," 5/8"); // VULGAR FRACTION FIVE EIGHTHS
     mappings.put("\u215E"," 7/8"); // VULGAR FRACTION SEVEN EIGHTHS
     mappings.put("\u215F"," 1/"); // FRACTION NUMERATOR ONE
     mappings.put("\u2160","I"); // ROMAN NUMERAL ONE
     mappings.put("\u2161","II"); // ROMAN NUMERAL TWO
     mappings.put("\u2162","III"); // ROMAN NUMERAL THREE
     mappings.put("\u2163","IV"); // ROMAN NUMERAL FOUR
     mappings.put("\u2164","V"); // ROMAN NUMERAL FIVE
     mappings.put("\u2165","VI"); // ROMAN NUMERAL SIX
     mappings.put("\u2166","VII"); // ROMAN NUMERAL SEVEN
     mappings.put("\u2167","VIII"); // ROMAN NUMERAL EIGHT
     mappings.put("\u2168","IX"); // ROMAN NUMERAL NINE
     mappings.put("\u2169","X"); // ROMAN NUMERAL TEN
     mappings.put("\u216A","XI"); // ROMAN NUMERAL ELEVEN
     mappings.put("\u216B","XII"); // ROMAN NUMERAL TWELVE
     mappings.put("\u216C","L"); // ROMAN NUMERAL FIFTY
     mappings.put("\u216D","C"); // ROMAN NUMERAL ONE HUNDRED
     mappings.put("\u216E","D"); // ROMAN NUMERAL FIVE HUNDRED
     mappings.put("\u216F","M"); // ROMAN NUMERAL ONE THOUSAND
     mappings.put("\u2170","i"); // SMALL ROMAN NUMERAL ONE
     mappings.put("\u2171","ii"); // SMALL ROMAN NUMERAL TWO
     mappings.put("\u2172","iii"); // SMALL ROMAN NUMERAL THREE
     mappings.put("\u2173","iv"); // SMALL ROMAN NUMERAL FOUR
     mappings.put("\u2174","v"); // SMALL ROMAN NUMERAL FIVE
     mappings.put("\u2175","vi"); // SMALL ROMAN NUMERAL SIX
     mappings.put("\u2176","vii"); // SMALL ROMAN NUMERAL SEVEN
     mappings.put("\u2177","viii"); // SMALL ROMAN NUMERAL EIGHT
     mappings.put("\u2178","ix"); // SMALL ROMAN NUMERAL NINE
     mappings.put("\u2179","x"); // SMALL ROMAN NUMERAL TEN
     mappings.put("\u217A","xi"); // SMALL ROMAN NUMERAL ELEVEN
     mappings.put("\u217B","xii"); // SMALL ROMAN NUMERAL TWELVE
     mappings.put("\u217C","l"); // SMALL ROMAN NUMERAL FIFTY
     mappings.put("\u217D","c"); // SMALL ROMAN NUMERAL ONE HUNDRED
     mappings.put("\u217E","d"); // SMALL ROMAN NUMERAL FIVE HUNDRED
     mappings.put("\u217F","m"); // SMALL ROMAN NUMERAL ONE THOUSAND
     mappings.put("\u2190","{\\textleftarrow}"); // LEFTWARDS ARROW
     mappings.put("\u2191","{\\textuparrow}"); // UPWARDS ARROW
     mappings.put("\u2192","{\\textrightarrow}"); // RIGHTWARDS ARROW
     mappings.put("\u2193","{\\textdownarrow}"); // DOWNWARDS ARROW
     mappings.put("\u2194","<->"); // LEFT RIGHT ARROW
     mappings.put("\u21D0","<="); // LEFTWARDS DOUBLE ARROW
     mappings.put("\u21D2","=>"); // RIGHTWARDS DOUBLE ARROW
     mappings.put("\u21D4","<=>"); // LEFT RIGHT DOUBLE ARROW
     mappings.put("\u2212","-"); // MINUS SIGN
     mappings.put("\u2215","/"); // DIVISION SLASH
     mappings.put("\u2216","\\"); // SET MINUS
     mappings.put("\u2217","*"); // ASTERISK OPERATOR
     mappings.put("\u2218","o"); // RING OPERATOR
     mappings.put("\u2219","."); // BULLET OPERATOR
     mappings.put("\u221E","$\\infty$"); // INFINITY
     mappings.put("\u2223","|"); // DIVIDES
     mappings.put("\u2225","||"); // PARALLEL TO
     mappings.put("\u2236",":"); // RATIO
     mappings.put("\u223C","\\~{}"); // TILDE OPERATOR
     mappings.put("\u2260","/="); // NOT EQUAL TO
     mappings.put("\u2261","="); // IDENTICAL TO
     mappings.put("\u2264","<="); // LESS-THAN OR EQUAL TO
     mappings.put("\u2265",">="); // GREATER-THAN OR EQUAL TO
     mappings.put("\u226A","<<"); // MUCH LESS-THAN
     mappings.put("\u226B",">>"); // MUCH GREATER-THAN
     mappings.put("\u2295","(+)"); // CIRCLED PLUS
     mappings.put("\u2296","(-)"); // CIRCLED MINUS
     mappings.put("\u2297","(x)"); // CIRCLED TIMES
     mappings.put("\u2298","(/)"); // CIRCLED DIVISION SLASH
     mappings.put("\u22A2","|-"); // RIGHT TACK
     mappings.put("\u22A3","-|"); // LEFT TACK
     mappings.put("\u22A6","|-"); // ASSERTION
     mappings.put("\u22A7","|="); // MODELS
     mappings.put("\u22A8","|="); // TRUE
     mappings.put("\u22A9","||-"); // FORCES
     mappings.put("\u22C5","."); // DOT OPERATOR
     mappings.put("\u22C6","*"); // STAR OPERATOR
     mappings.put("\u22D5","$\\#$"); // EQUAL AND PARALLEL TO
     mappings.put("\u22D8","<<<"); // VERY MUCH LESS-THAN
     mappings.put("\u22D9",">>>"); // VERY MUCH GREATER-THAN
     mappings.put("\u2329","{\\textlangle}"); // LEFT-POINTING ANGLE BRACKET
     mappings.put("\u232A","{\\textrangle}"); // RIGHT-POINTING ANGLE BRACKET
     mappings.put("\u2400","NUL"); // SYMBOL FOR NULL
     mappings.put("\u2401","SOH"); // SYMBOL FOR START OF HEADING
     mappings.put("\u2402","STX"); // SYMBOL FOR START OF TEXT
     mappings.put("\u2403","ETX"); // SYMBOL FOR END OF TEXT
     mappings.put("\u2404","EOT"); // SYMBOL FOR END OF TRANSMISSION
     mappings.put("\u2405","ENQ"); // SYMBOL FOR ENQUIRY
     mappings.put("\u2406","ACK"); // SYMBOL FOR ACKNOWLEDGE
     mappings.put("\u2407","BEL"); // SYMBOL FOR BELL
     mappings.put("\u2408","BS"); // SYMBOL FOR BACKSPACE
     mappings.put("\u2409","HT"); // SYMBOL FOR HORIZONTAL TABULATION
     mappings.put("\u240A","LF"); // SYMBOL FOR LINE FEED
     mappings.put("\u240B","VT"); // SYMBOL FOR VERTICAL TABULATION
     mappings.put("\u240C","FF"); // SYMBOL FOR FORM FEED
     mappings.put("\u240D","CR"); // SYMBOL FOR CARRIAGE RETURN
     mappings.put("\u240E","SO"); // SYMBOL FOR SHIFT OUT
     mappings.put("\u240F","SI"); // SYMBOL FOR SHIFT IN
     mappings.put("\u2410","DLE"); // SYMBOL FOR DATA LINK ESCAPE
     mappings.put("\u2411","DC1"); // SYMBOL FOR DEVICE CONTROL ONE
     mappings.put("\u2412","DC2"); // SYMBOL FOR DEVICE CONTROL TWO
     mappings.put("\u2413","DC3"); // SYMBOL FOR DEVICE CONTROL THREE
     mappings.put("\u2414","DC4"); // SYMBOL FOR DEVICE CONTROL FOUR
     mappings.put("\u2415","NAK"); // SYMBOL FOR NEGATIVE ACKNOWLEDGE
     mappings.put("\u2416","SYN"); // SYMBOL FOR SYNCHRONOUS IDLE
     mappings.put("\u2417","ETB"); // SYMBOL FOR END OF TRANSMISSION BLOCK
     mappings.put("\u2418","CAN"); // SYMBOL FOR CANCEL
     mappings.put("\u2419","EM"); // SYMBOL FOR END OF MEDIUM
     mappings.put("\u241A","SUB"); // SYMBOL FOR SUBSTITUTE
     mappings.put("\u241B","ESC"); // SYMBOL FOR ESCAPE
     mappings.put("\u241C","FS"); // SYMBOL FOR FILE SEPARATOR
     mappings.put("\u241D","GS"); // SYMBOL FOR GROUP SEPARATOR
     mappings.put("\u241E","RS"); // SYMBOL FOR RECORD SEPARATOR
     mappings.put("\u241F","US"); // SYMBOL FOR UNIT SEPARATOR
     mappings.put("\u2420","SP"); // SYMBOL FOR SPACE
     mappings.put("\u2421","DEL"); // SYMBOL FOR DELETE
     mappings.put("\u2423","{\\textvisiblespace}"); // OPEN BOX
     mappings.put("\u2424","NL"); // SYMBOL FOR NEWLINE
     mappings.put("\u2425","///"); // SYMBOL FOR DELETE FORM TWO
     mappings.put("\u2426","?"); // SYMBOL FOR SUBSTITUTE FORM TWO
     mappings.put("\u2460","(1)"); // CIRCLED DIGIT ONE
     mappings.put("\u2461","(2)"); // CIRCLED DIGIT TWO
     mappings.put("\u2462","(3)"); // CIRCLED DIGIT THREE
     mappings.put("\u2463","(4)"); // CIRCLED DIGIT FOUR
     mappings.put("\u2464","(5)"); // CIRCLED DIGIT FIVE
     mappings.put("\u2465","(6)"); // CIRCLED DIGIT SIX
     mappings.put("\u2466","(7)"); // CIRCLED DIGIT SEVEN
     mappings.put("\u2467","(8)"); // CIRCLED DIGIT EIGHT
     mappings.put("\u2468","(9)"); // CIRCLED DIGIT NINE
     mappings.put("\u2469","(10)"); // CIRCLED NUMBER TEN
     mappings.put("\u246A","(11)"); // CIRCLED NUMBER ELEVEN
     mappings.put("\u246B","(12)"); // CIRCLED NUMBER TWELVE
     mappings.put("\u246C","(13)"); // CIRCLED NUMBER THIRTEEN
     mappings.put("\u246D","(14)"); // CIRCLED NUMBER FOURTEEN
     mappings.put("\u246E","(15)"); // CIRCLED NUMBER FIFTEEN
     mappings.put("\u246F","(16)"); // CIRCLED NUMBER SIXTEEN
     mappings.put("\u2470","(17)"); // CIRCLED NUMBER SEVENTEEN
     mappings.put("\u2471","(18)"); // CIRCLED NUMBER EIGHTEEN
     mappings.put("\u2472","(19)"); // CIRCLED NUMBER NINETEEN
     mappings.put("\u2473","(20)"); // CIRCLED NUMBER TWENTY
     mappings.put("\u2474","(1)"); // PARENTHESIZED DIGIT ONE
     mappings.put("\u2475","(2)"); // PARENTHESIZED DIGIT TWO
     mappings.put("\u2476","(3)"); // PARENTHESIZED DIGIT THREE
     mappings.put("\u2477","(4)"); // PARENTHESIZED DIGIT FOUR
     mappings.put("\u2478","(5)"); // PARENTHESIZED DIGIT FIVE
     mappings.put("\u2479","(6)"); // PARENTHESIZED DIGIT SIX
     mappings.put("\u247A","(7)"); // PARENTHESIZED DIGIT SEVEN
     mappings.put("\u247B","(8)"); // PARENTHESIZED DIGIT EIGHT
     mappings.put("\u247C","(9)"); // PARENTHESIZED DIGIT NINE
     mappings.put("\u247D","(10)"); // PARENTHESIZED NUMBER TEN
     mappings.put("\u247E","(11)"); // PARENTHESIZED NUMBER ELEVEN
     mappings.put("\u247F","(12)"); // PARENTHESIZED NUMBER TWELVE
     mappings.put("\u2480","(13)"); // PARENTHESIZED NUMBER THIRTEEN
     mappings.put("\u2481","(14)"); // PARENTHESIZED NUMBER FOURTEEN
     mappings.put("\u2482","(15)"); // PARENTHESIZED NUMBER FIFTEEN
     mappings.put("\u2483","(16)"); // PARENTHESIZED NUMBER SIXTEEN
     mappings.put("\u2484","(17)"); // PARENTHESIZED NUMBER SEVENTEEN
     mappings.put("\u2485","(18)"); // PARENTHESIZED NUMBER EIGHTEEN
     mappings.put("\u2486","(19)"); // PARENTHESIZED NUMBER NINETEEN
     mappings.put("\u2487","(20)"); // PARENTHESIZED NUMBER TWENTY
     mappings.put("\u2488","1."); // DIGIT ONE FULL STOP
     mappings.put("\u2489","2."); // DIGIT TWO FULL STOP
     mappings.put("\u248A","3."); // DIGIT THREE FULL STOP
     mappings.put("\u248B","4."); // DIGIT FOUR FULL STOP
     mappings.put("\u248C","5."); // DIGIT FIVE FULL STOP
     mappings.put("\u248D","6."); // DIGIT SIX FULL STOP
     mappings.put("\u248E","7."); // DIGIT SEVEN FULL STOP
     mappings.put("\u248F","8."); // DIGIT EIGHT FULL STOP
     mappings.put("\u2490","9."); // DIGIT NINE FULL STOP
     mappings.put("\u2491","10."); // NUMBER TEN FULL STOP
     mappings.put("\u2492","11."); // NUMBER ELEVEN FULL STOP
     mappings.put("\u2493","12."); // NUMBER TWELVE FULL STOP
     mappings.put("\u2494","13."); // NUMBER THIRTEEN FULL STOP
     mappings.put("\u2495","14."); // NUMBER FOURTEEN FULL STOP
     mappings.put("\u2496","15."); // NUMBER FIFTEEN FULL STOP
     mappings.put("\u2497","16."); // NUMBER SIXTEEN FULL STOP
     mappings.put("\u2498","17."); // NUMBER SEVENTEEN FULL STOP
     mappings.put("\u2499","18."); // NUMBER EIGHTEEN FULL STOP
     mappings.put("\u249A","19."); // NUMBER NINETEEN FULL STOP
     mappings.put("\u249B","20."); // NUMBER TWENTY FULL STOP
     mappings.put("\u249C","(a)"); // PARENTHESIZED LATIN SMALL LETTER A
     mappings.put("\u249D","(b)"); // PARENTHESIZED LATIN SMALL LETTER B
     mappings.put("\u249E","(c)"); // PARENTHESIZED LATIN SMALL LETTER C
     mappings.put("\u249F","(d)"); // PARENTHESIZED LATIN SMALL LETTER D
     mappings.put("\u24A0","(e)"); // PARENTHESIZED LATIN SMALL LETTER E
     mappings.put("\u24A1","(f)"); // PARENTHESIZED LATIN SMALL LETTER F
     mappings.put("\u24A2","(g)"); // PARENTHESIZED LATIN SMALL LETTER G
     mappings.put("\u24A3","(h)"); // PARENTHESIZED LATIN SMALL LETTER H
     mappings.put("\u24A4","(i)"); // PARENTHESIZED LATIN SMALL LETTER I
     mappings.put("\u24A5","(j)"); // PARENTHESIZED LATIN SMALL LETTER J
     mappings.put("\u24A6","(k)"); // PARENTHESIZED LATIN SMALL LETTER K
     mappings.put("\u24A7","(l)"); // PARENTHESIZED LATIN SMALL LETTER L
     mappings.put("\u24A8","(m)"); // PARENTHESIZED LATIN SMALL LETTER M
     mappings.put("\u24A9","(n)"); // PARENTHESIZED LATIN SMALL LETTER N
     mappings.put("\u24AA","(o)"); // PARENTHESIZED LATIN SMALL LETTER O
     mappings.put("\u24AB","(p)"); // PARENTHESIZED LATIN SMALL LETTER P
     mappings.put("\u24AC","(q)"); // PARENTHESIZED LATIN SMALL LETTER Q
     mappings.put("\u24AD","(r)"); // PARENTHESIZED LATIN SMALL LETTER R
     mappings.put("\u24AE","(s)"); // PARENTHESIZED LATIN SMALL LETTER S
     mappings.put("\u24AF","(t)"); // PARENTHESIZED LATIN SMALL LETTER T
     mappings.put("\u24B0","(u)"); // PARENTHESIZED LATIN SMALL LETTER U
     mappings.put("\u24B1","(v)"); // PARENTHESIZED LATIN SMALL LETTER V
     mappings.put("\u24B2","(w)"); // PARENTHESIZED LATIN SMALL LETTER W
     mappings.put("\u24B3","(x)"); // PARENTHESIZED LATIN SMALL LETTER X
     mappings.put("\u24B4","(y)"); // PARENTHESIZED LATIN SMALL LETTER Y
     mappings.put("\u24B5","(z)"); // PARENTHESIZED LATIN SMALL LETTER Z
     mappings.put("\u24B6","(A)"); // CIRCLED LATIN CAPITAL LETTER A
     mappings.put("\u24B7","(B)"); // CIRCLED LATIN CAPITAL LETTER B
     mappings.put("\u24B8","(C)"); // CIRCLED LATIN CAPITAL LETTER C
     mappings.put("\u24B9","(D)"); // CIRCLED LATIN CAPITAL LETTER D
     mappings.put("\u24BA","(E)"); // CIRCLED LATIN CAPITAL LETTER E
     mappings.put("\u24BB","(F)"); // CIRCLED LATIN CAPITAL LETTER F
     mappings.put("\u24BC","(G)"); // CIRCLED LATIN CAPITAL LETTER G
     mappings.put("\u24BD","(H)"); // CIRCLED LATIN CAPITAL LETTER H
     mappings.put("\u24BE","(I)"); // CIRCLED LATIN CAPITAL LETTER I
     mappings.put("\u24BF","(J)"); // CIRCLED LATIN CAPITAL LETTER J
     mappings.put("\u24C0","(K)"); // CIRCLED LATIN CAPITAL LETTER K
     mappings.put("\u24C1","(L)"); // CIRCLED LATIN CAPITAL LETTER L
     mappings.put("\u24C2","(M)"); // CIRCLED LATIN CAPITAL LETTER M
     mappings.put("\u24C3","(N)"); // CIRCLED LATIN CAPITAL LETTER N
     mappings.put("\u24C4","(O)"); // CIRCLED LATIN CAPITAL LETTER O
     mappings.put("\u24C5","(P)"); // CIRCLED LATIN CAPITAL LETTER P
     mappings.put("\u24C6","(Q)"); // CIRCLED LATIN CAPITAL LETTER Q
     mappings.put("\u24C7","(R)"); // CIRCLED LATIN CAPITAL LETTER R
     mappings.put("\u24C8","(S)"); // CIRCLED LATIN CAPITAL LETTER S
     mappings.put("\u24C9","(T)"); // CIRCLED LATIN CAPITAL LETTER T
     mappings.put("\u24CA","(U)"); // CIRCLED LATIN CAPITAL LETTER U
     mappings.put("\u24CB","(V)"); // CIRCLED LATIN CAPITAL LETTER V
     mappings.put("\u24CC","(W)"); // CIRCLED LATIN CAPITAL LETTER W
     mappings.put("\u24CD","(X)"); // CIRCLED LATIN CAPITAL LETTER X
     mappings.put("\u24CE","(Y)"); // CIRCLED LATIN CAPITAL LETTER Y
     mappings.put("\u24CF","(Z)"); // CIRCLED LATIN CAPITAL LETTER Z
     mappings.put("\u24D0","(a)"); // CIRCLED LATIN SMALL LETTER A
     mappings.put("\u24D1","(b)"); // CIRCLED LATIN SMALL LETTER B
     mappings.put("\u24D2","(c)"); // CIRCLED LATIN SMALL LETTER C
     mappings.put("\u24D3","(d)"); // CIRCLED LATIN SMALL LETTER D
     mappings.put("\u24D4","(e)"); // CIRCLED LATIN SMALL LETTER E
     mappings.put("\u24D5","(f)"); // CIRCLED LATIN SMALL LETTER F
     mappings.put("\u24D6","(g)"); // CIRCLED LATIN SMALL LETTER G
     mappings.put("\u24D7","(h)"); // CIRCLED LATIN SMALL LETTER H
     mappings.put("\u24D8","(i)"); // CIRCLED LATIN SMALL LETTER I
     mappings.put("\u24D9","(j)"); // CIRCLED LATIN SMALL LETTER J
     mappings.put("\u24DA","(k)"); // CIRCLED LATIN SMALL LETTER K
     mappings.put("\u24DB","(l)"); // CIRCLED LATIN SMALL LETTER L
     mappings.put("\u24DC","(m)"); // CIRCLED LATIN SMALL LETTER M
     mappings.put("\u24DD","(n)"); // CIRCLED LATIN SMALL LETTER N
     mappings.put("\u24DE","(o)"); // CIRCLED LATIN SMALL LETTER O
     mappings.put("\u24DF","(p)"); // CIRCLED LATIN SMALL LETTER P
     mappings.put("\u24E0","(q)"); // CIRCLED LATIN SMALL LETTER Q
     mappings.put("\u24E1","(r)"); // CIRCLED LATIN SMALL LETTER R
     mappings.put("\u24E2","(s)"); // CIRCLED LATIN SMALL LETTER S
     mappings.put("\u24E3","(t)"); // CIRCLED LATIN SMALL LETTER T
     mappings.put("\u24E4","(u)"); // CIRCLED LATIN SMALL LETTER U
     mappings.put("\u24E5","(v)"); // CIRCLED LATIN SMALL LETTER V
     mappings.put("\u24E6","(w)"); // CIRCLED LATIN SMALL LETTER W
     mappings.put("\u24E7","(x)"); // CIRCLED LATIN SMALL LETTER X
     mappings.put("\u24E8","(y)"); // CIRCLED LATIN SMALL LETTER Y
     mappings.put("\u24E9","(z)"); // CIRCLED LATIN SMALL LETTER Z
     mappings.put("\u24EA","(0)"); // CIRCLED DIGIT ZERO
     mappings.put("\u2500","-"); // BOX DRAWINGS LIGHT HORIZONTAL
     mappings.put("\u2501","="); // BOX DRAWINGS HEAVY HORIZONTAL
     mappings.put("\u2502","|"); // BOX DRAWINGS LIGHT VERTICAL
     mappings.put("\u2503","|"); // BOX DRAWINGS HEAVY VERTICAL
     mappings.put("\u2504","-"); // BOX DRAWINGS LIGHT TRIPLE DASH HORIZONTAL
     mappings.put("\u2505","="); // BOX DRAWINGS HEAVY TRIPLE DASH HORIZONTAL
     mappings.put("\u2506","|"); // BOX DRAWINGS LIGHT TRIPLE DASH VERTICAL
     mappings.put("\u2507","|"); // BOX DRAWINGS HEAVY TRIPLE DASH VERTICAL
     mappings.put("\u2508","-"); // BOX DRAWINGS LIGHT QUADRUPLE DASH HORIZONTAL
     mappings.put("\u2509","="); // BOX DRAWINGS HEAVY QUADRUPLE DASH HORIZONTAL
     mappings.put("\u250A","|"); // BOX DRAWINGS LIGHT QUADRUPLE DASH VERTICAL
     mappings.put("\u250B","|"); // BOX DRAWINGS HEAVY QUADRUPLE DASH VERTICAL
     mappings.put("\u250C","+"); // BOX DRAWINGS LIGHT DOWN AND RIGHT
     mappings.put("\u250D","+"); // BOX DRAWINGS DOWN LIGHT AND RIGHT HEAVY
     mappings.put("\u250E","+"); // BOX DRAWINGS DOWN HEAVY AND RIGHT LIGHT
     mappings.put("\u250F","+"); // BOX DRAWINGS HEAVY DOWN AND RIGHT
     mappings.put("\u2510","+"); // BOX DRAWINGS LIGHT DOWN AND LEFT
     mappings.put("\u2511","+"); // BOX DRAWINGS DOWN LIGHT AND LEFT HEAVY
     mappings.put("\u2512","+"); // BOX DRAWINGS DOWN HEAVY AND LEFT LIGHT
     mappings.put("\u2513","+"); // BOX DRAWINGS HEAVY DOWN AND LEFT
     mappings.put("\u2514","+"); // BOX DRAWINGS LIGHT UP AND RIGHT
     mappings.put("\u2515","+"); // BOX DRAWINGS UP LIGHT AND RIGHT HEAVY
     mappings.put("\u2516","+"); // BOX DRAWINGS UP HEAVY AND RIGHT LIGHT
     mappings.put("\u2517","+"); // BOX DRAWINGS HEAVY UP AND RIGHT
     mappings.put("\u2518","+"); // BOX DRAWINGS LIGHT UP AND LEFT
     mappings.put("\u2519","+"); // BOX DRAWINGS UP LIGHT AND LEFT HEAVY
     mappings.put("\u251A","+"); // BOX DRAWINGS UP HEAVY AND LEFT LIGHT
     mappings.put("\u251B","+"); // BOX DRAWINGS HEAVY UP AND LEFT
     mappings.put("\u251C","+"); // BOX DRAWINGS LIGHT VERTICAL AND RIGHT
     mappings.put("\u251D","+"); // BOX DRAWINGS VERTICAL LIGHT AND RIGHT HEAVY
     mappings.put("\u251E","+"); // BOX DRAWINGS UP HEAVY AND RIGHT DOWN LIGHT
     mappings.put("\u251F","+"); // BOX DRAWINGS DOWN HEAVY AND RIGHT UP LIGHT
     mappings.put("\u2520","+"); // BOX DRAWINGS VERTICAL HEAVY AND RIGHT LIGHT
     mappings.put("\u2521","+"); // BOX DRAWINGS DOWN LIGHT AND RIGHT UP HEAVY
     mappings.put("\u2522","+"); // BOX DRAWINGS UP LIGHT AND RIGHT DOWN HEAVY
     mappings.put("\u2523","+"); // BOX DRAWINGS HEAVY VERTICAL AND RIGHT
     mappings.put("\u2524","+"); // BOX DRAWINGS LIGHT VERTICAL AND LEFT
     mappings.put("\u2525","+"); // BOX DRAWINGS VERTICAL LIGHT AND LEFT HEAVY
     mappings.put("\u2526","+"); // BOX DRAWINGS UP HEAVY AND LEFT DOWN LIGHT
     mappings.put("\u2527","+"); // BOX DRAWINGS DOWN HEAVY AND LEFT UP LIGHT
     mappings.put("\u2528","+"); // BOX DRAWINGS VERTICAL HEAVY AND LEFT LIGHT
     mappings.put("\u2529","+"); // BOX DRAWINGS DOWN LIGHT AND LEFT UP HEAVY
     mappings.put("\u252A","+"); // BOX DRAWINGS UP LIGHT AND LEFT DOWN HEAVY
     mappings.put("\u252B","+"); // BOX DRAWINGS HEAVY VERTICAL AND LEFT
     mappings.put("\u252C","+"); // BOX DRAWINGS LIGHT DOWN AND HORIZONTAL
     mappings.put("\u252D","+"); // BOX DRAWINGS LEFT HEAVY AND RIGHT DOWN LIGHT
     mappings.put("\u252E","+"); // BOX DRAWINGS RIGHT HEAVY AND LEFT DOWN LIGHT
     mappings.put("\u252F","+"); // BOX DRAWINGS DOWN LIGHT AND HORIZONTAL HEAVY
     mappings.put("\u2530","+"); // BOX DRAWINGS DOWN HEAVY AND HORIZONTAL LIGHT
     mappings.put("\u2531","+"); // BOX DRAWINGS RIGHT LIGHT AND LEFT DOWN HEAVY
     mappings.put("\u2532","+"); // BOX DRAWINGS LEFT LIGHT AND RIGHT DOWN HEAVY
     mappings.put("\u2533","+"); // BOX DRAWINGS HEAVY DOWN AND HORIZONTAL
     mappings.put("\u2534","+"); // BOX DRAWINGS LIGHT UP AND HORIZONTAL
     mappings.put("\u2535","+"); // BOX DRAWINGS LEFT HEAVY AND RIGHT UP LIGHT
     mappings.put("\u2536","+"); // BOX DRAWINGS RIGHT HEAVY AND LEFT UP LIGHT
     mappings.put("\u2537","+"); // BOX DRAWINGS UP LIGHT AND HORIZONTAL HEAVY
     mappings.put("\u2538","+"); // BOX DRAWINGS UP HEAVY AND HORIZONTAL LIGHT
     mappings.put("\u2539","+"); // BOX DRAWINGS RIGHT LIGHT AND LEFT UP HEAVY
     mappings.put("\u253A","+"); // BOX DRAWINGS LEFT LIGHT AND RIGHT UP HEAVY
     mappings.put("\u253B","+"); // BOX DRAWINGS HEAVY UP AND HORIZONTAL
     mappings.put("\u253C","+"); // BOX DRAWINGS LIGHT VERTICAL AND HORIZONTAL
     mappings.put("\u253D","+"); // BOX DRAWINGS LEFT HEAVY AND RIGHT VERTICAL LIGHT
     mappings.put("\u253E","+"); // BOX DRAWINGS RIGHT HEAVY AND LEFT VERTICAL LIGHT
     mappings.put("\u253F","+"); // BOX DRAWINGS VERTICAL LIGHT AND HORIZONTAL HEAVY
     mappings.put("\u2540","+"); // BOX DRAWINGS UP HEAVY AND DOWN HORIZONTAL LIGHT
     mappings.put("\u2541","+"); // BOX DRAWINGS DOWN HEAVY AND UP HORIZONTAL LIGHT
     mappings.put("\u2542","+"); // BOX DRAWINGS VERTICAL HEAVY AND HORIZONTAL LIGHT
     mappings.put("\u2543","+"); // BOX DRAWINGS LEFT UP HEAVY AND RIGHT DOWN LIGHT
     mappings.put("\u2544","+"); // BOX DRAWINGS RIGHT UP HEAVY AND LEFT DOWN LIGHT
     mappings.put("\u2545","+"); // BOX DRAWINGS LEFT DOWN HEAVY AND RIGHT UP LIGHT
     mappings.put("\u2546","+"); // BOX DRAWINGS RIGHT DOWN HEAVY AND LEFT UP LIGHT
     mappings.put("\u2547","+"); // BOX DRAWINGS DOWN LIGHT AND UP HORIZONTAL HEAVY
     mappings.put("\u2548","+"); // BOX DRAWINGS UP LIGHT AND DOWN HORIZONTAL HEAVY
     mappings.put("\u2549","+"); // BOX DRAWINGS RIGHT LIGHT AND LEFT VERTICAL HEAVY
     mappings.put("\u254A","+"); // BOX DRAWINGS LEFT LIGHT AND RIGHT VERTICAL HEAVY
     mappings.put("\u254B","+"); // BOX DRAWINGS HEAVY VERTICAL AND HORIZONTAL
     mappings.put("\u254C","-"); // BOX DRAWINGS LIGHT DOUBLE DASH HORIZONTAL
     mappings.put("\u254D","="); // BOX DRAWINGS HEAVY DOUBLE DASH HORIZONTAL
     mappings.put("\u254E","|"); // BOX DRAWINGS LIGHT DOUBLE DASH VERTICAL
     mappings.put("\u254F","|"); // BOX DRAWINGS HEAVY DOUBLE DASH VERTICAL
     mappings.put("\u2550","="); // BOX DRAWINGS DOUBLE HORIZONTAL
     mappings.put("\u2551","|"); // BOX DRAWINGS DOUBLE VERTICAL
     mappings.put("\u2552","+"); // BOX DRAWINGS DOWN SINGLE AND RIGHT DOUBLE
     mappings.put("\u2553","+"); // BOX DRAWINGS DOWN DOUBLE AND RIGHT SINGLE
     mappings.put("\u2554","+"); // BOX DRAWINGS DOUBLE DOWN AND RIGHT
     mappings.put("\u2555","+"); // BOX DRAWINGS DOWN SINGLE AND LEFT DOUBLE
     mappings.put("\u2556","+"); // BOX DRAWINGS DOWN DOUBLE AND LEFT SINGLE
     mappings.put("\u2557","+"); // BOX DRAWINGS DOUBLE DOWN AND LEFT
     mappings.put("\u2558","+"); // BOX DRAWINGS UP SINGLE AND RIGHT DOUBLE
     mappings.put("\u2559","+"); // BOX DRAWINGS UP DOUBLE AND RIGHT SINGLE
     mappings.put("\u255A","+"); // BOX DRAWINGS DOUBLE UP AND RIGHT
     mappings.put("\u255B","+"); // BOX DRAWINGS UP SINGLE AND LEFT DOUBLE
     mappings.put("\u255C","+"); // BOX DRAWINGS UP DOUBLE AND LEFT SINGLE
     mappings.put("\u255D","+"); // BOX DRAWINGS DOUBLE UP AND LEFT
     mappings.put("\u255E","+"); // BOX DRAWINGS VERTICAL SINGLE AND RIGHT DOUBLE
     mappings.put("\u255F","+"); // BOX DRAWINGS VERTICAL DOUBLE AND RIGHT SINGLE
     mappings.put("\u2560","+"); // BOX DRAWINGS DOUBLE VERTICAL AND RIGHT
     mappings.put("\u2561","+"); // BOX DRAWINGS VERTICAL SINGLE AND LEFT DOUBLE
     mappings.put("\u2562","+"); // BOX DRAWINGS VERTICAL DOUBLE AND LEFT SINGLE
     mappings.put("\u2563","+"); // BOX DRAWINGS DOUBLE VERTICAL AND LEFT
     mappings.put("\u2564","+"); // BOX DRAWINGS DOWN SINGLE AND HORIZONTAL DOUBLE
     mappings.put("\u2565","+"); // BOX DRAWINGS DOWN DOUBLE AND HORIZONTAL SINGLE
     mappings.put("\u2566","+"); // BOX DRAWINGS DOUBLE DOWN AND HORIZONTAL
     mappings.put("\u2567","+"); // BOX DRAWINGS UP SINGLE AND HORIZONTAL DOUBLE
     mappings.put("\u2568","+"); // BOX DRAWINGS UP DOUBLE AND HORIZONTAL SINGLE
     mappings.put("\u2569","+"); // BOX DRAWINGS DOUBLE UP AND HORIZONTAL
     mappings.put("\u256A","+"); // BOX DRAWINGS VERTICAL SINGLE AND HORIZONTAL DOUBLE
     mappings.put("\u256B","+"); // BOX DRAWINGS VERTICAL DOUBLE AND HORIZONTAL SINGLE
     mappings.put("\u256C","+"); // BOX DRAWINGS DOUBLE VERTICAL AND HORIZONTAL
     mappings.put("\u256D","+"); // BOX DRAWINGS LIGHT ARC DOWN AND RIGHT
     mappings.put("\u256E","+"); // BOX DRAWINGS LIGHT ARC DOWN AND LEFT
     mappings.put("\u256F","+"); // BOX DRAWINGS LIGHT ARC UP AND LEFT
     mappings.put("\u2570","+"); // BOX DRAWINGS LIGHT ARC UP AND RIGHT
     mappings.put("\u2571","/"); // BOX DRAWINGS LIGHT DIAGONAL UPPER RIGHT TO LOWER LEFT
     mappings.put("\u2572","\\"); // BOX DRAWINGS LIGHT DIAGONAL UPPER LEFT TO LOWER RIGHT
     mappings.put("\u2573","X"); // BOX DRAWINGS LIGHT DIAGONAL CROSS
     mappings.put("\u257C","-"); // BOX DRAWINGS LIGHT LEFT AND HEAVY RIGHT
     mappings.put("\u257D","|"); // BOX DRAWINGS LIGHT UP AND HEAVY DOWN
     mappings.put("\u257E","-"); // BOX DRAWINGS HEAVY LEFT AND LIGHT RIGHT
     mappings.put("\u257F","|"); // BOX DRAWINGS HEAVY UP AND LIGHT DOWN
     mappings.put("\u25CB","o"); // WHITE CIRCLE
     mappings.put("\u25E6","{\\textopenbullet}"); // WHITE BULLET
     mappings.put("\u2605","*"); // BLACK STAR
     mappings.put("\u2606","*"); // WHITE STAR
     mappings.put("\u2612","X"); // BALLOT BOX WITH X
     mappings.put("\u2613","X"); // SALTIRE
     mappings.put("\u2639",":-("); // WHITE FROWNING FACE
     mappings.put("\u263A",":-)"); // WHITE SMILING FACE
     mappings.put("\u263B","(-:"); // BLACK SMILING FACE
     mappings.put("\u266D","b"); // MUSIC FLAT SIGN
     mappings.put("\u266F","$\\#$"); // MUSIC SHARP SIGN
     mappings.put("\u2701","$\\%<$"); // UPPER BLADE SCISSORS
     mappings.put("\u2702","$\\%<$"); // BLACK SCISSORS
     mappings.put("\u2703","$\\%<$"); // LOWER BLADE SCISSORS
     mappings.put("\u2704","$\\%<$"); // WHITE SCISSORS
     mappings.put("\u270C","V"); // VICTORY HAND
     mappings.put("\u2713","v"); // CHECK MARK
     mappings.put("\u2714","V"); // HEAVY CHECK MARK
     mappings.put("\u2715","x"); // MULTIPLICATION X
     mappings.put("\u2716","x"); // HEAVY MULTIPLICATION X
     mappings.put("\u2717","X"); // BALLOT X
     mappings.put("\u2718","X"); // HEAVY BALLOT X
     mappings.put("\u2719","+"); // OUTLINED GREEK CROSS
     mappings.put("\u271A","+"); // HEAVY GREEK CROSS
     mappings.put("\u271B","+"); // OPEN CENTRE CROSS
     mappings.put("\u271C","+"); // HEAVY OPEN CENTRE CROSS
     mappings.put("\u271D","+"); // LATIN CROSS
     mappings.put("\u271E","+"); // SHADOWED WHITE LATIN CROSS
     mappings.put("\u271F","+"); // OUTLINED LATIN CROSS
     mappings.put("\u2720","+"); // MALTESE CROSS
     mappings.put("\u2721","*"); // STAR OF DAVID
     mappings.put("\u2722","+"); // FOUR TEARDROP-SPOKED ASTERISK
     mappings.put("\u2723","+"); // FOUR BALLOON-SPOKED ASTERISK
     mappings.put("\u2724","+"); // HEAVY FOUR BALLOON-SPOKED ASTERISK
     mappings.put("\u2725","+"); // FOUR CLUB-SPOKED ASTERISK
     mappings.put("\u2726","+"); // BLACK FOUR POINTED STAR
     mappings.put("\u2727","+"); // WHITE FOUR POINTED STAR
     mappings.put("\u2729","*"); // STRESS OUTLINED WHITE STAR
     mappings.put("\u272A","*"); // CIRCLED WHITE STAR
     mappings.put("\u272B","*"); // OPEN CENTRE BLACK STAR
     mappings.put("\u272C","*"); // BLACK CENTRE WHITE STAR
     mappings.put("\u272D","*"); // OUTLINED BLACK STAR
     mappings.put("\u272E","*"); // HEAVY OUTLINED BLACK STAR
     mappings.put("\u272F","*"); // PINWHEEL STAR
     mappings.put("\u2730","*"); // SHADOWED WHITE STAR
     mappings.put("\u2731","*"); // HEAVY ASTERISK
     mappings.put("\u2732","*"); // OPEN CENTRE ASTERISK
     mappings.put("\u2733","*"); // EIGHT SPOKED ASTERISK
     mappings.put("\u2734","*"); // EIGHT POINTED BLACK STAR
     mappings.put("\u2735","*"); // EIGHT POINTED PINWHEEL STAR
     mappings.put("\u2736","*"); // SIX POINTED BLACK STAR
     mappings.put("\u2737","*"); // EIGHT POINTED RECTILINEAR BLACK STAR
     mappings.put("\u2738","*"); // HEAVY EIGHT POINTED RECTILINEAR BLACK STAR
     mappings.put("\u2739","*"); // TWELVE POINTED BLACK STAR
     mappings.put("\u273A","*"); // SIXTEEN POINTED ASTERISK
     mappings.put("\u273B","*"); // TEARDROP-SPOKED ASTERISK
     mappings.put("\u273C","*"); // OPEN CENTRE TEARDROP-SPOKED ASTERISK
     mappings.put("\u273D","*"); // HEAVY TEARDROP-SPOKED ASTERISK
     mappings.put("\u273E","*"); // SIX PETALLED BLACK AND WHITE FLORETTE
     mappings.put("\u273F","*"); // BLACK FLORETTE
     mappings.put("\u2740","*"); // WHITE FLORETTE
     mappings.put("\u2741","*"); // EIGHT PETALLED OUTLINED BLACK FLORETTE
     mappings.put("\u2742","*"); // CIRCLED OPEN CENTRE EIGHT POINTED STAR
     mappings.put("\u2743","*"); // HEAVY TEARDROP-SPOKED PINWHEEL ASTERISK
     mappings.put("\u2744","*"); // SNOWFLAKE
     mappings.put("\u2745","*"); // TIGHT TRIFOLIATE SNOWFLAKE
     mappings.put("\u2746","*"); // HEAVY CHEVRON SNOWFLAKE
     mappings.put("\u2747","*"); // SPARKLE
     mappings.put("\u2748","*"); // HEAVY SPARKLE
     mappings.put("\u2749","*"); // BALLOON-SPOKED ASTERISK
     mappings.put("\u274A","*"); // EIGHT TEARDROP-SPOKED PROPELLER ASTERISK
     mappings.put("\u274B","*"); // HEAVY EIGHT TEARDROP-SPOKED PROPELLER ASTERISK
     mappings.put("\uFB00","ff"); // LATIN SMALL LIGATURE FF
     mappings.put("\uFB01","fi"); // LATIN SMALL LIGATURE FI
     mappings.put("\uFB02","fl"); // LATIN SMALL LIGATURE FL
     mappings.put("\uFB03","ffi"); // LATIN SMALL LIGATURE FFI
     mappings.put("\uFB04","ffl"); // LATIN SMALL LIGATURE FFL
     mappings.put("\uFB05","st"); // LATIN SMALL LIGATURE LONG S T
     mappings.put("\uFB06","st"); // LATIN SMALL LIGATURE ST
 
     mappings.put("\u00C0","\\`{A}"); // LATIN CAPITAL LETTER A WITH GRAVE
     mappings.put("\u00C1","\\'{A}"); // LATIN CAPITAL LETTER A WITH ACUTE
     mappings.put("\u00C2","\\^{A}"); // LATIN CAPITAL LETTER A WITH CIRCUMFLEX
     mappings.put("\u00C3","\\~{A}"); // LATIN CAPITAL LETTER A WITH TILDE
     mappings.put("\u00C4","\\\"{A}"); // LATIN CAPITAL LETTER A WITH DIAERESIS
     mappings.put("\u00C5","\\r{A}"); // LATIN CAPITAL LETTER A WITH RING ABOVE
     mappings.put("\u00C7","\\c{C}"); // LATIN CAPITAL LETTER C WITH CEDILLA
     mappings.put("\u00C8","\\`{E}"); // LATIN CAPITAL LETTER E WITH GRAVE
     mappings.put("\u00C9","\\'{E}"); // LATIN CAPITAL LETTER E WITH ACUTE
     mappings.put("\u00CA","\\^{E}"); // LATIN CAPITAL LETTER E WITH CIRCUMFLEX
     mappings.put("\u00CB","\\\"{E}"); // LATIN CAPITAL LETTER E WITH DIAERESIS
     mappings.put("\u00CC","\\`{I}"); // LATIN CAPITAL LETTER I WITH GRAVE
     mappings.put("\u00CD","\\'{I}"); // LATIN CAPITAL LETTER I WITH ACUTE
     mappings.put("\u00CE","\\^{I}"); // LATIN CAPITAL LETTER I WITH CIRCUMFLEX
     mappings.put("\u00CF","\\\"{I}"); // LATIN CAPITAL LETTER I WITH DIAERESIS
     mappings.put("\u00D1","\\~{N}"); // LATIN CAPITAL LETTER N WITH TILDE
     mappings.put("\u00D2","\\`{O}"); // LATIN CAPITAL LETTER O WITH GRAVE
     mappings.put("\u00D3","\\'{O}"); // LATIN CAPITAL LETTER O WITH ACUTE
     mappings.put("\u00D4","\\^{O}"); // LATIN CAPITAL LETTER O WITH CIRCUMFLEX
     mappings.put("\u00D5","\\~{O}"); // LATIN CAPITAL LETTER O WITH TILDE
     mappings.put("\u00D6","\\\"{O}"); // LATIN CAPITAL LETTER O WITH DIAERESIS
     mappings.put("\u00D9","\\`{U}"); // LATIN CAPITAL LETTER U WITH GRAVE
     mappings.put("\u00DA","\\'{U}"); // LATIN CAPITAL LETTER U WITH ACUTE
     mappings.put("\u00DB","\\^{U}"); // LATIN CAPITAL LETTER U WITH CIRCUMFLEX
     mappings.put("\u00DC","\\\"{U}"); // LATIN CAPITAL LETTER U WITH DIAERESIS
     mappings.put("\u00DD","\\'{Y}"); // LATIN CAPITAL LETTER Y WITH ACUTE
     mappings.put("\u00E0","\\`{a}"); // LATIN SMALL LETTER A WITH GRAVE
     mappings.put("\u00E1","\\'{a}"); // LATIN SMALL LETTER A WITH ACUTE
     mappings.put("\u00E2","\\^{a}"); // LATIN SMALL LETTER A WITH CIRCUMFLEX
     mappings.put("\u00E3","\\~{a}"); // LATIN SMALL LETTER A WITH TILDE
     mappings.put("\u00E4","\\\"{a}"); // LATIN SMALL LETTER A WITH DIAERESIS
     mappings.put("\u00E5","\\r{a}"); // LATIN SMALL LETTER A WITH RING ABOVE
     mappings.put("\u00E7","\\c{c}"); // LATIN SMALL LETTER C WITH CEDILLA
     mappings.put("\u00E8","\\`{e}"); // LATIN SMALL LETTER E WITH GRAVE
     mappings.put("\u00E9","\\'{e}"); // LATIN SMALL LETTER E WITH ACUTE
     mappings.put("\u00EA","\\^{e}"); // LATIN SMALL LETTER E WITH CIRCUMFLEX
     mappings.put("\u00EB","\\\"{e}"); // LATIN SMALL LETTER E WITH DIAERESIS
     mappings.put("\u00EC","\\`{i}"); // LATIN SMALL LETTER I WITH GRAVE
     mappings.put("\u00ED","\\'{i}"); // LATIN SMALL LETTER I WITH ACUTE
     mappings.put("\u00EE","\\^{i}"); // LATIN SMALL LETTER I WITH CIRCUMFLEX
     mappings.put("\u00EF","\\\"{i}"); // LATIN SMALL LETTER I WITH DIAERESIS
     mappings.put("\u00F1","\\~{n}"); // LATIN SMALL LETTER N WITH TILDE
     mappings.put("\u00F2","\\`{o}"); // LATIN SMALL LETTER O WITH GRAVE
     mappings.put("\u00F3","\\'{o}"); // LATIN SMALL LETTER O WITH ACUTE
     mappings.put("\u00F4","\\^{o}"); // LATIN SMALL LETTER O WITH CIRCUMFLEX
     mappings.put("\u00F5","\\~{o}"); // LATIN SMALL LETTER O WITH TILDE
     mappings.put("\u00F6","\\\"{o}"); // LATIN SMALL LETTER O WITH DIAERESIS
     mappings.put("\u00F9","\\`{u}"); // LATIN SMALL LETTER U WITH GRAVE
     mappings.put("\u00FA","\\'{u}"); // LATIN SMALL LETTER U WITH ACUTE
     mappings.put("\u00FB","\\^{u}"); // LATIN SMALL LETTER U WITH CIRCUMFLEX
     mappings.put("\u00FC","\\\"{u}"); // LATIN SMALL LETTER U WITH DIAERESIS
     mappings.put("\u00FD","\\'{y}"); // LATIN SMALL LETTER Y WITH ACUTE
     mappings.put("\u00FF","\\\"{y}"); // LATIN SMALL LETTER Y WITH DIAERESIS
     mappings.put("\u0100","\\={A}"); // LATIN CAPITAL LETTER A WITH MACRON
     mappings.put("\u0101","\\={a}"); // LATIN SMALL LETTER A WITH MACRON
     mappings.put("\u0102","\\u{A}"); // LATIN CAPITAL LETTER A WITH BREVE
     mappings.put("\u0103","\\u{a}"); // LATIN SMALL LETTER A WITH BREVE
     mappings.put("\u0104","\\k{A}"); // LATIN CAPITAL LETTER A WITH OGONEK
     mappings.put("\u0105","\\k{a}"); // LATIN SMALL LETTER A WITH OGONEK
     mappings.put("\u0106","\\'{C}"); // LATIN CAPITAL LETTER C WITH ACUTE
     mappings.put("\u0107","\\'{c}"); // LATIN SMALL LETTER C WITH ACUTE
     mappings.put("\u0108","\\^{C}"); // LATIN CAPITAL LETTER C WITH CIRCUMFLEX
     mappings.put("\u0109","\\^{c}"); // LATIN SMALL LETTER C WITH CIRCUMFLEX
     mappings.put("\u010A","\\.{C}"); // LATIN CAPITAL LETTER C WITH DOT ABOVE
     mappings.put("\u010B","\\.{c}"); // LATIN SMALL LETTER C WITH DOT ABOVE
     mappings.put("\u010C","\\v{C}"); // LATIN CAPITAL LETTER C WITH CARON
     mappings.put("\u010D","\\v{c}"); // LATIN SMALL LETTER C WITH CARON
     mappings.put("\u010E","\\v{D}"); // LATIN CAPITAL LETTER D WITH CARON
     mappings.put("\u010F","\\v{d}"); // LATIN SMALL LETTER D WITH CARON
     mappings.put("\u0112","\\={E}"); // LATIN CAPITAL LETTER E WITH MACRON
     mappings.put("\u0113","\\={e}"); // LATIN SMALL LETTER E WITH MACRON
     mappings.put("\u0114","\\u{E}"); // LATIN CAPITAL LETTER E WITH BREVE
     mappings.put("\u0115","\\u{e}"); // LATIN SMALL LETTER E WITH BREVE
     mappings.put("\u0116","\\.{E}"); // LATIN CAPITAL LETTER E WITH DOT ABOVE
     mappings.put("\u0117","\\.{e}"); // LATIN SMALL LETTER E WITH DOT ABOVE
     mappings.put("\u0118","\\k{E}"); // LATIN CAPITAL LETTER E WITH OGONEK
     mappings.put("\u0119","\\k{e}"); // LATIN SMALL LETTER E WITH OGONEK
     mappings.put("\u011A","\\v{E}"); // LATIN CAPITAL LETTER E WITH CARON
     mappings.put("\u011B","\\v{e}"); // LATIN SMALL LETTER E WITH CARON
     mappings.put("\u011C","\\^{G}"); // LATIN CAPITAL LETTER G WITH CIRCUMFLEX
     mappings.put("\u011D","\\^{g}"); // LATIN SMALL LETTER G WITH CIRCUMFLEX
     mappings.put("\u011E","\\u{G}"); // LATIN CAPITAL LETTER G WITH BREVE
     mappings.put("\u011F","\\u{g}"); // LATIN SMALL LETTER G WITH BREVE
     mappings.put("\u0120","\\.{G}"); // LATIN CAPITAL LETTER G WITH DOT ABOVE
     mappings.put("\u0121","\\.{g}"); // LATIN SMALL LETTER G WITH DOT ABOVE
     mappings.put("\u0122","\\c{G}"); // LATIN CAPITAL LETTER G WITH CEDILLA
     mappings.put("\u0123","\\c{g}"); // LATIN SMALL LETTER G WITH CEDILLA
     mappings.put("\u0124","\\^{H}"); // LATIN CAPITAL LETTER H WITH CIRCUMFLEX
     mappings.put("\u0125","\\^{h}"); // LATIN SMALL LETTER H WITH CIRCUMFLEX
     mappings.put("\u0128","\\~{I}"); // LATIN CAPITAL LETTER I WITH TILDE
     mappings.put("\u0129","\\~{i}"); // LATIN SMALL LETTER I WITH TILDE
     mappings.put("\u012A","\\={I}"); // LATIN CAPITAL LETTER I WITH MACRON
     mappings.put("\u012B","\\={i}"); // LATIN SMALL LETTER I WITH MACRON
     mappings.put("\u012C","\\u{I}"); // LATIN CAPITAL LETTER I WITH BREVE
     mappings.put("\u012D","\\u{i}"); // LATIN SMALL LETTER I WITH BREVE
     mappings.put("\u012E","\\k{I}"); // LATIN CAPITAL LETTER I WITH OGONEK
     mappings.put("\u012F","\\k{i}"); // LATIN SMALL LETTER I WITH OGONEK
     mappings.put("\u0130","\\.{I}"); // LATIN CAPITAL LETTER I WITH DOT ABOVE
     mappings.put("\u0134","\\^{J}"); // LATIN CAPITAL LETTER J WITH CIRCUMFLEX
     mappings.put("\u0135","\\^{j}"); // LATIN SMALL LETTER J WITH CIRCUMFLEX
     mappings.put("\u0136","\\c{K}"); // LATIN CAPITAL LETTER K WITH CEDILLA
     mappings.put("\u0137","\\c{k}"); // LATIN SMALL LETTER K WITH CEDILLA
     mappings.put("\u0139","\\'{L}"); // LATIN CAPITAL LETTER L WITH ACUTE
     mappings.put("\u013A","\\'{l}"); // LATIN SMALL LETTER L WITH ACUTE
     mappings.put("\u013B","\\c{L}"); // LATIN CAPITAL LETTER L WITH CEDILLA
     mappings.put("\u013C","\\c{l}"); // LATIN SMALL LETTER L WITH CEDILLA
     mappings.put("\u013D","\\v{L}"); // LATIN CAPITAL LETTER L WITH CARON
     mappings.put("\u013E","\\v{l}"); // LATIN SMALL LETTER L WITH CARON
     mappings.put("\u0141","\\L{}"); //LATIN CAPITAL LETTER L WITH STROKE
     mappings.put("\u0142","\\l{}"); //LATIN SMALL LETTER L WITH STROKE
     mappings.put("\u0143","\\'{N}"); // LATIN CAPITAL LETTER N WITH ACUTE
     mappings.put("\u0144","\\'{n}"); // LATIN SMALL LETTER N WITH ACUTE
     mappings.put("\u0145","\\c{N}"); // LATIN CAPITAL LETTER N WITH CEDILLA
     mappings.put("\u0146","\\c{n}"); // LATIN SMALL LETTER N WITH CEDILLA
     mappings.put("\u0147","\\v{N}"); // LATIN CAPITAL LETTER N WITH CARON
     mappings.put("\u0148","\\v{n}"); // LATIN SMALL LETTER N WITH CARON
     mappings.put("\u014C","\\={O}"); // LATIN CAPITAL LETTER O WITH MACRON
     mappings.put("\u014D","\\={o}"); // LATIN SMALL LETTER O WITH MACRON
     mappings.put("\u014E","\\u{O}"); // LATIN CAPITAL LETTER O WITH BREVE
     mappings.put("\u014F","\\u{o}"); // LATIN SMALL LETTER O WITH BREVE
     mappings.put("\u0150","\\H{O}"); // LATIN CAPITAL LETTER O WITH DOUBLE ACUTE
     mappings.put("\u0151","\\H{o}"); // LATIN SMALL LETTER O WITH DOUBLE ACUTE
     mappings.put("\u0154","\\'{R}"); // LATIN CAPITAL LETTER R WITH ACUTE
     mappings.put("\u0155","\\'{r}"); // LATIN SMALL LETTER R WITH ACUTE
     mappings.put("\u0156","\\c{R}"); // LATIN CAPITAL LETTER R WITH CEDILLA
     mappings.put("\u0157","\\c{r}"); // LATIN SMALL LETTER R WITH CEDILLA
     mappings.put("\u0158","\\v{R}"); // LATIN CAPITAL LETTER R WITH CARON
     mappings.put("\u0159","\\v{r}"); // LATIN SMALL LETTER R WITH CARON
     mappings.put("\u015A","\\'{S}"); // LATIN CAPITAL LETTER S WITH ACUTE
     mappings.put("\u015B","\\'{s}"); // LATIN SMALL LETTER S WITH ACUTE
     mappings.put("\u015C","\\^{S}"); // LATIN CAPITAL LETTER S WITH CIRCUMFLEX
     mappings.put("\u015D","\\^{s}"); // LATIN SMALL LETTER S WITH CIRCUMFLEX
     mappings.put("\u015E","\\c{S}"); // LATIN CAPITAL LETTER S WITH CEDILLA
     mappings.put("\u015F","\\c{s}"); // LATIN SMALL LETTER S WITH CEDILLA
     mappings.put("\u0160","\\v{S}"); // LATIN CAPITAL LETTER S WITH CARON
     mappings.put("\u0161","\\v{s}"); // LATIN SMALL LETTER S WITH CARON
     mappings.put("\u0162","\\c{T}"); // LATIN CAPITAL LETTER T WITH CEDILLA
     mappings.put("\u0163","\\c{t}"); // LATIN SMALL LETTER T WITH CEDILLA
     mappings.put("\u0164","\\v{T}"); // LATIN CAPITAL LETTER T WITH CARON
     mappings.put("\u0165","\\v{t}"); // LATIN SMALL LETTER T WITH CARON
     mappings.put("\u0168","\\~{U}"); // LATIN CAPITAL LETTER U WITH TILDE
     mappings.put("\u0169","\\~{u}"); // LATIN SMALL LETTER U WITH TILDE
     mappings.put("\u016A","\\={U}"); // LATIN CAPITAL LETTER U WITH MACRON
     mappings.put("\u016B","\\={u}"); // LATIN SMALL LETTER U WITH MACRON
     mappings.put("\u016C","\\u{U}"); // LATIN CAPITAL LETTER U WITH BREVE
     mappings.put("\u016D","\\u{u}"); // LATIN SMALL LETTER U WITH BREVE
     mappings.put("\u0170","\\H{U}"); // LATIN CAPITAL LETTER U WITH DOUBLE ACUTE
     mappings.put("\u0171","\\H{u}"); // LATIN SMALL LETTER U WITH DOUBLE ACUTE
     mappings.put("\u0172","\\k{U}"); // LATIN CAPITAL LETTER U WITH OGONEK
     mappings.put("\u0173","\\k{u}"); // LATIN SMALL LETTER U WITH OGONEK
     mappings.put("\u0174","\\^{W}"); // LATIN CAPITAL LETTER W WITH CIRCUMFLEX
     mappings.put("\u0175","\\^{w}"); // LATIN SMALL LETTER W WITH CIRCUMFLEX
     mappings.put("\u0176","\\^{Y}"); // LATIN CAPITAL LETTER Y WITH CIRCUMFLEX
     mappings.put("\u0177","\\^{y}"); // LATIN SMALL LETTER Y WITH CIRCUMFLEX
     mappings.put("\u0178","\\\"{Y}"); // LATIN CAPITAL LETTER Y WITH DIAERESIS
     mappings.put("\u0179","\\'{Z}"); // LATIN CAPITAL LETTER Z WITH ACUTE
     mappings.put("\u017A","\\'{z}"); // LATIN SMALL LETTER Z WITH ACUTE
     mappings.put("\u017B","\\.{Z}"); // LATIN CAPITAL LETTER Z WITH DOT ABOVE
     mappings.put("\u017C","\\.{z}"); // LATIN SMALL LETTER Z WITH DOT ABOVE
     mappings.put("\u017D","\\v{Z}"); // LATIN CAPITAL LETTER Z WITH CARON
     mappings.put("\u017E","\\v{z}"); // LATIN SMALL LETTER Z WITH CARON
     mappings.put("\u01CD","\\v{A}"); // LATIN CAPITAL LETTER A WITH CARON
     mappings.put("\u01CE","\\v{a}"); // LATIN SMALL LETTER A WITH CARON
     mappings.put("\u01CF","\\v{I}"); // LATIN CAPITAL LETTER I WITH CARON
     mappings.put("\u01D0","\\v{i}"); // LATIN SMALL LETTER I WITH CARON
     mappings.put("\u01D1","\\v{O}"); // LATIN CAPITAL LETTER O WITH CARON
     mappings.put("\u01D2","\\v{o}"); // LATIN SMALL LETTER O WITH CARON
     mappings.put("\u01D3","\\v{U}"); // LATIN CAPITAL LETTER U WITH CARON
     mappings.put("\u01D4","\\v{u}"); // LATIN SMALL LETTER U WITH CARON
     mappings.put("\u01E6","\\v{G}"); // LATIN CAPITAL LETTER G WITH CARON
     mappings.put("\u01E7","\\v{g}"); // LATIN SMALL LETTER G WITH CARON
     mappings.put("\u01E8","\\v{K}"); // LATIN CAPITAL LETTER K WITH CARON
     mappings.put("\u01E9","\\v{k}"); // LATIN SMALL LETTER K WITH CARON
     mappings.put("\u01EA","\\k{O}"); // LATIN CAPITAL LETTER O WITH OGONEK
     mappings.put("\u01EB","\\k{o}"); // LATIN SMALL LETTER O WITH OGONEK
     mappings.put("\u01F0","\\v{j}"); // LATIN SMALL LETTER J WITH CARON
     mappings.put("\u01F4","\\'{G}"); // LATIN CAPITAL LETTER G WITH ACUTE
     mappings.put("\u01F5","\\'{g}"); // LATIN SMALL LETTER G WITH ACUTE
     mappings.put("\u1E02","\\.{B}"); // LATIN CAPITAL LETTER B WITH DOT ABOVE
     mappings.put("\u1E03","\\.{b}"); // LATIN SMALL LETTER B WITH DOT ABOVE
     mappings.put("\u1E04","\\d{B}"); // LATIN CAPITAL LETTER B WITH DOT BELOW
     mappings.put("\u1E05","\\d{b}"); // LATIN SMALL LETTER B WITH DOT BELOW
     mappings.put("\u1E06","\\b{B}"); // LATIN CAPITAL LETTER B WITH LINE BELOW
     mappings.put("\u1E07","\\b{b}"); // LATIN SMALL LETTER B WITH LINE BELOW
     mappings.put("\u1E0A","\\.{D}"); // LATIN CAPITAL LETTER D WITH DOT ABOVE
     mappings.put("\u1E0B","\\.{d}"); // LATIN SMALL LETTER D WITH DOT ABOVE
     mappings.put("\u1E0C","\\d{D}"); // LATIN CAPITAL LETTER D WITH DOT BELOW
     mappings.put("\u1E0D","\\d{d}"); // LATIN SMALL LETTER D WITH DOT BELOW
     mappings.put("\u1E0E","\\b{D}"); // LATIN CAPITAL LETTER D WITH LINE BELOW
     mappings.put("\u1E0F","\\b{d}"); // LATIN SMALL LETTER D WITH LINE BELOW
     mappings.put("\u1E10","\\c{D}"); // LATIN CAPITAL LETTER D WITH CEDILLA
     mappings.put("\u1E11","\\c{d}"); // LATIN SMALL LETTER D WITH CEDILLA
     mappings.put("\u1E1E","\\.{F}"); // LATIN CAPITAL LETTER F WITH DOT ABOVE
     mappings.put("\u1E1F","\\.{f}"); // LATIN SMALL LETTER F WITH DOT ABOVE
     mappings.put("\u1E20","\\={G}"); // LATIN CAPITAL LETTER G WITH MACRON
     mappings.put("\u1E21","\\={g}"); // LATIN SMALL LETTER G WITH MACRON
     mappings.put("\u1E22","\\.{H}"); // LATIN CAPITAL LETTER H WITH DOT ABOVE
     mappings.put("\u1E23","\\.{h}"); // LATIN SMALL LETTER H WITH DOT ABOVE
     mappings.put("\u1E24","\\d{H}"); // LATIN CAPITAL LETTER H WITH DOT BELOW
     mappings.put("\u1E25","\\d{h}"); // LATIN SMALL LETTER H WITH DOT BELOW
     mappings.put("\u1E26","\\\"{H}"); // LATIN CAPITAL LETTER H WITH DIAERESIS
     mappings.put("\u1E27","\\\"{h}"); // LATIN SMALL LETTER H WITH DIAERESIS
     mappings.put("\u1E28","\\c{H}"); // LATIN CAPITAL LETTER H WITH CEDILLA
     mappings.put("\u1E29","\\c{h}"); // LATIN SMALL LETTER H WITH CEDILLA
     mappings.put("\u1E30","\\'{K}"); // LATIN CAPITAL LETTER K WITH ACUTE
     mappings.put("\u1E31","\\'{k}"); // LATIN SMALL LETTER K WITH ACUTE
     mappings.put("\u1E32","\\d{K}"); // LATIN CAPITAL LETTER K WITH DOT BELOW
     mappings.put("\u1E33","\\d{k}"); // LATIN SMALL LETTER K WITH DOT BELOW
     mappings.put("\u1E34","\\b{K}"); // LATIN CAPITAL LETTER K WITH LINE BELOW
     mappings.put("\u1E35","\\b{k}"); // LATIN SMALL LETTER K WITH LINE BELOW
     mappings.put("\u1E36","\\d{L}"); // LATIN CAPITAL LETTER L WITH DOT BELOW
     mappings.put("\u1E37","\\d{l}"); // LATIN SMALL LETTER L WITH DOT BELOW
     mappings.put("\u1E3A","\\b{L}"); // LATIN CAPITAL LETTER L WITH LINE BELOW
     mappings.put("\u1E3B","\\b{l}"); // LATIN SMALL LETTER L WITH LINE BELOW
     mappings.put("\u1E3E","\\'{M}"); // LATIN CAPITAL LETTER M WITH ACUTE
     mappings.put("\u1E3F","\\'{m}"); // LATIN SMALL LETTER M WITH ACUTE
     mappings.put("\u1E40","\\.{M}"); // LATIN CAPITAL LETTER M WITH DOT ABOVE
     mappings.put("\u1E41","\\.{m}"); // LATIN SMALL LETTER M WITH DOT ABOVE
     mappings.put("\u1E42","\\d{M}"); // LATIN CAPITAL LETTER M WITH DOT BELOW
     mappings.put("\u1E43","\\d{m}"); // LATIN SMALL LETTER M WITH DOT BELOW
     mappings.put("\u1E44","\\.{N}"); // LATIN CAPITAL LETTER N WITH DOT ABOVE
     mappings.put("\u1E45","\\.{n}"); // LATIN SMALL LETTER N WITH DOT ABOVE
     mappings.put("\u1E46","\\d{N}"); // LATIN CAPITAL LETTER N WITH DOT BELOW
     mappings.put("\u1E47","\\d{n}"); // LATIN SMALL LETTER N WITH DOT BELOW
     mappings.put("\u1E48","\\b{N}"); // LATIN CAPITAL LETTER N WITH LINE BELOW
     mappings.put("\u1E49","\\b{n}"); // LATIN SMALL LETTER N WITH LINE BELOW
     mappings.put("\u1E54","\\'{P}"); // LATIN CAPITAL LETTER P WITH ACUTE
     mappings.put("\u1E55","\\'{p}"); // LATIN SMALL LETTER P WITH ACUTE
     mappings.put("\u1E56","\\.{P}"); // LATIN CAPITAL LETTER P WITH DOT ABOVE
     mappings.put("\u1E57","\\.{p}"); // LATIN SMALL LETTER P WITH DOT ABOVE
     mappings.put("\u1E58","\\.{R}"); // LATIN CAPITAL LETTER R WITH DOT ABOVE
     mappings.put("\u1E59","\\.{r}"); // LATIN SMALL LETTER R WITH DOT ABOVE
     mappings.put("\u1E5A","\\d{R}"); // LATIN CAPITAL LETTER R WITH DOT BELOW
     mappings.put("\u1E5B","\\d{r}"); // LATIN SMALL LETTER R WITH DOT BELOW
     mappings.put("\u1E5E","\\b{R}"); // LATIN CAPITAL LETTER R WITH LINE BELOW
     mappings.put("\u1E5F","\\b{r}"); // LATIN SMALL LETTER R WITH LINE BELOW
     mappings.put("\u1E60","\\.{S}"); // LATIN CAPITAL LETTER S WITH DOT ABOVE
     mappings.put("\u1E61","\\.{s}"); // LATIN SMALL LETTER S WITH DOT ABOVE
     mappings.put("\u1E62","\\d{S}"); // LATIN CAPITAL LETTER S WITH DOT BELOW
     mappings.put("\u1E63","\\d{s}"); // LATIN SMALL LETTER S WITH DOT BELOW
     mappings.put("\u1E6A","\\.{T}"); // LATIN CAPITAL LETTER T WITH DOT ABOVE
     mappings.put("\u1E6B","\\.{t}"); // LATIN SMALL LETTER T WITH DOT ABOVE
     mappings.put("\u1E6C","\\d{T}"); // LATIN CAPITAL LETTER T WITH DOT BELOW
     mappings.put("\u1E6D","\\d{t}"); // LATIN SMALL LETTER T WITH DOT BELOW
     mappings.put("\u1E6E","\\b{T}"); // LATIN CAPITAL LETTER T WITH LINE BELOW
     mappings.put("\u1E6F","\\b{t}"); // LATIN SMALL LETTER T WITH LINE BELOW
     mappings.put("\u1E7C","\\~{V}"); // LATIN CAPITAL LETTER V WITH TILDE
     mappings.put("\u1E7D","\\~{v}"); // LATIN SMALL LETTER V WITH TILDE
     mappings.put("\u1E7E","\\d{V}"); // LATIN CAPITAL LETTER V WITH DOT BELOW
     mappings.put("\u1E7F","\\d{v}"); // LATIN SMALL LETTER V WITH DOT BELOW
     mappings.put("\u1E80","\\`{W}"); // LATIN CAPITAL LETTER W WITH GRAVE
     mappings.put("\u1E81","\\`{w}"); // LATIN SMALL LETTER W WITH GRAVE
     mappings.put("\u1E82","\\'{W}"); // LATIN CAPITAL LETTER W WITH ACUTE
     mappings.put("\u1E83","\\'{w}"); // LATIN SMALL LETTER W WITH ACUTE
     mappings.put("\u1E84","\\\"{W}"); // LATIN CAPITAL LETTER W WITH DIAERESIS
     mappings.put("\u1E85","\\\"{w}"); // LATIN SMALL LETTER W WITH DIAERESIS
     mappings.put("\u1E86","\\.{W}"); // LATIN CAPITAL LETTER W WITH DOT ABOVE
     mappings.put("\u1E87","\\.{w}"); // LATIN SMALL LETTER W WITH DOT ABOVE
     mappings.put("\u1E88","\\d{W}"); // LATIN CAPITAL LETTER W WITH DOT BELOW
     mappings.put("\u1E89","\\d{w}"); // LATIN SMALL LETTER W WITH DOT BELOW
     mappings.put("\u1E8A","\\.{X}"); // LATIN CAPITAL LETTER X WITH DOT ABOVE
     mappings.put("\u1E8B","\\.{x}"); // LATIN SMALL LETTER X WITH DOT ABOVE
     mappings.put("\u1E8C","\\\"{X}"); // LATIN CAPITAL LETTER X WITH DIAERESIS
     mappings.put("\u1E8D","\\\"{x}"); // LATIN SMALL LETTER X WITH DIAERESIS
     mappings.put("\u1E8E","\\.{Y}"); // LATIN CAPITAL LETTER Y WITH DOT ABOVE
     mappings.put("\u1E8F","\\.{y}"); // LATIN SMALL LETTER Y WITH DOT ABOVE
     mappings.put("\u1E90","\\^{Z}"); // LATIN CAPITAL LETTER Z WITH CIRCUMFLEX
     mappings.put("\u1E91","\\^{z}"); // LATIN SMALL LETTER Z WITH CIRCUMFLEX
     mappings.put("\u1E92","\\d{Z}"); // LATIN CAPITAL LETTER Z WITH DOT BELOW
     mappings.put("\u1E93","\\d{z}"); // LATIN SMALL LETTER Z WITH DOT BELOW
     mappings.put("\u1E94","\\b{Z}"); // LATIN CAPITAL LETTER Z WITH LINE BELOW
     mappings.put("\u1E95","\\b{z}"); // LATIN SMALL LETTER Z WITH LINE BELOW
     mappings.put("\u1E96","\\b{h}"); // LATIN SMALL LETTER H WITH LINE BELOW
     mappings.put("\u1E97","\\\"{t}"); // LATIN SMALL LETTER T WITH DIAERESIS
     mappings.put("\u1EA0","\\d{A}"); // LATIN CAPITAL LETTER A WITH DOT BELOW
     mappings.put("\u1EA1","\\d{a}"); // LATIN SMALL LETTER A WITH DOT BELOW
     mappings.put("\u1EB8","\\d{E}"); // LATIN CAPITAL LETTER E WITH DOT BELOW
     mappings.put("\u1EB9","\\d{e}"); // LATIN SMALL LETTER E WITH DOT BELOW
     mappings.put("\u1EBC","\\~{E}"); // LATIN CAPITAL LETTER E WITH TILDE
     mappings.put("\u1EBD","\\~{e}"); // LATIN SMALL LETTER E WITH TILDE
     mappings.put("\u1ECA","\\d{I}"); // LATIN CAPITAL LETTER I WITH DOT BELOW
     mappings.put("\u1ECB","\\d{i}"); // LATIN SMALL LETTER I WITH DOT BELOW
     mappings.put("\u1ECC","\\d{O}"); // LATIN CAPITAL LETTER O WITH DOT BELOW
     mappings.put("\u1ECD","\\d{o}"); // LATIN SMALL LETTER O WITH DOT BELOW
     mappings.put("\u1EE4","\\d{U}"); // LATIN CAPITAL LETTER U WITH DOT BELOW
     mappings.put("\u1EE5","\\d{u}"); // LATIN SMALL LETTER U WITH DOT BELOW
     mappings.put("\u1EF2","\\`{Y}"); // LATIN CAPITAL LETTER Y WITH GRAVE
     mappings.put("\u1EF3","\\`{y}"); // LATIN SMALL LETTER Y WITH GRAVE
     mappings.put("\u1EF4","\\d{Y}"); // LATIN CAPITAL LETTER Y WITH DOT BELOW
     mappings.put("\u1EF5","\\d{y}"); // LATIN SMALL LETTER Y WITH DOT BELOW
     mappings.put("\u1EF8","\\~{Y}"); // LATIN CAPITAL LETTER Y WITH TILDE
     mappings.put("\u1EF9","\\~{y}"); // LATIN SMALL LETTER Y WITH TILDE
     }
 
     public String getLatex(char c){
         String unicode = toUnicode(c);
         return mappings.get(unicode);
     }
     private String toUnicode(char ch) {
         return String.format("\\u%04x", (int) ch);
     }
 */
  
 }
