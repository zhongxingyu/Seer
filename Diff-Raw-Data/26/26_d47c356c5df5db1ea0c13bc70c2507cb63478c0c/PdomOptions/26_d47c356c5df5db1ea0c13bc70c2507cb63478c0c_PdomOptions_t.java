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
 
import java.io.File;
import java.io.IOException;
 import java.math.BigDecimal;
 
 import com.sun.jna.Pointer;
 import com.sun.jna.Structure;
 
 import core.GTerrorJava;
 import core.StrArray;
 
 public class PdomOptions extends Structure {
   public double evalue_cutoff;
   public Pointer hmm_files;
   public int nof_threads, chain_max_gap_length, write_alignments, write_aaseqs,
       cutoff;
 
  private void checkpdomfiles(StrArray pdomfiles) throws IOException {
    for (String filename : pdomfiles.to_a()) {
      File f = new File(filename);
      if (!f.exists()) {
        throw new IOException("HMM file '" + filename + "' does not exist");
      }
    }
  }
  
  public PdomOptions(StrArray pdomfiles) throws IOException {
     if (pdomfiles == null) {
       throw new NullPointerException("received a null pHMM file array");
     }
    checkpdomfiles(pdomfiles);
     this.nof_threads = 2;
     this.chain_max_gap_length = 50;
     this.evalue_cutoff = 0.0001;
     this.write_alignments = 0; // TODO: make this configurable
     this.write_aaseqs = 0; // TODO: make this configurable
     this.hmm_files = pdomfiles.to_ptr();
     this.cutoff = 0; // use trusted cutoff by default
   }
 
   public void set_evalue_cutoff(double cutoff) throws GTerrorJava {
     if (BigDecimal.valueOf(cutoff).compareTo(BigDecimal.valueOf(0)) < 0) {
       throw new GTerrorJava("E-value cutoff must not be negative!");
     }
     this.evalue_cutoff = cutoff;
   }
 
   public void set_chain_max_gap_length(int length) throws GTerrorJava {
     if (length < 0) {
       throw new GTerrorJava("chain gap length must not be negative!");
     }
     this.chain_max_gap_length = length;
   }
 
   public void set_number_of_threads(int nof_threads) throws GTerrorJava {
    if (nof_threads < 1) {
      throw new GTerrorJava("number of threads must not <1");
     }
     this.nof_threads = nof_threads;
   }
 
  public void set_pdomfiles(StrArray pdomfiles) throws IOException {
     if (pdomfiles == null) {
       throw new NullPointerException("received a null pHMM file array");
     }
    checkpdomfiles(pdomfiles);
     this.hmm_files = pdomfiles.to_ptr();
   }
   
   public void set_trusted_cutoff() {
     this.cutoff = 0;
   }
   
   public void set_gathering_cutoff() {
     this.cutoff = 1;
   }
   
   public void disable_cutoff() {
     this.cutoff = 2;
   }
 }
