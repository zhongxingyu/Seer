 /* Copyright (C) 2012 Mark
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.nguyenmp.gauchospace;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 
 import org.apache.http.client.CookieStore;
 import org.xml.sax.SAXNotRecognizedException;
 import org.xml.sax.SAXNotSupportedException;
 
 import com.nguyenmp.gauchospace.parser.WeeklyOutlineParser.UnparsableHtmlException;
 import com.nguyenmp.gauchospace.thing.Discussion;
 import com.nguyenmp.gauchospace.thing.Forum;
 import com.nguyenmp.gauchospace.thing.grade.GradeFolder;
 import com.nguyenmp.gauchospace.thing.grade.GradeItem;
 
 public class Tester {
 	public static void main(String[] args) throws SAXNotRecognizedException, SAXNotSupportedException, IOException, TransformerFactoryConfigurationError, TransformerException, UnparsableHtmlException{
 		CookieStore cookies = GauchoSpaceClient.login("username", "password");
 		List<Forum> forums = GauchoSpaceClient.getForums(1, cookies);
//		List<Discussion> discussions = GauchoSpaceClient.getForum(1, cookies);
		System.out.println(forums);
 	}
 	
 	private static int indent = 0;
 	private static void printFolder(GradeFolder folder) {
 		printIndent(indent); System.out.println(folder.getTitle());
 		indent++;
 		for (GradeItem item : folder.getItems()) {
 			if (item instanceof GradeFolder) {
 				printFolder((GradeFolder) item);
 			} else {
 				printIndent(indent); System.out.println(item.toString());
 			}
 		}
 		indent--;
 		printIndent(indent); System.out.println(folder.toString());
 	}
 	
 	private static void printIndent(int indent) {
 		for(int i = 0; i < indent; i++) 
 			System.out.print("\t");
 	}
 }
