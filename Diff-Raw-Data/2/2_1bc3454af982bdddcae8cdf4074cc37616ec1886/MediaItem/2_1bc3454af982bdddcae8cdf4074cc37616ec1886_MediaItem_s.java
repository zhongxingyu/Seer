 /*
  *  Copyright (C) 2012 Simon Robinson
  * 
  *  This file is part of Com-Me.
  * 
  *  Com-Me is free software; you can redistribute it and/or modify it 
  *  under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation; either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  Com-Me is distributed in the hope that it will be useful, but WITHOUT 
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
  *  Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with Com-Me.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ac.robinson.mediatablet.provider;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import ac.robinson.mediatablet.MediaTablet;
 import ac.robinson.mediatablet.R;
 import ac.robinson.mediautilities.FrameMediaContainer;
 import ac.robinson.mediautilities.MediaUtilities;
 import ac.robinson.mediautilities.SMILUtilities;
 import ac.robinson.util.BitmapUtilities;
 import ac.robinson.util.BitmapUtilities.CacheTypeContainer;
 import ac.robinson.util.IOUtilities;
 import ac.robinson.util.ImageCacheUtilities;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.media.ThumbnailUtils;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.text.TextUtils;
 import android.util.TypedValue;
 
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
 public class MediaItem implements BaseColumns {
 
 	public static final Uri CONTENT_URI = Uri.parse(MediaTabletProvider.URI_PREFIX + MediaTabletProvider.URI_AUTHORITY
 			+ MediaTabletProvider.URI_SEPARATOR + MediaTabletProvider.MEDIA_LOCATION);
 
 	public static final String[] PROJECTION_ALL = new String[] { MediaItem._ID, MediaItem.INTERNAL_ID,
 			MediaItem.PARENT_ID, MediaItem.DATE_CREATED, MediaItem.FILE_EXTENSION, MediaItem.MEDIA_EXTRA,
 			MediaItem.TYPE, MediaItem.VISIBILITY, MediaItem.DELETED };
 
 	public static final String INTERNAL_ID = "internal_id";
 	public static final String PARENT_ID = "parent_id";
 	public static final String DATE_CREATED = "date_created";
 	public static final String FILE_EXTENSION = "file_name";
 	public static final String MEDIA_EXTRA = "media_extra";
 	public static final String TYPE = "type";
 	public static final String VISIBILITY = "visibility";
 	public static final String DELETED = "deleted";
 
 	public static final int MEDIA_PRIVATE = 0;
 	public static final int MEDIA_PUBLIC = 1;
 
 	// added to filenames to distinguish between types
 	public static final String ICON_PUBLIC = "-p";
 
 	public static final String DEFAULT_SORT_ORDER = DATE_CREATED + " DESC";
 
 	private String mInternalId;
 	private String mParentId;
 	private long mCreationDate;
 	private String mFileExtension;
 	private String mMediaExtra;
 	private int mType;
 	private int mVisibility;
 	private int mDeleted;
 
 	public MediaItem(String internalId, String parentId, String originalFileName, int type, int visibility) {
 		mInternalId = internalId;
 		mParentId = parentId;
 		mCreationDate = System.currentTimeMillis();
 		setFileExtension(IOUtilities.getFileExtension(originalFileName));
 		setOriginalFileName(originalFileName);
 		mType = type;
 		mVisibility = visibility;
 		mDeleted = 0;
 	}
 
 	public MediaItem(String parentId, String originalFileName, int visibility) {
 		this(MediaTabletProvider.getNewInternalId(), parentId, originalFileName, MediaItem
 				.getMediaTypeFromFileName(originalFileName), visibility);
 	}
 
 	public MediaItem() {
 		this(null, null, MEDIA_PUBLIC);
 	}
 
 	public String getInternalId() {
 		return mInternalId;
 	}
 
 	public String getParentId() {
 		return mParentId;
 	}
 
 	public void setParentId(String parentId) {
 		mParentId = parentId;
 	}
 
 	public long getCreationDate() {
 		return mCreationDate;
 	}
 
 	public String getFileExtension() {
 		return mFileExtension;
 	}
 
 	public void setFileExtension(String fileExtension) {
 		mFileExtension = (fileExtension != null ? fileExtension.toLowerCase(Locale.ENGLISH) : null);
 	}
 
 	public String getOriginalFileName() {
 		return mMediaExtra;
 	}
 
 	public void setOriginalFileName(String mediaExtra) {
 		mMediaExtra = mediaExtra;
 	}
 
 	public void setTextExtra(String textExtra) {
 		mMediaExtra = textExtra;
 	}
 
 	public int getType() {
 		return mType;
 	}
 
 	public boolean isPubliclyShared() {
 		return (mVisibility == MEDIA_PUBLIC);
 	}
 
 	public void setPubliclySharedStatus(int sharedStatus) {
 		mVisibility = sharedStatus;
 	}
 
 	public File getFile() {
 		return getFile(mParentId, mInternalId, mFileExtension);
 	}
 
 	public static File getFile(String mediaParentId, String mediaInternalId, String mediaFileExtension) {
 		final File filePath = new File(PersonItem.getStorageDirectory(mediaParentId), mediaInternalId + "."
 				+ mediaFileExtension);
 		return filePath;
 	}
 
 	public static String getInternalId(String mediaFilePath) {
 		return IOUtilities.removeExtension(mediaFilePath);
 	}
 
 	public boolean getDeleted() {
 		return mDeleted == 0 ? false : true;
 	}
 
 	public void setDeleted(boolean deleted) {
 		mDeleted = deleted ? 1 : 0;
 	}
 
 	public String getCacheId(int visibility) {
 		return getCacheId(mInternalId, visibility);
 	}
 
 	public static String getCacheId(String internalId, int visibility) {
 		return (visibility == MEDIA_PRIVATE ? internalId : internalId + ICON_PUBLIC);
 	}
 
 	public static int getMediaTypeFromFileName(String fileName) {
 		if (fileName == null) {
 			return MediaTabletProvider.TYPE_UNKNOWN;
 		}
 		fileName = fileName.toLowerCase(Locale.ENGLISH);
 		if (fileName.endsWith(MediaUtilities.SYNC_FILE_EXTENSION)) { // note use of sync, rather than smil
 			return MediaTabletProvider.TYPE_NARRATIVE;
 		}
 		for (String extension : MediaTablet.TYPE_IMAGE_EXTENSIONS) {
 			if (fileName.endsWith(extension)) {
 				return MediaTabletProvider.TYPE_IMAGE_BACK;
 			}
 		}
 		for (String extension : MediaTablet.TYPE_VIDEO_EXTENSIONS) {
 			if (fileName.endsWith(extension)) {
 				return MediaTabletProvider.TYPE_VIDEO;
 			}
 		}
 		for (String extension : MediaTablet.TYPE_AUDIO_EXTENSIONS) {
 			if (fileName.endsWith(extension)) {
 				return MediaTabletProvider.TYPE_AUDIO;
 			}
 		}
 		for (String extension : MediaTablet.TYPE_TEXT_EXTENSIONS) {
 			if (fileName.endsWith(extension)) {
 				return MediaTabletProvider.TYPE_TEXT;
 			}
 		}
 		return MediaTabletProvider.TYPE_UNKNOWN;
 	}
 
 	/**
 	 * contentResolver may be null if parentOverlayId is null
 	 * 
 	 * @param resources
 	 * @param cacheTypeContainer
 	 * @param contentResolver
 	 * @param parentOverlayId
 	 * @return
 	 */
 	public Bitmap loadIcon(Resources resources, CacheTypeContainer cacheTypeContainer, ContentResolver contentResolver,
 			String parentOverlayId) {
 
 		int iconWidth = resources.getDimensionPixelSize(R.dimen.media_icon_width);
 		int iconHeight = resources.getDimensionPixelSize(R.dimen.media_icon_height);
 		Bitmap iconBitmap;
 		Bitmap mediaBitmap = Bitmap.createBitmap(iconWidth, iconHeight,
 				ImageCacheUtilities.mBitmapFactoryOptions.inPreferredConfig);
 		Canvas mediaCanvas = new Canvas(mediaBitmap);
 		Paint mediaPaint = BitmapUtilities.getPaint(Color.BLACK, 1);
 		mediaCanvas.drawColor(resources.getColor(R.color.icon_background));
 
 		boolean noMediaIcon = false;
 		if (getFile() != null && getFile().exists()) {
 			String mediaPath = getFile().getAbsolutePath();
 			switch (mType) {
 				case MediaTabletProvider.TYPE_IMAGE_BACK:
 				case MediaTabletProvider.TYPE_IMAGE_FRONT:
 					iconBitmap = BitmapUtilities.loadAndCreateScaledBitmap(mediaPath, iconWidth, iconHeight,
 							BitmapUtilities.ScalingLogic.CROP, true);
 					if (iconBitmap != null) {
 						mediaCanvas.drawBitmap(iconBitmap, (iconWidth - iconBitmap.getWidth()) / 2,
 								(iconHeight - iconBitmap.getHeight()) / 2, mediaPaint);
 					} else {
 						noMediaIcon = true;
 					}
 					break;
 
 				case MediaTabletProvider.TYPE_VIDEO:
 					// MINI_KIND: 512 x 384 thumbnail MICRO_KIND: 96 x 96 thumbnail
 					// TODO: this will never work if the video is in a non-public location...
 					iconBitmap = ThumbnailUtils.createVideoThumbnail(mediaPath, MediaStore.Video.Thumbnails.MINI_KIND);
 					if (iconBitmap != null) {
 						BitmapUtilities.scaleBitmap(iconBitmap, iconWidth, iconHeight,
 								BitmapUtilities.ScalingLogic.CROP);
 						mediaCanvas.drawBitmap(iconBitmap, (iconWidth - iconBitmap.getWidth()) / 2,
 								(iconHeight - iconBitmap.getHeight()) / 2, mediaPaint);
 					} else {
 						noMediaIcon = true;
 					}
 					break;
 
 				case MediaTabletProvider.TYPE_AUDIO:
 					noMediaIcon = true; // just show the icon
 					break;
 
 				case MediaTabletProvider.TYPE_TEXT:
 					BitmapUtilities.drawScaledText(mMediaExtra, mediaCanvas, mediaPaint,
 							resources.getColor(R.color.icon_text_no_image), 0,
 							resources.getDimensionPixelSize(R.dimen.icon_text_padding), 0, false, 0, false, iconHeight,
 							resources.getDimensionPixelSize(R.dimen.icon_maximum_text_size),
 							resources.getInteger(R.integer.icon_maximum_text_characters_per_line));
 					break;
 
 				case MediaTabletProvider.TYPE_NARRATIVE:
 					// draw the first frame of the narrative, or story icon if not present
 					// add audio overlay if present
 					ArrayList<FrameMediaContainer> narrativeFirstFrame = SMILUtilities.getSMILFrameList(getFile(), 1,
 							false, 1, false);
 					TypedValue resourceValue = new TypedValue();
					if (narrativeFirstFrame.size() == 1) {
 						FrameMediaContainer firstFrame = narrativeFirstFrame.get(0);
 						boolean hasImage = false;
 						if (firstFrame.mImagePath != null) {
 							iconBitmap = BitmapUtilities.loadAndCreateScaledBitmap(firstFrame.mImagePath, iconWidth,
 									iconHeight, BitmapUtilities.ScalingLogic.CROP, true);
 							if (iconBitmap != null) {
 								mediaCanvas.drawBitmap(iconBitmap, (iconWidth - iconBitmap.getWidth()) / 2,
 										(iconHeight - iconBitmap.getHeight()) / 2, mediaPaint);
 								hasImage = true;
 							}
 						}
 
 						boolean hasText = !TextUtils.isEmpty(firstFrame.mTextContent);
 						if (hasText) {
 							int textSpacing = resources.getDimensionPixelSize(R.dimen.icon_text_padding);
 							int textCornerRadius = resources.getDimensionPixelSize(R.dimen.icon_text_corner_radius);
 							BitmapUtilities.drawScaledText(
 									firstFrame.mTextContent,
 									mediaCanvas,
 									mediaPaint,
 									(hasImage ? resources.getColor(R.color.icon_text_with_image) : resources
 											.getColor(R.color.icon_text_no_image)),
 									(hasImage ? resources.getColor(R.color.icon_text_background) : 0),
 									resources.getDimensionPixelSize(R.dimen.icon_text_padding),
 									textCornerRadius,
 									hasImage,
 									0,
 									false,
 									(hasImage ? resources
 											.getDimensionPixelSize(R.dimen.icon_maximum_text_height_with_image)
 											- textSpacing : iconHeight - textSpacing), resources
 											.getDimensionPixelSize(R.dimen.icon_maximum_text_size), resources
 											.getInteger(R.integer.icon_maximum_text_characters_per_line));
 						}
 
 						boolean hasAudio = firstFrame.mAudioPaths.size() > 0;
 						resources.getValue(R.attr.icon_overlay_spacing_factor, resourceValue, true);
 						float spacingFactor = resourceValue.getFloat();
 						int iconSpacingHorizontal = Math.round(iconWidth * spacingFactor);
 						int iconSpacingVertical = Math.round(iconHeight * spacingFactor);
 						resources.getValue(R.attr.icon_overlay_svg_scale_factor, resourceValue, true);
 						float scaleFactor = resourceValue.getFloat();
 						Rect drawRect = null;
 						if (hasAudio) {
 							int resourceId = 0;
 							if (!(hasImage || hasText)) {
 								drawRect = new Rect(0, 0, iconWidth, iconHeight);
 								resourceId = R.raw.ic_audio_playback;
 							} else {
 								resourceId = R.raw.overlay_audio;
 								drawRect = new Rect(iconWidth - Math.round(iconWidth * scaleFactor)
 										- iconSpacingHorizontal, iconHeight - Math.round(iconHeight * scaleFactor)
 										- iconSpacingVertical, iconWidth - iconSpacingHorizontal, iconHeight
 										- iconSpacingVertical);
 							}
 							// using SVG so that we don't need resolution-specific icons
 							SVG audioSVG = SVGParser.getSVGFromResource(resources, resourceId);
 							mediaCanvas.drawPicture(audioSVG.getPicture(), drawRect);
 						}
 
 						if (hasImage || hasAudio || hasText) {
 							drawRect = new Rect(
 									iconWidth - Math.round(iconWidth * scaleFactor) - iconSpacingHorizontal,
 									iconSpacingVertical, iconWidth - iconSpacingHorizontal, iconSpacingVertical
 											+ Math.round(iconHeight * scaleFactor));
 							// using SVG so that we don't need resolution-specific icons
 							SVG narrativeSVG = SVGParser.getSVGFromResource(resources, R.raw.ic_narrative);
 							mediaCanvas.drawPicture(narrativeSVG.getPicture(), drawRect);
 						} else {
 							noMediaIcon = true;
 						}
 					} else {
 						noMediaIcon = true;
 					}
 					break;
 
 				case MediaTabletProvider.TYPE_UNKNOWN:
 				default:
 					noMediaIcon = true;
 					break;
 			}
 		}
 
 		if (noMediaIcon) {
 			int iconId = R.raw.ic_unknown_media;
 			switch (mType) {
 				case MediaTabletProvider.TYPE_IMAGE_BACK:
 				case MediaTabletProvider.TYPE_IMAGE_FRONT:
 					iconId = R.raw.ic_unknown_image;
 					break;
 				case MediaTabletProvider.TYPE_AUDIO:
 					iconId = R.raw.ic_unknown_audio;
 					break;
 				case MediaTabletProvider.TYPE_VIDEO:
 					iconId = R.raw.ic_unknown_video;
 					break;
 				case MediaTabletProvider.TYPE_NARRATIVE:
 					iconId = R.raw.ic_narrative;
 					break;
 				case MediaTabletProvider.TYPE_TEXT:
 				case MediaTabletProvider.TYPE_UNKNOWN:
 				default:
 					break;
 			}
 
 			// using SVG so that we don't need resolution-specific icons
 			Rect drawRect = new Rect(0, 0, iconWidth, iconHeight);
 			SVG noMediaSVG = SVGParser.getSVGFromResource(resources, iconId);
 			mediaCanvas.drawPicture(noMediaSVG.getPicture(), drawRect);
 
 			cacheTypeContainer.type = Bitmap.CompressFormat.PNG;
 		}
 
 		if ((noMediaIcon || mType == MediaTabletProvider.TYPE_AUDIO) && getOriginalFileName() != null) {
 			BitmapUtilities.drawScaledText(getOriginalFileName(), mediaCanvas, mediaPaint,
 					resources.getColor(R.color.icon_text_with_image), resources.getColor(R.color.icon_text_background),
 					resources.getDimensionPixelSize(R.dimen.icon_text_padding), 0, true, 0, false, iconHeight / 3,
 					resources.getDimensionPixelSize(R.dimen.icon_maximum_text_size),
 					resources.getInteger(R.integer.icon_maximum_text_characters_per_line));
 		}
 
 		// add the parent overlay if appropriate
 		if (parentOverlayId != null) {
 			TypedValue resourceValue = new TypedValue();
 			resources.getValue(R.attr.media_icon_person_size_factor, resourceValue, true);
 			float scaleFactor = resourceValue.getFloat();
 			int personWidth = Math.round(iconWidth * scaleFactor);
 			int personHeight = Math.round(iconHeight * scaleFactor);
 
 			File personFile = new File(MediaTablet.DIRECTORY_THUMBS, PersonItem.getCacheId(parentOverlayId));
 			if (!personFile.exists() && MediaTablet.DIRECTORY_THUMBS != null) {
 				// create unknown person icon for unknown public media (bad place, but not many other places are better)
 				if (PersonItem.UNKNOWN_PERSON_ID.equals(parentOverlayId)) {
 					Bitmap personBitmap = Bitmap.createBitmap(personWidth, personHeight,
 							ImageCacheUtilities.mBitmapFactoryOptions.inPreferredConfig);
 					Canvas personCanvas = new Canvas(personBitmap);
 					personCanvas.drawColor(resources.getColor(R.color.icon_background));
 
 					Rect drawRect = new Rect(0, 0, personWidth, personHeight);
 					SVG personSVG = SVGParser.getSVGFromResource(resources, PersonItem.UNKNOWN_PERSON_ICON);
 					personCanvas.drawPicture(personSVG.getPicture(), drawRect);
 
 					BitmapUtilities.saveBitmap(personBitmap, Bitmap.CompressFormat.PNG, 100, personFile);
 					personCanvas = null;
 				} else {
 					PersonManager.reloadPersonIcon(resources, contentResolver, parentOverlayId);
 				}
 			}
 
 			if (personFile.exists()) {
 				resources.getValue(R.attr.icon_overlay_spacing_factor, resourceValue, true);
 				float spacingFactor = resourceValue.getFloat();
 				int iconSpacingLeft = Math.round(iconWidth * spacingFactor);
 				int iconSpacingTop = Math.round(iconHeight * spacingFactor);
 				Bitmap personBitmap = BitmapUtilities.loadAndCreateScaledBitmap(personFile.getAbsolutePath(),
 						personWidth, personHeight, BitmapUtilities.ScalingLogic.CROP, true);
 				if (personBitmap != null) {
 					Rect drawRect = new Rect(iconSpacingLeft, iconSpacingTop, iconSpacingLeft + personWidth,
 							iconSpacingTop + personHeight);
 					mediaCanvas.drawBitmap(personBitmap, null, drawRect, mediaPaint);
 					mediaPaint.setColor(resources.getColor(R.color.icon_person_border));
 					mediaPaint.setStyle(Paint.Style.STROKE);
 					mediaCanvas.drawRect(drawRect, mediaPaint);
 				}
 			}
 		}
 
 		return mediaBitmap;
 	}
 
 	public static Bitmap loadTemporaryIcon(Resources res, boolean addBorder) {
 		int iconWidth = res.getDimensionPixelSize(R.dimen.media_icon_width);
 		int iconHeight = res.getDimensionPixelSize(R.dimen.media_icon_height);
 		Bitmap tempBitmap = Bitmap.createBitmap(iconWidth, iconHeight,
 				ImageCacheUtilities.mBitmapFactoryOptions.inPreferredConfig);
 		if (addBorder) {
 			int borderWidth = res.getDimensionPixelSize(R.dimen.icon_border_width);
 			Canvas tempBitmapCanvas = new Canvas(tempBitmap);
 			Paint tempBitmapPaint = BitmapUtilities.getPaint(0, 1);
 			tempBitmapCanvas.drawColor(res.getColor(R.color.icon_background));
 			BitmapUtilities
 					.addBorder(tempBitmapCanvas, tempBitmapPaint, borderWidth, res.getColor(R.color.icon_border));
 		} else {
 			tempBitmap.eraseColor(res.getColor(R.color.icon_background));
 		}
 		return tempBitmap;
 	}
 
 	public ContentValues getContentValues() {
 		final ContentValues values = new ContentValues();
 		values.put(INTERNAL_ID, mInternalId);
 		values.put(PARENT_ID, mParentId);
 		values.put(DATE_CREATED, mCreationDate);
 		values.put(FILE_EXTENSION, mFileExtension);
 		values.put(MEDIA_EXTRA, mMediaExtra);
 		values.put(TYPE, mType);
 		values.put(VISIBILITY, mVisibility);
 		values.put(DELETED, mDeleted);
 		return values;
 	}
 
 	public static MediaItem fromExisting(MediaItem existing, String newInternalId, String newParentId,
 			long newCreationDate) {
 		final MediaItem media = new MediaItem();
 		media.mInternalId = newInternalId;
 		media.mParentId = newParentId;
 		media.mCreationDate = newCreationDate;
 		media.mFileExtension = existing.mFileExtension;
 		media.mMediaExtra = existing.mMediaExtra;
 		media.mType = existing.mType;
 		media.mVisibility = existing.mVisibility;
 		media.mDeleted = existing.mDeleted;
 		return media;
 	}
 
 	public static MediaItem fromCursor(Cursor c) {
 		final MediaItem media = new MediaItem();
 		media.mInternalId = c.getString(c.getColumnIndexOrThrow(INTERNAL_ID));
 		media.mParentId = c.getString(c.getColumnIndexOrThrow(PARENT_ID));
 		media.mCreationDate = c.getLong(c.getColumnIndexOrThrow(DATE_CREATED));
 		media.mFileExtension = c.getString(c.getColumnIndexOrThrow(FILE_EXTENSION));
 		media.mMediaExtra = c.getString(c.getColumnIndexOrThrow(MEDIA_EXTRA));
 		media.mType = c.getInt(c.getColumnIndexOrThrow(TYPE));
 		media.mVisibility = c.getInt(c.getColumnIndexOrThrow(VISIBILITY));
 		media.mDeleted = c.getInt(c.getColumnIndexOrThrow(DELETED));
 		return media;
 	}
 
 	@Override
 	public String toString() {
 		return "MediaItem[" + mInternalId + "," + mParentId + "," + mCreationDate + "," + mFileExtension + ","
 				+ mMediaExtra + "," + mType + "," + mVisibility + "," + mDeleted + "]";
 	}
 }
