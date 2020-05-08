 package com.janrain.backplane.server.dao;
 
 import com.janrain.backplane.server.Access;
 import com.janrain.backplane.server.Grant;
 import com.janrain.backplane.server.GrantTokenRel;
 import com.janrain.backplane.server.Token;
 import com.janrain.backplane.server.config.BackplaneConfig;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.SuperSimpleDB;
 import org.apache.log4j.Logger;
 import org.apache.log4j.helpers.ISO8601DateFormat;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static com.janrain.backplane.server.BackplaneMessage.Field.ID;
 import static com.janrain.backplane.server.BackplaneMessage.Field.STICKY;
 
 
 /**
  * @author Tom Raney
  */
 
 public class TokenDAO extends DAO {
 
     TokenDAO(SuperSimpleDB superSimpleDB, BackplaneConfig bpConfig) {
         super(superSimpleDB, bpConfig);
     };
 
     public void persistToken(Token token) throws SimpleDBException {
         superSimpleDB.store(bpConfig.getAccessTokenTableName(), Token.class, token);
         // if any grants exist for this token be sure to persist
         // the relationships, but delete them first
         deleteRelsByTokenId(token.getIdValue());
         if (!token.getGrants().isEmpty()) {
             for (Grant grant : token.getGrants()) {
                 superSimpleDB.store(bpConfig.getAuthTokenRelTableName(),
                         GrantTokenRel.class, new GrantTokenRel(grant.getIdValue(), token.getIdValue()));
             }
         }
     }
 
     public Token retrieveToken(String tokenId) throws SimpleDBException {
         return superSimpleDB.retrieve(bpConfig.getAccessTokenTableName(), Token.class, tokenId);
     }
 
     public Token retrieveTokenByChannel(String channel) throws SimpleDBException {
         List<Token> tokens = superSimpleDB.retrieveWhere(bpConfig.getAccessTokenTableName(), Token.class, "channel='" + channel + "'", false);
         if (tokens.isEmpty()) {
             return null;
         }
         return tokens.get(0);
     }
 
     public void deleteToken(Token token) throws SimpleDBException {
         deleteTokenById(token.getIdValue());
     }
 
     public void deleteTokenById(String tokenId) throws SimpleDBException {
         superSimpleDB.delete(bpConfig.getAccessTokenTableName(), tokenId);
         deleteRelsByTokenId(tokenId);
     }
 
     public void deleteExpiredTokens() throws SimpleDBException {
        String expiredClause = Access.Field.ID.getFieldName() + " < '" + bpConfig.ISO8601.format(new Date(System.currentTimeMillis())) + "'";
         superSimpleDB.deleteWhere(bpConfig.getAccessTokenTableName(), expiredClause);
     }
 
     public List<Token> retrieveTokensByGrant(String grantId) throws SimpleDBException {
         ArrayList<Token> tokens = new ArrayList<Token>();
         List<GrantTokenRel> rels = superSimpleDB.retrieveWhere(bpConfig.getAuthTokenRelTableName(),
                 GrantTokenRel.class, "auth_id='" + grantId + "'", true);
         for (GrantTokenRel rel : rels) {
             tokens.add(retrieveToken(rel.getTokenId()));
         }
         return tokens;
     }
 
     public void revokeTokenByGrant(String grantId) throws SimpleDBException {
         List<Token> tokens = retrieveTokensByGrant(grantId);
         for (Token token : tokens) {
             deleteToken(token);
             logger.info("revoked token " + token.getIdValue());
         }
         logger.info("all tokens for grant " + grantId + " have been revoked");
     }
 
     private void deleteRelsByTokenId(String tokenId)  {
         List<GrantTokenRel> rels = null;
         try {
             rels = superSimpleDB.retrieveWhere(bpConfig.getAuthTokenRelTableName(), GrantTokenRel.class, "token_id='" + tokenId + "'", true);
             for (GrantTokenRel rel: rels) {
                 superSimpleDB.delete(bpConfig.getAuthTokenRelTableName(), rel.getIdValue());
             }
         } catch (SimpleDBException e) {
             // do nothing
         }
 
     }
 
     private static final Logger logger = Logger.getLogger(TokenDAO.class);
 
 
 }
