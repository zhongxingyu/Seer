 package pt.inevo.encontra.extract.xmp;
 
 import com.adobe.xmp.XMPConst;
 
 /**
  * NOTE   A number of Exif 2.2 properties are not included in XMP. These are generally properties that relate directly to the
  * image stream, or that are of little use without access to the image stream. A general XMP principle is that XMP
  * metadata should have value in and of itself, separate from the primary file content. The omitted properties include:
  * StripOffsets, RowsPerStrip, StripByteCounts, JPEGInterchangeFormat, and JPEGInterchangeFormatLength
 * Properties beginning with "GPS" are GPS properties that are also used by DIG-35 and are part of the JPEG-2000
  * standard.
  * The namespace name is http://ns.adobe.com/exif/1.0/
  * The preferred namespace prefix is exif
  *
  * Note: MISSING THE GPS PROPERTIES.
  */
 public interface ExifXMPFields {
 
     /**
      * XMP Exif main namespace URI.
      */
     public static String NS_URI = XMPConst.NS_EXIF;
 
     /**
      * XMP Exif specific namespace prefix.
      */
     public static String NS_PREFIX = "exif" + XMPUtil.XMP_SEPARATOR;
 
     public static String APERTURE_VALUE = NS_PREFIX + "ApertureValue";
 
     public static String BRIGHTNESS_VALUE = NS_PREFIX + "BrightnessValue";
 
     public static String CFA_PATTERN = NS_PREFIX + "CFAPattern";
 
     public static String COLOR_SPACE = NS_PREFIX + "ColorSpace";
 
     public static String COMPONENTS_CONFIGURATION = NS_PREFIX + "ComponentsConfiguration";
 
     public static String COMPRESSED_BITS_PER_PIXEL = NS_PREFIX + "CompressedBitsPerPixel";
 
     public static String CONTRAST = NS_PREFIX + "Contrast";
 
     public static String CUSTOM_RENDERED = NS_PREFIX + "CustomRendered";
 
     public static String DATE_TIME_ORIGINAL = NS_PREFIX + "DateTimeOriginal";
 
     public static String DATE_TIME_DIGITIZED = NS_PREFIX + "DateTimeDigitized";
 
     public static String DEVICE_SETTING_DESCRIPTION = NS_PREFIX + "DeviceSettingDescription";
 
     public static String DIGITAL_ZOOM_RATIO = NS_PREFIX + "DigitalZoomRatio";
 
     public static String EXIF_VERSION = NS_PREFIX + "ExifVersion";
 
     public static String EXPOSURE_BIAS_VALUE = NS_PREFIX + "ExposureBiasValue";
 
     public static String EXPOSURE_INDEX = NS_PREFIX + "ExposureIndex";
 
     public static String EXPOSURE_MODE = NS_PREFIX + "ExposureMode";
 
     public static String EXPOSURE_PROGRAM = NS_PREFIX + "ExposureProgram";
 
     public static String EXPOSURE_TIME = NS_PREFIX + "ExposureTime";
 
     public static String FILE_SOURCE = NS_PREFIX + "FileSource";
 
     public static String FLASH = NS_PREFIX + "Flash";
 
     public static String FLASH_ENERGY = NS_PREFIX + "FlashEnergy";
 
     public static String FLASH_PIX_VERSION = NS_PREFIX + "FlashpixVersion";
 
     public static String F_NUMBER = NS_PREFIX + "FNumber";
 
     public static String FOCAL_LENGTH = NS_PREFIX + "FocalLength";
 
     public static String FOCAL_LENGTH_IN_35_MM = NS_PREFIX + "FocalLengthIn35mmFilm";
 
     public static String FOCAL_PLANE_RESOLUTION_UNIT = NS_PREFIX + "FocalPlaneResolutionUnit";
 
     public static String FOCAL_PLANE_X_RESOLUTION = NS_PREFIX + "FocalPlaneXResolution";
 
     public static String FOCAL_PLANE_Y_RESOLUTION = NS_PREFIX + "FocalPlaneYResolution";
 
     public static String GAIN_CONTROL = NS_PREFIX + "GainControl";
 
     public static String IMAGE_UNIQUE_ID = NS_PREFIX + "ImageUniqueID";
 
     public static String ISO_SPEED_RATINGS = NS_PREFIX + "ISOSpeedRatings";
 
     public static String LIGHT_SOURCE = NS_PREFIX + "LightSource";
 
     public static String MAX_APERTURE_VALUE = NS_PREFIX + "MaxApertureValue";
 
     public static String METERING_MODE = NS_PREFIX + "MeteringMode";
 
     public static String OECF = NS_PREFIX + "OECF";
 
     public static String PIXEL_X_DIMENSION = NS_PREFIX + "PixelXDimension";
 
     public static String PIXEL_Y_DIMENSION = NS_PREFIX + "PixelYDimension";
 
     public static String RELATED_SOUND_FILE = NS_PREFIX + "RelatedSoundFile";
 
     public static String SATURATION = NS_PREFIX + "Saturation";
 
     public static String SCENE_CAPTURE_TYPE = NS_PREFIX + "SceneCaptureType";
 
     public static String SCENE_TYPE = NS_PREFIX + "SceneType";
 
     public static String SENSING_METHOD = NS_PREFIX + "SensingMethod";
 
     public static String SHARPNESS = NS_PREFIX + "Sharpness";
 
     public static String SHUTTER_SPEED_VALUE = NS_PREFIX + "ShutterSpeedValue";
 
     public static String SPATIAL_FREQUENCY_RESPONSE = NS_PREFIX + "SpatialFrequencyResponse";
 
     public static String SPECTRAL_SENSITIVITY = NS_PREFIX + "SpectralSensitivity";
 
     public static String SUBJECT_AREA = NS_PREFIX + "SubjectArea";
 
     public static String SUBJECT_DISTANCE = NS_PREFIX + "SubjectDistance";
 
     public static String SUBJECT_DISTANCE_RANGE = NS_PREFIX + "SubjectDistanceRange";
 
     public static String SUBJECT_LOCATION = NS_PREFIX + "SubjectLocation";
 
     public static String USER_COMMENT = NS_PREFIX + "UserComment";
 
     public static String WHITE_BALANCE = NS_PREFIX + "WhiteBalance";
 }
