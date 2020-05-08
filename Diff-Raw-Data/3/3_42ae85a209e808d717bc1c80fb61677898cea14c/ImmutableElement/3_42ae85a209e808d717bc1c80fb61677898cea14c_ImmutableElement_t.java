 /*
  * Copyright 2013 Christof Lemke
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
 package xml.entity.immutableelement;
 
 import java.util.NoSuchElementException;
 
 import javax.annotation.Nonnull;
import javax.annotation.Nullable;
 
 import xml.entity.select.dsl.DSL;
 import xml.entity.select.dsl.DSL.Join;
 
 import com.google.common.collect.ImmutableList;
 
 public interface ImmutableElement
 {
     /**
      * Get the name of the element. In case of an attribute, the name will
      * always start with an '@'. In case of a text node the name will always be
      * '#text' Otherwise it will be the name of the xml element
      * 
      * @return The element name
      */
     @Nonnull
     String name();
 
     /**
      * In case of an attribute it will be the attribute value. In case of an
      * text node it will be the text content. Otherwise it will be null.
      * 
      * @return
      */
     @Nullable
     String value();
 
     /**
      * The nodes contained within this node. Will always be the empty list for
      * attribute and text nodes.
      * 
      * @return A list of child nodes
      */
     @Nonnull
     ImmutableList<ImmutableElement> children();
 
     /**
      * Returns the single child contained in {@code XmlElement} with {@code name}
      * 
      * @throws NoSuchElementException
      *             if the element has no with that name
      * @throws IllegalArgumentException
      *             if the element has multiple children with that name
      */
     @Nonnull
     ImmutableElement child(String name);
 
     /**
      * Start to select nodes from this node.
      * 
      * @return A select
      */
     @Nonnull
     DSL.Select select();
 
     /**
      * Insert nodes into this node. This operation will never modify this node,
      * instead the result is retrieved by calling .element() when completing the
      * operation.
      * 
      * @return An insert
      */
     @Nonnull
     DSL.Insert insert();
 
     /**
      * Update nodes of this node. This operation will never modify this node,
      * instead the result is retrieved by calling .element() when completing the
      * operation.
      * 
      * @return An update
      */
     @Nonnull
     DSL.Update update();
 
     /**
      * Delete nodes from this node. This operation will never modify this node,
      * instead the result is retrieved by calling .element() when completing the
      * operation.
      * 
      * @return An update
      */
     @Nonnull
     DSL.Delete delete();
 
     /**
      * Not implemented yet
      * 
      * @return
      */
 	Join join();
 }
