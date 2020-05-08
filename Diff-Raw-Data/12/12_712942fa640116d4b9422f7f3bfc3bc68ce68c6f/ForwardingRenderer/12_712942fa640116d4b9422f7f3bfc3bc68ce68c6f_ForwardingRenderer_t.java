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
 
 package de.cosmocode.rendering;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.google.common.collect.ForwardingObject;
 
 /**
  * An abstract decorator for {@link Renderer}s.
  * 
  * @author Willi Schoenborn
  */
 public abstract class ForwardingRenderer extends ForwardingObject implements Renderer {
 
     @Override
     protected abstract Renderer delegate();
 
     @Override
     public Object build() throws RenderingException {
        return delegate().build();
     }
 
     @Override
     public Renderer endList() throws RenderingException {
         delegate().endList();
         return this;
     }
 
     @Override
     public Renderer endMap() throws RenderingException {
         delegate().endMap();
         return this;
     }
 
     @Override
     public Renderer key(CharSequence key) throws RenderingException {
         delegate().key(key);
         return this;
     }
 
     @Override
     public Renderer key(Object key) throws RenderingException {
         delegate().key(key);
         return this;
     }
 
     @Override
     public Renderer list() throws RenderingException {
         delegate().list();
         return this;
     }
 
     @Override
     public Renderer map() throws RenderingException {
         delegate().map();
         return this;
     }
 
     @Override
     public Renderer nullValue() throws RenderingException {
         delegate().nullValue();
         return this;
     }
 
     @Override
     public Renderer pairs(Map<?, ?> pairs) throws RenderingException {
         delegate().pairs(pairs);
         return this;
     }
 
     @Override
     public <T> Renderer pairs(Map<?, ? extends T> pairs, ValueRenderer<? super T> renderer) 
         throws RenderingException {
         delegate().pairs(pairs, renderer);
         return this;
     }
 
     @Override
     public Renderer pairs(Renderable pairs, RenderingLevel level) throws RenderingException {
         delegate().pairs(pairs, level);
         return this;
     }
 
     @Override
     public Renderer value(boolean value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(Calendar value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(CharSequence value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(Date value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(double value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(Enum<?> value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(byte[] value) throws RenderingException {
         delegate().value(value);
         return this;
     }
     
     @Override
     public Renderer value(InputStream value) throws RenderingException {
         delegate().value(value);
         return this;
     }
     
     @Override
     public Renderer value(Iterable<?> values) throws RenderingException {
         delegate().value(values);
         return this;
     }
 
     @Override
     public <T> Renderer value(Iterable<? extends T> values, ValueRenderer<? super T> renderer) 
         throws RenderingException {
         delegate().value(values, renderer);
         return this;
     }
 
     @Override
     public Renderer value(Iterator<?> values) throws RenderingException {
         delegate().value(values);
         return this;
     }
 
     @Override
     public <T> Renderer value(Iterator<? extends T> values, ValueRenderer<? super T> renderer) 
         throws RenderingException {
         delegate().value(values, renderer);
         return this;
     }
 
     @Override
     public Renderer value(Iterable<? extends Renderable> values, RenderingLevel level) throws RenderingException {
         delegate().value(values, level);
         return this;
     }
     
     @Override
     public Renderer value(Iterator<? extends Renderable> values, RenderingLevel level) throws RenderingException {
         delegate().value(values, level);
         return this;
     }
     
     @Override
     public Renderer value(long value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(Map<?, ?> pairs) throws RenderingException {
         delegate().value(pairs);
         return this;
     }
 
     @Override
     public <T> Renderer value(Map<?, ? extends T> pairs, ValueRenderer<? super T> renderer) 
         throws RenderingException {
         delegate().value(pairs, renderer);
         return this;
     }
 
     @Override
     public Renderer value(Object... values) throws RenderingException {
         delegate().value(values);
         return this;
     }
 
     @Override
     public Renderer value(Object value) throws RenderingException {
         delegate().value(value);
         return this;
     }
 
     @Override
     public Renderer value(Renderable pairs, RenderingLevel level) throws RenderingException {
         delegate().value(pairs, level);
         return this;
     }
 
     @Override
     public <T> Renderer value(T value, ValueRenderer<? super T> renderer) throws RenderingException {
         delegate().value(value, renderer);
         return this;
     }
 
     @Override
     public Renderer values(Iterable<?> values) throws RenderingException {
         delegate().values(values);
         return this;
     }
 
     @Override
     public <T> Renderer values(Iterable<? extends T> values, ValueRenderer<? super T> renderer) 
         throws RenderingException {
         delegate().values(values, renderer);
         return this;
     }
 
     @Override
     public Renderer values(Iterable<? extends Renderable> values, RenderingLevel level) throws RenderingException {
         delegate().values(values, level);
         return this;
     }
     
     @Override
     public Renderer values(Iterator<? extends Renderable> values, RenderingLevel level) throws RenderingException {
         delegate().values(values, level);
         return this;
     }
     
     @Override
     public Renderer values(Iterator<?> values) throws RenderingException {
         delegate().values(values);
         return this;
     }
 
     @Override
     public <T> Renderer values(Iterator<? extends T> values, ValueRenderer<? super T> renderer) 
         throws RenderingException {
         delegate().values(values, renderer);
         return this;
     }
 
     @Override
     public Renderer values(Object... values) throws RenderingException {
         delegate().values(values);
         return this;
     }
 
 }
