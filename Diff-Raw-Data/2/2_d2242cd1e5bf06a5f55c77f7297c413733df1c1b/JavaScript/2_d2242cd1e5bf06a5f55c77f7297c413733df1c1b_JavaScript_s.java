 /*
  * Copyright 2011 Jonathan Anderson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package me.footlights.api.ajax;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 
 /** Some JavaScript code (guarantee closure?). */
 public class JavaScript implements AjaxResponse
 {
 	public static interface Sink { public void accept(JavaScript code); }
 
 	@Override public String mimeType() { return "text/javascript"; }
 	@Override public InputStream data() { return new ByteArrayInputStream(code().getBytes()); }
 
 	public JavaScript() { this(""); }
 	public JavaScript(String code)
 	{
 		builder = new StringBuilder(code);
 		frozen = null;
 	}
 
 	public JavaScript append(JavaScript code)
 	{
 		append(code.exec());
 		return this;
 	}
 
 	public JavaScript append(String code)
 	{
 		if (frozen != null)
 			throw new UnsupportedOperationException(
 				"append()'ing to a frozen JavaScript");
 
 		builder.append(code);
 		return this;
 	}
 
 	public JavaScript appendText(String text) { return append(sanitizeText(text)); }
 
 	public JavaScript exec(JavaScript code)
 	{
 		return append(".exec('").appendText(code.asScript()).append("')");
 	}
 
 	/** JavaScript for an Ajax call. */
 	public static JavaScript ajax(String code)
 	{
 		return new JavaScript()
 			.append("context.ajax('").append(JavaScript.sanitizeText(code)).append("');");
 	}
 
 	public static JavaScript ajax(String code, String context)
 	{
 		return new JavaScript()
 			.append("sandboxes.getOrCreate(")
 			.append("'").append(context).append("', ")
 			.append("sandboxes['global']")
 			.append(")")
 			.append(".ajax('").append(JavaScript.sanitizeText(code)).append("');");
 	}
 
 	public static JavaScript log(String message)
 	{
 		return new JavaScript()
 			.append("context.log('")
 			.appendText(message)
 			.append("');");
 	}
 
 	/** Make a string safe to put within single quotes. */
 	public static String sanitizeText(String input)
 	{
 		return input
 			.replaceAll("\\\\", "\\\\\\\\")
 			.replaceAll("'", "\\\\'")
 			.replaceAll("\n", "\\\\n")
 			;
 	}
 
	@Override public String toString() { return builder.toString(); }

 	/** The user-specified code, without any encapsulation. */
 	public String asScript() { return code(); }
 
 	/**
 	 * Encapsulate code in a function.
 	 *
 	 * @param functionName    A name for the, er, anonymous function. Useful when debugging.
 	 */
 	public String asFunction(String functionName)
 	{
 		return "(function " + sanitizeText(functionName) + "(){" + code() + "})";
 	}
 
 	/** Encapsulate code in an anonymous function. */
 	public String asFunction() { return asFunction(""); }
 
 	/** JavaScript to execute the code inside an anonymous function scope. */
 	public String exec() { return asFunction() + "();"; }
 
 	private String code()
 	{
 		// TODO(jon): sanitization?
 		if (frozen == null) frozen = builder.toString();
 		return frozen;
 	}
 
 	private final StringBuilder builder;
 	private String frozen;
 }
