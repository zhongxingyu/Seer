 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011 CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.yaml;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.craftfire.commons.util.LoggingManager;
 
 /**
  * An interface for all kinds of yaml managers.
  */
 public interface YamlManager {
 
     /**
      * Returns a set of files used by the manager.
      * 
      * @return set of files
      */
     Set<File> getFiles();
 
     /**
      * Returns the logging manager used by this manager.
      * 
      * @return the logging manager
      */
     LoggingManager getLogger();
 
     /**
      * Sets the logging manager to be used by this manager.
      * 
      * @param  loggingManager            the logging manager
      * @throws IllegalArguementException if the loggingManager is {@code null}
      */
     void setLoggingManager(LoggingManager loggingManager);
 
     /**
      * Returns the root YamlNode of the document loaded by this manager.
      * 
      * @return the root YamlNode
      */
     YamlNode getRootNode();
 
     /**
      * Replaces the root YamlNode of the document loaded by this manager with given node.
      * 
      * @param node  the node to replace the root node
      * @return      the new root node
      */
     YamlNode setRootNode(YamlNode node);
 
     /**
      * Checks if node with given path exists in the manager.
      * 
      * @param node  the node path to check
      * @return      {@code true} if exists, {@code false} otherwise
      */
     boolean exist(String node);
 
     /**
      * Returns {@code boolean} value of node with given path.
      * <p>
      * Defaults to {@code false} if the node doesn't exist or can't be converted to {@code boolean}.
      * 
      * @param node  the node path
      * @return      {@code boolean} value of the node or {@code false} if not found or can't be converted
      */
     boolean getBoolean(String node);
 
     /**
      * Returns {@code boolean} value of node with given path.
      * 
      * @param node          the node path
      * @param defaultValue  the default value to use if the node doesn't exist or can't be converted to {@code boolean}
      * @return              {@code boolean} value of the node or {@code defaultValue} if not found or can't be converted
      */
     boolean getBoolean(String node, boolean defaultValue);
 
     /**
      * Returns {@code String} value of node with given path.
      * <p>
      * Defaults to {@code null} if the node doesn't exist or can't be converted to {@code String}.
      * 
      * @param node  the node path
      * @return      {@code String} value of the node or {@code null} if not found or can't be converted
      */
     String getString(String node);
 
     /**
      * Returns {@code String} value of node with given path.
      * 
      * @param node          the node path
      * @param defaultValue  the default value to use if the node doesn't exist or can't be converted to {@code String}
      * @return              {@code String} value of the node or {@code defaultValue} if not found or can't be converted
      */
     String getString(String node, String defaultValue);
 
     /**
      * Returns {@code int} value of node with given path.
      * <p>
      * Defaults to {@code 0} if the node doesn't exist or can't be converted to {@code int}.
      * 
      * @param node  the node path
      * @return      {@code int} value of the node or {@code null} if not found or can't be converted
      */
     int getInt(String node);
 
     /**
      * Returns {@code int} value of node with given path.
      * 
      * @param node          the node path
      * @param defaultValue  the default value to use if the node doesn't exist or can't be converted to {@code int}
      * @return              {@code int} value of the node or {@code defaultValue} if not found or can't be converted
      */
     int getInt(String node, int defaultValue);
 
     /**
      * Returns {@code long} value of node with given path.
      * <p>
      * Defaults to {@code 0} if the node doesn't exist or can't be converted to {@code long}.
      * 
      * @param node  the node path
      * @return      {@code long} value of the node or {@code null} if not found or can't be converted
      */
     long getLong(String node);
 
     /**
      * Returns {@code long} value of node with given path.
      * 
      * @param node          the node path
      * @param defaultValue  the default value to use if the node doesn't exist or can't be converted to {@code long}
      * @return              {@code long} value of the node or {@code defaultValue} if not found or can't be converted
      */
     long getLong(String node, long defaultValue);
 
     /**
      * Adds all nodes from given YamlManager to this manager's document.
      * 
      * @param  yamlManager   the YamlManager to take nodes from
      * @throws YamlException if the root node of the document is a scalar
      */
     void addNodes(YamlManager yamlManager) throws YamlException;
 
     /**
      * Adds all nodes from given map to this manager's document.
      * 
      * @param  map           map of nodes to add
      * @throws YamlException if the root node of the document is a scalar
      */
     void addNodes(Map<String, Object> map) throws YamlException;
 
     /**
      * Sets node with the given path to given value.
      * 
      * @param  node          the node path
      * @param  value         the value to set
      * @throws YamlException if one of path elements is a scalar node
      */
     void setNode(String node, Object value) throws YamlException;
 
     /**
      * Returns YamlNode with the given path.
      * 
      * @param  node          the node path
     * @return               the YamlNode, or {@code null} if not found
      * @throws YamlException if one of path elements is a scalar node
      */
     YamlNode getNode(String node) throws YamlException;
 
     /**
      * Returns a map of the values of all the final nodes. The map key is the node path, the map value is the node value. 
      * 
      * @return a map of values of all the final nodes
      */
     @Deprecated
     public Map<String, Object> getNodes();
 
     /**
      * Returns list of all scalar nodes loaded by the manager.
      * 
      * @return list of all scalar nodes
      */
     List<YamlNode> getFinalNodeList();
 
     /**
      * Returns total number of all scalar nodes loaded by the manager.
      * 
      * @return number of all scalar nodes
      */
     int getFinalNodeCount();
 
     /**
      * Saves the manager's document to the file it was loaded from.
      * <p>
      * Fails if the document wasn't loaded from a file or IOException occurred.
      * 
      * @return {@code true} if succeed, {@code false} otherwise
      */
     boolean save();
 
     /**
      * Loads the manager's document.
      * <p>
      * Fails if trying to load the document from a stream or a reader more than once, or IOException occurred.
      * 
      * @return {@code true} if succeed, {@code false} otherwise
      */
     boolean load();
 
 }
