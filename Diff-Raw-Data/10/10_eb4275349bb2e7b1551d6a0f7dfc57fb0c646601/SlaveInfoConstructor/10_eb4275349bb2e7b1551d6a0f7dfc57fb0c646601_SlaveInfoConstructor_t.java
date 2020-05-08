 package com.klose.Slave;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
import org.hyperic.jni.ArchName;
import org.hyperic.jni.ArchNotSupportedException;
 import org.hyperic.sigar.CpuPerc;
 import org.hyperic.sigar.FileSystem;
 import org.hyperic.sigar.FileSystemUsage;
 import org.hyperic.sigar.NfsFileSystem;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 import org.hyperic.sigar.cmd.SigarCommandBase;
 
 import com.klose.MsConnProto;
 import com.klose.MsConnProto.SlaveInfo;
 import com.klose.MsConnProto.SlaveInfo.Builder;
 import com.klose.MsConnProto.SlaveInfo.cpuInfo;
 import com.klose.MsConnProto.SlaveInfo.diskInfo;
 import com.klose.MsConnProto.SlaveInfo.memInfo;
 
 public class SlaveInfoConstructor extends SigarCommandBase{
 	private  String ip_port; //identify the slave node
 	private  String workdir; //specify the working directory that slave uses
 	static{
		try {
			System.loadLibrary("sigar-"+ArchName.getName());
		} catch (ArchNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	SlaveInfoConstructor(String ip_port, String workdir) {
 		this.ip_port = ip_port;
 		this.workdir = workdir;
 	}
 	@Override
 	public void output(String[] args) throws SigarException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * assemble the information of cpu, memory and disk usage about slave.
 	 * ip_port and workdir can specify slave.   
 	 * @param ip_port : identify the machine
 	 * @param workdir : the working directory that slave uses
 	 * @return SlaveInfo
 	 * @throws SigarException 
 	 */
 	public SlaveInfo assemble() throws SigarException {
 		SlaveInfo.Builder infoBuilder = SlaveInfo.newBuilder();
 		infoBuilder.setIpPort(this.ip_port);
 		infoBuilder.setWorkDir(this.workdir);
 		infoBuilder.addAllDiskInfo(new diskInfoIterable(assembleDiskInfo()));
 		infoBuilder.setCpuInfo(assembleCpuInfo());
 		infoBuilder.setMemInfo(assembleMemInfo());
 		return infoBuilder.build();
 	}
 	
 	private  SlaveInfo.cpuInfo assembleCpuInfo() throws SigarException {
 		 org.hyperic.sigar.CpuInfo[] infos =
 	            this.sigar.getCpuInfoList();
 	        CpuPerc[] cpus =
 	            this.sigar.getCpuPercList();
 	        org.hyperic.sigar.CpuInfo info = infos[0];
 		return SlaveInfo.cpuInfo.newBuilder().setVendor(info.getVendor())
 					.setModel(info.getModel())
 					.setMhz(String.valueOf((info.getMhz())))
 					.setTotalCpus(String.valueOf(info.getTotalCores()))
 					.setTotalIdleTime(CpuPerc.format(this.sigar.getCpuPerc().getIdle()))
 					.build();
 	}
 	
 	private SlaveInfo.memInfo assembleMemInfo() throws SigarException {
 		 org.hyperic.sigar.Mem memInfo = this.sigar.getMem();
 		 return SlaveInfo.memInfo.newBuilder()
 		 			.setTotalMemory(memInfo.getTotal()/1024/1024 +"M")
 		 			.setUsed( String.format("%.2f",memInfo.getUsedPercent())+ "%")
 		 			.setAvail(String.format("%.2f", memInfo.getFreePercent())+"%" )
 		 			.build();
 	}
 	
 	private class diskInfoIterable implements java.lang.Iterable<SlaveInfo.diskInfo>{
 
 		ArrayList<SlaveInfo.diskInfo> diskInfoList;
 		diskInfoIterable(ArrayList<SlaveInfo.diskInfo> list){
 			this.diskInfoList = list;
 		}
 		@Override
 		public Iterator<SlaveInfo.diskInfo> iterator() {
 			// TODO Auto-generated method stub
 			
 			return diskInfoList.iterator();
 		}
 		
 	}
 	private ArrayList<SlaveInfo.diskInfo> assembleDiskInfo() throws SigarException {
 		FileSystem[] fslist = this.proxy.getFileSystemList();
 		ArrayList<SlaveInfo.diskInfo> infoList = new ArrayList<SlaveInfo.diskInfo>();
 		for(FileSystem fs : fslist) {
 			SlaveInfo.diskInfo info = null;
 			 FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
 			 long used, avail, total, pct;
 			 used = usage.getTotal() - usage.getFree();
 		     avail = usage.getAvail();
 		     total = usage.getTotal();
 		     pct = (long)(usage.getUsePercent() * 100);
 		     String usePct;
 		        if (pct == 0) {
 		            usePct = "-";
 		        }
 		        else {
 		            usePct = pct + "%";
 		        }		   
 		        infoList.add(info.newBuilder().setDevName((fs.getDevName()))
 				        .setSize((formatSize(total)))
 				        .setUsed((formatSize(used)))
 				        .setAvail((formatSize(avail)))
 				        .setUsedPct(usePct)
 				        .setDirName((fs.getDirName()))		      
 				        .setType((fs.getSysTypeName() + "/" + fs.getTypeName()))
 				        .build());        
 		}
 		return infoList;
 	}
 	 private static String formatSize(long size) {
 	        return Sigar.formatSize(size * 1024);
 	    }
 	
 }
