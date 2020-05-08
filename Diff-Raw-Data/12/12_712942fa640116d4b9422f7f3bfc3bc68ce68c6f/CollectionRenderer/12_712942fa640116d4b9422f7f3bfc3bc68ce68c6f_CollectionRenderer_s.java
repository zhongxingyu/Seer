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
 
 import java.util.Deque;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.map.LinkedMap;
 
 import com.google.common.collect.Lists;
 
 /**
  * An implementation of the {@link Renderer} interface which builds a 
  * structure of {@link List}s and {@link Map}s.
  *
  * @author Willi Schoenborn
  */
 public final class CollectionRenderer extends AbstractRenderer {
 
     private final Deque<Object> stack = Lists.newLinkedList();
     
     private Mode mode = Mode.INITIAL;
     
     private Object build;
     
     private List<Object> peekList() {
         if (mode == Mode.LIST) {
             final Object peek = stack.element();
             @SuppressWarnings("unchecked")
             final List<Object> list = List.class.cast(peek);
             return list;
         } else {
             throw new RenderingException(String.format("Expected mode to be %s, but was %s", Mode.LIST, mode));
         }
     }
     
     private boolean peekIsList() {
         return stack.peek() instanceof List<?>;
     }
     
     private LinkedMap peekMap() {
         if (mode == Mode.MAP || mode == Mode.KEY) {
             final Object peek = stack.element();
             assert peek instanceof LinkedMap;
             return LinkedMap.class.cast(peek);
         } else {
             throw new RenderingException(String.format("Expected mode to be %s, but was %s", Mode.MAP, mode));
         }
     }
     
     private boolean peekIsMap() {
         return stack.peek() instanceof LinkedMap;
     }
     
     private Renderer append(Object value) {
         if (mode == Mode.LIST) {
             peekList().add(value);
         } else if (mode == Mode.KEY) {
             final LinkedMap map = peekMap();
             map.put(map.lastKey(), value);
             mode = Mode.MAP;
         } else {
             throw new RenderingException(String.format("Appending only works in %s and %s", Mode.LIST, Mode.KEY));
         }
         return this;
     }
     
     @Override
     protected RenderingLevel defaultLevel() {
         return Rendering.maxLevel();
     }
     
     @Override
     public Renderer list() throws RenderingException {
         if (Mode.LIST.isAllowedAfter(mode)) {
             stack.push(Lists.newArrayList());
             mode = Mode.LIST;
             return this;
         } else {
             throw new RenderingException(String.format("%s is not allowed after %s", Mode.LIST, mode));
         }
     }
     
     @Override
     public Renderer endList() throws RenderingException {
         if (mode == Mode.LIST) {
             final List<Object> peek = peekList();
             stack.pop();
             if (stack.isEmpty()) {
                 mode = Mode.DONE;
                 build = peek;
             } else if (peekIsList()) {
                 mode = Mode.LIST;
                 peekList().add(peek);
             } else if (peekIsMap()) {
                 mode = Mode.MAP;
                 final LinkedMap map = peekMap();
                 map.put(map.lastKey(), peek);
             } else {
                 throw new RenderingException("Unknown state");
             }
             return this;
         } else {
             throw new RenderingException(String.format("endList is not allowed when in %s mode", mode));
         }
     }
     
     @Override
     public Renderer map() throws RenderingException {
         if (Mode.MAP.isAllowedAfter(mode)) {
             stack.push(new LinkedMap());
             mode = Mode.MAP;
             return this;
         } else {
             throw new RenderingException(String.format("%s is not allowed after %s", Mode.MAP, mode));
         }
     }
     
     @Override
     public Renderer endMap() throws RenderingException {
         if (mode == Mode.MAP) {
             final LinkedMap peek = peekMap();
             stack.pop();
             if (stack.isEmpty()) {
                 mode = Mode.DONE;
                 build = peek;
             } else if (peekIsList()) {
                 mode = Mode.LIST;
                 peekList().add(peek);
             } else if (peekIsMap()) {
                 mode = Mode.MAP;
                 final LinkedMap map = peekMap();
                 map.put(map.lastKey(), peek);
             } else {
                 throw new RenderingException("Unknown state");
             }
             return this;
         } else {
             throw new RenderingException(String.format("endMap is not allowed when in %s mode", mode));
         }
     }
     
     @Override
     public Renderer key(CharSequence key) throws RenderingException {
         if (Mode.KEY.isAllowedAfter(mode)) {
             peekMap().put(key == null ? null : key.toString(), null);
             mode = Mode.KEY;
             return this;
         } else {
             throw new RenderingException(String.format("%s is not allowed after %s", Mode.KEY, mode));
         }
     }
     
     @Override
     public Renderer nullValue() throws RenderingException {
         return append(null);
     }
     
     @Override
     public Renderer value(boolean value) throws RenderingException {
         return append(Boolean.valueOf(value));
     }
     
     @Override
     public Renderer value(CharSequence value) throws RenderingException {
         return value == null ? nullValue() : append(value);
     }
     
     /**
      * {@inheritDoc}
      * <strong>Note</strong>: Beware of auto-boxing.
      */
     @Override
     public Renderer value(double value) throws RenderingException {
         return append(value);
     }
 
     /**
      * {@inheritDoc}
      * <strong>Note</strong>: Beware of auto-boxing.
      */
     @Override
     public Renderer value(long value) throws RenderingException {
         return append(value);
     }
     
     @Override
     public Object build() throws RenderingException {
         if (mode == Mode.DONE) {
             assert stack.isEmpty();
            assert build != null;
             return build;
         } else {
             throw new RenderingException(String.format("Structure not finished, current mode is %s", mode));
         }
     }
 
 }
