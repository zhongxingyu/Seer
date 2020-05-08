 
 package org.jims.modules.crossbow.objectmodel.resources;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * Appliance Node
  *
  * @author robert boczek
  */
 public class Appliance extends Resource {
 
     /**
      * Variable identifies name of remote zone to be installed
      */
     public Appliance(String resourceId, String projectId, ApplianceType type) {
         super(resourceId, projectId);
 
 				this.type = type;
     }
 
     public Appliance(String resourceId, String projectId, ApplianceType type, String repoId) {
         this(resourceId, projectId, type);
         this.repoId = repoId;
     }
 
 
 	public void addInterface( Interface p ) {
 
 		interfaces.add( p );
 
 		if ( ( null == p.getAppliance() )
 		     || ( ! this.getUUID().equals( p.getAppliance().getUUID() ) ) ) {
 			p.setAppliance( this );
 		}
 
 	}
 
 	public Interface getInterface( int i ) {
 		return interfaces.get( i );
 	}
 
 	public List< Interface > getInterfaces() {
 		return interfaces;
 	}
 
 
     /**
      * Get the value of repoId
      *
      * @return the value of repoId
      */
     public String getRepoId() {
         return repoId;
     }
 
     /**
      * Set the value of repoId
      *
      * @param repoId new value of repoId
      */
     public void setRepoId(String repoId) {
         this.repoId = repoId;
     }
 
 	public ApplianceType getType() {
 		return type;
 	}
 
 	public void setInterfaces(List< Interface > interfaces) {
 		this.interfaces = interfaces;
 	}
 
 	public UUID getUUID(){
 		return this.uuid;
 	}
 
 
 	@Override
 	public boolean equals( Object o ) {
 
 		boolean res = false;
 
 		if ( this == o ) {
 
 			res = true;
 
 		} else if ( o instanceof Appliance ) {
 
 			Appliance app = ( Appliance ) o;
 
 			res = ( type.equals( app.type ) && repoId.equals( app.repoId )
 			        && getResourceId().equals( app.getResourceId() )
 			        && getProjectId().equals( app.getProjectId() )
 			        && uuid.equals( app.getUUID() ) );
 
 		}
 
 		return res;
 
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 7;
 		hash = 97 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
 		return hash;
 	}
 
 
 	private ApplianceType type;
 	private String repoId = "";  // TODO  this is temporary and should be removed (GUI issue)
 	private List< Interface > interfaces = new LinkedList< Interface >();
 	private UUID uuid = UUID.randomUUID();
 
 }
