 import org.codeswarm.bytesize.*;
 
 /**
  * Example code for documentation.
  */
 public class ByteSizeDemo {
 
   public static void main(String[] args) throws java.text.ParseException {
 
     // Create a new byte size format using the default locale and settings.
     ByteSizeFormat format = new ByteSizeFormatBuilder().build();
 
     // Parse a byte size string.
     ByteSize a = format.parse(".85 gigabytes");
 
     // Convert 0.85 gigabytes to megabytes. Prints "850.0".
     System.out.println(a.numberOfBytes(ByteSizeUnits.MB));
 
     // Create a ByteSize object representing 1500 bytes.
     ExactByteSize b = ByteSizes.byteSize(1500);
 
     // Get the number of bytes. Prints "1500".
     System.out.println(b.numberOfBytes());
 
     // Format the size using abbreviated units. Prints "1.5 kB".
     System.out.println(format.format(b, ByteSizeFormat.WordLength.ABBREVIATION));
 
     // Create a new formatter for the Slovak language using IEC units (powers of 2)
     ByteSizeFormat format2 = new ByteSizeFormatBuilder()
       .unitSystem(ByteSizeUnits.IEC)
       .locale(java.util.Locale.forLanguageTag("sk"))
       .build();
 
    // Format the first size using full words.
     // Prints "810,623 mebibajtov" (810.623 mebibytes).
     System.out.println(format2.format(a, ByteSizeFormat.WordLength.FULL));
 
   }
 
 }
