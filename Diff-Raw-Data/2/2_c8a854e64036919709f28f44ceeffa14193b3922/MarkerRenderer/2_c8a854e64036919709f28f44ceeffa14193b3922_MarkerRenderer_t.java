 /**
  * 
  */
 package org.cotrix.web.manage.client.codelist.codes.marker;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.cotrix.web.common.shared.codelist.UICode;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.safecss.shared.SafeStyles;
 import com.google.gwt.safecss.shared.SafeStylesUtils;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.text.shared.SafeHtmlRenderer;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class MarkerRenderer implements SafeHtmlRenderer<UICode> {
 	
 	interface Template extends SafeHtmlTemplates {
 		@Template("<table style=\"border-collapse:collapse;width:100%;\" height=\"38px\" title=\"{1}\">{0}</table>")
 		SafeHtml table(SafeHtml value, String tooltip);
 		
		@Template("<tr style=\"{0}\"><td>&nbsp;</td></tr>")
 		SafeHtml row(SafeStyles color);
 		
 		@Template("<div style=\"width:100%;height:38px;background-color:#F1F1F1;\">&nbsp;</div>")
 		SafeHtml empty();
 	}
 	
 	private static final Template template = GWT.create(Template.class);
 	private static final SafeHtml EMPTY = template.empty();
 	
 	@Inject
 	private MarkerTypeUtil markerTypeUtil;
 	
 	@Override
 	public SafeHtml render(UICode object) {
 		return toSafeHtml(object);
 	}
 
 	@Override
 	public void render(UICode object, SafeHtmlBuilder builder) {
 		builder.append(toSafeHtml(object));		
 	}
 	
 	private SafeHtml toSafeHtml(UICode code) {
 		if (code == null || code.getAttributes().isEmpty()) return EMPTY;
 		List<MarkerType> markers = markerTypeUtil.resolve(code.getAttributes());
 		if (markers.isEmpty()) return EMPTY;
 		
 		SafeHtmlBuilder rowsBuilder = new SafeHtmlBuilder();
 		StringBuilder tooltipBuilder = new StringBuilder();
 		Iterator<MarkerType> markerIterator = markers.iterator();
 		while(markerIterator.hasNext()) {
 			MarkerType marker = markerIterator.next();
 			rowsBuilder.append(template.row(SafeStylesUtils.forTrustedBackgroundColor(marker.getBackgroundColor())));
 			tooltipBuilder.append(marker.getName());
 			if (markerIterator.hasNext()) tooltipBuilder.append(", ");
 		}
 				
 		return template.table(rowsBuilder.toSafeHtml(), tooltipBuilder.toString());
 	}
 
 }
