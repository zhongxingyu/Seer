 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.sensor;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.rules.Rule;
 import org.sonar.api.rules.Violation;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.language.ErlangFile;
 import org.sonar.plugins.erlang.violations.ErlangRuleManager;
 import org.sonar.plugins.erlang.violations.ErlangViolationResults;
 import org.sonar.plugins.erlang.violations.Issue;
 import org.sonar.plugins.erlang.violations.dialyzer.DialyzerRuleRepository;
 import org.sonar.plugins.erlang.violations.dialyzer.ErlangDialyzer;
 import org.sonar.plugins.erlang.violations.refactorerl.ErlangRefactorErl;
 import org.sonar.plugins.erlang.violations.refactorerl.RefactorErlRuleRepository;
 
 /**
  * Calls the dialyzer report parser and the refactorerl report parser and saves
  * violations to sonar
  * 
  * @author tkende
  * 
  */
 public class ViolationSensor extends AbstractErlangSensor {
 
 	private ErlangRuleManager dialyzerRuleManager = new ErlangRuleManager(DialyzerRuleRepository.RULES_FILE);
 	private ErlangRuleManager refactorErlRuleManager = new ErlangRuleManager(RefactorErlRuleRepository.RULES_FILE);
 	private RulesProfile rulesProfile;
 
 	public ViolationSensor(Erlang erlang, RulesProfile rulesProfile) {
 		super(erlang);
 		this.rulesProfile = rulesProfile;
 	}
 
 	private static final Logger LOG = LoggerFactory.getLogger(ViolationSensor.class);
 	private ErlangDialyzer dialyzer = new ErlangDialyzer();
 	private ErlangRefactorErl refactorErl = new ErlangRefactorErl();
 
 	public void analyse(Project project, SensorContext context) {
 		ErlangViolationResults violationIssues = refactorErl.refactorErl(project, refactorErlRuleManager, rulesProfile);
 		violationIssues.appendIssues(dialyzer.dialyzer(project, dialyzerRuleManager).getIssues());
 		for (InputFile inputFile : project.getFileSystem().mainFiles(getErlang().getKey())) {
 			try {
				// report = Erl
 				analyzeFile(inputFile, project, context, violationIssues);
 			} catch (Exception e) {
 				LOG.error("Can not analyze the file " + inputFile.getFileBaseDir() + "\\" + inputFile.getRelativePath(), e);
 			}

 		}
 	}
 
 	private void analyzeFile(InputFile inputFile, Project project, SensorContext context,
 			ErlangViolationResults violationIssues) throws IOException {
 		ErlangFile erlangFile = ErlangFile.fromInputFile(inputFile);
 		String actModuleName = erlangFile.getName().concat(".erl");
 		List<Issue> issues = violationIssues.filterIssuesByModuleName(actModuleName);
 		for (Issue issue : issues) {
 			Rule rule = Rule.create(DialyzerRuleRepository.REPOSITORY_NAME, issue.ruleId);
 			Violation violation = Violation.create(rule, erlangFile);
 			violation.setLineId(issue.line);
 			violation.setMessage(issue.descr);
 			context.saveViolation(violation);
 		}
 
 	}
 
 }
