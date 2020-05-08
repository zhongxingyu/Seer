 package org.jsmiparser.phase.xref;
 
 import org.jsmiparser.util.problem.annotations.ProblemMethod;
 import org.jsmiparser.util.problem.annotations.ProblemSeverity;
 import org.jsmiparser.util.token.IdToken;
 import org.jsmiparser.util.token.Token;
 import org.jsmiparser.util.location.Location;
 import org.jsmiparser.smi.SmiSymbol;
 
 /*
 * Copyright 2006 Davy Verstappen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 public interface XRefProblemReporter {
 
     @ProblemMethod(message = "Cannot find module %s")
     void reportCannotFindModule(IdToken moduleToken);
 
     @ProblemMethod(message = "Cannot find imported symbol %s in module %s")
     void reportCannotFindImportedSymbol(IdToken idToken, IdToken moduleToken);
 
     @ProblemMethod(message = "Cannot find symbol %s")
     void reportCannotFindSymbol(IdToken idToken);
 
     @ProblemMethod(message = "Found symbol %s but expected a %s instead of %s")
     void reportFoundSymbolButWrongType(IdToken idToken, Class<? extends SmiSymbol> expectedClass, Class<? extends SmiSymbol> actualClass);
 
     @ProblemMethod(message = "DEFVAL with bit values but variable doesn't have a BITS type.")
     void reportBitsValueWithoutBitsType(Location location);
 
     @ProblemMethod(message = "Cannot find bit field %s")
     void reportCannotFindBitField(IdToken idToken);
 
     @ProblemMethod(message = "Cannot find enum constant %s")
     void reportCannotFindEnumConstant(IdToken idToken);
 
     @ProblemMethod(message = "Invalid default value %s", severity = ProblemSeverity.WARNING)    
     void reportInvalidDefaultValue(IdToken idToken);
 
     @ProblemMethod(message = "An object identifier default value must be expressed as a single ASN.1 identifier, and not as a collection of sub-identifiers: %s", ref="RFC1902, 7.9")
     void reportOidDefaultValueMustBeSingleIdentifier(Token token);
 
     @ProblemMethod(message = "%s is not a valid value for the ACCESS(SMIv1) field. You are probably mixing v1/v2 constructions.", severity = ProblemSeverity.WARNING)
     void reportInvalidAccess(IdToken idToken);
 
     @ProblemMethod(message = "%s is not a valid value for the MAX-ACCESS(SMIv2) field. You are probably mixing v1/v2 constructions.", severity = ProblemSeverity.WARNING)
     void reportInvalidMaxAccess(IdToken idToken);
 
 }
