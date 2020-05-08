 package com.adaptiweb.utils.commons.param;
 
 import java.util.Map;
 
 public class ParameterQueryBuilder {
 	
 	public interface UrlEncoderProvider {
 		String encode(String value);
 	}
 
 	private static final UrlEncoderProvider plainUrlEncoder = new UrlEncoderProvider() {
 		@Override
 		public String encode(String value) {
 			return value;
 		}
 	};
 
 	ParameterMap params;
 	String baseUrl;
 
 	public ParameterQueryBuilder() {
 		this.params = new ParameterMap();
 	}
 
 	public ParameterQueryBuilder setBaseUrl(String baseUrl) {
 		this.baseUrl = baseUrl;
 		return this;
 	}
 	
 	public ParameterQueryBuilder addParameter(String name, String value) {
 		this.params.put(name, value);
 		return this;
 	}
 	
 	public ParameterQueryBuilder addAllParameters(Map<String, String> paramMap) {
 		if (paramMap != null) this.params.putAll(paramMap);
 		return this;
 	}
 	
 	public ParameterQueryBuilder addUrlParts(String... urlParts) {
 		for (String part : urlParts) {
 			String[] split = part.split("=");
 			if (split.length < 2) continue;
 			params.put(split[0], split[1]);
 		}
 		return this;
 	}
 	
 	public <I> ParameterQueryBuilder extractParameters(I input, Parameter<I>... parameters) {
 		for (Parameter<I> parameter : parameters) {
 			Object extractedValue = parameter.extractValue(input);
 			params.put(parameter.getParameterName(), formatValue(extractedValue));
 		}
 		return this;
 	}
 	
 	public String toUrlQueryString() {
 		return toUrlQueryString(plainUrlEncoder);
 	}
 	
 	public String toUrlQueryString(UrlEncoderProvider encoder) {
 		String urlQuery = toUrlQueryString(params, encoder);
		if (urlQuery == null || urlQuery.length() == 0) return baseUrl;
 		else if (baseUrl != null) return baseUrl + (baseUrl.contains("?") ? "&" : "?") + urlQuery;
 		else return urlQuery;
 	}
 	
 	@Override
 	public String toString() {
 		return toUrlQueryString();
 	}
 	
 	public ParameterMap toParameterMap() {
 		return params;
 	}
 	
 	private static String prepareParameter(boolean needAmp, Map.Entry<String, String> paramValue, UrlEncoderProvider encoder) {
 		return paramValue.getValue() == null 
 			? "" : formatParam(needAmp, paramValue.getKey(), paramValue.getValue(), encoder);
 	}
 
 	private static String formatParam(boolean needAmp, String paramName, String paramValue, UrlEncoderProvider encoder) {
 		return (needAmp ? "&" : "") + paramName + "=" + encoder.encode(paramValue);
 	}
 
 	private static String toUrlQueryString(ParameterMap parameterMap, UrlEncoderProvider encoder) {
 		StringBuilder result = new StringBuilder();
 		for (Map.Entry<String, String> paramValue : parameterMap.entrySet()) {
 			result.append(prepareParameter(result.length() > 0, paramValue, encoder));
 		}
 		return result.toString();
 	}
 
 	private static String formatValue(Object value) {
 		if (value == null) return null;
 		if (value instanceof String) return value.toString();
 		if (value instanceof Boolean) return ((Boolean) value) ? "1" : "0";
 		if (value instanceof Enum<?>) return ((Enum<?>) value).name();
 		return String.valueOf(value);
 	}
 
 }
