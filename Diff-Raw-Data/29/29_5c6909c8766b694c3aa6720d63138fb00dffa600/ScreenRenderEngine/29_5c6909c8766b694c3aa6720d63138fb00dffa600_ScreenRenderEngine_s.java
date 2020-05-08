 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2009-2010 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package parchment.render;
 
 import org.freedesktop.cairo.Context;
import org.gnome.gtk.PaperSize;
 import org.gnome.pango.FontDescription;
 
import parchment.format.Manuscript;
import quill.textbase.Folio;

 /**
  * A render engine outputting to a page size with an aspect ratio actually
  * appropriate for reading on-screen in a PDF viewer, not printed to paper.
  * 
  * @author Andrew Cowie
  */
 /*
  * Note that this isn't meant to be the screen preview of some other document
  * form; it's a stylesheet in its own right, with typefaces chosen
  * accordingly.
  */
 public class ScreenRenderEngine extends RenderEngine
 {
    public ScreenRenderEngine(PaperSize paper, Manuscript manuscript, Folio folio) {
        super(paper, manuscript, folio);
     }
 
     protected void specifyFonts(final Context cr) {
         serifFace = new Typeface(cr, new FontDescription("Linux Libertine, 9.1"), 0.0);
 
         monoFace = new Typeface(cr, new FontDescription("Inconsolata, 8.3"), 0.0);
 
         sansFace = new Typeface(cr, new FontDescription("Liberation Sans, 7.3"), 0.0);
 
         headingFace = new Typeface(cr, new FontDescription("Linux Libertine O C"), 0.0);
     }
 }
