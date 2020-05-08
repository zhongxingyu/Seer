 package org.makumba.parade;
 import javax.servlet.*;
 import javax.servlet.jsp.*;
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import org.eu.best.tools.*;
 
 public class TomcatManager implements ServletContainer
 {
   String managerURL;
   String user, pass;
 
   public void makeConfig(java.util.Properties config, javax.servlet.jsp.PageContext pc)
   {
     config.put("parade.servletContext.tomcatServerName", pc.getRequest().getServerName());
     config.put("parade.servletContext.tomcatServerPort", ""+pc.getRequest().getServerPort());
   }
 
   public void init(java.util.Properties config)
   {
     managerURL= "http://"+config.get("parade.servletContext.tomcatServerName")+":"+Config.getProperty("http.port")+"/manager/";
     user=Config.getProperty("tomcat.manager.user");
     pass=Config.getProperty("tomcat.manager.pass");
   }
 
   protected String makeAccess(String s)
   {
     try{
       HttpURLConnection uc= ForeignHttpAuthorizer.sendAuth(new URL(managerURL+s),user, pass);
       if(uc.getResponseCode()!=200)
 	throw new RuntimeException(uc.getResponseMessage());
       if(uc.getContentLength()==0)
 	throw new RuntimeException("content zero");
       StringWriter sw= new StringWriter();
       InputStreamReader ir= new InputStreamReader(uc.getInputStream());
       char[] buf= new char[1024];
       int n;
       while((n=ir.read(buf))!=-1)
 	sw.write(buf, 0, n);
       return sw.toString();
     }catch(IOException e){ throw new org.makumba.util.RuntimeWrappedException(e); }
   }
   
   Map servletContextCache=new HashMap();
 
   public synchronized int getContextStatus(String contextName)
   {
     if(servletContextCache.isEmpty())
       readContextStatus();
     
     Integer stt=(Integer) servletContextCache.get(contextName);
     if(stt==null)
       return NOT_INSTALLED;
     return stt.intValue();
   }
 
   void readContextStatus()
   {
     servletContextCache= new HashMap();
     StringTokenizer st=new StringTokenizer(makeAccess("list"), "\n"); 
     while(st.hasMoreTokens())
       {
 	String s= st.nextToken();
 	if(s.startsWith("OK"))
 	  continue;
 	int n= s.indexOf(":");
 	int m;
 	if(n!=-1 && (m= s.indexOf(":",n+1))!=-1)
 	  {
 	    String contextName=s.substring(0, n);
 	    String status=s.substring(n+1, m);
 	    if(status.equals("stopped"))
 	      {
 		servletContextCache.put(contextName, new Integer(STOPPED));
 		continue;
 	      }
 	    if(status.equals("running"))
 	      {
 		servletContextCache.put(contextName, new Integer(RUNNING));
 		continue;
 	      }
 	  }
 	throw new RuntimeException("Cannot understand context status list:\n"+s);
       }
   }
 
   public synchronized String installContext(String contextName, String contextPath)
   {
     if(getContextStatus(contextName)!=NOT_INSTALLED)
       return "context "+contextName+" already installed";
     try{
       String dir=".";
       String context= new File(contextPath).getCanonicalPath();
       String canDir=null; 
       File f;
       while((f=new File(dir)).exists() && !context.startsWith(canDir=f.getCanonicalPath()))
 	dir+=File.separator+"..";
 
       if(!f.exists())
 	throw new RuntimeException("cannot find common root to context");
       String s= makeAccess("install?path="+contextName+"&war=file:"+Config.paradeBaseRelativeToTomcatWebapps+File.separator+dir+context.substring(canDir.length()));
       if(s.startsWith("OK"))
      {
        servletContextCache.put(contextName, new Integer(RUNNING));
        String s1=stopContext(contextName);
        if(s1.startsWith("OK"))
           s+="<br>"+startContext(contextName);
        else
	{
	   s+="<br>Attempting to stop and start "+ contextName +" for checking correct installation failed "+ pleaseCheck(s1);
	   servletContextCache.put(contextName, new Integer(STOPPED));
	}
      }
      
       return s;
     }catch(IOException e){ throw new RuntimeException(e.getMessage()); }
   }
 
   public synchronized String unInstallContext(String contextName)
   {
     if(getContextStatus(contextName)==NOT_INSTALLED)
       return "context "+contextName+" not installed";
     String s=makeAccess("remove?path="+contextName);
     if(s.startsWith("OK"))
        servletContextCache.remove(contextName);
     return s;
   }
 
   public synchronized String startContext(String contextName)
   {
     if(getContextStatus(contextName)==RUNNING)
       return "context "+contextName+" already running";
     if(getContextStatus(contextName)==NOT_INSTALLED)
       return "context "+contextName+" not installed";
     String s= makeAccess("start?path="+contextName);
     if(s.startsWith("OK"))
        servletContextCache.put(contextName, new Integer(RUNNING));
    else
       s="Could not start "+contextName+". "+pleaseCheck(s);
     return s;
   }
 
   public synchronized String stopContext(String contextName)
   {
     if(getContextStatus(contextName)==STOPPED)
       return "context "+contextName+" already stopped";
     if(getContextStatus(contextName)==NOT_INSTALLED)
       return "context "+contextName+" not installed";
     String s= makeAccess("stop?path="+contextName);
     if(s.startsWith("OK"))
        servletContextCache.put(contextName, new Integer(STOPPED));
     return s;
   }
 
   public synchronized String reloadContext(String contextName)
   {
     if(getContextStatus(contextName)==STOPPED)
       return "context "+contextName+" stopped";
     if(getContextStatus(contextName)==NOT_INSTALLED)
       return "context "+contextName+" not installed";
     String s=makeAccess("reload?path="+contextName);
     if(s.startsWith("OK"))
        servletContextCache.put(contextName, new Integer(RUNNING));
    else
       s="Could not reload "+contextName+". "+pleaseCheck(s);
     return s;
   }
 
  static String pleaseCheck(String s)
  {
	return 
         "Please check the output on the Parade log. "+
         "Make sure that the filters and servlets declared in web.xml exist and are compiled. "+
         "Make sure that the tag libraries declared in web.xml exist. "+
         "A complete CVS update followed by 'clean' and 'compile' might help. "+
         "<br>Message from Tomcat (may be uncomplete) is: "+s;
  }

 }
