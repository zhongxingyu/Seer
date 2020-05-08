 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.core.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.security.GeneralSecurityException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 
 /**
  * A logical file.
  *
  * Files are immutable; to modify a file, you must create and freeze a {@link MutableFile}.
  */
 public class File implements me.footlights.plugin.File
 {
 	public static File from(EncryptedBlock header, Collection<EncryptedBlock> ciphertext)
 	{
 		List<Block> plaintext = Lists.newArrayListWithCapacity(ciphertext.size());
 		for (EncryptedBlock e : ciphertext) plaintext.add(e.plaintext());
 
 		return new File(header, plaintext, ciphertext);
 	}
 
 	public static MutableFile newBuilder() { return new MutableFile(); }
 	public static final class MutableFile
 	{
 		public MutableFile setContent(Collection<ByteBuffer> content)
 		{
 			this.content = ImmutableList.copyOf(content);
 			return this;
 		}
 
 		MutableFile setBlocks(Collection<Block> content)
 		{
 			List<ByteBuffer> bytes = Lists.newLinkedList();
 			for (Block b : content) bytes.add(b.content());
 			this.content = ImmutableList.copyOf(bytes);
 
 			return this;
 		}
 
 		MutableFile setDesiredBlockSize(int size)
 		{
 			this.desiredBlockSize = size;
 			return this;
 		}
 
 		/**
 		 * Produce a proper {@link File} by fixing the current contents of this
 		 * {@link MutableFile}.
 		 */
 		public File freeze() throws FormatException, GeneralSecurityException
 		{
 			// First, break the content into chunks of the appropriate size.
 			Collection<ByteBuffer> chunked =
 				rechunk(content, desiredBlockSize - Block.OVERHEAD_BYTES);
 
 			// Next, create {@link EncryptedBlock} objects.
 			List<EncryptedBlock> ciphertext = Lists.newLinkedList();
 
 			for (ByteBuffer b : chunked)
 				ciphertext.add(
 					Block.newBuilder()
 						.setContent(b)
 						.setDesiredSize(desiredBlockSize)
 						.build()
 						.encrypt());
 
 			// Finally, create the header. TODO: just embed links in all the blocks.
 			Block.Builder header = Block.newBuilder();
 			for (EncryptedBlock b : ciphertext) header.addLink(b.link());
 
 			return File.from(header.build().encrypt(), ciphertext);
 		}
 
 		private MutableFile() {}
 
 		private Iterable<ByteBuffer> content = Lists.newArrayList();
 		private int desiredBlockSize = 4096;
 	}
 
 
 	/**
 	 * The content of the file, transformed into an {@link InputStream}.
 	 */
 	public InputStream getInputStream()
 	{
 		final ByteBuffer[] buffers = new ByteBuffer[plaintext.size()];
 		for (int i = 0; i < buffers.length; i++)
 			buffers[i] = plaintext.get(i).content();
 
 		return new InputStream()
 		{
 			@Override public int read(byte[] buffer, int offset, int len)
 			{
 				if (len == 0) return 0;
 				if (blockIndex >= plaintext.size()) return -1;
 
 				int pos = offset;
				while (pos < (offset + len))
 				{
 					ByteBuffer next = buffers[blockIndex];
					if (next.remaining() > len)
					{
						next.get(buffer, pos, len);
						pos += len;
					}
					else if (next.remaining() > 0)
 					{
 						int bytes = next.remaining();
 						next.get(buffer, pos, bytes);
 						pos += bytes;
						blockIndex++;
 					}
 
 					if (pos == offset) return -1;
 				}
 
 				return (pos - offset);
 			}
 
 			/** This is a horrendously inefficient way of reading data. Don't! */
 			@Override public int read() throws IOException
 			{
 				byte[] data = new byte[1];
 				int bytes = read(data, 0, data.length);
 
 				if (bytes < 0) throw new BufferUnderflowException();
 				if (bytes == 0)
 					throw new Error(
 						"Implementation error in File.read(byte[1]): returned 0");
 
 				return data[0];
 			}
 
 
 			private int blockIndex;
 		};
 	}
 
 
 	/** Encrypted blocks to be saved in a {@link Store}. */
 	public List<EncryptedBlock> toSave()
 	{
 		LinkedList<EncryptedBlock> everything = Lists.newLinkedList(ciphertext);
 		everything.push(header);
 
 		return everything;
 	}
 
 	/** A link to the {@link File} itself. */
 	public Link link() { return header.link(); }
 
 
 	/**
 	 * The contents of the file.
 	 *  
 	 * @throws IOException    on I/O errors such as network failures
 	 */
 	List<ByteBuffer> content() throws IOException
 	{
 		List<ByteBuffer> content = Lists.newLinkedList();
 		for (Block b : plaintext) content.add(b.content());
 
 		return content;
 	}
 
 	@Override public boolean equals(Object o)
 	{
 		if (o == null) return false;
 		if (!(o instanceof File)) return false;
 
 		File f = (File) o;
 		if (!this.header.equals(f.header)) return false;
 		if (!this.plaintext.equals(f.plaintext)) return false;
 		if (!this.ciphertext.equals(f.ciphertext)) return false;
 
 		return true;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Encrypted File [ " + plaintext.size() + " blocks, name = '" + header.name() + "' ]";
 	}
 
 
 	/** Convert buffers of data, which may have any size, into buffers of a desired chunk size. */
 	private static Collection<ByteBuffer> rechunk(Iterable<ByteBuffer> content, int chunkSize)
 	{
 		Iterator<ByteBuffer> i = content.iterator();
 		ByteBuffer next = null;
 
 		List<ByteBuffer> chunked = Lists.newLinkedList();
 		ByteBuffer current = ByteBuffer.allocate(chunkSize);
 
 		while (true)
 		{
 			// Fetch the next input buffer (if necessary). If there are none, we're done.
 			if ((next == null) || !next.hasRemaining())
 			{
 				if (i.hasNext()) next = i.next();
 				else break;
 
 				// If the next batch of content is already the right size, add it directly.
 				if (next.remaining() == chunkSize)
 				{
 					chunked.add(next);
 					continue;
 				}
 			}
 
 			// If the current output buffer is full, create a new one.
 			if (current.remaining() == 0)
 			{
 				current.flip();
 				chunked.add(current);
 				current = ByteBuffer.allocate(chunkSize);
 			}
 
 			// Copy data from input to output.
 			int toCopy = Math.min(next.remaining(), current.remaining());
 			next.get(current.array(), current.position(), toCopy);
 			current.position(current.position() + toCopy);
 		}
 
 		if (current.position() > 0)
 		{
 			current.flip();
 			chunked.add(current);
 		}
 
 		return chunked;
 	}
 
 
 	/** Default constructor; produces an anonymous file */
 	private File(EncryptedBlock header,
 		Collection<Block> plaintext, Collection<EncryptedBlock> ciphertext)
 	{
 		this.header = header;
 		this.plaintext = ImmutableList.<Block>builder().addAll(plaintext).build();
 		this.ciphertext = ImmutableList.<EncryptedBlock>builder().addAll(ciphertext).build();
 	}
 
 
 	private final EncryptedBlock header;
 	private final ImmutableList<Block> plaintext;
 	private final ImmutableList<EncryptedBlock> ciphertext;
 }
