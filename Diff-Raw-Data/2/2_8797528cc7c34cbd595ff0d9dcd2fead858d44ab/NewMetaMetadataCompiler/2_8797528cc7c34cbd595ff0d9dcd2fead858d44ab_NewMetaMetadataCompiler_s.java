 package ecologylab.translators.metametadata;
 
 import java.io.File;
 import java.io.IOException;
 
 import ecologylab.generic.Debug;
 import ecologylab.io.Files;
 import ecologylab.semantics.metadata.builtins.MetadataBuiltinsTranslationScope;
 import ecologylab.semantics.metametadata.MetaMetadataRepository;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 import ecologylab.translators.java.JavaTranslationException;
 
 
 // TODO GENERATED_METADATA_TRANSLATION_SCOPE
 
 
 public class NewMetaMetadataCompiler extends Debug
 {
 
 	public static final String	DEFAULT_REPOSITORY_DIRECTORY					= ".." + Files.sep + "ecologylabSemantics" + Files.sep + "repository";
 
	public static final String	DEFAULT_GENERATED_SEMANTICS_LOCATION	= ".." + Files.sep + "ecologylabGeneratedSemantics";
 	
 	public static final String	META_METADATA_COMPILER_TSCOPE_NAME		= "meta-metadata-compiler-tscope";
 
 	public void compile() throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		MetaMetadataRepository repository = MetaMetadataRepository.load(new File(DEFAULT_REPOSITORY_DIRECTORY));
 		TranslationScope tscope = repository.traverseAndGenerateTranslationScope(META_METADATA_COMPILER_TSCOPE_NAME);
 		TranslationScope.setGraphSwitch();
 		MetaMetadataJavaTranslator jt = new MetaMetadataJavaTranslator();
 		TranslationScope metadataBuiltInTScope = MetadataBuiltinsTranslationScope.get();
 		for (ClassDescriptor cd : metadataBuiltInTScope.getClassDescriptors())
 			jt.excludeClassFromTranslation(cd);
 		jt.translateToJava(new File(DEFAULT_GENERATED_SEMANTICS_LOCATION), tscope);
 	}
 
 	/**
 	 * @param args
 	 * @throws JavaTranslationException 
 	 * @throws SIMPLTranslationException 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException, SIMPLTranslationException, JavaTranslationException
 	{
 		NewMetaMetadataCompiler compiler = new NewMetaMetadataCompiler();
 		compiler.compile();
 	}
 
 }
