 package org.atlasapi.remotesite.music.emipub;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.intl.Countries;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.regex.Pattern;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Restriction;
 import org.atlasapi.media.entity.Song;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 
 /**
  */
 public class EmiPubProcessor {
 
     private static final int NOT_FOUND = -1;
     //
     private static final Pattern SPLIT_PATTERN = Pattern.compile(":");
     private static final String EMI_PUB_WORKS_URI = "http://emimusicpub.com/works/";
     private static final String EMI_PUB_RIGHTS_URI = "http://emimusicpub.com/rights/";
    private static final String EMI_PUB_CURIE_PREFIX = "ep:";
 
     public void process(File csvFile, AdapterLog log, ContentWriter contentWriter) throws Exception {
         BufferedReader csv = getReader(csvFile);
 
         try {
             String currentWork = csv.readLine();
             String currentCode = null;
             String currentId = null;
             String currentWriter = null;
             StringBuilder currentRights = null;
             Song currentSong = null;
             while (currentWork != null) {
                 Iterable<String> data = Splitter.on(SPLIT_PATTERN).trimResults().split(currentWork);
                 //
                 String nextCode = Iterables.get(data, 0);
                 if (!nextCode.equals(currentCode)) {
                     if (currentSong != null) {
                         Version version = new Version();
                         Restriction restriction = new Restriction();
                         Encoding encoding = new Encoding();
                         Location location = new Location();
                         Policy policy = new Policy();
                         version.addManifestedAs(encoding);
                         version.setCanonicalUri(EMI_PUB_RIGHTS_URI + currentId);
                         version.setRestriction(restriction);
                         restriction.setMessage(currentRights.substring(0, currentRights.length() - 1));
                         encoding.addAvailableAt(location);
                         location.setUri(EMI_PUB_RIGHTS_URI + currentId);
                         location.setPolicy(policy);
                         policy.setAvailableCountries(Sets.newHashSet(Countries.GB));
                         currentSong.setVersions(Sets.newHashSet(version));
                         contentWriter.createOrUpdate(currentSong);
                     }
                    currentSong = new Song(EMI_PUB_WORKS_URI + nextCode, EMI_PUB_CURIE_PREFIX + nextCode, Publisher.EMI_PUB);
                     currentCode = nextCode;
                     currentId = Iterables.get(data, 1);
                    currentSong.setTitle(Iterables.get(data, 2));
                     currentRights = new StringBuilder();
                 }
                 //
                 String nextWriter = Iterables.get(data, 4);
                 if (!nextWriter.equals(currentWriter)) {
                     currentWriter = nextWriter;
                    currentSong.addPerson(new CrewMember(null, null, Publisher.EMI_PUB).withName(currentWriter));
                 }
                 //
                 currentRights.append(currentWriter).append(":").append(Iterables.get(data, 9)).append(":").append(Iterables.get(data, 11)).append("~");
                 currentWork = csv.readLine();
             }
         } finally {
             csv.close();
         }
     }
 
     private BufferedReader getReader(File csvFile) throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(csvFile));
         reader.readLine();
         return reader;
     }
 }
