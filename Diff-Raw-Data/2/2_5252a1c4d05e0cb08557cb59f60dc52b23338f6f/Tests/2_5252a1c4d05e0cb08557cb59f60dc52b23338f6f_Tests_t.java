 package asl;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 import asl.security.MemberDigest;
 import asl.util.Hex;
 
 public class Tests
 {
     public static void usage()
     {
         System.out.println("usage: " +System.getProperty("program.name")+ " <test>");
         System.exit(1);
     }
 
     public static void main(String args[])
     {
         MemberDigest d1 = new MemberDigest() {
             protected void addDigestMembers() {
                 addToDigest("Test string for first digest.");
             }
         };
         MemberDigest d2 = new MemberDigest() {
             protected void addDigestMembers() {
                 addToDigest("Test string for second digest.");
             }
         };
         MemberDigest d3 = new MemberDigest("SHA-1") {
             protected void addDigestMembers() {
                 addToDigest("Test string for third digest");
             }
         };
 
         MemberDigest d4 = new MemberDigest("SHA-1") {
             protected void addDigestMembers() {
                 addToDigest("Test string for fourth digest");
             }
         };
 
 
         System.out.format(" d1 MemberDigest(MD5)            : %s\n", d1.getDigestString());
         System.out.format(" d2 MemberDigest(MD5)            : %s\n", d2.getDigestString());
         System.out.format(" d3 MemberDigest(SHA-1)          : %s\n", d3.getDigestString());
         System.out.format(" d4 MemberDigest(SHA-1)          : %s\n", d4.getDigestString());
         System.out.println("");
 
         System.out.format(" d1.hex -> bytes -> hex          : %s\n", Hex.byteBufferToHexString(Hex.hexStringToByteBuffer(d1.getDigestString()), false));
         ArrayList<MemberDigest> coll = new ArrayList<MemberDigest>();
         coll.add(d1);
         System.out.format(" multi-digest: d1                : %s\n", Hex.byteBufferToHexString(MemberDigest.multiDigest(coll), false));
         coll.add(d3);
         System.out.format(" multi-digest: d1 ^ d3           : %s\n", Hex.byteBufferToHexString(MemberDigest.multiDigest(coll), false));
         coll.add(d2);
         System.out.format(" multi-digest: d1 ^ d3 ^ d2      : %s\n", Hex.byteBufferToHexString(MemberDigest.multiDigest(coll), false));
         coll.add(d2);
         System.out.format(" multi-digest: d1 ^ d3 ^ d2 ^ d2 : %s\n", Hex.byteBufferToHexString(MemberDigest.multiDigest(coll), false));
         coll.add(d4);
         System.out.format(" multi-digest: d1 ^ d3 ^ d4      : %s\n", Hex.byteBufferToHexString(MemberDigest.multiDigest(coll), false));
 
         String preSplit = "TAG1:TAG2:TAG3:";
         System.out.format("\nSplitting string \"%s\"\n", preSplit);
         int idx = 0;
         for (String part: preSplit.split(":")) {
             idx++;
            System.out.format(" part %d: \"%s\"\n", idx, part);
         }
     }
 }
