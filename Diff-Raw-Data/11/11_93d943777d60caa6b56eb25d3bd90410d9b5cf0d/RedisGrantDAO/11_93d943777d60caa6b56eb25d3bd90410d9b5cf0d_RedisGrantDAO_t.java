 package com.janrain.backplane2.server.dao.redis;
 
 import com.janrain.backplane2.server.BackplaneMessage;
 import com.janrain.backplane2.server.BackplaneServerException;
 import com.janrain.backplane2.server.Grant;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.dao.GrantDAO;
 import com.janrain.backplane2.server.dao.TokenDAO;
 import com.janrain.oauth2.TokenException;
 import com.janrain.redis.Redis;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Transaction;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Tom Raney
  */
 public class RedisGrantDAO implements GrantDAO {
 
     public RedisGrantDAO() {
         this.tokenDAO = new RedisTokenDAO();
     }
 
     @Override
     public List<Grant> getByClientId(String clientId) throws BackplaneServerException {
         List<Grant> grants = getAll();
         List<Grant> filtered = new ArrayList<Grant>();
         for (Grant grant: grants) {
             if (clientId.equals(grant.get(Grant.GrantField.ISSUED_TO_CLIENT_ID)) &&
                     (grant.getState().isActive())) {
                 filtered.add(grant);
             }
         }
         return filtered;
     }
 
     @Override
     public void deleteByBuses(@NotNull List<String> busesToDelete) throws BackplaneServerException, TokenException {
 
         Scope deleteBusesScope = new Scope(Scope.getEncodedScopesAsString(BackplaneMessage.Field.BUS, busesToDelete));
             for(Grant grant : getAll()) {
                 Set<String> grantBuses = grant.getAuthorizedScope().getScopeFieldValues(BackplaneMessage.Field.BUS);
                 if (grantBuses == null) continue;
                 for(String bus : grantBuses) {
                     if (busesToDelete.contains(bus)) {
                         revokeBuses(grant, deleteBusesScope);
                     }
                 }
             }
     }
 
     @Override
     public boolean revokeBuses(List<Grant> grants, String buses) throws BackplaneServerException, TokenException {
         Scope busesToRevoke = new Scope(Scope.getEncodedScopesAsString(BackplaneMessage.Field.BUS, buses));
         boolean changes = false;
         for (Grant grant : grants) {
             changes = revokeBuses(grant, busesToRevoke) || changes;
         }
         return changes;
     }
 
     @Override
     public Grant get(String id) throws BackplaneServerException {
         byte[] bytes = Redis.getInstance().get(getKey(id));
         if (bytes != null) {
             return (Grant) SerializationUtils.deserialize(bytes);
         } else {
             return null;
         }
     }
 
     @Override
     public List<Grant> getAll() throws BackplaneServerException {
         List<byte[]> listOfBytes = Redis.getInstance().lrange(getKey("list"), 0, -1);
         List<Grant> grants = new ArrayList<Grant>();
         for (byte[] bytes : listOfBytes) {
             if (bytes != null) {
                 grants.add((Grant) SerializationUtils.deserialize(bytes));
             }
         }
         return grants;
     }
 
     @Override
     public void persist(Grant obj) throws BackplaneServerException {
         byte[] bytes = SerializationUtils.serialize(obj);
         logger.info("adding grant " + obj.getIdValue() + " to redis");
         // todo: one hash object instead of top level entries + a separate list?
         Redis.getInstance().rpush(getKey("list"), bytes);
         Redis.getInstance().set(getKey(obj.getIdValue()), bytes);
     }
 
     @Override
     public void update(Grant existing, Grant updated) throws BackplaneServerException, TokenException {
         Jedis jedis = null;
         try {
             tokenDAO.revokeTokenByGrant(existing.getIdValue());
             jedis = Redis.getInstance().getJedis();
             byte[] newBytes = SerializationUtils.serialize(updated);
             byte[] oldBytes = jedis.get(getKey(existing.getIdValue()));
             Transaction t = jedis.multi();
             t.lrem(getKey("list"), 0, oldBytes);
             t.rpush(getKey("list"), newBytes);
             t.set(getKey(updated.getIdValue()), newBytes);
             t.exec();
             logger.info("Updated grant (and revoked tokens): " + updated.getIdValue());
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public void delete(String id) throws BackplaneServerException, TokenException {
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
             byte[] bytes = jedis.get(getKey(id));
             if (bytes != null) {
                 if (jedis.lrem(getKey("list"), 0, bytes) == 0) {
                     logger.warn("failed to remove grant " + id + " from list " + new String(getKey("list")));
                 }
                 jedis.del(getKey(id));
             }
            tokenDAO.revokeTokenByGrant(id);
             logger.info("deleted grant " + id);
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     // PRIVATE
 
     private static final Logger logger = Logger.getLogger(RedisGrantDAO.class);
 
     private TokenDAO tokenDAO;
 
     private static byte[] getKey(String id) {
         return ("v2_grant_" + id).getBytes();
     }
 
     private boolean revokeBuses(Grant grant, Scope busesToRevoke) throws BackplaneServerException {
 
         try {
             Scope grantScope = grant.getAuthorizedScope();
             Scope updatedScope = Scope.revoke(grantScope, busesToRevoke);
             if (updatedScope.equals(grantScope)) return false;
             if (!updatedScope.isAuthorizationRequired()) {
                 logger.info("Revoked all buses from grant: " + grant.getIdValue());
                 delete(grant.getIdValue());
             } else {
                 Grant updated = new Grant.Builder(grant, grant.getState()).scope(updatedScope).buildGrant();
                 update(grant, updated);
                 logger.info("Buses updated updated for grant: " + updated.getIdValue() + " remaining scope: '" + updated.getAuthorizedScope() + "'");
             }
             return true;
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage());
         }
 
     }
 }
