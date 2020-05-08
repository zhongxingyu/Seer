 package com.example.ship.Atlas;
 
 import com.example.ship.R;
 import android.content.Context;
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.xmlpull.v1.XmlPullParser;
 
 import java.util.ArrayList;
 
 /*
 Created by: IVAN
 Date: 22.04.13
  */
 
 public class ResourceManager {
     public final ArrayList<Texture> loadedTextures;                     // List для записи загруженных текстур
     protected final ArrayList<BuildableBitmapTextureAtlas> atlasList;      // List для записи всех атласов
 
     //=== Constructor ===//
     public ResourceManager () {
         loadedTextures = new ArrayList<Texture>();
         atlasList = new ArrayList<BuildableBitmapTextureAtlas>();
     }
 
     //=== Methods ===//
     public ITextureRegion getLoadedTextureRegion ( String pname ) {
         for (int i = 0; i < loadedTextures.size(); i++ )
             if ( loadedTextures.get(i).name.equalsIgnoreCase(pname) )   // IgnoreCase - сравнение без учёта регистра
                 return loadedTextures.get(i).textureRegion;             // Возвращаем Texture Region по имени текстуры.
         return null;
     }
 
     public void loadAllTextures ( Context context,TextureManager pTextureManager, String xmlpath ) {
         int curr_atlas = 0;         // Счётчик атласов
 
         try {
             XmlPullParser parser = context.getResources().getXml( R.xml.atlas );
 
             while (parser.getEventType() != XmlPullParser.END_DOCUMENT ) {
 //============================ Парсер =========================================//
 
                 //==== Случай START_TAG - атлас ====//
                 if (       ( parser.getEventType() == XmlPullParser.START_TAG )
                         && ( parser.getName().equals("Atlas") )
                         && ( parser.getDepth() == 2 ) ) {               // Глубина вложенности - 2
                     //=== Обработка ===//
                     int atlas_height = 1;
                     int atlas_width = 1;
                     TextureOptions textureOptions = TextureOptions.DEFAULT;
 
                     for (int i = 0; i < parser.getAttributeCount(); i++ ) {
                         if( parser.getAttributeName(i).equals("width") )
                             atlas_width = Integer.parseInt(parser.getAttributeValue(i));
                         if( parser.getAttributeName(i).equals("height") )               // Вытаскиваем параметры атласа
                             atlas_height = Integer.parseInt( parser.getAttributeValue(i) );
                        if( parser.getAttributeName(i).equals("texture_options") )
                             textureOptions = stringToTextureOptions(parser.getAttributeValue(i));
                     }
 
                     atlasList.add( new BuildableBitmapTextureAtlas(   pTextureManager
                                                                     , atlas_width
                                                                     , atlas_height
                                                                     , textureOptions ) ); // Создаём новый атлас
                     curr_atlas++;
                     //=================//
                 }
                 //==================================//
 
                 //===== Случай END_TAG - атлас =====//
                 if (       ( parser.getEventType() == XmlPullParser.END_TAG )
                         && ( parser.getName().equals("Atlas") )
                         && ( parser.getDepth() == 2 ) ) {
                     //=== Обработка ===//
                     try {
                         atlasList.get(curr_atlas).build(       // Генерируем новый атлас с текущим номером curr_atlas
                                 new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0,1,1));
                     }
                     catch (ITextureAtlasBuilder.TextureAtlasBuilderException e) {
                         // XXX: Обработка исключения
                     }
                     //=================//
                 }
                 //==================================//
 //=============================================================================//
             }
         }
         catch ( Throwable t ) {
             // XXX: Обработка исключения
             }
     }
 
     private TextureOptions stringToTextureOptions ( String opt ) {
 
         if ( opt.equals("NEAREST") ) return TextureOptions.NEAREST;
         if ( opt.equals("BILINEAR") ) return TextureOptions.BILINEAR;
         if ( opt.equals("REPEATING_NEAREST") ) return TextureOptions.REPEATING_NEAREST;
         if ( opt.equals("REPEATING_BILINEAR") ) return TextureOptions.REPEATING_BILINEAR;
         if ( opt.equals("NEAREST_PREMULTIPLYALPHA") ) return TextureOptions.NEAREST_PREMULTIPLYALPHA;
         if ( opt.equals("BILINEAR_PREMULTIPLYALPHA") ) return TextureOptions.BILINEAR_PREMULTIPLYALPHA;
         if ( opt.equals("REPEATING_NEAREST_PREMULTIPLYALPHA") ) return TextureOptions.REPEATING_NEAREST_PREMULTIPLYALPHA;
         if ( opt.equals("REPEATING_BILINEAR_PREMULTIPLYALPHA") ) return TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA;
 
         return TextureOptions.DEFAULT;
     }
 }
