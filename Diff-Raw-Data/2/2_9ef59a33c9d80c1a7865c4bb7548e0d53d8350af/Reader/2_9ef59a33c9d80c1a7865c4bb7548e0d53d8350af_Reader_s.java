 package com.github.evetools.marshal;
 
 import com.github.evetools.marshal.python.PyBase;
 import com.github.evetools.marshal.python.PyBool;
 import com.github.evetools.marshal.python.PyBuffer;
 import com.github.evetools.marshal.python.PyByte;
 import com.github.evetools.marshal.python.PyDBRowDescriptor;
 import com.github.evetools.marshal.python.PyDict;
 import com.github.evetools.marshal.python.PyDouble;
 import com.github.evetools.marshal.python.PyGlobal;
 import com.github.evetools.marshal.python.PyInt;
 import com.github.evetools.marshal.python.PyList;
 import com.github.evetools.marshal.python.PyLong;
 import com.github.evetools.marshal.python.PyMarker;
 import com.github.evetools.marshal.python.PyNone;
 import com.github.evetools.marshal.python.PyObject;
 import com.github.evetools.marshal.python.PyObjectEx;
 import com.github.evetools.marshal.python.PyPackedRow;
 import com.github.evetools.marshal.python.PyShort;
 import com.github.evetools.marshal.python.PyString;
 import com.github.evetools.marshal.python.PyTuple;
 import com.jcraft.jzlib.JZlib;
 import com.jcraft.jzlib.ZStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * Copyright (C)2011 by Gregor Anders
  * All rights reserved.
  *
  * This code is free software; you can redistribute it and/or modify
  * it under the terms of the BSD license (see the file LICENSE.txt
  * included with the distribution).
  */
 public class Reader {
 
 	private static class Buffer {
 
 		private final ByteBuffer buffer;
 
 		public Buffer(byte[] bytes) {
 			this.buffer = ByteBuffer.wrap(bytes);
 			this.buffer.order(ByteOrder.LITTLE_ENDIAN);
 		}
 
 		private void advancePosition(int size) {
 			this.buffer.position(this.buffer.position() + size);
 		}
 
 		public final int length() {
 			return this.buffer.array().length;
 		}
 
 		public final byte peekByte() {
 			final byte b = this.buffer.get();
 			this.buffer.position(this.buffer.position() - 1);
 			return b;
 		}
 
 		public final byte[] peekBytes(int offset, int size) {
 			byte[] bytes = null;
 			final int position = this.buffer.position();
 			this.buffer.position(offset);
 			bytes = this.readBytes(size);
 			this.buffer.position(position);
 			return bytes;
 		}
 
 		public final int position() {
 			return this.buffer.position();
 		}
 
 		public final byte readByte() {
 			return this.buffer.get();
 		}
 
 		public final byte[] readBytes(int size) {
 			final byte[] bytes = new byte[size];
 			this.buffer.get(bytes);
 			return bytes;
 		}
 
 		public final double readDouble() {
 			final double value = this.buffer.asDoubleBuffer().get();
 			this.advancePosition(8);
 			return value;
 		}
 
 		public final int readInt() {
 			final int value = this.buffer.asIntBuffer().get();
 			this.advancePosition(4);
 			return value;
 		}
 
 		public final long readLong() {
 			final long value = this.buffer.asLongBuffer().get();
 			this.advancePosition(8);
 			return value;
 		}
 
 		public final short readShort() {
 			final short value = this.buffer.asShortBuffer().get();
 			this.advancePosition(2);
 			return value;
 		}
 	}
 
 	interface IRead {
 		PyBase read() throws IOException;
 	}
 
 	private static byte fromBitSet(BitSet bitSet) {
 		byte b = 0;
 
 		for (int i = 0; i < bitSet.length(); i++) {
 			if (bitSet.get(i)) {
 				b |= 1 << i;
 			}
 		}
 		return b;
 	}
 
 	private final Buffer buffer;
 
 	private PyBase latest;
 
 	private final IRead[] loadMethods = new IRead[] {
 	/* 0x00 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadError();
 		}
 	},
 	/* 0x01 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNone();
 		}
 	},
 	/* 0x02 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadGlobal();
 		}
 	},
 	/* 0x03 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadLong();
 		}
 	},
 	/* 0x04 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadInt();
 		}
 	},
 	/* 0x05 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadShort();
 		}
 	},
 	/* 0x06 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadByte();
 		}
 	},
 	/* 0x07 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadIntMinus1();
 		}
 	},
 	/* 0x08 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadInt0();
 		}
 	},
 	/* 0x09 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadInt1();
 		}
 	},
 	/* 0x0a */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadDouble();
 		}
 	},
 	/* 0x0b */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadDouble0();
 		}
 	},
 	/* 0x0c */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x0d */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x0e */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadString0();
 		}
 	},
 	/* 0x0f */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadString1();
 		}
 	},
 	/* 0x10 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadString();
 		}
 	},
 	/* 0x11 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadStringRef();
 		}
 	},
 	/* 0x12 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadUnicode();
 		}
 	},
 	/* 0x13 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadBuffer();
 		}
 	},
 	/* 0x14 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadTuple();
 		}
 	},
 	/* 0x15 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadList();
 		}
 	},
 	/* 0x16 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadDict();
 		}
 	},
 	/* 0x17 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadInstance();
 		}
 	},
 	/* 0x18 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x19 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x1a */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x1b */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadReference();
 		}
 	},
 	/* 0x1c */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x1d */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x1e */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x1f */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadTrue();
 		}
 	},
 	/* 0x20 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadFalse();
 		}
 	},
 	/* 0x21 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x22 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadObjectEx();
 		}
 	},
 	/* 0x23 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadObjectEx();
 		}
 	},
 	/* 0x24 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x25 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadTuple1();
 		}
 	},
 	/* 0x26 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadList0();
 		}
 	},
 	/* 0x27 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadList1();
 		}
 	},
 	/* 0x28 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadUnicode0();
 		}
 	},
 	/* 0x29 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadUnicode1();
 		}
 	},
 	/* 0x2a */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadPacked();
 		}
 	},
 	/* 0x2b */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadSubStream();
 		}
 	},
 	/* 0x2c */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadTuple2();
 		}
 	},
 	/* 0x2d */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadMarker();
 		}
 	},
 	/* 0x2e */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadBuffer();
 		}
 	},
 	/* 0x2f */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadVarInt();
 		}
 	},
 	/* 0x30 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x31 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x32 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x33 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x34 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x35 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x36 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x37 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x38 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x39 */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	},
 	/* 0x3a */new IRead() {
 		@Override
 		public PyBase read() throws IOException {
 			return Reader.this.loadNotImplemented();
 		}
 	} };
 
 	private int position;
 
 	private Map<Integer, PyBase> shared;
 
 	private Buffer sharedBuffer;
 
 	private int type;
 
 	private Reader(Buffer buffer) throws IOException {
 		this.buffer = buffer;
 	}
 
 	public Reader(File file) throws IOException {
 		this(new FileInputStream(file));
 	}
 
 	public Reader(InputStream stream) throws IOException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
 		final byte[] bytes = new byte[4096];
 
 		int read = -1;
 		while (0 <= (read = stream.read(bytes))) {
 			baos.write(bytes, 0, read);
 		}
 
 		stream.close();
 
 		this.buffer = new Buffer(baos.toByteArray());
 	}
 
 	protected PyDBRowDescriptor toDBRowDescriptor(PyBase base)
 			throws IOException {
 
 		if (!(base instanceof PyObjectEx)) {
 			throw new IOException("Invalid Packed Row header: "
 					+ base.getType());
 		}
 
 		final PyObjectEx object = (PyObjectEx) base;
 
 		return new PyDBRowDescriptor(object);
 	}
 
 	protected int length() {
 
 		int length = 0;
 
 		length = this.buffer.readByte() & 0xFF;
 
 		if (length == 255) {
 			length = this.buffer.readInt();
 		}
 
 		return length;
 	}
 
 	protected PyBase loadBuffer() throws IOException {
 		final int size = this.length();
 		final byte[] bytes = this.buffer.readBytes(size);
 
 		// check for size is lame need a proper method
 		if (bytes[0] == 0x78) {
 
 			String str = new String(bytes);
 
 			System.out.println("zlib");
 			System.out.println(str.toString());
 
 			final byte[] zlibbytes = new byte[bytes.length + 1];
      System.arraycopy(bytes, 0, zlibbytes, 0, bytes.length);
 			zlibbytes[zlibbytes.length - 1] = 0;
 
 			int zlen = zlibbytes.length * 2;
 			byte[] zout = new byte[zlen];
 
 			boolean success = false;
 			final ZStream zstream = new ZStream();
 			int res = 0;
 
 			while (!success) {
 
 				zout = new byte[zlen];
 
 				zstream.next_in = zlibbytes;
 				zstream.next_in_index = 0;
 				zstream.next_out = zout;
 				zstream.next_out_index = 0;
 
 				if (zstream.inflateInit() != JZlib.Z_OK) {
 					throw new IOException("Error uncompressing zlib buffer");
 				}
 
 				while ((zstream.total_out < zlen)
 						&& (zstream.total_in < zlibbytes.length)) {
 					zstream.avail_in = zstream.avail_out = 1;
 					res = zstream.inflate(JZlib.Z_NO_FLUSH);
 					if (res == JZlib.Z_STREAM_END) {
 						success = true;
 						break;
 					}
 
 					if (res == JZlib.Z_DATA_ERROR) {
 						return new PyBuffer(bytes);
 					}
 				}
 
 				if (zstream.total_out < zlen) {
 					break;
 				}
 
 				if (!success) {
 					zout = null;
 					zlen = zlen * 2;
 				} else {
 					zstream.inflateEnd();
 
 					/*
 					 * for debugging byte[] uncom = new byte[(int)
 					 * zstream.total_out]; for (int loop = 0; loop <
 					 * uncom.length; loop++) { uncom[loop] = zout[loop]; }
 					 */
 
 					final Buffer buf = new Buffer(zout);
 					final Reader reader = new Reader(buf);
 
 					return reader.read();
 				}
 			}
 		}
 		return new PyBuffer(bytes);
 	}
 
 	protected PyBase loadByte() throws IOException {
 		final byte valueByte = this.buffer.readByte();
 		return new PyByte(valueByte);
 	}
 
 	protected PyBase loadDict() throws IOException {
 		final int size = this.length();
 
 		PyBase key = null;
 		PyBase value = null;
 
 		final PyDict dict = new PyDict();
 
 		for (int loop = 0; loop < size; loop++) {
 			value = this.loadPy();
 			key = this.loadPy();
 			dict.put(key, value);
 		}
 
 		return dict;
 	}
 
 	protected PyBase loadDouble() throws IOException {
 		return new PyDouble(this.buffer.readDouble());
 	}
 
 	protected PyBase loadDouble0() throws IOException {
 		return new PyDouble(0);
 	}
 
 	protected PyBase loadError() throws IOException {
 		throw new IOException("ERROR");
 	}
 
 	protected PyBase loadFalse() throws IOException {
 		return new PyBool(false);
 	}
 
 	protected PyBase loadGlobal() throws IOException {
 		final byte[] bytes = this.buffer.readBytes(this.length());
 		return new PyGlobal(new String(bytes));
 	}
 
 	protected PyBase loadInstance() throws IOException {
 		return new PyObject(this.loadPy(), this.loadPy());
 	}
 
 	protected PyBase loadInt() throws IOException {
 		return new PyInt(this.buffer.readInt());
 	}
 
 	protected PyBase loadInt0() throws IOException {
 		return new PyInt(0);
 	}
 
 	protected PyBase loadInt1() throws IOException {
 		return new PyInt(1);
 	}
 
 	protected PyBase loadIntMinus1() throws IOException {
 		return new PyInt(-1);
 	}
 
 	protected PyBase loadList() throws IOException {
 		return this.loadList(this.length());
 	}
 
 	protected PyBase loadList(int size) throws IOException {
 
 		final PyList tuple = new PyList();
 
 		PyBase base = null;
 
 		while (size > 0) {
 			base = this.loadPy();
 
 			if (base == null) {
 				throw new IOException("null element in list found");
 			}
 
 			tuple.add(base);
 			size--;
 		}
 
 		return tuple;
 	}
 
 	protected PyBase loadList0() throws IOException {
 		return this.loadList(0);
 	}
 
 	protected PyBase loadList1() throws IOException {
 		return this.loadList(1);
 	}
 
 	protected PyBase loadLong() throws IOException {
 		return new PyLong(this.buffer.readLong());
 	}
 
 	protected PyBase loadNone() throws IOException {
 		return new PyNone();
 	}
 
 	protected PyBase loadMarker() throws IOException {
 		return new PyMarker();
 	}
 
 	protected PyBase loadNotImplemented() throws IOException {
 		throw new IOException("Not implemented: "
 				+ Integer.toHexString(this.type) + " at: " + this.position);
 	}
 
 	protected PyBase loadObjectEx() throws IOException {
 
 		final PyObjectEx objectex = new PyObjectEx();
 
 		this.latest = objectex;
 
 		objectex.setHead(this.loadPy());
 
 		while (this.buffer.peekByte() != 0x2d) {
 			objectex.getList().add(this.loadPy());
 		}
 		this.buffer.readByte();
 
 		PyBase key = null;
 		PyBase value = null;
 
 		while (this.buffer.peekByte() != 0x2d) {
 			value = this.loadPy();
 			key = this.loadPy();
 			objectex.getDict().put(key, value);
 		}
 		this.buffer.readByte();
 
 		return objectex;
 	}
 
 	protected PyBase loadPacked() throws IOException {
 
 		final PyBase head = this.loadPy();
 		int size = this.length();
 		final byte[] bytes = this.buffer.readBytes(size);
 
 		final PyPackedRow base = new PyPackedRow(head, new PyBuffer(bytes));
 
 		final PyDBRowDescriptor desc = this.toDBRowDescriptor(head);
 
 		size = desc.size();
 
 		final byte[] out = this.zerouncompress(bytes, size);
 
 		final Buffer outbuf = new Buffer(out);
 
 		ArrayList<PyBase> list = desc.getTypeMap().get(new Integer(0));
 
 		for (final PyBase pyBase : list) {
 			final PyTuple tuple = pyBase.asTuple();
 			if (((PyByte) tuple.get(1)).getValue() == 5) {
 				base.put(tuple.get(0), new PyDouble(outbuf.readDouble()));
 			} else {
 				base.put(tuple.get(0), new PyLong(outbuf.readLong()));
 			}
 		}
 
 		list = desc.getTypeMap().get(new Integer(1));
 
 		for (final PyBase pyBase : list) {
 			final PyTuple tuple = pyBase.asTuple();
 			base.put(tuple.get(0), new PyInt(outbuf.readInt()));
 		}
 
 		list = desc.getTypeMap().get(new Integer(2));
 
 		for (final PyBase pyBase : list) {
 			final PyTuple tuple = pyBase.asTuple();
 			base.put(tuple.get(0), new PyShort(outbuf.readShort()));
 		}
 
 		list = desc.getTypeMap().get(new Integer(3));
 
 		for (final PyBase pyBase : list) {
 			final PyTuple tuple = pyBase.asTuple();
 			base.put(tuple.get(0), new PyByte(outbuf.readByte()));
 		}
 
 		list = desc.getTypeMap().get(new Integer(4));
 
 		int boolcount = 0;
 		int boolvalue = 0;
 
 		for (final PyBase pyBase : list) {
 			final PyTuple tuple = pyBase.asTuple();
 
 			if (boolcount == 0) {
 				boolvalue = outbuf.readByte();
 			}
 
 			final boolean val = ((boolvalue >> boolcount++) & 0x01) > 0 ? true
 					: false;
 
 			base.put(tuple.get(0), new PyBool(val));
 
 			if (boolcount == 8) {
 				boolcount = 0;
 			}
 		}
 
 		list = desc.getTypeMap().get(new Integer(5));
 
 		for (final PyBase pyBase : list) {
 			final PyTuple tuple = pyBase.asTuple();
 			base.put(tuple.get(0), this.loadPy());
 		}
 		return base;
 	}
 
 	protected PyBase loadPy() throws IOException {
 
 		this.position = this.buffer.position();
 
 		final byte magic = this.buffer.readByte();
 		final boolean sharedPy = (magic & 0x40) != 0;
 		this.type = magic;
 		this.type = (this.type & 0x3f);
 
 		final PyBase pyBase = this.loadMethods[this.type].read();
 
 		if (sharedPy) {
 			// this is a dirty hack and maybe leads to errors
 			if ((pyBase.isGlobal())
 					&& (pyBase.asGlobal().getValue().endsWith(
 							"blue.DBRowDescriptor"))) {
 				this.shared.put(new Integer(this.sharedBuffer.readInt()),
 						this.latest);
 			} else {
 				this.shared.put(new Integer(this.sharedBuffer.readInt()), pyBase);
 			}
 		}
 
 		return pyBase;
 	}
 
 	protected PyBase loadReference() throws IOException {
 		return this.shared.get(new Integer(this.length()));
 	}
 
 	protected PyBase loadShort() throws IOException {
 		return new PyShort(this.buffer.readShort());
 	}
 
 	protected PyBase loadString() throws IOException {
 		return new PyString(new String(this.buffer.readBytes(this.length())));
 	}
 
 	protected PyBase loadString0() throws IOException {
 		return new PyString("");
 	}
 
 	protected PyBase loadString1() throws IOException {
 		return new PyString(new String(this.buffer.readBytes(1)));
 	}
 
 	protected PyBase loadStringRef() throws IOException {
 		return new PyString(Strings.get(this.length()));
 	}
 
 	protected PyBase loadSubStream() throws IOException {
 		final int size = this.length();
 		final Buffer buf = new Buffer(this.buffer.readBytes(size));
 		final Reader reader = new Reader(buf);
 		return reader.read();
 	}
 
 	protected PyBase loadTrue() throws IOException {
 		return new PyBool(true);
 	}
 
 	protected PyBase loadTuple() throws IOException {
 		return this.loadTuple(this.length());
 	}
 
 	protected PyBase loadTuple(int size) throws IOException {
 
 		final PyTuple tuple = new PyTuple();
 
 		PyBase base = null;
 
 		while (size > 0) {
 			base = this.loadPy();
 
 			if (base == null) {
 				throw new IOException("null element in tuple found");
 			}
 
 			tuple.add(base);
 			size--;
 		}
 
 		return tuple;
 	}
 
 	protected PyBase loadTuple1() throws IOException {
 		return this.loadTuple(1);
 	}
 
 	protected PyBase loadTuple2() throws IOException {
 		return this.loadTuple(2);
 	}
 
 	protected PyBase loadUnicode() throws IOException {
 		return new PyString(new String(this.buffer.readBytes(this.length() * 2)));
 	}
 
 	protected PyBase loadUnicode0() throws IOException {
 		return new PyString("");
 	}
 
 	protected PyBase loadUnicode1() throws IOException {
 		return new PyString(new String(this.buffer.readBytes(2)));
 	}
 
 	protected PyBase loadVarInt() throws IOException {
 
 		final int size = this.length();
 
 		switch (size) {
 		case 0:
 			return new PyLong(0);
 		case 2:
 			return this.loadShort();
 		case 4:
 			return this.loadInt();
 		case 8:
 			return this.loadLong();
 		default:
 			final byte[] bytes = this.buffer.readBytes(size);
 
 			final BigInteger bi = new BigInteger(bytes);
 
 			return new PyLong(bi.longValue());
 		}
 	}
 
 	public PyBase read() throws IOException {
 
 		this.buffer.readByte();
 		final int size = this.buffer.readInt();
 
 		this.shared = new HashMap<Integer, PyBase>(size);
 
 		final int offset = this.buffer.length() - (size * 4);
 
 		this.sharedBuffer = new Buffer(
 				this.buffer.peekBytes(offset, (size * 4)));
 
 		PyBase base = null;
 
 		base = this.loadPy();
 
 		return base;
 	}
 
 	protected byte[] zerouncompress(byte[] bytes, int size) throws IOException {
 
 		final byte[] out = new byte[size + 16];
 		int outpos = 0;
 		byte current = 0;
 		int length = 0;
 		int pos = 0;
 
 		for (int loop = 0; loop < out.length; loop++) {
 			out[loop] = 0;
 		}
 
 		while (pos < bytes.length) {
 
 			current = bytes[pos++];
 
 			final BitSet bitSet = new BitSet(8);
 			for (int i = 0; i < 8; i++) {
 				if ((current & (1 << i)) > 0) {
 					bitSet.set(i);
 				}
 			}
 
 			if (bitSet.get(3)) {
 				length = Reader.fromBitSet(bitSet.get(0, 3)) + 1;
 				for (int i = 0; i < length; i++) {
 					out[outpos++] = 0;
 				}
 			} else {
 				length = 8 - Reader.fromBitSet(bitSet.get(0, 3));
 				for (int i = 0; i < length; i++) {
 					out[outpos++] = bytes[pos++];
 				}
 			}
 
 			if (bitSet.get(7)) {
 				length = Reader.fromBitSet(bitSet.get(4, 7)) + 1;
 				for (int i = 0; i < length; i++) {
 					out[outpos++] = 0;
 				}
 			} else {
 				length = 8 - Reader.fromBitSet(bitSet.get(4, 7));
 				for (int i = 0; (i < length) && (pos < bytes.length); i++) {
 					out[outpos++] = bytes[pos++];
 				}
 			}
 		}
 
 		return out;
 	}
 }
