 package com.github.tteofili.nlputils;
 
 import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.FieldType;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.junit.Test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
 * Testcase for {@link com.github.samplett.nlputils.util.SimpleNaiveBayesClassifier}
  */
 public class LuceneNaiveBayesClassifierTest {
 
     @Test
     public void ppsIntegrationTest() throws Exception {
 
         Directory dir = new RAMDirectory();
         WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_40);
         IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, analyzer);
         IndexWriter indexWriter = new IndexWriter(dir, conf);
 
         FieldType type = new FieldType();
         type.setIndexed(true);
         type.setStored(true);
         type.setStoreTermVectors(true);
 
         Document d = new Document();
         d.add(new Field("text", "CAVOUR ad.te napoleone III affare: cat. C/2 ottimo" +
                 " stato ingresso angolo cottura bagno con doccia e camera. " +
                 "ottimo per investimento o piccolo studio per professionisti" +
                 " e 99.000 Ag.Imm.", type));
         d.add(new Field("class", "A", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "TRASTEVERE via degli Orti di Trastevere in palazzo " +
                 "signorile (con s. portineria) appartamento mq 180 + cantina mq" +
                 " 6 con rifiniture di pregio marmi & armadi a muro + ampia " +
                 "balconata 50 mq assolutamente no agenzie E 930.000", type));
         d.add(new Field("class", "N", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "CORSO VITTORIO Emanuele V. del banco di santo spirito" +
                 " 3° piano con ascensore appartamento di 142 mq commerciali " +
                 "composto da: ingresso disimpegno tre camere soggiorno cucina" +
                 " due bagni due cantine per un totale di 15 mq e. 900.000 Ag.Imm.", type));
         d.add(new Field("class", "A", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "TRASTEVERE Ippolito Nievo quinto piano tripla " +
                 "esposizione ingresso salone doppio cucina abitabile tre " +
                 "camere servizio ripostiglio terrazzo e soffitta da ristrutturare " +
                 "e 650.000 Ag.Imm.", type));
         d.add(new Field("class", "A", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "TRASTEVERE E.Rolli solo privati palazzo epoca doppia" +
                 " esposizione ingresso soppalcato soggiorno 2 camere cucinotto " +
                 "bagno 84 mq IV piano no ascensore 385.000 giardino condominio", type));
         d.add(new Field("class", "N", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "CENTRO monti sforza elegante edificio con ampi spazi" +
                 " comuni ristrutturato ingresso soggiorno angolo cucina camera " +
                 "letto armadi a muro bagno vasca con finestra pavimenti cotto " +
                 "luminoso silenzioso doppio affaccio climatizzato e 405.000 ag. " +
                 "imm. cl en.g", type));
         d.add(new Field("class", "A", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "SAN LORENZO app.to epoca privato vende salone due " +
                 "camere cucina abit. due bagni ripostigli vari II piano con" +
                 " ascensore triplo affaccio E 530.000 ", type));
         d.add(new Field("class", "N", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "SAN LORENZO Via Porta Labicana appartamento mq 80 " +
                 "piano rialzato con ingresso 3 camere cucina bagno E 395.000 ", type));
         d.add(new Field("class", "N", type));
         indexWriter.addDocument(d);
 
         d = new Document();
         d.add(new Field("text", "SAN LORENZO via degli Umbri I° p. 3 stanze cucina " +
                 "servizio terrazzino interno buono stato E. 390.000 tratt. " +
                 "assoloutamente no agenzie ", type));
         d.add(new Field("class", "N", type));
         indexWriter.addDocument(d);
 
         indexWriter.commit();
 
         IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter.getDirectory()));
 
         LuceneSimpleNaiveBayesClassifier classifier = new LuceneSimpleNaiveBayesClassifier();
         classifier.train(indexSearcher, "text", "class", analyzer);
 
         Boolean isAgency = classifier.calculateClass("CENTRO S.Maria Maggiore " +
                 "angolo Napoleone III in palazzo epoca con portiere 110 mq ristrutt." +
                 " IIp salone doppio cucina ab. 2 camere bagno ripost. balcone " +
                 "perimetrale E. 730.000 tratt. ").equals("A");
         assertFalse(isAgency);
 
         isAgency = classifier.calculateClass("TRASTEVERE via del Mattonato in " +
                 "piccola palazzina d'epoca app.to finemente ristrutturato " +
                 "ingresso salone camera cucina tinello servizio balconcino " +
                 "aria condiz. e 540.000 Ag.Imm. ").equals("A");
         assertTrue(isAgency);
 
     }
 }
