 package xmllexicon;
 
 import graphexpr.GraphExpr;
 import graphexpr.GraphExprLexer;
 import graphexpr.GraphExprParser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.lang3.tuple.Pair;
 
 import pregroup.Lexicon;
 import pregroup.PartialComparator;
 import pregroup.TypeString;
 import util.InternalException;
 import util.UnknownTagException;
 
 
 public class SemanticLexicon
 	implements Lexicon
 {
 	private HashMap<String, List<List<GraphExpr>>> mFromTag =
 			new HashMap<String, List<List<GraphExpr>>>();
 	private HashMap<String, List<List<GraphExpr>>> mFromForm =
 			new HashMap<String, List<List<GraphExpr>>>();
 
 	private TypeRelations rels;
 
 	private static TypeString stringOfList(List<GraphExpr> l)
 	{
 		TypeString res = new TypeString();
 		for(GraphExpr g : l)
 			res.add(g.getType());
 		return res;
 	}
 	
 	public List<List<GraphExpr>> get(Pair<String,String> taggedWord)
 	{
 		List<List<GraphExpr>> res = mFromForm.get(taggedWord.getLeft());
 		if(res == null)
 			return mFromTag.get(taggedWord.getRight());
 		return res;
 	}
 	
 	public List<List<List<GraphExpr>>> graphExprs(List<Pair<String,String>> sentence) throws UnknownTagException
 	{
 		List<List<List<GraphExpr>>> res = new ArrayList<List<List<GraphExpr>>>();
 		
 		for(Pair<String,String> taggedWord : sentence)
 		{
 			List<List<GraphExpr>> l = get(taggedWord);
 
 			if(l != null && !l.isEmpty())
 			{
 				List<List<GraphExpr>> cloned = new ArrayList<List<GraphExpr>>();
 				for(List<GraphExpr> l1 :l)
 				{
 					List<GraphExpr> l2 = new ArrayList<GraphExpr>();
 					for(GraphExpr ge : l1)
 						l2.add((GraphExpr)ge.clone());
 					cloned.add(l2);
 				}
 				res.add(cloned);
 			}
 			else
 				throw new UnknownTagException(taggedWord.getRight());
 		}
 		
 		return res;
 	}
 	
 	public List<List<TypeString>> types(List<Pair<String,String>> sentence)
 	{
 		List<List<TypeString>> res = new ArrayList<List<TypeString>>();
 		for(Pair<String,String> taggedWord : sentence)
 		{
 			List<List<GraphExpr>> o = get(taggedWord);
 			List<TypeString> l = new ArrayList<TypeString>();
 			if(o == null)
 				res.add(new ArrayList<TypeString>());
 			else
 			{
 				for(List<GraphExpr> g : o)
 					l.add(stringOfList(g));
 				res.add(l);
 			}
 		}
 		return res;
 	}
 	
 	public void load(String filename) throws InternalException
 	{
 		try {
 		JAXBContext jaxbContext;
 			jaxbContext = JAXBContext
 					.newInstance("xmllexicon");
 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 		JAXBElement<?> lexicon = (JAXBElement<?>)unmarshaller
 				.unmarshal(new FileInputStream(filename));
 		
 		RawLexicon rl = (RawLexicon)lexicon.getValue();
 		Entries entries = rl.getEntries();
 		rels = rl.getRelations();
 		if(rels == null)
 			rels = new TypeRelations();
 		
 		if(entries.getEntry() != null)
 		{
 			for(EntryType ent : entries.getEntry()) {
 				String form = ent.getForm();
 				String tag = ent.getTag();
 				List<String> rawTypes = ent.getType();
 				List<List<GraphExpr>> res = new ArrayList<List<GraphExpr>>();
 				
 				for(String rt : rawTypes)
 				{
 					GraphExprLexer lexer = new GraphExprLexer(rt);
 					GraphExprParser parser = new GraphExprParser(lexer);
 					
 					java_cup.runtime.Symbol s;
 					try {
 						s = parser.parse();
 						if(s.value != null)
 							res.add((List<GraphExpr>)(s.value));
 					}
 					catch (Exception e) {
 						throw new InternalException("Unable to parse the following type: "+rt);
 					}
 				}
 				
 				if(form != null && form != "")
 					mFromForm.put(form, res);
 				else if(tag != null)
 					mFromTag.put(tag, res);
 				else
 					System.err.println("Undefined tag or form.");
 			}
 		}
 		else System.err.println("Warning, the lexicon loaded has no <entries> section.");
 		
 		} catch (JAXBException je) {
 			je.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		
 	}
 
 	public PartialComparator<String> getComparator() {
 		return rels;
 	}
 }
