 package impl.ananas.axk2.core;
 
 import java.io.IOException;
 import java.io.Reader;
 
 import javax.xml.parsers.DocumentBuilder;
 
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.InputSource;
 
 import ananas.axk2.core.DefaultAddress;
 import ananas.axk2.core.XmppAddress;
 import ananas.axk2.core.XmppContext;
 import ananas.axk2.core.util.StanzaContext;
 import ananas.blueprint4.core.BPEnvironment;
 import ananas.blueprint4.core.lang.BPElement;
 import ananas.blueprint4.core.util.BPDocumentBuilder;
 
 public class DefaultStanzaContext implements StanzaContext {
 
 	private final XmppContext _context;
 	private Element _element;
 	private Object _object;
 	private String _string;
 
 	public DefaultStanzaContext(XmppContext context) {
 		this._context = context;
 	}
 
 	@Override
 	public Element getElement() {
 		Element element = this._element;
 		if (element == null) {
 			this._element = element = this.__stringToElement(this._string);
 		}
 		return element;
 	}
 
 	@Override
 	public Object getObject() {
 		Object obj = this._object;
 		if (obj == null) {
 			Element element = this.getElement();
 			this._object = obj = this.__elementToObject(element);
 		}
 		return obj;
 	}
 
 	@Override
 	public String getString() {
 		String str = this._string;
 		if (str == null) {
 			this._string = str = this.__elementToString(this._element);
 		}
 		return str;
 	}
 
 	private Object __elementToObject(Element element) {
 		BPEnvironment bp = this._context.getBlueprintEnvironment();
 		BPDocumentBuilder builder = bp.getBPDocumentBuilderFactory()
 				.createBuilder();
 		BPElement bpe = builder.build(bp, element);
 		return bpe.getTarget(true);
 	}
 
 	private String __elementToString(Element element) {
 		if (element == null) {
 			return null;
 		}
 		DOMImplementation impl = element.getOwnerDocument().getImplementation();
 		DOMImplementationLS ls = (DOMImplementationLS) impl.getFeature("LS",
 				"3.0");
 		LSSerializer seri = ls.createLSSerializer();
 		return seri.writeToString(element);
 	}
 
 	private Element __stringToElement(String str) {
 		try {
 			if (str == null)
 				return null;
 			DocumentBuilder builder = this._context.getBlueprintEnvironment()
 					.getDOMDocumentBuilderFactory().newDocumentBuilder();
 			Reader in = new MyStringReader(str);
 			Document doc = builder.parse(new InputSource(in));
 			return doc.getDocumentElement();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private String __getAttr(String name) {
 		Element element = this.getElement();
 		if (element == null) {
 			return null;
 		}
 		String ret = element.getAttribute(name);
 		if (ret == null) {
 			return null;
 		} else if (ret.length() <= 0) {
 			return null;
 		} else {
 			return ret;
 		}
 	}
 
 	class MyStringReader extends Reader {
 
 		private final char[] _chs;
 		private final int _len;
 		private int _ptr;
 
 		public MyStringReader(String str) {
 			this._chs = str.toCharArray();
 			this._len = _chs.length;
 			this._ptr = 0;
 		}
 
 		@Override
 		public void close() throws IOException {
 			this._ptr = this._len;
 		}
 
 		@Override
 		public int read(char[] cbuf, int off, int len) throws IOException {
 			final int off_src = this._ptr;
 			final int len_src = this._len - off_src;
 			final int len_ret = (len < len_src) ? len : len_src;
 			final char[] src = this._chs;
 			for (int i = len_ret - 1; i >= 0; i--) {
 				cbuf[off + i] = src[off_src + i];
 			}
 			this._ptr = off_src + len_ret;
 			return len_ret;
 		}
 	}
 
 	@Override
 	public void setElement(Element element) {
 		this._string = null;
 		this._element = element;
 		this._object = null;
 	}
 
 	@Override
 	public void setString(String string) {
 		this._string = string;
 		this._element = null;
 		this._object = null;
 	}
 
 	@Override
 	public void setObject(Object object) {
 		this._string = null;
 		this._element = null;
 		this._object = object;
 	}
 
 	@Override
 	public String get_id() {
 		return this.__getAttr("id");
 	}
 
 	@Override
 	public XmppAddress get_from() {
 		String s = this.__getAttr("from");
 		if (s == null) {
 			return null;
 		} else {
 			return new DefaultAddress(s);
 		}
 	}
 
 	@Override
 	public XmppAddress get_to() {
 		String s = this.__getAttr("to");
 		if (s == null) {
 			return null;
 		} else {
 			return new DefaultAddress(s);
 		}
 	}
 
 	@Override
 	public String get_type() {
 		return this.__getAttr("type");
 	}
 
 	@Override
 	public String get_xml_lang() {
 		return this.__getAttr("xml:lang");
 	}
 }
