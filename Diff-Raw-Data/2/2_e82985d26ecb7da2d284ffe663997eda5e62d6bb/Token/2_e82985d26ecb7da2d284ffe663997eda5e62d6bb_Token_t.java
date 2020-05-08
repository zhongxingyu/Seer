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
 
 package com.janrain.backplane2.server;
 
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.message.MessageField;
 import com.janrain.commons.util.Pair;
 import com.janrain.oauth2.OAuth2;
 import com.janrain.oauth2.TokenException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 import static com.janrain.oauth2.OAuth2.OAUTH2_ACCESS_TOKEN_PARAM_NAME;
 
 /**
  * @author Tom Raney
  */
 
 public abstract class Token extends Base {
 
     public static final int TOKEN_LENGTH = 20;
     public static final String ANONYMOUS = "anonymous";
 
     public static enum Source {QUERYPARAM, POSTBODY, AUTHHEADER }
     
     public static enum TYPE {
 
         REGULAR_TOKEN("an", TokenAnonymous.class, EnumSet.allOf(Source.class)),
 
         PRIVILEGED_TOKEN("pr", TokenPrivileged.class, EnumSet.of(Source.AUTHHEADER));
 
         public String getPrefix() {
             return prefix;
         }
 
         public Class<? extends Token> getTokenClass() {
             return tokenClass;
         }
 
         public Collection<Source> getAllowedSources() {
             return allowedSources;
         }
 
         public static TYPE fromTokenString(String token) {
             TYPE type = null;
             try {
                 type = typesByPrefix.get(token.substring(0, PREFIX_LENGTH));
             } catch (Exception e) {
                 // do nothing
             }
             if (type == null) {
                 logger.error("invalid token! => '" + token + "'");
                 throw new IllegalArgumentException();
             }
             return type;
         }
 
         // - PRIVATE
 
         private final static int PREFIX_LENGTH = 2;
         private final String prefix;
         private final Class<? extends Token> tokenClass;
         private final EnumSet<Source> allowedSources;
         
         private static final Map<String,TYPE> typesByPrefix = new HashMap<String, TYPE>();
         static {
             for (TYPE t : values()) {
                 typesByPrefix.put(t.getPrefix(), t);
             }
         }
 
         private TYPE(String prefix, Class<? extends Token> tokenClass, EnumSet<Source> allowedSources) {
             this.prefix = prefix;
             this.tokenClass = tokenClass;
             this.allowedSources = allowedSources;
         }
     }
 
     /**
      * Empty default constructor for AWS to use.
      * Don't call directly.
      */
     public Token() {}
 
     /**
      * Token constructor
      * @param tokenString required
      * @param accessType  REGULAR_TOKEN or PRIVILEGED_TOKEN
      * @param buses       optional
      * @param scopeString optional
      * @param expires     if null, token does not expire
      * @throws BackplaneServerException
      */
     Token(String tokenString, TYPE accessType, String buses, String scopeString, Date expires) throws TokenException {
         super(tokenString,buses,expires);
 
         logger.debug("creating token with id '" + tokenString + "'");
         assert( tokenString.startsWith(accessType.getPrefix()) );
 
         put(TokenField.TYPE.getFieldName(), accessType.name());
 
         setScopeString(scopeString);
 
         validate();
     }
 
     public static @NotNull Token fromRequest(DaoFactory daoFactory, HttpServletRequest request, String accessTokenParam, String authorizationHeader)  throws TokenException {
         
         Pair<String, EnumSet<Source>> tokenAndSource = extractAccessToken(request.getQueryString(), accessTokenParam, authorizationHeader);
 
         if ( StringUtils.isBlank(tokenAndSource.getLeft()) ) {
             throw new TokenException("invalid token", HttpServletResponse.SC_FORBIDDEN);
         }
 
         Token token;
         try {
             token = daoFactory.getTokenDao().retrieveToken(tokenAndSource.getLeft());
         } catch (SimpleDBException e) {
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error loading token", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
 
         if (token == null || token.isExpired()) {
             throw new TokenException("invalid token", HttpServletResponse.SC_FORBIDDEN);
         }
 
         token.checkAllowedSources(tokenAndSource.getRight());
 
         return token;
     }
 
     /** Support only one OAuth token type */
     public static String getTokenType() {
         return "Bearer";
     }
 
     public TYPE getAccessType() {
         return TYPE.valueOf(this.get(TokenField.TYPE));
     }
 
     public Scope getScope()  {
         try {
             return new Scope(this.get(TokenField.SCOPE));
         } catch (TokenException e) {
             throw new IllegalStateException("Invalid scope on get(), should have been validated on token creation: " + this.get(TokenField.SCOPE));
         }
     }
 
     public boolean isPrivileged() {
         return getAccessType() == TYPE.PRIVILEGED_TOKEN;
     }
 
     public String getScopeString() {
         return get(TokenField.SCOPE);
     }
 
     public void setScopeString(String scopeString) {
         if (StringUtils.isBlank(scopeString)) {
             return;
         }
         put(TokenField.SCOPE.getFieldName(), scopeString.trim());
         logger.debug("new scope string: '" + scopeString + "'");
     }
 
     public void setMustReturnScopeInResponse(boolean flag) {
         this.mustReturnScopeInResponse = flag;
     }
 
     public boolean mustReturnScopeInResponse() {
         return this.mustReturnScopeInResponse;
     }
 
     public abstract String getChannelName();
 
 
     public static enum TokenField implements MessageField {
 
         TYPE("type", true),
 
         SCOPE("scope", false) {
             @Override
             public void validate(String value) throws RuntimeException {
                 super.validate(value);
                 try {
                     new Scope(value);
                 } catch (TokenException e) {
                     throw new IllegalArgumentException("Invalid scope: " + value);
                 }
             }
         };
 
         @Override
         public String getFieldName() {
             return fieldName;
         }
 
         @Override
         public boolean isRequired() {
             return required;
         }
 
         @Override
         public void validate(String value) throws RuntimeException {
             if (isRequired()) validateNotNull(getFieldName(), value);
         }
 
         // - PRIVATE
 
         private String fieldName;
         private boolean required = true;
 
         private TokenField(String fieldName, boolean required) {
             this.fieldName = fieldName;
             this.required = required;
         }
     }
 
     private static final Logger logger = Logger.getLogger(Token.class);
     private boolean mustReturnScopeInResponse = false;
 
     private static Pair<String,EnumSet<Source>> extractAccessToken(String queryString, String requestParam, String authHeader) {
         String token = null;
         EnumSet<Token.Source> foundIn = EnumSet.noneOf(Token.Source.class);
 
         if (StringUtils.isNotEmpty(queryString)) {
             Map<String,String> queryParamsMap = new HashMap<String, String>();
             for(String queryParamPair : Arrays.asList(queryString.split("&"))) {
                 String[] nameVal = queryParamPair.split("=", 2);
                queryParamsMap.put(nameVal[0], nameVal.length >1 ? nameVal[1] : null);
             }
             if(queryParamsMap.containsKey(OAUTH2_ACCESS_TOKEN_PARAM_NAME)) {
                 token = queryParamsMap.get(OAUTH2_ACCESS_TOKEN_PARAM_NAME);
                 foundIn.add(Token.Source.QUERYPARAM);
             }
         }
 
         if ( ! foundIn.contains(Token.Source.QUERYPARAM) && requestParam != null ) {
             // query parameter will mask body requestParam extracted by spring with @RequestParameter
             token = requestParam;
             foundIn.add(Token.Source.POSTBODY);
         }
 
         int tokenTypeLength = Token.getTokenType().length();
         if (authHeader != null && authHeader.startsWith(Token.getTokenType()) && authHeader.length() > tokenTypeLength) {
             token = authHeader.substring(tokenTypeLength + 1);
             foundIn.add(Token.Source.AUTHHEADER);
         }
 
         return new Pair<String, EnumSet<Token.Source>>(token, foundIn);
     }
 
     private void checkAllowedSources(Collection<Source> tokenFoundIn) throws TokenException {
         if (tokenFoundIn == null || tokenFoundIn.size() > 1) {
             throw new TokenException("exactly one token source allowed, found in: " + tokenFoundIn, HttpServletResponse.SC_FORBIDDEN);
         }
 
         for(Source tokenSource : tokenFoundIn) {
             if (! getAccessType().getAllowedSources().contains(tokenSource)) {
                 throw new TokenException("token source not allowed: " + tokenSource, HttpServletResponse.SC_FORBIDDEN);
             }
         }
     }
 }
