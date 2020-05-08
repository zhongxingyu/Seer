 /**
  *    Copyright 2012 MegaFon
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
  */
 package ru.histone.evaluator.nodes;
 
 import com.fasterxml.jackson.core.io.CharTypes;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.DecimalNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.fasterxml.jackson.databind.node.TextNode;
 
 import ru.histone.utils.IOUtils;
 import ru.histone.utils.StringUtils;
 import sun.security.util.BigInt;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 public class NodeFactory {
     private ObjectMapper jackson;
     public final Node UNDEFINED;
     public final BooleanHistoneNode TRUE;
     public final BooleanHistoneNode FALSE;
     public final Node NULL;
     public final NumberHistoneNode UNDEFINED_NUMBER;
 
     public NodeFactory(ObjectMapper jackson) {
         this.jackson = jackson;
 
         UNDEFINED = new UndefinedNode(this);
         UNDEFINED_NUMBER = new UndefinedNumberHistoneNode(this);
         NULL = new NullHistoneNode(this);
         TRUE = new BooleanHistoneNode(this, true);
         FALSE = new BooleanHistoneNode(this, false);
     }
 
     public ObjectHistoneNode object() {
         return new ObjectHistoneNode(this);
     }
 
     public ObjectHistoneNode object(ObjectHistoneNode src) {
         ObjectHistoneNode node = src.isGlobalObject() ? new GlobalObjectNode(this) : object();
         for (Map.Entry<Object, Node> entry : src.getElements().entrySet()) {
             node.add(entry.getKey(), entry.getValue());
         }
 //        node.elements = new LinkedHashMap<Object, Node>(src.elements);
         return node;
     }
 
     /**
      * Create array node object and fill it with nodes from specified arguments
      *
      * @param elements node elements to use for creating new array object
      * @return new array node object
      */
     public ObjectHistoneNode object(Node... elements) {
         if (elements == null) {
             return object();
         }
         ObjectHistoneNode node = object();
         for (Node item : elements) {
             node.add(item);
         }
         return node;
     }
 
     /**
      * Create array node object and fill it with nodes from specified arguments
      *
      * @param elements node elements to use for creating new array object
      * @return new array node object
      */
     public ObjectHistoneNode object(String... elements) {
         if (elements == null) {
             return object();
         }
         ObjectHistoneNode node = object();
         for (String item : elements) {
             node.add(string(item));
         }
         return node;
     }
 
     public ObjectHistoneNode object(Collection<Node> elements) {
         if (elements == null) {
             return object();
         }
         ObjectHistoneNode node = object();
         for (Node item : elements) {
             node.add(item);
         }
         return node;
     }
 
     public ObjectHistoneNode object(ArrayNode src) {
         if (src == null) {
             return object();
         }
         ObjectHistoneNode node = object();
         for (JsonNode entry : src) {
             node.add(jsonToNode(entry));
         }
         return node;
     }
 
     /**
      * Create src type src and fill it with items from specified ObjectNode src
      *
      * @param src source src ot use for copying items from
      * @return created obejct
      */
     public ObjectHistoneNode object(ObjectNode src) {
         if (src == null) {
             return object();
         }
         ObjectHistoneNode node = object();
         Iterator<Map.Entry<String, JsonNode>> iter = src.fields();
         while (iter.hasNext()) {
             Map.Entry<String, JsonNode> entry = iter.next();
             node.add(entry.getKey(), jsonToNode(entry.getValue()));
         }
         return node;
     }
 
     public Node jsonToNode(JsonNode json) {
         if (json == null || json.isNull()) {
             return this.NULL;
         }
         if (json.isArray()) {
             return object((ArrayNode) json);
         }
         if (json.isObject()) {
             return object((ObjectNode) json);
         }
         if (!json.isValueNode()) {
             throw new IllegalArgumentException(String.format("Unknown type of JsonNode = '%s'", json.toString()));
         }
 
         if (json.isBoolean()) {
             return json.booleanValue() ? this.TRUE : this.FALSE;
         }
         if (json.isNumber()) {
             return number(json.decimalValue());
         }
         if (json.isTextual()) {
             return string(json.asText());
         }
         throw new IllegalArgumentException(String.format("Unknown type of JsonNode = '%s'", json.toString()));
     }
 
     public ObjectNode jsonObject() {
         return jackson.getNodeFactory().objectNode();
     }
 
     public ArrayNode jsonArray() {
         return jackson.getNodeFactory().arrayNode();
     }
 
 	public String toJsonString(JsonNode jsonNode) {
 		if (jsonNode.isBigDecimal()) {
 			BigDecimal number = ((DecimalNode) jsonNode).decimalValue();
 			return number.toPlainString();
 		} else if (jsonNode.isObject()) {
 			ObjectNode objNode = (ObjectNode) jsonNode;
 			StringBuilder sb = new StringBuilder();
 			sb.append("{");
 			if (objNode.size() != 0) {
 				Iterator<Map.Entry<String, JsonNode>> it = ((ObjectNode) jsonNode).fields();
 				int count = 0;
 				for (; it.hasNext();) {
 					Map.Entry<String, JsonNode> en = it.next();
 					//don't show entry with only undefined node
 					if (en.getValue().isObject() && ((ObjectNode) en.getValue()).size() == 0) {
 						continue;
 					}
 					if (count > 0) {
 						sb.append(",");
 					}
 					++count;
 
 					sb.append('"');
 					CharTypes.appendQuoted(sb, en.getKey());
 					sb.append('"');
 					sb.append(':');
 					sb.append(toJsonString(en.getValue()));
 				}
 			}
 			sb.append("}");
 			return sb.toString();
 		}
 		return jsonNode.toString();
 	}
 
     public StringHistoneNode string(JsonNode value) {
         return new StringHistoneNode(this, value.asText());
     }
 
     public StringHistoneNode string(String value) {
         return new StringHistoneNode(this, value);
     }
 
     public JsonNode jsonString(String value) {
         return jackson.getNodeFactory().textNode(value);
     }
 
     /**
      * Create number type object using specified value
      *
      * @param value value
      * @return number type object
      */
     public NumberHistoneNode number(BigDecimal value) {
         return new NumberHistoneNode(this, value);
     }
 
     /**
      * Create number type object using specified value
      *
      * @param value value
      * @return number type object
      */
     public NumberHistoneNode number(int value) {
         return new NumberHistoneNode(this, BigDecimal.valueOf(value));
     }
 
     /**
      * Create number type object using specified value
      *
      * @param value value
      * @return number type object
      * @throws NumberFormatException if {@code value} is infinite or NaN.
      */
     public NumberHistoneNode number(double value) {
         return new NumberHistoneNode(this, BigDecimal.valueOf(value));
     }
 
     public JsonNode jsonNumber(BigDecimal value) {
         return jackson.getNodeFactory().numberNode(value);
     }
 
     public JsonNode jsonNumber(BigInteger val) {
         return jackson.getNodeFactory().numberNode(val);
     }
 
     public JsonNode jsonNode(Reader reader) throws IOException {
         return jackson.readTree(reader);
     }
 
     public Node string(InputStream resourceStream) throws IOException {
         String content = IOUtils.toString(resourceStream, "UTF-8");
         return string(content);
     }
 
     public StringHistoneNode string() {
         return new StringHistoneNode(this, StringUtils.EMPTY);
     }
 
     public JsonNode jsonBoolean(Boolean value) {
         return jackson.getNodeFactory().booleanNode(value);
     }
 
     public JsonNode jsonNull() {
         return jackson.getNodeFactory().nullNode();
     }
 
     /**
      * Create AST node using node type and node items
      *
      * @param type  node type
      * @param items items to put into node
      * @return AST node
      */
     public ArrayNode jsonArray(int type, JsonNode... items) {
         ArrayNode result = jackson.createArrayNode();
         result.add(jackson.getNodeFactory().numberNode(type));
 
         fillArrayWithArgs(result, items);
 
         return result;
     }
 
     /**
      * Create JSON array using specified items
      *
      * @param items items to pout into array
      * @return JSON array
      */
     public ArrayNode jsonArray(JsonNode... items) {
         ArrayNode result = jackson.createArrayNode();
 
         fillArrayWithArgs(result, items);
 
         return result;
     }
 
     private void fillArrayWithArgs(ArrayNode result, Object[] items) {
         for (Object item : items) {
             if (item instanceof String) {
                 result.add(jackson.getNodeFactory().textNode((String) item));
             } else if (item instanceof Number) {
                 if (item instanceof BigDecimal) {
                     result.add(jackson.getNodeFactory().numberNode((BigDecimal) item));
                 } else if (item instanceof BigInteger) {
                     result.add(jackson.getNodeFactory().numberNode((BigInteger) item));
                 } else {
                     throw new RuntimeException("Type " + item.getClass() + " in unsupported");//TODO
                 }
             } else if (item instanceof JsonNode) {
                 result.add((JsonNode) item);
             } else if (item == null) {
                 result.add(jackson.getNodeFactory().nullNode());
             }
         }
     }
 
     public JsonNode removeLast(ArrayNode array) {
         JsonNode result = null;
 
         Iterator<JsonNode> iter = array.iterator();
         while (iter.hasNext()) {
             result = iter.next();
         }
         iter.remove();
 
         return result;
     }
 }
