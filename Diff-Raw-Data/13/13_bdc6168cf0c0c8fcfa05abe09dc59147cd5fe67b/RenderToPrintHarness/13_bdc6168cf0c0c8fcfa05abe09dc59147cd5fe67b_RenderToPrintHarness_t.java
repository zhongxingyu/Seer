 /*
  * DevelopmentHarness.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.client;
 
 import java.io.IOException;
 
 import nu.xom.ParsingException;
 import nu.xom.ValidityException;
 
 import org.freedesktop.cairo.Context;
 import org.freedesktop.cairo.PdfSurface;
 import org.freedesktop.cairo.Surface;
 import org.gnome.gtk.Gtk;
 import org.gnome.gtk.PaperSize;
 import org.gnome.gtk.Unit;
 
 import parchment.render.RenderEngine;
 import parchment.render.ReportRenderEngine;
import quill.textbase.DataLayer;
 import quill.textbase.Folio;
 
 public class RenderToPrintHarness
 {
    private static DataLayer data;

     public static void main(String[] args) throws ValidityException, ParsingException, IOException {
         initializeDataLayer();
         loadExampleDocument();
         runRenderPipeline();
     }
 
    private static void initializeDataLayer() {
        data = new DataLayer();
    }

     private static void loadExampleDocument() throws ValidityException, ParsingException, IOException {
         Gtk.init(null);
         data.loadDocument("tests/ExampleProgram.xml");
     }
 
     private static void runRenderPipeline() throws IOException {
         final Context cr;
         final Surface surface;
         final Folio folio;
         final RenderEngine engine;
 
         /*
          * Setup a bogus target surface
          */
 
         surface = new PdfSurface("tmp/Render.pdf", PaperSize.A4.getWidth(Unit.POINTS),
                 PaperSize.A4.getHeight(Unit.POINTS));
         cr = new Context(surface);
 
         folio = data.getActiveDocument();
 
         engine = new ReportRenderEngine(PaperSize.A4, folio.get(0));
         engine.render(cr);
 
         surface.finish();
     }
 }
