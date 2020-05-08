 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.taglist;
 
 import org.sonar.api.CoreProperties;
 import org.sonar.api.batch.*;
 import org.sonar.api.measures.CountDistributionBuilder;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.PersistenceMode;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.Java;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.rules.*;
 import org.sonar.api.utils.SonarException;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 @DependsUpon(DecoratorBarriers.END_OF_VIOLATIONS_GENERATION)
 public class ViolationsDecorator implements Decorator {
   private static final String CHECKSTYLE_RULE_CONFIG_KEY = "Checker/TreeWalker/TodoComment";
   private static final String NOSONAR_RULE_CONFIG_KEY = "NoSonar";
 
   private RulesProfile rulesProfile;
   private RuleFinder ruleFinder;
 
   public ViolationsDecorator(RulesProfile rulesProfile, RuleFinder ruleFinder) {
     this.rulesProfile = rulesProfile;
     this.ruleFinder = ruleFinder;
   }
 
   @DependedUpon
   public List<Metric> dependedUpon() {
     return Arrays.asList(TaglistMetrics.TAGS, TaglistMetrics.OPTIONAL_TAGS, TaglistMetrics.MANDATORY_TAGS, TaglistMetrics.NOSONAR_TAGS,
             TaglistMetrics.TAGS_DISTRIBUTION);
   }
 
   public boolean shouldExecuteOnProject(Project project) {
     return Java.INSTANCE.equals(project.getLanguage());
   }
 
   public void decorate(Resource resource, DecoratorContext context) {
     if (Resource.QUALIFIER_CLASS.equals(resource.getQualifier())) {
       Collection<Rule> rules = new HashSet<Rule>();
 
       RuleQuery ruleQuery = RuleQuery.create()
           .withRepositoryKey(CoreProperties.CHECKSTYLE_PLUGIN)
           .withConfigKey(CHECKSTYLE_RULE_CONFIG_KEY);
       rules.addAll(ruleFinder.findAll(ruleQuery));
       ruleQuery = RuleQuery.create()
           .withRepositoryKey(CoreProperties.SQUID_PLUGIN)
           .withKey(NOSONAR_RULE_CONFIG_KEY);
       rules.addAll(ruleFinder.findAll(ruleQuery));
 
       saveFileMeasures(context, rules);
     }
   }
 
   protected void saveFileMeasures(DecoratorContext context, Collection<Rule> rules) {
     CountDistributionBuilder distrib = new CountDistributionBuilder(TaglistMetrics.TAGS_DISTRIBUTION);
     int mandatory = 0;
     int optional = 0;
     int noSonarTags = 0;
     for (Rule rule : rules) {
       ActiveRule activeRule = rulesProfile.getActiveRule(rule);
       if (activeRule != null) {
         for (Violation violation : context.getViolations()) {
           if (violation.getRule().equals(rule)) {
            if (isMandatory(activeRule.getSeverity())) {
               mandatory++;
             } else {
               optional++;
             }
             if (CoreProperties.SQUID_PLUGIN.equals(rule.getRepositoryKey())) {
               noSonarTags++;
             }
             distrib.add(getTagName(activeRule));
           }
         }
       }
     }
     saveMeasure(context, TaglistMetrics.TAGS, mandatory + optional);
     saveMeasure(context, TaglistMetrics.MANDATORY_TAGS, mandatory);
     saveMeasure(context, TaglistMetrics.OPTIONAL_TAGS, optional);
     saveMeasure(context, TaglistMetrics.NOSONAR_TAGS, noSonarTags);
     if (!distrib.isEmpty()) {
       context.saveMeasure(distrib.build().setPersistenceMode(PersistenceMode.MEMORY));
     }
   }
 
   protected static boolean isMandatory(RulePriority priority) {
     return priority.equals(RulePriority.BLOCKER) || priority.equals(RulePriority.CRITICAL);
   }
 
   private String getTagName(ActiveRule rule) {
     if (CoreProperties.CHECKSTYLE_PLUGIN.equals(rule.getRepositoryKey())) {
       return rule.getParameter("format");
     } else if (CoreProperties.SQUID_PLUGIN.equals(rule.getRepositoryKey())) {
       return "NOSONAR";
     }
     throw new SonarException("Taglist plugin doesn't work with rule: " + rule);
   }
 
   private void saveMeasure(DecoratorContext context, Metric metric, int value) {
     if (value > 0) {
       context.saveMeasure(metric, (double) value);
     }
   }
 }
