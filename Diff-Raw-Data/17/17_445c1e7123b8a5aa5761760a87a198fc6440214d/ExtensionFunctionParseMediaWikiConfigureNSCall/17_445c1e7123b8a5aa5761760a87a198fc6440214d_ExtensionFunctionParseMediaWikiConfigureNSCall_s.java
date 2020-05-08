 package org.sweble.wikitext.saxon;
 
 import static org.sweble.wikitext.saxon.util.*;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.util.JAXBSource;
 import javax.xml.transform.dom.DOMSource;
 
 import org.sweble.wikitext.engine.config.NamespaceImpl;
 import org.sweble.wikitext.engine.config.WikiConfigImpl;
 import net.sf.saxon.dom.NodeOverNodeInfo;
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.om.*;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.EmptyIterator;
 import net.sf.saxon.tree.iter.SingletonIterator;
 import net.sf.saxon.value.IntegerValue;
 import net.sf.saxon.value.StringValue;
 
 public class ExtensionFunctionParseMediaWikiConfigureNSCall extends ExtensionFunctionParseMediaWikiCall {
 
 
 	private static final long serialVersionUID = -6835617630116534748L;
 
 	private List<String> noAliases = Collections.emptyList();
 	private WikiConfigImpl config = null;
 	private DocumentInfo configDoc = null;
 
 	/**
 	 * see also http://old.nabble.com/problem-returning-a-document-fragment-from-saxon-9.3-integrated-extension-function-td32318492.html
 	 */
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public SequenceIterator<NodeInfo> call(
 			@SuppressWarnings("rawtypes") SequenceIterator<? extends Item>[] args, XPathContext ctx)
 					throws XPathException {
 		int keyId;
 		String prefix = "";
 		
 		DocumentInfo currentConfigDoc = (DocumentInfo) args[2].next();
 		if (configDoc == null || !configDoc.equals(currentConfigDoc))
 			try {
 				configDoc = currentConfigDoc;
 				config = WikiConfigImpl.load(new DOMSource(NodeOverNodeInfo.wrap(configDoc)));
 			} catch (JAXBException e) {
 				return EmptyIterator.getInstance();
 			}
 		
 		IntegerValue inInt = (IntegerValue) args[0].next();
 		StringValue in = (StringValue) args[1].next();
		
		while (args[0].next() != null)
 		{
			
			if(null == inInt) {
				return EmptyIterator.getInstance();
			}

 			if (null == in) {
 				prefix = "";
 			} else {
 				prefix = in.getStringValue().replaceAll(" ", "_");
 			}
 
 			keyId = inInt.getDecimalValue().intValue();
 
 			NamespaceImpl na;
 			switch (keyId)
 			{
 			case 10:
 				na = new NamespaceImpl(keyId, prefix, prefix, false, false, Arrays.asList("Template"));
 				break;
 			case 6:
 				na = new NamespaceImpl(keyId, prefix, prefix, false, true, Arrays.asList("File", "Image"));
 				break;
 			case 14:
 				na = new NamespaceImpl(keyId, prefix, prefix, false, false, Arrays.asList("Category"));
 				break;
 			case 4:
 				na = new NamespaceImpl(keyId, prefix, prefix, false, false, Arrays.asList("Wikipedia", "WP"));
 				break;
 			default:
 				na = new NamespaceImpl(keyId, prefix, prefix, false, false, noAliases);
 			}				
 			config.addNamespace(na);
 			switch (keyId)
 			{
 			case 0:
 				config.setDefaultNamespace(na);
 				break;
 			case 10: 
 				config.setTemplateNamespace(na);
 				break;
 			}
 			inInt = (IntegerValue) args[0].next();
 			in = (StringValue) args[1].next();
 		}
 		
 		SequenceIterator<NodeInfo> result = EmptyIterator.getInstance(); 
 		try
 		{
 			JAXBSource configSource = config.getAsJAXBSource();
 			DocumentInfo configXML = ctx.getConfiguration().buildDocument(configSource);
 			result = SingletonIterator.makeIterator((NodeInfo)configXML);
 		} 
 		catch (Exception e) // this should never fail ...
 		{
 			throw new RuntimeException(getStackTraceAsText(e), e);
 		}
 
 		return result; 
 	}
 
 }
