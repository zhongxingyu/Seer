 package edu.helsinki.sulka.configurations;
 
 public class TestLoginCodeConfiguration {
 	/**
 	 * Setups a TestLoginCodeConfiguration configuration that disallows all test logins.
 	 */
 	public TestLoginCodeConfiguration() { this.code = null; }
 	
 	/**
 	 * Setups a TestLoginCodeConfiguration configuration that allows test logins.
 	 * @param code A random, lengthy string.
 	 */
 	public TestLoginCodeConfiguration(String code) { this.code = code; }
 	
 	private String code = null;
 	public boolean isCorrectCode(final String userCode) {
		return this.code != null && this.code.length() > 0 && this.code.equals(userCode);
 	}
 }
