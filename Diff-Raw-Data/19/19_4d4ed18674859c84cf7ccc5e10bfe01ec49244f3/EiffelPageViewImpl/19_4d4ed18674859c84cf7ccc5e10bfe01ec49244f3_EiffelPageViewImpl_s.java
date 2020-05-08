 /**
  * 
  */
 package ch.ethz.e4mooc.client.page.eiffel;
 
 import ch.ethz.e4mooc.client.widgets.cmdline.CmdLineView;
 import ch.ethz.e4mooc.client.widgets.cmdline.CmdLineViewImpl;
 import ch.ethz.e4mooc.client.widgets.editor.EditorView;
 import ch.ethz.e4mooc.client.widgets.editor.EditorViewImpl;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.DivElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.ParagraphElement;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * The simple page only provides 2 input areas and a button.
  * 
  * @author hce
  *
  */
 public class EiffelPageViewImpl extends Composite implements EiffelPageView {
 
 	private static SimplePageViewImplUiBinder uiBinder = GWT.create(SimplePageViewImplUiBinder.class);
 
 	interface SimplePageViewImplUiBinder extends UiBinder<Widget, EiffelPageViewImpl> {
 	}
 	
 	@UiField
 	HTMLPanel mainPanel;
 	@UiField
 	EditorViewImpl editorView;
 	@UiField
 	CmdLineViewImpl cmdLineView;
 	@UiField
 	DivElement outputDiv;
 	
 //	@UiField
 //	TextArea outputText;
 	
 	/** The presenter of this view */
 	private Presenter presenter;
 
 	/**
 	 * Because this class has a default constructor, it can
 	 * be used as a binder template. In other words, it can be used in other
 	 * *.ui.xml files as follows:
 	 * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
 	 *   xmlns:g="urn:import:**user's package**">
 	 *  <g:**UserClassName**>Hello!</g:**UserClassName>
 	 * </ui:UiBinder>
 	 * Note that depending on the widget that is used, it may be necessary to
 	 * implement HasHTML instead of HasText.
 	 */
 	public EiffelPageViewImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	@Override
 	public EditorView getEditorView() {
 		return this.editorView;
 	}
 	
 	@Override
 	public CmdLineView getCmdLineView() {
 		return this.cmdLineView;
 	}
 
 	@Override
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 	
 	@Override
 	public void setOutputText(String text) {
 		
 		// remove any child node from the output-div that represents the output
 		while(outputDiv.hasChildNodes())
 			outputDiv.getFirstChild().removeFromParent();
 		
 		// we trim the text in case of leading or trailing spaces
 		String trimmedText = text.trim();
 		if(trimmedText.startsWith("<")) {
 			// assume that the output-text is in HTML format
 			outputDiv.setInnerHTML(trimmedText);
 		} else {
 			// we assume we have normal text, thus we need add HTML line breaks
 			ParagraphElement p = Document.get().createPElement();
 
 			String htmlText = text.replaceAll("\n", "<br>");
 			htmlText = htmlText.replaceAll(" ", "&nbsp;");
 			
 			p.setInnerHTML(htmlText);
 			outputDiv.appendChild(p);
 		}
 	}
 	
 	public void setDefaultOutputText() {
 		setOutputText("This will display the output!");
 	}
 
 	@Override
 	public void setOutputBoxHeight(int height) {
 		outputDiv.getStyle().setHeight(height, Unit.PX);
 	}
 
 	@Override
 	public void setBackgroundColor(String color) {
		mainPanel.getElement().getStyle().setBackgroundColor(color);
 	}
 }
