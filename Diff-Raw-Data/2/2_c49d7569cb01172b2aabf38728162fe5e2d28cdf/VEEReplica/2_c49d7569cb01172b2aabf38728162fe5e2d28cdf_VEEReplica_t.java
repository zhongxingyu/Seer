 /*
 * Claudia Project
 * http://claudia.morfeo-project.org
 *
 * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
 * S.A.Unipersonal (Telefonica I+D)
 *
 * See CREDITS file for info about members and contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License (AGPL) as 
 * published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Affero GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * If you want to use this software an plan to distribute a
 * proprietary application in any way, and you are not licensing and
 * distributing your source code under AGPL, you probably need to
 * purchase a commercial license of the product. Please contact
 * claudia-support@lists.morfeo-project.org for more information.
 */
 package com.telefonica.claudia.slm.deployment;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import com.telefonica.claudia.slm.deployment.hwItems.CPU;
 import com.telefonica.claudia.slm.deployment.hwItems.CPUConf;
 import com.telefonica.claudia.slm.deployment.hwItems.Disk;
 import com.telefonica.claudia.slm.deployment.hwItems.DiskConf;
 import com.telefonica.claudia.slm.deployment.hwItems.Memory;
 import com.telefonica.claudia.slm.deployment.hwItems.NIC;
 import com.telefonica.claudia.slm.deployment.hwItems.NICConf;
 import com.telefonica.claudia.slm.naming.DirectoryEntry;
 import com.telefonica.claudia.slm.naming.FQN;
 import com.telefonica.claudia.slm.naming.ReservoirDirectory;
 
 @Entity
 public class VEEReplica implements DirectoryEntry {
     
 	/*
 	   INIT      = 0,
     PENDING   = 1,
     HOLD      = 2,
     ACTIVE    = 3,
     STOPPED   = 4,
     SUSPENDED = 5,
     DONE      = 6,
     FAILED    = 7
 	 */
 	public static enum stateType {INIT, PENDING, HOLD, ACTIVE, STOPPED, SUSPENDED, DONE, FAILED};
 	
 	/*
     LCM_INIT       = 0,
     PROLOG         = 1,
     BOOT           = 2,
     RUNNING        = 3,
     MIGRATE        = 4,
     SAVE_STOP      = 5,
     SAVE_SUSPEND   = 6,
     SAVE_MIGRATE   = 7,
     PROLOG_MIGRATE = 8,
     PROLOG_RESUME  = 9,
     EPILOG_STOP    = 10,
     EPILOG         = 11,
     SHUTDOWN       = 12,
     CANCEL         = 13
 	 */
 	public static enum activeSubStateType {INIT, PROLOG, BOOT, RUNNING, MIGRATE, SAVE_STOP, SAVE_SUSPEND, SAVE_MIGRATE, PROLOG_MIGRATE, PROLOG_RESUME, EPILOG_STOP, EPILOG, SHUTDOWN, CANCEL};
 
     
 	@Id
 	@GeneratedValue
 	public long internalId;
 	
 	@Column(columnDefinition = "VARCHAR(32672)")
     private String customizationInformation = null;
     
     @ManyToOne
     private VEE vee = null;
     
     /**
      * Replica number, unique among the replicas of the related VEE.
      */
     private int id = 0;
     
     @Enumerated(EnumType.STRING)
 	private stateType state = null;
     
     @Enumerated(EnumType.STRING)
 	private activeSubStateType subState = null;    
     
 //   private String lbManagementPort = null;
     
     /**
      * The FQN of each component shouldn't be deleted when the VEEReplica is deleted,
      * due to the cycles in the Class diagram between the FQN hierarchy and the data model
      * one. Two remove orders should be issued.
      */
     @OneToOne(cascade={CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST})
     private FQN veeReplicaFQN = null;
     
     @OneToMany(mappedBy="veeReplica", cascade=CascadeType.ALL)
     private Set<Disk> disks = new HashSet<Disk>();
     
     @OneToMany(mappedBy="veeReplica", cascade=CascadeType.ALL)
     private Set<CPU> cpus = new HashSet<CPU>();
     
     @OneToMany(mappedBy="veeReplica", cascade=CascadeType.ALL)
     private Set<NIC> nics = new HashSet<NIC>();
     
     @OneToOne(mappedBy="veeReplica", cascade=CascadeType.ALL)
     private Memory memory = null;
     
 	private String osBoot = null;
 	
 	/**
 	 * OVF document that describe the VM as it was deployed in the underlying infrastructure.
 	 */
 	@Column(columnDefinition = "VARCHAR(32672)")
 	private String ovfRepresentation=null;
     
     /**
      * Identifier of the Virtual Machine represented by the replica in the underlying
      * infraestructure.
      */
     private int infraestructureId;
 
 	private long starTime;
 
 	private long monitoringTime;
     
     public VEEReplica() {}
     
     public VEEReplica(VEE vee) {
     	if(vee == null)
     		throw new IllegalArgumentException("VEE cannot be null");
         this.vee = vee;        
         id = vee.nextVEEReplicaId();        
         state = stateType.INIT;
         
         if(vee.getMemoryConf()!=null)
         	memory = new Memory(vee.getMemoryConf(),this);
         
         List<CPUConf> cpusConfs = vee.getCPUsConf();        
         if(cpusConfs != null) {
         	int cpuId = 1;
         	for(CPUConf cpuConf : cpusConfs)
         		cpus.add(new CPU(cpuId++, cpuConf, this));
         }
         
         List<DiskConf> disksConfs = vee.getDisksConf();
         if(disksConfs != null) {
         	int diskId = 1;
         	for(DiskConf diskConf : disksConfs) {
         		System.out.println(" >>> CREATING DISK FOR REPLICA " + this.getFQN() + ", CONF IS :");
         		System.out.println(" >>> Capacity: " + diskConf.getCapacity());
         		System.out.println(" >>> Type: " + diskConf.getType());
         		System.out.println(" >>> File System: " + diskConf.getFileSystem());
         		System.out.println(" >>> Image URL: " + diskConf.getImageURL());
         		disks.add(new Disk(diskId++, diskConf, this));
         	}
         }
         
         List<NICConf> nicsConfs = vee.getNICsConf();
         if(nicsConfs != null) {
         	int nicId = 1;
         	for(NICConf nicConf : nicsConfs) {
         		NIC newNic = new NIC(nicId++,nicConf, this);
         		newNic.getFQN();
         		nics.add(newNic);
         	}
         }
         
 		Set<Disk> disks;
 		disks = getDisks();
 		Iterator<Disk> diskIt = disks.iterator();
 
 		while (diskIt.hasNext()) {
 			// create disk
 			Disk disk = diskIt.next();
 			
 			if (disk.getDiskConf().getImageURL() != null) {
 				
 				String urlDisk;
 				
 				if (disk.getDiskConf().getImageURL().toString().contains("file:/"))
 					urlDisk = disk.getDiskConf().getImageURL().toString().replace("file:/", "file:///");
 				else
 					urlDisk = disk.getDiskConf().getImageURL().toString();
 				
 				disk.setUrlImage(urlDisk);
 			}
 			
 			disk.setReadOnly(false);
 			disk.setCloneDisk(true);
 		}
         
         this.getFQN();
     }
     
     @SuppressWarnings("unchecked")
 	public void registerHwElementsInResDir() {
     	if(memory != null)
     		ReservoirDirectory.getInstance().registerObject(memory.getFQN(), memory);
     	for(Disk disk : disks)
     		ReservoirDirectory.getInstance().registerObject(disk.getFQN(), disk);
     	for(CPU cpu : cpus)
     		ReservoirDirectory.getInstance().registerObject(cpu.getFQN(), cpu);
     	for(NIC nic : nics)
     		ReservoirDirectory.getInstance().registerObject(nic.getFQN(), nic);    		
     }
 
 	public stateType getVEEReplicaVmState() {
 		return state;
 	}
 	
 	public void setVEEReplicaVmState(stateType replicaState) {
 		this.state = replicaState;
 	}
 	
 	public activeSubStateType getVEEReplicaVmSubState() {
 		return subState;
 	}
 
 	public void setVEEReplicaVmSubState(activeSubStateType replicaSubState) {
 		this.subState = replicaSubState;
 	}
     
     public String getOVFEnvironment() {
         return customizationInformation;
     }
     
     public void setOVFEnvironment(String env) {
         customizationInformation=env;
     }
 
     public int getId() {
         return id;
     }
     
     public void setId(int id) {
     	this.id = id;
     }
 
     public int getInfraestructureId() {
         return infraestructureId;
     }
     
     public void setInfraestructureId(int id) {
     	this.infraestructureId = id;
     }
     
     public VEE getVEE() {
         return vee;
     }
     
     public void addDisk(Disk disk) {
         disks.add(disk);
     }
     
     public Set<Disk> getDisks() {
         return disks;
     }
     
     public void addCPU(CPU cpu) {
         cpus.add(cpu);
     }
     
     public Set<CPU> getCPUs() {
         return cpus;
     }
     
     public void addNIC(NIC nic) {
         nics.add(nic);
     }
     
     public Set<NIC> getNICs() {
         return nics;
     }
     
     public void setMemory(Memory memory) {
         this.memory = memory;
     }
     
     public Memory getMemory() {
         return memory;
     }    
     
 	public String getOsBoot() {
 		return osBoot;
 	}
 	
 	public void setOsBoot(String osBoot) {
 		this.osBoot = osBoot;
 	}
     
     public FQN getFQN() {
         if(veeReplicaFQN == null)
             veeReplicaFQN = ReservoirDirectory.getInstance().buildFQN(this);
         return veeReplicaFQN;
     }
     
     @Override
     public String toString() {
         return getFQN().toString();
     }
     
     @Override
     public int hashCode() {
         return getFQN().hashCode();
     }
     
     @Override
     public boolean equals(Object object) {
         
         if(object == null)
             return false;
         
         if(!(object instanceof VEEReplica))
             return false;
         
         return ((VEEReplica)object).getFQN().equals(getFQN());
         
     }
 
 	public void setStartTime(long parseLong) {
 		this.starTime= parseLong;
 	}
 
 	public long getStartTime() {
 		return this.starTime;
 	}
 	
 	public void setMonitoringTime(long parseLong) {
 		this.monitoringTime = parseLong;
 	}
 	
 	public long getMonitoringTime() {
 		return this.monitoringTime;
 	}
 
 	public void setOvfRepresentation(String ovfRepresentation) {
 		this.ovfRepresentation = ovfRepresentation;
 	}
 
 	public String getOvfRepresentation() {
 		return ovfRepresentation;
 	}
 	
 
 }
