 package com.bearprogrammer.resource;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.JspFragment;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ResourceTag extends SimpleTagSupport {
 	
 	static final Logger LOGGER = LoggerFactory.getLogger(ResourceTag.class);
 	
 	static final String DEFAULT_ENCODING = "UTF-8";
 	static final String DEFAULT_SERVLET_PATH = "/resources.do";
 	
 	String servletPath;
 	String encoding = DEFAULT_ENCODING;
 	
 	@Override
 	public void doTag() throws JspException, IOException {
 		PageContext context = (PageContext)getJspContext();
 		
 		initialize(context);
 		
 		// Add context path to resource servlet name
 		this.servletPath = context.getServletContext().getContextPath() + this.servletPath;
 		
 		// Check for encoding to be used from a init parameter
 		String encoding = context.getServletContext().getInitParameter(ResourceTag.class.getName().concat("encoding"));
 		if (encoding != null) {
 			this.encoding = encoding;
 		}
 		
 		JspFragment body = getJspBody();
 
 		// read the body content
 		StringWriter writer = new StringWriter();
 		body.invoke(writer);
 		String bodyContent = writer.toString();
 
 		// Group requested files by extension
 		String [] files = bodyContent.split("\n");
 		Map<String, List<String>> filesByExtension = new HashMap<String, List<String>>();
 		for (String file : files) {
 			file = file.trim();
 			if ("".equals(file)) continue;
 			
 			String extension = FilenameUtils.getExtension(file);
 			
 			List<String> filesForExtension = filesByExtension.get(extension);
 			if (filesForExtension == null) {
 				filesForExtension = new ArrayList<String>();
 				filesByExtension.put(extension, filesForExtension);
 			}
 			
 			LOGGER.debug("File: {}, Extension: {}", file, extension);
 			filesForExtension.add(file);
 		}
 		
 		StringBuilder result = new StringBuilder();
 		
 		// Create a tag for each type
 		for (String extension : filesByExtension.keySet()) {
 			Type type = TypeFactory.getType(extension);
 			if (type == null) {
 				throw new IllegalArgumentException("Type not found for: " + extension);
 			}
 			result.append(createTag(filesByExtension.get(extension), type));
 		}
 		
 		// write it to the output
 		context.getOut().write(result.toString());
 	}
 
 	synchronized void initialize(PageContext context) {
 		if (servletPath == null) {
 			// Check for the servlet path from a init parameter
 			String servletPath = context.getServletContext().getInitParameter(ResourceTag.class.getName().concat("servletPath"));
 			if (servletPath != null) {
 				this.servletPath = servletPath;
			} else {
				this.servletPath = DEFAULT_SERVLET_PATH;
 			}
 		}
 	}
 	
 	private String createTag(List<String> files, Type type) throws UnsupportedEncodingException {
 		StringBuilder tag = new StringBuilder();
 		tag.append("<");
 		tag.append(type.getTypeTag().getTagName());
 		
 		// Set the path to the servlet
 		tag.append(" ");
 		tag.append(type.getTypeTag().getAttributeName());
 		tag.append("=\"");
 		tag.append(servletPath);
 		tag.append("?");
 		
 		// Add the files to be loaded as parameter
 		for (String file : files) {
 			tag.append("file=");
 			tag.append(URLEncoder.encode(file, encoding));
 			tag.append("&");
 		}
 		
 		// Remove the last "&"
 		tag.setLength(tag.length() - 1);
 		
 		// Close the attribute
 		tag.append("\"");
 		
 		// Add all other attributes
 		Map<String, String> otherAttributes = type.getTypeTag().getOtherAttributes();
 		if (otherAttributes != null) {
 			for (Entry<String, String> attribute : otherAttributes.entrySet()) {
 				tag.append(attribute.getKey());
 				tag.append("=\"");
 				tag.append(attribute.getValue());
 				tag.append("\" ");
 			}
 		}
 		
 		// Close the tag
 		tag.append("></");
 		tag.append(type.getTypeTag().getTagName());
 		tag.append(">");
 		
 		return tag.toString();
 	}
 	
 }
