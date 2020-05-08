 package test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.xtext.Constants;
 import org.eclipse.xtext.junit4.util.ParseHelper;
 import org.eclipse.xtext.resource.IResourceFactory;
 import org.eclipse.xtext.validation.CheckMode;
 import org.eclipse.xtext.validation.IResourceValidator;
 import org.eclipse.xtext.validation.Issue;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 public class ParserHelper2<T extends EObject> extends ParseHelper<T> {
 
     @Inject
     private IResourceFactory resourceFactory;
 
     @Inject
     private IResourceValidator validator;
 
     @Inject
     @Named(Constants.FILE_EXTENSIONS)
     public String fileExtension;
 
     public T parse(InputStream in, URI uriToUse, Map<?,?> options, ResourceSet resourceSet) {
         Resource resource = resourceFactory.createResource(uriToUse);
         resourceSet.getResources().add(resource);
         try {
             resource.load(in, options);
         } catch (IOException e) {
             throw new WrappedException(e);
         }
 
         validate(resource);
 
         @SuppressWarnings("unchecked")
         final T root = (T) (resource.getContents().isEmpty()? null : resource.getContents().get(0));
 
         return root;
     }
 
     public void validate(T model) {
         validate(model.eResource());
     }
 
     public void validate(Resource resource) {
         List<Issue> issues = validator.validate(resource, CheckMode.ALL, null);
         if(!issues.isEmpty()) {
             throw new XtextParseError(issues);
         }
     }
 
 
 }
