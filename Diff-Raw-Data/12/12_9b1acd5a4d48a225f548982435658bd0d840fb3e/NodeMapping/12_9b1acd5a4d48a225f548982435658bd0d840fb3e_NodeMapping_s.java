 /*
  * Copyright 2011, 2012 Delving BV
  *
  *  Licensed under the EUPL, Version 1.0 or? as soon they
  *  will be approved by the European Commission - subsequent
  *  versions of the EUPL (the "Licence");
  *  you may not use this work except in compliance with the
  *  Licence.
  *  You may obtain a copy of the Licence at:
  *
  *  http://ec.europa.eu/idabc/eupl
  *
  *  Unless required by applicable law or agreed to in
  *  writing, software distributed under the Licence is
  *  distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  *  express or implied.
  *  See the Licence for the specific language governing
  *  permissions and limitations under the Licence.
  */
 
 package eu.delving.metadata;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 import java.util.*;
 
 import static eu.delving.metadata.NodeMappingChange.CODE;
 import static eu.delving.metadata.NodeMappingChange.DOCUMENTATION;
 import static eu.delving.metadata.StringUtil.*;
 
 /**
  * This class describes how one node is transformed into another, which is part of mapping
  * one hierarchy onto another.  It can contain a dictionary, as well as a snippet
  * of Groovy code.
  * <p/>
  * Instances of this class are placed in the RecDefNode elements of the record definition
  * so that that data structure can be used as a scaffolding to recursively write the code
  * for the Groovy builder.
  * <p/>
  * Instances are also stored in a list in the RecMapping, and upon reading a mapping they
  * are distributed into the local prototype instance of the record definition data structure.
  *
  * @author Gerald de Jong <gerald@delving.eu>
  */
 
 @XStreamAlias("node-mapping")
 public class NodeMapping {
 
     @XStreamAsAttribute
     public Path inputPath;
 
     @XStreamAsAttribute
     public Path outputPath;
 
     public List<Path> siblings;
 
     @XStreamAsAttribute
     public Operator operator;
 
     public Map<String, String> dictionary;
 
     @XStreamAlias("groovy-code")
     public List<String> groovyCode;
 
     public List<String> documentation;
 
     @XStreamOmitField
     public RecDefNode recDefNode;
 
     @XStreamOmitField
     public CodeOut codeOut;
 
     @XStreamOmitField
     private SortedSet sourceTreeNodes;
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         NodeMapping that = (NodeMapping) o;
         if (inputPath != null ? !inputPath.equals(that.inputPath) : that.inputPath != null) return false;
         if (outputPath != null ? !outputPath.equals(that.outputPath) : that.outputPath != null) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         return inputPath.hashCode();
     }
 
     public String getDocumentation() {
         return linesToString(documentation);
     }
 
     public void setDocumentation(String documentation) {
         this.documentation = stringToLines(documentation);
         notifyChanged(DOCUMENTATION);
     }
 
     public Operator getOperator() {
         if (recDefNode.hasOperator()) return recDefNode.getOperator();
         return operator == null ? Operator.ALL : operator;
     }
 
     public void clearStatsTreeNodes() {
         sourceTreeNodes = null;
     }
 
     public boolean hasMap() {
         return siblings != null;
     }
 
     public boolean hasSourceTreeNodes() {
         return sourceTreeNodes != null;
     }
 
     public boolean hasOneSourceTreeNode() {
         return hasSourceTreeNodes() && sourceTreeNodes.size() == 1;
     }
 
     public Object getSingleSourceTreeNode() {
         Iterator walk = sourceTreeNodes.iterator();
         return walk.hasNext() ? walk.next() : null;
     }
 
     public SortedSet getSourceTreeNodes() {
         return sourceTreeNodes;
     }
 
     public void attachTo(RecDefNode recDefNode) {
         this.recDefNode = recDefNode;
         this.outputPath = recDefNode.getPath();
     }
 
     // this method should be called from exactly ONE place!
     public NodeMapping setStatsTreeNodes(SortedSet statsTreeNodes, List<Path> inputPaths) {
         if (statsTreeNodes.isEmpty()) throw new RuntimeException();
         this.sourceTreeNodes = statsTreeNodes;
         setInputPaths(inputPaths);
         return this;
     }
 
     public NodeMapping setInputPath(Path inputPath) {
         this.inputPath = inputPath;
         return this;
     }
 
     public void notifyChanged(NodeMappingChange change) {
         if (recDefNode != null) recDefNode.notifyNodeMappingChange(this, change);
     }
 
     public NodeMapping setInputPaths(Collection<Path> inputPaths) {
         if (inputPaths.isEmpty()) throw new RuntimeException();
         Path parent = null;
         for (Path input : inputPaths) {
             if (parent == null) {
                 parent = input.parent();
             }
             else if (!parent.equals(input.parent())) {
                 throw new RuntimeException(String.format("Input path %s should all be from the same parent %s", input, parent));
             }
         }
         Iterator<Path> pathWalk = inputPaths.iterator();
         this.inputPath = pathWalk.next();
         if (pathWalk.hasNext()) {
             siblings = new ArrayList<Path>();
             while (pathWalk.hasNext()) siblings.add(pathWalk.next());
         }
         return this;
     }
 
     public List<Path> getInputPaths() {
         List<Path> inputPaths = new ArrayList<Path>();
         inputPaths.add(inputPath);
         if (siblings != null) inputPaths.addAll(siblings);
         Collections.sort(inputPaths);
         return inputPaths;
     }
 
     public NodeMapping setOutputPath(Path outputPath) {
         this.outputPath = outputPath;
         return this;
     }
 
     public boolean generatedCodeLooksLike(String codeString, RecMapping recMapping) {
         if (codeString == null) return false;
         List<String> list = Arrays.asList(getCode(getGeneratorEditPath(), recMapping).split("\n"));
         Iterator<String> walk = list.iterator();
         return isSimilar(codeString, walk);
     }
 
     public boolean codeLooksLike(String codeString) {
         return groovyCode == null || isSimilar(codeString, groovyCode.iterator());
     }
 
     public String getCode(EditPath editPath, RecMapping recMapping) {
         recMapping.toCode(editPath);
         return codeOut.toString();
     }
 
     public void revertToGenerated() {
         setGroovyCode(null, null);
     }
 
     public void setGroovyCode(String codeString, RecMapping recMapping) {
         if (codeString == null || generatedCodeLooksLike(codeString, recMapping)) {
             if (groovyCode != null) {
                 groovyCode = null;
                 notifyChanged(CODE);
             }
         }
         else if (groovyCode == null || !codeLooksLike(codeString)) {
             groovyCode = stringToLines(codeString);
             notifyChanged(CODE);
         }
     }
 
     public void toAttributeCode(Stack<String> groovyParams, EditPath editPath) {
         if (!recDefNode.isAttr()) return;
         toUserCode(groovyParams, editPath);
     }
 
     public void toLeafElementCode(Stack<String> groovyParams, EditPath editPath) {
         if (recDefNode.isAttr() || !recDefNode.isLeafElem()) return;
         toUserCode(groovyParams, editPath);
     }
 
     public boolean isUserCodeEditable() {
         return recDefNode.isAttr() || recDefNode.isLeafElem();
     }
 
     private boolean isSimilar(String codeString, Iterator<String> walk) {
         for (String line : codeString.split("\n")) {
             line = line.trim();
             if (line.isEmpty()) continue;
             if (!walk.hasNext()) return false;
             while (walk.hasNext()) {
                 String otherLine = walk.next().trim();
                 if (otherLine.isEmpty()) continue;
                 if (!otherLine.equals(line)) return false;
                 break;
             }
         }
         return !walk.hasNext();
     }
 
     private void toUserCode(Stack<String> groovyParams, EditPath editPath) {
         if (editPath != null) {
             String editedCode = editPath.getEditedCode(recDefNode.getPath());
             if (editedCode != null) {
                 indentCode(editedCode, codeOut);
                 return;
             }
             else if (groovyCode != null) {
                 indentCode(groovyCode, codeOut);
                 return;
             }
         }
         else if (groovyCode != null) {
             indentCode(groovyCode, codeOut);
             return;
         }
         toInnerLoop(getLocalPath(), groovyParams);
     }
 
     private void toInnerLoop(Path path, Stack<String> groovyParams) {
         if (path.isEmpty()) throw new RuntimeException();
         if (path.size() == 1) {
             if (dictionary != null) {
                 codeOut.line("from%s(%s)", toDictionaryName(this), toLeafGroovyParam(path));
             }
             else if (hasMap()) {
                 codeOut.line(getMapUsage());
             }
             else {
                 if (path.peek().getLocalName().equals("constant")) {
                     codeOut.line("'CONSTANT'");
                 }
                 else if (recDefNode.hasFunction()) {
                    if (recDefNode.getFieldType().equalsIgnoreCase("link")) {
                        codeOut.line("\"${%s(%s).sanitizeURI()}\"", recDefNode.getFunction(), toLeafGroovyParam(path));
                    }
                    else {
                        codeOut.line("\"${%s(%s)}\"", recDefNode.getFunction(), toLeafGroovyParam(path));
                    }
                 }
                 else {
                    codeOut.line("\"${%s}\"", toLeafGroovyParam(path));
                 }
             }
         }
         else if (recDefNode.isLeafElem()) {
             toInnerLoop(path.withRootRemoved(), groovyParams);
         }
         else {
             boolean needLoop;
             if (hasMap()) {
                 needLoop = !groovyParams.contains(getMapName());
                 if (needLoop) {
                     codeOut.line_(
                             "%s %s { %s ->",
                             toMapExpression(this), getOperator().getChar(), getMapName()
                     );
                 }
             }
             else {
                 String param = toLoopGroovyParam(path);
                 needLoop = !groovyParams.contains(param);
                 if (needLoop) {
                     codeOut.line_(
                             "%s %s { %s ->",
                             toLoopRef(path), getOperator().getChar(), param
                     );
                 }
             }
             toInnerLoop(path.withRootRemoved(), groovyParams);
             if (needLoop) codeOut._line("}");
         }
     }
 
     public Path getLocalPath() {
         NodeMapping ancestor = getAncestorNodeMapping(inputPath);
         if (ancestor.inputPath.isAncestorOf(inputPath)) {
             return inputPath.extendAncestor(ancestor.inputPath);
         }
         else {
             return inputPath;
         }
     }
 
     private String getMapUsage() {
         if (!hasMap()) return null;
         StringBuilder usage = new StringBuilder("\"");
         Iterator<Path> walk = getInputPaths().iterator();
         while (walk.hasNext()) {
             Path path = walk.next();
             usage.append(String.format("${%s['%s']}", getMapName(), path.peek().toMapKey()));
             if (walk.hasNext()) usage.append(" ");
         }
         usage.append("\"");
         return usage.toString();
     }
 
     public String getMapName() {
         return String.format("_M%d", inputPath.size());
     }
 
     public String toString() {
         if (recDefNode == null) return "No RecDefNode";
         String input = inputPath.getTail();
         if (hasMap()) {
             StringBuilder out = new StringBuilder();
             Iterator<Path> walk = getInputPaths().iterator();
             while (walk.hasNext()) {
                 out.append(walk.next().getTail());
                 if (walk.hasNext()) out.append(", ");
             }
             input = out.toString();
         }
         String wrap = groovyCode == null ? "p" : "b";
         return String.format("<html><%s>%s &rarr; %s</%s>", wrap, input, recDefNode.toString(), wrap);
     }
 
     private NodeMapping getAncestorNodeMapping(Path path) {
         for (RecDefNode ancestor = recDefNode.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
             for (NodeMapping nodeMapping : ancestor.getNodeMappings().values()) {
                 if (nodeMapping.inputPath.isAncestorOf(path)) return nodeMapping;
             }
         }
         return new NodeMapping().setInputPath(Path.create("input")).setOutputPath(outputPath.takeFirst());
     }
 
     private EditPath getGeneratorEditPath() {
         return new EditPath() {
 
             @Override
             public NodeMapping getNodeMapping() {
                 return NodeMapping.this;
             }
 
             @Override
             public String getEditedCode(Path path) {
                 return null;
             }
         };
     }
 }
 
