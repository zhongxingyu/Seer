 package com.orgsync.api;
 
 import com.orgsync.api.messages.ApiError;
 
 public class ApiResponse<T> {
 
 	private final T result;
 	private final ApiError error;
 
	public static final <T> ApiResponse<T> error(final ApiError error) {
		return new ApiResponse<T>(null, error);
	}

	@SuppressWarnings("unchecked")
	public static final <T> ApiResponse<T> success(final Object result) {
		return new ApiResponse<T>((T) result, null);
	}

	private ApiResponse(final T result, final ApiError error) {
		this.result = result;
 		this.error = error;
 	}
 
 	public boolean isSuccess() {
 		return error == null;
 	}
 
 	public final T getResult() {
 		return result;
 	}
 
 	public final ApiError getError() {
 		return error;
 	}
 
 }
