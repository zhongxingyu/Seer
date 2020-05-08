 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.tld.model;
 
 import org.jboss.tools.common.model.*;
 import org.jboss.tools.common.model.filesystems.XFileObject;
 
 public class FileSystemsTreeConstraint implements XFilteredTreeConstraint {
 
     public FileSystemsTreeConstraint() {}
 
     public void update(XModel model) {}
 
     public boolean accepts(XModelObject object) {
         return true;
     }
 
     public boolean isHidingAllChildren(XModelObject object) {
         if(object.getFileType() != XFileObject.FILE) return false;
         String entity = object.getModelEntity().getName();
         if(!entity.startsWith("File")) return false; //$NON-NLS-1$
         if(!entity.startsWith("FileTLD") && //$NON-NLS-1$
            !entity.startsWith("FileValidationRules") && //$NON-NLS-1$
            !entity.startsWith("FileFaceletTaglib") //$NON-NLS-1$
 ///        && !entity.equals("FileTiles")
         ) return false;
        return (!"true".equals(object.getAttributeValue("expanded"))); //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     public boolean isHidingSomeChildren(XModelObject object) {
         return false;
     }
 
 }
