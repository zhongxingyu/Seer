 package edu.mssm.pharm.maayanlab.Enrichr;
 
import edu.mssm.pharm.maayanlab.common.core.FileUtils;
 import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory;
 import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.GeneSetLibrary;
 
 public class GenerateRegressionFiles {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Enrichment app = new Enrichment(FileUtils.readResource("test_list.txt"));
 		for (EnrichmentCategory category : ResourceLoader.getInstance().getCategories())
 			for (GeneSetLibrary library : category.getLibraries())
 				FileUtils.writeFile("test_list." + library.getName() + "_table.txt", Enrichment.HEADER, app.enrich(library.getName()));
 	}
 
 }
