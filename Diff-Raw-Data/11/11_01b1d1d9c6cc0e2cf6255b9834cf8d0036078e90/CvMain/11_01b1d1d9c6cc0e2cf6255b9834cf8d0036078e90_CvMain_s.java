 package org.isma.tools.cv;
 
 
 import org.isma.tools.cv.model.Cv;
 
 import java.awt.*;
 import java.io.File;
 
 public class CvMain {
     private final CvXmlReader reader = new CvXmlReader();
     private final CvGenerator generator = new CvGenerator();
 
     private File generate(String cvXmlFile, Template template) throws Exception {
         Cv cv = reader.read(cvXmlFile);
         return generator.generateCv(cv, template);
     }
 
     public static void main(String[] args) throws Exception {
         CvMain main = new CvMain();
         Template template00 = new Template("/test_00/", "cv00.jsp");
 
//        File cv = main.generate("cv_MAMECHE.xml", template00);
        File cv = main.generate("cv_HOUILLON.xml", template00);
         Desktop.getDesktop().open(cv.getParentFile());
     }
 }
