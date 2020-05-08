 //////////////////////////////////////////////////////////////////////////////
 // Clirr: compares two versions of a java library for binary compatibility
 // Copyright (C) 2003  Lars Khne
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //////////////////////////////////////////////////////////////////////////////
 
 package net.sf.clirr.event;
 
 import java.io.IOException;
 import java.io.PrintStream;
 
 /**
  * A DiffListener that reports any detected difference to
  * an XML file. That file can be used by subsequent processing steps
  * to create nice looking reports in HTML, PDF, etc.
  *
  * @author lkuehne
  */
 public final class XmlDiffListener extends FileDiffListener
 {
     private static final String DIFFREPORT = "diffreport";
     private static final String DIFFERENCE = "difference";
 
     public XmlDiffListener(String outFile) throws IOException
     {
         super(outFile);
     }
 
     public void reportDiff(ApiDifference difference)
     {
         PrintStream out = getOutputStream();
         out.print("  <" + DIFFERENCE);
        out.print(" severity=\"" + difference.getSeverity() + "\">");
        out.print(" class=\"" + difference.getAffectedClass() + "\"");
         if (difference.getAffectedMethod() != null)
         {
             out.print(" method=\"" + difference.getAffectedMethod() + "\"");
         }
         if (difference.getAffectedField() != null)
         {
             out.print(" field=\"" + difference.getAffectedField() + "\"");
         }
         out.print(difference.getReport()); // TODO: XML escapes??
         out.println("</" + DIFFERENCE + '>');
     }
 
     /**
      * Writes an XML header and toplevel tag to the xml stream.
      *
      * @see DiffListener#start()
      */
     public void start()
     {
         PrintStream out = getOutputStream();
         out.println("<?xml version=\"1.0\"?>");
         out.println('<' + DIFFREPORT + '>');
        out.println("<!--");
        out.println("  In future versions the differences will have more attributes, e.g.");
        out.println("  - affected package");
        out.println("  - affected class/interface");
        out.println("  - affected method, if any");
        out.println("  - change type (ADD/REMOVE/CHANGE)");
        out.println("  - ... anything else you need?");
        out.println("-->");
     }
 
 
     /**
      * Closes the toplevel tag that was opened in start.
      *
      * @see DiffListener#stop()
      */
     protected void writeFooter()
     {
         PrintStream out = getOutputStream();
         out.println("</" + DIFFREPORT + '>');
     }
 }
