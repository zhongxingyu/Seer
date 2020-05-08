 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.cmis.spi;
 
 import org.apache.chemistry.*;
 import org.apache.commons.lang.StringUtils;
 import com.flexive.chemistry.webdav.extensions.CopyDocumentExtension;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.structure.FxType;
 
import static com.flexive.shared.CacheAdmin.getEnvironment;

 /**
  * The flexive repository implementation.
  * 
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FlexiveRepository implements Repository {
 
     private final FlexiveRepositoryConfig config;
 
     /**
      * @param contentStreamURI  the base URI for content streams.
      */
     public FlexiveRepository(String contentStreamURI) {
         this.config = new FlexiveRepositoryConfig(contentStreamURI);
     }
 
     public Connection getConnection(Map<String, Serializable> parameters) {
         return new FlexiveConnection(config, parameters);
     }
 
     public SPI getSPI() {
         return new FlexiveConnection(config, null);
     }
 
     public <T> T getExtension(Class<T> klass) {
         if (klass == CopyDocumentExtension.class) {
             return klass.cast(CopyDocument.getInstance());
         }
         return null;
     }
 
     public RepositoryInfo getInfo() {
         return new FlexiveRepositoryInfo();
     }
 
     public Collection<Type> getTypes(String typeId) {
         final List<Type> result = Lists.newArrayList();
         if (typeId == null) {
            for (FxType fxType : getEnvironment().getTypes()) {
                 result.add(new FlexiveType(fxType));
             }
         } else {
             if (FlexiveType.ROOT_TYPE_ID.equalsIgnoreCase(typeId)) {
                 // return all types
                for (FxType type : getEnvironment().getTypes()) {
                     result.add(new FlexiveType(type));
                 }
             } else {
                 // return type + descendants
                for (FxType derived : getEnvironment().getType(typeId).getDerivedTypes(true, true)) {
                     result.add(new FlexiveType(derived));
                 }
             }
         }
         return result;
     }
 
     public Type getType(String typeId) {
         return typeId != null ? new FlexiveType(typeId) : new FlexiveType(FxType.ROOT_ID);
     }
 
     public String getId() {
         return getInfo().getId();
     }
 
     public String getName() {
         return getInfo().getName();
     }
 
     public URI getURI() {
         // TODO: what to return? The URI depends on the frontend server (e.g. AtomPub)
         return null;
     }
 
     public String getRelationshipName() {
         return null;
     }
 
     public URI getThinClientURI() {
         return null;
     }
 
     public void addType(Type type) {
         throw new UnsupportedOperationException();
     }
 
     public Collection<Type> getTypes(String typeId, int depth, boolean returnPropertyDefinitions) {
         // TODO
         return getTypes(typeId);
     }
 
     /**
      * Extension to provide efficient copying of contents.
      */
     private static class CopyDocument implements CopyDocumentExtension {
         private static final CopyDocument INSTANCE = new CopyDocument();
 
         private CopyDocument() {
         }
 
         private static CopyDocument getInstance() {
             return INSTANCE;
         }
 
         public void copy(Connection conn, ObjectId id, ObjectId targetFolder, String newName, boolean overwrite, boolean shallow) {
             final CMISObject object = conn.getObject(id);
             if (!(object instanceof FlexiveObjectEntry)) {
                 throw new IllegalArgumentException("Cannot copy object of type " + object.getClass());
             }
             if (StringUtils.isBlank(newName)) {
                 throw new IllegalArgumentException("New resource name must not be null.");
             }
 
             final FlexiveObjectEntry doc = (FlexiveObjectEntry) object;
             final FlexiveFolder target = (FlexiveFolder) conn.getObject(targetFolder);
 
             // remove existing entry when overwrite is set
             if (overwrite) {
                 for (CMISObject child : target.getChildren()) {
                     if (newName.equals(child.getName())) {
                         // remove old object
                         if (child instanceof Document) {
                             ((Document) child).deleteAllVersions();
                         } else {
                             child.delete();
                         }
                     }
                 }
             }
 
             if (doc instanceof FlexiveFolder) {
                 // perform efficient copy through the tree engine
                 ((FlexiveFolder) doc).copyTo((FlexiveFolder) conn.getObject(targetFolder), newName, !shallow);
             } else {
                 // link object to new folder
                 final FlexiveObjectEntry copy = ((FlexiveDocument) doc).copy();
                 copy.setName(newName);
                 target.add(copy);
                 copy.save();
                 target.save();
             }
         }
     }
 }
