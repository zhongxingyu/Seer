 package pl.edu.agh.se;
 
 import javax.swing.JOptionPane;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderError;
 import org.drools.builder.KnowledgeBuilderErrors;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.ResourceFactory;
 import org.drools.logger.KnowledgeRuntimeLogger;
 import org.drools.logger.KnowledgeRuntimeLoggerFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 
 public class Diving {
 
 	public static final void main(String[] args) {
 		try {
 			KnowledgeBase kbase = readKnowledgeBase();
 			StatefulKnowledgeSession ksession = kbase
 					.newStatefulKnowledgeSession();
 			KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory
 					.newFileLogger(ksession, "test");
 			ksession.fireAllRules();
 			System.out.println("end session");
 			logger.close();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 
 	private static KnowledgeBase readKnowledgeBase() throws Exception {
 		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
 				.newKnowledgeBuilder();
 		kbuilder.add(ResourceFactory.newClassPathResource("Diving.drl"),
 				ResourceType.DRL);
 		KnowledgeBuilderErrors errors = kbuilder.getErrors();
 		if (errors.size() > 0) {
 			for (KnowledgeBuilderError error : errors) {
 				System.err.println(error);
 			}
 			throw new IllegalArgumentException("Could not parse knowledge.");
 		}
 		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
 		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
 		return kbase;
 	}
 
 	public static int askQuestion(String question, String[] answers) {
 		String ret = (String) JOptionPane.showInputDialog(null, question,
 				"Diving system", JOptionPane.QUESTION_MESSAGE, null, answers,
 				answers[0]);
 		for (int i = 0; i < answers.length; ++i) {
 			if (answers[i].equals(ret)) {
 				return i;
 			}
 		}
 		// should never ever happen
 		return -1;
 	}
 
 	public static void showResult(String result) {
 		JOptionPane.showMessageDialog(null, result, "The result",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 }
