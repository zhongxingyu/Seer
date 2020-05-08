 /*******************************************************************************
  * Copyright (c) 2007 Google, Inc.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Google, Inc.
  *******************************************************************************/
 package com.google.zoodiac.sourcemenu;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
 import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.MarkerAnnotation;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.internal.core.JavaElement;
 import org.eclipse.jdt.internal.corext.dom.Selection;
 import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;
//import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
 import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
 import org.eclipse.jdt.ui.IWorkingCopyManager;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.IUndoManager;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ltk.core.refactoring.TextChange;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * 
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class ChangeVisibilityHandler extends AbstractHandler {
 
   private static final List<ModifierKeyword> VISIBILITY_MODS =
       new ArrayList<ModifierKeyword>();
 
   static {
     VISIBILITY_MODS.add(ModifierKeyword.PRIVATE_KEYWORD);
     VISIBILITY_MODS.add(ModifierKeyword.PUBLIC_KEYWORD);
     VISIBILITY_MODS.add(ModifierKeyword.PROTECTED_KEYWORD);
   }
 
   public static final class IncreaseVisibilityHandler extends AbstractHandler {
 
     private final ChangeVisibilityHandler changeVisibilityHandler =
         new ChangeVisibilityHandler(1);
 
     @Override
     public Object execute(ExecutionEvent event) throws ExecutionException {
       return changeVisibilityHandler.execute(event);
     }
   }
 
   public static final class DecreaseVisibilityHandler extends AbstractHandler {
 
     private final ChangeVisibilityHandler changeVisibilityHandler =
         new ChangeVisibilityHandler(-1);
 
     @Override
     public Object execute(ExecutionEvent event) throws ExecutionException {
       return changeVisibilityHandler.execute(event);
     }
   }
 
   private final int direction;
 
   public ChangeVisibilityHandler(int direction) {
     this.direction = direction;
   }
 
   /** 
    * This assists the code to deal with the idiosynchasies of the different 
    * types of elements we want to operate on (classes, methods, fields, etc),
    * 
    */
   private interface ListRewriteAcquirer {
     ASTNode getASTNode();
 
     ChildListPropertyDescriptor getPropertyKey();
   }
 
 
   private static final class ListRewriteAcquirerForMethod implements
       ListRewriteAcquirer {
     private final ASTNode method;
 
     public ListRewriteAcquirerForMethod(MethodDeclaration method) {
       this.method = method;
     }
 
     @Override
     public ASTNode getASTNode() {
       return method;
     }
 
     @Override
     public ChildListPropertyDescriptor getPropertyKey() {
       return MethodDeclaration.MODIFIERS2_PROPERTY;
     }
   }
 
   private static final class ListRewriteAcquirerForField implements
       ListRewriteAcquirer {
     private final FieldDeclaration field;
 
     public ListRewriteAcquirerForField(FieldDeclaration field) {
       this.field = field;
     }
 
     @Override
     public ASTNode getASTNode() {
       return field;
     }
 
     @Override
     public ChildListPropertyDescriptor getPropertyKey() {
       return FieldDeclaration.MODIFIERS2_PROPERTY;
     }
   }
 
   private static final class ListRewriteAcquirerForType implements
       ListRewriteAcquirer {
     private final TypeDeclaration field;
 
     public ListRewriteAcquirerForType(TypeDeclaration field) {
       this.field = field;
     }
 
     @Override
     public ChildListPropertyDescriptor getPropertyKey() {
       return TypeDeclaration.MODIFIERS2_PROPERTY;
     }
 
     @Override
     public ASTNode getASTNode() {
       return field;
     }
   }
 
   private static final class ListRewriteAcquirerForEnumDeclaration implements
       ListRewriteAcquirer {
     private final EnumDeclaration field;
 
     public ListRewriteAcquirerForEnumDeclaration(EnumDeclaration enumDeclaration) {
       this.field = enumDeclaration;
     }
 
     @Override
     public ChildListPropertyDescriptor getPropertyKey() {
       return EnumDeclaration.MODIFIERS2_PROPERTY;
     }
 
     @Override
     public ASTNode getASTNode() {
       return field;
     }
   }
 
   private static final class ListRewriteAcquirerForAnnotationTypeDeclaration implements
       ListRewriteAcquirer {
     private final AnnotationTypeDeclaration field;
 
     public ListRewriteAcquirerForAnnotationTypeDeclaration(AnnotationTypeDeclaration field) {
       this.field = field;
     }
 
     @Override
     public ChildListPropertyDescriptor getPropertyKey() {
       return AnnotationTypeDeclaration.MODIFIERS2_PROPERTY;
     }
 
     @Override
     public ASTNode getASTNode() {
       return field;
     }
   }
 
   /**
    * the command has been executed, so extract extract the needed information
    * from the application context.
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException {
     try {
       IWorkbenchWindow window =
           HandlerUtil.getActiveWorkbenchWindowChecked(event);
       IWorkbenchPart activePart = window.getActivePage().getActivePart();
       IEditorPart editor = window.getActivePage().getActiveEditor();
       CompilationUnitEditor targetCompilationUnitEditor = null;
       ICompilationUnit compilationUnit = null;
       ListRewriteAcquirer listRewriteAcquirer = null;
       if (editor == activePart) {
         if (editor instanceof CompilationUnitEditor) {
           targetCompilationUnitEditor = (CompilationUnitEditor) editor;
           IWorkingCopyManager manager =
               JavaPlugin.getDefault().getWorkingCopyManager();
           compilationUnit =
               manager.getWorkingCopy(targetCompilationUnitEditor
                   .getEditorInput());
           listRewriteAcquirer =
               getMethodFieldOrClassDeclaration(targetCompilationUnitEditor,
                   compilationUnit);
         }
       } else {
         if (activePart instanceof PackageExplorerPart) {
           ISelection selection =
               activePart.getSite().getSelectionProvider().getSelection();
           if (selection instanceof IStructuredSelection) {
             IStructuredSelection structuredSelection =
                 (IStructuredSelection) selection;
             Object firstElement = structuredSelection.getFirstElement();
             IEditorPart editorPart =
                 EditorUtility.openInEditor(firstElement, false);
             if (editorPart instanceof CompilationUnitEditor) {
               targetCompilationUnitEditor = (CompilationUnitEditor) editorPart;
               IWorkingCopyManager manager =
                   JavaPlugin.getDefault().getWorkingCopyManager();
               compilationUnit =
                   manager.getWorkingCopy(targetCompilationUnitEditor
                       .getEditorInput());
               ASTParser parser = ASTParser.newParser(AST.JLS3);
               parser.setKind(ASTParser.K_COMPILATION_UNIT);
               parser.setSource(compilationUnit);
               final CompilationUnit rootCompilationUnit =
                   (CompilationUnit) parser.createAST(null);
               ASTNode nodeToOperateOn = null;
               
               while(nodeToOperateOn == null) {
                 nodeToOperateOn = ((JavaElement) firstElement)
                   .findNode(rootCompilationUnit);
               }
               
               listRewriteAcquirer =
                   getMethodFieldOrClassDeclaration(nodeToOperateOn);
             }
           }
         }
       }
 
       if (listRewriteAcquirer == null) {
         return null;
       }
 
       // Create AST
       ASTNode nodeToOperateOn = listRewriteAcquirer.getASTNode();
       AST methodAst = nodeToOperateOn.getAST();
       ASTRewrite astRewrite = ASTRewrite.create(methodAst);
       ListRewrite listRewrite =
           astRewrite.getListRewrite(nodeToOperateOn, listRewriteAcquirer
               .getPropertyKey());
 
//      TextChange textChange =
//          new CompilationUnitChange("blar", compilationUnit);
       MultiTextEdit multiTextEdit = new MultiTextEdit();
//      textChange.setEdit(multiTextEdit);
       multiTextEdit.addChild(changeVisibility(listRewrite,
           targetCompilationUnitEditor, astRewrite));
       // TODO duplicated
       IUndoManager undoManager =
           ((TextViewer) targetCompilationUnitEditor.getViewer())
               .getUndoManager();
       undoManager.beginCompoundChange();
       IDocument document =
           JavaUI.getDocumentProvider().getDocument(
               targetCompilationUnitEditor.getEditorInput());
       multiTextEdit.apply(document);
       undoManager.endCompoundChange();
 
 
       // TODO file is not marked dirty
       // TODO file bug: selected single character after undo
       // TODO file bug: doesn't appear correctly in undo menu
       // TODO make it work for class/enum/annotation
       // TODO Remove references to internal
 
     } catch (Exception e1) {
       e1.printStackTrace();
       throw new ExecutionException(e1.getMessage(), e1);
     }
 
     return null;
   }
 
   private TextEdit changeVisibility(ListRewrite listRewrite,
       CompilationUnitEditor targetCompilationUnitEditor, ASTRewrite astRewrite) {
 
     // Swap modifiers
     List<?>/* either <Modifier> or <MarkerAnnotation> */ originalModifiers = 
       listRewrite.getOriginalList();
     ModifierKeyword foundModifier = null;
     int lastIndexOfMarker = -1;
     int indexOfOriginalModifier = -1;
     int countRemovedSoFar = 0;
     for (int i = 0; i < originalModifiers.size(); i++) {
       Object temp = originalModifiers.get(i);
       if (temp instanceof MarkerAnnotation) {
         lastIndexOfMarker = i - countRemovedSoFar;
         continue;
       }
       if (!(temp instanceof Modifier)) {
         continue;
       }
       Modifier originalModifier = (Modifier)temp;
       ModifierKeyword originalModifierKeyword = originalModifier.getKeyword();
       if (VISIBILITY_MODS.contains(originalModifierKeyword)) {
         listRewrite.remove(originalModifier, null);
         foundModifier = originalModifierKeyword;
         indexOfOriginalModifier = i;
         countRemovedSoFar++;
       }
     }
     ModifierKeyword newModifierKeyWord = modifierFor(foundModifier);
 
     if (newModifierKeyWord != null) {
       ASTNode newModifier = astRewrite.getAST().newModifier(newModifierKeyWord);
       listRewrite.insertAt(newModifier,
           (indexOfOriginalModifier != -1) ? indexOfOriginalModifier : lastIndexOfMarker + 1, null);
     } // else we're making it package protected
 
     IDocument document =
         JavaUI.getDocumentProvider().getDocument(
             targetCompilationUnitEditor.getEditorInput());
     TextEdit edit = astRewrite.rewriteAST(document, null);
     // try {
     // document.replace(0,1,"+");
     // } catch (BadLocationException e) {
     // throw new RuntimeException(e);
     // }
     return edit;
   }
 
   private ModifierKeyword modifierFor(ModifierKeyword foundModifier) {
     int index = VISIBILITY_MODS.indexOf(foundModifier);
     index += direction;
     if (index < -1) {
       index = VISIBILITY_MODS.size() - 1;
     } else if (index >= VISIBILITY_MODS.size()) {
       index = -1;
     }
     if (index == -1) {
       return null;
     }
     return VISIBILITY_MODS.get(index);
   }
 
   private ListRewriteAcquirer getMethodFieldOrClassDeclaration(
       CompilationUnitEditor targetCompilationUnitEditor,
       ICompilationUnit compilationUnit) {
     SelectionAnalyzer selectionAnalyzer =
         getSelectionAnalyzer(targetCompilationUnitEditor);
     ASTParser parser = ASTParser.newParser(AST.JLS3);
     parser.setKind(ASTParser.K_COMPILATION_UNIT);
     parser.setSource(compilationUnit);
 
     // Find the method
     final CompilationUnit rootCompilationUnit =
         (CompilationUnit) parser.createAST(null);
     rootCompilationUnit.accept(selectionAnalyzer);
     ASTNode temp = selectionAnalyzer.getLastCoveringNode();
     return getMethodFieldOrClassDeclaration(temp);
   }
 
   private ListRewriteAcquirer getMethodFieldOrClassDeclaration(ASTNode temp) {
     while (true) {
       if (temp instanceof MethodDeclaration) {
         return new ListRewriteAcquirerForMethod((MethodDeclaration) temp);
       }
       if (temp instanceof FieldDeclaration) {
         return new ListRewriteAcquirerForField((FieldDeclaration) temp);
       }
       if (temp instanceof TypeDeclaration) {
         return new ListRewriteAcquirerForType((TypeDeclaration) temp);
       }
       if (temp instanceof EnumDeclaration) {
         return new ListRewriteAcquirerForEnumDeclaration((EnumDeclaration) temp);
       }
       if (temp instanceof AnnotationTypeDeclaration) {
         return new ListRewriteAcquirerForAnnotationTypeDeclaration(
             (AnnotationTypeDeclaration) temp);
       }
       temp = temp.getParent();
       if (temp == null) {
         return null;
       }
     }
   }
 
   private SelectionAnalyzer getSelectionAnalyzer(
       CompilationUnitEditor targetCompilationUnitEditor) {
     ITextSelection textSelection =
         (ITextSelection) targetCompilationUnitEditor.getSelectionProvider()
             .getSelection();
     Selection selection =
         Selection.createFromStartLength(textSelection.getOffset(),
             textSelection.getLength());
     SelectionAnalyzer selAnalyzer = new SelectionAnalyzer(selection, true);
     return selAnalyzer;
   }
 }
