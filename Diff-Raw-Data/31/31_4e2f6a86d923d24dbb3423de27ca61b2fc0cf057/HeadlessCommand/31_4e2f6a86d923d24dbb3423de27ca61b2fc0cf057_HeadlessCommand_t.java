 package org.blockout;
 
 import java.net.InetAddress;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 
 /**
  * Command object for JCommander to parse the additional parameters for the
  * headless mode.
  * 
  * @author Marc-Christian Schulze
  * 
  */
 @Parameters(commandDescription = "Run game in headless mode")
 public class HeadlessCommand {
 
 	@Parameter(names = { "-p", "--player" }, required = true, description = "Name of the player")
 	private String		playerName;
 
 	@Parameter(names = "--bootstrap", description = "IP-Address or hostname of peer to connect to by TCP/IP", converter = InetAddressConverter.class)
 	private InetAddress	bootstrapAddress;
 
	@Parameter(names = "--bootstrapPort", description = "Port of the peer to bootstrap from")
	private int			bootstrapPort;

 	public String getPlayerName() {
 		return playerName;
 	}
 
 	public InetAddress getBootstrapAddress() {
 		return bootstrapAddress;
 	}

	public int getBootstrapPort() {
		return bootstrapPort;
	}

 }
