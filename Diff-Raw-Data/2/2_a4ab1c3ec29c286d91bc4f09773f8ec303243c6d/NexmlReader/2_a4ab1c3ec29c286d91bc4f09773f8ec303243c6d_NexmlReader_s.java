 package mesquite.nexml.InterpretNEXML.NexmlReaders;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Enumeration;
 import java.net.URI;
 import java.lang.reflect.Method;
 
 import com.sun.tools.example.debug.bdi.MethodNotFoundException;
 import mesquite.lib.*;
 import mesquite.nexml.InterpretNEXML.NexmlMesquiteManager;
 import mesquite.nexml.InterpretNEXML.AnnotationHandlers.AnnotationWrapper;
 import mesquite.nexml.InterpretNEXML.AnnotationHandlers.PredicateHandler;
 
 import org.nexml.model.Annotatable;
 import org.nexml.model.Annotation;
 import org.nexml.model.Document;
 import org.nexml.model.Matrix;
 import org.nexml.model.OTUs;
 import org.nexml.model.TreeBlock;
 
 public class NexmlReader extends NexmlMesquiteManager {
 
 	/**
 	 *
 	 * @param employerEmployee
 	 */
 	public NexmlReader (EmployerEmployee employerEmployee) {
 		super(employerEmployee);
 	}
 
 	/**
 	 *
 	 * @param xmlDocument
 	 * @param mesProject
 	 * @return
 	 */
 	public MesquiteProject fillProjectFromNexml(Document xmlDocument,MesquiteProject mesProject) {
         resetNamespaceHandlers();
 		List<OTUs> xmlOTUsList = xmlDocument.getOTUsList();
 		MesquiteFile mesFile = mesProject.getFile(0);
 		// process taxa blocks
 		NexmlOTUsBlockReader nobr = new NexmlOTUsBlockReader(getEmployerEmployee());
 		List<Annotatable> xmlAnnoOTUsList = new ArrayList<Annotatable>();
 		for ( OTUs xmlOTUs : xmlDocument.getOTUsList() ) {
 			xmlAnnoOTUsList.add(xmlOTUs);
 		}
 		nobr.readBlocks(mesProject, mesFile, xmlAnnoOTUsList);
 
 		for ( OTUs xmlOTUs : xmlOTUsList ) {
 
 			// process tree blocks
 			NexmlTreeBlockReader ntbr = new NexmlTreeBlockReader(getEmployerEmployee());
 			List<Annotatable> xmlAnnoTreeBlockList = new ArrayList<Annotatable>();
 			for ( TreeBlock xmlTreeBlock : xmlDocument.getTreeBlockList(xmlOTUs) ) {
 				xmlAnnoTreeBlockList.add(xmlTreeBlock);
 			}
 			ntbr.readBlocks(mesProject, mesFile, xmlAnnoTreeBlockList);
 
 			// process characters blocks
 			NexmlCharactersBlockReader ncbr = new NexmlCharactersBlockReader(getEmployerEmployee());
 			List<Annotatable> xmlCharactersBlockList = new ArrayList<Annotatable>();
 			for ( Matrix<?> xmlMatrix : xmlDocument.getMatrices(xmlOTUs) ) {
 				xmlCharactersBlockList.add(xmlMatrix);
 			}
 			ncbr.readBlocks(mesProject, mesFile, xmlCharactersBlockList);
 		}
         for (Enumeration nhEnumeration = getActiveNamespaceHandlers(); nhEnumeration.hasMoreElements() ;) {
             URI uri = (URI) nhEnumeration.nextElement();
             PredicateHandler handler = getNamespaceHandlerFromURI(uri);
             Method method = null;
             Class[] args = new Class[1];
             args[0] = MesquiteProject.class;
             try {
                 method = handler.getClass().getMethod("initializeMesquiteProject", args);
             } catch (Exception e) {
                if (!(e instanceof MethodNotFoundException)) {
                     e.printStackTrace();
                 } else {
                     debug("tried to initializeMesquiteProject on class "+handler.getClass().toString());
                 }
             }
             if (method != null) {
                 try {
                     method.invoke(handler, mesProject);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         return mesProject;
 	}
 
 	/**
 	 *
 	 * @param mesAttachable
 	 * @param mesAnnotatable
 	 */
 	protected void readWrappedAnnotations(Attachable mesAttachable,Annotatable mesAnnotatable) {
 		for ( Annotation annotation : mesAnnotatable.getAllAnnotations() ) {
 			String name = annotation.getProperty();
 			if ( null == name || "".equals(name) ) {
 				name = annotation.getRel();
 			}
 			AnnotationWrapper aw = new AnnotationWrapper();
 			aw.setValue(annotation.getValue());
 			aw.setPredicateNamespace(annotation.getPredicateNamespace());
 			aw.setName(name);
 			mesAttachable.attach(aw);
 		}
 	}
     /**
 	 *
 	 * @param mesAssociable
 	 * @param xmlAnnotatable
 	 * @param segmentCount
 	 * @param mesListable
 	 */
 	protected void readAnnotations(Associable mesAssociable, Annotatable xmlAnnotatable,int segmentCount,Listable mesListable) {
 		for ( Annotation xmlAnnotation : xmlAnnotatable.getAllAnnotations() ) {
             URI namespace = xmlAnnotation.getPredicateNamespace();
             if (namespace == null) {
                 MesquiteMessage.discreetNotifyUser("no namespace defined for XML annotation "+xmlAnnotation.getProperty());
                 continue;
             }
 			PredicateHandler handler = getNamespaceHandler(xmlAnnotatable,xmlAnnotation);
             handler.read(mesAssociable, mesListable, segmentCount);
         }
 	}
 }
