 
 package com.github.erasmux.AndLibUtils;
 
 import java.io.*;
 import java.util.*;
 import java.nio.channels.*;
 
 public class JNIRenamer {
 
     private File file_;
     private String fileName_;
     private boolean readonly_;
     private ElfReader reader_;
     private long prelinked_;
 
 
     public JNIRenamer(File file,String filename,boolean readonly) throws IOException, FileNotFoundException {
         file_ = file;
         fileName_ = filename;
         readonly_ = readonly;
         
         RandomAccessFile raf = new RandomAccessFile(file_, "r");
         prelinked_ = Prelinked.GetPrelinkAddr(raf);
         raf.close();
 
         reader_ = new ElfReader(file_, readonly ? "r" : "rw");
         if (reader_.valid())
             reader_.readSections();
     }
 
     public File file() {
         return file_;
     }
 
     public String filename() {
         return fileName_;
     }
 
     /// search for the given function, and if newName!=null also tried to replace it.
     /// returns number of times the function was found/replaced. returns -1 on failure.
     public int findRenameFunc(String functionSig, String newName,
                               PrintStream out, PrintStream log, PrintStream err) throws IOException {
         boolean rename = newName != null;
         if (rename && readonly_) {
             if (err != null)
                 err.println("ERROR: JNIRenamer is readonly, can not rename function.");
             return -1;
         }
         if (!reader_.valid()) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" is not a valid ELF!");
             return -1;
         }
         if (!reader_.hasSection(".data")) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" does not have a .data section?!");
             return -1;
         }
         if (!reader_.hasSection(".rodata")) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" does not have a .rodata section?!");
             return -1;
         }
 
         long rodataAddr = reader_.sectionAddr(".rodata");
         long rodataBaseAddr = rodataAddr;
         if (prelinked_ >= 0)
             rodataBaseAddr += prelinked_;
         if (log != null) {
             log.println(String.format(".rodata section @ 0x%08X",rodataAddr));
             if (prelinked_ >= 0) {
                 log.println(String.format("file prelinked  @ 0x%08X",prelinked_));
                 log.println(String.format("=> .rodata base @ 0x%08X",rodataBaseAddr));
             }
         }
 
         int splitSig = functionSig.indexOf('(');
         if (splitSig < 0) {
             if (err != null)
                 err.println("ERROR: Invalid function signature: "+functionSig);
             return -1;
         }
         String funcName = functionSig.substring(0,splitSig);
         String onlySig = functionSig.substring(splitSig);
 
         Set<Long> funcOffsets = findStringInSection(funcName,".rodata",false,rodataBaseAddr,
                                                     "function name",log,err);
         Set<Long> sigOffsets = findStringInSection(onlySig,".rodata",false,rodataBaseAddr,
                                                    "signature",log,err);
         Set<Long> newOffsets = newName==null ? null :
             findStringInSection(newName,".rodata",true,rodataBaseAddr,
                                 "new function name",log,err);
         if (funcOffsets == null || sigOffsets == null)
             return -1;
         long newOffset = 0;
         if (rename) {
             if (newOffsets == null)
                 return -1;
             newOffset = newOffsets.iterator().next().longValue();
             if (log != null)
                 log.println(String.format("Matches will be replaced with new offset: 0x%08X",newOffset));
         }
 
         if (log != null)
             log.println(String.format("Searching%s .data for occurences of the function:",rename ? " and replacing" : ""));
         reader_.seekSection(".data",0);
         long dataAddr = reader_.sectionAddr(".data");
         int count = 0;
         Long lastValue = null;
         while (!reader_.finishedSection()) {
             Long curValue = new Long(reader_.readUInt());
             if (lastValue != null && funcOffsets.contains(lastValue)
                 && sigOffsets.contains(curValue)) {
                 if (out != null)
                     out.println(String.format("0x%08X",reader_.currentOffsetInSection()+dataAddr-8));
                 if (rename) {
                     reader_.reseek(-8);
                     reader_.writeUInt(newOffset);
                     reader_.skip(4);
                 }
                 ++count;
             }
             lastValue = curValue;
         }
         
         return count;
     }
 
     /// searches the given section for the given string returning all the offset it is found at.
     /// if onlyBest is true than preferably an exact match is returned, if there is no exact match
     /// the last non-exact match is returned.
     /// label is just for printing message and denotes what string we are looking for.
     private Set<Long> findStringInSection(String str, String section, boolean onlyBest, long deltaOfs,
                                           String label, PrintStream log, PrintStream err) throws IOException {
         if (str.length() < 1) {
             if (err != null)
                 err.println("Invalid "+label+" - empty string?!");
             return null;
         }
 
         if (log != null)
             log.println("Searching "+section+" for "+label+" \""+str+"\"...");
         Set<Long> offsets = new TreeSet<Long>();
         reader_.seekSection(section,0);
         long sectionAddr = reader_.sectionAddr(section);
         long lastMatch=-1,findOfs;
         while ( (findOfs=reader_.seekString(str)) >= 0 ) {
             if (onlyBest)
                 lastMatch = findOfs+deltaOfs;
             else
                 offsets.add(new Long(findOfs+deltaOfs));
             if (log!=null)
                 log.println(String.format("  found "+label+" @ 0x%08X",findOfs+sectionAddr));
             if (onlyBest && reader_.lastMatchWasExact())
                 break;
         }
 
         if (onlyBest && lastMatch > -1)
             offsets.add(new Long(lastMatch));
 
         if (offsets.size() <= 0) {
             if (err != null)
                 err.println("ERROR: "+label+" not found in .rodata: "+str);
             return null;
         }
 
         return offsets;
     }
 
     public void close() throws IOException {
         reader_.close();
     }
 
     // Functions for Command line interface:
 
     public static String CommandName = "JNI";
     public static String RenameCmd = "rename";
     public static String FindCmd = "find";
 
     public static String Usage() {
         return CommandName+" ["+RenameCmd+"|"+FindCmd+"] <file> ....";
     }
 
     static void PrintUsage() {
         System.out.println("usage: "+CommandLine.Command+" "+CommandName+" "+RenameCmd+
                            " [-o outfile] [-v] <file> <full function signature> <new function name>");
         System.out.println("       "+CommandLine.Command+" "+CommandName+" "+FindCmd+
                            " [-v] <file> <full function signature>");
     }
 
     public static boolean CheckArgs(String args[]) {
         return args.length >= 2 && args[0].equals(CommandName) &&
             (args[1].equals(RenameCmd) || args[1].equals(FindCmd));
     }
 
     public static boolean Help(String args[]) {
         if (args.length >= 2 && args[1].equals(CommandName)) {
             PrintUsage();
             System.out.println();
             System.out.println(CommandName+" "+RenameCmd+" - Tries to rename the given JNI function to the new name");
             System.out.println(" <full function signature> : for example: \"native_drawText(I[CIIFFI)V\"");
             System.out.println(" <new function name> :       for example: \"drawText\"");
             System.out.println(" -o outfile : write output to given file (default is overwrite current file)");
             System.out.println(" -v         : be verbose");
             System.out.println();
             System.out.println(CommandName+" "+FindCmd+"   - Searches for a function with the given name and signature");
             System.out.println("             prints all offsets it is found at and returns 0 on success");
             System.out.println("             returns 1 on failure");
             return true;
         }
         return false;
     }
 
     public static int Run(String args[]) {
         boolean rename = args.length > 1 && (args[1].equals(RenameCmd));
         boolean find = args.length > 1 && (args[1].equals(FindCmd));
         if (args.length < 4 || !CheckArgs(args) || (!rename && !find)) {
             PrintUsage();
             return -1;
         }
 
         boolean verbose = false;
         String outfile = null;
         List<String> params = new LinkedList<String>();
         for (int ii=2; ii<args.length; ++ii) {
             if (args[ii].equals("-o") && (ii+1)<args.length) {
                 outfile = args[++ii];
                 if (!rename) {
                    System.err.print("Warning: invalid argument -o for "+args[1]+" command, ignoring: -o "+outfile);
                     outfile = null;
                 }
             }
             else if (args[ii].equals("-v"))
                 verbose = true;
             else params.add(args[ii]);
         }
 
         int requiredArgs = rename ? 3 : 2;
 
         if (params.size() < requiredArgs) {
             PrintUsage();
             return 1;
         }
         if (params.size() > requiredArgs) {
             System.err.print("Warning: too many parameters, ignoring:");
             for (int ii=requiredArgs; ii<params.size(); ++ii)
                 System.err.print(" "+params.get(ii));
             System.err.println();
         }
         String infile = params.get(0);
         String functionSig = params.get(1);
         String newName = rename ? params.get(2) : null;
 
         File in = new File(infile);
         File out = outfile!=null ? new File(outfile) : in;
 
         // generate temp name and file:
         File temp = null;
         if (rename) {
             int tempCount = 0;
             while (temp == null) {
                 temp = new File(out.getParentFile(),out.getName()+String.format(".temp%04d",tempCount++));
                 if (temp.exists())
                     temp = null;
             }
         }
 
         int status = 0;
         try {
             if (verbose)
                 System.out.println(CommandName+" processing file "+in.getPath()+"...");
 
             if (rename) {
                 copyFile(in,temp);
 
                 JNIRenamer renamer = new JNIRenamer(temp,in.getPath(),false);
 
                 int count = renamer.findRenameFunc(functionSig, newName, System.out,
                                                    verbose ? System.out : null, System.err);
 
                 renamer.close();
 
                 if (count == 0)
                     System.err.println("ERROR: Found no matches.");
                 if (count > 1)
                     System.err.println(String.format("Warning: Found and replaced %d matches?!",count));
 
                 if (count >= 0) {
                     // move temp to out overwritting if necesarry:
                     if (out.exists() && !out.delete()) {
                         System.err.println("Error clearing previous output file "+out.getPath());
                         status = -3;
                     }
                     else if ( !temp.renameTo(out) ) {
                         System.err.println("Error moving temporary file "+temp.getPath()+" to "+out.getPath());
                         status = -3;
                     }
                     System.out.println("Result written to "+out.getPath());
                 } else status = -5;
             }
             else { // find
                 JNIRenamer renamer = new JNIRenamer(in,in.getPath(),true);
 
                 int count = renamer.findRenameFunc(functionSig, newName, System.out,
                                                    verbose ? System.out : null, System.err);
 
                 if (verbose && count == 0)
                     System.out.println("Found no matches.");
                 if (verbose && count > 1)
                     System.out.println(String.format("Warning: Found more than one match (%d matches)",count));
 
                 if (count > 0)
                     status = 0;
                 else if (count == 0)
                     status = 1;
                 else // count < 0
                     status = -5;
             }
 
         } catch (Exception e) {
             System.err.println("Error: "+e.getMessage());
             status = -3;
         } finally {
             if (temp!=null && temp.exists())
                 temp.delete();
         }
 
         return status;
     }
 
     private static void copyFile(File srcF, File trgF) throws IOException {
         FileChannel src = new FileInputStream(srcF).getChannel();
         FileChannel trg = new FileOutputStream(trgF).getChannel();
         trg.transferFrom(src, 0, src.size());
         src.close();
         trg.close();
     }
 }
