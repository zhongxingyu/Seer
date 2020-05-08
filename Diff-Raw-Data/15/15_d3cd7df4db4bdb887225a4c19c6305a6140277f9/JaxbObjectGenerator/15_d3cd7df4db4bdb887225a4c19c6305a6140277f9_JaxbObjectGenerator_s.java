 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.rest.generator;
 
 import com.cedarsoft.codegen.AbstractGenerator;
 import com.cedarsoft.codegen.CodeGenerator;
 import com.cedarsoft.codegen.DecisionCallback;
 import com.cedarsoft.codegen.GeneratorCliSupport;
 import com.cedarsoft.codegen.GeneratorConfiguration;
 import com.cedarsoft.codegen.model.DomainObjectDescriptor;
 import com.cedarsoft.codegen.model.DomainObjectDescriptorFactory;
 import com.cedarsoft.codegen.model.FieldTypeInformation;
 import com.cedarsoft.codegen.parser.Parser;
 import com.cedarsoft.codegen.parser.Result;
 import com.cedarsoft.io.WriterOutputStream;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.PrintStream;
 
 /**
  * Generates a Jaxb object corresponding to a domain object
  */
 public class JaxbObjectGenerator extends AbstractGenerator {
   public static void main( String[] args ) throws Exception {
     GeneratorCliSupport cliSupport = new GeneratorCliSupport( new JaxbObjectGenerator(), "jaxbgen" );
     cliSupport.run( args );
   }
 
   @NotNull
   @Override
   protected String getRunnerClassName() {
     return "com.cedarsoft.rest.generator.JaxbObjectGenerator$MyRunner";
   }
 
   public static class MyRunner implements Runner {
     @Override
     public void generate( @NotNull GeneratorConfiguration configuration ) throws Exception {
       Result result = Parser.parse( configuration.getClasspath(), configuration.getDomainSourceFiles() );
 
      DomainObjectDescriptor descriptor = new DomainObjectDescriptorFactory( result.getClassDeclaration() ).create();

       PrintStream statusPrinter = new PrintStream( new WriterOutputStream( configuration.getLogOut() ) );
 
       //Create the source files
       if ( configuration.getCreationMode().isCreate() ) {
         configuration.getLogOut().append( "Generating JAXB classes...\n" );
 
         CodeGenerator<StubDecisionCallback> codeGenerator = new CodeGenerator<StubDecisionCallback>( new StubDecisionCallback() );
         new Generator( codeGenerator, descriptor ).generate();
         codeGenerator.getModel().build( configuration.getDestination(), configuration.getResourcesDestination(), statusPrinter );
       }
 
       //Generate the tests
       if ( configuration.getCreationMode().isCreateTests() ) {
         configuration.getLogOut().append( "Generating tests...\n" );
 
         CodeGenerator<StubDecisionCallback> codeGenerator = new CodeGenerator<StubDecisionCallback>( new StubDecisionCallback() );
         new TestGenerator( codeGenerator, descriptor ).generateTest();
         codeGenerator.getModel().build( configuration.getTestDestination(), configuration.getTestResourcesDestination(), statusPrinter );
       }
     }
   }
 
   public static class StubDecisionCallback implements DecisionCallback {
     public boolean shallAddFieldStatement( @NotNull com.cedarsoft.rest.generator.AbstractGenerator<?> generator, @NotNull FieldTypeInformation fieldInfo, @NotNull Generator.Scope type ) {
       boolean probablyOwnType = generator.isProbablyOwnType( fieldInfo.getType() );
       boolean isCollectionType = fieldInfo.isCollectionType();
 
       switch ( type ) {
         case COMMON:
           return !probablyOwnType && !isCollectionType;
         case JAXB:
           return probablyOwnType || isCollectionType;
         case STUB:
           return probablyOwnType && !isCollectionType;
       }
 
       throw new UnsupportedOperationException( "Invalid type " + type );
     }
   }
 }
