 package backend.computations.operations;
 
 import backend.blocks.*;
 import backend.blocks.Countable.DisplayType;
 import backend.computations.infrastructure.Computable;
 import backend.computations.infrastructure.Solution;
 import backend.computations.infrastructure.Step;
 
 import java.util.*;
 
/** Matrix Column Space Operation
  *
  * @author dzee
  */
 public class M_Columnspace extends Computable
 {
   private Solution _solution;
 
 	@Override
 	public Solution getSolution()
 	{
 		return _solution;
 	}
 
 	/**Returns the column space basis of a matrix
 	 *
 	 *@param matrix the matrix*/
 	public M_Columnspace(Matrix matrix) throws Exception
 	{
 		List<Countable> matrixList = new ArrayList<>();
 		matrixList.add(matrix);
 		//TODO
 		DisplayType answerDisplayType = null;//resolveDisplayType(matrixList); // choose DisplayType to use
 
 		Double[][] values = matrix.getValues();
 		for (int i = 0; i < values.length; i++)
 		{
 			for (int j = 0; j < values[0].length; j++)
 			{
 				if (values[i][j] == null){
 					throw new IllegalArgumentException("ERROR: Each matrix index must contain a non-null entry");
 				}
 			}
 		}
 
 		List<Step> steps = new ArrayList<Step>();
 
 		//TODO
 		/**the reduced matrix*/
 //		Solution refsol=(new M_RowReduce(matrix)).getSolution();
 //		Matrix ref=(Matrix)(refsol.getAnswer());
 //		Double[][] refv=ref.getValues();
 //		steps.addAll(refsol.getSteps());
 
 		//stub
 		Double[][] refv=values;
 		List<Integer> isPivot=new ArrayList<>();
 		//first zero row
 		int fzr=0;
 		for (int i=0;i<refv.length;i++)
 		{
 			if (fzr>=refv[0].length)//beyond the last row
 				break;
 			if (refv[i][fzr]!=0)
 			{
 				isPivot.add(i);
 				//get to bottom of non-zero
 				while (fzr<refv[0].length && refv[i][fzr]!=0)
 				{
 					fzr++;
 				}
 			}
 		}
 
 		//the values of the pivot columns
 		Double[][] pivots=new Double[isPivot.size()][values[0].length];
 		for (int i=0;i<isPivot.size();i++)
 		{
 			for (int j=0;j<values[0].length;j++)
 			{
 				pivots[i][j]=values[isPivot.get(i)][j];
 			}
 		}
 
 		Matrix answer=new Matrix(answerDisplayType,pivots);
 		steps.add(new Step(answer));
 
 		List<Countable> inputs = new ArrayList<>();
 		inputs.add(matrix);
 
 		_solution = new Solution(Op.COLUMNSPACE, inputs, answer, steps);
 	}
 
 }
