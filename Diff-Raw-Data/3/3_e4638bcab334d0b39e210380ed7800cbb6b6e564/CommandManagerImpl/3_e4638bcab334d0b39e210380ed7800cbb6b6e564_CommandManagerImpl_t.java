 package polly.core.commands;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Stack;
 
 
 import org.apache.log4j.Logger;
 
 import polly.configuration.PollyConfiguration;
 import polly.core.users.UserManagerImpl;
 import polly.util.MillisecondStopwatch;
 import polly.util.Stopwatch;
 import polly.util.TypeMapper;
 
 import de.skuzzle.polly.parsing.AbstractParser;
 import de.skuzzle.polly.parsing.InputScanner;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.PollyParserFactory;
 import de.skuzzle.polly.parsing.SyntaxMode;
 import de.skuzzle.polly.parsing.Token;
 import de.skuzzle.polly.parsing.TokenType;
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.declarations.Namespace;
 import de.skuzzle.polly.parsing.declarations.VarDeclaration;
 import de.skuzzle.polly.parsing.tree.Expression;
 import de.skuzzle.polly.parsing.tree.Root;
 import de.skuzzle.polly.parsing.tree.literals.ChannelLiteral;
 import de.skuzzle.polly.parsing.tree.literals.DateLiteral;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.literals.ListLiteral;
 import de.skuzzle.polly.parsing.tree.literals.Literal;
 import de.skuzzle.polly.parsing.tree.literals.UserLiteral;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.CommandHistoryEntry;
 import de.skuzzle.polly.sdk.CommandManager;
 import de.skuzzle.polly.sdk.IrcManager;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.exceptions.UnknownCommandException;
 import de.skuzzle.polly.sdk.exceptions.UnknownSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 
 public class CommandManagerImpl implements CommandManager {
     
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
     
     
 	
 	private static Logger logger = Logger.getLogger(CommandManagerImpl.class.getName());
 	private Map<String, Command> commands;
 	private Set<String> ignoredCommands;
 	private UserManagerImpl userManager;
 	private PollyConfiguration config;
 	
 	/**
 	 * Command history. Key: channel, value: the last command executed on that channel
 	 */
 	private Map<String, CommandHistoryEntry> cmdHistory;
 	
 	
 	public CommandManagerImpl(UserManagerImpl userManager, PollyConfiguration config) {
 	    this.userManager = userManager;
 	    this.config = config;
 		this.commands = new HashMap<String, Command>();
 		this.ignoredCommands = new HashSet<String>(
 		        Arrays.asList(config.getIgnoredCommands()));
 		
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
         if (as.length() < 3) {
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
         this.commands.put(as, cmd);
         logger.debug("Command '" + as + "' with " + 
                 cmd.getSignatures().size() + " signatures successfuly registered");
 	}
 	
 
 	
 	@Override
 	public synchronized void unregisterCommand(Command command) {
 		Command cmd;
         try {
             cmd = this.getCommand(command.getCommandName());
             this.commands.remove(cmd.getCommandName());
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
 		return this.commands.containsKey(name);
 	}
 
 
 
 	@Override
 	public synchronized Command getCommand(Signature signature) 
 	        throws UnknownSignatureException, UnknownCommandException {
 		logger.debug("Looking for '" + signature.toString() + "'.");
 		
 		Command cmd = this.getCommand(signature.getName());
 		boolean found = false;
 		for (Signature formal : cmd.getSignatures()) {
 			if (formal.equals(signature)) {
 				found = true;
 				signature.setId(formal.getId());
 				logger.debug("Signature found. Formal id is " + signature.getId());
 				break;
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
 		Command cmd = this.commands.get(name);
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
         
         Namespace copy = null;
         Root root = null;
         try {
             Map<String, Types> constants = this.getCommandConstants(input);
             
             // get namespace and create copy for executor
             Namespace ns = this.userManager.getNamespace();
             copy = ns.copyFor(executor.getName());
             copy.enter();
             
             this.createContext(channel, executor, ircManager, constants, copy);
             
             root = this.parseMessage(input, copy);
         } catch (ParseException e) {
             // HACK: wrap exception into command exception, as ParseException is not 
             //       available in the sdk
             throw new CommandException(e.getMessage(), e);
         } finally {
         	if (copy != null) {
 				copy.leave();
         	}
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
 
 
 
 
     private Root parseMessage(String message, Namespace namespace) 
         throws UnsupportedEncodingException, ParseException {
     
         Stopwatch watch = new MillisecondStopwatch();
         watch.start();
         
         try {
             AbstractParser<?> parser = PollyParserFactory.createParser(
                     SyntaxMode.POLLY_CLASSIC);
             
             Root root = (Root) parser.parse(message.trim(), 
                 this.config.getEncodingName()); 
             
             if (root == null) {
                 return null;
             }
         
             logger.trace("Parsed input '" + message + "'");
             root.contextCheck(namespace);
             
             logger.trace("Collapsing all parameters");
             root.collapse(new Stack<Literal>());
 
             watch.stop();
             logger.trace("Parsing time: " + watch.getDifference() + "ms");
             
             return root;
         } catch (Exception e) {
             watch.stop();
             logger.trace("Parsing time: " + watch.getDifference() + "ms");
         }
         return null;
     }
 
 
 
     private void createContext(String channel, User user, IrcManager ircManager, 
     		Map<String, Types> constants, Namespace d) throws ParseException {
         
         List<Expression> channels = new ArrayList<Expression>();
         for (String chan : ircManager.getChannels()) {
             channels.add(new ChannelLiteral(chan));
         }
         
         // ISSUE: 0000008
         List<Expression> users = new ArrayList<Expression>();
         for (String u : ircManager.getChannelUser(channel)) {
             users.add(new UserLiteral(u));
         }
         d.addNormal(new VarDeclaration(new IdentifierLiteral("me"), 
         		new UserLiteral(user.getCurrentNickName()), true));
         d.addNormal(new VarDeclaration(new IdentifierLiteral("here"), 
         		new ChannelLiteral(channel), true));
         d.addNormal(new VarDeclaration(new IdentifierLiteral("all"), 
         		new ListLiteral(channels, Type.CHANNEL), true));
         d.addNormal(new VarDeclaration(new IdentifierLiteral("each"), 
         		new ListLiteral(users, Type.USER), true));
         
         Calendar tmp = Calendar.getInstance();
         tmp.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH) + 1);
         tmp.set(Calendar.HOUR, 0);
         tmp.set(Calendar.HOUR_OF_DAY, 0);
         tmp.set(Calendar.MINUTE, 0);
         tmp.set(Calendar.SECOND, 0);
         tmp.set(Calendar.MILLISECOND, 0);
         d.addNormal(new VarDeclaration(new IdentifierLiteral("morgen"), 
             new DateLiteral(tmp.getTime()), true));
         
         /*logger.trace("    me     := " + user.getCurrentNickName());
         logger.trace("    here   := " + channel);
         logger.trace("    all    := " + channels.toString());
         logger.trace("    each   := " + users);
         logger.trace("    morgen := " + tmp.getTime());*/
         
         if (constants != null && !constants.isEmpty()) {
             logger.trace("Command-specific constant names:");
             for (Entry<String, Types> e : constants.entrySet()) {
                 Literal l = TypeMapper.typesToLiteral(e.getValue());
                d.addNormal(new VarDeclaration(new IdentifierLiteral(e.getKey()), l, 
                    true));
                 logger.trace("    " + e.getKey() + " := " + l.toString());
             }
         }
     }
     
     
     
     private Signature createSignature(Root root) throws UnknownSignatureException {
         List<Types> parameters = new ArrayList<Types>();
         for (Literal lit : root.getResults()) {
             parameters.add(TypeMapper.literalToTypes(lit));
         }
         return new Signature(root.getName().getCommandName(), -1, parameters);
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
