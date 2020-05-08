 package controllers;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.io.StringReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import model.Message;
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.Attributes;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.OutputDocument;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.Util;
 import org.apache.commons.io.IOUtils;
 import org.w3c.css.sac.CSSParseException;
 import org.w3c.css.sac.InputSource;
 import org.w3c.dom.css.CSSRule;
 import org.w3c.dom.css.CSSRuleList;
 import org.w3c.dom.css.CSSStyleDeclaration;
 import org.w3c.dom.css.CSSStyleRule;
 import org.w3c.dom.css.CSSStyleSheet;
 import play.Logger;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.libs.Comet;
 import play.libs.F.Callback0;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results.Chunks.Out;
 import views.html.index;
 import com.avaje.ebean.Ebean;
 import com.steadystate.css.parser.CSSOMParser;
 
 public class Application extends Controller {
 
     public static Result index() {
         return ok(index.render(Form.form(Message.class), Message.find.orderBy("timestamp desc").findList()));
     }
     
     public static Result post() {
     	Form<Message> m = Form.form(Message.class);
     	Form f = m.bindFromRequest();
     	if(f.hasErrors()) {
     		ValidationError e = f.error("");
     		flash("error", e.message());
     		f.fill(new Message());
     		return badRequest(index.render(f, Message.find.all()));
     		//return badRequest(f.errors().values()+"");
     	}
     	Message l = m.bindFromRequest().get();
     	try {
 			add(l, null);
 			flash("info", "elem added");
 		} catch (IOException e) {
 			flash("error", "ERROR: " + e);
 			e.printStackTrace();
 		}
     	return redirect("/");
     }
     
     public static Result asyncPost() {
     	Logger.info("asyncPost");
     	Form<Message> m = Form.form(Message.class);
    	Form f = m.bindFromRequest();
     	if(f.hasErrors()) {
     		ValidationError e = f.error("");
     		flash("error", e.message());
     		f.fill(new Message());
    		return badRequest(index.render(f, Message.find.all()));
     		//return badRequest(f.errors().values()+"");
     	}
    	final Message l = m.bindFromRequest().get();
     	
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
     
 	private static void add(Message l, Comet comet) throws IOException {
 		if(comet != null) comet.sendMessage("adding...");
 		Logger.info("adding...");
 		URL url = new URL(l.getUrl());
 		URLConnection c = url.openConnection();
 		c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
 		InputStream in = c.getInputStream();
 		l.setContentType(c.getContentType());		
 		
 		File f = File.createTempFile(UUID.randomUUID().toString(), "");
 		f.deleteOnExit();
 		Logger.info(f.getAbsolutePath());
 		FileOutputStream o = new FileOutputStream(f);
 		IOUtils.copy(in, o);
 		o.close();
 		
 		/*
 		if(c.getContentType().startsWith("text/html")) {
 			List<Message> m = parse(f, l.getUrl());
 		}
 		*/
 		
 		if(c.getContentType().startsWith("text/html")) {
 			String s = process(f, l.getUrl(), comet);
 			l.setData(s.getBytes());
 		} else {
 			in = new FileInputStream(f);
 			l.setData(IOUtils.toByteArray(in));
 		}
 		
 		l.save();
 		in.close();
 		Logger.debug(f.getAbsolutePath() + (f.delete() ? " deleted":" not deleted"));
 	}
 	
 	// lista powiazanych plikow, plik zrodlowy bedzie musial miec zmienione odnosniki
 	private static List<Message> parse(File f, String sourceUrlString) {
 		return null;
 	}
 	
 	private static String process(File f, String sourceUrlString, Comet comet) {
 		try {
 			final URL sourceUrl=new URL(sourceUrlString);
 			Source source = new Source(f);
 			OutputDocument outputDocument = new OutputDocument(source);
 			StringBuilder sb=new StringBuilder();
 			
 			int cssCount = 0;
 			int scriptCount = 0;
 			int imgCount = 0;
 			
 			List all = source.getAllStartTags();
 			int k = 0;
 			for (Iterator i=all.iterator(); i.hasNext();) {
 				sb=new StringBuilder();
 			    StartTag startTag=(StartTag)i.next();
 			    //Logger.info(startTag.getName());
 			    
 				/**
 				 * replace <link rel="stylesheet" href="<path>" .../>
 				 * with <style type="text/css"> code </style>
 				 */			    
 			    if(startTag.getName().equalsIgnoreCase("link")) {
 				    Attributes attributes=startTag.getAttributes();
 				    String rel=attributes.getValue("rel");
 				    if (!"stylesheet".equalsIgnoreCase(rel)) continue;
 				    String href=attributes.getValue("href");
 				    if (href==null) continue;
 				    String styleSheetContent;
 				    try {
 				      styleSheetContent = Util.getString(new InputStreamReader(new URL(sourceUrl,href).openStream()));
 				      styleSheetContent = processCss(styleSheetContent, new URL(new URL(sourceUrlString),href));
 				      // Message m = new Message();
 				    } catch (Exception ex) {
 				    	Logger.error(ex.toString());
 				      continue; // don't convert if URL is invalid
 				    }
 				    
 				    /**
 				     * css parser
 				     *
 					try {
 						CSSOMParser css = new CSSOMParser();
 						InputSource source1 = new InputSource(new StringReader(styleSheetContent));
 						CSSStyleSheet stylesheet = css.parseStyleSheet(source1, null, null);
 						CSSRuleList ruleList = stylesheet.getCssRules();
 						for (int k = 0; k < ruleList.getLength(); k++) {
 							CSSRule rule = ruleList.item(k);
 							if (rule instanceof CSSStyleRule) {
 								CSSStyleRule styleRule = (CSSStyleRule) rule;
 								Logger.info("selector:" + k + ": " + styleRule.getSelectorText());
 								CSSStyleDeclaration styleDeclaration = styleRule.getStyle();
 								for (int z = 0; z < styleDeclaration.getLength(); z++) {
 									String property = styleDeclaration.item(z);
 									Logger.info("property: " + property + " :: " + "value: " + styleDeclaration.getPropertyCSSValue(property).getCssText());  
 								}
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					*/			    
 				    
 				    sb.setLength(0);
 				    sb.append("<style");
 				    Attribute typeAttribute=attributes.get("type");
 				    if (typeAttribute!=null) sb.append(' ').append(typeAttribute);
 				    sb.append(">\n").append(styleSheetContent).append("\n</style>");
 				    outputDocument.replace(startTag,sb);
 				    
 				    Logger.info("REPLACED css: "+href);
 				    if(comet != null) if(comet != null) comet.sendMessage("REPLACED css: "+href);
 				    cssCount++;
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
 				    
 				    outputDocument.replace(attributes, new HashMap<String, String>(){{
 				    	// put("src","http://www.copywriting.pl/wp-content/uploads/2011/09/udana-nazwa.jpg");
 				    	put("src",toBase64(src.startsWith("http") ? new URL(src) : new URL(sourceUrl, src)));
 				    }});
 				    
 				    Logger.info("REPLACED img src: "+src);
 				    if(comet != null) if(comet != null) comet.sendMessage("REPLACED img src: "+src);
 				    imgCount++;
 			    	
 			    }
 			    
 				/**
 				 * replace <script src="<path>" .../>
 				 * with <script> js code </script>
 				 */			    
 			    if(startTag.getName().equalsIgnoreCase("script")) {
 				    Attributes attributes=startTag.getAttributes();
 				    String src=attributes.getValue("src");
 				    if (src==null) continue;
 				    String jsText;
 				    try {
 				    	Logger.info(new URL(sourceUrl,src).toString());
 				    	if(comet != null) if(comet != null) comet.sendMessage(new URL(sourceUrl,src).toString());
 				    	jsText = Util.getString(new InputStreamReader(new URL(sourceUrl,src).openStream()));
 				    } catch (Exception ex) {
 				    	Logger.error(ex.toString());
 				      continue; // don't convert if URL is invalid
 				    }
 				    sb.setLength(0);
 				    sb.append("<script");
 				    Attribute typeAttribute=attributes.get("type");
 				    if (typeAttribute!=null) sb.append(' ').append(typeAttribute);
 				    sb.append(">\n").append(jsText).append("\n</script>");
 				    outputDocument.replace(startTag,sb);
 				    
 				    Logger.info("REPLACED js: "+src);
 				    if(comet != null) comet.sendMessage("REPLACED js: "+src);
 				    scriptCount++;
 			    	
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
 			
 			return outputDocument.toString();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * replace all url() with base64
 	 * @param input
 	 * @return
 	 */
 	private static String processCss(String input, URL baseUrl) {
 
 		//StringBuffer sb = new StringBuffer(input.length());
 		StringBuffer sb = new StringBuffer();
 		
 		// (url\s{0,}\(\s{0,}['"]{0,1})([^\)'"]*)(['"]{0,1}\))
 		String regex = "(url\\s{0,}\\(\\s{0,}['\"]{0,1})([^\\'\")]*)(['\"]{0,1}\\))";
 		// input.replaceAll(regex, "$1"+"URL"+"$2$3");
 		// return input;
 		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
 		Matcher m = p.matcher(input);
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
 					Logger.warn(m.group() + " => " + url.toString());
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
 			// response().setHeader("Content-Disposition", "inline; filename=\"myfile.txt\"");
 			return m.getData() != null ? ok(IOUtils.toBufferedInputStream(new ByteArrayInputStream(m.getData()))) : ok("no data");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return badRequest("no data");
 	}
 	
 	/**
 	 * test Comet socket
 	 * @return
 	 */
 	public static Result comet() {
 		Comet comet = new Comet("parent.log") {
 			public void onConnected() {
 				
 				int i = 0;
 				while(1 == 1) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 					}
 					sendMessage("comet message "+i++);
 				}
 				//close();
 			}
 			
 			@Override
 			public void onDisconnected(Callback0 callback) {
 				sendMessage("disconnect");
 				close();
 			}
 		};
 		
 		// otwiera socket (nieskonczony response)
 		return ok(comet);
 	}
     
 }
