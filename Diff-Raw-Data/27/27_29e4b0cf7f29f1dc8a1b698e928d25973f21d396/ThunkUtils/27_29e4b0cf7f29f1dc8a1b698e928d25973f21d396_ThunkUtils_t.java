 package org.fuelsyourcyb.exathunk;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class ThunkUtils {
     // Do two thunks have the same result?
     public static <Value> boolean statelessEquals(
             Thunk<Value> a, Thunk<Value> b) {
 	if (a.isFinished()) {
 	    return b.isFinished() &&
 		a.getResult().equals(b.getResult());
 	} else {
 	    return !b.isFinished();
 	}
     }
     
     // Do two thunks have the same result AND the same
     // internal state?
     public static <Value> boolean statefulEquals(
             Thunk<Value> a, Thunk<Value> b) {
 	if (!a.getState().equals(b.getState())) return false;
 	return statelessEquals(a, b);
     }
 
     public static <FuncId, Value> boolean isTerminalInnerNode(NTree<FuncId, Value> parseTree) {
 	if (parseTree.isBranch()) {
 	    for (NTree<FuncId, Value> child : parseTree.getChildren()) {
 		if (!child.isLeaf()) return false;
 	    }
 	    return true;
 	}
 	return false;
     }
 
     public static <FuncId, Value> boolean isEvaluatable(NTree<FuncId, Thunk<Value>> thunkTree) {
 	if (thunkTree.isBranch()) {
 	    for (NTree<FuncId, Thunk<Value>> child : thunkTree.getChildren()) {
 		if (!child.isLeaf()) return false;
 		if (!child.getValue().isFinished()) return false;
 	    }
 	    return true;
 	}
 	return false;
     }
 
     public static <FuncId, Value> NTree<FuncId, Thunk<Value>> makeThunkTree(
             NTree<FuncId, Value> parseTree,
 	    ThunkFactory<FuncId, Value> thunkFactory) {
 	if (parseTree.isEmpty()) {
 	    throw new ThunkEvaluationException("Empty computation");
 	} else if (parseTree.isLeaf()) {
 	    State initState = thunkFactory.getStateFactory().makeInitialState();
 	    return new NTree<FuncId, Thunk<Value>>(new PresentThunk<Value>(initState, parseTree.getValue()));
 	} else if (ThunkUtils.isTerminalInnerNode(parseTree)) {
 	    List<Value> params = new ArrayList<Value>(parseTree.getChildren().size());
 	    for (NTree<FuncId, Value> child : parseTree.getChildren()) {
 		params.add(child.getValue());
 	    }
 	    return new NTree<FuncId, Thunk<Value>>(thunkFactory.makeThunk(parseTree.getLabel(), params));
 	} else {
 	    List<NTree<FuncId, Thunk<Value>>> params = new ArrayList<NTree<FuncId, Thunk<Value>>>(parseTree.getChildren().size());
 	    for (NTree<FuncId, Value> child : parseTree.getChildren()) {
 		params.add(makeThunkTree(child, thunkFactory));
 	    }
 	    return new NTree<FuncId, Thunk<Value>>(parseTree.getLabel(), params);
 	}
     }
 
     public static class Evaluator<FuncId, Value> implements ParametricMutator<Either<FuncId, Thunk<Value>>, NTree<FuncId, Thunk<Value>>> {
	private final ThunkFactory<FuncId, Value> factory;

	public Evaluator(ThunkFactory<FuncId, Value> factory) {
	    this.factory = factory;
	}

 	public void mutate(Either<FuncId, Thunk<Value>> param, NTree<FuncId, Thunk<Value>> mutee) {
	    if (param.isLeft()) {
		if (ThunkUtils.isEvaluatable(mutee)) {
		    List<Value> params = new ArrayList<Value>(mutee.getChildren().size());
		    for (NTree<FuncId, Thunk<Value>> child : mutee.getChildren()) {
			Thunk<Value> thunk = child.getValue();
			params.add(thunk.getResult());
		    }
		    mutee.setValue(factory.makeThunk(param.left(), params));
		}
	    }
 	}
     }
 
 }
