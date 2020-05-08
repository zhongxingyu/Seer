 /*
  *  This file is part of the jasm project (http://code.google.com/p/jasm).
  *
  *  This file is licensed to you under the BSD License; You may not use
  *  this file except in compliance with the License. See the LICENSE.txt
  *  file distributed with this work for a copy of the License and information
  *  regarding copyright ownership.
  */
 package jasm.gen.test;
 
 import jasm.gen.AssemblyTestComponent;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import org.realityforge.cli.ArgsParser;
 import org.realityforge.cli.CLUtil;
 import org.realityforge.cli.Option;
 import org.realityforge.cli.OptionDescriptor;
 
 public final class Main {
   private static final int HELP_OPT = 'h';
   private static final int ASSEMBLER_OPT = 'a';
   private static final int GEN_SOURCE_OPT = 1;
   private static final int PATTERN_OPT = 2;
   private static final int START_OPT = 3;
   private static final int END_OPT = 4;
   private static final int NO_EXTERNAL_OPT = 5;
   private static final int NO_DISASSEMBLER_OPT = 6;
   private static final int REMOTE_ACCOUNT_OPT = 7;
   private static final int REMOTE_ASSEMBLER_PATH_OPT = 8;
 
   private static final OptionDescriptor[] OPTIONS = {
       new OptionDescriptor("help",
                            OptionDescriptor.ARGUMENT_DISALLOWED,
                            HELP_OPT,
                            "print this message and exit"),
       new OptionDescriptor("gen-source",
                            OptionDescriptor.ARGUMENT_DISALLOWED,
                            GEN_SOURCE_OPT,
                            "generates source file for each template"),
       new OptionDescriptor("pattern",
                            OptionDescriptor.ARGUMENT_REQUIRED,
                            PATTERN_OPT,
                            "only test templates with the substring in their name"),
       new OptionDescriptor("start",
                            OptionDescriptor.ARGUMENT_REQUIRED,
                            START_OPT,
                            "the serial number of the first template to be tested"),
       new OptionDescriptor("end",
                            OptionDescriptor.ARGUMENT_REQUIRED,
                            END_OPT,
                            "the serial number of the last template to be tested"),
       new OptionDescriptor("assembler",
                            OptionDescriptor.ARGUMENT_REQUIRED,
                            ASSEMBLER_OPT,
                            "the name of assembler to test. amd64, ia32, ppc32, ppc64, sparc32 or sparc64"),
       new OptionDescriptor("remote-account",
                            OptionDescriptor.ARGUMENT_REQUIRED,
                            REMOTE_ACCOUNT_OPT,
                            "the remote account to perform external tests on. ie user@host"),
      new OptionDescriptor("remote-assembler-path",
                            OptionDescriptor.ARGUMENT_REQUIRED,
                            REMOTE_ASSEMBLER_PATH_OPT,
                            "the path to assembler on remote machine. ie /usr/local/bin/"),
 
   };
 
   private static boolean _genSource;
   private static String _pattern;
   private static String _remoteAssemblerPath;
   private static String _remoteUserAndHost;
   private static int _start;
   private static int _end;
   private static AssemblerDef _assemblerDef;
   private static boolean _testExternal = true;
   private static boolean _testDisassembler = true;
 
   private static boolean processArgs(final String[] args) {
     final ArgsParser parser = new ArgsParser(args, OPTIONS);
     if (null != parser.getErrorString()) {
       System.err.println("Error: " + parser.getErrorString());
       return false;
     }
 
     // Get a list of parsed options
     final List options = parser.getArguments();
     final int size = options.size();
 
     for (int i = 0; i < size; i++) {
       final Option option = (Option) options.get(i);
 
       switch (option.getId()) {
         case Option.TEXT_ARGUMENT:
           System.out.println("Unknown arg: " + option.getArgument());
           return false;
 
         case HELP_OPT:
           printUsage();
           return false;
 
         case ASSEMBLER_OPT:
           _assemblerDef = AssemblerDef.valueOf(option.getArgument().toUpperCase());
           if (null == _assemblerDef) {
             System.out.println("Unknown assembler: " + option.getArgument());
           }
           break;
 
         case GEN_SOURCE_OPT:
           _genSource = true;
           break;
 
         case NO_EXTERNAL_OPT:
           _testExternal = false;
           break;
 
         case NO_DISASSEMBLER_OPT:
           _testDisassembler = false;
           break;
 
         case PATTERN_OPT:
           _pattern = option.getArgument();
           break;
 
         case REMOTE_ACCOUNT_OPT:
           _remoteUserAndHost = option.getArgument();
           break;
 
         case REMOTE_ASSEMBLER_PATH_OPT:
           _remoteAssemblerPath = option.getArgument();
           break;
 
         case START_OPT:
           try {
             _start = Integer.parseInt(option.getArgument());
           } catch (NumberFormatException nfe) {
             System.out.println("Start must be an integer.");
             return false;
           }
           break;
 
         case END_OPT:
           try {
             _end = Integer.parseInt(option.getArgument());
           } catch (NumberFormatException nfe) {
             System.out.println("end must be an integer.");
             return false;
           }
           break;
       }
     }
 
     if (null == _assemblerDef) {
       System.out.println("No assembler specified.");
       return false;
     }
 
     return true;
   }
 
   private static void printUsage() {
     final String lineSeparator = System.getProperty("line.separator");
 
     final StringBuffer msg = new StringBuffer();
 
     msg.append(lineSeparator);
     msg.append("Assembler Tester");
     msg.append(lineSeparator);
     msg.append("Usage: java ");
     msg.append(Main.class.getName());
     msg.append(" [options]");
     msg.append(lineSeparator);
     msg.append(lineSeparator);
     msg.append("Options: ");
     msg.append(lineSeparator);
 
     msg.append(CLUtil.describeOptions(OPTIONS));
 
     System.out.println(msg.toString());
   }
 
   public static void main(String[] args) {
     if (!processArgs(args)) {
       System.exit(0);
     }
     System.out.println("Assembler Test Starting");
     System.out.println();
 
     final ArrayList<AssemblyTestComponent> components = new ArrayList<AssemblyTestComponent>(2);
     if( _testDisassembler )
     {
       components.add(AssemblyTestComponent.DISASSEMBLER);
     }
     if( _testExternal )
     {
       components.add(AssemblyTestComponent.EXTERNAL_ASSEMBLER);
     }
 
     try {
       final AssemblyTester tester = newAssemblyTester(EnumSet.copyOf(components));
       final ExternalAssembler assembler = tester.getExternalAssembler();
       assembler.setRemoteUserAndHost(_remoteUserAndHost);
       if (null != _remoteAssemblerPath) assembler.setRemoteAssemblerPath(_remoteAssemblerPath);
       final TemplateSelector selector = tester.getSelector();
       selector.setPattern(_pattern);
       selector.setStartSerial(_start);
       if( 0 != _end ) selector.setEndSerial(_end);
       tester.setCreateExternalSource(_genSource);
       tester.run();
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(42);
     }
     System.exit(0);
   }
 
   private static AssemblyTester newAssemblyTester(EnumSet<AssemblyTestComponent> components)
       throws Exception {
     final String classname = _assemblerDef.getTesterClassname();
     final Constructor constructor =
         Class.forName(classname).getConstructor(EnumSet.class);
     return (AssemblyTester) constructor.newInstance(components);
   }
 }
