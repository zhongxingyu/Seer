 /*
  * Copyright (c) 2012 The University of Manchester, UK.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the names of The University of Manchester nor the names of its
  *   contributors may be used to endorse or promote products derived from this
  *   software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package uk.org.taverna.server.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.SoftReference;
 import java.net.URI;
 import java.util.Arrays;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.math.LongRange;
 
 /**
  * 
  * @author Robert Haines
  */
 public class PortData extends PortValue {
 
 	// If there is no data...
 	private static final byte[] EMPTY_DATA = new byte[0];
 
 	// Data cache
 	private SoftReference<byte[]> cache;
 	private LongRange dataGot;
 
 	PortData(Port parent, URI reference, String type, long size) {
 		super(parent, reference, type, size);
 
 		cache = new SoftReference<byte[]>(null);
 		dataGot = null;
 	}
 
 	@Override
 	public boolean isError() {
 		return false;
 	}
 
 	@Override
 	public PortValue get(int index) throws IndexOutOfBoundsException {
 		if (index != 0) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		return this;
 	}
 
 	@Override
 	public int size() {
 		return 1;
 	}
 
 	@Override
 	public long getDataSize() {
 		return size;
 	}
 
 	@Override
 	public InputStream getDataStream() {
 		return getRun().getOutputDataStream(getReference(), null);
 	}
 
 	@Override
 	public void writeDataToFile(File file) throws IOException {
 		InputStream is = getRun().getOutputDataStream(getReference(), null);
 		getRun().writeStreamToFile(is, file);
 	}
 
 	@Override
 	public byte[] getData() {
		return getData(new LongRange(0, 12));
 	}
 
 	@Override
 	public byte[] getData(int index) {
 		return getData();
 	}
 
 	public byte[] getData(int start, int length) {
 		// If length is zero then there is nothing to return.
 		if (length == 0) {
 			return EMPTY_DATA;
 		}
 
 		// LongRange is inclusive so (start + length) is too long by one.
 		return getData(new LongRange(start, (start + length - 1)));
 	}
 
 	private byte[] getData(LongRange range) {
 
 		byte[] data = cache.get();
 		if (data == null) {
 			dataGot = null;
 		}
 
 		// Return empty data if this value is empty.
 		if (getDataSize() == 0
 				|| getContentType().equalsIgnoreCase("application/x-empty")) {
 			return EMPTY_DATA;
 		}
 
 		// Check the range provided is sensible. LongRange is inclusive so size
 		// is too long by one.
 		if (range.getMinimumLong() < 0) {
 			range = new LongRange(0, range.getMaximumLong());
 		}
 		if (range.getMaximumLong() >= getDataSize()) {
 			range = new LongRange(range.getMinimumLong(),
 					(getDataSize() - 1));
 		}
 
 		// Find the data range(s) that we need to download.
 		LongRange[] need = fill(dataGot, range);
 
 		switch (need.length) {
 		case 0:
 			// We already have all the data we need, just return the right bit.
 			// dataGot cannot be null here and must fully encompass range.
 			int from = (int) (range.getMinimumLong() - dataGot.getMinimumLong());
 			int to = (int) (range.getMaximumLong() - dataGot.getMinimumLong());
 
 			// copyOfRange is exclusive!
 			return Arrays.copyOfRange(data, from, (to + 1));
 		case 1:
 			// we either have some data, at one end of range or either side of
 			// it, or none. dataGot can be null here.
 			// In both cases we download what we need.
 			byte[] newData = getRun().getOutputData(getReference(), need[0]);
 			if (dataGot == null) {
 				// This is the only data we have, return it all.
 				dataGot = range;
 				data = newData;
 				cache = new SoftReference<byte[]>(data);
 				return data;
 			} else {
 				// Add the new data to the correct end of the data we have,
 				// then return the range requested.
 				if (range.getMaximumLong() <= dataGot.getMaximumLong()) {
 					dataGot = new LongRange(range.getMinimumLong(),
 							dataGot.getMaximumLong());
 					data = ArrayUtils.addAll(newData, data);
 					cache = new SoftReference<byte[]>(data);
 
 					// copyOfRange is exclusive!
 					return Arrays.copyOfRange(data, 0,
 							(int) (range.getMaximumLong() + 1));
 				} else {
 					dataGot = new LongRange(dataGot.getMinimumLong(),
 							range.getMaximumLong());
 					data = ArrayUtils.addAll(data, newData);
 					cache = new SoftReference<byte[]>(data);
 
 					// copyOfRange is exclusive!
 					return Arrays.copyOfRange(data, (int) (range
 							.getMinimumLong() - dataGot.getMinimumLong()),
 							(int) (dataGot.getMaximumLong() + 1));
 				}
 			}
 		case 2:
 			// We definitely have some data and it is in the middle of the
 			// range requested. dataGot cannot be null here.
 			dataGot = range;
 			byte[] data1 = getRun().getOutputData(getReference(), need[0]);
 			byte[] data2 = getRun().getOutputData(getReference(), need[1]);
 			data = ArrayUtils.addAll(data1, data);
 			data = ArrayUtils.addAll(data, data2);
 			cache = new SoftReference<byte[]>(data);
 			return data;
 		}
 
 		// Should never get here! This is an error!
 		return null;
 	}
 
 	// Aaaarrrgh!
 	private LongRange[] fill(LongRange got, LongRange want) {
 		if (got == null) {
 			return new LongRange[] { want };
 		}
 
 		if (got.containsLong(want.getMinimumLong())) {
 			if (got.containsLong(want.getMaximumLong())) {
 				return new LongRange[0];
 			} else {
 				return new LongRange[] { new LongRange(
 						(got.getMaximumLong() + 1), want.getMaximumLong()) };
 			}
 		} else {
 			if (got.containsLong(want.getMaximumLong())) {
 				return new LongRange[] { new LongRange(want.getMinimumLong(),
 						(got.getMinimumLong() - 1)) };
 			} else {
 				if (want.getMaximumLong() < got.getMinimumLong()) {
 					return new LongRange[] { new LongRange(
 							want.getMinimumLong(), (got.getMinimumLong() - 1)) };
 				} else if (want.getMinimumLong() > got.getMaximumLong()) {
 					return new LongRange[] { new LongRange(
 							(got.getMaximumLong() + 1), want.getMaximumLong()) };
 				} else {
 					return new LongRange[] {
 							new LongRange(want.getMinimumLong(),
 									(got.getMinimumLong() - 1)),
 									new LongRange((got.getMaximumLong() + 1),
 											want.getMaximumLong()) };
 				}
 			}
 		}
 	}
 }
