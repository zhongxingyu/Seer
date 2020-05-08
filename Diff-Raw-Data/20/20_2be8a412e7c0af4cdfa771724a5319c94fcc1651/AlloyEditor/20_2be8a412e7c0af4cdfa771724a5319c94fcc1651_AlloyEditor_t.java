 package uk.ac.bham.cs.sdsts.editor;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 
 public class AlloyEditor extends EditorPart {
 
 	public static final String ID = "uk.ac.bham.cs.sdsts.editor.AlloyEditor";
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 		
 	}
 	private AlloyEditorInput input;
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		this.input = (AlloyEditorInput) input;
 		setSite(site);
 		setInput(input);
 		setPartName("Alloy Code: " + this.input.getEditorTitle());
 		
 	}
 
 	@Override
 	public boolean isDirty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	private StyledText text;
 
 	@Override
 	public void createPartControl(Composite parent) {
 		
 	    parent.setLayout(new FillLayout());
 
 	    text = new StyledText(parent, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
 	    
 	    text.setEditable(true);
 	    text.setEnabled(true);
 	    text.setText(input.getCodeString());
 	    
 	    text.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				Color blue = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
 				
 			    StyleRange range = new StyleRange(0, text.getText().length(), blue, null);
 			    text.setStyleRange(range);
 			}
 		});
 
 	    //Color blue = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
 	    //StyleRange range = new StyleRange(0, 4, blue, null);
 	    //text.setStyleRange(range);
 
 	}
 	private StyledText getText1(){
 		return text;
 	}
 	public static String getText(){
 		IEditorPart editor1 = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(editor1 instanceof AlloyEditor){
 			AlloyEditor editor = (AlloyEditor) editor1;
 			return editor.getText1().getText();
 		}
 		return "";
 	}
 	public static void selectAll(){
 		IEditorPart editor1 = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(editor1 instanceof AlloyEditor){
 			AlloyEditor editor = (AlloyEditor) editor1;
 			editor.getText1().selectAll();
 		}
 	}
 	private void coloraLineColumn1(int line, int start, int end){
 		line--;
 		start--;
 		end--;
 		end = end - start + 1;
 		String[] strings = text.getText().split("\n");
 		String string = "";
 		for(int i=0; i<line; ++i){
 			string += strings[i] + 1;
 		}
 		start += string.length();
 		//end += string.length();
 		
 		Color blue = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
 		
 	    StyleRange range = new StyleRange(start, end, blue, null);
 	    text.setStyleRange(range);
 	}
 
 	public static void coloraLineColumn(int line, int start, int leng){
 		IEditorPart editor1 = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(editor1 instanceof AlloyEditor){
 			AlloyEditor editor = (AlloyEditor) editor1;
 			editor.coloraLineColumn1(line, start, leng);
 		}
 	}
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
