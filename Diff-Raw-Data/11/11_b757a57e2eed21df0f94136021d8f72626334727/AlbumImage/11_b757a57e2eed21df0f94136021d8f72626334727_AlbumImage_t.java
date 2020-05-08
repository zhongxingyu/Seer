 package ch.bergturbenthal.raoa.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.ref.SoftReference;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.Semaphore;
 
 import lombok.Cleanup;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang.ObjectUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import ch.bergturbenthal.raoa.server.cache.AlbumManager;
 import ch.bergturbenthal.raoa.server.metadata.MetadataWrapper;
 import ch.bergturbenthal.raoa.server.metadata.PicasaIniEntryData;
 import ch.bergturbenthal.raoa.server.metadata.XmpWrapper;
 import ch.bergturbenthal.raoa.server.model.AlbumEntryData;
 import ch.bergturbenthal.raoa.server.thumbnails.ImageThumbnailMaker;
 import ch.bergturbenthal.raoa.server.thumbnails.VideoThumbnailMaker;
 
 import com.adobe.xmp.XMPException;
 import com.adobe.xmp.XMPMeta;
 import com.adobe.xmp.XMPMetaFactory;
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.Metadata;
 
 public class AlbumImage {
 
 	private static interface XmpRunnable {
 		void run(final XmpWrapper xmp);
 	}
 
 	private static Map<File, Object> imageLocks = new WeakHashMap<File, Object>();
 	private static Semaphore limitConcurrentScaleSemaphore = new Semaphore(4);
 	private static Map<File, SoftReference<AlbumImage>> loadedImages = new HashMap<File, SoftReference<AlbumImage>>();
 	private final static Logger logger = LoggerFactory.getLogger(AlbumImage.class);
 
 	private static final Integer STAR_RATING = Integer.valueOf(5);
 
 	public static AlbumImage createImage(final File file, final File cacheDir, final Date lastModified, final AlbumManager cacheManager) {
 		synchronized (lockFor(file)) {
 			final SoftReference<AlbumImage> softReference = loadedImages.get(file);
 			if (softReference != null) {
 				final AlbumImage cachedImage = softReference.get();
 				if (cachedImage != null && cachedImage.lastModified.equals(lastModified)) {
 					return cachedImage;
 				}
 			}
 			final AlbumImage newImage = new AlbumImage(file, cacheDir, lastModified, cacheManager);
 			loadedImages.put(file, new SoftReference<AlbumImage>(newImage));
 			return newImage;
 		}
 	}
 
 	private static synchronized Object lockFor(final File file) {
 		final Object existingLock = imageLocks.get(file);
 		if (existingLock != null) {
 			return existingLock;
 		}
 		final Object newLock = new Object();
 		imageLocks.put(file, newLock);
 		return newLock;
 	}
 
 	private final AlbumManager albumManager;
 
 	private final File cacheDir;
 
 	private final Object entryDataLock = new Object();
 
 	private final File file;
 
 	@Autowired
 	private ImageThumbnailMaker imageThumbnailMaker;
 
 	private final Date lastModified;
 
 	@Autowired
 	private VideoThumbnailMaker videoThumbnailMaker;
 
 	private AlbumImage(final File file, final File cacheDir, final Date lastModified, final AlbumManager cacheManager) {
 		this.file = file;
 		this.cacheDir = cacheDir;
 		this.lastModified = lastModified;
 		this.albumManager = cacheManager;
 	}
 
 	public void addKeyword(final String keyword) {
 		updateXmp(new XmpRunnable() {
 
 			@Override
 			public void run(final XmpWrapper xmp) {
 				xmp.addKeyword(keyword);
 			}
 		});
 	}
 
 	public Date captureDate() {
 		return getAlbumEntryData().getCreationDate();
 	}
 
 	public AlbumEntryData getAlbumEntryData() {
 		synchronized (entryDataLock) {
 
 			AlbumEntryData loadedMetaData = albumManager.getCachedData();
 			final Date lastModifiedMetadata = getMetadataLastModifiedTime();
 			if (loadedMetaData != null && ObjectUtils.equals(loadedMetaData.getLastModifiedMetadata(), lastModifiedMetadata)) {
 				return loadedMetaData;
 			}
 
 			loadedMetaData = new AlbumEntryData();
 			loadedMetaData.setLastModifiedMetadata(lastModifiedMetadata);
 			final Metadata metadata = getExifMetadata();
 			if (metadata != null) {
 				new MetadataWrapper(metadata).fill(loadedMetaData);
 			}
 			final PicasaIniEntryData picasaData = albumManager.getPicasaData();
 			if (picasaData != null) {
 				if (loadedMetaData.getRating() == null && picasaData.isStar()) {
 					loadedMetaData.setRating(STAR_RATING);
 				}
 				loadedMetaData.getKeywords().addAll(picasaData.getKeywords());
 				if (loadedMetaData.getCaption() == null) {
 					loadedMetaData.setCaption(picasaData.getCaption());
 				}
 			}
 			final File xmpSideFile = getXmpSideFile();
 			if (xmpSideFile.exists()) {
 				try {
 					{
 						@Cleanup
 						final FileInputStream fis = new FileInputStream(xmpSideFile);
 						final XmpWrapper xmp = new XmpWrapper(XMPMetaFactory.parse(fis));
 						final Integer rating = xmp.readRating();
 						if (rating != null) {
 							loadedMetaData.setRating(rating);
 						}
 						loadedMetaData.getKeywords().addAll(xmp.readKeywords());
 						final String description = xmp.readDescription();
 						if (description != null) {
 							loadedMetaData.setCaption(description);
 						}
 					}
 					{
 						@Cleanup
 						final FileInputStream fis = new FileInputStream(xmpSideFile);
 						loadedMetaData.setEditableMetadataHash(DigestUtils.shaHex(fis));
 					}
 				} catch (final IOException e) {
 					logger.error("Cannot read XMP-Sidefile: " + xmpSideFile, e);
 				} catch (final XMPException e) {
 					logger.error("Cannot read XMP-Sidefile: " + xmpSideFile, e);
 				}
 			} else {
 				loadedMetaData.setEditableMetadataHash("original-data");
 			}
 
 			albumManager.updateCache(loadedMetaData);
 			return loadedMetaData;
 		}
 	}
 
 	public long getAllFilesSize() {
 		long totalSize = file.length();
 		final File thumbnailSize = makeCachedFile();
 		if (thumbnailSize.exists()) {
 			totalSize += thumbnailSize.length();
 		}
 		final File xmpSideFile = getXmpSideFile();
 		if (xmpSideFile.exists()) {
 			totalSize += xmpSideFile.length();
 		}
 		return totalSize;
 	}
 
 	public String getName() {
 		return file.getName();
 	}
 
 	public long getOriginalFileSize() {
 		return file.length();
 	}
 
 	public File getThumbnail() {
 		return getThumbnail(false);
 	}
 
 	public File getThumbnail(final boolean onlyFromCache) {
 		try {
 			final File cachedFile = makeCachedFile();
 			final long originalLastModified = file.lastModified();
 			if (cachedFile.exists() && cachedFile.lastModified() == originalLastModified) {
 				albumManager.clearThumbnailException(getName());
 				return cachedFile;
 			}
 			if (onlyFromCache) {
 				return null;
 			}
 			synchronized (this) {
 				if (cachedFile.exists() && cachedFile.lastModified() == originalLastModified) {
 					albumManager.clearThumbnailException(getName());
 					return cachedFile;
 				}
 				limitConcurrentScaleSemaphore.acquire();
 				try {
 					if (isVideo()) {
 						videoThumbnailMaker.makeVideoThumbnail(file, cachedFile, cacheDir);
 					} else {
 						imageThumbnailMaker.makeImageThumbnail(file, cachedFile, cacheDir);
 					}
 					cachedFile.setLastModified(originalLastModified);
 				} finally {
 					limitConcurrentScaleSemaphore.release();
 				}
 			}
 			albumManager.clearThumbnailException(getName());
 			return cachedFile;
 		} catch (final Exception e) {
 			albumManager.recordThumbnailException(getName(), e);
 			return null;
 		}
 	}
 
 	public File getXmpSideFile() {
		final String name = file.getName();
		final int lastPt = name.lastIndexOf('.');
		final String baseName;
		if (lastPt > 0) {
			baseName = name.substring(0, lastPt);
		} else {
			baseName = name;
		}

		return new File(file.getParent(), baseName + ".xmp");
 	}
 
 	/**
 	 * returns true if the image is a video
 	 * 
 	 * @return
 	 */
 	public boolean isVideo() {
 		return file.getName().toLowerCase().endsWith(".mkv");
 	}
 
 	public Date lastModified() {
 		if (getThumbnail(true) == null) {
 			return new Date(lastModified.getTime() - 1);
 		}
 		return lastModified;
 	}
 
 	public void removeKeyword(final String keyword) {
 		updateXmp(new XmpRunnable() {
 
 			@Override
 			public void run(final XmpWrapper xmp) {
 				xmp.removeKeyword(keyword);
 			}
 		});
 	}
 
 	public void setCaption(final String caption) {
 		updateXmp(new XmpRunnable() {
 
 			@Override
 			public void run(final XmpWrapper xmp) {
 				xmp.updateDescription(caption);
 			}
 		});
 	}
 
 	public void setRating(final Integer rating) {
 		updateXmp(new XmpRunnable() {
 			@Override
 			public void run(final XmpWrapper xmp) {
 				xmp.setRating(rating);
 			}
 		});
 	}
 
 	@Override
 	public String toString() {
 		return "AlbumImage [file=" + file.getName() + "]";
 	}
 
 	private Metadata getExifMetadata() {
 		try {
 			Metadata exifMetadata;
 			final long startTime = System.currentTimeMillis();
 			exifMetadata = ImageMetadataReader.readMetadata(file);
 			final long endTime = System.currentTimeMillis();
 			logger.info("Metadata-Read: " + (endTime - startTime) + " ms");
 			return exifMetadata;
 		} catch (final IOException e) {
 			logger.warn("Cannot reade metadata from " + file, e);
 		} catch (final ImageProcessingException e) {
 			logger.warn("Cannot reade metadata from " + file, e);
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	private Date getMetadataLastModifiedTime() {
 		final File xmpSideFile = getXmpSideFile();
 		if (!xmpSideFile.exists()) {
 			return null;
 		}
 		return new Date(xmpSideFile.lastModified());
 	}
 
 	private File makeCachedFile() {
 		final String name = file.getName();
 		if (isVideo()) {
 			return new File(cacheDir, name.substring(0, name.length() - 4) + ".mp4");
 		}
 		return new File(cacheDir, name);
 	}
 
 	private void updateXmp(final XmpRunnable runnable) {
 		final File xmpSideFile = getXmpSideFile();
 		final XMPMeta xmpMeta;
 		if (xmpSideFile.exists()) {
 			try {
 				@Cleanup
 				final FileInputStream fis = new FileInputStream(xmpSideFile);
 				xmpMeta = XMPMetaFactory.parse(fis);
 			} catch (final IOException e) {
 				throw new RuntimeException("Cannot parse " + xmpSideFile, e);
 			} catch (final XMPException e) {
 				throw new RuntimeException("Cannot parse " + xmpSideFile, e);
 			}
 		} else {
 			xmpMeta = XMPMetaFactory.create();
 		}
 		final XmpWrapper xmp = new XmpWrapper(xmpMeta);
 		runnable.run(xmp);
 		final File tempSideFile = new File(xmpSideFile.getParent(), xmpSideFile.getName() + "-tmp");
 		try {
 			{
 				@Cleanup
 				final FileOutputStream fos = new FileOutputStream(tempSideFile);
 				XMPMetaFactory.serialize(xmpMeta, fos);
 			}
 			tempSideFile.renameTo(xmpSideFile);
 		} catch (final IOException e) {
 			throw new RuntimeException("Cannot write " + tempSideFile, e);
 		} catch (final XMPException e) {
 			throw new RuntimeException("Cannot write " + tempSideFile, e);
 		}
 
 	}
 
 }
