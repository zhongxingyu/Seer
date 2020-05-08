 /*
  * Thibaut Colar Nov 25, 2009
  */
 package net.colar.netbeans.fan.hints;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import javax.swing.text.BadLocationException;
 import net.colar.netbeans.fan.FanParserErrorKey;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.indexer.model.FanType;
import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import org.netbeans.modules.csl.api.Error;
 import org.netbeans.modules.csl.api.Hint;
 import org.netbeans.modules.csl.api.HintFix;
 import org.netbeans.modules.csl.api.HintsProvider;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.api.Rule;
 import org.netbeans.modules.csl.api.RuleContext;
 
 /**
  * Provide hints to user
  * @author thibautc
  */
 public class FanHintsProvider implements HintsProvider
 {
 
     public void computeErrors(HintsManager manager, RuleContext ctx, List<Hint> hints, List<Error> errors)
     {
         FanParserTask result = (FanParserTask) ctx.parserResult;
         if (result != null)
         {
             errors.addAll(result.getDiagnostics());
         }
 
         provideHints(ctx, hints, errors);
     }
 
     public void computeHints(HintsManager manager, RuleContext ctx, List<Hint> hints)
     {
         return;
     }
 
     public void computeSuggestions(HintsManager manager, RuleContext ctx, List<Hint> hints, int arg3)
     {
         //FanParserTask result = (FanParserTask) ctx.parserResult;
         //hints.add(new FanImportHint(result.getSourceFile()));
         return;
     }
 
     public void computeSelectionHints(HintsManager manager, RuleContext ctx, List<Hint> hints, int arg3, int arg4)
     {
         return;
     }
 
     public void cancel()
     {
     }
 
     public List<Rule> getBuiltinRules()
     {
         return Collections.<Rule>emptyList();
     }
 
     public RuleContext createRuleContext()
     {
         return new FanHintsRuleContext();
     }
 
     /**
      * Provide hints for some of the errors
      * @param ctx
      * @param hints
      */
     private void provideHints(RuleContext ctx, List<Hint> hints, List<Error> errors)
     {
         //FanParserTask result = (FanParserTask) ctx.parserResult;
         for (Error error : errors)
         {
             FanParserErrorKey key;
             try
             {
                 key = FanParserErrorKey.valueOf(error.getKey());
             } catch (IllegalArgumentException e)
             {
                 return;
             }
             switch (key)
             {
                 case UNKNOWN_TYPE:
                     proposeUsings(error, ctx, hints);
                     proposeCreateType(error, ctx, hints);
                     // TODO: propose to create type in The current file ?
                     break;
                 case UNKNOWN_ITEM:
                     proposeVar(error, ctx, hints);
                     break;
                 /*case UNKNOWN_SLOT:
                 fixes.add(new FanAddUsingFix(ctx));
                 hint = new Hint(new FanHintRule(), "", result.getSourceFile(),
                 new OffsetRange(error.getStartPosition(), error.getEndPosition()), fixes, 25);
                 break;
                  */
                 default:
                     System.err.println("Unknown key: " + key.name());
             }
         }
 
     }
 
     private void proposeCreateType(Error error, RuleContext ctx, List<Hint> hints)
     {
         FanParserTask result = (FanParserTask) ctx.parserResult;
         String type = null;
         try
         {
             type = result.getDocument().getText(error.getStartPosition(), error.getEndPosition() - error.getStartPosition());
         } catch (BadLocationException e)
         {
             return;
         }
         List<HintFix> fixes = new ArrayList<HintFix>();
         fixes.add(new FanCreateTypeFix(ctx, type));
         Hint hint = new Hint(new FanHintRule(), "", result.getSourceFile(),
                 new OffsetRange(error.getStartPosition(), error.getEndPosition()), fixes, 25);
         hints.add(hint);
     }
 
     private void proposeUsings(Error error, RuleContext ctx, List<Hint> hints)
     {
         FanParserTask result = (FanParserTask) ctx.parserResult;
         String type = null;
         try
         {
             type = result.getDocument().getText(error.getStartPosition(), error.getEndPosition() - error.getStartPosition());
         } catch (BadLocationException e)
         {
         }
         if (type != null)
         {
             Collection<FanType> types = FanType.findTypes(type);
             List<HintFix> fixes = new ArrayList<HintFix>();
             for (FanType t : types)
             {
                 // propose adding this specific type
                 String qn = t.getQualifiedName();
                 fixes.add(new FanAddUsingFix(ctx, qn));
                 // also propose adding the whole pod
                 String pod = t.getPod();
                 if (pod != null && pod.length() > 0 && !t.isJava())
                 {
                     fixes.add(new FanAddUsingFix(ctx, pod));
                 }
             }
             if (!fixes.isEmpty())
             {
                 Hint hint = new Hint(new FanHintRule(), "", result.getSourceFile(),
                         new OffsetRange(error.getStartPosition(), error.getEndPosition()), fixes, 25);
                 hints.add(hint);
             }
         }
     }
 
     private void proposeVar(Error error, RuleContext ctx, List<Hint> hints)
     {
         FanParserTask result = (FanParserTask) ctx.parserResult;
 
         AstNode node = FanLexAstUtils.findASTNodeAt(result.getAstTree(), error.getStartPosition());
 
         String var = null;
         try
         {
             var = result.getDocument().getText(error.getStartPosition(), error.getEndPosition() - error.getStartPosition());
         } catch (BadLocationException e)
         {
             return;
         }
        System.err.println("Var: " + var);
 
         List<HintFix> fixes = new ArrayList<HintFix>();
        if (!var.startsWith(".") && var.indexOf("(") == -1)
         {
             fixes.add(new FanCreateFieldFix(node, var));
             fixes.add(new FanCreateMethodFix(node, var));
         }
        /*
            TODO: try to guess:
         *      - field type
         *      - method return type
         *      - method parameters types/names
         * 
            TODO: propose creating field / methods
                - In remote file/types
 
        AstNode call = FanLexAstUtils.findParentNode(node, AstKind.AST_CALL);
        AstNode callExpr = FanLexAstUtils.findParentNode(node, AstKind.AST_CALL_EXPR);
        AstNode assign = FanLexAstUtils.findParentNode(node, AstKind.AST_EXPR_ASSIGN);

         */
 
         Hint hint = new Hint(new FanHintRule(), "", result.getSourceFile(),
                 new OffsetRange(error.getStartPosition(), error.getEndPosition()), fixes, 25);
        if (!fixes.isEmpty())
         {
             hints.add(hint);
         }
     }
 }
