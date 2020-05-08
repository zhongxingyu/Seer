 package com.hotcats.mp4artextractor.printer;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.hotcats.mp4artextractor.data.Mp4File;
 import com.hotcats.mp4artextractor.data.atom.Atom;
 import com.hotcats.mp4artextractor.data.atom.FtypAtom;
 import com.hotcats.mp4artextractor.data.atom.MoovAtom;
 
 public class Mp4FilePrinter {
 
   public static void print(Mp4File mp4File) {
     for (Atom a : mp4File.getAtoms()) {
       print(a);
     }
   }
 
   public static void print(Atom atom) {
     System.out.println("type: " + atom.getType());
     System.out.println("  size: " + atom.getSize());
     System.out.println("  extendedSize: " + atom.getExtendedSize());
     switch (atom.getType()) {
     case FTYP:
       printFtypAtom((FtypAtom) atom);
       break;
     case MOOV:
       printMoovAtom((MoovAtom) atom);
       break;
    case FREE:
      // Do nothing, all fields have already been printed.
      break;
     }
   }
 
   private static void printFtypAtom(FtypAtom ftypAtom) {
     printKeyValue("major brand", ftypAtom.getMajorBrand());
     printKeyValue("minor version", ftypAtom.getMinorVersion());
     printKeyValueList("compatible brands", ftypAtom.getCompatibleBrands());
   }
 
   private static void printMoovAtom(MoovAtom moovAtom) {
     // TODO implement
   }
 
   private static void printKeyValue(String key, byte[] value) {
     System.out.println("  " + key + ": " + Arrays.toString(value)
         + " (" + bytesToString(value) + ")");
   }
 
   private static void printKeyValueList(String key, List<byte[]> values) {
     System.out.println("  " + key + ":");
     for (byte[] value : values) {
       System.out.println("    " + Arrays.toString(value)
           + " (" + bytesToString(value) + ")");
     }
   }
 
   public static String bytesToString(byte[] bytes) {
     return new String(bytes);
   }
 }
