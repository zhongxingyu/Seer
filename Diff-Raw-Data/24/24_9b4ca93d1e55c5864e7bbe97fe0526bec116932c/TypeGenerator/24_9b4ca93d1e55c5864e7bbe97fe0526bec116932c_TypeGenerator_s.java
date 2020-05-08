 package de.nordakademie.nakjavagenerator.generators;
 
 import java.io.IOException;
 import java.util.Set;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.tools.JavaFileObject;
 
 @SupportedAnnotationTypes(value = { "de.nordakademie.nakjava.server.internal.model.VisibleField" })
 public class TypeGenerator extends AbstractProcessor {
 
 	private Filer filer;
 	private VisibleModelFieldsClass sourceClass;
 
 	@Override
 	public synchronized void init(ProcessingEnvironment processingEnv) {
 		// TODO Auto-generated method stub
 		super.init(processingEnv);
 		filer = processingEnv.getFiler();
 		sourceClass = new VisibleModelFieldsClass();
 	}
 
 	@Override
 	public boolean process(Set<? extends TypeElement> annotations,
 			RoundEnvironment roundEnv) {
 
 		TypeElement annotation;
 
 		if (annotations.size() == 1) {
 			annotation = annotations.iterator().next();
 
 			for (Element element : roundEnv
 					.getElementsAnnotatedWith(annotation)) {
 
 				sourceClass.addField(new VisibleModelField(element.asType()
 						.toString(), element.getEnclosingElement()
 						.getSimpleName().toString().toUpperCase()
 						+ "_"
 						+ element.getSimpleName().toString().toUpperCase(),
 						element.getEnclosingElement().asType().toString() + "."
 								+ element.getSimpleName()));
 			}
		}
 
		if (roundEnv.processingOver()) {
 			finish();
 		}
 
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	private void finish() {
 		try {
 			JavaFileObject file = filer.createSourceFile(
 					"de/nordakademie/nakjava/generated/VisibleModelFields",
 					null);
 			file.openWriter().append(sourceClass.getSourceContent()).close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
