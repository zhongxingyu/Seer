 package org.nhcham.ahoy;
 
 import java.math.BigInteger;
 import java.util.*;
 import java.io.*;
 import android.content.*;
 import android.content.res.*;
 import android.util.*;
 
 public class ApMessageFilter
 {
     final static String SSID_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
     final static String AHOY_PREFIX_CHAR = "a";
     final static int MAX_BITS = 178;
     final static String TAG = "ApMessageFilter";
     public HashMap<Integer, ILanguagePack> languagePacks;
     BigInteger radix = new BigInteger("62");
     
     public ApMessageFilter(Context context)
     {
         languagePacks = new HashMap<Integer, ILanguagePack>();
         try
         {
             BufferedReader r = new BufferedReader(new InputStreamReader(context.getAssets().open("languages.txt")));
             String line;
             while ((line = r.readLine()) != null)
             {
                 line = line.trim();
                 int spaceOffset = line.indexOf(' ');
                 if (spaceOffset < 0)
                     continue;
                 int languageId = Integer.parseInt(line.substring(0, spaceOffset), 10);
                 int spaceOffset2 = line.indexOf(' ', spaceOffset + 1);
                 if (spaceOffset2 < 0)
                     continue;
                 String languageTag = line.substring(spaceOffset + 1, spaceOffset2);
                 String languageMarker = line.substring(spaceOffset2 + 1);
 //                 Log.d(TAG, String.format("Now loading [%d] [%s]...", languageId, languageTag));
                 ILanguagePack pack = null;
                 if (languageTag.equals("utf8") || languageTag.equals("utf16"))
                     pack = new LanguagePackUtf(context, languageId, languageTag, languageMarker);
                 else
                     pack = new LanguagePack(context, languageId, languageTag, languageMarker);
                 languagePacks.put(languageId, pack);
             }
         } catch (IOException e) {
             // TODO: what do we do with this?
         }
     }
     
     public String ssidToMessage(final String s)
     {
         if (!s.startsWith(AHOY_PREFIX_CHAR))
             return null;
             
         if (s.length() != 31)
             return null;
             
         BigInteger bigint = BigInteger.ZERO;
         for (int i = s.length() - 1; i >= 1; i--)
         {
             // TODO: This is slow.
             int n = SSID_ALPHABET.indexOf(s.charAt(i));
             if (n == -1)
                 return null;
             bigint = bigint.multiply(radix);
             // TODO: This is slow.
             bigint = bigint.add(new BigInteger(String.format("%d", n)));
         }
 //         Log.d(TAG, String.format("BigInt is %s.", bigint.toString()));
         byte[] bits = new byte[MAX_BITS];
         for (int i = 0; i < MAX_BITS; i++)
             bits[i] = 0;
         int offset = 0;
         while (!bigint.equals(BigInteger.ZERO))
         {
             if (bigint.and(BigInteger.ONE).equals(BigInteger.ONE))
                 bits[offset] = 1;
             offset++;
             if (offset >= MAX_BITS)
                 // TODO: Should we return null here? Probably yes.
                 break;
             bigint = bigint.shiftRight(1);
         }
 //         String bitstring = new String();
 //         for (int i = 0; i < bits.length; i++)
 //             bitstring += bits[i] == 0 ? "0" : "1";
 //         Log.d(TAG, String.format("Bit string is %s.", bitstring));
         
         if (bits[0] == 1)
         {
             // this is language pack 1 to 127...
             int languagePackId = 0;
             for (int i = 0; i < 7; i++)
                 languagePackId |= (bits[i + 1] << (6 - i));
             if (languagePacks.containsKey(languagePackId))
             {
 //                 Log.d(TAG, String.format("Language pack id is [%d].", languagePackId));
                 final String message = languagePacks.get(languagePackId).decodeMessage(bits, 8);
 //                 Log.d(TAG, String.format("Message is [%s].", message));
                 return message;
             }
         }
         
        return s;
     }
     
     public String messageToSsid(final String _message)
     {
         String s = compactMessage(_message);
         int[] _result = this.encodeMessage(s);
         int bitLength = _result[0];
         int languageId = _result[1];
         byte[] bits = new byte[MAX_BITS];
         for (int i = 0; i < bits.length; i++)
             bits[i] = 0;
         languagePacks.get(languageId).encodeMessage(s, bits);
         
 //         String bitstring = new String();
 //         for (int i = 0; i < bits.length; i++)
 //             bitstring += bits[i] == 0 ? "0" : "1";
 //         Log.d(TAG, String.format("Bit string is %s.", bitstring));
         
         BigInteger bigint = BigInteger.ZERO;
         // most important stuff is at the beginning of bits, 
         // therefore: add it last, (put it in the LSB area)
         for (int i = bits.length - 1; i >= 0; i--)
         {
             bigint = bigint.shiftLeft(1);
             if (bits[i] == 1)
                 bigint = bigint.add(BigInteger.ONE);
         }
         
 //         Log.d(TAG, String.format("BigInteger is %s.", bigint.toString()));
         String ssid = AHOY_PREFIX_CHAR;
         
         while (!bigint.equals(BigInteger.ZERO))
         {
             BigInteger[] result = bigint.divideAndRemainder(radix);
             ssid += SSID_ALPHABET.charAt(result[1].intValue());
             bigint = result[0];
         }
         
         while (ssid.length() < 31)
             ssid += SSID_ALPHABET.charAt(0);
         
 //         Log.d(TAG, String.format("SSID is %s.", ssid));
         
         return ssid;
     }
     
     public int[] encodeMessage(String _message)
     {
         String message = compactMessage(_message);
 //         Log.d(TAG, String.format("Encoding message: [%s]", message));
         int minimumBitLength = -1;
         int minimumBitLengthLang = -1;
         for (int languageId : languagePacks.keySet())
         {
             ILanguagePack pack = languagePacks.get(languageId);
             if (pack.canEncodeMessage(message))
             {
                 short bitLength = pack.getEncodedMessageLength(message);
 //                 Log.d(TAG, String.format("[%s]: %4d bits", pack.languageTag(), bitLength));
                 
                 if (minimumBitLength == -1 || bitLength < minimumBitLength)
                 {
                     minimumBitLength = bitLength;
                     minimumBitLengthLang = languageId;
                 }
             }
         }
         Log.d(TAG, String.format("Message is probably [%s], length is %3d bits: [%s]", languagePacks.get(minimumBitLengthLang).languageTag(), minimumBitLength, message));
         int[] result = new int[2];
         result[0] = minimumBitLength;
         result[1] = minimumBitLengthLang;
         return result;
     }
     
     private String compactMessage(final String message)
     {
         // remove leading and trailing spaces, replace spans of multiple spaces with a single space
         return message.trim().replaceAll(" +", " ");
     }
 };
