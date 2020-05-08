 package newsrack.database.sql;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import newsrack.filter.Concept;
 import newsrack.filter.ConceptToken;
 import newsrack.filter.NR_ConceptCollection;
 
 public class SQL_ConceptStub extends Concept
 {
 	public SQL_ConceptStub(Long key, String name, String defnString, String token)
 	{
 		super(name, defnString);
 		setKey(key);
 		if ((token != null) && !token.equals(""))
 			setLexerToken(new ConceptToken(token));
 	}
 
 	public NR_ConceptCollection getCollection()
 	{
 		NR_ConceptCollection c = super.getCollection();
 		if (c == null) {
			c = (NR_ConceptCollection)SQL_Stmt.GET_COLLECTION_FOR_CONCEPT.execute(new Object[]{c.getKey()});
 			super.setCollection(c);
 		}
 		return c;
 	}
 
 	public int hashCode()
 	{
		super.getCollection();
 		return super.hashCode();
 	}
 
 	public Iterator<String> getKeywords() {
 		try {
 			return super.getKeywords();
 		}
 		catch (NullPointerException e) {
 			String keywords = (String)SQL_StmtExecutor.execute(
										"SELECT keywords FROM concept_table WHERE cpt_key = ?",
 										SQL_StmtType.QUERY,
 										new Object[]{getKey()},
 										new SQL_ValType[]{SQL_ValType.LONG},
 										null,
 										new GetStringResultProcessor(),
 										true);
 			super.setKeywords((java.util.List<String>)Arrays.asList(keywords.split("\n")));
 			return super.getKeywords();
 		}
 	}
 }
