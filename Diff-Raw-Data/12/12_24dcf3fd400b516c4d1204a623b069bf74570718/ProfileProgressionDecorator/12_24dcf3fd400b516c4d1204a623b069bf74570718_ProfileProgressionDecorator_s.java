 /*
  * Quality Profile Progression
  * Copyright (C) 2012 David T S Maitland
  * david.ts.maitland@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.qualityprofile.progression.batch;
 
import org.apache.commons.lang.ArrayUtils;

import org.sonar.plugins.qualityprofile.progression.ProfileProgressionException;
import org.sonar.plugins.qualityprofile.progression.ProfileProgressionPlugin;

 import java.math.BigDecimal;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.Decorator;
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.config.Settings;
 import org.sonar.api.database.DatabaseSession;
 import org.sonar.api.database.model.ResourceModel;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.resources.Scopes;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.api.rules.Violation;
 import org.sonar.jpa.dao.ProfilesDao;
 
 //TODO add global setting for email address
 //TODO add project setting for email address
 //TODO send email when changing profile
 //TODO send email when project breaches threshold for last profile
 public class ProfileProgressionDecorator implements Decorator
 {
   private DatabaseSession session;
   private RulesProfile profile;
   private Settings settings;
   private ProfilesDao profilesDao;
   Logger logger = LoggerFactory.getLogger(this.getClass());
 
   public ProfileProgressionDecorator(DatabaseSession session, RulesProfile profile, Settings settings)
   {
     super();
     this.session = session;
     this.profile = profile;
     this.settings = settings;
     this.profilesDao = new ProfilesDao(session);
   }
 
   public boolean shouldExecuteOnProject(Project project)
   {
     return isPluginEnabled();
   }
 
   protected boolean isPluginEnabled()
   {
     // first check if its been disabled globally
     boolean isEnabled = settings.getBoolean(ProfileProgressionPlugin.GLOBAL_QUALITY_PROFILE_CHANGE_ENABLED_KEY);
 
     logger.debug("{} is{} enabled at global level.", this.getClass().getSimpleName(), (isEnabled ? "" : " not"));
 
     if (isEnabled)
     // also check project
     {
       isEnabled = settings.getBoolean(ProfileProgressionPlugin.PROJECT_QUALITY_PROFILE_CHANGE_ENABLED_KEY);
 
       logger.debug("{} is{} enabled at project level.", this.getClass().getSimpleName(), (isEnabled ? "" : " not"));
     }
 
     return isEnabled;
   }
 
   public void decorate(Resource resource, DecoratorContext context)
   {
     if (Scopes.isProject(resource))
     {
       try
       {
         logger.debug("Analysed project: {}", resource.getEffectiveKey());
 
         int violationThreshold = getViolationThreshold();
 
         String[] profileNameArray = getProfileNameArray(resource.getLanguage().getKey());
 
         int projectViolationMeasure = getProjectsViolationPercentage(context);
 
         // if measure lower than threshold progress quality profile
         if (projectViolationMeasure < violationThreshold)
         {
           logger.info("{} project's % violations ({}) lower than threshold ({}).", new Object[] {resource.getEffectiveKey(),
             projectViolationMeasure, violationThreshold});
           progressQualityProfile(resource, profileNameArray);
         }
         else
         {
           logger.debug("Project's % violations is not lower than threshold.");
         }
 
       } catch (ProfileProgressionException pie)
       {
         logger.warn(pie.getMessage());
       }
     }
   }
 
   protected void progressQualityProfile(Resource resource, String[] profileNameArray) throws ProfileProgressionException
   {
     // get current quality profile - profile class
     // variable
     String profileName = profile.getName();
 
     logger.debug("Project's current profile: {}", profileName);
 
     // get current profile index
     int currentProfileIndex = getMatchingIndex(profileNameArray, profileName);
 
     // loop variables
     RulesProfile profileToUpdate = null;
     String parentProfileName = null;
 
     // if direct profile not a managed one
     check_profile_hierarchy: while (currentProfileIndex < 0)
     {
       if (profileToUpdate == null)
       {
         logger.debug("{} project's profile ({}) is not in incremetor's set {}.", new Object[] {resource.getEffectiveKey(), profileName,
           profileNameArray});
       }
 
       logger.debug("Getting {} profile's parent.", profileName);
 
       // get profile object
       profileToUpdate = profilesDao.getProfile(resource.getLanguage().getKey(), profileName);
 
       if (profileToUpdate == null)
       {
         throw new ProfileProgressionException("Unable to find profile '" + profileName + "' for language '"
           + resource.getLanguage().getName() + "'.");
       }
       else
       {
         // get parent profile's name
         parentProfileName = profileToUpdate.getParentName();
 
         if (parentProfileName != null)
         {
           logger.debug("{} profile's parent is {}.", profileName, parentProfileName);
 
           // check if parent profile is managed
           currentProfileIndex = getMatchingIndex(profileNameArray, parentProfileName);
         }
         else
         {
           logger.debug("Profile has null parent.");
           profileToUpdate = null;
           break check_profile_hierarchy;
         }
 
         // make parent current profile so we get that profile object in
         // next loop iteration
         profileName = parentProfileName;
       }
     }
 
     // if managed profile not found anywhere update the project to first
     // profile
     // or use next profile in list
     int nextProfileIndex = currentProfileIndex + 1;
 
     if (nextProfileIndex >= profileNameArray.length)
     {
       logger.debug("Project is associated with last profile ({}) in set ({}).", profile.getName(), profileNameArray);
     }
     else
     {
       String nextProfileName = profileNameArray[nextProfileIndex];
       logger.debug("Next profile name is {}.", nextProfileName);
 
       if (profileToUpdate == null)
       {
         updateProjectsProfile(resource, nextProfileName);
       }
       else
       {
         updateProfilesParent(profileToUpdate, nextProfileName);
       }
     }
   }
 
   protected void updateProfilesParent(RulesProfile profileToUpdate, String newParentProfileName)
   {
     logger.info("{} profile's parent is managed by {}", profileToUpdate.getName(), this.getClass().getSimpleName());
     logger.info("Changing {} profile's parent to {}", profileToUpdate.getName(), newParentProfileName);
     profileToUpdate.setParentName(newParentProfileName);
     session.save(profileToUpdate);
     session.commit();
   }
 
   protected void updateProjectsProfile(Resource resource, String newProfileName) throws ProfileProgressionException
   {
     logger.info("Changing {} project's profile to {}", resource.getEffectiveKey(), newProfileName);
 
     // build project resource model
     ResourceModel resourceModel = session.getEntityManager().find(ResourceModel.class, resource.getId());
 
     if (resourceModel == null)
     {
       throw new ProfileProgressionException("Unable to find project '" + resource.getEffectiveKey() + "' in database with id: "
         + resource.getId());
     }
     else
     {
       // set project's quality profile to next
       // one
       RulesProfile nextProfile = profilesDao.getProfile(resource.getLanguage().getKey(), newProfileName);
 
       if (nextProfile == null)
       {
         throw new ProfileProgressionException("Unable to find profile '" + newProfileName + "' for language "
           + resource.getLanguage().getName() + "'.");
       }
       else
       {
         resourceModel.setRulesProfile(nextProfile);
 
         // save project change
         session.save(resourceModel);
         session.commit();
       }
     }
   }
 
   protected int getProjectsViolationPercentage(DecoratorContext context)
   {
     List<Violation> violations = context.getViolations();
     logger.debug("Found {} violations.", violations.size());
 
     List<ActiveRule> rules = profile.getActiveRules();
     logger.debug("Found {} rules.", rules.size());
 
     // get % violation measure
     BigDecimal numberOfViolations = new BigDecimal(violations.size());
     BigDecimal numberOfRules = new BigDecimal(rules.size());
     BigDecimal projectViolationDecimal = numberOfViolations.divide(numberOfRules);
     int projectViolationPercentage = projectViolationDecimal.multiply(new BigDecimal(100)).intValue();
 
     logger.debug("Project's % violations: {}", projectViolationPercentage);
 
     return projectViolationPercentage;
   }
 
   protected String[] getProfileNameArray(String languageKey) throws ProfileProgressionException
   {
     // check for project setting
     String lastProfileName = getLanguagesTargetProfileName(languageKey, ProfileProgressionPlugin.PROJECT_TARGET_LANGUAGE_QUALITY_PROFILE_KEY);
 
     if (lastProfileName == null || lastProfileName.length() < 1)
     {
       // get global setting
       lastProfileName = getLanguagesTargetProfileName(languageKey, ProfileProgressionPlugin.GLOBAL_TARGET_LANGUAGE_QUALITY_PROFILE_KEY);
     }
 
     if (lastProfileName == null)
     {
       throw new ProfileProgressionException("No target quality profile found.");
     }
 
     return getProfileHierchy(languageKey, lastProfileName);
   }
 
   protected String[] getProfileHierchy(String languageKey, String lastProfileName) throws ProfileProgressionException
   {
     // collect profile hierarchy
     List<String> profiles = new ArrayList<String>();
     profiles.add(lastProfileName);
 
     // get last profile in hierarchy
     RulesProfile profile = profilesDao.getProfile(languageKey, lastProfileName);
 
     // check we can find the specified profile for the current project's language
     if (profile == null)
     {
       throw new ProfileProgressionException("Unable to find \"" + lastProfileName + "\" quality profile for language: " + languageKey);
     }
 
     // navigate up profile hierarchy
     while (profile != null && profile.getParentName() != null)
     {
       profiles.add(profile.getParentName());
       profile = profilesDao.getProfile(languageKey, profile.getParentName());
     }
 
     // convert List to String[]
     String[] profileHierarchy = profiles.toArray(new String[profiles.size()]);
 
     // make lastProfileName last
     ArrayUtils.reverse(profileHierarchy);
 
     return profileHierarchy;
   }
 
   protected String getLanguagesTargetProfileName(String languageKey, String settingName)
   {
     String targetProfileName = null;
 
     String targetProfileSetting = settings.getString(settingName);
 
     if (targetProfileSetting != null) {
 
       StringTokenizer tokenizer = new StringTokenizer(targetProfileSetting, ProfileProgressionPlugin.TARGET_LANGUAGE_QUALITY_PROFILE_DELIM);
 
       // if more than one language extract quality profile assigned to current project's language
       if (tokenizer.countTokens() > 1 && targetProfileSetting.contains(languageKey))
       {
         String langProfilePair = null;
         while (tokenizer.hasMoreTokens())
         {
           langProfilePair = tokenizer.nextToken();
           if (langProfilePair != null && langProfilePair.startsWith(languageKey))
           {
             targetProfileName = langProfilePair.substring(languageKey.length() + ProfileProgressionPlugin.TARGET_LANGUAGE_QUALITY_PROFILE_ASSIGN.length());
           }
         }
       }
       else
       // just take the value specified
       {
         targetProfileName = targetProfileSetting.replaceAll(ProfileProgressionPlugin.TARGET_LANGUAGE_QUALITY_PROFILE_DELIM, "").trim();
       }
     }
 
     return targetProfileName;
   }
 
   protected Integer getViolationThreshold(String thresholdKey) throws ProfileProgressionException
   {
     Integer violationThreshold = null;
 
     // get threshold setting
     try
     {
       violationThreshold = settings.getInt(thresholdKey);
     } catch (NumberFormatException nfe)
     {
       logger.warn("Error parsing {} setting value \"{}\" to an integer.", thresholdKey, settings.getString(thresholdKey));
       violationThreshold = 0;
     }
 
     logger.debug("Found {} setting: {}", thresholdKey, violationThreshold);
 
     // validate threshold value
     if (violationThreshold < 0 || violationThreshold >= 100)
     {
       throw new ProfileProgressionException(thresholdKey + " setting value " + violationThreshold
         + " out of range; should be in the range 0-99.");
     }
 
     return violationThreshold;
   }
 
   protected int getViolationThreshold() throws ProfileProgressionException
   {
     Integer violationThreshold = getViolationThreshold(ProfileProgressionPlugin.PROJECT_QUALITY_PROFILE_CHANGE_THRESHOLD_KEY);
 
     if (violationThreshold == null || violationThreshold == 0)
     {
       violationThreshold = getViolationThreshold(ProfileProgressionPlugin.GLOBAL_QUALITY_PROFILE_CHANGE_THRESHOLD_KEY);
     }
 
     if (violationThreshold == null || violationThreshold == 0)
     {
       throw new ProfileProgressionException("Violation threshold setting canot be found (or is zero).");
     }
 
     return violationThreshold;
   }
 
   protected int getMatchingIndex(String[] profileNameArray, String profileName)
   {
     int index = -1;
     for (int i = 0; i < profileNameArray.length; i++)
     {
       if (profileName.equals(profileNameArray[i]))
       {
         index = i;
         break;
       }
     }
 
     logger.debug("{} profile's index of array: {}", profileName, index);
 
     return index;
   }
 
   public void setProfilesDao(ProfilesDao profilesDao)
   {
     this.profilesDao = profilesDao;
   }
 
 }
