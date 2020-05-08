 package org.zeromq;
 
 public class ZmqException extends RuntimeException {
 
 	private static final long serialVersionUID = 1L;
 	private final int code;
 
 	ZmqException(final String msg, final int code) {
 		super(msg);
 		this.code = code;
 	}
 
 	public int getCode() {
 		return this.code;
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder b = new StringBuilder();
 		b.append(getClass().getSimpleName());
 		b.append(" (");
 		b.append(getCode());
 		b.append(") ");
 		b.append(getMessage());
 		return b.toString();
 	}
 
 }
