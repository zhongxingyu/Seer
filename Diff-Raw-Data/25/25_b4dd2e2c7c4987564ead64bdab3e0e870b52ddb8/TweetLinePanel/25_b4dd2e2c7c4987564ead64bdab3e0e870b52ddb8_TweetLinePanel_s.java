 package org.softwareFm.displayTweets;
 
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.softwareFm.displayCore.api.DisplayerContext;
 import org.softwareFm.swtBasics.text.ConfigForTitleAnd;
 import org.softwareFm.swtBasics.text.IButtonParent;
 import org.softwareFm.utilities.resources.IResourceGetter;
 import org.softwareFm.utilities.strings.Strings;
 
 public class TweetLinePanel extends Composite implements IButtonParent {
 
 	private final ImageRegistry imageRegistry;
 	private final IResourceGetter resourceGetter;
 	private final Text text;
 
 	public TweetLinePanel(Composite parent, int style, DisplayerContext displayerContext) {
 		super(parent, style);
 		setLayout(new RowLayout());
 		this.imageRegistry = displayerContext.imageRegistry;
 		this.resourceGetter = displayerContext.resourceGetter;
 		text = new Text(this, displayerContext.configForTitleAnd.style);
 		ConfigForTitleAnd config = displayerContext.configForTitleAnd;
 		text.setLayoutData(new RowData(config.titleWidth, config.titleHeight));
 	}
 
 	@Override
 	public Composite getButtonComposite() {
 		return this;
 	}
 
 	@Override
 	public ImageRegistry getImageRegistry() {
 		return imageRegistry;
 	}
 
 	@Override
 	public IResourceGetter getResourceGetter() {
 		return resourceGetter;
 	}
 
 	@Override
 	public void buttonAdded() {
 	}
 
 	public void setText(String newText) {
 		text.setText(Strings.nullSafeToString(newText));
 	}
 
 	public String getText() {
 		return text.getText();
 	}
 
 }
