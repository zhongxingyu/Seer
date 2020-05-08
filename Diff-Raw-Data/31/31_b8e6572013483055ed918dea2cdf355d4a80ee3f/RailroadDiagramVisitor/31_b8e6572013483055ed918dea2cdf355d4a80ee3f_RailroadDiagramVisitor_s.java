 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.ast.visitor;
 
 import de.weltraumschaf.jebnf.ast.Node;
 import de.weltraumschaf.jebnf.ast.Visitable;
 import de.weltraumschaf.jebnf.ast.nodes.IdentifierNode;
import de.weltraumschaf.jebnf.ast.nodes.NullNode;
 import de.weltraumschaf.jebnf.ast.nodes.RuleNode;
 import de.weltraumschaf.jebnf.ast.nodes.SyntaxNode;
 import de.weltraumschaf.jebnf.ast.nodes.TerminalNode;
 import de.weltraumschaf.jebnf.gfx.RailroadDiagram;
 import de.weltraumschaf.jebnf.gfx.shapes.Shape;
 import static de.weltraumschaf.jebnf.gfx.shapes.ShapeFactory.*;
 import de.weltraumschaf.jebnf.gfx.shapes.compound.ChoiceShape;
 import de.weltraumschaf.jebnf.gfx.shapes.compound.LoopShape;
 import de.weltraumschaf.jebnf.gfx.shapes.compound.OptionShape;
 import de.weltraumschaf.jebnf.gfx.shapes.compound.RowLayoutShape;
 import de.weltraumschaf.jebnf.gfx.shapes.compound.SequenceShape;
import de.weltraumschaf.jebnf.gfx.shapes.text.RuleShape;
 import java.util.Stack;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Visits the given {@link de.weltraumschaf.jebnf.ast.nodes.SyntaxNode} and generates
  * a {@link de.weltraumschaf.jebnf.gfx.RailroadDiagram} as result.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class RailroadDiagramVisitor implements Visitor<RailroadDiagram> {
 
     /**
      * Logging facility.
      */
     private static final Logger LOGGER = Logger.getLogger(RailroadDiagramVisitor.class.getName());
 
     /**
      * Holds the resulting diagram.
      */
     private RailroadDiagram diagram;
 
     /**
      * Stack of shapes currently created in {@link #visit(de.weltraumschaf.jebnf.ast.Visitable)}.
      */
     private final Stack<Shape> currentShapeStack = new Stack<Shape>();
 
     @Override
     public void beforeVisit(final Visitable visitable) {
         // Nothing to do here.
     }
 
     @Override
     public void visit(final Visitable visitable) {
         final Node node      = (Node) visitable;
         Shape shape          = empty();
 
         switch (node.getType()) {
             case SYNTAX: {
                 // Reset everything because it's the root node.
                 currentShapeStack.removeAllElements();
                 diagram = new RailroadDiagram();
                 diagram.setTitle(node.getAttribute(SyntaxNode.ATTRIBUTE_TITLE));
                 diagram.setMeta(node.getAttribute(SyntaxNode.ATTRIBUTE_META));
                 break;
             }
             case RULE: {
                 shape = rule(node.getAttribute(RuleNode.ATTRIBUTE_NAME));
                 diagram.add(row(shape));
                 shape = row(start());
                 diagram.add(shape);
                 break;
             }
             case CHOICE: {
                 shape = choice();
                 addShapeToCurrentShape(shape);
                 break;
             }
             case LOOP: {
                 shape = loop();
                 addShapeToCurrentShape(shape);
                 break;
             }
             case OPTION: {
                 shape = option();
                 addShapeToCurrentShape(shape);
                 break;
             }
             case SEQUENCE: {
                 shape = row();
                 addShapeToCurrentShape(shape);
                 break;
             }
             case IDENTIFIER: {
                 shape = identifier(node.getAttribute(IdentifierNode.ATTRIBUTE_VALUE));
                 addShapeToCurrentShape(shape);
                 break;
             }
             case TERMINAL: {
                 shape = terminal(node.getAttribute(TerminalNode.ATTR_IBUTE_VALUE));
                 addShapeToCurrentShape(shape);
                 break;
             }
             case COMMENT: {
                 // For the moment we ignore comments.
                 return;
             }
             case NULL: {
                 // Ignore null nodes.
                 return;
             }
             default:
                 LOGGER.log(Level.WARNING, String.format("Unsupported node type: '%s'!", node.getType()));
                 return;
         }
 
         currentShapeStack.push(shape);
     }
 
     @Override
     public void afterVisit(final Visitable visitable) {
         if (!currentShapeStack.isEmpty()) {
             currentShapeStack.pop();
         }
     }
 
     @Override
     public RailroadDiagram getResult() {
         return diagram;
     }
 
     private void addShapeToCurrentShape(final Shape shape) {
         final Shape currentShape = currentShapeStack.peek();
 
         if (currentShape instanceof RowLayoutShape) {
             ((RowLayoutShape) currentShape).append(shape);
         } else if (currentShape instanceof ChoiceShape) {
             ((ChoiceShape) currentShape).addChoice(shape);
         } else if (currentShape instanceof LoopShape) {
             ((LoopShape) currentShape).setLooped(shape);
         } else if (currentShape instanceof OptionShape) {
             ((OptionShape) currentShape).setOptional(shape);
         } else if (currentShape instanceof SequenceShape) {
             ((SequenceShape) currentShape).append(shape);
         }
     }
 
 }
