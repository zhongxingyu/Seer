 package translator;
 
 import org.apache.commons.lang3.tuple.Pair;
 import translator.domain.Vocab;
 import translator.domain.VocabSet;
 
 import java.io.File;
 import java.util.Locale;
 
 import static java.lang.String.format;
 
 //àçéèêîù œ
 public class Main {
     private Downloader downloader = new Downloader();
 
     public static void main(String[] args) throws Exception {
         new Main().go();
     }
 
     private void go() throws Exception {
         File srcDir = new File("src/main/resources");
        //File targetDir = new File("out/mp3");
         File targetDir = new File("e:/French");
         for (File file : srcDir.listFiles()) {
            if (!file.getName().equals("Unit3-noms.txt"))
                continue;
             VocabSet vocabSet = new VocabSet(file);
             vocabSet.getDescription();
             for (int i = 0; i < vocabSet.getVocab().size(); i++) {
                 Vocab vocab = vocabSet.getVocab().get(i);
                 File mp3Dir = new File(targetDir, vocabSet.getDescription());
                 mp3Dir.mkdirs();
                createMp3File(vocabSet.getFrom(), i + 1, "a", mp3Dir, vocab.getTextFrom(), null);
                createMp3File(vocabSet.getTo(), i + 1, "b", mp3Dir, vocab.getTextTo(), vocab.getGender());
             }
         }
     }
 
     private void createMp3File(Locale locale, int i, String sorter, File mp3Dir, String text, String gender) {
         String fileNameFormat = "%02d%s - %s%s.mp3";
         String extra = gender == null ? "" : "-" + gender;
         String fileName = format(fileNameFormat, i, sorter, text, extra);
         File file = new File(mp3Dir, fileName);
         if (!file.exists())
             downloader.downloadMp3(text, locale, file);
     }
 }
