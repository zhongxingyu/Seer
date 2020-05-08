 package com.undi.javascheme;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import com.undi.util.Reflector;
 
 public class SchemeEval {
   
   private static final SchemeObject emptyEnvironment = SchemeObject.THE_EMPTY_LIST;
   private SchemeObject globalEnvironment;
   
   public SchemeObject getGlobalEnv(){
     return this.globalEnvironment;
   }
   public boolean isSelfEvaluating(SchemeObject obj){
     return obj.isString() || obj.isBoolean() || obj.isNumber() || obj.isCharacter() || obj.isKeyword();
   }
   
   public void addNativeProc(String symbol, SchemeObject proc){
     defineVariable(SchemeObject.makeSymbol(symbol),
                     proc,
                     this.globalEnvironment);
   }
   
   public void addGlobalVar(String symbol, SchemeObject val){
 	  defineVariable(SchemeObject.makeSymbol(symbol), val, globalEnvironment);
   }
   
   private final SchemeObject globalEnv = SchemeObject.makeNativeProc(new SchemeNatives.NativeProc(){
     @Override
     public SchemeObject call(SchemeObject args) {
       return getGlobalEnv();
     }
   });
 
   public SchemeEval(){
     //load the stdlib by default
     this(true);
   }
   
   public SchemeEval(boolean loadStdlib){
     this.globalEnvironment = this.setupEnvironment();
     //Set up native procedures
     addNativeProc("+", SchemeNatives.add); 
     addNativeProc("-", SchemeNatives.sub); 
     addNativeProc("*", SchemeNatives.mult); 
     addNativeProc("quotient", SchemeNatives.quotient); 
     addNativeProc("/", SchemeNatives.div); 
     addNativeProc("remainder", SchemeNatives.mod);
     addNativeProc("sqrt", SchemeNatives.sqrt); 
     addNativeProc("=", SchemeNatives.numEql); 
     addNativeProc(">", SchemeNatives.greaterThan); 
     addNativeProc("<", SchemeNatives.lessThan); 
     addNativeProc(">=", SchemeNatives.greaterThanEqual); 
     addNativeProc("<=", SchemeNatives.lessThanEqual); 
     addNativeProc("cons", SchemeNatives.cons); 
     addNativeProc("car", SchemeNatives.car); 
     addNativeProc("cdr", SchemeNatives.cdr); 
     addNativeProc("length", SchemeNatives.length);
     addNativeProc("set-car!", SchemeNatives.setCar); 
     addNativeProc("set-cdr!", SchemeNatives.setCdr); 
     addNativeProc("list", SchemeNatives.list); 
     addNativeProc("null?", SchemeNatives.nullp); 
     addNativeProc("boolean?", SchemeNatives.booleanp); 
     addNativeProc("symbol?", SchemeNatives.symbolp); 
     addNativeProc("number?", SchemeNatives.numberp); 
     addNativeProc("character?", SchemeNatives.characterp); 
     addNativeProc("string?", SchemeNatives.stringp); 
     addNativeProc("list?", SchemeNatives.listp); 
     addNativeProc("procedure?", SchemeNatives.procedurep); 
     addNativeProc("number->string", SchemeNatives.numberToString); 
     addNativeProc("string->number", SchemeNatives.stringToNumber); 
     addNativeProc("symbol->string", SchemeNatives.symbolToString); 
     addNativeProc("string->symbol", SchemeNatives.stringToSymbol); 
     addNativeProc("char->number", SchemeNatives.characterToNumber); 
     addNativeProc("number->char", SchemeNatives.numberToCharacter); 
     addNativeProc("eq?", SchemeNatives.eqp);
     addNativeProc("print", SchemeNatives.print);
     addNativeProc("concat", SchemeNatives.concat);
     addNativeProc("read", SchemeNatives.read);
     
     addNativeProc("vector", SchemeNatives.vector);
     addNativeProc("vector?", SchemeNatives.vectorp);
     addNativeProc("make-vector", SchemeNatives.makeVector);
     addNativeProc("vector-ref", SchemeNatives.vectorRef);
     addNativeProc("vector-set!", SchemeNatives.vectorSet);
     addNativeProc("vector-add!", SchemeNatives.vectorAdd);
     addNativeProc("vector-fill!", SchemeNatives.vectorFill);
     addNativeProc("vector-concat", SchemeNatives.vectorConcat);
     addNativeProc("vector-length", SchemeNatives.vectorLength);
     addNativeProc("vector->list", SchemeNatives.vectorToList);
     addNativeProc("list->vector", SchemeNatives.listToVector);
     
     addNativeProc("make-hashmap", SchemeNatives.makeHashMap);
     addNativeProc("hashmap?", SchemeNatives.hashMapp);
     addNativeProc("hashmap-set!", SchemeNatives.hashMapSet);
     addNativeProc("hashmap-get", SchemeNatives.hashMapGet);
     addNativeProc("hashmap-del!", SchemeNatives.hashMapDel);
     addNativeProc("hashmap-keys", SchemeNatives.hashMapKeys);
     addNativeProc("hashmap-vals", SchemeNatives.hashMapVals);
     addNativeProc("hashmap-key?", SchemeNatives.hashMapContainsKey);
     addNativeProc("hashmap-val?", SchemeNatives.hashMapContainsVal);
     addNativeProc("hashmap-merge", SchemeNatives.hashMapMerge);
     
     addNativeProc("new", SchemeNatives.javaNew);
     addNativeProc(".", SchemeNatives.javaDot);
 
     addNativeProc("globalEnv", globalEnv);
     
     //Load the standard lib
     if(loadStdlib){
       System.out.println("Reading stdlib...");
       this.loadStdLib(globalEnvironment);
       System.out.println("Done.");
     }
   }
   
   public boolean isTaggedList(SchemeObject obj, SchemeObject tag){
     if(obj.isPair()){
       SchemeObject car = obj.getCar();
         return (car.isSymbol() && (car == tag));
     }
     return false;
   }
   
   public boolean isQuoted(SchemeObject obj){
     return isTaggedList(obj, SchemeObject.QUOTE_SYMBOL);
   }
   
   public SchemeObject quoteContents(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   
   public boolean isVariable(SchemeObject exp){
     return exp.isSymbol();
   }
   
   public boolean isAssignment(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.SET_SYMBOL);
   }
   public SchemeObject assignmentVariable(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   public SchemeObject assignmentValue(SchemeObject exp){
     return SchemeObject.caddr(exp);
   }
   
   public boolean isDefinition(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.DEFINE_SYMBOL);
   }
   public SchemeObject definitionVariable(SchemeObject exp){
     if(SchemeObject.cadr(exp).isSymbol()){
       return SchemeObject.cadr(exp);
     }else{
       return SchemeObject.caadr(exp);
     }
   }
   
   public SchemeObject definitionValue(SchemeObject exp){
     if(SchemeObject.cadr(exp).isSymbol()){
       return SchemeObject.caddr(exp);
     }else{
       return makeLambda(SchemeObject.cdadr(exp), SchemeObject.cddr(exp));
     }
   }
   
   public SchemeObject evalAssignment(SchemeObject exp, SchemeObject env){
     setVariableValue(assignmentVariable(exp), eval(assignmentValue(exp), env), env);
     return SchemeObject.OK_SYMBOL;
   }
   public SchemeObject evalDefinition(SchemeObject exp, SchemeObject env){
     defineVariable(definitionVariable(exp), eval(definitionValue(exp), env), env);
     return SchemeObject.OK_SYMBOL;
   }
   
   //Lambda Stuff
   public SchemeObject makeLambda(SchemeObject params, SchemeObject body){
     return SchemeObject.cons(SchemeObject.LAMBDA_SYMBOL, SchemeObject.cons(params, body));
   }
   public boolean isLambda(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.LAMBDA_SYMBOL);
   }
   public SchemeObject lambdaParams(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   public SchemeObject lambdaBody(SchemeObject exp){
     return SchemeObject.cddr(exp);
   }
   /**
   * If lambda arguments provided has a variadic argument
    * @param lambdaArgs
    * @return
    */
   public boolean isLambdaVarargs(SchemeObject lambdaParams){
 	  int len = lambdaParams.getListLength();
 	  if(len < 2){
 		  return false;
 	  }
 	  SchemeObject subParams = lambdaParams;
 	  for(int i = 1; i < len - 1; i++){
 		  subParams = subParams.getCdr();
 	  }
 	  if(subParams.getCar().getSymbol().equals("&")){
 		  return true;
 	  }else{
 		  return false;
 	  }
   }
   
   //Begin stuff
   public SchemeObject makeBegin(SchemeObject exp){
     return SchemeObject.cons(SchemeObject.BEGIN_SYMBOL, exp);
   }
   public boolean isBegin(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.BEGIN_SYMBOL);
   }
   public SchemeObject beginActions(SchemeObject exp){
     return exp.getCdr();
   }
   public boolean isLastExp(SchemeObject seq){
     return seq.getCdr().isEmptyList();
   }
   public SchemeObject firstExp(SchemeObject seq){
     return seq.getCar();
   }
   public SchemeObject restExps(SchemeObject seq){
     return seq.getCdr();
   }
   
   //If Stuff
   public SchemeObject makeIf(SchemeObject predicate, SchemeObject ifThen, SchemeObject ifElse){
     return SchemeObject.cons(SchemeObject.IF_SYMBOL,
                         SchemeObject.cons(predicate, 
                             SchemeObject.cons(ifThen, 
                                 SchemeObject.cons(ifElse, SchemeObject.THE_EMPTY_LIST))));
   }
   public boolean isIf(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.IF_SYMBOL);
   }
   public SchemeObject ifPredicate(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   public SchemeObject ifThen(SchemeObject exp){
     return SchemeObject.caddr(exp);
   }
   public SchemeObject ifElse(SchemeObject exp){
     if(SchemeObject.cadddr(exp).isEmptyList()){
       return SchemeObject.FALSE;
     }else{
       return SchemeObject.cadddr(exp);
     }
   }
   
   //And stuff
   public boolean isAnd(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.AND_SYMBOL);
   }
   public SchemeObject andPredicates(SchemeObject exp){
     return exp.getCdr();
   }
   
   //Or stuff
   public boolean isOr(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.OR_SYMBOL);
   }
   public SchemeObject orPredicates(SchemeObject exp){
     return exp.getCdr();
   }
   
   //Cond stuff
   public boolean isCond(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.COND_SYMBOL);
   }
   public SchemeObject condClauses(SchemeObject exp){
     return exp.getCdr();
   }
   public SchemeObject condPredicate(SchemeObject clause){
     return clause.getCar();
   }
   public SchemeObject condActions(SchemeObject clause){
     return clause.getCdr();
   }
   public boolean isCondElseClause(SchemeObject clause){
     return condPredicate(clause) == SchemeObject.ELSE_SYMBOL;
   }
   public SchemeObject sequenceToExp(SchemeObject seq){
     if(seq.isEmptyList()){
       return seq;
     }else if(isLastExp(seq)){
       return firstExp(seq);
     }else{
       return makeBegin(seq);
     }
   }
   public SchemeObject expandClauses(SchemeObject clauses){
     SchemeObject first;
     SchemeObject rest;
     if(clauses.isEmptyList()){
       return SchemeObject.FALSE;
     }else{
       first = clauses.getCar();
       rest = clauses.getCdr();
       if(isCondElseClause(first)){
         if(rest.isEmptyList()){
           return sequenceToExp(condActions(first));
         }else{
 					throw new SchemeException("else clause isn't last in cond ");
         }
       }else{
         return makeIf(condPredicate(first),
                       sequenceToExp(condActions(first)),
                       expandClauses(rest));
       }
     }
   }
   
   public SchemeObject condToIf(SchemeObject exp){
     return expandClauses(condClauses(exp));
   }
   
   //Apply form
   public boolean isApply(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.APPLY_SYMBOL);
   }
   public SchemeObject applyOperation(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   
   public SchemeObject applyOperands(SchemeObject exp, SchemeObject env){
     return prepApplyOperands(SchemeObject.cddr(exp), env);
   }
   
   public SchemeObject prepApplyOperands(SchemeObject exp, SchemeObject env){
     if(exp.isEmptyList()){
       return exp;
     }
     SchemeObject carObject = eval(exp.getCar(), env);
     if(carObject.isPair()){
       return SchemeObject.concatList(carObject, prepApplyOperands(exp.getCdr(), env));
     }else{
       return SchemeObject.cons(carObject, prepApplyOperands(exp.getCdr(), env));
     }
   }
   public SchemeObject makeApply(SchemeObject operation, SchemeObject operands){
     return SchemeObject.cons(operation, operands);
   }
   
   //Application (procedure call)
   public SchemeObject makeApplication(SchemeObject operator, SchemeObject operands){
     return SchemeObject.cons(operator, operands);
   }
   public boolean isApplication(SchemeObject exp){
     return exp.isPair();
   }
   public SchemeObject operator(SchemeObject exp){
     return exp.getCar();
   }
   public SchemeObject operands(SchemeObject exp){
     return exp.getCdr();
   }
   
   public boolean isNoOperands(SchemeObject ops){
     return ops.isEmptyList();
   }
   public SchemeObject firstOperand(SchemeObject ops){
     return ops.getCar();
   }
   public SchemeObject restOperands(SchemeObject ops){
     return ops.getCdr();
   }
   
   public SchemeObject listOfValues(SchemeObject exps, SchemeObject env){
     if(isNoOperands(exps)){
       return SchemeObject.THE_EMPTY_LIST;
     }else{
       return SchemeObject.cons(eval(firstOperand(exps), env),
                                 listOfValues(restOperands(exps), env));
     }
   }
   
   //Let stuff
   public boolean isLet(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.LET_SYMBOL);
   }
   //(let <((a 5))> ...)
   public SchemeObject letBindings(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   //(let ((a 5)) <...>)
   public SchemeObject letBody(SchemeObject exp){
     return SchemeObject.cddr(exp);
   }
   //(let ((<a> 5)) ...)
   public SchemeObject bindingParameter(SchemeObject binding){
     return SchemeObject.car(binding);
   }
   public SchemeObject bindingArgument(SchemeObject binding){
     return SchemeObject.cadr(binding);
   }
   /**
    * Generate a list of all the bindings from this set of let bindings
    * @param bindings
    * @return
    */
   public SchemeObject bindingsParameters(SchemeObject bindings){
     return bindings.isEmptyList()?
           SchemeObject.THE_EMPTY_LIST :
             SchemeObject.cons(bindingParameter(bindings.getCar()),
                   bindingsParameters(bindings.getCdr()));
   }
   public SchemeObject bindingsArguments(SchemeObject bindings){
     return bindings.isEmptyList()?
           SchemeObject.THE_EMPTY_LIST :
             SchemeObject.cons(bindingArgument(bindings.getCar()),
                   bindingsArguments(bindings.getCdr()));
   }
   
   public SchemeObject letParameters(SchemeObject exp){
     return bindingsParameters(letBindings(exp));
   }
   public SchemeObject letArguments(SchemeObject exp){
     return bindingsArguments(letBindings(exp));
   }
   
   public SchemeObject letToApplication(SchemeObject exp){
     return makeApplication(
               makeLambda(letParameters(exp), letBody(exp)),
               letArguments(exp));
   }
   
   //Load
   public boolean isLoad(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.LOAD_SYMBOL);
   }
   /**
    * 
    * @param exp - String filename to load
    * @param env - Environment to load into
    * @return
    */
   public SchemeObject loadFile(SchemeObject exp, SchemeObject env){
       String filename = new String(SchemeObject.cadr(exp).getString());
       FileInputStream fin = null;
       try {
         fin = new FileInputStream(filename);
       } catch (FileNotFoundException e) {
 				throw new SchemeException("File: " + filename + " not found for load");
       }
       
       loadStream(fin, env);
       
       try {
         fin.close();
       } catch (IOException e) {
         System.err.println("Unable to close file: " + filename);
       }
       
       return SchemeObject.OK_SYMBOL;
   }
   
   public SchemeObject loadStdLib(SchemeObject env){
     InputStream stdLibStream = SchemeEval.class.getResourceAsStream("/res/stdlib.scm");
     loadStream(stdLibStream, env);
     try{
       stdLibStream.close();
     }catch(Exception ex){
       ex.printStackTrace();
     }
     return SchemeObject.OK_SYMBOL;
   }
   
   /**
    * Loads code and evaluates it from a stream
    * @param in - stream to read from
    * @param env - environment to load into
    * @return
    */
   public SchemeObject loadStream(InputStream in, SchemeObject env){
     SchemeReader reader = new SchemeReader(in);
     SchemeObject obj = reader.read();
     while(obj != null){
       eval(obj, env);
       obj = reader.read();
     }
     return SchemeObject.OK_SYMBOL;
   }
   
   //While loops
   public boolean isWhile(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.WHILE_SYMBOL);
   }
   public SchemeObject whilePredicate(SchemeObject exp){
     return SchemeObject.cadr(exp);
   }
   
   public SchemeObject whileBody(SchemeObject exp){
     return SchemeObject.cddr(exp);
   }
   
   public SchemeObject doWhile(SchemeObject exp, SchemeObject env){
     SchemeObject predicate = whilePredicate(exp);
     SchemeObject body = whileBody(exp);
     
     SchemeObject result = SchemeObject.THE_EMPTY_LIST;
     while(SchemeObject.isTrue(eval(predicate, env))){
       SchemeObject exps = body;
       while(!isLastExp(exps)){
         eval(firstExp(exps), env);
         exps = exps.getCdr();
       }
       result = eval(firstExp(exps), env);
     }
     
     return result;
   }
   
   //Eval
   public boolean isEval(SchemeObject exp){
     return isTaggedList(exp, SchemeObject.EVAL_SYMBOL);
   }
   
   public char[] evalString(SchemeObject exp){
     return SchemeObject.cadr(exp).getString();
   }
   
   public SchemeObject doEval(SchemeObject exp, SchemeObject env){
     String stringToEval = new String(evalString(exp));
     SchemeReader reader = SchemeReader.getStringReader(stringToEval);
     SchemeObject obj = reader.read();
     SchemeObject retObj = null;
     while(obj != null){
       retObj = eval(obj, env);
       obj = reader.read();
     }
     reader.closeStream();
     if(retObj == null){
       return SchemeObject.OK_SYMBOL;
     }else{
       return retObj;
     }
   }
   
   //Environment stuff
   public SchemeObject enclosingEnvironment(SchemeObject env){
     return SchemeObject.cdr(env);
   }
   public SchemeObject firstFrame(SchemeObject env){
     return SchemeObject.car(env);
   }
   public SchemeObject makeFrame(SchemeObject vars, SchemeObject vals){
     //return SchemeObject.cons(vars, vals);
     SchemeObject bindings = SchemeObject.THE_EMPTY_LIST;
     while(!vars.isEmptyList()){
       bindings = SchemeObject.cons(vars.getCar(), 
             SchemeObject.cons(vals.getCar(), bindings));
       vars = vars.getCdr();
       vals = vals.getCdr();
     }
     return SchemeObject.makeHashMap(bindings);
   }
   
   public SchemeObject frameVars(SchemeObject frame){
     return SchemeObject.car(frame);
   }
   public SchemeObject frameVals(SchemeObject frame){
     return SchemeObject.cdr(frame);
   }
   
   public void addBindingToFrame(SchemeObject var, SchemeObject val, SchemeObject frame){
     //frame.setCar(SchemeObject.cons(var, frame.getCar()));
     //frame.setCdr(SchemeObject.cons(val, frame.getCdr()));
     frame.getHashMap().put(var, val);
   }
   
   public SchemeObject extendEnvironment(SchemeObject vars, SchemeObject vals,
                                   SchemeObject base_env){
     return SchemeObject.cons(makeFrame(vars, vals), base_env);
   }
   
   public SchemeObject lookupVariableValue(SchemeObject var, SchemeObject env){
     //Backup of env so we can look through variables again on java interop
     SchemeObject originalEnv = env;
     HashMap<SchemeObject, SchemeObject> frameMap;
     SchemeObject result = null;
     while(!env.isEmptyList()){
       frameMap = firstFrame(env).getHashMap();
       /*if(frameMap.containsKey(var)){
         return frameMap.get(var);
       }*/
       result = frameMap.get(var);
       if(result != null){
         return result;
       }
       env = enclosingEnvironment(env);
     }
     //Check if this is a java-defined name
     //Check for instance/static fields
     if(var.getSymbol().contains("/")){
       String[] parts = var.getSymbol().split("/");
       if(parts.length == 2){
         String varName = parts[0];
         String fieldName = parts[1];
         //Check if varName is a variable
         try{
           SchemeObject javaObj = lookupVariableValue(SchemeObject.makeSymbol(varName), originalEnv);
           return SchemeObject.makeJavaObj(Reflector.getInstanceField(javaObj.getJavaObj(), fieldName));
         }catch(IllegalArgumentException ex){
           //Not a variable, try for static field
           try{
             return SchemeObject.makeJavaObj(Reflector.getStaticField(varName, fieldName));
           }catch(Exception e){
             //Not a field, try for a static method 
             return SchemeObject.makeJavaStaticMethod(varName, fieldName);
           }
         }
       }
     }
     //Check for classes/methods
     if(var.getSymbol().startsWith(".") && var.getSymbol().length() > 1){
       System.out.println("Java method/member: " + var.getSymbol().substring(1));
       return SchemeObject.makeJavaMethod(var.getSymbol().substring(1));
     }else if(var.getSymbol().endsWith(".")){
       String className = var.getSymbol().substring(0, var.getSymbol().length() - 1);
       System.out.println("Java Constructor: " + className);
       return SchemeObject.makeJavaConstructor(className);
     }
     try{
     	Class cls = Reflector.classFromName(var.getSymbol());
     	if(cls != null){
     		return SchemeObject.makeJavaObj(cls);
     	}
     }catch(Exception e){ }
     throw new IllegalArgumentException("Unbound Variable: " + var.getSymbol());
   }
   
   public void setVariableValue(SchemeObject var, SchemeObject val, SchemeObject env){
     HashMap<SchemeObject, SchemeObject> frameMap;
     while(!env.isEmptyList()){
       frameMap = firstFrame(env).getHashMap();
       if(frameMap.containsKey(var)){
         frameMap.put(var, val);
         return;
       }
       env = enclosingEnvironment(env);
     }
 		throw new SchemeException("Unbound Variable - " + var.getSymbol());
   }
   
   public void defineVariable(SchemeObject var, SchemeObject val, SchemeObject env){
     SchemeObject frame;
     frame = firstFrame(env);
     addBindingToFrame(var, val, frame);
   }
   
   public SchemeObject setupEnvironment(){
     SchemeObject initialEnv;
     initialEnv = extendEnvironment(SchemeObject.THE_EMPTY_LIST,
                                       SchemeObject.THE_EMPTY_LIST, 
                                       emptyEnvironment);
     
     return initialEnv;
   }
   
   public SchemeObject eval(SchemeObject exp, SchemeObject env){
     SchemeObject procedure;
     SchemeObject arguments;
     TAILCALL:
       while(true){
         if(isSelfEvaluating(exp)){
           return exp;
         }else if(isQuoted(exp)){
           return quoteContents(exp);
         }else if(isDefinition(exp)){
           return evalDefinition(exp, env);
         }else if(isAssignment(exp)){
           return evalAssignment(exp, env);
         }else if(isVariable(exp)){
           try{
             return lookupVariableValue(exp, env);
           }catch(IllegalArgumentException ex){
 						throw new SchemeException("Unbound Variable: " + exp.getCar().getSymbol());
           }
         }else if(isEval(exp)){
           return doEval(exp, env);
         }else if(isIf(exp)){
           exp = SchemeObject.isTrue(eval(ifPredicate(exp), env))?
               ifThen(exp) :
                 ifElse(exp);
               //equiv: goto TAILCALL
               continue TAILCALL;
         }else if(isAnd(exp)){
           exp = andPredicates(exp);
           while(!isLastExp(exp)){
             if(SchemeObject.isFalse(eval(firstExp(exp), env))){
               exp = SchemeObject.FALSE;
               continue TAILCALL;
             }
             exp = restExps(exp);
           }
           exp = firstExp(exp);
           continue TAILCALL;
         }else if(isOr(exp)){
           exp = andPredicates(exp);
           while(!isLastExp(exp)){
             if(SchemeObject.isTrue(eval(firstExp(exp), env))){
               exp = SchemeObject.TRUE;
               continue TAILCALL;
             }
             exp = restExps(exp);
           }
           exp = firstExp(exp);
           continue TAILCALL;
         }else if(isLambda(exp)){
           return SchemeObject.makeCompoundProc(lambdaParams(exp), lambdaBody(exp), env);
         }else if(isWhile(exp)){
           return doWhile(exp, env);
         }else if(isBegin(exp)){
           exp = beginActions(exp);
           while(!isLastExp(exp)){
             eval(firstExp(exp), env);
             exp = restExps(exp);
           }
           exp = firstExp(exp);
           continue TAILCALL;
         }else if(isLoad(exp)){
           return loadFile(exp, env);
         }else if(isCond(exp)){
           exp = condToIf(exp);
           continue TAILCALL;
         }else if(isLet(exp)){
           exp = letToApplication(exp);
           continue TAILCALL;
         }else if(isApply(exp)){
           exp = makeApply(applyOperation(exp), applyOperands(exp, env));
           continue TAILCALL;
         }else if(isApplication(exp)){
           procedure = eval(operator(exp), env);
           arguments = listOfValues(operands(exp), env);
           if(procedure.isNativeProc()){
             int arity = procedure.getNativeProc().getArity();
             if(arity != -1 && arity != arguments.getListLength()){
 							throw new SchemeException("Native Method not found for " + exp.getCar().getSymbol() + " with "
 																				+ arguments.getListLength() + " params");
             }
 						try{
 							return procedure.getNativeProc().call(arguments);
 						}catch(Exception e){
 								e.printStackTrace();
 								throw new SchemeException("Error in native proc", e);
 						}
           }else if(procedure.isCompoundProc()){
             SchemeObject paramsList = procedure.getCompoundProcParams();
             if(isLambdaVarargs(paramsList)){
             	SchemeObject subParams = paramsList;
            	SchemeObject newParamsBuilder = null;
             	SchemeObject subArgs = arguments;
             	int numRegArgs = subParams.getListLength() - 2;
             	SchemeObject varArgName;
             	for(int i = 0; i < numRegArgs; i++){
            		if(newParamsBuilder == null){
            			newParamsBuilder = SchemeObject.makePair(subParams.getCar(), SchemeObject.THE_EMPTY_LIST);
            		}else{
            			newParamsBuilder = SchemeObject.concatList(newParamsBuilder, 
            					SchemeObject.makePair(subParams.getCar(), SchemeObject.THE_EMPTY_LIST));
            		}
             		subParams = subParams.getCdr();
             		subArgs = subArgs.getCdr();
             	}
             	varArgName = subParams.getCdr().getCar();
            	paramsList = SchemeObject.concatList(newParamsBuilder, 
            			SchemeObject.makePair(varArgName, SchemeObject.THE_EMPTY_LIST));
             	subArgs.setCar(SchemeObject.makePair(subArgs.getCar(), subArgs.getCdr()));
             	subArgs.setCdr(SchemeObject.THE_EMPTY_LIST);
             }else{
             	if(paramsList.getListLength() != arguments.getListLength()){
             		throw new SchemeException("Method not found for " + exp.getCar().getSymbol() + " with "
             				+ arguments.getListLength() + " params");
             	}
             }
             env = extendEnvironment(paramsList,
             		arguments,
             		procedure.getCompoundProcEnv());
             exp = makeBegin(procedure.getCompoundProcBody());
             continue TAILCALL;
           }else if(procedure.isJavaMethod()){
             return procedure.callJavaMethod(arguments);
           }else if(procedure.isJavaStaticMethod()){
             return procedure.callJavaStaticMethod(arguments);
           }else if(procedure.isJavaConstructor()){
             return SchemeObject.makeJavaObj(procedure.callJavaConstructor(arguments));
           }else{
 						throw new SchemeException("Unsupported procedure type: " + exp);
           }
         }else{
 					throw new SchemeException("Unsupported expression type: " + exp);
         }
       }
   }
 }
