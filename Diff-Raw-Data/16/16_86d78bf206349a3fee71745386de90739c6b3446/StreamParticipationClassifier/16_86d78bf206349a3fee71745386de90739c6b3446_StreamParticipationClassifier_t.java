 package org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart;
 
 import java.io.Serializable;
 import java.util.List;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.hackystat.projectbrowser.page.projectportfolio.jaxb.Measures.Measure;
 import org.hackystat.projectbrowser.page.projectportfolio.jaxb.Measures.Measure.
         ParticipationParameters;
 
 /**
  * Classify stream into 3 categories: GOOD, AVERAGE and POOR, 
  * according to the participation of members.
  * 
  * @author Shaoxuan Zhang
  *
  */
 public class StreamParticipationClassifier implements Serializable, StreamClassifier {
 
   /** Support serialization. */
   private static final long serialVersionUID = 8335959874933854182L;
   /** Name of this classifier. */
   public static final String name = "Participation";
   /** The expected percentage of members that participated. */
   private double memberPercentage;
   /** The lowest value with which a member is consider as participating. */
   private double thresholdValue;
   /** The expected percentage of time that has events happened. */
   private double frequencyPercentage;
 
   /**
    * @param memberPercentage the {@link memberPercentage} to set.
    * @param thresholdValue the {@link thresholdValue} to set.
    * @param frequencyPercentage the {@link frequencyPercentage} to set.
    */
   public StreamParticipationClassifier(final double memberPercentage, final double thresholdValue, 
       final double frequencyPercentage) {
     this.memberPercentage = memberPercentage;
     this.thresholdValue = thresholdValue;
     this.frequencyPercentage = frequencyPercentage;
   }
   
   /**
    * Parse the given MiniBarChart and produce a StreamCategory result.
    * If there are more than {@link memberPercentage} members who contribute value higher 
    * than {@link thresholdValue} in more than {@link frequencyPercentage} of time, the stream will
    * be consider as GOOD.
    * 
    * If there are not enough active members to get a GOOD, but the merged stream satisfies 
    * the GOOD criteria except member percentage, the stream will be considered as AVERAGE.
    * 
    * Otherwise, it will consider as POOR.
    * @param chart the input chart
    * @return StreamCategory enumeration. 
    */
   public PortfolioCategory getStreamCategory(MiniBarChart chart) {
     int activeMember = 0;
     for (List<Double> stream : chart.streams) {
       if (isTheStreamGood(stream)) {
         activeMember++;
       }
     }
     if (activeMember > this.memberPercentage * chart.streams.size() / 100) {
       return PortfolioCategory.GOOD;
     }
     if (isTheStreamGood(chart.streamData)) {
       return PortfolioCategory.AVERAGE;
     }
     return PortfolioCategory.POOR;
   }
 
   /**
    * Check if the stream contains more than {@link frequencyPercentage} values that are greater
    * than {@link thresholdValue}.
    * @param stream the given stream
    * @return true or false.
    */
   private boolean isTheStreamGood(List<Double> stream) {
     int goodValueCount = 0;
     for (Double value : stream) {
       if (value >= this.thresholdValue) {
         goodValueCount++;
       }
     }
     if (goodValueCount >= this.frequencyPercentage / 100 * stream.size() ) {
       return true;
     }
     return false;
   }
 
   /**
    * @param memberPercentage the memberPercentage to set
    */
   public void setMemberPercentage(double memberPercentage) {
     this.memberPercentage = memberPercentage;
   }
 
   /**
    * @return the memberPercentage
    */
   public double getMemberPercentage() {
     return memberPercentage;
   }
 
   /**
    * @param thresholdValue the thresholdValue to set
    */
   public void setThresholdValue(double thresholdValue) {
     this.thresholdValue = thresholdValue;
   }
 
   /**
    * @return the thresholdValue
    */
   public double getThresholdValue() {
     return thresholdValue;
   }
 
   /**
    * @param frequencyPercentage the frequencyPercentage to set
    */
   public void setFrequencyPercentage(double frequencyPercentage) {
     this.frequencyPercentage = frequencyPercentage;
   }
 
   /**
    * @return the frequencyPercentage
    */
   public double getFrequencyPercentage() {
     return frequencyPercentage;
   }
 
   /**
    * Return the panel for users to configure this stream trend classifer.
    * User can customize higher, lower thresholds and if higher is better.
    * @param id The Wicket component id.
    * @return a Panel
    */
   public Panel getConfigurationPanel(String id) {
     return new StreamParticipationClassifierConfigurationPanel(id, this);
   }
 
   /**
    * Return the category of the given value.
   * If the value < 0, NA will be return. Otherwise, always return OTHER.
    * @param value the given value.
    * @return a {@link PortfolioCategory} result
    */
   public PortfolioCategory getValueCategory(double value) {
    if (value < 0) {
      return PortfolioCategory.NA;
    }
     return PortfolioCategory.OTHER;
   }
 
   /**
    * @return the name of this classifier.
    */
   public String getName() {
     return name;
   }
 
   /**
    * Save classifier's setting into the given {@link Measure} instance.
    * @param measure the given {@link Measure} instance
    */
   public void saveSetting(Measure measure) {
     ParticipationParameters param = new ParticipationParameters();
     param.setFrequencyPercentage(frequencyPercentage);
     param.setMemberPercentage(memberPercentage);
     param.setThresoldValue(thresholdValue);
     measure.setParticipationParameters(param);
   }
 }
