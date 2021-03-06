 /*
  * Copyright 2002-2013 Drew Noakes
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  *
  * More information about this project is available at:
  *
  *    http://drewnoakes.com/code/exif/
  *    http://code.google.com/p/metadata-extractor/
  */
 package com.drew.metadata.exif;
 
 import com.drew.lang.annotations.NotNull;
 import com.drew.lang.annotations.Nullable;
 import com.drew.metadata.TagDescriptor;
 
 /**
  * Provides human-readable string representations of tag values stored in a <code>OlympusMakernoteDirectory</code>.
  *
  * @author Drew Noakes http://drewnoakes.com
  */
 public class OlympusMakernoteDescriptor extends TagDescriptor<OlympusMakernoteDirectory>
 {
     public OlympusMakernoteDescriptor(@NotNull OlympusMakernoteDirectory directory)
     {
         super(directory);
     }
 
     @Nullable
     public String getDescription(int tagType)
     {
         switch (tagType) {
             case OlympusMakernoteDirectory.TAG_SPECIAL_MODE:
                 return getSpecialModeDescription();
             case OlympusMakernoteDirectory.TAG_JPEG_QUALITY:
                 return getJpegQualityDescription();
             case OlympusMakernoteDirectory.TAG_MACRO_MODE:
                 return getMacroModeDescription();
             case OlympusMakernoteDirectory.TAG_BW_MODE:
                 return getBWModeDescription();
             case OlympusMakernoteDirectory.TAG_DIGI_ZOOM_RATIO:
                 return getDigiZoomRatioDescription();
             case OlympusMakernoteDirectory.TAG_CAMERA_ID:
                 return getCameraIdDescription();
             case OlympusMakernoteDirectory.TAG_FLASH_MODE:
                 return getFlashModeDescription();
             case OlympusMakernoteDirectory.TAG_FOCUS_RANGE:
                 return getFocusRangeDescription();
             case OlympusMakernoteDirectory.TAG_FOCUS_MODE:
                 return getFocusModeDescription();
             case OlympusMakernoteDirectory.TAG_SHARPNESS:
                 return getSharpnessDescription();
             default:
                 return super.getDescription(tagType);
         }
     }
 
     @Nullable
     public String getSharpnessDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_SHARPNESS, "Normal", "Hard", "Soft");
     }
 
     @Nullable
     public String getFocusModeDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_FOCUS_MODE, "Auto", "Manual");
     }
 
     @Nullable
     public String getFocusRangeDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_FOCUS_RANGE, "Normal", "Macro");
     }
 
     @Nullable
     public String getFlashModeDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_FLASH_MODE, null, null, "On", "Off");
     }
 
     @Nullable
     public String getDigiZoomRatioDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_DIGI_ZOOM_RATIO, "Normal", null, "Digital 2x Zoom");
     }
 
     @Nullable
     public String getCameraIdDescription()
     {
         byte[] bytes = _directory.getByteArray(OlympusMakernoteDirectory.TAG_CAMERA_ID);
         if (bytes == null)
             return null;
         return new String(bytes);
     }
 
     @Nullable
     public String getMacroModeDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_MACRO_MODE, "Normal (no macro)", "Macro");
     }
 
     @Nullable
     public String getBWModeDescription()
     {
         return getIndexedDescription(OlympusMakernoteDirectory.TAG_BW_MODE, "Off", "On");
     }
 
     @Nullable
     public String getJpegQualityDescription()
     {
        return getIndexedDescription(OlympusMakernoteDirectory.TAG_JPEG_QUALITY, null, "SQ", "HQ", "SHQ");
     }
 
     @Nullable
     public String getSpecialModeDescription()
     {
         int[] values = _directory.getIntArray(OlympusMakernoteDirectory.TAG_SPECIAL_MODE);
         if (values==null)
             return null;
         if (values.length < 1)
             return "";
         StringBuilder desc = new StringBuilder();
         switch (values[0]) {
             case 0:
                 desc.append("Normal picture taking mode");
                 break;
             case 1:
                 desc.append("Unknown picture taking mode");
                 break;
             case 2:
                 desc.append("Fast picture taking mode");
                 break;
             case 3:
                 desc.append("Panorama picture taking mode");
                 break;
             default:
                 desc.append("Unknown picture taking mode");
                 break;
         }
 
         if (values.length < 2)
             return desc.toString();
         desc.append(" - ");
         switch (values[1]) {
             case 0:
                 desc.append("Unknown sequence number");
                 break;
             case 1:
                 desc.append("1st in a sequence");
                 break;
             case 2:
                 desc.append("2nd in a sequence");
                 break;
             case 3:
                 desc.append("3rd in a sequence");
                 break;
             default:
                 desc.append(values[1]);
                 desc.append("th in a sequence");
                 break;
         }
         if (values.length < 3)
             return desc.toString();
         desc.append(" - ");
         switch (values[2]) {
             case 1:
                 desc.append("Left to right panorama direction");
                 break;
             case 2:
                 desc.append("Right to left panorama direction");
                 break;
             case 3:
                 desc.append("Bottom to top panorama direction");
                 break;
             case 4:
                 desc.append("Top to bottom panorama direction");
                 break;
         }
         return desc.toString();
     }
 }
