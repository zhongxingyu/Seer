 package ecologylab.translators.metametadata.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope;
 import ecologylab.semantics.metametadata.MetaMetadataRepository;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.translators.java.JavaTranslationException;
 import ecologylab.translators.java.JavaTranslator;
 
 public class TestNewMetaMetadataCompiler
 {
 	
 	public static void testGeneratingBasicTScope() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		MetaMetadataRepository repository = MetaMetadataRepository.readRepository(new File("data/testRepository/testGeneratingBasicTScope.xml"));
 		TranslationScope tscope = repository.traverseAndGenerateTranslationScope();
 		TranslationScope.setGraphSwitch();
 		JavaTranslator jt = new JavaTranslator();
 		TranslationScope metadataBuiltInTScope = MetadataBuiltinsTranslationScope.get();
 		for (ClassDescriptor cd : metadataBuiltInTScope.getClassDescriptors())
 			jt.excludeClassFromTranslation(cd);
 		jt.translateToJava(new File("c:/tmp/testbasictscope/"), tscope);
 	}
 
 	public static void main(String[] args) throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
		testGeneratingBasicTScope();
 	}
 }
