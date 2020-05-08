 import java.util.List;
 
 
 /**
  * @author KarelPetranek
  * Evaluation of one student answers.
  */
 public class Evaluation {
 	private Answer answerForQuestion(Question q, List<Answer> answers)  {
 		for (Answer a : answers)  {
 			if (q.getId() == a.getQuestionId())
 				return a;
 		}
 		
 		System.out.println("Warning: could not find student answer that corresponds to question " + q.getId());
 		return null;
 	}
 	
 	/**
 	 * Accumulator - the accumulator contains a triple for each main model. Each field
 	 * of the triple corresponds to one submodel.
 	 */
 	int[] accumulator;
 	
 	/**
 	 * Creates a new evaluation
 	 */
 	public Evaluation()  {
 		accumulator = new int[(Model.MainModel.values().length - 1) * (Model.SubModel.values().length - 1)];
 		
 	}
 	
	private void addModelToAccumulator(Model m, int[] accum) {
 		if (m.getMain() == Model.MainModel.NoModel)
 			return;
 		
 		int mainIndex = m.getMain().ordinal() - 1; // -1 - first item is NoModel
 		
 		// No specific submodel - increase all submodels
 		if (m.getSub() == Model.SubModel.NoSubmodel)  {
 			for (int i = 0; i < Model.SubModel.values().length - 1; i++)  {
				accum[mainIndex + i]++;
 			}
 		} else {
 			// Increase only specific submodel
 			int subIndex = m.getSub().ordinal() - 1;
			accum[mainIndex + subIndex]++; 
 		}
 	}
 	
	private void addQuestionAccumulator(int[] questionAccum)  {
		for (int i = 0; i < questionAccum.length; i++)  {
			if (questionAccum[i] > 0)
				accumulator[i]++;
		}
	}
 	
 	/**
 	 * Evaluates one student answers
 	 * @param questions List of questions in the test
 	 * @param answers Corresponding student answers
 	 * @return Number of consistent answers. See Dehnadi's remarks for instructions on how to judge this number.
 	 * If you use his exact test, 8 and more means the student is consistent, 7 and less means inconsistency.
 	 */
 	public int evaluate(List<Question> questions, List<Answer> answers)  {
 		// Accumulate models that correspond to student answers
 		for (Question q : questions)  {
 			Answer a = answerForQuestion(q, answers);
 			if (a == null)
 				continue;
 			
 			List<Model> models = q.modelsForAnswer(a);
			int[] questionAccumulator = new int[accumulator.length];
 			for (Model m : models)  {
				addModelToAccumulator(m, questionAccumulator);
 			}
			addQuestionAccumulator(questionAccumulator);
 		}
 
 		// Find maximum consistency
 		int max = 0;
 		for (int i = 0; i < accumulator.length; i++)
 			if (accumulator[i] > max)
 				max = accumulator[i];
 		return max;
 	}
 
 
 
 }
