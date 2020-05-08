 package net.sf.javascribe.engine;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.LanguageSupport;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.SourceFile;
 import net.sf.javascribe.api.config.ComponentBase;
 import net.sf.javascribe.api.config.ComponentSet;
 import net.sf.javascribe.api.config.Property;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 public class CodeGenerator {
 	private static Logger log = LogManager.getLogger(CodeGenerator.class);
 	
 	EnginePropertiesImpl engineProperties = null;
 	ApplicationDefinition def = null;
 	List<ComponentSet> compSets = null;
 	Map<String,String> systemAttributes = null;
 	Map<String,LanguageSupport> languageSupport = null;
 	Map<String,List<ProcessorEntry>> processors = null;
 	List<SourceFile> sourceFiles = null;
 
 	public CodeGenerator(EnginePropertiesImpl engineProperties,ApplicationDefinition app) {
 		this.engineProperties = engineProperties;
 		this.def = app;
 		this.languageSupport = engineProperties.getLanguageSupport();
 		this.processors = engineProperties.getProcessors();
 	}
 
 	public void generate() throws JavascribeException {
 		compSets = def.getComponents();
 		systemAttributes = def.getAttributes();
 		sourceFiles = new ArrayList<SourceFile>();
 
 		processComponents();
 
 		writeFiles();
 	}
 
 	private void processComponents() throws JavascribeException {
 		Map<String,TypeResolverImpl> typeMap = new HashMap<String,TypeResolverImpl>();
 		Map<String,Object> objects = new HashMap<String,Object>();
 
 		List<ComponentBase> compList = new ArrayList<ComponentBase>();
 		for(ComponentSet set : def.getComponents()) {
 			compList.addAll(set.getComponent());
 		}
 
 		log.info("Found "+compList.size()+" components to process");
 		boolean resort = true; // Do we sort the compList?
 
 		while(!compList.isEmpty()) {
 			if (resort) {
 				// The compList is sorted in 2 cases: first run, and if compList is added to.
 				Collections.sort(compList);
 				resort = false;
 			}
 			int priority = compList.get(0).getPriority();
 			List<ComponentBase> toRemove = new ArrayList<ComponentBase>();
 
 			for(ComponentBase comp : compList) {
 				String name = comp.getClass().getCanonicalName();
 
 				if (comp.getPriority()!=priority) {
 					// Once we reach components of a new priority, stop and resort the list.
 					break;
 				}
 
 				Map<String,String> properties = buildProperties(def.getGlobalProperties(),comp.getProperty());
 
 				toRemove.add(comp);
 				List<ProcessorEntry> currentProcessors = this.processors.get(name);
 				if (currentProcessors==null) {
 					log.warn("WARNING: Found no processors for "+comp.getClass().getCanonicalName());
 					//throw new JavascribeException("Found no processors for component class '"+name+"'");
 				}
 				else {
 					ProcessorContextImpl generatorContext = new ProcessorContextImpl(def.getBuildRoot(),engineProperties,languageSupport,systemAttributes,typeMap,sourceFiles,properties,objects);
 					for(ProcessorEntry entry : currentProcessors) {
 						applyProcessor(comp,generatorContext,entry);
 					}
 					if (generatorContext.getAddedComponents().size()>0) {
 						compList.addAll(generatorContext.getAddedComponents());
 						resort = true;
 						break;
 					}
 				}
 			}
 			compList.removeAll(toRemove);
 		}
 	}
 
 	private void applyProcessor(ComponentBase component,ProcessorContextImpl ctx,ProcessorEntry processorEntry) throws JavascribeException {
 		Class<?> processorClass = processorEntry.getProcessorClass();
 		Object processor = null;
 
 		try {
 			processor = processorClass.newInstance();
 			Method method = processorEntry.getMethod();
 			Class<?>[] types = method.getParameterTypes();
 			Object[] args = new Object[types.length];
 			for(int i=0;i<types.length;i++) {
 				if (ComponentBase.class.isAssignableFrom(types[i])) {
 					args[i] = component;
 				} else if (types[i]==ProcessorContext.class) {
 					args[i] = ctx;
 				} else {
 					throw new JavascribeException("In processor method, found unsupported parameter of class '"+types[i].getCanonicalName()+"'");
 				}
 			}
 			method.invoke(processor, args);
 		} catch(InstantiationException e) {
 			throw new JavascribeException("Exception while invoking component processor",e);
 		} catch(IllegalAccessException e) {
 			throw new JavascribeException("Exception while invoking component processor",e);
 		} catch(InvocationTargetException e) {
 			throw new JavascribeException("Exception while invoking component processor",e);
 		}
 	}
 
 	private Map<String,String> buildProperties(Map<String,String> globals,List<Property> compProps) {
 		Map<String,String> ret = new HashMap<String,String>();
 
 		ret.putAll(globals);
 		for(Property p : compProps) {
 			ret.put(p.getName(), p.getValue());
 		}
 
 		return ret;
 	}
 
 	private void writeFiles() throws JavascribeException {
 		FileWriter writer = null;
 		File f = null;
 		StringBuilder srcString = null;
 
 		for(SourceFile sourceFile : sourceFiles) {
 			try {
 				f = new File(sourceFile.getPath());
 				f.getParentFile().mkdirs();
 				try {
 					writer = new FileWriter(f);
 					srcString = sourceFile.getSource();
 					if (srcString==null) {
 						throw new JavascribeException("Found illegal state - Source File "+sourceFile.getPath()+"' has null content");
 					}
 					writer.write(srcString.toString());
 					writer.flush();
 				} finally {
 					if (writer!=null) {
 						try { writer.close(); } catch(Exception e) { }
 					}
 				}
 
 			} catch (Exception e) {
 				throw new JavascribeException(
 						"Exception while writing source file '"+sourceFile.getPath()+"'", e);
 			} finally {
 				if (writer != null) {
 					try {
 						writer.close();
 					} catch (Exception e) {
 					}
 				}
 			}
 		}
 
 	}
 
 }
