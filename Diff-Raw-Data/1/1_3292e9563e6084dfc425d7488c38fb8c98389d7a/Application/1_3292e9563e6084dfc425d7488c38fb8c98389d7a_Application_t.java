 package controllers;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import model.Message;
 import net.htmlparser.jericho.Attributes;
 import net.htmlparser.jericho.OutputDocument;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.Util;
 import org.apache.commons.io.IOUtils;
 import play.Logger;
 import play.cache.Cache;
 import play.data.Form;
 import play.libs.Comet;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Result;
 import views.html.index;
 import views.html.deps;
 import views.html.messages;
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Expr;
 import com.avaje.ebean.PagingList;
 
 public class Application extends Controller {
 
     private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22";
 	private static final int PAGE_MAX = 10;
 
 	private static Integer getPageNumber() {
 		try {
 			String p = request().getQueryString("p");
 			if(p != null) {
 				int i = Integer.parseInt(request().getQueryString("p"));
 				return i>0?i:0;
 			}
 		} catch (Exception e) {
 		}
 		return 0;
 	}
 	
 	public static Result index() {
         return ok(index.render(getList(null, getPageNumber())));
     }
     
 	private static List<Message> getListByHost(String host, int page) {
 		PagingList<Message> pl = Message.find.setMaxRows(PAGE_MAX).fetch("tags").where().eq("host", host).eq("parent", null).orderBy("timestamp desc").findPagingList(PAGE_MAX);
 		flash("total", pl.getTotalPageCount()+"");
 		Http.Context.current().args.put("pages", new Integer[pl.getTotalPageCount()]);
 		return pl.getPage(page).getList();
 	}
 	
 	private static List<Message> getListByTag(Long tagId, int page) {
 		PagingList<Message> pl = Message.find.setMaxRows(PAGE_MAX).fetch("tags").where().eq("tags.id", tagId).eq("parent", null).orderBy("timestamp desc").findPagingList(PAGE_MAX);
 		flash("total", pl.getTotalPageCount()+"");
 		Http.Context.current().args.put("pages", new Integer[pl.getTotalPageCount()]);
 		return pl.getPage(page).getList();
 	}
 	
     private static List<Message> getList(String search, int page) {
     	String uuid=session("uuid");
     	if(uuid==null) {
     		uuid=java.util.UUID.randomUUID().toString();
     		session("uuid", uuid);
     	}    	
 
     	if(search == null) {
     		PagingList<Message> pl = Message.find.setMaxRows(PAGE_MAX).fetch("tags").where().eq("parent", null).orderBy("timestamp desc").findPagingList(PAGE_MAX);
     		flash("total", pl.getTotalPageCount()+"");
     		Http.Context.current().args.put("pages", new Integer[pl.getTotalPageCount()]);
     		return pl.getPage(page).getList();
     		
     		// return Message.find.setMaxRows(PAGE_MAX).fetch("tags").where().eq("parent", null).orderBy("timestamp desc").findList();
     		/*
 	    	List<Message> list = (List<Message>) Cache.get(uuid+"list");
 	    	if(list == null) {
 	    		list = Message.find.setMaxRows(PAGE_MAX).fetch("tags").where().eq("parent", null).orderBy("timestamp desc").findList();
 	    		Cache.set(uuid+"list", list);
 	    	} else {
 	    		Logger.info("get CACHED");
 	    	}
 	    	return list;
 	    	*/
     	} else {
     		PagingList<Message> pl = Message.find.setMaxRows(PAGE_MAX).fetch("tags").where().or(Expr.or(Expr.ilike("title","%"+search+"%"), Expr.ilike("description","%"+search+"%")), Expr.ilike("url","%"+search+"%")).eq("parent", null).orderBy("timestamp desc").findPagingList(PAGE_MAX);
     		flash("total", pl.getTotalPageCount()+"");
     		Http.Context.current().args.put("pages", new Integer[pl.getTotalPageCount()]);
     		return pl.getPage(page).getList();
     	}
     }
     
     public static Result list(String search) {
     	Cache.remove(session("uuid")+"list"); 
     	Logger.info("SEARCH:"+search);
     	return ok(messages.render(getList(search, getPageNumber())));
     }
     
     public static Result search(String search) {
    	flash("search",search);
     	return ok(index.render(getList(search, getPageNumber())));
     }
     
     public static Result searchByHost(String host) {
     	return ok(index.render(getListByHost(host, getPageNumber())));
     }
     
     public static Result searchByTagId(Long tagId) {
     	return ok(index.render(getListByTag(tagId, getPageNumber())));
     }
     
     public static Result asyncPost() {
     	final Message l;
     	Logger.info("asyncPost");
     	Form<Message> m = Form.form(Message.class);
     	final Form f = m.bindFromRequest();
     	final String e;
     	if(f.hasErrors()) {
     		String e1 = "";
     		for(Object key: f.errors().keySet()) {
     			e1 += f.error((String) key);
     		}
     		e = e1;
     		flash("error", e1);
     		f.fill(new Message());
     		// return badRequest(index.render(f, Message.find.all()));
     		//return badRequest(f.errors().values()+"");
     		l = null;
     	} else {
     		l = m.bindFromRequest().get();
     		e = "";
     	}
     	
     	/*
     	Chunks<String> chunks = new StringChunks() {
 			
 			@Override
 			public void onReady(Chunks.Out<String> out) {
 				try {
 					add(l, out);
 					out.write("<script>alert('ssss');</script>");
 					out.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		};
 		response().setContentType("text/html");
 		return ok(chunks);
 		*/
 		
 		Comet comet = new Comet("parent.log") {
 		    public void onConnected() {
 		    	try {
 		    		if(l == null) {
 		    			sendMessage(e);
 		    			close();
 		    			return;
 		    		}
 					add(l, this);
 					sendMessage("DONE");
 					close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		    }
 		  };
     		
 		//response().setContentType("text/html");
 		return ok(comet);
     }
 
     public static String toBase64(URL url) throws IOException {
 		URLConnection c = url.openConnection();
 		InputStream in = c.getInputStream();
 		// return "data:"+c.getContentType()+";base64,"+org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(IOUtils.toByteArray(in));
 		return "data:"+c.getContentType()+";base64,"+org.apache.commons.codec.binary.Base64.encodeBase64String(IOUtils.toByteArray(in));
 		/*
 		File f = File.createTempFile(UUID.randomUUID().toString(), "");
 		FileOutputStream o = new FileOutputStream(f);
 		IOUtils.copy(in, o);
 		o.close();	
 		return f;
 		*/
     }
     
     /**
      * sciaga plik i zwraca gotowy obiekt message
      * @param url1
      * @return
      * @throws IOException
      */
     private static Message downloadFile(String url1, Comet comet) throws IOException {
     	Message l = new Message();
     	URL url = new URL(url1);
 		URLConnection c = url.openConnection();
 		c.addRequestProperty("User-Agent", DEFAULT_USER_AGENT);
 		InputStream in = c.getInputStream();
 
     	/**
     	 * wada:
     	 * jak jest przekierowanie Status 302, 
     	 * to np. zamiast zwrocic png, zwroci stronke html
     	 * 
     	 * obsluga 302
     	 */
 		Integer code = null;
 		String location302 = null;
 		if(c instanceof HttpURLConnection) {
 			HttpURLConnection hc = (HttpURLConnection) c;
 			code = hc.getResponseCode();
 			if(code != null && code == 302) {
 				location302 = hc.getHeaderField("Location");
 				url1 = location302;
 				in.close();
 				url = new URL(location302);
 				c = url.openConnection();
 				c.addRequestProperty("User-Agent", DEFAULT_USER_AGENT);
 				in = c.getInputStream();
 			}
 			Logger.info(code + " " + url1);
 			if(comet != null) comet.sendMessage(code + " " + url1);
 		}
 		
 		l.setUrl(url1);
 		l.setContentType(c.getContentType());
 		l.setContentEncoding(c.getContentEncoding());
 		l.setHeaderFields(c.getHeaderFields());
 		File f = File.createTempFile(UUID.randomUUID().toString(), "");		
 		f.deleteOnExit();
 		FileOutputStream o = new FileOutputStream(f);
 		IOUtils.copy(in, o);
 		o.close();
 		in = new FileInputStream(f);
 		l.setData(IOUtils.toByteArray(in));
 		in.close();
 		return l;
     }
     
 	private static void add(Message l, Comet  comet) throws IOException {
 				
 		if(comet != null) comet.sendMessage("adding...");
 		Logger.info("adding...");
 		URL url = new URL(l.getUrl());
 		URLConnection c = url.openConnection();
 		c.addRequestProperty("User-Agent", DEFAULT_USER_AGENT);
 		InputStream in = c.getInputStream();
 		l.setContentType(c.getContentType());
 		l.setContentEncoding(c.getContentEncoding());
 		l.setHeaderFields(c.getHeaderFields());
 		l.setHost(new URL(l.getUrl()).getHost());
 
 		/*
 		File f = File.createTempFile(UUID.randomUUID().toString(), "");
 		f.deleteOnExit();
 		Logger.info(f.getAbsolutePath());
 		FileOutputStream o = new FileOutputStream(f);
 		IOUtils.copy(in, o);
 		o.close();
 		*/
 		
 		/*
 		if(c.getContentType().startsWith("text/html")) {
 			List<Message> m = parse(f, l.getUrl());
 		}
 		*/
 		
 		if(c.getContentType().startsWith("text/html")) {
 			Object[] o1 = process(c, l, comet);
 			OutputDocument doc = (OutputDocument) o1[0];
 			List<Message> deps = (List<Message>) o1[1];
 			l.setData(doc.toString().getBytes());
     		Long mainId = Ebean.createSqlQuery("select nextval('message_seq') as seq, currval('message_seq')").findUnique().getLong("seq");
     		l.setId(mainId);
     		if(l.getTitle() == null) {
     			l.setTitle(l.getUrl());
     		}
 			l.save();
 			in.close();
 
 			/**
 			 * update deps
 			 */
 			if(!deps.isEmpty()) {
 				Logger.debug(deps+"");
 				Logger.info("DEPENDENCIES count: "+deps.size());
 				if(comet != null) comet.sendMessage("DEPENDENCIES count: "+deps.size());
 	    		String docString = doc.toString();
 	    		
 				Message main = Message.find.byId(mainId);
 				int i = 0;
 				for(Message d: deps) {
 					d.setParent(main);
 					Long dId = Ebean.createSqlQuery("select nextval('message_seq') as seq, currval('message_seq')").findUnique().getLong("seq");
 					d.setId(dId);
 					d.save();
 		    		docString = docString.replaceAll("##"+i+"##", "/open/"+dId);
 					i++;
 				}
 
 				// update main
 				main.setData(docString.getBytes());
 				main.update();
 			}
 		} else {
 			// in = new FileInputStream(f);
 			l.setTitle(l.getUrl());
 			l.setData(IOUtils.toByteArray(in));
 			/* bo jest cascade=CascadeType.ALL lub CascadeType.PERSIST
     		for(Tag t: l.getTags()) {
     			t.save();
     		}
     		*/
 			l.save();
 			in.close();
 		}
 		
 		//Logger.debug(f.getAbsolutePath() + (f.delete() ? " deleted":" not deleted"));
 	}
 	
 	// lista powiazanych plikow, plik zrodlowy bedzie musial miec zmienione odnosniki
 	private static List<Message> parse(File f, String sourceUrlString) {
 		return null;
 	}
 	
 	/**
 	 * [0] - content (OutputDocument)
 	 * [1] - deps
 	 * @param f
 	 * @param sourceUrlString
 	 * @param comet
 	 * @return
 	 */
 	private static Object[] process(URLConnection c, Message l, Comet comet) {
 		String sourceUrlString = l.getUrl();
 		List<Message> deps = new ArrayList<Message>();
 		try {
 			final URL sourceUrl=new URL(sourceUrlString);
 			Source source = new Source(c);
 			// TODO
 			// Source source = new Source(sourceUrl); // to get title and description in good encoding
 			OutputDocument outputDocument = new OutputDocument(source);
 			StringBuilder sb=new StringBuilder();
 			
 			int cssCount = 0;
 			int scriptCount = 0;
 			int imgCount = 0;
 			int depsCount = 0;
 			
 			List all = source.getAllStartTags();
 			int k = 0;
 			for (Iterator i=all.iterator(); i.hasNext();) {
 				sb=new StringBuilder();
 			    StartTag startTag=(StartTag)i.next();
 			    //Logger.info(startTag.getName());
 			    
 			    /**
 			     * title
 			     */
 			    if(startTag.getName().equalsIgnoreCase("title")) {
 			    	Logger.info("TITLE: "+startTag.getElement().getTextExtractor().toString());
 			    	l.setTitle(startTag.getElement().getTextExtractor().toString());
 			    }
 			    
 			    /**
 			     * description
 			     */
 			    if(startTag.getName().equalsIgnoreCase("meta")) {
 			    	Attributes a = startTag.getAttributes();
 			    	if(a.getValue("name") != null && a.getValue("name").equalsIgnoreCase("description")) {
 			    		Logger.info("DESCRIPTION: "+a.getValue("content"));
 			    		l.setDescription(a.getValue("content"));
 			    	}
 			    }
 			    
 			    /**
 			     * wywal ewentualny base tag (linki sa relatywne do tego i sie pierdzieli)
 			     */
 			    if(startTag.getName().equalsIgnoreCase("base")) {
 			    	outputDocument.remove(startTag);
 			    }
 			    
 			    /**
 			     * process @import inside <style/> tags
 			     */
 			    if(startTag.getName().equalsIgnoreCase("style")) {
 			    	String styleSheetContent = startTag.getElement().getContent().toString();
 			    	styleSheetContent = processCss(styleSheetContent, sourceUrl, comet);
 			    	outputDocument.replace(startTag.getElement().getContent(), styleSheetContent);
 			    }
 			    
 				/**
 				 * replace <link rel="stylesheet" href="<path>" .../>
 				 * with <style type="text/css"> code </style>
 				 */			    
 			    if(startTag.getName().equalsIgnoreCase("link")) {
 				    Attributes attributes=startTag.getAttributes();
 				    String rel=attributes.getValue("rel");
 				    String type = attributes.getValue("type");
 				    if (!("stylesheet".equalsIgnoreCase(rel) || "text/css".equalsIgnoreCase(type))) continue;
 				    String href=attributes.getValue("href");
 				    if (href==null) continue;
 				    String styleSheetContent;
 				    try {
 				    	/*
 				      styleSheetContent = Util.getString(new InputStreamReader(new URL(sourceUrl,href).openStream()));
 				      styleSheetContent = processCss(styleSheetContent, new URL(new URL(sourceUrlString),href), comet);
 				      */
 
 						Message m = downloadFile(new URL(sourceUrl,href).toString(), comet);
 						if(m.getContentEncoding() == null || !m.getContentEncoding().equals("gzip")) {
 							styleSheetContent = Util.getString(new InputStreamReader(new ByteArrayInputStream(m.getData())));
 							styleSheetContent = processCss(styleSheetContent, new URL(new URL(sourceUrlString),href), comet);
 							m.setData(styleSheetContent.getBytes());
 						} else {
 							// unzip to text, replace etc...
 							Logger.info("UNZIP "+m.getUrl());
 							GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(m.getData()));
 							File f1 = File.createTempFile(UUID.randomUUID().toString(), "");
 							f1.deleteOnExit();
 							//Logger.info(f1.getAbsolutePath());
 							FileOutputStream of = new FileOutputStream(f1);
 							IOUtils.copy(gzipInputStream, of);
 							of.close();
 							FileReader reader = new FileReader(f1);
 							styleSheetContent = Util.getString(reader);
 							styleSheetContent = processCss(styleSheetContent, new URL(new URL(sourceUrlString),href), comet);
 							m.setData(styleSheetContent.getBytes());
 							m.setContentEncoding(null);
 							reader.close();
 							Logger.debug(f1.getAbsolutePath() + (f1.delete() ? " deleted":" not deleted"));
 							
 						}
 						deps.add(m);
 						outputDocument.replace(attributes.get("href"), "href=\"##"+depsCount++ +"##\"");
 					    Logger.info("REPLACED css: "+href);
 					    if(comet != null) comet.sendMessage("REPLACED css: "+href);
 					    cssCount++;
 				      
 				    } catch (Exception ex) {
 				    	Logger.error(ex.toString());
 				      continue; // don't convert if URL is invalid
 				    }
 				    
 				    /*
 				    sb.setLength(0);
 				    sb.append("<style");
 				    Attribute typeAttribute=attributes.get("type");
 				    if (typeAttribute!=null) sb.append(' ').append(typeAttribute);
 				    sb.append(">\n").append(styleSheetContent).append("\n</style>");
 				    outputDocument.replace(startTag,sb);
 				    */
 			    }
 			    
 				/**
 				 * replace <img src="<path>" .../>
 				 * with base64 <img src="data:image/jpg;base64,..."/>
 				 */
 			    if(startTag.getName().equalsIgnoreCase("img")) {
 				    Attributes attributes=startTag.getAttributes();
 				    final String src=attributes.getValue("src");
 				    if (src==null) continue;
 				    
 				    // Logger.info("img: "+src);
 				    // toBase64(src);
 				    try {
 					    String photoUrl = (src.startsWith("http") ? new URL(src) : new URL(sourceUrl,src)).toString();
 					    Message m = downloadFile(photoUrl, comet);
 					    deps.add(m);
 					    outputDocument.replace(attributes.get("src"), "src=\"##"+depsCount++ +"##\"");
 	
 					    /**
 					     * Lazy Load Plugin for jQuery
 					     */
 					    final String src1=attributes.getValue("data-original");				    
 					    if(src1 != null) {
 						    String photoUrl1 = (src1.startsWith("http") ? new URL(src1) : new URL(sourceUrl,src1)).toString();
 						    Message m1 = downloadFile(photoUrl1, comet);
 						    deps.add(m1);
 						    outputDocument.replace(attributes.get("data-original"), "data-original=\"##"+depsCount++ +"##\"");
 					    }
 					    
 					    /*
 					    outputDocument.replace(attributes, new HashMap<String, String>(){{
 					    	// put("src","http://www.copywriting.pl/wp-content/uploads/2011/09/udana-nazwa.jpg");
 					    	put("src",toBase64(src.startsWith("http") ? new URL(src) : new URL(sourceUrl, src)));
 					    }});
 					    */
 					    
 					    Logger.info("REPLACED img src: "+src);
 					    if(comet != null) if(comet != null) comet.sendMessage("REPLACED img src: "+src);
 					    imgCount++;
 				    } catch (Exception e) {
 				    	Logger.error(e.toString());
 				    	continue;
 				    }
 			    	
 			    }
 			    
 				/**
 				 * replace <script src="<path>" .../>
 				 * with <script> js code </script>
 				 */			    
 			    if(startTag.getName().equalsIgnoreCase("script")) {
 				    Attributes attributes=startTag.getAttributes();
 				    String src=attributes.getValue("src");
 				    if (src==null) continue;
 				    // String jsText;
 				    try {
 				    	Logger.info(new URL(sourceUrl,src).toString());
 				    	if(comet != null) if(comet != null) comet.sendMessage(new URL(sourceUrl,src).toString());
 				    	// jsText = Util.getString(new InputStreamReader(new URL(sourceUrl,src).openStream()));
 				    	Message m = downloadFile(new URL(sourceUrl,src).toString(), comet);
 				    	deps.add(m);
 					    outputDocument.replace(attributes.get("src"), "src=\"##"+depsCount++ +"##\"");
 					    Logger.info("REPLACED js: "+src);
 					    if(comet != null) comet.sendMessage("REPLACED js: "+src);
 					    scriptCount++;
 				    } catch (Exception ex) {
 				    	Logger.error(ex.toString());
 				      continue; // don't convert if URL is invalid
 				    }
 				    /*
 				    sb.setLength(0);
 				    // sb.append("<!-- "+new URL(sourceUrl,src).toString()+" -->\n");
 				    sb.append("<script");
 				    Attribute typeAttribute=attributes.get("type");
 				    if (typeAttribute!=null) sb.append(' ').append(typeAttribute);
 				    sb.append(">\n").append(jsText).append("\n</script>");
 				    outputDocument.replace(startTag,sb);
 				    */
 			    	
 			    }
 			}
 			Logger.info("REPLACED css count: "+cssCount);
 			Logger.info("REPLACED img src count: "+imgCount);
 			Logger.info("REPLACED script count: "+scriptCount);
 			
 			if(comet != null) {
 				comet.sendMessage("REPLACED css count: "+cssCount);
 				comet.sendMessage("REPLACED img src count: "+imgCount);
 				comet.sendMessage("REPLACED script count: "+scriptCount);
 			}
 			
 			return new Object[]{outputDocument, deps};
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 	/**
 	 * replace @import directive with content
 	 * @param input
 	 * @param baseUrl
 	 * @param comet
 	 * @return
 	 */
 	private static String processCssImports(String input, URL baseUrl, Comet comet) {
 
 		//StringBuffer sb = new StringBuffer(input.length());
 		StringBuffer sb = new StringBuffer();
 		URL url = baseUrl;		
 		/**
 		 * @import search
 		 */
 		String regex = "(@import\\s{0,}url\\s{0,}\\s{0,}\\(['\"])([^'\"]*)(['\"]\\));";		
 		boolean match = false;
 		do {
 			sb = new StringBuffer();
 			Pattern p = Pattern.compile(regex, Pattern.DOTALL);
 			Matcher m = p.matcher(input);
 			while(m.find()) {
 				try {
 					if(m.group(2).startsWith("http")) {
 						url = new URL(m.group(2)); 
 					} else if(m.group(2).startsWith("data:")) {
 						url = null;
 					} else {
 						url = new URL(url, m.group(2));
 					}
 					if(url != null) {
 						Logger.info(m.group() + " => " + url.toString());
 						if(comet != null) comet.sendMessage(m.group() + " => " + url.toString());
 						try {
 							// String b64 = toBase64(url);
 							Message mes = downloadFile(url.toString(), comet);
 							m.appendReplacement(sb, Matcher.quoteReplacement(new String(mes.getData())));
 						} catch (IOException e) {
 							m.appendReplacement(sb, Matcher.quoteReplacement("")); // w razie problemow z dostepem do pliku
 							Logger.error(e.toString());
 						}
 					}
 				} catch (MalformedURLException e) {
 					Logger.error(e.toString());
 				}
 			}
 			m.appendTail(sb);
 			input = sb.toString();			
 			
 			Pattern p1 = Pattern.compile(regex, Pattern.DOTALL);
 			Matcher m1 = p1.matcher(input);
 			match = m1.find();
 		} while(match);
 		
 		return input.toString();
 	}
 	
 	/**
 	 * replace all url() with base64
 	 * @param input
 	 * @param comet 
 	 * @return
 	 */
 	private static String processCss(String input, URL baseUrl, Comet comet) {
 
 		String step1 = processCssImports(input, baseUrl, comet);
 
 		/**
 		 * other url() search
 		 */
 		StringBuffer sb = new StringBuffer();
 		// (url\s{0,}\(\s{0,}['"]{0,1})([^\)'"]*)(['"]{0,1}\))
 		String regex = "(url\\s{0,}\\(\\s{0,}['\"]{0,1})([^\\'\")]*)(['\"]{0,1}\\))";
 		// input.replaceAll(regex, "$1"+"URL"+"$2$3");
 		// return input;
 		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
 		Matcher m = p.matcher(step1);
 		while(m.find()) {
 			try {
 				URL url;
 				if(m.group(2).startsWith("http")) {
 					url = new URL(m.group(2)); 
 				} else if(m.group(2).startsWith("data:")) {
 					url = null;
 				} else {
 					url = new URL(baseUrl, m.group(2));
 				}
 				if(url != null) {
 					Logger.info(m.group() + " => " + url.toString());
 					if(comet != null) comet.sendMessage(m.group() + " => " + url.toString());
 					try {
 						String b64 = toBase64(url);
 						m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)+b64+m.group(3)));
 					} catch (IOException e) {
 						Logger.error(e.toString());
 					}
 				}
 			} catch (MalformedURLException e) {
 				Logger.error(e.toString());
 			}
 		}
 		m.appendTail(sb);
 		
 		return sb.toString();
 	}
 	
 	public static Result open(Long id) {
 		Message m = Ebean.find(Message.class, id);//Message.find.byId(id);
 		if(m == null) {
 			return badRequest("no data");
 		}
 		try {
 			response().setContentType(m.getContentType());
 			if(m.getContentEncoding() != null) {
 				response().setHeader(CONTENT_ENCODING, m.getContentEncoding());
 			}
 			// response().setHeader("Content-Disposition", "inline; filename=\"myfile.txt\"");
 			return m.getData() != null ? ok(IOUtils.toBufferedInputStream(new ByteArrayInputStream(m.getData()))) : ok("no data");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return badRequest("no data");
 	}
 	
 	/**
 	 * click/open link
 	 * @param id
 	 * @return
 	 */
 	public static Result clickPlus(Long id) {
 		Message m = Message.find.byId(id);
 		m.setClicks(m.getClicks()+1);
 		m.update();
 		Cache.remove(session("uuid")+"list");
 		return ok(Message.find.byId(id).getClicks()+"");
 	}
 	
 	public static Result deps(Long parentId) {
 		List<Message> m = Message.find.where().eq("parent.id", parentId).findList();
 		return ok(deps.render(m));
 	}
 }
