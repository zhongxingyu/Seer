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
 import static org.mockito.Matchers.anyDouble;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.rules.Violation;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.utils.ProjectUtil;
 
 public class BaseMetricSensorTest {
 
 	private SensorContext context;
 
 	@Before
 	public void setup() throws URISyntaxException {
 		context = ProjectUtil.mockContext();
 		Configuration configuration = mock(Configuration.class);
 		ArrayList<InputFile> inputFiles = new ArrayList<InputFile>();
 		inputFiles.add(ProjectUtil.getInputFileByPath("/org/sonar/plugins/erlang/erlcount/src/erlcount_sup.erl"));
 
 		new BaseMetricsSensor(new Erlang()).analyse(ProjectUtil.getProject(inputFiles, configuration), context);
 	}
 
 	@Test
 	public void shouldSaveErrorsAndFailuresInXML() throws URISyntaxException {
 		ArgumentCaptor<Metric> argument = ArgumentCaptor.forClass(Metric.class);
 		ArgumentCaptor<Double> argument2 = ArgumentCaptor.forClass(Double.class);
 		verify(context, times(10)).saveMeasure((Resource) anyObject(), argument.capture(), argument2.capture());
 		List<Metric> capturedMetrics = argument.getAllValues();
 		List<Double> capturedValues = argument2.getAllValues();
 		assertThat("First violation is not files", capturedMetrics.get(0).getKey(), Matchers.equalTo("files"));
 		assertThat(capturedValues.get(0), Matchers.equalTo(1D));
 		assertThat("First violation is not lines", capturedMetrics.get(1).getKey(), Matchers.equalTo("lines"));
 		assertThat(capturedValues.get(1), Matchers.equalTo(32D));
 		assertThat("First violation is not ncloc", capturedMetrics.get(2).getKey(), Matchers.equalTo("ncloc"));
 		assertThat(capturedValues.get(2), Matchers.equalTo(21D));
 		assertThat("First violation is not comment_lines", capturedMetrics.get(3).getKey(), Matchers.equalTo("comment_lines"));
 		assertThat(capturedValues.get(3), Matchers.equalTo(4D));
 	}
 
 }
