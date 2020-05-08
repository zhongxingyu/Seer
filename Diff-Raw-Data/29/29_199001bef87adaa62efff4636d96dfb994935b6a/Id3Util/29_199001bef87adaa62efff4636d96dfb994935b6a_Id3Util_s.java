 package org.jcoderz.mp3.intern.util;
 
import java.util.logging.Logger;

 import org.jaudiotagger.tag.FieldDataInvalidException;
 import org.jaudiotagger.tag.FieldKey;
 import org.jaudiotagger.tag.KeyNotFoundException;
 import org.jaudiotagger.tag.TagException;
 import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
 import org.jaudiotagger.tag.id3.ID3Tags;
 import org.jaudiotagger.tag.id3.ID3v11Tag;
 import org.jaudiotagger.tag.reference.GenreTypes;
 import org.jcoderz.commons.util.StringUtil;
 
 /**
  * Utilities around the mp3agic mp3 lib.
  * https://github.com/mpatric/mp3agic
  * 
  * @author Andreas Mandel
  */
 public final class Id3Util
 {
    private static final Logger LOGGER = Logger.getLogger(Id3Util.class.getName());
     
     /** No instances. */
     private Id3Util()
     {
         // no instances.
     }
 
     public static String fixGenre (AbstractID3v2Tag id3v2Tag)
     {
         String genre = id3v2Tag.getFirst(FieldKey.GENRE).trim();
         if (genre.equalsIgnoreCase("unknown")
             || genre.equalsIgnoreCase("undefined")
             || genre.equals("-")
             || genre.equalsIgnoreCase("genre"))
         {
             genre = "";
         }
         final Integer genreNum = GenreTypes.getInstanceOf().getIdForName(genre);
         final String result;
         if (genreNum == null)
         {
             if (StringUtil.isBlankOrNull(genre))
             {
                 result = "";
             }
             else if (genre.matches("\\([0-9]*\\).*"))
             {
                 result = genre;
             }
             else
             {
                 // 12 == Other....
                 result = "(12)" + genre;
             }
         }
         else
         {
             result = "(" + genreNum + ")" + genre;
         }
         return result;
     }
     
     public static ID3v11Tag toId3v11 (AbstractID3v2Tag id3v2Tag)
     {
         final ID3v11Tag result = new ID3v11Tag();
         copyTag(FieldKey.ARTIST, id3v2Tag, result);
         copyTag(FieldKey.ALBUM, id3v2Tag, result);
         copyGenre(id3v2Tag, result);
         copyTag(FieldKey.TITLE, id3v2Tag, result);
         copyTag(FieldKey.YEAR, id3v2Tag, result);
         copyTag(FieldKey.TRACK, id3v2Tag, result);
         copyTag(FieldKey.COMMENT, id3v2Tag, result);
         return result;
     }
 
     private static void copyGenre (
         AbstractID3v2Tag id3v2Tag, ID3v11Tag result)
     {
         final String genre = id3v2Tag.getFirst(FieldKey.GENRE);
         result.setGenre(genre);
         try
         {
             if (StringUtil.isEmptyOrNull(result.getFirstGenre())
                 && !StringUtil.isEmptyOrNull(genre))
             {
                 long genreNum = ID3Tags.findNumber(genre);
                 result.setGenre(GenreTypes.getInstanceOf().getValueForId((int) genreNum));
             }
         }
         catch (TagException e)
         {
             // set no genre 
         }
     }
 
     private static void copyTag (
         FieldKey fk, AbstractID3v2Tag from, ID3v11Tag to)
     {
         try
         {
             to.setField(fk, from.getFirst(fk));
         }
         catch (KeyNotFoundException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (FieldDataInvalidException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
