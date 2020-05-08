 package com.edge.twitter_research.relevance_filter;
 
 import org.apache.log4j.Logger;
 
 import com.edge.twitter_research.core.*;
 
 import org.jsoup.Jsoup;
 
 import cmu.arktweetnlp.Tagger;
 import cmu.arktweetnlp.Tagger.TaggedToken;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.regex.Pattern;
 
 
 public class TweetFeatureVectorGenerator {
 
     private enum TimeOfDay{
         REALLY_LATE_NIGHT(1),
         DAWN(2),
         EARLY_MORNING(3),
         LATE_MORNING(4),
         AFTERNOON(5),
         EVENING(6),
         NIGHT(7),
         LATE_NIGHT(8);
 
         private final int timeOfDay;
         TimeOfDay(int timeOfDay){this.timeOfDay = timeOfDay;}
         public int getTimeOfDay(){return timeOfDay;}
     }
 
 
     private enum DayOfWeek{
         WEEKDAY(1),
         WEEKEND(2);
 
         private final int dayOfWeek;
         DayOfWeek(int dayOfWeek){this.dayOfWeek = dayOfWeek;}
         public int getDayOfWeek(){return dayOfWeek;}
     }
 
 
     private enum UserVerification{
         VERIFIED(1),
         NOT_VERIFIED(2);
 
         private final int verified;
         UserVerification(int verified){this.verified = verified;}
         public int getVerified(){return verified;}
     }
 
 
     private enum URLLocation{
         FRONT(1),
         MIDDLE(2),
         END(3),
         NOT_PRESENT(4);
 
         private final int URLLocation;
         URLLocation(int URLLocation){this.URLLocation = URLLocation;}
         public int getURLLocation(){return URLLocation;}
     }
 
 
     private enum ContributorStatus{
         HAS_CONTRIBUTOR(1),
         NO_CONTRIBUTOR(2);
 
         private final int contributor;
         ContributorStatus(int contributor){this.contributor = contributor;}
         public int getContributor(){return contributor;}
     }
 
 
     private enum TweetType{
         PLAIN(1),
         RETWEET(2),
         REPLY(3);
 
         private final int tweetType;
         TweetType(int tweetType){this.tweetType = tweetType;}
         public int getTweetType(){return tweetType;}
     }
 
 
     private enum TweetNature{
         SENSITIVE(1),
         NOT_SENSITIVE(2);
 
         private final int tweetNature;
         TweetNature(int tweetNature){this.tweetNature = tweetNature;}
         public int getTweetNature(){return tweetNature;}
     }
 
 
     private enum ContentType{
         QUESTION(1),
         PLAIN(2);
 
         private final int contentType;
         ContentType(int contentType){this.contentType = contentType;}
         public int getContentType(){return contentType;}
     }
 
 
     private enum TweetSource{
         WEB(1),
         MOBILE(2);
 
         private final int tweetSource;
         TweetSource(int tweetSource){this.tweetSource = tweetSource;}
         public int getTweetSource(){return tweetSource;}
     }
 
 
     private enum POSTag{
         COMMON_NOUN,
         PRONOUN,
         PROPER_NOUN,
         NOMINAL_POSSESSIVE,
         PROPER_NOUN_POSSESSIVE,
         VERB,
         ADJECTIVE,
         ADVERB,
         INTERJECTION,
         DETERMINER,
         PRE_POST_POSITION,
         COORDINATING_CONJUNCTION,
         VERB_PARTICLE,
         EXISTENTIAL_THERE,
         NUMERAL,
         PUNCTUATION,
         GENERIC,
         NOMINAL_VERBAL,
         PROPER_NOUN_VERBAL,
         EXISTENTIAL_VERBAL;
     }
 
 
     private Logger logger;
     private Pattern emoticonMatchPattern;
     private Pattern mobileSourceMatchPattern;
     private DecimalFormat fractionFormat;
     private SimpleDateFormat twitterDateFormat;
     private String tweetText;
     private Tagger tweetPOStagger;
 
 
 
     private TimeOfDay timeOfDay;
     private DayOfWeek dayOfWeek;
     private URLLocation urlLocationInTweet;
     private long retweetCount;
     private int numCharsInTweet;
     private int numEmoticonsInTweet;
     private ContributorStatus hasContributors;
     private TweetType tweetType;
     private TweetNature tweetNature;
     private ContentType contentType;
     private TweetSource source;
     private double[] componentFractions;
     private long userFriends;
     private long userFollowers;
     private int userTotalTweets;
     private UserVerification userIsVerified;
     private double[] POSFractions;
 
 
     public TweetFeatureVectorGenerator() throws IOException{
         logger = Logger.getLogger(TweetFeatureVectorGenerator.class);
 
         twitterDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
 
         mobileSourceMatchPattern = Pattern.compile("(\\Qobile\\E|\\QiOS\\E|\\Qapp\\E|\\QApp\\E|\\QPlayStation\\E|" +
                 "\\Qokia\\E|\\Qnstag\\E|\\Qnap\\E|\\Qstream\\E|\\Qfon\\E|\\Qnette\\E|\\Qrite\\E|\\Qwicc\\E|\\Qweetlog\\E|" +
                 "\\Qterous\\E|\\Qastin\\E)");
 
         emoticonMatchPattern = Pattern.compile("(\\Q:)\\E|\\Q:D\\E|\\Q:(\\E|\\Q;)\\E|\\Q:-)\\E|\\Q:P\\E|\\Q=)" +
                 "\\E|\\Q(:\\E|\\Q;-)\\E|\\Q:/\\E|\\QXD\\E|\\Q=D\\E|\\Q=]\\E|\\QD:\\E|\\Q;D\\E|\\Q:]\\E|\\Q:-(\\E|\\Q=/\\E|" +
                 "\\Q:O\\E|\\Q=(\\E|\\Q):\\E|\\Q=P\\E|\\Q:'(\\E|\\Q:|\\E|\\Q:-D\\E|\\Q^_^\\E|\\Q(8\\E|\\Q:-/\\E|\\Q:o)\\E|" +
                 "\\Q:o\\E|\\Q:-P\\E|\\Q(;\\E|\\Q;P\\E|\\Qo:\\E|\\Q;]\\E|\\Q:@\\E|\\Q=[\\E|\\Q:\\\\E|\\Q;(\\E|\\Q:[\\E|\\Q8)" +
                 "\\E|\\Q;o)\\E|\\Q=\\\\E|\\Q=O\\E|\\Q(=\\E|\\Q[:\\E|\\Q;/\\E|\\Q8D\\E|\\Q:}\\E|\\Q\\m/\\E|\\QO:\\E|\\Q/:\\E|" +
                 "\\Q;O\\E|\\Q^-^\\E|\\Q8-)\\E|\\Q=|\\E|\\Q]:\\E|\\QD;\\E|\\Q:o(\\E|\\Q|:\\E|\\Q;-P\\E|\\Q);\\E|\\Q=o\\E|\\Q;-D" +
                 "\\E|\\Q:-\\\\E|\\Q(^_^)\\E|\\Q:-O\\E|\\Q:-o\\E|\\QD=\\E|\\Q(^_^;)\\E|\\Q;o\\E|\\Q;-(\\E|\\Q;@\\E|\\QP:\\E|" +
                 "\\Q@:\\E|\\Q:-|\\E|\\Q[=\\E|\\Q(^-^)\\E|\\Q[8\\E|\\Q(T_T)\\E|\\Q(-_-)\\E|\\Q(-:\\E|\\Q)=\\E|\\Q:{\\E|\\Q=}" +
                 "\\E|\\Q[;\\E|\\Q:?\\E|\\Q8-]\\E|\\Q:*(\\E|\\Qo;\\E|\\QD8\\E|\\Q;}\\E|\\Q;[\\E|\\Q:o/\\E|\\Q:oP\\E|\\Q:-]\\E|" +
                 "\\Q:oD\\E|\\Q8/\\E|\\Q8(\\E|\\Qo(^-^)o\\E)");
 
         componentFractions = new double[5];
 
         fractionFormat = new DecimalFormat("#.00000");
 
         tweetPOStagger = new Tagger();
         tweetPOStagger.loadModel(Constants.POS_TAGGING_MODEL_FILE_PATH);
 
         POSFractions = new double[20];
     }
 
 
     public String getFeatureVector(SimpleTweet simpleTweet){
         try{
             tweetText = simpleTweet.getText().toString();
         }catch (NullPointerException nullPointerException){
             logger.warn("Tweet text is null", nullPointerException);
             tweetText = "";
         }
 
         userFriends = simpleTweet.getUser().getFriendsCount();
 
         userFollowers = simpleTweet.getUser().getFollowersCount();
 
         userTotalTweets = simpleTweet.getUser().getStatusesCount();
 
         userIsVerified = simpleTweet.getUser().getVerified() ?
                 UserVerification.VERIFIED : UserVerification.NOT_VERIFIED;
 
         Date tweetDate = getDateObject(simpleTweet);
         dayOfWeek = getDayOfWeek(tweetDate);
         timeOfDay = getTimeOfDay(tweetDate);
 
         setComponentFractions(simpleTweet);
 
         urlLocationInTweet = getUrlLocationInTweet(simpleTweet);
 
         retweetCount = simpleTweet.getRetweetCount();
 
         numCharsInTweet = tweetText.length();
 
         hasContributors = simpleTweet.getContributors().isEmpty() ?
                 ContributorStatus.NO_CONTRIBUTOR : ContributorStatus.HAS_CONTRIBUTOR;
 
         tweetType = determineTweetType(simpleTweet);
 
         tweetNature = simpleTweet.getIsPossiblySensitive() ?
                 TweetNature.SENSITIVE : TweetNature.NOT_SENSITIVE;
 
         contentType = tweetText.contains("?") ?
                 ContentType.QUESTION : ContentType.PLAIN;
 
         numEmoticonsInTweet = emoticonCount();
 
         source = determineTweetSource(simpleTweet);
 
         setPOSFractions();
 
         return featureVectorToString();
     }
 
 
     private void setPOSFractions(){
 
         HashMap<Character, Integer> perTagOccurenceCount = new HashMap<Character, Integer>();
         List<TaggedToken> taggedTokenList = tweetPOStagger.tokenizeAndTag(tweetText);
 
         double validTags = 0;
         for (TaggedToken taggedToken : taggedTokenList ){
             char tag = taggedToken.tag.charAt(0);
             if ((tag == '#') || (tag == '@') || (tag == '~') ||
                     (tag == 'U') || (tag == 'E'))
                 continue;
             validTags++;
 
             if (perTagOccurenceCount.containsKey(tag)){
                 perTagOccurenceCount.put(tag,
                         perTagOccurenceCount.get(tag) + 1);
             }else{
                 perTagOccurenceCount.put(tag, 1);
             }
         }
 
         for (Map.Entry<Character, Integer> tag : perTagOccurenceCount.entrySet()){
             switch (tag.getKey()){
                 case 'N':
                     POSFractions[POSTag.COMMON_NOUN.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'O':
                     POSFractions[POSTag.PRONOUN.ordinal()] = tag.getValue()/validTags;
                     break;
                 case '^':
                     POSFractions[POSTag.PROPER_NOUN.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'S':
                     POSFractions[POSTag.NOMINAL_POSSESSIVE.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'Z':
                     POSFractions[POSTag.PROPER_NOUN_POSSESSIVE.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'V':
                     POSFractions[POSTag.VERB.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'A':
                     POSFractions[POSTag.ADJECTIVE.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'R':
                     POSFractions[POSTag.ADVERB.ordinal()] = tag.getValue()/validTags;
                     break;
                 case '!':
                     POSFractions[POSTag.INTERJECTION.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'D':
                     POSFractions[POSTag.DETERMINER.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'P':
                     POSFractions[POSTag.PRE_POST_POSITION.ordinal()] = tag.getValue()/validTags;
                     break;
                 case '&':
                     POSFractions[POSTag.COORDINATING_CONJUNCTION.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'T':
                     POSFractions[POSTag.VERB_PARTICLE.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'X':
                     POSFractions[POSTag.EXISTENTIAL_THERE.ordinal()] = tag.getValue()/validTags;
                     break;
                 case '$':
                     POSFractions[POSTag.NUMERAL.ordinal()] = tag.getValue()/validTags;
                     break;
                 case ',':
                     POSFractions[POSTag.PUNCTUATION.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'G':
                     POSFractions[POSTag.GENERIC.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'L':
                     POSFractions[POSTag.NOMINAL_VERBAL.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'M':
                     POSFractions[POSTag.PROPER_NOUN_VERBAL.ordinal()] = tag.getValue()/validTags;
                     break;
                 case 'Y':
                     POSFractions[POSTag.EXISTENTIAL_VERBAL.ordinal()] = tag.getValue()/validTags;
                     break;
                 default:
                     logger.error("Unknown tag");
                     break;
             }
         }
     }
 
 
 
     private TweetType determineTweetType(SimpleTweet simpleTweet){
         if (simpleTweet.getIsRetweet())
             return TweetType.RETWEET;
         else if (simpleTweet.getInReplyToScreenName() != null)
             return TweetType.REPLY;
         else
             return TweetType.PLAIN;
     }
 
 
     private URLLocation getUrlLocationInTweet(SimpleTweet simpleTweet){
         if (simpleTweet.getUrlEntities().isEmpty()){
             return URLLocation.NOT_PRESENT;
         }
 
         SimpleURLEntity firstURL = simpleTweet.getUrlEntities().get(0);
         if (firstURL.getStart() == 0)
             return URLLocation.FRONT;
         else if (firstURL.getEnd() == tweetText.length() - 1)
             return URLLocation.END;
         else
             return URLLocation.MIDDLE;
     }
 
 
     private TweetSource determineTweetSource(SimpleTweet simpleTweet){
         String cleanSource;
         try{
             cleanSource = Jsoup.parse(simpleTweet.getSource().toString())
                             .text().replaceAll("\\s", "");
         }catch (NullPointerException nullPointerException){
             cleanSource = "";
         }
 
         if ((cleanSource.contains("for") &&
                 !cleanSource.contains("forMac")) ||
                 (mobileSourceMatchPattern.split(tweetText)).length > 1){
             return TweetSource.MOBILE;
         }else
             return TweetSource.WEB;
     }
 
 
     private int emoticonCount(){
         return emoticonMatchPattern.split(tweetText).length - 1;
     }
 
 
     private void setComponentFractions(SimpleTweet simpleTweet){
         double urlChars = 0;
         double hashtagChars = 0;
         double mentionChars = 0;
         double mediaChars = 0;
 
         for (SimpleURLEntity url : simpleTweet.getUrlEntities()){
            urlChars += url.getEnd() - url.getStart();
         }
 
         for (SimpleHashtagEntity hashtag : simpleTweet.getHashTagEntities()){
            hashtagChars += hashtag.getEnd() - hashtag.getStart();
         }
 
         for (SimpleUserMentionEntity mention : simpleTweet.getUserMentionEntities()){
            mentionChars += mention.getEnd() - mention.getStart();
         }
 
         for(SimpleMediaEntity mediaEntity : simpleTweet.getMediaEntities()){
             mediaChars += mediaEntity.getMediaURL().length();
         }
 
         componentFractions[0] = urlChars/tweetText.length();
         componentFractions[1] = hashtagChars/tweetText.length();
         componentFractions[2] = mentionChars/tweetText.length();
         componentFractions[3] = mediaChars/tweetText.length();
        componentFractions[4] = (tweetText.length() -
                                (urlChars + mentionChars +
                                        hashtagChars + mediaChars +
                                        tweetText.split(" ").length - 1))/tweetText.length();

        if (componentFractions[0] +
                componentFractions[1] +
                componentFractions[2] +
                componentFractions[3] +
                componentFractions[4] > 1.0){
            logger.error("Error in calculating component fractions: " + tweetText + "\n" + tweetText.length() + "\n"
                    + urlChars + "\n" + hashtagChars + "\n" + mentionChars + "\n" + mediaChars);
         }
 
     }
 
 
     private Date getDateObject(SimpleTweet simpleTweet){
         try{
             return twitterDateFormat.parse(simpleTweet.getCreatedAt().toString());
         }catch(ParseException parseException){
             logger.error("Date string is not valid", parseException);
         }catch (NullPointerException nullPointerException){
             logger.warn("createdAt in SimpleTweet is null", nullPointerException);
         }
         return null;
     }
 
 
     private TimeOfDay getTimeOfDay(Date tweetDate){
 
         if (tweetDate != null){
             Calendar c = Calendar.getInstance();
             c.setTime(tweetDate);
             int hour = c.get(Calendar.HOUR_OF_DAY);
 
             if(hour >= 0  && hour < 3)
                 return TimeOfDay.REALLY_LATE_NIGHT;
             else if(hour > 2 && hour < 6)
                 return TimeOfDay.DAWN;
             else if(hour >5 && hour < 9)
                 return TimeOfDay.EARLY_MORNING;
             else if(hour > 8 && hour < 12)
                 return TimeOfDay.LATE_MORNING;
             else if(hour > 11 && hour < 15)
                 return TimeOfDay.AFTERNOON;
             else if(hour > 14 && hour < 18)
                 return TimeOfDay.EVENING;
             else if(hour > 17 && hour < 21)
                 return TimeOfDay.NIGHT;
             else if(hour > 20 && hour < 24)
                 return TimeOfDay.LATE_NIGHT;
         }
         return TimeOfDay.REALLY_LATE_NIGHT;
     }
 
 
     private DayOfWeek getDayOfWeek (Date tweetDate){
         if (tweetDate != null){
             Calendar c = Calendar.getInstance();
             c.setTime(tweetDate);
             int day = c.get(Calendar.DAY_OF_WEEK);
 
             if(!(day == Calendar.SATURDAY) && !(day == Calendar.SUNDAY))
                 return DayOfWeek.WEEKEND;
             return DayOfWeek.WEEKDAY;
         }
         return DayOfWeek.WEEKDAY;
     }
 
 
     private String featureVectorToString(){
         StringBuilder featureVector = new StringBuilder();
         featureVector.append(userFollowers);
         featureVector.append("|");
         featureVector.append(userFriends);
         featureVector.append("|");
         featureVector.append(userIsVerified.getVerified());
         featureVector.append("|");
         featureVector.append(userTotalTweets);
         featureVector.append("|");
         featureVector.append(timeOfDay.getTimeOfDay());
         featureVector.append("|");
         featureVector.append(dayOfWeek.getDayOfWeek());
         featureVector.append("|");
         featureVector.append(urlLocationInTweet.getURLLocation());
         featureVector.append("|");
         featureVector.append(retweetCount);
         featureVector.append("|");
         featureVector.append(numCharsInTweet);
         featureVector.append("|");
         featureVector.append(numEmoticonsInTweet);
         featureVector.append("|");
         featureVector.append(hasContributors.getContributor());
         featureVector.append("|");
         featureVector.append(tweetType.getTweetType());
         featureVector.append("|");
         featureVector.append(tweetNature.getTweetNature());
         featureVector.append("|");
         featureVector.append(contentType.getContentType());
         featureVector.append("|");
         featureVector.append(source.getTweetSource());
 
         for(double componentFraction : componentFractions){
             featureVector.append("|");
             featureVector.append(fractionFormat.format(componentFraction));
         }
 
         for(double POSFraction : POSFractions){
             featureVector.append("|");
             featureVector.append(fractionFormat.format(POSFraction));
         }
 
         return featureVector.toString();
     }
 
 }
 
