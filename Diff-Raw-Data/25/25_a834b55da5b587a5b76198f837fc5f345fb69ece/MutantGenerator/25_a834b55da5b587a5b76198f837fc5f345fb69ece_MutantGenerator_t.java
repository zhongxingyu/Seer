 package com.mutation.transform.asm;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mutation.EMutationOperator;
 import com.mutation.IMutantGenerator;
 import com.mutation.Mutant;
 import com.mutation.events.IEventListener;
 import com.mutation.events.MutantsGenerated;
 
 public class MutantGenerator implements IMutantGenerator {
 
 	private File originalClassPath;
 
 	public List<Mutant> generateMutants(String classUnderTest, byte[] originalClassByteCode,
 			EMutationOperator operator, IEventListener listener) {
		//TODO: remove loading class from file system but use the passed bytecode directly
 
 		String path = originalClassPath.getAbsolutePath() + "/" + classUnderTest.replace('.', '/') + ".class";
 		File originalClassFile = new File(path);
 
 		List<Mutant> result = new ArrayList<Mutant>();
 		if (operator.equals(EMutationOperator.ROR)) {
 			RORMutantCreator creator = new RORMutantCreator();
 			result.addAll(creator.generateMutants(classUnderTest, originalClassFile, listener));
 		}
 
 		listener.notifyEvent(new MutantsGenerated(result, classUnderTest, operator));
 
 		return result;
 	}
 
 	public void setOriginalClassPath(File originalClassPath) {
 		this.originalClassPath = originalClassPath;
 	}
 
 }
