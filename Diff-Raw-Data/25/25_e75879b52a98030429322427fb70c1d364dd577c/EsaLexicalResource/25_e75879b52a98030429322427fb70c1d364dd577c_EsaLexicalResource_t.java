 package lexent.resource.esa;
 
 import java.io.File;
 import java.util.Arrays;
 
 import lexent.resource.LexicalResource;
import lexent.resource.LocalContext;
 import de.tudarmstadt.ukp.similarity.algorithms.api.SimilarityException;
 import de.tudarmstadt.ukp.similarity.algorithms.vsm.InnerVectorProduct;
 import de.tudarmstadt.ukp.similarity.algorithms.vsm.VectorComparator;
 import de.tudarmstadt.ukp.similarity.algorithms.vsm.VectorNorm;
 import de.tudarmstadt.ukp.similarity.algorithms.vsm.store.vectorindex.VectorIndexReader;
 
 public class EsaLexicalResource implements LexicalResource {
 	
 	public EsaLexicalResource() {
 		esa = new VectorComparator(new VectorIndexReader(new File("../DKPRO_HOME/esaIndexesVector/en/wp")));
 		esa.setInnerProduct(InnerVectorProduct.LEFT_DICE);
 		esa.setNormalization(VectorNorm.NONE);
 		// TODO use Lili's sim func (balAPInc) instead
 	}
 	
	public double probEntails(String t, String h, LocalContext context) {
 		try {
 			return esa.getSimilarity(Arrays.asList(t), Arrays.asList(h));
 		} catch (SimilarityException e) {
 			return 0.0;
 		}
 	}
 	
 	private VectorComparator esa;
 	
 }
