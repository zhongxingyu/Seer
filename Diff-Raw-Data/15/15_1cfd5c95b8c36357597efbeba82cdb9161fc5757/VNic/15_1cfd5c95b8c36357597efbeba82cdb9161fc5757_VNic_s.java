 package org.jims.modules.crossbow.link;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import org.jims.modules.crossbow.enums.LinkParameters;
 import org.jims.modules.crossbow.exception.LinkException;
 import org.jims.modules.crossbow.lib.VNicHelper;
 import org.apache.log4j.Logger;
 
 /**
  * Implements all method from @see VNicMBean
  * 
  * @author robert boczek
  */
 public class VNic extends Link implements VNicMBean {
 
     private static final Logger logger = Logger.getLogger(VNic.class);
     private String parent;
     private final boolean temporary;
     private boolean plumbed;
     private boolean up;
     private boolean linkParent;
     private VNicHelper vNicHelper;
 
     /**
      * Constructor of VNic class
      *
      * @param name Name of vnic
      * @param temporary Specifies whether this vnic will exist between reboots
      * @param parent Name of link under whom this vnic works
      */
     public VNic(String name, boolean temporary, String parent) {
         super(name);
         this.temporary = temporary;
         this.parent = parent;
 
         //this.etherstubStatisticsGatherer = new EtherstubStatisticsGatherer(name);
     }
 
     /**
      * @see VNicMBean#isTemporary()
      */
     @Override
     public boolean isTemporary() {
         return this.temporary;
     }
 
     /**
      * @see VNicMBean#isEtherstubParent() 
      */
     @Override
     public boolean isEtherstubParent() throws LinkException {
         //@todo use jna to get info whether etherstub is parent of this vnic
         return linkParent;
     }
 
     /**
      * @see VNicMBean#getParent() 
      */
     @Override
     public String getParent() throws LinkException {
         
         if(this.parent == null){
             this.parent = this.linkHelper.getLinkParameter(this.name, LinkParameters.OVER);
         }
         return this.parent;
     }
 
 		@Override
 		public String getIpAddress() {
 
 			String res = "0.0.0.0";
 			String cmd[] = { "zlogin",
 			                 this.name.replaceAll( "\\.\\.IFACE[0-9]", "" ),  // TODO-DAWID  < this is temporary
 			                 "ifconfig " + this.name + " | grep inet | sed 's/.*inet \\([.0-9]*\\).*/\\1/'" };
 
 			try {
 
 				Process proc = Runtime.getRuntime().exec( cmd );
 				proc.waitFor();
 				res = new BufferedReader( new InputStreamReader( proc.getInputStream() ) ).readLine();
 
 			} catch ( Exception ex ) {
 				logger.error( "Exception while getting IP address.", ex );
 			}
 
 			return res;
 
 		}
 
 		@Override
 		public String getIpMask() {
 			return "255.255.255.0";  // TODO-DAWID  < temporary
 		}
 
     @Override
     public int hashCode() {
 
         int hash = 7;
         hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
 
         return hash;
 
     }
 
     @Override
     public boolean equals(Object o) {
 
         if (o == this) {
             return true;
         } else if (o instanceof VNic) {
             return name.equals(((VNic) o).getName());
         } else {
             return false;
         }
     }
 
     @Override
     public String toString() {
         return this.name;
     }
 
     public void setVNicHelper(VNicHelper vNicHelper) {
         this.vNicHelper = vNicHelper;
         super.linkHelper = vNicHelper;
     }
 }
