 package com.tehbeard.beardstat;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.tehbeard.beardstat.dataproviders.IStatDataProvider;
 import com.tehbeard.beardstat.dataproviders.ProviderQuery;
 import com.tehbeard.beardstat.dataproviders.ProviderQueryResult;
 import com.tehbeard.utils.mojang.api.profiles.Profile;
 import com.tehbeard.utils.mojang.api.profiles.ProfileCriteria;
 import com.tehbeard.utils.mojang.api.profiles.ProfileRepository;
 
 
 public class ProfileUUIDUpdater {
     
     public ProfileUUIDUpdater(Logger logger, IStatDataProvider provider, ProfileRepository profileRepo){
         
         logger.warning("Updating UUIDs, warning! This may take a while.");
         logger.info("Loading list of players stored in database");
         
         //Grab the players we have to process.
         ProviderQueryResult[] results = provider.queryDatabase(new ProviderQuery(null, IStatDataProvider.PLAYER_TYPE, null, false));
         List<ProfileCriteria> toProcess = new ArrayList<ProfileCriteria>();
         Set<String> names = new HashSet<String>();
         
         logger.info("Locating entries with no uuid.");
         for( ProviderQueryResult result : results){
             if(result.uuid == null){
                 toProcess.add(new ProfileCriteria(result.name,"minecraft"));
                 names.add(result.name.toLowerCase());
             }
         }
         logger.info("Found " + toProcess.size() + " entries with no uuid");
 
         //Process the players in batches of up to 128
         int partitionSize = 128;
         int batches = (int) Math.ceil(toProcess.size() / partitionSize);
         logger.info("Querying Mojang username -> uuid server @ " + partitionSize + " players per request");
         logger.info("Total batches: " + batches);
        int batch = 1;
         for (int i = 0; i < toProcess.size(); i += partitionSize) {
            logger.info("Processing batch: " + batch);
            batch++;
            
             Profile[] serverResults = profileRepo.findProfilesByCriteria(toProcess.subList(i,i + Math.min(partitionSize, toProcess.size() - i)).toArray(new ProfileCriteria[0]));
             for(Profile profile : serverResults){
                 provider.setUUID(profile.getName(), profile.getId());
                 names.remove(profile.getName().toLowerCase());
             }
         }
         logger.info("ids processed.");
         
         //Alert admin if any were not processed.
         if(names.size() > 0){
            logger.warning("Some usernames did not return a profile id, this could be an issue with mojang's server, or the username is no longer valid. (reverted to a non paid name)");
             logger.warning("These players uuids could not be collected.");
             for(String name : names){
                 logger.warning(name);
             }
             logger.warning("If you believe these players to be active, please try again.");
         }
     }
 
 }
