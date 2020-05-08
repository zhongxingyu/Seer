 package gov.nih.nci.cagrid.solr;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.solr.common.params.SolrParams;
 import org.apache.solr.common.util.NamedList;
 import org.apache.solr.core.SolrCore;
 import org.apache.solr.handler.component.ResponseBuilder;
 import org.apache.solr.handler.component.SearchComponent;
 import org.apache.solr.search.DocIterator;
 import org.apache.solr.search.DocList;
 import org.apache.solr.util.plugin.SolrCoreAware;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 
 public class PortalTreeComponent extends SearchComponent implements SolrCoreAware {
 
     private Log logger = LogFactory.getLog(getClass());
 
     public static final String TREE = "tree";
     private NamedList initParams = null;
 
 
     public static final String TYPE_FIELD = "type_field";
     public static final String TYPE_LABEL = "type_label";
     public static final String TERM_FIELD = "terms_field";
     public static final String TERM_LABEL = "terms_label";
 
     protected Map<String, String> treeLabelMap;
 
     String typeField, typeLabel;
     String termField, termLabel;
 
 
     @Override
     @SuppressWarnings("unchecked")
     public void init(NamedList args) {
         super.init(args);
         this.initParams = args;
     }
 
     public void inform(SolrCore core) {
 
         this.typeField = (String) this.initParams.get(TYPE_FIELD);
         this.typeLabel = (String) this.initParams.get(TYPE_LABEL);
         this.termField = (String) this.initParams.get(TERM_FIELD);
         this.termLabel = (String) this.initParams.get(TERM_LABEL);
 
 
 //
 //        if (a != null) {
 //            FieldType ft = core.getSchema().getFieldTypes().get(a);
 //            if (ft == null) {
 //                throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
 //                        "Unknown FieldType: '" + a + "' used in PortalTreeComponent");
 //            }
 //
 //        }
         treeLabelMap = SolrParams.toMap((NamedList) this.initParams.get(this.typeField));
     }
 
 
     public void prepare(ResponseBuilder rb) throws IOException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void process(ResponseBuilder rb) throws IOException {
         SolrParams p = rb.req.getParams();
         if (p.getBool(PortalTreeComponent.TREE, false)) {
             IndexReader reader = rb.req.getSearcher().getReader();
 
             final TreeBean typeTree = new TreeBean(new TreeDescriptor(this.typeField, this.typeLabel));
             computeSearchTree(rb.getResults().docList, reader, this.typeField, typeTree);
 
             final TreeBean termTree = new TreeBean(new TreeDescriptor(this.termField, this.termLabel));
             computeSearchTree(rb.getResults().docList, reader, this.termField, termTree);
 
             logger.debug("Removing Documents from SOLR response");
             NamedList response = rb.rsp.getValues();
             int idx = response.indexOf("response", 0);
             if (idx > 0)
                 response.remove(idx);
 
 
             logger.debug("Adding tree to SOLR Response");
             rb.rsp.add("tree", new ArrayList() {{
                 add(typeTree);
                 add(termTree);
             }}
             );
         }
     }
 
     private TreeBean computeSearchTree(DocList docs, IndexReader reader, String fieldName, TreeBean _tree) throws IOException {
 
 
         DocIterator iterator = docs.iterator();
 
         while (iterator.hasNext()) {
             int id = iterator.nextDoc();
 
             Field[] _fieldArr = reader.document(id).getFields(fieldName);
             if (_fieldArr != null) {
 
                 for (Field _field : _fieldArr) {
                // get by label
                    String _tNodeLabel = treeLabelMap.containsKey(_field.stringValue()) ? treeLabelMap.get(_field.stringValue()) : _field.stringValue();
 
 
                    TreeNode node = _tree.getByLabel(_tNodeLabel);
                     if (node == null) {
                         // new node
                        node = new TreeNode(_field.stringValue(), _tNodeLabel);
                         _tree.addNode(node);
                     }
                     node.setCount(node.getCount() + 1);
 
                 }
 
             } else {
                 logger.debug("Field " + fieldName + " not found in document. Will not build browse tree for the field");
             }
         }
 
         return _tree;
     }
 
 
     public String getDescription() {
         return "Portal Tree";
     }
 
     public String getSourceId() {
         return "$Revision: 1 $";
     }
 
     public String getSource() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getVersion() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
