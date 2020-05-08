 package phonemeAligner.simpleGaussianTraining;
 
 import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import phonemeScorers.io.PhonemeScorerExporter;
 import phonemeScorers.trainers.IterativePhonemeScorerTraining;
 
 import common.AudioLabel;
 import common.exceptions.ImplementationError;
 
 import dataExporters.AudacityLabelsExporter;
 import dataProducers.AudacityLabelImporter;
 import dataProducers.PowerExtractor;
 import dataProducers.TextImporter;
 import dataProducers.WaveImporter;
 
 public class MainLargeChunkPhonemeAlignment
 {
     public static void main(String[] args) throws UnsupportedAudioFileException, IOException, ImplementationError
     {
         String waveFile = args[0];
         String labelsFile = args[1];
         String outputFile = args[2];
         
         AudioInputStream stream = AudioSystem.getAudioInputStream(new File(waveFile));
         double totalTime = (double)stream.getFrameLength() / (double)stream.getFormat().getFrameRate();
         System.err.println("total time: " + totalTime);
         
         AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
         WaveImporter waveImporterForPowers = new WaveImporter(
                 waveFile, "../phonemeAligner/config_nospeech_nomel.xml");
         WaveImporter waveImporterForAudioData = new WaveImporter(
                 waveFile, "../phonemeAligner/phonemeAlignmentConfig.xml");
         PowerExtractor powerExtractor = new PowerExtractor();
         
         waveImporterForPowers.registerObserver(powerExtractor.getPowerObserver());
         waveImporterForAudioData.registerObserver(powerExtractor.getAudioFeaturesObserver());
         
         waveImporterForPowers.process();
         waveImporterForAudioData.process();
         waveImporterForAudioData.done();
         waveImporterForPowers.done();
 
         IterativePhonemeScorerTraining aligner =
             new IterativePhonemeScorerTraining(
                 5.0,
                 prepared,
                 powerExtractor.getPowerData(),
                 new GraphemesToPolishPhonemesConverter(),
                 totalTime);
 
        new PhonemeScorerExporter(outputFile).export(aligner.train(15));
         System.err.println("END");
     }
 }
