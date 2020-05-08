 package com.eucalyptus.webui.shared.resource;
 
 public class Template {
 	private String mark;
 	private String cpu;
 	private String mem;
 	private String disk;
 	private String bw;
 	private String image;
	private String ncpus;
 	
	public String getNcpus() {
    return ncpus;
  }
  public void setNcpus(String ncpus) {
    this.ncpus = ncpus;
  }
  
  public void setMark(String mark) {
 		this.mark = mark;
 	}
 	public String getMark() {
 		return this.mark;
 	}
 	
 	public void setCPU(String cpu) {
 		this.cpu = cpu;
 	}
 	public String getCPU() {
 		return this.cpu;
 	}
 	
 	public void setMem(String mem) {
 		this.mem = mem;
 	}
 	public String getMem() {
 		return this.mem;
 	}
 	
 	public void setDisk(String disk) {
 		this.disk = disk;
 	}
 	public String getDisk() {
 		return this.disk;
 	}
 	
 	public void setBw(String bw) {
 		this.bw = bw;
 	}
 	public String getBw() {
 		return this.bw;
 	}
 	
 	public void setImage(String image) {
 		this.image = image;
 	}
 	public String getImage() {
 		return this.image;
 	}
 }
