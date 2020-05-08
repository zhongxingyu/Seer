 package org.motechproject.ghana.national.messagegateway.domain;
 
 import org.joda.time.LocalDateTime;
 import org.junit.Test;
 import org.motechproject.model.Time;
 import org.motechproject.util.DateUtil;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class NextMondayDispatcherTest {
 
     @Test
     public void shouldReturnDateOfUpComingMondayGiveADate(){
         LocalDateTime generationTime = DateUtil.newDateTime(DateUtil.newDate(2012, 2, 27), new Time(10, 10)).toLocalDateTime();
         LocalDateTime returnedNextMondayDate = new NextMondayDispatcher().deliveryDate(SMS.fromText("", "", generationTime, null, null));
        LocalDateTime expectedNextMondayDate = DateUtil.newDateTime(DateUtil.newDate(2012, 3, 5), new Time(0, 0)).toLocalDateTime();
         assertThat(returnedNextMondayDate, is(equalTo(expectedNextMondayDate)));
 
         generationTime = DateUtil.newDateTime(DateUtil.newDate(2012, 2, 29), new Time(10, 10)).toLocalDateTime();
         returnedNextMondayDate = new NextMondayDispatcher().deliveryDate(SMS.fromText("", "", generationTime, null, null));
        expectedNextMondayDate = DateUtil.newDateTime(DateUtil.newDate(2012, 3, 5), new Time(0, 0)).toLocalDateTime();
         assertThat(returnedNextMondayDate, is(equalTo(expectedNextMondayDate)));
     }
 }
