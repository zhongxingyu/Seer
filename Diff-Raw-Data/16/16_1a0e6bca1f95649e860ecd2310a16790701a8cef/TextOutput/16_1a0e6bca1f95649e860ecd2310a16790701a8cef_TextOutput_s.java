 package net.abesto.jstuki.io;
 
 import java.text.MessageFormat;
 import java.util.Iterator;
 import net.abesto.jstuki.io.TextSyntax.Template;
 
 import net.abesto.jstuki.statements.*;
 
 /**
  *
 * @author abesto
  */
 public class TextOutput {
 
     private TextSyntax syntax;
     private StringBuilder builder;
     private int depth;
 
     public TextOutput(TextSyntax s) {
         syntax = s;
         depth = 0;
         builder = new StringBuilder();
     }
 
     private void indent() {
         for (int i = 0; i < depth; i++) {
             builder.append(syntax.getTemplate(Template.INDENT));
         }
     }
 
     private void format(Template t, String s) {
         String template = syntax.getTemplate(t);
         Object[] args = {s};
         builder.append(MessageFormat.format(template, args));
     }
 
     public String renderProcedure(Procedure procedure) {
         builder.delete(0, builder.length());
 
         format(Template.PROCEDURE, procedure.getLabel());
         depth++;
 
         for (Statement s : procedure.getChildren()) {
             render(s);
         }
 
         depth--;
         format(Template.ENDPROCEDURE, procedure.getLabel());
 
         return builder.toString();
     }
 
     private void renderBinaryConditional(BinaryConditional conditional) {
         indent();
         format(Template.IF, conditional.getLabel());
         depth++;
         render(conditional.getTrueCase());
         depth--;
 
         indent();
         format(Template.ELSE, "");
         depth++;
         render(conditional.getFalseCase());
         depth--;
 
         indent();
         format(Template.ENDIF, "");
     }
 
     private void renderConditional(Conditional conditional) {
         Iterator<ContainerStatement> cases = conditional.getCases().iterator();
 
         renderContainerStatement(cases.next(), Template.IF, Template.NULL);
 
         while (cases.hasNext()) {
             renderContainerStatement(cases.next(), Template.ELSEIF, Template.NULL);
         }
 
         indent();
         format(Template.ENDIF, "");
     }
 
     private void renderIteration(Iteration iteration) {
         renderContainerStatement(iteration, Template.LOOP, Template.ENDLOOP);
     }
 
     private void renderContainerStatement(ContainerStatement container, Template open, Template close) {
         if (open != Template.NULL) {
             indent();
         }
 
         format(open, container.getLabel());
         if (open != Template.NULL) {
             depth++;
         }
 
         for (Statement child : container.getChildren()) {
             render(child);
         }
 
         if (open != Template.NULL) {
             depth--;
         }
 
         if (close != Template.NULL) {
             indent();
         }
         format(close, container.getLabel());
     }
 
     private void renderStatement(Statement statement) {
         indent();
         builder.append(statement.getLabel());
         builder.append("\n");
     }
 
     private void render(Statement s) {
         Class c = s.getClass();
         if (c == BinaryConditional.class) {
             renderBinaryConditional((BinaryConditional) s);
         } else if (c == Conditional.class) {
             renderConditional((Conditional) s);
         } else if (c == ContainerStatement.class) {
             renderContainerStatement((ContainerStatement) s, Template.NULL, Template.NULL);
         } else if (c == Iteration.class) {
             renderIteration((Iteration) s);
         } else {
             renderStatement(s);
         }
     }
 }
