 package com.zetapuppis.arguments;
 
 import javax.annotation.Nonnull;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
 * Parses arguments from command line.
  * This is a high level parsing utility and it's the one that should always
  * be used, regardless of the type of parsing needed.
  * <p>
  * This class combines parsers for both {@link PositionalArgument} and
  * {@link SwitchArgument} ({@link PositionalParser} and {@link SwitchParser}
  * respectively) and performs some robustness checks that the other two parsers
  * won't perform.
  * <p>
  * For most of the use cases using this class will be enough, however if you
  * more flexibility it might be useful to use {@link PositionalParser} or
  * {@link SwitchParser} (or both).
  *
  * @see PositionalParser
  * @see SwitchParser
  */
 public class CommandLineParser {
     private final String[] mArgs;
     private final PositionalParser mPositionalParser = new PositionalParser();
     private final SwitchParser mSwitchParser = new SwitchParser();
     private final Set<String> mArgumentNameSet = new HashSet<String>();
     private int mPositionalArgumentsCount = 0;
 
     private CommandLineParser(@Nonnull final String[] args) {
         mArgs = new String[args.length];
         System.arraycopy(args, 0, mArgs, 0, args.length);
     }
 
     /**
      * Creates a new {@link CommandLineParser} instance from the current argument
      * list.
      * @param args input argument list
      * @return an instance of {@link CommandLineParser}
      */
     public static CommandLineParser from(final String[] args) {
         return new CommandLineParser(args);
     }
 
     /**
      * Returns a {@link CommandLineParser} that behaves equivalently to {@code this}
      * {@link CommandLineParser} but that will also parse the given positional
      * keyword.
      *
      * <p>Note that position indices starts at 1.
      * @param name unique name of the positional keyword to parse
      * @param position position of the argument
      * @return a {@link CommandLineParser} with the desired configuration
      * @throws CmdLineException if the specified position is not valid or a keyword
      *         with the same name already exists
      */
     public CommandLineParser addPositional(final String name, final int position) throws CmdLineException {
         return addPositional(new PositionalArgument(name, position));
     }
 
     /**
      * Returns a {@link CommandLineParser} that behaves equivalently to {@code this}
      * {@link CommandLineParser} but that will also parse the given positional
      * keyword.
      *
      * <p>Note that position indices starts at 1.
      * @param positionalArgument an instance of {@link PositionalArgument} that
      *                           represents the argument to be parsed whose name
      *                           it's unique across all the arguments for this parser
      * @return a {@link CommandLineParser} with the desired configuration
      * @throws CmdLineException if the specified position is not valid or a keyword
      *         with the same name already exists
      */
     public CommandLineParser addPositional(final PositionalArgument positionalArgument) throws CmdLineException {
         if (mArgumentNameSet.contains(positionalArgument.getName())) {
             throw new CmdLineException(
                     String.format("'%s' is a duplicate argument name for positional argument", positionalArgument.getName()));
         }
         mArgumentNameSet.add(positionalArgument.getName());
         mPositionalParser.addPositional(positionalArgument);
         mPositionalArgumentsCount++;
 
         return this;
     }
 
     /**
      * Returns a {@link CommandLineParser} that behaves equivalently to {@code this}
      * {@link CommandLineParser} but that will also parse the given switch-based
      * argument.
      * @param name unique name of the switch argument to parse
      * @param hasValue if the argument requires a value right afterwards
      * @param isRequired if the argument is mandatory
      * @return a {@link CommandLineParser} with the desired configuration
      * @throws CmdLineException if a switch argument with the same name already exists
      */
     public CommandLineParser addSwitch(final String name,
                                        final boolean hasValue,
                                        final boolean isRequired) throws CmdLineException {
         return addSwitch(new SwitchArgument(name, hasValue, isRequired));
     }
 
     /**
      * Returns a {@link CommandLineParser} that behaves equivalently to {@code this}
      * {@link CommandLineParser} but that will also parse the given switch-based
      * argument.
      * @param name unique name of the switch argument to parse
      * @param shortName short version of the same switch argument
      * @param hasValue if the argument requires a value right afterwards
      * @param isRequired if the argument is mandatory
      * @return a {@link CommandLineParser} with the desired configuration
      * @throws CmdLineException if a switch argument with the same name already exists
      */
     public CommandLineParser addSwitch(final String name,
                                        final String shortName,
                                        final boolean hasValue,
                                        final boolean isRequired) throws CmdLineException {
         return addSwitch(new SwitchArgument(name, shortName, hasValue, isRequired));
     }
 
     /**
      * Returns a {@link CommandLineParser} that behaves equivalently to {@code this}
      * {@link CommandLineParser} but that will also parse the given switch-based
      * argument.
      * @param switchArgument an instance of {@link SwitchArgument} that
      *                       represents the argument to be parsed whose name it's
      *                       unique across all the arguments for this parser
      * @return a {@link CommandLineParser} with the desired configuration
      * @throws CmdLineException if a switch argument with the same name already exists
      */
     public CommandLineParser addSwitch(final SwitchArgument switchArgument) throws CmdLineException {
         if (mArgumentNameSet.contains(switchArgument.getName())) {
             throw new CmdLineException(
                     String.format("'%s' is a duplicate argument name for keyword", switchArgument.getName()));
         }
         mArgumentNameSet.add(switchArgument.getName());
         mSwitchParser.addSwitch(switchArgument);
         return this;
     }
 
     /**
      * Parse the command line string list.
      * @return a {@link ParsedArguments} instance whose fields have been valorized
      *         with the parsed argument's values
      * @throws SwitchArgumentException if some error happens while parsing switch-based
      *         arguments
      * @throws PositionalArgumentException if some error happens while parsing
      *         positional keywords
      */
     public ParsedArguments parse() throws SwitchArgumentException, PositionalArgumentException {
         final ParsedArguments parsed = new ParsedArguments();
         String[] argsCopy = new String[mArgs.length];
         System.arraycopy(mArgs, 0, argsCopy, 0, mArgs.length);
 
         final ParsedArguments parsedPositions = mPositionalParser.parse(argsCopy);
         for (Map.Entry<String, String> k : parsedPositions) {
             parsed.set(k.getKey(), k.getValue());
         }
 
         argsCopy = LineUtils.shiftArgs(argsCopy, mPositionalArgumentsCount);
 
         final ParsedArguments parsedSwitches = mSwitchParser.parse(argsCopy);
         for (Map.Entry<String, String> o : parsedSwitches) {
             parsed.set(o.getKey(), o.getValue());
         }
 
         return parsed;
     }
 }
