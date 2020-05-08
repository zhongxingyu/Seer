 package com.fuzzyninja.remapper;
 
 import java.util.Hashtable;
 import java.util.Map.Entry;
 
 public class RemapManager {
     private Hashtable<String, String> packages = new Hashtable<String, String>();
     private Hashtable<String, String> classes = new Hashtable<String, String>();
     private Hashtable<String, String> methods = new Hashtable<String, String>();
     private Hashtable<String, String> fields = new Hashtable<String, String>();
     
     public RemapManager() {
 	
     }
     
     public boolean hasRemaps()
     {
 	return packages.size() > 0 || classes.size() > 0 || methods.size() > 0 || fields.size() > 0;
     }
     
     public String remapPackage(String name)
     {
 	for(Entry<String, String> entry : packages.entrySet())
 	{
	    if(name.startsWith(entry.getKey()) || name == "/")
 	    {
 		if(name.startsWith(entry.getValue()))
 		{
 		    return name;
 		}
 		
 		return entry.getValue() + name.substring(entry.getKey().length(), name.length());
 	    }
 	}
 	
 	return name;
     }
     
     public String remapClass(String name)
     {
 	if(classes.containsKey(name))
 	{
 	    name = classes.get(name);
 	}
 	
 	return remapPackage(name);
     }
     
     public String remapMethod(String name, String method, String desc)
     {
 	if(method.equals("<init>") || method.equals("<clinit>")) // Don't remap constructors and destructors
 	{
 	    return method;
 	}
 	
 	String temp = name + "." + method + desc;
 	
 	if(methods.containsKey(temp))
 	{
 	    return methods.get(temp);
 	}
     
 	return method;
     }
     
     public String remapField(String name, String field)
     {
 	String temp = name + "." + field;
 	
 	if(fields.containsKey(temp))
 	{
 	    return fields.get(temp);
 	}
     
 	return field;
     }
     
     public void addPackage(String from, String to)
     {	
 	from = normalize(from, true);
 	to = normalize(to, true);
 	
 	packages.put(from, to);
     }
     
     public void addClass(String nms, String from, String to)
     {
 	nms = normalize(nms, true);
 	
 	from = nms + normalize(from);
 	to = nms + normalize(to);
 	
 	classes.put(from, to);
     }
     
     public void addMethod(String name, String desc, String from, String to)
     {
 	name = normalize(name);
 	
 	from = name + "." + from + desc;
 	
 	methods.put(from, to);
     }
     
     public void addField(String name, String from, String to)
     {
 	name = normalize(name);
 	
 	from = name + "." + from;
 	
 	fields.put(from, to);
     }
     
     private String normalize(String str)
     {
 	return normalize(str, false);
     }
     
     private String normalize(String str, boolean isPackage)
     {
 	if(str.contains("."))
 	{
 	    str = str.replace(".", "/");
 	}
 	
 	if(str.contains("\\"))
 	{
 	    str = str.replace("\\", "/");
 	}
 	
 	if(isPackage && !str.endsWith("/"))
 	{
 	    str = str + "/";
 	}
 	
 	return str;
     }
 }
