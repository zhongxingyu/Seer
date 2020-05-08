 /*
  * The MIT License
  * 
  * Copyright (c) 2010 Petar Petrov
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.vexelon.mobileops;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 public enum GLBRequestType {
 	
 	LOGIN("/mg/myglobul.portal", ""),
 	GET_BALANCE("/mg/myglobul.bch", "action=billcheckperform"),
 	GET_MINUTES("/mg/myglobul.bch", "action=bundlecheckperform"),
 	GET_BANDWIDTH("/mg/myglobul.bch", "action=gprscheckperform"),
 	GET_CREDITLIMIT("/mg/myglobul.bch", "action=creditlimitcheckperform"),
 	GET_MSPACKAGE("/mg/myglobul.bch", "action=smsmmsCheckPerform"),
 	LOGOUT("/mg/myglobul.portal", "action=logout");
 
 	private final String path;
 	private final String params;
	private List<NameValuePair> list;
 	
 	GLBRequestType(String path, String params) {
 		this.path = path;
 		this.params = params;
 	}
 	
 	public String getPath() {
 		return this.path;
 	}
 	
 	public String getParams() {
 		return this.params;
 	}
 	
 	public List<NameValuePair> getParamsAsList() {
 		if (list == null) {
 			list = new ArrayList<NameValuePair>();
 			
 			String[] pairs = params.split("&");
 			for (String pair : pairs) {
 				String[] keys = pair.split("=");
 				
 				list.add( new BasicNameValuePair(keys[0], keys[1] != null ? keys[1] : "")  );
 			}			
 		}
 		
 		return list;
 	}
 
 }
