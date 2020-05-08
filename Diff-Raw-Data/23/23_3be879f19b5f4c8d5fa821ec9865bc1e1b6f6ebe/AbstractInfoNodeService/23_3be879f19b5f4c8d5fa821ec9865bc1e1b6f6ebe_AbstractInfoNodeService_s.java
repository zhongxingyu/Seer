 /*
  *   Copyright 2011-2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.thruzero.domain.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.jdom.JDOMException;
 
 import com.thruzero.common.core.bookmarks.InitializationParameterKeysBookmark;
 import com.thruzero.common.core.infonode.InfoNodeElement;
 import com.thruzero.common.core.infonode.builder.SaxInfoNodeBuilder;
 import com.thruzero.common.core.infonode.builder.filter.InfoNodeFilter;
 import com.thruzero.common.core.infonode.builder.filter.SimpleInfoNodeFilterChain;
 import com.thruzero.common.core.locator.Initializable;
 import com.thruzero.common.core.locator.InitializationException;
 import com.thruzero.common.core.locator.InitializationStrategy;
 import com.thruzero.common.core.locator.LocatorUtils;
 import com.thruzero.common.core.map.StringMap;
 import com.thruzero.common.core.support.ContainerPath;
 import com.thruzero.common.core.support.EntityPath;
 import com.thruzero.common.core.support.SimpleInfo;
 import com.thruzero.common.core.utils.ClassUtils;
 import com.thruzero.common.core.utils.ClassUtils.ClassUtilsException;
 import com.thruzero.common.core.utils.ExceptionUtilsExt;
 import com.thruzero.domain.dao.TextEnvelopeDAO;
 import com.thruzero.domain.model.DataStoreInfo;
 import com.thruzero.domain.model.TextEnvelope;
 import com.thruzero.domain.service.InfoNodeService;
 
 /**
 * An abstract base-class implementing the {@code InfoNodeService} interface, allowing for multiple types of
 * {@code InfoNodeService}s. <i>Note</i>: since a {@code Service} is a {@code Singleton}, there can be only one instance
 * of a concrete implementation. The {@code AbstractInfoNodeService} allows for sub-types of the {@code InfoNodeService}
 * to peacefully coexist.
  * <p>
  * An implementation of {@code AbstractInfoNodeService} need only provide a default constructor.
 *
  * @author George Norman
  */
 public abstract class AbstractInfoNodeService implements InfoNodeService, Initializable {
   private static final Logger logger = Logger.getLogger(AbstractInfoNodeService.class);
 
   private final TextEnvelopeDAO textEnvelopeDAO;
 
   private SimpleInfoNodeFilterChain infoNodeFilterChain;
 
   // ------------------------------------------------
   // InfoNodeServiceInitParamKeys
   // ------------------------------------------------
 
   /**
    * Initialization parameter keys defined for InfoNodeService.
    */
   @InitializationParameterKeysBookmark
   public interface InfoNodeServiceInitParamKeys extends InitializableParameterKeys {
     String SOURCE_SECTION = InfoNodeService.class.getName();
 
     String INFONODE_FILTER_LIST = "infoNodeFilters";
   }
 
   // ============================================================================
   // AbstractInfoNodeService
   // ============================================================================
 
   protected AbstractInfoNodeService(TextEnvelopeDAO textEnvelopeDAO) {
     this.textEnvelopeDAO = textEnvelopeDAO;
   }
 
   /**
   * @throws InitializationException if a problem is encountered with the given initParams.
    */
   @Override
   public void init(InitializationStrategy initStrategy) {
     StringMap initParams = LocatorUtils.getInheritedParameters(initStrategy, this.getClass(), AbstractInfoNodeService.class);
 
     String[] infoNodeFilters = initParams.getValueTransformer(InfoNodeServiceInitParamKeys.INFONODE_FILTER_LIST).getStringArrayValue();
     if (infoNodeFilters != null) {
       List<InfoNodeFilter> filterList = new ArrayList<InfoNodeFilter>();
 
       for (String filterClassName : infoNodeFilters) {
         try {
           Class<InfoNodeFilter> filterClass = ClassUtils.classFrom(filterClassName);
           filterList.add(ClassUtils.instanceFrom(filterClass));
         } catch (ClassUtilsException e) {
           throw ExceptionUtilsExt.logAndCreateLocatorException(logger, "Couldn't create filter named: " + filterClassName, e);
         }
       }
 
       infoNodeFilterChain = new SimpleInfoNodeFilterChain(filterList);
     }
   }
 
   /**
    * @see com.thruzero.common.core.locator.Initializable#reset()
    */
   @Override
   public void reset() {
     infoNodeFilterChain = null;
   }
 
   @Override
   public List<? extends InfoNodeElement> getInfoNodes(final ContainerPath containerPath, final boolean recursive) {
     List<InfoNodeElement> result = new ArrayList<InfoNodeElement>();
     List<? extends TextEnvelope> models = getTextEnvelopeDAO().getTextEnvelopes(containerPath, recursive);
 
     for (TextEnvelope infoNodeModel : models) {
       try {
         result.add(getSaxInfoNodeBuilder(false).buildInfoNode(infoNodeModel.getData(), infoNodeFilterChain));
       } catch (Exception e) {
         logger.error("Invalid InfoNode model.", e);
       }
     }
 
     return result;
   }
 
   @Override
   public List<EntityPath> getInfoNodePaths(ContainerPath containerPath, boolean recursive) {
     List<EntityPath> result = getTextEnvelopeDAO().getTextEnvelopePaths(containerPath, recursive);
 
     return result;
   }
 
   @Override
   public InfoNodeElement getInfoNode(final EntityPath entityPath) {
     InfoNodeElement result = null;
     TextEnvelope infoNodeModel = getTextEnvelopeDAO().getTextEnvelope(entityPath);
 
     if (infoNodeModel != null) {
       try {
         result = getSaxInfoNodeBuilder(false).buildInfoNode(infoNodeModel.getData(), infoNodeFilterChain);
       } catch (Exception e) {
         logger.error("Invalid InfoNode model.", e);
       }
     }
 
     return result;
   }
 
   @Override
   public InfoNodeElement getInfoNode(EntityPath entityPath, DataStoreInfo dataStoreInfo) {
     InfoNodeElement result;
 
     if (StringUtils.isNotEmpty(dataStoreInfo.getPrivateRootDataStorePath())) {
       entityPath = new EntityPath(dataStoreInfo.getPrivateRootDataStorePath(), entityPath.getContainerPath(), entityPath.getEntityName());
     }
     result = getInfoNode(entityPath);
 
     return result;
   }
 
   @Override
   public InfoNodeElement getFirstInfoNode(final EntityPath entityPath, final String xpathExpr) {
     InfoNodeElement result = null;
     InfoNodeElement infoNode = getInfoNode(entityPath);
 
     if (infoNode != null) {
       try {
         if (StringUtils.startsWith(xpathExpr, "//")) {
           infoNode.enableRootNode();
         }
        result = (InfoNodeElement)infoNode.find(xpathExpr);
       } catch (JDOMException e) {
         throw new RuntimeException("Exception while attempting to find node using xpathExpr: " + xpathExpr, e);
       }
     }
 
     return result;
   }
 
   @Override
   public boolean isExistingEntity(EntityPath entityPath) {
     boolean result = getTextEnvelopeDAO().isExistingTextEnvelope(entityPath);
 
     return result;
   }
 
   @Override
   public void save(InfoNodeElement infoNodeElement) {
     if (infoNodeElement.getEntityPath() == null) {
       throw new RuntimeException("Error attempting to save InfoNodeElement: The EntityPath is null. An EntityPath is required to save an InfoNodeElement.");
     }
 
     TextEnvelope domainObject = new TextEnvelope(infoNodeElement);
 
     getTextEnvelopeDAO().save(domainObject);
   }
 
   @Override
   public void saveOrUpdate(InfoNodeElement infoNodeElement) {
     if (infoNodeElement.getEntityPath() == null) {
       throw new RuntimeException("Error attempting to save or update InfoNodeElement: The EntityPath is null. An EntityPath is required to save or update an InfoNodeElement.");
     }
 
     if (isExistingEntity(infoNodeElement.getEntityPath())) {
       update(infoNodeElement);
     } else {
       save(infoNodeElement);
     }
   }
 
   @Override
   public void update(InfoNodeElement infoNodeElement) {
     if (infoNodeElement.getEntityPath() == null) {
       throw new RuntimeException("Error attempting to update InfoNodeElement: The EntityPath is null. An EntityPath is required to update an InfoNodeElement.");
     }
 
     TextEnvelope domainObject = new TextEnvelope(infoNodeElement);
 
     getTextEnvelopeDAO().update(domainObject);
   }
 
   @Override
   public void delete(InfoNodeElement infoNodeElement) {
     if (infoNodeElement.getEntityPath() == null) {
       throw new RuntimeException("Error attempting to delete InfoNodeElement: The EntityPath is null. An EntityPath is required to delete an InfoNodeElement.");
     }
 
     TextEnvelope domainObject = new TextEnvelope(infoNodeElement);
 
     getTextEnvelopeDAO().delete(domainObject);
 
   }
 
   @Override
   public String getRawData(final EntityPath entityPath) {
     String result = null;
     TextEnvelope infoNodeModel = getTextEnvelopeDAO().getTextEnvelope(entityPath);
 
     if (infoNodeModel != null) {
       result = infoNodeModel.getData(); // TODO-p0(geo) infoNodeFilterChain is ignored
     }
 
     return result;
   }
 
   @Override
   public void saveOrUpdateRawData(EntityPath entityPath, String data) {
     if (entityPath == null) {
       throw new RuntimeException("Error attempting to save or update InfoNodeElement: The EntityPath is null. An EntityPath is required to save or update an InfoNodeElement.");
     }
 
     TextEnvelope domainObject = new TextEnvelope(entityPath, data);
    domainObject.setId(entityPath);
     if (isExistingEntity(entityPath)) {
       getTextEnvelopeDAO().update(domainObject);
     } else {
       getTextEnvelopeDAO().save(domainObject);
     }
   }
 
   @Override
   public SimpleInfo getSimpleInfo() {
     return SimpleInfo.createSimpleInfo(this, textEnvelopeDAO);
   }
 
   protected TextEnvelopeDAO getTextEnvelopeDAO() {
     return textEnvelopeDAO;
   }
 
   protected static SaxInfoNodeBuilder getSaxInfoNodeBuilder(final boolean enableRootNode) {
     return enableRootNode ? SaxInfoNodeBuilder.WITH_ROOT_NODE : SaxInfoNodeBuilder.DEFAULT;
   }
 
 }
