 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.grouper;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.runtime.RuntimeServices;
 import org.apache.velocity.runtime.RuntimeSingleton;
 import org.apache.velocity.runtime.parser.ParseException;
 import org.apache.velocity.runtime.parser.node.SimpleNode;
 import org.sakaiproject.nakamura.api.lite.authorizable.Group;
 import org.sakaiproject.nakamura.grouper.api.GrouperConfiguration;
import org.sakaiproject.nakamura.grouper.api.GrouperNameManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * Create grouper name information from the authorizableId of a {@link Group}.
  * 
  * To use this component you have to define two things:
  * 1. A regular expression to apply to the authorizableId (grouper.groupId.regex)
  * 2. An output template to create a string for the grouper name. (grouper.name.template)
  * 
  */
 @Component
 @Service
 @Properties(value = {
 		@Property(name = "service.ranking", value = "20")
 })
public class TemplateGrouperNameProviderImpl implements GrouperNameManager {
 	
 	private static final Logger log = LoggerFactory.getLogger(TemplateGrouperNameProviderImpl.class);
 
 	@Reference
 	protected GrouperConfiguration config;
 
 	private Template template;
 
 	@Activate
 	@Modified
 	public void activate(Map<?,?> props) {
 		assert config.getGroupIdPattern() != null;
 		assert config.getGrouperNameTemplate() != null;
 		modified(props);
 	}
 
 	public void modified(Map<?,?> props) {
 		try {
 			Velocity.init();
 			RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();            
 			StringReader reader = new StringReader(config.getGrouperNameTemplate());
 			SimpleNode node = runtimeServices.parse(reader, "Grouper name template");
 			template = new Template();
 			template.setRuntimeServices(runtimeServices);
 			template.setData(node);
 			template.initDocument();
 		}
 		catch (ParseException pe) {
 			log.error("Could not parse the velocity template, {}", pe.getMessage());
 		}
 	}
 
 	@Override
 	public String getGrouperName(String groupId) {
 		if (groupId == null){
 			return null;
 		}
 		Matcher m = config.getGroupIdPattern().matcher(groupId);
 		if (! m.find() || m.groupCount() == 0){
 			throw new RuntimeException("Did not match " + groupId);
 		}
 		ArrayList<String> g = new ArrayList<String>();
 		for(int i = 0; i <= m.groupCount(); i++){
 			g.add(m.group(i));
 		}
 		VelocityContext context = new VelocityContext();
 		context.put("g", g);
 		context.put("extension", BaseGrouperNameProvider.getGrouperExtension(groupId, config));
 
 		StringWriter sw = new StringWriter();
 		template.merge(context, sw);
 		
 		// 
 		String grouperName = sw.toString();
 		String[] split = StringUtils.split(grouperName, ':');
 		for (String suffix: config.getPseudoGroupSuffixes()){
 			if (split[split.length - 2].endsWith(suffix)){
 				split[split.length - 2] = split[split.length - 2].substring(0, split[split.length - 2].indexOf(suffix)); 
 			}
 		}
 		return StringUtils.join(split, ':');
 	}
 
 	public void bindGrouperConfiguration(GrouperConfiguration gconfig) {
 		config = gconfig;
 		modified(null);
 	}
 
 }
