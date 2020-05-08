 /**
  * 
  */
 package org.dotplot.tokenizer.filter;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Map;
 
 import org.dotplot.core.ISourceType;
 import org.dotplot.tokenizer.Token;
 import org.dotplot.tokenizer.TokenizerException;
 import org.dotplot.tokenizer.filter.stemmingFilter.Stemmer;
 import org.dotplot.tokenizer.filter.stemmingFilter.StemmerLanguage;
 import org.dotplot.tokenizer.service.ITokenStream;
 import org.dotplot.tokenizer.service.TextType;
 
 /**
  * @author Nils Braden <nils.braden@mni.fh-giessen.de>
  * 
  */
 public class StemmerFilter extends BaseTokenFilter {
 
     public static final String STEMMER_TYPE = "STEMMER_TYPE";
     public static final String STEMMER_LANGUAGE = "STEMMER_LANGUAGE";
     private String selectedStemmer;
     private StemmerLanguage selectedLanguage;
 
     public StemmerFilter() {
 	super();
     }
 
     public StemmerFilter(ITokenStream tokenStream) {
 	super(tokenStream);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.dotplot.tokenizer.ITokenFilter#applyParameter(java.lang.Object[])
      */
     @Override
     public void applyParameter(Map<String, ? extends Object> parameter) {
 	try {
 	    this.selectedStemmer = (String) parameter.get(STEMMER_TYPE);
 	    this.selectedLanguage = (StemmerLanguage) parameter
 		    .get(STEMMER_LANGUAGE);
 	} catch (Exception e) {
 	}
     }
 
     @Override
    public int compareTo(ITokenFilter o) {
	// We dont actually need the Comparable-Interface
 	return 0;
     }
 
    @Override
    public boolean equals(Object obj) {
	// We dont actually need the equals-Method
	return super.equals(obj);
    }

     /**
      * 
      * @see org.dotplot.tokenizer.filter.BaseTokenFilter#filterToken(org.dotplot.
      *      tokenizer.Token)
      */
     @Override
     public Token filterToken(Token token) throws TokenizerException {
 	try {
 	    Class<?> c = Class
 		    .forName("org.dotplot.tokenizer.filter.stemmingFilter."
 			    + selectedStemmer);
 	    // Gucken, ob abstrakte Klasse "Stemmer" implementiert wird
 	    if (c.getGenericSuperclass().equals(Stemmer.class)) {
 		// Interface wird implementiert, neue Instanz erzeugen
 		Constructor<?> con = c.getConstructor(String.class,
 			StemmerLanguage.class);
 		Stemmer stemmer = (Stemmer) con.newInstance(token.getValue(),
 			selectedLanguage);
 		// einzelnes Wort stemmen
 		Token ret = new Token(stemmer.stem(), token.getType(), token
 			.getLine());
 		ret.setSource(token.getSource());
 		return ret;
 	    }
 	} catch (ClassNotFoundException e) {
 	    e.printStackTrace();
 	} catch (InstantiationException e) {
 	    e.printStackTrace();
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	} catch (IllegalArgumentException e) {
 	    e.printStackTrace();
 	} catch (InvocationTargetException e) {
 	    e.printStackTrace();
 	} catch (SecurityException e) {
 	    e.printStackTrace();
 	} catch (NoSuchMethodException e) {
 	    e.printStackTrace();
 	}
 
 	return null;
     }
 
     /**
      * 
      * @see org.dotplot.tokenizer.service.ITokenStream#getStreamType()
      */
     @Override
     public ISourceType getStreamType() {
 	return TextType.type;
     }
 }
