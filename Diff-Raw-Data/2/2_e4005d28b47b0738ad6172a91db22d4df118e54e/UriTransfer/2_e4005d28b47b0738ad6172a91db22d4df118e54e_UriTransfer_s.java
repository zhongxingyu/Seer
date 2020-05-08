 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.util.clipboard;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.swt.dnd.ByteArrayTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.TransferData;
 
 /**
  * SWT {@link Transfer} for transporting a number of object {@link URI}
  * -addressable objects. Expected data class is {@link UriTransferData}. In
  * copy/paste scenario, use the {@link ModelClipboard} facility.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class UriTransfer extends ByteArrayTransfer {
 
 	private static final String TYPE_NAME = "URI-transfer-format"; //$NON-NLS-1$
 	private static final int TYPE_ID = registerType(UriTransfer.TYPE_NAME);
 	private static final String URI_SEP = "#?#"; //$NON-NLS-1$
 	private static final String CHARSET = "UTF-8"; //$NON-NLS-1$
 
 	private static final UriTransfer INSTANCE = new UriTransfer();
 
 	/**
 	 * @return the singleton instance
 	 */
 	public static UriTransfer getInstance() {
 		return UriTransfer.INSTANCE;
 	}
 
 	@Override
 	protected int[] getTypeIds() {
 		return new int[] { UriTransfer.TYPE_ID };
 	}
 
 	@Override
 	protected String[] getTypeNames() {
 		return new String[] { UriTransfer.TYPE_NAME };
 	}
 
 	@Override
 	protected void javaToNative(final Object data, final TransferData transferData) {
 		if (data == null) {
 			return;
 		}
 		final UriTransferData realData = (UriTransferData) data;
 		if (realData.getUriStrings().isEmpty()) {
 			return;
 		}
 		final ByteArrayOutputStream out = new ByteArrayOutputStream();
 		final DataOutputStream dataOut = new DataOutputStream(out);
 		try {
 			final byte[] bytes = toTransferBytes(realData.getUriStrings());
 			dataOut.writeInt(bytes.length);
 			dataOut.write(bytes);
 			super.javaToNative(out.toByteArray(), transferData);
 		} catch (final IOException e) {
 			T.racer().error("Error when writing transfer data", e); //$NON-NLS-1$
 		} finally {
 			try {
 				dataOut.close();
 				out.close();
 			} catch (final IOException e) { // $JL-EXC$
 			}
 		}
 	}
 
 	@Override
 	protected Object nativeToJava(final TransferData transferData) {
 		ByteArrayInputStream in = null;
 		DataInputStream dataIn = null;
 
 		try {
 			final byte[] bytes = (byte[]) super.nativeToJava(transferData);
 			if (bytes == null || bytes.length == 0) {
 				return null;
 			}
 			in = new ByteArrayInputStream(bytes);
 			dataIn = new DataInputStream(in);
 			final int len = dataIn.readInt();
 			final byte[] uriBytes = new byte[len];
 			dataIn.readFully(uriBytes);
 			final List<String> uriStrings = fromTransferBytes(uriBytes);
 			return new UriTransferData(uriStrings);
 		} catch (final IOException e) {
 			T.racer().error("Error when writing transfer data", e); //$NON-NLS-1$
 			return null;
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 					dataIn.close();
 				}
 			} catch (final IOException e) {
 				//UriTransfer.sTracer.error("Error while reading stream", e); //$NON-NLS-1$
 			}
 		}
 	}
 
 	private byte[] toTransferBytes(final List<String> uriStrings) throws IOException {
 		final StringBuilder b = new StringBuilder();
 		for (final Iterator<String> iter = uriStrings.iterator(); iter.hasNext();) {
 			final String s = iter.next();
 			b.append(s);
 			if (iter.hasNext()) {
				//b.append(UriTransfer.URI_SEP);
 			}
 		}
 		return b.toString().getBytes(UriTransfer.CHARSET);
 	}
 
 	private List<String> fromTransferBytes(final byte[] bytes) throws IOException {
 		final String all = new String(bytes, UriTransfer.CHARSET);
 		final String[] parts = all.split(Pattern.quote(UriTransfer.URI_SEP));
 		return Arrays.asList(parts);
 	}
 
 	@Override
 	public String toString() {
 		return UriTransfer.TYPE_NAME;
 	}
 
 	private UriTransfer() {
 	}
 }
