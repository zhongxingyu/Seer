 package org.wyona.yarep.core;
 
 import org.apache.avalon.framework.configuration.Configuration;
 
 import java.io.File;
 
 /**
  *
  */
 public interface Map {
 
     /**
      *
      */
     public void readConfig(Configuration mapConfig, File repoConfigFile) throws RepositoryException;
 
     /**
      *
      */
     public boolean isResource(Path path) throws RepositoryException;
 
     /**
      *
      */
     public boolean isCollection(Path path) throws RepositoryException;
 
     /**
     * Check whether node with a particular path exists
     * @param path Path of node
      */
     public boolean exists(Path path) throws RepositoryException;
 
     /**
      *
      */
     public boolean delete(Path path) throws RepositoryException;
 
     /**
      *
      */
     public Path[] getChildren(Path path) throws RepositoryException;
 
     /**
      *
      */
     public UID getUID(Path path) throws RepositoryException;
 
     /**
      *
      */
     public UID create(Path path, int type) throws RepositoryException;
 
     /**
      *
      */
     public void addSymbolicLink(Path link, UID uid) throws RepositoryException;
 }
