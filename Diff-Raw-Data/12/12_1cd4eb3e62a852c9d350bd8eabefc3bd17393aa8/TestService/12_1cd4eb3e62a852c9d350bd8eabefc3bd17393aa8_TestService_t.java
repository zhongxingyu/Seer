 /**
  *** This software is licensed under the GNU General Public License, version 3.
  *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
  *** Copyright 2012 Andrew Heald.
  */
 
 package uk.org.sappho.applications.transcript.service.report;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import freemarker.cache.FileTemplateLoader;
 import freemarker.cache.TemplateLoader;
 import uk.org.sappho.applications.transcript.service.TranscriptException;
 import uk.org.sappho.applications.transcript.service.registry.TranscriptModule;
 import uk.org.sappho.applications.transcript.service.registry.TranscriptParameters;
 import uk.org.sappho.applications.transcript.service.report.vcs.mock.MockVersionControlModule;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 public class TestService<T> {
 
     public T getService(Class<T> type, String workingCopyPath, String workingCopyId, boolean readOnly,
                         boolean includeVersionControlProperties,
                         String defaultValue, boolean isMerge, boolean failOnValueChange,
                         Map<String, String> parameters) throws TranscriptException {
 
         TranscriptParameters transcriptParameters = new TranscriptParameters(
                 workingCopyPath, workingCopyId,
                 readOnly, false, includeVersionControlProperties, defaultValue, isMerge, failOnValueChange,
                 parameters);
         TranscriptModule transcriptModule = new TranscriptModule(transcriptParameters);
         MockVersionControlModule vcsModule = new MockVersionControlModule();
         return Guice.createInjector(transcriptModule, vcsModule).getInstance(type);
     }
 
     public TemplateLoader getTemplateloader() throws IOException {
 
        return new FileTemplateLoader(new File("src/test/resources/templates"));
     }
 }
