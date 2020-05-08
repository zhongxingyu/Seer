 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2009 SonarSource SA
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
 package org.sonar.plugins.ral;
 
 import org.sonar.api.batch.Decorator;
 import org.sonar.api.batch.DecoratorContext;
 import org.sonar.api.batch.DependedUpon;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.measures.PropertiesBuilder;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.resources.ResourceUtils;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.rules.*;
 import org.sonar.api.utils.KeyValueFormat;
 import org.sonar.api.utils.KeyValue;
 import org.sonar.api.Plugins;
 import org.sonar.api.Plugin;
 import org.apache.commons.configuration.Configuration;
 
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 public class RulesActivationDecorator implements Decorator {
   private RulesManager rulesManager;
   private RulesProfile rulesProfile;
   private Configuration configuration;
   private Plugins plugins;
 
   public RulesActivationDecorator(RulesManager rulesManager, RulesProfile rulesProfile, Configuration configuration, Plugins plugins) {
     this.rulesManager = rulesManager;
     this.rulesProfile = rulesProfile;
     this.configuration = configuration;
     this.plugins = plugins;
   }
 
   public boolean shouldExecuteOnProject(Project project) {
     return true;
   }
 
   @DependedUpon
   public Metric generatesTodoMetric() {
     return RulesActivationMetrics.RULES_ACTIVATION_LEVEL;
   }
 
   public void decorate(Resource resource, DecoratorContext context) {
     if (!ResourceUtils.isRootProject(resource)) {
       return;
     }
     computeActivationLevel(context);
   }
 
   private void computeActivationLevel(DecoratorContext context) {
     Map<RulePriority, Integer> weights = getPriorityWeights();
     List<RulesRepository<?>> repositories = rulesManager.getRulesRepositories(context.getProject().getLanguage());
 
     HashMap<RulePriority, Integer> activateDistribution = initPriorityMap();
     HashMap<RulePriority, Integer> totalDistribution = initPriorityMap();
 
     PropertiesBuilder distribution = new PropertiesBuilder(RulesActivationMetrics.RULES_ACTIVATION_LEVEL_DISTRIBUTION);
 
     double activatedWeight = 0;
     double totalWeight = 0;
 
     for (RulesRepository repository : repositories) {
       Plugin plugin = plugins.getPluginByExtension(repository);
       List<Rule> rules = repository.getInitialReferential();
       for (Rule rule : rules) {
         RulePriority totalPriority = getRulePriority(rule, plugin.getKey());
         RulePriority activePriority = getActiveRulePriority(rule, plugin.getKey());
         if (activePriority != null) {
           activateDistribution.put(activePriority, activateDistribution.get(activePriority) + 1);
           activatedWeight += weights.get(activePriority);
         }
        totalDistribution.put(totalPriority, activateDistribution.get(totalPriority) + 1);
         totalWeight += weights.get(totalPriority);
       }
     }
     HashMap<RulePriority, Integer> ref = initPriorityMap();
     for (RulePriority priority : ref.keySet()) {
       distribution.add(priority,activateDistribution.get(priority) +" / "+ totalDistribution.get(priority));
     }
 
 
     context.saveMeasure(RulesActivationMetrics.RULES_ACTIVATION_LEVEL, activatedWeight / totalWeight * 100);
     context.saveMeasure(distribution.build());
   }
 
   private HashMap<RulePriority,Integer> initPriorityMap(){
     HashMap<RulePriority, Integer> map = new HashMap<RulePriority, Integer>();
     map.put(RulePriority.BLOCKER, 0);
     map.put(RulePriority.CRITICAL, 0);
     map.put(RulePriority.MAJOR, 0);
     map.put(RulePriority.MINOR, 0);
     map.put(RulePriority.INFO, 0);
 
     return map;
   }
 
 
   private RulePriority getActiveRulePriority(Rule rule, String pluginKey) {
     if (isActive(rule.getKey(), pluginKey)) {
       return getRulePriority(rule, pluginKey);
     }
     return null;
   }
 
   private RulePriority getRulePriority(Rule rule, String pluginKey) {
     if (isActive(rule.getKey(), pluginKey)) {
       ActiveRule activeRule = rulesProfile.getActiveRule(pluginKey, rule.getKey());
       return activeRule.getPriority();
     }
     return rule.getPriority();
   }
 
   private boolean isActive(String ruleKey, String pluginKey) {
     // As the rule do not contain the plugin key, I must use the getActiveRule(xx,xx) method
     ActiveRule activeRule = rulesProfile.getActiveRule(pluginKey, ruleKey);
     if (activeRule == null) {
       return false;
     }
     return true;
   }
 
 
   private Map<RulePriority, Integer> getPriorityWeights() {
     // The key must be hard-coded since the WeightedViolationsDecorator is not accessible through the API
     String levelWeight = configuration.getString("sonar.core.rule.weight", "INFO=0;MINOR=1;MAJOR=3;CRITICAL=5;BLOCKER=10");
 
     Map<RulePriority, Integer> weights = KeyValueFormat.parse(levelWeight, new KeyValueFormat.Transformer<RulePriority, Integer>() {
       public KeyValue<RulePriority, Integer> transform(String key, String value) {
         try {
           return new KeyValue<RulePriority, Integer>(RulePriority.valueOf(key.toUpperCase()), Integer.parseInt(value));
         }
         catch (Exception e) {
           return null;
         }
       }
     });
 
     for (RulePriority priority : RulePriority.values()) {
       if (!weights.containsKey(priority)) {
         weights.put(priority, 1);
       }
     }
     return weights;
   }
 }
