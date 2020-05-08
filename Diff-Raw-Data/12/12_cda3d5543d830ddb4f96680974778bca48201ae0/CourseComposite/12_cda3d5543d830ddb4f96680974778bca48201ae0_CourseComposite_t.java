 /*
  * Copyright 2007 Lan Boon Ping. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package org.qi4j.chronos.test.model1;
 
 import java.io.Serializable;
 import org.qi4j.api.Composite;
 import org.qi4j.api.annotation.Mixins;
 import org.qi4j.chronos.model.TimeRange;
import org.qi4j.library.framework.PropertiesMixin;
 
 @Mixins( { PropertiesMixin.class } )
 //@Assertions({ TimeRangeValidationModifier.class })
 public interface CourseComposite extends Composite, TimeRange, Serializable
 {
 
 }
