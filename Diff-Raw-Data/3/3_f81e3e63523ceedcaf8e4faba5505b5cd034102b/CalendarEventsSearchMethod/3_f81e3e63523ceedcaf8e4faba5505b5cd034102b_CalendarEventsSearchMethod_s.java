 package net.cyklotron.cms.documents.calendar;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FieldCache;
 import org.apache.lucene.search.FieldCache.LongParser;
 import org.apache.lucene.search.FieldComparator;
 import org.apache.lucene.search.FieldComparatorSource;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TermRangeQuery;
 import org.apache.lucene.util.Version;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.TableState;
 import org.objectledge.templating.TemplatingContext;
 
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchUtil;
 import net.cyklotron.cms.search.searching.PageableResultsSearchMethod;
 
 /**
  * Calendar search method implementation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: CalendarSearchMethod.java,v 1.8 2005-08-10 05:31:05 rafal Exp $
  */
 public class CalendarEventsSearchMethod extends PageableResultsSearchMethod
 {
     private Date startDate;
     private Date endDate;
     
     private Query query;
     private String textQuery;
     private String[] acceptedSiteNames;
     
     public CalendarEventsSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Date startDate,
         Date endDate)
     {
         super(searchService, parameters, locale);
         this.startDate = startDate;
         this.endDate = endDate;
         this.textQuery = "";
     }
     
     public CalendarEventsSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Date startDate,
         Date endDate, 
         String textQuery)
     {
         this(searchService, parameters, locale, startDate, endDate);
         this.textQuery = textQuery;
     }
     
     public CalendarEventsSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Date startDate,
         Date endDate, 
         String textQuery,   
         String[] acceptedSiteNames)
     {
         this(searchService, parameters, locale, startDate, endDate, textQuery);
         this.acceptedSiteNames = acceptedSiteNames;
     }
     
     public CalendarEventsSearchMethod(SearchService searchService, Parameters parameters, Locale locale, CalendarSearchParameters searchParameters)
     {
         super(searchService, parameters, locale);
         this.startDate = searchParameters.getStartDate();
         this.endDate = searchParameters.getEndDate();
         this.textQuery = searchParameters.getTextQuery();
         if(searchParameters.getCategoryQuery() != null)
         {
             this.acceptedSiteNames = searchParameters.getCategoryQuery().getAcceptedSiteNames();
         }
         else
         {
             this.acceptedSiteNames = null;
         }
     }
     
     @Override
     public Query getQuery(CoralSession coralSession)
     throws Exception
     {
         return getCachedQuery(coralSession);
     }
 
     @Override
     public String getQueryString(CoralSession coralSession)
     {
         try
         {
             Query query = getCachedQuery(coralSession);
             return query.toString();
         }
         catch(Exception e)
         {
             return "";
         }
     }
 
     @Override
     public void setupTableState(TableState state)
     {
         super.setupTableState(state);
     }
 
     private Query getCachedQuery(CoralSession coralSession)
        throws Exception
     {
         if(query == null)
         {
             String range = parameters.get("range","ongoing");
             query = getQuery(coralSession, startDate, endDate, range, textQuery, acceptedSiteNames);
         }
         return query;
     }
 
     
     private Query getQuery(CoralSession coralSession, Date startDate, Date endDate, String range,
         String textQuery, String[] acceptedSiteNames)
         throws Exception
     {
         Analyzer analyzer = searchService.getAnalyzer(locale);
         BooleanQuery aQuery = new BooleanQuery();
 
         if(textQuery != null && textQuery.length() > 0)
         {
             QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_40, EXTENDED_FIELD_NAMES,
                 analyzer);
             parser.setDefaultOperator(QueryParser.AND_OPERATOR);
             parser.setDateResolution(DateTools.Resolution.SECOND);
 
             Query q = parser.parse(textQuery);
             aQuery.add(q, BooleanClause.Occur.MUST);
         }
 
         Term lowerEndDate = new Term("eventEnd", SearchUtil.dateToString(startDate));
         Term upperStartDate = new Term("eventStart", SearchUtil.dateToString(endDate));
         Term lowerStartDate = new Term("eventStart", SearchUtil.dateToString(startDate));
         Term upperEndDate = new Term("eventEnd", SearchUtil.dateToString(endDate));
 
 
         if(range.equals("ongoing"))
         {
             TermRangeQuery dateRange = TermRangeQuery.newStringRange(lowerEndDate.field(),
                 lowerEndDate.text(), null, true, true);
             TermRangeQuery dateRange2 = TermRangeQuery.newStringRange(lowerStartDate.field(), null,
                 upperStartDate.text(), true, true);
 
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
         else if(range.equals("in"))
         {
             TermRangeQuery dateRange = TermRangeQuery.newStringRange(lowerEndDate.field(),
                 lowerEndDate.text(), upperEndDate.text(), true, true);
             TermRangeQuery dateRange2 = TermRangeQuery.newStringRange(lowerStartDate.field(),
                 lowerStartDate
                 .text(), upperStartDate.text(), true, true);
 
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
         else if(range.equals("ending"))
         {
             TermRangeQuery dateRange = TermRangeQuery.newStringRange(lowerEndDate.field(),
                 lowerEndDate.text(), upperEndDate
                 .text(), true, true);
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
         }
         else if(range.equals("starting"))
         {
             TermRangeQuery dateRange2 = TermRangeQuery.newStringRange(lowerStartDate.field(),
                 lowerStartDate
                 .text(), upperStartDate.text(), true, true);
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
 
         aQuery.add(new BooleanClause(new TermQuery(new Term("titleCalendar",
             DocumentNodeResource.EMPTY_TITLE)), BooleanClause.Occur.MUST_NOT));
         
         if(acceptedSiteNames != null)
         {
             BooleanQuery sQuery = new BooleanQuery();
             for(String siteName : acceptedSiteNames)
             {
                 sQuery.add(new BooleanClause(new TermQuery(new Term(
                     SearchConstants.FIELD_SITE_NAME, siteName)), BooleanClause.Occur.SHOULD));
             }
             aQuery.add(new BooleanClause(sQuery, BooleanClause.Occur.MUST));
         }
 
         return aQuery;
     }
 
     @Override
     public String getErrorQueryString()
     {
         return "";
     }
 
     @Override 
     public SortField[] getSortFields()
     {
         if(parameters.isDefined("sort_field") && 
            parameters.isDefined("sort_order"))
         {
             if("closestEventStart".equals(parameters.get("sort_field", "")))
             {
                 SortField field2 = new SortField("eventStart", new ClosestEventFieldComparator(startDate, true), "desc".equals(parameters.get("sort_order","desc")));
                 return new SortField[] { field2 };
             }
             else
             {
                 return super.getSortFields();
             }
         }
         else
         {
            SortField field2 = new SortField(SearchConstants.FIELD_ID, SortField.LONG, "desc".equals("desc"));
             return new SortField[] { field2 };
         }
     }
     
     public void storeQueryParameters(TemplatingContext templatingContext)
     {
         super.storeQueryParameters(templatingContext);
         storeQueryParameter("range", templatingContext);
     }
     
     
     public class ClosestEventFieldComparator extends FieldComparatorSource
     {
         private ClosestDateParser parser;
         
         public ClosestEventFieldComparator(Date date, boolean evalEventsHigher)
         {
             this.parser = new ClosestDateParser(date.getTime(), evalEventsHigher);
         }
         
         public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
         throws IOException
         {
             return new DataLongComparator(numHits, fieldname, parser);
         }        
         
         public class DataLongComparator extends FieldComparator {
           private final long[] values;
           private long[] currentReaderValues;
           private final String field;
           private ClosestDateParser parser;
           private long bottom;
 
           DataLongComparator(int numHits, String field, FieldCache.Parser parser) {
             values = new long[numHits];
             this.field = field;
             this.parser = (ClosestDateParser) parser;
           }
 
           @Override
           public int compare(int slot1, int slot2) {
             // TODO: there are sneaky non-branch ways to compute
             // -1/+1/0 sign
             final long v1 = values[slot1];
             final long v2 = values[slot2];
             if (v1 > v2) {
               return 1;
             } else if (v1 < v2) {
               return -1;
             } else {
               return 0;
             }
           }
 
           @Override
           public int compareBottom(int doc) {
             // TODO: there are sneaky non-branch ways to compute
             // -1/+1/0 sign
             final long v2 = currentReaderValues[doc];
             if (bottom > v2) {
               return 1;
             } else if (bottom < v2) {
               return -1;
             } else {
               return 0;
             }
           }
 
           @Override
           public void copy(int slot, int doc) {
             values[slot] = currentReaderValues[doc];
           }
 
           @Override
           public void setNextReader(IndexReader reader, int docBase) throws IOException {
             currentReaderValues = FieldCache.DEFAULT.getLongs(reader, field, parser);
           }
           
           @Override
           public void setBottom(final int bottom) {
             this.bottom = values[bottom];
           }
 
           @Override
           public Comparable<?> value(int slot) {
             return Long.valueOf(values[slot]);
           }
         }
         
         public class ClosestDateParser
             implements LongParser
         {
             private static final long serialVersionUID = -9081043127213853682L;
             
             private Calendar selectedTime;
             private Calendar calendar;
             private boolean evalEventsHigher;
 
             public ClosestDateParser(Long selectedTime, boolean evalEventsHigher)
             {
                 this.selectedTime = java.util.Calendar.getInstance();
                 this.selectedTime.setTimeInMillis(selectedTime); 
                 this.selectedTime.set(java.util.Calendar.HOUR_OF_DAY, 0);
                 this.selectedTime.set(java.util.Calendar.MINUTE, 0);
                 this.selectedTime.set(java.util.Calendar.SECOND, 0);
                 this.selectedTime.set(java.util.Calendar.MILLISECOND, 0);
                 
                 this.calendar = java.util.Calendar.getInstance();
                 this.evalEventsHigher = evalEventsHigher;
             }
 
             public long parseLong(String string)
             {
                 Long result = 99999999999999999L;
                 try
                 {
                     Date date = SearchUtil.dateFromString(string);
                     
                     calendar.setTimeInMillis(date.getTime());
                     calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                     calendar.set(java.util.Calendar.MINUTE, 0);
                     calendar.set(java.util.Calendar.SECOND, 0);
                     calendar.set(java.util.Calendar.MILLISECOND, 0);
                     
                     result = Math.abs(selectedTime.getTimeInMillis() - calendar.getTimeInMillis());
                     // add converted time suffix to sort by event start time.
                     result += ((date.getTime() - calendar.getTimeInMillis())/8640); // divided by 1/1000 milisecunds.
                     
                     /* when absolute distance from events is the same
                      * as first set all events that started yet 
                      * then all that will start soon. 
                      */
                     if(evalEventsHigher == selectedTime.before(calendar))
                     { 
                         result+=10000;
                     }
                 }
                 catch(Exception e){}
                 return result;
             }
 
             @Override
             public boolean equals(Object o)
             {
                 if(o == null || !(o instanceof ClosestDateParser))
                 {
                     return false;
                 }
                 ClosestDateParser p = (ClosestDateParser)o;
                 return (p.selectedTime.getTimeInMillis() == selectedTime.getTimeInMillis())
                     && (p.evalEventsHigher == evalEventsHigher);
             }
             
             @Override
             public int hashCode()
             {
                 long t = selectedTime.getTimeInMillis();
                 return (int)(t ^ (t >>> 32) ^ (evalEventsHigher ? 0xFFFF : 0));
             }
             
             @Override
             public String toString()
             {
                 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                 return "ClosestDateParser(" + format.format(selectedTime.getTime()) + ", " + evalEventsHigher + ")";
             }
         }
     }
 }
