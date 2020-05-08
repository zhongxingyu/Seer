 /*
  * Copyright 2012 Jasha Joachimsthal
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package eu.jasha.portaltests.stories;
 
 import java.util.List;
 
 import org.jbehave.core.configuration.Configuration;
 import org.jbehave.core.embedder.StoryControls;
 import org.jbehave.core.failures.FailingUponPendingStep;
 import org.jbehave.core.io.LoadFromClasspath;
 import org.jbehave.core.io.StoryFinder;
 import org.jbehave.core.junit.JUnitStories;
 import org.jbehave.core.reporters.CrossReference;
 import org.jbehave.core.reporters.Format;
 import org.jbehave.core.reporters.StoryReporterBuilder;
 import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
 import org.jbehave.core.steps.spring.SpringStepsFactory;
 import org.jbehave.web.selenium.ContextView;
 import org.jbehave.web.selenium.LocalFrameContextView;
 import org.jbehave.web.selenium.SeleniumConfiguration;
 import org.jbehave.web.selenium.SeleniumContext;
 import org.jbehave.web.selenium.SeleniumContextOutput;
 import org.jbehave.web.selenium.SeleniumStepMonitor;
 import org.springframework.context.ApplicationContext;
 
 import static java.util.Arrays.asList;
 import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
 import static org.jbehave.core.reporters.Format.CONSOLE;
 import static org.jbehave.web.selenium.WebDriverHtmlOutput.WEB_DRIVER_HTML;
 
 /**
  * Handles story for new users
  */
 public class NewUserStories extends JUnitStories {
     public NewUserStories() {
         CrossReference crossReference = new CrossReference().withJsonOnly().withOutputAfterEachStory(true)
                 .excludingStoriesWithNoExecutedScenarios(true);
         ContextView contextView = new LocalFrameContextView().sized(640, 120);
         SeleniumContext seleniumContext = new SeleniumContext();
         SeleniumStepMonitor stepMonitor = new SeleniumStepMonitor(contextView, seleniumContext,
                 crossReference.getStepMonitor());
         Format[] formats = new Format[]{new SeleniumContextOutput(seleniumContext), CONSOLE, WEB_DRIVER_HTML};
         StoryReporterBuilder reporterBuilder = new StoryReporterBuilder()
                 .withCodeLocation(codeLocationFromClass(NewUserStories.class)).withFailureTrace(true)
                 .withFailureTraceCompression(true).withDefaultFormats().withFormats(formats)
                 .withCrossReference(crossReference);
 
         Configuration configuration = new SeleniumConfiguration().useSeleniumContext(seleniumContext)
                 .useFailureStrategy(new FailingUponPendingStep())
                 .useStoryControls(new StoryControls().doResetStateBeforeScenario(false)).useStepMonitor(stepMonitor)
                 .useStoryLoader(new LoadFromClasspath(NewUserStories.class))
                 .useStoryReporterBuilder(reporterBuilder);
         useConfiguration(configuration);
 
         ApplicationContext context = new SpringApplicationContextFactory("newUser-steps.xml").createApplicationContext();
         useStepsFactory(new SpringStepsFactory(configuration, context));
 
     }
 
     @Override
     protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/newuser.story"), null);
     }
 }
