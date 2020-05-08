 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 //
 // Compiler - main compiler class
 
 package me.pavlina.alco.compiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.nio.channels.FileChannel;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Scanner;
 import me.pavlina.alco.ast.AST;
 import me.pavlina.alco.ast.Package;
 import me.pavlina.alco.compiler.errors.CError;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.lex.Lexer;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.llvm.Emitter;
 import me.pavlina.alco.passes.ConstantFold;
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 
 /**
  * Main compiler class. This contains the command line UI and runs the compile
  * sequence.
  */
 public class Compiler
 {
 
     CmdlineArgs                args;
     Env                        env;
     AST                        ast;
     Map<String, String>        paths;
     Map<File, TokenStream>     streams;
     int                        bits;
     File                       lastFile, llFile, bcFile, sFile, oFile, xFile;
 
     /**
      * Run the compiler.
      * @param argv Command line arguments
      */
     public int run (String[] argv)
     {
         // Read arguments
         args = new CmdlineArgs ();
         JCommander jc = new JCommander (args);
         jc.setProgramName ("alco");
         try {
             jc.parse (argv);
         } catch (ParameterException e) {
             System.err.print (CmdlineHelpers.short_usage);
             System.err.println (e.getMessage ());
             return 1;
         }
         CmdlineHelpers.handle_dump_options (args);
 
         // Setup
         int rc;
         if ((rc = this.detect_machine ()) != 0) return rc;
         if ((rc = this.detect_paths ()) != 0) return rc;
         if (args.list_paths) {
             this.list_paths ();
             return 0;
         }
         if (args.sources.isEmpty ()) {
             System.err.println ("Error: no sources to compile");
             return 1;
         }
         for (String i: args.sources) {
             if (!i.endsWith (".al") && !i.endsWith (".o")) {
                 System.err.println ("Unknown source type: " + i);
                 return 1;
             }
         }
         if ((rc = this.check_paths ()) != 0) return rc;
 
         // Compile
         this.create_env ();
         if ((rc = this.lex ()) != 0) return rc;
         if (args.tokens) {
             // Debug option: dump tokens and quit
             return this.dump_tokens ();
         }
         if ((rc = this.parse ()) != 0) return rc;
 
         if (args.pre_ast) {
             // Debug option: dump AST and quit
             return this.dump_ast ();
         }
 
         // Constant arithmetic folding
         if ((rc = this.constantFold ()) != 0) return rc;
 
         // Resolution/checking
         if ((rc = this.checkTypes ()) != 0) return rc;
 
         if (args.ast) {
             // Debug option: dump AST and quit
             return this.dump_ast ();
         }
 
         // Generate LLVM
         if ((rc = this.genLLVM ()) != 0) return rc;
 
         // Compile
         if ((rc = this.genBitcode ()) != 0) return rc;
         if ((rc = this.genAssembly ()) != 0) return rc;
         if ((rc = this.genObject ()) != 0) return rc;
         if ((rc = this.genLinked ()) != 0) return rc;
 
         if ((rc = this.moveFinalFile ()) != 0) return rc;
 
         return 0;
     }
 
     private void copyFile (File sourceFile, File destFile)
         throws IOException
     {
         if (args.verbose) {
             System.out.println (sourceFile.getPath () + " -> " +
                                 destFile.getPath ());
         }
 
         destFile.createNewFile ();
 
         FileChannel src = null, dest = null;
         try {
             src = new FileInputStream (sourceFile).getChannel ();
             dest = new FileOutputStream (destFile).getChannel ();
             dest.transferFrom (src, 0, src.size ());
         } finally {
             if (src != null)
                 src.close ();
             if (dest != null)
                 dest.close ();
         }
     }
 
     /**
      * Detect the machine. Sets this.bits to 32 or 64, and whines if not on
      * Linux.
      * @return nonzero on error
      */
     private int detect_machine ()
     {
         if (!System.getProperty ("os.name").equals ("Linux")) {
             if (args.force_platform) {
                 System.err.println ("Warning: -force-platform: "
                                     + "compiling on non-supported OS "
                                     + System.getProperty ("os.name"));
                 if (args.machine == 0) {
                     System.err.println ("Error: -force-platform: "
                                         + "specify machine with -m");
                     return 1;
                 } else {
                     bits = args.machine;
                 }
             } else {
                 System.err.println ("Error: compiling on non-supported OS "
                                     + System.getProperty ("os.name"));
                 return 1;
             }
         } else if (args.machine == 0) {
             // Detect machine on Linux
             String mach;
             try {
                 Process pr = Runtime.getRuntime ().exec ("uname -m");
                 byte[] b = new byte[15];
                 InputStream in = pr.getInputStream ();
                 int read = in.read (b);
                 mach = new String (b, 0, read);
             } catch (Exception e) {
                 System.err.println (e);
                 return 1;
             }
             if (mach.equals ("x86_64\n")) {
                 bits = 64;
             } else if (mach.length () == 5 && mach.startsWith ("i")
                        && mach.endsWith ("86\n")) {
                 bits = 32;
             } else {
                 System.err.println ("Error: cannot detect machine");
                 return 1;
             }
         } else {
             bits = args.machine;
         }
         return 0;
     }
 
     /**
      * Detect and configure paths. Fills in this.paths.
      * @return nonzero on error
      */
     private int detect_paths ()
     {
         // Load in deafults
         paths = new HashMap<String, String> ();
         DefaultPaths.paths (paths);
         String config_file;
 
         // Load config files
         File ofdc_config = new File ("/etc/ofdc/config");
         if ((config_file = System.getenv ("OFDC_CONFIG")) != null) {
             if (ConfigFile.read (new File (config_file), paths) != 0) return 1;
         } else if (ofdc_config.exists ()) {
             if (ConfigFile.read (ofdc_config, paths) != 0) return 1;
         }
 
         // Load command line arguments
         for (String i: args.paths) {
             int idx = i.indexOf (':');
             if (idx == -1) {
                 System.err.println ("Warning: malformed -path argument " + i);
                 continue;
             }
             String key = i.substring (0, idx);
             String val = i.substring (idx + 1);
             if (key.length () == 0 || val.length () == 0) {
                 System.err.println ("Warning: malformed -path argument " + i);
                 continue;
             }
             paths.put (key, val);
         }
 
         // Map generic keys to machine keys
         if (bits == 32) {
             paths.put ("crt1", paths.get ("crt1-32"));
             paths.put ("crti", paths.get ("crti-32"));
             paths.put ("crtn", paths.get ("crtn-32"));
             paths.put ("ldso", paths.get ("ldso-32"));
             paths.put ("runtime", paths.get ("runtime-32"));
         } else if (bits == 64) {
             paths.put ("crt1", paths.get ("crt1-64"));
             paths.put ("crti", paths.get ("crti-64"));
             paths.put ("crtn", paths.get ("crtn-64"));
             paths.put ("ldso", paths.get ("ldso-64"));
             paths.put ("runtime", paths.get ("runtime-64"));
         }
         return 0;
     }
 
     /**
      * Make sure all required paths can be accessed.
      * @return nonzero on error
      */
     private int check_paths ()
     {
         String[] keys_all = {"llc", "llvm-as", "as", "ld"};
         String[] keys_link = {"crt1", "crti", "crtn", "ldso", "runtime"};
         for (String key: keys_all) {
             File f = new File (paths.get (key));
             if (!f.exists ()) {
                 System.err.println ("Error: missing path " + f.toString ());
                 return 1;
             }
         }
         if (args.emit_llvm || args.assembly || args.objfile) return 0;
         for (String key: keys_link) {
             File f = new File (paths.get (key));
             if (!f.exists ()) {
                 System.err.println ("Error: missing path " + f.toString ());
                 return 1;
             }
         }
         return 0;
     }
 
     /**
      * Dump a list of all paths and whether they are accessible
      */
     private void list_paths ()
     {
         String[] keys =
                 {"llc", "llvm-as", "as", "ld", "crt1-32", "crti-32", "crtn-32",
                         "ldso-32", "runtime-32", "crt1-64", "crti-64",
                         "crtn-64", "ldso-64", "runtime-64"};
         for (String key: keys) {
             File f = new File (paths.get (key));
             if (f.exists ()) {
                 System.out.printf ("%-10s (PRESENT): %s\n", key, f.toString ());
             } else {
                 System.out.printf ("%-10s (MISSING): %s\n", key, f.toString ());
             }
         }
     }
 
     /**
      * Create the compile environment (Env) object, to be passed to all compile
      * stages.
      */
     private void create_env ()
     {
         env = new Env (System.out, System.err, bits, args.debug);
     }
 
     /**
      * Run the lex stage. Note that this really only prepares the lexers; AlCo
      * uses a lazy lexer that reads each token as requested.
      * @return nonzero on error
      */
     private int lex ()
     {
         streams = new HashMap<File, TokenStream> ();
         for (String i: args.sources) {
             if (!i.endsWith (".al")) continue;
             File file = new File (i);
             Lexer lexer;
             TokenStream stream;
             try {
                 lexer = new Lexer (file);
             } catch (IOException e) {
                 System.err.println (e);
                 streams.clear ();
                 return 1;
             }
             try {
                 lexer.lex ();
             } catch (CError e) {
                 e.print (System.err);
                 if (args.error_trace) e.printStackTrace ();
                 return 1;
             }
             stream = new TokenStream (lexer);
             streams.put (file, stream);
         }
         return 0;
     }
 
     /**
      * Run the parse stage. Sets this.ast.
      * @return nonzero on error
      */
     private int parse ()
     {
         try {
             for (Map.Entry<File, TokenStream> i: streams.entrySet ()) {
                 Package pkg = new Package (i.getValue (), env);
                 if (ast == null)
                     ast = pkg;
                 else {
                     ((Package) ast).merge (pkg, env);
                 }
             }
         } catch (CError e) {
             e.print (System.err);
             if (args.error_trace) e.printStackTrace ();
             return 1;
         }
         return 0;
     }
 
     /**
      * Run the resolution/type-checking stage.
      * @return nonzero on error
      */
     private int checkTypes ()
     {
         try {
             Resolver resolver = new Resolver ();
             ast.checkTypes (env, resolver);
         } catch (CError e) {
             e.print (System.err);
             if (args.error_trace) e.printStackTrace ();
             return 1;
         }
         return 0;
     }
 
     /**
      * Run the constant arithmetic folding pass
      * @return nonzero on error
      */
     private int constantFold ()
     {
         try {
             ast = ConstantFold.run (ast);
         } catch (CError e) {
             e.print (System.err);
             if (args.error_trace) e.printStackTrace ();
             return 1;
         }
         return 0;
     }
 
     /**
      * Run the LLVM generation stage.
      */
     private int genLLVM ()
     {
         // Generate LLVM code
         Emitter emitter = new Emitter ();
         ast.genLLVM (env, emitter, null);
 
         // Emit it to a temporary file
         PrintStream llStream;
         try {
             lastFile = llFile = File.createTempFile ("alco", ".ll");
             llFile.deleteOnExit ();
             llStream = new PrintStream (llFile);
         } catch (IOException e) {
             System.err.println (e);
             return 1;
         }
         llStream.print (emitter);
         llStream.flush ();
 
         // Inspect?
         if (args.debug_mode) {
             System.out.println ("LLVM is at " + llFile.getPath ());
             System.out.println ("Inspect/modify, then press enter.");
             Scanner sc = new Scanner (System.in);
             while (!sc.nextLine ().equals (""));
         }
 
         return 0;
     }
 
     /**
      * Run the bitcode generation stage, if desired. */
     private int genBitcode ()
     {
         if (!(args.emit_llvm && !args.assembly)) return 0;
 
         try {
             lastFile = bcFile = File.createTempFile ("alco", ".bc");
             bcFile.deleteOnExit ();
             int rc = this.exec (paths.get ("llvm-as"), llFile.getPath (), "-o",
                               bcFile.getPath ());
             return rc;
         } catch (IOException e) {
             System.err.println (e);
             return 1;
         }
     }
 
     /**
      * Run the assembly generation stage, if desired. */
     private int genAssembly ()
     {
         if (args.emit_llvm) return 0;
 
         String march, optlevel;
         if (bits == 32)
             march = "x86";
         else if (bits == 64)
             march = "x86-64";
         else
             throw new RuntimeException
                 ("Don't know -march argument for architecture");
         switch (args.optlevel) {
         case 0: optlevel = "-O=0"; break;
         case 1: optlevel = "-O=1"; break;
         case 2: optlevel = "-O=2"; break;
         case 3: optlevel = "-O=3"; break;
         default:
             throw new RuntimeException ("Invalid optimisation level");
         }
 
         try {
             lastFile = sFile = File.createTempFile ("alco", ".s");
             int rc;
             sFile.deleteOnExit ();
             if (args.fpic || !((Package) ast).isExecutable ())
                 rc = this.exec (paths.get ("llc"), llFile.getPath (), "-o",
                                 sFile.getPath (), "-march", march,
                                 "-relocation-model=pic", optlevel);
             else
                 rc = this.exec (paths.get ("llc"), llFile.getPath (), "-o",
                                 sFile.getPath (), "-march", march, optlevel);
             return rc;
         } catch (IOException e) {
             System.err.println (e);
             return 1;
         }
     }
 
     /**
      * Run the object file generation stage, if desired. */
     private int genObject ()
     {
         if (args.emit_llvm || args.assembly) return 0;
 
         String wordSzArg;
         if (bits == 32)
             wordSzArg = "--32";
         else if (bits == 64)
             wordSzArg = "--64";
         else
             throw new RuntimeException
                 ("Don't know word size argument for architecture");
 
         try {
             lastFile = oFile = File.createTempFile ("alco", ".o");
             oFile.deleteOnExit ();
             int rc = this.exec (paths.get ("as"), sFile.getPath (), "-o",
                                 oFile.getPath (), wordSzArg);
             return rc;
         } catch (IOException e) {
             System.err.println (e);
             return 1;
         }
     }
 
     /**
      * Run the link phase, if desired */
     private int genLinked ()
     {
         if (args.objfile || args.assembly || args.emit_llvm) return 0;
 
         String emul;
         if (bits == 32)
             emul = "elf_i386";
         else if (bits == 64)
             emul = "elf_x86_64";
         else
             throw new RuntimeException
                 ("Don't know linker emulation mode for architecture");
 
         List<String> ldArgs = new ArrayList<String> ();
 
         boolean isExecutable = ((Package) ast).isExecutable ();
         try {
             lastFile = xFile = File.createTempFile
                 ("alco", isExecutable ? null : ".so");
             xFile.deleteOnExit ();
             ldArgs.add (paths.get ("ld"));
             ldArgs.add ("-m");
             ldArgs.add (emul);
             if (isExecutable)
                 ldArgs.add (paths.get ("crt1"));
             ldArgs.add (paths.get ("crti"));
             ldArgs.add (oFile.getPath ());
             for (String i: args.sources) {
                 if (i.endsWith (".o"))
                     ldArgs.add (i);
             }
             ldArgs.add (paths.get ("crtn"));
             ldArgs.add ("-lc");
             ldArgs.add ("-lm");
             ldArgs.add ("-lgc");
             if (!isExecutable) {
                 ldArgs.add ("-soname");
                 ldArgs.add (((Package) ast).getName () + ".alpha.so");
                 ldArgs.add ("-shared");
             }
             ldArgs.add ("-dynamic-linker");
             ldArgs.add (paths.get ("ldso"));
             ldArgs.add ("-o");
             ldArgs.add (xFile.getPath ());
             int rc = this.exec (ldArgs.toArray (new String[ldArgs.size ()]));
             return rc;
         } catch (IOException e) {
             System.err.println (e);
             return 1;
         }
     }
 
     /**
      * Move the final file to its destination */
     private int moveFinalFile ()
     {
         String fileName, pkgName;
         boolean executable = false;
 
         pkgName = ((Package) ast).getName ();
         if (args.output != null) {
            if (!args.objfile && !args.emit_llvm && !args.assembly)
                executable = true;
             fileName = args.output;
         } else {
             if (!args.objfile && !args.emit_llvm && !args.assembly) {
                 executable = true;
                 if (((Package) ast).isExecutable ())
                     fileName = pkgName;
                 else
                     fileName = pkgName + ".alpha.so";
             } else if (args.objfile && !args.emit_llvm && !args.assembly) {
                 fileName = pkgName + ".o";
             } else if (!args.emit_llvm && args.assembly) {
                 fileName = pkgName + ".s";
             } else if (args.emit_llvm && !args.assembly) {
                 fileName = pkgName + ".bc";
             } else if (args.emit_llvm && args.assembly) {
                 fileName = pkgName + ".ll";
             } else {
                 throw new RuntimeException ("Missed a case");
             }
         }
 
         File outFile = new File (fileName);
 
         try {
             this.copyFile (lastFile, outFile); 
         } catch (IOException e) {
             System.err.println (e);
             return 1;
         }
         
         if (executable)
             outFile.setExecutable (true, false);
 
         return 0;
     }
 
     private int exec (String... args) throws IOException
     {
         if (this.args.verbose) {
             boolean first = true;
             for (String i: args) {
                 if (first) first = false;
                 else System.out.print (" ");
                 System.out.print (i);
             }
             System.out.println ();
         }
         Process cmd = new ProcessBuilder (args)
             .redirectErrorStream (true)
             .start ();
         InputStream inp = cmd.getInputStream ();
         while (true) {
             int ch = inp.read ();
             if (ch == -1) break;
             System.err.write (ch);
         }
         System.err.flush ();
         int rc;
         try {
             rc = cmd.waitFor ();
         } catch (InterruptedException e) {
             System.err.println (e);
             return 1;
         } finally {
             Thread.interrupted ();
         }
         return rc;
     }
 
     /**
      * Print out a list of all tokens.
      * @return nonzero on error
      */
     private int dump_tokens ()
     {
         for (TokenStream stream: streams.values ()) {
             Token token;
             while (!(token = stream.next ()).is (Token.NO_MORE)) {
                 System.out.println (token);
             }
         }
         return 0;
     }
 
     /**
      * Print out a representation of the AST.
      * @return zero
      */
     private int dump_ast ()
     {
         ast.print (System.out);
         return 0;
     }
 
     /**
      * Instantiate and run the compiler.
      */
     public static void main (String[] argv)
     {
         Compiler c = new Compiler ();
         // Any respectable compiler must return an exit code
         System.exit (c.run (argv));
     }
 }
