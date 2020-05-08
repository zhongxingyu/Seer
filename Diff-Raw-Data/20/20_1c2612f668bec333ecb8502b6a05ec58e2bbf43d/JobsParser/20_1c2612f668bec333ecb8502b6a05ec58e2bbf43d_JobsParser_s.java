 package com.globalsight.tools;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.EndElement;
 import javax.xml.stream.events.StartElement;
 
 import net.sundell.snax.DefaultElementHandler;
 import net.sundell.snax.NodeModel;
 import net.sundell.snax.NodeModelBuilder;
 import net.sundell.snax.SNAXParser;
 import net.sundell.snax.SNAXUserException;
 
import com.globalsight.tools.FileProfilesParser.AttrHandler;
import com.globalsight.tools.FileProfilesParser.FP;
import com.globalsight.tools.FileProfilesParser.FPHandler;
import com.globalsight.tools.FileProfilesParser.FileExtensionHandler;
import com.globalsight.tools.FileProfilesParser.LocaleHandler;

 public class JobsParser {
    
     private XMLInputFactory factory;
     
     JobsParser(XMLInputFactory factory) {
         this.factory = factory;
     }
     
     public List<Job> parse(String jobsXml) 
                         throws SNAXUserException, XMLStreamException {
         SNAXParser<JobData> parser = SNAXParser.createParser(factory, MODEL);
         JobData data = new JobData();
         parser.parse(new StringReader(jobsXml), data);
         return data.jobs;
     }
     
     /**
     +     * Job XML
     +     * <Jobs>
     +     * <Job>
     +           <id>645</id>
     +           <name>simple.html_811557652</name>
     +           <state>DISPATCHED</state>
     +           <displayState>In Progress</displayState>
     +           <priority>3</priority>
     +           <wordcount>32</wordcount>
     +           <sourceLang>en_US</sourceLang>
     +           <dueDate>2/9/11 9:00:00 AM GMT-08:00</dueDate>
     +           <sourcePages>
     +                   <sourcePage>
     +                           <sourcePageId>1115</sourcePageId>
     +                           <externalPageId>en_US/webservice/simple.html_8115576
     +                   </sourcePage>
     +           </sourcePages>
     +           <workflows>
     +                   <workflow>
     +                           <wfId>868</wfId>
     +                           <targetLang>de_DE</targetLang>
     +                   </workflow>
     +           </workflows>
     +        </Job>
     +        </Jobs>
     +     */
     
     static final NodeModel<JobData> MODEL = new NodeModelBuilder<JobData>() {{
         elements("Jobs", "Job").attach(new JobHandler());
         elements("Jobs", "Job", "sourcePages", "sourcePage").attach(new SPHandler());
         elements("Jobs", "Job", "sourcePages", "sourcePage").child()
                         .attach(new SPPropertyHandler());
         elements("Jobs", "Job", "workflows", "workflow").attach(new WFHandler());
         elements("Jobs", "Job", "workflows", "workflow").child()
                         .attach(new WFPropertyHandler());
         // Default rule, put this last
         elements("Jobs", "Job").child().attach(new JobPropertyHandler());
     }}.build();
     
     class JobData {
         List<Job> jobs = new ArrayList<Job>();
         private Job current = null;
         private String spId = null, spExtId = null;
         private String wfId = null, wfTgtLocale = null;
         
         void start() {
             current = new Job();
         }
         void end() {
             jobs.add(current);
             current = null;
         }
     }
     
     static class JobHandler extends DefaultElementHandler<JobData> {
         @Override
         public void startElement(StartElement element, JobData data)
                 throws SNAXUserException {
             data.start();
         }
         @Override
         public void endElement(EndElement element, JobData data)
                 throws SNAXUserException {
             data.end();
         }
     }
     static class SPHandler extends DefaultElementHandler<JobData> {
         @Override
         public void endElement(EndElement element, JobData data)
                 throws SNAXUserException {
             if (data.spId != null && data.spExtId != null) {
                 data.current.addSourcePage(data.spId, data.spExtId);
             }
             else {
                 throw new SNAXUserException("Invalid source page for job " + 
                         data.current.getId() + "; id=" + data.spId + 
                         ", extId=" + data.spExtId);
             }
             data.spId = null;
             data.spExtId = null;
         }
     }
     static class WFHandler extends DefaultElementHandler<JobData> {
         @Override
         public void endElement(EndElement element, JobData data)
                 throws SNAXUserException {
             if (data.wfId != null && data.wfTgtLocale != null) {
                 data.current.addWorkflow(parseLong(data.wfId), 
                 						 data.wfTgtLocale);
             }
             else {
                 throw new SNAXUserException("Invalid workflow for job " + 
                         data.current.getId() + "; id=" + data.wfId + 
                         ", tgtLocale=" + data.wfTgtLocale);
             }
             data.wfId = null;
             data.wfTgtLocale = null;
         }
     }
     static class JobPropertyHandler extends DefaultElementHandler<JobData> {
         @Override
         public void characters(StartElement parent, Characters contents,
                 JobData data) throws SNAXUserException {
             String el = parent.getName().getLocalPart();
             if (el.equals("id")) {
                 data.current.setId(contents.getData());
             }
             else if (el.equals("name")) {
                 data.current.setName(contents.getData());
             }
             else if (el.equals("state")) {
                 data.current.setState(contents.getData());
             }
             else if (el.equals("displayState")) {
                 data.current.setDisplayState(contents.getData());
             }
             else if (el.equals("priority")) {
                 data.current.setPriority(contents.getData());
             }
             else if (el.equals("wordcount")) {
                 data.current.setWordCount(contents.getData());
             }
             else if (el.equals("sourceLang")) {
                 data.current.setSourceLang(contents.getData());
             }
             else if (el.equals("dueDate")) {
                 data.current.setDueDate(contents.getData());
             }
         }
     }
     static class SPPropertyHandler extends DefaultElementHandler<JobData> {
         @Override
         public void characters(StartElement parent, Characters contents,
                 JobData data) throws SNAXUserException {
             String el = parent.getName().getLocalPart();
             if (el.equals("sourcePageId")) {
                 data.spId = contents.getData();
             }
             else if (el.equals("externalPageId")) {
                 data.spExtId = contents.getData();
             }
         }
     }
     static class WFPropertyHandler extends DefaultElementHandler<JobData> {
         @Override
         public void characters(StartElement parent, Characters contents,
                 JobData data) throws SNAXUserException {
             String el = parent.getName().getLocalPart();
             if (el.equals("wfId")) {
                 data.wfId = contents.getData();
             }
             else if (el.equals("targetLang")) {
                 data.wfTgtLocale = contents.getData();
             }
         }
     }
     
     private static long parseLong(String s) throws SNAXUserException {
         try {
             return Long.valueOf(s);
         }
         catch (NumberFormatException e) {
             throw new SNAXUserException(e);
         }
     }
 }
