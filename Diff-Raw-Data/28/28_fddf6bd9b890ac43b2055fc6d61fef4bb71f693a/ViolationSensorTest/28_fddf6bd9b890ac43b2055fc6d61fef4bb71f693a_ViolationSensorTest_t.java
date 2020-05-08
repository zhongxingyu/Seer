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
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.api.rules.Violation;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.utils.ProjectUtil;
 import org.sonar.plugins.erlang.utils.RuleUtil;
 
 public class ViolationSensorTest {
 
 	private SensorContext context;
 	private ActiveRule activeRule;
 	private ActiveRule activeRule2;
 	private ActiveRule activeRule3;
 	private ActiveRule activeRule4;
 	private ActiveRule activeRule5;
 
 	@Before
 	public void setup() throws URISyntaxException {
 		context = ProjectUtil.mockContext();
 		Configuration configuration = mock(Configuration.class);
 		ArrayList<InputFile> srcFiles = new ArrayList<InputFile>();
 		ArrayList<InputFile> otherFiles = new ArrayList<InputFile>();
 		otherFiles
 				.add(ProjectUtil
 						.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/.eunit/RE-max_depth_of_calling.txt"));
 		otherFiles.add(ProjectUtil
 				.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/.eunit/RE-mcCabe.txt"));
 		otherFiles.add(ProjectUtil
 				.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/.eunit/RE-cohesion.txt"));
 		otherFiles.add(ProjectUtil
 				.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/.eunit/dialyzer.log"));
 		srcFiles.add(ProjectUtil
 				.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/src/refactorerl_issues.erl"));
 		srcFiles.add(ProjectUtil
 				.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/src/erlcount_lib.erl"));
 
 		RulesProfile rp = mock(RulesProfile.class);
 		List<ActiveRule> rlz = new ArrayList<ActiveRule>();
 		activeRule = RuleUtil.generateActiveRule("mcCabe", "R001", new HashMap<String, String>() {
 			{
 				put("maximum", "10");
 			}
 		});
 		activeRule2 = RuleUtil.generateActiveRule("max_depth_of_calling", "R002",
 				new HashMap<String, String>() {
 					{
 						put("maximum", "10");
 					}
 				});
 		activeRule3 = RuleUtil.generateActiveRule("cohesion", "R003",
 				new HashMap<String, String>() {
 					{
 						put("maximum", "4");
 					}
 				});
 		activeRule4 = RuleUtil.generateActiveRule("unused_fun", "D019", null);
 		activeRule5 = RuleUtil.generateActiveRule("callback_missing", "D041", null);
 
 		rlz.add(activeRule);
 		rlz.add(activeRule2);
 		rlz.add(activeRule3);
 		rlz.add(activeRule4);
 		rlz.add(activeRule5);
 		when(rp.getActiveRulesByRepository("Erlang")).thenReturn(rlz);
 		when(rp.getActiveRule("Erlang", "D019")).thenReturn(activeRule4);
 		when(rp.getActiveRule("Erlang", "D041")).thenReturn(activeRule5);
 		when(rp.getName()).thenReturn("Erlang");
 
 		new ViolationSensor(new Erlang(), rp).analyse(
 				ProjectUtil.getProject(srcFiles, otherFiles, configuration), context);
 	}
 
 	@Test
 	public void checkSensor() throws URISyntaxException {
 		ArgumentCaptor<Violation> argument = ArgumentCaptor.forClass(Violation.class);
 		verify(context, times(7)).saveViolation(argument.capture());
 		List<Violation> capturedViolations = argument.getAllValues();
		Violation v = RuleUtil.getViolationByKey(capturedViolations, "R002");
		assertThat("violation is not R002", v.getRule().getKey(),
 				Matchers.equalTo("R002"));
		assertThat("violation is not R002", v.getMessage(),
 				Matchers.equalTo("max_depth_of_calling is 11 (max allowed is 10)"));
		assertThat("violation is not R002", v.getLineId(),
 				Matchers.equalTo(22));
 	}
 
 }
