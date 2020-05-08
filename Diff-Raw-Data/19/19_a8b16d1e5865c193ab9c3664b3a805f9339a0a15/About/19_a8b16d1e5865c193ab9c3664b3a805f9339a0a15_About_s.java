 //
 // About.java
 //
 
 /*
 LOCI Checkstyle checks.
 
 Copyright (c) 2006, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.checks;
 
 import javax.swing.JOptionPane;
 
 /**
  * Displays a small information dialog about this package.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/projects/loci-checks/src/main/java/loci/checks/About.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/java/trunk/projects/loci-checks/src/main/java/loci/checks/About.java">SVN</a></dd></dl>
  *
  * @author Curtis Rueden ctrueden at wisc.edu
  */
 public final class About {
 
   /** URL of LOCI Software web page. */
   public static final String URL_LOCI_SOFTWARE =
     "http://www.loci.wisc.edu/software";
 
   /** URL of Checkstyle web page. */
   public static final String URL_CHECKSTYLE =
     "http://checkstyle.sourceforge.net/";
 
  private About() { }
 
   public static void main(String[] args) {
     JOptionPane.showMessageDialog(null,
       "LOCI Checkstyle checks, revision @svn.revision@, built @date@\n" +
       "Download Checkstyle from " + URL_CHECKSTYLE + "\n" +
       "Download LOCI software from " + URL_LOCI_SOFTWARE,
       "LOCI Checkstyle checks", JOptionPane.INFORMATION_MESSAGE);
     System.exit(0);
   }
 
 }
