 package uk.ac.ebi.fgpt.sampletab.tools;
 
 import java.io.File;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabSaferParser;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.SampleTabValidator;
 import uk.ac.ebi.fgpt.sampletab.utils.XMLUtils;
 
 public class APITestCallable implements Callable<Void> {
 
     private final File inputFile;
     private final String hostname;
     private final Set<Exception> errors = new HashSet<Exception>();
     
     private Logger log = LoggerFactory.getLogger(getClass());
     
     
     public APITestCallable(File inputFile, String hostname) {
         this.inputFile = inputFile;
         this.hostname = hostname;
     }
     
     private void compareGroup(SampleData st, GroupNode group, Document groupDoc) {
         //TODO finish
         Element root = groupDoc.getRootElement();
         for (Element e: XMLUtils.getChildrenByName(root, "Property")) {
             String type = e.attributeValue("class");
             //this is only the first value in cases where there are multiple values
             String value = XMLUtils.getChildByName(e, "Value").getTextTrim();
             if (type.equals("Submission Title") && 
                     !st.msi.submissionTitle.equals(value)) {
                 errors.add(new RuntimeException("Submission Title does not match in "+group.getGroupAccession()));
             } else if (type.equals("Submission Description") && 
                     !st.msi.submissionDescription.equals(value)) {
                 errors.add(new RuntimeException("Submission Description does not match in "+group.getGroupAccession()));
             } else if (type.equals("Submission Identifier") && 
                     !st.msi.submissionIdentifier.equals(value)) {
                 errors.add(new RuntimeException("Submission Identifier does not match in "+group.getGroupAccession()));
             } else if (type.equals("Submission Reference Layer") && 
                     !st.msi.submissionReferenceLayer.equals(new Boolean(value))) {
                 errors.add(new RuntimeException("Submission Reference Layer does not match in "+group.getGroupAccession()));
             } else if (type.equals("Group Accession") && 
                     !group.getGroupAccession().equals(value)) {
                 errors.add(new RuntimeException("Group Accession does not match in "+group.getGroupAccession()));
             }
         }
         // publications
         for (Element e: XMLUtils.getChildrenByName(root, "Publication")) {
             Element eDOI = XMLUtils.getChildByName(e, "DOI");
             Element ePubMedID = XMLUtils.getChildByName(e, "PubMedID");
          
             boolean doiExists = false;
             boolean pubmedExists = false;
             for (Publication p : st.msi.publications) {
                if (p.getDOI() != null && p.getDOI().equals(eDOI.getTextTrim())) {
                     doiExists = true;
                 }
                if (p.getPubMedID() != null && p.getPubMedID().equals(ePubMedID.getTextTrim())) {
                     pubmedExists = true;
                 }
             }
             
            if (eDOI != null && !doiExists) {
                 errors.add(new RuntimeException("DOI "+eDOI.getTextTrim()+" does not exist in "+group.getGroupAccession()));
             }
            if (ePubMedID != null && !pubmedExists) {
                 errors.add(new RuntimeException("PubMedID "+ePubMedID.getTextTrim()+" does not exist in "+group.getGroupAccession()));
             }
         }
         //TODO databases
         //TODO person
         //TODO organisation
         //TODO term source
     }
     
     private void compareSample(SampleData st, SampleNode sample, Document sampleDoc) {
         //TODO finish
         Element root = sampleDoc.getRootElement();
         for (Element e: XMLUtils.getChildrenByName(root, "Property")) {
             String type = e.attributeValue("class");
             //this is only the first value in cases where there are multiple values
             String value = XMLUtils.getChildByName(e, "Value").getTextTrim();
             if (type.equals("Sample Name") && 
                     !sample.getNodeName().equals(value)) {
                 errors.add(new RuntimeException("Sample Name does not match on "+sample.getSampleAccession()+" ( "+sample.getNodeName()+" vs "+value+")"));
             } else if (type.equals("Sample Accession") && 
                     !sample.getSampleAccession().equals(value)) {
                 errors.add(new RuntimeException("Sample Accession does not match on "+sample.getSampleAccession()+" ( "+sample.getSampleAccession()+" vs "+value+")"));
             } else if (type.equals("Sample Description") && 
                     !sample.getSampleDescription().equals(value)) {
                 errors.add(new RuntimeException("Sample Description does not match on "+sample.getSampleAccession()+" ( "+sample.getSampleDescription()+" vs "+value+")"));
             } else {
                 //this is a general attribute
                 //find the attribute on the sample object that matches the XML entry
                 //NB doens't attempt to resolve multiple attributes with same type
                 for (SCDNodeAttribute a : sample.getAttributes()) {
                     boolean isComment = false;
                     synchronized (CommentAttribute.class) {
                         isComment = CommentAttribute.class.isInstance(a);
                     }
                     boolean isCharacteristic = false;
                     synchronized (CharacteristicAttribute.class) {
                         isCharacteristic = CharacteristicAttribute.class.isInstance(a);
                     }
                     if (isComment) {
                         CommentAttribute ca = (CommentAttribute) a;
                         //TODO check attribute "comment"
                         if (type.equals(ca.type)) {
                             compareComment(ca, e, sample.getSampleAccession());
                         }
                     } else if (isCharacteristic) {
                         CharacteristicAttribute ca = (CharacteristicAttribute) a;
                         //TODO check attribute "characteristic"
                         if (type.equals(ca.type)) {
                             compareCharacteristic(ca, e, sample.getSampleAccession());
                         }
                     } else if (type.equals(a.getAttributeType())) {
                         compareAttribute(a, e, sample.getSampleAccession());
                     }
                 }
             }
             
         }
     }
     
     private void compareComment(CommentAttribute a, Element e, String accession) {
         //TODO finish
         String elementType = e.attributeValue("class");
         //this is only the first value in cases where there are multiple values
         String elementValue = XMLUtils.getChildByName(e, "Value").getTextTrim();
         
         String attributeType = a.type;
         String attributeValue = a.getAttributeValue().trim();
         
         if (!elementValue.equals(attributeValue)) {
             errors.add(new RuntimeException(elementType+" does not match on "+accession+" ( "+elementValue+" vs "+attributeValue+")"));
         } 
         //TODO unit
         //TODO term source
     }
     
     private void compareCharacteristic(CharacteristicAttribute a, Element e, String accession) {
         //TODO finish
         String elementType = e.attributeValue("class");
         //this is only the first value in cases where there are multiple values
         String elementValue = XMLUtils.getChildByName(e, "Value").getTextTrim();
         
         String attributeType = a.type;
         String attributeValue = a.getAttributeValue().trim();
         //XML automatically replaces consecutive spaces with single spaces
         while (attributeValue.contains("  ")) {
             attributeValue = attributeValue.replace("  ", " ");
         }
         
         if (!elementValue.equals(attributeValue)) {
             errors.add(new RuntimeException(elementType+" does not match on "+accession+" ( "+elementValue+" vs "+attributeValue+")"));
         } 
         //TODO unit
         //TODO term source
     }
     
     private void compareAttribute(SCDNodeAttribute a, Element e, String accession) {
         //TODO finish
         String elementType = e.attributeValue("class");
         //this is only the first value in cases where there are multiple values
         String elementValue = XMLUtils.getChildByName(e, "Value").getTextTrim();
         
         String attributeType = a.getAttributeType();
         String attributeValue = a.getAttributeValue().trim();
         
         if (!elementValue.equals(attributeValue)) {
             errors.add(new RuntimeException(elementType+" does not match on "+accession+" ( "+elementValue+" vs "+attributeValue+")"));
         } 
         //TODO term source
     }
     
     
     @Override
     public Void call() throws Exception {
         
         SampleTabSaferParser parser = new SampleTabSaferParser(new SampleTabValidator());
         
         SampleData st;
         try {
             st = parser.parse(inputFile);
         } catch (ParseException e) {
             log.error("Problem processing "+inputFile, e);
             for (ErrorItem err : e.getErrorItems()){
                 log.error(err.toString());
             }
             throw e;
         }
         
         //for each group
         for (GroupNode group : st.scd.getNodes(GroupNode.class)) {
             if (group.getGroupAccession() == null) continue;
             //get the XML corresponding to this group 
             URL url = new URL("http://"+hostname+"/biosamples/xml/group/"+group.getGroupAccession());
             Document groupDoc = XMLUtils.getDocument(url);
             
             //compare that XML to the group
             compareGroup(st, group, groupDoc);
             
             //for each sample in the group
             for (Node node : group.getParentNodes()) {
                 SampleNode sample = (SampleNode) node;
                 //get the XML corresponding to this sample 
                 url = new URL("http://"+hostname+"/biosamples/xml/sample/"+sample.getSampleAccession());
                 Document sampleDoc = XMLUtils.getDocument(url);
                 
                 //compare that XML to the sample
                 compareSample(st, sample, sampleDoc);
             }
         }
         //check errors
         
         for (Exception e : errors) {
             log.error(e.getMessage());
         }
                 
         return null;
     }
 
 }
