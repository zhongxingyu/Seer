 package spiderman.plugin.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import net.sf.saxon.xpath.XPathFactoryImpl;
 
 import org.eweb4j.spiderman.fetcher.Page;
 import org.eweb4j.spiderman.spider.SpiderListener;
 import org.eweb4j.spiderman.task.Task;
 import org.eweb4j.spiderman.xml.Field;
 import org.eweb4j.spiderman.xml.Parsers;
 import org.eweb4j.spiderman.xml.Target;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.FileUtil;
 import org.eweb4j.util.xml.Attrs;
 import org.eweb4j.util.xml.Tags;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.Serializer;
 import org.htmlcleaner.SimpleXmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.greenpineyu.fel.FelEngine;
 import com.greenpineyu.fel.FelEngineImpl;
 import com.greenpineyu.fel.context.FelContext;
 import com.greenpineyu.fel.function.CommonFunction;
 import com.greenpineyu.fel.function.Function;
 
 public class ModelParser extends DefaultHandler{
 
 	private Task task = null;
 	private Target target = null;
 	private SpiderListener listener = null;
 	private FelEngine fel = new FelEngineImpl();
 	
 	private final static Function fun = new CommonFunction() {
 		public String getName() {
 			return "$output";
 		}
 
 		public Object call(Object[] arguments) {
 			Object node = arguments[0];
 			boolean keepHeader = false;
 			if (arguments.length > 2)
 				keepHeader = (Boolean) arguments[1];
 			
 			return ParserUtil.xml(node, keepHeader);
 		}
 	};
 	
 	private void init(Task task, Target target, SpiderListener listener){
 		this.task = task;
 		this.target = target;
 		this.listener = listener;
 		
     	fel.addFun(fun);
     	Tags $Tags = Tags.me();
     	Attrs $Attrs = Attrs.me();
     	fel.getContext().set("$Tags", $Tags);
     	fel.getContext().set("$Attrs", $Attrs);
     	fel.getContext().set("$Util", CommonUtil.class);
 		fel.getContext().set("$target", this.target);
 		fel.getContext().set("$listener", this.listener);
 		fel.getContext().set("$task_url", this.task.url);
 	}
 	
 	public ModelParser(Task task, Target target, SpiderListener listener) {
 		init(task, target, listener);
 	}
 	
 	public static void main(String[] args){
 		List<String> list = new ArrayList<String>(Arrays.asList("1", "2", "3"));
 		list.addAll(0, Arrays.asList("0"));
 		System.out.println(list);
 	}
 	
 	public static void mains(String[] args) throws Exception{
 		File file = new File("d:\\xml.xml");
 		String xml = FileUtil.readFile(file);
 //		System.setProperty("javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true); // never forget this!
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
         XPathFactory xfactory = XPathFactoryImpl.newInstance();
         XPath xpath = xfactory.newXPath();
         XPathExpression expr = xpath.compile("//node");
         Object result = expr.evaluate(doc, XPathConstants.NODESET);
         NodeList nodes = (NodeList) result;
         FelEngine fel = new FelEngineImpl();
 //        int count = 0;
 //        String regex = "\\w+\\.(gif|png|jpg|jpeg|bmp)";
         for (int i = 0; i < nodes.getLength(); i++) {
         	if (i > 0)
         		break;
             
         	NodeList subs = (NodeList)xpath.compile("Description").evaluate(nodes.item(i), XPathConstants.NODESET);
         	
         	Node node = subs.item(0);
         	FelContext ctx = fel.getContext();
         	ctx.set("$this", node);
         	Tags $Tags = Tags.me();
         	Attrs $Attrs = Attrs.me();
 			ctx.set("$Tags", $Tags);
 			ctx.set("$Attrs", $Attrs);
     		
 			System.out.println($Attrs.xml(ParserUtil.xml(node, false)).rm("style").Tags().kp("p").ok());
     		
     		System.out.println(fel.eval("$Attrs.xml($output($this)).rm('style').Tags().kp('p').ok()"));
     		
 //    		Object newVal =  MVEL.eval("org.eweb4j.util.CommonUtil.toXml($this, false)", ctx);
 //    		System.out.println(newVal);
         }
 
 //        	
 //            NodeList subs = (NodeList)xpath.compile("*[matches(text(),'"+regex+"')]/text()").evaluate(nodes.item(i), XPathConstants.NODESET);
 //            if (subs == null || subs.getLength() == 0)
 //            	continue;
 //            for (int j = 0; j < subs.getLength(); j++) {
 //            	Node item = subs.item(j);
 //            	String value = item.getNodeValue();
 //            	List<String> imgs = CommonUtil.findByRegex(value, "[^\\s'=\"]+\\.(gif|png|jpg|jpeg|bmp)(?=[\"']?)");
 //            	System.out.println(item.getParentNode().getNodeName()+"->"+imgs);
 //            	count++;
 //            }
 //        }
 //        System.out.println("count->"+count);
         
         String html = "<div id='desc'><p><a href='http://www.baidu.com'>click me</a></p></div>";
         HtmlCleaner cleaner = new HtmlCleaner();
 		TagNode tagNode = cleaner.clean(html);
 		Object[] nodeVals = tagNode.evaluateXPath("//p");
 		StringWriter sw = new StringWriter();  
 	    Serializer serializer = new SimpleXmlSerializer(cleaner.getProperties());  
 	    serializer.write((TagNode)nodeVals[0], sw, "UTF-8");
 //	    System.out.println(sw.getBuffer().toString());
 		
 //		//第一步：获得解析工厂的实例  
 //        SAXParserFactory spf = SAXParserFactory.newInstance();  
 //        //第二部：获得工厂解析器  
 //        SAXParser sp = spf.newSAXParser();  
 //        //第三部：对xml进行解析  
 //        sp.parse(file, new ModelParser());
         
 	}
 	
 	public List<Map<String, Object>> parse(Page page) throws Exception{
 		listener.onInfo(Thread.currentThread(), task, "parse Page->[cType:" + page.getContentType()+",charset:"+page.getCharset()+",encoding:"+page.getEncoding()+",url->"+page.getUrl());
 		String contentType = page.getContentType();
 		if (contentType == null)
 			contentType = "text/html";
 		boolean isXml = contentType.contains("text/xml") || contentType.contains("application/rss+xml") || contentType.contains("application/xml");
 		
 		//解析xml
 		if (isXml) 
 			return parseXml(page);
 		
 		// TODO 解析 JSON
 		
 		//解析html
 		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 		list.add(parseHtml(page));
 		return list;
 	}
 
 	private List<Map<String, Object>> parseXml(Page page) throws Exception{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true); // never forget this!
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(new ByteArrayInputStream(page.getContentData()));
         XPathFactory xfactory = XPathFactoryImpl.newInstance();
         XPath xpathParser = xfactory.newXPath();
         
         final List<Field> fields = target.getModel().getField();
 		String isModelArray = target.getModel().getIsArray();
 		String modelXpath = target.getModel().getXpath();
 		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 		if ("1".equals(isModelArray) || "tre".equals(isModelArray)){
 			XPathExpression expr = xpathParser.compile(modelXpath);
 	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
 //		    listener.onInfo(Thread.currentThread(), "modelXpath -> " + modelXpath + " parse result -> " + result);
 	        if (result != null){
 		        NodeList nodes = (NodeList) result;
 		        if (nodes.getLength() > 0){
 			        for (int i = 0; i < nodes.getLength(); i++) {
 						list.add(parse2Map(nodes.item(i), xpathParser, fields));
 			        }
 		        }
 	        }
 		}else{
 			list.add(parse2Map(doc, xpathParser, fields));
 		}
 		return list;
 	}
 	
 	private Map<String, Object> parse2Map(Object item, XPath xpathParser, final List<Field> fields) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		fel.getContext().set("$fields", map);
 		for (Field field : fields){
 			String key = field.getName();
 			String isArray = field.getIsArray();
 			String isTrim = field.getIsTrim();
 			
 			Parsers parsers = field.getParsers();
 			if (parsers == null)
 				continue;
 			
 			List<org.eweb4j.spiderman.xml.Parser> parserList = parsers.getParser();
 			if (parserList == null || parserList.isEmpty())
 				continue;
 			
 			//field最终解析出来的结果
 			List<Object> values = new ArrayList<Object>();
 			for (int i = 0; i < parserList.size(); i++) {
 				org.eweb4j.spiderman.xml.Parser parser = parserList.get(i);
 				String xpath = parser.getXpath();
 				String attribute = parser.getAttribute();
 				String exp = parser.getExp();
 				String regex = parser.getRegex();
 				try {
 					if (xpath != null && xpath.trim().length() > 0) {
 						
 						XPathExpression expr = xpathParser.compile(xpath);
 				        Object result = expr.evaluate(item, XPathConstants.NODESET);
 				        
 						if (result == null)
 							continue;
 						
 						NodeList nodes = (NodeList) result;
 						if (nodes.getLength() == 0)
 							continue;
 						
 						if (attribute != null && attribute.trim().length() > 0){
 							for (int j = 0; j < nodes.getLength(); j++){
 								Node node = nodes.item(j);
 								Element e = (Element)node;
 								String attrVal = e.getAttribute(attribute);
 								values.add(attrVal);
 							}
 							
 							//正则
 							parseByRegex(regex, values);
 							// EXP表达式
 							parseByExp(exp, values);
 						}else if (xpath.endsWith("/text()")){
 							for (int j = 0; j < nodes.getLength(); j++){
 								Node node = nodes.item(j);
 								values.add(node.getNodeValue());
 							}
 							//正则
 							parseByRegex(regex, values);
 							// EXP表达式
 							parseByExp(exp, values);
 						} else {
 							for (int j = 0; j < nodes.getLength(); j++){
 								Node node = nodes.item(i);
 								values.add(node);
 							}
 							// 此种方式获取到的Node节点大部分都不是字符串，因此先执行表达式后执行正则
 							// EXP表达式
 							parseByExp(exp, values);
 							//正则
 							parseByRegex(regex, values);
 						}
 					}else{
 						List<Object> newValues = new ArrayList<Object>(values.size());
 						for (Object obj : values){
 							newValues.add(obj.toString());
 						}
 						//正则
 						parseByRegex(regex, newValues);
 						// EXP表达式
 						parseByExp(exp, newValues);
 						
 						if (!newValues.isEmpty()) {
 							values.clear();
 							values.addAll(newValues);
 						}
 					}
 				} catch (Exception e) {
 					listener.onError(Thread.currentThread(), task, "key->"+key +" parse failed cause->"+e.toString(), e);
 					continue;
 				}
 			}
 			
 			try {
 				if (values.isEmpty()) 
 					values.add("");
 				
 				// 相同 key，若values不为空，继续沿用
 				if (map.containsKey(key)){
 					//将原来的值插入到前面
 					Object obj = map.get(key);
 					if (obj instanceof Collection) {
 						values.addAll(0, (Collection<?>) obj);
 					} else {
 						values.add(0, obj);
 					}
 				}
 				
 				//数组的话，需要去除重复和空空元素
 				if (values.size() >= 2){
 					List<Object> noRepeatValues = new ArrayList<Object>();
 					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
 						Object obj = it.next();
 						if (noRepeatValues.contains(obj))
 							continue;
 						if (obj instanceof String) {
 							if (((String)obj) == null || ((String)obj).trim().length() == 0)
 								continue;
 						}
 						
 						noRepeatValues.add(obj);
 					}
 					values.clear();
 					values.addAll(noRepeatValues);
 				}
 				
 				//如果设置了trim
 				if ("1".equals(isTrim) || "true".equals(isTrim)) {
 					List<String> results = new ArrayList<String>(values.size());
 					for (Object obj : values){
 						results.add(String.valueOf(obj).trim());
 					}
 					values.clear();
 					values.addAll(results);
 				}
 				
 				//最终完成
 				if ("1".equals(isArray)){
 					map.put(key, values);
 				} else {
 					map.put(key, new ArrayList<Object>(values).get(0));
 				}
 				
 			} catch (Exception e) {
 				listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
 			}
 		}
 		
 		return map;
 	}
 	
 	private Map<String, Object> parseHtml(Page page){
 		Map<String, Object> map = new HashMap<String, Object>();
 		fel.getContext().set("$fields", map);
 		final List<Field> fields = target.getModel().getField();
 		HtmlCleaner cleaner = new HtmlCleaner();
 		TagNode rootNode = cleaner.clean(page.getContent());
 		for (Field field : fields){
 			String key = field.getName();
 			String isArray = field.getIsArray();
 			String isTrim = field.getIsTrim();
 			Parsers parsers = field.getParsers();
 			if (parsers == null)
 				continue;
 			
 			List<org.eweb4j.spiderman.xml.Parser> parserList = parsers.getParser();
 			if (parserList == null || parserList.isEmpty())
 				continue;
 			
 			//field最终解析出来的结果
 			List<Object> values = new ArrayList<Object>();
 			for (int i = 0; i < parserList.size(); i++) {
 				org.eweb4j.spiderman.xml.Parser parser = parserList.get(i);
 				String xpath = parser.getXpath();
 				String attribute = parser.getAttribute();
 				String exp = parser.getExp();
 				String regex = parser.getRegex();
 				try {
 					if (xpath != null && xpath.trim().length() > 0) {
 						Object[] nodeVals = rootNode.evaluateXPath(xpath);
 						if (nodeVals == null || nodeVals.length == 0)
 							continue;
 						
 						if (attribute != null && attribute.trim().length() > 0){
 							for (Object nodeVal : nodeVals){
 								TagNode node = (TagNode)nodeVal;
 								String attrVal = node.getAttributeByName(attribute);
 								values.add(attrVal);
 							}
 							//正则
 							parseByRegex(regex, values);
 							// EXP表达式
 							parseByExp(exp, values);
 						}else if (xpath.endsWith("/text()")){
 							for (Object nodeVal : nodeVals){
 								values.add(nodeVal.toString());
 							}
 							//正则
 							parseByRegex(regex, values);
 							// EXP表达式
 							parseByExp(exp, values);
 						}else {
 							for (Object nodeVal : nodeVals){
 								TagNode node = (TagNode)nodeVal;
 								values.add(node);
 							}
 							// 此种方式获取到的Node节点大部分都不是字符串，因此先执行表达式后执行正则
 							// EXP表达式
 							parseByExp(exp, values);
 							//正则
 							parseByRegex(regex, values);
 						}
 					}else {
 						
 						//第一步获得的是一个List<String>对象，交给下面的步骤进行解析
 						List<Object> newValues = new ArrayList<Object>();
 						for (Object nodeVal : values){
 							newValues.add(nodeVal.toString());
 						}
 						//正则
 						parseByRegex(regex, newValues);
 						// EXP表达式
 						parseByExp(exp, newValues);
 						
 						if (!newValues.isEmpty()) {
 							values.clear();
 							values.addAll(newValues);
 						}
 					}
 				} catch (Exception e) {
 					listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
 					continue;
 				}
 			}
 			
 			try {
 				if (values.isEmpty()) 
 					values.add("");
 				
 				// 相同 key，若values不为空，继续沿用
 				if (map.containsKey(key)){
 					//将原来的值插入到前面
 					Object obj = map.get(key);
 					if (obj instanceof Collection) {
 						values.addAll(0, (Collection<?>) obj);
 					} else {
 						values.add(0, obj);
 					}
 				}
 				
 				//数组的话，需要去除重复和空空元素
 				if (values.size() >= 2){
 					List<Object> noRepeatValues = new ArrayList<Object>();
 					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
 						Object obj = it.next();
 						if (noRepeatValues.contains(obj))
 							continue;
 						if (obj instanceof String) {
 							if (((String)obj) == null || ((String)obj).trim().length() == 0)
 								continue;
 						}
 						
 						noRepeatValues.add(obj);
 					}
 					values.clear();
 					values.addAll(noRepeatValues);
 				}
 				
 				//如果设置了trim
 				if ("1".equals(isTrim) || "true".equals(isTrim)) {
 					List<String> results = new ArrayList<String>(values.size());
 					for (Object obj : values){
 						results.add(String.valueOf(obj).trim());
 					}
 					values.clear();
 					values.addAll(results);
 				}
 				
 				//最终解析完成
 				if ("1".equals(isArray)){
 					map.put(key, values);
 				}else{
 					map.put(key, values.get(0).toString());
 				}
 			} catch (Exception e) {
 				listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
 			}
 		}
 		
 		return map;
 	}
 	
 	private void parseByExp(String exp, Collection<Object> list) {
 		if (exp == null || exp.trim().length() == 0)
 			return ;
 		
 		List<Object> newValue = new ArrayList<Object>();
 		if (list == null || list.isEmpty()){
 			try {
 	    		Object newVal = fel.eval(exp);
 				if (newVal != null)
 					newValue.add(newVal);
 			} catch (Exception e){
 				listener.onError(Thread.currentThread(), task, "exp->"+exp+" eval failed", e);
 			}
 		} else {
 			for (Object val : list){
 				if (val != null)
 					fel.getContext().set("$this", val);
 				try {
 		    		Object newVal = fel.eval(exp);
 					if (newVal != null)
 						newValue.add(newVal);
 				} catch (Exception e){
 					listener.onError(Thread.currentThread(), task, "exp->"+exp+" eval failed", e);
 				}
 			}
 		}
 		
 		if (!newValue.isEmpty()){
 			list.clear();
 			list.addAll(newValue);
 		}
 	}
 	
 	private void parseByRegex(String regex, Collection<Object> list) {
 		if (regex == null || regex.trim().length() == 0)
 			return ;
 		List<Object> newVals = new ArrayList<Object>(list.size());
 		for (Object obj : list) {
 			try {
 				String input = (String)obj;
 				if (input == null || input.trim().length() == 0)
 					continue;
 				List<String> vals = CommonUtil.findByRegex(input, regex);
 				if (vals == null)
					continue;
				for (String val : vals){
					if (val == null || val.trim().length() == 0)
						continue;
					newVals.add(val);
 				}
 			} catch (Exception e){
 				listener.onError(Thread.currentThread(), task, "regex->"+regex+" of "+obj+" parse failed", e);
				newVals.add(null);
 			}
 		}
 		
		list.clear();
		list.addAll(newVals);
 	}
 }
