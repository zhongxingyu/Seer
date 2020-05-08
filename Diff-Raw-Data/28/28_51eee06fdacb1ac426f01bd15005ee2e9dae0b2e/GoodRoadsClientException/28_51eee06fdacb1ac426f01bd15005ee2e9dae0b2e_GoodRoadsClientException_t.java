 package ru.goodroads.api;
 
 import ru.goodroads.data.ErrorCode;
 import ru.goodroads.net.jsonrpc.JSONRPCClientException;
 
 public class GoodRoadsClientException extends JSONRPCClientException {
 
 	private final int code;
	private final String message;
 	private static final long serialVersionUID = 1L;
 	
 	public GoodRoadsClientException(ErrorCode ec) {
 		super(ec.getDescription());
		this.message = ec.getDescription();
 		this.code = ec.getCode();
 	}
 
 	public GoodRoadsClientException(String message, int code) {
 		super(message);
		this.message = message;
 		this.code = code;
 	}
 	
 	public GoodRoadsClientException(String message) {
 		super(message);
		this.message = message;
 		this.code = ErrorCode.CLIENT_SIDE_ERROR.getCode();
 	}
 
 	public int getCode() {
 		return code;
 	}
 	
 	@Override
	public String getMessage() {
		return message;
	}
	
	@Override
 	public String getLocalizedMessage() {
		if (ErrorCode.fromCode(code) != ErrorCode.CLIENT_SIDE_ERROR)
			return ErrorCode.fromCode(code).getLocalizedDescription();
		else
			return message;
 	}
 }
