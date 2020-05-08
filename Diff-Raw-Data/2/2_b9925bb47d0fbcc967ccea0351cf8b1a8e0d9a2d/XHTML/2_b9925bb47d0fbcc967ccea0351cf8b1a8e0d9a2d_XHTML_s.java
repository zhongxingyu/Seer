 /*
 	XHTML.java
 
 	Author: David Fogel
 	Copyright 2009 David Fogel
 	All rights reserved.
 */
 
 package net.markout.xhtml;
 
 import java.io.*;
 
 import net.markout.*;
 import net.markout.support.*;
 import net.markout.types.*;
 
 /**
  * XHTML
  * 
  * THIS IS A GENERATED FILE, DO NOT EDIT!
  */
 public class XHTML extends DocumentWriterFactory {
 	// *** Class Members ***
 	public static final NamespaceURI NAMESPACE = new NamespaceURI("http://www.w3.org/1999/xhtml");
 	public static final NamespaceURI XML_NAMESPACE = new NamespaceURI("http://www.w3.org/XML/1998/namespace", "xml");
 	
 	public static final Name A = new Name(NAMESPACE,"a");
 	public static final Name ABBR = new Name(NAMESPACE,"abbr");
 	public static final Name ACCEPT = new Name(NAMESPACE,"accept");
 	public static final Name ACCEPT_CHARSET = new Name(NAMESPACE,"accept-charset");
 	public static final Name ACCESSKEY = new Name(NAMESPACE,"accesskey");
 	public static final Name ACRONYM = new Name(NAMESPACE,"acronym");
 	public static final Name ACTION = new Name(NAMESPACE,"action");
 	public static final Name ADDRESS = new Name(NAMESPACE,"address");
 	public static final Name ALIGN = new Name(NAMESPACE,"align");
 	public static final Name ALINK = new Name(NAMESPACE,"alink");
 	public static final Name ALT = new Name(NAMESPACE,"alt");
 	public static final Name APPLET = new Name(NAMESPACE,"applet");
 	public static final Name ARCHIVE = new Name(NAMESPACE,"archive");
 	public static final Name AREA = new Name(NAMESPACE,"area");
 	public static final Name AXIS = new Name(NAMESPACE,"axis");
 	public static final Name B = new Name(NAMESPACE,"b");
 	public static final Name BACKGROUND = new Name(NAMESPACE,"background");
 	public static final Name BASE = new Name(NAMESPACE,"base");
 	public static final Name BASEFONT = new Name(NAMESPACE,"basefont");
 	public static final Name BDO = new Name(NAMESPACE,"bdo");
 	public static final Name BGCOLOR = new Name(NAMESPACE,"bgcolor");
 	public static final Name BIG = new Name(NAMESPACE,"big");
 	public static final Name BLOCKQUOTE = new Name(NAMESPACE,"blockquote");
 	public static final Name BODY = new Name(NAMESPACE,"body");
 	public static final Name BORDER = new Name(NAMESPACE,"border");
 	public static final Name BR = new Name(NAMESPACE,"br");
 	public static final Name BUTTON = new Name(NAMESPACE,"button");
 	public static final Name CAPTION = new Name(NAMESPACE,"caption");
 	public static final Name CELLPADDING = new Name(NAMESPACE,"cellpadding");
 	public static final Name CELLSPACING = new Name(NAMESPACE,"cellspacing");
 	public static final Name CENTER = new Name(NAMESPACE,"center");
 	public static final Name CHAR = new Name(NAMESPACE,"char");
 	public static final Name CHAROFF = new Name(NAMESPACE,"charoff");
 	public static final Name CHARSET = new Name(NAMESPACE,"charset");
 	public static final Name CHECKED = new Name(NAMESPACE,"checked");
 	public static final Name CITE = new Name(NAMESPACE,"cite");
 	public static final Name CLASS = new Name(NAMESPACE,"class");
 	public static final Name CLASSID = new Name(NAMESPACE,"classid");
 	public static final Name CLEAR = new Name(NAMESPACE,"clear");
 	public static final Name CODE = new Name(NAMESPACE,"code");
 	public static final Name CODEBASE = new Name(NAMESPACE,"codebase");
 	public static final Name CODETYPE = new Name(NAMESPACE,"codetype");
 	public static final Name COL = new Name(NAMESPACE,"col");
 	public static final Name COLGROUP = new Name(NAMESPACE,"colgroup");
 	public static final Name COLOR = new Name(NAMESPACE,"color");
 	public static final Name COLS = new Name(NAMESPACE,"cols");
 	public static final Name COLSPAN = new Name(NAMESPACE,"colspan");
 	public static final Name COMPACT = new Name(NAMESPACE,"compact");
 	public static final Name CONTENT = new Name(NAMESPACE,"content");
 	public static final Name COORDS = new Name(NAMESPACE,"coords");
 	public static final Name DATA = new Name(NAMESPACE,"data");
 	public static final Name DATETIME = new Name(NAMESPACE,"datetime");
 	public static final Name DD = new Name(NAMESPACE,"dd");
 	public static final Name DECLARE = new Name(NAMESPACE,"declare");
 	public static final Name DEFER = new Name(NAMESPACE,"defer");
 	public static final Name DEL = new Name(NAMESPACE,"del");
 	public static final Name DFN = new Name(NAMESPACE,"dfn");
 	public static final Name DIR = new Name(NAMESPACE,"dir");
 	public static final Name DISABLED = new Name(NAMESPACE,"disabled");
 	public static final Name DIV = new Name(NAMESPACE,"div");
 	public static final Name DL = new Name(NAMESPACE,"dl");
 	public static final Name DT = new Name(NAMESPACE,"dt");
 	public static final Name EM = new Name(NAMESPACE,"em");
 	public static final Name ENCTYPE = new Name(NAMESPACE,"enctype");
 	public static final Name FACE = new Name(NAMESPACE,"face");
 	public static final Name FIELDSET = new Name(NAMESPACE,"fieldset");
 	public static final Name FONT = new Name(NAMESPACE,"font");
 	public static final Name FOR = new Name(NAMESPACE,"for");
 	public static final Name FORM = new Name(NAMESPACE,"form");
 	public static final Name FRAME = new Name(NAMESPACE,"frame");
 	public static final Name FRAMEBORDER = new Name(NAMESPACE,"frameborder");
 	public static final Name FRAMESET = new Name(NAMESPACE,"frameset");
 	public static final Name H1 = new Name(NAMESPACE,"h1");
 	public static final Name H2 = new Name(NAMESPACE,"h2");
 	public static final Name H3 = new Name(NAMESPACE,"h3");
 	public static final Name H4 = new Name(NAMESPACE,"h4");
 	public static final Name H5 = new Name(NAMESPACE,"h5");
 	public static final Name H6 = new Name(NAMESPACE,"h6");
 	public static final Name HEAD = new Name(NAMESPACE,"head");
 	public static final Name HEADERS = new Name(NAMESPACE,"headers");
 	public static final Name HEIGHT = new Name(NAMESPACE,"height");
 	public static final Name HR = new Name(NAMESPACE,"hr");
 	public static final Name HREF = new Name(NAMESPACE,"href");
 	public static final Name HREFLANG = new Name(NAMESPACE,"hreflang");
 	public static final Name HSPACE = new Name(NAMESPACE,"hspace");
 	public static final Name HTML = new Name(NAMESPACE,"html");
 	public static final Name HTTP_EQUIV = new Name(NAMESPACE,"http-equiv");
 	public static final Name I = new Name(NAMESPACE,"i");
 	public static final Name ID = new Name(NAMESPACE,"id");
 	public static final Name IFRAME = new Name(NAMESPACE,"iframe");
 	public static final Name IMG = new Name(NAMESPACE,"img");
 	public static final Name INPUT = new Name(NAMESPACE,"input");
 	public static final Name INS = new Name(NAMESPACE,"ins");
 	public static final Name ISINDEX = new Name(NAMESPACE,"isindex");
 	public static final Name ISMAP = new Name(NAMESPACE,"ismap");
 	public static final Name KBD = new Name(NAMESPACE,"kbd");
 	public static final Name LABEL = new Name(NAMESPACE,"label");
 	public static final Name LANG = new Name(NAMESPACE,"lang");
 	public static final Name LANGUAGE = new Name(NAMESPACE,"language");
 	public static final Name LEGEND = new Name(NAMESPACE,"legend");
 	public static final Name LI = new Name(NAMESPACE,"li");
 	public static final Name LINK = new Name(NAMESPACE,"link");
 	public static final Name LONGDESC = new Name(NAMESPACE,"longdesc");
 	public static final Name MAP = new Name(NAMESPACE,"map");
 	public static final Name MARGINHEIGHT = new Name(NAMESPACE,"marginheight");
 	public static final Name MARGINWIDTH = new Name(NAMESPACE,"marginwidth");
 	public static final Name MAXLENGTH = new Name(NAMESPACE,"maxlength");
 	public static final Name MEDIA = new Name(NAMESPACE,"media");
 	public static final Name MENU = new Name(NAMESPACE,"menu");
 	public static final Name META = new Name(NAMESPACE,"meta");
 	public static final Name METHOD = new Name(NAMESPACE,"method");
 	public static final Name MULTIPLE = new Name(NAMESPACE,"multiple");
 	public static final Name NAME = new Name(NAMESPACE,"name");
 	public static final Name NOFRAMES = new Name(NAMESPACE,"noframes");
 	public static final Name NOHREF = new Name(NAMESPACE,"nohref");
 	public static final Name NORESIZE = new Name(NAMESPACE,"noresize");
 	public static final Name NOSCRIPT = new Name(NAMESPACE,"noscript");
 	public static final Name NOSHADE = new Name(NAMESPACE,"noshade");
 	public static final Name NOWRAP = new Name(NAMESPACE,"nowrap");
 	public static final Name OBJECT = new Name(NAMESPACE,"object");
 	public static final Name OL = new Name(NAMESPACE,"ol");
 	public static final Name ONBLUR = new Name(NAMESPACE,"onblur");
 	public static final Name ONCHANGE = new Name(NAMESPACE,"onchange");
 	public static final Name ONCLICK = new Name(NAMESPACE,"onclick");
 	public static final Name ONDBLCLICK = new Name(NAMESPACE,"ondblclick");
 	public static final Name ONFOCUS = new Name(NAMESPACE,"onfocus");
 	public static final Name ONKEYDOWN = new Name(NAMESPACE,"onkeydown");
 	public static final Name ONKEYPRESS = new Name(NAMESPACE,"onkeypress");
 	public static final Name ONKEYUP = new Name(NAMESPACE,"onkeyup");
 	public static final Name ONLOAD = new Name(NAMESPACE,"onload");
 	public static final Name ONMOUSEDOWN = new Name(NAMESPACE,"onmousedown");
 	public static final Name ONMOUSEMOVE = new Name(NAMESPACE,"onmousemove");
 	public static final Name ONMOUSEOUT = new Name(NAMESPACE,"onmouseout");
 	public static final Name ONMOUSEOVER = new Name(NAMESPACE,"onmouseover");
 	public static final Name ONMOUSEUP = new Name(NAMESPACE,"onmouseup");
 	public static final Name ONRESET = new Name(NAMESPACE,"onreset");
 	public static final Name ONSELECT = new Name(NAMESPACE,"onselect");
 	public static final Name ONSUBMIT = new Name(NAMESPACE,"onsubmit");
 	public static final Name ONUNLOAD = new Name(NAMESPACE,"onunload");
 	public static final Name OPTGROUP = new Name(NAMESPACE,"optgroup");
 	public static final Name OPTION = new Name(NAMESPACE,"option");
 	public static final Name P = new Name(NAMESPACE,"p");
 	public static final Name PARAM = new Name(NAMESPACE,"param");
 	public static final Name PRE = new Name(NAMESPACE,"pre");
 	public static final Name PROFILE = new Name(NAMESPACE,"profile");
 	public static final Name PROMPT = new Name(NAMESPACE,"prompt");
 	public static final Name Q = new Name(NAMESPACE,"q");
 	public static final Name READONLY = new Name(NAMESPACE,"readonly");
 	public static final Name REL = new Name(NAMESPACE,"rel");
 	public static final Name REV = new Name(NAMESPACE,"rev");
 	public static final Name ROWS = new Name(NAMESPACE,"rows");
 	public static final Name ROWSPAN = new Name(NAMESPACE,"rowspan");
 	public static final Name RULES = new Name(NAMESPACE,"rules");
 	public static final Name S = new Name(NAMESPACE,"s");
 	public static final Name SAMP = new Name(NAMESPACE,"samp");
 	public static final Name SCHEME = new Name(NAMESPACE,"scheme");
 	public static final Name SCOPE = new Name(NAMESPACE,"scope");
 	public static final Name SCRIPT = new Name(NAMESPACE,"script");
 	public static final Name SCROLLING = new Name(NAMESPACE,"scrolling");
 	public static final Name SELECT = new Name(NAMESPACE,"select");
 	public static final Name SELECTED = new Name(NAMESPACE,"selected");
 	public static final Name SHAPE = new Name(NAMESPACE,"shape");
 	public static final Name SIZE = new Name(NAMESPACE,"size");
 	public static final Name SMALL = new Name(NAMESPACE,"small");
 	public static final Name SPAN = new Name(NAMESPACE,"span");
 	public static final Name SRC = new Name(NAMESPACE,"src");
 	public static final Name STANDBY = new Name(NAMESPACE,"standby");
 	public static final Name START = new Name(NAMESPACE,"start");
 	public static final Name STRIKE = new Name(NAMESPACE,"strike");
 	public static final Name STRONG = new Name(NAMESPACE,"strong");
 	public static final Name STYLE = new Name(NAMESPACE,"style");
 	public static final Name SUB = new Name(NAMESPACE,"sub");
 	public static final Name SUMMARY = new Name(NAMESPACE,"summary");
 	public static final Name SUP = new Name(NAMESPACE,"sup");
 	public static final Name TABINDEX = new Name(NAMESPACE,"tabindex");
 	public static final Name TABLE = new Name(NAMESPACE,"table");
 	public static final Name TARGET = new Name(NAMESPACE,"target");
 	public static final Name TBODY = new Name(NAMESPACE,"tbody");
 	public static final Name TD = new Name(NAMESPACE,"td");
 	public static final Name TEXT = new Name(NAMESPACE,"text");
 	public static final Name TEXTAREA = new Name(NAMESPACE,"textarea");
 	public static final Name TFOOT = new Name(NAMESPACE,"tfoot");
 	public static final Name TH = new Name(NAMESPACE,"th");
 	public static final Name THEAD = new Name(NAMESPACE,"thead");
 	public static final Name TITLE = new Name(NAMESPACE,"title");
 	public static final Name TR = new Name(NAMESPACE,"tr");
 	public static final Name TT = new Name(NAMESPACE,"tt");
 	public static final Name TYPE = new Name(NAMESPACE,"type");
 	public static final Name U = new Name(NAMESPACE,"u");
 	public static final Name UL = new Name(NAMESPACE,"ul");
 	public static final Name USEMAP = new Name(NAMESPACE,"usemap");
 	public static final Name VALIGN = new Name(NAMESPACE,"valign");
 	public static final Name VALUE = new Name(NAMESPACE,"value");
 	public static final Name VALUETYPE = new Name(NAMESPACE,"valuetype");
 	public static final Name VAR = new Name(NAMESPACE,"var");
 	public static final Name VLINK = new Name(NAMESPACE,"vlink");
 	public static final Name VSPACE = new Name(NAMESPACE,"vspace");
 	public static final Name WIDTH = new Name(NAMESPACE,"width");
 	public static final Name XMLNS = new Name(NAMESPACE,"xmlns");
 	public static final Name XML_LANG = new Name(XML_NAMESPACE,"lang");
 	public static final Name XML_SPACE = new Name(XML_NAMESPACE,"space");
 	
 	public static final Attribute ALIGN_BOTTOM = ALIGN.att("bottom");
 	public static final Attribute ALIGN_CENTER = ALIGN.att("center");
 	public static final Attribute ALIGN_CHAR = ALIGN.att("char");
 	public static final Attribute ALIGN_JUSTIFY = ALIGN.att("justify");
 	public static final Attribute ALIGN_LEFT = ALIGN.att("left");
 	public static final Attribute ALIGN_MIDDLE = ALIGN.att("middle");
 	public static final Attribute ALIGN_RIGHT = ALIGN.att("right");
 	public static final Attribute ALIGN_TOP = ALIGN.att("top");
 	public static final Attribute CHECKED_CHECKED = CHECKED.att("checked");
 	public static final Attribute CLEAR_ALL = CLEAR.att("all");
 	public static final Attribute CLEAR_LEFT = CLEAR.att("left");
 	public static final Attribute CLEAR_NONE = CLEAR.att("none");
 	public static final Attribute CLEAR_RIGHT = CLEAR.att("right");
 	public static final Attribute COLSPAN_1 = COLSPAN.att("1");
 	public static final Attribute COMPACT_COMPACT = COMPACT.att("compact");
 	public static final Attribute DECLARE_DECLARE = DECLARE.att("declare");
 	public static final Attribute DEFER_DEFER = DEFER.att("defer");
 	public static final Attribute DIR_LTR = DIR.att("ltr");
 	public static final Attribute DIR_RTL = DIR.att("rtl");
 	public static final Attribute DISABLED_DISABLED = DISABLED.att("disabled");
 	public static final Attribute ENCTYPE_APPLICATION_X_WWW_FORM_URLENCODED = ENCTYPE.att("application/x-www-form-urlencoded");
 	public static final Attribute FRAME_ABOVE = FRAME.att("above");
 	public static final Attribute FRAME_BELOW = FRAME.att("below");
 	public static final Attribute FRAME_BORDER = FRAME.att("border");
 	public static final Attribute FRAME_BOX = FRAME.att("box");
 	public static final Attribute FRAME_HSIDES = FRAME.att("hsides");
 	public static final Attribute FRAME_LHS = FRAME.att("lhs");
 	public static final Attribute FRAME_RHS = FRAME.att("rhs");
 	public static final Attribute FRAME_VOID = FRAME.att("void");
 	public static final Attribute FRAME_VSIDES = FRAME.att("vsides");
 	public static final Attribute FRAMEBORDER_0 = FRAMEBORDER.att("0");
 	public static final Attribute FRAMEBORDER_1 = FRAMEBORDER.att("1");
 	public static final Attribute ISMAP_ISMAP = ISMAP.att("ismap");
 	public static final Attribute METHOD_GET = METHOD.att("get");
 	public static final Attribute METHOD_POST = METHOD.att("post");
 	public static final Attribute MULTIPLE_MULTIPLE = MULTIPLE.att("multiple");
 	public static final Attribute NOHREF_NOHREF = NOHREF.att("nohref");
 	public static final Attribute NORESIZE_NORESIZE = NORESIZE.att("noresize");
 	public static final Attribute NOSHADE_NOSHADE = NOSHADE.att("noshade");
 	public static final Attribute NOWRAP_NOWRAP = NOWRAP.att("nowrap");
 	public static final Attribute READONLY_READONLY = READONLY.att("readonly");
 	public static final Attribute ROWSPAN_1 = ROWSPAN.att("1");
 	public static final Attribute RULES_ALL = RULES.att("all");
 	public static final Attribute RULES_COLS = RULES.att("cols");
 	public static final Attribute RULES_GROUPS = RULES.att("groups");
 	public static final Attribute RULES_NONE = RULES.att("none");
 	public static final Attribute RULES_ROWS = RULES.att("rows");
 	public static final Attribute SCOPE_COL = SCOPE.att("col");
 	public static final Attribute SCOPE_COLGROUP = SCOPE.att("colgroup");
 	public static final Attribute SCOPE_ROW = SCOPE.att("row");
 	public static final Attribute SCOPE_ROWGROUP = SCOPE.att("rowgroup");
 	public static final Attribute SCROLLING_AUTO = SCROLLING.att("auto");
 	public static final Attribute SCROLLING_NO = SCROLLING.att("no");
 	public static final Attribute SCROLLING_YES = SCROLLING.att("yes");
 	public static final Attribute SELECTED_SELECTED = SELECTED.att("selected");
 	public static final Attribute SHAPE_CIRCLE = SHAPE.att("circle");
 	public static final Attribute SHAPE_DEFAULT = SHAPE.att("default");
 	public static final Attribute SHAPE_POLY = SHAPE.att("poly");
 	public static final Attribute SHAPE_RECT = SHAPE.att("rect");
 	public static final Attribute SPAN_1 = SPAN.att("1");
 	public static final Attribute TYPE_BUTTON = TYPE.att("button");
 	public static final Attribute TYPE_CHECKBOX = TYPE.att("checkbox");
 	public static final Attribute TYPE_CIRCLE = TYPE.att("circle");
 	public static final Attribute TYPE_DISC = TYPE.att("disc");
 	public static final Attribute TYPE_FILE = TYPE.att("file");
 	public static final Attribute TYPE_HIDDEN = TYPE.att("hidden");
 	public static final Attribute TYPE_IMAGE = TYPE.att("image");
 	public static final Attribute TYPE_PASSWORD = TYPE.att("password");
 	public static final Attribute TYPE_RADIO = TYPE.att("radio");
 	public static final Attribute TYPE_RESET = TYPE.att("reset");
 	public static final Attribute TYPE_SQUARE = TYPE.att("square");
 	public static final Attribute TYPE_SUBMIT = TYPE.att("submit");
 	public static final Attribute TYPE_TEXT = TYPE.att("text");
 	public static final Attribute VALIGN_BASELINE = VALIGN.att("baseline");
 	public static final Attribute VALIGN_BOTTOM = VALIGN.att("bottom");
 	public static final Attribute VALIGN_MIDDLE = VALIGN.att("middle");
 	public static final Attribute VALIGN_TOP = VALIGN.att("top");
 	public static final Attribute VALUETYPE_DATA = VALUETYPE.att("data");
 	public static final Attribute VALUETYPE_OBJECT = VALUETYPE.att("object");
 	public static final Attribute VALUETYPE_REF = VALUETYPE.att("ref");
 	public static final Attribute XMLNS_HTTP___WWW_W3_ORG_1999_XHTML = XMLNS.att("http://www.w3.org/1999/xhtml");
 	public static final Attribute XML_SPACE_PRESERVE = XML_SPACE.att("preserve");
 	
 	public static final PublicIDLiteral STRICT_PUBLIC_ID = new PublicIDLiteral("-//W3C//DTD XHTML 1.0 Strict//EN");
 	public static final SystemLiteral STRICT_SYSTEM_ID = new SystemLiteral("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
 	public static final PublicIDLiteral TRANSITIONAL_PUBLIC_ID = new PublicIDLiteral("-//W3C//DTD XHTML 1.0 Transitional//EN");
 	public static final SystemLiteral TRANSITIONAL_SYSTEM_ID = new SystemLiteral("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
 	public static final PublicIDLiteral FRAMESET_PUBLIC_ID = new PublicIDLiteral("-//W3C//DTD XHTML 1.0 Frameset//EN");
 	public static final SystemLiteral FRAMESET_SYSTEM_ID = new SystemLiteral("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd");
 	
 	public static final EmptyElementPolicy EMPTY_ELEMENT_POLICY = 
 		new NamedEmptyElementPolicy(AREA, BASE, BASEFONT, BR, COL, FRAME, HR, IMG, INPUT, ISINDEX, LINK, META, PARAM);
 	static {
 		((NamedEmptyElementPolicy) EMPTY_ELEMENT_POLICY).setRequiresSpaceBeforeClosing(true);
 	}
 
 	// *** Public Methods ***
 	
 	public static HtmlDocumentWriter htmlDocumentWriter(OutputStream out) throws IOException {
 		return htmlDocumentWriter(out, "UTF-8");
 	}
 	
 	public static HtmlDocumentWriter htmlDocumentWriter(OutputStream out, String charset) throws IOException {
 		return htmlDocumentWriter(new OSXMLChunkWriter(out, charset));
 	}
 	
	public static HtmlDocumentWriter htmlDocumentWriter(boolean declareVersion, boolean declareDTD, Writer out) throws IOException {
 		return htmlDocumentWriter(new WriterXMLChunkWriter(out));
 	}
 	
 	public static HtmlDocumentWriter htmlDocumentWriter(XMLChunkWriter out) throws IOException {
 	
 		XMLOutputContext oc = new XMLOutputContext(out);
 		
 		oc.setEmptyElementPolicy(EMPTY_ELEMENT_POLICY);
 		
 		HtmlDocumentWriter dw = new HtmlDocumentWriter(oc);
 		
 		dw.defaultNamespace(NAMESPACE);
 		
 		return dw;
 	}
 	
 }
 
 
 
