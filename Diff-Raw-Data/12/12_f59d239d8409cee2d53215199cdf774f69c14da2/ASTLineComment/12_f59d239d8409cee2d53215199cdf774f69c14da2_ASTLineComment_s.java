 /*
 * $Id: ASTLineComment.java,v 1.3 2007-07-02 15:11:50 concentus Exp $
  * 
  * Copyright 2005 Sebastian Hasait
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
 package de.hasait.eclipse.ccg.javap.javacommentast.node;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import de.hasait.eclipse.ccg.javap.javacommentast.gen.JavaCommentParser;
 import de.hasait.eclipse.ccg.javap.javacommentast.gen.JavaCommentParserTreeConstants;
 import de.hasait.eclipse.ccg.parser.ICcgComment;
 import de.hasait.eclipse.ccg.parser.ICcgIndentSupport;
 
 /**
  * @author Sebastian Hasait (hasait at web.de)
 * @version $Revision: 1.3 $
  */
 public class ASTLineComment extends BlockOrLineComment implements ICcgIndentSupport {
 	private String _indent;
 
 	public ASTLineComment(int id) {
 		super(id);
 	}
 
 	public ASTLineComment(JavaCommentParser p, int id) {
 		super(p, id);
 	}
 
 	protected ICcgComment insertComment() {
 		ASTComment comment = ASTComment.create();
 		jjtInsertChild(comment, 1);
 		return comment;
 	}
 
 	public static ASTLineComment create() {
 		ASTLineComment lineComment = new ASTLineComment(JavaCommentParserTreeConstants.JJTLINECOMMENT);
 		lineComment.jjtAddChild(ASTLineCommentStart.create(), 0);
 		lineComment.jjtAddChild(ASTLineCommentEnd.create(), 1);
 		return lineComment;
 	}
 
 	public final String getIndent() {
 		return _indent;
 	}
 
 	public final void setIndent(final String indent) {
 		_indent = indent;
 	}
 
 	public String getSource() {
 		if (_indent != null) {
			BufferedReader vReader = new BufferedReader(new StringReader(getCommentText()));
 			StringWriter vResult = new StringWriter();
 			PrintWriter vWriter = new PrintWriter(vResult);
 			try {
 				String vLine;
 				while ((vLine = vReader.readLine()) != null) {
 					vWriter.append(_indent).append("//").println(vLine);
 				}
 			} catch (IOException e) {
 				// cannot happen
 			}
 			return vResult.getBuffer().toString();
 		}
 		return super.getSource();
 	}
 }
