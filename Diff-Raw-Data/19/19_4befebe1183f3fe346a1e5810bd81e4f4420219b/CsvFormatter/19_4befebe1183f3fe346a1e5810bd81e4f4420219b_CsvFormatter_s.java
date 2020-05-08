 package util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import models.Assessment;
 import models.ExtraQuestions;
 import models.ResearchSubject;
 
 public class CsvFormatter {
 
     private static final String VALUE_SEPARATOR = ", ";
     private static final String NEWLINE = "\n";
 
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
     private static final String COLUMNS;
 
     static {
         List<String> list = new ArrayList<String>();
         list.addAll(Arrays.asList(HEADER_VALUES));
 
         StringBuilder builder = new StringBuilder();
         for (String header : list) {
             builder.append(VALUE_SEPARATOR);
             builder.append(header);
         }
         String result = builder.toString();
        result.replaceFirst(VALUE_SEPARATOR, "");
 
         COLUMNS = result;
     }
 
     public String format(Assessment assessment) {
         return assessment.getAssessmentOrder() + VALUE_SEPARATOR + assessment.getTime() + VALUE_SEPARATOR + assessment.getScore();
     }
 
     public String format(List<ResearchSubject> subjects) {
         StringBuilder builder = new StringBuilder();
         builder.append(COLUMNS).append(NEWLINE);
 
         for (ResearchSubject subject : subjects) {
             builder.append(asCsv(subject)).append(NEWLINE);
         }
 
         return deleteLastNewLineFrom(builder.toString());
     }
 
     private String deleteLastNewLineFrom(String text) {
         int index = text.lastIndexOf(NEWLINE);
         return text.substring(0, index);
     }
 
     private String asCsv(ResearchSubject subject) {
         StringBuilder builder = new StringBuilder();
         builder.append(subject.getId()).append(VALUE_SEPARATOR).
                 append(subject.getPpCode()).append(VALUE_SEPARATOR).
                 append(subject.getVideoType()).append(VALUE_SEPARATOR).
 
                 append(format(subject.getCommercieelInzicht()) + VALUE_SEPARATOR).
                 append(format(subject.getVerkoopVaardigheden()) + VALUE_SEPARATOR).
                 append(format(subject.getOnderhandelen()) + VALUE_SEPARATOR).
                 append(format(subject.getKlantGerichtheid()) + VALUE_SEPARATOR).
                 append(format(subject.getMondelingCommuniceren()) + VALUE_SEPARATOR).
                 append(format(subject.getOpenheid()) + VALUE_SEPARATOR).
                 append(format(subject.getConscientieusheid()) + VALUE_SEPARATOR).
                 append(format(subject.getExtraversie()) + VALUE_SEPARATOR).
                 append(format(subject.getAangenaamheid()) + VALUE_SEPARATOR).
                 append(format(subject.getEmotioneleStabiliteit()) + VALUE_SEPARATOR).
                 append(format(subject.getEerlijkheid()) + VALUE_SEPARATOR).
                 append(format(subject.getIntelligentie()) + VALUE_SEPARATOR);
 
         ExtraQuestions extraQuestions = subject.getExtraQuestions();
         builder.append(extraQuestions.getTweedeGesprek() + VALUE_SEPARATOR).
                 append(extraQuestions.getAanneemt() + VALUE_SEPARATOR).
                 append(extraQuestions.getAfwijst() + VALUE_SEPARATOR).
                 append(extraQuestions.getCommercieel() + VALUE_SEPARATOR).
                 append(extraQuestions.getBackOffice() + VALUE_SEPARATOR).
                 append(extraQuestions.getComments());
 
         return builder.toString();
     }
 }
