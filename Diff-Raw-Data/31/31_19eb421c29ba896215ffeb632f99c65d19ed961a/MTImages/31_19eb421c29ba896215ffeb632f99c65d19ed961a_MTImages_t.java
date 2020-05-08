 /**
  * Get more info at : www.jrebirth.org .
  * Copyright JRebirth.org © 2011-2013
  * Contact : sebastien.bordes@jrebirth.org
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jrebirth.demo.masteringtables.resources;
 
 import static org.jrebirth.core.resource.Resources.create;
 
import org.jrebirth.core.resource.image.ImageExtension;
 import org.jrebirth.core.resource.image.ImageItem;
 import org.jrebirth.core.resource.image.LocalImage;
 
 /**
  * The MTImages interface providing all images.
  */
 public interface MTImages {
 
     /** The application main title image. */
    ImageItem MT_TITLE = create(new LocalImage("image/", "Title", ImageExtension.PNG));
 
     /** . */
    ImageItem RESULT_MONSTER = create(new LocalImage("image/", "Result_Monster", ImageExtension.PNG));
 
     /** . */
    ImageItem RESULT_SUCCESS_ICON = create(new LocalImage("image/", "Result_Success_Icon", ImageExtension.PNG));
 
     /** . */
    ImageItem RESULT_FAILURE_ICON = create(new LocalImage("image/", "Result_Failure_Icon", ImageExtension.PNG));
 }
