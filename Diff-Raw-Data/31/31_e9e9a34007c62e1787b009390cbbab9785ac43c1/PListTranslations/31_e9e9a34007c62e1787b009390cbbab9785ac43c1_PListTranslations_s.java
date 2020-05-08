 /**
  * 
  */
 package ecologylab.serialization.library.apple;
 
 import java.io.File;
 import java.io.IOException;
 
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.Format;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.TranslationScope;
 
 /**
  * @author Zachary O. Toups (zach@ecologylab.net)
  */
 public class PListTranslations
 {
 	public static final String		NAME						= "Apple PList";
 
 	@SuppressWarnings("unchecked")
 	protected static final Class	TRANSLATIONS[]	=
 																								{
 			ecologylab.serialization.library.apple.DictionaryProperty.class, KeyProperty.class,
 			PList.class, Property.class, StringProperty.class, ArrayProperty.class,
 			IntegerProperty.class, ArrayProperty.class, BooleanProperty.class, TrueProperty.class,
 			FalseProperty.class, RealProperty.class, DataProperty.class	};
 
 	public static TranslationScope get()
 	{
 		return TranslationScope.get(NAME, TRANSLATIONS);
 	}
 
 	public static void main(String[] args) throws SIMPLTranslationException, IOException
 	{
 		PList result = (PList) PListTranslations.get()
 		// .deserialize("/Users/toupsz/Dropbox/ttecBibForBill/simpTest2.xml");
																						.deserialize(	new File("/Users/toupsz/Dropbox/ttecBibForBill/simpTest3"),
 																													Format.XML);

		ClassDescriptor.serialize(result,
															new File("/Users/toupsz/Dropbox/ttecBibForBill/tecNewTutMap2.xml"),
															Format.XML);
 	}
 }
