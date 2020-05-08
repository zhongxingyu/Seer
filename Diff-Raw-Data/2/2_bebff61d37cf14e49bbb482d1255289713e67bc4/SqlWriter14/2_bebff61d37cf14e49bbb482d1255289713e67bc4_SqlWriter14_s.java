 /*
  * MediaWiki import/export processing tools
  * Copyright 2005 by Brion Vibber
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * $Id$
  */
 
 package org.mediawiki.importer;
 
 import java.util.Random;
 
 
 public class SqlWriter14 extends SqlWriter {
 	Random random;
 	Page currentPage;
 	Revision lastRevision;
 	
 	public SqlWriter14(SqlFileStream output) {
 		super(output);
 		random = new Random();
 	}
 	
 	public void writeStartPage(Page page) {
 		currentPage = page;
 		lastRevision = null;
 	}
 	
 	public void writeEndPage() {
 		if (lastRevision != null)
 			writeCurRevision(currentPage, lastRevision);
 		currentPage = null;
 		lastRevision = null;
 	}
 	
 	public void writeRevision(Revision revision) {
 		if (lastRevision != null)
 			writeOldRevision(currentPage, lastRevision);
 		lastRevision = revision;
 	}
 	
 	private void writeOldRevision(Page page, Revision revision) {
 		insertRow("old", new Object[][] {
 				{"old_id", new Integer(revision.Id)},
 				{"old_namespace", new Integer(page.Title.Namespace)},
 				{"old_title", titleFormat(page.Title.Text)},
 				{"old_text", revision.Text},
 				{"old_comment", revision.Comment},
 				{"old_user", new Integer(revision.Contributor.Id)},
 				{"old_user_text", revision.Contributor.Username},
 				{"old_timestamp", timestampFormat(revision.Timestamp)},
 				{"old_minor_edit", new Integer(revision.Minor ? 1 : 0)},
 				{"old_flags", "utf-8"},
 				{"inverse_timestamp", inverseTimestamp(revision.Timestamp)}});
 	}
 	
 	private void writeCurRevision(Page page, Revision revision) {
 		insertRow("cur", new Object[][] {
				{"cur_id", new Integer(revision.Id)},
 				{"cur_namespace", new Integer(page.Title.Namespace)},
 				{"cur_title", titleFormat(page.Title.Text)},
 				{"cur_text", revision.Text},
 				{"cur_comment", revision.Comment},
 				{"cur_user", new Integer(revision.Contributor.Id)},
 				{"cur_user_text", revision.Contributor.Username},
 				{"cur_timestamp", timestampFormat(revision.Timestamp)},
 				{"cur_restrictions", page.Restrictions},
 				{"cur_counter", new Integer(0)},
 				{"cur_is_redirect", new Integer(revision.isRedirect() ? 1 : 0)},
 				{"cur_minor_edit", new Integer(revision.Minor ? 1 : 0)},
 				{"cur_random", new Double(random.nextDouble())},
 				{"cur_touched", timestampFormat(now())},
 				{"inverse_timestamp", inverseTimestamp(revision.Timestamp)}});
 	}
 }
