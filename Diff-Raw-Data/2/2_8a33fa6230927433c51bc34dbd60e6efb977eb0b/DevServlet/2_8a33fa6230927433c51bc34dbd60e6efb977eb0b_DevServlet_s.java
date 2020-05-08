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
 import java.net.URLEncoder;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import om.OmException;
 import om.OmVersion;
 import om.question.*;
 import om.stdquestion.StandardQuestion;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.misc.*;
 import util.xml.XHTML;
 import util.xml.XML;
 
 /**
  * Servlet used to build and test questions on a developer machine. Not suitable
  * for any production or public demonstration use. Only supports one instance
  * of one question at a time.
  */
 public class DevServlet extends HttpServlet
 {
 	/** Constant for specifying the number of text boxes for typing package names in the interface. */
 	private final static int NUM_EXTRA_PACKAGE_SLOTS = 3;
 	/** Number of times the question is started, to check that it is using the random seed correctly. */
 	private final static int NUM_REPEAT_INITS = 2;
 	/** In-progress question (null if none) */
 	private Question qInProgress=null;
 
 	/** Classloader for in-progress question */
 	private ClosableClassLoader cclInProgress=null;
 
 	/** ID of in-progress question */
 	private String sInProgressID=null;
 
 	/** Whether the in-progress question has sent back results. */
 	private boolean questionHasSentResults;
 
 	/** Map of String (filename) -> Resource */
 	private Map<String,Resource> mResources=new HashMap<String,Resource>();
 
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
 		questionHasSentResults = false;
 	}
 
 	@Override
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
 
 	@Override
 	protected void doGet(HttpServletRequest request,HttpServletResponse response)
 		throws ServletException,IOException
 	{
 		handle(false,request,response);
 	}
 
 	@Override
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
 			String extraPackages = "";
 			for (int i = 0; i<NUM_EXTRA_PACKAGE_SLOTS; ++i) {
 				String extraPackage = request.getParameter("extra" + i).trim();
 				if (extraPackage.length()>0) {
 					extraPackages += "  <includepackage>"+extraPackage+"</includepackage>\n";
 				}
 			}
 			File fNew=new File(
 				qdQuestions.getQuestionsFolder(),request.getParameter("package")+".xml");
 			Writer w=new OutputStreamWriter(new FileOutputStream(fNew),"UTF-8");
 			w.write(
 				"<questiondefinition>\n" +
 				"  <sourcetree>"+request.getParameter("source")+"</sourcetree>\n" +
 				"  <package>"+request.getParameter("package")+"</package>\n" +
 				extraPackages +
 				"</questiondefinition>\n");
 			w.close();
 			response.sendRedirect(".");
 		}
 
 		QuestionDefinition[] aqd=qdQuestions.getQuestionDefinitions();
 
 		String extraPackagesHtml = "";
 		for (int i = 0; i<NUM_EXTRA_PACKAGE_SLOTS; ++i) {
 			extraPackagesHtml += "<input type='text' name='extra" + i + "' size='65' value='" +
 				((aqd.length>0 && aqd[aqd.length-1].getAdditionalPackageRoots().length>i) ?
 					aqd[aqd.length-1].getAdditionalPackageRoots()[i] : "") + "'/><br />";
 		}
 
 		// Create basic template
 		Document d=XML.parse(
 			"<xhtml>" +
 			"<head>" +
 			"<title>OpenMark-S (Om) question development</title>"+
 			"<style type='text/css'>\n"+
 			"body { font: 12px Verdana, sans-serif; }\n" +
 			"h1 { font: bold 14px Verdana, sans-serif; }\n" +
 			"a { color: black; }\n" +
 			"h2 { font: 14px Verdana, sans-serif; }\n" +
 			"#create,#questionbox { margin-bottom:20px; border:1px solid #888; padding:10px; }\n"+
 			"#create span { float:left; width:20em; margin-top: 5px }\n" +
 			"#create span.fields { width:auto; }\n" +
 			"#create div { clear:left; }\n"+
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
 			"<div><span>Package</span><span class='fields'><input type='text' name='package' size='65' value='"+
 				((aqd.length>0) ? aqd[aqd.length-1].getPackage().replaceAll("\\.[^.]$",".") : "")+
 				"'/></span></div>"+
 			"<div><span>Source tree</span><span class='fields'><input type='text' name='source' size='65' value='" +
 				((aqd.length>0) ? aqd[aqd.length-1].getSourceFolder().getAbsolutePath() : "")+
 				"'/></span></div>"+
 			"<div><span>Extra package (optional)</span><span class='fields'>" + extraPackagesHtml + "</span></div>"+
 			"<div><input type='submit' name='action' id='submit' value='Create'/></div>"+
 			"<p>This creates a new question definition file (.xml) in the questions " +
 			"folder of your Om webapp.</p>"+
 			"</form>"+
 			"</body>"+
 			"</xhtml>");
 
 		// Find the root element and chuck in a line for each question
 		Element eParent=XML.find(d,"id","questions");
 		for(int iQuestion=0;iQuestion<aqd.length;iQuestion++)
 		{
 			String encodedName = URLEncoder.encode(aqd[iQuestion].getID(), "UTF-8");
 			Element
 				eQ=XML.createChild(eParent,"li");
 			XML.createText(eQ," "+aqd[iQuestion].getID()+" ");
 			if(aqd[iQuestion].hasJar())
 			{
 				Element eRun=XML.createChild(eQ,"a");
 				eRun.setAttribute("href","run/"+encodedName+"/");
 				XML.createText(eRun,"(Run)");
 				XML.createText(eQ," ");
 			}
 			Element eBuild=XML.createChild(eQ,"a");
 			eBuild.setAttribute("href","build/"+encodedName+"/");
 			XML.createText(eBuild,"(Build)");
 			XML.createText(eQ," ");
 
 			Element eRemove=XML.createChild(eQ,"a");
 			eRemove.setAttribute("href","remove/"+encodedName+"/");
 			XML.createText(eRemove,"(Remove)");
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
 
 	private void handleRemove(String sRemainingPath,
 			HttpServletRequest request,HttpServletResponse response) throws Exception
 	{
 		String sQuestion=sRemainingPath.replaceAll("^([^/]*)/?.*$","$1");
 		String sAfter=sRemainingPath.replaceAll("^[^/]*/?(.*)$","$1");
 
 		if(!sAfter.equals(""))
 		{
 			sendError(request,response,
 				HttpServletResponse.SC_NOT_FOUND,"Not found","Don't know how to handle request: "+sRemainingPath, null);
 			return;
 		}
 
 		PrintWriter pw = response.getWriter();
 		File toRemove=new File(
 				qdQuestions.getQuestionsFolder(),sQuestion+".xml");
 		if (toRemove.exists()) {
 			if (!toRemove.delete()) {
 				pw.println("Could not remove the XML file.");
 			}
 			File jarToRemove=new File(
 					qdQuestions.getQuestionsFolder(),sQuestion+".jar");
 			if (jarToRemove.exists()) {
 				if (!jarToRemove.delete()) {
 					pw.println("Could not remove the jar file.");
 				}
 			}
 		} else {
 			pw.println("Unknown question.");
 		}
 		response.sendRedirect("..");
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
 			for(Map.Entry<String,Resource> me : mResources.entrySet())
 			{
 				fos=new FileOutputStream(new File(fResources, me.getKey()));
 				fos.write(me.getValue().getContent());
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
 			long randomSeed = System.currentTimeMillis();
 			if(sAfter.startsWith("v"))
 			{
 				iVariant=Integer.parseInt(sAfter.substring(1));
 				sAfter="";
 			}
 			if(sAfter.startsWith("rs"))
 			{
 				randomSeed = Long.parseLong(sAfter.substring(2));
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
 
 				ipInProgress=new InitParams(randomSeed,
 					sFG,sBG,dZoom,bPlain,cclInProgress,iVariant);
 				Rendering r=qInProgress.init(rr.dMeta,ipInProgress);
 
 				// Try starting the question a few times, and ensure we get the same result each time.
 				String xHTML = XML.saveString(r.getXHTML());
 				for (int i = 0; i < NUM_REPEAT_INITS; i++) {
 					Question qCopy = qInProgress.getClass().newInstance();
 					String newXHTML = XML.saveString(qCopy.init(rr.dMeta,ipInProgress).getXHTML());
 
 					if (!xHTML.equals(newXHTML)) {
 						response.setContentType("text/html");
 						response.setCharacterEncoding("UTF-8");
 						PrintWriter pw = new PrintWriter(response.getWriter());
 						pw.println("<html><head><title>Error starting question</title></head><body>");
 						pw.println("<div style='border: 1px solid #888; padding: 1em; background: #fdc; font-weight: bold'>Error: " +
 								"Starting the question twice with the same random seed produced " +
 								"different results. This means there is a bug in your question.</div>");
 						pw.println("<p><a href='../../build/" + sQuestion + "/'>Rebuild</a></p>");
						pw.println("<h2>First verions of the question HTML</h2><pre>");
 						pw.println(XHTML.escape(xHTML, XHTML.ESCAPE_TEXT));
 						pw.println("</pre><h2>Repeat version of the question HTML</h2><pre>");
 						pw.println(XHTML.escape(newXHTML, XHTML.ESCAPE_TEXT));
 						pw.println("</pre>");
 						pw.println("</body></html>");
 						pw.close();
 						return;
 					}
 				}
 
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
 				Resource r=mResources.get(sAfter.substring("resources/".length()));
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
 			for(Enumeration<?> e = request.getParameterNames(); e.hasMoreElements();)
 			{
 				String sName = (String)e.nextElement();
 				ap.setParameter(sName,request.getParameter(sName));
 			}
 			if(ipInProgress.isPlainMode()) ap.setParameter("plain","yes");
 
 			ActionRendering ar=qInProgress.action(ap);
 
 			if(ar.isSessionEnd())
 			{
 				response.setContentType("text/html");
 				response.setCharacterEncoding("UTF-8");
 				PrintWriter pw=new PrintWriter(response.getWriter());
 				pw.println("<html><head><title>Question ended</title></head><body>");
 				if (!questionHasSentResults) {
 					pw.println("<div style='border: 1px solid #888; padding: 1em; background: #fdc; font-weight: bold'>Error: The question ended without sending back any results.</div>");
 					pw.println("<p><a href='./'>Restart</a></p>");
 				} else {
 					pw.println("<p>Question ended. <a href='./'>Restart</a></p>");
 				}
 				pw.println("</body></html>");
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
 			else if(sPath.startsWith("/remove/"))
 				handleRemove(sPath.substring("/remove/".length()),request,response);
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
 			"<form method='post' action='./' id='question' autocomplete='off' class='om'/>"+
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
 				questionHasSentResults = true;
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
 		Map<String,String> mReplace=new HashMap<String,String>(getLabelReplaceMap());
 		mReplace.put("RESOURCES","resources");
 		mReplace.put("IDPREFIX","");
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
 	private Map<String,Map<String,String> > mLabelReplace=new HashMap<String,Map<String, String> >();
 
 	/**
 	 * Returns the map of label replacements appropriate for the current session.
 	 * @param us Session
 	 * @return Map of replacements (don't change this)
 	 * @throws IOException Any problems loading it
 	 */
 	private Map<String, String> getLabelReplaceMap() throws IOException
 	{
 		String sKey="!default";
 
 		// Get from cache
 		Map<String, String> mLabels=mLabelReplace.get(sKey);
 		if(mLabels!=null) return mLabels;
 
 		// Load from file
 		Map<String, String> m=new HashMap<String, String>();
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
