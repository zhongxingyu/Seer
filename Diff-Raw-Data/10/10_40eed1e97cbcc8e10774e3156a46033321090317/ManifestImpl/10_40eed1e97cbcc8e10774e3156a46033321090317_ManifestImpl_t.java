 package rosa.scanvas.model.client.impl;
 
 import java.util.List;
 
 import rosa.scanvas.model.client.AnnotationList;
 import rosa.scanvas.model.client.Manifest;
 import rosa.scanvas.model.client.Reference;
 import rosa.scanvas.model.client.Sequence;
 import rosa.scanvas.model.client.rdf.RdfException;
 import rosa.scanvas.model.client.rdf.RdfGraph;
 
 public class ManifestImpl extends ResourceMapImpl implements Manifest {
     public ManifestImpl(RdfGraph graph) throws RdfException {
         super(graph);
     }
 
     @Override
     public String label() {
         return graph.findObjectStringValue(aggregation_uri(), RDFS_LABEL);
     }
 
     @Override
     public String agent() {
         return graph.findObjectStringValue(aggregation_uri(),
                 SHARED_CANVAS_AGENT_LABEL);
     }
 
     @Override
     public String date() {
         return graph.findObjectStringValue(aggregation_uri(),
                 SHARED_CANVAS_DATE_LABEL);
     }
 
     public String rights() {
        return graph.findObjectStringValue(aggregation_uri(), DC_RIGHTS);
     }
 
     @Override
     public String location() {
         return graph.findObjectStringValue(aggregation_uri(),
                 SHARED_CANVAS_LOCATION_LABEL);
     }
 
     @Override
     public String description() {
         return graph.findObjectStringValue(aggregation_uri(), DC_DESCRIPTION);
     }
 
     @Override
     public String hasRelatedDescription() {
         return graph.findObjectStringValue(aggregation_uri(),
                 SHARED_CANVAS_HAS_RELATED_DESCRIPTION);
     }
 
     @Override
     public String hasRelatedService() {
         return graph.findObjectStringValue(aggregation_uri(),
                 SHARED_CANVAS_HAS_RELATED_SERVICE);
     }
 
     @Override
     public List<Reference<Sequence>> sequences() {
         return aggregatedReferences(SHARED_CANVAS_SEQUENCE, Sequence.class);
     }
 
     @Override
     public List<Reference<AnnotationList>> annotationsLists() {
         return aggregatedReferences(SHARED_CANVAS_ANNOTATION_LIST,
                 AnnotationList.class);
     }
 }
