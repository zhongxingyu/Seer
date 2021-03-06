 package r.nodes.truffle;
 
 import r.*;
 import r.data.*;
 import r.nodes.*;
 
 public abstract class FunctionCall extends BaseR {
 
     final RNode closureExpr;
     final RSymbol[] argsNames; // arguments of the call (not of the function), in order
     final RNode[] argsValues;
 
     public static FunctionCall getFunctionCall(ASTNode ast, RNode closureExpr, RSymbol[] argNames, RNode[] argExprs) {
         for (int i = 0; i < argNames.length; i++) { // FIXME this test is kind of inefficient, part of this job can be done by splitArguments
             if (argNames[i] != null || argExprs[i] == null) { // FIXME this test is a bit too strong, but I need a special node when there are defaults args
                 return getCachedGenericFunctionCall(ast, closureExpr, argNames, argExprs);
             }
         }
         return getSimpleFunctionCall(ast, closureExpr, argNames, argExprs);
     }
 
     // This class is more or less useless since the cached version is always as efficient (or at least one test + affectation for nothing which is meaningless in this case)
     public static FunctionCall getGenericFunctionCall(ASTNode ast, RNode closureExpr, RSymbol[] argNames, RNode[] argExprs) {
         return new FunctionCall(ast, closureExpr, argNames, argExprs) {
 
             @Override
             protected RFrame matchParams(RContext context, RFunction func, RFrame parentFrame) {
                 RFrame fframe = new RFrame(parentFrame, func);
                 RSymbol[] names = new RSymbol[argsValues.length];
 
                 int[] positions = computePositions(context, func, names);
                 displaceArgs(context, parentFrame, fframe, positions, argsValues, names, func.paramValues());
                 return fframe;
             }
         };
     }
 
     public static FunctionCall getCachedGenericFunctionCall(ASTNode ast, RNode closureExpr, RSymbol[] argNames, RNode[] argExprs) {
         return new FunctionCall(ast, closureExpr, argNames, argExprs) {
 
             RFunction lastCall;
             RSymbol[] names;
             int[] positions;
 
             @Override
             protected RFrame matchParams(RContext context, RFunction func, RFrame parentFrame) {
                 RFrame fframe = new RFrame(parentFrame, func);
                 if (func != lastCall) {
                     lastCall = func;
                     names = new RSymbol[argsValues.length];
                     positions = computePositions(context, func, names);
                 }
                 displaceArgs(context, parentFrame, fframe, positions, argsValues, names, func.paramValues());
                 return fframe;
 
             }
         };
     }
 
     public static FunctionCall getSimpleFunctionCall(ASTNode ast, RNode closureExpr, RSymbol[] argNames, RNode[] argExprs) {
         return new FunctionCall(ast, closureExpr, argNames, argExprs) {
 
             @Override
             protected RFrame matchParams(RContext context, RFunction func, RFrame parentFrame) {
                 RFrame fframe = new RFrame(parentFrame, func);
                 displaceArgs(context, parentFrame, fframe, argsValues, func.paramValues());
                 return fframe;
             }
         };
     }
 
     private FunctionCall(ASTNode ast, RNode closureExpr, RSymbol[] argNames, RNode[] argExprs) {
         super(ast);
         this.closureExpr = updateParent(closureExpr);
         this.argsNames = argNames;
         this.argsValues = updateParent(argExprs);
     }
 
     @Override
     public Object execute(RContext context, RFrame frame) {
         RClosure tgt = (RClosure) closureExpr.execute(context, frame);
         RFunction func = tgt.function();
 
         RFrame fframe = matchParams(context, func, tgt.environment());
 
         RNode code = func.body();
         Object res = code.execute(context, fframe);
         return res;
     }
 
     protected abstract RFrame matchParams(RContext context, RFunction func, RFrame parentFrame);
 
     protected int[] computePositions(final RContext context, final RFunction func, RSymbol[] names) {
         RSymbol[] defaultsNames = func.paramNames();
 
         int nbArgs = argsValues.length;
         int nbFormals = defaultsNames.length;
 
         boolean[] used = new boolean[nbFormals]; // Alloc in stack if we are lucky !
 
         boolean has3dots = false;
         int[] positions = new int[has3dots ? (nbArgs + nbFormals) : nbFormals]; // The right size is unknown in presence of ``...'' !
 
         for (int i = 0; i < nbArgs; i++) {
             if (argsNames[i] != null) {
                 for (int j = 0; j < nbFormals; j++) {
                     if (argsNames[i] == defaultsNames[j]) {
                         names[i] = argsNames[i];
                         positions[i] = j;
                         used[j] = true;
                     }
                 }
             }
         }
 
         int nextParam = 0;
         for (int i = 0; i < nbArgs; i++) {
             if (names[i] == null) {
                 while (nextParam < nbFormals && used[nextParam]) {
                     nextParam++;
                 }
                 if (nextParam == nbFormals) {
                     // TODO either error or ``...''
                     context.error(getAST(), "unused argument(s) (" + argsValues[i].getAST() + ")");
                 }
                 if (argsValues[i] != null) {
                     names[i] = defaultsNames[nextParam]; // This is for now useless but needed for ``...''
                     positions[i] = nextParam;
                     used[nextParam] = true;
                 } else {
                     nextParam++;
                 }
             }
         }
 
         int j = nbArgs;
         while (j < nbFormals) {
             if (!used[nextParam]) {
                 positions[j++] = nextParam;
             }
             nextParam++;
         }
 
         return positions;
     }
 
     /**
      * Displace args provided at the good position in the frame.
      *
      * @param context The global context (needed for warning ... and for know for evaluate)
      * @param parentFrame The frame to evaluate exprs (it's the last argument, since with promises, it should be removed or at least changed)
      * @param frame The frame to populate (not the one for evaluate expressions, cf parentFrame)
      * @param positions Where arguments need to be displaced (-1 means ``...'')
      * @param args Arguments provided to this calls
      * @param names Names of extra arguments (...).
      * @param fdefs Defaults values for unprovided parameters. futureparam 3dotsposition where ... as to be put
      */
     private static void displaceArgs(RContext context, RFrame parentFrame, RFrame frame, int[] positions, RNode[] args, RSymbol[] names, RNode[] fdefs) {
         int i;
         int argsGiven = args.length;
         int dfltsArgs = positions.length;
 
         for (i = 0; i < argsGiven; i++) {
             int p = positions[i];
             if (p >= 0) {
                 RNode v = args[i];
                 if (v != null) {
                     frame.writeAt(p, (RAny) args[i].execute(context, parentFrame)); // FIXME this is wrong ! We have to build a promise at this point and not evaluate
                     // FIXME and it's even worst since it's not the good frame at all !
                 } else {
                     v = fdefs[positions[i]];
                     if (v != null) { // TODO insert special value for missing
                         frame.writeAt(positions[i], (RAny) fdefs[positions[i]].execute(context, frame));
                     }
 
                 }
             } else {
                 // TODO add to ``...''
                 // Note that names[i] contains a key if needed
                 context.warning(args[i].getAST(), "need to be put in ``...'', which is NYI");
             }
         }
 
         for (; i < dfltsArgs; i++) { // For now we populate frames with prom/value.
             // I'm not found of this, there should be a way to only create/evaluate when needed.
             // Thus there could be a bug if a default values depends on another
             RNode v = fdefs[positions[i]];
             if (v != null) { // TODO insert special value for missing
                 frame.writeAt(positions[i], (RAny) fdefs[positions[i]].execute(context, frame));
             }
         }
     }
 
     private static void displaceArgs(RContext context, RFrame parentFrame, RFrame frame, RNode[] args, RNode[] fdefs) {
         int i = 0;
         for (; i < args.length; i++) {
            frame.writeAt(i, (RAny) args[i].execute(context, parentFrame)); // FIXME this is wrong ! We have to build a promise at this point and not evaluate
         }
         for (; i < fdefs.length; i++) {
             RNode v = fdefs[i];
             if (v != null) { // TODO insert special value for missing
                 frame.writeAt(i, (RAny) fdefs[i].execute(context, frame));
             }
         }
     }
 }
