 package org.daisy.saxon.functions.file;
 
 import java.io.File;
 
 import net.sf.saxon.expr.XPathContext;
 import net.sf.saxon.lib.ExtensionFunctionCall;
 import net.sf.saxon.lib.ExtensionFunctionDefinition;
 import net.sf.saxon.om.SequenceIterator;
 import net.sf.saxon.om.StructuredQName;
 import net.sf.saxon.trans.XPathException;
 import net.sf.saxon.tree.iter.SingletonIterator;
 import net.sf.saxon.type.BuiltInAtomicType;
 import net.sf.saxon.value.BooleanValue;
 import net.sf.saxon.value.SequenceType;
 import net.sf.saxon.value.StringValue;
 
 @SuppressWarnings("serial")
 public class FileExists extends ExtensionFunctionDefinition {
 
 	private static final StructuredQName funcname = new StructuredQName("pf",
 			"http://www.daisy.org/ns/pipeline/functions", "file-exists");
 
 	@Override
 	public SequenceType[] getArgumentTypes() {
 		return new SequenceType[] { SequenceType.SINGLE_STRING };
 	}
 
 	@Override
 	public StructuredQName getFunctionQName() {
 		return funcname;
 	}
 
 	@Override
 	public SequenceType getResultType(SequenceType[] arg0) {
 		return SequenceType.SINGLE_BOOLEAN;
 	}
 
 	@Override
 	public ExtensionFunctionCall makeCallExpression() {
 		return new ExtensionFunctionCall() {
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			public SequenceIterator call(SequenceIterator[] arguments,
 					XPathContext context) throws XPathException {
 
 				try {
 					String path = ((StringValue) arguments[0].next())
 							.getStringValue();
					boolean result = new File(path).exists();
 					return SingletonIterator.makeIterator(new BooleanValue(
 							result, BuiltInAtomicType.BOOLEAN));
 				} catch (Exception e) {
 					throw new XPathException("pf:file-exists failed", e);
 				}
 			}
 		};
 	}
 
 }
