 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.devservlet;
 
 import java.awt.GraphicsEnvironment;
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import om.*;
 import om.question.*;
 import om.stdquestion.StandardQuestion;
 
 import org.w3c.dom.*;
 
 import util.misc.*;
 import util.xml.*;
 
 /** 
  * Servlet used to build and test questions on a developer machine. Not suitable 
  * for any production or public demonstration use. Only supports one instance
  * of one question at a time.
  */
 public class DevServlet extends HttpServlet
 {
 	/** In-progress question (null if none) */
 	private Question qInProgress=null;
 	
 	/** Classloader for in-progress question */
 	private ClosableClassLoader cclInProgress=null;
 	
 	/** ID of in-progress question */
 	private String sInProgressID=null;
 
 	/** Map of String (filename) -> Resource */
 	private Map mResources=new HashMap();
 	
 	/** CSS */
 	private String sCSS=null;
 	
 	/** List of question definitions */
 	private QuestionDefinitions qdQuestions;
 	
 	/** Clear/reset question data */
 	private void resetQuestion()
 	{
 		// Get rid of question and (hopefully) clear its classloader
 		if(qInProgress!=null)
 		{
 			try
 			{
 				qInProgress.close();
 			}
 			catch(Throwable t)
 			{
 				// Ignore errors on close
 			}
 			mResources.clear();
 			
 			// Not all questions are laoded using ClosableClassLoaders; some are.
 			// This needs more examination to figure it out.
 			cclInProgress.close();
 			cclInProgress=null;
 			qInProgress=null;
 			sInProgressID=null;
 			sCSS=null;
 		}
 	}
 	
 	public void init() throws ServletException
 	{
 		try
 		{
 			System.setProperty("java.awt.headless", "true");
 		}
 		catch(Throwable t)
 		{
 		}
 		if(!GraphicsEnvironment.isHeadless())
 		{
 			throw new ServletException("Your application server must be set to run in " +
 				"headless mode. Add the following option to the Java command line that " +
 				"launches it: -Djava.awt.headless=true");
 		}
 		
 		try
 		{
 			qdQuestions=new QuestionDefinitions(getServletContext());
 		}
 		catch(OmException oe)
 		{
 			throw new ServletException(oe);
 		}		
 	}
 		
 	protected void doGet(HttpServletRequest request,HttpServletResponse response)
 		throws ServletException,IOException
 	{
 		handle(false,request,response);
 	}
 	
 	protected void doPost(HttpServletRequest request,HttpServletResponse response)
 		throws ServletException,IOException
 	{
 		handle(true,request,response);
 	}
 	
 	private void sendError(HttpServletRequest request,HttpServletResponse response,
 		int iCode,
 		String sTitle,
 		String sMessage, Throwable tException) 
 	{
 		try
 		{
 			response.setStatus(iCode);
 			response.setContentType("text/html");
 			response.setCharacterEncoding("UTF-8");
 			PrintWriter pw=response.getWriter();
 			pw.println(
 				"<html>" +
 				"<head><title>"+XHTML.escape(sTitle,XHTML.ESCAPE_TEXT)+"</title></head>"+
 				"<body>" +
 				"<h1>"+XHTML.escape(sTitle,XHTML.ESCAPE_TEXT)+"</h1>" +
 				"<p>"+XHTML.escape(sMessage,XHTML.ESCAPE_TEXT)+"</p>" +
 				"<p>" +
 				(
 					(sInProgressID!=null && request.getPathInfo().equals("/run/"+sInProgressID+"/")) ?
 						"<a href='./'>[Restart]</a> <a href='../../build/"+sInProgressID+"/'>[Rebuild]</a> "
 						: "")+
 				"<a href='../../'>[List]</a> "+
 				"</p>"+
 				(tException!=null ? 
 					"<pre>"+XHTML.escape(Exceptions.getString(
 						tException,new String[]{"om"}),XHTML.ESCAPE_TEXT)+"</pre>": "")+
 				"</body>" +
 				"</html>");
 			pw.close();
 		}
 		catch(IOException ioe)
 		{
 			// Ignore exception, they must have closed browser or something
 		}
 	}
 	
 	private void handleFront(boolean bPost,HttpServletRequest request,HttpServletResponse response)
 	  throws Exception
 	{
 		if(bPost)
 		{
 			File fNew=new File(
 				qdQuestions.getQuestionsFolder(),request.getParameter("package")+".xml");
 			Writer w=new OutputStreamWriter(new FileOutputStream(fNew),"UTF-8");
 			w.write(
 				"<questiondefinition>\n" +
 				"  <sourcetree>"+request.getParameter("source")+"</sourcetree>\n" +
 				"  <package>"+request.getParameter("package")+"</package>\n" +
 				(request.getParameter("extra").trim().length()>0 
 					? "  <includepackage>"+request.getParameter("extra")+"</includepackage>"
 					: "")+
 				"</questiondefinition>\n");
 			w.close();	
 			response.sendRedirect(".");
 		}
 		
 		QuestionDefinition[] aqd=qdQuestions.getQuestionDefinitions();
 
 		// Create basic template
 		Document d=XML.parse(
 			"<xhtml>" +
 			"<head>" +
 			"<title>OpenMark-S (Om) question development</title>"+
 			"<style type='text/css'>"+
 			"body { font: 12px Verdana, sans-serif; }" +
 			"h1 { font: bold 14px Verdana, sans-serif; }" +
 			"a { color: black; }" +
 			"h2 { font: 14px Verdana, sans-serif; }" +
 			"#create,#questionbox { margin-bottom:20px; border:1px solid #888; padding:10px; }"+
 			"#create span { clear:left; float:left; width:20em; }" +
 			"#crate div { margin-top:5px; }"+
 			"</style>"+
 			"</head>"+
 			"<body>"+
 			"<h1>" +
 			"  OpenMark-S (Om) question development ("+OmVersion.getVersion()+")" +
 			"</h1>" +
 			"<div id='questionbox'>" +
 			"<h2>Defined questions</h2>"+
 			"<ul id='questions'>"+
 			"</ul>"+
 			"</div>" +
 			"<form id='create' method='post' action='.'>" +
 			"<h2>Create new question</h2>" +
 			"<div><span>Package</span><input type='text' name='package' value='"+
 				((aqd.length>0) ? aqd[aqd.length-1].getPackage().replaceAll("\\.[^.]$",".") : "")+
 				"'/></div>"+
 			"<div><span>Source tree</span><input type='text' name='source' size='65' value='" +
 				((aqd.length>0) ? aqd[aqd.length-1].getSourceFolder().getAbsolutePath() : "")+ 
 				"'/></div>"+
 			"<div><span>Extra package (optional)</span><input type='text' name='extra' size='65' value='" +
 				((aqd.length>0 && aqd[aqd.length-1].getAdditionalPackageRoots().length>0) ? 
 					aqd[aqd.length-1].getAdditionalPackageRoots()[0] : "")+ 
 				"'/></div>"+
 			"<div><input type='submit' name='action' value='Create'/></div>"+
 			"<p>This creates a new question definition file (.xml) in the questions " +
 			"folder of your Om webapp.</p>"+
 			"<p>If you want to remove questions from the list, manually delete the .xml " +
 			"(you can easily make it again) or move it somewhere. You also need to " +
 			"manually edit the xml if you want to include more than one extra package, " +
 			"sorry.</p>"+
 			"</form>"+
 			"</body>"+
 			"</xhtml>");
 		
 		// Find the root element and chuck in a line for each question
 		Element eParent=XML.find(d,"id","questions");
 		for(int iQuestion=0;iQuestion<aqd.length;iQuestion++)
 		{
 			Element 
 				eQ=XML.createChild(eParent,"li");
 			XML.createText(eQ," "+aqd[iQuestion].getID()+" ");
 			if(aqd[iQuestion].hasJar())
 			{
 				Element eRun=XML.createChild(eQ,"a");
 				eRun.setAttribute("href","run/"+aqd[iQuestion].getID()+"/");
 				XML.createText(eRun,"(Run)");
 				XML.createText(eQ," ");
 			}
 			Element eBuild=XML.createChild(eQ,"a");
 			eBuild.setAttribute("href","build/"+aqd[iQuestion].getID()+"/");
 			XML.createText(eBuild,"(Build)");
 		}
 
 		XHTML.output(d,request,response,"en");		
 	}
 	
 	private void handleBuild(String sRemainingPath,
 		HttpServletRequest request,HttpServletResponse response) throws Exception
 	{
 		resetQuestion();
 		
 		String sQuestion=sRemainingPath.replaceAll("^([^/]*)/?.*$","$1");
 		String sAfter=sRemainingPath.replaceAll("^[^/]*/?(.*)$","$1");
 		
 		if(!sAfter.equals(""))
 		{
 			sendError(request,response,
 				HttpServletResponse.SC_NOT_FOUND,"Not found","Don't know how to handle request: "+sRemainingPath, null);
 			return;
 		}
 		
 		response.setContentType("text/html");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter pw=response.getWriter();
 		boolean bSuccess=qdQuestions.getQuestionDefinition(sQuestion).build(pw);
 		if(bSuccess)
 		{			
 			pw.println(
 				"<script type='text/javascript'>\n" +
 				"var re=/^(.*)\\/build\\/(.*)$/;\n"+
 				"location.href=location.href.replace(re,'$1/run/$2');\n" +
 				"</script>");
 		}
 		else
 		{
 			pw.println(
 				"<p><a href='javascript:location.reload()'>Rebuild</a></p>");
 		}
 		pw.println("</body></html>");
 		pw.close();
 	}
 	
 	private InitParams ipInProgress;
 	
 	private void handleRun(boolean bPost,String sRemainingPath,
 		HttpServletRequest request,HttpServletResponse response) throws Exception
 	{
 		String sQuestion=sRemainingPath.replaceAll("^([^/]*)/?.*$","$1");
 		String sAfter=sRemainingPath.replaceAll("^[^/]*/?(.*)$","$1");
 
 		// Must access page with / at end
 		if("".equals(sAfter) && !request.getRequestURI().endsWith("/"))
 		{
 			response.sendRedirect(request.getRequestURI()+"/");
			return;
 		}
 
 		if("save".equals(request.getQueryString()))
 		{
 			// Delete existing saved files
 			File fSave=new File(getServletContext().getRealPath("save"));
 			if(!fSave.exists()) fSave.mkdir();
 			File[] afExisting=IO.listFiles(fSave);
 			for(int i=0;i<afExisting.length;i++)
 			{
 				if(afExisting[i].isFile())
 					afExisting[i].delete();				
 			}
 			File fResources=new File(fSave,"resources");
 			if(!fResources.exists()) fResources.mkdir();
 			afExisting=IO.listFiles(fResources);
 			for(int i=0;i<afExisting.length;i++)
 			{
 				if(afExisting[i].isFile())
 					afExisting[i].delete();				
 			}
 			
 			// Save last xhtml			
 			FileOutputStream fos=new FileOutputStream(new File(fSave,"question.html"));
 			fos.write(sLastXHTML.getBytes("UTF-8"));
 			fos.close();
 			
 			// Save CSS
 			if(sCSS!=null)
 			{
 				fos=new FileOutputStream(new File(fSave,"style.css"));
 				fos.write(sCSS.getBytes("UTF-8"));
 				fos.close();
 			}
 			
 			// Save resources
 			for(Iterator i=mResources.entrySet().iterator();i.hasNext();)
 			{
 				Map.Entry me=(Map.Entry)i.next();
 				fos=new FileOutputStream(new File(fResources,(String)me.getKey()));
 				fos.write( ((Resource) me.getValue()).getContent());
 				fos.close();
 			}			
 			
 			response.setContentType("text/plain");
 			PrintWriter pw=response.getWriter();
 			pw.println(
 				"OK, saved a local copy in 'save' folder within webapp.\n\n" +
 				"Existing contents are cleared when you do that, so don't keep anything there!");
 			pw.close();
 			return;
 		}
 		
 		// Different question
 		if(!bPost)
 		{
 			int iVariant=-1;
 			if(sAfter.startsWith("v"))
 			{
 				iVariant=Integer.parseInt(sAfter.substring(1));
 				sAfter="";
 			}
 			if(sAfter.equals("")) 
 			{
 				resetQuestion();
 				QuestionDefinition qd=qdQuestions.getQuestionDefinition(sQuestion);
 				QuestionDefinition.RunReturn rr=qd.run();
 				qInProgress=rr.q;
 				cclInProgress=rr.ccl;
 				sInProgressID=sQuestion;
 				
 				String sAccess=request.getParameter("access");
 				boolean bPlain="plain".equals(sAccess);
 				double dZoom="big".equals(sAccess) ? 2.0 : 1.0;
 				String sFG="bw".equals(sAccess) ? "#00ff00" : null;
 				String sBG="bw".equals(sAccess) ? "#000000" : null;
 				
 				ipInProgress=new InitParams(System.currentTimeMillis(),
 					sFG,sBG,dZoom,bPlain,cclInProgress,iVariant);
 				Rendering r=qInProgress.init(rr.dMeta,ipInProgress);
 				
 				// Add resources
 				Resource[] arResources=r.getResources();
 				for(int i=0;i<arResources.length;i++)
 				{
 					mResources.put(arResources[i].getFilename(),arResources[i]);
 				}
 				
 				// Set style					
 				sCSS=r.getCSS();
 				
 				// Serve XHTML
 				serveXHTML(sQuestion,r,request,response,qInProgress);
 			}
 			else if(sCSS!=null && sAfter.equals("style.css"))
 			{
 				response.setContentType("text/css");
 				response.setCharacterEncoding("UTF-8");
 				response.getWriter().write(sCSS);
 				response.getWriter().close();
 			}
 			else if(sAfter.startsWith("resources/"))
 			{
 				Resource r=(Resource)mResources.get(sAfter.substring("resources/".length()));
 				if(r==null)
 				{
 					sendError(request,response,
 						HttpServletResponse.SC_NOT_FOUND,"Not found","Requested resource not found: "+sRemainingPath, null);
 				}
 				response.setContentType(r.getMimeType());
 				response.setContentLength(r.getContent().length);
 				if(r.getEncoding()!=null)
 					response.setCharacterEncoding(r.getEncoding());
 				response.getOutputStream().write(r.getContent());
 				response.getOutputStream().close();
 			}
 			else
 			{
 				sendError(request,response,
 					HttpServletResponse.SC_NOT_FOUND,"Not found","Don't know how to handle request: "+sRemainingPath, null);
 				return;
 			}
 		}
 		else
 		{
 			if(!sQuestion.equals(sInProgressID))
 			{
 				sendError(request,response,
 					HttpServletResponse.SC_METHOD_NOT_ALLOWED,
 					"POST not allowed","You cannot change to a different question mid-question (the " +
 					"developer servlet supports only a single session at a time, " +
 					"so don't open multiple browser windows).", null);
 				return;
 			}
 			if(sAfter.length()>0)
 			{
 				sendError(request,response,
 					HttpServletResponse.SC_METHOD_NOT_ALLOWED,
 					"POST not allowed","You cannot POST to any URL other than the question.", null);
 				return;
 			}
 		  
 		  ActionParams ap=new ActionParams();
 		  for(Enumeration e=request.getParameterNames();e.hasMoreElements();)
 		  {
 		  	String sName=(String)e.nextElement();
 		  	ap.setParameter(sName,request.getParameter(sName));
 		  }
 		  if(ipInProgress.isPlainMode()) ap.setParameter("plain","yes");
 		  
 		  ActionRendering ar=qInProgress.action(ap);
 		  
 		  if(ar.isSessionEnd())
 		  {
 				response.setContentType("text/html");
 				response.setCharacterEncoding("UTF-8");
 				PrintWriter pw=new PrintWriter(response.getWriter());
 				pw.println("<html><head><title>Question ended</title></head>" +
 						"<body>Question ended. <a href='./'>Restart</a></body></html>");
 				pw.close();
 		  }
 		  else
 		  {
 				// Add resources
 				Resource[] arResources=ar.getResources();
 				for(int i=0;i<arResources.length;i++)
 				{
 					mResources.put(arResources[i].getFilename(),arResources[i]);
 				}
 				
 				// Set style					
 				if(ar.getCSS()!=null) sCSS=ar.getCSS();
 				
 				// Serve XHTML
 				serveXHTML(sQuestion,ar,request,response,qInProgress);
 		  }
 		}
 	}
 
 	byte[] abTempCSS=null;
 
 	
 	private void handle(boolean bPost,
 		HttpServletRequest request,HttpServletResponse response) 
 	{
 		try
 		{
 			// Vitally important, otherwise any input with unicode gets screwed up
 			request.setCharacterEncoding("UTF-8");
 			
 			String sPath=request.getPathInfo();
 			if(sPath==null || sPath.equals("") || sPath.equals("/"))
 			{
 				// Must access page with / at end
 				if(request.getRequestURI().endsWith("/"))
 				{
 					handleFront(bPost,request,response);
 				}
 				else
 				{
 					response.sendRedirect(request.getRequestURI()+"/");
					return;
 				}
 			}
 			else if(sPath.startsWith("/build/"))
 				handleBuild(sPath.substring("/build/".length()),request,response);
 			else if(sPath.startsWith("/run/"))
 				handleRun(bPost,sPath.substring("/run/".length()),request,response);
 			else
 			{
 				sendError(request,response,HttpServletResponse.SC_NOT_FOUND,
 					"Not found","The URL you requested is not provided by this server.", null);				
 			}
 		}
 		catch(Throwable t)
 		{
 			sendError(request,response,
 				HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Error handling request","An exception occurred.", t);
 		}
 	}
 	
 	/** Remember last xhtml sent so we can save it */
 	private String sLastXHTML;
 	
 	private void serveXHTML(String sQuestion,Rendering r,
 		HttpServletRequest request,HttpServletResponse response,Question q)
 	  throws IOException
 	{
 		// Create basic template
 		Document d=XML.parse(
 			"<xhtml>" +
 			"<head>" +
 			"<title>Question: "+sQuestion+"</title>"+
 			"<link rel='stylesheet' href='style.css' type='text/css'/>"+
 			((new File("c:/hack.css")).exists() 
 				? "<link rel='stylesheet' href='file:///c:/hack.css' type='text/css'/>"
 				: "")+
 			"<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/>"+
 			"<meta http-equiv='imagetoolbar' content='no'/>"+
 			"<script type='text/javascript'>window.isDevServlet=true;</script>"+
 			"</head>"+
 			"<body>"+
 			"<h1 style='font: bold 14px Verdana'>Question: "+sQuestion+" " +
 			  "[<a href='./'>Restart</a> <small>" +
   		  	"<a href='./v0'>0</a> <a href='./v1'>1</a> <a href='./v2'>2</a> " +
   		  	  "<a href='./v3'>3</a> <a href='./v4'>4</a> " +
 			  	"<a href='./?access=plain'>Plain</a> <a href='./?access=bw'>Colour</a> " +
 			  	"<a href='./?access=big'>Big</a>" +
 			  	
 			  	"</small>] " +
 			  "[<a href='../../build/"+sQuestion+"/'>Rebuild</a>] " +
 			  "[<a href='../../'>List</a>] <small>[<a href='./?save'>Save</a>]</small>" +
 			"</h1>"+
 			"<div id='question'/>"+
 			"<pre id='results' style='clear:both'/>"+
 			"<pre id='log'/>"+
 			((new File("c:/hack.js")).exists() 
 				? "<script type='text/javascript' src='file:///c:/hack.js'/>"
 				: "")+
 			"</body>"+
 			"</xhtml>");
 		
 		// Get question top-level element and clone it into new document
 		Element eQuestion=(Element)d.importNode(r.getXHTML(),true);
 		Element eDiv=XML.find(d,"id","question");
 		if(q instanceof StandardQuestion)
 		{
 			double dZoom=((StandardQuestion)q).getZoom();
 			eDiv.setAttribute("style","width:"+Math.round(dZoom * 600)+"px;");
 		}
 		eDiv.appendChild(eQuestion);
 		
 		StringBuffer sbResults=new StringBuffer();
 		if(r instanceof ActionRendering)
 		{
 			Results rResults=((ActionRendering)r).getResults();
 			if(rResults!=null)
 			{
 				sbResults.append("Results\n=======\n\nScores\n------\n\n");
 				Score[] as=rResults.getScores();
 				for(int i=0;i<as.length;i++)
 				{
 					if(as[i].getAxis()==null)
 						sbResults.append("(default axis) ");
 					else
 						sbResults.append("["+as[i].getAxis()+"] ");
 					sbResults.append(as[i].getMarks()+"\n");
 				}
 				sbResults.append(
 					"\nSummaries\n---------\n\n"+
 					"Question: "+XHTML.escape(rResults.getQuestionLine()==null ? "" : rResults.getQuestionLine(),XHTML.ESCAPE_TEXT)+"\n"+
 					"Answer: "+XHTML.escape(rResults.getAnswerLine()==null ? "" : rResults.getAnswerLine(),XHTML.ESCAPE_TEXT)+"\n");
 				sbResults.append(
 					"\nActions\n-------\n\n"+XHTML.escape(rResults.getActionSummary()==null?"":rResults.getActionSummary(),XHTML.ESCAPE_TEXT));
 				XML.createText(XML.find(d,"id","results"),sbResults.toString());
 			}			
 		}
 		
 		if(q instanceof StandardQuestion)
 		{
 			StandardQuestion sq=(StandardQuestion)q;			
 			XML.createText(XML.find(d,"id","log"),sq.eatLog());
 		}
 		
 		// Fix up the replacement variables
 		Map mReplace=new HashMap(getLabelReplaceMap());
 		mReplace.put("RESOURCES","resources");
 		mReplace.put("FORMTARGET","./");
 		mReplace.put("FORMFIELD","");
 		XML.replaceTokens(eQuestion,mReplace);
 	
 		// Update document root
 		d.getDocumentElement().setAttribute("class",UserAgent.getBrowserString(request));
 
 		// Remember
 		StringWriter sw=new StringWriter();
 		XHTML.saveFullDocument(d,sw,false,"en");
 		sLastXHTML=sw.toString();
 				
 		// Whew! Now send to user
 		XHTML.output(d,request,response,"en");		
 	}
 	
 	/** Cache label replacement (Map of String (labelset id) -> Map ) */
 	private Map mLabelReplace=new HashMap();
 	
 	/**
 	 * Returns the map of label replacements appropriate for the current session.
 	 * @param us Session
 	 * @return Map of replacements (don't change this)
 	 * @throws IOException Any problems loading it
 	 */
 	private Map getLabelReplaceMap() throws IOException
 	{
 		String sKey="!default";
 		
 		// Get from cache
 		Map mLabels=(Map)mLabelReplace.get(sKey);
 		if(mLabels!=null) return mLabels;
 		
 		// Load from file
 		Map m=new HashMap();
 		File f=new File(getServletContext().getRealPath("WEB-INF/labels/"+sKey+".xml"));
 		if(!f.exists())
 			throw new IOException("Unable to find requested label set: "+sKey);
 		Document d=XML.parse(f);
 		Element[] aeLabels=XML.getChildren(d.getDocumentElement());
 		for(int i=0;i<aeLabels.length;i++)
 		{
 			m.put(
 					XML.getRequiredAttribute(aeLabels[i],"id"),
 					XML.getText(aeLabels[i]));
 		}
 		
 		// Cache and return
 		mLabelReplace.put(sKey,m);
 		return m;
 	}
 	
 }
