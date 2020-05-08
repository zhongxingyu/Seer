 // OpenPHACTS RDF Validator,
 // A tool for validating and storing RDF.
 //
 // Copyright 2012-2013  Christian Y. A. Brenninkmeijer
 // Copyright 2012-2013  University of Manchester
 // Copyright 2012-2013  OpenPhacts
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package uk.ac.manchester.cs.openphacts.ims.loader;
 
 import java.io.File;
 import java.util.List;
 import java.util.Set;
 import org.bridgedb.rdf.BridgeDBRdfHandler;
 import org.bridgedb.rdf.constants.VoidConstants;
 import org.bridgedb.sql.SQLUriMapper;
 import org.bridgedb.uri.UriListener;
 import org.bridgedb.uri.loader.LinksetHandler;
 import org.bridgedb.utils.BridgeDBException;
 import org.bridgedb.utils.StoreType;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.URIImpl;
 import uk.ac.manchester.cs.openphacts.ims.constants.DulConstants;
 import uk.ac.manchester.cs.openphacts.ims.loader.handler.ImsRdfHandler;
 import uk.ac.manchester.cs.openphacts.ims.loader.handler.PredicateFinderHandler;
 import uk.ac.manchester.cs.openphacts.ims.loader.handler.RdfInterfacteHandler;
 import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfReader;
 import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;
 import uk.ac.manchester.cs.openphacts.validator.Validator;
 import uk.ac.manchester.cs.openphacts.validator.ValidatorImpl;
 
 public class Loader 
 {
     private final Validator validator;
     private final RdfReader reader;
     private final UriListener uriListener;
             
     public Loader(StoreType storeType) throws BridgeDBException {
         validator = new ValidatorImpl();
         uriListener = SQLUriMapper.factory(false, storeType);
         reader = RdfFactoryIMS.getReader(storeType);
         BridgeDBRdfHandler.init();
     }
     
     private PredicateFinderHandler getPredicateFinderHandler(String uri, String rdfFormatName) throws BridgeDBException{
         PredicateFinderHandler finder = new PredicateFinderHandler();
         RdfParserPlus parser = new RdfParserPlus(finder);
         parser.parse(uri, rdfFormatName);
         return finder;
     }
     
     private PredicateFinderHandler getPredicateFinderHandler(File file, String rdfFormatName) throws BridgeDBException{
         PredicateFinderHandler finder = new PredicateFinderHandler();
         RdfParserPlus parser = new RdfParserPlus(finder);
         parser.parse(file, rdfFormatName);
         return finder;
     }
 
     private URI getObject(PredicateFinderHandler finder, URI predicate) throws BridgeDBException{
         Statement statement =  finder.getSinglePredicateStatements(predicate);
         if (statement != null){
             Value object = statement.getObject();
             if (object instanceof URI){
                 return (URI)object;
             }
             throw new BridgeDBException ("Unexpected Object in " + statement);
         }
         Integer count = finder.getPredicateCount(predicate);
         if (count == 0){
             throw new BridgeDBException("No statement found with predicate "+ predicate);
         }
         throw new BridgeDBException("Found " + count + " statements with predicate "+ predicate);
     }
     
     private URI getObject(Resource subject, URI predicate) throws VoidValidatorException, BridgeDBException {
         List<Statement> statements = reader.getStatementList(subject, predicate, null);
         if (statements == null || statements.isEmpty()){
             throw new BridgeDBException ("No statements found for subject " + subject + " and predicate " + predicate);
         }
         if (statements.size() == 1){
             return getObject(statements.get(0));
         } else {
             throw new BridgeDBException ("Found " + statements.size() + " statements for subject " + subject 
                     + " and predicate " + predicate);
         }
     }
 
     private Resource getLinksetId(PredicateFinderHandler finder) throws BridgeDBException{
         Statement statement =  finder.getSinglePredicateStatements(VoidConstants.LINK_PREDICATE);
         if (statement != null){
             return statement.getSubject();
         }
         statement =  finder.getSinglePredicateStatements(DulConstants.EXPRESSES);
         if (statement != null){
             return statement.getSubject();
         }
        throw new BridgeDBException("Unable to get LinksetrId");
     }
     
     public int load(String uri) throws VoidValidatorException, BridgeDBException{
         return load(uri, null);
     }
     
     public int load(String uri, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
         Resource context = new URIImpl(uri);
         PredicateFinderHandler finder = getPredicateFinderHandler(uri, rdfFormatName);
         RdfParserIMS parser = getParser(context, finder, null, null);
         parser.parse(uri, rdfFormatName);
         return parser.getMappingsetId();       
     }
 
     public int load(File file) throws VoidValidatorException, BridgeDBException{
         return load (file, null);
     }
     
     public int load(File file, String rdfFormatName) throws VoidValidatorException, BridgeDBException{
         return load (file, rdfFormatName, null, null);
     }
     
     public int load(File file, Set<String> viaLabels, Set<Integer> chainedLinkSets) 
             throws VoidValidatorException, BridgeDBException{
         return load (file, null,  viaLabels, chainedLinkSets);
     }
     
     public int load(File file, String rdfFormatName, Set<String> viaLabels, Set<Integer> chainedLinkSets) 
             throws VoidValidatorException, BridgeDBException{
         Resource context = new URIImpl(file.toURI().toString());
         PredicateFinderHandler finder = getPredicateFinderHandler(file, rdfFormatName);
         RdfParserIMS parser = getParser(context , finder, viaLabels, chainedLinkSets);
         parser.parse(file, rdfFormatName);
         return parser.getMappingsetId();       
     }
 
     public RdfParserIMS getParser(Resource context, PredicateFinderHandler finder, Set<String> viaLabels, 
            Set<Integer> chainedLinkSets) throws VoidValidatorException, BridgeDBException{
         Statement statement =  finder.getSinglePredicateStatements(VoidConstants.IN_DATASET);
         Resource linksetId;
         URI linkPredicate;
         String justification;    
         if (statement != null){
             linksetId  = statement.getSubject();
             Resource inDataSetLinkset = getObject(statement);
             linkPredicate = getObject(inDataSetLinkset, VoidConstants.LINK_PREDICATE);
             justification = getObject(inDataSetLinkset, DulConstants.EXPRESSES).stringValue();                
         } else {
             linksetId = getLinksetId(finder);
             linkPredicate = getObject(finder, VoidConstants.LINK_PREDICATE);
             justification = getObject(finder, DulConstants.EXPRESSES).stringValue();    
         }
         LinksetHandler linksetHandler = new LinksetHandler(uriListener, linkPredicate, justification, 
                 linksetId.stringValue(), true, viaLabels, chainedLinkSets);
         RdfInterfacteHandler readerHandler = new RdfInterfacteHandler(reader, context);
         ImsRdfHandler combinedHandler = 
                 new ImsRdfHandler(linksetHandler, readerHandler, linkPredicate);
         return new RdfParserIMS(linksetHandler, readerHandler, linkPredicate);
     }
 
     private URI getObject(Statement statement) throws BridgeDBException{
         if (statement.getObject() instanceof URI){
             return (URI)statement.getObject();
         } else {
             throw new BridgeDBException("Found statement " + statement + " but object is not a URI.");
         }
     }
     
 }
