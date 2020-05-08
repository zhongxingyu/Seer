 package net.paissad.waqtsalat.ui.components;
 
 import net.paissad.waqtsalat.ui.util.SWTResourceManager;
 
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 
 public class SearchBox extends Composite {
 
     private static final String SEARCH_A_CITY = "Search a city ...";
 
     private Text                text;
     private TableViewer         tableViewer;
     private Table               table;
 
     private Font                blankFont;
     private Font                normalFont;
 
     private boolean             defaultText   = true;
 
     private InputPolicyRule     inputPolicyRule;
 
     public SearchBox(Composite parent, int style) {
 
         super(parent, SWT.NONE);
         setLayout(new GridLayout(1, false));
 
         this.initTextComponent();
 
         tableViewer = new TableViewer(this, SWT.BORDER);
         tableViewer.setUseHashlookup(true);
         table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
         tableViewer.setUseHashlookup(true);
         table = tableViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
         table.setHeaderVisible(true);
         table.setLinesVisible(false);
     }
 
     private void initTextComponent() {
         text = new Text(this, SWT.BORDER | SWT.V_SCROLL | SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
         normalFont = text.getFont();
         FontData normalFontData = this.normalFont.getFontData()[0];
        blankFont = SWTResourceManager.getFont(normalFontData.getName(), normalFontData.getHeight(), SWT.ITALIC);
         text.setFont(blankFont);
         text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
         text.setText(SEARCH_A_CITY);
         text.addModifyListener(new ModifyListener() {
             @Override
             public void modifyText(ModifyEvent e) {
                 if (getInputPolicyRule() != null) {
                     if (tableViewer != null) {
                         tableViewer.setInput(getInputPolicyRule().getInput());
                         tableViewer.refresh();
                     }
                 }
             }
         });
         text.addFocusListener(new FocusListener() {
             @Override
             public void focusLost(FocusEvent e) {
                 updateTextFont();
             }
 
             @Override
             public void focusGained(FocusEvent e) {
                 if (defaultText) text.setText(""); //$NON-NLS-1$
                 text.setFont(normalFont);
             }
         });
     }
 
     private void updateTextFont() {
         if (text != null) {
             defaultText = (getCurrentSearchText() == null || getCurrentSearchText().isEmpty());
             if (defaultText) {
                 text.setFont(blankFont);
             } else {
                 text.setFont(normalFont);
             }
         }
     }
 
     public String getCurrentSearchText() {
         return this.text.getText();
     }
 
     public TableViewer getTableViewer() {
         return this.tableViewer;
     }
 
     public InputPolicyRule getInputPolicyRule() {
         return this.inputPolicyRule;
     }
 
     public void setInputPolicyRule(InputPolicyRule inputPolicyRule) {
         this.inputPolicyRule = inputPolicyRule;
     }
 
     /**
      * Set the table viewer input according the content of the Text component.
      * 
      * @author paissad
      */
     public static interface InputPolicyRule {
 
         /**
          * @return The input to use in the table viewer.
          */
         Object getInput();
 
     }
 
     @Override
     public void dispose() {
         blankFont.dispose();
         normalFont.dispose();
         super.dispose();
     }
 
 }
