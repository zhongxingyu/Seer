 package cc.warlock.core.stormfront.script.wsl;
 
 public class WSLAction extends WSLAbstractCommand {
 
 	private WSLScript script;
 	private WSLAbstractCommand command;
 	private IWSLValue when;
 	
 	public WSLAction(int lineNum, WSLScript script, WSLAbstractCommand command, IWSLValue when) {
 		super(lineNum);
 		this.script = script;
 		this.command = command;
 		this.when = when;
 	}
 	
 	private class WSLActionAdapter implements Runnable {
		private WSLAbstractCommand command;
		
		public WSLActionAdapter(WSLAbstractCommand command) {
			this.command = command;
		}
 		
 		public void run() {
 			command.execute();
 		}
 	}
 	
 	public void execute() {
		script.scriptCommands.addAction(new WSLActionAdapter(command), when.toString());
 	}
 
 }
