 package com.lavans.lacoder2.util;
 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.SAXException;
 
 import com.lavans.lacoder2.lang.StringUtils;
 
 /**
  * 設定ファイル読込クラス。
  * getInstance(String)で設定ファイル名を指定して読み込む。
  *
  * @author	dobashi
  * @version	1.0
  */
 public class Config {
 	/**
 	 * logger.
 	 * Loggerをluz内ではなく外部Loggerを使うようにしたので、Configクラスでも使用可能となった。
 	 */
 	private static Logger logger = LoggerFactory.getLogger(Config.class);
 
 	/** Default config file. */
 	public static final String CONFIG_FILE ="lacoder2.xml";
 
 	/** インスタンス一覧 */
 	private static Map<String, Config> instanceMap = new HashMap<String, Config>();
 
 	/** ノード一覧 */
 //	private Map<String, List<Node>>  nodeMap= null;
 //	/** propertyセクションの値一覧 */
 //	private Map<String, String> propertyMap = null;
 
 	/** 設定ファイル名 */
 	private String configFileName = null;
 
 	/** 設定ファイルのrootノード */
 	private Element root = null;
 	/** xpath パーサー */
 	private XPath xpath = null;
 
 	/** 書き込み許可モード時の書き込みクラス */
 	private LSOutput lsOutput = null;
 
 	/** 書き込み許可モード時のシリアライズクラス */
 	private LSSerializer lsSerializer = null;
 
 	/** 書き込み可能かどうか */
 	private boolean isReadOnly = false;
 
 	/** デバッグモード */
 	private boolean isDebug = false;
 
 	/**
 	 * インスタンス取得。設定ファイル名指定無しならlacoder.xmlから読み込む。
 	 * @return
 	 */
 	public static Config getInstance(){
 		return getInstance(CONFIG_FILE);
 	}
 
 	/**
 	 * インスタンス取得。設定ファイル名指定あり。
 	 * @param configFile
 	 * @param reload 再読込するかどうか
 	 * @return
 	 */
 	public static Config getInstance(String configFile){
 		return getInstance(configFile, false);
 	}
 	public static Config getInstance(String configFile, boolean reload) {
 		Config config = instanceMap.get(configFile);
 		if(reload || config == null){
 			logger.info("load "+ configFile);
 			config = new Config(configFile);
 			instanceMap.put(configFile, config);
 		}
 		return config;
 	}
 
 	protected Config(String fileName){
 		init(fileName);
 	}
 	/**
 	 * 初期化。
 	 * 動的に設定を再読込できるようにpublicにする。
 	 */
 	public void init(String fileName){
 		try {
 			Document doc;
 			// パースして DOM ツリーを作成する
 			try{
 				doc = getDocumenFromUrl(fileName);
 			}catch(FileNotFoundException e){
 				// 見つからない場合はリソースから取得(jarファイル内など)
 				doc = getDocumenFromResource(fileName);
 				// 読み込み専用
 				isReadOnly=true;
 			}
 
 			// 書き込み用クラスの初期化
 			initOutput(doc);
 
 			// Get root node.
 			root = doc.getDocumentElement();
 
 			xpath = XPathFactory.newInstance().newXPath();
 //			root = (Element)xpath.evaluate("/luz", new InputSource(fis), XPathConstants.NODE);
 
 			// debugの取得
 			isDebug = getParameterBoolean("debug");
 		} catch (SAXException | IOException | ParserConfigurationException e) {
 			// どこでRuntimeに入れるか検討
 			logger.error("Can't parse xml file.["+ fileName +"]", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * 書き込み用クラスの初期化
 	 * @param doc
 	 */
 	public void initOutput(Document doc){
 		DOMImplementation domImpl = doc.getImplementation();
 		DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");
 		lsOutput = domImplLS.createLSOutput();
 		lsSerializer = domImplLS.createLSSerializer();
 	}
 
 	/**
 	 * URLクラスを使用してxmlファイルを読み込む
 	 * @param fileName
 	 * @return
 	 * @throws FileNotFoundException ファイルが見つからない場合
 	 */
 	private Document getDocumenFromUrl(String fileName) throws SAXException, IOException, ParserConfigurationException{
 		// 設定ファイル -----------------------
 		// クラスローダーより設定ファイルを取得
 		URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
 		// nullの場合は絶対パスで指定されているケースを想定
 		if(url==null){
 			configFileName = fileName;
 		}else{
 			try{
 				configFileName = URLDecoder.decode(url.getFile(),"UTF-8");
 			}catch(UnsupportedEncodingException e){}
 		}
 
 		// InputStreamの作成
 		FileInputStream fis = new FileInputStream(configFileName);
 
 		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();	// ドキュメントビルダーファクトリを生成
 		DocumentBuilder builder = dbfactory.newDocumentBuilder();					// ドキュメントビルダーを生成
 		Document doc = builder.parse(fis);
 
 		fis.close();
 
 		return doc;
 	}
 
 	/**
 	 * リソースからの取得処理。
 	 * jar/zipファイルからの読み込み用。
 	 * 書き込み不可となる。
 	 *
 	 * @param fileName
 	 * @return
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	private Document getDocumenFromResource(String fileName) throws SAXException, IOException, ParserConfigurationException{
 		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();	// ドキュメントビルダーファクトリを生成
 		DocumentBuilder builder = dbfactory.newDocumentBuilder();					// ドキュメントビルダーを生成
 		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
 		if(is==null){
 			throw new FileNotFoundException(fileName);
 		}
 		Document doc = builder.parse(is);
 
 		return doc;
 	}
 
 	public Element getRoot(){
 		return root;
 	}
 
 	/**
 	 * ノードの取得。ノード単体用。
 	 * 該当するノード名が無い場合はnullを返す。
 	 *
 	 * @param nodeName
 	 * @return
 	 * @throws XPathExpressionException
 	 */
 	public Node getNode(String xql){
 		Node element = null;
 		try {
 			synchronized (xpath) {
 				element = (Node)xpath.evaluate(xql, root, XPathConstants.NODE);
 			}
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 		}
 		return element;
 	}
 
 	/**
 	 * ノードの取得。ノード複数用。親ノード指定あり。
 	 * 該当するノード名が無い場合はException。
 	 *
 	 * @param nodeName
 	 * @return
 	 * @throws XPathExpressionException
 	 */
 	public NodeList getNodeList(String xql, Object item) {
 		NodeList element;
 		synchronized (xpath) {
 			try {
 				element = (NodeList)xpath.evaluate(xql, item, XPathConstants.NODESET);
 			} catch (XPathExpressionException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return element;
 	}
 
 	/**
 	 * ノードの取得。ノード複数用。rootから。
 	 * 該当するノード名が無い場合はException。
 	 *
 	 * @param nodeName
 	 * @return
 	 * @throws XPathExpressionException
 	 */
 	public NodeList getNodeList(String xql){
 		return getNodeList(xql, root);
 	}
 
 	/**
 	 * テキストノードのコンテンツを取得します。
 	 * <luz>
 	 *   <property>
 	 *	 <name1>value1</name1>
 	 *   </property>
 	 * という設定ファイルに対して、getNodeValue("/luz/property/name1")で"value1"が返る。
 	 *
 	 * @param xql
 	 * @return
 	 * @throws XPathExpressionException
 	 */
 	public String getNodeValue(String xql){
 		return getNodeValue(xql, root);
 	}
 	public String getNodeValue(String xql,Object item) {
 		String result=null;
 		synchronized (xpath) {
 			try {
 				result = xpath.evaluate(xql, item);
 			} catch (XPathExpressionException e) {
 				new RuntimeException(e);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 設定値をintで返す。
 	 * lacoder2.xmlに設定されていない場合は引数で与えられたデフォルト値を使用する。
 	 * 設定されている場合はintに変換して返す。
 	 *
 	 * @param key 設定キー
 	 * @param defaultValue 設定ファイルに設定が無い場合のデフォルト。
 	 * @return
 	 * @throw NumberFormatException 設定が記述されているのにIntegerじゃない場合。
 	 */
 	public int getNodeValueInt(String key, int defaultValue){
 		return (int)getValueDefault(getNodeValue(key), defaultValue);
 	}
 	public long getNodeValueLong(String key, long defaultValue){
 		return getValueDefault(getNodeValue(key), defaultValue);
 	}
 
 //	public Object getNode(String xql,Object item, QName returnType) throws XPathExpressionException{
 //		Object result;
 //		synchronized (xpath) {
 //			result = xpath.evaluate(xql, item, XPathConstants.NODE);
 //		}
 //		return result;
 //	}
 //
 //	public Object getNodeAttribute(String xql,Object item, QName returnType) throws XPathExpressionException{
 //		Object result;
 //		synchronized (xpath) {
 //			result = xpath.evaluate(xql, item, XPathConstants.NODE);
 //		}
 //		return result;
 //	}
 
 	/**
 	 * Set node value.
 	 *
 	 * @param xql
 	 * @param value
 	 */
 	public void setNodeValue(String xql, String value){
 		Document doc = root.getOwnerDocument();
 		Node ele = getNode(xql);
 		ele.removeChild(ele.getFirstChild());
 		ele.appendChild(doc.createTextNode(value));
 	}
 
 	/**
 	 * テキストノードのコンテンツを取得します。
 	 * <luz>
 	 *   <property>
 	 *	 <name1>value1</name1>
 	 *	 <name1>value2</name1>
 	 *   </property>
 	 * という設定ファイルに対して、getNodeValueList("/luz/property/name1")で[value1,value2]が返ります。
 	 *
 	 * @param xql
 	 * @return
 	 * @throws XPathExpressionException
 	 */
 	public List<String> getNodeValueList(String xql, Element parent) {
 		NodeList nodeList = getNodeList(xql,parent);
 		List<String> valueList = new ArrayList<String>(nodeList.getLength());
 		for(int i=0; i< nodeList.getLength(); i++){
 			Node node = nodeList.item(i);
 			if(node.getFirstChild()!=null){
 				valueList.add(node.getFirstChild().getNodeValue());
 			}
 		}
 		return valueList;
 	}
 
 	/**
 	 * テキストノードのコンテンツをListで取得します。
 	 * rootから探します。
 	 *
 	 * @param xql
 	 * @return
 	 * @throws XPathExpressionException
 	 */
 	public List<String> getNodeValueList(String xql) {
 		return getNodeValueList(xql, root);
 	}
 
 	/**
 	 * Write to xml file.
 	 *
 	 * @throws IOException
 	 */
 	public void save(){
 		try {
 
 			if(isReadOnly){
 				throw new IOException("Xml file is readonly. It is in jar file.");
 			}
 
 			FileOutputStream fos = new FileOutputStream(configFileName);
 			//LSOutput に出力ストリームをセットする
 			lsOutput.setByteStream(fos);
 			//DOM ツリーをシリアライズする。
 			lsSerializer.write(root,lsOutput);
 			//ファイルストリームのクローズ
 			fos.close();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
	 * luz.xml(Configのデフォルトインスタンス)にあるデバッグモードを返す。
 	 * @return
 	 */
 	public static boolean isDebug(){
 		return getInstance().isDebug;
 	}
 
 	public String getParameter(String name){
 		return getNodeValue("parameter[@name='"+name+"']/@value", root);
 	}
 
 	public String getParameter(String name, String defaultValue){
 		String value = getParameter(name);
 		if(StringUtils.isEmpty(value)){
 			return defaultValue;
 		}
 		return value;
 	}
 
 	/**
 	 * パラメータ設定値をbooleanで返します。
 	 * @param name
 	 * @return
 	 */
 	public boolean getParameterBoolean(String name){
 		return Boolean.parseBoolean(getParameter(name));
 	}
 
 	/**
 	 * 設定値をintで返す。
 	 * iris.xmlに設定されていない場合は引数で与えられたデフォルト値を使用する。
 	 * 設定されている場合はintに変換して返す。
 	 *
 	 * @param key 設定キー
 	 * @param defaultValue 設定ファイルに設定が無い場合のデフォルト。
 	 * @return
 	 * @throw NumberFormatException 設定が記述されているのにIntegerじゃない場合。
 	 */
 	public int getParameterInt(String name, int defaultValue){
 		return (int)getValueDefault(getParameter(name), defaultValue);
 	}
 
 	public long getParameterLong(String name, long defaultValue){
 		return getValueDefault(getParameter(name), defaultValue);
 	}
 
 	private long getValueDefault(String strValue, long defaultValue){
 		long result = defaultValue;
 		if(!StringUtils.isEmpty(strValue)){
 			result = Long.parseLong(strValue);
 		}
 		return result;
 	}
 }
