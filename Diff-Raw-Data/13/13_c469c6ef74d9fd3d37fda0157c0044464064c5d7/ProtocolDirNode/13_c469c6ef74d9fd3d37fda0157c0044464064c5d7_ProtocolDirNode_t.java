 package module.protocols.domain;
 
 import jvstm.cps.ConsistencyPredicate;
 import pt.ist.bennu.core.domain.groups.PersistentGroup;
 import pt.ist.bennu.core.domain.groups.UnionGroup;
 
 public class ProtocolDirNode extends ProtocolDirNode_Base {
 
     /*
      * Node associated to the protocols
      */
     public ProtocolDirNode(ProtocolAuthorizationGroup group, String name, PersistentGroup readers) {
 	super();
 	setWriters(group);
 	setName(name);
 	setReadGroup(readers);
	setQuota((long) 50 * 1024 * 1024);
 	createTrashFolder();
     }
 
     /*
      * Node associated with the authorization groups
      */
     public ProtocolDirNode(ProtocolAuthorizationGroup group) {
 	super();
 
 	setOwnerGroup(group);
 
 	setWriteGroup(UnionGroup.getOrCreateUnionGroup(group.getAuthorizedWriterGroup(), ProtocolManager.getInstance()
 		.getAdministrativeGroup()));
 
 	createTrashFolder();
     }
 
     public void setWriters(ProtocolAuthorizationGroup group) {
 	this.setParent(group.getGroupDir());
     }
 
     @Override
     @ConsistencyPredicate
     public boolean checkParent() {
 	return super.checkParent() ? true : hasOwnerGroup();
     }
 
     @Override
     public void delete() {
 	removeWriteGroup();
 	removeOwnerGroup();
 	removeTrash();
 	super.delete();
     }
 
 }
