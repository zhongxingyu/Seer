 package fr.hors.limite.photo.common.client.widget;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
 
 public class FitImageCell extends AbstractCell<String> {
 
 	interface FitImageTemplate extends SafeHtmlTemplates {
 		@Template("<img src=\"{0}\" style=\"height:50px;\"/>")
		SafeHtml img(SafeUri url);
 	}
 
 	private static FitImageTemplate template;
 
 	public FitImageCell() {
 		if (template == null)
 			template = GWT.create(FitImageTemplate.class);
 
 	}
 
 	@Override
 	public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
 		if (value != null)
			sb.appendHtmlConstant(template.img(UriUtils.fromTrustedString(value)).asString());
 	};
 
 }
