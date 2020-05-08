 import java.io.File;
 import java.util.*;
 
 import event.*;
 import exception.*;
 import filter.*;
 import parser.*;
 import writer.*;
 
 
 public class TivooSystem {
 
     private List<Event> myOriginalList;
     private List<Event> myFilteredList;
     private Set<Writer> myWriters;
     private Set<Parser> myParsers;
     private FilterDecorator myHeadFilter;
     private static HashMap<String,Parser> myMap=new HashMap<String,Parser>();
 
     static {
 
     	myMap.put("DukeBasketBall.xml", new DukeBasketballParser());
     	myMap.put("dukecal.xml", new DukeCalendarParser()); 
     	myMap.put("googlecal.xml", new GoogleCalenderParser());
    	myMap.put("NFL.xml",new NFLParser());
    }
 
     public TivooSystem() {
         myOriginalList = new ArrayList<Event>();
         myFilteredList = new ArrayList<Event>();
         myParsers = new HashSet<Parser>();
         myWriters = new HashSet<Writer>();
         myHeadFilter = null;
     }
 
     public void loadFile (File file) {  
         try {
             Parser parser =  myMap.get(file.getName());
             parser.loadFile(file);
             myParsers.add(parser);}
         catch (NullPointerException e) {
             throw new TivooUnrecognizedFeed();
         }
     }
 
     public void addFilterByKeyword (String keyword) {
         FilterDecorator filter = new FilterByKeyword(keyword);
         addFilter(filter);
     }
 
     public void addFilterByTimeFrame (String startTime, String endTime) {
         FilterDecorator filter = new FilterByTimeFrame(startTime, endTime);
         addFilter(filter);
     }
     
     public void addFilterByKeywordSorting (String keyword) {
         FilterByKeywordSorting filter = new FilterByKeywordSorting(keyword);
         addFilter(filter);
     }
     
     public void addFilterByKeywordInGeneral (String keyword) {
         FilterByKeywordInGeneral filter = new FilterByKeywordInGeneral(keyword);
         addFilter(filter);
     }
     
 
     private void addFilter(FilterDecorator filter) {
         if (myHeadFilter == null) {
             myHeadFilter = filter;
         }
         else {
             filter.appendFilter(myHeadFilter);
             myHeadFilter = filter;
         }
     }
 
     public void addSummaryAndDetailPagesWriter(String directory) {
         Writer writer = new SummaryAndDetailsPagesWriter(directory);
         addWriter(writer);
     }
     
     
     public void addConflictWriter(String directory) {
         Writer writer = new ConflictWriter(directory);
         addWriter(writer);
     }
     
     public void addListWriter(String directory) {
         Writer writer = new ListWriter(directory);
         addWriter(writer);
     }
 
     private void addWriter(Writer writer) {
         myWriters.add(writer);
     }
 
     public void perform() {
         
         clear();
         
         parse();
         
         filter();
         
         output();
         
     }
 
     private void parse() {
         if (myParsers.size() == 0) {
             throw new TivooNoParserSelected();
         }
         for (Parser parser: myParsers) {
             parser.parse();
             myOriginalList.addAll(parser.getEventList());
         }
     }
 
     private void filter() {
         if (myHeadFilter == null) {
             throw new TivooNoFilterSelected();
         }
         myHeadFilter.filter(myOriginalList);
         myFilteredList = myHeadFilter.getFilteredList();
     }
 
     private void output() {
         if (myParsers.size() == 0) {
             throw new TivooNoWriterSelected();
         }
         
         for (Writer writer:myWriters) {
             System.out.println("Looping and Writing");
             writer.outputHTML(myFilteredList);
             System.out.println("output");
         }
     }
 
     private void clear() {
         myOriginalList = new ArrayList<Event>();
         myFilteredList = new ArrayList<Event>();
     }
 }
