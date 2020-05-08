 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.impl.plugin;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mule.galaxy.Dao;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.artifact.ArtifactType;
 import org.mule.galaxy.index.IndexManager;
 import org.mule.galaxy.plugin.DowngradeNotSupportedException;
 import org.mule.galaxy.plugin.Plugin;
 import org.mule.galaxy.plugin.UpgradeNotSupportedException;
 import org.mule.galaxy.render.RendererManager;
 import org.mule.galaxy.type.TypeManager;
 
 /**
  * Makes it easy to add indexes and views for a new artifact type.
  */
 public abstract class AbstractPlugin implements Plugin {
 
     protected Registry registry;
     protected Dao<ArtifactType> artifactTypeDao;
     protected RendererManager rendererManager;
     protected IndexManager indexManager;
     protected TypeManager typeManager;
     
     protected final Log log = LogFactory.getLog(getClass());
 
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     public void setArtifactTypeDao(Dao<ArtifactType> artifactTypeDao) {
         this.artifactTypeDao = artifactTypeDao;
     }
 
     public void setRendererManager(RendererManager viewManager) {
         this.rendererManager = viewManager;
     }
 
     public void setIndexManager(IndexManager indexManager) {
         this.indexManager = indexManager;
     }
 
     public void setTypeManager(TypeManager typeManager) {
         this.typeManager = typeManager;
     }
 
     public String getName() {
         return getClass().getName();
     }
 
     public void update(final Integer previousVersion) throws Exception
     {
         if (null == previousVersion)
         {
             log.info(String.format("Installing new plugin v%d: %s", getVersion(), getName()));
             doInstall();
         }
        else if (previousVersion > getVersion())
         {
             if (!isUpgradeSupported())
             {
                 throw new UpgradeNotSupportedException();
             }
             log.info(String.format("Upgrading plugin from v%d to v%d: %s", previousVersion, getVersion(), getName()));
             doUpgrade();
         }
        else if (previousVersion < getVersion())
         {
             if (!isDowngradeSupported())
             {
                 throw new DowngradeNotSupportedException();
             }
 
             log.info(String.format("Downgrading plugin from v%d to v%d: %s", previousVersion, getVersion(), getName()));
             doDowngrade();
         }
         else
         {
             // same version, nothing else to do
             log.info(String.format("Plugin version unchanged, using current v%d: %s", getVersion(), getName()));
         }
     }
 
     protected void doInstall() throws Exception
     {
         // no-op
     }
 
     public boolean isDowngradeSupported()
     {
         return false;
     }
 
     public boolean isUpgradeSupported()
     {
         return false;
     }
 
     protected void doUpgrade() throws Exception
     {
         // no-op
     }
 
     protected void doDowngrade() throws Exception
     {
         // no-op
     }
 
     protected void doInitialize() throws Exception
     {
         // no-op
     }
 
     public void initialize() throws Exception
     {
         log.info(String.format("Initializing v%d of plugin: %s", getVersion(), getName()));
         doInitialize();
     }
 }
