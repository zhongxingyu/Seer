 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server.dao;
 
 import com.janrain.backplane2.server.BackplaneMessage;
 import com.janrain.backplane2.server.GrantType;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.Token;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import com.janrain.oauth2.TokenException;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.TimerMetric;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_ACCESS_TOKEN;
 import static com.janrain.backplane2.server.config.Backplane2Config.SimpleDBTables.BP_REVOKED_TOKEN;
 
 /**
  * @author Tom Raney
  */
 
 public class TokenDAO extends DAO<Token> {
 
     TokenDAO(SuperSimpleDB superSimpleDB, Backplane2Config bpConfig, DaoFactory daoFactory) {
         super(superSimpleDB, bpConfig);
         this.cache = daoFactory.getTokenCache();
     }
 
     @Override
     public void persist(final Token token) throws SimpleDBException {
         try {
             v2persistTokenTimer.time(new Callable<Object>() {
                 @Override
                 public Object call() throws SimpleDBException {
                     superSimpleDB.store(bpConfig.getTableName(BP_ACCESS_TOKEN), Token.class, token, true);
                     return null;
                 }
             });
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (Exception e) {
             throw new SimpleDBException(e);
         }
     }
 
     @Override
     public void delete(final String tokenId) throws SimpleDBException {
         try {
             v2deleteTokenTimer.time(new Callable<Object>() {
                 @Override
                 public Object call() throws SimpleDBException {
                     superSimpleDB.delete(bpConfig.getTableName(BP_ACCESS_TOKEN), tokenId);
                     return null;
                 }
             });
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (Exception e) {
             throw new SimpleDBException(e);
         }
     }
 
     public Token retrieveToken(final String tokenId) throws SimpleDBException {
         if (! Token.looksLikeOurToken(tokenId)) {
             logger.error("invalid token! => '" + tokenId + "'");
             return null; //invalid token id, don't even try
         }
         try {
             Token cached = cache.get(tokenId);
             if (cached != null) return cached;
             return v2retrieveTokenTimer.time(new Callable<Token>() {
                 @Override
                 public Token call() throws SimpleDBException {
                     Token token = superSimpleDB.retrieve(bpConfig.getTableName(BP_ACCESS_TOKEN), Token.class, tokenId);
                    if (! token.getType().isRefresh()) cache.add(token);
                     return token;
                 }
             });
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (Exception e) {
             throw new SimpleDBException(e);
         }
     }
 
     public void deleteExpiredTokens() throws SimpleDBException {
         try {
             logger.info("Backplane token cleanup task started.");
             String expiredClause = Token.TokenField.EXPIRES.getFieldName() + " < '" + Backplane2Config.ISO8601.get().format(new Date(System.currentTimeMillis())) + "'";
             superSimpleDB.deleteWhere(bpConfig.getTableName(BP_ACCESS_TOKEN), expiredClause);
             superSimpleDB.deleteWhere(bpConfig.getTableName(BP_REVOKED_TOKEN), expiredClause);
         } catch (Exception e) {
             // catch-all, else cleanup thread stops
             logger.error("Backplane token cleanup task error: " + e.getMessage(), e);
         } finally {
             logger.info("Backplane token cleanup task finished.");
         }
     }
 
     public void cacheRevokedCleanup() throws SimpleDBException {
         for(Token revoked : superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_REVOKED_TOKEN), Token.class, null, true)) {
             cache.delete(revoked.getIdValue());
         }
     }
 
     public List<Token> retrieveTokensByGrant(String grantId) throws SimpleDBException {
         ArrayList<Token> tokens = new ArrayList<Token>();
         for(Token token : superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_ACCESS_TOKEN), Token.class,
                 Token.TokenField.BACKING_GRANTS.getFieldName() + " LIKE '%" + grantId + "%'", true)) {
             if (token.getBackingGrants().contains(grantId)) {
                 tokens.add(token);
             }
         }
         return tokens;
     }
 
     public void revokeTokenByGrant(String grantId) throws SimpleDBException {
         List<Token> tokens = retrieveTokensByGrant(grantId);
         for (Token token : tokens) {
             delete(token.getIdValue());
             superSimpleDB.store(bpConfig.getTableName(BP_REVOKED_TOKEN), Token.class, token, true);
             logger.info("revoked token " + token.getIdValue());
         }
         if (! tokens.isEmpty()) {
             logger.info("all tokens for grant " + grantId + " have been revoked");
         }
     }
 
     /**
      * @return the bus to which this channel is bound, never null
      *
      * @throws SimpleDBException
      * @throws TokenException if the channel is invalid and was bound to more than one bus
      */
     public String getBusForChannel(String channel) throws SimpleDBException, TokenException {
         if (StringUtils.isEmpty(channel)) return null;
         final Scope singleChannelScope = new Scope(BackplaneMessage.Field.CHANNEL, channel);
         try {
             List<Token> tokens = v2channelBusLookup.time(new Callable<List<Token>>() {
                 @Override
                 public List<Token> call() throws Exception {
                     return superSimpleDB.retrieveWhere(bpConfig.getTableName(BP_ACCESS_TOKEN), Token.class,
                                 Token.TokenField.TYPE.getFieldName() + "='" + GrantType.ANONYMOUS + "' AND " +
                                 Token.TokenField.SCOPE.getFieldName() + " LIKE '%" + singleChannelScope.toString() + "%'", true);
                 }
             });
             if (tokens == null || tokens.isEmpty()) {
                 logger.error("No anonymous tokens found to bind channel " + channel + " to a bus");
                 throw new TokenException("invalid channel: " + channel);
             }
 
             String bus = null;
             for (Token token : tokens) {
                 Scope tokenScope = token.getScope();
                 if ( tokenScope.containsScope(singleChannelScope) ) {
                     LinkedHashSet<String> buses = tokenScope.getScopeMap().get(BackplaneMessage.Field.BUS);
                     if (buses == null || buses.isEmpty()) continue;
                     if ( (bus != null && buses.size() == 1) || (buses.size() > 1) )  {
                         throw new TokenException("invalid channel, bound to more than one bus");
                     }
                     bus = buses.iterator().next();
                 }
             }
             if (bus == null) {
                 throw new TokenException("invalid channel: " + channel);
             } else {
                 return bus;
             }
         } catch (SimpleDBException sdbe) {
             throw sdbe;
         } catch (TokenException te) {
             throw te;
         } catch (Exception e) {
             throw new SimpleDBException(e);
         }
     }
 
     public boolean isValidBinding(String channel, String bus) throws SimpleDBException {
         try {
             return bus != null && channel != null && bus.equals(getBusForChannel(channel));
         } catch (TokenException e) {
             logger.error(e.getMessage(), e);
             return false;
         }
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(TokenDAO.class);
 
     private final ConfigLRUCache<Token> cache;
 
     private final TimerMetric v2persistTokenTimer = Metrics.newTimer(TokenDAO.class, "v2_sdb_persist_token", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final TimerMetric v2retrieveTokenTimer = Metrics.newTimer(TokenDAO.class, "v2_sdb_retrieve_token", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final TimerMetric v2deleteTokenTimer = Metrics.newTimer(TokenDAO.class, "v2_sdb_delete_token", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
     private final TimerMetric v2channelBusLookup = Metrics.newTimer(TokenDAO.class, "v2_sdb_bus_channel_lookup", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
 }
