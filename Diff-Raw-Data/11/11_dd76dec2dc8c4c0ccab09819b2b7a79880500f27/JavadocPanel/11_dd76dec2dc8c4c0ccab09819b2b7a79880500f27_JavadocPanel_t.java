 package org.softwareFm.displayJavadocAndSource;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.concurrent.Callable;
 
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.softwareFm.displayCore.api.BindingContext;
 import org.softwareFm.displayCore.api.BoundTitleAndTextField;
 import org.softwareFm.displayCore.api.DisplayerContext;
 import org.softwareFm.displayCore.api.DisplayerDetails;
 import org.softwareFm.jdtBinding.api.BindingRipperResult;
 import org.softwareFm.jdtBinding.api.JavaProjects;
 import org.softwareFm.swtBasics.SwtBasicConstants;
 import org.softwareFm.swtBasics.Swts;
 import org.softwareFm.swtBasics.images.IImageButtonListener;
 import org.softwareFm.swtBasics.images.ImageButton;
 import org.softwareFm.swtBasics.images.ImageButtons;
 import org.softwareFm.swtBasics.text.TitleAndTextField;
 import org.softwareFm.utilities.exceptions.WrappedException;
 import org.softwareFm.utilities.strings.Strings;
 
 public class JavadocPanel extends Composite {
 
 	private final ImageButton btnAttach;
 	private final TitleAndTextField txtLocal;
 	private final BoundTitleAndTextField txtRepository;
 	private BindingRipperResult ripped;
 
 	public JavadocPanel(Composite parent, int style, DisplayerContext context, DisplayerDetails displayerDetails) {
 		super(parent, style);
 		setLayout(new GridLayout());
 		txtRepository = new BoundTitleAndTextField(this, context, displayerDetails.withKey(DisplayJavadocConstants.repositoryKey));
 		ImageButtons.addBrowseButton(txtRepository, new Callable<String>() {
 			@Override
 			public String call() throws Exception {
 				return txtRepository.getText();
 			}
 		});
 		txtRepository.addEditButton();
 		btnAttach = ImageButtons.addRowButton(txtRepository, DisplayJavadocConstants.linkImageKey, DisplayJavadocConstants.linkKey, new IImageButtonListener() {
 			@Override
 			public void buttonPressed(ImageButton button) {
 				try {
 					new URL(txtRepository.getText());
 					assert ripped != null;
 					JavaProjects.setJavadoc(ripped.javaProject, ripped.classpathEntry, txtRepository.getText());
 					txtLocal.setText(txtRepository.getText());
 				} catch (MalformedURLException e) {
 					throw WrappedException.wrap(e);
 				}
 
 			}
 		});
 		ImageButtons.addHelpButton(txtRepository, DisplayJavadocConstants.repositoryKey);
 
 		txtLocal = new TitleAndTextField(context.configForTitleAnd, this, DisplayJavadocConstants.localKey);
 		ImageButtons.addRowButton(txtLocal, SwtBasicConstants.clearKey, SourceConstants.clearHelpKey, new IImageButtonListener() {
 			@Override
 			public void buttonPressed(ImageButton button) {
				JavaProjects.setJavadoc(ripped.javaProject, ripped.classpathEntry, "");
 				txtLocal.setText("");
 			}
 		});
 		ImageButtons.addHelpButton(txtLocal, DisplayJavadocConstants.localKey);
 		Swts.addGrabHorizontalAndFillGridDataToAllChildren(this);
 	}
 
 	public void setValue(BindingContext bindingContext, BindingRipperResult ripped) {
 		this.ripped = ripped;
 		Map<String, Object> data = bindingContext.data;
 		String repositoryValue = (String) (data == null ? null : data.get(DisplayJavadocConstants.repositoryKey));
 		txtRepository.setLastBindingContext(bindingContext);
 		txtRepository.setText(Strings.nullSafeToString(repositoryValue));
 		txtLocal.setText(ripped == null || ripped.classpathEntry == null ? null : JavaProjects.findJavadocFor(ripped.classpathEntry));
 		updateButtonStatus(repositoryValue);
 	}
 
 	private void updateButtonStatus(String value) {
 		if ("".equals(value))
 			btnAttach.disableButton(SwtBasicConstants.noValueSet);
 		else {
 			try {
 				new URL(value);
 				btnAttach.enableButton();
 			} catch (MalformedURLException e) {
 				btnAttach.disableButton(DisplayJavadocConstants.valueNeedsToBeUrl);
 			}
 		}
 	}
 
 }
