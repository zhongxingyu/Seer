 package chameleon.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 import chameleon.core.compilationunit.CompilationUnit;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.namespacepart.NamespacePart;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Invalid;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.input.ParseException;
 import chameleon.test.provider.ElementProvider;
 import chameleon.test.provider.ModelProvider;
 
 public class VerificationTest extends ModelTest {
 
 	public VerificationTest(ModelProvider provider,ElementProvider<NamespacePart> compilationUnitProvider) throws ParseException, IOException {
 		super(provider);
 		_elementProvider = compilationUnitProvider;
 	}
 
 	private ElementProvider<NamespacePart> _elementProvider;
 
 	public ElementProvider<NamespacePart> elementProvider() {
 		return _elementProvider;
 	}
 	
 	@Test
 	public void testVerification() throws LookupException {
 		for(NamespacePart element: elementProvider().elements(language())) {
 			VerificationResult result = element.verify();
			assertTrue(result.toString() ,Valid.create() == result);
 		}
 	}
 
 }
