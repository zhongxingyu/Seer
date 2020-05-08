 package org.jikespg.uide.editor;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 import lpg.lpgjavaruntime.IToken;
 import org.eclipse.jdt.internal.ui.JavaPluginImages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.uide.core.ErrorHandler;
 import org.eclipse.uide.defaults.DefaultOutliner;
 import org.eclipse.uide.editor.UniversalEditor;
 import org.eclipse.uide.parser.IParseController;
 import org.eclipse.uide.parser.ParseError;
 import org.jikespg.uide.parser.JikesPGParser;
 import org.jikespg.uide.parser.JikesPGParser.*;
 
 public class Outliner extends DefaultOutliner {
     Stack fItemStack= new Stack();
 
     private final class OutlineVisitor extends JikesPGParser.AbstractVisitor {
 	private StringBuffer fRHSLabel;
 	public void unimplementedVisitor(String s) {
 	    System.out.println(s);
 	}
 
 	public boolean visit(JikesPG n) {
 	    fItemStack.push(createTopItem("Options", n));
 	    return true;
 	}
 	public void endVisit(JikesPG n) {
 	    fItemStack.pop();
 	}
 	public boolean visit(option n) {
 	    Ioption_value value= n.getoption_value();
 
             if (value != null) {
                 if (value instanceof option_value0)
                     createSubItem(symbolImage(n.getSYMBOL()) + " = " + symbolImage(((option_value0) value).getSYMBOL()), n);
                 else if (value instanceof option_value1)
                     createSubItem(symbolImage(n.getSYMBOL()) + " = " + symbolListImage(((option_value1) value).getsymbol_list()), n);
             } else
 		createSubItem(symbolImage(n.getSYMBOL()), n);
             return true;
 	}
 	public boolean visit(AliasSeg n) {
 	    fItemStack.push(createTopItem("Alias", n));
 	    return true;
 	}
 	public void endVisit(AliasSeg n) {
 	    fItemStack.pop();
 	}
 	public boolean visit(DefineSeg n) {
 	    fItemStack.push(createTopItem("Define", n));
 	    return true;
 	}
 	public void endVisit(DefineSeg n) {
 	    fItemStack.pop();
 	}
         public boolean visit(define_segment1 n) {
             createSubItem(symbolImage(n.getmacro_name_symbol()), (ASTNode) n.getmacro_name_symbol());
             return true;
         }
         public boolean visit(eof_segment1 n) {
             fItemStack.push(createTopItem("EOF", n));
             return true;
         }
         public void endVisit(eof_segment1 n) {
             fItemStack.pop();
         }
         public boolean visit(ExportSeg n) {
             fItemStack.push(createTopItem("Export", n));
             return true;
         }
         public void endVisit(ExportSeg n) {
             fItemStack.pop();
         }
         public void endVisit(export_segment1 n) {
             createSubItem(symbolImage(n.getterminal_symbol()), (ASTNode) n.getterminal_symbol());
         }
         public boolean visit(GlobalsSeg n) {
             fItemStack.push(createTopItem("Globals", n));
             return true;
         }
         public void endVisit(GlobalsSeg n) {
             fItemStack.pop();
         }
         public void endVisit(globals_segment1 n) {
             createSubItem(blockImage(n.getaction_segment()), n.getaction_segment());
         }
 	public boolean visit(HeadersSeg n) {
 	    fItemStack.push(createTopItem("Headers", n));
 	    return true;
 	}
 	public void endVisit(HeadersSeg n) {
 	    fItemStack.pop();
 	}
         public void endVisit(include_segment1 n) {
             createTopItem("Include " + symbolImage(n.getSYMBOL()), n.getSYMBOL());
         }
 	public boolean visit(IdentifierSeg n) {
 	    fItemStack.push(createTopItem("Identifiers", n));
 	    return true;
 	}
 	public void endVisit(IdentifierSeg n) {
 	    fItemStack.pop();
 	}
         public void endVisit(start_symbol0 n) {
             createTopItem("Start = " + symbolImage(n), n);
         }
 	public void endVisit(start_symbol1 n) {
             createTopItem("Start = " + symbolImage(n), n);
 	}
 	public boolean visit(TerminalsSeg n) {
 	    fItemStack.push(createTopItem("Terminals", n));
 	    return true;
 	}
 	public void endVisit(TerminalsSeg n) {
 	    fItemStack.pop();
 	}
	public void endVisit(terminals_segment2 n) {
	    String label= nameImage(n.getname()) + " " + producesImage(n.getproduces()) + " " + symbolImage(n.getterminal_symbol());
            createSubItem(label, (ASTNode) n.getterminal_symbol());
	}
	public void endVisit(terminal_symbol0 n) {
	    createSubItem(symbolImage(n), n);
	}
	public void endVisit(terminal_symbol1 n) {
	    createSubItem(n.getLeftIToken().toString(), n);
 	}
 	public boolean visit(AstSeg n) {
 	    fItemStack.push(createTopItem("Ast", n));
 	    return true;
 	}
 	public void endVisit(AstSeg n) {
 	    fItemStack.pop();
 	}
 	public boolean visit(RulesSeg n) {
 	    fItemStack.push(createTopItem("Rules", n));
 	    return true;
 	}
 	public void endVisit(RulesSeg n) {
 	    fItemStack.pop();
 	}
 	public boolean visit(nonTerm n) {
 	    fItemStack.push(createSubItem(symbolImage(n.getSYMBOL()), n));
 	    return true;
 	}
         public void endVisit(nonTerm n) {
             fItemStack.pop();
         }
         public boolean visit(rhs n) {
             fRHSLabel = new StringBuffer();
             return true;
         }
         public void endVisit(rhs n) {
             createSubItem(fRHSLabel.toString(), n);
         }
         public void endVisit(symWithAttrs0 n) {
             fRHSLabel.append(' ');
             fRHSLabel.append(n.getIToken().toString());
         }
         public void endVisit(symWithAttrs1 n) {
             fRHSLabel.append(' ');
             fRHSLabel.append(n.getIToken().toString());
         }
         public void endVisit(symWithAttrs2 n) {
             fRHSLabel.append(' ');
             fRHSLabel.append(n.getSYMBOL().getIToken().toString());
         }
     }
 
     private static final String MESSAGE= "This is the default outliner. Add your own using the UIDE wizard and see class 'org.eclipse.uide.defaults.DefaultOutliner'";
 
     public String producesImage(Iproduces produces) {
 	if (produces instanceof produces0)
 	    return ((produces0) produces).getLeftIToken().toString();
 	else if (produces instanceof produces1)
 	    return ((produces1) produces).getLeftIToken().toString();
 	else if (produces instanceof produces2)
 	    return ((produces2) produces).getLeftIToken().toString();
 	else if (produces instanceof produces3)
 	    return ((produces3) produces).getLeftIToken().toString();
 	else
 	    return "<???>";
     }
 
     public String nameImage(Iname name) {
 	if (name instanceof name0)
 	    return ((name0) name).getLeftIToken().toString();
 	else if (name instanceof name1)
 	    return ((name1) name).getLeftIToken().toString();
 	else if (name instanceof name2)
 	    return "$empty";
 	else if (name instanceof name3)
 	    return "$error";
 	else if (name instanceof name4)
 	    return "$eol";
 	else if (name instanceof name5)
 	    return ((name5) name).getLeftIToken().toString();
 	else
 	    return "<???>";
     }
 
     private String symbolImage(IASTNodeToken symbol) {
 	return symbol.getLeftIToken().toString();
     }
 
     private String symbolListImage(Isymbol_list symbols) {
         SYMBOLList symbolList= (SYMBOLList) symbols;
         StringBuffer buff= new StringBuffer();
         buff.append('(');
         for(int i=0; i < symbolList.size(); i++) {
             if (i > 0) buff.append(',');
             buff.append(symbolImage(symbolList.getSYMBOLAt(i)));
         }
         buff.append(')');
         return buff.toString();
     }
 
     private String blockImage(ASTNodeToken block) {
         return block.getLeftIToken().toString();
     }
 
     public TreeItem createTopItem(String label) {
 	return createTopItem(label, null);
     }
 
     public TreeItem createTopItem(String label, ASTNode n) {
 	TreeItem treeItem= new TreeItem(tree, SWT.NONE);
 	treeItem.setText(label);
 	treeItem.setImage(JavaPluginImages.DESC_MISC_PUBLIC.createImage());
 	if (n != null)
 	    treeItem.setData(n);
 	return treeItem;
     }
 
     public TreeItem createSubItem(String label) {
         return createSubItem(label, null);
     }
 
     public TreeItem createSubItem(String label, ASTNode n) {
 	TreeItem treeItem= new TreeItem((TreeItem) fItemStack.peek(), SWT.NONE);
 	treeItem.setText(label);
         if (n != null)
             treeItem.setData(n);
 	treeItem.setImage(JavaPluginImages.DESC_MISC_PUBLIC.createImage());
 	return treeItem;
     }
 
     public void createOutlinePresentation(IParseController controller, int offset) {
 //	fController= controller;
 	try {
 	    if (controller != null && tree != null) {
 		if (controller.hasErrors() && false) {
 		    createDebugContents(controller);
 		} else {
 		    JikesPG jpg= (JikesPG) controller.getCurrentAst();
 
 		    if (jpg != null) {
 			tree.setRedraw(false);
 			tree.removeAll();
 			fItemStack.clear();
 			jpg.accept(new OutlineVisitor());
 		    }
 		}
 //		tree.setSelection(new TreeItem[] { tree.getItem(new Point(0, 0)) });
 	    }
 	    //selectTreeItemAtTextOffset(offset);
 	} catch (Throwable e) {
 	    ErrorHandler.reportError("Could not generate outline", e);
 	} finally {
 	    if (tree != null)
 		tree.setRedraw(true);
 	}
     }
 
     private void createDebugContents(IParseController controller) {
 	createTopItem(MESSAGE);
 	List errors= controller.getErrors();
 	createTopItem("Found " + errors.size() + " syntax error(s) in input: ");
 	for(Iterator error= errors.iterator(); error.hasNext(); ) {
 	    ParseError pe= (ParseError) error.next();
 	    createTopItem("  " + pe.description);
 	}
 	int count= controller.getParser().getParseStream().getSize();
 	if (count > 1) {
 	createTopItem("Tokens:");
 	for(int n= 1; n < 100 && n < count; n++) {
 	    IToken token= controller.getParser().getParseStream().getTokenAt(n);
 	    String label= n + ": "
 		    + controller.getParser().getParseStream().orderedTerminalSymbols()[token.getKind()] + " = "
 		    + token.toString();
 	    createTopItem(label);
 	}
 	if (count >= 100)
 	    createTopItem("rest of outline truncated...");
 	}
     }
 
     public void setEditor(UniversalEditor editor) {
         System.out.println(editor.getTitle());
     }
 
     public void setTree(Tree tree) {
         super.setTree(tree);
 	this.tree.addSelectionListener(new SelectionListener() {
 	    public void widgetSelected(SelectionEvent e) {
 		TreeItem ti= (TreeItem) e.item;
 		Object data= ti.getData();
 
 		if (data instanceof ASTNode) {
 		    ASTNode node= (ASTNode) ti.getData();
 		    IToken token= node.getLeftIToken();
 
 		    IEditorPart activeEditor= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		    AbstractTextEditor textEditor= (AbstractTextEditor) activeEditor;
 
 		    textEditor.selectAndReveal(token.getStartOffset(), token.getEndOffset()-token.getStartOffset()+1);
 //		    textEditor.setFocus();
 		}
 	    }
 	    public void widgetDefaultSelected(SelectionEvent e) { }
 	});
     }
 }
