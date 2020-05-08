 package seg.jUCMNav.importexport;
 
 import grl.Actor;
 import grl.ActorRef;
 import grl.Contribution;
 import grl.ContributionType;
 import grl.Decomposition;
 import grl.DecompositionType;
 import grl.Dependency;
 import grl.GRLGraph;
 import grl.IntentionalElement;
 import grl.IntentionalElementRef;
 import grl.IntentionalElementType;
 import java.io.FileInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import seg.jUCMNav.extensionpoints.IURNImport;
 import seg.jUCMNav.model.ModelCreationFactory;
 import seg.jUCMNav.model.commands.changeConstraints.ContainerRefBindChildCommand;
 import seg.jUCMNav.model.commands.create.AddContainerRefCommand;
 import seg.jUCMNav.model.commands.create.AddIntentionalElementRefCommand;
 import seg.jUCMNav.model.commands.create.AddMetadataCommand;
 import seg.jUCMNav.model.commands.create.CreateElementLinkCommand;
 import seg.jUCMNav.model.commands.create.CreateGrlGraphCommand;
 import seg.jUCMNav.model.util.URNNamingHelper;
 import seg.jUCMNav.views.property.LinkRefPropertySource;
 import urn.URNspec;
 import urncore.IURNContainerRef;
 import urncore.Metadata;
 
 /**
  * This class import a GRL catalog from an xml file
  * 
  * @author Jean-Franois Roy
  */
 public class ImportGRLCatalog extends DefaultHandler implements IURNImport {
 
     private URNspec urn;
     private GRLGraph graph;
     private HashMap map;
     private int state;
     private ArrayList idList;
 
     private Vector autolayoutDiagrams;
 
     /*
      * (non-Javadoc)
      * 
      * @see seg.jUCMNav.extensionpoints.IURNImport#importURN(java.io.FileInputStream)
      */
     public URNspec importURN(FileInputStream fis, Vector autolayoutDiagrams) throws InvocationTargetException {
        URNspec urn = ModelCreationFactory.getNewURNspec(false, false, false);
 
         // Remove ucm diagram (only one diagram is insert by default).
         // urn.getUrndef().getSpecDiagrams().remove(0);
 
         return importURN(fis, urn, autolayoutDiagrams);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see seg.jUCMNav.extensionpoints.IURNImport#importURN(java.lang.String)
      */
     public URNspec importURN(String filename, Vector autolayoutDiagrams) throws InvocationTargetException {
         // not used
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see seg.jUCMNav.extensionpoints.IURNImport#importURN(java.io.FileInputStream, urn.URNspec)
      */
     public URNspec importURN(FileInputStream fis, URNspec urn, Vector autolayoutDiagrams) throws InvocationTargetException {
         this.urn = (URNspec) EcoreUtil.copy(urn);
         this.map = new HashMap();
         this.autolayoutDiagrams = autolayoutDiagrams;
         this.state = -1;
         this.idList = new ArrayList();
         // Use the default parser
         SAXParserFactory factory = SAXParserFactory.newInstance();
         factory.setValidating(true);
         try {
             // Parse the input
             SAXParser saxParser = factory.newSAXParser();
 
             saxParser.parse(fis, this);
 
             // Sanitize urnspec to resolve naming conflict
             URNNamingHelper.sanitizeURNspec(this.urn);
 
         } catch (Exception e) {
             throw new InvocationTargetException(e);
         }
         return this.urn;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see seg.jUCMNav.extensionpoints.IURNImport#importURN(java.lang.String, urn.URNspec)
      */
     public URNspec importURN(String filename, URNspec urn, Vector autolayoutDiagrams) throws InvocationTargetException {
         // not used
         return null;
     }
 
     // SAXParser function
     public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
         if ("grl-catalog".equals(qName)) {//Root element //$NON-NLS-1$
             // Create a new GRLGraph in the urnspec
             CreateGrlGraphCommand graphCmd = new CreateGrlGraphCommand(urn);
 
             if (state == -1)
                 state=0;
             else
             {
                 throw new SAXException("Could not create a GrlGraph"); //$NON-NLS-1$
             }
 
             if (graphCmd.canExecute()) {
                 graph = graphCmd.getDiagram();
                 graphCmd.execute();
                 graph.setDescription(attrs.getValue("description")); //$NON-NLS-1$
                 if (attrs.getValue("name") != null) { //$NON-NLS-1$
                     graph.setName(attrs.getValue("name")); //$NON-NLS-1$
                 }
                 // Add the diagram in the autolayout vector
                 autolayoutDiagrams.add(graph.getId());
             } else {
                 throw new SAXException("Could not create a GrlGraph"); //$NON-NLS-1$
             }
         } else if ("intentional-element".equals(qName)) { //$NON-NLS-1$
             if (state != 1)
             {
                 throw new SAXException("<intentional-element> not at the right place..."); //$NON-NLS-1$
             }
             // Create the intentional element
             IntentionalElementType type = IntentionalElementType.get(attrs.getValue("type")); //$NON-NLS-1$
             IntentionalElementRef ref = (IntentionalElementRef) ModelCreationFactory.getNewObject(urn, IntentionalElementRef.class, type.getValue());
 
             AddIntentionalElementRefCommand elementCmd = new AddIntentionalElementRefCommand(graph, ref);
             if (elementCmd.canExecute()) {
                 elementCmd.execute();
                 // Set the definition properties define in the catalog
                 ref.getDef().setDecompositionType(DecompositionType.get(attrs.getValue("decompositiontype"))); //$NON-NLS-1$
 
                 ref.getDef().setDescription(attrs.getValue("description")); //$NON-NLS-1$
 
                 if (URNNamingHelper.isNameValid(ref.getDef(), attrs.getValue("name")).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                     ref.getDef().setName(attrs.getValue("name")); //$NON-NLS-1$
                 } else {
                     ref.getDef().setName(attrs.getValue("name")); //$NON-NLS-1$
                     URNNamingHelper.resolveNamingConflict(urn, ref.getDef());
                 }
 
                 // Add the new element in the hashmap for reference from links
                 map.put(attrs.getValue("id"), ref.getDef()); //$NON-NLS-1$
             } else {
                 throw new SAXException("Could not create IntentionalElementRef " + attrs.getValue("name")); //$NON-NLS-1$ //$NON-NLS-2$
             }
         } else if ( "metadata".equals(qName) && state == 1 ) { //$NON-NLS-1$
             if ( state != 1 ) 
             {
                 System.out.println( "state problem!!!" );  
                 throw new SAXException("<metadata> not at the right place..."); //$NON-NLS-1$
             } 
             else 
             {
                 IntentionalElement IE = (IntentionalElement) map.get( attrs.getValue( "elem" ) ); //$NON-NLS-1$
                 
                 if ( IE == null )
                 {   
                     System.out.println( "IE problem!!!" );
                     throw new SAXException("Invalid intentionalelement in containment link"); //$NON-NLS-1$
                 }
               
                 Metadata metadataObject = (Metadata) ModelCreationFactory.getNewObject( urn, Metadata.class );
               
                 metadataObject.setName( attrs.getValue( "name" ) ); //$NON-NLS-1$
                 metadataObject.setValue( attrs.getValue( "value" ) ); //$NON-NLS-1$
                 AddMetadataCommand metadataCommand = new AddMetadataCommand( IE, metadataObject, null );
                 metadataCommand.execute();               
             }
         
       } else if ("dependency".equals(qName)) { //$NON-NLS-1$
             if (state != 2)
             {
                 throw new SAXException("<dependency> not at the right place..."); //$NON-NLS-1$
             }
             // Create a dependency between the 2 elements
             IntentionalElement dependee = (IntentionalElement) map.get(attrs.getValue("dependeeid")); //$NON-NLS-1$
             IntentionalElement depender = (IntentionalElement) map.get(attrs.getValue("dependerid")); //$NON-NLS-1$
 
             if (dependee == null || depender == null) {
                 throw new SAXException("Invalid intentional element id in dependency"); //$NON-NLS-1$
             }
             Dependency depend = (Dependency) ModelCreationFactory.getNewObject(urn, Dependency.class);
             CreateElementLinkCommand linkCmd = new CreateElementLinkCommand(urn, depender, depend);
             linkCmd.setTarget(dependee);
             if (linkCmd.canExecute()) {
                 linkCmd.execute();
                 // Set the name and description
                 depend.setName(attrs.getValue("name")); //$NON-NLS-1$
                 depend.setDescription(attrs.getValue("description")); //$NON-NLS-1$
             } else {
                 throw new SAXException("Could not create Dependency"); //$NON-NLS-1$
             }
         } else if ("decomposition".equals(qName)) { //$NON-NLS-1$
             if (state != 2)
             {
                 throw new SAXException("<decomposition> not at the right place..."); //$NON-NLS-1$
             }
             // Create a decomposition between the 2 elements
             IntentionalElement src = (IntentionalElement) map.get(attrs.getValue("srcid")); //$NON-NLS-1$
             IntentionalElement dest = (IntentionalElement) map.get(attrs.getValue("destid")); //$NON-NLS-1$
 
             if (src == null || dest == null) {
                 throw new SAXException("Invalid intentional element id in decomposition"); //$NON-NLS-1$
             }
             Decomposition decomp = (Decomposition) ModelCreationFactory.getNewObject(urn, Decomposition.class);
             CreateElementLinkCommand linkCmd = new CreateElementLinkCommand(urn, src, decomp);
             linkCmd.setTarget(dest);
             if (linkCmd.canExecute()) {
                 // Set the name and description
                 decomp.setName(attrs.getValue("name")); //$NON-NLS-1$
                 decomp.setDescription(attrs.getValue("description")); //$NON-NLS-1$
                 linkCmd.execute();
             } else {
                 throw new SAXException("Could not create Decomposition"); //$NON-NLS-1$
             }
         } else if ("contribution".equals(qName) && state == 2) { //$NON-NLS-1$
             if (state != 2)
             {
                 throw new SAXException("<contribution> not at the right place..."); //$NON-NLS-1$
             }
             // Create a contribution between the 2 elements
             IntentionalElement src = (IntentionalElement) map.get(attrs.getValue("srcid")); //$NON-NLS-1$
             IntentionalElement dest = (IntentionalElement) map.get(attrs.getValue("destid")); //$NON-NLS-1$
 
             if (src == null || dest == null) {
                 throw new SAXException("Invalid intentional element id in contribution"); //$NON-NLS-1$
             }
             Contribution contrib = (Contribution) ModelCreationFactory.getNewObject(urn, Contribution.class);
             CreateElementLinkCommand linkCmd = new CreateElementLinkCommand(urn, src, contrib);
             linkCmd.setTarget(dest);
             if (linkCmd.canExecute()) {
                 linkCmd.execute();
                 // Set the name and description
                 contrib.setName(attrs.getValue("name")); //$NON-NLS-1$
                 contrib.setDescription(attrs.getValue("description")); //$NON-NLS-1$
                 // Set the contribution type, qualitative and quantitative
                 contrib.setContribution(ContributionType.get(attrs.getValue("contributiontype"))); //$NON-NLS-1$
                 String quantitativeContrib=attrs.getValue("quantitativeContribution");  //$NON-NLS-1$
                 if (quantitativeContrib != null)
                     contrib.setQuantitativeContribution(Integer.parseInt(quantitativeContrib));
                 else
                 {
                     // Older .grl file, without quantitative contribution. 
                     // Use mapping from qualitative contribution.
                     LinkRefPropertySource.syncElementLinkQuantitativeContribution(contrib, contrib.getContribution());
                 }
                 // Set the correlation
                 if (attrs.getValue("correlation") == "true") { //$NON-NLS-1$ //$NON-NLS-2$
                     contrib.setCorrelation(true);
                 } else {
                     contrib.setCorrelation(false);
                 }
                 
                 // Add the new element in the hashmap for reference from links
                 map.put(attrs.getValue("name"), contrib); //$NON-NLS-1$
             } else {
                 throw new SAXException("Could not create Contribution"); //$NON-NLS-1$
             }
         } else if ( "metadata".equals(qName) && state == 2 ) { //$NON-NLS-1$
             if ( state != 2 ) 
             {
                 System.out.println( "state problem!!!" );  
                 throw new SAXException("<metadata> not at the right place..."); //$NON-NLS-1$
             } 
             else 
             {
                 Contribution C = (Contribution) map.get( attrs.getValue( "elem" )); //$NON-NLS-1$               
                   
                 if ( C == null )
                 {   
                     System.out.println( "Contribution recognition problem!!!" );
                     throw new SAXException("Invalid Contribution recognition in containment link"); //$NON-NLS-1$
                 }
                 
                 Metadata metadataObject = (Metadata) ModelCreationFactory.getNewObject( urn, Metadata.class );
                 
                 metadataObject.setName( attrs.getValue( "name" ) ); //$NON-NLS-1$
                 metadataObject.setValue( attrs.getValue( "value" ) ); //$NON-NLS-1$
                 AddMetadataCommand metadataCommand = new AddMetadataCommand( C, metadataObject, null );
                 metadataCommand.execute();               
             }
         } else if ("actor".equals(qName)) { //$NON-NLS-1$
             if (state != 3)
             {
                 throw new SAXException("<actorContIE> not at the right place..."); //$NON-NLS-1$
             }
 
             // Create the actor
             ActorRef ref = (ActorRef) ModelCreationFactory.getNewObject(urn, ActorRef.class);
 
             AddContainerRefCommand elementCmd = new AddContainerRefCommand(graph, ref);
             if (elementCmd.canExecute()) {
                 elementCmd.execute();
                 // Set the definition properties defined in the catalog
                 Actor actor = (Actor)ref.getContDef();
                 actor.setDescription(attrs.getValue("description")); //$NON-NLS-1$
 
                 if (URNNamingHelper.isNameValid(actor, attrs.getValue("name")).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                     actor.setName(attrs.getValue("name")); //$NON-NLS-1$
                 } else {
                     actor.setName(attrs.getValue("name")); //$NON-NLS-1$
                     URNNamingHelper.resolveNamingConflict(urn, actor);
                 }
 
                 // Add the new element in the hashmap for reference from links
                 map.put(attrs.getValue("id"), actor); //$NON-NLS-1$
             } else {
                 throw new SAXException("Could not create ActorRef " + attrs.getValue("name")); //$NON-NLS-1$ //$NON-NLS-2$
             }
         } else if ("actorContIE".equals(qName)) { //$NON-NLS-1$
             if (state != 4)
             {
                 throw new SAXException("<actorContIE> not at the right place..."); //$NON-NLS-1$
             }
 
             // Create a containment link from between the actor reference and the IE reference
             Actor actor = (Actor) map.get(attrs.getValue("actor")); //$NON-NLS-1$
             IntentionalElement ie = (IntentionalElement) map.get(attrs.getValue("ie")); //$NON-NLS-1$         
 
             if (actor == null || ie == null) {
                 throw new SAXException("Invalid actor or IE id in containment link"); //$NON-NLS-1$
             }
 
             if (!idList.contains(attrs.getValue("ie"))) { //$NON-NLS-1$
                 ContainerRefBindChildCommand linkCmd = new ContainerRefBindChildCommand((IURNContainerRef)actor.getContRefs().get(0), (IntentionalElementRef)ie.getRefs().get(0));
                 idList.add(attrs.getValue("ie")); //$NON-NLS-1$
                 if (linkCmd.canExecute()) {
                     linkCmd.execute();
                 } else {
                     throw new SAXException("Could not create containment link"); //$NON-NLS-1$
                 }
             }
             // Else: skip... element already bound!
         } else if ("intentional-element-ref".equals(qName)) {
             if (state != 5) {
                 System.out.println( "state problem!!!" );  
                 throw new SAXException("<intentional-element-ref> not at the right place..."); //$NON-NLS-1$
             } else {
                 // Create the intentional element
                 IntentionalElementType type = IntentionalElementType.get(attrs.getValue("type")); //$NON-NLS-1$
                 IntentionalElementRef ref = (IntentionalElementRef) ModelCreationFactory.getNewObject(urn, IntentionalElementRef.class, 
                                              type.getValue(), map.get(attrs.getValue("definitionid")));
                 
                 AddIntentionalElementRefCommand elementCmd = new AddIntentionalElementRefCommand(graph, ref);
                 if (elementCmd.canExecute()) {
                     elementCmd.execute();
                     // Set the definition properties define in the catalog
                     //ref.getDef().setDecompositionType(DecompositionType.get(attrs.getValue("decompositiontype"))); //$NON-NLS-1$
     
                     //ref.getDef().setDescription(attrs.getValue("description")); //$NON-NLS-1$
                     
                     if (URNNamingHelper.isNameValid(ref.getDef(), attrs.getValue("name")).equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                         ref.setName(attrs.getValue("name")); //$NON-NLS-1$
                     } else {
                         ref.setName(attrs.getValue("name")); //$NON-NLS-1$
                         URNNamingHelper.resolveNamingConflict(urn, ref.getDef());
                     }
                     
                     // Add the new element in the hashmap for reference from links
                     map.put(attrs.getValue("id"), ref.getDef()); //$NON-NLS-1$
                 }
             }
         } else if ( "metadata".equals(qName) && state == 5 ) { //$NON-NLS-1$
             if ( state != 5 ) {
                 System.out.println( "state problem!!!" );  
                 throw new SAXException("<metadata> not at the right place..."); //$NON-NLS-1$
             } else {
                 IntentionalElement C = (IntentionalElement) map.get( attrs.getValue( "elem" )); //$NON-NLS-1$               
                 if ( C == null ) {   
                     System.out.println( "intetnional element ref recognition problem!!!" );
                     throw new SAXException("Invalid intetnional element ref recognition in containment link"); //$NON-NLS-1$
                 }
                 
                 Metadata metadataObject = (Metadata) ModelCreationFactory.getNewObject( urn, Metadata.class );
                 
                 metadataObject.setName( attrs.getValue( "name" ) ); //$NON-NLS-1$
                 metadataObject.setValue( attrs.getValue( "value" ) ); //$NON-NLS-1$
                 AddMetadataCommand metadataCommand = new AddMetadataCommand( C, metadataObject, null );
                 metadataCommand.execute();       
             }
         } else if ("contribution".equals(qName) && state == 6) { //$NON-NLS-1$
               if (state != 6) {
                   throw new SAXException("<contribution> of refs not at the right place..."); //$NON-NLS-1$
               }
               // Create a contribution between the 2 elements
               IntentionalElement src = (IntentionalElement) map.get(attrs.getValue("srcid")); //$NON-NLS-1$
               IntentionalElement dest = (IntentionalElement) map.get(attrs.getValue("destid")); //$NON-NLS-1$
 
               if (src == null || dest == null) {
                   throw new SAXException("Invalid intentional element id in contribution"); //$NON-NLS-1$
               }
               
               Contribution contrib = (Contribution) ModelCreationFactory.getNewObject(urn, Contribution.class);
               CreateElementLinkCommand linkCmd = new CreateElementLinkCommand(urn, src, contrib);
               linkCmd.setTarget(dest);
               if (linkCmd.canExecute()) {
                   linkCmd.execute();
                   // Set the name and description
                   contrib.setName(attrs.getValue("name")); //$NON-NLS-1$
                   contrib.setDescription(attrs.getValue("description")); //$NON-NLS-1$
                   // Set the contribution type, qualitative and quantitative
                   contrib.setContribution(ContributionType.get(attrs.getValue("contributiontype"))); //$NON-NLS-1$
                   String quantitativeContrib=attrs.getValue("quantitativeContribution");  //$NON-NLS-1$
                   if (quantitativeContrib != null)
                       contrib.setQuantitativeContribution(Integer.parseInt(quantitativeContrib));
                   else {
                       // Older .grl file, without quantitative contribution. 
                       // Use mapping from qualitative contribution.
                       LinkRefPropertySource.syncElementLinkQuantitativeContribution(contrib, contrib.getContribution());
                   }
                   // Set the correlation
                   if (attrs.getValue("correlation") == "true") { //$NON-NLS-1$ //$NON-NLS-2$
                       contrib.setCorrelation(true);
                   } else {
                       contrib.setCorrelation(false);
                   }
                   
                   // Add the new element in the hashmap for reference from links
                   map.put(attrs.getValue("name"), contrib); //$NON-NLS-1$
               } else {
                   throw new SAXException("Could not create Contribution"); //$NON-NLS-1$
               }
         } else if ( "metadata".equals(qName) && state == 6 ) { //$NON-NLS-1$
             if ( state != 6 ) 
             {
                 System.out.println( "state problem!!!" );  
                 throw new SAXException("<metadata> not at the right place..."); //$NON-NLS-1$
             } 
             else 
             {
                 Contribution C = (Contribution) map.get( attrs.getValue( "elem" )); //$NON-NLS-1$               
                   
                 if ( C == null )
                 {   
                     System.out.println( "Contribution recognition problem!!!" );
                     throw new SAXException("Invalid Contribution recognition in containment link"); //$NON-NLS-1$
                 }
                 
                 Metadata metadataObject = (Metadata) ModelCreationFactory.getNewObject( urn, Metadata.class );
                 
                 metadataObject.setName( attrs.getValue( "name" ) ); //$NON-NLS-1$
                 metadataObject.setValue( attrs.getValue( "value" ) ); //$NON-NLS-1$
                 AddMetadataCommand metadataCommand = new AddMetadataCommand( C, metadataObject, null );
                 metadataCommand.execute();               
             }
         } else if ("element-def".equals(qName)) { //$NON-NLS-1$
             if (state == 0)
                 state=1;
             else
             {
                 throw new SAXException("<element-def> not at the right place..."); //$NON-NLS-1$
             }
         } else if ("link-def".equals(qName)) { //$NON-NLS-1$
             if (state == 1)
                 state=2;
             else
             {
                 throw new SAXException("<link-def> not at the right place..."); //$NON-NLS-1$
             }
         } else if ("actor-def".equals(qName)) { //$NON-NLS-1$
             if (state == 2)
                 state=3;
             else
             {
                 throw new SAXException("<actor-def> not at the right place..."); //$NON-NLS-1$
             }
         } else if ("actor-IE-link-def".equals(qName)) { //$NON-NLS-1$
             if (state == 3)
                 state=4;
             else
             {
                 throw new SAXException("<actor-IE-link-def> not at the right place..."); //$NON-NLS-1$
             }
         } else if ( "intentional-element-ref-def".equals( qName ) ) { //$NON-NLS-1$
               if ( state == 4 )
                   state = 5;
               else
               {
                   throw new SAXException("<intentional-element-ref-def> not at the right place..."); //$NON-NLS-1$
               }
         } else if ( "intentional-element-ref-link-def".equals( qName ) ) { //$NON-NLS-1$
             if ( state == 5 )
                 state = 6;
             else
             {
                 throw new SAXException("<intentional-element-ref-link-def> not at the right place..."); //$NON-NLS-1$
             }
         }         
         else {
             throw new SAXException("Could not parse element:" + qName); //$NON-NLS-1$
         }
     }
 }
