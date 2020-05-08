 /*
  * Copyright 2008 buschmais GbR
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
 package com.buschmais.maexo.mbeans.osgi.core;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.management.openmbean.CompositeType;
 import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
 import javax.management.openmbean.OpenMBeanOperationInfoSupport;
 import javax.management.openmbean.OpenMBeanParameterInfo;
 import javax.management.openmbean.OpenMBeanParameterInfoSupport;
 import javax.management.openmbean.OpenType;
 import javax.management.openmbean.SimpleType;
 import javax.management.openmbean.TabularType;
 
 import com.buschmais.maexo.framework.commons.mbean.dynamic.OpenTypeFactory;
 
 /**
  * Class holding all constants for BundleMBeans.
  */
 public final class BundleMBeanConstants {
 
 	/** Constant "name". */
 	public static final String HEADER_ITEM_NAME = "name";
 
 	/** Constant "value". */
 	public static final String HEADER_ITEM_VALUE = "value";
 
 	/** Short description of this MBean. */
 	public static final String MBEAN_DESCRIPTION = "Bundle MBean";
 
 	/** MBean object name format. */
 	public static final String OBJECTNAME_FORMAT = "com.buschmais.maexo:type=Bundle,name=%s,version=%s";
 
 	/**
 	 * Header properties ({@link HEADER_ITEM_NAME}, {@link HEADER_ITEM_VALUE}).
 	 */
 	public static final List<String> HEADER_ITEMS = Arrays.asList(new String[] {
 			HEADER_ITEM_NAME, HEADER_ITEM_VALUE });
 
 	/** Composite type representing one header entry. */
 	public static final CompositeType HEADER_TYPE = OpenTypeFactory
 			.createCompositeType("headerEntry", "bundle header entry",
 					HEADER_ITEMS.toArray(new String[0]), HEADER_ITEMS
 							.toArray(new String[0]), new OpenType[] {
 							SimpleType.STRING, SimpleType.STRING });
 
 	/**
 	 * Tabular type containing header entries as composite type.
 	 */
 	public static final TabularType HEADERS_TYPE = OpenTypeFactory
 			.createTabularType(
 					"headers",
 					"TabularType representing a bundle's Manifest headers and values.",
 					HEADER_TYPE, new String[] { "name" });
 
 	/** MBean attribute info for {@link BundleMBean#getBundleId()}. */
 	public static final OpenMBeanAttributeInfoSupport ID = new OpenMBeanAttributeInfoSupport(
 			"bundleId", "The unique identifier of this bundle.",
 			SimpleType.INTEGER, true, false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getState()}. */
 	public static final OpenMBeanAttributeInfoSupport STATE = new OpenMBeanAttributeInfoSupport(
 			"state",
 			"An element of UNINSTALLED,INSTALLED,RESOLVED,STARTING,STOPPING,ACTIVE.",
 			SimpleType.INTEGER, true, false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getHeaders()}. */
	public static final OpenMBeanAttributeInfoSupport HEADERS = new OpenMBeanAttributeInfoSupport(
 			"headers",
 			"A TabularData object containing this bundle's Manifest headers and values.",
 			HEADERS_TYPE, true, false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getLastModified()}. */
 	public static final OpenMBeanAttributeInfoSupport LASTMODIFIED = new OpenMBeanAttributeInfoSupport(
 			"lastModified", "The time when this bundle was last modified.",
 			SimpleType.LONG, true, false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getLastModifiedAsDate()}. */
 	public static final OpenMBeanAttributeInfoSupport LASTMODIFIEDASDATE = new OpenMBeanAttributeInfoSupport(
 			"lastModifiedAsDate",
 			"The time when this bundle was last modified.", SimpleType.DATE,
 			true, false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getLocation()}. */
 	public static final OpenMBeanAttributeInfoSupport LOCATION = new OpenMBeanAttributeInfoSupport(
 			"location",
 			"The string representation of this bundle's location identifier.",
 			SimpleType.STRING, true, false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getRegisteredServices()}. */
 	public static final OpenMBeanAttributeInfoSupport REGISTEREDSERVICES = new OpenMBeanAttributeInfoSupport(
 			"registeredServices",
 			"This bundle's ObjectName list for all services it has registered or null if this bundle has no registered services.",
 			OpenTypeFactory.createArrayType(1, SimpleType.OBJECTNAME), true,
 			false, false);
 
 	/** MBean attribute info for {@link BundleMBean#getServicesInUse()}. */
 	public static final OpenMBeanAttributeInfoSupport SERVICESINUSE = new OpenMBeanAttributeInfoSupport(
 			"servicesInUse",
 			"This bundle's ObjectName list for all services it is using or returns null if this bundle is not using any services.",
 			OpenTypeFactory.createArrayType(1, SimpleType.OBJECTNAME), true,
 			false, false);
 
	/**
	 * Enum type for bundle states for MBean attribute
	 * {@link BundleMBean#getStateAsName()}
	 */
 	public enum State {
 		UNINSTALLED, INSTALLED, RESOLVED, STARTING, STOPPING, ACTIVE, UNKNOWN
 	}
 
 	/** MBean attribute info for {@link BundleMBean#getStateAsName()}. */
	public static final OpenMBeanAttributeInfoSupport STATEASNAME = new OpenMBeanAttributeInfoSupport(
 			"stateAsName",
 			"An element of UNINSTALLED,INSTALLED,RESOLVED,STARTING,STOPPING,ACTIVE,UNKNOWN.",
 			SimpleType.STRING, true, false, false);
 
 	/** MBean operation info for operation {@link BundleMBean#start()}. */
 	public static final OpenMBeanOperationInfoSupport START = new OpenMBeanOperationInfoSupport(
 			"start", "Start the bundle",
 			new OpenMBeanParameterInfoSupport[] {}, SimpleType.VOID,
 			OpenMBeanOperationInfoSupport.ACTION_INFO);
 
 	/** MBean operation info for operation {@link BundleMBean#stop()}. */
 	public static final OpenMBeanOperationInfoSupport STOP = new OpenMBeanOperationInfoSupport(
 			"stop", "Stop the bundle", new OpenMBeanParameterInfoSupport[] {},
 			SimpleType.VOID, OpenMBeanOperationInfoSupport.ACTION_INFO);
 
 	/** MBean operation info for operation {@link BundleMBean#update()}. */
 	public static final OpenMBeanOperationInfoSupport UPDATE = new OpenMBeanOperationInfoSupport(
 			"update", "Update the bundle",
 			new OpenMBeanParameterInfoSupport[] {}, SimpleType.VOID,
 			OpenMBeanOperationInfoSupport.ACTION_INFO);
 
 	/** MBean operation info for operation {@link BundleMBean#uninstall()}. */
 	public static final OpenMBeanOperationInfoSupport UNINSTALL = new OpenMBeanOperationInfoSupport(
 			"uninstall", "Uninstall the bundle",
 			new OpenMBeanParameterInfoSupport[] {}, SimpleType.VOID,
 			OpenMBeanOperationInfoSupport.ACTION_INFO);
 
 	/** MBean operation info for operation {@link BundleMBean#update(String)}. */
 	public static final OpenMBeanOperationInfoSupport UPDATEFROMURL = new OpenMBeanOperationInfoSupport(
 			"update", "Update the bundle from the given url",
 			new OpenMBeanParameterInfo[] { new OpenMBeanParameterInfoSupport(
 					"url", "URL", SimpleType.STRING) }, SimpleType.VOID,
 			OpenMBeanOperationInfoSupport.ACTION_INFO);
 
 	/** MBean operation info for operation {@link BundleMBean#update(byte[])}. */
 	public static final OpenMBeanOperationInfoSupport UPDATEFROMBYTEARRAY = new OpenMBeanOperationInfoSupport(
 			"update", "Update the bundle from a byte array.",
 			new OpenMBeanParameterInfo[] { new OpenMBeanParameterInfoSupport(
 					"in", "BYTEARRAY", OpenTypeFactory.createArrayType(1,
 							SimpleType.BYTE)) }, SimpleType.VOID,
 			OpenMBeanOperationInfoSupport.ACTION_INFO);
 
 	/**
 	 * Private Constructor.
 	 */
 	private BundleMBeanConstants() {
 
 	}
 }
