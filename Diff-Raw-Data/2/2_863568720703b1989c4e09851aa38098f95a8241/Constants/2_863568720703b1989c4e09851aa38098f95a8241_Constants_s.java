 /*
  * Copyright 2008-2009 buschmais GbR
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 package com.buschmais.maexo.test;
 
 public class Constants {
 
 	public static final String MAEXO_VERSION = "1.1.0-SNAPSHOT";
	public static final String SLF4J_VERSION = "1.5.6";
 	public static final String SPRING_VERSION = "2.5.6.A";
 	public static final String SPRING_OSGI_VERSION = "1.2.0";
 	public static final String FELIX_CONFIGADMIN_VERSION = "1.0.10";
 	public static final String OSGI_COMPENDIUM_VERSION = "1.2.0";
 
 	public static final String ARTIFACT_SWITCHBOARD = "com.buschmais.maexo.modules.framework, maexo-framework.switchboard, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_COMMONS_MBEAN = "com.buschmais.maexo.modules.framework, maexo-framework.commons.mbean, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_TESTBUNDLE = "com.buschmais.maexo.test, maexo-test.testbundle, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_OSGI_CORE_MBEAN = "com.buschmais.maexo.modules.mbeans, maexo-mbeans.osgi.core, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_OSGI_COMPENDIUM_MBEAN = "com.buschmais.maexo.modules.mbeans, maexo-mbeans.osgi.compendium, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_MBEAN_SERVER_FACTORY = "com.buschmais.maexo.modules.server, maexo-server.factory, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_PLATFORM_MBEAN_SERVER = "com.buschmais.maexo.modules.server, maexo-server.platform, "
 			+ MAEXO_VERSION;
 
 	public static final String ARTIFACT_AOPALLIANCE = "org.aopalliance, com.springsource.org.aopalliance, 1.0.0";
 
 	public static final String ARTIFACT_ASM = "org.objectweb.asm, com.springsource.org.objectweb.asm, 2.2.3";
 
 	public static final String ARTIFACT_EASYMOCK = "org.easymock, com.springsource.org.easymock, 2.3.0";
 
 	public static final String ARTIFACT_JUNIT = "org.junit, com.springsource.junit, 3.8.2";
 
 	public static final String ARTIFACT_SLF4J_API = " org.slf4j,slf4j-api, "
 			+ SLF4J_VERSION;
 
 	public static final String ARTIFACT_SLF4J_JDK14 = " org.slf4j,slf4j-jdk14, "
 			+ SLF4J_VERSION;
 
 	public static final String ARTIFACT_SLF4J_JCL = " org.slf4j,jcl-over-slf4j, "
 			+ SLF4J_VERSION;
 
 	public static final String ARTIFACT_SPRING_AOP = "org.springframework, org.springframework.aop, "
 			+ SPRING_VERSION;
 	public static final String ARTIFACT_SPRING_BEANS = "org.springframework, org.springframework.beans, "
 			+ SPRING_VERSION;
 	public static final String ARTIFACT_SPRING_CORE = "org.springframework, org.springframework.core, "
 			+ SPRING_VERSION;
 	public static final String ARTIFACT_SPRING_CONTEXT = "org.springframework, org.springframework.context, "
 			+ SPRING_VERSION;
 	public static final String ARTIFACT_SPRING_TEST = "org.springframework, org.springframework.test, "
 			+ SPRING_VERSION;
 
 	public static final String ARTIFACT_SPRING_OSGI_ANNOTATION = "org.springframework.osgi, spring-osgi-annotation, "
 			+ SPRING_OSGI_VERSION;
 	public static final String ARTIFACT_SPRING_OSGI_CORE = "org.springframework.osgi, spring-osgi-core, "
 			+ SPRING_OSGI_VERSION;
 	public static final String ARTIFACT_SPRING_OSGI_EXTENDER = "org.springframework.osgi, spring-osgi-extender, "
 			+ SPRING_OSGI_VERSION;
 	public static final String ARTIFACT_SPRING_OSGI_IO = "org.springframework.osgi, spring-osgi-io, "
 			+ SPRING_OSGI_VERSION;
 	public static final String ARTIFACT_SPRING_OSGI_TEST = "org.springframework.osgi, spring-osgi-test, "
 			+ SPRING_OSGI_VERSION;
 
 	public static final String ARTIFACT_FELIX_CONFIGADMIN = "org.apache.felix, org.apache.felix.configadmin, "
 			+ FELIX_CONFIGADMIN_VERSION;
 	public static final String ARTIFACT_OSGI_COMPENDIUM = "org.apache.felix, org.osgi.compendium, "
 			+ OSGI_COMPENDIUM_VERSION;
 
 	public static final String[] TEST_FRAMEWORK_BUNDLES_NAMES = new String[] {
 			ARTIFACT_ASM, ARTIFACT_AOPALLIANCE, ARTIFACT_JUNIT,
 			ARTIFACT_SLF4J_JDK14, ARTIFACT_SLF4J_API, ARTIFACT_SLF4J_JCL,
 			ARTIFACT_SPRING_AOP, ARTIFACT_SPRING_BEANS,
 			ARTIFACT_SPRING_CONTEXT, ARTIFACT_SPRING_CORE,
 			ARTIFACT_SPRING_TEST, ARTIFACT_SPRING_OSGI_ANNOTATION,
 			ARTIFACT_SPRING_OSGI_CORE, ARTIFACT_SPRING_OSGI_EXTENDER,
 			ARTIFACT_SPRING_OSGI_IO, ARTIFACT_SPRING_OSGI_TEST };
 }
