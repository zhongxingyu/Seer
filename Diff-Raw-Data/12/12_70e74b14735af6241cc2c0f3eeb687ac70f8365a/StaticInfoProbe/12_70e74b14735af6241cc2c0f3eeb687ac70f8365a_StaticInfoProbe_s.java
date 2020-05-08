 package eu.celarcloud.celar_ms.ProbePack.ProbeLibrary;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import eu.celarcloud.celar_ms.ProbePack.Probe;
 import eu.celarcloud.celar_ms.ProbePack.ProbeMetric;
 import eu.celarcloud.celar_ms.ProbePack.ProbePropertyType;
 
 public class StaticInfoProbe extends Probe{
 		
 	public StaticInfoProbe(String name, int freq){
 		super(name,freq);
 		this.addProbeProperty(0,"os",ProbePropertyType.STRING,"","OS release and version");
 		this.addProbeProperty(1,"arch",ProbePropertyType.STRING,"","Machine architecture");
 		this.addProbeProperty(2,"cpuNum",ProbePropertyType.STRING,"","Number of CPUs");
 		this.addProbeProperty(3,"btime",ProbePropertyType.STRING,"","boot time");
 		
 		this.setPullableFlag(true);
 	}
 	
 	public StaticInfoProbe(){
		this("StaticInfoProbe", 30); //don't really care about period since info is only collected once
 	}
 
 	@Override
 	public String getDescription() {
 		return "Probe that collects static info related to the OS and System at MS startup";
 	}
 
 	@Override
 	public ProbeMetric collect() {
         HashMap<Integer,Object> values = new HashMap<Integer,Object>();
         
 //      System.out.println(this.getOSRelease()+" "+this.getMachineArch()+" "+this.getNumCPU()+" "+this.getBootTime());
         
         values.put(0, this.getOSRelease());
         values.put(1, this.getMachineArch());
         values.put(2, this.getNumCPU());
         values.put(3, this.getBootTime());
 		return new ProbeMetric(values);
 	}
 	
 	private String getOSRelease(){
 		String os = "Linux";
 		try{	
 			String[] cmd = {"/bin/sh","-c","cat /etc/*-release"};
 			Process p = Runtime.getRuntime().exec(cmd);
 			p.waitFor();
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String line = "";
 			while ((line = b.readLine()) != null){
 				if (line.contains("CentOS"))
 					os = line;
 				else if (line.contains("PRETTY_NAME"))
 					os = line.split("=")[1].replace("\"", ""); // Debian or Ubuntu
 			}
 	        b.close();	
 		}
 		catch(Exception e){
 			this.writeToProbeLog(Level.SEVERE, "Couldn't read OS release"); // going to return linux
 		}
 		
 		return os;
 	}
 	
 	private String getMachineArch(){
 		String arch = "";
 		try{
 			Process p = Runtime.getRuntime().exec("uname -m");
 			p.waitFor();
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			arch = b.readLine();
 	        b.close();
 		}
 		catch(Exception e){
 			this.writeToProbeLog(Level.SEVERE, "Couldn't read machine arch release"); // going to return empty string
 		}	
 		return arch;
 	}
 	
 	private String getNumCPU(){
 		String cpu = "1";
 		String[] cmd = {"/bin/sh","-c","cat /proc/cpuinfo | grep -c processor"};
 		try{
 			Process p = Runtime.getRuntime().exec(cmd);
 			p.waitFor();
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			cpu = b.readLine();
 	        b.close();
 		}
 		catch(Exception e){
 			this.writeToProbeLog(Level.SEVERE, "Couldn't read number of CPUs"); // going to return 1 CPU
 		}	
 		return cpu;		
 	}
 	
 	private String getBootTime(){
 		String btime = "";
 		String[] cmd = {"/bin/sh","-c","cat /proc/stat | grep btime"};
 		try{
 			Process p = Runtime.getRuntime().exec(cmd);
 			p.waitFor();
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			btime = b.readLine();
 			if(!btime.equals(""))
 				btime = btime.split(" ")[1];				
 	        b.close();
 		}
 		catch(Exception e){
 			this.writeToProbeLog(Level.SEVERE, "Couldn't read boot time"); // going to return empty string
 		}	
 		return btime;		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		StaticInfoProbe s = new StaticInfoProbe();
 		s.collect();
 	}
 }
