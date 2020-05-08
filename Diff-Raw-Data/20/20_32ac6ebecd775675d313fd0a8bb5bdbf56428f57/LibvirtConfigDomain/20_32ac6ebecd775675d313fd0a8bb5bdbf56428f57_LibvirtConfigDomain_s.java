 package org.inria.myriads.libvirt.domain;
 
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 /**
  * 
  * Libvirt domain class.
  * 
  * @author msimonin
  *
  */
 /**
  * @author msimonin
  *
  */
 @XmlRootElement(name = "domain")
@XmlType (name = "", propOrder = {"uuid", "name", "memory", "vcpus", "os", "features", "devices" })
 public class LibvirtConfigDomain 
 {
     /** virtualization type. */
     private String type_;
     
     /** domain uuid.*/
     private String uuid_;
     
     /** domain name.*/
     private String name_;
     
     /** domain vcpus.*/
    private int vcpus_ = 1;
     
     /** domain memory.*/
    private long memory_ = 1024 * 128;
 
     /** domain os.*/
     private LibvirtConfigOs os_;
 
     /** devices.*/
     private LibvirtConfigDevices devices_;
     
     /** features.*/
     private LibvirtConfigFeatures features_;
     
 
     /**
      * 
      */
     public LibvirtConfigDomain()
     {
        os_ = new LibvirtConfigOs();
        devices_ = new LibvirtConfigDevices();
        features_ = new LibvirtConfigFeatures();
     }
 
     /**
      * @return the type
      */
     @XmlAttribute(name = "type")
     public String getType() 
     {
         return type_;
     }
 
     /**
      * @param type the type to set
      * @return this
      */
     public LibvirtConfigDomain setType(String type)
     {
         this.type_ = type;
         return this;
     }
 
     /**
      * @return the uuid_
      */
     public String getUuid_() 
     {
         return uuid_;
     }
 
     /**
      * @param uuid the uuid to set
      * @return this
      */
     public LibvirtConfigDomain setUuid(String uuid)
     {
         this.uuid_ = uuid;
         return this;
     }
 
     /**
      * @return the name
      * 
      */
     public String getName() 
     {
         return name_;
     }
 
     /**
      * @param name the name to set
      * @return this
      */
     public LibvirtConfigDomain setName(String name) 
     {
         this.name_ = name;
         return this;
     }
 
     /**
      * @return the vcpus_
      */
    public int getVcpus()
     {
        return vcpus_;
     }
 
     /**
      * @param vcpus the vcpus to set
      * @return this
      */
    public LibvirtConfigDomain setVcpus(int vcpus)
     {
        this.vcpus_ = vcpus;
         return this;
     }
 
     /**
      * @return the memory
      */
     public long getMemory() 
     {
         return memory_;
     }
 
     /**
      * @param memory the memory to set
      * @return this
      */
     public LibvirtConfigDomain setMemory(long memory) 
     {
         this.memory_ = memory;
         return this;
     }
 
     /**
      * @return the os
      */
     public LibvirtConfigOs getOs()
     {
         return os_;
     }
 
     /**
      * @param os the os to set
      * @return this
      */
     public LibvirtConfigDomain setOs(LibvirtConfigOs os)
     {
         os_ = os;
         return this;
     }
 
     /**
      * @return the devices
      */
     public LibvirtConfigDevices getDevices()
     {
         return devices_;
     }
 
     /**
      * @param devices the devices to set
      * @return this
      */
     public LibvirtConfigDomain setDevices(LibvirtConfigDevices devices)
     {
         devices_ = devices;
         return this;
     }
 
     /**
      * @return the uuid
      */
     public String getUuid()
     {
         return uuid_;
     }
 
     /**
      * @return the features
      */
     public LibvirtConfigFeatures getFeatures()
     {
         return features_;
     }
 
     /**
      * @param features the features to set
      * @return this
      */
     public LibvirtConfigDomain setFeatures(LibvirtConfigFeatures features)
     {
         features_ = features;
         return this;
     }
 
     /**
      * 
      * Add a disk.
      * 
      * @param disk      The disk to add.
      * @return 
      * @return this
      */
     public LibvirtConfigDomain addDisk(LibvirtConfigDisk disk)
     {
         getDevices().addDisk(disk);
         return this;
     }
   
     /**
      * 
      * Add an interface.
      * 
      * @param interf     The interface to add.
      * @return this
      */
     public LibvirtConfigDomain addInterface(LibvirtConfigInterface interf)
     {
         getDevices().addInterface(interf);
         return this;
     }
     
     /**
      * 
      * Add an interface.
      * 
      * @param serial     The serial to add.
      * @return this
      */
     public LibvirtConfigDomain addSerial(LibvirtConfigSerialConsole serial)
     {
         getDevices().addSerial(serial);
         return this;
     }
     
     /**
      * 
      * Add an interface.
      * 
      * @param console   The console to add.
      * @return this
      */
     public LibvirtConfigDomain addConsole(LibvirtConfigSerialConsole console)
     {
         getDevices().addSerial(console);
         return this;
     }
 
     /**
      * 
      * Sets the os type.
      * 
      * @param type  the type
      */
     public void setOsType(String type)
     {
         getOs().setType(type);
     }
 }
