 /*
 This file is part of the Polygraph bulk messaging framework
 Copyright (C) 2013 Wolfgang Illmeyer
 
 The Polygraph framework is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package com.illmeyer.polygraph.template;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.illmeyer.polygraph.core.CoreConstants;
 import com.illmeyer.polygraph.core.data.MessagePart;
 
 import freemarker.core.Environment;
 import freemarker.template.TemplateDirectiveBody;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateModel;
 import freemarker.template.TemplateModelException;
 import freemarker.template.utility.DeepUnwrap;
 
 /**
  * Abstracts away the environment of the template engine and offers an interface more suited to Polygraph's needs
  * @author escitalopram
  *
  */
 public class PolygraphEnvironment {
 	private TemplateModel[] loopVars;
 	private TemplateDirectiveBody body;
 	private Environment env;
 	
 	public PolygraphEnvironment(TemplateModel[] loopVars,
 			Environment env, TemplateDirectiveBody body) {
 		this.loopVars=loopVars;
 		this.body=body;
 		this.env=env;
 	}
 
 	public Object getLoopVar(int index) {
 		try {
 			return DeepUnwrap.unwrap(loopVars[index]) ;
 		} catch (TemplateModelException e) {
 			throw new PolygraphTemplateException(e);
 		}
 	}
 	
 	public void setLoopVar(int index, Object value) {
 		try {
 			loopVars[index]=env.getObjectWrapper().wrap(value);
 		} catch (TemplateModelException e) {
 			throw new PolygraphTemplateException(e);
 		}
 	}
 	
 	public Writer getWriter() {
 		return env.getOut();
 	}
 	
 	public void executeBody(Writer writer) throws IOException {
 		try {
 			body.render(writer);
 		} catch (TemplateException e) {
 			throw new PolygraphTemplateException(e);
 		}
 	}
 	public TagStack getTagStack() {
 		TagStack ts = (TagStack) env.getCustomAttribute(CoreConstants.ECA_TAGSTACK);
 		if (ts==null) {
 			ts=new TagStack();
 			env.setCustomAttribute(CoreConstants.ECA_TAGSTACK, ts);
 		}
 		return ts;
 	}
 
 	public void executeBody() throws IOException {
 		executeBody(getWriter());
 	}
 	
 	public void registerMessagePart(String name, MessagePart p) throws PolygraphTemplateException {
 		Map<String,MessagePart> parts = getParts();
 		if (parts.containsKey(name)) throw new PolygraphTemplateException(String.format("Message part '%s' is already registered", name));
 		parts.put(name, p);
 	}
 	
 	public MessagePart getNamedPart(String name) {
 		Map<String,MessagePart> parts = getParts();
 		return parts.get(name);
 	}
 
 	private Map<String,MessagePart> getParts() {
 		@SuppressWarnings("unchecked")
 		Map<String,MessagePart> partMap = (Map<String, MessagePart>) env.getCustomAttribute(CoreConstants.ECA_PARTS); 
 		if (partMap==null) {
 			partMap = new HashMap<String, MessagePart>();
 			env.setCustomAttribute(CoreConstants.ECA_PARTS, partMap);
 		}
 		return partMap;
 	}
 	
 	public <A> A requireParentTag(Class<A> tagClass) {
 		TagStack ts = getTagStack();
 		if (ts.size()>1) {
 			PolygraphTag tag = ts.get(ts.size()-2);
 			if (tagClass.isInstance(tag)) {
 				@SuppressWarnings("unchecked")
 				A tag2 = (A)tag;
 				return tag2;
 			}
 		}
 		throw new PolygraphTemplateException(String.format("Parent tag of type %s expected but not found.",tagClass.getName()));
 	}
 	
 	public <A> A requireAncestorTag(Class<A> tagClass) {
 		TagStack ts = getTagStack();
 		if (ts.size()>1) {
 			for (int i=ts.size()-2;i>=0;--i) {
 				PolygraphTag tag=ts.get(i);
				if (!tagClass.isInstance(tag)) {
 					@SuppressWarnings("unchecked")
 					A tag2 = (A)tag;
 					return tag2;
 				}
 			}
 		}
 		throw new PolygraphTemplateException(String.format("Ancestor tag of type %s expected but not found.",tagClass.getName()));
 	}
 	
 	public void setCustomAttribute(String key, Object value) {
 		env.setCustomAttribute(key, value);
 	}
 	
 	public Object getCustomAttribute(String key) {
 		return env.getCustomAttribute(key);
 	}
 
 	public boolean hasBody() {
 		return body!=null;
 	}
 }
