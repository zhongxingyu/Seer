 /***********************************************************************************************************************
  *
  * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 package eu.stratosphere.sopremo.serialization;
 
 import it.unimi.dsi.fastutil.bytes.ByteArrayList;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.IdentityHashMap;
 import java.util.Map;
 import java.util.SortedSet;
 
 import com.esotericsoftware.kryo.DefaultSerializer;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.KryoCopyable;
 import com.esotericsoftware.kryo.Registration;
 import com.esotericsoftware.kryo.Serializer;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 import eu.stratosphere.nephele.types.Record;
 import eu.stratosphere.sopremo.AbstractSopremoType;
 import eu.stratosphere.sopremo.ISopremoType;
 import eu.stratosphere.sopremo.SopremoEnvironment;
 import eu.stratosphere.sopremo.cache.NodeCache;
 import eu.stratosphere.sopremo.expressions.EvaluationExpression;
 import eu.stratosphere.sopremo.pact.SopremoUtil;
 import eu.stratosphere.sopremo.type.CachingArrayNode;
 import eu.stratosphere.sopremo.type.IArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.IObjectNode;
 import eu.stratosphere.sopremo.type.MissingNode;
 import eu.stratosphere.sopremo.type.ObjectNode;
 import eu.stratosphere.sopremo.type.ReusingSerializer;
 import eu.stratosphere.sopremo.type.typed.TypedObjectNode;
 
 /**
  * @author Arvid Heise
  */
 @DefaultSerializer(value = SopremoRecord.SopremoRecordKryoSerializer.class)
 public final class SopremoRecord extends AbstractSopremoType implements ISopremoType,
 		KryoCopyable<SopremoRecord>, Record {
 
 	/**
 	 * 
 	 */
 	private static final int MISSING = -1;
 
 	private final transient ByteArrayList binaryRepresentation = new ByteArrayList();
 
 	private final transient Input input = new Input();
 
 	private final transient Output output = new Output(new OutputStream() {
 		@Override
 		public void write(byte[] b, int off, int len) throws IOException {
 			SopremoRecord.this.binaryRepresentation.addElements(SopremoRecord.this.binaryRepresentation.size(), b, off,
 				len);
 		};
 
 		@Override
 		public void write(byte[] b) throws IOException {
 			SopremoRecord.this.binaryRepresentation.addElements(SopremoRecord.this.binaryRepresentation.size(), b);
 		};
 
 		@Override
 		public void write(int b) throws IOException {
 			SopremoRecord.this.binaryRepresentation.add((byte) b);
 		}
 	});
 
 	private final transient NodeCache nodeCache = new NodeCache(CachingNodeFactory.getInstance());
 
 	private transient IJsonNode node;
 
 	private final transient Kryo kryo;
 
 	private final transient int offsets[];
 
 	private final transient Map<Class<? extends IJsonNode>, NodeSerializer<IJsonNode>> serializers =
 		new IdentityHashMap<Class<? extends IJsonNode>, NodeSerializer<IJsonNode>>();
 
 	private final transient Map<Class<? extends IJsonNode>, NodeDeserializer<IJsonNode>> deserializers =
 		new IdentityHashMap<Class<? extends IJsonNode>, NodeDeserializer<IJsonNode>>();
 
 	/**
 	 * Initializes SopremoRecord.
 	 */
 	SopremoRecord() {
 		this(SopremoRecordLayout.EMPTY);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public SopremoRecord(SopremoRecordLayout layout) {
 		this.layout = layout;
 		this.offsets = new int[layout.getNumKeys()];
 		this.kryo = SopremoEnvironment.getInstance().getEvaluationContext().getKryo();
 
 		this.serializers.put(IObjectNode.class, (NodeSerializer) new ObjectSerializer());
 		this.serializers.put(IArrayNode.class, (NodeSerializer) new ArraySerializer());
 		this.serializers.put(IJsonNode.class, new PrimitiveSerializer());
 		this.deserializers.put(IObjectNode.class, (NodeDeserializer) new ObjectSerializer());
 		this.deserializers.put(IArrayNode.class, (NodeDeserializer) new CachingArrayDeserializer());
 		this.deserializers.put(IJsonNode.class, new PrimitiveSerializer());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.ISopremoType#appendAsString(java.lang.Appendable)
 	 */
 	@Override
 	public void appendAsString(Appendable appendable) throws IOException {
 		getNode().appendAsString(appendable);
 	}
 
 	IJsonNode getNodeDirectly() {
 		return this.node;
 	}
 
 	/**
 	 * Returns the node.
 	 * 
 	 * @return the node
 	 */
 	public IJsonNode getNode() {
 		if (this.node == null) {
 			this.input.setBuffer(this.binaryRepresentation.elements(), 0, this.binaryRepresentation.size());
 			final IJsonNode readNode = readRecursively(this.node);
 			final TypedObjectNode typedNode = this.layout.getTypedNode();
 			if (typedNode != null) {
 				this.node = typedNode;
 				typedNode.setBackingNode((IObjectNode) readNode);
 			} else
 				this.node = readNode;
 		}
 		return this.node;
 	}
 
 	@SuppressWarnings("unchecked")
 	private IJsonNode readRecursively(final IJsonNode possibleTarget) {
 		final Registration registration = this.kryo.readClass(this.input);
 		final Class<IJsonNode> type = registration.getType();
 		return this.getDeserializer(type).read(
 			possibleTarget == null || possibleTarget.getType() != type ? null : possibleTarget, registration);
 	}
 
 	@SuppressWarnings("unchecked")
 	private IJsonNode readRecursively(final NodeCache nodeCache) {
 		final Registration registration = this.kryo.readClass(this.input);
 		final Class<IJsonNode> type = registration.getType();
 		return this.getDeserializer(type).read(nodeCache.getNode(type), registration);
 	}
 
 	// private Class<? extends IJsonNode> getImplementation(Class<? extends IJsonNode> interfaceType) {
 	// if (interfaceType == IObjectNode.class)
 	// return ObjectNode.class;
 	// if (interfaceType == IArrayNode.class)
 	// return ArrayNode.class;
 	// return interfaceType;
 	// }
 
 	/**
 	 * Sets the node to the specified value.
 	 * 
 	 * @param node
 	 *        the node to set
 	 */
 	public void setNode(IJsonNode node) {
 		if (node == null)
 			throw new NullPointerException("node must not be null");
 
 		this.node = node;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.nephele.types.Record#write(java.io.DataOutput)
 	 */
 	@Override
 	public void write(DataOutput out) throws IOException {
 		if (this.node != null) {
 			Arrays.fill(this.offsets, MISSING);
 			this.binaryRepresentation.clear();
 			writeRecursivelyToBuffer(this.node, this.layout.getExpressionIndex());
 			this.output.flush();
 			final EvaluationExpression[] calculatedKeyExpressions = this.layout.getCalculatedKeyExpressions();
 			for (int index = 0; index < calculatedKeyExpressions.length; index++) {
 				this.offsets[index + this.layout.getNumDirectDataKeys()] = this.binaryRepresentation.size();
 				final IJsonNode calculatedValue = calculatedKeyExpressions[index].evaluate(this.node);
 				this.kryo.writeClass(this.output, calculatedValue.getType());
 				this.kryo.writeObject(this.output, calculatedValue);
 				this.output.flush();
 			}
 		} else if (SopremoUtil.DEBUG && this.binaryRepresentation.size() == 0)
 			throw new IllegalStateException("Attempt to write zero length binary representation");
 
 		for (int index = 0; index < this.offsets.length; index++) {
 			if (SopremoUtil.DEBUG && this.offsets[index] == 0)
 				throw new IllegalStateException();
 			out.writeInt(this.offsets[index]);
 		}
 		final int size = this.binaryRepresentation.size();
 		out.writeInt(size);
 		out.write(this.binaryRepresentation.elements(), 0, size);
 	}
 
 	void write(Output out) {
 		if (this.node != null) {
 			Arrays.fill(this.offsets, MISSING);
 			writeRecursivelyToBuffer(this.node, this.layout.getExpressionIndex());
 			this.output.flush();
 			
 			final EvaluationExpression[] calculatedKeyExpressions = this.layout.getCalculatedKeyExpressions();
 			for (int index = 0; index < calculatedKeyExpressions.length; index++) {
 				this.offsets[index + this.layout.getNumDirectDataKeys()] = this.binaryRepresentation.size();
 				final IJsonNode calculatedValue = calculatedKeyExpressions[index].evaluate(this.node);
 				this.kryo.writeClass(this.output, calculatedValue.getType());
 				this.kryo.writeObject(this.output, calculatedValue);
 				this.output.flush();
 			}
 		} else if (SopremoUtil.DEBUG && this.binaryRepresentation.size() == 0)
 			throw new IllegalStateException("Attempt to write zero length binary representation");
 
 
 		for (int index = 0; index < this.offsets.length; index++) {
 			if (SopremoUtil.DEBUG && this.offsets[index] == 0)
 				throw new IllegalStateException();
 			out.writeInt(this.offsets[index], true);
 		}
 		final int size = this.binaryRepresentation.size();
 		out.writeInt(size, true);
 		out.write(this.binaryRepresentation.elements(), 0, size);
 	}
 
 	private void writeRecursivelyToBuffer(final IJsonNode node, ExpressionIndex expressionIndex) {
 		NodeSerializer<IJsonNode> serializer = getSerializer(node.getType());
 		SopremoRecord.this.kryo.writeClass(SopremoRecord.this.output, node.getType());
 		if (node instanceof TypedObjectNode)
 			serializer.write(((TypedObjectNode) node).getBackingNode(), expressionIndex);
 		else
 			serializer.write(node, expressionIndex);
 	}
 
 	/**
 	 * @param node2
 	 * @return
 	 */
 	private NodeSerializer<IJsonNode> getSerializer(final Class<? extends IJsonNode> type) {
 		final NodeSerializer<IJsonNode> serializer = this.serializers.get(type);
 		if (serializer == null) {
 			final NodeSerializer<IJsonNode> defaultSerializer = this.serializers.get(IJsonNode.class);
 			this.serializers.put(type, defaultSerializer);
 			return defaultSerializer;
 		}
 		return serializer;
 	}
 
 	private NodeDeserializer<IJsonNode> getDeserializer(final Class<? extends IJsonNode> type) {
 		final NodeDeserializer<IJsonNode> deserializer = this.deserializers.get(type);
 		if (deserializer == null) {
 			final NodeDeserializer<IJsonNode> defaultSerializer = this.deserializers.get(IJsonNode.class);
 			this.deserializers.put(type, defaultSerializer);
 			return defaultSerializer;
 		}
 		return deserializer;
 	}
 
 	private static interface NodeSerializer<T extends IJsonNode> {
 		public void write(T node, ExpressionIndex expressionIndex);
 	}
 
 	private static interface NodeDeserializer<T extends IJsonNode> {
 		public T read(T node, Registration registration);
 	}
 
 	private int position() {
		return this.binaryRepresentation.size() + this.output.position();
 	}
 
 	private class ObjectSerializer implements NodeSerializer<IObjectNode>, NodeDeserializer<IObjectNode> {
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.serialization.SopremoRecord.NodeSerializer#write(eu.stratosphere.sopremo.type.IJsonNode
 		 * ,
 		 * java.util.List)
 		 */
 		@Override
 		public void write(IObjectNode node, ExpressionIndex expressionIndex) {
 			final SortedSet<String> fieldNames = node.getFieldNames();
 			SopremoRecord.this.output.writeInt(fieldNames.size());
 			for (String fieldName : fieldNames) {
 				SopremoRecord.this.output.writeString(fieldName);
 				final ExpressionIndex subIndex;
 				if (expressionIndex != null) {
 					subIndex = expressionIndex.subIndex(fieldName);
 					if (subIndex != null && subIndex.getExpression() != null)
 						SopremoRecord.this.offsets[subIndex.getKeyIndex()] = position();
 				} else
 					subIndex = null;
 				writeRecursivelyToBuffer(node.get(fieldName), subIndex);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.serialization.SopremoRecord.NodeSerializer#read(eu.stratosphere.sopremo.type.IJsonNode
 		 * , com.esotericsoftware.kryo.Registration)
 		 */
 		@Override
 		public IObjectNode read(IObjectNode target, Registration registration) {
 			if (target != null)
 				target.clear();
 			else
 				target = new ObjectNode();
 
 			int size = SopremoRecord.this.input.readInt();
 			for (int index = 0; index < size; index++) {
 				final String key = SopremoRecord.this.input.readString();
 				// add caching
 				target.put(key, readRecursively((IJsonNode) null));
 			}
 			return target;
 		}
 	}
 
 	private class CachingArrayDeserializer implements NodeDeserializer<CachingArrayNode<IJsonNode>> {
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.serialization.SopremoRecord.NodeSerializer#read(eu.stratosphere.sopremo.type.IJsonNode
 		 * , com.esotericsoftware.kryo.Registration)
 		 */
 		@Override
 		public CachingArrayNode<IJsonNode> read(CachingArrayNode<IJsonNode> target, Registration registration) {
 			if (target != null)
 				target.clear();
 			else
 				target = new CachingArrayNode<IJsonNode>();
 
 			int size = SopremoRecord.this.input.readInt();
 			target.clear();
 			for (int index = 0; index < size; index++)
 				target.add(readRecursively(target.getUnusedNode()));
 			return target;
 		}
 	}
 
 	private class ArraySerializer implements NodeSerializer<IArrayNode<IJsonNode>> {
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.serialization.SopremoRecord.NodeSerializer#write(eu.stratosphere.sopremo.type.IJsonNode
 		 * ,
 		 * java.util.List)
 		 */
 		@Override
 		public void write(IArrayNode<IJsonNode> node, ExpressionIndex expressionIndex) {
 			final int size = node.size();
 			SopremoRecord.this.output.writeInt(size);
 			for (int index = 0; index < size; index++) {
 				final ExpressionIndex subIndex;
 				if (expressionIndex != null) {
 					subIndex = getSubIndex(expressionIndex, size, index);
 					if (subIndex != null && subIndex.getExpression() != null)
 						SopremoRecord.this.offsets[subIndex.getKeyIndex()] = position();
 				} else
 					subIndex = null;
 				writeRecursivelyToBuffer(node.get(index), subIndex);
 			}
 		}
 
 		private ExpressionIndex getSubIndex(ExpressionIndex expressionIndex, final int size, int index) {
 			final ExpressionIndex subIndex = expressionIndex.get(index);
 			if (subIndex != null)
 				return subIndex;
 			return expressionIndex.get(index - size);
 		}
 	}
 
 	private class PrimitiveSerializer implements NodeSerializer<IJsonNode>, NodeDeserializer<IJsonNode> {
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.serialization.SopremoRecord.NodeSerializer#write(eu.stratosphere.sopremo.type.IJsonNode
 		 * , eu.stratosphere.sopremo.serialization.ExpressionIndex)
 		 */
 		@Override
 		public void write(IJsonNode node, ExpressionIndex expressionIndex) {
 			SopremoRecord.this.kryo.writeObject(SopremoRecord.this.output, node);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see
 		 * eu.stratosphere.sopremo.serialization.SopremoRecord.NodeSerializer#read(eu.stratosphere.sopremo.type.IJsonNode
 		 * , com.esotericsoftware.kryo.Registration)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public IJsonNode read(IJsonNode target, Registration registration) {
 			final Serializer<IJsonNode> serializer = registration.getSerializer();
 			if (target != null && serializer instanceof ReusingSerializer<?> &&
 				registration.getType() == target.getClass())
 				return ((ReusingSerializer<IJsonNode>) serializer).read(SopremoRecord.this.kryo,
 					SopremoRecord.this.input, target, registration.getType());
 			return serializer.read(SopremoRecord.this.kryo, SopremoRecord.this.input, registration.getType());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.nephele.types.Record#read(java.io.DataInput)
 	 */
 	@Override
 	public void read(DataInput in) throws IOException {
 		this.node = null;
 		for (int index = 0; index < this.offsets.length; index++) {
 			this.offsets[index] = in.readInt();
 			if (SopremoUtil.DEBUG && this.offsets[index] == 0)
 				throw new IllegalStateException("Attempt to read zero offset");
 		}
 
 		final int size = in.readInt();
 		if (SopremoUtil.DEBUG && size <= 0)
 			throw new IllegalStateException("Attempt to read zero length binary representation");
 		this.binaryRepresentation.size(size);
 		in.readFully(this.binaryRepresentation.elements(), 0, size);
 	}
 
 	void read(Input in) {
 		this.node = null;
 		for (int index = 0; index < this.offsets.length; index++) {
 			this.offsets[index] = in.readInt(true);
 			if (SopremoUtil.DEBUG && this.offsets[index] == 0)
 				throw new IllegalStateException("Attempt to read zero offset");
 		}
 
 		final int size = in.readInt(true);
 		if (SopremoUtil.DEBUG && size <= 0)
 			throw new IllegalStateException("Attempt to read zero length binary representation");
 		this.binaryRepresentation.size(size);
 		in.read(this.binaryRepresentation.elements(), 0, size);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.esotericsoftware.kryo.KryoCopyable#copy(com.esotericsoftware.kryo.Kryo)
 	 */
 	@Override
 	public SopremoRecord copy(Kryo kryo) {
 		final SopremoRecord sopremoRecord = new SopremoRecord();
 		if (this.node == null)
 			sopremoRecord.node = null;
 		else
 			sopremoRecord.node = SopremoUtil.copyInto(this.node, this.nodeCache);
 		sopremoRecord.binaryRepresentation.clear();
 		sopremoRecord.binaryRepresentation.addAll(this.binaryRepresentation);
 		return sopremoRecord;
 	}
 
 	public IJsonNode getKey(EvaluationExpression expression, IJsonNode target) {
 		if (this.node == null) {
 			int offset = getKeyOffset(this.layout.getKeyIndex(expression));
 			if (offset < 0)
 				return MissingNode.getInstance();
 			return getValueAtOffset(offset, target);
 		}
 		return expression.evaluate(this.node);
 	}
 
 	public IJsonNode getKey(EvaluationExpression expression, NodeCache nodeCache) {
 		if (this.node == null) {
 			int offset = getKeyOffset(this.layout.getKeyIndex(expression));
 			if (offset == MISSING)
 				return MissingNode.getInstance();
 			return getValueAtOffset(offset, nodeCache);
 		}
 		return expression.evaluate(this.node);
 	}
 
 	public IJsonNode getKey(int expressionIndex, IJsonNode target) {
 		if (this.node == null) {
 			int offset = getKeyOffset(expressionIndex);
 			if (offset == MISSING)
 				return MissingNode.getInstance();
 			return getValueAtOffset(offset, target);
 		}
 		return this.layout.getExpression(expressionIndex).evaluate(this.node);
 	}
 
 	public IJsonNode getKey(int expressionIndex, NodeCache nodeCache) {
 		if (this.node == null) {
 			int offset = getKeyOffset(expressionIndex);
 			if (offset == MISSING)
 				return MissingNode.getInstance();
 			return getValueAtOffset(offset, nodeCache);
 		}
 		return this.layout.getExpression(expressionIndex).evaluate(this.node);
 	}
 
 	public int getKeyOffset(int expressionIndex) {
 		if (expressionIndex == SopremoRecordLayout.VALUE_INDEX)
 			return 0;
 		return this.offsets[expressionIndex];
 	}
 
 	public IJsonNode getValueAtOffset(int offset, IJsonNode target) {
 		if (offset == 0)
 			return getNode();
 		this.input.setBuffer(this.binaryRepresentation.elements(), offset, this.binaryRepresentation.size());
 		return readRecursively(target);
 	}
 
 	public IJsonNode getValueAtOffset(int offset, NodeCache nodeCache) {
 		if (offset == 0)
 			return getNode();
 		this.input.setBuffer(this.binaryRepresentation.elements(), offset, this.binaryRepresentation.size());
 		return readRecursively(nodeCache);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + this.layout.hashCode();
 		result = prime * result + getNode().hashCode();
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SopremoRecord other = (SopremoRecord) obj;
 		return this.layout.equals(other.layout) && getNode().equals(other.getNode());
 	}
 
 	public static class SopremoRecordKryoSerializer<Node extends IJsonNode> extends
 			ReusingSerializer<SopremoRecord> {
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#write(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Output, java.lang.Object)
 		 */
 		@Override
 		public void write(Kryo kryo, Output output, SopremoRecord object) {
 			kryo.writeObject(output, object.layout);
 			object.write(output);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see eu.stratosphere.sopremo.type.ReusingSerializer#read(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Input, java.lang.Object, java.lang.Class)
 		 */
 		@Override
 		public SopremoRecord read(Kryo kryo, Input input, SopremoRecord oldInstance,
 				Class<SopremoRecord> type) {
 			final SopremoRecordLayout layout = kryo.readObject(input, SopremoRecordLayout.class);
 			final SopremoRecord record;
 			if (oldInstance.layout.equals(layout))
 				record = oldInstance;
 			else
 				record = new SopremoRecord(layout);
 			record.read(input);
 			return record;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#read(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Input, java.lang.Class)
 		 */
 		@Override
 		public SopremoRecord read(Kryo kryo, Input input, Class<SopremoRecord> type) {
 			final SopremoRecordLayout layout = kryo.readObject(input, SopremoRecordLayout.class);
 			final SopremoRecord record = new SopremoRecord(layout);
 			record.read(input);
 			return record;
 		}
 
 	}
 
 	private transient SopremoRecordLayout layout;
 
 	/**
 	 * Returns the layout.
 	 * 
 	 * @return the layout
 	 */
 	public SopremoRecordLayout getLayout() {
 		return this.layout;
 	}
 
 	/**
 	 * Sets the layout to the specified value.
 	 * 
 	 * @param layout
 	 *        the layout to set
 	 */
 	public void setLayout(SopremoRecordLayout layout) {
 		if (layout == null)
 			throw new NullPointerException("layout must not be null");
 
 		this.layout = layout;
 	}
 
 	/**
 	 * @param to
 	 */
 	public void copyTo(SopremoRecord to) {
 		if (this.binaryRepresentation.size() > 0) {
 			to.binaryRepresentation.clear();
 			to.binaryRepresentation.addElements(0, this.binaryRepresentation.elements(), 0,
 				this.binaryRepresentation.size());
 			System.arraycopy(this.offsets, 0, to.offsets, 0, this.offsets.length);
 			to.node = null;
 		} else {
 			to.binaryRepresentation.clear();
 			to.node = this.node.clone();
 		}
 	}
 
 	public IJsonNode getKey(EvaluationExpression expression) {
 		return getKey(expression, (IJsonNode) null);
 	}
 
 }
