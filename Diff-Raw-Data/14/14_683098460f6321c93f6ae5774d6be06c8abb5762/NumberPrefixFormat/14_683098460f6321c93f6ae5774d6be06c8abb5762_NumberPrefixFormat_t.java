 package info.mikaelsvensson.ftpbackup.util;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import java.text.FieldPosition;
 import java.text.Format;
 import java.text.NumberFormat;
 import java.text.ParsePosition;
 
 //TODO: Write unit tests
 
 /**
  * Formats numbers using SI prefixes (e.g. kilo and M) or binary prefixes (e.g. kibi or Mi).
  * <p/>
  * {@code [integer][.fraction][EPTGMK][i]}
  * <p/>
  * The prefix part defines if a specific prefix should be used or if the most suitable prefix
  * should be chosen for each formatted value.
  * <p/>
  * Specific prefix:
  * <br />{@code exbi} or {@code Ei}
  * <br />{@code pebi} or {@code Pi}
  * <br />{@code tebi} or {@code Ti}
  * <br />{@code gibi} or {@code Gi}
  * <br />{@code mebi} or {@code Mi}
  * <br />{@code kibi} or {@code Ki} (note: both lower-case k and upper-case K)
  * <br />{@code exa} or {@code E}
  * <br />{@code peta} or {@code P}
  * <br />{@code tera} or {@code T}
  * <br />{@code giga} or {@code G}
  * <br />{@code mega} or {@code M}
  * <br />{@code kilo} or {@code k} (note: only lower-case k)
  * <p/>
  * {@code new NumberPrefixFormat("M").format(123456789L)} returns {@code 123.457M}.
  * <p/>
  * {@code new NumberPrefixFormat("mega").format(123456789L)} returns {@code 123.457mega}.
  * <p/>
  * Best-choice prefix:
  * The formatter can choose individual prefixes for each formatted value. The only thing you need
  * to do is specify if an SI or a binary prefix should be chosen.
  * <p/>
  * Use a single "i" to choose from the binary prefixes:
  * <br />{@code new NumberPrefixFormat("i").format(1024L)} returns {@code 1Ki}.
  * <p/>
  * An SI prefix will be chosen if "i" is NOT specified:
  * <br />{@code new NumberPrefixFormat("").format(1024L)} returns {@code 1.024k}.
  * <p/>
  * Other examples;
  * <p/>
  * {@code new NumberPrefixFormat("_k").format(123456789L)} returns {@code 123, 456.789 k}.
  * <p/>
  * {@code new NumberPrefixFormat("").format(123456789L)} returns {@code 123.457M}.
  * <p/>
  * {@code new NumberPrefixFormat(".2 M").format(123456789L)} returns {@code 123.46 M}.
  * <p/>
  * {@code new NumberPrefixFormat(".2").format(123456789L)} returns {@code 123.46M}.
  * <p/>
  * {@code new NumberPrefixFormat(".4_Mi").format(1024L)} returns {@code 0.0010 Mi}.
  * <p/>
  * {@code new NumberPrefixFormat("3.2_Ki").format(8*1024L + 512)} returns {@code 008.50 Ki}.
  */
 class NumberPrefixFormat extends Format {
 // ------------------------------ FIELDS ------------------------------
 
     public static final String BYTES_SYMBOL = "B";
     public static final String BYTES_NAME = "bytes";
     public static final String BITS_SYMBOL = "b";
     public static final String BITS_NAME = "bits";
     private NumberFormat nf = NumberFormat.getNumberInstance();
     private boolean binary;
 
     private Prefix prefix = null;
     private String unitText = "";
     private String prefixText = "";
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     NumberPrefixFormat(String arguments) {
         if (arguments != null && arguments.length() > 0) {
             binary = arguments.charAt(arguments.length() - 1) == 'i';
             if (binary) {
                 arguments = arguments.substring(0, arguments.length() - 1);
             }
             /*
             for (String s : new String[]{BITS_NAME, BITS_SYMBOL, BYTES_NAME, BYTES_SYMBOL}) {
                 if (arguments.endsWith(s)) {
                     unitText = s;
                     arguments = arguments.substring(0, arguments.length() - s.length());
                     break;
                 }
             }
             */
             for (Prefix p : Prefix.values()) {
                 if (p.binary == binary && arguments.endsWith(p.abbr)) {
                     prefixText = p.abbr;// + (binary ? "i" : "");;
                     prefix = p;
                     arguments = arguments.substring(0, arguments.length() - prefixText.length());
                     break;
                 } else if (p.binary == binary && arguments.endsWith(p.symbol)) {
                     prefixText = p.symbol;// + (binary ? "i" : "");
                     prefix = p;
                     arguments = arguments.substring(0, arguments.length() - prefixText.length());
                     break;
                 }
             }
             if (arguments.length() > 0) {
                 char lastChar = arguments.charAt(arguments.length() - 1);
                 if (Character.isWhitespace(lastChar) || lastChar == '_') {
                     prefixText = " " + prefixText;
                     arguments = arguments.substring(0, arguments.length() - 1);
                 }
                 int dotPos = arguments.indexOf('.');
                 if (dotPos > 0) {
                     nf.setMaximumFractionDigits(Integer.parseInt(arguments.substring(dotPos + 1)));
                     nf.setMinimumFractionDigits(Integer.parseInt(arguments.substring(dotPos + 1)));
                     nf.setMaximumIntegerDigits(Integer.parseInt(arguments.substring(0, dotPos)));
                     nf.setMinimumIntegerDigits(Integer.parseInt(arguments.substring(0, dotPos)));
                 } else if (dotPos == 0) {
                     nf.setMaximumFractionDigits(Integer.parseInt(arguments.substring(1)));
                     nf.setMinimumFractionDigits(Integer.parseInt(arguments.substring(1)));
                 } else if (arguments.length() > 0) {
                     nf.setMaximumIntegerDigits(Integer.parseInt(arguments));
                     nf.setMinimumIntegerDigits(Integer.parseInt(arguments));
                 }
             }
         }
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
         if (obj instanceof Number) {
             Number number = (Number) obj;
             long size = number.longValue();
             double value = size;
             Prefix pr = prefix;
             String prText = prefixText;
             if (null == pr) {
                 for (Prefix p : Prefix.values()) {
                     if (p.binary == binary) {
                         if (p.minValue <= size) {
                             pr = p;
                            prText += p.symbol;
                             break;
                         }
                     }
                 }
             }
             if (pr != null) {
                 value /= pr.minValue;
             }
             String formattedValue = nf.format(value);
             toAppendTo.append(formattedValue + prText + (pr != null && pr.binary ? "i" : "") + unitText);
         }
         return toAppendTo;
     }
 
     private Prefix getBestPrefixMatch(long value) {
         Prefix bestMatch = null;
         return bestMatch;
     }
 
     @Override
     public Object parseObject(String source, ParsePosition pos) {
         throw new NotImplementedException();
     }
 
 // -------------------------- ENUMERATIONS --------------------------
 
     private enum Prefix {
         bin_exbi("exb", "E", 1L << 60, true),
         bin_pebi("peb", "P", 1L << 50, true),
         bin_tebi("teb", "T", 1L << 40, true),
         bin_gibi("gib", "G", 1L << 30, true),
         bin_mebi("meb", "M", 1L << 20, true),
         bin_kibi("kib", "K", 1L << 10, true),
         /*bin_____("", "", 1L << 0, true),*/
 
         si_exa_("exa", "E", 1000000000000000000L, false),
         si_peta("peta", "P", 1000000000000000L, false),
         si_tera("tera", "T", 1000000000000L, false),
         si_giga("giga", "G", 1000000000L, false),
         si_mega("mega", "M", 1000000L, false),
         si_kilo("kilo", "k", 1000L, false)/*,
         si_____("", "", 1L, false)*/;
 
         private String abbr;
         private String symbol;
         private long minValue;
         private boolean binary;
 
         Prefix(String abbr, String symbol, long minValue, boolean binary) {
             this.abbr = abbr;
             this.symbol = symbol;
             this.minValue = minValue;
             this.binary = binary;
         }
     }
 }
