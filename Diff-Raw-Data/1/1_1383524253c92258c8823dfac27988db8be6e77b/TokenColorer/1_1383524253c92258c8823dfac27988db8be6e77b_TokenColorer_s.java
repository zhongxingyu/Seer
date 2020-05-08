 package org.eclipse.imp.box.editor;
 
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.services.ITokenColorer;
 import org.eclipse.imp.services.base.TokenColorerBase;
 import org.eclipse.jface.text.TextAttribute;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 
 import org.eclipse.imp.box.parser.BoxParsersym;
 
 import lpg.runtime.IToken;
 
 public class TokenColorer extends TokenColorerBase implements BoxParsersym,
 		ITokenColorer {
 
 	TextAttribute commentAttribute, keywordAttribute, stringAttribute,
 			numberAttribute, doubleAttribute, identifierAttribute;
 
 	public TextAttribute getColoring(IParseController controller, IToken token) {
 		switch (token.getKind()) {
 		case TK_H:
 		case TK_V:
 		case TK_HOV:
 		case TK_HV:
 		case TK_I:
 		case TK_WD:
 			return keywordAttribute;
 		default:
 			return super.getColoring(controller, token);
 		}
 	}
 
 	public TokenColorer() {
 		super();
 		// TODO:  Define text attributes for the various
 		// token types that will have their text colored
 		Display display = Display.getDefault();
 		commentAttribute = new TextAttribute(display
 				.getSystemColor(SWT.COLOR_DARK_RED), null, SWT.ITALIC);
 		stringAttribute = new TextAttribute(display
 				.getSystemColor(SWT.COLOR_DARK_BLUE), null, SWT.BOLD);
 		identifierAttribute = new TextAttribute(display
 				.getSystemColor(SWT.COLOR_BLACK), null, SWT.NORMAL);
 		doubleAttribute = new TextAttribute(display
 				.getSystemColor(SWT.COLOR_DARK_GREEN), null, SWT.BOLD);
 		numberAttribute = new TextAttribute(display
 				.getSystemColor(SWT.COLOR_DARK_YELLOW), null, SWT.BOLD);
 		keywordAttribute = new TextAttribute(display
 				.getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD);
 	}
 
 }
