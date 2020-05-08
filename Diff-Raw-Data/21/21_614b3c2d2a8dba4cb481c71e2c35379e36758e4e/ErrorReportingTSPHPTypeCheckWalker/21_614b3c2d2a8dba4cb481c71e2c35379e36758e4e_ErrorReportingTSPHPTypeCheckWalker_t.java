 /*
  * Copyright 2012 Robert Stoll <rstoll@tutteli.ch>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package ch.tutteli.tsphp.typechecker.antlr;
 
 import ch.tutteli.tsphp.common.IErrorLogger;
 import ch.tutteli.tsphp.common.IErrorReporter;
 import ch.tutteli.tsphp.common.exceptions.TSPHPException;
 import ch.tutteli.tsphp.typechecker.ITypeCheckerController;
 import java.util.ArrayDeque;
 import java.util.Collection;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.TreeNodeStream;
 
 /**
  *
  * @author Robert Stoll <rstoll@tutteli.ch>
  */
 public class ErrorReportingTSPHPTypeCheckWalker extends TSPHPTypeCheckWalker implements IErrorReporter
 {
 
     private Collection<IErrorLogger> errorLoggers = new ArrayDeque<>();
     private boolean hasFoundError;
 
     public ErrorReportingTSPHPTypeCheckWalker(TreeNodeStream input, ITypeCheckerController theController) {
         super(input, theController);
     }
 
     @Override
     public boolean hasFoundError() {
         return hasFoundError;
     }
 
     @Override
     public void reportError(RecognitionException exception) {
         hasFoundError = true;
         for (IErrorLogger logger : errorLoggers) {
            logger.log(new TSPHPException("Line " + exception.line + "|" + exception.charPositionInLine
                    + " type checker exception occured. Unexpected token: " + exception.token.getText(), exception));
         }
     }
 
     @Override
     public void addErrorLogger(IErrorLogger errorLogger) {
         errorLoggers.add(errorLogger);
     }
 }
