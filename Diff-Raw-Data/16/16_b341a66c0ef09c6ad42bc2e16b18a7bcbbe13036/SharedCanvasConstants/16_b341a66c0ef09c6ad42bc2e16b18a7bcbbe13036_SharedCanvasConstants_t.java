 package rosa.scanvas.model.client;
 
 // TODO clean this up
 
 public interface SharedCanvasConstants {
     public static final String ORE_NS_URI = "http://www.openarchives.org/ore/terms/";
     public static final String FOAF_NS_URI = "http://xmlns.com/foaf/0.1/";
     public static final String OA_NS_URI = "http://www.w3.org/ns/oa#";
     public static final String EXIF_NS_URI = "http://www.w3.org/2003/12/exif/ns/";
     public static final String DCMI_TYPES_NS_URI = "http://purl.org/dc/dcmitype/";
     public static final String SHARED_CANVAS_NS_URI = "http://www.shared-canvas.org/ns/";
     public static final String CNT_NS_URI = "http://www.w3.org/2011/content#";
     public static final String DCTERMS_NS_URI = "http://purl.org/dc/terms/";
     public static final String DC_NS_URI = "http://purl.org/dc/elements/1.1/";
     public static final String RDFS_NS_URI = "http://www.w3.org/2000/01/rdf-schema#";
     public static final String RDF_NS_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
 
     public static final String ORE_RESOURCE_MAP = ORE_NS_URI + "ResourceMap";
     public static final String ORE_DESCRIBES = ORE_NS_URI + "describes";
     public static final String ORE_AGGREGATES = ORE_NS_URI + "aggregates";
 
     public static final String DCTERMS_CREATOR = DCTERMS_NS_URI + "creator";
     public static final String DCTERMS_CONFORMS_TO = DCTERMS_NS_URI
             + "conformsTo";
 
     public static final String FOAF_NAME = FOAF_NS_URI + "name";
 
     public static final String DC_RIGHTS = DC_NS_URI + "rights";
     public static final String DC_DESCRIPTION = DC_NS_URI + "description";
     public static final String DC_FORMAT = DC_NS_URI + "format";
 
     public static final String EXIF_WIDTH = EXIF_NS_URI + "width";
     public static final String EXIF_HEIGHT = EXIF_NS_URI + "height";
 
     public static final String RDFS_LABEL = RDFS_NS_URI + "label";
 
     public static final String SHARED_CANVAS_MANIFEST = SHARED_CANVAS_NS_URI
             + "Manifest";
     public static final String SHARED_CANVAS_SEQUENCE = SHARED_CANVAS_NS_URI
             + "Sequence";
     public static final String SHARED_CANVAS_ANNOTATION_LIST = SHARED_CANVAS_NS_URI
             + "AnnotationList";
     public static final String SHARED_CANVAS_CANVAS = SHARED_CANVAS_NS_URI
             + "Canvas";
 
     public static final String SHARED_CANVAS_AGENT_LABEL = SHARED_CANVAS_NS_URI
             + "agentLabel";
 
    public static final String OA_HAS_ANNOTATIONS = OA_NS_URI
             + "hasAnnotations";
 
     public static final String SHARED_CANVAS_FOR_CANVAS = SHARED_CANVAS_NS_URI
             + "forCanvas";
 
     public static final String SHARED_CANVAS_FOR_MOTIVATION = SHARED_CANVAS_NS_URI
             + "forMotivation";
 
     public static final String SHARED_CANVAS_DATE_LABEL = SHARED_CANVAS_NS_URI
             + "dateLabel";
 
     public static final String SHARED_CANVAS_LOCATION_LABEL = SHARED_CANVAS_NS_URI
             + "locationLabel";
 
     public static final String SHARED_CANVAS_HAS_RELATED_DESCRIPTION = SHARED_CANVAS_NS_URI
             + "hasRelatedDescription";
 
     public static final String SHARED_CANVAS_HAS_RELATED_SERVICE = SHARED_CANVAS_NS_URI
             + "hasRelatedService";
     public static final String SHARED_CANVAS_READING_DIR = SHARED_CANVAS_NS_URI
             + "readingDirection";
 
     public static final String RDF_TYPE = RDF_NS_URI + "type";
 
     public static final String OA_ANNOTATION = OA_NS_URI + "Annotation";
 
     public static final String OA_MOTIVATED_BY = OA_NS_URI + "motivatedBy";
 
     public static final String OA_HAS_TARGET = OA_NS_URI + "hasTarget";
 
     public static final String OA_HAS_BODY = OA_NS_URI + "hasBody";
 
     public static final String OA_CHOICE = OA_NS_URI + "Choice";
 
     public static final String OA_COMPOSITE = OA_NS_URI + "Composite";
 
     public static final String OA_DEFAULT = OA_NS_URI + "default";
     
     public static final String OA_ITEM = OA_NS_URI + "item";
     
     public static final String DCMI_IMAGE = DCMI_TYPES_NS_URI + "Image";
 
     public static final String DCMI_TEXT = DCMI_TYPES_NS_URI + "Text";
 
     public static final String CNT_CHARS = CNT_NS_URI + "chars";
 }
