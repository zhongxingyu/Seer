 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2011 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.xml;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jdom.xpath.XPath;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 
 import screen.tools.sbs.context.ContextException;
 import screen.tools.sbs.context.ContextHandler;
 import screen.tools.sbs.context.defaults.ContextKeys;
 import screen.tools.sbs.context.defaults.EnvironmentVariablesContext;
 import screen.tools.sbs.context.defaults.RepositoryContext;
 import screen.tools.sbs.objects.Dependency;
 import screen.tools.sbs.objects.Description;
 import screen.tools.sbs.objects.EnvironmentVariables;
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.objects.Flag;
 import screen.tools.sbs.objects.Import;
 import screen.tools.sbs.objects.Library;
 import screen.tools.sbs.objects.Pack;
 import screen.tools.sbs.objects.TinyPack;
 import screen.tools.sbs.repositories.RepositoryComponent;
 import screen.tools.sbs.repositories.RepositoryFilter;
 import screen.tools.sbs.utils.FieldBool;
 import screen.tools.sbs.utils.FieldBuildMode;
 import screen.tools.sbs.utils.FieldBuildType;
 import screen.tools.sbs.utils.FieldException;
 import screen.tools.sbs.utils.FieldFile;
 import screen.tools.sbs.utils.FieldPath;
 import screen.tools.sbs.utils.FieldPathType;
 import screen.tools.sbs.utils.FieldString;
 import screen.tools.sbs.utils.Logger;
 import screen.tools.sbs.utils.Utilities;
 
 public class SBSDomDataFiller {
 	private ContextHandler contextHandler;
 	private Pack pack;
 	private Pack testPack;
 	private FieldPath sbsXmlPath;
 	private static String propertyNameQuery = "//properties/name/text()";
 	private static String propertyVersionQuery = "//properties/version/text()";
 	private static String propertyBuildTypeQuery = "//properties/buildtype/text()";
 	private String propertyName;
 	private String propertyVersion;
 	private String propertyBuildType;
 	private boolean isRelease;
 	
 	public SBSDomDataFiller(ContextHandler contextHandler, Pack pack, Pack testPack, FieldPath sbsXmlPath) {
 		this.contextHandler = contextHandler;
 		this.sbsXmlPath = sbsXmlPath;
 		this.pack = pack;
 		this.testPack = testPack;
 	}
 	
 	public void fill(Document doc, boolean isTest) throws ContextException, FieldException{
 		//ErrorList errList = GlobalSettings.getGlobalSettings().getErrorList();
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 		
 		FieldString fieldCompileMode = variables.getFieldString("_COMPILE_MODE");
 		String compileMode = fieldCompileMode.getString();
 		isRelease = "Release".equals(compileMode);
 		
 		Element root = doc.getRootElement();
 		
 		try {
 			if(pack == null)
 				pack = new Pack();
 			if(testPack == null)
 				testPack = new Pack();
 			//properties
 			
 			//name
 			{
 				XPath xpa = XPath.newInstance(propertyNameQuery);
 				propertyName = xpa.valueOf(root);
 				Logger.debug("propertyName : "+propertyName);
 				pack.getProperties().setName(new FieldString(propertyName));
 			}
 			
 			//version
 			{
 				XPath xpa = XPath.newInstance(propertyVersionQuery);
 				propertyVersion = xpa.valueOf(root);
 				Logger.debug("propertyVersion : "+propertyVersion);
 				pack.getProperties().setVersion(new FieldString(propertyVersion));
 			}
 			
 			//build type
 			{
 				XPath xpa = XPath.newInstance(propertyBuildTypeQuery);
 				propertyBuildType = xpa.valueOf(root);
 				Logger.debug("propertyBuildType : "+propertyBuildType);
 				pack.getProperties().setBuildType(new FieldString(propertyBuildType));
 			}
 			
 			if(isTest){
 				//test
 				List<?> test = root.getChildren("test");
 				Iterator<?> iterator = test.iterator();
 				while(iterator.hasNext()){
 					testPack.getProperties().setName(new FieldString(propertyName+"/Test"));
 					testPack.getProperties().setVersion(new FieldString(propertyVersion));
 					testPack.getProperties().setBuildType(new FieldString("executable"));
 					FieldPath path = new FieldPath(sbsXmlPath.getOriginalString()+"/test");
 					processDependencies((Element) iterator.next(), testPack, path);
 					
 					//descriptions
 					processDescriptions(root, testPack, path);
 					
 					//imports
 					processImports(root, testPack, path);
 				}
 			}
 			else{
 				processAll(root, pack, sbsXmlPath);
 			}
 			
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void processAll(Element root, Pack pack, FieldPath xmlPath) throws ContextException, FieldException {
 		//main
 		List<?> main = root.getChildren("main");
 		Iterator<?> iterator = main.iterator();
 		while(iterator.hasNext()){
 			Element next = (Element) iterator.next();
 			processDependencies(next,pack,xmlPath);
 			processFlags(next,pack,xmlPath);
 		}
 		
 		//descriptions
 		processDescriptions(root, pack, xmlPath);
 		
 		//imports
 		processImports(root, pack, xmlPath);
 	}
 
 	private void processDependencies(Element root, Pack pack, FieldPath xmlPath) throws ContextException, FieldException {
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 		
 		//dependencies
 		Logger.debug("dependencies");
 		List<?> depsRoot = root.getChildren("dependencies");
 		Iterator<?> rootIterator = depsRoot.iterator();
 		if(rootIterator.hasNext()){
 			Element next = (Element) rootIterator.next();
 			List<?> deps = (next).getChildren("dependency");
 			Iterator<?> iterator = deps.iterator();
 			while(iterator.hasNext()){
 				Element dep = (Element) iterator.next();
 				List<Library> tmpLibList = new ArrayList<Library>();
 				
 				//dependency
 				Logger.debug("\tdependency");
 				Dependency newDep = new Dependency();
 				
 				String name = dep.getAttributeValue("name");
 				newDep.setName(new FieldString(name));
 
 				String version = dep.getAttributeValue("version");
 				newDep.setVersion(new FieldString(version));
 				
 				String export = dep.getAttributeValue("export");
 				newDep.setExport(new FieldBool(export));
 				
 				// includes
 				List<?> inclRoot = dep.getChildren("includes");
 				Iterator<?> inclIterator = inclRoot.iterator();
 				while(inclIterator.hasNext()){
 					Element inclNext = (Element) inclIterator.next();
 					Logger.debug("\t\tincludes");
 					List<?> paths = inclNext.getChildren("path");
 					Iterator<?> pathIterator = paths.iterator();
 					while(pathIterator.hasNext()){
 						//path
 						Logger.debug("\t\t\tpath");
 						Element path = (Element) pathIterator.next();
 						
 						String pathString = path.getTextTrim();
 						Logger.debug("\t\t\t\ttext : "+pathString);
 						
 						String type = path.getAttributeValue("type");
 						FieldPathType pType = new FieldPathType();
 						pType.set(type);
 						Logger.debug("\t\t\t\ttype : "+type);
 						
 						String buildMode = path.getAttributeValue("build");
 						Logger.debug("\t\t\t\tbuild : "+buildMode);
 						
 						FieldPath fieldPath = pType.getFieldPath(xmlPath.getOriginalString(), pathString);
						if(buildMode!=null)
							fieldPath.setBuildMode(new FieldBuildMode(buildMode));
 						
 						if(fieldPath.getBuildMode().isSameMode(isRelease))
 							newDep.addIncludePath(fieldPath);
 					}
 				}
 				
 				//libraries
 				List<?> libsRoot = dep.getChildren("libraries");
 				Iterator<?> libsRootIterator = libsRoot.iterator();
 				if(libsRootIterator.hasNext()){
 					Element libsRootNext = (Element) libsRootIterator.next();
 					Logger.debug("\t\tlibraries");
 					List<?> paths = libsRootNext.getChildren("path");
 					Iterator<?> pathsIterator = paths.iterator();
 					while(pathsIterator.hasNext()){
 						//path
 						Logger.debug("\t\t\tpath");
 						Element path = (Element) pathsIterator.next();
 						String pathString = path.getTextTrim();
 						Logger.debug("\t\t\t\ttext : "+pathString);
 						
 						String type = path.getAttributeValue("type");
 						FieldPathType pType = new FieldPathType();
 						pType.set(type);
 						Logger.debug("\t\t\t\ttype : "+type);
 						
 						String buildMode = path.getAttributeValue("build");
 						Logger.debug("\t\t\t\tbuild : "+buildMode);
 						
 						FieldPath fieldPath = pType.getFieldPath(xmlPath.getOriginalString(), pathString);
						if(buildMode!=null)
							fieldPath.setBuildMode(new FieldBuildMode(buildMode));
 						
 						if(fieldPath.getBuildMode().isSameMode(isRelease))
 							newDep.addLibraryPath(fieldPath);
 					}
 					List<?> libs = libsRootNext.getChildren("lib");
 					Iterator<?> libsIterator = libs.iterator();
 					while(libsIterator.hasNext()){
 						//lib
 						Logger.debug("\t\t\tlib");
 						Element lib = (Element) libsIterator.next();
 						String libString = lib.getTextTrim();
 						Logger.debug("\t\t\t\ttext : "+libString);
 
 						String libVersion = lib.getAttributeValue("version");
 						
 						Library library = new Library();
 						library.setName(new FieldString(libString));
 						library.setVersion(new FieldString(libVersion));
 						
 						newDep.addLibrary(library);
 						tmpLibList.add(library);
 					}
 				}
 				
 				if(newDep.getSbs() && !(pack instanceof TinyPack)){
 					//retrieve dependency file in SBS repository
 					String packName = newDep.getName().getString();
 					String packVersion = newDep.getVersion().getString();
 					
 					FieldString fieldEnvName = variables.getFieldString("ENV_NAME");
 					String envName = fieldEnvName.getString();
 					
 					RepositoryComponent finder = new RepositoryComponent(newDep.getName(), newDep.getVersion(), fieldEnvName);
 					RepositoryFilter retrieved = finder.retrieve(contextHandler.<RepositoryContext>get(ContextKeys.REPOSITORIES).getRepositoryFilterTable());
 					if(retrieved == null){
 						ErrorList.instance.addError("Unable to retrieve component into repositories :\n"+
 									"- component name : "+packName+"\n"+
 									"- component version : "+packVersion+"\n"+
 									"- compiler : "+envName);
 						return;
 					}
 					String fullPath = retrieved.getData().getPath().getString()+"/"+packName+"/"+packVersion;
 					
 					//String fullPath = repoRoot +"/"+packName+"/"+packVersion;					
 					
 					if(new File(fullPath+"/component.xml").exists()){
 						//if component.xml exists, retrieve contents into pack
 						Document doc = SBSDomParser.parserFile(new File(fullPath+"/component.xml"));
 						Element root2 = doc.getRootElement();
 						Logger.debug("import "+fullPath+"/component.xml");
 						
 						processAll(root2, pack, new FieldPath(fullPath));
 					}
 					else { 
 						if(Utilities.isWindows())
 							ErrorList.instance.addError("Can't retrieve file component.xml in "+fullPath+" folder : component "+packName+" with version "+packVersion+" doesn't exist");
 						else {
 							ErrorList.instance.addWarning("Can't retrieve file component.xml in "+fullPath+" folder : component "+packName+" with version "+packVersion+" doesn't exist => Uses default settings");
 							Iterator<Library> libIterator = tmpLibList.iterator();
 							while(libIterator.hasNext()){
 								//add default library description
 								Description description = new Description();
 								Library lib = libIterator.next();
 								description.setName(lib.getName().getString());
 								
 								EnvironmentVariables additionalVars = new EnvironmentVariables();
 								additionalVars.put("LIB_NAME", lib.getName().getOriginalString().replaceAll("/", ""));
 								
 								FieldString fs = new FieldString("${DEFAULT_SHARED_LIB_COMPILE_NAME}");
 								description.setCompileName(fs.getString(additionalVars));
 								
 								fs = new FieldString("${DEFAULT_SHARED_LIB_FULL_NAME}");
 								description.setFullName(fs.getString(additionalVars));
 								
 								description.setBuildMode(FieldBuildMode.Type.ALL);
 								description.setBuildType(FieldBuildType.Type.SHARED_LIBRARY);
 								pack.addDescription(description);
 							}
 						}
 					}
 				}
 				
 				pack.addDependency(newDep);
 			}
 		}
 	}
 
 	private void processFlags(Element root, Pack pack, FieldPath xmlPath) {
 		//flags
 		Logger.debug("flags");
 		List<?> optsRoot = root.getChildren("flags");
 		Iterator<?> optsRootIterator = optsRoot.iterator();
 		while(optsRootIterator.hasNext()){
 			Element optsRootNext = (Element) optsRootIterator.next();
 			List<?> opts = optsRootNext.getChildren("flag");
 			Iterator<?> optsIterator = opts.iterator();
 			while(optsIterator.hasNext()){
 				//dependency
 				Logger.debug("\tflag");
 				Element opt = (Element) optsIterator.next();
 				Flag flag = new Flag();
 				
 				String flagValue = opt.getAttributeValue("flag");
 				flag.setFlag(new FieldString(flagValue));
 				Logger.debug("\t\t\tflag : "+flagValue);
 				
 				String value = opt.getAttributeValue("value");
 				flag.setValue(value);
 				Logger.debug("\t\t\tvalue : "+value);
 				
 				String buildMode = opt.getAttributeValue("build");
 				Logger.debug("\t\t\t\tbuild : "+buildMode);
				if(buildMode!=null)
					flag.setBuildMode(new FieldBuildMode(buildMode));
 				
 				if(flag.getBuildMode().isSameMode(isRelease))
 					pack.addFlag(flag);
 			}
 		}
 	}
 		
 	void processDescriptions(Element root, Pack pack, FieldPath xmlPath) throws ContextException{		
 		//descriptions
 		Logger.debug("descriptions");
 		List<?> descRoot = root.getChildren("descriptions");
 		Iterator<?> descRootIterator = descRoot.iterator();
 		while(descRootIterator.hasNext()){
 			Element descRootNext = (Element) descRootIterator.next();
 			List<?> descs = descRootNext.getChildren("library");
 			Iterator<?> descsIterator = descs.iterator();
 			while(descsIterator.hasNext()){
 				//dependency
 				Logger.debug("\tlibrary");
 				Element lib = (Element) descsIterator.next();
 				Description description = new Description();
 				
 				String name = lib.getAttributeValue("name");
 				description.setName(name);
 				Logger.debug("\t\t\tname : "+name);
 				
 				String fullName = lib.getAttributeValue("full-name");
 				description.setFullName(fullName);
 				Logger.debug("\t\t\tfull-name : "+fullName);
 				
 				String compileName = lib.getAttributeValue("compile-name");
 				description.setCompileName(compileName);
 				Logger.debug("\t\t\tcompile-name : "+compileName);
 
 				String buildType = lib.getAttributeValue("type");
 				description.setBuildType(buildType);
 				Logger.debug("\t\t\ttype : "+buildType);
 
 				String buildMode = lib.getAttributeValue("build");
				if(buildMode!=null)
					description.setBuildMode(buildMode);
 				Logger.debug("\t\t\tbuild : "+buildMode);
 
 				if(description.getBuildMode().isSameMode(isRelease))
 					pack.addDescription(description);
 			}
 		}
 	}
 	
 	private void processImports(Element root, Pack pack, FieldPath xmlPath) throws ContextException, FieldException {		
 		//descriptions
 		Logger.debug("imports");
 		List<?> importRoot = root.getChildren("imports");
 		Iterator<?> importRootIterator = importRoot.iterator();
 		while(importRootIterator.hasNext()){
 			Element importRootNext = (Element) importRootIterator.next();
 			List<?> imports = importRootNext.getChildren("import");
 			Iterator<?> importsIterator = imports.iterator();
 			while(importsIterator.hasNext()){
 				//dependency
 				Logger.debug("\timport");
 				Element imp = (Element) importsIterator.next();
 				Import import_ = new Import();
 				
 				String buildMode = imp.getAttributeValue("build");
				if(buildMode!=null)
					import_.setBuildMode(buildMode);
 				Logger.debug("\t\tbuild : "+buildMode);
 				
 				//path
 				String file = imp.getAttributeValue("file");
 				Logger.debug("\t\tfile : "+file);
 				
 				String type = imp.getAttributeValue("pathtype");
 				FieldPathType pType = new FieldPathType();
 				pType.set(type);
 				Logger.debug("\t\tpathtype : "+type);
 				
 				FieldFile fieldFile = pType.getFieldFile(xmlPath.getOriginalString(), file);
 				import_.setFile(fieldFile);
 				
 				if(import_.getBuildMode().isSameMode(isRelease) && !(pack instanceof TinyPack)){
 					String file2 = import_.getFile().getString();
 					File importFile = new File(file2);
 					if(importFile.exists()){
 						//if component.xml exists, retrieve contents into pack
 						Document doc = SBSDomParser.parserFile(importFile);
 						Element root2 = doc.getRootElement();
 						Logger.debug("import "+file2);
 						
 						processAll(root2, pack, new FieldPath(importFile.getParent()));
 					}
 					else{
 						
 					}
 				}
 			}
 		}
 	}
 }
