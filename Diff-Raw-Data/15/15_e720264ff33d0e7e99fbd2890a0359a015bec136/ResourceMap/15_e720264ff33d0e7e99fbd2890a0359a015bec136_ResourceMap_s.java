 package rose.m3;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import rose.m3.RoseCollection.Book;
 import rose.m3.RoseCollection.ImageList;
 import rose.m3.RoseCollection.ImageTagging;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFList;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DC;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.DCTypes;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * Model rose content as Shared Canvas,
  * http://www.shared-canvas.org/datamodel/spec/.
  */
 public class ResourceMap {
     private static final String ORE_NS_URI = "http://www.openarchives.org/ore/terms/";
     private static final String FOAF_NS_URI = "http://xmlns.com/foaf/0.1/";
     private static final String OA_NS_URI = "http://www.w3.org/ns/oa#";
     private static final String EXIF_NS_URI = "http://www.w3.org/2003/12/exif/ns/";
     private static final String DCMI_TYPES_NS_URI = "http://purl.org/dc/dcmitype/";
     private static final String SHARED_CANVAS_NS_URI = "http://www.shared-canvas.org/ns/";
     private static final String CNT_NS_URI = "http://www.w3.org/2011/content#";
 
     private static final String READING_DIR_LEFT_TO_RIGHT = "Left-to-Right";
     // private static final String READING_DIR_RIGHT_TO_LEFT = "Right-to-Left";
     private static final String AGENT_NAME = "Rosa tools";
 
     private final Model model;
 
     private final Resource resource_map_type;
     private final Resource aggregation_type;
     private final Resource annotation_type;
     private final Resource annotation_list_type;
     private final Resource manifest_type;
     private final Resource sequence_type;
     private final Resource canvas_type;
     private final Resource image_type;
     private final Resource content_as_text_type;
 
     private final Resource painting_motivation;
     private final Resource describing_motivation;
 
     private final Property aggregates;
     private final Property describes;
     private final Property described_by;
     private final Property has_target;
     private final Property has_annotations;
     private final Property has_body;
     private final Property motivated_by;
     private final Property for_canvas;
     private final Property for_motivation;
     private final Property character_content;
     private final Property agent_label;
     private final Property location_label;
     private final Property date_label;
     private final Property has_related_description;
 
     private final Property name;
     private final Property image_width;
     private final Property image_height;
     private final Property reading_dir;
 
     public ResourceMap() {
         this.model = ModelFactory.createDefaultModel();
 
         this.resource_map_type = model.createResource(ORE_NS_URI
                 + "ResourceMap");
         this.aggregation_type = model
                 .createResource(ORE_NS_URI + "Aggregation");
         this.annotation_type = model.createResource(OA_NS_URI + "Annotation");
         this.annotation_list_type = model.createResource(SHARED_CANVAS_NS_URI
                 + "AnnotationList");
         this.sequence_type = model.createResource(SHARED_CANVAS_NS_URI
                 + "Sequence");
         this.manifest_type = model.createResource(SHARED_CANVAS_NS_URI
                 + "Manifest");
         this.canvas_type = model
                 .createResource(SHARED_CANVAS_NS_URI + "Canvas");
         this.image_type = model.createResource(DCMI_TYPES_NS_URI + "Image");
         this.content_as_text_type = model.createResource(CNT_NS_URI
                 + "ContentAsText");
 
         this.painting_motivation = model.createResource(SHARED_CANVAS_NS_URI
                 + "painting");
         this.describing_motivation = model.createResource(OA_NS_URI
                 + "describing");
 
         this.aggregates = model.createProperty(ORE_NS_URI + "aggregates");
         this.describes = model.createProperty(ORE_NS_URI + "describes");
         this.described_by = model.createProperty(ORE_NS_URI + "isDescribedBy");
         this.has_target = model.createProperty(OA_NS_URI + "hasTarget");
         this.has_body = model.createProperty(OA_NS_URI + "hasBody");
         this.has_annotations = model.createProperty(OA_NS_URI
                 + "hasAnnotations");
         this.motivated_by = model.createProperty(OA_NS_URI + "motivatedBy");
         this.for_canvas = model.createProperty(SHARED_CANVAS_NS_URI
                 + "forCanvas");
         this.for_motivation = model.createProperty(SHARED_CANVAS_NS_URI
                 + "forMotivation");
         this.character_content = model.createProperty(CNT_NS_URI + "chars");
         this.agent_label = model.createProperty(SHARED_CANVAS_NS_URI
                 + "agentLabel");
         this.date_label = model.createProperty(SHARED_CANVAS_NS_URI
                 + "dateLabel");
         this.location_label = model.createProperty(SHARED_CANVAS_NS_URI
                 + "locationLabel");
         this.has_related_description = model
                 .createProperty(SHARED_CANVAS_NS_URI + "hasRelatedDescription");
 
         this.name = model.createProperty(FOAF_NS_URI + "name");
         this.image_width = model.createProperty(EXIF_NS_URI + "width");
         this.image_height = model.createProperty(EXIF_NS_URI + "height");
         this.reading_dir = model.createProperty(SHARED_CANVAS_NS_URI
                 + "readingDirection");
 
         model.setNsPrefix("foaf", FOAF_NS_URI);
         model.setNsPrefix("ore", ORE_NS_URI);
         model.setNsPrefix("oa", OA_NS_URI);
         model.setNsPrefix("dcterms", DCTerms.NS);
         model.setNsPrefix("exif", EXIF_NS_URI);
         model.setNsPrefix("dcmitypes", DCMI_TYPES_NS_URI);
     }
 
     private String get_aggregation_uri(String resource_map_uri) {
         return resource_map_uri + "#aggregation";
     }
 
     private void add_resource_map_and_aggregation(String service_url,
             Resource agg) {
         Resource resmap = model.createResource(service_url);
 
         resmap.addProperty(RDF.type, resource_map_type);
 
         Resource agent = model.createResource();
         agent.addProperty(RDF.type, DCTerms.AgentClass);
         agent.addProperty(name, AGENT_NAME);
 
         model.add(resmap, DCTerms.creator, agent);
 
         // TODO What should modified value be?
         resmap.addLiteral(DCTerms.modified,
                 model.createTypedLiteral(GregorianCalendar.getInstance()));
 
         agg.addProperty(RDF.type, aggregation_type);
 
         model.add(resmap, describes, agg);
         model.add(agg, described_by, resmap);
     }
 
     /**
      * Create a resource map and aggregation. Return the aggregation.
      * 
      * @param service_url
      * @return aggregation.
      */
     private Resource add_resource_map_and_aggregation(String service_url) {
         Resource agg = model.createResource(get_aggregation_uri(service_url));
         add_resource_map_and_aggregation(service_url, agg);
         return agg;
     }
 
     public Model modelCollection(String service_url, RoseCollection col) {
         Resource agg = add_resource_map_and_aggregation(service_url);
 
         agg.addProperty(RDFS.label, col.name());
 
         for (int i = 0; i < col.size(); i++) {
             RoseCollection.Book b = col.getBook(i);
             Resource manifest = model
                     .createResource(service_url + "/" + b.id());
             manifest.addProperty(RDF.type, manifest_type);
 
             manifest.addProperty(RDFS.label, b.fullName());
             model.add(agg, aggregates, manifest);
         }
 
         return model;
     }
 
     private String get_canvas_uri(Book book, String image_id) {
         return book.dataUrl() + RoseCollection.shortImageName(image_id)
                 + "#canvas";
     }
 
     private String get_illustration_annotation_uri(Book book, String image_id,
             int which) {
         return book.dataUrl() + RoseCollection.shortImageName(image_id)
                 + "#annotation;illustration;" + which;
     }
 
     private String get_transcription_annotation_uri(Book book, String image_id) {
         return book.dataUrl() + RoseCollection.shortImageName(image_id)
                 + "#annotation;transcription";
     }
 
     private String get_image_annotation_uri(Book book, String image_id) {
         return book.dataUrl() + RoseCollection.shortImageName(image_id)
                 + "#image";
     }
 
     private void make_list(Resource res, List<Resource> seq) {
         RDFList list = model.createList(seq.iterator());
 
         if (list.size() > 0) {
             res.addProperty(RDF.type, RDF.List);
             res.addProperty(RDF.first, list.get(0));
             res.addProperty(RDF.rest, list.removeHead());
         }
     }
 
     public Model modelReadingSequence(String service_url, Book book)
             throws IOException {
         Resource sequence = add_resource_map_and_aggregation(service_url);
 
         sequence.addProperty(RDF.type, sequence_type);
         sequence.addProperty(RDFS.label, "Read");
         sequence.addProperty(reading_dir, READING_DIR_LEFT_TO_RIGHT);
 
         ImageList images = book.retrieveImageList();
 
         List<Resource> canvas_seq = new ArrayList<Resource>();
 
         for (int i = 0; i < images.size(); i++) {
             String image_id = images.image(i);
             String canvas_uri = get_canvas_uri(book, image_id);
 
             Resource canvas = model.createResource(canvas_uri);
             canvas.addProperty(RDF.type, canvas_type);
 
             canvas.addLiteral(RDFS.label,
                     RoseCollection.shortImageName(image_id));
 
            if (!images.missing(i)) {
                 canvas.addLiteral(image_width, images.width(i));
                 canvas.addLiteral(image_height, images.height(i));
             }
 
             String annotation_url = service_url.replace("/sequence", "/canvas/"
                     + RoseCollection.shortImageName(image_id) + "/annotations");
             canvas.addProperty(has_annotations,
                     model.createResource(annotation_url));
 
             canvas_seq.add(canvas);
         }
 
         make_list(sequence, canvas_seq);
 
         return model;
     }
 
     public Model modelManifest(String service_url, Book book)
             throws IOException {
         Resource manifest = add_resource_map_and_aggregation(service_url);
 
         manifest.addProperty(RDF.type, manifest_type);
 
         manifest.addProperty(RDFS.label, book.fullName());
         manifest.addProperty(agent_label, book.repository());
         manifest.addProperty(date_label, book.date());
         manifest.addProperty(location_label, book.location());
         manifest.addProperty(DC.rights, book.permissionStatement());
         manifest.addProperty(has_related_description, book.descriptionUrl());
 
         {
             Resource r = model.createResource(service_url + "/" + "sequence");
             r.addProperty(RDF.type, sequence_type);
             model.add(manifest, aggregates, r);
         }
 
         {
             Resource r = model
                     .createResource(service_url + "/" + "annotations");
             r.addProperty(RDF.type, annotation_list_type);
             model.add(manifest, aggregates, r);
         }
 
         return model;
     }
 
     public Model modelTranscriptionAnnotations(String service_url, Book book)
             throws IOException {
         Resource annotations = add_resource_map_and_aggregation(service_url);
         annotations.addProperty(RDF.type, annotation_list_type);
         annotations.addProperty(for_motivation, describing_motivation);
 
         add_transcription_annotations(book, annotations);
 
         return model;
     }
 
     private Resource add_text_annotation(Resource agg, String annotation_uri,
             String target_uri, String text, String format) {
         Resource text_annotation = model.createResource(annotation_uri);
 
         text_annotation.addProperty(RDF.type, annotation_type);
         text_annotation.addProperty(motivated_by, describing_motivation);
 
         Resource target = model.createResource(target_uri);
         text_annotation.addProperty(has_target, target);
 
         Resource text_annotation_body = model.createResource();
         text_annotation_body.addProperty(RDF.type, DCTypes.Text);
         text_annotation_body.addProperty(RDF.type, content_as_text_type);
         text_annotation_body.addProperty(DC.format, format);
         text_annotation_body.addProperty(character_content, text);
 
         text_annotation.addProperty(has_body, text_annotation_body);
 
         return text_annotation;
     }
 
     private List<Resource> add_illustration_annotations(Book book,
             Resource annotations) throws IOException {
         List<Resource> result = new ArrayList<Resource>();
 
         if (!book.hasIllustrationTagging()) {
             return result;
         }
 
         ImageList images = book.retrieveImageList();
         ImageTagging tagging = book.retrieveIllustrationTagging(images);
 
         for (int i = 0; i < images.size(); i++) {
             String image_id = images.image(i);
 
             result.addAll(add_illustration_annotations(book, tagging, i,
                     image_id, annotations));
         }
 
         return result;
     }
 
     private List<Resource> add_illustration_annotations(Book book,
             ImageTagging tagging, int image, String image_id,
             Resource annotations) throws IOException {
         String canvas_uri = get_canvas_uri(book, image_id);
 
         List<Resource> result = new ArrayList<Resource>();
 
         for (int illus : tagging.findIllusIndexes(image)) {
             String uri = get_illustration_annotation_uri(book, image_id, illus);
             String text = tagging.descriptions(illus);
 
             Resource a = add_text_annotation(annotations, uri, canvas_uri,
                     text, "text/plain");
 
             if (a != null) {
                 result.add(a);
             }
         }
 
         return result;
     }
 
     private List<Resource> add_transcription_annotations(Book book,
             Resource annotations) throws IOException {
         ImageList images = book.retrieveImageList();
 
         List<Resource> result = new ArrayList<Resource>();
 
         if (!book.hasTranscription()) {
             return result;
         }
 
         for (int i = 0; i < images.size(); i++) {
             Resource r = add_transcription_annotation(book, images, i,
                     annotations);
 
             if (r != null) {
                 result.add(r);
             }
         }
 
         return result;
     }
 
     private Resource add_transcription_annotation(Book book, ImageList images,
             int image, Resource annotations) throws IOException {
         String image_id = images.image(image);
         String canvas_uri = get_canvas_uri(book, image_id);
 
         String uri = get_transcription_annotation_uri(book, image_id);
         ByteArray data = new ByteArray(4 * 1024);
 
         URL url = new URL(images.transcriptionUrl(image));
 
         HttpURLConnection huc = (HttpURLConnection) url.openConnection();
         huc.setRequestMethod("GET");
         huc.connect();
 
         if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {
             InputStream is = huc.getInputStream();
             data.append(is);
             is.close();
             huc.disconnect();
 
             String text = new String(data.array, 0, data.length, "UTF-8");
             return add_text_annotation(annotations, uri, canvas_uri, text,
                     "text/xml");
         } else {
             return null;
         }
     }
 
     public Model modelIllustrationDescriptionAnnotations(String service_url,
             Book book) throws IOException {
         Resource annotations = add_resource_map_and_aggregation(service_url);
         annotations.addProperty(RDF.type, annotation_list_type);
         annotations.addProperty(for_motivation, describing_motivation);
 
         add_illustration_annotations(book, annotations);
 
         return model;
     }
 
     private List<Resource> add_image_annotations(Book book, Resource annotations)
             throws IOException {
         List<Resource> result = new ArrayList<Resource>();
 
         ImageList images = book.retrieveImageList();
 
         for (int i = 0; i < images.size(); i++) {
             Resource a = add_image_annotation(book, images, i, annotations);
 
             if (a != null) {
                 result.add(a);
             }
         }
 
         return result;
     }
 
     private Resource add_image_annotation(Book book, ImageList images,
             int image, Resource annotations) throws IOException {
         String image_id = images.image(image);
         String image_uri = get_image_annotation_uri(book, image_id);
         String iiif_service_url = images.iiifServiceUrl(image);
         String canvas_uri = get_canvas_uri(book, image_id);
 
         if (images.missing(image)) {
             return null;
         }
 
         Resource image_annotation = model.createResource(image_uri);
         image_annotation.addProperty(RDF.type, annotation_type);
         image_annotation.addProperty(motivated_by, painting_motivation);
 
         Resource canvas = model.createResource(canvas_uri);
         canvas.addProperty(RDF.type, canvas_type);
 
         Resource image_annotation_body = model.createResource(iiif_service_url);
 
         image_annotation.addProperty(has_body, image_annotation_body);
         image_annotation.addProperty(has_target, canvas);
 
         image_annotation_body.addProperty(DC.format, "image/jpg");
         image_annotation_body.addProperty(DCTerms.conformsTo, "IIIF");
         image_annotation_body.addProperty(RDF.type, image_type);
 
         return image_annotation;
     }
 
     public Model modelImageAnnotations(String service_url, Book book)
             throws IOException {
         Resource annotations = add_resource_map_and_aggregation(service_url);
         annotations.addProperty(RDF.type, annotation_list_type);
         annotations.addProperty(for_motivation, painting_motivation);
 
         List<Resource> seq = new ArrayList<Resource>();
 
         seq.addAll(add_image_annotations(book, annotations));
 
         make_list(annotations, seq);
 
         return model;
     }
 
     public Model modelAllAnnotations(String service_url, Book book)
             throws IOException {
         Resource annotations = add_resource_map_and_aggregation(service_url);
         annotations.addProperty(RDF.type, annotation_list_type);
 
         List<Resource> seq = new ArrayList<Resource>();
 
         seq.addAll(add_image_annotations(book, annotations));
         seq.addAll(add_illustration_annotations(book, annotations));
         seq.addAll(add_transcription_annotations(book, annotations));
 
         make_list(annotations, seq);
 
         return model;
     }
 
     public Model modelAllAnnotationsOfCanvas(String service_url, Book book,
             String image_frag) throws IOException {
         Resource annotations = add_resource_map_and_aggregation(service_url);
         annotations.addProperty(RDF.type, annotation_list_type);
 
         ImageList images = book.retrieveImageList();
 
         int image = images.guess(image_frag);
 
         if (image == -1) {
             return model;
         }
 
         String image_id = images.image(image);
         String canvas_uri = get_canvas_uri(book, image_id);
         Resource canvas = model.createResource(canvas_uri);
 
         annotations.addProperty(for_canvas, canvas);
 
         List<Resource> seq = new ArrayList<Resource>();
 
         {
             Resource a = add_image_annotation(book, images, image, annotations);
 
             if (a != null) {
                 seq.add(a);
             }
         }
 
         if (book.hasIllustrationTagging()) {
             ImageTagging tagging = book.retrieveIllustrationTagging(images);
 
             seq.addAll(add_illustration_annotations(book, tagging, image,
                     image_id, annotations));
         }
 
         if (book.hasTranscription()) {
             Resource a = add_transcription_annotation(book, images, image,
                     annotations);
 
             if (a != null) {
                 seq.add(a);
             }
         }
 
         make_list(annotations, seq);
 
         return model;
     }
 }
