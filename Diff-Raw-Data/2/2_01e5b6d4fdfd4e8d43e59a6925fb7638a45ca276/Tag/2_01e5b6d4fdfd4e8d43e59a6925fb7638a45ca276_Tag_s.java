 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.missive.core;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.missive.util.ClassUtil;
 
 /**
  * 
  * @author Gavin M Litchfield
  * 
  * @param <V>
  *            The class type of the value this tag represents.
  * 
  */
 public class Tag<V> {
 
 	private static AtomicInteger counter = new AtomicInteger(0);
 
 	public static final Logger log = LoggerFactory.getLogger(Tag.class);
 
 	/** FIXME use thread-safe immutable set */
 	private static final Set<Class<?>> primitives = new HashSet<Class<?>>();
 
 	private final static int IS_ENUM = 0x1;
 	private final static int IS_LIST = 0x2;
 	private final static int IS_PRIM = 0x4;
 
 	static {
 		primitives.add(Byte.class);
 		primitives.add(Short.class);
 		primitives.add(Integer.class);
 		primitives.add(Long.class);
 		primitives.add(Float.class);
 		primitives.add(Double.class);
 		primitives.add(Boolean.class);
 		primitives.add(Character.class);
 	}
 
 	/**
 	 * Collect all tags from provided type.
 	 * 
 	 * @throws MissiveException
 	 */
 	public static Tag<?>[] collectAll(final Class<?> clazz)
 			throws MissiveException {
 		try {
 			return ClassUtil.constantFieldsAll(clazz, Tag.class).toArray(
 					new Tag[0]);
 		} catch (final Exception e) {
 			throw new MissiveException(e);
 		}
 	}
 
 	/**
 	 * Collect top level tags from provided type.
 	 * 
 	 * @throws MissiveException
 	 */
 	public static Tag<?>[] collectTop(final Class<?> clazz)
 			throws MissiveException {
 		try {
 			return ClassUtil.constantFieldsTop(clazz, Tag.class).toArray(
 					new Tag[0]);
 		} catch (final Exception e) {
 			throw new MissiveException(e);
 		}
 	}
 
 	/**
 	 * Create tag for given type using constant field name as tag name.
 	 * 
 	 * @throws MissiveException
 	 */
 	public static <V> Tag<V> create(final Class<V> clazz)
 			throws MissiveException {
 		return new Tag<V>(clazz);
 	}
 
 	/**
 	 * Create tag for given name and type.
 	 */
 	public static <V> Tag<V> create(final String name, final Class<V> clazz) {
 		return new Tag<V>(name, clazz);
 	}
 
 	protected static boolean isEnum(final Class<?> clazz) {
 		return clazz.isEnum();
 	}
 
 	protected static boolean isList(final Class<?> clazz) {
 		if (clazz.isArray()) {
 			return true;
 		} else if (Collection.class.isAssignableFrom(clazz)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	protected static boolean isPrim(final Class<?> clazz) {
 		return primitives.contains(clazz);
 	}
 
 	protected static int mask(final Class<?> clazz) {
 		int mask = 0;
 		if (isEnum(clazz)) {
 			mask |= IS_ENUM;
 		}
 		if (isList(clazz)) {
 			mask |= IS_LIST;
 		}
 		if (isPrim(clazz)) {
 			mask |= IS_PRIM;
 		}
 		return mask;
 	}
 
 	public static int maxIndex() {
 		return counter.get();
 	}
 
 	protected static int nameHash(final Class<?> clazz) {
 		return clazz.getName().hashCode();
 	}
 
 	protected static int nextIndex() {
 		return counter.getAndIncrement();
 	}
 
 	private static Object parsePrimitiveFromString(final Class<?> clazz,
 			final String value) {
 
 		if (clazz == Byte.class) {
 
 			return Byte.parseByte(value);
 
 		} else if (clazz == Short.class) {
 
 			return Short.parseShort(value);
 
 		} else if (clazz == Integer.class) {
 
 			return Integer.parseInt(value);
 
 		} else if (clazz == Long.class) {
 
 			return Long.parseLong(value);
 
 		} else if (clazz == Float.class) {
 
 			return Float.parseFloat(value);
 
 		} else if (clazz == Double.class) {
 
 			return Double.parseDouble(value);
 
 		} else if (clazz == Boolean.class) {
 
 			/** note : null for false */
 
 			if ("true".equalsIgnoreCase(value)) {
 				return true;
 			}
 			if ("y".equalsIgnoreCase(value)) {
 				return true;
 			}
 			if ("yes".equalsIgnoreCase(value)) {
 				return true;
 			}
 			return false;
 
 		} else if (clazz == Character.class) {
 
 			return new Character(value.charAt(0));
 
 		} else {
 
 			throw new MissiveException("parse failure " + clazz);
 
 		}
 
 	}
 
 	private final Class<V> type;
 
 	private final int hashCode = nameHash(getClass());
 
 	private final int index = nextIndex();
 
 	private final int mask;
 
 	private volatile String name;
 
 	private final Class<?> spot;
 
 	/**
 	 * Constructor expecting baked-in generic parameter.
 	 * <p>
 	 * Will use constant field name as tag name.
 	 * 
 	 * @throws MissiveException
 	 */
 	@SuppressWarnings("unchecked")
 	public Tag() throws MissiveException {
 		try {
 
 			this.name = null; // will lazy init
 			this.spot = ClassUtil.instanceSpot(5);
 			this.type = (Class<V>) ClassUtil.genericParam(getClass());
 
 			mask = mask(type);
 
 		} catch (final Throwable e) {
 			log.error("construct failure", e);
 			throw new MissiveException(e);
 		}
 	}
 
 	/**
 	 * Constructor will use constant field name as tag name.
 	 * 
 	 * @throws MissiveException
 	 */
 	private Tag(final Class<V> type) throws MissiveException {
 		try {
 
 			this.name = null; // will lazy init
 			this.spot = ClassUtil.instanceSpot(5);
 			this.type = type;
 
 			mask = mask(type);
 
 		} catch (final Throwable e) {
 			log.error("construct failure", e);
 			throw new MissiveException(e);
 		}
 	}
 
 	/**
 	 * Constructor expecting baked-in generic parameter.
 	 * 
 	 * @throws MissiveException
 	 */
 	@SuppressWarnings("unchecked")
 	public Tag(final String name) throws MissiveException {
 		try {
 
 			this.name = name;
 			this.spot = null; // will not use
 			this.type = (Class<V>) ClassUtil.genericParam(getClass());
 
 			mask = mask(type);
 
 		} catch (final Throwable e) {
 			log.error("construct failure", e);
 			throw new MissiveException(e);
 		}
 	}
 
 	public Tag(final String name, final Class<V> type) {
 
 		this.name = name;
 		this.spot = null; // will not use
 		this.type = type;
 
 		mask = mask(type);
 
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public V cast(final Object o) throws MissiveException {
 		try {
 
 			/* If the class is enum and object is string, attempt a valueOf */
 			if (isEnum() && o instanceof String) {
 
 				final Class<? extends Enum> klaz = (Class<? extends Enum>) type;
 				return (V) Enum.valueOf(klaz, (String) o);
 
 				/* If class is primitive and object is string, attempt to parse */
 			} else if (isPrim() && o instanceof String) {
 
 				return (V) parsePrimitiveFromString(type, (String) o);
 
 			} else {
 
 				try {
 					/* Attempt a normal cast */
 					return type.cast(o);
 				} catch (final ClassCastException e) {
 					/*
 					 * Last ditch, attempt to find constructor which accepts
 					 * object o
 					 */
 					return type.getConstructor(o.getClass()).newInstance(o);
 				}
 			}
 
 		} catch (final Throwable e) {
 
 			final String message = //
 			"Failed to cast object in tag " + name() + " " + o;
 			log.error(message, e);
 			throw new MissiveException(message, e);
 
 		}
 	}
 
 	protected String defaultTagName() {
 		return "TAG=" + index();
 	}
 
 	/** Tag value type. */
 	public Class<V> type() {
 		return type;
 	}
 
 	/** Tag name. */
 	public final String name() {
 		/** lazy init */
 		String name = this.name;
 		if (name == null) {
 			synchronized (this) {
 				try {
 					name = ClassUtil.constantFieldName(spot, this);
 				} catch (final Throwable e) {
 					name = defaultTagName();
 				}
 				this.name = name;
 			}
 		}
 		return name;
 	}
 
 	@Override
 	public int hashCode() {
 		return hashCode;
 	}
 
 	/** Tag instantiation index. */
 	public int index() {
 		return index;
 	}
 
 	public final boolean isEnum() {
 		return (mask & IS_ENUM) != 0;
 	}
 
 	public final boolean isList() {
 		return (mask & IS_LIST) != 0;
 	}
 
 	public final boolean isPrim() {
 		return (mask & IS_PRIM) != 0;
 	}
 
 	@Override
 	public String toString() {
 		return name();
 	}
 
 }
