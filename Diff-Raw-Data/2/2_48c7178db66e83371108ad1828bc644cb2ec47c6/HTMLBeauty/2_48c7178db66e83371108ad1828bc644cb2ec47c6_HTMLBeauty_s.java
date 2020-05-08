 package net.idea.restnet.c.html;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Writer;
 
 import net.idea.restnet.c.AbstractResource;
 import net.idea.restnet.c.ResourceDoc;
 import net.idea.restnet.c.resource.TaskResource;
 
 import org.restlet.Request;
 import org.restlet.data.Form;
 import org.restlet.data.Method;
 import org.restlet.data.Reference;
 
 public class HTMLBeauty {
 	protected static String jsGoogleAnalytics = null;
 	
 	public void writeHTMLHeader(Writer w,String title,Request request,ResourceDoc doc) throws IOException {
 		writeHTMLHeader(w, title, request,"",doc);
 	}
 	public void writeHTMLHeader(Writer w,String title,Request request,String meta,ResourceDoc doc) throws IOException {
 
 		writeTopHeader(w, title, request, meta,doc);
 		writeSearchForm(w, title, request, meta);
 		
 	}
 
 	public String jsGoogleAnalytics() {
 		if (jsGoogleAnalytics==null) try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
					getClass().getClassLoader().getResourceAsStream("ambit2/rest/config/googleanalytics.js"))
 			);
 			StringBuilder b = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) {
             	b.append(line);
             	b.append('\n');
             }
             jsGoogleAnalytics = b.toString();
 			reader.close();
 			
 		} catch (Exception x) { jsGoogleAnalytics = null;}
 		return jsGoogleAnalytics;
 	}
 	
 	public void writeTopHeader(Writer w,String title,Request request,String meta,ResourceDoc doc) throws IOException {
 		Reference baseReference = request==null?null:request.getRootRef();
 		w.write(
 				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
 			);
 		
 		w.write(String.format("<html %s %s %s>",
 				"xmlns=\"http://www.w3.org/1999/xhtml\"",
 				"xmlns:dc=\"http://purl.org/dc/elements/1.1/\"",
 				"xmlns:ot=\"http://opentox.org/api/1.1/\"")
 				);
 		
 		w.write(String.format("<head> <meta property=\"dc:creator\" content=\"%s\"/> <meta property=\"dc:title\" content=\"%s\"/>",
 				request.getResourceRef(),
 				title
 				)
 				);
 		
 		Reference ref = request.getResourceRef().clone();
 		ref.addQueryParameter("media", Reference.encode("application/rdf+xml"));
 		w.write(String.format("<link rel=\"meta\" type=\"application/rdf+xml\" title=\"%s\" href=\"%s\"/>",
 				title,
 				ref
 				)); 
 		
 		w.write(String.format("<link rel=\"primarytopic\" type=\"application/rdf+xml\" href=\"%s\"/>",
 				ref
 				)); 		
 		//<link rel="primarytopic" href="http://opentox.org/api/1_1/opentox.owl#Compound"/>
 		
 		w.write(String.format("<title>%s</title>",title));
 		
 		w.write(String.format("<script type=\"text/javascript\" src=\"%s/jquery/jquery-1.4.2.min.js\"></script>\n",baseReference));
 		w.write(String.format("<script type=\"text/javascript\" src=\"%s/jquery/jquery.tablesorter.min.js\"></script>\n",baseReference));
 		w.write(meta);
 				
 		w.write(String.format("<link href=\"%s/style/ambit.css\" rel=\"stylesheet\" type=\"text/css\">",baseReference));
 		w.write("<meta name=\"robots\" content=\"index,nofollow\"><META NAME=\"GOOGLEBOT\" CONTENT=\"index,noFOLLOW\">");
 		w.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
 		//w.write(String.format("<script type=\"text/javascript\" src=\"%s/js/dojo.js.uncompressed\" djConfig=\"parseOnLoad:true, isDebug:true\"></script>\n",baseReference));
 
 		//w.write(String.format("<script type=\"text/javascript\" src=\"%s/jme/jme.js\"></script>\n",baseReference));
 
 		//w.write("<script>function changeImage(img,src)  {    document.getElementById(img).src=src;} </script>\n");
 
 		w.write("</head>\n");
 		w.write("<body>");
 		w.write(String.format("<link rel=\"stylesheet\" href=\"%s/style/tablesorter.css\" type=\"text/css\" media=\"screen\" title=\"Flora (Default)\">",baseReference));
 		w.write("\n");
 		w.write("<div style= \"width: 100%; background-color: #516373;");
 		w.write("border: 1px solid #333; padding: 0px; margin: 0px auto;\">");
 		w.write("<div class=\"spacer\"></div>");
 		
 		String top;
 		if (doc!= null) {
 			top = String.format("&nbsp;<a style=\"color:#99CC00\" href='%s' target='_Ontology' title='Opentox %s (%s), describes representation of OpenTox REST resources'>OpenTox %s</a>&nbsp;",
 					doc.getPrimaryTopic(),
 					doc.getResource(),doc.getPrimaryTopic(),doc.getResource());
 			top += String.format("<a style=\"color:#99CC00\" href='%s' target='_API' title='REST API documentation'>REST API</a>&nbsp;",doc.getPrimaryDoc());
 		} else top = "";
 
 		w.write(String.format("<div class=\"row\"><span class=\"left\">&nbsp;%s",top));
 		w.write("</span>");
 	
 		
 		w.write(String.format("	<span class=\"right\">%s&nbsp;<a style=\"color:#99CC00\" href='%s/%s'>%s</a>",
 				top,
 				baseReference.toString(),
 				getLoginLink(),
 				request.getClientInfo().getUser()==null?"Login":"My account"));
 		
 		
 		//w.write(String.format("&nbsp;<a href=\"%s/help\">Help</a>",baseReference.toString()));
 		w.write("</span></div>");
 		w.write("	<div class=\"spacer\"></div>");
 		w.write("</div>");
 		w.write("<div>");		
 		
 		//w.write(String.format("<a href='%s/ttc?text=50-00-0&search=%s' title='Threshold of toxicological concern prediction'>TTC</a>&nbsp;",baseReference,Reference.encode("C=O")));
 		//w.write(String.format("<a href='%s/query/compound/search/all'>Query compounds</a>&nbsp;",baseReference));
 		//w.write(String.format("<a href='%s/compound'>Chemical&nbsp;compounds</a>&nbsp;",baseReference));
 
 		//w.write(String.format("<a href='%s/dataset?max=25'>Datasets</a>&nbsp;",baseReference));
 		//w.write(String.format("<a href='%s/algorithm' title='Predictive algorithms'>Algorithms</a>&nbsp;",baseReference));
 		//w.write(String.format("<a href='%s/model' title='Models'>Models</a>&nbsp;",baseReference));
 		//w.write(String.format("<a href='%s%s'>References</a>&nbsp;",baseReference,ReferenceResource.reference));
 
 		//w.write(String.format("<a href='%s/query/similarity?search=c1ccccc1Oc2ccccc2&threshold=0.9' title='Search for similar structures'>Similarity</a>&nbsp;",baseReference));
 		//w.write(String.format("<a href='%s/query/smarts?text=\"\"' title='Substructure search by SMARTS patterns'>Substructure</a>&nbsp;",baseReference));
 
 		
 		//w.write(String.format("&nbsp;<a href='http://toxpredict.org' title='Predict'>ToxPredict</a>&nbsp;"));
 		//w.write(String.format("<a href='%s/depict?search=c1ccccc1' title='Structure diagram'>Depiction</a>&nbsp;",baseReference));
 		//w.write(String.format("<a href='%s/depict/reaction?search=c1ccccc1' title='SMIRKS test'>Reactions</a>&nbsp;",baseReference));
 
 	
 		writeTopLinks(w, title, request, meta, doc, baseReference);
 		//w.write(String.format("&nbsp;<a href='%s/help'>Help</a>&nbsp;",baseReference));
 
 		w.write("</div>");
 
 		w.write("\n<div id=\"targetDiv\"></div>\n");
 		w.write("\n<div id=\"statusDiv\"></div>\n");
 		//w.write("\n<textarea id=\"targetDiv\"></textarea>\n");
 	}
 	
 	public void writeTopLinks(Writer w,String title,Request request,String meta,ResourceDoc doc, Reference baseReference) throws IOException {
 		w.write(String.format("<a href='%s%s'>Tasks</a>&nbsp;",TaskResource.resource,baseReference));
 	}
 	public void writeSearchForm(Writer w,String title,Request request ,String meta) throws IOException {
 		writeSearchForm(w, title, request, meta,Method.GET);
 	}
 	
 	protected Form getParams(Form params,Request request) {
 		if (params == null) 
 			if (Method.GET.equals(request.getMethod()))
 				params = request.getResourceRef().getQueryAsForm();
 			//if POST, the form should be already initialized
 			else 
 				params = request.getEntityAsForm();
 		return params;
 	}
 	public void writeSearchForm(Writer w,String title,Request request ,String meta,Method method) throws IOException {
 		writeSearchForm(w, title, request, meta,method,null);
 	}
 	
 	protected String getLogoURI(String root) {
 		return String.format("%s/images/ambit-logo.png",root==null?"":root);
 	}
 
 	protected String getHomeURI() {
 		return "/";
 	}
 	
 	public void writeSearchForm(Writer w,String title,Request request ,String meta,Method method,Form params) throws IOException {
 		Reference baseReference = request==null?null:request.getRootRef();
 		w.write("<table width='100%' bgcolor='#ffffff'>");
 		w.write("<tr>");
 		w.write("<td align='left' width='256px'>");
 		w.write(String.format("<a href=\"%s\"><img src='%s' width='256px' alt='%s' title='%s' border='0'></a>\n",
 						getHomeURI(),getLogoURI(baseReference.toString()),getTitle(),baseReference));
 		w.write("</td>");
 		w.write("<td align='center'>");
 		String query_smiles = "";
 		try {
 			Form form = getParams(params,request);
 			if ((form != null) && (form.size()>0))
 				query_smiles = form.getFirstValue(AbstractResource.search_param);
 			else query_smiles = null;
 		} catch (Exception x) {
 			query_smiles = "";
 		}
 		w.write(String.format("<form action='' method='%s'>\n",method));
 		w.write(String.format("<input name='%s' size='80' value='%s'>\n",AbstractResource.search_param,query_smiles==null?"":query_smiles));
 		w.write("<input type='submit' value='Search'><br>");
 		//w.write(baseReference.toString());
 
 		w.write("</form>\n");
 		w.write("<br><b title='RESTNet demo web services'</b>");		
 		w.write("</td>");
 		w.write("<td align='right' width='256px'>");
 //		w.write(String.format("<a href=\"http://opentox.org\"><img src=\"%s/images/logo.png\" width=\"256\" alt=\"%s\" title='%s' border='0'></a>\n",baseReference,"AMBIT",baseReference));
 
 		w.write("</td>");
 		w.write("</tr></table>");		
 		
 		
 		
 		w.write("<hr>");
 		
 	}	
 	public void writeHTMLFooter(Writer output,String title,Request request) throws IOException {
 		Reference baseReference = request==null?null:request.getRootRef();
 		
 		output.write("<div class=\"footer\">");
 
 		output.write("<span class=\"right\">");
 		//output.write(String.format("<a href='http://www.cefic.be'><img src=%s/images/logocefic.png border='0' width='115' height='60'></a>&nbsp;",baseReference));
 		//output.write(String.format("<a href='http://www.cefic-lri.org'><img src=%s/images/logolri.png border='0' width='115' height='60'></a>&nbsp;",baseReference));
 		//output.write(String.format("<a href='http://www.opentox.org'><img src=%s/images/logo.png border='0' width='115' height='60'></a>",baseReference));
 		output.write("<br>Developed by Ideaconsult Ltd. (2005-2012)"); 
 		output.write("  <A HREF=\"http://validator.w3.org/check?uri=referer\">");
 		output.write(String.format("    <IMG SRC=\"%s/images/valid-html401-blue-small.png\" ALT=\"Valid HTML 4.01 Transitional\" TITLE=\"Valid HTML 4.01 Transitional\" HEIGHT=\"16\" WIDTH=\"45\" border=\"0\">",baseReference));
 		output.write("  </A>&nbsp; ");
 		output.write("<A HREF=\"http://jigsaw.w3.org/css-validator/check/referer\">");
 		output.write(String.format("    <IMG SRC=\"%s/images/valid-css-blue-small.png\" TITLE=\"Valid CSS\" ALT=\"Valid CSS\" HEIGHT=\"16\" WIDTH=\"45\" border=\"0\">",baseReference));
 		output.write("  </A>");
 
 		output.write("</span>");		
 		output.write("</div>");
 		output.write("\n");
 		output.write(jsGoogleAnalytics()==null?"":jsGoogleAnalytics());
 		output.write("</body>");
 		output.write("</html>");
 
 	}	
 	public String getLoginLink() {
 		return "opentoxuser";
 	}
 	public String getTitle() {
 		return "RESTNet";
 	}
 	
 	
 	public String printWidgetHeader(String header) {
 		return	String.format(
 				"<div class=\"ui-widget \" style=\"margin-top: 20px; padding: 0 .7em;\">\n"+
 				"<div class=\"ui-widget-header ui-corner-top\"><p>%s</p></div>\n",header);
 	}
 	public String printWidgetFooter() {
 		return	String.format("</div>\n");
 	}
 	public String printWidgetContentHeader(String style) {
 		return	String.format("<div class=\"ui-widget-content ui-corner-bottom %s\">\n",style);
 	}
 	public String printWidgetContentFooter() {
 		return	String.format("</div>\n");
 	}	
 	public String printWidgetContentContent(String content) {
 		return
 		String.format("<p>%s</p>\n",content);
 	}	
 	public String printWidgetContent(String content,String style) {
 		return String.format("%s\n%s\n%s",
 				printWidgetContentHeader(style),
 				printWidgetContentContent(content),
 				printWidgetContentFooter());
 	}
 	
 	
 	public String printWidget(String header,String content,String style) {
 		return String.format("%s\n%s\n%s",
 				printWidgetHeader(header),
 				printWidgetContent(content,style),
 				printWidgetFooter());
 
 	}
 	
 	public String printWidget(String header,String content) {
 		return String.format("%s\n%s\n%s",
 				printWidgetHeader(header),
 				printWidgetContent(content,""),
 				printWidgetFooter());
 
 	}	
 }
