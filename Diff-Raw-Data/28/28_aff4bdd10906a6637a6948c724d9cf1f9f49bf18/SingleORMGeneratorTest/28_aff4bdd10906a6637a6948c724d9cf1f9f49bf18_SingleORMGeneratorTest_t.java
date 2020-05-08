 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: SingleORMGeneratorTest.java,v 1.5 2011/08/25 12:42:40 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.orm.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.texo.modelgenerator.test.models.TestModel;
 
 /**
  * Generates the ORM for a single ecore model.
  * 
  * @see TestModel
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.5 $
  */
 public class SingleORMGeneratorTest extends ORMGeneratorTest {
 
   @Override
   protected List<String> getModelFileRelativePaths() {
     final List<String> modelFiles = new ArrayList<String>();
    modelFiles.add("issues/bz369931.ecore"); //$NON-NLS-1$
    //    modelFiles.add("samples/emap.ecore"); //$NON-NLS-1$
     //        modelFiles.add("base/identifiable.ecore"); //$NON-NLS-1$
     return modelFiles;
   }
 
   @Override
   protected boolean useSharedEPackageRegistry() {
     return true;
   }
 
 }
