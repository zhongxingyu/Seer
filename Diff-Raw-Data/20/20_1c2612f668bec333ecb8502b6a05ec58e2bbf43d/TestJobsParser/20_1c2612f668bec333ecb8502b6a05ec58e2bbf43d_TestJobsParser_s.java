 package com.globalsight.tools;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
 
 import javax.xml.stream.XMLInputFactory;
 
 import org.junit.Test;
 
 public class TestJobsParser {
     XMLInputFactory factory = XMLInputFactory.newFactory();
     
     @Test
     public void test() throws Exception {
         try {
             List<Job> jobs = 
                     new JobsParser(factory).parse(SAMPLE_JOB_XML);
             assertEquals(2, jobs.size());
             expect(jobs.get(0), "1000", "simple.html_312089757", "EXPORTED",
                    "Exported", "3", "32", "en_US", "2/17/11 5:00:00 PM UTC",
                    Arrays.asList(new Job.SourcePage[] {
                        new Job.SourcePage("1000", "en_US/webservice/simple.html_312089757/src/gscli/simple.html")
                    }),
                    Arrays.asList(new Workflow[] {
                        new Workflow(1, "fr_FR")
                    })
             );
             expect(jobs.get(1), "1001", "simple.html_811636338", "EXPORTED",
                     "Exported", "3", "32", "en_US", "2/17/11 5:00:00 PM UTC",
                     Arrays.asList(new Job.SourcePage[] {
                         new Job.SourcePage("1001", "en_US/webservice/simple.html_811636338/src/gscli/simple.html")
                     }),
                     Arrays.asList(new Workflow[] {
                         new Workflow(2, "fr_FR")
                     })
              );
         }
         catch (Exception e) {
             fail(e.getMessage());
             throw e;
         }
     }
 
     void expect(Job job, String id, String name, String state, String displayState,
                 String priority, String wordCount, String sourceLang, String dueDate,
                 List<Job.SourcePage> sourcePages, List<Workflow> workflows) {
         assertEquals("Error with job " + job.getId(),id, job.getId());
         assertEquals("Error with job " + job.getId(),name, job.getName());
         assertEquals("Error with job " + job.getId(),state, job.getState());
         assertEquals("Error with job " + job.getId(),displayState, job.getDisplayState());
         assertEquals("Error with job " + job.getId(),priority, job.getPriority());
         assertEquals("Error with job " + job.getId(),wordCount, job.getWordCount());
         assertEquals("Error with job " + job.getId(),sourceLang, job.getSourceLang());
         assertEquals("Error with job " + job.getId(),dueDate, job.getDueDate());
         assertEquals("Error with job " + job.getId(),
                      sourcePages.size(), job.getSourcePages().size());
         if (sourcePages.size() == job.getSourcePages().size()) {
             for (int i = 0; i < sourcePages.size(); i++) {
                 assertEquals("Job " + job.getId() + ", unexpected sourcePage " + i, 
                         sourcePages.get(i).getId(),
                         job.getSourcePages().get(i).getId());
                 assertEquals("Job " + job.getId() + ", unexpected sourcePage " + i, 
                         sourcePages.get(i).getExternalPageId(),
                         job.getSourcePages().get(i).getExternalPageId());
             }
         }
         assertEquals(workflows.size(), job.getWorkflows().size());
         if (workflows.size() == job.getWorkflows().size()) {
             for (int i = 0; i < workflows.size(); i++) {
                 assertEquals("Job " + job.getId() + ", unexpected workflows " + i, 
                         workflows.get(i).getId(),
                         job.getWorkflows().get(i).getId());
                 assertEquals("Job " + job.getId() + ", unexpected workflows " + i, 
                         workflows.get(i).getTargetLocale(),
                         job.getWorkflows().get(i).getTargetLocale());
             }
         }
     }
     
     static final String SAMPLE_JOB_XML = 
         "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + 
         "<Jobs> " + 
         "    <Job>" + 
         "        <id>1000</id>" + 
         "        <name>simple.html_312089757</name>" + 
         "        <state>EXPORTED</state>" + 
         "        <displayState>Exported</displayState>" + 
         "        <priority>3</priority>" + 
         "        <wordcount>32</wordcount>" + 
         "        <sourceLang>en_US</sourceLang>" + 
         "        <dueDate>2/17/11 5:00:00 PM UTC</dueDate>" + 
         "        <sourcePages>" + 
         "            <sourcePage>" + 
         "                <sourcePageId>1000</sourcePageId>" + 
         "                <externalPageId>en_US/webservice/simple.html_312089757/src/gscli/simple.html</externalPageId>" +
         "            </sourcePage>" +
         "        </sourcePages>" +
         "        <workflows>" +
         "            <workflow>" + 
         "                <wfId>1</wfId>" + 
         "                <targetLang>fr_FR</targetLang>" + 
         "            </workflow>" + 
         "        </workflows>" + 
         "    </Job>" + 
         "    <Job>" + 
         "        <id>1001</id>" + 
         "        <name>simple.html_811636338</name>" + 
         "        <state>EXPORTED</state>" + 
         "        <displayState>Exported</displayState>" + 
         "        <priority>3</priority>" + 
         "        <wordcount>32</wordcount>" + 
         "        <sourceLang>en_US</sourceLang>" + 
         "        <dueDate>2/17/11 5:00:00 PM UTC</dueDate>" + 
         "        <sourcePages>" + 
         "            <sourcePage>" + 
         "                <sourcePageId>1001</sourcePageId>" + 
         "                <externalPageId>en_US/webservice/simple.html_811636338/src/gscli/simple.html</externalPageId>" +
         "            </sourcePage>" +
         "        </sourcePages>" +
         "        <workflows>" +
         "            <workflow>" +
         "                <wfId>2</wfId>" + 
         "                <targetLang>fr_FR</targetLang>" +                              "            </workflow>" + 
         "        </workflows>" + 
         "    </Job>" + 
         "</Jobs>";
 }
