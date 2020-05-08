 /*
 New BSD license: http://opensource.org/licenses/bsd-license.php
 Copyright (c) 2003, 2004, 2005 Sun Microsystems, Inc.
 901 San Antonio Road, Palo Alto, CA 94303 USA.
 All rights reserved.
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
  */
 package net.java.rdf.sommer;
 
 import net.java.rdf.annotations.functional;
 import net.java.rdf.annotations.inverseFunctional;
 import net.java.rdf.annotations.rdf;
 import net.java.rdf.sommer.util.*;
 import org.openrdf.model.*;
 import org.openrdf.model.Statement;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.XMLSchema;
 import org.openrdf.query.*;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandlerException;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.n3.N3Writer;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.*;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import static java.util.logging.Level.FINE;
 import java.util.logging.Logger;
 import org.openrdf.rio.trig.TriGWriter;
 
 /**
  * @author Henry Story
  */
 public class SesameMapper extends Mapping {
 
    static public abstract class SesameInit implements Init {
 
       public Mapper create(String... uri) {
          return new SesameMapper(this, uri);
       }
 
       public abstract boolean hasInferencing();
 
       public abstract RepositoryConnection getConnection();
 
       public abstract ValueFactory getValueFactory();
 
       public void export(PrintStream out) {
          try {
             getConnection().export(new TriGWriter(out));
          } catch (RepositoryException ex) {
             Logger.getLogger(SesameMemorySailInit.class.getName()).log(Level.SEVERE, null, ex);
          } catch (RDFHandlerException ex) {
             Logger.getLogger(SesameMemorySailInit.class.getName()).log(Level.SEVERE, null, ex);
          }
       }
    }
    static transient Logger log = Logger.getLogger(SesameMapper.class.getName());
    public transient ValueFactory vf; //public for debugging only
    transient RepositoryConnection rep;
    Resource[] graphs;
    static DatatypeFactory xmldf = null;
    private static final TimeZone TZ = TimeZone.getTimeZone("UTC");
    private boolean inference = true; //to set on or off
 
    //enabling duplicate filters slows things down a lot, so this should be settable
    private boolean enableDuplicateFilter = true;
 
    /** leaky and dangerous as the array contents could be changed, but certainly should not be... should copy the contents*/
    public Resource[] grphs() {
       return graphs;
    }
 
 
    static {
       try {
          xmldf = DatatypeFactory.newInstance();
       } catch (DatatypeConfigurationException e) {
          e.printStackTrace(); //todo: decide what exception to throw
 
       }
    }
    SesameInit init;
 
    /**
     * Constructs a repository tied to a graph
     *
     * @param init Initialisation behavior
     * @param urls the name of the graph or null for the default graph
     */
    SesameMapper(SesameInit init, String... urls) {
       this.init = init;
       vf = init.getValueFactory();
       rep = init.getConnection();
       inference = init.hasInferencing();
       this.graphs = new Resource[urls.length];
       for (int i = 0; i < urls.length; i++) {
          this.graphs[i] = (urls[i] == null) ? null : vf.createURI(urls[i]);
       }
       this.writeGrphs = this.graphs[0];
       initInstanceMapper();
    }
    Resource writeGrphs;
 
    Resource getWriteGraphs() {
       return writeGrphs;
    }
 
    /**
     * add a context to the list of 'read only contexts'
     *
     * @param context
     */
    void addContext(String context) {
       Resource resrc = vf.createURI(context);
       boolean contains = false;
       for (int i = 0; i < graphs.length; i++) {
          if (graphs[i].equals(resrc)) {
             contains = true;
             break;
          }
       }
       if (!contains) {
          Resource newWg[] = new Resource[graphs.length + 1];
          java.lang.System.arraycopy(graphs, 0, newWg, 0, graphs.length);
          newWg[graphs.length] = resrc;
          graphs = newWg;
       }
    }
    /**
     * A mapping from objects to resources is one to one.
     * We only give one name to an object. The other owl:sameAs names can be found in the database.
     */
    private WeakHashMap<Object, Resource> obj2Resource = new WeakHashMap<Object, Resource>();
    /**
     * A mapping from resources to objects is one to many.
     * This is because many objects can have the same name. For example
     * - 2 objects implement different classes (one way to get multiple inheritance in java)
     * - another object is created in a constructor that is an ifp or cifp and so the new object receives the
     * same name
     */
    private WeakHashMap<Resource, List<Object>> resource2Obj = new WeakHashMap<Resource, List<Object>>();
    Date updateTime = new Date();
 
    //this is really the wrong way to do this but it should do for a demo
    public boolean isDirty(Date update) {
       return update == null || update.before(updateTime);
    }
 
    void setChanged() {
       updateTime = new Date();
    }
 
    /**
     * todo: It should be possible to set different repositories for different Maps. As such
     * todo: we may not want this to be static... At the same time we may also want different maps for different
     * todo: repositories
     *
     * @return
     */
    public RepositoryConnection rep() {
       if (rep == null) {
          vf = init.getValueFactory();
          rep = init.getConnection();
          inference = init.hasInferencing();
       }
       return rep;
    }
 
    public void importFrom(Reader r, String baseUri, String mimeType) throws IOException, GraphUpdateException, net.java.rdf.sommer.util.ParseException {
       try {
          rep().add(r, baseUri, RDFFormat.forMIMEType(mimeType), getWriteGraphs());
          rep.commit();
          setChanged();
       } catch (RDFParseException e) {
          log.severe("error line=" + e.getLineNumber() + " col=" +
                  e.getColumnNumber());
          throw new net.java.rdf.sommer.util.ParseException(e);
       } catch (RepositoryException e) {
          throw new GraphUpdateException(e);
       }
    }
 
    public void importInto(Reader r, String baseUri, String mimeType, String context) throws IOException, GraphUpdateException, ParseException {
       try {
          Resource ctxtRes = vf.createURI(context);
          rep.clear(ctxtRes); //todo: should really be cleared? Works ok for Beatnik, but...
 
          rep().add(r, baseUri, RDFFormat.forMIMEType(mimeType), ctxtRes);
          rep.commit();
          addContext(context);
          setChanged();
       } catch (RDFParseException e) {
          log.severe("error line=" + e.getLineNumber() + " col=" +
                  e.getColumnNumber());
          throw new net.java.rdf.sommer.util.ParseException(e);
       } catch (RepositoryException e) {
          throw new GraphUpdateException(e);
       }
    }
 
    /**
     * note this clear here only clears what was written to the graph.
     * it does not remove all the facts from the read only graphs. Is this more useful that what we wanted before?
     *
     * @return
     */
    public boolean clear() {
       obj2Resource.clear();
       resource2Obj.clear();
       try {
          rep().clear(getWriteGraphs());  //todo: if we could would we want to restore the deletions of the graphs we wrote to?
       } catch (RepositoryException e) {
          log.log(Level.WARNING, "could not clear graph " + graphs, e); //todo: decide what exception to throw
          return false;
       }
       return true;
    }
 
    public <T> RDFCollection<T> replaceVirtualCollection(Object thiz, rdf relation, Collection<T> oldColl, Collection<T> newColl, Type genType) {
       if (oldColl instanceof RDFCollection) {
          RDFCollection<T> rdfcoll = (RDFCollection<T>) oldColl;
          rdfcoll.replaceWith(newColl);
          return rdfcoll;
       } else {
          return createVirtualCollection(thiz, relation, newColl, genType);
       }
    }
 
    public <T> Collection<T> setCollectionField(Object sourceObj, rdf relation, Collection<T> newColl, Type type) {
 //        log.fine("setting collection for obj=" + obj + " R=" + relation.value() + " v=" + coll + "multiple=" + multiple);
 
       //todo: wrong! what if the newCollection is linked to another object? TEST TEST TEST
       if (newColl instanceof RDFCollection) {
          return newColl;
       }
       URI rel = vf.createURI(relation.value());
       RDFCollection<T> wrapped;
       Resource id = (Resource) map(sourceObj);
       //what should I really do if coll is null? return null perhaps.
 
       //mhh what if the collection allready contains elements?
       //what happens if people add objects to coll (the contained collection)?
       //1. see if we allready have a bnode at the end of the relation.
       //WARNING! This SHOULD BE FUNCTIONAL SO THERE SHOULD ONLY BE ONE ELEMENT removed!
       //TEST TEST
       //NOTE THE RELATIONS ON THE OBJECT RELATED ARE NOT REMOVED!
       try {
          if (relation.inverse()) {
             rep().remove((Resource) null, rel, id, graphs);
          } else {
             rep().remove(id, rel, null, graphs);
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
 
       wrapped = new RDFCollection<T>(this, newColl, ClassAnalysis.collectionType(type));
       Resource collId = (Resource) map(wrapped); //that should have been set in the construction phase above
       //add the relation from the object to the collection
       try {
          if (relation.inverse()) {
             rep().add(collId, rel, id, getWriteGraphs());
          } else {
             rep().add(id, rel, collId, getWriteGraphs());
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
 
       //what is the id? the id of coll or the id of the wrapper collection?
       //if someone sets a value, but then continues to use the reference of the non wrapped value, we have a problem...
       //need to add a relation for each element of coll
 
 
       return wrapped;
    }
 
    public <T> RDFCollection<T> createVirtualCollection(Object thiz,
            rdf relation,
            Collection<T> oldFieldValue,
            Type fieldType) {
       if (oldFieldValue instanceof RDFCollection) {
          log.warning("createVirtualCollection was called twice! This should not happen");
          return (RDFCollection<T>) oldFieldValue;
       }
       URI uri = vf.createURI(relation.value());
       //the collection is just a way to collect multiple relations from the object
       Resource id = (Resource) map(thiz);
       return new RDFCollection<T>(this, oldFieldValue, ClassAnalysis.collectionType(fieldType), thiz, uri, relation.inverse());
    }
 
    public <T> Collection<T> getCollectionField(Object thiz,
            rdf relation,
            Collection<T> oldFieldValue,
            Type fieldType) {
       Resource id = (Resource) map(thiz);
       RepositoryResult<Statement> si = null;
       try {
          URI uri = vf.createURI(relation.value());
          si = (relation.inverse()) ? rep().getStatements(null, uri, id, inference, graphs)
                  : rep().getStatements(id, uri, null, inference, graphs);
          if (si.hasNext()) {
             Value collObj = (relation.inverse()) ? si.next().getSubject() : si.next().getObject();
             si.close();
             si = null;
             return new RDFCollection<T>(this, (Resource) collObj, ClassAnalysis.collectionType(fieldType));
          } else {
             si.close();
             si = null;
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } finally {
          if (si != null) {
             try {
                si.close();
             } catch (RepositoryException e) {
                e.printStackTrace();
             }
          }
       }
       return oldFieldValue;
    }
 
    public Object setField(Class fieldClass, Object obj, rdf relation, Object value) {
 //        log.fine("setting field " + (relation.inverse() ? "-" : "") + " <" + relation.value() + "> for object " + obj +
 //                " with class " + fieldClass + ". Current value= " + value);
       URI uri;
       if (rdf.sameAs.equals(relation.value())) {
          if (value == null) {
             log.warning("not implemented. Need to do what?"); //todo
             return null;
          }
          if (!(value instanceof java.net.URL || value instanceof java.net.URI)) {
             //todo: should also be able to put a String there, in order to reduce cpu
             throw new Error("@rdf field of owl:sameAs relation can only take variables of type URI or URL ");
          }
          if (relation.range().length() != 0) {
             throw new Error("@rdf parameter with no value cannot be of type literal");
          }
          if (!obj2Resource.containsKey(obj)) {
             //todo: but what if the object allready has a name, and someone is giving it a new name?
             //then we just name this object
             java.net.URI name = (java.net.URI) value;
             name(obj, vf.createURI(name.toString()));
             return value;
          } //else if we have an infering database it will be able to infer the properties on the blank node
       }
       uri = vf.createURI(relation.value());
       Resource objId = (Resource) map(obj);
 
       try {
          if (relation.inverse()) {
             rep().remove((Resource) null, uri, objId, graphs);
          } else {
             rep().remove(objId, uri, null, graphs);
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       //what should we do here? Abort, throw an error?
       }
 
       //add new relation
       if (value != null) {
          try {
             if (relation.inverse()) {
                Value val = map(value);
                rep().add((Resource) val, uri, objId, getWriteGraphs());
             } else {
                Value val = map(value, relation.range());
                rep().add(objId, uri, val, getWriteGraphs());
             }
          } catch (RepositoryException e) {
             e.printStackTrace(); //todo: decide what exception to throw
          }
       }
       return value;
    /*
    if (fieldType instanceof ParameterizedType) {
    // list the raw type information
    ParameterizedType ptype = (ParameterizedType) fieldType;
    Type rtype = ptype.getRawType();
    log.fine("rawType is instance of " +
    rtype.getClass().getName());
    log.fine(" (" + rtype + ")");
    // list the actual type arguments
    Type[] targs = ptype.getActualTypeArguments();
    log.fine("actual type arguments are:");
    for (int j = 0; j < targs.length; j++) {
    log.fine(" instance of " +
    targs[j].getClass().getName() + ":");
    log.fine("  (" + targs[j] + ")");
    }
    } else {
    log.fine
    ("getGenericType is not a ParameterizedType!");
    }
    if (fieldClass.getTypeParameters().length > 0) {
    log.fine(fieldClass + " has " + fieldClass.getTypeParameters().length + " type parameters");
    for (TypeVariable tv : fieldClass.getTypeParameters()) {
    log.fine("t.name=" + tv.getName() + " t.gd=" + tv.getGenericDeclaration());
    }
    }
     */
    }
 
    public Object getField(Class fieldClass, Object sourceObj, rdf relation, Object fieldVal) {
 //        log.fine("getting field  " + (relation.inverse() ? "-" : "") + "<" + relation.value() + "> for object " + obj + " with class " + fieldClass + ". Current value= " + value);
       if (fieldVal == null) {
          RepositoryResult<Statement> stmtIt = null;
          try {
             if (rdf.sameAs.equals(relation.value())) {
                Value id = findKnownMappedValueFor(sourceObj);
                if (id instanceof org.openrdf.model.URI) {
                   org.openrdf.model.URI pubid = (org.openrdf.model.URI) id;
                   if (fieldClass.equals(java.net.URL.class)) {
                      return new java.net.URL(pubid.toString());
                   } else if (fieldClass.equals(java.net.URI.class)) {
                      return new java.net.URI(pubid.toString());
                   }
                } //search for other owl-same as names that might be URIs
                stmtIt = rep().getStatements((Resource) id, OWL.SAMEAS, null, inference, graphs);
                if (enableDuplicateFilter) stmtIt.enableDuplicateFilter();
                while (stmtIt.hasNext()) {
                   Value v = stmtIt.next().getObject();
                   if (v instanceof org.openrdf.model.URI) {
                      org.openrdf.model.URI uri = (org.openrdf.model.URI) v;
                      name(sourceObj, uri);
                      if (fieldClass.equals(URL.class)) {
                         return new java.net.URL(uri.toString());
                      } else if (fieldClass.equals(URI.class)) {
                         return new java.net.URI(uri.toString());
                      }
                   }
                }
                return null;
             }
             if (!relation.inverse()) {
                Resource id = (Resource) map(sourceObj);
                stmtIt = rep().getStatements(id, vf.createURI(relation.value()), null, inference, graphs);
             } else {
                Resource id = (Resource) map(sourceObj, relation.range());
                stmtIt = rep().getStatements(null, vf.createURI(relation.value()), id, inference, graphs);
             }
             if (stmtIt.hasNext()) {
                Statement first = stmtIt.next();
                Value object = (relation.inverse()) ? first.getSubject() : first.getObject();
                fieldVal = map(object, fieldClass);
                log.fine("returning field " + fieldVal);
             }
          } catch (MalformedURLException e) {
             e.printStackTrace(); //todo: decide what exception to throw
          } catch (URISyntaxException e) {
             e.printStackTrace(); //todo: decide what exception to throw
          } catch (RepositoryException e) {
             e.printStackTrace(); //todo: decide what exception to throw
          } finally {
             if (stmtIt != null) {
                try {
                   stmtIt.close();
                } catch (RepositoryException e) {
                   e.printStackTrace(); //todo: decide what exception to throw
                }
             }
          }
       }
       return fieldVal;
    }
 
    /**
     * remember the name of the object
     *
     * @param obj
     * @param id
     */
    public void name(Object obj, Resource id) {
       //we have to cast now, because most objects won't know they implement SommerMapable until after compilation
       if (obj == null) {
          return;
       }
       if (!(obj instanceof Class)) { //though we will want to do something like this even for classes!
          SommerMapable mapable = (SommerMapable) obj;
          mapable.setSommerRewriteMapper(this);
       }
       obj2Resource.put(obj, id);
       List<Object> names = resource2Obj.get(id);
       if (names == null) {
          names = new LinkedList();
          resource2Obj.put(id, names);
       }
       if (!names.contains(obj)) {
          names.add(obj);
       }
    }
 
    //
    //
    // Implementations of RewriteMapper
    //
    //
    public void cifpName(Object thiz, Class clazz, Class[] argTypes, Object[] values) {
       assert (argTypes.length == values.length);
       log.fine("in cifpName: looking for a name for " + thiz + " of type " +
               clazz);
       Resource existingName = obj2Resource.get(thiz);
       if (existingName != null) {
          log.fine("cifpName: " + thiz + " was allready mapped to " +
                  existingName);
          return;
       }
       Resource name = getCIFPValue(clazz, argTypes, values);
       if (name == null) {
          return;
       }
       name(thiz, name);
       log.fine("cifpName: mapped " + name.toString() + "=" + thiz);
    }
 
    public <T> T cifpObject(Class<T> clazz, Class[] argTypes, Object[] values) {
       assert (argTypes.length == values.length);
       log.fine("in cifpObject: looking for an object that satisfies the cifp of " +
               clazz);
       Resource name = getCIFPValue(clazz, argTypes, values);
       if (name == null) {
          log.fine("cifpObject: none found.");
          return null;
       }
       T result = resourceGet(name, clazz);
       log.fine("cifpObject: found " + result + " that was mapped to " + name);
       return result;
    }
 
    /**
     * Find the object, if one exists that has the relations given by the rdf annotations
     * on the arguments of the constructor clazz, to the values.
     *
     * @param clazz    the class of the constructor
     * @param argTypes the argument types identifying the particular constructor
     * @param values   the values that may have allready have a mapping in the rdf graph
     * @return an rdf resource that has the required realtions to the mapped values, or null
     */
    private Resource getCIFPValue(Class clazz, Class[] argTypes, Object[] values) {
       assert (argTypes.length == values.length);
       Constructor cons;
       try {
          cons = clazz.getConstructor(argTypes);
       } catch (NoSuchMethodException e) {
          e.printStackTrace(); //todo: decide what exception to throw
          return null;
       }
       rdf[] rdfs = ClassAnalysis.filterRdfAnnotations(cons.getParameterAnnotations());
 
       HashSet<Value> result = null;
 
       for (int i = 0; i < rdfs.length; i++) {
          URI rel = vf.createURI(rdfs[i].value()); //todo check for inverses
          Value value = map(values[i]);
          if (value == null) {
             return null; //one of the objects is not yet mapped.
          }
          //todo: could there be some complex situation where an object does not yet have a value but should?
          RepositoryResult<Statement> stats = null;
          HashSet objects = new HashSet();
          try {
             stats = rep().getStatements(null, rel, value, inference, graphs);
             while (stats.hasNext()) {
                log.fine("in cifpValue: found 1 object");
                objects.add(stats.next().getSubject());
             }
          } catch (RepositoryException e) {
             e.printStackTrace(); //todo: decide what exception to throw
          } finally {
             if (stats != null) {
                try {
                   stats.close();
                } catch (RepositoryException e) {
                   e.printStackTrace(); //todo: decide what exception to throw
                }
             }
          }
          if (i == 0) {
             result = objects;
          } else {
             result.retainAll(objects);
          }
          if (result.isEmpty()) {
             log.fine("cifpValue: returning null");
             return null;
          }
       }
       return (Resource) result.iterator().next();
    }
    HashMap<Class, JavaInstanceMapper> literalMap = new HashMap<Class, JavaInstanceMapper>();
 
    void addJavaInstance(JavaInstanceMapper map) {
       literalMap.put(map.getObjClass(), map);
    }
 
    /* This is general enough that it can be in a superclass, as it is no longer Sesame dependent */
    private void initInstanceMapper() {
       addJavaInstance(new JavaInstanceMapper(this, String.class) {
 
          Object java2rdf(Object string) {
             return getFactory().createLiteral((String) string);
          }
 
          Object rdf2java(Object rdfObj) {
             if (rdfObj instanceof Literal)
                return ((Literal) rdfObj).getLabel();
             else return null;
          }
       });
       addJavaInstance(new JavaInstanceMapper(this, Date.class) {
 
          Object java2rdf(Object date) {
             return getFactory().createLiteralType(dateToXsdString((Date) date), XMLSchema.DATETIME.toString());
          }
 
          Object rdf2java(Object rdfObj) {
             Literal lit = (Literal) rdfObj;
             assert XMLSchema.DATETIME.equals(lit.getDatatype()) : lit +
                     " is not a datetime literal";
             Date sol = xmldf.newXMLGregorianCalendar(lit.getLabel()).toGregorianCalendar().getTime();
             log.fine("GREGORIAN sol=" + sol);
             return sol;
          }
       });
       addJavaInstance(new JavaInstanceMapper(this, java.net.URL.class) {
 
          Object java2rdf(Object url) {
             return getFactory().createLiteral(((URL) url).toExternalForm());
          }
 
          Object rdf2java(Object rdfObj) {
             String uriStr;
             if (rdfObj instanceof Literal) {
                Literal lit = (Literal) rdfObj;
                assert XMLSchema.ANYURI.equals(lit.getDatatype());
                uriStr = lit.getLabel();
             } else if (rdfObj instanceof org.openrdf.model.URI) {
                org.openrdf.model.URI uri = (URI) rdfObj;
                uriStr = uri.toString();
             } else {
                return null;
             }
             try {
                return new java.net.URL(uriStr);
             } catch (MalformedURLException e) {
                log.log(Level.SEVERE, "Literal is not a parseable URI", e);
                return null; //todo:
             }
          }
       });
       addJavaInstance(new JavaInstanceMapper(this, java.net.URI.class) {
 
          Object java2rdf(Object uri) {
             return getFactory().createResource(uri.toString());
          }
 
          Object rdf2java(Object rdfObj) {
             String uriStr;
             if (rdfObj instanceof Literal) {
                Literal lit = (Literal) rdfObj;
                assert XMLSchema.ANYURI.equals(lit.getDatatype());
                uriStr = lit.getLabel();
             } else if (rdfObj instanceof org.openrdf.model.URI) {
                org.openrdf.model.URI uri = (URI) rdfObj;
                uriStr = uri.toString();
             } else {
                return null;
             }
             try {
                return new java.net.URI(uriStr);
             } catch (URISyntaxException e) {
                log.log(Level.SEVERE, "Literal is not a parseable URI", e);
                return null;
             }
          }
       });
       addJavaInstance(new JavaInstanceMapper(this, Integer.class) {
 
          Object java2rdf(Object integer) {
             return getFactory().createLiteralType(((Integer) integer).toString(), XMLSchema.INT.toString());
          }
 
          Object rdf2java(Object rdfObj) {
             Literal lit = (Literal) rdfObj;
             assert XMLSchema.INT.equals(lit.getDatatype());
             return Integer.getInteger(lit.getLabel());
          }
       });
 
    //
    //todo: write a bunch more for all the other literal classes
    //
    }
 
    /**
     * Just find the Value of the object if it is known or a literal
     *
     * @param obj
     * @return
     */
    public Value findKnownMappedValueFor(Object obj) {
       if (obj == null) {
          return null;
       }
       Value resultId = obj2Resource.get(obj);
       if (resultId != null) {
          return resultId;
       }
       JavaInstanceMapper instcMpr = literalMap.get(obj.getClass());
       if (instcMpr != null) {
          return (Value) instcMpr.java2rdf(obj);
       }
       return resultId;
    }
 
    /**
     * Find a known mapped value of the object, whith the given type
     *
     * @param obj         the object we are looking a mapping for
     * @param literalType the type of the literal
     * @return
     */
    Value findKnownMappedValueFor(Object obj, String literalType) {
       if (literalType.length() == 0) {
          return findKnownMappedValueFor(obj);
       }
       if ((obj instanceof java.net.URI || obj instanceof java.net.URL) &&
               literalType.equals(rdf.xsd + "anyURI")) {
          return vf.createLiteral(obj.toString(), vf.createURI(literalType));
       }
       return findKnownMappedValueFor(obj);
    }
 
    public static String dateToXsdString(Date date) {
       GregorianCalendar calendar = new GregorianCalendar();
       //todo: warning: due to a bug in Sesmae 2 alpha 3 we have to set the time zone to TZ and remove millisecond precision
       //todo: see http://www.openrdf.org/forum/mvnforum/viewthread?thread=956
       calendar.setTimeZone(TZ);
       calendar.setTime(date);
       XMLGregorianCalendar xmlGregorianCalendar = xmldf.newXMLGregorianCalendar(calendar);
       xmlGregorianCalendar.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
       return xmlGregorianCalendar.toXMLFormat();
    }
 
    /**
     * Map the java object to a RDF Value.
     *
     * @param obj
     * @param literalType the type the object should be mapped to, or "" if not a literal
     * @return
     */
    public Value map(Object obj, String literalType) {
       if (literalType.length() == 0) {
          return map(obj);
       }
       if ((obj instanceof java.net.URI || obj instanceof java.net.URL) &&
               literalType.equals(rdf.xsd + "anyURI")) {
          return vf.createLiteral(obj.toString(), vf.createURI(literalType));
       }
       return map(obj);
    }
 
    /**
     * Tries to find the Identity of an object by looking for identiy or inverse functional
     * properties.
     */
    class IdFinderSerialiser extends RdfSerialiser {
 
       Resource resultId;
 
       public Resource getId() {
          return resultId;
       }
 
       @Override
       public boolean continueProcessing() {
          return resultId == null;
       }     
       
       public boolean isInterestingField(Field fld) {
          if (resultId != null) {//stop at first answer
             return false;
          }
          if (fld.getAnnotation(rdf.class) == null) {
             return false;
          }
          if (Modifier.isStatic(fld.getModifiers())) {
             return false;
          }
          return true;
       }
 
       public Object processField(Object sourceObj, Field f, Object value) {
          if (value == null) {
             return null;
          }
          rdf rdfann = f.getAnnotation(rdf.class);
          if ((f.getType().equals(java.net.URL.class) ||
                  f.getType().equals(java.net.URI.class)) &&
                  rdf.sameAs.equals(rdfann.value())) {
             if (value != null) {
                resultId = vf.createURI(value.toString());
                return null; //and we're done
             }
          } else if (f.getAnnotation(inverseFunctional.class) != null) {
             //todo: what if it is a functional relation on an inverse relation?
             if (Collection.class.isAssignableFrom(f.getType())) {
                if (f.getAnnotation(functional.class) != null) {
                   //todo: don't know what to do return;
                   return null;
                } else {
                   URI rel = vf.createURI(rdfann.value());
                   Collection coll = (Collection) value;
                   if (coll != null) {
                      for (Object o : coll) {
                         Value ido = findKnownMappedValueFor(o);
                         RepositoryResult<Statement> clit = null;
                         if (null != ido) {
                            try {
                               clit = (rdfann.inverse()) ? rep().getStatements((Resource) ido, rel, null, inference, graphs) : rep().getStatements(null, rel, ido, inference, graphs);
                               if (clit.hasNext()) {
                                  Statement st = clit.next();
                                  resultId = (Resource) ((rdfann.inverse()) ? st.getObject() : st.getSubject());
                                  return null; //and we're done
                               }
                            } catch (RepositoryException e) {
                               e.printStackTrace(); //todo: decide what exception to throw
                            } finally {
                               if (clit != null) {
                                  try {
                                     clit.close();
                                  } catch (RepositoryException e) {
                                     e.printStackTrace(); //todo: decide what exception to throw
                                  }
                               }
                            }
                         }
                      }
                   }
                }
             } else {
                //just an ifp on an object (a functional and inverse functional relation
                URI rel = vf.createURI(rdfann.value());
                RepositoryResult<Statement> clit = null;
                try {
                   Value ido = findKnownMappedValueFor(value, rdfann.range());
                   if (null != ido) {
                      //this object has been mapped or is a literal
                      clit = (rdfann.inverse()) ? rep().getStatements((Resource) ido, rel, null, inference, graphs) : rep().getStatements(null, rel, ido, inference, graphs);
                      if (clit.hasNext()) {
                         Statement st = clit.next();
                         resultId = (Resource) ((rdfann.inverse()) ? st.getSubject() : st.getObject());
                         return null; //and we're done
                      }
                   }
                } catch (RepositoryException e) {
                   e.printStackTrace(); //todo: decide what exception to throw
                } finally {
                   if (clit != null) {
                      try {
                         clit.close();
                      } catch (RepositoryException e) {
                         e.printStackTrace(); //todo: decide what exception to throw
                      }
                   }
                }
             }
          }
          return null;
       }
    }
 
    /**
     * take an unmapped object and add all its relations to the graph
     */
    class MappingSerialiser extends RdfSerialiser {
 
       Object thiz;
       Resource name;
       HashMap<Field, Object> fieldInfo = new HashMap<Field, Object>();
 
       public MappingSerialiser(Object thiz, Resource name) {
          this.thiz = thiz;
          this.name = name;
       }
 
       public boolean isInterestingField(Field fld) {
          return fld.getAnnotation(rdf.class) != null;
       }
 
       public Object processField(Object sourceObj, Field f, Object val) {
          if (val != null) {
             fieldInfo.put(f, val);
          }
          return null;
       }
 
       /**
        * This second Iteration will go through the fields and add the relations to the database
        */
       class SecondIteration extends RdfSerialiser {
 
          public boolean isInterestingField(Field fld) {
             rdf ann = fld.getAnnotation(rdf.class);
             if (ann == null) {
                return false;
             }
             if (rdf.sameAs.equals(ann.value())) {
                return false; //because we have already used this information in a previous pass
             }
             return true;
          }
 
          public Object processField(Object sourceObj, Field fld, Object value) {
             rdf ann = fld.getAnnotation(rdf.class);
             Value objValue = null;
             if (Collection.class.equals(fld.getType())) {
                if (fld.getAnnotation(functional.class) == null) {
                   return createVirtualCollection(sourceObj, ann, (Collection) value, fld.getGenericType());
                } else {
                   return getCollectionField(sourceObj, ann, (Collection) value, fld.getGenericType());
                }
             } else {
                Object fldVal = fieldInfo.get(fld);
                if (fldVal == null) {
                   return null;
                }
                objValue = map(fldVal, ann.range());
             }
             try {
                if (ann.inverse()) {
                   rep().add((Resource) objValue, vf.createURI(ann.value()), name, getWriteGraphs());
                } else {
                   rep().add(name, vf.createURI(ann.value()), objValue, getWriteGraphs());
                }
             } catch (RepositoryException e) {
                log.log(Level.SEVERE, "could not add relation to repository", e);
             }
             return null;
          }
       }
 
        void mapTheFields() {
          name(thiz, name);
 
          //add type field Info
          String uriStr = getClassURI(thiz.getClass()); //todo: what about multiple annotations from superclasses
          URI type = vf.createURI(uriStr);
          try {
             rep().add(name, RDF.TYPE, type, getWriteGraphs());
          } catch (RepositoryException e) {
             e.printStackTrace(); //todo: decide what exception to throw
          }
 
          SecondIteration it = new SecondIteration();
          ((RdfSerialisable) thiz).rdfSerialise(it);
       }
    }
 
    /**
     * Map the java object to a RDF Value.
     *
     * @param obj
     * @return
     */
    public Value map(Object obj) {
       if (obj == null) {
          return null;
       }
       if (obj instanceof Class) { //currently I don't add this to the mapper, I probably should...
          Class c = (Class) obj;         
          URI classUri = vf.createURI(getClassURI(c));
          name(obj, classUri);
          return classUri;
       }
       Value resultId = findKnownMappedValueFor(obj);
       if (resultId != null) {
          return resultId;
       }
       if (obj instanceof RdfSerialisable) {
          IdFinderSerialiser idFinder = new IdFinderSerialiser();
          ((RdfSerialisable) obj).rdfSerialise(idFinder);
          resultId = idFinder.getId();
       }
 
       if (resultId == null) {
          for (Constructor c : obj.getClass().getConstructors()) {
             resultId = findFromCifpConstructor(c, obj);
             if (resultId != null) {
                break;
             }
          }
       }
       if (resultId == null) {
          resultId = vf.createBNode();
       }
       if (obj instanceof RdfSerialisable) {
          MappingSerialiser mapser = new MappingSerialiser(obj, (Resource) resultId);
          RdfSerialisable s = (RdfSerialisable) obj;
          s.rdfSerialise(mapser);
          mapser.mapTheFields();
       }
 
       return resultId;
    }
 
    /**
     * filter for fields that have given annotations
     * Note: this is used by findFromCifpConstructor (perhaps the class should be inernal to the method)
     * also: this should be more subtle: if fields are private and in a superclass, then should we find a solution?
     */
    class FilterFieldsSerialiser extends RdfSerialiser {
 
       rdf[] filterAnn;
       HashMap<rdf, Value> result = new HashMap<rdf, Value>();
       int resnum = 0;
 
       public FilterFieldsSerialiser(rdf[] annotations) {
          this.filterAnn = annotations;
       }
 
       public boolean isInterestingField(Field fld) {
          rdf fan = fld.getAnnotation(rdf.class);
          if (fan == null) {
             return false;
          }
          for (rdf ann : filterAnn) {
             if (result.get(ann) != null) {
                return false; //we already have an obj for this relation todo: don't iterate over it
             }
             if (fan.value().equals(ann.value()) && fan.inverse() ==
                     ann.inverse()) {
                return true;
             }
          }
          return false;
       }
 
       public Object processField(Object sourceObj, Field f, Object value) {
          if (value == null) {
             return null;
          }
          rdf ann = f.getAnnotation(rdf.class);
          Value rdfval = findKnownMappedValueFor(value, ann.range());
          if (rdfval == null) {
             return null;
          }
          result.put(ann, rdfval);
          resnum++;
          return null;
       }
 
       boolean solved() {
          return resnum == filterAnn.length;
       }
 
       HashMap<rdf, Value> solution() {
          return result;
       }
    }
 
    private Resource findFromCifpConstructor(Constructor con, Object obj) {
       if (con.getAnnotation(inverseFunctional.class) == null) {
          return null;
       }
       //if all the parameters have @rdf relations, we are ok
       if (!(obj instanceof RdfSerialisable)) {
          return null;
       }
       rdf[] rdfs = ClassAnalysis.filterRdfAnnotations(con.getParameterAnnotations());
       if (rdfs == null) {
          return null; //this constructor is not of the right form
       }
       FilterFieldsSerialiser ser = new FilterFieldsSerialiser(rdfs);
       ((RdfSerialisable) obj).rdfSerialise(ser);
 
       if (!ser.solved()) {
          return null;
       }
       HashMap<rdf, Value> result = ser.solution();
 
       //now construct the query
       StringBuilder query = new StringBuilder();
       query.append("SELECT O FROM CONTEXT ");
       for (int i = 0; i < graphs.length; i++) {
          query.append('<').append(graphs[i].toString()).append('>');
          if (i + 1 < graphs.length) {
             query.append(',');
          }
       }
       query.append(' ');
       boolean first = true;
       for (Entry<rdf, Value> e : result.entrySet()) {
          rdf ann = e.getKey();
          Value value = e.getValue();
          if (!first) {
             query.append(", ");
          }
          if (ann.inverse()) {
             query.append("{<").append(value).append(">} <").append(ann.value()).append("> {O}");
          } else {
             query.append("{O} <").append(ann.value()).append("> {");
             if (value instanceof Literal) {
                Literal lit = (Literal) value;
                query.append("\"").append(lit.getLabel()).append("\"");
                if (lit.getDatatype() != null) {
                   query.append("^^<" + lit.getDatatype() + ">");
                } else if (lit.getLanguage() != null &&
                        (!"".equals(lit.getLanguage()))) {
                   query.append("@" + lit.getLanguage());
                }
             } else {
                query.append("<").append(value).append(">");
             }
             query.append("}");
          }
          first = false;
       }
       log.fine("query=" + query.toString());
       System.out.println("query" + query.toString());
       //RepositoryResult<List<Value>> values = null;
       TupleQueryResult values = null;
       try {
          TupleQuery tq = rep().prepareTupleQuery(QueryLanguage.SERQL, query.toString());
          values = tq.evaluate();
          if (values.hasNext()) {
             return (Resource) values.next().getValue("O");
          }
       } catch (MalformedQueryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (QueryEvaluationException e) {
          e.printStackTrace(); //todo: decide what exception to throw
 
       } finally {
          if (values != null) {
             try {
                values.close();
             } catch (QueryEvaluationException e) {
                e.printStackTrace(); //todo: decide what exception to throw
 
             }
          }
       }
 
       return null;
    }
 
    /**
     * Smush this graph.
     * Need this because don't have an inferencing engine
     */
    public void smush() {
       //this is replaced by inferencing code now...
       throw new Error("to be removed");
    }
 
    /**
     * map the resource into an object of the given class
     *
     * @param value
     * @param sprClzz the class or super class of any object returned
     * @return the object, or null if no object could be constructed
     */
    private <T> T map(Value value, Class<T> sprClzz) {
       JavaInstanceMapper instcMap = literalMap.get(sprClzz);
       if (instcMap != null) {
          return (T) instcMap.rdf2java(value);
       }
       //at this point the value should be a resource
       if (!(value instanceof Resource)) {
          return null;
       }
       Resource id = (Resource) value;
       T res = resourceGet(id, sprClzz);
 
       if (res != null) {
          return (T) res;
       }
       Class clazz = mostSpecificSubClass(sprClzz, getTypesOf(id));
      if (clazz == null)
    	  return null;
 
       try {
          try {
             Constructor<T> emptyConstructor = clazz.getConstructor();
             res = emptyConstructor.newInstance();
          } catch (Exception e) {
             //we don't have an empty constructor
             //find all the @inverseFunctional constructors and try to create an object using them
             Constructor[] cons = clazz.getConstructors();
             constructors:
             for (Constructor con : cons) {
                try {
                   if (id instanceof URI) {
                      //just check to see if the constructor is tagged @rdf and has one argument only a URI or URL
                      rdf ann = (rdf) con.getAnnotation(rdf.class);
                      if (ann != null && rdf.sameAs.equals(ann.value()) &&
                              con.getParameterTypes().length == 1) {
                         if (con.getParameterTypes()[0].equals(URL.class)) {
                            try {
                               res = (T) con.newInstance(new URL(value.toString()));
                               break constructors;
                            } catch (MalformedURLException eurl) {
                            }
                         } else if (con.getParameterTypes()[0].equals(URI.class)) {
                            try {
                               res = (T) con.newInstance(new java.net.URI(value.toString()));
                               break constructors;
                            } catch (URISyntaxException eurl) {
                               log.severe("Sesame thought we had a URI that java could not parse: " +
                                       value);
                            }
                         }
                      }
                   } else {
                      //if all the parameters have @rdf relations, we are ok
                      rdf[] rdfs = ClassAnalysis.filterRdfAnnotations(con.getParameterAnnotations());
                      if (rdfs == null) {
                         continue constructors;
                      }
                      Value[] ids = new Value[rdfs.length];
                      for (int i = 0; i < ids.length; i++) {
                         RepositoryResult<Statement> clIt = null;
 
                         try {
                            if (rdfs[i].inverse()) {
                               clIt = rep().getStatements(null, vf.createURI(rdfs[i].value()), id, inference, graphs);
                            } else {
                               clIt = rep().getStatements(id, vf.createURI(rdfs[i].value()), null, inference, graphs);
                            }
                            if (clIt.hasNext()) {
                               Statement rel = clIt.next();
                               ids[i] = (rdfs[i].inverse()) ? rel.getSubject() : rel.getObject();
                            } else {
                               continue constructors;
                            }
                         } catch (RepositoryException e1) {
                            e1.printStackTrace(); //todo: decide what exception to throw
                         } finally {
                            if (clIt != null) {
                               try {
                                  clIt.close();
                               } catch (RepositoryException e1) {
                                  e1.printStackTrace(); //todo: decide what exception to throw
 
                               }
                            }
                         }
                      }
                      //make the objects from the ids
                      Object[] params = new Object[rdfs.length];
                      for (int i = 0; i < ids.length; i++) {
                         params[i] = map(ids[i], con.getParameterTypes()[i]);
                         if (params[i] == null) {
                            continue constructors;
                         }
                      }
                      res = (T) con.newInstance(params); //todo: try another constructor if exception thrown?
 
                      if (res != null) {
                         break;
                      }
                   }
                } catch (IllegalAccessException e1) {
                   log.log(FINE, "Could not use constructor ", e1);
                }
             }
          }
          name(res, id);
       } catch (InstantiationException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (InvocationTargetException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
       return (T) res;
    }
 
    /**
     * Get all the known types of resources res.
     *
     * @param res
     * @return the known types as a list of URI types
     */
    private ArrayList<String> getTypesOf(Resource res) {
       //first we find what the rdf type(s) of the subject subj are
       ArrayList<String> rdfClasses = new ArrayList();
       RepositoryResult<Statement> si = null;
       try {
          si = rep.getStatements((Resource) res, RDF.TYPE, null, inference, graphs);
          while (si.hasNext()) {
             Statement s = si.next();
             rdfClasses.add(s.getObject().toString());
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
 
       } finally {
          if (si != null) {
             try {
                si.close();
             } catch (RepositoryException e) {
                e.printStackTrace();
             }
          }
       }
       return rdfClasses;
    }
 
    /**
     * look in our map for an object with rdf name and the most precise subclass
     *
     * @param name  the rdf
     * @param clazz the supertype of the object to be returned
     * @return the object in our map, if one exists
     */
    <T> T resourceGet(Resource name, Class<T> clazz) {
       T result = null;
       List<Object> list = resource2Obj.get(name);
       if (list == null) {
          return null;
       }
       for (Object o : list) {
          if (clazz.isInstance(o)) {
             if (result != null &&
                     o.getClass().isAssignableFrom(result.getClass())) {
                continue;
             } else {
                result = (T) o;
             }
          }
       }
       return result;
    }
 
    public boolean removeRelation(Resource source, URI relation, boolean inverse, Object obj) {
       try {
          Value id = map(obj);
          if (id != null) {
             if (inverse) {
                rep().remove(vf.createStatement((Resource) id, relation, source), graphs);
             } else {
                rep().remove(vf.createStatement(source, relation, id), graphs);
             }
             return true;
          } else {
             log.warning("warning: null object to delete");
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
       return false;
    }
 
    /**
     * @param source
     * @param relation
     * @param obj
     */
    public boolean addRelation(Resource source, URI relation, boolean inverse, Object obj) {
       Value id = map(obj);
       if (id != null) {
          try {
             if (inverse) {
                rep().add((Resource) id, relation, source, getWriteGraphs());
             } else {
                rep().add(source, relation, id, getWriteGraphs());
             }
          } catch (RepositoryException e) {
             //nasty things can happen here if the obj is a String for example and inverse is true
             e.printStackTrace(); //todo: decide what exception to throw
 
             return false;
          }
          return true;
       } else {
          log.warning("warning: null object to add");
          return true;
       }
    }
 
    public void removeAll(Resource source, URI relation, boolean inverse) {
 //      ArrayList<Statement> buggy = new ArrayList<Statement>();
       try {
          //todo: deal with inferencing!
          if (inverse) {
             rep().remove((Resource) null, relation, source, graphs);
          } else {
             rep().remove(source, relation, null, graphs);
          //need to copy everything to a collection first, due to bug in Sesame alpha 3
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
 
       }
    }
 
    /**
     * Get the collection starting with subj and the given relation, with contents of type clazz
     *
     * @param subj        the name of the object from which to go
     * @param relationUri the relation from subj
     * @param inverse     if the inverse of the relationURi is desired
     * @param clazz       the class to return (could be a subclass if this is warranted by the database)
     * @return a collection of java objects that are related by the relationUri relation to the collection
     */
    public <E> ArrayList<E> getCollection(Value subj, URI relationUri, boolean inverse, Class<E> clazz) {
       RepositoryResult<Statement> si = null;
       ArrayList result = new ArrayList<E>();
 
       try {
          si = (inverse) ? rep().getStatements(null, relationUri, subj, inference, graphs) : rep().getStatements((Resource) subj, relationUri, null, inference, graphs);
          if (enableDuplicateFilter) si.enableDuplicateFilter();
          while (si.hasNext()) {
             Statement s = si.next();
             Value res = (inverse) ? s.getSubject() : s.getObject();
             E mappedObj = map(res, clazz);
             if (mappedObj != null) {
                result.add(mappedObj);
             }
          }
          //it would be better if the database returned this in its answer set
          if (relationUri.toString().equals(rdf.sameAs)) {
             E map = map(subj, clazz);
             if (map != null) {
                result.add(map);
             }
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } finally {
          if (si != null) {
             try {
                si.close();
             } catch (RepositoryException e) {
                e.printStackTrace();
             }
          }
       }
       return result;
    }
 
    public boolean isMapped(Object o) {
       return obj2Resource.containsKey(o);
    }
 
    public <T> T getObjectById(String uri, Class<T> clazz) {
       URI id = vf.createURI(uri);
       try {
          if (rep().hasStatement(id, null, null, inference, graphs) ||
                  rep().hasStatement(null, null, id, inference, graphs)) {
             return map(id, clazz);
          }
       } catch (RepositoryException ex) {
          Logger.getLogger(SesameMapper.class.getName()).log(Level.SEVERE, null, ex);
       }
       return null;
    }
 
    public <T> T createObjectWithId(String uri, Class<T> clazz) {
       URI id = vf.createURI(uri);
       return map(id, clazz);
    }
 
    String getClassURI(Class clazz) {
       rdf ann = (rdf) clazz.getAnnotation(rdf.class);
       if (ann == null || "".equals(ann.value())) {
          return "urn:java:" + clazz.getName();
       } else {
          return ann.value();
       }
    }
 
    public <T> Collection<T> getAllObjectsOfType(Class<T> clazz) {
       URI classType = vf.createURI(getClassURI(clazz));
 
       ArrayList<Resource> resids = new ArrayList<Resource>();
       RepositoryResult<Statement> iter = null;
       try {
          iter = rep().getStatements(null, RDF.TYPE, classType, inference, graphs);
          if (enableDuplicateFilter) iter.enableDuplicateFilter();
          while (iter.hasNext()) {
             Resource res = iter.next().getSubject();
             if (res != null) {
                resids.add(res);
             }
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } finally {
          if (iter != null) {
             try {
                iter.close();
             } catch (RepositoryException e) {
                e.printStackTrace(); //todo: decide what exception to throw
             }
          }
       }
 
       ArrayList<T> result = new ArrayList<T>();
       for (Resource id : resids) {
          result.add(map(id, clazz)); //todo: first remove all sources that have a sameas relation
       }
       return result;
    }
 
    public <T> T addObject(T object) {
       map(object);
       return object;
    }
 
    public boolean addObjects(Object... objects) {
       for (Object o : objects) {
          addObject(o);
       }
       return true;
    }
 
    public void addStatements(Collection<net.java.rdf.sommer.util.Statement> statements) {
       for (net.java.rdf.sommer.util.Statement s : statements) {
          addStatement(s);
       }
       setChanged();
    }
 
    private void addStatement(net.java.rdf.sommer.util.Statement s) {
       if (s == null) {
          log.warning("addStatment(null)!");
          return; //todo: replace with assertion
       } else {
          log.info("now trying to add " + s);
       }
       if (!s.isComplete()) {
          log.info("tried to add an incomplete statement: " + s);
          return;
       }
       Object sub = s.getSubject();
       Resource subRes;
       URI relation;
       Value objVal;
 
       if (sub instanceof SommerMapable) {
          subRes = (Resource) map(sub);
          if (subRes == null) {
             log.info("cannot add a statement with a subject that is not yet mapped " +
                     s);
             return;
          }
       } else {
          //it could also be a Literal, what should one do then?
          log.info("Don't know how to handle a non mappable subject (yet) in " +
                  s);
          return;
       }
 
       java.net.URI uri = s.getRelation();
       relation = vf.createURI(uri.toString());
 
       Object obj = s.getObject();
       objVal = map(obj);
       if (objVal == null) {
          log.info("the object is not mapped " + s);
          return;
       }
 
       //now we have subject relation object
       try {
          rep().add(subRes, relation, objVal, writeGrphs);
          log.info("statement added " + s);
       } catch (RepositoryException e) {
          log.severe("could not add relation " + s);
       }
    }
 
    public void removeStatements(Collection<net.java.rdf.sommer.util.Statement> statements) {
       for (net.java.rdf.sommer.util.Statement s : statements) {
          removeStatement(s);
       }
       setChanged();
    }
 
    private void removeStatement(net.java.rdf.sommer.util.Statement s) {
       if (s == null) {
          log.warning("removeStatment(null)!");
          return; //todo: turn into an assertion
       } else {
          log.info("removing " + s);
       }
 
       if (!s.isComplete()) {
          return;
       }
       Object sub = s.getSubject();
       Resource subRes;
       URI relation;
       Value objVal;
 
       if (sub instanceof SommerMapable) {
          subRes = (Resource) map(sub);
          if (subRes == null) {
             log.info("cannot remove a statement with a subject that is not yet mapped " +
                     s);
             return;
          }
       } else {
          //it could also be a Literal, what should one do then?
          log.info("Don't know how to handle a non mappable subject (yet) in " +
                  s);
          return;
       }
 
       java.net.URI uri = s.getRelation();
       relation = vf.createURI(uri.toString());
 
       Object obj = s.getObject();
       objVal = map(obj);
       if (objVal == null) {
          log.info("the object is not mapped " + s);
          return;
       }
 
       //now we have subject relation object
       try {
          //todo: removing relations from write only graphs should create diff graphs.
          //todo: the behavior should be pluggable probably
          rep().remove(subRes, relation, objVal, graphs);
       } catch (RepositoryException e) {
          log.severe("could not add relation " + s);
       }
 
    }
 
    public boolean remove(Object obj) {
       Resource id = obj2Resource.get(obj);
       if (id == null) {
          return false;
       }
 
       try {
          rep().remove(id, null, null, graphs);
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
       try {
          rep().remove((Resource) null, (URI) null, id, graphs);
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
       unmap(obj);
       return true;
    }
 
    public boolean unmap(Object obj) {
       Resource resource = obj2Resource.remove(obj);
       if (resource == null) {
          return false;
       }
       List<Object> objList = resource2Obj.get(resource);
       objList.remove(obj);
       assert !objList.contains(obj) : "More than one identical object in list, memory leak!";
       if (objList.isEmpty()) {
          resource2Obj.remove(resource);
       }
       SommerMapable smbl = (SommerMapable) obj;
       smbl.setSommerRewriteMapper(null);
       return true;
    }
 
    public void output(OutputStream out) {
       log.fine("Outputting graph");
       try {
          rep().export(new N3Writer(out), graphs);
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (RDFHandlerException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
    }
 
    public void output(Writer out) {
       log.fine("Outputting graph");
       try {
          rep().export(new N3Writer(out), graphs);
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (RDFHandlerException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
    }
 
    public String graphId() {
       return graphs.toString();
    }
 
    public boolean equals(Object mapped1, Object mapped2) {
       if (mapped1 == null || mapped2 == null) {
          return false;
       }
       Resource res1 = obj2Resource.get(mapped1);
       Resource res2 = obj2Resource.get(mapped2);
       if (res1 == null || res2 == null) {
          return false;
       }
       if (res1.equals(res2)) {
          return true;
       }
       //inferencing graphs would need only 1 query.
       try {
          if (rep().hasStatement(res1, OWL.SAMEAS, res2, inference, graphs) ||
                  rep().hasStatement(res1, OWL.SAMEAS, res2, inference, graphs)) {
             return true;
          }
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       }
       //we could go into further checks for non inferencing graphs, but that could be a little heavy
       return false;
    }
 
    public class QueryByExampleSerialiser extends RdfSerialiser {
 
       StringBuilder query = new StringBuilder("SELECT Obj0 FROM CONTEXT ");
       boolean first = true;
       int counter = 0;
       HashMap<Object, Integer> objToNum = new HashMap<Object, Integer>();
 
       public QueryByExampleSerialiser() {
          for (int i = 0; i < graphs.length; i++) {
             query.append('<').append(graphs[i].toString()).append('>');
             if (i + 1 < graphs.length) {
                query.append(',');
             }
          }
          query.append(' ');
       }
 
       String getIdForObject(Object o) {
          if (objToNum.containsKey(o)) {
             return "Obj" + objToNum.get(o);
          }
          objToNum.put(o, counter);
          return "Obj" + counter++;
       }
 
       @Override
       public String toString() {
          return query.toString();
       }
 
       public boolean isInterestingField(Field fld) {
          return fld.getAnnotation(rdf.class) != null;
       }
 
       public Object processField(Object sourceObj, Field fld, Object res) {
          if (res == null) {
             return null;
          }
          if (!first) {
             query.append(", ");
          } else {
             first = false;
          }
          rdf ann = fld.getAnnotation(rdf.class); //should never be null since we did the check above
 
          if (Collection.class.isAssignableFrom(fld.getType())) {
             if (fld.getAnnotation(functional.class) != null) {
                //need to work on this
             } else {
                for (Object i : (Collection) res) {
                   Object digdeeper = appendRelation(sourceObj, ann, i);
                   digdeeper(digdeeper);
                }
             }
          } else {
             Object digdeeper = appendRelation(sourceObj, ann, res);
             digdeeper(digdeeper);
          }
          return null;
       }
 
       private Object appendRelation(Object sourceObj, rdf ann, Object res) {
          Object digdeeper = null;
          if (ann.inverse()) {
             if (res instanceof URI || res instanceof URL) {
                query.append("{<").append(res.toString()).append(">} <").append(ann.value()).append("> {" +
                        getIdForObject(sourceObj) + "} ");
             } else {
                query.append("{").append(getIdForObject(res)).append("} <").append(ann.value()).append("> {" +
                        getIdForObject(sourceObj) + "} ");
                digdeeper = res;
             }
          } else {
             query.append("{" + getIdForObject(sourceObj) + "} <").append(ann.value()).append("> {");
             if (res instanceof Integer || res instanceof Float ||
                     res instanceof Long || res instanceof String ||
                     ((res instanceof java.net.URI || res instanceof java.net.URL) &&
                     ann.range().length() > 0)) {
                query.append("\"").append(res.toString()).append("\"");
                if (res instanceof Integer) {
                   query.append("^^xsd:integer");
                } else if (res instanceof Float) {
                   query.append("^^xsd:float");
                } else if (res instanceof Long) {
                   query.append("^^xsd:long");
                } else if (res instanceof java.net.URI ||
                        res instanceof java.net.URL) {
                   query.append("^^xsd:anyURI");
                }
             } else if (res instanceof Date) {
                query.append("\"").append(dateToXsdString((Date) res)).append("\"");
                query.append("^^xsd:dateTime");
             } else if (res instanceof java.net.URI ||
                     res instanceof java.net.URL) {
                query.append("<").append(res.toString()).append(">");
             } else {
                query.append(getIdForObject(res));
                digdeeper = res;
             }
             query.append("} ");
          }
 
          return digdeeper;
       }
 
       private void digdeeper(Object digdeeper) {
          if (digdeeper != null && digdeeper instanceof RdfSerialisable) {
             ((RdfSerialisable) digdeeper).rdfSerialise(this);
          }
       }
    }
 
    public <T> Collection<T> queryByExample(Object eg, Class<T> clazz) {
       if (!(eg instanceof RdfSerialisable)) {
          return new ArrayList<T>();
       }
       RdfSerialisable serObj = (RdfSerialisable) eg;
       QueryByExampleSerialiser query = new QueryByExampleSerialiser();
       serObj.rdfSerialise(query);
 
       log.info("query=" + query.toString());
       TupleQueryResult values = null;
       ArrayList<Value> rvalAnswers = new ArrayList<Value>();
       try {
          values = rep().prepareTupleQuery(QueryLanguage.SERQL, query.toString()).evaluate();
          while (values.hasNext()) {
             rvalAnswers.add(values.next().getValue("Obj0"));
          }
       } catch (MalformedQueryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (RepositoryException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } catch (QueryEvaluationException e) {
          e.printStackTrace(); //todo: decide what exception to throw
       } finally {
          if (values != null) {
             try {
                values.close();
             } catch (QueryEvaluationException e) {
                e.printStackTrace(); //todo: decide what exception to throw
             }
          }
       }
       ArrayList<T> results = new ArrayList<T>();
       for (Value rv : rvalAnswers) {
          T t = map(rv, clazz);
         if (t != null)
        	 results.add(t);
       }
 
       return results;
    }
 
    public Object createLiteral(String s) {
       return vf.createLiteral(s);
    }
 
    public Object createLiteral(String s, String lang) {
       return vf.createLiteral(s, lang);
    }
 
    public Object createLiteralType(String s, String uriType) {
       return vf.createLiteral(s, vf.createURI(uriType));
    }
 
    public Object createResource(String uri) {
       return vf.createURI(uri);
    }
 
    public long size() {
       try {
          return rep().size(graphs);
       } catch (RepositoryException ex) {
          Logger.getLogger(SesameMapper.class.getName()).log(Level.SEVERE, null, ex);
       }
       return -1;
    }
 
    public List<Class> getClassesOf(String uri) {
       List<Class> result = new ArrayList<Class>();
       RepositoryResult<Statement> typeStmts = null;
       try {
          URI ssmeuri = vf.createURI(uri);
          typeStmts = rep().getStatements(ssmeuri, RDF.TYPE, (Value) null, inference, graphs);
          if (enableDuplicateFilter) typeStmts.enableDuplicateFilter();
          while (typeStmts.hasNext()) {
             Statement stm = typeStmts.next();
             Value type = stm.getObject();
             if (type instanceof URI) {
                List<Class> clazzes = classmap.get(type.stringValue());
                if (clazzes != null) {
                   result.addAll(clazzes);
                }
             }
          }
          typeStmts.close();
       } catch (RepositoryException ex) {
          Logger.getLogger(SesameMapper.class.getName()).log(Level.SEVERE, null, ex);
       } finally {
          if (typeStmts != null) {
             try {
                typeStmts.close();
             } catch (RepositoryException ex) {
                Logger.getLogger(SesameMapper.class.getName()).log(Level.SEVERE, null, ex);
             }
          }
       }
 
       //filter out all the superclasses
       //this is a square type of calculation, but since the number of classes an object belongs to should
       //be relatively small, this should not be too bad. But watch out.
       int nulls = 0;
       outerloop:
       for (int i = 0; i < result.size(); i++) {
          Class outer = result.get(i);
          if (outer == null) {
             continue;
          }
          for (int ii = i + 1; ii < result.size(); ii++) {
             Class inner = result.get(ii);
             if (inner == null) {
                continue;
             }
             if (inner.isAssignableFrom(outer)) {
                result.set(ii, null);
                nulls++;
                continue;
             }
             if (outer.isAssignableFrom(inner)) {
                result.set(i, null);
                nulls++;
                continue outerloop;
             }
          }
       }
       if (nulls == 0) {
          return result;
       }
       ArrayList filteredres = new ArrayList<Class>(result.size() - nulls);
       for (Class c : result) {
          if (c != null) {
             filteredres.add(c);
          }
       }
 
       return filteredres;
    }
 
    public void enableInferencing(boolean on) {
       inference = on;
    }
 
    public boolean isInferencingEnabled() {
       return inference;
    }
 
    //if this were not allowed to return literals, this should return a Resource
    public Value getId(Object sommermaped) {
       return findKnownMappedValueFor(sommermaped);
    }
 
    public <T> ArrayList<T> getRelatedObject(Object subject, String relationURI, boolean inverse, Class<T> clazz) {
       Value res = findKnownMappedValueFor(subject);
       return getCollection(res, vf.createURI(relationURI), inverse, clazz);
    }
 
    /** make the field f accessible to do setting and getting operations on */
    private void configureField(final Field f) {
       if (!f.isAccessible()) {
          AccessController.doPrivileged(new PrivilegedAction<Object>() {
 
             public Object run() {
                f.setAccessible(true);
                return null;
             }
          });
       }
    }
 
    public void mapStatic(Class c) {
 
       //2. get all the static fields that are @rdf annotated
       for (Field fld : c.getDeclaredFields()) {
          if (!Modifier.isStatic(fld.getModifiers())) {
             continue;
          }
          rdf fann = fld.getAnnotation(rdf.class);
          if (fann == null) {
             continue;
          }
 
          //3. for each of those fields query the graph and set the value
          configureField(fld); //todo: do this only once ( perhaps in the map() code
          Object res;
          if (Collection.class.isAssignableFrom(fld.getType())) {
             if (fld.getAnnotation(functional.class) != null) {
                res = getCollectionField(c, fann, null, fld.getGenericType()); //TODO: pass the old value of the field, and do something with it
             } else {
                res = createVirtualCollection(c, fann, null, fld.getGenericType()); //TODO: pass the old value of the field...
             }
          } else {
             res = getField(fld.getType(), c, fann, null); //TODO: deal with old value
          }
          try {
             fld.set(c, res);
          } catch (IllegalArgumentException ex) {
             Logger.getLogger(SesameMapper.class.getName()).log(Level.SEVERE, null, ex);
          } catch (IllegalAccessException ex) {
             Logger.getLogger(SesameMapper.class.getName()).log(Level.SEVERE, null, ex);
          }
 
       }
 
 
 
 
    }
 
    /**
     * This about this implementiation a lot more carefully!
     * @param <T>
     * @param o
     * @param clazz
     * @return
     */
    public <T> T cast(Object o, Class<T> clazz) {
       Value id = getId(o);
       return map(id, clazz);
    }
 }
