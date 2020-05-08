 package models.origo.core;
 
 import main.origo.core.Node;
 import main.origo.core.helpers.UIElementHelper;
 import main.origo.core.ui.UIElement;
 import play.data.validation.Constraints;
 import play.db.jpa.JPA;
 
 import javax.persistence.*;
 import java.util.*;
 
 @Entity
 @Table(name = "root", uniqueConstraints = @UniqueConstraint(name = "nodeVersion", columnNames = {"nodeId", "version"}))
 public final class RootNode implements Node {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long id;
 
     @Constraints.Required
     public String nodeId;
 
     @Constraints.Required
     public Integer version;
 
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date publish;
 
     @Temporal(value = TemporalType.TIMESTAMP)
     public Date unPublish;
 
     @Column(name="type")
     public String nodeType;
 
     public String themeVariant;
 
     /**
      * Only kept to make sure all elements added to the HEAD region are unique (we don't want duplicate javascript or css resources).
      */
     @Transient
     private Map<String, UIElement> headElement = new HashMap<String, UIElement>();
     /**
      * Only kept to make sure all scripts added to the bottom of PAGE are unique (we don't want duplicate javascript here either).
      */
     @Transient
     private Map<String, UIElement> tailElement = new HashMap<String, UIElement>();
     /**
      * A list of UIElements for each region on the page. The key is the region name.
      */
     @Transient
     private Map<String, List<UIElement>> uiElements = new HashMap<String, List<UIElement>>();
 
     private RootNode() {
     }
 
     public RootNode(Integer version) {
         this(UUID.randomUUID().toString(), version);
     }
 
     public RootNode(String nodeId, Integer version) {
         this.nodeId = nodeId;
         this.version = version;
     }
 
     @Override
     public String getTitle() {
         return toString();
     }
 
     @Override
     public String getNodeId() {
         return nodeId;
     }
 
     @Override
     public Integer getVersion() {
         return version;
     }
 
     @Override
     public Date getDatePublished() {
         return publish;
     }
 
     @Override
     public Date getDateUnpublished() {
         return unPublish;
     }
 
     @Override
     public String getThemeVariant() {
         return themeVariant;
     }
 
     @Override
     public Set<String> getRegions() {
         return this.uiElements.keySet();
     }
 
     /* Interface methods */
     @Override
     public List<UIElement> getUIElements(String region) {
         return this.uiElements.get(region.toLowerCase());
     }
 
     @Override
     public UIElement addHeadUIElement(UIElement uiElement) {
         return addHeadUIElement(uiElement, false);
     }
 
     @Override
     public UIElement addTailUIElement(UIElement uiElement) {
         return addTailUIElement(uiElement, false);
     }
 
     @Override
     public UIElement addUIElement(UIElement uiElement) {
         return addUIElement(uiElement, false);
     }
 
     @Override
     public UIElement addHeadUIElement(UIElement uiElement, boolean reorderElementsBelow) {
         String elementKey = String.valueOf(uiElement.hashCode());
         if (headElement.containsKey(elementKey)) {
             return headElement.get(elementKey);
         } else {
             headElement.put(elementKey, uiElement);
             return addUIElement(uiElement, reorderElementsBelow, HEAD, uiElement.getWeight());
         }
     }
 
     @Override
     public UIElement addTailUIElement(UIElement uiElement, boolean reorderElementsBelow) {
         String elementKey = String.valueOf(uiElement.hashCode());
         if (tailElement.containsKey(elementKey)) {
             return tailElement.get(elementKey);
         } else {
             tailElement.put(elementKey, uiElement);
             return addUIElement(uiElement, reorderElementsBelow, TAIL, uiElement.getWeight());
         }
     }
 
     @Override
     public UIElement addUIElement(UIElement uiElement, boolean reorderElementsBelow) {
         Meta meta = Meta.findWithNodeIdAndSpecificVersion(nodeId, version, uiElement.id);
         if (meta == null) {
             meta = Meta.defaultMeta();
         }
 
         String regionKey = meta.region.toLowerCase();
         return addUIElement(uiElement, reorderElementsBelow, regionKey, meta.weight.intValue());
     }
 
     private UIElement addUIElement(UIElement uiElement, boolean reorderElementsBelow, String regionKey, int weight) {
         if (!uiElements.containsKey(regionKey)) {
             uiElements.put(regionKey, new ArrayList<UIElement>());
         }
         uiElement.setWeight(weight);
         uiElements.get(regionKey).add(uiElement);
         if (reorderElementsBelow) {
             UIElementHelper.repositionUIElements(uiElements.get(regionKey), uiElement);
         }
         UIElementHelper.reorderUIElements(uiElements.get(regionKey));
         return uiElement;
     }
 
     @Override
     public boolean removeHeadUIElement(UIElement uiElement) {
         return removeUIElement(uiElement, HEAD);
     }
 
     @Override
     public boolean removeTailUIElement(UIElement uiElement) {
         return removeUIElement(uiElement, TAIL);
     }
 
     @Override
     public boolean removeUIElement(UIElement uiElement) {
         Meta meta = Meta.findWithNodeIdAndSpecificVersion(nodeId, version, uiElement.id);
         if (meta == null) {
             meta = Meta.defaultMeta();
         }
         String regionKey = meta.region.toLowerCase();
         return removeUIElement(uiElement, regionKey);
     }
 
     private boolean removeUIElement(UIElement uiElement, String regionKey) {
         if (uiElements.get(regionKey).remove(uiElement)) {
             UIElementHelper.reorderUIElements(uiElements.get(regionKey));
             return true;
         }
         return false;
     }
 
     @Override
     public String toString() {
         return "Node ("+ nodeType +" - " + nodeId + "," + version + ")";
     }
 
     private static void initializeNode(RootNode node) {
         node.uiElements = new HashMap<String, List<UIElement>>();
         node.uiElements.put(HEAD, new ArrayList<UIElement>());
         node.headElement = new HashMap<String, UIElement>();
     }
 
 
     private static void initializeNodes(Collection<RootNode> nodes) {
         for (RootNode node : nodes) {
             initializeNode(node);
         }
     }
 
     public static List<RootNode> findAllCurrentVersions(Date today) {
         String queryString = "select n from models.origo.core.RootNode n " +
                 "where n.version = (" +
                 "select max(n2.version) from models.origo.core.RootNode n2 " +
                 "where n2.nodeId = n.nodeId and " +
                 "(n2.publish = null or n2.publish < :today) and" +
                 "(n2.unPublish = null or n2.unPublish >= :today)" +
                 ")";
         final Query query = JPA.em().createQuery(queryString);
         query.setParameter("today", today);
         List<RootNode> nodes = query.getResultList();
         initializeNodes(nodes);
         return nodes;
     }
 
     public static RootNode findWithNodeIdAndSpecificVersion(String nodeId, Integer version) {
         String queryString = "select n from models.origo.core.RootNode n " +
                 "where n.nodeId = :nodeId and n.version = :version";
         final Query query = JPA.em().createQuery(queryString);
         query.setParameter("nodeId", nodeId);
         query.setParameter("version", version);
         RootNode node = (RootNode)query.getSingleResult();
         if (node != null) {
             initializeNode(node);
         }
         return node;
     }
 
     public static RootNode findLatestPublishedVersionWithNodeId(String nodeId, Date today) {
         String queryString = "select distinct n from models.origo.core.RootNode n " +
                 "where n.nodeId = :nodeId and " +
                 "(n.publish = null or n.publish < :today) and " +
                 "(n.unPublish = null or n.unPublish >= :today) " +
                 "order by n.version desc";
         final Query query = JPA.em().createQuery(queryString);
         query.setParameter("nodeId", nodeId);
         query.setParameter("today", today);
         List<RootNode> nodes = query.getResultList();
         if (nodes.isEmpty()) {
             return null;
         }
         RootNode node = nodes.get(0);
         initializeNode(node);
         return node;
     }
 
     public static RootNode findLatestVersionWithNodeId(String nodeId) {
         String queryString = "select distinct n from models.origo.core.RootNode n " +
                 "where n.nodeId = :nodeId " +
                 "order by n.version desc";
         final Query query = JPA.em().createQuery(queryString);
         query.setParameter("nodeId", nodeId);
         List<RootNode> nodes = query.getResultList();
         if (nodes.isEmpty()) {
             return null;
         }
         RootNode node = nodes.get(0);
         initializeNode(node);
         return node;
     }
 
     public static List<RootNode> findAllVersionsWithNodeId(String nodeId) {
         String queryString = "select distinct n from models.origo.core.RootNode n " +
                 "where n.nodeId = :nodeId " +
                 "order by n.version desc";
         final Query query = JPA.em().createQuery(queryString);
         query.setParameter("nodeId", nodeId);
         List<RootNode> nodes = query.getResultList();
         initializeNodes(nodes);
         return nodes;
     }
 
     public RootNode copy() {
         return copy(false);
     }
 
     public RootNode copy(boolean increaseVersion) {
        RootNode copy = new RootNode(nodeId, increaseVersion ? version + 1 : version);
         copy.publish = publish;
         copy.unPublish = unPublish;
         copy.nodeType = nodeType;
         copy.themeVariant = themeVariant;
         return copy;
     }
 
     public RootNode save() {
         JPA.em().persist(this);
         return this;
     }
 
 }
