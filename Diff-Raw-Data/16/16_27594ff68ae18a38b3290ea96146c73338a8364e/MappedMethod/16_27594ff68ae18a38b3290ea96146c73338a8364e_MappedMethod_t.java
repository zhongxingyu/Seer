 package com.duder.domain;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: faffi
  * Date: 6/20/13
  * Time: 9:42 PM
  * To change this template use File | Settings | File Templates.
  */
 
 /**
     @RequestMapping(value = "/export/{templateId}/html", method = RequestMethod.GET)
 	public void exportHTML(@PathVariable String templateId, HttpServletResponse res) throws Exception
  */
 public class MappedMethod
 {
 	private String name;
 	private String httpMethodRestriction = null;
 	private String signature = "";
 	private List<MappedMethodParameter> parms = new ArrayList<MappedMethodParameter>();
 	private List<MappedMethodAnnotation> arms = new ArrayList<MappedMethodAnnotation>();
 
 	public MappedMethod(String name)
 	{
 		this.name = name;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public String getHttpMethodRestriction()
 	{
 		//remove the class, not relevant
 		//TODO: Expand this to cover removing the entire qualified name, org.springframework.web.bind.annotation.RequestMethod
 		//Remember that it can be multiple values too, parse it as a String array maybe? { element1, element2 }
 		if (httpMethodRestriction == null)
 			return "ANY";
 		if (httpMethodRestriction.startsWith("{"))
 			httpMethodRestriction = httpMethodRestriction.substring(1,httpMethodRestriction.length());
 		if (httpMethodRestriction.endsWith("}"))
 			httpMethodRestriction = httpMethodRestriction.substring(0,httpMethodRestriction.length()-1);
 
 		//Split the values into an array after removing the curly braces
 		final String[] reqMethods = httpMethodRestriction.split(",");
 		//For each value, capture from the last '.' to EOL
 		final List<String> ret = new ArrayList<String>();
 
 		//Make sure this works when the value IS NOT an array
 		for (final String s : reqMethods)
 		{
 			final String[] temp = s.split("\\.");
 			ret.add(temp[temp.length-1]);
 			//System.err.println(Arrays.asList(s.split("\\.")));
 		}
 
 		if (httpMethodRestriction != null)
 			return ret.toString();
 		else
 			return "ANY";   //No restriction identified
 	}
 
 	public boolean isHttpVerbRestricted()
 	{
 		return this.httpMethodRestriction != null;
 	}
 
 	public void setHttpMethodRestriction(String httpMethodRestriction)
 	{
 		this.httpMethodRestriction = httpMethodRestriction;
 	}
 
 	public List<MappedMethodParameter> getParameters()
 	{
 		return parms;
 	}
 
 	public List<MappedMethodAnnotation> getAnnotations()
 	{
 		return arms;
 	}
 	public void addAnnotation(MappedMethodAnnotation mma)
 	{
 		this.arms.add(mma);
 	}
 	public void addParameter(MappedMethodParameter parm)
 	{
 		this.parms.add(parm);
 	}
 	public String getRequestMappingURL()
 	{
 		for (final MappedMethodAnnotation mma: arms)
 		{
 			if (mma.getName().equals("RequestMapping"))
 			{
 				if (mma.getAnnotationParms().containsKey("value"))
 				{
 					String ret = mma.getAnnotationParms().get("value").replace("\"",""); //Gotta replace the quotes like for the controller ;)
					if (ret.startsWith("{") && ret.endsWith("}") && ret.split(",").length > 1)  //Make sure it starts and ends with curly braces, comma denotes multiple values
 					{
						ret = ret.substring(1,ret.length()-1);
 					}
 					//This breaks when the requestMapping value is an array
 					//TODO: Fix case where request mapping holds an array of values
 					return ret.startsWith("/") ? ret : "/" + ret;  //Add forward slash for pretty
 				}
 				else
 				{
 					return "";  //No request mapping value means it handles it at the class level annotation
 				}
 			}
 		}
 		return null;
 	}
 
 	public String getSignature()
 	{
 		return signature;
 	}
 
 	public void setSignature(String signature)
 	{
 		this.signature = signature;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "MappedMethod{" +
 				"name='" + name + '\'' +
 				", httpMethodRestriction='" + httpMethodRestriction + '\'' +
 				", parms=" + parms +
 				", arms=" + arms +
 				'}';
 	}
 }
