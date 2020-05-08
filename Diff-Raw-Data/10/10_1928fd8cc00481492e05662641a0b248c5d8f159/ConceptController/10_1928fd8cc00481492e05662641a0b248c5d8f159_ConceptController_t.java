 package dk.sst.snomedcave.controllers;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import dk.sst.snomedcave.dao.ConceptRelationRepository;
 import dk.sst.snomedcave.dao.ConceptRepository;
 import dk.sst.snomedcave.model.Concept;
 import dk.sst.snomedcave.model.ConceptRelation;
 import org.apache.commons.collections15.CollectionUtils;
 import org.apache.commons.collections15.Predicate;
 import org.apache.commons.collections15.Transformer;
 import org.apache.log4j.Logger;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.graphdb.traversal.Traverser;
 import org.neo4j.kernel.Traversal;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.inject.Inject;
 import java.util.*;
 
 import static java.util.Collections.reverse;
 import static java.util.Collections.sort;
 import static org.apache.commons.collections15.CollectionUtils.collect;
 import static org.apache.commons.collections15.CollectionUtils.filter;
 import static org.neo4j.graphdb.DynamicRelationshipType.withName;
 import static org.neo4j.graphdb.traversal.Evaluators.returnWhereEndNodeIs;
 
 @Controller
 @RequestMapping("/concepts/")
 public class ConceptController {
     private static Logger logger = Logger.getLogger(ConceptController.class);
     private static final Comparator<ConceptNode> CONCEPTNAME_COMPARATOR = new Comparator<ConceptNode>() {
         @Override
         public int compare(ConceptNode o1, ConceptNode o2) {
             return o1.name.compareTo(o2.name);
         }
     };
     @Inject
     ConceptRepository conceptRepository;
 
     @Inject
     ConceptRelationRepository conceptRelationRepository;
 
     @Inject
     Neo4jTemplate neo4jTemplate;
 
     @Inject
     WebUtils webUtils;
 
     private Concept isAType;
 
     final TraversalDescription td = Traversal.description()
             .breadthFirst()
             .relationships(withName("childs"), Direction.OUTGOING)
             .relationships(withName("child"), Direction.OUTGOING);
 
     private Concept getIsA() {
         if (isAType == null) {
             isAType = conceptRepository.getByConceptId("116680003");
         }
         return isAType;
     }
 
    private ConceptNode toConceptNode(Concept concept) {
        return new ConceptNode(
                concept.getConceptId(),
                concept.getTerm(),
                false);
    }

     @RequestMapping(value = "search", produces = "application/json;charset=utf-8")
     public ResponseEntity<String> search(@RequestParam("query") String query) {
         Concept concept = conceptRepository.getByFullyspecifiedName(query);
 
         logger.info("query=\"" + query + "\" concept=" + concept);
         if (concept == null) {
             return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
         }
         return new ResponseEntity<String>(webUtils.toJson(concept), HttpStatus.OK);
     }
 
     @RequestMapping(value = "node", produces = "application/json;charset=utf-8")
     public ResponseEntity<String> nodeJson(@RequestParam("id") String conceptId) {
         Gson gson = new GsonBuilder().create();
         final Concept concept = conceptRepository.getByConceptId(conceptId);
         final Set<ConceptRelation> childs = new HashSet<ConceptRelation>(concept.getChilds());
         filter(childs, new Predicate<ConceptRelation>() {
             @Override
             public boolean evaluate(ConceptRelation relation) {
                 return shouldInclude(relation);
             }
         });
         return new ResponseEntity<String>(gson.toJson(
                 new ConceptNode(
                         concept.getConceptId(),
                         concept.getTerm(),
                         collect(childs, new Transformer<ConceptRelation, ConceptNode>() {
                             @Override
                             public ConceptNode transform(ConceptRelation relation) {
                                 Concept child = get(get(relation).getChild());
                                 return new ConceptNode(child.getConceptId(), child.getTerm(), childs.size() > 0);
                             }
                         })))
                 , HttpStatus.OK);
     }
 
     @RequestMapping(value = "tree", produces = "application/json;charset=utf-8")
     public ResponseEntity<String> treeJson(@RequestParam("id") String conceptId) {
         Gson gson = new GsonBuilder().create();
         return new ResponseEntity<String>(gson.toJson(conceptTree(conceptId).getRoot()), HttpStatus.OK);
     }
 
     public TreeResponse conceptTree(final String conceptId) {
         Concept target = conceptRepository.getByConceptId(conceptId);
         if (target == null) {
             logger.debug("Did not find any concepts with conceptId=" + conceptId);
            return new TreeResponse(null);
         }
         List<Concept> levels = getThreeLevelsUpFrom(target);
         reverse(levels);
         final Set<ConceptRelation> rootChilds = new HashSet<ConceptRelation>(target.getChilds());
         filter(rootChilds, new Predicate<ConceptRelation>() {
             @Override
             public boolean evaluate(ConceptRelation relation) {
                 return shouldInclude(relation);
             }
         });
         ConceptNode root = new ConceptNode(
                 target.getConceptId(),
                 target.getTerm(),
                 collect(rootChilds, new Transformer<ConceptRelation, ConceptNode>() {
                     @Override
                     public ConceptNode transform(ConceptRelation relation) {
                         final Concept concept = get(get(relation).getChild());
                         return new ConceptNode(concept.getConceptId(), concept.getTerm(), rootChilds.size() > 0);
                     }
                 }));
 
         for (int i = 1; i <= 3; i++) {
             if (i >= levels.size()) {
                 break;
             }
             Concept concept = levels.get(i);
             root = new ConceptNode(concept.getConceptId(), concept.getTerm(), root);
             if (i == 1) { //First parent
                 final Set<ConceptRelation> childs = new HashSet<ConceptRelation>(concept.getChilds());
                 filter(childs, new Predicate<ConceptRelation>() {
                     @Override
                     public boolean evaluate(ConceptRelation relation) {
                         return shouldInclude(relation) && !get(get(relation).getChild()).getConceptId().equals(conceptId);
                     }
                 });
                 for (ConceptRelation relation : childs) {
                     Concept child = get(get(relation).getChild());
                     if (!child.getConceptId().equals(root.conceptId)) {
                         root.childs.add(new ConceptNode(child.getConceptId(), child.getTerm(), child.getChilds().size() > 0));
                     }
                 }
             }
             sort(root.childs, CONCEPTNAME_COMPARATOR);
         }
 
         return new TreeResponse(root);
     }
 
     private boolean shouldInclude(ConceptRelation relation) {
         final Concept concept = get(get(relation).getChild());
         final Concept type = get(relation.getType());
         return concept.getStatus() == 0 && type.getNodeId().equals(getIsA().getNodeId());
     }
 
     private List<Concept> getThreeLevelsUpFrom(Concept target) {
         Node targetNode = neo4jTemplate.getNode(target.getNodeId());
         Concept drugAllergy = conceptRepository.getByConceptId("416098002");
         Node drugAllergyNode = neo4jTemplate.getNode(drugAllergy.getNodeId());
 
         final Traverser paths = td.evaluator(returnWhereEndNodeIs(targetNode)).traverse(drugAllergyNode);
 
         final Iterator<Path> iterator = paths.iterator();
         List<Concept> result = new ArrayList<Concept>();
         if (iterator.hasNext()) {
             final Path path = iterator.next();
             for (PropertyContainer container : path) {
                 if (container instanceof Node) {
                     Node node = (Node) container;
                     if (node.getProperty("__type__").equals(Concept.class.getCanonicalName())) {
                         result.add(conceptRepository.findOne(node.getId()));
                     }
                 }
             }
         }
         return result;
     }
 
     private ConceptRelation get(ConceptRelation relation) {
         return conceptRelationRepository.findOne(relation.getNodeId());
     }
 
     private Concept get(Concept concept) {
         return conceptRepository.findOne(concept.getNodeId());
     }
 }
