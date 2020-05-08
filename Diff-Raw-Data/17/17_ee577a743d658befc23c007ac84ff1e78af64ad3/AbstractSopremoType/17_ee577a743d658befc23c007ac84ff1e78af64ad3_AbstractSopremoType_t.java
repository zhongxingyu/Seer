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
 package eu.stratosphere.sopremo;
 
 import java.io.IOException;
 
 import com.esotericsoftware.kryo.Kryo;
 
 /**
  * Provides basic implementations of the required methods of {@link SopremoType}
  * 
  * @author Arvid Heise
  */
 public abstract class AbstractSopremoType implements ISopremoType {
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return toString(this);
 	}
 
 	private final static ThreadLocal<Kryo> CloneHelper = new ThreadLocal<Kryo>() {
 		/* (non-Javadoc)
 		 * @see java.lang.ThreadLocal#initialValue()
 		 */
 		@Override
 		protected Kryo initialValue() {
			Kryo kryo = new Kryo();kryo.setReferences(false);
			return kryo;
 		}
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public AbstractSopremoType clone() {
 		return getKryo().copy(this);
 		// kryo 2.20 makes fancy stuff such as caching of clones - we need more control
 		// if (this instanceof KryoCopyable<?>)
 		// return ((KryoCopyable<AbstractSopremoType>) this).copy(CloneHelper);
 		// final Serializer<AbstractSopremoType> serializer = CloneHelper.getSerializer(getClass());
 		// return serializer.copy(CloneHelper, this);
 	}
 
 	/**
 	 * Unreliable API - subject to change
 	 */
 	protected Kryo getKryo() {
 		return CloneHelper.get();
 	}
 
 	protected void checkCopyType(AbstractSopremoType copy) {
 		if (copy.getClass() != this.getClass())
 			throw new AssertionError(String.format("Create copy returned wrong type. Expected %s but was %s",
 				this.getClass(), copy.getClass()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	public AbstractSopremoType shallowClone() {
 		return getKryo().copyShallow(this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public final <T extends AbstractSopremoType> T copy() {
 		return (T) this.clone();
 	}
 
 	/**
 	 * Returns a string representation of the given {@link SopremoType}.
 	 * 
 	 * @param type
 	 *        the SopremoType that should be used
 	 * @return the string representation
 	 */
 	public static String toString(final ISopremoType type) {
 		final StringBuilder builder = new StringBuilder();
 		try {
 			type.appendAsString(builder);
 		} catch (IOException e) {
 			// cannot happen
 		}
 		return builder.toString();
 	}
 }
