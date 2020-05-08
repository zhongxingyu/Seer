 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.threedee.assets;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import de.ailis.threedee.exceptions.AssetIOException;
 import de.ailis.threedee.exceptions.AssetNotFoundException;
 
 
 /**
  * Asset Provider which loads assets from the class path.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public class ClasspathAssetProvider implements AssetProvider
 {
     /** The asset type directory mapping. */
     private static Map<AssetType, String> directories;
 
     /** The base directory. */
     private final String baseDir;
 
     {
         directories = new HashMap<AssetType, String>();
         directories.put(AssetType.TEXTURE, "/textures/");
         directories.put(AssetType.MATERIAL, "/materials/");
         directories.put(AssetType.ANIMATION, "/animations/");
         directories.put(AssetType.MESH, "/meshes/");
         directories.put(AssetType.SCENE, "/scenes/");
         directories.put(AssetType.ASSETS, "/assets/");
     }
 
 
     /**
      * Constructs an asset provider which loads assets from the root directory
      * of the class path.
      */
 
     public ClasspathAssetProvider()
     {
        this.baseDir = "";
     }
 
 
     /**
      * Constructs an asset provider which loads assets from the specified
      * directory of the class path.
      *
      * @param baseDir
      *            The base directory (relative to the root directory).
      */
 
     public ClasspathAssetProvider(final String baseDir)
     {
         this.baseDir = "/" + baseDir;
     }
 
 
     /**
      * @see AssetProvider#exists(AssetType, String)
      */
 
     @Override
     public boolean exists(final AssetType type, final String id)
     {
         final String dir = directories.get(type);
         for (final AssetFormat format : type.getFormats())
         {
             for (final String extension : format.getExtensions())
             {
                 final String filename = this.baseDir + dir + id + extension;
                 if (getClass().getResource(filename + ".gz") != null) return true;
                 if (getClass().getResource(filename) != null) return true;
             }
         }
         return false;
     }
 
 
     /**
      * @see AssetProvider#openInputStream(AssetType, String)
      */
 
     @Override
     public AssetInputStream openInputStream(final AssetType type,
         final String id)
     {
         final String dir = directories.get(type);
         for (final AssetFormat format : type.getFormats())
         {
             for (final String extension : format.getExtensions())
             {
                 final String filename = this.baseDir + dir + id + extension;
                 InputStream stream = getClass()
                     .getResourceAsStream(filename + ".gz");
                 if (stream != null)
                 {
                     try
                     {
                         return new AssetInputStream(format,
                             new GZIPInputStream(
                                 stream));
                     }
                     catch (final IOException e)
                     {
                         throw new AssetIOException(e.toString(), e);
                     }
                 }
                 stream = getClass()
                     .getResourceAsStream(filename);
                 if (stream != null)
                     return new AssetInputStream(format, stream);
             }
         }
         throw new AssetNotFoundException("Asset not found: " + type + " / "
             + id);
     }
 }
