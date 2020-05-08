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
  * $Id: EclipseModelGeneratorSingleModelTest.java,v 1.31 2011/10/25 22:48:55 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.modelgenerator.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Runs {@link EclipseModelGeneratorTest} for a single model file.
  * 
  * @see TestModel
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.31 $
  */
 
 public class EclipseModelGeneratorSingleModelTest extends EclipseModelGeneratorTest {
 
   @Override
   public void testGenerateModels() throws Exception {
     // setGenerateTexoModels(true);
     super.testGenerateModels();
   }
 
   @Override
   protected List<String> getModelFileRelativePaths() {
     final List<String> modelFiles = new ArrayList<String>();
     //    modelFiles.add("issues/bz423760.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/extlibrary.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/library.ecore"); //$NON-NLS-1$
    //    modelFiles.add("issues/bz422811.ecore"); //$NON-NLS-1$
    modelFiles.add("samples/forum.xsd"); //$NON-NLS-1$
     //    modelFiles.add("samples/library.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/sport-society.xcore"); //$NON-NLS-1$
     //    modelFiles.add("samples/sport-competition.xcore"); //$NON-NLS-1$
     //modelFiles.add("base/identifiable-xcore.xcore"); //$NON-NLS-1$
     //    modelFiles.add("samples/sport-society.xcore"); //$NON-NLS-1$
     //    modelFiles.add("issues/bz423760.ecore"); //$NON-NLS-1$
     //    modelFiles.add("texo/TexoDataRequest.xsd"); //$NON-NLS-1$
     //    modelFiles.add("texo/TexoDataResponse.xsd"); //$NON-NLS-1$
     //    modelFiles.add("samples/graphiti.ecore"); //$NON-NLS-1$
     //    modelFiles.add("schemaconstructs/List.xsd");//$NON-NLS-1$
     //    modelFiles.add("issues/bz411874.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/forum.xsd"); //$NON-NLS-1$
     //    modelFiles.add("base/identifiable.ecore"); //$NON-NLS-1$
     //    modelFiles.add("samples/kdm.ecore"); //$NON-NLS-1$
     //    modelFiles.add("schemaconstructs/ListUnion.xsd"); //$NON-NLS-1$
     return modelFiles;
   }
 
   @Override
   protected boolean useSharedEPackageRegistry() {
     return true;
   }
 }
