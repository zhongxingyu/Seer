 package org.makumba.parade.view.managers;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringWriter;
 
 import org.apache.log4j.Logger;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.view.interfaces.FileEditorView;
 
 public class FileEditViewManager implements FileEditorView {
 
 	static Logger logger = Logger.getLogger(FileEditViewManager.class.getName());
 	
 	public String getFileEditorView(Row r, String path, File file, String[] source) {
 		StringWriter result = new StringWriter();
 		PrintWriter out = new PrintWriter(result);
 		
 		java.io.File f= new java.io.File(file.getPath());
 		java.io.File d;
 		String content="";
 		
 		if (source != null) {
 			content = source[0];
 
 			// we save
 			if (f.getParent() != null) {
 				d = new java.io.File(f.getParent());
 				d.mkdirs();
 			}
 			try {
 				f.createNewFile();
 				boolean windows = System.getProperty("line.separator").length() > 1;
 				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
 				for (int i = 0; i < content.length(); i++) {
 					if (windows || content.charAt(i) != '\r')
 						pw.print(content.charAt(i));
 				}
 				pw.close();
 			} catch (IOException e) {
 				logger.error("Error while creating file ",e);
 			}
 			
 		}
 		else {
 			// we read the file
 			if (f.exists()) {
 				Reader rd;
 				try {
 					rd = new BufferedReader(new FileReader(f));
 					int c;
 					StringBuffer sb = new StringBuffer();
 					while ((c = rd.read()) != -1) {
 						sb.append((char) c);
 					}
 					content = sb.toString();
 				} catch (FileNotFoundException e) {
 					logger.error(e);
 				} catch (IOException e) {
 					logger.error(e);
 				}
 			}
 		}
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < content.length(); i++)
 			if (content.charAt(i) == '<')
 				sb.append("&lt;");
 			else if (content.charAt(i) == '&')
 				sb.append("&amp;");
 			else
 				sb.append(content.charAt(i));
 		content = sb.toString();
 
 		
 		
 		out.println(
 "<html><head>\n"+
 "<title>"+file.getName()+" - ParaDe editor</title>\n"+
 
 "<SCRIPT LANGUAGE=\"JavaScript\">\n"+
 "<!--\n"+
 "// Javascript code for automatically resizing textarea\n"+
 "// Written by David W. Jeske and contributed to the public domain.\n"+
 
 "function onResize() {\n"+
 "  resizeTA(document.sourceEdit.source);\n"+
 "}\n"+
 
 "function resizeTA(TA) {\n"+
 "  var winW, winH;\n"+
 "  var usingIE = 0;\n"+
 
 "  // these paramaters have to match the font you specify with your\n"+
 "  // style tag on the textarea.\n"+
 "  var fontMetricWidth = 7;\n"+
 "  var fontMetricHeight = 14;\n"+
 
 "  // you don't want this smaller than 1,1\n"+
 "  var minWidthInCols = 20;\n"+
 "  var minHeightInRows = 7;\n"+
 
 "  // offset fudge factors.\n"+
 "  // Making these bigger makes the textarea smaller.\n"+
 "  var leftOffsetFudge = 40;\n"+
 "  var topOffsetFudge = 20;\n"+
 
 
 "  if (parseInt(navigator.appVersion)>3) {\n"+
 "    if (navigator.appName==\"Netscape\") {\n"+
 "     winW = window.innerWidth;\n"+
 "      winH = window.innerHeight;\n"+
 "    }\n"+
 "    if (navigator.appName.indexOf(\"Microsoft\")!=-1) {\n"+
 "      winW = document.body.offsetWidth;\n"+
 "      winH = document.body.offsetHeight;\n"+
 "      usingIE = 1;\n"+
 "    }"+
 "  }"+
 
 "  if (! usingIE ) {\n"+
 "    return; // this javascript below does not work for netscape\n"+
 "  }\n"+
 
 " // this code computes the upper-left corner offset\n"+
 "  // by walking all the elements in the html page\n"+
 "  toffset = 0;\n"+
 "  loffset = 0;\n"+
 "  offsetobj = TA;\n"+
 "  while (offsetobj) {\n"+
 "    toffset += offsetobj.offsetTop + offsetobj.clientTop;\n"+
 "    loffset += offsetobj.offsetLeft + offsetobj.clientLeft;\n"+
 "    offsetobj = offsetobj.offsetParent;\n"+
 "  }\n"+
 
 " // compute and set the width\n"+
 "  var overhead = loffset + leftOffsetFudge;\n"+
 "  var ta_width = ((winW - overhead))  / fontMetricWidth;\n"+
 "  if (ta_width < minWidthInCols) {\n"+
 "    ta_width = minWidthInCols;\n"+
 "  }"+
 "  TA.cols = ta_width;\n"+
 
 
 "  // compute and set the height\n"+
 "  var overhead = toffset + topOffsetFudge;\n"+
 "  var ta_height = (winH - overhead) / fontMetricHeight;\n"+
 "  if (ta_height < minHeightInRows) {\n"+
 "    ta_height = minHeightInRows;\n"+
 " }\n"+
 "  TA.rows = ta_height;\n"+
 "}\n"+
 
 "function onLoad() {\n"+
 "  onResize();"+
 "  document.sourceEdit.source.focus();\n"+
 "  document.sourceEdit.pagestatus.value=\"Loaded.\";\n"+
 "  document.sourceEdit.Submit.disabled=true;\n"+
 "}\n"+
 
 // http://www.webreference.com/dhtml/diner/beforeunload/bunload4.html
 "function unloadMess(){\n"+
 "    mess = \"You have unsaved changes.\"\n"+
 "    return mess;\n"+
 "}\n"+
 
 "function setBunload(on){\n"+
 "    window.onbeforeunload = (on) ? unloadMess : null;\n"+
 "}\n"+
 
 // to be called when the content of the big textarea changes
 "function setModified(){\n"+
 "    document.sourceEdit.pagestatus.value=\"MODIFIED\";\n"+
 "    document.sourceEdit.Submit.disabled=false;\n"+
 "    setBunload(true);\n"+
 "}\n"+
 
 "//-->\n"+
 "</script>\n"+
 
 "</head>\n"+
 
 "<body bgcolor=\"#dddddd\" TOPMARGIN=0 LEFTMARGIN=0 RIGHTMARGIN=0 BOTTOMMARGIN=0 marginwidth=0 marginheight=0 STYLE='margin: 0px' "+
 "onload=\"javascript:onLoad()\" onresize=\"javascript:onResize()\">\n"+
 "<form name=\"sourceEdit\" method=\"post\" action=\"edit?context="+r.getRowname()+"&path="+path+"&file="+f.getPath()+"\" style='margin:0px;'>\n"+
 
 "<input type=\"submit\" name=\"Submit\" value='(S)ave!' ACCESSKEY='S' onclick=\"javascript:setBunload(false)\">\n"+
 "<a href='/?context="+r.getRowname()+"' target='_top' title='"+r.getParade().getBaseDir()+"'>\n"+
 (r.getRowname().equals("")?"(root)":r.getRowname())+
 "</a>:<a href=/servlet/file?context="+r.getRowname()+"&path="+r.getRowpath()+path+">"+path.replace(java.io.File.separatorChar,'/')+"</a>/<b>"+
 file.getName()+"</b>\n"+
 "<br>\n"+
 
 "<textarea name=\"source\" style=\"width:100%;height:92%\" cols=\"90\" rows=\"23\" wrap=\"virtual\"\n"+
 "onKeyPress=\"javascript:setModified()\" STYLE=\"font-face:Lucida Console; font-size:8pt\">"+content+"</textarea>"+
 
 "</form>\n"+
 "</body>\n"+
 "</html>"
 
 		);
 		
 		return result.toString();
 	}
 	
 }
