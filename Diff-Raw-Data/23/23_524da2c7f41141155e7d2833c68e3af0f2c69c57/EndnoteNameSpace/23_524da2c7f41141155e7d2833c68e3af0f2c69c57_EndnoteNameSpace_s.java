 package ecologylab.xml.library.endnote;
 
 import ecologylab.xml.TranslationSpace;
 
 /**
  * Contains all of the information necessary to translate XML objects used in
  * the network communications of the Rogue game. Use MasterNameSpace.get() to
  * acquire a NameSpace object fit for use in the game.
  * 
  * @author Zach Toups
  * 
  */
 public class EndnoteNameSpace
 {
 
     public static final String    NAME           = "endnote";
 
    protected static final String PACKAGE_NAME   = "ecologylab.xml.endnote";
 
     protected static final Class  TRANSLATIONS[] =
                                                  {
 
             ecologylab.xml.subelements.StringState.class, ecologylab.xml.subelements.IntState.class,
             ecologylab.xml.subelements.ArrayListState.class,
 
             ecologylab.xml.library.endnote.Author.class,
             ecologylab.xml.library.endnote.ContributorList.class,
             ecologylab.xml.library.endnote.DateList.class,
             ecologylab.xml.library.endnote.Keyword.class,
             ecologylab.xml.library.endnote.KeywordList.class,
             ecologylab.xml.library.endnote.Record.class,
             ecologylab.xml.library.endnote.Records.class,
             ecologylab.xml.library.endnote.TitleList.class,
             ecologylab.xml.library.endnote.XmlState.class,
 
             ecologylab.xml.library.geom.Point2DDoubleState.class,
             ecologylab.xml.library.geom.Rectangle2DDoubleState.class,
             ecologylab.xml.library.geom.Ellipse2DDoubleState.class,
             ecologylab.xml.library.geom.RectangularShape.class };
 
     public static TranslationSpace get()
     {
         return TranslationSpace.get(NAME, PACKAGE_NAME, TRANSLATIONS);
     }
 
     /**
      * @return the tRANSLATIONS
      */
     public static Class[] getTRANSLATIONS()
     {
         return TRANSLATIONS;
     }
 }
