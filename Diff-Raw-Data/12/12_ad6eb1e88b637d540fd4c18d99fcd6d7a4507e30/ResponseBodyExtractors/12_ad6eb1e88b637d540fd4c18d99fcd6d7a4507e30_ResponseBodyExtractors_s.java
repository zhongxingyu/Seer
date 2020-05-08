 package com.anthavio.hatatitla.inout;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.anthavio.hatatitla.Cutils;
 import com.anthavio.hatatitla.HttpHeaderUtil;
 import com.anthavio.hatatitla.SenderResponse;
 
 /**
  * Storage for ResponseExtractor
  * 
  * @author martin.vanek
  *
  */
 public class ResponseBodyExtractors {
 	/*
 		private class ExtractorEntry<T> {
 
 			private final String mimeType; // null means any response mime type
 
 			private final int[] httpStatusCodes; //null means any http status code
 
 			private final ResponseBodyExtractor<T> extractor;
 
 			public ExtractorEntry(ResponseBodyExtractor<T> extractor) {
 				this(extractor, null, null);
 			}
 
 			public ExtractorEntry(ResponseBodyExtractor<T> extractor, String mimeType) {
 				this(extractor, mimeType, null);
 			}
 
 			public ExtractorEntry(ResponseBodyExtractor<T> extractor, String mimeType, int... httpStatusCodes) {
 				if (extractor == null) {
 					throw new IllegalArgumentException("Null extractor");
 				}
 				this.extractor = extractor;
 
 				if ("*".equals(mimeType)) {
 					this.mimeType = null;
 				} else {
 					this.mimeType = mimeType;
 				}
 
 				if (httpStatusCodes == null || httpStatusCodes.length == 0 || httpStatusCodes[0] == 0) {
 					this.httpStatusCodes = null;
 				} else {
 					this.httpStatusCodes = httpStatusCodes;
 				}
 			}
 
 		}
 
 		private List<ExtractorEntry<?>> extractors = new ArrayList<ExtractorEntry<?>>();
 	*/
 	private Map<String, ResponseExtractorFactory> factories = new HashMap<String, ResponseExtractorFactory>();
 
 	public ResponseBodyExtractors() {
 		//extractors.add(new ExtractorEntry<String>(STRING));
 		//extractors.add(new ExtractorEntry<byte[]>(BYTES));
 
 		JaxbExtractorFactory jaxbFactory = new JaxbExtractorFactory();
 		this.factories.put("application/xml", jaxbFactory);
 		this.factories.put("text/xml", jaxbFactory);
 
 		//Jackson support is optional
 		try {
 			Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
 			factories.put("application/json", new Jackson2ExtractorFactory());
 		} catch (ClassNotFoundException cnf) {
 			try {
 				Class.forName("org.codehaus.jackson.map.ObjectMapper");
 				factories.put("application/json", new Jackson1ExtractorFactory());
 			} catch (ClassNotFoundException cnf2) {
 			}
 		}
 	}
 
 	public ResponseExtractorFactory getExtractorFactory(String mimeType) {
 		return factories.get(mimeType);
 	}
 
 	public void setExtractorFactory(ResponseExtractorFactory extractorFactory, String mimeType) {
 		if (extractorFactory == null) {
 			throw new IllegalArgumentException("extractor factory is null");
 		}
 		factories.put(mimeType, extractorFactory);
 	}
 
 	public static final ResponseBodyExtractor<String> STRING = new ResponseBodyExtractor<String>() {
 
 		@Override
 		public String extract(SenderResponse response) throws IOException {
 			return HttpHeaderUtil.readAsString(response);
 		}
 	};
 
 	public static final ResponseBodyExtractor<byte[]> BYTES = new ResponseBodyExtractor<byte[]>() {
 
 		@Override
 		public byte[] extract(SenderResponse response) throws IOException {
 			return HttpHeaderUtil.readAsBytes(response);
 		}
 	};
 
 	/**
 	 * Extracts Response into desired resultType or fails miserably.
 	 * This method does NOT close Response
 	 */
 	public <T extends Serializable> T extract(SenderResponse response, Class<T> resultType) throws IOException {
 		String contentType = response.getFirstHeader("Content-Type");
 		ResponseBodyExtractor<?> extractor = getExtractor(contentType, response, resultType);
 		if (extractor == null) {
 			throw new IllegalArgumentException("No extractor found for class " + resultType.getName() + " and Content-Type "
 					+ contentType);
 		}
 		return (T) extractor.extract(response);
 	}
 
 	public <T extends Serializable> ResponseBodyExtractor<T> getExtractor(String contentType, SenderResponse response,
 			Class<T> clazz) {
 		// Ignore Content-Type for String or Byte Array result
 		if (clazz.equals(String.class)) {
 			return (ResponseBodyExtractor<T>) ResponseBodyExtractors.STRING;
 		} else if (clazz.equals(byte[].class)) {
 			return (ResponseBodyExtractor<T>) ResponseBodyExtractors.BYTES;
 		}
 
 		if (Cutils.isEmpty(contentType)) {
 			throw new IllegalArgumentException("Content-Type header not found");
 		}
 
		String mimeType = HttpHeaderUtil.getMimeType(contentType, "missing/mimetype");
 		ResponseExtractorFactory extractorFactory = factories.get(mimeType);
 		if (extractorFactory == null) {
 			throw new IllegalArgumentException("No extractor factory found for mime type " + mimeType);
 		}
 		ResponseBodyExtractor<T> extractor = extractorFactory.getExtractor(response, clazz);
 		return extractor;
 	}
 
 }
