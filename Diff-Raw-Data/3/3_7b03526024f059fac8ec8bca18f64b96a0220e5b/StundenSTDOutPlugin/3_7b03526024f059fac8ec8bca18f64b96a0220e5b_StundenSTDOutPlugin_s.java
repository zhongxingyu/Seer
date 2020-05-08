 package nu.wasis.stunden.plugins;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import nu.wasis.stunden.model.Day;
 import nu.wasis.stunden.model.Entry;
 import nu.wasis.stunden.model.WorkPeriod;
 import nu.wasis.stunden.plugin.OutputPlugin;
 import nu.wasis.stunden.plugins.stdout.config.StundenSTDOutPluginConfig;
 import nu.wasis.stunden.util.DateUtils;
 
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 
 @PluginImplementation
 public class StundenSTDOutPlugin implements OutputPlugin {
 
     // private static final Logger LOG = Logger.getLogger(StundenSTDOutPlugin.class);
 
     private static final void p(final String message) {
         System.out.println(message);
     }
 
     @Override
     public void output(final WorkPeriod workPeriod, final Object config) {
         List<String> noWork = null;
         if (null != config) {
             final StundenSTDOutPluginConfig myConfig = (StundenSTDOutPluginConfig) config;
             noWork = myConfig.getNoWork();
         }
         p("Start of period\t: " + workPeriod.getBegin().toString(DateUtils.DATE_FORMATTER));
         p("End of period\t: " + workPeriod.getEnd().toString(DateUtils.DATE_FORMATTER));
         p("============================");
 
         if (workPeriod.getDays().isEmpty()) {
             p("[Period contains no entries.]");
             return;
         }
 
         for (final Day day : workPeriod.getDays()) {
             p("");
             p(day.getDate().toString(DateUtils.DATE_FORMATTER));
             p("==========");
             final Map<String, Duration> durations = new HashMap<>();
             Duration totalDuration = null;
             for (final Entry entry : day.getEntries()) {
                p(entry.getBegin().toString(DateUtils.TIME_FORMATTER) + " - " + entry.getEnd().toString(DateUtils.TIME_FORMATTER) + ": "
                  + entry.getProject().getName());
                 final Duration newDuration = new Interval(entry.getBegin(), entry.getEnd()).toDuration();
                 if (null == totalDuration) {
                     totalDuration = new Duration(newDuration);
                 } else {
                     if (!noWork.contains(entry.getProject().getName())) {
                         totalDuration = totalDuration.plus(newDuration);
                     }
                 }
                 if (!durations.containsKey(entry.getProject().getName())) {
                     durations.put(entry.getProject().getName(), newDuration);
                 } else {
                     final Duration originalPeriod = durations.get(entry.getProject().getName());
                     durations.put(entry.getProject().getName(), originalPeriod.plus(newDuration));
                 }
             }
             p("Summary:");
             for (final Map.Entry<String, Duration> entry : durations.entrySet()) {
                 p("\t" + entry.getKey() + ": " + entry.getValue().toPeriod().toString(DateUtils.PERIOD_FORMATTER));
             }
             p("\tTotal: " + totalDuration.toPeriod().normalizedStandard().toString(DateUtils.PERIOD_FORMATTER));
         }
     }
 
     @Override
     public Class<StundenSTDOutPluginConfig> getConfigurationClass() {
         return StundenSTDOutPluginConfig.class;
     }
 }
