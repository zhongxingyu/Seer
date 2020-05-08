 package lambda;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import lambda.ast.IRedex;
 import lambda.ast.Lambda;
 import lambda.ast.RedexFinder;
 
 public class LambdaInterpreter
 {
 	private LinkedList<Lambda> steps = new LinkedList<Lambda>();
 	private Lambda sourceLambda;
 	private Lambda lambda;
 	private boolean isCyclic;
 
 	public LambdaInterpreter(Lambda sourceLambda)
 	{
 		this.sourceLambda = sourceLambda;
 		initialize();
 	}
 
 	public void initialize()
 	{
 		lambda = sourceLambda;
 		isCyclic = false;
 		steps.clear();
 		push();
 	}
 
 	public boolean step(Environment env)
 	{
 		return step(env, null);
 	}
 
 	public boolean step(Environment env, IRedex redex)
 	{
 		if (!isCyclic)
 		{
 			Reducer.Result ret = Reducer.reduce(lambda, env, redex);
 			isCyclic = AlphaComparator.alphaEquiv(lambda, ret.lambda);
 			lambda = ret.lambda;
 			push();
 			return ret.reduced;
 		}
 		return false;
 	}
 
 	public void revert()
 	{
 		pop();
 	}
 
 	public boolean isRevertable()
 	{
 		return !steps.isEmpty();
 	}
 
 	public int getStep()
 	{
		return steps.size() - 1;
 	}
 
 	public boolean isNormal()
 	{
 		boolean etaEnabled = Environment.getEnvironment().getBoolean(Environment.KEY_ETA_REDUCTION);
 		return RedexFinder.isNormalForm(lambda, etaEnabled);
 	}
 
 	public boolean isCyclic()
 	{
 		return !isNormal() && isCyclic;
 	}
 
 	public boolean isTerminated()
 	{
 		return isNormal() || isCyclic();
 	}
 
 	public Lambda getLambda()
 	{
 		return lambda;
 	}
 
 	public List<Lambda> getSteps()
 	{
 		return Collections.unmodifiableList(steps);
 	}
 
 	private void push()
 	{
 		steps.push(lambda);
 	}
 
 	private void pop()
 	{
 		if (!steps.isEmpty())
 		{
 			lambda = steps.pop();
 		}
 	}
 }
