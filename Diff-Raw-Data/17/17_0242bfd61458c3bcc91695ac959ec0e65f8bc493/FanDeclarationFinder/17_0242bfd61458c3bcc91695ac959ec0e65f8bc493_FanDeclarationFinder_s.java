 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.handlers;
 
 //import fanx.util.FileUtil;
 import java.io.File;
 import java.util.Hashtable;
import java.util.List;
 import javax.swing.text.Document;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.NBFanParser;
 import net.colar.netbeans.fan.indexer.model.FanDocument;
import net.colar.netbeans.fan.indexer.model.FanSlot;
import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase.VarKind;
 import net.colar.netbeans.fan.scope.FanFieldScopeVar;
 import net.colar.netbeans.fan.scope.FanMethodScopeVar;
 import net.colar.netbeans.fan.scope.FanTypeScopeVar;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.modules.csl.api.DeclarationFinder;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.spi.ParserResult;
 import org.netbeans.modules.parsing.api.Snapshot;
 import org.netbeans.modules.parsing.api.Source;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
 *
  * @author thibautc
  */
 public class FanDeclarationFinder implements DeclarationFinder
 {
 
     public DeclarationLocation findDeclaration(ParserResult result, int caretOffset)
     {
         FanParserTask task = (FanParserTask) result;
         AstNode rootNode = task.getAstTree();
         AstNode curNode = FanLexAstUtils.findASTNodeAt(rootNode, caretOffset);
         String text = FanLexAstUtils.getNodeText(curNode.getParseNode(), task.getParsingResult().inputBuffer).trim();
 
         AstNode scopeNode = FanLexAstUtils.getScopeNode(curNode);
 
         // Try local vars first
         Hashtable<String, FanAstScopeVarBase> vars = scopeNode.getAllScopeVars(false);
         if (vars.containsKey(text))
         {
             FanAstScopeVarBase var = vars.get(text);
             VarKind kind = var.getKind();
             if (kind.isLocal())
             {
                 AstNode nd = var.getType().getScopeNode();
                 FileObject fo = task.getSourceFile();
                 return new DeclarationLocation(fo, nd.getStartLocation().getIndex());
             }
         }
 
         // resolve external
         FanResolvedType t;
         String slot = null;
 
         t = FanResolvedType.resolveCallLeftHandSide(curNode);
 
         if (t != null)
         {
             slot = text;
             // We want to search in the file where the slot is defined (could be inherited)
             t = t.resolveSlotBaseType(slot, task);
         } else
         {
             t = FanResolvedType.resolveInScope(scopeNode, text);
         }
 
         if (!t.isResolved())
         {
             t = FanResolvedType.makeFromTypeSig(scopeNode, text);
         }
 
         if (t.isResolved() && t.getDbType().isFromSource())
         {
             return findTypeDeclaration(t, slot);
         }
 
         // Not found
         return null;
     }
 
     public OffsetRange getReferenceSpan(Document doc, int caretOffset)
     {
         Token tk = FanLexAstUtils.getFanTokenAt(doc, caretOffset);
         TokenHierarchy th = TokenHierarchy.get(doc);
         int start = tk.offset(th);
         int end = start + tk.length();
         OffsetRange range = new OffsetRange(start, end);
         System.out.println("getRefSpan: " + start + "-" + end);
         return range;
     }
 
     private DeclarationLocation findTypeDeclaration(FanResolvedType t, String slot)
     {
         // Find the relevant item in the source
         Long docId = t.getDbType().getSrcDocId();
         FanDocument doc = FanDocument.findById(docId);
         FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(doc.getPath())));
         Source source = Source.create(fo);
         Snapshot snapshot = source.createSnapshot();
         // Parse the snaphot
         NBFanParser parser = new NBFanParser();
         try
         {
             parser.parse(snapshot, true);
         } catch (Throwable e)
         {
             return null;
         }
         FanParserTask fanResult = (FanParserTask) parser.getResult();
         AstNode rootScope = fanResult.getRootScope();
 
         FanTypeScopeVar typeVar = findTypeVar(rootScope, t.getDbType().getSimpleName());
 
         // look for a specific slot
         if (slot != null)
         {
             FanAstScopeVarBase slotVar = findSlot(typeVar, slot);
             if (slotVar != null)
             {
                 return new DeclarationLocation(fo, slotVar.getNode().getStartLocation().getIndex());
             }
         }
 
         // if not asked for a slot .. or the slot not found, go to the type
         if (typeVar != null)
         {
             return new DeclarationLocation(fo, typeVar.getNode().getStartLocation().getIndex());
         }
 
         // not found
         return null;
     }
 
     private FanTypeScopeVar findTypeVar(AstNode rootScope, String name)
     {
         for (FanAstScopeVarBase var : rootScope.getLocalScopeVars().values())
         {
             if (var instanceof FanTypeScopeVar)
             {
                 if (var.getName().equals(name))
                 {
                     return (FanTypeScopeVar) var;
                 }
             }
         }
         return null;
     }
 
     private FanAstScopeVarBase findSlot(FanTypeScopeVar type, String slot)
     {
         for (FanAstScopeVarBase slotVar : type.getNode().getLocalScopeVars().values())
         {
             if (slotVar instanceof FanMethodScopeVar || slotVar instanceof FanFieldScopeVar)
             {
                 if (slotVar.getName().equals(slot))
                 {
                     return slotVar;
                 }
             }
         }
         return null;
     }
 }
