 package sorcer.util;
 
import junit.framework.Assert;
 import org.junit.Test;
 
 /**
  * @author Rafał Krupiński
  */
 public class ArtifactCoordinatesTest {
 	@Test
 	public void testUnresolveSimplePackage() throws Exception {
 		ArtifactCoordinates coords = ArtifactCoordinates.unresolve("example/artifact/version/artifact-version.jar");
 		Assert.assertEquals(new ArtifactCoordinates("example","artifact","version"),coords);
 	}
 
 	@Test
 	public void testUnresolveComplexPackage() throws Exception {
 		ArtifactCoordinates coords = ArtifactCoordinates.unresolve("com/example/artifact/version/artifact-version.jar");
 		Assert.assertEquals(new ArtifactCoordinates("com.example","artifact","version"),coords);
 	}
 
 	@Test
 	public void testUnresolveClassifier() throws Exception {
 		ArtifactCoordinates coords = ArtifactCoordinates.unresolve("com/example/artifact/version/artifact-version-classifier.jar");
 		Assert.assertEquals(new ArtifactCoordinates("com.example","artifact","jar","version","classifier"),coords);
 	}
 
 	@Test
 	public void testUnresolveShort() throws Exception {
 		Assert.assertNull(ArtifactCoordinates.unresolve("one/two/three"));
 	}
 
 	@Test
 	public void testUnresolveNoExt() throws Exception {
 		Assert.assertNull(ArtifactCoordinates.unresolve("com/example/artifact/version/artifact-version"));
 	}
 
 
 	@Test
 	public void testUnresolveInvalidExt() throws Exception {
 		Assert.assertNull(ArtifactCoordinates.unresolve("com/example/artifact/version/artifact-version."));
 	}
 
 }
