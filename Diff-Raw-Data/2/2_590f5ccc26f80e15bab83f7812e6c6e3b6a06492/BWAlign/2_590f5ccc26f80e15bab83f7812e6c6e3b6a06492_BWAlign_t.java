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
 // BurrowsWheelerAlignment.java
 // Since: 2011/02/10
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 
 import org.utgenome.UTGBException;
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.utgenome.util.StandardOutputStream;
 import org.utgenome.weaver.align.SequenceBoundary.PosOnGenome;
 import org.utgenome.weaver.align.record.AlignmentRecord;
 import org.utgenome.weaver.align.record.RawRead;
 import org.utgenome.weaver.align.record.ReadSequenceReader;
 import org.utgenome.weaver.align.record.ReadSequenceReaderFactory;
 import org.utgenome.weaver.align.strategy.BWAStrategy;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.ObjectHandler;
 import org.xerial.util.ObjectHandlerBase;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 
 /**
  * Burrows-Wheeler aligner for IUPAC sequences
  * 
  * @author leo
  * 
  */
 public class BWAlign extends GenomeWeaverCommand
 {
     private static Logger _logger = Logger.getLogger(BWAlign.class);
 
     @Override
     public String name() {
         return "align";
     }
 
     @Override
     public String getOneLineDescription() {
         return "performs alignment";
     }
 
     @Override
     public Object getOptionHolder() {
         return this;
     }
 
     @Argument(index = 0)
     private String fastaFilePrefix;
 
     @Argument(index = 1)
     private String readFile;
 
     @Option(symbol = "q", description = "query sequence")
     private String query;
 
     //    @Option(longName = "sam", description = "output in SAM format")
     //    public boolean outputSAM           = false;
 
     @Option(symbol = "N", description = "Num mismatches allowed. default=0")
     public int     numMismachesAllowed = 0;
 
     public static class SAMOutput implements ObjectHandler<AlignmentRecord>
     {
 
         FMIndexOnGenome fmIndex;
         PrintWriter     out;
 
         public SAMOutput(FMIndexOnGenome fmIndex, OutputStream out) {
             this.fmIndex = fmIndex;
             this.out = new PrintWriter(new OutputStreamWriter(out));
         }
 
         @Override
         public void init() throws Exception {
             fmIndex.outputSAMHeader(out);
         }
 
         @Override
         public void handle(AlignmentRecord r) throws Exception {
             out.println(r.toSAMLine());
         }
 
         @Override
         public void finish() throws Exception {
             out.close();
         }
     }
 
     @Override
     public void execute(String[] args) throws Exception {
 
         if (query == null && readFile == null)
             throw new UTGBException("no query is given");
 
         ReadSequenceReader reader = null;
         if (query != null) {
             _logger.info("query sequence: " + query);
             reader = ReadSequenceReaderFactory.singleQueryReader(query);
         }
        else if (readFile != null) {
             reader = ReadSequenceReaderFactory.createReader(readFile);
         }
 
         FMIndexOnGenome fmIndex = new FMIndexOnGenome(fastaFilePrefix);
         SAMOutput samOutput = new SAMOutput(fmIndex, new StandardOutputStream());
         query(fmIndex, reader, samOutput);
     }
 
     private static class GenomeCoordinateConverter extends ObjectHandlerBase<AlignmentSA>
     {
 
         private FMIndexOnGenome                fmIndex;
         private ObjectHandler<AlignmentRecord> handler;
 
         public GenomeCoordinateConverter(FMIndexOnGenome fmIndex, ObjectHandler<AlignmentRecord> handler) {
             this.fmIndex = fmIndex;
             this.handler = handler;
         }
 
         @Override
         public void handle(AlignmentSA aln) throws Exception {
             fmIndex.toGenomeCoordinate(aln.common.queryName, aln, handler);
         }
 
     }
 
     public static void query(final FMIndexOnGenome fmIndex, ReadSequenceReader readReader,
             final ObjectHandler<AlignmentRecord> handler) throws Exception {
 
         handler.init();
         final BWAStrategy aligner = new BWAStrategy(fmIndex);
         readReader.parse(new ObjectHandlerBase<RawRead>() {
             @Override
             public void handle(final RawRead input) throws Exception {
                 aligner.align(input, new GenomeCoordinateConverter(fmIndex, handler));
             };
         });
         handler.finish();
 
     }
 
     public static void querySingle(String fastaFilePrefix, final String query,
             final ObjectHandler<AlignmentRecord> resultHandler) throws Exception {
 
         final FMIndexOnGenome fmIndex = new FMIndexOnGenome(fastaFilePrefix);
         query(fmIndex, ReadSequenceReaderFactory.singleQueryReader(query), resultHandler);
     }
 
     public static class FMIndexOnGenome
     {
         public final FMIndex            fmIndexF;
         public final FMIndex            fmIndexR;
         private final SparseSuffixArray saF;
         private final SparseSuffixArray saR;
         private final WaveletArray      wvF;
         private final WaveletArray      wvR;
         private final SequenceBoundary  index;
 
         private final long              N;
         private final int               K;
 
         public FMIndexOnGenome(String fastaFilePrefix) throws UTGBException, IOException {
             BWTFiles forwardDB = new BWTFiles(fastaFilePrefix, Strand.FORWARD);
             BWTFiles reverseDB = new BWTFiles(fastaFilePrefix, Strand.REVERSE);
 
             // Load the boundary information of the concatenated chr sequences 
             index = SequenceBoundary.loadSilk(forwardDB.pacIndex());
             N = index.totalSize;
             K = IUPAC.values().length;
 
             // Load sparse suffix arrays
             _logger.info("Loading sparse suffix arrays");
             saF = SparseSuffixArray.loadFrom(forwardDB.sparseSuffixArray());
             saR = SparseSuffixArray.loadFrom(reverseDB.sparseSuffixArray());
 
             // Load Wavelet arrays
             _logger.info("Loading a Wavelet array of the forward BWT");
             wvF = WaveletArray.loadFrom(forwardDB.bwtWavelet());
             _logger.info("Loading a Wavelet array of the reverse BWT");
             wvR = WaveletArray.loadFrom(reverseDB.bwtWavelet());
 
             // Prepare FM-indexes
             fmIndexF = new FMIndex(wvF);
             fmIndexR = new FMIndex(wvR);
         }
 
         public void outputSAMHeader(PrintWriter out) {
             out.print(index.toSAMHeader());
         }
 
         public void toGenomeCoordinate(String querySeq, AlignmentSA result, ObjectHandler<AlignmentRecord> reporter)
                 throws Exception {
             //            if (_logger.isTraceEnabled())
             _logger.info(SilkLens.toSilk("alignment", result));
 
             final long querySize = result.common.query.textSize();
 
             for (long i = result.suffixInterval.lowerBound; i <= result.suffixInterval.upperBound; ++i) {
                 long pos = -1;
                 switch (result.strand) {
                 case FORWARD:
                     pos = saR.get(i, fmIndexR);
                     pos = (fmIndexR.textSize() - 1 - pos) - querySize;
                     break;
                 case REVERSE:
                     pos = saF.get(i, fmIndexF);
                     break;
                 }
                 pos += 1;
                 if (pos != -1) {
                     PosOnGenome p = index.translate(pos, result.strand);
                     AlignmentRecord rec = new AlignmentRecord();
                     rec.chr = p.chr;
                     rec.start = p.pos;
                     rec.strand = result.strand;
                     rec.score = result.alignmentScore;
                     rec.numMismatches = result.numMismatches;
                     rec.querySeq = result.strand == Strand.FORWARD ? querySeq : result.common.query.reverse()
                             .toString();
                     rec.readName = querySeq;
                     rec.end = p.pos + result.wordIndex;
                     rec.setCIGAR(result.cigar().toCIGARString());
                     reporter.handle(rec);
                 }
             }
 
         }
     }
 
 }
