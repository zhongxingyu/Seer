 package net.cyklotron.cms.documents.internal;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FieldCache;
 import org.apache.lucene.search.FieldComparator;
 import org.apache.lucene.search.FieldComparatorSource;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TermRangeQuery;
 import org.apache.lucene.search.FieldCache.LongParser;
 import org.apache.lucene.util.Version;
 import org.jcontainer.dna.Logger;
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
     private Logger log;
     private Date startDate;
     private Date endDate;
     
     private String[] fieldNames;
     private Query query;
     private String textQuery;
     
     public CalendarEventsSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Logger log,
         Date startDate,
         Date endDate)
     {
         super(searchService, parameters, locale);
         this.startDate = startDate;
         this.endDate = endDate;
         this.log = log;
     }
     
     public CalendarEventsSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Logger log,
         Date startDate,
         Date endDate, 
         String textQuery)
     {
         this(searchService, parameters, locale, log, startDate, endDate);
         this.textQuery = textQuery;
     }
 
     @Override
     public Query getQuery(CoralSession coralSession)
     throws Exception
     {
         return getQuery(coralSession, getFieldNames());
     }
 
     @Override
     public String getQueryString(CoralSession coralSession)
     {
         try
         {
             Query query = getQuery(coralSession, getFieldNames());
             return query.toString();
         }
         catch(Exception e)
         {
             return "";
         }
     }
 
     private String[] getFieldNames()
     {
         if(fieldNames == null)
         {
             fieldNames = DEFAULT_FIELD_NAMES;
             String qField = parameters.get("field","any");
             if(!qField.equals("any"))
             {
                 fieldNames = new String[1];
                 fieldNames[0] = qField;
             }
         }
         return fieldNames;
     }
 
     @Override
     public void setupTableState(TableState state)
     {
         super.setupTableState(state);
     }
 
     private Query getQuery(CoralSession coralSession, String[] fieldNames)
        throws Exception
     {
         if(query == null)
         {
             String range = parameters.get("range","ongoing");
             query = getQuery(coralSession, startDate, endDate, range, textQuery);
         }
         return query;
     }
 
     
     private Query getQuery(CoralSession coralSession, Date startDate, Date endDate, String range, String textQuery)
         throws Exception
     {
         Analyzer analyzer = searchService.getAnalyzer(locale);
         BooleanQuery aQuery = new BooleanQuery();
 
         if(textQuery.length() > 0)
         {
             QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, DEFAULT_FIELD_NAMES,
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
             TermRangeQuery dateRange = new TermRangeQuery(lowerEndDate.field(), lowerEndDate.text(), null, true, true);
             TermRangeQuery dateRange2 = new TermRangeQuery(lowerStartDate.field(), null, upperStartDate.text(), true, true);
 
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
         else if(range.equals("in"))
         {
             TermRangeQuery dateRange = new TermRangeQuery(lowerEndDate.field(),
                 lowerEndDate.text(), upperEndDate.text(), true, true);
             TermRangeQuery dateRange2 = new TermRangeQuery(lowerStartDate.field(), lowerStartDate
                 .text(), upperStartDate.text(), true, true);
 
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
         else if(range.equals("ending"))
         {
             TermRangeQuery dateRange = new TermRangeQuery(lowerEndDate.field(),
                 lowerEndDate.text(), upperEndDate
                 .text(), true, true);
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
         }
         else if(range.equals("starting"))
         {
             TermRangeQuery dateRange2 = new TermRangeQuery(lowerStartDate.field(), lowerStartDate
                 .text(), upperStartDate.text(), true, true);
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
 
         aQuery.add(new BooleanClause(new TermQuery(new Term("titleCalendar",
             DocumentNodeResource.EMPTY_TITLE)), BooleanClause.Occur.MUST_NOT));
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
                SortField field2 = new SortField("eventStart", new ClosestEventStartFieldComparator(startDate), "desc".equals("desc"));
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
         storeQueryParameter("field", templatingContext);
         storeQueryParameter("range", templatingContext);
     }
     
     
     public class ClosestEventStartFieldComparator extends FieldComparatorSource
     {
         private ClosestDateParser parser;
         
         public ClosestEventStartFieldComparator(Date date)
         {
             this.parser = new ClosestDateParser(date.getTime());
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
           public Comparable value(int slot) {
             return Long.valueOf(values[slot]);
           }
         }
         
         public class ClosestDateParser
             implements LongParser
         {
             private Long selectedTime;
             Calendar calendar;
 
             public ClosestDateParser(Long selectedTime)
             {
                 this.calendar = java.util.Calendar.getInstance();
                 calendar.setTimeInMillis(selectedTime);
                 calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                 calendar.set(java.util.Calendar.MINUTE, 0);
                 calendar.set(java.util.Calendar.SECOND, 0);
                 calendar.set(java.util.Calendar.MILLISECOND, 0);
                 
                 this.selectedTime = calendar.getTimeInMillis();
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
 
                     result = Math.abs(selectedTime - date.getTime());
                 }
                 catch(Exception e){}
                 return result;
             }
 
             protected Object readResolve()
             {
                 return new ClosestDateParser(selectedTime);
             }
 
             public String toString()
             {
                 return FieldCache.class.getName() + ".CLOSEST_DATE_PARSER";
             }
         }
     }
 }
