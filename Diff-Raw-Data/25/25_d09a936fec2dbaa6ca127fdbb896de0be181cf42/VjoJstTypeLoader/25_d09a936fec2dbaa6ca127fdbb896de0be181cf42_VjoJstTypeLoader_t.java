 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.core.ts;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.vjet.dsf.jsgroup.bootstrap.JsLibBootstrapLoader;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.ts.IJstTypeLoader;
 import org.eclipse.vjet.dsf.jst.ts.util.JstSrcFileCollector;
 import org.eclipse.vjet.dsf.jst.ts.util.JstTypeSerializer;
 import org.eclipse.vjet.dsf.jstojava.parser.VjoParser;
 import org.eclipse.vjet.dsf.ts.event.group.AddGroupEvent;
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 
 public class VjoJstTypeLoader implements IJstTypeLoader {
 
 	protected SourceType createType(String groupName,
 			String actualGroupFolderName, String srcPath, File file)
 			throws IOException, URISyntaxException {
 
 		String typeName = getTypeName(srcPath, file);
 
 		FileInputStream stream;
 		stream = new FileInputStream(file);
 		byte[] bs = new byte[stream.available()];
 		stream.read(bs);
 		stream.close();
 
 		String absolutePath = file.getAbsolutePath();
 		absolutePath = absolutePath.replace("\\", "/");
 		URI fileURI = null;
 		if (absolutePath.contains(groupName)) {
 			fileURI = new URI(absolutePath.substring(absolutePath
 					.indexOf(groupName)));
 		} else if (absolutePath.contains(actualGroupFolderName)) {
 			fileURI = new URI(absolutePath.substring(
 					absolutePath.indexOf(actualGroupFolderName)).replace(
 					actualGroupFolderName, groupName));
 
 		} else {
 			fileURI = new URI(absolutePath);
 		}
 
 		TypeSpaceMgr.getInstance().getTypeToFileMap().put(
 				groupName + "#" + typeName, fileURI);
 
 		SourceType srcType = new SourceType(groupName, typeName,
 				new String(bs), file);
 
 		return srcType;
 	}
 
 	public List<SourceType> loadJstTypes(List<AddGroupEvent> groupList) {
 
 		List<SourceType> typeList = new ArrayList<SourceType>();
 
 		for (AddGroupEvent group : groupList) {
 			String groupName = group.getGroupName();
 			String groupPath = group.getGroupPath();
 			String actualGroupFolderName = groupPath.substring(groupPath
 					.lastIndexOf("\\") + 1);
 			List<String> srcPathList = group.getSourcePathList();
 
 			File groupFolderOrFile = getGroupSrcFolder(groupPath, null);
 			List<String> bootStrapPath = group.getBootStrapList();
 			if (bootStrapPath != null && bootStrapPath.size() != 0) {
 				String bootstrapJS = getBootStrapJs(groupFolderOrFile, bootStrapPath);
                 JsLibBootstrapLoader.load(bootstrapJS, groupName);
 			}
 
 
 
 			if (srcPathList == null || srcPathList.size() == 0) {
 				typeList.addAll(loadJstTypesFromGroup(groupName,
 						actualGroupFolderName, groupFolderOrFile,group.getSrcPathInclusionPatterns(), group.getSrcPathExclusionPatterns()));
 			} else {
 				for (String srcPath : srcPathList) {
 					File srcFolder = getGroupSrcFolder(groupPath, srcPath);
 					typeList.addAll(loadJstTypesFromGroup(groupName,
 							actualGroupFolderName, srcFolder, group.getSrcPathInclusionPatterns(), group.getSrcPathExclusionPatterns()));
 				}
 			}
 		}
 
 		return typeList;
 
 	}
 
 	private String getBootStrapJs(File groupFolder, List<String> bootStrapPath) {
 		StringBuilder bootStrapJs = new StringBuilder();
 		for(String root: bootStrapPath){
 			try {
 				bootStrapJs.append(createType("bootstrap", "", root + "bootstrap.js", new File(groupFolder, root + File.separatorChar + "bootstrap.js")).getSource());
 			} catch (IOException e) {
 				// do nothing
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return bootStrapJs.toString();
 	}
 
 	private File getGroupSrcFolder(String groupPath, String srcPath) {
 
 		if (groupPath != null) {
			
			// handle spaces or other utf-8 chars
			groupPath = decodePath(groupPath);
			
			
 			File groupFile = new File(groupPath);
 
 			if (groupFile.exists()) {
 				if (!groupFile.isDirectory() || srcPath == null) {
 					return groupFile;
 				} else {
 					File srcFolder = new File(groupFile, srcPath);
 					return srcFolder;
 				}
 			}
 		} else if (srcPath != null) {
			srcPath = decodePath(srcPath);
 			File srcFolder = new File(srcPath);
 
 			if (srcFolder.exists() && srcFolder.isDirectory()) {
 				return srcFolder;
 			}
 		}
 
 		return null;
 
 	}
 
	private String decodePath(String groupPath) {
		try {
			groupPath = URLDecoder.decode(groupPath, "utf-8");
		} catch (UnsupportedEncodingException e) {
			VjetPlugin.getDefault().error("could not decode " + groupPath, e);
		}
		return groupPath;
	}

 	private List<SourceType> loadJstTypesFromGroup(String groupName,
 			String actualGroupFolderName, File groupFolderOrFile, List<String> inclusionPatterns, List<String> exclusionPatterns) {
 
 		ArrayList<SourceType> srcTypeList = new ArrayList<SourceType>();
 
 		if (groupFolderOrFile != null) {
 
 			if (groupFolderOrFile.isDirectory()) {
 				return loadJstTypesFromProject(groupName,
 						actualGroupFolderName, groupFolderOrFile, inclusionPatterns,exclusionPatterns);
 			} else if (!TypeSpaceMgr.getInstance().existGroup(groupName)) {
 				return loadJstTypesFromLibrary(groupName, groupFolderOrFile);
 			}
 		}
 
 		return srcTypeList; // return empty list
 
 	}
 
 	private String getTypeName(String srcPath, File file) {
 		String fileName = file.getPath();
 
 		// replace / and \ with .
 		fileName = fileName.replace("\\", ".");
 		fileName = fileName.replace("/", ".");
 
 		String srcFolder = srcPath;
 		srcFolder = srcFolder.replace("\\", ".");
 		srcFolder = srcFolder.replace("/", ".");
 
 		int start = fileName.lastIndexOf(srcFolder);
 		start += srcFolder.length() + 1; // skip the trailing .
 
 		int end = fileName.lastIndexOf(".js"); // remove .js from type name
 
 		String typeName = fileName.substring(start, end);
 		return typeName;
 	}
 
 	protected List<SourceType> loadJstTypesFromProject(String groupName,
 			String actualGroupFolderName, File srcFolder, List<String> inclusionPatterns, List<String> exclusionPatterns) {
 
 		JstSrcFileCollector fileColl = new JstSrcFileCollector();
 		ArrayList<SourceType> srcTypeList = new ArrayList<SourceType>();
 
 		if (srcFolder != null) {
 
 			List<File> list = fileColl.getJsSrcFiles(srcFolder);
 
 			for (File file : list) {
 
 				try {
 					if (isVjoFile(file) && !isExcluded(srcFolder, file,inclusionPatterns,exclusionPatterns)) {
 						srcTypeList.add(createType(groupName,
 								actualGroupFolderName, srcFolder.getPath(),
 								file));
 					}
 
 				} catch (IOException e) {
 
 				} catch (URISyntaxException e) {
 					DLTKCore.error(e.toString(), e);
 				}
 			}
 		}
 
 		return srcTypeList;
 
 	}
 
 	private boolean isExcluded(File srcFolder, File file, List<String> inclusionPatterns,
 			List<String> exclusionPatterns) {
 		if( inclusionPatterns.size()==0 && exclusionPatterns.size()==0){
 			return false;
 		}
 
 		char[][] inclusionPatternsChar = new char[][]{};
 
 			inclusionPatternsChar = processPatterns(srcFolder,
 					inclusionPatterns);
 
 		char[][] exclusionPatternsChar = new char[][]{};
 			exclusionPatternsChar = processPatterns(srcFolder,
 					exclusionPatterns);
 
 
 
 		boolean excluded = org.eclipse.dltk.mod.compiler.util.Util.isExcluded(file.toString().toCharArray(), inclusionPatternsChar,
 				exclusionPatternsChar, file.isDirectory());
 		if(excluded){
 			System.out.println("file excluded :" + file);
 		}
 		return excluded;
 
 
 	}
 
 	private char[][] processPatterns(File srcFolder,
 			List<String> patterns) {
 		char[][] patternsChar;
 		int length = patterns.size();
 		patternsChar = new char[length][];
 		for (int i = 0; i < length; i++) {
 			patternsChar[i] = new File(srcFolder,patterns.get(i)).getAbsolutePath().toString().toCharArray();
 		}
 		return patternsChar;
 	}
 
 	private boolean isVjoFile(File file) {
 		if (file.exists()) {
 			return file.getName().endsWith(VjetPlugin.VJO_SUBFIX);
 		}
 		return false;
 	}
 
 	protected List<SourceType> loadJstTypesFromLibrary(String groupName,
 			File libFile) {
 		List<SourceType> typeList = new ArrayList<SourceType>();
 
 		String libFileName = libFile.getName().toLowerCase();
 
 		if (CodeassistUtils.isBinaryPath(libFileName)) {
 			ZipFile jarFile = null;
 			try {
 				jarFile = new ZipFile(libFile);
 
 				// load in bootstrap.js first
 				ZipEntry bootstrapEntry = jarFile.getEntry("bootstrap.js");
 				if(bootstrapEntry!=null){
 					InputStream stream = jarFile.getInputStream(bootstrapEntry);
 					JsLibBootstrapLoader.load(VjoParser.load(stream, "bootstrap.js"), groupName);
 				}
 
 				Enumeration<? extends ZipEntry> enumeration = jarFile.entries();
 
 				while (enumeration.hasMoreElements()) {
 
 					ZipEntry elem = enumeration.nextElement();
 
 					if (elem.getName().endsWith(".ser")) {
 						typeList.addAll(loadAllTypes(groupName, jarFile, elem));
 					}
 					else if(!elem.getName().contains("bootstrap.js")) {
 						typeList.add(createType(groupName, jarFile, elem));
 					}
 				}
 			} catch (IOException e) {
 
 			}finally{
 				try {
 					jarFile.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		return typeList;
 	}
 
 	protected List<SourceType> loadAllTypes(String groupName, ZipFile jarFile, ZipEntry elem) throws IOException {
 
 		InputStream stream = jarFile.getInputStream(elem);
 
 		List<IJstType> jstTypes = JstTypeSerializer.getInstance().deserialize(stream);
 
 		List<SourceType> srcTypes = new ArrayList<SourceType>();
 
 		for (IJstType type : jstTypes) {
 			if(JstCache.getInstance().getType(type.getName())==null){
 				JstCache.getInstance().addType((JstType)type);
 				if(type.getAliasTypeName()!=null && type instanceof JstObjectLiteralType){
 					JstCache.getInstance().addAliasType(type.getAliasTypeName(), (JstObjectLiteralType)type);
 				}
 
 			}
 			srcTypes.add(new SourceType(groupName, type));			
 		}
 
 		return srcTypes;		
 	}	
 
 	protected SourceType createType(String groupName, ZipFile jarFile,
 			ZipEntry elem) throws IOException {
 
 		String typeName = elem.getName();
 		if (!CodeassistUtils.isVjetFileName(typeName)) {
 			return null;
 		}
 		int end = typeName.lastIndexOf(VjetPlugin.VJO_SUBFIX); // remove .js
 
 
 
 
 
 		typeName = typeName.substring(0, end);
 		typeName = typeName.replace("\\", ".");
 		typeName = typeName.replace("/", ".");
 
 		InputStream stream = jarFile.getInputStream(elem);
 		byte[] bs = new byte[stream.available()];
 		stream.read(bs);
 		stream.close();
 
 		String source = new String(bs);
 
 		File f = null;
 		f = new File(jarFile.getName() + "!"
 				+ elem.getName());
 
 		SourceType srcType = new SourceType(groupName, typeName, source, f);
 
 		return srcType;
 
 	}
 
 }
