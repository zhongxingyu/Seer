 package org.wyona.security.impl.yarep;
 
 import java.util.Vector;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfiguration;
 import org.apache.log4j.Logger;
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.Item;
 import org.wyona.security.core.api.User;
 import org.wyona.yarep.core.Node;
 
 /**
  * Group implementation based on Yarep
  */
 public class YarepGroup extends YarepItem implements Group {
     protected static final Logger log = Logger.getLogger(YarepGroup.class);
     
     private Vector members;
     private Vector parents;
 
     public static final String MEMBERS = "members";
 
     public static final String MEMBER = "member";
 
     public static final String MEMBER_ID = "id";
     private static final String MEMBER_TYPE = "type";
     private static final String USER_TYPE = "user";
     private static final String GROUP_TYPE = "group";
     
     public static final String GROUP = "group";
 
     /**
      * Instantiates an existing YarepGroup from a repository node.
      * 
      * @param identityManager
      * @param node
      * @throws AccessManagementException
      */
     public YarepGroup(IdentityManager identityManager, Node node) throws AccessManagementException {
         super(identityManager, node); // this will call configure()
     }
 
     /**
      * Creates a new YarepGroup with the given id as a child of the given parent
      * node. The user is not saved.
      * 
      * @param identityManager
      * @param parentNode
      * @param id
      * @param name
      * @throws AccessManagementException
      */
     public YarepGroup(IdentityManager identityManager, Node parentNode, String id, String name)
             throws AccessManagementException {
         this(identityManager, parentNode, id, name, id + ".xml");
     }
     
     public YarepGroup(IdentityManager identityManager, Node parentNode, String id, String name, String nodeName) throws AccessManagementException {
         super(identityManager, parentNode, id, name, nodeName);
         this.members = new Vector();
         this.parents = new Vector();
 
     }
 
     /**
      * @see org.wyona.security.impl.yarep.YarepItem#configure(org.apache.avalon.framework.configuration.Configuration)
      */
     protected void configure(Configuration config) throws ConfigurationException, AccessManagementException {
         setID(config.getAttribute(ID));
         setName(config.getChild(NAME, false).getValue());
 
         this.members = new Vector();
         this.parents = new Vector();
         Configuration[] memberNodes = config.getChild(MEMBERS).getChildren(MEMBER);
 
         for (int i = 0; i < memberNodes.length; i++) {
             String id = memberNodes[i].getAttribute(MEMBER_ID);
             // type attribute is optional and helps to differentiate between users and groups
             String type = memberNodes[i].getAttribute(MEMBER_TYPE, USER_TYPE);
             if (type.equals(USER_TYPE)) {
                 User user = getIdentityManager().getUserManager().getUser(id);
                 addMember(user);
             } else if (type.equals(GROUP_TYPE)) {
                 log.warn("Beware of loops when adding groups within groups!");
                 Group group = getIdentityManager().getGroupManager().getGroup(id);
                 addMember(group);
             } else {
                 log.error("No such member/item type: " + type);
             }
         }
     }
 
     /**
      * @see org.wyona.security.impl.yarep.YarepItem#createConfiguration()
      */
     protected Configuration createConfiguration() throws AccessManagementException {
         DefaultConfiguration config = new DefaultConfiguration(GROUP);
         config.setAttribute(ID, getID());
         DefaultConfiguration nameNode = new DefaultConfiguration(NAME);
         nameNode.setValue(getName());
         config.addChild(nameNode);
 
         DefaultConfiguration membersNode = new DefaultConfiguration(MEMBERS);
         config.addChild(membersNode);
 
         Item[] items = getMembers();
 
         for (int i = 0; i < items.length; i++) {
             DefaultConfiguration memberNode = new DefaultConfiguration(MEMBER);
             memberNode.setAttribute(MEMBER_ID, items[i].getID());
             membersNode.addChild(memberNode);
         }
 
         return config;
     }
 
     /**
      * @see org.wyona.security.core.api.Group#addMember(org.wyona.security.core.api.Item)
      */
     public void addMember(Item item) throws AccessManagementException {
         if (null != item){
             this.members.add(item);
         } else {
             log.warn("Item is null. Can't add item/user to the group '" + getID() + "'");
         }
     }
 
     /**
      * @see org.wyona.security.core.api.Group#getParents()
      */
     public Group[] getParents() throws AccessManagementException {
         log.error("TODO: Set parent not implemented yet!");
         Group[] g = new Group[parents.size()];
         for (int i = 0; i < g.length; i++) {
             g[i] = (Group) parents.elementAt(i);
         }
         return g;
     }
 
     /**
      * @see org.wyona.security.core.api.Group#getMembers()
      */
     public Item[] getMembers() throws AccessManagementException {
         Item[] m = new Item[members.size()];
         for (int i = 0; i < m.length; i++) {
             m[i] = (Item) members.elementAt(i);
         }
         return m;
     }
 
     /**
      * @see org.wyona.security.core.api.Group#isMember(org.wyona.security.core.api.Item)
      */
     public boolean isMember(Item item) throws AccessManagementException {
         return item != null && this.members.contains(item);
     }
 
     /**
      * @see org.wyona.security.core.api.Group#removeMember(org.wyona.security.core.api.Item)
      */
     public void removeMember(Item item) throws AccessManagementException {
         if (null != item) {
            this.members.remove(item.getID());
         } else {
             log.warn("Item is null. Can't remove item/user from the group '" + getID() + "'");
         }
     }
 
 }
