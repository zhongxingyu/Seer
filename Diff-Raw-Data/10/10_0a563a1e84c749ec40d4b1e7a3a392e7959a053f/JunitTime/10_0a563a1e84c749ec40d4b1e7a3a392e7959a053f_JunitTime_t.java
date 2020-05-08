 package com.googlecode.junittime;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.resources.FileResource;
import org.apache.commons.io.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import com.googlecode.junittime.domain.TestCaseRepository;
 import com.googlecode.junittime.domain.XmlTestCaseExtractor;
 import com.googlecode.junittime.domain.ExtractionException;
 import com.googlecode.junittime.domain.reporting.HtmlReportGenerator;
 
 public class JunitTime extends Task {
     static final String REPORT_CSV = "junit-time-report.csv";
     static final String REPORT_HTML = "junit-time-report.html";
 
     private File toDir;
     private FileSet from;
 
     public void addFileSet(FileSet from) {
         this.from = from;
     }
 
     public void setToDir(File toDir) {
         this.toDir = toDir;
     }
 
     public void execute() throws BuildException {
         ensureTestResultsExist();
         try {
             report(extract());
         } catch (ExtractionException e) {
             throw new BuildException("Unable to extract test execution information", e);
         } catch (IOException e) {
             throw new BuildException("Unable to generate junit-time report", e);
         }
     }
 
     private TestCaseRepository extract() throws ExtractionException {
         TestCaseRepository repository = new TestCaseRepository();
         Iterator iterator = from.iterator();
         while (iterator.hasNext()) {
            FileResource file = (FileResource) iterator.next();
             new XmlTestCaseExtractor(file.getFile()).extractTo(repository);
         }
         return repository;
     }
 
     private void report(TestCaseRepository repository) throws IOException {
        File reportFile = new File(toDir, REPORT_HTML);        
        FileUtils.touch(reportFile);
         new HtmlReportGenerator().generate(reportFile, repository);
     }
 
     private void ensureTestResultsExist() {
         if (!from.getDir().exists()) {
             throw new BuildException("Directory " + from.getDir().getAbsolutePath() + " not found");
         }
     }
 }
