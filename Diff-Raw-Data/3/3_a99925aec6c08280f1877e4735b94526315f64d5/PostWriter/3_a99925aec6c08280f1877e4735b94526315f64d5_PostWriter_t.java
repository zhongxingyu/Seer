 package uk.ac.lancs.e_science.jsf.components.blogger;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.sakaiproject.util.ResourceLoader;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlForm;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.servlet.http.HttpServletRequest;
 
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Creator;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.LinkRule;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostElement;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostUtilities;
 import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;
 
 
 
 public class PostWriter {
 	
 	private UIComponent uicomponent;
 	private String contextPath;
 	private ResourceLoader messages;
 	private ResponseWriter writer;
 	private String formClientId; 
 	
 	public PostWriter( FacesContext context, UIComponent uicomponent){
 		this.uicomponent = uicomponent;
 		writer = context.getResponseWriter();
         messages = new ResourceLoader("uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages");
 		HttpServletRequest req =((HttpServletRequest)context.getExternalContext().getRequest());
 		contextPath = req.getContextPath();
 		formClientId = getFormClientId(uicomponent, context);
 		
 	}
 	
 	public void printFullContent(Post post, boolean writeComments, boolean linkInTitle, boolean showCreator) throws IOException{
 
 		//writer.write("<br/>");
 		writeHeaderForFullConent(writer,post, linkInTitle, showCreator);
 		//writer.write("<br/>");
 		writeElementsForFullContent(writer,post);
 		if (writeComments){
 			writer.write("<br/>");
 			writeComments(writer,post);
 		}
 	}
 	public void printShortContent(Post post, boolean writeComments, boolean showCreator) throws IOException{
 		this.writeHeaderForFullConent(writer, post, true, showCreator);
 		/*
 		writer.startElement("table",uicomponent);
 		writer.writeAttribute("cellpading","0",null);
 		writer.writeAttribute("cellspacing","0",null);
 		writer.writeAttribute("width","100%",null);
 		writer.startElement("tr",uicomponent);
 		
 		writer.startElement("td",uicomponent);
 		writeTitleAndAuthorAndDate(writer,post,true,"aTitleHeader",showCreator);
 		writer.endElement("td");
 		
 		writer.endElement("tr");
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		*/
 		if (post.getShortText()!=null && !(post.getShortText().equals("")))
 		{
 			/*
 			writer.startElement("span",uicomponent);
 			writer.writeAttribute("class","spanShortText",null);
 			writer.write(post.getShortText());
 			writer.endElement("span");
 			*/
 		}
 		else {
 			PostUtilities util = new PostUtilities();
 			String text =util.getFirstParagraphOrNFirstCharacters(post,400);
 			writer.write(text);
 		}
 		/*
 		writer.endElement("td");
 		writer.endElement("tr");
 		
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("class","tdGap",null);
 		writer.write("&nbsp;");
 		writer.endElement("td");
 		writer.endElement("tr");
 		writer.endElement("table");
 		*/
 		if (writeComments){
 			writer.write("<br/>");
 			writeComments(writer,post);
 		}
 	}	
 	
 	
 	//private void writeHeaderForFullConent(ResponseWriter writer,Post post, boolean linkInTitle, boolean showCreator) throws IOException{
 	private void writeHeaderForFullConent(ResponseWriter writer,Post post, boolean linkInTitle, boolean showCreator) throws IOException{
 		
 		writer.startElement("table",uicomponent);
 		writer.writeAttribute("cellpading","0",null);
 		writer.writeAttribute("cellspacing","0",null);
 		writer.writeAttribute("width","100%",null);
 		
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		writeTitleAndAuthorAndDate(writer,post,linkInTitle,"aTitleHeader", showCreator);
 		writer.endElement("td");
 		writer.endElement("tr");
 
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		//writer.write("<br/>");
 		writer.endElement("td");
 		writer.endElement("tr");
 		
 		if (post.getShortText()!=null && !(post.getShortText().trim().equals(""))){
 			//writeGap(writer);
 			writer.startElement("tr",uicomponent);
 			writer.startElement("td",uicomponent);
 			writer.startElement("span",uicomponent);
 			writer.writeAttribute("class","spanShortText",null);
 			writer.write(post.getShortText());
 			writer.endElement("span");
 			writer.endElement("td");
 			writer.endElement("tr");
 		}
 		writeGap(writer);
 		
 		writer.endElement("table");
 		
 	}
 	private void writeTitleAndAuthorAndDate(ResponseWriter writer, Post post, boolean linkInTitle, String titleStyle, boolean showCreator) throws IOException{
 		writer.startElement("table",uicomponent);
 		writer.writeAttribute("width","100%",null);
 		writer.writeAttribute("cellpading","0",null);
 		writer.writeAttribute("cellspacing","0",null);
 		writer.startElement("tr",uicomponent);
 		//td
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("class","tdTitle",null);
 		if (!linkInTitle){
 			writer.startElement("span",uicomponent);
 			writer.writeAttribute("class","spanTitle",null);
 			writer.write(post.getTitle());
 			writer.endElement("span");
 		} else {
 			writer.startElement("a",uicomponent);
 			writer.writeAttribute("href","#",null);
 			writer.writeAttribute("class",titleStyle,null);
 			writer.writeAttribute("onClick","javascript:document.getElementById('idSelectedPost').value='"+post.getOID()+"';document.forms['"+formClientId+"'].submit();",null);
 			writer.writeAttribute("class","spanTitle",null);
 			writer.write(post.getTitle());
 			writer.endElement("a");
 			
 		}
 		writer.endElement("td");
 		if (showCreator){
 			writer.startElement("td",uicomponent);
 			writer.writeAttribute("class","tdAuthor",null);
 			Creator creator = post.getCreator();
 			if (creator!=null){
 				writer.startElement("span",uicomponent);
 				writer.writeAttribute("style","font-size:12px; font-family:Verdana, Arial, Helvetica, sans-serif",null);
 				writer.write(creator.getDisplayName());
 				/*
 				Date date = new Date(post.getDate());
 				writer.write(" ("+DateFormat.getDateInstance(DateFormat.SHORT).format(date)+")");
 				*/
 				writer.endElement("span");
 			}
 			writer.endElement("td ");
 		}
 			writer.startElement("td",uicomponent);
 		writer.writeAttribute("align","right", null);
 		writer.startElement("span",uicomponent);
 		Date date = new Date(post.getDate());
 		writer.write(" ("+DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(date)+")");
 		writer.endElement("span");
 			writer.endElement("td ");
 		
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("align","right", null);
 		if (post.getState().isPrivate())
 			writer.write("<img src='img/private.gif' alt='p'/>");
 		else {
 			if (post.getState().getAllowComments())
 				writer.write("<img src='img/commentsAllowed.gif' alt='c'/>");
 			if (!post.getState().getReadOnly())
 				writer.write("<img src='img/pencil.gif' alt='w'/>");
 		}
 		writer.endElement("td");
 		
 		writer.endElement("tr");
 		writer.endElement("table");
 		
 	}
 	private void writeGap(ResponseWriter writer) throws IOException{
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("class","tdGap",null);
 		writer.write("&nbsp;");
 		writer.endElement("td");
 		writer.endElement("tr");
 		
 	}
 
 	private void writeElementsForFullContent(ResponseWriter writer, Post post) throws IOException{
 		PostElement[] elements = post.getElements();
 		if (elements!=null){
 			writer.startElement("table",uicomponent);
 			writer.writeAttribute("class","mainTable",null);
 			writer.writeAttribute("width","100%",null);
 			writer.writeAttribute("cellpading","0",null);
 			writer.writeAttribute("cellspacing","0",null);
 			for (int i=0;i<elements.length;i++){
 				PostElement element = elements[i];
 				writer.startElement("tr",uicomponent);
 				writer.startElement("td",uicomponent);
 				if (element instanceof Paragraph)
 					writeParagraph(writer, (Paragraph) element);
 				if (element instanceof Image)
 					writeImage(writer, (Image) element);
 				if (element instanceof LinkRule)
 					writeLinkRule(writer, (LinkRule)element);
 				if (element instanceof File)
 					writeFile(writer, (File)element);
 
 				writer.endElement("td"); 
 				writer.endElement("tr");
 				
 				writer.startElement("tr",uicomponent);
 				writer.startElement("td",uicomponent); //uicomponent is a gap between paragraphs and images
 				writer.write("<br/>");
 				writer.endElement("td"); 
 				writer.endElement("tr");
 			}
 			writer.endElement("table");//---------------------------------- 0#
 		}
 	}
 	private void writeParagraph(ResponseWriter writer, Paragraph paragraph) throws IOException{
 		writer.startElement("table",uicomponent);
 		writer.writeAttribute("cellpading","0",null);
 		writer.writeAttribute("cellspacing","0",null);
 		writer.writeAttribute("width","100%",null);
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 
 		writer.write(paragraph.getText());
 
 		writer.endElement("td");
 		writer.endElement("tr");
 		writer.endElement("table");
 		
 	}
 	private void writeImage(ResponseWriter writer, Image image) throws IOException{
 		writer.startElement("table",uicomponent);
 		writer.writeAttribute("width","100%",null);
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("style","text-align:center",null);
 		
 		StringBuilder onClick =new StringBuilder();
 		onClick.append("javascript:");
 		onClick.append("var win = window.open('',\"Call\",\"width=500,height=450,status=no,resizable=yes,scrollbars=1\");");
 		onClick.append("var doc = win.document;");
 		onClick.append("doc.writeln('<html>');");
 		onClick.append("doc.writeln('<body>');");
 		onClick.append("doc.writeln('<img src="+contextPath+"/servletForImages?idImage="+image.getIdImage()+"&size=original></img>');");
 		onClick.append("doc.writeln('</body>');");
 		onClick.append("doc.writeln('</html>');");
 		onClick.append("doc.close();");
 		onClick.append("return false;");
 		
 		writer.startElement("input",uicomponent);
 		writer.writeAttribute("type","image",null);
 		writer.writeAttribute("src",contextPath+"/servletForImages?idImage="+image.getIdImage()+"&size=websize",null);
 		writer.writeAttribute("onClick",onClick,null);
 		writer.endElement("input");
 		
 		writer.endElement("td");
 		writer.endElement("tr");
 		writer.endElement("table");
 	}
 	
 	private void writeFile(ResponseWriter writer, File file) throws IOException{
 		
 		Blogger blogger = BloggerManager.getBlogger();
 		File fileInDB = blogger.getFile(file.getIdFile());
 		if (fileInDB!=null){
 			String link = contextPath+"/servletForFiles?fileId="+file.getIdFile()+"&fileDescription="+file.getDescription(); 
 			
 			StringBuilder onClick =new StringBuilder();
 			onClick.append("javascript:");
 			onClick.append("var win = window.open('"+link+"',\"File\",\"width=600,height=550,menubar=yes,toolbar=yes,status=yes,scrollbars=yes,location=yes,resizable=yes\");");
 			onClick.append("var doc = win.document;");
 			onClick.append("doc.close();");
 			onClick.append("return false;");
 			
 			writer.startElement("a",uicomponent);
 			writer.writeAttribute("href",link,null);
 			writer.writeAttribute("onClick",onClick,null);		
 			writer.write(file.getDescription());
 			writer.endElement("a");
 		} else{
  			writer.write("<i>"+file.getDescription()+" ("+messages.getString("nonAvailableInPreview")+")</i>");
 		}
 	}	
 	
 	private void writeLinkRule(ResponseWriter writer, LinkRule link) throws IOException{
 		StringBuilder onClick =new StringBuilder();
 		onClick.append("javascript:");
 		onClick.append("var win = window.open('"+link.getLinkExpression()+"',\"Call\",\"width=600,height=5 50,menubar=yes,toolbar=yes,status=yes,scrollbars=yes,location=yes,resizable=yes\");");
 		onClick.append("var doc = win.document;");
 		onClick.append("doc.close();");
 		onClick.append("return false;");
 		
 		writer.startElement("a",uicomponent);
 		writer.writeAttribute("href",link.getLinkExpression(),null);
 		writer.writeAttribute("onClick",onClick,null);		
 		writer.write(link.getDescription());
 		writer.endElement("a");
 	}	
 	private void writeComments(ResponseWriter writer, Post post) throws IOException{
 		if (post.getComments()==null || post.getComments().length==0)
 			return;
 		writer.startElement("table",null);
 		writer.writeAttribute("cellpading","0",null);
 		writer.writeAttribute("cellspacing","0",null);
 		writer.writeAttribute("width","100%",null);
 		writer.startElement("tr",uicomponent);
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("class","tdCommentHeader",null);
 		writer.writeAttribute("colspan","2",null);
 		writer.write(messages.getString("postComments"));
 		writer.endElement("td");
 		writer.startElement("td",uicomponent);
 		writer.writeAttribute("class","tdCommentHeader",null);
 		writer.endElement("td");
 		writer.endElement("tr");
 		
 		for (int i=0;i<post.getComments().length;i++){
 			
 			Comment comment = post.getComments()[i];
 			writer.startElement("tr",uicomponent);
 			writer.startElement("td",uicomponent);
 			writer.writeAttribute("width","100px",null);
 			writer.writeAttribute("class","tdComment tdComment1",null);
 			writer.write(comment.getCreator().getDisplayName());
 			writer.write("<br/>");
 			Date date = new Date(comment.getDate());
			writer.write(" ("+DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(date)+")");
			//writer.write(" ("+DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)+")");
 			writer.endElement("td");
 			writer.startElement("td",uicomponent);
 			writer.writeAttribute("class","tdComment tdComment2",null);
 			writer.write(comment.getText());
 			writer.endElement("td");
 			writer.endElement("tr");
 		}
 		writer.endElement("table");
 	}
 	
 	
 	private String getFormClientId(UIComponent component,FacesContext context){
 		if (component==null)
 			return null;
 		UIComponent parent = component.getParent();
 		if (parent == null)
 			return null;
 		if (parent instanceof HtmlForm)
 			return parent.getClientId(context);
 		return getFormClientId(parent,context);
 			
 
 	}
 }
