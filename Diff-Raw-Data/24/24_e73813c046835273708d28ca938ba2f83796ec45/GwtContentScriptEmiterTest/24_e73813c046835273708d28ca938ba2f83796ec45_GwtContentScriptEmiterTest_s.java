 /**
  * 
  */
 package com.google.gwt.chrome.crx.linker.emiter;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import com.google.gwt.chrome.crx.client.GwtContentScript;
 import com.google.gwt.chrome.crx.linker.artifact.GwtContentScriptArtifact;
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.UnableToCompleteException;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JPackage;
 
 /**
  * @author webdizz
  * 
  */
 public class GwtContentScriptEmiterTest {
 
 	@Mock
 	private TreeLogger logger;
 
 	@Mock
 	private GeneratorContext context;
 
 	@Mock
 	private JClassType userType;
 
 	@Mock
 	private GwtContentScript.ManifestInfo spec;
 
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 
 		when(userType.getSimpleSourceName()).thenReturn(Emiter.GWT_CONTENTSCRIPT_USER_TYPE);
 		when(userType.getQualifiedSourceName()).thenReturn(Emiter.GWT_CONTENTSCRIPT_USER_TYPE);
 		JPackage jpackage = mock(JPackage.class);
 		when(userType.getPackage()).thenReturn(jpackage);
 		when(userType.getAnnotation(GwtContentScript.ManifestInfo.class)).thenReturn(spec);
 	}
 
 	@Test(expected = UnableToCompleteException.class)
 	public void shouldThrowUnableToCompleteExceptionArtifactWithoutManifestAnnotation()
 			throws UnableToCompleteException {
 		when(userType.getAnnotation(GwtContentScript.ManifestInfo.class)).thenReturn(null);
 
 		invokeCodeEmition();
 	}
 
 	@Test
 	public void shouldCallToCreateGeneratedClassname() throws UnableToCompleteException {
 		invokeCodeEmition();
 
 		verify(userType).getQualifiedSourceName();
 	}
 
 	@Test
 	public void shouldGatherArtifactAttributesFromSpec() throws UnableToCompleteException {
 		invokeCodeEmition();
 
 		verify(spec).matches();
 		verify(spec).allFrames();
 		verify(spec).runAt();
 	}
 
 	@Test
 	public void shouldCommitCreatedArtifact() throws UnableToCompleteException {
 		invokeCodeEmition();
		String subclassName = userType.getQualifiedSourceName().replace(".", "_") + "_generated";
 		GwtContentScriptArtifact artifact;
		artifact = new GwtContentScriptArtifact(subclassName, spec.matches(), spec.runAt(), spec.allFrames());
 
 		verify(context).commitArtifact(logger, artifact);
 	}
 
 	protected void invokeCodeEmition() throws UnableToCompleteException {
 		Emiter emiter = new GwtContentScriptEmiter();
 		emiter.emit(logger, context, userType, Emiter.GWT_CONTENTSCRIPT_USER_TYPE);
 	}
 }
