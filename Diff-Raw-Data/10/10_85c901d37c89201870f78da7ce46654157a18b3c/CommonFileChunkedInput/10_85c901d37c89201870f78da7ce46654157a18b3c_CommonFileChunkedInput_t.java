 /**
  * This file is part of Waarp Project (named also Waarp or GG).
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
  * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
  * 
  * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  * 
  * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Waarp . If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.waarp.gateway.kernel.commonfile;
 
 import org.waarp.common.command.exception.CommandAbstractException;
 import org.waarp.common.exception.FileEndOfTransferException;
 import org.waarp.common.exception.FileTransferException;
 import org.waarp.common.file.DataBlock;
 import org.waarp.common.file.FileInterface;
 import org.waarp.gateway.kernel.HttpIncorrectRetrieveException;
 
 import org.jboss.netty.handler.stream.ChunkedInput;
 
 /**
  * @author Frederic Bregier
  * 
  */
 public class CommonFileChunkedInput implements ChunkedInput {
 
 	private FileInterface document = null;
 
 	private boolean lastChunkAlready = false;
 
 	private long offset = 0;
 
 	/**
 	 * @param document
 	 * @throws HttpIncorrectRetrieveException
 	 */
 	public CommonFileChunkedInput(FileInterface document)
 			throws HttpIncorrectRetrieveException {
 		this.document = document;
 		try {
 			this.document.retrieve();
 		} catch (CommandAbstractException e) {
 			throw new HttpIncorrectRetrieveException(e);
 		}
 	}
 
 	@Override
 	public boolean hasNextChunk() {
 		return (!lastChunkAlready);
 	}
 
 	@Override
 	public Object nextChunk() throws HttpIncorrectRetrieveException {
 		// Document
 		DataBlock block;
 		try {
 			block = this.document.readDataBlock();
 		} catch (FileEndOfTransferException e) {
 			lastChunkAlready = true;
 			return null;
 		} catch (FileTransferException e) {
 			throw new HttpIncorrectRetrieveException(e);
 		}
 		lastChunkAlready = block.isEOF();
 		offset += block.getByteCount();
 		return block.getBlock();
 	}
 
 	@Override
 	public boolean isEndOfInput() {
 		return (lastChunkAlready);
 	}
 
 	@Override
 	public void close() throws HttpIncorrectRetrieveException {
 		try {
 			if (this.document.isInReading()) {
 				this.document.abortFile();
 			}
 		} catch (CommandAbstractException e) {
 			throw new HttpIncorrectRetrieveException(e);
 		}
 		lastChunkAlready = true;
 	}
 
 	/**
 	 * Returns the number of transferred bytes.
 	 */
 	public long getTransferredBytes() {
 		return offset;
 	}
 
 }
