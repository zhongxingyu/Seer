 package ecologylab.translators.metametadata.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope;
 import ecologylab.semantics.metametadata.MetaMetadata;
 import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
 import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
 import ecologylab.semantics.metametadata.MetaMetadataRepository;
 import ecologylab.semantics.metametadata.MetaMetadataScalarField;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.translators.java.JavaTranslationException;
 import ecologylab.translators.metametadata.MetaMetadataJavaTranslator;
 
 public class TestNewMetaMetadataCompiler
 {
 	
 	private static void doTest(String TSName, File testingRepository, File outputDir) throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		System.err.println("\n\n\n\nTest: " + TSName + "\n\n\n\n\n");
 		
 		MetaMetadataRepository repository = MetaMetadataRepository.readRepository(testingRepository);
 		TranslationScope tscope = repository.traverseAndGenerateTranslationScope(TSName);
 		TranslationScope.setGraphSwitch();
 		MetaMetadataJavaTranslator jt = new MetaMetadataJavaTranslator();
 		TranslationScope metadataBuiltInTScope = MetadataBuiltinsTranslationScope.get();
 		for (ClassDescriptor cd : metadataBuiltInTScope.getClassDescriptors())
 			jt.excludeClassFromTranslation(cd);
 		jt.translateToJava(outputDir, tscope);
 	}
 	
 	public static void testGeneratingBasicTScope() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		doTest("basic-tscope", new File("data/testRepository/testGeneratingBasicTScope.xml"), new File("c:/tmp/testbasictscope/"));
 	}
 	
 	public static void testTypeGraphs() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		doTest("type-graphs", new File("data/testRepository/testTypeGraphs.xml"), new File("c:/tmp/testtypegraphs/"));
 	}
 
 	public static void testInlineMmd() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		doTest("inline-mmd", new File("data/testRepository/testInlineMmd.xml"), new File("c:/tmp/testinlinemmd/"));
 	}
 
 	public static void testArticles() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		doTest("articles", new File("data/testRepository/testArticles.xml"), new File("c:/tmp/testarticles/"));
 	}
 	
 	public static void testScalarCollections() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		doTest("scalar-collections", new File("data/testRepository/testScalarCollections.xml"), new File("c:/tmp/testscalarcollections/"));
 	}
 	
 	public static void testPolymorphicFields() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		doTest("poly-fields", new File("data/testRepository/testPolymorphicFields.xml"), new File("c:/tmp/testpolyfields/"));
 	}
 	
	public static void testOtherTags() throws IOException, SIMPLTranslationException, JavaTranslationException
	{
		doTest("other-tags", new File("data/testRepository/testOtherTags.xml"), new File("c:/tmp/testothertags/"));
	}
	
 	/**
 	 * use testArticles.xml as the input repository to validate inheritance relationships (any field:
 	 * declaredMmd, inheritedField, nested field + mmds: inheritedMmd, mmds: inlineMmds).
 	 */
 	@Test
 	public void testArticlesInheritanceRelationships()
 	{
 		MetaMetadataRepository repository = MetaMetadataRepository.readRepository(new File("data/testRepository/testArticles.xml"));
 		TranslationScope tscope  = repository.traverseAndGenerateTranslationScope("test-articles-inheritance");
 		
 		
 		
 		MetaMetadata metadata = repository.getByTagName("metadata");
 		Assert.assertNull(metadata.getInheritedMmd());
 		Assert.assertTrue(metadata.getInlineMmds() == null || metadata.getInlineMmds().isEmpty());
 		// meta_metadata_name
 		MetaMetadataScalarField metadata__meta_metadata_name = (MetaMetadataScalarField) metadata.getChildMetaMetadata().get("meta_metadata_name");
 		Assert.assertNull(metadata__meta_metadata_name.getInheritedField());
 		Assert.assertSame(metadata, metadata__meta_metadata_name.getDeclaringMmd());
 		// mixins
 		MetaMetadataCollectionField metadata__mixins = (MetaMetadataCollectionField) metadata.getChildMetaMetadata().get("mixins");
 		Assert.assertNull(metadata__mixins.getInheritedField());
 		Assert.assertSame(metadata, metadata__mixins.getDeclaringMmd());
 		Assert.assertSame(metadata, metadata__mixins.getInheritedMmd());
 		
 		
 		
 		MetaMetadata document = repository.getByTagName("document");
 		Assert.assertSame(metadata, document.getInheritedMmd());
 		Assert.assertTrue(document.getInlineMmds() == null || document.getInlineMmds().isEmpty());
 		Assert.assertEquals(metadata__meta_metadata_name, document.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, document.getChildMetaMetadata().get("mixins"));
 		// location
 		MetaMetadataScalarField document__location = (MetaMetadataScalarField) document.getChildMetaMetadata().get("location");
 		Assert.assertNull(document__location.getInheritedField());
 		Assert.assertSame(document, document__location.getDeclaringMmd());
 		// additional_locations
 		MetaMetadataCollectionField document__additional_locations = (MetaMetadataCollectionField) document.getChildMetaMetadata().get("additional_locations");
 		Assert.assertNull(document__additional_locations.getInheritedField());
 		Assert.assertSame(document, document__additional_locations.getDeclaringMmd());
 		Assert.assertNull(document__additional_locations.getInheritedMmd());
 		
 		
 		
 		MetaMetadata author = repository.getByTagName("mmd_inline_author_in_authors_in_article");
 		Assert.assertSame(metadata, author.getInheritedMmd());
 		Assert.assertTrue(author.getInlineMmds().size() == 1);
 		Assert.assertSame(author, author.getInlineMmd("author"));
 		Assert.assertEquals(metadata__meta_metadata_name, author.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, author.getChildMetaMetadata().get("mixins"));
 		// name
 		MetaMetadataScalarField author__name = (MetaMetadataScalarField) author.getChildMetaMetadata().get("name");
 		Assert.assertNull(author__name.getInheritedField());
 		Assert.assertSame(author, author__name.getDeclaringMmd());
 		// affiliation
 		MetaMetadataScalarField author__affiliation = (MetaMetadataScalarField) author.getChildMetaMetadata().get("affiliation");
 		Assert.assertNull(author__affiliation.getInheritedField());
 		Assert.assertSame(author, author__affiliation.getDeclaringMmd());
 		
 		
 		
 		MetaMetadata source = repository.getByTagName("mmd_inline_source_in_article");
 		Assert.assertSame(document, source.getInheritedMmd());
 		Assert.assertTrue(source.getInlineMmds().size() == 1);
 		Assert.assertSame(source, source.getInlineMmd("source"));
 		Assert.assertEquals(metadata__meta_metadata_name, source.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, source.getChildMetaMetadata().get("mixins"));
 		Assert.assertEquals(document__additional_locations, source.getChildMetaMetadata().get("additional_locations"));
 		// archive_name
 		MetaMetadataScalarField source__archive_name = (MetaMetadataScalarField) source.getChildMetaMetadata().get("archive_name");
 		Assert.assertNull(source__archive_name.getInheritedField());
 		Assert.assertSame(source, source__archive_name.getDeclaringMmd());
 		// location
 		MetaMetadataScalarField source__location = (MetaMetadataScalarField) source.getChildMetaMetadata().get("location");
 		Assert.assertSame(document__location, source__location.getInheritedField());
 		Assert.assertTrue(!document__location.isHide());
 		Assert.assertTrue(source__location.isHide());
 		// year_of_publication
 		MetaMetadataScalarField source__year_of_publication = (MetaMetadataScalarField) source.getChildMetaMetadata().get("year_of_publication");
 		Assert.assertNull(source__year_of_publication.getInheritedField());
 		Assert.assertSame(source, source__year_of_publication.getDeclaringMmd());
 		// isbn
 		MetaMetadataScalarField source__isbn = (MetaMetadataScalarField) source.getChildMetaMetadata().get("isbn");
 		Assert.assertNull(source__isbn.getInheritedField());
 		Assert.assertSame(source, source__isbn.getDeclaringMmd());
 		
 		
 		
 		MetaMetadata article = repository.getByTagName("article");
 		Assert.assertSame(document, article.getInheritedMmd());
 		Assert.assertTrue(article.getInlineMmds().size() == 2);
 		Assert.assertSame(author, article.getInlineMmd("author"));
 		Assert.assertSame(source, article.getInlineMmd("source"));
 		Assert.assertEquals(metadata__meta_metadata_name, article.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, article.getChildMetaMetadata().get("mixins"));
 		Assert.assertEquals(document__location, article.getChildMetaMetadata().get("location"));
 		Assert.assertEquals(document__additional_locations, article.getChildMetaMetadata().get("additional_locations"));
 		// title
 		MetaMetadataScalarField article__title = (MetaMetadataScalarField) article.getChildMetaMetadata().get("title");
 		Assert.assertNull(article__title.getInheritedField());
 		Assert.assertSame(article, article__title.getDeclaringMmd());
 		// authors
 		MetaMetadataCollectionField article__authors = (MetaMetadataCollectionField) article.getChildMetaMetadata().get("authors");
 		Assert.assertNull(article__authors.getInheritedField());
 		Assert.assertSame(article, article__authors.getDeclaringMmd());
 		Assert.assertSame(author, article__authors.getInheritedMmd());
 		// source
 		MetaMetadataCompositeField article__source = (MetaMetadataCompositeField) article.getChildMetaMetadata().get("source");
 		Assert.assertNull(article__source.getInheritedField());
 		Assert.assertSame(article, article__source.getDeclaringMmd());
 		Assert.assertSame(source, article__source.getInheritedMmd());
 		// pages
 		MetaMetadataScalarField article__pages = (MetaMetadataScalarField) article.getChildMetaMetadata().get("pages");
 		Assert.assertNull(article__pages.getInheritedField());
 		Assert.assertSame(article, article__pages.getDeclaringMmd());
 		
 		
 		
 		MetaMetadata tag = repository.getByTagName("mmd_inline_tag_in_classifications_in_paper");
 		Assert.assertSame(metadata, tag.getInheritedMmd());
 		Assert.assertTrue(tag.getInlineMmds().size() == 1);
 		Assert.assertSame(tag, tag.getInlineMmd("tag"));
 		Assert.assertEquals(metadata__meta_metadata_name, tag.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, tag.getChildMetaMetadata().get("mixins"));
 		// tag_name
 		MetaMetadataScalarField tag__tag_name = (MetaMetadataScalarField) tag.getChildMetaMetadata().get("tag_name");
 		Assert.assertNull(tag__tag_name.getInheritedField());
 		Assert.assertSame(tag, tag__tag_name.getDeclaringMmd());
 		// link
 		MetaMetadataScalarField tag__link = (MetaMetadataScalarField) tag.getChildMetaMetadata().get("link");
 		Assert.assertNull(tag__link.getInheritedField());
 		Assert.assertSame(tag, tag__link.getDeclaringMmd());
 		
 		
 		
 		MetaMetadata paper = repository.getByTagName("paper");
 		Assert.assertSame(article, paper.getInheritedMmd());
 		Assert.assertTrue(paper.getInlineMmds().size() == 1);
 		Assert.assertSame(tag, paper.getInlineMmd("tag"));
 		Assert.assertEquals(metadata__meta_metadata_name, paper.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, paper.getChildMetaMetadata().get("mixins"));
 		Assert.assertEquals(document__location, paper.getChildMetaMetadata().get("location"));
 		Assert.assertEquals(document__additional_locations, paper.getChildMetaMetadata().get("additional_locations"));
 		Assert.assertEquals(article__title, paper.getChildMetaMetadata().get("title"));
 //		Assert.assertSame(article__authors, paper.getChildMetaMetadata().get("authors"));
 		Assert.assertEquals(article__source, paper.getChildMetaMetadata().get("source"));
 		Assert.assertEquals(article__pages, paper.getChildMetaMetadata().get("pages"));
 		// authors: TODO
 		// abstract_field
 		MetaMetadataScalarField paper__abstract_field = (MetaMetadataScalarField) paper.getChildMetaMetadata().get("abstract_field");
 		Assert.assertNull(paper__abstract_field.getInheritedField());
 		Assert.assertSame(paper, paper__abstract_field.getDeclaringMmd());
 		// references
 		MetaMetadataCollectionField paper__references = (MetaMetadataCollectionField) paper.getChildMetaMetadata().get("references");
 		Assert.assertNull(paper__references.getInheritedField());
 		Assert.assertSame(paper, paper__references.getDeclaringMmd());
 		Assert.assertSame(paper, paper__references.getInheritedMmd());
 		// citations
 		MetaMetadataCollectionField paper__citations = (MetaMetadataCollectionField) paper.getChildMetaMetadata().get("citations");
 		Assert.assertNull(paper__citations.getInheritedField());
 		Assert.assertSame(paper, paper__citations.getDeclaringMmd());
 		Assert.assertSame(paper, paper__citations.getInheritedMmd());
 		// classifications
 		MetaMetadataCollectionField paper__classifications = (MetaMetadataCollectionField) paper.getChildMetaMetadata().get("classifications");
 		Assert.assertNull(paper__classifications.getInheritedField());
 		Assert.assertSame(paper, paper__classifications.getDeclaringMmd());
 		Assert.assertSame(tag, paper__classifications.getInheritedMmd());
 		// keywords
 		MetaMetadataCollectionField paper__keywords = (MetaMetadataCollectionField) paper.getChildMetaMetadata().get("keywords");
 		Assert.assertNull(paper__keywords.getInheritedField());
 		Assert.assertSame(paper, paper__keywords.getDeclaringMmd());
 		Assert.assertSame(tag, paper__keywords.getInheritedMmd());
 		
 		
 		
 		MetaMetadata acm_paper = repository.getByTagName("acm_paper");
 		Assert.assertSame(paper, acm_paper.getInheritedMmd());
 		Assert.assertTrue(acm_paper.getInlineMmds() == null || acm_paper.getInlineMmds().isEmpty());
 		Assert.assertEquals(metadata__meta_metadata_name, acm_paper.getChildMetaMetadata().get("meta_metadata_name"));
 		Assert.assertEquals(metadata__mixins, acm_paper.getChildMetaMetadata().get("mixins"));
 		Assert.assertEquals(document__location, acm_paper.getChildMetaMetadata().get("location"));
 		Assert.assertEquals(document__additional_locations, acm_paper.getChildMetaMetadata().get("additional_locations"));
 		Assert.assertEquals(article__source, acm_paper.getChildMetaMetadata().get("source"));
 		Assert.assertEquals(article__pages, acm_paper.getChildMetaMetadata().get("pages"));
 		Assert.assertEquals(paper__abstract_field, acm_paper.getChildMetaMetadata().get("abstract_field"));
 		Assert.assertEquals(paper__references, acm_paper.getChildMetaMetadata().get("references"));
 		Assert.assertEquals(paper__citations, acm_paper.getChildMetaMetadata().get("citations"));
 		Assert.assertEquals(paper__classifications, acm_paper.getChildMetaMetadata().get("classifications"));
 		Assert.assertEquals(paper__keywords, acm_paper.getChildMetaMetadata().get("keywords"));
 		// title
 		MetaMetadataScalarField acm_paper__title = (MetaMetadataScalarField) acm_paper.getChildMetaMetadata().get("title");
 		Assert.assertEquals(article__title, acm_paper__title.getInheritedField());
 		Assert.assertSame(article, acm_paper__title.getDeclaringMmd());
 		// authors
 		MetaMetadataCollectionField acm_paper__authors = (MetaMetadataCollectionField) acm_paper.getChildMetaMetadata().get("authors");
 //		Assert.assertSame(article__authors, acm_paper__authors.getInheritedField()); // should inherit from paper__authors
 		Assert.assertSame(article, acm_paper__authors.getDeclaringMmd());
 		Assert.assertSame(author, acm_paper__authors.getInheritedMmd());
 		// authors.name
 		MetaMetadataScalarField acm_paper__authors__name = (MetaMetadataScalarField) acm_paper__authors.getChildMetaMetadata().get("name");
 		Assert.assertEquals(author__name, acm_paper__authors__name.getInheritedField());
 		Assert.assertSame(author, acm_paper__authors__name.getDeclaringMmd());
 		Assert.assertEquals("location", acm_paper__authors__name.getNavigatesTo());
 		// authors.affiliation
 		MetaMetadataScalarField acm_paper__authors__affiliation = (MetaMetadataScalarField) acm_paper__authors.getChildMetaMetadata().get("affiliation");
 		Assert.assertEquals("./affiliation", acm_paper__authors__affiliation.getXpath());
 	}
 	
 	public static void main(String[] args) throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
//		testGeneratingBasicTScope();
//		testTypeGraphs();
//		testInlineMmd();
//		testArticles();
//		testScalarCollections();
//		testPolymorphicFields();
		testOtherTags();
 	}
 	
 }
