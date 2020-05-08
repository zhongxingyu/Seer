 package util;
 
 import static org.hamcrest.CoreMatchers.is;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import models.Assessment;
 import models.ResearchSubject;
 import org.junit.Test;
 import play.test.UnitTest;
 
 public class CsvFormatterTest extends UnitTest {
 
     private static final String VALUE_SEPARATOR = ", ";
     private static final String NEWLINE = "\n";
 
     private final CsvFormatter formatter = new CsvFormatter();
 
     @Test
     public void formatsAssessmentAsCsv() throws Exception {
         Assessment assessment = new Assessment();
         int order = 1;
         String time = "2";
         int score = 3;
         assessment.setAssessmentOrder(order).setTime(time).setScore(score);
 
         String result = formatter.format(assessment);
         assertThat(result, is("1, 2.0, 3"));
     }
 
     @Test
     public void formatsResearchSubjectAsCsv() throws Exception {
         List<ResearchSubject> subjects = new ArrayList<ResearchSubject>();
         ResearchSubject subject1 = sampleResearchSubjectWithIdentifier(1);
         ResearchSubject subject2 = sampleResearchSubjectWithIdentifier(2);
 
         subjects.add(subject1);
         subjects.add(subject2);
 
         String result = formatter.format(subjects);
 
        assertEquals(headers() + NEWLINE + asCsv(subject1) + NEWLINE + asCsv(subject2), result);
     }
 
     private String asCsv(ResearchSubject subject) {
         StringBuilder builder = new StringBuilder();
         builder.
                 append(subject.getId() + VALUE_SEPARATOR).
                 append(subject.getPpCode() + VALUE_SEPARATOR).
                 append(subject.getVideoType() + VALUE_SEPARATOR).
 
                 append(formatter.format(subject.getCommercieelInzicht()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getVerkoopVaardigheden()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getOnderhandelen()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getKlantGerichtheid()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getMondelingCommuniceren()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getOpenheid()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getConscientieusheid()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getExtraversie()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getAangenaamheid()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getEmotioneleStabiliteit()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getEerlijkheid()) + VALUE_SEPARATOR).
                 append(formatter.format(subject.getIntelligentie()) + VALUE_SEPARATOR).
 
                 append(subject.getExtraQuestions().getTweedeGesprek() + VALUE_SEPARATOR).
                 append(subject.getExtraQuestions().getAanneemt() + VALUE_SEPARATOR).
                 append(subject.getExtraQuestions().getAfwijst() + VALUE_SEPARATOR).
                 append(subject.getExtraQuestions().getCommercieel() + VALUE_SEPARATOR).
                 append(subject.getExtraQuestions().getBackOffice() + VALUE_SEPARATOR).
                 append(subject.getExtraQuestions().getComments());
 
         return builder.toString();
     }
 
     private String headers() {
         List<String> list = new ArrayList<String>();
         list.addAll(Arrays.asList(HEADER_VALUES));
 
         StringBuilder builder = new StringBuilder();
         for (String header : list) {
             builder.append(VALUE_SEPARATOR);
             builder.append(header);
         }
         String result = builder.toString();
        result.replaceFirst(VALUE_SEPARATOR, "");

        return result;
     }
 
     private ResearchSubject sampleResearchSubjectWithIdentifier(int identifier) {
         String identifierAsString = Integer.toString(identifier);
 
         ResearchSubject subject = new ResearchSubject();
         subject.setPpCode("code_" + identifier);
         subject.setVideoType("video_" + identifier);
 
         subject.getCommercieelInzicht().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getVerkoopVaardigheden().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getOnderhandelen().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getKlantGerichtheid().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getMondelingCommuniceren().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getOpenheid().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getConscientieusheid().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getExtraversie().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getAangenaamheid().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getEmotioneleStabiliteit().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getEerlijkheid().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
         subject.getIntelligentie().setAssessmentOrder(identifier).setTime(identifierAsString).setScore(identifier);
 
         subject.getExtraQuestions().setTweedeGesprek(identifier).setAanneemt(identifier).
                 setAfwijst(identifier).setCommercieel(identifier).setBackOffice(identifier).setComments(identifierAsString);
 
         return subject;
     }
 
     private static final String[] HEADER_VALUES = new String[]{"id", "pp_code", "video_type",
             "commercieelinzicht_bvolgorde", "commercieelinzicht_vtijd", "commercieelinzicht_score",
             "verkoopvaardigheden_bvolgorde", "verkoopvaardigheden_vtijd", "verkoopvaardigheden_score",
             "onderhandelen_bvolgorde", "onderhandelen_vtijd", "onderhandelen_score",
             "klantgerichtheid_bvolgorde", "klantgerichtheid_vtijd", "klantgerichtheid_score",
             "mondelingcommuniceren_bvolgorde", "mondelingcommuniceren_vtijd", "mondelingcommuniceren_score",
             "openheid_bvolgorde", "openheid_vtijd", "openheid_score",
             "conscientieusheid_bvolgorde", "conscientieusheid_vtijd", "conscientieusheid_score",
             "extraversie_bvolgorde", "extraversie_vtijd", "extraversie_score",
             "aangenaamheid_bvolgorde", "aangenaamheid_vtijd", "aangenaamheid_score",
             "emotionelestabiliteit_bvolgorde", "emotionelestabiliteit_vtijd", "emotionelestabiliteit_score",
             "eerlijkheid_bvolgorde", "eerlijkheid_vtijd", "eerlijkheid_score",
             "intelligentie_bvolgorde", "intelligentie_vtijd", "intelligentie_score",
             "tweedegesprek", "aanneemt", "afwijst", "commercieel", "backoffice", "comments"};
 }
