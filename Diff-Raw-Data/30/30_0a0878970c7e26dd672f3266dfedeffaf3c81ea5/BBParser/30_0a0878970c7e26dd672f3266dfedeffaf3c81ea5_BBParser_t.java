 package com.tysanclan.site.projectewok.util.bbcode;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.tysanclan.site.projectewok.util.bbcode.ast.BoldNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.ImageNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.ItalicNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.QuoteNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.StrikeThroughNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.TextNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.URLNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.UnderlineNode;
 import com.tysanclan.site.projectewok.util.bbcode.ast.YouTubeNode;
 
 class BBParser {
 	private static final char CLOSE = ']';
 
 	private static final String OPEN = "[";
 
 	private static final Pattern WIDTH = Pattern.compile("width=(\\d+)");
 
 	private static final Pattern HEIGHT = Pattern.compile("height=(\\d+)");
 
 	private enum TagRecognition {
 		YOUTUBE("[youtube]", "[/youtube]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseYoutube(current, buffer);
 			}
 		},
 		BOLD("[b]", "[/b]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseBold(current, buffer);
 			}
 		},
 		UNDERLINE("[u]", "[/u]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseUnderline(current, buffer);
 			}
 		},
 		ITALIC("[i]", "[/i]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseItalic(current, buffer);
 			}
 		},
 		STRIKETHROUGH("[s]", "[/s]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseStrikeThrough(current, buffer);
 			}
 		},
 		QUOTE("[quote", "[/quote]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseQuote(current, buffer);
 			}
 		},
 		URL("[url", "[/url]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseUrl(current, buffer);
 			}
 		},
 		IMG("[img", "[/img]") {
 			@Override
 			public void invokeParser(BBParser parser, BBAstNode current,
 					ParseBuffer buffer) throws BBParseException {
 				parser.parseImage(current, buffer);
 			}
 		};
 
 		private final String opening;
 
 		private final String terminator;
 
 		private TagRecognition(String opening, String terminator) {
 			this.opening = opening;
 			this.terminator = terminator;
 		}
 
 		private boolean check(BBTokenStream stream) {
 			return opening.equals(stream.peekTokens(opening.length()));
 		}
 
 		public abstract void invokeParser(BBParser parser, BBAstNode current,
 				ParseBuffer buffer) throws BBParseException;
 	}
 
 	private BBTokenStream tokenStream;
 
 	private RootNode root;
 
 	public BBParser(String input) {
 		this.tokenStream = new BBTokenStream(input);
 
 	}
 
 	protected void parseStrikeThrough(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		StrikeThroughNode node = new StrikeThroughNode(current);
 
 		current.addChild(node);
 
 		tokenStream.consumeTokens(3);
 
 		parseTag(buffer, node, TagRecognition.STRIKETHROUGH);
 	}
 
 	protected void parseImage(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 
 		tokenStream.consumeTokens(4);
 
 		ImageNode node;
 
 		if ("]".equals(tokenStream.peekTokens(1))) {
 			tokenStream.consumeTokens(1);
 			node = new ImageNode(current);
 		} else {
 			String propstr = tokenStream.peekUntil(CLOSE);
 			tokenStream.consumeTokens(propstr.length() + 1);
 
 			Integer width = null;
 			Integer height = null;
 
 			String[] props = propstr.split("\\s");
 			for (String prop : props) {
 				String p = prop.trim();
 
 				if (p.isEmpty())
 					continue;
 
 				Matcher w = WIDTH.matcher(p);
 				Matcher h = HEIGHT.matcher(p);
 
 				if (w.matches()) {
 					width = Integer.parseInt(w.group(1));
 				} else if (h.matches()) {
 					height = Integer.parseInt(h.group(1));
 				} else {
 					throw new BBParseException(
 							"Invalid image property expression: " + p);
 				}
 			}
 
 			node = new ImageNode(current, width, height);
 		}
 
 		current.addChild(node);
 		parseTag(buffer, node, TagRecognition.IMG);
 
 	}
 
 	protected void parseUrl(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		tokenStream.consumeTokens(4);
 
 		String link = null;
 		if ("=".equals(tokenStream.peekTokens(1))) {
 			tokenStream.consumeToken();
 			link = tokenStream.peekUntil(CLOSE);
 			tokenStream.consumeTokens(link.length());
 		}
 
 		tokenStream.consumeToken();
 
 		URLNode node = new URLNode(current, link);
 		current.addChild(node);
 
 		parseTag(buffer, node, TagRecognition.URL);
 
 		if (node.getChildren().isEmpty()) {
 			throw new BBParseException("Empty link text");
 		} else {
 			StringBuilder children = new StringBuilder();
 			for (BBAstNode child : node.getChildren()) {
 				child.renderTo(children);
 			}
 
 			if (children.length() == 0) {
 				throw new BBParseException("Empty link text");
 			}
 		}
 	}
 
 	protected void parseQuote(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		tokenStream.consumeTokens(6);
 
 		String user = null;
 		if ("=".equals(tokenStream.peekTokens(1))) {
 			tokenStream.consumeToken();
 			user = tokenStream.peekUntil(CLOSE);
 			tokenStream.consumeTokens(user.length());
 		}
 
 		tokenStream.consumeToken();
 
 		QuoteNode node = new QuoteNode(current, user);
 		current.addChild(node);
 
 		parseTag(buffer, node, TagRecognition.QUOTE);
 	}
 
 	protected void parseItalic(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		ItalicNode node = new ItalicNode(current);
 
 		current.addChild(node);
 
 		tokenStream.consumeTokens(3);
 
 		parseTag(buffer, node, TagRecognition.ITALIC);
 	}
 
 	protected void parseUnderline(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		UnderlineNode node = new UnderlineNode(current);
 		current.addChild(node);
 
 		tokenStream.consumeTokens(3);
 
 		parseTag(buffer, node, TagRecognition.UNDERLINE);
 
 	}
 
 	protected void parseBold(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		BoldNode node = new BoldNode(current);
 		current.addChild(node);
 
 		tokenStream.consumeTokens(3);
 
 		parseTag(buffer, node, TagRecognition.BOLD);
 	}
 
 	private void parseTag(ParseBuffer buffer, BBAstNode node, TagRecognition rec)
 			throws BBParseException {
 		String lookahead = tokenStream.peekTokens(rec.terminator.length());
 
 		String prevLookahead = null;
 
 		while (!rec.terminator.equals(lookahead)) {
 			if (!tokenStream.hasMoreTokens() || lookahead.equals(prevLookahead)) {
 				throw new BBParseException(String.format("Missing %s tag",
 						rec.terminator));
 			}
 
 			parseText(node, buffer);
 
 			prevLookahead = lookahead;
 
 			lookahead = tokenStream.peekTokens(rec.terminator.length());
 		}
 
 		if (buffer.length() > 0) {
 			node.addChild(new TextNode(root, buffer.clear()));
 		}
 
 		tokenStream.consumeTokens(rec.terminator.length());
 	}
 
 	protected void parseYoutube(BBAstNode current, ParseBuffer buffer)
 			throws BBParseException {
 		YouTubeNode node = new YouTubeNode(current);
 		current.addChild(node);
 
 		tokenStream.consumeTokens(9);
 
 		parseTag(buffer, node, TagRecognition.YOUTUBE);
 	}
 
 	public BBAstNode getAbstractSyntaxTree() throws BBParseException {
 		if (root == null) {
 			this.root = new RootNode();
 
 			parse();
 		}
 
 		return root;
 	}
 
 	public void parse() throws BBParseException {
 		ParseBuffer buffer = new ParseBuffer();
 
 		while (tokenStream.hasMoreTokens()) {
 			parseText(this.root, buffer);
 		}
 
 		if (buffer.length() > 0) {
 			root.addChild(new TextNode(root, buffer.clear()));
 		}
 
 	}
 
 	private void parseText(BBAstNode node, ParseBuffer buffer)
 			throws BBParseException {
 		String peek = tokenStream.peekTokens(1);
 
 		if (OPEN.equals(peek)) {
 			if (buffer.length() > 0) {
 				node.addChild(new TextNode(node, buffer.clear()));
 			}
 
 			for (TagRecognition rec : TagRecognition.values()) {
 				if (rec.check(tokenStream)) {
 					rec.invokeParser(this, node, buffer);
 					return;
 				}
 			}
 
			// Not a tag
			buffer.append(peek);
			tokenStream.consumeToken();
 		} else {
 			buffer.append(peek);
 			tokenStream.consumeToken();
 		}
 	}
 
 	private static class ParseBuffer {
 		private StringBuilder buffer;
 
 		public ParseBuffer() {
 			buffer = new StringBuilder();
 		}
 
 		public String clear() {
 			String res = buffer.toString();
 			buffer = new StringBuilder();
 
 			return res;
 		}
 
 		public int length() {
 			return buffer.length();
 		}
 
 		public void append(String data) {
 			buffer.append(data);
 		}
 	}
 
 	private static class RootNode extends DefaultNode {
 		public RootNode() {
 			super(null);
 		}
 
 		public void renderTo(StringBuilder builder) {
 			for (BBAstNode node : getChildren()) {
 				node.renderTo(builder);
 			}
 		}
 	}
 }
