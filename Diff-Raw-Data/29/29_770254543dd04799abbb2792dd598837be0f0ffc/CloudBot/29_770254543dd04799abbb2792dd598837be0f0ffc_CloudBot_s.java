 /*
     CloudBot is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Foobar is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.kroesch.clt.net;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayDeque;
 import java.util.Properties;
 import java.util.Queue;
 
 import javax.management.relation.Role;
 
 import org.jibble.pircbot.DccFileTransfer;
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 import de.kroesch.clt.Environment;
 import de.kroesch.clt.InternalEnvironment;
 import de.kroesch.clt.Parser;
 import de.kroesch.clt.internal.Get;
 import de.kroesch.clt.internal.Internal;
 import de.kroesch.clt.security.AuthCommand;
 import de.kroesch.clt.security.Authority;
 
 /**
  * Implementation of an IRC bot infrastructure.
  * 
  * @author Karsten Kroesch <karsten@kroesch.de>
  */
 public class CloudBot extends PircBot implements InternalEnvironment {
 
 	private final String BOTMASTER_USER = "botmaster";
 
 	private final Properties properties;
 
 	private final Parser parser;
 
 	private final Queue<Runnable> commandStack = new ArrayDeque<Runnable>();
 
 	private String lastErrorMessage = "OK";
 
 	private PrintWriter writer;
 
 	private StringWriter messageWriter;
 
 	private Authority authority = new Authority();
 
 	public CloudBot() throws NickAlreadyInUseException, IOException, IrcException {
 		parser = new Parser(this);
 		properties = new Properties();
 		messageWriter = new StringWriter();
 		writer = new PrintWriter(messageWriter);
 		
 		setVerbose(true);
 		setName(NickNameGenerator.generate());
		connect(properties.getProperty("botmaster.host"));
 		joinChannel("#botnet");
 
 		sendMessage("#botnet", String.format("CloudBot/%s - Up and running",
 				Environment.VERSION));
 
 	}
 
 	/**
 	 * Handle private messages. Sends answer output to sender.
 	 */
 	@Override
 	protected void onPrivateMessage(String sender, String login,
 			String hostname, String message) {
 		if (!BOTMASTER_USER.equals(sender))
 			return;
 
 		execute(message);
 
 		writer.flush();
 		StringBuffer messageBuffer = messageWriter.getBuffer();
 		if (!"OK".equals(lastErrorMessage)) {
 			messageBuffer.append("? ");
 			messageBuffer.append(lastErrorMessage);
 		}
 
 		String convertMessage = messageBuffer.toString().replace("\n", " // ")
 				.replace("\t", "  ");
 		sendMessage("botmaster", convertMessage);
 
 		messageBuffer.delete(0, messageBuffer.length());
 	}
 
 	/**
 	 * Handles incoming messages from botmaster. Does not answer.
 	 */
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 
 		if (!"botmaster".equals(sender))
 			return;
 
 		execute(message);
 
 		writer.flush();
 		StringBuffer messageBuffer = messageWriter.getBuffer();
 		messageBuffer.delete(0, messageBuffer.length());
 	}
 
 	/*
 	 * Common functionality for incoming messages
 	 */
 	private void execute(String message) {
 		Runnable consoleCommand = parser.parseCommand(message);
 
 		if (!(consoleCommand instanceof Internal))
 			commandStack.add(consoleCommand);
 		try {
 			if (consoleCommand instanceof AuthCommand) {
 				AuthCommand cmd = (AuthCommand) consoleCommand;
 				if (!authority.authorize(cmd))
 					throw new SecurityException("Not authorized.");
 			}
 			consoleCommand.run();
 			lastErrorMessage = "OK";
 		} catch (Exception ex) {
 			lastErrorMessage = ex.getMessage();
 		}
 	}
 
 	/**
 	 * Handles file uploads.
 	 * 
 	 * Scripts go to work directory, JARs go to plugins directory.
 	 */
 	@Override
 	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
 		File incoming = transfer.getFile();
 		File toSave;
 		if (incoming.getName().endsWith(".jar")) {
 			toSave = new File("plugins/" + incoming.getName());
 		} else {
 			toSave = new File(incoming.getName());
 		}
 		transfer.receive(toSave, true);
 	}
 
 	public PrintWriter writer() {
 		return writer;
 	}
 
 	public String get(String key) {
 		return properties.getProperty(key);
 	}
 
 	public void set(String key, String value) {
 		properties.setProperty(key, value);
 	}
 
 	public Queue<Runnable> history() {
 		return commandStack;
 	}
 
 	public String lastError() {
 		return lastErrorMessage;
 	}
 
 	public void setLastErrorMessage(String message) {
 		this.lastErrorMessage = message;
 	}
 
 	public Properties getProperties() {
 		return properties;
 	}
 
 	public Parser parser() {
 		return parser;
 	}
 
 	public String expect(String question, String choices) {
 		throw new UnsupportedOperationException("Does not work in Bot mode");
 	}
 
 	public static void main(String[] args) throws Exception {
 		CloudBot bot = new CloudBot();
 	}
 
 	public Authority authority() {
 		return authority;
 	}
 }
