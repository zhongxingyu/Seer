 /*
   Copyright (c) 2010 Sascha Steinbiss <steinbiss@zbh.uni-hamburg.de>
   Copyright (c) 2010 Center for Bioinformatics, University of Hamburg
 
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
 
 package ltr;
 
 import com.sun.jna.NativeLong;
 import com.sun.jna.Pointer;
 import com.sun.jna.Structure;
 
 import core.Bioseq;
 import core.GTerrorJava;
 import core.Range;
 
 public class PBSOptions extends Structure {
   public int radius, max_edist;
   public Range alilen, offsetlen, trnaoffsetlen;
   public int ali_score_match, ali_score_mismatch, ali_score_insertion,
       ali_score_deletion;
   public Pointer trna_lib;
 
   public PBSOptions(String trna_lib_file) throws GTerrorJava {
     if (trna_lib_file == null) {
       throw new NullPointerException("received a null tRNA library string");
     }
     this.radius = 30;
     this.max_edist = 2;
     this.alilen.start = new NativeLong(11);
     this.alilen.end = new NativeLong(30);
     this.offsetlen.start = new NativeLong(0);
     this.offsetlen.end = new NativeLong(5);
     this.trnaoffsetlen.start = new NativeLong(0);
     this.trnaoffsetlen.end = new NativeLong(5);
     this.ali_score_match = 5;
     this.ali_score_mismatch = -10;
     this.ali_score_insertion = -20;
     this.ali_score_deletion = -20;
     this.trna_lib = (new Bioseq(trna_lib_file)).to_ptr();
   }
 
   public void set_alignment_length(long min, long max) throws GTerrorJava {
     if (min > max) {
       throw new GTerrorJava("min (" + min + ") > max (" + max + ")");
     }
     this.alilen.start = new NativeLong(min);
     this.alilen.end = new NativeLong(max);
   }
 
   public void set_pbs_offset_length(long min, long max) throws GTerrorJava {
     if (min > max) {
       throw new GTerrorJava("min (" + min + ") > max (" + max + ")");
     }
     this.offsetlen.start = new NativeLong(min);
     this.offsetlen.end = new NativeLong(max);
   }
 
   public void set_trna_offset_length(long min, long max) throws GTerrorJava {
     if (min > max) {
       throw new GTerrorJava("min (" + min + ") > max (" + max + ")");
     }
     this.trnaoffsetlen.start = new NativeLong(min);
     this.trnaoffsetlen.end = new NativeLong(max);
   }
 
   public void set_alignment_scores(int match, int mismatch, int deletion,
       int insertion) {
     this.ali_score_match = match;
     this.ali_score_mismatch = mismatch;
     this.ali_score_insertion = insertion;
     this.ali_score_deletion = deletion;
   }
 
  public void set_radius(int radius) throws GTerrorJava {
    if (radius < 1) {
      throw new GTerrorJava("radius must not be zero or negative");
    }
     this.radius = radius;
   }
 
  public void set_max_edist(int dist) throws GTerrorJava {
    if (dist < 0) {
      throw new GTerrorJava("maximum edist must not be negative");
    }
     this.max_edist = dist;
   }
 
   public void set_trna_library(String trna_lib_file) throws GTerrorJava {
     this.trna_lib = (new Bioseq(trna_lib_file)).to_ptr();
   }
 
 }
