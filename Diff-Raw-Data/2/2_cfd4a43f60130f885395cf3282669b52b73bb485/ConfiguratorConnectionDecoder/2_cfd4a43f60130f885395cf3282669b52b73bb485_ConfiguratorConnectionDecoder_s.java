 package ar.edu.it.itba.pdc.proxy.implementations.configurator.implementations;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.ws.rs.core.MediaType;
 
 import nl.bitwalker.useragentutils.Browser;
 import nl.bitwalker.useragentutils.OperatingSystem;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.block.Block;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.block.BrowserBlock;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.block.IPBlock;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.block.OSBlock;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.interfaces.ConfiguratorConnectionDecoderInt;
 
 public class ConfiguratorConnectionDecoder implements
 		ConfiguratorConnectionDecoderInt {
 
 	private boolean logged = false;
 	private boolean specification = false;
 	private boolean closeConnection = false;
 	private boolean applyTransformations = false;
 	private boolean applyRotations = false;
 	private boolean blockAll = false;
 	private List<BrowserBlock> browserBlock;
 	private List<OSBlock> OSBlock;
 	private List<IPBlock> ipBlock;
 	private Map<String, String> reply;
 	// private Set<InetAddress> blockedAddresses;
 	// private Set<String> blockedMediaType;
 	// private Set<String> blockedURIs;
 	private int maxSize = -1;
 	private Logger decoderLog = Logger.getLogger(this.getClass());
 	private Block block;
 
 	public ConfiguratorConnectionDecoder() {
 		reply = new HashMap<String, String>();
 		fillReply();
 		browserBlock = new ArrayList<BrowserBlock>();
 		OSBlock = new ArrayList<OSBlock>();
 		ipBlock = new ArrayList<IPBlock>();
 		// blockedAddresses = new TreeSet<InetAddress>();
 		// blockedMediaType = new TreeSet<String>();
 		// blockedURIs = new TreeSet<String>();
 	}
 
 	public boolean closeConnection() {
 		return closeConnection;
 	}
 
 	public String decode(String s) {
 		s = s.replace("\n", "");
 		if (!logged) {
 			String[] credentials = s.split(":");
 			if (credentials.length == 2 && credentials[0].equals("admin")
 					&& credentials[1].equals("pdc2012")) {
 				decoderLog.info("Admin logged in");
 				logged = true;
 				return reply.get("LOG_IN_OK");
 			} else {
 				decoderLog.info("Log in failed. Closing connection");
 				closeConnection = true;
 				return reply.get("LOGIN_ERROR");
 			}
 		} else if (!specification) {
 			String[] args = s.split(" ");
 			if (args[0].equals("HELP")) {
 				return printHelp();
 			}
 			if (args.length != 3) {
 				return reply.get("WRONG_COMMAND");
 			}
 			if (args[0].contains("FOR")) {
 				int index;
 				if (args[1].contains("BROWSER")) {
 					if ((index = containsBrowser(args[2])) != -1) {
 						block = browserBlock.get(index);
 						specification = true;
 						return "200 - Specify actions for "
 								+ ((BrowserBlock) block).getBrowser()
 										.toString() + "\n";
 					} else {
 						try {
 							block = new BrowserBlock(Browser.valueOf(args[2]));
 							browserBlock.add((BrowserBlock) block);
 							specification = true;
 							return "200 - Specify actions for "
 									+ ((BrowserBlock) block).getBrowser()
 											.toString() + "\n";
 						} catch (IllegalArgumentException e) {
 							return reply.get("WRONG_PARAMETERS");
 						}
 					}
 				} else if (args[1].contains("OS")) {
 					if ((index = containsOS(args[2])) != -1) {
 						block = OSBlock.get(index);
 						specification = true;
 						return "200 - Specify actions for "
 								+ ((OSBlock) block).getOS().toString() + "\n";
 					} else {
 						try {
 							block = new OSBlock(
 									OperatingSystem.valueOf(args[2]));
 							OSBlock.add((OSBlock) block);
 							specification = true;
 							return "200 - Specify actions for "
 									+ ((OSBlock) block).getOS().toString()
 									+ "\n";
 						} catch (IllegalArgumentException e) {
 							return reply.get("WRONG_PARAMETERS");
 						}
 					}
 				} else if (args[1].contains("IP")) {
 					InetAddress ip;
 					try {
 						ip = InetAddress.getByName(args[2]);
 					} catch (UnknownHostException e) {
 						return reply.get("WRONG_PARAMETERS");
 					}
 					if ((index = containsIP(ip)) != -1) {
 						block = ipBlock.get(index);
 						specification = true;
 						return "200 - Specify actions for "
 								+ ((IPBlock) block).getIp() + "\n";
 					} else {
 						block = new IPBlock(ip);
 						ipBlock.add((IPBlock) block);
 						specification = true;
 						return "200 - Specify actions for "
 								+ ((IPBlock) block).getIp() + "\n";
 					}
 				} else {
 					return reply.get("WRONG_COMMAND");
 				}
 			} else {
 				return "400 - Must specify Browser, OS or IP\n";
 			}
 		} else {
 			String[] args = s.split(" ");
 			if (args[0].equals("BLOCK")) {
 				return analyzeBlockCommand(args);
 			} else if (args[0].equals("UNBLOCK")) {
 				return analyzeUnblockCommand(args);
 			} else if (args[0].equals("TRANSFORMATIONS")) {
 				if (args.length != 2) {
 					return reply.get("WRONG_COMMAND");
 				}
 				if (args[1].equals("ON")) {
 					decoderLog.info("Transformations turned on");
 					block.setApplyTransformations(true);
 					return reply.get("TRANSF_ON");
 				} else if (args[1].equals("OFF")) {
 					decoderLog.info("Transformations turned off");
 					block.setApplyTransformations(false);
 					return reply.get("TRANSF_OFF");
 				} else {
 					return reply.get("WRONG_PARAMETERS");
 				}
 			} else if (args[0].equals("ROTATIONS")) {
 				if (args.length != 2) {
 					return reply.get("WRONG_COMMAND");
 				}
 				if (args[1].equals("ON")) {
 					decoderLog.info("Rotations turned on");
 					block.setApplyRotations(true);
 					return reply.get("ROT_ON");
 				} else if (args[1].equals("OFF")) {
 					decoderLog.info("Rotations turned off");
 					block.setApplyRotations(false);
 					return reply.get("ROT_OFF");
 				} else {
 					return reply.get("WRONG_PARAMETERS");
 				}
 			} else if (args.length == 3 && args[0].equals("GET")
 					&& args[1].equals("CONF")) {
 				if (args.length != 3) {
 					return reply.get("WRONG_COMMAND");
 				}
 				if (args[2].equals("ROTATIONS")) {
 					if (applyRotations) {
 						return reply.get("ROT_ON");
 					} else {
 						return reply.get("ROT_OFF");
 					}
 				} else if (args[2].equals("TRANSFORMATIONS")) {
 					if (applyTransformations) {
 						return reply.get("TRANSF_ON");
 					} else {
 						return reply.get("TRANSF_OFF");
 					}
 				} else if (args[2].equals("BLOCK")) {
 					StringBuffer sb = new StringBuffer();
 					sb.append("200 - Blocked list:\n");
 					sb.append("Media Types:\n");
 					for (String mt : block.getBlockedMediaType()) {
 						sb.append("\t" + mt.toString() + "\n");
 					}
 					sb.append("URIs:\n");
 					for (String p : block.getBlockedURIs()) {
 						sb.append("\t" + p + "\n");
 					}
 					sb.append("IP addresses:\n");
 					for (InetAddress addr : block.getBlockedAddresses()) {
 						sb.append("\t" + addr.toString() + "\n");
 					}
 					return sb.toString();
 				} else {
 					return reply.get("WRONG_PARAMETERS");
 				}
 			} else if (args[0].equals("EXIT")) {
 				decoderLog.info("EXIT command received. Closing connection");
 				closeConnection = true;
 				logged = false;
 				block = null;
 				return "Bye bye\n";
 			} else if (args[0].equals("HELP")) {
 				return printHelp();
 			} else if (args[0].equals("CHANGE")) {
 				specification = false;
 				block = null;
 				return "200 - Change accepted\n";
 			} else {
 				return reply.get("WRONG_COMMAND");
 			}
 		}
 	}
 
 	private String analyzeBlockCommand(String[] line) {
 		if (line.length == 2 && line[1].equals("ALL")) {
 			block.setBlockAll(true);
 			return "200 - All access blocked\n";
 		}
 		if (line.length != 3)
 			return reply.get("WRONG_COMMAND");
 		String type = line[1];
 		String arg = line[2];
 		if (type.equals("IP")) {
 			InetAddress addr;
 			try {
 				addr = InetAddress.getByName(arg);
 				decoderLog.info("Blocking " + addr.toString());
 				block.addBlockedAddress(addr);
 				return "200 - " + arg + " blocked\n";
 			} catch (Exception e) {
 				return reply.get("WRONG_PARAMETERS");
 			}
 		} else if (type.equals("MTYPE")) {
 			MediaType mt = analyzeMediaType(arg);
 			if (mt == null) {
 				return "400 - Invalid media type\n";
 			}
 			decoderLog.info("Blocking " + mt.toString());
 			block.addBlockedMType(mt.toString());
 			return "200 - " + mt.toString() + " blocked\n";
 		} else if (type.equals("SIZE")) {
 			try {
 				Integer max = Integer.parseInt(arg);
 				block.setMaxSize(max);
 				decoderLog.info("Blocking files bigger than " + max);
 				return "200 - Sizes bigger than " + block.getMaxSize()
 						+ " are now blocked\n";
 			} catch (NumberFormatException e) {
 				return "400 - Invalid size\n";
 			}
 		} else if (type.equals("URI")) {
 			try {
 				Pattern p = Pattern.compile(arg);
 				decoderLog.info("Blocking " + p.toString());
 				block.addBlockedURI(p.pattern());
 				return "200 - " + arg + " blocked\n";
 			} catch (PatternSyntaxException e) {
 				return "400 - Invalid pattern\n";
 			}
 		} else {
 			return reply.get("WRONG_PARAMETERS");
 		}
 	}
 
 	private String analyzeUnblockCommand(String[] line) {
 		if (line.length == 2 && line[1].equals("ALL")) {
 			blockAll = false;
 			return "200 - All access unblocked\n";
 		}
 		if (line.length != 3)
 			return reply.get("WRONG_COMMAND");
 		String type = line[1];
 		String arg = line[2];
 		if (type.equals("IP")) {
 			InetAddress addr;
 			try {
 				addr = InetAddress.getByName(arg);
 				decoderLog.info("Unblocking " + addr.toString());
 				block.removeBlockedIP(addr);
 				return "200 - " + arg + " blocked\n";
 			} catch (UnknownHostException e) {
 				return reply.get("WRONG_PARAMETERS");
 			}
 		} else if (type.equals("MTYPE")) {
 			MediaType mt = analyzeMediaType(arg);
 			if (mt == null) {
 				return "400 - Invalid media type\n";
 			}
 			decoderLog.info("Unblocking " + mt.toString());
 			block.removeBlockedMType(mt.toString());
 			return "200 - " + mt.getType() + mt.getSubtype() + " unblocked\n";
 		} else if (type.equals("SIZE")) {
 			try {
 				Integer max = Integer.parseInt(arg);
 				block.setMaxSize(-1);
 				if (max == -1) {
 					return "200 - All sizes are permited\n";
 				}
 				decoderLog.info("Unblocking files bigger than" + max);
 				return "200 - Sizes bigger than " + max
 						+ " are now unblocked\n";
 			} catch (NumberFormatException e) {
 				return "400 - Invalid size\n";
 			}
 		} else if (type.equals("URI")) {
 			try {
 				Pattern p = Pattern.compile(arg);
 				block.removeBlockedURI(p.pattern());
 				decoderLog.info("Unblocking " + p.toString());
 				return "200 - " + p.pattern() + " unblocked\n";
 			} catch (PatternSyntaxException e) {
 				return "400 - Invalid pattern\n";
 			}
 		} else {
 			return reply.get("WRONG_PARAMETERS");
 		}
 	}
 
 	private MediaType analyzeMediaType(String mtype) {
 		MediaType media;
 		try {
 			media = MediaType.valueOf(mtype);
 		} catch (IllegalArgumentException e) {
 			return null;
 		}
 		return media;
 	}
 
 	private void fillReply() {
 		reply.put("LOG_IN_OK", "200 - Welcome\n");
 		reply.put("LOGIN_ERROR", "400 - Wrong username and/or password\n");
 		reply.put("WRONG_COMMAND", "401 - Wrong command\n");
 		reply.put("WRONG_PARAMETERS", "401 - Wrong parameters\n");
 		reply.put("TRANSF_ON", "200 - Transformations are on\n");
 		reply.put("TRANSF_OFF", "200 - Transformations are off\n");
 		reply.put("ROT_ON", "200 - Rotations are on\n");
 		reply.put("ROT_OFF", "200 - Rotations are off\n");
 	}
 
 	private String printHelp() {
 		decoderLog.info("HELP command received");
 		StringBuffer sb = new StringBuffer();
 		sb.append("Available commands: BLOCK - UNBLOCK - TRANSFORMATIONS - ROTATIONS - GET - FOR - CHANGE\n");
 		sb.append("BLOCK or UNBLOCK usage: (BLOCK | UNBLOCK) SP (IP|URI|MTYPE|SIZE) SP (VALUE)\n");
 		sb.append("TRANSFORMATIONS or ROTATIONS usage: (TRANSFORMATIONS|ROTATIONS) SP (ON|OFF)\n");
 		sb.append("GET usage: GET SP CONF SP (ROTATIONS|TRANSFORMATIONS|BLOCK)\n");
 		sb.append("FOR usage: FOR SP (BROWSER|IP|OS| SP (VALUE)\n");
 		sb.append("CHANGE usage: CHANGE\n");
 		sb.append("FOR example values: OS: LINUX | MAC_OS_X | WINDOWS_7 - BROWSER: FIREFOX | CHROME | IE\n");
 
 		return sb.toString();
 	}
 
 	public Object[] getBlockedAddressesFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return null;
 		return browserBlock.get(index).getBlockedAddresses().toArray();
 	}
 
 	public Object[] getBlockedAddressesFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return null;
 		return ipBlock.get(index).getBlockedAddresses().toArray();
 	}
 
 	public Object[] getBlockedAddressesFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return null;
 		return OSBlock.get(index).getBlockedAddresses().toArray();
 	}
 
 	public Object[] getBlockedMediaTypeFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return null;
 		return browserBlock.get(index).getBlockedMediaType().toArray();
 	}
 
 	public Object[] getBlockedMediaTypeFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return null;
 		return ipBlock.get(index).getBlockedMediaType().toArray();
 	}
 
 	public Object[] getBlockedMediaTypeFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return null;
 		return OSBlock.get(index).getBlockedMediaType().toArray();
 	}
 
 	public Object[] getBlockedURIsFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return null;
 		return browserBlock.get(index).getBlockedURIs().toArray();
 	}
 
 	public Object[] getBlockedURIsFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return null;
 		return ipBlock.get(index).getBlockedURIs().toArray();
 	}
 
 	public Object[] getBlockedURIsFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return null;
 		return OSBlock.get(index).getBlockedURIs().toArray();
 	}
 
 	public int getMaxSizeFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return -1;
 		return browserBlock.get(index).getMaxSize();
 	}
 
 	public int getMaxSizeFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return -1;
 		return ipBlock.get(index).getMaxSize();
 	}
 
 	public int getMaxSizeFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return -1;
 		return OSBlock.get(index).getMaxSize();
 	}
 
 	public boolean applyRotationsFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return false;
 		return browserBlock.get(index).isApplyRotations();
 	}
 
 	public boolean applyRotationsFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return false;
 		return ipBlock.get(index).isApplyRotations();
 	}
 
 	public boolean applyRotationsFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return false;
 		return OSBlock.get(index).isApplyRotations();
 	}
 
 	public boolean applyTransformationsFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return false;
 		return browserBlock.get(index).isApplyTransformations();
 	}
 
 	public boolean applyTransformationsFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return false;
 		return ipBlock.get(index).isApplyTransformations();
 	}
 
 	public boolean applyTransformationsFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return false;
 		return OSBlock.get(index).isApplyTransformations();
 	}
 
 	public boolean blockAllFor(Browser b) {
 		int index = containsBrowser(b.toString());
 		if (index == -1)
 			return false;
 		return browserBlock.get(index).isBlockAll();
 	}
 
 	public boolean blockAllFor(InetAddress addr) {
 		int index = containsIP(addr);
 		if (index == -1)
 			return false;
 		return ipBlock.get(index).isBlockAll();
 	}
 
 	public boolean blockAllFor(OperatingSystem os) {
 		int index = containsOS(os.toString());
 		if (index == -1)
 			return false;
 		return OSBlock.get(index).isBlockAll();
 	}
 
 	public void reset() {
 		closeConnection = false;
 	}
 
 	private int containsBrowser(String brow) {
 		for (int i = 0; i < browserBlock.size(); i++) {
 			if (browserBlock.get(i).getBrowser().toString().equals(brow))
 				return i;
 		}
 		return -1;
 	}
 
 	private int containsOS(String OS) {
 		for (int i = 0; i < OSBlock.size(); i++) {
 			if (OSBlock.get(i).getOS().toString().equals(OS))
 				return i;
 		}
 		return -1;
 	}
 
 	private int containsIP(InetAddress ip) {
 		for (int i = 0; i < ipBlock.size(); i++) {
			if (ipBlock.get(i).getIp().equals(ip))
 				return i;
 		}
 		return -1;
 	}
 
 }
