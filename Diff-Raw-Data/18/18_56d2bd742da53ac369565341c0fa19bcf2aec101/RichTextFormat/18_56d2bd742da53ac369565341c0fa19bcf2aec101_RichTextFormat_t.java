 package jbookreader.fileformats.impl;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jbookreader.book.IBook;
 import jbookreader.book.IBookFactory;
 import jbookreader.book.IContainerNode;
 import jbookreader.book.INode;
 import jbookreader.book.ITextNode;
 import jbookreader.css.CSSParser;
 import jbookreader.fileformats.IErrorHandler;
 import jbookreader.fileformats.IFileFormatDescriptor;
 import jbookreader.style.IStyleRule;
 import jbookreader.style.IStylesheet;
 import lumag.rtf.IRTFContentHandler;
 import lumag.rtf.RTFParser;
 
 import org.xml.sax.SAXException;
 
 class RichTextFormat implements IFileFormatDescriptor {
 
 	private static class RTFHandler implements IRTFContentHandler {
 		private interface IControlHandler {
 			void control(String string, boolean hasParameter, int parameter);
 		}
 
 		private final IBookFactory factory;
 		private final IBook book;
 		private IContainerNode container;
 
 		private final RTFParser parser;
 
 		private Map<String, IControlHandler> controlHandlers =
 			new HashMap<String, IControlHandler>();
 		
 		private int unicodeCharLength = 1;
 		private int skipChars;
 
 		// FIXME: correctly implement non-text destinations
 		private int ignoredLevel = Integer.MAX_VALUE;
 		
 		public RTFHandler(IBookFactory factory, RTFParser parser) {
 			this.factory = factory;
 			this.parser = parser;
 			
 			fillControlHandlers();
 
 			this.book = factory.newBook();
 			IContainerNode body = factory.newContainerNode();
			body.setNodeTag("rtf");
 
 			book.addBody(body, "");
 			container = factory.newContainerNode();
			container.setNodeTag("par");
 			body.add(container);
 		}
 
 		private void fillControlHandlers() {
 			controlHandlers.put("rtf", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					System.out.println("RTF version " + parameter);
 				}
 			});
 			controlHandlers.put("ansicpg", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					parser.setCharacterSet(parameter);
 				}
 			});
 			controlHandlers.put("par", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					IContainerNode node = factory.newContainerNode();
					node.setNodeTag("par");
 					container.getParentNode().add(node);
 					container = node;
 				}
 			});
 			controlHandlers.put("\n", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					IContainerNode node = factory.newContainerNode();
					node.setNodeTag("par");
 					container.getParentNode().add(node);
 					container = node;
 				}
 			});
 			controlHandlers.put("\r", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					IContainerNode node = factory.newContainerNode();
					node.setNodeTag("par");
 					container.getParentNode().add(node);
 					container = node;
 				}
 			});
 			controlHandlers.put("uc", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					unicodeCharLength = parameter;
 				}
 			});
 			controlHandlers.put("u", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					string(Character.valueOf((char) parameter).toString());
 					skipChars = unicodeCharLength;
 				}
 			});
 			controlHandlers.put("~", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					// XXX: nbsp
 					string("\u00A0");
 				}
 			});
 			controlHandlers.put(" ", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					// XXX: nbsp
 					string("\u00A0");
 				}
 			});
 			controlHandlers.put("_", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					// XXX: nb dash;
 					string("\u2011");
 				}
 			});
 			controlHandlers.put("fonttbl", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					ignoredLevel = parser.getLevel();
 				}
 			});
 			controlHandlers.put("stylesheet", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					ignoredLevel = parser.getLevel();
 				}
 			});
 			controlHandlers.put("pict", new IControlHandler() {
 				public void control(String string, boolean hasParameter, int parameter) {
 					ignoredLevel = parser.getLevel();
 				}
 			});
 		}
 
 		public boolean control(String string, boolean hasParameter, int parameter) {
 			if (parser.getLevel() >= ignoredLevel) {
 				// assume we support all controls in ignored destinations
 				return true;
 			}
 			
 			if (controlHandlers.containsKey(string)) {
 				controlHandlers.get(string).control(string, hasParameter, parameter);
 				return true;
 			}
 			
 			return false;
 		}
 
 		public void endGroup() {
 			if (parser.getLevel() <= ignoredLevel) {
 				ignoredLevel = Integer.MAX_VALUE;
 			}
 //			System.out.println("end " + parser.getLevel());
 		}
 
 		public void startGroup() {
 //			System.out.println("start " + parser.getLevel());
 		}
 
 		public void string(String string) {
 			if (parser.getLevel() >= ignoredLevel) {
 				return;
 			}
 			
 			if (string.length() == 0) {
 				return;
 			}
 			
 			if (skipChars >= string.length()) {
 				skipChars -= string.length();
 			} else {
 				ITextNode node = factory.newTextNode();
 				node.setText(string.substring(skipChars));
 				container.add(node);
 				skipChars = 0;
 			}
 		}
 
 		public IBook getBook() {
 			return book;
 		}
 
 		public void binaryBlob(byte[] bs) {
 			skipChars--;
 		}
 
 	}
 
 	private final Collection<String> extensions;
 	private IStylesheet<INode> stylesheet;
 	
 	public RichTextFormat() {
 		extensions = new ArrayList<String>();
 		extensions.add(".rtf");
 	}
 
 	public IBook parse(String uri, IErrorHandler handler, IBookFactory factory)
 			throws SAXException, IOException {
 		RTFParser parser = new RTFParser();
 		RTFHandler rtfHandler = new RTFHandler(factory, parser);
 		parser.setHandler(rtfHandler);
 		InputStream stream = new BufferedInputStream(
 				new FileInputStream(uri));
 		byte[] buffer = new byte[1024];
 		while (true) {
 			int len = stream.read(buffer);
 			if (len < 0) {
 				break;
 			}
 			parser.process(buffer, 0, len);
 		}
 		stream.close();
 		return rtfHandler.getBook();
 	}
 
 	public String getDescription() {
 		return "Rich Text Format";
 	}
 
 	public Collection<String> getExtensions() {
 		return extensions;
 	}
 
 	public IStylesheet<INode> getStylesheet() {
 		if (stylesheet == null) {
 			try {
 				stylesheet = CSSParser.parse("resources/css/rtf.css");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (stylesheet == null) {
 			return new IStylesheet<INode>() {
 				public List<IStyleRule> getApplicableRules(INode node) {
 					return new LinkedList<IStyleRule>();
 				}
 			};
 		}
 		return stylesheet;
 	}
 
 }
