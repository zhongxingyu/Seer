 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.loaders.textures;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.jagatoo.loaders.textures.cache.HashTextureCache;
 import org.jagatoo.loaders.textures.cache.TextureCache;
 import org.jagatoo.loaders.textures.formats.TextureFormatLoader;
 import org.jagatoo.loaders.textures.formats.TextureFormatLoaderDDS;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoader;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoaderBMP;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoaderGIF;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoaderImageIOImageInputStream;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoaderPCX;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoaderSGI;
 import org.jagatoo.loaders.textures.formats.TextureImageFormatLoaderTGA;
 import org.jagatoo.loaders.textures.locators.TextureStreamLocator;
 import org.jagatoo.loaders.textures.locators.TextureStreamLocatorFile;
 import org.jagatoo.loaders.textures.locators.TextureStreamLocatorURL;
 import org.jagatoo.logging.JAGTLog;
 import org.jagatoo.logging.ProfileTimer;
import org.jagatoo.opengl.enums.TextureImageFormat;
 
 /**
  * Loads Textures from various image resources.<br>
  * <br>
  * Loading (<b>by name</b>) works in the following order:<br>
  * If the texture was already loaded and is still in the cache then the
  * existing texture is returned. So don't modify loaded nad cached textures!<br>
  * All {@link TextureStreamLocator}s are tried in the order, in which they are
  * registered.<br>
  * If a stream was found then the following is tried on the stream:<br>
  * All {@link TextureImageFormatLoader}s are tried in the order, in which they are
  * registered. A texture can then be created from this {@link AbstractTextureImage}.<br>
  * All {@link TextureFormatLoader}s are tried in the order, in which they are
  * registered.<br>
  * At last the fallback {@link TextureImageFormatLoader} is used to load the
  * Texture. It will most probably use ImageIO.<br>
  * The loading stops as soon as a Texture is created.
  * 
  * @author Matthias Mann
  * @author Marvin Froehlich (aka Qudus)
  * @author Amos Wenger (aka BlueSky)
  */
 public abstract class AbstractTextureLoader
 {
     private TextureCache textureCache;
     
     protected final ArrayList<TextureStreamLocator> textureStreamLocators = new ArrayList<TextureStreamLocator>();
     protected boolean autoDotAddedOnce = false;
     
     protected final ArrayList<TextureImageFormatLoader> textureImageFormatLoaders = new ArrayList< TextureImageFormatLoader >();
     protected final ArrayList<TextureFormatLoader> textureFormatLoaders = new ArrayList< TextureFormatLoader >();
     
     protected TextureImageFormatLoader fallbackTextureImageFormatLoader;
     
     /**
      * Sets the new {@link TextureCache} to be used to cache loaded Textures.
      * 
      * @param textureCache the new {@link TextureCache}
      * @param copyCachedTextures if true, the currently cached Textures are copied to the new cache
      *  
      * @return the previously used {@link TextureCache}.
      */
     public TextureCache setCache( TextureCache textureCache, boolean copyCachedTextures )
     {
         synchronized ( this.textureCache )
         {
             if ( textureCache == null )
             {
                 throw new IllegalArgumentException( "textureCache must not be null." );
             }
             
             TextureCache prevCache = this.textureCache;
             
             if ( copyCachedTextures )
             {
                 String[] keys = prevCache.getCachedKeys();
                 
                 for ( int i = 0; i < keys.length; i++ )
                 {
                     textureCache.add( keys[i], prevCache.get( keys[i] ) );
                 }
             }
             
             this.textureCache = textureCache;
             
             return( prevCache );
         }
     }
     
     /**
      * @return the {@link TextureCache} used to cache loaded Textures (never null).
      */
     public final TextureCache getCache()
     {
         return( textureCache );
     }
     
     /**
      * Registers a {@link TextureStreamLocator}. A TextureStreamLocator returns an
      * InputStream that holds the data for a Texture or an Image. Note: No check
      * is performed to prevent duplicate registrations!
      * 
      * @param tsl the TextureStreamLocator to add
      */
     public synchronized void addTextureStreamLocator( TextureStreamLocator tsl )
     {
         textureStreamLocators.add( tsl );
     }
     
     /**
      * Registers a {@link TextureStreamLocatorURL}. A TextureStreamLocator returns an
      * InputStream that holds the data for a Texture or a Image. Note: No check
      * is performed to prevent duplicate registrations!
      * 
      * @param url the URL to create and add a {@link TextureStreamLocatorURL} from
      * 
      * @return the created {@link TextureStreamLocatorURL}.
      */
     public synchronized TextureStreamLocatorURL addTextureStreamLocator( URL url )
     {
         TextureStreamLocatorURL tsl = new TextureStreamLocatorURL( url );
         
         addTextureStreamLocator( tsl );
         
         return( tsl );
     }
     
     /**
      * Registers a {@link TextureStreamLocatorURL}. A TextureStreamLocator returns an
      * InputStream that holds the data for a Texture or a Image. Note: No check
      * is performed to prevent duplicate registrations!
      * 
      * @param baseURL the base-URL to create and add a {@link TextureStreamLocatorURL} from
      * @param resName the resource-name to compose a new URL from (together with baseURL)
      * 
      * @return the created {@link TextureStreamLocatorURL}.
      * @throws MalformedURLException 
      */
     public synchronized TextureStreamLocatorURL addTextureStreamLocator( URL baseURL, String resName ) throws MalformedURLException
     {
         return( addTextureStreamLocator( new URL( baseURL, resName ) ) );
     }
     
     /**
      * Registers a {@link TextureStreamLocatorFile}. A TextureStreamLocator returns an
      * InputStream that holds the data for a Texture or a Image. Note: No check
      * is performed to prevent duplicate registrations!
      * 
      * @param file the File to create and add a {@link TextureStreamLocatorFile} from
      * 
      * @return the created {@link TextureStreamLocatorFile}.
      */
     public synchronized TextureStreamLocatorFile addTextureStreamLocator( File file )
     {
         TextureStreamLocatorFile tsl = new TextureStreamLocatorFile( file );
         
         addTextureStreamLocator( tsl );
         
         return( tsl );
     }
     
     /**
      * Registers a {@link TextureStreamLocatorFile}. A TextureStreamLocator returns an
      * InputStream that holds the data for a Texture or a Image. Note: No check
      * is performed to prevent duplicate registrations!
      * 
      * @param filename the filename to create and add a {@link TextureStreamLocatorFile} from
      * 
      * @return the created {@link TextureStreamLocatorFile}.
      */
     public synchronized TextureStreamLocatorFile addTextureStreamLocator( String filename )
     {
         TextureStreamLocatorFile tsl = new TextureStreamLocatorFile( filename );
         
         addTextureStreamLocator( tsl );
         
         return( tsl );
     }
     
     /**
      * Removes a registered TextureStreamLocator.
      * 
      * @param tsl the TextureStreamLocator to add
      * 
      * @return true if a registration was removed.
      */
     public synchronized boolean removeTextureStreamLocator( TextureStreamLocator tsl )
     {
         return( textureStreamLocators.remove( tsl ) );
     }
     
     /**
      * Registers a TextureImageFormatLoader. A TextureImageFormatLoader creates a
      * TextureImage from a given InputStream (if it has the correct format).
      * Note: No check is performed to prevent duplicate registrations!
      * 
      * @param l The TextureImageLoader.
      */
     public synchronized void addTextureImageFormatLoader( TextureImageFormatLoader l )
     {
         textureImageFormatLoaders.add( 0, l );
     }
     
     /**
      * Removes a registered TextureImageFormatLoader
      * 
      * @param l the TextureImageLoader to be removed
      * 
      * @return true if a registration was removed.
      */
     public synchronized boolean removeTextureImageFormatLoader( TextureImageFormatLoader l )
     {
         return( textureImageFormatLoaders.remove( l ) );
     }
     
     /**
      * Registers a TextureStreamLoader. A TextureStreamLoader creates a Texture
      * from a given InputStream (if it has the correct format). Note: No check
      * is made to prevent duplicate registrations!
      * 
      * @param tsl the TextureStreamLoader to add
      */
     public synchronized void addTextureFormatLoader( TextureFormatLoader tsl )
     {
         textureFormatLoaders.add( 0, tsl );
     }
     
     /**
      * Removes a registered TextureStreamLoader.
      * 
      * @param tsl the TextureStreamLoader to be removed
      * 
      * @return true if a registration was removed.
      */
     public synchronized boolean removeTextureFormatLoader( TextureFormatLoader tsl )
     {
         return( textureFormatLoaders.remove( tsl ) );
     }
     
     /**
      * Sets the {@link TextureImageFormatLoader}, that is used as the
      * fallback-loader, if no other format-loader was capable of loading
      * the requested texture resource.
      * 
      * @param ftifl
      */
     public void setFallbackTextureImageFormatLoader( TextureImageFormatLoader ftifl )
     {
         if ( ftifl == null )
         {
             throw new IllegalArgumentException( "ftifl must not be null." );
         }
         
         this.fallbackTextureImageFormatLoader = ftifl;
     }
     
     /**
      * @return the {@link TextureImageFormatLoader}, that is used as the
      * fallback-loader, if no other format-loader was capable of loading
      * the requested texture resource.
      */
     public final TextureImageFormatLoader getFallbackTextureImageFormatLoader()
     {
         return( fallbackTextureImageFormatLoader );
     }
     
     
     protected AbstractTexture createTextureFromTextureImage( AbstractTextureImage ti, boolean loadMipmaps, TextureFactory texFactory )
     {
         AbstractTexture tex = texFactory.createTexture( ti.getFormat() );
         
         tex.setImage( 0, ti );
         
        if ( loadMipmaps && ( ti.getFormat() != TextureImageFormat.DEPTH ) && ( ti.getFormat().getPixelSize() > 1 ) )
         {
             MipmapGenerator.createMipMaps( ti, tex, texFactory );
         }
         
         return( tex );
     }
     
     private static final void resetInputStreamIfTexIsNull( Object tex, BufferedInputStream in )
     {
         if ( tex == null )
         {
             try
             {
                 /*
                  * The TextureFormatLoader wasn't able to decode the stream.
                  * Reset it to resue it for the next one.
                  */
                 in.reset();
             }
             catch ( IOException e )
             {
                 e.printStackTrace();
             }
         }
     }
     
     private AbstractTextureImage tryToLoadFromTextureImageFormatLoaders( BufferedInputStream in, boolean flipVertically, boolean acceptAlpha, boolean allowStreching, TextureFactory texFactory )
     {
         AbstractTextureImage ti = null;
         
         for ( int i = 0; ( i < textureImageFormatLoaders.size() ) && ( ti == null ); i++ )
         {
             TextureImageFormatLoader tifl = textureImageFormatLoaders.get( i );
             try
             {
                 ti = tifl.loadTextureImage( in, acceptAlpha, flipVertically, allowStreching, texFactory );
             }
             catch ( Throwable t )
             {
                 t.printStackTrace();
             }
             
             resetInputStreamIfTexIsNull( ti, in );
         }
         
         return( ti );
     }
     
     private AbstractTexture tryToLoadFromTextureFormatLoaders( BufferedInputStream in, boolean flipVertically, boolean acceptAlpha, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory )
     {
         AbstractTexture tex = null;
         
         for ( int i = 0; ( i < textureFormatLoaders.size() ) && ( tex == null ); i++ )
         {
             TextureFormatLoader tfl = textureFormatLoaders.get( i );
             
             try
             {
                 tex = tfl.loadTexture( in, acceptAlpha, loadMipmaps, allowStreching, texFactory );
             }
             catch ( IOException e )
             {
                 e.printStackTrace();
             }
             
             resetInputStreamIfTexIsNull( tex, in );
         }
         
         return( tex );
     }
     
     private AbstractTexture loadFromFallbackLoader( BufferedInputStream in, boolean flipVertically, boolean acceptAlpha, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory )
     {
         AbstractTextureImage ti = null;
         AbstractTexture tex = null;
         
         try
         {
             ti = fallbackTextureImageFormatLoader.loadTextureImage( in, acceptAlpha, flipVertically, allowStreching, texFactory );
             
             if ( ti != null )
             {
                 tex = createTextureFromTextureImage( ti, loadMipmaps, texFactory );
             }
         }
         catch ( Throwable t )
         {
             t.printStackTrace();
         }
         
         resetInputStreamIfTexIsNull( ti, in );
         
         return( tex );
     }
     
     /**
      * Tries to load the texture-image from an InputStream.
      * 
      * @param in the InputStream to load the Texture from
      * @param flipVertically flip the image vertically or not
      * @param acceptAlpha try to load with alpha channel or not
      * @param loadMipmaps create mipmaps?
      * @param allowStreching If true, the image is streched to power-of-two width and height, if necessary.
      * @param texFactory 
      * 
      * @return the loaded TextureImage or null.
      */
     protected AbstractTextureImage loadTextureImageFromStream( BufferedInputStream in, boolean flipVertically, boolean acceptAlpha, boolean allowStreching, TextureFactory texFactory )
     {
         if ( in == null )
         {
             return( null );
         }
         
         int available = 0;
         try
         {
             available = in.available();
         }
         catch ( IOException e )
         {
             if ( !e.getMessage().contains( "Stream closed" ) )
                 e.printStackTrace();
             
             return( null );
         }
         
         AbstractTextureImage texImg = null;
         
         if ( available > 0 )
         {
             in.mark( Integer.MAX_VALUE );
             
             if ( texImg == null )
             {
                 texImg = tryToLoadFromTextureImageFormatLoaders( in, flipVertically, acceptAlpha, allowStreching, texFactory );
             }
             
             if ( texImg == null )
             {
                 AbstractTexture tex = tryToLoadFromTextureFormatLoaders( in, flipVertically, acceptAlpha, false, allowStreching, texFactory );
                 
                 if ( tex != null )
                 {
                     texImg = tex.getImage( 0 );
                 }
             }
             
             if ( texImg == null )
             {
                 try
                 {
                     texImg = fallbackTextureImageFormatLoader.loadTextureImage( in, acceptAlpha, flipVertically, allowStreching, texFactory );
                     
                     resetInputStreamIfTexIsNull( texImg, in );
                 }
                 catch ( Throwable t )
                 {
                     t.printStackTrace();
                 }
             }
         }
         
         try
         {
             in.close();
         }
         catch ( IOException e )
         {
             //e.printStackTrace();
         }
         
         return( texImg );
     }
     
     /**
      * Tries to load the texture from an InputStream.
      * 
      * @param in the InputStream to load the Texture from
      * @param flipVertically flip the image vertically or not
      * @param acceptAlpha try to load with alpha channel or not
      * @param loadMipmaps create mipmaps?
      * @param allowStreching If true, the image is streched to power-of-two width and height, if necessary.
      * @param texFactory 
      * 
      * @return the loaded Texture or null.
      */
     protected AbstractTexture loadTextureFromStream( BufferedInputStream in, boolean flipVertically, boolean acceptAlpha, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory )
     {
         if ( in == null )
         {
             return( null );
         }
         
         int available = 0;
         try
         {
             available = in.available();
         }
         catch ( IOException e )
         {
             if ( !e.getMessage().contains( "Stream closed" ) )
                 e.printStackTrace();
             
             return( null );
         }
         
         AbstractTexture tex = null;
         
         if ( available > 0 )
         {
             in.mark( Integer.MAX_VALUE );
             
             if ( tex == null )
             {
                 AbstractTextureImage ti = tryToLoadFromTextureImageFormatLoaders( in, flipVertically, acceptAlpha, allowStreching, texFactory );
                 
                 if ( ti != null )
                 {
                     tex = createTextureFromTextureImage( ti, loadMipmaps, texFactory );
                 }
             }
             
             if ( tex == null )
             {
                 tex = tryToLoadFromTextureFormatLoaders( in, flipVertically, acceptAlpha, loadMipmaps, allowStreching, texFactory );
             }
             
             if ( tex == null )
             {
                 tex = loadFromFallbackLoader( in, flipVertically, acceptAlpha, loadMipmaps, allowStreching, texFactory );
             }
         }
         
         try
         {
             in.close();
         }
         catch ( IOException e )
         {
             //e.printStackTrace();
         }
         
         return( tex );
     }
     
     
     protected String generateCacheKey( String name, boolean acceptAlpha, boolean loadMipmaps, boolean flipVertically, boolean allowStreching )
     {
         String cacheKey = name + "-" + ( acceptAlpha ? "RGBA?" : "RGB" ) + "-" + ( loadMipmaps ? "MULTI_LEVEL_MIPMAP" : "BASE_LEVEL" ) + "-" + ( flipVertically ? "flipped" : "" ) + "-" + ( allowStreching ? "streched?" : "unstreched" );
         
         return( cacheKey );
     }
     
     protected final AbstractTexture checkCache( String name, String key )
     {
         synchronized ( getCache() )
         {
             if ( !getCache().isEnabled() )
                 return( null );
             
             AbstractTexture tex = getCache().get( key );
             
             if ( tex == null )
             {
                 return( null );
             }
             
             JAGTLog.debug( "Texture [", name, "] taken from cache" );
             
             return( tex );
         }
     }
     
     
     protected BufferedInputStream getInputStream( String name )
     {
         InputStream in = null;
         
         if ( ( !autoDotAddedOnce ) && ( textureStreamLocators.isEmpty() ) )
         {
             // Added for noobs
             addTextureStreamLocator( new TextureStreamLocatorFile( new File( "." ) ) );
             autoDotAddedOnce = true;
         }
         
         for ( int i = 0; ( i < textureStreamLocators.size() ) && ( in == null ); i++ )
         {
             TextureStreamLocator tsl = textureStreamLocators.get( i );
             
             try
             {
                 in = tsl.openTextureStream( name );
             }
             catch ( Throwable ex )
             {
                 ex.printStackTrace();
             }
         }
         
         if ( in instanceof BufferedInputStream )
         {
             return( (BufferedInputStream)in );
         }
         
         return( new BufferedInputStream( in ) );
     }
     
     
     /**
      * Retrives the texture-image with the given name. Loading works in the following
      * order:<br>
      * All {@link TextureStreamLocator}s are tried in the order in which they are
      * registered.<br>
      * If a stream was found then the following is tried on the stream:<br>
      * All {@link TextureImageFormatLoader}s are tried in the order in which they are
      * registered. A texture is then created with this {@link AbstractTextureImage}
      * (mipmaps currently not implemented).<br>
      * All {@link TextureFormatLoader}s are tried in the order in which they are
      * registered.<br>
      * At last the fallback {@link TextureImageFormatLoader} is used to load the
      * Texture. It will most probably use ImageIO.<br>
      * The loading stops as soon as a Texture is created.<br>
      * 
      * @param name The name of the texture.
      * @param flipVertically flip the image vertically or not
      * @param acceptAlpha try to load with alpha channel or not
      * @param allowStreching If true, the image is streched to power-of-two width and height, if necessary.
      * @param texFactory 
      * 
      * @return The {@link AbstractTextureImage} object or a null, if it was not found.
      */
     protected AbstractTextureImage loadTextureImage( String name, boolean flipVertically, boolean acceptAlpha, boolean allowStreching, TextureFactory texFactory )
     {
         ProfileTimer.startProfile( JAGTLog.LOG_CHANNEL, "TextureLoader::loadOrGetTextureImage" );
         
         if ( ( name == null ) || name.equals( "" ) )
         {
             return( null );
         }
         
         AbstractTextureImage texImg = null;
         
         BufferedInputStream in = getInputStream( name );
         
         texImg = loadTextureImageFromStream( in, flipVertically, acceptAlpha, allowStreching, texFactory );
         
         ProfileTimer.endProfile();
         
         return( texImg );
     }
     
     /**
      * This method is called by the loaded after a texture haa actually been
      * loaded, but not taken from teh cache.
      * 
      * @param texture
      * @param resourceName
      */
     protected abstract void onTextureLoaded( AbstractTexture texture, String resourceName );
     
     /**
      * Retrives the texture with the given name. Loading works in the following
      * order:<br>
      * If the texture was already loaded and is still in the cache then the
      * existing texture is returned. So don't modify textures returned by this
      * method.<br>
      * All {@link TextureStreamLocator}s are tried in the order in which they are
      * registered.<br>
      * If a stream was found then the following is tried on the stream:<br>
      * All {@link TextureImageFormatLoader}s are tried in the order in which they are
      * registered. A texture is then created with this {@link AbstractTextureImage}
      * (mipmaps currently not implemented).<br>
      * All {@link TextureFormatLoader}s are tried in the order in which they are
      * registered.<br>
      * At last the fallback {@link TextureImageFormatLoader} is used to load the
      * Texture. It will most probably use ImageIO.<br>
      * The loading stops as soon as a Texture is created.<br>
      * Mipmaps are generated, if requested.
      * 
      * @param name The name of the texture.
      * @param flipVertically flip the image vertically or not
      * @param acceptAlpha try to load with alpha channel or not
      * @param loadMipmaps create mipmaps?
      * @param allowStreching If true, the image is streched to power-of-two width and height, if necessary.
      * @param texFactory 
      * 
      * @return The {@link AbstractTexture} object or a null, if it was not found.
      */
     protected AbstractTexture loadOrGetTexture( String name, boolean flipVertically, boolean acceptAlpha, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory )
     {
         ProfileTimer.startProfile( JAGTLog.LOG_CHANNEL, "TextureLoader::loadOrGetTexture" );
         
         if ( ( name == null ) || name.equals( "" ) )
         {
             return( null );
         }
         
         AbstractTexture tex = null;
         
         String cacheKey = generateCacheKey( name, acceptAlpha, loadMipmaps, flipVertically, allowStreching );
         
         if ( ( tex = checkCache( name, cacheKey ) ) != null )
         {
             return( tex );
         }
         
         BufferedInputStream in = getInputStream( name );
         
         tex = loadTextureFromStream( in, flipVertically, acceptAlpha, loadMipmaps, allowStreching, texFactory );
         
         if ( tex != null )
         {
             // If tex has been loaded successfully, put it in the cache.
             // We don't put it in the cache if it has not been loaded, or
             // it will never be loaded correctly (if created after having
             // tried once to load it.
             if ( getCache().isEnabled() )
             {
                 getCache().add( cacheKey, tex );
                 tex.setCacheKey( cacheKey );
             }
             
             tex.setName( name );
             
             onTextureLoaded( tex, name );
         }
         
         ProfileTimer.endProfile();
         
         return( tex );
     }
     
     /**
      * Retrives the texture with the given name. Loading works in the following
      * order:<br>
      * All {@link TextureImageFormatLoader}s are tried in the order in which they are
      * registered. A texture is then created with this {@link AbstractTextureImage}
      * (mipmaps currently not implemented).<br>
      * All {@link TextureFormatLoader}s are tried in the order in which they are
      * registered.<br>
      * At last the fallback {@link TextureImageFormatLoader} is used to load the
      * Texture. It will most probably use ImageIO.<br>
      * The loading stops as soon as a Texture is created.<br>
      * Mipmaps are generated, if requested.
      * 
      * @param imageURL The name of the texture.
      * @param flipVertically flip the image vertically or not
      * @param acceptAlpha try to load with alpha channel or not
      * @param loadMipmaps create mipmaps?
      * @param allowStreching If true, the image is streched to power-of-two width and height, if necessary.
      * @param texFactory 
      * 
      * @return The {@link AbstractTexture} object or a null, if it was not found.
      */
     protected AbstractTexture loadTextureFromURL( URL imageURL, boolean flipVertically, boolean acceptAlpha, boolean loadMipmaps, boolean allowStreching, TextureFactory texFactory )
     {
         if ( imageURL == null )
         {
             return( null );
         }
         
         AbstractTexture tex = null;
         String name = null;
         
         //String cacheKey;
         try
         {
             name = imageURL.toURI().toString();
             
             //cacheKey = generateCacheKey( name, acceptAlpha, loadMipmaps, flipVertically, allowStreching );
         }
         catch ( URISyntaxException e )
         {
             RuntimeException re = new RuntimeException();
             re.initCause( e );
             
             throw( re );
         }
         
         /*
         if ( ( tex = checkCache( name, cacheKey ) ) != null )
         {
             return( tex );
         }
         */
         
         InputStream in = null;
         
         try
         {
             in = imageURL.openStream();
         }
         catch ( Throwable ex )
         {
             //ex.printStackTrace();
         }
         
         if ( in == null )
         {
             return( null );
         }
         
         if ( !( in instanceof BufferedInputStream ) )
         {
             in = new BufferedInputStream( in );
         }
         
         tex = loadTextureFromStream( (BufferedInputStream)in, flipVertically, acceptAlpha, loadMipmaps, allowStreching, texFactory );
         
         if ( tex != null )
         {
             /*
             // If tex has been loaded successfully, put it in the cache.
             // We don't put it in the cache if it has not been loaded, or
             // it will never be loaded correctly (if created after having
             // tried once to load it.
             if ( getCache().isEnabled() )
             {
                 getCache().add( cacheKey, tex );
                 tex.setCacheKey( cacheKey );
             }
             */
             
             tex.setName( name );
             
             onTextureLoaded( tex, name );
         }
         
         return( tex );
     }
     
     
     /**
      * Adds all known standard {@link TextureImageFormatLoader}s.
      */
     protected void initStandardFormatLoaders()
     {
         /*
          * Add all standard format-loaders in reverse order...
          */
         
         addTextureImageFormatLoader( new TextureImageFormatLoaderSGI() );
         addTextureImageFormatLoader( new TextureImageFormatLoaderBMP() );
         addTextureImageFormatLoader( new TextureImageFormatLoaderPCX() );
         addTextureImageFormatLoader( new TextureImageFormatLoaderGIF() );
         addTextureImageFormatLoader( new TextureImageFormatLoaderTGA() );
         
         addTextureFormatLoader( new TextureFormatLoaderDDS() );
     }
     
     
     protected AbstractTextureLoader( TextureCache textureCache, TextureImageFormatLoader fallbackTextureImageFormatLoader )
     {
         if ( textureCache == null )
             throw new NullPointerException( "textureCache must not be null" );
         
         this.textureCache = textureCache;
         
         if ( fallbackTextureImageFormatLoader == null )
             throw new NullPointerException( "fallbackTextureImageFormatLoader must not be null." );
         
         this.fallbackTextureImageFormatLoader = fallbackTextureImageFormatLoader;
         
         initStandardFormatLoaders();
     }
     
     protected AbstractTextureLoader()
     {
         //this( new HashTextureCache(), new TextureImageFormatLoaderImageIOBufferedImage() );
         this( new HashTextureCache(), new TextureImageFormatLoaderImageIOImageInputStream() );
     }
 }
