 package uk.ac.ebi.fgpt.sampletab;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SCDNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 
 public class CorrectorTermSource {
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void correct(SampleData sampledata) {
         //remove any unused term sources
         //first find which are used
         Set<String> usedTsNames = new HashSet<String>();
         for (SCDNode scdnode : sampledata.scd.getAllNodes()){
             for (SCDNodeAttribute attr : scdnode.getAttributes()){
                 if (AbstractNodeAttributeOntology.class.isInstance(attr)){
                     AbstractNodeAttributeOntology attrOnt = (AbstractNodeAttributeOntology) attr;
                     //if this attribute has a term source at all
                     if (attrOnt.getTermSourceREF() != null && attrOnt.getTermSourceREF().trim().length() > 0){
                         //add it to the pool
                         usedTsNames.add(attrOnt.getTermSourceREF().trim());
                     }
                 }
             }
         }
         
         //now trim them from the msi
         //use a new list so original can be deleted from
         for (TermSource ts: new ArrayList<TermSource>(sampledata.msi.termSources)){
             if (!usedTsNames.contains(ts.getName())) {
                 log.info("Removing unused term source "+ts.getName());
                 sampledata.msi.termSources.remove(ts);
             }
         }
         
         //add to the msi any term source which samples use
         for (String usedTsName : usedTsNames) {
             boolean exists = false;
             for (TermSource ts: new ArrayList<TermSource>(sampledata.msi.termSources)) {
                 if (usedTsName.equals(ts.getName())) {
                     exists = true;
                     break;
                 }
             }
             
             if (!exists) {
                 TermSource ts = new TermSource(usedTsName, null, null);
                 sampledata.msi.termSources.add(ts);
             }
         }
         
         
         //in some cases, term sources have a null URL
         //for a sub-set of these we can guess them from being popular, e.g. EFO, NCBI Taxonomy, etc
         //for others, delete them as they are not useful
         for (int i = 0; i < sampledata.msi.termSources.size(); i++){
             TermSource ts = sampledata.msi.termSources.get(i);
             if (ts.getURI() == null){
                 if (ts.getName().equals("EFO")){
                     ts = new TermSource(ts.getName(), "http://www.ebi.ac.uk/efo/", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("PAR")){
                     ts = new TermSource(ts.getName(), "http://psidev.cvs.sourceforge.net/psidev/psi/mi/controlledVocab/proteomeBinder/psi-par.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("PSI")){
                     ts = new TermSource(ts.getName(), "http://psidev.sourceforge.net/ms/xml/mzdata/psi-ms-cv-1.7.2.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("MeSH")){
                     ts = new TermSource(ts.getName(), "http://www.nlm.nih.gov/mesh/", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("MP")){
                     ts = new TermSource(ts.getName(), "ftp://ftp.informatics.jax.org/pub/reports/MPheno_OBO.ontology", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("PRIDE")){
                     ts = new TermSource(ts.getName(), "http://pride-proteome.cvs.sourceforge.net/pride-proteome/PRIDE/schema/pride_cv.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("NEWT")){
                     ts = new TermSource(ts.getName(), "http://www.ebi.ac.uk/newt/", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("GPM")){
                     ts = new TermSource(ts.getName(), "http://www.thegpm.org/", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("GPMDB")){
                     ts = new TermSource(ts.getName(), "http://gpmdb.thegpm.org/", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("MS")){
                     ts = new TermSource(ts.getName(), "http://psidev.cvs.sourceforge.net/viewvc/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("CCO")){
                     ts = new TermSource(ts.getName(), "http://cellcycleonto.cvs.sourceforge.net/cellcycleonto/ONTOLOGIES/OBO/cco.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("MOD")){
                     ts = new TermSource(ts.getName(), "http://psidev.cvs.sourceforge.net/psidev/psi/mod/data/PSI-MOD.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("SEP")){
                     ts = new TermSource(ts.getName(), "http://code.google.com/p/gelml/source/browse/trunk/CV/sep.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("DOID")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/phenotype/human_disease.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("CL")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/anatomy/cell_type/cell.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("IDO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/infectious_disease.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("EO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/phenotype/environment/environment_ontology.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("BTO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/anatomy/BrendaTissue.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("GO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/genomic-proteomic/gene_ontology.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("PO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/developmental/plant_development/plant/po_temporal.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("TAIR")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/developmental/plant_development/Arabidopsis/arabidopsis_development.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("ZEA")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/anatomy/gross_anatomy/plant_gross_anatomy/cereals/zea_mays_anatomy.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("CHEBI")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/chemical/chebi.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("UO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/phenotype/unit.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("ZFA")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/zebrafish_anatomy.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("TAO")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/teleost_anatomy.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else if (ts.getName().equals("DDANAT")){
                     ts = new TermSource(ts.getName(), "http://obo.cvs.sourceforge.net/obo/obo/ontology/anatomy/gross_anatomy/microbial_gross_anatomy/dictyostelium/dictyostelium_anatomy.obo", null);
                     sampledata.msi.termSources.set(i, ts);
                 } else {
                     log.error("Removing null term source "+ts.getName());
                     sampledata.msi.termSources.remove(i);
                     //because the list is now shorter, adjust the counter too
                     i--;
                     //now remove any reference to this ontology on any node
                     for (SCDNode scdnode : sampledata.scd.getAllNodes()){
                         for (SCDNodeAttribute attr : scdnode.getAttributes()){
                             if (AbstractNodeAttributeOntology.class.isInstance(attr)){
                                 AbstractNodeAttributeOntology attrOnt = (AbstractNodeAttributeOntology) attr;
                                 //if this attribute has this term source, remove it
                                if (attrOnt.getTermSourceREF().equals(ts.getName())){
                                     attrOnt.setTermSourceREF(null);
                                     attrOnt.setTermSourceIDInteger(null);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         //TODO add a date if no version present?
     }
 }
