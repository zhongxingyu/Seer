 package uk.ac.ebi.chemet.service.query.taxonmy;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.util.Version;
 import uk.ac.ebi.chemet.service.index.other.TaxonomyIndex;
 import uk.ac.ebi.chemet.service.query.AbstractQueryService;
 import uk.ac.ebi.resource.organism.Kingdom;
 import uk.ac.ebi.resource.organism.Taxonomy;
 import uk.ac.ebi.service.query.name.NameService;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * TaxonmyQueryService - 23.02.2012 <br/>
  * <p/>
  * Class descriptions.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public class TaxonomyQueryService
         extends AbstractQueryService<Taxonomy>
         implements NameService<Taxonomy> {
 
     private static final Logger LOGGER = Logger.getLogger(TaxonomyQueryService.class);
 
     public static final Term KINGDOM = new Term("Kingdom");
     public static final Term CODE = new Term("Code");
 
     public TaxonomyQueryService() {
         super(new TaxonomyIndex());
     }
 
     @Override
     public Taxonomy getIdentifier() {
         return new Taxonomy();
     }
 
     @Override
     public Collection<Taxonomy> searchName(String name, boolean approximate) {
 
 //        Query query = approximate ? new WildcardQuery(NAME.createTerm(name + "*"))
 //                : new TermQuery(NAME.createTerm(name));
         try{
             return setup(getIdentifiers(getParser(NAME).parse(name + "*")));
         }catch (Exception ex){
             ex.printStackTrace();
         }
         return Arrays.asList();
     }
 
     private Collection<Taxonomy> setup(Collection<Taxonomy> identifiers) {
         for (Taxonomy id : identifiers) {
             id.setCode(getCode(id));
             id.setKingdom(Kingdom.getKingdom(getKingdom(id)));
             id.setOfficialName(getNames(id).iterator().next());
         }
         return identifiers;
     }
 
     @Override
     public Collection<String> getNames(Taxonomy identifier) {
         return values(construct(identifier.getAccession(), IDENTIFIER), NAME);
     }
 
     public String getKingdom(Taxonomy identifier) {
         return firstValue(identifier, KINGDOM);
     }
 
     public String getCode(Taxonomy identifier) {
         return firstValue(identifier, CODE);
     }
 
     public Collection<Taxonomy> searchTaxonomyIdentifier(String identifier, boolean approximate) {
         Collection<Taxonomy> identifiers = getIdentifiers(construct(identifier, IDENTIFIER, approximate));
         return setup(identifiers);
     }
 
     public Collection<Taxonomy> searchCode(String code, boolean approximate) {
         Collection<Taxonomy> identifiers = getIdentifiers(construct(code, CODE, approximate));
         return setup(identifiers);
     }
 
     public static void main(String[] args) {
         TaxonomyQueryService service = new TaxonomyQueryService();
         service.setMaxResults(50);
         service.setMinSimilarity(0.5f);
 
 
         long start = System.currentTimeMillis();
         service.setMaxResults(5);
            service.searchCode("lacpl", false);

         long end = System.currentTimeMillis();
         System.out.println("Time: " + (end - start) + "ms");
     }
 
 
 }
