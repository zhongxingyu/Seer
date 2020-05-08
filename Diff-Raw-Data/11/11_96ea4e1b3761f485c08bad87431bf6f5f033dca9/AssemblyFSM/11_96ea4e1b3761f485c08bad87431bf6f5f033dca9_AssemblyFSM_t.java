 package de.tuberlin.dima.presslufthammer.data;
 
 import java.io.IOException;
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 import de.tuberlin.dima.presslufthammer.data.columnar.ColumnReader;
 import de.tuberlin.dima.presslufthammer.data.columnar.Tablet;
 import de.tuberlin.dima.presslufthammer.data.hierarchical.RecordEncoder;
 import de.tuberlin.dima.presslufthammer.data.hierarchical.RecordStore;
 
 /**
  * An object that represents the FSM that is used to assemble records from the
  * columnar storage. The FSM is constructed from a given {@link SchemaNode} and
  * the records can be assembled from a {@link Tablet} to a {@link RecordStore}.
  * 
 * The algorithm is more or less taken from the dremel paper but a few additions
 * were necessary to handle corner cases. The algorithm does not work if taken
 * one-to-one from the paper.
 * 
  * @author Aljoscha Krettek
  * 
  */
 public class AssemblyFSM {
     private List<AssemblyFSMNode> nodes;
     private SchemaNode schema;
 
     /**
      * Constructs the FSM from the given schema.
      */
     public AssemblyFSM(SchemaNode schema) {
         nodes = Lists.newLinkedList();
         this.schema = schema;
 
         constructFSM(schema);
     }
 
     /**
      * Assembles data from the tablet to the record store using the assembly
      * FSM.
      */
     public void assembleRecords(Tablet source, RecordStore target)
             throws IOException {
         initializeReaders(source);
 
         AssemblyFSMNode startNode = nodes.get(0);
         ColumnReader reader = startNode.getReader();
         while (reader.hasNext()) {
             assembleRecord(target);
         }
     }
 
     /**
      * Adjusts the structure of the record being assembled. This corresponds to
      * the "return to level" and "move to level" methods from the Dremel paper.
      * It is not as simple as shown in the paper, however, we also have to the
      * isFirstChild methods here...
      */
     private void adjustRecordStructure(ColumnReader reader,
             RecordEncoder record, SchemaNode currentSchema,
             SchemaNode lastSchema) throws IOException {
         // "moveTo"
         SchemaNode commonAncestor = commonAncestor(currentSchema, lastSchema);
 
         record.returnToLevel(commonAncestor);
        if (currentSchema.isFirstField() && !currentSchema.isRepeated() && commonAncestor.hasParent()) {
             record.returnToLevel(commonAncestor.getParent());
         }
 
         if (reader.getNextDefinition() >= currentSchema.getMaxDefinition()) {
             record.moveToLevel(currentSchema.getParent());
         } else {
             // just move to the correct level for the definition level of the
             // reader
             SchemaNode correctSchema = currentSchema
                     .getSchemaForDefinitionLevel(reader.getNextDefinition());
             record.moveToLevel(correctSchema);
         }
     }
 
     /**
      * Assembles a single record to the {@link RecordStore}.
      */
     private void assembleRecord(RecordStore target) throws IOException {
         RecordEncoder record = target.startRecord();
         AssemblyFSMNode lastNode = null;
         SchemaNode lastSchema = schema;
         AssemblyFSMNode currentNode = nodes.get(0);
         SchemaNode currentSchema = currentNode.getSchema();
         ColumnReader reader = currentNode.getReader();
 
         // System.out.println("START OF ASSEMBLE -----------------------");
         while (reader != null && reader.hasNext()) {
             // System.out.println("READER: " +
             // currentSchema.getQualifiedName());
             adjustRecordStructure(reader, record, currentSchema, lastSchema);
             // System.out.println(record);
             Object value = reader.getNextValue();
             if (value != null) {
                 // System.out.println("APPENDING VALUE: " + value);
                 record.appendValue(currentSchema, value);
                 // System.out.println(record);
             }
             lastNode = currentNode;
             lastSchema = lastNode.getSchema();
             // System.out.println("NEXT REP: " + reader.getNextRepetition());
             currentNode = currentNode.getTransition(reader.getNextRepetition());
             // System.out.println(currentNode.toString());
             reader = currentNode.getReader();
             currentSchema = currentNode.getSchema();
             if (currentSchema != null) {
                 record.returnToLevel(currentSchema.getParent());
             }
         }
         // if (reader == null) {
         // System.out.println("READER IS NULL");
         // }
         record.returnToLevel(schema);
         record.finalizeRecord();
     }
 
     /**
      * Gets the readers from the source tablet and stores them in the
      * corresponding nodes of the assembly FSM.
      */
     private void initializeReaders(Tablet tablet) {
         for (AssemblyFSMNode node : nodes) {
             if (!node.isFinal()) {
                 node.setReader(tablet.getColumnReader(node.getSchema()));
             }
         }
     }
 
     /**
      * Constructs the assembly FSM from the given schema, this is a pretty
      * straightforward implementation of the algorithm given in the paper.
      */
     private void constructFSM(SchemaNode schema) {
         List<SchemaNode> fields = constructFieldList(schema);
         for (int i = 0; i < fields.size(); ++i) {
             SchemaNode field = fields.get(i);
             nodes.add(new AssemblyFSMNode(field));
         }
         nodes.add(AssemblyFSMNode.getFinalState());
 
         for (int i = 0; i < fields.size(); ++i) {
             SchemaNode field = fields.get(i);
             AssemblyFSMNode fsmNode = nodes.get(i);
             int maxLevel = field.getRepetition();
             AssemblyFSMNode barrier = nodes.get(i + 1);
             int barrierLevel;
             if (barrier.isFinal()) {
                 barrierLevel = 0;
             } else {
                 barrierLevel = commonRepetitionLevel(field, barrier.getSchema());
             }
 
             for (int j = i; j >= 0; --j) {
                 SchemaNode preField = fields.get(j);
                 if (preField.getRepetition() <= barrierLevel) {
                     continue;
                 }
                 int backLevel = commonRepetitionLevel(field, preField);
                 fsmNode.setTransition(backLevel, nodes.get(j));
                 // this seems necessary for some corner cases, not in the
                 // algorithm from the dremel paper though
                 for (int level = backLevel; level >= 0; --level) {
                     fsmNode.setTransition(level, nodes.get(j));
                 }
             }
 
             for (int level = 0; level <= barrierLevel; ++level) {
                 fsmNode.setTransition(level, barrier);
             }
 
             for (int level = barrierLevel + 1; level <= maxLevel; ++level) {
                 if (!fsmNode.hasTransition(level)) {
                     if (fsmNode.hasTransition(level - 1)) {
                         fsmNode.setTransition(level,
                                 fsmNode.getTransition(level - 1));
                     }
                 }
             }
         }
     }
 
     /**
      * Returns the common ancestor {@link SchemaNode} of two {@link SchemaNode}S
      */
     private SchemaNode commonAncestor(SchemaNode field1, SchemaNode field2) {
         SchemaNode parent1 = field1;
         SchemaNode parent2 = field2;
 
         if (field1.equals(field2)) {
             return field1.getParent();
         }
 
         while (parent1 != null && parent2 != null && !parent1.equals(parent2)) {
             if (parent1.getFullDefinition() >= parent2.getFullDefinition()) {
                 parent1 = parent1.getParent();
             } else {
                 parent2 = parent2.getParent();
             }
         }
 
         if (parent1 != null && parent2 != null && parent1.equals(parent2)) {
             return parent1;
         } else {
             return null;
         }
     }
 
     /**
      * Returns the common repetition level {@link SchemaNode} of two
      * {@link SchemaNode}S. This is used by the FSM construction algorithm.
      */
     private int commonRepetitionLevel(SchemaNode field1, SchemaNode field2) {
         // this one is easy ... :D
         if (field1.equals(field2)) {
             return field1.getRepetition();
         }
 
         SchemaNode commonAncestor = commonAncestor(field1, field2);
         if (commonAncestor == null) {
             return 0;
         } else {
             return commonAncestor.getRepetition();
         }
     }
 
     /**
      * Returns the list of {@link SchemaNode}S that are primitive fields in the
      * given schema.
      */
     private List<SchemaNode> constructFieldList(SchemaNode schema) {
         if (schema.isRecord()) {
             List<SchemaNode> result = Lists.newLinkedList();
             for (SchemaNode childSchema : schema.getFieldList()) {
                 result.addAll(constructFieldList(childSchema));
             }
             return result;
         } else {
             List<SchemaNode> result = Lists.newArrayList(schema);
             return result;
         }
     }
 
     /**
      * For debugging...
      */
     @Override
     public String toString() {
         StringBuilder result = new StringBuilder();
         for (AssemblyFSMNode node : nodes) {
             result.append(node.toString());
         }
         return result.toString();
     }
 }
