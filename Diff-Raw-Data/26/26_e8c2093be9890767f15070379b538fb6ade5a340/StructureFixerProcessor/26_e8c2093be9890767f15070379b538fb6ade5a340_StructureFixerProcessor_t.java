 /* $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  *
  */
 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
 import dk.statsbiblioteket.broadcasttranscoder.domscontent.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.util.List;
 
 public class StructureFixerProcessor extends ProcessorChainElement {
 
     private static Logger logger = LoggerFactory.getLogger(StructureFixerProcessor.class);
 
     public StructureFixerProcessor() {
     }
 
     public StructureFixerProcessor(ProcessorChainElement childElement) {
         super(childElement);
     }
 
     @Override
     protected void processThis(TranscodeRequest request, Context context) throws ProcessorException {
         handleMissingStart(request, context);
         handleMissingEnd(request, context);
         handleHoles(request, context);
         handleOverlaps(request, context);
     }
 
     private void handleOverlaps(TranscodeRequest request, Context context) throws ProcessorException {
         ProgramStructure.Overlaps overlaps = request.getLocalProgramStructure().getOverlaps();
         if (overlaps == null) {
             return;
         }
         List<Overlap> overlapList = overlaps.getOverlap();
         if (overlapList == null || overlapList.isEmpty()) {
             return;
         }
         for (Overlap overlap: overlapList) {
             handleSingleOverlap(request, overlap);
         }
     }
 
     private void handleSingleOverlap(TranscodeRequest request, Overlap overlap) throws ProcessorException {
        BroadcastMetadata bmd1 = request.getPidMap().get(overlap.getFile1UUID());
         BroadcastMetadata bmd2 = request.getPidMap().get(overlap.getFile2UUID());
         File file1 = request.getFileMap().get(bmd1);
         File file2 = request.getFileMap().get(bmd2);
         TranscodeRequest.FileClip clip1 = null;
         TranscodeRequest.FileClip clip2 = null;
         for (TranscodeRequest.FileClip clip: request.getClips()) {
             String clipFilename = (new File(clip.getFilepath())).getName();
             if (clipFilename.equals(file1.getName())) {
                 clip1 = clip;
             } else
             if (clipFilename.equals(file2.getName())) {
                 clip2 = clip;
             }
         }
         if (clip1 == null) {
             throw new ProcessorException("Could not find " + file1.getAbsolutePath() + " in clip");
         }
         if (clip2 == null) {
             throw new ProcessorException("Could not find " + file2.getAbsolutePath() + " in clip");
         }
         switch (overlap.getOverlapType()) {
             case(0):
                 final long startOffsetBytes = overlap.getOverlapLength() * request.getBitrate();
                 logger.info("Fixing overlap '" + overlap + "' by setting start offset to '" + startOffsetBytes + " bytes' in" +
                         " file '" + clip2.getFilepath() + "'");
                 clip2.setStartOffsetBytes(startOffsetBytes);
                 break;
             case(1):
                 logger.info("Fixing overlap '" + overlap + "' by removing '" + clip2.getFilepath() + "'");
                 request.getClips().remove(clip2);
                 break;
             case(2):
                 logger.info("Fixing overlap '" + overlap + "' by removing '" + clip2.getFilepath() + "'");
                 request.getClips().remove(clip2);
                 break;
             case(3):
                 logger.info("Fixing overlap '" + overlap + "' by removing '" + clip1.getFilepath() + "'");
                 request.getClips().remove(clip1);
                 break;
         }
     }
 
     private void handleMissingStart(TranscodeRequest request, Context context) throws ProcessorException {
         MissingStart missingStart = request.getLocalProgramStructure().getMissingStart();
         if (missingStart != null) {
                 logger.warn("Ignoring " + missingStart.getMissingSeconds() + " missing seconds at start for " +
                         context.getProgrampid());
         }
     }
 
     private void handleMissingEnd(TranscodeRequest request, Context context) throws ProcessorException {
         MissingEnd missingEnd = request.getLocalProgramStructure().getMissingEnd();
         if (missingEnd != null) {
              logger.warn("Ignoring  " + missingEnd.getMissingSeconds() + " missing seconds at end for " +
                         context.getProgrampid());
         }
     }
 
     private void handleHoles(TranscodeRequest request, Context context) throws ProcessorException {
         ProgramStructure.Holes holes = request.getLocalProgramStructure().getHoles();
         if (holes == null) {
             return;
         }
         List<Hole> holeList = holes.getHole();
         if (holeList == null || holeList.isEmpty()) {
             return;
         } else {
             for (Hole hole: holeList) {
                 logger.warn("Ignoring a hole of length " + hole.getHoleLength() + " seconds in the recording of " + context.getProgrampid());
             }
         }
     }
 
 
 }
