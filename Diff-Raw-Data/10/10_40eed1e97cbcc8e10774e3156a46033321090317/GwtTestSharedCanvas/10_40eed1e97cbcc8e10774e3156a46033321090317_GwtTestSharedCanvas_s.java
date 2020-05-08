 package rosa.scanvas.model.client.jsonld;
 
 import java.util.List;
 
 import rosa.scanvas.model.client.Annotation;
 import rosa.scanvas.model.client.AnnotationBody;
 import rosa.scanvas.model.client.AnnotationList;
 import rosa.scanvas.model.client.Canvas;
 import rosa.scanvas.model.client.Manifest;
 import rosa.scanvas.model.client.ManifestCollection;
 import rosa.scanvas.model.client.Reference;
 import rosa.scanvas.model.client.Sequence;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class GwtTestSharedCanvas extends AbstractGwtTest {
     private final static String COLLECTION_ENDPOINT = "http://rosetest.library.jhu.edu/sc/";
     private final static String MANIFEST_ENDPOINT = COLLECTION_ENDPOINT
             + "Douce195";
     private final static String ANNOTATION_LIST_ENDPOINT = COLLECTION_ENDPOINT
             + "Douce195/canvas/1r/annotations";
     private final static String SEQUENCE_ENDPOINT = COLLECTION_ENDPOINT
             + "Douce195/sequence";
 
     private <T> void check_refs(List<Reference<T>> refs, Class<T> type,
             List<String> aggregates) {
         for (Reference<T> ref : refs) {
             assertNotNull(ref.uri());
 
             String label = ref.label();
 
             if (label != null) {
                 assertFalse(label.isEmpty());
             }
 
             assertEquals(type, ref.type());
             assertTrue(aggregates.contains(ref.uri()));
         }
     }
 
     private void check_manifest_collection(ManifestCollection col) {
         assertNotNull(col);
         assertNotNull(col.label());
         assertNotNull(col.creatorName());
 
         List<String> aggregates = col.aggregates();
         List<Reference<Manifest>> refs = col.manifests();
 
         assertTrue(aggregates.size() > 0);
         assertTrue(refs.size() > 0);
         assertEquals(aggregates.size(), refs.size());
 
         check_refs(refs, Manifest.class, aggregates);
     }
 
     private void check_manifest(Manifest man) {
         assertNotNull(man);
         assertNotNull(man.label());
         assertNotNull(man.creatorName());
         assertNotNull(man.agent());
         assertNotNull(man.date());
         assertNotNull(man.hasRelatedDescription());

         List<String> aggregates = man.aggregates();
 
         assertTrue(aggregates.size() > 0);
 
         List<Reference<AnnotationList>> als = man.annotationsLists();
         List<Reference<Sequence>> seqs = man.sequences();
 
         assertEquals(aggregates.size(), als.size() + seqs.size());
 
         check_refs(als, AnnotationList.class, aggregates);
         check_refs(seqs, Sequence.class, aggregates);
     }
 
     private void check_annotation_list_of_canvas(AnnotationList al) {
         assertNotNull(al);
         assertNotNull(al.uri());
         // assertNotNull(al.label());
         assertNotNull(al.creatorName());
         assertNotNull(al.forCanvas());
 
         List<String> aggregates = al.aggregates();
 
         assertTrue(aggregates.size() > 0);
 
         assertEquals(aggregates.size(), al.size());
 
         for (int i = 0; i < al.size(); i++) {
             Annotation a = al.annotation(i);
 
             assertNotNull(a);
             assertNotNull(a.uri());
 
             assertTrue(a.targets().size() > 0);
 
             for (String target : a.targets()) {
                 assertNotNull(target);
             }
 
             AnnotationBody body = a.body();
             assertNotNull(body);
 
             assertNotNull(body.uri());
             assertNotNull(body.format());
 
             if (body.isImage()) {
                 assertNull(body.defaultItem());
                 assertTrue(body.otherItems().isEmpty());
                 assertTrue(body.format().startsWith("image"));
                 assertEquals("IIIF", body.conformsTo());
             } else if (body.isText()) {
                 assertNull(body.defaultItem());
                 assertTrue(body.otherItems().isEmpty());
 
                 assertTrue(body.format().startsWith("text"));
                 assertNotNull(body.textContent());
                 assertFalse(body.textContent().isEmpty());
             }
         }
     }
     
     private void check_sequence(Sequence seq) {
         assertNotNull(seq);
         assertNotNull(seq.uri());
         assertNotNull(seq.label());
         assertNotNull(seq.creatorName());
         assertNotNull(seq.readingDirection());
 
         assertTrue(seq.size() > 0);
         
         List<String> aggregates = seq.aggregates();
 
         assertTrue(aggregates.size() > 0);
         assertEquals(aggregates.size(), seq.size());
                 
         for (Canvas canvas: seq) {
             assertNotNull(canvas.uri());
             assertNotNull(canvas.label());
             assertNotNull(canvas.hasAnnotations());
 
             assertTrue(canvas.height() > 0);
             assertTrue(canvas.width() > 0);
 
             assertTrue(aggregates.contains(canvas.uri()));
             
             assertTrue(canvas.hasAnnotations().size() > 0);
             
             for (Reference<AnnotationList> ref: canvas.hasAnnotations()) {
                 assertNotNull(ref.uri());
             }
         }
     }
 
     public void testManifestCollection() {
         checkRemoteSharedCanvas(COLLECTION_ENDPOINT, ManifestCollection.class,
                 new AsyncCallback<ManifestCollection>() {
                     public void onFailure(Throwable error) {
                         fail(error.getMessage());
                     }
 
                     public void onSuccess(ManifestCollection col) {
                         check_manifest_collection(col);
                     }
                 });
     }
 
     public void testManifest() {
         checkRemoteSharedCanvas(MANIFEST_ENDPOINT, Manifest.class,
                 new AsyncCallback<Manifest>() {
                     public void onFailure(Throwable error) {
                         fail(error.getMessage());
                     }
 
                     public void onSuccess(Manifest man) {
                         check_manifest(man);
                     }
                 });
     }
 
     public void testAnnotationListOfCanvas() {
         checkRemoteSharedCanvas(ANNOTATION_LIST_ENDPOINT, AnnotationList.class,
                 new AsyncCallback<AnnotationList>() {
                     public void onFailure(Throwable error) {
                         fail(error.getMessage());
                     }
 
                     public void onSuccess(AnnotationList al) {
                         check_annotation_list_of_canvas(al);
                     }
                 });
     }
 
     public void testSequence() {
         checkRemoteSharedCanvas(SEQUENCE_ENDPOINT, Sequence.class,
                 new AsyncCallback<Sequence>() {
                     public void onFailure(Throwable error) {
                         fail(error.getMessage());
                     }
 
                     public void onSuccess(Sequence seq) {
                         check_sequence(seq);
                     }
                 });
     }
 }
