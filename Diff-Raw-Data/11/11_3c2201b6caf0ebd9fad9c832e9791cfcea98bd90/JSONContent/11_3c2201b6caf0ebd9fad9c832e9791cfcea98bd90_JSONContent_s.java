 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 import org.json.extension.JSONConstructor;
 import org.json.extension.JSONEncoder;
 
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.patterns.Immutable;
 
 /**
  * use the JSONConverter to produce JSON output of java objects.
  * 
  * @author Detlef Hüttemann
  * @author Willi Schoenborn
  */
 @Immutable
 public class JSONContent extends Content {
 
     public static final JSONContent EMPTY = new JSONContent(new JSONObject());
     
     private final byte [] bytes;
     
     public JSONContent(JSONRenderer renderer) {
         if (renderer == null) throw new NullPointerException("Renderer must not be null");
         bytes = renderer.toString().getBytes();
     }
     
     public JSONContent(JSONObject object) {
         if (object == null) throw new NullPointerException("JSONObject must not be null");
         bytes = object.toString().getBytes();
     }
     
     public JSONContent(JSONArray array) {
         if (array == null) throw new NullPointerException("JSONArray must not be null");
         bytes = array.toString().getBytes();
     }
     
     public JSONContent(JSONConstructor constructor) {
         if (constructor == null) throw new NullPointerException("JSONConstructor must not be null");
         bytes = constructor.toString().getBytes();
     }
     
     public JSONContent(Object object) throws ConversionException, JSONException {
         if (object == null) {
             bytes = "null".getBytes();
         } else if (object instanceof JSONEncoder) {
             final JSONStringer builder = new JSONStringer();
             ((JSONEncoder) object).encodeJSON(builder);
             bytes = builder.toString().getBytes();
         } else if (object instanceof Iterable<?>) {
             final Iterable<?> list = (Iterable<?>) object;
             final JSONConstructor json = new JSONStringer();            
             json.array();            
             for (Object e : list) {
                 if (e instanceof JSONEncoder) {
                     ((JSONEncoder) e).encodeJSON(json);
                 } else {
                     json.value(e);
                 }
             }            
             json.endArray();            
             bytes = json.toString().getBytes();
         } else if (object instanceof Map<?, ?>) {
             final Map<?, ?> map = (Map<?, ?>) object;
             final JSONConstructor json = new JSONStringer();            
             json.object();
             for (Map.Entry<?, ?> entry : map.entrySet()) {
                 json.key(entry.getKey().toString());
                 if (entry.getValue() instanceof JSONEncoder) {
                     ((JSONEncoder) entry.getValue()).encodeJSON(json);                    
                 } else {
                     json.value(entry.getValue());
                 }
             }
             json.endObject();            
             bytes = json.toString().getBytes();
         } else {
             final JSONConverter converter = new JSONConverter();
             final StringBuffer buf = new StringBuffer();
             converter.convert(buf, object);
             bytes = buf.toString().getBytes();
         }
         
         _length = bytes.length;
         _mime = MimeType.JSON;
     }
     
     @Override
     public void write(OutputStream out) throws IOException {
         out.write(bytes, 0, (int) _length);
     }
     
 }
