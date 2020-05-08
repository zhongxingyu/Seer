 package com.zenika.dorm.core.test.processor.impl;
 
 import com.zenika.dorm.core.graph.DependencyNode;
 import com.zenika.dorm.core.graph.impl.DefaultDependency;
 import com.zenika.dorm.core.graph.impl.DefaultDependencyNode;
 import com.zenika.dorm.core.model.DormFile;
 import com.zenika.dorm.core.model.DormOrigin;
 import com.zenika.dorm.core.model.DormRequest;
 import com.zenika.dorm.core.model.impl.DefaultDormFile;
 import com.zenika.dorm.core.model.impl.DefaultDormMetadata;
 import com.zenika.dorm.core.model.impl.DefaultDormOrigin;
 import com.zenika.dorm.core.model.impl.DefaultDormRequest;
 import com.zenika.dorm.core.processor.RequestProcessor;
 import com.zenika.dorm.core.processor.impl.DormProcessor;
 import com.zenika.dorm.core.test.unit.AbstractUnitTest;
import org.fest.assertions.Assertions;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Mockito.verify;
 
 /**
  * Unit tests for the dorm processor extension
  *
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class DormProcessorUnitTest extends AbstractUnitTest {
 
     @Mock
     private RequestProcessor requestProcessor;
 
     @InjectMocks
     private DormProcessor processor = new DormProcessor();
 
     @Test
    public void pushStandardArtifact() {
 
         String qualifier = "test-1.0";
         String version = "1.0";
         String filename = "testfile.jar";
         File file = new File("/tmp/testfile.jar");
 
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(DormRequest.ORIGIN, DefaultDormOrigin.ORIGIN);
         properties.put(DormRequest.VERSION, version);
         properties.put(DormRequest.FILENAME, filename);
         properties.put("qualifier", qualifier);
 
         DormOrigin origin = new DefaultDormOrigin(qualifier);
         DormFile dormFile = DefaultDormFile.create(filename, file);
 
         DormRequest request = DefaultDormRequest.create(properties, file);
 
         DependencyNode node = new DefaultDependencyNode(new DefaultDependency(
                 new DefaultDormMetadata("1.0", origin), dormFile));
 
         given(requestProcessor.createNode(origin, request)).willReturn(node);
 
        Assertions.assertThat(processor.push(request)).isEqualTo(node);
 
         verify(requestProcessor).createNode(origin, request);
     }
 }
