 package org.makumba.parade;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 public class ServletContextManager 
 {
   static String reloadLog=Config.paradeBase+"tomcat"+File.separator+"logs"+File.separator+"paradeReloadResult.txt";
 
   ServletContainer container;
   Map containerInitParams;
 
   Properties config;
   String fileName=Config.paradeBase+"servletContext.properties";
   
   Map webinfCache= new HashMap();
 
   {
     loadConfig();
   }
 
   void loadConfig()
   {
     try{
       config=new Properties();
       config.load(new FileInputStream(fileName));
     } catch(IOException e){ throw new RuntimeException(e.getMessage()); }
   }
 
   synchronized ServletContainer getServletContainer(javax.servlet.jsp.PageContext pc)
   {
     if(container==null)
     try{
       container=(ServletContainer)Config.class.getClassLoader().loadClass(config.getProperty("parade.servletContext.servletContainer")).newInstance();
       if(pc!=null)
 	{
 	  config.put("parade.servletContext.paradeContext", 
 		     ((javax.servlet.http.HttpServletRequest)pc.getRequest()).getContextPath());
 	  container.makeConfig(config, pc);
 	  config.save(new FileOutputStream(fileName), "Parade servlet context config");
 	}
       container.init(config);
     }catch(Exception e) { throw new RuntimeException(e.getMessage());}
     return container;
   }
 
 
   public void setParadeSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     String p= (String)row.get("parade.path");
     String s=null;
     synchronized (webinfCache)
       {
 	s=(String)webinfCache.get(p);
 	if(s==null || !new File(p+File.separator+s).isDirectory())
 	  {
 	    //	    System.out.println(p+File.separator+s);
 	    webinfCache.put(p, s= searchWebinf(p, new File(p)));
 	  }
       }
     if(!s.equals("NO WEBINF"))
       {
 	row.put("servletContext.path", s);
 	row.put("servletContext.name", "/"+row.get("parade.row"));
 	row.put("servletContext.status", new Integer(getServletContainer(pc).getContextStatus((String)row.get("servletContext.name"))));
       }
   }
 
   public void servletContextStartSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     setParadeSimple(row, pc);
     if(!isParadeCheck(row))
       row.put("result", getServletContainer(pc).startContext((String)row.get("servletContext.name")));
   }
 
 
   public void servletContextStopSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     setParadeSimple(row, pc);
     if(!isParadeCheck(row))
       row.put("result", getServletContainer(pc).stopContext((String)row.get("servletContext.name")));
   }
 
   public void servletContextReloadSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     // must check if it's not this one
     setParadeSimple(row, pc);
     if(!isParade(row))
       row.put("result", getServletContainer(pc).reloadContext((String)row.get("servletContext.name")));
     else
       {
 	try{
 	  String antCommand="ant";
 	  
 	  if(System.getProperty("os.name").toLowerCase().indexOf("windows")!=-1)
 	    antCommand="ant.bat";
 	  
 	  File f= new File(reloadLog);
 	  f.delete();
 	  //Process p= 
 	  Runtime.getRuntime().exec(antCommand+" -buildfile "+Config.paradeBase+"build.xml reload");
 	    //final InputStream i=p.getInputStream();
 	  /*	  new Thread(new Runnable(){
 	    public void run(){
 	      try{
 		int c;
 		while((c=i.read())!=-1)
 		  System.out.print((char)c);
 	      }catch(IOException e){ e.printStackTrace(); }
 	    }
 	  }).start();
 	  */     
 	  
 	  while(!f.exists())
 	    try{
 	    Thread.currentThread().sleep(100);
 	  }catch(Throwable t){}
 	  loadConfig();
 	  row.put("reload", new Integer(Integer.parseInt
 					(config.getProperty
 					 ("parade.servletContext.selfReloadWait"))));
 
 	}catch(IOException e){ 	row.put("result", "Cannot reload Parade "+e); e.printStackTrace();}
       }
   }
 
   public void servletContextInstallSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     setParadeSimple(row, pc);
     if(!isParadeCheck(row))
       row.put("result", getServletContainer(pc).installContext
 	      ((String)row.get("servletContext.name"),
 	       (String)row.get("parade.path")+File.separator+
 	       (String)row.get("servletContext.path")
 		));
   }
 
   public void servletContextRemoveSimple(java.util.Map row, javax.servlet.jsp.PageContext pc)
   {
     setParadeSimple(row, pc);
     if(!isParadeCheck(row))
       row.put("result", getServletContainer(pc).unInstallContext((String)row.get("servletContext.name")));
   }
 
   public static String searchWebinf(String original, File p)
   {
     String[] s= p.list();
     for(int i=0; i<s.length; i++)
       {
 	File f= new File(p, s[i]);
 	if(f.isDirectory() && !f.getName().equals("serialized") && f.toString().indexOf("tomcat"+File.separator+"logs")==-1)
 	  {
 	    if(f.getName().equals("WEB-INF") && new File(f, "web.xml").exists())
 	      {
 		if(f.getParent().toString().equals(original))
 		  return ".";
 		return (f.getParent().toString().substring(original.length()+1)).replace(File.separatorChar, '/');
 	      }
 	    else
 	      {
 		String ret=searchWebinf(original,f);
 		if(!ret.equals("NO WEBINF"))
 		  return ret;
 	      }
 	  }
       }
     return "NO WEBINF";
   }
 
   public static boolean isParadeCheck(java.util.Map row){ 
     if(isParade(row))
       {
 	row.put("result", "You can only reload Parade!");
 	return true;
       }
     return false;
   }
 
   public static boolean isParade(java.util.Map row){ 
     try{
       return row.get("parade.path").equals(new File(Config.paradeBase).getCanonicalPath()); 
     }catch(IOException e){ throw new RuntimeException(e.getMessage()); }
   }
 
   public static void main(String[] argv) throws IOException
   {
     ServletContextManager s= new ServletContextManager();
     PrintStream ps =new PrintStream(new FileOutputStream(reloadLog));
     ps.flush();
     ps.println(s.getServletContainer(null).reloadContext("/"+s.config.getProperty("parade.servletContext.paradeContext")));
   }
 }
