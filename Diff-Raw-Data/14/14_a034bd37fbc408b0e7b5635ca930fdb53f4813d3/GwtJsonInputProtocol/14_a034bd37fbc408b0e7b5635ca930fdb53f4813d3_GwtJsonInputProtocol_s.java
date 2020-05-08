 /*******************************************************************************
  * Copyright (c) 2013, Minor Gordon
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in
  *       the documentation and/or other materials provided with the
  *       distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGE.
  ******************************************************************************/
 
 package org.thryft.protocol;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.util.Stack;
 
 import org.thryft.protocol.GwtJsonInputProtocol.NestedInputProtocol;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.googlecode.gwt.crypto.bouncycastle.util.encoders.Base64;
 
 public class GwtJsonInputProtocol extends
         JsonInputProtocol<NestedInputProtocol> {
     abstract class NestedInputProtocol extends AbstractInputProtocol {
         protected NestedInputProtocol(final JSONValue node) {
             myNode = node;
         }
 
         public abstract Type getType();
 
         @Override
         public byte[] readBinary() throws InputProtocolException {
             return Base64.decode(readString());
         }
 
         @Override
         public boolean readBool() throws InputProtocolException {
             return _readChildNode().isBoolean().booleanValue();
         }
 
         @Override
         public byte readByte() throws InputProtocolException {
             return (byte) _readChildNode().isNumber().doubleValue();
         }
 
         @Override
         public double readDouble() throws InputProtocolException {
             return _readChildNode().isNumber().doubleValue();
         }
 
         @Override
         public <E extends Enum<E>> E readEnum(final Class<E> enumClass)
                 throws InputProtocolException {
             throw new UnsupportedOperationException();
         }
 
         @Override
         public short readI16() throws InputProtocolException {
             return (short) _readChildNode().isNumber().doubleValue();
         }
 
         @Override
         public int readI32() throws InputProtocolException {
             return (int) _readChildNode().isNumber().doubleValue();
         }
 
         @Override
         public long readI64() throws InputProtocolException {
             return (long) _readChildNode().isNumber().doubleValue();
         }
 
         @Override
         public ListBegin readListBegin() throws InputProtocolException {
             final JSONArray node = _readChildNode().isArray();
             if (node == null) {
                 throw new InputProtocolException("expected JSON array");
             }
             _getInputProtocolStack().push(__createArrayInputProtocol(node));
             return new ListBegin(Type.VOID, node.size());
         }
 
         @Override
         public MapBegin readMapBegin() throws InputProtocolException {
             final JSONObject node = _readChildNode().isObject();
             if (node == null) {
                 throw new InputProtocolException("expected JSON object");
             }
             _getInputProtocolStack().push(__createMapObjectInputProtocol(node));
             return new MapBegin(Type.VOID, Type.VOID, node.size());
         }
 
         @Override
         public Object readMixed() throws InputProtocolException {
            throw new UnsupportedOperationException();
         }
 
         @Override
         public String readString() throws InputProtocolException {
             return _readChildNode().isString().stringValue();
         }
 
         @Override
         public String readStructBegin() throws InputProtocolException {
             final JSONObject node = _readChildNode().isObject();
             if (node == null) {
                 throw new InputProtocolException("expected JSON object");
             }
             _getInputProtocolStack().push(
                     __createStructObjectInputProtocol(node));
             return "";
         }
 
         protected JSONValue _getMyNode() {
             return myNode;
         }
 
         protected abstract JSONValue _readChildNode();
 
         private final JSONValue myNode;
     }
 
     private final class ArrayInputProtocol extends NestedInputProtocol {
         public ArrayInputProtocol(final JSONArray node) {
             super(node);
         }
 
         @Override
         public Type getType() {
             return Type.LIST;
         }
 
         @Override
         protected JSONArray _getMyNode() {
             return (JSONArray) super._getMyNode();
         }
 
         @Override
         protected JSONValue _readChildNode() {
             return _getMyNode().get(nextValueIndex++);
         }
 
         private int nextValueIndex = 0;
     }
 
     private final class MapObjectInputProtocol extends NestedInputProtocol {
         public MapObjectInputProtocol(final JSONObject node) {
             super(node);
             fieldNameStack.addAll(node.keySet());
         }
 
         @Override
         public Type getType() {
             return Type.MAP;
         }
 
         @Override
         protected JSONObject _getMyNode() {
             return (JSONObject) super._getMyNode();
         }
 
         @Override
         protected JSONValue _readChildNode() {
             if (nextReadIsKey) {
                 nextReadIsKey = false;
                 return new JSONString(fieldNameStack.peek());
             } else {
                 nextReadIsKey = true;
                 return _getMyNode().get(fieldNameStack.pop());
             }
         }
 
         private final Stack<String> fieldNameStack = new Stack<String>();
         private boolean nextReadIsKey = true;
     }
 
     private final class RootInputProtocol extends NestedInputProtocol {
         protected RootInputProtocol(final JSONValue node) {
             super(node);
         }
 
         @Override
         public Type getType() {
             throw new UnsupportedOperationException();
         }
 
         @Override
         protected JSONValue _readChildNode() {
             return _getMyNode();
         }
     }
 
     private final class StructObjectInputProtocol extends NestedInputProtocol {
         public StructObjectInputProtocol(final JSONObject node) {
             super(node);
             fieldNameStack.addAll(node.keySet());
         }
 
         @Override
         public Type getType() {
             return Type.STRUCT;
         }
 
         @Override
         public FieldBegin readFieldBegin() throws InputProtocolException {
             if (!fieldNameStack.isEmpty()) {
                 return new FieldBegin(fieldNameStack.peek(), Type.VOID,
                         (short) -1);
             } else {
                 return new FieldBegin();
             }
         }
 
         @Override
         public void readFieldEnd() throws InputProtocolException {
             fieldNameStack.pop();
         }
 
         @Override
         protected JSONObject _getMyNode() {
             return (JSONObject) super._getMyNode();
         }
 
         @Override
         protected JSONValue _readChildNode() {
             return _getMyNode().get(fieldNameStack.peek());
         }
 
         private final Stack<String> fieldNameStack = new Stack<String>();
     }
 
     public GwtJsonInputProtocol(final JSONValue rootNode) {
         this.rootNode = checkNotNull(rootNode);
         _getInputProtocolStack().push(__createRootInputProtocol(rootNode));
     }
 
     public GwtJsonInputProtocol(final String json) throws IOException {
         this(JSONParser.parseStrict(json));
     }
 
     @Override
     public Type getCurrentFieldType() {
         if (!_getInputProtocolStack().isEmpty()) {
             return _getInputProtocolStack().peek().getType();
         } else {
             return Type.VOID;
         }
     }
 
     @Override
     public void reset() {
         _getInputProtocolStack().clear();
         _getInputProtocolStack().push(__createRootInputProtocol(rootNode));
     }
 
     protected StructObjectInputProtocol __createStructObjectInputProtocol(
             final JSONObject node) {
         return new StructObjectInputProtocol(node);
     }
 
     private ArrayInputProtocol __createArrayInputProtocol(final JSONArray node) {
         return new ArrayInputProtocol(node);
     }
 
     private MapObjectInputProtocol __createMapObjectInputProtocol(
             final JSONObject node) {
         return new MapObjectInputProtocol(node);
     }
 
     private RootInputProtocol __createRootInputProtocol(
             final JSONValue parsedTree) {
         return new RootInputProtocol(parsedTree);
     }
 
     private final JSONValue rootNode;
 }
