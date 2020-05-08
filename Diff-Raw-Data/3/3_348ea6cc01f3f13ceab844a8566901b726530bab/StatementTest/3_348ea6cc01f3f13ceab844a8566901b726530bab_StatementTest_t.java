 package test.wikibase.dataModel.claim;
 
 import org.junit.Test;
 import wikibase.dataModel.claim.Rank;
import wikibase.dataModel.reference.ReferenceList;
 import wikibase.dataModel.claim.Statement;
 import wikibase.dataModel.entity.PropertyId;
 import wikibase.dataModel.snak.PropertyNoValueSnak;
 import wikibase.dataModel.snak.Snak;
 import wikibase.dataModel.snak.SnakList;
 
 import static org.junit.Assert.assertEquals;
 
 public class StatementTest {
     @Test
     public void assertCanConstructWithAllArguments() throws Exception {
         PropertyNoValueSnak mainSnak = new PropertyNoValueSnak(new PropertyId("p42"));
         SnakList qualifiers = new SnakList();
         ReferenceList references = new ReferenceList();
         Rank rank = Rank.DEPRECATED;
 
         assertConstructorArgumentsAreSet( mainSnak, qualifiers, references, rank );
     }
 
     private void assertConstructorArgumentsAreSet( Snak mainSnak, SnakList qualifiers, ReferenceList references, Rank rank ) {
         Statement claim = new Statement(mainSnak, qualifiers, references, rank);
 
         assertEquals(claim.getMainSnak(), mainSnak);
         assertEquals(claim.getQualifiers(), qualifiers);
         assertEquals(claim.getReferences(), references);
         assertEquals(claim.getRank(), rank);
     }
 }
