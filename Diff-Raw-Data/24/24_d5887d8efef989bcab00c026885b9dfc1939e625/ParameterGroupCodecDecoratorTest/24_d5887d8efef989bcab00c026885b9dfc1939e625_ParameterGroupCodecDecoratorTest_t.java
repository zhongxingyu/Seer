 package org.hbird.transport.payloadcodec;
 
 import org.mockito.Mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.hbird.core.commons.tmtc.Parameter;
 import org.hbird.core.commons.tmtc.ParameterGroup;
 import org.hbird.core.commons.tmtc.exceptions.UnknownParameterException;
 import org.hbird.transport.payloadcodec.codecparameters.CodecParameter;
 import org.hbird.transport.payloadcodec.codecparameters.ParameterGroupCodecDecorator;
 import org.hbird.transport.payloadcodec.exceptions.NoEncodingException;
 import org.hbird.transport.payloadcodec.exceptions.UnexpectedParameterTypeException;
 import org.hbird.transport.payloadcodec.exceptions.UnknownParameterEncodingException;
 import org.hbird.transport.payloadcodec.exceptions.UnsupportedParameterEncodingException;
 import org.hbird.transport.payloadcodec.testsupport.MockSpaceSystemModel;
 import org.hbird.core.spacesystemmodel.SpaceSystemModel;
 import org.hbird.core.spacesystemmodel.encoding.Encoding;
 import org.hbird.core.spacesystemmodel.encoding.Encoding.BinaryRepresentation;
 import org.hbird.core.spacesystemmodel.exceptions.ParameterNotInGroupException;
 import org.hbird.core.spacesystemmodel.exceptions.ParameterNotInModelException;
 import org.hbird.core.spacesystemmodel.exceptions.UnknownParameterGroupException;
import org.hbird.core.spacesystemmodel.tmtcgroups.HummingbirdParameterGroup;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ParameterGroupCodecDecoratorTest {
 
 	private final SpaceSystemModel ssm = new MockSpaceSystemModel();
 	private final ParameterGroupCodecDecorator decorator = new ParameterGroupCodecDecorator(ssm.getEncodings());
 
 	@Mock
 	private SpaceSystemModel mockModel;
 
 	private static Map<String, Encoding> mockEncodings;
 
 	private ParameterGroup mockGroup;
 
 	@Mock
 	private Parameter<String> mockStringParameter;
 	private static final String MOCK_STRING_PARAMETER_NAME = "MockStringParameter";
 
 	@BeforeClass
 	public static void setupEncodings() {
 		mockEncodings = new HashMap<String, Encoding>(1);
 		Encoding asciiEncoding = new Encoding(8, BinaryRepresentation.ASCII);
 		mockEncodings.put(MOCK_STRING_PARAMETER_NAME, asciiEncoding);
 	}
 
 	/**
 	 * TODO Pre-Mockito, uses test support MockSpaceSystemModel and the ParameterGroupCodecDecorator field initialised
 	 * in this class. Convert to Mockito like the other tests at some point.
 	 */
 	@Test
 	public void testDecorateParameterGroup() throws UnsupportedParameterEncodingException, UnknownParameterEncodingException, UnexpectedParameterTypeException,
 			UnknownParameterGroupException, ParameterNotInGroupException, NoEncodingException, UnknownParameterException, ParameterNotInModelException {
 		Map<String, ParameterGroup> codecAwareParameterGroups = decorator.decorateParameterGroups(ssm.getParameterGroups());
 
 		for (ParameterGroup pg : codecAwareParameterGroups.values()) {
 			Map<String, Parameter<Integer>> integerParameters = pg.getIntegerParameters();
 			if (integerParameters != null) {
 				for (Parameter<Integer> p : integerParameters.values()) {
 					if (!(p instanceof CodecParameter<?>)) {
 						fail("Parameter " + p.getQualifiedName() + " was not decorated by the codec decorator");
 					}
 					CodecParameter<Integer> cp = (CodecParameter<Integer>) p;
 					assertNotNull(cp);
 				}
 			}
 
 			Map<String, Parameter<Long>> longParameters = pg.getLongParameters();
 			if (longParameters != null) {
 				for (Parameter<Long> p : longParameters.values()) {
 					if (!(p instanceof CodecParameter<?>)) {
 						fail("Parameter " + p.getQualifiedName() + " was not decorated by the codec decorator");
 					}
 					CodecParameter<Long> cp = (CodecParameter<Long>) p;
 					assertNotNull(cp);
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testDecorateStringContainingParameterGroup() throws NoEncodingException, UnsupportedParameterEncodingException,
 			UnknownParameterEncodingException, UnexpectedParameterTypeException {
 
 		ParameterGroupCodecDecorator decorator = new ParameterGroupCodecDecorator(mockEncodings);
 
 		// Setup
 		Map<String, ParameterGroup> parameterGroups = new HashMap<String, ParameterGroup>(1);
		mockGroup = new HummingbirdParameterGroup("Mock Group", "MG", "", "");
 		parameterGroups.put("mockGroup", mockGroup);
 		Map<String, Parameter<String>> stringParameters = new HashMap<String, Parameter<String>>();
 		stringParameters.put("mockStringParam", mockStringParameter);
 
 		when(mockModel.getParameterGroups()).thenReturn(parameterGroups);
 		when(mockStringParameter.getQualifiedName()).thenReturn(MOCK_STRING_PARAMETER_NAME);
 
		mockGroup.addStringParameter(mockStringParameter);
 
 		Map<String, ParameterGroup> codecAwareParameterGroups = decorator.decorateParameterGroups(mockModel.getParameterGroups());
 
		for (ParameterGroup pg : codecAwareParameterGroups.values()) {
			Map<String, Parameter<String>> decoratedStringParameters = pg.getStringParameters();
			if (decoratedStringParameters != null) {
				for (Parameter<String> p : decoratedStringParameters.values()) {
					if (!(p instanceof CodecParameter<?>)) {
						fail("Parameter " + p.getQualifiedName() + " was not decorated by the codec decorator");
					}
					CodecParameter<String> cp = (CodecParameter<String>) p;
					assertNotNull(cp);
				}
			}
		}
 
 	}
 
 }
