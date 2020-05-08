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
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
 import org.eclipse.jdt.core.dom.Assignment;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.core.dom.FieldAccess;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
 import org.eclipse.jdt.internal.corext.dom.NodeFinder;
//import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
 import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
 import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
 import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal.DeleteBlockingExitPolicy;
 import org.eclipse.jdt.ui.IWorkingCopyManager;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.IUndoManager;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.text.link.LinkedModeModel;
 import org.eclipse.jface.text.link.LinkedModeUI;
 import org.eclipse.jface.text.link.LinkedPosition;
 import org.eclipse.jface.text.link.LinkedPositionGroup;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ltk.core.refactoring.TextChange;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * 
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class AddPrivateFinalHandler extends AbstractHandler {
 
   private static class Result {
     private Result(List<MethodDeclaration> constructors, FieldDeclaration field) {
       this.constructors = constructors;
       this.field = field;
     }
 
     private final List<MethodDeclaration> constructors;
 
     private final FieldDeclaration field;
   }
 
   private static final String CREATED_VARIABLE_NAME = "object";
 
   private static final String CREATED_VARIABLE_TYPE = "Object";
 
   /**
    * Finds all constructors and fields for a given class.
    */
   private static Result findAllConstructorsAndFields(final ASTNode typeNode) {
     final List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
     final List<MethodDeclaration> constructors =
         new ArrayList<MethodDeclaration>();
     ASTVisitor visitor = new ASTVisitor() {
       
       @Override
       public boolean visit(FieldDeclaration node) {
         fields.add(node);
         return false;
       }
 
       @Override
       public boolean visit(MethodDeclaration node) {
         if (!node.isConstructor()) {
           return false;
         }
         constructors.add(node);
         return false;
       }
 
       @Override
       /**
        * Prevents this from finding constructors from inner classes (and 
        * otherwise wasted effort).
        */
       public boolean visit(TypeDeclaration node) {
         return node == typeNode;
       }
 
     };
     typeNode.accept(visitor);
 
     FieldDeclaration field = fields.get(fields.size() - 1);
     return new Result(constructors, field);
   }
 
   private static Point getPosition(
       CompilationUnitEditor targetCompilationUnitEditor) {
     return targetCompilationUnitEditor.getViewer().getSelectedRange();
   }
 
   private static AbstractTypeDeclaration getTypeNode(
       CompilationUnitEditor targetCompilationUnitEditor) {
     IWorkingCopyManager manager =
         JavaPlugin.getDefault().getWorkingCopyManager();
     ICompilationUnit compilationUnit =
         manager.getWorkingCopy(targetCompilationUnitEditor.getEditorInput());
     CompilationUnit root =
         JavaPlugin.getDefault().getASTProvider().getAST(compilationUnit,
             ASTProvider.WAIT_YES, null);
 
     Point selectedRange = getPosition(targetCompilationUnitEditor);
     NodeFinder finder = new NodeFinder(selectedRange.x, selectedRange.y);
     root.accept(finder);
     ASTNode node = finder.getCoveringNode();
     ASTNode nameNode =
         NodeFinder.perform(root, node.getStartPosition(), node.getLength());
     while (true) {
       
       if (node instanceof TypeDeclaration) {
         TypeDeclaration temp = (TypeDeclaration)node;
         // this is an interface, keep on going
         if (!temp.isInterface()) {
           break;
         }
       }
       if (node instanceof EnumDeclaration) {
         //TODO: enums need non-public constructors
         break;
       }
       
       node = node.getParent();
       if (node == null) {
         // TODO: better error handling -- this is not applicable to this file/selection
         throw new RuntimeException("Not applicable");
       }
     }
     AbstractTypeDeclaration typeNode = (AbstractTypeDeclaration) node;
     
     return typeNode;
   }
 
   private static void go(CompilationUnitEditor targetCompilationUnitEditor)
       throws BadLocationException {
 
     ASTNode typeNode = getTypeNode(targetCompilationUnitEditor);
 
     // Find our nodes again.
     Result stuff = findAllConstructorsAndFields(typeNode);
 
     final List<ASTNode> types = new ArrayList<ASTNode>();
     final List<ASTNode> names = new ArrayList<ASTNode>();
 
     // Member variable find
     FieldDeclaration field = stuff.field;
     ASTVisitor fieldVisitor = new ASTVisitor() {
       @Override
       public boolean visit(SimpleType node) {
         types.add(node);
         return false;
       }
 
       @Override
       public boolean visit(VariableDeclarationFragment node) {
         names.add(node);
         return false;
       }
     };
     field.accept(fieldVisitor);
 
     // Constructor parameter find
     for (MethodDeclaration constructor : stuff.constructors) {
       @SuppressWarnings("unchecked")
       List<SingleVariableDeclaration> parameters = constructor.parameters();
       SingleVariableDeclaration param = parameters.get(parameters.size() - 1);
       ASTVisitor parameterVisitor = new ASTVisitor() {
         @Override
         public boolean visit(SingleVariableDeclaration node) {
           names.add(node.getName());
           types.add(node.getType());
           return false;
         }
       };
       param.accept(parameterVisitor);
     }
     for (MethodDeclaration constructor : stuff.constructors) {
 
       // Body find
       final List<Assignment> assignments = new ArrayList<Assignment>();
       ASTVisitor bodyVisitor = new ASTVisitor() {
         @Override
         public boolean visit(Assignment node) {
           assignments.add(node);
           return false;
         }
       };
       Block body = constructor.getBody();
       body.accept(bodyVisitor);
       ASTVisitor assignmentVisitor = new ASTVisitor() {
         @Override
         public boolean visit(SimpleName node) {
           names.add(node);
           return false;
         }
       };
       assignments.get(assignments.size() - 1).accept(assignmentVisitor);
     }
 
     // Set up linking between edit boxes
     IDocument document =
         JavaUI.getDocumentProvider().getDocument(
             targetCompilationUnitEditor.getEditorInput());
     LinkedPositionGroup typesGroup = new LinkedPositionGroup();
     for (int i = 0; i < types.size(); i++) {
       ASTNode elem = types.get(i);
       typesGroup.addPosition(new LinkedPosition(document, elem
           .getStartPosition(), elem.getLength(), i));
     }
     LinkedPositionGroup namesGroup = new LinkedPositionGroup();
     for (int i = 0; i < names.size(); i++) {
       ASTNode elem = names.get(i);
       namesGroup.addPosition(new LinkedPosition(document, elem
           .getStartPosition(), elem.getLength(), i));
     }
     LinkedModeModel model = new LinkedModeModel();
     model.addGroup(typesGroup);
     model.addGroup(namesGroup);
     model.forceInstall();
     model.addLinkingListener(new EditorHighlightingSynchronizer(
         targetCompilationUnitEditor));
     ITextViewer viewer = targetCompilationUnitEditor.getViewer();
 
     Point selection = viewer.getSelectedRange();
     LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
     ui.setExitPolicy(new DeleteBlockingExitPolicy(document));
     Point selectedRange = getPosition(targetCompilationUnitEditor);
     ui.setExitPosition(viewer, selectedRange.x, selectedRange.y,
         Integer.MAX_VALUE);
     ui.enter();
   }
 
   /**
    * the command has been executed, so extract extract the needed information
    * from the application context.
    */
   @Override
   @SuppressWarnings("unchecked")
   public Object execute(ExecutionEvent event) throws ExecutionException {
     try {
       IWorkbenchWindow window =
           HandlerUtil.getActiveWorkbenchWindowChecked(event);
       IWorkbenchPart activePart = window.getActivePage().getActivePart();
       IEditorPart editor = window.getActivePage().getActiveEditor();
       IWorkingCopyManager manager =
           JavaPlugin.getDefault().getWorkingCopyManager();
       CompilationUnitEditor targetCompilationUnitEditor = null;
       ICompilationUnit compilationUnit = null;
       if (editor == activePart) {
         if (editor instanceof CompilationUnitEditor) {
           targetCompilationUnitEditor = (CompilationUnitEditor) editor;
           compilationUnit =
               manager.getWorkingCopy(targetCompilationUnitEditor
                   .getEditorInput());
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
               compilationUnit =
                   manager.getWorkingCopy(targetCompilationUnitEditor
                       .getEditorInput());
               ASTParser parser = ASTParser.newParser(AST.JLS3);
               parser.setKind(ASTParser.K_COMPILATION_UNIT);
               parser.setSource(compilationUnit);
               final CompilationUnit rootCompilationUnit =
                   (CompilationUnit) parser.createAST(null);
             }
           }
         }
       }
 
 
 
       ITextViewer viewer = targetCompilationUnitEditor.getViewer();
       Point seletion = viewer.getSelectedRange();
 
 
       // Find the containing type.
       AbstractTypeDeclaration typeNode =
           getTypeNode(targetCompilationUnitEditor);
 
       // Common info
       AST ast = typeNode.getRoot().getAST();
       ASTRewrite astRewrite = ASTRewrite.create(ast);
 
       // Construct a member variable declaration.
       SimpleName varType = ast.newSimpleName(CREATED_VARIABLE_TYPE);
       SimpleName varName = ast.newSimpleName(CREATED_VARIABLE_NAME);
       SimpleType variableType = ast.newSimpleType(varType);
       VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
       vdf.setName(varName);
       FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(vdf);
       fieldDeclaration.modifiers().add(
           ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
       fieldDeclaration.modifiers().add(
           ast.newModifier(ModifierKeyword.FINAL_KEYWORD));
       fieldDeclaration.setType(variableType);
       ListRewrite listRewrite =
           astRewrite.getListRewrite(typeNode, typeNode
               .getBodyDeclarationsProperty());
       List<Object> originalList = listRewrite.getOriginalList();
       int lastIndex = 0;
       for (int i = originalList.size() - 1; i >= 0; i--) {
         final Object node = originalList.get(i);
         if (node instanceof FieldDeclaration) {
           lastIndex = i + 1;
           break;
         }
       }
       listRewrite.insertAt(fieldDeclaration, lastIndex, null);
 
       List<MethodDeclaration> constructors = findAllConstructors(typeNode);
 
       // Handle no constructors by creating an empty constructor
       if (constructors.isEmpty()) {
         MethodDeclaration constructor =
             typeNode.getAST().newMethodDeclaration();
         SimpleName name = ast.newSimpleName(typeNode.getName().getIdentifier());
         constructor.setName(name);
         Block body = ast.newBlock();
         constructor.setBody(body);
         constructor.setConstructor(true);
         List modifiers = constructor.modifiers();
         modifiers.add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
         ListRewrite rewrite =
             astRewrite.getListRewrite(typeNode, typeNode
                 .getBodyDeclarationsProperty());
         rewrite.insertFirst(constructor, null);
 
         constructors.add(constructor);
       }
       for (MethodDeclaration constructor : constructors) {
         reWriteConstructor(ast, astRewrite, constructor);
       }
 
 
//      TextChange textChange =
//          new CompilationUnitChange("blar", compilationUnit);
 
       MultiTextEdit multiTextEdit = new MultiTextEdit();
//      textChange.setEdit(multiTextEdit);
 
       IDocument document =
           JavaUI.getDocumentProvider().getDocument(
               targetCompilationUnitEditor.getEditorInput());
       TextEdit edit = astRewrite.rewriteAST(document, null);
       multiTextEdit.addChild(edit);
 
       IUndoManager undoManager =
           ((TextViewer) targetCompilationUnitEditor.getViewer())
               .getUndoManager();
       undoManager.beginCompoundChange();
       multiTextEdit.apply(document);
       undoManager.endCompoundChange();
       go(targetCompilationUnitEditor);
     } catch (BadLocationException e) {
       throw new ExecutionException(
           "Error accessing cursor location in document", e);
     } catch (PartInitException e) {
       throw new ExecutionException("Could not open editor", e);
     }
 
     return null;
   }
 
   //TODO: consolidate with findAllConstructorsAndFields?
   private List<MethodDeclaration> findAllConstructors(final ASTNode typeNode) {
     // Find all constructors.
     final List<MethodDeclaration> constructors =
         new ArrayList<MethodDeclaration>();
     ASTVisitor visitor = new ASTVisitor() {
       @Override
       public boolean visit(MethodDeclaration node) {
         if (!node.isConstructor()) {
           return false;
         }
         constructors.add(node);
         return false;
       }
       
       @Override
       public boolean visit(TypeDeclaration node) {
         return node == typeNode;
       }
       
     };
     typeNode.accept(visitor);
 
     return constructors;
   }
 
   private void reWriteConstructor(AST ast, ASTRewrite astRewrite,
       MethodDeclaration constructor) {
     SimpleName varType;
     SimpleName varName;
     SimpleType variableType;
 
     // Add a parameter
     varType = ast.newSimpleName(CREATED_VARIABLE_TYPE);
     varName = ast.newSimpleName(CREATED_VARIABLE_NAME);
     variableType = ast.newSimpleType(varType);
     SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
     svd.setName(varName);
     svd.setType(variableType);
     ListRewrite parametersRewrite =
         astRewrite.getListRewrite(constructor,
             MethodDeclaration.PARAMETERS_PROPERTY);
     parametersRewrite.insertLast(svd, null);
 
     // Add an assignment statement
     varName = ast.newSimpleName(CREATED_VARIABLE_NAME);
     Block block = constructor.getBody();
     Assignment assignment = ast.newAssignment();
     FieldAccess fieldAccess = ast.newFieldAccess();
     fieldAccess.setExpression(ast.newThisExpression());
     fieldAccess.setName(varName);
     assignment.setLeftHandSide(fieldAccess);
     varName = ast.newSimpleName(CREATED_VARIABLE_NAME);
     assignment.setRightHandSide(varName);
     ExpressionStatement expression = ast.newExpressionStatement(assignment);
     ListRewrite blockRewrite =
         astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
     blockRewrite.insertLast(expression, null);
   }
 }
