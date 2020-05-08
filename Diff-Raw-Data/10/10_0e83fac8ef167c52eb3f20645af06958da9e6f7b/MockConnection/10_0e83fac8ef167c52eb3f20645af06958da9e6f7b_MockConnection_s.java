 package org.arachb.owlbuilder.lib;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class MockConnection implements AbstractConnection {
 	
 	private static MockResults mockPublicationResults = new MockResults();
 	static {
 		mockPublicationResults.setInteger("id", 1);
 		mockPublicationResults.setString("publication_type","Journal");
 		mockPublicationResults.setString("dispensation","");
 		mockPublicationResults.setString("downloaded","");
 		mockPublicationResults.setString("reviewed","");
 		mockPublicationResults.setString("title","");
 		mockPublicationResults.setString("alternate_title","");
 		mockPublicationResults.setString("author_list","");
 		mockPublicationResults.setString("editor_list","");
 		mockPublicationResults.setString("source_publication","");
 		mockPublicationResults.setInteger("volume",1);
 		mockPublicationResults.setString("issue","");
 		mockPublicationResults.setString("serial_identifier","");
 		mockPublicationResults.setString("page_range","");
 		mockPublicationResults.setString("publication_date","");
 		mockPublicationResults.setString("publication_year","");
 		mockPublicationResults.setString("doi","");
 		mockPublicationResults.setString("generated_id","");
         mockPublicationResults.setString("curation_status","");
         mockPublicationResults.setString("curation_update","");
 
 	}
 	private static MockResults mockTermResults = new MockResults();
 	static {
 		mockTermResults.setInteger("id",1);
 		mockTermResults.setString("source_id","http://purl.obolibrary.org/obo/NCBITaxon_336608");
 		mockTermResults.setInteger("domain",3);
 		mockTermResults.setInteger("authority",1);
 		mockTermResults.setString("label","Tetragnatha straminea");
 		mockTermResults.setString("generated_id","");
 		mockTermResults.setString("comment","");
 	}
 
 	private static MockResults mockTermResults2 = new MockResults();
 	static {
 		mockTermResults2.setInteger("id",2);
 		mockTermResults2.setString("source_id","http://purl.obolibrary.org/obo/SPD_0000001");
 		mockTermResults2.setInteger("domain",2);
 		mockTermResults2.setInteger("authority",1);
 		mockTermResults2.setString("label","whole organism");
 		mockTermResults2.setString("generated_id","");
 		mockTermResults2.setString("comment","");
 	}
 
 	
 	private static MockResults mockPrimaryParticipantResults = new MockResults();
 	static {
 		mockPrimaryParticipantResults.setInteger("id",1);
 		mockPrimaryParticipantResults.setInteger("taxon",2);
 		mockPrimaryParticipantResults.setInteger("substrate",3);
 		mockPrimaryParticipantResults.setInteger("anatomy",4);
 		mockPrimaryParticipantResults.setString("quantification","some");
 		mockPrimaryParticipantResults.setString("generated_id","");
 		mockPrimaryParticipantResults.setString("label","");
 		mockPrimaryParticipantResults.setString("publication_taxon","");
 		mockPrimaryParticipantResults.setString("publication_anatomy","");
 		mockPrimaryParticipantResults.setString("publication_substrate","");
 	}
 
 	private static MockResults mockSecondaryParticipantResults = new MockResults();
 	static {
 		mockSecondaryParticipantResults.setInteger("id",2);
 		mockSecondaryParticipantResults.setInteger("taxon",4);
 		mockSecondaryParticipantResults.setInteger("substrate",6);
 		mockSecondaryParticipantResults.setInteger("anatomy",8);
 		mockSecondaryParticipantResults.setString("quantification","some");
 		mockSecondaryParticipantResults.setString("generated_id","");
 		mockSecondaryParticipantResults.setString("label","");
 		mockSecondaryParticipantResults.setString("publication_taxon","");
 		mockSecondaryParticipantResults.setString("publication_anatomy","");
 		mockSecondaryParticipantResults.setString("publication_substrate","");
 	}
 	
 	private static MockResults mockAssertionResults = new MockResults();
 	static{
 		mockAssertionResults.setInteger("id", 1);
 		mockAssertionResults.setInteger("publication",6);
 		mockAssertionResults.setInteger("behavior_term",9);
 		mockAssertionResults.setString("publication_behavior","behavior");
 		mockAssertionResults.setInteger("taxon", 12);
 		mockAssertionResults.setString("publication_taxon","Arachida");
 		mockAssertionResults.setString("publication_anatomy","whole body");
 		mockAssertionResults.setInteger("evidence",15);
 		mockAssertionResults.setString("generated_id","");
 
 	}
 	
 	private static Map<String,String> mockImportSourceMap = new HashMap<String,String>();
 	static{
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/eco.owl", "evidence");
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/ncbitaxon.owl", "taxonomy");
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/spd.owl", "anatomy");
 		mockImportSourceMap.put("http://behavior-ontology.googlecode.com/svn/trunk/behavior.owl", "behavior");
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/go.owl", "gene products");
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/pato.owl", "qualities");
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/chebi.owl", "chemistry");
 		mockImportSourceMap.put("http://purl.obolibrary.org/obo/uberon.owl", "bilateralian anatomy");
 	}
 
 	private static Map<String,String> mockOntologyNamesMap = new HashMap<String,String>();
 	static{
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/eco.owl", "Evidence Codes (ECO)");
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/ncbitaxon.owl", "NCBI Taxonomy");
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/spd.owl", "Spider Anatomy");
 		mockOntologyNamesMap.put("http://behavior-ontology.googlecode.com/svn/trunk/behavior.owl", 
 				                 "NeuroBehavior Ontology");
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/go.owl", "Gene Ontology");
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/pato.owl", "Phenotype and Trait Ontology");
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/chebi.owl", "CHEBI");
 		mockOntologyNamesMap.put("http://purl.obolibrary.org/obo/uberon.owl", "Uberon");
 	}
 
 	public MockConnection() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void close() throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Publication getPublication(int get_publication) throws SQLException {
 		Publication result = new Publication();
         result.fill(mockPublicationResults);
 		return result;
 	}
 
 	@Override
 	public Set<Publication> getPublications() throws SQLException {
 		Set<Publication> results = new HashSet<Publication>();
 		Publication p1 = new Publication();
 		p1.fill(mockPublicationResults);
 		results.add(p1);
 		return results;
 	}
 
 	@Override
 	public void updatePublication(Publication pub) throws SQLException {
 		mockPublicationResults.setString("generated_id", pub.get_generated_id());
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Term getTerm(int i) throws SQLException {
 		Term result = new Term();
         result.fill(mockTermResults);
 		return result;
 	}
 
 	@Override
 	public Set<Term> getTerms() throws SQLException {
 		Set<Term> results = new HashSet<Term>();
 		Term t1 = new Term();
 		t1.fill(mockTermResults);
 		results.add(t1);
 		return results;
 		
 	}
 
 	@Override
 	public void updateTerm(Term testTerm) throws SQLException {
 		mockTermResults.setString("generated_id", testTerm.get_generated_id());
 	}
 
 	
 	@Override
 	public Participant getPrimaryParticipant(Assertion a) throws SQLException {
 		Participant result = new Participant();
         result.fill(mockPrimaryParticipantResults);
 		return result;
 	}
 
 	@Override
 	public Set<Participant> getParticipants(Assertion a) throws SQLException {
 		Set<Participant> results = new HashSet<Participant>();
 		Participant p1 = new Participant();
 		p1.fill(mockSecondaryParticipantResults);
 		results.add(p1);
 		return results;
 	}
 
 	@Override
 	public void updateParticipant(Participant p) throws SQLException{
 		mockPrimaryParticipantResults.setString("generated_id", p.get_generated_id());
 	}
 
 	@Override
 	public Assertion getAssertion(int i) throws SQLException {
 		Assertion result = new Assertion();
         result.fill(mockAssertionResults);
 		return result;
 	}
 
 
 	@Override
 	public Set<Assertion> getAssertions() throws SQLException {
 		Set<Assertion> results = new HashSet<Assertion>();
 		Assertion a1 = new Assertion();
 		a1.fill(mockAssertionResults);
 		results.add(a1);
 		return results;
 	}
 
 	@Override
 	public void updateAssertion(Assertion a) throws SQLException {
 		mockAssertionResults.setString("generated_id", a.get_generated_id());		
 	}
 
 	@Override
 	public Map<String, String> loadImportSourceMap() throws Exception {
 		return mockImportSourceMap;
 	}
 
 	@Override
 	public Map<String, String> loadOntologyNamesForLoading() throws SQLException {
 		return mockOntologyNamesMap;
 	}
 
 
 }
