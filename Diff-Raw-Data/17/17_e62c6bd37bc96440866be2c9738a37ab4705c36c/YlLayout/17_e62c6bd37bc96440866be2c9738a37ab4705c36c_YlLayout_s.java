 package com.luzi82.yukilayout;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class YlLayout {
 
 	Ele mRoot;
 	int mRootWidth;
 	int mRootHeight;
 
 	TreeMap<String, Ele> mId2Ele = new TreeMap<String, YlLayout.Ele>();
 
 	TreeMap<String, Object> arg = new TreeMap<String, Object>();
 
 	LinkedList<Ele> mElementList = new LinkedList<YlLayout.Ele>();
 
 	public YlLayout(File file) throws ParserConfigurationException,
 			SAXException, IOException {
 		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 		parser.parse(file, new DH());
 	}
 
 	public YlLayout(InputStream inputStream)
 			throws ParserConfigurationException, SAXException, IOException {
 		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 		parser.parse(inputStream, new DH());
 	}
 
 	public void setRootSize(int width, int height) {
 		this.mRootWidth = width;
 		this.mRootHeight = height;
 	}
 
 	public void paint(YlGraphics graphics) {
 		mRoot.paint(graphics);
 	}
 
 	public class DH extends DefaultHandler {
 
 		LinkedList<Ele> stack = new LinkedList<YlLayout.Ele>();
 
 		@Override
 		public void startElement(String uri, String localName, String qName,
 				Attributes attributes) throws SAXException {
 			Ele ele = null;
 			Ele parent = null;
 			if (stack.size() > 0) {
 				parent = stack.getFirst();
 			}
 			if (qName.equals("screen")) {
 				ele = new Screen();
 			} else if (qName.equals("drag")) {
 				ele = new Drag();
 			} else if (qName.equals("trans")) {
 				ele = new Trans();
 			} else if (qName.equals("repeat")) {
 				ele = new Repeat();
 			} else if (qName.equals("text")) {
 				ele = new Text();
 			} else if (qName.equals("void")) {
 				ele = new Void();
 			} else if (qName.equals("img")) {
 				ele = new Img();
 			} else {
 				throw new SAXException("unknown element: " + qName);
 			}
 			ele.processAttributes(attributes);
 			if (parent != null) {
 				parent.child.addFirst(ele);
 				ele.pParent = parent;
 			}
 			stack.push(ele);
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 			if (stack.size() == 1) {
 				mRoot = stack.getFirst();
 			}
 			stack.pop();
 		}
 
 	}
 
 	public abstract class Ele {
 
 		public Ele pParent;
 
 		public LinkedList<Ele> child = new LinkedList<YlLayout.Ele>();
 
 		public TreeMap<String, StoreRule> var = new TreeMap<String, YlLayout.StoreRule>();
 
 		public Ele() {
 			mElementList.add(this);
 		}
 
 		public void processAttributes(Attributes attributes) {
 			int attrLen = attributes.getLength();
 			for (int attrIdx = 0; attrIdx < attrLen; ++attrIdx) {
 				String attrName = attributes.getQName(attrIdx);
 				String attrValue = attributes.getValue(attrIdx);
 				setRule(attrName, attrValue);
 			}
 		}
 
 		public void setRule(String key, String value) {
 			if (key.equals("id")) {
 				mId2Ele.put(value, this);
 			}
 			try {
 				Field f = getClass().getField(key);
 				if ((f != null) && (f.getType() == StoreRule.class)) {
 					StoreRule rule = (StoreRule) f.get(this);
 					rule.set(value);
 				}
 			} catch (SecurityException e) {
 			} catch (NoSuchFieldException e) {
 			} catch (IllegalArgumentException e) {
 			} catch (IllegalAccessException e) {
 			}
 			if (key.startsWith(VAR_PREFIX)
 					&& key.length() > VAR_PREFIX.length()) {
 				String varKey = key.substring(VAR_PREFIX.length());
 				var.put(varKey, new StoreRule(this, value));
 			}
 		}
 
 		public Object cal(String key) throws ParseException {
 			try {
 				Field f = getClass().getField(key);
 				if ((f != null) && (Val.class.isAssignableFrom(f.getType()))) {
 					Val val = (Val) f.get(this);
 					return val.val();
 				}
 			} catch (SecurityException e) {
 			} catch (NoSuchFieldException e) {
 			} catch (IllegalArgumentException e) {
 			} catch (IllegalAccessException e) {
 			}
 			return null;
 		}
 
 		public void paint(YlGraphics graphics) {
 			for (Ele e : child) {
 				e.paint(graphics);
 			}
 		}
 	}
 
 	public class Screen extends Ele {
 
 		public Screen() {
 			super();
 		}
 
 		public StoreRule backgroundColor = new StoreRule(this);
 
 		public Val width = new Val() {
 			@Override
 			public Object val() {
 				return mRootWidth;
 			}
 		};
 
 		public Val height = new Val() {
 			@Override
 			public Object val() {
 				return mRootHeight;
 			}
 		};
 
 		@Override
 		public void paint(YlGraphics graphics) {
 			YlColor backgroundColor = this.backgroundColor.color();
 			if (backgroundColor != null) {
 				graphics.clear(backgroundColor);
 			}
 			super.paint(graphics);
 		}
 	}
 
 	public class Drag extends Ele {
 
 		public StoreRule x = new StoreRule(this, "0");
 		public StoreRule y = new StoreRule(this, "0");
 
 		@Override
 		public void paint(YlGraphics graphics) {
 			Float xf = x.floatt();
 			Float yf = y.floatt();
 			graphics.push();
 			graphics.translate(xf, yf);
 			super.paint(graphics);
 			graphics.pop();
 		}
 
 	}
 
 	public class Repeat extends Ele {
 
 		public StoreRule foreach = new StoreRule(this);
 
 		private int _index = -1;
 		private Object _item = null;
 
 		public Val index = new Val() {
 			@Override
 			public Object val() {
 				return _index;
 			}
 		};
 
 		public Val item = new Val() {
 			@Override
 			public Object val() {
 				return _item;
 			}
 		};
 
 		@Override
 		public void paint(YlGraphics graphics) {
 			List<?> forVal = foreach.list();
 			_index = 0;
 			for (Object v : forVal) {
 				_item = v;
 				super.paint(graphics);
 				++_index;
 			}
 			_index = -1;
 			_item = null;
 		}
 
 	}
 
 	public class Text extends Ele {
 
 		public StoreRule x = new StoreRule(this, "0");
 		public StoreRule y = new StoreRule(this, "0");
 		public StoreRule color = new StoreRule(this);
 		public StoreRule text = new StoreRule(this);
 
 		@Override
 		public void paint(YlGraphics graphics) {
 			graphics.text(x.floatt(), y.floatt(), color.color(), text.string());
 		}
 
 	}
 
 	public class Trans extends Ele {
 
 		public StoreRule x = new StoreRule(this, "0");
 		public StoreRule y = new StoreRule(this, "0");
 
 		@Override
 		public void paint(YlGraphics graphics) {
 			Float xf = x.floatt();
 			Float yf = y.floatt();
 			graphics.push();
 			graphics.translate(xf, yf);
 			super.paint(graphics);
 			graphics.pop();
 		}
 	}
 
 	public class Void extends Ele {
 
 	}
 
 	public class Img extends Ele {
 
 		public StoreRule src = new StoreRule(this, "");
 		public StoreRule x0 = new StoreRule(this, "0");
 		public StoreRule y0 = new StoreRule(this, "0");
 		public StoreRule x1 = new StoreRule(this, "0");
 		public StoreRule y1 = new StoreRule(this, "0");
 		public StoreRule u0 = new StoreRule(this, "0");
 		public StoreRule v0 = new StoreRule(this, "0");
 		public StoreRule u1 = new StoreRule(this, "0");
 		public StoreRule v1 = new StoreRule(this, "0");
 
 		@Override
 		public void paint(YlGraphics graphics) {
 			graphics.img(src.string(), x0.floatt(), y0.floatt(), x1.floatt(),
 					y1.floatt(), u0.floatt(), v0.floatt(), u1.floatt(),
 					v1.floatt());
 		}
 
 	}
 
 	public abstract class Val {
 
 		public abstract Object val();
 
 		public YlColor color() {
 			Object v = val();
 			if (v == null) {
 				return null;
 			} else {
 				return new YlColor(v);
 			}
 		}
 
 		public Float floatt() {
 			Object v = val();
 			if (v == null) {
 				return null;
 			} else if (v instanceof Float) {
 				return (Float) v;
 			} else {
 				return Float.valueOf(v.toString());
 			}
 		}
 
 		public List<?> list() {
 			Object v = val();
 			if (v == null) {
 				return null;
 			} else {
 				return toList(v);
 			}
 		}
 
 		public String string() {
 			Object v = val();
 			if (v == null) {
 				return null;
 			} else {
 				return v.toString();
 			}
 		}
 	}
 
 	public abstract class Rule extends Val {
 
 		final Ele ele;
 		protected String[] ruleExp;
 
 		public Rule(Ele ele) {
 			this.ele = ele;
 		}
 
 		public Object val() {
 			try {
 				if (ruleExp == null) {
					ruleExp = YlExp.parse(rule());
 				}
 				return ruleToVal(ele, ruleExp);
 			} catch (ParseException e) {
 				throw new Error(e);
 			}
 		}
 
 		public abstract String rule();
 
 	}
 
 	public class StoreRule extends Rule {
 
 		String input;
 
 		public StoreRule(Ele ele) {
 			super(ele);
 		}
 
 		public StoreRule(Ele ele, String input) {
 			super(ele);
 			this.input = input;
 		}
 
 		// @Override
 		public void set(String value) {
 			this.input = value;
 			ruleExp = null;
 		}
 
 		@Override
 		public String rule() {
 			return input;
 		}
 
 	}
 
 	public static final String VAR_PREFIX = "var.";
 
 	public Object ruleToVal(Ele ele, String[] ruleExp) throws ParseException {
 		// if (rule == null)
 		// return null;
 		//
 		// String[] exp = YlExp.parse(rule);
 		int offset = 0;
 
 		LinkedList<Object> calStack = new LinkedList<Object>();
 
 		while (offset < ruleExp.length) {
 			String v = ruleExp[offset++];
 			if (v.equals("@")) {
 				int paramLen = Integer.parseInt((String) calStack.pop());
 				Object[] objAry = new Object[paramLen];
 				for (int i = paramLen - 1; i >= 0; --i) {
 					objAry[i] = var(ele, calStack.pop());
 				}
 				String funcName = (String) calStack.pop();
 				Class<?>[] paramClass = new Class[paramLen];
 				Arrays.fill(paramClass, Object.class);
 				try {
 					Method method = YlFunc.class
 							.getMethod(funcName, paramClass);
 					Object ret = method.invoke(null, objAry);
 					calStack.push(ret);
 				} catch (SecurityException e) {
 					// throw new ParseException(rule, rule.length());
 					throw new Error(e);
 				} catch (NoSuchMethodException e) {
 					// System.err.println(funcName);
 					// throw new ParseException(rule, rule.length());
 					throw new Error(e);
 				} catch (IllegalArgumentException e) {
 					throw new Error(e);
 				} catch (IllegalAccessException e) {
 					throw new Error(e);
 				} catch (InvocationTargetException e) {
 					throw new Error(e);
 				}
 			} else if (v.equals(".")) {
 				String b = (String) calStack.pop();
 				Object a = var(ele, calStack.pop());
 				if (a instanceof Ele) {
 					Ele ae = (Ele) a;
 					Object obj = ae.cal(b);
 					calStack.push(obj);
 				} else if (a instanceof String) {
 					calStack.push(((String) a) + "." + b);
 				} else {
 					boolean good = false;
 					if (!good) {
 						try {
 							Method m = a.getClass().getMethod(b);
 							if (m != null) {
 								calStack.push(m.invoke(a));
 								good = true;
 							}
 						} catch (SecurityException e) {
 						} catch (NoSuchMethodException e) {
 						} catch (IllegalArgumentException e) {
 						} catch (IllegalAccessException e) {
 						} catch (InvocationTargetException e) {
 						}
 					}
 					if (!good) {
 						try {
 							Field f = a.getClass().getField(b);
 							if (f != null) {
 								calStack.push(f.get(a));
 								good = true;
 							}
 						} catch (SecurityException e) {
 							e.printStackTrace();
 						} catch (NoSuchFieldException e) {
 							e.printStackTrace();
 						} catch (IllegalArgumentException e) {
 							e.printStackTrace();
 						} catch (IllegalAccessException e) {
 							e.printStackTrace();
 						}
 					}
 					if (!good) {
 						// throw new ParseException(rule, rule.length());
 						throw new Error();
 					}
 				}
 			} else if (v.equals("--")) {
 				Object a = var(ele, calStack.pop());
 				calStack.push(YlFunc.neg(a));
 			} else if (v.equals("+")) {
 				Object b = var(ele, calStack.pop());
 				Object a = var(ele, calStack.pop());
 				calStack.push(YlFunc.add(a, b));
 			} else if (v.equals("-")) {
 				Object b = var(ele, calStack.pop());
 				Object a = var(ele, calStack.pop());
 				calStack.push(YlFunc.sub(a, b));
 			} else if (v.equals("*")) {
 				Object b = var(ele, calStack.pop());
 				Object a = var(ele, calStack.pop());
 				calStack.push(YlFunc.mul(a, b));
 			} else if (v.equals("/")) {
 				Object b = var(ele, calStack.pop());
 				Object a = var(ele, calStack.pop());
 				if ((a instanceof Number) && (b instanceof Number)) {
 					calStack.push(YlFunc.div(a, b));
 				} else if ((a instanceof String) && (b instanceof String)) {
 					calStack.push(((String) a) + "/" + ((String) b));
 				} else {
 					// throw new ParseException(rule, rule.length());
 					throw new Error();
 				}
 			} else {
 				calStack.push(v);
 			}
 		}
 
 		if (calStack.size() != 1) {
 			// throw new ParseException(rule, rule.length());
 			throw new Error("calStack.size() != 1");
 		}
 
 		Object ret = calStack.getFirst();
 		ret = var(ele, ret);
 
 		return ret;
 	}
 
 	public Object var(Ele ele, Object in) throws ParseException {
 		if (in instanceof String) {
 			String s = (String) in;
 			try {
 				return Float.parseFloat(s);
 			} catch (NumberFormatException e) {
 			}
 			if (ele != null) {
 				Object v = ele.cal(s);
 				if (v != null)
 					return v;
 			}
 			while (ele != null) {
 				Rule r = ele.var.get(s);
 				if (r != null) {
 					return r.val();
 				}
 				ele = ele.pParent;
 			}
 			if (mId2Ele.containsKey(s)) {
 				return mId2Ele.get(s);
 			}
 			if (arg.containsKey(s)) {
 				return arg.get(s);
 			}
 			return s;
 		} else {
 			return in;
 		}
 	}
 
 	public void setArg(String key, Object value) {
 		arg.put(key, value);
 	}
 
 	public static List<?> toList(Object obj) {
 		if (obj instanceof List) {
 			return (List<?>) obj;
 		}
 		if (obj instanceof Iterable) {
 			Iterable<?> objIterable = (Iterable<?>) obj;
 			LinkedList<Object> ret = new LinkedList<Object>();
 			for (Object o : objIterable) {
 				ret.add(o);
 			}
 			return ret;
 		}
 		if (obj.getClass().isArray()) {
 			LinkedList<Object> ret = new LinkedList<Object>();
 			int count = Array.getLength(obj);
 			for (int i = 0; i < count; ++i) {
 				ret.add(Array.get(obj, i));
 			}
 			return ret;
 		}
 		throw new ClassCastException();
 	}
 
 	public Ele[] getElementArray() {
 		return mElementList.toArray(new Ele[0]);
 	}
 }
