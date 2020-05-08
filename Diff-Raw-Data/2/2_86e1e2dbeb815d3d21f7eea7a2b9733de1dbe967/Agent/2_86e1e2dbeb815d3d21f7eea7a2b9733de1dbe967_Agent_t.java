 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.agent;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.beequeue.launcher.BeeQueueHome;
 import org.beequeue.worker.WorkerData;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 import org.hyperic.sigar.SigarProxy;
 import org.hyperic.sigar.cmd.Shell;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectWriter;
 
 public class Agent {
 	public final static ObjectMapper om = new ObjectMapper();
 	
 	private static final String PS_CMD = "ps";
 	private static final String CPU_CMD = "cpu";
 	private static final String MEM_CMD = "mem";
 
 	final public Shell shell = new Shell();
 	final public Sigar sigar = shell.getSigar();
     final public SigarProxy proxy = shell.getSigarProxy();
     public File outputDirectory;
 
 	public String timestamp;
     
 	public ProcRawData[] getStatusOfProcesses()
 			throws SigarException {
 		long[] pidList = this.proxy.getProcList();
 		ProcRawData[] ps = new ProcRawData[pidList.length];
 		for (int i = 0; i < pidList.length; i++) {
 			long pid = pidList[i];
 			ps[i]=new ProcRawData(pid,this);
 		}
 		return ps;
 	}
 	
 	public MemRawData getMemoryData() throws SigarException {
 		MemRawData memInfo = new MemRawData();
 		memInfo.mem = this.sigar.getMem();
 		memInfo.swap = this.sigar.getSwap();
 		return memInfo;
 	}
 	
 	
 	public CpuRawData getCpuData() 
 			throws SigarException {
 		CpuRawData o = new CpuRawData();
 		o.info = this.sigar.getCpuInfoList()[0];
 		o.all = this.sigar.getCpuPercList();
 		o.total = this.sigar.getCpuPerc();
 		return o;
 	}
 
 	
     public final static String SIGNAL = "SIGTERM";
 	public void kill(long[] split) throws SigarException {
 		for (int i = 0; i < split.length; i++) {
 			this.sigar.kill(split[i], SIGNAL);
 		}
 	}
 
 
 	public void runStatistics() throws IOException, SigarException {
 		ensureDirectoryAndSetTimestamp();
 		CpuRawData cpuData = getCpuData();
 		MemRawData memoryData = getMemoryData();
 		WorkerData.instance.calcHostStatistics(cpuData,memoryData);
 		dump(CPU_CMD,cpuData);
 		dump(MEM_CMD,memoryData);
 	}
 	
 	public ProcRawData[] runProcessStatistics() throws IOException, SigarException {
 		ensureDirectoryAndSetTimestamp();
 		ProcRawData[] statusOfProcesses = getStatusOfProcesses();
 		WorkerData.instance.updateStatusOfProcesses(statusOfProcesses);
 		dump(PS_CMD,statusOfProcesses);
 		return statusOfProcesses;
 	}
 	
 
 	public void ensureDirectoryAndSetTimestamp() {
 		this.outputDirectory = BeeQueueHome.instance.getHost();
 		if(!outputDirectory.isDirectory() && !outputDirectory.mkdirs() ){
 			System.err.println("Cannot use or create "+ outputDirectory  );
 		}
		this.timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
 	}
 
 
 	public void dump(String metricName, Object systemMetric) throws IOException {
 		File f = outputDirectory;
 		f = new File(f, metricName); if( !f.isDirectory() && !f.mkdirs() ) throw new FileNotFoundException(f.toString());
 		File dumpFile = new File(f,timestamp+".txt");
 		FileWriter w = new FileWriter(dumpFile);
 		System.out.println("@Artifact: "+metricName+": "+dumpFile);
 		ObjectWriter pp = om.writerWithDefaultPrettyPrinter();
 		w.write(pp.writeValueAsString(systemMetric));
 		w.close();
 	}
 
 
 }
