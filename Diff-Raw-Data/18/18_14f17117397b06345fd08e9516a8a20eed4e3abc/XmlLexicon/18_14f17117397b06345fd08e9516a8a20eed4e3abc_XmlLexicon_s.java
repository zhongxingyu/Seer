 package xmllexicon;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import pregroup.FreeType;
 import pregroup.Lexicon;
 import pregroup.TypeString;
 
 
 public class XmlLexicon
 	extends HashMap<String, List<TypeString<FreeType>>>
 	implements Lexicon<FreeType>
 {
 	private static final long serialVersionUID = 1L;
 
 	public List<TypeString<FreeType>> types(String word)
 	{
 		List<TypeString<FreeType>> l = get(word);
 		if(l == null)
 			return new ArrayList<TypeString<FreeType>>();
 		return l;
 	}
 	
 	public void load(String filename)
 	{
 		try {
 		JAXBContext jaxbContext;
 			jaxbContext = JAXBContext
 					.newInstance("xmllexicon");
 		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 		JAXBElement<?> lexicon = (JAXBElement<?>)unmarshaller
 				.unmarshal(new FileInputStream(filename));
 		
 		RawLexicon rl = (RawLexicon)lexicon.getValue();
 		List<EntryType> entries = rl.getEntry();
 		
 		for(EntryType ent : entries) {
 			String form = ent.getForm();
 			List<String> rawTypes = ent.getType();
 			List<TypeString<FreeType>> res = new ArrayList<TypeString<FreeType>>();
 			
 			for(String rt : rawTypes)
 			{
 				TypeStringLexer lex = new TypeStringLexer(new StringReader(rt));
 				TypeString<FreeType> ft = new TypeString<FreeType>();
 				
 				//! Simple parsing algorithm
 				Symbol sym = lex.yylex();
 				try {
 					while(sym.type != TypeStringLexer.EOF)
 					{
 						String bt = (String)sym.val;
 						sym = lex.yylex();
 						
 						int exp = 0;
 						if(sym.type == TypeStringLexer.LBRA)
 						{
 							sym = lex.yylex();
 							exp = (Integer)sym.val;
 							sym = lex.yylex();
 							sym = lex.yylex();
 						}
 						ft.add(new FreeType(bt), exp);
 					}
 				} catch(IOException ioe)
 				{ ; }
 				
 				res.add(ft);
 			}
 			
 			put(form, res);
 		}
 		
 		} catch (JAXBException je) {
 			je.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		
 	}
 }
