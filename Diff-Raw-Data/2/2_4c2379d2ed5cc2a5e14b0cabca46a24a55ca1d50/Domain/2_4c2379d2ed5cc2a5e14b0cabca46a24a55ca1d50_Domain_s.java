 package com.github.musikk.dns;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 public class Domain implements MessageContent<Domain> {
 	private final Collection<String> labels = new ArrayList<>();
 
 	/**
 	 * Creates a new empty {@code Domain}.
 	 */
 	public Domain() {
 	}
 
 	/**
 	 * Creates a new {@code Domain} with the given {@code labels}.
 	 *
 	 * @param labels
 	 *            the labels for the domain
 	 */
 	public Domain(String... labels) {
 		this.labels.addAll(Arrays.asList(labels));
 	}
 
 	/**
 	 * Convenience method that splits the given name at its period characters
 	 * and returns a new {@code Domain} with appropriate labels.
 	 *
 	 * @param name
 	 *            a domain name
 	 * @return the {@code Domain} representing the {@code name}
 	 */
 	public static Domain fromQName(String name) {
 		return new Domain(name.split("\\."));
 	}
 	@Override
 	public Domain toBytes(ByteBuffer buf) {
 		// TODO use compression
 		for (String l : labels) {
 			Util.writeCharacterString(buf, l.getBytes());
 		}
 		buf.put((byte) 0);
 		return this;
 	}
 
 	@Override
 	public Domain fromBytes(ByteBuffer buf) throws IOException {
 		labels.clear();
 		while (true) {
 			int labelLength = buf.get() & 0xFF;
 			if (labelLength == 0) {
 				break;
 			}
 			if ((labelLength & 0b1100_0000) == 0b1100_0000) {
 				// TODO save the labels in some sort of prefix tree to avoid duplicate parsing
				int pointerOffset = (labelLength & 0b0011_1111) << 8 | buf.get();
 				// pointer
 				Domain pointee = new Domain();
 				pointee.fromBytes((ByteBuffer) buf.duplicate().position(pointerOffset));
 				labels.addAll(pointee.labels);
 				break; // pointer must be last
 			}
 			byte[] labelBytes = new byte[labelLength];
 			buf.get(labelBytes);
 			labels.add(new String(labelBytes));
 		}
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		return "Domain [labels=" + labels + "]";
 	}
 }
