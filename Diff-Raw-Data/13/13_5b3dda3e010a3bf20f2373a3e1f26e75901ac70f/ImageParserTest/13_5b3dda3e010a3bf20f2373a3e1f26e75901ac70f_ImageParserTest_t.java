 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.core.IMParser;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.media.FxImageMetadata;
 import com.flexive.shared.media.FxMediaEngine;
 import com.flexive.shared.media.FxMediaType;
 import com.flexive.shared.media.FxMetadata;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 
 /**
  * Test for ImageMagick/ImageParser
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"image", "imageParser"})
 public class ImageParserTest {
 
     private static String testData =
             "Image: IMG_0048.JPG\n" +
                     "  Format: JPEG (Joint Photographic Experts Group JFIF format)\n" +
                     "  Class: DirectClass\n" +
                     "  Geometry: 2048x1536+0+0\n" +
                     "  Type: TrueColor\n" +
                     "  Endianess: Undefined\n" +
                     "  Colorspace: RGB\n" +
                     "  Channel depth:\n" +
                     "    Red: 8-bit\n" +
                     "    Green: 8-bit\n" +
                     "    Blue: 8-bit\n" +
                     "  Channel statistics:\n" +
                     "    Red:\n" +
                     "      Min: 0 (0)\n" +
                     "      Max: 255 (1)\n" +
                     "      Mean: 115.399 (0.452545)\n" +
                     "      Standard deviation: 88.9127 (0.348677)\n" +
                     "    Green:\n" +
                     "      Min: 0 (0)\n" +
                     "      Max: 255 (1)\n" +
                     "      Mean: 82.4621 (0.323381)\n" +
                     "      Standard deviation: 60.6466 (0.23783)\n" +
                     "    Blue:\n" +
                     "      Min: 0 (0)\n" +
                     "      Max: 255 (1)\n" +
                     "      Mean: 66.7881 (0.261914)\n" +
                     "      Standard deviation: 53.3319 (0.209145)\n" +
                     "  Colors: 272549\n" +
                     "  Rendering intent: Undefined\n" +
                     "  Resolution: 180x180\n" +
                     "  Units: PixelsPerInch\n" +
                     "  Filesize: 840.578kb\n" +
                     "  Interlace: None\n" +
                     "  Background color: white\n" +
                     "  Border color: rgb(223,223,223)\n" +
                     "  Matte color: grey74\n" +
                     "  Transparent color: black\n" +
                     "  Page geometry: 2048x1536+0+0\n" +
                     "  Dispose: Undefined\n" +
                     "  Iterations: 0\n" +
                     "  Compression: JPEG\n" +
                     "  Quality: 90\n" +
                     "  Orientation: TopLeft\n" +
                     "  Exif:DateTime: 2006:08:07 22:27:24\n" +
                     "  Exif:Make: Canon\n" +
                     "  Exif:Model: Canon PowerShot A400\n" +
                     "  Exif:Orientation: 1\n" +
                     "  Exif:ResolutionUnit: 2\n" +
                     "  Exif:XResolution: 180/1\n" +
                     "  Exif:YCbCrPositioning: 1\n" +
                     "  Exif:YResolution: 180/1\n" +
                     "  Jpeg:colorspace: 2\n" +
                     "  Jpeg:sampling-factor: 2x1,1x1,1x1\n" +
                     "  Signature: 8af18c5d3e9992092fdf56d60bde44ba3380ff29aed343d8fa4eea33bb0af8ea\n" +
                     "  Unknown: 196\n" +
                     "  Profile-exif: 8700 bytes\n" +
                     "  Tainted: False\n" +
                     "  User time: 0.141u\n" +
                     "  Elapsed time: 0:01\n" +
                     "  Pixels per second: 14.7784mb\n" +
                     "  Version: ImageMagick 6.3.4 05/11/07 Q16 http://www.imagemagick.org";
 
     private static final String TEST_RESULT = "<?xml version=\"1.0\" ?><Image source=\"IMG_0048.JPG\"><Format>JPEG (Joint Photographic Experts Group JFIF format)</Format><Class>DirectClass</Class><Geometry>2048x1536+0+0</Geometry><Type>TrueColor</Type><Endianess>Undefined</Endianess><Colorspace>RGB</Colorspace><Channel-depth><Red>8-bit</Red><Green>8-bit</Green><Blue>8-bit</Blue></Channel-depth><Channel-statistics><Red><Min>0 (0)</Min><Max>255 (1)</Max><Mean>115.399 (0.452545)</Mean><Standard-deviation>88.9127 (0.348677)</Standard-deviation></Red><Green><Min>0 (0)</Min><Max>255 (1)</Max><Mean>82.4621 (0.323381)</Mean><Standard-deviation>60.6466 (0.23783)</Standard-deviation></Green><Blue><Min>0 (0)</Min><Max>255 (1)</Max><Mean>66.7881 (0.261914)</Mean><Standard-deviation>53.3319 (0.209145)</Standard-deviation></Blue></Channel-statistics><Colors>272549</Colors><Rendering-intent>Undefined</Rendering-intent><Resolution>180x180</Resolution><Units>PixelsPerInch</Units><Filesize>840.578kb</Filesize><Interlace>None</Interlace><Background-color>white</Background-color><Border-color>rgb(223,223,223)</Border-color><Matte-color>grey74</Matte-color><Transparent-color>black</Transparent-color><Page-geometry>2048x1536+0+0</Page-geometry><Dispose>Undefined</Dispose><Iterations>0</Iterations><Compression>JPEG</Compression><Quality>90</Quality><Orientation>TopLeft</Orientation><Exif-DateTime>2006:08:07 22:27:24</Exif-DateTime><Exif-Make>Canon</Exif-Make><Exif-Model>Canon PowerShot A400</Exif-Model><Exif-Orientation>1</Exif-Orientation><Exif-ResolutionUnit>2</Exif-ResolutionUnit><Exif-XResolution>180/1</Exif-XResolution><Exif-YCbCrPositioning>1</Exif-YCbCrPositioning><Exif-YResolution>180/1</Exif-YResolution><Jpeg-colorspace>2</Jpeg-colorspace><Jpeg-sampling-factor>2x1,1x1,1x1</Jpeg-sampling-factor><Signature>8af18c5d3e9992092fdf56d60bde44ba3380ff29aed343d8fa4eea33bb0af8ea</Signature><Unknown>196</Unknown><Profile-exif>8700 bytes</Profile-exif><Tainted>False</Tainted><User-time>0.141u</User-time><Elapsed-time>0:01</Elapsed-time><Pixels-per-second>14.7784mb</Pixels-per-second><Version>ImageMagick 6.3.4 05/11/07 Q16 http://www.imagemagick.org</Version></Image>";
     private static final String TEST_FILE_EXIF = "src" + File.separatorChar + "framework" + File.separatorChar + "testresources" + File.separatorChar + "image" + File.separatorChar + "Exif.JPG";
     private static final String TEST_FILE_GIF = "src" + File.separatorChar + "framework" + File.separatorChar + "testresources" + File.separatorChar + "image" + File.separatorChar + "GIF_Image.GIF";
 
     @Test(dependsOnMethods = {"identifyTest"})
     public void parserTest() throws Exception {
         if (!FxMediaEngine.isImageMagickIdentifySupported())
             return;
         assert IMParser.parse(new ByteArrayInputStream(FxSharedUtils.getBytes(testData))).startsWith(TEST_RESULT);
         FxSharedUtils.ProcessResult res = FxSharedUtils.executeCommand("identify", "-verbose", TEST_FILE_EXIF);
         String result = IMParser.parse(new ByteArrayInputStream(FxSharedUtils.getBytes(res.getStdOut())));
         assert result.indexOf("<Profile-exif>4604 bytes</Profile-exif>") > 0;
         assert result.indexOf("<Geometry>2048x1536+0+0</Geometry>") > 0;
         assert result.indexOf("<Channel-depth><Red>8-bit</Red><Green>8-bit</Green>") > 0;
         res = FxSharedUtils.executeCommand("identify", "-verbose", TEST_FILE_GIF);
         result = IMParser.parse(new ByteArrayInputStream(FxSharedUtils.getBytes(res.getStdOut())));
         assert result.indexOf("<Signature>2f0ab5863f31c675bab16309bfa161fd331e55e6c6781ec1f6cd0305f725cfa9</Signature>") > 0;
         assert result.indexOf("<Compression>LZW</Compression>") > 0;
     }
 
     @Test
     public void identifyTest() throws Exception {
         if (!FxMediaEngine.hasImageMagickInstalled())
             return;
         FxSharedUtils.ProcessResult res = FxSharedUtils.executeCommand("identify", "-version");
         System.out.println("Commandline: [" + res.getCommandLine() + "]");
         System.out.println("ExitCode: " + res.getExitCode());
         System.out.println("Error: [" + res.getStdErr() + "]");
         System.out.println("Out: [" + res.getStdOut() + "]");
         if (res.getExitCode() != 0 || res.getStdOut().indexOf("ImageMagick") <= 0) {
             assert false : "Please install ImageMagick (and add it to your path) for imaging tests to work!";
         }
     }
 
     public void mediaEngine() throws Exception {
         File exif = new File(TEST_FILE_EXIF);
         /*Sanselan.getImageInfo(exif).dump();
         ICC_Profile prof = Sanselan.getICCProfile(exif);
         if (prof != null) {
             System.out.println("We have an ICC profile");
         } else
             System.out.println("No ICC profile");
 //        Sanselan.getFormatCompliance(exif).dump();
         IImageMetadata imageMetadata = Sanselan.getMetadata(exif);
         for (Object o : imageMetadata.getItems())
             System.out.println("Metadata: " + o);
         */
         FxMetadata md = FxMediaEngine.identify(null, exif);
         System.out.println("mimeTyp: " + md.getMimeType());
         System.out.println("Filename: " + md.getFilename());
         System.out.println("mediaType: " + md.getMediaType());
         if (md.getMediaType() == FxMediaType.Image) {
             FxImageMetadata imd = md.asImageMetadata();
             System.out.println("BPP: " + imd.getBitsPerPixel());
             System.out.println("color type:" + imd.getColorType());
             System.out.println("compression:" + imd.getCompressionAlgorithm());
             System.out.println("format:" + imd.getFormat());
             System.out.println("format-desc:" + imd.getFormatDescription());
             System.out.println("height:" + imd.getHeight());
             System.out.println("width:" + imd.getWidth());
             System.out.println("x-res:" + imd.getXResolution());
             System.out.println("y-res:" + imd.getYResolution());
 
         }
         for (FxMetadata.FxMetadataItem i : md.getMetadata()) {
             System.out.println("Metadata: [" + i.getKey() + "] -> [" + i.getValue() + "]");
         }
         System.out.println("IM available: " + FxMediaEngine.hasImageMagickInstalled());
         if (FxMediaEngine.hasImageMagickInstalled()) {
             System.out.println("IM version: " + FxMediaEngine.getImageMagickVersion());
             System.out.println("IM based identify possible: " + FxMediaEngine.isImageMagickIdentifySupported());
         }
        //strip <?xml version="1.0" ?> since sometimes it is returned as <?xml version="1.0"?>
        Assert.assertEquals(md.toXML().substring(md.toXML().indexOf("?>")+2), "<metadata mediatype=\"Image\" mimetype=\"image/jpeg\" filename=\"Exif.JPG\"><imageData><width>2048</width><height>1536</height><bpp>24</bpp><colorType><![CDATA[RGB]]></colorType><compressionAlgorithm><![CDATA[JPEG]]></compressionAlgorithm><format><![CDATA[JPEG]]></format><formatDescription><![CDATA[JPEG (Joint Photographic Experts Group) Format]]></formatDescription><xResolution>180.0</xResolution><yResolution>180.0</yResolution></imageData><meta key=\"Make\"><![CDATA[Canon]]></meta><meta key=\"Model\"><![CDATA[Canon PowerShot A70]]></meta><meta key=\"Orientation\"><![CDATA[1]]></meta><meta key=\"XResolution\"><![CDATA[180]]></meta><meta key=\"YResolution\"><![CDATA[180]]></meta><meta key=\"ResolutionUnit\"><![CDATA[2]]></meta><meta key=\"DateTime\"><![CDATA[2006-08-07T18:20:21.000+0200]]></meta><meta key=\"YCbCrPositioning\"><![CDATA[1]]></meta><meta key=\"Exif_IFD_Pointer\"><![CDATA[196]]></meta><meta key=\"ExposureTime\"><![CDATA[1/30]]></meta><meta key=\"FNumber\"><![CDATA[56/10]]></meta><meta key=\"ExifVersion\"><![CDATA[48, 50, 50, 48]]></meta><meta key=\"DateTimeOriginal\"><![CDATA[2006-08-07T18:20:21.000+0200]]></meta><meta key=\"DateTimeDigitized\"><![CDATA[2006-08-07T18:20:21.000+0200]]></meta><meta key=\"ComponentsConfiguration\"><![CDATA[1, 2, 3, 0]]></meta><meta key=\"CompressedBitsPerPixel\"><![CDATA[3]]></meta><meta key=\"ShutterSpeedValue\"><![CDATA[157/32]]></meta><meta key=\"ApertureValue\"><![CDATA[159/32]]></meta><meta key=\"ExposureBiasValue\"><![CDATA[0]]></meta><meta key=\"MaxApertureValue\"><![CDATA[145/32]]></meta><meta key=\"MeteringMode\"><![CDATA[5]]></meta><meta key=\"Flash\"><![CDATA[16]]></meta><meta key=\"FocalLength\"><![CDATA[519/32]]></meta><meta key=\"MakerNote\"><![CDATA[14, 0, 1, 0, 3, 0, 46, 0, 0, 0, 92, 4, 0, 0, 2, 0, 3, 0, 4, 0, 0, 0, -72, 4, 0, 0, 3, 0, 3, 0, 4, 0, 0, 0, -64, 4, 0, 0, 4, 0, 3, 0, 34, 0, 0, 0, -56, 4, 0, 0, 0... (574)]]></meta><meta key=\"UserComment\"><![CDATA[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0... (264)]]></meta><meta key=\"FlashpixVersion\"><![CDATA[48, 49, 48, 48]]></meta><meta key=\"ColorSpace\"><![CDATA[1]]></meta><meta key=\"PixelXDimension\"><![CDATA[2048]]></meta><meta key=\"PixelYDimension\"><![CDATA[1536]]></meta><meta key=\"Interoperability_IFD_Pointer\"><![CDATA[1540]]></meta><meta key=\"FocalPlaneXResolution\"><![CDATA[2048000/208]]></meta><meta key=\"FocalPlaneYResolution\"><![CDATA[1536000/156]]></meta><meta key=\"FocalPlaneResolutionUnit\"><![CDATA[2]]></meta><meta key=\"FileSource\"><![CDATA[3]]></meta><meta key=\"CustomRendered\"><![CDATA[0]]></meta><meta key=\"ExposureMode\"><![CDATA[0]]></meta><meta key=\"WhiteBalance\"><![CDATA[0]]></meta><meta key=\"DigitalZoomRatio\"><![CDATA[1]]></meta><meta key=\"SceneCaptureType\"><![CDATA[1]]></meta><meta key=\"GPSLatitudeRef\"><![CDATA[R98]]></meta><meta key=\"GPSLatitude\"><![CDATA[48, 49, 48, 48]]></meta><meta key=\"Compression\"><![CDATA[6]]></meta><meta key=\"XResolution\"><![CDATA[180]]></meta><meta key=\"YResolution\"><![CDATA[180]]></meta><meta key=\"ResolutionUnit\"><![CDATA[2]]></meta><meta key=\"JPEGInterchangeFormat\"><![CDATA[2036]]></meta><meta key=\"JPEGInterchangeFormatLength\"><![CDATA[2427]]></meta></metadata>");
     }
 }
