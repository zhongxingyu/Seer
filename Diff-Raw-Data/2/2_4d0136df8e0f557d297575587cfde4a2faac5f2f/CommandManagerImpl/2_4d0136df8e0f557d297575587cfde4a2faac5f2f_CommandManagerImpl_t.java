 package de.skuzzle.polly.core.internal.commands;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 import org.apache.log4j.Logger;
 
 
 import de.skuzzle.polly.core.parser.Evaluator;
 import de.skuzzle.polly.core.parser.InputScanner;
 import de.skuzzle.polly.core.parser.ParseException;
 import de.skuzzle.polly.core.parser.Position;
 import de.skuzzle.polly.core.parser.Token;
 import de.skuzzle.polly.core.parser.TokenType;
 import de.skuzzle.polly.core.parser.ast.Identifier;
 import de.skuzzle.polly.core.parser.ast.Root;
 import de.skuzzle.polly.core.parser.ast.declarations.Declaration;
 import de.skuzzle.polly.core.parser.ast.declarations.Namespace;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 import de.skuzzle.polly.core.parser.ast.expressions.Expression;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.ChannelLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.DateLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.ListLiteral;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.Literal;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.UserLiteral;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.util.MillisecondStopwatch;
 import de.skuzzle.polly.core.util.Stopwatch;
 import de.skuzzle.polly.core.util.TypeMapper;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.CommandHistoryEntry;
 import de.skuzzle.polly.sdk.CommandManager;
 import de.skuzzle.polly.sdk.Configuration;
 import de.skuzzle.polly.sdk.IrcManager;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.exceptions.UnknownCommandException;
 import de.skuzzle.polly.sdk.exceptions.UnknownSignatureException;
 import de.skuzzle.polly.sdk.time.DateUtils;
 
 
 
 public class CommandManagerImpl implements CommandManager {
     
     private final static int MIN_COMMAND_NAME_LENGTH = 2;
     
     private class HistoryEntryImpl implements CommandHistoryEntry {
         private Command command;
         private Signature Signature;
         private String name;
         
         
         public HistoryEntryImpl(Command command, Signature signature, String name) {
             this.command = command;
             this.Signature = signature;
             this.name = name;
         }
 
 
 
         @Override
         public Command getCommand() {
             return this.command;
         }
         
         
         
         @Override
         public Signature getSignature() {
             return this.Signature;
         }
         
         
         
         @Override
         public String getExecuterName() {
             return this.name;
         }
     }
     
     
 	private final static String[] DAYS = {"montag", "dienstag", "mittwoch", 
 	    "donnerstag", "freitag", "samstag", "sonntag"};
 	
 	private static Logger logger = Logger.getLogger(CommandManagerImpl.class.getName());
 	private Map<String, Command> commands;
 	private Set<String> ignoredCommands;
 	private String encodingName;
 	
 	
 	/**
 	 * Command history. Key: channel, value: the last command executed on that channel
 	 */
 	private Map<String, CommandHistoryEntry> cmdHistory;
 	
 	
 	public CommandManagerImpl(String encoding, Configuration config) {
 	    this.encodingName = encoding;
 		this.commands = new HashMap<String, Command>();
 		this.ignoredCommands = new HashSet<String>(
 		        config.readStringList(Configuration.IGNORED_COMMANDS));
 		
 		this.cmdHistory = new HashMap<String, CommandHistoryEntry>();
 	}
 	
 	
 	
 	@Override
 	public synchronized void registerCommand(Command cmd) 
 			throws DuplicatedSignatureException {
 		
 	    this.registerCommand(cmd.getCommandName(), cmd);
 	}
 	
 	
 	
 	@Override
 	public synchronized void registerCommand(String as, Command cmd) 
 	        throws DuplicatedSignatureException {
         if (as.length() < MIN_COMMAND_NAME_LENGTH) {
             throw new IllegalArgumentException(
                     "Too short commandname: " + as);
         }
         if (this.ignoredCommands.contains(as)) {
             logger.warn("Ignoring command '" + as + "'.");
             return;
         }
         if (this.isRegistered(as)) {
             throw new DuplicatedSignatureException(as);
         }
         this.commands.put(as.toLowerCase(), cmd);
         logger.debug("Command '" + as + "' with " + 
                 cmd.getSignatures().size() + " signatures successfuly registered");
 	}
 	
 
 	
 	@Override
 	public synchronized void unregisterCommand(Command command) {
 		Command cmd;
         try {
             cmd = this.getCommand(command.getCommandName());
             this.commands.remove(cmd.getCommandName().toLowerCase());
             logger.debug("Unregistered command: " + command.getCommandName());
         } catch (UnknownCommandException e) {
             logger.debug("Tried to unregister nonexistent command", e);
         }
 
 	}
 	
 	
 	
 	@Override
 	public List<Command> getRegisteredCommands() {
 		return Collections.unmodifiableList(
 		    new ArrayList<Command>(this.commands.values()));
 	}
 	
 	
 	
 	@Override
 	public boolean isRegistered(Command cmd) {
 		return this.isRegistered(cmd.getCommandName());
 	}
 	
 	
 	
 	@Override
 	public boolean isRegistered(String name) {
 		return this.commands.containsKey(name.toLowerCase());
 	}
 
 
 
 	@Override
 	public synchronized Command getCommand(Signature signature) 
 	        throws UnknownSignatureException, UnknownCommandException {
 		logger.debug("Looking for '" + signature.toString() + "'.");
 		
 		Command cmd = this.getCommand(signature.getName());
 		boolean found = false;
 		if (signature.equals(cmd.getHelpSignature0()) || signature.equals(cmd.getHelpSignature1())) {
 		    found = true;
 		} else {
     		for (Signature formal : cmd.getSignatures()) {
     			if (formal.equals(signature)) {
     				found = true;
     				signature.setId(formal.getId());
     				logger.debug("Signature found. Formal id is " + signature.getId());
     				break;
     			}
     		}
 		}
 		if (!found) {
 			throw new UnknownSignatureException(signature);
 		}
 		
 		assert cmd != null;
 		return cmd;
 	}
 
 
 
 	@Override
 	public Command getCommand(String name) throws UnknownCommandException {
 		Command cmd = this.commands.get(name.toLowerCase());
 		if (cmd == null)	 {
 			throw new UnknownCommandException(name);
 		}
 		return cmd;
 	}
 
 	
 	
 	@Override
     public void executeString(String input, String channel, boolean inQuery, 
             User executor, IrcManager ircManager) 
                 throws UnsupportedEncodingException, 
                        UnknownSignatureException, InsufficientRightsException, 
                        CommandException, UnknownCommandException {
         Stopwatch watch = new MillisecondStopwatch();
         watch.start();
         
         Root root = null;
         try {
             Map<String, Types> constants = this.getCommandConstants(input);
             
             final Namespace rootNs = Namespace.forName(executor.getName());
             final Namespace workingNs = rootNs.enter();
             
             this.createContext(channel, executor, ircManager, constants, workingNs);
             root = this.parseMessage(input, rootNs, workingNs);
         } catch (ParseException e) {
             // HACK: wrap exception into command exception, as ParseException is not 
             //       available in the sdk
             throw new CommandException(e.getMessage(), e);
         } catch (ASTTraversalException e) {
             // HACK: dito
             throw new CommandException(e.getMessage(), e);
         } 
         
         if (root == null) {
             return;
         }
         Signature sig = this.createSignature(root);
         
         Command cmd = this.getCommand(sig);
         try {
             logger.debug("Executing '" + cmd + "' on channel " + 
                 channel);
             
             cmd.doExecute(executor, channel, inQuery, sig);
             synchronized (this.cmdHistory) {
                 if (cmd.trackInHistory()) {
                     this.cmdHistory.put(channel, new HistoryEntryImpl(cmd, sig, 
                         executor.getCurrentNickName()));
                 }
             }
         } finally {
             watch.stop();
             logger.trace("Execution time: " + watch.getDifference() + "ms");
         }
 	}
 	
 	
 	
 	@Override
 	public CommandHistoryEntry getLastCommand(String channel) {
 	    synchronized (this.cmdHistory) {
             return this.cmdHistory.get(channel);
         }
 	}
 
 
 
 
     private Root parseMessage(String message, Namespace rootNs, Namespace workingNs) 
             throws UnsupportedEncodingException, ASTTraversalException {
     
         final Stopwatch watch = new MillisecondStopwatch();
         watch.start();
         
         final Evaluator eval = new Evaluator(message.trim(), this.encodingName, 
             new SimpleProblemReporter());
         eval.evaluate(rootNs, workingNs);
 
         watch.stop();
         if (eval.getRoot() != null) {
             logger.trace("Parsing time: " + watch.getDifference() + "ms");
         }
         
         final Root root = eval.getRoot();
         if (eval.errorOccurred()) {
             throw eval.getLastError();
         }
         return root;
     }
     
     
     
     private void createContext(String channel, User user, IrcManager ircManager, 
     		Map<String, Types> constants, Namespace d) throws ASTTraversalException {
         
         List<Expression> channels = new ArrayList<Expression>();
         for (String chan : ircManager.getChannels()) {
             channels.add(new ChannelLiteral(Position.NONE, chan));
         }
         
         // ISSUE: 0000008
         List<Expression> users = new ArrayList<Expression>();
         for (String u : ircManager.getChannelUser(channel)) {
             users.add(new UserLiteral(Position.NONE, u));
         }
         d.declare(new Declaration(Position.NONE, new Identifier("me"), 
             new UserLiteral(Position.NONE, user.getCurrentNickName())));
         d.declare(new Declaration(Position.NONE, new Identifier("here"), 
             new ChannelLiteral(Position.NONE, channel)));
         d.declare(new Declaration(Position.NONE, new Identifier("all"), 
             new ListLiteral(Position.NONE, channels, Type.CHANNEL)));
         d.declare(new Declaration(Position.NONE, new Identifier("each"), 
             new ListLiteral(Position.NONE, users, Type.USER)));
         
         int m = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
         d.declare(new Declaration(Position.NONE, new Identifier("morgen"), 
             new DateLiteral(Position.NONE, DateUtils.getDayDate(m + 1))));
         d.declare(new Declaration(Position.NONE, new Identifier("bermorgen"), 
             new DateLiteral(Position.NONE, DateUtils.getDayDate(m + 2))));
         
         
         int start = Calendar.MONDAY;
         for (String day : DAYS) {
             d.declare(new Declaration(Position.NONE, new Identifier(day), 
                 new DateLiteral(Position.NONE, DateUtils.getDayDate(start++))));
         }
         
         /*logger.trace("    me     := " + user.getCurrentNickName());
         logger.trace("    here   := " + channel);
         logger.trace("    all    := " + channels.toString());
         logger.trace("    each   := " + users);
         logger.trace("    morgen := " + tmp.getTime());*/
         
         if (constants != null && !constants.isEmpty()) {
             logger.trace("Command-specific constant names:");
             for (Entry<String, Types> e : constants.entrySet()) {
                 Literal l = TypeMapper.typesToLiteral(e.getValue());
                 d.declare(new Declaration(Position.NONE, 
                     new Identifier(e.getKey()), l));
                 logger.trace("    " + e.getKey() + " := " + l.toString());
             }
         }
     }
     
     
     
     private Signature createSignature(Root root) throws UnknownSignatureException {
         List<Types> parameters = new ArrayList<Types>(root.getResults().size());
         for (Literal lit : root.getResults()) {
             parameters.add(TypeMapper.literalToTypes(lit));
         }
         return new Signature(root.getCommand().getId(), -1, parameters);
     }
 
 
     
     private Map<String, Types> getCommandConstants(String input) {
         try {
            InputScanner s = new InputScanner(input);
             Token id = s.lookAhead();
             if (!id.matches(TokenType.COMMAND)) {
                 return null;
             }
             
             Command cmd = this.getCommand(id.getStringValue());
             
             logger.trace("Renewing command-specific constants");
             cmd.renewConstants();
             return cmd.getConstants();
         } catch (Exception e) {
             return null;
         }
     }
 }
