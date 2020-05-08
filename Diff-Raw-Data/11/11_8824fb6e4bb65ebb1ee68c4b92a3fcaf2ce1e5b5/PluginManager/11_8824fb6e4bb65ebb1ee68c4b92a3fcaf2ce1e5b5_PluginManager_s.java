 /*
  * Copyright 2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package bingo.lang.plugin;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import bingo.lang.Assert;
 import bingo.lang.Classes;
 import bingo.lang.Collections;
 import bingo.lang.Enumerables;
 import bingo.lang.Predicates;
 import bingo.lang.Reflects;
 import bingo.lang.Strings;
 import bingo.lang.beans.BeanModel;
 import bingo.lang.beans.BeanProperty;
 import bingo.lang.json.JSON;
 import bingo.lang.resource.Resource;
 import bingo.lang.resource.Resources;
 import bingo.lang.xml.XmlDocument;
 import bingo.lang.xml.XmlElement;
 import bingo.lang.xml.XmlValidator;
 
 public class PluginManager<P extends Plugin> {
 	
 	private static final XmlValidator validator = XmlValidator.of(Resources.getInputStream(PluginManager.class,"plugin.xsd"));
 	
 	public static final String[] DEFAULT_PLUGIN_SEARCH_PATHS = new String[]{"/META-INF/plugins","/plugins"};
 	
 	protected Class<P>    pluginType;
 	protected String[] 	  locations;
 	protected List<P>     plugins = new CopyOnWriteArrayList<P>();
 	private LoadContext	  context = new LoadContext();
 	
 	protected PluginManager(){
 
 	}
 	
 	public PluginManager(Class<P> pluginType){
 		this.pluginType = pluginType;
 	}
 	
 	public PluginManager(Class<P> pluginType,String location){
 		this.pluginType = pluginType;
 		this.locations  = new String[]{location};
 	}
 	
 	public PluginManager(Class<P> pluginType,String[] locations){
 		this.pluginType = pluginType;
 		this.locations  = locations;
 	}
 	
 	public P[] getPlugins(){
 		return Collections.toArray(plugins, pluginType);
 	}
 	
 	public P getPlugin(String name){
 		return Enumerables.firstOrNull(plugins,Predicates.<P>nameEqualsIgnoreCase(name));
 	}
 	
 	public synchronized P[] load(){
 		if(null == locations){
 			locations = DEFAULT_PLUGIN_SEARCH_PATHS;
 		}
 
 		for(String location : locations){
			if(!location.startsWith(Resources.CLASSPATH_URL_PREFIX) || !location.startsWith(Resources.CLASSPATH_ALL_URL_PREFIX)){
 				location = Resources.CLASSPATH_ALL_URL_PREFIX + location;
 			}
 			
 			if(location.endsWith(".xml")){
 				load(location);
 			}else{
 				load(location + "/" + pluginType.getName() + ".xml");	
 			}
 		}
 		
 		try {
 	        for(XmlElement e : context.adds){
 	        	loadNewPlugin(e);
 	        }
 	        
 	        for(XmlElement e : context.sets){
 	        	loadExistPlugin(e);
 	        }
         } catch (Throwable e) {
         	throw new PluginException("Loading plugin failed : {0}",e.getMessage(),e);
         }
 		
 		return getPlugins();
 	}
 	
 	protected void load(String location) {
 		Resource[] resources = Resources.scanQuietly(location);
 		
 		for(Resource resource : resources){
 			load(XmlDocument.load(resource));
 		}
 	}
 	
 	protected void load(XmlDocument doc) {
 		validator.validate(doc);
 		
 		for(XmlElement e : doc.rootElement().childElements()){
 			if(e.name().equals("add")) {
 				context.adds.add(e);
 			}else if(e.name().equals("set")){
 				context.sets.add(e);
 			}else{
 				throw new IllegalStateException("found unknow xml element '" + e.name() + "' in : " + doc.url());
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected void loadNewPlugin(XmlElement e) throws Throwable{
		String   name        = e.requiredAttributeValue("name");
 		String   clazzName   = e.requiredAttributeValue("class");
 		Class<?> clazzObject = Classes.forName(clazzName);
 		
 		Assert.isFalse(getPlugin(name) != null,"plugin name '{0}' aleady exists, please check the xml : {1}",name,e.documentUrl());
 		
 		Assert.isTrue(pluginType.isAssignableFrom(clazzObject),"'class' value of plugin '" + name + "' is invalid in xml : " + e.documentUrl());
 		
         P plugin = (P)Reflects.newInstance(clazzObject);
 		
 		plugin.setName(name);
 		
 		loadPlugin(e, plugin);
 		
 		plugins.add(plugin);
 	}
 	
 	protected void loadExistPlugin(XmlElement e) throws Throwable{
 		String name  = e.requiredAttributeValue("name");
 		String clazz = e.attributeValue("class");
 		
 		Plugin plugin = getPlugin(name);
 
 		Assert.notNull(plugin,"plugin '{0}' not exists, make sure the name is correct or use <add...>",name);
 		
 		if(!Strings.isEmpty(clazz)){
 			plugins.remove(name.toLowerCase());
 			loadNewPlugin(e);
 			return;
 		}
 		
 		loadPlugin(e, plugin);
 	}
 	
 	@SuppressWarnings("rawtypes")
     protected void loadPlugin(XmlElement e,Plugin plugin) throws Throwable{
 		XmlElement document = e.childElement("document");
 
 		if(null != document){
 			String title       = Strings.trim(document.childElementText("title"));
 			String summary     = Strings.trim(document.childElementText("summary"));
 			String description = Strings.trim(document.childElementText("description"));
 			
 			if(!Strings.isEmpty(title)){
 				plugin.setTitle(title);
 			}
 			
 			if(!Strings.isEmpty(summary)){
 				plugin.setSummary(summary);
 			}
 			
 			if(!Strings.isEmpty(description)){
 				plugin.setDescription(description);
 			}
 		}
 		
 		XmlElement properties = e.childElement("properties");
 		
 		if(null != properties){
 			BeanModel model = BeanModel.get(plugin.getClass());
 			
 			for(XmlElement prop : properties.childElements()){
 				String propName  = prop.requiredAttributeValue("name");
 				String propValue = Strings.trimToNull(prop.attributeValueOrText("value"));
 				String valueType = prop.attributeValue("type");
 				
 				BeanProperty p = model.getPropertyIgnoreCase(propName);
 
 				Assert.notNull(p,"property '{0}' not found in plugin type '{1}'",propName,plugin.getClass().getName());
 				
 				if("json".equalsIgnoreCase(valueType)){
 					p.setValue(plugin, JSON.decode(propValue,p.getType()));
 				}else{
 					p.setValue(plugin, propValue);	
 				}
 			}
 		}
 	}
 	
 	private static final class LoadContext {
 		List<XmlElement> adds = new ArrayList<XmlElement>();
 		List<XmlElement> sets = new ArrayList<XmlElement>();
 	}
 }
