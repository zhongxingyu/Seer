 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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
 
 package org.romaframework.aspect.validation.feature;
 
 import org.romaframework.aspect.validation.ValidationAspect;
 import org.romaframework.core.schema.Feature;
 import org.romaframework.core.schema.FeatureType;
 
 public class ValidationActionFeatures {
 
	public static final Feature<Boolean>	VALIDATE	= new Feature<Boolean>(ValidationAspect.ASPECT_NAME, "validate", FeatureType.ACTION, Boolean.class, Boolean.TRUE);
 
 }
