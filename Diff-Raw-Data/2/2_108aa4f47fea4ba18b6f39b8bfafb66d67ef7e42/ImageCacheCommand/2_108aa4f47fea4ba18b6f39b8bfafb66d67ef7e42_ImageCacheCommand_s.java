 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.photo.plugin.cmd;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.cache.Cache;
 import org.xwiki.cache.CacheException;
 import org.xwiki.cache.CacheManager;
 import org.xwiki.cache.config.CacheConfiguration;
 import org.xwiki.cache.eviction.LRUEvictionConfiguration;
 import org.xwiki.context.Execution;
 
 import com.celements.photo.container.ImageDimensions;
 import com.celements.photo.plugin.CelementsPhotoPlugin.SupportedFormat;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.web.Utils;
 
 public class ImageCacheCommand {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       ImageCacheCommand.class);
 
   /**
    * Cache for already served images.
    */
   private Cache<byte[]> imageCache;
 
   private boolean initializedCache;
 
   /**
    * The size of the cache. This parameter can be configured using
    * the key <tt>xwiki.plugin.image.cache.capacity</tt>.
    */
   private int capacity = 50;
 
   /**
    * The time to live (seconds) of a cache entry after last access. This parameter can be
    * configured using the key <tt>xwiki.plugin.image.cache.ttl</tt>.
    */
   private Integer ttlConfig = 2500000;
 
   public ImageCacheCommand() {
     initializedCache = false;
   }
 
   Cache<byte[]> getImageCache() {
     if (!initializedCache) {
       initCache();
     }
     return imageCache;
   }
 
   /* Copy, Paste & Customize from com.xpn.xwiki.plugin.image */
   synchronized void initCache() {
     CacheConfiguration configuration = new CacheConfiguration();
     
     configuration.setConfigurationId("celements.photo");
     
     // Set folder o store cache
     File tempDir = getContext().getWiki().getTempDirectory(getContext());
     File imgTempDir = new File(tempDir, configuration.getConfigurationId());
     try {
       imgTempDir.mkdirs();
     } catch (Exception ex) {
       LOGGER.warn("Cannot create temporary files", ex);
     }
     configuration.put("cache.path", imgTempDir.getAbsolutePath());
     
     // Set cache constraints
     LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
     capacity = readIntegerValue("xwiki.plugin.image.cache.capacity", capacity);
     lru.setMaxEntries(capacity);
     ttlConfig = readIntegerValue("xwiki.plugin.image.cache.ttl", ttlConfig);
     lru.setTimeToLive(ttlConfig);
     LOGGER.debug("creating an image cache with capacity [" + lru.getMaxEntries()
         + "] and ttl [" + lru.getTimeToLive() + "] and cache.path ["
         + lru.get("cache.path") + "].");
     configuration.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);
 
     try {
       imageCache = getCacheManager().createNewCache(configuration);
     } catch (CacheException exp) {
       LOGGER.error("Error initializing the image cache.", exp);
     }
     initializedCache = true;
   }
 
   private Integer readIntegerValue(String paramKey, Integer defaultValue) {
     String capacityParam = "";
     try {
       capacityParam = getContext().getWiki().Param(paramKey);
       if ((capacityParam != null) && (!capacityParam.equals(""))) {
         return Integer.parseInt(capacityParam);
       }
     } catch (NumberFormatException ex) {
       LOGGER.error("Error in ImagePlugin reading capacity: " + capacityParam, ex);
     }
     return defaultValue;
   }
 
   public void addToCache(String key, XWikiAttachment attachment) throws XWikiException {
     if (getImageCache() != null) {
       try {
         getImageCache().set(key, IOUtils.toByteArray(attachment.getContentInputStream(
             getContext())));
       } catch (IOException exp) {
         LOGGER.error("Failed to cache image [" + key + "].", exp);
       }
     } else {
       LOGGER.info("Caching of images deactivated.");
     }
   }
 
   public InputStream getImageForKey(String key) {
     if (getImageCache() != null) {
       byte[] cachedData = getImageCache().get(key);
       if (cachedData != null) {
         return new ByteArrayInputStream(cachedData);
       }
     }
     return null;
   }
 
   String getCacheKey(XWikiAttachment attachment, ImageDimensions dimension,
       String copyright, String watermark, int cropX, int cropY, int cropW, int cropH,
       boolean blackNwhite, Color defaultBg, boolean lowerBounds, Integer lowBoundPos, 
       boolean raw) throws NoSuchAlgorithmException {
     String key = attachment.getId() 
         + "-" + attachment.getVersion()
         + "-" + getType(attachment.getMimeType(getContext()))
         + "-" + attachment.getDate().getTime()
         + "-" + dimension.getWidth()
         + "-" + dimension.getHeight()
         + "-" + getAditionalInfoHash(copyright, watermark, cropX, cropY, cropW, cropH,
             blackNwhite, defaultBg, lowerBounds, lowBoundPos, raw);
     return key;
   }
 
   String getAditionalInfoHash(String copyright, String watermark, int cropX, int cropY, 
       int cropW, int cropH, boolean blackNwhite, Color defaultBg, boolean lowerBounds, 
       Integer lowBoundPos, boolean raw) throws NoSuchAlgorithmException {
     String hashValue = "";
     if(raw) {
       hashValue += "<:>raw image";
     } else {
       if(((watermark != null) && (watermark.length() > 0))
           || ((copyright != null) && (copyright.length() > 0))){
         hashValue += "<:>" + watermark + "<:>" + copyright;
       }
       if((cropX >= 0) && (cropY >= 0) && (cropW > 0) && (cropH > 0)){
         hashValue += "<:>" + cropX + ":" + cropY + "_" + cropW + "x" + cropH;
       }
       if(blackNwhite) {
         hashValue += "<:>Black and White";
       }
       if(defaultBg != null) {
         hashValue += "<:>col" + defaultBg.getRGB() + defaultBg.getAlpha();
       }
       if(lowerBounds) {
         hashValue += "<:>lower bounds";
         if(lowBoundPos != null) {
           hashValue += "<:>pos=" + lowBoundPos;
         }
       }
     }
     String hash = "";
    if("".equals(hashValue)) {
       MessageDigest md = MessageDigest.getInstance("MD5");
       md.update(hashValue.getBytes());
       byte[] digest = md.digest();
       for(int i = 0; i < digest.length; i++){
         hash += Integer.toHexString(Math.abs((int)digest[i]));
       }
     }
     return hash;
   }
 
   /**
    * @return the type of the image, as an integer code, used in the generation
    *  of the key of the image cache
    */
   public static int getType(String mimeType)
   {
     for (SupportedFormat f : SupportedFormat.values()) {
       if (f.getMimeType().equals(mimeType)) {
         return f.getCode();
       }
     }
     return 0;
   }
 
   public void flushCache() {
     if ((initializedCache) && (imageCache != null)) {
       imageCache.removeAll();
     }
     imageCache = null;
   }
 
   private XWikiContext getContext() {
     return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
         "xwikicontext");
   }
 
   private CacheManager getCacheManager() {
     return Utils.getComponent(CacheManager.class);
   }
 
 }
