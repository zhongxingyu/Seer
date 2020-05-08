 /**
  *
  * This is the MIT License
  * http://www.opensource.org/licenses/mit-license.php
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
  *
  */
 package com.yosanai.java.aws.console;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 /**
  * @author Saravana Perumal Shanmugam
 * 
  */
 public class Messages {
     private static final String BUNDLE_NAME = "com.yosanai.java.aws.console.messages"; //$NON-NLS-1$
 
     private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
 
     private Messages() {
     }
 
     public static String getString(String key) {
         try {
             return RESOURCE_BUNDLE.getString(key);
         } catch (MissingResourceException e) {
             return '!' + key + '!';
         }
     }
 }
