 /**
  * This file is part of Project Control Center (PCC).
  * 
  * PCC (Project Control Center) project is intellectual property of 
  * Dmitri Anatol'evich Pisarenko.
  * 
  * Copyright 2010, 2011 Dmitri Anatol'evich Pisarenko
  * All rights reserved
  *
  **/
 
 package co.altruix.injectorfactory;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import ru.altruix.commons.api.di.InjectorFactory;
 
 import at.silverstrike.pcc.api.gtaskexporter.GoogleTasksExporterFactory;
import at.silverstrike.pcc.api.gtaskprioritycalculator.GoogleTasksPriorityCalculatorFactory;
 import at.silverstrike.pcc.api.gtaskrelevance2.RelevantTaskSetCalculatorFactory;
 import at.silverstrike.pcc.api.tj3bookingsparser.BookingsFile2BookingsFactory;
 import co.altruix.pcc.api.booking2calendarevententry.Booking2CalendarEventEntryConverterFactory;
 import co.altruix.pcc.api.calendarevent2pcceventconverter.CalendarEventEntry2PccEventConverterFactory;
 import co.altruix.pcc.api.eventexporter.EventExporterFactory;
 import co.altruix.pcc.api.existingeventsfilter.ExistingEventsFilterFactory;
 import co.altruix.pcc.api.exporter2googlecalendar.Exporter2GoogleCalendarFactory;
 import co.altruix.pcc.api.gcaleventimporter.GoogleCalendarEventImporterFactory;
 import co.altruix.pcc.api.outgoingqueuechannel.OutgoingQueueChannelFactory;
 import co.altruix.pcc.impl.di.DefaultPccWorkerInjectorFactory;
 
 import com.google.inject.ConfigurationException;
 import com.google.inject.Injector;
 
 /**
  * @author DP118M
  * 
  */
 public final class TestDefaultInjectorFactory {
     @Test
     public void test() {
         final InjectorFactory injectorFactory =
                 new DefaultPccWorkerInjectorFactory();
         final Injector injector = injectorFactory.createInjector();
 
         Assert.assertNotNull(injector);
 
         try {
             injector.getInstance(OutgoingQueueChannelFactory.class);
             injector.getInstance(Exporter2GoogleCalendarFactory.class);
             injector.getInstance(GoogleTasksExporterFactory.class);
             injector.getInstance(RelevantTaskSetCalculatorFactory.class);
             injector.getInstance(ExistingEventsFilterFactory.class);
             injector.getInstance(GoogleCalendarEventImporterFactory.class);
             injector.getInstance(CalendarEventEntry2PccEventConverterFactory.class);
             injector.getInstance(EventExporterFactory.class);
            injector.getInstance(GoogleTasksPriorityCalculatorFactory.class);
             final Booking2CalendarEventEntryConverterFactory test =
                     injector.getInstance(Booking2CalendarEventEntryConverterFactory.class);
             
             Assert.assertNotNull(test);
             
             injector.getInstance(BookingsFile2BookingsFactory.class);
         } catch (final ConfigurationException exception) {
             Assert.fail(exception.getMessage());
         } catch (final ClassCastException exception) {
             Assert.fail(exception.getMessage());
         }
     }
 }
