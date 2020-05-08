 package com.innovatrics.iseglib;
 
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.ptr.IntByReference;
 
 /**
  * Contains bindings for the libiseglib.so library.
  * @author Martin Vysny
  */
 public class SegLib {
 
     public static final int INTENSITY_THRESHOLD_TOO_DARK = 85;
     public static final int INTENSITY_THRESHOLD_TOO_LIGHT = 15;
     /**
      * Summary: Defines minimal width of accepted fingerprint images
      */
     public static final int ISEGLIB_MIN_IMAGE_WIDTH = 90;
     /**
      * Summary: Defines maximal width of accepted fingerprint images
      */
     public static final int ISEGLIB_MAX_IMAGE_WIDTH = 1800;
     /**
      * Summary: Defines minimal height of accepted fingerprint images
      */
     public static final int ISEGLIB_MIN_IMAGE_HEIGHT = 90;
     /**
      * Summary: Defines maximal height of accepted fingerprint images
      */
     public static final int ISEGLIB_MAX_IMAGE_HEIGHT = 1800;
 
     static interface SegLibNative extends Library {
 
         final SegLibNative INSTANCE = (SegLibNative) Native.loadLibrary("iseglib", SegLibNative.class); // NOI18N
         public static final int SEGMENTATION_INFO_MISSING_FINGER_UNKNOWN = 1;
         public static final int SEGMENTATION_INFO_MISSING_FINGER1 = 2;
         public static final int SEGMENTATION_INFO_MISSING_FINGER2 = 4;
         public static final int SEGMENTATION_INFO_MISSING_FINGER3 = 8;
         public static final int SEGMENTATION_INFO_MISSING_FINGER4 = 16;
         public static final int SEGMENTATION_INFO_LEFT_HAND = 1024;
         public static final int SEGMENTATION_INFO_RIGHT_HAND = 2048;
         public static final int ISEGLIB_E_NOERROR = 0;
 
         int ISegLib_Init();
 
         int ISegLib_SetLicenseContent(byte[] licenseContent, int length);
 
         int ISegLib_Terminate();
 
         int ISegLib_GetVersion(IntByReference major, IntByReference minor);
 
         int ISegLib_NFIQScore(int width, int height, int imageResolution, final byte[] rawImage, IntByReference nfiqScore);
 
         int ISegLib_GetImageQuality(int width, int height, int imageResolution, final byte[] rawImage, IntByReference quality);
 
         int ISegLib_GetImageQualityInfo(int width, int height, int imageResolution, final byte[] rawImage, byte[] colorQualityBmpImage, IntByReference length, IntByReference activePixelsCount);
 
         int ISegLib_SegmentFingerprints(int width, int height, int imageResolution, final byte[] rawImage, int expectedFingersCount, int minFingersCount, int maxFingersCount, int maxRotation, int options, IntByReference segmentedFingersCount, IntByReference globalAngle, int[] roundingBoxes, byte[] boxedBmpImage, IntByReference boxedBmpImageLength, int outWidth, int outHeight, byte[] rawImage1, byte[] rawImage2, byte[] rawImage3, byte[] rawImage4, byte bcgValue, IntByReference feedback);
 
         int ISegLib_ManualSegmentation(int width, int height, byte[] rawImage, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, int outWidth, int outHeight, byte[] outRawImage, byte bcgValue);
 
         int ISegLib_GetImageIntensity(int width, int height, byte[] rawImage, IntByReference intensity);
 
         int ISegLib_ConvertToRaw(final byte[] imageData, int imageLength, int/*ISEGLIB_IMAGE_FORMAT*/ imageFormat, IntByReference width, IntByReference height, byte[] rawImage, IntByReference rawImageLength);
 
         int ISegLib_ConvertRawToImage(final byte[] rawImage, int width, int height, byte[] outImage, int /*ISEGLIB_IMAGE_FORMAT*/ imageFormat, int bitrate, IntByReference length);
 
         String ISegLib_GetErrorMessage(int errcode);
     }
 
     private static void check(int result) {
         if (result != SegLibNative.ISEGLIB_E_NOERROR) {
             throw new SegLibException(SegLibNative.INSTANCE.ISegLib_GetErrorMessage(result), result);
         }
     }
 
     /**
      *	Initializes the library.<p/>This function initializes and checks the integrity of the library and verifies the validity
      *	of the license. It should be called prior to any other function from the library.
      */
     public void init() {
         check(SegLibNative.INSTANCE.ISegLib_Init());
     }
 
     /**
      * Sets license data for further license check.<p/>
      * This function sets the license data for license check done when calling ISegLib_Init.
      * This function helps to avoid the usage of license files. It is meant to protect the license file content.
      * It has to be called before ISegLib_Init function.
      * @param licenseContent license file content
      * @param length Size of license data
      */
     public void setLicenseContent(byte[] licenseContent, int length) {
         check(SegLibNative.INSTANCE.ISegLib_SetLicenseContent(licenseContent, length));
     }
 
     /**
      * Terminates the use of the library.<p/>
      * This function releases all resources allocated by the library.
      * It should be called as the very last function of the library.
      */
     public void terminate() {
         check(SegLibNative.INSTANCE.ISegLib_Terminate());
     }
 
     /**
      * Returns the library version.<p/>
      * This function returns the version number of the library
      * @return version, never null
      */
     public SegLibVersion getVersion() {
         final IntByReference major = new IntByReference();
         final IntByReference minor = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_GetVersion(major, minor));
         return new SegLibVersion(major.getValue(), minor.getValue());
     }
 
     /**
      * Returns NFIQ score (quality score defined by NIST) a single fingerprint image.<p/>
      * This function returns NFIQ score (quality score defined by NIST) a fingerprint image
      * quality of the input fingerprint image. Image quality number is calculated in accordance with the general guidelines contained in Section 2.1.42 of ANSI/INCITS 358 standard.
      * @param width The number of pixels indicating the width of the image
      * @param height The number of pixels indicating the height of the image
      * @param imageResolution Resolution (in DPI) of the input image. Typical resolution is 500 DPI.
      * @param rawImage Pointer to the uncompressed raw image
      * @return NFIQ score, the output range is from 1 (highest quality) to 5 (lowest quality)
      */
     public int nfiqScore(int width, int height, int imageResolution, final byte[] rawImage) {
         final IntByReference result = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_NFIQScore(width, height, imageResolution, rawImage, result));
         return result.getValue();
     }
 
     /**
      * Returns quality of a single fingerprint image.<p/>
      * This function returns quality of the input fingerprint image. Image quality number is calculated in accordance with the general guidelines contained in Section 2.1.42 of ANSI/INCITS 358 standard.
      * @param width The number of pixels indicating the width of the image
      * @param height The number of pixels indicating the height of the image
      * @param imageResolution Resolution (in DPI) of the input image. Typical resolution is 500 DPI.
      * @param rawImage Pointer to the uncompressed raw image
      * @return quality; the output range is from 0 (lowest quality) to 100 (highest quality)
      */
     public int getImageQuality(int width, int height, int imageResolution, final byte[] rawImage) {
         final IntByReference result = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_GetImageQuality(width, height, imageResolution, rawImage, result));
         return result.getValue();
     }
 
     /**
      *	Returns color image quality map and total number of active pixels in the image.<p/>
      * 	This function returns color quality map as bmp image of the input fingerprint image.
      *	It also retuns total number of active pixels (pixels located in high quality zone, not lying in the noisy background).
      *	Total active pixels count can be used in order to detect void/blank images.
      *	This function works for both slap images and single finger images.
      * @param	imageResolution Resolution (in DPI) of the input image. Typical resolution is 500 DPI.
      * @param	raw uncompressed raw image
      * @return image with quality.
      */
     public SegLibImage getImageQualityInfo(final RawImage raw, int imageResolution) {
         final IntByReference length = new IntByReference();
         final IntByReference activePixelsCount = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_GetImageQualityInfo(raw.width, raw.height, imageResolution, raw.rawImage, null, length, activePixelsCount));
         final SegLibImage result = new SegLibImage();
         result.colorQualityBmpImage = new byte[length.getValue()];
         check(SegLibNative.INSTANCE.ISegLib_GetImageQualityInfo(raw.width, raw.height, imageResolution, raw.rawImage, result.colorQualityBmpImage, length, activePixelsCount));
         result.activePixelsCount = activePixelsCount.getValue();
         return result;
     }
 
     /**
      *	Segments slap fingerprint image into individual prints.<p/>
      *	This function separates slap fingerprint image into individual prints, returns
      *	image with color boxes indicating positions of detected fingers, returns number of
      *	prints detected in the input image, returns information on missing digits (fingers),
      *	for 4 and 3 finger slap images, returns information about hand position (left/right hand).
      * @param	raw Pointer to the uncompressed raw image
      * @param	imageResolution Resolution (in DPI) of the input image. Typical resolution is 500 DPI.
      * @param	expectedFingersCount Number if fingers expected to be found in the input slap image. Valid range: 0..4
      * @param	minFingersCount Minimum number of fingers that has to be detected in the input slap image. If less fingers are detected, an error code is returned. Valid range: 0..4
      * @param	maxFingersCount Maximum number of fingers that has to be detected in the input slap image. If more fingers are detected, an error code is returned. Valid range: 0..4
      * @param	maxRotation Value indicating maximum rotation of fingers in the input slap image. Value is in degrees. Valid range: 0..45
      * @param	options Reserved for future use, should be set to 0.
      * @param	outWidth Indicates width of returned raw images of segmented fingers. If found segmented fingers are smaller, they will be centered in the middle and border will be set to bcgValue
      * @param	outHeight Indicates height of returned raw images of segmented fingers. If found segmented fingers are smaller, they will be centered in the middle and border will be set to bcgValue
      * @param	bcgValue Value used for background for returned segmented images if these images have smaller dimensions than outWith,outHeight
      * @return segmentation result, never null.
      */
     public SegmentationResult segmentFingerprints(final RawImage raw, int imageResolution, int expectedFingersCount, int minFingersCount, int maxFingersCount, int maxRotation, int options, int outWidth, int outHeight, byte bcgValue) {
         final SegmentationResult result = new SegmentationResult();
         final IntByReference globalAngle = new IntByReference();
         final IntByReference segmentedFingersCount = new IntByReference();
         final IntByReference feedback = new IntByReference();
         final int[] roundingBoxes = new int[8 * 4];
         final int outRawLength = outWidth * outHeight;
         final byte[] rawImage1 = new byte[outRawLength];
         final byte[] rawImage2 = new byte[outRawLength];
         final byte[] rawImage3 = new byte[outRawLength];
         final byte[] rawImage4 = new byte[outRawLength];
         final IntByReference boxedBmpImageLength = new IntByReference(getColorBmpLength(raw.width, raw.height));
         result.boxedBmpImage = new byte[boxedBmpImageLength.getValue()];
         check(SegLibNative.INSTANCE.ISegLib_SegmentFingerprints(raw.width, raw.height, imageResolution, raw.rawImage, expectedFingersCount, minFingersCount, maxFingersCount, maxRotation, options, segmentedFingersCount, globalAngle, roundingBoxes, result.boxedBmpImage, boxedBmpImageLength, outWidth, outHeight, rawImage1, rawImage2, rawImage3, rawImage4, bcgValue, feedback));
         if (boxedBmpImageLength.getValue() > result.boxedBmpImage.length) {
             // the array was not sufficient. This is not expected... the getColorBmpLength method should have computed the correct number...
             throw new AssertionError("the array was not sufficient: assumed that " + result.boxedBmpImage.length + " bytes would be enough but " + boxedBmpImageLength.getValue() + " was requested");
         }
         result.globalAngle = globalAngle.getValue();
         result.segmentedFingersCount = segmentedFingersCount.getValue();
         result.feedback = SegInfoEnum.fromFeedback(feedback.getValue());
         byte[][] rawImages = new byte[][]{rawImage1, rawImage2, rawImage3, rawImage4};
         result.fingerprints = new SegmentedFingerprint[result.segmentedFingersCount];
         for (int i = 0; i < result.segmentedFingersCount; i++) {
             final SegmentedFingerprint sf = new SegmentedFingerprint();
             result.fingerprints[i] = sf;
             sf.rawImage = rawImages[i];
             sf.roundingBox = Rect.from(roundingBoxes, i * 8);
         }
         return result;
     }
 
     private static int getColorBmpLength(int width, int height) {
         int offset = (width * 3) & 0x03;
         if (offset != 0) {
             offset = 4 - offset;
         }
         return 54 + (3 * width + offset) * height;
     }
 
     /**
      *	Performs manual segmentation, e.g. extracts specified rectangle from slap image.<p/>
      *	This function returns the content of the rectangle as manually specified by user. The content of this rectangle is returned as raw image.
      * @param	raw Pointer to the uncompressed raw image
      * @param	rect the rectangle
      * @param	outWidth Indicates width of returned raw image. If extracted image is smaller, it will be centered in the middle and border will be set to bcgValue
      * @param	outHeight Indicates height of returned raw image. If extracted image is smaller, it will be centered in the middle and border will be set to bcgValue
      * @param	bcgValue Value used for background for returned image if this image has smaller dimensions than outWith,outHeight
      */
     public byte[] manualSegmentation(final RawImage raw, final Rect rect, int outWidth, int outHeight, byte bcgValue) {
         final byte[] outRawImage = new byte[outWidth * outHeight];
         check(SegLibNative.INSTANCE.ISegLib_ManualSegmentation(raw.width, raw.height, raw.rawImage, rect.point1.x, rect.point1.y, rect.point2.x, rect.point2.y, rect.point3.x, rect.point3.y, rect.point4.x, rect.point4.y, outWidth, outHeight, outRawImage, bcgValue));
         return outRawImage;
     }
 
     /**
      *	Returns image intensity (darkness).<p/>
      *	This function analyzes fingerprint image contained in the input image and returns the intensity of the contained print(s).
      *	The intensity value is low for dry fingerprints (and for low pressure prints) and high for wet fingerprints (or for high pressure prints)
      *	This function can be either used with single finger images or with slap images.
      * @param	raw the uncompressed raw image
      * @return	intensity Indicates the intensity of the input image. Returned values are in the following range: 0..100.
      * 0 means lowest intensity (dry finger, low pressure), 100 means highest intensity (wet fnger, high pressure).
      */
     public int getImageIntensity(final RawImage raw) {
         final IntByReference intensity = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_GetImageIntensity(raw.width, raw.height, raw.rawImage, intensity));
         return intensity.getValue();
     }
 
     /**
      *	Reads formatted image from memory and converts it to raw 8-bit format.<p/>
      *	This function reads image encoded in a byte array and converts it
      *	to raw 8-bit format as described in paragraph <link Fingerprint Image Data>
      * @param	imageData the image in BMP format stored in the memory
      * @param	imageLength Indicates length of input image
      * @param	imageFormat Indicates format in which input image is encoded
      * @return raw image, never null.
      */
     public RawImage convertToRaw(final byte[] imageData, int imageLength, SegLibImageFormatEnum imageFormat) {
         final IntByReference length = new IntByReference();
         final IntByReference width = new IntByReference();
         final IntByReference height = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_ConvertToRaw(imageData, imageLength, imageFormat.cval, width, height, null, length));
         final byte[] raw = new byte[length.getValue()];
         check(SegLibNative.INSTANCE.ISegLib_ConvertToRaw(imageData, imageLength, imageFormat.cval, width, height, raw, length));
         return new RawImage(width.getValue(), height.getValue(), raw);
     }
 
     /**
      *	Converts raw 8-bit image into formatted image.<p/>
      *	This function reads raw 8-bit image and encodes it into specified image format
      * @param raw the raw image
      * @param    imageFormat Indicates image format in which the output image should be encoded
      * @param    bitrate Specifies compression rate for JPEG2000 and WSQ images. Ignored for other image formats.
      * @return input image will be encoded as specified and returned. See also values {@link #INTENSITY_THRESHOLD_TOO_DARK} and
      * {@link #INTENSITY_THRESHOLD_TOO_LIGHT} constants for recommended thresholds defining too dark and too light prints.
      */
     public byte[] convertRawToImage(final RawImage raw, SegLibImageFormatEnum imageFormat, int bitrate) {
         final IntByReference length = new IntByReference();
         check(SegLibNative.INSTANCE.ISegLib_ConvertRawToImage(raw.rawImage, raw.width, raw.height, null, imageFormat.cval, bitrate, length));
         final byte[] result = new byte[length.getValue()];
         check(SegLibNative.INSTANCE.ISegLib_ConvertRawToImage(raw.rawImage, raw.width, raw.height, result, imageFormat.cval, bitrate, length));
         return result;
     }
 }
