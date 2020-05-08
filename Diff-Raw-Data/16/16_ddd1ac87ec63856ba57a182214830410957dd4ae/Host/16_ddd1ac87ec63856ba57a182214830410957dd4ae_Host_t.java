 package name.xumingjun.rest;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import name.xumingjun.rest.bean.AbstractJsonBean;
 import name.xumingjun.util.Stream;
 
 @Path("/host")
 
 public class Host {
 	
 //	Runtime rt = Runtime.getRuntime();
 //	rt.exec("uname -a ").getInputStream();
 	
 //	ProcessBuilder pb = new ProcessBuilder("myshellScript.sh", "myArg1", "myArg2");
 //	 Map<String, String> env = pb.environment();
 //	 env.put("VAR1", "myValue");
 //	 env.remove("OTHERVAR");
 //	 env.put("VAR2", env.get("VAR1") + "suffix");
 //	 pb.directory(new File("myDir"));
 //	 Process p = pb.start();
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getGeneral() {
 		//get cached hardware info, if possible
 		if(null == hardwareInfo) {
 			hardwareInfo = getHardware();
 		}
 		return new HostInfo(hardwareInfo, getUpTime()).toJson();
 	}
 	
 	static final String SYSTEM_INFOMATION = "System Information";
 	static final String SYSTEM_INFOMATION_REGEXP = "\\s+([-\\w ]+)\\: (.+)";
 	static Map<String,String> hardwareInfo = null; // hardware info is barely changed
 	public Map<String,String> getHardware() {
 		InputStream in = null;
 		Map<String,String> hw = new HashMap<String,String>();
 		try {
 			ProcessBuilder pb = new ProcessBuilder("sudo", "dmidecode");
 			in = pb.start().getInputStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line = null;
 			boolean findStart = false;
 			while(null != (line= br.readLine())) {
 				if(!findStart){
 					findStart = line.matches(SYSTEM_INFOMATION);
 				} else {
 					Pattern pat = Pattern.compile(SYSTEM_INFOMATION_REGEXP);
 					Matcher m = pat.matcher(line);
 					if(m.matches()) {
 						hw.put(m.group(1), m.group(2));
 					} else { // end
 						break;
 					}
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			Stream.close(in);
 		}
 		return hw;
 	}
 	
	
 	public String getUpTime() {
 		InputStream in = null;
 		String uptime = null;
 		try {
 			ProcessBuilder pb = new ProcessBuilder("uptime");
 			in = pb.start().getInputStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			uptime = parseUpTime(br.readLine());
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			Stream.close(in);
 		}
 		return uptime;
 	}
	
	static final String UP_TIME_REGEXP = ".+up\\s+(.+),\\s+\\d\\s+user.+";
	String parseUpTime(String raw) {
		Pattern pat = Pattern.compile(UP_TIME_REGEXP);
		Matcher m = pat.matcher(raw);
		return m.matches() ? m.group(1) : "unknown duration";
	}
 
 	class HostInfo extends AbstractJsonBean {
 		public Map<String, String> hardware;
 		public String upTime;
 		public HostInfo(Map<String, String> hw, String uptime) {
 			this.hardware = hw;
 			this.upTime = uptime;
 		}
 	}
 }
