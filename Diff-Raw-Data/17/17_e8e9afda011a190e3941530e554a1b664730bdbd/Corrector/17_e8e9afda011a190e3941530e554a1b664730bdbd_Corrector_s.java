 package uk.ac.ebi.fgpt.sampletab;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractRelationshipAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
 import uk.ac.ebi.fgpt.sampletab.utils.EuroPMCUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonException;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonUtils;
import uk.ac.ebi.fgpt.sampletab.utils.europmc.ws.QueryException_Exception;
 
 public class Corrector {
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
     
     private TermSource efo = new TermSource("EFO", "http://www.ebi.ac.uk/efo/", null);
     private TermSource ncbiTaxonomy = new TermSource("NCBI Taxonomy", "http://www.ncbi.nlm.nih.gov/taxonomy/", null);
 
     public String getInitialCapitals(String in) {
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
     
     public String stripHTML(String in) {
         if (in == null){
             return in;
         }
         String out = in;
         //extra whitespace
         out = out.trim();
         
         //<br>
         //<b>
         out = out.replaceAll("\\s*\\</?[bB][rR]? ?/?\\>\\s*"," ");
         //<p>
         out = out.replaceAll("\\s*\\</?[pP] ?/?\\>\\s*"," ");
         //<i>
         out = out.replaceAll("\\s*\\</?[iI] ?/?\\>\\s*"," ");
         
         //some UTF-8 hacks
         out = out.replace("ÃƒÂ¼", "ü");
         //spaces handled by SampleTabWriter
         
         return out;
     }
     
     
     private UnitAttribute correctUnit(UnitAttribute unit) {
         String lcval = unit.getAttributeValue().toLowerCase();
         if (lcval.equals("alphanumeric")
                 || lcval.equals("na")
                 || lcval.equals("censored/uncensored")
                 || lcval.equals("m/f")
                 || lcval.equals("test/control")
                 || lcval.equals("yes/no")
                 || lcval.equals("y/n")
                || lcval.equals("na")
                 || lcval.equals("missing")) {
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
     
     private SCDNodeAttribute correctOrganism(OrganismAttribute attr, SampleData sampledata) {
         if (attr.getAttributeValue().startsWith("Organism:")) {
             attr.setAttributeValue(attr.getAttributeValue().substring(9));
         }
         
         if (attr.getAttributeValue().equals("Clostridium difficle")) {
             attr.setAttributeValue("Clostridium difficile");
         } else if (attr.getAttributeValue().equals("Staphylococcus aureu")) {
             attr.setAttributeValue("Staphylococcus aureus");
         } else if (attr.getAttributeValue().equals("Homo sapien")) {
             attr.setAttributeValue("Homo sapiens");
         } else if (attr.getAttributeValue().equals("Lepeophteirus salmonis")) {
             attr.setAttributeValue("Lepeophtheirus salmonis");
         } else if (attr.getAttributeValue().equals("Salmon salar")) {
             attr.setAttributeValue("Salmo salar");
         } else if (attr.getAttributeValue().equals("Gadus morrhua")) {
             attr.setAttributeValue("Gadus morhua");
         } else if (attr.getAttributeValue().equals("Mus muscules")) {
             attr.setAttributeValue("Mus musculus");
         } else if (attr.getAttributeValue().equals("Mus muculus")) {
             attr.setAttributeValue("Mus musculus");
         } else if (attr.getAttributeValue().equals("Candida albicancs")) {
             attr.setAttributeValue("Candida albicans");
         }
         
         if (attr.getTermSourceREF() == null){
             if (attr.getAttributeValue().matches("[0-9]+")){
                 Integer taxid = new Integer(attr.getAttributeValue());
                 try {
                     String taxonName = TaxonUtils.getSpeciesOfID(taxid);
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
         } else if (attr.getTermSourceID().startsWith("http://purl.org/obo/owl/NCBITaxon#NCBITaxon_")) {
             Integer taxid = new Integer(attr.getTermSourceID().substring("http://purl.org/obo/owl/NCBITaxon#NCBITaxon_".length(), attr.getTermSourceID().length()));
             attr.setTermSourceIDInteger(taxid);
             String ncbiTaxonomyName = sampledata.msi.getOrAddTermSource(ncbiTaxonomy);
             attr.setTermSourceREF(ncbiTaxonomyName);
         }
         
         return attr;
     }
     
     private SCDNodeAttribute correctCharacteristic(CharacteristicAttribute attr, SampleData sampledata) {        
         //bulk replace underscore with space in types
         attr.type = attr.type.replace("_", " ");
 
         //remove technical attributes
         if (attr.type.toLowerCase().equals("channel")) {
             return null;
         }
                             
         // make organism a separate attribute
         if (attr.type.toLowerCase().equals("organism") 
                 || attr.type.toLowerCase().equals("organi") //from ArrayExpress
                 || attr.type.toLowerCase().equals("arrayexpress-species") //from ENA SRA
                 || attr.type.toLowerCase().equals("cell organism") //from ENA SRA
                 || attr.type.toLowerCase().equals("taxon_id")
                 ) {
             return correctOrganism(new OrganismAttribute(attr.getAttributeValue()), sampledata);
         }
         
         // make sex a separate attribute
         if (attr.type.toLowerCase().equals("sex") 
                 || attr.type.toLowerCase().equals("gender")
                 || attr.type.toLowerCase().equals("arrayexpress-sex") //from ENA SRA
                 || attr.type.toLowerCase().equals("cell sex") //from ENA SRA
                 || attr.type.toLowerCase().equals("donor_sex") //from ENA SRA
                 || attr.type.toLowerCase().equals("sample gender") //from ENA SRA
                 || attr.type.toLowerCase().equals("sex stage") //from ENA SRA
                 || attr.type.toLowerCase().equals("sexs") //from ENA SRA
                 ) {
             //this will handle the real corrections
             return correctSex(new SexAttribute(attr.getAttributeValue()), sampledata);
         }
         
         //change samplename into synonyms
         if (attr.type.toLowerCase().equals("sample name") 
                 || attr.type.toLowerCase().equals("sample_name")
                 ) {
             return new CommentAttribute("synonym", attr.getAttributeValue());
         }
         //change sample description into secondary description
         if (attr.type.toLowerCase().equals("sample description") 
                 || attr.type.toLowerCase().equals("sample_description")
                 ) {
             return new CommentAttribute("secondary description", attr.getAttributeValue());
         }
         
         //TODO make material a separate attribute
         
         // fix typos
         if (attr.type.toLowerCase().equals("age")) {
             attr.type = "age";
             //TODO some simple regex expansions, e.g. 5W to 5 weeks
         } else if (attr.type.toLowerCase().equals("age in years")
                 || attr.type.toLowerCase().equals("age_in_years")) {
             attr.type = "age";
             attr.unit = new UnitAttribute();
             attr.unit.type = null;
             attr.unit.setAttributeValue("year");
         } else if (attr.type.toLowerCase().equals("developmental stage")
                 || attr.type.toLowerCase().equals("developmentalstage")
                 || attr.type.toLowerCase().equals("dev-stage")
                 || attr.type.toLowerCase().equals("dev_stage")
                 || attr.type.toLowerCase().equals("develomental stage") //typo
                 || attr.type.toLowerCase().equals("developmental point")
                 || attr.type.toLowerCase().equals("developmental satge") //typo
                 || attr.type.toLowerCase().equals("developmental stages")
                 || attr.type.toLowerCase().equals("developmental_stage")
                 || attr.type.toLowerCase().equals("developmetal stage") //typo
                 || attr.type.toLowerCase().equals("develpmental stage") //typo
                 || attr.type.toLowerCase().equals("tissue/dev_stage")
                 || attr.type.toLowerCase().equals("dissue/developmental stage")) {
             attr.type = "developmental stage";
         } else if (attr.type.toLowerCase().equals("disease state")
                 || attr.type.toLowerCase().equals("diseasestate")) {
             attr.type = "disease state";
         } else if (attr.type.toLowerCase().equals("ecotype")
                 || attr.type.toLowerCase().equals("strain/ecotype")) {
             attr.type = "ecotype";
             if (attr.getAttributeValue().toLowerCase().equals("col-0")
                     || attr.getAttributeValue().toLowerCase().equals("columbia-0")
                     || attr.getAttributeValue().toLowerCase().equals("columbia (col0) ")) {
                     attr.setAttributeValue("Columbia-0");
             } else if (attr.getAttributeValue().toLowerCase().equals("columbia")
                     || attr.getAttributeValue().toLowerCase().equals("col")) {
                 attr.setAttributeValue("Columbia");
             }
         } else if (attr.type.toLowerCase().equals("ethnicity")) {
             attr.type = "ethnicity";
             //ethnicity, population, race are a mess, leave alone
         } else if (attr.type.toLowerCase().equals("genotype")
                 || attr.type.toLowerCase().equals("individualgeneticcharacteristics")
                 || attr.type.toLowerCase().equals("genotype/variation") ) {
             attr.type = "genotype";
             if (attr.getAttributeValue().toLowerCase().equals("wildtype")
                     || attr.getAttributeValue().toLowerCase().equals("wild type")
                     || attr.getAttributeValue().toLowerCase().equals("wild-type")
                     || attr.getAttributeValue().toLowerCase().equals("wild_type")
                     || attr.getAttributeValue().toLowerCase().equals("wt")) {
                 attr.setAttributeValue("wild type");
             }
         } else if (attr.type.toLowerCase().equals("histology")) {
             attr.type = "histology";
         } else if (attr.type.toLowerCase().equals("individual")) {
             //TODO investigate
             attr.type = "individual";
         } else if (attr.type.toLowerCase().equals("organism part") 
                 || attr.type.toLowerCase().equals("organismpart")
                 || attr.type.toLowerCase().equals("tissue")
                 || attr.type.toLowerCase().equals("tissue type")
                 || attr.type.toLowerCase().equals("source tissue")
                 || attr.type.toLowerCase().equals("tissue -type")
                 || attr.type.toLowerCase().equals("tissue-type")
                 || attr.type.toLowerCase().equals("tissue_type")
                 || attr.type.toLowerCase().equals("tissue origin")) {
             attr.type = "organism part";
             if (attr.getAttributeValue().toLowerCase().equals("blood")) {
                 attr.setAttributeValue("blood");
                 attr.setTermSourceREF(sampledata.msi.getOrAddTermSource(efo));
                 attr.setTermSourceID("http://www.ebi.ac.uk/efo/EFO_0000296");
             } else if (attr.getAttributeValue().toLowerCase().equals("skin")) {
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
             attr.type = "phenotype";
         } else if (attr.type.toLowerCase().equals("stage")) {
             attr.type = "stage";
         } else if (attr.type.toLowerCase().equals("cultivar")
                 || attr.type.toLowerCase().equals("cultivar_acc")
                 || attr.type.toLowerCase().equals("cultivar/accession")) {
             attr.type = "cultivar";
         } else if (attr.type.toLowerCase().equals("strain")
                 || attr.type.toLowerCase().equals("strainorline")
                 || attr.type.toLowerCase().equals("strain or line")
                 || attr.type.toLowerCase().equals("strain (or line)")
                 || attr.type.toLowerCase().equals("strain name")
                 || attr.type.toLowerCase().equals("strain background")
                 || attr.type.toLowerCase().equals("strain/background")
                 || attr.type.toLowerCase().equals("strain/genotype")
                 || attr.type.toLowerCase().equals("strain description")
                 || attr.type.toLowerCase().equals("strains")
                 || attr.type.toLowerCase().equals("strain id")
                 || attr.type.toLowerCase().equals("strain source")
                 || attr.type.toLowerCase().equals("strain details")
                 || attr.type.toLowerCase().equals("strain type")
                 || attr.type.toLowerCase().equals("strain fgsc number")
                 || attr.type.toLowerCase().equals("strain(s)")
                 || attr.type.toLowerCase().equals("strain (cy3)")
                 || attr.type.toLowerCase().equals("strain (cy5)")
                 || attr.type.toLowerCase().equals("strain (mouse)")
                 || attr.type.toLowerCase().equals("strain (rat)")
                 || attr.type.toLowerCase().equals("strain/accession")
                 || attr.type.toLowerCase().equals("strain value")
                 || attr.type.toLowerCase().equals("plant strain")
                 || attr.type.toLowerCase().equals("type_strain")
                 || attr.type.toLowerCase().equals("cell line")
                 || attr.type.toLowerCase().equals("cell line/clone")
                 || attr.type.toLowerCase().equals("cell line specifics")
                 || attr.type.toLowerCase().equals("cell lines")
                 || attr.type.toLowerCase().equals("cell l ine") //typo
                 || attr.type.toLowerCase().equals("cell loine") //typo
                 || attr.type.toLowerCase().equals("cell lineage")
                 || attr.type.toLowerCase().equals("cell-line")
                 || attr.type.toLowerCase().equals("cell_line")
                 || attr.type.toLowerCase().equals("cellline")
                 //|| attr.type.toLowerCase().equals("cells") //from ENA SRA
                 || attr.type.toLowerCase().equals("tissue/cell lines")
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
                 || attr.type.toLowerCase().equals("celltype")
                 || attr.type.toLowerCase().equals("developmental stage/cell type")
                 || attr.type.toLowerCase().equals("disease/cell type")
                 || attr.type.toLowerCase().equals("tissue/cell type")) {
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
         } else if (attr.type.toLowerCase().equals("latitude")
                 || attr.type.toLowerCase().equals("geographic location (latitude)")
                 || attr.type.toLowerCase().equals("lat")) {
             if (attr.getAttributeValue().matches("^[0-9.-]+$")) {
                 attr.type = "latitude";
             } else {
                 attr.type = "latitude (raw)";
             }
         } else if (attr.type.toLowerCase().equals("longitude")
                 || attr.type.toLowerCase().equals("geographic location (longitude)")
                 || attr.type.toLowerCase().equals("lng")) {
             if (attr.getAttributeValue().matches("^[0-9.-]+$")) {
                 attr.type = "longitude";
             } else {
                 attr.type = "longitude (raw)";
             }
         } else if (attr.type.toLowerCase().equals("substrain")
                 || attr.type.toLowerCase().equals("sub-strain")
                 || attr.type.toLowerCase().equals("sub_strain")) {
             attr.type = "substrain"; 
         } else if (attr.type.toLowerCase().equals("sub_species")
                 || attr.type.toLowerCase().equals("subsp")
                 || attr.type.toLowerCase().equals("subsp.")
                 || attr.type.toLowerCase().equals("subspecies")) {
             attr.type = "subspecies"; 
         } else if (attr.type.toLowerCase().equals("mating type")
                 || attr.type.toLowerCase().equals("mating-type")
                 || attr.type.toLowerCase().equals("mating_type")) {
             attr.type = "mating type"; 
         //demote some to comments
         } else if (attr.type.toLowerCase().equals("collected by")
                 || attr.type.toLowerCase().equals("collected-by")
                 || attr.type.toLowerCase().equals("collected_by")) {
             return new CommentAttribute("collected by", attr.getAttributeValue());
         } else if (attr.type.toLowerCase().equals("collection_date")
                 || attr.type.toLowerCase().equals("collection date")
                 || attr.type.toLowerCase().equals("collection date (yyyymmdd)")
                 || attr.type.toLowerCase().equals("collection year")
                 || attr.type.toLowerCase().equals("collection-date")
                 || attr.type.toLowerCase().equals("collection_year")
                 || attr.type.toLowerCase().equals("date of collection")
                 || attr.type.toLowerCase().equals("date sample colllected")
                 || attr.type.toLowerCase().equals("isolation_year")
                 || attr.type.toLowerCase().equals("sample collection date")
                 || attr.type.toLowerCase().equals("sample date")
                 || attr.type.toLowerCase().equals("sampling date")
                 || attr.type.toLowerCase().equals("sampling-date")
                 || attr.type.toLowerCase().equals("time of sample collection")
                 || attr.type.toLowerCase().equals("year isolated")) {
             return new CommentAttribute("colection date", attr.getAttributeValue());
         } 
         
         
         //TODO demote some characteristics to comments
         
 
         if (attr.unit != null){
             attr.unit = correctUnit(attr.unit);
         }
         
         return attr;
     }
 
     
     private SCDNodeAttribute correctComment(CommentAttribute attr, SampleData sampledata) {        
         //bulk replace underscore with space in types
         attr.type = attr.type.replace("_", " ");
 
         //change samplename into synonyms
         if (attr.type.toLowerCase().equals("sample name") 
                 || attr.type.toLowerCase().equals("sample_name")
                 ) {
             return new CommentAttribute("synonym", attr.getAttributeValue());
         }
         //change sample description into secondary description
         if (attr.type.toLowerCase().equals("sample description") 
                 || attr.type.toLowerCase().equals("sample_description")
                 ) {
             return new CommentAttribute("secondary description", attr.getAttributeValue());
         }
                 
         //TODO promote some comments to characteristics
         
 
         if (attr.unit != null){
             attr.unit = correctUnit(attr.unit);
         }
         
         return attr;
     }
     
     public void correct(SampleData sampledata) {
         if (sampledata.msi.submissionTitle == null || sampledata.msi.submissionTitle.length() == 0 ) {
             sampledata.msi.submissionTitle = SampleTabUtils.generateSubmissionTitle(sampledata);
         } else {
             sampledata.msi.submissionTitle = stripHTML(sampledata.msi.submissionTitle);
         }
         
         if (sampledata.msi.submissionDescription == null || sampledata.msi.submissionDescription.length() == 0) {
             //no submission description
             //check if there is a publication
             //Collection<Integer> pubmedids = sampledata.msi.getPubmedIDs();
 
             //Do it this way so it is compatible with limpopo-sampletab rather than SNAPSHOT
             Collection<Integer> pubmedids = new ArrayList<Integer>();
             for (Publication p : sampledata.msi.publications) {
                 Integer pubmedid = null;
                 try {
                     pubmedid = Integer.parseInt(p.getPubMedID());
                 } catch (NumberFormatException e) {
                     //do nothing
                 }
                 if (pubmedid != null && !pubmedids.contains(pubmedid)){
                     pubmedids.add(pubmedid);
                 }
             }
             
             
             if (pubmedids.size() > 0) {
                 sampledata.msi.submissionDescription = "Samples from  publications. ";
                 for (Integer i : pubmedids) {
                     String title = null;
                     try {
                         title = EuroPMCUtils.getTitleByPUBMEDid(i);
                    } catch (QueryException_Exception e) {
                         log.error("Problem getting PubMedID "+i, e);
                     }
                     if (title != null) {
                         sampledata.msi.submissionDescription = sampledata.msi.submissionDescription + title+" ";
                     }
                 }
             }
         } else {
             sampledata.msi.submissionDescription = stripHTML(sampledata.msi.submissionDescription);
         }
         
         
         
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
                             || cha.getAttributeValue().toLowerCase().equals("null")
                             || cha.getAttributeValue().toLowerCase().equals("missing")
                             || cha.getAttributeValue().toLowerCase().equals("[not reported]")
                             || cha.getAttributeValue().toLowerCase().equals("[not requested]")) {
                         //leave unknown-sex as is. implies it has been looked at and is non-determinate
                         s.removeAttribute(cha);
                         continue;
                     }
                 }
 
                 SCDNodeAttribute updated = a;
                 
                 boolean isCharacteristic = false;
                 synchronized(CharacteristicAttribute.class) {
                     isCharacteristic = CharacteristicAttribute.class.isInstance(a);
                 }
                 boolean isComment = false;
                 synchronized(CommentAttribute.class) {
                     isComment = CommentAttribute.class.isInstance(a);
                 }
                 boolean isSex = false;
                 synchronized(SexAttribute.class) {
                     isSex = SexAttribute.class.isInstance(a);
                 }
                 boolean isOrganism = false;
                 synchronized(OrganismAttribute.class) {
                     isOrganism = OrganismAttribute.class.isInstance(a);
                 }
                 // tidy all characteristics
                 if (isCharacteristic) {
                     updated = correctCharacteristic((CharacteristicAttribute) a, sampledata);
                 } else if (isSex) {
                     updated = correctSex((SexAttribute) a, sampledata);
                 } else if (isOrganism) {
                     updated = correctOrganism((OrganismAttribute) a, sampledata);
                 } else if (isComment) {
                     updated = correctComment((CommentAttribute) a, sampledata);
                 }
                 
                 //TODO promote some comments to characteristics
                 //age
                 //gender
                 //strain
                 //tissue
 
                 boolean isRelationship = false;
                 synchronized(AbstractRelationshipAttribute.class) {
                     isRelationship = AbstractRelationshipAttribute.class.isInstance(a);
                 }
                 
                 if (isRelationship) {
                     //Relationships may refer to other samples in the same submission by name
                     //It is better to refer by BioSD accession.
                     AbstractRelationshipAttribute rela = (AbstractRelationshipAttribute) a;
                     String targetName = rela.getAttributeValue();
                     SampleNode target = sampledata.scd.getNode(targetName, SampleNode.class);
                     if (target != null && target.getSampleAccession() != null) {
                         rela.setAttributeValue(target.getSampleAccession());
                     }
                 }
 
                 
                 //comparison by identity
                 //replace in same position
                 if (updated != a) {
                     int i = s.getAttributes().indexOf(a);
                     s.removeAttribute(a);
                     if (updated != null) {
                         s.addAttribute(updated, i);
                     }
                 }
             }
             //convert some attributes into values on the sample itself i.e. Sample Description
             for (SCDNodeAttribute a : new ArrayList<SCDNodeAttribute>(s.getAttributes())) {
                 boolean isCommentAttribute = false;
                 synchronized(CommentAttribute.class){
                     isCommentAttribute = CommentAttribute.class.isInstance(a);
                 }
                 boolean isCharacteristicAttribute = false;
                 synchronized(CharacteristicAttribute.class){
                     isCharacteristicAttribute = CharacteristicAttribute.class.isInstance(a);
                 }
                 
                 if (s.getSampleDescription() == null && isCommentAttribute) {
                     CommentAttribute ca = (CommentAttribute) a;
                     if (ca.type.toLowerCase().equals("sample description") || 
                             ca.type.toLowerCase().equals("sample_description") || 
                             ca.type.toLowerCase().equals("description")) {
                         s.setSampleDescription(ca.getAttributeValue());
                         s.removeAttribute(a);
                     }
                 } else if (s.getSampleDescription() == null && isCharacteristicAttribute) {
                     CharacteristicAttribute ca = (CharacteristicAttribute) a;
                     if (ca.type.toLowerCase().equals("sample description") || 
                             ca.type.toLowerCase().equals("sample_description") || 
                             ca.type.toLowerCase().equals("description")) {
                         s.setSampleDescription(ca.getAttributeValue());
                         s.removeAttribute(a);
                     }
                 }
             }
         }
         
         //correct term sources
         CorrectorTermSource cts = new CorrectorTermSource();
         cts.correct(sampledata);
         
         //try to normalize the object model
         Normalizer norm = new Normalizer();
         norm.normalize(sampledata);
     }
 }
