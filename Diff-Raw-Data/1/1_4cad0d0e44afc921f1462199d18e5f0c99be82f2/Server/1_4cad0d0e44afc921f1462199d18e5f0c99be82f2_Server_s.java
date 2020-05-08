 package edu.colorado.csci3308.inventory;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.Session;
 
 public class Server {
 	
 	private Integer serverId;
 	private String hostname;
 	private String ipAddress;
 	private String macAddress;
 	private Integer totalMemory;
 	private Integer topHeight;
 	private Integer locationId;
 	private Integer rackId;
 	private ChassisModel model;
 	private Motherboard motherboard;
 	private String cards;
 	private String systemstring;
 	private String processors;
 	private static Date scanDate = null;
 	private Date serverScanDate = null;
 	private String codeRevision;
 	private Boolean isPingable;
 	private Boolean lastScanOK;
 	
 	public static final int MAX_HOSTNAME_LEN = 20;
 	public static final int MAX_IP_ADDRESS_LEN = 15;
 	public static final int MAX_MAC_ADDRESS_LEN = 17;
 	
 	public Server(String hostname){
 		this.hostname = hostname;
 	}
 	
 	public Server(Integer serverId,
 				  String hostname,
 				  String ipAddress,
 				  String macAddress,
 				  Integer totalMemory,
 				  ChassisModel model,
 				  Motherboard motherboard,
 				  String processors,
 				  String codeRevision,
 				  Boolean isPingable,
 				  Boolean lastScanOK,
 				  String cards) {
 		
 		this(serverId, hostname, ipAddress, macAddress, totalMemory, null, 1, null, model, 
 				motherboard, processors, codeRevision, isPingable, lastScanOK, cards);
 	}
 	
 	public Server(Integer serverId,
 				  String hostname,
 				  String ipAddress,
 				  String macAddress,
 				  Integer totalMemory,
 				  Integer topHeight,
 				  Integer locationId,
 				  Integer rackId,
 				  ChassisModel model,
 				  Motherboard motherboard,
 				  String processors,
 				  String codeRevision,
 				  Boolean isPingable,
 				  Boolean lastScanOK,
 				  String cards) {
 		
 		this.serverId = serverId;
 		this.hostname = hostname;
 		this.ipAddress = ipAddress;
 		this.macAddress = macAddress;
 		this.totalMemory = totalMemory;
 		this.topHeight = topHeight;
 		this.rackId = rackId;
 		this.locationId = locationId;
 		this.motherboard = motherboard;
 		this.model = model;
 		this.processors = processors;	
 		this.codeRevision = codeRevision;
 		this.isPingable = isPingable;
 		this.lastScanOK = lastScanOK;
 		this.cards = cards;
 	}
 	
 	public Motherboard getMotherboard() {
 		return motherboard;
 	}
 
 	public Integer getServerId() {
 		return serverId;
 	}
 	
 	public static Date getScanDate() {
 		return scanDate;
 	}
 
 	public String getHostname() {
 		return hostname;
 	}
 
 	public String getIpAddress() {
 		return ipAddress;
 	}
 
 	public String getMacAddress() {
 		return macAddress;
 	}
 
 	public Integer getTotalMemory() {
 		return totalMemory;
 	}
 
 	public Integer getTopHeight() {
 		return topHeight;
 	}
 
 	public void setTopHeight(Integer topHeight) {
 		this.topHeight = topHeight;
 	}
 
 	public Integer getLocationId() {
 		return locationId;
 	}
 
 	public Integer getRackId() {
 		return rackId;
 	}
 	
 	public ChassisModel getChassisModel() {
 		return model;
 	}
 	
 	public String getProcessors() {
 		return processors;
 	}
 	
 	public String[] getProcessorsArray() {
 		String[] result = processors.split(",");
 		return result;
 	}
 	
 	public String getSystemname() {
 		return systemstring;
 	}
 	
 	public Date getServerScanDate(){
 		return this.serverScanDate;
 	}
 	
 	public String getCodeRevision() {
 		return codeRevision;
 	}
 	
 	public void setLocationId(Integer locationId) {
 		this.locationId = locationId;
 	}
 
 	public void setRackId(Integer rackId) {
 		this.rackId = rackId;
 	}
 	
 	public Boolean isPingable() {
 		return isPingable;
 	}
 	
 	public Boolean lastScanOK() {
 		return 	lastScanOK;
 	}
 	
 	public String getCards() {
 		return cards;
 	}
 	
 	public void scan(){
 		Date d = new Date();
 		Server.scanDate  = d;
 		this.serverScanDate  = d;
 		
 		this.scanSystemName();
 		if(this.systemstring!=null){
 			//this.scanChassis();
 			this.scanCards();
 			this.scanProcessors();
 			this.scanMacAddress();
 			this.scanMobo();
 			this.scanMemory();
 		}
 		
 		//TODO - Let the user know if scan didn't run correctly, for generated report or Scan On Demand
 		ServerDB.updateServer(this);
 	}
 	
 	private void scanMacAddress() {
 		String command;
 		if(this.systemstring.contains("Ubuntu")){
 			command = "ifconfig | grep HWaddr";
 		}
 		else {
 			command = "ifconfig | grep ether";
 		}
 		String address = this.execute(command);
 		if(address.isEmpty()){
 		}
 		else {
 			try{ String addresses[];
 				if(this.systemstring.contains("Ubuntu")){
 					addresses = address.split("HWaddr");
 					address = addresses[1].split(" ")[1];
 				}
 				else if(this.systemstring.contains("FreeBSD")) {
 					addresses = address.split("HWaddr");
 					address = addresses[1].split(" ")[1];
 				}
 				this.macAddress = address;
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	//Not sure if this is working correctly, need to test on real system, not the virtual machine I'm on
 	private void scanMemory(){
 		String command = "dmidecode --type memory | grep \"Size:\"";
 		String size = this.sudoExecute(command);
 		if(size.isEmpty()){
 		}
 		else {
 			try{
 				String sizes[] = size.split("Size:");
 				Integer temp;
 				this.totalMemory = 0;
 				for(int i =0; i<sizes.length;i++){
 					try{
 						sizes[i] = sizes[i].split("MB")[0];
 						sizes[i] = sizes[i].trim();
 						temp = Integer.valueOf(sizes[i]);
 						this.totalMemory += temp;
 					}
 					catch(NumberFormatException e){
 						e.printStackTrace();
 					}
 				}
 			}
 			catch(Exception e){
 				System.out.println("ScanMemory: " + e);
 			}
 		}
 		System.out.println(this.totalMemory);
 		
 	}
 	/*  dmidecode pulls from a motherboard, not from  chassis.  This isn't really getting us the data we want.
 	private void scanChassis(){
 		String command = "dmidecode --type chassis";
 		String chassis = this.sudoExecute(command);
 		try{
 		String mfg = chassis.split("Manufacturer:")[1].split("Type")[0];
 		//TODO - finish searching for chassis info
 		//Integer height = Integer.valueOf(chassis.split("Height:")[1].split("Number")[0]);
 		String name = chassis.split("Type:")[1].split("Lock:")[0];
 		//Integer maxDataDrives = ??
 		this.model = new ChassisModel(1, name, 10, 10, mfg);
 		}
 		catch(Exception e){
 			System.out.println("ScanChassis: " + e);			
 		}
 	}
 	*/
 	
 	
 	private void scanMobo(){
 		String command = "dmidecode --type baseboard | grep Serial";
 		String serial = this.sudoExecute(command);
 		try{
 		serial = serial.split(":")[1];
 		
 		String command1 = "dmidecode --type baseboard | grep Product";
 		String model = this.sudoExecute(command1);
 		model = model.split(":")[1];
 		
 		String command2 = "dmidecode --type baseboard | grep Manufacturer";
 		String mfg = this.sudoExecute(command2);
 		mfg = mfg.split(":")[1];
 		
 		Motherboard mobo = new Motherboard(1, mfg, model, null);
 		motherboard = mobo;
 		}
 		catch(Exception e){
 			System.out.println("ScanMobo: " + e);			
 		}
 		
 	}
 	
 	private void scanProcessors() {
 		String command = "dmidecode --type 4 | grep Version";
 		String tempName = this.sudoExecute(command);
 		String command2 = "dmidecode --type 4 | grep \"Current Speed\"";
 		String tempMHz = this.sudoExecute(command2);
 		this.processors = "";
 		//Parse cpuinfo output
 		try{
 			String procs[] = tempName.split("\n");
 			String MHz[] = tempMHz.split("\n");
 			for(int i=0;i<procs.length;i++){
 				String name = procs[i].split(":")[1];
 				String freq = MHz[i].split(":")[1];
 				freq = freq.split("MHz")[0];
 				Double frequency = Double.valueOf(freq);
 				Double freqGHz = frequency/1000;
 				processors += name + ": " +freqGHz + ", ";
 			}
 		}
 		catch(Exception e){
 			System.out.println("ScanProcessors: " + e);			
 		}
 	}
 
 	public void scanSystemName(){
 		String command="uname -v;";
 		String tempstring = this.execute(command);
 		System.out.println(tempstring);
 		if(!tempstring.isEmpty()){
 			this.systemstring = tempstring;
 			this.isPingable = true;
 		}
 		
 	}	
 	
 	public void scanCards(){
 		if(this.systemstring.contains("Ubuntu")){
 			String command="lspci -m";
 			String tempstring = this.execute(command);
 			try{
 		//	parsing output
 				String cardlist[] = tempstring.split("00:");
 				this.cards = "";
 				for(int i=0;i<cardlist.length;i++){
 					String infolist[] = cardlist[i].split("\"");
 					if(infolist.length >1){
 						this.cards +=infolist[5] + " " + infolist[3];
 					}
 				}
 			}
 			catch(Exception e){
 				System.out.println("ScanCards: " + e);			
 			}
 		}
 		else {//FreeBSD
 			String command = "pciconf -lv";
 			String tempstring = this.execute(command);
 			try{
 				String cardlist[] = tempstring.split("vendor");
 				String name;
 				String vendor;
 				for(int i=0;i<cardlist.length;i++){
 					name = cardlist[i+1].split("device")[1].split("class")[0];
 					vendor = cardlist[i+1].split("device")[0];
 					this.cards += name + " " + vendor+", ";
 				}
 				
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}	
 	
 	public void shutdown(){
 		this.scanSystemName();
 		String command;
 		if(this.systemstring.contains("Ubuntu")){
 			command = "shutdown -h now";
 		}
 		else{
 			command = "shutdown -p now";
 		}
 		this.sudoExecute(command);
 	}
 	//TODO- fix these suppress warnings... idk
 	@SuppressWarnings("finally")
 	public String execute(String command){
 		 String tempstring ="";
 		try{
 		      JSch jsch=new JSch();
 
 //		      String user="csci3308";
 //		      String host=this.getHostname();
 //		      String pass="Inventory";
 		      
 		      String user="csci3308";
 		      String host=this.hostname;
 		      String pass="Inventory";
 		      
 		      JSch.setConfig("StrictHostKeyChecking", "no");
 		      jsch.setKnownHosts("/home/+"+user+"/.ssh/known_hosts");
 		      
 		      Session session=jsch.getSession(user, host, 22);
 		      session.setPassword(pass);
 		      session.connect();
 
 		      Channel channel=session.openChannel("exec");
 		      ((ChannelExec)channel).setCommand(command);
 
 
 		      channel.setInputStream(null);
 
 		      ((ChannelExec)channel).setErrStream(System.err);
 
 		      InputStream in=channel.getInputStream();
 
 		      channel.connect();
 		      byte[] tmp=new byte[32768];
 		     
 		      while(true){
 		        while(in.available()>0){
 		          int i=in.read(tmp, 0, 1024);
 		          if(i<0)break;
 		          tempstring = new String(tmp, 0, i);
 		          System.out.println(tempstring);
 		        }
 		        if(channel.isClosed()){
 		          System.out.println(command + "exit-status: "+channel.getExitStatus());
 		          break;
 		        }
 		        try{Thread.sleep(1000);}catch(Exception ee) {ee.printStackTrace();}
 		      }
 		      channel.disconnect();
 		      session.disconnect();
 		    }
 		    catch(Exception e){
 		      System.out.println(e);
 		    }
 			finally{
 				return tempstring;
 			}
 
 	}
 	@SuppressWarnings("finally")
 	public String sudoExecute(String command){
 		 String tempstring ="";
 		try{
 		      JSch jsch=new JSch();
 
 //		      String user="csci3308";
 //		      String host=this.getHostname();
 //		      String pass="Inventory";
 //		      
 		      String user="root";
 		      String host=this.hostname;
 		      String pass="Inventory";
 		      
 		      JSch.setConfig("StrictHostKeyChecking", "no");
 		      jsch.setKnownHosts("/home/+"+user+"/.ssh/known_hosts");
 		      
 		      Session session=jsch.getSession(user, host, 22);
 		      session.setPassword(pass);
 		      session.connect();
 
 		      Channel channel=session.openChannel("exec");
 		      ((ChannelExec)channel).setCommand("sudo -S -p '' "+command);
 
 
 		      channel.setInputStream(null);
 
 		      ((ChannelExec)channel).setErrStream(System.err);
 
 		      InputStream in=channel.getInputStream();
 		      OutputStream out=channel.getOutputStream();
 		      channel.connect();
 		      
 		      out.write(("Inventory"+"\n").getBytes());
 		      out.flush();
 		      
 		      byte[] tmp=new byte[32768];
 		     
 		      while(true){
 		        while(in.available()>0){
 		          int i=in.read(tmp, 0, 1024);
 		          if(i<0)break;
 		          tempstring = new String(tmp, 0, i);
 		          System.out.println(tempstring);
 		        }
 		        if(channel.isClosed()){
 		          System.out.println(command + "exit-status: "+channel.getExitStatus());
 		          break;
 		        }
 		        try{Thread.sleep(1000);}catch(Exception ee) {ee.printStackTrace();}
 		      }
 		      channel.disconnect();
 		      session.disconnect();
 		    }
 		    catch(Exception e){
 		      System.out.println(e);
 		    }
 			finally{
 				return tempstring;
 			}
 
 	}
 
 	public String toString(){
 		String serverString = "Server " + this.serverId + ": " + this.hostname + " " + this.ipAddress+ " " + this.macAddress+ " " + this.totalMemory+ " " + this.topHeight+ " " + this.locationId
 				+ " " + this.rackId+ " " + this.model+ " " + this.motherboard+ " " + this.processors+ " " + this.cards+ " " + this.systemstring
 				+ " " + Server.scanDate+ " " + this.serverScanDate;
 		return serverString;
 	}
 	
 }
