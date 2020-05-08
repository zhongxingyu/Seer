 package org.softwareFm.displayLists;
 
 import java.util.Arrays;
 import java.util.concurrent.Callable;
 
 import org.eclipse.swt.widgets.Composite;
 import org.softwareFm.displayCore.api.ILineEditable;
 import org.softwareFm.swtBasics.images.ImageButtons;
 import org.softwareFm.swtBasics.text.TitleAndTextField;
 
 public class NameAndUrlLineEditor extends NameAndValueLineEditor {
 
 	public NameAndUrlLineEditor() {
 		super(DisplayListsConstants.urlNameKey, DisplayListsConstants.urlValueKey);
 	}
 
 	@Override
 	protected void addButtons(ILineEditable<NameAndValue> lineEditable, Composite parent, int index, final TitleAndTextField text) {
		ImageButtons.addBrowseButton(text, new Callable<String>() {
 			@Override
 			public String call() throws Exception {
 				return text.getText();
 			}
 		});
 		super.addButtons(lineEditable, parent, index, text);
 	}
 
 	public static void main(String[] args) {
 		ListPanel.show(new NameAndUrlLineEditor(), Arrays.asList("name1$value1", "name2$value2"));
 	}
 }
