 package models;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import util.Utils;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamImplicit;
 
 import exception.CommandException;
 
 /**
  * Execute a sequence of commands 
  *  
  * @author Paolo Di Tommaso
  *
  */
 @XStreamAlias("process")
 public class ProcessCommand extends AbstractCommand implements Serializable {
 
 	@XStreamImplicit
 	public List<AbstractCommand> commands;
 	
 	/** The default constructor */
 	public ProcessCommand() {}
 	
 	/** The copy constructor */
 	public ProcessCommand(ProcessCommand that) {
 		super(that);
 		this.commands = Utils.copy(that.commands);
 	}
 	
 	/** 
 	 * @return <code>true</code> if this process has at least one command to execute, <code>false</code> otherwise 
 	 */
 	public boolean hasCommands() {
 		return size()>0;
 	} 
 
 	/**
 	 * Initialize all commands 
 	 */
 	@Override
 	public void init(ContextHolder ctx) {
 		super.init(ctx);
 		
 		if(!hasCommands()) return;
 		
 		for( AbstractCommand cmd : commands ) {
 			cmd.init(ctx);
 		}
 	}
 	
 	/**
 	 * Run che process invoking the run method on each single command 
 	 * 
 	 * @throws CommandException 
 	 */
 	@Override
 	protected boolean run() throws CommandException {
 
 		for( AbstractCommand cmd : commands ) {
 			boolean fail = !cmd.execute();
 
 			if( fail ) { 
 				return false;
 			};
 		}
 
 		return true;
 		
 	}
 	
	@Override
	protected boolean done(boolean success) {
		ctx.result.elapsedTime = this.elapsedTime;
		return success;
	}
 	
 	public void add( AbstractCommand cmd ) { 
 		if( cmd == null ) return;
 
 		if( commands == null ) {
 			commands = new ArrayList<AbstractCommand>();
 		}
 		commands.add(cmd);
 	}
 	
 	public void addAll( ProcessCommand that ) {
 		
 		if( that == null || that.commands == null ) { return; } 
 		
 		if( commands == null ) {
 			commands = new ArrayList<AbstractCommand>();
 		}
 		
 		commands.addAll( that.commands ); 
 	}
 	
 	public int size() { 
 		return commands != null ? commands.size() : 0;
 	}
 	
 }
