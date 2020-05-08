 /**
 - * Copyright (C) 2009 STMicroelectronics
  *
  * This file is part of "Mind Compiler" is free software: you can redistribute
  * it and/or modify it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact: mind-members@lists.minalogic.net
  *
  * Authors: Matthieu Leclercq
  * Contributors:
  */
 
 package org.ow2.mind.doc.idl;
 
 import org.ow2.mind.annotation.AnnotationChainFactory;
 import org.ow2.mind.doc.idl.parser.IDLFileLoader;
 import org.ow2.mind.idl.BasicIDLLocator;
 import org.ow2.mind.idl.BasicIncludeResolver;
 import org.ow2.mind.idl.BasicInterfaceReferenceResolver;
 import org.ow2.mind.idl.CacheIDLLoader;
 import org.ow2.mind.idl.CachingIncludeResolver;
 import org.ow2.mind.idl.ExtendsInterfaceLoader;
 import org.ow2.mind.idl.IDLLoader;
 import org.ow2.mind.idl.IDLLocator;
 import org.ow2.mind.idl.IDLTypeCheckerLoader;
 import org.ow2.mind.idl.IncludeHeaderResolver;
 import org.ow2.mind.idl.IncludeLoader;
 import org.ow2.mind.idl.IncludeResolver;
 import org.ow2.mind.idl.InputResourcesIncludeResolver;
 import org.ow2.mind.idl.InputResourcesInterfaceReferenceResolver;
 import org.ow2.mind.idl.InterfaceReferenceResolver;
 import org.ow2.mind.idl.KindDecorationLoader;
 import org.ow2.mind.idl.RecursiveIDLLoader;
 import org.ow2.mind.idl.RecursiveIDLLoaderImpl;
 import org.ow2.mind.idl.ReferencedInterfaceResolver;
 import org.ow2.mind.idl.annotation.AnnotationLoader;
 import org.ow2.mind.idl.annotation.AnnotationProcessorLoader;
 import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.XMLSTNodeFactoryImpl;
 
 
 public final class IDLLoaderChainFactory {
 
   private IDLLoaderChainFactory() {
   }
 
   public static IDLLocator newLocator() {
     IDLLocator idlLocator;
     final BasicIDLLocator bil = new BasicIDLLocator();
     idlLocator = bil;
 
     return idlLocator;
   }
 
   public static IDLLoader newLoader() {
     return newLoader(newLocator());
   }
 
   public static IDLLoader newLoader(final IDLLocator idlLocator) {
 
     // Loader chain components
     IDLLoader idlLoader;
     final IDLFileLoader ifl = new IDLFileLoader();
     final AnnotationLoader al = new AnnotationLoader();
     final AnnotationProcessorLoader apl1 = new AnnotationProcessorLoader();
     final IncludeLoader uil = new IncludeLoader();
     final ExtendsInterfaceLoader eil = new ExtendsInterfaceLoader();
     final IDLTypeCheckerLoader tcl = new IDLTypeCheckerLoader();
     final KindDecorationLoader kdl = new KindDecorationLoader();
     final AnnotationProcessorLoader apl2 = new AnnotationProcessorLoader();
     final CacheIDLLoader cil = new CacheIDLLoader();
 
     idlLoader = cil;
     cil.clientIDLLoaderItf = apl2;
     apl2.clientIDLLoaderItf = kdl;
     kdl.clientIDLLoaderItf = tcl;
     tcl.clientIDLLoaderItf = uil;
     uil.clientIDLLoaderItf = eil;
     eil.clientIDLLoaderItf = apl1;
     apl1.clientIDLLoaderItf = al;
     al.clientIDLLoaderItf = ifl;
 
     apl1.setPhase(IDLLoaderPhase.AFTER_PARSING.name());
     apl2.setPhase(IDLLoaderPhase.AFTER_CHECKING.name());
 
     al.annotationCheckerItf = AnnotationChainFactory.newAnnotationChecker();
 
     // Recursive IDL Loader
     RecursiveIDLLoader recursiveIDLLoader;
     final RecursiveIDLLoaderImpl ril = new RecursiveIDLLoaderImpl();
     ril.clientIDLLoaderItf = idlLoader;
     recursiveIDLLoader = ril;
 
     // IncludeResolver sub-chain
     IncludeResolver includeResolver;
     final BasicIncludeResolver bir = new BasicIncludeResolver();
     final IncludeHeaderResolver ihr = new IncludeHeaderResolver();
     final InputResourcesIncludeResolver irir = new InputResourcesIncludeResolver();
     final CachingIncludeResolver cir = new CachingIncludeResolver();
 
     includeResolver = cir;
     cir.clientResolverItf = irir;
     irir.clientResolverItf = ihr;
     ihr.clientResolverItf = bir;
 
     bir.recursiveIdlLoaderItf = recursiveIDLLoader;
     bir.idlLocatorItf = idlLocator;
     cir.idlLoaderItf = idlLoader;
 
     uil.idlResolverItf = includeResolver;
 
     // Interface Reference Resolver
     InterfaceReferenceResolver interfaceReferenceResolver;
     final BasicInterfaceReferenceResolver birr = new BasicInterfaceReferenceResolver();
     final InputResourcesInterfaceReferenceResolver irirr = new InputResourcesInterfaceReferenceResolver();
     final ReferencedInterfaceResolver rir = new ReferencedInterfaceResolver();
 
     interfaceReferenceResolver = rir;
     rir.clientResolverItf = irirr;
     irirr.clientResolverItf = birr;
     birr.recursiveIdlLoaderItf = recursiveIDLLoader;
 
     eil.interfaceReferenceResolverItf = interfaceReferenceResolver;
     tcl.interfaceReferenceResolverItf = interfaceReferenceResolver;
 
     ifl.idlLocatorItf = idlLocator;
 
     // node factories
    final XMLSTNodeFactoryImpl xnf = new XMLSTNodeFactoryImpl();
    final STNodeFactoryImpl nf = new STNodeFactoryImpl();
     ifl.nodeFactoryItf = xnf;
     ihr.nodeFactoryItf = nf;
 
     return idlLoader;
   }
 }
