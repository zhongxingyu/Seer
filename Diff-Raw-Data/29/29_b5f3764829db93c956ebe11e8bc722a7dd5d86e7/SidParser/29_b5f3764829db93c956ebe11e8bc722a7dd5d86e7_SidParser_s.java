 package liv.ac.uk.vomssnooper;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Reads in a site-info.def file (and related vo.d files) and makes the fields into objects.
  * 
  * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
  * @since 2012-05-10
  */
 
 public class SidParser {
 
 	private String oldSidDir; // The site file to read from
 	private WordList myVOs; // List of VOs (used to reject backup files and trash)
 	private HashMap<String, VirtOrgInfo> voidInfo; // A collection of VO info
 
 	/**
 	 * Constructor
 	 * 
 	 * @param os old sid dir
 	 * @param vi the collection of vo info
 	 * @param mv list of VOs that I support
 	 * 
 	 */
 	public SidParser(String os, HashMap<String, VirtOrgInfo> vi, WordList mv) {
 		oldSidDir = os;
 		voidInfo = vi;
 		myVOs = mv;
 	}
 
 	/**
 	 * Parse the site info file
 	 * 
 	 * @return null
 	 */
 	public void parseDocument() {
 
 		// Storage for the record I consider
 		String caDnLine = null;
 		String vomsServersLine = null;
 		String vomsesLine = null;
 
 		// First get the sid lines
 		File sid = new File(oldSidDir + "/site-info.def");
 		if (!sid.isFile()) {
 			System.out.println("site-info.def file " + sid + " not found");
 			System.exit(1);
 		}
 
 		// Get the yaim variables
 		ArrayList<String> yaimVariables = new ArrayList<String>();
 		try {
 			yaimVariables = cmdExec("bash -x " + oldSidDir + "/site-info.def");
 		}
 		catch (Exception e) {
 			System.out.println("Problem while while reading old site-info.def " + e.getMessage());
 			System.exit(1);
 		}
 
 		// Sort so we can depend on the order
 		Collections.sort(yaimVariables, String.CASE_INSENSITIVE_ORDER);
 
 		// Go over the yaim variables, selecting VO lines
 		Pattern pattern = Pattern.compile("VO_(.*)_VOMS.*");
 		
 		
 		for (String yaimVariable: yaimVariables) {
 
 			Matcher matcher = pattern.matcher(yaimVariable);
 			if (matcher.find()) {
 
 				String voName = matcher.group(1).toLowerCase();
 
 				// Only use it if it's desired, else dump it.
 				if (myVOs.containsNoCase(voName)) {
 
 					// Collect all the lines first to overcome any order problems
 
 					if (yaimVariable.matches(".*CA_DN.*")) {
 						if (voidInfo.containsKey(voName) == true) {
 							System.out.println("Warning: the " + voName + " sid records are duplicated! Results may be chaotic.");
 						}
 						else {
 							// Make a new set of records
 							voidInfo.put(voName, new VirtOrgInfo());
 						}
 
 						// Initial values
 						voidInfo.get(voName).setVoNameAndVoNickName(voName);
 						voidInfo.get(voName).setVodStyle(false);
 						voidInfo.get(voName).setAtMySite(true);
 
 						// Store the CA DNs
 						caDnLine = yaimVariable;
 					}
 
 					if (yaimVariable.matches(".*VOMS_SERVERS.*")) {
 						// Store the VOMS Servers
 						vomsServersLine = yaimVariable;
 					}
 
 					if (yaimVariable.matches(".*VOMSES.*")) {
 						// Store the VOMSES
 						vomsesLine = yaimVariable;
 
 						// As it is sorted, this triggers the end of a run, so now do more parsing
 						// Break that CA DN variable up and go setting fields
 						ArrayList<String> elements = breakString(caDnLine);
 
 						for (String caDn: elements){
 							VomsServer theVomsServer = new VomsServer();
 							theVomsServer.setMembersListUrl("dummy");
 							theVomsServer.setCaDn(caDn);
 							voidInfo.get(voName).addVomsServer(theVomsServer);
 						}
 
 						// VOMSES found - break the variable up and go setting fields
 						elements = breakString(vomsesLine);
 						int ii = -1;
 						for (String vomses: elements) {
 							ii++;
 							ArrayList<VomsServer> vomsServers = voidInfo.get(voName).getVomsServers();
 
 							// More pattern matching to save a lot of tinkering
							Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
 							Matcher m = p.matcher(vomses);
 							if (m.find()) {
 								String vo1 = m.group(1);
 								String host = m.group(2);
 								String vomsServerPort = m.group(3);
 								String dn = m.group(4);
 								String vo2 = m.group(5);
 
 								vomsServers.get(ii).setHostname(host);
 								vomsServers.get(ii).setVomsServerPort(Integer.parseInt(vomsServerPort));
 								vomsServers.get(ii).setDn(dn);
 							}
 						}
 
 						// Voms Servers found - break the variable up and go setting fields
 						elements = breakString(vomsServersLine);
             for (String el: elements){
 							ArrayList<VomsServer> vomsServers = voidInfo.get(voName).getVomsServers();
 
 							// Pattern matching to save a lot of tinkering
 							Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
 							Matcher m = p.matcher(el);
 							if (m.find()) {
 								String hostPart = m.group(1);
 								Integer httpsPort = Integer.parseInt(m.group(2));
 
 								// Find the voms server that this record applies to
 								Boolean setPort = false;
 								for (VomsServer v: vomsServers){
 									String h = v.getHostname();
 									if (h.equalsIgnoreCase(hostPart)) {
 										// This is the one
 										v.setHttpsPort(httpsPort);
 										setPort = true;
 									}
 								}
 								if (!setPort) {
 									System.out.println("Warning: Unable to find a voms server for one of these: " + vomsServersLine);
 								}
 							}
 							else {
 								System.out.println("Warning: Weird VOMS_SERVER line: " + el.toString());
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Next get the vod files, that lie in files in the vo.d directory
 		File dir = new File(oldSidDir + "/vo.d");
 		String[] vodFiles = dir.list();
 		if (vodFiles == null) {
 			System.out.println("the vo.d directory does not exist");
 			System.exit(1);
 		}
 		for (int i = 0; i < vodFiles.length; i++) {
 			String vodFile = vodFiles[i];
 
 			// Only use it if it's desired, else dump it.
 			if (myVOs.containsNoCase(vodFile)) {
 
 				// Read the yaim variables for each files found
 				ArrayList<String> vodYaimVariables = new ArrayList<String>();
 				try {
 					vodYaimVariables = cmdExec("bash -x " + oldSidDir + "/vo.d/" + vodFile);
 				}
 				catch (Exception e) {
 					System.out.println("Problem while while reading old vod. file " + vodFile + ", " + e.getMessage());
 					System.exit(1);
 				}
 				// Sort it so we can depend on the order
 				Collections.sort(vodYaimVariables, String.CASE_INSENSITIVE_ORDER);
 
 				// Go over the lines, look for VO ones
 				Pattern vodVomsPattern = Pattern.compile("^\\+ VOMS.*");
 				
 				for (String vodYaimVariable: vodYaimVariables) {
 
 
 					Matcher matcher = vodVomsPattern.matcher(vodYaimVariable);
 					if (matcher.find()) {
 
 						// Name is same as file, for VODs
 						String voName = vodFile.toLowerCase();
 						// VirtOrgInfo thisVo = voidInfo.get(voName);
 
 						// Get all the fields in advance
 						if (vodYaimVariable.matches(".*CA_DN.*")) {
 							caDnLine = vodYaimVariable;
 							// Make a new record, if we need to
 							if (voidInfo.containsKey(voName.toLowerCase()) == true) {
 								System.out.println("Warning: the " + voName + " vod records are duplicated! Results may be chaotic.");
 							}
 							else {
 								voidInfo.put(voName.toLowerCase(), new VirtOrgInfo());
 							}
 							voidInfo.put(voName, new VirtOrgInfo());
 							voidInfo.get(voName).setVoNameAndVoNickName(voName);
 							voidInfo.get(voName).setVodStyle(true);
 							voidInfo.get(voName).setAtMySite(true);
 
 						}
 						if (vodYaimVariable.matches(".*VOMS_SERVERS.*")) {
 							vomsServersLine = vodYaimVariable;
 						}
 						if (vodYaimVariable.matches(".*VOMSES.*")) {
 							vomsesLine = vodYaimVariable;
 
 							// Last one found, so parse the fields
 
 							// Break up the CA DN variable
 							ArrayList<String> elements = breakString(caDnLine);
 							for (String caDn: elements) {
 								VomsServer theVomsServer = new VomsServer();
 								theVomsServer.setCaDn(caDn);
 								theVomsServer.setMembersListUrl("dummy");
 								voidInfo.get(voName).addVomsServer(theVomsServer);
 							}
 
 							// VOMSES found - break the variable up and go setting fields
 							elements = breakString(vomsesLine);
 							int ii = -1;
 							for (String vomses: elements) {
 								ii++;
 								ArrayList<VomsServer> vomsServers = voidInfo.get(voName).getVomsServers();
 
 								// Pattern matching to save a lot of tinkering
								Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)");
 								Matcher m = p.matcher(vomses);
 								if (m.find()) {
 									String vo1 = m.group(1);
 									String host = m.group(2);
 									String vomsServerPort = m.group(3);
 									String dn = m.group(4);
 									String vo2 = m.group(5);
 									vomsServers.get(ii).setHostname(host);
 									vomsServers.get(ii).setVomsServerPort(Integer.parseInt(vomsServerPort));
 									vomsServers.get(ii).setDn(dn);
 								}
 							}
 
 							// Voms Servers found - break the variable up and go setting
 							// fields
 							elements = breakString(vomsServersLine);
 							ii = -1;
 							for (String el: elements) {
 								ii++;
 								ArrayList<VomsServer> vomsServers = voidInfo.get(voName).getVomsServers();
 
 								// Pattern matching to save a lot of tinkering
 								Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
 								Matcher m = p.matcher(el);
 								if (m.find()) {
 									String hostToFind = m.group(1);
 									Integer httpsPort = Integer.parseInt(m.group(2));
 
 									// Go over all the VOMS Servers, finding the one that matches this record
 									Boolean setPort = false;
 									for (VomsServer v: vomsServers) {
 									
 										String h = v.getHostname();
 										if (h.equalsIgnoreCase(hostToFind)) {
 											v.setHttpsPort(httpsPort);
 											setPort = true;
 										}
 									}
 									if (!setPort) {
 										System.out.println("Warning: Unable to find a voms server for one of these: " + vomsServersLine);
 									}
 								}
 								else {
 									System.out.println("Warning: Weird VOMS_SERVER line: " + el.toString());
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Finally, sort those voms servers
 		ArrayList<VirtOrgInfo> v = new ArrayList<VirtOrgInfo>(voidInfo.values());
 
 		for (VirtOrgInfo voi: v) {
 			voi.sortVomsServers();
 		}
 	}
 
 	/**
 	 * Executes some command and returns its stdout
 	 * 
 	 * @param cmdLine command to execute
 	 * @return output from command
 	 */
 	public static ArrayList<String> cmdExec(String cmdLine) throws Exception {
 
 		// Return output as a list
 		ArrayList<String> output = new ArrayList<String>();
 
 		String lineBack;
 		try {
 			Process p = Runtime.getRuntime().exec(cmdLine);
 			BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 			while ((lineBack = input.readLine()) != null) {
 				output.add(lineBack);
 			}
 			input.close();
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 			throw new Exception("Some problem occured while running " + cmdLine + " " + ex.getMessage());
 		}
 		return output;
 	}
 
 	/**
	 * Breaks up a yaim variable string, according to this contract Contract: + NAME= then: Nothing - string is empty Space - string
	 * is empty String, without leading ' char - whole content is one element
 	 * 
	 * String with leading ' char: then: without '\''' - string is simple string bounded by '', one element with '\''' ... string is a
	 * sequence of elements separated by that
 	 * 
 	 * @param cmdLine command to execute
 	 * @return output from command
 	 */
 
 	private ArrayList<String> breakString(String s) {
 
 		String payload = s;
 
 		payload = payload.substring(s.indexOf('=') + 1);
 		payload = payload.trim();
 		if (payload.isEmpty()) {
 			// No elements
 			return new ArrayList<String>();
 		}
 
 		if (!payload.startsWith("'")) {
 			// One element
 			ArrayList<String> res = new ArrayList<String>();
 			res.add(payload);
 			return res;
 		}
 		else {
 			// String with leading ' char.
 			// Cut off bounding chars
 			payload = payload.substring(1, payload.length() - 1);
 			if (payload.startsWith("'\\''")) {
 				// Some elements ...
 
 				// This is what '\'' looks like in Java as a regex. Don't blame me.
 				String[] tokens = payload.split("\\'\\\\\'\'");
 				ArrayList<String> res = new ArrayList<String>();
 				for (int ii = 0; ii < tokens.length; ii++) {
 					if (!tokens[ii].matches("^\\s*$")) {
 						res.add(tokens[ii]);
 					}
 				}
 				return res;
 			}
 			else {
 				// One element
 				ArrayList<String> res = new ArrayList<String>();
 				res.add(payload);
 				return res;
 			}
 		}
 	}
 }
