 package org.jikespg.uide.compare;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.swt.graphics.Image;
 import org.jikespg.uide.IJikesPGResources;
 import org.jikespg.uide.JikesPGPlugin;
 import org.jikespg.uide.parser.JikesPGParser.*;
 
 public class JikesPGStructureNode extends DocumentRangeNode implements ITypedElement {
     public final static int OPTION= 0;
     public final static int BODY= 1;
     public static final int ACTION= 2;
     public static final int TERMINAL= 3;
     public static final int RULE= 4;
     public static final int GLOBAL= 5;
     public static final int HEADER= 6;
     public static final int DEFINE= 7;
     public static final int IMPORT= 8;
     public static final int INCLUDE= 9;
     public static final int ALIAS= 10;
     public static final int NONTERMINAL= 11;
 
     private static final char OPTION_PREFIX= 'O';
     private static final char BODY_PREFIX= 'B';
     private static final char ACTION_PREFIX= 'A';
     private static final char TERMINAL_PREFIX= 'T';
     private static final char RULE_PREFIX= 'R';
     private static final char GLOBAL_PREFIX= 'G';
     private static final char HEADER_PREFIX= 'H';
     private static final char DEFINE_PREFIX= 'D';
     private static final char IMPORT_PREFIX= 'M';
     private static final char INCLUDE_PREFIX= 'I';
     private static final char ALIAS_PREFIX= 'L';
     private static final char NONTERMINAL_PREFIX= 'N';
 
     private ASTNode fASTNode;
 
     public JikesPGStructureNode(ASTNode root, IDocument document, int typeCode, String name) {
        super(typeCode, buildIDFor(typeCode, name), document, offsetOf(root), lengthOf(root, document));
         fASTNode= root;
     }
 
     public JikesPGStructureNode(ASTNode node, JikesPGStructureNode parent, int typeCode, String name) {
	super(typeCode, buildIDFor(typeCode, name), parent.getDocument(), offsetOf(node), lengthOf(node, parent.getDocument()));
         fASTNode= node;
     }
 
    private static int offsetOf(ASTNode n) { return (n != null) ? n.getLeftIToken().getStartOffset() : 0; }
    private static int lengthOf(ASTNode n, IDocument doc) { return (n != null) ? n.getRightIToken().getEndOffset() - n.getLeftIToken().getStartOffset() + 1 : doc.getLength(); }
 
     private static String buildIDFor(int typeCode, String name) {
         switch(typeCode) {
         case OPTION: return OPTION_PREFIX + name;
         case BODY: return BODY_PREFIX + name;
         case ACTION: return ACTION_PREFIX + name;
         case TERMINAL: return TERMINAL_PREFIX + name;
         case RULE: return RULE_PREFIX + name;
         case GLOBAL: return GLOBAL_PREFIX + name;
         case HEADER: return HEADER_PREFIX + name;
         case DEFINE: return DEFINE_PREFIX + name;
         case IMPORT: return IMPORT_PREFIX + name;
         case INCLUDE: return INCLUDE_PREFIX + name;
         case ALIAS: return ALIAS_PREFIX + name;
         case NONTERMINAL: return NONTERMINAL_PREFIX + name;
         }
         return "???";
     }
 
     public ASTNode getASTNode() {
         return fASTNode;
     }
 
     public String getName() {
 	return ASTLabelProvider.getLabelFor(fASTNode);
     }
 
     public Image getImage() {
 	return ASTLabelProvider.getImageFor(fASTNode);
     }
 
     public String getType() {
 	return "jikespg";
     }
 
     public Object[] getChildren() {
	if (fASTNode == null) return new Object[0];

	GetChildrenVisitor v= new GetChildrenVisitor(this);
 
         fASTNode.accept(v);
         return v.getChildren();
     }
 
     //
     // N.B.: Don't re-implement hashCode() and equals()!!!
     // The diff computation relies on the typeCode/ID being sufficient to identify a node.
     //
 
 //    public int hashCode() {
 //        return 131 + fASTNode.hashCode() * 7;
 //    }
 //
 //    public boolean equals(Object obj) {
 //        if (!(obj instanceof JikesPGStructureNode))
 //            return false;
 //
 //        JikesPGStructureNode other= (JikesPGStructureNode) obj;
 //
 //        return this.fASTNode.equals(other.fASTNode);
 //    }
 
     public String toString() {
         return fASTNode.toString();
     }
 }
 
 class ASTLabelProvider implements ILabelProvider {
     private Set fListeners= new HashSet();
 
     private static ImageRegistry sImageRegistry= JikesPGPlugin.getInstance().getImageRegistry();
 
     private static Image DEFAULT_IMAGE= sImageRegistry.get(IJikesPGResources.DEFAULT_AST);
 
     public Image getImage(Object element) {
         ASTNode n= (ASTNode) element;
 
         return getImageFor(n);
     }
 
     public static Image getImageFor(ASTNode n) {
         return DEFAULT_IMAGE;
     }
 
     public String getText(Object element) {
         ASTNode n= (ASTNode) element;
 
         return getLabelFor(n);
     }
 
     public static String getLabelFor(ASTNode n) {
	if (n == null) return "<grammar file>"; // seems this only happens when the entire grammar can't be parsed
         if (n instanceof JikesPG) return "grammar";
         if (n instanceof options_segment) return "options";
         if (n instanceof AliasSeg) return "aliases";
         if (n instanceof DefineSeg) return "defines";
         if (n instanceof GlobalsSeg) return "globals";
         if (n instanceof HeadersSeg) return "headers";
         if (n instanceof ImportSeg) return "imports";
         if (n instanceof IncludeSeg) return "includes";
         if (n instanceof RulesSeg) return "rules";
         if (n instanceof TerminalsSeg) return "terminals";
 
         if (n instanceof define_segment1) return /*"macro " +*/ ((define_segment1) n).getmacro_name_symbol().toString();
         if (n instanceof nonTerm) return /*"non-terminal " +*/ ((nonTerm) n).getSYMBOL().toString();
         if (n instanceof terminal) return /*"terminal " +*/ ((terminal) n).getterminal_symbol().toString();
 
         return "<???>";
     }
 
     public void addListener(ILabelProviderListener listener) {
         fListeners.add(listener);
     }
 
     public void dispose() { }
 
     public boolean isLabelProperty(Object element, String property) {
         return false;
     }
 
     public void removeListener(ILabelProviderListener listener) {
         fListeners.remove(listener);
     }
 }
