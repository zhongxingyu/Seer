 package gov.nih.nci.nautilus.test;
 
 import junit.framework.TestCase;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import gov.nih.nci.nautilus.criteria.*;
 import gov.nih.nci.nautilus.de.*;
 import gov.nih.nci.nautilus.query.CompoundQuery;
 import gov.nih.nci.nautilus.query.GeneExpressionQuery;
 import gov.nih.nci.nautilus.query.QueryManager;
 import gov.nih.nci.nautilus.query.QueryType;
 import gov.nih.nci.nautilus.query.ComparativeGenomicQuery;
 import gov.nih.nci.nautilus.view.GeneExprSampleView;
 import gov.nih.nci.nautilus.view.GroupType;
 import gov.nih.nci.nautilus.view.ViewFactory;
 import gov.nih.nci.nautilus.view.ViewType;
 import gov.nih.nci.nautilus.queryprocessing.ge.GeneExpr;
 import gov.nih.nci.nautilus.resultset.ResultsetProcessor;
 import gov.nih.nci.nautilus.resultset.*;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprSampleViewContainer;
 import gov.nih.nci.nautilus.resultset.gene.GeneExprSingleViewResultsContainer;
 import gov.nih.nci.nautilus.resultset.gene.GeneResultset;
 import gov.nih.nci.nautilus.resultset.gene.ViewByGroupResultset;
 import gov.nih.nci.nautilus.resultset.gene.ReporterResultset;
 import gov.nih.nci.nautilus.resultset.gene.SampleFoldChangeValuesResultset;
 
 import java.text.DecimalFormat;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: BhattarR
  * Date: Sep 20, 2004
  * Time: 1:41:11 PM
  * To change this template use Options | File Templates.
  */
 public class QueryTest extends TestCase {
 	 private static final DecimalFormat resultFormat = new DecimalFormat("0.00");
      DiseaseOrGradeCriteria diseaseCrit;
      FoldChangeCriteria foldCrit;
      CopyNumberCriteria copyNumberCrit;
      GeneIDCriteria  geneIDCrit;
      GeneOntologyCriteria ontologyCrit;
      PathwayCriteria pathwayCrit;
      RegionCriteria regionCrit;
      CloneOrProbeIDCriteria cloneCrit;
      CloneOrProbeIDCriteria probeCrit;
      ArrayPlatformCriteria allPlatformCrit;
      ArrayPlatformCriteria affyOligoPlatformCrit;
      ArrayPlatformCriteria cdnaPlatformCrit;
      SNPCriteria snpCrit;
 
     protected void setUp() throws Exception {
         buildDiseaseTypeCrit();
         buildRegionCrit();
         buildPlatformCrit();
         buildCloneCrit();
         buildProbeCrit();
         buildFoldChangeCrit();
         buildGeneIDCrit();
         buildOntologyCrit();
         buildPathwayCrit();
         buildSNPCrit();
         buildCopyChangeCrit();
     }
     public static class GeneExpression extends QueryTest {
           public void testGeneExprQuery() {
         GeneExpressionQuery q = (GeneExpressionQuery) QueryManager.createQuery(QueryType.GENE_EXPR_QUERY_TYPE);
              q.setQueryName("Test Gene Query");
             q.setAssociatedView(ViewType.GENE_SINGLE_SAMPLE_VIEW);
             //q.setAssociatedView(ViewFactory.newView(ViewType.GENE_GROUP_SAMPLE_VIEW));
             //q.setGeneIDCrit(geneIDCrit);
             q.setGeneOntologyCrit(ontologyCrit);
             //q.setRegionCrit(regionCrit);
             //q.setPathwayCrit(pathwayCrit);
 
             q.setArrayPlatformCrit(allPlatformCrit);
            //q.setPlatCriteria(affyOligoPlatformCrit);
            //q.setPlatCriteria(cdnaPlatformCrit);
 
             //q.setCloneOrProbeIDCrit(cloneCrit);
             //q.setCloneProbeCrit(probeCrit);
             q.setDiseaseOrGradeCrit(diseaseCrit);
             q.setFoldChgCrit(foldCrit);
 
             try {
                ResultSet[] geneExprObjects = QueryManager.executeQuery(q);
                 print(geneExprObjects);
                 if (geneExprObjects.length > 0)
                     testResultset(geneExprObjects);
             } catch(Throwable t ) {
                 t.printStackTrace();
             }
 
     }
       private void print(ResultSet[] geneExprObjects) {
             int count = 0;
             HashSet probeIDS = new HashSet();
             HashSet cloneIDs = new HashSet();
 
 
           for (int i = 0; i < geneExprObjects.length; i++) {
               ResultSet obj = geneExprObjects[i];
                 GeneExpr exprObj = null;
                 if (obj instanceof GeneExpr.GeneExprGroup)  {
                     exprObj = (GeneExpr.GeneExprGroup) obj;
                 }
                  else if (obj instanceof GeneExpr.GeneExprSingle)  {
                     exprObj = (GeneExpr.GeneExprSingle) obj;
                 }
                 if (exprObj.getProbesetId() != null) {
                     System.out.println("Disease: "+ exprObj.getDiseaseType());
 
                   System.out.println("ProbesetID: " + exprObj.getProbesetId() + " :Exp Value: "
                                 + exprObj.getExpressionRatio() + "  GeneSymbol: " + exprObj.getGeneSymbol() );
                     probeIDS.add(exprObj.getProbesetId());
                 }
                 if ( exprObj.getCloneId() != null) {
                     System.out.println("CloneID: " + exprObj.getCloneId()+ " :Exp Value: "
                                 + exprObj.getExpressionRatio() + "  GeneSymbol: " + exprObj.getGeneSymbol());
                     cloneIDs.add(exprObj.getCloneId() );
                 }
 
                  ++count;
             }
             System.out.println("Total Number Of Samples: " + count);
             StringBuffer p = new StringBuffer();
             for (Iterator iterator = probeIDS.iterator(); iterator.hasNext();) {
                 Long aLong = (Long) iterator.next();
                 p.append(aLong.toString() + ",");
             }
             System.out.println("Total Probes: " + probeIDS.size());
             System.out.println(p.toString());
             StringBuffer c = new StringBuffer();
             for (Iterator iterator = cloneIDs.iterator(); iterator.hasNext();) {
                 Long aLong = (Long) iterator.next();
                 c.append(aLong.toString() + ",");
             }
             System.out.println("Total clones: " + cloneIDs.size());
             System.out.println(c.toString());
 
             return ;
     }
       public void testResultset(ResultSet[] geneExprObjects){
     	assertNotNull(geneExprObjects);
         assertTrue(geneExprObjects.length > 0);
         ResultsContainer resultsContainer = ResultsetProcessor.handleGeneExprView(geneExprObjects, GroupType.DISEASE_TYPE_GROUP);
 		if (resultsContainer instanceof GeneExprSampleViewContainer){
 			GeneExprSampleViewContainer geneExprSampleViewContainer = (GeneExprSampleViewContainer) resultsContainer;
 	        GeneExprSingleViewResultsContainer geneViewContainer = geneExprSampleViewContainer.getGeneExprSingleViewContainer();
 
 	    	Collection genes = geneViewContainer.getGeneResultsets();
 	    	Collection labels = geneViewContainer.getGroupsLabels();
 	    	Collection sampleIds = null;
 	    	StringBuffer header = new StringBuffer();
 	    	StringBuffer sampleNames = new StringBuffer();
 	        StringBuffer stringBuffer = new StringBuffer();
 	    	header.append("Gene\tReporter\t");
 	    	sampleNames.append("Name\tName\t\tType\t");
 	    	for (Iterator labelIterator = labels.iterator(); labelIterator.hasNext();) {
 	        	String label = (String) labelIterator.next();
 	        	header.append("Disease: "+label);
 	        	sampleIds = geneViewContainer.getBiospecimenLabels(label);
 	           	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 	            	sampleNames.append(sampleIdIterator.next()+"\t");
 	            	header.append("\t");
 	           	}
 	
 	    	}
 	
 	    	//System.out.println("Gene Count: "+genes.size());
 			System.out.println(header.toString());
 			System.out.println(sampleNames.toString());
 	    	for (Iterator geneIterator = genes.iterator(); geneIterator.hasNext();) {
 	    		GeneResultset geneResultset = (GeneResultset)geneIterator.next();
 	    		Collection reporters = geneResultset.getReporterResultsets();
 	        	//System.out.println("Repoter Count: "+reporters.size());
 	    		for (Iterator reporterIterator = reporters.iterator(); reporterIterator.hasNext();) {
 	        		ReporterResultset reporterResultset = (ReporterResultset)reporterIterator.next();
 	        		Collection groupTypes = reporterResultset.getGroupByResultsets();
 	            	//System.out.println("Group Count: "+groupTypes.size());
 	        		for (Iterator groupIterator = groupTypes.iterator(); groupIterator.hasNext();) {
 	        			ViewByGroupResultset groupResultset = (ViewByGroupResultset)groupIterator.next();
 	        			String label = groupResultset.getType().getValue().toString();
 	        			sampleIds = geneViewContainer.getBiospecimenLabels(label);
 	//        			Collection biospecimens = groupResultset.getBioSpecimenResultsets();
 	//                	System.out.println("Biospecimen Count: "+biospecimens.size());
 	//            		for (Iterator biospecimenIterator = biospecimens.iterator(); biospecimenIterator.hasNext();) {
 	//            			BioSpecimenResultset biospecimenResultset = (BioSpecimenResultset)biospecimenIterator.next();
 	        			            stringBuffer = new StringBuffer();
 	                	            stringBuffer.append(geneResultset.getGeneSymbol().getValueObject().toString()+"\t"+
 	                	            					reporterResultset.getReporter().getValue().toString()+"\t\t");
 														//"| GroupType : "+groupResultset.getType().getValue().toString()+"\t");
 	                               	for (Iterator sampleIdIterator = sampleIds.iterator(); sampleIdIterator.hasNext();) {
 	                               		String sampleId = (String) sampleIdIterator.next();
 	                               		SampleFoldChangeValuesResultset samplesResultset = groupResultset.getBioSpecimenResultset(sampleId);
 	                               		if(samplesResultset != null){
 	                               			Double ratio = (Double)samplesResultset.getFoldChangeRatioValue().getValue();
 	                               			stringBuffer.append(resultFormat.format(ratio)+"\t");
 	                               		}
 	                               	}
 	//            		}
 	                               	System.out.println(stringBuffer.toString());
 	        		}
 	
 	    		}
 	
 	    	}
 		  }
         }
     }
     public static class CGH extends QueryTest {
        public void testCGHExprQuery() {
         ComparativeGenomicQuery q = (ComparativeGenomicQuery) QueryManager.createQuery(QueryType.CGH_QUERY_TYPE);
              q.setQueryName("Test CGH Query");
              //q.setAssociatedView(ViewFactory.newView(ViewType.GENE_SINGLE_SAMPLE_VIEW));
              //q.setAssociatedView(ViewFactory.newView(ViewType.GENE_GROUP_SAMPLE_VIEW));
             //q.setGeneIDCrit(geneIDCrit);
             //q.setGeneOntologyCrit(ontologyCrit);
             //q.setRegionCrit(regionCrit);
             //q.setPathwayCrit(pathwayCrit);
 
             AssayPlatformCriteria crit = new AssayPlatformCriteria();
             crit.setAssayPlatformDE(new AssayPlatformDE(Constants.AFFY_100K_SNP_ARRAY));
             q.seAssayPlatformCrit(crit);
             //q.setRegionCrit(regionCrit);
             //q.setSNPCrit(snpCrit);
             //q.setGeneIDCrit(geneIDCrit);
             q.setDiseaseOrGradeCrit(diseaseCrit);
             q.setCopyNumberCrit(copyNumberCrit);
 
             try {
                 ResultSet[] cghObjects = QueryManager.executeQuery(q);
                 //print(geneExprObjects);
                 //testResultset(geneExprObjects);
             } catch(Throwable t ) {
                 t.printStackTrace();
             }
 
         }
     }
 
 
 
      public static Test suite() {
 		TestSuite suit =  new TestSuite();
         suit.addTest(new TestSuite(GeneExpression.class));
         //suit.addTest(new TestSuite(CGH.class));
         return suit;
 	}
 
 	public static void main (String[] args) {
 		junit.textui.TestRunner.run(suite());
 
     }
 
 
     private void buildGeneIDCrit() {
         ArrayList inputIDs = new ArrayList();
         //inputIDs.add(0, "220988");    // Locus Link for hnRNPA3
         //inputIDs.add(0, "BF195526");      // accession numbers for hnRNPA3 gene are AW080932, AA527502, AA528233
          // inputIDs.add(0, "hnRNPA3");
          GeneIdentifierDE.GeneSymbol gs = new GeneIdentifierDE.GeneSymbol("CTNND2");
          inputIDs.add(gs);
         // GeneIdentifierDE geIDObj =
            //       new GeneIdentifierDE.LocusLink((String)inputIDs.get(0));
        // GeneIdentifierDE geIDObj =
         //                new GeneIdentifierDE.GenBankAccessionNumber((String)inputIDs.get(0));
         //GeneIdentifierDE geIDObj =
           //              new GeneIdentifierDE.GeneSymbol((String)inputIDs.get(0));
         ArrayList geneIDentifiers = new ArrayList();
 
         geneIDentifiers.add(gs);
         geneIDCrit = new GeneIDCriteria();
         geneIDCrit.setGeneIdentifiers(geneIDentifiers);
     }
 
     private void buildRegionCrit() {
         regionCrit = new RegionCriteria();
 
         // cytoband and start & end positions are mutually exclusive
         //regionCrit.setCttoband(new CytobandDE("p36.23"));
         regionCrit.setStart(new BasePairPositionDE.StartPosition(new Integer(6900000)));
         regionCrit.setEnd(new BasePairPositionDE.EndPosition(new Integer(8800000)));
 
         // Chromosome Number is mandatory
         regionCrit.setChromNumber(new ChromosomeNumberDE(new String("1")));
     }
 
     private void buildOntologyCrit() {
         ontologyCrit = new GeneOntologyCriteria();
         ontologyCrit.setGOIdentifier(new GeneOntologyDE("GO:0008150"));
     }
     private void buildSNPCrit() {
         ArrayList inputIDs = new ArrayList();
         // build DBSNPs
         SNPIdentifierDE.DBSNP snp1 = new SNPIdentifierDE.DBSNP("rs1396904");
         SNPIdentifierDE.DBSNP snp2 = new SNPIdentifierDE.DBSNP("rs10489139");
         SNPIdentifierDE.DBSNP snp3 = new SNPIdentifierDE.DBSNP("rs4475768");
         SNPIdentifierDE.DBSNP snp4 = new SNPIdentifierDE.DBSNP("rs10492941");
         SNPIdentifierDE.DBSNP snp5 = new SNPIdentifierDE.DBSNP("rs966366");
 
 
         // build SNPProbesets
         /*SNPIdentifierDE.SNPProbeSet snp1 = new SNPIdentifierDE.SNPProbeSet ("SNP_A-1676650");
         SNPIdentifierDE.SNPProbeSet snp2 = new SNPIdentifierDE.SNPProbeSet ("SNP_A-1748913");
         SNPIdentifierDE.SNPProbeSet snp3 = new SNPIdentifierDE.SNPProbeSet ("SNP_A-1667950");
         SNPIdentifierDE.SNPProbeSet snp4 = new SNPIdentifierDE.SNPProbeSet ("SNP_A-1642581");
         SNPIdentifierDE.SNPProbeSet snp5 = new SNPIdentifierDE.SNPProbeSet ("SNP_A-1657367");
         */
 
         inputIDs.add(snp1 ); inputIDs.add(snp2);  inputIDs.add(snp3);
          inputIDs.add(snp4);  inputIDs.add(snp5);
         snpCrit = new SNPCriteria();
         snpCrit.setSNPIdentifiers(inputIDs);
     }
 
     private void buildPathwayCrit() {
         pathwayCrit = new PathwayCriteria();
         PathwayDE obj1 = new PathwayDE("AcetaminophenPathway");
         PathwayDE obj2 = new PathwayDE("41bbPathway");
         Collection pathways = new ArrayList();
         pathways.add(obj1); pathways.add(obj2);
         pathwayCrit.setPathwayNames(pathways);
     }
     private void buildPlatformCrit() {
         allPlatformCrit = new ArrayPlatformCriteria(new ArrayPlatformDE(Constants.ALL_PLATFROM));
         affyOligoPlatformCrit = new ArrayPlatformCriteria(new ArrayPlatformDE(Constants.AFFY_OLIGO_PLATFORM));
         cdnaPlatformCrit = new ArrayPlatformCriteria(new ArrayPlatformDE(Constants.CDNA_ARRAY_PLATFORM));
     }
     private void buildDiseaseTypeCrit() {
          diseaseCrit = new DiseaseOrGradeCriteria();
          diseaseCrit.setDisease(new DiseaseNameDE("GBM"));
     }
 
     private void buildProbeCrit() {
         probeCrit = new CloneOrProbeIDCriteria();
         //probeCrit.setCloneIdentifier(new CloneIdentifierDE.ProbesetID("204655_at"));
        // probeCrit.setCloneIdentifier(new CloneIdentifierDE.ProbesetID("243387_at"));
         probeCrit.setCloneIdentifier(new CloneIdentifierDE.ProbesetID("210354_at"));
 
     }
 
     private void buildCloneCrit() {
         cloneCrit = new CloneOrProbeIDCriteria();
         //cloneCrit.setCloneIdentifier(new CloneIdentifierDE.BACClone("IMAGE:755299"));
         //cloneCrit.setCloneIdentifier(new CloneIdentifierDE.BACClone("IMAGE:1287390"));
         //cloneCrit.setCloneIdentifier(new CloneIdentifierDE.BACClone("IMAGE:2709102"));
         //cloneCrit.setCloneIdentifier(new CloneIdentifierDE.BACClone("IMAGE:3303708"));
         cloneCrit.setCloneIdentifier(new CloneIdentifierDE.IMAGEClone("1579639"));
     }
 
 
     private void buildFoldChangeCrit() {
         Float upRegExpected = new Float(1.2);
         Float downRegExpected = new Float(0.8);
         ExprFoldChangeDE.UpRegulation upRegObj = new ExprFoldChangeDE.UpRegulation(upRegExpected );
         ExprFoldChangeDE.DownRegulation downRegObj = new ExprFoldChangeDE.DownRegulation(downRegExpected );
         ExprFoldChangeDE.UnChangedRegulationUpperLimit upUnChangedObj = new ExprFoldChangeDE.UnChangedRegulationUpperLimit(upRegExpected  );
         ExprFoldChangeDE.UnChangedRegulationDownLimit downUnChangedRegObj = new ExprFoldChangeDE.UnChangedRegulationDownLimit(downRegExpected );
 
         foldCrit = new FoldChangeCriteria();
         Collection objs = new ArrayList(4);
         //objs.add(upRegObj);
         //objs.add(downRegObj);
         objs.add(upUnChangedObj); objs.add(downUnChangedRegObj);
         foldCrit.setFoldChangeObjects(objs);
     }
 
      private void buildCopyChangeCrit() {
         Float amplification = new Float(2.0);
         Float deletion = new Float(0.8);
         CopyNumberDE.Amplification ampObj = new CopyNumberDE.Amplification(amplification );
         CopyNumberDE.Deletion deletionObj = new CopyNumberDE.Deletion(deletion);
         CopyNumberDE.UnChangedCopyNumberUpperLimit upCopyNumberObj = new CopyNumberDE.UnChangedCopyNumberUpperLimit(amplification);
         CopyNumberDE.UnChangedCopyNumberDownLimit  downCopyNumberObj = new CopyNumberDE.UnChangedCopyNumberDownLimit(deletion);
 
         copyNumberCrit = new CopyNumberCriteria();
         Collection objs = new ArrayList(4);
         objs.add(ampObj);
         //objs.add(deletionObj);
         //objs.add(upCopyNumberObj); objs.add(downCopyNumberObj);
         copyNumberCrit.setCopyNumbers(objs);
     }
 }
