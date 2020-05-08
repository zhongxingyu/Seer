 /**
  * Copyright (c) 2008 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.rfcgen.wizard;
 
 import java.io.ByteArrayInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 
 import com.sap.conn.jco.JCoDestination;
 import com.sap.conn.jco.JCoException;
 import com.sap.conn.jco.JCoFunctionTemplate;
 import com.sap.conn.jco.JCoListMetaData;
 import com.sap.conn.jco.JCoRecordMetaData;
 import com.sap.conn.jco.JCoRepository;
 
 /**
  * The actual job that creates the mapping.
  * @author vwegert
  *
  */
 public class CreateMappingRunnable implements IRunnableWithProgress {
 
 	private class StructureFieldInfo {
 		public String fieldName;
 		public String fieldType;
 		public String attributeName;
 		public String comment;
 	}
 
 	private class StructureInfo {
 		public String structureName;
 		public String className;
 		public List<StructureFieldInfo> fields = new Vector<StructureFieldInfo>();
 	}
 
 	private enum FunctionModuleParameterType {
 		IMPORTING, EXPORTING, CHANGING, TABLES;
 	}
 
 	private class FunctionModuleParameter {
 		public FunctionModuleParameterType parameterType;
 		public String parameterName;
 		public String javaType;
 		public String attributeName;
 		public String comment;
 		public boolean isStructure;
 		public boolean isTable;
 	}
 
 	private class FunctionModuleInfo {
 		public String functionModuleName;
 		public String callClassName;
 		public String requestClassName;
 		public String responseClassName;
 		public List<FunctionModuleParameter> parameters = new Vector<FunctionModuleParameter>();
 	}
 
 
 	private JCoRepository repository;
 
 	private Collection<JCoFunctionTemplate> functionModules;
 	private String packageName;
 	private boolean useCallStyleMapping;
 	private IPath filePath;
 	private String fileName;
 
 	private Map<String, StructureInfo> structures = new TreeMap<String, StructureInfo>();
 	private Map<String, FunctionModuleInfo> functions = new TreeMap<String, FunctionModuleInfo>();
 	private StringBuilder source;
 
 	/**
 	 * Default constructor.
 	 * @param destination 
 	 * @param functionModules
 	 * @param packageName
 	 * @param useCallStyleMapping
 	 * @param filePath
 	 * @param fileName
 	 * @throws JCoException 
 	 */
 	public CreateMappingRunnable(JCoDestination destination, Collection<JCoFunctionTemplate> functionModules,
 			String packageName, boolean useCallStyleMapping, IPath filePath, String fileName) throws JCoException {
 		this.repository = destination.getRepository();
 		this.functionModules = functionModules;
 		this.packageName = packageName;
 		this.useCallStyleMapping = useCallStyleMapping;
 		this.filePath = filePath;
 		this.fileName = fileName;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		try {
 			monitor.beginTask("Generating RFC Mapping", 30);
 			structures.clear();
 			functions.clear();
 
 			monitor.subTask("Creating a list of modules and data structures...");
 			readMetaData();
 			monitor.worked(10);
 			if (monitor.isCanceled()) {
 				throw new InterruptedException();
 			}
 
 			monitor.subTask("Generating the source code...");
 			generateMapping();
 			monitor.worked(10);
 			if (monitor.isCanceled()) {
 				throw new InterruptedException();
 			}
 
 			monitor.subTask("Writing mapping file...");
 			writeMapping(monitor);
 			if (monitor.isCanceled()) {
 				throw new InterruptedException();
 			}
 
 		} catch (JCoException e) {
 			throw new InvocationTargetException(e);
 		} catch (CoreException e) {
 			throw new InvocationTargetException(e);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/**
 	 * Read the metadata of all structures and function modules to be created.
 	 * @throws JCoException
 	 */
 	private void readMetaData() throws JCoException {
 		for (JCoFunctionTemplate function: functionModules) {
 			JCoListMetaData iface = function.getFunctionInterface();
 			for (int pos = 0; pos < iface.getFieldCount(); pos++) {
 				if (iface.isStructure(pos)) {
 					addStructure(repository.getStructureDefinition(iface.getRecordTypeName(pos)));
 				} else if (iface.isTable(pos)) {
 					addStructure(iface.getRecordMetaData(pos));
 				}
 			}
 			addFunctionModule(function, iface);
 		}
 	}
 
 	/**
 	 * Adds a structure to the list of structures to generate.
 	 * @param struct
 	 */
 	private void addStructure(JCoRecordMetaData struct) {
 		if (!structures.containsKey(struct.getName())) {
 			StructureInfo info = new StructureInfo();
 			info.structureName = struct.getName();
 			info.className = struct.getName();
 			for (int pos = 0; pos < struct.getFieldCount(); pos++) {
 				StructureFieldInfo field = new StructureFieldInfo();
 				field.fieldName = struct.getName(pos);
 				field.fieldType = removeStandardPackages(struct.getClassNameOfField(pos));
 				field.attributeName = struct.getName(pos);
				field.comment = struct.getDescription(pos).replaceAll("\"", "'");
 				info.fields.add(field);
 			}
 			structures.put(struct.getName(), info);
 		}
 	}
 
 	/**
 	 * Adds a function module to the list of structures to generate.
 	 * @param function
 	 * @param iface
 	 */
 	private void addFunctionModule(JCoFunctionTemplate function, JCoListMetaData iface) {
 		if (!functions.containsKey(function.getName())) {
 			FunctionModuleInfo info = new FunctionModuleInfo();
 			info.functionModuleName = function.getName();
 			if (useCallStyleMapping) {
 				info.callClassName = function.getName() + "Call";
 			} else {
 				info.requestClassName = function.getName() + "Request";
 				info.responseClassName = function.getName() + "Response";
 			}
 
 			for (int pos = 0; pos < iface.getFieldCount(); pos++) {
 				FunctionModuleParameterType paramType = null;
 				if (iface.isImport(pos)) {
 					paramType = FunctionModuleParameterType.IMPORTING;
 				} else if (iface.isExport(pos)) {
 					paramType = FunctionModuleParameterType.EXPORTING;
 				} else if (iface.isChanging(pos)) {
 					paramType = FunctionModuleParameterType.CHANGING;
 				} else if (iface.isTable(pos)) {
 					paramType = FunctionModuleParameterType.TABLES;
 				} 
 
 				if (paramType != null) {					
 					FunctionModuleParameter param = new FunctionModuleParameter();
 					param.parameterType = paramType;
 					param.parameterName = iface.getName(pos);
 					param.attributeName = iface.getName(pos);
					param.comment = iface.getDescription(pos).replaceAll("\"", "'");
 					param.isStructure = iface.isStructure(pos);
 					param.isTable = iface.isTable(pos);
 					if (param.isStructure || param.isTable) {
 						param.javaType = structures.get(iface.getRecordTypeName(pos)).className;
 					} else {
 						param.javaType = removeStandardPackages(iface.getClassNameOfField(pos));
 					}
 					info.parameters.add(param);
 				}
 			}
 			functions.put(function.getName(), info);
 		}
 	}
 
 	/**
 	 * @param className
 	 * @return the name, stripped of the standard package names
 	 */
 	private String removeStandardPackages(String className) {
 		return className.replace("java.lang.", "").replace("java.util.", "");
 	}
 
 	/**
 	 * @param input
 	 * @param length
 	 * @param addQuotes
 	 * @return the input string padded to length with spaces
 	 */
 	private String padString(String input, int length, boolean addQuotes) {
 		StringBuilder tmp = new StringBuilder(length);
 		tmp.append(input);
 		int len = length;
 		if (addQuotes) {
 			tmp.insert(0, '"');
 			tmp.append('"');
 			len += 2;
 		}
 		while (tmp.length() < len) {
 			tmp.append(' ');
 		}
 		return tmp.toString();
 	}
 	
 	/**
 	 * Generates the mapping source code.
 	 */
 	private void generateMapping() {
 		source = new StringBuilder();
 
 		source.append(MessageFormat.format("package \"{0}\"\n\n", packageName));
 
 		// add the structures
 		for (StructureInfo info: structures.values()) {
 			source.append(MessageFormat.format("structure \"{0}\" '{'\n", info.structureName));
 			source.append(MessageFormat.format("\tclass {0} '{'\n", info.className));
 			int nameLength = 0;
 			int typeLength = 0;
 			int attrLength = 0;
 			for (StructureFieldInfo field: info.fields) {
 				nameLength = Math.max(nameLength, field.fieldName.length());
 				typeLength = Math.max(typeLength, field.fieldType.length());
 				attrLength = Math.max(attrLength, field.attributeName.length());
 			}
 			for (StructureFieldInfo field: info.fields) {
 				source.append(MessageFormat.format("\t\tfield {0} = {1} {2} comment \"{3}\";\n",
 						padString(field.fieldName, nameLength, true), 
 						padString(field.fieldType, typeLength, false), 
 						padString(field.attributeName, attrLength, false), 
 						field.comment));
 			}
 			source.append("\t}\n");
 			source.append("}\n\n");
 		}
 
 		// add the function modules
 		for (FunctionModuleInfo info: functions.values()) {
 			source.append(MessageFormat.format("function module \"{0}\" '{'\n", info.functionModuleName));
 			if (useCallStyleMapping) {
 				source.append(MessageFormat.format("\tclass {0} '{'\n", info.callClassName));
 				appendParameters(info.parameters, FunctionModuleParameterType.IMPORTING);
 				appendParameters(info.parameters, FunctionModuleParameterType.EXPORTING);
 				appendParameters(info.parameters, FunctionModuleParameterType.CHANGING);
 				appendParameters(info.parameters, FunctionModuleParameterType.TABLES);
 				source.append("\t}\n");				
 			} else {
 				source.append(MessageFormat.format("\trequest class {0} '{'\n", info.requestClassName));
 				appendParameters(info.parameters, FunctionModuleParameterType.IMPORTING);
 				appendParameters(info.parameters, FunctionModuleParameterType.CHANGING);
 				appendParameters(info.parameters, FunctionModuleParameterType.TABLES);
 				source.append("\t}\n");				
 				source.append(MessageFormat.format("\tresponse class {0} '{'\n", info.responseClassName));
 				appendParameters(info.parameters, FunctionModuleParameterType.EXPORTING);
 				appendParameters(info.parameters, FunctionModuleParameterType.CHANGING);
 				appendParameters(info.parameters, FunctionModuleParameterType.TABLES);
 				source.append("\t}\n");								
 			}
 			source.append("}\n\n");
 		}
 	}
 
 	/**
 	 * Appends all parameters of the specified type.
 	 * @param parameters
 	 * @param type
 	 */
 	private void appendParameters(List<FunctionModuleParameter> parameters,	FunctionModuleParameterType type) {
 		int nameLength = 0;
 		int typeLength = 0;
 		int attrLength = 0;
 		for (final FunctionModuleParameter param: parameters) {
 			nameLength = Math.max(nameLength, param.parameterName.length());
 			typeLength = Math.max(typeLength, param.javaType.length());
 			attrLength = Math.max(attrLength, param.attributeName.length());
 		}
 		for (final FunctionModuleParameter param: parameters) {
 			if (param.parameterType == type) {
 				switch(param.parameterType) {
 				case IMPORTING:
 					source.append(MessageFormat.format("\t\timport field {0} = {1} {2} comment \"{3}\";\n",
 							padString(param.parameterName, nameLength, true), 
 							padString(param.javaType, typeLength, false), 
 							padString(param.attributeName, attrLength, false), 
 							param.comment));
 					break;
 				case EXPORTING:
 					source.append(MessageFormat.format("\t\texport field {0} = {1} {2} comment \"{3}\";\n",
 							padString(param.parameterName, nameLength, true), 
 							padString(param.javaType, typeLength, false), 
 							padString(param.attributeName, attrLength, false), 
 							param.comment));
 					break;
 				case CHANGING:
 					source.append(MessageFormat.format("\t\tchange field {0} = {1} {2} comment \"{3}\";\n",
 							padString(param.parameterName, nameLength, true), 
 							padString(param.javaType, typeLength, false), 
 							padString(param.attributeName, attrLength, false), 
 							param.comment));
 					break;
 				case TABLES:                        
 					source.append(MessageFormat.format("\t\ttable        {0} = {1} {2} comment \"{3}\";\n",
 							padString(param.parameterName, nameLength, true), 
 							padString(param.javaType, typeLength, false), 
 							padString(param.attributeName, attrLength, false), 
 							param.comment));
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Writes the mapping source code to the file specified. 
 	 * @param monitor 
 	 * @throws CoreException 
 	 */
 	private void writeMapping(IProgressMonitor monitor) throws CoreException {
 		IPath path = filePath.append(fileName);
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 		file.create(new ByteArrayInputStream(source.toString().getBytes()), true, new SubProgressMonitor(monitor, 10));
 	}
 
 }
