 package tivoo.output;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import com.hp.gagawa.java.Document;
 import com.hp.gagawa.java.DocumentType;
 import com.hp.gagawa.java.Node;
 import com.hp.gagawa.java.elements.*;
 import tivoo.Event;
 
 
 public abstract class HTMLBuilder
 {
 
     private static final String TITLE = "Generic View";
     private static final String TIVOO_CSS = "../css/tivooStyle.css";
     private static final String UNIQUE_CSS = "";
     private static final String DETAIL_PAGE_FOLDER = "details_dir";
 
     protected static final List<String> DAYS_LIST = initializeDaysList();
     protected static final List<String> MONTHS_LIST = initializeMonthsList();
     
     private String mySummaryPageFileName;
     private String myDetailPageDirectory;
     
     protected HTMLBuilder (String summaryPageFileName)
     {
         mySummaryPageFileName = summaryPageFileName;
         myDetailPageDirectory = determineDetailPageDirectory(summaryPageFileName);
     }
     
     public void buildHTML(List<Event> eventList) throws IOException
     {
         buildSummaryPage(eventList);
         buildDetailPages(eventList);
     }
     
     protected abstract Node buildView (List<Event> eventList);
     
     protected A linkToDetailsPage (Event currentEvent)
     {
         StringBuilder link = new StringBuilder(DETAIL_PAGE_FOLDER);
         link.append("/");
         link.append(createDetailsPageURL(currentEvent));
 
         A detailsLink = new A();
         detailsLink.appendText(currentEvent.getTitle());
         detailsLink.setHref(link.toString());
         return detailsLink;
     }
     
     protected Div constructEventDiv (Event currentEvent)
     {
         Div eventInfo = new Div().setId("event");
         
         P eventP = new P();
         eventP.appendChild(linkToDetailsPage(currentEvent));
         eventP.appendChild(new Br());
         eventP.appendText(formatDateTimespan(currentEvent));
         
         eventInfo.appendChild(eventP);
         return eventInfo;
     }
     
     protected String formatDateTimespan (Event currentEvent)
     {
         StringBuilder timespan = new StringBuilder();
         
         Calendar start = currentEvent.getStartTime();
         timespan.append(formatDate(start));
         timespan.append(" - ");
        
         Calendar end = currentEvent.getEndTime();
         if (isAllOnOneDay(start, end))
         {
             timespan.append(formatClockTime(end));
         }
         else
         {
             timespan.append(formatDate(end));
         }
             
         return timespan.toString();
     }
     
     protected String formatClockTimespan (Event currentEvent)
     {
         StringBuilder clockTimespan = new StringBuilder();
         clockTimespan.append(formatClockTime(currentEvent.getStartTime()));
         clockTimespan.append(" - ");
         clockTimespan.append(formatClockTime(currentEvent.getEndTime()));
         return clockTimespan.toString();
     }    
     
     protected String getDate (Event currentEvent)
     {
         return String.format("%1$tm/%<te", currentEvent.getStartTime());
     }
     
     protected Map<String, List<Event>> sortByDayOfWeek (List<Event> eventList)
     {
         Map<String, List<Event>> sortedEvents = new TreeMap<String, List<Event>>();
         for (Event currentEvent : eventList)
         {
             String eventDay = getDay(currentEvent);
             if (!sortedEvents.containsKey(eventDay))
                 sortedEvents.put(eventDay, new ArrayList<Event>());
             sortedEvents.get(eventDay).add(currentEvent);
         }
         return sortedEvents;
     }
     
     protected Map<String, List<Event>> sortByDate (List<Event> eventList)
     {
         Map<String, List<Event>> sortedEvents = new TreeMap<String, List<Event>>();
         for (Event currentEvent : eventList)
         {
             String eventDay = getDate(currentEvent);
             if (!sortedEvents.containsKey(eventDay))
                 sortedEvents.put(eventDay, new ArrayList<Event>());
             sortedEvents.get(eventDay).add(currentEvent);
         }
         return sortedEvents;
     }
     
     protected String getTitle ()
     {
         return TITLE;
     }
     
     protected String getUniqueCSS ()
     {
         return UNIQUE_CSS;
     }
     
     private static List<String> initializeDaysList ()
     {
         String[] days =
             new String[] {
                     "Sunday",
                     "Monday",
                     "Tuesday",
                     "Wednesday",
                     "Thursday",
                     "Friday",
                     "Saturday" };
         List<String> dayList = new ArrayList<String>();
         for (String d : days)
             dayList.add(d);
         return dayList;
     }
     
     private static List<String> initializeMonthsList ()
     {
         String[] months =
             new String[] {
                     "January",
                     "February",
                     "March",
                     "April",
                     "May",
                     "June",
                     "July",
                     "August",
                     "September",
                     "October",
                     "November",
                     "December" };
         List<String> monthList = new ArrayList<String>();
         for (String m : months)
             monthList.add(m);
         return monthList;
     }
 
     private String determineDetailPageDirectory (String summaryPageFileName)
     {
         StringBuilder detailPageDirectory = new StringBuilder();
         if (summaryPageFileName.contains("/"))
         {
             int lastFolder = summaryPageFileName.lastIndexOf("/");
             detailPageDirectory.append(summaryPageFileName.substring(0, lastFolder + 1));
             detailPageDirectory.append(DETAIL_PAGE_FOLDER);
         }
         else
         {
             detailPageDirectory.append(DETAIL_PAGE_FOLDER);
         }
         return detailPageDirectory.toString();
     }
     
     private void buildSummaryPage (List<Event> eventList) throws IOException
     {
         if (mySummaryPageFileName.contains("/"))
         {
             File outputDirectory = new File(myDetailPageDirectory);
             outputDirectory.mkdirs();
         }
         File summaryPage = new File(mySummaryPageFileName);
         summaryPage.createNewFile();
         writeSummaryPageHTML(summaryPage, eventList);
     }
     
     private void buildDetailPages (List<Event> eventList) throws IOException
     {
         File detailsDirectory = new File(myDetailPageDirectory);
         detailsDirectory.mkdirs();
         for (Event currentEvent : eventList)
         {
             String detailPageURL = createDetailsPageURL(currentEvent);
             File detailPage = new File(myDetailPageDirectory + "/" + detailPageURL);
             boolean isNewFile = detailPage.createNewFile();
             if (isNewFile)
                 writeDetailsPageHTML(currentEvent, detailPage);
         }
     }
     
     private void writeSummaryPageHTML (File summaryPage, List<Event> eventList) throws IOException
     {
         FileOutputStream fos = new FileOutputStream(summaryPage);
         OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
 
         Document doc = initializeHTMLDocument(getTitle(), "");
         
         String uniqueCSS = getUniqueCSS();
         if (uniqueCSS.length() > 0)
         {
             doc.head.appendChild(insertCSS(uniqueCSS));
         }
         
         writeHeader(doc);    
         writeSummaryPageContent(doc, eventList);
         writeFooter(doc);
         
         out.write(doc.write());
         out.close();
     }
     
     private void writeSummaryPageContent(Document doc, List<Event> eventList)
     {
         Div content = new Div().setCSSClass("content");
         String title = getTitle();
         content.appendChild(new H3().appendText(title));
         Node view = buildView(eventList);
         content.appendChild(view);
         doc.body.appendChild(content);
     }
     
     private void writeDetailsPageHTML (Event currentEvent, File detailPage) throws IOException
     {
         FileOutputStream fos = new FileOutputStream(detailPage);
         OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
 
         Document doc = initializeHTMLDocument("Details", "../");
         writeHeader(doc);
         writeDetailsPageContent(currentEvent, doc);
         writeFooter(doc);
         
         out.write(doc.write());
         out.close();
     }
     
     private void writeDetailsPageContent (Event currentEvent, Document doc)
     {
         Div content = new Div().setCSSClass("content");
         content.appendChild(new H4().appendText(currentEvent.getTitle()));
 
         createParagraphTag(content, "Time", formatDateTimespan(currentEvent));
         createParagraphTag(content, "Location", currentEvent.getLocation());
         createParagraphTag(content, "Description", currentEvent.getDescription());
         
         doc.body.appendChild(content);
     }
     
     private Document initializeHTMLDocument (String title, String cssFilePathExtender)
     {
         Document doc = new Document(DocumentType.HTMLTransitional);
         doc.head.appendChild(new Title().appendText(title));
         
         StringBuilder cssFilePath = new StringBuilder(cssFilePathExtender);
         cssFilePath.append(TIVOO_CSS);
         doc.head.appendChild(insertCSS(cssFilePath.toString()));
         
         return doc;
     }
     
     private Link insertCSS (String filePath)
     {
         Link tivooStyle = new Link();
         tivooStyle.setRel("stylesheet");
         tivooStyle.setType("text/css");
         tivooStyle.setHref(filePath);
         return tivooStyle;
     }
     
     private void writeHeader (Document doc)
     {
         Div header = new Div().setCSSClass("header");
         Table caption = new Table();
         Tr title = new Tr().appendChild(new Td().appendText("TiVOO"));
         caption.appendChild(title);
         header.appendChild(caption);
         doc.body.appendChild(header);
     }
 
     private String createDetailsPageURL (Event currentEvent)
     {
         StringBuilder url = new StringBuilder();
         url.append(currentEvent.getTitle()
                                .replaceAll("\\s+", "_")
                                .replaceAll("[^A-z_0-9]", "")
                                .trim());
         url.append(".html");
         return url.toString();
     }
     
     private void createParagraphTag (Div div, String category, String contents)
     {
         P paragraph = new P();
         if (category != null)
         {
             paragraph.appendChild(new U().appendText(category));
             paragraph.appendText(": ");
         }
         paragraph.appendText(contents);
         div.appendChild(paragraph);
     }
     
     private void writeFooter (Document doc)
     {
         Div footer = new Div().setCSSClass("footer");
         footer.appendText("Designed by Siyang, Hui, Ian, & Eric");
         footer.appendChild(new Br());
         footer.appendText("&copy; 2012");
         doc.body.appendChild(footer);
     }
     
     private boolean isAllOnOneDay(Calendar start, Calendar end)
     {
         return (start.get(Calendar.DAY_OF_WEEK) == end.get(Calendar.DAY_OF_WEEK));
     }
     
     private String formatDate (Calendar cal)
     {
         return String.format("%1$ta %<tm/%<te at %<tl:%<tM %<Tp", cal);
     }
     
     private String formatClockTime (Calendar cal)
     {
         return String.format("%1$tl:%<tM %<Tp", cal);
     }
     
     private String getDay (Event currentEvent)
     {
         return String.format("%1$tA", currentEvent.getStartTime());
     }
     
 }
