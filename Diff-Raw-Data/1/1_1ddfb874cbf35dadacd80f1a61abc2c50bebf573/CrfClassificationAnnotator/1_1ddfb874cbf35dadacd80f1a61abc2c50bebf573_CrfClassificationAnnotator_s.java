 /**
  * 
  */
 package com.maalaang.comtwitter.uima.annotator;
 
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 
 import cc.mallet.fst.CRF;
 import cc.mallet.pipe.Pipe;
 import cc.mallet.types.ArraySequence;
 import cc.mallet.types.Instance;
 
 import com.maalaang.omtwitter.uima.type.TokenAnnotation;
 
 /**
  * @author Sangwon Park
  *
  */
 public class CrfClassificationAnnotator extends JCasAnnotator_ImplBase {
 	private final String PARAM_CRF_MODEL_FILE = "crfModelFile";
 	
 	private	CRF crf = null;
 	private Pipe pipe = null;
 	private Logger logger = null;
 	
 	public void initialize(UimaContext aContext) throws ResourceInitializationException {
 		super.initialize(aContext);
 		
 		logger = aContext.getLogger();
 		
 		try {
 			FileInputStream fis = new FileInputStream((String)aContext.getConfigParameterValue(PARAM_CRF_MODEL_FILE));
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			crf = (CRF)ois.readObject();
 			pipe = crf.getInputPipe();
 			pipe.setTargetProcessing(false);
 			
 			ois.close();
 			fis.close();
 			
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, e.getMessage());
 			throw new ResourceInitializationException(e);
 		}		
 		
 	}
 
 	public void process(JCas aJCas) throws AnalysisEngineProcessException {
 		FSIterator<Annotation> it = aJCas.getAnnotationIndex(TokenAnnotation.type).iterator();
 		int size = aJCas.getAnnotationIndex(TokenAnnotation.type).size();
 		String[][] data = new String[2][size];
 		
 		int i = 0;
 		while (it.hasNext()) {
 			TokenAnnotation tokenAnnotation = (TokenAnnotation)it.next();
 			data[0][i] = tokenAnnotation.getCoveredText();
 			data[1][i] = tokenAnnotation.getPosTag();
 			i++;
 		}
 		
 		Instance inst = new Instance(data, null, null, null);
 		
 		@SuppressWarnings("unchecked")
 		ArraySequence<String> label = (ArraySequence<String>) crf.label(inst).getTarget();
 		
 		it.moveToFirst();
 		i = 0;
 		while (it.hasNext()) {
 			TokenAnnotation tokenAnnotation = (TokenAnnotation)it.next();
 			String strLabel = label.get(i);
 			tokenAnnotation.setEntityLabel(strLabel);
 			i++;
 		}
 	}
 }
