 /*
  * The MIT License (MIT)
  *
  * Copyright (c) 2013 Steven Willems
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package be.idevelop.fiber;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static be.idevelop.fiber.ObjectCreator.OBJECT_CREATOR;
import static java.util.Collections.EMPTY_MAP;
 
 public class MapSerializer<M extends Map<Object, Object>> extends Serializer<M> {
 
     public MapSerializer(Class<M> mapClass) {
         super(mapClass);
 
         OBJECT_CREATOR.registerClass(mapClass);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public M read(Input input) {
         int length = input.readInteger();
        M map;
        if (length == 0) {
            map = (M) EMPTY_MAP;
        } else {
            map = OBJECT_CREATOR.createNewInstance(getSerializedClass(), input.createReferenceId());
             Map<Object, Object> tempMap = new HashMap<Object, Object>(length);
             for (int i = 0; i < length; i++) {
                 tempMap.put(input.read(), input.read());
             }
             map.putAll(tempMap);
         }
         return map;
     }
 
     @Override
     public void write(M map, Output output) {
         output.writeInt(map.size());
         for (Map.Entry<Object, Object> entry : map.entrySet()) {
             output.write(entry.getKey());
             output.write(entry.getValue());
         }
     }
 }
