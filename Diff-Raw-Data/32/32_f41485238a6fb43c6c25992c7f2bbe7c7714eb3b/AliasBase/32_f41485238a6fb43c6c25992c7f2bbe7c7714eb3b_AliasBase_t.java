 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.model.geo;
 
import de.cosmocode.rendering.Renderable;
 
 /**
  * An alias specified a localized name of a {@link ToponymBase} and is therefore
  * a tupel containing:
  * <ol>
  *   <li>a name</li>
  *   <li>a language code</li>
  *   <li>a reference to a {@link ToponymBase}</li>
  * </ol>.
  * 
  * @see <a href="http://download.geonames.org/export/dump/">Geonames.org Download Server</a>
  *
  * @since 2.0
  * @author Willi Schoenborn
  */
public interface AliasBase extends Renderable {
 
     /**
      * Provides the localized name of the associated toponym.
      * 
      * @since 2.0
      * @return the localized name
      */
     String getName();
     
     /**
      * Provides the ISO 639-2 language code.
      * 
      * @since 2.0
      * @return the iso language code
      */
     String getLanguageCode();
     
     /**
      * Provides the associated toponym.
      * 
      * @since 2.0
      * @return the associated toponym
      */
     ToponymBase getToponym();
     
 }
