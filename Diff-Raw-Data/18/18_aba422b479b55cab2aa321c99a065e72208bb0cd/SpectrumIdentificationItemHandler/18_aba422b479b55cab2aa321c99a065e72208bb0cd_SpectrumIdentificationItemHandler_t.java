 package au.org.intersect.samifier.parser.mzidentml;
 
import java.util.ArrayList;
import java.util.List;

 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class SpectrumIdentificationItemHandler extends DefaultHandler {
     private static final String SPECTRUM_ID_ITEM = "SpectrumIdentificationItem";
     private static final String PEPTIDE_EVIDENCE = "PeptideEvidence";
     private static final String CV_PARAM = "cvParam";
     private static final String MASCOT_SCORE = "mascot:score";
     private static final String ATTR_NAME = "name";
     private static final String ATTR_VALUE = "value";
     private static final String ATTR_ID = "id";
     private static final String ATTR_START = "start";
     private static final String ATTR_END = "end";
     private static final String ATTR_DB_SEQUENCE_REF = "DBSequence_Ref";
     private static final String PEPTIDE_EVIDENCE_REF = "PeptideEvidenceRef";
     private static final String ATTR_PEPTIDE_EVIDENCE_REF = "peptideEvidence_ref";
 
     private String peptideSequence;
     private String protein;
     private String confidenceScore;
     private String id;
     private String start;
     private String end;
    private List<String> references;
 
     private MzidReader reader;
 
     public SpectrumIdentificationItemHandler(MzidReader mzidReader,
             String peptideSequence) {
         super();
         this.reader = mzidReader;
         this.peptideSequence = peptideSequence;
        this.references = new ArrayList<String>();
     }
 
     public void startElement(String uri, String name, String qName,
             Attributes attrs) {
         if (PEPTIDE_EVIDENCE.equals(qName)) {
             protein = reader.getAccessionFromDbSequenceRef(attrs
                     .getValue(ATTR_DB_SEQUENCE_REF));
             id = attrs.getValue(ATTR_ID);
             start = attrs.getValue(ATTR_START);
             end = attrs.getValue(ATTR_END);
         } else if (CV_PARAM.equals(qName)) {
             if (attrs.getValue(ATTR_NAME).equalsIgnoreCase(MASCOT_SCORE)) {
                 confidenceScore = attrs.getValue(ATTR_VALUE);
             }
         } else if (PEPTIDE_EVIDENCE_REF.equals(qName)) {
            references.add(attrs.getValue(ATTR_PEPTIDE_EVIDENCE_REF));
         }
     }
 
     public void endElement(String uri, String name, String qName) {
         if (SPECTRUM_ID_ITEM.equals(qName)) {
             if (id != null) {
                 reader.build(id, peptideSequence, protein, start, end,
                         confidenceScore);
            } else if (references.size() > 0) {
                for (String reference : references) {
                    reader.addReference(reference, confidenceScore, peptideSequence);
                }
             }
             reader.removeHandler();
         }
     }
 }
