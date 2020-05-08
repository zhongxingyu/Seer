 package com.orbious.util.tokyo.app;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import tokyocabinet.HDB;
 import com.orbious.util.tokyo.Bytes;
 import com.orbious.util.tokyo.HDBWrapper;
 import com.orbious.util.tokyo.WrapperException;
 import gnu.getopt.Getopt;
 
 public class Dumper {
 
   private static void usage() {
     System.out.println(
     		"Usage: Dumper [-h] [-k|[-s <keystr>] -v <valueclass>]\n" +
     		"         -c <keyclass> -t <tokyofile.hdb> -o <outputfile>\n" +
     		"\n" +
     		"    -h                    Print this help message and exit.\n" +
     		"    -k                    Dump keys only.\n" +
     		"    -s <keystr>           Dump the value for key <keystr> (in string format).\n" +
     		"    -v <valueclass>       The value class.\n" +
     		"    -c <keyclass>         The key class.\n" +
     		"                          One of either java.lang.[String|Integer|Long|Double]\n" +
     		"    -t <tokyofile.hdb>    The tokyo cabinet HDB file to dump.\n" +
     		"    -o <outputfile>       Where to write the output to.\n");
     System.exit(1);
   }
 
   public static void main(String[] args) {
     Getopt opts;
     int c;
     boolean keys = false;
     String keystr = null;
     String keyclass = null;
     String valueclass = null;
     String tokyofile = null;
     String outfile = null;
 
     opts = new Getopt("Dumper", args, "hks:v:c:t:o:");
 
     while ( (c = opts.getopt()) != -1 ) {
       switch ( c ) {
         case 'h':
           usage();
         case 'k':
           keys = true;
           break;
         case 's':
           keystr = opts.getOptarg();
           break;
         case 'v':
           valueclass = opts.getOptarg();
         case 'c':
           keyclass = opts.getOptarg();
           break;
         case 't':
           tokyofile = opts.getOptarg();
           break;
         case 'o':
           outfile = opts.getOptarg();
           break;
       }
     }
 
     if ( tokyofile == null ) {
       System.err.println("You must specify a tokyofile.hdb?\n");
       usage();
     } else if ( outfile == null ) {
       System.err.println("You must specify an outputfile?\n");
       usage();
     }
 
     if ( keys ) {
       // dumping keys only
       if ( keyclass == null ) {
         System.err.println("You must specify a key class?\n");
         usage();
      } else if ( !keyclass.equals("String") && !keyclass.equals("Integer") &&
          !keyclass.equals("Long") && !keyclass.equals("Double") ) {
         System.err.println("Unsupported key class type " + keyclass);
         usage();
       }
       System.out.println("Dumping keys from " + tokyofile +
           " with keys " + keyclass + " to " + outfile);
       dumpKeys(keyclass, tokyofile, outfile);
     } else {
       // dumping keys + values
       if ( keystr != null ) {
         // dumping a value for a key
         System.out.println("Dumping the value from " + tokyofile +
             " for the key " + keystr + " (" + keyclass + ") as a " +
             valueclass + " to " + outfile);
         dumpValue(keystr, keyclass, valueclass, tokyofile, outfile);
       } else {
         // TODO
       }
     }
   }
 
   private static BufferedWriter openWriter(String outfile) {
     BufferedWriter bw;
 
     try {
       bw = new BufferedWriter(new FileWriter(new File(outfile)));
     } catch ( IOException ioe ) {
       System.err.println("Failed to initialize output writer to " + outfile);
       ioe.printStackTrace();
       return null;
     }
 
     return bw;
   }
 
   private static Class<?> cast(String clazzstr) {
     Class<?> clazz = null;
     try {
       clazz = Class.forName(clazzstr);
     } catch ( ClassNotFoundException cnfe ) {
       cnfe.printStackTrace();
       return null;
     }
 
     return clazz;
   }
 
   private static boolean implementz(Class<?> clazz, Class<?> intf) {
     Class<?>[] intfs;
 
     intfs = clazz.getInterfaces();
     for ( int i = 0; i < intfs.length; i++ ) {
       if ( intfs[i].equals(intf) ) {
         return true;
       }
     }
 
     return false;
   }
 
   private static void dumpValue(String keystr, String keyclass, String valueclass,
       String tokyofile, String outfile) {
     BufferedWriter bw;
     HDBWrapper hdbw;
     Class<?> kclazz;
     byte[] kb;
     Class<?> vclazz;
     Object value;
 
     bw = openWriter(outfile);
     if ( bw == null ) {
       return;
     }
 
     kclazz = cast(keyclass);
     if ( kclazz == null ) {
       return;
     }
 
     vclazz = cast(valueclass);
     if ( vclazz == null ) {
       return;
     }
 
     kb = Bytes.convert(kclazz, keystr);
 
     hdbw = new HDBWrapper();
     try {
       hdbw.initReader(new File(tokyofile), 1);
     } catch ( WrapperException we ) {
       System.err.println("Error opening tokyo file " + tokyofile.toString());
       we.printStackTrace();
       return;
     }
 
     try {
       value = hdbw.read(kb, vclazz);
     } catch ( UnsupportedEncodingException uee ) {
       System.err.println("The value class (" + valueclass + ") is not supported");
       uee.printStackTrace();
       return;
     }
 
     try {
       if ( !implementz(vclazz, List.class) ) {
         // just write the output
         bw.write((String)value);
       } else {
         // a list, we can use newlines !
         List<?> l = (List<?>)value;
         for ( int i = 0; i < l.size(); i++ ) {
           bw.write(l.get(i) + "\n");
         }
       }
     } catch ( IOException ioe ) {
       System.err.println("Error writing to " + outfile);
       ioe.printStackTrace();
     }
 
     try {
       bw.close();
     } catch ( IOException ioe ) {
       System.err.println("Error closing " + outfile);
       ioe.printStackTrace();
     }
 
     try {
       hdbw.close();
     } catch ( WrapperException ignored ) { }
   }
 
   private static void dumpKeys(String keyclass, String tokyofile, String outfile) {
     HDBWrapper hdbw;
     HDB hdb;
     byte[] bytes;
     Class<?> clazz;
     BufferedWriter bw;
 
     bw = openWriter(outfile);
     if ( bw == null ) {
       return;
     }
 
     clazz = cast(keyclass);
     if ( clazz == null ) {
       return;
     }
 
     hdbw = new HDBWrapper();
     try {
       hdbw.initReader(new File(tokyofile), 1);
     } catch ( WrapperException we ) {
       System.err.println("Error opening tokyo file " + tokyofile.toString());
       we.printStackTrace();
       return;
     }
 
     // could be big, therefore we must access the hdb directly ..
     hdb = hdbw.hdb();
     hdb.iterinit();
 
     try {
       Object obj;
       while ( (bytes = hdb.iternext()) != null ) {
         obj = Bytes.convert(clazz, bytes);
         bw.write(obj + "\n");
       }
     } catch ( IOException ioe ) {
       System.err.println("Error writing to " + outfile);
       ioe.printStackTrace();
     }
 
     try {
       bw.close();
     } catch ( IOException ioe ) {
       System.err.println("Error closing " + outfile);
       ioe.printStackTrace();
     }
 
     try {
       hdbw.close();
     } catch ( WrapperException ignored ) { }
   }
 
 }
