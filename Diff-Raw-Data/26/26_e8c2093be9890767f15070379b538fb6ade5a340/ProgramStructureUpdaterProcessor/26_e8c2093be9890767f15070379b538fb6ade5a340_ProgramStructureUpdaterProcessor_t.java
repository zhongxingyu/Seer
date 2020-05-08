 package dk.statsbiblioteket.broadcasttranscoder.processors;
 
 import dk.statsbiblioteket.broadcasttranscoder.cli.Context;
 import dk.statsbiblioteket.broadcasttranscoder.domscontent.*;
 import dk.statsbiblioteket.broadcasttranscoder.util.JaxbWrapper;
 import dk.statsbiblioteket.doms.central.CentralWebservice;
 import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import java.io.ByteArrayOutputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This processor has the following behaviour. If the locally calculated ProgramStructure
  * is distinct from that in DOMS, or if there is only a dummy ProgramStructure in DOMS, then
  * the local value is written back to DOMS.. Otherwise no action is taken.
  */
 public class ProgramStructureUpdaterProcessor extends ProcessorChainElement {
 
     private static final Logger logger = LoggerFactory.getLogger(ProgramStructureUpdaterProcessor.class);
 
     public ProgramStructureUpdaterProcessor() {
     }
 
     public ProgramStructureUpdaterProcessor(ProcessorChainElement childElement) {
         super(childElement);
     }
 
     @Override
     protected void processThis(TranscodeRequest request, Context context) throws ProcessorException {
         ProgramStructure domsProgramStructure = request.getDomsProgramStructure();
         ProgramStructure localProgramStructure = request.getLocalProgramStructure();
         if (isDummy(domsProgramStructure) ||
                 !areSemanticallyEqual(domsProgramStructure, localProgramStructure)) {
             logger.debug("Writing new program structure for " + context.getProgrampid() + " to DOMS");
             writeStructureToDoms(request, context);
         } else {
             return;
         }
     }
 
     protected void writeStructureToDoms(TranscodeRequest request, Context context) throws ProcessorException {
         CentralWebservice doms = context.getDomsApi();
         ProgramStructure structure = request.getLocalProgramStructure();
         ObjectFactory objectFactory = new ObjectFactory();
         StringWriter writer = new StringWriter();
         try {
             JAXBContext.newInstance(ProgramStructure.class).createMarshaller()
                     .marshal(objectFactory.createProgramStructure(structure), writer);
         } catch (JAXBException e) {
             throw new ProcessorException("Failed to xml-ilise program structure for "+context.getProgrampid(),e);
         }
         String xmlString = writer.getBuffer().toString();
         logger.debug("New program structure for " + context.getProgrampid() + " :\n" + xmlString);
         List<String> pids = new ArrayList<String>();
         pids.add(context.getProgrampid());
         try {
             doms.markInProgressObject(pids, "Enriching shard content with result of shard analysis");
             doms.modifyDatastream(context.getProgrampid(), "PROGRAM_STRUCTURE", xmlString, "Updated PROGRAM_STRUCTURE with " +
                     "result from analysis by Broadcast Transcoder Application");
         } catch (Exception e) {
             throw new ProcessorException("Failed to write to DOMS for "+context.getProgrampid(),e);
         } finally {
             try {
                 doms.markPublishedObject(pids, "Updated PROGRAM_STRUCTURE with " +
                         "result from analysis by Broadcast Transcoded Application");
             } catch (Exception e) {
                 logger.error("Problem republishing object " + context.getProgrampid(), e);
                 throw new ProcessorException("Problem republishing object " + context.getProgrampid(), e);
             }
         }
     }
 
     /**
      * A dummy structure is ... to be defined. But should have a flag indicating it has never
      * been filled.
      * @param structure
      * @return
      */
      boolean isDummy(ProgramStructure structure) {
         return (structure == null);
     }
 
     /**
      * This is just an "equals" method with particular logic to check that e.g. an empty array of holes
      * is the same as a null value.
      * @param s1
      * @param s2
      * @return
      */
      boolean areSemanticallyEqual(ProgramStructure s1, ProgramStructure s2) {
          if (!compareMissingStart(s1, s2)) {
              return false;
          } else if (!compareMissingEnd(s1, s2)) {
              return false;
          } else if (!compareHoles(s1, s2)) {
              return false;
          } else if (!compareOverlaps(s1, s2)) {
              return false;
          }
          return true;
     }
 
    private boolean compareMissingStart(ProgramStructure s1, ProgramStructure s2) {
        MissingStart ms1 = s1.getMissingStart();
        MissingStart ms2 = s2.getMissingStart();
        if (ms1 == null && ms2 == null) {
            return true;
        } else if (ms1 == null || ms2 == null) {
            return false;
        } else {
            if (ms1.getMissingSeconds() == ms2.getMissingSeconds()) {
                return true;
            } else {
                return false;
            }
        }
    }
 
    private boolean compareMissingEnd(ProgramStructure s1, ProgramStructure s2) {
        MissingEnd me1 = s1.getMissingEnd();
        MissingEnd me2 = s2.getMissingEnd();
        if (me1 == null && me2 == null) {
            return true;
        } else if (me1 == null || me2 == null) {
            return false;
        } else {
            if (me1.getMissingSeconds() == me2.getMissingSeconds()) {
                return true;
            } else {
                return false;
            }
        }
    }
 
     private boolean compareHoles(ProgramStructure s1, ProgramStructure s2) {
         boolean noHoles1 = (s1.getHoles() == null) || (s1.getHoles().getHole() == null) || (s1.getHoles().getHole().isEmpty());
         boolean noHoles2 = (s2.getHoles() == null) || (s2.getHoles().getHole() == null) || (s2.getHoles().getHole().isEmpty());
         if (noHoles1 && noHoles2) {
             return true;
         } else if (noHoles1 || noHoles2) {
             return false;
         } else {
             List<Hole> holes1 = s1.getHoles().getHole();
             List<Hole> holes2 = s2.getHoles().getHole();
             if (holes1.size() != holes2.size()) {
                 return false;
             } else {
                 for (int j = 0; j < holes1.size(); j++) {
                     Hole hole1 = holes1.get(j);
                     Hole hole2 = holes2.get(j);
                     if ( !(hole1.getFile1UUID().equals(hole2.getFile1UUID())
                             && hole1.getFile2UUID().equals(hole2.getFile2UUID())
                             && hole1.getHoleLength() == hole2.getHoleLength() )
                             ) return false;
                 }
             }
         }
         return true;
     }
 
     private boolean compareOverlaps(ProgramStructure s1, ProgramStructure s2) {
         boolean noOverlaps1 = (s1.getOverlaps() == null) || (s1.getOverlaps().getOverlap() == null) || (s1.getOverlaps().getOverlap().isEmpty());
         boolean noOverlaps2 = (s2.getOverlaps() == null) || (s2.getOverlaps().getOverlap() == null) || (s2.getOverlaps().getOverlap().isEmpty());
         if (noOverlaps1 && noOverlaps2) {
             return true;
         } else if (noOverlaps1 || noOverlaps2) {
             return false;
         } else {
             List<Overlap> overlaps1 = s1.getOverlaps().getOverlap();
             List<Overlap> overlaps2 = s2.getOverlaps().getOverlap();
             if (overlaps1.size() != overlaps2.size()) {
                 return false;
             } else {
                 for (int j = 0 ; j < overlaps1.size(); j++) {
                     Overlap overlap1 = overlaps1.get(j);
                     Overlap overlap2 = overlaps2.get(j);
                     if (!(
                            overlap1.getFile1UUID().equals(overlap2.getFile1UUID())
                                     &&    overlap1.getFile2UUID().equals(overlap2.getFile2UUID())
                                     &&    overlap1.getOverlapLength() == overlap2.getOverlapLength()
                                     &&    overlap1.getOverlapType() == overlap2.getOverlapType()
                     )
                             ) {
                         return false;
                     }
                 }
                 return true;
             }
         }
 
     }
 
 }
