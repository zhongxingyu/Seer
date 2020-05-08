 package org.zeromq;
 
 public class ZmqException extends RuntimeException {
 
	public static enum Code {

		EADDRINUSE(ZmqLibrary.EADDRINUSE),
		EADDRNOTAVAIL(ZmqLibrary.EADDRNOTAVAIL),
		ECONNREFUSED(ZmqLibrary.ECONNREFUSED),
		EFSM(ZmqLibrary.EFSM),
		EINPROGRESS(ZmqLibrary.EINPROGRESS),
		EMTHREAD(ZmqLibrary.EMTHREAD),
		ENETDOWN(ZmqLibrary.ENETDOWN),
		ENOBUFS(ZmqLibrary.ENOBUFS),
		ENOCOMPATPROTO(ZmqLibrary.ENOCOMPATPROTO),
		ENOTSUP(ZmqLibrary.ENOTSUP),
		EPROTONOSUPPORT(ZmqLibrary.EPROTONOSUPPORT),
		ETERM(ZmqLibrary.ETERM);

		public static Code findByCode(final int code) {
			for (final Code x : values())
				if (code == x.code)
					return x;
			throw new IllegalArgumentException("Unknown " + Code.class.getSimpleName() + ": " + code);
		}

		public final int code;

		Code(final int code) {
			this.code = code;
		}

	}

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
