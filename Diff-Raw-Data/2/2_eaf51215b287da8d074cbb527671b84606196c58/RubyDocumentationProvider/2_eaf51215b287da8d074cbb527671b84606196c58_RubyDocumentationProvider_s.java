 package org.eclipse.dltk.ruby.internal.ui.documentation;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.ruby.core.model.FakeMethod;
 import org.eclipse.dltk.ruby.internal.ui.docs.RiHelper;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyPartitionScanner;
 import org.eclipse.dltk.ruby.ui.text.IRubyPartitions;
 import org.eclipse.dltk.ui.documentation.IScriptDocumentationProvider;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.rules.FastPartitioner;
 
 
 public class RubyDocumentationProvider implements IScriptDocumentationProvider {
 		
 	protected String getLine (Document d, int line) throws BadLocationException {
 		return d.get(d.getLineOffset(line), d.getLineLength(line));
 	}
 	
 	/**
 	 * Installs a partitioner with <code>document</code>.
 	 * 
 	 * @param document
 	 *            the document
 	 */
 	private static void installStuff(Document document) {
 		String[] types = new String[] { IRubyPartitions.RUBY_STRING, IRubyPartitions.RUBY_COMMENT,
 				IDocument.DEFAULT_CONTENT_TYPE };
 		FastPartitioner partitioner = new FastPartitioner(new RubyPartitionScanner(), types);
 		partitioner.connect(document);
 		document.setDocumentPartitioner(IRubyPartitions.RUBY_PARTITIONING, partitioner);
 	}
 	
 	/**
 	 * Removes partitioner with <code>document</code>.
 	 * 
 	 * @param document
 	 *            the document
 	 */
 	private static void removeStuff(Document document) {
 		document.setDocumentPartitioner(IRubyPartitions.RUBY_PARTITIONING, null);
 	}
 	
 	protected String getHeaderComment(IMember member) {
 		if (member instanceof IField) {
 			return null;
 		}
 		try {
 			ISourceRange range = member.getSourceRange();
 			if (range == null)
 				return null;
 
 			IBuffer buf = null;
 
 			ISourceModule compilationUnit = member.getSourceModule();
 			if (!compilationUnit.isConsistent()) {
 				return null;
 			}
 
 			buf = compilationUnit.getBuffer();
 
 			int start = range.getOffset();
 			int end = start;
 
 			String contents = buf.getContents();
 
 			String result = "";
 
 			Document doc = new Document(contents);
 			installStuff(doc);
 
 			int pos = 0;
 			
 			if (start > 0) {
 				try {
 					int line = doc.getLineOfOffset(start);
 					if (line == 0)
 						return null;
 					IRegion inf = doc.getLineInformation(line - 1);
 					pos = inf.getOffset() + inf.getLength() - 1;
 				} catch (BadLocationException e) {
 					return null;
 				}
 			}
 
 			try {
 				while (pos >= 0 && pos <= doc.getLength()) {
 					ITypedRegion region = TextUtilities.getPartition(doc,
 							IRubyPartitions.RUBY_PARTITIONING, pos, true);
 					if (region.getType().equals(IRubyPartitions.RUBY_DOC)
 							|| region.getType().equals(
 									IRubyPartitions.RUBY_COMMENT)) {
 						start = region.getOffset();
 					}
 					if (region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
 						String content = doc.get(region.getOffset(),
 								region.getLength()).trim();
 						if (content.length() > 0)
 							break;
 					}
 					pos = region.getOffset() - 1;
 				}
 				
 				pos = start + 1;
 				
 				while (pos <= doc.getLength()) {
 					ITypedRegion region = TextUtilities.getPartition(doc,
 							IRubyPartitions.RUBY_PARTITIONING, pos, true);
 					if (region.getType().equals(IRubyPartitions.RUBY_DOC)
 							|| region.getType().equals(
 									IRubyPartitions.RUBY_COMMENT)) {
 						end = region.getOffset() + region.getLength();
 					}
 					if (region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
 						String content = doc.get(region.getOffset(),
 								region.getLength()).trim();
 						if (content.length() > 0)
 							break;
 					}
 					pos = region.getOffset() + region.getLength() + 1;
 				}
 				
 				if (end >= doc.getLength())
 					end = doc.getLength() - 1;
 
 				result = doc.get(start, end - start);
 
 			} catch (BadLocationException e1) {
 				return null;
 			} finally {
 				removeStuff(doc);
 			}						
 
 			return result;
 
 		} catch (ModelException e) {
 		}
 		return null;
 	}
 	
 	private Reader proccessBuiltin (FakeMethod method) {
 		String divider;
 		if (0 != (method.getFlags() & Modifiers.AccStatic))
 			divider = "::";
 		else
 			divider = "#";
 		String keyword = method.getReceiver() + divider + method.getElementName();
 		RiHelper helper = RiHelper.getInstance();
 		String doc = helper.getDocFor(keyword);
		if (doc.contains("Nothing known about") || doc.trim().length() == 0) {
 			// XXX megafix: some Kernel methods are documented in Object
 			if (method.getReceiver().equals("Kernel")) {
 				keyword = "Object" + divider + method.getElementName();
 				doc = helper.getDocFor(keyword);
 			}
 		}
 		if (doc != null)
 			return new StringReader(doc);
 		return new StringReader("Built-in method");
 	}
 	
 	public Reader getInfo(IMember member, boolean lookIntoParents, boolean lookIntoExternal)  {
 		if (member instanceof FakeMethod) {
 			FakeMethod method = (FakeMethod) member;
 			return proccessBuiltin(method);
 		}
 		String header = getHeaderComment(member);
 		return new StringReader (convertToHTML (header));
 	}
 	
 	private static String replaceSpecTag(String original, String sc, String tag) {
 		String filtered = original;
 		if (sc.equals("*") || sc.equals("+"))
 			sc= "\\" + sc;
 		Pattern bold = Pattern.compile(sc + "[_a-zA-Z0-9]+" + sc);
 		while (true) {
 			Matcher matcher = bold.matcher(filtered);
 			if (matcher.find()) {
 				String startStr = filtered.substring(0, matcher.start());
 				String endStr = filtered.substring(matcher.end());
 				String grp = matcher.group();
 				filtered = startStr + "<" + tag + ">"+ grp.substring(1, grp.length() - 1) + "</" + tag + ">" + endStr;				
 			} else
 				break;
 		}		
 		
 		return filtered;
 	}
 	
 	protected String convertToHTML (String header) {
 		if (header == null)
 			return "";
 		StringBuffer result = new StringBuffer ();
 		Document d = new Document (header);
 		boolean enabled = true;
 		for (int line = 0; ;line++) {
 			try {
 				String str = getLine (d, line).trim();
 				if (str == null)
 					break;
 				if (str.startsWith("#--")) {
 					enabled = false;
 				} else if (str.startsWith("#++")) {
 					enabled = true;
 				}
 				if (!enabled)
 					continue;
 				
 				if (str.startsWith("=begin"))
 					continue;
 				
 				if (str.startsWith("=end"))
 					continue;
 				
 				while (str.length() > 0 && str.startsWith("#"))
 					str = str.substring(1);
 				
 				str = replaceSpecTag(str, "*", "b");
 				str = replaceSpecTag(str, "+", "tt");
 				str = replaceSpecTag(str, "_", "em");
 				
 				str.replaceAll("\\*[_a-zA-Z0-9]+\\*", "");
 				
 				
 				if (str.length() == 0)				
 					result.append("<p>");
 				else { 
 					if (str.trim().startsWith("== ")) {
 						result.append ("<h2>");
 						result.append (str.substring(3));
 						result.append ("</h2>");
 					} else
 					if (str.trim().startsWith("= ")) {
 						result.append ("<h1>");
 						result.append (str.substring(2));
 						result.append ("</h1>");
 					} else  if (str.trim().startsWith("---")) {
 						result.append ("<hr>");
 					} else {
 						result.append (str + "<br>");
 					}
 				}
 			} catch (BadLocationException e) {
 				break;
 			}
 			
 		}
 		//result.append("</p>\n");
 		return result.toString();
 	}
 	
 
 	public Reader getInfo(String content) {
 		return null;
 	}
 }
 
