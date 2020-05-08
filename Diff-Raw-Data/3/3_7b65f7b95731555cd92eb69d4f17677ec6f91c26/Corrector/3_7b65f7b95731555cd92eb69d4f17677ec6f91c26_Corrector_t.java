 package uk.ac.ebi.fgpt.sampletab;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.dom4j.DocumentException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SCDNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractRelationshipAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonException;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonUtils;
 
 public class Corrector {
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
     
     private TermSource efo = new TermSource("EFO", "http://www.ebi.ac.uk/efo/", null);
     private TermSource ncbiTaxonomy = new TermSource("NCBI Taxonomy", "http://www.ncbi.nlm.nih.gov/taxonomy/", null);
 
     public String getInitialCapitals(String in){
         StringBuilder sb = new StringBuilder();
         boolean space = true;
         for (Character currentChar : in.toCharArray()) {
             switch (currentChar) {
                 case ' ':
                     space = true;
                     sb.append(currentChar);
                     break;
                 default:
                     if (space){
                         sb.append(Character.toUpperCase(currentChar));
                         space = false;
                     } else {
                         sb.append(currentChar);
                     }
                     break;
             }
         }
         return sb.toString();
     }
     
     public String stripHTML(String in){
        if (in == null){
            return in;
        }
         String out = in;
         out = out.replaceAll("\\s*\\<[bB][rR]? ?/?\\>\\s*"," ");
         
         //some UTF-8 hacks
         out = out.replaceAll("ÃƒÂ¼", "ü");
         
         return out;
     }
     
     
     private UnitAttribute correctUnit(UnitAttribute unit){
         String lcval = unit.getAttributeValue().toLowerCase();
         if (lcval.equals("alphanumeric")
                 || lcval.equals("na")
                 || lcval.equals("censored/uncensored")
                 || lcval.equals("m/f")
                 || lcval.equals("test/control")
                 || lcval.equals("yes/no")
                 || lcval.equals("y/n")
                 || lcval.equals("na")) {
             return null;
         } else if (lcval.equals("meter")
                 || lcval.equals("meters")) {
             unit.setAttributeValue("meter");
         } else if (lcval.equals("cellsperliter")
                 || lcval.equals("cells per liter")
                 || lcval.equals("cellperliter")
                 || lcval.equals("cell per liter")
                 || lcval.equals("cellsperlitre")
                 || lcval.equals("cells per litre")
                 || lcval.equals("cellperlitre")
                 || lcval.equals("cell per litre")) {
             unit.setAttributeValue("cell per liter");
         } else if (lcval.equals("cellspermilliliter")
                 || lcval.equals("cells per milliliter")
                 || lcval.equals("cellpermilliliter")
                 || lcval.equals("cell per milliliter")
                 ||lcval.equals("cellspermillilitre")
                 || lcval.equals("cells per millilitre")
                 || lcval.equals("cellpermillilitre")
                 || lcval.equals("cell per millilitre")) {
             unit.setAttributeValue("cell per millilitre");
         } else if (lcval.equals("micromolesperliter")
                 || lcval.equals("micromoleperliter")
                 || lcval.equals("micromole per liter")
                 || lcval.equals("micromoles per liter")
                 || lcval.equals("micromolesperlitre")
                 || lcval.equals("micromoleperlitre")
                 || lcval.equals("micromole per litre")
                 || lcval.equals("micromoles per litre")) {
             unit.setAttributeValue("micromole per liter");
         } else if (lcval.equals("microgramsperliter")
                 || lcval.equals("microgramperliter")
                 || lcval.equals("microgram per liter")
                 || lcval.equals("micrograms per liter")
                 || lcval.equals("microgramsperlitre")
                 || lcval.equals("microgramperlitre")
                 || lcval.equals("microgram per litre")
                 || lcval.equals("micrograms per litre")) {
             unit.setAttributeValue("microgram per liter");
         } else if (lcval.equals("micromolesperkilogram")
                 || lcval.equals("micromoles per kilogram")
                 || lcval.equals("micromoleperkilogram")
                 || lcval.equals("micromole per kilogram")) {
             unit.setAttributeValue("micromole per kilogram");
         } else if (lcval.equals("psu")
                 || lcval.equals("practicalsalinityunit")
                 || lcval.equals("practical salinity unit")
                 || lcval.equals("practical salinity units")
                 || lcval.equals("pss-78")
                 || lcval.equals("practicalsalinityscale1978 ")) {
             //technically, this is not a unit since its dimensionless..
             unit.setAttributeValue("practical salinity unit");
         } else if (lcval.equals("micromoles")
                 || lcval.equals("micromole")) {
             unit.setAttributeValue("micromole");
         } else if (lcval.equals("decimalhours")
                 || lcval.equals("decimalhour")
                 || lcval.equals("hours")
                 || lcval.equals("hour")) {
             unit.setAttributeValue("hour");
         } else if (lcval.equals("day")
                 || lcval.equals("days")) {
             unit.setAttributeValue("day");
         } else if (lcval.equals("week")
                 || lcval.equals("weeks")) {
             unit.setAttributeValue("week");
         } else if (lcval.equals("month")
                 || lcval.equals("months")) {
             unit.setAttributeValue("month");
         } else if (lcval.equals("year")
                 || lcval.equals("years")) {
             unit.setAttributeValue("year");
         } else if (lcval.equals("percentage")) {
             unit.setAttributeValue("percent");
         } else if (lcval.equals("decimal degrees")
                 || lcval.equals("decimal degree")
                 || lcval.equals("decimaldegrees")
                 || lcval.equals("decimaldegree")) {
             unit.setAttributeValue("decimal degree");
         } else if (lcval.equals("celcius")
                 || lcval.equals("degree celcius")
                 || lcval.equals("degrees celcius")
                 || lcval.equals("degreecelcius")
                 || lcval.equals("centigrade")
                 || lcval.equals("degree centigrade")
                 || lcval.equals("degrees centigrade")
                 || lcval.equals("degreecentigrade")
                 || lcval.equals("c")
                 || lcval.equals("??c")
                 || lcval.equals("degree c")
                 || lcval.equals("internationaltemperaturescale1990")
                 || lcval.equals("iternationaltemperaturescale1990")) {
             unit.setAttributeValue("Celcius");
         } 
         return unit;
     }
     
     private SCDNodeAttribute correctSex(SexAttribute attr, SampleData sampledata){
         if (attr.getAttributeValue().toLowerCase().equals("male")
                 || attr.getAttributeValue().toLowerCase().equals("m")
                 || attr.getAttributeValue().toLowerCase().equals("man")) {
             attr.setAttributeValue("male");
             attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0001266");
             attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
         } else if (attr.getAttributeValue().toLowerCase().equals("female")
                 || attr.getAttributeValue().toLowerCase().equals("f")
                 || attr.getAttributeValue().toLowerCase().equals("woman")) {
             attr.setAttributeValue("female");
             attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0001265");
             attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
         }
         return attr;
     }
     
     private SCDNodeAttribute correctOrganism(OrganismAttribute attr, SampleData sampledata){
 
         if (attr.getTermSourceREF() == null){
             if (attr.getAttributeValue().matches("[0-9]+")){
                 Integer taxid = new Integer(attr.getAttributeValue());
                 try {
                     String taxonName = TaxonUtils.getTaxonOfID(taxid);
                     attr.setAttributeValue(taxonName);
                     String ncbiTaxonomyName = sampledata.msi.getOrAddTermSource(ncbiTaxonomy);
                     attr.setTermSourceREF(ncbiTaxonomyName);
                     attr.setTermSourceIDInteger(taxid);
                 } catch (TaxonException e) {
                     log.warn("Unable to find taxon #"+taxid);
                     //e.printStackTrace();
                 }
             } else {
                 Integer taxid = null;
                 String speciesName = attr.getAttributeValue();
                                 
                 try {
                     taxid = TaxonUtils.findTaxon(speciesName);
                 } catch (TaxonException e) {
                     log.warn("Unable to find taxid for "+speciesName);
                     //e.printStackTrace();
                 }
                 if (taxid != null){
                     attr.setTermSourceIDInteger(taxid);
                     String ncbiTaxonomyName = sampledata.msi.getOrAddTermSource(ncbiTaxonomy);
                     attr.setTermSourceREF(ncbiTaxonomyName);
                 }
             }
         } else if (attr.getTermSourceID().startsWith("http://purl.org/obo/owl/NCBITaxon#NCBITaxon_")){
             Integer taxid = new Integer(attr.getTermSourceID().substring("http://purl.org/obo/owl/NCBITaxon#NCBITaxon_".length(), attr.getTermSourceID().length()));
             attr.setTermSourceIDInteger(taxid);
             String ncbiTaxonomyName = sampledata.msi.getOrAddTermSource(ncbiTaxonomy);
             attr.setTermSourceREF(ncbiTaxonomyName);
         }
         
         return attr;
     }
     
     private SCDNodeAttribute correctCharacteristic(CharacteristicAttribute attr, SampleData sampledata){        
         //bulk replace underscore with space in types
         attr.type = attr.type.replace("_", " ");
 
         //remove technical attributes
         if (attr.type.toLowerCase().equals("channel")){
             return null;
         }
                             
         // make organism a separate attribute
         if (attr.type.toLowerCase().equals("organism") 
                 || attr.type.toLowerCase().equals("organi") //from ArrayExpress
                 || attr.type.toLowerCase().equals("arrayexpress-species") //from ENA SRA
                 || attr.type.toLowerCase().equals("cell organism") //from ENA SRA
                 ) {
             return correctOrganism(new OrganismAttribute(attr.getAttributeValue()), sampledata);
         }
         
         // make sex a separate attribute
         if (attr.type.toLowerCase().equals("sex") 
                 || attr.type.toLowerCase().equals("gender")
                 || attr.type.toLowerCase().equals("arrayexpress-sex") //from ENA SRA
                 || attr.type.toLowerCase().equals("cell sex") //from ENA SRA
                 ) {
             //this will handle the real corrections
             return correctSex(new SexAttribute(attr.getAttributeValue()), sampledata);
         }
         
         
         //TODO make material a separate attribute
         
         // fix typos
         if (attr.type.toLowerCase().equals("age")) {
             attr.type = "age";
             //TODO some simple regex expansions, e.g. 5W to 5 weeks
         } else if (attr.type.toLowerCase().equals("developmental stage")
                 || attr.type.toLowerCase().equals("developmentalstage")) {
             attr.type = "developmental stage";
         } else if (attr.type.toLowerCase().equals("disease state")
                 || attr.type.toLowerCase().equals("diseasestate")) {
             attr.type = "disease state";
         } else if (attr.type.toLowerCase().equals("ecotype")) {
             attr.type = "ecotype";
             if (attr.getAttributeValue().toLowerCase().equals("col-0")
                     || attr.getAttributeValue().toLowerCase().equals("columbia-0")
                     || attr.getAttributeValue().toLowerCase().equals("columbia (col0) ")){
                     attr.setAttributeValue("Columbia-0");
             } else if (attr.getAttributeValue().toLowerCase().equals("columbia")
                     || attr.getAttributeValue().toLowerCase().equals("col")) {
                 attr.setAttributeValue("Columbia");
             }
         } else if (attr.type.toLowerCase().equals("ethnicity")) {
             attr.type = "ethnicity";
             //ethnicity, population, race are a mess, leave alone
         } else if (attr.type.toLowerCase().equals("genotype")
                 ||attr.type.toLowerCase().equals("individualgeneticcharacteristics")
                 ||attr.type.toLowerCase().equals("genotype/variation") ) {
             attr.type = "genotype";
             if (attr.getAttributeValue().toLowerCase().equals("wildtype")
                     || attr.getAttributeValue().toLowerCase().equals("wild type")
                     || attr.getAttributeValue().toLowerCase().equals("wild-type")
                     || attr.getAttributeValue().toLowerCase().equals("wild_type")
                     || attr.getAttributeValue().toLowerCase().equals("wt")) {
                 attr.setAttributeValue("wild type");
             }
         } else if (attr.type.toLowerCase().equals("histology")) {
             attr.type = getInitialCapitals(attr.type);
         } else if (attr.type.toLowerCase().equals("individual")) {
             //TODO investigate
             attr.type = getInitialCapitals(attr.type);
         } else if (attr.type.toLowerCase().equals("organism part") 
                 ||attr.type.toLowerCase().equals("organismpart")
                 ||attr.type.toLowerCase().equals("tissue")) {
             attr.type = "organism part";
             if (attr.getAttributeValue().toLowerCase().equals("blood")){
                 attr.setAttributeValue("blood");
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000296");
             } else if (attr.getAttributeValue().toLowerCase().equals("skin")){
                 attr.setAttributeValue("skin");
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000962");
             } else if (attr.getAttributeValue().toLowerCase().equals("bone marrow")){
                 attr.setAttributeValue("bone marrow");
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000868");
             } else if (attr.getAttributeValue().toLowerCase().equals("liver")) {
                 attr.setAttributeValue(attr.getAttributeValue().toLowerCase());
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000887");
             } else if (attr.getAttributeValue().toLowerCase().equals("breast")
                     || attr.getAttributeValue().toLowerCase().equals("mammary gland")) {
                 attr.setAttributeValue("mammary gland");
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000854");
             } 
         } else if (attr.type.toLowerCase().equals("phenotype")) {
             attr.type = getInitialCapitals(attr.type);
         } else if (attr.type.toLowerCase().equals("stage")) {
             attr.type = getInitialCapitals(attr.type);
         } else if (attr.type.toLowerCase().equals("strain")
                 || attr.type.toLowerCase().equals("strainorline")
                 || attr.type.toLowerCase().equals("cell line")
                 || attr.type.toLowerCase().equals("cellline")
                 || attr.type.toLowerCase().equals("arrayexpress-strainorline")
                 || attr.type.toLowerCase().equals("coriell id")
                 || attr.type.toLowerCase().equals("coriell catalog id")
                 || attr.type.toLowerCase().equals("coriell cell line")
                 || attr.type.toLowerCase().equals("cell line (coriell id)")
                 || attr.type.toLowerCase().equals("coriell cell culture id")
                 || attr.type.toLowerCase().equals("coriell cell line repository identifier")
                 || attr.type.toLowerCase().equals("coriell dna id")
                 || attr.type.toLowerCase().equals("fibroblast cell strain") //TODO add cell type too
                 || attr.type.toLowerCase().equals("hapmap sample id")
                 || attr.type.toLowerCase().equals("breed")
                 ) {
             //Leave cultivar and ecotype alone
             attr.type = "strain";
         } else if (attr.type.toLowerCase().equals("time")
                 || attr.type.toLowerCase().equals("time point")) {
             attr.type = "time point";
             //TODO fix "Time Unit" being a separate characteristic
             //TODO fix embedding of units in the string (e.g. 24h) 
         } else if (attr.type.toLowerCase().equals("cell type")
                 || attr.type.toLowerCase().equals("celltype")) {
             attr.type = "cell type";
             //TODO clarify some of these as tissue or cell type
             if (attr.getAttributeValue().toLowerCase().equals("liver")) {
                 attr.setAttributeValue(attr.getAttributeValue().toLowerCase());
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000887");
             } else if (attr.getAttributeValue().toLowerCase().equals("blood")) {
                 attr.setAttributeValue(attr.getAttributeValue().toLowerCase());
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000296");
             } else if (attr.getAttributeValue().toLowerCase().equals("breast")
                     || attr.getAttributeValue().toLowerCase().equals("mammary gland")) {
                 attr.setAttributeValue("mammary gland");
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000854");
             } 
         }
         
         //TODO HTML URL encoding e.g. %3E %apos; %quot;
         
         //TODO demote some characteristics to comments
         
         
         if (attr.unit != null){
             attr.unit = correctUnit(attr.unit);
         }
         
         return attr;
     }
     
     
     public void correct(SampleData sampledata) {
         sampledata.msi.submissionTitle = stripHTML(sampledata.msi.submissionTitle);
         sampledata.msi.submissionDescription = stripHTML(sampledata.msi.submissionDescription);
         
         
         for (SampleNode s : sampledata.scd.getNodes(SampleNode.class)) {
             //convert to array so we can delete and add attributes if needed
             for (SCDNodeAttribute a : new ArrayList<SCDNodeAttribute>(s.getAttributes())) {
                 boolean isAbstractSCDAttribute = false;
                 synchronized(AbstractNodeAttribute.class){
                     isAbstractSCDAttribute = AbstractNodeAttribute.class.isInstance(a);
                 }
 
                 // tidy things that apply to all attributes
                 if (isAbstractSCDAttribute) {
                     AbstractNodeAttribute cha = (AbstractNodeAttribute) a;
                     // remove not applicables
                     if (cha.getAttributeValue().toLowerCase().equals("n/a")
                             || cha.getAttributeValue().toLowerCase().equals("na")
                             || cha.getAttributeValue().toLowerCase().equals("none")
                             || cha.getAttributeValue().toLowerCase().equals("unknown")
                             || cha.getAttributeValue().toLowerCase().equals("--")
                             || cha.getAttributeValue().toLowerCase().equals("not applicable")
                             || cha.getAttributeValue().toLowerCase().equals("null")) {
                         //leave unknown-sex as is. implies it has been looked at and is non-determinate
                         s.removeAttribute(cha);
                         continue;
                     }
                 }
 
                 SCDNodeAttribute updated = a;
                 
                 boolean isCharacteristic = false;
                 synchronized(CharacteristicAttribute.class){
                     isCharacteristic = CharacteristicAttribute.class.isInstance(a);
                 }
                 boolean isSex = false;
                 synchronized(SexAttribute.class){
                     isSex = SexAttribute.class.isInstance(a);
                 }
                 boolean isOrganism = false;
                 synchronized(OrganismAttribute.class){
                     isOrganism = OrganismAttribute.class.isInstance(a);
                 }
                 // tidy all characteristics
                 if (isCharacteristic) {
                     updated = correctCharacteristic((CharacteristicAttribute) a, sampledata);
                 } else if (isSex) {
                     updated = correctSex((SexAttribute) a, sampledata);
                 } else if (isOrganism) {
                     updated = correctOrganism((OrganismAttribute) a, sampledata);
                 }
                 
                 //TODO comments
                 //TODO promote some comments to characteristics
 
                 boolean isRelationship = false;
                 synchronized(AbstractRelationshipAttribute.class){
                     isRelationship = AbstractRelationshipAttribute.class.isInstance(a);
                 }
                 
                 if (isRelationship){
                     //Relationships may refer to other samples in the same submission by name
                     //It is better to refer by BioSD accession.
                     AbstractRelationshipAttribute rela = (AbstractRelationshipAttribute) a;
                     String targetName = rela.getAttributeValue();
                     SampleNode target = sampledata.scd.getNode(targetName, SampleNode.class);
                     if (target != null && target.getSampleAccession() != null){
                         rela.setAttributeValue(target.getSampleAccession());
                     }
                 }
 
                 //comparison by identity
                 //replace in same position
                 if (updated != a){
                     int i = s.getAttributes().indexOf(a);
                     s.removeAttribute(a);
                     if (updated != null){
                         s.addAttribute(updated, i);
                     }
                 }
             }
         }
         
         CorrectorTermSource cts = new CorrectorTermSource();
         cts.correct(sampledata);
     }
 }
