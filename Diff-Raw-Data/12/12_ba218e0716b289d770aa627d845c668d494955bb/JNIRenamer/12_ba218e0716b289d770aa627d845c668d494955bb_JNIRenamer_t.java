 
 package com.github.erasmux.AndLibUtils;
 
 import java.io.*;
 import java.util.*;
 import java.nio.channels.*;
 
 public class JNIRenamer {
 
     private File file_;
    private String fileName_;
     private ElfReader reader_;
     private long prelinked_;
 
 
    public JNIRenamer(File file,String filename) throws IOException, FileNotFoundException {        
         file_ = file;
        fileName_ = filename;
         
         RandomAccessFile raf = new RandomAccessFile(file,"r");
         prelinked_ = Prelinked.GetPrelinkAddr(raf);
         raf.close();
 
         reader_ = new ElfReader(file_, "rw");
         if (reader_.valid())
             reader_.readSections();
     }
 
     public File file() {
         return file_;
     }
 
     public String filename() {
        return fileName_;
     }
 
     public boolean rename(String functionSig, String newName, PrintStream log, PrintStream err) throws IOException {
         if (!reader_.valid()) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" is not a valid ELF!");
             return false;
         }
         if (prelinked_ <= 0) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" is not prelinked?!");
             return false;
         }
         if (!reader_.hasSection(".data")) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" does not have a .data section?!");
             return false;
         }
         if (!reader_.hasSection(".rodata")) {
             if (err != null)
                 err.println("ERROR: File "+filename()+" does not have a .rodata section?!");
             return false;
         }
 
         long rodataAddr = reader_.sectionAddr(".rodata");
         long rodataBaseAddr = rodataAddr + prelinked_;
         if (log != null) {
             log.println(String.format("file prelinked  @ 0x%08X",prelinked_));
             log.println(String.format(".rodata section @ 0x%08X",rodataAddr));
             log.println(String.format("=> .rodata base @ 0x%08X",rodataBaseAddr));
         }
 
         int splitSig = functionSig.indexOf('(');
         if (splitSig < 0) {
             if (err != null)
                 err.println("ERROR: Invalid function signature: "+functionSig);
             return false;
         }
         String funcName = functionSig.substring(0,splitSig);
         String onlySig = functionSig.substring(splitSig);
 
         Set<Long> funcOffsets = findStringInSection(funcName,".rodata",false,rodataBaseAddr,
                                                     "function name",log,err);
         Set<Long> sigOffsets = findStringInSection(onlySig,".rodata",false,rodataBaseAddr,
                                                    "signature",log,err);
         Set<Long> newOffsets = findStringInSection(newName,".rodata",true,rodataBaseAddr,
                                                    "new function name",log,err);
         if (funcOffsets == null || sigOffsets == null || newOffsets == null)
             return false;
         long newOffset = newOffsets.iterator().next().longValue();
 
         if (log != null)
             log.println("Searching .data for occurences of the function...");
         reader_.seekSection(".data",0);
         long dataAddr = reader_.sectionAddr(".data");
         int count = 0;
         Long lastValue = null;
         while (!reader_.finishedSection()) {
             Long curValue = new Long(reader_.readUInt());
             if (lastValue != null && funcOffsets.contains(lastValue)
                 && sigOffsets.contains(curValue)) {
                 if (log != null)
                     log.println(String.format("  found match @ 0x%08X replace to new offset: 0x%08X",
                                               reader_.currentOffsetInSection()+dataAddr-8,newOffset));
                 reader_.reseek(-8);
                 reader_.writeUInt(newOffset);
                 reader_.skip(4);
                 ++count;
             }
             lastValue = curValue;
         }
 
         if (err != null) {
             if (count == 0)
                 err.println("ERROR: Found no matches.");
             if (count > 1)
                 err.println(String.format("Warning: Found and replaced %d matches?!",count));
         }
         
         return count > 0;
     }
 
     /// searches the given section for the given string returning all the offset it is found at.
     /// if onlyFirst is true only the first match is returned.
     /// label is just for printing message and denotes what string we are looking for.
     private Set<Long> findStringInSection(String str, String section, boolean onlyFirst, long deltaOfs,
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
         long findOfs;
         while ( (findOfs=reader_.seekString(str)) >= 0 ) {
             offsets.add(new Long(findOfs+deltaOfs));
             if (log!=null)
                 log.println(String.format("  found "+label+" @ 0x%08X",findOfs+sectionAddr));
             if (onlyFirst)
                 break;
         }
 
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
 
     public static String CommandName = "renameJNI";
 
     public static String Usage() {
         return CommandName+" [-o outfile] [-v] <file> <full function signature> <new function name>";
     }
 
     static void PrintUsage() {
         System.out.println("usage: "+CommandLine.Command+" "+Usage());
     }
 
     public static boolean CheckArgs(String args[]) {
         return args.length >= 1 &&
             args[0].equals(CommandName);
     }
 
     public static boolean Help(String args[]) {
         if (args.length >= 2 && args[1].equals(CommandName)) {
             PrintUsage();
             System.out.println("Tries to rename the given JNI function to the new name");
             System.out.println(" <full function signature> : for example: \"native_drawText(I[CIIFFI)V\"");
             System.out.println(" <new function name> :       for example: \"drawText\"");
             System.out.println(" -o outfile : write output to given file (default is overwrite current file)");
             System.out.println(" -v         : be verbose");
             return true;
         }
         return false;
     }
 
     public static int Run(String args[]) {
         if (args.length < 4 || !CheckArgs(args)) {
             PrintUsage();
             return 1;
         }
 
         boolean verbose = false;
         String outfile = null;
         List<String> params = new LinkedList<String>();
         for (int ii=1; ii<args.length; ++ii) {
             if (args[ii].equals("-o") && (ii+1)<args.length) {
                 outfile = args[++ii];
             }
             else if (args[ii].equals("-v"))
                 verbose = true;
             else params.add(args[ii]);
         }
 
         if (params.size() < 3) {
             PrintUsage();
             return 1;
         }
         if (params.size() > 3) {
             System.err.print("Warning: too many parameters, ignoring:");
             for (int ii=3; ii<params.size(); ++ii)
                 System.err.print(" "+params.get(ii));
             System.err.println();
         }
         String infile = params.get(0);
         String functionSig = params.get(1);
         String newName = params.get(2);
 
         File in = new File(infile);
         File out = outfile!=null ? new File(outfile) : in;
 
         // generate temp name and file:
         File temp = null;
         int tempCount = 0;
         while (temp == null) {
             temp = new File(out.getParentFile(),out.getName()+String.format(".temp%04d",tempCount++));
             if (temp.exists())
                 temp = null;
         }
 
         int status = 0;
         try {
 
             System.out.println(CommandName+" processing file "+in.getPath()+"...");
 
             copyFile(in,temp);
 
            JNIRenamer renamer = new JNIRenamer(temp,in.getPath());
 
             boolean ok = renamer.rename(functionSig, newName,
                                         verbose ? System.out : null, System.err);
             renamer.close();
 
             if (ok) {
                 // move temp to out overwritting if necesarry:
                 if (out.exists() && !out.delete()) {
                     System.err.println("Error clearing previous output file "+out.getPath());
                     status = 3;
                 }
                 else if ( !temp.renameTo(out) ) {
                     System.err.println("Error moving temporary file "+temp.getPath()+" to "+out.getPath());
                     status = 3;
                 }
                 System.out.println("Result written to "+out.getPath());
             } else status = 5;
 
         } catch (Exception e) {
             System.err.println("Error: "+e.getMessage());
             status = 3;
         } finally {
             if (temp.exists())
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
