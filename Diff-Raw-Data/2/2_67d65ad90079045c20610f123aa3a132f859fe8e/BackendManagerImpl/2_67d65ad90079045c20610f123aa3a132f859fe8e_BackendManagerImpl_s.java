 /*
  * Copyright (C) 2012-2014  Cedric Cheneau
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.holmes.core.backend;
 
 import com.google.common.base.Function;
 import com.google.common.eventbus.EventBus;
 import net.holmes.core.backend.exception.BackendErrorMessage;
 import net.holmes.core.backend.exception.BackendException;
 import net.holmes.core.backend.response.ConfigurationFolder;
 import net.holmes.core.backend.response.Settings;
 import net.holmes.core.business.configuration.ConfigurationDao;
 import net.holmes.core.business.configuration.ConfigurationNode;
 import net.holmes.core.business.configuration.UnknownNodeException;
 import net.holmes.core.business.media.model.RootNode;
 import net.holmes.core.common.event.ConfigurationEvent;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.regex.Pattern;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 import static com.google.common.collect.Lists.transform;
 import static net.holmes.core.backend.exception.BackendErrorMessage.*;
 import static net.holmes.core.common.ConfigurationParameter.*;
 import static net.holmes.core.common.FileUtils.isValidDirectory;
 import static net.holmes.core.common.UniqueIdGenerator.newUniqueId;
 import static net.holmes.core.common.event.ConfigurationEvent.EventType.*;
 
 /**
  * Backend manager implementation.
  */
 public final class BackendManagerImpl implements BackendManager {
     private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp|file)://.+$", Pattern.CASE_INSENSITIVE);
 
     private final ConfigurationDao configurationDao;
     private final EventBus eventBus;
 
     /**
      * Instantiates a new backend manager implementation.
      *
      * @param configurationDao configuration dao
      * @param eventBus         event bus
      */
     @Inject
     public BackendManagerImpl(final ConfigurationDao configurationDao, final EventBus eventBus) {
         this.configurationDao = configurationDao;
         this.eventBus = eventBus;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Collection<ConfigurationFolder> getFolders(final RootNode rootNode) {
         return transform(configurationDao.getNodes(rootNode), new ConfigurationNodeFactory());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ConfigurationFolder getFolder(final String id, final RootNode rootNode) {
         try {
             return new ConfigurationNodeFactory().apply(configurationDao.getNode(rootNode, id));
         } catch (UnknownNodeException e) {
             throw new BackendException(rootNode == RootNode.PODCAST ? PODCAST_UNKNOWN_ERROR : FOLDER_UNKNOWN_ERROR, e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void addFolder(final ConfigurationFolder folder, final RootNode rootNode) {
         // Validate
         if (rootNode == RootNode.PODCAST) {
             validatePodcast(folder, rootNode, null);
         } else {
             validateFolder(folder, rootNode, null);
         }
 
         // Build new configuration node
         ConfigurationNode node = new ConfigurationNode(newUniqueId(), folder.getName(), folder.getPath());
         try {
             // Save config
             if (configurationDao.addNode(rootNode, node)) {
                 // Post add folder event
                 eventBus.post(new ConfigurationEvent(ADD_FOLDER, node, rootNode));
             }
         } catch (IOException e) {
             throw new BackendException(e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void editFolder(final String id, final ConfigurationFolder folder, final RootNode rootNode) {
         // Check folder
         if (rootNode == RootNode.PODCAST) {
             validatePodcast(folder, rootNode, id);
         } else {
             validateFolder(folder, rootNode, id);
         }
 
         try {
             // Edit node
             ConfigurationNode node = configurationDao.editNode(rootNode, id, folder.getName(), folder.getPath());
 
             if (node != null) {
                 // Post update folder event
                 eventBus.post(new ConfigurationEvent(UPDATE_FOLDER, node, rootNode));
             }
         } catch (IOException e) {
             throw new BackendException(e);
         } catch (UnknownNodeException e) {
             throw new BackendException(rootNode == RootNode.PODCAST ? PODCAST_UNKNOWN_ERROR : FOLDER_UNKNOWN_ERROR, e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeFolder(final String id, final RootNode rootNode) {
         try {
             // Remove node
             ConfigurationNode node = configurationDao.removeNode(id, rootNode);
 
             // Post remove folder event
             eventBus.post(new ConfigurationEvent(DELETE_FOLDER, node, rootNode));
         } catch (IOException e) {
             throw new BackendException(e);
         } catch (UnknownNodeException e) {
             throw new BackendException(rootNode == RootNode.PODCAST ? PODCAST_UNKNOWN_ERROR : FOLDER_UNKNOWN_ERROR, e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Settings getSettings() {
         return new Settings(configurationDao.getParameter(UPNP_SERVER_NAME),
                 configurationDao.getParameter(PODCAST_PREPEND_ENTRY_NAME));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void saveSettings(final Settings settings) {
         checkNonEmpty(settings.getServerName(), SETTINGS_SERVER_NAME_ERROR);
 
         configurationDao.setParameter(UPNP_SERVER_NAME, settings.getServerName());
         configurationDao.setParameter(PODCAST_PREPEND_ENTRY_NAME, settings.getPrependPodcastItem());
         try {
             // save settings
             configurationDao.save();
         } catch (IOException e) {
             throw new BackendException(e);
         }
         // Post event
         eventBus.post(new ConfigurationEvent(SAVE_SETTINGS));
     }
 
     /**
      * Validate folder.
      *
      * @param folder     folder to validate
      * @param rootNode   root configuration node
      * @param excludedId folder id excluded from duplication checking
      */
     private void validateFolder(final ConfigurationFolder folder, final RootNode rootNode, final String excludedId) {
         // Check folder's name and path are not empty
         checkNonEmpty(folder.getName(), FOLDER_NAME_ERROR);
         checkNonEmpty(folder.getPath(), FOLDER_PATH_ERROR);
 
         // Check folder path exists
         if (!isValidDirectory(new File(folder.getPath()))) {
             throw new BackendException(FOLDER_PATH_UNKNOWN_ERROR);
         }
 
         // Check for duplication
         checkDuplicatedConfigurationFolder(folder, rootNode, excludedId, FOLDER_DUPLICATED_ERROR);
     }
 
     /**
      * Validate podcast.
      *
      * @param podcast    podcast to validate
      * @param rootNode   root configuration node
      * @param excludedId podcast id excluded from duplication check
      */
     private void validatePodcast(final ConfigurationFolder podcast, final RootNode rootNode, final String excludedId) {
         // Check podcast name and path are not empty
         checkNonEmpty(podcast.getName(), PODCAST_NAME_ERROR);
         checkNonEmpty(podcast.getPath(), PODCAST_URL_ERROR);
 
         // Check podcast URL is correct
         if (!URL_PATTERN.matcher(podcast.getPath()).matches()) {
             throw new BackendException(PODCAST_BAD_URL_ERROR);
         }
 
         // Check for duplication
         checkDuplicatedConfigurationFolder(podcast, rootNode, excludedId, PODCAST_DUPLICATED_ERROR);
     }
 
     /**
      * Checks string is not null or empty.
      *
      * @param toCheck string to check
      * @param message error message
      */
     private void checkNonEmpty(String toCheck, BackendErrorMessage message) {
         if (isNullOrEmpty(toCheck)) {
             throw new BackendException(message);
         }
     }
 
     /**
      * Checks configuration does not already exist.
      *
      * @param folder       configuration folder to check
      * @param rootNode     root node
      * @param excludedId   folder id to exclude from duplication check
      * @param errorMessage error message
      */
     private void checkDuplicatedConfigurationFolder(final ConfigurationFolder folder, final RootNode rootNode, final String excludedId, final BackendErrorMessage errorMessage) {
         if (configurationDao.findNode(rootNode, excludedId, folder.getName(), folder.getPath()) != null) {
             throw new BackendException(errorMessage);
         }
     }
 
     /**
     *
      */
     private static final class ConfigurationNodeFactory implements Function<ConfigurationNode, ConfigurationFolder> {
 
         /**
          * {@inheritDoc}
          */
         @Override
         public ConfigurationFolder apply(ConfigurationNode node) {
             return node == null ? null : new ConfigurationFolder(node.getId(), node.getLabel(), node.getPath());
         }
     }
 }
