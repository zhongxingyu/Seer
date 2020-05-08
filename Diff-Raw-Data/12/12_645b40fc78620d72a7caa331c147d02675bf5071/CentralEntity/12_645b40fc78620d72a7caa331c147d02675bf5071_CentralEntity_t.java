 /*
  * CentralEntity.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.domain;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.io.IOException;
import java.io.InvalidObjectException;
 import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 
 import com.google.common.primitives.Longs;
 
 @MappedSuperclass
 abstract class CentralEntity implements Serializable {
 	private static final long serialVersionUID = -644686735609065541L;
 
 	private static final Pattern UUID_REGEX = Pattern
 			.compile("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
 
 	private UUID uuid;
 
 	protected CentralEntity() {
 		uuid = UUID.randomUUID();
 	}
 
 	@Id
 	@Column(length = 36, nullable = false, updatable = false)
 	public String getId() {
 		return uuid.toString();
 	}
 
 	@Transient
 	protected byte[] getIdBytes() {
 		byte[] idBytes = new byte[16];
 		byte[] upperBytes = Longs.toByteArray(uuid.getMostSignificantBits());
 		byte[] lowerBytes = Longs.toByteArray(uuid.getLeastSignificantBits());
 		System.arraycopy(upperBytes, 0, idBytes, 0, 8);
 		System.arraycopy(lowerBytes, 0, idBytes, 8, 8);
 		return idBytes;
 	}
 
 	protected void setId(String id) {
 		checkNotNull(id);
 		checkArgument(UUID_REGEX.matcher(id).matches());
 		uuid = UUID.fromString(id);
 	}
 
 	private void readObject(ObjectInputStream ois) throws IOException,
 			ClassNotFoundException {
 		ois.defaultReadObject();
 		checkNotNull(uuid);
 	}
	
	/*
	 * See Effective Java, second edition, item 74.
	 */
	@SuppressWarnings("unused")
	private void readObjectNoData() throws ObjectStreamException {
		throw new InvalidObjectException("stream data required");
	}
 }
