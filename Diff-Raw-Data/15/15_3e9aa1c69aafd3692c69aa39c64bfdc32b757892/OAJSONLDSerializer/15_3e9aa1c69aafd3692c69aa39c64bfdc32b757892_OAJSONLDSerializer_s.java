 package net.metadata.openannotation.lorestore.util;
 
 import org.ontoware.aifbcommons.collection.ClosableIterator;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.ontoware.rdf2go.model.Statement;
 import org.ontoware.rdf2go.model.node.DatatypeLiteral;
 import org.ontoware.rdf2go.model.node.LanguageTagLiteral;
 import org.ontoware.rdf2go.model.node.Literal;
 import org.ontoware.rdf2go.model.node.Resource;
 import org.ontoware.rdf2go.model.node.URI;
 import org.ontoware.rdf2go.model.node.Variable;
 import org.apache.log4j.Logger;
 import de.dfki.km.json.jsonld.JSONLDProcessingError;
 /* 
  * Based on 
  * https://github.com/ismriv/jsonld-rdf2go/blob/master/src/main/java/ie/deri/smile/jsonld/rdf2go/RDF2GoJSONLDSerializer.java
  * 
  * Modified fore lorestore to work with new JSONLD API and to ignore schema triples
  * 
  * Copyright (c) 2012 Ismael Rivera
 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 public class OAJSONLDSerializer extends de.dfki.km.json.jsonld.JSONLDSerializer {
     protected final Logger LOG = Logger.getLogger(OAJSONLDSerializer.class);
         public void importModel(Model model) {
             ClosableIterator<Statement> statements = model.iterator();
             while (statements.hasNext()) {
                 Statement statement = statements.next();
                 handleStatement(statement);
             }
             statements.close();
         }
 
         public void importModelSet(ModelSet modelSet) {
            ClosableIterator<Model> models = null;
            models = modelSet.getModels();
            while(models.hasNext()){
                Model model = models.next();
                ClosableIterator<Statement> statements = modelSet.findStatements(model.getContextURI(), Variable.ANY, Variable.ANY, Variable.ANY);
                while (statements.hasNext()) {
                     handleStatement(statements.next());
                }
                statements.close();
                
             }
            models.close();
         }
 
         public void handleStatement(Statement nextStatement) {
             Resource subject = nextStatement.getSubject();
             URI predicate = nextStatement.getPredicate();
             Object object = nextStatement.getObject();
             String subjString = subject.toString(),
                predString = predicate.toString(),
                objString = object.toString();
             // don't serialize schema triples
             if (subjString.startsWith("http://www.w3.org/2000/01/rdf-schema") ||
                 subjString.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns") ||
                 subjString.startsWith("http://www.openannotation.org/ns/") ||
                 subjString.startsWith("http://purl.org/dc/terms/") ||
                 predString.startsWith("http://www.w3.org/2000/01/rdf-schema#subClassOf") ||
                 predString.startsWith("http://www.w3.org/2000/01/rdf-schema#subPropertyOf") ||
                 objString.startsWith("http://www.w3.org/2000/01/rdf-schema#Property") ||
                 objString.startsWith("http://www.w3.org/2000/01/rdf-schema#Resource") ||
                 objString.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns")){
                 return;
                 
             }
             
             if (object instanceof DatatypeLiteral) {
                 DatatypeLiteral literal = (DatatypeLiteral) object;
                 String value = literal.getValue();
                 String datatype = literal.getDatatype().toString();
                 triple(subjString, predString, value, datatype, null);
             } else if (object instanceof LanguageTagLiteral) {
                 LanguageTagLiteral literal = (LanguageTagLiteral) object;
                 String value = literal.getValue();
                 triple(subjString, predString, value, null, literal.getLanguageTag());
             } else if (object instanceof Literal) {
                 Literal literal = (Literal) object;
                 String value = literal.getValue();
                 triple(subjString, predString, value, null, null);
             } else {
                 triple(subjString, predString, objString);
             }
         }
 
         @Override
         public void parse(Object input) throws JSONLDProcessingError {
             if (input == null){
                 return;
             }
             if (input instanceof Model) {
                 importModel((Model)input);
             } else if (input instanceof ModelSet){
                 importModelSet((ModelSet) input);
             } else {
                 throw new JSONLDProcessingError("JSON LD Serializer expects Model or ModelSet input");
             }
         }
 }
