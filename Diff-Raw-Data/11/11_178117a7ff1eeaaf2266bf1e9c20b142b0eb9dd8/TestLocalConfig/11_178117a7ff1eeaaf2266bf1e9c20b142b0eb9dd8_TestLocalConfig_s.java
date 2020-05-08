 //package org.jenkinsci.plugins.periodicreincarnationtest;
 //
 //import static org.junit.Assert.assertNotNull;
 //import static org.junit.Assert.assertTrue;
 //
 //import java.io.IOException;
 //
 //import hudson.model.Job;
 //import jenkins.model.Jenkins;
 //
 //import org.jenkinsci.plugins.periodicreincarnation.PeriodicReincarnation;
 //import org.junit.Rule;
 //import org.junit.Test;
 //import org.jvnet.hudson.test.JenkinsRule;
 //import org.jvnet.hudson.test.recipes.LocalData;
 //import org.xml.sax.SAXException;
 //
//import com.gargoylesoftware.htmlunit.html.HtmlElement;
 //import com.gargoylesoftware.htmlunit.html.HtmlPage;
 //
 //public class TestLocalConfig {
 //    @Rule 
 //    public JenkinsRule jenkinsRule = new JenkinsRule();
 //        
 //    @LocalData
 //    @Test
 //    public void testConfig() throws IOException, SAXException {
 //        assertNotNull(PeriodicReincarnation.get());
 //        final Job<?, ?> job1 = (Job<?, ?>) Jenkins.getInstance().getItem(
 //                "afterbuild_test");
 //        assertNotNull("job missing.. @LocalData problem?", job1);
 //        final Job<?, ?> job2 = (Job<?, ?>) Jenkins.getInstance().getItem(
 //                "afterbuild_test2");
 //        assertNotNull("job missing.. @LocalData problem?", job2);
 //        /*
 //         * No idea why this is not working...
 //         * https://groups.google.com/forum/#!topic/jenkinsci-dev/CKzCck-XrQI
 //         * 
 //         */
 //        final HtmlPage page = jenkinsRule.createWebClient().goTo("job/afterbuild_test/configure");
 //        final String allElements = page.asText();
 //        assertTrue(allElements.contains("Periodic"));
 //    }
 //}
