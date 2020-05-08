 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // genome-weaver Project
 //
 // BWT.java
 // Since: 2011/02/10
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.format.fasta.CompactFASTA;
 import org.utgenome.format.fasta.FASTAPullParser;
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.utgenome.weaver.align.SequenceBoundary.SequenceIndex;
 import org.xerial.lens.SilkLens;
 import org.xerial.silk.SilkWriter;
 import org.xerial.util.StopWatch;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Command;
 
 /**
  * Performs burrows-wheeler transform
  * 
  * @author leo
  * 
  */
 public class BWTransform implements Command
 {
     private static Logger _logger = Logger.getLogger(BWTransform.class);
 
     @Override
     public String name() {
         return "bwt";
     }
 
     @Override
     public String getOneLineDescription() {
         return "Burrows-Wheeler Transformation (BWT)";
     }
 
     @Override
     public Object getOptionHolder() {
         return this;
     }
 
     /**
      * input FASTA file (.fa, .tar.gz, .fa.gz, types are allowed)
      */
     @Argument(index = 0)
     private String fastaFile;
 
     @Override
     public void execute(String[] args) throws Exception {
 
         if (fastaFile == null)
             throw new UTGBException(UTGBErrorCode.MISSING_FILES, "no input FASTA file is given");
 
         // Output IUPAC sequence to a file
         BWTFiles files = new BWTFiles(fastaFile);
         _logger.info("input FASTA file: " + fastaFile);
         _logger.info("IUPAC file: " + files.iupacForward());
         _logger.info("index file: " + files.pacFileIndex());
 
        long totalSize = -1;
         {
             // Read the input FASTA file, then encode the sequences using the IUPAC code         
             BufferedOutputStream iupacFile = new BufferedOutputStream(new FileOutputStream(files.iupacForward()));
             SilkWriter indexOut = new SilkWriter(new BufferedWriter(new FileWriter(files.pacFileIndex())));
             IUPACSequenceWriter encoder = new IUPACSequenceWriter(iupacFile);
             FASTAPullParser fasta = new FASTAPullParser(new File(fastaFile));
            long lineCount = 1;
            long offset = 0;
             for (String desc; (desc = fasta.nextDescriptionLine()) != null; lineCount++) {
                 String seqName = CompactFASTA.pickSequenceName(desc);
                 _logger.info(String.format("reading %s", seqName));
                 for (String seq; (seq = fasta.nextSequenceLine()) != null; lineCount++) {
                     seq = seq.trim();
                     for (int i = 0; i < seq.length(); ++i) {
                         // 'A' .. 'Z'
                         char base = Character.toUpperCase(seq.charAt(i));
                         IUPAC iupac = IUPAC.encode(base);
                         if (iupac == IUPAC.None) {
                             // illegal character
                             _logger.warn(String.format("illegal character '%s' at line:%,d, pos:%d, char:%s", base,
                                     lineCount, i + 1));
                             continue;
                         }
 
                         encoder.append(iupac);
                     }
                 }
 
                long pos = encoder.size();
                long sequenceSize = pos - offset;
                 SequenceIndex index = new SequenceIndex(seqName, desc, sequenceSize, offset);
                 indexOut.leafObject("index", index);
                 _logger.info("\n" + SilkLens.toSilk("index", index));
 
                 // append a sentinel
                 encoder.append(IUPAC.None);
 
                 offset = encoder.size();
             }
             encoder.close();
             totalSize = encoder.size();
             _logger.info("total size: " + totalSize);
             indexOut.leaf("total size", totalSize);
 
             indexOut.close();
         }
 
         // Reverse the IUPAC sequence
         {
             IUPACSequence forwardSeq = new IUPACSequence(files.iupacForward(), files);
             {
                 _logger.info("Reverse the sequence");
                 _logger.info("Reverse IUPAC file: " + files.iupacReverse());
                 IUPACSequenceWriter encoder = new IUPACSequenceWriter(new BufferedOutputStream(new FileOutputStream(
                         files.iupacReverse())));
                 // reverse IN[0..n-2] (excludes the sentinel)
                 for (long i = forwardSeq.size() - 2; i >= 0; --i) {
                     encoder.append(forwardSeq.getIUPAC(i));
                 }
                 // append a sentinel.
                 encoder.append(IUPAC.None);
                 encoder.close();
             }
 
             {
                 // Create a suffix array and BWT string of the forward IUPAC sequence
                 _logger.info("Creating a suffix array of the forward sequence");
                 File suffixArrayFile = files.sparseSuffixArrayForward();
                 File bwtFile = files.bwtForward();
                 File wvFile = files.bwtWaveletForward();
                 buildSuffixArray(forwardSeq, files, suffixArrayFile, bwtFile, wvFile);
             }
         }
 
         {
             // Create a suffix array of the reverse IUPAC sequence
             IUPACSequence reverseSeq = new IUPACSequence(files.iupacReverse(), totalSize);
             _logger.info("Creating a suffix array of the reverse sequence");
             File suffixArrayFile = files.sparseSuffixArrayReverse();
             File bwtFile = files.bwtReverse();
             File wvFile = files.bwtWaveletReverse();
             buildSuffixArray(reverseSeq, files, suffixArrayFile, bwtFile, wvFile);
         }
 
     }
 
     public static void buildSuffixArray(IUPACSequence seq, BWTFiles f, File suffixArrayFile, File bwtFile, File wvFile)
             throws IOException, UTGBException {
 
         StopWatch timer = new StopWatch();
         LIntArray SA = new LIntArray(seq.textSize());
         {
             LSAIS.suffixsort(seq, SA, 16);
             _logger.info("Sparse SA file: " + suffixArrayFile);
             SparseSuffixArray sparseSA = SparseSuffixArray.buildFromSuffixArray(SA, 16);
             sparseSA.saveTo(suffixArrayFile);
         }
         _logger.info(String.format("Suffix array construction finshed. %.2f sec.", timer.getElapsedTime()));
 
         // Create a BWT string of the IUPAC sequence from the generated suffix array
         {
             _logger.info("Creating a BWT string: " + bwtFile);
             IUPACSequenceWriter writer = new IUPACSequenceWriter(
                     new BufferedOutputStream(new FileOutputStream(bwtFile)));
             bwt(seq, SA, writer);
             writer.close();
         }
 
         // Create a Wavelet array 
         {
             _logger.info("Loading BWT: " + bwtFile);
             IUPACSequence bwt = new IUPACSequence(bwtFile, f);
             _logger.info("Creating wavelet array " + wvFile);
             WaveletArray wv = new WaveletArray(bwt, 16);
             wv.saveTo(wvFile);
         }
 
     }
 
     public static void bwt(IUPACSequence seq, LIntArray SA, IUPACSequenceWriter out) throws IOException {
         for (long i = 0; i < SA.size(); ++i) {
             if (SA.get(i) == 0) {
                 out.append(IUPAC.None);
             }
             else {
                 out.append(seq.getIUPAC(SA.get(i) - 1));
             }
         }
     }
 
 }
