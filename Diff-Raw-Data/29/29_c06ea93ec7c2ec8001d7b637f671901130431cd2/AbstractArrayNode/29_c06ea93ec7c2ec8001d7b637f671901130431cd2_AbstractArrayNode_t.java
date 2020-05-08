 /***********************************************************************************************************************
  *
  * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
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
 package eu.stratosphere.sopremo.type;
 
 import java.io.IOException;
 import java.util.AbstractList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import com.esotericsoftware.kryo.DefaultSerializer;
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.KryoCopyable;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 import eu.stratosphere.sopremo.cache.ArrayCache;
 import eu.stratosphere.sopremo.pact.SopremoUtil;
 import eu.stratosphere.util.AppendUtil;
 
 /**
  * @author Arvid Heise
  */
 @DefaultSerializer(AbstractArrayNode.ArraySerializer.class)
 public abstract class AbstractArrayNode<T extends IJsonNode> extends AbstractJsonNode implements IArrayNode<T>,
 		KryoCopyable<AbstractArrayNode<T>> {
 
 	/**
 	 * Initializes AbstractArrayNode.
 	 */
 	public AbstractArrayNode() {
 		super();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void copyValueFrom(final IJsonNode otherNode) {
 		this.checkForSameType(otherNode);
 		IArrayNode<T> array = (IArrayNode<T>) otherNode;
 		int index = 0;
 		// try to reuse existing nodes
 		for (int length = Math.max(this.size(), array.size()); index < length; index++) {
 			final IJsonNode existingNode = this.get(index);
 			final IJsonNode newNode = array.get(index);
 			if (existingNode.getType() == newNode.getType())
 				existingNode.copyValueFrom(newNode);
 			else
 				this.set(index, (T) newNode.clone());
 		}
 
 		for (int length = array.size(); index < length; index++)
 			this.add((T) array.get(0).clone());
 		for (int length = this.size(); index < length; length--)
 			this.remove(index);
 	}
 
 	// /* (non-Javadoc)
 	// * @see com.esotericsoftware.kryo.KryoSerializable#write(com.esotericsoftware.kryo.Kryo,
 	// com.esotericsoftware.kryo.io.Output)
 	// */
 	// @Override
 	// public void write(Kryo kryo, Output output) {
 	// final int size = size();
 	// output.write(size);
 	// for (int index = 0; index < size(); index++)
 	// SopremoUtil.writeNode(kryo, output, get(index));
 	// }
 	//
 	// /* (non-Javadoc)
 	// * @see com.esotericsoftware.kryo.KryoSerializable#read(com.esotericsoftware.kryo.Kryo,
 	// com.esotericsoftware.kryo.io.Input)
 	// */
 	// @Override
 	// public void read(Kryo kryo, Input input) {
 	// final int size = input.readInt();
 	// clear();
 	// for (int index = 0; index < size(); index++)
 	// add(SopremoUtil.readNode(kryo, input, null));
 	// }
 
 	@Override
 	public IArrayNode<T> addAll(final Iterable<? extends T> it) {
 		for (final T jsonNode : it)
 			this.add(jsonNode);
 		return this;
 	}
 
 	@Override
 	public IArrayNode<T> addAll(final T[] nodes) {
 		this.addAll(Arrays.asList(nodes));
 		return this;
 	}
 
 	@Override
 	public boolean contains(T node) {
 		for (final IJsonNode element : this)
 			if (node.equals(element))
 				return true;
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.type.AbstractJsonNode#clone()
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public AbstractArrayNode<T> clone() {
 		return (AbstractArrayNode<T>) super.clone();
 	}
 
 	public List<T> asList() {
 		return new AbstractList<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return this.iterator();
 			}
 
 			@Override
 			public int size() {
 				return AbstractArrayNode.this.size();
 			}
 
 			@Override
 			public T get(int index) {
 				return AbstractArrayNode.this.get(index);
 			}
 		};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see eu.stratosphere.sopremo.type.IJsonNode#getType()
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public Class<IArrayNode<T>> getType() {
 		return (Class) IArrayNode.class;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setSize(final int len) {
 		for (int i = len, size = size(); i < size; i++)
 			this.set(i, (T) MissingNode.getInstance());
 	}
 
 	public static final class ArraySerializer extends ReusingSerializer<AbstractArrayNode<?>> {
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#write(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Output, java.lang.Object)
 		 */
 		@Override
 		public void write(Kryo kryo, Output output, AbstractArrayNode<?> array) {
 			output.writeInt(array.size());
 
 			for (IJsonNode entry : array)
 				kryo.writeClassAndObject(output, entry);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see com.esotericsoftware.kryo.Serializer#read(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Input, java.lang.Class)
 		 */
 		@Override
 		public AbstractArrayNode<?> read(Kryo kryo, Input input, Class<AbstractArrayNode<?>> type) {
 			final int len = input.readInt();
 
			@SuppressWarnings("unchecked")
			AbstractArrayNode<IJsonNode> array = (AbstractArrayNode<IJsonNode>) kryo.newInstance(type);
 			for (int i = 0; i < len; i++)
 				array.add((IJsonNode) kryo.readClassAndObject(input));
 			return array;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * @see eu.stratosphere.sopremo.type.ReusingSerializer#read(com.esotericsoftware.kryo.Kryo,
 		 * com.esotericsoftware.kryo.io.Input, java.lang.Object, java.lang.Class)
 		 */
 		@Override
 		public AbstractArrayNode<?> read(Kryo kryo, Input input, AbstractArrayNode<?> oldInstance,
 				Class<AbstractArrayNode<?>> type) {
 			if (oldInstance == null)
 				return read(kryo, input, type);
 
 			final int len = input.readInt();
 			@SuppressWarnings("unchecked")
 			ArrayNode<IJsonNode> array = (ArrayNode<IJsonNode>) oldInstance;
 
 			for (int i = 0; i < len; i++)
 				array.set(i, SopremoUtil.deserializeInto(kryo, input, array.get(i)));
 
 			array.setSize(len);
 			return array;
 		}
 	}
 
 	@Override
 	public T[] toArray(ArrayCache<T> arrayCache) {
 		T[] result = arrayCache.getArray(this.size());
 		this.fillArray(result);
 		return result;
 	}
 
 	protected void fillArray(IJsonNode[] result) {
 		int i = 0;
 		for (final IJsonNode node : this)
 			result[i++] = node;
 	}
 
 	@Override
 	public int compareToSameType(final IJsonNode other) {
 		@SuppressWarnings("unchecked")
 		final IArrayNode<T> node = (IArrayNode<T>) other;
 		final Iterator<T> entries1 = this.iterator(), entries2 = node.iterator();
 
 		while (entries1.hasNext() && entries2.hasNext()) {
 			final IJsonNode entry1 = entries1.next(), entry2 = entries2.next();
 			final int comparison = entry1.compareTo(entry2);
 			if (comparison != 0)
 				return comparison;
 		}
 
 		if (entries1.hasNext())
 			return 1;
 		if (entries2.hasNext())
 			return -1;
 		return 0;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 41;
 		int hashCode = prime;
 		for (IJsonNode node : this)
 			hashCode = (hashCode + node.hashCode()) * prime;
 		return prime;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof IArrayNode))
 			return false;
 
 		final Iterator<T> thisIter = this.iterator(), thatIter = ((IArrayNode<T>) obj).iterator();
 		while (thisIter.hasNext() && thatIter.hasNext())
 			if (!thisIter.next().equals(thatIter.next()))
 				return false;
 		return thisIter.hasNext() == thatIter.hasNext();
 	}
 
 	@Override
 	public void appendAsString(final Appendable appendable) throws IOException {
 		AppendUtil.append(appendable, this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.esotericsoftware.kryo.KryoCopyable#copy(com.esotericsoftware.kryo.Kryo)
 	 */
 	@Override
 	public AbstractArrayNode<T> copy(Kryo kryo) {
 		final ArrayNode<T> node = new ArrayNode<T>();
 		node.copyValueFrom(this);
 		return node;
 	}
 }
