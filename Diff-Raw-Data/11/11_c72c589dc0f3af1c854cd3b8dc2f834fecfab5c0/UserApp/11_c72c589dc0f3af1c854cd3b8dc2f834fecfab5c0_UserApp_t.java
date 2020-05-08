 package com.eucalyptus.webui.shared.user;
 
 import java.util.Date;
 
 public class UserApp implements java.io.Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private int id;
 	private Date apptime;
 	private EnumUserAppStatus status;
 	private String comment;
 	private String keyPair;
 	private String securityGroup;
 	
 	private int ncpus;
 	private int mem;
 	private int disk;
 	private int bw;
 	
 	private int userId;
 	private int vmImageTypeId;
 	
 	private Date srvStartingTime;
 	private Date srvEndingTime;
 	
 	private String euca_vm_instance_key;
 	
 	private int cpu_srv_id;
 	private int mem_srv_id;
 	private int disk_srv_id;
 	
 	private int bw_srv_id;
 	private int public_ip_srv_id;
 	private int private_ip_srv_id;
 	
 	public UserApp() {
 		
 	}
 	
 	public UserApp(int id, Date apptime, EnumUserAppStatus status, int del, String comment,
 					int ncpus, int mem, int disk, int bw,
 					String keyPair, String securityGroup, int userId, int vmImageTypeId, Date srvStartingTime, Date srvEndingTime, String euca_vm_instance_key,
 					int cpu_srv_id, int mem_srv_id, int disk_srv_id, int bw_srv_id, int public_ip_srv_id, int private_ip_srv_id) {
 		this.setUAId(id);
 		this.setAppTime(apptime);
		this.setStatus(status);
 		this.setSrvStartingTime(srvStartingTime);
 		this.setSrvEndingingTime(srvEndingTime);
 		this.setComment(comment);
 		this.setNcpus(ncpus);
 		this.setMem(mem);
 		this.setDisk(disk);
 		this.setBw(bw);
 		this.setKeyPair(keyPair);
 		this.setSecurityGroup(securityGroup);
 		this.setUserId(userId);
 		this.setVmImageTypeId(vmImageTypeId);
 		this.setEucaVMInstanceKey(euca_vm_instance_key);
 		this.setCPUSrvId(cpu_srv_id);
 		this.setMemSrvId(mem_srv_id);
 		this.setDiskSrvId(disk_srv_id);
 		this.setBwSrvId(bw_srv_id);
 		this.setPubIpSrvId(public_ip_srv_id);
 		this.setPriIpSrvId(private_ip_srv_id);
 	}
 	
 	public void setUAId(int ua_id) {
 		this.id = ua_id;
 	}
 	public int getUAId() {
 		return this.id;
 	}
 	
 	public void setAppTime(Date apptime) {
 		this.apptime = apptime;
 	}
 	public Date getAppTime() {
 		return this.apptime;
 	}
 	
 	public void setStatus(EnumUserAppStatus status) {
 		this.status = status;
 	}
 	public EnumUserAppStatus getStatus() {
 		return this.status;
 	}
 	
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 	public String getComments() {
 		return this.comment;
 	}
 	
 	public void setNcpus(int ncpus) {
 		this.ncpus = ncpus;
 	}
 	public int getNcpus() {
 		return this.ncpus;
 	}
 	
 	public void setMem(int mem) {
 		this.mem = mem;
 	}
 	public int getMem() {
 		return this.mem;
 	}
 	
 	public void setDisk(int disk) {
 		this.disk = disk;
 	}
 	public int getDisk() {
 		return this.disk;
 	}
 	
 	public void setBw(int bw) {
 		this.bw = bw;
 	}
 	public int getBw() {
 		return this.bw;
 	}
 	
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 	public int getUserId() {
 		return this.userId;
 	}
 	
 	public void setVmImageTypeId(int vmImageTypeId) {
 		this.vmImageTypeId = vmImageTypeId;
 	}
 	public int getVmIdImageTypeId() {
 		return this.vmImageTypeId;
 	}
 	
 	public void setSrvStartingTime(Date startingTime) {
 		this.srvStartingTime = startingTime;
 	}
 	public Date getSrvStartingTime() {
 		return this.srvStartingTime;
 	}
 	
 	public void setSrvEndingingTime(Date endingTime) {
 		this.srvEndingTime = endingTime;
 	}
 	public Date getSrvEndingTime() {
 		return this.srvEndingTime;
 	}
 	
 	public void setKeyPair(String keyPair) {
 		this.keyPair = keyPair;
 	}
 	public String getKeyPair() {
 		return this.keyPair;
 	}
 	
 	public void setSecurityGroup(String securityGroup) {
 		this.securityGroup = securityGroup;
 	}
 	public String getSecurityGroup() {
 		return this.securityGroup;
 	}
 	
 	public void setEucaVMInstanceKey(String euca_vm_instance_key) {
 		this.euca_vm_instance_key = euca_vm_instance_key;
 	}
 	public String getEucaVMInstanceKey() {
 		return this.euca_vm_instance_key;
 	}
 	
 	public void setCPUSrvId(int cpu_srv_id) {
 		this.cpu_srv_id = cpu_srv_id;
 	}
 	public int getCPUSrvId() {
 		return this.cpu_srv_id;
 	}
 	
 	public void setMemSrvId(int mem_srv_id) {
 		this.mem_srv_id = mem_srv_id;
 	}
 	public int getMemSrvId() {
 		return this.mem_srv_id;
 	}
 	
 	public void setDiskSrvId(int disk_srv_id) {
 		this.disk_srv_id = disk_srv_id;
 	}
 	public int getDiskSrvId() {
 		return this.disk_srv_id;
 	}
 	
 	public void setBwSrvId(int bw_srv_id) {
 		this.bw_srv_id = bw_srv_id;
 	}
 	public int getBwSrvId() {
 		return this.bw_srv_id;
 	}
 	
 	public void setPubIpSrvId(int public_ip_srv_id) {
 		this.public_ip_srv_id = public_ip_srv_id;
 	}
 	public int getPubIpSrvId() {
 		return this.public_ip_srv_id;
 	}
 	
 	public void setPriIpSrvId(int private_ip_srv_id) {
 		this.private_ip_srv_id = private_ip_srv_id;
 	}
 	public int getPriIpSrvId() {
 		return this.private_ip_srv_id;
 	}
 }
