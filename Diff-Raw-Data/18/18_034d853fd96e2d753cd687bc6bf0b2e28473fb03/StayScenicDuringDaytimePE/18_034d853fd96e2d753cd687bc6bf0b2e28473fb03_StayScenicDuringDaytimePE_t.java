 package org.s4digester.tourist.pe;
 
 import com.google.gson.Gson;
 import org.apache.commons.collections.functors.InstantiateFactory;
 import org.apache.commons.collections.map.DefaultedMap;
 import org.apache.commons.collections.map.LazyMap;
 import org.apache.s4.core.ProcessingElement;
 import org.apache.s4.core.Stream;
 import org.s4digester.tourist.event.NextMillOfDayUpdateEvent;
 import org.s4digester.tourist.event.SignalingEvent;
 import org.s4digester.tourist.event.StayScenicDuringDaytimeEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.s4digester.tourist.util.TimeUtil.getAge;
 import static org.s4digester.tourist.util.TimeUtil.getMillOfToday;
 
 /**
  * 白天，[08:00,18:00]在景区停留超过3个小时的用户的PE，满足条件就发出event，可以对同一个用户重复发送。
  * 1. 如果用户14点进入景区， 20点离开。如何在18:00时判断用户符合条件？
  * 对所有用户的信令取最新的时间，如果最新时间为18:00时，触发检测
  */
 public class StayScenicDuringDaytimePE extends ProcessingElement {
     private Logger logger = LoggerFactory.getLogger(getClass());
     private Map<String, SingleImsiProcessor> processorMap = LazyMap.decorate(new HashMap<String, SingleImsiProcessor>(), InstantiateFactory.getInstance(SingleImsiProcessor.class, new Class[0], new Object[0]));
     private Stream<StayScenicDuringDaytimeEvent>[] streams;
     //如果用户14点进入景区， 20点离开。如何在18:00时判断用户符合条件？
     // 因此，对于所有信令，一旦到达或超过18:00，就说统计周期变更，需要检测用户是不是符合条件了
     //这个变量保存最近的18点属于哪一天。比如，最新event17号17点，则值为17号。如果为17号19点，则为18号
     private long next18Age;
     private Stream<NextMillOfDayUpdateEvent>[] nextMillOfDayUpdateEventStreams;
 
     @Override
     protected void onCreate() {
         logger.info("create StayScenicDuringDaytimePE");
     }
 
     @Override
     protected void onRemove() {
         logger.info("remove StayScenicDuringDaytimePE");
     }
 
     public void setStreams(Stream<StayScenicDuringDaytimeEvent>... streams) {
         this.streams = streams;
     }
 
     public void onEvent(SignalingEvent event) {
         if (logger.isTraceEnabled()) {
             logger.trace("receive Signaling:{}", new Gson().toJson(event));
         }
         long eventAge18 = getAge18(event.getTime());
         if (eventAge18 > next18Age) {
             if (logger.isTraceEnabled()) {
                 logger.trace("new age:[{} - {}]", next18Age, eventAge18);
             }
             NextMillOfDayUpdateEvent nextMillOfDayUpdateEvent = new NextMillOfDayUpdateEvent();
             nextMillOfDayUpdateEvent.setAge(eventAge18);
             nextMillOfDayUpdateEvent.setMillOfDay(18 * 60 * 60 * 1000);
             emit(nextMillOfDayUpdateEvent, nextMillOfDayUpdateEventStreams);
         }
         SingleImsiProcessor processor = processorMap.get(event.getImsi());
         StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = null;
         stayScenicDuringDaytimeEvent = processor.check(event);
         if (stayScenicDuringDaytimeEvent != null) {
             if (logger.isTraceEnabled()) {
                 logger.trace("emit event: {}", new Gson().toJson(stayScenicDuringDaytimeEvent));
             }
             emit(stayScenicDuringDaytimeEvent, streams);
         }
     }
 
     public static long getAge18(long eventTime) {
        try {
            System.out.println(Calendar.getInstance().get(Calendar.ZONE_OFFSET));
            System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("1970-01-01 00:00:00").getTime());
            System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(0)));
            System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("1970-01-01 08:00:00").getTime());
        } catch (ParseException e) {
        }
         long age = getAge(eventTime);
         long millOfToday = getMillOfToday(eventTime);
         return millOfToday <= 18 * 60 * 60 * 1000 ? age : age + 1;
     }
 
    public static void main(String[] args) {
        System.out.println( getAge18(1352686915296L));
        System.out.println( getAge18(1352686915342L));
    }

     public void onEvent(NextMillOfDayUpdateEvent event) {
         if (event.getMillOfDay() == 18 * 60 * 60 * 1000) {
             if (next18Age != event.getAge()) {
                 next18Age = event.getAge();
                 //每天18点定时执行，如果用户最后一次信令在景区，并且直到18点都没有信令，则认为用户一直在。
                 for (Map.Entry<String, SingleImsiProcessor> entry : processorMap.entrySet()) {
                     StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = entry.getValue().forceCheck(entry.getKey(), event.getAge() * 24 * 60 * 60 * 1000 + event.getMillOfDay(), entry.getValue().lastStatus.isInside);
                     if (stayScenicDuringDaytimeEvent != null) {
                         emit(stayScenicDuringDaytimeEvent, streams);
                     }
                 }
             }
         }
     }
 
     public void setNextMillOfDayUpdateEventStreams(Stream<NextMillOfDayUpdateEvent>... nextMillOfDayUpdateEventStreams) {
         this.nextMillOfDayUpdateEventStreams = nextMillOfDayUpdateEventStreams;
     }
 
 
     /**
      * TODO: 需要考虑Event乱序到达的情况
      */
     public static class SingleImsiProcessor {
         private Logger logger = LoggerFactory.getLogger(getClass());
         private Status lastStatus = new Status();
 
         public SingleImsiProcessor() {
         }
 
         /**
          * 强制检查是否复合条件。如果当前还不符合3个小时，但还在景区，而且到18:00不离开就满3个小时，也算符合条件。
          *
          * @param imsi
          * @return
          */
         public StayScenicDuringDaytimeEvent forceCheck(String imsi, long eventTime, boolean inside) {
             synchronized (lastStatus) {
                 if (lastStatus.stayTimeOfToday >= 3 * 60 * 60 * 1000 && lastStatus.isInside && (lastStatus.stayTimeOfToday + (18 * 60 * 60 * 1000 - getMillOfToday(lastStatus.eventTime))) >= 3 * 60 * 60 * 1000) {
                     StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = new StayScenicDuringDaytimeEvent();
                     stayScenicDuringDaytimeEvent.setAge(getAge(lastStatus.eventTime));
                     stayScenicDuringDaytimeEvent.setImsi(imsi);
                     lastStatus.eventTime = eventTime;
                     lastStatus.isInside = inside;
                     lastStatus.stayTimeOfToday = 0;
                     return stayScenicDuringDaytimeEvent;
                 }
             }
             return null;
         }
 
         public StayScenicDuringDaytimeEvent check(SignalingEvent event) {
             boolean isInsideNow = isInside(event);
             synchronized (lastStatus) {
                 if (isNewCircle(lastStatus.eventTime, event.getTime())) { //如果是新的统计周期，则清空
                     if (logger.isTraceEnabled()) {
                         logger.trace("new age");
                     }
                     //首先判断老的周期是不是复合条件
                     StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = forceCheck(event.getImsi(), event.getTime(), isInsideNow);
                     return stayScenicDuringDaytimeEvent;
                 } else {
                     if (!lastStatus.isInside && !isInsideNow) {//一直不在景区
                         lastStatus.eventTime = event.getTime();
                     } else if (lastStatus.isInside && isInsideNow) { //一直在景区
                         lastStatus.stayTimeOfToday += (event.getTime() - lastStatus.eventTime);
                         lastStatus.eventTime = event.getTime();
                     } else if (!lastStatus.isInside && isInsideNow) { //新进入景区
                         lastStatus.eventTime = event.getTime();
                         lastStatus.isInside = isInsideNow;
                     } else if (lastStatus.isInside && !isInsideNow) { //新离开景区
                         lastStatus.stayTimeOfToday += (event.getTime() - lastStatus.eventTime);
                         lastStatus.eventTime = event.getTime();
                         lastStatus.isInside = isInsideNow;
                     }
                     if (logger.isTraceEnabled()) {
                         logger.trace("imsi[{}] stayTimeOfToday[{}]", event.getImsi(), lastStatus.stayTimeOfToday);
                     }
                     if (lastStatus.stayTimeOfToday > 3 * 60 * 60 * 1000) {
                         StayScenicDuringDaytimeEvent stayScenicDuringDaytimeEvent = new StayScenicDuringDaytimeEvent();
                         stayScenicDuringDaytimeEvent.setAge(getAge(lastStatus.eventTime));
                         stayScenicDuringDaytimeEvent.setImsi(event.getImsi());
                         return stayScenicDuringDaytimeEvent;
                     } else {
                         return null;
                     }
                 }
 
             }
         }
 
 
         private boolean isNewCircle(long lastTime, long time) {
             return getAge18(lastTime) == getAge18(time);
         }
 
         private boolean isInside(SignalingEvent event) {
             //TODO: 需要根据知识库获取
             return ("tourist".equals(event.getCell()));
         }
     }
 
     private static class Status {
         private boolean isInside = false;
         private long eventTime = -1;
         private long stayTimeOfToday = 0;
     }
 
 }
