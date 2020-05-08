 /*  HellowJava, alpha version
  *  (c) 2005-2010 Gustavo Maia Neto (gutomaia)
  *
  *  HellowJava and all other Hellow flavors will be always
  *  freely distributed under the terms of an GPLv3 license.
  *
  *  Human Knowledge belongs to the World!
  *--------------------------------------------------------------------------*/
 
 package net.guto.hellow.protocol;
 
 import net.guto.hellow.core.ConnectionHandle;
 import net.guto.hellow.core.SocketConnection;
 import net.guto.hellow.core.listener.CommandListener;
 
 public abstract class Msnp {
	
 	protected final String EL = "\r\n";
 	protected String _passport = null;
 	protected int _trid = 1;
 
 	abstract String getHost();
 
 	abstract Integer getPort();
 
 	abstract void execute(String command);
 
 	private ConnectionHandle _connectionHandle;
 

 	public Msnp() {
 		_connectionHandle = new SocketConnection();
 	}
 
 	protected void connect(String host, int port) {
 		_connectionHandle.connect(host, port);
 	}
 
 	protected void disconnect() {
 		_connectionHandle.disconnect();
 	}
	
	public void setConnectionHandle(ConnectionHandle connectionHandle){
 		_connectionHandle = connectionHandle;
 	}
 
 	private CommandListener _commandListener = null;
 
 	public void addCommandListener(CommandListener commandListener) {
 		_commandListener = commandListener;
 	}
 
 	protected void send(String command) {
 		_connectionHandle.send(command);
 		_trid++;
 		if (_commandListener != null && command != null)
 			_commandListener.sendedCommand(command);
 	}
 
 	protected final void listen() {
 		while (_connectionHandle.hasMoreCommands()) {
 			String command = _connectionHandle.nextCommand();
			if (!command.trim().equals("")){
 				execute(command);
				_commandListener.receivedCommand(command);
 			}
 		}
 	}
 }
