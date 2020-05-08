 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 //
 // CmdlineArgs - command line arguments for AlCo
 
 package me.pavlina.alco.compiler;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 /**
  * All command line arguments
  */
 @Parameters(separators = "=")
 public class CmdlineArgs
 {
 
     /** List of source code files */
     @Parameter
     public List<String> sources        = new ArrayList<String> ();
 
     /** Display help and exit */
     @Parameter(names = {"-h", "-help"})
     public boolean      help           = false;
 
     /** Display version information and exit */
     @Parameter(names = "-version")
     public boolean      version        = false;
 
     /** Place the output into &lt;FILE&gt; */
     @Parameter(names = "-o")
     public String       output;
 
     /** Show all commands before executing */
     @Parameter(names = {"-v", "-verbose"})
     public boolean      verbose        = false;
 
     /** Set paths - try -path=help */
     @Parameter(names = "-path")
     public List<String> paths          = new ArrayList<String> ();
 
     /** List paths and exit */
     @Parameter(names = "-list-paths")
     public boolean      list_paths     = false;
 
     /** Include debugging information */
     @Parameter(names = "-g")
     public boolean      debug;
 
     /** Set optimisation level */
     @Parameter(names = "-O", validateWith = OptimisationValidator.class)
     public int          optlevel       = 0;
 
     /** Set machine (32, 64) */
     @Parameter(names = "-m", validateWith = MachineValidator.class)
     public int          machine        = 0;
 
     /** Generate position-independent code */
     @Parameter(names = "-fPIC")
     public boolean      fpic           = false;
 
     /** Link with &lt;lib&gt; */
     @Parameter(names = "-l")
     public List<String> libs           = new ArrayList<String> ();
 
     /** Add &lt;dir&gt; to the library search path */
     @Parameter(names = "-L")
     public List<String> lib_dirs       = new ArrayList<String> ();
 
     /** Add &lt;dir&gt; to the package search path */
     @Parameter(names = "-P")
     public List<String> pkg_dirs       = new ArrayList<String> ();
 
     /** Give &lt;opt&gt; to the LLVM static compiler */
     @Parameter(names = "-llc")
     public List<String> llc_opts       = new ArrayList<String> ();
 
     /** Give &lt;opt&gt; to the assembler */
     @Parameter(names = "-as")
     public List<String> as_opts        = new ArrayList<String> ();
 
     /** Give &lt;opt&gt; to the linker */
     @Parameter(names = "-ld")
     public List<String> ld_opts        = new ArrayList<String> ();
 
     /** Use LLVM formats rather than machine code */
     @Parameter(names = "-emit-llvm")
     public boolean      emit_llvm      = false;
 
     /** Stop after generating assembly */
     @Parameter(names = "-S")
     public boolean      assembly       = false;
 
     /** Stop after assembling */
     @Parameter(names = "-c")
     public boolean      objfile        = false;
 
     /**
      * Run in debug mode. Currently, this means that the compiler will pause and
      * allow inspection of temporary files before processing them.
      */
     @Parameter(names = "-debug-mode")
     public boolean      debug_mode     = false;
 
     /** Print a stack trace for compiler errors */
     @Parameter(names = "-error-trace")
     public boolean      error_trace    = false;
 
     /** Dump the token list after lexing, and quit */
     @Parameter(names = "-tokens")
     public boolean      tokens         = false;
 
     /** Dump the AST after parsing, and quit */
     @Parameter(names = "-ast")
     public boolean      ast            = false;
 
     /** Force compiling on an unsupported platform */
     @Parameter(names = "-force-platform")
     public boolean      force_platform = false;
 }
