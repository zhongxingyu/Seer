 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 //
 // Command line helpers - usage string, etc.
 
 package me.pavlina.alco.compiler;
 import me.pavlina.alco.ProgramInfo;
 
 /**
  * Helper strings and functions for the command line UI.
  */
 public class CmdlineHelpers {
     /**
      * Usage string, to be printed on -help */
     public static final String usage =
         "Usage: alco [options] SOURCES...\n"
       + "\n"
       + "  Options:\n"
       + "    -h, -help         display help and exit\n"
       + "    -version          display version information and exit\n"
       + "    -o <file>         place the output into <FILE>\n"
       + "    -v, -verbose      show all commands before executing\n"
       + "\n"
       + "    -path <PATH>      set paths - try -path=help\n"
       + "    -list-paths       list paths and exit\n"
       + "\n"
       + "    -llc <opt>        give <opt> to the LLVM static compiler\n"
       + "    -as <opt>         give <opt> to the assembler\n"
       + "    -ld <opt>         give <opt> to the linker\n"
       + "    -g                include debugging information\n"
       + "    -O <n>            set optimisation level (0, 1, 2, 3)\n"
       + "    -m <bits>         set machine (32, 64)\n"
       + "    -fPIC             generate position-independent code (implicit\n"
       + "                      with non-executable packages)\n"
       + "    -l <lib>          link with <lib>\n"
       + "    -L <dir>          add <dir> to the library search path\n"
       + "    -P <dir>          add <dir> to the package search path\n"
       + "    -emit-llvm        use LLVM formats rather than machine code\n"
       + "    -S                stop after generating assembly\n"
       + "    -c                stop after assembling\n"
       + "------------------------------------------------------------------\n"
       + "    -debug-mode       run in debug mode\n"
       + "    -error-trace      print a stack trace for compiler errors\n"
       + "    -tokens           dump the token list after lexing, and quit\n"
       + "    -ast              dump the AST after parsing, and quit\n"
       + "    -force-platform   force compiling on an unsupported platform\n";
 
     /**
      * Short usage string, to be printed on invalid option */
     public static final String short_usage =
         "Usage: alco [options] SOURCES...\n\n";
 
     /**
      * Version string, to be printed on -version */
     public static final String version_string =
         "alco: Alpha compiler version %s\n"
       + "Copyright (c) %d, %s.\n";
 
     /**
      * Paths help, to be printed on -path=help */
     public static final String paths_help =
         "The -path argument can be used to give paths to files at compile\n"
       + "time. The format is: -path=key:value, where the valid keys are:\n"
       + "  llc, llvm-as, as, ld, crt1-64, crti-64, crtn-64, ldso-64,\n"
       + "  crt1-32, crti-32, crtn-32, ldso-32, runtime-64, runtime-32\n";
 
     /**
      * Check for the "dump and quit" options, and run them. This handles -help,
      * -version, and -paths=help.
      */
     public static void handle_dump_options (CmdlineArgs args)
     {
         if (args.help) {
             System.err.print (CmdlineHelpers.usage);
             System.exit (0);
         }
         if (args.version) {
             System.err.printf (CmdlineHelpers.version_string,
                                ProgramInfo.version, ProgramInfo.copyrightyear,
                                ProgramInfo.copyrightcredit);
             System.exit (0);
         }
         if (args.paths.contains ("help")) {
             System.err.print (CmdlineHelpers.paths_help);
             System.exit (0);
         }
     }
 }
