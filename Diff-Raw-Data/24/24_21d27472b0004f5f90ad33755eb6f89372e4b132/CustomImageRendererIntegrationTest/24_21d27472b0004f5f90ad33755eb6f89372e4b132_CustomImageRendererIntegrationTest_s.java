 package org.lawrencebower.docgen.core.generator.custom.renderer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.lawrencebower.docgen.core.document.component.DocComponent;
 import org.lawrencebower.docgen.core.document.component.ImageComponent;
 import org.lawrencebower.docgen.core.document.component.position.DocPosition;
 import org.lawrencebower.docgen.core.document.component.position.HorizontalAlignment;
 import org.lawrencebower.docgen.core.exception.DocGenException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:META-INF/integration-test-config.xml"})
 public class CustomImageRendererIntegrationTest extends AbstractCustomRendererTest {
 
    private String imageFileLocation = "L:\\pictures\\random\\bod.bmp";
 
     @Before
     public void setup() {
         super.prepareDirs();
     }
 
     @Test
     public void testRenderComponent_alignedComponent_validFile() {
 
         String expectedOutputFilePath = inputPackage + "image_renderer_expected_output.pdf";
         String outFilePath = outputPackage + "image_renderer_output.pdf";
 
         DocPosition leftPosition = new DocPosition(HorizontalAlignment.LEFT);
         DocComponent leftImageComponent = new ImageComponent(leftPosition, imageFileLocation);
 
         DocPosition centerPosition = new DocPosition(HorizontalAlignment.CENTER);
         DocComponent centerImageComponent = new ImageComponent(centerPosition, imageFileLocation);
 
         DocPosition rightPosition = new DocPosition(HorizontalAlignment.RIGHT);
         DocComponent rightImageComponent = new ImageComponent(rightPosition, imageFileLocation);
 
         createPDFAndCompareWithExpected(expectedOutputFilePath,
                                         outFilePath,
                                         leftImageComponent,
                                         centerImageComponent,
                                         rightImageComponent);
     }
 
     @Test
     public void testRenderComponent_scaledComponent_validFile() {
 
         String expectedOutputFilePath = inputPackage + "image_renderer_expected_output2.pdf";
         String outFilePath = outputPackage + "image_renderer_output2.pdf";
 
         DocPosition leftPosition = new DocPosition(HorizontalAlignment.LEFT);
         ImageComponent leftImageComponent = new ImageComponent(leftPosition, imageFileLocation);
         leftImageComponent.setSize(10,10);
 
         DocPosition centerPosition = new DocPosition(HorizontalAlignment.CENTER);
         ImageComponent centerImageComponent = new ImageComponent(centerPosition, imageFileLocation);
         centerImageComponent.setSize(50,75);
 
         DocPosition rightPosition = new DocPosition(HorizontalAlignment.RIGHT);
         ImageComponent rightImageComponent = new ImageComponent(rightPosition, imageFileLocation);
         rightImageComponent.setSize(100,50);
 
         createPDFAndCompareWithExpected(expectedOutputFilePath,
                                         outFilePath,
                                         leftImageComponent,
                                         centerImageComponent,
                                         rightImageComponent);
     }
 
     @Test
     public void testRenderComponent_invalidFilePath_throwsError() {
 
         String expectedOutputFilePath = inputPackage + "image_renderer_expected_output2.pdf";
         String outFilePath = outputPackage + "image_renderer_output2.pdf";
 
         DocPosition leftPosition = new DocPosition(HorizontalAlignment.LEFT);
         ImageComponent imageComponent = new ImageComponent(leftPosition, "i dont exist");
         imageComponent.setSize(10, 10);
 
         try {
             createPDFAndCompareWithExpected(expectedOutputFilePath,
                                             outFilePath,
                                             imageComponent);
         } catch (DocGenException e) {
             assertEquals("java.io.FileNotFoundException: C:\\GitHub\\docgen\\core\\i dont exist (The system cannot find the file specified)", e.getMessage());
             return;
         }
 
         fail();
     }
 }
