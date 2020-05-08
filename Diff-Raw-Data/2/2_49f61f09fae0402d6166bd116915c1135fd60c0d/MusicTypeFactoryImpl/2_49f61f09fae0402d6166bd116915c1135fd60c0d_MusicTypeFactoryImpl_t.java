 /**
  * Copyright 2011 Alex Jones
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  *
  * @author unclealex72
  *
  */
 
 package uk.co.unclealex.music.common;
 
 import com.google.common.base.Function;
 
 /**
  * The default implementation of {@link MusicTypeFactory}.
  * 
  * @author alex
  * 
  */
 public class MusicTypeFactoryImpl implements MusicTypeFactory {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public MusicType createFlacType() {
 		return new FlacTypeImpl();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public MusicType createOggType() {
 		return new OggTypeImpl();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public MusicType createMp3Type() {
 		return new Mp3TypeImpl();
 	}
 
 	class CompareFunction implements Function<MusicType, Class<? extends MusicType>> {
 
 		@Override
 		public Class<? extends MusicType> apply(MusicType musicType) {
 			class Visitor extends MusicTypeVisitor.Default<Class<? extends MusicType>> {
 				@Override
 				public Class<? extends MusicType> visit(FlacType flacType) {
 					return FlacType.class;
 				}
 
 				@Override
 				public Class<? extends MusicType> visit(OggType oggType) {
 					return OggType.class;
 				}
 
 				@Override
 				public Class<? extends MusicType> visit(Mp3Type mp3Type) {
 					return Mp3Type.class;
 				}
 			}
 			Visitor visitor = new Visitor();
 			return musicType.accept(visitor);
 		}
 	}
 
 	abstract class AbstractMusicType {
 
 		/**
 		 * The file extension for this music type.
 		 */
 		private String extension;
 		
 		public AbstractMusicType(String extension) {
 			super();
 			this.extension = extension;
 		}
 
 		@Override
 		public int hashCode() {
 			return getExtension().hashCode();
 		}
 
 		public boolean equals(Object obj) {
			return (obj instanceof MusicType) && getExtension().equals(((MusicType) obj).getExtension());
 		}
 
 		public int compareTo(MusicType o) {
 			return getExtension().compareTo(o.getExtension());
 		}
 
 		@Override
 		public String toString() {
 			return getExtension();
 		}
 		
 		public String getExtension() {
 			return extension;
 		}
 	}
 
 	class FlacTypeImpl extends AbstractMusicType implements FlacType {
 
 		public FlacTypeImpl() {
 			super("flac");
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public <E> E accept(MusicTypeVisitor<E> musicTypeVisitor) {
 			return musicTypeVisitor.visit(this);
 		}
 	}
 
 	class OggTypeImpl extends AbstractMusicType implements OggType {
 
 		public OggTypeImpl() {
 			super("ogg");
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public <E> E accept(MusicTypeVisitor<E> musicTypeVisitor) {
 			return musicTypeVisitor.visit(this);
 		}
 	}
 	
 	class Mp3TypeImpl extends AbstractMusicType implements Mp3Type {
 
 		public Mp3TypeImpl() {
 			super("mp3");
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public <E> E accept(MusicTypeVisitor<E> musicTypeVisitor) {
 			return musicTypeVisitor.visit(this);
 		}
 	}
 
 }
