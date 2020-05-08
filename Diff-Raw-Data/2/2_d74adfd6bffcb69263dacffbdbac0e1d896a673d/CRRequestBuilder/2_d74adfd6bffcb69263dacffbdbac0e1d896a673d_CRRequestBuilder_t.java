 package com.gentics.cr.util;
 
 import java.util.ArrayList;
 
 import javax.portlet.PortletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.rest.ContentRepository;
 import com.gentics.cr.rest.javabin.JavaBinContentRepository;
 import com.gentics.cr.rest.javaxml.JavaXmlContentRepository;
 import com.gentics.cr.rest.json.JSONContentRepository;
 import com.gentics.cr.rest.php.PHPContentRepository;
 import com.gentics.cr.rest.rss.RomeContentRepository;
 import com.gentics.cr.rest.xml.MnogosearchXmlContentRepository;
 import com.gentics.cr.rest.xml.XmlContentRepository;
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class CRRequestBuilder {
 
 	protected RepositoryType repotype;
 	protected boolean isDebug=false;
 	protected String filter;
 	protected String start;
 	protected String count;
 	protected String debug;
 	protected String type;
 	protected String contentid;
 	protected String[] attributes;
 	protected String[] sorting;
 	protected String[] plinkattributes;
 	protected String[] permissions;
 	protected String[] options;
 	protected Object request;
 	protected Object response;
 	
 	public String[] getAttributeArray()
 	{
 		return(this.attributes);
 	}
 	
 	public String[] getOptionArray()
 	{
 		return(this.options);
 	}
 	
 	public RepositoryType getRepositoryType()
 	{
 		return this.repotype;
 	}
 	
 	public boolean isDebug()
 	{
 		return this.isDebug;
 	}
 	
 	public CRRequestBuilder(PortletRequest request)
 	{
 		this.request = request;
 					
 		this.filter = (String) request.getParameter("filter");
 		this.contentid = (String) request.getParameter("contentid");
 		this.start = (String) request.getParameter("start");
 		this.count = request.getParameter("count");
 		this.sorting = request.getParameterValues("sorting");
 		this.attributes =  this.prepareAttributesArray(request.getParameterValues("attributes"));
 		this.plinkattributes =  request.getParameterValues("plinkattributes");
 		this.debug = (String)request.getParameter("debug");
 		this.permissions = request.getParameterValues("permissions"); 
 		this.options = request.getParameterValues("options");
 		this.type=request.getParameter("type");
 		this.isDebug = (request.getParameter("debug")!=null && request.getParameter("debug").equals("true"));
 		
 		//if filter is not set and contentid is => use contentid instad
 		if (("".equals(filter) || filter == null)&& contentid!=null && !contentid.equals("")){
 			filter = "object.contentid ==" + contentid;
 		}
 		//SET PERMISSIONS-RULE
 		filter = this.createPermissionsRule(filter, permissions);
 		
 		//Initialize RepositoryType
 		if(this.type!=null)
 		{
 			if(this.type.equalsIgnoreCase("JSON"))this.repotype=RepositoryType.JSON;
 			else if(this.type.equalsIgnoreCase("PHP"))this.repotype=RepositoryType.PHP;
 			else if(this.type.equalsIgnoreCase("JAVAXML"))this.repotype=RepositoryType.JAVAXML;
 			else if(this.type.equalsIgnoreCase("MNOGOSEARCHXML"))this.repotype=RepositoryType.MNOGOSEARCHXML;
 			else if(this.type.equalsIgnoreCase("JAVABIN"))this.repotype=RepositoryType.JAVABIN;
 			else this.repotype=RepositoryType.XML;
 		}
 		else
 		{
 			this.repotype=RepositoryType.XML;
 		}
 		
 		//Set debug flag
 		if("true".equals(debug))
 			this.isDebug=true;
 
 	}
 	
 	
 	public CRRequestBuilder(HttpServletRequest request)
 	{
 		this.request = request;
 		this.filter = (String) request.getParameter("filter");
 		this.contentid = (String) request.getParameter("contentid");
 		this.start = (String) request.getParameter("start");
 		this.count = request.getParameter("count");
 		this.sorting = request.getParameterValues("sorting");
 		this.attributes =  this.prepareAttributesArray(request.getParameterValues("attributes"));
 		this.plinkattributes =  request.getParameterValues("plinkattributes");
 		this.debug = (String)request.getParameter("debug");
 		this.permissions = request.getParameterValues("permissions"); 
 		this.options = request.getParameterValues("options");
 		this.type=request.getParameter("type");
 		this.isDebug = (request.getParameter("debug")!=null && request.getParameter("debug").equals("true"));
 		
 		//if filter is not set and contentid is => use contentid instad
 		if (("".equals(filter) || filter == null)&& contentid!=null && !contentid.equals("")){
			filter = "object.contentid == '" + contentid+"'";
 		}
 		//SET PERMISSIONS-RULE
 		filter = this.createPermissionsRule(filter, permissions);
 		
 		//Initialize RepositoryType
 		if(this.type!=null)
 		{
 			if(this.type.equalsIgnoreCase("JSON"))this.repotype=RepositoryType.JSON;
 			else if(this.type.equalsIgnoreCase("PHP"))this.repotype=RepositoryType.PHP;
 			else if(this.type.equalsIgnoreCase("JAVAXML"))this.repotype=RepositoryType.JAVAXML;
 			else if(this.type.equalsIgnoreCase("RSS"))this.repotype = RepositoryType.RSS;
 			else if(this.type.equalsIgnoreCase("MNOGOSEARCHXML"))this.repotype=RepositoryType.MNOGOSEARCHXML;
 			else if(this.type.equalsIgnoreCase("JAVABIN"))this.repotype=RepositoryType.JAVABIN;
 			else this.repotype=RepositoryType.XML;
 		}
 		else
 		{
 			this.repotype=RepositoryType.XML;
 		}
 		
 		//Set debug flag
 		if("true".equals(debug))
 			this.isDebug=true;
 
 	}
 	
 	public CRRequest getCRRequest()
 	{
 		CRRequest req = new CRRequest(filter,start,count,sorting,attributes,plinkattributes);
 		req.setContentid(this.contentid);
 		req.setRequest(this.request);
 		req.setResponse(this.response);
 		return req;
 	}
 	
 	public Object getRequest(){
 		return this.request;
 	}
 	
 	//Wrapps filter rule with the given set of permissions
 	protected String createPermissionsRule(String filter, String[] permissions)
 	{
 		String ret=filter;
 		if((permissions != null) && (permissions.length>0))
 		{
 			if((filter!=null)&&(!filter.equals("")))
 			{
 				ret="("+filter+") AND object.permissions CONTAINSONEOF "+CRUtil.prepareParameterArrayForRule(permissions);
 			}
 			else
 			{
 				ret="object.permissions CONTAINSONEOF "+CRUtil.prepareParameterArrayForRule(permissions);
 			}
 		}
 		return ret;
 	}
 	
 	protected String[] prepareAttributesArray(String[] attributes)
 	{
 		ArrayList<String> ret = new ArrayList<String>();
 		if(attributes!=null)
 		{
 			for(String item: attributes)
 			{
 				if(item.contains(","))
 				{
 					String[] items = item.split(",");
 					for(String subatt:items)
 					{
 						ret.add(subatt);
 					}
 				}
 				else
 				{
 					ret.add(item);
 				}
 			}
 		}
 		return ret.toArray(new String[ret.size()]);
 	}
 	
 	public ContentRepository getContentRepository(String encoding)
 	{
 		ContentRepository cr = null;
 		switch(this.getRepositoryType())
 		{
 			case JSON:
 				cr = new JSONContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
 				break;
 			case PHP:
 				cr = new PHPContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
 				break;
 			case JAVAXML:
 				cr = new JavaXmlContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
 				break;
 			case RSS:
 				cr = new RomeContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
 				break;
 			case MNOGOSEARCHXML:
 				cr = new MnogosearchXmlContentRepository(this.getAttributeArray(), encoding, this.getOptionArray());
 				break;
 			case JAVABIN:
 				cr = new JavaBinContentRepository(this.getAttributeArray(), encoding, this.getOptionArray());
 				break;
 			default:
 				cr = new XmlContentRepository(this.getAttributeArray(),encoding,this.getOptionArray());
 		}
 		return cr;
 	}
 	
 }
