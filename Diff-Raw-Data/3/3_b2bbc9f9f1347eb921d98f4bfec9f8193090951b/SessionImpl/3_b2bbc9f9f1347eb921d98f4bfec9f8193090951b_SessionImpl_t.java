 package ch.x42.terye;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.AccessControlException;
 
 import javax.jcr.AccessDeniedException;
 import javax.jcr.Credentials;
 import javax.jcr.InvalidItemStateException;
 import javax.jcr.InvalidSerializedDataException;
 import javax.jcr.Item;
 import javax.jcr.ItemExistsException;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.LoginException;
 import javax.jcr.NamespaceException;
 import javax.jcr.Node;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.ReferentialIntegrityException;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.UnsupportedRepositoryOperationException;
 import javax.jcr.ValueFactory;
 import javax.jcr.Workspace;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.retention.RetentionManager;
 import javax.jcr.security.AccessControlManager;
 import javax.jcr.version.VersionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import ch.x42.terye.value.ValueFactoryImpl;
 
 public class SessionImpl implements Session {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private RepositoryImpl repository;
     private WorkspaceImpl workspace;
     private ItemManager itemManager;
     private ValueFactoryImpl valueFactory;
     private boolean live = true;
 
     public SessionImpl(RepositoryImpl repository, String workspaceName)
             throws RepositoryException {
         this.repository = repository;
         workspace = new WorkspaceImpl(workspaceName, this);
         itemManager = new ItemManager(this);
         valueFactory = new ValueFactoryImpl();
         log.debug("Session created for workspace {}", workspaceName);
     }
 
     protected ItemManager getItemManager() {
         return itemManager;
     }
 
     @Override
     public Repository getRepository() {
         return repository;
     }
 
     @Override
     public String getUserID() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String[] getAttributeNames() {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Object getAttribute(String name) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Workspace getWorkspace() {
         return workspace;
     }
 
     @Override
     public Node getRootNode() throws RepositoryException {
         return getItemManager().getNode(new Path("/"));
     }
 
     @Override
     public Session impersonate(Credentials credentials) throws LoginException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node getNodeByUUID(String uuid) throws ItemNotFoundException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Node getNodeByIdentifier(String id) throws ItemNotFoundException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Item getItem(String absPath) throws PathNotFoundException,
             RepositoryException {
         Path path = new Path(absPath).getCanonical();
         return getItemManager().getItem(path);
     }
 
     @Override
     public Node getNode(String absPath) throws PathNotFoundException,
             RepositoryException {
         Path path = new Path(absPath).getCanonical();
         return getItemManager().getNode(path);
     }
 
     @Override
     public Property getProperty(String absPath) throws PathNotFoundException,
             RepositoryException {
         Path path = new Path(absPath).getCanonical();
         return getItemManager().getProperty(path);
     }
 
     @Override
     public boolean itemExists(String absPath) throws RepositoryException {
         Path path = new Path(absPath).getCanonical();
         return getItemManager().itemExists(path);
     }
 
     @Override
     public boolean nodeExists(String absPath) throws RepositoryException {
         return itemExists(absPath) && getItem(absPath).isNode();
     }
 
     @Override
     public boolean propertyExists(String absPath) throws RepositoryException {
         return itemExists(absPath) && !getItem(absPath).isNode();
     }
 
     @Override
     public void move(String srcAbsPath, String destAbsPath)
             throws ItemExistsException, PathNotFoundException,
             VersionException, ConstraintViolationException, LockException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void removeItem(String absPath) throws VersionException,
             LockException, ConstraintViolationException, AccessDeniedException,
             RepositoryException {
         Path path = new Path(absPath).getCanonical();
         getItemManager().removeItem(path);
     }
 
     @Override
     public void save() throws AccessDeniedException, ItemExistsException,
             ReferentialIntegrityException, ConstraintViolationException,
             InvalidItemStateException, VersionException, LockException,
             NoSuchNodeTypeException, RepositoryException {
         getItemManager().persistChanges();
     }
 
     @Override
     public void refresh(boolean keepChanges) throws RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public boolean hasPendingChanges() throws RepositoryException {
         return getItemManager().hasPendingChanges();
     }
 
     @Override
     public ValueFactory getValueFactory()
             throws UnsupportedRepositoryOperationException, RepositoryException {
         return valueFactory;
     }
 
     @Override
     public boolean hasPermission(String absPath, String actions)
             throws RepositoryException {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void checkPermission(String absPath, String actions)
             throws AccessControlException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public boolean hasCapability(String methodName, Object target,
             Object[] arguments) throws RepositoryException {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public ContentHandler getImportContentHandler(String parentAbsPath,
             int uuidBehavior) throws PathNotFoundException,
             ConstraintViolationException, VersionException, LockException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void importXML(String parentAbsPath, InputStream in, int uuidBehavior)
             throws IOException, PathNotFoundException, ItemExistsException,
             ConstraintViolationException, VersionException,
             InvalidSerializedDataException, LockException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void exportSystemView(String absPath, ContentHandler contentHandler,
             boolean skipBinary, boolean noRecurse)
             throws PathNotFoundException, SAXException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void exportSystemView(String absPath, OutputStream out,
             boolean skipBinary, boolean noRecurse) throws IOException,
             PathNotFoundException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void exportDocumentView(String absPath,
             ContentHandler contentHandler, boolean skipBinary, boolean noRecurse)
             throws PathNotFoundException, SAXException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void exportDocumentView(String absPath, OutputStream out,
             boolean skipBinary, boolean noRecurse) throws IOException,
             PathNotFoundException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setNamespacePrefix(String prefix, String uri)
             throws NamespaceException, RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public String[] getNamespacePrefixes() throws RepositoryException {
         return new String[0];
     }
 
     @Override
     public String getNamespaceURI(String prefix) throws NamespaceException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String getNamespacePrefix(String uri) throws NamespaceException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void logout() {
         // reset node manager
         itemManager = null;
         live = false;
     }
 
     @Override
     public boolean isLive() {
         return live;
     }
 
     @Override
     public void addLockToken(String lt) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public String[] getLockTokens() {
        return new String[0];
     }
 
     @Override
     public void removeLockToken(String lt) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public AccessControlManager getAccessControlManager()
             throws UnsupportedRepositoryOperationException, RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public RetentionManager getRetentionManager()
             throws UnsupportedRepositoryOperationException, RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 }
