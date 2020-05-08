 package no.bouvet.kpro.mix;
 
 import no.bouvet.kpro.renderer.Instructions;
 import no.bouvet.kpro.renderer.audio.AudioPlaybackTarget;
 import no.bouvet.kpro.renderer.audio.AudioRenderer;
 import no.lau.vdvil.cache.testresources.TestMp3s;
 import no.lau.vdvil.handler.Composition;
 import no.lau.vdvil.handler.DownloadAndParseFacade;
 import no.lau.vdvil.handler.MultimediaPart;
 import no.lau.vdvil.handler.persistence.DvlXML;
 import no.lau.vdvil.handler.persistence.PartXML;
 import no.lau.vdvil.player.InstructionPlayer;
 import no.lau.vdvil.player.VdvilPlayer;
 import no.vdvil.renderer.audio.AudioDescription;
 import no.vdvil.renderer.audio.AudioXMLParser;
 import org.codehaus.httpcache4j.cache.VdvilHttpCache;
 
 import javax.sound.sampled.LineUnavailableException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class JavaZoneExample {
     URL returning = TestMp3s.returningDvl;
     URL not_alone = TestMp3s.not_aloneDvl;
     URL scares_me = TestMp3s.scares_meDvl;
 
     AudioXMLParser audioXMLParser;
 
     JavaZoneExample() {
         DownloadAndParseFacade downloadAndParseFacade = new DownloadAndParseFacade();
         downloadAndParseFacade.addCache(VdvilHttpCache.create());
         audioXMLParser = new AudioXMLParser(downloadAndParseFacade);
         downloadAndParseFacade.addParser(audioXMLParser);
     }
 
     public static void main(String[] args) throws IOException, LineUnavailableException, InterruptedException {
        JavaZoneExample test = new JavaZoneExample();
 
         Composition composition = test.parts();
         Float masterBpm = 150F;
         Instructions instructions = composition.instructions(masterBpm);
         VdvilPlayer player = new InstructionPlayer(masterBpm, instructions, Collections.singletonList(new AudioRenderer(new AudioPlaybackTarget())));
         player.play(0);
         while (player.isPlaying()) {
             Thread.sleep(500);
         }
     }
 
     public Composition parts() {
         List<MultimediaPart> parts = new ArrayList<MultimediaPart>();
         try {
             parts.add(createAudioPart("4479230163500364845", 0, 32, not_alone));
             parts.add(createAudioPart("5403996530329584526", 16, 48, scares_me));
             parts.add(createAudioPart("8313187524105777940", 32, 70, not_alone));
             parts.add(createAudioPart("5403996530329584526", 48, 64, scares_me));
             parts.add(createAudioPart("1826025806904317462", 64, 112, scares_me));
             parts.add(createAudioPart("6401936245564505757", 96, 140, returning));
             parts.add(createAudioPart("6401936245564505757", 96, 140, returning));
             parts.add(createAudioPart("6182122145512625145", 128, 174, returning));
             parts.add(createAudioPart("3378726703924324403", 144, 174, returning));
             parts.add(createAudioPart("4823965795648964701", 174, 175, returning));
             parts.add(createAudioPart("5560598317419002938", 175, 176, returning));
             parts.add(createAudioPart("9040781467677187716", 176, 240, returning));
             parts.add(createAudioPart("8301899110835906945", 208, 224, scares_me));
             parts.add(createAudioPart("5555459205073513470", 224, 252, scares_me));
         } catch (IOException e) {
             throw new RuntimeException("This should not happen");
         }
         return new Composition("JavaZone Demo", 150F, parts, TestMp3s.javaZoneComposition);
     }
 
     private AudioDescription createAudioPart(String id, int start, int end, URL url) throws IOException {
         return audioXMLParser.parse(PartXML.create(id, start, end, DvlXML.create("URL Name", url)));
     }
 }
