 package sk.stuba.fiit.perconik.core.java;
 
 import java.util.LinkedList;
 import org.eclipse.jdt.core.IClassFile;
 import org.eclipse.jdt.core.IJavaElement;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.JavaElementType;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 
 public final class ClassFiles
 {
 	private ClassFiles()
 	{
 		throw new AssertionError();
 	}
 	
 	public static final String path(final IClassFile file)
 	{
 		LinkedList<String> segments = Lists.newLinkedList();
 		
 		for (IJavaElement element: JavaElements.upToRoot(file))
 		{
 			switch (JavaElementType.valueOf(element))
 			{
 				case CLASS_FILE:
 				case PACKAGE_FRAGMENT:
					segments.addFirst(element.getElementName());
 					break;
 					
 				case PACKAGE_FRAGMENT_ROOT:
 					segments.addFirst(element.getPath().toString());
 					break;
 
 				default:
 					continue;
 			}
 		}
 		
 		return Joiner.on('/').join(segments);
 	}
 }
