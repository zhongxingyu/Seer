 /*
  * Copyright (c) 2010 BlipIt Committers
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package com.thoughtworks.blipit;
 
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.thoughtworks.blipit.domain.Blip;
 import com.thoughtworks.blipit.persistance.DataStoreHelper;
 import com.thoughtworks.contract.BlipItResponse;
 import com.thoughtworks.contract.Constants;
 import com.thoughtworks.contract.common.Category;
 import com.thoughtworks.contract.common.Channel;
 import com.thoughtworks.contract.common.GetChannelsResponse;
 import com.thoughtworks.contract.publish.BlipItPublishResource;
 import com.thoughtworks.contract.publish.DeleteBlipRequest;
 import com.thoughtworks.contract.publish.SaveBlipRequest;
 import com.thoughtworks.contract.publish.SaveBlipResponse;
 import org.datanucleus.util.StringUtils;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 
 import javax.jdo.PersistenceManager;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static com.thoughtworks.blipit.Utils.splitByComma;
 
 public class BlipItPublishResourceImpl extends BlipItCommonServerResource implements BlipItPublishResource {
     private static final Logger log = Logger.getLogger(BlipItPublishResourceImpl.class.getName());
 
     public BlipItPublishResourceImpl() {
         super();
     }
 
     @Post
     public Representation acceptBlip(Representation entity) {
         final Representation[] result = {null};
         Blip blip = getBlip(new Form(entity));
         DataStoreHelper.save(blip, new Utils.ResultHandler<Blip>() {
             public void onSuccess(Blip arg) {
                 setStatus(Status.SUCCESS_CREATED);
                 result[0] = new StringRepresentation("Blip creation successful", MediaType.TEXT_PLAIN);
                 log.info("Blip with title, " + arg.getTitle() + " saved successfully with ID: " + arg.getKey());
             }
 
             public void onError(Throwable throwable) {
                 setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                 result[0] = new StringRepresentation("Blip creation failed with error: " + throwable, MediaType.TEXT_PLAIN);
                 log.log(Level.SEVERE, "Blip creation failed with error", throwable);
             }
         });
         return result[0];
     }
 
     @Post
     public SaveBlipResponse saveBlip(SaveBlipRequest saveBlipRequest) {
         final SaveBlipResponse saveBlipResponse = new SaveBlipResponse();
         Blip blip = constructBlip(saveBlipRequest, saveBlipResponse);
         if (saveBlipResponse.isSuccess()) {
             DataStoreHelper.save(blip, new Utils.ResultHandler<Blip>() {
                 public void onSuccess(Blip savedBlip) {
                     saveBlipResponse.setSuccess();
                     String keyAsString = savedBlip.getKeyAsString();
                     saveBlipResponse.setBlipId(keyAsString);
                     log.info("Blip with title, " + savedBlip.getTitle() + " saved successfully with ID: " + keyAsString);
                 }
 
                 public void onError(Throwable throwable) {
                     saveBlipResponse.setFailure(Utils.getBlipItError(throwable.getMessage()));
                     log.log(Level.SEVERE, "Blip creation failed with error", throwable);
                 }
             });
         }
         return saveBlipResponse;
     }
 
     private Blip constructBlip(SaveBlipRequest saveBlipRequest, SaveBlipResponse saveBlipResponse) {
         Blip blip = null;
         PersistenceManager persistenceManager = null;
 
         try {
             persistenceManager = DataStoreHelper.getPersistenceManager();
             List<Channel> channels = saveBlipRequest.getChannels();
             Set<Key> channelKeys = new HashSet<Key>();
             for (Channel channel : channels) {
                 channelKeys.add(mapToChannelKey(channel));
             }
 
             String blipId = saveBlipRequest.getBlipId();
             String title = saveBlipRequest.getMetaDataValue(Constants.TITLE);
             String description = saveBlipRequest.getMetaDataValue(Constants.DESC);
             GeoPt blipLoc = Utils.asGeoPoint(saveBlipRequest.getBlipLocation());
 
             if (StringUtils.notEmpty(blipId)) {
                 Key blipKey = KeyFactory.stringToKey(blipId);
                 blip = persistenceManager.getObjectById(Blip.class, blipKey);
                 blip.setTitle(title);
                 blip.setDescription(description);
                 blip.setGeoPoint(blipLoc);
                 blip.setChannelKeys(channelKeys);
             } else {
                 blip = new Blip(title, description, blipLoc, channelKeys);
             }
            saveBlipResponse.setSuccess();
         } catch (Exception e) {
             saveBlipResponse.setFailure(Utils.getBlipItError(e.getMessage()));
             log.log(Level.SEVERE, "Blip creation failed with error", e);
         } finally {
             if (persistenceManager != null) persistenceManager.close();
         }
 
         return blip;
     }
 
     @Delete
     public BlipItResponse deleteBlip(DeleteBlipRequest deleteBlipRequest) {
         final String blipId = deleteBlipRequest.getBlipId();
         final BlipItResponse blipItResponse = new BlipItResponse();
         DataStoreHelper.delete(Blip.class, blipId, new Utils.ResultHandler<Blip>() {
             public void onSuccess(Blip arg) {
                 blipItResponse.setSuccess();
                 log.info("Blip with ID, " + blipId + " deleted successfully");
             }
 
             public void onError(Throwable throwable) {
                 blipItResponse.setFailure(Utils.getBlipItError(throwable.getMessage()));
                 log.log(Level.SEVERE, "Blip deletion failed with error", throwable);
             }
         });
         return blipItResponse;
     }
 
     private Key mapToChannelKey(Channel channel) {
         return KeyFactory.stringToKey(channel.getId());
     }
 
     // TODO: This method should attempt to load the channel ID from data store and then create the Blip. look at saveBlip()
     // FIX ME: This method is broken !!! Does not set the channels correctly on a given blip !!!
     private Blip getBlip(Form form) {
         String blipTitle = form.getFirstValue("alert.title");
         String blipDescription = form.getFirstValue("alert.desc");
         Float blipLatitude = Float.valueOf(form.getFirstValue("alert.loc.lat"));
         Float blipLongitude = Float.valueOf(form.getFirstValue("alert.loc.long"));
         List<String> channelStrs = splitByComma(form.getFirstValue("alert.channels"));
         List<com.thoughtworks.blipit.domain.Channel> channels = new ArrayList<com.thoughtworks.blipit.domain.Channel>();
 
 /*
         for (String channelStr : channelStrs) {
             com.thoughtworks.blipit.domain.Channel channel = new com.thoughtworks.blipit.domain.Channel();
             channel.setName(channelStr);
             channel.setCategory(CategoryEnum.AD);
             channels.add(channel);
         }
 */
         return new Blip(blipTitle, blipDescription, new GeoPt(blipLatitude, blipLongitude), null); // <-- we're assigning 'null' for channel keys here
     }
 
     @Get
     public GetChannelsResponse getAvailableChannels(Category channelCategory) {
         return getChannels(channelCategory);
     }
 }
