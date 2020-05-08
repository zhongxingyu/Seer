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
 
 package com.janrain.backplane2.server.provision;
 
 import com.janrain.backplane2.server.*;
 import com.janrain.backplane2.server.config.*;
 import com.janrain.backplane2.server.dao.DAOFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.message.AbstractMessage;
 import com.janrain.commons.supersimpledb.message.MessageField;
 import com.janrain.crypto.HmacHashUtils;
 import com.janrain.oauth2.TokenException;
 import com.janrain.servlet.ServletUtil;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 
 /**
  * Controller handling the API calls for backplane customer configuration provisioning.
  *
  * @author Johnny Bufu
  */
 @Controller
 @RequestMapping(value="/v2/provision/*")
 @SuppressWarnings({"UnusedDeclaration"})
 public class ProvisioningController2 {
 
     // - PUBLIC
 
     @RequestMapping(value = "/bus/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> busList(HttpServletRequest request, @RequestBody ListRequest listRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
         return doList(BusConfig2.class, listRequest.getEntities(), BusConfig2.Field.BUS_NAME);
     }
 
     @RequestMapping(value = "/user/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> userList(HttpServletRequest request, @RequestBody ListRequest listRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
         return doList(User.class, listRequest.getEntities(), User.Field.USER);
     }
 
     @RequestMapping(value = "/client/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> clientList(HttpServletRequest request, @RequestBody ListRequest listRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
         return doList(Client.class, listRequest.getEntities(), Client.Field.USER);
     }
 
     @RequestMapping(value = "/bus/delete", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> busDelete(HttpServletRequest request, @RequestBody ListRequest deleteRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(deleteRequest.getAdmin(), deleteRequest.getSecret());
         return doDelete(BusConfig2.class, deleteRequest.getEntities());
     }
 
     @RequestMapping(value = "/user/delete", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> userDelete(HttpServletRequest request, @RequestBody ListRequest deleteRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(deleteRequest.getAdmin(), deleteRequest.getSecret());
         return doDelete(User.class, deleteRequest.getEntities());
     }
 
     @RequestMapping(value = "/client/delete", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> clientDelete(HttpServletRequest request, @RequestBody ListRequest deleteRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(deleteRequest.getAdmin(), deleteRequest.getSecret());
         return doDelete(Client.class, deleteRequest.getEntities());
     }
 
     @RequestMapping(value = "/bus/update", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> busUpdate(HttpServletRequest request, @RequestBody BusUpdateRequest updateRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         return doUpdate(BusConfig2.class, updateRequest);
     }
 
     @RequestMapping(value = "/user/update", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> userUpdate(HttpServletRequest request, @RequestBody UserUpdateRequest updateRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         return doUpdate(User.class, updateRequest);
     }
 
     @RequestMapping(value = "/client/update", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> clientUpdate(HttpServletRequest request, @RequestBody ClientUpdateRequest updateRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         logger.debug("client updateRequest: '" + updateRequest + "'");
         return doUpdate(Client.class, updateRequest);
     }
 
     @RequestMapping(value = "/grant/list", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, Map<String, String>> grantList(HttpServletRequest request, @RequestBody ListRequest listRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         bpConfig.checkAdminAuth(listRequest.getAdmin(), listRequest.getSecret());
 
         Map<String,Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
 
         for(String clientId : listRequest.getEntities()) {
             try {
                 Map<String,String> grantsList = new HashMap<String, String>();
                 Map<Scope, Set<Grant>> scopeGrants = new GrantLogic(daoFactory).retrieveClientGrants(clientId, null);
                 for (Set<Grant> grantSets : scopeGrants.values()) {
                     for(Grant grant : grantSets) {
                         grantsList.put(grant.getIdValue(), grant.getAuthorizedScope().toString());
                     }
                     result.put(clientId, grantsList);
                 }
             } catch (final BackplaneServerException e) {
                 logger.error("Error looking up grants for client " + clientId, e);
                 result.put(clientId, new HashMap<String, String>() {{put("error", e.getMessage());}});
             } catch (final TokenException te) {
                 logger.error("token (unexpected scope processing?) error: " + te.getMessage(), te);
                 result.put(clientId, new HashMap<String, String>() {{
                     put("error", te.getMessage());
                 }});
             }
         }
         return result;
     }
 
     @RequestMapping(value = "/grant/add", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> grantAdd(HttpServletRequest request, @RequestBody GrantRequest grantRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         logger.debug("grant add request: '" + grantRequest + "'");
         return doGrant(grantRequest, true);
     }
 
     @RequestMapping(value = "/grant/revoke", method = RequestMethod.POST)
     @ResponseBody
     public Map<String, String> gantRevoke(HttpServletRequest request, @RequestBody GrantRequest grantRequest) throws AuthException {
         ServletUtil.checkSecure(request);
         logger.debug("grant revoke request: '" + grantRequest + "'");
         return doGrant(grantRequest, false);
     }
 
     /**
      * Handle auth errors as part of normal application flow
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final AuthException e, HttpServletResponse response) {
         logger.info("Provisioning authentication error: " + e.getMessage());
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     /**
      * Handle all other errors
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final Exception e, HttpServletResponse response) {
         logger.error("Error handling provisioning request", bpConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(ProvisioningController2.class);
 
     private static final String BACKPLANE_UPDATE_SUCCESS = "BACKPLANE_UPDATE_SUCCESS";
     private static final String BACKPLANE_DELETE_SUCCESS = "BACKPLANE_DELETE_SUCCESS";
     private static final String GRANT_UPDATE_SUCCESS = "GRANT_UPDATE_SUCCESS";
     private static final String BACKPLANE_ENTRY_NOT_FOUND = "BACKPLANE_ENTRY_NOT_FOUND";
     private static final String ERR_MSG_FIELD = "ERR_MSG";
     private static final String CONFIG_NOT_FOUND = "CONFIG_NOT_FOUND";
 
     @Inject
     private Backplane2Config bpConfig;
 
     @Inject
     private DAOFactory daoFactory;
 
     private <T extends AbstractMessage> Map<String, Map<String, String>> doList(Class<T> entityType, List<String> entityNames, MessageField orderField) {
 
         if (entityNames.size() == 0) return doListAll(entityType);
 
         final Map<String,Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
         for(String entityName : entityNames) {
             T config = null;
             Exception thrown = null;
             try {
                 config = (T) daoFactory.getDaoByObjectType(entityType).get(entityName);
             } catch (Exception e) {
                 thrown = e;
             }
             final String errMgs = thrown != null ? thrown.getMessage() : config == null ? CONFIG_NOT_FOUND : null;
 
             result.put(entityName,
                 errMgs != null ? new HashMap<String, String>() {{ put(ERR_MSG_FIELD, errMgs); }} :
                 config);
         }
         return result;
     }
 
     private <T extends AbstractMessage> Map<String, Map<String, String>> doListAll(Class<T> entityType) {
         Map<String,Map<String,String>> result = new LinkedHashMap<String, Map<String, String>>();
         try {
             List<T> items = daoFactory.getDaoByObjectType(entityType).getAll();
             for(T config :  items) {
                 result.put(config.getIdValue(), config);
             }
         } catch (final Exception e) {
             result.put(ERR_MSG_FIELD, new HashMap<String, String>() {{ put(ERR_MSG_FIELD, e.getMessage()); }});
         }
         return result;
     }
 
     private <T extends AbstractMessage> Map<String, String> doDelete(Class<T> entityType, List<String> entityNames) {
         Map<String,String> result = new LinkedHashMap<String, String>();
         for(String entityName : entityNames) {
             String deleteStatus = BACKPLANE_DELETE_SUCCESS;
             try {
                 if (daoFactory.getDaoByObjectType(entityType).get(entityName) == null) {
                     deleteStatus = BACKPLANE_ENTRY_NOT_FOUND;
                 } else {
                     daoFactory.getDaoByObjectType(entityType).delete(entityName);
                 }
             } catch (Exception e) {
                 deleteStatus = e.getMessage();
             }
             result.put(entityName, deleteStatus);
         }
         return result;
     }
 
     private <T extends AbstractMessage> Map<String, String> doUpdate(Class<T> entityType, UpdateRequest<T> updateRequest) throws AuthException {
         bpConfig.checkAdminAuth(updateRequest.getAdmin(), updateRequest.getSecret());
         return updateConfigs(entityType, updateRequest.getConfigs());
     }
 
     private <T extends AbstractMessage> Map<String, String> updateConfigs(Class<T> customerConfigType, List<T> bpConfigs) {
         Map<String,String> result = new LinkedHashMap<String, String>();
         for(T config : bpConfigs) {
             String updateStatus = BACKPLANE_UPDATE_SUCCESS;
             try {
                 if (config instanceof User) {
                     // hash the new user password
                     User user = (User) config;
                     user.put(User.Field.PWDHASH.getFieldName(), HmacHashUtils.hmacHash(user.get(User.Field.PWDHASH)));
                 } else if (config instanceof BusConfig2) {
                     User user = null;
                     user = daoFactory.getBusOwnerDAO().get(config.get(BusConfig2.Field.OWNER.getFieldName()));
 
                     if (user == null) {
                        String fieldName = config.get(BusConfig2.Field.OWNER.getFieldName());
                        throw new BackplaneServerException("Invalid bus owner: " + fieldName);
                     }
                 }
 
                 config.validate();
                 daoFactory.getDaoByObjectType(customerConfigType).persist(config);
             } catch (Exception e) {
                 updateStatus = e.getMessage();
             }
             result.put(config.getIdValue(), updateStatus);
         }
         return result;
     }
 
     private Map<String, String> doGrant(GrantRequest grantRequest, boolean addRevoke) throws AuthException {
         Map<String,String> result = new LinkedHashMap<String, String>();
         bpConfig.checkAdminAuth(grantRequest.getAdmin(), grantRequest.getSecret());
 
         for(Map.Entry<String,String> newGrantEntry : grantRequest.getGrants().entrySet()) {
             String clientId = newGrantEntry.getKey();
             String buses = newGrantEntry.getValue();
             try {
                 if (null == daoFactory.getClientDAO().get(clientId)) {
                     result.put(clientId, "invalid client_id");
                 } else {
                     List<String> busesAsList = Scope.getScopesAsList(buses);
                     validateBuses(busesAsList);
                     if (addRevoke) {
                         addGrant(grantRequest.getAdmin(), clientId, busesAsList);
                         result.put(clientId, "GRANT_UPDATE_SUCCESS");
                     } else {
                         revokeBuses(clientId, busesAsList);
                         result.put(clientId, "GRANT_UPDATE_SUCCESS");
                     }
                 }
             } catch (Exception e) {
                 result.put(clientId, e.getMessage());
             }
         }
         return result;
     }
 
     private void validateBuses(List<String> buses) throws BackplaneServerException {
         for(String bus : buses) {
             if (null == daoFactory.getBusDao().get(bus)) {
                 throw new BackplaneServerException("Invalid bus: " + bus);
             }
         }
     }
 
     private void addGrant(String issuer, String clientId, List<String> buses) throws SimpleDBException, BackplaneServerException {
         Grant grant = new Grant.Builder(
                 GrantType.CLIENT_CREDENTIALS,
                 GrantState.ACTIVE,
                 issuer,
                 clientId,
                 Scope.getEncodedScopesAsString(BackplaneMessage.Field.BUS, buses))
                 .buildGrant();
         daoFactory.getGrantDao().persist(grant);
     }
 
     private void revokeBuses(String clientId, List<String> buses) throws TokenException, SimpleDBException, BackplaneServerException {
         boolean updated = false;
         Scope busesToRevoke = new Scope(Scope.getEncodedScopesAsString(BackplaneMessage.Field.BUS, buses));
         if ( ! daoFactory.getGrantDao().revokeBuses(daoFactory.getGrantDao().getByClientId(clientId), buses) ) {
             throw new BackplaneServerException("No grants found to revoke for buses: " + buses);
         }
     }
 
     // type helper classes for JSON mapper
     private static class BusUpdateRequest extends UpdateRequest<BusConfig2> {}
     private static class UserUpdateRequest extends UpdateRequest<User> {}
     private static class ClientUpdateRequest extends UpdateRequest<Client> {}
 }
