 /**
  * Copyright 2012 Omar Siam
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.sweble.wikitext.saxon;
 
 import static org.sweble.wikitext.saxon.util.*;
 import static de.fau.cs.osr.ptk.common.xml.XmlConstants.PTK;
 import static de.fau.cs.osr.ptk.common.xml.XmlConstants.PTK_NS;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.transform.dom.DOMSource;
 
 import de.fau.cs.osr.ptk.common.xml.SerializationException;
 import de.fau.cs.osr.ptk.common.xml.XmlWriter;
 import de.fau.cs.osr.utils.NameAbbrevService;
 
 import org.sweble.wikitext.engine.CompilerException;
 import org.sweble.wikitext.engine.ExpansionCallback;
 import org.sweble.wikitext.engine.ExpansionDebugHooks;
 import org.sweble.wikitext.engine.ExpansionFrame;
 import org.sweble.wikitext.engine.ExpansionVisitor;
 import org.sweble.wikitext.engine.FullPage;
 import org.sweble.wikitext.engine.PageId;
 import org.sweble.wikitext.engine.PageTitle;
 import org.sweble.wikitext.engine.ParserFunctionBase;
 import org.sweble.wikitext.engine.WtEngine;
 import org.sweble.wikitext.engine.config.WikiConfigImpl;
 import org.sweble.wikitext.engine.lognodes.ResolveParserFunctionLog;
 import org.sweble.wikitext.engine.lognodes.ResolveTagExtensionLog;
 import org.sweble.wikitext.engine.lognodes.ResolveTransclusionLog;
 import org.sweble.wikitext.engine.nodes.EngCompiledPage;
 import org.sweble.wikitext.parser.nodes.WtNode;
 import org.sweble.wikitext.parser.nodes.WtNodeList;
 import org.sweble.wikitext.parser.nodes.WtTagExtension;
 import org.sweble.wikitext.parser.nodes.WtTagExtensionBody;
 import org.sweble.wikitext.parser.nodes.WtTemplate;
 import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
 import org.sweble.wikitext.parser.nodes.WtText;
 
 import net.sf.saxon.dom.NodeOverNodeInfo;
 import net.sf.saxon.event.Builder;
 import net.sf.saxon.event.PipelineConfiguration;
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.lib.ExtensionFunctionCall;
 import net.sf.saxon.om.*;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.EmptyIterator;
 import net.sf.saxon.tree.iter.SingletonIterator;
 import net.sf.saxon.value.StringValue;
 
 public class ExtensionFunctionParseMediaWikiCall extends ExtensionFunctionCall {
 
 	private static final long serialVersionUID = 1621372360808681807L;
 
 	/**
 	 * see also http://old.nabble.com/problem-returning-a-document-fragment-from-saxon-9.3-integrated-extension-function-td32318492.html
 	 */
 
 	protected static Map<String, FullPage> knownPages = Collections.synchronizedMap(new LinkedHashMap<String, FullPage>());
 	protected static boolean reportProblems = false;
 	
 	protected static WikiConfigImpl config = null;
 	protected static DocumentInfo configDoc = null;
 	
 	private TreeModel treeModel = null;
 	private PipelineConfiguration pipe = null;
 	
 	private Properties envProperties = System.getProperties();
 	private Properties enforceSaxon9Propertiess = new Properties(envProperties);
 	
 	private NameAbbrevService as = new NameAbbrevService(PTK, PTK_NS,
 			new String[]{"de.fau.cs.osr.ptk.common.test", "ptk"},
 			new String[]{"de.fau.cs.osr.ptk.common.xml", "ptk"},
 			new String[]{"org.sweble.wikitext.lazy.parser", "swc", "http://sweble.org/doc/site/tooling/sweble/sweble-wikitext"},
 			new String[]{"org.sweble.wikitext.lazy.preprocessor", "swc"},
 			new String[]{"org.sweble.wikitext.lazy.utils", "swc"},
 			new String[]{"org.sweble.wikitext.engine", "swc"},
 			new String[]{"org.sweble.wikitext.engine.nodes", "swc"},
 			new String[]{"org.sweble.wikitext.engine.log", "swc"},
 			new String[]{"org.sweble.wikitext.parser.nodes", "swc"});
 
 	//	@Override
 	//	public void copyLocalData(ExtensionFunctionCall destination) {
 	//		ExtensionFunctionParseMediaWikiCall dest = (ExtensionFunctionParseMediaWikiCall) destination;
 	//	}
 
 	public ExtensionFunctionParseMediaWikiCall() {
 		super();
 	    enforceSaxon9Propertiess.put("javax.xml.transform.TransformerFactory",
 				"net.sf.saxon.TransformerFactoryImpl");
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public SequenceIterator<NodeInfo> call(
 			@SuppressWarnings("rawtypes") SequenceIterator<? extends Item>[] args, XPathContext ctx)
 					throws XPathException {
 		SequenceIterator<NodeInfo> result = null;
 		String data = "";
 		String title = "input";
 		try {
 			
 			DocumentInfo currentConfigDoc = (DocumentInfo) args[2].next();
 			if (configDoc == null)
 				try {
 					configDoc = currentConfigDoc;
 					config = WikiConfigImpl.load(new DOMSource(NodeOverNodeInfo.wrap(configDoc)));
 				} catch (JAXBException e) {
 					return EmptyIterator.getInstance();
 				}
 			else 
 				synchronized (configDoc) {
 					if (!configDoc.equals(currentConfigDoc))				
 						try {
 							configDoc = currentConfigDoc;
 							config = WikiConfigImpl.load(new DOMSource(NodeOverNodeInfo.wrap(configDoc)));
 						} catch (JAXBException e) {
 							return EmptyIterator.getInstance();
 						}	
 				}
 			
 			StringValue in = (StringValue) args[0].next();
 			if(null == in) {
 				return EmptyIterator.getInstance();
 			}
 			
 			title = in.getStringValue();
 
 			in = (StringValue) args[1].next();
 			if(null == in) {
 				return EmptyIterator.getInstance();
 			}
 			
 			data = in.getStringValue();
 			
 			WtEngine wtEngine = new WtEngine(config);
 			if (reportProblems) {
 				wtEngine.setDebugHooks(new ExpansionDebugHooks() {
 					
 				@Override
 				public WtNode afterResolveParserFunction(
 						ExpansionVisitor expansionVisitor, WtTemplate n,
 						ParserFunctionBase pfn,
 						List<? extends WtNode> argsValues, WtNode result,
 						ResolveParserFunctionLog log) {
 					WtNode newResult = super.afterResolveParserFunction(expansionVisitor, n, pfn, argsValues,
 							result, log);
 					if (newResult == null)
 						System.err.println("Can't resolve " + " as a parser function.");
 					return newResult;
 				}
 				
 				@Override
 					public WtNode afterResolveTagExtension(
 							ExpansionVisitor expansionVisitor,
 							WtTagExtension n, String name,
 							WtNodeList attributes,
 							WtTagExtensionBody wtTagExtensionBody,
 							WtNode result, ResolveTagExtensionLog log) {
 					WtNode newResult = super.afterResolveTagExtension(expansionVisitor, n, name, attributes, wtTagExtensionBody, result, log);
 					if (newResult == null)
 						System.err.println("Can't resolve " + name + " as tag extension.");
 					return newResult;
 					}
 				
 				@Override
 					public WtNode afterResolveTransclusion(
 							ExpansionVisitor expansionVisitor, WtTemplate n,
 							String target, List<WtTemplateArgument> args,
 							WtNode result, ResolveTransclusionLog log) {
 					WtNode newResult = super.afterResolveTransclusion(expansionVisitor, n, target, args, result, log);
 					if (newResult == null)
 						System.err.println("Can't resolve " + target + " as a transclusion.");
 					return newResult;
 					}
 				});
 			}
 			EngCompiledPage p = wtEngine.postprocess(new PageId(PageTitle.make(config, title), 0), data, new ExpansionCallback() {
 
 				@Override
 				public FullPage retrieveWikitext(ExpansionFrame arg0, PageTitle arg1)
 						throws Exception {
 					FullPage ret = knownPages.get(arg1.getTitle()); 
 					return ret;
 				}
 
 				@Override
 				public String fileUrl(PageTitle pageTitle, int width,
 						int height) throws Exception {
 					// TODO: Used for existence checks. Pretend that it does for now.
 					if (pageTitle.getTitle().contains(".")) 
 						return "file://" + pageTitle.getTitle();
 					return null;
 				}
 			});
 			
 //			Iterator<Warning> iter = p.getWarnings().iterator();
 //			while (iter.hasNext())
 //			{
 //				Warning w = iter.next();
 //				System.err.println(w.toString());
 //			}
 
 			DocumentInfo doc = null;
 			XmlWriter<WtNode> ptkToXmlWriter = new XmlWriter<WtNode>(WtNode.class, WtNodeList.WtNodeListImpl.class, WtText.class);
 			net.sf.saxon.Configuration saxonConf = ctx.getConfiguration();
 			if (treeModel == null) // that doesn't change afaik.
 			{
 				treeModel = saxonConf.getParseOptions().getModel();
 				pipe = saxonConf.makePipelineConfiguration();
 			}
 			// The following only works if a TransfromerFactory is used which is aware of the existence of
 			// the Saxon 9 implementations, e. g. the Saxon 9 factory net.sf.saxon.TransformerFactoryImpl.
 			// One factory that has problems with this is the Saxon 6 factory which is used by <oXygen/>
 			
 			System.setProperties(enforceSaxon9Propertiess);
 			Builder builder = treeModel.makeBuilder(pipe);
 			builder.setTiming(false);
 			builder.setLineNumbering(false);
 			builder.setPipelineConfiguration(pipe);
 			try
 			{
 				ptkToXmlWriter.serialize(p, builder, as);
 			}
 			finally
 			{
 				System.setProperties(envProperties);
 			}
 
 			doc = (DocumentInfo) builder.getCurrentRoot();
 
 			result = SingletonIterator.makeIterator((NodeInfo)doc);
 		} catch (CompilerException e) {
			writeWikiTextOnException(e.getPageTitle().getTitle(), e, e.getWikiText() == "" ? data : e.getWikiText());
 		} catch (SerializationException e) {
 			writeWikiTextOnException(title, e, data);
 		} catch (Exception e) {
 			writeWikiTextOnException("", e, data);
 		}
 
 		return result; 
 	}
 
 	private void writeWikiTextOnException(String title, Exception e, String wikiText) {
 		String tempFileName = "Temp file not set!";		
 
 		// Create temp file.
 		File temp;
 		try {
 			temp = File.createTempFile(title == "" ? "wikitext" : title + "_", ".wikitext");
 
 			// Delete temp file when program exits.
 			// temp.deleteOnExit();
 
 			// Write to temp file
 			BufferedWriter tempOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp), Charset.forName("UTF-8")));
 			tempOut.write(wikiText);
 			tempOut.close();
 			tempFileName = temp.getAbsolutePath();
 		} catch (IOException e1) {
 			// That's really bad... How did that happen ???
 			e1.printStackTrace();
 		}
 
 		throw new RuntimeException("See: " + tempFileName + ". Error while parsing. " + getStackTraceAsText(e));
 	}
 }
