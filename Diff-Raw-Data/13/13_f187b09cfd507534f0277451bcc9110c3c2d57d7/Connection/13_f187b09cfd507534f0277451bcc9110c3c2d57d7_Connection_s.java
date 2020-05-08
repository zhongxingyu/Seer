 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.connection;
 
 import Sirius.navigator.exception.ConnectionException;
 
 import Sirius.server.localserver.method.MethodMap;
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.Link;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.newuser.User;
 import Sirius.server.newuser.UserException;
 import Sirius.server.newuser.UserGroup;
 import Sirius.server.search.Query;
 import Sirius.server.search.SearchOption;
 import Sirius.server.search.SearchResult;
 import Sirius.server.search.store.Info;
 import Sirius.server.search.store.QueryData;
 
 import Sirius.util.image.ImageHashMap;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.Icon;
 
 import de.cismet.security.Proxy;
 
 /**
  * A singleton factory class that creates and manages connections.
  *
  * @author   Pascal
  * @version  1.0 12/22/2002
  */
 public interface Connection {
 
     //~ Methods ----------------------------------------------------------------
 
     // Connection --------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   callserverURL  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean connect(String callserverURL) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   callserverURL  DOCUMENT ME!
      * @param   proxy          DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean connect(String callserverURL, Proxy proxy) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean reconnect() throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      */
     void disconnect();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     boolean isConnected();
     /**
      * Default -----------------------------------------------------------------
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     String[] getDomains() throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     ImageHashMap getDefaultIcons() throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   name  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Icon getDefaultIcon(String name) throws ConnectionException;
     /**
      * User --------------------------------------------------------------------
      *
      * @param   userGroupLsName  DOCUMENT ME!
      * @param   userGroupName    DOCUMENT ME!
      * @param   userLsName       DOCUMENT ME!
      * @param   userName         DOCUMENT ME!
      * @param   password         DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      * @throws  UserException        DOCUMENT ME!
      */
     User getUser(String userGroupLsName, String userGroupName, String userLsName, String userName, String password)
             throws ConnectionException, UserException;
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Vector getUserGroupNames() throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   username  DOCUMENT ME!
      * @param   domain    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      * @throws  UserException        DOCUMENT ME!
      */
     Vector getUserGroupNames(String username, String domain) throws ConnectionException, UserException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user         DOCUMENT ME!
      * @param   oldPassword  DOCUMENT ME!
      * @param   newPassword  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      * @throws  UserException        DOCUMENT ME!
      */
     boolean changePassword(User user, String oldPassword, String newPassword) throws ConnectionException, UserException;
     /**
      * Node --------------------------------------------------------------------
      *
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Node[] getRoots(User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user    DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Node[] getRoots(User user, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   node  DOCUMENT ME!
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Node[] getChildren(Node node, User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user    DOCUMENT ME!
      * @param   nodeID  DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Node getNode(User user, int nodeID, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   node    DOCUMENT ME!
      * @param   parent  DOCUMENT ME!
      * @param   user    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Node addNode(Node node, Link parent, User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   node  DOCUMENT ME!
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean deleteNode(Node node, User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   from  DOCUMENT ME!
      * @param   to    DOCUMENT ME!
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean addLink(Node from, Node to, User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   from  DOCUMENT ME!
      * @param   to    DOCUMENT ME!
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean deleteLink(Node from, Node to, User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Node[] getClassTreeNodes(User user) throws ConnectionException;
     /**
      * Classes & Objects -------------------------------------------------------
      *
      * @param   user     DOCUMENT ME!
      * @param   classID  DOCUMENT ME!
      * @param   domain   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaClass getMetaClass(User user, int classID, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user    DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaClass[] getClasses(User user, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   usr    DOCUMENT ME!
      * @param   query  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject[] getMetaObject(User usr, Query query) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user      DOCUMENT ME!
      * @param   objectID  DOCUMENT ME!
      * @param   classID   DOCUMENT ME!
      * @param   domain    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject getMetaObject(User user, int objectID, int classID, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user   DOCUMENT ME!
      * @param   query  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject[] getMetaObjectByQuery(User user, String query) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user        DOCUMENT ME!
      * @param   MetaObject  DOCUMENT ME!
      * @param   domain      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject insertMetaObject(User user, MetaObject MetaObject, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user    DOCUMENT ME!
      * @param   query   DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     int insertMetaObject(User user, Query query, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user        DOCUMENT ME!
      * @param   MetaObject  DOCUMENT ME!
      * @param   domain      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     int updateMetaObject(User user, MetaObject MetaObject, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user        DOCUMENT ME!
      * @param   MetaObject  DOCUMENT ME!
      * @param   domain      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     int deleteMetaObject(User user, MetaObject MetaObject, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user  DOCUMENT ME!
      * @param   c     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject getInstance(User user, MetaClass c) throws ConnectionException;
     /**
      * Dynmaic Search ----------------------------------------------------------
      *
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     HashMap getSearchOptions(User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user    DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     HashMap getSearchOptions(User user, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user           DOCUMENT ME!
      * @param   classIds       DOCUMENT ME!
      * @param   searchOptions  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     SearchResult search(User user, String[] classIds, SearchOption[] searchOptions) throws ConnectionException;
 
     /**
      * add single query root and leaf returns a query_id.
      *
      * @param   user         DOCUMENT ME!
      * @param   name         DOCUMENT ME!
      * @param   description  DOCUMENT ME!
      * @param   statement    DOCUMENT ME!
      * @param   resultType   DOCUMENT ME!
      * @param   isUpdate     DOCUMENT ME!
      * @param   isRoot       DOCUMENT ME!
      * @param   isUnion      DOCUMENT ME!
      * @param   isBatch      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     int addQuery(User user,
             String name,
             String description,
             String statement,
             int resultType,
             char isUpdate,
             char isRoot,
             char isUnion,
             char isBatch) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user         DOCUMENT ME!
      * @param   name         DOCUMENT ME!
      * @param   description  DOCUMENT ME!
      * @param   statement    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     int addQuery(User user, String name, String description, String statement) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user           DOCUMENT ME!
      * @param   queryId        DOCUMENT ME!
      * @param   typeId         DOCUMENT ME!
      * @param   paramkey       DOCUMENT ME!
      * @param   description    DOCUMENT ME!
      * @param   isQueryResult  DOCUMENT ME!
      * @param   queryPosition  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean addQueryParameter(User user,
             int queryId,
             int typeId,
             String paramkey,
             String description,
             char isQueryResult,
             int queryPosition) throws ConnectionException;
 
     /**
      * position set in order of the addition.
      *
      * @param   user         DOCUMENT ME!
      * @param   queryId      DOCUMENT ME!
      * @param   paramkey     DOCUMENT ME!
      * @param   description  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean addQueryParameter(User user, int queryId, String paramkey, String description) throws ConnectionException;
     /**
      * QueryData ---------------------------------------------------------------
      *
      * @param   queryDataId  DOCUMENT ME!
      * @param   domain       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean deleteQueryData(int queryDataId, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user  DOCUMENT ME!
      * @param   data  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     boolean storeQueryData(User user, QueryData data) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   id      DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     QueryData getQueryData(int id, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   userGroup  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Info[] getUserGroupQueryInfos(UserGroup userGroup) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     Info[] getUserQueryInfos(User user) throws ConnectionException;
     /**
      * Methods -----------------------------------------------------------------
      *
      * @param   user  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MethodMap getMethods(User user) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   user    DOCUMENT ME!
      * @param   domain  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MethodMap getMethods(User user, String domain) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   classId                DOCUMENT ME!
      * @param   user                   DOCUMENT ME!
      * @param   representationFields   DOCUMENT ME!
      * @param   representationPattern  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject[] getAllLightweightMetaObjectsForClass(int classId,
             User user,
             String[] representationFields,
             String representationPattern) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   classId               DOCUMENT ME!
      * @param   user                  DOCUMENT ME!
      * @param   representationFields  DOCUMENT ME!
      * @param   formater              DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject[] getAllLightweightMetaObjectsForClass(int classId,
             User user,
             String[] representationFields,
             AbstractAttributeRepresentationFormater formater) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   classId                DOCUMENT ME!
      * @param   user                   DOCUMENT ME!
      * @param   query                  DOCUMENT ME!
      * @param   representationFields   DOCUMENT ME!
      * @param   representationPattern  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject[] getLightweightMetaObjectsByQuery(int classId,
             User user,
             String query,
             String[] representationFields,
             String representationPattern) throws ConnectionException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   classId               DOCUMENT ME!
      * @param   user                  DOCUMENT ME!
      * @param   query                 DOCUMENT ME!
      * @param   representationFields  DOCUMENT ME!
      * @param   formater              DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ConnectionException  DOCUMENT ME!
      */
     MetaObject[] getLightweightMetaObjectsByQuery(int classId,
             User user,
             String query,
             String[] representationFields,
             AbstractAttributeRepresentationFormater formater) throws ConnectionException;
 
     /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   key   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  ConnectionException  DOCUMENT ME!
      */
     String getConfigAttr(final User user, final String key) throws ConnectionException;
 
     boolean hasConfigAttr(final User user, final String key) throws ConnectionException;
 }
