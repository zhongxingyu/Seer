 package com.hotteststudio.model;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.siegmann.epublib.domain.Book;
 import nl.siegmann.epublib.domain.Identifier;
 import nl.siegmann.epublib.domain.MediaType;
 import nl.siegmann.epublib.domain.Metadata;
 import nl.siegmann.epublib.domain.Resource;
 import nl.siegmann.epublib.domain.Resources;
 import nl.siegmann.epublib.domain.Spine;
 import nl.siegmann.epublib.domain.SpineReference;
 import nl.siegmann.epublib.domain.TOCReference;
 import nl.siegmann.epublib.epub.EpubReader;
 import nl.siegmann.epublib.service.MediatypeService;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.hotteststudio.epubreader.MainActivity;
 import com.hotteststudio.epubreader.Reader;
 import com.hotteststudio.util.XCommon;
 
 public class GenBookHTML {
 	
 	//public String epubFile;
 	public EpubReader epub;
 	public Book book;
 	public Spine spine;
 	public String bookUUID;
 	
 	// content of epub
 	public StringBuilder finalHTML;
 	public String resultHTML = "epubtemp.html";
 	public String allcontent = "";	
 	public String folderName = "";
 	public String styleChapter = "";
 	public String bookData = "";
 	public String finalPathFile = "";
 	
 	public ArrayList<String> arrSrc;
 	public ArrayList<String> arrTitle;
 	public ArrayList<String> arrChapter;
 	public ArrayList<String> arrLinkChapter;
 	public Setting setting;
 	
 	public GenBookHTML(InputStream file, String _folderName) {
 		try {
 			epub = new EpubReader();
 			book = epub.readEpub(file);
 			spine = book.getSpine();
 			arrChapter = new ArrayList<String>();
 			arrLinkChapter = new ArrayList<String>();
 			logTableOfContents(book.getTableOfContents().getTocReferences(), 0);
 			setting = new Setting();
 			
 			folderName = _folderName;
 			
 			try {
 				resultHTML = XCommon.deAccent(folderName) + ".html";
 			} catch (Exception ee) {
 				Log.d("GenBookHTML","Convert file name error: " + ee);
 			}
 			
 			arrSrc = new ArrayList<String>();
 			arrTitle = new ArrayList<String>();
 			
 			finalPathFile = XCommon.getRootPath() + folderName + "/" + resultHTML;
 			
 			// xoa file nay neu da ton tai;
 			File f = new File(finalPathFile);
 			if (f.exists()) {
 				f.delete();
 			}
 			
 			LoadEpub load = new LoadEpub();
 			load.execute();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private class LoadEpub extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				
 				loadImage();
 				//extractHTML();
 				getChapterConfig();
 				//styleChapter = styleChapter + "#chapID {display: none;}";
 				
 				Log.d("hihi", arrChapter.size() + " & " + bookData.split(",").length);
 				
 				genEbookHTML();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			Reader.progressDialog.dismiss();
 			Reader.webview.loadUrl("file://" + finalPathFile);
 			
 			saveToRecentList();
 		}
 		
 	}
 	
 	public String genHTML() {
 		
 		String html = "<head>\n" 
 		  + "<meta http-equiv='Content-Type' content='Type=text/html; charset=utf-8'>"
 		  +"<!-- Include the Monocle library and styles -->"
 		  +"\n<script src=\"file:///android_asset/monocore.js\"></script>"
 		  +"\n<script src=\"file:///android_asset/monoctrl.js\"></script>"
 		  +"\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/monocore.css\" />"
 		  +"\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/monoctrl.css\" />"
 			  +"\n<style>"
 			    +"#reader { width: 100%; height: 100%; border: 0px solid #000; }"
 			    +"@font-face {font-family: 'Times New Roman'; src:url('file:///android_asset/fonts/times.ttf') format('truetype'); font-weight: normal; font-style: normal;}"
 			    +"body * {text-align:justify !important;font-family: 'Times New Roman' !important;font-style:normal !important; text-decoration:none !important;}"
 			    +"div,p,span,* {text-align:justify !important;}"
 			  +"</style>"
 		+"\n</head>\n"
 			  + genBookData() 
 		+"\n<body style='margin:0px; padding:0px;'>"
 		  +"\n<div id=\"reader\">"
 		  +"</div>"
 		  
 		+"\n</body>";
 		
 		return html;
 	}
 	
 	public String genBookData() {
 		String component = genComponent();
 		String components = genComponents();
 		String content = genContents();
 		String metadata = genMetaData();
 		
 		String temp = "<script type=\"text/javascript\">"
 			
 			+"var bookData={\n"
 				+ components + " \n"
 				+ content + " \n"
 				+ component + " \n"
 				+ metadata + " \n"
 			+"} \n"
 			+ "var scrubber;"
 			+ "var toc;"
 			+ "var magnifier;"
 			+"Monocle.Events.listen(window, 'load', " +
 				"function () {" +
 				//"	window.reader = Monocle.Reader('reader', bookData,{flipper: Monocle.Flippers.Slider, panels:Monocle.Panels.Marginal});" +
 					"var placeSaver = new Monocle.Controls.PlaceSaver('placesaver');"+
 					"Monocle.Reader('reader', bookData, {flipper: Monocle.Flippers.Slider, place: placeSaver.savedPlace()}, function (rdr) {"
 						+ "window.reader1 = rdr;"
 						+ "magnifier = new Monocle.Controls.Magnifier(rdr);"
 						+ "rdr.addControl(magnifier, 'standard', { hidden: true });"
 						+ "scrubber = new Monocle.Controls.Scrubber(rdr);"
 						+ "rdr.addControl(scrubber, 'standard', { hidden: true });"
 						+ "toc = Monocle.Controls.Contents(rdr);"
 						+ "rdr.addControl(toc, 'modal', { hidden: true });"
 						+ "rdr.addControl(placeSaver, 'invisible');"
 						//+ "rdr.showControl(scrubber);"
 						//+ "rdr.showControl(toc);"	
 						//+ "Monocle.Events.listen('reader', 'monocle:componentloaded', function() { alert('hihi');} );"
 					+ "});" +
 					
 				"} );"
 			+ "function showTOC() {"
 				+"if (window.reader1.showingControl(toc)==true) {"
 					+"window.reader1.hideControl(toc);"
 				+"} else {window.reader1.showControl(toc);}"
 			+ "}"
 			
 			+ "function showProgressbar() {"
 				+"if (window.reader1.showingControl(scrubber)==true) {"
 					+"window.reader1.hideControl(scrubber);"
 					+"window.reader1.hideControl(magnifier);"
 				+"} else {window.reader1.showControl(scrubber);" 
 					+"window.reader1.showControl(magnifier);"
 					+"}"
 			+ "}"
 			
 			+ "</script>"
 			
 			;
 		return temp;
 	}
 	
 	public String genComponent() {
 		return "getComponent: function (componentId) {return {url:componentId};},";
 	}
 	
 	public String genContents() {
 		String content = "getContents: function () {return [";
 		int t = 0;
 		for (int i = 0; i< arrSrc.size(); i++) {
 			String strChap = "";
 			
 			//Log.d("huhu",arrSrc.get(i) + " - - " +arrLinkChapter.get(t));
 			if (arrSrc.get(i).equals( arrLinkChapter.get(t)) ) {
 				strChap = arrChapter.get(t);
 				strChap = strChap.replace("\"", "'");
 				t++;
 			} else {
 				strChap = "Info " + i;
 			}
 			
 			content = content + "{title:\"" + strChap + "\",src: \"" + arrSrc.get(i) + "\"},";
 		}
 		content = content + "]},";
 		
 		return content;
 	}
 	
 	public String genComponents() {
 		String components = "getComponents: function () {return [";
 		for (String src : arrSrc) {
 			components = components + "'" + src +"',";
 		}
 		components = components + "];},";
 		return components;
 	}
 	
 	public String genMetaData() {
 		return "getMetaData: function(key) {return {title: \"A book\",creator: \"Inventive Labs\"}[key];}";
 	}
 	
 	public void saveToFile() {
 		try {
 			String localfile = XCommon.getRootPath() + folderName + "/" + resultHTML;
 			FileWriter f = new FileWriter(new File(localfile), false);
 			BufferedWriter writer = new BufferedWriter(f);
 			writer.write(finalHTML.toString());
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void loadImage() {
 		MediaType[] bitmapTypes = { MediatypeService.PNG, MediatypeService.GIF, MediatypeService.JPG };
 		Resources resource = book.getResources();
 		List<Resource> bitmapResouces = resource.getResourcesByMediaTypes(bitmapTypes);
 		
 		saveImage(bitmapResouces);
 	}
 	
 	private void saveImage(List<Resource> bitmapResouces) {
 
 		String localfile = XCommon.getRootPath() + folderName + "/Images";
 		File f = new File(localfile);
 		
 		boolean b;
 		if (!f.exists()) {
 			b = f.mkdirs();
 		}
 
 		try {
 			for (Resource rr : bitmapResouces) {
 				
 				InputStream is = rr.getInputStream();
 				String tempfile = rr.getHref();
 				Log.d("zzzzzz",tempfile);
 				try {
 					tempfile = tempfile.substring(tempfile.lastIndexOf("/"));
 				} catch (Exception e) {
 					e.printStackTrace();
 					Log.d("fak saveImage",">>> " + tempfile);
 				}
 				//tempfile = tempfile.replaceAll("/", "");
 				File file = new File(localfile, tempfile);
 				if (file.exists()) return;
 				Log.d("dddddddddd",tempfile);
 				
 			    OutputStream output = new FileOutputStream(file);
 			    try {
 			        
 		            final byte[] buffer = new byte[1024];
 		            int read;
 
 		            while ((read = is.read(buffer)) != -1) {
 		                output.write(buffer, 0, read);
 		            }
 
 		            output.flush();
 		        
 		            output.close();
 			        
 			    } catch (Exception e) {
 			        e.printStackTrace();
 			    }
 			    is.close();
 			    
 			    //Log.d("yeah","finish");
 	
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String decodeHTML(String html) {
 		String finalPath = XCommon.getRootPath() + folderName + "/Images";
 		
 		
 		html = html.replace("%20", " ");
 		html = html.replace("alt=\"\" src=\"../", "alt=\"\" src=\"file://" + finalPath);
 		
 		Document doc = Jsoup.parse(html);
 		Elements eles = doc.getElementsByTag("img");
 		for (Element element : eles) {
 			try {
 				String str = element.attr("src");
 				Log.d("mm$$", "xxx " + str);
 				str = str.substring(str.lastIndexOf("/"));
 				element.attr("src", finalPath + str);
 				Log.d("mm", "@@@ " + finalPath + str);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		Elements elesXHref = doc.getElementsByTag("image");
 		for (Element element : elesXHref) {
 			String str = element.attr("xlink:href");
 			str = str.substring(str.lastIndexOf("/"));
 			element.attr("src", finalPath + str);
 			Log.d("mm", "@@@ " + finalPath + str);
 		}
 		
 		return html;
 	}
 	
 	public void extractHTML() {
 		try {
 			int i=0;
 			String finalPath = XCommon.getRootPath();
 			
 			for (SpineReference bookSection : spine.getSpineReferences()) {
 			    Resource res = bookSection.getResource();
 			    
 			    String href = res.getHref();
 			    String title = res.getTitle();
 	        	String content = new String(res.getData(), "UTF-8");
 	        	
 	        	href = href.replace("/", "");
 	        	href = href.replace(" ", "");
 	        	href = href.replace(".xhtml", ".html");
 	        	content = content.replace("%20", " ");
 	        	
 	        	content = content.replace("alt=\"\" src=\"../", "alt=\"\" src=\"file://" + (finalPath + folderName + "/") );
 	        	
 	        	Document doc = Jsoup.parse(content);
 			    String content2 = doc.getElementsByTag("body").html();
 	        	
 			    // xu li charset, css o day 
 			    // <meta http-equiv='Content-Type' content='Type=text/html; charset=utf-8'>
 			    
 	        	arrSrc.add(href);
 	        	arrTitle.add(title);
 	        	// remove later
 	        	i++;
 	        	try {
 	        		File fol = new File(XCommon.getRootPath() + folderName);
 	        		boolean b = fol.mkdirs();
 	        		
 	        		
 	        		
 	        		String localfile = fol + "/" + href;
 	        		
 	        		File temp = new File(localfile);
 
 	    			FileWriter f = new FileWriter(temp, false);
 	    			
 	    			
 	    			
 	    			BufferedWriter writer = new BufferedWriter(f);
 	    			//writer.write(content); // do xu li cai vu remove the html thua`
 	    			writer.write(content2);
 	    			writer.close();
 
 	    		} catch (Exception e) {
 	    			e.printStackTrace();
 	    		}
 	        	
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void getUUID() {
 		List<Identifier> listIden = book.getMetadata().getIdentifiers();
 		
 		for (Identifier identifier : listIden) {
 			bookUUID = identifier.getValue();
 			if (bookUUID.contains("urn:uuid:")) {
 				bookUUID = bookUUID.replace("urn:uuid:", "");
 				Log.d("jjjj", bookUUID);
 				break;
 			}
 		}
 	}
 	
 	public void logTableOfContents(List<TOCReference> tocReferences, int depth) {
 	    if (tocReferences == null) {
 	      return;
 	    }
 	    for (TOCReference tocReference : tocReferences) {
 	      StringBuilder tocString = new StringBuilder();
 	      for (int i = 0; i < depth; i++) {
 	        tocString.append("\t");
 	      }
 	      tocString.append(tocReference.getTitle());
 	      //Log.d("epublib chapter", tocReference.getCompleteHref());
 	      //arrChapters.add(new BookChapter(tocReference.getCompleteHref(), tocString.toString(), 0));
 	      arrChapter.add(tocString.toString());
 	      String s = tocReference.getCompleteHref().replace("/", "");
 	      arrLinkChapter.add(s);
 	      
 	      // update
 	      /*String chapId = XCommon.deAccent(tocString.toString().replace(" ", ""));
 	      styleChapter = styleChapter + "#" + chapId + ",";
 	      bookData = bookData + "'" + chapId + "', ";
 	      Log.d("chap ne>>",styleChapter);
 	      Log.d("bookData ne>>",bookData);*/
 	      
 	      logTableOfContents(tocReference.getChildren(), depth + 1);
 	    }
 	  }
 	
 	
 	public void saveTextToFile(String text, boolean append) {
 		try {
 			File f = new File(finalPathFile);
 			if (f.exists() == false) {
 				f.createNewFile();
 			}
 			FileWriter fw = new FileWriter(f, append);
 			BufferedWriter writer = new BufferedWriter(fw);
 			writer.write(text);
 			writer.flush();
 			writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// xu li epub file o day
 	
 	public StringBuilder genEbookHTML() {
 		StringBuilder sb = new StringBuilder();
 		//StringBuilder content = fullContent();
 		
 		sb.append("<head>"); 
 		sb.append("\n<meta http-equiv='Content-Type' content='Type=text/html; charset=utf-8'>");
 		sb.append("\n<script src=\"file:///android_asset/monocore.js\"></script>");
 		sb.append("\n<script src=\"file:///android_asset/monoctrl.js\"></script>");
 		sb.append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/monocore.css\" />");
 		sb.append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/monoctrl.css\" />");
 		sb.append("\n<style type=\"text/css\">");
 		sb.append("\n#reader { position: relative; width: 100%; height: 100%; border: 0px solid #000; }");
 		sb.append(styleChapter);
 		sb.append("\nbody * {font-style:normal !important; text-decoration:none !important; line-height:2 !important; font-weight:normal !important; font-size: 100% !important;}");
 	    //sb.append("body * {font-family: 'Athelas' !important; font-size: 100% !important;}");
 	    sb.append("\nbody h1:first-of-type {font-weight:bold !important; font-size: 200% !important; }");
 	    sb.append("\np {margin-top: 1em !important;}");
 	    sb.append("\na,ins {text-decoration:none !important;}");
 	    sb.append("\nspan {text-align:justify !important;}");
 	    sb.append("\n</style>");
 	    
 	    sb.append("\n<script type=\"text/javascript\">");
 	    sb.append("\nvar scrubber;");
 	    sb.append("\nvar toc;");
 	    sb.append("\nvar magnifier;");
 	    sb.append("\n");
 	    sb.append("\nvar bookData = Monocle.bookDataFromIds([" + bookData  + "]);");
 	    //sb.append("\nMonocle.Events.listen(window,'load',function () { window.reader = Monocle.Reader('reader',bookData, { flipper: Monocle.Flippers.Slider }); });");
 	    sb.append("\nMonocle.Events.listen(window,'load',function () {");
 	    sb.append("\nvar placeSaver = new Monocle.Controls.PlaceSaver('placesaver');");
 	    sb.append("\nMonocle.Reader('reader',bookData, { flipper: " + setting.getFlipper(2) + " , place: placeSaver.savedPlace() } , ");
 	    sb.append("\nfunction (rdr) {");
 	    sb.append("\nwindow.reader1 = rdr;");
 	    sb.append("\nmagnifier = new Monocle.Controls.Magnifier(rdr);");
 	    sb.append("\nrdr.addControl(magnifier, 'standard', { hidden: true });");
 	    sb.append("\nscrubber = new Monocle.Controls.Scrubber(rdr);");
 	    sb.append("\nrdr.addControl(scrubber, 'standard', { hidden: true });");
 	    sb.append("\ntoc = Monocle.Controls.Contents(rdr);");
 	    sb.append("\nrdr.addControl(toc, 'modal', { hidden: true });");
 	    sb.append("\nrdr.addControl(placeSaver, 'invisible');");
 	    sb.append("\n");
 	    sb.append("\n});");
 	    sb.append("\n});");
 	    
 	    // add some support js here
 	    sb.append("\nfunction showProgressbar() {");
 	    sb.append("\nif (window.reader1.showingControl(scrubber)==true) {");
 	    sb.append("\nwindow.reader1.hideControl(scrubber);");
 	    sb.append("\nwindow.reader1.hideControl(magnifier);");
 	    sb.append("\n} else {window.reader1.showControl(scrubber);");
 	    sb.append("\nwindow.reader1.showControl(magnifier);}}");
 	    sb.append("\n");
 	    sb.append("\nfunction goToChapter(chapterId) {");
 	    sb.append("\nwindow.reader1.moveTo(window.reader1.properties.book.locusOfChapter(chapterId));");
 	    sb.append("\n}");
 	    sb.append("\n");
 	    sb.append("\n");
 	  
 	    
 	    sb.append("\n</script>");
 
 	    sb.append("\n</head>");
 	    sb.append( "\n<body>\n<div id=\"reader\"></div>\n");
 	    
 	    saveTextToFile(sb.toString(), true);
 	    fullContent();
 	
 	    saveTextToFile("\n</body>", true);	
 	    
 		return sb;
 	}
 	
 	private void getChapterConfig() {
 		try {
 			int i = 0;
 			for (SpineReference bookSection : spine.getSpineReferences()) {
 				i++; 
 				String strChapID = "chap" + i;
 				styleChapter = styleChapter + "#" + strChapID + ",";
 	        	bookData = bookData + "'" + strChapID + "', ";
 	        	Log.d("GenChapter",bookSection.toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		styleChapter = styleChapter + "#chapID {display: none;}";
 	}
 	
 	private void getChapterConfig2() {
 		try {
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		styleChapter = styleChapter + "#chapID {display: none;}";
 	}
 	
 	
 	public StringBuilder fullContent() {
 		StringBuilder temp = new StringBuilder();
 		try {
 			int i = 0;
 			for (SpineReference bookSection : spine.getSpineReferences()) {
 				i++; 
 			    Resource res = bookSection.getResource();
 			    
 			   
 			    StringBuilder content = new StringBuilder();
 			    content.append(new String(res.getData(), "UTF-8"));
 	        	
 			    replaceAll(content, "%20", " ");
 			    replaceAll(content, "alt=\"\" src=\"../", "alt=\"\" src=\"file://" + (XCommon.getRootPath() + folderName + "/"));
 			    
 			    // xu li vu chi lay body content
 			    Document doc = Jsoup.parse(content.toString());
 			    StringBuilder content2 = new StringBuilder();
 			    
 			    // remove style co san
 			    Elements els = doc.select("style");
 			    for(Element e: els){
 			        e.remove();
 			    }
 			    
 			    // remove empty tags
 			    for (Element element : doc.select("*")) {
 			        if (!element.hasText() && element.isBlock()) {
 			        	if (element.getElementsByTag("img")!=null && element.getElementsByTag("img").size()<1)
 			        		element.remove();
 			        }
 			    }
 			    
 			    content2.append(doc.getElementsByTag("body").html());
 			    
 //	        	StringBuilder nameid = new StringBuilder();
 //	        	nameid.append(res.getHref());
 //			    replaceAll(nameid, "/", "");
 //			    replaceAll(nameid, " ", "");
 //			    replaceAll(nameid, ".xhtml", "");
 //			    replaceAll(nameid, ".html", "");
 //			    replaceAll(nameid, ".htm", "");
 //			    replaceAll(nameid, ".", "");
 			    //Log.d("here", ">>" + nameid);
 			    StringBuilder strData = new StringBuilder();
 			    String style = "text-align:justify !important;" +
 			    				"font-size:" + setting.fontSize + "px !important;" +
 			    				"line-height:2 !important; font-weight:normal !important;"
 			    		;
 			    String strChapID = "chap" + i;
 			    saveTextToFile("\n<div id=\"" + strChapID  + "\" class='chapter-container' style='" + style +"' >\n", true);
 	        	
 	        	saveTextToFile(decodeHTML(content2.toString()), true);
 	        	saveTextToFile("\n</div>", true);
 			}
 			Log.d("final string >>" , ">>>" + temp);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		//styleChapter = styleChapter + "#chapID {display: none;}";
 		return temp;
 	}
 	
 	private void replaceAll(StringBuilder builder, String from, String to)
 	{
 	    int index = builder.indexOf(from);
 	    while (index != -1)
 	    {
 	        builder.replace(index, index + from.length(), to);
 	        index += to.length(); 
 	        index = builder.indexOf(from, index);
 	    }
 	}
 	
 	// lay ve thong tin metadata cua sach
 	public EpubInfo getBookInfo() {
 		EpubInfo bm = new EpubInfo();
 		Metadata metadata = book.getMetadata();
 		getUUID();
 		if (metadata.getTitles().size()>0)
 		bm.title = metadata.getTitles().get(0).toString();
 		
 		if (metadata.getCoverImage() != null)
 		bm.coverImg = metadata.getCoverImage().getHref();
 		bm.coverImg = bm.coverImg.substring(bm.coverImg.lastIndexOf("/")+1);
 		//Log.i("hihi",">> " + bm.coverImg);
 		
 		if (metadata.getAuthors().size()>0)
 		bm.author = metadata.getAuthors().get(0).getFirstname() + " " + metadata.getAuthors().get(0).getLastname();
 		
 		if (metadata.getPublishers().size()>0)
 		bm.publisher = metadata.getPublishers().get(0).toString();
 		
 		bm.uuid = bookUUID;
 		//bm.uuid = metadata.get
 		//bm.publisher = metadata.getRights().get(0).toString();
 		//bm.subject = metadata.getSubjects().get(0).toString();
 		
 		
 		//Log.d("dayne",bm.creator + " " + bm.coverImg + " " + bm.rights + " " + bm.publisher);
 		
 		return bm;
 	}
 	
 	// save to xml for the selected book
 	public void saveToRecentList() {
 		EpubInfo e = getBookInfo();
 		e.path = Reader.EPUB_PATH;
 		
 		// xu li trung epubuuid
 		for (EpubInfo ei : MainActivity.settings.arrRecentEpub) {
 			if (ei.uuid.equals(e.uuid) && ei.path.equals(e.path)) {
 				return;
 			}
 		}
 		MainActivity.settings.arrRecentEpub.add(e);
 		String strXML = MainActivity.xmlHandler.xstream.toXML(MainActivity.settings);
 		XCommon.saveTextToFile(XCommon.XML_FILE, strXML, false);
 		//Log.d("day ne",">>> " + strXML);
 	}
 	
 }
 
