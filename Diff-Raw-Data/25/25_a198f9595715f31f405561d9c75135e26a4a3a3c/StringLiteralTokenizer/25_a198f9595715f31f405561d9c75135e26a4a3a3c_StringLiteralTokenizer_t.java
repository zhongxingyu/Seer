 package au.com.miskinhill.search.tokenizer;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.StopAnalyzer;
 import org.apache.lucene.analysis.Token;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 
 import com.hp.hpl.jena.rdf.model.Literal;
 
 public class StringLiteralTokenizer extends RDFLiteralTokenizer {
 	
 	private static Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
 	static {
 		analyzers.put(null, new StandardAnalyzer(new String[0] /* no stop words */));
 		analyzers.put("en", new SnowballAnalyzer("English", 
 				StopAnalyzer.ENGLISH_STOP_WORDS));
 	}
 	
 	private TokenStream delegate;
 
 	public StringLiteralTokenizer(Literal node) {
		String lang = node.getLanguage();
		if (lang.length() > 2)
			lang = lang.substring(0, 2);
		Analyzer analyzer = analyzers.get(lang);
 		if (analyzer == null)
 			analyzer = analyzers.get(null); // use default
 		delegate = analyzer.tokenStream(null, new StringReader(node.getString()));
 	}
 	
 	public Token next(Token reuseableToken) throws IOException {
 		return delegate.next(reuseableToken);
 	}
 	
 }
