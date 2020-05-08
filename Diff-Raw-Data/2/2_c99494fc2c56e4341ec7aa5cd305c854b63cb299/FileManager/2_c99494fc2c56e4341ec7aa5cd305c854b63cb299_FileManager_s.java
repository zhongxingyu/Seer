 package org.makumba.parade;
 import java.util.*;
 import java.io.*;
 
 public class FileManager 
 {
   
   FileFilter filter;
 
   {
    filter= (FileFilter)Config.getManager(Config.getProperty("FileManager.filter")); 
   }
   public void setParadeSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     String path= (String)row.get("parade.path");
     // nothing for now
     // we could e.g. compute the newest file from path/ on (attention, ignore tomcat logs and makumba.jar...
   }
 
   public synchronized void findPathSimple(java.util.Map row, javax.servlet.jsp.PageContext pc) throws ParadeException
   {
     String path=null;
     String s[]=pc.getRequest().getParameterValues("parade.path");
     if(s==null || (path=s[0].trim()).length()==0)
       return;
     if(s.length!=1 || path.indexOf(" ")!=-1)    
       throw new ParadeException("multiple paths, or path containing spaces");
     File f= new File(path);
     try{
     if(f.exists() && f.isDirectory())
       {
 	row.put("parade.path", f.getCanonicalPath());
 	return;
       }
     }catch(IOException e){}
     throw new ParadeException("directory does not exist or cannot be accessed");
   }
 
   public File initFileOperation(java.util.Map data, javax.servlet.jsp.PageContext pc)
        throws ParadeException
   {
     List ignored= new ArrayList();
     pc.setAttribute("file.readingTime", new Long(new java.util.Date().getTime()), javax.servlet.jsp.PageContext.REQUEST_SCOPE);
     pc.setAttribute("file.ignoredList", ignored, javax.servlet.jsp.PageContext.REQUEST_SCOPE);
     Map row= (Map)pc.findAttribute("parade.rowData");
    File f=new File(row.get("parade.path")+File.separator+((String)row.get("file.path")).replace('/', File.separatorChar));
     return f;
   }
 
   public void readFiles(java.util.Map data, javax.servlet.jsp.PageContext pc)
        throws ParadeException
   {
     File f=initFileOperation(data, pc);
     if(!f.isDirectory())
       return;
     pc.setAttribute("file.baseFile", f, javax.servlet.jsp.PageContext.REQUEST_SCOPE);
     ArrayList ignored=(ArrayList)pc.findAttribute("file.ignoredList");
     File[] dir= f.listFiles();
     for(int i= 0; i<dir.length; i++)
       {
 	String name=dir[i].getName();
 	if(!filter.accept(dir[i]))
 	  {
 	    ignored.add(dir[i]);
 	    continue;
 	  }
 	Map m= new HashMap();
 	data.put(name, m);
 	m.put("file.file", dir[i]);
 	m.put("file.name", name);
 
 	/*	for(Iterator k= row.entrySet().iterator(); k.hasNext(); )
 	  {
 	    Map.Entry me= (Map.Entry)k.next();
 	    m.put(me.getKey(), me.getValue());
 	  }
 	  */
       }
   }
 
 
   public void deleteFile(java.util.Map data, javax.servlet.jsp.PageContext pc)
   {
     for(Iterator i= data.values().iterator(); i.hasNext(); )
       {
 	Map m = (Map)i.next();
 	File f=(File)m.get("file.file");
 	if(f==null ||!f.exists())
 	  m.put("result", m.get("file.name")+" does not exist");
 	else
 	  {
 	    f.delete();
 	    m.put("result", m.get("file.name")+" was deleted");
 	  }
       }
   }
 
   public void readTree(java.util.Map data, javax.servlet.jsp.PageContext pc)
        throws ParadeException
   {
     findAllSubdirs((String)pc.findAttribute("file.path"), initFileOperation(data, pc), data, pc);
   }
 
   public void findAllSubdirs(String path, File f, java.util.Map data, javax.servlet.jsp.PageContext pc)
   {
     ArrayList ignored=(ArrayList)pc.findAttribute("file.ignoredList");
     File[] dir= f.listFiles();
     for(int i= 0; i<dir.length; i++)
       {
 	if(!filter.accept(dir[i]))
 	  {
 	    ignored.add(dir[i]);
 	    continue;
 	  }
 	if(dir[i].isDirectory())
 	  {
 	    Map m= new HashMap();
 	    String name=dir[i].getName()+"/";
 	    if(path.length()>0)
 	      name=path+name;
 	    data.put(name, m);
 	    m.put("file.path", name);
 	    m.put("file.name", dir[i].getName());
 	    m.put("isFolder", "yes");
 	    m.put("file.children", new ArrayList());
 	    findAllSubdirs(name, dir[i], data, pc);	    
 	  }
       }
   }
 
   public void setFileDataSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     	File f= (File)row.get("file.file");
         if(f==null) return;
     	if(f.isDirectory())
 	  row.put("isFolder", "yes");
     	long l= f.lastModified();
      
     	row.put("file.date", new Long(l));
     	row.put("file.age", new Long(((Long)pc.findAttribute("file.readingTime")).longValue()-l));
     	row.put("file.size", new Long(f.length()));
   }
 
   public static List setChildren(Map data)
   {
     List result= new ArrayList();
     List orderedData= new ArrayList();
     orderedData.addAll(data.values());
     Collections.sort(orderedData, new MapComparator(null, "file.path"));
     for(Iterator i= orderedData.iterator(); i.hasNext(); )
       {
 	Map m= (Map)i.next();
 	String path= (String)m.get("file.path");
 	int n= path.substring(0, path.length()-1).lastIndexOf('/');
 	List l=result;
 	if(n!=-1)
 	  {
 	    l=(List)((Map)data.get(path.substring(0, n+1))).get("file.children");
 	  }
 	l.add(m);
       }
     return result;
   }
 
   static public void printTree(StringBuffer sb, int level, List l)
   {
     for(Iterator j= l.iterator(); j.hasNext(); )
       {
 	for(int i=0; i<level; i++)
 	  sb.append("  ");
 	Map m= (Map)j.next();
 	sb.append(m.get("file.name")).append("\n");
 	printTree(sb, level+1,(List)m.get("file.children"));
       }
   }
 }
