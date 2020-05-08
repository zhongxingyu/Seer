 /*
   Copyright (c) 2009 Philipp Carpus  <random234@gmx.net>
   Copyright (c) 2009 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

 
 package annotationsketch;
 
 import gtnative.GT;
 
 import com.sun.jna.NativeLong;
 
 import core.GTerror;
 import core.GTerrorJava;
 
 public class CanvasCairoFile extends CanvasCairo {
   public CanvasCairoFile(Style style, int width, int height,
       ImageInfo image_info) throws GTerrorJava {
     NativeLong n_width = new NativeLong(width);
     NativeLong n_height = new NativeLong(height);
 
     if (image_info == null) {
      throw new GTerrorJava("ImageInfo is not initialized");
     } else {
      canvas_ptr = GT.INSTANCE.gt_canvas_cairo_file_new(style.to_ptr(), 1,
          n_width, n_height, image_info.to_ptr());
     }
   }
 
   public CanvasCairoFile(Style style, int width, int height) {
     NativeLong n_width = new NativeLong(width);
     NativeLong n_height = new NativeLong(height);
     canvas_ptr = GT.INSTANCE.gt_canvas_cairo_file_new(style.to_ptr(), 1,
         n_width, n_height, null);
   }
 
   protected void finalize() {
     super.finalize();
   }
 
   public void to_file(String filename) throws GTerrorJava {
     GTerror err = new GTerror();
     int rval = GT.INSTANCE.gt_canvas_cairo_file_to_file(canvas_ptr, filename,
         err.to_ptr());
     if (rval != 0) {
       throw new GTerrorJava(err.get_err());
     }
   }
 
 }
