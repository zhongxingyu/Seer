 /*
  * Copyright (c) 2012, Dienst Landelijk Gebied - Ministerie van Economische Zaken
  * 
  * Gepubliceerd onder de BSD 2-clause licentie, 
  * zie https://github.com/MinELenI/CBSviewer/blob/master/LICENSE.md voor de volledige licentie.
  */
 package nl.mineleni.cbsviewer.servlet.wms.cache;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.imageio.ImageIO;
 
 import org.opengis.geometry.BoundingBox;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Cache voor wms getmap image responses.
  * 
  * @author prinsmc
  */
 public class WMSCache extends Cache<BoundingBox, CacheImage, BufferedImage> {
 	/** logger. */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(WMSCache.class);
 
 	/** cache locatie/pad. */
 	private String cacheDir;
 
 	/**
 	 * constructor met pad voor de cache en initiële cache data.
 	 * 
 	 * @param cacheData
 	 *            cache data
 	 * @param cacheDir
 	 *            pad voor de cache
 	 * @param maxSize
 	 *            the max size
 	 * @param secondsToLive
 	 *            the seconds to live
 	 * @throws IOException
 	 *             als de gevraagde directory niet schrijfbaar is.
	 * @see WMSCache#WMSCache(String, int)
 	 */
 	public WMSCache(final Map<BoundingBox, BufferedImage> cacheData,
 			final String cacheDir, final int maxSize, final int secondsToLive)
 			throws IOException {
 		this(cacheDir, maxSize);
 		for (final Entry<BoundingBox, BufferedImage> e : cacheData.entrySet()) {
 			this.put(e.getKey(), new CacheImage(e.getValue(), secondsToLive));
 		}
 	}
 
 	/**
 	 * constructor met pad voor de cache.
 	 * 
 	 * @param cacheDir
 	 *            pad voor de cache
 	 * @param maxSize
 	 *            the max size
 	 * @throws IOException
 	 *             als de gevraagde directory niet schrijfbaar is.
 	 */
 	public WMSCache(final String cacheDir, final int maxSize)
 			throws IOException {
 		super(maxSize);
 
 		final File f = new File(cacheDir);
 		final boolean createdDirs = f.mkdirs();
 		if (createdDirs && LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Directory tree aangemaakt voor "
 					+ f.getCanonicalPath());
 		}
 		if (f.isDirectory() && f.canWrite()) {
 			LOGGER.debug("Cache directory is: " + f.getCanonicalPath());
 			this.cacheDir = cacheDir;
 		} else {
 			LOGGER.debug("Cache directory: " + f.getCanonicalPath()
 					+ " is niet geldig.");
 			throw new IOException("De gevraagde cache directory (" + cacheDir
 					+ ") is niet schrijfbaar.");
 		}
 	}
 
 	/**
 	 * Geeft het pad van de cache.
 	 * 
 	 * @return de cacheDir
 	 */
 	public String getCacheDir() {
 		return this.cacheDir;
 	}
 
 	/**
 	 * plaatst de image in de cache met de sleutel.
 	 * 
 	 * @param bbox
 	 *            de sleutel
 	 * @param cacheValue
 	 *            het object dat wordt opgeslagen in de cache
 	 * @param secondsToLive
 	 *            the seconds to live
	 * 
	 * @see nl.mineleni.cbsviewer.servlet.wms.cache.Caching#put(Object,
	 *      Cachable)
 	 */
 	public void put(final BoundingBox bbox, final BufferedImage cacheValue,
 			final long secondsToLive) {
 
 		final CacheImage c = new CacheImage(cacheValue, secondsToLive);
 		// bestand opslaan/maken
 		try {
 			final File temp = File.createTempFile("wmscache", ".png", new File(
 					this.cacheDir));
 			temp.deleteOnExit();
 			c.setFileName(temp.getCanonicalPath());
 			ImageIO.write(cacheValue, "png", temp);
 			LOGGER.debug("Opslaan in cache: " + bbox + ", pad:"
 					+ temp.getCanonicalPath());
 		} catch (final IOException e) {
 			LOGGER.error("Cache image opslaan is niet gelukt.", e);
 		}
 		super.put(bbox, c);
 	}
 
 	/**
 	 * Verwijder het gevraagde object uit de cache.
 	 * 
 	 * @param bbox
 	 *            de sleutel
 	 */
 	@Override
 	public void remove(final BoundingBox bbox) {
 		super.remove(bbox);
 		final CachableImage<BufferedImage> entry = this.get(bbox);
 		if ((entry != null) && !(new File(entry.getName())).delete()) {
 			LOGGER.warn("Het cachebestand kon niet worden verwijderd.");
 		}
 	}
 
 	/**
 	 * Haalt een opgeslagen afbeelding op.
 	 * 
 	 * @param bbox
 	 *            de sleutel
 	 * @return de opgelagen afbeelding of null
 	 */
 	public BufferedImage getImage(final BoundingBox bbox) {
 		if (this.get(bbox) == null) {
 			return null;
 		}
 		return this.get(bbox).getImage();
 	}
 }
