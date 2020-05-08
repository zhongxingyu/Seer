 package dk.statsbiblioteket.doms.gui.model;
 
 import dk.statsbiblioteket.doms.client.datastreams.Datastream;
 import dk.statsbiblioteket.doms.client.exceptions.NotFoundException;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.exceptions.XMLParseException;
import dk.statsbiblioteket.doms.client.methods.Method;
 import dk.statsbiblioteket.doms.client.objects.CollectionObject;
 import dk.statsbiblioteket.doms.client.objects.ContentModelObject;
 import dk.statsbiblioteket.doms.client.objects.DataObject;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.client.relations.LiteralRelation;
 import dk.statsbiblioteket.doms.client.relations.ObjectRelation;
 import dk.statsbiblioteket.doms.client.relations.Relation;
 import dk.statsbiblioteket.doms.client.utils.Constants;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: abr
  * Date: 10/25/11
  * Time: 3:17 PM
  * To change this template use File | Settings | File Templates.
  */
public class DigitalObjectTreeWrapper implements DataObject {
 
     private DigitalObject wrapped;
 
 
 
     public DigitalObjectTreeWrapper(DigitalObject wrapped) {
             this.wrapped = wrapped;
     }
 
     public DigitalObject getWrapped() {
         return wrapped;
     }
 
     public List<DigitalObject> getChildren() throws ServerOperationFailed {
         Set<DigitalObject> localchildren = getChildObjects(Constants.VIEW_GUI);
         List<DigitalObject> resultChildren = new ArrayList<DigitalObject>();
         for (DigitalObject localchild : localchildren) {
             resultChildren.add(new DigitalObjectTreeWrapper(localchild));
         }
         return resultChildren;
     }
 
     @Override
     public String getContentmodelTitle() throws ServerOperationFailed {
         if (wrapped instanceof DataObject) {
             DataObject dataObject = (DataObject) wrapped;
             return dataObject.getContentmodelTitle();
         }
         return "Doms Object";
     }
 
    public Set<Method> getMethods() throws ServerOperationFailed {
        return null;
    }
    
     @Override
     public void save() throws ServerOperationFailed, XMLParseException {
         wrapped.save();
     }
 
     @Override
     public void save(String viewAngle) throws ServerOperationFailed, XMLParseException {
         wrapped.save(viewAngle);
     }
 
     @Override
     public String getPid() {
         return wrapped.getPid();
     }
 
     @Override
     public Set<CollectionObject> getCollections() throws ServerOperationFailed {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void addToCollection(CollectionObject collection) throws ServerOperationFailed {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     @Override
     public List<ContentModelObject> getType() throws ServerOperationFailed {
         return wrapped.getType();
     }
 
     @Override
     public String getTitle() throws ServerOperationFailed {
         return wrapped.getTitle();
     }
 
     @Override
     public void setTitle(String title) throws ServerOperationFailed {
         wrapped.setTitle(title);
     }
 
     @Override
     public Constants.FedoraState getState() throws ServerOperationFailed {
         return wrapped.getState();
     }
 
     @Override
     public void setState(Constants.FedoraState state) throws ServerOperationFailed {
         wrapped.setState(state);
     }
 
     @Override
     public void setState(Constants.FedoraState state, String viewAngle) throws ServerOperationFailed {
         wrapped.setState(state, viewAngle);
     }
 
     @Override
     public Date getLastModified() throws ServerOperationFailed {
         return wrapped.getLastModified();
     }
 
     @Override
     public Date getCreated() throws ServerOperationFailed {
         return wrapped.getCreated();
     }
 
     @Override
     public List<Datastream> getDatastreams() throws ServerOperationFailed {
         return wrapped.getDatastreams();
     }
 
     @Override
     public Datastream getDatastream(String id) throws ServerOperationFailed, NotFoundException {
         return wrapped.getDatastream(id);
     }
 
     @Override
     public void addDatastream(Datastream addition) throws ServerOperationFailed {
         wrapped.addDatastream(addition);
     }
 
     @Override
     public void removeDatastream(Datastream deleted) throws ServerOperationFailed {
         wrapped.removeDatastream(deleted);
     }
 
     @Override
     public List<Relation> getRelations() throws ServerOperationFailed {
         return wrapped.getRelations();
     }
 
     @Override
     public List<ObjectRelation> getInverseRelations() throws ServerOperationFailed {
         return wrapped.getInverseRelations();
     }
 
     @Override
     public void removeRelation(Relation relation) {
         wrapped.removeRelation(relation);
     }
 
     @Override
     public ObjectRelation addObjectRelation(String predicate, DigitalObject object) throws ServerOperationFailed {
         return wrapped.addObjectRelation(predicate, object);
     }
 
     @Override
     public LiteralRelation addLiteralRelation(String predicate, String value) {
         return wrapped.addLiteralRelation(predicate, value);
     }
 
     @Override
     public Set<DigitalObject> getChildObjects(String viewAngle) throws ServerOperationFailed {
         return wrapped.getChildObjects(viewAngle);
     }
 
     @Override
     public List<ObjectRelation> getInverseRelations(String predicate) throws ServerOperationFailed {
         return wrapped.getInverseRelations(predicate);
     }
 }
