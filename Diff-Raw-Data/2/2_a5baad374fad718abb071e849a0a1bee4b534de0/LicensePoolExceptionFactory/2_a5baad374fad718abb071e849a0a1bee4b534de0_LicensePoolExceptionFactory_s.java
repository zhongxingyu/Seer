 package com.pearson.ed.lplc.exception;
 
 import java.util.Properties;
 
 import org.hibernate.HibernateException;
 import org.springframework.dao.DataAccessException;
 
 public class LicensePoolExceptionFactory {
 	private String defaultKey;
 	private Properties codeDescProperties;
 		
 	public void setCodeDescProperties(Properties codeDescProperties) {
 		this.codeDescProperties = codeDescProperties;
 	}
 
 	public void setDefaultKey(String defaultKey) {
 		this.defaultKey = defaultKey;
 	}
 	
 	public LicensePoolException getLicensePoolException(String message) {
 		return getLicensePoolException(message, null);
 	}
 	
 	public LicensePoolException getLicensePoolException(Throwable cause) {
 		return getLicensePoolException(null, cause);
 	}
 	
 	public LicensePoolException getLicensePoolException(String message, Throwable cause) {
 		String code = null;
 		String desc = null;
 		if (cause == null) {
 			code = defaultKey;
 		}
 		else if (cause instanceof DataAccessException ||
 				cause instanceof HibernateException) {
 			code = "LPLC0003";
 		}
 		else if (cause instanceof LPLCFieldsNotValidException) {
 			code = "LPLC0004";
 		}
 		else if (cause instanceof ComponentValidationException) {
 			code = "LPLC0005";
 		}
 		else if (cause instanceof RequiredObjectNotFound) {
 			code = "LPLC0006";
 		}
 		else if (cause instanceof UnsupportedFunctionalityException) {
 			code = "LPLC0007";
 		}
 		else if (cause instanceof RetriesExceededException) {
 			code = "LPLC0008";
 		}		
 		else if (cause instanceof ObjectAlreadyExists) {
 			code = "LPLC0009";
 		}
 		else if (cause instanceof NewSubscriptionsDeniedException) {
 			code = "LPLC0010";
 		}
 		else if (cause instanceof LicensePoolExpiredException) {
			code = "'LPLC0011";
 		}
 		else if (cause instanceof LicensePoolForFutureException) {
 			code = "LPLC0012";
 		}
 		else if (cause instanceof LicensePoolUnavailableException) {
 			code = "LPLC0013";
 		}
 		else if (cause instanceof LPLCBaseException) {
 			code = "LPLC0002";
 		}
 		else {
 			code = defaultKey;
 		}
 		desc = codeDescProperties.getProperty(code);
 		if (desc == null) {
 			code = defaultKey;
 			desc = codeDescProperties.getProperty(code);
 		}
 		
 		return new LicensePoolException(message, cause, new LicensePoolExceptionElement(code, desc));
 	}
 }
