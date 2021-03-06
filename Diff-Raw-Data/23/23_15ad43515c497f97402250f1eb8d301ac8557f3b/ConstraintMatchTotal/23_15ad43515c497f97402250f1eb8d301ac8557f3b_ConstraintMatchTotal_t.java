 /*
  * Copyright 2013 JBoss Inc
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
 
 package org.optaplanner.core.api.score.constraint;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Set;
 
import com.google.common.collect.Lists;
 import org.apache.commons.lang.builder.CompareToBuilder;
 import org.kie.api.runtime.rule.RuleContext;
 
 public abstract class ConstraintMatchTotal implements Serializable, Comparable<ConstraintMatchTotal> {
 
     protected final String constraintPackage;
     protected final String constraintName;
     protected final int scoreLevel;
 
     protected ConstraintMatchTotal(String constraintPackage, String constraintName, int scoreLevel) {
         this.constraintPackage = constraintPackage;
         this.constraintName = constraintName;
         this.scoreLevel = scoreLevel;
     }
 
     public String getConstraintPackage() {
         return constraintPackage;
     }
 
     public String getConstraintName() {
         return constraintName;
     }
 
     public int getScoreLevel() {
         return scoreLevel;
     }
 
     public abstract Set<? extends ConstraintMatch> getConstraintMatchSet();
 
     public abstract Number getWeightTotalAsNumber();
 
     // ************************************************************************
     // Worker methods
     // ************************************************************************
 
     protected List<Object> extractJustificationList(RuleContext kcontext) {
         List<Object> droolsMatchObjects = kcontext.getMatch().getObjects();
         // Drools always returns the rule matches in reverse order
        return Lists.reverse(droolsMatchObjects);
     }
 
     public String getIdentificationString() {
         return constraintPackage + "/" + constraintName + "/level" + scoreLevel;
     }
 
     @Override
     public int compareTo(ConstraintMatchTotal other) {
         return new CompareToBuilder()
                 .append(getConstraintPackage(), other.getConstraintPackage())
                 .append(getConstraintName(), other.getConstraintName())
                 .append(getScoreLevel(), other.getScoreLevel())
                 .append(getWeightTotalAsNumber(), other.getWeightTotalAsNumber())
                 .toComparison();
     }
 
     @Override
     public String toString() {
         return getIdentificationString() + "=" + getWeightTotalAsNumber();
     }
 
 }
