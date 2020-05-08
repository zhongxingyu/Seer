 package eu.scapeproject;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import eu.scapeproject.model.Agent;
 import eu.scapeproject.model.BitStream;
 import eu.scapeproject.model.File;
 import eu.scapeproject.model.IntellectualEntity;
 import eu.scapeproject.model.Representation;
 import eu.scapeproject.model.UUIDIdentifier;
 import eu.scapeproject.model.metadata.TechnicalMetadata;
 import eu.scapeproject.model.metadata.dc.DCMetadata;
 import eu.scapeproject.model.metadata.mix.NisoMixMetadata;
 
 public class ModelUtil {
 	public static final Agent createTestCreator() {
 		return new Agent.Builder()
 				.name("henry testcreator")
 				.note("a test agent")
 				.role("creator")
 				.type("human")
 				.build();
 	}
 
 	public static final IntellectualEntity createEntity(List<Representation> representations) {
 		IntellectualEntity.Builder ieBuilder = new IntellectualEntity.Builder()
 				.identifier(new UUIDIdentifier())
 				.descriptive(createDCMetadata());
 		if (representations != null && representations.size() != 0) {
 			ieBuilder.representations(representations);
 		}
 		return ieBuilder.build();
 	}
 
 	public static final DCMetadata createDCMetadata() {
 		return new DCMetadata.Builder()
 				.creator(createTestCreator())
 				.title("A test entity")
 				.date(new Date())
 				.language("en")
 				.build();
 	}
 
 	public static final Representation createImageRepresentation(URI uri) {
 		File file = new File.Builder()
				.uri(uri)
 				.technical(createNisoMetadata())
 				.bitStreams(new ArrayList<BitStream>()) 
 				.build();
 		return new Representation.Builder()
 			.file(file)
 			.technical(createNisoMetadata())
 			.build();
 	}
 
 	private static NisoMixMetadata createNisoMetadata() {
 		return new NisoMixMetadata.Builder()
 				.height(0)
 				.width(0)
 				.build();
 	}
 }
