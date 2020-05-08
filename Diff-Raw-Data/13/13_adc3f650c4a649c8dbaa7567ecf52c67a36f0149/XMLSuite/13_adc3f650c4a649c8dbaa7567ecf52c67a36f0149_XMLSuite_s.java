 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: XMLSuite.java,v $
	$Date: 2003-06-20 00:37:24 $ 
 */
 package net.sf.statcvs.output.xml;
 
 import java.io.*;
 import java.util.*;
 
 import net.sf.statcvs.model.*;
 
 /**
  * XMLSuite
  * 
  * @author Tammo van Lessen
  */
 public class XMLSuite {
 
 	/**
 	 * 
 	 */
 	private XMLSuite()
 	{
 	}
 	
 	public static void generate(CvsContent content, DocumentRenderer renderer) 
 		throws IOException {
 		renderer.render(new CommitLogDocument(content));
 		renderer.render(new DirectorySizesDocument(content));
 		renderer.render(new IndexDocument(content));
 
 		// author pages
 		for (Iterator i = content.getAuthors().iterator(); i.hasNext(); ) {
 			Author author = (Author)i.next();
 			renderer.render(new AuthorDocument(content, author,
 											   getAuthorPageFilename(author)));
 		}
 	}
 
 	/**
 	 * Escapes evil characters in author's names. E.g. "#" must be escaped
 	 * because for an author "my#name" a page "author_my#name.html" will be
 	 * created, and you can't link to that in HTML
 	 * @param authorName an author's name
 	 * @return a version safe for creation of files and URLs
 	 */
 	public static String escapeAuthorName(String authorName) {
 		return authorName.replaceAll("#", "_");
 	}
  
 
 	/**
 	 * @param author an author
 	 * @return filename for author's page
 	 */
 	public static String getAuthorPageFilename(Author author) {
 		return "user_" + escapeAuthorName(author.getName());
 	}
 
 }
